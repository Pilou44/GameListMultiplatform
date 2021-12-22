package com.wechantloup.gameListManager.common.model

class Platform(
    val gameList: GameList,
    val gameListBackup: GameList?,
    val path: String,
) {

    override fun toString(): String {
        return gameList.provider.system
    }
}