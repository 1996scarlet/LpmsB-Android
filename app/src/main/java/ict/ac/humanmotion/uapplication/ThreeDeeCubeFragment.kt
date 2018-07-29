package ict.ac.humanmotion.uapplication

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import com.google.gson.Gson
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.upload_fragment.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class ThreeDeeCubeFragment : MyFragment(), Callback<String> {

    private val TAG = "3DFragment"

    private val FRAGMENT_TAG = 1

    override var myFragmentTag: Int = FRAGMENT_TAG

    private val MAX_DATA_POINT_NUM = 300

    private var dataCount: Double = 0.0

    //    private lateinit var glView: LpmsBSurfaceView
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
////        val rootView =
//        inflater.inflate(R.layout.fragment_section_dummy, container, false)
////        val args = arguments
//
//        glView = LpmsBSurfaceView(activity)
//
//        return glView
//    }
//
//    override fun updateView(d: LpmsBData, s: ImuStatus) {
//        if (!s.measurementStarted) return
//
//        glView.lmRenderer.q[0] = d.quat[0]
//        glView.lmRenderer.q[1] = d.quat[1]
//        glView.lmRenderer.q[2] = d.quat[2]
//        glView.lmRenderer.q[3] = d.quat[3]
//
//        glView.requestRender()
//    }

    var biasL0 = 0f
    var biasL1 = 0f
    var biasL2 = 0f

    var biasA0 = 0f
    var biasA1 = 0f
    var biasA2 = 0f

    private lateinit var Series0: LineGraphSeries<DataPoint>
    private lateinit var Series1: LineGraphSeries<DataPoint>
    private lateinit var Series2: LineGraphSeries<DataPoint>

    override fun onResponse(call: Call<String>?, response: Response<String>?) =
            println("Human Motion Data Uploaded:${response?.body()}")

    override fun onFailure(call: Call<String>?, t: Throwable) = t.printStackTrace()

    private var resultList: MutableList<LpmsBData> = mutableListOf()

    val resultService = Retrofit.Builder()
            .client(OkHttpClient())
            .baseUrl("http://192.168.0.2:23456/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build().create(UploadServer::class.java)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.upload_fragment, container, false)

        rootView.findViewById<ToggleButton>(R.id.toggle).setOnCheckedChangeListener { compoundButton, isChecked ->
            //            if () return@setOnCheckedChangeListener

            if (!isChecked && !text_motion.text.isNullOrEmpty()) {
                Log.d(TAG, "Sending message to server!")
                resultService.postSave(text_motion.text.toString(), Gson().toJson(
                        resultList.map {
                            SVMDataBean(
                                    it.linAcc[0] - biasL0,
                                    it.linAcc[1] - biasL1,
                                    it.linAcc[2] - biasL2,
                                    it.acc[0] - biasA0,
                                    it.acc[1] - biasA1,
                                    it.acc[2] - biasA2
                            )
                        })).enqueue(this)
            }

            resultList.clear()
        }

        initGraph(rootView)

        return rootView
    }

    override fun updateView(d: LpmsBData, s: ImuStatus) {
        if (!s.measurementStarted || !toggle.isChecked) return

        if (resultList.isEmpty()) {

            biasL0 = d.linAcc[0]
            biasL1 = d.linAcc[1]
            biasL2 = d.linAcc[2]

            biasA0 = d.acc[0]
            biasA1 = d.acc[1]
            biasA2 = d.acc[2]

            println("rebias")
        }

        resultList.add(d)

        val temp = resultList.last()

        Series0.appendData(DataPoint(dataCount, temp.acc[0].toDouble() - biasA0), true, MAX_DATA_POINT_NUM)
        Series1.appendData(DataPoint(dataCount, temp.acc[1].toDouble() - biasA1), true, MAX_DATA_POINT_NUM)
        Series2.appendData(DataPoint(dataCount, temp.acc[2].toDouble() - biasA2), true, MAX_DATA_POINT_NUM)

        dataCount++
    }

    private fun initGraph(rootView: View) {

        Series0 = LineGraphSeries()
        Series1 = LineGraphSeries()
        Series2 = LineGraphSeries()

        Series0.apply {
            title = "X-Axis"
            color = Color.RED
        }
        Series1.apply {
            title = "Y-Axis"
            color = Color.GREEN
        }
        Series2.apply {
            title = "Z-Axis"
            color = Color.BLUE
        }

        rootView.findViewById<GraphView>(R.id.Upload_Graph).apply {
            addSeries(Series0)
            addSeries(Series1)
            addSeries(Series2)
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
}