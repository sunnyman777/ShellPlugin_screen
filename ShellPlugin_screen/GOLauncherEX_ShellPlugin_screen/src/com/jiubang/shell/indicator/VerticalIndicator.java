package com.jiubang.shell.indicator;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLView;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.shell.utils.GLImageUtil;

/**
 * 竖向指示器
 *
 */
public class VerticalIndicator extends GLView {
	/**
	 * 指示器图片
	 */
	private GLDrawable mIndicatorDraw = null;

	/**
	 * 当前Y轴上滚动量
	 */
	private int mCurOffset = 0;
	/**
	 * Y轴上最大滚动量
	 */
	private int mTotalOffset = 0;
	/**
	 * 总行数
	 */
	private int mTotalRows = 1;
	/**
	 * 一屏可显示的行数
	 */
	private int mNumRows = 1;
	/**
	 * 指示器高度
	 */
	private int mIndicatorHeight = 0;

	public VerticalIndicator(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		try {
			final Drawable drawableIndicator = getContext().getResources().getDrawable(R.drawable.gl_scrollv);
			if (drawableIndicator != null) {
//				if (drawableIndicator instanceof BitmapDrawable) {
//					mIndicatorDraw = new BitmapGLDrawable((BitmapDrawable) drawableIndicator);
//				} else if (drawableIndicator instanceof NinePatchDrawable) {
//					mIndicatorDraw = new NinePatchGLDrawable((NinePatchDrawable) drawableIndicator);
//				}
				mIndicatorDraw = GLImageUtil.getGLDrawable(drawableIndicator);
			}
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		}
	}

	@Override
	protected void onDraw(GLCanvas canvas) {
		if (null != mIndicatorDraw && mTotalRows > mNumRows) {
			float curProcess = mCurOffset * 1.0f / mTotalOffset;
			int top = (int) ((getHeight() - mIndicatorHeight) * curProcess);
			mIndicatorDraw.setBounds(0, top, getWidth(), top + mIndicatorHeight);
			mIndicatorDraw.draw(canvas);
		}
	}

	public void setParameter(int totalRows, int numRows, int curOffset, int totalOffset) {
		setTotalRows(totalRows);
		setNumRows(numRows);
		setCurOffset(curOffset);
		setLastOffset(totalOffset);
		if (null != mIndicatorDraw && mTotalRows != 0) {
			mIndicatorHeight = getHeight() * mNumRows / mTotalRows;
		} else {
			mIndicatorHeight = 0;
		}
//		requestLayout();
	}
	
	private void setTotalRows(int totalRows) {
		if (mTotalRows != totalRows) {
			mTotalRows = totalRows;
		}
	}
	
	private void setNumRows(int numRow) {
		if (mNumRows != numRow && numRow != 0) {
			mNumRows = numRow;
		}
	}

	public void setCurOffset(int curOffset) {
		if (mCurOffset != curOffset) {
			mCurOffset = curOffset;
			postInvalidate();
		}
	}
	
	private void setLastOffset(int totalOffset) {
		if (mTotalOffset != totalOffset) {
			mTotalOffset = totalOffset;
		}
	}
}
