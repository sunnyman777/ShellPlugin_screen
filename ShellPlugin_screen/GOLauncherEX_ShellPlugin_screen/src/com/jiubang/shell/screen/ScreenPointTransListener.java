package com.jiubang.shell.screen;
/**
 * 
 * 屏幕层的位置转换监听器
 * 针对添加状态（屏幕缩小）的位置映射处理
 * @author jiangxuwen
 *
 */
public interface ScreenPointTransListener {

	/**
	 * 获取相对于大屏幕的坐标
	 * 
	 * @param x
	 * @param y
	 * @param real  长度为2，相对于大屏幕的真实值
	 * @return true 有经过映射处理  false 没经过映射处理
	 */
	public boolean virtualPointToReal(float x, float y, float[] real);
	
	/**
	 * 获取相对于小屏幕的坐标
	 * 
	 * @param x
	 * @param y
	 * @param virtual 长度为2，相对于小屏幕的虚拟值
	 * @return true 有经过映射处理  false 没经过映射处理
	 */
	public boolean realPointToVirtual(float x, float y, float[] virtual);
	
}
