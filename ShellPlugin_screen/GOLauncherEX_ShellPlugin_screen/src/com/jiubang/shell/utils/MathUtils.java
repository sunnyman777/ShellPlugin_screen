package com.jiubang.shell.utils;

import android.util.FloatMath;

/**
 * 与数学相关的工具类
 * @author wangzhuobin
 *
 */
public class MathUtils {
	public static final float PI_DEGREE = (float) (180.0f / Math.PI);
	/**
	 * 获取随机整数的方法
	 * @param start 开始范围
	 * @param end   结束范围
	 * @return  返回的数可能包括开始范围和结束范围[start,end]
	 */
	public static int getRandomInt(int start, int end) {
		int result = 0;
		if (end == start) {
			result = end;
		} else if (end > start) {
			result = start + (int) (Math.random() * (end - start + 1));
		}

		return result;
	}

	/**
	 * <br>功能简述: 获取两个点之间的距离的平方，相当于 x1 - x2的平方 加上 y1-y2的平方
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param point0X
	 * @param point0Y
	 * @param point1X
	 * @param point1Y
	 * @return
	 */
	public static int getDistancePow2(int point0X, int point0Y, int point1X, int point1Y) {
		return (point0X - point1X) * (point0X - point1X) + (point0Y - point1Y)
				* (point0Y - point1Y);
	}

	/**
	 * <br>功能简述: 获取两个点之间的距离
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param point0X
	 * @param point0Y
	 * @param point1X
	 * @param point1Y
	 * @return
	 */
	public static float getDistance(float point0X, float point0Y, float point1X, float point1Y) {
		return FloatMath.sqrt((point0X - point1X) * (point0X - point1X) + (point0Y - point1Y)
				* (point0Y - point1Y));
	}

	/**
	 * <br>判断一个坐标点是否在一个圆内
	 * @param x 被判断的点的x坐标
	 * @param y 被判断的点的y坐标
	 * @param cycleCenterX 被判断的圆的圆心的x坐标
	 * @param cycleCenterY 被判断的圆的圆心的y坐标
	 * @param cycleRadius  被判断的圆的半径
	 * @return
	 */
	public static boolean isInsideCycle(float x, float y, float cycleCenterX, float cycleCenterY,
			float cycleRadius) {
		float deltaX = x - cycleCenterX;
		float deltaY = y - cycleCenterY;
		return deltaX * deltaX + deltaY * deltaY <= cycleRadius * cycleRadius;
	}

	/**
	 * <br>功能简述: 获取三角型一个角的度数
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param line_near1 邻边长度
	 * @param line_near2 邻边长度
	 * @param line_opposite 对边长度
	 * @return 角的度数，0-360
	 */
	public static float getTrangleDegree(float line_near1, float line_near2, float line_opposite) {
		float pow2 = line_near1 * line_near1 + line_near2 * line_near2 - line_opposite
				* line_opposite;
		return (float) (180.0f / Math.PI * Math.acos(pow2 / (2 * line_near1 * line_near2)));
	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public static float getDegree(float x1, float y1, float cycle_center_x, float cycle_center_y,
			float radius) {
		float relativeX = x1 - cycle_center_x;
		float degree = (float) (180.0f / Math.PI * Math.acos(relativeX / radius));
		return y1 > cycle_center_y ? -degree : degree;
	}

	public static final float piAngleToDegree(double piValue) {
		return (float) (PI_DEGREE * piValue);
	}
}
