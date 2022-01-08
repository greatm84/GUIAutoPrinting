import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AutoPrinter",
        state = rememberWindowState(width = 400.dp, height = 300.dp)
    ) {

        val lastPrintTime by rememberSaveable { mainViewModel.lastPrintTime }
        var text by rememberSaveable { mutableStateOf("last PrintTime is ${TimeUtil.formatTime(lastPrintTime)}") }
        var btnText by rememberSaveable { mutableStateOf("Do Print") }
        var btnEnabled by rememberSaveable { mutableStateOf(true) }

        if (TimeUtil.isInWeek(lastPrintTime)) {
            exitApplication()
            return@Window
        }

        val scope = rememberCoroutineScope()

        MaterialTheme {
            Column(Modifier.fillMaxSize().padding(10.dp), Arrangement.spacedBy(5.dp)) {
                Text(text)
                Button(modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        if (!btnEnabled) {
                            return@Button
                        }

                        btnEnabled = false
                        scope.launch(Dispatchers.IO) {
                            text = "Printing..."
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
                                text = "Image send done"
                                return@setPrintable Printable.PAGE_EXISTS
                            }

                            if (job.printDialog()) {
                                try {
                                    job.print()
                                    mainViewModel.putLastPrintingTIme()
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