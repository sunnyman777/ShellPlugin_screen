package com.jiubang.shell.folder.smartcard;

import android.content.Context;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.widget.GLImageView;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.folder.smartcard.data.LessUseAppItem;

/**
 * 
 * @author guoyiqing
 * 
 */
public class GLLessUseAppItemLayout extends GLLinearLayout {

	private LessUseAppItem mLessUseApp;
	private GLImageView mAppIconView;
	private ShellTextViewWrapper mAppNameView;
//	private ShellTextViewWrapper mAppInfoView;

	public GLLessUseAppItemLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GLLessUseAppItemLayout(Context context) {
		super(context);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mAppIconView = (GLImageView) findViewById(R.id.app_icon);
		mAppNameView = (ShellTextViewWrapper) findViewById(R.id.app_name);
//		mAppInfoView = (ShellTextViewWrapper) findViewById(R.id.app_info);
	}

	public void setLessUseAppItem(LessUseAppItem item) {
		mLessUseApp = item;
		if (mLessUseApp == null) {
			return;
		}
		if (mAppIconView != null) {
			mAppIconView.setImageDrawable(item.getIcon());
		}
		if (mAppNameView != null) {
			mAppNameView.setText(item.getAppName());
		}
//		if (mAppInfoView != null) {
//			mAppInfoView.setText(item.getSize());
//		}
	}


}
