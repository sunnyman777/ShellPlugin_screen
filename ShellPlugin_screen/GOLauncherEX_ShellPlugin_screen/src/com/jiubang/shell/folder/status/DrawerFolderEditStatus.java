package com.jiubang.shell.folder.status;

import java.util.ArrayList;

import android.view.KeyEvent;

import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.shell.animation.DragAnimation;
import com.jiubang.shell.animation.DragAnimation.DragAnimationInfo;
import com.jiubang.shell.appdrawer.IActionBar;
import com.jiubang.shell.appdrawer.component.GLAppDrawerAppIcon;
import com.jiubang.shell.appdrawer.controler.IconViewController;
import com.jiubang.shell.common.component.GLModel3DMultiView;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.listener.UninstallListener;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.effect.IconCircleEffect;
import com.jiubang.shell.folder.GLAppFolderBaseGridView;
import com.jiubang.shell.model.IModelState;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-3-11]
 */
public class DrawerFolderEditStatus extends FolderStatus {

	public DrawerFolderEditStatus(GLAppFolderBaseGridView gridView) {
		super(gridView);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK :
				mFolderStatusManager.changeGridStatus(FolderStatusManager.GRID_NORMAL_STATUS);
				break;

			default :
				break;
		}
		return true;
	}
	@Override
	public void popupMenu() {

	}
	
	@Override
	public void dismissMenu() {
		
	}

	@Override
	public ArrayList<IActionBar> getBottomBarViewGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<IActionBar> getTopBarViewGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IActionBar getBottomBarViewByGridSatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IActionBar getTopBarViewByGridSatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IActionBar getBottomBarViewByOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IActionBar getTopBarViewByOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTabStatusID() {
		return FolderStatusManager.DRAWER_FOLDER_GRID;
	}

	@Override
	public int getGridStatusID() {
		return FolderStatusManager.GRID_EDIT_STATUS;
	}

	@Override
	public boolean onClickUnderStatus(GLAdapterView<?> parent, GLView view, int position, long id) {
		if (view instanceof IconView<?>) {
			((IconView<?>) view).startClickEffect(null, IconCircleEffect.ANIM_DURATION_CLICK, false);
			
//			((IconView<?>) view).startWaveEffect(mShell, WaveEffect.sDURATION, WaveEffect.WAVE_SIZE,
//					WaveEffect.WAVE_DEPTH, WaveEffect.DAMPING, null);
			return true;
		}
		return false;
	}

	@Override
	public boolean onLongClickUnderStatus(GLAdapterView<?> parent, GLView view, int position, long id) {
		IconViewController.getInstance().removeIconNewFlag(view);
		if (mDragControler != null) {
			if (view instanceof IconView) {
				mDragControler.startDrag(view, (DragSource) parent, ((IconView) view).getInfo(),
						DragController.DRAG_ACTION_MOVE, mDragTransInfo, new DragAnimationInfo(
								true, DragController.DRAG_ICON_SCALE, false,
								DragAnimation.DURATION_100, null));
				return true;
			}
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
	public void onGridViewHide() {
//		changeToNarmal();
		super.onGridViewHide();
	}
	@Override
	public void onGridViewShow() {
		int count = mFolderBaseGridView.getChildCount();
		for (int i = 0; i < count; i++) {
			GLView viewItem = mFolderBaseGridView.getChildAt(i);
			if (viewItem instanceof GLAppDrawerAppIcon) {
				GLAppDrawerAppIcon icon = (GLAppDrawerAppIcon) viewItem;
				FunAppItemInfo info = icon.getInfo();
				if (info  != null && !info.isSysApp()) {
					GLModel3DMultiView multiView = icon.getMultiView();
					multiView.setCurrenState(IModelState.UNINSTALL_STATE);
					multiView.setOnSelectClickListener(new UninstallListener(info.getAppItemInfo(),
							null));
				}
				icon.startShake();
			}
		}
		super.onGridViewShow();
	}
}
