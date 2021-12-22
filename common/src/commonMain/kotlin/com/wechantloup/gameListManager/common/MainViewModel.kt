package com.wechantloup.gameListManager.common

import com.google.gson.Gson
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import com.wechantloup.gameListManager.common.model.*
import com.wechantloup.gameListManager.common.utils.JsonToXml
import com.wechantloup.gameListManager.common.utils.XmlToJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.util.EnumSet
import kotlin.coroutines.CoroutineContext

class IOScope : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
}

class MainViewModel() {

    private val _stateFlow = MutableStateFlow(State())
    val stateFlow: StateFlow<State> = _stateFlow

    private val scope = IOScope()

    private val gson = Gson()

    private val gameSources = listOf(
        Source.NAS,
        Source.RETROPIE
    )

    private var share: DiskShare? = null
    private var currentPlatform: Platform? = null

    init {
        _stateFlow.value = stateFlow.value.copy(sources = gameSources)
    }

//    @Suppress("BlockingMethodInNonBlockingContext")
//    override fun onCleared() {
//        viewModelScope.launch(Dispatchers.IO) {
//            share?.close()
//            super.onCleared()
//        }
//    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun setSource(source: Source) {
        scope.launch {
            if (share?.isConnected == true) {
                share?.close()
            }
            share = source.connectTo()
            _stateFlow.value = stateFlow.value.copy(
                platforms = getPlatforms(),
            )
        }
    }

    fun setPlatform(selectedPlatform: Platform) {
        currentPlatform = selectedPlatform
        _stateFlow.value = stateFlow.value.copy(
            games = selectedPlatform.gameList.getGamesCopy(),
            hasBackup = selectedPlatform.gameListBackup != null,
        )
    }

    fun onGameSetForKids(gameId: String, value: Boolean) {
        val platform = currentPlatform ?: return
        platform.gameList.games.first { it.id == gameId }.kidgame = value
        scope.launch { savePlatform(platform) }
    }

    fun onGameSetFavorite(gameId: String, value: Boolean) {
        val platform = currentPlatform ?: return
        platform.gameList.games.first { it.id == gameId }.favorite = value
        scope.launch { savePlatform(platform) }
    }

    fun copyBackupValues() {
        val platform = currentPlatform ?: return
        val gameListBackup = platform.gameListBackup ?: return
        platform.gameList.games.forEach { game ->
            val backup = gameListBackup.games.firstOrNull { it.id == game.id }
            backup?.let {
                game.kidgame = backup.kidgame
                game.favorite = backup.favorite
            }
        }
        scope.launch { savePlatform(platform) }
    }

    fun setAllFavorite() {
        val platform = currentPlatform ?: return
        val allFavorite = platform.gameList.games.all { it.favorite == true }
        platform.gameList.games.forEach {
            it.favorite = !allFavorite
        }
        scope.launch { savePlatform(platform) }
    }

    fun setAllForKids() {
        val platform = currentPlatform ?: return
        val allForKids = platform.gameList.games.all { it.kidgame == true }
        platform.gameList.games.forEach {
            it.kidgame = !allForKids
        }
        scope.launch { savePlatform(platform) }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun savePlatform(platform: Platform) = withContext(Dispatchers.IO) {
        val share = share ?: return@withContext
        val holder = GameListHolder(platform.gameList)
        val path = platform.path
        val newJson = gson.toJson(holder)

        val jsonToXml = JsonToXml.Builder(newJson)
            .forceAttribute("/gameList/game/id")
            .forceAttribute("/gameList/game/source")
            .build()
        val newXml = jsonToXml.toFormattedString(2)

        val logger = KotlinLogging.logger {}
        logger.info { "New xml = $newXml" }

        val outFile = share.openFile(
            path,
            EnumSet.of(AccessMask.GENERIC_WRITE),
            null,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OVERWRITE,
            null)
        val outputStream = outFile.outputStream
        outputStream.use {
            it.write(newXml.toByteArray(Charsets.UTF_8))
        }

        val platforms = getPlatforms()
        val newPlatform = platforms.first { it.path == platform.path }
        currentPlatform = newPlatform

        _stateFlow.value = stateFlow.value.copy(
            games = newPlatform.gameList.getGamesCopy(),
            hasBackup = newPlatform.gameListBackup != null
        )
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun Source.connectTo(): DiskShare? = withContext(Dispatchers.IO) {
            var connection: Connection? = null
            var session: Session? = null
            try {
                val client = SMBClient()
                connection = client.connect(ip)
                val ac = AuthenticationContext(login, password.toCharArray(), "DOMAIN")
                session = connection.authenticate(ac)
                (session.connectShare(path) as DiskShare)
            } catch (e: Exception) {
                e.printStackTrace()
                session?.close()
                connection?.close()
                null
            }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun getPlatforms(): List<Platform> = withContext(Dispatchers.IO) {
        val platforms = mutableListOf<Platform>()
        val share = share ?: return@withContext emptyList()

        for (file in share.listClean("", "*")) {
            val folderName = file.fileName
            if (share.isFolder("", folderName)) {
                val filePath = "$folderName\\$GAMELIST_FILE"
                share.extractGameList(folderName, GAMELIST_FILE)?.let { it ->
                    val gameListBackup = share.extractGameList(folderName, GAMELIST_BACKUP_FILE)
                    platforms.add(Platform(it, gameListBackup, filePath))
                }
            }
        }
        platforms
    }

    private fun DiskShare.extractGameList(folderName: String, fileName: String): GameList? {
        val filePath = "$folderName\\$fileName"

        if (!fileExists(filePath)) return null

        val readFile = openFile(
            filePath,
            EnumSet.of(AccessMask.GENERIC_READ),
            null,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OPEN,
            null
        )

        val inputStream = readFile.inputStream
        val xmlToJson: XmlToJson = XmlToJson.Builder(inputStream, null).build()
        inputStream.close()

        val jsonString: String = xmlToJson.toString()

        val holder = gson.fromJson(jsonString, GameListHolder::class.java)
        return holder.gameList
    }

    private fun DiskShare.isFolder(path: String, fileName: String): Boolean =
        folderExists("$path\\$fileName")

    private fun DiskShare.listClean(path: String, pattern: String) =
        list(path, pattern).filter { it.fileName != "." && it.fileName != ".." }

    data class State(
        val sources: List<Source> = emptyList(),
        val platforms: List<Platform> = emptyList(),
        val games: List<Game> = emptyList(),
        val hasBackup: Boolean = false,
    )

    companion object {
        private const val GAMELIST_FILE = "gamelist.xml"
        private const val GAMELIST_BACKUP_FILE = "gamelist.backup.xml"
    }
}