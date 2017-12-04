package com.jiubang.shell.folder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLayoutInflater;
import com.go.proxy.GoLauncherLogicProxy;
import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.IAppCoreMsgId;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IFolderMsgId;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FavoriteInfo;
import com.jiubang.ggheart.data.info.GestureSettingInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ScreenAppWidgetInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderController;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderInfo;
import com.jiubang.ggheart.plugin.shell.folder.GLFolderModifyBaseActivity;
import com.jiubang.shell.IShell;
import com.jiubang.shell.appdrawer.component.GLAppDrawerBaseGrid;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.drag.DragController.DragListener;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.folder.GLAppFolderMainView.FolderStatusListener;
import com.jiubang.shell.folder.GLAppFolderMainView.FolderViewAnimationListener;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.preview.GLSense;
import com.jiubang.shell.screen.GLCellLayout;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-2-21]
 */
@SuppressWarnings("rawtypes")
public class GLAppFolder implements IMessageHandler, DragListener {

	private static volatile GLAppFolder sInstance;
	public final IShell shell;
	private GLAppFolderController mFolderController;

	private HashMap<Long, GLScreenFolderIcon> mScreenFolderIcons;
	private HashMap<Long, GLDockFolderIcon> mDockFolderIcons;
	private HashMap<Long, GLAppDrawerFolderIcon> mDrawerFolderIcons;

	private GLAppFolderMainView mFolderMainView;
	private boolean mIsFolderEditAnimating = false;
	
	private GLAppFolder(IShell shell, GLAppFolderMainView mainView, DragController dragController) {
		this.shell = shell;
		mFolderMainView = mainView;
		MsgMgrProxy.registMsgHandler(this);
		mFolderController = GLAppFolderController.getInstance();
		mScreenFolderIcons = new HashMap<Long, GLScreenFolderIcon>();
		mDockFolderIcons = new HashMap<Long, GLDockFolderIcon>();
		mDrawerFolderIcons = new HashMap<Long, GLAppDrawerFolderIcon>();
		dragController.addDragListener(this);
	}

	public static GLAppFolder buildGLAppFolder(IShell shell, GLAppFolderMainView mainView,
			DragController dragController) {
		if (sInstance == null) {
			sInstance = new GLAppFolder(shell, mainView, dragController);
		}
		return sInstance;
	}

