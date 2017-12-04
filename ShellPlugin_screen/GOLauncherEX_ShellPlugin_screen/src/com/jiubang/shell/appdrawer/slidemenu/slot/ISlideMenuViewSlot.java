package com.jiubang.shell.appdrawer.slidemenu.slot;

import com.go.gl.view.GLView;

/**
 * 侧边栏内功能场景入口类型接口
 * @author wuziyi
 *
 */
public interface ISlideMenuViewSlot {

	public int getFuntionNameResId();

	public int getIconResId();

	public int getBackgroundResId();

	public int getViewId();
	
	public void showExtendFunctionView(GLView view, boolean needAnimation, Object...objs);
	
	public void hideExtendFunctionView(GLView view, boolean needAnimation, Object...objs);
}
