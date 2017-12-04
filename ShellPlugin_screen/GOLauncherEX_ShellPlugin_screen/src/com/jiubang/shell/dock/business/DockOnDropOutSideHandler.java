package com.jiubang.shell.dock.business;

import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.appdrawer.allapp.GLAllAppGridView;
import com.jiubang.shell.dock.component.DockDragHander;
import com.jiubang.shell.dock.component.GLDockLineLayout;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.screen.GLWorkspace;
/**
 * 
 * @author dingzijian
 *
 */
public class DockOnDropOutSideHandler extends DockOnDropBaseHandler {

	public DockOnDropOutSideHandler(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, DropAnimationInfo resetInfo,
			DockDragHander dockDragHander, GLDockLineLayout dockLineLayout) {
		super(source, x, y, xOffset, yOffset, dragView, dragInfo, resetInfo, dockDragHander,
				dockLineLayout);
	}

	@Override
	public boolean handleOnDrop() {
		super.handleOnDrop();
		switch (mDockDragHander.mDragResult) {
			case DockDragHander.DRAG_RESULT_NEW_FLODER :
				return isMergeFolder(mDragView, mDragInfo);
			case DockDragHander.DRAG_RESULT_ADD_IN_FLODER :
				return isIntoFolder(mDragView, mDragInfo, mResetInfo);
			case DockDragHander.DRAG_RESULT_NONE :
				onDropFinish();
				return false;
			case DockDragHander.DRAG_RESULT_INSERT :
				if (mDockDragHander.mDragIndex == -1) {
					return false;
				}
				boolean ret = false;
				if (mDragSource instanceof GLWorkspace) { // 从屏幕层添加到dock
					ret = dragOverHandle(SCREEN_TO_DOCK_CREATE_INSERT_ICON_AND_FOLDER, false,
							false, mDragView, mDockDragHander.mDragIndex, mDockLineLayout,
							mDockLineLayout.getLineID(), mDragInfo);
				} else if (mDragSource instanceof GLAllAppGridView) { // 从功能表添加到dock
					if (mDragView.getDragViewType() == DragView.DRAGVIEW_TYPE_APPDRAWER_FOLDER) {
						ret = dragOverHandle(APPDRAWER_TO_DOCK_CREATE_INSERT_FOLDER, false, false,
								mDragView, mDockDragHander.mDragIndex, mDockLineLayout,
								mDockLineLayout.getLineID(), mDragInfo);
					} else {
						ret = dragOverHandle(APPDRAWER_TO_DOCK_CREATE_INSERT_ICON, false, false,
								mDragView, mDockDragHander.mDragIndex, mDockLineLayout,
								mDockLineLayout.getLineID(), mDragInfo);
					}
				} else { // 从文件夹内拖出并添加到dock：1 屏幕层的文件夹 2 功能表里的文件夹
					ret = dragOverHandle(FOLDER_TO_DOCK_CREATE_INSERT_ICON, false, false,
							mDragView, mDockDragHander.mDragIndex, mDockLineLayout,
							mDockLineLayout.getLineID(), mDragInfo);
				}
				if (ret) {
					resetAnimationForOutFrame(mDragView, mDockDragHander.mDragIndex, mResetInfo,
							mDockDragHander.getPositionNeedCount());
				} else {
					onDropFinish();
				}
				return ret;
			case DockDragHander.DRAG_RESULT_EXCHANGE_POSITION ://处理图标换位
				boolean exchangRet = false;
				exchangRet = mDockDragHander
						.saveMoveToScreenData(mDragView, mDragInfo, mDragSource);
				mDockDragHander.clearMoveToScreenAnim();
				if (exchangRet) {
					resetAnimationForOutFrame(mDragView, mDockDragHander.getExchangeIndex(),
							mResetInfo, mDockDragHander.getPositionNeedCount());
				}
				return exchangRet;
			default :
				break;
		}
		return false;
	}

	@Override
	public void needExtrusionAnimate() {
		mNeedExtrusionAnimate = !(mDockDragHander.getPositionNeedCount() == mDockLineLayout
				.getChildCount());

	}

}
