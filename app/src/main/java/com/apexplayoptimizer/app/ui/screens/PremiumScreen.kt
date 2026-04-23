package com.apexplayoptimizer.app.ui.screens

import android.app.Activity
import androidx.annotation.StringRes
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.apexplayoptimizer.app.R
import com.apexplayoptimizer.app.data.BillingManager
import com.apexplayoptimizer.app.data.MonetizationManager
import com.apexplayoptimizer.app.data.ProductIds
import com.apexplayoptimizer.app.data.UserTier
import com.apexplayoptimizer.app.ui.theme.*
import kotlinx.coroutines.launch

private data class PlanFeature(@StringRes val textRes: Int, val included: Boolean)

private data class Plan(
    val tier:           UserTier,
    val monthlyFull:    String,
    val yearlyFull:     String,
    @StringRes val yearlyNoteRes: Int,
    @StringRes val badgeRes: Int?,
    val color:          Color,
    val features:       List<PlanFeature>
)

private val PLANS = listOf(
    Plan(
        tier = UserTier.FREE, monthlyFull = "Free", yearlyFull = "Free",
        yearlyNoteRes = R.string.premium_always_free, badgeRes = null, color = Color(0xFF4A5568),
        features = listOf(
            PlanFeature(R.string.plan_free_f1, true),
            PlanFeature(R.string.plan_free_f2, true),
            PlanFeature(R.string.plan_free_f3, true),
            PlanFeature(R.string.plan_free_f4, true),
            PlanFeature(R.string.plan_free_f5, true),
            PlanFeature(R.string.plan_free_f6, false),
            PlanFeature(R.string.plan_free_f7, false),
            PlanFeature(R.string.plan_free_f8, false),
            PlanFeature(R.string.plan_free_f9, false),
        )
    ),
    Plan(
        tier = UserTier.PLUS, monthlyFull = "\$4.99/mo", yearlyFull = "\$3.33/mo",
        yearlyNoteRes = R.string.plan_plus_yearly_note, badgeRes = R.string.premium_badge_popular, color = Color(0xFF4C8BF5),
        features = listOf(
            PlanFeature(R.string.plan_plus_f1, true),
            PlanFeature(R.string.plan_plus_f2, true),
            PlanFeature(R.string.plan_plus_f3, true),
            PlanFeature(R.string.plan_plus_f4, true),
            PlanFeature(R.string.plan_plus_f5, true),
            PlanFeature(R.string.plan_plus_f6, true),
            PlanFeature(R.string.plan_plus_f7, false),
            PlanFeature(R.string.plan_plus_f8, false),
            PlanFeature(R.string.plan_plus_f9, false),
        )
    ),
    Plan(
        tier = UserTier.PRO, monthlyFull = "\$29.99/mo", yearlyFull = "\$19.99/mo",
        yearlyNoteRes = R.string.plan_pro_yearly_note, badgeRes = R.string.badge_pro, color = Color(0xFF8B5CF6),
        features = listOf(
            PlanFeature(R.string.plan_pro_f1, true),
            PlanFeature(R.string.plan_pro_f2, true),
            PlanFeature(R.string.plan_pro_f3, true),
            PlanFeature(R.string.plan_pro_f4, true),
            PlanFeature(R.string.plan_pro_f5, true),
            PlanFeature(R.string.plan_pro_f6, true),
            PlanFeature(R.string.plan_pro_f7, true),
            PlanFeature(R.string.plan_pro_f8, true),
        )
    ),
)

