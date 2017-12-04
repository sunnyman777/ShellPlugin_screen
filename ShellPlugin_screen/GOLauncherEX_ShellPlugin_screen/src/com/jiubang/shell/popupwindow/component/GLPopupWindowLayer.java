package com.jiubang.shell.popupwindow.component;

import android.content.Context;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.IShell;
import com.jiubang.shell.IView;
import com.jiubang.shell.common.component.ShellContainer;
import com.jiubang.shell.orientation.GLOrientationControler;
import com.jiubang.shell.popupwindow.IPopupWindow;
import com.jiubang.shell.popupwindow.PopupWindowControler;

/**
 * 弹出菜单层
 * @author yangguanxiang
 *
 */
public class GLPopupWindowLayer extends GLFrameLayout implements GLView.OnTouchListener, IView {

	private PopupWindowControler mPopupMenuControler;
	private GLView mCurrentPopupWindow;
	boolean mIsExiting = false;

	private final Rect mRect = new Rect();
	private GLPopupWindowMiddleView mMiddleView;

	public GLPopupWindowLayer(Context context) {
		super(context);
		initViews(context);
	}

	private void initViews(Context context) {
		mMiddleView = new GLPopupWindowMiddleView(context);
		mMiddleView.setVisibility(GONE);
		addView(mMiddleView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		setOnTouchListener(this);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		for (int i = 0; i < getChildCount(); i++) {
			GLView child = getChildAt(i);
			final ViewGroup.LayoutParams flp = (ViewGroup.LayoutParams) child.getLayoutParams();

			if (flp instanceof PopupWindowLayoutParams) {
				final PopupWindowLayoutParams lp = (PopupWindowLayoutParams) flp;
				child.layout(lp.x, lp.y, lp.x + lp.width, lp.y + lp.height);
			} else {
				child.layout(0, 0, right - left, bottom - top);
			}
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(width, height + DrawUtils.getNavBarHeight());
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean flag = false;
		if (mCurrentPopupWindow != null) {
			flag = mCurrentPopupWindow.onKeyUp(keyCode, event);
		}
		if (!flag) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				exit(true);
				flag = true;
			}
		}
		return flag;
	}

	public void setPopupMenuController(PopupWindowControler controler) {
		mPopupMenuControler = controler;
	}

	public void exit(boolean animate) {
		if (mIsExiting) {
			return;
		}
		preExit();
		if (animate) {
			if (mCurrentPopupWindow != null) {
				((IPopupWindow) mCurrentPopupWindow).onExit(this, animate);
				((IPopupWindow) mCurrentPopupWindow).onWithExit(animate);
			}
		} else {
			if (mCurrentPopupWindow != null) {
				((IPopupWindow) mCurrentPopupWindow).onWithExit(animate);
			}
			onExit();
		}
	}

	public void enter(boolean animate) {
		preEnter();
		if (animate) {
			if (mCurrentPopupWindow != null) {
				((IPopupWindow) mCurrentPopupWindow).onEnter(this, animate);
				((IPopupWindow) mCurrentPopupWindow).onWithEnter(animate);
			}
		} else {
			if (mCurrentPopupWindow != null) {
				((IPopupWindow) mCurrentPopupWindow).onWithEnter(animate);
			}
			onEnter();
		}
	}

	public void setPopupWindow(IPopupWindow window) {
		if (mCurrentPopupWindow != null) {
			removeView(mCurrentPopupWindow);
		}
		if (window != null) {
			if (window instanceof GLView) {
				mCurrentPopupWindow = (GLView) window;
				addView((GLView) window);
			} else {
				throw new IllegalArgumentException("The instance of IPopupWindow must be a GLView");
			}
		}
	}

	private void preEnter() {
		GLOrientationControler.keepOrientationAllTheTime(true);
		ShellContainer.setDispatchTouchEvent(false);
	}

	public void onEnter() {
		ShellContainer.setDispatchTouchEvent(true);
	}

	private void preExit() {
		mIsExiting = true;
		ShellContainer.setDispatchTouchEvent(false);
	}

	public void onExit() {
		mIsExiting = false;
		ShellContainer.setDispatchTouchEvent(true);
		setVisibility(GONE);
		mPopupMenuControler.releaseReference();
		mMiddleView.reset();
		mMiddleView.setVisibility(GONE);
		GLOrientationControler.keepOrientationAllTheTime(false);
		post(new Runnable() {

			@Override
			public void run() {
				if (mCurrentPopupWindow != null) {
					removeView(mCurrentPopupWindow);
					mCurrentPopupWindow.cleanup();
					mCurrentPopupWindow = null;
				}
			}
		});
	}
	
	public GLPopupWindowMiddleView getMiddleView() {
		return mMiddleView;
	}

	@Override
	public boolean onTouch(GLView v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :
				return true;
			case MotionEvent.ACTION_UP :
				int x = (int) event.getX();
				int y = (int) event.getY();
				if (null != mCurrentPopupWindow) {
					mCurrentPopupWindow.getHitRect(mRect);
					if (mPopupMenuControler.isShowing() && !mRect.contains(x, y)) {
						exit(true);
						return true;
					}
				}
				break;
		}
		return false;
	}

	/**
	 * @author zouguiquan
	 */
	public static class PopupWindowLayoutParams extends FrameLayout.LayoutParams {
		public int x;
		public int y;
		public PopupWindowLayoutParams(int width, int height) {
			super(width, height);
		}
	}

	@Override
	public void setVisible(boolean visible, boolean animate, Object obj) {
		setVisible(visible);
	}

	@Override
	public void setShell(IShell shell) {

	}

	@Override
	public int getViewId() {
		return IViewId.POPUP_WINDOW_LAYER;
	}

	@Override
	public void onAdd(GLViewGroup parent) {

	}

	@Override
	public void onRemove() {

	}
}
