package com.jiubang.shell.appdrawer.component;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.scroller.ScreenScroller;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.util.graphics.DrawUtils;
import com.jiubang.shell.common.component.AbsScrollableGridViewHandler.ScrollZoneListener;
import com.jiubang.shell.common.component.GLScrollableBaseGrid;
import com.jiubang.shell.common.component.HorScrollableGridViewHandler;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.model.CommonImageManager;
import com.jiubang.shell.scroller.effector.CoupleScreenEffector;
import com.jiubang.shell.utils.GLImageUtil;

/**
 * 单行或单列可滚动GridView（横屏时左右滚动，竖屏时上下滚动）
 * @author yangguanxiang
 *
 */
public abstract class GLLinearScrollableGridView extends GLScrollableBaseGrid {

	private boolean mShowNextLabel;
	private boolean mShowPreLabel;
	private GLDrawable mNextLabelV;
	private GLDrawable mNextLabelH;
	private GLDrawable mPreLabelV;
	private GLDrawable mPreLabelH;
	private GLDrawable mEdgeHightLight;
	protected boolean mFourceHorMode;
	private int mAlphaStep;
	protected boolean mIsInScrollZone;
	protected ScrollZoneListener mListener;
	protected boolean mIsHitLabelRect;
	protected ShellTextViewWrapper mNoDataView;
	protected boolean mNoData;

	public GLLinearScrollableGridView(Context context) {
		super(context);
		init();
	}
	
