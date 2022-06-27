package com.ecotrak.lab8_callflow

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ecotrak.lab8_callflow.ui.theme.Lab8_callFlowTheme
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        USBRepository.connect(applicationContext,9600)

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
}

@HiltViewModel
class MainViewModel @Inject constructor(

) : ViewModel() {
    var tare : MutableState<Int> = mutableStateOf(1000)
    var weight : MutableState<Int> = mutableStateOf(1_000_000)
    var lastBucket = mutableStateOf(100_000)
    var bucketCount = mutableStateOf(100)
    var expectedLoad = mutableStateOf(1_000_000)

    val totalLoad : MutableState<Float>
        get() = USBRepository.loadWeight
}

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = USBRepository.loadWeight.value.toString(),
            fontSize = 84.sp
        )
    }
}






























