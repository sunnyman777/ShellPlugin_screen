package com.jiubang.shell.appdrawer.slidemenu.slot;

import android.content.Intent;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.jiubang.ggheart.components.appmanager.SimpleAppManagerActivity;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.data.statistics.IGoLauncherUserBehaviorStatic;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 侧边栏应用管理功能块
 * @author wuziyi
 *
 */
public class SlideMenuAppManagerSlot extends AbsSlideMenuSlot {

	@Override
	public int getFuntionNameResId() {
		return R.string.slide_menu_app_title;
	}

	@Override
	public int getIconResId() {
		return R.drawable.gl_appdrawer_slide_menu_app_manage_icon;
	}

	@Override
	public int getBackgroundResId() {
		return R.drawable.gl_appdrawer_slide_menu_app_manage_bg;
	}

	@Override
	public int getViewId() {
		return IViewId.APP_MANAGE;
	}

	@Override
	public void showExtendFunctionView(GLView view, boolean needAnimation, Object...objs) {
		// 这个比较特殊，是启动Activity的，不能调用super方法
//		super.showExtendFunctionView(view);
		Intent intent = new Intent(ShellAdmin.sShellManager.getActivity(),
				SimpleAppManagerActivity.class);
		ShellAdmin.sShellManager.getShell().startActivitySafely(intent);
		String opCode = "si_manage";
		GuiThemeStatistics.sideOpStaticData("-1", opCode, 1, "-1");
		GuiThemeStatistics
				.goLauncherUserBehaviorStaticDataCache(IGoLauncherUserBehaviorStatic.APP_MANAGER_PORTAL_SB);
	}

	@Override
	public void hideExtendFunctionView(GLView view, boolean needAnimation, Object...objs) {
		super.hideExtendFunctionView(view, needAnimation);

	}

}
