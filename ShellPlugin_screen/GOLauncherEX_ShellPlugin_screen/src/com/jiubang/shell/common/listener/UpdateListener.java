package com.jiubang.shell.common.listener;

import android.content.Context;
import android.content.Intent;

import com.go.gl.view.GLView;
import com.go.proxy.ApplicationProxy;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.config.GOLauncherConfig;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreStatisticsUtil;
import com.jiubang.ggheart.data.info.FunItemInfo;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.shell.common.component.GLModel3DMultiView.OnSelectClickListener;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * APP更新监听器
 * @author yejijiong
 *
 */
public class UpdateListener implements OnSelectClickListener {
	private FunItemInfo mItemInfo;
	private Context mContext;
	private AppDrawerControler mAppDrawerControler;
	
	public UpdateListener(FunItemInfo itemInfo) {
		super();
		mContext = ShellAdmin.sShellManager.getActivity();
		mItemInfo = itemInfo;
		mAppDrawerControler = AppDrawerControler.getInstance(mContext);
	}
	
	@Override
	public void onClick(GLView v) {
		Intent intent = mItemInfo.getIntent();
		// 统计：默认值是保存点击GoStore图标更新标志进入
		AppManagementStatisticsUtil.getInstance().saveCurrentEnter(mContext,
				AppManagementStatisticsUtil.ENTRY_TYPE_GOSTORE_ICON);
		// 点击应用和store图标的更新标识，进入更新列表，退出功能表——清除标识；没有点击更新标识，退出功能表——不清除标识。
		FuncAppDataHandler.getInstance(mContext).setmClickAppupdate(true);
		
		GoStoreStatisticsUtil.setCurrentEntry(
				GoStoreStatisticsUtil.ENTRY_TYPE_APP_UPDATE, mContext);
		// 要根据渠道配置信息，确定升级小图标点击后的动作
		// Add by wangzhuobin 2012.07.28
		ChannelConfig channelConfig = GOLauncherConfig.getInstance(ApplicationProxy.getContext()).getChannelConfig();
		if (channelConfig != null && channelConfig.isNeedAppCenter()) {
			// 有应用中心跳应用中心
			// update by zhoujun 应用中心图标上面的更新数字，跳转到应用中心的首页
			//							AppsManagementActivity.startAppCenter(mContext,
			//									MainViewGroup.ACCESS_FOR_UPDATE);
			//如果是点击应用中心上面的数字，传入口值ACCESS_FOR_APPCENTER_UPATE，否则传入口值ACCESS_FOR_UPDATE
			//add by xiedezhi 2012.11.14
			if (null != intent
					&& null != intent.getAction()
					&& intent.getAction().equals(
							ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER)) {
				AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mContext,
						AppRecommendedStatisticsUtil.ENTRY_TYPE_APPFUNC_UPDATE);
				AppsManagementActivity.startAppCenter(mContext,
						MainViewGroup.ACCESS_FOR_APPCENTER_UPATE, false);
			} else {
				AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mContext,
						AppRecommendedStatisticsUtil.ENTRY_TYPE_APP_ICON);
				AppsManagementActivity.startAppCenter(mContext,
						MainViewGroup.ACCESS_FOR_UPDATE, false);
			}
		} else {
			// 没有的话跳应用管理模块
			AppManagementStatisticsUtil.getInstance().saveCurrentEnter(mContext,
					AppManagementStatisticsUtil.ENTRY_TYPE_APPFUNC_UPDATE);
//			AppCore.getInstance()
//					.getApplicationManager()
//					.show(IDiyFrameIds.APPFUNC_FRAME,
//							AppsManageView.APPS_UPDATE_VIEW_ID);
			AppsManagementActivity.startAppCenter(mContext, MainViewGroup.ACCESS_FOR_UPDATE, false);
		}
	}
}
