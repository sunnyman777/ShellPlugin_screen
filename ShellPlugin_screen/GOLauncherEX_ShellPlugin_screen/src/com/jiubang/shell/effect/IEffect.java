package com.jiubang.shell.effect;

import android.view.animation.Interpolator;

import com.go.gl.graphics.GLCanvas;
import com.jiubang.shell.effect.AbstractEffect.EffectItemObserver;

/**
 * 动画特效接口
 * @author zouguiquan
 *
 */
public interface IEffect {
	/**
	 * 更新绘画过程中的需要用到的参数,如颜色值,透明度等
	 * @param drawInfo
	 */
	public void updateEffect(Object[] drawInfo);

	/**
	 * 绘制动画
	 * @param canvas
	 * @param drawingTime
	 * @return 绘制后的状态结果
	 */
	public void drawEffect(GLCanvas canvas, long drawingTime, Object[] params);

	/**
	 * 特效是否已经结束
	 * @return true 结束 false 未结束
	 */
	public boolean isEffectComplete();

	/**
	 * 特效是否已经开始
	 * @return true 开始 false 未开始
	 */
	public boolean isEffectStart();

	/**
	 * 开启特效标记
	 */
	public void startEffect();

	/**
	 * 关闭特效标记
	 */
	public void endEffect();

	/**
	 * 获取动画延迟执行时间
	 * @return
	 */
	public long getDelay();
	
	public void setDelay(long delay);

	/**
	 * 获取动画时间
	 * @return
	 */
	public long getDuration();

	public void setInterpolation(Interpolator interpolator);
	
	/**
	 * 类似fillAfter的效果 </br>
	 * 注意：但如果使用队列EffectControler.EFFECT_MODE_SEQUENCE的话，会引起队列阻塞
	 * @param isHolding
	 */
	public void setHoldEnding(boolean isHolding);
	
	public boolean isHoldEnding();

	public void setEffectObserver(EffectItemObserver effectObserver);
	
	/**
	 * 重置状态，如果该动画对象需要重用，在调用startEffect()前调用该方法重置
	 */
	public void resetEffect();

}
