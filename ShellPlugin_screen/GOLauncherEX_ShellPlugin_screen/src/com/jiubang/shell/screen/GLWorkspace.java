package com.jiubang.shell.screen;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.Translate3DAnimation;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.graphics.ext.BlurGLDrawable;
import com.go.gl.scroller.FastVelocityTracker;
import com.go.gl.view.GLContentView;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.view.GLView.OnLongClickListener;
import com.go.gl.view.GLViewGroup;
import com.go.gl.view.GLViewParent;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.effector.united.IEffectorIds;
import com.golauncher.message.IAppDrawerMsgId;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IDockMsgId;
import com.golauncher.message.IFolderMsgId;
import com.golauncher.message.IScreenAdvertMsgId;
import com.golauncher.message.IScreenFrameMsgId;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingUtils;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;
import com.jiubang.ggheart.apps.desks.appfunc.model.IBackgroundInfoChangedObserver;
import com.jiubang.ggheart.apps.desks.diy.AppInvoker;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.WallpaperControler;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockLogicControler;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConfig;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditController;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.apps.desks.purchase.FunctionPurchaseManager;
import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.ggheart.components.advert.AdvertConstants;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.GlobalSetConfig;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.FavoriteInfo;
import com.jiubang.ggheart.data.info.GestureSettingInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ScreenAppWidgetInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.data.statistics.IGoLauncherUserBehaviorStatic;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.IconUtilities;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.plugin.SecurityPoxyFactory;
import com.jiubang.ggheart.plugin.UnsupportSecurityPoxyException;
import com.jiubang.ggheart.plugin.common.OrientationTypes;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.IShell;
import com.jiubang.shell.animation.BackgroundAnimation;
import com.jiubang.shell.animation.DragAnimation;
import com.jiubang.shell.animation.DragAnimation.DragAnimationInfo;
import com.jiubang.shell.animation.DropAnimation;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.animation.TranslateValue3DAnimation;
import com.jiubang.shell.animation.TranslateValue3DAnimation.TranslateValue3DAnimationListener;
import com.jiubang.shell.appdrawer.allapp.GLAllAppGridView;
import com.jiubang.shell.appdrawer.controler.IconViewController;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.component.ShellContainer;
import com.jiubang.shell.common.component.TransformationInfo;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.deletezone.GLDeleteZone;
import com.jiubang.shell.dock.GLDock;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.drag.DragController.DragGestureListener;
import com.jiubang.shell.drag.DragController.DragListener;
import com.jiubang.shell.drag.DragInfoTranslater;
import com.jiubang.shell.drag.DragScroller;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.drag.DropTarget;
import com.jiubang.shell.effect.EffectController.EffectListener;
import com.jiubang.shell.effect.IconCircleEffect;
import com.jiubang.shell.folder.BaseFolderIcon;
import com.jiubang.shell.folder.GLAppDrawerFolderGridView;
import com.jiubang.shell.folder.GLAppFolder;
import com.jiubang.shell.folder.GLAppFolderBaseGridView;
import com.jiubang.shell.folder.GLAppFolderMainView.FolderStatusListener;
import com.jiubang.shell.folder.GLAppFolderMainView.FolderViewAnimationListener;
import com.jiubang.shell.folder.GLDockFolderGridVIew;
import com.jiubang.shell.folder.GLDockFolderIcon;
import com.jiubang.shell.folder.GLScreenFolderGridView;
import com.jiubang.shell.folder.GLScreenFolderIcon;
import com.jiubang.shell.gesture.GLGestureHandler;
import com.jiubang.shell.gesture.OnMultiTouchGestureListener;
import com.jiubang.shell.gesture.PointInfo;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.indicator.DesktopIndicator;
import com.jiubang.shell.orientation.GLOrientationControler;
import com.jiubang.shell.popupwindow.component.actionmenu.QuickActionMenuHandler;
import com.jiubang.shell.preview.GLSenseWorkspace;
import com.jiubang.shell.screen.back.GLIWallpaperDrawer;
import com.jiubang.shell.screen.component.GLScreenShortCutIcon;
import com.jiubang.shell.screen.utils.ScreenUtils;
import com.jiubang.shell.scroller.ShellScreenScroller;
import com.jiubang.shell.scroller.ShellScreenScrollerListener;
import com.jiubang.shell.scroller.effector.CoupleScreenEffector;
import com.jiubang.shell.scroller.effector.gridscreen.GridScreenContainer;
import com.jiubang.shell.scroller.effector.subscreen.SubScreenContainer;
import com.jiubang.shell.theme.GLAppDrawerThemeControler;
import com.jiubang.shell.utils.GaussianBlurEffectUtils;
import com.jiubang.shell.utils.IconUtils;
import com.jiubang.shell.utils.ToastUtils;
import com.jiubang.shell.widget.GLWidgetSpace;
import com.jiubang.shell.widget.Go3DWidgetManager;
import com.jiubang.shell.widget.component.GLWidgetContainer;

/**
 * 桌面屏幕层的主容器
 * 
 * @author jiangxuwen
 * 
 */
