package com.jiubang.shell.folder.status;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Intent;
import android.view.KeyEvent;

import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.shell.IShell.IFolderClosedCallback;
import com.jiubang.shell.animation.DragAnimation;
import com.jiubang.shell.animation.DragAnimation.DragAnimationInfo;
import com.jiubang.shell.appdrawer.IActionBar;
import com.jiubang.shell.appdrawer.component.GLAppDrawerAppIcon;
import com.jiubang.shell.appdrawer.controler.IconViewController;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.effect.EffectController.EffectListener;
import com.jiubang.shell.effect.IconCircleEffect;
import com.jiubang.shell.folder.BaseFolderIcon;
import com.jiubang.shell.folder.GLAppFolderBaseGridView;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-3-11]
 */
public class DrawerFolderNormalStatus extends FolderStatus {
	
	public DrawerFolderNormalStatus(GLAppFolderBaseGridView gridView) {
		super(gridView);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean ret = false;
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK :
				mFolderBaseGridView.getFolderIcon().closeFolder(true);
				ret = true;
				break;

			default :
				break;
		}
		return ret;
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

		return FolderStatusManager.GRID_NORMAL_STATUS;
	}

	@Override
	public boolean onClickUnderStatus(GLAdapterView<?> parent, GLView view, int position, long id) {
		
		if (view instanceof GLAppDrawerAppIcon) {
			GLAppDrawerAppIcon icon = (GLAppDrawerAppIcon) view;

			icon.startClickEffect(new EffectListener() {

				@Override
				public void onEffectComplete(Object callBackFlag) {
					final GLAppDrawerAppIcon icon = (GLAppDrawerAppIcon) callBackFlag;
					FunAppItemInfo info = icon.getInfo();
					final Intent intent = info.getIntent();
					boolean needCloseFolder = false;
					if (intent != null) {
						ComponentName componentName = intent.getComponent();
						if (componentName != null
								&& componentName.getPackageName().equals(PackageName.MEDIA_PLUGIN)) {
							needCloseFolder = true; // 非常特殊的资源管理插件图标，如果关闭了文件夹的话，底下会出现屏幕或功能表的View，界面严重错乱
						}
						String action = intent.getAction();
						if (ICustomAction.ACTION_PROMANAGE.equals(action)
								|| ICustomAction.ACTION_RECENTAPP.equals(action)) {
							needCloseFolder = true;
						}
					}
					if (needCloseFolder) {
						final BaseFolderIcon<?> folderIcon = mFolderBaseGridView.getFolderIcon();
						icon.post(new Runnable() {

							@Override
							public void run() {
								folderIcon.closeFolder(false, new IFolderClosedCallback() {
									public void callback() {
										MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
												ICommonMsgId.START_ACTIVITY, -1, intent, null);
										icon.postDelayed(new Runnable() {

											@Override
											public void run() {
												gameFolderAcc();
											}
										}, 2000);
									}
								});
							}
						});
					} else {
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								ICommonMsgId.START_ACTIVITY, -1, intent, null);
						icon.postDelayed(new Runnable() {

							@Override
							public void run() {
								gameFolderAcc();
							}
						}, 2000);
					}
				}

				@Override
				public void onEffectStart(Object callBackFlag) {
				}
			}, IconCircleEffect.ANIM_DURATION_CLICK, false);
			
//			icon.startWaveEffect(mShell, WaveEffect.sDURATION, WaveEffect.WAVE_SIZE,
//					WaveEffect.WAVE_DEPTH, WaveEffect.DAMPING, new EffectListener() {
//
//						@Override
//						public void onEffectComplete(Object callBackFlag) {
//							GLAppDrawerAppIcon icon = (GLAppDrawerAppIcon) callBackFlag;
//							FunAppItemInfo info = icon.getInfo();
//							Intent intent = info.getIntent();
//							MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
//									ICommonMsgId.START_ACTIVITY, -1, intent, null);
//							BaseFolderIcon<?> folderIcon = mFolderBaseGridView.getFolderIcon();
//							folderIcon.closeFolder();
//						}
//
//						@Override
//						public void onEffectStart(Object callBackFlag) {
//						}
//					});
			return true;
		}
		return false;
	}

	@Override
	public boolean onLongClickUnderStatus(GLAdapterView<?> parent, final GLView view, int position, long id) {
		if (SettingProxy.getScreenSettingInfo().mLockScreen) {
			LockScreenHandler.showLockScreenNotification(ShellAdmin.sShellManager.getActivity());
			return true;
		}
	
		mFolderStatusManager.changeGridStatus(FolderStatusManager.GRID_EDIT_STATUS);
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

}
