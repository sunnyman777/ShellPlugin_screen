package com.jiubang.shell.appdrawer.controler;

import java.util.ArrayList;

import android.view.KeyEvent;

import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.jiubang.shell.animation.DragAnimation;
import com.jiubang.shell.animation.DragAnimation.DragAnimationInfo;
import com.jiubang.shell.appdrawer.IActionBar;
import com.jiubang.shell.appdrawer.component.GLAppDrawerBaseGrid;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.effect.EffectController.EffectListener;
import com.jiubang.shell.effect.IconCircleEffect;
import com.jiubang.shell.folder.BaseFolderIcon;
import com.jiubang.shell.folder.GLAppDrawerFolderIcon;
import com.jiubang.shell.folder.status.FolderStatusManager;
/**
 * 所有tab编辑状态
 * @author wuziyi
 *
 */
public class AllAppEditStatus extends AllAppTabStatus {
	private float[] mDragTransInfo = new float[5];
	public AllAppEditStatus(ArrayList<IActionBar> topBarGroup, GLAppDrawerBaseGrid gridView,
			ArrayList<IActionBar> bottomBarGroup) {
		super(topBarGroup, gridView, bottomBarGroup);
	}

	@Override
	public boolean onClickUnderStatus(GLAdapterView<?> parent, final GLView view, int position, long id) {
		if (view instanceof GLAppDrawerFolderIcon) {
			
			((IconView<?>) view).startClickEffect(new EffectListener() {
				
				@Override
				public void onEffectStart(Object object) {
				}
				
				@Override
				public void onEffectComplete(Object object) {
					((BaseFolderIcon<?>) view).openFolder(FolderStatusManager.GRID_EDIT_STATUS);
				}
			}, IconCircleEffect.ANIM_DURATION_CLICK, false);
			
			return true;
		} else if (view instanceof IconView<?>) {
//			((IconView<?>) view).startClickEffect(null, IconEffect.ANIM_DURATION_CLICK, false);
			return true;
		}
		return false;
	}

	@Override
	public boolean onLongClickUnderStatus(GLAdapterView<?> parent, GLView view, int position, long id) {
		IconViewController.getInstance().removeIconNewFlag(view);
		if (view instanceof IconView) {
			mDragControler.startDrag(view, mGridView, ((IconView) view).getInfo(),
					DragController.DRAG_ACTION_MOVE, mDragTransInfo,
					new DragAnimationInfo(true, DragController.DRAG_ICON_SCALE, false,
							DragAnimation.DURATION_100, null));
			return true;
		}
		return false;
	}

	@Override
	public boolean onTouchMoveUnderStatus() {
		return false;
	}

	@Override
	public boolean onTouchUpUnderStatus() {
		return false;
	}

	@Override
	public boolean onTouchDownUnderStatus() {
		return false;
	}

	@Override
	public int getGridStatusID() {
		return AppDrawerStatusManager.GRID_EDIT_STATUS;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean ret = false;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			mStatusManager.changeGridStatus(AppDrawerStatusManager.GRID_NORMAL_STATUS);
			mGridView.doChildExitEditStateAnimation();
			ret = true;
		}
		if (ret) {
			return ret;
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}

	@Override
	public IActionBar getBottomBarViewByGridSatus() {
		return mBottomBarGroup.get(1);
	}

	@Override
	public IActionBar getTopBarViewByGridSatus() {
		return mTopBarGroup.get(1);
	}

}
