package com.jiubang.shell.popupwindow.component.listmenu.folder;

import java.util.ArrayList;

import android.view.KeyEvent;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.appfunc.menu.BaseMenuItemInfo;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.shell.folder.GLAppFolderGridVIewContainer;
import com.jiubang.shell.folder.status.FolderStatusManager;
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
public class GLAppFolderGameModeMenu extends GLViewGroup
		implements
			com.go.gl.widget.GLAdapterView.OnItemClickListener,
			IPopupWindow {
	protected GLBaseListMenu mListMenu;
	protected GLAppFolderGridVIewContainer mFolderGridVIewContainer;
	
	public GLAppFolderGameModeMenu(GLAppFolderGridVIewContainer appFolderGridVIewContainer) {
		super(ShellAdmin.sShellManager.getContext());
		mFolderGridVIewContainer = appFolderGridVIewContainer;
		mListMenu = new GLBaseListMenu();
		addView(mListMenu, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mListMenu.setOnItemClickListener(this);
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

		BaseMenuItemInfo normalMenuItemInfo = new BaseMenuItemInfo();
		normalMenuItemInfo.mText = mContext
				.getString(R.string.app_folder_menu_game_mode_normal);
		baseMenuItemInfos.add(normalMenuItemInfo);
		BaseMenuItemInfo accMenuItemInfo = new BaseMenuItemInfo();
		accMenuItemInfo.mText = mContext
				.getString(R.string.app_folder_menu_game_mode_silent);
		baseMenuItemInfos.add(accMenuItemInfo);

		BaseMenuItemInfo powerSavingItemInfo = new BaseMenuItemInfo();
		powerSavingItemInfo.mText = mContext
				.getString(R.string.app_folder_menu_game_mode_power_saving);
		baseMenuItemInfos.add(powerSavingItemInfo);

		adapter.setItemList(baseMenuItemInfos);
		mListMenu.setMenuAdapter(adapter);
		//		}
		mListMenu.setItemPadding(DrawUtils.dip2px(9), 0, 0, 0);
		if (animate) {
			mListMenu.doShowAnimation(layer, 0.5f, 0f);
		}
	}

	@Override
	public void onExit(GLPopupWindowLayer layer, boolean animate) {
		if (animate) {
			mListMenu.doHideAnimation(layer, 0.5f, 0f);
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
		params.x =  0;
		int paddingTop = StatusBarHandler.isHide() ? 0 : StatusBarHandler.getStatusbarHeight();
		params.y = DrawUtils.dip2px(48) + paddingTop;
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
		int resID = -1;
		switch (position) {
			case 0 :
				FolderStatusManager.getInstance().changeGridStatus(
						FolderStatusManager.GRID_NORMAL_STATUS);
				break;
			case 1 :
				FolderStatusManager.getInstance().changeGridStatus(
						FolderStatusManager.FOLDER_SILENT_MODE_STATUS);
				resID = R.string.app_folder_game_mode_silent_tips;
				break;
			case 2 :
				FolderStatusManager.getInstance().changeGridStatus(
						FolderStatusManager.FOLDER_POWER_SAVING_MODE_STATUS);
				resID = R.string.app_folder_game_mode_power_saving_tips;
				break;
			default :
				break;
		}
		if (resID != -1) {
			DeskToast.makeText(mContext, resID, Toast.LENGTH_LONG).show();
		}
		ShellAdmin.sShellManager.getShell().getPopupWindowControler().dismiss(false);
	}
}
