package com.bhavans.mybhavans.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhavans.mybhavans.R
import com.bhavans.mybhavans.core.ui.theme.BhavansPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    // ── Phase tracking ──────────────────────────────────────────────
    // Phase 0: initial blank
    // Phase 1: Brand entrance – logo scales up + fades in (0 → 800ms)
    // Phase 2: Morph – bg color changes, logo shrinks & rises, text/card appear (800 → 2200ms)
    // Phase 3: Settle + navigate (2200 → 2800ms)

    // ── Animatable values ───────────────────────────────────────────
    val logoScale = remember { Animatable(0.4f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoSize = remember { Animatable(180f) } // dp
    val logoOffsetY = remember { Animatable(0f) } // dp, negative = up

    val bgMorph = remember { Animatable(0f) } // 0 = white, 1 = gradient

    val titleAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }

    val cardAlpha = remember { Animatable(0f) }
    val cardOffsetY = remember { Animatable(60f) } // slides up

    val buttonAlpha = remember { Animatable(0f) }
    val buttonScaleX = remember { Animatable(0.3f) }

    LaunchedEffect(Unit) {
        // ── PHASE 1: Brand entrance ─────────────────────────────────
        launch { logoAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
        launch { logoScale.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
        // Show title with the logo
        launch {
            delay(300)
            titleAlpha.animateTo(1f, tween(500))
        }

        delay(900)

        // ── PHASE 2: Morph into login layout ────────────────────────
        // Background morph
        launch { bgMorph.animateTo(1f, tween(800, easing = LinearEasing)) }
        // Logo shrinks and moves up
        launch { logoSize.animateTo(80f, tween(700, easing = FastOutSlowInEasing)) }
        launch { logoOffsetY.animateTo(-100f, tween(700, easing = FastOutSlowInEasing)) }
        // Subtitle fades in
        launch {
            delay(200)
            subtitleAlpha.animateTo(1f, tween(400))
        }
        // Card slides up and fades in
        launch {
            delay(300)
            cardAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        }
        launch {
            delay(300)
            cardOffsetY.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        }

        delay(700)

        // ── PHASE 3: Button morph + settle ──────────────────────────
        launch { buttonAlpha.animateTo(1f, tween(400)) }
        launch { buttonScaleX.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }

        delay(700)

        // Navigate
        onSplashComplete()
    }

    // ── Background ──────────────────────────────────────────────────
    val whiteBg = Color.White
    val gradientTop = BhavansPrimary.copy(alpha = 0.1f)
    val gradientBottom = MaterialTheme.colorScheme.background

    // Interpolate background
    val currentTopColor = lerp(whiteBg, gradientTop, bgMorph.value)
    val currentBottomColor = lerp(whiteBg, gradientBottom, bgMorph.value)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(currentTopColor, currentBottomColor)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Logo ────────────────────────────────────────────────
            Image(
                painter = painterResource(id = R.drawable.mybhavans_logo),
                contentDescription = "MyBhavans Logo",
                modifier = Modifier
                    .size(logoSize.value.dp)
                    .offset(y = logoOffsetY.value.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value),
                colorFilter = ColorFilter.tint(Color.Black)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Title – Hero style ──────────────────────────────────
            Text(
                text = "MyBhavans",
                modifier = Modifier
                    .offset(y = logoOffsetY.value.dp)
                    .alpha(titleAlpha.value),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    fontSize = 34.sp
                ),
                color = Color.Black
            )

            // ── Subtitle ────────────────────────────────────────────
            Text(
                text = "Your College Community",
                modifier = Modifier
                    .offset(y = logoOffsetY.value.dp)
                    .alpha(subtitleAlpha.value)
                    .padding(top = 4.dp),
                style = MaterialTheme.typography.bodyLarge.copy(
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── Card preview ────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (logoOffsetY.value + cardOffsetY.value).dp)
                    .alpha(cardAlpha.value),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Placeholder form lines (visual only)
                    repeat(2) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {}
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Sign In button morphing in ──────────────────
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .alpha(buttonAlpha.value)
                            .graphicsLayer {
                                scaleX = buttonScaleX.value
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = BhavansPrimary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Sign In",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Simple color lerp: interpolate between [start] and [end] by [fraction] (0..1).
 */
private fun lerp(start: Color, end: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * f,
        green = start.green + (end.green - start.green) * f,
        blue = start.blue + (end.blue - start.blue) * f,
        alpha = start.alpha + (end.alpha - start.alpha) * f
    )
}
