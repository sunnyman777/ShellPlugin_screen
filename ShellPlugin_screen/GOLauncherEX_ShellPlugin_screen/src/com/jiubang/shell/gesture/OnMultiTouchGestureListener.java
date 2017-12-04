package com.jiubang.shell.gesture;

/**
 * 
 * <br>类描述: 多点触摸接口
 * <br>功能详细描述: 实现多点触摸的场景需要继承，由多指触摸控制层回调
 * 
 * @author  chenjingmian
 * @date  [2012-9-3]
 */
public interface OnMultiTouchGestureListener {
	
	public static final int DIRECTION_LEFT = 1;  
	public static final int DIRECTION_RIGHT = 2;
	public static final int DIRECTION_UP = 3;
	public static final int DIRECTION_DOWN = 4;

	/**
	* 单指滑动
	* @param p 当前触摸点
	* @param dx x方向上的移动距离
	* @param dy y方向上的移动距离
	* @return
	*/
	boolean onSwipe(PointInfo p, float dx, float dy);

	/**
	 * 双指平行滑动
	 * @param p 当前触摸点
	 * @param dx x方向上的移动距离
	 * @param dy y方向上的移动距离
	 * @param direction 方向
	 * @return
	 */
	boolean onTwoFingerSwipe(PointInfo p, float dx, float dy, int direction);

	/***
	 * 双指向内或向外缩放操作
	 * @param p 当前触摸点
	 * @param scale 缩放比例
	 * @param scaleX X轴方向的缩放比例 
	 * @param scaleY Y轴方向的缩放比例
	 * @param angle 旋转角度
	 * @param dx 第一个点在x方向上的移动距离
	 * @param dy 第一个点在y方向上的移动距离
	 * @return
	 */
	boolean onScale(PointInfo p, float scale, float scaleX, float scaleY, float dx, float dy,
			float angle);

	// /**
	// * 单击屏幕
	// * @param p 当前触摸点
	// * @return
	// */
	// boolean onSingleTap(PointInfo p);

	/**
	 * 双击屏幕
	 * @param p 当前触摸点
	 * @return
	 */
	boolean onDoubleTap(PointInfo p);

	//	boolean onLongPress(PointInfo p);

	// /**
	// * 手指快速划过屏幕，离开屏幕时有速度
	// * @param p 当前触摸点
	// * @param velocityX x方向上的速度
	// * @param velocityY y方向上的速度
	// * @return
	// */
	// boolean onFling(PointInfo p, float velocityX, float velocityY);
}
