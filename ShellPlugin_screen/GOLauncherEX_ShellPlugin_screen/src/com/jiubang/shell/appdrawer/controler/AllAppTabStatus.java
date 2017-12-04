package com.jiubang.shell.appdrawer.controler;

import java.util.ArrayList;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.Toast;

import com.gau.golauncherex.plugin.shell.R;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;
import com.jiubang.shell.IShell;
import com.jiubang.shell.appdrawer.IActionBar;
import com.jiubang.shell.appdrawer.component.GLAppDrawerBaseGrid;
import com.jiubang.shell.appdrawer.component.GLExtrusionGridView;
import com.jiubang.shell.common.component.GLActionBar;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.utils.ToastUtils;
/**
 * 所有应用tab的状态
 * @author wuziyi
 *
 */
public abstract class AllAppTabStatus extends Status {

	protected int mTopBarIdx;
	protected int mBottomBarIdx;
	protected ArrayList<IActionBar> mTopBarGroup;
	protected ArrayList<IActionBar> mBottomBarGroup;
	protected GLAppDrawerBaseGrid mGridView;

	public AllAppTabStatus(ArrayList<IActionBar> topBarGroup, GLAppDrawerBaseGrid gridView, ArrayList<IActionBar> bottomBarGroup) {
		mStatusManager = AppDrawerStatusManager.getInstance();
		mTopBarGroup = topBarGroup;
		mGridView = gridView;
		mBottomBarGroup = bottomBarGroup;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return false;
	}


	@Override
	public int getTabStatusID() {
		return AppDrawerStatusManager.ALLAPP_TAB;
	}

	@Override
	public void popupMenu() {
		// 前3次进入功能表的提示语句
		PreferencesManager manager = new PreferencesManager(ShellAdmin.sShellManager.getContext(),
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		int count = manager.getInt(IPreferencesIds.ALL_APP_MENU_SHOW_TIME_3D, 0);
		if (count < 3) {
			count++;
			manager.putInt(IPreferencesIds.ALL_APP_MENU_SHOW_TIME_3D, count);
			manager.commit();
			ToastUtils.showToast(R.string.appdrawer_goto_slide_menu, Toast.LENGTH_LONG);
		}
		
		ShellAdmin.sShellManager.getShell().getPopupWindowControler().showAllAppMenu(true);
	}

	@Override
	public void dismissMenu() {
		ShellAdmin.sShellManager.getShell().getPopupWindowControler().dismiss(false);
		MediaPluginFactory.getSwitchMenuControler().dismissMenu();
	}
	
	@Override
	public IActionBar getBottomBarViewByOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IActionBar getTopBarViewByOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<IActionBar> getBottomBarViewGroup() {
		return mBottomBarGroup;
	}

	@Override
	public ArrayList<IActionBar> getTopBarViewGroup() {
		return mTopBarGroup;
	}

	@Override
	public void setShell(IShell shell) {
		super.setShell(shell);
		for (IActionBar view : mTopBarGroup) {
			if (view instanceof GLActionBar) {
				((GLActionBar) view).setShell(shell);
			}
		}
		for (IActionBar view : mBottomBarGroup) {
			if (view instanceof GLActionBar) {
				((GLActionBar) view).setShell(shell);
			}
		}
	}

	@Override
	public void setGridView(GLExtrusionGridView gridView) {
		mGridView = (GLAppDrawerBaseGrid) gridView;
	}

	@Override
	public GLAppDrawerBaseGrid getGridView() {
		return mGridView;
	}
}
