package com.jiubang.shell.indicator;

import android.os.Bundle;

/**
 * 指示器更新监听器
 * @author wuziyi
 *
 */
public interface IndicatorUpdateListener {
	/**
	 * 更新指示器
	 * @param num 指示器总共有多少个点
	 * @param current 当前在第几点上
	 */
	public void updateIndicator(int total, int current);
	
	/**
	 * 滑动更新指示器
	 * @param type 是点状还是条状指示器
	 * @param bundle 滑动偏移量
	 */
	public void onSlidingScreen(int type, Bundle bundle);
}
