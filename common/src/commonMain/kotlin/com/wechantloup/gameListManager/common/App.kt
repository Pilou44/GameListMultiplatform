package com.wechantloup.gameListManager.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wechantloup.gameListManager.common.model.Game
import com.wechantloup.gameListManager.common.model.Platform
import com.wechantloup.gameListManager.common.model.Source

@Composable
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }

    Button(onClick = {
        text = "Hello, ${getPlatformName()}"
    }) {
        Text(text)
    }
}


@Composable
fun MainScreen(
    viewModel: MainViewModel,
) {
    val state = viewModel.stateFlow.collectAsState()
    MainScreen(
        sources = state.value.sources,
        platforms = state.value.platforms,
        games = state.value.games,
        onSourceSelected = viewModel::setSource,
        onPlatformSelected = viewModel::setPlatform,
        onForChildClicked = viewModel::onGameSetForKids,
        onFavoriteClicked = viewModel::onGameSetFavorite,
        onCopyBackupClicked = viewModel::copyBackupValues,
        onAllChildClicked = viewModel::setAllForKids,
        onAllFavoriteClicked = viewModel::setAllFavorite,
    )
}

@Composable
fun MainScreen(
    sources: List<Source>,
    platforms: List<Platform>,
    games: List<Game>,
    onSourceSelected: (Source) -> Unit,
    onPlatformSelected: (Platform) -> Unit,
    onForChildClicked: (String, Boolean) -> Unit,
    onFavoriteClicked: (String, Boolean) -> Unit,
    onCopyBackupClicked: () -> Unit,
    onAllChildClicked: () -> Unit,
    onAllFavoriteClicked: () -> Unit,
) {

    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = stringResource(R.string.app_name)
//                    )
//                },
//                backgroundColor = MaterialTheme.colors.surface,
//            )
//        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
//            Dropdown(
//                title = "Sources",
//                values = sources,
//                onValueSelected = onSourceSelected,
//                modifier = Modifier.align(Alignment.CenterHorizontally)
//            )
//            Dropdown(
//                title = "Platforms",
//                values = platforms,
//                onValueSelected = onPlatformSelected,
//                modifier = Modifier.align(Alignment.CenterHorizontally)
//            )
            Header(
                onCopyBackupClicked = onCopyBackupClicked,
                onAllChildClicked = onAllChildClicked,
                onAllFavoriteClicked = onAllFavoriteClicked
            )
            GameListItem(
                modifier = Modifier.weight(1f),
                games = games,
                onForChildClicked = onForChildClicked,
                onFavoriteClicked = onFavoriteClicked,
            )
        }
    }
}

@Composable
fun Header(
    modifier: Modifier = Modifier,
    onCopyBackupClicked: () -> Unit,
    onAllChildClicked: () -> Unit,
    onAllFavoriteClicked: () -> Unit,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        TextButton(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            onClick = onCopyBackupClicked,
        ) {
            Text(
//                text = stringResource(R.string.button_label_copy_backup)
                text = "Copy values from backup"
            )
        }
        IconButton(
            modifier = Modifier
//                .size(dimensionResource(id = R.dimen.space_xl))
                .size(32.dp)
                .align(Alignment.CenterVertically),
            onClick = onAllChildClicked
        ) {
            Icon(
                painter = getChildIcon(),
                contentDescription = "All for kids",
                tint = MaterialTheme.colors.primary,
            )
        }
        IconButton(
            modifier = Modifier
//                .size(dimensionResource(id = R.dimen.space_xl))
                .size(32.dp)
                .align(Alignment.CenterVertically),
            onClick = onAllFavoriteClicked) {
            Icon(
//                painter = painterResource(R.drawable.ic_star_24),
                painter = getStarIcon(),
                contentDescription = "All favorites",
                tint = MaterialTheme.colors.primary,
            )
        }
    }
}

@Composable
fun GameListItem(
    modifier: Modifier = Modifier,
    games: List<Game>,
    onForChildClicked: (String, Boolean) -> Unit,
    onFavoriteClicked: (String, Boolean) -> Unit,
) {
    LazyColumn(modifier) {
        games.forEach { game ->
            item {
                GameItem(
                    game = game,
                    onForChildClicked = { checked -> onForChildClicked(game.id, checked) },
                    onFavoriteClicked = { checked -> onFavoriteClicked(game.id, checked) }
                )
            }
        }
    }
}

@Composable
fun GameItem(
    modifier: Modifier = Modifier,
    game: Game,
    onForChildClicked: (Boolean) -> Unit,
    onFavoriteClicked: (Boolean) -> Unit,
) {
    GameItem(
        modifier = modifier,
        game.name ?: game.path,
        game.kidgame ?: false,
        game.favorite ?: false,
        onForChildClicked,
        onFavoriteClicked,
    )
}

@Composable
fun GameItem(
    modifier: Modifier = Modifier,
    name: String,
    isForChild: Boolean,
    isFavorite: Boolean,
    onForChildClicked: (Boolean) -> Unit,
    onFavoriteClicked: (Boolean) -> Unit,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f)
//                .padding(dimensionResource(R.dimen.space_s)),
                .padding(8.dp),
            text = name,
        )
        Checkbox(
            modifier = Modifier
                .align(Alignment.CenterVertically)
//                .width(dimensionResource(id = R.dimen.space_xl)),
                .width(32.dp),
            checked = isForChild,
            onCheckedChange = onForChildClicked,
        )
        Checkbox(
            modifier = Modifier
                .align(Alignment.CenterVertically)
//                .width(dimensionResource(id = R.dimen.space_xl)),
                .width(32.dp),
            checked = isFavorite,
            onCheckedChange = onFavoriteClicked,
        )
    }
}
