package com.jiubang.shell.appdrawer.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.view.animation.DecelerateInterpolator;

import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.view.GLView;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.SettingProxy;
import com.go.util.animation.ExponentialInterpolator;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.data.info.AppSettingDefault;
import com.jiubang.shell.animation.DepthAnimation;
import com.jiubang.shell.animation.Rotate3DAnimation;
import com.jiubang.shell.animation.WindmillAnimation;
import com.jiubang.shell.appdrawer.GLAppDrawer;
import com.jiubang.shell.appdrawer.component.GLGridViewContainer;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 功能表进出动画工厂
 * @author yangguanxiang
 *
 */
public class AnimationFactory {
	public static final int EFFECT_DEFAULT = 1;
	public static final int EFFECT_WINDWILL = 2;
	public static final int EFFECT_DEPTH = 4;
	public static final int EFFECT_ROTATE = 5;
	public static final int EFFECT_TV = 3;
	public static final int EFFECT_NONE = 6;

	public static final long DEFAULT_DURATION = 450;
	// private final static String TYPE = "anim";
	private static Random sRrandom = new Random();
	private final static int MAX_RANDOM = 5;

	public static final int HIDE_ANIMATION = 7; // 隐藏时带动画
	public static final int SHOW_ANIMATION = 8; // 显示时带动画
	public static final int FADE_IN_ANIMATION = 9; // 淡入动画
	public static final int FADE_OUT_ANIMATION = 10; // 淡出动画
	/**
	 * 
	 * @param effectId
	 *            动画的XML或者动画的ID
	 * @param context
	 * @param packageName
	 *            包路径
	 * @return 动画类
	 */
	public static boolean startEnterAnimation(int effectId, Context context,
			GLAppDrawer appDrawer, AnimationListener animationListener) {
		boolean ret = false;
		Object animation = null;
		int nextInt = -1;
		switch (effectId) {
			case 0 :
				nextInt = sRrandom.nextInt(MAX_RANDOM) + 1;
				animation = getEnterAnimation(nextInt, context);
				break;
			case -1 :
				FunAppSetting funAppSetting = SettingProxy.getFunAppSetting();
				if (funAppSetting != null) {
					int[] effects = funAppSetting.getAppInOutCustomRandomEffect();
					if (effects != null) {
						nextInt = sRrandom.nextInt(effects.length);
						animation = getEnterAnimation(effects[nextInt], context);
					}
				}
				break;
			default :
				animation = getEnterAnimation(effectId, context);
				break;
		}
		if (animation instanceof Animation) {
			Animation enterAnimation = (Animation) animation;
			enterAnimation.setDuration(DEFAULT_DURATION);
			AnimationTask task = new AnimationTask(appDrawer, enterAnimation,
					animationListener, true, AnimationTask.PARALLEL);
			GLAnimationManager.startAnimation(task);
			ret = true;
		} else if (animation instanceof FlyAnimation) {
			FlyAnimation flyAnimation = (FlyAnimation) animation;
			flyAnimation.setDuration(DEFAULT_DURATION);
			Animation alphaAnimation = createAlphaAnimation(true, DEFAULT_DURATION);
			alphaAnimation.setAnimationListener(animationListener);
			appDrawer.startAnimation(alphaAnimation);
			GLGridViewContainer gridViewContainer = appDrawer.getGridViewContainer();
			List<GLView> glViews = gridViewContainer.getCurScreenIcons();
			if (glViews != null && !glViews.isEmpty()) {
				flyAnimation.startFlyAnimation(appDrawer.getWidth(), appDrawer.getHeight(), false,
						(ArrayList<GLView>) glViews, null, gridViewContainer.getCurGridRow(),
						gridViewContainer.getCurGridCol());
			}
			ret = true;
		}
		return ret;
	}

