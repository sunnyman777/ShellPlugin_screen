package com.jiubang.shell.screen;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gau.go.gostaticsdk.StatisticsManager;
import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.animation.Translate3DAnimation;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.NinePatchGLDrawable;
import com.go.gl.view.GLContentView;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.view.GLViewParent;
import com.go.gl.widget.GLImageView;
import com.go.gowidget.core.GoWidgetConstant;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.proxy.ValueReturned;
import com.go.util.AppUtils;
import com.go.util.AsyncHandler;
import com.go.util.BroadCaster.BroadCasterObserver;
import com.go.util.ConvertUtils;
import com.go.util.DeferredHandler;
import com.go.util.GoViewCompatProxy;
import com.go.util.device.Machine;
import com.go.util.file.media.ThumbnailManager;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.effector.united.IEffectorIds;
import com.go.util.log.LogConstants;
import com.golauncher.message.IAppCoreMsgId;
import com.golauncher.message.IAppDrawerMsgId;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.ICoverFrameMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IDockMsgId;
import com.golauncher.message.IFolderMsgId;
import com.golauncher.message.IScreenEditMsgId;
import com.golauncher.message.IScreenFrameMsgId;
import com.golauncher.message.IScreenPreviewMsgId;
import com.golauncher.message.IWidgetMsgId;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.appwidget.AppWidgetUpdateOptionsProxy;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingUtils;
import com.jiubang.ggheart.apps.desks.diy.DiyScheduler;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.cover.CoverFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ICustomWidgetIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.IScreenObserver;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.Search;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.controller.ScreenAdvertBusiness;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.controller.ScreenControler;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditController;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditStatistics;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.push.util.ScreenEditPushConstants;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.push.util.ScreenEditPushController;
import com.jiubang.ggheart.apps.desks.diy.frames.tipsforgl.GuideControler;
import com.jiubang.ggheart.apps.desks.diy.pref.PrefUpgradeHandler;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.apps.desks.imagepreview.ChangeIconPreviewActivity;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.apps.gowidget.AbsWidgetInfo;
import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.ggheart.apps.gowidget.GoWidgetProviderInfo;
import com.jiubang.ggheart.apps.gowidget.InnerWidgetInfo;
import com.jiubang.ggheart.apps.gowidget.ScreenEditItemInfo;
import com.jiubang.ggheart.apps.gowidget.WidgetParseInfo;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreStatisticsUtil;
import com.jiubang.ggheart.apps.systemwidget.SysSubWidgetInfo;
import com.jiubang.ggheart.common.controler.CommonControler;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.components.IQuickActionId;
import com.jiubang.ggheart.components.advert.AdvertHomeScreenUtils;
import com.jiubang.ggheart.components.advert.AdvertInfo;
import com.jiubang.ggheart.components.appmanager.AppManagerUtils;
import com.jiubang.ggheart.components.appmanager.CleanScreenInfo;
import com.jiubang.ggheart.components.renamewindow.RenameActivity;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.data.DataType;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.EffectSettingInfo;
import com.jiubang.ggheart.data.info.FavoriteInfo;
import com.jiubang.ggheart.data.info.FeatureItemInfo;
import com.jiubang.ggheart.data.info.GestureSettingInfo;
import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.RelativeItemInfo;
import com.jiubang.ggheart.data.info.ScreenAppWidgetInfo;
import com.jiubang.ggheart.data.info.ScreenFolderInfo;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.statistics.GLGoWeatherStatisticsUtil;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.data.statistics.IGoLauncherUserBehaviorStatic;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.folder.FolderConstant;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.LauncherWidgetHost;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.plugin.SecurityPoxyFactory;
import com.jiubang.ggheart.plugin.UnsupportSecurityPoxyException;
import com.jiubang.ggheart.plugin.common.OrientationTypes;
import com.jiubang.ggheart.plugin.notification.NotificationType;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderController;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderInfo;
import com.jiubang.ggheart.plugin.shell.folder.GLScreenFolderModifyActivity;
import com.jiubang.ggheart.tuiguanghuodong.double11.Double11NotificationController;
import com.jiubang.ggheart.tuiguanghuodong.double11.DoubleElevenDefaultData;
import com.jiubang.ggheart.tuiguanghuodong.double11.ScreenIconForElevenController;
import com.jiubang.ggheart.tuiguanghuodong.double11.bean.ScreenIconBeanForEleven;
import com.jiubang.shell.IShell;
import com.jiubang.shell.IView;
import com.jiubang.shell.animation.BackgroundAnimation;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.appdrawer.animation.AnimationFactory;
import com.jiubang.shell.appdrawer.component.GLAbsExtendFuncView;
import com.jiubang.shell.appdrawer.component.GLAppDrawerAppIcon;
import com.jiubang.shell.appdrawer.component.IExtendFuncViewEventListener;
import com.jiubang.shell.appdrawer.promanage.GLProManageContainer;
import com.jiubang.shell.appdrawer.recentapp.GLRecentAppContainer;
import com.jiubang.shell.common.component.GLModel3DMultiView.FolderCoverAnimationListener;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.component.ShellContainer;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.common.listener.TransformListener;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.dock.component.GLDockIconView;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.drag.DragController.DragGestureListener;
import com.jiubang.shell.drag.DragInfoTranslater;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.drag.DropTarget;
import com.jiubang.shell.effect.EffectController.EffectListener;
import com.jiubang.shell.folder.BaseFolderIcon;
import com.jiubang.shell.folder.GLAppDrawerFolderIcon;
import com.jiubang.shell.folder.GLAppFolder;
import com.jiubang.shell.folder.GLAppFolderMainView;
import com.jiubang.shell.folder.GLAppFolderMainView.FolderStatusListener;
import com.jiubang.shell.folder.GLAppFolderMainView.FolderViewAnimationListener;
import com.jiubang.shell.folder.GLDockFolderGridVIew;
import com.jiubang.shell.folder.GLDockFolderIcon;
import com.jiubang.shell.folder.GLScreenFolderGridView;
import com.jiubang.shell.folder.GLScreenFolderIcon;
import com.jiubang.shell.gesture.OnMultiTouchGestureListener;
import com.jiubang.shell.gesture.PointInfo;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.indicator.DesktopIndicator;
import com.jiubang.shell.indicator.IndicatorListener;
import com.jiubang.shell.indicator.ScreenIndicator;
import com.jiubang.shell.model.CommonImageManager;
import com.jiubang.shell.orientation.GLOrientationControler;
import com.jiubang.shell.popupwindow.PopupWindowControler;
import com.jiubang.shell.preview.GLScreenPreviewMsgBean;
import com.jiubang.shell.preview.GLSense;
import com.jiubang.shell.preview.GLSenseWorkspace;
import com.jiubang.shell.screen.back.GLBackWorkspace;
import com.jiubang.shell.screen.component.GLScreenShortCutIcon;
import com.jiubang.shell.screen.utils.ScreenUtils;
import com.jiubang.shell.scroller.ShellScreenScroller;
import com.jiubang.shell.utils.GLImageUtil;
import com.jiubang.shell.utils.GaussianBlurEffectUtils;
import com.jiubang.shell.utils.IconUtils;
import com.jiubang.shell.utils.ToastUtils;
import com.jiubang.shell.utils.ViewUtils;
import com.jiubang.shell.widget.Go3DWidgetManager;
import com.jiubang.shell.widget.GoWidgetActionReceiver;
import com.jiubang.shell.widget.component.GLWidgetContainer;
import com.jiubang.shell.widget.component.GLWidgetErrorView;
import com.jiubang.shell.widget.component.GLWidgetUpdateView;
import com.jiubang.shell.widget.component.GLWidgetView;
import com.jiubang.shell.widget.resize.GLWidgetResizeView;

/**
 * 
 * @author jiangchao
 * 
 */
public class GLScreen extends GLFrameLayout
		implements
			Search.ISearchEventListener,
			IMessageHandler,
			IView,
			PopupWindowControler.ActionListener,
			IndicatorListener,
			OnMultiTouchGestureListener,
			FolderStatusListener,
			FolderViewAnimationListener,
			BackgroundAnimation,
			DragGestureListener,
			IScreenObserver,
			BroadCasterObserver,
			IExtendFuncViewEventListener {
	// 消息 for init
	private final static int START_DESKTOP_LOADER = 1;

	// for setting
	private final static int UPDATE_DESKTOP_SETTING = 10;
	private final static int UPDATE_SCREEN_SETTING = 11;
	private final static int UPDATE_EFFECT_SETTING = 12;
	private final static int UPDATE_THEME_SETTING = 13;

	// for preview
	private final static int DELETE_SCREEN = 20;

	// for database
	private final static int REFRESH_UNINSTALL = 30;
	private final static int REFRESH_UNINSTALL_PACKAGE = 38;
	private final static int UPDATE_ITEMS_IN_SDCARD = 32;
	private final static int REFRESH_CHANGE_APP = 39;

	// for folder
	private final static int UPDATE_ALL_FOLDER = 33;
	private final static int ADD_ITEM_FROM_FOLDER_ANIMATION = 37;

	private GLDesktopBinder mGLBinder;
	private static boolean sIsLoading = false;
	
	private DragController mDragController;
	private GLSuperWorkspace mGLSuperWorkspace; //包含0屏的workspace
	private GLWorkspace mGLWorkspace;
	private GLBackWorkspace mBackWorkspace;
	private GLLinearLayout mTextLayout;
	private GLWidgetResizeView mGlWidgetResizeView;
	private AppWidgetManager mWidgetManager;
	private Go3DWidgetManager m3DWidgetManager;
	private GoWidgetActionReceiver mGoWidgetActionReceiver;

	// 指示器
	private DesktopIndicator mDesktopIndicator;
	static protected boolean sIndicatorOnBottom = false; // 指示器在屏幕底部
	static protected boolean sShowIndicator = true; // 指示器可见标识
	private boolean mTextLayoutvisiable = false; // 添加界面锁屏和toucher主题应用成功显示动画标识
	
	public static boolean sDockVisible = true; // Dock的可见标识

	private GLLayoutInflater mGlInflater = null;
	private Context mContext;
	private IShell mShell;

	private boolean mInitWorkspace = false;

	int mCurrentScreen = -1;
	private boolean mIsWidgetEditMode = false;
	// for workspaceEditor
	LauncherWidgetHost mWidgetHost;

	public ScreenControler mControler;
	
	// 用于相应弹出菜单，找到对应编辑的view
	private long mDraggedItemId = -1;

	private boolean mNeedOpenGGMenu;

	private boolean mIsKeyDown;

	// 保存将要进入添加模块tab值
	private int mCurTabId;

	// preview
	// 同步锁
	private byte[] mLockData = new byte[0];
	// 是否正在展示预览的标识，表示图标从预览进入
	public static boolean sIsShowPreview = false;
	public int mPreCurrentScreen = 0;
	public int mPreHomeScreen = 0;
	private int mPreNewCurrentScreen = 0;

	private GLView mGLWidgetView; // 被编辑的widget
	private GLScreenFolderIcon mNewFolderIcon = null; // 添加文件夹时新建的文件夹icon
	private int[] mCellPos; // 用于添加应用程序或是widget到桌面，保存添加元素的区域大小

	/**
	 * 边缘图片（用于图标拖动至边缘显示）
	 */
	private NinePatchGLDrawable mBorderBgDrawable;
	/**
	 * 当前需要绘制高亮颜色的边缘
	 */
	private int mCurBorder = BORDER_NONE;
	private static final int BORDER_NONE = 0;
	private static final int BORDER_LEFT = 1;
	private static final int BORDER_RIGHT = 2;
	private final int mNum180 = 180;
	/**
	 * 边缘图片大小
	 */
	private int mBorderBgSize;

	private boolean mNeedChangeXOffset = false; // 是否需要改变壁纸的x偏移，用于锁定壁纸；

	private final static long MAX_BITMAP_SIZE = 38 * 1024;

	private final static int COMPRESS_BITMAP_WIDTH = 84;
	private int mIndicatorHeight;

	/**
	 * 下次layout是否同时显示指示器，用于退出编辑界面动画结束后显示指示器
	 */
	private boolean mIsNextLayoutShowIndicator = false;

	private boolean mIsInOutAnimating;

	private int mVisibleState = GLView.VISIBLE;
	
	private static final long ON_GGMENU_SHOW_ANIMATION_DURATION = 300;
	
	private SpannableStringBuilder mDefaultKeySsb;

	private boolean mHandleSdCardReflush;
	
	private ScreenEditPushController mPushController;
	
	//indicate if need to request for screen count
	private boolean mNeedRequestLayout;
	
	// 标识错误的appwidget是不是被用户点击需要恢复
	private boolean mRestoreAppWidget = false;
	// 需要恢复的appwidget的信息
	private ScreenAppWidgetInfo mRestoreScreenAppWidgetInfo = null;
	
	private IView mExtendFuncView;
	
	//GGMenuShow是否正在显示
	private boolean mHasGGMenuShow = false;
	
	public GLScreen(Context context) {
		super(context);
		this.mContext = context;
		init();
	}

	private void init() {
		mWidgetHost = new LauncherWidgetHost(ShellAdmin.sShellManager.getActivity(),
				LauncherEnv.APP_WIDGET_HOST_ID);
		mWidgetManager = AppWidgetManager.getInstance(ShellAdmin.sShellManager.getActivity());
		m3DWidgetManager = Go3DWidgetManager.getInstance(ShellAdmin.sShellManager.getActivity(),
				mContext);
		mControler = new ScreenControler(ShellAdmin.sShellManager.getActivity(), mWidgetHost, this);
		setHasPixelOverlayed(false);
		mIndicatorHeight = getResources().getDimensionPixelSize(R.dimen.dots_indicator_height);
		mDefaultKeySsb = new SpannableStringBuilder();
		Selection.setSelection(mDefaultKeySsb, 0);

		mPushController = new ScreenEditPushController(ShellAdmin.sShellManager.getActivity());
		
		initViews();
		
		SettingProxy.getInstance(ApplicationProxy.getContext()).registerObserver(this);
		MsgMgrProxy.registMsgHandler(this);
		ScreenSettingInfo mScreenInfo = SettingProxy.getScreenSettingInfo();
		sIndicatorOnBottom = mScreenInfo.mIndicatorPosition
				.equals(ScreenIndicator.INDICRATOR_ON_BOTTOM);
		// 先初始化的dock数据
		sDockVisible = ShortCutSettingInfo.sEnable;

		mBorderBgDrawable = new NinePatchGLDrawable((NinePatchDrawable) mContext.getResources()
				.getDrawable(R.drawable.gl_appdrawer_border_bg));
		mBorderBgSize = ShellAdmin.sShellManager.getContext().getResources()
				.getDimensionPixelSize(R.dimen.appdrawer_border_bg_size);
		
		mControler.loadSetting();
		initWorkspace();
	}

	private void initViews() {
		mGLSuperWorkspace = new GLSuperWorkspace(mContext);
		addView(mGLSuperWorkspace, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		mDesktopIndicator = new DesktopIndicator(mContext);
		mDesktopIndicator.setIndicatorListener(this);
		addView(mDesktopIndicator, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, getResources()
				.getDimensionPixelSize(R.dimen.dots_indicator_height)));
		
		mTextLayout = new GLLinearLayout(mContext);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, DrawUtils.dip2px(80));
		lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		lp.topMargin =  StatusBarHandler.isHide() ? 0 : StatusBarHandler.getStatusbarHeight();
		mTextLayout.setBackgroundDrawable(GLImageUtil.getGLDrawable(R.drawable.gl_screen_apply_succeed_bg));
		mTextLayout.setVisibility(GLView.GONE);
		ShellTextViewWrapper txtView = new ShellTextViewWrapper(mContext);
		LinearLayout.LayoutParams txtLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		txtView.getTextView().setShadowLayer(5, 2, 0, Color.GRAY);
		txtView.setGravity(Gravity.CENTER);
		txtView.setTextColor(Color.WHITE);
		txtView.setText(R.string.locker_apply_succeed);
		txtView.setTextSize(15);
		mTextLayout.addView(txtView, txtLp);
		addView(mTextLayout, lp);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		if (mHasGGMenuShow) {
			mDesktopIndicator.setVisible(false);
		} else {
			mDesktopIndicator.setVisible(true);
			if (sIndicatorOnBottom) {
				// 正常情况
				mDesktopIndicator.setVisible(true);
				Rect rect = calculationIndicatorPosition(GLWorkspace.sLayoutScale);
				mDesktopIndicator.layout(rect.left, rect.top, rect.right,
						rect.bottom);
			} else {
				int indicatorHeight = getResources().getDimensionPixelSize(
						R.dimen.slider_indicator_height);
				int t = StatusBarHandler.isHide() ? 0 : StatusBarHandler
						.getStatusbarHeight();
				mDesktopIndicator.layout(left, t, right, t + indicatorHeight);
			}
		}

		if (mIsNextLayoutShowIndicator) {
			mIsNextLayoutShowIndicator = false;
			notifyAnimationState(false, true);
		}
	}

	/**
	 * 计算指示器位置
	 */
	private Rect calculationIndicatorPosition(float layoutScale) {
		Rect rect = new Rect();
		int screenEditBoxHeight = 0;
		
		if (layoutScale < 1.0f) {
			
			if (layoutScale == GLWorkspace.getScreenMenuScale()) {
				
				screenEditBoxHeight = (int) getContext().getResources().getDimension(
						R.dimen.screen_edit_box_menu);
				
			} else if (layoutScale == GLWorkspace.getScreenEditLevelOneScale()) {
				
				//添加模块一级的高度
				screenEditBoxHeight = ScreenEditController.getInstance().getNormalEditHeight();
				
			} else if (layoutScale == GLWorkspace.getScreenEditLevelTwoScale()) {
				
				//添加模块二级的高度
				screenEditBoxHeight = ScreenEditController.getInstance().getLargeEditHeight();
			} 
			
			//计算编辑容器的上边高度
			int statusBarHeight = StatusBarHandler.getStatusbarHeight();
			int topPadding = GLCellLayout.getTopPadding();
			int bottomPadding = GLCellLayout.getBottomPadding();
			int space = (int) (getHeight() - getHeight() * layoutScale) / 2;
			int upSpace = (int) (space - statusBarHeight + topPadding * layoutScale);
			int downSpace = (int) (space + bottomPadding * layoutScale - screenEditBoxHeight);
			int ty = (upSpace - downSpace) / 2;
			ty = upSpace - ty;
			int bottom = getBottom() - screenEditBoxHeight - ty / 2;
			rect.set(0, bottom - mIndicatorHeight / 2, getRight(), bottom);
		} else { // 非屏幕编辑状态
			if (GoLauncherActivityProxy.getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
				int b = sDockVisible ? getBottom() - DockUtil.getBgHeight() : getBottom();
				rect.set(0, b - mIndicatorHeight, getRight(), b);
			} else {
				rect.set(0, getBottom() - mIndicatorHeight + DrawUtils.dip2px(3.0f), getRight(),
						getBottom());
			}
		}
		return rect;
	}

//	@Override
//	protected void onFinishInflate() {
//		super.onFinishInflate();
//		MsgMgrProxy.registerMessageHandler(this);
//		GoSettingControler controler = GOLauncherApp.getSettingControler();
//		controler.getShortCutSettingInfo();
//		ScreenSettingInfo mScreenInfo = controler.getScreenSettingInfo();
//		sIndicatorOnBottom = mScreenInfo.mIndicatorPosition
//				.equals(ScreenIndicator.INDICRATOR_ON_BOTTOM);
//		// 先初始化的dock数据
//		sDockVisible = ShortCutSettingInfo.sEnable;
//
//		mBorderBgDrawable = new NinePatchGLDrawable((NinePatchDrawable) mContext.getResources()
//				.getDrawable(R.drawable.gl_appdrawer_border_bg));
//		mBorderBgSize = ShellAdmin.sShellManager.getContext().getResources()
//				.getDimensionPixelSize(R.dimen.appdrawer_border_bg_size);
//		
//		loadSetting();
//		initDesktopIndicator();
//		initWorkspace();
//		mTextLayout = (GLLinearLayout) this.findViewById(R.id.workspace_textlayout);
//	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		super.dispatchDraw(canvas);
		if (mCurBorder != BORDER_NONE) {
			canvas.save();
			switch (mCurBorder) {
				case BORDER_LEFT :
					canvas.rotate(mNum180, mBorderBgSize / 2, mHeight / 2);
					break;
				case BORDER_RIGHT :
					break;
			}
			mBorderBgDrawable.draw(canvas);
			canvas.restore();
		}
	}

	protected void setDockVisibleFlag(boolean visible) {
		sDockVisible = visible;
	}

	private void initWorkspace() {
		mGLWorkspace = (GLWorkspace) this.findViewById(R.id.diyworkspace);
		mGLWorkspace.setFocusable(true);

		if (mControler.getDesktopSettingInfo() != null) {
			mGLWorkspace.setmAutoStretch(mControler.getDesktopSettingInfo().getAutoFit());
			mGLWorkspace.setDesktopRowAndCol(mControler.getDesktopSettingInfo().getRows(),
					mControler.getDesktopSettingInfo().getColumns());
		}

		mGlInflater = ShellAdmin.sShellManager.getLayoutInflater();

		// 先初始化的dock数据
		SettingProxy.getShortCutSettingInfo();
	

		// 取屏幕个数
		int screenCount = mControler.getScreenCount();
		for (int i = 0; i < screenCount; i++) {
			GLCellLayout screen = new GLCellLayout(mContext);
			screen.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
			mGLWorkspace.addView(screen);
			screen.setOnLongClickListener(mGLWorkspace);
			screen.setPointTransListener(mGLWorkspace);
		}
		// 初始化屏幕特效
		mGLWorkspace.initEffector();
		handleEffectSettingChange(mControler.getEffectSettingInfo());
		ScreenSettingInfo screenSettingInfo = mControler.getScreenSettingInfo();
		// 屏幕设置
		if (screenSettingInfo != null) {
			mDesktopIndicator.setVisible(sShowIndicator);
			if (mCurrentScreen >= 0) {
				mGLWorkspace.setCurrentScreen(mCurrentScreen);
			} else {
				mCurrentScreen = screenSettingInfo.mMainScreen;
				mGLWorkspace.setCurrentScreen(mCurrentScreen);
			}
			mGLWorkspace.setMainScreen(screenSettingInfo.mMainScreen);
			mGLWorkspace.setWallpaperScroll(screenSettingInfo.mWallpaperScroll);
			mGLWorkspace.setCycleMode(screenSettingInfo.mScreenLooping);
			mDesktopIndicator.setAutoHide(screenSettingInfo.mAutoHideIndicator);

			mGLWorkspace.refreshSubView();
			setScreenIndicator(DeskSettingUtils.getIsShowZeroScreen());
			
			// dock条处于隐藏状态
			if (!ShortCutSettingInfo.sEnable) {
				mGLWorkspace.requestLayout(GLWorkspace.CHANGE_SOURCE_DOCK, 0);
			} else {
				mGLWorkspace.requestLayout(GLWorkspace.CHANGE_SOURCE_INDICATOR,
						0);
			}
			this.postInvalidate();
		}
	}

	public void setDragController(DragController dragController) {
		mDragController = dragController;
		if (mGLWorkspace != null) {
			mGLWorkspace.setDragController(mDragController);
			mDragController.addDropTarget(mGLWorkspace, IViewId.SCREEN);
			mDragController.addDragListener(mGLWorkspace);
		}
	}

	public DragController getDragController() {
		return mDragController;
	}

	public void setCurrentViewLocation(int left, int top) {
		// TODO Auto-generated method stub

	}

	public boolean isIntersectWithTarget(DropTarget target, float x, float y, PointF mPointF) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleMessage(Object sender, int msgId, int param, Object... objects) {
		boolean ret = false;
		//		Log.i("jiang", "**************"+msgId);
		switch (msgId) {
		/********************** BEGINE AppDateEngine 发送的消息 *******************************/
			case IAppCoreMsgId.EVENT_LOAD_FINISH :
				loadScreen();
				break;
			case IAppCoreMsgId.EVENT_LOAD_ICONS_FINISH :
			case IAppCoreMsgId.EVENT_LOAD_TITLES_FINISH :
				break;

			case IAppCoreMsgId.EVENT_THEME_CHANGED:
				mDesktopIndicator.applyTheme();
				if (mBackWorkspace != null) {
					ThemeInfoBean infoBean = ThemeManager
							.getInstance(ApplicationProxy.getContext()).getCurThemeInfoBean();
					if (infoBean != null) {
						boolean remove = true;
						ThemeInfoBean.MiddleViewBean middleViewBean = infoBean.getMiddleViewBean();
						if (middleViewBean != null) {
							if (middleViewBean.mHasMiddleView) {
								mBackWorkspace.setMiddleView(infoBean.getPackageName(),
										middleViewBean.mIsSurfaceView);
								remove = false;
							}
						}
						if (remove) {
							mBackWorkspace.removeMiddleView();
						}
					} // end infoBean
				} // end object
				break;
				
			case ICommonMsgId.COMMON_ON_HOME_ACTION :
				if (isVisible()) {
					ret = onHomeAction((GestureSettingInfo) objects[0]);
				}
				break;
			case ICommonMsgId.COMMON_IMAGE_CHANGED :
				int count = mGLWorkspace.getChildCount();
				for (int i = 0; i < count; i++) {
					GLView child = mGLWorkspace.getChildAt(i);
					if (child != null && child instanceof GLCellLayout) {
						GLCellLayout layout = (GLCellLayout) child;
						int size = layout.getChildCount();
						for (int j = 0; j < size; j++) {
							GLView c = layout.getChildAt(j);
							if (c instanceof IconView) {
								((IconView) c).reloadResource();
							}
						}
					}
				}
				ret = true;
				break;
			case IAppCoreMsgId.EVENT_SD_MOUNT:
			case IAppCoreMsgId.EVENT_REFLUSH_SDCARD_IS_OK : 
				if (isLoading()) {
					Log.i("EventSDCard", "================isLoading===" + msgId);
					mHandleSdCardReflush = true;
				} else {
					Log.i("EventSDCard", "================isLoading = false===" + msgId);
					onSdCardReflush();
				}
				break;
			case IAppCoreMsgId.EVENT_UPDATE_EXTERNAL_PACKAGES: 
				m3DWidgetManager.refreshExternalWidget((ArrayList<String>) objects[0]);
				break;
			case IScreenFrameMsgId.SCREEN_LONG_CLICK :
				handleLongClickAction(objects);
				// mGLWorkspace.requestLayout();
				break;

			case IScreenFrameMsgId.SCREEN_UPDATE_INDICATOR : {
				if (objects[0] != null && objects[0] instanceof Bundle) {
					updateIndicator(param, (Bundle) objects[0]);
				}
				break;
			}
			case IScreenFrameMsgId.SCREEN_INDICRATOR_POSITION: {
				setIndicatorPost((String) objects[0]);
			}
				break;
			case ICommonMsgId.INDICATOR_CHANGE_SHOWMODE: {
				// 指示器改变了模式
				mDesktopIndicator.doWithShowModeChanged();
				break;
			}

			case ICommonMsgId.CHANGE_ICON_STYLE : {
				actionChangeIcon((Bundle) (objects[0]));
				break;
			}

			case ICommonMsgId.ICON_RENAME : {
				if (null != objects[0] && objects[0] instanceof Long && null != objects[1]
						&& objects[1] instanceof ArrayList && !((ArrayList) objects[1]).isEmpty()) {
					long itemid = (Long) objects[0];
					String name = (String) ((ArrayList) objects[1]).get(0);
					actionRename(name, itemid);
				}
				break;
			}

			case IScreenFrameMsgId.SCREEN_RESET_DEFAULT: {
				resetDefaultIcon();
				break;
			}

			case IScreenFrameMsgId.SCREEN_ENTER_SCREEN_EDIT_LAYOUT: {
				if (mGLWorkspace != null) {
					//关闭功能表
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
							IAppDrawerMsgId.APPDRAWER_EXIT, -1, false, 0);
					// boolean animation = param == 1 ? true : false;

					if (objects.length > 0) {
						mCurTabId = (Integer) objects[0];
					}
					if (GLAppFolder.getInstance().isFolderOpened()) {
						postDelayed(new Runnable() {

							@Override
							public void run() {
								mGLWorkspace.normalToSmallPreAction(mCurTabId, null,
										GLWorkspace.SCREEN_TO_SMALL_LEVEL_ONE);
							}
						}, GLAppFolderMainView.sFolderAnimationDuration);
					} else {
						mGLWorkspace.normalToSmallPreAction(mCurTabId, null,
								GLWorkspace.SCREEN_TO_SMALL_LEVEL_ONE);
					}
				}
				break;
			}

			case IScreenFrameMsgId.SCREEN_EDIT_ITEM_TO_SCREEN : {
				if (param >= 0 && objects != null && objects.length > 0) {
					GLView view = (GLView) objects[0];
					if (view.getTag() != null) {
						screenAutoFly(view, param);
					}
				}
				break;
			}

			case IScreenFrameMsgId.SCREEN_DEL_ITEM_FROM_FOLDER : {
				if (param >= 0 && objects != null && objects.length > 0) {
					GLView view = (GLView) objects[0];
					if (view != null && view.getTag() != null) {
						ret = screenDeleteItemFromFolder(objects[0]);
					}
				}
				break;
			}

			case IScreenFrameMsgId.PREVIEW_ADD_TO_SCREEN : {
				if (objects[0] != null && objects[6] != null && objects[7] != null
						&& objects[8] != null) {

					final DragView dragView = (DragView) objects[0];
					final int x = (Integer) objects[1];
					final int y = (Integer) objects[2];
					final int screen = (Integer) objects[3];
					final int xOffset = (Integer) objects[4];
					final int yOffset = (Integer) objects[5];
					final DragSource source = (DragSource) objects[6];
					final Object dragInfo = objects[7];
					final DropAnimationInfo resetInfo = (DropAnimationInfo) objects[8];

					boolean add = (Boolean) objects[9];
					if (add) {
						return mGLWorkspace.dropToTargetCell(source, x, y, xOffset, yOffset,
								dragView, dragInfo, resetInfo, screen, (int[]) objects[10]);
					} else {
						return mGLWorkspace.onDrop(source, x, y, xOffset, yOffset, dragView,
								dragInfo, resetInfo, screen);
					}
				}
				break;
			}

			case IScreenFrameMsgId.SCREEN_UPDATE_ITEM_INFOMATION : {
				if (objects != null && objects.length > 0) {
					final ItemInfo itemInfo = (ItemInfo) objects[0];
					final int screen = param;
					updateDesktopItem(screen, itemInfo);
				}
			}
				break;

			case IScreenFrameMsgId.SCREEN_CHANGE_VIEWS_POSITIONS : {
				if (param >= 0 && objects != null && objects.length > 0) {
					for (Object itemInfo : (ArrayList<ItemInfo>) objects[0]) {
						if (itemInfo instanceof ItemInfo) {
							if (null != mControler) {
								mControler.updateDesktopItem(param, (ItemInfo) itemInfo);
							}
						}
					}
				}
			}
				break;

			case IScreenFrameMsgId.SCREEN_MERGE_ITEMS : {
				if (param >= 0 && objects != null && objects.length > 0) {
					GuiThemeStatistics
					.goLauncherUserBehaviorStaticDataCache(IGoLauncherUserBehaviorStatic.LAUNCHER_OP_01);
					mergeFolder((DragView) objects[0], param, (Integer) objects[1],
							(Integer) objects[2]);
				}
			}
				break;

			case IScreenFrameMsgId.SCREEN_MOVE_TO_FOLDER : {
				if (param >= 0 && objects != null && objects.length > 0) {
					ret = moveToFolder((DragView) objects[0], param, (Integer) objects[1],
							(Integer) objects[2], (DropAnimationInfo) objects[3], objects[4],
							(DragSource) objects[5]);
					GuiThemeStatistics
					.goLauncherUserBehaviorStaticDataCache(IGoLauncherUserBehaviorStatic.LAUNCHER_OP_02);
				}
			}
				break;

			// 添加前先判断屏幕空间
			case IScreenFrameMsgId.SCREEN_EDIT_PRE_ADD : {
				if (objects != null && objects.length > 2) {
					ret = checkVacant((List<Integer>) objects[0], (int[]) objects[1],
							(float[]) objects[2]);
				}
			}
				break;

			case IScreenFrameMsgId.SCREEN_GET_CELLLAYOUT : {
				if (objects != null && objects.length > 0) {
					@SuppressWarnings("unchecked")
					ArrayList<GLCellLayout> list = (ArrayList<GLCellLayout>) objects[0];
					count = mGLWorkspace.getChildCount();
					for (int i = 0; i < count; i++) {
						GLView child = mGLWorkspace.getChildAt(i);
						if (child != null && child instanceof GLCellLayout) {
							GLCellLayout layout = (GLCellLayout) child;
							list.add(layout);
						}
					}
				}
			}
				break;

			case IScreenFrameMsgId.SCREEN_CREATE_ITEM : {
				if (param >= 0 && objects != null && objects.length > 1) {
					GLView[] newViews = (GLView[]) objects[1];
					newViews[0] = createDesktopView((ItemInfo) objects[0], param, true);
				}
			}
				break;

			case IScreenFrameMsgId.SCREEN_ADD_DESKTOP_ITEM : {
				if (param >= 0 && objects != null) {
					if (objects.length > 0) {
						ItemInfo info = (ItemInfo) objects[0];
						addDesktopItem(param, info);
						if (objects.length > 1) {
							final int type = (Integer) objects[1];
							// 如果是功能表拖出来的文件夹，异步添加文件夹内部items
							if (type == DragView.DRAGVIEW_TYPE_APPDRAWER_FOLDER
									&& mGLBinder != null && info instanceof UserFolderInfo) {
								mGLBinder.synchFolderFromDrawer((UserFolderInfo) info, null, false);
							}// end if 4
						} // end if 3
					} // end if 2
				} // end if 1
			}
				break;

			case IScreenFrameMsgId.SCREEN_SNAP_TO_SCREEN : {
				boolean noElastic = false;
				int duration = 0;
				if (objects != null && objects.length == 2) {
					noElastic = (Boolean) objects[0];
					duration = (Integer) objects[1];
				}
				mGLWorkspace.snapToScreen(param, noElastic, duration);
			}
				break;

			case IScreenFrameMsgId.SCREEN_GET_ALLOCATE_APPWIDGET_ID: {
				// 获取由host生成的新的appwidget的ID
				if (objects[0] != null && objects[0] instanceof Bundle) {
					Bundle bundle = (Bundle) objects[0];
					bundle.putInt("id", mWidgetHost.allocateAppWidgetId());
					ret = true;
				}
			}
				break;

			case IScreenFrameMsgId.SCREEN_DEL_APPWIDGET_ID: {
				// host删除一个appwidgetID
				// mWidgetHost.deleteAppWidgetId(param);
				if (m3DWidgetManager.isGoWidget(param)) {
					m3DWidgetManager.deleteWidget(param);
				} else {
					// 系统widget
					mWidgetHost.deleteAppWidgetId(param);
				}
				ret = true;
			}
				break;

			case IScreenFrameMsgId.SCREEN_ADD_APPWIDGET: {
				ret = addAppWidget(param);
			}
				break;
				
			case IScreenFrameMsgId.ALLOW_SCREEN_TO_SCROLL : {
				mGLWorkspace.unlock();
			}
				break;
				
			case IScreenFrameMsgId.SCREEN_CHANGE_TO_SMALL : {
				if (param > 0 && mGLWorkspace != null) {
					mGLWorkspace.enterEditState(param);
				}
			}
				break;

			case IScreenFrameMsgId.SCREEN_BACK_TO_LAST_LEVEL : {
				if (mGLWorkspace != null) {
					if (!mGLWorkspace.mExitToLevelFlag) {
						mGLWorkspace.exitToLevel(GLWorkspace.SCREEN_BACK_TO_LAST_LEVEL);
						leaveNewFolderState();
					}
				}
			}
				break;	
			case IScreenFrameMsgId.SCREEN_FIND_NEAREST_VACANT_CELL : {
				if (objects != null && objects.length > 3) {
					int screenIndex = mGLWorkspace.getCurrentScreen();
					if (param > -1) {
						screenIndex = param;
					}
					final int x = (Integer) objects[0];
					final int y = (Integer) objects[1];
					final int[] cellXY = (int[]) objects[2];
					final int[] centerPoint = (int[]) objects[3];
					if (screenIndex > -1 && screenIndex < mGLWorkspace.getChildCount()) {
						GLCellLayout layout = (GLCellLayout) mGLWorkspace.getChildAt(screenIndex);
						if (layout != null) {
							layout.findNearestVacantArea(x, y, 1, 1, 1, 1, null, cellXY, null);
							if (cellXY[0] != -1 && cellXY[1] != -1) {
								GLCellLayout.cellToRealCenterPoint(cellXY[0], cellXY[1],
										centerPoint);
							} // end if 4
						} // enf if 3
					} // end if 2
				} // end if 1
			}
				break;

			case IScreenFrameMsgId.SCREEN_SHOW_PREVIEW: {
				if (mGLWorkspace.isEditState()) {
					leaveNewFolderState();
					mGLWorkspace.exitEditState();
				} else {
					Boolean fromSetting = true;
					if (objects[0] != null) {
						fromSetting = (Boolean) objects[0];
						showPreview(fromSetting);
					} else {
						showPreview(true);
					}
				}
			}
				break;
				
			case IScreenFrameMsgId.SCREEN_AUTO_FLY : {
				if (param >= 0 && objects != null && objects.length > 0) {
					final GLView view = (GLView) objects[0];
					if (view.getTag() != null) {
						screenAutoFly(view, param);
					}
				}
			}
				break;

			case IScreenFrameMsgId.SCREEN_ADD_GO_WIDGET : {
				// 成功添加widget至桌面

				int screenindex;

				screenindex = mGLWorkspace.getCurrentScreen();
				ret = addGoWidget((Bundle) objects[0], screenindex, param);
				// add by jiang添加完允许滑动
				mGLWorkspace.unlock();

			}
				break;

			case IScreenFrameMsgId.PREVIEW_SCREEN_ADD : {
				ret = true;
				synchronized (mLockData) {
					if (null != mControler) {
						mControler.addScreen(param);
					}
					addCellLayout();
				}
			}
				break;

			case IScreenFrameMsgId.SCREEN_ADD_BLANK_CELLLAYOUT : {
				addBlankCellLayout();
				ret = true;
			}
				break;

			case IScreenFrameMsgId.SCREEN_BLANK_TO_NORMAL : {
				ret = true;
				synchronized (mLockData) {
					if (null != mControler) {
						mControler.addScreen(param);
					}
				}
				break;
			}

			case IScreenFrameMsgId.PREVIEW_SAVE_SCREEN_DATA : {
				if (objects[0] != null && objects[0] instanceof Bundle) {
					moveScreen((Bundle) objects[0]);
					ret = true;
				}
				if (objects[0] != null && objects[0] instanceof Boolean) {
					if ((Boolean) objects[0]) {
						final int currentScreen = mPreCurrentScreen;
						// 删除的屏幕在当前屏左边．则新的当前屏减1
						if (param < currentScreen) {
							mPreNewCurrentScreen = currentScreen - 1;
						} else if (param == currentScreen) {
							// 如果相等，则把第一屏设为当前屏
							mPreNewCurrentScreen = 0;
						} else if (param > currentScreen) {
							// 如果相等，则把第一屏设为当前屏
							mPreNewCurrentScreen = currentScreen;
						}
						GLSense.sCurScreenId = mPreNewCurrentScreen;
					}
				}
			}
				break;

			case IScreenFrameMsgId.PREVIEW_SCREEN_ENTER : {
				sIsShowPreview = false;
				final int screenIndex = param;
				// 从屏幕预览回来，切屏时间使用参数指定值
				int duration = 300;
				boolean isDrag = false;
				if (objects[0] != null && objects[0] instanceof Integer) {
					duration = ((Integer) objects[0]).intValue();
					if (duration <= -100) {
						duration = 300;
						isDrag = true;
					}
				}
				// mGLWorkspace.mDragging = isDrag;
				//更新一下指示器状态
				mDesktopIndicator.setVisible(sShowIndicator);
				mGLWorkspace.initScrollData();
				mGLWorkspace.scaleForMenuPreAction(false, false);
				mGLWorkspace.requestLayout();
				turnToScreen(screenIndex, true, duration);
				handleMessage(this, IScreenFrameMsgId.SCREEN_RESHOW_LAST_GUIDE, 0);
				ret = true;
			}
				break;

			case IScreenFrameMsgId.SCREEN_HIDE_CURRENT_GUIDE :
				GuideControler guideControler = GuideControler.getInstance(mContext);
				guideControler.hideCloudViewByType(GuideControler.CLOUD_TYPE_SCREEN);
				break;
				
			case IScreenFrameMsgId.SCREEN_RESHOW_LAST_GUIDE :
				guideControler = GuideControler.getInstance(mContext);
				guideControler.reshowCloudViewByType(GuideControler.CLOUD_TYPE_SCREEN);
				break;
				
			case IScreenFrameMsgId.SCREEN_ADD_APPDRAWER_ICON_TO_SCREEN : {
				if (param > -1 && objects != null && objects.length > 1) {
					final int[] cellXY = (int[]) objects[0];
					final Object dragInfo = (Object) objects[1];
					final int dragType = DragController.getDragType(null, dragInfo);
					final ItemInfo itemInfo = DragInfoTranslater.createItemInfoForScreen(dragType,
							dragInfo, cellXY[0], cellXY[1]);
					addDesktopItem(param, itemInfo);
					mGLWorkspace.addInScreen(createDesktopView(itemInfo, param, true), (int) param,
							cellXY[0], cellXY[1], 1, 1);
					// 如果是功能表拖出来的文件夹，异步添加文件夹内部items
					if (dragType == DragView.DRAGVIEW_TYPE_APPDRAWER_FOLDER && mGLBinder != null
							&& itemInfo instanceof UserFolderInfo) {
						mGLBinder.synchFolderFromDrawer((UserFolderInfo) itemInfo, null, false);
					}
				}
			}
				break;

			case IScreenFrameMsgId.PREVIEW_SCREEN_REMOVE : {
				final int screenIndex = param;
				synchronized (mLockData) {
					if (null != mControler) {
						// 清除引用，释放资源
						mControler.unbindObjectInScreen(screenIndex);
						mControler.delScreen(screenIndex);
					}
					mGLWorkspace.removeScreen(screenIndex);
//					mHandler.obtainMessage(DELETE_SCREEN, screenIndex, -1).sendToTarget();
				}
			}
				break;

			case IFolderMsgId.ON_FOLDER_DROP_COMPELETE : {
				if (objects != null && objects.length > 3) {
					ItemInfo shortCutInfo = (ItemInfo) objects[1];
					boolean success = (Boolean) objects[2];
					GLScreenFolderIcon folderIcon = (GLScreenFolderIcon) objects[3];
					if (mGLWorkspace.mNeedSnapToScreenAfterFolderClosed > -1) {
						handleScreenFolderDragEvent(shortCutInfo, folderIcon, success, true);
					} else {
						handleScreenFolderDragEvent(shortCutInfo, folderIcon, success, false);
					}
//					GLAppFolder.getInstance().batchStartIconEditEndAnimation();
				}
			}
				break;
			case IFolderMsgId.SCREEN_FOLDER_ON_DRAG_FLING : {
				if (objects != null && objects.length > 1) {
					final ItemInfo shortCutInfo = (ItemInfo) objects[0];
					final GLScreenFolderIcon folderIcon = (GLScreenFolderIcon) objects[1];
					handleScreenFolderDragEvent(shortCutInfo, folderIcon, true, true);
				}
			}
				break;
			case IFolderMsgId.FOLDER_APP_LESS_TWO :
				final GLScreenFolderIcon folderIcon = (GLScreenFolderIcon) objects[0];
				UserFolderInfo userFolderInfo = folderIcon.getInfo();
				if (objects[1] instanceof UserFolderInfo) {
					userFolderInfo = (UserFolderInfo) objects[1];
				}
				folderIcon.closeFolder(true);
				if (mControler != null) {
					if (userFolderInfo.getContents().size() <= 1) {
						ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.0f, 1f, 0.0f,
								Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
						scaleAnimation.setDuration(300);
						folderIcon.startAnimation(scaleAnimation);
						deleteScreenFolder(userFolderInfo, userFolderInfo.mScreenIndex, true);
						mControler.removeUserFolder(userFolderInfo);
					} else {
						final ShortCutInfo shortCutInfo = (ShortCutInfo) objects[1];
						userFolderInfo.remove(shortCutInfo.mInScreenId);
						ShortCutInfo info = userFolderInfo.getChildInfo(0);
						info.mCellX = userFolderInfo.mCellX;
						info.mCellY = userFolderInfo.mCellY;
						int index = userFolderInfo.mScreenIndex;
						GLView addView = addDesktopView(info, index, true, false);
						mControler.addDesktopItem(info.mScreenIndex, info);
						deleteScreenFolder(userFolderInfo, userFolderInfo.mScreenIndex, true);
						mControler.removeUserFolder(userFolderInfo);
						Message msg = new Message();
						msg.what = ADD_ITEM_FROM_FOLDER_ANIMATION;
						msg.obj = addView;
						mHandler.sendMessage(msg);
					}
				}
				break;
			case IScreenFrameMsgId.SCREEN_RECT_TO_POSITION : {
				ret = validateRect((Rect) objects[0]);
			}
				break;

			case IScreenFrameMsgId.SCREEN_ADD_USER_FOLDER : {
				if (objects != null && objects.length > 0) {
					UserFolderInfo info = (UserFolderInfo) objects[0];
					addDeskUserFolder(info);
				}
			}
				break;

			case IScreenFrameMsgId.PREVIEW_NOTIFY_DESKTOP : {
				changeDrawState(param == 1);
				break;
			}

			case IScreenFrameMsgId.PREVIEW_REPLACE_CARD : {
				changeDrawState(param == 1);
				break;
			}

			case IScreenFrameMsgId.SCREEN_EXIT_RESIZE_WIDGET : {
				stopWidgetEdit();
			}
				break;

			case IScreenFrameMsgId.PREVIEW_TO_MAIN_SCREEN :
				sIsShowPreview = false;
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW,
						IScreenPreviewMsgId.PREVIEW_TO_MAIN_SCREEN_ANIMATE, GLSense.sCurScreenId);

				break;

			case IScreenFrameMsgId.SET_WALLPAPER_DRAWABLE: {
				if (objects != null && objects.length > 0) {
					if (mNeedChangeXOffset) {
						mControler.updateScreenSettingInfo();
						mGLWorkspace
								.setWallpaperScroll(mControler.getScreenSettingInfo().mWallpaperScroll);
						mNeedChangeXOffset = false;
					}
					final Drawable bg = (Drawable) objects[0];
					// param是状态栏的高度，如果隐藏的话就为0，但是因为3D是全屏layout，所以不算状态栏的高度一直为0
//					final int offset = param;
					final int offset = 0;
					mGLWorkspace.setWallpaper(bg, offset);
					mGLWorkspace.invalidate();
					//					if (mNeedChangeXOffset) {
					//						ShellScreenScroller scroller = mGLWorkspace.getScreenScroller();
					//						Log.v("BackGround", "glscreen called");
					//						int span = GoLauncherActivityProxy.isPortait()
					//								? GLWorkspace.sPageSpacingX
					//								: GLWorkspace.sPageSpacingY;
					//						scroller.setScreenSize(DrawUtils.sWidthPixels + span,
					//								DrawUtils.sHeightPixels);
					//						int x = -scroller.getBackgroundOffsetX(scroller.getScroll());
					//						int y = -scroller.getBackgroundOffsetY();
					//						if (scroller.getOrientation() == ShellScreenScroller.HORIZONTAL) {
					//							x += scroller.getScroll();
					//						} else {
					//							y += scroller.getScroll();
					//						}
					//						mBackWorkspace.updateXY(x, y);
					//						mBackWorkspace.updateOffsetX(scroller.getScroll(), true);
					//						mNeedChangeXOffset = false;
					//					}
				}
				break;
			}

			case IScreenFrameMsgId.SCREEN_UPDATE_WALLPAPER_FOR_SCROLL_MODE_CHANGE : {
				mNeedChangeXOffset = true;
			}
				break;
			case IScreenFrameMsgId.SCREEN_FIRE_WIDGET_ONENTER : {
				if (param == -1) {
					param = mGLWorkspace.getCurrentScreen();
				}
				fireWidgetVisible(true, param);
			}
				break;

			case IScreenFrameMsgId.SCREEN_FIRE_WIDGET_ONLEAVE : {
				if (param == -1) {
					param = mGLWorkspace.getCurrentScreen();
				}
				fireWidgetVisible(false, param);
			}
				break;

			// 设置特效预览
			case IScreenFrameMsgId.SCREENEDIT_SHOW_TAB_EFFECT_SETTING : {
				if (mGLWorkspace != null) {
					if (objects != null && objects.length == 2) {
						mGLWorkspace.primeEffectorAutoShow(param, (Integer) objects[0],
								(Integer) objects[1]);
					} else {
						mGLWorkspace.effectorAutoShow(param);
					}
				}
			}
				break;

			case IScreenFrameMsgId.PREVIEW_RESIZE_DRAGVIEW : {
				if (mGLWorkspace != null && objects[0] instanceof Float) {
					mGLWorkspace.resizeDragViewForPreview((Float) objects[0], (Float) objects[0]);
				}
			}
				break;

			case IScreenFrameMsgId.SCREEN_ENTER_SCROLL_ZONE_BG : {
				if (param == 1) {
					//Left
					mCurBorder = BORDER_LEFT;
					mBorderBgDrawable.setBounds(0, 0, mBorderBgSize, DrawUtils.sHeightPixels);
				} else if (param == 2) {
					//Right
					mCurBorder = BORDER_RIGHT;
					mBorderBgDrawable.setBounds(DrawUtils.sWidthPixels - mBorderBgSize, 0,
							DrawUtils.sWidthPixels, DrawUtils.sHeightPixels);
				} else {
					// None
					mCurBorder = BORDER_NONE;
				}
			}
				break;

			case IScreenFrameMsgId.SCREEN_SHOW_MIDDLE_VIEW : {
				if (mBackWorkspace != null) {
					mBackWorkspace.showMiddleView();
					mBackWorkspace.onStateMethod(GLBackWorkspace.STATE_ON_RESUME);
				}
				ret = true;
			}
				break;

			case IScreenFrameMsgId.SCREEN_HIDE_MIDDLE_VIEW: {
				if (mBackWorkspace != null) {
					mBackWorkspace.hideMiddleView();
					mBackWorkspace.onStateMethod(GLBackWorkspace.STATE_ON_STOP);
				}
				ret = true;
			}
				break;

			case IScreenFrameMsgId.SCREEN_ENABLE_WIDGET_DRAWING_CACHE : {
				mGLWorkspace.setEnableWidgetDrawingCache(param == 1);
				ret = true;
			}
				break;

			case IScreenFrameMsgId.SCREEN_SET_HOME : {
				// 设置为主屏
				final int mainScreen = param;
				ScreenSettingInfo screenSettingInfo = mControler.getScreenSettingInfo();
				// 更新数据库
				if (mControler != null && screenSettingInfo != null
						&& screenSettingInfo.mMainScreen != mainScreen) {
					screenSettingInfo = mControler.updateScreenSettingInfo();
					screenSettingInfo.mMainScreen = mainScreen;
					SettingProxy.updateScreenSettingInfo2(screenSettingInfo, false);
				}
				mGLWorkspace.setMainScreen(mainScreen);
				ret = true;
			}
				break;

			case IAppCoreMsgId.EVENT_INSTALL_APP :
			case IAppCoreMsgId.EVENT_INSTALL_PACKAGE : {
				String pkgString = (String) objects[0];
				//是从go动态壁纸处点击安装
				int state = mPushController.saveInstalledPackage(pkgString);
				if (state == ScreenEditPushConstants.REQUEST_ID_FOR_WALLPAPER) {
					if (ScreenEditStatistics.sStatisticDebug) {
						StatisticsManager.getInstance(ApplicationProxy.getContext()).setDebugMode();
					}
					StatisticsManager.getInstance(ApplicationProxy.getContext()).upLoadBasicOptionStaticData(
							ScreenEditStatistics.STATISTICS_FUN_ID, pkgString, 
							ScreenEditStatistics.STATISTICS_OPERATE_APP_INSTALLED, 
							ScreenEditStatistics.STATISTICS_OPERATE_SUCCESS, 
							Statistics.getUid(ShellAdmin.sShellManager.getActivity()), 
							ScreenEditStatistics.STATISTICS_ENTER, 
							ScreenEditStatistics.STATISTICS_TYPE_DYNAMICA_WALLPAPER, -1);	
				}
				pkgString = null;
			}
				break;

			case IAppCoreMsgId.EVENT_UNINSTALL_APP : {
				handleUninstallApps((ArrayList<AppItemInfo>) objects[1]);
				break;
			}
				
			case IAppCoreMsgId.EVENT_UNINSTALL_PACKAGE: {
				mHandler.sendMessage(mHandler.obtainMessage(REFRESH_UNINSTALL_PACKAGE,
						(String) objects[0]));
				break;
			}
			
			case IAppCoreMsgId.EVENT_CHANGE_APP:
				if (param == AppDataEngine.EVENT_CHANGE_APP_DISABLE
						|| param == AppDataEngine.EVENT_CHANGE_APP_COMPONENT) {
					if (objects != null) {
						List<AppItemInfo> list = (List<AppItemInfo>) objects[1];
						AppItemInfo info = list.get(0);
						mHandler.sendMessage(mHandler.obtainMessage(REFRESH_CHANGE_APP,
								info.mIntent));
					}
				}
				break;
			case IAppCoreMsgId.EVENT_UPDATE_PACKAGE:
			case IAppCoreMsgId.EVENT_UPDATE_APP: 
				if (objects != null && objects[1] != null) {
					List<List<AppItemInfo>> data = (List<List<AppItemInfo>>) objects[1];
					if (data.get(1) != null) {
						ArrayList<AppItemInfo> disableList = (ArrayList<AppItemInfo>) data
								.get(1);
						if (!disableList.isEmpty()) {
							for (AppItemInfo info : disableList) {
								mHandler.sendMessage(mHandler.obtainMessage(REFRESH_CHANGE_APP,
										info.mIntent));
							}
						}
					}
				}
				break;
			
			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED :
				CellUtils.init(getContext());
				hidePopupWindow(false);
				reloadGOWidget();
				reLoadMiddleView();
				mGLWorkspace.getZeroHandler().changeZeroScreenLayout();
