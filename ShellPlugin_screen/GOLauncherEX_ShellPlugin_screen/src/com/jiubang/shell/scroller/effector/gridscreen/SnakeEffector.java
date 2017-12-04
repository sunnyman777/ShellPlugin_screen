package com.jiubang.shell.scroller.effector.gridscreen;

import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.util.DisplayMetrics;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.graphics.NinePatchGLDrawable;
import com.go.proxy.ApplicationProxy;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.shell.animation.ValueAnimation;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.screen.GLSuperWorkspace;

/**
 * 
 * <br>
 * 类描述:双子星效果 <br>
 * 功能详细描述:
 * 
 * @author songsiyu
 * @date [2012-9-3]
 */
class SnakeEffector extends MGridScreenEffector {
	private static final float PI = 3.14f;
	private static final float TRANSLATE_ANGLE = -50f; // 向下倾斜的角度
	private static final float TRANSLATE_YAXIS = 0.70f; // 倾斜的Y方向翻转位置比例
	private static final float TRANSLATE_YOFFSET = 0.123f; // 为了倾斜的视角在中间倾斜后Y轴上移动的位置比例
	private static final int IN_OUT_ANIMATION_DURATION = 400; // 动画时间
	private static final int TRACK_LIGHT_COUNT = 0;
	private static final float LIGHT_OFFSET = 1.25f;
	private float mRatio;
	private float mXLength; // 在X轴上移动的单位长
	private float mYLength; // 在Y轴上移动的单位长
	private boolean mLastRow = false; // 移动到屏幕最后一行，将Y轴移动变为X轴
	private float mSurplusHeight; // 超出屏幕的部分
	DisplayMetrics mMetrics = ApplicationProxy.getContext().getResources().getDisplayMetrics();
	private  Interpolator mInterpolator = new AccelerateInterpolator();
	int mLastOffset;
	boolean mIsScrollReallyFinished = true;
	boolean mIsSCrollStarted = false;
	boolean mIsOnScrollEndAnimate = false;
	private ValueAnimation mTranslateAnimation = new ValueAnimation(0);
	GLDrawable mTrackDrawable;
	private NinePatchGLDrawable mBackgroudDrawable;
	private GLDrawable mBackLightDrawable;
	private GLDrawable mBackBottomLight;
	Rect mRect;
	int mTraceAlpha = ALPHA;
	int mTraceAlphaFactor = -10;
	float mLightArcHeightX; // 光轨的圆弧高度，等于光轨在X轴上的横移减去X周上的直线移动距离
	float mOutterArcLength; // 光点的大圆弧长
	float mXLineTrace; // 光点的横向直线运动的长
	int mIconTextPadding = ShellAdmin.sShellManager.getContext().getResources()
			.getDimensionPixelSize(R.dimen.app_icon_text_pad);
	private long mAlphaAnimationTime;
	private boolean mXLineTraceExpand;
	/**
	 * 是否已经执行了进入动画
	 */
	private boolean mHasDoInAnimate = false; //解决ADT-15038国内包：在第一屏设置蛇形特效后，第一屏划到第二屏特效显示有误
	public SnakeEffector(Rect rect) {
		init(rect);
	}

	private void init(Rect rect) {
		if (mContainer != null) {
			rect.left = 0;
			rect.top = 0;
			rect.right = mContainer.getWidth();
			rect.bottom = mContainer.getHeight();
		}
		mRect = new Rect(0, 0, rect.right - rect.left, rect.bottom - rect.top);
		Resources res = ShellAdmin.sShellManager.getContext().getResources();
		if (mTrackDrawable == null) {
			mTrackDrawable = GLDrawable.getDrawable(res, R.drawable.gl_effect_snake_light);
		}
		int height = (int) (mTrackDrawable.getIntrinsicHeight() * LIGHT_OFFSET);
		mTrackDrawable.setBounds(-mTrackDrawable.getIntrinsicWidth() >> 1, -height,
				mTrackDrawable.getIntrinsicWidth() >> 1, 0);
		if (mBackgroudDrawable == null) {
			NinePatchDrawable drawable = (NinePatchDrawable) res
					.getDrawable(R.drawable.gl_effect_snake_background);
			mBackgroudDrawable = new NinePatchGLDrawable(drawable);
		}
		mBackgroudDrawable.setBounds(mRect);
		if (mBackLightDrawable == null) {
			mBackLightDrawable = GLDrawable.getDrawable(res, R.drawable.gl_effect_snake_backlight);
		}
		if (mBackBottomLight == null) {
			mBackBottomLight = GLDrawable.getDrawable(res, R.drawable.gl_effect_snake_bottom_light);
		}
		Rect backRect = new Rect(mRect.left, mRect.top, mBackLightDrawable.getIntrinsicWidth(),
				mRect.bottom - mBackBottomLight.getIntrinsicHeight());
		mBackLightDrawable.setBounds(backRect);
		Rect bottomRect = new Rect(mRect.left, backRect.bottom,
				mBackBottomLight.getIntrinsicWidth(), mRect.bottom);
		mBackBottomLight.setBounds(bottomRect);
	}

