package com.jiubang.shell.dock.business;

import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.dock.component.DockDragHander;
import com.jiubang.shell.dock.component.GLDockLineLayout;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
/**
 * 
 * @author dingzijian
 *
 */
public class DockOnDropDockHandler extends DockOnDropBaseHandler {

	public DockOnDropDockHandler(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, DropAnimationInfo resetInfo,
			DockDragHander dockDragHander, GLDockLineLayout dockLineLayout) {
		super(source, x, y, xOffset, yOffset, dragView, dragInfo, resetInfo, dockDragHander,
				dockLineLayout);
	}

	@Override
	public boolean handleOnDrop() {
		super.handleOnDrop();
		AnimationListenerHandler listenerHandler = new AnimationListenerHandler(
				AnimationListenerHandler.HOMING_ANIMATION_TO_DOCK);
		//		AnimationListener animationListener = new AnimationListener() {
		//
		//			@Override
		//			public void onAnimationStart(Animation animation) {
		//				if (mActionMenuHandler.needShowActionMenu(mX, mY, mDragView)) {
		//					mResetInfo.setNeedToShowCircle(false);
		//					DockQuickActionMenuBusiness actionMenuBusiness = new DockQuickActionMenuBusiness();
		//					actionMenuBusiness.showQuickActionMenu(mDragView.getOriginalView());
		//				}
		//			}
		//
		//			@Override
		//			public void onAnimationRepeat(Animation animation) {
		//				
		//			}
		//
		//			@Override
		//			public void onAnimationEnd(Animation animation) {
		////				mDockDragHander.hideFolder();
		////				if (mDockLineLayout.getLongClickViewIndex() != mDockDragHander.mDragIndex) {
		//				if (!mActionMenuHandler.menuState()) {
		//					mDockLineLayout.updateIndexAfterExtrusion();
		//					mDockLineLayout.updateIconsSizeAndRequestLayout();
		//				}
		////				} else {
		////					if (!mShowMenuFlag) {
		////						mDockLineLayout.clearChilderenAnimation();
		////					} else {
		////						mShowMenuFlag = false;
		////					}
		////				}
		//			}
		//
		//		};

		switch (mDockDragHander.mDragResult) {
			case DockDragHander.DRAG_RESULT_NEW_FLODER :
				return isMergeFolder(mDragView, mDragInfo);
			case DockDragHander.DRAG_RESULT_ADD_IN_FLODER :
				return isIntoFolder(mDragView, mDragInfo, mResetInfo);
			case DockDragHander.DRAG_RESULT_NONE :
				if (mDockDragHander.mDragIndex == -1) {
					resetAnimation(mDockDragHander.mDragInitIndex, mResetInfo, false, mDragView,
							listenerHandler, mDockDragHander.getPositionNeedCount());
				} else {
					resetAnimation(mDockDragHander.mDragIndex, mResetInfo, true, mDragView,
							listenerHandler, mDockDragHander.getPositionNeedCount());
				}
				break;
			case DockDragHander.DRAG_RESULT_INSERT :
				if (mDockDragHander.mDragIndex < 0) {
					onDropFinish();
				} else {
					resetAnimation(mDockDragHander.mDragIndex, mResetInfo, true, mDragView,
							listenerHandler, mDockDragHander.getPositionNeedCount());
				}
				break;
			default :
				break;
		}

		return false;
	}

	@Override
	public void needExtrusionAnimate() {
		mNeedExtrusionAnimate = mDockDragHander.getPositionNeedCount() == mDockLineLayout
				.getChildCount();
	}

}
