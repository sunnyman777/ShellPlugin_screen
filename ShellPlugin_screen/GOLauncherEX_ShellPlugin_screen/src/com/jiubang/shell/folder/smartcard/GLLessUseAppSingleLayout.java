package com.jiubang.shell.folder.smartcard;

import android.content.Context;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.widget.GLButton;
import com.go.gl.widget.GLImageView;
import com.jiubang.ggheart.apps.desks.diy.pref.PrefConst;
import com.jiubang.ggheart.apps.desks.diy.pref.PrivatePreference;
import com.jiubang.ggheart.components.appmanager.AppManagerUtils;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.folder.smartcard.data.LessUseAppItem;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author guoyiqing
 * 
 */
public class GLLessUseAppSingleLayout extends GLAbsCardView implements
		OnClickListener {

	private GLButton mUninstallView;
	private ShellTextViewWrapper mAppNameView;
	private ShellTextViewWrapper mAppInfoView;
	private GLImageView mAppIconView;
	private LessUseAppItem mLessAppItem;

	public GLLessUseAppSingleLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GLLessUseAppSingleLayout(Context context) {
		super(context);
		init();
	}

	private void init() {
		mOrderLevel = ICardConst.ORDER_LEVEL_UNUSEAPP;
		mCardType = ICardConst.CARD_TYPE_UNUSEAPP;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mUninstallView = (GLButton) findViewById(R.id.smartcard_lessuse_uninstall);
		mUninstallView.setOnClickListener(this);
		mAppNameView = (ShellTextViewWrapper) findViewById(R.id.app_name);
		mAppInfoView = (ShellTextViewWrapper) findViewById(R.id.app_info);
		mAppInfoView.setHasPixelOverlayed(false);
		mAppInfoView.setTextColor(0x80FFFFFF);
		mAppIconView = (GLImageView) findViewById(R.id.smartcard_app_icon);
	}

	public void setLessUseApp(LessUseAppItem item) {
		mLessAppItem = item;
		if (item == null) {
			return;
		}
		if (mAppIconView != null) {
			mAppIconView.setImageDrawable(item.getIcon());
		}
		if (mAppNameView != null) {
			mAppNameView.setText(item.getAppName());
		}
		if (mAppInfoView != null) {
			mAppInfoView.setText(mLessAppItem.getSize() + " "
					+ mContext.getString(R.string.smartcard_lessuseapp_idle)
					+ mLessAppItem.getIdleDay()
					+ mContext.getString(R.string.smartcard_lessuseapp_days));
		}
	}

	@Override
	public void onClick(GLView v) {
		if (v == mUninstallView) {
			if (mLessAppItem != null) {
				AppManagerUtils.uninstallAPK(mLessAppItem.getPackage(),
						ShellAdmin.sShellManager.getActivity());
			}
			if (mOnCardClickListener != null) {
				mOnCardClickListener.onDismissClick(this);
			}
			PrivatePreference pref = PrivatePreference
					.getPreference(getContext());
			pref.putLong(PrefConst.KEY_SMART_CARD_LESS_CREATE_TIME,
					System.currentTimeMillis());
			pref.commit();
		}
	}

}
