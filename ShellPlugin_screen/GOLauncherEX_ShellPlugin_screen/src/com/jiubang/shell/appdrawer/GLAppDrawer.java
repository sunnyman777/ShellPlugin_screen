package com.jiubang.shell.appdrawer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.GoLauncherLogicProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.util.BroadCaster.BroadCasterObserver;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.ImageFilter;
import com.go.util.graphics.ImageUtil;
import com.golauncher.message.IAppCoreMsgId;
import com.golauncher.message.IAppDrawerMsgId;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IFolderMsgId;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.IBackgroundInfoChangedObserver;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.DataType;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.IShell;
import com.jiubang.shell.IView;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.appdrawer.animation.AnimationFactory;
import com.jiubang.shell.appdrawer.component.GLAbsExtendFuncView;
import com.jiubang.shell.appdrawer.component.GLGridViewContainer;
import com.jiubang.shell.appdrawer.component.IExtendFuncViewEventListener;
import com.jiubang.shell.appdrawer.hideapp.GLHideAppMainView;
import com.jiubang.shell.appdrawer.promanage.GLProManageContainer;
import com.jiubang.shell.appdrawer.recentapp.GLRecentAppContainer;
import com.jiubang.shell.appdrawer.search.GLAppDrawerSearchMainView;
import com.jiubang.shell.appdrawer.widget.GLWidgetMainView;
import com.jiubang.shell.drag.DropTarget;
import com.jiubang.shell.folder.BaseFolderIcon;
import com.jiubang.shell.folder.GLAppFolderMainView.FolderStatusListener;
import com.jiubang.shell.folder.GLAppFolderMainView.FolderViewAnimationListener;
import com.jiubang.shell.gesture.OnMultiTouchGestureListener;
import com.jiubang.shell.gesture.PointInfo;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.theme.GLAppDrawerThemeControler;
import com.jiubang.shell.utils.ToastUtils;

/**
 * 该类主要负责接收外界事件或消息，然后分发给controler或者mainview去执行
 * @author wuziyi
 *
 */
