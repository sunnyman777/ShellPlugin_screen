package com.jiubang.shell.appdrawer.controler;

import java.util.ArrayList;

import android.content.Context;
import android.util.SparseArray;

import com.jiubang.shell.appdrawer.IActionBar;
import com.jiubang.shell.appdrawer.allapp.GLAllAppGridView;
import com.jiubang.shell.appdrawer.allapp.actionbar.GLAllAppBottomActionBar;
import com.jiubang.shell.appdrawer.allapp.actionbar.GLAllAppTopActionBar;
import com.jiubang.shell.appdrawer.allapp.actionbar.GLFolderActionBar;
import com.jiubang.shell.appdrawer.allapp.actionbar.GLPreviewBar;
import com.jiubang.shell.appdrawer.component.GLAppDrawerBaseGrid;
import com.jiubang.shell.appdrawer.component.GLExtrusionGridView;
import com.jiubang.shell.folder.GLAppDrawerFolderGridView;
import com.jiubang.shell.folder.GLAppFolderBaseGridView;
import com.jiubang.shell.folder.GLDockFolderGridVIew;
import com.jiubang.shell.folder.GLScreenFolderGridView;
import com.jiubang.shell.folder.status.DrawerFolderEditStatus;
import com.jiubang.shell.folder.status.DrawerFolderNormalStatus;
import com.jiubang.shell.folder.status.DrawerFolderPowerSavingModeStatus;
import com.jiubang.shell.folder.status.DrawerFolderSilentModeStatus;
import com.jiubang.shell.folder.status.FolderStatusManager;
import com.jiubang.shell.folder.status.ScreenFolderNormalStatus;
import com.jiubang.shell.folder.status.ScreenFolderPowerSavingModeStatus;
import com.jiubang.shell.folder.status.ScreenFolderSilentModeStatus;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * tab状态类工厂
 * @author wuziyi
 *
 */
public class StatusFactory {

