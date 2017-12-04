package com.jiubang.shell.animation;

import android.content.Context;
import android.util.AttributeSet;

import com.go.gl.animation.Transformation3D;
import com.go.gl.animation.Translate3DAnimation;

/**
 * 
 * 附带动作变化监听者的3D动画
 * 可针对透明度和缩放比例的变化
 * @author jiangxuwen
 *
 */
public class TranslateValue3DAnimation extends Translate3DAnimation {

	private TranslateValue3DAnimationListener mListener;
	private float mFromAlpha;
	private float mToALpha;
	
	private float mFromScale;
	private float mToScale;
	
	public TranslateValue3DAnimation(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public TranslateValue3DAnimation(float fromXDelta, float toXDelta, float fromYDelta,
			float toYDelta, float fromZDelta, float toZDelta) {
		super(fromXDelta, toXDelta, fromYDelta, toYDelta, fromZDelta, toZDelta);
	}

	public TranslateValue3DAnimation(int fromXType, float fromXValue, int toXType, float toXValue,
			int fromYType, float fromYValue, int toYType, float toYValue, int fromZType,
			float fromZValue, int toZType, float toZValue) {
		super(fromXType, fromXValue, toXType, toXValue, fromYType, fromYValue, toYType, toYValue,
				fromZType, fromZValue, toZType, toZValue);
	}
	
	@Override
	protected void applyTransformation(float interpolatedTime, Transformation3D t) {
		super.applyTransformation(interpolatedTime, t);
		if (mListener != null) {
			final float[] matrix = t.getMatrix();
			// matrix[12], matrix[13], matrix[14] 分别对应 x, y, z
			mListener.onValue(interpolatedTime, matrix[12], matrix[13], matrix[14]);
			
	        if (mFromAlpha != mToALpha) {
	        	float dAlpha = mFromAlpha;
	        	dAlpha = mFromAlpha + ((mToALpha - mFromAlpha) * interpolatedTime);
	        	mListener.onAlpha(dAlpha);
	        }
	        if (mFromScale != mToScale) {
	        	float dScale = mFromScale;
	        	dScale = mFromScale + ((mToScale - mFromScale) * interpolatedTime);
	        	mListener.onScale(dScale);
	        }
		}
	}
	
	public void setListenerAlphaChange(float fromAlpha, float toAlpha) {
		mFromAlpha = fromAlpha;
		mToALpha = toAlpha;
	}
	
	public void setListenerScaleChange(float fromScale, float toScale) {
		mFromScale = fromScale;
		mToScale = toScale;
	}
	
	public void setTranslateValue3DAnimationListener(TranslateValue3DAnimationListener listener) {
		mListener = listener;
	}
	
	/**
	 * TranslateValue3DAnimation 变化的监听者
	 * @author jiangxuwen
	 *
	 */
	public interface TranslateValue3DAnimationListener {
		void onValue(float interpolatedTime, float x, float y, float z);
		void onAlpha(float alpha);
		void onScale(float scale);
	}
}
