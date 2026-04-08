package com.apexplayoptimizer.app.ui.screens

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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.apexplayoptimizer.app.R
import com.apexplayoptimizer.app.data.MonetizationManager
import com.apexplayoptimizer.app.data.UserTier
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
    val badge:       String? = null,
    val credits:     Int = 0,
    val tokens:      Int = 0
)

private data class Partner(
    val icon:    String,
    val name:    String,
    val tagline: String,
    val deal:    String,
    val color:   Color,
    val url:     String
)

private val CREDIT_PACKS = listOf(
    StoreItem("c5",  "⚡", "Starter Pack",  "5 Boost Credits",      "$0.99",  Primary,                credits = 5),
    StoreItem("c20", "⚡", "Value Pack",    "20 Boost Credits",     "$2.99",  Blue,   badge = "POPULAR", credits = 20),
    StoreItem("c50", "⚡", "Power Pack",    "50 Boost Credits",     "$5.99",  Purple,                 credits = 50),
)

private val TOKEN_PACKS = listOf(
    StoreItem("t10", "🌐", "Ping Lite",      "10 Speed Tokens",      "$1.49",  Teal,                    tokens = 10),
    StoreItem("t50", "🌐", "Ping Pro",       "50 Speed Tokens",      "$4.99",  Teal,   badge = "BEST VALUE", tokens = 50),
)

private val MEGA_PACK = StoreItem(
    id = "mega", icon = "🎮", name = "Gaming Power Pack",
    description = "50 Credits + 100 Tokens + 7-day Plus trial",
    price = "$9.99", color = Purple, badge = "BEST DEAL",
    credits = 50, tokens = 100
)

private val PARTNERS = listOf(
    Partner("🖱", "ProGear",      "Elite Gaming Peripherals",     "15% off with code APEX15",   Primary, "https://progear.gg/apex"),
    Partner("⚡", "VoltEnergy",   "Fuel Your Gaming Sessions",    "Free can with any order",    Orange,  "https://voltenergy.com/apex"),
    Partner("🎧", "SoundCore",    "Immersive Gaming Audio",       "Free shipping on all orders", Teal,  "https://soundcore.com/apex"),
)

@Composable
fun StoreScreen(nav: NavController) {
    val ctx          = LocalContext.current
    val uriHandler   = LocalUriHandler.current
    var credits      by remember { mutableStateOf(MonetizationManager.getCredits(ctx)) }
    var tokens       by remember { mutableStateOf(MonetizationManager.getTokens(ctx)) }
    var adViews      by remember { mutableStateOf(MonetizationManager.getDailyAdViews(ctx)) }
    val tier                = MonetizationManager.getTier(ctx)
    var showAdDialog by remember { mutableStateOf(false) }
    var purchaseItem by remember { mutableStateOf<StoreItem?>(null) }
    var purchaseDone by remember { mutableStateOf<String?>(null) }

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

    // Simulated purchase confirmation dialog
    purchaseItem?.let { item ->
        PurchaseConfirmDialog(item = item,
            onConfirm = {
                MonetizationManager.addCredits(ctx, item.credits)
                MonetizationManager.addTokens(ctx, item.tokens)
                credits = MonetizationManager.getCredits(ctx)
                tokens  = MonetizationManager.getTokens(ctx)
                purchaseDone = item.name
                purchaseItem = null
            },
            onDismiss = { purchaseItem = null }
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
                    StoreItemCard(item, Modifier.weight(1f)) { purchaseItem = item }
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
                    StoreItemCard(item, Modifier.weight(1f)) { purchaseItem = item }
                }
                Spacer(Modifier.weight(1f))
            }

            // ── Mega Pack ────────────────────────────────────────────────────────
            StoreSectionHeader(stringResource(R.string.store_section_bundles))
            MegaPackCard(MEGA_PACK) { purchaseItem = MEGA_PACK }

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
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(R.string.store_go_premium_title), fontSize = 15.sp, fontWeight = FontWeight.Black, color = Purple)
                            Text(stringResource(R.string.store_go_premium_desc), fontSize = 11.sp, color = TextSecondary)
                        }
                        Text(stringResource(R.string.store_view_plans), fontSize = 12.sp, color = Purple, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Partner Deals ────────────────────────────────────────────────────
            StoreSectionHeader(stringResource(R.string.store_section_partners))
            Text(
                stringResource(R.string.store_partners_desc),
                fontSize = 11.sp, color = TextMuted,
                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
            )
            PARTNERS.forEach { partner ->
                PartnerCard(partner, uriHandler)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun StoreItemCard(item: StoreItem, modifier: Modifier, onBuy: () -> Unit) {
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
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .background(item.color.copy(0.12f))
                    .border(1.dp, item.color.copy(0.3f), RoundedCornerShape(8.dp))
                    .clickable { onBuy() }
                    .padding(vertical = 8.dp),
                Alignment.Center
            ) { Text(item.price, fontSize = 12.sp, fontWeight = FontWeight.Black, color = item.color) }
        }
    }
}

