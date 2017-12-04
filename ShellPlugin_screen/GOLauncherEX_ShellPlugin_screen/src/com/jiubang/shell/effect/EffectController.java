package com.jiubang.shell.effect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLView;

/**
 * 特效控制器,可以插入多种特效组合成动画,可顺序执行或同时执行
 * @author zouguiquan
 */
public class EffectController {

	private List<IEffect> mEffects;
	private Object mCallbackFlag;
	private List<EffectListener> mEffectListeners;
//	private boolean mEnableEffect;

	private long mAnimationStartTime = NOT_START;
	// 动画未开始
	private static final long NOT_START = -1;

	private int mEffectMode = EFFECT_MODE_TOGETHER;
	//顺序执行
	public static final int EFFECT_MODE_TOGETHER = 0;
	//同时执行
	public static final int EFFECT_MODE_SEQUENCE = 1;

	public EffectController() {
		mEffects = new ArrayList<IEffect>();
		mEffectListeners = new ArrayList<EffectListener>();
	}

	/**
	 * 开始画特效
	 * @param canvas
	 * @param drawingTime
	 */
	public void doEffect(GLCanvas canvas, long drawingTime, final GLView glView, Object... params) {
		if (isEffectStart() && !isAllEffectCompleted() || needHoldEnding()) {
			if (mAnimationStartTime == NOT_START) {
				//特效开始
				mAnimationStartTime = drawingTime;
				if (mEffectListeners != null) {
					for (EffectListener listener : mEffectListeners) {
						listener.onEffectStart(mCallbackFlag);
					}
				}
			}
			if (mEffectMode == EFFECT_MODE_TOGETHER) {
				//特效同时在画
				Iterator<IEffect> it = mEffects.iterator();
				while (it.hasNext()) {
					IEffect effect = it.next();
					if (!effect.isEffectComplete() || effect.isHoldEnding()) {
						effect.drawEffect(canvas, drawingTime, params);
						if (effect.isEffectComplete() && !effect.isHoldEnding()) {
							it.remove();
						}
					} else {
						it.remove();
					}
				}
			} else if (mEffectMode == EFFECT_MODE_SEQUENCE) {
				drawEffectOneByOne(canvas, drawingTime, params);
			}
			glView.invalidate();
			if (isAllEffectCompleted()) {
				completeEffect();
			}
		}
	}

	private void drawEffectOneByOne(GLCanvas canvas, long drawingTime, Object... params) {
		IEffect effect = mEffects.get(0);
		if (!effect.isEffectComplete() || effect.isHoldEnding()) {
			effect.drawEffect(canvas, drawingTime, params);
			if (effect.isEffectComplete() && !effect.isHoldEnding()) {
				mEffects.remove(effect);
			}
		} else {
			mEffects.remove(effect);
			drawEffectOneByOne(canvas, drawingTime, params);
		}
	}
	
	private boolean needHoldEnding() {
		if (null == mEffects || mEffects.size() == 0) {
			return false;
		}

		for (IEffect effect : mEffects) {
			if (null != effect && effect.isHoldEnding()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 结束特效
	 */
	public void completeEffect() {
		if (mEffectListeners != null) {
			for (EffectListener listener : mEffectListeners) {
				if (listener != null) {
					listener.onEffectComplete(mCallbackFlag);
				}
			}
		}
//		endEffect();
	}

	/**
	 * 添加一个图标类型的特效
	 */
	public void addEffect(IEffect effect) {
		if (effect != null && !mEffects.contains(effect)) {
			mEffects.add(effect);
		} else {
			throw new IllegalArgumentException("the same effect instance already exist, if you wanna to repeat the effect again, pls create another instance");
		}
	}

	/**
	 * 调用此方法,然后invalidate,触发特效
	 * @param callbackFlag
	 */
	public void startAllEffect(Object callbackFlag, GLView glView) {
		mCallbackFlag = callbackFlag;
		for (IEffect effect : mEffects) {
			effect.startEffect();
		}
		glView.invalidate();
	}
	
	public void setCallbackFlag(Object callbackFlag) {
		mCallbackFlag = callbackFlag;
	}

	public void endAllEffect() {
		for (IEffect effect : mEffects) {
			if (effect != null) {
				effect.endEffect();
			}
		}
		// 清理工作不让你做 让clearEffect()做
//		mEffects.clear();
		mAnimationStartTime = NOT_START;
	}

	/**
	 * 所有特效是否已经被触发
	 * @return
	 */
	public boolean isAllEffectStart() {
		if (null == mEffects || mEffects.size() == 0) {
			return false;
		}

		for (IEffect effect : mEffects) {
			if (null != effect && !effect.isEffectStart()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 是否有任一特效已经被触发
	 * @return
	 */
	public boolean isEffectStart() {
		if (null == mEffects || mEffects.size() == 0) {
			return false;
		}

		for (IEffect effect : mEffects) {
			if (null != effect && effect.isEffectStart()) {
				return true;
			}
		}
		return true;
	}

	/**
	 * 特效是否已经完成
	 * @return
	 */
	public boolean isAllEffectCompleted() {
		if (null == mEffects || mEffects.size() == 0) {
			return true;
		}

		for (IEffect effect : mEffects) {
			if (null != effect && !effect.isEffectComplete()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 清除所有动画
	 */
	public void clearEffect() {
		if (mEffects != null) {
			mEffects.clear();
		}
		mAnimationStartTime = NOT_START;
	}

	public void addEffectListener(EffectListener listener) {
		if (listener != null && !mEffectListeners.contains(listener)) {
			mEffectListeners.add(listener);
		}
	}
	
	public void cleanEffectListener() {
		if (mEffectListeners != null) {
			mEffectListeners.clear();
		}
	}

	/**
	 * 对整个特效的回调
	 */
	public static interface EffectListener {
		public void onEffectStart(Object object);
		public void onEffectComplete(Object object);
	}

	public int getEffectMode() {
		return mEffectMode;
	}

	public void setEffectMode(int effectMode) {
		mEffectMode = effectMode;
	}
}
