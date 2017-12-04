package com.jiubang.shell.screen;

/**
 * 
 * @author zhujian
 *
 */
public abstract class AbsZeroHandler {

	protected GLSuperWorkspace mGlSuperWorkspace;
	protected GLWorkspace mGLWorkspace;

	
	public AbsZeroHandler(GLSuperWorkspace workspace) {
		mGlSuperWorkspace = workspace;
		if (mGlSuperWorkspace != null) {
			mGLWorkspace = mGlSuperWorkspace.getGLWorkSpace();
		}
	}
	
	abstract void setCureentScreen(int currentScreen); //设置当前屏
	
	abstract int getCureentScreen(); //获取当前屏
	
	abstract int snapToScreen(int screen); //跳转

	abstract int getIndicatorItemsSize(); //获取指示器应该显示的个数
	
	abstract void setCurrentScreenForMoveScreen(); //没有地方调用，先放着
	
	abstract int getDrawingScreenA(); //特效相关，不是很清楚
	abstract int getDrawingScreenB(); //特效相关，不是很清楚
	
	abstract int onScreenChanged(int newScreen); // 屏幕索引改变时重设当前屏
	
	abstract int onDropScreenCount(int screenCount); //放手后的screentCount
	
	abstract int onDragMultiTouchEventbeyongZero(); //velocityX > 0处理多点触控问题
	
	abstract int onDragMultiTouchEventBelowZero(); //velocityX <= 0处理多点触控问题

	abstract int clickIndicatorItem(int index); //点击指示器索引处理
	
	abstract void changeZeroScreenLayout(); // 处理这个COMMON_CONFIGURATION_CHANGEED消息
	
	abstract boolean canDoubleTap(); //是否支持双击
	
	abstract boolean removeZeroScreen(); //移除0屏

	abstract void addZeroScreen(); //添加0屏
	
	abstract boolean isInZeroScreen(); //是否在0屏


	
	
}
