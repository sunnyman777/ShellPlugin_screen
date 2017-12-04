package com.jiubang.shell.folder.smartcard;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.MotionEvent;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.util.graphics.DrawUtils;
import com.jiubang.shell.indicator.DesktopIndicator;
import com.jiubang.shell.scroller.ShellScreenScroller;
import com.jiubang.shell.scroller.ShellScreenScrollerListener;

/**
 * 
 * @author guoyiqing
 * 
 */
public class GLScrollCardGroup extends GLFrameLayout implements
		ShellScreenScrollerListener {

	private static final int SCROLLER_DURATION = 400;
	private ShellScreenScroller mScroller;
	private static final int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	private int mTouchState = TOUCH_STATE_REST;
	private float mLastMotionX;
	private float mlastMotionY;
	private DesktopIndicator mIndicator;
	private SparseArray<Integer> mCurrentCardTypeMap;
	private int mContainerId;

	public GLScrollCardGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GLScrollCardGroup(Context context) {
		super(context);
		init();
	}

	private void init() {
		mScroller = new ShellScreenScroller(mContext, this);
		mScroller.setBackgroundAlwaysDrawn(true);
		mScroller.setDuration(SCROLLER_DURATION);
		mScroller.setMaxOvershootPercent(0);
		mScroller.setOrientation(ShellScreenScroller.HORIZONTAL);
	}

	private int getScreenWidth() {
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		return metrics.widthPixels;
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int left = 0;
		int top = 0;
		int width = getScreenWidth();
		int height = b - t;
		mScroller.setScreenSize(width, height);
		int itemWidth = width;
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			GLView child = getChildAt(i);
			if (child != null && child.isVisible()) {
				child.layout(left, top, left + itemWidth, top + height);
				left += itemWidth;
			}
		}
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		mScroller.invalidateScroll();
		super.dispatchDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (null != mScroller) {
			mScroller.onTouchEvent(event, event.getAction());
		}
		return true;
	}

	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		if ((action == MotionEvent.ACTION_MOVE)
				&& (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		final float x = ev.getX();
		final float y = ev.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			mLastMotionX = x;
			mlastMotionY = y;
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
					: TOUCH_STATE_SCROLLING;
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			final int xoffset = (int) (x - mLastMotionX);
			final int yoffset = (int) (y - mlastMotionY);
			if (Math.abs(yoffset) < Math.abs(xoffset)
					&& Math.abs(xoffset) > DrawUtils.sTouchSlop) {
				mTouchState = TOUCH_STATE_SCROLLING;
				mScroller.onTouchEvent(ev, MotionEvent.ACTION_DOWN);
			}
			break;
		}
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP: {
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		default:
			break;
		}
		return mTouchState != TOUCH_STATE_REST;
	}

	@Override
	public ShellScreenScroller getScreenScroller() {
		return mScroller;
	}

	@Override
	public void setScreenScroller(ShellScreenScroller scroller) {
		if (scroller != null) {
			mScroller = scroller;
		}
	}

	@Override
	public void onFlingIntercepted() {
	}

	@Override
	public void onScrollStart() {
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
		if (mCurrentCardTypeMap != null) {
			GLAbsCardView view = (GLAbsCardView) getChildAt(newScreen);
			mCurrentCardTypeMap.put(mContainerId, view.getCardType());
		}
	}

	@Override
	public void onScrollFinish(int currentScreen) {
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (mScroller != null) {
			mScroller.setScreenSize(w, h);
		}
	}

	@Override
	public void computeScroll() {
		if (mScroller != null) {
			mScroller.computeScrollOffset();
		}
	}

	public void setIndicator(DesktopIndicator indicator) {
		mIndicator = indicator;
	}

	public void setCardsViews(List<GLAbsCardView> views) {
		if (views == null) {
			return;
		}
		removeAllViews();
		int length = views.size();
		mScroller.setScreenCount(length);
		for (int i = 0; i < length; i++) {
			addView(views.get(i));
		}
	}

	@Override
	public void removeView(GLView view) {
		super.removeView(view);
		mScroller.setScreenCount(getChildCount());
	}
	
	public void setCurrent(int current) {
		if (mScroller != null) {
			mScroller.setCurrentScreen(current);
		}
		if (mCurrentCardTypeMap != null) {
			GLAbsCardView view = (GLAbsCardView) getChildAt(current);
			mCurrentCardTypeMap.put(mContainerId, view.getCardType());
		}
	}
	
	public void setContainerId(int id) {
		mContainerId = id;
	}
	
	public void setCurrentCardTypeMap(SparseArray<Integer> map) {
		mCurrentCardTypeMap = map;
	}

}
