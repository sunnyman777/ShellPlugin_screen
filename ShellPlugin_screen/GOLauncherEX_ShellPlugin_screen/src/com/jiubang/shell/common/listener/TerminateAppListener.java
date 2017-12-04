package com.jiubang.shell.common.listener;

import com.go.gl.view.GLView;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.shell.common.component.GLModel3DMultiView.OnSelectClickListener;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 正在运行关闭进程监听器
 * @author yejijiong
 *
 */
public class TerminateAppListener implements OnSelectClickListener {

	private FunAppItemInfo mItemInfo;

	public TerminateAppListener(FunAppItemInfo itemInfo) {
		super();
		mItemInfo = itemInfo;
	}

	@Override
	public void onClick(GLView v) {
		if (mItemInfo != null) {
			AppDrawerControler.getInstance(ShellAdmin.sShellManager.getActivity()).terminateApp(
					mItemInfo);
			GuiThemeStatistics.sideOpStaticData("-1", "rg_clear_sigle", 1, "-1");
		}
	}

}
