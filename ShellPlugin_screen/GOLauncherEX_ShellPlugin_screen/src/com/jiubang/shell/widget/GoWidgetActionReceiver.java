package com.jiubang.shell.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.go.gowidget.core.GoWidgetConstant;
import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.IAppCoreMsgId;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 
 * @author zhouxuewen
 *
 */
public class GoWidgetActionReceiver extends BroadcastReceiver {
	public static int sPenddingAddWidgetId;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Bundle bundle = intent.getExtras();
		if (action.equals(GoWidgetConstant.ACTION_CONFIG_FINISH) || action.equals(ICustomAction.ACTION_CONFIG_FINISH)) {
			int widgetid = bundle.getInt(GoWidgetConstant.GOWIDGET_ID, 0);
			if (GoWidgetManager.isGoWidget(widgetid) && sPenddingAddWidgetId == widgetid) {
				// 添加到桌面上
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
						IScreenFrameMsgId.SCREEN_ADD_GO_WIDGET, -1, bundle);
				sPenddingAddWidgetId = 0;
			}
		} else if (action.equals(GoWidgetConstant.ACTION_REQUEST_FOCUS)) {
			int widgetId = bundle.getInt(GoWidgetConstant.GOWIDGET_ID, 0);
			if (GoWidgetManager.isGoWidget(widgetId)) {
				// 返回到桌面
				MsgMgrProxy.sendBroadcast(this, ICommonMsgId.BACK_TO_MAIN_SCREEN, -1);
			}
		} else if (action.equals(GoWidgetConstant.ACTION_CHANGE_WIDGETS_THEME)) {
			// 大主题，通知桌面，所有放在桌面的widget更换皮肤
			String pkgName = intent.getStringExtra(GoWidgetConstant.WIDGET_THEME_KEY);
			MsgMgrProxy.sendBroadcastHandler(this, IAppCoreMsgId.EVENT_CHANGE_WIDGET_THEME, 0, pkgName,
					null);
		} else if (action.equals(GoWidgetConstant.ACTION_GOTO_GOWIDGET_FRAME)) {
			// 退出主题预览界面
			// 改为跳转至添加界面
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.SCREEN_ENTER_SCREEN_EDIT_LAYOUT, 1, ScreenEditConstants.TAB_GOWIDGET, null);
		}
		// else if
		// (action.equals(GoWidgetConstant.ACTION_ON_OFF_RECEIVER_CAN_UNINSTALL))
		// {
		// boolean canUninstall =
		// bundle.getBoolean(GoWidgetConstant.ACTION_ON_OFF_RECEIVER_CAN_UNINSTALL_DATA);
		// if (canUninstall)
		// {
		// //通知桌面正式卸载开关
		// MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
		// IDiyMsgIds.GOWIDGET_UNINSTALL_GOWIDGET_SWITCH, -1,
		// null, null);
		// }else {
		// //do nothing
		// }
		// }
	}
}
