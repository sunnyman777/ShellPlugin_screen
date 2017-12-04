package com.jiubang.shell.animation;

/**
 * 
 * <br>类描述:动画类的常量
 * <br>功能详细描述:
 * 
 * @author  yuanzhibiao
 * @date  [2012-9-2]
 */
public class AnimationConstant {
	/**
	 * 长按屏幕层图标时放大倍数
	 */
	public static final float DRAG_SCALE_UP = 1.15f;

	/**
	 * 反过来，放下view的时候图标的缩小倍数，
	 * {@link #DRAG_SCALE_UP } 的倒数
	 */
	public static final float DROP_SCALE_DOWN = 1.0f / DRAG_SCALE_UP;

	/**
	 * 长按图标文字淡出效果时间
	 */
	public static final int LONG_CLICK_UP_ALPHA_DURATION = DragAnimation.DURATION_288 - 50; //此处减去100是为了保证文字淡出动画比长按抬起动画先完成 //CHECKSTYLE IGNORE

	/**
	 * 长按图标放手文字淡入效果时间
	 */
	public static final int LONG_CLICK_DOWN_ALPHA_DURATION = 200 ; //CHECKSTYLE IGNORE //文件夹里边放手动画执行时间为200 为了保证文字能够完成淡入动画，时间设置不能大于200
	//此处减去100是为了保证文字淡出动画比长按抬起动画先完成
	//	public static final int LongClickDownAlpha_DURATION = LongClickIconBackAnimCreator.DURATION_TRANSLATE_150 + LongClickIconBackAnimCreator.DURATION_SCALE_300;

	/**
	 * 动画的中心比例值(旋转、缩放等用到)
	 */
	public static final float SCALE_CENTER = 0.5f;
	
	/**
	 * 动画透明度最大值
	 */
	public static final int ANIMATION_MAX_ALPHA = 255; 

}
