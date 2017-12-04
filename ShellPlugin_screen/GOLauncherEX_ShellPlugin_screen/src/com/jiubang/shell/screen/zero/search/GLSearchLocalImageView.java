package com.jiubang.shell.screen.zero.search;
import android.content.Context;
import android.util.AttributeSet;

import com.go.gl.view.GLLinearLayout;
import com.go.gl.widget.GLImageView;
/**
 * 拦截上层焦点view
 * @author liulixia
 *
 */
public class GLSearchLocalImageView extends GLImageView {

	public GLSearchLocalImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public GLSearchLocalImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void setPressed(boolean pressed) {
		// TODO Auto-generated method stub
		 if (pressed && ((GLLinearLayout) getGLParent()).isPressed()) {
             return;
         }
        super.setPressed(pressed);
	}
}
