package com.jiubang.shell.scroller;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.animation.Interpolator;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.scroller.FastVelocityTracker;
import com.jiubang.shell.screen.back.GLCycloidDrawListener;
import com.jiubang.shell.screen.back.GLIWallpaperDrawer;

/**
 * 分屏视图的滚动器。 到两端继续切换就到另外一端
 * 
 * @author dengweiming
 * 
 */
class ShellCycloidScreenScroller extends ShellScreenScroller implements GLCycloidDrawListener {
	private static final int ALPHA = 255;
	boolean mFadeAtEndEnabled = true; // 当切换到两端回绕的时候使用淡入淡出的方式衔接背景壁纸

	static final int reduceOneCycle(int index, int count) {
		while (index < 0) {
			index += count;
		}
		while (index >= count) {
			index -= count;
		}
		return index;
	}

	public ShellCycloidScreenScroller(Context context, ShellScreenScrollerListener screenGroup) {
		this(context, screenGroup, null);
	}

	public ShellCycloidScreenScroller(Context context, ShellScreenScrollerListener screenGroup,
			FastVelocityTracker tracker) {
		super(context, screenGroup, tracker);
		mCycloid = true;
	}

	@Override
	public void setPadding(float paddingFactor) {
		if (mTotalSize <= 0) {
			return;
		}
		// 忽略 paddingFactor，保留 mPaddingFactor为0.5f的默认值
		//CHECKSTYLE:OFF
		if (mPaddingFactor == 0.5f) {
			return;
		}
		abortAnimation();
		//CHECKSTYLE:OFF
		mPaddingFactor = 0.5f; // 注意在setScreenCount方法中已经修改它了，这里还原
		// mState = FINISHED;
		mMinScroll = -mScreenSize / 2;
		mMaxScroll = mTotalSize + mMinScroll;
		mScrollRatio = mMaxScroll > mMinScroll ? 1.0f / (mMaxScroll - mMinScroll) : 0;

		// 重设当前滚动量
		scrollScreenGroup(getDstScreen() * mScreenSize);
		mScrollFloat = mScroll;
	}

	@Override
	protected void scrollScreenGroup(int newScroll) {
		int scroll = Math.round(rewindScroll(newScroll));
		if (mState == ON_SCROLL) {
			mEndScroll += scroll - newScroll;
		}
		super.scrollScreenGroup(scroll);
		mFloatIndex = newScroll / (float) mScreenSize;
		mScrollFloat = rewindScroll(getScrollFloat());
	}

	@Override
	protected int computeScreenIndex(int scroll) {
		final int index = super.computeScreenIndex(scroll);
		return 0 <= index && index < mScreenCount ? index : 0;
	}

	@Override
	public void onScroll(int delta) {
		if ((mScroll > -mScreenSize && mScroll < 0)
				|| (mScroll > mScreenSize * (mScreenCount - 1) && mScroll < mScreenSize
						* mScreenCount)) {
			final int newScroll = mScroll + delta;
			if (newScroll < 0 || newScroll >= mLastScreenPos) {
				delta = onScrollAtEnd(delta);
			}

			if (delta == 0) {
				invalidate();
				return;
			}
			mEndScroll += delta;
			if (mDeferScrollUpdate) {
				mFirstSmooth = true;
				mSmoothingTime = System.nanoTime() * ONE_OVER_NANOTIME_DIV;
				invalidate();
			} else {
				scrollScreenGroup(mEndScroll);
			}
		} else {
			super.onScroll(delta);
		}
	}

	@Override
	public boolean computeScrollOffset() {
		if ((mScroll > -mScreenSize && mScroll < 0)
				|| (mScroll > mScreenSize * (mScreenCount - 1) && mScroll < mScreenSize
						* mScreenCount)) {
			if (mScrollComputed) {
				if (mState != FINISHED) {
					invalidateScroll();
					invalidate();
					return true;
				}
				return false;
			}
			return super.computeScrollOffset();
		} else {
			return super.computeScrollOffset();
		}
	}
	
	@Override
	protected int onScrollAtEnd(int delta) {
		if (mScreenCount < 2) { // 只有一屏时不循环滚动
			return super.onScrollAtEnd(delta);
		}
		return delta;
	}

	float rewindScroll(float scroll) {
		if (mTotalSize == 0) {
			return 0;
		}
		if (mScreenCount > 1) { // 只有一屏时不进行
			scroll %= mTotalSize;
			if (scroll < mMinScroll) {
				scroll += mTotalSize;
			} else if (scroll >= mMaxScroll) {
				scroll -= mTotalSize;
			}
		}
		return scroll;
	}

	@Override
	protected boolean flingToScreen(int dstScreen, int duration) {
		return gotoScreen(dstScreen, duration, mInterpolatorBak);
	}

	@Override
	protected int checkScreen(int screen) {
		return screen;
	}

	/**
	 * @param dstScreen
	 *            约定位于范围[-1, mScreenCount], 除非使用
	 *            {@link #setGoShortPathEnabled(boolean)}禁掉走最近路径
	 */
	@Override
	protected boolean gotoScreen(int dstScreen, int duration, Interpolator interpolator) {
		if (mGoShortPath) {
			if (dstScreen > mCurrentScreen && (dstScreen - mCurrentScreen) * 2 > mScreenCount) {
				dstScreen -= mScreenCount;
			} else if (dstScreen < mCurrentScreen
					&& (mCurrentScreen - dstScreen) * 2 > mScreenCount) {
				dstScreen += mScreenCount;
			}
		}
		return super.gotoScreen(dstScreen, duration, interpolator);
	}

	@Override
	public int getDstScreen() {
		return mDstScreen = reduceOneCycle(mDstScreen, mScreenCount);
	}

