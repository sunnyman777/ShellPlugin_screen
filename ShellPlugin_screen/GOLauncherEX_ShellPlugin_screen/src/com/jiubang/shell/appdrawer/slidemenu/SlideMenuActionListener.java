package com.jiubang.shell.appdrawer.slidemenu;

/**
 * 侧边栏动画执行回调方法
 * @author wuziyi
 *
 */
public interface SlideMenuActionListener {
	
	public void onSlideMenuShowStart();
	
	public void onSlideMenuShowEnd();
	
	public void onSlideMenuHideStart();
	
	public void onSlideMenuHideEnd();
	
	public void onSlideMenuShowPersent(float persent);

}
