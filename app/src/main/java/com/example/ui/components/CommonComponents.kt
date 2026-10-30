package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppStrings
import com.example.model.CurrencyHelper
import com.example.model.DateHelper
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryPurple
import kotlin.math.sin

@Composable
fun HisabTopBar(
    selectedYearMonth: String,
    onSelectYearMonth: (String) -> Unit,
    isPrivacyHidden: Boolean,
    onTogglePrivacy: () -> Unit,
    language: String,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var monthMenuExpanded by remember { mutableStateOf(false) }
    val months = remember { DateHelper.getAvailableMonths() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Title
            Text(
                text = AppStrings.get("app_name", language),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.testTag("app_title")
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Eye toggle for privacy
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(1.dp, CircleShape)
                        .testTag("privacy_toggle_button")
                ) {
                    IconButton(
                        onClick = onTogglePrivacy,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (isPrivacyHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Privacy",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Month dropdown selector
                Box {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .height(38.dp)
                            .shadow(1.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { monthMenuExpanded = true }
                            .testTag("month_selector_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = DateHelper.formatYearMonthDisplay(selectedYearMonth),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Month",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = monthMenuExpanded,
                        onDismissRequest = { monthMenuExpanded = false }
                    ) {
                        months.forEach { ym ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = DateHelper.formatYearMonthDisplay(ym),
                                        fontWeight = if (ym == selectedYearMonth) FontWeight.Bold else FontWeight.Normal,
                                        color = if (ym == selectedYearMonth) PrimaryIndigo else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onSelectYearMonth(ym)
                                    monthMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Language toggle pill (BN / EN)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .height(38.dp)
                        .shadow(1.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleLanguage() }
                        .testTag("language_toggle_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = if (language == "bn") "EN" else "BN",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HisabBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    language: String,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        val items = listOf(
            Triple(AppStrings.get("home", language), Icons.Filled.Home, Icons.Outlined.Home),
            Triple(AppStrings.get("budget", language), Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
            Triple(AppStrings.get("transactions", language), Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
            Triple(AppStrings.get("reports", language), Icons.Filled.BarChart, Icons.Outlined.BarChart),
            Triple(AppStrings.get("settings", language), Icons.Filled.Settings, Icons.Outlined.Settings)
        )

        items.forEachIndexed { index, (label, filledIcon, outlinedIcon) ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) filledIcon else outlinedIcon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        maxLines = 1,
                        softWrap = false,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryIndigo,
                    selectedTextColor = PrimaryIndigo,
                    indicatorColor = Color(0xFFEEF2FF),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_tab_$index")
            )
        }
    }
}

@Composable
fun AvailableBalanceCard(
    availableBalance: Double,
    monthIncome: Double,
    monthExpense: Double,
    currencyCode: String,
    selectedYearMonth: String,
    isPrivacyHidden: Boolean,
    language: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF4338CA),
                        Color(0xFF4F46E5),
                        Color(0xFF7C3AED)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.get("available_balance", language),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = DateHelper.formatYearMonthShort(selectedYearMonth),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Balance Text
            Text(
                text = CurrencyHelper.format(availableBalance, currencyCode, isPrivacyHidden),
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp
                ),
                modifier = Modifier.testTag("available_balance_text")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Income / Expense Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Income",
                            tint = Color(0xFF6EE7B7),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = AppStrings.get("month_income", language),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "+${CurrencyHelper.format(monthIncome, currencyCode, isPrivacyHidden)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF6EE7B7),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                // Expense
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = "Expense",
                            tint = Color(0xFFFCA5A5),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = AppStrings.get("month_expense", language),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "-${CurrencyHelper.format(monthExpense, currencyCode, isPrivacyHidden)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFFCA5A5),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun rememberIsOnline(): Boolean {
    val context = androidx.compose.ui.platform.LocalContext.current
    val connectivityManager = remember {
        context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
    }
    var isOnline by remember {
        mutableStateOf(
            connectivityManager?.let { cm ->
                val activeNetwork = cm.activeNetwork ?: return@let false
                val caps = cm.getNetworkCapabilities(activeNetwork) ?: return@let false
                caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } ?: false
        )
    }

    androidx.compose.runtime.DisposableEffect(connectivityManager) {
        if (connectivityManager == null) return@DisposableEffect onDispose {}

        val callback = object : android.net.ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                isOnline = true
            }
            override fun onLost(network: android.net.Network) {
                isOnline = false
            }
        }
        val request = android.net.NetworkRequest.Builder()
            .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        try {
            connectivityManager.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            // ignore
        }

        onDispose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    return isOnline
}

@Composable
fun WaterWaveCanvas(
    modifier: Modifier = Modifier,
    fillLevel: Float = 0.45f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val baseWaterHeight = height * (1f - fillLevel)
        val wavePath1 = Path()
        val wavePath2 = Path()

        val waveLength = (width * 0.8f).coerceAtLeast(10f)
        val amplitude = 22f

        wavePath1.moveTo(0f, height)
        wavePath1.lineTo(0f, baseWaterHeight)

        wavePath2.moveTo(0f, height)
        wavePath2.lineTo(0f, baseWaterHeight + 10f)

        var x = 0f
        while (x <= width) {
            val y1 = baseWaterHeight + amplitude * sin((2 * Math.PI * x / waveLength + phase).toFloat())
            val y2 = baseWaterHeight + amplitude * sin((2 * Math.PI * x / (waveLength * 1.2f) - phase * 0.8f).toFloat())
            wavePath1.lineTo(x, y1)
            wavePath2.lineTo(x, y2)
            x += 10f
        }

        wavePath1.lineTo(width, height)
        wavePath1.close()

        wavePath2.lineTo(width, height)
        wavePath2.close()

        // Layer 2: Deep wave
        drawPath(
            path = wavePath2,
            brush = Brush.verticalGradient(
                colors = listOf(
                    PrimaryIndigo.copy(alpha = 0.35f),
                    PrimaryPurple.copy(alpha = 0.6f)
                ),
                startY = baseWaterHeight,
                endY = height
            )
        )

        // Layer 1: Foreground wave with shimmer
        drawPath(
            path = wavePath1,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF818CF8).copy(alpha = 0.5f),
                    PrimaryIndigo.copy(alpha = 0.85f)
                ),
                startY = baseWaterHeight - 15f,
                endY = height
            )
        )
    }
}

@Composable
fun BannerAdBar(
    modifier: Modifier = Modifier
) {
    val isOnline = rememberIsOnline()
    // Do NOT show ads if offline
    if (!isOnline) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        color = Color(0xFFFFFFFF),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Info icon
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Ad Info",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Ad Badge
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = Color(0xFF334155)
                ) {
                    Text(
                        text = "Ad",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "Google AdMob • Verified Partner",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "Smart Financial Management • Best Rate",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { /* Ad action click */ }
            ) {
                Text(
                    text = "Open",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}
