package com.jiubang.shell.popupwindow.component.actionmenu;

import com.go.gl.view.GLView;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-5-17]
 */
public class QuickActionMenuHandler {
	private static volatile QuickActionMenuHandler sInstance;
	private boolean mShowMenu;
	private int mDragStartX = -1; // 起始拖动点
	private int mDragStartY = -1; // 起始拖动点

	private QuickActionMenuHandler() {
	}
	public static QuickActionMenuHandler getInstance() {
		if (sInstance == null) {
			sInstance = new QuickActionMenuHandler();
		}
		return sInstance;
	}

	public void onDragStart(DragSource source, Object info, int dragAction) {
		mShowMenu = true;
	}
	
	public void onDragOver(int x, int y, DragView dragView) {
		needShowActionMenu(x, y, dragView);
	}
	
	public void onDragEnd() {
		reset();
	}

	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		// 设置dragX，dragY的初始值，永于判断是否取消气泡菜单
		// ADT-12679 3D桌面－DOCK栏：dock栏添加文件夹后，拖动文件夹小幅度向上甩动，会弹出编辑菜单且菜单位置显示不正确
		// 多次调用onDragEnter，初始坐标会被多次重置，此处改为只初始化一次
		if (mDragStartX == -1 && mDragStartY == -1) {
			mDragStartX = x;
			mDragStartY = y;
		}
	}

	public boolean needShowActionMenu(int x, int y, DragView dragView) {
		GLView gLTargetView = dragView.getOriginalView();
		//		// add by dengdazhong 2013-05-22 for ADT-12587 3D桌面－屏幕：桌面卸载的小部件还存在缩放、主题等菜单，请修改
		//		boolean isErrorWidget = (gLTargetView instanceof GLWidgetContainer)
		//				&& (((GLWidgetContainer) gLTargetView).getWidget() instanceof GLWidgetErrorView);
		//		// add by dengdazhong 2013-05-22 end
		if (mDragStartX > 0
				&& mDragStartY > 0
				&& ((Math.abs(mDragStartX - x) > gLTargetView.getWidth() / 8 || Math
						.abs(mDragStartY - y) > gLTargetView.getHeight() / 8))) {
			// 取消弹出菜单
			mShowMenu = false;
		}
		return mShowMenu;
	}

	public boolean menuState() {
		return mShowMenu;
	}

	public void reset() {
		mDragStartX = -1;
		mDragStartY = -1;
		mShowMenu = false;
	}
}
