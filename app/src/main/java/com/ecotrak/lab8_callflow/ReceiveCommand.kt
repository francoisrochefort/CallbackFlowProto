package com.ecotrak.lab8_callflow

/**
 * Commands received from a USBDevice
 */
enum class ReceiveCommand(val code: String) {
    SystemStatus("AD00"),
    CalibrationStatus("AD01"),

    CalibrationRunNumber("AD02"),
    CabAngleOffset("AD03"),
    AngleLimitLow("AD04"),
    AngleResetPoint("AD05"),
    AngleAddPoint("AD06"),
    AngleLimitHigh("AD07"),
    TareWeight("AD08"),
    ProcessPressure("AD09"),
    ProcessAngle("AD10"),
    CabAngle("AD11"),
    AngleSpeed("AD12"),
    CompFactor("AD13"),
    CurrentWeight("AD14"),
    LoadWeight("AD15"),
    AddWeight("AD16")
}