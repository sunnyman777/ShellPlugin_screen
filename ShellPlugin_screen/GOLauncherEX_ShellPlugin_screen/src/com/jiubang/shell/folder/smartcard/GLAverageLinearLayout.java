package com.jiubang.shell.folder.smartcard;

import android.content.Context;
import android.util.AttributeSet;

import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLView;

/**
 * 
 * @author guoyiqing
 *
 */
public class GLAverageLinearLayout extends GLLinearLayout {

	private int mColumn = 4;

	public GLAverageLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GLAverageLinearLayout(Context context) {
		super(context);
	}
	
	public void setColumn(int column) {
		if (mColumn != column) {
			mColumn = column;
			requestLayout();
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int left = 0;
		int top = 0;
		int width = r - l;
		int height = b - t;
		int itemWidth = width / mColumn;
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			GLView child = getChildAt(i);
			if (child != null && child.isVisible()) {
				child.layout(left, top, left + itemWidth, top + height);
				left += itemWidth;
			}
		}
	}
	
}
