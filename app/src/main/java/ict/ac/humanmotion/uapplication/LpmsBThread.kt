package ict.ac.humanmotion.uapplication

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.math.BigDecimal
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.LinkedBlockingDeque

class LpmsBThread  constructor( var mAdapter: BluetoothAdapter?) : Thread() {
     val TAG = "lpms"

    val MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    val PACKET_ADDRESS0 = 0
    val PACKET_ADDRESS1 = 1
    val PACKET_FUNCTION0 = 2
    val PACKET_FUNCTION1 = 3
     val PACKET_RAW_DATA = 4
     val PACKET_LRC_CHECK0 = 5
     val PACKET_LRC_CHECK1 = 6
     val PACKET_END = 7
     val PACKET_LENGTH0 = 8
     val PACKET_LENGTH1 = 9

     val LPMS_ACK = 0
     val LPMS_NACK = 1
     val LPMS_GET_CONFIG = 4
     val LPMS_GET_STATUS = 5
     val LPMS_GOTO_COMMAND_MODE = 6
     val LPMS_GOTO_STREAM_MODE = 7
     val LPMS_GOTO_SLEEP_MODE = 8
     val LPMS_GET_SENSOR_DATA = 9
     val LPMS_SET_TRANSMIT_DATA = 10

     val STATE_IDLE = 0

     val MAX_BUFFER = 512

     var rxState = PACKET_END
     var rxBuffer = ByteArray(MAX_BUFFER)
     var txBuffer = ByteArray(MAX_BUFFER)
     var rawTxData = ByteArray(MAX_BUFFER)
     var rawRxBuffer = ByteArray(MAX_BUFFER)
     var currentAddress: Int = 0
     var currentFunction: Int = 0
     var currentLength: Int = 0
     var rxIndex = 0
     var b: Byte = 0
     var lrcCheck: Int = 0
     var isConnected = false
     var nBytes: Int = 0
     var timeout: Int = 0
     var waitForAck: Boolean = false
     var waitForData: Boolean = false
     var state: Int = 0
     var inBytes = ByteArray(2)
     lateinit var mInStream: InputStream
     lateinit var mOutStream: OutputStream
     var mSocket: BluetoothSocket? = null
     lateinit var address: String
    lateinit var device: BluetoothDevice
         set
     var isGetGyroscope = true
     var isGetAcceleration = true
     var isGetMagnetometer = true
     var isGetQuaternion = true
     var isGetEulerAngler = true
     var isGetLinearAcceleration = true
     var isGetPressure = true
     var imuId = 0
     var dos: DataOutputStream? = null
    var startStamp = 0.0f
    var resetTimestampFlag = true
     var newDataFlag = false
     var dataQueue = LinkedBlockingDeque<LpmsBData>()
     var mLpmsBData = LpmsBData()
     var frameCounter = 0

     val lpmsBData: LpmsBData?
        get() {
            if (dataQueue.peekLast() != null) {
                val d = LpmsBData(dataQueue.peekLast())
                dataQueue.removeLast()
                return d
            }
            return null
        }

     fun connect(address: String, id: Int): Boolean {
        this.address = address
        imuId = id

        Log.e(TAG, "[LpmsBThread] Connect to: " + this.address)

        if (mAdapter == null) {
            Log.e(TAG, "[LpmsBThread] Didn't find Bluetooth adapter")

            return false
        }

        mAdapter!!.cancelDiscovery()

        Log.e(TAG, "[LpmsBThread] Getting device")
        try {
            device = mAdapter!!.getRemoteDevice(this.address)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "[LpmsBThread] Invalid Bluetooth address", e)
            return false
        }

