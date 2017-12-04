package com.jiubang.shell.common.listener;

import com.jiubang.shell.common.component.TransformationInfo;

/**
 * GLView图形图像相关信息的监听器
 * @author jiangxuwen
 *
 */
public interface TransformListener {
  
	/**
	 * 设置缩放比
	 * @param scaleX 宽度缩放比例值
	 * @param scaleY 高度缩放比例值
	 */
	public void setScaleXY(float scaleX, float scaleY);
	
	/**
	 * 设置缩放的轴心
	 * @param pivotX 轴心位于x方向的值
	 * @param pivotY 轴心位于y方向的值
	 */
	public void setPivotXY(float pivotX, float pivotY);
	
	/**
	 * 设置偏移量
	 * @param transX x方向的偏移值
	 * @param transY y方向的偏移值
	 */
	public void setTranslateXY(float transX, float transY);
	
	/**
	 * 进行归位的动画
	 */
	public void animateToSolution();
	
	/**
	 * 设置图形图像相关信息的载体
	 * @param info
	 */
	public void setTransformationInfo(TransformationInfo info);
	
	/**
	 * 获取图形图像相关信息的载体
	 * @param info
	 */
	TransformationInfo getTransformationInfo();
	
	/**
	 * 设置是否自动适应格子大小
	 * @param autoFit
	 */
	public void setAutoFit(boolean autoFit);
	
}