public class GLAppDrawer extends GLFrameLayout
		implements
			IView,
			IMessageHandler,
			IExtendFuncViewEventListener,
			OnMultiTouchGestureListener,
			FolderStatusListener,
			FolderViewAnimationListener,
			IBackgroundInfoChangedObserver,
			BroadCasterObserver {

	private AppDrawerControler mAppDrawerControler;
	private GLAppDrawerMainView mMainView;
	private IView mExtendFuncView;
	private GLView mExtendFuncIcon;
	private IShell mShell;
	private Context mContext;

	
	public static final long DURATION_SHOW_EXTEND_FUNC_VIEW = 450;
	
	public static final int EXTEND_FUNC_ANIM_TYPE_NONE = 0;
	public static final int EXTEND_FUNC_ANIM_TYPE_ZOOM = 1;
	public static final int EXTEND_FUNC_ANIM_TYPE_FLY = 2;
	public static final int EXTEND_FUNC_ANIM_TYPE_BLUR = 3;
	
//	private static final int BACK_TO_SCREEN = -1;
	private static final int NO_ACTION = -2;

	/**
	 * 是否首次进入功能表
	 */
	private boolean mIsFirstEnter = true;
	/**
	 * 是否首次进行layout
	 */
	private boolean mIsFirstLayout = true;

	private FuncAppDataHandler mDataHandler;
	private boolean mExitAfterHideExtendFuncView;

	private static GLAppDrawer sInstance;

	public GLAppDrawer(Context context) {
		super(context);
		sInstance = this;
		mContext = context;
		init(context);
	}

	private void init(Context context) {
		mAppDrawerControler = AppDrawerControler
				.getInstance(ShellAdmin.sShellManager.getActivity());
		SettingProxy.getInstance(ApplicationProxy.getContext()).registerObserver(this);
		MsgMgrProxy.registMsgHandler(this);
		mDataHandler = FuncAppDataHandler.getInstance(ShellAdmin.sShellManager.getActivity());
		mDataHandler.registerBgInfoChangeObserver(this);
		setHasPixelOverlayed(false);
		
		mMainView = new GLAppDrawerMainView(context);
		addView(mMainView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
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
		switch (msgId) {
			case IAppCoreMsgId.EVENT_LOAD_FINISH :
				break;
			case IAppCoreMsgId.EVENT_LOAD_ICONS_FINISH :
				break;
			case IAppCoreMsgId.EVENT_LOAD_TITLES_FINISH :
				break;
			case IAppCoreMsgId.EVENT_INSTALL_APP : {
				mAppDrawerControler.handleCacheInfo(objects[1], true);
				mAppDrawerControler.checkIsMediaManagementPluginInstall(objects[0]);
			}
				break;
			case IAppCoreMsgId.EVENT_INSTALL_PACKAGE : {
				if (objects[0] != null && objects[0] instanceof String
						&& ((String) objects[0]).startsWith(AppFuncConstants.THEME_PACKGE_NAME)) {
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
							AppFuncConstants.THEME_NEW_INSTALLED, objects[0]);
				}
				mAppDrawerControler.checkIsMediaManagementPluginInstall(objects[0]);
			}
				break;
			case IAppCoreMsgId.EVENT_UNINSTALL_APP : {
				mAppDrawerControler.handleCacheInfo(objects[1], false);
				for (int i = 0; i < objects.length; i++) {
					Object object = objects[i];
					if (object instanceof ArrayList<?>) {
						ArrayList<?> list = (ArrayList<?>) object;
						for (Object obj : list) {
							if (obj instanceof AppItemInfo) {
								AppItemInfo itemInfo = (AppItemInfo) obj;
								mAppDrawerControler.removeRecentAppItem(itemInfo.mIntent);
							}
						}
					}
				}
				mAppDrawerControler.checkIsMediaManagementPluginUnInstall(objects[0]);
			}
				break;
			case IAppCoreMsgId.EVENT_UNINSTALL_PACKAGE : {
				mAppDrawerControler.checkIsMediaManagementPluginUnInstall(objects[0]);
			}
				break;
			case IAppCoreMsgId.EVENT_UPDATE_PACKAGE : {
				mAppDrawerControler.checkIsMediaManagementPluginUpdate(objects[0]);
			}
			case IAppCoreMsgId.EVENT_UPDATE_APP : {
				ArrayList<ArrayList<AppItemInfo>> data = (ArrayList<ArrayList<AppItemInfo>>) objects[1];
				ArrayList<AppItemInfo> addList = data.get(0);
				ArrayList<AppItemInfo> removeList = data.get(1);
				if (!addList.isEmpty() || !removeList.isEmpty()) {
					// 缓冲池应该做次修改，支持更新逻辑
					mAppDrawerControler.addInCacheList(removeList, false);
					mAppDrawerControler.addInCacheList(addList, true);
					mAppDrawerControler.handleCachedAppsList();
				}
			}
				break;
			case IAppCoreMsgId.EVENT_CHANGE_APP : {
				List<AppItemInfo> data = (List<AppItemInfo>) objects[1];
				switch (param) {
					case AppDataEngine.EVENT_CHANGE_APP_DISABLE:
						// 减少
						if (!data.isEmpty()) {
							mAppDrawerControler.addInCacheList((ArrayList<AppItemInfo>) data, false);
							mAppDrawerControler.handleCachedAppsList();
						}
						break;
					case AppDataEngine.EVENT_CHANGE_APP_COMPONENT:
						// 改变
						if (!data.isEmpty()) {
							mAppDrawerControler.addInCacheList(data.get(0), false);
							mAppDrawerControler.addInCacheList(data.get(1), true);
							mAppDrawerControler.handleCachedAppsList();
						}
						break;
					case AppDataEngine.EVENT_CHANGE_APP_ENABLE:
						// 增加
						if (!data.isEmpty()) {
							mAppDrawerControler.addInCacheList((ArrayList<AppItemInfo>) data, true);
							mAppDrawerControler.handleCachedAppsList();
						}
						break;
		
					default:
						break;
					}
			}
				break;
			case IAppCoreMsgId.EVENT_SD_MOUNT :
				break;

			case IAppCoreMsgId.EVENT_SD_SHARED :
				break;

			case IAppCoreMsgId.EVENT_REFLUSH_SDCARD_IS_OK : {
				ArrayList<AppItemInfo> sdCardInfos = (ArrayList<AppItemInfo>) objects[1];
				mAppDrawerControler.handleSDCardEvent(sdCardInfos);
			}
				break;
			case IAppCoreMsgId.EVENT_REFLUSH_TIME_IS_UP : {
				ArrayList<AppItemInfo> sdCardInfos = (ArrayList<AppItemInfo>) objects[1];
				mAppDrawerControler.handleTimeUpEvent(sdCardInfos);
			}
				break;
			case IAppCoreMsgId.EVENT_THEME_CHANGED : {
				GLAppDrawerThemeControler.getInstance(mContext).reloadThemeData();
				//				CommonImageManager.getInstance().reloadFolderResource();
				mMainView.handleTabBottomThemeChange();
				mMainView.handleInidcatorThemeChange();
				handleBgThemeChange();
			}
				break;
			case ICommonMsgId.COMMON_ON_HOME_ACTION : {
//				onAppdrawerExit();
				if (isVisible()) {
					mMainView.onHomeAction();
				}
			}
				break;
			case IAppDrawerMsgId.APPDRAWER_EXIT : {
//				onAppdrawerExit();
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_ALL_APP_GRID_VIEW,
						IAppDrawerMsgId.APPDRAWER_RESET_SCROLL_STATE, -1);
//				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_PRO_MANAGE_GRID_VIEW,
//						this, IAppDrawerMsgId.APPDRAWER_RESET_SCROLL_STATE, -1);
//				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_RECENT_APP_GRID_VIEW,
//						this, IAppDrawerMsgId.APPDRAWER_RESET_SCROLL_STATE, -1);
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_FOLDER_ACTION_BAR,
						IAppDrawerMsgId.APPDRAWER_RESET_SCROLL_STATE, -1);
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_PREVIEW_BAR,
						IAppDrawerMsgId.APPDRAWER_RESET_SCROLL_STATE, -1);
				boolean animate = false;
				int snapDuration = 0;
				if (objects != null && objects.length == 2) {
					animate = (Boolean) objects[0];
					snapDuration = (Integer) objects[1];
				}
				mShell.showStage(IShell.STAGE_SCREEN, animate);
				if (param != -1) {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_SNAP_TO_SCREEN, param, false, snapDuration);
				}
			}
				break;
			case IAppDrawerMsgId.APPDRAWER_POPUP_MENU : {
				mMainView.popupMenu();
			}
				break;
			case IAppDrawerMsgId.APPDRAWER_START_SORT : {
				//				int sortType = param;
				mMainView.startSort(true);
			}
				break;
			case IAppDrawerMsgId.APPDRAWER_TAB_HOME_THEME_CHANGE : {
				GLAppDrawerThemeControler.getInstance(mContext).parseTabHomeTheme(); // 重新获取数据
				mMainView.handleTabBottomThemeChange();
			}
				break;
			case IAppDrawerMsgId.EVENT_APPS_LIST_UPDATE : {
				final Object updateBean = objects[0];
				new AsyncTask<Void, Void, Boolean>() {
					@Override
					protected Boolean doInBackground(Void... params) {
						return mAppDrawerControler.appListUpdate(updateBean);
					};

					protected void onPostExecute(Boolean needRefresh) {
						if (needRefresh) {
							MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_ALL_APP_GRID_VIEW,
									IAppDrawerMsgId.APPDRAWER_ALL_APP_ICON_STATE_CHANGE, -1);
							if (mMainView.getVisibility() == GLView.VISIBLE) {
								mMainView.regetUpdateableAppsCount();
							}
						}
					};
				}.execute();
			}
				break;
			case IFolderMsgId.ON_FOLDER_DROP_COMPELETE : {
				Object target = objects[0];
				Object dragInfo = objects[1];
				boolean success = (Boolean) objects[2];
				DropAnimationInfo resetInfo = (DropAnimationInfo) objects[3];
				long folderId = (Long) objects[4];
				mMainView.onFolderDropComplete(target, dragInfo, success, resetInfo, folderId);
//				GLAppFolder.getInstance().batchStartIconEditEndAnimation();
				break;
			}
			case IAppDrawerMsgId.EVENT_UPDATEABLE_APPS_COUNT_CHANGE : {
				if (objects == null || objects.length <= 0) {
					return false;
				}
				final AppsBean appsBean = mAppDrawerControler.getUpdateableAppBeans();
				if (appsBean == null) {
					return false;
				}
				final String packageName = (String) objects[0];
				new AsyncTask<Void, Void, Boolean>() {
					@Override
					protected Boolean doInBackground(Void... params) {
						ArrayList<AppBean> listBeans = appsBean.mListBeans;
						final int count = mAppDrawerControler.getUpdateableAppCount();
						if (null != listBeans && count >= 0) {
							for (AppBean bean : listBeans) {
								if (packageName.equals(bean.mPkgName)) {
									int newCount = count - 1;
									mAppDrawerControler.setUpdateableAppCount(newCount);
									return true;
								}
							}
						}
						return false;
					};

					protected void onPostExecute(Boolean needRefresh) {
						if (needRefresh && mMainView.getVisibility() == GLView.VISIBLE) {
							mMainView.regetUpdateableAppsCount();
						}
					};
				}.execute();
				break;
			}
			case IAppDrawerMsgId.APPDRAWER_NOTIFY_REGET_UPDATEABEL_APPS_COUNT : {
				if (mMainView.getVisibility() == GLView.VISIBLE) {
					mMainView.regetUpdateableAppsCount();
				}
				break;
			}
			case IAppDrawerMsgId.APPDRAWER_LOCATE_APP : {
				Intent intent = (Intent) objects[0];
				mMainView.locateApp(intent);
				break;
			}
			case ICommonMsgId.INDICATOR_CHANGE_SHOWMODE : {
				// 指示器改变了模式
				mMainView.getGridViewContainer().getHorIndicator().doWithShowModeChanged();
				break;

			}
			case IAppCoreMsgId.MEDIA_PLUGIN_CHANGE : {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_ALL_APP_ACTION_BAR,
						IAppCoreMsgId.MEDIA_PLUGIN_CHANGE, -1);
				break;
			}
			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED :
				mMainView.onConfigurationChanged(param);
				break;
			case IAppDrawerMsgId.APPDRAWER_SPECIAL_FOLDER_DISMISS : {
				mMainView.dismissSpecialFolder(param);
				break;
			}
			case ICommonMsgId.COMMON_FULLSCREEN_CHANGED : {
				if (objects != null && objects.length > 0 && objects[0] instanceof Bundle) {
					Bundle bundle = (Bundle) objects[0];
					boolean updateDB = bundle.getBoolean(StatusBarHandler.FIELD_UPDATE_DB);
					if (updateDB) {
						mMainView.requestLayout();
					}
				}
				break;
			}
			case IAppDrawerMsgId.APPDRAWER_ARRANGE_APP : {
				if (param == 0) {
					StatisticsData.countUserActionData(StatisticsData.FUNC_ACTION_ID_APPLICATION,
							StatisticsData.USER_ACTION_TWENTYONE,
							IPreferencesIds.APP_FUNC_ACTION_DATA);
					mMainView.startArrangeApp(2);
				} else if (param == 1) {
					mMainView.recoverArrange();
					StatisticsData.countUserActionData(StatisticsData.FUNC_ACTION_ID_APPLICATION,
							StatisticsData.USER_ACTION_TWENTYTWO,
							IPreferencesIds.APP_FUNC_ACTION_DATA);
				}
			}
				break;
			case IAppDrawerMsgId.APPDRAWER_ICON_ON_DRAG_FLING : {
				boolean isDragFromFolder = param == 1 ? true : false;
				mMainView.dragFlingDelete(isDragFromFolder, objects[0]);
				break;
			}
			case ICommonMsgId.SHOW_EXTEND_FUNC_VIEW : {
				boolean show = param == 1 ? true : false;
				int viewId = (Integer) objects[0];
				boolean needAnimation = true;
				if (show) {
					if (objects.length > 1 && objects[1] instanceof GLView) {
						mExtendFuncIcon = (GLView) objects[1];
					}
					if (objects.length > 2 && objects[2] instanceof Boolean) {
						needAnimation = (Boolean) objects[2];
					}
					if (mExtendFuncView == null) {
						showExtendFuncView(viewId, needAnimation);
					}
				} else {
					if (mExtendFuncView != null
							&& ((GLView) mExtendFuncView).getAnimation() == null) {
						hideExtendFuncView(viewId, needAnimation, objects);
					}
				}
				break;
			}
			case IAppDrawerMsgId.APPDRAWER_ENTER_FUNCTION_SLOT : {
				if (mExtendFuncView == null) {
					mMainView.enterFuntionSlot(param, true, objects);
				}
				break;
			}
			case IAppDrawerMsgId.APPDRAWER_SLIDE_MENU_ACTION : {
				if (param == 1) {
					mMainView.showSlideMenu(true);
				} else if (param == 0) {
					mMainView.hideSlideMenu(true);
				}
				break;
			}
			default :
				break;
		}
		return false;
	}
	
	private void onAppdrawerExit() {
		mMainView.hideSlideMenu(false);
		if (mExtendFuncView != null && ((GLView) mExtendFuncView).getAnimation() == null) {
			mExitAfterHideExtendFuncView = true;
			hideExtendFuncView(mExtendFuncView.getViewId(), false);
		}
	}

	private void hideExtendFuncView(int viewId, boolean animate, Object... objects) {
		int animateType = EXTEND_FUNC_ANIM_TYPE_NONE;
		switch (viewId) {
			case IViewId.WIDGET_MANAGE :
				animateType = EXTEND_FUNC_ANIM_TYPE_ZOOM;
				break;
			case IViewId.HIDE_APP_MANAGE :
				animateType = EXTEND_FUNC_ANIM_TYPE_ZOOM;
				break;
			case IViewId.PRO_MANAGE :
				animateType = EXTEND_FUNC_ANIM_TYPE_BLUR;
				break;
			case IViewId.APP_DRAWER_SEARCH :
				animateType = EXTEND_FUNC_ANIM_TYPE_FLY;
				break;
			case IViewId.RECENT_APP :
				animateType = EXTEND_FUNC_ANIM_TYPE_BLUR;
				break;
			default :
				break;
		}
		AnimationSet extendViewOutAnimation = null;
		AnimationSet mainInAnimation = null;
		GLView glView = mShell.getView(viewId);

		if (animateType == EXTEND_FUNC_ANIM_TYPE_ZOOM) {
			float scale = 1.0f;
			int pivotXValue = 0;
			int pivotYValue = 0;
			if (mExtendFuncIcon != null) {
				int[] loc = new int[2];
				mExtendFuncIcon.getLoactionInGLViewRoot(loc);
				pivotXValue = loc[0] + mExtendFuncIcon.getWidth() / 2;
				pivotYValue = loc[1] + mExtendFuncIcon.getHeight() / 2;

				if (GoLauncherActivityProxy.isPortait()) {
					scale = GoLauncherActivityProxy.getScreenHeight() / mExtendFuncIcon.getHeight();
				} else {
					scale = GoLauncherActivityProxy.getScreenWidth() / mExtendFuncIcon.getWidth();
				}
			}

			int centerX = GoLauncherActivityProxy.getScreenWidth() / 2;
			int centerY = GoLauncherActivityProxy.getScreenHeight() / 2;
			float offsetX = centerX - pivotXValue;
			float offsetY = centerY - pivotYValue;

			float mainPivotXValue = pivotXValue;
			Animation mainAlphaAnim = new AlphaAnimation(0.0f, 1.0f);
			Animation mainScaleAnim = new ScaleAnimation(scale, 1.0f, scale, 1.0f, mainPivotXValue,
					pivotYValue);
			Animation mainTransAnim = new TranslateAnimation(offsetX, 0, offsetY, 0);
			mainInAnimation = new AnimationSet(true);
			mainInAnimation.addAnimation(mainAlphaAnim);
			mainInAnimation.addAnimation(mainScaleAnim);
			mainInAnimation.addAnimation(mainTransAnim);
			mainInAnimation.setDuration(DURATION_SHOW_EXTEND_FUNC_VIEW);

			Animation extendViewalphaAnim = new AlphaAnimation(1.0f, 0.0f);
			Animation extendViewscaleAnim = new ScaleAnimation(1.0f, 1.0f / scale, 1.0f,
					1.0f / scale, pivotXValue, pivotYValue);
			extendViewOutAnimation = new AnimationSet(true);
			extendViewOutAnimation.addAnimation(extendViewalphaAnim);
			extendViewOutAnimation.addAnimation(extendViewscaleAnim);
			extendViewOutAnimation.setDuration(DURATION_SHOW_EXTEND_FUNC_VIEW);
		}
		mMainView.setExtendFuncInAnimation(mainInAnimation);
		mMainView.handleExtendFuncAnim(true, animateType, animate, objects);
		((GLAbsExtendFuncView) glView).setOutAnimation(extendViewOutAnimation);
		mShell.remove(viewId, animate);
	}

	private Animation[] createExtendFuncViewAnimation(int animateType) {
		AnimationSet extendViewInAnimation = null;
		AnimationSet mainOutAnimation = null;
		if (animateType == EXTEND_FUNC_ANIM_TYPE_ZOOM) {
			float scale = 1.0f;
			int pivotXValue = 0;
			int pivotYValue = 0;
			if (mExtendFuncIcon != null) {
				int[] loc = new int[2];
				mExtendFuncIcon.getLoactionInGLViewRoot(loc);
				pivotXValue = loc[0] + mExtendFuncIcon.getWidth() / 2;
				pivotYValue = loc[1] + mExtendFuncIcon.getHeight() / 2;

				if (GoLauncherActivityProxy.isPortait()) {
					scale = GoLauncherActivityProxy.getScreenHeight() / mExtendFuncIcon.getHeight();
				} else {
					scale = GoLauncherActivityProxy.getScreenWidth() / mExtendFuncIcon.getWidth();
				}
			}

			int centerX = GoLauncherActivityProxy.getScreenWidth() / 2;
			int centerY = GoLauncherActivityProxy.getScreenHeight() / 2;
			float offsetX = centerX - pivotXValue;
			float offsetY = centerY - pivotYValue;

			float mainPivotXValue = pivotXValue;

			Animation mainAlphaAnim = new AlphaAnimation(1.0f, 0.0f);
			mainAlphaAnim.setDuration(DURATION_SHOW_EXTEND_FUNC_VIEW * 2 / 3);
			Animation mainScaleAnim = new ScaleAnimation(1.0f, scale, 1.0f, scale, mainPivotXValue,
					pivotYValue);
			mainScaleAnim.setDuration(DURATION_SHOW_EXTEND_FUNC_VIEW);
			Animation mainTransAnim = new TranslateAnimation(0, offsetX, 0, offsetY);
			mainTransAnim.setDuration(DURATION_SHOW_EXTEND_FUNC_VIEW);
			mainOutAnimation = new AnimationSet(true);
			mainOutAnimation.addAnimation(mainAlphaAnim);
			mainOutAnimation.addAnimation(mainScaleAnim);
			mainOutAnimation.addAnimation(mainTransAnim);

			

			Animation extendViewalphaAnim = new AlphaAnimation(0.0f, 1.0f);
			Animation extendViewscaleAnim = new ScaleAnimation(1.0f / scale, 1.0f, 1.0f / scale,
					1.0f, pivotXValue, pivotYValue);
			extendViewInAnimation = new AnimationSet(true);
			extendViewInAnimation.addAnimation(extendViewalphaAnim);
			extendViewInAnimation.addAnimation(extendViewscaleAnim);
			extendViewInAnimation.setDuration(DURATION_SHOW_EXTEND_FUNC_VIEW);
			
		} else {
			
		}
		Animation[] animations = new Animation[2];
		animations[0] = extendViewInAnimation;
		animations[1] = mainOutAnimation;
		return animations;
	}

	protected void showExtendFuncView(int viewId, boolean animate) {
		int animateType = EXTEND_FUNC_ANIM_TYPE_NONE;
		GLAbsExtendFuncView view = null;
		switch (viewId) {
			case IViewId.WIDGET_MANAGE :
				view = new GLWidgetMainView(mContext);
				animateType = EXTEND_FUNC_ANIM_TYPE_ZOOM;
				break;
			case IViewId.HIDE_APP_MANAGE :
				view = new GLHideAppMainView(mContext);
				animateType = EXTEND_FUNC_ANIM_TYPE_ZOOM;
				break;
			case IViewId.PRO_MANAGE :
				view = new GLProManageContainer(mContext);
				animateType = EXTEND_FUNC_ANIM_TYPE_BLUR;
				break;
			case IViewId.APP_DRAWER_SEARCH :
				view = new GLAppDrawerSearchMainView(mContext);
				animateType = EXTEND_FUNC_ANIM_TYPE_FLY;
				break;
			case IViewId.RECENT_APP :
				view = new GLRecentAppContainer(mContext);
				animateType = EXTEND_FUNC_ANIM_TYPE_BLUR;
				break;
			default :
				break;
		}
		if (view != null) {
			if (!animate) {
				animateType = EXTEND_FUNC_ANIM_TYPE_NONE;
			}
			Animation[] animations = createExtendFuncViewAnimation(animateType);
			mMainView.setExtendFuncOutAnimation(animations[1]);
			view.setInAnimation(animations[0]);
			mMainView.handleExtendFuncAnim(false, animateType, animate);
			MsgMgrProxy.sendMessage(GLAppDrawer.this,
					IDiyFrameIds.APP_DRAWER_ALL_APP_TOP_ACTION_BAR,
					IAppDrawerMsgId.APPDRAWER_SHOW_SIDEBAR_GUIDE_CLOUD, 0);
			view.setExtendFuncViewEventListener(this);
			try {
				mShell.show(view, animate);
			} catch (IllegalArgumentException e) {
				view.setExtendFuncViewEventListener(null);
				view.setInAnimation(null);
			}
		}
	}

	@Override
	public int getMsgHandlerId() {
		return IDiyFrameIds.APP_DRAWER;
	}

	public void setShell(IShell shell) {
		mShell = shell;
		mMainView.setShell(shell);
	}

	public IShell getShell() {
		return mShell;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean ret;
		if (mExtendFuncView != null) {
			ret = mExtendFuncView.onKeyDown(keyCode, event);
		}
//		else if (mSlideMenu.isVisible()) {
//			ret = mSlideMenu.onKeyDown(keyCode, event);
//		} 
		else {
			ret = mMainView.onKeyDown(keyCode, event);
		}
		return ret;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean ret;
		if (mExtendFuncView != null) {
			ret = mExtendFuncView.onKeyUp(keyCode, event);
		} 
//		else if (mSlideMenu.isVisible()) {
//			if (keyCode == KeyEvent.KEYCODE_BACK) {
//				hideSlideMenu(true);
//				ret = true;
//			} else {
//				ret = mSlideMenu.onKeyUp(keyCode, event);
//			}
//		} 
		else {
			ret = mMainView.onKeyUp(keyCode, event);
		}
		return ret;
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		boolean ret;
		if (mExtendFuncView != null) {
			ret = mExtendFuncView.onKeyLongPress(keyCode, event);
		}
//		else if (mSlideMenu.isVisible()) {
//			ret = mSlideMenu.onKeyLongPress(keyCode, event);
//		} 
		else {
			ret = mMainView.onKeyLongPress(keyCode, event);
		}
		return ret;
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		boolean ret;
		if (mExtendFuncView != null) {
			ret = mExtendFuncView.onKeyMultiple(keyCode, repeatCount, event);
		}
//		else if (mSlideMenu.isVisible()) {
//			ret = mSlideMenu.onKeyMultiple(keyCode, repeatCount, event);
//		} 
		else {
			ret = mMainView.onKeyMultiple(keyCode, repeatCount, event);
		}
		return ret;
	}

	@Override
	public void extendFuncViewPreEnter(IView view) {
		mExtendFuncView = view;
	}

	@Override
	public void extendFuncViewOnEnter(IView view) {
		mMainView.clearAnimation();
//		mSlideMenu.clearAnimation();
	}

	@Override
	public void extendFuncViewPreExit(IView view) {
	}

	@Override
	public void extendFuncViewOnExit(IView view) {
		mExtendFuncView = null;
		mExtendFuncIcon = null;
		mMainView.clearAnimation();
//		mSlideMenu.clearAnimation();
		if (mExitAfterHideExtendFuncView) {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
					IAppDrawerMsgId.APPDRAWER_EXIT, -1, true, 0);
			mExitAfterHideExtendFuncView = false;
		} else {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_ALL_APP_TOP_ACTION_BAR,
					IAppDrawerMsgId.APPDRAWER_SHOW_SIDEBAR_GUIDE_CLOUD, 1);
		}
	}

	public void setVisible(final boolean visible, boolean animate, Object obj) {
		if (isVisible() == visible) {
			return;
		}

		setVisible(visible);
		int effect = FuncAppDataHandler.getInstance(ShellAdmin.sShellManager.getActivity())
				.getInoutEffect();
		animate = animate && effect != AnimationFactory.EFFECT_NONE;
		
		Object[] objects = (Object[]) obj;
		int viewId = NO_ACTION;
		if (objects != null && objects.length > 0 && objects[0] instanceof Integer) {
			viewId = (Integer) objects[0];
		}
		if (!mAppDrawerControler.isInitedAllFunItemInfo()) {
			animate = false;
		}
		if (visible) {
			mShell.showCoverFrame(!visible);
			if (mIsFirstEnter) {
				// 强制取消第一次的进入动画
				animate = false;
			}
			if (animate) {
				activeEnterAnimation(effect);
			} else {
				if (!mIsFirstEnter) {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_ALL_APP_TOP_ACTION_BAR,
							IAppDrawerMsgId.APPDRAWER_SHOW_SIDEBAR_GUIDE_CLOUD, 1);
				}
			}
			mShell.hide(IViewId.SCREEN, animate);
			mShell.hide(IViewId.DOCK, animate, AnimationFactory.HIDE_ANIMATION);
		} else {
			if (animate) {
				activeExitAnimation(effect);
			} else {
				// 退出罩子层延时，据说原因是屏幕会卡喔
				postDelayed(new Runnable() {

					@Override
					public void run() {
						mShell.showCoverFrame(!visible);
					}
				}, 50);
			}
			mShell.show(IViewId.SCREEN, animate);
			mShell.show(IViewId.DOCK, animate, AnimationFactory.SHOW_ANIMATION);
			onAppdrawerExit();
		}
		// 如果带有额外参数的，需要继续执行跳转
		showNextStage(viewId, animate);

		mMainView.onAppDrawerVisibilityChanged(visible, animate, mIsFirstEnter);
		if (mIsFirstEnter) {
			mIsFirstEnter = false;
		}
		
		// 前3次进入功能表的提示语句
		PreferencesManager manager = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		int count = manager.getInt(IPreferencesIds.ENTER_FUNC, 0);
		if (count < 3) {
			count++;
			manager.putInt(IPreferencesIds.ENTER_FUNC, count);
			manager.commit();
			ToastUtils.showToast(R.string.screen_goto_func_tip, Toast.LENGTH_LONG);
		}
	}

	private void activeEnterAnimation(int effect) {
		mShell.show(IViewId.PROTECTED_LAYER, false);
		clearAnimation();
		AnimationFactory.startEnterAnimation(effect, mContext, this,
				new AnimationListenerAdapter() {

					@Override
					public void onAnimationEnd(Animation animation) {
						postDelayed(new Runnable() {
							public void run() {
								mShell.hide(IViewId.PROTECTED_LAYER, false);
								clearAnimation();
						MsgMgrProxy.sendMessage(GLAppDrawer.this, IDiyFrameIds.APP_DRAWER_ALL_APP_TOP_ACTION_BAR,
								IAppDrawerMsgId.APPDRAWER_SHOW_SIDEBAR_GUIDE_CLOUD, 1);
					}
				}, 50);
					}
				});
	}

	private void activeExitAnimation(int effect) {
		mShell.show(IViewId.PROTECTED_LAYER, false);
		clearAnimation();
		AnimationFactory.startExitAnimation(effect, mContext, this,
				new AnimationListenerAdapter() {

					@Override
					public void onAnimationEnd(Animation animation) {

						post(new Runnable() {
							@Override
							public void run() {
								clearAnimation();
								mShell.hide(IViewId.PROTECTED_LAYER, false);
							}
						});

						postDelayed(new Runnable() {

							@Override
							public void run() {
								mShell.showCoverFrame(true);
							}
						}, 50);
					}
				});
	}

	private void showNextStage(final int iVIewId, boolean animate) {
		switch (iVIewId) {
			case IDiyFrameIds.APP_DRAWER_SEARCH :
				// 搜索改版后，这个要删掉
				Drawable drawable = getBackground();
				if (!(drawable instanceof BitmapDrawable)) {
					drawable = null;
				}
				mShell.hide(IViewId.SCREEN, false);
				mShell.hide(IViewId.DOCK, false);
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
						IDiyFrameIds.APP_DRAWER_SEARCH, drawable, null);
				break;
			case IDiyFrameIds.MEDIA_MANAGEMENT_FRAME : // 资源管理层不进行处理
				break;
			case IViewId.WIDGET_MANAGE : 
				mMainView.showSlideMenu(false);
				mMainView.enterFuntionSlot(iVIewId, animate);
				break;
			case NO_ACTION : 
				break;
			default :
				break;
		}
	}

	@Override
	public int getViewId() {
		return IViewId.APP_DRAWER;
	}

	@Override
	public void onAdd(GLViewGroup parent) {

	}

	@Override
	public void onRemove() {

	}

	@Override
	public void onFolderOpenEnd(int curStatus) {
		mMainView.onFolderOpenEnd(curStatus);
	}

	@Override
	public void onFolderCloseEnd(int curStatus, BaseFolderIcon<?> baseFolderIcon, boolean needReopen) {
		mMainView.onFolderCloseEnd(curStatus, baseFolderIcon, needReopen);
	}

	@Override
	public void onFolderOpen(BaseFolderIcon<?> baseFolderIcon, boolean animate, int curStatus,
			boolean reopen) {
		mMainView.onFolderOpen(baseFolderIcon, animate, curStatus, reopen);
		MsgMgrProxy.sendMessage(GLAppDrawer.this,
				IDiyFrameIds.APP_DRAWER_ALL_APP_TOP_ACTION_BAR,
				IAppDrawerMsgId.APPDRAWER_SHOW_SIDEBAR_GUIDE_CLOUD, 0);
	}

	@Override
	public void onFolderClose(BaseFolderIcon<?> baseFolderIcon, boolean animate, int curStatus) {
		mMainView.onFolderClose(baseFolderIcon, animate, curStatus);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_ALL_APP_TOP_ACTION_BAR,
				IAppDrawerMsgId.APPDRAWER_SHOW_SIDEBAR_GUIDE_CLOUD, 1);
	}

	@Override
	public void onFolderStatusChange(int oldStatus, int newStatus) {
		mMainView.onFolderStatusChange(oldStatus, newStatus);
	}

	@Override
	public void onFolderReLayout(BaseFolderIcon<?> baseFolderIcon, int curStatus) {
		mMainView.onFolderReLayout(baseFolderIcon, curStatus);
	}

	@Override
	public boolean onSwipe(PointInfo p, float dx, float dy) {
		return mMainView.onSwipe(p, dx, dy);
	}

	@Override
	public boolean onTwoFingerSwipe(PointInfo p, float dx, float dy, int direction) {
		return mMainView.onTwoFingerSwipe(p, dx, dy, direction);
	}

	@Override
	public boolean onScale(PointInfo p, float scale, float scaleX, float scaleY, float dx,
			float dy, float angle) {
		return mMainView.onScale(p, scale, scaleX, scaleY, dx, dy, angle);
	}

	@Override
	public boolean onDoubleTap(PointInfo p) {
		return mMainView.onDoubleTap(p);
	}

	public static Drawable getBg() {
		return sInstance.getBackground();
	}

	/**
	 * 绘制功能表背景
	 */
	private void setAppDrawerBg() {
		// 原始背景图
		BitmapDrawable origBg = null;
		switch (mDataHandler.getShowBg()) {
			case FunAppSetting.BG_NON : {
				// 显示默认颜色
				setBackgroundColor(AppFuncConstants.DEFAULT_BG_COLOR);
				break;
			}
			case FunAppSetting.BG_DEFAULT :
				GLAppDrawerThemeControler themeCtrl = GLAppDrawerThemeControler
						.getInstance(mContext);
				if (themeCtrl.isDefaultTheme() == false) {
					try {
						origBg = (BitmapDrawable) themeCtrl.getDrawable(
								themeCtrl.getThemeBean().mWallpaperBean.mImagePath, false, -1);
						setBgImgWithBgColor(origBg);
					} catch (Exception e) {
						// 在某些主题使用了9切图作为背景，导致程序崩溃
						setBackgroundColor(AppFuncConstants.DEFAULT_BG_COLOR);
					}
				} else {
					setBackgroundColor(AppFuncConstants.DEFAULT_BG_COLOR);
				}
				break;
			case FunAppSetting.BG_GO_THEME :
			case FunAppSetting.BG_CUSTOM :
				// 自定义和GO主题背景
				// 需要更新背景图片则重新获取
				origBg = mDataHandler.getBg();
				setBgImgWithBgColor(origBg);
				break;
			default :
				setBackgroundColor(AppFuncConstants.DEFAULT_BG_COLOR);
				break;
		}
	}

	private void setBgImgWithBgColor(BitmapDrawable origBg) {
		if ((null == origBg) || (origBg.getBitmap() == null) || (origBg.getBitmap().isRecycled())) {
			// 如果设置了显示背景图却又没有选择图片，则显示默认颜色
			setBackgroundColor(AppFuncConstants.DEFAULT_BG_COLOR);
		} else {
			// 如果有图片，则不使用颜色
			Bitmap imgWithBgColor = createBgImgWithBgColor(origBg);
			if (imgWithBgColor != null) {
				BitmapDrawable bg = new BitmapDrawable(imgWithBgColor);
				setBackgroundDrawable(bg);
			} else {
				setBackgroundDrawable(origBg);
			}
		}
	}

	/**
	 * 根据背景颜色和自定义背景创建功能表背景图
	 */
	private Bitmap createBgImgWithBgColor(BitmapDrawable origBg) {
//		recycleMergeBg();

		if (origBg.getBitmap() == null || origBg.getBitmap().isRecycled()
				|| origBg.getBitmap().hasAlpha()) {
			return null;
		}
		
		Bitmap mergeBg = initMergeBg();
		Canvas mergeBgCanvas;
		// 合并功能表背景与颜色
		if (mergeBg == null) {
			// 创建图片不成功则直接返回
			return null;
		} else {
			mergeBgCanvas = new Canvas(mergeBg);
		}

		if (1 == mDataHandler.getBlurBackground()) {
			mergeBgCanvas.drawColor(Color.BLACK);
			ImageUtil.drawStretchImage(mergeBgCanvas, origBg.getBitmap(), 0, 0, getWidth(),
					getHeight(), null);
			// 把壁纸模糊(功能表背景为非透明时的应用)
			blurBackground(true, mergeBg, mergeBgCanvas);
		} else {
			ImageUtil.drawStretchImage(mergeBgCanvas, origBg.getBitmap(), 0, 0, getWidth(),
					getHeight(), null);
		}
		return mergeBg;
	}

	/**
	 * 创建合成背景图
	 * 
	 * @return
	 */
	private Bitmap initMergeBg() {
		try {
			Bitmap mergeBg = Bitmap.createBitmap(getWidth(), getHeight(),
					GoLauncherLogicProxy.isHighQualityDrawing() ? Config.ARGB_8888 : Config.RGB_565);
			return mergeBg;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.gcIfAllocateOutOfHeapSize();
		}
		return null;
	}

