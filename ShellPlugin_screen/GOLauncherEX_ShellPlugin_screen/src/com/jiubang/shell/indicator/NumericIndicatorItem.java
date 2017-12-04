package com.jiubang.shell.indicator;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.widget.GLTextViewWrapper;

/**
 * 
 * @author zhujian
 *
 */
public class NumericIndicatorItem extends GLTextViewWrapper {

	private Drawable mDrawable;

	public NumericIndicatorItem(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void onDraw(GLCanvas canvas) {
		// TODO Auto-generated method stub
		canvas.drawDrawable(mDrawable);
		super.onDraw(canvas);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub

		super.onLayout(changed, left, top, right, bottom);
		int l = (mWidth - mDrawable.getIntrinsicWidth()) / 2;
		int t = (mHeight - mDrawable.getIntrinsicHeight()) / 2;
		int r = l + mDrawable.getIntrinsicWidth();
		int b = t + mDrawable.getIntrinsicHeight();
		mDrawable.setBounds(l, t, r, b);

	}

	public void setDrawable(Drawable drawable) {
		mDrawable = drawable;
		int l = (mWidth - mDrawable.getIntrinsicWidth()) / 2;
		int t = (mHeight - mDrawable.getIntrinsicHeight()) / 2;
		int r = l + mDrawable.getIntrinsicWidth();
		int b = t + mDrawable.getIntrinsicHeight();
		mDrawable.setBounds(l, t, r, b);
	}
}
