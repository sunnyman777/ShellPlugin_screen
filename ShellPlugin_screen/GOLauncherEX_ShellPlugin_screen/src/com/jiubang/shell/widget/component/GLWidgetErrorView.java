package com.jiubang.shell.widget.component;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import com.go.gl.view.GLView;
import com.go.gl.widget.GLTextView;
import com.go.gowidget.core.IGoWidget3D;
import com.go.gowidget.core.WidgetCallback;

/**
 * 3DWidget加载错误时显示的view
 * 
 * @author luopeihuan
 * 
 */
public class GLWidgetErrorView extends GLTextView implements IGoWidget3D {
	
	public GLWidgetErrorView(Context context) {
		super(context);
	}

	public GLWidgetErrorView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GLWidgetErrorView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public GLView getContentView() {
		return this;
	}

	@Override
	public void setWidgetCallback(WidgetCallback callback) {
	}

	@Override
	public void onStart(Bundle data) {
	}

	@Override
	public void onStop() {
	}

	@Override
	public void onDelete() {
	}

	@Override
	public void onRemove() {
	}

	@Override
	public boolean onApplyTheme(Bundle data) {
		return false;
	}

	@Override
	public void onEnter() {
	}

	@Override
	public void onLeave() {
	}

	@Override
	public boolean onActivate(boolean animate, Bundle data) {
		return false;
	}

	@Override
	public boolean onDeactivate(boolean animate, Bundle data) {
		return false;
	}

	@Override
	public void onClearMemory() {
		
	}

	@Override
	public void onEnableInvalidate() {

	}

	@Override
	public void onDisableInvalidate() {

	}

	@Override
	public int getBackgroundAnimationType() {
		return 0;
	}

	@Override
	public GLView getKeepView() {
		return null;
	}
}
