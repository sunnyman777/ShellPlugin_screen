package com.jiubang.shell.folder;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;

import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.util.ConvertUtils;
import com.go.util.SortHelper;
import com.go.util.graphics.effector.united.IEffectorIds;
import com.go.util.sort.CompareMethod;
import com.go.util.sort.ComparePriorityMethod;
import com.go.util.sort.CompareTimeInFolder;
import com.go.util.sort.CompareTitleMethod;
import com.go.util.sort.IBaseCompareable;
import com.go.util.sort.IPriorityLvCompareable;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IFolderMsgId;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.appfunc.setting.AppFuncAutoFitManager;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogSingleChoice;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUninstallHelper.ActiveNotFoundCallBack;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.IBackgroundInfoChangedObserver;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunItemInfo;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderController;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.appdrawer.component.GLAppDrawerAppIcon;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.common.component.GLModel3DMultiView;
import com.jiubang.shell.common.component.HorScrollableGridViewHandler;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.listener.UninstallListener;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.drag.DropTarget;
import com.jiubang.shell.folder.adapter.GLAppDrawerFolderAdapter;
import com.jiubang.shell.folder.status.FolderStatusManager;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.model.IModelState;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-3-11]
 */
public class GLAppDrawerFolderGridView extends GLAppFolderBaseGridView<GLAppDrawerFolderIcon>
		implements
			IBackgroundInfoChangedObserver,
			ActiveNotFoundCallBack {

	protected FuncAppDataHandler mFuncAppDataHandler;
	protected AppFuncUtils mUtils;
	protected FunAppSetting mSetting;
	
	public GLAppDrawerFolderGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GLAppDrawerFolderGridView(Context context) {
		super(context);
	}
	@Override
	protected void init() {
		super.init();
		mFuncAppDataHandler = FuncAppDataHandler
				.getInstance(ShellAdmin.sShellManager.getActivity());
		mFuncAppDataHandler.registerBgInfoChangeObserver(this);
		mSetting = SettingProxy.getFunAppSetting();
		mUtils = AppFuncUtils.getInstance(ShellAdmin.sShellManager.getActivity());
		handleRowColumnSetting(false);
		handleScrollerSetting();
	}
	@Override
	public ShellBaseAdapter createAdapter(Context context, List infoList) {
		return new GLAppDrawerFolderAdapter(context, infoList);
	}
	
	@Override
	protected void handleScrollerSetting() {
		super.handleScrollerSetting();
		int effectType = FuncAppDataHandler.getInstance(ShellAdmin.sShellManager.getActivity())
				.getIconEffect();
		if (effectType == IEffectorIds.EFFECTOR_TYPE_RANDOM_CUSTOM) {
			// 如果是随机自定义的特效
			FunAppSetting setting = null;
			if ((setting = SettingProxy.getFunAppSetting()) != null) {
				int[] effects = setting.getAppIconCustomRandomEffect();
				((HorScrollableGridViewHandler) mScrollableHandler).setEffectType(effects);
			}
		} else {
			// 一般特效
			((HorScrollableGridViewHandler) mScrollableHandler).setEffectType(effectType);
		}
		boolean cycle = mFuncAppDataHandler.getScrollLoop() == FunAppSetting.ON;
		mScrollableHandler.setCycleScreenMode(cycle);
	}

	@Override
	protected void handleRowColumnSetting(boolean updateDB) {
		int standard = mFuncAppDataHandler.getStandard();
		setGridRowsAndColumns(standard);
		super.handleRowColumnSetting(updateDB);
	}
//	@Override
//	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//		if (changed) {
//			handleRowColumnSetting(false);
//		}
//		super.onLayout(changed, left, top, right, bottom);
//	}
	@Override
	public void onItemClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		mStatus.onClickUnderStatus(parent, view, position, id);
	}
	@Override
	public boolean onItemLongClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		super.onItemLongClick(parent, view, position, id);
		mStatus.onLongClickUnderStatus(parent, view, position, id);
		return true;
	}

	public void setGridRowsAndColumns(int standard) {
		int smallerBound = mUtils.getSmallerBound();
		int column = 0;
		switch (standard) {
			case FunAppSetting.LINECOLUMNNUMXY_SPARSE : {
				if (GoLauncherActivityProxy.isPortait()) {
					if (smallerBound <= 240) {
						column += 4;
					} else {
						column += 4;
					}
					if (mNumColumns != column) {
						mNumColumns = column;
						return;
					}
				} else {
					if (smallerBound <= 240) {
						column += 4;
					} else {
						column += 5;
					}
					if (mNumColumns != column) {
						mNumColumns = column;
						return;
					}
				}
				break;
			}
			case FunAppSetting.LINECOLUMNNUMXY_MIDDLE : {
				if (GoLauncherActivityProxy.isPortait()) {
					if (smallerBound <= 240) {
						column += 4;
					} else {
						column += 5;
					}
					if (mNumColumns != column) {
						mNumColumns = column;
						return;
					}
				} else {
					if (smallerBound <= 240) {
						column += 5;
					} else {
						column += 6;
					}
					if (mNumColumns != column) {
						mNumColumns = column;
						return;
					}
				}
				break;
			}
			case FunAppSetting.LINECOLUMNNUMXY_MIDDLE_2 : {
				if (GoLauncherActivityProxy.isPortait()) {
					if (smallerBound <= 240) {
						column += 4;
					} else {
						column += 4;
					}
					if (mNumColumns != column) {
						mNumColumns = column;
						return;
					}
				} else {
					if (smallerBound <= 240) {
						column += 5;
					} else {
						column += 6;
					}
					if (mNumColumns != column) {
						mNumColumns = column;
						return;
					}
				}
				break;
			}
			case FunAppSetting.LINECOLUMNNUMXY_THICK : {
				if (GoLauncherActivityProxy.isPortait()) {
					if (smallerBound <= 240) {
						column += 5;
					} else {
						column += 5;
					}
					if (mNumColumns != column) {
						mNumColumns = column;
						return;
					}
				} else {
					if (smallerBound <= 240) {
						column += 5;

					} else {
						column += 6;

					}
					if (mNumColumns != column) {
						mNumColumns = column;
						return;
					}
				}
				break;
			}
			case FunAppSetting.LINECOLUMNNUMXY_DIY : {

				if (GoLauncherActivityProxy.isPortait()) {
					mNumColumns = mSetting.getColNum();
				} else {
					mNumColumns = mSetting.getRowNum();
				}

				return;
			}
			case FunAppSetting.LINECOLUMNNUMXY_AUTO_FIT : {
				AppFuncAutoFitManager autoFitMgr = AppFuncAutoFitManager
						.getInstance(ShellAdmin.sShellManager.getActivity());
				if (GoLauncherActivityProxy.isPortait()) {
					mNumColumns = autoFitMgr.getAppDrawerColumnsV();
				} else {
					mNumColumns = autoFitMgr.getAppDrawerColumnsH();
				}

				break;
			}
			default : {
				if (GoLauncherActivityProxy.isPortait()) {
					if (smallerBound <= 240) {
						column += 4;
					} else {
						column += 4;
					}
					if (mNumColumns != column) {
						mNumColumns = column;
						return;
					}
				} else {
					if (smallerBound <= 240) {
						column += 4;
					} else {
						column += 5;
					}
					if (mNumColumns != column) {
						mNumColumns = column;
						return;
					}
				}
				break;
			}
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return mStatus.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return mStatus.onKeyUp(keyCode, event);
	}

//	@Override
//	public void dataChangeOnMoveStart(Object dragInfo, int targetIndex, int sourceIndex) {
//			super.dataChangeOnMoveStart(dragInfo, targetIndex, sourceIndex);
//			AppDrawerControler.getInstance(mContext).refreshFolderBarTarget(mFolderIcon.getInfo());
//	}
	
	@Override
	public void dataChangeOnMoveEnd(Object dragInfo, int targetIndex, int sourceIndex) {
		super.dataChangeOnMoveEnd(dragInfo, targetIndex, sourceIndex);
	}

	@Override
	public void dataChangeOnDrop(Object dragInfo, int targetIndex, int sourceIndex) {
		super.dataChangeOnDrop(dragInfo, targetIndex, sourceIndex);
		AppDrawerControler.getInstance(ShellAdmin.sShellManager.getActivity())
		.refreshFolderBarTarget(mFolderIcon.getInfo());
		//更新数据库
		if (targetIndex != sourceIndex) {
			GLAppFolderController.getInstance().moveAppDrawerFolderInnerItem(mFolderIcon.getInfo(),
					sourceIndex, targetIndex);
		}
	}
	@Override
	public void onDropCompleted(DropTarget target, Object dragInfo, boolean success,
			DropAnimationInfo resetInfo) {
		super.onDropCompleted(target, dragInfo, success, resetInfo);
		Log.i("dzj", "AppDrawerFolder_onDropCompleted");
		mFolderIcon.setFolderThumbnailVisible(true);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
				IFolderMsgId.ON_FOLDER_DROP_COMPELETE, -1, target, dragInfo, success, resetInfo,
				mFolderIcon.getInfo().getFolderId());
	}
	
	@Override
	public boolean onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo,
			DropAnimationInfo resetInfo) {
		// TODO Auto-generated method stub
		resetInfo.setNeedToShowCircle(false);
		return super.onDrop(source, x, y, xOffset, yOffset, dragView, dragInfo,
				resetInfo);
	}
	/**
	 * 检查Normal状态下图标的当前状态
	 */
	public void checkNormalIconStatus() {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			GLView viewItem = getChildAt(i);
			if (viewItem instanceof GLAppDrawerAppIcon) {
				GLAppDrawerAppIcon icon = (GLAppDrawerAppIcon) viewItem;
				icon.checkSingleIconNormalStatus();
				icon.stopShake();
			}
		}
	}
	
	public void changeIconToShake() {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			GLView viewItem = getChildAt(i);
			if (viewItem instanceof GLAppDrawerAppIcon) {
				GLAppDrawerAppIcon icon = (GLAppDrawerAppIcon) viewItem;
				FunAppItemInfo info = icon.getInfo();
				GLModel3DMultiView multiView = icon.getMultiView();
				if (!info.isSysApp()) {
					multiView.setCurrenState(IModelState.UNINSTALL_STATE);
					multiView.setOnSelectClickListener(new UninstallListener(info.getAppItemInfo(),
							this));
				} else {
					multiView.setCurrenState(IModelState.NO_STATE);
					multiView.setOnSelectClickListener(null);
				}
				icon.startShake();
			}
		}
	}
	/**
	 * 处理显示应用程序名改变
	 */
	@SuppressWarnings("rawtypes")
	public void handleShowAppNameChange() {
		int count = mAdapter.getCount();
		boolean isShowName = (mFuncAppDataHandler.getShowName() < FunAppSetting.APPNAMEVISIABLEYES)
				? false
				: true;
		for (int i = 0; i < count; i++) {
			IconView iconView = (IconView) getViewAtPosition(i);
			if (iconView != null) {
				iconView.setEnableAppName(isShowName);
			}
		}
	}
	
	@Override
	public void onGirdStatusChange(int gridStatusId) {
		handleIconShake(gridStatusId);
	}
	
	private void handleIconShake(int gridStatusId) {
		switch (gridStatusId) {
			case FolderStatusManager.GRID_NORMAL_STATUS :
				checkNormalIconStatus();
				break;
			case FolderStatusManager.GRID_EDIT_STATUS :
				changeIconToShake();
			default :
				break;
		}
	}
	
	@Override
	protected void onVisibilityChanged(GLView changedView, int visibility) {
		if (changedView instanceof GLAppFolderMainView) {
		super.onVisibilityChanged(changedView, visibility);
		switch (visibility) {
			case GLView.VISIBLE :
					GLAppFolder.getInstance().addFolderViewAnimationListener(this,
							IViewId.APP_FOLDER);
				break;
			case GLView.GONE :
			case GLView.INVISIBLE :
				GLAppFolder.getInstance().removeFolderViewAnimationListener(IViewId.APP_FOLDER);
				break;
			default :
				break;
		}
	}
	}
	
	@Override
	public void onFolderNameChange(String name) {
		mFolderIcon.getInfo().setIconTitle(name);
		mFolderIcon.setTitle(name);
		mFolderIcon.rebuildIconCache();
		mController.updateDrawerFoldetTitle(mFolderIcon.getFolderInfo().folderId, name);
	}

	@Override
	public void showSortDialog() {
		DialogSingleChoice sortDialog = createSortDialog();
		sortDialog.setOnItemClickListener(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				mPositionChange = true;
				List<FunAppItemInfo> folderContent = mFolderIcon.getInfo().getFolderContent();
				CompareMethod<IPriorityLvCompareable> method = new ComparePriorityMethod();
				CompareMethod<? extends IBaseCompareable> nextMethod = null;
				switch (item) {
					case SORTTYPE_LETTER :
						nextMethod = new CompareTitleMethod();
						break;
					case SORTTYPE_TIMENEAR :
						nextMethod = new CompareTimeInFolder();
						nextMethod.setOrder(CompareMethod.DESC);
						break;
					case SORTTYPE_TIMEREMOTE :
						nextMethod = new CompareTimeInFolder();
						break;
					default :
						break;
				}
				method.setNextMethod(nextMethod);
				SortHelper.doSort(folderContent, method);
				setData(folderContent);
				FunItemInfo funItemInfo = null;
				for (int i = 0; i < folderContent.size(); ++i) {
					funItemInfo = folderContent.get(i);
					if (null == funItemInfo) {
						continue;
					}
					funItemInfo.setIndex(i);
				}
				mController.updateDrawerFolderIndex(mFolderIcon.getInfo().getFolderId(), folderContent);
				mFolderIcon.refreshIcon();
				AppDrawerControler.getInstance(ShellAdmin.sShellManager.getActivity())
						.refreshFolderBarTarget(mFolderIcon.getInfo());
			}
		});
	}
	@Override
	public void onFolderOpenEnd(int curStatus) {
		switch (curStatus) {
			case FolderStatusManager.GRID_EDIT_STATUS :
				changeIconToShake();
				break;

			default :
				break;
		}
		super.onFolderOpenEnd(curStatus);
	}
