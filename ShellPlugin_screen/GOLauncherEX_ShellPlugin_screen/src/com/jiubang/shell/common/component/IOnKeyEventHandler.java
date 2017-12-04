package com.jiubang.shell.common.component;

import android.view.KeyEvent;

/**
 * 
 * @author yangguanxiang
 *
 */
public interface IOnKeyEventHandler {
	public boolean onKeyDown(int keyCode, KeyEvent event);

	public boolean onKeyUp(int keyCode, KeyEvent event);

	public boolean onKeyLongPress(int keyCode, KeyEvent event);

	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event);
}
