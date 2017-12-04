package com.jiubang.shell.indicator;

/**
 * 
 * 指示器监听接口
 * 
 */
public interface IndicatorListener {

	public abstract void clickIndicatorItem(int index);

	public abstract void sliding(float percent);
	
}