public class GLWorkspace extends GLWidgetSpace
		implements
			IBackgroundInfoChangedObserver,
			ShellScreenScrollerListener,
			SubScreenContainer,
			GridScreenContainer,
			OnClickListener,
			OnLongClickListener,
			DropTarget,
			DragSource,
			DragScroller,
			DragListener,
			ScreenPointTransListener,
			OnMultiTouchGestureListener,
			TranslateValue3DAnimationListener,
			FolderStatusListener,
			FolderViewAnimationListener,
			BackgroundAnimation, DragGestureListener {
	// --------------------------------------------------------------------//
	private static final int FLAG_INCLUDE_STOPPED_PACKAGES = 0x00000020;
	protected ShellScreenScroller mScroller;
	int mScreenCount = 0;
	// 触屏状态
	public static final int TOUCH_STATE_RESET = 0;
	public static final int TOUCH_STATE_SCROLL = 1;
	// 当前触屏状态
	public int mTouchState = TOUCH_STATE_RESET;
	private float mTouchX;
	private float mTouchY;
	private CoupleScreenEffector mScreenEffector;

	public static final int CHANGE_SOURCE_DOCK = 1; // dock状态改变时布局
	public static final int CHANGE_SOURCE_INDICATOR = 2; // 指示器状态改变时布局
	public static final int CHANGE_SOURCE_STATUSBAR = 3; // 状态栏状态改变时布局

	public final static int DRAW_STATE_ALL = 0; // 正常状态，绘制所有内容
	public final static int DRAW_STATE_ONLY_BACKGROUND = 1; // 被半透明物体遮挡，只绘制背景
	public final static int DRAW_STATE_DISABLE = 2; // 完全隐藏
	public final static int DRAW_STATE_NORMAL_TO_SMALLER_ENTERING = 3; // 进入添加模块动画进行时
	public final static int DRAW_STATE_SMALLER_TO_NORMAL_ENTERING = 4; // 退出添加模块动画进行时
	public final static int DRAW_STATE_ZOOM_OUT = 5; // 缩小动画进行时
	public final static int DRAW_STATE_ZOOM_IN = 6; // 放大动画进行时
	int mDrawState;

	boolean mShowquickmenu = false;
	GLView mLongclickview;
	private int mScrollingDuration = 450;
	private int mScrollingBounce = 40;
	private IShell mShell;

	private boolean mAllowLongPress = true;
//	private boolean mIsDragging = false;
	private boolean mAutoStretch = false;
	private int mDragViewType = -1; // 拖拽的view的类型
	
	private boolean mIsEnterEditedAddBlank = false;
	
	/**
	 * CellInfo for the cell that is currently being dragged
	 */
	private GLCellLayout.CellInfo mDragInfo;

	// 默认屏幕
	private int mMainScreen;

	// 桌面行数和列数
	private int mDesktopRows = 4;
	private int mDesktopColumns = 4;
	// 单屏幕的格子数
	private int mGridCount;

	// 单个格子宽度
	private int mGridWidth;
	// 单个格子高度
	private int mGridHeight;

	// 实际图标的大小
	private int mSubWidth;
	private int mSubHeight;

	// 当前屏和下一屏的单元格的显示状态 point.x :1为bubbletext -2为widget 0为空; point.y :对应的view的索引
	private Point[] mCurGridsState;
	private Point[] mNextGridsState;
	// 要绘制的当前屏（A）和下一屏（B）
	private int mScreenA = -101;
	private int mScreenB = -102;
	private Paint mPaint;
	// 图标特效
	//	private CoupleScreenEffector mDeskScreenEffector;
	//	mCurScreenEffector
	private Rect mDestRect = new Rect(); // 在格子特效（带透明参数）中的裁剪目标（矩形区域）
	private Matrix mMatrix = new Matrix(); // 绘制格子特效（带透明度）时的矩阵
	private boolean mFirstLayout = true;

	private boolean mCycleMode = false; //循环模式
	private IconView mDragTargetIcon; // 拖拽时的目标icon
	// drag状态
	private static final int DRAG_OVER_NONE = 0; // 不在屏幕区域范围内
	private static final int DRAG_OVER_CURR = 1; // 当前屏范围内
	private static final int DRAG_OVER_PREV = 2; // 上一屏范围内
	private static final int DRAG_OVER_NEXT = 3; // 下一屏范围内
	// 拖动的模式
	private int mDragOverMode = DRAG_OVER_NONE;
	// 拖拽时的目标CellLayout，一般设置是当前的拖拽屏
	private GLCellLayout mDragTargetLayout = null;
	private final int[] mTempCell = new int[2];
	private float[] mDragTransInfo = new float[5];
	private DragController mDragController;

	private int mEffectorType;
//	protected int mCurrentScreen;
	private static final int INVALID_SCREEN = -1;
	// 准备切换到的目标屏下标
	private int mNextScreen = INVALID_SCREEN;
	public static float sLayoutScale = 1.0f;
	// workspace的状态
	public static final int STATE_NORMAL = 0; // 默认
	public static final int STATE_SMALL_ONE = 1; // 缩小（编辑界面）第一阶级
	public static final int STATE_SMALL_TWO = 2; // 缩小（编辑界面）第二阶级
	int mScaleState = STATE_NORMAL;
	protected int[] mTargetCell = new int[2]; // 目标网格
	private int mDragOverX = -1;
	private int mDragOverY = -1;
	private int mTopViewId = IViewId.SCREEN;
//	private boolean mNeedHideMenu = true; // 是否需要关闭菜单
//	private boolean mShouldShowQuickMenu = false;
//	private GLView mQuickMenuTargetView;
	private TranslateValue3DAnimation mStateChangeAnimation;

	// workspace 缩放比例
	/**
	 *  竖屏添加app时界面的缩放比例,不能用final。因为显示DOCK栏与隐藏DOCK两种情况下，缩放比例不一样
	 *  通过 getScaleForAddAppPortrait()获得计算后的缩放比例
	 */
	private static final float SCREEN_EDIT_LEVEL_ONE_SCALE = 0.672f;
	private static final float SCALE_EDIT_LEVEL_TWO_SCALE = 0.415f;
	
	public static final int SCREEN_BACK_TO_LAST_LEVEL = -1;
	public static final int SCREEN_TO_LEVEL_NORMAL = 0;
	public static final int SCREEN_TO_SMALL_LEVEL_ONE = 1;
	public static final int SCREEN_TO_SMALL_LEVEL_TWO = 2;

	// 自动播放特效第一次标识
	private boolean mShowAutoEffect1 = false;
	// 自动播放特效第二次标识
	private boolean mShowAutoEffect2 = false;
	// 自动特效的返回屏
	private int mNextDestScreen;
	// 自动特效的展示时间
	private static final int AUTO_EFFECT_TIME = 750;

	// 页间距
	protected static int sPageSpacingX;
	protected static int sPageSpacingY;
	private boolean mHaveChange = false;
	public static final int ANIMDURATION = 450;
	private static final float SMALLONE_DEPTH = -320; // 第一阶段的缩放深度为-320
	private static final float SMALLTWO_DEPTH = -1000; // 第二阶段的缩放深度为-1000
	private static final float SMALLTWO_FOR_WIDGET_DEPTH = -650; // widget第二阶段的缩放深度为-650
	private float mCx;
	private float mCy;
	private long mStartTime = 0;
	protected int mSmallLeveInLandscape = 0;
	protected Object mSmallLeveObj;
	
	private long mTouchDownTime; // 点击下去的当时时间
	private static final int PRESSED_STATE_DURATION = 125; // 单击图标的响应时间差
	private GLView mClickView = null;
	private float mTranslateY;
	private float mTranslateZ;
	private int mLastLevel;
	private int mMinimumFlingVelocity;
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
	// 滑动方向与水平方向夹角小于60度的一律视为滚动屏幕
	public static final float SCROLL_MINDEGREE = 60.0f;
	public static final int HALF_CIRCLE_ANGLE = 180;
	public static final float SCROLL_TAN_MINDEGREE = (float) Math.tan(SCROLL_MINDEGREE
			/ HALF_CIRCLE_ANGLE * 3.1415926);
	private static final int PAGING_TOUCH_SLOP = 16;
	private long mTempTime;
	private boolean mTouchedWidget;
	private boolean mTouchedSystemWidget;
	private boolean mTouchedIcon;
	protected boolean mTouchedIndicator;
	private static final long SNAP_DELAY_TIME = 1500;

	private GLGestureHandler mGestureHandler;
	private boolean mLocked; // 屏幕是否被锁定的标识
	// 标志是否响应touch事件
	private boolean mAllowToScroll = true;
	
	private boolean mCanPerformGestureSwip;

//	private int mDragStartX = -1; // 起始拖动点
//	private int mDragStartY = -1; // 起始拖动点

	/**
	 * 通知widget进入或离开当前屏幕的Runnable
	 */
	private WidgetRunnable mEnterRunnable = new WidgetRunnable(true);
	private WidgetRunnable mLeaveRunnable = new WidgetRunnable(false);

	private boolean mIsWallPaperScrollDelay = false; //设置项：滑屏壁纸是否延迟滚动
	private int mBackgroundOffset; //壁纸当前的偏移值
	private static final float BACKGROUND_MOVE_SCALE = 0.08f; //壁纸和滚动器滑动的比例
	// 通知widget延时，当停留在某个屏幕操过这个时间才通知widget
	public final static int FIRE_WIDGET_DELAY = 1000;

//	private DesktopSettingInfo mDesktopSettingInfo;
	protected boolean mLoadFinish; // 桌面加载是否已经完成的标识
	private boolean mNeedRequestLayout; // 是否需要重新布局的标识
	private boolean mIsAlreadyDrop = false; // 是否已经跑了onDrop方法

	private static final int SCROLL_ZONE_NONE = 0;
	private static final int SCROLL_ZONE_LEFT = 1;
	private static final int SCROLL_ZONE_RIGHT = 2;
	private int mInScrollZone = SCROLL_ZONE_NONE;
	private boolean mWallpaperScrollEnabled; // 壁纸是否可滚的标识
	private float mScaleFactor;
	private MotionEvent mPointerDownEvent;
	/**
	 * 图标是否正在进行飞出屏幕删除动画（此种情况等动画完成后才清除拖拽图标）
	 */
	private boolean mIsFlyToDelete = false;
	
	private FuncAppDataHandler mDataHandler;
	int mNeedSnapToScreenAfterFolderClosed = -1;
	//处理添加界面界面切换时不断点击返回键
	public boolean mExitToLevelFlag = false;
	boolean mTwoFingerSwipeHandled;
	boolean mOnScaleHandled;
	protected boolean mSameLocation = false; // 是否放回原来的位置
	
	private MotionEvent mMotionEventForAnalys; // 从外部传入的触屏事件，原因：加入0屏后，workspace本身会滚动，会影响到原来事件的ev.getX,mVelocityTracker的判断

	private AbsZeroHandler mZeroHandler = null;
	private boolean mDragIntoDock;
	
	public void setZeroHandler(AbsZeroHandler handler) {
		mZeroHandler = handler;
	}

	public AbsZeroHandler getZeroHandler() {
		return mZeroHandler;
	}
	
	public void setShell(IShell mShell) {
		this.mShell = mShell;
		mGestureHandler = new GLGestureHandler(ShellAdmin.sShellManager.getActivity(), mShell);
	}

	public GLWorkspace(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
//		mScroller = new ShellScreenScroller(mContext, this);
		//		setOvershootAmount(mScrollingBounce);
		//		setScrollDuration(mScrollingDuration);
		mMaxDistanceForFolderCreation = 0.55f * IconUtilities.getStandardIconSize(context);
		// 把特效的初始化延后，避免特效初始化的时候格子的高宽还没有赋值
//		initEffector();
		mVelocityTracker = new FastVelocityTracker();
		mSwipTouchSlop = (int) (DrawUtils.sDensity * PAGING_TOUCH_SLOP + 0.5f) * 4;
		ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
		mSwipVelocity = mMinimumFlingVelocity * 4;
		mTouchSlop = (int) (configuration.getScaledTouchSlop());
//		if (!mIsWallPaperScrollDelay) {
//			mScroller.setBackgroundAlwaysDrawn(false);
//		}
		
//		mDesktopSettingInfo = settingControler.getDesktopSettingInfo();
		mPaint = new Paint();
		mDataHandler = FuncAppDataHandler.getInstance(ShellAdmin.sShellManager.getActivity());
		mDataHandler.registerBgInfoChangeObserver(this);
		checkNeedBlurWallPaper();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
		}

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
		}

		// The children are given the same width and height as the workspace
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			GLView view = getChildAt(i);
			if (view != null) {
				view.measure(widthMeasureSpec, heightMeasureSpec);
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (mLoadFinish && ((mScroller != null && !mScroller.isFinished()) || mDrawState != DRAW_STATE_ALL)) {
			mNeedRequestLayout = true;
			return;
		}
		final int cellwidth = DrawUtils.sWidthPixels;
		
		if (!mHaveChange) {
			mHaveChange = true;
			mScroller.setLayoutScale(sLayoutScale);
			if (mLastsLayoutScale == 0) {
				sPageSpacingX = (int) (cellwidth * (1 - sLayoutScale) * sLayoutScale / 2);
			} else {
				sPageSpacingX = (int) (cellwidth * (1 - mLastsLayoutScale) * mLastsLayoutScale / 2);
				mLastsLayoutScale = 0;
				mHaveChange = false;
			}
			
			sPageSpacingY = 0;
			if (sLayoutScale < 1.0f) {
				// Dock可见情况下，top值较大
				final int topValue = ShortCutSettingInfo.sEnable ? 45 : 5; // 由28改成0，处理隐藏DOCK栏进入添加界面，屏幕预览区域和操作区域有点重叠
				sPageSpacingY = (int) (getHeight() * ((topValue + 0.1f) / 800));
			}
			
		}
		mScroller.setScreenSize((int) (cellwidth + sPageSpacingX), getHeight());
		int childLeft = 0;
//		final int width = cellwidth;
		final int height = b - t;
		final int childTop = sPageSpacingY;
		final int gap = sPageSpacingX;

		int realWitdh = cellwidth;
		int realHeight = height;

		if (mScaleState != STATE_NORMAL) {
			// realHeight *= sLayoutScale;
			// realWitdh *= sLayoutScale;
			realWitdh += gap; //如果是添加界面。需要加上每个屏幕的间隔。整个workspace的宽度会比原来扩大
		}

		//reset and update
		mGridWidth = 0;
		mGridHeight = 0;
		getCellWidth();
		getCellHeight();


		final int count = getChildCount();
		// 横向平铺GLCellLayout
		for (int i = 0; i < count; i++) {
			GLView child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				child.layout(childLeft, 0, childLeft + cellwidth, height);
				childLeft += realWitdh;
			}
		}
		
		if (!mZeroHandler.isInZeroScreen()) {
			initScrollData();
		}
		
		if (mFirstLayout) {
			mFirstLayout = false;
			mScreenA = getCurrentScreen();
			updateGridCellSize();
		}
		
		if (mScreenEffector != null) {
			mScreenEffector.notifyRegetScreenRect();
		}
	}

	@SuppressLint("WrongCall")
	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		//		Log.v("Shell", " workspace dispatchDraw ");
		switch (mDrawState) {
			case DRAW_STATE_ALL :
				drawAll(canvas);
				break;

			case DRAW_STATE_ONLY_BACKGROUND :
				mScroller.drawBackground(canvas, mScroller.getScroll());
				// if (mScaleState == STATE_SMALL) {
				// canvas.drawColor(mBackgroudColor);
				// }
				break;

			default :
				break;
		}
	}

	/**
	 * 绘制全部
	 * 
	 * @param canvas
	 */
	@SuppressLint("WrongCall")
	private void drawAll(GLCanvas canvas) {
//		mScroller.invalidateScroll();
		// 如果是延迟绘制，由workspace自行绘制壁纸；
		// 如果不是，则滚动过程，由特效调用绘制壁纸，静止时，由workspace绘制
		if (mIsWallPaperScrollDelay) {
			// 记得绘制完背景之后把标志设为true
			mScroller.setBackgroundAlwaysDrawn(false);
			mScroller.drawBackground(canvas, mBackgroundOffset);
			mScroller.setBackgroundAlwaysDrawn(true);
		}

		if (!mScroller.isFinished()) {
//			mScroller.onDraw(canvas);
		} else {
			if (!mIsWallPaperScrollDelay) {
				mScroller.drawBackground(canvas, mScroller.getScroll());
			}
			int index = mCurrentScreen;
			GLCellLayout child = (GLCellLayout) getChildAt(index);
			if (child != null) {
				
				int save = canvas.save();
				canvas.translate(child.getLeft(), child.getTop());
				child.drawBackground(canvas);
				canvas.restoreToCount(save);
				drawChild(canvas, getChildAt(index), getDrawingTime());
			}
			if (mScaleState != STATE_NORMAL) {
				if ((index - 1) >= 0) {
					child = (GLCellLayout) getChildAt(index - 1);
					if (child != null) {
						int save = canvas.save();
						canvas.translate(child.getLeft(), child.getTop());
						child.drawBackground(canvas);
						canvas.restoreToCount(save);
						drawChild(canvas, child, getDrawingTime());
					}
				}
				if ((index + 1) < getChildCount()) {
					child = (GLCellLayout) getChildAt(index + 1);
					if (child != null) {
						int save = canvas.save();
						canvas.translate(child.getLeft(), child.getTop());
						child.drawBackground(canvas);
						canvas.restoreToCount(save);
						drawChild(canvas, child, getDrawingTime());
					}
				}
			}
		}

		if (mIsWallPaperScrollDelay) {
			// 判断壁纸是否还需要做动画
			final int scroll = mScroller.getScroll();
			if (mBackgroundOffset != scroll) {
				final int totalWidth = mScroller.getScreenSize() * (mScreenCount - 1);
				final int halfScreenWidth = (int) (mScroller.getScreenSize() * 0.5);
				if (mBackgroundOffset <= halfScreenWidth && scroll >= totalWidth - halfScreenWidth) {
					mBackgroundOffset += totalWidth + mScroller.getScreenSize();
				} else if (mBackgroundOffset >= totalWidth - halfScreenWidth
						&& scroll <= halfScreenWidth) {
					mBackgroundOffset -= totalWidth + mScroller.getScreenSize();
				}
				//　move是壁纸的移动距离，小于1则直接+-1,不直接赋值（不然最后会小跳），防止因精度问题导致一直invalidate
				final int move = Math.round((scroll - mBackgroundOffset) * BACKGROUND_MOVE_SCALE);
				mBackgroundOffset += scroll > mBackgroundOffset ? Math.max(1, move) : Math.min(-1,
						move);
				invalidate();
			}
		}
	}

	protected void initEffector() {
//		mScreenEffector = new CoupleScreenEffector(mScroller, CoupleScreenEffector.PLACE_DESK);
//		mScreenEffector.setVerticalSlide(false);
//		// mScroller.setDepthEnabled(true);
//		mScroller.setDuration(350);
//		//		mScroller.setDuration(getDuration(50));
//		mScroller.setMaxOvershootPercent(0);
//		//		mScroller.setInterpolator(InterpolatorFactory.getInterpolator(InterpolatorFactory.QUARTIC));
//		mScroller.setBackgroundAlwaysDrawn(true);
////		mScroller.setEffector(mCurScreenEffector);
	}

	public void setEffector(int type) {
		if (mScreenEffector == null) {
			initEffector();
		}
//		if (mEffectorType == type && type != IEffectorIds.EFFECTOR_TYPE_RANDOM_CUSTOM) {
//			return;
//		}
		mEffectorType = type;
		mScreenEffector.setType(type);
	}
	
	public void restorePrimeEffector() {
		setEffector(mPreviousEffectType);
	}

	public void setCustomRandomEffectorEffects(int[] effects) {
		mScreenEffector.setDeskCustomRandomEffects(effects);
	}

	public void initScrollData() {
		setCurrentScreen(mCurrentScreen);
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
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStart() {
		// 通知widget不刷新
		notifyWidgetVisible(false);
		setAllowLongPress(false);
		setEnableWidgetDrawingCache(true);
	}

	@Override
	public void onFlingStart() {
		//		setGLViewWrapperDeferredInvalidate(true);
	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		// TODO Auto-generated method stub
		updateWallpaperOffset(newScroll);
		prepareSubView();
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		// TODO Auto-generated method stub
		// 更新指示器
		mCurrentScreen = mZeroHandler.onScreenChanged(newScreen);
		
//		if (((GLSuperWorkspace) getGLParent()).isShowingZero() && newScreen > 0) {
//			mCurrentScreen = newScreen - 1;
//		} else {
//			mCurrentScreen = newScreen;
//		}
		
		updateDotsIndicator(newScreen);
		prepareSubView();
		checkGridState(newScreen, oldScreen);
		GLCellLayout oldCellLayout = getScreenView(oldScreen);
		if (oldCellLayout != null) {
			oldCellLayout.clearVisualizeDropLocation();
		}
	}

	// 通知widget 进入/离开屏幕
	void notifyWidgetVisible(boolean visible) {
		// Log.i("luoph", visible ? "OnEnter": "onLeave");
		GLViewParent parent = getGLParent().getGLParent();
		if (parent != null && parent instanceof GLScreen) {
			int msgid = visible
					? IScreenFrameMsgId.SCREEN_FIRE_WIDGET_ONENTER
					: IScreenFrameMsgId.SCREEN_FIRE_WIDGET_ONLEAVE;
			((GLScreen) parent).handleMessage(this, msgid, getCurrentScreen());
		}
	}

	/**
	 * 获取通知widget的runnable
	 * 
	 * @param visible
	 * @return
	 */
	public WidgetRunnable getWidgetRunnable(boolean visible) {
		return visible ? mEnterRunnable : mLeaveRunnable;
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		//		setGLViewWrapperDeferredInvalidate(false);
		//通知widget刷新
		notifyWidgetVisible(true);
		if (mDragController.isKeepDragging()) {
			mDragController.releaseDragging(null);
		}
//		if (mScaleState == STATE_NORMAL) {
			setEnableWidgetDrawingCache(false);
//		}
		if (mShowAutoEffect1) {
			mShowAutoEffect1 = false;
			mShowAutoEffect2 = true;
			snapToScreen(mNextDestScreen, false, AUTO_EFFECT_TIME);
		} else if (mShowAutoEffect2) {
			mShowAutoEffect2 = false;
			if (mPreviousEffectType != Integer.MAX_VALUE) {
				restorePrimeEffector();
				mPreviousEffectType = Integer.MAX_VALUE;
			}
			if (mPrimeEffectEntranceId != -1) {
				DeskSettingUtils.showPayDialog(ShellAdmin.sShellManager.getActivity(),
						mPrimeEffectEntranceId);
				mPrimeEffectEntranceId = -1;
			}
		} else {
			if (mNeedRequestLayout) {
				requestLayout();
				mNeedRequestLayout = false;
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mScaleState != STATE_NORMAL && w == DrawUtils.sWidthPixels) {
			w += sPageSpacingX;
		}
		mScroller.setScreenSize(w, h);
	}

	@Override
	public void computeScroll() {
		if (!mScroller.computeScrollOffset()) {
			if (mNextScreen != INVALID_SCREEN) {
				mNextScreen = INVALID_SCREEN;
			}
		}
	}

	/**
	 * 
	 * @param screen
	 * @param noElastic
	 *            是否使用弹性效果
	 * @param duration
	 *            小于0则自动计算时间
	 */
	public void snapToScreen(int screen, boolean noElastic, int duration) {
		// buildChildrenDrawingCache();
		mNextScreen = screen;
		screen = mZeroHandler.snapToScreen(screen);
		//		if (((GLSuperWorkspace) getGLParent()).isShowingZero()) {
//			screen++;
//		}
		mScroller.gotoScreen(screen, duration, noElastic);
		prepareSubView();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
//		if (mLocked) {
//			return true;
//		}
		//从外部传入的触屏事件
		if (mMotionEventForAnalys != null) {
			ev = mMotionEventForAnalys;
		}
		
		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		final int slop = mTouchSlop;
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				mSwipDy = 0;
				mCanPerformGestureSwip = false;
				mTwoFingerSwipeHandled = false;
				mOnScaleHandled = false;
				setAllowLongPress(true);
				mClickView = null;
				mTouchState = mScroller.isFinished() ? TOUCH_STATE_RESET : TOUCH_STATE_SCROLL;
				mTouchX = ev.getX();
				mTouchY = ev.getY();
				int[] cellXY = new int[2];
				GLCellLayout layout = getCurrentScreenView();
				if (mScaleState != STATE_NORMAL) {
					mTouchDownTime = SystemClock.uptimeMillis();
					float[] real = new float[2];
					virtualPointToReal(mTouchX, mTouchY, real);
					if (layout != null) {
						if (GLCellLayout.pointToCellExact((int) real[0], (int) real[1], cellXY)) {
							GLView child = layout.getChildViewByCell(cellXY);
							if (child != null) {
								mClickView = child;
							} else {
								mClickView = layout;
							} // end else
						}
					} // end if layout
				}
				mInterceptTouchDownX = mTouchX;
				mInterceptTouchDownY = mTouchY;
				mInterceptTouchMoveX = 0;
				mInterceptTouchMoveY = 0;
				mInterceptTouchMoved = false;
				mTouchedWidget = false;
				mTouchedSystemWidget = false;
				mTouchedIcon = false;
				touchChildren((int) mTouchX, (int) mTouchY);
				if (!isLock()) {
					mScroller.onTouchEvent(ev, MotionEvent.ACTION_DOWN);
				}
				break;
			case MotionEvent.ACTION_MOVE :
				if (mTouchState != TOUCH_STATE_SCROLL) {
					mVelocityTracker.addMovement(ev);
					mVelocityTracker.computeCurrentVelocity(1000);
					mInterceptTouchVY = mVelocityTracker.getYVelocity();
					if (!mInterceptTouchMoved) {
						// 一旦超出拖动范围不会再更新，作为初始的拖动斜率
						mInterceptTouchMoveX = Math.abs(ev.getX() - mInterceptTouchDownX);
						mInterceptTouchMoveY = Math.abs(ev.getY() - mInterceptTouchDownY);
						mInterceptTouchMoved = mInterceptTouchMoveX > slop
								|| mInterceptTouchMoveY > slop;
					}
					if (mInterceptTouchMoved) {
						if (mInterceptTouchMoveY <= mInterceptTouchMoveX * SCROLL_TAN_MINDEGREE) {
							// 横向滑动
							mTouchState = TOUCH_STATE_SCROLL;
							if (!isLock()) {
								mScroller.onTouchEvent(ev, action);
							}
						} else {
							mCanPerformGestureSwip = checkIfCanPerformGestureSwip();
						}
					}
				}
				break;
			case MotionEvent.ACTION_UP :				
//				IconView.resetIconPressState();
				
				/*
				 * if (mShowquickmenu) { // 显示气泡框 MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				 * this, IScreenFrameMsgId.SCREEN_LONG_CLICK, 0, mLongclickview); mShowquickmenu = false;
				 * mLayoutParams = null; }
				 */
				if (mScaleState != STATE_NORMAL) {
					if (mClickView != null && mClickView instanceof GLCellLayout) {
						if (((GLCellLayout) mClickView).mState == GLCellLayout.STATE_NORMAL_CONTENT) {
							exitEditState();
						}
					} else if (mClickView != null
							&& (SystemClock.uptimeMillis() - mTouchDownTime) <= PRESSED_STATE_DURATION) {
						// 点击屏幕任何位置均响应退出编辑操作，去掉点击图标或小部件无响应的交互形式
						exitEditState();
					}
				}
			case MotionEvent.ACTION_CANCEL :
				// Log.w("Test", "onInterceptTouch mIsDragging: " + mDragController.isDragging());
				boolean shortFling = false; // 在阈值距离之内发生的急促甩动
				if (!isLock() && mTouchState == TOUCH_STATE_RESET && !mInterceptTouchMoved
						&& !mDragController.isDragging() && !mCanPerformGestureSwip) {
					//					mScroller.onTouchEvent(ev, action);
					shortFling = mScroller.onTouchEvent(ev, action)
							|| Math.abs(mScroller.getFlingVelocityY()) > mSwipVelocity;
				}
				
				final GLCellLayout currentScreen = getCurrentScreenView();
				if (currentScreen != null && !currentScreen.lastDownOnOccupiedCell()) {
					getLocationOnScreen(mTempCell);
					// Send a tap to the wallpaper if the last down was on
					// empty space
				  Bundle bundle = WallpaperControler.createWallpaperCommandBundle((int) ev.getX(), (int) ev.getY());

				  MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						ICommonMsgId.SEND_WALLPAPER_COMMAND, -1, bundle, null);
				  bundle = null;
				}
				
				mTouchState = TOUCH_STATE_RESET;
				mClickView = null;
//				mSwipDy = 0;
				mScaleFactor = 1.0f;
//				mCanPerformGestureSwip = false;
				if (shortFling) {
					return true;
				}
				break;

			default :
				break;
		}
		return mTouchState != TOUCH_STATE_RESET;
	}

	private boolean checkIfCanPerformGestureSwip() {
		return !mTouchedWidget || mTouchedSystemWidget || mTouchedIcon;
	}

	public void touchChildren(int x, int y) {
		final GLCellLayout currentScreen = getCurrentScreenView();
		if (currentScreen == null) {
			return;
		}

		int count = currentScreen.getChildCount();
		Rect rect = new Rect();
		for (int i = 0; i < count; i++) {
			GLView child = currentScreen.getChildAt(i);
			if (child == null) {
				continue;
			}
			child.getHitRect(rect);
			if (rect.contains(x, y)) {
				if (child instanceof GLWidgetContainer) {
					mTouchedWidget = true;
					Object tag = child.getTag();
					if (tag instanceof FavoriteInfo) {
						mTouchedSystemWidget = true;
					} else if (tag instanceof ScreenAppWidgetInfo) {
						ScreenAppWidgetInfo widgetInfo = (ScreenAppWidgetInfo) tag;
						if (widgetInfo.isSystemWidget()) {
							mTouchedSystemWidget = true;
						}
					}
				} else if (child instanceof IconView<?>) {
					mTouchedIcon = true;
				}
				break;
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		//添加文件夹,禁止滑动
//		if (!mAllowToTouch) {
//			return true;
//		}
		
//		if (mLocked) {
//			/*	MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_EDIT, this,
//						IScreenFrameMsgId.SCREEN_EDIT_CHANGE_NORMAL, 0, null);*/
//			exitEditState();
//			return true;
//		}
		//从外部传入的触屏事件
		if (mMotionEventForAnalys != null) {
			event = mMotionEventForAnalys;
		}
		
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				if (!isLock()) {
					mScroller.onTouchEvent(event, action);
				}
				break;

			case MotionEvent.ACTION_MOVE :
				if (!isLock()) {
					mScroller.onTouchEvent(event, action);
				}
				break;

			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL :				
//				IconView.resetIconPressState();
				// Log.w("Test", "onTouch mIsDragging: " + mDragController.isDragging());
				if (!mDragController.isDragging() && !mCanPerformGestureSwip && !isLock()) {
					mScroller.onTouchEvent(event, action);
				}
				mTouchState = TOUCH_STATE_RESET;
//				mSwipDy = 0;
				mScaleFactor = 1.0f;
//				mCanPerformGestureSwip = false;
				break;

			default :
				break;
		}
		return true;
	}

	// ----------------------------- End
	// ScreenScrollerListener---------------------------------------//

	@Override
	public void drawScreen(GLCanvas canvas, int screen) {
//		if (mScroller.getCurrentDepth() != 0) {
//			mScroller.setDepthEnabled(false);
//		}
		GLCellLayout cell = (GLCellLayout) getChildAt(screen);
		if (cell != null) {
//			if (!mScroller.isFinished()) {
				// cell.closeWidgetHardwareAccelerated();
				// cell.buildChildrenDrawingCache();
			cell.drawBackground(canvas);
			cell.draw(canvas);
				
//			}
		}
	}

	@Override
	public void drawScreen(GLCanvas canvas, int screen, int alpha) {
//		if (mScroller.getCurrentDepth() != 0) {
//			mScroller.setDepthEnabled(false);
//		}
		GLCellLayout cell = (GLCellLayout) getChildAt(screen);
		if (cell != null) {
			if (!mScroller.isFinished()) {
				final int oldAlpha = canvas.getAlpha();
				canvas.setAlpha(alpha);
				cell.drawBackground(canvas);
				cell.draw(canvas);
				canvas.setAlpha(oldAlpha);
			}
		}
	}

	/**
	 * 获取当前显示的屏幕id
	 * 
	 * @return 当前屏幕.
	 */
	public int getCurrentScreen() {
		mCurrentScreen = mZeroHandler.getCureentScreen();
		return mCurrentScreen;

//		if (((GLSuperWorkspace) getGLParent()).isShowingZero()) {
//			if (mScroller.getDstScreen() > 0) {
//				return mCurrentScreen = mScroller.getDstScreen() - 1;
//			} else {
//				return mCurrentScreen;
//			}
//		} else {
//			return mCurrentScreen = mScroller.getDstScreen();
//		}
	}

	public void setDesktopRowAndCol(int row, int col) {
		if (row >= 0 && col >= 0) {
			mDesktopRows = row;
			mDesktopColumns = col;
			mGridCount = row * col;
			GLCellLayout.setRows(row);
			GLCellLayout.setColums(col);

			final int count = getChildCount();
			for (int i = 0; i < count; i++) {
				GLCellLayout layout = (GLCellLayout) getChildAt(i);
				if (layout != null) {
					layout.requestLayout();
				}
			}
			requestLayout();
		}
	}

	/**
	 * 设置图标自动拉伸
	 * 
	 * @param autoStretch
	 *            true自动拉伸
	 */
	public void setAutoStretch(boolean autoStretch) {
		if (mAutoStretch != autoStretch) {
			mAutoStretch = autoStretch;
			GLCellLayout.setAutoStretch(mAutoStretch);
			GLCellLayout child = null;
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				child = (GLCellLayout) getChildAt(i);
				child.requestLayout();
			}
		}
	}

	public void setmAutoStretch(boolean mAutoStretch) {
		this.mAutoStretch = mAutoStretch;
	}

	public boolean getmAutoStretch() {
		return mAutoStretch;
	}
	/**
	 * 设置当前屏幕
	 * 
	 * @param currentScreen
	 *            当前屏幕id.
	 */
	public void setCurrentScreen(int curScreen) {
		
		mZeroHandler.setCureentScreen(curScreen);
		
//		mScreenCount = getChildCount();
//		mCurrentScreen = curScreen;
//		if (mCurrentScreen >= mScreenCount) {
//			mCurrentScreen = mScreenCount - 1;
//		}
//		
//		if (((GLSuperWorkspace) getGLParent()).isShowingZero()) {
//			// 先设置一次总数，再赋值mCurrentScreen = curScreen;
//			// 否则会出现刚进入预览的index大于删除了屏幕之后的总数
//			// Screencroll检测index是否大于总数，是则回调onScreenChange又把mCurrentScreen赋值为0
//			if (mScreenCount != mScroller.getScreenCount() - 1) {
//				mScroller.setScreenCount(mScreenCount + 1);
//			}
//			mScroller.setCurrentScreen(mCurrentScreen + 1);
//		} else {
//			// 先设置一次总数，再赋值mCurrentScreen = curScreen;
//			// 否则会出现刚进入预览的index大于删除了屏幕之后的总数
//			// Screencroll检测index是否大于总数，是则回调onScreenChange又把mCurrentScreen赋值为0
//			if (mScreenCount != mScroller.getScreenCount()) {
//				mScroller.setScreenCount(mScreenCount);
//			}
//			mScroller.setCurrentScreen(mCurrentScreen);
//		}
		
	}
	public void setCurrentScreenForMoveScreen(int curScreen) {
		mScreenCount = getChildCount();
		// 先设置一次总数，再赋值mCurrentScreen = curScreen;
		// 否则会出现刚进入预览的index大于删除了屏幕之后的总数
		// Screencroll检测index是否大于总数，是则回调onScreenChange又把mCurrentScreen赋值为0
//		if (((GLSuperWorkspace) getGLParent()).isShowingZero()) {
//			if (mScreenCount != mScroller.getScreenCount()) {
//				mScroller.setScreenCount(mScreenCount);
//			}
//		} else {
//			if (mScreenCount != mScroller.getScreenCount() - 1) {
//				mScroller.setScreenCount(mScreenCount + 1);
//			}
//		}
		mZeroHandler.setCurrentScreenForMoveScreen();
		mCurrentScreen = curScreen;
		if (mCurrentScreen >= mScreenCount) {
			mCurrentScreen = mScreenCount - 1;
		}
	}
	/**
	 * 添加组件到指定屏幕
	 * 
	 * @param child
	 *            组件
	 * @param screen
	 *            屏幕索引
	 * @param x
	 *            组件所在列
	 * @param y
	 *            组件所在行
	 * @param spanX
	 *            组件宽度
	 * @param spanY
	 *            组件高度
	 */
	void addInScreen(GLView child, int screen, int x, int y, int spanX, int spanY, boolean insert) {
		if (child == null || screen < 0 || screen >= getChildCount()) {
			if (child == null) {
				// ScreenMissIconBugUtil.showToast(ScreenMissIconBugUtil.ERROR_ADDINSCREEN_CHILD_NULL);
			} else if (screen < 0) {
				// ScreenMissIconBugUtil
				// .showToast(ScreenMissIconBugUtil.ERROR_ADDINSCREEN_SCREEN_SMALLER_0);
			} else if (screen >= getChildCount()) {
				// ScreenMissIconBugUtil
				// .showToast(ScreenMissIconBugUtil.ERROR_ADDINSCREEN_SCREEN_BIGGER_0);
			}
			return;
		}

		GLCellLayout group = (GLCellLayout) getChildAt(screen);
		blankCellToNormal(group, true);
		// child.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT));
		ViewGroup.LayoutParams lp = child.getLayoutParams();
		if (lp == null || !(lp instanceof GLCellLayout.LayoutParams)) {
			lp = new GLCellLayout.LayoutParams(x, y, spanX, spanY);
		} else {
			((GLCellLayout.LayoutParams) lp).cellX = x;
			((GLCellLayout.LayoutParams) lp).cellY = y;
			((GLCellLayout.LayoutParams) lp).cellHSpan = spanX;
			((GLCellLayout.LayoutParams) lp).cellVSpan = spanY;
		}
		child.setLayoutParams(lp);
		group.addView(child, insert ? 0 : -1, lp);
		// group.addView(child);
		child.setOnLongClickListener(this);
		child.setOnClickListener(this);
		refreshSubView();
		mScroller.invalidateScroll();
	}

	/**
	 * 添加组件到指定屏幕
	 * 
	 * @param child
	 *            组件
	 * @param screen
	 *            屏幕索引
	 * @param x
	 *            组件所在列
	 * @param y
	 *            组件所在行
	 * @param spanX
	 *            组件宽度
	 * @param spanY
	 *            组件高度
	 */
	void addInScreen(GLView child, int screen, int x, int y, int spanX, int spanY) {
		addInScreen(child, screen, x, y, spanX, spanY, false);
	}

	void addInCurrentScreen(GLView child, int x, int y, int spanX, int spanY) {
		addInScreen(child, mCurrentScreen, x, y, spanX, spanY, false);
	}

	@Override
	public void onClick(final GLView v) {
		
		// requestLayout();
		EffectListener animationListener = new EffectListener() {
			
			@Override
			public void onEffectComplete(Object callBackFlag) {
				
				GLView v = (GLView) callBackFlag;
				if (v.getTag() instanceof ShortCutInfo) {
					ShortCutInfo shortcut = (ShortCutInfo) v.getTag();
					Intent intent = DockUtil.filterDockBrowserIntent(
							ShellAdmin.sShellManager.getActivity(), shortcut.mItemType, shortcut.mIntent);
					Rect rect = new Rect();
					v.getGlobalVisibleRect(rect);

					ArrayList<Object> posArrayList = new ArrayList<Object>();

					posArrayList.add(rect);
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							ICommonMsgId.START_ACTIVITY, AppInvoker.TYPE_SCREEN, intent, posArrayList);
					if (intent != null) {
						final String action = intent.getAction();
						if (action != null && action.equals(ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE)) {
							StatisticsData.countStatData(ShellAdmin.sShellManager.getActivity(), StatisticsData.ENTRY_KEY_GOFOLDER);
		//					GoStoreStatisticsUtil.setCurrentEntry(
		//							GoStoreStatisticsUtil.ENTRY_TYPE_FUNTAB_ICON, mActivity);
							AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(ShellAdmin.sShellManager.getActivity(), 
									AppRecommendedStatisticsUtil.ENTRY_TYPE_APPFUNC_ICO_GOSTORE);
						}
					}
					posArrayList.clear();
					posArrayList = null;
//					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IScreenFrameMsgId.START_ACTIVITY,
//							-1, intent, null);
				}
			}

			@Override
			public void onEffectStart(Object callBackFlag) {
			}
		};

		if (v instanceof GLScreenFolderIcon) {
			
			((BaseFolderIcon<?>) v).startClickEffect(new EffectListener() {
				
				@Override
				public void onEffectStart(Object object) {
				}
				
				@Override
				public void onEffectComplete(Object object) {
					((BaseFolderIcon<?>) v).openFolder();
				}
			}, IconCircleEffect.ANIM_DURATION_CLICK, false);
			
		} else if (v.getTag() instanceof FavoriteInfo) {
			
			FavoriteInfo info = (FavoriteInfo) v.getTag();
			if (info.mWidgetInfo != null) {
				final GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
				String packageName = widgetManager.getWidgetPackage(info.mWidgetInfo);
				if (packageName != null) {
					// modified by liulixia
					// 抽出业务层逻辑，独立开cn包和主包代码，原因是cn包要对一些定制的widgt做特殊的处理
					if (packageName.equals(PackageName.TASK_PACKAGE)) {
						packageName = PackageName.RECOMMAND_GOTASKMANAGER_PACKAGE;
					} else if (packageName
							.equals(PackageName.RECOMMAND_TTDONDTING_PACKAGE)) {
						StatisticsData.updateAppClickData(
								ApplicationProxy.getContext(),
								info.mWidgetInfo.mPackage,
								AdvertConstants.ADVERT_STATISTICS_TYPE,
								info.mMapId, info.mAId);
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_ADVERT_BUSINESS,
								IScreenAdvertMsgId.REQUEST_ADVERT_STAT_CLICK_ACTION, -1,
								info.mWidgetInfo.mPackage, info.mAId, info.mClickUrl, info.mMapId);
						PreferencesManager sp = new PreferencesManager(
								ApplicationProxy.getContext(),
								IPreferencesIds.TTDONGTING_WIDGET_ADD,
								Context.MODE_PRIVATE);
						boolean addTTDtongTing = sp
								.getBoolean(
										IPreferencesIds.TTDONGTING_WIDGET_IS_ADD,
										false);
						if (GoAppUtils.isAppExist(
										mContext,
										PackageName.RECOMMAND_TTDONDTING_PACKAGE)
								&& addTTDtongTing) {
							pickAppWidget();
							return;
						}
					}
					
					int code = GoAppUtils.widgetOperate(info, packageName);
					if (code == GoAppUtils.WIDGET_CLICK_RETURN) {
						return;
					} else if (code == GoAppUtils.WIDGET_CLICK_PICK_APP_WIDGET) {
						pickAppWidget();
						return;
					}
					
					final Activity activity = ShellAdmin.sShellManager.getActivity();
					// (旧版本)推荐widget下载 通过电子市场 或者通过浏览器下载
					String linkArray[] = { packageName, info.mUrl };
					String title = packageName; // 默认使用包名做为文件名
					if (info.mTitleId > 0) {
						// 因为资源是放在主包，所以要用主包的context
						title = ShellAdmin.sShellManager.getActivity().getResources()
								.getString(info.mTitleId);
					}

					boolean isCnUser = GoAppUtils.isCnUser(activity);
					//						String content = mActivity.getString(R.string.fav_content);
					String detail = getContext().getResources().getString(R.string.fav_app);
					if (info.mDetailId > 0) {
						// 因为资源是放在主包，所以要用主包的context
						detail = ShellAdmin.sShellManager.getActivity().getResources()
								.getString(info.mDetailId);
					}
					
					// 实时统计
					if (packageName != null && !packageName.equals("") && info.mAId != null
							&& info.mMapId != null && info.mClickUrl != null) {
						StatisticsData.updateAppClickData(activity, packageName,
								AdvertConstants.ADVERT_STATISTICS_GOWIDGET_TYPE, info.mMapId,
								info.mAId);
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_ADVERT_BUSINESS,
								IScreenAdvertMsgId.REQUEST_ADVERT_STAT_CLICK_ACTION, -1,
								packageName, info.mAId, info.mClickUrl, info.mMapId);
					}

					// 扩展GA字段，支持不同的GA链接。add by Ryan 2013.05.27
					String gaLink = LauncherEnv.GOLAUNCHER_FORWIDGET_GOOGLE_REFERRAL_LINK;
					if (info.mGALink != null) {
						gaLink = info.mGALink;
					}
					// add by Ryan 2013.05.27 end

					CheckApplication.downloadAppFromMarketFTPGostore(activity, detail, linkArray,
							gaLink, title, System.currentTimeMillis(), isCnUser,
							CheckApplication.FROM_SCREEN_FAVORITE_WIDGET, 0, null);

					linkArray = null;
					title = null;
					detail = null;
				}
			} 
		} else {
			IconView<?> iconView = (IconView<?>) v;
			iconView.startClickEffect(animationListener, IconCircleEffect.ANIM_DURATION_CLICK,
					false);
		}
	}

	/**
	 * 弹出选择系统小部件的列表
	 */
	private void pickAppWidget() {
		if (SettingProxy.getScreenSettingInfo().mLockScreen) {
			LockScreenHandler.showLockScreenNotification(ShellAdmin.sShellManager.getActivity());
			return;
		}
		// 发消息通知进入桌面编辑状态

		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SCREEN_ENTER_SCREEN_EDIT_LAYOUT, 1, null, null);
		
		// 添加widget
		Bundle bundle = new Bundle();
		bundle.putInt("id", -1);
		// 向屏幕层要求一个ID
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_GET_ALLOCATE_APPWIDGET_ID, -1, bundle);
		
		int allocateAppWidget = bundle.getInt("id");
		if (allocateAppWidget > -1) {
			Intent pickIntent = new Intent(
					AppWidgetManager.ACTION_APPWIDGET_PICK);
			pickIntent.putExtra(Intent.EXTRA_TITLE,
					getContext().getText(R.string.select_widget_app));
			pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					allocateAppWidget);
			//
			ShellAdmin.sShellManager.getActivity().startActivityForResult(pickIntent,
					IRequestCodeIds.REQUEST_PICK_APPWIDGET);
		}
	}
	
	@Override
	public boolean onLongClick(GLView v) {
		// 判断当前是否锁定屏幕编辑
		boolean isLockEditState = SettingProxy.getScreenSettingInfo().mLockScreen;
		if (!(v instanceof GLCellLayout) && isLockEditState) {
			LockScreenHandler.showLockScreenNotification(ShellAdmin.sShellManager.getActivity());
			return true;
		}
		if (mScaleState != STATE_NORMAL) {
			if (mClickView != null) {
				v = mClickView;
			} else {
				return true;
			}
		}
		IconViewController.getInstance().removeIconNewFlag(v);
		if (!(v instanceof GLCellLayout)) {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.SCREEN_LONG_CLICK, 0, v);
			return true;
		}
		// mShowquickmenu = true;
		// mLongclickview = v;
		if (mScaleState == STATE_NORMAL) {
			// 长按桌面改为显示菜单
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SHOW_GLGGMENU, -1);
//			normalToSmallPreAction(null, null, null, GLWorkspace.SCREEN_TO_SMALL_LEVEL_ONE);
//			updateDotsIndicator(mCurrentScreen);
		} else if (((GLCellLayout) v).getState() != GLCellLayout.STATE_BLANK_CONTENT) {
			exitEditState();

			mClickView = null;
		}
		return true;
	}

	// 供外部调用
	/*
	 * public void refreshSubView() { // 如果当前特效不是单元格特效，那就什么也不做 if (mDeskScreenEffector.getmType() <
	 * CoupleScreenEffector.SUBSCREEN_EFFECTOR_COUNT_IN_DESK) { return; } mScreenA =
	 * mScroller.getDrawingScreenA(); final int length = mDesktopRows * mDesktopColumns; if (null ==
	 * mCurGridsState || mCurGridsState.length != length) { mCurGridsState = new Point[length]; }
	 * changeGridState(mCurGridsState, mScreenA);
	 * 
	 * mScreenB = mScroller.getDrawingScreenB(); if (null == mNextGridsState ||
	 * mNextGridsState.length != length) { mNextGridsState = new Point[length]; }
	 * changeGridState(mNextGridsState, mScreenB); }
	 */

	/***
	 * 指示器相关 start
	 */
	public void updateIndicatorItems() {
		Bundle dataBundle = new Bundle();
		
		//判断0屏，设置指示器总数
		int size = 0; 
		size = mZeroHandler.getIndicatorItemsSize();
//		if (((GLSuperWorkspace) getGLParent()).isShowingZero()) {
//			size = getChildCount() + 1;
//		} else {
//			size = getChildCount();
//		}
		dataBundle.putInt(DesktopIndicator.TOTAL, size);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SCREEN_UPDATE_INDICATOR,
						DesktopIndicator.UPDATE_SCREEN_NUM, dataBundle);
		dataBundle = null;
	}

	private synchronized void updateSliderIndicator() {
		final int offset = mScroller.getIndicatorOffset();
		Bundle dataBundle = new Bundle();
		dataBundle.putInt(DesktopIndicator.OFFSET, offset);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_UPDATE_INDICATOR, DesktopIndicator.UPDATE_SLIDER_INDICATOR,
				dataBundle);
		dataBundle = null;
	}

	private void updateDotsIndicator(final int current) {
		Bundle dataBundle = new Bundle();
		dataBundle.putInt(DesktopIndicator.CURRENT, current);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_UPDATE_INDICATOR, DesktopIndicator.UPDATE_DOTS_INDICATOR,
				dataBundle);
		dataBundle = null;
	}

	/**
	 * dock, 指示器， 状态栏发生状态改变时（隐藏或显示）调用
	 * 
	 * @param object
	 * @param state
	 * @param isDrag
	 *            是否因拖动引起的
	 */
	protected void requestLayout(int object, int state) {
		// 是否扩充范围
		final boolean expand = state == 0 ? true : false;
		int topExtra = 0;
		int bottomExtra = 0;
		int rightExtra = 0;
		final boolean isPortrait = DrawUtils.sHeightPixels > DrawUtils.sWidthPixels;
		boolean indicatorInBottom = false;
		boolean showIndicator = true;

		// 指示器是否在下面
		showIndicator = GLScreen.sShowIndicator;
		indicatorInBottom = GLScreen.sIndicatorOnBottom;
		int indicatorHeight = getResources().getDimensionPixelSize(R.dimen.slider_indicator_height);
		switch (object) {
		   // Dock
		   case CHANGE_SOURCE_DOCK:
			final int dockHeight = DockUtil.getBgHeight();
			if (isPortrait) {
				bottomExtra = expand ? -dockHeight : 0;
			} else {
				rightExtra = expand ? -dockHeight : 0;
			}
//			if (screenLayout != null) {
//				screenLayout.setDockVisibleFlag(!expand);
//			}
			GLScreen.sDockVisible = !expand;
			break;

			// 指示器
			case CHANGE_SOURCE_INDICATOR :
				if (!ShortCutSettingInfo.sEnable) {
					if (isPortrait) {
						bottomExtra -= DockUtil.getBgHeight();
					} else {
						rightExtra -= DockUtil.getBgHeight();
					}
				}
				break;

			// 状态栏
			case CHANGE_SOURCE_STATUSBAR :
			   if (mDragController != null && mDragController.isDragging()) {
//				 // 状态栏的处理比较特殊
//				 final int statusBarHeight = StatusBarHandler.getStatusbarHeight();
//				 topExtra = expand ? 0 : statusBarHeight;
				 return; // 正在拖拽情况下，没必要layout，直接退出
			   }
			    // dock是否可见
			   if (!ShortCutSettingInfo.sEnable) {
				 if (isPortrait) {
				 	bottomExtra -= DockUtil.getBgHeight();
				 } else {
				 	rightExtra -= DockUtil.getBgHeight();
				}
			}
				 
				break;

			default :
				break;
		}
		if (indicatorInBottom) {
			topExtra -= indicatorHeight;
			int h = showIndicator ? indicatorHeight / 2 : 0;
			bottomExtra += h;
		} else {
			int h = showIndicator ? 0 : indicatorHeight;
			topExtra -= h;
		}

		startToLayout(topExtra, bottomExtra, rightExtra);
	} // end requestLayout

	private void startToLayout(int topPadding, int bottomPadding, int rightPadding) {
		// 先变当前屏
		GLCellLayout layout = getCurrentScreenView();
		if (layout != null) {
			 GLCellLayout.setTopExtra(topPadding);
			 GLCellLayout.setBottomExtra(bottomPadding);
			 GLCellLayout.setRightExtra(rightPadding);
			 layout.requestLayout();
		}
		final int count = getChildCount();
		// 再变其它屏
		for (int i = 0; i < count; i++) {
			GLView child = this.getChildAt(i);
			if (child != null) {
				child.requestLayout();
			}
		} // end for
	}

	/**
	 * 
	 * @return 当前屏幕View
	 */
	public GLCellLayout getCurrentScreenView() {
		return (GLCellLayout) getChildAt(getCurrentScreen());
	}

	public void setScrollDuration(int duration) {
		mScrollingDuration = duration;
		mScroller.setDuration(duration);
	}

	public void setOvershootAmount(int bounce) {
		if (mScrollingBounce == bounce) {
			return;
		}
		mScrollingBounce = bounce;
		mScroller.setMaxOvershootPercent(bounce);
	}

	public void setAutoTweakElasicity(boolean enabled) {
		mScroller.setEffectorMaxOvershootEnabled(enabled);
	}

	@Override
	public void drawScreenCell(GLCanvas canvas, int screen, int index) {
		final int realIndex = index % mGridCount;
		//		final int screen = index / mGridCount;
		GLCellLayout layout = (GLCellLayout) getChildAt(screen);

		final int culumn = realIndex % mDesktopColumns; // 第几列
		final int row = realIndex / mDesktopColumns; // 第几行

		if (layout != null) {
			if (layout.getChildCount() < 1 || null == mCurGridsState || null == mNextGridsState) {
				return;
			}
			final int length = mDesktopRows * mDesktopColumns;
			if (mCurGridsState.length != length || mNextGridsState.length != length) {
				prepareSubView();
			}
			Point gridPoint = null;
			if (screen == mScreenA) {
				gridPoint = mCurGridsState[realIndex];
			} else if (screen == mScreenB) {
				gridPoint = mNextGridsState[realIndex];
			}

			if (null == gridPoint || gridPoint.x == 0) {
				return;
			}
			final boolean cull = canvas.isCullFaceEnabled();
			// 如果是单个格子
			if (gridPoint.x == 1) {
				GLView view = layout.getChildAt(gridPoint.y);
				if (null == view /*|| view.getVisibility() != VISIBLE*/) { // 应用特效时，滑屏不显示隐藏图标
					return;
				}
				Object obj = view.getTag();
				if (obj instanceof ItemInfo) {
					ItemInfo info = (ItemInfo) obj;
					if (info.mCellX >= GLCellLayout.sColumns || info.mCellY >= GLCellLayout.sRows) {
						return;
					}
				}
				if (view instanceof GLWidgetContainer) {
					layout.checkWidgetDrawingCache(view);
				}
				canvas.setCullFaceEnabled(false);
				toDrawChild(canvas, layout, view, getDrawingTime());
			} else if (gridPoint.x == -2) {
				GLView view = layout.getChildAt(gridPoint.y);
				if (null == view || view.getVisibility() != VISIBLE) { // 应用特效时，滑屏不显示隐藏图标
					return;
				}
				Object obj = view.getTag();
				if (obj instanceof ItemInfo) {
					ItemInfo info = (ItemInfo) obj;
					if (info.mCellX >= GLCellLayout.sColumns || info.mCellY >= GLCellLayout.sRows) {
						return;
					}
				}
				if (view instanceof GLWidgetContainer) {
					layout.checkWidgetDrawingCache(view);
					if (view.isDrawingCacheEnabled()) {
						view.buildDrawingCache(canvas);
					}
				}
				canvas.setCullFaceEnabled(false);
				float depth = mScroller.getCurrentDepth(); // 获取当前深度

				final float gw = mGridWidth;
				final float gh = mGridHeight;

				final int insetX = Math.round(gw * 0.4f * depth * 0.5f);	//TODO:mSubWidth为0,暂时使用40%的缩进裁剪
				final int insetY = Math.round(gh * 0.4f * depth * 0.5f);

				final float clipLeft = GLCellLayout.getLeftPadding() + culumn * gw;
				final float clipTop = GLCellLayout.getTopPadding() + row * gh;
				canvas.clipRect(clipLeft + insetX, clipTop + insetY, clipLeft + gw - insetX,
						clipTop + gh - insetY);

				if (!mScroller.isFinished()) {
					toDrawChild(canvas, layout, view, getDrawingTime());
				}
			}
			canvas.setCullFaceEnabled(cull);
		}
	}

	@Override
	public void drawScreenCell(GLCanvas canvas, int screen, int index, int alpha) {
		final int oldAlpha = canvas.getAlpha();
		canvas.multiplyAlpha(alpha);
		drawScreenCell(canvas, screen, index);
		canvas.setAlpha(oldAlpha);
	}

	private void toDrawChild(GLCanvas canvas, GLCellLayout layout, GLView childView,
			long drawingTime) {
		drawChild(canvas, childView, drawingTime);
	}

	@Override
	public int getCellRow() {
		return mDesktopRows;
	}

	@Override
	public int getCellCol() {
		return mDesktopColumns;
	}

	@Override
	public int getCellCount() {
		return mGridCount * getChildCount();
	}

	@Override
	public int getCellWidth() {
		if (mGridWidth == 0) {
			mGridWidth = GLCellLayout.getViewOffsetW();
		}
		return mGridWidth;
	}
	
	/**
	 * 
	 * @return 单屏幕的格子总数
	 */
	public int getGridCount() {
		return mGridCount;
	}

	@Override
	public int getCellHeight() {
		if (mGridHeight == 0) {
			mGridHeight = GLCellLayout.getViewOffsetH();
		}
		return mGridHeight;
	}

	@Override
	public void drawScreenBackground(GLCanvas canvas, int screen) {
		if (mScaleState == STATE_NORMAL) {
			return;
		}
		
		if (mScroller.getCurrentScreenOffset() != 0 || mScroller.getCurrentDepth() != 0) { //加上这个判断避免最后一帧出现闪帧，闪帧原因不明
			GLCellLayout layout = (GLCellLayout) getChildAt(screen);
			if (layout != null) {
				layout.drawBackground(canvas);
				layout.drawCenterCross(canvas);
			}
		}
	}

	private void changeGridState(Point[] grids, int screen) {
		final int gridCount = grids.length;
		for (int i = 0; i < gridCount; i++) {
			grids[i] = new Point(0, 0);
		}
		final GLCellLayout layout = (GLCellLayout) getChildAt(screen);
		if (layout == null) {
			return;
		}
		int count = layout.getChildCount();
		for (int i = 0; i < count; i++) {
			GLView subView = layout.getChildAt(i);
			if (null == subView) {
				continue;
			}
			final ItemInfo itemInfo = (ItemInfo) subView.getTag();
			if (null == itemInfo || itemInfo.mCellX < 0 || itemInfo.mCellY < 0) {
				continue;
			}

			// 初始位置
			final int index = mDesktopColumns * itemInfo.mCellY + itemInfo.mCellX;
			if (index >= gridCount || index < 0) {
				continue;
			}
			if (subView instanceof GLScreenShortCutIcon) {
				grids[index].x = 1;
				grids[index].y = i;
			} else if (itemInfo.mItemType == IItemType.ITEM_TYPE_APP_WIDGET
					|| itemInfo.mItemType == IItemType.ITEM_TYPE_FAVORITE
					|| itemInfo.mItemType == IItemType.ITEM_TYPE_USER_FOLDER) {
				if (itemInfo.mSpanX == 1 && itemInfo.mSpanY == 1) {
					grids[index].x = 1;
					grids[index].y = i;
					continue;
				}
				grids[index].x = -2;
				grids[index].y = i;
				for (int j = 0; j < itemInfo.mSpanX; j++) {
					for (int k = 0; k < itemInfo.mSpanY; k++) {
						final int subIndex = index + mDesktopColumns * k + j;
						if (subIndex >= 0 && subIndex < gridCount) {
							grids[subIndex].x = -2;
							grids[subIndex].y = i;
						}
					}
				}
			}
		} // end for
	}

	// 内部调用
	private void prepareSubView() {
		// 如果当前特效不是单元格特效，那就什么也不做
		if (mScreenEffector.getCurEffectorType() == IEffectorIds.SUBSCREEN_EFFECTOR_TYPE) {
			return;
		}
		final int length = mDesktopRows * mDesktopColumns;
		if (null == mCurGridsState || mCurGridsState.length != length) {
			mCurGridsState = new Point[length];
			// 设为较大的负数，是为了一定进入下面的changeGridState
			mScreenA = -101;
		}
		if (null == mNextGridsState || mNextGridsState.length != length) {
			mNextGridsState = new Point[length];
			mScreenB = -102;
		}

		if (mScreenA != mZeroHandler.getDrawingScreenA()) {
			mScreenA = mZeroHandler.getDrawingScreenA();
			changeGridState(mCurGridsState, mScreenA);
		}
		if (mScreenB != mZeroHandler.getDrawingScreenB()) {
			mScreenB = mZeroHandler.getDrawingScreenB();
			changeGridState(mNextGridsState, mScreenB);
		}
		
		//判断是否有0屏，0屏需要-1
//		if (((GLSuperWorkspace) getGLParent()).isShowingZero()) {
//			if (mScreenA != mScroller.getDrawingScreenA() - 1) {
//				mScreenA = mScroller.getDrawingScreenA() - 1;
//				changeGridState(mCurGridsState, mScreenA);
//			}
//			if (mScreenB != mScroller.getDrawingScreenB() - 1) {
//				mScreenB = mScroller.getDrawingScreenB() - 1;
//				changeGridState(mNextGridsState, mScreenB);
//			}
//		} else {
//			if (mScreenA != mScroller.getDrawingScreenA()) {
//				mScreenA = mScroller.getDrawingScreenA();
//				changeGridState(mCurGridsState, mScreenA);
//			}
//			if (mScreenB != mScroller.getDrawingScreenB()) {
//				mScreenB = mScroller.getDrawingScreenB();
//				changeGridState(mNextGridsState, mScreenB);
//			}
//		}
		
	} // end prepareSubView

	// 供外部调用
	public void refreshSubView() { // 如果当前特效不是单元格特效，那就什么也不做
		if (mScreenEffector.getCurEffectorType() == IEffectorIds.SUBSCREEN_EFFECTOR_TYPE) {
			return;
		}
		//判断是否有0屏，0屏需要-1
		mScreenA = mZeroHandler.getDrawingScreenA();
//		if (((GLSuperWorkspace) getGLParent()).isShowingZero()) {
//			mScreenA = mScroller.getDrawingScreenA() - 1;
//		} else {
//			mScreenA = mScroller.getDrawingScreenA();
//
//		}

		final int length = mDesktopRows * mDesktopColumns;
		if (null == mCurGridsState || mCurGridsState.length != length) {
			mCurGridsState = new Point[length];
		}
		changeGridState(mCurGridsState, mScreenA);

		//判断是否有0屏，0屏需要-1
		mScreenB = mZeroHandler.getDrawingScreenB();
//
//		if (((GLSuperWorkspace) getGLParent()).isShowingZero()) {
//			mScreenB = mScroller.getDrawingScreenB() - 1;
//		} else {
//			mScreenB = mScroller.getDrawingScreenB();
//		}
		if (null == mNextGridsState || mNextGridsState.length != length) {
			mNextGridsState = new Point[length];
		}
		changeGridState(mNextGridsState, mScreenB);
	}

	public void refreshScreenIndex() {
		int screenNum = getChildCount();
		for (int i = 0; i < screenNum; i++) {
			final GLCellLayout layout = (GLCellLayout) getChildAt(i);
			layout.setScreen(i);
			layout.updateItemInfoScreenIndex();
		}
	}
	/**
	 * 进入添加界面前的准备工作
	 */
	public void normalToSmallPreAction(int screenEditTabId, Object info, int smallLevel) {
		int current = mCurrentScreen;
		//清除0屏幕指示器
		if (mZeroHandler.removeZeroScreen()) {
			setCurrentScreen(current);
		};
		
		ShellContainer.sEnableOrientationControl = false;
		GLOrientationControler.setSmallModle(true);
		GLOrientationControler.setOrientationType(OrientationTypes.VERTICAL);
		if (GoLauncherActivityProxy.isPortait()) {
			normalToSmall(screenEditTabId, info, smallLevel);
			setCellLayoutGridState(getCurrentScreen(), true, -1);
		} else {
			mSmallLeveInLandscape = smallLevel;
			mSmallLeveObj = info;
		}
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_HIDE_CURRENT_GUIDE, 0);
	}

	/**
	 * 
	 * 指定屏是否有子元素
	 * 
	 * @param screenId
	 *            屏ID
	 */
	public boolean hasChildElement(int screenId) {
		GLCellLayout child = (GLCellLayout) getChildAt(screenId);
		return child != null && child.getChildCount() > 0;
	}

	protected void normalToSmall(int screenEditTabId, Object info, int smallLevel) {
		if ((sLayoutScale < 1.0f && sLayoutScale != getScreenMenuScale())
				|| (!GoLauncherActivityProxy.isPortait())) {
			return;
		}
		mSmallLeveInLandscape = -1;
		
		// 修复-->增加了 "+"屏后，mCurrentCount改变了，mDstScreen屏幕索引有误
		// init screenedit
		enterEditState(smallLevel);
		mShell.show(IViewId.SCREEN_EIDT, true, screenEditTabId, info);

		try {
			SecurityPoxyFactory.getSecurityPoxy().clearSecurityResult(); // 清除安全的标识
		} catch (UnsupportSecurityPoxyException ex) {
		}
//		setEnableWidgetDrawingCache(true);
	}

	/***
	 * 屏幕满时，桌面背景变红
	 */
	public void resetScreenBg(boolean isFull) {
		final GLCellLayout group = (GLCellLayout) getChildAt(mCurrentScreen);
		if (group != null) {
			group.changeBackground(isFull);
		}
	}

	/***
	 * 重设所有背景为默认颜色
	 */
	public void resetAllScreenBg() {
		final int screenCount = getChildCount();
		for (int i = 0; i < screenCount; i++) {
			final GLCellLayout cl = (GLCellLayout) getChildAt(i);
			cl.changeBackground(false);
		}
	}

	public boolean allowLongPress() {
		return mAllowLongPress;
	}

	public void setAllowLongPress(boolean allowLongPress) {
		if (mAllowLongPress != allowLongPress) {
			mAllowLongPress = allowLongPress;
			if (!mAllowLongPress) {
				final GLCellLayout currentScreen = getCurrentScreenView();
				if (currentScreen != null) {
					currentScreen.cancelLongPress();
				}
			}
		}
	}

	/**
	 * 开始长按
	 * 
	 * @param cellInfo
	 *            拖拽格子信息
	 */
	public void startDrag(GLCellLayout.CellInfo cellInfo) {
		GLView child = cellInfo.cell; // 被点击的View

		// Make sure the drag was started by a long press as opposed to a long
		// click.
		if (!child.isInTouchMode()) {
			return;
		}
		setEnableCellLayoutDrawingCache(false);
		if (child instanceof GLWidgetContainer) {
//			GLCellLayout.sEnableWidgetDrawingCache = true;
			child.setDrawingCacheEnabled(true);
		}

//		mNeedHideMenu = true;
		mDragInfo = cellInfo; // 拖拽信息
		mDragOverMode = DRAG_OVER_CURR; // 当前屏幕范围内
		//		int zDepth = DragController.DRAG_ICON_Z_DEPTH;
		//		if (child.getTag() instanceof ScreenAppWidgetInfo) {
		//			zDepth = DragController.DRAG_WIDGET_Z_DEPTH;
		//		}

		float scale = DragController.DRAG_ICON_SCALE;
		if (child instanceof GLWidgetContainer) {
			scale = DragController.DRAG_WIDGET_SCALE;
		}
		GLCellLayout current = (GLCellLayout) getChildAt(cellInfo.screen);
		if (null != current) {
//			mIsDragging = true;
			current.markCellsAsUnoccupiedForView(child);
			// current.onDragChild(child);
			final DragAnimationInfo dragAnimationInfo = new DragAnimationInfo(true, scale,
					false, DragAnimation.DURATION_200, null);
			//			mDragTransInfo[2] = zDepth;
			// 设置额外绘制信息的标记，网格、背景、前后屏的边框
			/*
			 * current.setDrawExtraFlag(getCellLayoutDrawExtraFlag( cellInfo.screen ==
			 * mCurrentScreen, cellInfo.screen));
			 */
			mDragTargetLayout = current;
			mTempCell[0] = cellInfo.cellX;
			mTempCell[1] = cellInfo.cellY;
			// 重新生成拖拽子View绘制在目标格子的轮廓图片
			// current.visualizeDropLocation(child, child.getLeft(), child.getTop(), mTempCell);
			// 如果是缩小状态下，放大值适当减小
			if (mScaleState != STATE_NORMAL) {
				//				mDragTransInfo[2] = -zDepth;
				mDragTransInfo[2] = mTranslateZ;
				int[] result = new int[2];
				GLCellLayout.cellToPoint(mTempCell[0], mTempCell[1], result);
				float[] virtual = new float[2];
				realPointToVirtual(result[0], result[1], virtual);
				mDragController.startDrag(child, this, child.getTag(),
						DragController.DRAG_ACTION_MOVE, mDragTransInfo, 0,
						getScaleValue(mTranslateZ), (int) virtual[0], (int) virtual[1],
						dragAnimationInfo);
			} else {
				mDragTransInfo[2] = 0;
				mDragController.startDrag(child, this, child.getTag(),
						DragController.DRAG_ACTION_MOVE, mDragTransInfo, dragAnimationInfo);
			}
		}
	}

	@Override
	public void setDragController(DragController dragger) {
		mDragController = dragger;
	}

	/**
	 * 获取目标屏
	 */
	private int getDestScreen() {
		return mScroller.isFinished() ? mCurrentScreen : mNextScreen;
	}

	/**
	 * Return the current {@link CellLayout}, correctly picking the destination screen while a
	 * scroll is in progress.
	 */
	protected GLCellLayout getCurrentDropLayout() {
		return (GLCellLayout) getChildAt(getDestScreen());
	}

	@Override
	public void onDropCompleted(DropTarget target, Object dragInfo, boolean success,
			DropAnimationInfo resetInfo) {
		if (dragInfo != null && dragInfo instanceof ItemInfo && mDragInfo != null) {
			ItemInfo info = (ItemInfo) dragInfo;
			// 放手后在目标区域成功放置
			if (success) {
				if (!(target instanceof GLWorkspace)) {
					GLViewParent parent = getGLParent().getGLParent();
					// 删除桌面item
					if (parent != null && parent instanceof GLScreen) {
						// 1,如果拖拽到dock并且是文件夹，则不需要删除文件夹里面数据 
						// 2,如果是垃圾桶则要删除
						if (target instanceof GLDeleteZone || target instanceof GLDock) {
							// 如果拖拽到dock并且是文件夹，则不需要删除文件夹里面数据
							final boolean delFolderItems = !(target instanceof GLDock && info instanceof UserFolderInfo);
							final GLView targetView = ScreenUtils.getViewByItemId(
									info.mInScreenId, mDragInfo.screen, this);
							((GLScreen) parent).deleteScreenItem(info, mDragInfo.screen, delFolderItems);
							if (target instanceof GLDeleteZone) {
								// widget的话要通知widget remove和delete
								if (info != null && info instanceof ScreenAppWidgetInfo) {
									final Go3DWidgetManager widgetManager = Go3DWidgetManager
											.getInstance(ShellAdmin.sShellManager.getActivity(),
													mContext);
									ScreenAppWidgetInfo widgetInfo = (ScreenAppWidgetInfo) info;
									widgetManager.removeWidget(widgetInfo.mAppWidgetId);
									widgetManager.deleteWidget(widgetInfo.mAppWidgetId);
								}
								// widget和图标拖到垃圾桶都要cleanup
								post(new Runnable() {

									@Override
									public void run() {
										if (targetView != null) {
											targetView.cleanup();
//											GLAppFolder.getInstance().batchStartIconEditEndAnimation();
										}
									}
								});
							}
						}
					}
				}
			} else { // 放手后在目标区域不成功放置，回到原来位置
//				int[] dstCenterXY = new int[2];
				GLCellLayout layout = (GLCellLayout) getChildAt(mDragInfo.screen);
				if (layout != null) {
					final GLView targetView = ScreenUtils.getViewByItemId(
							info.mInScreenId, mDragInfo.screen, this);
					int[] xy = new int[2];
					targetView.getLocationInWindow(xy);
					int centerX = xy[0] + targetView.getWidth() / 2;
					int centerY = xy[1] + targetView.getHeight() / 2;
					resetInfo.setLocationPoint(centerX, centerY);
//					layout.cellToCenterPoint(info.mCellX, info.mCellY, dstCenterXY);
//					resetInfo.setLocationPoint(dstCenterXY[0], dstCenterXY[1]);
					resetInfo.setDuration(DragAnimation.DURATION_200);
					resetInfo.setLocationType(DropAnimationInfo.LOCATION_CENTER);
				}
//				postDelayed(new Runnable() {
//
//					@Override
//					public void run() {
//						GLAppFolder.getInstance().batchStartIconEditEndAnimation();
//					}
//				}, 200);
			} // end else
		} // end if 1
		resetDragInfo();
		//		mIsDragging = false;
	}
	
	/**
	 * 拖拽信息还原
	 */
	private void resetDragInfo() {
		// 以下是不管成功与否的操作，进行一些拖拽信息的还原
		mDragInfo = null;
		mDragTargetIcon = null;
		if (getChildAt(mCurrentScreen) != null) {
			getChildAt(mCurrentScreen).invalidate();
		}
		mLastReorderX = -1;
		mLastReorderY = -1;
		mDragViewType = -1;
	}

