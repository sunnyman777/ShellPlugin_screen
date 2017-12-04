package com.jiubang.shell.indicator;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.proxy.ApplicationProxy;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.IndicatorBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.IndicatorItem;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.IndicatorShowMode;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.utils.GLImageUtil;

/**
 * 桌面指示器
 * 
 * @author luopeihuan
 * 
 */
public class DesktopIndicator extends GLViewGroup implements AnimationListener {
	private Indicator mIndicator;
	public static final int UPDATE_SCREEN_NUM = 0;
	public static final int UPDATE_SLIDER_INDICATOR = 1;
	public static final int UPDATE_DOTS_INDICATOR = 2;

	// for Bundle
	public static final String OFFSET = "offset";
	public final static String CURRENT = "current";
	public final static String TOTAL = "total";

	public static final int INDICATOR_TYPE_PAGER = 1;
	public static final int INDICATOR_TYPE_SLIDER_TOP = 2;
	public static final int INDICATOR_TYPE_SLIDER_BOTTOM = 3;

	// 自动隐藏的显示时间
	public static final int VISIABLE_DURATION = 600;

	private int mIndicatorType = 1;
	private int mItems = 0;
	private int mCurrent = 0;
	private Animation mAnimation;
	private Handler mHandler = new Handler();
	private int mDotIndicatorHeight;
	private int mSliderIndicatorHeight;
	private int mDefaultDotsIndicatorNormalResID = R.drawable.gl_normalbar;
	private int mDefaultDotsIndicatorLightResID = R.drawable.gl_lightbar;

	private int mDotsMaxNum = 10;

	private SliderIndicator mSliderIndicator;
	private ScreenIndicator mDotsIndicator;

	private boolean mAutoHide = false;
	public boolean isAutoHide() {
		return mAutoHide;
	}

	private boolean mVisible = true;
	// 指示器展现与否的标识，减少频繁地设置指示器的visible
	private boolean mShow = true;
	private IndicatorShowMode mDisplayMode = IndicatorShowMode.None;

	private int mChangeAlpha = 255;
	
	/**
	 * 
	 * @param context
	 *            上下文
	 */
	public DesktopIndicator(Context context) {
		super(context);
		initIndicator(context);
	}