	@Override
	public void onSizeChanged(int w, int h) {
		super.onSizeChanged(w, h);
		mRatio = 1.0f / w;
		if (mContainer != null) {
			init(new Rect(0, 0, mContainer.getWidth(), mContainer.getHeight()));
		}
		reSize();
	}

	private void reSize() {
		if (mContainer instanceof GLSuperWorkspace) {
			if (mMetrics.widthPixels > mMetrics.heightPixels) {
				mXLineTraceExpand = true;
				mSurplusHeight = 0;
			} else if (ShortCutSettingInfo.sEnable) {
				mXLineTraceExpand = false;
				mSurplusHeight = DockUtil.getBgHeight()
						+ ShellAdmin.sShellManager.getContext().getResources()
								.getDimensionPixelSize(R.dimen.dots_indicator_height);
			} else {
				mXLineTraceExpand = false;
				mSurplusHeight = 0;
			}
		} else {
			mSurplusHeight = 0;
		}
		if (mBackLightDrawable != null) {
			float scale = (mContainer.getWidth() + 0.1f)
					/ (mBackLightDrawable.getIntrinsicWidth() * 1.43f);
			Rect backRect = new Rect(mRect.left, mRect.top,
					(int) (mBackLightDrawable.getIntrinsicWidth() * scale), mRect.bottom
							- mBackBottomLight.getIntrinsicHeight());
			mBackLightDrawable.setBounds(backRect);
			Rect bottomRect = new Rect(mRect.left, backRect.bottom,
					(int) (scale * 1.15f * 0.43f * mBackLightDrawable.getIntrinsicWidth()),
					mRect.bottom);
			mBackBottomLight.setBounds(bottomRect);
		}
	}

	@Override
	public void onDrawScreen(GLCanvas canvas, int screen, int offset) {
		onDrawScreen(canvas, screen, (float) offset);
	}
	