//	public void setShouldShowQuickMenu(GLView view) {
//		mQuickMenuTargetView = view;
//		mShouldShowQuickMenu = true;
//	}
	
//	public void cancelQuickMenu() {
//		mQuickMenuTargetView = null;
//		mShouldShowQuickMenu = false;
//	}
	
	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		if (mShell.getCurrentStage() != IShell.STAGE_SCREEN) {
			return;
		}
		if ((source instanceof GLWorkspace) || (source instanceof GLDock)) {
			if (source instanceof GLDock && info instanceof DockItemInfo) {
				DockItemInfo dockItemInfo = (DockItemInfo) info;
				prepareCellLayoutGrideStateFromDock(dockItemInfo);
			}
			setCellLayoutGridState(getCurrentScreen(), true, -1);
			QuickActionMenuHandler.getInstance().onDragStart(source, info, dragAction);
		}
//		if (!(info instanceof ScreenAppWidgetInfo)) {
//			GLAppFolder.getInstance().batchStartIconEditAnimation();
//		}
	}

	@Override
	public void onDragEnd() {
		// 该方法一定得调用，用于重置快捷菜单相关属性
		QuickActionMenuHandler.getInstance().onDragEnd();
		if (mShell.getCurrentStage() == IShell.STAGE_SCREEN
				|| mShell.getCurrentStage() == IShell.STAGE_APP_FOLDER) {
			if (mDragController.getDragSource() == this
					&& mDragController.getOriginator() instanceof GLWidgetContainer) {
				//					GLCellLayout.sEnableWidgetDrawingCache = false;
				mDragController.getOriginator().setDrawingCacheEnabled(false);
			}
			if (!mIsFlyToDelete) {
				mDragInfo = null; // 防止内存泄露
			}
		}
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			setCellLayoutGridState(i, false, -1);
		}
	}

	@Override
	public void onEnterLeftScrollZone() {
		mInScrollZone = SCROLL_ZONE_LEFT;
		//进入屏幕预览widget不要用DrawingCache
		setEnableWidgetDrawingCache(false);
		// TODO:消息的id没有写明注释 0，1和2是什么意思？
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_ENTER_SCROLL_ZONE_BG, 1);
//		if (getGLParent() instanceof GLScreen) {
//			cancelQuickMenu();
//		}
		setGLSenseNeedScaleWidget();
	}

	@Override
	public void onEnterRightScrollZone() {
		mInScrollZone = SCROLL_ZONE_RIGHT;
		//进入屏幕预览widget不要用DrawingCache
		setEnableWidgetDrawingCache(false);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_ENTER_SCROLL_ZONE_BG, 2);
