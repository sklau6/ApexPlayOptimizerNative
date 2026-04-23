package com.apexplayoptimizer.app.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ── Product IDs ───────────────────────────────────────────────────────────────
// TODO: Replace with the exact Product IDs you created in the Google Play Console
//       under Monetize → Products → In-app products / Subscriptions.
object ProductIds {
    // Subscriptions
    const val PLUS_MONTHLY   = "apex_plus_monthly"
    const val PLUS_YEARLY    = "apex_plus_yearly"
    const val PRO_MONTHLY    = "apex_pro_monthly"
    const val PRO_YEARLY     = "apex_pro_yearly"

    val ALL_SUBS = listOf(PLUS_MONTHLY, PLUS_YEARLY, PRO_MONTHLY, PRO_YEARLY)

    // Consumable in-app products (Store packs)
    const val CREDITS_5  = "apex_credits_5"
    const val CREDITS_20 = "apex_credits_20"
    const val CREDITS_50 = "apex_credits_50"
    const val TOKENS_10  = "apex_tokens_10"
    const val TOKENS_50  = "apex_tokens_50"
    const val MEGA_PACK  = "apex_mega_pack"

    val ALL_INAPP = listOf(CREDITS_5, CREDITS_20, CREDITS_50, TOKENS_10, TOKENS_50, MEGA_PACK)

    /** Returns (credits, tokens) granted for a consumable product. */
    fun grant(productId: String): Pair<Int, Int> = when (productId) {
        CREDITS_5  ->  5 to 0
        CREDITS_20 -> 20 to 0
        CREDITS_50 -> 50 to 0
        TOKENS_10  ->  0 to 10
        TOKENS_50  ->  0 to 50
        MEGA_PACK  -> 50 to 100
        else       ->  0 to 0
    }
}

private const val TAG = "BillingManager"

// ── State ─────────────────────────────────────────────────────────────────────

data class BillingState(
    val connected:        Boolean                     = false,
    val products:         List<ProductDetails>        = emptyList(),
    val activePurchases:  List<Purchase>              = emptyList(),
    val pendingMessage:   String?                     = null,
)

// ── Manager ───────────────────────────────────────────────────────────────────

class BillingManager(private val ctx: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _state = MutableStateFlow(BillingState())
    val state: StateFlow<BillingState> = _state

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            scope.launch { purchases.forEach { handlePurchase(it) } }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User cancelled purchase")
        } else {
            Log.w(TAG, "Purchase update error: ${billingResult.debugMessage}")
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(ctx)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    // ── Connection ────────────────────────────────────────────────────────────

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "BillingClient connected")
                    _state.value = _state.value.copy(connected = true)
                    scope.launch {
                        loadProducts()
                        restorePurchases()
                    }
                } else {
                    Log.w(TAG, "BillingClient setup failed: ${result.debugMessage}")
                }
            }
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "BillingClient disconnected — reconnecting")
                _state.value = _state.value.copy(connected = false)
                startConnection()
            }
        })
    }

    fun endConnection() = billingClient.endConnection()

    // ── Load products ─────────────────────────────────────────────────────────

    private suspend fun loadProducts() {
        fun buildParams(ids: List<String>, type: String) =
            QueryProductDetailsParams.newBuilder().setProductList(
                ids.map { QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it).setProductType(type).build() }
            ).build()

        val subsResult  = withContext(Dispatchers.IO) { billingClient.queryProductDetails(buildParams(ProductIds.ALL_SUBS,   BillingClient.ProductType.SUBS))  }
        val inappResult = withContext(Dispatchers.IO) { billingClient.queryProductDetails(buildParams(ProductIds.ALL_INAPP,  BillingClient.ProductType.INAPP)) }
        val all = (subsResult.productDetailsList  ?: emptyList()) +
                  (inappResult.productDetailsList ?: emptyList())
        _state.value = _state.value.copy(products = all)
        Log.d(TAG, "Loaded ${all.size} products (subs + inapp)")
    }

    // ── Restore purchases ─────────────────────────────────────────────────────

    suspend fun restorePurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val result = withContext(Dispatchers.IO) { billingClient.queryPurchasesAsync(params) }
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val active = result.purchasesList.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            _state.value = _state.value.copy(activePurchases = active)
            active.forEach { applyEntitlement(it) }
            Log.d(TAG, "Restored ${active.size} active subscriptions")
        }
    }

    // ── Launch purchase flow ──────────────────────────────────────────────────

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails, offerToken: String?) {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .also { if (offerToken != null) it.setOfferToken(offerToken) }
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()
        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "launchBillingFlow failed: ${result.debugMessage}")
        }
    }

    // ── Handle & acknowledge purchase ─────────────────────────────────────────

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        val productId = purchase.products.firstOrNull() ?: return

        if (productId in ProductIds.ALL_INAPP) {
            // Consumable — consume so it can be re-purchased, then grant rewards
            val consumeResult = withContext(Dispatchers.IO) {
                billingClient.consumePurchase(
                    ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                )
            }
            if (consumeResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val (credits, tokens) = ProductIds.grant(productId)
                if (credits > 0) MonetizationManager.addCredits(ctx, credits)
                if (tokens  > 0) MonetizationManager.addTokens(ctx, tokens)
                val msg = buildString {
                    if (credits > 0) append("+$credits Boost Credits")
                    if (tokens  > 0) { if (isNotEmpty()) append(" · "); append("+$tokens Speed Tokens") }
                    append(" added!")
                }
                _state.value = _state.value.copy(pendingMessage = msg)
                Log.d(TAG, "Consumable granted: $msg")
            } else {
                Log.w(TAG, "Consume failed: ${consumeResult.billingResult.debugMessage}")
            }
        } else {
            // Subscription — acknowledge so Google doesn't refund after 3 days
            if (!purchase.isAcknowledged) {
                val ackResult = withContext(Dispatchers.IO) {
                    billingClient.acknowledgePurchase(
                        AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                    )
                }
                if (ackResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.w(TAG, "Acknowledge failed: ${ackResult.debugMessage}")
                    return
                }
            }
            applyEntitlement(purchase)
            val current = _state.value.activePurchases.toMutableList()
            if (current.none { it.purchaseToken == purchase.purchaseToken }) current.add(purchase)
            _state.value = _state.value.copy(
                activePurchases = current,
                pendingMessage  = "Purchase successful! Your plan has been activated."
            )
        }
    }

    // ── Map purchase → UserTier and persist ───────────────────────────────────

    private fun applyEntitlement(purchase: Purchase) {
        val productId = purchase.products.firstOrNull() ?: return
        val tier = when (productId) {
            ProductIds.PLUS_MONTHLY, ProductIds.PLUS_YEARLY -> UserTier.PLUS
            ProductIds.PRO_MONTHLY,  ProductIds.PRO_YEARLY  -> UserTier.PRO
            else                                             -> return
        }
        MonetizationManager.setTier(ctx, tier)
        Log.d(TAG, "Entitlement applied: $tier for product $productId")
    }

    fun clearPendingMessage() {
        _state.value = _state.value.copy(pendingMessage = null)
    }

    // ── Convenience: find ProductDetails by ID ────────────────────────────────

    fun productDetailsFor(productId: String): ProductDetails? =
        _state.value.products.find { it.productId == productId }

    /** Returns the cheapest subscription offer token for a given product, or null. */
    fun offerTokenFor(productDetails: ProductDetails): String? =
        productDetails.subscriptionOfferDetails
            ?.minByOrNull { offer ->
                offer.pricingPhases.pricingPhaseList
                    .sumOf { it.priceAmountMicros }
            }?.offerToken
}
