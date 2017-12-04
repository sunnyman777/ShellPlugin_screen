package com.jiubang.shell.popupwindow;

import com.jiubang.shell.popupwindow.component.GLPopupWindowLayer;

/**
 * 弹出菜单接口
 * @author yangguanxiang
 *
 */
public interface IPopupWindow {
	public void onEnter(GLPopupWindowLayer layer, boolean animate);

	public void onExit(GLPopupWindowLayer layer, boolean animate);
	
	public void onWithEnter(boolean animate);
	
	public void onWithExit(boolean animate);
}
