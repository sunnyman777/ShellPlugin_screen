package com.jiubang.shell.animation;

import java.util.List;

import android.util.Log;

import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationSet;
import com.jiubang.shell.drag.DragView;

/**
 * 复位动画
 * @author yangguanxiang
 *
 */
public class DropAnimation extends AnimationSet {

	public static final int DURATION_100 = 100;
	public static final int DURATION_210 = 210;
	public static final int DURATION_250 = 250;
	public static final int DURATION_300 = 300;
	public static final int DURATION_320 = 320;

	public DropAnimation(boolean shareInterpolator, float fromXDelta, float toXDelta,
			float fromYDelta, float toYDelta, float fromZDelta, float toZDelta, float curScale,
			float finalScale, DragView dragView) {
		super(shareInterpolator);

		TranslateValue3DAnimation backAnim = new TranslateValue3DAnimation(fromXDelta, toXDelta,
				fromYDelta, toYDelta, fromZDelta, toZDelta);
		this.addAnimation(backAnim);
	}

	/**
	 * 
	 * @author yangguanxiang
	 *
	 */
	public static class DropAnimationInfo {
		public final static int LOCATION_LEFT_TOP = 1;
		public final static int LOCATION_CENTER = 2;
		public final static int LOCATION_BOTTOM = 3;
		public final static int LOCATION_ICON_CENTER = 4;

		private int mDuration = -1;
		private int mDelay;
		// X Y Z三维
		private float[] mLocationPoint;
		private float mFinalScale;
		// set
		private int mLocationType = LOCATION_LEFT_TOP;

		//插值器动画类型，默认线性
		private int mInterpolatorType = -1;
		private int mEase = -1;
		private float[] mArgs;
		private AnimationListener mListener;
		private List<Animation> mExAnimationList;
		
		//默认下显示水波纹
		private boolean mNeedToCircle = true;

		public DropAnimationInfo() {
			mLocationPoint = new float[3]; // CHECKSTYLE IGNORE
			mLocationPoint[0] = -1;
			mLocationPoint[1] = -1;
			mLocationPoint[2] = 0; // CHECKSTYLE IGNORE
			mFinalScale = 1.0f;
		}

		public void setLocationPoint(float x, float y, float z, float scale) {
			mLocationPoint[0] = x;
			mLocationPoint[1] = y;
			mLocationPoint[2] = z;
			setFinalScale(scale);
			Log.d("depth", "setLocation 4:x=" + x + " y=" + y + " z=" + z);
		}

		public void setLocationPoint(float x, float y, float z) {
			mLocationPoint[0] = x;
			mLocationPoint[1] = y;
			mLocationPoint[2] = z;
			Log.d("depth", "setLocation 3:x=" + x + " y=" + y + " z=" + z);
		}

		public void setLocationPoint(float x, float y) {
			mLocationPoint[0] = x;
			mLocationPoint[1] = y;
			Log.d("depth", "setLocation x=" + x + " y=" + y);
		}

		public void setLocationType(int locType) {
			mLocationType = locType;
		}

		public int getLocationType() {
			return mLocationType;
		}

		public void setDuration(int duration) {
			mDuration = duration;
		}

		public void setDelay(int delay) {
			mDelay = delay;
		}

		public float getFinalScale() {
			return mFinalScale;
		}

		public void setFinalScale(float mFinalScale) {
			this.mFinalScale = mFinalScale;
		}

		public void setInterpolatorType(int mInterpolatorType) {
			this.mInterpolatorType = mInterpolatorType;
		}

		public void setEase(int ease) {
			mEase = ease;
		}
		
		public void setInterpolatorArgs(float[] args) {
			mArgs = args;
		}
		
		public float[] getInterpolatorArgs() {
			return mArgs;
		}

		public float[] getLocationPoint() {
			return mLocationPoint;
		}

		public int getDuration() {
			return mDuration;
		}

		public int getDelay() {
			return mDelay;
		}

		public int getInterpolatorType() {
			return mInterpolatorType;
		}

		public int getEase() {
			return mEase;
		}

		public void setAnimationListener(AnimationListener listener) {
			mListener = listener;
		}

		public AnimationListener getAnimationListener() {
			return mListener;
		}
		
		public List<Animation> getExAnimationList() {
			return mExAnimationList;
		}

		public void setExAnimationList(List<Animation> exAnimationList) {
			mExAnimationList = exAnimationList;
		}
		
		public void setNeedToShowCircle(boolean b) {
			mNeedToCircle = b;
		}
		
		public boolean isNeedToShowCircle() {
			return mNeedToCircle;
		}
		
		// TODO:LH
		/*
		 * public static class DragViewHolder{ private DragLayer.LayoutParams
		 * mLayoutParams; private float mScale;
		 * 
		 * public DragViewHolder(DragLayer.LayoutParams layoutParams, float scale){
		 * mLayoutParams = layoutParams; mScale = scale; }
		 * 
		 * public void setX(int x){ mLayoutParams.x = x; }
		 * 
		 * public void setY(int y){ mLayoutParams.y = y; }
		 * 
		 * public void setScale(float scale){ mScale = scale; }
		 * 
		 * public int getX(){ return mLayoutParams.x; }
		 * 
		 * public int getY(){ return mLayoutParams.y; }
		 * 
		 * public float getScale(){ return mScale; } }
		 */
	}

}