	public static GLAppFolder getInstance() {
		if (sInstance == null) {
			throw new NullPointerException("GLAppFolder not yet builded");
		}
		return sInstance;
	}
	@Override
	public boolean handleMessage(Object sender, int msgId, int param, final Object... objects) {
		switch (msgId) {
			case ICommonMsgId.ICON_RENAME :
				List<Object> list = (List<Object>) objects[1];
				long itemId = (Long) objects[0];
				mFolderMainView.onNameChange((String) list.get(0), itemId);
				break;
			case ICommonMsgId.COMMON_ON_HOME_ACTION :
				if (mFolderMainView.isVisible()) {
					return onHomeAction((GestureSettingInfo) objects[0]);
				}
				break;
			case ICommonMsgId.COMMON_IMAGE_CHANGED :
				mFolderMainView.loadResource();
				break;
			case IFolderMsgId.MODIFY_FOLDER_COMPELETE:
				if (objects[0] != null && objects[0] instanceof Intent) {
					Intent intent = (Intent) objects[0];
					mFolderMainView.onFolderModify();
					String folderName = intent
							.getStringExtra(GLFolderModifyBaseActivity.FOLDER_NAME);
					long folderId = intent.getLongExtra(GLFolderModifyBaseActivity.FOLDER_ID, -1);
					GLAppFolderInfo folderInfo = mFolderController.getFolderInfoById(folderId,
							GLAppFolderInfo.FOLDER_FROM_SCREEN);
					if (folderInfo != null) {
						getFolderIcon(folderInfo).refreshIcon();
					}
					if (folderName != null) {
						mFolderMainView.onNameChange(folderName,
								intent.getLongExtra(GLFolderModifyBaseActivity.FOLDER_ID, -1));
					}
				}
				break;
			case IFolderMsgId.FOLDER_RELAYOUT :
				if (isFolderOpened()) {
					BaseFolderIcon<?> folderIcon = null;
					if (objects != null && objects.length > 0
							&& objects[0] instanceof BaseFolderIcon<?>) {
						folderIcon = (BaseFolderIcon<?>) objects[0];
					}
					mFolderMainView.relayoutFolder(folderIcon);
				}
				break;
			case IFolderMsgId.FOLDER_KEEP_OPEN :
				if (objects != null && objects.length > 0 && objects[0] instanceof Boolean) {
					boolean keep = (Boolean) objects[0];
					Object dragInfo = null;
					if (objects.length > 1) {
						dragInfo = objects[1];
					}
					mFolderMainView.keepOpenFolder(keep, dragInfo);
				}
				break;
			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED :
				if (isFolderOpened()) {
					mFolderMainView.onConfigurationChanged(param);
				}
				break;
			case IFolderMsgId.FOLDER_RESET_DEFAULT:
			case ICommonMsgId.CHANGE_ICON_STYLE:
				if (isFolderOpened()) {
					mFolderMainView.onFolderContentIconChange((Bundle) objects[0]);
				}
				break;
			case IAppCoreMsgId.EVENT_INSTALL_APP :
				BaseFolderIcon<?> folderIcon = getCurFolderIcon();
				if (objects.length > 2 && objects[0] instanceof String
						&& objects[1] instanceof ArrayList<?>
						&& objects[2] instanceof BaseFolderIcon<?>
						&& folderIcon == (BaseFolderIcon<?>) objects[2]) {
					boolean needRefresh = false;
					String packageName = (String) objects[0];
					if (PackageName.RECOMMEND_APP_PACKAGE.equals(packageName)) {
						if (folderIcon instanceof GLScreenFolderIcon
								|| folderIcon instanceof GLDockFolderIcon) {
							ArrayList<AppItemInfo> appItemInfos = (ArrayList<AppItemInfo>) objects[1];
							ArrayList<ItemInfo> itemInfos = new ArrayList<ItemInfo>();
							for (AppItemInfo appItemInfo : appItemInfos) {
								if (folderIcon.getFolderInfo().folderType == appItemInfo.mClassification) {
									ShortCutInfo scInfo = new ShortCutInfo();
									scInfo.mIcon = appItemInfo.mIcon;
									scInfo.mIntent = appItemInfo.mIntent;
									scInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
									scInfo.mSpanX = 1;
									scInfo.mSpanY = 1;
									scInfo.mTitle = appItemInfo.mTitle;
									scInfo.mInScreenId = -1;
									scInfo.setRelativeItemInfo(appItemInfo);
									itemInfos.add(scInfo);
								}
							}
							if (!itemInfos.isEmpty()) {
								mFolderController.addAppToScreenFolderBatch(
										folderIcon.getFolderInfo().folderId, itemInfos, false);
								needRefresh = true;
							}
						}
					}
					if (isFolderOpened() && needRefresh) {
						mFolderMainView.relayoutFolder(folderIcon);
					}
				}
				break;
			case IAppCoreMsgId.EVENT_UNINSTALL_APP :
				deleteUninstallApp(objects);
				break;
			case IAppCoreMsgId.EVENT_CHANGE_APP:
				if (param == AppDataEngine.EVENT_CHANGE_APP_DISABLE
						|| param == AppDataEngine.EVENT_CHANGE_APP_COMPONENT) {
					if (objects != null) {
						List<AppItemInfo> infoList = (List<AppItemInfo>) objects[1];
						deleteUninstallApp(null, infoList);
					}
				}
				break;
			case IAppCoreMsgId.EVENT_UPDATE_PACKAGE:	
			case IAppCoreMsgId.EVENT_UPDATE_APP:
				if (objects != null && objects[1] != null) {
					List<List<AppItemInfo>> data = (List<List<AppItemInfo>>) objects[1];
					if (data.get(1) != null) {
						ArrayList<AppItemInfo> disableList = (ArrayList<AppItemInfo>) data.get(1);
						if (!disableList.isEmpty()) {
							deleteUninstallApp(null, disableList);
						}
					}
				}
				break;
			case IFolderMsgId.FOLDER_LOCATE_APP :
				if (isFolderOpened()) {
					Intent intent = (Intent) objects[0];
					mFolderMainView.onFolderLocateApp(intent);
				}
				break;
			case IFolderMsgId.FOLDER_SD_EVENT_REMOVE_ITEMS:
				mFolderMainView.post(new Runnable() {

					@Override
					public void run() {
						deleteUninstallApp(objects);

					}
				});
				break;
			case ICommonMsgId.INDICATOR_CHANGE_SHOWMODE:
				mFolderMainView.getGLAppFolderGridVIewContainer().getIndicator().doWithShowModeChanged();
				break;
			case IFrameworkMsgId.SYSTEM_ON_RESUME:
				mFolderMainView.onResume();
				break;
			default :
				break;
		}
		return false;
	}

