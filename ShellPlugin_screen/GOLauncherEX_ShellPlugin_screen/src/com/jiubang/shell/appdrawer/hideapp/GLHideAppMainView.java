package com.jiubang.shell.appdrawer.hideapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.KeyEvent;
import android.view.View;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLImageView;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.golauncher.message.IAppCoreMsgId;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingUtils;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogStatusObserver;
import com.jiubang.ggheart.apps.desks.appfunc.HideAppActivity;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.apps.desks.purchase.FunctionPurchaseManager;
import com.jiubang.ggheart.common.controler.InvokeLockControler;
import com.jiubang.ggheart.common.password.PasswordActivity.ActionResultCallBack;
import com.jiubang.ggheart.data.info.DeskLockSettingInfo;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.appdrawer.component.GLAbsExtendFuncView;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 隐藏程序层
 * 
 * @author yangguanxiang
 * 
 */
public class GLHideAppMainView extends GLAbsExtendFuncView implements
		OnClickListener, IMessageHandler {

	// private GLRelativeLayout mHideAppLayout;

	private GLHideAppGridViewContainer mGridViewGroup;

	private GLImageView mLockButton;

	private GLImageView mEditButton;

	/**
	 * 进入该页面是否已经输入过了密码
	 */
	private boolean mEnterLockState;

	public GLHideAppMainView(Context context) {
		super(context);
//		MsgMgrProxy.registMsgHandler(this);
		inflateHideAppView();
		setHasPixelOverlayed(false);
		setStatusBarPadding();
	}

	private void inflateHideAppView() {
		GLLayoutInflater glLayoutInflater = ShellAdmin.sShellManager
				.getLayoutInflater();
		if (GoLauncherActivityProxy.isPortait()) {
			glLayoutInflater.inflate(R.layout.gl_hide_app_layout_port, this);
		} else {
			glLayoutInflater.inflate(R.layout.gl_hide_app_layout_land, this);
		}
	}

	private void initView() {
		mGridViewGroup = (GLHideAppGridViewContainer) findViewById(R.id.hide_app_viewgroup);
		mGridViewGroup.setGridView();
		mLockButton = (GLImageView) findViewById(R.id.hide_app_lock_button);
		if (FunctionPurchaseManager.getInstance(ApplicationProxy.getContext()).getPayFunctionState(
				FunctionPurchaseManager.PURCHASE_ITEM_SECURITY) == FunctionPurchaseManager.STATE_GONE) {
			mLockButton.setVisibility(View.GONE);
		}
		mEditButton = (GLImageView) findViewById(R.id.hide_app_edit_button);
		mLockButton.setOnClickListener(this);
		mEditButton.setOnClickListener(this);
		ShellTextViewWrapper txtTitle = (ShellTextViewWrapper) findViewById(R.id.title);
		txtTitle.showTextShadow();
		setupLockPic();
	}

	private void setupLockPic() {
		DeskLockSettingInfo settingInfo = SettingProxy.getDeskLockSettingInfo();
		if (FunctionPurchaseManager.getInstance(ApplicationProxy.getContext()).getPayFunctionState(
				FunctionPurchaseManager.PURCHASE_ITEM_SECURITY) == FunctionPurchaseManager.STATE_VISABLE) {
			mLockButton.setImageResource(R.drawable.gl_hide_app_lock_prime_selector);
		} else {
			if (settingInfo.mLockHideApp) {
				mLockButton.setImageResource(R.drawable.gl_hide_app_lock_selector);
			} else {
				mLockButton.setImageResource(R.drawable.gl_hide_app_unlock_selector);
			}
		}
	}

	@Override
	public void onAdd(GLViewGroup parent) {
		initView();
		mGridViewGroup.notifyGridDataSetChange();
		setupEnterState();
		MsgMgrProxy.registMsgHandler(this);
		super.onAdd(parent);
	}
	
	@Override
	public void onRemove() {
		MsgMgrProxy.unRegistMsgHandler(this);
		super.onRemove();
	}

	private void setupEnterState() {
		DeskLockSettingInfo settingInfo = SettingProxy.getDeskLockSettingInfo();
		if (settingInfo.mLockHideApp) {
			mEnterLockState = true;
		} else {
			mEnterLockState = false;
		}
	}

	@Override
	public boolean handleMessage(Object sender, int msgId, int param,
			Object... objects) {
		switch (msgId) {
		case IAppCoreMsgId.EVENT_UNINSTALL_APP:
		case IAppCoreMsgId.EVENT_UNINSTALL_PACKAGE:
			mGridViewGroup.notifyGridDataSetChange();
		case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED:
			removeAllViews();
			inflateHideAppView();
			onAdd(this);
			break;
		case ICommonMsgId.COMMON_ON_HOME_ACTION:
			// 这个是3D插件的home键消息
			removeHideAppView();
			break;
		case IFrameworkMsgId.SYSTEM_ON_RESUME:
			mGridViewGroup.notifyGridDataSetChange();
			setupLockPic();
			break;
		default:
			break;
		}
		return false;
	}

	private void removeHideAppView() {
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
				ICommonMsgId.SHOW_EXTEND_FUNC_VIEW, 0, IViewId.HIDE_APP_MANAGE);
	}

	@Override
	public void onClick(GLView v) {
		int id = v.getId();
		switch (id) {
			case R.id.hide_app_edit_button :
				DialogStatusObserver observer = DialogStatusObserver.getInstance();
				if (observer.isDialogShowing()) {
					return;
				}
			if (SettingProxy.getScreenSettingInfo().mLockScreen) { // 判断当前是否锁屏
					LockScreenHandler.showLockScreenNotification(ShellAdmin.sShellManager
								.getShell().getActivity());
				return;
			}
				Intent hideIntent = new Intent(ShellAdmin.sShellManager.getShell().getActivity(),
						HideAppActivity.class);
			ShellAdmin.sShellManager.getShell().startActivitySafely(hideIntent);
			GuiThemeStatistics.sideOpStaticData("-1", "hi_add", 1, "-1");
			break;

			case R.id.hide_app_lock_button :
				// 设置的上锁和解锁，还有付费
				if (FunctionPurchaseManager.getInstance(ApplicationProxy.getContext())
						.getPayFunctionState(FunctionPurchaseManager.PURCHASE_ITEM_SECURITY) == FunctionPurchaseManager.STATE_CAN_USE) {
					InvokeLockControler controler = InvokeLockControler.getInstance(mShell
							.getActivity());
					final DeskLockSettingInfo settingInfo = SettingProxy.getDeskLockSettingInfo();
					String password = controler.getPassWord();
					if (password == null || !mEnterLockState) {
						controler.startLockAction(0, new ActionResultCallBack() {

							@Override
							public void onUnlockSuccess(int actionId) {
								mEnterLockState = true;
								// 换成上锁的图片，改变设置项属性
								mLockButton.setImageResource(R.drawable.gl_hide_app_lock_selector);
								settingInfo.mLockHideApp = true;
								SettingProxy.updateDesLockSettingInfo(settingInfo);
							}

							@Override
							public void onUnlockFail(int actionId) {

							}
						}, mShell.getActivity(), ((BitmapDrawable) mContext.getResources()
								.getDrawable(R.drawable.gl_hide_app_icon)).getBitmap(), mContext
								.getString(R.string.menuitem_hide_tilt));
					} else {
						// 换图片，改变设置项属性
						if (settingInfo.mLockHideApp) {
							mLockButton.setImageResource(R.drawable.gl_hide_app_unlock_selector);
							settingInfo.mLockHideApp = false;
						} else {
							mLockButton.setImageResource(R.drawable.gl_hide_app_lock_selector);
							settingInfo.mLockHideApp = true;
						}
						SettingProxy.updateDesLockSettingInfo(settingInfo);
					}
					GuiThemeStatistics.sideOpStaticData("-1", "hi_lock", 1, "-1");
				} else {
					DeskSettingUtils.showPayDialog(mShell.getActivity(), 502);
				}
				break;

			default :
			break;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return mGridViewGroup.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return mGridViewGroup.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		return mGridViewGroup.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		return mGridViewGroup.onKeyMultiple(keyCode, repeatCount, event);
	}

	@Override
	public int getViewId() {
		return IViewId.HIDE_APP_MANAGE;
	}

	@Override
	public int getMsgHandlerId() {
		return IDiyFrameIds.APP_DRAWER_HIDE_APP_MANAGE;
	}
}
