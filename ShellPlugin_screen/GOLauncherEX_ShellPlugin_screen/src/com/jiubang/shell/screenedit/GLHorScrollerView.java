package com.jiubang.shell.screenedit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLImageView;
import com.go.proxy.SettingProxy;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.shell.indicator.DesktopIndicator;
import com.jiubang.shell.screenedit.tabs.GLBaseTab;
import com.jiubang.shell.screenedit.tabs.ScreenEditImageLoader;
import com.jiubang.shell.scroller.ShellScreenScroller;
import com.jiubang.shell.scroller.ShellScreenScrollerListener;
import com.jiubang.shell.utils.ViewUtils;

/**
 * 
 * @author zouguiquan
 *
 */
public class GLHorScrollerView extends GLFrameLayout implements ShellScreenScrollerListener {

	private ShellScreenScroller mScroller;
	private DesktopIndicator mIndicator;

	public final static int SCROLLER_DURATION = 400;
	private static final int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;					// 触屏状态
	private int mTouchState = TOUCH_STATE_REST;							// 当前触屏状态
	private float mLastMotionX;											// 上次触屏离开的x坐标
	private float mlastMotionY;

	private GLBaseTab mBaseTab;
	private GLView mHeadView;
	private GLView mBackView;

	private int mPageCount = 0;

	public GLHorScrollerView(Context context) {
		this(context, null);
	}

	public GLHorScrollerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mScroller = new ShellScreenScroller(getContext(), this);
		mScroller.setBackgroundAlwaysDrawn(true);
		mScroller.setDuration(SCROLLER_DURATION);
		mScroller.setMaxOvershootPercent(0);

		// 判断是否为循环显示
		ScreenSettingInfo mScreenSettingInfo = SettingProxy.getScreenSettingInfo();
		if (mScreenSettingInfo != null) {
			boolean islooper = mScreenSettingInfo.mScreenLooping;
			ShellScreenScroller.setCycleMode(this, islooper);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		int width = r - l;
		int height = b - t;
		int left = 0;
		int top = 0;
		int childcount = getChildCount();

		for (int i = 0; i < childcount; i++) {
			GLViewGroup childView = (GLViewGroup) getChildAt(i);
			if (childView != null) {
				childView.layout(left, top, left + width, height);
				left += width;
			}
		}
	}

	public void setBaseTab(GLBaseTab baseTab) {
		mBaseTab = baseTab;
	}

	public void initChildView() {

		ViewUtils.cleanupAllChildren(this);

		if (mBaseTab == null) {
			return;
		}

		int totleItemCount = 0;
		int itemCount = mBaseTab.getItemCount();
		totleItemCount = itemCount;

		if (totleItemCount == 0) {
			return;
		}

		if (mHeadView != null) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			addView(mHeadView, params);
			totleItemCount++;
		}

		for (int i = 0; i < itemCount; i++) {
			GLView childView = mBaseTab.getView(i);
			if (childView != null) {
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				addView(childView, params);
			}
		}

