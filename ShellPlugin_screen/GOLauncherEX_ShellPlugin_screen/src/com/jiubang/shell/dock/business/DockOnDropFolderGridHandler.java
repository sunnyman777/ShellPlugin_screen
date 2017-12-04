package com.jiubang.shell.dock.business;

import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IFolderMsgId;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.dock.component.DockDragHander;
import com.jiubang.shell.dock.component.GLDockLineLayout;
import com.jiubang.shell.drag.DragInfoTranslater;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.folder.GLAppFolder;
import com.jiubang.shell.folder.GLDockFolderGridVIew;
/**
 * 
 * @author dingzijian
 *
 */
public class DockOnDropFolderGridHandler extends DockOnDropBaseHandler {

	public DockOnDropFolderGridHandler(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, DropAnimationInfo resetInfo,
			DockDragHander dockDragHander, GLDockLineLayout dockLineLayout) {
		super(source, x, y, xOffset, yOffset, dragView, dragInfo, resetInfo, dockDragHander,
				dockLineLayout);
	}

	@Override
	public boolean handleOnDrop() {
		if (GLAppFolder.getInstance().isFolderOpened()
				&& !GLAppFolder.getInstance().isFolderClosing()) {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_FOLDER, IFolderMsgId.FOLDER_KEEP_OPEN,
					-1, true, mDragInfo);
			return false;
		}
		super.handleOnDrop();
		GLDockFolderGridVIew gridView = (GLDockFolderGridVIew) mDragSource;
		mFolderId = gridView.getFolderIcon().getFolderInfo().folderId;
		ShortCutInfo folerIconInfoFromDock = (ShortCutInfo) DragInfoTranslater
				.createItemInfoForDock(mDragView.getDragViewType(), mDragInfo);
		boolean ret = false;
		switch (mDockDragHander.mDragResult) {
			case DockDragHander.DRAG_RESULT_NEW_FLODER :
				ret = isMergeFolder(mDragView, mDragInfo);
				if (ret) {
					mDockLineLayout.deleteFolderItem(null, null, folerIconInfoFromDock,
							mDockLineLayout.getLineID());
					requestFolderLayout();
				}
				return ret;
			case DockDragHander.DRAG_RESULT_ADD_IN_FLODER :
				IconView mergeViewTarget = mDockDragHander.getMergedView();
				UserFolderInfo folderInfo = null;
				if (mergeViewTarget != null) {
					DockItemInfo itemInfoTarget = (DockItemInfo) mergeViewTarget.getInfo();
					if (itemInfoTarget.mItemInfo instanceof UserFolderInfo) {
						folderInfo = (UserFolderInfo) itemInfoTarget.mItemInfo;
					}
				}
				ret = isIntoFolder(mDragView, mDragInfo, mResetInfo);
				if (ret && mFolderId != folderInfo.mInScreenId) {
					mDockLineLayout.deleteFolderItem(null, null, folerIconInfoFromDock,
							mDockLineLayout.getLineID());
					requestFolderLayout();
				} else {
					ret = false;
				}
				return ret;
			case DockDragHander.DRAG_RESULT_NONE :
				if (mDockLineLayout.getChildCount() == DockUtil.ICON_COUNT_IN_A_ROW) {
					return false;
				}
				//甩动处理，强制执行获取被甩动的view添加的位置
				mDockDragHander.doDragOver(mDragSource, mDockDragHander.mDragIndex, mX, mY,
						mXOffset, mYOffset, mDragView, (ItemInfo) mDragInfo);
				if (mDockDragHander.mDragResult == DockDragHander.DRAG_RESULT_INSERT) {
					boolean flag = dragOverHandle(FOLDER_FROM_DOCK_TO_DOCK_CREATE_INSERT_ICON,
							false, false, mDragView, mDockDragHander.mDragIndex, mDockLineLayout,
							mDockLineLayout.getLineID(), mDragInfo);
					if (!flag) {
						return false;
					}
					if (GLAppFolder.getInstance().isFolderOpened()) {
						mDockLineLayout.updateIndexAfterExtrusion();
						onDropFinish();
					} else {
						resetAnimationForOutFrame(mDragView, mDockDragHander.mDragIndex,
								mResetInfo, mDockDragHander.getPositionNeedCount());
					}
					ret = true;
				}
				return ret;
			case DockDragHander.DRAG_RESULT_INSERT :
				if (mDockDragHander.mDragIndex == -1) {
					return false;
				}
				ret = dragOverHandle(FOLDER_FROM_DOCK_TO_DOCK_CREATE_INSERT_ICON, false, false,
						mDragView, mDockDragHander.mDragIndex, mDockLineLayout,
						mDockLineLayout.getLineID(), mDragInfo);
				if (ret) {
					resetAnimationForOutFrame(mDragView, mDockDragHander.mDragIndex, mResetInfo,
							mDockDragHander.getPositionNeedCount());
					requestFolderLayout();
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

	public void requestFolderLayout() {
		if (mGLDockFolderIcon != null) {
			DockItemInfo dockItemInfo = (DockItemInfo) mGLDockFolderIcon.getInfo();
			if (null != dockItemInfo && dockItemInfo.mItemInfo instanceof UserFolderInfo) {
				UserFolderInfo folderInfo = (UserFolderInfo) dockItemInfo.mItemInfo;
				if (folderInfo.getChildCount() > 1) {
					mGLDockFolderIcon.post(new Runnable() {

						@Override
						public void run() {
							if (GLAppFolder.getInstance().isFolderOpened()) {
								MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_FOLDER,
										IFolderMsgId.FOLDER_RELAYOUT, -1, mGLDockFolderIcon);
							}
						}
					});

				}
			}
		}
	}
}
