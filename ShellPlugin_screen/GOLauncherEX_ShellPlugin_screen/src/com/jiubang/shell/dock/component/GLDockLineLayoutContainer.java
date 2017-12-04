package com.jiubang.shell.dock.component;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnTouchListener;
import com.go.gl.view.GLViewGroup;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.util.device.Machine;
import com.go.util.graphics.effector.united.IEffectorIds;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IDockMsgId;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.shell.IShell;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.dock.GLDock;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.folder.GLAppFolder;
import com.jiubang.shell.gesture.GLGestureHandler;
import com.jiubang.shell.popupwindow.PopupWindowControler;
import com.jiubang.shell.scroller.ShellScreenScroller;
import com.jiubang.shell.scroller.ShellScreenScrollerEffector;
import com.jiubang.shell.scroller.ShellScreenScrollerListener;
import com.jiubang.shell.scroller.effector.CoupleScreenEffector;
import com.jiubang.shell.scroller.effector.subscreen.SubScreenContainer;

/**
 * 
 * <br>
 * 类描述:dock行排版窗口 <br>
 * 功能详细描述:
 * 
 * @author ruxueqin
 * @date [2012-10-16]
 */
public class GLDockLineLayoutContainer extends GLViewGroup
		implements
			ShellScreenScrollerListener,
			SubScreenContainer,
			OnTouchListener
			 {

	private ShellScreenScroller mScroller; // 滚动器

	private ShellScreenScrollerEffector mDeskScreenEffector; // 配合滚动器使用的效果器，主要作用是实现循环滚动

	private int mTouchSlop; // 区分滑动和点击的阈值

	private Point mDownPoint = new Point(-1, -1); // 下手时的坐标点

	private int mInterceptTouchMoveX; // 记录x方向的触屏滑动是否超过mTouchSlop

	private int mInterceptTouchMoveY; // 记录y方向的触屏滑动是否超过mTouchSlop

	public boolean mLongClicked = false; // 是否已响应长按

	private boolean mRespondGestured = false; // 是否已响应手势

//	private OnDockGestureListner mGestureListener; // 手势响应者

	//private int mCurLine; // 当前显示第几行

	private IconView mCurrentView; // 当前操作ICON对象

	private int mClickX;

	private int mClickY;
	
	private GLDockLinerLayoutAdapter mAdapter;
	
	private GLGestureHandler mGestureHandler;
	
	private DragController mDragController;
	
	private IShell mShell;
	
	private boolean mIsRowChangedWhileAddIconFrameIsTop = false; // dock添加图标界面可见的情况下切换dock页面

	public GLDockLineLayoutContainer(Context context) {
		super(context);
		
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		mScroller = new ShellScreenScroller(context, this);
		mScroller.setDuration(450);
		mDeskScreenEffector = new CoupleScreenEffector(mScroller, CoupleScreenEffector.PLACE_DESK,
				IEffectorIds.SUBSCREEN_EFFECTOR_TYPE);
		mScroller.setBackgroundAlwaysDrawn(true);
		mScroller.setEffector(mDeskScreenEffector);
		
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (GoLauncherActivityProxy.isPortait()) {
			layoutPort(changed, l, t, r, b);
		} else {
			layoutLand(changed, l, t, r, b);
		}
	}

	private void layoutPort(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();

		int left = 0;
		int top = 0;
		int right = 0;
		int bottom = 0;

		for (int i = 0; i < count; i++) {
			AbsGLLineLayout layout = (AbsGLLineLayout) getChildAt(i);
			left = i * (r - l);
			right = left + (r - l);
			top = 0;
			bottom = top + (b - t);
			layout.layout(left, top, right, bottom);
		}
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		measureChildren(widthMeasureSpec, heightMeasureSpec);
	}
	private void layoutLand(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();

		int left = 0;
		int top = 0;
		int right = 0;
		int bottom = 0;

		for (int i = 0; i < count; i++) {
			AbsGLLineLayout layout = (AbsGLLineLayout) getChildAt(i);
			left = 0;
			right = left + (r - l);
			top = i * (b - t);
			bottom = top + (b - t);
			layout.layout(left, top, right, bottom);
		}
	}

	@SuppressLint("WrongCall")
	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		//偏移0屏
		if (GoLauncherActivityProxy.isPortait()) {
			canvas.translate(0, GLDock.sZeroScreenDockTranslate);
		} else {
			canvas.translate(GLDock.sZeroScreenDockTranslate, 0);
		}
		
		mScroller.invalidateScroll();
		if (!mScroller.isFinished()) {
			mScroller.onDraw(canvas);
		} else {
			// 只画当前屏幕
			long drawingTime = this.getDrawingTime();
			AbsGLLineLayout children = getCurLineLayout();
			if (children != null) {
				this.drawChild(canvas, children, drawingTime);
			}
		}
	}

	public void setOrientation(int orientation) {
		mScroller.setOrientation(orientation);
	}

	public void setScreenCount(int count) {
		mScroller.setScreenCount(count);
	}

	public void setCurrentScreen(int dstscreen) {
		mScroller.setCurrentScreen(dstscreen);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mScroller.setScreenSize(w, h);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	public void computeScroll() {
		mScroller.computeScrollOffset();
	}

	@Override
	public ShellScreenScroller getScreenScroller() {
		// TODO Auto-generated method stub
		return mScroller;
	}

	@Override
	public void setScreenScroller(ShellScreenScroller scroller) {
		// TODO Auto-generated method stub
		mScroller = scroller;
	}

	@Override
	public void onFlingIntercepted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStart() {
		// TODO Auto-generated method stub
		if (mQuickActionMenu != null) {
			mQuickActionMenu.dismiss(true);
		}
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
		setGLDockDragListener(newScreen);
		if (MsgMgrProxy.getTopFrameId() == IDiyFrameIds.DOCK_ADD_ICON_FRAME) {
			//dock页面切换后，如果dock添加图标界面当前可见，则重新进行dock添加图标界面参数初始化
			GLDockLineLayout curLineLayout = (GLDockLineLayout) getChildAt(newScreen);
			final int count = curLineLayout.getChildCount();
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK_ADD_ICON_FRAME,
					IDockMsgId.DOCK_ADD_ICON_INIT, getClickBlankIndex(), count, null);
			mIsRowChangedWhileAddIconFrameIsTop = true;
		} else {
			mIsRowChangedWhileAddIconFrameIsTop = false;
		}
	}
	
	private void setGLDockDragListener(int newScreen) {
		GLDockLineLayout dockLineLayout = (GLDockLineLayout) getChildAt(newScreen);
		GLDock glDock = (GLDock) getGLParent();
		glDock.setGLDockDragListener(dockLineLayout);
		//侧边 Dock 引导,场景如下: 未付费用户第二次滑动到 dock 第二行时出现，Prime推荐去掉优化。(2014-01-14)
		/*if (newScreen == 1) {
			PreferencesManager sp = new PreferencesManager(getContext(),
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			
			int count = sp.getInt(IPreferencesIds.DOCK_SIDE_DOCK_GUIDE_MASK_SLIDING_COUNT, 0);
			count++;
			sp.putInt(IPreferencesIds.DOCK_SIDE_DOCK_GUIDE_MASK_SLIDING_COUNT, count);
			sp.commit();
			if (count == 2) {
			    SideDockGuideUtil.showSideDockGuideMask(ShellAdmin.sShellManager.getActivity());
			}
		}*/
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		// checkShowSlipGuide();
	}

	public int getCurLine() {
		return mScroller.getCurrentScreen();

	}

	public AbsGLLineLayout getCurLineLayout() {
		if (getCurLine() >= 0 && getCurLine() < getChildCount()) {
			return (AbsGLLineLayout) getChildAt(getCurLine());
		}

		return null;
	}
	
	public int getClickBlankIndex() {
		int index = -1;
		AbsGLLineLayout curLineLayout = getCurLineLayout();
		if (mDownPoint != null && curLineLayout != null) {
			index = curLineLayout.getChildCount();
			if (GoLauncherActivityProxy.isPortait()) {
				for (int i = 0; i < curLineLayout.getChildCount(); i++) {
					GLView iconView = curLineLayout.getChildAt(i);
					if (mDownPoint.x < iconView.getLeft()) {
						index = i;
						break;
					}
				}
			} else {
				for (int i = 0; i < curLineLayout.getChildCount(); i++) {
					GLView iconView = curLineLayout.getChildAt(i);
					if (mDownPoint.y > iconView.getBottom()) {
						index = i;
						break;
					}
				}
			}
		}
		return index;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				mCurrentView = null;
				mLongClicked = false;
				mRespondGestured = false;
				mInterceptTouchMoveX = 0;
				mInterceptTouchMoveY = 0;
				mDownPoint.set(x, y);
				if (isAllowToScroll()) {
					mScroller.onTouchEvent(ev, ev.getAction());
				}
				break;

			case MotionEvent.ACTION_MOVE :
				if (mInterceptTouchMoveX < mTouchSlop && mInterceptTouchMoveY < mTouchSlop) {
					mInterceptTouchMoveX = Math.abs(x - mDownPoint.x);
					mInterceptTouchMoveY = Math.abs(y - mDownPoint.y);
				}
				break;

			case MotionEvent.ACTION_CANCEL :
				mDownPoint.set(-1, -1);
				break;
			case MotionEvent.ACTION_UP :
//				IconView.resetIconPressState();
				mDownPoint.set(-1, -1);
				break;

			default :
				break;
		}

		boolean intercepteTouch = mInterceptTouchMoveX >= mTouchSlop
				|| mInterceptTouchMoveY >= mTouchSlop;
		if (intercepteTouch) {
			// 手势响应
			if (GoLauncherActivityProxy.isPortait()) {
				mRespondGestured = mInterceptTouchMoveY > mInterceptTouchMoveX;
			} else {
				mRespondGestured = mInterceptTouchMoveX > mInterceptTouchMoveY;
			}
			if (!mLongClicked && mRespondGestured && null != mCurrentView) {
				DockItemInfo dockItemInfo = (DockItemInfo) mCurrentView.getInfo();
				respondGesture(dockItemInfo.mGestureInfo.mUpIntent);
			}
		}

		return intercepteTouch || !mScroller.isFinished();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mLongClicked || mRespondGestured) {
			return true;
		}
		if (isAllowToScroll()) {
			mScroller.onTouchEvent(event, event.getAction());
		}
