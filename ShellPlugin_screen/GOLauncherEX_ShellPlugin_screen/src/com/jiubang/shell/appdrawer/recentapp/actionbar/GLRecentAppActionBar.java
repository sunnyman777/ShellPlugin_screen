package com.jiubang.shell.appdrawer.recentapp.actionbar;

import android.content.Context;
import android.util.AttributeSet;

import com.go.gl.view.GLFrameLayout;
import com.jiubang.shell.appdrawer.IActionBar;

/**
 * 最近打开底部栏
 * @author yangguanxiang
 *
 */
public class GLRecentAppActionBar extends GLFrameLayout implements IActionBar {


	public GLRecentAppActionBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean needDrawBg() {
		return false;
	}

	@Override
	public void onInOutAnimationStart(boolean in) {

	}

	@Override
	public void onInOutAnimationEnd(boolean in) {

	}

	@Override
	public void onConfigurationChanged() {

	}

	@Override
	public void onParentInOutAnimationStart(boolean in) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onParentInOutAnimationEnd(boolean in) {
		// TODO Auto-generated method stub

	}

}