//	public void recycleMergeBg() {
//		if (mMergeBg != null) {
//			mMergeBg.recycle();
//			mMergeBg = null;
//		}
//	}

	/**
	 * 模糊背景
	 * @param drawColor
	 */
	private void blurBackground(boolean drawColor, Bitmap mergeBg, Canvas mergeBgCanvas) {
		Bitmap bitmap = ImageFilter.convertToARGB8888(mergeBg);
		if (bitmap != null) {
			boolean res = ImageFilter.trickyBlur(bitmap, 2, 4);
			if (res) {
				Canvas canvas = new Canvas(bitmap);
				if (bitmap != mergeBg) {
					canvas.setBitmap(mergeBg);
					canvas.drawBitmap(bitmap, 0, 0, null);
				}
			}
			if (bitmap != mergeBg) {
				bitmap.recycle();
			}
		}
		if (drawColor) {
			final int color = 0x56000000;
			mergeBgCanvas.drawColor(color);
		}
	}

	public void handleBgThemeChange() {
		setAppDrawerBg();
	}

	@Override
	public boolean handleChanges(MessageID msgId, Object obj1, Object obj2) {
		switch (msgId) {
			case BG_CHANGED :
				setAppDrawerBg();
				break;
			case BLUR_BACKGROUND_CHANGED : // 模糊背景改变
				setAppDrawerBg();
				break;
			default :
				break;
		}
		return false;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mIsFirstLayout) {
			setAppDrawerBg();
			mIsFirstLayout = false;
		}
	}
	
	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		Drawable bg = getBackground();
		if (bg != null) {
			bg.setBounds(0, 0, getWidth(), getHeight() + DrawUtils.getNavBarHeight());
		}
		super.dispatchDraw(canvas);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return false;
	}

	public GLGridViewContainer getGridViewContainer() {
		return mMainView.getGridViewContainer();
	}
	
	/**
	 * 
	 * @author yangguanxiang
	 *
	 */
	public static interface OnAppDrawerVisibilityChangedListener {
		public void onAppDrawerVisibilityChanged(boolean visible, boolean animate,
				boolean isFirstEnter);
	}

	@Override
	public void onBCChange(int msgId, int param, Object... objects) {
		switch (msgId) {
			case IAppCoreMsgId.APPCORE_DATACHANGE : {
				if (param == DataType.DATATYPE_DESKTOPSETING) {
					DesktopSettingInfo desktopSettingInfo = SettingProxy.getDesktopSettingInfo();
					int fontSize = desktopSettingInfo.getFontSize();
					int fontColor = Color.WHITE;
					if (GoLauncherLogicProxy.getCustomTitleColor()) {
						int color = GoLauncherLogicProxy.getAppTitleColor();
						if (color != 0) {
							fontColor = color;
						}
					} else {
						DeskThemeControler themeControler = AppCore.getInstance()
								.getDeskThemeControler();
						if (themeControler != null && themeControler.isUsedTheme()) {
							DeskThemeBean themeBean = themeControler.getDeskThemeBean();
							if (themeBean != null && themeBean.mScreen != null) {
								if (null != themeBean.mScreen.mFont) {
									int color = themeBean.mScreen.mFont.mColor;
									if (color != 0) {
										fontColor = color;
									}
								}
							}
						}
					}
					mMainView.setFontSizeColor(fontSize, fontColor);
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_FOLDER_ACTION_BAR,
							IAppDrawerMsgId.APPDRAWER_UPDATE_FOLDER_ACTION_BAR_ICON_TITLE_COLOR,
							fontColor);
				}
				break;
			}

			default :
				break;
		}
	}
}
