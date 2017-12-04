package com.jiubang.shell.animation;

import com.go.gl.view.GLView;

/**
 * 隐藏、显示背景动画接口（主要针对workspace）
 * @author linshaowu
 *
 */
public interface BackgroundAnimation {
	
	public static final int ANIMATION_TYPE_ALPHA = 0;
	public static final int ANIMATION_TYPE_BLUR = 1;

	public void hideBgAnimation(int type, GLView glView, Object...params);

	public void showBgAnimation(int type, GLView glView, Object...params);
	
	/**
	 * <br>功能简述:显示workspace内容，不做动画
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param glView
	 */
    public void showBgWithoutAnimation(int type, GLView glView, Object...params);

	/**
	 * 背景动画完成状态监听
	 * @author linshaowu
	 *
	 */
	public interface AnimationBgListener {

		public void endAnimation();
	}
}
