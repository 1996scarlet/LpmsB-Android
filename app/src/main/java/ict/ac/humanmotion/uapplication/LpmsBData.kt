package ict.ac.humanmotion.uapplication

class LpmsBData {
    var imuId = 0
    var timestamp = 0.0f
    var frameNumber = 0
    var gyr = FloatArray(3)
    var acc = FloatArray(3)
    var mag = FloatArray(3)
    var quat = FloatArray(4)
    var euler = FloatArray(3)
    var linAcc = FloatArray(3)
    var pressure: Float = 0.toFloat()

    internal constructor()

    internal constructor(d: LpmsBData) {
        imuId = d.imuId
        timestamp = d.timestamp
        frameNumber = d.frameNumber


        System.arraycopy(d.gyr, 0, gyr, 0, 3)
        System.arraycopy(d.acc, 0, acc, 0, 3)
        System.arraycopy(d.mag, 0, mag, 0, 3)
        System.arraycopy(d.quat, 0, quat, 0, 4)
        System.arraycopy(d.euler, 0, euler, 0, 3)
        System.arraycopy(d.linAcc, 0, linAcc, 0, 3)

        pressure = d.pressure
    }
}