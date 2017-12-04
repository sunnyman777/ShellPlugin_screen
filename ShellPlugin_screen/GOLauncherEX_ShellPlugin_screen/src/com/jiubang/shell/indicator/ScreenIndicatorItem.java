/**
 * 
 */
package com.jiubang.shell.indicator;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.widget.GLImageView;

/**
 * @author ruxueqin
 * 
 */
public class ScreenIndicatorItem extends GLImageView {
	public int mIndex;

	private Paint mPaint;

	private float mText_X;
	private float mText_Y;

	/**
	 * 绘制模式
	 */
	public static final int DRAW_MODE_GENERAL = 1; // 共性模式 ，使用Indicator组件属性
	public static final int DRAW_MODE_INDIVIDUAL = 2; // 个性模式
	private int mDrawMode = DRAW_MODE_GENERAL;

	/**
	 * @param context
	 */
	public ScreenIndicatorItem(Context context) {
		super(context);
		int id = R.dimen.indicator_numeric_textsize;
		Resources resources = getResources();
		mPaint = new Paint();
		mPaint.setTextSize(resources.getDimensionPixelSize(id));
		mPaint.setColor(0xb3000000);
		mPaint.setAntiAlias(true);
	}

	@Override
	protected void onDraw(GLCanvas canvas) {

		super.onDraw(canvas);

//		if (mDrawMode == DRAW_MODE_GENERAL
//				&& ScreenIndicator.sShowmode.equals(ScreenIndicator.SHOWMODE_NUMERIC)) {
//			// 使用共性模式并开了数字模式才画数字
//			canvas.drawText(Integer.toString(mIndex + 1), mText_X, mText_Y, mPaint);
//		}
	}

//	public void updateTextBound() {
//		if (ScreenIndicator.sShowmode.equals(ScreenIndicator.SHOWMODE_NUMERIC)) {
//			// 把mIndex = 0，就是第一页这样变换是因为1使拿的排版数据比其他数字窄，不居中
//			int value = (mIndex == 0) ? 2 : (mIndex + 1);
//			String string = new String(Integer.toString(value));
//			Rect bounds = new Rect();
//			mPaint.getTextBounds(string, 0, string.length(), bounds);
//			mText_X = (getWidth() - (bounds.right - bounds.left)) / 2;
//			mText_Y = (getHeight() + (bounds.bottom - bounds.top)) / 2;
//			string = null;
//			bounds = null;
//		}
//	}

	/**
	 * @param mDrawMode
	 *            the mDrawMode to set
	 */
	public void setDrawMode(int drawMode) {
		this.mDrawMode = drawMode;
	}
}