//		int action = event.getAction();
//		switch (action) {
//			case MotionEvent.ACTION_CANCEL :
//			case MotionEvent.ACTION_UP :
//				IconView.resetIconPressState();
//				break;
//
//			default :
//				break;
//		}
		return true;
	}
	
	@Override
	public boolean onTouch(GLView v, MotionEvent event) {
		int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				if (v instanceof IconView) {
						mCurrentView = (IconView) v;					
				}
				mClickX = (int) event.getX();
				mClickY = (int) event.getX();
			}
				break;

			case MotionEvent.ACTION_UP :
				break;

			default :
				break;
		}

		return false;
	}

	private boolean isAllowToScroll() {
		return !GLAppFolder.getInstance().isFolderOpened();
	}
	
	/**
	 * @param mGestureListner
	 *            the mGestureListner to set
	 */
	/*public void setGestureListner(OnDockGestureListner mGestureListener) {
		this.mGestureListener = mGestureListener;
	}*/

	public void setLongClicked() {
		mLongClicked = true;
	}

	/**
	 * 设置循环模式
	 * 
	 * @param bool
	 *            是否循环
	 */
	public void setCycle(boolean bool) {
		ShellScreenScroller.setCycleMode(this, bool);
		mScroller.setOvershootPercent(0); // 设置弹跳值
	}

	/**
	 * 跳到某一行
	 * 
	 * @param screen
	 */
	public void snapToScreen(int screen) {
		mScroller.gotoScreen(screen, 450, false);
	}

	public IconView getCurrentIcon() {
		return mCurrentView;
	}

	public void setCurrentIcon(IconView view) {
		mCurrentView = view;
	}

	public boolean isTouching() {
		return mDownPoint.x >= 0 && mDownPoint.y >= 0;
	}

	public Point getDownPoint() {
		return mDownPoint;
	}

	@Override
	public void drawScreen(GLCanvas canvas, int screen) {
		// TODO Auto-generated method stub
		GLView view = null;
		view = getChildAt(screen);
		if (null != view) {
			view.draw(canvas);
		}
	}

	@Override
	public void drawScreen(GLCanvas canvas, int screen, int alpha) {
		// TODO Auto-generated method stub

	}

