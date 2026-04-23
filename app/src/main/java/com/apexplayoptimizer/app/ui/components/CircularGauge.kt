package com.apexplayoptimizer.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apexplayoptimizer.app.ui.theme.GaugeTrack
import com.apexplayoptimizer.app.ui.theme.TextMuted

@Composable
fun CircularGauge(
    value: Float,
    label: String,
    unit: String = "%",
    color: Color,
    modifier: Modifier = Modifier,
    gaugeSize: Dp = 110.dp,
    strokeWidth: Dp = 10.dp,
    startAngle: Float = 135f,
    sweepTotal: Float = 270f,
    displayValue: String = "${value.toInt()}$unit"
) {
    val gaugeTrack = GaugeTrack
    val textMuted   = TextMuted

    val animVal by animateFloatAsState(
        targetValue = value.coerceIn(0f, 100f),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "gauge_$label"
    )

    Box(modifier.size(gaugeSize), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset  = stroke / 2f
            // Use DrawScope.size (the canvas dimensions) to avoid conflict with our gaugeSize param
            val arcSize  = Size(width = this.size.width - stroke, height = this.size.height - stroke)
            val topLeft  = Offset(inset, inset)

            drawArc(
                color      = gaugeTrack,
                startAngle = startAngle,
                sweepAngle = sweepTotal,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            if (animVal > 0f) {
                drawArc(
                    color      = color,
                    startAngle = startAngle,
                    sweepAngle = sweepTotal * (animVal / 100f),
                    useCenter  = false,
                    topLeft    = topLeft,
                    size       = arcSize,
                    style      = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                displayValue,
                fontSize     = (gaugeSize.value * 0.19f).sp,
                fontWeight   = FontWeight.Black,
                color        = color
            )
            Text(
                label,
                fontSize     = (gaugeSize.value * 0.11f).sp,
                color        = textMuted,
                fontWeight   = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
