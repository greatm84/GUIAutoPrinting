import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import model.MainViewModel

private val mainViewModel: MainViewModel by lazy { MainViewModel() }

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AutoPrinter",
        state = rememberWindowState(width = 300.dp, height = 300.dp)
    ) {
        val lastPrintTime = remember { mainViewModel.lastPrintTime }
        MaterialTheme {
            Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
                Button(modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        mainViewModel.putLastPrintingTIme()
                    }) {
                    Text("last PrintTime is ${lastPrintTime.value}")
                }
            }
        }
    }
}