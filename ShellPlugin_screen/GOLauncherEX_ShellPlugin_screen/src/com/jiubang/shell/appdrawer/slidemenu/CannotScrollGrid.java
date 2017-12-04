package com.jiubang.shell.appdrawer.slidemenu;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;

import com.go.gl.widget.GLGridView;

/**
 * 不可滚动的grid
 */
public class CannotScrollGrid extends GLGridView {

	public CannotScrollGrid(Context context) {
		super(context);
		// 覆盖掉系统自带的selector
		setSelector(new StateListDrawable());
	}

	public CannotScrollGrid(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 覆盖掉系统自带的selector
		setSelector(new StateListDrawable());
	}

	public CannotScrollGrid(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// 覆盖掉系统自带的selector
		setSelector(new StateListDrawable());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
	
}
