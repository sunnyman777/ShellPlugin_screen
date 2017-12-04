package com.jiubang.shell.appdrawer.promanage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.KeyEvent;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.MsgMgrProxy;
import com.go.util.graphics.DrawUtils;
import com.golauncher.message.IAppDrawerMsgId;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.appfunc.setting.AppFuncAutoFitManager;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;
import com.jiubang.ggheart.apps.desks.appfunc.model.IBackgroundInfoChangedObserver;
import com.jiubang.ggheart.components.IQuickActionId;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunItemInfo;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.animation.DragAnimation;
import com.jiubang.shell.animation.DragAnimation.DragAnimationInfo;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.appdrawer.component.GLAppDrawerAppIcon;
import com.jiubang.shell.appdrawer.component.GLLightBaseGrid;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.common.component.GLModel3DMultiView;
import com.jiubang.shell.common.component.HorScrollableGridViewHandler;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.listener.TerminateAppListener;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.effect.EffectController.EffectListener;
import com.jiubang.shell.effect.IconCircleEffect;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.model.IModelState;
import com.jiubang.shell.popupwindow.PopupWindowControler;
import com.jiubang.shell.scroller.effector.CoupleScreenEffector;

/**
 * 
 * @author yangguanxiang
 *
 */
public class GLProManageGridView extends GLLightBaseGrid
		implements
			IBackgroundInfoChangedObserver,
			PopupWindowControler.ActionListener,
			BatchAnimationObserver,
			IMessageHandler {

	private static final long ANIMATION_DURATION_ICON_SCALE = 150;
	private static final long ANIMATION_OFFSET_ICON_SCALE = 50;
	private static final int ANIMATION_REFRESH_GRID_VIEW = 0;
	private static final int ANIMATION_KILL = 1;
	private boolean mIsInEdit;
	private AppDrawerControler mControler;
	private PopupWindowControler mPopupWindowControler;
	private DragController mDragControler;
	private float[] mDragTransInfo = new float[5];
	private ArrayList<FunAppItemInfo> mProManageList;
	private ArrayList<FunAppItemInfo> mAnimatonInfoList = new ArrayList<FunAppItemInfo>();
	private long mStartOffset = -ANIMATION_OFFSET_ICON_SCALE;
	private AnimationTask mRefreshAnimTask;
	private AnimationTask mKillAnimTask;
	private boolean mIsAnimation;

	public GLProManageGridView(Context context) {
		super(context);
		init();
	}

	private void init() {
		mRefreshAnimTask = new AnimationTask(false, AnimationTask.PARALLEL);
		mRefreshAnimTask.setBatchAnimationObserver(this, ANIMATION_REFRESH_GRID_VIEW);
		mKillAnimTask = new AnimationTask(false, AnimationTask.PARALLEL);
		mKillAnimTask.setBatchAnimationObserver(this, ANIMATION_KILL);

		mControler = AppDrawerControler.getInstance(ShellAdmin.sShellManager.getActivity());
		handleScrollerSetting();
	}

	@Override
	public void setDragController(DragController dragger) {
		mDragControler = dragger;
	}

	@Override
	public void setTopViewId(int id) {

	}

	@Override
	public int getTopViewId() {
		return IViewId.PRO_MANAGE;
	}

	@Override
	public void onItemClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		if (!mIsInEdit) {
			((IconView<?>) view).startClickEffect(new EffectListener() {

				@Override
				public void onEffectComplete(Object callBackFlag) {
					GLAppDrawerAppIcon icon = (GLAppDrawerAppIcon) callBackFlag;
					FunAppItemInfo info = icon.getInfo();
					Intent intent = info.getIntent();
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							ICommonMsgId.START_ACTIVITY, -1, intent, null);
				}

				@Override
				public void onEffectStart(Object callBackFlag) {
				}
			}, IconCircleEffect.ANIM_DURATION_CLICK, false);
		} else {
			showQuickActionMenu(view);
		}
	}

	public void hideQuickActionMenu() {
		if (mPopupWindowControler != null) {
			mPopupWindowControler.dismiss(true);
		}
	}

	private boolean showQuickActionMenu(GLView target) {
		if (target == null) {
			return false;
		}
		GLAppDrawerAppIcon icon = (GLAppDrawerAppIcon) target;
		FunAppItemInfo info = (FunAppItemInfo) icon.getInfo();

		int[] xy = new int[2];
		target.getLocationInWindow(xy);
		Rect targetRect = new Rect(xy[0], xy[1], xy[0] + target.getWidth(), xy[1]
				+ target.getHeight());

		mPopupWindowControler = ShellAdmin.sShellManager.getShell().getPopupWindowControler();

		Resources res = ShellAdmin.sShellManager.getContext().getResources();
		if (res != null) {
			mPopupWindowControler.addQuickActionMenuItem(IQuickActionId.INFO,
					res.getDrawable(R.drawable.gl_appdrawer_promanage_info),
					res.getString(R.string.infotext));
			if (!info.isIgnore()) {
				mPopupWindowControler.addQuickActionMenuItem(IQuickActionId.LOCK,
						res.getDrawable(R.drawable.gl_appdrawer_promanage_lock),
						res.getString(R.string.lock2text));
			} else {
				mPopupWindowControler.addQuickActionMenuItem(IQuickActionId.UNLOCK,
						res.getDrawable(R.drawable.gl_appdrawer_promanage_unlock),
						res.getString(R.string.unlock2text));
			}
			mPopupWindowControler.showQuickActionMenu(targetRect, target, target, this, this);
		}
		return true;
	}

	@Override
	public boolean onItemLongClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		boolean ret = super.onItemLongClick(parent, view, position, id);
		GuiThemeStatistics.sideOpStaticData("-1", "rg_press", 1, "-1");
		if (!mIsInEdit) {
			changeToEditMode();
		}
		if (view instanceof IconView) {
			mDragControler.startDrag(view, this, ((IconView) view).getInfo(),
					DragController.DRAG_ACTION_MOVE, mDragTransInfo,
					new DragAnimationInfo(true, DragController.DRAG_ICON_SCALE, false,
							DragAnimation.DURATION_100, null));
			ret = true;
		}
		return ret;
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
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_PRO_MANAGE_ACTION_BAR,
				IAppDrawerMsgId.APPDRAWER_PRO_MANAGE_CHANGE_TO_EDIT_STATE, 0);
	}

	private void changeToEditMode() {
		mIsInEdit = true;
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			GLView viewItem = getChildAt(i);
			if (viewItem instanceof GLAppDrawerAppIcon) {
				GLAppDrawerAppIcon icon = (GLAppDrawerAppIcon) viewItem;
				GLModel3DMultiView multiView = icon.getMultiView();
				FunAppItemInfo info = (FunAppItemInfo) icon.getInfo();
				multiView.setCurrenState(IModelState.KILL_STATE);
				multiView.setOnSelectClickListener(new TerminateAppListener(info));
				icon.startShake();
			}
		}
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_PRO_MANAGE_ACTION_BAR,
				IAppDrawerMsgId.APPDRAWER_PRO_MANAGE_CHANGE_TO_EDIT_STATE, 1);
	}

	@Override
	public void dataChangeOnMoveStart(Object dragInfo, int targetIndex, int sourceIndex) {

	}

	@Override
	public void dataChangeOnMoveEnd(Object dragInfo, int targetIndex, int sourceIndex) {

	}

	@Override
	public void dataChangeOnDrop(Object dragInfo, int targetIndex, int sourceIndex) {

	}

	@Override
	public void refreshGridView() {
		mProManageList = new ArrayList<FunAppItemInfo>(
				(ArrayList<FunAppItemInfo>) mControler.getProManageFunAppItems());
		if (!mProManageList.isEmpty()) {
			mNoData = false;
		} else {
			mNoData = true;
		}
		setData(mProManageList);
	}

	private void refreshGridView(boolean animate) {
		if (animate) {
			ArrayList<FunAppItemInfo> proMangeList = mControler.getProManageFunAppItems();
			Iterator<FunAppItemInfo> it = mProManageList.iterator();
			while (it.hasNext()) {
				FunAppItemInfo funAppItemInfo = it.next();
				if (!proMangeList.contains(funAppItemInfo)) {
					it.remove();
				}
			}
			mAnimatonInfoList.clear();
			for (FunAppItemInfo funAppItemInfo : proMangeList) {
				if (!mProManageList.contains(funAppItemInfo)) {
					if (funAppItemInfo.isIgnore()) {
						mProManageList.add(0, funAppItemInfo);
					} else {
						mProManageList.add(funAppItemInfo);
					}
					mAnimatonInfoList.add(funAppItemInfo);
				}
			}

			if (!mProManageList.isEmpty()) {
				mNoData = false;
			} else {
				mNoData = true;
			}
			mRefreshAnimTask.reset();
			mStartOffset = -ANIMATION_OFFSET_ICON_SCALE;
			setData(mProManageList);
		} else {
			refreshGridView();
		}
	}

	@Override
	public ShellBaseAdapter createAdapter(Context context, List infoList) {
		return new GLProManageAdapter(context, infoList);
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
		switch (msgId) {
			case ALL_TASKMANAGE :
				processKillAllAnimation();
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_PRO_MANAGE_ACTION_BAR,
						IAppDrawerMsgId.APPDRAWER_PRO_MANAGE_REFRESH, 1);
				break;
			case SINGLE_TASKMANAGE :
				processKillAnimation((FunAppItemInfo) obj1);
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_PRO_MANAGE_ACTION_BAR,
						IAppDrawerMsgId.APPDRAWER_PRO_MANAGE_REFRESH, 1);
				break;
			case LOCK_LIST_CHANGED :
				refreshGridView(false);
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_PRO_MANAGE_ACTION_BAR,
						IAppDrawerMsgId.APPDRAWER_PRO_MANAGE_REFRESH, 0);
				break;
			default :
				break;
		}
		return false;
	}

	private void processKillAnimation(FunAppItemInfo info) {
		mKillAnimTask.reset();

		GLView appIcon = mAdapter.getViewByItem(info);
		if (appIcon != null) {
			Animation animation = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setDuration(ANIMATION_DURATION_ICON_SCALE);
			animation.setFillAfter(true);
			mKillAnimTask.addAnimation(appIcon, animation, null);
			mProManageList.remove(info);
			ShellAdmin.sShellManager.getShell().show(IViewId.PROTECTED_LAYER, false);
			mIsAnimation = true;
			GLAnimationManager.startAnimation(mKillAnimTask);
		}
	}

	@SuppressWarnings("unchecked")
	private void processKillAllAnimation() {
		Iterator<FunAppItemInfo> it = mProManageList.iterator();
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
			FunAppItemInfo info = (FunAppItemInfo) icon.getInfo();
			if (!info.isIgnore()) {
				Animation animation = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF,
						0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setDuration(ANIMATION_DURATION_ICON_SCALE);
				mStartOffset += ANIMATION_OFFSET_ICON_SCALE;
				animation.setStartOffset(mStartOffset);
				animation.setFillAfter(true);
				mKillAnimTask.addAnimation(icon, animation, null);
				//				mProManageList.remove(info);
			}
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
		if (!mAnimatonInfoList.isEmpty()) {
			processRefreshGridViewAnimation();
		} else {
			if (mIsAnimation) {
				ShellAdmin.sShellManager.getShell().hide(IViewId.PROTECTED_LAYER, false);
				mIsAnimation = false;
			}
		}
	}

	@Override
	public void callBackToChild(GLView view) {
		super.callBackToChild(view);
		if (view instanceof GLAppDrawerAppIcon) {
			GLAppDrawerAppIcon appIcon = (GLAppDrawerAppIcon) view;
			appIcon.clearAnimation();
			if (mIsInEdit) {
				appIcon.startShake();
			}
			FunAppItemInfo info = appIcon.getInfo();
			if (info.isIgnore()) {
				appIcon.getMultiView().setLowerRightState(IModelState.LOCK_STATE);
			} else {
				appIcon.getMultiView().setLowerRightState(IModelState.NO_STATE);
			}
			if (mIsInEdit) {
				GLModel3DMultiView multiView = appIcon.getMultiView();
				if (multiView.getCurrentState() != IModelState.KILL_STATE) {
					multiView.setCurrenState(IModelState.KILL_STATE);
					multiView.setOnSelectClickListener(new TerminateAppListener(info));
				}
			}
			//			processRefreshGridViewAnimation(appIcon, info);
		}
	}

	private void processRefreshGridViewAnimation() {
		int currentScreen = ((HorScrollableGridViewHandler) mScrollableHandler).getCurrentScreen();
		ArrayList<GLView> children = ((HorScrollableGridViewHandler) mScrollableHandler)
				.getChildren(currentScreen);
		for (GLView child : children) {
			IconView<FunItemInfo> icon = (IconView<FunItemInfo>) child;
			FunAppItemInfo info = (FunAppItemInfo) icon.getInfo();
			if (/*!info.isIgnore() && */mAnimatonInfoList.contains(info)) {
				Animation animation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF,
						0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setDuration(ANIMATION_DURATION_ICON_SCALE);
				mStartOffset += ANIMATION_OFFSET_ICON_SCALE;
				animation.setStartOffset(mStartOffset);
				mRefreshAnimTask.addAnimation(icon, animation, null);
			}
		}
		mAnimatonInfoList.clear();

		if (mRefreshAnimTask.isValid()) {
			ShellAdmin.sShellManager.getShell().show(IViewId.PROTECTED_LAYER, false);
			mIsAnimation = true;
			GLAnimationManager.startAnimation(mRefreshAnimTask);
		} else {
			ShellAdmin.sShellManager.getShell().hide(IViewId.PROTECTED_LAYER, false);
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
			if (mIsInEdit) {
				changeToNormalMode();
			} else {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SHELL_FRAME,
						ICommonMsgId.SHOW_EXTEND_FUNC_VIEW, 0, IViewId.PRO_MANAGE);
			}
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
			case ANIMATION_REFRESH_GRID_VIEW :
				ShellAdmin.sShellManager.getShell().hide(IViewId.PROTECTED_LAYER, false);
				break;
			case ANIMATION_KILL :
				refreshGridView(true);
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
	protected int getLongPressTimeout() {
		if (mIsInEdit) {
			return QUICK_LONG_PRESS_TIMEOUT;
		}
		return super.getLongPressTimeout();
	}

	@Override
	protected void onScreenChange(int newScreen, int oldScreen) {
		// TODO Auto-generated method stub

	}

	public void onAdd() {
		mControler.setProManageObserver(this);
		MsgMgrProxy.registMsgHandler(this);
	}

	public void onRemove() {
		mControler.setProManageObserver(null);
		MsgMgrProxy.unRegistMsgHandler(this);
	}
	
	@Override
	public boolean onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo,
			DropAnimationInfo resetInfo) {
		resetInfo.setNeedToShowCircle(false);
		return super.onDrop(source, x, y, xOffset, yOffset, dragView, dragInfo,
				resetInfo);
	}

	@Override
	public int getMsgHandlerId() {
		return IDiyFrameIds.APP_DRAWER_PRO_MANAGE_GRID_VIEW;
	}

	@Override
	public boolean handleMessage(Object who, int msgId, int param, Object... objs) {
		// TODO Auto-generated method stub
		return false;
	}
}