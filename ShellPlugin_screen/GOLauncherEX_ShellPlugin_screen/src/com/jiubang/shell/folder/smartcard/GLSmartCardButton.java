package com.jiubang.shell.folder.smartcard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnTouchListener;
import com.go.gl.widget.GLButton;

/**
 * 
 * @author guoyiqing
 *
 */
public class GLSmartCardButton extends GLButton implements OnTouchListener {

	private int mCheckColor = 0xFF87b400;
	private int mUncheckColor = 0xFFFFFFFF;

	public GLSmartCardButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public GLSmartCardButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public GLSmartCardButton(Context context) {
		super(context);
		init();
	}

	private void init() {
		setDispatchTouchEventEnabled(false);
		setOnTouchListener(this);
	}
	
	public void setColor(int checkColor, int uncheckColor) {
		mCheckColor = checkColor;
		mUncheckColor  = uncheckColor;
	}
	
	@Override
	public boolean onTouch(GLView v, MotionEvent event) {
		if (v == this) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				setTextColor(mCheckColor);
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				setTextColor(mUncheckColor);
				break;
			default:
				break;
			}
		}
		return false;
	}
	
}
