package ict.ac.humanmotion.uapplication

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DataFragment : MyFragment(), Callback<String> {
    override fun onResponse(call: Call<String>?, response: Response<String>?) =
            println("Human Motion Data Uploaded")

    override fun onFailure(call: Call<String>?, t: Throwable) = t.printStackTrace()

    private val TAG = "DataFragment"
    private val FRAGMENT_TAG = 2

    private var resultList: MutableList<LpmsBData> = mutableListOf()

    override var myFragmentTag: Int = FRAGMENT_TAG

    private lateinit var accSeries0: LineGraphSeries<DataPoint>
    private lateinit var accSeries1: LineGraphSeries<DataPoint>
    private lateinit var accSeries2: LineGraphSeries<DataPoint>

    private lateinit var gyrSeries0: LineGraphSeries<DataPoint>
    private lateinit var gyrSeries1: LineGraphSeries<DataPoint>
    private lateinit var gyrSeries2: LineGraphSeries<DataPoint>

    private lateinit var magSeries0: LineGraphSeries<DataPoint>
    private lateinit var magSeries1: LineGraphSeries<DataPoint>
    private lateinit var magSeries2: LineGraphSeries<DataPoint>

    private var dataCount: Double = 0.0
    private val MAX_DATA_POINT_NUM = 300

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.data_fragment, container, false)

        accSeries0 = LineGraphSeries()
        accSeries1 = LineGraphSeries()
        accSeries2 = LineGraphSeries()

        accSeries0.apply {
            title = "X-Axis"
            color = Color.RED
        }
        accSeries1.apply {
            title = "Y-Axis"
            color = Color.GREEN
        }
        accSeries2.apply {
            title = "Z-Axis"
            color = Color.BLUE
        }

        gyrSeries0 = LineGraphSeries()
        gyrSeries1 = LineGraphSeries()
        gyrSeries2 = LineGraphSeries()

        gyrSeries0.apply {
            title = "X-Axis"
            color = Color.RED
        }
        gyrSeries1.apply {
            title = "Y-Axis"
            color = Color.GREEN
        }
        gyrSeries2.apply {
            title = "Z-Axis"
            color = Color.BLUE
        }

        magSeries0 = LineGraphSeries()
        magSeries1 = LineGraphSeries()
        magSeries2 = LineGraphSeries()

        magSeries0.apply {
            title = "X-Axis"
            color = Color.RED
        }
        magSeries1.apply {
            title = "Y-Axis"
            color = Color.GREEN
        }
        magSeries2.apply {
            title = "Z-Axis"
            color = Color.BLUE
        }

        val xLabels = arrayOf("0", "20", "40", "60", "80", "100")
        val yLabels0 = arrayOf("4", "2", "0", "-2", "-4")
        val yLabels1 = arrayOf("2000", "1000", "0", "-1000", "-2000")
        val yLabels2 = arrayOf("200", "100", "0", "-100", "-200")


//         IMPORTANT : you must use 'findViewById' instead of default Kotlin style
        val accGraph = rootView.findViewById<GraphView>(R.id.accGraph)
        accGraph.apply {
            addSeries(accSeries0)
            addSeries(accSeries1)
            addSeries(accSeries2)
            viewport.apply {
                legendRenderer.isVisible = true
                legendRenderer.align = LegendRenderer.LegendAlign.TOP
                setMinX(0.0)
                setMinY(-4.0)
                setMaxX(MAX_DATA_POINT_NUM.toDouble())
                setMaxY(4.0)
                isXAxisBoundsManual = true
                isYAxisBoundsManual = true
            }
        }

        val gyrGraph = rootView.findViewById<GraphView>(R.id.gyrGraph)
        gyrGraph.apply {
            addSeries(gyrSeries0)
            addSeries(gyrSeries1)
            addSeries(gyrSeries2)
            viewport.apply {
                legendRenderer.isVisible = true
                legendRenderer.align = LegendRenderer.LegendAlign.TOP
                setMinX(0.0)
                setMinY(-2000.0)
                setMaxX(MAX_DATA_POINT_NUM.toDouble())
                setMaxY(2000.0)
                isXAxisBoundsManual = true
                isYAxisBoundsManual = true
            }
        }

        val magGraph = rootView.findViewById<GraphView>(R.id.magGraph)
        magGraph.apply {
            addSeries(magSeries0)
            addSeries(magSeries1)
            addSeries(magSeries2)
            viewport.apply {
                legendRenderer.isVisible = true
                legendRenderer.align = LegendRenderer.LegendAlign.TOP
                setMinX(0.0)
                setMinY(-200.0)
                setMaxX(MAX_DATA_POINT_NUM.toDouble())
                setMaxY(200.0)
                isXAxisBoundsManual = true
                isYAxisBoundsManual = true
            }
        }

        return rootView
    }


    override fun updateView(d: LpmsBData, s: ImuStatus) {
        if (!s.measurementStarted) return

        resultList.add(d)

        accSeries0.appendData(DataPoint(dataCount, d.acc[0].toDouble()), true, MAX_DATA_POINT_NUM)
        accSeries1.appendData(DataPoint(dataCount, d.acc[1].toDouble()), true, MAX_DATA_POINT_NUM)
        accSeries2.appendData(DataPoint(dataCount, d.acc[2].toDouble()), true, MAX_DATA_POINT_NUM)

        gyrSeries0.appendData(DataPoint(dataCount, d.gyr[0].toDouble()), true, MAX_DATA_POINT_NUM)
        gyrSeries1.appendData(DataPoint(dataCount, d.gyr[1].toDouble()), true, MAX_DATA_POINT_NUM)
        gyrSeries2.appendData(DataPoint(dataCount, d.gyr[2].toDouble()), true, MAX_DATA_POINT_NUM)

        magSeries0.appendData(DataPoint(dataCount, d.mag[0].toDouble()), true, MAX_DATA_POINT_NUM)
        magSeries1.appendData(DataPoint(dataCount, d.mag[1].toDouble()), true, MAX_DATA_POINT_NUM)
        magSeries2.appendData(DataPoint(dataCount, d.mag[2].toDouble()), true, MAX_DATA_POINT_NUM)


        dataCount++
    }

    fun clearView() {
        dataCount = 0.0

        accSeries0.resetData(arrayOf(DataPoint(0.0, 0.0)))
        accSeries1.resetData(arrayOf(DataPoint(0.0, 0.0)))
        accSeries2.resetData(arrayOf(DataPoint(0.0, 0.0)))

        gyrSeries0.resetData(arrayOf(DataPoint(0.0, 0.0)))
        gyrSeries1.resetData(arrayOf(DataPoint(0.0, 0.0)))
        gyrSeries2.resetData(arrayOf(DataPoint(0.0, 0.0)))

        magSeries0.resetData(arrayOf(DataPoint(0.0, 0.0)))
        magSeries1.resetData(arrayOf(DataPoint(0.0, 0.0)))
        magSeries2.resetData(arrayOf(DataPoint(0.0, 0.0)))
    }
}