	private boolean onHomeAction(GestureSettingInfo info) {
		return mFolderMainView.onHomeAction(info);
	}

	private void deleteUninstallApp(Object... objects) {
		ArrayList<AppItemInfo> uninstallapps = (ArrayList<AppItemInfo>) objects[1];
		if (isFolderOpened()) {
			mFolderMainView.onFolderContentUninstall(uninstallapps);
		}
		ArrayList<GLAppFolderInfo> appFolderInfos = mFolderController
				.onFolderAppUninstall(uninstallapps);
		for (GLAppFolderInfo folderInfo : appFolderInfos) {
			BaseFolderIcon<?> folderIcon = getFolderIcon(folderInfo);
			folderIcon.refreshIcon();
			int frameId = -1;
			switch (folderInfo.folderFrom) {
				case GLAppFolderInfo.FOLDER_FROM_DOCK :
					frameId = IDiyFrameIds.DOCK;
					break;
				case GLAppFolderInfo.FOLDER_FROM_SCREEN :
					frameId = IDiyFrameIds.SCREEN;
					break;
				default :
					break;
			}
			UserFolderInfo userFolderInfo = folderInfo.getScreenFoIderInfo();
			if (userFolderInfo != null && userFolderInfo.getChildCount() < 1 && frameId != -1) {
				MsgMgrProxy.sendMessage(this, frameId, IFolderMsgId.FOLDER_APP_LESS_TWO, -1,
						folderIcon, userFolderInfo);
			}
		}
	}

	@Override
	public int getMsgHandlerId() {
		return IDiyFrameIds.APP_FOLDER;
	}

