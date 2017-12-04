package com.jiubang.shell.appdrawer.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.go.gl.view.GLView;
import com.jiubang.shell.appdrawer.component.GLLightGridViewContainer;

/**
 * widget管理包含指示器的grid组件（还可以扩展指示器位置变换，grid背景变换等功能）
 * @author wuziyi
 *
 */
public class GLWidgetContainer extends GLLightGridViewContainer {

	public GLWidgetContainer(Context context) {
		this(context, null);
	}

	public GLWidgetContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void refreshGridView() {
		mCurGridView.refreshGridView();
	}
	
	public void reloadIconPreView(String packageName) {
		if (packageName != null) {
			GLView view = mCurGridView.getAdapter().getViewByKey(packageName);
			if (view instanceof GLWidgetView) {
				GLWidgetView widget = (GLWidgetView) view;
				widget.startLoadIcon();
			}
		}
	}
	
	public void setGridView() {
		if (mCurGridView == null) {
			setGridView(new GLWidgetGrid(mContext));
		}
	}
	
	public void startLoadIcon() {
		if (mCurGridView instanceof GLWidgetGrid) {
			((GLWidgetGrid) mCurGridView).startLoadIcon();
		}
	}
}
