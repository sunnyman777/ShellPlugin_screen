package com.jiubang.shell.effect;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;

import com.gau.go.gostaticsdk.utiltool.DrawUtils;
import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.graphics.BitmapGLDrawable;
import com.go.gl.graphics.GLCanvas;

/**
 * 用于图标上的特效
 * 
 * @author zouguiquan
 * 
 */
public class IconCircleEffect extends AbstractEffect {

	// -----------------光圈动画相关参数-------------------//
	private static BitmapGLDrawable sCircleBitmap = null;
	private Context mContext;
	// 光圈的中心点位置
	private int mCircleCenterX;
	private int mCircleCenterY;
	// 透明值
	private static final int ALPHA_MAX = 255;
	// 开始光圈大小
	private static final float FROMSCALE = 0.5f;
	// 结束光圈大小
	private static final float TOSCALE = 2.1f;
	// 光圈半径
	public static final int CIRCLE_RADIUS = DrawUtils.dip2px(26);
	// 点击图标光圈时间
	public static final long ANIM_DURATION_CLICK = 120;
	// 拖放图标光圈时间
	public static final long ANIM_DURATION_DRAG = 450;

	public IconCircleEffect(long duration, Context context) {
		super(duration);
		mContext = context;

		if (sCircleBitmap == null) {
			try {
				// 获取光圈图片资源
				BitmapDrawable bitmapDrawable = (BitmapDrawable) mContext
						.getResources().getDrawable(R.drawable.gl_round);
				sCircleBitmap = new BitmapGLDrawable(bitmapDrawable);
			} catch (OutOfMemoryError e) {
				return;
			}
		}
	}

	/**
	 * 在画特效的时候更新参数,通常会在onDraw或dispatchDraw上调用
	 */
	@Override
	public void updateEffect(Object[] drawInfo) {
		mCircleCenterX = (Integer) drawInfo[0];
		mCircleCenterY = (Integer) drawInfo[1];
	}

	@Override
	public void drawEffect(GLCanvas canvas, long drawingTime, Object[] params) {
		super.drawEffect(canvas, drawingTime, params);
		drawCircle(canvas, drawingTime);
	}

	private void drawCircle(GLCanvas canvas, long drawingTime) {
		if (sCircleBitmap == null) {
			return;
		}
		int oldAlpha = canvas.getAlpha();
		float complete = 0f;
		sCircleBitmap.setBounds(mCircleCenterX - CIRCLE_RADIUS, mCircleCenterY
				- CIRCLE_RADIUS, mCircleCenterX + CIRCLE_RADIUS, mCircleCenterY
				+ CIRCLE_RADIUS);

		long elapsedTime = drawingTime - mStartDrawTime - mDelay;

		complete = drawCircle(canvas, elapsedTime, mCircleCenterX,
				mCircleCenterY);
		canvas.setAlpha(oldAlpha);
	}

	private float drawCircle(GLCanvas canvas, long elapsedTime, float pivotX,
			float pivotY) {
		int alpha = 0;
		float scale = 0;

		float t = (float) elapsedTime / mDuration;
		t = Math.max(0, Math.min(t, 1));

		alpha = (int) (ALPHA_MAX * (1 - t));
		scale = InterpolatorFactory.lerp(FROMSCALE, TOSCALE, t);

		// canvas.multiplyAlpha(alpha); //此处回会让第二个圈滑到一半就完全透明
		canvas.setAlpha(alpha);
		drawScale(canvas, scale, pivotX, pivotY);

		return t;
	}

	private void drawScale(GLCanvas canvas, float scale, float pivotX,
			float pivotY) {
		canvas.save();
		canvas.scale(scale, scale, pivotX, pivotY);
		sCircleBitmap.draw(canvas);
		canvas.restore();
	}

	@Override
	protected void effecting(GLCanvas canvas, float interpolatorTime,
			Object[] params) {
		// TODO Auto-generated method stub
		
	}
}
