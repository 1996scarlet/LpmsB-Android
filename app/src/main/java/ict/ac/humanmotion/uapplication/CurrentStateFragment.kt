package ict.ac.humanmotion.uapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import com.google.gson.Gson
import kotlinx.android.synthetic.main.state_fragment.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class CurrentStateFragment : MyFragment(), Callback<String> {

    private val TAG = "CurrentStateFragment"
    private val FRAGMENT_TAG = 2

    override var myFragmentTag: Int = FRAGMENT_TAG

    private lateinit var stateView: TextView

    var biasL0 = 0f
    var biasL1 = 0f
    var biasL2 = 0f

    var biasA0 = 0f
    var biasA1 = 0f
    var biasA2 = 0f

    private var dataCounter = 0
    override fun onResponse(call: Call<String>?, response: Response<String>?) {
        stateView.text = response?.body()
    }

    override fun onFailure(call: Call<String>?, t: Throwable) = t.printStackTrace()

    private var resultList: MutableList<SVMDataBean> = mutableListOf()

    val resultService = Retrofit.Builder()
            .client(OkHttpClient())
            .baseUrl("http://192.168.0.2:23456/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build().create(UploadServer::class.java)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.state_fragment, container, false)

        stateView = rootView.findViewById(R.id.text_state)

        rootView.findViewById<ToggleButton>(R.id.toggle).setOnCheckedChangeListener { _, isChecked ->

            if (!isChecked) {

            } else {
                resultList.clear()
                dataCounter = 0
            }
        }

        return rootView
    }

    override fun updateView(d: LpmsBData, s: ImuStatus) {
        if (!s.measurementStarted || !toggle.isChecked) return

        println("measure")

        if (resultList.isEmpty()) {

            biasL0 = d.linAcc[0]
            biasL1 = d.linAcc[1]
            biasL2 = d.linAcc[2]

            biasA0 = d.acc[0]
            biasA1 = d.acc[1]
            biasA2 = d.acc[2]
        }

        resultList.add(SVMDataBean(d.linAcc[0] - biasL0, d.linAcc[1] - biasL1, d.linAcc[2] - biasL2,
                d.acc[0] - biasA0, d.acc[1] - biasA1, d.acc[2] - biasA2))

        if (resultList.size >= 20) {
            resultService.postPredict(Gson().toJson(
                    resultList.subList(dataCounter, dataCounter + 20))).enqueue(this)

            dataCounter++
        }
    }
}