	@Override
	public boolean isScrollAtEnd() {
		if (mScreenCount < 2) {
			return super.isScrollAtEnd();
		}
		return false;
	}

	@Override
	public boolean isOldScrollAtEnd() {
		if (mScreenCount < 2) {
			return super.isOldScrollAtEnd();
		}
		return false;
	}

	@Override
	public boolean drawBackground(GLCanvas canvas, int scroll) {
		if (mWallpaperDrawer != null) {
			if (!super.drawBackground(canvas, scroll)) {
				return false;
			}
		} else {

			if (!super.drawBackground(canvas, scroll)) {
				return false;
			}
			if (!mBackgroundScrollEnabled || !mFadeAtEndEnabled || mBgAlwaysDrawn
					|| mBackgroundDrawable == null || mScreenCount < 2
					|| (mState == ShellMScroller.ON_FLING && mIsOvershooting)) {
				return true;
			}
			int alpha = 0;
			if (scroll > mLastScreenPos) {
				alpha = (scroll - mLastScreenPos) * ALPHA / mScreenSize;
				scroll -= mTotalSize;
			} else if (scroll < 0) {
				alpha = -scroll * ALPHA / mScreenSize;
				scroll += mTotalSize;
			}
			if (alpha != 0) {
				if (mBitmap != null && mPaint != null) {
					mPaint.setAlpha(alpha);
					super.drawBackground(canvas, scroll);
					mPaint.setAlpha(ALPHA);
				} else {
					// mBackgroundDrawable.setAlpha(alpha);
					final int savedAlpha = canvas.getAlpha();
					canvas.multiplyAlpha(alpha);
					super.drawBackground(canvas, scroll);
					canvas.setAlpha(savedAlpha);
					// mBackgroundDrawable.setAlpha(ALPHA);
				}
			}
		}
		return true;
	}

	@Override
	public int getBackgroundOffsetX(int scroll) {
		if (scroll > mLastScreenPos) {
			scroll = (scroll + mLastScreenPos) / 2;
		} else if (scroll < 0) {
			scroll /= 2;
		}
		return super.getBackgroundOffsetX(scroll);
	}

	@Override
	public void setBackground(Drawable drawable) {
		super.setBackground(drawable);
		if (mBitmap != null) {
			// 背景壁纸为BitmapDrawable类型的
			mPaint = new Paint();
		} else {
			mPaint = null;
		}
	}

	@Override
	public int getPreviousScreen() {
		return reduceOneCycle(mCurrentScreen - 1, mScreenCount);
	}

	@Override
	public int getNextScreen() {
		return reduceOneCycle(mCurrentScreen + 1, mScreenCount);
	}

	/**
	 * 返回当前绘制的左边子屏索引
	 * 
	 * @return -1表示无效索引
	 */
	@Override
	public int getDrawingScreenA() {
		int drawingScreenA = mCurrentScreen;
		if (getCurrentScreenOffset() > 0) {
			--drawingScreenA;
		}
		int res = reduceOneCycle(drawingScreenA, mScreenCount);
		if (mScreenCount < 2 && res != drawingScreenA) {
			return -1;
		}
		return res;
	}

	/**
	 * 返回当前绘制的右边子屏索引
	 * 
	 * @return -1表示无效索引（在只绘制一屏的时候也是返回-1）
	 */
	@Override
	public int getDrawingScreenB() {
		int drawingScreenB = mCurrentScreen;
		final int offset = getCurrentScreenOffset();
		if (offset == 0) {
			return -1;
		}
		if (offset < 0) {
			++drawingScreenB;
		}
		int res = reduceOneCycle(drawingScreenB, mScreenCount);

		if (mScreenCount < 2 && res != drawingScreenB) {
			return -1;
		}
		return res;
	}

	/**
	 * 因为是循环滚动的，因此往两个方向都可以到达目标位置，启用这个选项可以选择较近的方向，默认是启用的。 如果不启用，那么
	 * {@link #gotoScreen(int, int, boolean)} 可以不限制目标位置在[-1, mScreenCount]
	 * 
	 * @param enabled
	 */
	@Override
	public void setGoShortPathEnabled(boolean enabled) {
		mGoShortPath = enabled;
	}
	
	@Override
	public void setWallpaperDrawer(GLIWallpaperDrawer wallpaperDrawer) {
		super.setWallpaperDrawer(wallpaperDrawer);
		if (wallpaperDrawer != null) {
			wallpaperDrawer.setCycloidDrawListener(this);
		}
	}

	@Override
	public int getCurrentAlpha() {
		int alpha = 0;
		int scroll = mScroll;
		if (!mBackgroundScrollEnabled || !mFadeAtEndEnabled || mBgAlwaysDrawn
				|| mWallpaperDrawer == null || mWallpaperDrawer.getBackgroundDrawable() == null 
				|| mScreenCount < 2 || (mState == ShellMScroller.ON_FLING && mIsOvershooting)) {
			return alpha;
		}
		if (scroll > mLastScreenPos) {
			alpha = (scroll - mLastScreenPos) * 255 / mScreenSize;
			scroll -= mTotalSize;
		} else if (scroll < 0) {
			alpha = -scroll * 255 / mScreenSize;
			scroll += mTotalSize;
		}
		return alpha;
	}

	@Override
	public int getBackgroundX(int scroll) {
		int x = -getBackgroundOffsetX(scroll);
		if (mOrientation == HORIZONTAL) {
			x += mScroll;
		}
		return x;
	}

	@Override
	public int getCycloidScroll() {
		int scroll = mScroll;
		if (scroll > mLastScreenPos) {
			scroll -= mTotalSize;
		} else if (scroll < 0) {
			scroll += mTotalSize;
		}
		return scroll;
	}
}
