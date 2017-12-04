package com.jiubang.shell.appdrawer.controler;

import java.util.ArrayList;

import android.content.Intent;
import android.view.KeyEvent;
import android.widget.Toast;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.golauncher.message.IAppDrawerMsgId;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.shell.animation.DragAnimation;
import com.jiubang.shell.animation.DragAnimation.DragAnimationInfo;
import com.jiubang.shell.appdrawer.IActionBar;
import com.jiubang.shell.appdrawer.component.GLAppDrawerAppIcon;
import com.jiubang.shell.appdrawer.component.GLAppDrawerBaseGrid;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.effect.EffectController.EffectListener;
import com.jiubang.shell.effect.IconCircleEffect;
import com.jiubang.shell.folder.BaseFolderIcon;
import com.jiubang.shell.folder.GLAppDrawerFolderIcon;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.utils.ToastUtils;
/**
 * 所有tab正常状态
 * @author wuziyi
 *
 */
public class AllAppNormalStatus extends AllAppTabStatus {

	private float[] mDragTransInfo = new float[5];

	public AllAppNormalStatus(ArrayList<IActionBar> topBarGroup, GLAppDrawerBaseGrid gridView,
			ArrayList<IActionBar> bottomBarGroup) {
		super(topBarGroup, gridView, bottomBarGroup);
	}

	@Override
	public boolean onClickUnderStatus(GLAdapterView<?> parent, final GLView view, int position, long id) {
		((IconView<?>) view).startClickEffect(new EffectListener() {
			@Override
			public void onEffectComplete(Object callBackFlag) {
				if (callBackFlag instanceof GLAppDrawerAppIcon) {
					GLAppDrawerAppIcon icon = (GLAppDrawerAppIcon) callBackFlag;
					FunAppItemInfo info = icon.getInfo();
					Intent intent = info.getIntent();
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							ICommonMsgId.START_ACTIVITY, -1, intent, null);
				} else if (view instanceof GLAppDrawerFolderIcon) {
					((BaseFolderIcon<?>) view).openFolder();
				}
			}

			@Override
			public void onEffectStart(Object callBackFlag) {
			}
		}, IconCircleEffect.ANIM_DURATION_CLICK, false);
		return false;
	}

	@Override
	public boolean onLongClickUnderStatus(GLAdapterView<?> parent, final GLView view, int position,
			long id) {
		if (SettingProxy.getScreenSettingInfo().mLockScreen) {
			LockScreenHandler.showLockScreenNotification(ShellAdmin.sShellManager.getActivity());
			return true;
		}
		AppDrawerControler controler = AppDrawerControler.getInstance(ShellAdmin.sShellManager
				.getActivity());
		if (!controler.isInitedAllFunItemInfo()) {
			ToastUtils.showToast(R.string.app_fun_strat_loading, Toast.LENGTH_SHORT);
			return true;
		}
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mStatusManager.changeGridStatus(AppDrawerStatusManager.GRID_EDIT_STATUS);
				IconViewController.getInstance().removeIconNewFlag(view);
			}
		});
		if (mDragControler != null) {
			if (view instanceof IconView) {
				mDragControler.startDrag(view, mGridView, ((IconView<?>) view).getInfo(),
						DragController.DRAG_ACTION_MOVE, mDragTransInfo, new DragAnimationInfo(
								true, DragController.DRAG_ICON_SCALE, false,
								DragAnimation.DURATION_100, null));
			}
		}
		return true;
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
		return AppDrawerStatusManager.GRID_NORMAL_STATUS;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean ret = false;
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			popupMenu();
			ret = true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
					IAppDrawerMsgId.APPDRAWER_EXIT, -1, true, 0);
			return true;
		}
		if (ret) {
			return ret;
		} else {
			return super.onKeyUp(keyCode, event);
		}

	}

	@Override
	public IActionBar getBottomBarViewByGridSatus() {
		return mBottomBarGroup.get(0);
	}

	@Override
	public IActionBar getTopBarViewByGridSatus() {
		return mTopBarGroup.get(0);
	}
}