//		if (!GoLauncherActivityProxy.isPortait()) {
//			MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK, this,
//					IScreenFrameMsgId.ICON_FROM_SCREEN_TO_DOCK_TO_SCRENN, -1);
//		}
//		if (getGLParent() instanceof GLScreen) {
//			cancelQuickMenu();
//		}
		setGLSenseNeedScaleWidget();
	}

	@Override
	public void onEnterTopScrollZone() {
//		if (getGLParent() instanceof GLScreen) {
//			cancelQuickMenu();
//		}
	}

	@Override
	public void onEnterBottomScrollZone() {
//		if (getGLParent() instanceof GLScreen) {
//			cancelQuickMenu();
//		}
	}

	@Override
	public void onExitScrollZone() {
		mInScrollZone = SCROLL_ZONE_NONE;
		// TODO:消息的id没有写明注释 0，1和2是什么意思？
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_ENTER_SCROLL_ZONE_BG, 0);
	}
	
	public void setGLSenseNeedScaleWidget() {
		if (mDragInfo != null && mDragInfo.cell instanceof GLWidgetContainer) {
			GLSenseWorkspace.sNeedScaleWidget = true;
		}
	}
	
	/**
	 * 拖动widget进入屏幕预览,缩小widget
	 * @param scaleX
	 * @param scaleY
	 */
	public void resizeDragViewForPreview(float scaleX, float scaleY) {
		if (mDragInfo != null && mDragInfo.cell instanceof GLWidgetContainer) {
			GLWidgetContainer widget = (GLWidgetContainer) mDragInfo.cell;
			if (scaleX == 0 && scaleY == 0) {
				widget.setTransformationInfo(null);
			} else {
				TransformationInfo transformationInfo = widget.getTransformationInfo();
				if (transformationInfo == null) {
					transformationInfo = new TransformationInfo();
					widget.setTransformationInfo(transformationInfo);
				}
				widget.setPivotXY(widget.getWidth() / 2, widget.getHeight() / 2);
				widget.setScaleXY(scaleX, scaleX);
			}
		}
	}

	@Override
	public void onScrollLeft() {
		if (mScaleState == STATE_NORMAL) {
			if (mDragTargetIcon != null) {
				mDragTargetIcon.cancleFolderReady();
				mDragTargetIcon = null;
			}
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SCREEN_SHOW_PREVIEW,
					-1, false);
		}
	}

	@Override
	public void onScrollRight() {
		if (mScaleState == STATE_NORMAL) {
			if (mDragTargetIcon != null) {
				mDragTargetIcon.cancleFolderReady();
				mDragTargetIcon = null;
			}
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SCREEN_SHOW_PREVIEW,
					-1, false);
		}
	}

	@Override
	public void onScrollTop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollBottom() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPostScrollRunnable(int direction) {
		// TODO Auto-generated method stub

	}

	@Override
	public Rect getScrollLeftRect() {

		int layoutHeight = StatusBarHandler.getDisplayHeight();
		int h = GLScreen.sDockVisible ? layoutHeight - DockUtil.getBgHeight() : layoutHeight;
		//暂时写死40
		return new Rect(0, 0, mContext.getResources().getDimensionPixelSize(R.dimen.scroll_zone), h);
	}

	@Override
	public Rect getScrollRightRect() {
		int layoutHeight = StatusBarHandler.getDisplayHeight();
		int layoutWidth = StatusBarHandler.getDisplayWidth();
		int h = GLScreen.sDockVisible ? layoutHeight - DockUtil.getBgHeight() : layoutHeight;

		//暂时写死40
		return new Rect(layoutWidth
				- mContext.getResources().getDimensionPixelSize(R.dimen.scroll_zone), 0,
				layoutWidth, h);

	}

	@Override
	public Rect getScrollTopRect() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rect getScrollBottomRect() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getScrollType() {
		return DragScroller.SCROLL_TYPE_HORIZONTAL;
	}

	@Override
	public int getScrollDelay() {
		return DragScroller.SCROLL_DELAY_HORIZONTAL;
	}

	@Override
	public int getNextScrollDelay() {
		return DragScroller.NEXT_SCROLL_DELAY_HORIZONTAL;
	}

	/**
	 * 与文件夹相关的drop处理（包括合成文件夹和拖进文件夹）
	 * 只能在onDrop方法里面调用
	 * @param x
	 * @param y
	 * @param dragView
	 * @param dragInfo
	 * @param resetInfo
	 * @param screen
	 * @return
	 */
	private boolean onDropConnectFolder(int x, int y, final DragView dragView, Object dragInfo,
			DropAnimationInfo resetInfo, int screen, DragSource dragSource) {
		boolean ret = false;
		// 如果合成文件夹则不需要往下执行
		if (mDragMode == DRAG_MODE_CREATE_FOLDER) {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.SCREEN_MERGE_ITEMS, screen, dragView, x, y);
			// 因为动画已经关闭了文件夹，所以没必要用手动挡
			// cleanupFolderCreation();
			ret = true;
		} else if (mDragMode == DRAG_MODE_ADD_TO_FOLDER) {
			ret = MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.SCREEN_MOVE_TO_FOLDER, screen, dragView, x, y, resetInfo, dragInfo,
					dragSource);
			// 因为动画已经关闭了文件夹，所以没必要用手动挡
			// cleanupFolderCreation();
		}
		return ret;
	}
	
	/**
	 * 执行放手后的动画(动画结束后的事件处理是关键)
	 * 只能在onDrop方法里面调用
	 * @param source
	 * @param dragInfo
	 * @param dragView
	 * @param spanX
	 * @param spanY
	 * @param screenIndex
	 * @param cell
	 * @param resetInfo
	 * @param cellLayout
	 * @param dragFromScreen
	 * @param createBlank
	 */
	private void doDropAnim(final DragSource source, final Object dragInfo, final DragView dragView, int spanX, 
			int spanY, final int screenIndex, final GLView cell, final DropAnimationInfo resetInfo,
			final GLCellLayout cellLayout, final boolean dragFromScreen, final boolean createBlank, final int x, final int y) {
		// 拖拽类型
		final int dragTpye = dragView.getDragViewType();
		// 放手后目标的中心坐标
		int[] dstCenterXY = new int[2];
		// 根据找到的合适格子进行数值转换
		GLCellLayout.cellsToCenterPoint(mTargetCell[0], mTargetCell[1], spanX, spanY, dstCenterXY);
		final GLView orignalView = dragView.getOriginalView();
		// 如果拖拽的item是dock等其他模块的图标，则需要进行图标大小的size统一（因为dockIcon是没有文字显示的，高度默认会比屏幕层的小）
		if (source != this && orignalView instanceof IconView<?>) {
			float[] iconCenterPoint = IconUtils.getIconCenterPoint(dstCenterXY[0], dstCenterXY[1],
					GLScreenShortCutIcon.class);
			resetInfo.setLocationPoint(iconCenterPoint[0], iconCenterPoint[1] - mTranslateY,
					mTranslateZ, DockUtil.getIconSize(4) / (float) dragView.getWidth());
			resetInfo.setLocationType(DropAnimationInfo.LOCATION_ICON_CENTER);
		} else {
			//			int[] xy = new int[2];
			//			orignalView.getLocationInWindow(xy);
			//			int centerX = xy[0] + orignalView.getWidth() / 2;
			//			int centerY = xy[1] + orignalView.getHeight() / 2;
			//			resetInfo.setLocationPoint(centerX, centerY);
			resetInfo.setLocationPoint(dstCenterXY[0], dstCenterXY[1] - mTranslateY, mTranslateZ);
			resetInfo.setLocationType(DropAnimationInfo.LOCATION_CENTER);
		}
		resetInfo.setDuration(DragAnimation.DURATION_200);
				if (QuickActionMenuHandler.getInstance().needShowActionMenu(x, y, dragView)) {
					// 编辑模式不弹出快捷菜单
					resetInfo.setNeedToShowCircle(false);
					if (sLayoutScale == 1.0f) {
						//需要先获取上一级的GLsSuperWorkspce
						if (getGLParent() != null &&  getGLParent().getGLParent() != null) {
							GLViewParent viewParent = getGLParent().getGLParent();
							if (viewParent instanceof GLScreen) {
								((GLScreen) viewParent).showQuickActionMenu(dragView.getOriginalView());
							}
						}
					}
					QuickActionMenuHandler.getInstance().reset();
				}
		resetInfo.setAnimationListener(new AnimationListenerAdapter() {

			@Override
			public void onAnimationEnd(Animation animation) {
				GLView newChild = cell;
				// 如果托拽源不是桌面则需要生成新的item
				if (!dragFromScreen) {
					final GLView[] newViews = new GLView[1];
					// 对拖拽的info进行类型转换，得到桌面屏幕层的itemInfo
					final ItemInfo info = DragInfoTranslater.createItemInfoForScreen(dragTpye,
							dragInfo, mTargetCell[0], mTargetCell[1]);
					// 写进数据库
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_ADD_DESKTOP_ITEM, screenIndex, info, dragTpye);
					// 请求生成桌面的view
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_CREATE_ITEM, screenIndex, info, newViews);
					// view生成成功则进行UI添加
					if (newViews[0] != null) {
						newChild = newViews[0];
						// 添加到屏幕
						addInScreen(newChild, screenIndex, mTargetCell[0], mTargetCell[1], 1, 1);
					} else {
						// TODO：添加不成功需删除相应数据
						return;
					}
				} else {
					if (!mSameLocation) {
						GuiThemeStatistics
								.goLauncherUserBehaviorStaticDataCache(IGoLauncherUserBehaviorStatic.LAUNCHER_OP_04);
					}
				}
				if (!mSameLocation) {
					// 执行把view放到当前屏幕的操作（不管拖拽源是哪个模块都要执行）
					cellLayout.onDropChild(newChild, mTargetCell);
					// 执行当前屏幕放手后的事件处理
					cellLayout.clearVisualizeDropLocation();
					// 更新被拖拽的item的数据信息
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_UPDATE_ITEM_INFOMATION, screenIndex,
							newChild.getTag());
					// 在需要情况下，新建一个屏幕（屏幕的个数限制为9）
					if (createBlank && mScaleState != STATE_NORMAL
							&& getChildCount() < GLSenseWorkspace.MAX_CARD_NUMS) {
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
								IScreenFrameMsgId.SCREEN_ADD_BLANK_CELLLAYOUT, -1);
					}
					// 关于放手后文件夹的逻辑处理（仔建那边的业务逻辑）
					post(new Runnable() {
						@Override
						public void run() {
							if (GLAppFolder.getInstance().isFolderOpened()) {
								onFolderFingIconToScreen(null);
							}
							if (source instanceof GLDock) {
								orignalView.cleanup();
							}
						}
					});
				} else {
					mSameLocation = false;
				}
			}
		});
	} // end doDropAnim
	
	private void doDropWithoutAnim(final DragSource source, final Object dragInfo, final DragView dragView, int spanX, 
			int spanY, final int screenIndex, final GLView cell, final DropAnimationInfo resetInfo,
			final GLCellLayout cellLayout, final boolean dragFromScreen, final boolean createBlank, final int x, final int y) {
		// 拖拽类型
		final int dragTpye = dragView.getDragViewType();
		// 放手后目标的中心坐标
		int[] dstCenterXY = new int[2];
	    // 根据找到的合适格子进行数值转换
		GLCellLayout.cellsToCenterPoint(mTargetCell[0], mTargetCell[1], spanX, spanY, dstCenterXY);
		final GLView orignalView = dragView.getOriginalView();
		// 如果拖拽的item是dock等其他模块的图标，则需要进行图标大小的size统一（因为dockIcon是没有文字显示的，高度默认会比屏幕层的小）
		
		resetInfo.setDuration(0);

		GLView newChild = cell;
		// 如果托拽源不是桌面则需要生成新的item
		if (!dragFromScreen) {
			final GLView[] newViews = new GLView[1];
			// 对拖拽的info进行类型转换，得到桌面屏幕层的itemInfo
			final ItemInfo info = DragInfoTranslater.createItemInfoForScreen(dragTpye, dragInfo,
					mTargetCell[0], mTargetCell[1]);
			// 写进数据库
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.SCREEN_ADD_DESKTOP_ITEM, screenIndex, info, dragTpye);
			// 请求生成桌面的view
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.SCREEN_CREATE_ITEM, screenIndex, info, newViews);
			// view生成成功则进行UI添加
			if (newViews[0] != null) {
				newChild = newViews[0];
				// 添加到屏幕
				addInScreen(newChild, screenIndex, mTargetCell[0], mTargetCell[1], 1, 1);
			} else {
				// TODO：添加不成功需删除相应数据
				return;
			}
		}
		if (!mSameLocation) {
			// 执行把view放到当前屏幕的操作（不管拖拽源是哪个模块都要执行）
			cellLayout.onDropChild(newChild, mTargetCell);
			// 执行当前屏幕放手后的事件处理
			cellLayout.clearVisualizeDropLocation();
			// 更新被拖拽的item的数据信息
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.SCREEN_UPDATE_ITEM_INFOMATION, screenIndex, newChild.getTag());
			// 在需要情况下，新建一个屏幕（屏幕的个数限制为9）
			if (createBlank && mScaleState != STATE_NORMAL
					&& getChildCount() < GLSenseWorkspace.MAX_CARD_NUMS) {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
						IScreenFrameMsgId.SCREEN_ADD_BLANK_CELLLAYOUT, -1);
			}
			// 关于放手后文件夹的逻辑处理（仔建那边的业务逻辑）
			post(new Runnable() {
				@Override
				public void run() {
					if (GLAppFolder.getInstance().isFolderOpened()) {
						onFolderFingIconToScreen(null);
					}
					if (source instanceof GLDock) {
						orignalView.cleanup();
					}
				}
			});
		} else {
			mSameLocation = false;
		}
	} // end doDropWithoutAnim
	
	public boolean onDrop(final DragSource source, int x, int y, int xOffset, int yOffset,
			final DragView dragView, Object dragInfo, DropAnimationInfo resetInfo, int screen) {
		final int screenIndex = screen;
		// 如果图标移动到0屏则返回
		if (mZeroHandler.isInZeroScreen()) {
			return false;
		}
		// 将目标屏幕状态置为普通状态
		if (mDragTargetLayout != null) {
			mDragTargetLayout.setDrawStatus(GLCellLayout.DRAW_STATUS_NORMAL);
		}

		// 如果合成文件夹(或者拖进文件夹)则不需要往下执行
		if (mDragMode == DRAG_MODE_CREATE_FOLDER || mDragMode == DRAG_MODE_ADD_TO_FOLDER) {
			return onDropConnectFolder(x, y, dragView, dragInfo, resetInfo, screenIndex, source);
		}
		
		final GLCellLayout cellLayout = (GLCellLayout) getChildAt(screenIndex);
		// 判断当前屏幕是否有效，否则不做放手后操作，不需要往下执行
		if (cellLayout == null) {
//			GLAppFolder.getInstance().batchStartIconEditEndAnimation();
			return false;
		}
		
		// mIsDragging = false;
		int centerX = x;
		int centerY = y;

		mIsAlreadyDrop = true;
		// 如果不是普通模式则需要进行坐标的转换
		if (mScaleState != STATE_NORMAL) {
			float[] realXY = new float[2];
			// 先转换为真实值
			virtualPointToReal(centerX, centerY, realXY);
			centerX = (int) realXY[0];
			centerY = (int) realXY[1];
		}
		// 减去偏移数值，得到当前抓起的item的中心点（x，y）
		centerX = (int) (centerX - xOffset);
		centerY = (int) (centerY - yOffset);

		GLView tempCell = dragView;
		boolean tempdragFromScreen = false;
		// 从dock或者功能表托出来的都是1 x 1的item，默认为1 x 1
		int spanX = 1;
		int spanY = 1;
		// 如果是屏幕层本身发起的托拽，则用mDragInfo.cell进行赋值
		if (mDragInfo != null && mDragInfo.cell != null) {
			tempdragFromScreen = true;
			tempCell = mDragInfo.cell;
			spanX = mDragInfo.spanX;
			spanY = mDragInfo.spanY;
		}
		
		final GLView cell = tempCell; // 当前抓起的item
		int[] sourceCell = new int[2];
		Object tag = cell.getTag();
		if (tag instanceof ItemInfo) {
			ItemInfo info = (ItemInfo) tag;
			sourceCell[0] = info.mCellX;
			sourceCell[1] = info.mCellY;
		}
		final boolean dragFromScreen = tempdragFromScreen; // 拖拽的发起者是屏幕层的标志
		
		int[] resultSpan = new int[2];
		// 计算目标网格,看是否有空间可以放置
		mTargetCell = cellLayout.createArea(centerX, centerY, spanX, spanY, spanX, spanY, cell,
				mTargetCell, resultSpan, GLCellLayout.MODE_ON_DROP);

		final boolean foundCell = mTargetCell != null && mTargetCell[0] >= 0 && mTargetCell[1] >= 0;
		// 找不到空闲合适的位置
		if (!foundCell) {
			GLViewParent parent = cell.getGLParent();
			// 需要找到拖拽view的父容器，然后将该位置置为已占用
			if (parent != null && parent instanceof GLCellLayout) {
				GLCellLayout.LayoutParams lp = (GLCellLayout.LayoutParams) cell.getLayoutParams();
				mTargetCell[0] = lp.cellX;
				mTargetCell[1] = lp.cellY;
				((GLCellLayout) parent).markCellsAsOccupiedForView(cell);
			}
			((GLScreen) getGLParent().getGLParent()).setScreenRedBg(); // 找不到就提示,背景变红
//			postDelayed(new Runnable() {
//				
//				@Override
//				public void run() {
//					GLAppFolder.getInstance().batchStartIconEditEndAnimation();
//					
//				}
//			}, 200);
			return false;
		}

		// 放手之后，是否需要新建一个屏幕的标识（如果在“+”号屏上放手标识为true）
		final boolean[] createBlank = new boolean[1];
		// 如果拖拽源是屏幕层，则进行以下的处理
		mSameLocation = false;
		if (dragFromScreen) {
			// 如果放手后的屏幕跟拽起的屏幕不相同，则需要进行数据的变换和UI的更新
			if (screenIndex != mDragInfo.screen) {
				cell.clearAnimation(); //确保没有动画与此View关联，避免removeView时添加到DisappearingView中，导致下一帧被detachedFromWindow
				GLViewParent viewParent = cell.getGLParent();
				// 原来的屏幕先进行移除
				if (viewParent != null) {
					((GLViewGroup) viewParent).removeView(cell);
				}
				// 判断是否需要新建一个屏幕（当前屏是“+”号屏则需要）
				createBlank[0] = blankCellToNormal(cellLayout, false);
				// 把拖拽的item添加到当前屏幕上
				cellLayout.addView(cell);

				// 最后对拖拽item的tag进行更新
				if (tag != null && tag instanceof ItemInfo) {
					ItemInfo info = (ItemInfo) tag;
					info.mCellX = mTargetCell[0];
					info.mCellY = mTargetCell[1];
				}
			} else {
				// 是否放回原来的位置
				if (sourceCell[0] == mTargetCell[0] && sourceCell[1] == mTargetCell[1]) {
					mSameLocation = true;
				}
			}
		}
		if (GLAppFolder.getInstance().isFolderOpened()
				&& !GLAppFolder.getInstance().isFolderClosing()) {
			//不应该做复位动画 
//			doDropWithoutAnim(source, dragInfo, dragView, spanX, spanY, screenIndex, cell,
//					resetInfo, cellLayout, dragFromScreen, createBlank[0], x, y);
			return false;
		} else {
			// 进行放手后的动画
			doDropAnim(source, dragInfo, dragView, spanX, spanY, screenIndex, cell, resetInfo,
					cellLayout, dragFromScreen, createBlank[0], x, y);
		}
		// 动态壁纸响应放下事件
		actionToLiveWallpaper();
		
//		postDelayed(new Runnable() {
//			
//			@Override
//			public void run() {
//				GLAppFolder.getInstance().batchStartIconEditEndAnimation();
//				
//			}
//		}, 200);
		
		return true;
	}

	/**
	 * 把item放到指定的位置上
	 * @param source
	 * @param x
	 * @param y
	 * @param xOffset
	 * @param yOffset
	 * @param dragView
	 * @param dragInfo
	 * @param resetInfo
	 * @param screen
	 * @param targetCell 长度为2的数组，指定的位置（cellX， cellY）,可以为null.
	 */
	public boolean dropToTargetCell(final DragSource source, int x, int y, int xOffset, int yOffset,
			final DragView dragView, Object dragInfo, DropAnimationInfo resetInfo, int screen, int[] targetCell) {
		
		final int screenIndex = screen;
		final GLCellLayout cellLayout = (GLCellLayout) getChildAt(screenIndex);
		// 判断当前屏幕是否有效，否则不做放手后操作，不需要往下执行
		if (cellLayout == null) {
			return false;
		}
		
		GLView tempCell = dragView;
		boolean tempdragFromScreen = false;
		// 从dock或者功能表托出来的都是1 x 1的item，默认为1 x 1
		int spanX = 1;
		int spanY = 1;
		// 如果是屏幕层本身发起的托拽，则用mDragInfo.cell进行赋值
		if (mDragInfo != null && mDragInfo.cell != null) {
			tempdragFromScreen = true;
			tempCell = mDragInfo.cell;
			spanX = mDragInfo.spanX;
			spanY = mDragInfo.spanY;
		}
		
		final GLView cell = tempCell; // 当前抓起的item
		final boolean dragFromScreen = tempdragFromScreen; // 拖拽的发起者是屏幕层的标志
		if (targetCell == null) {
			targetCell = new int[2];
			GLCellLayout.pointToCellExact(x, y, spanX, spanY, targetCell); // 转化为对应的格子
			
		}
		mTargetCell = targetCell;

		final boolean foundCell = mTargetCell != null && mTargetCell[0] >= 0 && mTargetCell[1] >= 0;
		// 找不到空闲合适的位置
		if (!foundCell) {
			// clearVacantCache();
			GLViewParent parent = cell.getGLParent();
			// 需要找到拖拽view的父容器，然后将该位置置为已占用
			if (parent != null && parent instanceof GLCellLayout) {
				GLCellLayout.LayoutParams lp = (GLCellLayout.LayoutParams) cell.getLayoutParams();
				mTargetCell[0] = lp.cellX;
				mTargetCell[1] = lp.cellY;
				((GLCellLayout) parent).markCellsAsOccupiedForView(cell);
			}
			((GLScreen) (getGLParent().getGLParent())).setScreenRedBg(); // 找不到就提示,背景变红
			return false;
		}

		// 如果拖拽源是屏幕层，则进行以下的处理
		mSameLocation = false;
		if (dragFromScreen) {
			// 如果放手后的屏幕跟拽起的屏幕不相同，则需要进行数据的变换和UI的更新
			if (screenIndex != mDragInfo.screen) {
				cell.clearAnimation(); //确保没有动画与此View关联，避免removeView时添加到DisappearingView中，导致下一帧被detachedFromWindow
				GLViewParent viewParent = cell.getGLParent();
				// 原来的屏幕先进行移除
				if (viewParent != null) {
					((GLViewGroup) viewParent).removeView(cell);
				}
				// 把拖拽的item添加到当前屏幕上
				cellLayout.addView(cell);

				Object tag = cell.getTag();
				// 最后对拖拽item的tag进行更新
				if (tag != null && tag instanceof ItemInfo) {
					ItemInfo info = (ItemInfo) tag;
					info.mCellX = mTargetCell[0];
					info.mCellY = mTargetCell[1];
				}
			}
		}
		// 进行放手后的动画
		doDropAnim(source, dragInfo, dragView, spanX, spanY, screenIndex, cell, resetInfo, cellLayout, dragFromScreen, false, x, y);
//		postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				GLAppFolder.getInstance().batchStartIconEditEndAnimation();
//			}
//		}, 200);
		// 动态壁纸响应放下事件
		actionToLiveWallpaper();
		return true;
	}
	
	/**
	 *  动态壁纸响应放下事件
	 */
	private void actionToLiveWallpaper() {
		getLocationOnScreen(mTempCell);
		Bundle bundle = new Bundle();
		bundle.putString(WallpaperControler.COMMAND, WallpaperControler.COMMAND_DROP);
		final int dropX = mTargetCell[0] * GLCellLayout.sCellRealWidth
				+ GLCellLayout.sCellRealWidth / 2;
		final int dropY = mTargetCell[1] * GLCellLayout.sCellRealHeight
				+ GLCellLayout.sCellRealHeight / 2;
		bundle.putInt(WallpaperControler.FIELD_COMMAND_X, mTempCell[0] + dropX);
		bundle.putInt(WallpaperControler.FIELD_COMMAND_Y, mTempCell[1] + dropY);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
				ICommonMsgId.SEND_WALLPAPER_COMMAND, -1, bundle, null);
		bundle = null;
	}
	
	@Override
	public boolean onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, DropAnimationInfo resetInfo) {
		int screen = getDestScreen();
		if (mInScrollZone != SCROLL_ZONE_NONE && getCurrentScreenView() != null) {
			GLCellLayout cellLayout = getCurrentScreenView();
			cellLayout.revertTempState();
			cellLayout.clearVisualizeDropLocation();
			setCellLayoutGridState(getCurrentScreen(), false, GLCellLayout.DRAW_STATUS_NORMAL);
			int screenCount = mScroller.getScreenCount();
			screenCount = mZeroHandler.onDropScreenCount(screenCount);
			switch (mInScrollZone) {
				case SCROLL_ZONE_LEFT :
					if (!mCycleMode && getCurrentScreen() - 1 < 0) {
						return onDrop(source, x, y, xOffset, yOffset, dragView, dragInfo,
								resetInfo, screen);
					}
					screen = getCurrentScreen() - 1 < 0 ? getCurrentScreen() - 1
							+ screenCount : getCurrentScreen() - 1;
					x = getWorkspaceWidth();
					break;
				case SCROLL_ZONE_RIGHT :
					
					if (!mCycleMode && getCurrentScreen() + 1 >= screenCount) {
						return onDrop(source, x, y, xOffset, yOffset, dragView, dragInfo,
								resetInfo, screen);
					}
					screen = getCurrentScreen() + 1 >= screenCount
							? getCurrentScreen() + 1 - screenCount
							: getCurrentScreen() + 1;
					x = 0;
					break;
			}
			int spanX = 1;
			int spanY = 1;
			if (dragInfo instanceof ScreenAppWidgetInfo) {
				spanX = ((ScreenAppWidgetInfo) dragInfo).mSpanX;
				spanY = ((ScreenAppWidgetInfo) dragInfo).mSpanY;
			}
			boolean isExistVacant = ScreenUtils.findVacant(new int[2], spanX, spanY, screen, this);
			if (!isExistVacant) {
				ToastUtils.showToast(R.string.no_more_room, Toast.LENGTH_SHORT);
				return false;
			}
			cellLayout = (GLCellLayout) getChildAt(screen);
			if (cellLayout != null) {
				mDragController.setKeepDragging(true);
				resetInfo.setDuration(DropAnimation.DURATION_100);
				setCurrentDropLayout(cellLayout);
				if (GLAppFolder.getInstance().isFolderOpened()) {
					mNeedSnapToScreenAfterFolderClosed = screen;
				} else {
					snapToScreen(screen, false, 300);
				}
			}
		}
		return onDrop(source, x, y, xOffset, yOffset, dragView, dragInfo, resetInfo, screen);
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		mDragIntoDock = false;
		setCellLayoutGridState(getCurrentScreen(), true, -1);
		if (GLAppFolder.getInstance().isFolderOpened()) {
			mDragController.setDragScroller(this);
			return;
		}
		QuickActionMenuHandler.getInstance().onDragEnter(source, x, y, xOffset, yOffset, dragView, dragInfo);
//		mIsDragging = true;
		mDragViewType = dragView.getDragViewType();
		// 获取当前拖拽的屏幕
		GLCellLayout dest = getCurrentDropLayout();
		boolean changeScreen = false;
		if (dest != mDragTargetLayout) {
			// 如果拖拽屏发生了改变，通知并进行重新设置
			if (mDragTargetLayout != null) {
				mDragTargetLayout.onDragExit();
				changeScreen = true;
			}
			mDragTargetLayout = dest;
		}

		if (mDragTargetLayout != null) {
			// 如果当前拖拽屏发生了改变，通知
			if (changeScreen) {
				mDragTargetLayout.onDragEnter();
			}
			// 设置额外绘制内容的标志
			// mDragTargetLayout.setDrawExtraFlag(getCellLayoutDrawExtraFlag(true, mCurrentScreen));
		}
		//每次拖拽都要重新设一次
		mDragController.setDragScroller(this);
		if (!mShell.isViewVisible(IViewId.DELETE_ZONE)) {
			mShell.show(IViewId.DELETE_ZONE, true);
		}
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		if (GLAppFolder.getInstance().isFolderOpened()) {
			return;
		}

		mDragViewType = dragView.getDragViewType();

		//		final int touchSlop = ViewConfiguration.getTouchSlop();
//		if (mNeedHideMenu) {
//			if (mDragStartX > 0
//					&& mDragStartY > 0
//					&& ((Math.abs(mDragStartX - x) > GLCellLayout.sCellRealWidth / 3 || Math
//							.abs(mDragStartY - y) > GLCellLayout.sCellRealHeight / 3))) {
//				// 取消弹出菜单
//				if (getGLParent() instanceof GLScreen) {
//					cancelQuickMenu();
//					mDragStartX = -1;
//					mDragStartY = -1;
//				}
//				setNeedHideQuickMenu(false);
//			}
//		}
		
		if (sLayoutScale == 1.0f) {
			QuickActionMenuHandler.getInstance().onDragOver(x, y, dragView);
		}
		
		int realX = x;
		int realY = y;
		if (mScaleState != STATE_NORMAL) {
			float[] realXY = new float[2];
			// 先转换为真实值
			virtualPointToReal(x, y, realXY);
			realX = (int) realXY[0];
			realY = (int) realXY[1];
		}

		realX = (int) (realX - xOffset);
		realY = (int) (realY - yOffset);

		// 获取当前拖拽的屏幕
		GLCellLayout cellLayout = getCurrentDropLayout();
		if (cellLayout == null) {
			return;
		}

		if (checkScreen(cellLayout, realX, realY, x, y)) {
			return;
		}

		if (cellLayout.mState == GLCellLayout.STATE_NORMAL_CONTENT) {
			if (cellLayout != mDragTargetLayout) {
				setCurrentDropLayout(cellLayout);
			}
			// if (!isInsideWorkspace(point)) {
			// cellLayout.clearVisualizeDropLocation();
			// cellLayout.revertTempState();
			// return;
			// }
			GLView cell = mDragInfo != null ? mDragInfo.cell : dragView;
			if (cell == null) {
				return;
			}
			ViewGroup.LayoutParams lp = cell.getLayoutParams();
			int cellHSpan = 1; // 从文件夹内拖出来的一定是1行1列
			int cellVSpan = 1;
			if (lp instanceof GLCellLayout.LayoutParams) {
				GLCellLayout.LayoutParams dragViewLp = (GLCellLayout.LayoutParams) lp;
				cellHSpan = dragViewLp.cellHSpan;
				cellVSpan = dragViewLp.cellVSpan;
			}

			mDragViewVisualCenter[0] = realX;
			mDragViewVisualCenter[1] = realY;
			// 自动添加图标的代码？
			// if (isAddState) {
			// if (!isOverWorkspace(realX, realY)) {
			// cellLayout.caculateCellXY(dragView, mTargetCell, realX, realY);
			// return;
			// }
			// }
			cellLayout.findAllVacantCells(null, cell);
			mTargetCell = cellLayout.findNearestArea(realX, realY, cellHSpan, cellVSpan,
					mTargetCell);
			setCurrentDropOverCell(mTargetCell[0], mTargetCell[1]);

			float targetCellDistance = cellLayout.getDistanceFromCell(realX, realY, mTargetCell);

			final GLView dragOverView = cellLayout.getChildViewByCell(mTargetCell);
			manageFolderFeedback(cellLayout, mTargetCell, targetCellDistance, dragOverView);

			int minSpanX = cellHSpan;
			int minSpanY = cellVSpan;
			boolean nearestDropOccupied = cellLayout.isNearestDropLocationOccupied(realX, realY,
					minSpanX, minSpanY, cell, mTargetCell);
			if (!nearestDropOccupied) {
				cellLayout.caculateCellXY(cell, mTargetCell, minSpanX, minSpanY, realX, realY);
			} else if ((mDragMode == DRAG_MODE_NONE || mDragMode == DRAG_MODE_REORDER)
					&& !mReorderAlarm.alarmPending()
					&& (mLastReorderX != mTargetCell[0] || mLastReorderY != mTargetCell[1])) {
				ReorderAlarmListener listener = new ReorderAlarmListener(mDragViewVisualCenter,
						minSpanX, minSpanY, minSpanX, minSpanY, cell);
				mReorderAlarm.setOnAlarmListener(listener);
				mReorderAlarm.setAlarm(REORDER_TIMEOUT);
			}

			if (mDragMode == DRAG_MODE_CREATE_FOLDER || mDragMode == DRAG_MODE_ADD_TO_FOLDER
					|| !nearestDropOccupied) {
				cellLayout.revertTempState();
				if (mDragMode == DRAG_MODE_CREATE_FOLDER) {
					cellLayout.setOutlineVisible(false); // 合并文件夹背景打开不显示轮廓
				}
			}
		}
	}

