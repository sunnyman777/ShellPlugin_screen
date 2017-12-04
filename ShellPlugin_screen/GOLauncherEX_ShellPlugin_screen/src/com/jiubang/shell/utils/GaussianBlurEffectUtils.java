package com.jiubang.shell.utils;

import java.util.ArrayList;

import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.Translate3DAnimation;
import com.go.gl.graphics.ext.GaussianBlurProcessor;
import com.go.gl.view.GLView;
import com.go.util.device.CpuManager;
import com.jiubang.shell.folder.GLAppFolderMainView;

/**
 * 
 * @author yangguanxiang
 *
 */
public class GaussianBlurEffectUtils {
	public static float sBlurRadius = 5.0f;
	public static int sBlurTotalSteps = 2;
	public static int sBlurStepsPerFrame = 1;
	public static float sPrecision = 2.5f;
	private static ArrayList<GLView> sTargetList = new ArrayList<GLView>();
	static {
		autoFitBlurConfig();
	}

	public static boolean isEnableBlur() {
		return CpuManager.getCpuCoreNums() >= 1;
	}

	private static void autoFitBlurConfig() {
		if (CpuManager.getCpuCoreNums() >= 8) {
			sBlurRadius = 4.0f;
			sBlurTotalSteps = 32;
			sPrecision = GaussianBlurProcessor.PRECISION_HIGHT;
		}
	}

	private final static float ALPHA_FULL = 1.0f;
	private final static float ALPHA_GONE_BLUR = 0.7f;
	private final static float ALPHA_GONE = 0.0f;
	private final static float DEPTH = -250.0f;

	public static void enableBlurWithZoomOutAnimation(final GLView target,
			final AnimationListener listener) {
		if (target == null) {
			return;
		}
		sTargetList.add(target);
		target.setHasPixelOverlayed(false);
		if (GaussianBlurEffectUtils.isEnableBlur()) {
			target.enableBlur(GaussianBlurEffectUtils.sBlurRadius,
					GaussianBlurEffectUtils.sBlurTotalSteps,
					GaussianBlurEffectUtils.sBlurStepsPerFrame, GaussianBlurEffectUtils.sPrecision);
			target.setBlurAlphaProportion(0.0f);
			AlphaAnimation alphaAnim = new AlphaAnimation(ALPHA_FULL, ALPHA_GONE_BLUR);
			Translate3DAnimation transAnim = new Translate3DAnimation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
					DEPTH);
			AnimationSet animSet = new AnimationSet(true);
			animSet.addAnimation(alphaAnim);
			animSet.addAnimation(transAnim);
			animSet.setFillAfter(true);
			animSet.setDuration(GLAppFolderMainView.sFolderAnimationDuration);
			animSet.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationProcessing(Animation animation, float interpolatedTime) {
					target.setBlurAlphaProportion(interpolatedTime);
					if (listener != null) {
						listener.onAnimationProcessing(animation, interpolatedTime);
					}
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					target.setBlurAlphaProportion(1.0f);
					if (listener != null) {
						listener.onAnimationEnd(animation);
					}
				}

				@Override
				public void onAnimationStart(Animation animation) {
					if (listener != null) {
						listener.onAnimationStart(animation);
					}
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					if (listener != null) {
						listener.onAnimationRepeat(animation);
					}
				}
			});
			target.startAnimation(animSet);
		} else {
			AlphaAnimation alphaAnim = new AlphaAnimation(ALPHA_FULL, ALPHA_GONE);
			Translate3DAnimation transAnim = new Translate3DAnimation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
					DEPTH);
			AnimationSet animSet = new AnimationSet(true);
			animSet.addAnimation(alphaAnim);
			animSet.addAnimation(transAnim);
			animSet.setFillAfter(true);
			animSet.setDuration(GLAppFolderMainView.sFolderAnimationDuration);
			animSet.setAnimationListener(listener);
			target.startAnimation(animSet);
		}
	}

	public static void disableBlurWithZoomInAnimation(final GLView target,
			final AnimationListener listener) {
		if (target == null) {
			return;
		}
		sTargetList.remove(target);
		if (GaussianBlurEffectUtils.isEnableBlur()) {
			AlphaAnimation alphaAnim = new AlphaAnimation(ALPHA_GONE_BLUR, ALPHA_FULL);
			Translate3DAnimation transAnim = new Translate3DAnimation(0.0f, 0.0f, 0.0f, 0.0f,
					DEPTH, 0.0f);
			AnimationSet animSet = new AnimationSet(true);
			animSet.addAnimation(alphaAnim);
			animSet.addAnimation(transAnim);
			animSet.setFillAfter(true);
			animSet.setDuration(GLAppFolderMainView.sFolderAnimationDuration);
			animSet.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationProcessing(Animation animation, float interpolatedTime) {
					target.setBlurAlphaProportion(1.0f - interpolatedTime);
					if (listener != null) {
						listener.onAnimationProcessing(animation, interpolatedTime);
					}
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					target.setBlurAlphaProportion(0.0f);
					target.disableBlur();
					if (listener != null) {
						listener.onAnimationEnd(animation);
					}
				}

				@Override
				public void onAnimationStart(Animation animation) {
					if (listener != null) {
						listener.onAnimationStart(animation);
					}
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					if (listener != null) {
						listener.onAnimationRepeat(animation);
					}
				}
			});
			target.startAnimation(animSet);
		} else {
			AlphaAnimation alphaAnim = new AlphaAnimation(ALPHA_GONE, ALPHA_FULL);
			Translate3DAnimation transAnim = new Translate3DAnimation(0.0f, 0.0f, 0.0f, 0.0f,
					DEPTH, 0.0f);
			AnimationSet animSet = new AnimationSet(true);
			animSet.addAnimation(alphaAnim);
			animSet.addAnimation(transAnim);
			animSet.setFillAfter(true);
			animSet.setDuration(GLAppFolderMainView.sFolderAnimationDuration);
			animSet.setAnimationListener(listener);
			target.startAnimation(animSet);
		}
	}

	public static void disableBlurWithoutAnimation(final GLView target) {
		if (target == null) {
			return;
		}
		target.clearAnimation();
		if (GaussianBlurEffectUtils.isEnableBlur()) {
			target.setBlurAlphaProportion(0.0f);
			target.disableBlur();
		}
		sTargetList.remove(target);
	}

	public static void invalidateBlurEffect() {
		if (GaussianBlurEffectUtils.isEnableBlur()) {
			for (GLView target : sTargetList) {
				if (target != null) {
					target.disableBlur();
					target.enableBlur(GaussianBlurEffectUtils.sBlurRadius,
							GaussianBlurEffectUtils.sBlurTotalSteps,
							GaussianBlurEffectUtils.sBlurTotalSteps,
							GaussianBlurEffectUtils.sPrecision);
				}
			}
		}
	}
}
