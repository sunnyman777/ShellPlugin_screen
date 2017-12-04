package com.jiubang.shell.model;

import android.content.Context;
import android.util.AttributeSet;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLView;

/**
 * 
 * <br>类描述:模型view基类
 * <br>功能详细描述:
 * 
 * @author  guoweijie
 * @date  [2013-3-22]
 */
public abstract class AbsMs3dView extends GLView {
	
	public float mModelScale = 1f;
	
	protected ModelItem mModelItem;

	public AbsMs3dView(Context context) {
		this(context, null);
	}

	public AbsMs3dView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AbsMs3dView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initModel();
	}

	protected abstract void initModel();

	public float getModelWidth() {
		float xLen = mModelItem.getXLen();
		return xLen * mModelScale;
	}

	public float getModelHeight() {
		float yLen = mModelItem.getYLen();
		return yLen * mModelScale;
	}
	
	public float getModelDepth() {
		float zLen = mModelItem.getZLen();
		return zLen * mModelScale;
	}

	public float getDefaultModelWidth() {
		return mModelItem.getXLen();
	}

	@Override
	protected void onDraw(GLCanvas canvas) {
		super.onDraw(canvas);

		onRender(canvas);
	}

	protected void onRender(GLCanvas canvas) {
		if (mModelItem == null) {
			return;
		}
		canvas.translate(getWidth() * 0.5f, getHeight() * -0.5f, 0);
		canvas.scale(mModelScale, mModelScale, mModelScale);
		mModelItem.render(canvas);
	}

	@Override
	public void cleanup() {
		if (mModelItem != null) {
			mModelItem.cleanup();
		}
		super.cleanup();
	}
}