//	public void setNeedHideQuickMenu(boolean flag) {
//		mNeedHideMenu = false;
//	}

	@Override
	public void onDragExit(DragSource source, DropTarget nextTarget, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
//		mIsDragging = false;
		if (!(nextTarget instanceof GLDock)) {
			setCellLayoutGridState(getCurrentScreen(), false, -1);
			mDragIntoDock = false;
		} else {
			mDragIntoDock = true;
		}
		GLCellLayout cellLayout = getCurrentDropLayout();
		if (cellLayout != null) {
			cellLayout.clearVisualizeDropLocation();
			cellLayout.revertTempState();
			cellLayout.invalidate();
		}
		if (!mIsAlreadyDrop) {
//			cancelQuickMenu();
		} else {
			mIsAlreadyDrop = false;
		}
		mDragViewType = -1;
		cleanupReorder(true);
		cleanupAddToFolder();
		cleanupFolderCreation();
		setDragMode(DRAG_MODE_NONE);
		mDragTargetLayout = null;
	}

	@Override
	public void onDragMove(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
	}

	@Override
	public void setTopViewId(int id) {
		mTopViewId = id;
	}

	@Override
	public int getTopViewId() {
		return mTopViewId;
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, Rect recycle) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 屏幕加载完成之后，标记所有屏幕的占用标记
	 */
	protected void markAllCellOccupied() {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			GLCellLayout layout = (GLCellLayout) getChildAt(i);
			if (layout != null) {
				layout.markOccupied(null);
			}
		}
	} // end markAllCellOccupied

	/*-------------------------- 以下代码是拖拽的新逻辑 ------------------------------*/

	private static final int FOLDER_BG_SHOW_TIMEOUT = 0; // 创建新文件夹的等待时间为0
	private static final int REORDER_TIMEOUT = 250; // 进行图标抖动的等待时间
	private final Alarm mFolderBgShowAlarm = new Alarm();
	private final Alarm mReorderAlarm = new Alarm();
	/*
	 * private FolderRingAnimator mDragFolderRingAnimator = null; private FolderIcon
	 * mDragOverFolderIcon = null;
	 */
	private boolean mCreateUserFolderOnDrop = false;
	private float mMaxDistanceForFolderCreation;
	private float[] mDragViewVisualCenter = new float[2];
	private int mLastReorderX = -1;
	private int mLastReorderY = -1;

	// Related to dragging, folder creation and reordering
	private static final int DRAG_MODE_NONE = 0;
	protected static final int DRAG_MODE_CREATE_FOLDER = 1;
	protected static final int DRAG_MODE_ADD_TO_FOLDER = 2;
	protected static final int DRAG_MODE_REORDER = 3;
	private int mDragMode = DRAG_MODE_NONE;

	//	private int mTotalOffsetUp;
	//	private int mTotalOffsetDown;
	//	private int[] mLoc = new int[2];

	private int mPrimeEffectEntranceId = -1;
	private int mPreviousEffectType = Integer.MAX_VALUE;

	private void setCurrentDropLayout(GLCellLayout layout) {
		if (mDragTargetLayout != null) {
			mDragTargetLayout.revertTempState();
			// mDragTargetLayout.onDragExit();
		}
		mDragTargetLayout = layout;
		if (mDragTargetLayout != null) {
			// mDragTargetLayout.onDragEnter();
		}
		cleanupReorder(true);
		// 文件夹的处理
		// cleanupFolderCreation();
		setCurrentDropOverCell(-1, -1);
	}

	private void setCurrentDropOverCell(int x, int y) {
		if (x != mDragOverX || y != mDragOverY) {
			mDragOverX = x;
			mDragOverY = y;
			setDragMode(DRAG_MODE_NONE);
		}
	}

	private void manageFolderFeedback(GLCellLayout targetLayout, int[] targetCell, float distance,
			GLView dragOverView) {
		boolean userFolderPending = willCreateUserFolder(targetLayout, targetCell, distance, false);
		if (mDragMode == DRAG_MODE_NONE && userFolderPending && !mFolderBgShowAlarm.alarmPending()) {
			mFolderBgShowAlarm.setOnAlarmListener(new FolderBgShowAlarmListener(targetLayout,
					targetCell[0], targetCell[1], DRAG_MODE_CREATE_FOLDER));
			mFolderBgShowAlarm.setAlarm(FOLDER_BG_SHOW_TIMEOUT);
			return;
		}

		boolean willAddToFolder = willAddToExistingUserFolder(targetLayout, targetCell, distance);
		if (mDragMode == DRAG_MODE_NONE && willAddToFolder && !mFolderBgShowAlarm.alarmPending()) {
			mFolderBgShowAlarm.setOnAlarmListener(new FolderBgShowAlarmListener(targetLayout,
					targetCell[0], targetCell[1], DRAG_MODE_ADD_TO_FOLDER));
			mFolderBgShowAlarm.setAlarm(FOLDER_BG_SHOW_TIMEOUT);
			if (targetLayout != null) {
				// targetLayout.clearVisualizeDropLocation();
				targetLayout.setOutlineVisible(false);
			}
			return;
		}

		// if (willAddToFolder && mDragMode == DRAG_MODE_NONE) {
		/*
		 * mDragOverFolderIcon = (FolderIcon) dragOverView; mDragOverFolderIcon.onDragEnter(info);
		 */
		// if (targetLayout != null) {
		// targetLayout.clearVisualizeDropLocation();
		// targetLayout.setOutlineVisible(false);
		// }
		// setDragMode(DRAG_MODE_ADD_TO_FOLDER);
		// return;
		// }

		if (mDragMode == DRAG_MODE_ADD_TO_FOLDER && !willAddToFolder) {
			setDragMode(DRAG_MODE_NONE);
		}
		if (mDragMode == DRAG_MODE_CREATE_FOLDER && !userFolderPending) {
			setDragMode(DRAG_MODE_NONE);
		}

		return;
	}

	// 是否创建新的文件夹
	boolean willCreateUserFolder(GLCellLayout target, int[] targetCell, float distance,
			boolean considerTimeout) {
		if (distance > mMaxDistanceForFolderCreation) {
			return false;
		}
		GLView dropOverView = target.getChildViewByCell(targetCell);

		if (dropOverView != null) {
			GLCellLayout.LayoutParams lp = (GLCellLayout.LayoutParams) dropOverView
					.getLayoutParams();
			if (lp.useTmpCoords && (lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.cellY)) {
				return false;
			}
		}

		boolean hasntMoved = false;
		if (mDragInfo != null) {
			hasntMoved = dropOverView == mDragInfo.cell;
		}

		if (dropOverView == null || hasntMoved || (considerTimeout && !mCreateUserFolderOnDrop)) {
			return false;
		}

		boolean aboveShortcut = dropOverView.getTag() instanceof ShortCutInfo;
		boolean aboveUserFolder = dropOverView.getTag() instanceof UserFolderInfo;
		boolean willBecomeShortcut = mDragViewType == DragView.DRAGVIEW_TYPE_SCREEN_ICON
				|| mDragViewType == DragView.DRAGVIEW_TYPE_DOCK_ICON
				|| mDragViewType == DragView.DRAGVIEW_TYPE_APPDRAWER_ICON
				|| mDragViewType == DragView.DRAGVIEW_TYPE_SCREEN_USERFOLDER_ELEMENT;

		// 符合两个icon合成一新文件夹
		if (aboveShortcut && willBecomeShortcut) {
			updateDragTargetIcon(dropOverView);
			return true;
		}
		// 符合合并两个文件夹
		else if (aboveUserFolder
				&& (mDragViewType == DragView.DRAGVIEW_TYPE_SCREEN_USERFOLDER
						|| mDragViewType == DragView.DRAGVIEW_TYPE_APPDRAWER_FOLDER || mDragViewType == DragView.DRAGVIEW_TYPE_DOCK_FOLDER)) {
			updateDragTargetIcon(dropOverView);
			return true;
		}
		return false;
	}

	// 是否放进已存在的文件夹
	boolean willAddToExistingUserFolder(GLCellLayout target, int[] targetCell, float distance) {
		if (distance > mMaxDistanceForFolderCreation) {
			return false;
		}
		GLView dropOverView = target.getChildViewByCell(targetCell);

		if (dropOverView != null) {
			GLCellLayout.LayoutParams lp = (GLCellLayout.LayoutParams) dropOverView
					.getLayoutParams();
			if (lp.useTmpCoords && (lp.tmpCellX != lp.cellX || lp.tmpCellY != lp.cellY)) {
				return false;
			}
			boolean addToFolder = mDragViewType == DragView.DRAGVIEW_TYPE_SCREEN_ICON
					|| mDragViewType == DragView.DRAGVIEW_TYPE_APPDRAWER_ICON
					|| mDragViewType == DragView.DRAGVIEW_TYPE_DOCK_ICON
					|| mDragViewType == DragView.DRAGVIEW_TYPE_SCREEN_USERFOLDER_ELEMENT;
			if (mDragInfo != null) {
//				addToFolder = (dropOverView != mDragInfo.cell);
				addToFolder = addToFolder && dropOverView != mDragInfo.cell;
			}
			if (addToFolder && dropOverView instanceof GLScreenFolderIcon) {
				// FolderIcon folderIcon = (FolderIcon) dropOverView;
				updateDragTargetIcon(dropOverView);
				// 文件夹没有个数限制
				return true;
				// if (fi.acceptDrop(dragInfo)) {
				// return true;
				// }
			}
		}
		return false;
	}

	void setDragMode(int dragMode) {
		if (dragMode != mDragMode) {
			if (dragMode == DRAG_MODE_NONE) {
				cleanupAddToFolder();
				cleanupReorder(false);
				cleanupFolderCreation();
			} else if (dragMode == DRAG_MODE_ADD_TO_FOLDER) {
				cleanupReorder(true);
				// cleanupFolderCreation();
			} else if (dragMode == DRAG_MODE_CREATE_FOLDER) {
				// cleanupAddToFolder();
				cleanupReorder(true);
			} else if (dragMode == DRAG_MODE_REORDER) {
				cleanupAddToFolder();
				cleanupFolderCreation();
			}
			mDragMode = dragMode;
		}
	}

	protected int getDragMode() {
		return mDragMode;
	}

	private void cleanupAddToFolder() {
		/*
		 * if (mDragOverFolderIcon != null) { mDragOverFolderIcon.onDragExit(); mDragOverFolderIcon
		 * = null; }
		 */
		if (mDragTargetIcon != null) {
			mDragTargetIcon.cancleFolderReady();
			mDragTargetIcon = null;
		}
	}

	private void cleanupFolderCreation() {
		/*
		 * if (mDragFolderRingAnimator != null) { mDragFolderRingAnimator.animateToNaturalState(); }
		 */
		if (mDragTargetIcon != null) {
			mDragTargetIcon.cancleFolderReady();
			mDragTargetIcon = null;
		}
		mFolderBgShowAlarm.cancelAlarm();
	}

	private void cleanupReorder(boolean cancelAlarm) {
		// Any pending reorders are canceled
		if (cancelAlarm) {
			mReorderAlarm.cancelAlarm();
		}
		mLastReorderX = -1;
		mLastReorderY = -1;
	}

	/**
	 * 
	 * 类描述:内部类，文件夹背景展现的监听器 功能详细描述:
	 * 
	 * @date [2012-9-7]
	 */
	class FolderBgShowAlarmListener implements OnAlarmListener {
//		GLCellLayout mLayout;
		int mCellX;
		int mCellY;
		int mType;

		public FolderBgShowAlarmListener(GLCellLayout layout, int cellX, int cellY, int type) {
//			this.mLayout = layout;
			this.mCellX = cellX;
			this.mCellY = cellY;
			this.mType = type;
		}

		@Override
		public void onAlarm(Alarm alarm) {
			/*
			 * if (mDragFolderRingAnimator == null) { mDragFolderRingAnimator = new
			 * FolderRingAnimator(ApplicationProxy.getContext(), null); }
			 * mDragFolderRingAnimator.setFolderBg(ApplicationProxy.getContext());
			 * mDragFolderRingAnimator.setCell(mCellX, mCellY);
			 * mDragFolderRingAnimator.setCellLayout(mLayout);
			 * mDragFolderRingAnimator.animateToAcceptState();
			 * mLayout.showFolderAccept(mDragFolderRingAnimator);
			 */
			// mLayout.clearVisualizeDropLocation();
			if (mDragTargetIcon != null) {
				mDragTargetIcon.readyForFolder(true);
				//				mLayout.setOutlineVisible(false);
				setDragMode(mType);
			}
		}

		@Override
		public void clean() {
			// TODO Auto-generated method stub
		}
	}

	/**
	 * 
	 * 类描述:内部类，图标/widget抖动的监听器 功能详细描述:
	 * 
	 * @date [2012-9-7]
	 */
	class ReorderAlarmListener implements OnAlarmListener {
		float[] mDragViewCenter;
		int minSpanX, minSpanY, mSpanX, mSpanY;
		GLView mChild;

		public ReorderAlarmListener(float[] dragViewCenter, int minSpanX, int minSpanY, int spanX,
				int spanY, GLView child) {
			this.mDragViewCenter = dragViewCenter;
			this.minSpanX = minSpanX;
			this.minSpanY = minSpanY;
			this.mSpanX = spanX;
			this.mSpanY = spanY;
			this.mChild = child;
		}

		@Override
		public void onAlarm(Alarm alarm) {
			GLCellLayout targetLayout = mDragTargetLayout;
			if (targetLayout == null) {
				targetLayout = getCurrentScreenView();
				return;
			}
			mTargetCell = targetLayout.findNearestArea((int) mDragViewVisualCenter[0],
					(int) mDragViewVisualCenter[1], mSpanX, mSpanY, mTargetCell);
			mLastReorderX = mTargetCell[0];
			mLastReorderY = mTargetCell[1];

			int[] resultSpan = new int[2];
			mTargetCell = targetLayout.createArea((int) mDragViewVisualCenter[0],
					(int) mDragViewVisualCenter[1], minSpanX, minSpanY, mSpanX, mSpanY, mChild,
					mTargetCell, resultSpan, GLCellLayout.MODE_DRAG_OVER);

			if (mTargetCell[0] < 0 || mTargetCell[1] < 0) {
				targetLayout.revertTempState();
			} else {
				setDragMode(DRAG_MODE_REORDER);
			}
			targetLayout.caculateCellXY(mChild, mTargetCell, minSpanX, minSpanY, (int) mDragViewVisualCenter[0],
					(int) mDragViewVisualCenter[1]);
		}

		@Override
		public void clean() {
			mChild = null;
		}

	}

	/**
	 * 
	 * @return 当前主屏
	 */
	public int getMainScreen() {
		return mMainScreen;
	}
	
	@Override
	public void addView(GLView child) {
		if (!(child instanceof GLCellLayout)) {
			return;
		}
		initCellLayout((GLCellLayout) child);
		super.addView(child);
	}

	@Override
	public void addView(GLView child, int index) {
		if (!(child instanceof GLCellLayout)) {
			return;
		}
		initCellLayout((GLCellLayout) child);
		super.addView(child, index);
	}

	@Override
	public void addView(GLView child, int index, LayoutParams params) {
		if (!(child instanceof GLCellLayout)) {
			return;
		}
		initCellLayout((GLCellLayout) child);
		super.addView(child, index, params);
	}

	@Override
	public void addView(GLView child, int width, int height) {
		if (!(child instanceof GLCellLayout)) {
			return;
		}
		initCellLayout((GLCellLayout) child);
		super.addView(child, width, height);
	}

	@Override
	public void addView(GLView child, LayoutParams params) {
		if (!(child instanceof GLCellLayout)) {
			return;
		}
		initCellLayout((GLCellLayout) child);
		super.addView(child, params);
	}

	protected void initCellLayout(GLCellLayout screen) {
		GLCellLayout.setRows(mDesktopRows);
		GLCellLayout.setColums(mDesktopColumns);
		GLCellLayout.setAutoStretch(mAutoStretch);
		screen.setOnLongClickListener(this);
//		screen.enableHardwareLayers();
	}
	
	/**
	 * 添加屏幕
	 * 
	 * @param screen
	 *            屏幕
	 * @param position
	 *            添加位置
	 */
	public void addScreen(GLCellLayout screen, int position) {
		this.addView(screen, position);
		//添加监听
		screen.setOnLongClickListener(this);
		screen.setPointTransListener(this);
		// 更新指示器屏幕总数
		updateIndicatorItems();
		// moveItemPositions(position, +1);
	}

	private void updateDragTargetIcon(GLView view) {
		if (view instanceof IconView) {
			if (mDragTargetIcon == null || mDragTargetIcon != view) {
				mDragTargetIcon = (IconView) view;
			}
		} // end if
	} // end updateDragTargetIcon

	/**
	 * 根据深度变化值来获取y方向的偏移值
	 * 3D夹角 angle = 22.5f， 由公式 Math.tan(angle * Math.PI / 180) 可求得 比例 k = 0.4142135
	 * 32 为相对于指示器的偏移量
	 * @return ty
	 */
	private float getTranslateY(float layoutScale) {
		
		int screenEditBoxHeight = 0;
		if (layoutScale ==  getScreenMenuScale()) {
			
			// 竖屏添加模块的缩放比例
			screenEditBoxHeight = (int) getContext().getResources().getDimension(
					R.dimen.screen_edit_box_menu);
			
		} else if (layoutScale == getScreenEditLevelOneScale()) {
			
			//添加模块一级的高度
			screenEditBoxHeight = ScreenEditController.getInstance().getNormalEditHeight();
			
		} else if (layoutScale == getScreenEditLevelTwoScale()) {
			
			//添加模块二级的高度			
			screenEditBoxHeight = ScreenEditController.getInstance().getLargeEditHeight();
		}
		
		int statusBarHeight = StatusBarHandler.getStatusbarHeight();
		int topPadding = GLCellLayout.getTopPadding();
		int bottomPadding = GLCellLayout.getBottomPadding();
		if (Machine.IS_SDK_ABOVE_KITKAT && Machine.canHideNavBar()) {
			int height = DrawUtils.sHeightPixels;
			int space = (int) (height - height * layoutScale) / 2;
			int upSpace = (int) (space - statusBarHeight + topPadding * layoutScale);
			int downSpace = (int) (space - screenEditBoxHeight - DrawUtils.getNavBarHeight()
					+ bottomPadding * layoutScale + DrawUtils.getNavBarHeight() * layoutScale);
			int ty = (upSpace - downSpace) / 2;
			ty = (int) (ty / layoutScale);
			return ty;
		} else {
			int height = getHeight();
			int space = (int) (height - height * layoutScale) / 2;
			int upSpace = (int) (space - statusBarHeight + topPadding * layoutScale);
			int downSpace = (int) (space + bottomPadding * layoutScale - screenEditBoxHeight);
			int ty = (upSpace - downSpace) / 2;
			ty = (int) (ty / layoutScale);
			return ty;
		}
	}

	/**
	 * 根据深度变化值来获取对应的缩放值
	 * 
	 * @return scale
	 */
	private float getScaleValue(float depth) {
		final GLContentView rootView = getGLRootView();
		return rootView.getProjectScale(depth);
	}

	/**
	 * 根据缩放值来获取对应的深度变化值
	 * 
	 * @return scale
	 */
	private float getDepthValue(float scale) {
		final GLContentView rootView = getGLRootView();
		return rootView.getDepthForProjectScale(scale);
	}

	@Override
	public boolean virtualPointToReal(float x, float y, float[] real) {
		boolean ret = true;
		if (sLayoutScale < 1.0f) {
			final GLContentView rootView = getGLRootView();
			rootView.unprojectFromReferencePlane(x, -y, mTranslateZ, real);
			real[1] -= mTranslateY;
			real[1] *= -1;
		} else {
			real[0] = x;
			real[1] = y;
			ret = false;
		}
		return ret;
	} // end virtualPointToReal

	@Override
	public boolean realPointToVirtual(float x, float y, float[] virtual) {
		boolean ret = true;
		if (sLayoutScale < 1.0f) {
			final GLContentView rootView = getGLRootView();
			rootView.projectFromWorldToReferencePlane(x, -y + mTranslateY, mTranslateZ, virtual);
			virtual[1] *= -1;
		} else {
			virtual[0] = x;
			virtual[1] = y;
			ret = false;
		}
		return ret;
	}
	
	public float getTranslateY() {
		return mTranslateY;
	}

	public float getTranslateZ() {
		return mTranslateZ;
	}

	/**
	 * 自动播放特效
	 * 
	 * @param type
	 *            指定的特效类型（此处自提供播放，不存进数据库）
	 */
	public void effectorAutoShow(int type) {
		setEffector(type);
		mShowAutoEffect1 = true;
		mNextDestScreen = mCurrentScreen;
		if (mCurrentScreen == 0) {
			int dest = mCurrentScreen + 1;
			snapToScreen(dest, false, AUTO_EFFECT_TIME);
		} else {
			int dest = mCurrentScreen - 1;
			snapToScreen(dest, false, AUTO_EFFECT_TIME);
		}
	} // end effectorAutoShow
	
	public void primeEffectorAutoShow(int type, int entranceId, int previousEffectType) {
		effectorAutoShow(type);
		mPrimeEffectEntranceId = entranceId;
		mPreviousEffectType = previousEffectType;
	}
	
	/**
	 * 退出添加编辑模式
	 */
	public void exitEditState() {
		if ((mStateChangeAnimation != null && !mStateChangeAnimation.hasEnded())
				|| ScreenEditConstants.sISANIMATION) {
			return;
		}
		exitToLevel(SCREEN_TO_LEVEL_NORMAL);
		// 同时移除屏幕下方的添加模块
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.LEAVE_NEW_FOLDER_STATE, -1);
		mShell.remove(IViewId.SCREEN_EIDT, true, false);
		mExitToLevelFlag = false;
		mIsEnterEditedAddBlank = false;
	}
	
	/**
	 * 退出到添加模式的指定阶级
	 * level 只能是 SCREEN_TO_SMALL_LEVEL_ONE & SCREEN_TO_LEVEL_NORMAL & SCREEN_BACK_TO_LAST_LEVEL
	 */
	 //记录退出添加界面前的sLayoutScale,保证退出动画依然保留屏幕间的间隙,待动画执行完后恢复
	float mLastsLayoutScale ;
	
	public void exitToLevel(int level) {
		mLastsLayoutScale = sLayoutScale;
		if (mScaleState == STATE_NORMAL) {
			return;
		}
		// 如果是返回上一级则递减一
		if (level == SCREEN_BACK_TO_LAST_LEVEL) {
			level = mLastLevel -= 1;
		}
		final int state = level == SCREEN_TO_SMALL_LEVEL_ONE ? STATE_SMALL_ONE : STATE_NORMAL;
		final float scale = level == SCREEN_TO_SMALL_LEVEL_ONE
				? getScreenEditLevelOneScale()/*SCALE_FACTOR_FOR_EDIT_PORTRAIT*/
				: 1.0f;
		final float curTz = scale == 1.0f ? 0 : getDepthValue(scale);
		final float curTy = curTz == 0 ? 0 : getTranslateY(scale);
		final float tz = mTranslateZ;
		final float ty = mTranslateY;
		mStateChangeAnimation = new TranslateValue3DAnimation(0, 0, ty, curTy, tz, curTz);
		mStateChangeAnimation.setTranslateValue3DAnimationListener(this);
		if (state == STATE_NORMAL) {
			mStateChangeAnimation.setListenerAlphaChange(255, 0);
//			mLastsLayoutScale = 0;
		}
		mStateChangeAnimation.setDuration(ANIMDURATION);
		mStateChangeAnimation.setInterpolator(new DecelerateInterpolator());
		mStateChangeAnimation.setAnimationListener(new AnimationListenerAdapter() {

			@Override
			public void onAnimationStart(Animation animation) {
				notifyAnimationState(true, false);
				mExitToLevelFlag = true ;
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mScaleState = state;
				notifyAnimationState(false, true);
				mExitToLevelFlag = false ;
				if (mScaleState == STATE_NORMAL) {
					GLCellLayout.sDrawBackground = false;
					setCellLayoutGridState(getCurrentScreen(), false, -1);
					mShell.remove(IViewId.SCREEN_EIDT, true, false);
//					setEnableWidgetDrawingCache(false);
					setEnableCellLayoutDrawingCache(false);

//					if (ScreenEditConstants.sISBACKTOAPPDRAWERWIDGET) {
					if (ScreenEditConfig.sEXTERNAL_FROM_ID == ScreenEditConstants.EXTERNAL_ID_APPDRAWER_SLIDEMENU) {
						mShell.showStage(IShell.STAGE_APP_DRAWER, false, IViewId.WIDGET_MANAGE);
						ScreenEditConfig.sEXTERNAL_FROM_ID = 0;
					} else {
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
								IScreenFrameMsgId.SCREEN_RESHOW_LAST_GUIDE, 0);
					}
					//TODO：添加后会闪
					
					if (DeskSettingUtils.getIsShowZeroScreen()) {
						post(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								//退出动画完成后添加界面时把0屏和指示器添加上
								mZeroHandler.addZeroScreen();
							}
						});
					}
					requestLayout();
					post(new Runnable() {
						
						@Override
						public void run() {
							GLOrientationControler.setSmallModle(false);
							GLOrientationControler.keepOrientationAllTheTime(false);
							GLOrientationControler.setOrientationType(-1);
						}
					});
				}
			}
		});
		
		mHaveChange = false;
		sLayoutScale = scale;
		mTranslateY = curTy;
		mTranslateZ = curTz;
		if (state == STATE_NORMAL) {
			sPageSpacingX = 0;
			mClickView = null;
			removeAddScreen();
			ShellContainer.sEnableOrientationControl = true;
		} else {
			mStateChangeAnimation.setFillAfter(true);
		}
		requestLayout();
		setEnableCellLayoutDrawingCache(true);
		mZeroHandler.mGlSuperWorkspace.startAnimation(mStateChangeAnimation);   //TODO:因为滑动滑需要最大的view缩放，不然添加界面滑动的时候缩放不了
	} // end exitEditState
	
	public boolean isEditScaleAnim() {
		if (mStateChangeAnimation != null && !mStateChangeAnimation.hasEnded()) {
			return true;
		}
		return false;
	}

	/**
	 * 进入添加编辑模式
	 */
	public void enterEditState(int level) {

		if (mStateChangeAnimation != null && !mStateChangeAnimation.hasEnded()) {
			return;
		}
		
		float curTy = mTranslateY;
		float curTz = mTranslateZ;
		float scale = 1.0f;

		if (level == SCREEN_TO_SMALL_LEVEL_ONE) {
			
			if (!mIsEnterEditedAddBlank) {
				// 屏幕的个数限制为9
				if (getChildCount() < GLSenseWorkspace.MAX_CARD_NUMS) {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_ADD_BLANK_CELLLAYOUT, -1);
					mIsEnterEditedAddBlank = true;
				}
			}
			
			scale = getScreenEditLevelOneScale();
			mScaleState = STATE_SMALL_ONE;
			
		} else if (level == SCREEN_TO_SMALL_LEVEL_TWO) {
			
			if (!mIsEnterEditedAddBlank) {
				//通过判断是否进入第一层来控制此处是否需要添加一个空屏
				if (getChildCount() < GLSenseWorkspace.MAX_CARD_NUMS) {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_ADD_BLANK_CELLLAYOUT, -1);
					mIsEnterEditedAddBlank = true;
				}
			}
			
			scale = getScreenEditLevelTwoScale(); 
			mScaleState = STATE_SMALL_TWO;
		}
		
		mLastLevel = level;
		mHaveChange = false;
		float tz = getDepthValue(scale);
		sLayoutScale = scale;
		GLCellLayout.sDrawBackground = true;
		requestLayout();

		final float ty = getTranslateY(scale);
		mTranslateY = ty;
		mTranslateZ = tz;
		
		mStateChangeAnimation = new TranslateValue3DAnimation(0, 0, curTy, ty, curTz, tz);
		mStateChangeAnimation.setTranslateValue3DAnimationListener(this);
		if (mScaleState == STATE_SMALL_ONE || mScaleState == STATE_SMALL_TWO) {
			mStateChangeAnimation.setListenerAlphaChange(0, 255);
		}
		mStateChangeAnimation.setDuration(ANIMDURATION);
		mStateChangeAnimation.setFillAfter(true);
		mStateChangeAnimation.setInterpolator(new DecelerateInterpolator());
		mStateChangeAnimation.setAnimationListener(new AnimationListenerAdapter() {
			@Override
			public void onAnimationStart(Animation animation) {
				notifyAnimationState(true, true);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				notifyAnimationState(false, true);
//				setEnableCellLayoutDrawingCache(false);
			}
		});
		setEnableCellLayoutDrawingCache(true);
		mZeroHandler.mGlSuperWorkspace.startAnimation(mStateChangeAnimation);   //TODO:因为滑动滑需要最大的view缩放，不然添加界面滑动的时候缩放不了
	} // end enterEditState

	private void notifyAnimationState(boolean isStart, boolean isEnterAnimation) {
		GLViewParent parent = getGLParent().getGLParent();
		if (parent != null && parent instanceof GLScreen) {
			((GLScreen) parent).notifyAnimationState(isStart, isEnterAnimation);
		}
	}

	public void setGLViewWrapperDeferredInvalidate(boolean flag) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			GLView child = getChildAt(i);
			if (child instanceof GLCellLayout) {
				GLCellLayout layout = (GLCellLayout) child;
				layout.setGLViewWrapperDeferredInvalidate(flag);
			}
		}
	}

	/**
	 * 设置默认屏幕
	 * 
	 * @param screen
	 *            屏幕
	 */
	public void setMainScreen(int screen) {
		if (screen >= getChildCount() || screen < 0) {
			// Log.i(LOG_TAG, "Cannot reset default screen to " + screen);
			return;
		}
		mMainScreen = screen;
	}

	@Override
	public boolean onSwipe(PointInfo p, float dx, float dy) {
		if (!mScroller.isFinished() || mScaleState != STATE_NORMAL || mShell.isViewVisible(IViewId.SCREEN_PREVIEW)
				|| !mCanPerformGestureSwip) {
			return false;
		}
		mSwipDy += dy;
		if (/*(!mTouchedWidget || mTouchedSystemWidget) && */mTouchState != TOUCH_STATE_SCROLL
				&& Math.abs(mSwipDy) > mSwipTouchSlop
				&& Math.abs(mInterceptTouchVY) > mSwipVelocity) {
			setAllowLongPress(false);
			if (mSwipDy < -mSwipTouchSlop) { //向上滑
				GestureSettingInfo info = getGestureSetting(GestureSettingInfo.GESTURE_UP_ID);
				mGestureHandler.handleGesture(info);
				readyPopupDialog(info, true);
				mCanPerformGestureSwip = false;
				mSwipDy = 0;
//				IconView.resetIconPressState();
				return true;
			}
			if (mSwipDy > mSwipTouchSlop) { //向下滑
				GestureSettingInfo info = getGestureSetting(GestureSettingInfo.GESTURE_DOWN_ID);
				mGestureHandler.handleGesture(info);
				readyPopupDialog(info, false);
				mCanPerformGestureSwip = false;
				mSwipDy = 0;
//				IconView.resetIconPressState();
				return true;
			}
		}
		return false;
	}
	
	private void readyPopupDialog(GestureSettingInfo info, boolean isSwipeUp) {
		int enternceId = -1;
		//上滑屏幕预览,上滑打开应用,下滑打开应用,后第一次都不再弹出推荐Prime的对话框。(2014-01-14)
		/*if (info.mGestureAction == GlobalSetConfig.GESTURE_SELECT_APP) {
			if (isSwipeUp) {
				enternceId = 307;
			} else {
				enternceId = 308;
			}
		} else if (isSwipeUp && info.mGoShortCut == GlobalSetConfig.GESTURE_SHOW_PREVIEW) {
			enternceId = 306;
		}*/
		if (enternceId != -1) {
			// 未付费并且未弹过
			if (FunctionPurchaseManager.getInstance(getApplicationContext()).getPayFunctionState(
					FunctionPurchaseManager.PURCHASE_ITEM_QUICK_ACTIONS) == FunctionPurchaseManager.STATE_VISABLE
					&& DeskSettingUtils.getGesturePrimeDialogPreference(mContext, enternceId) != DeskSettingUtils.CONDITION_PASS_HAD_SHOW) {
				DeskSettingUtils.setGesturePrimeDialogPreference(mContext, enternceId,
						DeskSettingUtils.CONDITION_PASS_NEED_SHOW);
			}
		}
	}

	@Override
	public boolean onTwoFingerSwipe(PointInfo p, float dx, float dy, int direction) {
		if (p.getPointCount() != 2 || !mScroller.isFinished()
				|| mShell.isViewVisible(IViewId.SCREEN_PREVIEW) || mTwoFingerSwipeHandled
				|| mOnScaleHandled) {
			return false;
		}
//		IconView.resetIconPressState();
		setAllowLongPress(false);
		if (direction == OnMultiTouchGestureListener.DIRECTION_DOWN) {
			// 双指下滑打开全屏插件
			try {
				PackageManager packageManager = ApplicationProxy.getContext().getPackageManager();
				Intent intent = packageManager.getLaunchIntentForPackage(PackageName.TOUCHER_PRO);
				if (intent == null) {
					throw new Exception("no found GOTouch!");
				}
				intent = new Intent();
				intent.setClassName(PackageName.TOUCHER_PRO,
						"com.gau.go.touchhelperex.TouchHelperMainActivity");
				mShell.startActivitySafely(intent);
				mTwoFingerSwipeHandled = true;
			} catch (Exception e) {
				// 用户是否设置了不再显示全屏插件下载提示
				boolean isToucherExExist = GoAppUtils.isAppExist(mContext, PackageName.TOUCHER_EX);
				if (isToucherExExist) {
						Intent intent = new Intent();
						intent.setClassName(PackageName.TOUCHER_EX,
								"com.gau.go.touchhelperex.TouchHelperMainActivity");
						mShell.startActivitySafely(intent);
						mTwoFingerSwipeHandled = true;
					}
				}
		} else if (direction == OnMultiTouchGestureListener.DIRECTION_UP) {
			if (isGestureHasPay(GestureSettingInfo.GESTURE_SWIPEUP_ID)) {
				GestureSettingInfo info = getGestureSetting(GestureSettingInfo.GESTURE_SWIPEUP_ID);
				mGestureHandler.handleGesture(info);
				mTwoFingerSwipeHandled = true;
			}
			return true;
		}
		return true;
	}
	
	/**
	 * <br>功能简述:检查收费项是否付费
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param gestureId
	 * @return
	 */
	private boolean isGestureHasPay(int gestureId) {
		// 添加收费提示，判断是否已经付费，已付费则继续调用手势，否则弹出对话框
		FunctionPurchaseManager purchaseManager = FunctionPurchaseManager.getInstance(mContext
				.getApplicationContext());
		boolean hasPay = purchaseManager
				.isItemCanUse(FunctionPurchaseManager.PURCHASE_ITEM_QUICK_ACTIONS);
		if (hasPay) {
			return true;
		}

		GestureSettingInfo info = SettingProxy.getGestureSettingInfo(gestureId);
		if (info.mGestureAction != GlobalSetConfig.GESTURE_DISABLE) {
			if (GoAppUtils.isAppExist(mContext, LauncherEnv.Plugin.PRIME_GETJAR_KEY)) {
				FunctionPurchaseManager.getInstance(mContext.getApplicationContext())
						.showItemExpiredNotPayPaye("9",
								FunctionPurchaseManager.UNPAY_TIP_GUESTURE);
			}
		}
		return false;
	}

	@Override
	public boolean onScale(PointInfo p, float scale, float scaleX, float scaleY, float dx,
			float dy, float angle) {
		//判断是否有0屏，0屏不能进入预览界面
		if (mZeroHandler.isInZeroScreen() || !mScroller.isFinished() || mScaleState != STATE_NORMAL
				|| mShell.isViewVisible(IViewId.SCREEN_PREVIEW) || mTwoFingerSwipeHandled
				|| p.getPointCount() != 2 || mOnScaleHandled) {
			return false;
		}
//		IconView.resetIconPressState();
		setAllowLongPress(false);
		mScaleFactor *= scale;
		// if (mScaleFactor < 0.8f) { //双指向内收缩
		// MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, this,
		// IScreenPreviewMsgId.PREVIEW_SHOW,
		// -1, false);
		// }

		float offsetAngle = getOffsetAngle(angle);
		// Log.i("Test==anger="+angle, "offsetAngle: " + offsetAngle);
		if (offsetAngle > scale * 25) {
			if (Math.abs(angle) > 30 && Math.abs(angle) < (360 - 30)) {
				if (angle > 0 && angle < 180) {
					if (isGestureHasPay(GestureSettingInfo.GESTURE_ROTATECW_ID)) {
						GestureSettingInfo info = getGestureSetting(GestureSettingInfo.GESTURE_ROTATECW_ID);
						mGestureHandler.handleGesture(info);
					}
				} else {
					if (isGestureHasPay(GestureSettingInfo.GESTURE_ROTATECCW_ID)) {
						GestureSettingInfo info = getGestureSetting(GestureSettingInfo.GESTURE_ROTATECCW_ID);
						mGestureHandler.handleGesture(info);
					}
				}
				mOnScaleHandled = true;
				return true;
			}
		} else {
			if (mScaleFactor > 1.6f) {
				// mtext = "放大";
				// 放大手势暂时保留，不开放
				// mListener.handleMessage(this, IMsgType.SYNC,
				// IScreenFrameMsgId.SCREEN_PINCHOUT, 0, null, null);
				// mMultiTouchOccured = true;
				// tempShowToast(mtext, Toast.LENGTH_SHORT);
				mOnScaleHandled = true;
				return true;
			} else if (mScaleFactor < 0.8f) {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SCREEN_SHOW_PREVIEW, -1, false);
				mOnScaleHandled = true;
				return true;
			}
		}

		return true;
	}
	
	
	private float getOffsetAngle(float angle) {
		float temp = Math.abs(angle);
		temp %= 360;
		if (temp > 180) {
			temp = 360 - temp;
		}
		return temp;
	}

	private Rect mDockRect = null; //dock的区域范围

	public void setDockRect() {
		mDockRect = new Rect();
		ShellAdmin.sShellManager.getShell().getView(IViewId.DOCK).getHitRect(mDockRect);
	}

	@Override
	public boolean onDoubleTap(PointInfo p) {

		setDockRect();
		//如果在dock范围内则不响应
		if (mDockRect != null && mShell.isViewVisible(IViewId.DOCK)
				&& mDockRect.contains((int) p.getX(), (int) p.getY())) {
			return false;
		}

		if (mTouchedIndicator || mTouchedIcon || mTouchedWidget || mScaleState != STATE_NORMAL
				|| mShell.isViewVisible(IViewId.SCREEN_PREVIEW)) {
			return false;
		}
		GestureSettingInfo info = getGestureSetting(GestureSettingInfo.GESTURE_DOUBLLE_CLICK_ID);
		mGestureHandler.handleGesture(info);
		return true;
	}

	private GestureSettingInfo getGestureSetting(int key) {		
		return SettingProxy.getGestureSettingInfo(key);		
	}

	/**
	 * 是否处于编辑模式的判断 1 是 0 否
	 * @return
	 */
	public boolean isEditState() {
		return mScaleState != STATE_NORMAL;
	}

	@Override
	public void onValue(float interpolatedTime, float x, float y, float z) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScale(float scale) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAlpha(float alpha) {
		GLCellLayout.setLiveAlpha((int) alpha);
	}

	// 删除带“+”号的屏幕
	public boolean removeAddScreen() {
		Log.v("BackGround", "drawbackground removeAddScreen");
		final int lastIndex = getChildCount() - 1;
		if (lastIndex > -1) {
			GLCellLayout layout = (GLCellLayout) getChildAt(lastIndex);
			if (layout != null && layout instanceof GLCellLayout) {
				if (layout.getState() == GLCellLayout.STATE_BLANK_CONTENT) {
					removeScreen(lastIndex);
					return true;
				}
			}
		}
		return false;
	}

	public void setCycleMode(boolean cycle) {
		if (mCycleMode != cycle) {
			mCycleMode = cycle;
			ShellScreenScroller.setCycleMode(mZeroHandler.mGlSuperWorkspace, cycle);
			mScreenEffector.onAttachReserveEffector(mZeroHandler.mGlSuperWorkspace);
		}
	}

	/**
	 * 设置绘制哪些内容
	 * 
	 * @param state
	 *            {@link #DRAW_STATE_ALL} 表示背景和视图， {@link #DRAW_STATE_DISABLE}
	 *            表示不绘制任何东西， {@link #DRAW_STATE_ONLY_BACKGROUND} 表示只绘制背景。
	 */
	public void setDrawState(int state) {
		switch (state) {
			case DRAW_STATE_ALL :
			case DRAW_STATE_DISABLE :
			case DRAW_STATE_ONLY_BACKGROUND :
				mDrawState = state;
				break;
		}
	}

	private void moveItemPositions(int screen, int diff) {
		// MOVE THE REMAINING ITEMS FROM OTHER SCREENS
		int screenNum = getChildCount();
		for (int i = screen + 1; i < screenNum; i++) {
			final GLCellLayout layout = (GLCellLayout) getChildAt(i);
			layout.setScreen(layout.getScreen() + diff);
		}
	}

	/**
	 * 删除屏幕
	 * 
	 * @param screen
	 *            屏幕id
	 */
	public void removeScreen(int screen) {
		if (screen < 0 || screen >= getChildCount()) {
			return;
		}

		final GLCellLayout layout = (GLCellLayout) getChildAt(screen);
		if (layout == null) {
			return;
		}

		int childCount = layout.getChildCount();
		Go3DWidgetManager widgetManager = Go3DWidgetManager.getInstance(
				ShellAdmin.sShellManager.getActivity(), mContext);
		for (int j = 0; j < childCount; j++) {
			final GLView view = layout.getChildAt(j);
			Object tag = view.getTag();
			// DELETE ALL ITEMS FROM SCREEN
			final ItemInfo item = (ItemInfo) tag;
			if (item instanceof ScreenAppWidgetInfo) {
				final ScreenAppWidgetInfo launcherAppWidgetInfo = (ScreenAppWidgetInfo) item;
				int widgetId = launcherAppWidgetInfo.mAppWidgetId;

				if (widgetManager.isGoWidget(widgetId)) {
					//通知widget本身被移除
					widgetManager.removeWidget(widgetId);
					widgetManager.deleteWidget(widgetId);
				}
			}
		}

		// 屏幕在可循环且当前屏是第一屏时，需要重置当前屏
		boolean refresh = mCurrentScreen == 0;
		moveItemPositions(screen, -1);
		removeView(layout);
		layout.cleanup();

		int currentScreen = mCurrentScreen;
		if (screen < currentScreen) // 删除的屏幕在当前屏之前，要更新屏幕索引
		{
			currentScreen -= 1;
			refresh = true;
		} else if (screen == currentScreen) // 删除的屏幕就是当前屏，要更新屏幕索引
		{
			currentScreen = 0;
			refresh = true;
		}

		// 确保当前屏在0 ~ max之间
		mCurrentScreen = Math.max(0, Math.min(currentScreen, getChildCount() - 1));

		// 以上两种情况或者 屏幕是循环且是最后一屏（循环模式下最后一屏索引为0）也要保存屏幕索引
		if (refresh) {
			setCurrentScreen(mCurrentScreen);
		}

		if (screen <= mMainScreen) {
			int mainScreen = mMainScreen;
			if (screen < mMainScreen) {
				mainScreen -= 1;
			} else if (screen == mMainScreen) {
				mainScreen = 0;
			}
			mMainScreen = Math.max(0, Math.min(mainScreen, getChildCount() - 1));
			//			if (mListener != null) {
			//				mListener.handleMessage(this, IScreenFrameMsgId.SCREEN_SET_HOME,
			//						mMainScreen, null, null);
			//			}
		}
		updateIndicatorItems();
		// 此方法会重置当前屏幕索引，当为循环时，当前屏为"+"屏时，mCurrentScreen会置为0
		//		mScroller.setScreenCount(getChildCount()); // 同步更新，不要等到onLayout
        // 屏幕数目发生变化，发送广播通知多屏多壁纸
		sendBroadcastToMultipleWallpaper(false, true);

		// 通知功能表的屏幕预览刷新 
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_PREVIEW_BAR,
				IAppDrawerMsgId.APPDRAWER_ON_SCREEN_REMOVED, 0);
		
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_REFRESH_INDEX, -1, true);
	}
	
	/**
	 * <br>功能简述: Dock栏发起的拖拽，预处理网格状态
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param info
	 */
	private void prepareCellLayoutGrideStateFromDock(DockItemInfo info) {
		GLCellLayout cellLayout = (GLCellLayout) getChildAt(getCurrentScreen());
		if (cellLayout != null) {
			int columnNum = DockLogicControler.getInstance().getColumnNum(info.getmRowId());
			int indexInRow = info.getmIndexInRow();
			indexInRow = indexInRow < 0
					? 0
					: (indexInRow >= columnNum ? columnNum - 1 : indexInRow);
			
			float ratioX = 1.0f;
			float ratioY = 1.0f;
			if (GoLauncherActivityProxy.isPortait()) {
				ratioX = (indexInRow + 0.5f) / columnNum;
				ratioY = (cellLayout.getCountY() - 0.5f) / cellLayout.getCountY();
			} else {
				ratioX = (cellLayout.getCountX() - 0.5f) / cellLayout.getCountX();
				ratioY = 1.0f - (indexInRow + 0.5f) / columnNum;
			}
			cellLayout.calculateDragCenter(ratioX, ratioY);
		}
	}

	/**
	 * 设置指定的屏幕的网格状态
	 * 
	 * @param index
	 *            屏幕索引
	 * @param show
	 * @param drawState
	 */
	public void setCellLayoutGridState(int index, boolean show, int drawState) {
		GLCellLayout cellLayout = (GLCellLayout) getChildAt(index);
		if (cellLayout != null && cellLayout.getState() == GLCellLayout.STATE_NORMAL_CONTENT) {
			final boolean tempShow = (index == mCurrentScreen && mScaleState != STATE_NORMAL)
					? true
					: show;
			cellLayout.setDrawCross(tempShow);
			// 判断是否处于屏幕的编辑模式
			if (mScaleState != STATE_NORMAL) {
				cellLayout.setIsEditState(true);
			} else {
				cellLayout.setIsEditState(false);
			}
			if (drawState != -1) {
				cellLayout.setmDrawStatus(drawState);
			}
		}
	}

	public void checkGridState(int newScreen, int oldScreen) {
		if ((mDragController != null && mDragController.isDragging()) || mScaleState != STATE_NORMAL) {
			setCellLayoutGridState(newScreen, true, -1);
			setCellLayoutGridState(oldScreen, false, GLCellLayout.DRAW_STATUS_NORMAL);
		}
	}

	private boolean checkScreen(GLCellLayout cellLayout, int realX, int realY, int x, int y) {
		boolean ret = false;
		// 处于编辑模式需要进行的操作
		if (mScaleState != STATE_NORMAL) {
			// 判断是否在当前cellLayout区域内
			if (!cellLayout.includePoint(realX, realY)) {
				// 状态还原
				cellLayout.revertTempState();
				// 满足y坐标的范围则进行滚屏的判断
				if (realY >= GLCellLayout.getTopPadding()
						&& realY < getHeight() - GLCellLayout.getBottomPadding()) {
					if (x >= 0 && x <= sPageSpacingX) {
						long curTime = SystemClock.uptimeMillis();
						if (mCurrentScreen - 1 >= 0 && curTime - mTempTime >= SNAP_DELAY_TIME) {
							// 向左滚
							snapToScreen(mCurrentScreen - 1, false, -1);
							mTempTime = curTime;
						}
					} else if (x >= getWorkspaceWidth() * 0.5 + getWorkspaceWidth() * sLayoutScale * 0.5
							+ sPageSpacingX
							&& x <= getWorkspaceWidth()) {
						long curTime = SystemClock.uptimeMillis();
						if (mCurrentScreen + 1 < getChildCount()
								&& curTime - mTempTime >= SNAP_DELAY_TIME) {
							// 向右滚
							snapToScreen(mCurrentScreen + 1, false, -1);
							mTempTime = curTime;
						}
					}
				}
				ret = true;
			}
		}
		return ret;
	} // end checkScreen

	/**
	 * 往屏幕上添加组件的时候，如果是带“+”号的屏幕，要先转化为正常态的屏幕
	 * 
	 * @param cellLayout
	 */
	protected boolean blankCellToNormal(GLCellLayout cellLayout, boolean createBlank) {
		boolean ret = false;
		if (cellLayout.getState() == GLCellLayout.STATE_BLANK_CONTENT) {
			cellLayout.blankToNormal();
			ret = true;
			// 屏幕的个数限制为9
			if (createBlank && mScaleState != STATE_NORMAL
					&& getChildCount() < GLSenseWorkspace.MAX_CARD_NUMS) {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
						IScreenFrameMsgId.SCREEN_ADD_BLANK_CELLLAYOUT, -1);
			}
		}
		return ret && !createBlank;
	} // end blankCellToNormal

	@Override
	public void onFolderOpenEnd(int curStatus) {

	}

	@Override
	public void onFolderCloseEnd(int curStatus, BaseFolderIcon<?> baseFolderIcon, boolean needReopen) {
		if (mNeedSnapToScreenAfterFolderClosed > -1) {
			snapToScreen(mNeedSnapToScreenAfterFolderClosed, false, 300);
			mNeedSnapToScreenAfterFolderClosed = -1;
		}
	}

	@Override
	public void onFolderOpen(BaseFolderIcon<?> baseFolderIcon, boolean animate, int curStatus,
			boolean reopen) {
	}

	@Override
	public void onFolderClose(BaseFolderIcon<?> baseFolderIcon, boolean animate, int curStatus) {

	}

	@Override
	public void onFolderStatusChange(int oldStatus, int newStatus) {

	}

	@Override
	public void onFolderReLayout(final BaseFolderIcon<?> baseFolderIcon, int curStatus) {
	}

	/**
	 * Unlocks the SlidingDrawer so that touch events are processed.
	 * 
	 * @see #lock()
	 */
	public void unlock() {
		mLocked = false;
	}

	/**
	 * Locks the SlidingDrawer so that touch events are ignores.
	 * 
	 * @see #unlock()
	 */
	public void lock() {
		mLocked = true;
	}

	public boolean isLock() {
		return mLocked || GLAppFolder.getInstance().isFolderOpened();
	}
	

