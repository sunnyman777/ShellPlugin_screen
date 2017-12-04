package com.jiubang.shell.common.component;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.widget.GLTextViewWrapper;
import com.jiubang.shell.animation.TweenAnimation;
import com.jiubang.shell.animation.TweenAnimation.TweenAnimationCallback;

/**
 * 最近打开扫把按钮
 * @author wuziyi
 *
 */
public abstract class GLExtentButton extends GLFrameLayout implements TweenAnimationCallback, AnimationListener {
	
	private GLDrawable mClearView;
	private GLDrawable mViewBackground;
	private GLTextViewWrapper mTextView;
	private TweenAnimation mTweenAnimation;
	private boolean mIsExtended;
	private boolean mIsExtending;
	
	private static final int ROTATE_VALUE = 180;
	private static final int FULL_ALPHA = 255;
	
	private static final int ANIMATION_DURATION = 500;
	
	private float mCurAlphaValue = FULL_ALPHA;
	private float mCurRotateValue;
	private float mCurTranslateValue;
	
	public GLExtentButton(Context context) {
		super(context);
	}
	
	public GLExtentButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GLExtentButton(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onFinishInflate() {
		initView();
		super.onFinishInflate();
	}
	
	private void initView() {
//		mClearView = GLImageUtil.getGLDrawable(R.drawable.gl_appdrawer_slide_menu_recent_clear);
		mClearView = getOriginalImage();
//		mViewBackground = GLImageUtil.getGLDrawable(R.drawable.gl_appdrawer_slide_menu_clear_bg);
		mViewBackground = getExtentBg();
		mTextView = (GLTextViewWrapper) findViewById(R.id.appdrawer_slide_menu_clear_text);
		mTextView.setVisible(false);
		mTextView.setText(getTextContent());
		mTweenAnimation = new TweenAnimation();
		mTweenAnimation.setAnimationListener(this);
		mTweenAnimation.setTweenAnimationCallback(this);
		mTweenAnimation.setFillAfter(true);
		mTweenAnimation.setDuration(ANIMATION_DURATION);
	}
	
	public void extendView() {
		mIsExtending = true;
		mIsExtended = true;
		mTextView.setVisible(true);
		mTextView.setHasPixelOverlayed(false);
		startAnimation(mTweenAnimation);
	}
	
	public void resetView() {
		mIsExtending = true;
		mIsExtended = false;
		startAnimation(mTweenAnimation);
	}
	
	protected abstract String getTextContent();
	
	protected abstract GLDrawable getOriginalImage();
	
	protected abstract GLDrawable getTouchDownBg();
	
	protected abstract GLDrawable getExtentBg();
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int l = getWidth() - mClearView.getIntrinsicWidth() - getPaddingRight();
//		int t = (getHeight() + getPaddingTop() + getPaddingBottom() - mClearView.getIntrinsicHeight()) / 2;
		int t = mTextView.getTop();
		Rect bounds = new Rect(l, t, l + mClearView.getIntrinsicWidth(), t + mClearView.getIntrinsicHeight());
		mClearView.setBounds(bounds);
		mViewBackground.setBounds(bounds);
//		mTextView.layout(left, top, right, bottom);
//		mViewBackground.setBounds(bounds);
	}
	
	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		// 扫把的旋转，平移，渐变or消失
		Rect bounds = mClearView.getBounds();
		int centerX = bounds.left + bounds.width() / 2;
		int centerY = bounds.top + bounds.height() / 2;
		int count = canvas.save();
		canvas.rotate(mCurRotateValue, centerX + mCurTranslateValue, centerY);
		canvas.translate(mCurTranslateValue, 0);
		int hideAlpha = (int) (FULL_ALPHA - mCurAlphaValue);
		if (mIsExtended) {
			mClearView.setAlpha(hideAlpha);
		} else {
			mClearView.setAlpha((int) mCurAlphaValue);
		}
		mClearView.draw(canvas);
		canvas.restoreToCount(count);
		if (mIsExtending || mIsExtended) {
			// 背景图的伸长缩短
			Rect backgound = mClearView.getBounds();
			int left = (int) (backgound.left +  mCurTranslateValue);
			mViewBackground.setBounds(left, backgound.top, backgound.right, backgound.bottom);
			mViewBackground.draw(canvas);
		}
		// 文字的渐变显示or消失
		int saveCount = canvas.save();
		if (mIsExtended) {
			mTextView.setAlpha((int) mCurAlphaValue);
		} else {
			mTextView.setAlpha(hideAlpha);
		}
		super.dispatchDraw(canvas);
		canvas.restoreToCount(saveCount);
	}

	@Override
	public void callback(float interpolatedTime) {
		int dst = mTextView.getWidth() - mClearView.getIntrinsicWidth();
		if (mIsExtended) {
			mCurRotateValue =  -ROTATE_VALUE * interpolatedTime;
			mCurTranslateValue = -dst * interpolatedTime;
			float remapTime = InterpolatorFactory.remapTime(0.5f, 1f, interpolatedTime);
			mCurAlphaValue = FULL_ALPHA * remapTime;
		} else {
			mCurRotateValue = ROTATE_VALUE + ROTATE_VALUE * interpolatedTime;
			mCurTranslateValue = -dst + dst * interpolatedTime;
			float remapTime = InterpolatorFactory.remapTime(0f, 0.5f, interpolatedTime);
			mCurAlphaValue = FULL_ALPHA * remapTime;
		}
		
		if (interpolatedTime >= 1.0f) {
		}
	}
	
	public boolean isExtented() {
		return mIsExtended;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				if (mIsExtended) {
					changeDrawable(true);
				}
				break;
			case MotionEvent.ACTION_MOVE :
							
				break;
			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL :
				if (mIsExtended) {
					changeDrawable(false);
				}
				break;
	
			default:
				break;
		}
		return super.onTouchEvent(event);
	}

	private void changeDrawable(boolean isLightEffect) {
		if (isLightEffect) {
			mTextView.setTextColor(0xFF85C443);
//			mViewBackground = GLImageUtil.getGLDrawable(R.drawable.gl_appdrawer_slide_menu_clear_bg_light);
			mViewBackground = getTouchDownBg();
		} else {
			mTextView.setTextColor(0xFFFFFFFF);
//			mViewBackground = GLImageUtil.getGLDrawable(R.drawable.gl_appdrawer_slide_menu_clear_bg);
			mViewBackground = getExtentBg();
		}
		
	}
	
	@Override
	public boolean performClick() {
		boolean ret = super.performClick();
		if (isExtented()) {
			resetView();
		} else {
			extendView();
		}
		return ret;
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		mIsExtending = false;
		if (mIsExtended) {
			
		} else {
			mTextView.setVisible(false);
			mTextView.setHasPixelOverlayed(true);
		}
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onAnimationProcessing(Animation animation, float interpolatedTime) {
		// TODO Auto-generated method stub
		
	}

}
