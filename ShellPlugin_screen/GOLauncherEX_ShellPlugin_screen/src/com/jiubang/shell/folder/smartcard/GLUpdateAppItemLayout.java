package com.jiubang.shell.folder.smartcard;

import android.content.Context;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.widget.GLImageView;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.folder.smartcard.data.UpdateAppItem;

/**
 * 
 * @author guoyiqing
 * 
 */
public class GLUpdateAppItemLayout extends GLLinearLayout {

	private UpdateAppItem mUpdateAppItem;
	private GLImageView mAppIconView;
	private ShellTextViewWrapper mAppNameView;
	private ShellTextViewWrapper mAppInfoView;

	public GLUpdateAppItemLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GLUpdateAppItemLayout(Context context) {
		super(context);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mAppIconView = (GLImageView) findViewById(R.id.app_icon);
		mAppNameView = (ShellTextViewWrapper) findViewById(R.id.app_name);
		mAppInfoView = (ShellTextViewWrapper) findViewById(R.id.app_info);
	}

	public void setUpdateAppItem(UpdateAppItem item) {
		mUpdateAppItem = item;
		if (mUpdateAppItem == null) {
			return;
		}
		if (mAppIconView != null) {
			mAppIconView.setImageDrawable(item.getIcon());
		}
		if (mAppNameView != null) {
			mAppNameView.setText(item.getAppName());
		}
		if (mAppInfoView != null) {
			mAppInfoView.setText(item.getSize());
		}
	}

	public UpdateAppItem getUpdateAppItem() {
		return mUpdateAppItem;
	}
	

}
