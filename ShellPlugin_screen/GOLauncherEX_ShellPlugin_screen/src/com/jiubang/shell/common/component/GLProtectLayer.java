package com.jiubang.shell.common.component;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnTouchListener;
import com.go.gl.view.GLViewGroup;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.IShell;
import com.jiubang.shell.IView;

/**
 * 拦截点击和触摸的层，一般用于动画过程当中，保护动画显示流程的完整
 * @author wuziyi
 *
 */
public class GLProtectLayer extends GLFrameLayout implements OnTouchListener, IView {
	
	private boolean mIsBlockTouch = true;
	
	private boolean mIsBlockPressKey = true;

	public GLProtectLayer(Context context) {
		super(context);
		setOnTouchListener(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean flag = false;
		if (mIsBlockPressKey) {
			flag = true;
		}
		return flag;
	}
	
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		boolean flag = false;
		if (mIsBlockPressKey) {
			flag = true;
		}
		return flag;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean flag = false;
		if (mIsBlockPressKey) {
			flag = true;
		}
		return flag;
	}

	@Override
	public boolean onTouch(GLView v, MotionEvent event) {
		boolean flag = false;
		if (mIsBlockTouch) {
			flag = true;
		}
		return flag;
	}

	@Override
	public void setVisible(boolean visible, boolean animate, Object obj) {
		setVisible(visible);
	}

	@Override
	public void setShell(IShell shell) {
		
	}

	@Override
	public int getViewId() {
		return IViewId.PROTECTED_LAYER;
	}

	@Override
	public void onAdd(GLViewGroup parent) {
		
	}

	@Override
	public void onRemove() {
		
	}
	
	public void showProtect(boolean blockTouch, boolean blockPressKey) {
		mIsBlockTouch = blockTouch;
		mIsBlockPressKey = blockPressKey;
		setVisibility(VISIBLE);
	}
	
	public void removeProtect() {
		setVisibility(GONE);
	}
	
}
