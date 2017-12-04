package com.jiubang.shell.effect;

import com.go.gl.graphics.GLCanvas;

/**
 * 
 * @author zouguiquan
 *
 */
public class ScaleValueEffect extends AbstractEffect {
	
	private float mToScale;
	private float mFromScale;
	private float mPivotX;
	private float mPivotY;

	public ScaleValueEffect(float toScale, float fromScale, float pivotX, float pivotY, long duration) {
		super(duration);
		mToScale = toScale;
		mFromScale = fromScale;
		mPivotX = pivotX;
		mPivotY = pivotY;
		// 其实我不太懂为啥又要写一个这样阉割的缩放效果，也许是要effectcontroler来控制队列效果
	}

	@Override
	public void updateEffect(Object[] drawInfo) {
//		mToScale = (Float) drawInfo[0];
//		mFromScale = (Float) drawInfo[1];
	}

	@Override
	protected void effecting(GLCanvas canvas, float interpolatorTime, Object[] params) {
		float scale = 0;
		scale = mFromScale + (mToScale - mFromScale) * interpolatorTime;
		canvas.scale(scale, scale, mPivotX, mPivotY);
	}
}