	private synchronized BaseFolderIcon createFolderIcon(GLAppFolderInfo folderInfo) {
		GLLayoutInflater inflater = ShellAdmin.sShellManager.getLayoutInflater();
		BaseFolderIcon folderIcon = null;
		switch (folderInfo.folderFrom) {
			case GLAppFolderInfo.FOLDER_FROM_APPDRAWER :
				folderIcon = mDrawerFolderIcons.get(folderInfo.folderId);
				if (folderIcon == null || folderIcon.isRecyled()) {
					folderIcon = (BaseFolderIcon) inflater.inflate(
							R.layout.gl_appdrawer_folder_icon, null);
					mDrawerFolderIcons.put(folderInfo.folderId, (GLAppDrawerFolderIcon) folderIcon);
					GLAppFolderController.getInstance().addFolderInfo(folderInfo);
				}
				break;
			case GLAppFolderInfo.FOLDER_FROM_SCREEN :
				folderIcon = mScreenFolderIcons.get(folderInfo.folderId);
				if (folderIcon == null || folderIcon.isRecyled()) {
					folderIcon = (BaseFolderIcon) inflater.inflate(R.layout.gl_screen_folder_icon,
							null);
					mScreenFolderIcons.put(folderInfo.folderId, (GLScreenFolderIcon) folderIcon);
					GLAppFolderController.getInstance().addFolderInfo(folderInfo);
				}
				break;
			case GLAppFolderInfo.FOLDER_FROM_DOCK :
				folderIcon = mDockFolderIcons.get(folderInfo.folderId);
				if (folderIcon == null || folderIcon.isRecyled()) {
					folderIcon = (BaseFolderIcon) inflater.inflate(R.layout.gl_dock_folder_icon,
							null);
					mDockFolderIcons.put(folderInfo.folderId, (GLDockFolderIcon) folderIcon);
					GLAppFolderController.getInstance().addFolderInfo(folderInfo);
				}
				break;
			default :
				break;
		}
		// ADT-16234 dock栏存在一个只有一个图标的文件夹，桌面用一个图标挤压该文件夹至桌面，再用该文件夹挤压dock栏上刚挤压的图标至桌面，再用桌面其它文件夹挤压dock栏文件夹，桌面图标会重叠
		if (folderIcon != null && folderIcon.getLayoutParams() instanceof GLCellLayout.LayoutParams) {
			GLCellLayout.LayoutParams layoutParams = (GLCellLayout.LayoutParams) folderIcon
					.getLayoutParams();
			layoutParams.useTmpCoords = false;
		}
		return folderIcon;
	}

	public synchronized BaseFolderIcon getFolderIcon(GLAppFolderInfo folderInfo) {
		if (folderInfo == null) {
			Log.i("dzj", "getFolderIcon but folderInfo = null ");
			return null;
		}
		BaseFolderIcon folderIcon = createFolderIcon(folderInfo);
		initIconFromDeskSetting(folderIcon);
		folderIcon.setFolderInfo(folderInfo);
		return folderIcon;

	}
	
	
	public synchronized GLAppDrawerFolderIcon getFolderIcon(long id) {
		return mDrawerFolderIcons.get(id);
	}
	
	public synchronized BaseFolderIcon<?> removeFolderIcon(final GLAppFolderInfo folderInfo) {
		if (folderInfo != null) {
			switch (folderInfo.folderFrom) {
				case GLAppFolderInfo.FOLDER_FROM_APPDRAWER :
					return mDrawerFolderIcons.remove(folderInfo.folderId);
				case GLAppFolderInfo.FOLDER_FROM_SCREEN :
					return mScreenFolderIcons.remove(folderInfo.folderId);
				case GLAppFolderInfo.FOLDER_FROM_DOCK :
					return mDockFolderIcons.remove(folderInfo.folderId);
				default :
					break;
			}
		}
		return null;
	}

	public void setFolderStatusListener(FolderStatusListener listener) {
		mFolderMainView.setFolderStatusListener(listener);
	}

	public void addFolderViewAnimationListener(FolderViewAnimationListener animationListener,
			int viewID) {
		mFolderMainView.addFolderViewAnimationListener(animationListener, viewID);
	}

	public void removeFolderViewAnimationListener(int viewID) {
		mFolderMainView.removeFolderViewAnimationListener(viewID);
	}

	public boolean isFolderOpened() {
		return mFolderMainView.isFolderOpened();
	}

	public boolean isFolderOpened(long id) {
		return mFolderMainView.isFolderOpened() && getCurFolderIcon().getFolderInfo().folderId == id;
	}
	
	public BaseFolderIcon<?> getCurFolderIcon() {
		return mFolderMainView.getCurFolderIcon();
	}
	public boolean isFolderClosing() {
		return mFolderMainView.isClosing();
	}