	@Override
	public void onDrawScreen(GLCanvas canvas, int screen, float offset) {
		if (!mHasDoInAnimate) {
			onScrollStart();
		}
		final GridScreenContainer container = mContainer;
		final int row = container.getCellRow();
		final int col = container.getCellCol();
		final int screenWidth = container.getWidth();
		int index = row * col * screen;
		final int end = Math.min(container.getCellCount(), index + row * col);
		float t = offset * mRatio;
		final int cellWidth = container.getCellWidth();
		final int cellHeight = container.getCellHeight();
		mHeight = mContainer.getHeight();
		mWidth = mContainer.getWidth();
		mXLength = cellWidth * (col - 1);
		mYLength = mContainer.getCellHeight();
		mLightArcHeightX = mWidth - mTrackDrawable.getIntrinsicWidth() - mXLength;
		final int paddingLeft = container.getPaddingLeft();
		final int paddingTop = container.getPaddingTop();
		float distance; // 总的移动距离
		float deltaX; // 在X轴上的位置参照
		int deltaY; // 在Y轴上的位置参照
		float cellOffset;
		canvas.translate(-offset, 0);
		canvas.translate(-screenWidth * screen, 0);
		boolean animationing = mTranslateAnimation.animate();
		int animateAngle = (int) mTranslateAnimation.getValue();
		if (animationing) {
			canvas.translate(0, -mHeight * TRANSLATE_YOFFSET * (animateAngle / TRANSLATE_ANGLE));
			canvas.translate(screenWidth + mContainer.getWidth() * HALF, mHeight * TRANSLATE_YAXIS);
			canvas.rotateAxisAngle(animateAngle, 1, 0, 0);
			canvas.translate(-(screenWidth + mContainer.getWidth() * HALF), -mHeight
					* TRANSLATE_YAXIS);
			// 背景只画一次
			if (offset <= 0 || screen == 0 && !mScroller.isCircular()) {
				canvas.save();
				canvas.setAlpha((int) (ALPHA * (animateAngle / TRANSLATE_ANGLE)));
				canvas.translate(screenWidth * screen, 0);
				mBackgroudDrawable.draw(canvas);
				mBackLightDrawable.draw(canvas);
				mBackBottomLight.draw(canvas);
				canvas.setAlpha(ALPHA);
				canvas.restore();
			}
		} else if (mIsOnScrollEndAnimate) {
			mIsOnScrollEndAnimate = false;
			mIsScrollReallyFinished = true;
			mHasDoInAnimate = false;
		} else if (!mIsScrollReallyFinished) {
			canvas.translate(0, -mHeight * TRANSLATE_YOFFSET);
			canvas.translate(screenWidth + mContainer.getWidth() * HALF, mHeight * TRANSLATE_YAXIS);
			canvas.rotateAxisAngle(animateAngle, 1, 0, 0);
			canvas.translate(-(screenWidth + mContainer.getWidth() * HALF), -mHeight
					* TRANSLATE_YAXIS);
			// 背景只画一次
			if (offset <= 0 || screen == 0 && !mScroller.isCircular()) {
				canvas.save();
				canvas.setAlpha(ALPHA);
				canvas.translate(screenWidth * screen, 0);
				mBackgroudDrawable.draw(canvas);
				mBackLightDrawable.draw(canvas);
				mBackBottomLight.draw(canvas);
				canvas.restore();
			}
		}
		if (t < 0) {
			t = -t;
		}
		if (t > 1) {
			t = 1;
		}
		if (mScroller.isScrollAtEnd()) {
			t = mInterpolator.getInterpolation(t);
		}
		requestQuality(canvas, GridScreenEffector.DRAW_QUALITY_HIGH);
		for (int i = 0, cellY = paddingTop; i < row && index < end; ++i) {
			for (int j = 0, cellX = paddingLeft; j < col && index < end; ++j, ++index) {
				canvas.save();
				boolean isDraw = true;
				float x, y;
				// 根据offset的正负来确定AB屏幕
				if (offset > 0) {
					if (i % 2 != 0) {
						cellOffset = (col - j - 1) * cellWidth;
					} else {
						cellOffset = j * cellWidth;
					}
					distance = (row * mXLength + (row - 1) * mYLength + cellWidth) * t + cellOffset;
					deltaY = (int) (distance / (mXLength + mYLength));
					mLastRow = deltaY == i;
					deltaX = distance % (mXLength + mYLength);
					x = traceX(deltaX, deltaY);
					y = traceY(deltaX, deltaY);
					if (i % 2 != 0) {
						x = -x + cellOffset;
					} else {
						x = x - cellOffset;
					}
					canvas.translate(x, y);
				} else {
					if (i % 2 != 0) {
						cellOffset = j * cellWidth;
					} else {
						cellOffset = (col - j - 1) * cellWidth;
					}
					distance = (row * mXLength + (row - 1) * mYLength + cellWidth) * t + cellOffset;
					deltaY = (int) (distance / (mXLength + mYLength));
					mLastRow = deltaY == row - 1 - i;
					deltaX = distance % (mXLength + mYLength);
					x = traceX(deltaX, deltaY);
					y = traceYScreenB(deltaX, deltaY);
					if (i % 2 != 0) {
						x = x - cellOffset;
					} else {
						x = -x + cellOffset;
					}
					canvas.translate(x, y);
				}
				//				int cellRow = DrawUtils.sDensity < 2 ? 4 : 5;
				//				float height = row < cellRow ? mHeight - mSurplusHeight - cellHeight / 2 : mHeight
				//						- mSurplusHeight - cellHeight; // TODO: 为什么行数小于默认会出问题
				isDraw = !((cellX + x < -cellWidth)
						|| (cellY + y + cellHeight > mContainer.getHeight() - mSurplusHeight)
						|| (cellX + x > mContainer.getWidth()) || (cellY + y < 0));
				if (isDraw) {
					container.drawScreenCell(canvas, screen, index);
				}
				canvas.restore();
				cellX += cellWidth;
			}
			cellY += cellHeight;
		}
		if (offset > 0) {
			for (int k = 0; k < TRACK_LIGHT_COUNT; k++) {
				drawTrackLight(canvas, screen, row, t, cellWidth, k);
			}
		} else if (offset == 0) {
			if (mTranslateAnimation.animate()) {
				if (mTranslateAnimation.getDstValue() == 0) {
					mTraceAlpha = (int) (ALPHA * (mTranslateAnimation.getValue() / TRANSLATE_ANGLE));
				}
			}
		}
		//		canvas.translate(screenWidth * screen, 0);
		//		mBackLightDrawable.draw(canvas);
	}

