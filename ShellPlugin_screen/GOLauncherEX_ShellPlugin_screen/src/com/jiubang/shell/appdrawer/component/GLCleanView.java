package com.jiubang.shell.appdrawer.component;

import android.content.Context;

import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLFrameLayout;

/**
 * 
 * @author yangguanxiang
 *
 */
public class GLCleanView extends GLFrameLayout {
	private OnCleanButtonClickListener mOnCleanButtonClickListener;
	private GLDrawable mBgDrawable;
	public GLCleanView(Context context) {
		super(context);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (mBgDrawable != null) {
			mBgDrawable.setBounds(0, 0, mWidth, mHeight);
		}
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		if (mBgDrawable != null) {
			mBgDrawable.draw(canvas);
		}
	}

	@Override
	public void setPressed(boolean pressed) {
		if (pressed) {
			startPressAnimation();
		}
	}

	private void startPressAnimation() {
		clearAnimation();
		ScaleAnimation inAnim = new ScaleAnimation(1.0f, 0.7f, 1.0f, 0.7f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		inAnim.setFillAfter(true);
		inAnim.setDuration(100);
		inAnim.setAnimationListener(new AnimationListenerAdapter() {
			@Override
			public void onAnimationEnd(com.go.gl.animation.Animation animation) {
				ScaleAnimation outAnim = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f,
						Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				outAnim.setFillAfter(true);
				outAnim.setDuration(100);
				outAnim.setAnimationListener(new AnimationListenerAdapter() {
					@Override
					public void onAnimationEnd(com.go.gl.animation.Animation animation) {
						if (mOnCleanButtonClickListener != null) {
							mOnCleanButtonClickListener.onClick();
						}
					}
				});
				startAnimation(outAnim);
			}
		});
		startAnimation(inAnim);
	}

	public void setBg(GLDrawable bg) {
		mBgDrawable = bg;
		if (mBgDrawable != null) {
			mBgDrawable.setBounds(0, 0, mWidth, mHeight);
		}
	}

	public void setOnCleanButtonClickListener(OnCleanButtonClickListener listener) {
		mOnCleanButtonClickListener = listener;
	}

	/**
	 * 
	 * @author yangguanxiang
	 *
	 */
	public static interface OnCleanButtonClickListener {
		public void onClick();
	}
}
