package com.jiubang.shell.appdrawer.hideapp;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.ggheart.apps.appfunc.controler.AppConfigControler;
import com.jiubang.ggheart.apps.appfunc.setting.AppFuncAutoFitManager;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUninstallHelper.ActiveNotFoundCallBack;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.appdrawer.component.GLAppDrawerAppIcon;
import com.jiubang.shell.appdrawer.component.GLLightBaseGrid;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.common.component.GLModel3DMultiView;
import com.jiubang.shell.common.component.HorScrollableGridViewHandler;
import com.jiubang.shell.common.listener.UninstallListener;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.model.IModelState;
import com.jiubang.shell.scroller.effector.CoupleScreenEffector;

/**
 * 隐藏程序显示界面的gird
 * 
 * @author wuziyi
 * 
 */
public class GLHideAppGridView extends GLLightBaseGrid implements
		ActiveNotFoundCallBack {
	private boolean mIsInEdit;
	private AppConfigControler mAppConfigControler;
	private volatile ArrayList<FunAppItemInfo> mList;

	public GLHideAppGridView(Context context) {
		super(context);
		init();
	}

	public GLHideAppGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mAppConfigControler = AppConfigControler.getInstance(mContext);
		handleScrollerSetting();
	}

	@Override
	public void setDragController(DragController dragger) {

	}

	@Override
	public void setTopViewId(int id) {

	}

	@Override
	public int getTopViewId() {
		return IViewId.HIDE_APP_MANAGE;
	}

	@Override
	public void onItemClick(GLAdapterView<?> parent, GLView v, int position,
			long id) {
		if (!mIsInEdit) {
			List<AppItemInfo> list = AppConfigControler.getInstance(mContext)
					.getHideApps();
			AppItemInfo info = list.get(position);
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					ICommonMsgId.START_ACTIVITY, -1, info.mIntent, null);
		}
	}

	@Override
	public String getNoDataText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refreshGridView() {
		mIsInEdit = false;
		ArrayList<AppItemInfo> tmpList = (ArrayList<AppItemInfo>) mAppConfigControler
				.getHideApps();
		if (mList == null) {
			mList = new ArrayList<FunAppItemInfo>();
		}
		mList.clear();
		for (AppItemInfo appItemInfo : tmpList) {
			mList.add(new FunAppItemInfo(appItemInfo));
		}
		setData(mList);
		handleRowColumnSetting(false);
	}

	@Override
	public ShellBaseAdapter createAdapter(Context context, List infoList) {

		return new GLHideAppGridAdapter(context, infoList);
	}

	@Override
	protected void handleScrollerSetting() {
		if (mScrollableHandler == null) {
			mScrollableHandler = new HorScrollableGridViewHandler(mContext,
					this, CoupleScreenEffector.PLACE_MENU, false, true) {

				@Override
				public void onEnterLeftScrollZone() {
				}

				@Override
				public void onEnterRightScrollZone() {
				}

				@Override
				public void onEnterTopScrollZone() {
				}

				@Override
				public void onEnterBottomScrollZone() {
				}

				@Override
				public void onExitScrollZone() {
				}

				@Override
				public void onScrollLeft() {
				}

				@Override
				public void onScrollRight() {
				}

				@Override
				public void onScrollTop() {
				}

				@Override
				public void onScrollBottom() {
				}
			};
		}
	}

	@Override
	public boolean onItemLongClick(GLAdapterView<?> parent, GLView view,
			int position, long id) {
		if (SettingProxy.getScreenSettingInfo().mLockScreen) { // 判断当前是否锁屏
			LockScreenHandler.showLockScreenNotification(mContext);
			return false;
		}
		mIsInEdit = true;
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			GLView viewItem = getChildAt(i);
			if (viewItem instanceof GLAppDrawerAppIcon) {
				GLAppDrawerAppIcon icon = (GLAppDrawerAppIcon) viewItem;
				FunAppItemInfo info = icon.getInfo();
				GLModel3DMultiView multiView = icon.getMultiView();
				if (!info.isSysApp()) {
					multiView.setCurrenState(IModelState.UNINSTALL_STATE, null);
					multiView.setOnSelectClickListener(new UninstallListener(
							info.getAppItemInfo(), this));
				} else {
					multiView.setCurrenState(IModelState.NO_STATE);
					multiView.setOnSelectClickListener(null);
				}
				icon.startShake();
			}
		}
		GuiThemeStatistics.sideOpStaticData("-1", "hi_press", 1, "-1");
		return true;
	}

	private void changeToNormalMode() {
		mIsInEdit = false;
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			GLView viewItem = getChildAt(i);
			if (viewItem instanceof GLAppDrawerAppIcon) {
				GLAppDrawerAppIcon icon = (GLAppDrawerAppIcon) viewItem;
				GLModel3DMultiView multiView = icon.getMultiView();
				multiView.setCurrenState(IModelState.NO_STATE);
				multiView.setOnSelectClickListener(null);
				icon.stopShake();
			}
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mIsInEdit) {
				changeToNormalMode();
			} else {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
						ICommonMsgId.SHOW_EXTEND_FUNC_VIEW, 0, IViewId.HIDE_APP_MANAGE);
			}
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void handleRowColumnSetting(boolean updateDB) {
		Context context = ShellAdmin.sShellManager.getActivity();
		AppFuncAutoFitManager autoFitManager = AppFuncAutoFitManager.getInstance(context);
		int iconHeight = autoFitManager.getIconHeight();
		int row = (getHeight() - getPaddingTop() - getPaddingBottom())
				/ (iconHeight + getVerticalSpacing());
		int column = 0;
		if (GoLauncherActivityProxy.isPortait()) {
			row = Math.min(row, 4);
			if (mList.size() > 9) {
				column = 4;
			} else {
				column = 3;
			}
		} else {
			row = Math.min(row, 2);
			column = 7;
		}
		mNumRows = Math.max(row, 1);
		mNumColumns = column;
	}

	@Override
	public void noActiveCallBack(Intent intent) {
		refreshGridView();
	}

	@Override
	protected void onScreenChange(int newScreen, int oldScreen) {
		// TODO Auto-generated method stub
		
	}
}
