package com.jiubang.shell.appdrawer.controler;
/**
 * 状态转换监听器
 * @author wuziyi
 *
 */
public interface StatusChangeListener {

	public void onSceneStatusChange(Status oldStatus, Status curStatus, Object[] objects);
	
	public void onGridStatusChange(Status oldStatus, Status gridStatus, Object[] objects);
}
