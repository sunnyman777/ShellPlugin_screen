package com.jiubang.shell.appdrawer.slidemenu.slot;

import android.content.Intent;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeManageActivity;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeManageView;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 侧边栏付费高级功能块
 * @author wuziyi
 *
 */
public class SlideMenuThemeSlot extends AbsSlideMenuSlot {

	@Override
	public int getFuntionNameResId() {
		return R.string.slide_menu_morethemes_title;
	}

	@Override
	public int getIconResId() {
		return R.drawable.gl_appdrawer_slide_menu_morethemes_icon;
	}

	@Override
	public int getBackgroundResId() {
		return R.drawable.gl_appdrawer_slide_menu_prime_bg;
	}

	@Override
	public int getViewId() {
		return IViewId.MORE_THEMES;
	}

	@Override
	public void showExtendFunctionView(GLView view, boolean needAnimation, Object...objs) {
//		super.showExtendFunctionView(view, needAnimation);
		Intent intent = new Intent(ShellAdmin.sShellManager.getActivity(),
				ThemeManageActivity.class);
		intent.putExtra("entrance", ThemeManageView.LAUNCHER_THEME_VIEW_ID);
		ShellAdmin.sShellManager.getShell().startActivitySafely(intent);
		
		String opCode = "si_morethemes";
		GuiThemeStatistics.sideOpStaticData("-1", opCode, 1, "-1");
	}

	@Override
	public void hideExtendFunctionView(GLView view, boolean needAnimation, Object...objs) {
		super.hideExtendFunctionView(view, needAnimation);

	}

}
