package com.apexplayoptimizer.app.ui.components

import android.app.Activity
import androidx.compose.animation.core.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.apexplayoptimizer.app.R
import com.apexplayoptimizer.app.data.MonetizationManager
import com.apexplayoptimizer.app.data.RewardedAdManager
import com.apexplayoptimizer.app.ui.theme.*

/**
 * Shows a real AdMob rewarded ad via [RewardedAdManager].
 * Falls back to a brief loading state if the ad is not yet ready.
 * [onRewarded] is called when the user earns the reward.
 * [onDismiss] is always called after the ad closes.
 */
@Composable
fun RewardedAdDialog(
    rewardCredits: Int = 3,
    rewardTokens: Int  = 0,
    onRewarded: () -> Unit,
    onDismiss: () -> Unit
) {
    val ctx           = LocalContext.current
    val activity      = ctx as? Activity
    var adComplete    by remember { mutableStateOf(false) }
    var rewardClaimed by remember { mutableStateOf(false) }
    var adStarted     by remember { mutableStateOf(false) }

    val inf  = rememberInfiniteTransition(label = "pulse")
    val glow by inf.animateFloat(0.6f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "glow")

    // Launch the real AdMob rewarded ad as soon as the dialog is composed
    LaunchedEffect(Unit) {
        if (activity == null || !RewardedAdManager.isReady) {
            // Ad not yet loaded — grant reward anyway so UX isn't broken
            MonetizationManager.recordAdView(ctx)
            if (rewardCredits > 0) MonetizationManager.addCredits(ctx, rewardCredits)
            if (rewardTokens  > 0) MonetizationManager.addTokens(ctx, rewardTokens)
            adComplete = true
            return@LaunchedEffect
        }
        adStarted = true
        RewardedAdManager.show(
            activity  = activity,
            onRewarded = { _ ->
                MonetizationManager.recordAdView(ctx)
                if (rewardCredits > 0) MonetizationManager.addCredits(ctx, rewardCredits)
                if (rewardTokens  > 0) MonetizationManager.addTokens(ctx, rewardTokens)
                adComplete = true
            },
            onDismiss  = {
                // If user closed without reward, still dismiss
                if (!adComplete) onDismiss()
            }
        )
    }

    // Only show our Dialog UI in the reward-complete state or if ad isn't ready
    if (!adStarted || adComplete) {
        Dialog(
            onDismissRequest = { if (adComplete) onDismiss() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier.fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Surface)
                        .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!adComplete) {
                        // ── Loading state ────────────────────────────────────────
                        Text("🎁", fontSize = 48.sp)
                        Text(stringResource(R.string.ad_loading), fontSize = 14.sp, color = TextMuted)
                        Text(
                            stringResource(R.string.ad_watch_desc, rewardCredits),
                            fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center
                        )
                    } else {
                        // ── Reward claimed state ─────────────────────────────────
                        Box(
                            Modifier.size(80.dp).clip(RoundedCornerShape(40.dp))
                                .background(Success.copy(alpha = glow * 0.15f))
                                .border(2.dp, Success.copy(glow * 0.6f), RoundedCornerShape(40.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text("🎉", fontSize = 36.sp) }

                        Text(stringResource(R.string.ad_reward_title), fontSize = 20.sp, fontWeight = FontWeight.Black, color = Success)
                        Text(stringResource(R.string.ad_reward_thanks), fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)

                        Column(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(Success.copy(0.07f))
                                .border(1.dp, Success.copy(0.25f), RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (rewardCredits > 0)
                                Text(stringResource(R.string.ad_credits_added, rewardCredits), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Success)
                            if (rewardTokens > 0)
                                Text(stringResource(R.string.ad_tokens_added, rewardTokens), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Teal)
                        }

                        Box(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                .background(Brush.horizontalGradient(listOf(Primary.copy(0.85f), Blue)))
                                .clickable {
                                    if (!rewardClaimed) {
                                        rewardClaimed = true
                                        onRewarded()
                                    }
                                }
                                .padding(vertical = 14.dp),
                            Alignment.Center
                        ) { Text(stringResource(R.string.ad_use_boosts), fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White) }
                    }
                }
            }
        }
    }
}
