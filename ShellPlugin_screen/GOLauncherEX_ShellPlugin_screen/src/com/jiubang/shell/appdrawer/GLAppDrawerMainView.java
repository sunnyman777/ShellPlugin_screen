package com.jiubang.shell.appdrawer;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.animation.InterpolatorValueAnimation;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.scroller.FastVelocityTracker;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLView;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.util.graphics.DrawUtils;
import com.golauncher.message.IAppDrawerMsgId;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;
import com.jiubang.ggheart.apps.desks.appfunc.model.IBackgroundInfoChangedObserver;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.SpecialAppManager;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.pref.PrefConst;
import com.jiubang.ggheart.apps.desks.diy.pref.PrivatePreference;
import com.jiubang.ggheart.data.info.AppSettingDefault;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.SpecialAppItemInfo;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.ThreadName;
import com.jiubang.ggheart.plugin.SecurityPoxyFactory;
import com.jiubang.ggheart.plugin.UnsupportSecurityPoxyException;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderController;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderInfo;
import com.jiubang.ggheart.smartcard.RecommInfoServer;
import com.jiubang.shell.IShell;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.appdrawer.GLAppDrawer.OnAppDrawerVisibilityChangedListener;
import com.jiubang.shell.appdrawer.allapp.actionbar.GLFolderActionBar;
import com.jiubang.shell.appdrawer.allapp.actionbar.GLPreviewBar;
import com.jiubang.shell.appdrawer.animation.AnimationFactory;
import com.jiubang.shell.appdrawer.component.GLBarContainer;
import com.jiubang.shell.appdrawer.component.GLBottomBarContainer;
import com.jiubang.shell.appdrawer.component.GLGridViewContainer;
import com.jiubang.shell.appdrawer.component.GLTopBarContainer;
import com.jiubang.shell.appdrawer.controler.AbsStatusManager;
import com.jiubang.shell.appdrawer.controler.AllAppTabStatus;
import com.jiubang.shell.appdrawer.controler.AppDrawerStatusManager;
import com.jiubang.shell.appdrawer.controler.Status;
import com.jiubang.shell.appdrawer.controler.StatusChangeListener;
import com.jiubang.shell.appdrawer.slidemenu.GLAppDrawerSlideMenu;
import com.jiubang.shell.appdrawer.slidemenu.SlideMenuActionListener;
import com.jiubang.shell.common.component.VerScrollableGridViewHandler;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.common.management.JobManager;
import com.jiubang.shell.common.management.JobManager.Job;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.drag.DropTarget;
import com.jiubang.shell.folder.BaseFolderIcon;
import com.jiubang.shell.folder.GLAppFolder;
import com.jiubang.shell.folder.GLAppFolderMainView.FolderStatusListener;
import com.jiubang.shell.folder.GLAppFolderMainView.FolderViewAnimationListener;
import com.jiubang.shell.folder.status.FolderStatusManager;
import com.jiubang.shell.gesture.OnMultiTouchGestureListener;
import com.jiubang.shell.gesture.PointInfo;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.utils.GaussianBlurEffectUtils;
import com.jiubang.shell.utils.ToastUtils;

/**
 * 你升职了
 * 功能表主view
 */
