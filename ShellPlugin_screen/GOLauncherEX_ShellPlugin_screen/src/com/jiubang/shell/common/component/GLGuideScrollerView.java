package com.jiubang.shell.common.component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLView;
import com.go.util.graphics.DrawUtils;
import com.jiubang.shell.scroller.ShellMScroller;
import com.jiubang.shell.scroller.ShellScreenScroller;
import com.jiubang.shell.scroller.ShellScreenScrollerListener;
import com.jiubang.shell.scroller.effector.subscreen.SubScreenContainer;
import com.jiubang.shell.scroller.effector.subscreen.SubScreenEffector;

/**
 * 
 */
public class GLGuideScrollerView extends GLLinearLayout
		implements
			ShellScreenScrollerListener,
			SubScreenContainer {

	private ShellScreenScroller mScroller;
	private SubScreenEffector mDeskScreenEffector;

	private final static int SCROLLER_DURATION = 400;
	private static final int TOUCH_STATE_REST = 0;
	// 触屏状态
	private final static int TOUCH_STATE_SCROLLING = 1;
	// 当前触屏状态
	private int mTouchState = TOUCH_STATE_REST;
	// 上次触屏离开的x坐标
	private float mLastMotionX;
	private float mlastMotionY;

	public GLGuideScrollerView(Context context) {
		this(context, null);
	}

	public GLGuideScrollerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mScroller = new ShellScreenScroller(getContext(), this);
		mScroller.setOrientation(ShellMScroller.VERTICAL);
		mScroller.setBackgroundAlwaysDrawn(true);
		mScroller.setDuration(SCROLLER_DURATION);
		mScroller.setMaxOvershootPercent(0);

		mDeskScreenEffector = new SubScreenEffector(mScroller);
		mDeskScreenEffector.setType(0);
	}
	
	public void setScreenCount(int height, int value) {
		int count = value % height == 0 ? value / height : value / height + 1;
		
		mScroller.setScreenCount(count);
		mScroller.setCurrentScreen(0);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

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
	public boolean onTouchEvent(MotionEvent event) {
		if (null != mScroller) {
			mScroller.onTouchEvent(event, event.getAction());
		}
		return true;
	}

	@SuppressLint("WrongCall")
	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		mScroller.invalidateScroll();
		canvas.save();
		final int scrollY = getScrollY();
		canvas.clipRect(0, scrollY, getWidth(), scrollY + getHeight());
		super.dispatchDraw(canvas);
		canvas.restore();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.d("zgq", "onSizeChanged w= " + w + " h= " + h);
		mScroller.setScreenSize(w, h);
	}
	
	@Override
	public void computeScroll() {
		mScroller.computeScrollOffset();
	}

	@Override
	public ShellScreenScroller getScreenScroller() {
		return mScroller;
	}

	@Override
	public void setScreenScroller(ShellScreenScroller scroller) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFlingIntercepted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFlingStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawScreen(GLCanvas canvas, int screen) {
		// TODO Auto-generated method stub
		GLView gridview = getChildAt(screen);
		if (gridview != null) {
			gridview.draw(canvas);
		}
	}

	@Override
	public void drawScreen(GLCanvas canvas, int screen, int alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public void invalidateScreen() {
		// TODO Auto-generated method stub

	}

	@Override
	public GLView getScreenView(int screen) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rect getScreenRect() {
		// TODO Auto-generated method stub
		return null;
	}
}