	/**
	 * 
	 * @param effectId
	 *            动画的XML或者动画的ID
	 * @param context
	 * @param packageName
	 *            包路径
	 * @return 动画类
	 */
	public static boolean startExitAnimation(int effectId, Context context,
			GLAppDrawer appDrawer, AnimationListener animationListener) {
		boolean ret = false;
		int nextInt = -1;
		Object animation = null;
		switch (effectId) {
			case 0 :
				nextInt = sRrandom.nextInt(MAX_RANDOM) + 1;
				animation = getExitAnimation(nextInt, context);
				break;
			case -1 :
				FunAppSetting funAppSetting = SettingProxy.getFunAppSetting();
				if (funAppSetting != null) {
					int[] effects = funAppSetting.getAppInOutCustomRandomEffect();
					if (effects != null) {
						nextInt = sRrandom.nextInt(effects.length);
						animation = getExitAnimation(effects[nextInt], context);
					}
				}
				break;
			default :
				animation = getExitAnimation(effectId, context);
				break;
		}
		if (animation instanceof Animation) {
			Animation enterAnimation = (Animation) animation;
			enterAnimation.setDuration(DEFAULT_DURATION);
			AnimationTask task = new AnimationTask(appDrawer, enterAnimation, animationListener,
					true, AnimationTask.PARALLEL);
			GLAnimationManager.startAnimation(task);
			ret = true;
		} else if (animation instanceof FlyAnimation) {
			FlyAnimation flyAnimation = (FlyAnimation) animation;
			flyAnimation.setDuration(DEFAULT_DURATION);
			Animation alphaAnimation = createAlphaAnimation(false, DEFAULT_DURATION);
			alphaAnimation.setAnimationListener(animationListener);
			appDrawer.startAnimation(alphaAnimation);
			GLGridViewContainer gridViewContainer = appDrawer.getGridViewContainer();
			List<GLView> glViews = gridViewContainer.getCurScreenIcons();
			if (glViews != null && !glViews.isEmpty()) {
				flyAnimation.startFlyAnimation(appDrawer.getWidth(), appDrawer.getHeight(), true,
						(ArrayList<GLView>) glViews, null, gridViewContainer.getCurGridRow(),
						gridViewContainer.getCurGridCol());
			}
			ret = true;
		}
		return ret;
	}