//	public boolean isAllowToScroll() {
//		return mAllowToScroll && !GLAppFolder.getInstance().isFolderOpened();
//	}
//
//	public void allowToScroll() {
//		mAllowToScroll = true;
//	}
//
//	public void disallowToScroll() {
//		mAllowToScroll = false;
//	}
	
	
	public void changeXY(int x, int y, int[] result) {
		float startX = (float) (getWorkspaceWidth() * (1 - sLayoutScale) * 0.5);
		float startY = (32 * DrawUtils.sHeightPixels + 0.001f) / 800;
		float minX = startX + x * sLayoutScale;
		float minY = startY + y * sLayoutScale;
		result[0] = (int) minX;
		result[1] = (int) minY;
	}

	/**
	 * 
	 * 类描述:操作widget可见性
	 * 功能详细描述:
	 * 
	 * @date  [2012-9-7]
	 */
	public static class WidgetRunnable implements Runnable {
		GLViewGroup mViewGroup = null;
		boolean mVisible;

		public WidgetRunnable(boolean visible) {
			mVisible = visible;
		}

		void setScreen(GLViewGroup viewGroup) {
			mViewGroup = viewGroup;
		}

		@Override
		public void run() {
			// 存在一些极端的现象，可能在其他的线程置空了 ShellAdmin.sShellManager 或者 mViewGroup，所以添加try-catch保护   -by Yugi 2013-06-14
			try {
				if (mViewGroup != null) {
					final Go3DWidgetManager widgetManager = Go3DWidgetManager.getInstance(
							ShellAdmin.sShellManager.getActivity(), mViewGroup.getContext());
					if (null != widgetManager) {
						widgetManager.fireVisible(mViewGroup, mVisible);
					}
					mViewGroup = null;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	/**
	* 设置背景
	* 
	* @param drawable
	* @param offset
	*/
	public void setWallpaper(Drawable drawable, int offset) {
		final boolean nullDrawable = drawable == null;
		// drawable为null表示有动态壁纸，背景一定被绘制了
		GLDrawable oldBlurBackground = null;
		GLDrawable oldBlurSrcDrawable = null;
		if (drawable != mBackground) {
			mBackground = drawable;
			oldBlurBackground = mBlurBackground;
			oldBlurSrcDrawable = mBlurSrcDrawable;
			mBlurBackground = null;
		}
		mScroller.setBackgroundAlwaysDrawn(nullDrawable);
		GLViewParent parent = getGLParent();
		if (mIsBlurBackground && parent != null
				&& ((GLView) parent).getVisibility() == GLView.INVISIBLE) {
			blurBackground();
		} else {
			mScroller.setBackground(drawable);
		}
		if (oldBlurBackground != null && oldBlurBackground != mScroller.getBackground()) {
			oldBlurBackground.clear();
			oldBlurSrcDrawable.clear();
		}
		mScroller.setScreenOffsetY(offset);
		ThemeInfoBean themeBean = ThemeManager.getInstance(mContext).getCurThemeInfoBean();
		//默认为16位的方式，当为1的时候是以32位的方式
		if (themeBean != null) {
			int drawModel = themeBean.getmDrawModel();
			if (drawModel == 1) {
				mShell.changePixelFormat(true);
			} else {
				// 跟以前的方式一样，不变
				mShell.changePixelFormat(nullDrawable);
			}
		}
		postInvalidate();
	}

	public void setWallpaperYOffset(int offset) {
		mScroller.setScreenOffsetY(offset);
	}

	public void setEnableWidgetDrawingCache(final boolean cache) {
		GLCellLayout.sEnableWidgetDrawingCache = cache;
	}
	
	public void setEnableCellLayoutDrawingCache(final boolean cache) {
		GLCellLayout.sEnableDrawingCache = cache;
		if (!cache) {
			int count = getChildCount();
			for (int i = 0; i < count; i++) {
				GLCellLayout layout = (GLCellLayout) getChildAt(i);
				layout.setDrawingCacheEnabled(cache);
			}
		}
		invalidate();
	}

	protected void setWallpaperDrawer(GLIWallpaperDrawer drawer) {
		if (mScroller != null) {
			mScroller.setWallpaperDrawer(drawer);
		}
	}

	public void onFolderFingIconToScreen(GLScreenFolderIcon folderIcon) {
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_FOLDER,
				IFolderMsgId.FOLDER_RELAYOUT, -1, folderIcon);
	}

	public void setWallpaperScroll(boolean enabled) {
		mWallpaperScrollEnabled = enabled;
		if (mScroller != null) {
			mScroller.setBackgroundScrollEnabled(enabled);
		}
	}
	
	private void updateWallpaperOffset(int scroll) {
		if (!mWallpaperScrollEnabled) {
			return;
		}

		final int count = getChildCount();
		if (count <= 0) {
			return;
		}
		final int scrollRange = getChildAt(count - 1).getLeft();
		if (scroll >= 0 && scroll <= scrollRange) {
			Bundle dataBundle = WallpaperControler.createWallpaperOffsetBundle(count, scroll,
					scrollRange);
			// 多屏多壁纸支持,将滑屏的offset传给多屏多壁纸
			MsgMgrProxy.postMessage(ShellAdmin.sShellManager.getActivity(),
					IDiyFrameIds.SCHEDULE_FRAME, ICommonMsgId.UPDATE_WALLPAPER_OFFSET, -1,
					dataBundle, null);
			dataBundle = null;
		}
	}

	public static final String MULTIPLE_WALLPAPER_CURRENT_SCREEN_NUMBER = "currentScreenNumber";
	public static final String MULTIPLE_WALLPAPER_CURRENT_SCREEN_REAL_NUMBER = "currentScreenRealNumber";
	public static final String MULTIPLE_WALLPAPER_CURRENT_SCREEN_INDEX = "currentScreenIndex";
	
	/**
	 * 发送广播给多屏多壁纸应用，通知当前屏幕数与当前屏幕下标
	 * 
	 * @param isAddBlankCellLayout
	 * @param isScreenNumberChanged
	 */
	public void sendBroadcastToMultipleWallpaper(boolean isAddBlankCellLayout,
			boolean isScreenNumberChanged) {

		// 多屏多壁纸应用都没安装，不用发广播了
		// if (!GoAppUtils.isAppExist(getContext(),
		// LauncherEnv.Plugin.MULTIPLEWALLPAPER_PKG_NAME)) {
		// return;
		// }

		int currentScreenNumber = getChildCount();
		int currentScreenRealNumber = currentScreenNumber;
		int currentScreenIndex = getCurrentScreen();

		if (isAddBlankCellLayout) {
			// 这里是长按进入编辑页面，屏幕多一屏，是添加屏幕，不算真正的屏幕，需要减去1
			currentScreenRealNumber = currentScreenNumber - 1;
		}

		Intent intent = new Intent();
		if (Machine.IS_HONEYCOMB_MR1) {
			// 3.1之后，系统的package manager增加了对处于“stopped state”应用的管理
			intent.setFlags(FLAG_INCLUDE_STOPPED_PACKAGES);
		}

		// 壁纸设置模式
		boolean isScrollMode = SettingProxy.getScreenSettingInfo().mWallpaperScroll;

		if (!isScrollMode) {
			// 如果壁纸设置是竖屏模式而不是默认模式，即屏幕不可滚动

			if (isScreenNumberChanged) {

				// 如果屏幕数目发生变化，则需要发送屏幕数目
				intent.setAction(ICustomAction.ACTION_CURRENT_WALLPAPER_NUMBER);
				intent.putExtra(MULTIPLE_WALLPAPER_CURRENT_SCREEN_REAL_NUMBER,
						currentScreenRealNumber);
			}

		} else {
			if (isScreenNumberChanged) {

				// 如果屏幕数目发生变化，则需要发送屏幕数目和当前屏幕下标
				intent.setAction(ICustomAction.ACTION_CURRENT_WALLPAPER_NUMBER_AND_CURRENT_SCREEN_INDEX);
				intent.putExtra(MULTIPLE_WALLPAPER_CURRENT_SCREEN_NUMBER, currentScreenNumber);
				intent.putExtra(MULTIPLE_WALLPAPER_CURRENT_SCREEN_REAL_NUMBER,
						currentScreenRealNumber);
				intent.putExtra(MULTIPLE_WALLPAPER_CURRENT_SCREEN_INDEX, currentScreenIndex);
			} else {

				// 如果屏幕数目没发生变化，则只需要发送当前屏幕下标
				intent.setAction(ICustomAction.ACTION_CURRENT_SCREEN_INDEX);
				intent.putExtra(MULTIPLE_WALLPAPER_CURRENT_SCREEN_INDEX, currentScreenIndex);
			}
		}
		ShellAdmin.sShellManager.getActivity().sendBroadcast(intent);
	}

	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean onDragFling(final DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, final Object dragInfo, DropAnimationInfo resetInfo, int direction,
			int velocityX, int velocityY) {
		boolean ret = false;
		if (GLAppFolder.getInstance().isFolderOpened()
				&& !GLAppFolder.getInstance().isFolderClosing()) {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_FOLDER, IFolderMsgId.FOLDER_KEEP_OPEN,
					-1, true, dragInfo);
			return false;
		}
		AnimationListener listener = null;
		if (source instanceof GLWorkspace) { // source是GLWorkspace
			listener = new AnimationListenerAdapter() {

				@Override
				public void onAnimationEnd(Animation animation) {
					GLViewParent parent = getGLParent().getGLParent();
					// 删除桌面item
					if (parent != null && parent instanceof GLScreen && mDragInfo != null
							&& dragInfo != null) {
						ItemInfo info = (ItemInfo) dragInfo;
						final GLView targetView = ScreenUtils.getViewByItemId(info.mInScreenId,
								mDragInfo.screen, GLWorkspace.this);
						((GLScreen) parent).deleteScreenItem(info, mDragInfo.screen, true);
						// widget的话要通知widget remove和delete
						if (info instanceof ScreenAppWidgetInfo) {
							final Go3DWidgetManager widgetManager = Go3DWidgetManager.getInstance(
									ShellAdmin.sShellManager.getActivity(), mContext);
							ScreenAppWidgetInfo widgetInfo = (ScreenAppWidgetInfo) info;
							widgetManager.removeWidget(widgetInfo.mAppWidgetId);
							widgetManager.deleteWidget(widgetInfo.mAppWidgetId);
						}
						// widget和图标删除都要cleanup
						post(new Runnable() {
							@Override
							public void run() {
								if (targetView != null) {
									targetView.cleanup();
								}
							}
						});
					}
					resetDragInfo();
					mIsFlyToDelete = false;
				}
			};
		}
		ret = IconUtils.getInstance().iconFlyToDelete(source, x, y, xOffset, yOffset, dragView,
				dragInfo, resetInfo, direction, velocityX, velocityY, listener);
		if (ret) {
			if (source instanceof GLWorkspace) {
				mIsFlyToDelete = true;
			} else if (source instanceof GLDock) {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK,
						IDockMsgId.DOCK_ON_DRAG_FLING, -1, dragInfo);
			} else if (source instanceof GLScreenFolderGridView) {
				GLAppFolderBaseGridView gridView = (GLAppFolderBaseGridView) (dragView.getOriginalView().getGLParent());
				if (gridView != null) {
					GLScreenFolderIcon folderIcon = (GLScreenFolderIcon) (gridView.getFolderIcon());
					if (folderIcon != null) {
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
								IFolderMsgId.SCREEN_FOLDER_ON_DRAG_FLING, -1, dragInfo, folderIcon);
					}
				}
			} else if (source instanceof GLDockFolderGridVIew) {
				GLAppFolderBaseGridView gridView = (GLAppFolderBaseGridView) (dragView.getOriginalView().getGLParent());
				if (gridView != null) {
					GLDockFolderIcon folderIcon = (GLDockFolderIcon) (gridView.getFolderIcon());
					if (folderIcon != null) {
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK,
								IDockMsgId.DOCK_FOLDER_ON_DRAG_FLING, -1, dragInfo, folderIcon);
					}
				}
			} else if (source instanceof GLAppDrawerFolderGridView) {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
						IAppDrawerMsgId.APPDRAWER_ICON_ON_DRAG_FLING, 1, dragInfo);
			} else if (source instanceof GLAllAppGridView) {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
						IAppDrawerMsgId.APPDRAWER_ICON_ON_DRAG_FLING, 0, dragInfo);
			}
			resetInfo.setNeedToShowCircle(false);
		}
