package com.jiubang.shell.common.listener;
import com.go.gl.view.GLView;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUninstallHelper;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUninstallHelper.ActiveNotFoundCallBack;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.shell.common.component.GLModel3DMultiView.OnSelectClickListener;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * <br>类描述:点击卸载的监听器
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-11-15]
 */
public class UninstallListener implements OnSelectClickListener {
	private AppItemInfo mItemInfo;
	private ActiveNotFoundCallBack mCallback;
	
	public UninstallListener(AppItemInfo itemInfo,
			ActiveNotFoundCallBack callback) {
		super();
		mItemInfo = itemInfo;
		mCallback = callback;
	}

	@Override
	public void onClick(GLView v) {
		boolean success = AppFuncUninstallHelper.uninstallApp(
				ShellAdmin.sShellManager.getActivity(), mItemInfo.mIntent, mCallback);
		if (success) {
			//用户行为统计。
			StatisticsData.countUserActionData(StatisticsData.FUNC_ACTION_ID_APPLICATION,
					StatisticsData.USER_ACTION_TWO, IPreferencesIds.APP_FUNC_ACTION_DATA);
		}
	}
	
}