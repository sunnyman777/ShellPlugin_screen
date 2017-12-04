package com.jiubang.shell.appdrawer.search;

import android.content.Context;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLRelativeLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
/**
 * 
 * @author dingzijian
 *
 */
public class GLAppDrawerSearchHeader extends GLRelativeLayout implements OnClickListener {

	private ShellTextViewWrapper mTitleTextView;
	
	private GLSearchClearButton mClearBtn;
	
	private ClearBtnOnClickListener mClearBtnOnClickListener;
	
	public GLAppDrawerSearchHeader(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mTitleTextView = (ShellTextViewWrapper) findViewById(R.id.app_search_grid_view_ttile);
		mClearBtn = (GLSearchClearButton) findViewById(R.id.app_search_header_clear_btn);
		mClearBtn.setOnClickListener(this);
	}

	public void setText(String text) {
		mTitleTextView.setText(text);
	}
	
	public void setClearBtnVisible(boolean visible) {
		mClearBtn.setVisible(visible);
	}
	
	@Override
	public void onClick(GLView glView) {
		switch (glView.getId()) {
			case R.id.app_search_header_clear_btn :
				if (mClearBtn.isExtented() && mClearBtnOnClickListener != null) {
					mClearBtnOnClickListener.clearBtnOnClick(mClearBtn);
				}
				break;

			default :
				break;
		}
	}
	/**
	 * 
	 * @author dingzijian
	 *
	 */
	public interface ClearBtnOnClickListener {
		public void clearBtnOnClick(GLSearchClearButton glview);
	};
	
	public void setClearBtnOnClickListener(ClearBtnOnClickListener clearBtnOnClickListener) {
		mClearBtnOnClickListener = clearBtnOnClickListener;
	}
}
