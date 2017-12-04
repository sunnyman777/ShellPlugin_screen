package com.jiubang.shell.popupwindow.component;

import android.content.Context;

import com.go.gl.graphics.ColorGLDrawable;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLFrameLayout;

/**
 * 
 * @author yangguanxiang
 *
 */
public class GLPopupWindowMiddleView extends GLFrameLayout {

	private static final int COLOR_DARK = 0xff000000;
	private ColorGLDrawable mDarkDrawable = new ColorGLDrawable(COLOR_DARK);
	private int mDarkAlpha = 0;

	public GLPopupWindowMiddleView(Context context) {
		super(context);
		setHasPixelOverlayed(false);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		mDarkDrawable.setBounds(0, 0, mWidth, mHeight);
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		mDarkDrawable.setAlpha(mDarkAlpha);
		if (mDarkAlpha > 0) {
			mDarkDrawable.draw(canvas);
		}
		super.dispatchDraw(canvas);
	}

	public void setDarkAlpha(int alpha) {
		mDarkAlpha = alpha;
	}

	public void reset() {
		mDarkAlpha = 0;
		setDrawingCacheEnabled(false);
		clearAnimation();
	}
}