		if (mBackView != null) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			addView(mBackView, params);
			totleItemCount++;
		}

		int currentPage = Math.min(totleItemCount, Math.max(0, mBaseTab.getCurrentScreen()));

		mScroller.setScreenCount(totleItemCount);
		mScroller.setCurrentScreen(currentPage);
		mPageCount = totleItemCount;

		if (mIndicator != null) {
			mIndicator.setTotal(totleItemCount);
			mIndicator.setCurrent(currentPage);
		}
	}
	
	public void appendHeadView(GLView view) {
		mHeadView = view;
	}

	public void appendBackView(GLView view) {
		mBackView = view;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mBaseTab == null) {
			return true;
		}

		if (null != mScroller) {
			mScroller.onTouchEvent(event, event.getAction());
		}
		return true;
	}

	@Override
	public ShellScreenScroller getScreenScroller() {
		return mScroller;
	}

	@Override
	public void setScreenScroller(ShellScreenScroller scroller) {
		mScroller = scroller;
	}

	@Override
	public void onFlingIntercepted() {
	}

	@Override
	public void onScrollStart() {
		postInvalidate();
	}

	@Override
	public void onFlingStart() {
	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		if (mIndicator != null) {
			mIndicator.setCurrent(newScreen);
		}
	}

	@Override
	public void onScrollFinish(int currentScreen) {

		if (mBaseTab.needAsyncLoadImage()) {	
			
			ScreenEditImageLoader imageLoader = mBaseTab.getImageLoader();
			if (imageLoader == null) {
				return;
			}
			
			int index = currentScreen;

			GLView child = getChildAt(index);
			if (child == null) {
				return;
			}
			
			GLImageView imageView = (GLImageView) child.findViewById(R.id.widgetPreview);
			if (imageView != null) {
				if (mHeadView != null) {
					index--;
				}
				
				imageLoader.setCurrentLoadPage(index);
				imageLoader.loadImage(imageView, index);
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mScroller.setScreenSize(w, h);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mBaseTab == null) {
			return true;
		}

		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		final float x = ev.getX();
		final float y = ev.getY();
		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				mLastMotionX = x;
				mlastMotionY = y;
				mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
				break;
			}

			case MotionEvent.ACTION_MOVE : {
				final int xoffset = (int) (x - mLastMotionX);
				final int yoffset = (int) (y - mlastMotionY);
				if (Math.abs(yoffset) < Math.abs(xoffset)
						&& Math.abs(xoffset) > DrawUtils.sTouchSlop) {
					mTouchState = TOUCH_STATE_SCROLLING;
					mScroller.onTouchEvent(ev, MotionEvent.ACTION_DOWN);
				}

				break;
			}

			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP : {
				mTouchState = TOUCH_STATE_REST;
				break;
			}

			default :
				break;
		}

		return mTouchState != TOUCH_STATE_REST;

	}

	@Override
	public void computeScroll() {
		mScroller.computeScrollOffset();
	}

	public void snapToScreen(int screen, boolean noElastic, int duration) {
		mScroller.gotoScreen(screen, duration, noElastic);
	}

	public int getPageCount() {
		return mPageCount;
	}

	public void setToScreen(int whichScreen) {
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		scrollTo(whichScreen * getWidth(), 0);
	}

	@SuppressLint("WrongCall")
	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		mScroller.invalidateScroll();

		canvas.save();
		final int scrollX = getScrollX();

		final int height = mBaseTab.getTabHeight();
		canvas.clipRect(scrollX, 0, getWidth() + scrollX, height);
		super.dispatchDraw(canvas);
		canvas.restore();
	}

	public ShellScreenScroller getScroller() {
		return mScroller;
	}

	public void setIndicator(GLView indicator) {
		if (indicator instanceof DesktopIndicator) {
			mIndicator = (DesktopIndicator) indicator;
		} else {
			throw new IllegalArgumentException("Please set DesktopIndicator");
		}
	}

	public void startChangeAnim() {
		setVisibility(VISIBLE);
		
		final GLView glView = getChildAt(mScroller.getCurrentScreen());
		if (glView == null) {
			return;
		}
		
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation tranAnim = new TranslateAnimation(0, 0, 0, 0,
				Animation.RELATIVE_TO_SELF, -0.15f, Animation.RELATIVE_TO_SELF, 0.0f);
		tranAnim.setDuration(400);
		
		AlphaAnimation alphaAnim = new AlphaAnimation(0.1f, 1.0f);
		alphaAnim.setDuration(400);
		
		animationSet.addAnimation(tranAnim);
		animationSet.addAnimation(alphaAnim);
		animationSet.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationProcessing(Animation animation, float interpolatedTime) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				glView.setHasPixelOverlayed(true);
				if (mBaseTab != null) {
					mBaseTab.refreshTitle();
				}
			}
		});
		
		glView.setHasPixelOverlayed(false);
		glView.startAnimation(animationSet);
	}
}