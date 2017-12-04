package com.jiubang.shell.appdrawer.slidemenu.slot;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.animation.InterpolatorValueAnimation;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.widget.GLImageView;
import com.go.gl.widget.GLTextViewWrapper;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
/**
 * 侧边栏内几个功能场景入口图标
 * @author wuziyi
 *
 */
public class SlideMenuSlotItemIcon extends GLFrameLayout {
	
	private GLImageView mIcon;
	private ShellTextViewWrapper mText;
	private float mScale = 1f;
	private InterpolatorValueAnimation mAnimation;
	private boolean mIsDrawingAnimation;
	private IClickEffectListener mClickEffectListener;
	
	@Override
	public void draw(GLCanvas canvas) {
		if (mAnimation.isFinished() && mIsDrawingAnimation) {
			if (mClickEffectListener != null) {
				mClickEffectListener.onClickEffectEnd(this);
				mIsDrawingAnimation = false;
			}
			//回调执行完毕
		} else if (!mAnimation.isFinished()) {
			mAnimation.animate();
			invalidate();
		}
		mScale = mAnimation.getValue();
		canvas.scale(mScale, mScale, getWidth() / 2, getHeight() / 2);
		super.draw(canvas);
	}
	
	public SlideMenuSlotItemIcon(Context context) {
		super(context);
	}
	
	public SlideMenuSlotItemIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onFinishInflate() {
		mIcon = (GLImageView) findViewById(R.id.appdrawer_slide_menu_slot_icon);
		mText = (ShellTextViewWrapper) findViewById(R.id.appdrawer_slide_menu_slot_text);
		mAnimation = new InterpolatorValueAnimation(1f);
		mAnimation.setFillAfter(true);
	}

	public GLImageView getIcon() {
		return mIcon;
	}

	public void setIconDrawable(Drawable icon) {
		mIcon.setImageDrawable(icon);
	}

	public GLTextViewWrapper getText() {
		return mText;
	}

	public void setText(String string) {
		mText.setText(string);
	}
	
	@Override
	public void setPressed(boolean pressed) {
		if (pressed) {
			effectScale(true);
		} else {
			effectScale(false);
		}
		super.setPressed(pressed);
	}
	
	private void effectScale(boolean isPress) {
//		Animation animation = null;
		if (isPress) {
//			animation = new ScaleAnimation(1.0f, 0.7f, 1.0f, 0.7f,
//					Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//			animation = new TranslateAnimation(0, 0, 0, 0);
//			animation.setFillAfter(false);
//			mAnimation.setInterpolation(InterpolatorFactory.getInterpolator(InterpolatorFactory.ELASTIC, InterpolatorFactory.EASE_OUT,
//					new float[] { 0.5f, 0.5f }));
			mAnimation.setInterpolation(InterpolatorFactory.getInterpolator(InterpolatorFactory.QUADRATIC));
			mAnimation.start(0.9f, 50);
		} else {
//			animation = new ScaleAnimation(0.7f, 1f, 0.7f, 1f,
//					Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//			animation = new TranslateAnimation(0, 0, 0, 0);
//			animation.setFillAfter(false);
			mAnimation.setInterpolation(InterpolatorFactory.getInterpolator(InterpolatorFactory.QUADRATIC));
			mAnimation.start(1f, 50);
		}
		mIsDrawingAnimation = true;
		if (mClickEffectListener != null) {
			mClickEffectListener.onClickEffectStart(this);
		}
		invalidate();
//		animation.setDuration(2000);
//		startAnimation(animation);
	}
	
	public boolean isDrawingAnimation() {
		return mIsDrawingAnimation;
	}
	
	public void setClickEffectListener(IClickEffectListener clickEffectListener) {
		mClickEffectListener = clickEffectListener;
	}

	@Override
	public void cleanup() {
		mClickEffectListener = null;
		super.cleanup();
	}
	
	/**
	 * 动画回调接口
	 */
	public interface IClickEffectListener {
		public void onClickEffectStart(SlideMenuSlotItemIcon icon);
		public void onClickEffectEnd(SlideMenuSlotItemIcon icon);
	}

}
