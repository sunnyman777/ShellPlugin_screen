package com.jiubang.shell.appdrawer.hideapp;

import android.content.Context;
import android.util.AttributeSet;

import com.jiubang.shell.appdrawer.component.GLLightGridViewContainer;

/**
 * 隐藏程序包含指示器的grid组件（还可以扩展指示器位置变换，grid背景变换等功能）
 * @author wuziyi
 *
 */
public class GLHideAppGridViewContainer extends GLLightGridViewContainer {

	public GLHideAppGridViewContainer(Context context) {
		this(context, null);
	}

	public GLHideAppGridViewContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void refreshGridView() {
		mCurGridView.refreshGridView();
	}
	
	public void setGridView() {
		if (mCurGridView == null) {
			setGridView(new GLHideAppGridView(mContext));
		}
	}
}
