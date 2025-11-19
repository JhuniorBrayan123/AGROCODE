package com.kaquenduri.prueba01_mqtt.utils

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ResponsiveLayout(
    modifier: Modifier = Modifier,
    content: @Composable ResponsiveScope.() -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    val responsiveScope = ResponsiveScope(
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        isTablet = screenWidth >= 600.dp,
        isPhone = screenWidth < 600.dp,
        isLandscape = screenWidth > screenHeight
    )
    
    Box(modifier = modifier) {
        responsiveScope.content()
    }
}

class ResponsiveScope(
    val screenWidth: Dp,
    val screenHeight: Dp,
    val isTablet: Boolean,
    val isPhone: Boolean,
    val isLandscape: Boolean
) {
    @Composable
    fun ResponsiveColumn(
        modifier: Modifier = Modifier,
        spacing: Dp = 16.dp,
        content: @Composable () -> Unit
    ) {
        if (isTablet && isLandscape) {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                content()
            }
        } else {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                content()
            }
        }
    }
    
    @Composable
    fun ResponsiveCard(
        modifier: Modifier = Modifier,
        content: @Composable ColumnScope.() -> Unit
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isTablet) 8.dp else 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(
                    if (isTablet) 24.dp else 16.dp
                )
            ) {
                content()
            }
        }
    }
    
    @Composable
    fun ResponsiveButton(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        content: @Composable RowScope.() -> Unit
    ) {
        Button(
            onClick = onClick,
            modifier = modifier.height(
                if (isTablet) 56.dp else 48.dp
            )
        ) {
            content()
        }
    }
    
    @Composable
    fun ResponsiveText(
        text: String,
        modifier: Modifier = Modifier,
        style: androidx.compose.ui.text.TextStyle = androidx.compose.material3.MaterialTheme.typography.bodyMedium
    ) {
        Text(
            text = text,
            modifier = modifier,
            style = style.copy(
                fontSize = if (isTablet) style.fontSize * 1.2f else style.fontSize
            )
        )
    }
    
    @Composable
    fun ResponsiveSpacing(
        modifier: Modifier = Modifier
    ) {
        Spacer(
            modifier = modifier.height(
                if (isTablet) 24.dp else 16.dp
            )
        )
    }
}

@Composable
fun rememberResponsiveScope(): ResponsiveScope {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    return remember(screenWidth, screenHeight) {
        ResponsiveScope(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            isTablet = screenWidth >= 600.dp,
            isPhone = screenWidth < 600.dp,
            isLandscape = screenWidth > screenHeight
        )
    }
}
