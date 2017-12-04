package com.jiubang.shell.appdrawer.recentapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.KeyEvent;

import com.go.gl.animation.Animation;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.MsgMgrProxy;
import com.go.util.graphics.DrawUtils;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.appfunc.setting.AppFuncAutoFitManager;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;
import com.jiubang.ggheart.apps.desks.appfunc.model.IBackgroundInfoChangedObserver;
import com.jiubang.ggheart.components.IQuickActionId;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunItemInfo;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.appdrawer.component.GLAppDrawerAppIcon;
import com.jiubang.shell.appdrawer.component.GLLightBaseGrid;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.common.component.HorScrollableGridViewHandler;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.popupwindow.PopupWindowControler;
import com.jiubang.shell.scroller.effector.CoupleScreenEffector;

/**
 * 
 * @author yangguanxiang
 *
 */
public class GLRecentAppGridView extends GLLightBaseGrid
		implements
			IBackgroundInfoChangedObserver,
			PopupWindowControler.ActionListener,
			BatchAnimationObserver {

	private static final long ANIMATION_DURATION_ICON_SCALE = 150;
	private static final long ANIMATION_OFFSET_ICON_SCALE = 50;
	private static final int ANIMATION_KILL = 0;
	private AppDrawerControler mControler;
	private ArrayList<FunAppItemInfo> mRecentAppList;
	private long mStartOffset = -ANIMATION_OFFSET_ICON_SCALE;
	private AnimationTask mRefreshAnimTask;
	private AnimationTask mKillAnimTask;
	private boolean mIsAnimation;

	public GLRecentAppGridView(Context context) {
		super(context);
		init();
	}

	private void init() {
		mKillAnimTask = new AnimationTask(false, AnimationTask.PARALLEL);
		mKillAnimTask.setBatchAnimationObserver(this, ANIMATION_KILL);

		mControler = AppDrawerControler.getInstance(ShellAdmin.sShellManager.getActivity());
		mControler.setRecentAppObserver(this);
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
		return IViewId.RECENT_APP;
	}

	@Override
	public void onItemClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		if (view instanceof GLAppDrawerAppIcon) {
			GLAppDrawerAppIcon icon = (GLAppDrawerAppIcon) view;
			FunAppItemInfo info = icon.getInfo();
			Intent intent = info.getIntent();
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, ICommonMsgId.START_ACTIVITY,
					-1, intent, null);
			GuiThemeStatistics.sideOpStaticData("-1", "si_rnc_click", 1, "-1");
		}
	}

	@Override
	public void refreshGridView() {
		mRecentAppList = new ArrayList<FunAppItemInfo>(
				(ArrayList<FunAppItemInfo>) mControler.getRecentFunAppItems(mNumRows * mNumColumns));
		if (!mRecentAppList.isEmpty()) {
			mNoData = false;
		} else {
			mNoData = true;
		}
		setData(mRecentAppList);
	}

	@Override
	public ShellBaseAdapter createAdapter(Context context, List infoList) {
		return new GLRecentAppAdapter(context, infoList);
	}

	@Override
	protected void handleScrollerSetting() {
		mScrollableHandler = new HorScrollableGridViewHandler(mContext, this,
				CoupleScreenEffector.PLACE_MENU, false, true) {

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

	@Override
	protected void handleRowColumnSetting(boolean updateDB) {
		Context context = ShellAdmin.sShellManager.getActivity();
		AppFuncAutoFitManager autoFitManager = AppFuncAutoFitManager.getInstance(context);
		int iconHeight = autoFitManager.getIconHeight();
		int iconWidth = autoFitManager.getIconWidth();
		mNumRows = mHeight / iconHeight;
		mNumColumns = mWidth / iconWidth;

		if (mNumRows < 1) {
			mNumRows = 1;
		}
		if (mNumColumns < 1) {
			mNumColumns = 1;
		}

		if (DrawUtils.sDensity >= 2.0f) {
			int padding = (getWidth() - mNumColumns * iconWidth) / (mNumColumns + 1) / 2;
			if (padding < 12) {
				padding = 12;
			}
			setPadding(padding, getPaddingTop(), padding, getPaddingBottom());
		}
	}

	@Override
	public boolean handleChanges(MessageID msgId, Object obj1, Object obj2) {
		//		switch (msgId) {
		//			case ALL_TASKMANAGE :
		//				processKillAllAnimation();
		//				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_PRO_MANAGE_ACTION_BAR,
		//						IAppDrawerMsgId.APPDRAWER_PRO_MANAGE_REFRESH, 1);
		//				break;
		//			case SINGLE_TASKMANAGE :
		//				processKillAnimation((FunAppItemInfo) obj1);
		//				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_PRO_MANAGE_ACTION_BAR,
		//						IAppDrawerMsgId.APPDRAWER_PRO_MANAGE_REFRESH, 1);
		//				break;
		//			case LOCK_LIST_CHANGED :
		//				refreshGridView(false);
		//				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_PRO_MANAGE_ACTION_BAR,
		//						IAppDrawerMsgId.APPDRAWER_PRO_MANAGE_REFRESH, 0);
		//				break;
		//			default :
		//				break;
		//		}
		//		return false;

		switch (msgId) {
			case CLEAR_RECENTAPP :
				processKillAllAnimation();
				break;

			default :
				break;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private void processKillAllAnimation() {
		Iterator<FunAppItemInfo> it = mRecentAppList.iterator();
		while (it.hasNext()) {
			FunAppItemInfo info = it.next();
			if (!info.isIgnore()) {
				it.remove();
			}
		}
		mStartOffset = -ANIMATION_OFFSET_ICON_SCALE;
		mKillAnimTask.reset();
		int currentScreen = ((HorScrollableGridViewHandler) mScrollableHandler).getCurrentScreen();
		ArrayList<GLView> children = ((HorScrollableGridViewHandler) mScrollableHandler)
				.getChildren(currentScreen);
		int count = children.size();
		for (int i = count - 1; i >= 0; i--) {
			IconView<FunItemInfo> icon = (IconView<FunItemInfo>) children.get(i);
			Animation animation = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setDuration(ANIMATION_DURATION_ICON_SCALE);
			mStartOffset += ANIMATION_OFFSET_ICON_SCALE;
			animation.setStartOffset(mStartOffset);
			animation.setFillAfter(true);
			mKillAnimTask.addAnimation(icon, animation, null);
		}
		if (mKillAnimTask.isValid()) {
			ShellAdmin.sShellManager.getShell().show(IViewId.PROTECTED_LAYER, false);
			mIsAnimation = true;
			GLAnimationManager.startAnimation(mKillAnimTask);
		} else {
			refreshGridView();
		}
	}

	@Override
	protected void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
		super.onLayout(isChanged, left, top, right, bottom);
		if (mIsAnimation) {
			ShellAdmin.sShellManager.getShell().hide(IViewId.PROTECTED_LAYER, false);
			mIsAnimation = false;
		}
	}

	@Override
	public void onActionClick(int action, Object target) {
		if (null == target) {
			return;
		}
		GLAppDrawerAppIcon icon = (GLAppDrawerAppIcon) target;
		Intent intent = ((FunAppItemInfo) icon.getInfo()).getAppItemInfo().mIntent;
		switch (action) {
			case IQuickActionId.INFO : {
				mControler.skipAppInfobyIntent(intent);
				break;
			}
			case IQuickActionId.LOCK : {
				mControler.addIgnoreAppItem(intent);
				break;
			}
			case IQuickActionId.UNLOCK : {
				mControler.delIgnoreAppItem(intent);
				break;
			}
			default :
				break;
		}
	}

	@Override
	public String getNoDataText() {
		return null;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SHELL_FRAME,
					ICommonMsgId.SHOW_EXTEND_FUNC_VIEW, 0, IViewId.RECENT_APP);
			return true;
		}
		return false;
	}

	@Override
	public void onStart(int what, Object[] params) {
	}

	@Override
	public void onFinish(int what, Object[] params) {
		switch (what) {
			case ANIMATION_KILL :
				refreshGridView();
				break;
			default :
				break;
		}
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		refreshGridView();
	}

	@Override
	protected void onScreenChange(int newScreen, int oldScreen) {
		// TODO Auto-generated method stub

	}

	public void onAdd() {
	}

	public void onRemove() {
	}
}