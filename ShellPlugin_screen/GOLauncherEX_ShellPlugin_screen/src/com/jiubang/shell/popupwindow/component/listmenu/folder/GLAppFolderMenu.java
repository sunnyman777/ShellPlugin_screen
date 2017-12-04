package com.jiubang.shell.popupwindow.component.listmenu.folder;

import java.util.ArrayList;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.SettingProxy;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.appfunc.menu.BaseMenuItemInfo;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.pref.PrefConst;
import com.jiubang.ggheart.apps.desks.diy.pref.PrivatePreference;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.data.statistics.IGoLauncherUserBehaviorStatic;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderInfo;
import com.jiubang.shell.folder.GLAppFolderGridVIewContainer;
import com.jiubang.shell.folder.GLAppFolderMainView;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.popupwindow.IPopupWindow;
import com.jiubang.shell.popupwindow.component.GLPopupWindowLayer;
import com.jiubang.shell.popupwindow.component.GLPopupWindowLayer.PopupWindowLayoutParams;
import com.jiubang.shell.popupwindow.component.listmenu.GLBaseListMenu;
import com.jiubang.shell.popupwindow.component.listmenu.GLBaseMenuAdapter;
/**
 * 
 * @author dingzijian
 *
 */
public class GLAppFolderMenu extends GLViewGroup
		implements
			com.go.gl.widget.GLAdapterView.OnItemClickListener,
			IPopupWindow {
	protected GLBaseListMenu mListMenu;
	protected GLAppFolderGridVIewContainer mFolderGridVIewContainer;
	private PrivatePreference mPrivatePreference;
	
	public GLAppFolderMenu(GLAppFolderGridVIewContainer appFolderGridVIewContainer) {
		super(ShellAdmin.sShellManager.getContext());
		mFolderGridVIewContainer = appFolderGridVIewContainer;
		mListMenu = new GLBaseListMenu();
		addView(mListMenu, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mListMenu.setOnItemClickListener(this);
		mPrivatePreference = PrivatePreference.getPreference(ApplicationProxy.getContext());
	}
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		mListMenu.layout(0, 0, mWidth, mHeight);
	}
	@Override
	public void onEnter(GLPopupWindowLayer layer, boolean animate) {
		//		if (mListMenu.getAdapter() == null) {
		GLBaseMenuAdapter adapter = new GLBaseMenuAdapter(mContext);
		ArrayList<BaseMenuItemInfo> baseMenuItemInfos = new ArrayList<BaseMenuItemInfo>();

		BaseMenuItemInfo sortBaseMenuItemInfo = new BaseMenuItemInfo();
		sortBaseMenuItemInfo.mText = mContext
				.getString(R.string.app_folder_menu_sort);
		baseMenuItemInfos.add(sortBaseMenuItemInfo);
		if (mFolderGridVIewContainer.getCurrentFolderType() == GLAppFolderInfo.TYPE_RECOMMAND_GAME) {
			BaseMenuItemInfo gameAccMenuItemInfo = new BaseMenuItemInfo();
			boolean gameAcc = mPrivatePreference.getBoolean(
					PrefConst.KEY_GAME_FOLDER_ACCELERATE_SWITCH, true);
			gameAccMenuItemInfo.mText = gameAcc
					? mContext
							.getString(R.string.app_folder_menu_game_acc_off)
					: mContext
							.getString(R.string.app_folder_menu_game_acc_on);
			baseMenuItemInfos.add(gameAccMenuItemInfo);
		}
		adapter.setItemList(baseMenuItemInfos);
		mListMenu.setMenuAdapter(adapter);
		//		}
		mListMenu.setItemPadding(DrawUtils.dip2px(9), 0, 0, 0);
		if (animate) {
			mListMenu.doShowAnimation(layer, false);
		}
	}

	@Override
	public void onExit(GLPopupWindowLayer layer, boolean animate) {
		if (animate) {
			mListMenu.doHideAnimation(layer, false);
		}
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = 0;
		int statusBarHeight = StatusBarHandler.isHide() ? 0 : StatusBarHandler.getStatusbarHeight();
		boolean isPortait = GoLauncherActivityProxy.isPortait();
		if (isPortait) {
			width = (int) (GoLauncherActivityProxy.getScreenWidth() / 2.2);
		} else {
			width = (int) (GoLauncherActivityProxy.getScreenWidth() / 3.5);
		}
		int listWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
		int listHeightSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec)
				- statusBarHeight, MeasureSpec.AT_MOST);
		mListMenu.measure(listWidthSpec, listHeightSpec);
		int listMeasureWidth = mListMenu.getMeasuredWidth();
		int listMeasureHeight = mListMenu.getMeasuredHeight();
		setMeasuredDimension(MeasureSpec.makeMeasureSpec(listMeasureWidth, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(listMeasureHeight, MeasureSpec.EXACTLY));
		PopupWindowLayoutParams params = new PopupWindowLayoutParams(listMeasureWidth,
				listMeasureHeight);
		params.x = ShellAdmin.sShellManager.getShell().getContainer().getWidth() - listMeasureWidth;
		int paddingTop = StatusBarHandler.isHide() ? 0 : StatusBarHandler.getStatusbarHeight();
		params.y = DrawUtils.dip2px(38) + paddingTop;
		setLayoutParams(params);
	}
	@Override
	public void onWithEnter(boolean animate) {

	}

	@Override
	public void onWithExit(boolean animate) {

	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		 return mListMenu.onKeyUp(keyCode, event);
	}
	
	@Override
	public void onItemClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		switch (position) {
			case 0 :
				if (SettingProxy.getScreenSettingInfo().mLockScreen) { // 判断当前是否锁屏
					LockScreenHandler.showLockScreenNotification(ShellAdmin.sShellManager
							.getActivity());
					return;
				}
				mFolderGridVIewContainer.showSortDialog();
				GuiThemeStatistics
						.goLauncherUserBehaviorStaticDataCache(IGoLauncherUserBehaviorStatic.FOLDER_OPEN_02);
				break;
			case 1 :
				final boolean gameAcc = mPrivatePreference.getBoolean(
						PrefConst.KEY_GAME_FOLDER_ACCELERATE_SWITCH, true);
				if (gameAcc) {
					final DialogConfirm dialogConfirm = new DialogConfirm(
							ShellAdmin.sShellManager.getActivity());
					dialogConfirm.show();
					dialogConfirm.setTitle(R.string.app_folder_menu_game_acc_off);
					dialogConfirm.setMessage(R.string.app_folder_game_acc_dialog_message);
					dialogConfirm.setNegativeButton(R.string.cancel, new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							dialogConfirm.dismiss();
						}
					});

					dialogConfirm.setPositiveButton(R.string.ok, new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							mPrivatePreference.putBoolean(
									PrefConst.KEY_GAME_FOLDER_ACCELERATE_SWITCH, !gameAcc);
							mPrivatePreference.commit();
							GLAppFolderMainView folderMainView = (GLAppFolderMainView) mFolderGridVIewContainer
									.getGLParent().getGLParent();
							folderMainView.setGameFolderUI(true);
						}
					});
				} else {
					mPrivatePreference.putBoolean(PrefConst.KEY_GAME_FOLDER_ACCELERATE_SWITCH,
							!gameAcc);
					mPrivatePreference.commit();
					GLAppFolderMainView folderMainView = (GLAppFolderMainView) mFolderGridVIewContainer
							.getGLParent().getGLParent();
					folderMainView.setGameFolderUI(true);
				}
				break;
			default :
				break;
		}
		ShellAdmin.sShellManager.getShell().getPopupWindowControler().dismiss(false);
	}
}
