package ict.ac.humanmotion.uapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ThreeDeeCubeFragment : MyFragment() {
    private val FRAGMENT_TAG = 1

    private lateinit var glView: LpmsBSurfaceView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val rootView =
        inflater.inflate(R.layout.fragment_section_dummy, container, false)
//        val args = arguments

        glView = LpmsBSurfaceView(activity)

        return glView
    }

    override fun getMyFragmentTag(): Int = FRAGMENT_TAG

    override fun updateView(d: LpmsBData, s: ImuStatus) {
        if (!s.measurementStarted) return

        glView.lmRenderer.q[0] = d.quat[0]
        glView.lmRenderer.q[1] = d.quat[1]
        glView.lmRenderer.q[2] = d.quat[2]
        glView.lmRenderer.q[3] = d.quat[3]

        glView.requestRender()
    }
}