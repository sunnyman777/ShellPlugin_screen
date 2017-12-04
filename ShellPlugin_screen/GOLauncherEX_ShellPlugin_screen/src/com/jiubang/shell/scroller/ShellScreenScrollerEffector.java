package com.jiubang.shell.scroller;

import com.go.gl.graphics.GLCanvas;

/**
 * 分屏滚动器的特效器
 * 
 * @author dengweiming
 * 
 */
public interface ShellScreenScrollerEffector {
	/**
	 * 设置具体某种特效的类型
	 * 
	 * @param type
	 */
	public void setType(int type);

	/**
	 * 使用随机选择特效的时候，选取下一个特效
	 */
	public void updateRandomEffect();

	/**
	 * 绘制视图
	 * 
	 * @param canvas
	 * @return
	 */
	public boolean onDraw(GLCanvas canvas);

	/**
	 * 被加载时的响应
	 * 
	 * @param container
	 */
	public void onAttach(ShellScreenScrollerListener container);
	
	/**
	 * 把非当前的特效与scroller绑定
	 * 
	 * @param container
	 */
	public void onAttachReserveEffector(ShellScreenScrollerListener container);

	/**
	 * 被卸载时的响应
	 */
	public void onDetach();

	/**
	 * 视图大小或者滚动方向变化时的响应
	 * 
	 * @param w
	 * @param h
	 * @param orientation
	 */
	public void onSizeChanged(int w, int h, int orientation);

	/**
	 * 获取期望的最大过冲比例[0, 50)
	 * 
	 * @return 0表示不使用过冲插值器（切屏时没有回弹的效果）
	 */
	public int getMaxOvershootPercent();

	/**
	 * 设置绘图质量
	 * 
	 * @param quality
	 */
	public void setDrawQuality(int quality);

	/**
	 * 关闭时的处理
	 */
	public void recycle();

	/**
	 * 设置屏幕之前的间隙
	 * 
	 * @param gap
	 */
	public void setScreenGap(int gap);

	/**
	 * 设置上边距
	 * 
	 * @param top
	 */
	public void setTopPadding(int top);

	/**
	 * 设置是否可以上下滑动
	 * 
	 * @param verticalSlide
	 */
	public void setVerticalSlide(boolean verticalSlide);

	/**
	 * 是否需要启用Next Widget的绘图缓冲
	 * @return
	 */
	public boolean isNeedEnableNextWidgetDrawingCache();
	
	/**
	 * 是否禁用壁纸滚动
	 * @return
	 */
	public boolean disableWallpaperScrollDelay();
	
	/**
	 * 滚动开始
	 * @param source
	 */
	public void onScrollStart();
	
	/**
	 * 滚动结束
	 * @param source
	 */
	public void onScrollEnd();
	
	/**
	 * 甩动
	 */
	public void onFlipStart();
	
	/**
	 * 甩动中断
	 * @param source
	 */
	public void onFlipInterupted();
	
	/**
	 * 是否动画中
	 * @return
	 */
	public boolean isAnimationing();
	
	/**
	 * 主题切换
	 */
	public void onThemeSwitch();
	
	/**
	 * 通知特效重新获取屏幕矩阵
	 */
	public void notifyRegetScreenRect();
	
	/**
	 * 滚动中手指刚离开的一刻 
	 */
	public void onScrollTouchUp();
	
	public int getType();
	
	/**
	 * <br>功能简述:获取具体特效
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public Object getEffector();
}
