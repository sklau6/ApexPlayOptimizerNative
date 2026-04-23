package com.apexplayoptimizer.app.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.apexplayoptimizer.app.R
import com.apexplayoptimizer.app.data.BillingManager
import com.apexplayoptimizer.app.data.MonetizationManager
import com.apexplayoptimizer.app.data.ProductIds
import com.apexplayoptimizer.app.data.UserTier
import com.apexplayoptimizer.app.ui.components.BannerAdView
import com.apexplayoptimizer.app.ui.components.NativeAdCard
import com.apexplayoptimizer.app.ui.components.RewardedAdDialog
import com.apexplayoptimizer.app.ui.navigation.Screen
import com.apexplayoptimizer.app.ui.theme.*

private data class StoreItem(
    val id:          String,
    val icon:        String,
    val name:        String,
    val description: String,
    val price:       String,
    val color:       Color,
    val badge:       String? = null
)

private val CREDIT_PACKS = listOf(
    StoreItem(ProductIds.CREDITS_5,  "⚡", "Starter Pack", "5 Boost Credits",  "$0.99", Color(0xFF4C8BF5)),
    StoreItem(ProductIds.CREDITS_20, "⚡", "Value Pack",   "20 Boost Credits", "$2.99", Color(0xFF4C8BF5), badge = "POPULAR"),
    StoreItem(ProductIds.CREDITS_50, "⚡", "Power Pack",   "50 Boost Credits", "$5.99", Color(0xFF8B5CF6)),
)

private val TOKEN_PACKS = listOf(
    StoreItem(ProductIds.TOKENS_10, "🌐", "Ping Lite", "10 Speed Tokens", "$1.49", Color(0xFF14B8A6)),
    StoreItem(ProductIds.TOKENS_50, "🌐", "Ping Pro",  "50 Speed Tokens", "$4.99", Color(0xFF14B8A6), badge = "BEST VALUE"),
)

private val MEGA_PACK = StoreItem(
    id = ProductIds.MEGA_PACK, icon = "🎮", name = "Gaming Power Pack",
    description = "50 Credits + 100 Tokens + 7-day Plus trial",
    price = "$9.99", color = Color(0xFF8B5CF6), badge = "BEST DEAL"
)