//		GLAppFolder.getInstance().batchStartIconEditEndAnimation();
		return ret;
	}
	
	@Override
	public boolean onDragTwoFingerSwipe(DragSource source, PointInfo p, float dx, float dy,
			int direction) {
		return false;
	}

	@Override
	public boolean onDragScale(DragSource source, PointInfo p, float scale, float scaleX,
			float scaleY, float dx, float dy, float angle) {
		return false;
	}
	
	@Override
	public boolean onDragMultiTouchEvent(DragSource source, MotionEvent ev) {
		if (mShell.getCurrentStage() != IShell.STAGE_SCREEN) {
			return false;
		}
		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		switch (action) {
			case MotionEvent.ACTION_POINTER_UP : {
				int pid = ev.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
				if (pid == 1) {
					if (null == mPointerDownEvent) {
						break;
					}
					float upX = ev.getX(pid);
//					float upY = ev.getY(pid);
					float downX = mPointerDownEvent.getX(pid);
//					float downY = mCurrentDownEvent.getY(pid);
					final float velocityX = upX - downX;
//					final float velocityY = upY - downY;
				if (Math.abs(velocityX) > mMinimumFlingVelocity) {

					int currentScreen = getCurrentScreen();
					if (velocityX > 0) {
						currentScreen = mZeroHandler
								.onDragMultiTouchEventbeyongZero();
						if (currentScreen == -999 || mDragIntoDock) {
							return false;
						} else {
							snapToScreen(currentScreen - 1, false, -1);
						}
					} else {
						currentScreen = mZeroHandler
								.onDragMultiTouchEventBelowZero();
						if (currentScreen == -999 || mDragIntoDock) {
							return false;
						} else {
							snapToScreen(currentScreen + 1, false, -1);
						}
					}
				}
				}
			}
				break;
			case MotionEvent.ACTION_POINTER_DOWN : {
				int pid = ev.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
				if (pid == 1) {
					if (mPointerDownEvent != null) {
						mPointerDownEvent.recycle();
					}
					mPointerDownEvent = MotionEvent.obtain(ev);
				}
			}
				break;
			default :
				break;
		}
		return false;
	}
	
	/**
	 * 通过索引拿GLcellLayout
	 * @param screenindex
	 * @return
	 */
	public GLCellLayout getScreenView(int screenindex) {
		if (screenindex < 0 || screenindex >= getChildCount()) {
			return null;
		}

		return (GLCellLayout) getChildAt(screenindex);
	}
	
	public void setDragTrgetIconNull() {
		mDragTargetIcon = null;
	}

	@Override
	public void hideBgAnimation(int type, GLView view, Object...params) {
		switch (type) {
			case BackgroundAnimation.ANIMATION_TYPE_ALPHA :
				float from = (Float) params[0];
				float to = (Float) params[1];
				long duration = (Long) params[2];
				AnimationTask task = new AnimationTask(false, AnimationTask.PARALLEL);
				GLCellLayout layout = getCurrentScreenView();
				int count = layout.getChildCount();
				for (int i = 0; i < count; i++) {
					GLView child = layout.getChildAt(i);
					if (child == view) {
						continue;
					}
					if (child instanceof GLWidgetContainer) {
						GLWidgetContainer container = (GLWidgetContainer) child;
						if (container.getWidget() == view) {
							continue;
						}
					}
					Animation animation = new AlphaAnimation(from, to);
					animation.setFillAfter(true);
					animation.setDuration(duration);
					task.addAnimation(child, animation, null);
				}
				if (task.isValid()) {
					GLAnimationManager.startAnimation(task);
				}
				break;
			case BackgroundAnimation.ANIMATION_TYPE_BLUR :
				GaussianBlurEffectUtils.enableBlurWithZoomOutAnimation(mShell.getView(IViewId.SCREEN), null);
				break;
			default :
				break;
		}
	}

	@Override
	public void showBgAnimation(int type, GLView view, Object... params) {
		switch (type) {
			case BackgroundAnimation.ANIMATION_TYPE_ALPHA :
				float from = (Float) params[0];
				float to = (Float) params[1];
				long duration = (Long) params[2];
				AnimationTask task = new AnimationTask(false, AnimationTask.PARALLEL);
				GLCellLayout layout = getCurrentScreenView();
				int count = layout.getChildCount();
				for (int i = 0; i < count; i++) {
					GLView child = layout.getChildAt(i);
					if (child == view) {
						continue;
					}
					if (child instanceof GLWidgetContainer) {
						GLWidgetContainer container = (GLWidgetContainer) child;
						if (container.getWidget() == view) {
							continue;
						}
					}
					Animation animation = new AlphaAnimation(from, to);
					animation.setDuration(duration);
					task.addAnimation(child, animation, null);
				}
				if (task.isValid()) {
					GLAnimationManager.startAnimation(task);
				}
				break;
			case BackgroundAnimation.ANIMATION_TYPE_BLUR :
				GaussianBlurEffectUtils.disableBlurWithZoomInAnimation(
						mShell.getView(IViewId.SCREEN), null);
				break;
			default :
				break;
		}
	}

	@Override
	public void showBgWithoutAnimation(int type, GLView view, Object... params) {
		switch (type) {
			case BackgroundAnimation.ANIMATION_TYPE_ALPHA :
				GLCellLayout layout = getCurrentScreenView();
				int count = layout.getChildCount();
				for (int i = 0; i < count; i++) {
					GLView child = layout.getChildAt(i);
					if (child == view) {
						continue;
					}
					if (child instanceof GLWidgetContainer) {
						GLWidgetContainer container = (GLWidgetContainer) child;
						if (container.getWidget() == view) {
							continue;
						}
					}
					child.clearAnimation();
					child.setAlpha(255);
				}
				break;
			case BackgroundAnimation.ANIMATION_TYPE_BLUR :
				GaussianBlurEffectUtils.disableBlurWithoutAnimation(mShell.getView(IViewId.SCREEN));
				break;
			default :
				break;
		}
	}
	
	/**
	 * 在onLayout的时候更新一个格子的size
	 */
	private void updateGridCellSize() {
		if (mScreenEffector != null && mScroller != null) {
			mScreenEffector.onSizeChanged(mScroller.getScreenWidth(), mScroller.getScreenHeight(), mScroller.getOrientation());
		}
	}
	
	protected GLCellLayout.CellInfo getDragInfo() {
		return mDragInfo;
	}
	
	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		GLCellLayout layout = getCurrentScreenView();
		if (layout != null) {
			layout.cancelLongPress();
		}
	}
	
	//--------------------------------------------以下实现进入功能表模糊背景-----------------------------//
	private BlurGLDrawable mBlurBackground = null; //模糊后的壁纸
	private GLDrawable mBlurSrcDrawable = null; // 模糊的源图（GLDrawable）
	private Drawable mBackground = null; //未模糊的壁纸
	private boolean mIsBlurBackground = false;
	
	public void blurBackground() {
		if (mIsBlurBackground) { // 只有模糊壁纸情况下才切换模糊壁纸
			if (mBlurBackground == null) {
				createBlurBackground();
			}
			if (mBlurBackground != null) {
//				mBackground = mScroller.getBackground();
				mScroller.setBackground(mBlurBackground, false);
				invalidate();
			}
		}
	}
	
	public void unblurBackground() {
		if (mIsBlurBackground) { // 只有模糊壁纸情况下才切换为非模糊壁纸，其他情况不用处理
			if (mBackground != null) {
				mScroller.setBackground(mBackground, false);
				invalidate();
			}
		}
	}
	
	private void createBlurBackground() {
		// 当前可能是动态壁纸，不创建模糊壁纸
		if (/*mBlurBackground == null && */mBackground != null) {
			mBlurSrcDrawable = GLDrawable.getDrawable(mBackground);
			mBlurBackground = new BlurGLDrawable(mBlurSrcDrawable, false);
			mBlurBackground.setBlurStep(BlurGLDrawable.getDesiredBlurStep(DrawUtils.sDensity), 1);
		}
	}
	
