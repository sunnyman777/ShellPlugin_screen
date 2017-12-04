package com.jiubang.shell.gesture;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;

import com.gau.golauncherex.plugin.shell.R;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.util.log.LogConstants;
import com.go.util.window.WindowControl;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IDockMsgId;
import com.golauncher.message.IScreenFrameMsgId;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.appfunc.controler.SwitchControler;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingMainActivity;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogStatusObserver;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DockConstants;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.DesktopIndicator;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeManageActivity;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeManageView;
import com.jiubang.ggheart.data.GlobalSetConfig;
import com.jiubang.ggheart.data.info.GestureSettingInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.statistics.StaticTutorial;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;
import com.jiubang.shell.IShell;

/**
 * 桌面手势处理器
 * 
 * @author yuankai
 * @version 1.0
 */
public class GLGestureHandler {

	/**
	 * 窗口控制器
	 */
	private Activity mActivity;

	private IShell mShell;

	private HashMap<String, Integer> mActionMapping = new HashMap<String, Integer>();
	/**
	 * 手势处理器构造方法
	 * 
	 * @param frameControl
	 *            帧控制器
	 * @param messageSender
	 *            消息发送器
	 * @param activity
	 *            上下文
	 */
	public GLGestureHandler(Activity activity, IShell shell) {
		mActivity = activity;
		mShell = shell;
		init();
	}

	private void init() {
		mActionMapping.put(ICustomAction.ACTION_NONE, GlobalSetConfig.GESTURE_DISABLE);
		mActionMapping.put(ICustomAction.ACTION_SHOW_MAIN_SCREEN,
				GlobalSetConfig.GESTURE_SHOW_MAIN_SCREEN);
		mActionMapping.put(ICustomAction.ACTION_SHOW_MAIN_OR_PREVIEW,
				GlobalSetConfig.GESTURE_SHOW_MAIN_SCREEN_OR_PREVIEW);
		mActionMapping.put(ICustomAction.ACTION_SHOW_PREVIEW, GlobalSetConfig.GESTURE_SHOW_PREVIEW);
		mActionMapping.put(ICustomAction.ACTION_SHOW_FUNCMENU,
				GlobalSetConfig.GESTURE_OPEN_CLOSE_APPFUNC);
		mActionMapping.put(ICustomAction.ACTION_SHOW_EXPEND_BAR,
				GlobalSetConfig.GESTURE_SHOW_HIDE_NOTIFICATIONEXPAND);
		mActionMapping.put(ICustomAction.ACTION_SHOW_HIDE_STATUSBAR,
				GlobalSetConfig.GESTURE_SHOW_HIDE_NOTIFICATIONBAR);
		mActionMapping.put(ICustomAction.ACTION_ENABLE_SCREEN_GUARD,
				GlobalSetConfig.GESTURE_LOCK_SCREEN);
		mActionMapping.put(ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE,
				GlobalSetConfig.GESTURE_GOSTORE);
		mActionMapping.put(ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME,
				GlobalSetConfig.GESTURE_OPEN_THEME_SETTING);
		mActionMapping.put(ICustomAction.ACTION_SHOW_PREFERENCES,
				GlobalSetConfig.GESTURE_OPEN_DESK_PREFENECE);
		mActionMapping.put(ICustomAction.ACTION_SHOW_MENU, GlobalSetConfig.GESTURE_SHOW_MENU);
		mActionMapping.put(ICustomAction.ACTION_SHOW_DIYGESTURE,
				GlobalSetConfig.GESTURE_SHOW_DIYGESTURE);
		mActionMapping.put(ICustomAction.ACTION_SHOW_PHOTO, GlobalSetConfig.GESTURE_SHOW_PHOTO);
		mActionMapping.put(ICustomAction.ACTION_SHOW_MUSIC, GlobalSetConfig.GESTURE_SHOW_MUSIC);
		mActionMapping.put(ICustomAction.ACTION_SHOW_VIDEO, GlobalSetConfig.GESTURE_SHOW_VIDEO);
	}

