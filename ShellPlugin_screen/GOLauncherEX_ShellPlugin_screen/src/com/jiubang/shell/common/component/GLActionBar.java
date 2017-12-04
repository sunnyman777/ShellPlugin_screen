package com.jiubang.shell.common.component;

import android.content.Context;

import com.jiubang.shell.IShell;
import com.jiubang.shell.appdrawer.IActionBar;
/**
 * 工具条基类
 * @author wuziyi
 *
 */
public abstract class GLActionBar extends GLLinearPanel implements IActionBar {

	protected IShell mShell;

	public GLActionBar(Context context) {
		super(context);
	}

	public void setShell(IShell shell) {
		mShell = shell;
	}

}
