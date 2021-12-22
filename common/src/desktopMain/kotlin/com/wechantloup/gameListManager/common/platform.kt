package com.wechantloup.gameListManager.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

actual fun getPlatformName(): String {
    return "Desktop"
}

@Composable
actual fun getChildIcon() = painterResource("images/teddy-bear.svg")

@Composable
actual fun getStarIcon() = painterResource("images/star-outline.svg")
