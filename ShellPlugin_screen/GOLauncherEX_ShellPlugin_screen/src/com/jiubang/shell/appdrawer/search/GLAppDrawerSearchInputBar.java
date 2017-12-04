package com.jiubang.shell.appdrawer.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLRelativeLayout;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLEditText;
import com.go.gl.widget.GLImageButton;
import com.go.proxy.ApplicationProxy;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;

/**
 * 
 * @author dingzijian
 *
 */
public class GLAppDrawerSearchInputBar extends GLRelativeLayout
		implements
			TextWatcher,
			com.go.gl.view.GLView.OnClickListener {
	private GLEditText mSearchKeyEditText;

	private GLImageButton mClearKeyBtn;

	private SearchKeyChangedListener mKeyChangedListener;

	private Runnable mRunnable;

	private String mLastSearchKey = "";

	public GLAppDrawerSearchInputBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		Drawable drawableLeft = mContext.getResources().getDrawable(
				R.drawable.gl_appdrawer_search_input_bar_loune);
		drawableLeft.setBounds(0, 0, drawableLeft.getMinimumWidth(),
				drawableLeft.getMinimumHeight()); //必须设置图片大小，否则不显示
		Drawable drawableRight = mContext.getResources().getDrawable(android.R.color.transparent);
		drawableRight.setBounds(r - DrawUtils.dip2px(36), 0, r, 0); //必须设置图片大小，否则不显示
		mSearchKeyEditText.getEditText().setCompoundDrawables(drawableLeft, null, drawableRight,
				null);
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mSearchKeyEditText = (GLEditText) findViewById(R.id.gl_search_edit_text);
		mSearchKeyEditText.setHint(ApplicationProxy.getContext().getString(
				com.gau.go.launcherex.R.string.app_search_hint));
		mSearchKeyEditText.getEditText().setLongClickable(false);
		if (Build.VERSION.SDK_INT >= 11) {
			mSearchKeyEditText.getEditText().setCustomSelectionActionModeCallback(
					new ActionMode.Callback() {

						@Override
						public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
							return false;
						}

						@Override
						public void onDestroyActionMode(ActionMode mode) {

						}

						@Override
						public boolean onCreateActionMode(ActionMode mode, Menu menu) {
							return false;
						}

						@Override
						public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
							return false;
						}
					});
		}
		mClearKeyBtn = (GLImageButton) findViewById(R.id.gl_search_clear_key_btn);
		mClearKeyBtn.setOnClickListener(this);
		if (StatusBarHandler.isHide()) {
			GLRelativeLayout.LayoutParams layoutParams = new LayoutParams(
					LayoutParams.MATCH_PARENT, DrawUtils.dip2px(36));
			layoutParams.addRule(GLRelativeLayout.CENTER_VERTICAL, GLRelativeLayout.TRUE);
			mSearchKeyEditText.setLayoutParams(layoutParams);
			GLRelativeLayout.LayoutParams laParams = new LayoutParams(LayoutParams.MATCH_PARENT,
					DrawUtils.dip2px(83) - StatusBarHandler.getStatusbarHeight());
			setLayoutParams(laParams);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(final Editable s) {
		final String key = s.toString();
		mRunnable = new Runnable() {

			@Override
			public void run() {
				Editable editable = mSearchKeyEditText.getText();
				String text = editable != null ? editable.toString() : null;
				if (key == null || !key.equals(text)) {
					return;
				}
				if (mKeyChangedListener != null && !mLastSearchKey.equals(key)) {
					mKeyChangedListener.afterSearchKeyChanged(key);
					mLastSearchKey = key;
				}
				if ("".equals(key)) {
					mClearKeyBtn.setVisible(false);
				} else {
					mClearKeyBtn.setVisible(true);
				}

			}
		};
		postDelayed(mRunnable, 600);
	}

	@Override
	public void onClick(GLView view) {
		switch (view.getId()) {
			case R.id.gl_search_clear_key_btn :
				mSearchKeyEditText.setText("");

				if (mKeyChangedListener != null) {
					mKeyChangedListener.clearSearchKey();
				}
				break;

			default :
				break;
		}
	}

	public boolean showIM(boolean show) {
		InputMethodManager methodManager = (InputMethodManager) ApplicationProxy.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		boolean isHIde = false;
		if (show) {
			mSearchKeyEditText.requestFocus();
			isHIde = methodManager.showSoftInput(mSearchKeyEditText.getEditText(),
					InputMethodManager.SHOW_IMPLICIT);
		} else {
			isHIde = methodManager.hideSoftInputFromWindow(mSearchKeyEditText.getEditText()
					.getWindowToken(), 0);
		}
		return isHIde;
	}

	public String getEditTextSearchKey() {
		return mSearchKeyEditText.getEditText().getText().toString();
	}
	public void setSearchKeyChangedListener(SearchKeyChangedListener keyChangedListener) {
		if (keyChangedListener == null) {
			mSearchKeyEditText.getEditText().removeTextChangedListener(this);
		} else {
			mSearchKeyEditText.addTextChangedListener(this);
		}
		this.mKeyChangedListener = keyChangedListener;
	}
	/**
	 * 
	 * @author dingzijian
	 *
	 */
	public interface SearchKeyChangedListener {
		public void afterSearchKeyChanged(String searchKey);

		public void clearSearchKey();
	}
}
