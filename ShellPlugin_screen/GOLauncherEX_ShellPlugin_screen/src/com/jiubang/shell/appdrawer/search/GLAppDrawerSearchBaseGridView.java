package com.jiubang.shell.appdrawer.search;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.Gravity;

import com.go.gl.widget.GLAdapterView.OnItemClickListener;
import com.go.gl.widget.GLGridView;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.appfunc.setting.AppFuncAutoFitManager;
import com.jiubang.ggheart.launcher.IconUtilities;
/**
 * 
 * @author dingzijian
 *
 */
public abstract class GLAppDrawerSearchBaseGridView extends GLGridView
		implements
			OnItemClickListener,
			com.go.gl.widget.GLAdapterView.OnItemLongClickListener {
	protected OnStartSearchResutlListener mSearchResutlListener;
	
	public GLAppDrawerSearchBaseGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnItemClickListener(this);
		setOnItemLongClickListener(this);
		autoFitColumns();
		setStretchMode(STRETCH_COLUMN_WIDTH);
		setGravity(Gravity.CENTER);
		setSelector(new StateListDrawable());
	}

	private void autoFitColumns() {
		int colum = -1;
		if (GoLauncherActivityProxy.isPortait()) {
			int iconStandardSize = DrawUtils.dip2px(56);
			int gridWidthV = GoLauncherActivityProxy.getScreenWidth();
			int iconWidth = Math.round(1.0f
					* IconUtilities.getStandardIconSize(ApplicationProxy.getContext())
					* DrawUtils.dip2px(80) / iconStandardSize);
			colum = gridWidthV / iconWidth;
		} else {
			AppFuncAutoFitManager autoFitManager = AppFuncAutoFitManager.getInstance(ApplicationProxy.getContext());
			colum = autoFitManager.getAppDrawerColumnsH();
		}
		setNumColumns(colum);
	}

	public void setSearchResutlListener(OnStartSearchResutlListener mSearchResutlListener) {
		this.mSearchResutlListener = mSearchResutlListener;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (getMeasuredWidth() == 0 || getMeasuredHeight() == 0) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		} else {
			setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
		}
	}
/**
 * 
 * @author dingzijian
 *
 */
	public interface OnStartSearchResutlListener {
		public void onAppStart(String intent);

		public void onMediaStart(String title);

		public void onAppLocated(String intent);
	}

}
