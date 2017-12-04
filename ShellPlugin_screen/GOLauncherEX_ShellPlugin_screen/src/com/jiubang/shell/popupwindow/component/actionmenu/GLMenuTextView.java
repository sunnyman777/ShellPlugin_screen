package com.jiubang.shell.popupwindow.component.actionmenu;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.go.gl.widget.GLTextViewWrapper;
import com.jiubang.ggheart.components.ISelfObject;
import com.jiubang.ggheart.components.TextFont;
import com.jiubang.ggheart.components.TextFontInterface;

/**
 * 
 * @author zouguiquan
 *
 */
public class GLMenuTextView extends GLTextViewWrapper implements ISelfObject, TextFontInterface {

	private TextFont mTextFont;
	private Typeface mTypeface;
	private int mStyle;

	public GLMenuTextView(Context context) {
		this(context, null);
	}

	public GLMenuTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GLMenuTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		selfConstruct();
	}

	public void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top,
			Drawable right, Drawable bottom) {
		getTextView().setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
	}

	public void setTypeface(Typeface tf, int style) {
		getTextView().setTypeface(tf, style);
	}

	@Override
	public void onInitTextFont() {
		if (mTextFont == null) {
			mTextFont = new TextFont(this);
		}
	}

	@Override
	public void onUninitTextFont() {
		if (null != mTextFont) {
			mTextFont.selfDestruct();
			mTextFont = null;
		}
	}

	@Override
	public void onTextFontChanged(Typeface typeface, int style) {
		mTypeface = typeface;
		mStyle = style;
		setTypeface(mTypeface, mStyle);
	}

	@Override
	public void selfConstruct() {
		onInitTextFont();
	}

	@Override
	public void selfDestruct() {
		onUninitTextFont();
	}
}
