package com.jiubang.shell.appdrawer.search;

import android.content.Context;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLDrawable;
import com.jiubang.shell.common.component.GLExtentButton;
import com.jiubang.shell.utils.GLImageUtil;

/**
 * 最近打开扫把按钮
 * @author wuziyi
 *
 */
public class GLSearchClearButton extends GLExtentButton {

	public GLSearchClearButton(Context context) {
		super(context);
	}
	
	public GLSearchClearButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GLSearchClearButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected String getTextContent() {
		return mContext.getString(R.string.appfunc_no_recent_clear);
	}

	@Override
	protected GLDrawable getOriginalImage() {
		return GLImageUtil.getGLDrawable(R.drawable.gl_appdrawer_slide_menu_recent_clear);
	}

	@Override
	protected GLDrawable getTouchDownBg() {
		return GLImageUtil.getGLDrawable(R.drawable.gl_appdrawer_slide_menu_clear_bg_light);
	}

	@Override
	protected GLDrawable getExtentBg() {
		return GLImageUtil.getGLDrawable(R.drawable.gl_appdrawer_slide_menu_clear_bg);
	}

}
