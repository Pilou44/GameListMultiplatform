package com.wechantloup.gameListManager.common

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable

@Preview
@Composable
fun AppPreview() {
    App()
}

@Preview
@Composable
fun HeaderPreview() {
    Header(
        onCopyBackupClicked = {},
        onAllChildClicked = {},
        onAllFavoriteClicked = {},
    )
}

@Preview
@Composable
fun GameItemPreview() {
    GameItem(
        name = "Sonic",
        isForChild = true,
        isFavorite = false,
        onForChildClicked = {},
        onFavoriteClicked = {},
    )
}

//@Preview
//@Composable
//fun DropdownPreview() {
//    Dropdown(
//        title = "Games",
//        values = listOf("Sonic", "Sonic2"),
//        onValueSelected = {}
//    )
//}