@Composable
fun StoreScreen(nav: NavController) {
    val ctx          = LocalContext.current
    val activity     = ctx as? Activity
    var credits      by remember { mutableStateOf(MonetizationManager.getCredits(ctx)) }
    var tokens       by remember { mutableStateOf(MonetizationManager.getTokens(ctx)) }
    var adViews      by remember { mutableStateOf(MonetizationManager.getDailyAdViews(ctx)) }
    val tier         = MonetizationManager.getTier(ctx)
    var showAdDialog by remember { mutableStateOf(false) }
    var purchaseDone by remember { mutableStateOf<String?>(null) }

    // ── Google Play Billing ───────────────────────────────────────────────────
    val billingManager = remember { BillingManager(ctx) }
    val billingState   by billingManager.state.collectAsState()

    DisposableEffect(Unit) {
        billingManager.startConnection()
        onDispose { billingManager.endConnection() }
    }

    LaunchedEffect(billingState.pendingMessage) {
        billingState.pendingMessage?.let { msg ->
            credits      = MonetizationManager.getCredits(ctx)
            tokens       = MonetizationManager.getTokens(ctx)
            purchaseDone = msg
            billingManager.clearPendingMessage()
        }
    }

    // true once billing is connected AND at least the INAPP products are loaded
    val storeReady = billingState.connected && billingState.products.any { p ->
        p.productId in ProductIds.ALL_INAPP
    }

    fun buyItem(item: StoreItem) {
        if (!billingState.connected) {
            Toast.makeText(ctx, "Not connected to Play Store. Check your internet and try again.", Toast.LENGTH_LONG).show()
            billingManager.startConnection()
            return
        }
        val details = billingManager.productDetailsFor(item.id)
        if (activity != null && details != null) {
            billingManager.launchPurchaseFlow(activity, details, null)
        } else {
            Toast.makeText(ctx, "This product is not available yet. Create it in Play Console under In-app products.", Toast.LENGTH_LONG).show()
        }
    }

    if (showAdDialog) {
        RewardedAdDialog(
            rewardCredits = 3,
            rewardTokens  = 0,
            onRewarded = {
                showAdDialog = false
                credits  = MonetizationManager.getCredits(ctx)
                adViews  = MonetizationManager.getDailyAdViews(ctx)
            },
            onDismiss = { showAdDialog = false }
        )
    }

    Column(Modifier.fillMaxSize().background(Background)) {
        // ── Header ──────────────────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().background(Surface).statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            Arrangement.SpaceBetween, Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(Card)
                    .border(1.dp, CardBorder, RoundedCornerShape(18.dp)).clickable { nav.popBackStack() },
                Alignment.Center
            ) { Text("←", fontSize = 18.sp, color = TextPrimary, fontWeight = FontWeight.Bold) }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.store_title), fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = 3.sp)
                Text(stringResource(R.string.store_subtitle), fontSize = 10.sp, color = TextSecondary)
            }

            Box(
                Modifier.clip(RoundedCornerShape(10.dp)).background(Card)
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp)).padding(8.dp, 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚡ $credits", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary)
                    Text("🌐 $tokens", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Teal)
                }
            }
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = 72.dp)) {

            // ── Billing connection banner ─────────────────────────────────────
            if (!storeReady) {
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Orange.copy(0.07f))
                        .border(1.dp, Orange.copy(0.25f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("⏳", fontSize = 16.sp)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                if (billingState.connected) "Loading store products…" else "Connecting to Play Store…",
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Orange
                            )
                            Text(
                                "Prices will appear once the store is ready.",
                                fontSize = 10.sp, color = TextMuted
                            )
                        }
                    }
                }
            } else {
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Success.copy(0.07f))
                        .border(1.dp, Success.copy(0.2f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("✅", fontSize = 14.sp)
                        Text("Play Store connected — tap any item to purchase.", fontSize = 11.sp, color = Success)
                    }
                }
            }

            // ── Purchase success toast ───────────────────────────────────────────
            purchaseDone?.let { name ->
                Box(
                    Modifier.fillMaxWidth().padding(16.dp, 8.dp, 16.dp, 0.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Success.copy(0.1f))
                        .border(1.dp, Success.copy(0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("✅", fontSize = 16.sp)
                        Text(stringResource(R.string.store_purchase_success, name), fontSize = 13.sp, color = Success, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Text("✕", fontSize = 14.sp, color = TextMuted, modifier = Modifier.clickable { purchaseDone = null })
                    }
                }
            }

            // ── Rewarded Ads section ─────────────────────────────────────────────
            StoreSectionHeader(stringResource(R.string.store_section_earn))
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.horizontalGradient(listOf(Primary.copy(0.08f), Purple.copy(0.06f))))
                    .border(1.dp, Primary.copy(0.18f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.store_ad_title), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(stringResource(R.string.store_ad_desc), fontSize = 11.sp, color = TextSecondary)
                        val remaining = MonetizationManager.MAX_DAILY_ADS - adViews
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(MonetizationManager.MAX_DAILY_ADS) { i ->
                                Box(
                                    Modifier.size(8.dp).clip(RoundedCornerShape(4.dp))
                                        .background(if (i < remaining) Primary else TextMuted.copy(0.3f))
                                )
                            }
                            Text(stringResource(R.string.store_ad_remaining, remaining, MonetizationManager.MAX_DAILY_ADS), fontSize = 10.sp, color = TextMuted)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Box(
                        Modifier.clip(RoundedCornerShape(12.dp))
                            .background(
                                if (MonetizationManager.canWatchAd(ctx))
                                    Brush.horizontalGradient(listOf(Primary.copy(0.85f), Blue))
                                else
                                    Brush.horizontalGradient(listOf(TextMuted.copy(0.3f), TextMuted.copy(0.2f)))
                            )
                            .clickable(enabled = MonetizationManager.canWatchAd(ctx)) { showAdDialog = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("▶", fontSize = 18.sp, color = Color.White)
                            Text(stringResource(R.string.store_ad_watch_btn), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Boost Credit packs ───────────────────────────────────────────────
            StoreSectionHeader(stringResource(R.string.store_section_credits))
            if (tier == UserTier.PLUS || tier == UserTier.PRO) {
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(10.dp)).background(Success.copy(0.07f))
                        .border(1.dp, Success.copy(0.2f), RoundedCornerShape(10.dp)).padding(10.dp)
                ) {
                    Text(stringResource(R.string.store_unlimited_boosts, tier.displayName), fontSize = 12.sp, color = Success, fontWeight = FontWeight.SemiBold)
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), Arrangement.spacedBy(10.dp)) {
                CREDIT_PACKS.forEach { item ->
                    StoreItemCard(item, Modifier.weight(1f), storeReady) { buyItem(item) }
                }
            }

            // ── Speed Token packs ────────────────────────────────────────────────
            StoreSectionHeader(stringResource(R.string.store_section_tokens))
            Text(
                stringResource(R.string.store_tokens_desc),
                fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
            )
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), Arrangement.spacedBy(10.dp)) {
                TOKEN_PACKS.forEach { item ->
                    StoreItemCard(item, Modifier.weight(1f), storeReady) { buyItem(item) }
                }
                Spacer(Modifier.weight(1f))
            }

            // ── Mega Pack ────────────────────────────────────────────────────────
            StoreSectionHeader(stringResource(R.string.store_section_bundles))
            MegaPackCard(MEGA_PACK, storeReady) { buyItem(MEGA_PACK) }

            // ── Go Premium CTA ───────────────────────────────────────────────────
            if (tier == UserTier.FREE) {
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.verticalGradient(listOf(Purple.copy(0.12f), Purple.copy(0.05f))))
                        .border(1.dp, Purple.copy(0.22f), RoundedCornerShape(18.dp))
                        .clickable { nav.navigate(Screen.Premium.route) }
                        .padding(16.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(R.string.store_go_premium_title), fontSize = 15.sp, fontWeight = FontWeight.Black, color = Purple)
                            Text(stringResource(R.string.store_go_premium_desc), fontSize = 11.sp, color = TextSecondary)
                        }
                        Text(stringResource(R.string.store_view_plans), fontSize = 12.sp, color = Purple, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }

            // ── Native Advanced Ad ──────────────────────────────────────
            Spacer(Modifier.height(4.dp))
            NativeAdCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(Modifier.height(8.dp))

            // ── Banner Ad ───────────────────────────────────────────────
            BannerAdView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StoreItemCard(item: StoreItem, modifier: Modifier, storeReady: Boolean, onBuy: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(16.dp)).background(Card)
            .border(1.dp, item.color.copy(0.25f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item.badge?.let { badge ->
                Box(
                    Modifier.clip(RoundedCornerShape(5.dp))
                        .background(item.color).padding(horizontal = 6.dp, vertical = 2.dp)
                ) { Text(badge, fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 0.3.sp) }
            } ?: Spacer(Modifier.height(14.dp))
            Text(item.icon, fontSize = 24.sp)
            Text(item.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(item.description, fontSize = 10.sp, color = TextMuted, lineHeight = 14.sp)
            Spacer(Modifier.weight(1f))
            val btnColor  = if (storeReady) item.color else TextMuted
            val btnAlpha  = if (storeReady) 0.12f else 0.07f
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .background(btnColor.copy(btnAlpha))
                    .border(1.dp, btnColor.copy(if (storeReady) 0.3f else 0.15f), RoundedCornerShape(8.dp))
                    .clickable(enabled = storeReady) { onBuy() }
                    .padding(vertical = 8.dp),
                Alignment.Center
            ) {
                Text(
                    if (storeReady) item.price else "⋯",
                    fontSize = 12.sp, fontWeight = FontWeight.Black,
                    color = if (storeReady) item.color else TextMuted
                )
            }
        }
    }
}

