package com.jiubang.shell.folder.smartcard;

import android.content.Context;
import android.util.AttributeSet;

import com.go.gl.view.GLLinearLayout;

/**
 * 
 * @author guoyiqing
 *
 */
public abstract class GLAbsCardView extends GLLinearLayout {

	private boolean mShowed; 
	protected int mOrderLevel;
	protected int mCardType;
	protected OnCardClickListener mOnCardClickListener;
	
	public GLAbsCardView(Context context, AttributeSet attrs) {
		super(context, attrs); 
	}
	
	public GLAbsCardView(Context context) {
		super(context);
	}

	public int getOrderLevel() {
		return mOrderLevel;
	}
	
	public boolean isShowed() {
		return mShowed;
	}
	
	public void setShowed(boolean show) {
		mShowed = show;
	}
	
	public int getCardType() {
		return mCardType;
	}
	
	public void setCardClickListener(OnCardClickListener listener) {
		mOnCardClickListener = listener;
	}
	
}