//	@Override
//	public void refreshGridView() {
//		setData(mFolderIcon.getFolderInfo().getAppDrawerFolderInfo().getFolderContent());
//		mFolderIcon.refreshIcon();
//	}
//
//	@Override
//	public int getContentSize() {
//		return mFolderIcon.getInfo().getFolderSize();
//	}

	@Override
	public boolean handleChanges(MessageID msgId, Object obj1, Object obj2) {
		switch (msgId) {
			case STANDARD_CHANGED : // 行列数改变
				handleRowColumnSetting(false);
				break;
			case ICONEFFECT_CHANGED : // 横向特效改变
			case SCROLL_LOOP_CHANGED : // 循环模式改变
				handleScrollerSetting();
				break;
			default :
				break;
		}
		return false;
	}

	@Override
	public void onFolderContentUninstall(ArrayList<AppItemInfo> uninstallapps) {
		GLAppDrawerFolderAdapter adapter = (GLAppDrawerFolderAdapter) getAdapter();
		for (AppItemInfo appItemInfo : uninstallapps) {
			for (int i = 0; i < adapter.getCount(); i++) {
				FunAppItemInfo funAppItemInfo = (FunAppItemInfo) adapter.getItem(i);
				if (null != funAppItemInfo
						&& ConvertUtils.intentCompare(appItemInfo.mIntent,
								funAppItemInfo.getIntent())) {
					//					adapter.remove(funAppItemInfo);
					clearDeletedView(funAppItemInfo);
					AppDrawerControler.getInstance(ShellAdmin.sShellManager.getActivity())
							.refreshFolderBarTarget(mFolderIcon.getInfo());
					//					mController.removeAppFromDrawerFolder(mFolderIcon.getFolderInfo().folderId,
					//							funAppItemInfo.getIntent());
					break;
				}
			}
		}
		if (getChildCount() <= 0) {
			mFolderIcon.closeFolder(true);
			return;
		}
		mFolderIcon.refreshIcon();
	}

	@Override
	public void onFolderLocateApp(Intent intent) {
		GLAppDrawerFolderAdapter adapter = (GLAppDrawerFolderAdapter) getAdapter();
		for (int i = 0; i < adapter.getCount(); i++) {
			FunAppItemInfo funAppItemInfo = (FunAppItemInfo) adapter.getItem(i);
			if (null != funAppItemInfo
					&& ConvertUtils.intentCompare(intent, funAppItemInfo.getIntent())) {
				scrollToTargetItem(i);
				IconView focusAppIcon = (IconView) getViewAtPosition(i);
				focusAppIcon.startLocatAppAnimation();
				break;
			}
		}
		super.onFolderLocateApp(intent);
	}
	
	@Override
	public void callBackToChild(GLView view) {
		super.callBackToChild(view);
		handleIconShake(mStatus.getGridStatusID());
	}
	
	@Override
	protected int getLongPressTimeout() {
		if (mStatus.getGridStatusID() == FolderStatusManager.GRID_EDIT_STATUS) {
			return QUICK_LONG_PRESS_TIMEOUT;
		}
		return super.getLongPressTimeout();
	}

	@Override
	public void noActiveCallBack(Intent intent) {
		//mId : 1029141
		//邮件标题 : GOLauncherEX v4.05beta Fix Report 76706Ошибка
		if (intent == null) {
			return;
		}
		
		clearDeletedView(mFolderIcon.getInfo().getFunAppItemInfo(intent), false);
		if (getChildCount() <= 0) {
			mFolderIcon.closeFolder(true);
		}
		mController.removeAppFromDrawerFolder(mFolderIcon.getFolderInfo().folderId, intent, true,
				GLAppFolderController.REMOVE_FOLDER_LESS_ONE);
		mFolderIcon.refreshIcon();
		AppDrawerControler.getInstance(ShellAdmin.sShellManager.getActivity())
				.refreshFolderBarTarget(mFolderIcon.getInfo());
		requestLayout();
	}

	@Override
	protected void onScreenChange(int newScreen, int oldScreen) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onScrollStart() {
		super.onScrollStart();
		IconView.sEnableStateAnimation = false;
	}
	
	@Override
	protected void onScrollFinish() {
		super.onScrollFinish();
		IconView.sEnableStateAnimation = true;
	}
}