@Composable
fun PremiumScreen(nav: NavController) {
    val ctx          = LocalContext.current
    val activity     = ctx as? Activity
    val scope        = rememberCoroutineScope()
    var currentTier  by rememberUserTierLocal(ctx)
    var billingCycle by remember { mutableStateOf("yearly") }

    // ── Billing setup ────────────────────────────────────────────────────────
    val billingManager = remember { BillingManager(ctx) }
    val billingState   by billingManager.state.collectAsState()

    DisposableEffect(Unit) {
        billingManager.startConnection()
        onDispose { billingManager.endConnection() }
    }

    // Refresh tier whenever active purchases change
    LaunchedEffect(billingState.activePurchases) {
        currentTier = MonetizationManager.getTier(ctx)
    }

    // Show pending success message and refresh tier
    LaunchedEffect(billingState.pendingMessage) {
        if (billingState.pendingMessage != null) {
            currentTier = MonetizationManager.getTier(ctx)
            billingManager.clearPendingMessage()
        }
    }

    Column(Modifier.fillMaxSize().background(Background)) {
        // ── Header ──────────────────────────────────────────────────────────────
        Box(
            Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Purple.copy(0.20f), Background)))
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(Card)
                    .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                    .clickable { nav.popBackStack() }.align(Alignment.CenterStart),
                Alignment.Center
            ) { Text("←", fontSize = 18.sp, color = TextPrimary, fontWeight = FontWeight.Bold) }

            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.premium_title),
                    fontSize = 16.sp, fontWeight = FontWeight.Black,
                    color = TextPrimary, letterSpacing = 2.sp
                )
                Text(stringResource(R.string.premium_subtitle), fontSize = 11.sp, color = TextSecondary)
            }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 32.dp)
        ) {
            // ── Current plan banner ──────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth().padding(16.dp, 8.dp, 16.dp, 0.dp)
                    .clip(RoundedCornerShape(12.dp)).background(Card)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp, 11.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val dot = when (currentTier) { UserTier.PRO -> Purple; UserTier.PLUS -> Primary; else -> TextMuted }
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(dot))
                    Text(stringResource(R.string.premium_current_plan, currentTier.displayName), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Text(
                    if (currentTier == UserTier.FREE) stringResource(R.string.premium_upgrade) else stringResource(R.string.premium_active),
                    fontSize = 12.sp,
                    color = if (currentTier == UserTier.FREE) Primary else Success,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Billing toggle ───────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp)).background(Card)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                listOf("monthly" to stringResource(R.string.billing_monthly), "yearly" to stringResource(R.string.billing_yearly)).forEach { (key, label) ->
                    val sel = billingCycle == key
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .background(if (sel) Primary else Color.Transparent)
                            .clickable { billingCycle = key }
                            .padding(vertical = 11.dp),
                        Alignment.Center
                    ) {
                        Text(
                            label,
                            fontSize = 11.sp,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                            color = if (sel) Color.White else TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Plan cards ──────────────────────────────────────────────────────
            PLANS.forEach { plan ->
                PlanCard(
                    plan         = plan,
                    billingCycle = billingCycle,
                    currentTier  = currentTier,
                    onSelect     = { selected ->
                        if (selected.tier == UserTier.FREE) return@PlanCard
                        // Map selected plan + billing cycle to a Product ID
                        val productId = when (selected.tier) {
                            UserTier.PLUS -> if (billingCycle == "yearly") ProductIds.PLUS_YEARLY else ProductIds.PLUS_MONTHLY
                            UserTier.PRO  -> if (billingCycle == "yearly") ProductIds.PRO_YEARLY  else ProductIds.PRO_MONTHLY
                            else          -> null
                        }
                        val details = productId?.let { billingManager.productDetailsFor(it) }
                        val token   = details?.let { billingManager.offerTokenFor(it) }

                        if (activity != null && details != null && token != null) {
                            billingManager.launchPurchaseFlow(activity, details, token)
                        } else if (!billingState.connected) {
                            Toast.makeText(
                                ctx,
                                "Not connected to Play Store. Check your internet and try again.",
                                Toast.LENGTH_LONG
                            ).show()
                            billingManager.startConnection()
                        } else {
                            Toast.makeText(
                                ctx,
                                "Products are not available yet. Please ensure subscriptions are created in Play Console.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
                Spacer(Modifier.height(10.dp))
            }

            // ── Compliance footer ────────────────────────────────────────────────
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    stringResource(R.string.premium_restore),
                    fontSize = 13.sp, color = Primary, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        scope.launch { billingManager.restorePurchases() }
                    }
                )
                Text(
                    stringResource(R.string.premium_auto_renew),
                    fontSize = 10.sp, color = TextMuted, textAlign = TextAlign.Center, lineHeight = 15.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.premium_terms), fontSize = 10.sp, color = TextMuted)
                    Text("·", fontSize = 10.sp, color = TextMuted)
                    Text(stringResource(R.string.premium_privacy), fontSize = 10.sp, color = TextMuted)
                }
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan:        Plan,
    billingCycle: String,
    currentTier: UserTier,
    onSelect:    (Plan) -> Unit
) {
    val isActive = plan.tier == currentTier
    val price    = if (billingCycle == "yearly") plan.yearlyFull else plan.monthlyFull
    val note     = if (billingCycle == "yearly") stringResource(plan.yearlyNoteRes) else ""

    Box(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) plan.color.copy(0.07f) else Card)
            .border(
                if (isActive) 2.dp else 1.dp,
                if (isActive) plan.color.copy(0.55f) else CardBorder,
                RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Plan header row
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            plan.tier.displayName.uppercase(),
                            fontSize = 17.sp, fontWeight = FontWeight.Black, color = plan.color
                        )
                        plan.badgeRes?.let { badgeRes ->
                            Box(
                                Modifier.clip(RoundedCornerShape(6.dp))
                                    .background(plan.color).padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(stringResource(badgeRes), fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 0.5.sp)
                            }
                        }
                    }
                    Text(price, fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                    if (note.isNotEmpty()) Text(note, fontSize = 10.sp, color = TextMuted)
                }
                if (isActive) {
                    Box(
                        Modifier.clip(RoundedCornerShape(8.dp))
                            .background(plan.color.copy(0.15f))
                            .border(1.dp, plan.color.copy(0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) { Text(stringResource(R.string.badge_active), fontSize = 9.sp, fontWeight = FontWeight.Black, color = plan.color, letterSpacing = 1.sp) }
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(CardBorder))

            // Features list
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                plan.features.forEach { feat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Text(
                            if (feat.included) "✓" else "✗",
                            fontSize = 12.sp,
                            color = if (feat.included) Success else TextMuted.copy(0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(feat.textRes),
                            fontSize = 12.sp,
                            color = if (feat.included) TextSecondary else TextMuted.copy(0.5f)
                        )
                    }
                }
            }

            // CTA button
            when {
                isActive && plan.tier != UserTier.FREE -> {
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(Card)
                            .border(1.dp, plan.color.copy(0.3f), RoundedCornerShape(12.dp))
                            .padding(vertical = 12.dp),
                        Alignment.Center
                    ) { Text(stringResource(R.string.premium_cta_current), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = plan.color) }
                }
                plan.tier == UserTier.PRO -> {
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(Purple.copy(0.8f), Purple)))
                            .clickable { onSelect(plan) }
                            .padding(vertical = 14.dp),
                        Alignment.Center
                    ) {
                        Text(stringResource(R.string.premium_cta_get_pro), fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
                plan.tier == UserTier.PLUS -> {
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(Primary.copy(0.85f), Blue)))
                            .clickable { onSelect(plan) }
                            .padding(vertical = 14.dp),
                        Alignment.Center
                    ) { Text(stringResource(R.string.premium_cta_get_plus), fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White) }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun rememberUserTierLocal(ctx: android.content.Context): MutableState<UserTier> =
    remember { mutableStateOf(MonetizationManager.getTier(ctx)) }
