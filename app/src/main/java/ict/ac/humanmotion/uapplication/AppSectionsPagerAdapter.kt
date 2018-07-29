package ict.ac.humanmotion.uapplication

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class AppSectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(i: Int): Fragment = when (i) {
        0 -> ConnectionFragment()

        3 -> MoreDataFragment()

        2 -> DataFragment()

        1 -> ThreeDeeCubeFragment()

//        4 -> HumanMotionFragment()
        4 -> CurrentStateFragment()

        else -> DataFragment()
    }

    override fun getCount(): Int = 5

    override fun getPageTitle(position: Int): CharSequence? = when (position) {
        0 -> "Connection"

        3 -> "Orientation Data"

        2 -> "Raw Data"

        1 -> "3D Cube"

//        4 -> "Human Motion"
        4 -> "State"

        else -> "null"
    }

}
