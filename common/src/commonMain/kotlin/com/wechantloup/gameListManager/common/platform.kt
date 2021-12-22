package com.wechantloup.gameListManager.common

import androidx.compose.ui.graphics.painter.Painter

expect fun getPlatformName(): String
expect fun getChildIcon(): Painter
expect fun getStarIcon(): Painter