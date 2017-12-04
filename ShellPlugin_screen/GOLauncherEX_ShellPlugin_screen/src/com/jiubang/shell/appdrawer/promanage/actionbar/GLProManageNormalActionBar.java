package com.jiubang.shell.appdrawer.promanage.actionbar;

import android.content.Context;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLinearLayout;
import com.jiubang.shell.appdrawer.IActionBar;
import com.jiubang.shell.common.component.ShellTextViewWrapper;

/**
 * 正在运行顶部Title
 * @author yangguanxiang
 *
 */
public class GLProManageNormalActionBar extends GLLinearLayout implements IActionBar {

	private ShellTextViewWrapper mTxtTitle;
	public GLProManageNormalActionBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		mTxtTitle = (ShellTextViewWrapper) findViewById(R.id.txt_title);
		mTxtTitle.showTextShadow();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		//		int offset = DrawUtils.dip2px(6);
		//		mTextView.layout(mTextView.getLeft(), mTextView.getTop() + offset, mTextView.getRight(),
		//				mTextView.getBottom() + offset);
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
		// TODO Auto-generated method stub

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
