package com.jiubang.shell.popupwindow.component.listmenu.appdrawer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.util.graphics.DrawUtils;
import com.golauncher.message.IAppDrawerMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogSingleChoice;
import com.jiubang.ggheart.apps.desks.appfunc.menu.AllAppMenuControler;
import com.jiubang.ggheart.apps.desks.appfunc.menu.AppFuncAllAppMenuItemInfo;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.common.controler.CommonControler;
import com.jiubang.ggheart.components.SingleChoiceDialog;
import com.jiubang.ggheart.plugin.shell.folder.GLDrawerFolderModifyActivity;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.popupwindow.component.GLPopupWindowLayer;
import com.jiubang.shell.utils.ToastUtils;

/**
 * 3D功能表所有程序菜单
 *
 */
public class GLAllAppMenu extends GLAppDrawerBaseMenu {
	private AllAppMenuControler mControler;
	
	public GLAllAppMenu() {
		super();
		mControler = AllAppMenuControler.getInstance();
	}
	
	@Override
	public void onItemClick(GLAdapterView<?> arg0, GLView arg1, int position, long arg3) {
		AppFuncAllAppMenuItemInfo itemInfo = (AppFuncAllAppMenuItemInfo) mListMenu.getAdapter()
				.getItem(position);
		int actionId = itemInfo.mActionId;
		if (!mControler.handleItemClickEvent(actionId)) {
			switch (actionId) {
				case AppFuncAllAppMenuItemInfo.ACTION_SORT_ICON :
					showSelectSort();
					break;
				case AppFuncAllAppMenuItemInfo.ACTION_CREATE_NEW_FOLDER :
						Intent newFolderIntent = new Intent(mActivity,
								GLDrawerFolderModifyActivity.class);
						newFolderIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						ShellAdmin.sShellManager.getShell().startActivitySafely(newFolderIntent);
					break;
				case AppFuncAllAppMenuItemInfo.ACTION_APP_ARRANGE_APP :
					if (CommonControler.getInstance(mActivity).isAppClassifyLoadFinish()) {
						showArrangeAppChoiceDialog();
					} else {
						ToastUtils.showToast(R.string.app_classify_not_ready, Toast.LENGTH_SHORT);
					}
					//用户行为统计
//					StatisticsData.countUserActionData(StatisticsData.FUNC_ACTION_ID_APPLICATION,
//							StatisticsData.USER_ACTION_TEN, IPreferencesIds.APP_FUNC_ACTION_DATA);
					break;
				default :
					break;
			}
		}
		super.onItemClick(arg0, arg1, position, arg3);
	}
	
	private void showSelectSort() {
		int selectedItem = SettingProxy.getFunAppSetting().getSortType();

		try {
			DialogSingleChoice mDialog = new DialogSingleChoice(mActivity);
			mDialog.show();
			mDialog.setTitle(ShellAdmin.sShellManager.getContext().getString(R.string.dlg_sortChangeTitle));
			final CharSequence[] items = ShellAdmin.sShellManager.getContext().getResources().getTextArray(
					R.array.select_sort_style);
			mDialog.setItemData(items, selectedItem, true);
			mDialog.setOnItemClickListener(new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					// 开始排序
					SettingProxy.getFunAppSetting().setSortType(item, false);
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
							IAppDrawerMsgId.APPDRAWER_START_SORT, item);
				}
			});
		} catch (Exception e) {
			try {
				ToastUtils.showToast(R.string.alerDialog_error, Toast.LENGTH_SHORT);
			} catch (OutOfMemoryError error) {
				OutOfMemoryHandler.handle();
			}
		}
	}

	private void showArrangeAppChoiceDialog() {
		try {
			boolean enableRecover = true;
			PreferencesManager pManager = new PreferencesManager(mActivity,
					IPreferencesIds.PREFERENCE_APPDRAW_ARRANGE_CONFG,
					Context.MODE_PRIVATE);
			String folderId = pManager.getString(
					IPreferencesIds.PREFERENCE_APPDRAW_ARRANGE_FOLDERS_ID, "");
			if (folderId.trim().equals("")
					&& !AppDrawerControler.getInstance(ApplicationProxy.getContext())
							.checkSupportAppTableRestore()) {
				enableRecover = false;
			}
			String[] items = {
					ShellAdmin.sShellManager.getContext().getString(
							R.string.appdraw_arrange_dialog_item_arrange),
					ShellAdmin.sShellManager.getContext().getString(
							R.string.appdraw_arrange_dialog_item_revcover) };
			String[] summary = {
					ShellAdmin.sShellManager.getContext().getString(
							R.string.appdraw_arrange_dialog_item_arrange_content),
					ShellAdmin.sShellManager.getContext().getString(
							R.string.appdraw_arrange_dialog_item_revcover_content) };
			Drawable[] images = {
					mContext.getResources().getDrawable(R.drawable.gl_appdrawer_arrange),
					mContext.getResources().getDrawable(R.drawable.gl_appdrawer_arrange_restore) };
			final SingleChoiceDialog dialog = new SingleChoiceDialog(mActivity);
			if (!enableRecover) {
				dialog.setDisableItemPosition(1);
			}
			dialog.show();
			dialog.setItemMinHeight(DrawUtils.dip2px(70));
			dialog.setTitle(ShellAdmin.sShellManager.getContext().getString(
					R.string.appdraw_arrange_dialog_title));
			dialog.setItemData(items, summary, images, 0, false);
			dialog.setOnItemClickListener(new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == 0) {
						MsgMgrProxy.sendMessage(GLAllAppMenu.this,
								IDiyFrameIds.APP_DRAWER,
								IAppDrawerMsgId.APPDRAWER_ARRANGE_APP, 0);
					} else {
						MsgMgrProxy.sendMessage(GLAllAppMenu.this,
								IDiyFrameIds.APP_DRAWER,
								IAppDrawerMsgId.APPDRAWER_ARRANGE_APP, 1);
					}
				}
			});
		} catch (Exception e) {
			try {
				ToastUtils.showToast(R.string.alerDialog_error, Toast.LENGTH_SHORT);
				e.printStackTrace();
			} catch (OutOfMemoryError error) {
				OutOfMemoryHandler.handle();
			}
		}
	}
	
	@Override
	public void onEnter(final GLPopupWindowLayer layer, boolean animate) {
		if (mListMenu.getAdapter() == null) {
			mListMenu.setMenuAdapter(new GLAllAppMenuAdapter(mActivity, mControler
					.getMenuItemResource()));
		}
		mListMenu.setItemPadding(DrawUtils.dip2px(9), 0, 0, 0);
		super.onEnter(layer, animate);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_ALL_APP_TOP_ACTION_BAR,
				IAppDrawerMsgId.APPDRAWER_ALL_APP_MENU_BE_OPEN, -1);
	}
}
