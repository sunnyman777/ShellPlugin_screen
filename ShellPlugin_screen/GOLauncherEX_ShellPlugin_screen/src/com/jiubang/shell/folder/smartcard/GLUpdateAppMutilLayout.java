package com.jiubang.shell.folder.smartcard;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.proxy.MsgMgrProxy;
import com.go.util.AppUtils;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.shell.folder.smartcard.data.UpdateAppItem;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author guoyiqing
 * 
 */
public class GLUpdateAppMutilLayout extends GLAbsCardView implements
		OnClickListener {

	private static final String SAVE_LASTUPDATETIME = "save_lastupdatetime";
	private GLAverageLinearLayout mAverageLinearLayout;
	private List<UpdateAppItem> mUpdateAppItems;
	private GLSmartCardButton mViewAllTextView;
	private List<UpdateAppItem> mAllAppItems;

	public GLUpdateAppMutilLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GLUpdateAppMutilLayout(Context context) {
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
		mViewAllTextView = (GLSmartCardButton) findViewById(R.id.smartcard_updateapp_viewall);
		mViewAllTextView.setOnClickListener(this);
	}

	public void setUpdateApps(int column, List<UpdateAppItem> apps) {
		mAllAppItems = mUpdateAppItems = apps;
		mAverageLinearLayout.removeAllViews();
		if (apps == null || apps.isEmpty()) {
			return;
		}
		GLUpdateAppItemLayout view = null;
		mAverageLinearLayout.setColumn(column);
		int count = 0;
		for (UpdateAppItem updateAppItem : apps) {
			view = (GLUpdateAppItemLayout) ShellAdmin.sShellManager
					.getLayoutInflater().inflate(
							R.layout.gl_smartcard_update_item, null);
			view.setUpdateAppItem(updateAppItem);
			view.setOnClickListener(this);
			mAverageLinearLayout.addView(view);
			count++;
			if (count >= column) {
				break;
			}
		}
	}

	private void clickView(GLUpdateAppItemLayout view) {
		if (view == null) {
			return;
		}
		UpdateAppItem item = view.getUpdateAppItem();
		if (item == null || mUpdateAppItems.isEmpty()) {
			return;
		}
		mUpdateAppItems.remove(item);
		int count = mAverageLinearLayout.getChildCount();
		int min = Math.min(count, mUpdateAppItems.size());
		GLUpdateAppItemLayout child = null;
		for (int i = 0; i < count; i++) {
			child = (GLUpdateAppItemLayout) mAverageLinearLayout.getChildAt(i);
			if (i < min) {
				child.setUpdateAppItem(mUpdateAppItems.get(i));
			} else {
				mAverageLinearLayout.removeView(child);
			}
		}
	}

	@Override
	public void onClick(GLView v) {
		if (v == mViewAllTextView) {
			if (AppUtils.gotoMarketMyApp(mContext)) {
				saveReadLastUpdateTimeAsync();
				if (mOnCardClickListener != null) {
					mOnCardClickListener.onDismissClick(this);
				}
			} else {
				Toast.makeText(
						getContext(),
						"Make sure you have Google Market before download,please!",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			if (v instanceof GLUpdateAppItemLayout) {
				clickView((GLUpdateAppItemLayout) v);
				UpdateAppItem item = ((GLUpdateAppItemLayout) v)
						.getUpdateAppItem();
				launcherApp(item);
			}
		}
	}

	private void saveReadLastUpdateTimeAsync() {
		new Thread(SAVE_LASTUPDATETIME) {
			@Override
			public void run() {
				super.run();
				saveReadLastUpdateTime();
			}
		}.start();
	}
	
	private void saveReadLastUpdateTime() {
		if (mAllAppItems != null) {
			for (UpdateAppItem item : mAllAppItems) {
				AppItemInfo info = AppDataEngine.getInstance(mContext)
						.getAppItem(item.getMainIntent());
				if (info != null) {
					info.setReadUpdateInfoTime(mContext,
							item.getLastUpdateTime());
				}
			}
		}
	}

	private void launcherApp(UpdateAppItem item) {
		if (item != null && item.getMainIntent() != null) {
			MsgMgrProxy
					.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							ICommonMsgId.START_ACTIVITY, -1,
							item.getMainIntent(), null);
		}
	}

}