	public Status createStatus(int statusID) {
		Status status = null;
		switch (statusID) {
			case AppDrawerStatusManager.ALLAPP_TAB | AppDrawerStatusManager.GRID_NORMAL_STATUS :
				status = new AllAppNormalStatus(
						ActionBarFactory.getTopBarGroup(AppDrawerStatusManager.ALLAPP_TAB),
						(GLAppDrawerBaseGrid) GridViewFactory.getGridView(AppDrawerStatusManager.ALLAPP_TAB),
						ActionBarFactory.getBottomBarGroup(AppDrawerStatusManager.ALLAPP_TAB));
				break;
			case AppDrawerStatusManager.ALLAPP_TAB | AppDrawerStatusManager.GRID_EDIT_STATUS :
				status = new AllAppEditStatus(
						ActionBarFactory.getTopBarGroup(AppDrawerStatusManager.ALLAPP_TAB),
						(GLAppDrawerBaseGrid) GridViewFactory.getGridView(AppDrawerStatusManager.ALLAPP_TAB),
						ActionBarFactory.getBottomBarGroup(AppDrawerStatusManager.ALLAPP_TAB));
				break;
//			case AppDrawerStatusManager.RECENTAPP_TAB | AppDrawerStatusManager.GRID_NORMAL_STATUS :
//				status = new RecentAppNormalStatus(
//						ActionBarFactory.getTopBarGroup(AppDrawerStatusManager.RECENTAPP_TAB),
//						(GLAppDrawerBaseGrid) GridViewFactory.getGridView(AppDrawerStatusManager.RECENTAPP_TAB),
//						ActionBarFactory.getBottomBarGroup(AppDrawerStatusManager.RECENTAPP_TAB));
//				break;
//			case AppDrawerStatusManager.PROMANAGER_TAB | AppDrawerStatusManager.GRID_NORMAL_STATUS :
//				status = new ProManageNormalStatus(
//						ActionBarFactory.getTopBarGroup(AppDrawerStatusManager.PROMANAGER_TAB),
//						(GLAppDrawerBaseGrid) GridViewFactory.getGridView(AppDrawerStatusManager.PROMANAGER_TAB),
//						ActionBarFactory.getBottomBarGroup(AppDrawerStatusManager.PROMANAGER_TAB));
//				break;
//			case AppDrawerStatusManager.PROMANAGER_TAB | AppDrawerStatusManager.GRID_EDIT_STATUS :
//				status = new ProManageEditStatus(
//						ActionBarFactory.getTopBarGroup(AppDrawerStatusManager.PROMANAGER_TAB),
//						(GLAppDrawerBaseGrid) GridViewFactory.getGridView(AppDrawerStatusManager.PROMANAGER_TAB),
//						ActionBarFactory.getBottomBarGroup(AppDrawerStatusManager.PROMANAGER_TAB));
//				break;
			case FolderStatusManager.DRAWER_FOLDER_GRID | FolderStatusManager.GRID_NORMAL_STATUS :
				status = new DrawerFolderNormalStatus(
						(GLAppFolderBaseGridView<?>) GridViewFactory
								.getGridView(FolderStatusManager.DRAWER_FOLDER_GRID));
				break;
			case FolderStatusManager.DRAWER_FOLDER_GRID | FolderStatusManager.GRID_EDIT_STATUS :
				status = new DrawerFolderEditStatus(
						(GLAppFolderBaseGridView<?>) GridViewFactory
						.getGridView(FolderStatusManager.DRAWER_FOLDER_GRID));
				break;
			case FolderStatusManager.DRAWER_FOLDER_GRID | FolderStatusManager.FOLDER_SILENT_MODE_STATUS :
				status = new DrawerFolderSilentModeStatus(
						(GLAppFolderBaseGridView<?>) GridViewFactory
								.getGridView(FolderStatusManager.DRAWER_FOLDER_GRID));
				break;
			case FolderStatusManager.DRAWER_FOLDER_GRID | FolderStatusManager.FOLDER_POWER_SAVING_MODE_STATUS :
				status = new DrawerFolderPowerSavingModeStatus(
						(GLAppFolderBaseGridView<?>) GridViewFactory
								.getGridView(FolderStatusManager.DRAWER_FOLDER_GRID));
				break;
			case FolderStatusManager.SCREEN_FOLDER_GRID | FolderStatusManager.GRID_NORMAL_STATUS :
				status = new ScreenFolderNormalStatus(
						(GLAppFolderBaseGridView<?>) GridViewFactory
								.getGridView(FolderStatusManager.SCREEN_FOLDER_GRID));
				break;
			case FolderStatusManager.SCREEN_FOLDER_GRID | FolderStatusManager.FOLDER_SILENT_MODE_STATUS :
				status = new ScreenFolderSilentModeStatus(
						(GLAppFolderBaseGridView<?>) GridViewFactory
								.getGridView(FolderStatusManager.SCREEN_FOLDER_GRID));
				break;
			case FolderStatusManager.SCREEN_FOLDER_GRID | FolderStatusManager.FOLDER_POWER_SAVING_MODE_STATUS :
				status = new ScreenFolderPowerSavingModeStatus(
						(GLAppFolderBaseGridView<?>) GridViewFactory
								.getGridView(FolderStatusManager.SCREEN_FOLDER_GRID));
				break;
			case FolderStatusManager.DOCK_FOLDER_GRID | FolderStatusManager.GRID_NORMAL_STATUS :
				status = new ScreenFolderNormalStatus(
						(GLAppFolderBaseGridView<?>) GridViewFactory
								.getGridView(FolderStatusManager.DOCK_FOLDER_GRID));
				break;
			case FolderStatusManager.DOCK_FOLDER_GRID | FolderStatusManager.FOLDER_SILENT_MODE_STATUS :
				status = new ScreenFolderSilentModeStatus(
						(GLAppFolderBaseGridView<?>) GridViewFactory
						.getGridView(FolderStatusManager.DOCK_FOLDER_GRID));
				break;
			case FolderStatusManager.DOCK_FOLDER_GRID | FolderStatusManager.FOLDER_POWER_SAVING_MODE_STATUS :
				status = new ScreenFolderPowerSavingModeStatus(
						(GLAppFolderBaseGridView<?>) GridViewFactory
						.getGridView(FolderStatusManager.DOCK_FOLDER_GRID));
				break;
			default :
				break;
		}
		return status;
	}

	/**
	 * ActionBar工厂
	 * @author yangguanxiang
	 *
	 */
	private static class ActionBarFactory {

		private static SparseArray<ArrayList<IActionBar>> sTopBarGroupMap = new SparseArray<ArrayList<IActionBar>>();
		private static SparseArray<ArrayList<IActionBar>> sBottomBarGroupMap = new SparseArray<ArrayList<IActionBar>>();

		private static GLAllAppTopActionBar sTabSwitchBar = null;

