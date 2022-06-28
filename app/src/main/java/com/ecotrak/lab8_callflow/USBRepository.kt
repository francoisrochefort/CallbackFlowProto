package com.ecotrak.lab8_callflow

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.ecotrak.lab8_callflow.USBRepository.connect
import java.io.IOException
import java.lang.IllegalStateException

/**
 * Processes data from a [USBSource].
 *
 * Call [connect] to initialize the connection
 */
object USBRepository {

    private lateinit var source : USBSource
    private var connected : Boolean = false

    private var dataBuffer = ByteArray(0)
    private val dataRegex = Regex("<[^<>]*>") // How received strings should be formatted : <X...X>
    private val brokenDataRegex = Regex("^[^<>]*>") // How received strings shouldn't be formatted : X...X>

    private const val TAG = "USBRepository"


    var calibrationRunNumber = mutableStateOf(0)
    var cabAngleOffset = mutableStateOf(0.0f)
    var angleLimitLow = mutableStateOf(0.0f)
    var angleLimitHigh = mutableStateOf(0.0f)
    var angleResetPoint = mutableStateOf(0.0f)
    var tareWeight = mutableStateOf(0)
    var processPressure = mutableStateOf(0.0f)
    var processAngle = mutableStateOf(0.0f)
    var cabAngle = mutableStateOf(0.0f)
    var angleSpeed = mutableStateOf(0.0f)
    var compFactor = mutableStateOf(0.0f)
    var currentWeight = mutableStateOf(0.0f)
    var loadWeight = mutableStateOf(0.0f)

    var systemErrors = mutableStateOf(Array(8) {false})

    /**
     * Called from [USBSource] when there is new data
     */
    fun processData(_data: ByteArray) {

        if (!connected) {
            Log.e(TAG,"This repository is not connected to any source !!")
            return
        }

        val data = dataBuffer + _data //append any pre-existing data to the beginning
        var dataStr = String(data)

        if (dataStr.first() != '<') {
            val oldData = dataStr
            dataStr = brokenDataRegex.replace(dataStr, "")
            Log.e("INCOMPLETE_DATA", "Received incomplete data : $oldData, returned : $dataStr")
        }

        //Log.d(TAG,"Received Data : $dataStr")

        for (match in dataRegex.findAll(dataStr)) { // Process <X...X> sequences one by one leaving any incomplete data to be reused
            val matchData = match.value
            decode(matchData)
            dataStr = dataStr.drop(matchData.length)
        }

        //Set remaining data to buffer, if empty will clear it
        dataBuffer = dataStr.toByteArray()
    }

    /**
     * Connect to a [USBSource]
     *
     * Should be called before anything else
     */
    fun connect(context:Context,baudRate: Int) {
        source = USBSource()
        try {
            source.connectUSB(context, baudRate)
        }
        catch(e: IOException) {
            // Connection failed
            systemErrors.value[0] = true
            return
        }
        source.startRead()
        connected = true
        systemErrors.value[0] = false
    }

    /**
     * Sends a message to a [USBSource] with the format "<[{command}][command][{data}][data]>"
     *
     * Needs an active connection
     */
    fun send(command: SendCommand, data:String = "") {
        if (!connected) {
            Log.e(TAG,"This repository is not connected to any source !!")
            return
        }

        if (data.contains('<') or data.contains('>')) {
            throw IOException("Unsupported characters ('<', '>') in SendCommand : ${command.name}, $command")
        }

        if (command.code == "CA11" && data.isBlank()) {
            throw IOException("Code ${command.code} from ${command.name} must be accompanied by data.")
        }

        source.write("<${command.code}$data>".toByteArray(Charsets.US_ASCII),1000)
    }

    private fun decode(data: String) {
        if (data.length < 7) { //<CODE*> -> minimum 6 characters to be valid
            throw IOException("Decoding $data failed : A valid code should contain at least 7 characters (including delimiters), received ${data.length}!")
        }

        val value : String = data.subSequence(5, data.length-1) as String
        when (val code = data.subSequence(1,5)) {
            ReceiveCommand.SystemStatus.code -> { setSystemStatus(value) }
            ReceiveCommand.CalibrationStatus.code -> { setSystemStatus(value) }

            ReceiveCommand.CalibrationRunNumber.code -> { setCalRunNum(value) }
            ReceiveCommand.CabAngleOffset.code -> { setCabAngleOffset(value) }

            ReceiveCommand.AngleLimitLow.code -> { setAngleLimitLow(value) }
            ReceiveCommand.AngleLimitHigh.code -> { setAngleLimitHigh(value) }

            ReceiveCommand.AngleResetPoint.code -> { setAngleResetPoint(value) }
            ReceiveCommand.AngleAddPoint.code -> { /*TODO: Implement add point*/}

            ReceiveCommand.TareWeight.code -> { setTareWeight(value) }
            ReceiveCommand.ProcessPressure.code -> { setProcessPressure(value) }
            ReceiveCommand.ProcessAngle.code -> { setProcessAngle(value)
            }
            ReceiveCommand.CabAngle.code -> { setCabAngle(value) }
            ReceiveCommand.AngleSpeed.code -> { setAngleSpeed(value) }
            ReceiveCommand.CompFactor.code -> { setCompFactor(value) }
            ReceiveCommand.CurrentWeight.code -> { setCurrentWeight(value) }
            ReceiveCommand.LoadWeight.code -> { setLoadWeight(value) }
            ReceiveCommand.AddWeight.code -> {/*TODO: Implement add weight*/}

            else -> { throw IOException("Unknown or unimplemented ReceiveCommand code [$code] from data : $data") }
        }
    }

    private fun setSystemStatus(value: String) {

        val data = value.toInt() //this probably doesn't work

        if (data and 0b00000001 == 0b00000001) {
            throw IllegalStateException()
        }
        //TODO: finish implementing this
    }

    private fun setCalibrationStatus(value: String) {
        //TODO: implement this
    }

    private fun setCalRunNum(value: String) {
        calibrationRunNumber.value = value.toInt()
    }

    private fun setCabAngleOffset(value: String) {
        cabAngleOffset.value = value.toInt() * 0.1f
    }

    private fun setAngleLimitLow(value: String) {
        angleLimitLow.value = value.toInt() * 0.1f
    }

    private fun setAngleLimitHigh(value: String) {
        angleLimitHigh.value = value.toInt() * 0.1f
    }

    private fun setAngleResetPoint(value: String) {
        angleResetPoint.value = value.toInt() * 0.1f
    }

    private fun setTareWeight(value: String) {
        tareWeight.value = value.toInt()
    }

    private fun setProcessPressure(value: String) {
        processPressure.value = value.toInt() * 0.1f
    }

    private fun setProcessAngle(value: String) {
        processAngle.value = value.toInt() * 0.1f
    }

    private fun setCabAngle(value: String) {
        cabAngle.value = value.toInt() * 0.1f
    }

    private fun setAngleSpeed(value: String) {
        angleSpeed.value = value.toInt() * 0.1f
    }

    private fun setCompFactor(value: String) {
        compFactor.value = value.toInt() * 0.1f
    }

    private fun setCurrentWeight(value: String) {
        currentWeight.value = value.toInt() * 0.1f
    }

    private fun setLoadWeight(value: String) {
        loadWeight.value = value.toInt() * 0.1f
    }
}