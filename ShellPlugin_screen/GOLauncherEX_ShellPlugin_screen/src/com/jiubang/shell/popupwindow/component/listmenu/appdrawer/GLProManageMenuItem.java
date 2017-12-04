package com.jiubang.shell.popupwindow.component.listmenu.appdrawer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLImageView;
import com.go.proxy.ApplicationProxy;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;
import com.jiubang.ggheart.apps.desks.appfunc.model.IBackgroundInfoChangedObserver;
import com.jiubang.shell.appdrawer.component.GLCleanView.OnCleanButtonClickListener;
import com.jiubang.shell.appdrawer.component.GLMemoryCleanButton;
import com.jiubang.shell.appdrawer.component.GLMemoryCleanButton.OnRefreshAnimationListener;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.theme.GLAppDrawerThemeControler;
import com.jiubang.shell.utils.ToastUtils;

/**
 * 
 * @author yangguanxiang
 *
 */
public class GLProManageMenuItem extends GLLinearLayout
		implements
			OnCleanButtonClickListener,
			OnRefreshAnimationListener,
			IBackgroundInfoChangedObserver {

	private GLMemoryCleanButton mBtnClean;
	private ShellTextViewWrapper mTxtMemoryPercent;
	private ShellTextViewWrapper mTxtMemoryDetails;
	private ShellTextViewWrapper mTxtMemoryTitle;
	private GLImageView mForwardPromanage;

	private StringBuilder mInfoBuilder = new StringBuilder();

	private AppDrawerControler mAppDrawerControler;
	private boolean mRefreshWithAnimation;

	public GLProManageMenuItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		mAppDrawerControler = AppDrawerControler
				.getInstance(ShellAdmin.sShellManager.getActivity());
		mAppDrawerControler.setProManageObserver(this);
	}

	@Override
	protected void onFinishInflate() {
		mBtnClean = (GLMemoryCleanButton) findViewById(R.id.clean_button);
		mBtnClean.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(GLView v) {
				// 空方法，防止响应menu点击事件

			}
		});
		mBtnClean.setOnCleanButtonClickListener(this);
		mBtnClean.setOnRefreshAnimationListener(this);
		mTxtMemoryPercent = (ShellTextViewWrapper) findViewById(R.id.txt_memory_percent);
		mTxtMemoryDetails = (ShellTextViewWrapper) findViewById(R.id.txt_memory_details);
		mTxtMemoryDetails.changeAlpha(128);
		mTxtMemoryTitle = (ShellTextViewWrapper) findViewById(R.id.txt_memory_title);
		mTxtMemoryTitle.setText(R.string.appdrawer_menu_item_promange_title);
		mTxtMemoryTitle.changeAlpha(128);
		mForwardPromanage = (GLImageView) findViewById(R.id.btn_forward_promanage);
		loadResource();

		mRefreshWithAnimation = false;
		mBtnClean.refresh(mRefreshWithAnimation);
	}

	private void loadResource() {
		GLAppDrawerThemeControler themeCtrl = GLAppDrawerThemeControler
				.getInstance(ApplicationProxy.getContext());
		int textColor = themeCtrl.getThemeBean().mAllAppMenuBean.mMenuTextColor;
		mTxtMemoryDetails.setTextColor(textColor);
		mTxtMemoryTitle.setTextColor(textColor);
		
		mForwardPromanage.setImageDrawable(themeCtrl.getGLDrawable(
				themeCtrl.getThemeBean().mAllAppMenuBean.mMenuForwardPromanage,
				true,
				R.drawable.gl_appdrawer_promanage_menu_item_arrow));
		mBtnClean.loadResource(true);
	}

	/**
	 * 更新内存文字
	 */
	private void updateMemoryProgressText(long usedMemory, long totalMemory, float memoryPercent) {
		mInfoBuilder.delete(0, mInfoBuilder.length());
		mInfoBuilder.append(usedMemory).append("M");
		mInfoBuilder.append("/");
		mInfoBuilder.append(totalMemory).append("M");
		mTxtMemoryDetails.setText(mInfoBuilder.toString());
		mTxtMemoryPercent.setText((int) (memoryPercent * 100.0f) + "%");
	}

	@Override
	public void onClick() {
		long availableMemory = mAppDrawerControler.retriveAvailableMemory() / 1024;
		long totalMemory = 0;
		while (totalMemory == 0) {
			totalMemory = mAppDrawerControler.retriveTotalMemory() / 1024;
		}
		mMemoryBeforeClean = totalMemory - availableMemory;
		mAppDrawerControler
				.terminateAllProManageTask(mAppDrawerControler.getProManageFunAppItems());
	}

	@Override
	public boolean handleChanges(MessageID msgId, Object obj1, Object obj2) {
		if (msgId == MessageID.ALL_TASKMANAGE) {
			mRefreshWithAnimation = true;
			mBtnClean.refresh(mRefreshWithAnimation);
			return true;
		}
		return false;
	}

	private long mMemoryBeforeClean;
	@Override
	public void onRefreshStart(long usedMemory, long totalMemory, float memoryPercent) {
	}

	@Override
	public void onRefreshing(long usedMemory, long totalMemory, float memoryPercent) {
	}

	@Override
	public void onRefreshEnd(final long usedMemory, final long totalMemory,
			final float memoryPercent) {
		if (!mRefreshWithAnimation) {
			updateMemoryProgressText(usedMemory, totalMemory, memoryPercent);
		} else {
			post(new Runnable() {

				@Override
				public void run() {
					updateMemoryProgressText(usedMemory, totalMemory, memoryPercent);
					if (mMemoryBeforeClean > usedMemory) {
						ToastUtils.showToast(
								getResources().getString(
										R.string.appdrawer_menu_item_promange_memory_release_info,
										mMemoryBeforeClean - usedMemory), Toast.LENGTH_LONG);
					}
					mMemoryBeforeClean = 0;
				}
			});
		}
	}

}
