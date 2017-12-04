package com.jiubang.shell.appdrawer;


/**
 * 
 * @author yangguanxiang
 *
 */
public interface IActionBar {
	/**
	 * 是否需要画背景
	 * @return
	 */
	public boolean needDrawBg();

	public void onInOutAnimationStart(boolean in);

	public void onInOutAnimationEnd(boolean in);

	//add by zhangxi @2013-09-06 for 处理横竖屏
	public void onConfigurationChanged();

	public void onParentInOutAnimationStart(boolean in);

	public void onParentInOutAnimationEnd(boolean in);
}