//			if (mGLSuperWorkspace.isShowingZero()) {
//				mGLSuperWorkspace.changeZeroScreenLayout();
//			}

				//					if (!mEnterPrew) {
				mGLWorkspace.requestLayout(GLWorkspace.CHANGE_SOURCE_INDICATOR, 0);
				//				if (mGLWorkspace.getVisibility() != GLView.VISIBLE) {
				ShellScreenScroller scroller = mGLWorkspace.getScreenScroller();
				Log.v("BackGround", "glscreen called");
				int span = GoLauncherActivityProxy.isPortait()
						? GLWorkspace.sPageSpacingX
						: GLWorkspace.sPageSpacingY;
				scroller.setScreenSize(DrawUtils.sWidthPixels + span, DrawUtils.sHeightPixels);
				int x = -scroller.getBackgroundOffsetX(scroller.getScroll());
				int y = -scroller.getBackgroundOffsetY();
				if (scroller.getOrientation() == ShellScreenScroller.HORIZONTAL) {
					x += scroller.getScroll();
				} else {
					y += scroller.getScroll();
				}
				mBackWorkspace.updateXY(x, y);
				mBackWorkspace.updateOffsetX(scroller.getScroll(), true);
				//				}

				//					}
				mUiDefHandler.sendEmptyMessage(DEFERRED_MSG_ENTER_EDIT_MODE);
				
				break;

			case IScreenFrameMsgId.SCREEN_ADD_SHORTCUT: {
				ret = addShortcut(objects[0]);
			}
				break;
			case IScreenFrameMsgId.SCREEN_ADD_LIVE_FOLDER: {
				ret = addLiveFolder(objects[0]);
			}
				break;
			case IScreenFrameMsgId.SCREEN_SHOW_HOME: {
				if (mGLWorkspace.isEditState() && mGLWorkspace.mScroller.isFinished()) {
					leaveNewFolderState();
					mGLWorkspace.exitEditState();
				} else {
					PreferencesManager sharedPreferences = new PreferencesManager(
							ShellAdmin.sShellManager.getActivity(),
							IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
					boolean firstShow = sharedPreferences.getBoolean(
							IPreferencesIds.SHOULD_SHOW_PREVIEW_HOME, true);
					if (firstShow && mGLWorkspace.mLoadFinish
							&& !mGLSuperWorkspace.isInZeroScreen()) {
						sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_PREVIEW_HOME,
								false);
						sharedPreferences.commit();
						GLSense.sIsHOME = true;
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SCREEN_SHOW_PREVIEW, -1, false);
					} else {
						post(new Runnable() {

							@Override
							public void run() {
								mGLWorkspace.snapToScreen(mGLWorkspace.getMainScreen(), false, -1);
							}
						});
					}

				}
				ret = true;
			}
				break;
			case IScreenFrameMsgId.SCREEN_SHOW_MAIN_SCREEN_OR_PREVIEW: {
				if (mGLWorkspace.isEditState()) {
					leaveNewFolderState();
					mGLWorkspace.exitEditState();
				} else {
					if (mGLWorkspace.getCurrentScreen() == mGLWorkspace.getMainScreen()) {
						showPreview(false);
					} else {
						post(new Runnable() {

							@Override
							public void run() {
								mGLWorkspace.snapToScreen(mGLWorkspace.getMainScreen(), false, -1);
							}
						});
					}
				}
				ret = true;
			}
				break;
			case IScreenFrameMsgId.SCREEN_TO_APPDRAWER : {
				if (mGLWorkspace.isEditState()) {
					leaveNewFolderState();
					mGLWorkspace.exitEditState();
					postDelayed(new Runnable() {

						@Override
						public void run() {
							mShell.showStage(IShell.STAGE_APP_DRAWER, true);
						}
					}, GLWorkspace.ANIMDURATION + 150);
				} else {
					if (sIsShowPreview) {
						mShell.showStage(IShell.STAGE_SCREEN, true);
						postDelayed(new Runnable() {

							@Override
							public void run() {
								mShell.showStage(IShell.STAGE_APP_DRAWER, true);
							}
						}, GLSenseWorkspace.LEAVE_DURATION + 50);
					} else {
						mShell.showStage(IShell.STAGE_APP_DRAWER, true);
					}
				}
				ret = true;
			}
				break;
			case IScreenFrameMsgId.SHOW_GLGGMENU: {
//				if (BetaController.getInstance().needToShowBetaDialog()) {
//					DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							handleOpenMenu();
//						}
//					};
//					BetaController.getInstance().showBetaDialog(listener);
//				} else {
					handleOpenMenu();
//				}
			}
				break;
			case IScreenFrameMsgId.LEAVE_NEW_FOLDER_STATE : {
				//				if (mGLWorkspace.isEditState()) {
				leaveNewFolderState();
				//					mGLWorkspace.exitEditState();
				//				}
			}
				break;
			case IScreenFrameMsgId.GET_DROP_DOCK_LOCATION : {
				DragSource source = (DragSource) objects[0];
				DragView dragView = (DragView) objects[1];
				int[] cellXY = (int[]) objects[2];
				int[] cellXYRealPonts = (int[]) objects[3];

				ArrayList<Integer> list = (ArrayList<Integer>) objects[4];
				//来自桌面

				if (source instanceof GLWorkspace) {
					if (dragView.getOriginalView() instanceof GLScreenShortCutIcon) {
						GLScreenShortCutIcon icon = (GLScreenShortCutIcon) dragView
								.getOriginalView();
						ItemInfo info = icon.getInfo();
						if (info.mScreenIndex == mGLWorkspace.getCurrentScreen()) {
							cellXY[0] = info.mCellX;
							cellXY[1] = info.mCellY;
							GLCellLayout.cellsToCenterPoint(cellXY[0], cellXY[1], list.get(0),
									list.get(1), cellXYRealPonts);;
							ret = true;
						} else {
							ret = checkVacant(list, cellXYRealPonts, cellXY);

						}
					} else if (dragView.getOriginalView() instanceof GLScreenFolderIcon) {
						GLScreenFolderIcon folder = (GLScreenFolderIcon) dragView.getOriginalView();
						UserFolderInfo info = folder.getInfo();
						if (info.mScreenIndex == mGLWorkspace.getCurrentScreen()) {
							cellXY[0] = info.mCellX;
							cellXY[1] = info.mCellY;
							GLCellLayout.cellsToCenterPoint(cellXY[0], cellXY[1], list.get(0),
									list.get(1), cellXYRealPonts);;
							ret = true;
						} else {
							ret = checkVacant(list, cellXYRealPonts, cellXY);
						}
					}

				} else {
					ret = checkVacant(list, cellXYRealPonts, cellXY);
				}
			}
				break;
			case IScreenFrameMsgId.EXCHANGE_ICON_FROM_DOCK :
				int screenIndex = mGLWorkspace.getCurrentScreen();
				DragView dragView = (DragView) objects[1];
				if (ScreenUtils.findVacant(new int[2], 1, 1, screenIndex, mGLWorkspace, dragView.getOriginalView())) {
					DockItemInfo dockItemInfo = (DockItemInfo) objects[0];
					DragSource source2 = (DragSource) objects[3];
					ItemInfo finalInfo = dockItemInfo.mItemInfo;
					int[] cell = new int[] { finalInfo.mCellX, finalInfo.mCellY };
					if (finalInfo instanceof UserFolderInfo
							&& source2 instanceof GLDockFolderGridVIew) {
						ItemInfo dragInfo = (ItemInfo) objects[2];
						UserFolderInfo userFolderInfoFromDock = (UserFolderInfo) finalInfo;
						mControler.removeItemsFromFolder(userFolderInfoFromDock,
								(ShortCutInfo) dragInfo);
						userFolderInfoFromDock.remove(dragInfo);
						int size = userFolderInfoFromDock.getContents().size();
						if (size == 1) {
							finalInfo = userFolderInfoFromDock.getContents().get(0);
							finalInfo.mCellX = cell[0];
							finalInfo.mCellY = cell[1];
							addDesktopItem(screenIndex, finalInfo);
							GLView addView = addDesktopView(finalInfo, screenIndex, true, true);
							Message msg = new Message();
							msg.what = ADD_ITEM_FROM_FOLDER_ANIMATION;
							msg.obj = addView;
							mHandler.sendMessage(msg);
							ret = true;
						} else if (size == 0) {
							finalInfo = null;
							ret = true;
						} else {
							finalInfo = dockItemInfo.mItemInfo;
							addDesktopItem(screenIndex, finalInfo);
							addDesktopView(finalInfo, screenIndex, true, true);
							ret = true;
						}
					} else {
						addDesktopItem(screenIndex, finalInfo);
						addDesktopView(finalInfo, screenIndex, true, true);
						ret = true;
					}
				} else {
					ret = false;
				}
				break;

			case IScreenFrameMsgId.SCREEN_SEND_BROADCASTTO_MULTIPLEWALLPAPER :
				// add by chenbingdong
				// 通过ScreenFrame来发送广播给多屏多壁纸
				mGLWorkspace.sendBroadcastToMultipleWallpaper(false, false);
				break;

			case IScreenFrameMsgId.SCREEN_REPLACE_RECOMMEND_ICON: {
				String pkgName = (String) objects[0];
				replaceDeskIcon(pkgName);
			}
				break;

			case IScreenFrameMsgId.REPLACE_RECOMMAND_ICON_IN_FOLDER: {
				String pkgName = (String) objects[0];
				replaceRecommandIconInFolder(pkgName);
				break;
			}

			case IScreenFrameMsgId.APPLY_GO_WIDGET_THEME: {
				ret = applyGoWidgetTheme(param, (Bundle) objects[0]);
				break;
			}

			case ICommonMsgId.COMMON_FULLSCREEN_CHANGED : {
				// 一般3.X的pad状态栏在下面，上面是没有的，所以不需要重新排版
				mGLSuperWorkspace.statusBarLayout();
				if (!(Machine.IS_HONEYCOMB && !Machine.IS_ICS)) {
//					final boolean isFullScreen = param == 1;
//					final int yOffset = isFullScreen ? 0 : StatusBarHandler.getStatusbarHeight();
//					mGLWorkspace.setWallpaperYOffset(yOffset);
//					if (!mIsShowPreview) { 
						mGLWorkspace.requestLayout(GLWorkspace.CHANGE_SOURCE_STATUSBAR, param);
//					}
					mGLWorkspace.invalidate();
				}
				break;
			}

			case IScreenFrameMsgId.SCREEN_NEED_TO_LAYOUT_BY_DOCK : {
				if (param > -1 && param < 2) {
					mGLWorkspace.requestLayout(GLWorkspace.CHANGE_SOURCE_DOCK, param);
				}
			}
				break;

			case IScreenFrameMsgId.SCREEN_SHOW_OR_HIDE_INDICATOR : {
				if (sShowIndicator) {
					mDesktopIndicator.setVisible(param == 1);
				}
			}
				break;

			case IScreenFrameMsgId.GOTO_SCREEN_EDIT_TAB: {
				// 我来自何方
				final int smallLevel = param;
				String pkg = null;
				Object info = null;

				if (objects.length > 0) {
					pkg = (String) objects[0];
				}
				if (objects.length > 1) {
					info = objects[1];
				}
				final Object finalInfo = info;
				
				if (pkg != null && pkg.equals(PackageName.MULTIPLEWALLPAPER_PKG_NAME)) {
					mCurTabId = ScreenEditConstants.TAB_ID_WALLPAPER; // 多屏多壁纸跳转
				} else if (pkg != null && pkg.equals(ScreenEditConstants.TAB_ADDGOWIDGET)) {
					mCurTabId = ScreenEditConstants.TAB_ID_SUB_GOWIDGET;
				} else if (pkg != null && pkg.equals(ScreenEditConstants.TAB_EFFECTS)) { 
					mCurTabId = ScreenEditConstants.TAB_ID_EFFECTS;
				} else if (pkg != null && pkg.equals(ScreenEditConstants.TAB_ADDSYSTEMWIDGET)) {
					mCurTabId = ScreenEditConstants.TAB_ID_SUB_SYSTEMWIDGET;
				} else if (pkg != null && pkg.equals(ScreenEditConstants.TAB_MAIN)) {
					mCurTabId = ScreenEditConstants.TAB_ID_MAIN;
				} else if (pkg != null && pkg.equals(ScreenEditConstants.TAB_SYSTEMWIDGET)) {
					mCurTabId = ScreenEditConstants.TAB_ID_SYSTEMWIDGET;
				} else {
					mCurTabId = ScreenEditConstants.TAB_ID_GOWIDGET; // Gowidget跳转
				}

				post(new Runnable() {

					@Override
					public void run() {
						//判断跳转前是否在添加界面
						mGLWorkspace.normalToSmallPreAction(mCurTabId, finalInfo, smallLevel);
					}
				});

				break;
			}
			case IScreenEditMsgId.SCREEN_EDIT_ADD_GOWIDGET: {
				if (objects[0] != null) {
					final Object object = objects[0];
					postDelayed(new Runnable() {
						@Override
						public void run() {
							//	MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_EDIT, this,
							//IScreenFrameMsgId.SCREEN_EDIT_ADD_GOWIDGET, 0, object);
						}
					}, 1000);
				}
				break;
			}
			case IScreenFrameMsgId.SCREEN_REFRESH_INDEX : {
				mGLWorkspace.refreshScreenIndex();
				break;
			}

			case IScreenFrameMsgId.SCREEN_SET_CURRENTSCREEN : {
				if (param > -1) {
					mGLWorkspace.setCurrentScreen(param);
				}
			}
				break;

			case IScreenFrameMsgId.SCREEN_GET_HOME_CURRENT_SCREEN : {
				mPreCurrentScreen = mGLWorkspace.getCurrentScreen();
				mPreHomeScreen = mGLWorkspace.getMainScreen();
			}
				break;

			case IScreenFrameMsgId.SCREEN_FORCE_RELAYOUT : {
				mGLWorkspace.requestLayout();
			}
				break;

			//一键清理屏幕获取扫描结果
			case IScreenFrameMsgId.CLEAN_SCREEN_SCAN_SCREEN:
				ret = scanScreenCleanDataList((List) objects[1]);
				break;

			//一键清理屏幕获取扫描结果
			case IScreenFrameMsgId.CLEAN_SCREEN_DEL_ONE_ICON:
				ret = scanDelScanIcon(objects[0]);
				break;

			case IScreenFrameMsgId.LOCK_SCREEN_TO_SCROLL :
				mGLWorkspace.lock();
				;
				break;
			case IScreenFrameMsgId.SCREEN_IS_IN_EDIT_STATE : {
				ret = mGLWorkspace.isEditState();
				break;
			}
			case IScreenFrameMsgId.SCREEN_EXIT_EDIT_STATE : {
				if (mGLWorkspace.isEditState()) {
					leaveNewFolderState();
					mGLWorkspace.exitEditState();
				}
				break;
			}
			case IScreenFrameMsgId.SCREEN_ON_GGMENU_SHOW : {
				onGGMenuShow(param == 1, (Boolean) objects[0], (Boolean) objects[1]);
				break;
			}
			case IScreenFrameMsgId.UNINSTALL_APP_FROM_DELETEZONE : {
				mControler.uninstallApplication((Intent) objects[0]);
				break;
			}
				
				//桌面1、5屏添加广告图标
			case IScreenFrameMsgId.SCREEN_ADD_ADVERT_SHORT_CUT:
				ret = addAdvertShortCut(objects[0], param);
				break;
			
				//桌面1、5屏添加文件夹广告图标	
			case IScreenFrameMsgId.SCREEN_ADD_ADVERT_FOLDER:
				ret = addAdvertFolder(objects[0], param);
				break;
				
				//清除15屏广告图标
			case IScreenFrameMsgId.SCREEN_CLEAR_ADVERT_ICON:
				ret = clearAdvertIcon(objects[0]);
				break;
				
				//设置首屏图标信息缓存
			case IScreenFrameMsgId.SET_HOME_SCREEN_ICON_CACHE:
				ret = setHomeScreenIconCache();
				break;
				
			case IScreenFrameMsgId.SCREEN_IS_SCROLL_FINISHED:
				ret = mGLWorkspace.mScroller.isFinished();
				break;
				
			case IScreenFrameMsgId.SCREEN_IS_SCALE_ANIM_FINISHED:
				ret = mGLWorkspace.isEditScaleAnim();
				break;

			case IScreenFrameMsgId.SCREEN_ENABLE_CELLLAYOUT_DRAWING_CACHE :
				mGLWorkspace.setEnableCellLayoutDrawingCache(param == 1);
				ret = true;
				break;
			case IScreenFrameMsgId.SCREEN_ADD_SHORTCUT_COMPLETE: {
				// 处理其他程序发生的添加快捷方式请求
				ret = installShortcut((ShortCutInfo) objects[0]);
				break;
			}
			case IScreenFrameMsgId.GO_LOCKER_PRECHANGE: {
				if (!mTextLayoutvisiable) {
					mTextLayoutvisiable = true;
				Animation animation = new Translate3DAnimation(0, 0,
						- GLCellLayout.sLongAxisEndPadding / 2, 0, 0, 0);
				animation.setDuration(250);
				if (animation != null && mTextLayout != null) {
					AnimationTask task = new AnimationTask(false,
							AnimationTask.PARALLEL);
					task.addAnimation(mTextLayout, animation, null);
					mTextLayout.setVisibility(View.VISIBLE);
					GLAnimationManager.startAnimation(task);
				}
				mDesktopIndicator.setVisibility(View.INVISIBLE);
			}
				break;
			}	
			case IScreenFrameMsgId.GO_LOCKER_CHANGED: {
				if (mTextLayoutvisiable) {
				Animation animation = new Translate3DAnimation(0, 0,
						- GLCellLayout.sLongAxisEndPadding / 2, 0, 0, 0);
				animation.setDuration(250);
				if (animation != null && mTextLayout != null) {
					AnimationTask task = new AnimationTask(false,
							AnimationTask.PARALLEL);
					task.addAnimation(mTextLayout, animation, new AnimationListenerAdapter() {
						
						@Override
						public void onAnimationEnd(Animation arg0) {
							if (mTextLayoutvisiable) {
								mTextLayoutvisiable = false;
							}
						}
					});
					GLAnimationManager.startAnimation(task);
					mTextLayout.setVisibility(View.INVISIBLE);
				}
				mDesktopIndicator.setVisibility(View.VISIBLE);
				}
				break;
			}
			
			case IScreenFrameMsgId.REQUEST_SCREEN_EDIT_PUSH_LIST : {
				if (mPushController != null) {
					int typeId = (Integer) objects[0];
					String saveTimeKey = (String) objects[1];
					mPushController.requestPushData(typeId, saveTimeKey);
				}
			}
				break;
			
			case IScreenFrameMsgId.EFFECT_START_WAVE: {
				float centerX = (Float) objects[0];
				float centerY = (Float) objects[1];
				IShell iShell = (IShell) objects[2];
				EffectListener effectListener = (EffectListener) objects[3];
				long delay =  (Long) objects[4];

				float virtual[] = new float[2];
				mGLWorkspace.realPointToVirtual(centerX, centerY, virtual);
				int waveSize = DrawUtils.dip2px(10);
				int damping = 10;
				int waveDepth = 36;
				virtual[1] = virtual[1] + mGLWorkspace.getTranslateY()
							* GLWorkspace.sLayoutScale;

				int cx = (int) virtual[0];
				int cy = (int) virtual[1];
				if (iShell != null) {
					iShell.wave(IViewId.CORE_CONTAINER, cx, cy, 600, waveSize, waveDepth,
							damping, effectListener, delay);
				}
				break;
			}
			case IScreenFrameMsgId.SCREEN_ADD_SEARCH_WIDGET: {
				// 添加搜索组件
				addSearchWidget();
				break;
			}
			case IScreenFrameMsgId.SCREEN_SHOWING_AUTO_EFFECT : {
				ret = mGLWorkspace.isShowingAutoEffect();
				break;
			}
			
			
			//设置0屏指示器
			case IScreenFrameMsgId.SCREEN_ZERO_SETINDICATOR : {
				if (objects[0] != null && objects[0] instanceof Boolean) {
					setScreenIndicator((Boolean) objects[0]);
				}
				break;
			}
			//发给0屏的消息
			case IScreenFrameMsgId.SCREEN_ZERO_SEND_MESSAGE:
				if (mGLSuperWorkspace != null) {
					mGLSuperWorkspace.handMessage(sender, 1, msgId, param, objects[0], null);
				}	
				break;
			//0屏移动隐藏效果	
			case IScreenFrameMsgId.SCREEN_ZERO_INDICATOR_AND_DOCKMOVE : {
				float movePercent = (Float) objects[0];
				int alpha = (int) ((1 - movePercent) * 255);
				if (mDesktopIndicator != null) {
					mDesktopIndicator.setAlpha(alpha);
				}
				break;
			}
			
			//0屏移动隐藏效果	
			case IScreenFrameMsgId.SCREEN_SHOW_ZERO_SCREEN: {
				showZeroScreen(objects[0]);
				break;
			}
			
			case IFrameworkMsgId.SYSTEM_ON_RESUME : {
				param = mGLWorkspace.getCurrentScreen();
				fireWidgetVisible(true, param);
				break;
			}
			
			case IFrameworkMsgId.SYSTEM_ON_PAUSE : {
				param = mGLWorkspace.getCurrentScreen();
				fireWidgetVisible(false, param);
				break;
			}
			
			case IScreenFrameMsgId.SCREEN_GUIDE_ENTER_ANIM : {
				if (mGLWorkspace.mLoadFinish) {
					mGLWorkspace.startIOSAnimation();
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK,
							IDockMsgId.DOCK_START_IOS_ANIMATION, 0);
				}
				startTheme2MaskView();
				break;
			}
			case IScreenFrameMsgId.SCREEN_REMOVE_BLANK_SCREEN : {
				mNeedRequestLayout = mGLWorkspace.removeAddScreen();
				break;
			}
			case IScreenFrameMsgId.SCREEN_CLEAN_RESTOREAPPWIDGET_INFO: {
				mRestoreAppWidget = false;
				mRestoreScreenAppWidgetInfo = null;
				break;
			}
			case IScreenFrameMsgId.SCREEN_ADD_VIEW : {
				if (objects[0] instanceof ItemInfo) {
					addScreenView((ItemInfo) objects[0], param);
				}
				break;
			}
			
			case IScreenFrameMsgId.SCREEN_DELETE_VIEW : {
				if (objects[0] instanceof ItemInfo) {
					deleteItem((ItemInfo) objects[0], param);
				}
				break;
			}
			
			case IScreenFrameMsgId.SCREEN_RELOAD : {
				reloadDesktop();
			}
			    break;
			case IScreenFrameMsgId.SCREEN_LEAVE_EDIT_STATE: {
				if (mGLWorkspace.isEditState()) {
					leaveNewFolderState();
					mGLWorkspace.exitEditState();
				} 
				break;
			}
			case ICommonMsgId.SHOW_EXTEND_FUNC_VIEW : {
				boolean show = param == 1 ? true : false;
				int viewId = (Integer) objects[0];
				if (show) {
					if (mExtendFuncView == null) {
						showExtendFuncView(viewId);
					}
				} else {
					if (mExtendFuncView != null
							&& ((GLView) mExtendFuncView).getAnimation() == null) {
						hideExtendFuncView(viewId);
					}
				}
				break;
			}
			default :
				break;
		}
		return ret;
	}
	
	private void onSdCardReflush() {
		ArrayList<ItemInfo> changeItemInfos = mControler.handleSDIsReady();
		if (changeItemInfos != null && !changeItemInfos.isEmpty()) {
			mHandler.sendMessage(mHandler.obtainMessage(UPDATE_ITEMS_IN_SDCARD,
					changeItemInfos));
		}
		// 更新系统文件夹图标
		mHandler.sendEmptyMessage(UPDATE_ALL_FOLDER);
		// 停止正在编辑的widget
		stopWidgetEdit();
	}

	private boolean installShortcut(ShortCutInfo shortCutInfo) {
		if (shortCutInfo == null) {
			return false;
		}

		boolean ret = false;
		final int currnetScreen = mGLWorkspace.getCurrentScreen();
		int[] xy = new int[2];
		boolean isExistVacant = ScreenUtils.findVacant(xy, shortCutInfo.mSpanX,
				shortCutInfo.mSpanY, currnetScreen, mGLWorkspace);

		final int screenCount = mGLWorkspace.getChildCount();
		int screen = currnetScreen;
		if (!isExistVacant) {
			for (int i = 0; i < screenCount; i++) {
				if (i != currnetScreen) {
					isExistVacant = ScreenUtils.findVacant(xy, shortCutInfo.mSpanX,
							shortCutInfo.mSpanY, i, mGLWorkspace);

					if (isExistVacant) {
						screen = i;
						break;
					}
				}
			}
		}

		if (isExistVacant) {
			shortCutInfo.mCellX = xy[0];
			shortCutInfo.mCellY = xy[1];
			final GLView view = createDesktopView(shortCutInfo, screen, true);
			if (view != null) {
				final ItemInfo info = (ItemInfo) view.getTag();
				if (info != null) {
					try {
						mGLWorkspace.addInScreen(view, screen, info.mCellX, info.mCellY, info.mSpanX,
								info.mSpanY, false);
					} catch (IllegalStateException e) {
						e.printStackTrace();
						Log.e("illegalstateException", "IllegalStateException add in screen");
					}
				}
			}
			// addDesktopView(shortCutInfo, screen, true);
			addDesktopItem(screen, shortCutInfo);
			
			//记录是快捷方式的图标，一键清理屏幕需要用到
			AppManagerUtils.addShortCutIdToDB(ShellAdmin.sShellManager.getActivity(), shortCutInfo.mInScreenId);
			ret = true;
		}
		return ret;
	}
	
	private void handleOpenMenu() {
		if (mGLWorkspace.isEditState()) {
			leaveNewFolderState();
			mGLWorkspace.exitEditState();
			postDelayed(new Runnable() {

				@Override
				public void run() {
					showGGMenu(true);
				}
			}, GLWorkspace.ANIMDURATION + 50);
		} else {
			showGGMenu(true);
		}
	}
	
	/**
	 * <br>功能简述:清除15屏广告图标
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean clearAdvertIcon(Object object) {
		try {
			if (object == null || !(object instanceof ArrayList)) {
				return false;
			}
			
			//判断是否有5屏
			if (mGLWorkspace.getChildCount() != 5) {
				return false;
			}
			boolean cleanScreen1 = false;
			boolean cleanScreen5 = false;

			
			ArrayList<AdvertInfo> advertInfoList = (ArrayList<AdvertInfo>) object;
			for (AdvertInfo advertInfo : advertInfoList) {
				if (advertInfo.mScreen == 0) {
					cleanScreen1 = true;
				}
				
				if (advertInfo.mScreen == 4) {
					cleanScreen1 = true;
				}
			}
					
			//判断15屏是否插入过广告图标
			if (cleanScreen1) {
				if (!deleteOneScreenAllIcon(0)) {
					return false;	
				}
			}
			
			if (cleanScreen5) {
				if (!deleteOneScreenAllIcon(5)) {
					return false;	
				}
			}
			
			//清空首屏广告
			if (!deledAllHomeScreenAdverIcon(object)) {
				return false;	
			}
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * <br>功能简述:删除首屏幕所有广告图标
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param object
	 */
	public boolean deledAllHomeScreenAdverIcon(Object object) {
		try {
			ArrayList<ItemInfo> viewItemInfoList = new ArrayList<ItemInfo>();
			GLCellLayout homeCellLayout = mGLWorkspace.getScreenView(2);
			int homeCellLayoutSize = homeCellLayout.getChildCount();
			ArrayList<AdvertInfo> advertInfoList = (ArrayList<AdvertInfo>) object;
			for (AdvertInfo advertInfo : advertInfoList) {
				if (advertInfo.mScreen == 2) {
					for (int i = 0; i < homeCellLayoutSize; i++) {
						Object tag = homeCellLayout.getChildAt(i).getTag();
						if (tag != null && tag instanceof ItemInfo) {
							ItemInfo itemInfo = (ItemInfo) tag;
							if (advertInfo.mCellX == itemInfo.mCellX && advertInfo.mCellY == itemInfo.mCellY) {
								viewItemInfoList.add(itemInfo);	//先纪录当前屏幕图标信息
							}
						}
					}
				}
			}
			
			//遍历所有图标删除
			for (ItemInfo itemInfo : viewItemInfoList) {
				deleteItem(itemInfo, 2);
//				Log.i("lch", "首屏图标删除成功：" + itemInfo.toString());
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * <br>功能简述:设置首屏图标当前图标的信息缓存
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean setHomeScreenIconCache() {
		String iconInfoString = getHomeScreenIconInfo();
		AdvertHomeScreenUtils.saveHomeScreenCache(ShellAdmin.sShellManager.getActivity(), iconInfoString);
		return true;
	}
	
	/**
	 * <br>功能简述:获取首屏图标当前图标的信息
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public String getHomeScreenIconInfo() {
		//判断是否有5屏和是否首屏
		if (mGLWorkspace.getChildCount() != 5) {
			return null;
		}
		
		GLCellLayout cellLayout = mGLWorkspace.getScreenView(2); //获取首屏
		
		int screenViewSize = cellLayout.getChildCount();
		StringBuffer buffer = new StringBuffer();
		//遍历对应的屏幕控件
		for (int i = 0; i < screenViewSize; i++) {
			Object object = cellLayout.getChildAt(i).getTag();
			String cacheString = AdvertHomeScreenUtils.getIconInfoString(object);
			buffer.append(cacheString);
		}
		return buffer.toString();
	}
	
	/**
	 * <br>功能简述:插入广告图标到桌面
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param object ShortCutInfo
	 * @param addScreen 需要插入的屏幕
	 * @return
	 */
	private boolean addAdvertShortCut(Object object, int addScreen) {
		boolean ret = false;
		if (object == null || !(object instanceof ShortCutInfo)) {
			return ret;
		}

		int screenCount = mGLWorkspace.mScroller.getScreenCount();
		
		if (addScreen < 0 || addScreen >= screenCount) {
			return ret;
		}
		
		ShortCutInfo shortCutInfo = (ShortCutInfo) object;
		
		//如果是首屏要先判断对应的位置是否有图标。进行删除
		if (addScreen == 2) {
			deleteOneAdvertIcon(addScreen, shortCutInfo); //删除需要插入广告图标对应位置原有的图标
		}
		
		
		shortCutInfo.mSpanX = 1;
		shortCutInfo.mSpanY = 1;
		GLView bubble = createDesktopView(shortCutInfo, addScreen, true);
		
		mGLWorkspace.addInScreen(bubble, addScreen, shortCutInfo.mCellX, shortCutInfo.mCellY, 1, 1, true);
		addDesktopItem(addScreen, shortCutInfo);
		ret = true;
		return ret;
	}
	
	/**
	 * <br>功能简述:插入广告文件夹到桌面
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param object 文件夹
	 * @param addScreen 插入的屏幕
	 * @return
	 */
	private boolean addAdvertFolder(Object object, int addScreen) {
		boolean ret = false;
		// 添加一个新的文件夹
		if (object == null || !(object instanceof UserFolderInfo)) {
			return ret;
		}
		
		int screenCount = mGLWorkspace.mScroller.getScreenCount();
		
		if (addScreen < 0 || addScreen >= screenCount) {
			return ret;
		}
		
		UserFolderInfo folderInfo = (UserFolderInfo) object;
		
		//如果是首屏要先判断对应的位置是否有图标。进行删除
		if (addScreen == 2) {
			deleteOneAdvertIcon(addScreen, folderInfo); //删除需要插入广告图标对应位置原有的图标
		}
		
		try {
			addDesktopItem(addScreen, folderInfo);	//添加到数据库
			
			//关联图标，标题等
			GLView newFolder = createDesktopView(folderInfo, addScreen, true);
			
			//插入到屏幕
			mGLWorkspace.addInScreen(newFolder, addScreen, folderInfo.mCellX, folderInfo.mCellY, 1, 1, true);
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (ret) {
			//添加文件夹内容
			addUserFolderContent(folderInfo.mInScreenId, folderInfo, folderInfo.getContents(), false);
		}
			
		return ret;
	}
	
	/**
	 * <br>功能简述:删除需要插入广告图标对应位置原有的图标
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param screenIndex
	 * @param shortCutInfo
	 * @return
	 */
	public boolean deleteOneAdvertIcon(int screenIndex, ItemInfo shortCutInfo) {
		try {
			GLCellLayout cellLayout = mGLWorkspace.getScreenView(screenIndex);
			int firstCellLayoutSize = cellLayout.getChildCount();
			for (int i = 0; i < firstCellLayoutSize; i++) {
				Object object = cellLayout.getChildAt(i).getTag();
				if (object != null && object instanceof ItemInfo) {
					ItemInfo itemInfo =	(ItemInfo) object;
					if (shortCutInfo.mCellX == itemInfo.mCellX && shortCutInfo.mCellY == itemInfo.mCellY) {
						deleteItem(itemInfo, screenIndex);
//						Log.i("lch", "删除图标成功！");
						return true;
					}
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * <br>功能简述:清除指定屏幕里面的图标
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param screenIndex
	 * @return
	 */
	public boolean deleteOneScreenAllIcon(int screenIndex) {
		try {
			ArrayList<ItemInfo> viewItemInfoList = new ArrayList<ItemInfo>();
			GLCellLayout cellLayout = mGLWorkspace.getScreenView(screenIndex);
			int firstCellLayoutSize = cellLayout.getChildCount();
			for (int i = 0; i < firstCellLayoutSize; i++) {
				Object object = cellLayout.getChildAt(i).getTag();
				if (object != null && object instanceof ItemInfo) {
					viewItemInfoList.add((ItemInfo) object);	//先纪录当前屏幕图标信息
				}
			}
			
			//遍历所有图标删除
			for (ItemInfo itemInfo : viewItemInfoList) {
				deleteItem(itemInfo, screenIndex);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 删除屏幕一项
	 * 
	 * @param itemInfo
	 *            　
	 * @param screenindex
	 *            　屏幕索引，-1可以遍历屏幕
	 */
	public synchronized void deleteItem(ItemInfo itemInfo, int screenindex) {
		if (null == itemInfo) {
			return;
		}

		// 删除view
		GLView targetView = ScreenUtils
				.getViewByItemId(itemInfo.mInScreenId, screenindex, mGLWorkspace);
		if (targetView != null) {
			GLViewParent parent = targetView.getGLParent();
			if (parent != null && parent instanceof GLViewGroup) {
				((GLViewGroup) parent).removeView(targetView);
			}
		}

		mControler.removeDesktopItem(itemInfo);

		int type = itemInfo.mItemType;
		if (type == IItemType.ITEM_TYPE_SHORTCUT || type == IItemType.ITEM_TYPE_APP_WIDGET) {
			if (type == IItemType.ITEM_TYPE_SHORTCUT) {
				ScreenUtils.unbindShortcut((ShortCutInfo) itemInfo);
			}

			if (itemInfo instanceof ScreenAppWidgetInfo) {
				int widgetId = ((ScreenAppWidgetInfo) itemInfo).mAppWidgetId;
				if (GoWidgetManager.isGoWidget(widgetId)) {
					AppCore.getInstance().getGoWidgetManager().deleteWidget(widgetId);
				} else
				// 系统widget
				{
					mWidgetHost.deleteAppWidgetId(widgetId);
				}
			}
		} else if (type == IItemType.ITEM_TYPE_USER_FOLDER) {
			// 删除文件夹
			ScreenUtils.unbindeUserFolder((UserFolderInfo) itemInfo);
			mControler.removeUserFolder(itemInfo);
		}
	}
	
	/**
	 * GGMenu显示或隐藏时，屏幕层进行缩小或放大的动画
	 * @param show
	 * @param needAnimation
	 */
	private void onGGMenuShow(final boolean show, boolean needAnimation,
			boolean needKeepWorkspaceScale) {
		final GLContentView rootView = getGLRootView();
		if (null == rootView) {
			return;
		}
		GLCellLayout layout = mGLWorkspace.getCurrentScreenView();
		if (layout != null) {
			mHasGGMenuShow = show;
			if (show) {
				handleMessage(this, IScreenFrameMsgId.SCREEN_HIDE_CURRENT_GUIDE, 0);
			} else {
				handleMessage(this, IScreenFrameMsgId.SCREEN_RESHOW_LAST_GUIDE, 0);
			}
			layout.setHasPixelOverlayed(false);
			// 不需要保持workspace的缩放状态
			if (!needKeepWorkspaceScale) {
//				mDock.setVisible(!show, needAnimation, null);
				if (!show) {
					mShell.show(IViewId.DOCK, needAnimation);
				} else {
					mShell.hide(IViewId.DOCK, needAnimation);
				}
				mGLWorkspace.scaleForMenuPreAction(show, needAnimation);
			}
			requestLayout();
		}
	}

	private boolean onHomeAction(GestureSettingInfo info) {
		boolean ret = false;
		if (isVisible()) {
			PopupWindowControler popupWindowControler = mShell.getPopupWindowControler();
			if (popupWindowControler.isShowing()) {
				hidePopupWindow(false);
				ret = true;
			}
//			if (info.mGoShortCut != GlobalSetConfig.GESTURE_SHOW_MENU
//					&& MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
//							IAppCoreMsgId.MENU_IS_SHOWING, -1, null, null)) {
//				showGGMenu(true);
//				ret = true;
//			}
			if (mExtendFuncView != null && ((GLView) mExtendFuncView).getAnimation() == null) {
				hideExtendFuncView(mExtendFuncView.getViewId());
				ret = true;
			}
		}
		return ret;
	}

	// 添加快捷方式
	private boolean addShortcut(Object object) {
		boolean ret = false;
		if (object == null || !(object instanceof ShortCutInfo)) {
			return ret;
		}
		ShortCutInfo itemInfo = (ShortCutInfo) object;
		int[] xy = new int[2];
		boolean vacant = ScreenUtils.findVacant(xy, 1, 1, mGLWorkspace.getCurrentScreen(),
				mGLWorkspace);
		if (!vacant) {
			setScreenRedBg();
		} else {
			//				BubbleTextView bubble = inflateBubbleTextView(itemInfo.mTitle, itemInfo.mIcon, itemInfo);
			GLView view = createShortcutIcon((ShortCutInfo) itemInfo, true);
			itemInfo.mCellX = xy[0];
			itemInfo.mCellY = xy[1];
			itemInfo.mSpanX = 1;
			itemInfo.mSpanY = 1;
			mGLWorkspace.addInCurrentScreen(view, xy[0], xy[1], 1, 1);
			addDesktopItem(mGLWorkspace.getCurrentScreen(), itemInfo);
			ret = true;
		}
		return ret;
	}

	// 添加快捷方式(双11专用)
	private boolean addShortcutForEleven() {
		
		if (isLoading()) {
			return false;
		}
		
		ScreenIconBeanForEleven elevenBean = ScreenIconForElevenController
				.getController(mContext).getBean();

		if (elevenBean == null) {
			return false;
		}

		if (elevenBean.mDrawable == null) {

			return false;
		}

		PreferencesManager pm = new PreferencesManager(mContext);

		boolean ret = false;

		if (pm.getInt(IPreferencesIds.DOUBLE_ELEVEN_IS_ADD_SC, -1) == Integer
				.valueOf(elevenBean.mVersion)) {
			return false;
		}

		if (elevenBean.isVaild()) {
			if (elevenBean.getNextScheduleTime() != 0) {
				return false;
			}
		} else {
			return false;
		}

		ShortCutInfo itemInfo = new ShortCutInfo();
		itemInfo.mTitle = elevenBean.mName;
		itemInfo.mIcon = elevenBean.mDrawable;
		itemInfo.mItemType = IItemType.ITEM_TYPE_SHORTCUT;
		Uri uri = Uri.parse(elevenBean.mUrl);
		Intent intent = new Intent(ICustomAction.ACTION_DOUBLE11_SHORTCUT_URL
				+ elevenBean.mVersion);
		intent.setData(uri);
		itemInfo.mIntent = intent;

		int[] xy = new int[2];
		int totalScreenNum = mGLWorkspace.getChildCount();
		int mainScreen = mGLWorkspace.getMainScreen();
		int insertScreen = mainScreen;
		boolean vacant = false;
		vacant = ScreenUtils.findVacant(xy, 1, 1, mainScreen, mGLWorkspace);
		if (!vacant) {
			int k = totalScreenNum / 2;
			if (!(totalScreenNum % 2 == 0)) {
				k = k + 1;
			}
			for (int i = 1; i <= k; i++) {
				insertScreen = mainScreen - i;
				if (insertScreen >= 0) {
					// 左边
					vacant = ScreenUtils.findVacant(xy, 1, 1, insertScreen,
							mGLWorkspace);
					if (vacant) {
						insertBubbleElevenView(itemInfo, insertScreen, xy, pm,
								Integer.valueOf(elevenBean.mVersion));
						ret = true;
						return ret;
					} else {
						insertScreen = mainScreen + i;
						if (insertScreen < totalScreenNum) {
							vacant = ScreenUtils.findVacant(xy, 1, 1,
									mainScreen + i, mGLWorkspace);
							if (vacant) {
								insertBubbleElevenView(itemInfo, insertScreen,
										xy, pm,
										Integer.valueOf(elevenBean.mVersion));
								ret = true;
								return ret;
							}
						}
					}
				} else {
					insertScreen = mainScreen + i;
					if (insertScreen < totalScreenNum) {
						vacant = ScreenUtils.findVacant(xy, 1, 1, insertScreen,
								mGLWorkspace);
						if (vacant) {
							insertBubbleElevenView(itemInfo, insertScreen, xy,
									pm, Integer.valueOf(elevenBean.mVersion));
							ret = true;
							return ret;
						}
					}
				}
			}

		} else {
			insertBubbleElevenView(itemInfo, insertScreen, xy, pm,
					Integer.valueOf(elevenBean.mVersion));
			ret = true;
		}
		
		// 双11未读数字
		if (ret && itemInfo.getRelativeItemInfo() != null) {
			int count = elevenBean.mCount != null ? Integer.valueOf(elevenBean.mCount) : 0;
			if (count > 0) {
				itemInfo.getRelativeItemInfo().setUnreadCount(count);
				itemInfo.getRelativeItemInfo().setNotificationType(NotificationType.NOTIFICATIONTYPE_MORE_APP);
				MsgMgrProxy.sendBroadcast(this, ICommonMsgId.NOTIFICATION_CHANGED,
						NotificationType.NOTIFICATIONTYPE_MORE_APP, elevenBean.mCount);
				Double11NotificationController.addRecord(ShellAdmin.sShellManager.getActivity(), itemInfo.mIntent, count);
			}
		}
		
		return ret;
	}
	
	//添加双11的图标
		private void insertBubbleElevenView(ShortCutInfo itemInfo,
			int insertScreen, int[] xy, PreferencesManager pm, int version) {

		GLView view = createShortcutIcon((ShortCutInfo) itemInfo, true);
		itemInfo.mCellX = xy[0];
		itemInfo.mCellY = xy[1];
		itemInfo.mSpanX = 1;
		itemInfo.mSpanY = 1;
		mGLWorkspace.addInScreen(view, insertScreen, xy[0], xy[1], 1, 1, true);
		addDesktopItem(insertScreen, itemInfo);
		pm.putInt(IPreferencesIds.DOUBLE_ELEVEN_IS_ADD_SC, version);
		pm.commit();
		ScreenIconForElevenController
		.getController(mContext).setBeanNull();
	}
			
		/**
		 * <br>功能简述:时间dstTime是否在begin~end区间内
		 * <br>功能详细描述:
		 * <br>注意:
		 * @param dstTime
		 * @param begin
		 * @param end
		 * @return
		 */
	private boolean isInTime(String dstTime, String begin, String end) {
		if (dstTime == null
				|| begin == null
				|| end == null
				|| (dstTime.compareTo(begin) >= 0 && dstTime.compareTo(end) <= 0)) {
			return true;
		}
		return false;
	}

	// 添加文件夹
	private boolean addLiveFolder(Object object) {
		boolean ret = false;
		return ret;
	}
	private boolean validateRect(Rect rect) {
		if (mGLWidgetView == null || mGLWidgetView.getTag() == null) {
			return false;
		}

		int[] position = new int[4];
		boolean collision = false;
		GLCellLayout cl = (GLCellLayout) mGLWorkspace.getChildAt(mGLWorkspace.getCurrentScreen());
		cl.rectToPosition(rect, position);
		ScreenAppWidgetInfo widgetInfo = (ScreenAppWidgetInfo) mGLWidgetView.getTag();
		collision = ScreenUtils.ocuppiedArea(mGLWorkspace.getCurrentScreen(),
				widgetInfo.mAppWidgetId, new Rect(position[0], position[1], position[0]
						+ position[2], position[1] + position[3]), mGLWorkspace);
		if (!collision) {
			//检查是直接删除widget重新添加一个还是只是更改当前widget布局宽高
			boolean isNeedChange = checkWidgetNeedChange(widgetInfo, new Rect(position[0], position[1], position[0]
					+ position[2], position[1] + position[3]));
			if (!isNeedChange) {
				GLCellLayout.LayoutParams lp = (GLCellLayout.LayoutParams) mGLWidgetView
						.getLayoutParams();
				lp.cellX = position[0];
				lp.cellY = position[1];
				lp.cellHSpan = position[2];
				lp.cellVSpan = position[3];
				mGLWidgetView.requestLayout();

				ScreenAppWidgetInfo info = (ScreenAppWidgetInfo) mGLWidgetView.getTag();
				info.mCellX = position[0];
				info.mCellY = position[1];
				info.mSpanX = position[2];
				info.mSpanY = position[3];

				final int appWidgetId = info.mAppWidgetId;
				if (!GoWidgetManager.isGoWidget(appWidgetId)) {
					final Intent motosize = new Intent(ICustomAction.ACTION_SET_WIDGET_SIZE);
					final AppWidgetProviderInfo appWidgetInfo = mWidgetManager
							.getAppWidgetInfo(appWidgetId);
					if (appWidgetInfo != null) {
						motosize.setComponent(appWidgetInfo.provider);
					}

					motosize.putExtra("appWidgetId", appWidgetId);
					motosize.putExtra("com.motorola.blur.home.EXTRA_NEW_WIDGET", true);

					motosize.putExtra("spanX", lp.cellHSpan);
					motosize.putExtra("spanY", lp.cellVSpan);

					// send the broadcast
					ShellAdmin.sShellManager.getActivity().sendBroadcast(motosize);
					
					if (Build.VERSION.SDK_INT >= 16 && appWidgetId > 0) { // 系统widget且SDK为4.1以上
						Bundle options = new Bundle();
						options.putInt(AppWidgetUpdateOptionsProxy.OPTION_APPWIDGET_MIN_WIDTH,
								(int) (widgetInfo.mSpanX * GLCellLayout.sCellRealWidth / DrawUtils.sDensity));
						options.putInt(AppWidgetUpdateOptionsProxy.OPTION_APPWIDGET_MIN_HEIGHT,
								(int) (widgetInfo.mSpanY * GLCellLayout.sCellRealHeight / DrawUtils.sDensity));
						options.putInt(AppWidgetUpdateOptionsProxy.OPTION_APPWIDGET_MAX_WIDTH,
								(int) (widgetInfo.mSpanX * GLCellLayout.sCellRealWidth / DrawUtils.sDensity));
						options.putInt(AppWidgetUpdateOptionsProxy.OPTION_APPWIDGET_MAX_HEIGHT,
								(int) (widgetInfo.mSpanY * GLCellLayout.sCellRealHeight / DrawUtils.sDensity));
						AppWidgetUpdateOptionsProxy.updateAppWidgetOptions(mWidgetManager, appWidgetId,
								options);
					}
				}
			}
		}
		return collision;
	}

	/**
	  * 检查widget是否需要更改样式
	  * 只针对go天气和go任务管理器
	  * @param curwidgetView
	  * @param rect
	  * @return
	  */
	private boolean checkWidgetNeedChange(ScreenAppWidgetInfo curwidgetView, Rect rect) {
		try {
			//通过id获取当前widgt的基本信息
			GoWidgetManager goWidgetManager = AppCore.getInstance().getGoWidgetManager();
			GoWidgetBaseInfo curWidgetInfo = goWidgetManager.getWidgetInfo(curwidgetView.mAppWidgetId);
			if (null == curWidgetInfo) {
				return false;
			}
			
			//不用goWidgetBaseInfo.mPackage获取包名。因为go任务管理器要特殊处理
			String packageName = goWidgetManager.getWidgetPackage(curWidgetInfo); 
//			Log.i("lch", "packageName:" + packageName);
			if (packageName == null) {
				return false;
			}
			
			//获取包命对应widget的样式列表
		    ArrayList<WidgetParseInfo> widgetStyleList = com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenUtils.getWidgetStyle(ShellAdmin.sShellManager.getActivity(), packageName);

		    //获取widget对应的信息
		    GoWidgetProviderInfo widgetProviderInfo = com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenUtils.getWidgetProviderInfo(ShellAdmin.sShellManager.getActivity(), packageName);
			
		    
		    WidgetParseInfo newWidgetInfo = null;
			
		    //go天气
			if (packageName.equals(PackageName.RECOMMAND_GOWEATHEREX_PACKAGE)) {
				 //获取符合匹配的go天气样式
				newWidgetInfo = com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenUtils.getWeatherWidgetStyle(widgetStyleList, widgetProviderInfo, rect, curwidgetView.mSpanX, curwidgetView.mSpanY, curWidgetInfo);
				
			}
			
			//go任务管理器,有旧版本和新版本2个
			else if (packageName.equals(PackageName.TASK_PACKAGE) || packageName.equals(PackageName.RECOMMAND_GOTASKMANAGER_PACKAGE)) {
				 //获取符合匹配的go天气样式
				newWidgetInfo = com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenUtils.getTaskWidgetStyle(widgetStyleList, widgetProviderInfo, rect, curwidgetView.mSpanX, curwidgetView.mSpanY, curWidgetInfo);
			}

			else {
				return false;
			}
			
			if (newWidgetInfo == null) {
				return false;
			}
			
		   
			
			
			GoWidgetBaseInfo info = new GoWidgetBaseInfo();
			info.mWidgetId = goWidgetManager.allocateWidgetId();
			info.mType = newWidgetInfo.type;
			info.mLayout = newWidgetInfo.layoutID;
			info.mTheme = curWidgetInfo.mTheme;
			info.mThemeId = curWidgetInfo.mThemeId;
			info.mPrototype = curWidgetInfo.mPrototype;
			info.mReplaceGroup = curWidgetInfo.mReplaceGroup;
			AppWidgetProviderInfo provider = widgetProviderInfo.getProviderInfo();
			String widgetPackage = provider.provider.getPackageName();
			if (!"".equals(newWidgetInfo.longkeyConfigActivty)) {
				ComponentName temp = new ComponentName(widgetPackage, newWidgetInfo.longkeyConfigActivty);
				provider.configure = temp;
			}
			if (provider != null) {
				provider.minHeight = DrawUtils.dip2px(newWidgetInfo.minHeight);
				provider.minWidth = DrawUtils.dip2px(newWidgetInfo.minWidth);
				if (provider.provider != null) {
					info.mPackage = provider.provider.getPackageName();
				}
				if (provider.configure != null) {
					info.mClassName = provider.configure.getClassName();
				}
			}
			InnerWidgetInfo innerWidgetInfo = goWidgetManager.getInnerWidgetInfo(info.mPrototype);
			//go任务管理器以前是内置了。所以还残留内置的代码。这里需要做一下判断
			// 内置
			if (innerWidgetInfo != null) {
				// 更新包名为实际inflate xml的包名
				info.mPackage = innerWidgetInfo.mInflatePkg;
				info.mPrototype = innerWidgetInfo.mPrototype;
			}
			
			//设置传递参数，传递给gowidget内部使用
			Bundle bundle = new Bundle();
			bundle.putInt(GoWidgetConstant.GOWIDGET_ID, info.mWidgetId);
			bundle.putInt(GoWidgetConstant.GOWIDGET_TYPE, info.mType);
			bundle.putString(GoWidgetConstant.GOWIDGET_LAYOUT, info.mLayout);
			bundle.putString(GoWidgetConstant.GOWIDGET_THEME, info.mTheme);
			bundle.putInt(GoWidgetConstant.GOWIDGET_THEMEID, info.mThemeId);
			bundle.putInt(GoWidgetConstant.GOWIDGET_PROTOTYPE, info.mPrototype); // 内置类型
			bundle.putParcelable(GoWidgetConstant.GOWIDGET_PROVIDER, provider);
			bundle.putBoolean(GoWidgetConstant.GOWIDGET_ADD_TO_SCREEN, true);
			bundle.putInt(com.jiubang.ggheart.apps.gowidget.GoWidgetConstant.REPLACE_GROUP, info.mReplaceGroup);
			
			int curScreen = mGLWorkspace.getCurrentScreen(); //当前屏幕数
			deleteScreenItem(curwidgetView, curScreen, false); //删除当前widget
			changeNewGoWidget(info, bundle, curScreen, rect); //重新添加新的widget
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 切换新样式widget
	 * 只针对go天气和go任务管理器
	 * @param info
	 * @param bundle
	 * @param screenindex
	 * @param rect
	 * @return
	 */
	private boolean changeNewGoWidget(GoWidgetBaseInfo info, Bundle bundle, int screenindex, Rect rect) {
		if (bundle == null || info == null) {
			return false;
		}

		boolean ret = false;
		
		//创建新的widget
		GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
		boolean add = widgetManager.addGoWidget(info);
		
		GLCellLayout cellLayout = mGLWorkspace.getScreenView(screenindex);
		if (cellLayout == null || !add) {
			return false;
		}

		int cellX =  rect.left; //x坐标
		int cellY =  rect.top;  //y坐标
		int cols = rect.width(); //列数
		int rows = rect.height(); //行数
		
		GLView widgetView = m3DWidgetManager.createView(info.mWidgetId); //创建新的widget
		if (widgetView != null) {
			ScreenAppWidgetInfo appWidgetInfo = new ScreenAppWidgetInfo(info.mWidgetId);
			widgetView.setTag(appWidgetInfo);
			appWidgetInfo.mCellX = cellX; 
			appWidgetInfo.mCellY = cellY;
			appWidgetInfo.mSpanX = cols; 
			appWidgetInfo.mSpanY = rows;
			mGLWorkspace.addInScreen(widgetView, screenindex, cellX, cellY, cols, rows, false); //添加到桌面
			m3DWidgetManager.startWidget(info.mWidgetId, bundle); //启动widget内部的初始化
			addDesktopItem(screenindex, appWidgetInfo); //写入数据库
			
			mGLWidgetView = widgetView; //重新设置当前编辑的widget是新的widget
			ret = true;
		}
			
		if (!ret) {
			widgetManager.deleteWidget(info.mWidgetId);
		}

		return ret;
	}
	/**
	 * 通知widget进入/离开屏幕显示区域
	 * 
	 * @param visible
	 * @throws Exception 
	 */
	private void fireWidgetVisible(boolean visible, int screenIndex) {
		try {
			final GLWorkspace.WidgetRunnable r = mGLWorkspace.getWidgetRunnable(visible);
			r.setScreen((GLViewGroup) mGLWorkspace.getChildAt(screenIndex));
			AsyncHandler asyncHandler = m3DWidgetManager.getAsyncHandler();

			if (visible) {
				// 通知进入需要延时
				asyncHandler.postDelayed(r, GLWorkspace.FIRE_WIDGET_DELAY);
			} else {
				// 先取消掉之前的进入通知
				asyncHandler.removeCallbacks(mGLWorkspace.getWidgetRunnable(true));
				asyncHandler.post(r);
			}
		} catch (Exception e) {
			Log.i(VIEW_LOG_TAG, "fireVisible err " + visible);
		}
	}

	@Override
	public int getMsgHandlerId() {
		return IDiyFrameIds.SCREEN;
	}

	// 接到后台消息后加载桌面
	private void loadScreen() {
		if (!mInitWorkspace) {
			loadScreenInfo();
			mHandler.sendEmptyMessage(START_DESKTOP_LOADER);
		}

	}

	// 加载屏幕信息
	private void loadScreenInfo() {
		if (null != mControler) {
			mControler.loadScreen();
		}
	}

	private void startDesktopLoader() {
		setLoading(true);
		mGLBinder = new GLDesktopBinder(this, mControler.getShortCutLinkList(mCurrentScreen));
		mGLBinder.startBinding();
	}

	private void stopDesktopLoader() {
		if (mGLBinder != null) {
			// 停止加载组件
			mGLBinder.cancel();
			mGLBinder = null;
		}
		setLoading(false);
	}

	public void bindShortcut(LinkedList<ItemInfo> shortcuts) {
		int count = Math.min(GLDesktopBinder.ITEMS_COUNT, shortcuts.size());
		// Log.i("jiang", "count:" + count);
		while (count-- > 0) {
			ItemInfo itemInfo = shortcuts.removeFirst();
			GLView addView = null;
			boolean isBreak = false;
			if (itemInfo.mItemType == IItemType.ITEM_TYPE_APP_WIDGET) {
				addView = filterWidgetView((ScreenAppWidgetInfo) itemInfo);
				isBreak = true;
			} else {
				addView = createDesktopView(itemInfo, itemInfo.mScreenIndex, false);
				if (itemInfo.mItemType == IItemType.ITEM_TYPE_FAVORITE) {
					isBreak = true;
				}
			}

			if (addView != null) {
				mGLWorkspace.addInScreen(addView, itemInfo.mScreenIndex, itemInfo.mCellX,
						itemInfo.mCellY, itemInfo.mSpanX, itemInfo.mSpanY, false);
				mGLWorkspace.postInvalidate();
				this.postInvalidate();

				if (isBreak) {
					break;
				}
			} /*
				* else if (addView == null && itemInfo.mItemType == IItemType.ITEM_TYPE_APP_WIDGET) {
				* ScreenMissIconBugUtil.showToast( ScreenMissIconBugUtil.ERROR_ADDVIEW_WIDGET_NULL); }
				*/
		}

		if (shortcuts.isEmpty()) {
			if (mGLBinder != null) {
				mGLBinder.notifyLoadFinish();
			}
		}
	}

	// 添加文件夹内容
	public void addUserFolderContent(long folderId, UserFolderInfo folderInfo,
			ArrayList<ItemInfo> items, boolean isFromDrawer) {
		if (null == items) {
			mControler.addUserFolderContent(folderInfo, isFromDrawer);
		} else {
			mControler.addUserFolderContent(folderId, items, isFromDrawer);
		}
	}
	
	private void handleAddSysWidget(SysSubWidgetInfo info) {
		int appWidgetId = mWidgetHost.allocateAppWidgetId();
		
		if (info.getCustomerInfo() != null 
				&& info.getCustomerInfo().equals(ScreenEditConstants.SEARCH_WIDGET_TAG)) {
			Intent intent = new Intent();
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			intent.putExtra(DiyScheduler.EXTRA_CUSTOM_WIDGET, DiyScheduler.SEARCH_WIDGET);

			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IScreenFrameMsgId.CUSTOMER_PICK_WIDGET, Activity.RESULT_OK, intent, null);
			return;
		}
		
		boolean allow = false;
		try {
			Method method = AppWidgetManager.class.getMethod("bindAppWidgetIdIfAllowed", int.class, ComponentName.class);
			allow = (Boolean) method.invoke(mWidgetManager, appWidgetId,
					info.getProviderInfo().provider);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		if (allow) {
			Intent intent = new Intent();
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			
			if (info.getCustomerInfo() != null && 
					info.getCustomerInfo().equals(ScreenEditConstants.SEARCH_WIDGET_TAG)) {
				intent.putExtra(DiyScheduler.EXTRA_CUSTOM_WIDGET, DiyScheduler.SEARCH_WIDGET);
			}
			
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IScreenFrameMsgId.CUSTOMER_PICK_WIDGET, Activity.RESULT_OK, intent, null);
		} else {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IAppCoreMsgId.BIND_SYSTEM_WIDGET, appWidgetId, info, null);
		}
	}

	private void flyIconToScreen(Object object, List<?> objects) {
		if (objects != null && objects.size() > 0 && objects.get(0) instanceof Rect) {
			//			// 最后放置的位置
			//			mGLViewTemp = (View) object;
			//			int screenIndex = mGLWorkspace.getCurrentScreen();
			//			Object tagObject = ((GLView) object).getTag();
			//			if (tagObject == null) {
			//				return;
			//			}
			//			if (mRectTemp == null) {
			//				Rect rect = (Rect) objects.get(0);
			//				mRectTemp = rect;
			//			}
			//			int realX = mRectTemp.left;
			//			int realY = mRectTemp.top;
			//			mRectTemp = null;
			//			if (Workspace.getLayoutScale() < 1.0) {
			//				float[] realXY = new float[2];
			//				// 先转换为真实值
			//				Workspace.virtualPointToReal(realX, realY, realXY);
			//				realX = (int) realXY[0];
			//				realY = (int) realXY[1];
			//			}
			//			if (mDragType == DragFrame.TYPE_ADD_APP_DRAG) {
			//				if (!mGLWorkspace.isOverWorkspace(realX, realY)) {
			//					return;
			//				}
			//			}
			// 发消息到添加层,添加widget至桌面
			if (object instanceof AbsWidgetInfo) {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_EDIT,
						IScreenEditMsgId.SCREEN_EDIT_ADD_GOWIDGET_TO_SCREEN,
						((AbsWidgetInfo) object).getAddIndex());
				return;
			} else if (object instanceof SysSubWidgetInfo) {
				handleAddSysWidget((SysSubWidgetInfo) object);
				return;
			}

			//			ItemInfo tagInfo = null;
			//			if (tagObject instanceof ItemInfo) {
			//				tagInfo = (ItemInfo) tagObject;
			//			}
			//
			//			if (tagInfo == null) {
			//				return;
			//			}
			// change 位置不需要再次计算
			// int[] xy = mGLWorkspace.estimateDropCell(realX, realY,
			// tagInfo.mSpanX, tagInfo.mSpanY,
			// null, screenIndex, null);
			//			if (mCellPos == null) {
			//				setScreenRedBg();
			//			} else {
			//				if (mCurrentFolderInfo != null) {
			//					if (null != mControler) {
			//						mControler.moveDesktopItemFromFolder(tagInfo, screenIndex,
			//								mCurrentFolderInfo.mInScreenId);
			//						MsgMgrProxy.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
			//								IScreenFrameMsgId.DELETE_CACHE_INFO_IN_FOLDER, -1, tagInfo, null);
			//					}
			//					// 更新缓存
			//					if (mCurrentFolderInfo instanceof UserFolderInfo) {
			//						((UserFolderInfo) mCurrentFolderInfo).remove(tagInfo.mInScreenId);
			//						if (!deleteFolderOrNot((UserFolderInfo) mCurrentFolderInfo, true)) {
			//							// if (tagInfo instanceof ShortCutInfo) {
			//							// int type =
			//							// AppIdentifier.whichTypeOfNotification(mActivity,
			//							// (( ShortCutInfo ) tagInfo).mIntent);
			//							// if (type != NotificationType.IS_NOT_NOTIFICSTION)
			//							// {
			//							// (( UserFolderInfo )
			//							// mCurrentFolderInfo).mTotleUnreadCount -= ((
			//							// ShortCutInfo ) tagInfo).mCounter;
			//							// }
			//							// }
			//							// 更新文件夹图标
			//							updateFolderIconAsync((UserFolderInfo) mCurrentFolderInfo, false, false);
			//						}
			//					}
			//					mCurrentFolderInfo = null;
			//				}
			//				if (tagInfo instanceof ShortCutInfo) {
			//					Log.i(LogConstants.HEART_TAG, "drag over app");
			//					mGLWorkspace.blankCellToNormal(mGLWorkspace.getCurrentScreenView());
			//					// 应用程序图标
			//					tagInfo.mCellX = mCellPos[0];
			//					tagInfo.mCellY = mCellPos[1];
			//					addDesktopItem(screenIndex, tagInfo);
			//					addDesktopView(tagInfo, screenIndex, true);
			//				}
			//			}
			//			mCellPos = null;
			//			clearDragState();
			//			if (null != getCurrentScreen()) {
			//				getCurrentScreen().setStatusNormal();
			//			}
		}
	}

	private synchronized GLView createDesktopView(ItemInfo itemInfo, int screenIndex, boolean sync) {

		if (itemInfo == null) {
			// ScreenMissIconBugUtil
			// .showToast(ScreenMissIconBugUtil.ERROR_CREATEDESKTOPVIEW_APP_INFO_NULL);
			return null;
		}

		GLView addView = null;
		itemInfo.mScreenIndex = screenIndex;
		switch (itemInfo.mItemType) {
			case IItemType.ITEM_TYPE_APPLICATION :
				addView = createShortcutIcon((ShortCutInfo) itemInfo, sync);
				break;

			case IItemType.ITEM_TYPE_SHORTCUT :
				addView = createShortcutIcon((ShortCutInfo) itemInfo, true);
				break;

			/*
			 * case IItemType.ITEM_TYPE_APP_WIDGET : addView = createAppWidgetView(itemInfo); break;
			 * 
			 * case IItemType.ITEM_TYPE_LIVE_FOLDER : addView = createLiveFolder(itemInfo); break;
			 * 
			 * case IItemType.ITEM_TYPE_USER_FOLDER : addView = createUserFolder(itemInfo); break;
			 * 
			 */
			case IItemType.ITEM_TYPE_FAVORITE :
				addView = createFavoriteView(itemInfo);
				break;

			case IItemType.ITEM_TYPE_USER_FOLDER :
				addView = createUserFolder(itemInfo);
				break;
			default :
				break;
		}
		return addView;
	}

	private GLView createShortcutIcon(ShortCutInfo item, boolean sync) {
		if (item == null) {
			return null;
		}

		if (sync) {
			loadCompleteInfo(item);
			return inflateIconView(item.mTitle, item.mIcon, item);
		} else {
			if (mGLBinder != null) {
				mGLBinder.loadShortcutAsync(item);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private GLView createUserFolder(ItemInfo item) {
		if (item != null && item instanceof UserFolderInfo) {
			UserFolderInfo folderInfo = (UserFolderInfo) item;
			/*
			 * FolderIcon newFolder = FolderIcon.fromXml(getFolderIconId(), IShell.this,
			 * mGLWorkspace.getCurrentScreenView(), folderInfo, getDisplayTitle(folderInfo.mTitle));
			 * newFolder.setTag(folderInfo); newFolder.close();
			 * newFolder.setShowShadow(getShadowState());
			 */
			// customDeskTopBackground(newFolder);
			// GLFrameLayout iconFrameLayout = (GLFrameLayout) mGlInflater.inflate(
			// R.layout.gl_screen_shortcut_icon, null);
			// ArrayList<ItemInfo> infos = mControler.getFolderItems(folderInfo.mInScreenId);
			// folderInfo.addAll(infos);
			GLAppFolderInfo appFolderInfo = new GLAppFolderInfo(folderInfo,
					GLAppFolderInfo.FOLDER_FROM_SCREEN);
			GLAppFolderController.getInstance().addFolderInfo(appFolderInfo);
			GLScreenFolderIcon folderIcon = (GLScreenFolderIcon) GLAppFolder.getInstance()
					.getFolderIcon(appFolderInfo);
			folderIcon.setInfo(folderInfo);
			if (folderInfo.mFeatureIconType == ImagePreviewResultType.TYPE_PACKAGE_RESOURCE
					|| folderInfo.mFeatureIconType == ImagePreviewResultType.TYPE_IMAGE_FILE
					|| folderInfo.mFeatureIconType == ImagePreviewResultType.TYPE_APP_ICON) {
				folderIcon.useFolderFeatureIcon();
			}
			return folderIcon;
		}
		return null;
	}

	private GLView createFavoriteView(ItemInfo item) {
		if (item instanceof FavoriteInfo) {
			FavoriteInfo favInfo = mControler.getFavoriteInfo((FavoriteInfo) item);
			if (favInfo != null && favInfo.mPreview > 0) {
				GLLayoutInflater layoutInflater = ShellAdmin.sShellManager.getLayoutInflater();
				GLImageView imageView = (GLImageView) layoutInflater.inflate(
						R.layout.gl_favorite_widget, null);
				// 因为资源是放在主包，所以要用主包的context
				imageView.setImageDrawable(ShellAdmin.sShellManager.getActivity().getResources()
						.getDrawable(favInfo.mPreview));
				GLWidgetContainer favorite = new GLWidgetContainer(getContext(), imageView);
				imageView.setOnClickListener(favorite);
				favorite.setTag(item);
				return favorite;
			}
		}
		return null;
	}

	/*
	 * private int getFolderIconId() { return GoLauncher.isLargeIcon() ? R.layout.folder_icon_large
	 * : R.layout.folder_icon; }
	 * 
	 * private CharSequence getDisplayTitle(CharSequence title) { 增加判断是否需要显示名称 if
	 * (mDesktopSettingInfo != null && !mDesktopSettingInfo.isShowTitle()) { return null; } return
	 * title; }
	 */

	public void loadCompleteInfo(ShortCutInfo info) {
		if (info == null || info.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
			return;
		}

		// 如果是系统应用程序，在这里更新一次图标和title
		final AppDataEngine dataEngine = AppDataEngine.getInstance(ApplicationProxy.getContext());
		final AppItemInfo ainfo = dataEngine.getCompletedAppItem(info.mIntent);
		if (ainfo != null) {
			// 需要判断是自定义图标还是系统图标来进行是否修改图标的值
			if (!info.mIsUserIcon || info.mIcon == null) {
				// 在没有使用自定义图标或者没有图标信息的情况下赋予程序图标
				info.mIcon = ainfo.getIcon();
			}

			if (!info.mIsUserTitle || info.mTitle == null) {
				// 在没有使用自定义title或没有title信息情况下赋予程序title信息
				info.mTitle = ainfo.getTitle();
			}
		}
	}

	private GLView inflateIconView(CharSequence title, Drawable icon, ShortCutInfo info) {
		// GLView iconFrameLayout = null ;
		/*
		 * // OutOfMemoryHandler.gcIfAllocateOutOfHeapSize (); //
		 * 为了解决一些图标比正常图标位置高出的问题，将布局文件进行调整，无需系统自行选择 - by Yugi 2012.9.12 final boolean isPort =
		 * GoLauncher.getOrientation() == Configuration.ORIENTATION_PORTRAIT; final int appLayoutId
		 * = GoLauncher.isLargeIcon() ? isPort ? R.layout.application_large_port :
		 * R.layout.application_large_land : isPort ? R.layout.application_port :
		 * R.layout.application_land;
		 */
		/*
		 * try { bubble = (BubbleTextView) mInflater.inflate(appLayoutId,
		 * mGLWorkspace.getCurrentScreenView(), false); } catch (InflateException e) { //
		 * ScreenMissIconBugUtil //
		 * .showToast(ScreenMissIconBugUtil.ERROR_INFLATEBUBBLETEXT_INFLATEEXCEPTION ); }
		 * 
		 * if (bubble == null) { // ScreenMissIconBugUtil.showToast(ScreenMissIconBugUtil
		 * .ERROR_BUBBLE_NULL); return null; }
		 * 
		 * bubble.setIcon(icon); liyh 2010-11-23 12:00 增加判断是否需要显示名称 if (mDesktopSettingInfo != null)
		 * { if (mDesktopSettingInfo.isShowTitle()) { bubble.setText(title); } else {
		 * bubble.setText(null); } bubble.setShowShadow(!mDesktopSettingInfo.isTransparentBg()); }
		 * // customDeskTopBackground(bubble); bubble.setTag(tag); tag.registerObserver(bubble);
		 */

		// iconFrameLayout = (GLFrameLayout) mGlInflater.inflate(
		// R.layout.gl_screen_shortcut_icon, null);
		GLScreenShortCutIcon screenShortCutIcon = (GLScreenShortCutIcon) mGlInflater.inflate(
				R.layout.gl_screen_shortcut_icon, null);
		// screenShortCutIcon.setText(title);
		// screenShortCutIcon.setIcon(icon);
		// screenShortCutIcon.setTag(tag);
		screenShortCutIcon.setInfo(info);
		// iconFrameLayout.setTag(info);
		return screenShortCutIcon;
	}

	private GLView filterWidgetView(final ScreenAppWidgetInfo widgetInfo) {
		OutOfMemoryHandler.handle();
		final int widgetId = widgetInfo.mAppWidgetId;

		if (widgetId < 0) // 自定义的widget
		{
			return createCustomWidget(widgetInfo);
		} else // 标准widget
		{
			AppWidgetProviderInfo info = mWidgetManager.getAppWidgetInfo(widgetId);
			final AppWidgetProviderInfo originalInfo = info;
			boolean allow = true;
			if (info == null && widgetInfo.mProviderIntent != null) {
				// 如果取不到, 重新获取一个新的widgetid，再次绑定
				int newId = 0;
				try {
					// 申请新的widgetid
					newId = mWidgetHost.allocateAppWidgetId();
					widgetInfo.mAppWidgetId = newId;
					
					final ComponentName provider = widgetInfo.mProviderIntent.getComponent();
					if (provider != null) {
						Method method = AppWidgetManager.class.getMethod("bindAppWidgetIdIfAllowed", int.class,
								ComponentName.class);
						allow = (Boolean) method.invoke(mWidgetManager, newId, provider);
//						AppWidgetManagerWrapper.bindAppWidgetId(mWidgetManager, newId, provider);
					}

					info = mWidgetManager.getAppWidgetInfo(newId);
					// 更新到数据库
					mControler.updateDBItem(widgetInfo);
				} catch (RuntimeException e) {
					Log.e(VIEW_LOG_TAG, "Problem binding appWidgetId " + newId);
					if (newId > 0) {
						mWidgetHost.deleteAppWidgetId(newId);
					}
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
					if (newId > 0) {
						mWidgetHost.deleteAppWidgetId(newId);
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					if (newId > 0) {
						mWidgetHost.deleteAppWidgetId(newId);
					}
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					if (newId > 0) {
						mWidgetHost.deleteAppWidgetId(newId);
					}
				}
			}
			createHostViewAndBrocast(widgetInfo, info);
			
			/*--------------------------------------widgetview的点击恢复和加载出错的信息上传 begin---------------------------*/
			mControler.checkErrorAppWidget(widgetInfo, originalInfo, info, allow);
			GLView glWidget = new GLWidgetContainer(mContext, new GLWidgetView(mContext,
					widgetInfo.mHostView));

			glWidget.setTag(widgetInfo);
			return glWidget;
		}
	}

	public void restoreAppWidget(ScreenAppWidgetInfo screenAppWidgetInfo) {
		if (Build.VERSION.SDK_INT >= 16) {
			int appWidgetId = mWidgetHost.allocateAppWidgetId();
			boolean allow = false;
			try {
				Method method = AppWidgetManager.class.getMethod("bindAppWidgetIdIfAllowed",
						int.class, ComponentName.class);
				allow = (Boolean) method.invoke(mWidgetManager, appWidgetId,
						screenAppWidgetInfo.mProviderIntent.getComponent());
			} catch (Exception e) {
				e.printStackTrace();
			}
			mRestoreAppWidget = true;
			mRestoreScreenAppWidgetInfo = screenAppWidgetInfo;
			if (allow) {
				Intent intent = new Intent();
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IScreenFrameMsgId.CUSTOMER_PICK_WIDGET, Activity.RESULT_OK, intent, null);
			} else {
				AppWidgetProviderInfo appWidgetProviderInfo = new AppWidgetProviderInfo();
				appWidgetProviderInfo.provider = screenAppWidgetInfo.mProviderIntent.getComponent();
				SysSubWidgetInfo info = new SysSubWidgetInfo();
				info.setProviderInfo(appWidgetProviderInfo);
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IAppCoreMsgId.BIND_SYSTEM_WIDGET, appWidgetId, info, null);
			}
		} else {
			mRestoreAppWidget = true;
			mRestoreScreenAppWidgetInfo = screenAppWidgetInfo;
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IWidgetMsgId.PICK_WIDGET, 0,
					null, null);
		}
	}
	
	private void createHostViewAndBrocast(final ScreenAppWidgetInfo sawInfo,
			final AppWidgetProviderInfo awpInfo) {
		try {
			AppWidgetHostView widgetView = mWidgetHost.createView(
					ShellAdmin.sShellManager.getActivity(), sawInfo.mAppWidgetId, awpInfo);
			if (widgetView != null) {
				sawInfo.mHostView = widgetView;
				widgetView.setTag(sawInfo);

				int[] span = new int[] { sawInfo.mSpanX, sawInfo.mSpanY };
				// broadcast add widget
				ScreenUtils.appwidgetReadyBroadcast(sawInfo.mAppWidgetId, awpInfo.provider, span,
						mContext);
				GoViewCompatProxy.updateAppWidgetSize(widgetView, null,
						(int) (sawInfo.mSpanX * GLCellLayout.sCellRealWidth / DrawUtils.sDensity),
						(int) (sawInfo.mSpanY * GLCellLayout.sCellRealHeight / DrawUtils.sDensity),
						(int) (sawInfo.mSpanX * GLCellLayout.sCellRealWidth / DrawUtils.sDensity),
						(int) (sawInfo.mSpanY * GLCellLayout.sCellRealHeight / DrawUtils.sDensity));
				span = null;
			}
		} catch (Exception e) {
			Log.i(VIEW_LOG_TAG, "createHostViewAndBrocast fail, AppWidgetProviderInfo = " + awpInfo);
			// e.printStackTrace();
		}
	}

	// 创建自定义widget
	private GLView createCustomWidget(ScreenAppWidgetInfo widgetInfo) {
		if (widgetInfo == null) {
			return null;
		}

		// 搜索widget
		if (widgetInfo.mAppWidgetId == ICustomWidgetIds.SEARCH_WIDGET) {
			ValueReturned vr = new ValueReturned();
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					ICommonMsgId.RETRIEVE_SEARCH_WIDGET_VIEW, 0, vr);
			Search search = (Search) vr.mValue;
			widgetInfo.mHostView = search;
			if (search != null) {
				search.setSearchEventListener(this);
				search.setTag(widgetInfo);
				GLView glWidget = new GLWidgetContainer(mContext, new GLWidgetView(mContext,
						widgetInfo.mHostView));
				glWidget.setTag(widgetInfo);
				return glWidget;
			}
		} else {
			return createGoWidget(widgetInfo);
		}
		return null;
	}

	private GLView createGoWidget(ScreenAppWidgetInfo widgetInfo) {
		GLView goWidgetView = m3DWidgetManager.createView(widgetInfo.mAppWidgetId);
		if (goWidgetView != null) {
			if (goWidgetView instanceof GLWidgetView) {
				widgetInfo.mHostView = ((GLWidgetView) goWidgetView).getView();
			}
			goWidgetView.setTag(widgetInfo);
		}
		return goWidgetView;
	}

	/**
	 * loadShortcutAsync到UI上回调
	 * 
	 * @param item
	 */
	public void postLoadShortcut(ShortCutInfo item) {
		if (item == null) {
			return;
		}

		final GLView view = inflateIconView(item.mTitle, item.mIcon, item);
		// final View view = inflateBubbleTextView(item.mTitle, item.mIcon,
		// item);
		if (view != null) {
			mGLWorkspace.addInScreen(view, item.mScreenIndex, item.mCellX, item.mCellY,
					item.mSpanX, item.mSpanY, false);
		} else {
			// ScreenMissIconBugUtil.showToast(ScreenMissIconBugUtil.ERROR_ADDVIEW_APP_SHORTCUT_NULL);
		}
	}

	static boolean isLoading() {
		return sIsLoading;
	}

	void setLoading(boolean loading) {
		sIsLoading = loading;
	}

	public void loadFinish() {
		setLoading(false);
		mGLWorkspace.mLoadFinish = true;
		if (mHandleSdCardReflush) {
			onSdCardReflush();
			mHandleSdCardReflush = false;
		} else {
			mControler.updateAllFolder(true);
		}
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IScreenFrameMsgId.SCREEN_FINISH_LOADING,
				-1, null, null);

		// 发送广播给多屏多壁纸应用，通知当前屏幕数与当前屏幕下标
		mGLWorkspace.sendBroadcastToMultipleWallpaper(false, true);
		mGLWorkspace.markAllCellOccupied();
		m3DWidgetManager.startListening();
		if (ViewUtils.isVisibleOnTree(this)) {
			startTheme2MaskView();
		}
		// 读双１１屏幕生成图标的未读数字记录
		Double11NotificationController.updateNotification(ShellAdmin.sShellManager.getActivity());
		new PrefUpgradeHandler(ApplicationProxy.getContext()).sendPrefUpgradeMsg();
	}

	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case START_DESKTOP_LOADER : {
					// mGLWorkspace.getCurrentScreen());
					mGLWorkspace.setCurrentScreen(mCurrentScreen);
					mInitWorkspace = true;
					startDesktopLoader();

					break;
				}
				case UPDATE_EFFECT_SETTING : {
					handleEffectSettingChange(null);
					break;
				}
				case UPDATE_SCREEN_SETTING :
					handleScreenSettingChange();
					break;

				case DELETE_SCREEN : {
//					mGLWorkspace.removeScreen(msg.arg1);
					//					StatisticsData.sSCREEN_COUNT = mGLWorkspace.getChildCount();
					break;
				}
				case UPDATE_DESKTOP_SETTING : {
					handleDesktopSettingChange();
					break;
				}

				case REFRESH_UNINSTALL : {
					// 卸载后更新屏幕
					if (msg.obj != null && msg.obj instanceof Intent) {
						Intent intent = (Intent) msg.obj;
						uninstallApp(intent);
						ComponentName name = intent.getComponent();
						if (name != null && name.getPackageName().equals(PackageName.MEDIA_PLUGIN)) {
							// 多媒体插件包卸载，重启桌面
							MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
									ICommonMsgId.RESTART_GOLAUNCHER, -1, null, null);
						}
					}
					break;
				}
				
				case REFRESH_UNINSTALL_PACKAGE : {
					if (msg.obj != null && msg.obj instanceof String) {
						String packageName = (String) msg.obj;
						uninstallPackage(packageName);
					}
					break;
				}
				
				case REFRESH_CHANGE_APP : {
					if (msg.obj != null && msg.obj instanceof Intent) {
						Intent intent = (Intent) msg.obj;
						handleAppChange(intent);
					}
					break;
				}
				
				case ADD_ITEM_FROM_FOLDER_ANIMATION : {
					try {
						final GLView view = (GLView) msg.obj;
						ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
								Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
						scaleAnimation.setDuration(300);
						view.startAnimation(scaleAnimation);
					} catch (Exception e) {
					}
				}
					break;
			//				
			//				case UPDATE_FOLDER_LIST : {
			//					ArrayList<ItemInfo> folderList = (ArrayList<ItemInfo>) msg.obj;
			//					updateFolderList(folderList);
			//					break;
			//				}
				case UPDATE_ITEMS_IN_SDCARD : {
					if (msg.obj != null) {
						@SuppressWarnings("unchecked")
						ArrayList<ItemInfo> itemList = (ArrayList<ItemInfo>) msg.obj;
						updateItems(itemList);
						AppDataEngine.getInstance(ApplicationProxy.getContext()).onHandleFolderThemeIconStyleChanged();
					}
					break;
				}
				case UPDATE_ALL_FOLDER : {
					mControler.updateAllFolder(true);
					break;
				}
			}
		}
	};

	private void handleUninstallIntent(Intent intent) {
		mHandler.sendMessage(mHandler.obtainMessage(REFRESH_UNINSTALL, intent));
		final ArrayList<ItemInfo> itemInfos = mControler.unInstallApp(intent);
		if (null != itemInfos) {
			//			mHandler.sendMessage(mHandler.obtainMessage(UPDATE_FOLDER_LIST, itemInfos));
		}
	}

	private void handleUninstallApps(ArrayList<AppItemInfo> infos) {
		if (infos == null) {
			return;
		}
		int size = infos.size();

		for (int i = 0; i < size; i++) {
			AppItemInfo info = infos.get(i);
			if (null == info) {
				continue;
			}

			handleUninstallIntent(info.mIntent);
		}
	}

	private void uninstallApp(Intent intent) {
		// 清理UI
		final int screenCount = mGLWorkspace.getChildCount();
		GLCellLayout layout = null;
		for (int i = 0; i < screenCount; i++) {
			layout = (GLCellLayout) mGLWorkspace.getChildAt(i);
			if (layout == null) {
				continue;
			}

			int index = 0;
			while (index < layout.getChildCount()) {
				final GLView childView = layout.getChildAt(index);
				if (childView != null) {
					final Object tag = childView.getTag();
					if (tag != null) {
						if (tag instanceof ShortCutInfo) {
							ShortCutInfo shortCutInfo = (ShortCutInfo) tag;
							/**
							 * resolved by dengdazhong date: 2012.7.27
							 * 修复：ADT-6899 添加一键锁屏快捷方式到桌面，卸载GO锁屏时没有清除该快捷方式
							 * 移除桌面图标时除了判断intent是否一致外
							 * ，还要判断他们是否属于同一个程序，因为有可能是快捷方式，被卸载程序的快捷方式也需要移除 if
							 * (ConvertUtils.intentCompare(intent,
							 * shortCutInfo.mIntent))
							 */
							if (ConvertUtils.intentCompare(intent, shortCutInfo.mIntent)
									|| ConvertUtils.isIntentsBelongSameApp(intent,
											shortCutInfo.mIntent)) {
								layout.removeView(childView);
								if (null != mControler) {
									mControler.removeDesktopItem((ShortCutInfo) tag);
								}
								continue;
							}
						} else if (tag instanceof UserFolderInfo) {
//							final UserFolderInfo folderInfo = (UserFolderInfo) tag;
//							ArrayList<ShortCutInfo> list = folderInfo.remove(intent);
//							if (list.size() > 0) {
//								updateFolderIconAsync(folderInfo, false, true);
//							}
						} else if (tag instanceof ScreenAppWidgetInfo) {
							ComponentName component = intent.getComponent();
							if (component != null) {
								String packageName = component.getPackageName();
								if (packageName != null && !packageName.equals("")) {
									if (removeWidget(packageName, layout, childView, (ScreenAppWidgetInfo) tag)) {
										continue;
									}
								}
							}
						}
					}
				}
				++index;
			}
		}
		mControler.clearDesktopItems(intent);
	}
	
	/**
	 * 处理App的Component发生改变的事件：如Disable,Component变化
	 * @param intent
	 */
	private void handleAppChange(Intent intent) {
		// 清理UI
		final int screenCount = mGLWorkspace.getChildCount();
		GLCellLayout layout = null;
		for (int i = 0; i < screenCount; i++) {
			layout = (GLCellLayout) mGLWorkspace.getChildAt(i);
			if (layout == null) {
				continue;
			}

			int index = 0;
			while (index < layout.getChildCount()) {
				final GLView childView = layout.getChildAt(index);
				if (childView != null) {
					final Object tag = childView.getTag();
					if (tag != null) {
						if (tag instanceof ShortCutInfo) {
							ShortCutInfo shortCutInfo = (ShortCutInfo) tag;
							if (ConvertUtils.intentCompare(intent, shortCutInfo.mIntent)) {
								layout.removeView(childView);
								if (null != mControler) {
									mControler.removeDesktopItem((ShortCutInfo) tag);
								}
								continue;
							}
						}
					}
				}
				++index;
			}
		}
		mControler.clearDesktopItems(intent);
	}
	
	private void uninstallPackage(String packageName) {
		if (packageName == null || packageName.equals("")) { // 包名为空直接返回
			return;
		}
		// 清理UI
		final int screenCount = mGLWorkspace.getChildCount();
		GLCellLayout layout = null;
		for (int i = 0; i < screenCount; i++) {
			layout = (GLCellLayout) mGLWorkspace.getChildAt(i);
			if (layout == null) {
				continue;
			}

			int index = 0;
			while (index < layout.getChildCount()) {
				final GLView childView = layout.getChildAt(index);
				if (childView != null) {
					final Object tag = childView.getTag();
					if (tag != null) {
						if (tag instanceof ScreenAppWidgetInfo) {
							if (removeWidget(packageName, layout, childView,
									(ScreenAppWidgetInfo) tag)) {
								continue;
							}
						}
					}
				}
				++index;
			}
		}
	}
	
	private boolean removeWidget(String packageName, GLCellLayout layout, GLView childView,
			ScreenAppWidgetInfo widgetInfo) {
		int widgetId = widgetInfo.mAppWidgetId;
		if (m3DWidgetManager.isGoWidget(widgetId)) { // GoWidget
			GoWidgetBaseInfo curWidgetInfo = m3DWidgetManager.getWidgetInfo(widgetId);
			packageName = m3DWidgetManager.getInflatePackage(packageName);
			if (curWidgetInfo != null && curWidgetInfo.mPackage != null
					&& curWidgetInfo.mPackage.equals(packageName)) {
				layout.removeView(childView);
				if (null != mControler) {
					mControler.removeDesktopItem(widgetInfo);
				}
				m3DWidgetManager.deleteWidget(widgetId);
				return true;
			}
		} else { // 系统widget
			if (widgetInfo.mProviderIntent != null) {
				ComponentName componentName = widgetInfo.mProviderIntent.getComponent();
				if (componentName != null
						&& componentName.getPackageName().equals(packageName)) {
					layout.removeView(childView);
					if (null != mControler) {
						mControler.removeDesktopItem(widgetInfo);
					}
					mWidgetHost.deleteAppWidgetId(widgetId);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 功能简述:替换放在桌面上的下载图标为指定应用 功能详细描述: 注意:
	 * 
	 * @author chenguanyu
	 * @param packageName
	 */
	private void replaceDeskIcon(String packageName) {
		if (null == packageName) {
			return;
		}
		int screenCount = mGLWorkspace.getChildCount();
		for (int i = 0; i < screenCount; i++) {
			GLCellLayout layout = (GLCellLayout) mGLWorkspace.getChildAt(i);
			if (layout != null) {
				int index = 0;
				while (index < layout.getChildCount()) {
					GLView v = layout.getChildAt(index);
					index++;
					if (v != null) {
						ItemInfo info = (ItemInfo) v.getTag();
						boolean isAdvertIcon = checkIsAdvertIcon(info);
						if (info != null
								&& (info.mItemType == IItemType.ITEM_TYPE_SHORTCUT || isAdvertIcon)) {
							Intent tempIntent = ((ShortCutInfo) info).mIntent;
							if (tempIntent != null
									&& tempIntent.getComponent() != null
									&& tempIntent.getComponent().getPackageName() != null
									&& (tempIntent.getComponent().getPackageName()
											.equals(packageName) || (tempIntent.getComponent()
											.getPackageName()
											.equals(PackageName.RECOMMAND_GOLOCKER_PACKAGE) && packageName
											.equals(PackageName.LOCKER_PRO_PACKAGE)))) {
								ShortCutInfo newInfo = new ShortCutInfo();
								final AppDataEngine dataEngine = AppDataEngine.getInstance(ApplicationProxy.getContext());
								ArrayList<AppItemInfo> dbItemInfos = dataEngine
										.getAllAppItemInfos();
								for (int j = 0; j < dbItemInfos.size(); j++) {
									AppItemInfo dbItemInfo = dbItemInfos.get(j);
									if (null == dbItemInfo.mIntent.getComponent()) {
										continue;
									}
									String dbPackageName = dbItemInfo.mIntent.getComponent()
											.getPackageName();
									if (dbPackageName.equals(packageName)
											|| (dbPackageName
													.equals(PackageName.RECOMMAND_GOLOCKER_PACKAGE) && packageName
													.equals(PackageName.LOCKER_PRO_PACKAGE))) {
										newInfo.mIntent = dbItemInfo.mIntent;
										newInfo.mTitle = dbItemInfo.mTitle;
										newInfo.setRelativeItemInfo(dbItemInfo);
										newInfo.mIcon = dbItemInfo.mIcon;
										break;
									}
								}
								newInfo.mCellX = info.mCellX;
								newInfo.mCellY = info.mCellY;
								newInfo.mScreenIndex = info.mScreenIndex;
								newInfo.mSpanX = 1;
								newInfo.mSpanY = 1;
								newInfo.mInScreenId = System.currentTimeMillis();
								//udpate by caoyaming 2014-03-19 桌面推动态壁纸功能--防止桌面推Next动态壁纸时,AppDataEngine.addAllAppItems()方法将Next相关应用信息过滤,导致Intent为空,桌面上的推荐动态壁纸图标会显示系统默认图标. 
								if (newInfo.mIntent == null && PackageName.RECOMM_LIVEWALLPAPER_PKG_NAME.equals(packageName)) {
									PackageInfo packageInfo = AppUtils.getAppPackageInfo(mContext, packageName);
									newInfo.mIntent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
									newInfo.mIcon = packageInfo.applicationInfo.loadIcon(mContext.getPackageManager());
									newInfo.mTitle = packageInfo.applicationInfo.loadLabel(mContext.getPackageManager());
								}
								//udpate by caoyaming 2014-03-19 end
								deleteScreenItem(info, info.mScreenIndex, false);
								GLScreenShortCutIcon bubble = (GLScreenShortCutIcon) ShellAdmin.sShellManager
										.getLayoutInflater().inflate(
												R.layout.gl_screen_shortcut_icon, null);
								bubble.setInfo(newInfo);
								mGLWorkspace.addInScreen(bubble, newInfo.mScreenIndex,
										newInfo.mCellX, newInfo.mCellY, newInfo.mSpanX,
										newInfo.mSpanY, true);
								addDesktopItem(newInfo.mScreenIndex, newInfo);

							}
						}
					}
				}
			}
		}
	}

	/**
	 * <br>功能简述:检查是否15屏广告图标。
	 * <br>功能详细描述:如果是15屏幕广告图标。把图片类型由自定义改为默认类型
	 * <br>注意:
	 * @param info
	 * @return
	 */
	public boolean checkIsAdvertIcon(ItemInfo info) {
		try {
			if (info == null) {
				return false;
			}
			Intent tempIntent = ((ShortCutInfo) info).mIntent;
			if (tempIntent != null && tempIntent.getAction() != null) {
				if (tempIntent.getAction().equals(ICustomAction.ACTION_SCREEN_ADVERT)
						&& info.mItemType == IItemType.ITEM_TYPE_APPLICATION) {
					//把图片类型由自定义改为默认类型
					((ShortCutInfo) info).mFeatureIconType = ImagePreviewResultType.TYPE_DEFAULT;
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private void replaceRecommandIconInFolder(String packageName) {
		if (null == packageName) {
			return;
		}

		int screenCount = mGLWorkspace.getChildCount();
		BaseFolderIcon folderIcon = null;
		for (int i = 0; i < screenCount; i++) {
			GLCellLayout layout = (GLCellLayout) mGLWorkspace.getChildAt(i);
			if (layout == null) {
				continue;
			}
			int index = 0;
			final int childCount = layout.getChildCount();
			while (index < childCount) {
				GLView v = layout.getChildAt(index);
				index++;
				if (v == null) {
					continue;
				}

				// 查找文件夹内符合要求的推荐图标
				ItemInfo info = (ItemInfo) v.getTag();
				if (info.mItemType == IItemType.ITEM_TYPE_USER_FOLDER) {
					int count = ((UserFolderInfo) info).getChildCount();
					for (int j = 0; j < count; j++) {
						ShortCutInfo shortCutInfo = ((UserFolderInfo) info).getChildInfo(j);
						if (shortCutInfo != null && shortCutInfo.mIntent != null
								&& shortCutInfo.mIntent.getComponent() != null) {
							String pkgName = shortCutInfo.mIntent.getComponent().getPackageName();
							if (pkgName.equals(packageName)
									|| (pkgName.equals(PackageName.LOCKER_PACKAGE) && packageName
											.equals(PackageName.LOCKER_PRO_PACKAGE))) {
								checkIsAdvertIcon(shortCutInfo); //如果是15屏幕广告图标。把图片类型由自定义改为默认类型
								ShortCutInfo newInfo = shortCutInfo;
								if (v instanceof BaseFolderIcon) {
									folderIcon = (BaseFolderIcon) v;
								}
								final PackageManager pm = ShellAdmin.sShellManager.getActivity()
										.getPackageManager();
								newInfo.mIntent = pm.getLaunchIntentForPackage(packageName);
								if (newInfo.mIntent != null) {
									final ResolveInfo resolveInfo = pm.resolveActivity(newInfo.mIntent, 0);
									if (resolveInfo != null) {
										newInfo.mTitle = resolveInfo.loadLabel(pm); // 获得应用程序的Label
										newInfo.mIcon = resolveInfo.loadIcon(pm); // 获得应用程序图标
										newInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
										String intentString = ConvertUtils.intentToString(newInfo.mIntent);
										DataProvider dataProvider = DataProvider
												.getInstance(ShellAdmin.sShellManager.getActivity());
										dataProvider.updateFolderIntentAndType(info.mInScreenId,
												newInfo.mInScreenId, intentString, newInfo.mItemType,
												newInfo.mFeatureIconType);
										if (folderIcon != null) {
											folderIcon.refreshIcon();
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

//	private void initTypeface(FontBean bean) {
//		final int count = mGLWorkspace.getChildCount();
//		for (int i = 0; i < count; i++) {
//			GLCellLayout screen = (GLCellLayout) mGLWorkspace.getChildAt(i);
//			final int childCount = screen.getChildCount();
//			for (int j = 0; j < childCount; j++) {
//				GLView child = screen.getChildAt(j);
//				if (child instanceof IconView<?>) {
//					((IconView) child).setFontTypeFace(bean.mFontTypeface, bean.mFontStyle);
//				}
//			}
//		}
//	}

	private boolean handleDesktopSettingChange() {
		boolean ret = false;
		boolean reload = false;
		DesktopSettingInfo desktopSettingInfo = mControler.updateDesktopSettingInfo();
		if (desktopSettingInfo != null) {

			final int row = mGLWorkspace.getCellRow();
			final int col = mGLWorkspace.getCellCol();
			final boolean autofit = mGLWorkspace.getmAutoStretch();

			if (desktopSettingInfo.isReload()) {
				desktopSettingInfo.setReload(false);
				reload = true;
			}

			if (row != desktopSettingInfo.getRows() || col != desktopSettingInfo.getColumns()) {
				mGLWorkspace.setDesktopRowAndCol(desktopSettingInfo.getRows(),
						desktopSettingInfo.getColumns());
				reload = true;
			}
 
			if (autofit != desktopSettingInfo.getAutoFitWithMargin()) {
				mGLWorkspace.setmAutoStretch(desktopSettingInfo.getAutoFitWithMargin());
				reload = true;
			}

			if (IconUtils.getInstance().getGlowColor() != desktopSettingInfo.mPressColor) {
				IconUtils.getInstance().initGlowingOutColor();
			}

			if (reload) {
				reloadDesktop();
			} else {
				final int fontSize = desktopSettingInfo.getFontSize();
				final int fontColor = desktopSettingInfo.mTitleColor;
				final boolean showlabel = desktopSettingInfo.isShowTitle();
				final boolean showShadow = !desktopSettingInfo.isTransparentBg();
				final int count = mGLWorkspace.getChildCount();
				for (int i = 0; i < count; i++) {
					GLCellLayout screen = (GLCellLayout) mGLWorkspace.getChildAt(i);
					final int childCount = screen.getChildCount();
					for (int j = 0; j < childCount; j++) {
						GLView child = screen.getChildAt(j);
						if (child instanceof IconView<?>) {
							((IconView) child).setTitleColor(fontColor);
							//更新主题颜色
							((IconView) child).refreshScreenIconTextColor();
							((IconView) child).setFontSize(fontSize);
							((IconView) child).showTitle(showlabel, false);
							((IconView) child).setTextViewBg(showShadow);
						}
						if (child instanceof TransformListener) {
							((TransformListener) child).setAutoFit(autofit);
						}
					}
				}
			}

			ret = true;

		}

		return ret;
	}

	private void reloadGOWidget() {
		// 停止正在替换错误的widget
		stopWidgetEdit();
		m3DWidgetManager.cleanView();

		final int screenCount = mGLWorkspace.getChildCount();
		GLCellLayout layout = null;
		for (int i = 0; i < screenCount; i++) {
			layout = (GLCellLayout) mGLWorkspace.getChildAt(i);
			if (layout == null) {
				continue;
			}
			GLView[] children = new GLView[layout.getChildCount()];
			for (int j = 0; j < layout.getChildCount(); j++) {
				final GLView childView = layout.getChildAt(j);
				children[j] = childView;
			}
			for (int index = 0; index < layout.getChildCount(); index++) {
				final GLView childView = children[index];
				if (childView != null) {
					final Object tag = childView.getTag();
					if (tag != null && tag instanceof ScreenAppWidgetInfo) {
						ScreenAppWidgetInfo itemInfo = (ScreenAppWidgetInfo) tag;
						int widgetId = itemInfo.mAppWidgetId;
						if (!m3DWidgetManager.isNextWidget(widgetId)) {
							Log.i("3dwidget",
									"=========reloadGOWidget()=======item in screen is GOWidget"
											+ widgetId);
							if (m3DWidgetManager.isGoWidget(widgetId)) {
								//通知widget本身被移除
								m3DWidgetManager.removeWidget(widgetId);
							}
							//需要移除
							GLViewParent parent = childView.getGLParent();
							if (parent != null && parent instanceof GLViewGroup) {
								// 清除view
								((GLViewGroup) parent).removeViewInLayout(childView);
								// 清除纹理资源
								GLContentView.requestCleanUp(childView);
								//									post(new Runnable() {
								//
								//										@Override
								//										public void run() {
								//											targetView.cleanup();
								//										}
								//									});

								// 仅清除到BubbleTextView的注册关系
								itemInfo.clearAllObserver();

								//重新添加gowidgetview
								GLView addView = filterWidgetView((ScreenAppWidgetInfo) itemInfo);
								mGLWorkspace.addInScreen(addView, i, itemInfo.mCellX,
										itemInfo.mCellY, itemInfo.mSpanX, itemInfo.mSpanY, false);
								mGLWorkspace.postInvalidate();
								this.postInvalidate();
							}
						}
					}
				}
			}
		}
		m3DWidgetManager.startListening();
		MsgMgrProxy.sendBroadcast(this, ICommonMsgId.COMMON_WIDGET_RELOADED, -1);
		//		MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_PREVIEW_BAR, this,
		//				IScreenFrameMsgId.COMMON_WIDGET_RELOADED, -1);
	}

	private void reloadDesktop() {
		// 停止正在替换错误的widget
		stopWidgetEdit();

		AppCore.getInstance().getGoWidgetManager().cancelReplaceWidget();
		//	mThemeSpreader.cancel();
		if (!mInitWorkspace) {
			return;
		}

		if (mGLBinder != null) {
			//停止加载组件
			mGLBinder.cancel();
			mGLBinder = null;
		}

		// 记录当前屏索引
		mCurrentScreen = mGLWorkspace.getCurrentScreen();

		// 重新加载
		mControler.loadSetting();
		loadScreenInfo();
		
		this.post(new Runnable() {
			@Override
			public void run() {
				// 设置加载状态
				setLoading(true);
				// 清除已经注册到ItemInfo的所有View
				mControler.unRigistDesktopObject();
				// 清除所有视图
				clearAllDesktopView();

				initWorkspace();
				startDesktopLoader();
			}
		});
	}

	private void clearAllDesktopView() {
		GLAppFolder.getInstance().clearScreenFolderIcons();
		ViewUtils.cleanupAllChildren(mGLWorkspace);
		m3DWidgetManager.cleanView();

		// 清除缓存的GoWidget View
		//		AppCore.getInstance().getGoWidgetManager().cleanView();
	}
	
	private boolean handleScreenSettingChange() {
		boolean ret = false;
		ScreenSettingInfo screenSettingInfo = mControler.updateScreenSettingInfo();
		if (screenSettingInfo != null) {
			mGLWorkspace.setMainScreen(screenSettingInfo.mMainScreen);
			mGLWorkspace.setWallpaperScroll(screenSettingInfo.mWallpaperScroll);
			mGLWorkspace.setCycleMode(screenSettingInfo.mScreenLooping);
			mDesktopIndicator.setVisible(screenSettingInfo.mEnableIndicator);
			sShowIndicator = screenSettingInfo.mEnableIndicator;
			mDesktopIndicator.setAutoHide(screenSettingInfo.mAutoHideIndicator);
			mGLWorkspace.requestLayout(GLWorkspace.CHANGE_SOURCE_INDICATOR, 0);
			ret = true;
		}
		return ret;
	}

	private boolean handleEffectSettingChange(final EffectSettingInfo settingInfo) {
		EffectSettingInfo effectSettingInfo = settingInfo;
		if (settingInfo == null) {
			effectSettingInfo = mControler.updateEffectSettingInfo();
		}

		boolean ret = false;
		if (effectSettingInfo != null) {
			mGLWorkspace.setScrollDuration(effectSettingInfo.getDuration());
			if (effectSettingInfo.mEffectorType == IEffectorIds.EFFECTOR_TYPE_RANDOM_CUSTOM) {
				mGLWorkspace
						.setCustomRandomEffectorEffects(effectSettingInfo.mEffectCustomRandomEffects);
				mGLWorkspace.setEffector(effectSettingInfo.mEffectorType);
			} else {
				mGLWorkspace.setEffector(effectSettingInfo.mEffectorType); // 先设置效果器，限制当前使用的弹力
			}
			mGLWorkspace.setOvershootAmount(effectSettingInfo.getOvershootAmount());
			mGLWorkspace.setAutoTweakElasicity(effectSettingInfo.mAutoTweakElasticity);
			if (null != mControler.getScreenSettingInfo()) {
				sShowIndicator = mControler.getScreenSettingInfo().mEnableIndicator;
			}
			ret = true;
		}
		return ret;
	}
	
	/**
	 * 显示操作菜单
	 */
	public boolean showQuickActionMenu(GLView target) {
//		hideQuickActionMenu(false);
		if (target == null) {
			return false;
		}

		int[] xy = new int[2];
		target.getLocationInWindow(xy);
		// 放手才出来所以不用加上statusbar高度
		//		if (!StatusBarHandler.isHide()) {
		//			xy[1] += StatusBarHandler.getStatusbarHeight(StatusBarHandler.TYPE_FULLSCREEN_RETURN_HEIGHT);
		//		}
		/*
		 * if (Workspace.getLayoutScale() < 1.0f) { xy[0] *= Workspace.getLayoutScale(); xy[1] *=
		 * Workspace.getLayoutScale(); xy[0] += Workspace.sPageSpacingX / 2; xy[1] +=
		 * Workspace.sPageSpacingY / 2; }
		 */
		PopupWindowControler popupWindowControler = mShell.getPopupWindowControler();
		Rect targetRect = new Rect(xy[0], xy[1], xy[0] + target.getWidth(),
				(int) (xy[1] + target.getHeight() * 0.9));

		ItemInfo itemInfo = (ItemInfo) target.getTag();
		if (itemInfo != null) {
			int itemType = itemInfo.mItemType;

//			mClickActionMenu = new QuickActionMenu(ShellAdmin.sShellManager.getActivity(), target,
//					targetRect, mShell.getOverlayedViewGroup(), this);
			Resources res = mContext.getResources();
			switch (itemType) {
				case IItemType.ITEM_TYPE_APPLICATION : {
					ShortCutInfo cutInfo = (ShortCutInfo) itemInfo;
					// 判断是否屏幕广告图标,是就不给换图标和卸载
					if (cutInfo.mIntent != null
							&& cutInfo.mIntent.getAction() != null
							&& cutInfo.mIntent.getAction().equals(
									ICustomAction.ACTION_SCREEN_ADVERT)) {

						popupWindowControler.addQuickActionMenuItem(IQuickActionId.RENAME,
								res.getDrawable(R.drawable.gl_icon_rename),
								res.getString(R.string.renametext));
						popupWindowControler.addQuickActionMenuItem(IQuickActionId.DELETE,
								res.getDrawable(R.drawable.gl_icon_del),
								res.getString(R.string.deltext));
					} else {
						popupWindowControler.addQuickActionMenuItem(IQuickActionId.CHANGE_ICON,
								res.getDrawable(R.drawable.gl_icon_change),
								res.getString(R.string.menuitem_change_icon_dock));
						popupWindowControler.addQuickActionMenuItem(IQuickActionId.RENAME,
								res.getDrawable(R.drawable.gl_icon_rename),
								res.getString(R.string.renametext));
						popupWindowControler.addQuickActionMenuItem(IQuickActionId.DELETE,
								res.getDrawable(R.drawable.gl_icon_del),
								res.getString(R.string.deltext));
						popupWindowControler.addQuickActionMenuItem(IQuickActionId.UNINSTALL,
								res.getDrawable(R.drawable.gl_icon_uninstall),
								res.getString(R.string.uninstalltext));
					}

				}
					break;

				case IItemType.ITEM_TYPE_SHORTCUT :
					popupWindowControler.addQuickActionMenuItem(IQuickActionId.CHANGE_ICON,
							res.getDrawable(R.drawable.gl_icon_change),
							res.getString(R.string.menuitem_change_icon_dock));
					popupWindowControler.addQuickActionMenuItem(IQuickActionId.RENAME,
							res.getDrawable(R.drawable.gl_icon_rename),
							res.getString(R.string.renametext));
					popupWindowControler.addQuickActionMenuItem(IQuickActionId.DELETE,
							res.getDrawable(R.drawable.gl_icon_del),
							res.getString(R.string.deltext));
					break;

				// case IItemType.ITEM_TYPE_LIVE_FOLDER: {
				// mClickActionMenu.addItem(IQuickActionId.CHANGE_ICON, R.drawable.gl_icon_change,
				// R.string.changeicontext);
				// mClickActionMenu.addItem(IQuickActionId.RENAME, R.drawable.gl_icon_rename,
				// R.string.renametext);
				// mClickActionMenu.addItem(IQuickActionId.DELETE, R.drawable.gl_icon_del,
				// R.string.deltext);
				// }
				// break;

				case IItemType.ITEM_TYPE_USER_FOLDER : {

					popupWindowControler.addQuickActionMenuItem(IQuickActionId.CHANGE_ICON,
							res.getDrawable(R.drawable.gl_icon_change),
							res.getString(R.string.menuitem_change_icon_dock));
					popupWindowControler.addQuickActionMenuItem(IQuickActionId.RENAME_FOLDER,
							res.getDrawable(R.drawable.gl_icon_rename),
							res.getString(R.string.renametext));
					popupWindowControler.addQuickActionMenuItem(IQuickActionId.EDIT,
							res.getDrawable(R.drawable.gl_icon_add),
							res.getString(R.string.tab_add_main));
					popupWindowControler.addQuickActionMenuItem(IQuickActionId.DELETE,
							res.getDrawable(R.drawable.gl_icon_del),
							res.getString(R.string.deltext));
				}
					break;

				case IItemType.ITEM_TYPE_APP_WIDGET : {
					// add by dengdazhong 2013-05-22 for ADT-12587 3D桌面－屏幕：桌面卸载的小部件还存在缩放、主题等菜单，请修改
					boolean isErrorOrUpdateWidget = (target instanceof GLWidgetContainer)
							&& ((((GLWidgetContainer) target).getWidget() instanceof GLWidgetErrorView) || (((GLWidgetContainer) target)
									.getWidget() instanceof GLWidgetUpdateView));
					// add by dengdazhong 2013-05-22 end
					// 不是ErrorWidget才有其他选项
					if (!isErrorOrUpdateWidget) {
						popupWindowControler.addQuickActionMenuItem(IQuickActionId.RESIZE,
								res.getDrawable(R.drawable.gl_icon_zoom),
								res.getString(R.string.zoomtext));

						ScreenAppWidgetInfo widgetInfo = (ScreenAppWidgetInfo) itemInfo;
						if (GoWidgetManager.isGoWidget(widgetInfo.mAppWidgetId)) {
							GoWidgetBaseInfo baseInfo = AppCore.getInstance().getGoWidgetManager()
									.getWidgetInfo(widgetInfo.mAppWidgetId);
							if (baseInfo != null && baseInfo.mPackage != null
									&& baseInfo.mClassName != null
									&& baseInfo.mClassName.length() > 0) {
								popupWindowControler.addQuickActionMenuItem(IQuickActionId.CONFIG,
										res.getDrawable(R.drawable.gl_config),
										res.getString(R.string.configtext));
							}
							// 3Dwidget不支持主题，所以屏蔽掉主题选项
							// update by zhoujun 应用游戏中心的widget 不需要换肤操作 (goStore也暂时不换)
							if (baseInfo == null
									|| (baseInfo.mPrototype != GoWidgetBaseInfo.PROTOTYPE_APPGAME
									&& baseInfo.mPrototype != GoWidgetBaseInfo.PROTOTYPE_GOSTORE
									&& !baseInfo.mPackage.equals(PackageName.CLEAN_MASTER_PACKAGE) 
									&& !baseInfo.mPackage.equals(PackageName.NEXT_BROWSER_PACKAGE_NAME) 
									&& baseInfo.mPrototype != GoWidgetBaseInfo.PROTOTYPE_GOSWITCH)) {
								// GOwidget换肤入口
								popupWindowControler.addQuickActionMenuItem(IQuickActionId.THEME,
										res.getDrawable(R.drawable.gl_skin),
										res.getString(R.string.skintext));
							}
							// update by zhoujun 2012-08-16 end
						}
					}
					popupWindowControler.addQuickActionMenuItem(IQuickActionId.DELETE,
							res.getDrawable(R.drawable.gl_icon_del),
							res.getString(R.string.deltext));
				}
					break;
				case IItemType.ITEM_TYPE_FAVORITE : {
					popupWindowControler.addQuickActionMenuItem(IQuickActionId.DELETE,
							res.getDrawable(R.drawable.gl_icon_del),
							res.getString(R.string.deltext));
				}
					break;

				default :
					break;
			}
			popupWindowControler.showQuickActionMenu(targetRect, target, target, this, this);
		}
		return true;
	}

	/**
	 * 取消弹出菜单
	 * 
	 * @param dismissWithCallback
	 *            ， 是否回调， true仅取消菜单显示，false会回调到
	 *            {@link QuickActionMenu.onActionListener#onActionClick(int, View)} 并传回一个
	 *            {@link IQuickActionId#CANCEL}事件
	 */
	protected void hidePopupWindow(boolean dismissWithCallback) {
		PopupWindowControler popupWindowControler = mShell.getPopupWindowControler();
		if (popupWindowControler.isShowing()) {
			if (dismissWithCallback) {
				popupWindowControler.cancel(true);
			} else {
				popupWindowControler.dismiss(true);
			}
		}
	}

	/**
	 * <br>功能简述:扫描需要删除
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param objects
	 * @return
	 */
	public boolean scanScreenCleanDataList(List objects) {
		List sourceList = new ArrayList<Object>();
		int screenCount = mGLWorkspace.getChildCount();
		for (int i = 0; i < screenCount; i++) {
			GLCellLayout cellLayout = mGLWorkspace.getScreenView(i);
			int screenViewSize = cellLayout.getChildCount();
			//遍历对应的屏幕控件
			for (int j = 0; j < screenViewSize; j++) {
				Object object = cellLayout.getChildAt(j).getTag();
				sourceList.add(object);
			}
		}
		AppManagerUtils.scanCleanList(ShellAdmin.sShellManager.getActivity(), sourceList, objects);
		return true;
	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param delListObject
	 * @return
	 */
	public boolean scanDelScanIcon(Object delListObject) {
		try {
			if (delListObject instanceof ArrayList) {
				ArrayList<CleanScreenInfo> delIconList = (ArrayList<CleanScreenInfo>) delListObject;
				if (delIconList != null) {
					int size = delIconList.size();
					for (int i = 0; i < size; i++) {

						try {
							CleanScreenInfo cleanScreenInfo = delIconList.get(i);
							ItemInfo info = cleanScreenInfo.mItemInfo;
							//判断是否文件夹里面的图标
							if (cleanScreenInfo.mType == AppManagerUtils.TYPE_FOLDER_SHORTCUT) {
								ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
								items.add(info);
								mControler.removeUserFolderContent(cleanScreenInfo.mFolderId,
										items, false);
								UserFolderInfo folderInfo = mControler
										.getFolderItemInfo(cleanScreenInfo.mFolderId);
								folderInfo.clear();
								// 由于一些绑定的原因，这里直接重新设置初始化标志
								folderInfo.mContentsInit = false;
								folderInfo.mIsFirstCreate = true;
								updateFolderIconAsync(folderInfo, true, true);
								ArrayList<ItemInfo> folderItems = getFolderContentFromDB(folderInfo);
								GLView view = ScreenUtils.getViewByItemId(folderInfo.mInScreenId,
										folderInfo.mScreenIndex, mGLWorkspace);
								if (view instanceof GLScreenFolderIcon) {
									if (folderItems.size() >= 1) {
										GLScreenFolderIcon icon = (GLScreenFolderIcon) view;
										icon.refreshIcon();
									} else {
										mControler.removeUserFolder(folderInfo);
										GLCellLayout cell = mGLWorkspace
												.getScreenView(folderInfo.mScreenIndex);
										if (cell != null) {
											cell.removeView(view);
										}
									}

								}
							} else {
								deleteScreenItem(info, info.mScreenIndex, false);
								//								deleteItem(info, info.mScreenIndex);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	// 职责同 screenrame 的 onDestroy
	public void clear() {

	}

	@Override
	public void setVisible(final boolean visible, boolean animate, Object obj) {
		//		setVisible(visible);
//		if (isVisible() == visible) {
//			return;
//		}
		
		if (mIsInOutAnimating) {
			if ((visible && mVisibleState  == GLView.INVISIBLE)
					|| (!visible && mVisibleState == GLView.VISIBLE)) {
				clearAnimation();
			}
		}
		mVisibleState = visible ? GLView.VISIBLE : GLView.INVISIBLE;
		if (animate) {
			mIsInOutAnimating = true;
			AnimationSet animationSet = null;
			if (visible) {
				animationSet = AnimationFactory.getPopupAnimation(AnimationFactory.SHOW_ANIMATION,
						AnimationFactory.DEFAULT_DURATION, getHeight(), false);
				//				setVisible(visible);
			} else {
				animationSet = AnimationFactory.getPopupAnimation(AnimationFactory.HIDE_ANIMATION,
						AnimationFactory.DEFAULT_DURATION, getHeight(), false);
			}
			//			animation.setDuration(AnimationFactory.DEFAULT_DURATION);
			AnimationTask task = new AnimationTask(this, animationSet, new AnimationListenerAdapter() {

				@Override
				public void onAnimationEnd(Animation animation) {
					mIsInOutAnimating = false;
					post(new Runnable() {

						@Override
						public void run() {
							setVisible(visible);
						}
					});
				}
			}, true, AnimationTask.PARALLEL);
			GLAnimationManager.startAnimation(task);
		} else {
			mIsInOutAnimating = false;
			clearAnimation();
			setVisible(visible);
		}
		if (visible) {
			mGLWorkspace.unblurBackground();
		} else {
			mGLWorkspace.blurBackground();
		}
	}

	@Override
	public void setShell(IShell shell) {
		mShell = shell;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean ret = false;
		if (mExtendFuncView != null) {
			ret = mExtendFuncView.onKeyDown(keyCode, event);
		} else {
			switch (keyCode) {
				case KeyEvent.KEYCODE_MENU :
					if (event.isLongPress()) {
						mNeedOpenGGMenu = false;
					} else if (!mIsKeyDown) {
						//判断是否在0屏，在0屏就屏蔽菜单按钮
						if (!mGLSuperWorkspace.isInZeroScreen()) {
							mNeedOpenGGMenu = true;
						}
					}
					break;

				case KeyEvent.KEYCODE_BACK :
					if (mIsWidgetEditMode) {
						stopWidgetEdit();
					}/* else if (mGLWorkspace != null) {
						if (!mGLWorkspace.mExitToLevelFlag) {
							mGLWorkspace.exitToLevel(GLWorkspace.SCREEN_BACK_TO_LAST_LEVEL);
							leaveNewFolderState();
						}
						}*/
					ret = true;
					break;

				default :
					break;
			}
		}
		return ret || super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (mExtendFuncView != null) {
			return mExtendFuncView.onKeyUp(keyCode, event);
		} else {
			mIsKeyDown = false;
			if (keyCode == KeyEvent.KEYCODE_MENU) {
				pressMenuKey(0 != (event.getFlags() & KeyEvent.FLAG_VIRTUAL_HARD_KEY));
				return true;
			} else {
				//判断是否在0屏，返回按钮
				if (keyCode == KeyEvent.KEYCODE_BACK && mGLSuperWorkspace.isInZeroScreen()) {
					mGLSuperWorkspace.onKeyBack(); //0屏监听返回按钮
					return true;
				} else {
					return super.onKeyUp(keyCode, event);
				}
			}
		}
	}

	private boolean pressMenuKey(boolean vibrate) {
//		if (BetaController.getInstance().needToShowBetaDialog()) {
//			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
//				
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					if (mNeedOpenGGMenu) {
//						mNeedOpenGGMenu = false;
//						if (mGLWorkspace.mScaleState == GLWorkspace.STATE_NORMAL
//								&& !mShell.isViewVisible(IViewId.SCREEN_PREVIEW)) {
//							showGGMenu(true);
//						}
//					}
//				}
//			};
//			BetaController.getInstance().showBetaDialog(listener);
//			
//		} else {
			if (mNeedOpenGGMenu) {
				mNeedOpenGGMenu = false;
				if (mGLWorkspace.mScaleState == GLWorkspace.STATE_NORMAL
						&& !mShell.isViewVisible(IViewId.SCREEN_PREVIEW) && mGLWorkspace.getScreenScroller().isFinished()) {
					showGGMenu(true);
				}
			}
//		}

		return true;
	}

	@Override
	public void onActionClick(int action, Object target) {
		ItemInfo targetInfo = null;
		if (target != null && target instanceof ItemInfo) {
			targetInfo = (ItemInfo) target;
		} else if (target == null || !(target instanceof GLView)) {
			return;
		}

		targetInfo = targetInfo == null ? (ItemInfo) ((GLView) target).getTag() : targetInfo;
		if (targetInfo == null) {
			return;
		}
		mDraggedItemId = targetInfo.mInScreenId;
		switch (action) {
			case IQuickActionId.CANCEL : {
				// MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
				// IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.DRAG_FRAME, null, null);
				break;
			}

			case IQuickActionId.CHANGE_ICON : {
				BitmapDrawable iconDrawable = null;
				String defaultNameString = "";
				if (mDraggedItemId >= 0) {
					GLView editingView = ScreenUtils.getViewByItemId(mDraggedItemId,
							mGLWorkspace.getCurrentScreen(), mGLWorkspace);
					if (editingView != null) {
						if (editingView.getTag() instanceof RelativeItemInfo) {
							RelativeItemInfo tagInfo = (RelativeItemInfo) editingView.getTag();

							if (tagInfo instanceof ShortCutInfo) {
								if (null != tagInfo.getRelativeItemInfo()) {
									iconDrawable = tagInfo.getRelativeItemInfo().getIcon();
									if (tagInfo.getRelativeItemInfo().mTitle != null) {
										defaultNameString = tagInfo.getRelativeItemInfo().mTitle
												.toString();
									}
								}
							} else if (editingView instanceof GLScreenFolderIcon) {
								GLScreenFolderIcon folderIcon = (GLScreenFolderIcon) editingView;
								iconDrawable = (BitmapDrawable) CommonImageManager.getInstance()
										.getDrawable(CommonImageManager.RES_FOLDER_BG);
								if (folderIcon.getInfo().mTitle != null) {
									defaultNameString = folderIcon.getInfo().mTitle.toString();
								}
							}
						}
					}
					if (target instanceof GLScreenFolderIcon) {
						ChangeIconPreviewActivity.sFromWhatRequester = ChangeIconPreviewActivity.USER_FOLDER_STYLE;
					}// 文件夹 } 
					else {
						ChangeIconPreviewActivity.sFromWhatRequester = ChangeIconPreviewActivity.SCREEN_STYLE; // 图标
					}
					Bundle bundle = new Bundle();
					
					if (iconDrawable != null) {
						Bitmap defaultBmp = iconDrawable.getBitmap();
						if (null != defaultBmp) {
							ByteArrayOutputStream os = new ByteArrayOutputStream();
							defaultBmp.compress(Bitmap.CompressFormat.PNG, 100, os);
							if (os.toByteArray().length > MAX_BITMAP_SIZE) {
								defaultBmp = ThumbnailManager.getInstance(
										ShellAdmin.sShellManager.getActivity()).getImageThumbnail(
										defaultBmp, COMPRESS_BITMAP_WIDTH);
							}
							bundle.putParcelable(ChangeIconPreviewActivity.DEFAULT_ICON_BITMAP,
									defaultBmp);
						}
					}
					bundle.putString(ChangeIconPreviewActivity.DEFAULT_NAME, defaultNameString);
					try {
						Intent intent = new Intent(ShellAdmin.sShellManager.getActivity(),
								ChangeIconPreviewActivity.class);
						intent.putExtras(bundle);
						mShell.startActivityForResultSafely(intent,
								IRequestCodeIds.REQUEST_THEME_FORICON);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
			case IQuickActionId.RENAME_FOLDER : {
				startReNameActivity(action, targetInfo);
				GuiThemeStatistics
						.goLauncherUserBehaviorStaticDataCache(IGoLauncherUserBehaviorStatic.FOLDER_PRESS_06);
				break;
			}
			case IQuickActionId.RENAME : {
				startReNameActivity(action, targetInfo);
				GuiThemeStatistics
						.goLauncherUserBehaviorStaticDataCache(IGoLauncherUserBehaviorStatic.ICON_PRESS_02);
				break;
			}
			case IQuickActionId.DELETE : {
				if (null != targetInfo) {
					actionDelete(targetInfo);

				}
				break;
			}
			case IQuickActionId.UNINSTALL : {
				actionUninstall((GLView) target);
				break;
			}
			case IQuickActionId.RESIZE : {
				editWdiget((GLView) target);
				break;
			}

			case IQuickActionId.THEME : {
				actionChangeWidgetSkin((ScreenAppWidgetInfo) targetInfo);
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_LONG_CLICK_WIDGET,
						StatisticsData.USER_ACTION_FOUR, IPreferencesIds.DESK_ACTION_DATA);
			}
				break;

			case IQuickActionId.CONFIG : {
				actionConfig(targetInfo);
				break;
			}
			case IQuickActionId.EDIT : {
				actionEditFolder(targetInfo);
				break;
			}
		}
	}

	private void startReNameActivity(int action, ItemInfo targetInfo) {
		Intent intent = new Intent(ShellAdmin.sShellManager.getActivity(),
				RenameActivity.class);
		CharSequence title = ScreenUtils.getItemTitle(targetInfo);
		intent.putExtra(RenameActivity.NAME, title);
		intent.putExtra(RenameActivity.HANDLERID, IDiyFrameIds.SCREEN);
		intent.putExtra(RenameActivity.ITEMID, targetInfo.mInScreenId);
		if (action == IQuickActionId.RENAME_FOLDER) {
			intent.putExtra(RenameActivity.SHOW_RECOMMENDEDNAME, true);
			intent.putExtra(RenameActivity.FINISH_WHEN_CHANGE_ORIENTATION, true);
		}
		ShellAdmin.sShellManager.getActivity().startActivityForResult(intent,
				IRequestCodeIds.REQUEST_RENAME);
	}

	private void actionEditFolder(ItemInfo targetInfo) {
		if (targetInfo instanceof UserFolderInfo) {
			Intent newFolderIntent = new Intent(ApplicationProxy.getContext(),
					GLScreenFolderModifyActivity.class);
			Bundle bundle = new Bundle();
			bundle.putLong(GLScreenFolderModifyActivity.FOLDER_ID, targetInfo.mInScreenId);
			newFolderIntent.putExtras(bundle);
			ShellAdmin.sShellManager.getShell().startActivityForResultSafely(newFolderIntent,
					IRequestCodeIds.REQUEST_MODIFY_FOLDER);
		}
	}

	private void actionChangeWidgetSkin(ScreenAppWidgetInfo widgetInfo) {
		if (widgetInfo != null && GoWidgetManager.isGoWidget(widgetInfo.mAppWidgetId)) {
			GoWidgetBaseInfo info = AppCore.getInstance().getGoWidgetManager()
					.getWidgetInfo(widgetInfo.mAppWidgetId);

			if (info != null) {
				//update by wangzhuobin 2014-04-18 V5.0版本产品需求(GO天气内置widget菜单跳转):点击主题打开GooglePlay天气详情页面
				if (info.mPrototype == GoWidgetBaseInfo.PROTOTYPE_GOWEATHER
						&& !AppUtils
								.isAppExist(mContext, PackageName.RECOMMAND_GOWEATHEREX_PACKAGE)) {
					GoAppUtils.gotoBrowserIfFailtoMarket(getApplicationContext(),
							LauncherEnv.GO_WEATHER_EX_GA, LauncherEnv.Url.GO_WEATHER_EX_URL);
					//长按菜单->主题点击
					GLGoWeatherStatisticsUtil.uploadOperationStatisticData(getApplicationContext(),
							GLGoWeatherStatisticsUtil.ACTION_MENU_THEME);
					return;
				}
				//update by wangzhuobin 2014-04-18 end
				
				//update by caoyaming 2014-03-18 V4.16版本产品需求(GO天气widget菜单跳转优化):点击主题打开Go天气主题设置页面.
				if (actionChangeWidgetSkinFromWeatherEx(info.mPackage)) {
					//跳转成功,直接返回.
					return;
				}
				//其它Widget或启动天气主题设置页面异常.
				//update by caoyaming 2014-03-18 end
				
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.WIDGET_THEME_CHOOSE, null, null);

				MsgMgrProxy.sendMessage(this, IDiyFrameIds.WIDGET_THEME_CHOOSE,
						IWidgetMsgId.WIDGETCHOOSE_SKIN, -1, info, null);
			}
		}
	}
	/**
	 * 如果是点击了天气Widget菜单中的主题,则跳转到对应的页面.
	 * @param info
	 * @return 是否跳转成功 true:跳转成功  false:跳转失败
	 */
	private boolean actionChangeWidgetSkinFromWeatherEx(String packageName) {
		if (PackageName.RECOMMAND_GOWEATHEREX_PACKAGE.equals(packageName)) {
			//Go天气Widget
			try {
				//启动天气主题设置页面
				Intent intent = new Intent();
			    intent.setClassName(packageName, PackageName.GO_WEATHEREX_THEME_SETTING_ACTIVITY);
			    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		        //Tab类型:0为热门、1为精选、2为本地
		        intent.putExtra("extra_theme_tab", 1);
		        //主题类型:0为系统Widget、1为背景、2为GOWidget
		        intent.putExtra("extra_theme_type", 2);
		        //进入主题界面的入口统计,固定为6
		        intent.putExtra("extra_theme_entrance", 6);
		        mContext.startActivity(intent);
			    return true;
			} catch (Exception e) {
				//启动异常,使用默认处理方式.
				Log.e(GLScreen.class.getName(), "start weather themesetting activity error!", e);
			}
		}
		return false;
	}
	private synchronized void actionConfig(ItemInfo itemInfo) {
		ScreenAppWidgetInfo info = (ScreenAppWidgetInfo) itemInfo;
		if (info != null) {
			final GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
			GoWidgetBaseInfo baseInfo = widgetManager.getWidgetInfo(info.mAppWidgetId);
			try {
				Intent intent = new Intent();
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				final ComponentName cn = widgetManager.getConfigComponent(baseInfo);
				if (cn != null) {
					intent.setComponent(cn);
				}

				Bundle bundle = new Bundle();
				bundle.putBoolean(GoWidgetConstant.GOWIDGET_SETTING_ENTRY, true);
				bundle.putInt(GoWidgetConstant.GOWIDGET_ID, info.mAppWidgetId);

				// 传递而外的信息，方便设置activity更换主题
				bundle.putString(GoWidgetConstant.GOWIDGET_THEME, baseInfo.mTheme);
				bundle.putInt(GoWidgetConstant.GOWIDGET_THEMEID, baseInfo.mThemeId);
				bundle.putInt(GoWidgetConstant.GOWIDGET_TYPE, baseInfo.mType);
				intent.putExtras(bundle);
				ShellAdmin.sShellManager.getActivity().startActivity(intent);
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_LONG_CLICK_WIDGET,
						StatisticsData.USER_ACTION_THREE, IPreferencesIds.DESK_ACTION_DATA);
			} catch (Exception e) {
				Log.i(VIEW_LOG_TAG, "start gowidget config error, widgetid = " + info.mAppWidgetId);
			}
		}
	}

	private synchronized void actionDelete(ItemInfo itemInfo) {
		if (null == itemInfo) {
			return;
		}
		/*
		 * // 从文件夹拖出 // if(mDragFromFolder) if (DragFrame.TYPE_SCREEN_FOLDER_DRAG == mDragType) { if
		 * (mCurrentFolderInfo != null) { if (null != mControler) { // final int screenIndex =
		 * mGLWorkspace.getCurrentScreen(); // mControler.moveDesktopItemFromFolder(itemInfo, //
		 * screenIndex, // mCurrentFolderInfo.mInScreenId); mControler
		 * .removeItemFromFolder(itemInfo, mCurrentFolderInfo.mInScreenId, false); } // 更新缓存 if
		 * (mCurrentFolderInfo instanceof UserFolderInfo) { itemInfo.selfDestruct();
		 * ((UserFolderInfo) mCurrentFolderInfo).remove(itemInfo.mInScreenId); if
		 * (!deleteFolderOrNot((UserFolderInfo) mCurrentFolderInfo, true)) { // if (itemInfo
		 * instanceof ShortCutInfo) { // int type = //
		 * AppIdentifier.whichTypeOfNotification(mActivity, // (( ShortCutInfo ) itemInfo).mIntent);
		 * // if (type != NotificationType.IS_NOT_NOTIFICSTION) { // (( UserFolderInfo ) //
		 * mCurrentFolderInfo).mTotleUnreadCount -= (( // ShortCutInfo ) itemInfo).mCounter; // } //
		 * } // 更新图标 updateFolderIconAsync((UserFolderInfo) mCurrentFolderInfo, false, false); } } }
		 * 
		 * // mDragFromFolder = false; mDragType = DragFrame.TYPE_SCREEN_ITEM_DRAG;
		 * mCurrentFolderInfo = null; } else
		 */
		{
			// 删除view
			mControler.removeDesktopItem(itemInfo);
			final GLView targetView = ScreenUtils.getViewByItemId(itemInfo.mInScreenId,
					itemInfo.mScreenIndex, mGLWorkspace);
			if (targetView != null) {
				GLViewParent parent = targetView.getGLParent();
				if (parent != null && parent instanceof GLViewGroup) {
					// Log.i("jiang", "2222:"+mGLWorkspace.getLayoutParams());
					((GLViewGroup) parent).removeView(targetView);
					Object obj = targetView.getTag();
					if (obj != null && obj instanceof ScreenAppWidgetInfo) {
						ScreenAppWidgetInfo widgetInfo = (ScreenAppWidgetInfo) obj;
						m3DWidgetManager.removeWidget(widgetInfo.mAppWidgetId);
						m3DWidgetManager.deleteWidget(widgetInfo.mAppWidgetId);
					}
					post(new Runnable() {

						@Override
						public void run() {
							targetView.cleanup();

						}

					});
					// Log.i("jiang", ":"+mGLWorkspace.getLayoutParams());
					// mGLWorkspace.setCurrentScreen(2);
					// requestLayout();
					// postInvalidate();
				}
			}

			int type = itemInfo.mItemType;
			if (type == IItemType.ITEM_TYPE_SHORTCUT || type == IItemType.ITEM_TYPE_APP_WIDGET) {
				if (type == IItemType.ITEM_TYPE_SHORTCUT) {
					// ScreenUtils.unbindShortcut((ShortCutInfo) itemInfo);
				} /*
					* else if (type == IItemType.ITEM_TYPE_LIVE_FOLDER) {
					* ScreenUtils.unbindLiveFolder((ScreenLiveFolderInfo) itemInfo); }
					*/

				/*
				 * if (itemInfo instanceof ScreenAppWidgetInfo) { int widgetId =
				 * ((ScreenAppWidgetInfo) itemInfo).mAppWidgetId; if
				 * (GoWidgetManager.isGoWidget(widgetId)) {
				 * AppCore.getInstance().getGoWidgetManager().deleteWidget(widgetId); } else //
				 * 系统widget { mWidgetHost.deleteAppWidgetId(widgetId); } }
				 */
			} else if (type == IItemType.ITEM_TYPE_USER_FOLDER) { // 删除文件夹
				ScreenUtils.unbindeUserFolder((UserFolderInfo) itemInfo);
				mControler.removeUserFolder(itemInfo);
			}
		}
	}

	private void resetDefaultIcon() {
		if (mDraggedItemId >= 0) {
			GLView editingView = ScreenUtils.getViewByItemId(mDraggedItemId,
					mGLWorkspace.getCurrentScreen(), mGLWorkspace);
			mDraggedItemId = -1;
			if (editingView != null) {
				if (editingView.getTag() instanceof RelativeItemInfo) {
					RelativeItemInfo tagInfo = (RelativeItemInfo) editingView.getTag();
					BitmapDrawable iconDrawable = null;
					if (null != tagInfo.getRelativeItemInfo()) {
						iconDrawable = tagInfo.getRelativeItemInfo().getIcon();
					}
					if (tagInfo instanceof FeatureItemInfo) {
						((FeatureItemInfo) tagInfo).resetFeature();
					}

					updateDesktopItem(tagInfo.mScreenIndex, tagInfo);
					if (null != iconDrawable) {
						((IconView<?>) editingView).setIcon((BitmapDrawable) iconDrawable);
						setItemIcon(editingView, iconDrawable, false);
						if (editingView instanceof GLScreenFolderIcon) {
							GLScreenFolderIcon folderIcon = (GLScreenFolderIcon) editingView;
							folderIcon.resetResource();
						}
					}
				}
			}
		}
	}

	void setItemIcon(GLView targetView, Drawable icon, boolean isUserIcon) {
		if (targetView != null) {
			ItemInfo targetInfo = (ItemInfo) targetView.getTag();
			if (targetInfo != null) {
				if (targetInfo instanceof ShortCutInfo) {
					((ShortCutInfo) targetInfo).mIcon = icon;
					((ShortCutInfo) targetInfo).mIsUserIcon = isUserIcon;
				} else if (targetInfo instanceof UserFolderInfo) {
					((UserFolderInfo) targetInfo).mIcon = icon;
					((UserFolderInfo) targetInfo).mIsUserIcon = isUserIcon;
					updateFolderIconAsync((UserFolderInfo) targetInfo, false, false);
				}
			}
		}
	}

	private void actionChangeIcon(Bundle iconBundle) {
		GLView editingView = null;
		Drawable iconDrawable = null;
		if (mDraggedItemId >= 0) {
			editingView = ScreenUtils.getViewByItemId(mDraggedItemId,
					mGLWorkspace.getCurrentScreen(), mGLWorkspace);

			if ((editingView != null) /* && (editingView instanceof BubbleTextView) */
					&& (iconBundle != null)) {
				ItemInfo tagInfo = (ItemInfo) editingView.getTag();
				if (tagInfo == null) {
					// Log.i(LOG_TAG, "change icon fail tagInfo == null");
					return;
				}

				boolean isDefaultIcon = false;
				int type = iconBundle.getInt(ImagePreviewResultType.TYPE_STRING);
				if (ImagePreviewResultType.TYPE_RESOURCE_ID == type) {
					int id = iconBundle.getInt(ImagePreviewResultType.IMAGE_ID_STRING);
					/*
					 * iconDrawable = mActivity.getResources().getDrawable(id);
					 * 
					 * if (tagInfo instanceof FeatureItemInfo) { ((FeatureItemInfo)
					 * tagInfo).setFeatureIcon(iconDrawable, type, null, id, null);
					 * updateDesktopItem(tagInfo.mScreenIndex, tagInfo); }
					 */
				} else if (ImagePreviewResultType.TYPE_IMAGE_FILE == type) {
					if (tagInfo instanceof FeatureItemInfo) {
						String path = iconBundle
								.getString(ImagePreviewResultType.IMAGE_PATH_STRING);
						((FeatureItemInfo) tagInfo).setFeatureIcon(null, type, null, 0, path);
						if (((FeatureItemInfo) tagInfo).prepareFeatureIcon()) {
							iconDrawable = ((FeatureItemInfo) tagInfo).getFeatureIcon();
							updateDesktopItem(tagInfo.mScreenIndex, tagInfo);
						}
					}
				}

				else if (ImagePreviewResultType.TYPE_IMAGE_URI == type) {
					if (tagInfo instanceof FeatureItemInfo) {
						String path = iconBundle
								.getString(ImagePreviewResultType.IMAGE_PATH_STRING);
						((FeatureItemInfo) tagInfo).setFeatureIcon(null, type, null, 0, path);
						if (((FeatureItemInfo) tagInfo).prepareFeatureIcon()) {
							iconDrawable = ((FeatureItemInfo) tagInfo).getFeatureIcon();
							updateDesktopItem(tagInfo.mScreenIndex, tagInfo);
						}
					}
				} else if (ImagePreviewResultType.TYPE_PACKAGE_RESOURCE == type
						|| ImagePreviewResultType.TYPE_APP_ICON == type) {
					String packageStr = iconBundle
							.getString(ImagePreviewResultType.IMAGE_PACKAGE_NAME);
					String path = iconBundle.getString(ImagePreviewResultType.IMAGE_PATH_STRING);
					ImageExplorer imageExplorer = ImageExplorer
							.getInstance(ShellAdmin.sShellManager.getActivity());
					iconDrawable = imageExplorer.getDrawable(packageStr, path);
					if (null != iconDrawable) {
						if (iconDrawable instanceof NinePatchDrawable) {
							// Toast.makeText(mActivity,
							// R.string.folder_change_ninepatchdrawable_toast,
							// Toast.LENGTH_LONG).show();
							return;
						}
						if (tagInfo instanceof FeatureItemInfo) {
							((FeatureItemInfo) tagInfo).setFeatureIcon(iconDrawable, type,
									packageStr, 0, path);
							updateDesktopItem(tagInfo.mScreenIndex, tagInfo);
						}
					}
				} /*
					* else { BitmapDrawable bmp = null; if (tagInfo instanceof RelativeItemInfo) { bmp
					* = ((RelativeItemInfo) tagInfo).getRelativeItemInfo().getIcon(); }
					* 
					* if (null != bmp) {
					* bmp.setTargetDensity(mActivity.getResources().getDisplayMetrics()); iconDrawable
					* = bmp; isDefaultIcon = true; }
					* 
					* if (tagInfo instanceof FeatureItemInfo) { ((FeatureItemInfo)
					* tagInfo).resetFeature(); updateDesktopItem(tagInfo.mScreenIndex, tagInfo); } }
					* 
					* if (iconDrawable == null) { Toast.makeText(mActivity, R.string.save_image_error,
					* Toast.LENGTH_LONG).show(); return; }
					* 
					* ((BubbleTextView) editingView).setIcon(iconDrawable); setItemIcon(editingView,
					* iconDrawable, !isDefaultIcon);
					*/
			}
		}
		if (editingView != null) {
			((IconView<?>) editingView).setIcon((BitmapDrawable) iconDrawable);
			if (editingView instanceof GLScreenFolderIcon) {
				((GLScreenFolderIcon) editingView).useFolderFeatureIcon();
			}
			setItemIcon(editingView, iconDrawable, true);
		}
		mGLWorkspace.requestLayout();
	}
	
	private void actionUninstall(GLView editView) {
		if (editView == null) {
			return;
		}

		// if ((editView instanceof BubbleTextView) || (editView instanceof TextView)) {
		if (editView.getTag() instanceof ShortCutInfo) {
			ShortCutInfo shortCutInfo = (ShortCutInfo) editView.getTag();
			mControler.uninstallApplication(shortCutInfo.mIntent);
		}
		// }
	}

	private void actionRename(String newName, long inscreenid) {
		// 重命名
		GLView editingView = ScreenUtils.getViewByItemId(inscreenid,
				mGLWorkspace.getCurrentScreen(), mGLWorkspace);

		if (editingView == null /* || !(editingView instanceof BubbleTextView) */) {
			return;
		}

		Object tag = ((GLView) editingView).getTag();
		if (null != tag && tag instanceof ItemInfo) {
			if (tag instanceof FeatureItemInfo) {
				((FeatureItemInfo) tag).setFeatureTitle(newName);
			}
			if (null != mControler) {
				ItemInfo info = (ItemInfo) tag;
				mControler.updateDesktopItem(info.mScreenIndex, info);
			}

			if (tag instanceof ShortCutInfo) {
				((ShortCutInfo) tag).setTitle(newName, true);
			} else if (tag instanceof ScreenFolderInfo) {
				((ScreenFolderInfo) tag).mTitle = newName;
			}
			((IconView) editingView).setTitle(newName);
			requestLayout();
			// else if (tag instanceof ScreenFolderInfo)
			// {
			// ((ScreenFolderInfo) tag).mTitle = newName;
			// // 如果当前文件夹是打开的，则发消息更新编辑框的文字
			// if (((ScreenFolderInfo) tag).mOpened)
			// {
			// MsgMgrProxy.sendMessage(this,
			// IDiyFrameIds.DESK_USER_FOLDER_FRAME,
			// DeskUserFolderFrame.UPDATE_FOLDER_NAME, -1,
			// newName, null);
			// }// end if
			// }

		}

		/*
		 * if (mDesktopSettingInfo != null && mDesktopSettingInfo.isShowTitle()) { ((BubbleTextView)
		 * editingView).setText(newName); } else { ((BubbleTextView) editingView).setText(null); }
		 */

		// ((GLView) editingView).setText(newName);
	}

	//	@Override
	//	public void onAnimationStart(Animation animation) {
	//		// TODO Auto-generated method stub
	//
	//	}
	//
	//	@Override
	//	public void onAnimationEnd(Animation animation) {
	//		requestLayout();
	//	}
	//
	//	@Override
	//	public void onAnimationRepeat(Animation animation) {
	//		// TODO Auto-generated method stub
	//
	//	}

	// ---------start-------指示器------------//

	@Override
	public void clickIndicatorItem(int index) {
//		if (mGLSuperWorkspace.isShowingZero()) {
//			index--;
//		}
		if (null != mGLWorkspace) {
			index = mGLWorkspace.getZeroHandler().clickIndicatorItem(index);
			if (index < mGLWorkspace.getChildCount()
					&& mDesktopIndicator.getVisible()) {
				mGLWorkspace.snapToScreen(index, false, -1);
			}
		}
	}

	@Override
	public void sliding(float percent) {
		if (0 <= percent && percent <= 100 && mDesktopIndicator.getVisible()) {
			mGLWorkspace.getScreenScroller().setScrollPercent(percent);
		}
	}

	private void updateDesktopItem(final int screenIndex, ItemInfo itemInfo) {
		if (null != mControler) {
			mControler.updateDesktopItem(screenIndex, itemInfo);
			mGLWorkspace.refreshSubView();
			mGLWorkspace.requestLayout();
		}
	}

	private void updateIndicator(int type, Bundle bundle) {
		mDesktopIndicator.updateIndicator(type, bundle);
	}

	public void setIndicatorOnBottom(boolean yes) {
		sIndicatorOnBottom = yes;
	}

	/**
	 * 重置指示器位置
	 */
	private void setIndicatorPost(String position) {
		if (position != null) {
			setIndicatorOnBottom(position.equals(ScreenIndicator.INDICRATOR_ON_BOTTOM));
		}
		this.requestLayout();
		for (int i = 0; i < mGLWorkspace.getChildCount(); i++) {
			GLView screenView = mGLWorkspace.getChildAt(i);
			if (screenView != null) {
				screenView.requestLayout();
			}
		}
	}

	// --------end--------指示器------------//

	/**
	 * 处理数据改变
	 * 
	 * @param dataType
	 *            改变的数据类型
	 * @return 是否已处理
	 */
	private boolean handleAppCoreChange(int dataType, Object object) {
		boolean ret = false;
		switch (dataType) {
			case DataType.DATATYPE_DESKTOPSETING : {
				ret = true;
				CellUtils.init(getContext());
				mHandler.sendEmptyMessage(UPDATE_DESKTOP_SETTING);
				break;
			}

			case DataType.DATATYPE_EFFECTSETTING : {
				ret = true;
				mHandler.sendEmptyMessage(UPDATE_EFFECT_SETTING);
				break;
			}

			case DataType.DATATYPE_SCREENSETTING : {
				ret = true;
				mHandler.sendEmptyMessage(UPDATE_SCREEN_SETTING);
				break;
			}

			case DataType.DATATYPE_THEMESETTING : {
				ret = true;
				mHandler.sendEmptyMessage(UPDATE_THEME_SETTING);
				break;
			}

			case DataType.DATATYPE_APPDATA_REMOVE : {
				mHandler.sendMessage(mHandler.obtainMessage(REFRESH_UNINSTALL, object));
				break;
			}
//			case DataType.DATATYPE_DESKFONTCHANGED : {
//				if (object instanceof FontBean) {
//					FontBean bean = (FontBean) object;
//					initTypeface(bean);
//				}
//				break;
//			}
			default :
				break;
		}
		return ret;
	}

	@Override
	public int getViewId() {
		return IViewId.SCREEN;
	}

	@Override
	public void onAdd(GLViewGroup parent) {
		mGLWorkspace.setShell(mShell);
		// 注册监听器，支持scroll widget
		mGLWorkspace.registerProvider();
		startListening();
	}

	@Override
	public void onRemove() {
		// 取消scroll widget监听
		mGLWorkspace.unregisterProvider();
		mGLWorkspace.unbindWidgetScrollable();
	}

	private void addDesktopItem(int screenIndex, ItemInfo itemInfo) {
		if (null != mControler) {
			mControler.addDesktopItem(screenIndex, itemInfo);

//			if (itemInfo != null && itemInfo.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
//				AppItemInfo appItemInfo = ((ShortCutInfo) itemInfo).getRelativeItemInfo();
//				if (appItemInfo != null) {
//					CommonControler.getInstance(ApplicationProxy.getContext())
//							.checkShortCutIsIsRecommend(appItemInfo); // 检查加入屏幕的快捷方式是否推荐应用
//				}
//			}
		}
	}

	private GLView addDesktopView(ItemInfo itemInfo, final int screenIndex, boolean sync, boolean syncAddView) {
		final GLView view = createDesktopView(itemInfo, screenIndex, sync);
		if (view != null) {
			if (syncAddView) {
				// ADT-16647 挤压图标，图标挤压到桌面时会闪一下
				// 同步删除pos（x, y）对应图标，异步在pos(x, y)新增图标，时间间隔，造成跳帧。此处新增同步添加及删除入口，原入口调用逻辑不变
				final ItemInfo info = (ItemInfo) view.getTag();
				if (info != null) {
					try {
						mGLWorkspace.addInScreen(view, screenIndex, info.mCellX, info.mCellY,
								info.mSpanX, info.mSpanY, false);
					} catch (IllegalStateException e) {
						e.printStackTrace();
						Log.e("illegalstateException", "IllegalStateException add in screen");
					}
				}
			} else {
				mGLWorkspace.post(new Runnable() {
					@Override
					public void run() {
						final ItemInfo info = (ItemInfo) view.getTag();
						if (info != null) {
							try {
								mGLWorkspace.addInScreen(view, screenIndex, info.mCellX, info.mCellY,
										info.mSpanX, info.mSpanY, false);
							} catch (IllegalStateException e) {
								// 由于多线程的原因，可能会导致同一个view被加入两次的问题，出现这种情况捕获该问题
								e.printStackTrace();
								Log.e("illegalstateException", "IllegalStateException add in screen");
							}
						}
					}
				});
			}
		}
		return view;
	}

	private boolean addAppWidget(int widgetId) {
		boolean ret = false;
		int xy[] = new int[2];
		AppWidgetProviderInfo info = mWidgetManager.getAppWidgetInfo(widgetId);
		GLCellLayout cellLayout = mGLWorkspace.getCurrentScreenView();

		if (cellLayout == null || info == null) {
			ToastUtils.showToast(R.string.add_widget_failed, Toast.LENGTH_LONG);
		} else // 合法
		{
			int[] spans = new int[2];
			boolean vacant = true;
			if (mRestoreAppWidget) {
				//必须在最后真正添加widget时，才将需要恢复的WIDGET VIEW删除
				//同时保持原来widget view的位置信息
				deleteItem(mRestoreScreenAppWidgetInfo, mGLWorkspace.getCurrentScreen());
				spans[0] = mRestoreScreenAppWidgetInfo.mSpanX;
				spans[1] = mRestoreScreenAppWidgetInfo.mSpanY;
				xy[0] = mRestoreScreenAppWidgetInfo.mCellX;
				xy[1] = mRestoreScreenAppWidgetInfo.mCellY;
				mRestoreAppWidget = false;
				mRestoreScreenAppWidgetInfo = null;
			} else {
				spans = cellLayout.rectToCell(info.minWidth, info.minHeight);
				vacant = ScreenUtils.findVacant(xy, spans[0], spans[1],
						mGLWorkspace.getCurrentScreen(), mGLWorkspace);
			}

			if (!vacant) {
				setScreenRedBg();
			} else {
				OutOfMemoryHandler.gcIfAllocateOutOfHeapSize();
				boolean getHostView = true;
				AppWidgetHostView widgetView = null;
				try {
					widgetView = mWidgetHost.createView(mContext, widgetId, info);
				} catch (OutOfMemoryError e) {
					OutOfMemoryHandler.handle();
					getHostView = false;
				} catch (Throwable e) {
					Log.i(VIEW_LOG_TAG, "add widget Exception:" + e.toString());
					getHostView = false;
				}

				if (widgetView == null || !getHostView) {
					ToastUtils.showToast(R.string.add_widget_failed, Toast.LENGTH_LONG);
				} else {
					ScreenAppWidgetInfo appWidgetInfo = new ScreenAppWidgetInfo(widgetId,
							info.provider);
					appWidgetInfo.mHostView = widgetView;
					GLView glWidget = new GLWidgetContainer(mContext, new GLWidgetView(mContext,
							widgetView));

					glWidget.setTag(appWidgetInfo);
					appWidgetInfo.mCellX = xy[0];
					appWidgetInfo.mCellY = xy[1];
					appWidgetInfo.mSpanX = spans[0];
					appWidgetInfo.mSpanY = spans[1];
					mGLWorkspace.addInCurrentScreen(glWidget, xy[0], xy[1], spans[0], spans[1]);

					// 调整屏幕位置防止添加widget横竖屏切换位置偏移
					// mGLWorkspace.changeOrientation(true);

					addDesktopItem(mGLWorkspace.getCurrentScreen(), appWidgetInfo);

					// broadcast add widget
					ScreenUtils.appwidgetReadyBroadcast(widgetId, info.provider, spans,
							ShellAdmin.sShellManager.getActivity());
					GoViewCompatProxy
							.updateAppWidgetSize(
									widgetView,
									null,
									(int) (appWidgetInfo.mSpanX * GLCellLayout.sCellRealWidth / DrawUtils.sDensity),
									(int) (appWidgetInfo.mSpanY * GLCellLayout.sCellRealHeight / DrawUtils.sDensity),
									(int) (appWidgetInfo.mSpanX * GLCellLayout.sCellRealWidth / DrawUtils.sDensity),
									(int) (appWidgetInfo.mSpanY * GLCellLayout.sCellRealHeight / DrawUtils.sDensity));
					//天气通需求
					if (info.provider.getPackageName().equals(
							PackageName.RECOMMAND_TIANQITONG_PACKAGE)) {
						uninstallFakeWidget(PackageName.RECOMMAND_TIANQITONG_PACKAGE);
					}
					// 天天动听需求
					if (info.provider.getPackageName().equals(
							PackageName.RECOMMAND_TTDONDTING_PACKAGE)) {
						PreferencesManager sp = new PreferencesManager(mContext,
								IPreferencesIds.TTDONGTING_WIDGET_ADD, Context.MODE_PRIVATE);
						sp.putBoolean(IPreferencesIds.TTDONGTING_WIDGET_HAS_ADDED, true);
						sp.commit();
						uninstallFakeWidget(PackageName.RECOMMAND_TTDONDTING_PACKAGE);
					}
					ret = true;
				}
			}
		}

		if (!ret) // 添加失败需要删除已经分配的widgetid
		{
			mWidgetHost.deleteAppWidgetId(widgetId);
		}
		return ret;
	}

	/**
	 * 
	 * @param packageName
	 */
	private void uninstallFakeWidget(String packageName) {
		// 清理UI
		final int screenCount = mGLWorkspace.getChildCount();
		GLCellLayout layout = null;
		for (int i = 0; i < screenCount; i++) {
			layout = (GLCellLayout) mGLWorkspace.getChildAt(i);
			if (layout == null) {
				continue;
			}
			int index = 0;
			while (index < layout.getChildCount()) {
				final GLView childView = layout.getChildAt(index);
				if (childView != null) {
					final Object tag = childView.getTag();
					if (tag != null) {
						if (tag instanceof FavoriteInfo) {
							FavoriteInfo widgetBaseInfo = (FavoriteInfo) tag;
							GoWidgetBaseInfo gowidgetInfo = widgetBaseInfo.mWidgetInfo;
							if (gowidgetInfo == null
									|| (gowidgetInfo != null && gowidgetInfo.mPackage
											.equals(packageName))) {
								layout.removeView(childView);
								if (null != mControler) {
									mControler
											.removeDesktopItem((FavoriteInfo) tag);
								}
								continue;
							}
						}
					}
					++index;
				}
			}
		}
	}
	
	/**
	 * 添加GOWidget
	 * 
	 * @param bundle
	 * @param screenindex
	 *            第几个屏幕
	 * @return
	 */
	private boolean addGoWidget(Bundle bundle, int screenindex, int param) {
		if (bundle == null) {
			return false;
		}

		boolean ret = false;
		GoWidgetBaseInfo info = new GoWidgetBaseInfo();
		info.mWidgetId = bundle.getInt(GoWidgetConstant.GOWIDGET_ID);
		info.mType = bundle.getInt(GoWidgetConstant.GOWIDGET_TYPE);
		info.mLayout = bundle.getString(GoWidgetConstant.GOWIDGET_LAYOUT);
		info.mTheme = bundle.getString(GoWidgetConstant.GOWIDGET_THEME);
		info.mThemeId = bundle.getInt(GoWidgetConstant.GOWIDGET_THEMEID, -1);
		info.mPrototype = bundle.getInt(GoWidgetConstant.GOWIDGET_PROTOTYPE,
				GoWidgetBaseInfo.PROTOTYPE_NORMAL);
		info.mReplaceGroup = bundle.getInt(com.jiubang.ggheart.apps.gowidget.GoWidgetConstant.REPLACE_GROUP, -1);
		// 统计GO STORE为手动添加
		if (info.mPrototype == GoWidgetBaseInfo.PROTOTYPE_GOSTORE) {
			GoStoreStatisticsUtil.saveWidgetRecord(mContext, info.mWidgetId + "",
					GoStoreStatisticsUtil.WIDGET_KEY_ADD_TYPE, "1");
			GoStoreStatisticsUtil.saveWidgetRecord(mContext, info.mWidgetId + "",
					GoStoreStatisticsUtil.WIDGET_KEY_TYPE, info.mType + "");
		}
		GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
		// // 尝试找出其他内置的widget，如任务管理器widget

		AppWidgetProviderInfo providerInfo = bundle
				.getParcelable(GoWidgetConstant.GOWIDGET_PROVIDER);
		int minWidth = 0, minHeight = 0;
		if (providerInfo != null) {
			minWidth = providerInfo.minWidth;
			minHeight = providerInfo.minHeight;
			if (providerInfo.provider != null) {
				info.mPackage = providerInfo.provider.getPackageName();
				info.mEntry = providerInfo.provider.getClassName();
			}
			if (providerInfo.configure != null) {
				info.mClassName = providerInfo.configure.getClassName();
			}
			// update by zhoujun 应用中心的widget和gostore的
			// widget包名一样，这里需要用mPrototype来区分他们
			// InnerWidgetInfo innerWidgetInfo =
			// widgetManager.getInnerWidgetInfo(info.mPackage);
			InnerWidgetInfo innerWidgetInfo = widgetManager.getInnerWidgetInfo(info.mPrototype);
			// update by zhoujun 2012-08-13 end
			// 内置
			if (innerWidgetInfo != null) {
				// 更新包名为实际inflate xml的包名
				info.mPackage = innerWidgetInfo.mInflatePkg;
				info.mPrototype = innerWidgetInfo.mPrototype;
			}
		}

		boolean add = widgetManager.addGoWidget(info);

		GLCellLayout cellLayout = (GLCellLayout) mGLWorkspace.getChildAt(screenindex);
		if (cellLayout == null || !add) {
			return false;
		}

		// AbstractFrame topFrame = mFrameManager.getTopFrame();
		// final boolean fromScreenEditFrame = (topFrame != null &&
		// topFrame.getId() == IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
		// 修改是否在添加界面的判断
		boolean fromScreenEditFrame = false;
		if (GLWorkspace.sLayoutScale < 1.0f) {
			fromScreenEditFrame = true;
		}

		int xy[] = new int[2];
		final int[] spans = cellLayout.rectToCell(minWidth, minHeight);
		boolean vacant = ScreenUtils.findVacant(xy, spans[0], spans[1], screenindex, mGLWorkspace);
		if (!vacant) {
			// ScreenUtils.showToast(R.string.no_more_room, mActivity);

			// 对所有屏幕进行计算，并保持有足够空间添加gowidget的屏幕下标
			ArrayList<Integer> enoughSpaceIndexList = new ArrayList<Integer>();
			// 如果是用屏幕编辑进来的话，最后一个是虚拟屏，不需要包括在内，所以要-1
			final int count = fromScreenEditFrame
					? (mGLWorkspace.getChildCount() - 1)
					: mGLWorkspace.getChildCount();
			boolean hasEnoughSpace;
			for (int i = 0; i < count; i++) {
				cellLayout = (GLCellLayout) mGLWorkspace.getChildAt(i);
				int[] cell = cellLayout.rectToCell(minWidth, minHeight);
				hasEnoughSpace = ScreenUtils.findVacant(xy, cell[0], cell[1], i, mGLWorkspace);
				if (hasEnoughSpace) {
					enoughSpaceIndexList.add(i);
				}
			}
			if (fromScreenEditFrame) {
				setScreenRedBg();
			}

		} else {
			GLView widgetView = null;
			m3DWidgetManager = Go3DWidgetManager.getInstance(
					ShellAdmin.sShellManager.getActivity(), mContext);
			widgetView = m3DWidgetManager.createView(info.mWidgetId);
			if (widgetView == null) {
				ToastUtils.showToast(R.string.add_widget_failed, Toast.LENGTH_LONG);;
			} else {
				ScreenAppWidgetInfo appWidgetInfo = new ScreenAppWidgetInfo(info.mWidgetId);
				widgetView.setTag(appWidgetInfo);
				appWidgetInfo.mCellX = xy[0];
				appWidgetInfo.mCellY = xy[1];
				appWidgetInfo.mSpanX = spans[0];
				appWidgetInfo.mSpanY = spans[1];
				// TODO:传入选择屏的id
				mGLWorkspace.addInScreen(widgetView, screenindex, xy[0], xy[1], spans[0], spans[1]);
				m3DWidgetManager.startWidget(info.mWidgetId, bundle);
				addDesktopItem(screenindex, appWidgetInfo);
				ret = true;
			}
		}

		if (!ret) {
			widgetManager.deleteWidget(info.mWidgetId);
		}

		return ret;
	}

	/**
	 * 判断当前widget是否能成功添加至桌面
	 * 
	 * @param bundle
	 * @param screenindex
	 * @return
	 */
	private boolean addGoWidgetByCinfig(Bundle bundle, int screenindex) {
		if (bundle == null) {
			return false;
		}
		AppWidgetProviderInfo providerInfo = bundle
				.getParcelable(GoWidgetConstant.GOWIDGET_PROVIDER);
		int minWidthByCinfig = 0, minHeightByCinfig = 0;
		if (providerInfo != null) {
			minWidthByCinfig = providerInfo.minWidth;
			minHeightByCinfig = providerInfo.minHeight;
		}

		GLCellLayout cellLayout = (GLCellLayout) mGLWorkspace.getChildAt(screenindex);
		if (cellLayout == null) {
			return false;
		}
		int xy[] = new int[2];
		final int[] spans = cellLayout.rectToCell(minWidthByCinfig, minHeightByCinfig);
		boolean vacant = ScreenUtils.findVacant(xy, spans[0], spans[1], screenindex, mGLWorkspace);
		return vacant;
	}

	private boolean applyGoWidgetTheme(int widgetId, Bundle bundle) {
		if (bundle == null) {
			return false;
		}
		m3DWidgetManager.applyWidgetTheme(widgetId, bundle);
		return true;
	}

	/**
	 * 长按widget弹出大小调整编辑框
	 * @param widgetView
	 */
	private void editWdiget(GLView widgetView) {
		if (widgetView == null || mGLWorkspace == null) {
			return;
		}

		mIsWidgetEditMode = true;
		final GLCellLayout screen = mGLWorkspace.getCurrentScreenView();
		if (screen == null) {
			return;
		}

		mGLWidgetView = widgetView;

		final float minw = GLCellLayout.sCellRealWidth; //每个小空格的宽度
		final float minh = GLCellLayout.sCellRealHeight; //每个小空格的高度

		ScreenAppWidgetInfo widgetInfo = (ScreenAppWidgetInfo) widgetView.getTag();

		final int screenRectLeft = GLCellLayout.getLeftPadding();
		final int screenRectTop = GLCellLayout.getTopPadding();
		final int screenPaddingRight = GLCellLayout.getRightPadding();
		final int screenPaddingBottom = GLCellLayout.getBottomPadding();

		final int screenWdith = screen.getWidth(); //屏幕宽度
		final int screenHeight = screen.getHeight(); //屏幕高度

		//屏幕所占的矩形区域
		final Rect screenRect = new Rect(screenRectLeft, screenRectTop, screenWdith
				- screenPaddingRight, screenHeight - screenPaddingBottom);

		final int widgetX = widgetInfo.mCellX * (int) minw;  //widget左边距 = 所在区域位置 X 每个空置的宽度
		final int widgetY = widgetInfo.mCellY * (int) minh;
		final float widgetWidth = widgetInfo.mSpanX * minw;
		final float widgetHeight = widgetInfo.mSpanY * minh;

		//widget所在的矩形区域
		Rect widgetRect = new Rect(widgetX, widgetY, (int) (widgetX + widgetWidth),
				(int) (widgetY + widgetHeight));
		widgetRect.offset(screenRectLeft, screenRectTop);

		// 加入widget缩放view
		if (mGlWidgetResizeView == null) {
			mGlWidgetResizeView = new GLWidgetResizeView(mContext);
		}
		mGlWidgetResizeView.setSize(screenRect, widgetRect, null);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		if (mGlWidgetResizeView.getGLParent() != null) {
			((GLViewGroup) mGlWidgetResizeView.getGLParent()).removeView(mGlWidgetResizeView);
		}
		addView(mGlWidgetResizeView, params);
		mGlWidgetResizeView.requestFocus();
	}

	private void stopWidgetEdit() {
		if (!mIsWidgetEditMode) {
			mGLWidgetView = null;
			return;
		}
		removeView(mGlWidgetResizeView);
		mGlWidgetResizeView.cleanup();
		mIsWidgetEditMode = false;
		if (mGLWidgetView != null) {
			Object tag = mGLWidgetView.getTag();
			if (tag != null && tag instanceof ScreenAppWidgetInfo) {
				ScreenAppWidgetInfo widgetInfo = (ScreenAppWidgetInfo) tag;

				// 更新数据库
				if (mGLWorkspace != null) {
					updateDesktopItem(mGLWorkspace.getCurrentScreen(), widgetInfo);
				}
			}
			mGLWidgetView = null;
		}
	}

	/**
	 * 添加界面，判断当前屏是否有足够空间
	 */
	private boolean checkVacant(List<Integer> objects, int[] centerXY, float[] translate) {
		if (objects != null && objects instanceof ArrayList && objects.size() > 1) {
			int spanX = objects.get(0);
			int spanY = objects.get(1);
			int[] xy = new int[2];
			//			scale[0] = GLWorkspace.sLayoutScale;
			boolean vacant = false;
			if (mNewFolderIcon != null && mNewFolderIcon.getInfo() != null) {
				UserFolderInfo useFolderInfo = mNewFolderIcon.getInfo();
				spanX = 1;
				spanY = 1;
				xy[0] = useFolderInfo.mCellX;
				xy[1] = useFolderInfo.mCellY;
				vacant = true;
			} else {
				vacant = ScreenUtils.findVacant(xy, spanX, spanY, mGLWorkspace.getCurrentScreen(),
						mGLWorkspace);
			}
			if (vacant) {
				GLCellLayout.cellsToCenterPoint(xy[0], xy[1], spanX, spanY, centerXY);
				translate[0] = mGLWorkspace.getTranslateY();
				translate[1] = mGLWorkspace.getTranslateZ();
				float[] realXY = new float[2];
				realXY = IconUtils.getIconCenterPoint(centerXY[0], centerXY[1],
						GLScreenShortCutIcon.class);
				centerXY[0] = (int) realXY[0];
				centerXY[1] = (int) realXY[1];
				return true;
			} else {
				xy = null;
				// 变红
				setScreenRedBg();
				return false;
			}
		}
		return false;
	}

	/**
	 * 添加界面，判断当前屏是否有足够空间
	 */
	private boolean checkVacant(List<Integer> objects, int[] centerXY, int[] cellXY) {
		if (objects != null && objects instanceof ArrayList && objects.size() > 1) {
			int spanX = objects.get(0);
			int spanY = objects.get(1);
			int[] xy = new int[2];
			boolean vacant = false;

			vacant = ScreenUtils.findVacant(xy, spanX, spanY, mGLWorkspace.getCurrentScreen(),
					mGLWorkspace);
			cellXY[0] = xy[0];
			cellXY[1] = xy[1];

			if (vacant) {
				GLCellLayout.cellsToCenterPoint(xy[0], xy[1], spanX, spanY, centerXY);
				return true;
			}
		}
		return false;
	}

	public ScreenControler getScreenControler() {
		return mControler;
	}

	/**
	 * 长按桌面图标或widget的处理
	 */
	private void handleLongClickAction(Object[] objects) {
		if (objects != null) {
			GLView oldView = (GLView) objects[0];
			GLView newView = oldView;
			if (!(oldView instanceof GLCellLayout)) {
				newView = (GLView) oldView.getGLParent();
			}
			GLCellLayout.CellInfo cellInfo = (GLCellLayout.CellInfo) newView.getTag();
			// This happens when long clicking an item with the dpad/trackball
			if (cellInfo == null) {
				return;
			}
			if (mGLWorkspace.allowLongPress()) {
				if (cellInfo.cell == null) {
					// 如果长按的是空白区域且无动画
					if (cellInfo.valid) { // 如果数据合法
						// User long pressed on empty space
						mGLWorkspace.setAllowLongPress(false); // 设置不能长按
						mGLWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
								HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);

						// TODO:直接进入编辑界面,进入workspaceeditor并显示动画
					}
				} else {
					if (!mGLWorkspace.isLock()) {
						// 如果是长按某个子项，就进行拖拽
						// User long pressed on an item
						mGLWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
								HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
						mGLWorkspace.startDrag(cellInfo);
					}
					// 判断是否在添加界面。如果在桌面且不是编辑状态就显示快捷菜单
					if (!mGLWorkspace.isEditState()) {
						//						mGLWorkspace.setShouldShowQuickMenu(oldView);
						//						showQuickActionMenu(oldView);
						// 长按桌面图标提示，前三次提示  add by zhengxiangcan
						if (oldView != null) {
							if (oldView.getTag() instanceof ItemInfo) {
								ItemInfo info = (ItemInfo) oldView.getTag();
								// 当长按图标为桌面应用的图标（非文件夹、widget）
								if (info != null) {
									if (info.mItemType == IItemType.ITEM_TYPE_APPLICATION
											|| info.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
										PreferencesManager manager = new PreferencesManager(
												mContext,
												IPreferencesIds.DESK_SHAREPREFERENCES_FILE,
												Context.MODE_PRIVATE);
										int count = manager.getInt(
												IPreferencesIds.SCREEN_LONG_PRESS_TIP, 0);
										if (count < 3) {
											count++;
											manager.putInt(IPreferencesIds.SCREEN_LONG_PRESS_TIP,
													count);
											manager.commit();
											DeskToast
													.makeText(
															GoLauncherActivityProxy.getActivity(),
															ShellAdmin.sShellManager
																	.getContext()
																	.getString(
																			R.string.screen_long_press_new_tip),
															Toast.LENGTH_LONG).show();
										} // end if count
									}
								} // end if info
							} // end if targetView
							if (oldView instanceof GLScreenShortCutIcon
									|| oldView instanceof GLScreenFolderIcon) {
								oldView.setPressed(false);
							}
						}
					}
				}
			}
		}
	} // end handleLongClickAction

	/**
	 * 合并文件夹
	 * 
	 * @return
	 */
	private boolean mergeFolder(DragView dragView, int screenIndex, final int x, final int y) {
		final GLCellLayout currentCellLayout = mGLWorkspace.getCurrentDropLayout();
		if (null != currentCellLayout
				&& mGLWorkspace.getDragMode() == GLWorkspace.DRAG_MODE_CREATE_FOLDER) {
			// 如果当前屏绘制状态是DRAW_STATUS_MERGE_FOLDER，就判定为合并文件夹
			// 合并文件夹操作
			// View target = currentCellLayout.getmMergerFolderChildView();
			final GLView target = currentCellLayout.getChildViewByCell(mGLWorkspace.mTargetCell);
			final GLView source = dragView.getOriginalView();
			if (target == null || source == null) {
				return false;
			}

			ItemInfo targetInfo = (ItemInfo) target.getTag();
			ItemInfo sourceInfo = null;
			final int dragViewType = dragView.getDragViewType();

			if (dragViewType == DragView.DRAGVIEW_TYPE_SCREEN_ICON) {
				sourceInfo = (ItemInfo) source.getTag();
			}
			// 如果不是桌面的shortCutInfo类型，则进行类型转换
			if (dragViewType == DragView.DRAGVIEW_TYPE_APPDRAWER_ICON) {
				sourceInfo = DragInfoTranslater
						.createShortCutInfoForScreen(((GLAppDrawerAppIcon) source).getInfo());
			} else if (dragViewType == DragView.DRAGVIEW_TYPE_DOCK_ICON) {
				sourceInfo = DragInfoTranslater.createItemInfoForScreen(dragViewType,
						((GLDockIconView) source).getInfo());
			} else if (dragViewType == DragView.DRAGVIEW_TYPE_SCREEN_USERFOLDER) {
				sourceInfo = ((GLScreenFolderIcon) source).getInfo();
				GuiThemeStatistics
						.goLauncherUserBehaviorStaticDataCache(IGoLauncherUserBehaviorStatic.LAUNCHER_OP_03);
			} else if (dragViewType == DragView.DRAGVIEW_TYPE_DOCK_FOLDER) {
				sourceInfo = DragInfoTranslater.createItemInfoForScreen(dragViewType,
						((GLDockFolderIcon) source).getInfo());
				GuiThemeStatistics
						.goLauncherUserBehaviorStaticDataCache(IGoLauncherUserBehaviorStatic.LAUNCHER_OP_03);
			} else if (dragViewType == DragView.DRAGVIEW_TYPE_APPDRAWER_FOLDER) {
				sourceInfo = DragInfoTranslater.createItemInfoForScreen(dragViewType,
						((GLAppDrawerFolderIcon) source).getInfo());
				GuiThemeStatistics
						.goLauncherUserBehaviorStaticDataCache(IGoLauncherUserBehaviorStatic.LAUNCHER_OP_03);
			} else if (dragViewType == DragView.DRAGVIEW_TYPE_SCREEN_USERFOLDER_ELEMENT) {
				sourceInfo = DragInfoTranslater.createShortCutInfoForScreen((ShortCutInfo) source
						.getTag());
			}

			if (targetInfo == null || sourceInfo == null) {
				return false;
			}
			if (screenIndex != targetInfo.mScreenIndex) {
				return false;
			}

			if (targetInfo instanceof ShortCutInfo && sourceInfo instanceof ShortCutInfo) {
				// 判断是否是无效图标（如：名称为加载中的图标）
				Intent it = ((ShortCutInfo) sourceInfo).mIntent;
				if (it == null) {
					return false;
				}
				// 文件夹智能命名 封装list
				ArrayList<AppItemInfo> infoList = new ArrayList<AppItemInfo>();
				infoList.add(((ShortCutInfo) sourceInfo).getRelativeItemInfo());
				infoList.add(((ShortCutInfo) targetInfo).getRelativeItemInfo());
				// 添加文件夹
				UserFolderInfo folderInfo = new UserFolderInfo();
				folderInfo.mTitle = mContext.getResources().getString(R.string.folder_name);
				folderInfo.mInScreenId = System.currentTimeMillis();
				folderInfo.mCellX = targetInfo.mCellX;
				folderInfo.mCellY = targetInfo.mCellY;

				// add by:zzf 相同程序合并时，过滤掉一个
				if (((ShortCutInfo) targetInfo).mIntent == null
						|| ((ShortCutInfo) sourceInfo).mIntent == null) {
					return false;
				}
				// 通过componentName比较，不用转String
				//解决不了dock默认图标的intent问题
				final boolean equal = ConvertUtils.intentCompare(
						((ShortCutInfo) targetInfo).mIntent, ((ShortCutInfo) sourceInfo).mIntent);
				if (!equal) {
					// 如果不同，进行移动操作
					folderInfo.add(targetInfo);
					mControler.moveDesktopItemToFolder(targetInfo, folderInfo.mInScreenId);
				} else {
					// 删除桌面程序DB信息
					mControler.removeDesktopItemInDBAndCache(targetInfo);
				}
				// 移除View,无须移除view的监听者等信息
				currentCellLayout.removeView(target);
				//					ScreenUtils.removeViewByItemInfo(targetInfo, mGLWorkspace);

				folderInfo.add(sourceInfo);
				addDesktopItem(screenIndex, folderInfo);

				// if (mDragType == DragFrame.TYPE_SCREEN_FOLDER_DRAG) {
				// // 从文件夹拖出來的，还要删除文件夹内的图标
				// if (mCurrentFolderInfo != null) {
				// if (null != mControler) {
				// mControler.moveDesktopItemFromFolder(sourceInfo,
				// mGLWorkspace.getCurrentScreen(), mCurrentFolderInfo.mInScreenId);
				// mControler.addItemInfoToFolder(sourceInfo, folderInfo.mInScreenId);
				// MsgMgrProxy.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
				// IScreenFrameMsgId.DELETE_CACHE_INFO_IN_FOLDER, -1, sourceInfo, null);
				// }
				// // 更新缓存
				// if (mCurrentFolderInfo instanceof UserFolderInfo) {
				// ((UserFolderInfo) mCurrentFolderInfo).remove(sourceInfo.mInScreenId);
				// if (!deleteFolderOrNot((UserFolderInfo) mCurrentFolderInfo, true)) {
				// // 更新文件夹图标
				// updateFolderIconAsync((UserFolderInfo) mCurrentFolderInfo, false,
				// false);
				// }
				// }
				// mCurrentFolderInfo = null;
				// }
				// } else {
				{
					// 删除桌面项
					// if (mDragType == DragFrame.TYPE_DOCK_DRAG && sourceInfo.mInScreenId == 0) {
					// sourceInfo.mInScreenId = System.currentTimeMillis();
					// }
					mControler.moveDesktopItemToFolder(sourceInfo, folderInfo.mInScreenId);

					if (dragViewType == DragView.DRAGVIEW_TYPE_SCREEN_ICON) {
						final int index = sourceInfo.mScreenIndex;
						if (index >= 0 && index < mGLWorkspace.getChildCount()) {
							// 删除ＵＩ，无须移除view的监听者等信息
							GLCellLayout sourceLayout = (GLCellLayout) mGLWorkspace
									.getChildAt(index);
							if (sourceLayout != null && sourceLayout.indexOfChild(source) >= 0) {
								sourceLayout.removeView(source);
							}
							//							ScreenUtils.removeViewByItemInfo(sourceInfo, mGLWorkspace);
						}
					}
				}

				final GLAppFolderInfo info = new GLAppFolderInfo(folderInfo,
						GLAppFolderInfo.FOLDER_FROM_SCREEN);
				final BaseFolderIcon folderIcon = GLAppFolder.getInstance().getFolderIcon(info);
				// 增加ＵＩ
				// FolderIcon newFolder = FolderIcon.fromXml(getFolderIconId(), mActivity,
				// mGLWorkspace.getCurrentScreenView(), folderInfo,
				// getDisplayTitle(folderInfo.mTitle));
				// newFolder.close();
				// newFolder.setShowShadow(getShadowState());
				mGLWorkspace.addInCurrentScreen(folderIcon, folderInfo.mCellX, folderInfo.mCellY,
						1, 1);
				// 必须要加到屏幕之后再刷新
				folderIcon.setInfo(folderInfo);

				// 刷新文件夹内容
				// refreshFolderItems(folderInfo.mInScreenId, false);
				// mMergeFordleIconToOpen = newFolder;

				folderInfo.mIsFirstCreate = true;
				// 文件夹智能命名-场景：图标重叠创建文件夹
				String smartfoldername = CommonControler.getInstance(mContext).generateFolderName(
						infoList);
				actionRename(smartfoldername, folderInfo.mInScreenId);
				ShellAdmin.sShellManager.getShell().show(IViewId.PROTECTED_LAYER, false);
				mGLWorkspace.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						folderIcon.setFolderCoverAnimationListner(new FolderCoverAnimationListener() {
							
							@Override
							public void onFolderCoverAnimationEnd(boolean isOpened, GLView icon) {
								ShellAdmin.sShellManager.getShell().hide(IViewId.PROTECTED_LAYER, false);
								folderIcon.setFolderCoverAnimationListner(null);
							}
						});
						folderIcon.createFolderAnimation(x, y, new AnimationListenerAdapter() {

							@Override
							public void onAnimationEnd(Animation animation) {
								// TODO Auto-generated method stub
								post(new Runnable() {

									@Override
									public void run() {
										postDelayed(new Runnable() {

											@Override
											public void run() {
												if (mGLWorkspace.mScaleState == GLWorkspace.STATE_NORMAL) {
													folderIcon.openFolder();
												}
//												GLAppFolder.getInstance()
//														.batchStartIconEditEndAnimation();
											}
										}, 100);

										target.cleanup();
										if (!(source instanceof GLAppDrawerAppIcon)) {
											source.cleanup();
										}
									}
								});

							}
						});
					}

				});
				return true;
			} else if (targetInfo instanceof UserFolderInfo && sourceInfo instanceof UserFolderInfo) {

				// 以拖拽目标作为新文件夹的壳子
				final UserFolderInfo newFolderInfo = (UserFolderInfo) targetInfo;
				// 被拖拽的文件夹将被删除
				final UserFolderInfo delFolderInfo = (UserFolderInfo) sourceInfo;

				ArrayList<AppItemInfo> infoList = new ArrayList<AppItemInfo>();

				for (int i = 0; i < newFolderInfo.getContents().size(); i++) {
					infoList.add(newFolderInfo.getChildInfo(i).getRelativeItemInfo());
				}

				for (int i = 0; i < delFolderInfo.getContents().size(); i++) {
					infoList.add(delFolderInfo.getChildInfo(i).getRelativeItemInfo());

					if (mControler != null) {
						// 图标去重
						mControler.removeItemsFromFolder(newFolderInfo,
								delFolderInfo.getChildInfo(i));
						newFolderInfo.add(delFolderInfo.getContents().get(i));
						mControler.moveDesktopItemToFolder(delFolderInfo.getContents().get(i),
								newFolderInfo.mInScreenId);
					}
				}

				if (dragView.getDragViewType() == DragView.DRAGVIEW_TYPE_SCREEN_USERFOLDER) {
					deleteScreenFolder(delFolderInfo, delFolderInfo.mScreenIndex, true);
					mControler.removeUserFolder(delFolderInfo);
				}

				final GLAppFolderInfo info = new GLAppFolderInfo(newFolderInfo,
						GLAppFolderInfo.FOLDER_FROM_SCREEN);
				final BaseFolderIcon folderIcon = GLAppFolder.getInstance().getFolderIcon(info);
				// 增加ＵＩ
				// FolderIcon newFolder = FolderIcon.fromXml(getFolderIconId(), mActivity,
				// mGLWorkspace.getCurrentScreenView(), folderInfo,
				// getDisplayTitle(folderInfo.mTitle));
				// newFolder.close();
				// newFolder.setShowShadow(getShadowState());
				mGLWorkspace.addInCurrentScreen(folderIcon, newFolderInfo.mCellX,
						newFolderInfo.mCellY, 1, 1);
				// 必须要加到屏幕之后再刷新
				folderIcon.setInfo(newFolderInfo);

				// 刷新文件夹内容
				// refreshFolderItems(folderInfo.mInScreenId, false);
				// mMergeFordleIconToOpen = newFolder;

				newFolderInfo.mIsFirstCreate = true;
				// 文件夹智能命名-场景：图标重叠创建文件夹
				//智能命名
				boolean needName = ((UserFolderInfo) targetInfo).mTitle.equals(mContext
						.getString(R.string.folder_name)) ? true : false;
				if (needName) {
					String smartfoldername = CommonControler.getInstance(mContext)
							.generateFolderName(infoList);
					actionRename(smartfoldername, newFolderInfo.mInScreenId);
				}
				mGLWorkspace.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						folderIcon.cancleFolderReady();
//						GLAppFolder.getInstance().batchStartIconEditEndAnimation();
					}

				});
				// 显示转圈圈
				// showProgressDialog();
				// // 以拖拽目标作为新文件夹的壳子
				// final UserFolderInfo newFolderInfo = (UserFolderInfo) targetInfo;
				// // 被拖拽的文件夹将被删除
				// final UserFolderInfo delFolderInfo = (UserFolderInfo) sourceInfo;
				// // 如果是屏幕的拖拽，先删除屏幕上的文件夹
				// if (mDragType == DragFrame.TYPE_SCREEN_ITEM_DRAG) {
				// ScreenUtils.removeViewByItemInfo(delFolderInfo, mGLWorkspace);
				// }
				// //文件夹智能命名-场景：文件夹合并创建文件夹
				// ArrayList<AppItemInfo> infoList = new ArrayList<AppItemInfo>();
				// String defaultname = mActivity.getResources().getString(
				// R.string.folder_name);
				// //未命名文件夹才参与智能命名
				// if (newFolderInfo.mTitle.equals(defaultname)) {
				// for (int i = 0; i < newFolderInfo.getChildCount(); i++) {
				// ShortCutInfo itemInfo = newFolderInfo.getChildInfo(i);
				// if (itemInfo != null) {
				// infoList.add(itemInfo.getRelativeItemInfo());
				// }
				// }
				// for (int i = 0; i < delFolderInfo.getChildCount(); i++) {
				// ShortCutInfo itemInfo = delFolderInfo.getChildInfo(i);
				// if (itemInfo != null) {
				// infoList.add(itemInfo.getRelativeItemInfo());
				// }
				// }
				// String smartfoldername = CommonControler.getInstance(
				// mActivity).generateFolderName(infoList);
				// actionRename(smartfoldername, targetInfo.mInScreenId);
				// }
				//
				// new Thread(ThreadName.SCREEN_FOLDER_MERGING) {
				// @Override
				// public void run() {
				// final int count = delFolderInfo.getChildCount();
				// ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
				// for (int i = 0; i < count; i++) {
				// ShortCutInfo itemInfo = delFolderInfo.getChildInfo(i);
				// if (itemInfo != null) {
				// items.add(itemInfo);
				// // 图标去重
				// mControler.removeItemsFromFolder(newFolderInfo, itemInfo);
				// }
				// }
				// final boolean dragFromAppfunc = mDragType ==
				// DragFrame.TYPE_APPFUNC_FOLDERITEM_DRAG
				// ? true
				// : false;
				// addUserFolderContent(newFolderInfo.mInScreenId, newFolderInfo, items,
				// dragFromAppfunc);
				// newFolderInfo.addAll(items);
				// newFolderInfo.mTotleUnreadCount += delFolderInfo.mTotleUnreadCount;
				// // refreshFolderItems(newFolderInfo.mInScreenId, false);
				//
				// if (mDragType == DragFrame.TYPE_SCREEN_ITEM_DRAG) {
				// mControler.removeUserFolder(delFolderInfo);
				// // 删除文件夹
				// ScreenUtils.unbindeUserFolder(delFolderInfo);
				// }
				// // 合并完成，通知关闭进度条
				// Message message = mHandler.obtainMessage();
				// message.what = FINISH_MERGING;
				// message.obj = new long[] { newFolderInfo.mInScreenId };
				// mHandler.sendMessage(message);
				// };
				// }.start();
				// return true;
				
			}// end else if
		}
		return false;
	} // end mergeFolder

	private void addInFolderAnimation(final GLView target, final DragView dragView, final int x,
			final int y, DropAnimationInfo resetInfo, final GLView originalView,
			final boolean cleanUp, final int addIndex, final DragSource dragSource) {
//		((GLScreenFolderIcon) target).refreshIcon();
		((GLScreenFolderIcon) target).refreshForAddIcon(addIndex);
		final AnimationListener animationListener = new AnimationListenerAdapter() {

			@Override
			public void onAnimationEnd(Animation animation) {
				post(new Runnable() {

					@Override
					public void run() {
						//						((GLScreenFolderIcon) target).refreshIcon();
						if (cleanUp) {
							originalView.cleanup();
						}
						
//						postDelayed(new Runnable() {
//
//							@Override
//							public void run() {
//								GLAppFolder.getInstance().batchStartIconEditEndAnimation();
//							}
//
//						}, 50);
						final GLScreenFolderIcon folderIcon = (GLScreenFolderIcon) target;
						if (dragSource instanceof GLScreenFolderGridView) {
							GLScreenFolderGridView folderGridView = (GLScreenFolderGridView) dragSource;
							if (folderIcon != folderGridView.getFolderIcon()) {
								folderGridView.getFolderIcon().refreshIcon();
							}
						}
						if (folderIcon.getFolderChildCount() > 4) {
							postDelayed(new Runnable() {
								@Override
								public void run() {
									folderIcon.refreshIcon();
								}
							}, 1000);
						}
						
						//						int childCount = folderIcon.getFolderChildCount();
						//						switch (childCount) {
						//							case 4 :
						//							case 6 :
						//								folderIcon.startEditAnimation();
						//								break;
						//							case 8 :
						//								folderIcon.startEditAnimation();
						//								postDelayed(new Runnable() {
						//									@Override
						//									public void run() {
						//										folderIcon.refreshForAddIcon(BaseFolderIcon.MAX_ICON_COUNT);
						//									}
						//								}, 1000);
						//								break;
						//							default :
						//								break;
						//						}
					}
				});

			}
		};
//		if (addIndex >= BaseFolderIcon.MAX_ICON_COUNT) {
//			Rect outRect = ((GLScreenFolderIcon) target).getOperationArea(null)[0];
//			resetInfo.setLocationType(DropAnimationInfo.LOCATION_CENTER);
//			resetInfo.setLocationPoint(outRect.left + ((GLScreenFolderIcon) target).getIconSize()
//					/ 2, outRect.top + ((GLScreenFolderIcon) target).getIconSize() / 2);
//			resetInfo.setDuration(250);
//			resetInfo.setFinalScale(0);
//			resetInfo.setAnimationListener(animationListener);
//			((GLScreenFolderIcon) target).addInFolderAnimation(x, y, addIndex,
//					(IconView<?>) dragView.getOriginalView(), null);
//		} else {
			resetInfo.setDuration(0);
			ShellAdmin.sShellManager.getShell().show(IViewId.PROTECTED_LAYER, false);
			mGLWorkspace.post(new Runnable() {

				@Override
				public void run() {
					((GLScreenFolderIcon) target).setFolderCoverAnimationListner(new FolderCoverAnimationListener() {
						
						@Override
						public void onFolderCoverAnimationEnd(boolean isOpened, GLView icon) {
							ShellAdmin.sShellManager.getShell().hide(IViewId.PROTECTED_LAYER, false);
							((GLScreenFolderIcon) target).setFolderCoverAnimationListner(null);
						}
					});
					((GLScreenFolderIcon) target).addInFolderAnimation(x, y, addIndex,
							 animationListener);
				}
			});

//		}
	}

	private boolean moveToFolder(final DragView dragView, int screenIndex, final int x,
			final int y, final DropAnimationInfo resetInfo, final Object dragInfo, DragSource dragSource) {
		if (dragView == null || dragView.getOriginalView() == null) {
			// 设置进入预览的标识为false，否则下次将无法拖动图标进文件夹
			// mIsShowPreview = false;
			return false;
		}

		final int dragViewType = dragView.getDragViewType();
		final GLView originalView = dragView.getOriginalView();
		GLCellLayout currentScreen = (GLCellLayout) mGLWorkspace.getChildAt(screenIndex);
		if (currentScreen == null) {
			return false;
		}

		Object tagObject = null;
		boolean sameFolder = false;
		if (dragViewType == DragView.DRAGVIEW_TYPE_SCREEN_ICON) {
			tagObject = originalView.getTag();
		}
		// 如果不是桌面的shortCutInfo类型，则进行类型转换
		if (dragViewType == DragView.DRAGVIEW_TYPE_APPDRAWER_ICON) {
			tagObject = DragInfoTranslater
					.createShortCutInfoForScreen(((GLAppDrawerAppIcon) originalView).getInfo());
		} else if (dragViewType == DragView.DRAGVIEW_TYPE_DOCK_ICON) {
			tagObject = DragInfoTranslater
					.createShortCutInfoForScreen(((GLDockIconView) originalView).getInfo());
		} else if (dragViewType == DragView.DRAGVIEW_TYPE_SCREEN_USERFOLDER_ELEMENT) {
			final GLView target = currentScreen.getChildViewByCell(mGLWorkspace.mTargetCell);
			final UserFolderInfo folderInfo = (UserFolderInfo) target.getTag();
			if (folderInfo.getContents().contains((ShortCutInfo) originalView.getTag())) {
				sameFolder = true;
			}
			tagObject = DragInfoTranslater.createShortCutInfoForScreen((ShortCutInfo) originalView
					.getTag());
		}

		if (tagObject == null || !(tagObject instanceof ShortCutInfo)) {
			return false;
		}
		final ShortCutInfo itemInfo = (ShortCutInfo) tagObject;
		final GLView target = currentScreen.getChildViewByCell(mGLWorkspace.mTargetCell);
		final UserFolderInfo folderInfo = (UserFolderInfo) target.getTag();
		// 如果是从此文件夹内拖出来的图标则不进行操作
		if (folderInfo.getContents() != null && mControler.isTheSameIconInToFolder(folderInfo, itemInfo)
				&& dragViewType == DragView.DRAGVIEW_TYPE_SCREEN_USERFOLDER_ELEMENT && sameFolder) {
			int addIndex = mControler.getAddInFolderIndex(((GLScreenFolderIcon) target).getInfo(),
					itemInfo);
			addIndex = mControler.handleDuplicateItemInfo(itemInfo, folderInfo, addIndex,
					FolderConstant.MAX_ICON_COUNT);
			//			((GLScreenFolderIcon) target).refreshIcon();
			addInFolderAnimation(target, dragView, x, y, resetInfo, originalView, false, addIndex,
					dragSource);
			//因为动画已经做了关闭操作，不需要在onDragExit里 setDragMode(DRAG_MODE_NONE)再关闭，直接置空
			mGLWorkspace.setDragTrgetIconNull();
			return false;
		}
		if (dragViewType == DragView.DRAGVIEW_TYPE_SCREEN_ICON) {
			if (screenIndex == itemInfo.mScreenIndex && originalView instanceof IconView) {
				currentScreen.removeView(originalView);
				//由于动画需要，不能进行cleanup，放到动画做完后cleanup
				//originalView.cleanup(); 
				// tagInfo.unRegisterObserver(dragView);
			} else {
				if (originalView.getGLParent() instanceof GLCellLayout) {
					((GLCellLayout) originalView.getGLParent()).removeView(originalView);
				}
				// deleteItem(itemInfo, -1);
			}
			if (mControler != null) {
				if (mControler.isTheSameIconInToFolder(folderInfo, dragInfo)) {
					//					((GLScreenFolderIcon) target).refreshIcon();
					int addIndex = mControler.getAddInFolderIndex(
							((GLScreenFolderIcon) target).getInfo(), dragInfo);
					addIndex = mControler.handleDuplicateItemInfo(itemInfo, folderInfo, addIndex,
							FolderConstant.MAX_ICON_COUNT);
					addInFolderAnimation(target, dragView, x, y, resetInfo, originalView, false,
							addIndex, dragSource);
					mControler.removeDesktopItem((ItemInfo) dragInfo);
					return true;
				}
				// 图标去重
				//				mControler.removeItemsFromFolder(folderInfo, itemInfo);
				// 修改数据库
				mControler.moveDesktopItemToFolder(itemInfo, folderInfo.mInScreenId);

			}
		} else if (dragViewType == DragView.DRAGVIEW_TYPE_APPDRAWER_ICON
				|| dragViewType == DragView.DRAGVIEW_TYPE_DOCK_ICON
				|| dragViewType == DragView.DRAGVIEW_TYPE_SCREEN_USERFOLDER_ELEMENT) {
			if (mControler.isTheSameIconInToFolder(folderInfo, itemInfo)) {
				int addIndex = mControler.getAddInFolderIndex(
						((GLScreenFolderIcon) target).getInfo(), itemInfo);
				addIndex = mControler.handleDuplicateItemInfo(itemInfo, folderInfo, addIndex,
						FolderConstant.MAX_ICON_COUNT);
				addInFolderAnimation(target, dragView, x, y, resetInfo, originalView, false,
						addIndex, dragSource);
				return true;
			}
			mControler.dragItemInfoToFolder(itemInfo, folderInfo);
		} 
			// 添加到缓存
		folderInfo.add(itemInfo);
		// 刷新文件夹
		// 功能标拖出的图标加入桌面文件夹不需要cleanup，要不然会空了一个位置
		boolean needCleanUp = dragViewType == DragView.DRAGVIEW_TYPE_APPDRAWER_ICON ? false : true;
		final int addIndex = mControler.getAddInFolderIndex(
				((GLScreenFolderIcon) target).getInfo(), dragInfo);
		addInFolderAnimation(target, dragView, x, y, resetInfo, originalView, needCleanUp,
				addIndex, dragSource);
		return true;
	}

	// 添加界面交互逻辑

	/***
	 * 屏幕飞图标
	 * 
	 * @param GLView
	 * @param dragType
	 * @return
	 */
	private boolean screenAutoFly(GLView view, int dragType) {
		// setScreenRedBg();
		boolean addable = true; //是否要执行添加的标识
		if (mCellPos == null) {
			mCellPos = new int[2];
		}
		
		//从添加模块添加文件夹
		if (dragType == ScreenEditConstants.ADD_FOLDER_ID) {
			if (mNewFolderIcon == null) {
				return false;
			}
			final GLScreenFolderIcon folder = mNewFolderIcon;
			final UserFolderInfo userFolderInfo = folder.getInfo();
			final ItemInfo itemInfo = (ItemInfo) view.getTag();

			if (itemInfo == null || userFolderInfo == null
					|| userFolderInfo.mScreenIndex != mGLWorkspace.getCurrentScreen()) {
				return false;
			}
			// 具体位置的获取，在执行动画的时候需要用到
			GLCellLayout.LayoutParams lp = (GLCellLayout.LayoutParams) folder.getLayoutParams();
			if (lp == null) {
				return false;
			}
			mCellPos[0] = lp.cellX;
			mCellPos[1] = lp.cellY;
			// 在缓存将元素添加到文件夹内
			userFolderInfo.add(itemInfo);
			if (mControler != null) {
				mControler.addItemInfoToFolder(itemInfo, userFolderInfo.mInScreenId);
			}
			// 更新文件夹显示
			mNewFolderIcon.refreshIcon();
			return true;
			//			updateFolderIconAsync(userFolderInfo, false, false);
		} 
		//从添加模块添加go小部件
		else if (dragType == ScreenEditConstants.ADD_WIDGET_ID) {
			
			AbsWidgetInfo info = (AbsWidgetInfo) view.getTag();
			if (info != null) {
				int wRow = info.mRow;
				int mCol = info.mCol;
				ScreenUtils.findVacant(mCellPos, mCol, wRow, mGLWorkspace.getCurrentScreen(),
						mGLWorkspace);
			}
		}
		//从添加模块添加系统小部件
		else if (dragType == ScreenEditConstants.ADD_SYSTEM_WIDGET_ID) {
			SysSubWidgetInfo info = (SysSubWidgetInfo) view.getTag();
			if (info != null) {
				int wRow = info.getCellHeight();
				int mCol = info.getCellWidth();
				ScreenUtils.findVacant(mCellPos, mCol, wRow, mGLWorkspace.getCurrentScreen(),
						mGLWorkspace);
			}
		}
		//从添加模块添加系统快捷方式
		else if (dragType == ScreenEditConstants.ADD_SYSTEM_SHORTCUTS_ID) {
			ScreenUtils.findVacant(mCellPos, 1, 1, mGLWorkspace.getCurrentScreen(), mGLWorkspace);
		}
		// 普通应用程序添加
		else {
			ScreenUtils.findVacant(mCellPos, 1, 1, mGLWorkspace.getCurrentScreen(), mGLWorkspace);
		}
		
		if (addable) {
			// int[] pos = new int[2];
			// ScreenUtils.cellToPoint(mGLWorkspace.getCurrentScreen(), mCellPos[0], mCellPos[1],
			// mGLWorkspace, pos);
			// if (Workspace.getLayoutScale() < 1.0) {
			// float[] realXY = new float[2];
			// Workspace.realPointToVirtual(pos[0], pos[1], realXY);
			// pos[0] = (int) realXY[0];
			// pos[1] = (int) realXY[1];
			// }
			// 用于最后的图标生成（flyIconToScreen）
			// mRectTemp = new Rect(pos[0], pos[1], pos[0] + mCellPos[0], pos[1] + mCellPos[1]);
			// ArrayList<Integer> list = new ArrayList<Integer>();
			// list.add(pos[0]);
			// list.add(pos[1]);
			// 准备拖拽层
			// MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
			// IDiyFrameIds.DRAG_FRAME, null, null);
			// 显示指示器(指示器在下端时继续隐藏不显示)
			// if (!mLayout.isIndicatorOnBottom()) {
			// showIndicator();
			// }
			// add by jiang 添加的时候禁止滑动
			// mGLWorkspace.unTouch();
			// 让图标飞
			// MsgMgrProxy.sendMessage(this, IDiyFrameIds.DRAG_FRAME, IScreenFrameMsgId.START_TO_AUTO_FLY,
			// dragType, object, list);

			// 临时逻辑直接添加到屏幕
			int screenIndex = mGLWorkspace.getCurrentScreen();
			Object tagObject = view.getTag();
			ItemInfo tagInfo = null;
			if (tagObject instanceof ItemInfo) {
				tagInfo = (ItemInfo) tagObject;
			}

			if (tagInfo == null) {
				// return;
			}
			if (tagInfo instanceof ShortCutInfo) {
				Log.i(LogConstants.HEART_TAG, "drag over app");
				mGLWorkspace.blankCellToNormal(mGLWorkspace.getCurrentScreenView(), true);
				// 应用程序图标
				tagInfo.mCellX = mCellPos[0];
				tagInfo.mCellY = mCellPos[1];
				addDesktopItem(screenIndex, tagInfo);
				addDesktopView(tagInfo, screenIndex, true, false);
			} else if (tagObject instanceof AbsWidgetInfo) {
				AbsWidgetInfo info = (AbsWidgetInfo) view.getTag();
				List<Rect> lists = new ArrayList();
				lists.add(new Rect(mCellPos[0], mCellPos[1], info.mRow, info.mCol));
				flyIconToScreen(tagObject, lists);
			} else if (tagObject instanceof SysSubWidgetInfo) {
				SysSubWidgetInfo info = (SysSubWidgetInfo) view.getTag();
				List<Rect> lists = new ArrayList();
				lists.add(new Rect(mCellPos[0], mCellPos[1], info.getCellHeight(), info.getCellWidth()));
				flyIconToScreen(tagObject, lists);
			} 
			//从添加界面添加系统快捷方式，传过来的是ResolveInfo
			else if (tagObject instanceof ResolveInfo) {
				
			}
			mCellPos = null;
		}
		return true;
	}
	/***
	 * 删除文件夹中的一项
	 * 
	 * @param object
	 * @return
	 */
	private boolean screenDeleteItemFromFolder(Object object) {
		if (mNewFolderIcon == null) {
			return false;
		}
		UserFolderInfo currentFolderInfo = ((GLScreenFolderIcon) mNewFolderIcon).getInfo();
		if (currentFolderInfo != null) {
			GLView targetView = (GLView) object;
			ItemInfo tagInfo = (ItemInfo) targetView.getTag();
			Intent srcIntent = ((ShortCutInfo) tagInfo).mIntent;
			ArrayList<ItemInfo> list = currentFolderInfo.getContents();
			for (int i = 0; i < list.size(); i++) {
				Intent desIntent = ((ShortCutInfo) list.get(i)).mIntent;
				if (ConvertUtils.intentCompare(srcIntent, desIntent)) {
					if (null != mControler) {
						ItemInfo deleteinfo = list.get(i);

						currentFolderInfo.remove(deleteinfo);
						if (mControler != null) {
							mControler.moveDesktopItemFromFolder(deleteinfo,
									mGLWorkspace.getCurrentScreen(), currentFolderInfo.mInScreenId);
						}
						post(new Runnable() {
							@Override
							public void run() {
								if (mNewFolderIcon != null) {
									mNewFolderIcon.refreshIcon();
								}
							}
						});
					}
				}
			}
			currentFolderInfo = null;
		}
		return true;
	}

	/***
	 * 屏幕变红一秒钟
	 */
	public void setScreenRedBg() {
		ToastUtils.showToast(R.string.no_more_room, Toast.LENGTH_SHORT);
		mGLWorkspace.resetScreenBg(true);
		postDelayed(new Runnable() {
			@Override
			public void run() {
				mGLWorkspace.resetAllScreenBg();
			}
		}, 1000);
	}

	@Override
	public void updateFolderIconAsync(final UserFolderInfo folderInfo, boolean reload,
			boolean checkDel) {
		if (folderInfo == null) {
			return;
		}
		final GLAppFolderInfo appfolderInfo = GLAppFolderController.getInstance()
				.getFolderInfoById(folderInfo.mInScreenId, GLAppFolderInfo.FOLDER_FROM_SCREEN);
		final GLScreenFolderIcon folderIcon = (GLScreenFolderIcon) GLAppFolder.getInstance()
				.getFolderIcon(appfolderInfo);

		if (reload) {
			folderInfo.mContentsInit = false;
		}

		if (folderIcon != null && mGLBinder != null) {
			mGLBinder.updateFolderIconAsync(folderIcon, checkDel);
		}
	}

	/**
	 * 从数据库重新读取Folder内容
	 * 
	 * @param userFolderInfo
	 * @return
	 */
	public ArrayList<ItemInfo> getFolderContentFromDB(UserFolderInfo userFolderInfo) {
		synchronized (userFolderInfo) {
			/**
			 * ADT-3723 非必现：桌面文件夹消失，有消失动画 步骤：1、创建文件件 2、打开文件夹。拖动图标进行排序 3、点击+号按钮，添加或删除部分程序 4、完成
			 * 原因：换位线程与添加程序进文件夹线程同步问题 修改方法：对userFolderInfo加锁
			 */
			return mControler.getFolderItems(userFolderInfo.mInScreenId);
		}
	}

	/**
	 * 删除屏幕的一个Item，包括UI和数据都移除
	 * 
	 * @param itemInfo
	 * @param screenindex
	 *            屏幕索引，-1可以遍历屏幕
	 * @param delFolderItems
	 *            如果删除的是文件夹 true表示删除文件夹内部的数据 false 表示不删除（比如从桌面拖拽到dock就不需要删除）
	 */
	public synchronized void deleteScreenItem(ItemInfo itemInfo, int screenindex,
			boolean delFolderItems) {
		if (null == itemInfo) {
			return;
		}
		// 删除view
		GLView targetView = ScreenUtils.getViewByItemId(itemInfo.mInScreenId, screenindex,
				mGLWorkspace);
		if (targetView != null) {
			GLViewParent parent = targetView.getGLParent();
			if (parent != null && parent instanceof GLViewGroup) {
				((GLViewGroup) parent).removeView(targetView);
			}
		}

		int type = itemInfo.mItemType;
		if (type == IItemType.ITEM_TYPE_SHORTCUT) {
			mControler.removeDesktopItemInDBAndCache(itemInfo);
		} else {
			mControler.removeDesktopItem(itemInfo);
		}
		if (type == IItemType.ITEM_TYPE_SHORTCUT || type == IItemType.ITEM_TYPE_APP_WIDGET) {
			if (type == IItemType.ITEM_TYPE_SHORTCUT) {
				ScreenUtils.unbindShortcut((ShortCutInfo) itemInfo);
			}

			if (itemInfo instanceof ScreenAppWidgetInfo) {
				int widgetId = ((ScreenAppWidgetInfo) itemInfo).mAppWidgetId;
				if (m3DWidgetManager.isGoWidget(widgetId)) {
					//通知widget本身被移除
					m3DWidgetManager.removeWidget(widgetId);
					m3DWidgetManager.deleteWidget(widgetId);
				} else {
					// 系统widget
					mWidgetHost.deleteAppWidgetId(widgetId);
				}
			}
		} else if (type == IItemType.ITEM_TYPE_USER_FOLDER) {
			if (delFolderItems) {
				// 删除文件夹
				ScreenUtils.unbindeUserFolder((UserFolderInfo) itemInfo);
				GLScreenFolderIcon folderIcon = (GLScreenFolderIcon) targetView;
				if (folderIcon != null) {
					folderIcon.onIconRemoved();
				}
				mControler.removeUserFolder(itemInfo);
			}
		}
	} // end deleteScreenItem
	/**
	 * 删除屏幕的一个Folder，包括UI和数据都移除
	 * 
	 * @param itemInfo
	 * @param screenindex
	 *            屏幕索引，-1可以遍历屏幕
	 * @param delFolderItems
	 *            如果删除的是文件夹 true表示删除文件夹内部的数据 false 表示不删除（比如从桌面拖拽到dock就不需要删除）
	 */
	public synchronized void deleteScreenFolder(ItemInfo itemInfo, int screenindex,
			boolean delFolderItems) {
		if (null == itemInfo) {
			return;
		}
		// 删除view
		GLView targetView = ScreenUtils.getViewByItemId(itemInfo.mInScreenId, screenindex,
				mGLWorkspace);
		if (targetView != null) {
			GLViewParent parent = targetView.getGLParent();
			if (parent != null && parent instanceof GLViewGroup) {
				((GLViewGroup) parent).removeView(targetView);
			}
		}
		if (itemInfo.mItemType == IItemType.ITEM_TYPE_USER_FOLDER) {
			mControler.removeDesktopItem(itemInfo);
			if (delFolderItems) {
				// 删除文件夹
				UserFolderInfo folderInfo = (UserFolderInfo) itemInfo;
				folderInfo.clearContents();
				folderInfo.selfDestruct();
				GLScreenFolderIcon folderIcon = (GLScreenFolderIcon) targetView;
				if (folderIcon != null) {
				    folderIcon.onIconRemoved();
				}
				mControler.removeUserFolder(itemInfo);
			}
		}
	} // end deleteScreenItem
	/**
	 * 屏幕预览
	 */

	public static boolean sForceHide = false;
	private void changeDrawState(boolean showAll) {
		if (showAll) {
			if (mControler.getScreenSettingInfo() != null) {
				mDesktopIndicator.show();
			}

			// 桌面恢复普通状态
			mGLWorkspace.setDrawState(GLWorkspace.DRAW_STATE_ALL);
			//			mGLWorkspace.setVisibility(View.VISIBLE);
		} else {
			clearFocus();
			// mLayout.getIndicator().setVisible(false);
			mDesktopIndicator.hide();
			mGLWorkspace.setDrawState(GLWorkspace.DRAW_STATE_ONLY_BACKGROUND);
			//			mGLWorkspace.setVisibility(View.INVISIBLE);
		}
		mGLWorkspace.postInvalidate();
	}

	// 显示屏幕预览
	private boolean showPreview(boolean fromSetting) {		
		// 如果当前是文件夹打开状态，则不进行屏幕预览的展现
		if (GLAppFolder.getInstance().isFolderOpened()) {
			return false;
		}

		//	屏幕预览,显示横竖屏切换
		ShellContainer.sEnableOrientationControl = false;
		GLOrientationControler.setPreviewModel(true);
		if (GoLauncherActivityProxy.isPortait()) {
			GLOrientationControler.setPreviewOrientationType(OrientationTypes.VERTICAL);
		} else {
			GLOrientationControler.setPreviewOrientationType(OrientationTypes.HORIZONTAL);
		}

		// 进入屏幕预览不画格子_start
		GLCellLayout cellLayout = (GLCellLayout) mGLWorkspace.getChildAt(mGLWorkspace
				.getCurrentScreen());
		cellLayout.clearVisualizeDropLocation(); //清除轮廓信息
		cellLayout.setmDrawStatus(GLCellLayout.DRAW_STATUS_NORMAL);	//普通状态
		cellLayout.setDrawCross(false); //不画十字
		// 进入屏幕预览不画格子_end
		if (mShell.isViewVisible(IViewId.SCREEN_PREVIEW)) {
			return false;
		}
		//		mShell.show(IViewId.SCREEN_PREVIEW, true);
		mShell.showStage(IShell.STAGE_PREVIEW, true);
		// 展示预览
		sIsShowPreview = true;

		mGLWorkspace.clearFocus();
		hidePopupWindow(true); // 取消快捷菜单
		int param = isLoading() ? GLSense.SCREEN_LOADING : GLSense.SCREEN_LOADED;
		if (fromSetting) {
			param = GLSense.FROM_SETTING | param;
		}
		int screenCount = mGLWorkspace.getChildCount();

		GLScreenPreviewMsgBean bean = new GLScreenPreviewMsgBean();
		bean.currentScreenId = mGLWorkspace.getCurrentScreen();
		bean.mainScreenId = mGLWorkspace.getMainScreen();
		for (int i = 0; i < screenCount; i++) {
			GLScreenPreviewMsgBean.PreviewImg image = new GLScreenPreviewMsgBean.PreviewImg();
			image.previewView = mGLWorkspace.getChildAt(i);
			//			((GLCellLayout) image.previewView).setChildrenDrawnWithCacheEnabled(false);
			//			((GLCellLayout) image.previewView).destroyChildrenDrawingCache();
			image.screenId = i;
			image.canDelete = !mGLWorkspace.hasChildElement(i);
			bean.screenPreviewList.add(image);
		}
		//
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW,
				IScreenPreviewMsgId.PREVIEW_INIT, 1, bean);

		try {
			SecurityPoxyFactory.getSecurityPoxy().clearSecurityResult(); // 清除安全的标识
		} catch (UnsupportSecurityPoxyException ex) {
		}
		
		handleMessage(this, IScreenFrameMsgId.SCREEN_HIDE_CURRENT_GUIDE, 0);
		return fromSetting;
	}

	private void addCellLayout() {

		GLCellLayout screen = new GLCellLayout(mContext);
		screen.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		// screen.setNeedToTryCatch(mNeedToCatch);
		mGLWorkspace.addScreen(screen, mGLWorkspace.getChildCount());
		StatisticsData.sSCREEN_COUNT = mGLWorkspace.getChildCount();
		//		 添加新的一个屏，屏幕数发生变化，发广播给多屏多壁纸
		mGLWorkspace.sendBroadcastToMultipleWallpaper(false, true);
	}

	/**
	 * 添加带有“+”号的空白屏幕
	 */
	private void addBlankCellLayout() {
		GLCellLayout blankScreen = new GLCellLayout(mContext);
		blankScreen.setBlank(GLCellLayout.STATE_BLANK_CONTENT);
		mGLWorkspace.addScreen(blankScreen, mGLWorkspace.getChildCount());
		// 添加了一个空白屏，屏幕数发生变化，发广播给多屏多壁纸
		mGLWorkspace.sendBroadcastToMultipleWallpaper(true, true);
	}

	/**
	 * 当把位置为src的项移动到dst位置时，计算原来在cur位置的项的新位置
	 * 
	 * @return
	 */

	private void moveScreen(Bundle bundle) {
		final int srcScreenIndex = bundle.getInt(GLSense.FIELD_SRC_SCREEN);
		final int destScreenIndex = bundle.getInt(GLSense.FIELD_DEST_SCREEN);
		final int currentScreen = mPreCurrentScreen;
		final int homeScreen = mPreHomeScreen;
		//		Log.i("jiang", "srcScreenIndex: " + srcScreenIndex + " destScreenIndex: " + destScreenIndex
		//				+ " currentScreen: " + currentScreen + " homeScreen: " + homeScreen);
		final int newCurrentScreen = ScreenUtils.computeIndex(currentScreen, srcScreenIndex,
				destScreenIndex);
		mPreNewCurrentScreen = newCurrentScreen;
		final int newHomeScreen = ScreenUtils.computeIndex(homeScreen, srcScreenIndex,
				destScreenIndex);

		// 如果目标位置位于目的位置之后，索引需-1
		GLCellLayout cellLayout = (GLCellLayout) mGLWorkspace.getChildAt(srcScreenIndex);
		if (cellLayout == null) {
			return;
		}
		mGLWorkspace.removeView(cellLayout);
		int realDestIndex = destScreenIndex;
		if (destScreenIndex >= mGLWorkspace.getChildCount()) {
			// 添加到最后
			realDestIndex = -1;
		}
		mGLWorkspace.addView(cellLayout, realDestIndex);

		// 做数据库操作
		if (null != mControler) {
			mControler.moveScreen(srcScreenIndex, destScreenIndex);
		}

		if (newCurrentScreen != currentScreen) {
			// 防止由于背景切换速度慢而造成的索引不匹配的情况
			// mGLWorkspace.setCurrentScreen(newCurrentScreen);
		}

		if (newHomeScreen != homeScreen) {
			mGLWorkspace.setMainScreen(newHomeScreen);
			ScreenSettingInfo screenSettingInfo = mControler.getScreenSettingInfo();
			if (mControler != null && screenSettingInfo != null
					&& screenSettingInfo.mMainScreen != newHomeScreen) {
				screenSettingInfo.mMainScreen = newHomeScreen;
				SettingProxy.updateScreenSettingInfo2(screenSettingInfo, false);
			}
		}
	}

	/**
	 * @param screen
	 * @param noElastic
	 *            是否使用弹性效果
	 * @param duration
	 *            小于0则自动计算时间
	 */
	private void turnToScreen(int screen, boolean noElastic, int duration) {
		// 没有编辑widget才可跳转屏幕
		if (mGLWorkspace != null && !mIsWidgetEditMode) {
			ScreenSettingInfo screenSetInfo = SettingProxy.getScreenSettingInfo();
			if (screenSetInfo.mScreenLooping) {
				if (screen < 0) {
					screen = mGLWorkspace.getChildCount() - 1;
				} else if (screen >= mGLWorkspace.getChildCount()) {
					screen = 0;
				}
			} else {
				if (screen < 0) {
					screen = 0;
				} else if (screen >= mGLWorkspace.getChildCount()) {
					screen = mGLWorkspace.getChildCount() - 1;
				}
			}
//			mGLWorkspace.snapToScreen(screen, noElastic, duration);
			mGLWorkspace.setCurrentScreen(screen);
		}

	}

	private void startListening() {
		try {
			mWidgetHost.startListening();
			registGoWidgetAction();
		} catch (Throwable e) {
			// 保护Widget可能导致的异常，如内存溢出
		}
	}

	private void stopListening() {
		try {
			mWidgetHost.stopListening();
			unRegistGoWidgetAction();
		} catch (Throwable e) {
			// 保护Widget可能导致的异常，如内存溢出
		}
	}

	private void registGoWidgetAction() {
		mGoWidgetActionReceiver = new GoWidgetActionReceiver();
		IntentFilter filter = new IntentFilter(GoWidgetConstant.ACTION_CONFIG_FINISH);
		filter.addAction(GoWidgetConstant.ACTION_REQUEST_FOCUS);
		filter.addAction(ICustomAction.ACTION_CONFIG_FINISH); // 2D Widget的配置action
		filter.addAction(GoWidgetConstant.ACTION_GOTO_GOWIDGET_FRAME);
		// filter.addAction(GoWidgetConstant.ACTION_ON_OFF_RECEIVER_CAN_UNINSTALL);
		ShellAdmin.sShellManager.getActivity().registerReceiver(mGoWidgetActionReceiver, filter);
	}

	private void unRegistGoWidgetAction() {
		if (mGoWidgetActionReceiver != null) {
			ShellAdmin.sShellManager.getActivity().unregisterReceiver(mGoWidgetActionReceiver);
			mGoWidgetActionReceiver = null;
		}
	}

	@Override
	public boolean onSwipe(PointInfo p, float dx, float dy) {
		return mGLWorkspace.onSwipe(p, dx, dy);
	}

	@Override
	public boolean onTwoFingerSwipe(PointInfo p, float dx, float dy, int direction) {
		if (mIsWidgetEditMode) {
			mGLWorkspace.mTwoFingerSwipeHandled = true;
			return true;
		}
		return mGLWorkspace.onTwoFingerSwipe(p, dx, dy, direction);
	}

	@Override
	public boolean onScale(PointInfo p, float scale, float scaleX, float scaleY, float dx,
			float dy, float angle) {
		if (mIsWidgetEditMode) {
			mGLWorkspace.mOnScaleHandled = true;
			return true;
		}
		return mGLWorkspace.onScale(p, scale, scaleX, scaleY, dx, dy, angle);
	}

	@Override
	public boolean onDoubleTap(PointInfo p) {
		if (mGLWorkspace.getZeroHandler().canDoubleTap()) {
			return mGLWorkspace.onDoubleTap(p);
		}
		return false;
	}

	private boolean addDeskUserFolder(Object object) {
		boolean ret = false;
		// 添加一个新的文件夹
		if (object == null || !(object instanceof UserFolderInfo)) {
			return ret;
		}
		int[] xy = new int[2];
		final int curScreen = mGLWorkspace.getCurrentScreen();
		final boolean vacant = ScreenUtils.findVacant(xy, 1, 1, curScreen, mGLWorkspace);
		if (!vacant) {
			xy = null;
			setScreenRedBg();
		} else {
			GLCellLayout layout = mGLWorkspace.getCurrentScreenView();
			if (layout != null) {
				mGLWorkspace.blankCellToNormal(layout, true);
				UserFolderInfo userFolderInfo = (UserFolderInfo) object;
				userFolderInfo.mCellX = xy[0];
				userFolderInfo.mCellY = xy[1];
				mControler.addDesktopItem(mGLWorkspace.getCurrentScreen(), userFolderInfo);
				// mNewFolderId = userFolderInfo.mInScreenId;
				final GLView[] newViews = new GLView[1];
				handleMessage(this, IScreenFrameMsgId.SCREEN_CREATE_ITEM, curScreen, userFolderInfo,
						newViews);
				if (newViews[0] != null && newViews[0] instanceof GLScreenFolderIcon) {
					mNewFolderIcon = (GLScreenFolderIcon) newViews[0];
					mGLWorkspace.addInScreen(mNewFolderIcon, curScreen, xy[0], xy[1], 1, 1);
					inNewFolderState();
				}
				return ret;
			}
		}
		return ret;
	}

	@Override
	public void onFolderOpenEnd(int curStatus) {
		mGLWorkspace.onFolderOpenEnd(curStatus);
	}

	@Override
	public void onFolderCloseEnd(int curStatus, BaseFolderIcon<?> baseFolderIcon, boolean needReopen) {
		mGLWorkspace.onFolderCloseEnd(curStatus, baseFolderIcon, needReopen);
	}

	@Override
	public void onFolderOpen(BaseFolderIcon<?> baseFolderIcon, boolean animate, int curStatus,
			boolean reopen) {
		mGLWorkspace.notifyWidgetVisible(false);
		mGLWorkspace.onFolderOpen(baseFolderIcon, animate, curStatus, reopen);
		GaussianBlurEffectUtils.enableBlurWithZoomOutAnimation(this, null);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK, IDockMsgId.DOCK_ENABLE_BLUR, -1);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_HIDE_CURRENT_GUIDE, 0);
	}

	@Override
	public void onFolderClose(BaseFolderIcon<?> baseFolderIcon, boolean animate, int curStatus) {
		if (animate) {
			GaussianBlurEffectUtils.disableBlurWithZoomInAnimation(this,
					new AnimationListenerAdapter() {
						@Override
						public void onAnimationEnd(Animation animation) {
							mGLWorkspace.notifyWidgetVisible(true);
						}
					});
		} else {
			GaussianBlurEffectUtils.disableBlurWithoutAnimation(this);
		}
		mGLWorkspace.onFolderClose(baseFolderIcon, animate, curStatus);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK, IDockMsgId.DOCK_DISABLE_BLUR, animate
				? 1
				: 0);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_RESHOW_LAST_GUIDE, 0);
	}

	@Override
	public void onFolderStatusChange(int oldStatus, int newStatus) {
		mGLWorkspace.onFolderStatusChange(oldStatus, newStatus);
	}


	@Override
	public void onFolderReLayout(BaseFolderIcon<?> baseFolderIcon, int curStatus) {
		mGLWorkspace.onFolderReLayout(baseFolderIcon, curStatus);
	}

	/***
	 * 添加页面，开始添加新文件夹
	 */
	private synchronized void inNewFolderState() {
		if (mGLWorkspace != null) {
			mGLWorkspace.lock();
			//			mGLWorkspace.mInNewFolderState = true ;
//			if (mGLWorkspace.isAllowToScroll()) {
//				mGLWorkspace.disallowToScroll();
//			}

			// 把相邻的两侧的卡片也设成半透明
			int index = mGLWorkspace.getCurrentScreen();
			if (index - 1 >= 0) {
				GLCellLayout leftCellLayout = mGLWorkspace.getScreenView(index - 1);
				if (null != leftCellLayout) {
					leftCellLayout.setCoverWithBg(true);
				}
			}
			if (index + 1 < mGLWorkspace.getChildCount()) {
				GLCellLayout rightCellLayout = mGLWorkspace.getScreenView(index + 1);
				if (null != rightCellLayout) {
					rightCellLayout.setCoverWithBg(true);
				}
			}
			
		}

		if (mDesktopIndicator != null) {
			mDesktopIndicator.setTouchable(false);
		}
	} // end inNewFolderState

	/***
	 * 添加页面，结束添加新文件夹
	 */
	private synchronized void leaveNewFolderState() {
		if (mDesktopIndicator != null) {
			mDesktopIndicator.setTouchable(true);
		}

		if (mGLWorkspace != null) {
//			if (mGLWorkspace.isLock()) {
//				mGLWorkspace.allowToScroll();
//			}
			mGLWorkspace.unlock();
			// 如果文件夹什么都没添加则将其移除
			if (mNewFolderIcon != null) {
				UserFolderInfo info = mNewFolderIcon.getInfo();
				if (info != null && info.getContents().size() < 1) {
					deleteScreenItem(info, info.mScreenIndex, true);
					mNewFolderIcon.cleanup();
				}
			}
			for (int i = 0; i < mGLWorkspace.getChildCount(); i++) {
				GLCellLayout cellLayout = mGLWorkspace.getScreenView(i);
				if (null != cellLayout) {
					cellLayout.setCoverWithBg(false);
				}
			}
		}

		mNewFolderIcon = null;
	} // end leaveNewFolderState

	public void setBackWorkspace(GLBackWorkspace backWorkspace) {
		mBackWorkspace = backWorkspace;
		if (mGLWorkspace != null) {
			mGLWorkspace.setWallpaperDrawer(mBackWorkspace);
		}
	}

	// 通知罩子层显示主题2.0界面
	private void startTheme2MaskView() {
		ThemeInfoBean infoBean = ThemeManager.getInstance(ApplicationProxy.getContext())
				.getCurThemeInfoBean();
		if (infoBean != null) {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					ICommonMsgId.EVENT_INIT_THEME_LAUNCHER_PROXY, -1, null, null);
			if (infoBean.isMaskView()) {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						ICoverFrameMsgId.COVER_FRAME_ADD_VIEW, CoverFrame.COVER_VIEW_THEME,
						infoBean.getPackageName(), null);
			}
			ThemeInfoBean.MiddleViewBean middleViewBean = infoBean.getMiddleViewBean();
			if (middleViewBean != null) {
				if (middleViewBean.mHasMiddleView) {
					mBackWorkspace.setMiddleView(infoBean.getPackageName(),
							middleViewBean.mIsSurfaceView);
				}
			} else {
				mBackWorkspace.removeMiddleView();
			}
		}
	}

	//	public void startIndicatorAnimation(AnimationTask task, int level, final boolean isEnter) {
	//		if (mDesktopIndicator == null || mDesktopIndicator.getVisibility() == GLView.INVISIBLE) {
	//			return;
	//		}
	//		mIsIgnoreLayout = true;
	//		if (sIndicatorOnBottom) { // 在下方才进行动画
	//			float scale = 1.0f;
	//			if (level == GLWorkspace.SCREEN_TO_SMALL_LEVEL_ONE || level == GLWorkspace.SCREEN_BACK_TO_LAST_LEVEL) { // 第一级
	//				scale = GLWorkspace.SCALE_FACTOR_FOR_EDIT_PORTRAIT;
	//			} else if (level == GLWorkspace.SCREEN_TO_SMALL_LEVEL_TWO) { // App第二级
	//				scale = GLWorkspace.SCALE_FACTOR_FOR_ADD_APP_PORTRAIT;
	//			} else if (level == GLWorkspace.SCREEN_TO_SMALL_LEVEL_TWO_FOR_WIDGET) { // Widget第二级
	//				scale = GLWorkspace.SCALE_FACTOR_FOR_ADD_GOWIDGET_PORTRAIT;
	//			}
	//			Rect rect = calculationIndicatorPosition(scale);
	////			AnimationTask task = new AnimationTask(true, AnimationTask.PARALLEL);
	//			int offsetY = rect.top - mDesktopIndicator.getTop();
	//			Animation moveAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, offsetY);
	//			moveAnimation.setDuration(350);
	////			moveAnimation.setFillEnabled(true);
	//			moveAnimation.setFillAfter(true);
	//				
	//			task.addAnimation(mDesktopIndicator, moveAnimation, new AnimationListener() {
	//				@Override
	//				public void onAnimationStart(Animation animation) {
	//				}
	//				
	//				@Override
	//				public void onAnimationRepeat(Animation animation) {
	//				}
	//				
	//				@Override
	//				public void onAnimationEnd(Animation animation) {
	//					requestLayout();
	//				}
	//			});
	//			GLAnimationManager.startAnimation(task);
	//		}
	//	}

	public void notifyAnimationState(boolean isStart, boolean isEnterAnimation) {
		if (mDesktopIndicator == null) {
			return;
		}
		if (sIndicatorOnBottom) { // 指示器在下方
			if (!isStart && !isEnterAnimation) { // 需要等到layout完成之后才显示，否则会出现指示器跳动
				mIsNextLayoutShowIndicator = true;
				return;
			}
			if (isStart && mDesktopIndicator.getVisibility() == GLView.VISIBLE || mDesktopIndicator.isAutoHide()) {
				mDesktopIndicator.setVisibility(GLView.INVISIBLE);
			} else if (!isStart && mDesktopIndicator.getVisibility() == GLView.INVISIBLE) {
				mDesktopIndicator.setVisibility(GLView.VISIBLE);
			}
		} 
	}

	@Override
	public void hideBgAnimation(int type, GLView view, Object...params) {
		mGLWorkspace.hideBgAnimation(type, view, params);
		mDesktopIndicator.setAlpha(0);
	}

	@Override
	public void showBgAnimation(int type, GLView view, Object...params) {
		mGLWorkspace.showBgAnimation(type, view, params);
		mDesktopIndicator.setAlpha(255);
	}

	@Override
	public void showBgWithoutAnimation(int type, GLView view, Object...params) {
		mGLWorkspace.showBgWithoutAnimation(type, view, params);
		mDesktopIndicator.setAlpha(255);
	}

	private void handleScreenFolderDragEvent(ItemInfo shortCutInfo,
			final GLScreenFolderIcon folderIcon, boolean isSuccess,
			final boolean isAlwaysColseFolder) {
		if (isSuccess) {
			final UserFolderInfo userFolderInfo = folderIcon.getInfo();
			if (userFolderInfo.getContents().size() <= 2) {
				if (userFolderInfo.getContents().size() == 1) {
					folderIcon.closeFolder(true);
					boolean ret = false;
					if (mControler != null) {
						ret = mControler.removeUserFolder(userFolderInfo);
					}
					
					if (ret) {
						ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
								Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
						scaleAnimation.setDuration(300);
						folderIcon.startAnimation(scaleAnimation);
						deleteScreenItem(userFolderInfo, userFolderInfo.mScreenIndex, true);
					}
				} else {
					folderIcon.closeFolder(true);
					if (mControler != null) {
						userFolderInfo.remove(shortCutInfo.mInScreenId);
						ShortCutInfo info = userFolderInfo.getChildInfo(0);
						info.mCellX = userFolderInfo.mCellX;
						info.mCellY = userFolderInfo.mCellY;
						int index = userFolderInfo.mScreenIndex;
						GLView addView = addDesktopView(info, index, true, false);
						mControler.addDesktopItem(info.mScreenIndex, info);
						deleteScreenFolder(userFolderInfo, userFolderInfo.mScreenIndex, true);
						mControler.removeUserFolder(userFolderInfo);
						folderIcon.cleanup();

						Message msg = new Message();
						msg.what = ADD_ITEM_FROM_FOLDER_ANIMATION;
						msg.obj = addView;
						mHandler.sendMessage(msg);
					}
				}
			} else {
				if (isAlwaysColseFolder) {
					folderIcon.closeFolder(true);
				}
				userFolderInfo.remove(shortCutInfo);
				if (mControler != null) {
					mControler.moveDesktopItemFromFolder(shortCutInfo, 0,
							userFolderInfo.mInScreenId);
				}
			}
		}
		
		if (!isAlwaysColseFolder && GLAppFolder.getInstance().isFolderOpened()
				&& !GLAppFolder.getInstance().isFolderClosing()) {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_FOLDER,
					IFolderMsgId.FOLDER_KEEP_OPEN, -1, true);
		}
		
		if (folderIcon != null && !folderIcon.isRecyled()) {
			post(new Runnable() {
				@Override
				public void run() {
					if (folderIcon.isIconEnterAnimateFinish()) {
						folderIcon.refreshIcon();
					}
				}
			});
		}
	}

	@Override
	public boolean onDragFling(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, DropAnimationInfo resetInfo, int direction,
			int velocityX, int velocityY) {
		return mGLWorkspace.onDragFling(source, x, y, xOffset, yOffset, dragView, dragInfo,
				resetInfo, direction, velocityX, velocityY);
	}
	@Override
	public boolean onDragTwoFingerSwipe(DragSource source, PointInfo p, float dx, float dy,
			int direction) {
		return mGLWorkspace.onDragTwoFingerSwipe(source, p, dx, dy, direction);
	}

	@Override
	public boolean onDragScale(DragSource source, PointInfo p, float scale, float scaleX,
			float scaleY, float dx, float dy, float angle) {
		return mGLWorkspace.onDragScale(source, p, scale, scaleX, scaleY, dx, dy, angle);
	}

	@Override
	public boolean onDragMultiTouchEvent(DragSource source, MotionEvent ev) {
		return mGLWorkspace.onDragMultiTouchEvent(source, ev);
	}

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		mGLWorkspace.cancelLongPress();
	}
	
	private void showGGMenu(boolean animate) {
		PopupWindowControler popupWindowControler = mShell.getPopupWindowControler();
		popupWindowControler.showGGMenu(animate);
	}
	
	public Search findSearchWidgetOnCurrentScreen() {
		return ScreenUtils.findSearchOnCurrentScreen(mGLWorkspace);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				mGLWorkspace.mTouchedIndicator = false;
				touchChildren((int) ev.getX(), (int) ev.getY());
				break;
			default :
				break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	public void touchChildren(int x, int y) {
		Rect rect = new Rect();
		if (mDesktopIndicator != null) {
			mDesktopIndicator.getHitRect(rect);
			if (rect.contains(x, y)) {
				mGLWorkspace.mTouchedIndicator = true;
			}
		}
	}

	@Override
	public boolean onSearchKeyDown(int keyCode, KeyEvent event) {
		return onKeyDown(keyCode, event);
	}

	@Override
	public boolean onSearchKeyUp(int keyCode, KeyEvent event) {
		return onKeyUp(keyCode, event);
	}

	@Override
	public boolean onSearchKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		return onKeyMultiple(keyCode, repeatCount, event);
	}

	@Override
	public boolean showSearchDialog(String initialQuery, boolean selectInitialQuery,
			Bundle appSearchData, boolean globalSearch) {
		Bundle bundle = new Bundle();
		if (initialQuery == null) {
			initialQuery = getTypedText();
			clearTypedText();
		}
		bundle.putString(Search.FIELD_INITIAL_QUERY, initialQuery);
		bundle.putBoolean(Search.FIELD_SELECT_INITIAL_QUERY, selectInitialQuery);
		bundle.putBundle(Search.FIELD_SEARCH_DATA, appSearchData);
		bundle.putBoolean(Search.FIELD_GLOBAL_SEARCH, globalSearch);
		return MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
				ICommonMsgId.SHOW_SEARCH_DIALOG, -1, bundle, null);
	}
	
	public String getTypedText() {
		return mDefaultKeySsb.toString();
	}

	private void clearTypedText() {
		mDefaultKeySsb.clear();
		mDefaultKeySsb.clearSpans();
		Selection.setSelection(mDefaultKeySsb, 0);
	}
	
	private void addSearchWidget() {
		LayoutInflater inflater = LayoutInflater.from(ShellAdmin.sShellManager
				.getActivity());
		Search search = (Search) inflater.inflate(com.gau.go.launcherex.R.layout.widget_search, null);
		search.setSearchEventListener(this);
		ScreenAppWidgetInfo searchWidgetInfo = new ScreenAppWidgetInfo(
				ICustomWidgetIds.SEARCH_WIDGET);
		searchWidgetInfo.mHostView = search;

		// 固定长宽
		final int spanX = 4;
		final int spanY = 1;
		searchWidgetInfo.mSpanX = spanX;
		searchWidgetInfo.mSpanY = spanY;

		int[] xy = new int[2];
		boolean vacant = ScreenUtils.findVacant(xy, spanX, spanY, mGLWorkspace.getCurrentScreen(),
				mGLWorkspace);
		if (!vacant) {
			setScreenRedBg();
		} else {
			searchWidgetInfo.mCellX = xy[0];
			searchWidgetInfo.mCellY = xy[1];
			search.setTag(searchWidgetInfo);
			GLView glWidget = new GLWidgetContainer(mContext, new GLWidgetView(mContext,
					searchWidgetInfo.mHostView));
			glWidget.setTag(searchWidgetInfo);
			mGLWorkspace.addInCurrentScreen(glWidget, xy[0], xy[1], spanX, spanY);
			addDesktopItem(mGLWorkspace.getCurrentScreen(), searchWidgetInfo);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void updateItems(ArrayList<ItemInfo> list) {
		if (list == null || mGLWorkspace == null) {
			return;
		}
		for (ItemInfo itemInfo : list) {
			if (itemInfo != null) {
				GLView targetView = ScreenUtils.getViewByItemId(itemInfo.mInScreenId, -1,
						mGLWorkspace);
				if (targetView != null && itemInfo != null && targetView instanceof IconView) {
					((IconView) targetView).refreshIcon();
				}
			}
		}
	}
	
	/**
	 * 重新加载一次中间层
	 * 2013-8-13 by Yugi
	 */
	private void reLoadMiddleView() {
		if (mBackWorkspace != null) {
			startTheme2MaskView();
		}
	}
	


	private boolean isTheRightTimeToAdd() {
		// 系统时间
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dstDateTime = formatter.format(currentTime);
		return isInTime(dstDateTime.toString(), DoubleElevenDefaultData.START_DATE_ONE, DoubleElevenDefaultData.END_DATE_ONE)
				|| isInTime(dstDateTime.toString(), DoubleElevenDefaultData.START_DATE_TWO,
						DoubleElevenDefaultData.END_DATE_TWO);
	}

	// 双11图标
//	public void addShortcutForEleven(Activity activity) {
//
//		if (!isTheRightTimeToAdd()) {
//			return;
//		}
//		
//		PreferencesManager sharedPreferences = new PreferencesManager(activity);
//		boolean sHasPutIcon = sharedPreferences.getBoolean(
//				IPreferencesIds.DOUBLE_ELEVEN_IS_ADD_SHORTCUT, false);
//		if (sHasPutIcon) {
//			return;
//		}
//		Intent shortcut = new Intent(
//				"com.android.launcher.action.INSTALL_SHORTCUT");
//		// 快捷方式的名称
//		// String name = activity.getResources().getString(
//		// R.string.gomarket_appcenter_title);
//		String name = DoubleElevenDefaultData.DOUBLE_ICON_NAME;
//		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
//		// 不允许重复创建
//		shortcut.putExtra("duplicate", false);
//
//		// 指定当前的Activity为快捷方式启动的对象: 如 com.everest.video.VideoPlayer
//		// 这里必须为Intent设置一个action，可以任意(但安装和卸载时该参数必须一致)
//		Uri uri = Uri.parse(PromotionUtil.getStatisticUrl(DoubleElevenDefaultData.DOUBEL_ELEVEN_URL));
//		Intent respondIntent = new Intent(Intent.ACTION_VIEW, uri);
//		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, respondIntent);
//
//		// 快捷方式的图标
//		ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(
//				activity, R.drawable.gl_double11_screenshortcut1);
//		shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
//
//		activity.sendBroadcast(shortcut);
//		activity.setResult(Activity.RESULT_OK, shortcut);
//		sharedPreferences.putBoolean(
//				IPreferencesIds.DOUBLE_ELEVEN_IS_ADD_SHORTCUT, true);
//		sharedPreferences.commit();
//	}
	
	@Override
	protected void onVisibilityChanged(GLView changedView, int visibility) {
		//fixme: requestLayout();
		if (changedView == this) {
			if (visibility == VISIBLE) {
				if (mNeedRequestLayout) {
					requestLayout();
					mNeedRequestLayout = false;
				}
				if (mGLWorkspace.mScaleState == GLWorkspace.STATE_NORMAL && mShell != null
						&& !mShell.isViewVisible(IViewId.SCREEN_PREVIEW)) {
					handleMessage(this, IScreenFrameMsgId.SCREEN_RESHOW_LAST_GUIDE, 0);
				}
				mGLWorkspace.notifyWidgetVisible(true);
			} else {
				handleMessage(this, IScreenFrameMsgId.SCREEN_HIDE_CURRENT_GUIDE, 0);
				mGLWorkspace.notifyWidgetVisible(false);
			}
		}
	}

	public boolean showZeroScreen(Object object) {
		boolean ref = true;
		setScreenIndicator(DeskSettingUtils.getIsShowZeroScreen()); //设置0屏指示器的图标
		if (mGLSuperWorkspace != null) {
			mGLSuperWorkspace.setIsShowZeroScreen(object);
		}
		return ref;
	}
	
	/**
	 * <br>功能简述:设置指示器的图标
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void setScreenIndicator(boolean isShowZeroScreen) {
		//更新指示器
		if (mDesktopIndicator != null) {
			//判断是否有0屏
			if (isShowZeroScreen) {
				mDesktopIndicator.addCustomDotImage(0, mContext.getResources().getDrawable(R.drawable.gl_zero_screen_indicator_light)
						, mContext.getResources().getDrawable(R.drawable.gl_zero_screen_indicator_normal));
				mDesktopIndicator.setTotal(mGLWorkspace.getChildCount() + 1);
				mDesktopIndicator.setCurrent(mGLWorkspace.getCurrentScreen() + 1);
			} else {
				mDesktopIndicator.clearCustomDotImage();
				mDesktopIndicator.setTotal(mGLWorkspace.getChildCount());
				mDesktopIndicator.setCurrent(mGLWorkspace.getCurrentScreen());
			}
			mDesktopIndicator.updateContent();
		}
	}
	
	private static final int DEFERRED_MSG_ENTER_EDIT_MODE = 0x111;
	
	private UiDeferredHandler mUiDefHandler = new UiDeferredHandler();
	/**
	 * 
	 * <br>
	 * 类描述: <br>
	 * 功能详细描述: 3D模式下的UI空闲handler.
	 * 
	 * @author
	 * @date [2012-10-18]
	 */
	private class UiDeferredHandler extends DeferredHandler {
		@Override
		public void handleIdleMessage(Message msg) {
			switch (msg.what) {
				case DEFERRED_MSG_ENTER_EDIT_MODE :
					//长按桌面进入屏幕添加，屏幕添加需要先切换到竖屏模式，然后再进行workspace的缩放，否则缩放的比例会不准确
					//因此缩放的动作放在DeferredHandler,确保其动作在屏幕旋转完成后再进行
					//横屏下切换到竖屏进入添加界面
					if (mGLWorkspace != null
							&& mGLWorkspace.mSmallLeveInLandscape == GLWorkspace.SCREEN_TO_SMALL_LEVEL_ONE) {
						mGLWorkspace.normalToSmall(mCurTabId, null,
								GLWorkspace.SCREEN_TO_SMALL_LEVEL_ONE);
						mGLWorkspace.setCellLayoutGridState(mGLWorkspace.getCurrentScreen(), true,
								-1);
						mCurTabId = 0;

					} else if (mGLWorkspace != null
							&& mGLWorkspace.mSmallLeveInLandscape == GLWorkspace.SCREEN_TO_SMALL_LEVEL_TWO) {

						if (mGLWorkspace.mSmallLeveObj instanceof ScreenEditItemInfo) {

							mGLWorkspace.normalToSmallPreAction(
									ScreenEditConstants.TAB_ID_SUB_SYSTEMWIDGET,
									mGLWorkspace.mSmallLeveObj,
									GLWorkspace.SCREEN_TO_SMALL_LEVEL_TWO);

						} else if (mGLWorkspace.mSmallLeveObj instanceof GoWidgetProviderInfo) {

							mGLWorkspace.normalToSmallPreAction(
									ScreenEditConstants.TAB_ID_SUB_GOWIDGET,
									mGLWorkspace.mSmallLeveObj,
									GLWorkspace.SCREEN_TO_SMALL_LEVEL_TWO);
						}

						mCurTabId = 0;
					}

					break;
				default :
					break;
			}
		}
	}
	
	private void addScreenView(ItemInfo info, int type) {
		switch (type) {
			case ScreenAdvertBusiness.ADD_GO_WIDGET_BY_FAV_INFO :
				if (info instanceof FavoriteInfo) {
					GoWidgetBaseInfo widgetBaseInfo = ((FavoriteInfo) info).mWidgetInfo;
					// 添加新视图
					AppCore.getInstance().getGoWidgetManager().addGoWidget(widgetBaseInfo);
					ScreenAppWidgetInfo widgetInfo = new ScreenAppWidgetInfo(
							widgetBaseInfo.mWidgetId, null, info);
					GLView widgetView = filterWidgetView(widgetInfo);
					addScreenView(info, widgetView);
					m3DWidgetManager.startWidget(widgetBaseInfo.mWidgetId, null);
				}
				break;
		}
	}
	
	private void addScreenView(ItemInfo info, GLView view) {
		mGLWorkspace.addInScreen(view, info.mScreenIndex, info.mCellX, info.mCellY,
				info.mSpanX, info.mSpanY, false);
		mControler.addDesktopItem(info.mScreenIndex, info);
	}

	@Override
	public void onBCChange(int msgId, int param, Object... objects) {
		switch (msgId) {
			case IAppCoreMsgId.APPCORE_DATACHANGE : {
				handleAppCoreChange(param, objects[0]);
				break;
			}
			default :
				break;
		}
	}
	
	private void showExtendFuncView(int viewId) {
		mGLWorkspace.notifyWidgetVisible(false);
		GaussianBlurEffectUtils.enableBlurWithZoomOutAnimation(this, null);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK, IDockMsgId.DOCK_ENABLE_BLUR, -1);
		showTargetView(viewId);
	}
	
	private void hideExtendFuncView(int viewId) {
		GaussianBlurEffectUtils.disableBlurWithZoomInAnimation(this, new AnimationListenerAdapter() {
			@Override
			public void onAnimationEnd(Animation animation) {
				mGLWorkspace.notifyWidgetVisible(true);
			}
		});
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK, IDockMsgId.DOCK_DISABLE_BLUR, 1);
		mShell.remove(viewId, true);
	}

	protected void showTargetView(int viewId) {
		GLAbsExtendFuncView view = null;
		switch (viewId) {
			case IViewId.PRO_MANAGE :
				view = new GLProManageContainer(mContext);
				break;
			case IViewId.RECENT_APP :
				view = new GLRecentAppContainer(mContext);
				break;
			default :
				break;
		}
		view.setExtendFuncViewEventListener(this);
		mShell.show(view, true);
	}

	@Override
	public void extendFuncViewPreEnter(IView view) {
		mExtendFuncView = view;
	}

	@Override
	public void extendFuncViewOnEnter(IView view) {
	}

	@Override
	public void extendFuncViewPreExit(IView view) {
		
	}

	@Override
	public void extendFuncViewOnExit(IView view) {
		mExtendFuncView = null;
	}
}
