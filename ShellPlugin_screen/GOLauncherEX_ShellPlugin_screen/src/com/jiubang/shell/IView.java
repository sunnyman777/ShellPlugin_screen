package com.jiubang.shell;

import com.go.gl.view.GLViewGroup;
import com.jiubang.shell.common.component.IOnKeyEventHandler;

/**
 * 
 * @author yangguanxiang
 *
 */
public interface IView extends IOnKeyEventHandler {
	public void setVisible(boolean visible, boolean animate, Object obj);
	
	public void setShell(IShell shell);
	
	public int getViewId();
	
	public void onAdd(GLViewGroup parent);
	
	public void onRemove();
}