		private static ArrayList<IActionBar> getTopBarGroup(int statusID) {
			ArrayList<IActionBar> group = sTopBarGroupMap.get(statusID);
			if (group == null) {
				switch (statusID) {
					case AppDrawerStatusManager.ALLAPP_TAB :
						group = new ArrayList<IActionBar>(2);
						group.add(getTabSwitchBar());
						group.add(new GLFolderActionBar(ShellAdmin.sShellManager.getContext()));
						sTopBarGroupMap.put(statusID, group);
						break;
//					case AppDrawerStatusManager.RECENTAPP_TAB :
//						group = new ArrayList<IActionBar>(1);
//						group.add(getTabSwitchBar());
//						sTopBarGroupMap.put(statusID, group);
//						break;
//					case AppDrawerStatusManager.PROMANAGER_TAB :
//						group = new ArrayList<IActionBar>(2);
//						group.add(getTabSwitchBar());
//						group.add(new GLProManageEditActionBar(ShellAdmin.sShellManager.getContext()));
//						sTopBarGroupMap.put(statusID, group);
					default :
						break;
				}
			}
			return group;
		}

		private static ArrayList<IActionBar> getBottomBarGroup(int statusID) {
			ArrayList<IActionBar> group = sBottomBarGroupMap.get(statusID);
			if (group == null) {
				switch (statusID) {
					case AppDrawerStatusManager.ALLAPP_TAB :
						group = new ArrayList<IActionBar>(2);
						group.add(new GLAllAppBottomActionBar(ShellAdmin.sShellManager.getContext()));
						group.add(new GLPreviewBar(ShellAdmin.sShellManager.getContext()));
						sBottomBarGroupMap.put(statusID, group);
						break;
//					case AppDrawerStatusManager.RECENTAPP_TAB :
//						group = new ArrayList<IActionBar>(1);
//						group.add(new GLRecentAppActionBar(ShellAdmin.sShellManager.getContext()));
//						sBottomBarGroupMap.put(statusID, group);
//						break;
//					case AppDrawerStatusManager.PROMANAGER_TAB :
//						group = new ArrayList<IActionBar>(1);
//						group.add(new GLProManageActionBar(ShellAdmin.sShellManager.getContext()));
//						sBottomBarGroupMap.put(statusID, group);
					default :
						break;
				}
			}
			return group;
		}

		private static GLAllAppTopActionBar getTabSwitchBar() {
			if (sTabSwitchBar == null) {
				sTabSwitchBar = new GLAllAppTopActionBar(ShellAdmin.sShellManager.getContext());
			}
			return sTabSwitchBar;
		}
	}
	
	/**
	 * GridView工厂
	 * @author yangguanxiang
	 *
	 */
	private static class GridViewFactory {
		private static SparseArray<GLExtrusionGridView> sGridViewMap = new SparseArray<GLExtrusionGridView>();

		private static GLExtrusionGridView getGridView(int statusID) {
			GLExtrusionGridView gridView = sGridViewMap.get(statusID);
			if (gridView == null) {
				Context context = ShellAdmin.sShellManager.getContext();
				switch (statusID) {
					case AppDrawerStatusManager.ALLAPP_TAB :
						gridView = new GLAllAppGridView(context);
						sGridViewMap.put(statusID, gridView);
						break;
//					case AppDrawerStatusManager.RECENTAPP_TAB :
//						gridView = new GLRecentAppGridView(context);
//						sGridViewMap.put(statusID, gridView);
//						break;
//					case AppDrawerStatusManager.PROMANAGER_TAB :
//						gridView = new GLProManageGridView(context);
//						sGridViewMap.put(statusID, gridView);
//						break;
					case FolderStatusManager.DRAWER_FOLDER_GRID :
						gridView = new GLAppDrawerFolderGridView(context);
						sGridViewMap.put(statusID, gridView);
						break;
					case FolderStatusManager.SCREEN_FOLDER_GRID :
						gridView = new GLScreenFolderGridView(context);
						sGridViewMap.put(statusID, gridView);
						break;
					case FolderStatusManager.DOCK_FOLDER_GRID :
						gridView = new GLDockFolderGridVIew(context);
						sGridViewMap.put(statusID, gridView);
						break;
					default :
						break;
				}
			}
			return gridView;
		}
	}
	
	/**
	 * 获取所有GridView的Map
	 * @return
	 */
	public static SparseArray<GLExtrusionGridView> getGridViewMap() {
		return GridViewFactory.sGridViewMap;
	}
	
	/**
	 * 获取所有TopBar
	 * @return
	 */
	public static SparseArray<ArrayList<IActionBar>> getTopBarGroupMap() {
		return ActionBarFactory.sTopBarGroupMap;
	}
	
	/**
	 * 获取所有的BottomBar
	 * @return
	 */
	public static SparseArray<ArrayList<IActionBar>> getBottomBarGroupMap() {
		return ActionBarFactory.sBottomBarGroupMap;
	}
	
	/**
	 * 获取所有的TabSwitchBar
	 * @return
	 */
	public static GLAllAppTopActionBar getsTabSwitchBar() {
		return ActionBarFactory.sTabSwitchBar;
	}
}
