package com.ecotrak.lab8_callflow

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecotrak.lab8_callflow.ui.theme.Lab8_callFlowTheme
import com.hoho.android.usbserial.util.SerialInputOutputManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            Lab8_callFlowTheme {
                MainScreen()
            }
        }
    }
}

@HiltAndroidApp
class Lab8App: Application()

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun provideUSBSource(
        @ApplicationContext context : Context
    ) : USBSource {
        val baudRate: Int = 9600
        val source: USBSource = USBSource()
        source.connectUSB(context, baudRate)
        return source
    }
}

sealed class USBEvent {
    data class OnAngleChange(val angle: Float): USBEvent()
    data class OnBucketLoadChange(val bucketLoad: Float): USBEvent()
}

class MainRepository @Inject constructor(
    private val source: USBSource
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUSBEventFlow() : Flow<USBEvent> =  callbackFlow<USBEvent> {

        // Create a listener
        val listener = object : SerialInputOutputManager.Listener {
            override fun onNewData(data: ByteArray?) {

                val angle: Float = String(data!!).toFloat()
                sendBlocking(USBEvent.OnAngleChange(angle))
            }
            override fun onRunError(e: Exception?) {
                TODO("Not yet implemented")
            }
        }

        // Start listening using the listener
        val usbIoManager = SerialInputOutputManager(source.port, listener)
        usbIoManager.start()

        awaitClose {
            // Unregister the listener
        }
    }

}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: MainRepository
) : ViewModel() {

    var angle: Float by mutableStateOf(0f)
    var bucketLoad: Float by mutableStateOf(0f)

    init {
        collectUSBEventFlow()
    }

    private fun collectUSBEventFlow() = viewModelScope.launch {
        repo.getUSBEventFlow().collect { event ->
            when (event) {
                is USBEvent.OnAngleChange -> {
                    angle = event.angle
                }
                is USBEvent.OnBucketLoadChange -> {
                    bucketLoad = event.bucketLoad
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = viewModel.angle.toString(),
            fontSize = 84.sp
        )
    }
}



































/*@Provides
fun provideUsbManager(
    @ApplicationContext context : Context
) : UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

@Provides
fun provideUsbSerialDriver(
    @ApplicationContext context : Context,
    manager : UsbManager
) : UsbSerialDriver {
    val driver: UsbSerialDriver = UsbSerialProber.getDefaultProber().findAllDrivers(manager)[0]
    while (!manager.hasPermission(driver.device)) {
        manager.requestPermission(
            driver.device,
            PendingIntent.getBroadcast(context, 0, Intent("com.android.example.USB_PERMISSION"), PendingIntent.FLAG_IMMUTABLE))
        Thread.sleep(1000)
    }
    return driver
}

@Provides
fun provideUsbDeviceConnection(
    manager : UsbManager,
    driver: UsbSerialDriver
) : UsbDeviceConnection = manager.openDevice(driver.device)

@Provides
fun provideUsbSerialPort(
    driver: UsbSerialDriver,
    connection: UsbDeviceConnection
) : UsbSerialPort {

    val baudRate: Int = 9600
    val port: UsbSerialPort = driver.ports[0]
    port.open(connection)
    port.setParameters(
        baudRate,
        8,
        UsbSerialPort.STOPBITS_1,
        UsbSerialPort.PARITY_NONE)
    return port
}*/