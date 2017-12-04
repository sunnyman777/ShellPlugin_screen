package com.jiubang.shell.appdrawer;

/**
 * 功能表点击事件回调（主要在侧边栏已经打开的情况下使用）
 * @author wuziyi
 *
 */
public interface GridContainerTouchListener {
	
	public void onGridContainerTouchMove(float offsetX);
	
	public void onGridContainerTouchUp(float touchUpX);
	
	public void onGridContainerTriggerShowSlideMenuArea();
}
