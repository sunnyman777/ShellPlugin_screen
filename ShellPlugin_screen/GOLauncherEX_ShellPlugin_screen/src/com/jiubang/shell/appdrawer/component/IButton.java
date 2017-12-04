package com.jiubang.shell.appdrawer.component;

import com.go.gl.view.GLView;

/**
 * 
 * @author yangguanxiang
 *
 */
public interface IButton {
	public void loadResource();
	public boolean doClick(GLView view);
	public boolean doLongClick();
}