	private void drawTrackLight(GLCanvas canvas, int screen, final int row, float t,
			final int cellWidth, int k) {
		mLightArcHeightX = cellWidth >> 1;
		mLightArcHeightX = Math.min(mContainer.getCellHeight() >> 1, mLightArcHeightX);
		mOutterArcLength = getArcLength(getRound(mYLength / 2, mLightArcHeightX), mYLength / 2); // 大圆弧长
		if (mXLineTraceExpand) {
			mXLineTrace = cellWidth * (mContainer.getCellCol() - 1);
			mXLength = mContainer.getWidth() - cellWidth / 2;
		} else {
			mXLineTrace = cellWidth * (mContainer.getCellCol() - 2);
			mXLength = mContainer.getWidth() - cellWidth;
		}
		int offset = mTrackDrawable.getIntrinsicWidth() * 3;
		float allDistance = mOutterArcLength * (row - 1) + mXLineTrace * (row - 2) + mXLength * 2
				+ offset;
		t -= (mTrackDrawable.getIntrinsicWidth() + 0.1f) * k / allDistance;
		float lightDistance = allDistance * t;
		int i;
		float residue;
		if (lightDistance <= mXLength + mOutterArcLength) {
			i = 0;
			residue = lightDistance % (mOutterArcLength + mXLength); // 余
		} else {
			i = (int) ((lightDistance - mOutterArcLength - mXLength) / (mOutterArcLength + mXLineTrace)) + 1;  // 第几次
			residue = (lightDistance - mOutterArcLength - mXLength)
					% (mOutterArcLength + mXLineTrace); // 余
		}
		if (i >= mContainer.getCellRow()) {
			return;
		}
		int saveLightTrace = canvas.save();
		if (mTraceAlpha < 0 && mTraceAlphaFactor < 0 || mTraceAlpha > ALPHA
				&& mTraceAlphaFactor > 0) {
			mTraceAlphaFactor = -mTraceAlphaFactor;
		}
		mTraceAlpha += mTraceAlphaFactor;
		canvas.setAlpha(mTraceAlpha);
		canvas.translate(screen * mContainer.getWidth(), mTrackDrawable.getIntrinsicHeight());
		float[] location = drawLeftLightTrace(row, cellWidth, t, i, residue);
		float x1 = location[0];
		float y1 = location[1];
		canvas.translate(x1, y1);
		mTrackDrawable.draw(canvas);
		canvas.setAlpha(ALPHA);
		canvas.restoreToCount(saveLightTrace);
	}

	/**
	 * 画光点在左边的那条轨迹
	 * @param row
	 * @param cellWidth
	 * @param t
	 * @param  
	 * @return
	 */
	private float[] drawLeftLightTrace(int row, int cellWidth, float t, int i, float residue) {
		float[] trace = new float[2];
		int width = mContainer.getWidth();
		if (i == 0) {
			if (residue <= mXLength) {
				trace[0] = width - residue;
				trace[1] = i * mYLength;
			} else if (residue < mXLength + mOutterArcLength) {
				trace = drawOutterTrace(residue - mXLength, true, i);
			}
		} else {
			if (i == mContainer.getCellRow() - 1 || residue <= mXLineTrace) {
				if (i % 2 == 0) {
					trace[0] = mXLineTrace - residue + cellWidth;
				} else {
					trace[0] = cellWidth + residue;
				}
				trace[1] = i * mYLength;
			} else if (residue < mXLineTrace + mOutterArcLength) {
				if (i % 2 == 0) {
					trace = drawOutterTrace(residue - mXLineTrace, true, i);
				} else {
					trace = drawOutterTrace(residue - mXLineTrace, false, i);
				}
			}
		}
		if (!(i == 0 && residue <= mXLength) && mXLineTraceExpand) {
			trace[0] -= mContainer.getCellWidth() / 2;
		}
		return trace;
	}

