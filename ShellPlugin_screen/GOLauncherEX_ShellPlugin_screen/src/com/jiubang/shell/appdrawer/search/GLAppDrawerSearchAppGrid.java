package com.jiubang.shell.appdrawer.search;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;

import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.util.window.OrientationControl;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.apps.appfunc.controler.MediaFileSuperVisor;
import com.jiubang.ggheart.apps.desks.appfunc.model.FuncSearchResultItem;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.realtiemstatistics.RealTimeStatisticsUtil;
import com.jiubang.ggheart.plugin.shell.IViewId;
/**
 * 
 * @author dingzijian
 *
 */
public class GLAppDrawerSearchAppGrid extends GLAppDrawerSearchBaseGridView {

	public GLAppDrawerSearchAppGrid(Context context, AttributeSet attrs) {
		super(context, attrs);

	}
	@Override
	public void onItemClick(GLAdapterView<?> arg0, GLView arg1, int pos, long arg3) {
		FuncSearchResultItem resultItem = (FuncSearchResultItem) getAdapter().getItem(pos);
		OrientationControl.keepCurrentOrientation(GoLauncherActivityProxy.getActivity());
		switch (resultItem.mType) {
			case FuncSearchResultItem.ITEM_TYPE_LOCAL_HISTORY_APPS :
				startLocalApp(resultItem);
				RealTimeStatisticsUtil.upLoadAppDrawerSearch("ck_his");
				break;
			case FuncSearchResultItem.ITEM_TYPE_LOCAL_APPS :
				startLocalApp(resultItem);
				break;
			case FuncSearchResultItem.ITEM_TYPE_APP_CENTER_APPS :
				AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(
						ApplicationProxy.getContext(),
						AppRecommendedStatisticsUtil.ENTRY_TYPE_APPFUNC_SEARCH);
				AppsDetail.jumpToDetail(ApplicationProxy.getContext(), resultItem.appInfo,
						AppsDetail.START_TYPE_APPFUNC_SEARCH, pos, true);
				RealTimeStatisticsUtil.upLoadAppDrawerSearch("ck001", "",
						resultItem.recApp.info.appid, resultItem.recApp.info.packname,
						resultItem.mTitle);
				break;
			default :
				if (mSearchResutlListener != null) {
					mSearchResutlListener.onMediaStart(resultItem.mTitle);
				}
				MediaFileSuperVisor.getInstance(ApplicationProxy.getContext()).openMediaFile(
						resultItem.fileInfo, MediaFileSuperVisor.MEDIA_FILE_OPEN_BY_SEARCH, null);
				break;
		}
	}
	
	private void startLocalApp(FuncSearchResultItem resultItem) {
		if (mSearchResutlListener != null) {
			mSearchResutlListener.onAppStart(resultItem.mIntent.toUri(Intent.URI_INTENT_SCHEME));
		}

		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, ICommonMsgId.START_ACTIVITY, -1,
				resultItem.mIntent, null);
	}
	
	@Override
	public boolean onItemLongClick(GLAdapterView<?> arg0, GLView arg1, int pos, long arg3) {
		final FuncSearchResultItem resultItem = (FuncSearchResultItem) getAdapter().getItem(pos);
		if (resultItem.mType == FuncSearchResultItem.ITEM_TYPE_LOCAL_APPS
				|| resultItem.mType == FuncSearchResultItem.ITEM_TYPE_LOCAL_HISTORY_APPS) {
			if (mSearchResutlListener != null) {
				mSearchResutlListener.onAppLocated(resultItem.mIntent
						.toUri(Intent.URI_INTENT_SCHEME));
			}
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
					ICommonMsgId.SHOW_EXTEND_FUNC_VIEW, 0, IViewId.APP_DRAWER_SEARCH, resultItem.mIntent);
			return true;
		}
		return false;
	}
}
