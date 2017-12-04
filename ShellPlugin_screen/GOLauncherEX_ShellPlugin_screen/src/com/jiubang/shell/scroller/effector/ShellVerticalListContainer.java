package com.jiubang.shell.scroller.effector;

import com.go.gl.graphics.GLCanvas;

/**
 * 竖向容器
 * @author yejijiong
 *
 */
public interface ShellVerticalListContainer {
	public static final int PART_UP = 0;
	public static final int PART_DOWN = 1;
	
	/**
	 * 绘制瀑布特效部分
	 * 
	 * @param canvas
	 *            画布
	 * @param drawPart
	 * 			  绘制部分
	 */
	public void drawWaterFallEffector(GLCanvas canvas, int drawPart);
}