@Composable
private fun MegaPackCard(item: StoreItem, storeReady: Boolean, onBuy: () -> Unit) {
    val purple = Purple
    val textMuted = TextMuted
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(purple.copy(0.12f), Primary.copy(0.08f))))
            .border(2.dp, purple.copy(0.35f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = 12.dp)
                ) {
                    Text(item.icon, fontSize = 26.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                            if (item.badge != null) {
                                Box(
                                    Modifier.clip(RoundedCornerShape(5.dp)).background(purple).padding(horizontal = 6.dp, vertical = 2.dp)
                                ) { Text(item.badge, fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.White) }
                            }
                        }
                        Text(item.description, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        if (storeReady) item.price else "⋯",
                        fontSize = 18.sp, fontWeight = FontWeight.Black,
                        color = if (storeReady) purple else textMuted
                    )
                    Box(
                        Modifier.clip(RoundedCornerShape(10.dp))
                            .background(
                                if (storeReady)
                                    Brush.horizontalGradient(listOf(purple.copy(0.8f), purple))
                                else
                                    Brush.horizontalGradient(listOf(textMuted.copy(0.2f), textMuted.copy(0.15f)))
                            )
                            .clickable(enabled = storeReady) { onBuy() }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) { Text(stringResource(R.string.store_buy_btn), fontSize = 13.sp, fontWeight = FontWeight.Black, color = if (storeReady) Color.White else textMuted) }
                }
            }
        }
    }
}

@Composable
private fun StoreSectionHeader(title: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, letterSpacing = 1.sp)
        Box(Modifier.weight(1f).height(1.dp).background(CardBorder))
    }
}