@Composable
private fun MegaPackCard(item: StoreItem, onBuy: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(Purple.copy(0.12f), Primary.copy(0.08f))))
            .border(2.dp, Purple.copy(0.35f), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.icon, fontSize = 28.sp)
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                            Box(
                                Modifier.clip(RoundedCornerShape(5.dp)).background(Purple).padding(horizontal = 6.dp, vertical = 2.dp)
                            ) { Text(item.badge ?: "", fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.White) }
                        }
                        Text(item.description, fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(item.price, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Purple)
                Box(
                    Modifier.clip(RoundedCornerShape(10.dp))
                        .background(Brush.horizontalGradient(listOf(Purple.copy(0.8f), Purple)))
                        .clickable { onBuy() }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) { Text(stringResource(R.string.store_buy_btn), fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White) }
            }
        }
    }
}

@Composable
private fun PartnerCard(partner: Partner, uriHandler: androidx.compose.ui.platform.UriHandler) {
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(16.dp)).background(Card)
            .border(1.dp, partner.color.copy(0.2f), RoundedCornerShape(16.dp))
            .clickable { uriHandler.openUri(partner.url) }
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                        .background(partner.color.copy(0.12f))
                        .border(1.dp, partner.color.copy(0.25f), RoundedCornerShape(12.dp)),
                    Alignment.Center
                ) { Text(partner.icon, fontSize = 20.sp) }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(partner.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(partner.tagline, fontSize = 11.sp, color = TextSecondary)
                    Box(
                        Modifier.clip(RoundedCornerShape(4.dp))
                            .background(partner.color.copy(0.1f)).padding(horizontal = 6.dp, vertical = 2.dp)
                    ) { Text(partner.deal, fontSize = 10.sp, color = partner.color, fontWeight = FontWeight.SemiBold) }
                }
            }
            Text("›", fontSize = 20.sp, color = TextMuted, fontWeight = FontWeight.Bold)
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

@Composable
private fun PurchaseConfirmDialog(item: StoreItem, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)).background(Surface)
                .border(1.dp, item.color.copy(0.3f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(item.icon, fontSize = 40.sp)
                Text(item.name, fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                Text(item.description, fontSize = 12.sp, color = TextSecondary)
                Box(Modifier.fillMaxWidth().height(1.dp).background(CardBorder))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.store_total), fontSize = 13.sp, color = TextMuted)
                    Text(item.price, fontSize = 16.sp, fontWeight = FontWeight.Black, color = item.color)
                }
                Text(
                    stringResource(R.string.store_payment_secure),
                    fontSize = 10.sp, color = TextMuted,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                            .background(Card).border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                            .clickable { onDismiss() }.padding(vertical = 13.dp),
                        Alignment.Center
                    ) { Text(stringResource(R.string.store_cancel), fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.SemiBold) }
                    Box(
                        Modifier.weight(2f).clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(item.color.copy(0.85f), item.color)))
                            .clickable { onConfirm() }.padding(vertical = 13.dp),
                        Alignment.Center
                    ) { Text(stringResource(R.string.store_buy_price, item.price), fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White) }
                }
            }
        }
    }
}
