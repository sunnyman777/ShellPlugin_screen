package com.jiubang.shell.folder.smartcard;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.util.AppUtils;
import com.jiubang.ggheart.apps.desks.diy.pref.PrefConst;
import com.jiubang.ggheart.apps.desks.diy.pref.PrivatePreference;
import com.jiubang.ggheart.components.appmanager.SimpleAppManagerActivity;
import com.jiubang.shell.folder.smartcard.data.LessUseAppItem;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author guoyiqing
 * 
 */
public class GLLessUseAppMutilLayout extends GLAbsCardView implements
		OnClickListener {

	private GLAverageLinearLayout mAverageLinearLayout;
	private List<LessUseAppItem> mLessUseApps;
	private GLSmartCardButton mViewAllTextView;

	public GLLessUseAppMutilLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GLLessUseAppMutilLayout(Context context) {
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
		mAverageLinearLayout = (GLAverageLinearLayout) findViewById(R.id.smartcard_lessuse_average_layout);
		mViewAllTextView = (GLSmartCardButton) findViewById(R.id.smartcard_lessuse_viewall);
		mViewAllTextView.setOnClickListener(this);
	}

	public void setLessUseApps(int column, List<LessUseAppItem> apps) {
		mLessUseApps = apps;
		mAverageLinearLayout.removeAllViews();
		if (apps == null || apps.isEmpty()) {
			return;
		}
		GLLessUseAppItemLayout view = null;
		mAverageLinearLayout.setColumn(column);
		for (LessUseAppItem lessUseAppItem : apps) {
			view = (GLLessUseAppItemLayout) ShellAdmin.sShellManager
					.getLayoutInflater().inflate(
							R.layout.gl_smartcard_lessuse_item, null);
			view.setLessUseAppItem(lessUseAppItem);
			view.setOnClickListener(this);
			mAverageLinearLayout.addView(view);
		}
	}

	@Override
	public void onClick(GLView v) {
		Intent intent = new Intent(ShellAdmin.sShellManager.getActivity(),
				SimpleAppManagerActivity.class);
		AppUtils.safeStartActivity(ShellAdmin.sShellManager.getActivity(),
				intent);
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
