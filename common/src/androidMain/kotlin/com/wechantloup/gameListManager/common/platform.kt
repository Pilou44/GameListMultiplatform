package com.wechantloup.gameListManager.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

actual fun getPlatformName(): String {
    return "Android"
}

@Composable
actual fun getChildIcon() = painterResource(R.drawable.ic_child_24)

@Composable
actual fun getStarIcon() = painterResource(R.drawable.ic_star_24)