	private static Object getEnterAnimation(int effectId, Context context) {
		Animation animation = null;
		FunAppSetting appSetting = SettingProxy.getFunAppSetting();
		switch (effectId) {
			case EFFECT_DEFAULT : {
				if (appSetting.getTurnScreenDirection() == AppSettingDefault.TURNSCREENDIRECTION) {
					FlyAnimation flyAnimation = new FlyAnimation();
					return flyAnimation;
				} else {
					animation = new DepthAnimation(300, 0, 0, 1);
					animation.setDuration(450);
				}
				break;
			}
			case EFFECT_WINDWILL : {
				animation = new WindmillAnimation(true, -300, 0, 0, 1, 0, 360);
			}
				break;
			case EFFECT_DEPTH : {
				animation = new DepthAnimation(300, 0, 0, 1);
				animation.setDuration(450);
			}
				break;
			case EFFECT_ROTATE : { // 翻转效果
				animation = new Rotate3DAnimation(90, 0, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, 0, 1, 0);
				animation.setDuration(450);
			}
				break;
			case EFFECT_TV : { // 电视机效果
				animation = new ScaleAnimation(2, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setInterpolator(new ExponentialInterpolator());
				animation.setDuration(575);
			}
				break;
			case EFFECT_NONE : { // 无效果
				animation = null;
			}
				break;
			default :
				animation = new AlphaAnimation(0, 1);
				break;
		}
		return animation;
	}

	private static Object getExitAnimation(int effectId, Context context) {
		Animation animation = null;
		FunAppSetting appSetting = SettingProxy.getFunAppSetting();
		switch (effectId) {
			case EFFECT_DEFAULT : {
				if (appSetting.getTurnScreenDirection() == AppSettingDefault.TURNSCREENDIRECTION) {
					FlyAnimation flyAnimation = new FlyAnimation();
					return flyAnimation;
				} else {
					animation = new DepthAnimation(0, 300, 1, 0);
					animation.setDuration(400);
				}
				break;
			}
			case EFFECT_WINDWILL : {
				animation = new WindmillAnimation(true, 0, -300, 1, 0, 0, -360);
			}
				break;
			case EFFECT_DEPTH : {
				animation = new DepthAnimation(0, 300, 1, 0);
				animation.setDuration(400);
			}
				break;
			case EFFECT_ROTATE : { // 翻转效果
				animation = new Rotate3DAnimation(0, 90, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, 0, 1, 0);
				animation.setDuration(400);
			}
				break;
			case EFFECT_TV : { // 电视机效果
				animation = new ScaleAnimation(1, 2, 1, 0, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setInterpolator(new ExponentialInterpolator());
				animation.setDuration(400);
			}
				break;
			case EFFECT_NONE : { // 无效果
				animation = null;
			}
				break;
			default :
				animation = new AlphaAnimation(1, 0);
				break;
		}
		return animation;
	}

	public static AnimationSet getPopupAnimation(int animId, long duration, int offset,
			boolean directionUp) {
		int effect = FuncAppDataHandler.getInstance(ShellAdmin.sShellManager.getActivity())
				.getInoutEffect();
		AnimationSet animationSet = new AnimationSet(false);
		Animation translateAnimation = null;
		Animation alphaAnimation = null;
		switch (animId) {
			case HIDE_ANIMATION :
				if (effect == EFFECT_DEFAULT) {
					translateAnimation = createTranslateAnimation(false, duration, offset,
							directionUp);
				}
				alphaAnimation = createAlphaAnimation(false, duration);
				break;
			case SHOW_ANIMATION :
				if (effect == EFFECT_DEFAULT) {
					translateAnimation = createTranslateAnimation(true, duration, offset,
							directionUp);
				}
				alphaAnimation = createAlphaAnimation(true, duration);
				break;
			default :
				break;
		}
		if (translateAnimation != null) {
			animationSet.addAnimation(translateAnimation);
		}
		animationSet.addAnimation(alphaAnimation);
		return animationSet;
	}

	public static Animation createAlphaAnimation(boolean show, long duration) {
		float fromAlpha = 1f;
		float toAlpha = 0f;
		if (show) {
			fromAlpha = 0f;
			toAlpha = 1f;
		}
		Animation alphaAnimation = new AlphaAnimation(fromAlpha, toAlpha);
		alphaAnimation.setDuration(duration);
		return alphaAnimation;
	}

	private static Animation createTranslateAnimation(boolean show, long duration, int offset,
			boolean directionUp) {
		Animation translateAnimation = null;
		if (directionUp) {
			translateAnimation = createTranslateUpAnimation(show, offset);
			
		} else {
			translateAnimation = createTranslateDownAnimation(show, offset);
		}
		return translateAnimation;
	}

	private static Animation createTranslateUpAnimation(boolean show, int offset) {
		Animation translateAnimation;
		if (show) {
			if (GoLauncherActivityProxy.isPortait()) {
				translateAnimation = new TranslateAnimation(0, 0, offset, 0);
			} else {
				translateAnimation = new TranslateAnimation(offset, 0, 0, 0);
			}
			translateAnimation.setDuration(DEFAULT_DURATION);
			translateAnimation.setInterpolator(new DecelerateInterpolator(0.8f));
		} else {
			if (GoLauncherActivityProxy.isPortait()) {
				translateAnimation = new TranslateAnimation(0, 0, 0, offset);
			} else {
				translateAnimation = new TranslateAnimation(0, offset, 0, 0);
			}
			translateAnimation.setDuration(DEFAULT_DURATION);
			translateAnimation.setInterpolator(new DecelerateInterpolator(1.5f));
		}
		return translateAnimation;
	}

	private static Animation createTranslateDownAnimation(boolean show, int offset) {
		Animation translateAnimation;
		if (show) {
			if (GoLauncherActivityProxy.isPortait()) {
				translateAnimation = new TranslateAnimation(0, 0, -offset, 0);
			} else {
				translateAnimation = new TranslateAnimation(-offset, 0, 0, 0);
			}
			translateAnimation.setDuration(DEFAULT_DURATION);
			translateAnimation.setInterpolator(new DecelerateInterpolator(0.8f));
		} else {
			if (GoLauncherActivityProxy.isPortait()) {
				translateAnimation = new TranslateAnimation(0, 0, 0, -offset / 2);
			} else {
				translateAnimation = new TranslateAnimation(0, -offset, 0, 0);
			}
			translateAnimation.setDuration(DEFAULT_DURATION);
			translateAnimation.setInterpolator(new DecelerateInterpolator(1.5f));
		}
		return translateAnimation;
	}
}