public class GLAppDrawerMainView extends GLFrameLayout
		implements
			IBackgroundInfoChangedObserver,
			StatusChangeListener,
			OnMultiTouchGestureListener,
			FolderStatusListener,
			FolderViewAnimationListener,
			BatchAnimationObserver,
			SlideMenuActionListener,
			GridContainerTouchListener,
			OnAppDrawerVisibilityChangedListener {

	private AbsStatusManager mStatusManager;
	private FuncAppDataHandler mDataHandler;
	private DragController mDragController;
	private IShell mShell;

	private GLBarContainer mTopBarContainer;
	private GLBarContainer mBottomBarContainer;
	private GLGridViewContainer mGridViewContainer;

	private AppDrawerControler mControler;

	private int mSwipTouchSlop;
	private int mSwipVelocity;
	private FastVelocityTracker mVelocityTracker;
	private float mInterceptTouchVY;
	private float mInterceptTouchDownX;
	private float mInterceptTouchDownY;
	private float mInterceptTouchMoveX;
	private float mInterceptTouchMoveY;
	private boolean mInterceptTouchMoved;
	private int mTouchSlop;
	private int mSwipDy;
	private FuncAppDataHandler mFuncAppDataHandler;
	// 滑动方向与水平方向夹角小于60度的一律视为滚动屏幕
	public static final float SCROLL_MINDEGREE = 60.0f;
	public static final int HALF_CIRCLE_ANGLE = 180;
	public static final float SCROLL_TAN_MINDEGREE = (float) Math.tan(SCROLL_MINDEGREE
			/ HALF_CIRCLE_ANGLE * 3.1415926);
	private static final int PAGING_TOUCH_SLOP = 16;
	private int mTopBarSize = 0;
	private int mBottomBarSize = 0;
	/**
	 * 竖屏时Grid的顶部padding
	 */
	private int mGridPaddingTopV = 0;
	/**
	 * 竖屏时Grid的底部padding
	 */
	private int mGridPaddingBottomV = 0;
	/**
	 * 横屏时Grid的顶部padding
	 */
	private int mGridPaddingTopH = 0;
	/**
	 * 横屏时Grid的底部padding
	 */
	private int mGridPaddingBottomH = 0;
	/**
	 * Grid顶部栏位置扩大距离（隐藏顶部栏填充顶部栏位置）
	 */
	private int mGridTopEnxtend = 0;
	/**
	 * Grid底部栏位置扩大距离（隐藏顶部栏填充底部栏位置）
	 */
	private int mGridBottomEnxtend = 0;
	public static final int SWIPE_ANIMATION_DURATION = 300;
	private static final int ANIMATION_SWIPE_UP = 1;
	private static final int ANIMATION_SWIPE_DOWN = 2;
	public static final int ANIMATION_TOP_CONTAINER_SHOW = 3;
	public static final int ANIMATION_TOP_CONTAINER_HIDE = 4;
	public static final int ANIMATION_BOTTOM_CONTAINER_SHOW = 5;
	public static final int ANIMATION_BOTTOM_CONTAINER_HIDE = 6;
	/**
	 * 是否正在进行手势动画
	 */
	private boolean mIsDoingSwipeAnimation = false;
	/**
	 * AllApp是否显示更新标识
	 */
	private boolean mShowAllAppUpdate = false;
	private boolean mTouchOnNonGestureArea;

	private Animation mExtendFunOutAnimation;
	private Animation mExtendFunInAnimation;

	private boolean mIsDownOnLeft;
	private InterpolatorValueAnimation mSlideAnimation;
	private GLAppDrawerSlideMenu mSlideMenu;
	private static boolean sIsSlideMenuShow;
	private static final int SLIDE_ANIMATION_DURATION = 500;
	/**
	 * 侧边栏拉动响应比例，数字越大代BroadCasterObserver表越容易滑出／收起
	 */
	private static final int SLIDE_EFFECT_X_RATIO = 4;

	public GLAppDrawerMainView(Context context) {
		super(context);
		mDataHandler = FuncAppDataHandler.getInstance(ShellAdmin.sShellManager.getActivity());
		mDataHandler.registerBgInfoChangeObserver(this);
		mStatusManager = AppDrawerStatusManager.getInstance();
		mStatusManager.registListener(this);
		setHasPixelOverlayed(false);
		mControler = AppDrawerControler.getInstance(ShellAdmin.sShellManager.getActivity());

		mVelocityTracker = new FastVelocityTracker();
		mSwipTouchSlop = (int) (DrawUtils.sDensity * PAGING_TOUCH_SLOP + 0.5f) * 4;
		ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mSwipVelocity = configuration.getScaledMinimumFlingVelocity() * 4;
		mTouchSlop = configuration.getScaledTouchSlop();
		mFuncAppDataHandler = FuncAppDataHandler
				.getInstance(ShellAdmin.sShellManager.getActivity());
		mTopBarSize = mContext.getResources().getDimensionPixelSize(
				R.dimen.appdrawer_top_bar_container_height);
		mBottomBarSize = mContext.getResources().getDimensionPixelSize(
				R.dimen.appdrawer_bottom_bar_container_height);
		mShowAllAppUpdate = mDataHandler.isShowAppUpdate();
		
		initViews();
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		if (mSlideAnimation != null && !mSlideAnimation.isFinished()) {
			mSlideAnimation.animate();
		}
		super.dispatchDraw(canvas);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (!isCanAction()) {
			return true;
		} else {
			return super.dispatchTouchEvent(ev);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean ret = false;
		int action = ev.getAction() & MotionEvent.ACTION_MASK;
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		if (!sIsSlideMenuShow) {
			int direct = mFuncAppDataHandler.getSlideDirection();
			if (direct == FunAppSetting.SCREENMOVEHORIZONTAL) {
				final int slop = mTouchSlop;
				switch (action) {
					case MotionEvent.ACTION_DOWN :
						mSwipDy = 0;
						mInterceptTouchDownX = ev.getX();
						mInterceptTouchDownY = ev.getY();
						mInterceptTouchMoveX = 0;
						mInterceptTouchMoveY = 0;
						mInterceptTouchMoved = false;
						if (!GoLauncherActivityProxy.isPortait()) {
							Rect rect = new Rect();

							mTopBarContainer.getHitRect(rect);
							if (mFuncAppDataHandler.isShowTabRow() && rect.contains(x, y)) {
								mTouchOnNonGestureArea = true;
							}
							mBottomBarContainer.getHitRect(rect);
							if (mFuncAppDataHandler.isShowActionBar()
									&& rect.contains(x, y)) {
								mTouchOnNonGestureArea = true;
							}
						}
						break;
					case MotionEvent.ACTION_MOVE :
						if (!mTouchOnNonGestureArea) {
							mVelocityTracker.addMovement(ev);
							mVelocityTracker.computeCurrentVelocity(1000);
							mInterceptTouchVY = mVelocityTracker.getYVelocity();
							if (!mInterceptTouchMoved) {
								// 一旦超出拖动范围不会再更新，作为初始的拖动斜率
								mInterceptTouchMoveX = Math.abs(ev.getX()
										- mInterceptTouchDownX);
								mInterceptTouchMoveY = Math.abs(ev.getY()
										- mInterceptTouchDownY);
								mInterceptTouchMoved = mInterceptTouchMoveX > slop
										|| mInterceptTouchMoveY > slop;
							}
							if (mInterceptTouchMoved
									&& mInterceptTouchMoveY > mInterceptTouchMoveX
											* SCROLL_TAN_MINDEGREE) {
								ret = true;
							}
						}
						break;
					case MotionEvent.ACTION_UP :
					case MotionEvent.ACTION_CANCEL :
						//							mSwipDy = 0;
						mTouchOnNonGestureArea = false;
						break;

					default :
						break;
				}
			}
		}
		return ret;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int action = ev.getAction() & MotionEvent.ACTION_MASK;
		switch (action) {
			case MotionEvent.ACTION_MOVE :
				mVelocityTracker.addMovement(ev);
				mVelocityTracker.computeCurrentVelocity(1000);
				mInterceptTouchVY = mVelocityTracker.getYVelocity();
				break;
			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL :
				mTouchOnNonGestureArea = false;
				break;
			default :
				break;
		}
		return super.onTouchEvent(ev);
	}

	public void setDragController(DragController dragController) {
		mDragController = dragController;
		mStatusManager.setDragController(mDragController);
	}

	private void initViews() {
		// 第一次进功能表初始化
		mGridViewContainer = new GLGridViewContainer(mContext);
		addView(mGridViewContainer);
		
		GLLayoutInflater inflater = ShellAdmin.sShellManager.getLayoutInflater();
		inflater.inflate(R.layout.gl_appdrawer_slide_menu, this, true);
		
		mTopBarContainer = new GLTopBarContainer(mContext);
		addView(mTopBarContainer);
		
		mBottomBarContainer = new GLBottomBarContainer(mContext);
		addView(mBottomBarContainer);
		
		mSlideMenu = (GLAppDrawerSlideMenu) findViewById(R.id.appdrawer_slide_menu);
		mSlideAnimation = new InterpolatorValueAnimation(0);
		mSlideAnimation.setInterpolation(InterpolatorFactory.getInterpolator(
				InterpolatorFactory.CUBIC, InterpolatorFactory.EASE_OUT));
		mSlideMenu.setValueAnimation(mSlideAnimation);
		mSlideMenu.setSlideMenuActionListener(this);
		mGridViewContainer.setValueAnimation(mSlideAnimation);
		mGridViewContainer.setGridContainerTouchListener(this);
		
		handleShowTopBarChange();
		handleShowBottomBarChange();
		updateGridPadding();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int slideMenuWidth = mSlideMenu.getMeasuredWidth();
        int slideMenuHeight = mSlideMenu.getMeasuredHeight();
		mSlideMenu.layout(left, top, left + slideMenuWidth, top + slideMenuHeight);
		int statusBarHeight = StatusBarHandler.isHide() ? 0 : StatusBarHandler.getStatusbarHeight();
		if (GoLauncherActivityProxy.isPortait()) {
			mTopBarContainer.layout(0, statusBarHeight, mWidth, statusBarHeight + mTopBarSize);
			mBottomBarContainer.layout(0, mHeight - mBottomBarSize, mWidth, mHeight);
			mGridViewContainer.layout(0, statusBarHeight + mTopBarSize + mGridTopEnxtend
					+ mGridPaddingTopV, mWidth, mHeight + mGridBottomEnxtend - mBottomBarSize
					- mGridPaddingBottomV);
		} else {
			mTopBarContainer.layout(0, statusBarHeight, mWidth, statusBarHeight + mTopBarSize);
			mBottomBarContainer.layout(0, mHeight - mBottomBarSize, mWidth, mHeight);
			mGridViewContainer.layout(0, statusBarHeight + mTopBarSize + mGridTopEnxtend + mGridPaddingTopH,
					mWidth, mHeight + mGridBottomEnxtend - mBottomBarSize - mGridPaddingBottomH);
		}
	}

	private void updateGridPadding() {
		if (mDataHandler.getSlideDirection() == FunAppSetting.SCREENMOVEVERTICAL
				&& mDataHandler.getVerticalScrollEffect() == VerScrollableGridViewHandler.WATERFALL_VERTICAL_EFFECTOR) {
			mGridPaddingTopH = mContext.getResources().getDimensionPixelSize(
					R.dimen.appdrawer_waterfall_effector_size);
			if (!mDataHandler.isShowTabRow()) {
				mGridPaddingTopV = mContext.getResources().getDimensionPixelSize(
						R.dimen.appdrawer_top_bar_container_height);
			} else {
				mGridPaddingTopV = 0;
			}
		} else {
			mGridPaddingTopV = 0;
			mGridPaddingTopH = 0;
		}

		if (mDataHandler.getSlideDirection() == FunAppSetting.SCREENMOVEVERTICAL
				&& mDataHandler.getVerticalScrollEffect() == VerScrollableGridViewHandler.WATERFALL_VERTICAL_EFFECTOR) {
			mGridPaddingBottomH = mContext.getResources().getDimensionPixelSize(
					R.dimen.appdrawer_waterfall_effector_size);
			if (!mDataHandler.isShowActionBar()) {
				mGridPaddingBottomV = mContext.getResources().getDimensionPixelSize(
						R.dimen.appdrawer_bottom_bar_container_height);
			} else {
				mGridPaddingBottomV = 0;
			}
		} else {
			mGridPaddingBottomH = 0;
			mGridPaddingBottomV = 0;
		}
	}

	public void notifyGridDataSetChange() {
		mGridViewContainer.notifyGridDataSetChange();
		Status curStatus = mStatusManager.getCurStatus();
		if (curStatus instanceof AllAppTabStatus) {
			ArrayList<IActionBar> bar = ((AllAppTabStatus) curStatus).getTopBarViewGroup();
			if (bar.get(1) instanceof GLFolderActionBar) {
				((GLFolderActionBar) bar.get(1)).notifyDataSetChanged();
			}
		}
	}

	/**
	 * 刷新功能表顶部容器布局
	 */
	public void requestLayoutTopBar() {
		mTopBarContainer.requestLayout();
	}

	/**
	 * 刷新功能表底部容器布局
	 */
	public void requestLayoutBottomBar() {
		mBottomBarContainer.requestLayout();
	}

	/**
	 * 刷新功能表GridView
	 */
	public void requestLayoutGridView() {
		mGridViewContainer.requestLayout();
	}

	/**
	 * 这个回调方法全部基本上是数据更新需要刷新UI
	 */
	@Override
	public boolean handleChanges(MessageID msgId, Object obj1, Object obj2) {
		switch (msgId) {
			case ALL_PROGRAMSORT :
			case ADD_BATCH_APP :
			case APP_ADDED :
			case HIDE_APPS :
				notifyGridDataSetChange();
//				mShell.showProgressBar(false);
				break;
			case APP_REMOVED :
				if (obj2 instanceof FunFolderItemInfo) {
					FunFolderItemInfo folder = (FunFolderItemInfo) obj2;
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_ALL_APP_GRID_VIEW,
							IAppDrawerMsgId.APPDRAWER_ALL_APP_REMOVE_ICON, -1, folder);
				}
				notifyGridDataSetChange();
//				mShell.showProgressBar(false);
				break;
			case ADD_ITEM :
			case ADD_ITEMS :
			case REMOVE_ITEM :
			case REMOVE_ITEMS :
				notifyGridDataSetChange();
//				mShell.showProgressBar(false);
				break;
			case REFREASH_APPDRAWER :
				notifyGridDataSetChange();
				break;
			case REFREASH_FOLDERBAR_TARGET :
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_FOLDER_ACTION_BAR,
						IAppDrawerMsgId.APPDRAWER_REFREASH_FOLDERBAR_TARGET, 0, obj2);
				break;
			case SHOWNAME_CHANGED : // 显示程序名字改变
				mGridViewContainer.handleShowAppNameChange();
				break;
			case SHOW_APP_UPDATE_CHANGE : // 显示程序更新改变
				mGridViewContainer.handleAllAppIconStateChange();
				mShowAllAppUpdate = mDataHandler.isShowAppUpdate();
				break;
			case STANDARD_CHANGED : // 行列数改变
				mGridViewContainer.handleRowColumnSettingChange();
				break;
			case SLIDEDIRECTION_CHANGED : // 滑动方向改变
			case VERTICAL_SCROLL_EFFECT_CHANGED : // 竖屏特效改变
				mGridViewContainer.handleScrollerSettingChange();
				updateGridPadding();
				mGridViewContainer.buildShadowDrawable();
				break;
			case ICONEFFECT_CHANGED : // 横向特效改变
				mGridViewContainer.handleScrollerSettingChange();
				updateGridPadding();
				break;
			case SCROLL_LOOP_CHANGED : // 循环模式改变
				mGridViewContainer.handleScrollLoopChange();
				break;
			case SHOW_HOME_KEY_ONLY_CHANGE : // 是否只显示home键改变
				((GLBottomBarContainer) mBottomBarContainer).handleShowHomeKeyOnlyChange();
				break;
			case SHOW_TAB_ROW_CHANGED : // 是否显示tab栏改变
				updateGridPadding();
				handleShowTopBarChange();
				break;
			case SHOW_ACTION_BAR_CHANGE : // 是否显示底部栏改变
				updateGridPadding();
				handleShowBottomBarChange();
				break;
			case FIRST_INIT_DONE : // 整个功能表加载完成
				boolean isFirstCreatDB = (Boolean) obj2;
				if (isFirstCreatDB) {
					mControler.arrangeAppAuto(0, false);
					ArrayList<SpecialAppItemInfo> specItemInfoList = SpecialAppManager
							.getInstance().getAppItemInfos(ICustomAction.ACTION_RECENTAPP);
					if (!specItemInfoList.isEmpty()) {
						mControler.moveFunItemInfo(
								mControler.getFunItemInfo(specItemInfoList.get(0).mIntent), 0);
					}
					specItemInfoList = SpecialAppManager.getInstance().getAppItemInfos(
							ICustomAction.ACTION_PROMANAGE);
					if (!specItemInfoList.isEmpty()) {
						mControler.moveFunItemInfo(
								mControler.getFunItemInfo(specItemInfoList.get(0).mIntent), 1);
					}
				}
				Status status = mStatusManager.getCurStatus();
				if (status instanceof AllAppTabStatus) {
					ArrayList<IActionBar> topBars = ((AllAppTabStatus) status).getTopBarViewGroup();
					if (topBars.get(1) instanceof GLFolderActionBar) {
						((GLFolderActionBar) topBars.get(1)).refreshGridView();
						((GLFolderActionBar) topBars.get(1)).requestLayout();
					}
					ArrayList<IActionBar> bottomBars = ((AllAppTabStatus) status)
							.getBottomBarViewGroup();
					if (bottomBars.get(1) instanceof GLPreviewBar) {
						((GLPreviewBar) bottomBars.get(1)).indicateToOutSide();
						((GLPreviewBar) bottomBars.get(1)).refreshGridView();
						((GLPreviewBar) bottomBars.get(1)).requestLayout();
					}
				}
				mShell.showProgressBar(false);
				MsgMgrProxy.sendMessage(this,
						IDiyFrameIds.APP_DRAWER_ALL_APP_TOP_ACTION_BAR,
						IAppDrawerMsgId.APPDRAWER_SHOW_SIDEBAR_GUIDE_CLOUD, 1);
				break;
			case RELOAD_INIT_DONE : // 整个功能表2次加载完成
//				GLAppFolder.getInstance().refreshAppdrawerFolderIconInfos();
				mShell.showProgressBar(false);
				break;
			case ARRANGE_END : 
				mControler.refreshAppDrawer();
//				refreshDrawerFolderIcons();
				mGridViewContainer.scrollToFirst();
				mShell.showProgressBar(false);
				PrivatePreference pref = PrivatePreference.getPreference(mContext);
			    boolean classify = pref.getBoolean(PrefConst.KEY_APP_CLASSIFY_FIRST_TIME_AUTO_ARRAGEMENT, false);
			    //Only show the toast in the first time auto-folder
			    if (!classify) {
					ToastUtils.showToast(R.string.appdrawer_automatically_classified, Toast.LENGTH_LONG);
					pref.putBoolean(PrefConst.KEY_APP_CLASSIFY_FIRST_TIME_AUTO_ARRAGEMENT, true);
			    	pref.commit();
			    }
			    RecommInfoServer.getServer(mContext).loadDataAysnc();
				break;
			default :
				break;
		}
		return false;
	}

	/**
	 * 重新获取应用中心可更新应用数目
	 */
	public void regetUpdateableAppsCount() {
		if (mShowAllAppUpdate) {
			mGridViewContainer.regetUpdateableAppsCount();
		}
	}

	/**
	 * 处理顶部栏显示改变
	 */
	private void handleShowTopBarChange() {
		if (mDataHandler.isShowTabRow()) {
			mGridTopEnxtend = 0;
			mTopBarContainer.setVisibility(GLView.VISIBLE);
		} else {
			mGridTopEnxtend = -mTopBarSize;
			mTopBarContainer.setVisibility(GLView.INVISIBLE);
		}
		requestLayout();
	}

	/**
	 * 处理底部栏显示改变
	 */
	private void handleShowBottomBarChange() {
		if (mDataHandler.isShowActionBar()) {
			mGridBottomEnxtend = 0;
			mBottomBarContainer.setVisibility(GLView.VISIBLE);
		} else {
			mGridBottomEnxtend = mBottomBarSize;
			mBottomBarContainer.setVisibility(GLView.INVISIBLE);
		}
		requestLayout();
	}

	/**
	 * 处理tab栏、底部栏主题改变
	 */
	public void handleTabBottomThemeChange() {
		((GLTopBarContainer) mTopBarContainer).handleTabThemeChange();
		((GLBottomBarContainer) mBottomBarContainer).handleBottomThemeChange();
	}

	public void handleInidcatorThemeChange() {
		mGridViewContainer.handleInidcatorThemeChange();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (isCanAction()) {
			if (mSlideMenu.isVisible()) {
				return mSlideMenu.onKeyDown(keyCode, event);
			} else if (mStatusManager.getCurStatus().onKeyDown(keyCode, event)) {
				return true;
			}
			return super.onKeyDown(keyCode, event);
		}
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (isCanAction()) {
			if (mSlideMenu.isVisible()) {
				return mSlideMenu.onKeyUp(keyCode, event);
			} else if (mStatusManager.getCurStatus().onKeyUp(keyCode, event)) {
				return true;
			}
			return super.onKeyUp(keyCode, event);
		}
		return false;
	}

	@Override
	public void onSceneStatusChange(Status oldStatus, Status curStatus, Object[] objects) {
		if (oldStatus != null) {
			mDragController.removeDropTarget(oldStatus.getGridView());
			mDragController.removeDragListener(oldStatus.getGridView());
			IActionBar topBar = oldStatus.getTopBarViewByGridSatus();
			if (topBar instanceof DropTarget) {
				mDragController.removeDropTarget((DropTarget) topBar);
			}
			IActionBar bottomBar = oldStatus.getBottomBarViewByGridSatus();
			if (bottomBar instanceof DropTarget) {
				mDragController.removeDropTarget((DropTarget) bottomBar);
			}
		}

		if (curStatus != null) {
			mDragController.removeDragListener((GLTopBarContainer) mTopBarContainer);

			mGridViewContainer.setCurStatus(curStatus);
			mBottomBarContainer.setBarViewGroup(curStatus.getBottomBarViewGroup());
			mTopBarContainer.setBarViewGroup(curStatus.getTopBarViewGroup());
			mDragController.addDropTarget(curStatus.getGridView(), curStatus.getGridView()
					.getTopViewId());
			mDragController.addDragListener(curStatus.getGridView());
			IActionBar topBar = curStatus.getTopBarViewByGridSatus();
			if (topBar instanceof DropTarget) {
				DropTarget target = (DropTarget) topBar;
				mDragController.addDropTarget(target, target.getTopViewId());
			}
			IActionBar bottomBar = curStatus.getBottomBarViewByGridSatus();
			if (bottomBar instanceof DropTarget) {
				DropTarget target = (DropTarget) bottomBar;
				mDragController.addDropTarget(target, target.getTopViewId());
			}
		}
	}

	@Override
	public void onGridStatusChange(Status oldStatus, Status curStatus, Object[] objects) {
		if (oldStatus != null) {
			IActionBar topBar = oldStatus.getTopBarViewByGridSatus();
			if (topBar instanceof DropTarget) {
				mDragController.removeDropTarget((DropTarget) topBar);
			}
			IActionBar bottomBar = oldStatus.getBottomBarViewByGridSatus();
			if (bottomBar instanceof DropTarget) {
				mDragController.removeDropTarget((DropTarget) bottomBar);
			}
		}

		if (curStatus != null) {
			mGridViewContainer.setCurStatus(curStatus);
			boolean animate = true;
			if (objects != null && objects.length > 0 && objects[0] instanceof Boolean) {
				animate = (Boolean) objects[0];
			}
			mBottomBarContainer.switchBarView(curStatus.getBottomBarViewByGridSatus(), animate);
			mTopBarContainer.switchBarView(curStatus.getTopBarViewByGridSatus(), animate);
			IActionBar topBar = curStatus.getTopBarViewByGridSatus();
			if (topBar instanceof DropTarget) {
				DropTarget target = (DropTarget) topBar;
				mDragController.addDropTarget(target, target.getTopViewId());
			}
			IActionBar bottomBar = curStatus.getBottomBarViewByGridSatus();
			if (bottomBar instanceof DropTarget) {
				DropTarget target = (DropTarget) bottomBar;
				mDragController.addDropTarget(target, target.getTopViewId());
			}
		}
	}

	public void setShell(IShell shell) {
		mShell = shell;
		mTopBarContainer.setShell(mShell);
		mBottomBarContainer.setShell(mShell);
		mStatusManager.setShell(mShell);
		setDragController(shell.getDragController());
	}


	public void popupMenu() {
		if (mStatusManager != null) {
			mStatusManager.getCurStatus().popupMenu();
		}
	}

	public void handleExtendFuncAnim(final boolean visible, int animateType, boolean animate,
			final Object... objects) {
		mBottomBarContainer.clearAnimation();
		mTopBarContainer.clearAnimation();
		mGridViewContainer.clearAnimation();
		if (animateType != GLAppDrawer.EXTEND_FUNC_ANIM_TYPE_NONE) {
			AnimationTask task = new AnimationTask(true, AnimationTask.PARALLEL);
			switch (animateType) {
				case GLAppDrawer.EXTEND_FUNC_ANIM_TYPE_ZOOM :
					setVisible(visible);
					if (animate) {
						Animation animation = null;
						if (visible) {
							animation = mExtendFunInAnimation;
						} else {
							animation = mExtendFunOutAnimation;
						}
						if (animation != null) {
							task.addAnimation(this, animation, new AnimationListenerAdapter() {

								@Override
								public void onAnimationStart(Animation animation) {
									GLAppDrawerMainView.this.setDrawingCacheEnabled(true);
								}

								@Override
								public void onAnimationEnd(Animation animation) {
									GLAppDrawerMainView.this.setDrawingCacheEnabled(false);
									if (mShell != null) {
										mShell.hide(IViewId.PROTECTED_LAYER, false);
									}
								}
							});
							if (mShell != null) {
								mShell.show(IViewId.PROTECTED_LAYER, false);
							}
							GLAnimationManager.startAnimation(task);
						}
					}
					break;
				case GLAppDrawer.EXTEND_FUNC_ANIM_TYPE_FLY :
					Animation alphaAnim = null;
					Animation scaleAnim = null;
					if (visible) {
						setVisible(visible);
						FunAppSetting appSetting = SettingProxy.getFunAppSetting();
						if (appSetting.getShowTabRow() == AppSettingDefault.SHOW_TAB_ROW) {
							mTopBarContainer
									.translateInAnimation(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
						}
						if (appSetting.getShowActionBar() == AppSettingDefault.SHOW_ACTION_BAR) {
							mBottomBarContainer
									.translateInAnimation(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
						}
						alphaAnim = new AlphaAnimation(0.0f, 1.0f);
						scaleAnim = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f,
								Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					} else {
						FunAppSetting appSetting = SettingProxy.getFunAppSetting();
						if (appSetting.getShowTabRow() == AppSettingDefault.SHOW_TAB_ROW) {
							mTopBarContainer
									.translateOutAnimation(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
						}
						if (appSetting.getShowActionBar() == AppSettingDefault.SHOW_ACTION_BAR) {
							mBottomBarContainer
									.translateOutAnimation(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
						}
						alphaAnim = new AlphaAnimation(1.0f, 0.0f);
						scaleAnim = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					}
					AnimationSet animSet = new AnimationSet(true);
					animSet.addAnimation(alphaAnim);
					animSet.addAnimation(scaleAnim);
					animSet.setDuration(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
					animSet.setFillAfter(true);
					mGridViewContainer.setHasPixelOverlayed(false);
					task.addAnimation(mGridViewContainer, animSet, new AnimationListenerAdapter() {

						@Override
						public void onAnimationEnd(Animation animation) {

							post(new Runnable() {

								@Override
								public void run() {
									if (mShell != null) {
										mShell.hide(IViewId.PROTECTED_LAYER, false);
									}
									mGridViewContainer.setHasPixelOverlayed(true);
									if (!visible) {
										setVisible(visible);
									}
									if (objects.length >= 2 && objects[1] instanceof Intent) {
										Intent intent = (Intent) objects[1];
										if (mShell != null) {
											mShell.show(IViewId.PROTECTED_LAYER, false);
										}
										locateApp(intent);
									}
								}
							});
						}
					});
					if (mShell != null) {
						mShell.show(IViewId.PROTECTED_LAYER, false);
					}
					GLAnimationManager.startAnimation(task);
					break;
				case GLAppDrawer.EXTEND_FUNC_ANIM_TYPE_BLUR :
					if (animate) {
						if (visible) {
							setVisible(visible);
							FunAppSetting appSetting = SettingProxy.getFunAppSetting();
							if (appSetting.getShowTabRow() == AppSettingDefault.SHOW_TAB_ROW) {
								mTopBarContainer
										.translateInAnimation(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
							}
							if (appSetting.getShowActionBar() == AppSettingDefault.SHOW_ACTION_BAR) {
								mBottomBarContainer
										.translateInAnimation(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
							}
							GaussianBlurEffectUtils.disableBlurWithZoomInAnimation(
									mGridViewContainer, null);
						} else {
							FunAppSetting appSetting = SettingProxy.getFunAppSetting();
							if (appSetting.getShowTabRow() == AppSettingDefault.SHOW_TAB_ROW) {
								mTopBarContainer
										.translateOutAnimation(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
							}
							if (appSetting.getShowActionBar() == AppSettingDefault.SHOW_ACTION_BAR) {
								mBottomBarContainer
										.translateOutAnimation(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
							}
							GaussianBlurEffectUtils.enableBlurWithZoomOutAnimation(
									mGridViewContainer, null);
						}
					} else {
						if (visible) {
							GaussianBlurEffectUtils.disableBlurWithoutAnimation(mGridViewContainer);
						}
					}
					
					break;
				default :
					break;
			}

		} else {
			setVisible(visible);
		}
	}

	
	@Override
	public void onAppDrawerVisibilityChanged(boolean visible, boolean animate, boolean isFirstEnter) {
		int effect = FuncAppDataHandler.getInstance(ShellAdmin.sShellManager.getActivity())
				.getInoutEffect();
		boolean needAnimate = animate && (effect == AnimationFactory.EFFECT_NONE ? false : true);
		if (visible) {
			// 第一次进来，额外做点事
			if (isFirstEnter) {
				mShell.showProgressBar(true);
				mStatusManager.changeStatus(AppDrawerStatusManager.ALLAPP_TAB,
						AppDrawerStatusManager.GRID_NORMAL_STATUS);
				mControler.startAllAppInitThread();
				// 强制取消第一次的进入动画
				needAnimate = false;
			}
			mGridViewContainer.clearAnimation();
			mBottomBarContainer.clearAnimation();
			mTopBarContainer.clearAnimation();
			// 每次进来标题闪烁
//			if (DrawUtils.sDensity >= 1.5) {
//				// 特殊原因的判断
//				mTopBarContainer.startSpark();
//			}
//			GuideControler cloudView = GuideControler.getInstance(mContext);
//			cloudView.showSlideMenuGuide();
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.SCREEN_ENABLE_WIDGET_DRAWING_CACHE, 1);
			if (needAnimate) {
				activeEnterAnimation(needAnimate, effect);
			}
		} else {
			mStatusManager.getCurStatus().dismissMenu();
			mStatusManager.changeGridStatus(AppDrawerStatusManager.GRID_NORMAL_STATUS, needAnimate);
//			if (DrawUtils.sDensity >= 1.5) {
//				// 特殊原因的判断
//				mTopBarContainer.pauseSpark();
//			}
			boolean needRefresh = false;
			try { // 这个不需要主动刷新
				SecurityPoxyFactory.getSecurityPoxy().clearSecurityResult(); // 清除安全的标识
			} catch (UnsupportSecurityPoxyException ex) {
			}
			if (mDataHandler.ismClickAppupdate()) {
				if (mControler.clearAllAppUpdate()) {
					needRefresh = true;
				}
			}
			if (needRefresh) {
				mGridViewContainer.handleAllAppIconStateChange(); // 通知刷新
			}
			if (needAnimate) {
				// 加载完才能做动画
					activeExitAnimation(animate, effect);
				}
			
//			GuideControler cloudView = GuideControler.getInstance(mContext);
//			cloudView.removeFromCoverFrame(GuideControler.CLOUD_ID_APPDRAW_SIDEBAR);
			
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.SCREEN_ENABLE_WIDGET_DRAWING_CACHE, 0);
		}
	}


	private void activeEnterAnimation(boolean animate, int effect) {
		mGridViewContainer.clearAnimation();
		mBottomBarContainer.clearAnimation();
		mTopBarContainer.clearAnimation();
		if (animate) {
			if (effect == AnimationFactory.EFFECT_DEFAULT) {
				FunAppSetting appSetting = SettingProxy.getFunAppSetting();
				if (appSetting.getShowTabRow() == AppSettingDefault.SHOW_TAB_ROW) {
					mTopBarContainer.translateInAnimation(AnimationFactory.DEFAULT_DURATION);
				}
				if (appSetting.getShowActionBar() == AppSettingDefault.SHOW_ACTION_BAR) {
					mBottomBarContainer.translateInAnimation(AnimationFactory.DEFAULT_DURATION);
				}
			}
		}
	}

	private void activeExitAnimation(boolean animate, int effect) {
		mGridViewContainer.clearAnimation();
		mBottomBarContainer.clearAnimation();
		mTopBarContainer.clearAnimation();
		if (animate) {
			if (effect == AnimationFactory.EFFECT_DEFAULT) {
				FunAppSetting appSetting = SettingProxy.getFunAppSetting();
				if (appSetting.getShowTabRow() == AppSettingDefault.SHOW_TAB_ROW) {
					mTopBarContainer.translateOutAnimation(AnimationFactory.DEFAULT_DURATION);
				}
				if (appSetting.getShowActionBar() == AppSettingDefault.SHOW_ACTION_BAR) {
					mBottomBarContainer.translateOutAnimation(AnimationFactory.DEFAULT_DURATION);
				}
			}
		}
	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param async 是否异步　true:异步　false:同步
	 */
	public void startSort(boolean async) {
		mShell.showProgressBar(true);
		if (async) {
			Thread thread = new Thread(ThreadName.FUNC_SORT) {
				@Override
				public void run() {
					mControler.startSort(true);
					mShell.showProgressBar(false);
				}
			};
			thread.start();
		} else {
			mControler.startSort(true);
			mShell.showProgressBar(false);
		}
	}

	@Override
	public boolean onSwipe(PointInfo p, float dx, float dy) {
		mSwipDy += dy;
		if (!mGridViewContainer.isVerScroll() && mGridViewContainer.isScrollFinish()
				&& !sIsSlideMenuShow && !mTouchOnNonGestureArea
				&& Math.abs(mSwipDy) > mSwipTouchSlop
				&& Math.abs(mInterceptTouchVY) > mSwipVelocity) {
			if (mFuncAppDataHandler.isGlideUpActionEnable() && mSwipDy < -mSwipTouchSlop) { //向上滑
				if (!mIsDoingSwipeAnimation) {
					AnimationTask task = new AnimationTask(true, AnimationTask.PARALLEL);
//					Animation moveAnimation = null;
					int type = ANIMATION_TOP_CONTAINER_SHOW;
//					if (GoLauncherActivityProxy.isPortait()) {
//						float startOffsetY;
//						float endOffsetY;
						if (mDataHandler.isShowTabRow()) {
//							startOffsetY = 0.0f;
//							endOffsetY = mTopBarSize * -1.0f;
							type = ANIMATION_TOP_CONTAINER_HIDE;
							mTopBarContainer.translateOutAnimation(SWIPE_ANIMATION_DURATION);
						} else {
//							startOffsetY = mTopBarSize * -1.0f;
//							endOffsetY = 0.0f;
							mTopBarContainer.translateInAnimation(SWIPE_ANIMATION_DURATION);
						}
//						moveAnimation = new TranslateAnimation(0.0f, 0.0f, startOffsetY, endOffsetY);
//					} else {
//						float startOffsetX;
//						float endOffsetX;
//						if (mDataHandler.isShowTabRow()) {
//							startOffsetX = 0.0f;
//							endOffsetX = mTopBarSize * -1.0f;
//							type = ANIMATION_TOP_CONTAINER_HIDE;
//						} else {
//							startOffsetX = mTopBarSize * -1.0f;
//							endOffsetX = 0.0f;
//						}
//						moveAnimation = new TranslateAnimation(startOffsetX, endOffsetX, 0.0f, 0.0f);
//					}
//					moveAnimation.setDuration(SWIPE_ANIMATION_DURATION);
//					task.addAnimation(mTopBarContainer, moveAnimation, null);
					mGridViewContainer.doSwipeAnimation(task, type, mTopBarSize);
					task.setBatchAnimationObserver(this, ANIMATION_SWIPE_UP);
					//					GLAnimationManager.startAnimation(task);
					if (task.isValid()) {
						mShell.show(IViewId.PROTECTED_LAYER, false);
						JobManager.postJob(new Job(-1, task, false));
					}
				}
				return true;
			}
			if (mSwipDy > mSwipTouchSlop) { //向下滑
				if (!mIsDoingSwipeAnimation) {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
							ICommonMsgId.SHOW_EXTEND_FUNC_VIEW, 1, IViewId.APP_DRAWER_SEARCH);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onTwoFingerSwipe(PointInfo p, float dx, float dy, int direction) {
		return true;
	}

	@Override
	public boolean onScale(PointInfo p, float scale, float scaleX, float scaleY, float dx,
			float dy, float angle) {
		return true;
	}

	@Override
	public boolean onDoubleTap(PointInfo p) {
		return true;
	}

	@Override
	public void onStart(int what, Object[] params) {
		switch (what) {
			case ANIMATION_SWIPE_UP :
				if (mTopBarContainer.getVisibility() == GLView.INVISIBLE) {
					mTopBarContainer.setVisibility(GLView.VISIBLE);
				}
				mIsDoingSwipeAnimation = true;
				break;
			case ANIMATION_SWIPE_DOWN :
				if (mBottomBarContainer.getVisibility() == GLView.INVISIBLE) {
					mBottomBarContainer.setVisibility(GLView.VISIBLE);
				}
				mIsDoingSwipeAnimation = true;
				break;
			default :
				break;
		}
	}

	@Override
	public void onFinish(int what, Object[] params) {
		FunAppSetting funAppSetting = SettingProxy.getFunAppSetting();
		switch (what) {
			case ANIMATION_SWIPE_UP :
				if (mDataHandler.isShowTabRow()) {
					funAppSetting.setShowTabRow(FunAppSetting.OFF);
				} else {
					funAppSetting.setShowTabRow(FunAppSetting.ON);
				}
				mIsDoingSwipeAnimation = false;
				break;
			case ANIMATION_SWIPE_DOWN :
				if (mDataHandler.isShowActionBar()) {
					funAppSetting.setShowActionBar(FunAppSetting.OFF);
				} else {
					funAppSetting.setShowActionBar(FunAppSetting.ON);
				}
				mIsDoingSwipeAnimation = false;
				break;
			default :
				break;
		}
		mShell.hide(IViewId.PROTECTED_LAYER, false);
	}

	@Override
	public void onFolderOpen(BaseFolderIcon<?> baseFolderIcon, boolean animate, int curStatus,
			boolean reopen) {
		if (mFuncAppDataHandler.isShowTabRow()) {
			mTopBarContainer.onFolderOpen(baseFolderIcon, animate, curStatus, reopen);
		}
		mBottomBarContainer.onFolderOpen(baseFolderIcon, animate, curStatus, reopen);
		mGridViewContainer.onFolderOpen(baseFolderIcon, animate, curStatus, reopen);
		GaussianBlurEffectUtils.enableBlurWithZoomOutAnimation(mGridViewContainer, null);
	}

	@Override
	public void onFolderClose(BaseFolderIcon<?> baseFolderIcon, boolean animate, int curStatus) {
		if (mFuncAppDataHandler.isShowTabRow()) {
			mTopBarContainer.onFolderClose(baseFolderIcon, animate, curStatus);
		}
		mBottomBarContainer.onFolderClose(baseFolderIcon, animate, curStatus);
		mGridViewContainer.onFolderClose(baseFolderIcon, animate, curStatus);
		if (animate) {
			GaussianBlurEffectUtils.disableBlurWithZoomInAnimation(mGridViewContainer, null);
		} else {
			GaussianBlurEffectUtils.disableBlurWithoutAnimation(mGridViewContainer);
		}
	}

	@Override
	public void onFolderStatusChange(int oldStatus, int newStatus) {
		if (mTopBarContainer.isVisible()) {
			mTopBarContainer.onFolderStatusChange(oldStatus, newStatus);
		}
		if (mBottomBarContainer.isVisible()) {
			mBottomBarContainer.onFolderStatusChange(oldStatus, newStatus);
		}
		mGridViewContainer.onFolderStatusChange(oldStatus, newStatus);
		switch (newStatus) {
			case FolderStatusManager.GRID_EDIT_STATUS :
				mStatusManager.changeGridStatus(AppDrawerStatusManager.GRID_EDIT_STATUS);
				break;
			case FolderStatusManager.GRID_NORMAL_STATUS :
				mStatusManager.changeGridStatus(AppDrawerStatusManager.GRID_NORMAL_STATUS);
				break;
			default :
				break;
		}
	}

	@Override
	public void onFolderOpenEnd(int curStatus) {
		mGridViewContainer.onFolderOpenEnd(curStatus);
	}

	@Override
	public void onFolderCloseEnd(int curStatus, BaseFolderIcon<?> baseFolderIcon, boolean needReopen) {
		mGridViewContainer.onFolderCloseEnd(curStatus, baseFolderIcon, needReopen);
	}

	public void onFolderDropComplete(Object target, Object dragInfo, boolean success,
			DropAnimationInfo resetInfo, long folderId) {
		mGridViewContainer.onFolderDropComplete(target, dragInfo, success, resetInfo, folderId);
	}

	@Override
	public void onFolderReLayout(BaseFolderIcon<?> baseFolderIcon, int curStatus) {
		mGridViewContainer.onFolderReLayout(baseFolderIcon, curStatus);
	}

	public void locateApp(Intent intent) {
		mGridViewContainer.locateApp(intent);
	}

	public void setFontSizeColor(int size, int color) {
		mGridViewContainer.setFontSizeColor(size, color);
	}

	public GLGridViewContainer getGridViewContainer() {
		return mGridViewContainer;
	}

	private boolean isCanAction() {
		return isVisible();
	}

	public void onHomeAction() {
		if (isVisible()) {
			Status status = mStatusManager.getCurStatus();
			if (status != null) {
				status.dismissMenu();
			}
			mStatusManager.changeGridStatus(AppDrawerStatusManager.GRID_NORMAL_STATUS);
			mGridViewContainer.resetAlpha();
		}
		mSlideMenu.onHomeAction();
	}

	public void onConfigurationChanged(int param) {
		if (mStatusManager != null) {
			Status cur = mStatusManager.getCurStatus();
			if (cur != null) {
				cur.dismissMenu();
			}
		}
		MediaPluginFactory.getSwitchMenuControler().dismissMenu();

		//add by zhangxi @2013-09-06 for将横竖屏的情况通知到顶部
		mTopBarContainer.onConfigurationChanged();
	}

	public void dismissSpecialFolder(int folderType) {
		Status curStatus = mStatusManager.getCurStatus();
		if (curStatus instanceof AllAppTabStatus) {
			IActionBar bar = ((AllAppTabStatus) curStatus).getTopBarViewByGridSatus();
			if (bar instanceof GLFolderActionBar) {
				((GLFolderActionBar) bar).hideSpecalFolder(folderType);
			}
		}
	}

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		mGridViewContainer.cancelLongPress();
		mTopBarContainer.cancelLongPress();
	}


	
	public void refreshDrawerFolderIcons() {
		post(new Runnable() {
			public void run() {
				GLAppFolder.getInstance().refreshDrawerFolderIcons();
			}
		});
	}

	/**
	 * 恢复智能整理
	 */
	public void recoverArrange() {
		mShell.showProgressBar(true);
		new Thread(ThreadName.FUNC_RECOVERARRANGE) {
			@Override
			public void run() {
				PreferencesManager manager = new PreferencesManager(mContext,
						IPreferencesIds.PREFERENCE_APPDRAW_ARRANGE_CONFG, Context.MODE_PRIVATE);
				String ids = manager.getString(
						IPreferencesIds.PREFERENCE_APPDRAW_ARRANGE_FOLDERS_ID, "");
				if (!ids.trim().equals("")) {
					String[] idArrary = ids.split("#");
					for (String id : idArrary) {
						try {
							GLAppFolderInfo info = GLAppFolderController.getInstance()
									.getFolderInfoById(Long.parseLong(id),
											GLAppFolderInfo.FOLDER_FROM_APPDRAWER);
							if (info != null) {
								GLAppFolderController.getInstance().removeAppDrawerFolder(
										info.getAppDrawerFolderInfo(), false);
							}

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					manager.clear();
					mControler.startSort(true);
					mControler.refreshAppDrawer();
					mShell.showProgressBar(false);
				} else {
					mControler.restoreAppTable();
					GLAppFolderController.getInstance().clearAppDrawerFolderInfos();
					GLAppFolder.getInstance().setForceRefreshGLAppFolderGrid();
					mControler.reloadAllAppItems();
				}
			};
		}.start();
	}

	public void dragFlingDelete(boolean isDragFromFolder, Object dragInfo) {
		mGridViewContainer.dragFlingDelete(isDragFromFolder, dragInfo);

	}

	public void setExtendFuncInAnimation(Animation animation) {
		mExtendFunInAnimation = animation;
	}

	public void setExtendFuncOutAnimation(Animation animation) {
		mExtendFunOutAnimation = animation;
	}

	/**
	 * 智能整理
	 */
	public void startArrangeApp(final int entrance) {
		//TODO 收回转转bar
		mShell.showProgressBar(true);
		new Thread(ThreadName.FUNC_ARRANGE) {

			@Override
			public void run() {
				mControler.arrangeAppAuto(entrance, true);
			}

		}.start();
	}
	
	public void showSlideMenu(boolean needAnimation) {
		mSlideMenu.setVisible(true);
		sIsSlideMenuShow = true;
		int width = mSlideMenu.getWidth();
		if (needAnimation) {
			doSlideMenuAnimation(0, width, SLIDE_ANIMATION_DURATION);
		} else {
			mSlideAnimation.setValue(width);
			mSlideAnimation.setDstValue(width);
		}
	}

	public void hideSlideMenu(boolean needAnimation) {
		if (needAnimation) {
			doSlideMenuAnimation(mSlideMenu.getWidth(), 0, SLIDE_ANIMATION_DURATION);
		} else {
			mSlideMenu.setVisible(false);
			sIsSlideMenuShow = false;
			mSlideAnimation.setValue(0);
			mSlideAnimation.setDstValue(0);
			mGridViewContainer.resetAlpha();
		}
	}

	public void doSlideMenuAnimation(float startValue, float dstValue, long duration) {
		mSlideAnimation.start(startValue, dstValue, duration);
		mGridViewContainer.startSlideAnimation(mSlideAnimation);
		mSlideMenu.startSlideAnimation(mSlideAnimation);
	}

	public void doSlideMenuAnimation(float dstValue, long duration) {
		mSlideAnimation.start(dstValue, duration);
		mGridViewContainer.startSlideAnimation(mSlideAnimation);
		mSlideMenu.startSlideAnimation(mSlideAnimation);
	}


	@Override
	public void onSlideMenuShowStart() {
		if (!mGridViewContainer.isBlur()) {
			mGridViewContainer.enableBlur(GaussianBlurEffectUtils.sBlurRadius,
					GaussianBlurEffectUtils.sBlurTotalSteps,
					GaussianBlurEffectUtils.sBlurStepsPerFrame, GaussianBlurEffectUtils.sPrecision);
			mGridViewContainer.setBlurAlphaProportion(0.0f);
		}
		ShellAdmin.sShellManager.getShell().show(IViewId.PROTECTED_LAYER, false);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_ALL_APP_TOP_ACTION_BAR,
				IAppDrawerMsgId.APPDRAWER_SHOW_SIDEBAR_GUIDE_CLOUD, 0);
	}
	@Override
	public void onSlideMenuShowEnd() {
		ShellAdmin.sShellManager.getShell().hide(IViewId.PROTECTED_LAYER, false);
	}

	@Override
	public void onSlideMenuHideStart() {
		ShellAdmin.sShellManager.getShell().show(IViewId.PROTECTED_LAYER, false);
	}

	@Override
	public void onSlideMenuHideEnd() {
		mSlideMenu.setVisible(false);
		sIsSlideMenuShow = false;
		ShellAdmin.sShellManager.getShell().hide(IViewId.PROTECTED_LAYER, false);
		mGridViewContainer.disableBlur();
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_ALL_APP_TOP_ACTION_BAR,
				IAppDrawerMsgId.APPDRAWER_SHOW_SIDEBAR_GUIDE_CLOUD, 1);
	}

	@Override
	public void onSlideMenuShowPersent(float persent) {
		mTopBarContainer.setSidebarShowPersent(persent);
		mGridViewContainer.setSidebarShowPersent(persent);
	}

	@Override
	public void onGridContainerTouchMove(float offsetX) {
		if (mSlideMenu.getRight() >= 0 && mSlideMenu.getRight() <= mSlideMenu.getWidth()) {
			//以下这种两情况下不能再移动了,只能给予一定的offset补偿
			if (mSlideMenu.getRight() + offsetX >= mSlideMenu.getWidth()) {
				offsetX = mSlideMenu.getWidth() - mSlideMenu.getRight();
				if (offsetX == 0) {
					return;
				}
			} else if (mSlideMenu.getRight() + offsetX <= 0) {
				offsetX = 0 - mSlideMenu.getRight();
				if (offsetX == 0) {
					return;
				}
			}
			mGridViewContainer.offsetLeftAndRight((int) (offsetX * GLGridViewContainer.VIEW_OFFSET_PERSENT));
			mSlideMenu.offsetLeftAndRight((int) offsetX);
			mSlideAnimation.setValue(mSlideMenu.getRight());
			mSlideAnimation.setDstValue(mSlideMenu.getRight());
//			mMainView.invalidate();
//			mSlideMenu.invalidate();
			invalidate();
		}
	}

	@Override
	public void onGridContainerTouchUp(float touchUpX) {
		int slideMenuWidth = mSlideMenu.getWidth();
		int slideMenuRight = mSlideMenu.getRight();
		float persent = (float) slideMenuRight / slideMenuWidth;
		long duration = (long) (persent * SLIDE_ANIMATION_DURATION);
		boolean forceSlideLeft = false;
		int effectX;
		//计算相应边界值
		if (mIsDownOnLeft) {
			effectX = slideMenuWidth / SLIDE_EFFECT_X_RATIO;
			mIsDownOnLeft = false;
		} else {
			int slop = ViewConfiguration.get(mContext).getScaledTouchSlop();
			int effectedOffsetX = slideMenuRight - slideMenuWidth;
			if (effectedOffsetX <= 0 && Math.abs(effectedOffsetX) <= slop) {
				forceSlideLeft = true;
			}
			effectX = slideMenuWidth / SLIDE_EFFECT_X_RATIO * (SLIDE_EFFECT_X_RATIO - 1);
		}
		//执行滑动动画，滑动方向
		if (slideMenuRight <= effectX || forceSlideLeft) {
			doSlideMenuAnimation(0, duration);
		} else {
			doSlideMenuAnimation(slideMenuWidth, duration);
		}

	}

	@Override
	public void onGridContainerTriggerShowSlideMenuArea() {
		mSlideMenu.setVisible(true);
		if (!mGridViewContainer.isBlur()) {
			mGridViewContainer.enableBlur(GaussianBlurEffectUtils.sBlurRadius,
					GaussianBlurEffectUtils.sBlurTotalSteps,
					GaussianBlurEffectUtils.sBlurStepsPerFrame, GaussianBlurEffectUtils.sPrecision);
			mGridViewContainer.setBlurAlphaProportion(0.0f);
		}
		mIsDownOnLeft = true;
		sIsSlideMenuShow = true;
		mSlideAnimation.setDstValue(1);
	}
	
	public static boolean isSlideMenuShow() {
		return sIsSlideMenuShow;
	}
	
	public void enterFuntionSlot(int slotId, boolean needAnimation, Object...objs) {
		mSlideMenu.enterFuntionSlot(slotId, needAnimation, objs);
	}

}
