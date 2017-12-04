package com.jiubang.shell.appdrawer.promanage.actionbar;

import android.content.Context;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLFrameLayout;
import com.go.proxy.SettingProxy;
import com.golauncher.message.IAppDrawerMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.shell.appdrawer.IActionBar;
import com.jiubang.shell.common.component.GLCheckBox;
import com.jiubang.shell.common.component.GLCheckBox.OnCheckedChangeListener;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 正在运行tab底部栏
 * @author yangguanxiang
 *
 */
public class GLProManageActionBar extends GLFrameLayout
		implements
			IActionBar,
			IMessageHandler,
			OnCheckedChangeListener {

	public static final int ANIMATION_DURATION = 500;
	private GLMemoryCleanButtonComponent mCleanComponent;
	private GLCheckBox mCbxHideLockedApp;
	private FunAppSetting mFunAppSetting;

	public GLProManageActionBar(Context context, AttributeSet attrs) {
		super(context, attrs);
//		MsgMgrProxy.registMsgHandler(this);
		mFunAppSetting = SettingProxy.getFunAppSetting();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mCleanComponent = (GLMemoryCleanButtonComponent) findViewById(R.id.clean_button_component);
		mCbxHideLockedApp = (GLCheckBox) findViewById(R.id.checkbox_hide_lock_apps);
		mCbxHideLockedApp.setTextColor(getResources().getColor(
				R.color.promanage_memory_info_text_color));
		mCbxHideLockedApp.setOnCheckedChangeListener(this);
		boolean showApp = FunAppSetting.SHOWAPPS == mFunAppSetting.getShowNeglectApp();
		mCbxHideLockedApp.setChecked(!showApp);
		mCbxHideLockedApp.setOnCheckedChangeListener(this);
	}

//	@Override
//	public void cleanup() {
//		super.cleanup();
//		MsgMgrProxy.unRegistMsgHandler(this);
//	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		super.dispatchDraw(canvas);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
//		mCleanComponent.layout(mCleanComponent.getLeft(),
//				mCleanComponent.getTop() - DrawUtils.dip2px(15), mCleanComponent.getRight(),
//				mCleanComponent.getBottom());
	}

	@Override
	public boolean needDrawBg() {
		return false;
	}

	@Override
	public void onInOutAnimationStart(boolean in) {

	}

	@Override
	public void onInOutAnimationEnd(boolean in) {

	}

	@Override
	public void onConfigurationChanged() {

	}

	@Override
	public boolean handleMessage(Object sender, int msgId, int param, Object... objects) {
		switch (msgId) {
			case IAppDrawerMsgId.APPDRAWER_PRO_MANAGE_REFRESH :
				mCleanComponent.refresh(param == 1 ? true : false);
				break;
			case IAppDrawerMsgId.APPDRAWER_PRO_MANAGE_CHANGE_TO_EDIT_STATE :
				handleStateChanged(param == 1 ? true : false);
				break;

			default :
				break;
		}
		return false;
	}

	@Override
	public int getMsgHandlerId() {
		return IDiyFrameIds.APP_DRAWER_PRO_MANAGE_ACTION_BAR;
	}

	@Override
	public void onCheckedChanged(GLCheckBox checkBox, boolean isChecked) {
		mFunAppSetting.setShowNeglectApp(isChecked
				? FunAppSetting.NEGLECTAPPS
				: FunAppSetting.SHOWAPPS);
		AppDrawerControler.getInstance(ShellAdmin.sShellManager.getActivity())
				.notifyLockListChange();

	}

	private void handleStateChanged(boolean changeToEdit) {
		Animation cbxAnim = null;
		mCbxHideLockedApp.setVisible(true);
		if (changeToEdit) {
			cbxAnim = new TranslateAnimation(-mCbxHideLockedApp.getWidth() * 1.5f, 0, 0, 0);
		} else {
			cbxAnim = new TranslateAnimation(0, -mCbxHideLockedApp.getWidth() * 1.5f, 0, 0);
			cbxAnim.setAnimationListener(new AnimationListenerAdapter() {

				@Override
				public void onAnimationEnd(Animation animation) {
					post(new Runnable() {

						@Override
						public void run() {
							mCbxHideLockedApp.clearAnimation();
							mCbxHideLockedApp.setVisible(false);
						}
					});
				}
			});
		}
		cbxAnim.setFillAfter(true);
		cbxAnim.setDuration(ANIMATION_DURATION);
		mCbxHideLockedApp.startAnimation(cbxAnim);
//		mCleanComponent.handleStateChanged(changeToEdit);
	}

	@Override
	public void onParentInOutAnimationStart(boolean in) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onParentInOutAnimationEnd(boolean in) {
		// TODO Auto-generated method stub
		
	}

}
