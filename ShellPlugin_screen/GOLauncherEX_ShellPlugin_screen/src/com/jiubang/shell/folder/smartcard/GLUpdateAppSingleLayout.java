package com.jiubang.shell.folder.smartcard;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.widget.GLImageView;
import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.folder.smartcard.data.UpdateAppItem;

/**
 * 
 * @author guoyiqing
 * 
 */
public class GLUpdateAppSingleLayout extends GLAbsCardView implements
		OnClickListener {

	private GLSmartCardButton mOpenView;
	private ShellTextViewWrapper mAppNameView;
	private ShellTextViewWrapper mAppInfoView;
	private GLImageView mAppIconView;
	private UpdateAppItem mUpdateAppItem;

	public GLUpdateAppSingleLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GLUpdateAppSingleLayout(Context context) {
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
		mOpenView = (GLSmartCardButton) findViewById(R.id.smartcard_updateapp_open);
		mOpenView.setOnClickListener(this);
		mAppNameView = (ShellTextViewWrapper) findViewById(R.id.app_name);
		mAppInfoView = (ShellTextViewWrapper) findViewById(R.id.app_info);
		mAppInfoView.setHasPixelOverlayed(false);
		mAppInfoView.setTextColor(0x80FFFFFF);
		mAppIconView = (GLImageView) findViewById(R.id.smartcard_app_icon);
	}

	public void setUpdateAppItem(UpdateAppItem item) {
		mUpdateAppItem = item;
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
			mAppInfoView.setText(mUpdateAppItem.getSize() + " "
					+ formate(mUpdateAppItem.getUpdateTime()));
		}
	}

	private String formate(long date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return format.format(new Date(date));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public void onClick(GLView v) {
		if (mUpdateAppItem != null && mUpdateAppItem.getMainIntent() != null) {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					ICommonMsgId.START_ACTIVITY, -1,
					mUpdateAppItem.getMainIntent(), null);
		}
		if (mOnCardClickListener != null) {
			mOnCardClickListener.onDismissClick(this);
		}
	}

}
