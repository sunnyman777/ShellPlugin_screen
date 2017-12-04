package com.jiubang.shell.common.component;

import android.content.Context;

import com.go.gl.view.GLViewWrapper;
import com.jiubang.ggheart.components.CircleProgressBar;

/**
 * 圆形带进度的进度条
 * @author yangguanxiang
 *
 */
public class GLCircleProgressBar extends GLViewWrapper {

	private CircleProgressBar mProgressBar;
	public GLCircleProgressBar(Context context) {
		super(context);
		mProgressBar = new CircleProgressBar(context);
		setView(mProgressBar, null);
	}

	@Override
	public void setBackgroundColor(int color) {
		mProgressBar.setBackgroundColor(color);
	}

	public void setProgressColor(int color) {
		mProgressBar.setProgressColor(color);
	}

	public void setBackgroundAlpha(int alpha) {
		mProgressBar.setBackgroundAlpha(alpha);
	}

	public void setProgressAlpha(int alpha) {
		mProgressBar.setProgressAlpha(alpha);
	}

	public void setStroke(int stroke) {
		mProgressBar.setStroke(stroke);
	}

	public void setProgress(float progress) {
		mProgressBar.setProgress(progress);
	}

	public float getProgress() {
		return mProgressBar.getProgress();
	}
}
