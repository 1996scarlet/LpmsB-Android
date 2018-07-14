package ict.ac.humanmotion.uapplication;

import android.content.Context;
import android.opengl.GLSurfaceView;

class LpmsBSurfaceView extends GLSurfaceView {
    public LpmsBRenderer lmRenderer;

	public LpmsBSurfaceView(Context context) {
		super(context);

		lmRenderer = new LpmsBRenderer(context);		
		setRenderer(lmRenderer);	
	}
}