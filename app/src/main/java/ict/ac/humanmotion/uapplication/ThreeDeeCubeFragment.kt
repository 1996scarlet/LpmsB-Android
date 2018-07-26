package ict.ac.humanmotion.uapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import com.google.gson.Gson
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
    override fun onResponse(call: Call<String>?, response: Response<String>?) =
            println("Human Motion Data Uploaded")

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
            if (text_motion.text.isNullOrEmpty()) return@setOnCheckedChangeListener

            if (!isChecked) {
                Log.d(TAG, "Sending message to server!")
                resultService.postSave(text_motion.text.toString(), Gson().toJson(
                        resultList.map { SVMDataBean(it.gyr[0], it.gyr[1], it.gyr[2], it.quat[0], it.quat[1], it.quat[2], it.quat[3]) })).enqueue(this)

            } else {
                resultList.clear()
            }
        }

        return rootView
    }

    override fun updateView(d: LpmsBData, s: ImuStatus) {
        if (!s.measurementStarted || !toggle.isChecked) return
        Log.d(TAG, "Measuring data from lpms!")
        resultList.add(d)
    }
}