	public void handleGesture(Intent intent, ArrayList<Rect> posArrayList) {
		GestureSettingInfo info = new GestureSettingInfo();
		if (mActionMapping.containsKey(intent.getAction())) {
			info.mGoShortCut = mActionMapping.get(intent.getAction());
			handleGoShortCutGesture(info.mGoShortCut);
		} else {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, ICommonMsgId.START_ACTIVITY,
					-1, intent, posArrayList);
		}
	}

	/**
	 * 处理手势方法
	 * 
	 * @param info
	 *            手势标识数据结构
	 * @param isClickHomeKey
	 *            是否点击home键触发
	 */
	public void handleGesture(GestureSettingInfo info) {
		if (info == null) {
			return;
		}

		switch (info.mGestureAction) {
			case GlobalSetConfig.GESTURE_DISABLE :
				// do nothing
				break;
			case GlobalSetConfig.GESTURE_GOSHORTCUT :
				handleGoShortCutGesture(info.mGoShortCut);
				break;

			case GlobalSetConfig.GESTURE_SELECT_SHORTCUT : {
				// 打开应用程序
				String uriString = info.mAction;
				if (uriString != null && uriString.length() > 0) {
					Intent launchIntent = null;
					try {
						launchIntent = Intent.parseUri(uriString, 0);
						Rect rect = new Rect(0, 0, GoLauncherActivityProxy.getScreenWidth(),
								GoLauncherActivityProxy.getScreenHeight());
						ArrayList<Rect> posArrayList = new ArrayList<Rect>();
						posArrayList.add(rect);
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								ICommonMsgId.START_ACTIVITY, -1, launchIntent, posArrayList);
						posArrayList.clear();
					} catch (URISyntaxException e) {
						return;
					}
				}
			}
				break;

			case GlobalSetConfig.GESTURE_SELECT_APP : {
				// 打开应用程序
				String uriString = info.mAction;
				if (uriString != null && uriString.length() > 0) {
					Intent launchIntent = null;
					try {
						launchIntent = Intent.parseUri(uriString, 0);
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								ICommonMsgId.START_ACTIVITY, -1, launchIntent, null);
					} catch (URISyntaxException e) {
						return;
					}
				}
			}
				break;

			default :
				break;
		}
	}

	/**
	 * <br>
	 * 用来显示状态栏和指示器 <br>
	 * 功能详细描述:
	 * 
	 * @author maxiaojun
	 * @date [2012-9-19]
	 */
	class ResetStatusThread extends Thread {
		private boolean mIsVib;
		private DesktopIndicator mIndicator;

		public ResetStatusThread(boolean isVib, DesktopIndicator indicator) {
			this.mIsVib = isVib;
			this.mIndicator = indicator;
		}

		@Override
		public void run() {
			if (!mIsVib && null != mIndicator) {
				mIndicator.show();
			}
		}
	};

	private void handleGoShortCutGesture(int gestureAction) {
		switch (gestureAction) {
			case GlobalSetConfig.GESTURE_SHOW_MAIN_SCREEN : {
				DialogStatusObserver observer = DialogStatusObserver.getInstance();
//				if (mShell.getCurrentStage() == IShell.STAGE_SCREEN) {
//					if (observer.isDialogShowing()) {
//						observer.dismissDialog();
//					} else {
//						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, this,
//								IScreenFrameMsgId.SCREEN_SHOW_HOME, -1);
//					}
//				} else {
//					AbstractFrame frame = mShell.getFrameControl().getTopFrame();
//					if (frame != null) {
//						switch (frame.getId()) {
//							case IDiyFrameIds.APPFUNC_SEARCH_FRAME :
//								MsgMgrProxy.sendMessage(this, IDiyFrameIds.APPFUNC_SEARCH_FRAME,
//										IFrameworkMsgId.SYSTEM_HOME_CLICK, -1, null, null);
//								break;
//							case IDiyFrameIds.MEDIA_MANAGEMENT_FRAME :
//								MsgMgrProxy.sendMessage(this, IDiyFrameIds.MEDIA_MANAGEMENT_FRAME,
//										IScreenFrameMsgId.MEDIA_MANAGEMENT_BACK_TO_MAIN_SCREEN, -1, null,
//										null);
//								break;
//							case IDiyFrameIds.IMAGE_BROWSER_FRAME :
//								MsgMgrProxy.sendMessage(this, IDiyFrameIds.IMAGE_BROWSER_FRAME,
//										IFrameworkMsgId.SYSTEM_HOME_CLICK, -1, null, null);
//								MsgMgrProxy.sendMessage(this, IDiyFrameIds.MEDIA_MANAGEMENT_FRAME,
//										IScreenFrameMsgId.MEDIA_MANAGEMENT_BACK_TO_MAIN_SCREEN, -1, null,
//										null);
//								break;
//							default :
//								mShell.showStage(IShell.STAGE_SCREEN, true);
//								break;
//						}
//					} else {
//						mShell.showStage(IShell.STAGE_SCREEN, true);
//					}
//				}
				if (observer.isDialogShowing()) {
					observer.dismissDialog();
				} else {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_SHOW_HOME, -1);
				}
			}
				break;

			case GlobalSetConfig.GESTURE_SHOW_MAIN_SCREEN_OR_PREVIEW : {
				DialogStatusObserver observer = DialogStatusObserver.getInstance();
//				if (mShell.getCurrentStage() == IShell.STAGE_SCREEN) {
//					if (observer.isDialogShowing()) {
//						observer.dismissDialog();
//					} else {
//						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, this,
//								IScreenFrameMsgId.SCREEN_SHOW_HOME_OR_SHOW_PREVIEW, -1);
//					}
//				} else {
//					AbstractFrame frame = mShell.getFrameControl().getTopFrame();
//					if (frame != null) {
//						switch (frame.getId()) {
//							case IDiyFrameIds.APPFUNC_SEARCH_FRAME :
//								MsgMgrProxy.sendMessage(this, IDiyFrameIds.APPFUNC_SEARCH_FRAME,
//										IFrameworkMsgId.SYSTEM_HOME_CLICK, -1, null, null);
//								break;
//							case IDiyFrameIds.MEDIA_MANAGEMENT_FRAME :
//								MsgMgrProxy.sendMessage(this, IDiyFrameIds.MEDIA_MANAGEMENT_FRAME,
//										IScreenFrameMsgId.MEDIA_MANAGEMENT_BACK_TO_MAIN_SCREEN, -1, null,
//										null);
//								break;
//							case IDiyFrameIds.IMAGE_BROWSER_FRAME :
//								MsgMgrProxy.sendMessage(this, IDiyFrameIds.IMAGE_BROWSER_FRAME,
//										IFrameworkMsgId.SYSTEM_HOME_CLICK, -1, null, null);
//								MsgMgrProxy.sendMessage(this, IDiyFrameIds.MEDIA_MANAGEMENT_FRAME,
//										IScreenFrameMsgId.MEDIA_MANAGEMENT_BACK_TO_MAIN_SCREEN, -1, null,
//										null);
//								break;
//							default :
//								mShell.showStage(IShell.STAGE_SCREEN, true);
//								break;
//						}
//					} else {
//						mShell.showStage(IShell.STAGE_SCREEN, true);
//					}
//				}
				if (observer.isDialogShowing()) {
					observer.dismissDialog();
				} else {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SCREEN_SHOW_MAIN_SCREEN_OR_PREVIEW, -1);
				}
			}
				break;

			case GlobalSetConfig.GESTURE_SHOW_PREVIEW : // 显示预览
			{
//				if (mShell.getCurrentStage() == IShell.STAGE_SCREEN) {
//					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, this,
//							IScreenPreviewMsgId.PREVIEW_SHOW, -1, false);
//				} else {
//					mShell.showStage(IShell.STAGE_SCREEN, true);
//				}
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SCREEN_SHOW_PREVIEW, -1, false);
			}
				break;

			case GlobalSetConfig.GESTURE_SHOW_HIDE_NOTIFICATIONBAR : // 显示/不显示状态栏
			{
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						ICommonMsgId.SHOW_HIDE_STATUSBAR, -1, null, null);
			}
				break;

			case GlobalSetConfig.GESTURE_SHOW_HIDE_NOTIFICATIONEXPAND : // 显示通知扩展栏
			{
				try {
					WindowControl.setIsFullScreen(mActivity, false, true);
					WindowControl.expendNotification(mActivity);

				} catch (Exception e) {
					Log.i(LogConstants.HEART_TAG, e.toString());
				}
			}
				break;

			case GlobalSetConfig.GESTURE_OPEN_CLOSE_APPFUNC : // 打开/关闭功能表
			{
//				boolean handled = false;
//				AbstractFrame frame = mShell.getFrameControl().getTopFrame();
//				if (frame != null) {
//					switch (frame.getId()) {
//						case IDiyFrameIds.APPFUNC_SEARCH_FRAME :
//							MsgMgrProxy.sendMessage(this, IDiyFrameIds.APPFUNC_SEARCH_FRAME,
//									IScreenFrameMsgId.BACK_TO_MAIN_SCREEN, -1, null, null);
//							handled = true;
//							break;
//						case IDiyFrameIds.MEDIA_MANAGEMENT_FRAME :
//							SwitchControler.getInstance(mActivity).showAppDrawerFrame(true);
//							handled = true;
//							break;
//						case IDiyFrameIds.IMAGE_BROWSER_FRAME :
//							MsgMgrProxy.sendMessage(this, IDiyFrameIds.IMAGE_BROWSER_FRAME,
//									IFrameworkMsgId.SYSTEM_HOME_CLICK, -1, null, null);
//							SwitchControler.getInstance(mActivity).showAppDrawerFrame(true);
//							handled = true;
//							break;
//						default :
//							break;
//					}
//				}
//				if (!handled) {
//					if (mShell.getCurrentStage() == IShell.STAGE_APP_DRAWER
//							|| (mShell.getCurrentStage() == IShell.STAGE_APP_FOLDER && GLAppFolder
//									.getInstance().getFolderFrom() == GLAppFolderInfo.FOLDER_FROM_APPDRAWER)) {
//
//						mShell.showStage(ShellFrame.STAGE_SCREEN, true);
//					} else {
//						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, this,
//								IScreenFrameMsgId.SCREEN_TO_APPDRAWER, -1);
//					}
//				}
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
						IScreenFrameMsgId.SCREEN_TO_APPDRAWER, -1);
			}
				break;

			case GlobalSetConfig.GESTURE_SHOW_HIDE_DOCK : {
//				int type = DockFrame.HIDE_ANIMATION_NO;
//				if (mShell.getCurrentStage() == IShell.STAGE_SCREEN) {
//					type = DockFrame.HIDE_ANIMATION;
//				}
//				if (ShortCutSettingInfo.sEnable) {
//					MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK, this,
//							IScreenFrameMsgId.DOCK_HIDE, type);
//				} else {
//					if (mShell.getCurrentStage() != IShell.STAGE_SCREEN) {
//						MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK, this,
//								IScreenFrameMsgId.DOCK_SHOW, type, true);
//					} else {
//						MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK, this,
//								IScreenFrameMsgId.DOCK_SHOW, type);
//					}
//				}
				
				if (MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
						IScreenFrameMsgId.SCREEN_IS_IN_EDIT_STATE, -1)) {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_EXIT_EDIT_STATE, -1);
