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

class MoreDataFragment : MyFragment() {

    private val TAG = "MoreDataFragment"

    val ARG_SECTION_NUMBER = "section_number"

    private val MAX_DATA_POINT_NUM = 300

    private val FRAGMENT_TAG = 3

    override var myFragmentTag: Int = FRAGMENT_TAG

//    private lateinit var quatSeries0: LineGraphSeries<DataPoint>
//    private lateinit var quatSeries1: LineGraphSeries<DataPoint>
//    private lateinit var quatSeries2: LineGraphSeries<DataPoint>
//    private lateinit var quatSeries3: LineGraphSeries<DataPoint>
//
//    private lateinit var eulerSeries0: LineGraphSeries<DataPoint>
//    private lateinit var eulerSeries1: LineGraphSeries<DataPoint>
//    private lateinit var eulerSeries2: LineGraphSeries<DataPoint>

    private lateinit var linAccSeries0: LineGraphSeries<DataPoint>
    private lateinit var linAccSeries1: LineGraphSeries<DataPoint>
    private lateinit var linAccSeries2: LineGraphSeries<DataPoint>

    private var dataCount: Double = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.more_data_fragment_layout, container, false)

//        initQuaternion(rootView)
//        initEuler(rootView)
        initLineAcc(rootView)

//        val xLabels = arrayOf("0", "20", "40", "60", "80", "100")
//        val yLabels0 = arrayOf("1.0", "0.5", "0", "-0.5", "-1.0")
//        val yLabels1 = arrayOf("180", "90", "0", "-90", "-180")
//        val yLabels2 = arrayOf("4", "2", "0", "-2", "-4")

        return rootView
    }

    private fun initLineAcc(rootView: View) {

        linAccSeries0 = LineGraphSeries()
        linAccSeries1 = LineGraphSeries()
        linAccSeries2 = LineGraphSeries()

        linAccSeries0.apply {
            title = "X-Axis"
            color = Color.RED
        }
        linAccSeries1.apply {
            title = "Y-Axis"
            color = Color.GREEN
        }
        linAccSeries2.apply {
            title = "Z-Axis"
            color = Color.BLUE
        }

        val linAccGraph = rootView.findViewById<GraphView>(R.id.linAccGraph)
        linAccGraph.apply {
            addSeries(linAccSeries0)
            addSeries(linAccSeries1)
            addSeries(linAccSeries2)
            viewport.apply {
                legendRenderer.align = LegendRenderer.LegendAlign.TOP
                legendRenderer.isVisible = true
                setMinX(0.0)
                setMinY(-4.0)
                setMaxX(MAX_DATA_POINT_NUM.toDouble())
                setMaxY(4.0)
                isXAxisBoundsManual = true
                isYAxisBoundsManual = true
            }
        }
    }

    private fun initEuler(rootView: View) {

//        eulerSeries0 = LineGraphSeries()
//        eulerSeries1 = LineGraphSeries()
//        eulerSeries2 = LineGraphSeries()
//
//        eulerSeries0.apply {
//            title = "X-Axis"
//            color = Color.RED
//        }
//        eulerSeries1.apply {
//            title = "Y-Axis"
//            color = Color.GREEN
//        }
//        eulerSeries2.apply {
//            title = "Z-Axis"
//            color = Color.BLUE
//        }

//        val eulerGraph = rootView.findViewById<GraphView>(R.id.eulerGraph)
//        eulerGraph.apply {
//            addSeries(eulerSeries0)
//            addSeries(eulerSeries1)
//            addSeries(eulerSeries2)
//            viewport.apply {
//                legendRenderer.isVisible = true
//                legendRenderer.align = LegendRenderer.LegendAlign.TOP
//                setMinX(0.0)
//                setMinY(-180.0)
//                setMaxX(MAX_DATA_POINT_NUM.toDouble())
//                setMaxY(180.0)
//                isXAxisBoundsManual = true
//                isYAxisBoundsManual = true
//            }
//        }
    }

    private fun initQuaternion(rootView: View) {

//        quatSeries0 = LineGraphSeries()
//        quatSeries1 = LineGraphSeries()
//        quatSeries2 = LineGraphSeries()
//        quatSeries3 = LineGraphSeries()
//
//        quatSeries0.apply {
//            title = "W-Axis"
//            color = Color.YELLOW
//        }
//        quatSeries1.apply {
//            title = "X-Axis"
//            color = Color.RED
//        }
//        quatSeries2.apply {
//            title = "Y-Axis"
//            color = Color.GREEN
//        }
//        quatSeries3.apply {
//            title = "Z-Axis"
//            color = Color.BLUE
//        }

//        val quatGraph = rootView.findViewById<GraphView>(R.id.quatGraph)
//        quatGraph.apply {
//            addSeries(quatSeries0)
//            addSeries(quatSeries1)
//            addSeries(quatSeries2)
//            addSeries(quatSeries3)
//            viewport.apply {
//                legendRenderer.isVisible = true
//                legendRenderer.align = LegendRenderer.LegendAlign.TOP
//                setMinX(0.0)
//                setMinY(-1.0)
//                setMaxX(MAX_DATA_POINT_NUM.toDouble())
//                setMaxY(1.0)
//                isXAxisBoundsManual = true
//                isYAxisBoundsManual = true
//            }
//        }
    }


    override fun updateView(d: LpmsBData, s: ImuStatus) {
        if (!s.measurementStarted) return

//        quatSeries0.appendData(DataPoint(dataCount, d.quat[0].toDouble()), true, MAX_DATA_POINT_NUM)
//        quatSeries1.appendData(DataPoint(dataCount, d.quat[1].toDouble()), true, MAX_DATA_POINT_NUM)
//        quatSeries2.appendData(DataPoint(dataCount, d.quat[2].toDouble()), true, MAX_DATA_POINT_NUM)
//        quatSeries3.appendData(DataPoint(dataCount, d.quat[3].toDouble()), true, MAX_DATA_POINT_NUM)
//
//        eulerSeries0.appendData(DataPoint(dataCount, d.euler[0].toDouble()), true, MAX_DATA_POINT_NUM)
//        eulerSeries1.appendData(DataPoint(dataCount, d.euler[1].toDouble()), true, MAX_DATA_POINT_NUM)
//        eulerSeries2.appendData(DataPoint(dataCount, d.euler[2].toDouble()), true, MAX_DATA_POINT_NUM)

        linAccSeries0.appendData(DataPoint(dataCount, d.linAcc[0].toDouble()), true, MAX_DATA_POINT_NUM)
        linAccSeries1.appendData(DataPoint(dataCount, d.linAcc[1].toDouble()), true, MAX_DATA_POINT_NUM)
        linAccSeries2.appendData(DataPoint(dataCount, d.linAcc[2].toDouble()), true, MAX_DATA_POINT_NUM)

        dataCount++
    }
}