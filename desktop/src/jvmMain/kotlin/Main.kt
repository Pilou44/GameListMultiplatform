import com.wechantloup.gameListManager.common.MainScreen
import com.wechantloup.gameListManager.common.MainViewModel
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.res.svgResource

fun main() = application {
    val viewModel = MainViewModel()
    Window(onCloseRequest = ::exitApplication) {
        DesktopMaterialTheme {
            MainScreen(viewModel)
        }
    }
}