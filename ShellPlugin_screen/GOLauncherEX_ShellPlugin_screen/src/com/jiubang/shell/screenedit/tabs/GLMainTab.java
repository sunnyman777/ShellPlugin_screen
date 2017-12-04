package com.jiubang.shell.screenedit.tabs;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.MsgMgrProxy;
import com.go.util.device.Machine;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenEditMsgId;
import com.golauncher.message.IScreenFrameMsgId;
import com.golauncher.message.IWidgetMsgId;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConfig;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditController;
import com.jiubang.ggheart.apps.gowidget.ScreenEditItemInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.screenedit.GLMainAdapter;

/**
 * 添加模块的初始界面
 * @author zouguiquan
 *
 */
public class GLMainTab extends GLGridTab {

	public GLMainTab(Context context, int tabId, int level) {
		super(context, tabId, level);
		mNeedShowProgress = false;
		mNeedAsyncLoadImage = false;
	}

	@Override
	public ArrayList<Object> requestData() {
		if (mDataList != null && mDataList.size() > 0) {
			return mDataList;
		}

		return ScreenEditController.getInstance().requestMainTabData();
	}

	@Override
	public void onItemClick(GLAdapterView<?> adapter, GLView view, int position, long id) {
		super.onItemClick(adapter, view, position, id);

		if (isMultipleClick()) {
			return;
		}

		ScreenEditItemInfo itemInfo = (ScreenEditItemInfo) view.getTag();
		if (itemInfo == null) {
			return;
		}

		switch (itemInfo.getId()) {
			case ScreenEditConstants.CLICK_TAB_ADD :
				//添加应用程序
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_EDIT,
						IScreenEditMsgId.SCREEN_EDIT_CHANGE_TAB, ScreenEditConstants.TAB_ID_APPS,
						itemInfo);

				//用户行为统计
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
						StatisticsData.USER_ACTION_FIVE, IPreferencesIds.DESK_ACTION_DATA);
				break;

			case ScreenEditConstants.CLICK_TAB_FOLDER :
				if (!checkScreenVacant(1, 1)) {
					return;
				}

				//添加文件夹
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_EDIT,
						IScreenEditMsgId.SCREEN_EDIT_CHANGE_TAB, ScreenEditConstants.TAB_ID_FOLDER);

				addFolder(mContext, ScreenEditConstants.CLICK_TAB_FOLDER);
				// 屏蔽workspace的触摸响应
				/*MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
						IDiyMsgIds.IN_NEW_FOLDER_STATE, -1, null, null);
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
						IDiyMsgIds.SCREEN_EDIT_ADD_FORLDER, 0, null, null);*/

				//用户行为统计
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
						StatisticsData.USER_ACTION_SIX, IPreferencesIds.DESK_ACTION_DATA);
				break;

			case ScreenEditConstants.CLICK_TAB_GO_WIDGET :

				ScreenEditController.isFristShowScreenEditGOWidget(mContext, true);

				//添加文件夹
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_EDIT,
						IScreenEditMsgId.SCREEN_EDIT_CHANGE_TAB,
						ScreenEditConstants.TAB_ID_GOWIDGET);

				//用户行为统计
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
						StatisticsData.USER_ACTION_SEVEN, IPreferencesIds.DESK_ACTION_DATA);
				break;

			case ScreenEditConstants.CLICK_TAB_SYSTEM_WIDGET :

				boolean isSupportBind = Machine.isSupportBindWidget(mContext); // 是否支持绑定widgetId的权限
				if (isSupportBind) {

					//添加系统小部件
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_EDIT,
							IScreenEditMsgId.SCREEN_EDIT_CHANGE_TAB,
							ScreenEditConstants.TAB_ID_SYSTEMWIDGET);
				} else {
					// 不支持反射的，由于获取不了绑定widgetId的权限，按列表方式添加系统widget
					if (checkScreenVacant(1, 1)) {
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IWidgetMsgId.PICK_WIDGET, 0, null, null);
					}
				}

				// 用户行为统计
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
						StatisticsData.USER_ACTION_EIGHT, IPreferencesIds.DESK_ACTION_DATA);
				break;

			case ScreenEditConstants.CLICK_TAB_GO_SHORTCUT :

				//添加Go快捷方式
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_EDIT,
						IScreenEditMsgId.SCREEN_EDIT_CHANGE_TAB,
						ScreenEditConstants.TAB_ID_GOSHORTCUT);

				//用户行为统计
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
						StatisticsData.USER_ACTION_TEN, IPreferencesIds.DESK_ACTION_DATA);
				break;

			default :
				break;
		}
	}

	@Override
	public ShellBaseAdapter<Object> createAdapter(Context mContext, List<Object> infoList) {
		return new GLMainAdapter(mContext, infoList);
	}

	/**
	 * 添加文件夹
	 * createType 暂时未使用到，可以考虑删除
	 */
	private void addFolder(Context mContext, int createType) {
		UserFolderInfo folderInfo = new UserFolderInfo();
		folderInfo.mTitle = mContext.getText(R.string.folder_name);
		// 发送给屏幕层要求添加一个文件夹
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_ADD_USER_FOLDER, -1, folderInfo);
	}
	
	@Override
	public boolean onBackExit() {
		if (ScreenEditConfig.sEXTERNAL_FROM_ID == ScreenEditConstants.EXTERNAL_ID_APPDRAWER_SLIDEMENU) {
			return true;
		}
		return super.onBackExit();
	}
}