package com.jiubang.shell.model;

import com.go.gl.graphics.GLCanvas;

/**
 * @author chendongcheng
 */
public class ParticleModel {
	private float mStartX;
	private float mStartY;
	private float mStartZ;

	private float mVy;
	private float mDisY;
	private float mStopY;

	private float mTimeSpan;

	private float mTranX;
	private float mTranY;
	private float mTranZ;
	
	private ModelItem mModelItem;

	public void addTimeSpan(float speed) {
		mTimeSpan += speed;
	}
	
	public ParticleModel(ModelItem item) {
		mModelItem = item;
	}

	public void initStartPosition(float startX, float startY, float startZ, float vy, float stopY, float disY) {
		mStartX = startX;
		mStartY = startY;
		mStartZ = startZ;

		mVy = vy;
		mStopY = stopY;
		mDisY = disY;
		mTimeSpan = 0;
	}
	
	public void change() {
		float disy = mVy * mTimeSpan;
		mTranY = mStartY + disy;
		float angle = (float) (disy / mDisY * 4 * Math.PI);

		float offset = (float) (1 * Math.sin(angle));
		mTranX = mStartX + offset;
		mTranZ = mStartZ + offset;
		if (mTranY > mStopY) {
			if (mOnDisppearListener != null) {
				mOnDisppearListener.onDisppear(this);
			}
		}
	}
	
	public void draw(GLCanvas canvas) {
		canvas.save();
//		float disy = mVy * mTimeSpan;
//		mTranY = mStartY + disy;
//		float angle = (float) (disy / mStopY * 4 * Math.PI);
//
//		float offset = (float) (2 * Math.sin(angle));
//		mTranX = mStartX + offset;
//		mTranZ = mStartZ + offset;
//		if (mTranY > mStopY) {
//			//mTimeSpan = 0;
//			if (mOnDisppearListener != null) {
//				mOnDisppearListener.onDisppear(this);
//			}
//		}

		canvas.translate(mTranX, mTranY, mTranZ);
		mModelItem.render(canvas);
		canvas.restore();
	}
	
	private OnDisppearListener mOnDisppearListener;

	public void setOnDisppearListener(OnDisppearListener listener) {
		mOnDisppearListener = listener;
	}

	/**
	 * @author chendongcheng
	 */
	public interface OnDisppearListener {
		public void onDisppear(ParticleModel particle);
	}
}
