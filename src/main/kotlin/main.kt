import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import model.MainViewModel
import util.TimeUtil
import java.awt.print.Printable
import java.awt.print.PrinterException
import java.awt.print.PrinterJob
import java.io.IOException
import javax.imageio.ImageIO

private val mainViewModel: MainViewModel by lazy { MainViewModel() }

@Composable
fun comboBoxAfterTime(items: List<String>, selectedIndex: Int, selectedIndexChanged: (Int) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column {
        Text(text = "Select term period to Popup Showing")
        Text(
            items[selectedIndex],
            modifier = Modifier.fillMaxWidth().clickable(onClick = { expanded = true }).background(Color.LightGray)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth().background(Color.DarkGray)
        ) {
            items.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    selectedIndexChanged(index)
                    expanded = false
                }) {
                    Text(text = s)
                }
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AutoPrinter",
        state = rememberWindowState(width = 400.dp, height = 300.dp)
    ) {

        val lastPrintTime = mainViewModel.getLastPrintingTime()
        val lastPeriodIndex = mainViewModel.getLastPeriodIndex()

        val needToExit = when (lastPeriodIndex) {
            0 -> TimeUtil.isInWeek(lastPrintTime)
            1 -> TimeUtil.isIn2Weeks(lastPrintTime)
            2 -> TimeUtil.isIn3Weeks(lastPrintTime)
            else -> false
        }

        if (needToExit) {
            exitApplication()
            return@Window
        }

        val scope = rememberCoroutineScope()

        MaterialTheme {
            Column(Modifier.fillMaxSize().padding(10.dp), Arrangement.spacedBy(5.dp)) {
                val periodItems = listOf("1 Week", "2 Weeks", "3 Weeks")
                var txtStatus by rememberSaveable {
                    mutableStateOf("last PrintTime is ${TimeUtil.formatTime(lastPrintTime)}")
                }
                var btnText by rememberSaveable { mutableStateOf("Do Print") }
                var btnEnabled by rememberSaveable { mutableStateOf(true) }
                var periodIndex by rememberSaveable { mutableStateOf(lastPeriodIndex) }

                comboBoxAfterTime(periodItems, periodIndex) { index ->
                    periodIndex = index
                }
                Text(txtStatus)
                Button(modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        if (!btnEnabled) {
                            return@Button
                        }

                        btnEnabled = false
                        scope.launch(Dispatchers.IO) {
                            txtStatus = "Printing..."
                            btnText = "Now working"

                            val job = PrinterJob.getPrinterJob()
                            job.setPrintable { graphics, pageFormat, pageIndex ->
                                val image = try {
                                    ImageIO.read(javaClass.getResource("/sample.png"))
                                } catch (e: IOException) {
                                    println("Not found exception")
                                    null
                                }

                                if (image == null || pageIndex != 0) {
                                    return@setPrintable Printable.NO_SUCH_PAGE
                                }

                                graphics.drawImage(
                                    image,
                                    pageFormat.imageableX.toInt(),
                                    pageFormat.imageableY.toInt(),
                                    pageFormat.imageableWidth.toInt(),
                                    pageFormat.imageableHeight.toInt(),
                                    null
                                )
                                txtStatus = "Image send done"
                                return@setPrintable Printable.PAGE_EXISTS
                            }

                            if (job.printDialog()) {
                                try {
                                    job.print()
                                    mainViewModel.putLastPrintingTime()
                                    mainViewModel.putLastPeriodIndex(periodIndex)
                                } catch (e: PrinterException) {
                                    e.printStackTrace()
                                }
                                exitApplication()
                            } else {
                                println("Canceled?")
                            }

                            btnEnabled = true
                            btnText = "Do Print"

                            println("Hello")
                        }
                    }) {
                    Text(btnText)
                }
            }
        }
    }
}