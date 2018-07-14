package ict.ac.humanmotion.uapplication;

import android.support.v4.app.Fragment;

public abstract class MyFragment extends Fragment {
	public interface MyFragmentListener { 
		void onUserInput(int mode, String data);
	}

	public abstract int getMyFragmentTag();
	public abstract void updateView(LpmsBData d, ImuStatus s);
}
