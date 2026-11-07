package com.example.pinjambalik.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.pinjambalik.util.AppConfig
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    BackHandler {
        // Prevent accidental back exit or close smoothly
    }

    val scale = remember { Animatable(0.85f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
        delay(1200L) // Total ~2 seconds splash
        onSplashFinished()
    }

    // Background blue mixed with white gradient as requested
    val splashGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E3A8A), // Deep Royal Blue
            Color(0xFF2563EB), // Vibrant Blue
            Color(0xFF60A5FA), // Light Blue
            Color(0xFFDBEAFE), // Soft Ice Blue
            Color(0xFFFFFFFF)  // Pure White
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(splashGradient)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onSplashFinished()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .scale(scale.value)
        ) {
            // App Logo with modern card elevation
            Surface(
                modifier = Modifier
                    .size(150.dp)
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                color = Color.White
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_pinjambalik_logo),
                        contentDescription = "Pinjam Balik Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(26.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Name
            Text(
                text = AppConfig.APP_NAME,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Tagline
            Text(
                text = "Dokumentasi Sewa & Identitas Pelanggan",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Developer Badge: "by Rafline Dev"
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = Color(0xFF1D4ED8),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFFFDE047),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "by ${AppConfig.DEVELOPER_NAME}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(42.dp))

            // Subtle loading indicator
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color(0xFF1D4ED8),
                strokeWidth = 2.5.dp
            )
        }
    }
}
