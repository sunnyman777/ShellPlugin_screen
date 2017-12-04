package com.jiubang.shell.screen.back;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLViewWrapper;

/**
 * 用于普通中间层的view与3D引擎的映射
 * 
 * @author jiangxuwen
 * 
 */
public class GLMiddleView extends GLViewWrapper {

	public GLMiddleView(Context context, View view) {
		super(context);
		setView(view, null);
		// setPersistentDrawingCache(false); //让绘图缓冲一直常驻
	}

	public GLMiddleView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GLMiddleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void cleanup() {
		if (getView() != null) {
			getView().setTag(R.id.tag_glwidgetview, null);
		}
		super.cleanup();
	}

	@Override
	public void setOnClickListener(final OnClickListener l) {
		if (getView() != null) {
			getView().setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					l.onClick(GLMiddleView.this);
				}
			});
		} else {
			super.setOnClickListener(l);
		}
	}

	@Override
	public void setOnLongClickListener(final OnLongClickListener l) {
		if (getView() != null) {
			getView().setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					return l.onLongClick(GLMiddleView.this);
				}

			});
		} else {
			super.setOnLongClickListener(l);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (getView() != null) {
			return getView().onTouchEvent(event);
		} else {
			return super.onTouchEvent(event);
		}
	}
}