//	/**
//	 * 清空壁纸
//	 */
//	private void clearBackgroundDrawable() {
//		if (mBlurBackground != null) {
//			mBlurBackground.clear();
//			mBlurBackground = null;
//		}
//	}
	
	@Override
	public boolean handleChanges(MessageID msgId, Object obj1, Object obj2) {
		switch (msgId) {
			case BG_CHANGED : // 功能表背景改变
			case BLUR_BACKGROUND_CHANGED : // 模糊背景改变
				checkNeedBlurWallPaper();
				break;
		}
		return false;
	};
	
	/**
	 * 检查是否需要模糊壁纸（仅使用功能表无背景且开启模糊效果下需要模糊壁纸）
	 */
	private void checkNeedBlurWallPaper() {
		int bgType = mDataHandler.getShowBg();
		if ((bgType == FunAppSetting.BG_NON || (bgType == FunAppSetting.BG_DEFAULT && GLAppDrawerThemeControler
				.getInstance(mContext).isDefaultTheme()))
				&& 1 == mDataHandler.getBlurBackground()) {
			mIsBlurBackground = true;
			GLDrawable oldBlurBackground = mBlurBackground;
			GLDrawable oldBlurSrcDrawable = mBlurSrcDrawable;
			mBlurBackground = null;
			createBlurBackground();
//			GLViewParent parent = getGLParent().getGLParent();
//			if (parent != null && parent instanceof GLScreen) {
//				if (((GLScreen) parent).getVisibility() == GLView.INVISIBLE) {
//					blurBackground();
//				}
//			}
			
			if (mShell != null && mShell.getCurrentStage() == IShell.STAGE_APP_DRAWER) {
				blurBackground();
			}
			if (oldBlurBackground != null && oldBlurBackground != mScroller.getBackground()) {
				oldBlurBackground.clear();
				oldBlurSrcDrawable.clear();
			}
		} else {
			mIsBlurBackground = false;
			if (mBlurBackground != null) {
				Drawable drawable = mScroller.getBackground();
				if (drawable == mBlurBackground) {
					if (mBackground != null) {
						mScroller.setBackground(mBackground, false);
					}
				}
				if (mBlurBackground != mScroller.getBackground()) {
					mBlurBackground.clear();
					mBlurSrcDrawable.clear();
					mBlurBackground = null;
				}
			}
		}
	}
	
	@Override
	public Rect getScreenRect() {
		Rect rect = new Rect();
		int statusBarHeight = StatusBarHandler.isHide() ? 0 : StatusBarHandler.getStatusbarHeight();
		if (ShortCutSettingInfo.sEnable) {
			if (GoLauncherActivityProxy.isPortait()) {
				rect.set(0, statusBarHeight, GoLauncherActivityProxy.getScreenWidth(), GoLauncherActivityProxy.getScreenHeight() - DockUtil.getBgHeight());
			} else {
				rect.set(0, statusBarHeight, GoLauncherActivityProxy.getScreenWidth() - DockUtil.getBgHeight(), GoLauncherActivityProxy.getScreenHeight());
			}
		} else {
			rect.set(0, statusBarHeight, GoLauncherActivityProxy.getScreenWidth(), GoLauncherActivityProxy.getScreenHeight());
		}
		return rect;
	}

	@Override
	public void invalidateScreen() {
		invalidate();
	}

	@Override
	public Activity getLauncherActivity() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean isShowingAutoEffect() {
		return mShowAutoEffect1 || mShowAutoEffect2;
	}
	
	@Override
	protected void onVisibilityChanged(GLView changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
	}
	
	public void setScrollerAndEffector(ShellScreenScroller screenScroller, CoupleScreenEffector effector) {
		mScroller = screenScroller;
		mScreenEffector = effector;
	}
	
	/**
	 * @param 对mOtionEventForAnalys进行赋值
	 */
	public void setMOtionEventForAnalys(MotionEvent mOtionEventForAnalys) {
		mMotionEventForAnalys = mOtionEventForAnalys;
	}
	
	public IShell getShell() {
		return mShell;
	}
	
	public int getWorkspaceWidth() {
		return DrawUtils.sWidthPixels;
	}
	
	public static float getScreenEditLevelOneScale() {
		float result = SCREEN_EDIT_LEVEL_ONE_SCALE;
		if (ShortCutSettingInfo.sEnable) {
			result = SCREEN_EDIT_LEVEL_ONE_SCALE;
		} else {
			/**
			 * 新的缩放比例 ＝ （有DOCK栏的workspace度）/ 无DOCK栏的worspace高度 ＊ SCALE_FACTOR_FOR_EDIT_PORTRAIT
			 */
			result = (float) (DrawUtils.sHeightPixels - DockUtil.getBgHeight())
					/ DrawUtils.sHeightPixels;
			result *= SCREEN_EDIT_LEVEL_ONE_SCALE;
		}
		return result;
	}
	
	public static float getScreenEditLevelTwoScale() {
		float result = SCALE_EDIT_LEVEL_TWO_SCALE;
		if (ShortCutSettingInfo.sEnable) {
			result = SCALE_EDIT_LEVEL_TWO_SCALE;
		} else {
			/**
			 * 新的缩放比例 ＝ （有DOCK栏的workspace度）/ 无DOCK栏的worspace高度 ＊ SCALE_FACTOR_FOR_ADD_APP_PORTRAIT
			 */
			result = (float) (DrawUtils.sHeightPixels - DockUtil.getBgHeight())
					/ DrawUtils.sHeightPixels;
			result *= SCALE_EDIT_LEVEL_TWO_SCALE;
		}
		return result;
	}
	
	public static float getScreenMenuScale() {
		float result = 0.87f;
		if (ShortCutSettingInfo.sEnable) {
			result = 0.87f;
		} else {
			/**
			 * 新的缩放比例 ＝ （有DOCK栏的workspace度）/ 无DOCK栏的worspace高度 ＊
			 * SCALE_FACTOR_FOR_ADD_GOWIDGET_PORTRAIT
			 */
			result = (float) (DrawUtils.sHeightPixels - DockUtil.getBgHeight())
					/ DrawUtils.sHeightPixels;
			result *= 0.87f;
		}
		return result;
	}
	
	public void startIOSAnimation() {
		
		GLCellLayout cellLayout = getCurrentScreenView();
		for (int i = 0; i < cellLayout.getChildCount(); i++) {
			GLView child = cellLayout.getChildAt(i);
			GLCellLayout.LayoutParams params = (GLCellLayout.LayoutParams) child.getLayoutParams();
			
			float cellWeight = getCellWeight(params);
			long duration = 200 + (long) (cellWeight * 200);
			long startOffset = (long) (cellWeight * 120);
			
			AnimationSet animationSet = new AnimationSet(false);
			Translate3DAnimation tranAnim = new Translate3DAnimation(0, 0, 0, 0, DrawUtils.dip2px(650), 0);
			tranAnim.setDuration(duration);
			tranAnim.setStartOffset(startOffset);
			tranAnim.setInterpolator(new DecelerateInterpolator());
			
			AlphaAnimation alphaAnim = new AlphaAnimation(0.7f, 1.0f);
			alphaAnim.setDuration(duration);
			alphaAnim.setStartOffset(startOffset);
			alphaAnim.setInterpolator(new DecelerateInterpolator());
			
			animationSet.addAnimation(tranAnim);
			animationSet.addAnimation(alphaAnim);
			child.startAnimation(animationSet);
		}
	}
	
	private float getCellWeight(GLCellLayout.LayoutParams params) {
		
		float hWeight = calculateWeight(params.cellX, params.cellHSpan, GLCellLayout.sColumns, 0);
		float vWeight = calculateWeight(params.cellY, params.cellVSpan, GLCellLayout.sRows, 0);
		return (hWeight + vWeight) / 2.0f - 1.0f;
	}
	
	private float calculateWeight(int size, int span, int total, int middleOffset) {
		float result = 0f;
		int middle = total / 2 - middleOffset;
		boolean isEven = total % 2 == 0;
		int tempSpan = span;
		if (isEven) {
			while (tempSpan > 0) {
				if (size < middle && size >= 0) {
					result += middle - size;
				} else if (size >= middle && size < total) {
					result += size - middle + 1;
				}
				tempSpan--;
				size++;
			}
		} else {
			while (tempSpan > 0) {
				if (size < middle && size >= 0) {
					result += middle - size + 1;
				} else if (size >= middle && size < total) {
					result += size - middle + 1;
				} else if (size == middle) {
					result += 1;
				}
				tempSpan--;
				size++;
			}
		}
		return result / (float) span;
	}
	
	public void scaleForMenuPreAction(boolean show, boolean needAnimation) {
		showOrHideGGMenu(show, needAnimation);
		setCellLayoutGridState(getCurrentScreen(), false, -1);
	}
	
	public void showOrHideGGMenu(final boolean show, final boolean needAnimation) {
		if (show) {
			setEnableCellLayoutDrawingCache(true);
		}
		TranslateValue3DAnimation stateChangeAnimation = null;
		float curTy = mTranslateY;
		float curTz = mTranslateZ;
		float scale = show ? getScreenMenuScale() : 1.0f;
		mTranslateZ = show ? getDepthValue(scale) : 0.0f;
		mTranslateY = show ? getTranslateY(scale) : 0.0f;
		sLayoutScale = scale;

		stateChangeAnimation = new TranslateValue3DAnimation(0, 0, curTy,
				mTranslateY, curTz, mTranslateZ);
		stateChangeAnimation.setInterpolator(new DecelerateInterpolator());
		stateChangeAnimation.setFillAfter(true);
		stateChangeAnimation.setDuration(needAnimation ? ANIMDURATION : 0);
		stateChangeAnimation
				.setAnimationListener(new AnimationListenerAdapter() {

					@Override
					public void onAnimationStart(Animation animation) {
						notifyAnimationState(true, true);
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						notifyAnimationState(false, true);
						if (!show) {
							setEnableCellLayoutDrawingCache(false);
						}
					}
				});
		mZeroHandler.mGlSuperWorkspace.startAnimation(stateChangeAnimation);
	}
}
