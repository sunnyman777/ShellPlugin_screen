package com.jiubang.shell.common.component;

import android.content.res.Resources;

import com.go.gl.view.GLContentView;
import com.go.proxy.ApplicationProxy;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 插件主surfaceView
 * @author yangguanxiang
 *
 */
public class ShellContentView extends GLContentView {

	public ShellContentView(boolean translucent) {
		super(ApplicationProxy.getApplication(), translucent);
	}

	@Override
	public Resources getResources() {
		return ShellAdmin.sShellManager.getContext().getResources();
	}
}
