package ict.ac.humanmotion.uapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView

class HumanMotionFragment : MyFragment() {


    private val TAG = "HumanMotionFragment"
    private val FRAGMENT_TAG = 4

    override var myFragmentTag: Int = FRAGMENT_TAG

    private lateinit var webView:WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.human_fragment, container, false)


        webView = rootView.findViewById<WebView>(R.id.webView).apply {
            webChromeClient = WebChromeClient()
            settings.apply {
                javaScriptEnabled=true
                allowFileAccess = true
                allowFileAccessFromFileURLs = true
            }
            loadUrl("file:///android_asset/six.html")
        }

        return rootView
    }

    override fun updateView(d: LpmsBData, s: ImuStatus) {
        if (!s.measurementStarted) return
        webView.loadUrl("javascript:getMag(${d.mag[0]},${d.mag[1]},${d.mag[2]})")
    }
}