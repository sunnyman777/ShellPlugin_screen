package com.jiubang.shell.widget.component;

import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;

import com.go.gl.animation.Transformation3D;
import com.go.gl.graphics.BitmapGLDrawable;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.view.GLView.OnLongClickListener;
import com.jiubang.shell.animation.ValueAnimation;
import com.jiubang.shell.common.component.TransformationInfo;
import com.jiubang.shell.common.listener.TransformListener;
import com.jiubang.shell.utils.ViewUtils;

/**
 * 
 * @author dengdazhong
 *
 */
public class GLWidgetContainer extends GLFrameLayout
		implements
			OnClickListener,
			OnLongClickListener,
			TransformListener {
	private final static long BACK_DURATION = 300;
	private final static int STATE_NORMAL = 0;
	private final static int STATE_BACK_ANIMATE = 1;
	private static final int ALPHA_FULL = 255;
	private int mState = STATE_NORMAL;
	private GLView mWidget;
	private OnClickListener mOnClickListener;
	private OnLongClickListener mOnLongClickListener;
	private TransformationInfo mTFInfo;
	private ValueAnimation mValueAnimation;
	private Averages mAverages;
	private int mChangeAlpha = ALPHA_FULL;
	private boolean mAutoFit;
	
	public GLWidgetContainer(Context context, GLView widget) {
		super(context);
		setHasPixelOverlayed(false);
		if (widget != null) {
			mWidget = widget;
			//			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			//			lp.gravity = Gravity.CENTER;
			//			addView(mWidget, lp);
			addView(mWidget);
			//			setDrawingCacheDepthBuffer(true);
			//			mWidget.setOnClickListener(this);
			mWidget.setOnLongClickListener(this);
		}
		//				setBackgroundColor(Color.BLUE);
	}

	//	@Override
	//	public boolean dispatchTouchEvent(MotionEvent ev) {
	//		if (mWidget != null) {
	//			return mWidget.dispatchTouchEvent(ev);
	//		} else {
	//			return super.dispatchTouchEvent(ev);
	//		}
	//	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (mWidget != null) {
			mWidget.layout(0, 0, mWidth, mHeight);
			ViewUtils.autoFitDrawingCacheScale(mWidget);
		}
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		mOnClickListener = l;
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		mOnLongClickListener = l;
	}

	@Override
	public boolean onLongClick(GLView v) {
		if (mOnLongClickListener != null) {
			return mOnLongClickListener.onLongClick(this);
		}
		return false;
	}

	@Override
	public void onClick(GLView v) {
		if (mOnClickListener != null) {
			mOnClickListener.onClick(this);
		}
	}

	@Override
	public void setScaleXY(float scaleX, float scaleY) {
		if (mTFInfo != null) {
			mTFInfo.mScaleX = scaleX;
			mTFInfo.mScaleY = scaleY;
		}
	}

	@Override
	public void setPivotXY(float pivotX, float pivotY) {
		if (mTFInfo != null) {
			mTFInfo.mPivotX = pivotX;
			mTFInfo.mPivotY = pivotY;
		}
	}

	@Override
	public void setTranslateXY(float transX, float transY) {
		if (mTFInfo != null) {
			mTFInfo.mTranslationX = transX;
			mTFInfo.mTranslationY = transY;
		}
	}

	@Override
	public void setTransformationInfo(TransformationInfo info) {
		mTFInfo = info;
	}

	@Override
	public TransformationInfo getTransformationInfo() {
		return mTFInfo;
	}

	@Override
	public void setAutoFit(boolean autoFit) {
		mAutoFit = autoFit;
	}
	
	@Override
	protected void dispatchDraw(GLCanvas canvas) {
//		if (mAutoFit) {
//			canvas.clipRect(0, 0, mWidth, mHeight);
//		}
		if (mState == STATE_NORMAL) {
			final int oldAlpha = canvas.getAlpha();
			
			if (mChangeAlpha != ALPHA_FULL) {
				canvas.multiplyAlpha(mChangeAlpha);
			}
			if (mTFInfo != null) {
				int saveCount = canvas.save();
				canvas.scale(mTFInfo.mScaleX, mTFInfo.mScaleY, mTFInfo.mPivotX, mTFInfo.mPivotY);
				canvas.translate(mTFInfo.mTranslationX, mTFInfo.mTranslationY);
				super.dispatchDraw(canvas);
				canvas.restoreToCount(saveCount);
			} else {
				super.dispatchDraw(canvas);
			}
			canvas.setAlpha(oldAlpha);
		} else if (mState == STATE_BACK_ANIMATE) {
			if (mAverages != null && mValueAnimation != null && mTFInfo != null) {
				if (mValueAnimation.animate()) {
					//    				final int saveCount = canvas.save();
					final float value = mValueAnimation.getValue();
					final float scaleX = mAverages.mStartSx + mAverages.mAScaleX * value;
					final float scaleY = mAverages.mStartSy + mAverages.mAScaleY * value;
					final float translateX = mAverages.mStartTx + mAverages.mATransX * value;
					final float translateY = mAverages.mStartTy + mAverages.mATransY * value;
					setScaleXY(scaleX, scaleY);
					setTranslateXY(translateX, translateY);
					//    				canvas.scale(scaleX, scaleY, mTFInfo.mPivotX, mTFInfo.mPivotY);
					//    				canvas.translate(translateX, translateY);
					super.dispatchDraw(canvas);
					//    	    		canvas.restoreToCount(saveCount);
					invalidate();
				} else {
					mState = STATE_NORMAL;
					mAverages = null;
					mValueAnimation = null;
					mTFInfo = null;
					super.dispatchDraw(canvas);
				}
			}
		}
	}

	@Override
	public void setAlpha(int alpha) {
		mChangeAlpha = alpha;
	}

	public GLView getWidget() {
		return mWidget;
	}

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		if (mWidget != null) {
			mWidget.cancelLongPress();
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mWidget instanceof GLWidgetView) {
			((GLWidgetView) mWidget).adjustInternalViewWrapperPosition();
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public void setDrawingCacheEnabled(boolean enabled) {
		if (mWidget != null) {
			mWidget.setDrawingCacheEnabled(enabled);
		} else {
			super.setDrawingCacheEnabled(enabled);
		}
	}

	@Override
	public void setDrawingCacheAnchor(Point p) {
		if (mWidget != null) {
			mWidget.setDrawingCacheAnchor(p);
		} else {
			super.setDrawingCacheAnchor(p);
		}
	}

	@Override
	public void setDrawingCacheTransform(Transformation3D t) {
		if (mWidget != null) {
			mWidget.setDrawingCacheTransform(t);
		} else {
			super.setDrawingCacheTransform(t);
		}
	}

	@Override
	public BitmapGLDrawable getDrawingCache(GLCanvas canvas) {
		if (mWidget != null) {
			return mWidget.getDrawingCache(canvas);
		}
		return super.getDrawingCache(canvas);
	}

	@Override
	public void buildDrawingCache(GLCanvas canvas) {
		if (mWidget != null) {
			mWidget.buildDrawingCache(canvas);
		} else {
			super.buildDrawingCache(canvas);
		}
	}

	@Override
	public boolean isDrawingCacheEnabled() {
		if (mWidget != null) {
			return mWidget.isDrawingCacheEnabled();
		}
		return super.isDrawingCacheEnabled();
	}
	
	@Override
	public void animateToSolution() {
		if (mTFInfo != null) {
			if (mValueAnimation == null) {
				mValueAnimation = new ValueAnimation(0);
			}
			mAverages = new Averages();
			mAverages.mStartSx = mTFInfo.mScaleX;
			mAverages.mStartSy = mTFInfo.mScaleY;
			mAverages.mStartTx = mTFInfo.mTranslationX;
			mAverages.mStartTy = mTFInfo.mTranslationY;
			mAverages.mATransX = -mTFInfo.mTranslationX;
			mAverages.mATransY = -mTFInfo.mTranslationY;
			mAverages.mAScaleX = 1.0f - mTFInfo.mScaleX;
			mAverages.mAScaleY = 1.0f - mTFInfo.mScaleY;

			mValueAnimation.start(1.0f, BACK_DURATION);
			mValueAnimation.animate();
			mState = STATE_BACK_ANIMATE;
			invalidate();
		}
	}
	// end animateToSolution
	
	/**
	 * 平均变化值
	 * @author jiangxuwen
	 *
	 */
	class Averages {

		float mStartTx;
		float mStartTy;

		float mATransX;
		float mATransY;

		float mStartSx;
		float mStartSy;

		float mAScaleX;
		float mAScaleY;
	}
	
	@Override
	public void cleanup() {
		super.cleanup();
		mOnClickListener = null;
		mOnLongClickListener = null;
		mWidget = null;
		mTFInfo = null;
		mAverages = null;
		mValueAnimation = null;
	}
//	@Override
//	public void setDrawingCacheEnabled(boolean enabled) {
//		if(mWidget != null){
//			mWidget.setDrawingCacheEnabled(enabled);
//		}
////		super.setDrawingCacheEnabled(enabled);
//	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		ViewUtils.autoFitDrawingCacheScale(this);
	}
}