	/**
	 * 画光点的大圆弧轨迹
	 * @param trackLength
	 * @param left
	 * @param i 第几次画该轨迹
	 * @return
	 */
	private float[] drawOutterTrace(float trackLength, boolean left, int i) {
		float[] trace = new float[2];
		float r = getRound(mYLength / 2, mLightArcHeightX);
		float theta = getArcAngle(r, mYLength / 2);
		float angle = ((PI * r - theta * r) / 2 + trackLength) / r;
		float x = (float) (Math.sin(angle) * r);
		if (left) {
			trace[0] = mContainer.getCellWidth() - (x - (r - mLightArcHeightX));
		} else {
			trace[0] = mContainer.getCellWidth() + mXLineTrace + (x - (r - mLightArcHeightX));
		}
		if (angle <= PI / 2) {
			trace[1] = (float) (mYLength / 2 - Math.cos(angle) * r) + i * mYLength;
		} else {
			trace[1] = (float) (mYLength / 2 + Math.cos(angle - PI) * r) + i * mYLength;
		}
		return trace;
	}
	/**
	 * 求圆弧半径
	 * @param arcWidthY
	 * @param arcHeightX
	 * @return
	 */
	private float getRound(float arcWidthY, float arcHeightX) {
		return arcHeightX / 2 + arcWidthY * arcWidthY / (2 * arcHeightX);
	}
	/**
	 * 求圆弧的角度
	 * @param r
	 * @param arcWidthY
	 * @return
	 */
	private float getArcAngle(float r, float arcWidthY) {
		return (float) (Math.asin(arcWidthY / r) * 2);
	}
	/**
	 * 求圆弧长
	 * @param r
	 * @param arcWidthY
	 * @return
	 */
	private float getArcLength(float r, float arcWidthY) {
		return (float) (Math.asin(arcWidthY / r) * 2 * r);
	}
	/**
	 * 图标在X轴上的轨迹
	 * @param deltaX
	 * @param deltaY
	 * @return
	 */
	private float traceX(float deltaX, int deltaY) {
		if (deltaX <= mXLength || mLastRow) {
			if (deltaY % 2 == 0) {
				return deltaX;
			} else {
				return mXLength - deltaX;
			}
		} else {
			if (deltaY % 2 == 0) {
				return mXLength;
			} else {
				return 0;
			}
		}
	}
	/**
	 * 图标在Y轴上的轨迹
	 * @param deltaX
	 * @param deltaY
	 * @return
	 */
	private float traceY(float deltaX, int deltaY) {
		if (deltaX <= mXLength || mLastRow) {
			return -deltaY * mYLength;
		} else {
			return -deltaY * mYLength - deltaX + mXLength;
		}
	}
	/**
	 * 第二屏的图标在Y轴上的轨迹
	 * @param deltaX
	 * @param deltaY
	 * @return
	 */
	private float traceYScreenB(float deltaX, int deltaY) {
		if (deltaX <= mXLength || mLastRow) {
			return deltaY * mYLength;
		} else {
			return deltaY * mYLength + deltaX - mXLength;
		}
	}

	public void startInOutAnimation(boolean in) {
		float start;
		if (mTranslateAnimation.animate()) {
			start = mTranslateAnimation.getValue();
		} else {
			start = in ? 0 : TRANSLATE_ANGLE;
		}
		float end = in ? TRANSLATE_ANGLE : 0;
		mTranslateAnimation.start(start, end, IN_OUT_ANIMATION_DURATION);
		mTranslateAnimation.animate();
		if (!in) {
			mAlphaAnimationTime = System.currentTimeMillis();
		}
	}

	@Override
	public void onScrollStart() {
		mHasDoInAnimate = true;
		if (!mIsSCrollStarted) {
			startInOutAnimation(true);
			mIsSCrollStarted = true;
		}

		mIsScrollReallyFinished = false;
		mIsOnScrollEndAnimate = false;
	}

	@Override
	public void onScrollEnd() {
		mIsScrollReallyFinished = false;
		mIsOnScrollEndAnimate = true;
		mIsSCrollStarted = false;
		startInOutAnimation(false);
	}

	private boolean isTimeOut() {
		if (mIsOnScrollEndAnimate
				&& System.currentTimeMillis() - mAlphaAnimationTime > IN_OUT_ANIMATION_DURATION) {
			mIsOnScrollEndAnimate = false;
			mIsScrollReallyFinished = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean isAnimationing() {
		//		if (isTimeOut()) {
		//			return false;
		//		}
		return !mIsScrollReallyFinished;
	}

	@Override
	public void onDetach() {
		mScroller.setDepthEnabled(false);
		super.onDetach();
	}

	@Override
	public void notifyRegetScreenRect() {
		super.notifyRegetScreenRect();
		if (mContainer != null) {
			if (mRect.right - mRect.left != mContainer.getWidth()
					|| mRect.bottom - mRect.top != mContainer.getHeight()) {
				init(new Rect(0, 0, mContainer.getWidth(), mContainer.getHeight()));
			}
			reSize();
		}
	}

	@Override
	public boolean needDrawBackground() {
		return false;
	}

}
