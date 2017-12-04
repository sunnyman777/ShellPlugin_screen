package com.jiubang.shell.common.component;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLFrameLayout;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.util.graphics.DrawUtils;
import com.jiubang.shell.orientation.GLOrientationControler;
import com.jiubang.shell.theme.GLAppDrawerThemeControler;
import com.jiubang.shell.utils.GLImageUtil;

/**
 * 
 * <br>类描述: 加载框 
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 */
public class GLProgressBar extends GLFrameLayout {
	/**
	 * 圆圈样式
	 */
	public final static int MODE_INDETERMINATE = 0;
	/**
	 * 水平样式
	 */
	public final static int ORIENTATION_HORIZONTAL = 1;
	/**
	 * 垂直样式
	 */
	public final static int ORIENTATION_VERTICAL = 2;

	private int mMode = MODE_INDETERMINATE;
	/**
	 * 圆圈样式图片
	 */
	private GLDrawable mIndeterminateProgressDrawable;
	/**
	 * 水平／垂直样式当前进度图
	 */
	protected GLDrawable mDeterminateProgressDrawable;
	private Rect mIndeterminateProgressBound = new Rect();
	private int mProgressBarWidht;
	private int mProgressBarHeight;
	private int mCurrentRotate = 0;
	private Drawable mBackground;
	private final static int PROGRESS_STEP = 10;
	protected GLAppDrawerThemeControler mThemeCtrl;
	/**
	 * 水平／垂直样式最大值
	 */
	private long mMaxProgress = 0;
	/**
	 * 水平／垂直样式当前值
	 */
	private long mCurProgress = 0;
	/**
	 * 水平／垂直样式当前进度图的Bound
	 */
	private Rect mDeterminateProgressBound = new Rect();

	public GLProgressBar(Context context, int mode, Drawable prgDrawable, Drawable maxDrawable) {
		this(context);
		init(mode, prgDrawable, maxDrawable);
	}
	
	public GLProgressBar(Context context) {
		this(context, null);
	}
	
