package com.jiubang.shell.common.component;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.widget.GLTextViewWrapper;
import com.jiubang.ggheart.components.sidemenuadvert.tools.SideToolsInfo;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.shell.model.CommonImageManager;
import com.jiubang.shell.model.IModelItemType;
/**
 * SmallToolIconVIew,iconview implementation for extend other useful methods, onclick,draw,,and so on
 * @author hanson
 */
public class SmallToolIconVIew extends IconView<SideToolsInfo> {

	protected GLModel3DMultiView mMultiView;

	protected GLModel3DView mItemView;
	protected GLTextViewWrapper mTitleView;

	public SmallToolIconVIew(Context context) {
		this(context, null);
	}

	public SmallToolIconVIew(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mMultiView = (GLModel3DMultiView) findViewById(R.id.multmodel);
		mItemView = (GLModel3DView) mMultiView.findViewById(R.id.model);
		mTitleView = (GLTextViewWrapper) findViewById(R.id.app_name);
		mMultiView.setBgImageDrawable(CommonImageManager.getInstance().getDrawable(
				CommonImageManager.RES_FOLDER_BG));
		initIconFromSetting(false);
	}

	@Override
	public void setIcon(BitmapDrawable drawable) {
		if (drawable != null) {
			mItemView.changeTexture(drawable.getBitmap());
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitleView.setText(title);
	}

	public void setTitleHide(int hide) {
		mTitleView.setVisibility(hide);
	}

	public GLModel3DMultiView getMultiView() {
		return mMultiView;
	}
 
	@Override
	public void refreshIcon() {
		
		if (mInfo != null) {
			mItemView.setModelItem(IModelItemType.GENERAL_ICON);
			if (mInfo.getIcon() != null) {

				mItemView.changeTexture(((BitmapDrawable) mInfo.getIcon()).getBitmap());
			}
			String title = mInfo.getTitle();
			if (title == null || title.trim().equals("")) {
				title = AppItemInfo.DEFAULT_TITLE;
			}
			if (mTitleView != null) {
				mTitleView.setText(title);
			}
			if (mIconRefreshObserver != null) {
				mIconRefreshObserver.onIconRefresh();
			}
		}
	}

	@Override
	public void onIconRemoved() {
	}

	/**
	 * 检查单个图标在Normal状态下的当前状态
	 */
	@Override
	public void checkSingleIconNormalStatus() {

	}
 
	@Override
	public void setPressed(boolean pressed) {
		super.setPressed(pressed);
		if (pressed) {
			setAlpha(CLICK_HALF_ALPHA);
		} else {
			setAlpha(CLICK_NO_ALPHA);
		}
	}
	@Override
	public void reloadResource() {
		
	}
}
