package com.jiubang.shell.screenedit.tabs;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenEditMsgId;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConfig;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.gowidget.ScreenEditItemInfo;
import com.jiubang.ggheart.apps.systemwidget.SystemWidgetLoader;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.screenedit.GLSysWidgetAdapter;

/**
 * 添加系统小部件第一级Tab
 * @author zouguiquan
 *
 */
public class GLSysWidgetTab extends GLGridTab {

	private ArrayList<Object> mWidgetAndShortCut;

	public GLSysWidgetTab(Context context, int tabId, int tabLevel) {
		super(context, tabId, tabLevel);

		mPreTabId = ScreenEditConstants.TAB_ID_MAIN;
	}

	@Override
	public ArrayList<Object> requestData() {
		if (mDataList != null && mDataList.size() > 0) {
			return mDataList;
		}

		if (mWidgetAndShortCut == null || mWidgetAndShortCut.size() <= 0) {
			mWidgetAndShortCut = SystemWidgetLoader.getSortedWidgetsAndShortcuts(m2DContext);
		}

		return SystemWidgetLoader.getSysWidgetList(mWidgetAndShortCut, m2DContext);
	}

	@Override
	public void handleAppChanged(int msgId, String pkgName, boolean showing) {
		super.handleAppChanged(msgId, pkgName, showing);

		if (mWidgetAndShortCut != null) {
			mWidgetAndShortCut.clear();
		}

		if (showing) {
			refreshData();
		}
	}

	@Override
	public ShellBaseAdapter<Object> createAdapter(Context mContext, List<Object> infoList) {
		return new GLSysWidgetAdapter(mContext, infoList, mImageLoader);
	}

	@Override
	public Bitmap onLoadImage(int index) {

		Bitmap bitmap = null;
		if (mDataList != null) {
			ScreenEditItemInfo info = (ScreenEditItemInfo) mDataList.get(index);

			if (info == null) {
				return null;
			}

			Drawable drawable = SystemWidgetLoader.getSysWidgetIcon(m2DContext, info.getPkgName());

			if (drawable != null) {
				bitmap = mEditController.composeIconMaskBitmap(drawable, true, false);
			}
		}
		return bitmap;
	}

	@Override
	public void onItemClick(GLAdapterView<?> adapter, GLView view, int position, long id) {
		super.onItemClick(adapter, view, position, id);

		ScreenEditItemInfo info = (ScreenEditItemInfo) getItem(position);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_EDIT,
				IScreenEditMsgId.SCREEN_EDIT_CHANGE_TAB,
				ScreenEditConstants.TAB_ID_SUB_SYSTEMWIDGET, mWidgetAndShortCut, info.getPkgName(),
				info.getTitle());
	}
	
	@Override
	public boolean onBackExit() {
		if (ScreenEditConfig.sEXTERNAL_FROM_ID == ScreenEditConstants.EXTERNAL_ID_APPDRAWER_SLIDEMENU) {
			return true;
		}
		return super.onBackExit();
	}
}
