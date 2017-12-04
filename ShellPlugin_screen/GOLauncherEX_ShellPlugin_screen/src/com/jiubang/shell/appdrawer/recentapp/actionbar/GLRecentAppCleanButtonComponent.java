package com.jiubang.shell.appdrawer.recentapp.actionbar;

import android.content.Context;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLinearLayout;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.shell.appdrawer.component.GLCleanView.OnCleanButtonClickListener;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author yangguanxiang
 *
 */
public class GLRecentAppCleanButtonComponent extends GLLinearLayout
		implements
			OnCleanButtonClickListener {

	private GLRecentAppCleanButton mBtnClean;
	private ShellTextViewWrapper mTxtTitle;

	private AppDrawerControler mAppDrawerControler;

	public GLRecentAppCleanButtonComponent(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mAppDrawerControler = AppDrawerControler
				.getInstance(ShellAdmin.sShellManager.getActivity());
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mBtnClean = (GLRecentAppCleanButton) findViewById(R.id.btn_clean);
		mBtnClean.setOnCleanButtonClickListener(this);
		mTxtTitle = (ShellTextViewWrapper) findViewById(R.id.txt_recentapp_info);
		mTxtTitle.showTextShadow();
	}

	@Override
	public void onClick() {
		mAppDrawerControler.removeAllRecentAppItems();
	}
}
