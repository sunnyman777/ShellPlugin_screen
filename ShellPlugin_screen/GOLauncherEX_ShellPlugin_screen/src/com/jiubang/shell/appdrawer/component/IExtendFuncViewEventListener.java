package com.jiubang.shell.appdrawer.component;

import com.jiubang.shell.IView;

/**
 * 
 * @author yangguanxiang
 *
 */
public interface IExtendFuncViewEventListener {
	/**
	 * 进入动画前回调
	 * @param view
	 */
	public void extendFuncViewPreEnter(IView view);
	/**
	 * 进入动画后回调
	 * @param view
	 */
	public void extendFuncViewOnEnter(IView view);
	/**
	 * 退出动画前回调
	 * @param view
	 */
	public void extendFuncViewPreExit(IView view);
	/**
	 * 进入动画后回调
	 * @param view
	 */
	public void extendFuncViewOnExit(IView view);
}
