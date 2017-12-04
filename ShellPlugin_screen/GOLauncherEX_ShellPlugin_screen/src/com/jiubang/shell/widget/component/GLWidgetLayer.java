package com.jiubang.shell.widget.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;

import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.IShell;
import com.jiubang.shell.IView;

/**
 * 
 * <br>类描述:显示全屏widget
 * <br>功能详细描述:
 * 
 * @author  luopeihuan
 * @date  [2012-10-24]
 */
public class GLWidgetLayer extends GLFrameLayout implements IView {
	private GLView mWidgetFullView;
	
	private Runnable mRemoveRunnable = new Runnable() {
		@Override
		public void run() {
			removeAllViews();
		}
	};

	public GLWidgetLayer(Context context) {
		this(context, null);
	}
	
	public GLWidgetLayer(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
	}
	
	public GLWidgetLayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}
	
	public void showFullWidget(GLView widgetView) {
		setVisibility(VISIBLE);
		mWidgetFullView = widgetView;
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		addView(widgetView, lp);
	}
	
	public void hideFullWidget() {
		setVisibility(INVISIBLE);
		if (mWidgetFullView != null) {
			post(mRemoveRunnable);
			mWidgetFullView = null;
		}
	}

	@Override
	public void setVisible(boolean visible, boolean animate, Object obj) {
		setVisible(visible);
	}

	@Override
	public void setShell(IShell shell) {
		
	}

	@Override
	public int getViewId() {
		return IViewId.WIDGET_LAYER;
	}

	@Override
	public void onAdd(GLViewGroup parent) {
		
	}

	@Override
	public void onRemove() {
		
	}
}