	/**
	 * 
	 * @param context
	 *            上下文
	 * @param attrs
	 *            xml属性
	 */
	public DesktopIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		initIndicator(context);
	}

	/**
	 * 
	 * @param context
	 *            上下文
	 * @param attrs
	 *            xml属性
	 * @param defStyle
	 *            默认风格
	 */
	public DesktopIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initIndicator(context);
	}

	private void initIndicator(Context context) {
		mDotIndicatorHeight = getResources().getDimensionPixelSize(
				R.dimen.dots_indicator_height);
		mSliderIndicatorHeight = getResources().getDimensionPixelSize(
				R.dimen.slider_indicator_height);

		mDotsIndicator = new ScreenIndicator(context);
		mDotsIndicator.setDotsImage(mDefaultDotsIndicatorLightResID,
				mDefaultDotsIndicatorNormalResID);
		mDotsIndicator.setScreen(mItems, mCurrent);

		mSliderIndicator = new SliderIndicator(context);
		mSliderIndicator.setIndicator(R.drawable.gl_indicator,
				R.drawable.gl_indicator_bg);

		// 加载主题
		applyTheme();

		if (showSliderIndicator()) {
			addView(mSliderIndicator);
		} else {
			addView(mDotsIndicator);
		}
	}
	
	public void setSliderIndicatorHeight(int height) {
		mSliderIndicatorHeight = height;
	}
	
	public void setSliderIndicator(int indicatorRes, int bgRes) {
		mSliderIndicator.setIndicator(indicatorRes, bgRes);
	}

	public void setIndicatorHeight(int height) {
		mDotIndicatorHeight = height;
	}

	/**
	 * 设置默认点状页面指示器图片，如果不设置，使用screen里默认的图片
	 * 
	 * @param selected
	 * @param unSelected
	 */
	public void setDefaultDotsIndicatorImage(int selected, int unSelected) {
		mDefaultDotsIndicatorNormalResID = unSelected;
		mDefaultDotsIndicatorLightResID = selected;
		if (null != mDotsIndicator) {
			mDotsIndicator.setDefaultDotsIndicatorImage(
					mDefaultDotsIndicatorLightResID,
					mDefaultDotsIndicatorNormalResID);
			mDotsIndicator.setDotsImage(mDefaultDotsIndicatorLightResID,
					mDefaultDotsIndicatorNormalResID);
		}
	}

	@Override
	public void addView(GLView child) {
		if (null == child) {
			return;
		} else if (child instanceof Indicator) {
			mIndicator = (Indicator) child;
		}

		removeAllViews();
		super.addView(child);
	}

	/**
	 * 设置当前屏幕
	 * 
	 * @param items
	 *            屏幕总数
	 */
	public void setTotal(int items) {
		if (items != mItems) {
			mItems = items;

			if (mItems > mDotsMaxNum) {
				addView(mSliderIndicator);
			} else {
				addView(mDotsIndicator);
			}
			if (null != mIndicator) {
				mIndicator.setTotal(items);
			}
		}
	}

	/**
	 * 更新指示器
	 * 
	 * @param type
	 * @param bundle
	 */
	public void updateIndicator(int type, Bundle bundle) {
		if (type == DesktopIndicator.UPDATE_SCREEN_NUM) {
			final int num = bundle.getInt(DesktopIndicator.TOTAL);
			setTotal(num);
		} else if (type == DesktopIndicator.UPDATE_SLIDER_INDICATOR) {
			final int offset = bundle.getInt(DesktopIndicator.OFFSET);
			setOffset(offset);
		} else if (type == DesktopIndicator.UPDATE_DOTS_INDICATOR) {
			final int current = bundle.getInt(DesktopIndicator.CURRENT);
			setCurrent(current);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int realHeight = 0;
		if (showSliderIndicator()) {
			realHeight = mSliderIndicatorHeight;
		} else {
			realHeight = mDotIndicatorHeight;
		}
		mIndicator.measure(getWidth(), realHeight);

		int realHeightMeasurespec = MeasureSpec.makeMeasureSpec(realHeight,
				MeasureSpec.EXACTLY);
		super.onMeasure(widthMeasureSpec, realHeightMeasurespec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		LinearLayout.LayoutParams params;
		if (showSliderIndicator()) {
			params = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER_VERTICAL;
			mIndicator.measure(getWidth(), mSliderIndicatorHeight);
			mIndicator.setLayoutParams(params);
			mIndicator.layout(0, 0, getWidth(), mSliderIndicatorHeight);
		} else {
			params = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER;
			mIndicator.measure(getWidth(), mDotIndicatorHeight);
			mIndicator.setLayoutParams(params);
			mIndicator.layout(0, 0, getWidth(), mDotIndicatorHeight);
		}
	}

	public void setDotIndicatorLayoutMode(int mode) {
		if (null != mDotsIndicator) {
			mDotsIndicator.setmLayoutMode(mode);
		}
	}

	public void setDotIndicatorDrawMode(int mode) {
		if (null != mDotsIndicator) {
			mDotsIndicator.setDrawMode(mode);
		}
	}

	/**
	 * 设置百分比（条形指示器）
	 */
	public void setOffset(int offset) {
		if (null != mIndicator && offset != mIndicator.mOffset) {
			// setVisibility(View.VISIBLE);
			mIndicator.setOffset(offset);
			mIndicator.postInvalidate();

			mHandler.removeCallbacks(mAutoHideRunnable);
			if (mAutoHide && mShow) {
				setVisibility(GLView.VISIBLE);
				mHandler.postDelayed(mAutoHideRunnable, VISIABLE_DURATION);
			}
		}
	}

	/**
	 * 
	 * @param position
	 *            position
	 */
	public void setCurrent(int position) {
		// setVisibility(View.VISIBLE);
		if (null != mIndicator && mItems <= mDotsMaxNum && position >= 0) {
			mIndicator.setCurrent(position);
		}

		mHandler.removeCallbacks(mAutoHideRunnable);
		if (mAutoHide && mShow) {
			setVisibility(GLView.VISIBLE);
			mHandler.postDelayed(mAutoHideRunnable, VISIABLE_DURATION);
		}
		if (position >= 0) {
			mCurrent = position;
			postInvalidate();
		}
	}

	public int getCurrent() {
		return mCurrent;
	}

	/**
	 * 设置指示器类型：点状或条状
	 * 
	 * @param type
	 *            类型
	 */
	public void setType(int type) {
		if (type != mIndicatorType) {
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
					getLayoutParams());
			if (type == INDICATOR_TYPE_SLIDER_BOTTOM) {
				lp.gravity = Gravity.BOTTOM;
			} else {
				lp.gravity = Gravity.TOP;
			}
			setLayoutParams(lp);
			mIndicatorType = type;
			removeAllViews();
			initIndicator(getContext());
		}
	}

	/**
	 * 设置自动隐藏
	 * 
	 * @param autohide
	 *            true自动隐藏
	 */
	public void setAutoHide(boolean autohide) {
		mAutoHide = autohide;
		if (mAutoHide) {
			setVisibility(INVISIBLE);
		} else {
			setVisibility(VISIBLE);
		}
	}

	private Runnable mAutoHideRunnable = new Runnable() {
		@Override
		public void run() {
			DesktopIndicator.this.setHasPixelOverlayed(false);
			if (mAnimation == null) {
//              使用com.go.gl.animation.Animation的无法调用AnimationUtils.loadAnimation？？
//				mAnimation = AnimationUtils.loadAnimation(getContext(),
//						R.anim.fade_out_fast);\
				mAnimation = new AlphaAnimation(1, 0);
				mAnimation.setDuration(300);
				mAnimation.setInterpolator(new AccelerateInterpolator());
//				mAnimation.setAnimationListener(DesktopIndicator.this);
				
			} else {
				try {
					// This little thing seems to be making some androids piss
					// off
					if (!mAnimation.hasEnded()) {
						mAnimation.reset();
					}
				} catch (NoSuchMethodError e) {
				}
			}
//			startAnimation(mAnimation);

			AnimationTask task = new AnimationTask(DesktopIndicator.this, mAnimation,
					DesktopIndicator.this, true, AnimationTask.PARALLEL);
			GLAnimationManager.startAnimation(task);
		}
	};

	@Override
	public void onAnimationEnd(Animation animation) {
		setVisibility(GLView.INVISIBLE);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}
	
	@Override
	public void onAnimationProcessing(Animation animation, float interpolatedTime) {
		
	}

	/**
	 * 隐藏指示器
	 */
	public void hide() {
		mShow = false;
		setVisibility(GLView.INVISIBLE);
	}

	/**
	 * 显示指示器
	 */
	public void show() {
		mShow = true;
		if (mVisible) {
			if (!mAutoHide) {
				setVisibility(GLView.VISIBLE);
			}
			mHandler.removeCallbacks(mAutoHideRunnable);
//			 if (mAutoHide)
//			 {
//			 mHandler.postDelayed(mAutoHideRunnable, VISIABLE_DURATION);
//			 }
		}
	}

	/**
	 * 设置可见性
	 * 
	 * @param visible
	 *            true 时为可见
	 */
	public void setVisible(boolean visible) {
		if (mVisible != visible) {
			mVisible = visible;
			invalidate();
		}
	}
	@Override
	public void setVisibility(int visibility) {
		// TODO Auto-generated method stub
		super.setVisibility(visibility);
	}
	public boolean getVisible() {
		return mVisible;
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		int visibility = getVisibility();
		if (mVisible && visibility == VISIBLE && null != mIndicator) {
			final int oldAlpha = canvas.getAlpha();
			canvas.multiplyAlpha(mChangeAlpha);
			mIndicator.draw(canvas);
			canvas.setAlpha(oldAlpha);
		}
	}

	public void setDisplayMode(IndicatorShowMode mode) {
		if (mDisplayMode != mode) {
			mDisplayMode = mode;
			requestLayout();
		}
	}

	private boolean showSliderIndicator() {
		if (mItems > mDotsMaxNum) {
			return true;
		} else {
			return false;
		}
	}

	public void applyTheme() {
		AppCore appCore = AppCore.getInstance();
		if (null == appCore) {
			// "我的主题"，不同进程，访问不了appcore
			return;
		}
		mDotsIndicator.applyTheme();
		float themeVersion = 0;
		IndicatorBean indicatorBean = null;
		DeskThemeControler themeControler = appCore.getDeskThemeControler();
		String indicatorMode = ThemeManager
				.getInstance(ApplicationProxy.getContext())
				.getScreenStyleSettingInfo().getIndicatorStyle();
		if (themeControler != null
				&& !indicatorMode.equals(ScreenIndicator.SHOWMODE_NORMAL)
				&& !indicatorMode.equals(ScreenIndicator.SHOWMODE_NUMERIC)) {
			DeskThemeBean themeBean = themeControler.getDeskThemeBean();
			if (themeBean != null) {
				themeVersion = themeBean.mDeskVersion;
				// themeVersion = themeBean.getVerId();
				indicatorBean = themeBean.mIndicator;
			}
		}

		// 由于以前的条状指示器主题配置错误
		// 1以下版本不支持
		float version = 1.0f;
		if (indicatorBean != null && themeVersion > version) {
			// mDotsMaxNum = indicatorBean.mWhenScreenCount;
			// setDisplayMode(indicatorBean.mIndicatorShowMode);
			if (indicatorBean.mSlide != null) {
				setSlideIndicator(indicatorBean.mSlide, themeControler);
			}
		} else // default theme
		{
			mDotsMaxNum = 10;
			setDisplayMode(IndicatorShowMode.None);
			setSlideIndicator(null, null);
		}
	}

	private void setSlideIndicator(IndicatorItem item,
			DeskThemeControler controler) {
		// IndicatorItem.mSelectedBitmap-->Indicator
		// IndicatorItem.mUnSelectedBitmap-->IndicatorBG

		GLDrawable glIndicator = null;
		GLDrawable glIndicatorBg = null;

		if (item != null && controler != null) {
			Drawable indicator = null;
			if (null != item.mSelectedBitmap) {
				indicator = controler
						.getThemeResDrawable(item.mSelectedBitmap.mResName);

				if (null == indicator) {
					indicator = getResources()
							.getDrawable(R.drawable.gl_indicator);
				}
			}

			if (indicator != null) {
//				if (indicator instanceof BitmapDrawable) {
//					glIndicator = new BitmapGLDrawable(
//							(BitmapDrawable) indicator);
//				} else if (indicator instanceof NinePatchDrawable) {
//					glIndicator = new NinePatchGLDrawable(
//							(NinePatchDrawable) indicator);
//				}
				glIndicator = GLImageUtil.getGLDrawable(indicator);
			}
			if (null != indicator) {
				Drawable indicatorBG = null;
				if (null != item.mUnSelectedBitmap) {
					indicatorBG = controler
							.getThemeResDrawable(item.mUnSelectedBitmap.mResName);

					if (null == indicatorBG) {
						indicatorBG = getResources().getDrawable(
								R.drawable.gl_indicator_bg);
					}
				}

				if (indicatorBG != null) {
//					if (indicatorBG instanceof BitmapDrawable) {
//						glIndicatorBg = new BitmapGLDrawable(
//								(BitmapDrawable) indicatorBG);
//					} else if (indicatorBG instanceof NinePatchDrawable) {
//						glIndicatorBg = new NinePatchGLDrawable(
//								(NinePatchDrawable) indicatorBG);
//					}
					glIndicatorBg = GLImageUtil.getGLDrawable(indicatorBG);
				}

				mSliderIndicator.setIndicator(glIndicator, glIndicatorBg);
				return;
			}
		}
		// else
		{
			mSliderIndicator.setIndicator(R.drawable.gl_indicator,
					R.drawable.gl_indicator_bg);
		}
	}

	public void setIndicatorListener(IndicatorListener listner) {
		if (null != mDotsIndicator) {
			mDotsIndicator.setListener(listner);
		}
		if (null != mSliderIndicator) {
			mSliderIndicator.setListener(listner);
		}
	}

	public void doWithShowModeChanged() {
		if (null != mDotsIndicator) {
			mDotsIndicator.doWithShowModeChanged();
		}
	}

	/***
	 * 设置指示器是否响应触摸事件
	 * 
	 * @param touch
	 */
	public void setTouchable(boolean touch) {
		if (mIndicator != null) {
			mIndicator.mIsCanTouch = touch;
		}
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if (!mIndicator.mIsCanTouch) {
			return true;
		}
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public void setAlpha(int alpha) {
		
		int state = View.VISIBLE;
		if (alpha == 0) {
			state = View.INVISIBLE;
			setTouchable(false);
		}
		
		//如果显示一定范围才能点击
		else if (alpha > 200) {
			setTouchable(true);
		}
		
		if (getVisibility() != state) {
			setVisibility(state);
		}
		
		//设置指示器透明度
		mIndicator.postInvalidate();
		mChangeAlpha = alpha;
	}
	
	public void setIsFromAddFrame(boolean b) {
		if (mDotsIndicator != null) {
			mDotsIndicator.setIsFromAddFrame(b);
		}
	}
	
	/**
	 * 设置某页显示自定义点
	 * @param index
	 * @param selected
	 * @param unSelected
	 */
	public void addCustomDotImage(int index, Drawable selected, Drawable unSelected) {
		if (mDotsIndicator != null) {
			mDotsIndicator.addCustomDotImage(index, selected, unSelected);
		}
	}
	
	public void clearCustomDotImage() {
		if (mDotsIndicator != null) {
			mDotsIndicator.clearCustomDotImage();
		}
	}
	
	public void updateContent() {
		if (mDotsIndicator != null) {
			mDotsIndicator.updateContent();
		}
	}
	
	public void setDotIndicatorItemWidth(int width) {
		if (mDotsIndicator != null) {
			mDotsIndicator.setDotWidth(width);
		}
	}
}
