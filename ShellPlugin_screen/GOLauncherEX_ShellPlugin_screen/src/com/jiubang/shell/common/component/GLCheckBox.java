package com.jiubang.shell.common.component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout.LayoutParams;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.widget.GLImageView;

/**
 * 
 * @author yangguanxiang
 *
 */
public class GLCheckBox extends GLLinearLayout implements OnClickListener {

	//	private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

	private GLImageView mImageHook;
	private ShellTextViewWrapper mTxtInfo;
	private OnCheckedChangeListener mOnCheckedChangeListener;
	private boolean mChecked;
	private Drawable mCheckDrawable;
	private Drawable mUncheckDrawable;
	private boolean mTitleOnLeft;
	public GLCheckBox(Context context) {
		super(context);
		init();
	}

	public GLCheckBox(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		int textResId = attrs == null ? 0 : attrs.getAttributeResourceValue(
				"http://schemas.android.com/apk/res/android", "text", 0);
		if (textResId > 0) {
			setText(textResId);
		}
	}

	private void init() {
		setOnClickListener(this);
		setGravity(Gravity.CENTER_VERTICAL);
		setOrientation(GLLinearLayout.HORIZONTAL);
		mImageHook = new GLImageView(mContext);
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		addView(mImageHook, lp);
		setHookDrawable(R.drawable.gl_checkbox_unchecked, R.drawable.gl_checkbox_checked);

		mTxtInfo = new ShellTextViewWrapper(mContext);
		mTxtInfo.setSingleLine();
		mTxtInfo.setTextColor(Color.WHITE);
		mTxtInfo.setTextSize(14);
		mTxtInfo.showTextShadow();
		lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		addView(mTxtInfo, lp);
	}

	/**
	 * title在checkBox左边，true为左边，flase为右边。默认为右边
	 * @param flag
	 */
	public void setTitleOnLeft(boolean flag) {
		if (mTitleOnLeft == flag) {
			return;
		}
		removeView(mImageHook);
		removeView(mTxtInfo);
		mTitleOnLeft = flag;
		if (mTitleOnLeft) {
			addView(mTxtInfo);
			addView(mImageHook);
		} else {
			addView(mImageHook);
			addView(mTxtInfo);
		}
	}

	public void setTextSize(int size) {
		mTxtInfo.setTextSize(size);
	}

	public void setTextColor(int color) {
		mTxtInfo.setTextColor(color);
	}
	
	public void showTextShadow() {
		mTxtInfo.showTextShadow();
	}
	
	public void hideTextShadow() {
		mTxtInfo.hideTextShadow();
	}

	public void setHookDrawable(Drawable uncheck, Drawable check) {
		//		mImageHook.setBackgroundDrawable(uncheck);
		//		StateListDrawable stateDrawable = new StateListDrawable();
		//		stateDrawable.addState(CHECKED_STATE_SET, check);
		//		mImageHook.setImageDrawable(stateDrawable);

		if (uncheck instanceof GLDrawable || check instanceof GLDrawable) {
			throw new IllegalArgumentException("drawable cannot be GLDrawable");
		}
		mUncheckDrawable = uncheck;
		mCheckDrawable = check;
		updateCheckDrawable();
	}

	private void updateCheckDrawable() {
		mImageHook.setBackgroundDrawable(mUncheckDrawable);
		if (isChecked()) {
			mImageHook.setImageDrawable(mCheckDrawable);
		} else {
			mImageHook.setImageDrawable(null);
		}
	}

	public void setHookDrawable(int uncheck, int check) {
		//		setHookDrawable(GLImageUtil.getGLDrawable(uncheck), GLImageUtil.getGLDrawable(check));
		setHookDrawable(getResources().getDrawable(uncheck), getResources().getDrawable(check));
	}

	public void setText(CharSequence text) {
		mTxtInfo.setText(text);
	}

	public void setText(int resId) {
		mTxtInfo.setText(resId);
	}

	public void setChecked(boolean checked) {
		if (mChecked != checked) {
			mChecked = checked;
			//			refreshDrawableState();
			updateCheckDrawable();
			if (mOnCheckedChangeListener != null) {
				mOnCheckedChangeListener.onCheckedChanged(this, mChecked);
			}
		}
	}

	public boolean isChecked() {
		return mChecked;
	}

	public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
		mOnCheckedChangeListener = listener;
	}

	/**
	 * 
	 * @author yangguanxiang
	 *
	 */
	public static interface OnCheckedChangeListener {
		void onCheckedChanged(GLCheckBox checkBox, boolean isChecked);
	}

	public void toggle() {
		setChecked(!mChecked);
	}

	//	@Override
	//	protected int[] onCreateDrawableState(int extraSpace) {
	//		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
	//		if (isChecked()) {
	//			mergeDrawableStates(drawableState, CHECKED_STATE_SET);
	//		}
	//		return drawableState;
	//	}
	//
	//	@Override
	//	protected void drawableStateChanged() {
	//		super.drawableStateChanged();
	//		Drawable stateDrawable = mImageHook.getDrawable();
	//		if (stateDrawable != null) {
	//			int[] myDrawableState = getDrawableState();
	//			stateDrawable.setState(myDrawableState);
	//			invalidate();
	//		}
	//	}

	@Override
	public void onClick(GLView v) {
		toggle();
	}
}