	public GLLinearScrollableGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		mNextLabelV = getNextLabelV();
		mNextLabelH = getNextLabelH();
		mPreLabelV = getPreLabelV();
		mPreLabelH = getPreLabelH();
		mEdgeHightLight = GLImageUtil.getGLDrawable(CommonImageManager.getInstance().getDrawable(
				CommonImageManager.RES_ACTION_BAR_CROSSOVER));
		initNoDataView();
		handleRowColumnSetting(false);
		handleScrollerSetting();
	}
	
	protected GLDrawable getNextLabelV() {
		return GLImageUtil.getGLDrawable(CommonImageManager.getInstance().getDrawable(
				CommonImageManager.RES_ACTION_BAR_NEXTPAGE_V));
	}
	
	protected GLDrawable getNextLabelH() {
		return GLImageUtil.getGLDrawable(CommonImageManager.getInstance().getDrawable(
				CommonImageManager.RES_ACTION_BAR_NEXTPAGE_H));
	}
	
	protected GLDrawable getPreLabelV() {
		return GLImageUtil.getGLDrawable(CommonImageManager.getInstance().getDrawable(
				CommonImageManager.RES_ACTION_BAR_PREPAGE_V));
	}
	
	protected GLDrawable getPreLabelH() {
		return GLImageUtil.getGLDrawable(CommonImageManager.getInstance().getDrawable(
				CommonImageManager.RES_ACTION_BAR_PREPAGE_H));
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (changed) {
			handleRowColumnSetting(false);
			handleScrollerSetting();
			int width = right - left;
			int height = bottom - top;
			if (GoLauncherActivityProxy.isPortait() || mFourceHorMode) {
				int padding = DrawUtils.dip2px(4);
				if (mNextLabelV != null) {
					mNextLabelV.setBounds(
							width - mNextLabelV.getIntrinsicWidth() - padding,
							(height - mNextLabelV.getIntrinsicHeight()) / 2,
							width - padding,
							(height - mNextLabelV.getIntrinsicHeight()) / 2
									+ mNextLabelV.getIntrinsicHeight());
				}
				if (mPreLabelV != null) {
					mPreLabelV.setBounds(
							padding,
							(height - mPreLabelV.getIntrinsicHeight()) / 2,
							padding + mPreLabelV.getIntrinsicWidth(),
							(height - mPreLabelV.getIntrinsicHeight()) / 2
									+ mPreLabelV.getIntrinsicHeight());
				}
			} else {
				int padding = DrawUtils.dip2px(2);
				if (mNextLabelH != null) {
					mNextLabelH.setBounds(
							(width - mNextLabelH.getIntrinsicWidth()) / 2,
							height - mNextLabelH.getIntrinsicHeight() - padding,
							(width - mNextLabelH.getIntrinsicWidth()) / 2
									+ mNextLabelH.getIntrinsicWidth(), height - padding);
				}
				if (mPreLabelH != null) {
					mPreLabelH.setBounds(
							(width - mPreLabelH.getIntrinsicWidth()) / 2,
							padding,
							(width - mPreLabelH.getIntrinsicWidth()) / 2
									+ mPreLabelH.getIntrinsicWidth(),
							padding + mPreLabelH.getIntrinsicHeight());
				}
			}
		}
		super.onLayout(changed, left, top, right, bottom);
		if (mNoData) {
			mNoDataView.measure(MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY));
			mNoDataView.layout(left, top, right, bottom);
		}
		changeLabelFlag();
	}

	@Override
	protected void handleScrollerSetting() {
		if (mScrollableHandler == null) {
			mScrollableHandler = new HorScrollableGridViewHandler(mContext, this,
					CoupleScreenEffector.PLACE_NONE, false, true) {
//				@Override
//				protected void clipCanvas(GLCanvas canvas) {
//					//do nothing
//				}

				@Override
				public void onEnterLeftScrollZone() {
					mAlphaStep = 0;
					int centerX = getPaddingLeft() / 2;
					int centerY = mHeight / 2;
					int left = centerX - mEdgeHightLight.getIntrinsicWidth() / 2;
					int top = centerY - mEdgeHightLight.getIntrinsicHeight() / 2;
					mEdgeHightLight.setBounds(left, top,
							left + mEdgeHightLight.getIntrinsicWidth(),
							top + mEdgeHightLight.getIntrinsicHeight());
					mIsInScrollZone = true;
					if (mListener != null) {
						mListener.onEnterLeftScrollZone();
					}
				}

				@Override
				public void onEnterRightScrollZone() {
					mAlphaStep = 0;
					int centerX = mWidth - getPaddingRight() / 2;
					int centerY = mHeight / 2;
					int left = centerX - mEdgeHightLight.getIntrinsicWidth() / 2;
					int top = centerY - mEdgeHightLight.getIntrinsicHeight() / 2;
					mEdgeHightLight.setBounds(left, top,
							left + mEdgeHightLight.getIntrinsicWidth(),
							top + mEdgeHightLight.getIntrinsicHeight());
					mIsInScrollZone = true;
					if (mListener != null) {
						mListener.onEnterRightScrollZone();
					}
				}

				@Override
				public void onEnterTopScrollZone() {
					mAlphaStep = 0;
					int centerX = mWidth / 2;
					int centerY = getPaddingTop() / 2;
					int left = centerX - mEdgeHightLight.getIntrinsicWidth() / 2;
					int top = centerY - mEdgeHightLight.getIntrinsicHeight() / 2;
					mEdgeHightLight.setBounds(left, top,
							left + mEdgeHightLight.getIntrinsicWidth(),
							top + mEdgeHightLight.getIntrinsicHeight());
					mIsInScrollZone = true;
					if (mListener != null) {
						mListener.onEnterTopScrollZone();
					}
				}

				@Override
				public void onEnterBottomScrollZone() {
					mAlphaStep = 0;
					int centerX = mWidth / 2;
					int centerY = mHeight - getPaddingBottom() / 2;
					int left = centerX - mEdgeHightLight.getIntrinsicWidth() / 2;
					int top = centerY - mEdgeHightLight.getIntrinsicHeight() / 2;
					mEdgeHightLight.setBounds(left, top,
							left + mEdgeHightLight.getIntrinsicWidth(),
							top + mEdgeHightLight.getIntrinsicHeight());
					mIsInScrollZone = true;
					if (mListener != null) {
						mListener.onEnterBottomScrollZone();
					}
				}

				@Override
				public void onExitScrollZone() {
					mIsInScrollZone = false;
					mEdgeHightLight.setBounds(0, 0, 0, 0);
					if (mListener != null) {
						mListener.onExitScrollZone();
					}
				}

				@Override
				public void onScreenChanged(int newScreen, int oldScreen) {
					super.onScreenChanged(newScreen, oldScreen);
					changeLabelFlag();
				}
			};
		}
		((HorScrollableGridViewHandler) mScrollableHandler).setOrientation(GoLauncherActivityProxy.isPortait() || mFourceHorMode
				? ScreenScroller.HORIZONTAL
				: ScreenScroller.VERTICAL);
	}
	
	@Override
	public synchronized boolean onTouchEvent(MotionEvent ev) {
		if (mNoData) {
			return false;
		}
		int downX = (int) ev.getX();
		int downY = (int) ev.getY();
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (mScrollableHandler instanceof HorScrollableGridViewHandler) {
					HorScrollableGridViewHandler scrollHandler = (HorScrollableGridViewHandler) mScrollableHandler;
					if (GoLauncherActivityProxy.isPortait() || mFourceHorMode) {
						if (mNextLabelV != null && scrollHandler.isScrollFinished() && mShowNextLabel) {
							Rect rect = mNextLabelV.getBounds();
							if (downX >= rect.left) {
								scrollHandler.scrollTo(scrollHandler.getCurrentScreen() + 1, true);
								mIsHitLabelRect = true;
								return true;
							}
						}
						if (mPreLabelV != null && scrollHandler.isScrollFinished() && mShowPreLabel) {
							Rect rect = mPreLabelV.getBounds();
							if (downX <= rect.right) {
								scrollHandler.scrollTo(scrollHandler.getCurrentScreen() - 1, true);
								mIsHitLabelRect = true;
								return true;
							}
						}
					} else {
						if (mNextLabelH != null && scrollHandler.isScrollFinished() && mShowNextLabel) {
							Rect rect = mNextLabelH.getBounds();
							if (downY >= rect.top) {
								scrollHandler.scrollTo(scrollHandler.getCurrentScreen() + 1, true);
								mIsHitLabelRect = true;
								return true;
							}
						}
						if (mPreLabelH != null && scrollHandler.isScrollFinished() && mShowPreLabel) {
							Rect rect = mPreLabelH.getBounds();
							if (downY <= rect.bottom) {
								scrollHandler.scrollTo(scrollHandler.getCurrentScreen() - 1, true);
								mIsHitLabelRect = true;
								return true;
							}
						}
					}
				}
				break;
				
			default:
				break;
		}
		if (mIsHitLabelRect) {
			return true;
		} else {
			return super.onTouchEvent(ev);
		}
	}
	
	@Override
	protected void onScrollFinish() {
		mIsHitLabelRect = false;
	}

	private void changeLabelFlag() {
		int totalScreen = ((HorScrollableGridViewHandler) mScrollableHandler).getTotalScreen();
		int currentScreen = ((HorScrollableGridViewHandler) mScrollableHandler).getCurrentScreen();
		if (totalScreen > 1) {
			if (currentScreen == 0) {
				mShowNextLabel = true;
				mShowPreLabel = false;
			} else if (currentScreen == totalScreen - 1) {
				mShowNextLabel = false;
				mShowPreLabel = true;
			} else {
				mShowNextLabel = true;
				mShowPreLabel = true;
			}
		} else {
			mShowNextLabel = false;
			mShowPreLabel = false;
		}
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		if (!mNoData) {
			super.dispatchDraw(canvas);
		} else {
			mNoDataView.draw(canvas);
		}
		if (GoLauncherActivityProxy.isPortait() || mFourceHorMode) {
			canvas.translate(getScrollX(), 0);
			if (mShowNextLabel) {
				if (mNextLabelV != null) {
					mNextLabelV.draw(canvas);
				}
			}
			if (mShowPreLabel) {
				if (mPreLabelV != null) {
					mPreLabelV.draw(canvas);
				}
			}
		} else {
			canvas.translate(0, getScrollY());
			if (mShowNextLabel) {
				if (mNextLabelH != null) {
					mNextLabelH.draw(canvas);
				}
			}
			if (mShowPreLabel) {
				if (mPreLabelH != null) {
					mPreLabelH.draw(canvas);
				}
			}
		}

		if (mIsInScrollZone) {
			mAlphaStep += 25;
			if (mAlphaStep > 255) {
				mAlphaStep = 255;
			}
			mEdgeHightLight.setAlpha(mAlphaStep);
			mEdgeHightLight.draw(canvas);
		}

	}

	public void setFourceHorMode(boolean isEnable) {
		mFourceHorMode = isEnable;
	}
	
	protected void initNoDataView() {
		mNoDataView = new ShellTextViewWrapper(mContext);
		LayoutParams p = new LayoutParams(
				GLScrollableBaseGrid.LayoutParams.MATCH_PARENT,
				GLScrollableBaseGrid.LayoutParams.MATCH_PARENT);
		mNoDataView.setLayoutParams(p);
		mNoDataView.setText(getNoDateText());
		mNoDataView.setGravity(Gravity.CENTER/* | Gravity.TOP*/);
		mNoDataView.setSingleLine();
		mNoDataView.setTextSize(15);
		addViewInLayout(mNoDataView, 0, mNoDataView.getLayoutParams(), true);
	}
	
	protected abstract String getNoDateText();
	
}