	public GLProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mThemeCtrl = GLAppDrawerThemeControler.getInstance(mContext);
	}
	
	private void init(int mode, Drawable prgDrawable, Drawable backGround) {
		mMode = mode;
		switch (mMode) {
			case MODE_INDETERMINATE :
				setIndeterminateProgressDrawable(prgDrawable);
				break;
			case ORIENTATION_HORIZONTAL :
			case ORIENTATION_VERTICAL :
				setDeterminateProgressDrawable(prgDrawable);
				break;
			default :
				break;
		}
		setBackgroundDrawable(backGround);
	}

	public void setIndeterminateProgressDrawable(Drawable drawable) {
		if (mIndeterminateProgressDrawable != null) {
			mIndeterminateProgressDrawable.clear();
		}
		if (drawable != null) {
			mIndeterminateProgressDrawable = GLImageUtil.getGLDrawable(drawable);
		} else { // 图片为空初始化默认图片
			mIndeterminateProgressDrawable = GLImageUtil.getGLDrawable(mContext.getResources().getDrawable(
					R.drawable.gl_progressbar_indeterminate_white));
		}
		if (mIndeterminateProgressDrawable != null) {
			mProgressBarWidht = mIndeterminateProgressDrawable.getIntrinsicWidth();
			mProgressBarHeight = mIndeterminateProgressDrawable.getIntrinsicHeight();
		}
	}
	
	/**
	 * 设置水平／竖直样式进度条图片
	 * @param prgDrawable
	 */
	public void setDeterminateProgressDrawable(Drawable prgDrawable) {
		if (mDeterminateProgressDrawable != null) {
			mDeterminateProgressDrawable.clear();
		}
		if (prgDrawable != null) {
			mDeterminateProgressDrawable = GLImageUtil.getGLDrawable(prgDrawable);
		} else {
			mDeterminateProgressDrawable = GLImageUtil
					.getGLDrawable(R.drawable.gl_appdrawer_process_memory_green);
		}
	}
	
	public void setBackgroundDrawable(Drawable drawable) {
		if (drawable == null) {
			if (mMode == MODE_INDETERMINATE) {
//				mBackground = mContext.getResources().getDrawable(R.drawable.gl_progressbar_bg);
			} else {
				mBackground = mContext.getResources().getDrawable(R.drawable.gl_appdrawer_process_memory_bg);
			}
		} else {	
			mBackground = GLImageUtil.getGLDrawable(drawable);
		}
		super.setBackgroundDrawable(mBackground);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		switch (mMode) {
			case MODE_INDETERMINATE :
				resetIndeterminateProgressBound();
				break;
			case ORIENTATION_HORIZONTAL :
			case ORIENTATION_VERTICAL :
				resetDeterminateProgressBound();
				break;
			default :
				break;
		}
	}

	private void resetIndeterminateProgressBound() {
		mIndeterminateProgressBound.left = (mWidth - mProgressBarWidht) / 2;
		mIndeterminateProgressBound.top = (mHeight - mProgressBarHeight) / 2;
		mIndeterminateProgressBound.right = mIndeterminateProgressBound.left + mProgressBarWidht;
		mIndeterminateProgressBound.bottom = mIndeterminateProgressBound.top + mProgressBarHeight;
		mIndeterminateProgressDrawable.setBounds(mIndeterminateProgressBound);
	}
	
	private void resetDeterminateProgressBound() {
		if (mMode == ORIENTATION_HORIZONTAL) {
      		int prgRight = (int) (1.0f * mWidth * mCurProgress / mMaxProgress);
			mDeterminateProgressBound.left = 0;
			mDeterminateProgressBound.top = 0;
			mDeterminateProgressBound.right = prgRight;
			mDeterminateProgressBound.bottom = mHeight;
		} else if (mMode == ORIENTATION_VERTICAL) {
			int prgHeight = (int) (1.0f * mHeight * mCurProgress / mMaxProgress);
			mDeterminateProgressBound.left = 0;
			mDeterminateProgressBound.top = mHeight - prgHeight;
			mDeterminateProgressBound.right = mWidth;
			mDeterminateProgressBound.bottom = mHeight;
		}
		if (mDeterminateProgressDrawable != null) {
			mDeterminateProgressDrawable.setBounds(mDeterminateProgressBound);
		}
	}

	public void show() {
		GLOrientationControler.keepOrientationAllTheTime(true);
		setVisible(true);
		if (mMode == MODE_INDETERMINATE) {
			mCurrentRotate = 0;
			invalidate();
		}
	}

	public void hide() {
		GLOrientationControler.keepOrientationAllTheTime(false);
		setVisible(false);
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		if (getHeight() == GoLauncherActivityProxy.getScreenHeight()) {
			Drawable bg = getBackground();
			if (bg != null) {
				bg.setBounds(0, 0, getWidth(), getHeight() + DrawUtils.getNavBarHeight());
			}
		}
		super.dispatchDraw(canvas);
		switch (mMode) {
			case MODE_INDETERMINATE :
				drawIndeterminateProgress(canvas);
				break;
			case ORIENTATION_HORIZONTAL :
			case ORIENTATION_VERTICAL :
				drawDeterminateProgress(canvas);
				break;
			default :
				break;
		}
	}

	private void drawIndeterminateProgress(GLCanvas canvas) {
		if (mIndeterminateProgressDrawable == null) {
			return;
		}
		mCurrentRotate += PROGRESS_STEP;
		if (mCurrentRotate >= 360) {
			mCurrentRotate = 0;
		}
		canvas.rotate(mCurrentRotate, mWidth / 2, mHeight / 2);
		mIndeterminateProgressDrawable.draw(canvas);
		invalidate();
	}
	
	private void drawDeterminateProgress(GLCanvas canvas) {
		if (mDeterminateProgressDrawable != null) {
			mDeterminateProgressDrawable.draw(canvas);
		}
	}

	@Override
	public void cleanup() {
		super.cleanup();
		if (mIndeterminateProgressDrawable instanceof GLDrawable) {
			((GLDrawable) mIndeterminateProgressDrawable).clear();	//XXX:如果mBGDrawable有其他地方引用，会导致其他地方绘制不出来
		} else {
			releaseDrawableReference(mIndeterminateProgressDrawable);
		}
		mIndeterminateProgressDrawable = null;
		if (mBackground instanceof GLDrawable) {
			((GLDrawable) mBackground).clear();	//XXX:如果mBGDrawable有其他地方引用，会导致其他地方绘制不出来
		} else {
			releaseDrawableReference(mBackground);
		}
		mIndeterminateProgressDrawable = null;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}
	
	public void setMode(int mode) {
		mMode = mode;
	}
	
	public void setCurProgress(long curProgress) {
		mCurProgress = curProgress;
		resetDeterminateProgressBound();
	}
	
	public void setMaxProgress(long maxProgress) {
		mMaxProgress = maxProgress;
	}
	
	public long getCurProgress() {
		return mCurProgress;
	}
	
	public long getMaxProgress() {
		return mMaxProgress;
	}
	
	public Rect getDeterminateProgressBound() {
		return mDeterminateProgressBound;
	}
}