//	private QuickActionMenu mQuickActionMenu = null;
	private PopupWindowControler mQuickActionMenu = null;

//	public void setQuickActionMenu(QuickActionMenu quickActionMenu) {
//		mQuickActionMenu = quickActionMenu;
//	}
	
	public void setQuickActionMenu(PopupWindowControler quickActionMenu) {
		mQuickActionMenu = quickActionMenu;
	}

	public int getClickX() {
		return mClickX;
	}

	public int getClickY() {
		return mClickY;
	}

	public void clearClickView() {
		if (mCurrentView != null) {
			mCurrentView = null;
		}
	}

	public void clearQuickActionMenu() {
		if (mQuickActionMenu != null) {
			mQuickActionMenu = null;
		}
	}
	
	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		AbsGLLineLayout layout = getCurLineLayout();
		if (layout != null) {
			int count = layout.getChildCount();
			for (int i = 0; i < count; i++) {
				GLView child = layout.getChildAt(i);
				child.cancelLongPress();
			}
		}
	}

	@Override
	public void invalidateScreen() {
	}

	@Override
	public GLView getScreenView(int screen) {
		return null;
	}

	@Override
	public Rect getScreenRect() {
		return null;
	}
	
	public void onConfigurationChanged() {
		int orientation = GoLauncherActivityProxy.isPortait()
				? ShellScreenScroller.HORIZONTAL
				: ShellScreenScroller.VERTICAL;
		setOrientation(orientation);
		requestLayoutDockIcons();
	}
	/**
	 * 重新加载数据
	 * @param infoMap
	 */
	public void bindDockIconData(ConcurrentHashMap<Integer, ArrayList<DockItemInfo>> infoMap) {
		setCycle(getSettingInfo().mAutoRevolve); // 设置循环模式
		setAdapter(new GLDockLinerLayoutAdapter(infoMap));
		onConfigurationChanged();
		setScreenCount(mAdapter.getCount());
		setGLDockDragListener(0);
	}
	
	private void setAdapter(GLDockLinerLayoutAdapter layoutAdapter) {
		mAdapter = layoutAdapter;
		requestLayoutDockIcons();
	}
	/**
	 * 刷新所有icon
	 */
	public void requestLayoutDockIcons() {
		if (mAdapter != null) {
//			cleanUpAllViews();
//			removeAllViews();
			for (int i = 0; i < mAdapter.getCount(); i++) {
				GLDockLineLayout glLineLayout = (GLDockLineLayout) mAdapter.getView(i,
						getChildAt(i), this);
				if (glLineLayout.getGLParent() == null) {
					addView(glLineLayout);
				}
			}
		}
	}
	public void cleanUpAllViews() {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			GLView glView = getChildAt(i);
			glView.cleanup();
		}
	}
	/***
	 * 设置信息
	 * 
	 * @return
	 */
	private ShortCutSettingInfo getSettingInfo() {
		return SettingProxy.getShortCutSettingInfo();
	}

	public void setIshell(IShell shell) {
		mShell = shell;
		mGestureHandler = new GLGestureHandler(mShell.getActivity(), mShell);
	}

	public void respondGesture(Intent intent) {
		IconView curView = mCurrentView;
		if (null == curView) {
			return;
		}
		
//		curView.start3DMultiViewUpAnimation();
		
		ArrayList<Rect> posArrayList = new ArrayList<Rect>();
		Rect rect = new Rect();
		curView.getGlobalVisibleRect(rect);
		posArrayList.add(rect);

		if (Machine.isIceCreamSandwichOrHigherSdk() && intent != null
				&& ICustomAction.ACTION_ENABLE_SCREEN_GUARD.equals(intent.getAction())) {
			curView.invalidate();
		}
		// MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
		// ICommonMsgId.START_ACTIVITY, -1, intent, posArrayList);
		// posArrayList.clear();
		// posArrayList = null;

		if (intent != null) {
			mGestureHandler.handleGesture(intent, posArrayList);
		}
	}
	
	public void setDragController(DragController dragController) {
		mDragController = dragController;
	}
	
	protected DragController getDragController() {
		return mDragController;
	}
	
	public void reloadIconRes() {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			GLDockLineLayout layout = (GLDockLineLayout) getChildAt(i);
			int size = layout.getChildCount();
			for (int j = 0; j < size; j++) {
				GLView child = layout.getChildAt(j);
				if (child instanceof IconView<?>) {
					IconView<?> iconView = (IconView<?>) child;
					iconView.reloadResource();
				}
			}
		}
	}
	
	public boolean isRowChangedWhileAddIconFrameIsTop() {
		return mIsRowChangedWhileAddIconFrameIsTop;
	}

	public void setRowChangedWhileAddIconFrameIsTop(boolean isRowChangedWhileAddIconFrameIsTop) {
		this.mIsRowChangedWhileAddIconFrameIsTop = isRowChangedWhileAddIconFrameIsTop;
	}
}
