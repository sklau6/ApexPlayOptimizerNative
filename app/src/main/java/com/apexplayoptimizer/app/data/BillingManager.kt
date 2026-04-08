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
    const val PLUS_MONTHLY   = "apex_plus_monthly"    // TODO: replace
    const val PLUS_YEARLY    = "apex_plus_yearly"     // TODO: replace
    const val PRO_MONTHLY    = "apex_pro_monthly"     // TODO: replace
    const val PRO_YEARLY     = "apex_pro_yearly"      // TODO: replace

    val ALL_SUBS = listOf(PLUS_MONTHLY, PLUS_YEARLY, PRO_MONTHLY, PRO_YEARLY)
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
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                ProductIds.ALL_SUBS.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                }
            ).build()

        val result = withContext(Dispatchers.IO) { billingClient.queryProductDetails(params) }
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _state.value = _state.value.copy(products = result.productDetailsList ?: emptyList())
            Log.d(TAG, "Loaded ${result.productDetailsList?.size} products")
        } else {
            Log.w(TAG, "queryProductDetails failed: ${result.billingResult.debugMessage}")
        }
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

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails, offerToken: String) {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
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

        // Acknowledge so Google doesn't refund after 3 days
        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            val ackResult = withContext(Dispatchers.IO) { billingClient.acknowledgePurchase(ackParams) }
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
