package com.jiubang.shell.appdrawer.slidemenu.slot;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.plugin.shell.IViewId;

/**
 * 侧边栏应用小部件功能块
 * @author wuziyi
 *
 */
public class SlideMenuWidgetSlot extends AbsSlideMenuSlot {

	@Override
	public int getFuntionNameResId() {
		return R.string.slide_menu_widget_title;
	}

	@Override
	public int getIconResId() {
		return R.drawable.gl_appdrawer_slide_menu_widget_icon;
	}

	@Override
	public int getBackgroundResId() {
		return R.drawable.gl_appdrawer_slide_menu_widget_bg;
	}

	@Override
	public int getViewId() {
		return IViewId.WIDGET_MANAGE;
	}

	@Override
	public void showExtendFunctionView(GLView view, boolean needAnimation, Object...objs) {
		super.showExtendFunctionView(view, needAnimation);
		String opCode = "si_widget";
		GuiThemeStatistics.sideOpStaticData("-1", opCode, 1, "-1");
	}

	@Override
	public void hideExtendFunctionView(GLView view, boolean needAnimation, Object...objs) {
		super.hideExtendFunctionView(view, needAnimation);

	}

}
