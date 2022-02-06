import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import java.net.URL
import java.time.Duration
import java.time.Instant
import javax.imageio.ImageIO

private val mainViewModel: MainViewModel by lazy { MainViewModel() }

enum class PrintStatus {
    NONE, BEGIN, ERROR, DONE
}

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

        val lastTimeInst = Instant.ofEpochMilli(lastPrintTime)
        val nowTimeInst = Instant.now()

        val elapsedTime = Duration.between(lastTimeInst, nowTimeInst)

        val scope = rememberCoroutineScope()

        MaterialTheme {
            Column(Modifier.fillMaxSize().padding(10.dp), Arrangement.spacedBy(5.dp)) {
                val periodItems = listOf("1 Week", "2 Weeks", "3 Weeks")
                var txtStatus by rememberSaveable {
                    mutableStateOf(
                        "last PrintTime is ${TimeUtil.formatTime(lastPrintTime)} " +
                                " (${elapsedTime.toDays()} 일 전)"
                    )
                }
                var btnText by rememberSaveable { mutableStateOf("Do Print") }
                var btnEnabled by rememberSaveable { mutableStateOf(true) }
                var periodIndex by rememberSaveable { mutableStateOf(lastPeriodIndex) }

                comboBoxAfterTime(periodItems, periodIndex) { index ->
                    periodIndex = index
                }
                Text(txtStatus)
                Row(Modifier.fillMaxWidth()) {
                    Button(onClick = {
                        mainViewModel.putLastPrintingTime()
                        mainViewModel.putLastPeriodIndex(periodIndex)
                        exitApplication()
                    }) {
                        Text(text = "Skip Now")
                    }
                    Button(
                        onClick = {
                            if (!btnEnabled) {
                                return@Button
                            }

                            btnEnabled = false
                            scope.launch(Dispatchers.IO) {
                                printWork(javaClass.getResource("/sample.png")) { printStatus ->
                                    when (printStatus) {
                                        PrintStatus.NONE -> {
                                            btnEnabled = true
                                            btnText = "Do Print"

                                            println("Hello")
                                        }
                                        PrintStatus.BEGIN -> {
                                            txtStatus = "Printing..."
                                            btnText = "Now working"
                                        }
                                        PrintStatus.ERROR -> {

                                        }
                                        PrintStatus.DONE -> {
                                            txtStatus = "Image send done"
                                            mainViewModel.putLastPrintingTime()
                                            mainViewModel.putLastPeriodIndex(periodIndex)
                                            exitApplication()
                                        }
                                    }
                                }
                            }
                        }) {
                        Text(btnText)
                    }
                }
            }
        }
    }
}


private fun printWork(fileURL: URL?, statusChanged: (PrintStatus) -> Unit) {
    statusChanged(PrintStatus.BEGIN)

    val job = PrinterJob.getPrinterJob()
    job.setPrintable { graphics, pageFormat, pageIndex ->
        val image = try {
            ImageIO.read(fileURL)
        } catch (e: IOException) {
            println("Not found exception")
            null
        }

        if (image == null || pageIndex != 0) {
            statusChanged(PrintStatus.ERROR)
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

        return@setPrintable Printable.PAGE_EXISTS
    }

    if (job.printDialog()) {
        try {
            job.print()
            statusChanged(PrintStatus.DONE)
        } catch (e: PrinterException) {
            e.printStackTrace()
        }
    } else {
        println("Canceled?")
    }

    statusChanged(PrintStatus.NONE)
}