	public void initIconFromDeskSetting(IconView view) {

		if (GoLauncherLogicProxy.getCustomTitleColor()) {
			int color = GoLauncherLogicProxy.getAppTitleColor();
			if (color != 0) {
				view.setTitleColor(color);
			} else {
				view.setTitleColor(Color.WHITE);
			}
		} else {
			DeskThemeControler themeControler = AppCore.getInstance().getDeskThemeControler();
			if (themeControler != null && themeControler.isUsedTheme()) {
				DeskThemeBean themeBean = themeControler.getDeskThemeBean();
				if (themeBean != null && themeBean.mScreen != null) {
					if (null != themeBean.mScreen.mFont) {
						int color = themeBean.mScreen.mFont.mColor;
						if (color == 0) {
							view.setTitleColor(Color.WHITE);
						} else {
							view.setTitleColor(color);
						}
					}
				}
			} else {
				view.setTitleColor(Color.WHITE);
			}
		}
		//		if (GoLauncher.getIsShowAppTitle()) {
		//			mTextView.setVisibility(GLView.VISIBLE);
		//		} else {
		//			mTextView.setVisibility(GLView.INVISIBLE);
		//		}

	}

	public int getFolderFrom() {
		return mFolderMainView.getFolderFrom();
	}

	private void batchStartIconEditEndAnimation() {
		if (!mIsFolderEditAnimating) {
			return;
		}
		Iterator<Long> iterator = mScreenFolderIcons.keySet().iterator();
		while (iterator.hasNext()) {
			Long id = (Long) iterator.next();
			GLScreenFolderIcon screenFolderIcon = mScreenFolderIcons.get(id);
			if (screenFolderIcon.getFolderChildCount() >= 4) {
				screenFolderIcon.endEditAnimation();
			}
		}

		Iterator<Long> dockIterator = mDockFolderIcons.keySet().iterator();
		while (dockIterator.hasNext()) {
			Long id = (Long) dockIterator.next();
			GLDockFolderIcon dockFolderIcon = mDockFolderIcons.get(id);
			if (dockFolderIcon.getFolderChildCount() >= 4) {
				dockFolderIcon.endEditAnimation();
			}
		}
		mIsFolderEditAnimating = false;
	}
	
	public void batchStartIconEditAnimation() {
		Iterator<Long> iterator = mScreenFolderIcons.keySet().iterator();
		while (iterator.hasNext()) {
			Long id = (Long) iterator.next();
			GLScreenFolderIcon screenFolderIcon = mScreenFolderIcons.get(id);
			if (screenFolderIcon.getFolderChildCount() >= 4) {
				screenFolderIcon.startEditAnimation();
				mIsFolderEditAnimating = true;
			}
		}

		Iterator<Long> dockIterator = mDockFolderIcons.keySet().iterator();
		while (dockIterator.hasNext()) {
			Long id = (Long) dockIterator.next();
			GLDockFolderIcon dockFolderIcon = mDockFolderIcons.get(id);
			if (dockFolderIcon.getFolderChildCount() >= 4) {
				dockFolderIcon.startEditAnimation();
				mIsFolderEditAnimating = true;
			}
		}
	}

	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		if (source instanceof GLSense || source instanceof GLAppFolderBaseGridView<?>
				|| info instanceof ScreenAppWidgetInfo || source instanceof GLAppDrawerBaseGrid
				|| info instanceof FavoriteInfo) {
			return;
		}
		batchStartIconEditAnimation();
	}
	
	public void setForceRefreshGLAppFolderGrid() {
		mFolderMainView.setForceRefreshGLAppFolderGrid();
	}
	
	@Override
	public void onDragEnd() {
		mFolderMainView.postDelayed(new Runnable() {

			@Override
			public void run() {
				batchStartIconEditEndAnimation();
			}
		}, 500);
	}
	
	public void refreshDrawerFolderIcons() {
		for (Long folderID : mDrawerFolderIcons.keySet()) {
			mDrawerFolderIcons.get(folderID).refreshIcon();
		}
	}

	public void clearScreenFolderIcons() {
		mScreenFolderIcons.clear();
	}

	public void clearDockFolderIcons() {
		mDockFolderIcons.clear();
	}

	public void clearDrawerFolderIcons() {
		mDrawerFolderIcons.clear();
	}
}
