package com.jiubang.shell.common.component;

import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.widget.GLTextViewWrapper;
import com.go.proxy.GoLauncherLogicProxy;
import com.jiubang.shell.common.component.ShellTextViewManager.ShellTextStatusListener;

/**
 * 3D插件文字View，用于统一处理字体风格
 * @author yangguanxiang
 *
 */
public class ShellTextViewWrapper extends GLTextViewWrapper implements ShellTextStatusListener {

	private int mChangeAlpha = 255;

	public ShellTextViewWrapper(Context context) {
		super(context);
		init();
	}

	public ShellTextViewWrapper(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		onFontTypeChanged(GoLauncherLogicProxy.getAppTypeface(),
				GoLauncherLogicProxy.getAppTypefaceStyle());
		ShellTextViewManager.registerListener(this);
	}

	@Override
	public void onFontTypeChanged(Typeface tf, int style) {
		TextView textView = getTextView();
		if (textView != null) {
			textView.setTypeface(tf, style);
		}
	}

	@Override
	public void cleanup() {
		ShellTextViewManager.unregisterListener(this);
		super.cleanup();
	}

	/**
	 * 设置为斜体
	 */
	public void setItalic() {
		TextView textView = getTextView();
		if (textView != null) {
			Typeface tf = Typeface.DEFAULT;
			Resources res = getResources();
			if (res != null) {
				Configuration config = res.getConfiguration();
				if (config != null) {
					Locale locale = config.locale;
					if (locale != null) {
						String language = locale.getLanguage();
						if ("zh".equals(language) || "ko".equals(language) || "ja".equals(language)) {
							tf = Typeface.MONOSPACE;
						}
					}
				}
			}
			textView.setTypeface(tf, Typeface.ITALIC);
		}
	}

	public void changeAlpha(int alpha) {
		mChangeAlpha = alpha;
	}

	@Override
	protected void onDraw(GLCanvas canvas) {
		final int oldAlpha = canvas.getAlpha();
		if (mChangeAlpha != 255) {
			canvas.multiplyAlpha(mChangeAlpha);
		}
		super.onDraw(canvas);
		canvas.setAlpha(oldAlpha);
	}
}
