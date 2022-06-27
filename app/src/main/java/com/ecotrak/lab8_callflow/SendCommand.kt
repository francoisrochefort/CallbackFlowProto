package com.ecotrak.lab8_callflow

/**
 * Commands to be sent to a USBDevice
 *
 * Defined [here](https://github.com/eco-trak/ProGuide/issues/1)
 */
@Suppress("Unused")
enum class SendCommand(val code: String) {
    PauseProcessLoad("APPL"),
    ClearWholeLoad("ACWL"),
    ClearLastBucket("ACLB"),
    SetTareWeight("ASTW"),
    ExitCalibration("CA01"),
    EnterCalibration("CA02"),
    SetCalibrationToZero("CA03"),
    SetLowerLimit("CA04"),
    SetResetPoint("CA05"),
    SetAdditionPoint(""), //Addition or additional ?
    SetHigherLimit("CA07"), //Written as "Set Hi Limit", interpreted as higher limit, TODO: change if not correct.
    SaveStaticFactor("CA08"),
    SetStaticFactor("CA09"),
    StartCalibration0("CA10"), //What is a calibration 0 ?
    StartCalibrationX1("CA11"), //What is a calibration X1 ?
    SaveCalibration("CA19"),
    StartLowDynFactorCalibration("CA21"),
    StartMedDynFactorCalibration("CA29"),
    SaveDynFactor("CA29")
}