        mSocket = null
        Log.e(TAG, "[LpmsBThread] Creating socket")
        try {
            mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE)
        } catch (e: Exception) {
            Log.e(TAG, "[LpmsBThread] Socket create() failed", e)
            return false
        }

        Log.e(TAG, "[LpmsBThread] Trying to connect..")
        try {
            mSocket!!.connect()
        } catch (e: IOException) {
            Log.e(TAG, "[LpmsBThread] Couldn't connect to device", e)
            return false
        }

        Log.e(TAG, "[LpmsBThread] Connected!")

        try {
            mInStream = mSocket!!.inputStream
            mOutStream = mSocket!!.outputStream
        } catch (e: IOException) {
            Log.e(TAG, "[LpmsBThread] Streams not created", e)
            return false
        }

        resetTimestamp()

        val t = Thread(ClientReadThread())
        t.start()

        frameCounter = 0

        return true
    }

    inner class ClientReadThread : Runnable {
        override fun run() {
            /* Thread t = new Thread(new ClientStateThread());
        	t.start(); */

            while (mSocket!!.isConnected) {
                try {
                    nBytes = mInStream.read(rawRxBuffer)
                } catch (e: Exception) {
                    break
                }

                parse()
            }
        }
    }

    inner class ClientStateThread : Runnable {
        override fun run() {
            try {
                while (mSocket!!.isConnected) {
                    if (!waitForAck && !waitForData) {
                        when (state) {
                            STATE_IDLE -> {
                            }
                        }
                    } else if (timeout > 100) {
                        Log.d(TAG, "[LpmsBThread] Receive timeout")
                        timeout = 0
                        state = STATE_IDLE
                        waitForAck = false
                        waitForData = false
                    } else {
                        Thread.sleep(10)
                        ++timeout
                    }

                    try {
                        Thread.sleep(1)
                    } catch (e: InterruptedException) {
                    }

                }
            } catch (e: Exception) {
                Log.d(TAG, "[LpmsBThread] Connection interrupted")
                isConnected = false
            }

        }
    }

     fun setAcquisitionParameters(isGetGyroscope: Boolean,
                                          isGetAcceleration: Boolean,
                                          isGetMagnetometer: Boolean,
                                          isGetQuaternion: Boolean,
                                          isGetEulerAngler: Boolean,
                                          isGetLinearAcceleration: Boolean,
                                          isGetPressure: Boolean) {
        this.isGetGyroscope = isGetGyroscope
        this.isGetAcceleration = isGetAcceleration
        this.isGetMagnetometer = isGetMagnetometer
        this.isGetQuaternion = isGetQuaternion
        this.isGetEulerAngler = isGetEulerAngler
        this.isGetLinearAcceleration = isGetLinearAcceleration
        this.isGetPressure = isGetPressure
    }

    private fun parseSensorData() {
        var o = 0
        val r2d = 57.2958f

        mLpmsBData.imuId = imuId
        mLpmsBData.timestamp = convertRxbytesToFloat(o, rxBuffer)
        o += 4
        mLpmsBData.frameNumber = frameCounter
        frameCounter++

        if (isGetGyroscope) {
            mLpmsBData.gyr[0] = convertRxbytesToFloat(o, rxBuffer) * r2d
            o += 4
            mLpmsBData.gyr[1] = convertRxbytesToFloat(o, rxBuffer) * r2d
            o += 4
            mLpmsBData.gyr[2] = convertRxbytesToFloat(o, rxBuffer) * r2d
            o += 4
        }

        if (isGetAcceleration) {
            mLpmsBData.acc[0] = convertRxbytesToFloat(o, rxBuffer)
            o += 4
            mLpmsBData.acc[1] = convertRxbytesToFloat(o, rxBuffer)
            o += 4
            mLpmsBData.acc[2] = convertRxbytesToFloat(o, rxBuffer)
            o += 4
        }

        if (isGetMagnetometer) {
            mLpmsBData.mag[0] = convertRxbytesToFloat(o, rxBuffer)
            o += 4
            mLpmsBData.mag[1] = convertRxbytesToFloat(o, rxBuffer)
            o += 4
            mLpmsBData.mag[2] = convertRxbytesToFloat(o, rxBuffer)
            o += 4
        }

        if (isGetQuaternion) {
            mLpmsBData.quat[0] = convertRxbytesToFloat(o, rxBuffer)
            o += 4
            mLpmsBData.quat[1] = convertRxbytesToFloat(o, rxBuffer)
            o += 4
            mLpmsBData.quat[2] = convertRxbytesToFloat(o, rxBuffer)
            o += 4
            mLpmsBData.quat[3] = convertRxbytesToFloat(o, rxBuffer)
            o += 4
        }

        if (isGetEulerAngler) {
            mLpmsBData.euler[0] = convertRxbytesToFloat(o, rxBuffer) * r2d
            o += 4
            mLpmsBData.euler[1] = convertRxbytesToFloat(o, rxBuffer) * r2d
            o += 4
            mLpmsBData.euler[2] = convertRxbytesToFloat(o, rxBuffer) * r2d
            o += 4
        }

        if (isGetLinearAcceleration) {
            mLpmsBData.linAcc[0] = convertRxbytesToFloat(o, rxBuffer)
            o += 4
            mLpmsBData.linAcc[1] = convertRxbytesToFloat(o, rxBuffer)
            o += 4
            mLpmsBData.linAcc[2] = convertRxbytesToFloat(o, rxBuffer)
            o += 4
        }

        if (isGetPressure) {
            mLpmsBData.pressure = convertRxbytesToFloat(o, rxBuffer)
            o += 4
        }

        dataQueue.addFirst(LpmsBData(mLpmsBData))

        newDataFlag = true
    }

    fun hasNewData(): Boolean {
        return dataQueue.peekLast() != null
    }

    private fun parseFunction() {
        when (currentFunction) {
            LPMS_ACK -> Log.d(TAG, "[LpmsBThread] Received ACK")

            LPMS_NACK -> Log.d(TAG, "[LpmsBThread] Received NACK")

            LPMS_GET_CONFIG -> {
            }

            LPMS_GET_STATUS -> {
            }

            LPMS_GOTO_COMMAND_MODE -> {
            }

            LPMS_GOTO_STREAM_MODE -> {
            }

            LPMS_GOTO_SLEEP_MODE -> {
            }

            LPMS_GET_SENSOR_DATA -> parseSensorData()

            LPMS_SET_TRANSMIT_DATA -> {
            }
        }

        waitForAck = false
        waitForData = false
    }

    private fun parse() {
        var lrcReceived = 0

        loop@ for (i in 0 until nBytes) {
            b = rawRxBuffer[i]

            when (rxState) {
                PACKET_END -> if (b.toInt() == 0x3a) {
                    rxState = PACKET_ADDRESS0
                }

                PACKET_ADDRESS0 -> {
                    inBytes[0] = b
                    rxState = PACKET_ADDRESS1
                }

                PACKET_ADDRESS1 -> {
                    inBytes[1] = b
                    currentAddress = convertRxbytesToInt16(0, inBytes)
                    rxState = PACKET_FUNCTION0
                }

                PACKET_FUNCTION0 -> {
                    inBytes[0] = b
                    rxState = PACKET_FUNCTION1
                }

                PACKET_FUNCTION1 -> {
                    inBytes[1] = b
                    currentFunction = convertRxbytesToInt16(0, inBytes)
                    rxState = PACKET_LENGTH0
                }

                PACKET_LENGTH0 -> {
                    inBytes[0] = b
                    rxState = PACKET_LENGTH1
                }

                PACKET_LENGTH1 -> {
                    inBytes[1] = b
                    currentLength = convertRxbytesToInt16(0, inBytes)
                    rxState = PACKET_RAW_DATA
                    rxIndex = 0
                }

                PACKET_RAW_DATA -> if (rxIndex == currentLength) {
                    lrcCheck = (currentAddress and 0xffff) + (currentFunction and 0xffff) + (currentLength and 0xffff)

                    for (j in 0 until currentLength) {
                        if (j < MAX_BUFFER) {
                            lrcCheck += rxBuffer[j].toInt() and 0xff
                        } else
                            break
                    }

                    inBytes[0] = b
                    rxState = PACKET_LRC_CHECK1
                } else {
                    if (rxIndex < MAX_BUFFER) {
                        rxBuffer[rxIndex] = b
                        ++rxIndex
                    } else
                        break@loop
                }

                PACKET_LRC_CHECK1 -> {
                    inBytes[1] = b

                    lrcReceived = convertRxbytesToInt16(0, inBytes)
                    lrcCheck = lrcCheck and 0xffff

                    if (lrcReceived == lrcCheck) {
                        parseFunction()
                    } else {
                    }

                    rxState = PACKET_END
                }

                else -> rxState = PACKET_END
            }
        }
    }

    private fun sendData(address: Int, function: Int, length: Int) {
        var txLrcCheck: Int

        txBuffer[0] = 0x3a
        convertInt16ToTxbytes(address, 1, txBuffer)
        convertInt16ToTxbytes(function, 3, txBuffer)
        convertInt16ToTxbytes(length, 5, txBuffer)

        System.arraycopy(rawTxData, 0, txBuffer, 7, length)

        txLrcCheck = address
        txLrcCheck += function
        txLrcCheck += length

        for (i in 0 until length) {
            txLrcCheck += rawTxData[i].toInt()
        }

        convertInt16ToTxbytes(txLrcCheck, 7 + length, txBuffer)
        txBuffer[9 + length] = 0x0d
        txBuffer[10 + length] = 0x0a

        for (i in 0 until 11 + length) {
            Log.d(TAG, "[LpmsBThread] Sending: " + java.lang.Byte.toString(txBuffer[i]))
        }

        try {
            Log.d(TAG, "[LpmsBThread] Sending data")
            mOutStream.write(txBuffer, 0, length + 11)
        } catch (e: Exception) {
            Log.d(TAG, "[LpmsBThread] Error while sending data")
        }

    }

     fun sendAck() {
        sendData(0, LPMS_ACK, 0)
    }

     fun sendNack() {
        sendData(0, LPMS_NACK, 0)
    }

    private fun convertRxbytesToFloat(offset: Int, buffer: ByteArray): Float {
        val v = 0
        val t = ByteArray(4)

        for (i in 0..3) {
            t[3 - i] = buffer[i + offset]
        }

        return java.lang.Float.intBitsToFloat(ByteBuffer.wrap(t).getInt(0))
    }

     fun convertRxbytesToInt(offset: Int, buffer: ByteArray): Int {
        val v: Int
        val t = ByteArray(4)

        for (i in 0..3) {
            t[3 - i] = buffer[i + offset]
        }

        v = ByteBuffer.wrap(t).getInt(0)

        return v
    }

    private fun convertRxbytesToInt16(offset: Int, buffer: ByteArray): Int {
        val v: Int
        val t = ByteArray(2)

        for (i in 0..1) {
            t[1 - i] = buffer[i + offset]
        }

        v = ByteBuffer.wrap(t).getShort(0).toInt() and 0xffff

        return v
    }

     fun convertIntToTxbytes(v: Int, offset: Int, buffer: ByteArray) {
        val t = ByteBuffer.allocate(4).putInt(v).array()

        for (i in 0..3) {
            buffer[3 - i + offset] = t[i]
        }
    }

    private fun convertInt16ToTxbytes(v: Int, offset: Int, buffer: ByteArray) {
        val t = ByteBuffer.allocate(2).putShort(v.toShort()).array()

        for (i in 0..1) {
            buffer[1 - i + offset] = t[i]
        }
    }

     fun convertFloatToTxbytes(f: Float, offset: Int, buffer: ByteArray) {
        val v = java.lang.Float.floatToIntBits(f)
        val t = ByteBuffer.allocate(4).putInt(v).array()

        for (i in 0..3) {
            buffer[3 - i + offset] = t[i]
        }
    }

    fun close() {
        mAdapter!!.cancelDiscovery()

        isConnected = false

        try {
            mSocket!!.close()
        } catch (ignored: Exception) {
        }

        Log.d(TAG, "[LpmsBThread] Connection closed")
    }

    private fun resetTimestamp() {
        resetTimestampFlag = true
    }

    companion object {

        fun round(d: Float, decimalPlace: Int): Float {
            var bd = BigDecimal(java.lang.Float.toString(d))
            bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP)

            return bd.toFloat()
        }
    }
}	