//					ShellAdmin.sShellManager.getContentView().postDelayed(new Runnable() {
//
//						@Override
//						public void run() {
//							int type = DockFrame.HIDE_ANIMATION;
//							if (ShortCutSettingInfo.sEnable) {
//								MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK, this,
//										IScreenFrameMsgId.DOCK_HIDE, type);
//							} else {
//								MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK, this,
//										IScreenFrameMsgId.DOCK_SHOW, type);
//							}
//						}
//					}, GLWorkspace.ANIMDURATION + 150);
				} else {
					int type = DockConstants.HIDE_ANIMATION;
					if (ShortCutSettingInfo.sEnable) {
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK,
								IDockMsgId.DOCK_HIDE, type);
					} else {
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK,
								IDockMsgId.DOCK_SHOW, type);
					}
				}
			}
				break;
			case GlobalSetConfig.GESTURE_LOCK_SCREEN : {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						ICommonMsgId.ENABLE_KEYGUARD, -1, null, null);
			}
				break;
			case GlobalSetConfig.GESTURE_GOSTORE : {
				AppsManagementActivity.startAppCenter(mActivity,
						MainViewGroup.ACCESS_FOR_APPCENTER_THEME, false);
			}
				break;
			case GlobalSetConfig.GESTURE_OPEN_THEME_SETTING : {
				Intent mythemesIntent = new Intent();
				mythemesIntent.putExtra("entrance", ThemeManageView.LAUNCHER_THEME_VIEW_ID);
				mythemesIntent.setClass(mActivity, ThemeManageActivity.class);
				mActivity.startActivity(mythemesIntent);
			}
				break;
			case GlobalSetConfig.GESTURE_OPEN_DESK_PREFENECE : {
				if (DeskSettingMainActivity.sStopped) {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							ICommonMsgId.PREFERENCES, -1, null, null);
				}
			}
				break;
			case GlobalSetConfig.GESTURE_SHOW_MENU : {
//				if (mShell.getCurrentStage() == IShell.STAGE_SCREEN) {
//					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, this,
//							IScreenFrameMsgId.SCREEN_SHOW_HIDE_MENU, -1);
//				}
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SHOW_GLGGMENU, -1);
			}
				break;
			case GlobalSetConfig.GESTURE_SHOW_DIYGESTURE : {
				if (GoLauncherActivityProxy.isGOLauncherOnTop()) {
					Intent intent = new Intent(ICustomAction.ACTION_SHOW_DIYGESTURE);
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							ICommonMsgId.START_ACTIVITY, -1, intent, null);
				}
			}
				break;
			case GlobalSetConfig.GESTURE_SHOW_PHOTO : {
				if (MediaPluginFactory.isMediaPluginExist(mActivity)) {
					if (AppFuncUtils.getInstance(mActivity).isMediaPluginCompatible()) {
						// 打开功能表并进入图片管理界面
						StatisticsData.countMenuData(mActivity, StatisticsData.FUNTAB_KEY_IMAGE);
						SwitchControler.getInstance(mActivity).showMediaManagementImageContent();
					}
				} else {
					showMedPlugDownDialog();
				}
			}
				break;
			case GlobalSetConfig.GESTURE_SHOW_MUSIC : {
				if (MediaPluginFactory.isMediaPluginExist(mActivity)) {
					if (AppFuncUtils.getInstance(mActivity).isMediaPluginCompatible()) {
						// 打开功能表并进入音乐管理界面
						StatisticsData.countMenuData(mActivity, StatisticsData.FUNTAB_KEY_AUDIO);
						SwitchControler.getInstance(mActivity).showMediaManagementMusicContent();
					}
				} else {
					showMedPlugDownDialog();
				}
			}
				break;
			case GlobalSetConfig.GESTURE_SHOW_VIDEO : {
				if (MediaPluginFactory.isMediaPluginExist(mActivity)) {
					if (AppFuncUtils.getInstance(mActivity).isMediaPluginCompatible()) {
						// 打开功能表并进入视频管理界面
						StatisticsData.countMenuData(mActivity, StatisticsData.FUNTAB_KEY_VIDEO);
						SwitchControler.getInstance(mActivity).showMediaManagementVideoContent();
					}
				} else {
					showMedPlugDownDialog();
				}
			}
				break;
			default :
				break;
		}
	}

	/**
	 * 更新菜单SharedPreferences记录的值，经过手势滑动之后，改为false，按下menu不再需要打开用户提示
	 */
	private void updateMenuSharedPreferencesValue() {
		if (StaticTutorial.sCheckScreenMenuOpen) {
			StaticTutorial.sCheckScreenMenuOpen = false;
			PreferencesManager sharedPreferences = new PreferencesManager(mActivity,
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_SCREEN_MENU_OPEN_GUIDE, false);
			sharedPreferences.commit();
		}
	}

	private void showMedPlugDownDialog() {
		final Context context = mShell.getContext();
		String textFirst = context
				.getString(R.string.download_mediamanagement_plugin_dialog_text_first);
		String textMiddle = context
				.getString(R.string.download_mediamanagement_plugin_dialog_text_middle);
		String textLast = context
				.getString(R.string.download_mediamanagement_plugin_dialog_text_last);
		SpannableStringBuilder messageText = new SpannableStringBuilder(textFirst + textMiddle
				+ textLast);
		messageText.setSpan(new RelativeSizeSpan(0.8f), textFirst.length(),
				messageText.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		messageText.setSpan(
				new ForegroundColorSpan(context.getResources().getColor(
						R.color.snapshot_icon_title_color)), textFirst.length(), textFirst.length()
						+ textMiddle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  //设置提示为绿色

		DialogConfirm dialog = new DialogConfirm(mActivity);
		dialog.show();
		dialog.setTitle(context.getString(R.string.download_mediamanagement_plugin_dialog_title));
		dialog.setMessage(messageText);
		dialog.setPositiveButton(context
				.getString(R.string.download_mediamanagement_plugin_dialog_download_btn_text),
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// 跳转进行下载
						Context context = ApplicationProxy.getContext();
						String packageName = PackageName.MEDIA_PLUGIN;
						String url = LauncherEnv.Url.MEDIA_PLUGIN_FTP_URL; // 插件包ftp地址
						String linkArray[] = { packageName, url };
						String title = context
								.getString(R.string.mediamanagement_plugin_download_title);
						boolean isCnUser = GoAppUtils.isCnUser(context);

						CheckApplication.downloadAppFromMarketFTPGostore(context, "",
								linkArray, LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK, title,
								System.currentTimeMillis(), isCnUser,
								CheckApplication.FROM_MEDIA_DOWNLOAD_DIGLOG, 0, null);
					}
				});
		dialog.setNegativeButton(
				context.getString(R.string.download_mediamanagement_plugin_dialog_later_btn_text),
				null);
	}
}
