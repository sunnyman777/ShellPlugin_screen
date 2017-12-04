package com.jiubang.shell.folder.status;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.util.AppUtils;
import com.go.util.ConvertUtils;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IFolderMsgId;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.apps.desks.imagepreview.ChangeIconPreviewActivity;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.components.IQuickActionId;
import com.jiubang.ggheart.components.renamewindow.RenameActivity;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FeatureItemInfo;
import com.jiubang.ggheart.data.info.GestureSettingInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.RelativeItemInfo;
import com.jiubang.ggheart.data.info.SelfAppItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderController;
import com.jiubang.shell.IShell.IFolderClosedCallback;
import com.jiubang.shell.animation.DragAnimation;
import com.jiubang.shell.animation.DragAnimation.DragAnimationInfo;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.appdrawer.IActionBar;
import com.jiubang.shell.appdrawer.controler.IconViewController;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.effect.EffectController.EffectListener;
import com.jiubang.shell.effect.IconCircleEffect;
import com.jiubang.shell.folder.BaseFolderIcon;
import com.jiubang.shell.folder.GLAppFolderBaseGridView;
import com.jiubang.shell.folder.GLScreenFolderGridView;
import com.jiubang.shell.folder.adapter.GLScreenFolderAdapter;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.popupwindow.PopupWindowControler;
import com.jiubang.shell.popupwindow.component.actionmenu.QuickActionMenuHandler;
import com.jiubang.shell.screen.component.GLScreenShortCutIcon;
import com.jiubang.shell.utils.ToastUtils;
/**
 * 
 * <br>类描述:DOCK与Screen共用这个Status，但不是同一个对象。
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-3-11]
 */
public class ScreenFolderNormalStatus extends FolderStatus
		implements
			PopupWindowControler.ActionListener {

//	private QuickActionMenu mClickActionMenu;
	private PopupWindowControler mPopupWindowControler;

//	private Runnable mCloseActionMenuR;

	private ShortCutInfo mActionInfo;
	public ScreenFolderNormalStatus(GLAppFolderBaseGridView gridView) {
		super(gridView);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
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
		return null;
	}

	@Override
	public ArrayList<IActionBar> getTopBarViewGroup() {
		return null;
	}

	@Override
	public IActionBar getBottomBarViewByGridSatus() {
		return null;
	}

	@Override
	public IActionBar getTopBarViewByGridSatus() {
		return null;
	}

	@Override
	public IActionBar getBottomBarViewByOrder() {

		return null;
	}

	@Override
	public IActionBar getTopBarViewByOrder() {
		return null;
	}

	@Override
	public int getTabStatusID() {

		return FolderStatusManager.SCREEN_FOLDER_GRID;
	}

	@Override
	public int getGridStatusID() {

		return FolderStatusManager.GRID_NORMAL_STATUS;
	}

	@Override
	public boolean onClickUnderStatus(GLAdapterView<?> parent, GLView view, int position, long id) {
		if (view instanceof GLScreenShortCutIcon) {
			GLScreenShortCutIcon icon = (GLScreenShortCutIcon) view;
			
			icon.startClickEffect(new EffectListener() {

				@Override
				public void onEffectComplete(Object callBackFlag) {
					final GLScreenShortCutIcon icon = (GLScreenShortCutIcon) callBackFlag;
					ShortCutInfo info = icon.getInfo();
					final Intent intent = info.mIntent;
//					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, ICommonMsgId.START_ACTIVITY,
//							-1, intent, null);
					Rect rect = new Rect();
					icon.getGlobalVisibleRect(rect);

					final ArrayList<Object> posArrayList = new ArrayList<Object>();

					posArrayList.add(rect);
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
												ICommonMsgId.START_ACTIVITY, -1, intent,
												posArrayList);
										icon.postDelayed(new Runnable() {
											
											@Override
											public void run() {
												gameFolderAcc();
											}
										}, 2000);
									};
								});
							}
						});
					} else {
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								ICommonMsgId.START_ACTIVITY, -1, intent, posArrayList);
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
//							GLScreenShortCutIcon icon = (GLScreenShortCutIcon) callBackFlag;
//							ShortCutInfo info = icon.getInfo();
//							Intent intent = info.mIntent;
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
	public boolean onLongClickUnderStatus(GLAdapterView<?> parent, GLView view, int position, long id) {
		if (SettingProxy.getScreenSettingInfo().mLockScreen) {
			LockScreenHandler.showLockScreenNotification(ShellAdmin.sShellManager.getActivity());
			return true;
		}
		IconViewController.getInstance().removeIconNewFlag(view);
		if (mDragControler != null) {
			view.setPressed(false);
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

	/**
	 * 显示操作菜单
	 */
	private boolean showQuickActionMenu(GLView target) {
		hideQuickActionMenu(false);
		if (target == null) {
			return false;
		}
		int[] xy = new int[2];
		target.getLocationInWindow(xy);
//		if (!StatusBarHandler.isHide()) {
//			xy[1] += StatusBarHandler.getStatusbarHeight(StatusBarHandler.TYPE_NOT_FULLSCREEN_RETURN_HEIGHT);
//		}
		Rect targetRect = new Rect(xy[0], xy[1], xy[0] + target.getWidth(),
				(int) (xy[1] + target.getHeight() * 0.9));
		ItemInfo itemInfo = (ItemInfo) target.getTag();
		if (itemInfo != null && itemInfo.mItemType != IItemType.ITEM_TYPE_FAVORITE) {
			int itemType = itemInfo.mItemType;

//			mClickActionMenu = new QuickActionMenu(ShellAdmin.sShellManager.getActivity(),
//					itemInfo, targetRect, mShell.getOverlayedViewGroup(), this);
			mPopupWindowControler = ShellAdmin.sShellManager.getShell().getPopupWindowControler();
			
			Resources res = ShellAdmin.sShellManager.getContext().getResources();
			switch (itemType) {
				case IItemType.ITEM_TYPE_APPLICATION : {
					ShortCutInfo cutInfo = (ShortCutInfo) itemInfo;
					// 判断是否屏幕广告图标,是就不给换图标和卸载
					if (cutInfo.mIntent != null
							&& cutInfo.mIntent.getAction() != null
							&& cutInfo.mIntent.getAction().equals(
									ICustomAction.ACTION_SCREEN_ADVERT)) {

						// Log.i("jiang", "广告图标");
						mPopupWindowControler.addQuickActionMenuItem(IQuickActionId.RENAME,
								res.getDrawable(R.drawable.gl_icon_rename),
								res.getString(R.string.renametext));
						mPopupWindowControler.addQuickActionMenuItem(IQuickActionId.DELETE,
								res.getDrawable(R.drawable.gl_icon_del),
								res.getString(R.string.deltext));
					} else {
						mPopupWindowControler.addQuickActionMenuItem(IQuickActionId.CHANGE_ICON,
								res.getDrawable(R.drawable.gl_icon_change),
								res.getString(R.string.menuitem_change_icon_dock));
						mPopupWindowControler.addQuickActionMenuItem(IQuickActionId.RENAME,
								res.getDrawable(R.drawable.gl_icon_rename),
								res.getString(R.string.renametext));
						mPopupWindowControler.addQuickActionMenuItem(IQuickActionId.DELETE,
								res.getDrawable(R.drawable.gl_icon_del),
								res.getString(R.string.deltext));
						AppItemInfo appItemInfo = cutInfo.getRelativeItemInfo();
						if (appItemInfo != null && !appItemInfo.getIsSysApp()) {
							mPopupWindowControler.addQuickActionMenuItem(IQuickActionId.UNINSTALL,
									res.getDrawable(R.drawable.gl_icon_uninstall),
									res.getString(R.string.uninstalltext));
						}
					}

				}
					break;
				case IItemType.ITEM_TYPE_SHORTCUT :
					mPopupWindowControler.addQuickActionMenuItem(IQuickActionId.CHANGE_ICON,
							res.getDrawable(R.drawable.gl_icon_change),
							res.getString(R.string.menuitem_change_icon_dock));
					mPopupWindowControler.addQuickActionMenuItem(IQuickActionId.RENAME,
							res.getDrawable(R.drawable.gl_icon_rename),
							res.getString(R.string.renametext));
					mPopupWindowControler.addQuickActionMenuItem(IQuickActionId.DELETE,
							res.getDrawable(R.drawable.gl_icon_del),
							res.getString(R.string.deltext));
					break;

				default :
					break;
			}
			mPopupWindowControler.showQuickActionMenu(targetRect, target, itemInfo, this, this);
		}
		return true;
	}

	/**
	 * 取消弹出菜单
	 * 
	 * @param dismissWithCallback
	 *            ， 是否回调， true仅取消菜单显示，false会回调到
	 *            {@link QuickActionMenu.onActionListener#onActionClick(int, View)} 并传回一个
	 *            {@link IQuickActionId#CANCEL}事件
	 */
	protected void hideQuickActionMenu(boolean dismissWithCallback) {
		if (mPopupWindowControler != null) {
			if (dismissWithCallback) {
				mPopupWindowControler.cancel(true);
			} else {
				mPopupWindowControler.dismiss(true);
			}
			mPopupWindowControler = null;
		}
	}

	@Override
	public void onActionClick(int action, Object target) {

		ShortCutInfo targetInfo = null;
		if (target != null && target instanceof ShortCutInfo) {
			targetInfo = (ShortCutInfo) target;
		} else if (target == null || !(target instanceof GLView)) {
			return;
		}

		//		targetInfo = targetInfo == null ? (ItemInfo) ((GLView) target).getTag() : targetInfo;
		//		if (targetInfo == null) {
		//			return;
		//		}
		//		mDraggedItemId = targetInfo.mInScreenId;
		switch (action) {
			case IQuickActionId.CANCEL : {
				// MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
				// IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.DRAG_FRAME, null, null);
				break;
			}

			case IQuickActionId.CHANGE_ICON : {
				BitmapDrawable iconDrawable = null;
				String defaultNameString = "";
				//			if (mDraggedItemId >= 0) {
				//				GLView editingView = ScreenUtils.getViewByItemId(mDraggedItemId,
				//						mGLWorkspace.getCurrentScreen(), mGLWorkspace);
				//				if (editingView != null) {
				//					if (editingView.getTag() instanceof RelativeItemInfo) {
				//						RelativeItemInfo tagInfo = (RelativeItemInfo) editingView.getTag();

				if (targetInfo instanceof ShortCutInfo) {
					if (null != targetInfo.getRelativeItemInfo()) {
						iconDrawable = targetInfo.getRelativeItemInfo().getIcon();
						if (targetInfo.getRelativeItemInfo().mTitle != null) {
							defaultNameString = targetInfo.getRelativeItemInfo().mTitle.toString();
						}
						mActionInfo = targetInfo;
					}
				} /*
					* else if (editingView instanceof FolderIcon) { iconDrawable =
					* ScreenUtils.getFolderBackIcon(); if (((FolderIcon) editingView).getText()
					* != null) { defaultNameString = ((FolderIcon) editingView).getText()
					* .toString(); } }
					*/
				//					}
				//				}
				//			}
				/*
				 * if (target instanceof FolderIcon) { ChangeIconPreviewActivity.sFromWhatRequester =
				 * ChangeIconPreviewActivity.USER_FOLDER_STYLE; // 文件夹 } else {
				 */
				if (mFolderBaseGridView instanceof GLScreenFolderGridView) {
					ChangeIconPreviewActivity.sFromWhatRequester = ChangeIconPreviewActivity.SCREEN_FOLDER_ITEM_STYLE; // 图标
				} else {
					ChangeIconPreviewActivity.sFromWhatRequester = ChangeIconPreviewActivity.DOCK_FOLDER_ITEM_STYLE; // 图标
				}
				// }

				Bundle bundle = new Bundle();
				if (null != iconDrawable) {
					bundle.putParcelable(ChangeIconPreviewActivity.DEFAULT_ICON_BITMAP,
							iconDrawable.getBitmap());
				}
				bundle.putString(ChangeIconPreviewActivity.DEFAULT_NAME, defaultNameString);
				try {
					Intent intent = new Intent(ShellAdmin.sShellManager.getActivity(),
							ChangeIconPreviewActivity.class);
					intent.putExtras(bundle);
					ShellAdmin.sShellManager.getActivity().startActivityForResult(intent,
							IRequestCodeIds.REQUEST_THEME_FORICON);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case IQuickActionId.RENAME : {
				mActionInfo = targetInfo;
				Intent intent = new Intent(ShellAdmin.sShellManager.getActivity(),
						RenameActivity.class);
				intent.putExtra(RenameActivity.NAME, targetInfo.mTitle);
				intent.putExtra(RenameActivity.HANDLERID, IDiyFrameIds.APP_FOLDER);
				intent.putExtra(RenameActivity.ITEMID, targetInfo.mInScreenId);
				intent.putExtra(RenameActivity.SHOW_RECOMMENDEDNAME, false);
				intent.putExtra(RenameActivity.FINISH_WHEN_CHANGE_ORIENTATION, false);
				ShellAdmin.sShellManager.getActivity().startActivityForResult(intent,
						IRequestCodeIds.REQUEST_RENAME);
				break;
			}
			case IQuickActionId.DELETE : {
				if (null != targetInfo) {
					if (mFolderBaseGridView.getAdapter().getCount() < 3) {
						mFolderBaseGridView.getFolderIcon().closeFolder(true);
						int handlerId = -1;
						if (mFolderBaseGridView instanceof GLScreenFolderGridView) {
							handlerId = IDiyFrameIds.SCREEN;
						} else {
							handlerId = IDiyFrameIds.DOCK;
						}
						MsgMgrProxy.sendMessage(this, handlerId, IFolderMsgId.FOLDER_APP_LESS_TWO, -1,
								mFolderBaseGridView.getFolderIcon(), targetInfo);
					} else {
						mFolderBaseGridView.getAdapter().remove(targetInfo);
						GLAppFolderController.getInstance().removeAppFromScreenFolder(
								targetInfo.mInScreenId,
								mFolderBaseGridView.getFolderIcon().getFolderInfo().folderId);
						clearIconViewAndReLayout(targetInfo);
					}
				}
				break;
			}
			case IQuickActionId.UNINSTALL : {
				uninstallApp(targetInfo);
				break;
			}
		}

	}

	private void clearIconViewAndReLayout(ShortCutInfo targetInfo) {
		mFolderBaseGridView.clearDeletedView(targetInfo);
		if (mFolderBaseGridView.getChildCount() < 1) {
			mFolderBaseGridView.getFolderIcon().closeFolder(true);
			return;
		}
		mFolderBaseGridView.post(new Runnable() {

			@Override
			public void run() {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_FOLDER,
						IFolderMsgId.FOLDER_RELAYOUT, -1);

			}
		});
	}
	public void actionChangeIcon(Bundle iconBundle) {
		//		GLView editingView = null;
		Drawable iconDrawable = null;
		//		if (mDraggedItemId >= 0) {
		//			editingView = ScreenUtils.getViewByItemId(mDraggedItemId,
		//					mGLWorkspace.getCurrentScreen(), mGLWorkspace);
		//
		//			if ((editingView != null) /* && (editingView instanceof BubbleTextView) */
		//					&& (iconBundle != null)) {
		//				ItemInfo tagInfo = (ItemInfo) editingView.getTag();
		//				if (tagInfo == null) {
		//					// Log.i(LOG_TAG, "change icon fail tagInfo == null");
		//					return;
		//				}

		boolean isDefaultIcon = false;
		int type = iconBundle.getInt(ImagePreviewResultType.TYPE_STRING);
		if (ImagePreviewResultType.TYPE_RESOURCE_ID == type) {
			int id = iconBundle.getInt(ImagePreviewResultType.IMAGE_ID_STRING);
			/*
			 * iconDrawable = mActivity.getResources().getDrawable(id);
			 * 
			 * if (tagInfo instanceof FeatureItemInfo) { ((FeatureItemInfo)
			 * tagInfo).setFeatureIcon(iconDrawable, type, null, id, null);
			 * updateDesktopItem(tagInfo.mScreenIndex, tagInfo); }
			 */
		} else if (ImagePreviewResultType.TYPE_IMAGE_FILE == type) {
			if (mActionInfo instanceof FeatureItemInfo) {
				String path = iconBundle.getString(ImagePreviewResultType.IMAGE_PATH_STRING);
				((FeatureItemInfo) mActionInfo).setFeatureIcon(null, type, null, 0, path);
				if (((FeatureItemInfo) mActionInfo).prepareFeatureIcon()) {
					iconDrawable = ((FeatureItemInfo) mActionInfo).getFeatureIcon();
					updateFolderApp(mActionInfo);
				}
			}
		} else if (ImagePreviewResultType.TYPE_IMAGE_URI == type) {
			if (mActionInfo instanceof FeatureItemInfo) {
				String path = iconBundle.getString(ImagePreviewResultType.IMAGE_PATH_STRING);
				((FeatureItemInfo) mActionInfo).setFeatureIcon(null, type, null, 0, path);
				if (((FeatureItemInfo) mActionInfo).prepareFeatureIcon()) {
					iconDrawable = ((FeatureItemInfo) mActionInfo).getFeatureIcon();
					updateFolderApp(mActionInfo);
				}
			}
		} else if (ImagePreviewResultType.TYPE_PACKAGE_RESOURCE == type
				|| ImagePreviewResultType.TYPE_APP_ICON == type) {
			String packageStr = iconBundle.getString(ImagePreviewResultType.IMAGE_PACKAGE_NAME);
			String path = iconBundle.getString(ImagePreviewResultType.IMAGE_PATH_STRING);
			ImageExplorer imageExplorer = ImageExplorer.getInstance(ShellAdmin.sShellManager
					.getActivity());
			iconDrawable = imageExplorer.getDrawable(packageStr, path);
			if (null != iconDrawable) {
				if (iconDrawable instanceof NinePatchDrawable) {
					// Toast.makeText(mActivity,
					// R.string.folder_change_ninepatchdrawable_toast,
					// Toast.LENGTH_LONG).show();
					return;
				}
				if (mActionInfo instanceof FeatureItemInfo) {
					((FeatureItemInfo) mActionInfo).setFeatureIcon(iconDrawable, type, packageStr,
							0, path);
					//					updateDesktopItem(mActionInfo.mScreenIndex, mActionInfo);
					updateFolderApp(mActionInfo);
				}
			}
		} else {
			BitmapDrawable bmp = null;
			if (mActionInfo instanceof RelativeItemInfo) {
				bmp = ((RelativeItemInfo) mActionInfo).getRelativeItemInfo().getIcon();
			}
			if (null != bmp) {
				bmp.setTargetDensity(ApplicationProxy.getContext().getResources().getDisplayMetrics());
				iconDrawable = bmp;
				isDefaultIcon = true;
			}
			if (mActionInfo instanceof FeatureItemInfo) {
				((FeatureItemInfo) mActionInfo).resetFeature();
				//				updateDesktopItem(mActionInfo.mScreenIndex, mActionInfo);
				updateFolderApp(mActionInfo);
			}
		}

		//		if (iconDrawable == null) {
		//			Toast.makeText(mActivity, R.string.save_image_error, Toast.LENGTH_LONG).show();
		//			return;
		//		}

		//		((BubbleTextView) editingView).setIcon(iconDrawable);
		//		setItemIcon(editingView, iconDrawable, !isDefaultIcon);

		mFolderBaseGridView.getFolderIcon().refreshIcon();
	}

	private void updateFolderApp(ShortCutInfo shortCutInfo) {
		if (shortCutInfo != null) {
			mFolderController.updateScreenFolderApp(mFolderBaseGridView.getFolderIcon()
					.getFolderInfo().folderId, mActionInfo);
			prepareItemInfo(shortCutInfo);
			GLScreenShortCutIcon shortCutIcon = (GLScreenShortCutIcon) mFolderBaseGridView
					.getAdapter().getViewByItem(shortCutInfo);
			shortCutIcon.refreshIcon();
		}
	}

	public void actionChangeAppName(String name, long itemId) {
		if (mActionInfo != null && mActionInfo.mInScreenId == itemId) {
			mActionInfo.setFeatureTitle(name);
			mActionInfo.mTitle = name;
			mActionInfo.mIsUserTitle = true;
			updateFolderApp(mActionInfo);
		}
	}

	private boolean prepareItemInfo(ItemInfo info) {
		boolean bRet = false;
		if (null == info) {
			return bRet;
		}
		try {
			// 关联
			// 图标、名称
			switch (info.mItemType) {
				case IItemType.ITEM_TYPE_APPLICATION : {
					ShortCutInfo sInfo = (ShortCutInfo) info;
					if (null == sInfo.getRelativeItemInfo()) {
						bRet |= sInfo.setRelativeItemInfo(mFolderController.getAppItemInfo(
								sInfo.mIntent, null, sInfo.mItemType));
					} else if (sInfo.getRelativeItemInfo() instanceof SelfAppItemInfo) {
						AppItemInfo appItemInfo = mFolderController.getAppItemInfo(sInfo.mIntent,
								null, sInfo.mItemType);
						if (!(appItemInfo instanceof SelfAppItemInfo)) {
							bRet |= sInfo.setRelativeItemInfo(appItemInfo);
						}
					}
					if (null == sInfo.getFeatureIcon()) {
						bRet |= sInfo.prepareFeatureIcon();
					}

					// 数据冗余导致
					if (null != sInfo.getFeatureIcon()) {
						sInfo.mIcon = sInfo.getFeatureIcon();
						sInfo.mIsUserIcon = true;
					} else {
						if (null != sInfo.getRelativeItemInfo()) {
							sInfo.mIcon = sInfo.getRelativeItemInfo().getIcon();
						}
						sInfo.mIsUserIcon = false;
					}
					if (null != sInfo.getFeatureTitle()) {
						sInfo.mTitle = sInfo.getFeatureTitle();
						sInfo.mIsUserTitle = true;
					} else {
						if (null != sInfo.getRelativeItemInfo()) {
							sInfo.mTitle = sInfo.getRelativeItemInfo().getTitle();
						}
						sInfo.mIsUserTitle = false;
					}
				}
					break;

				case IItemType.ITEM_TYPE_SHORTCUT : {
					ShortCutInfo sInfo = (ShortCutInfo) info;
					if (null == sInfo.getRelativeItemInfo()) {
						bRet |= sInfo.setRelativeItemInfo(mFolderController.getAppItemInfo(
								sInfo.mIntent, null, sInfo.mItemType));
					}
					if (null == sInfo.getFeatureIcon()) {
						bRet |= sInfo.prepareFeatureIcon();
					}

					// 数据冗余导致
					if (null != sInfo.getFeatureIcon()) {
						sInfo.mIcon = sInfo.getFeatureIcon();
						sInfo.mIsUserIcon = true;
					} else {
						if (null != sInfo.getRelativeItemInfo()) {
							sInfo.mIcon = sInfo.getRelativeItemInfo().getIcon();
						}
						sInfo.mIsUserIcon = false;
					}
					if (null != sInfo.getFeatureTitle()) {
						sInfo.mTitle = sInfo.getFeatureTitle();
						sInfo.mIsUserTitle = true;
					} else {
						if (null != sInfo.getRelativeItemInfo()) {
							sInfo.mTitle = sInfo.getRelativeItemInfo().getTitle();
						}
						sInfo.mIsUserTitle = false;
					}
				}
					break;
				default :
					break;
			}
		} catch (Exception e) {

		}
		return bRet;
	}

	public void actionUninstallApp(ArrayList<AppItemInfo> uninstallapps) {
		GLScreenFolderAdapter adapter = (GLScreenFolderAdapter) mFolderBaseGridView.getAdapter();
		for (AppItemInfo appItemInfo : uninstallapps) {
			for (int i = 0; i < adapter.getCount(); i++) {
				ShortCutInfo shortCutInfo = adapter.getItem(i);
				if (null != shortCutInfo
						&& ConvertUtils.intentCompare(appItemInfo.mIntent, shortCutInfo.mIntent)) {
					clearIconViewAndReLayout(shortCutInfo);
					break;
				}
			}
		}
	}

	private void uninstallApp(ShortCutInfo shortCutInfo) {
		if (shortCutInfo == null) {
			return;
		}
		if (shortCutInfo.mIntent != null) {
			final ComponentName componentName = shortCutInfo.mIntent.getComponent();
			if (componentName != null) {
				try {
					// go主题和go精品假图标提示用户不能删除
					if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE.equals(shortCutInfo.mIntent
							.getAction())
							|| ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME
									.equals(shortCutInfo.mIntent.getAction())
							|| ICustomAction.ACTION_FUNC_SPECIAL_APP_GOWIDGET
									.equals(shortCutInfo.mIntent.getAction())) {
						ToastUtils.showToast(R.string.uninstall_fail, Toast.LENGTH_LONG);
						// ScreenUtils.showToast(R.string.uninstall_fail, mActivity);
					} else {
						AppUtils.uninstallPackage(ShellAdmin.sShellManager.getActivity(),
								componentName.getPackageName());
					}
				} catch (Exception e) {
					ToastUtils.showToast(R.string.uninstall_fail, Toast.LENGTH_LONG);
					// 处理卸载异常
					// ScreenUtils.showToast(R.string.uninstall_fail, mActivity);
				}
			}
		} else {
			ToastUtils.showToast(R.string.uninstall_fail, Toast.LENGTH_LONG);
			// 卸载失败
			// ScreenUtils.showToast(R.string.uninstall_fail, mActivity);
		}
	}
	// }

	@Override
	public boolean onHomeAction(GestureSettingInfo info) {
//		boolean ret = false;
//		if (mClickActionMenu != null && mClickActionMenu.isShowing()) {
//			hideQuickActionMenu(false);
//			ret = true;
//		}
//		ret |= super.onHomeAction(info);
//		return ret;
		
		hideQuickActionMenu(false);
		return super.onHomeAction(info);
	}
	
	@Override
	public void onDrop(DragSource source, final int x, final int y, int xOffset, int yOffset,
			final DragView dragView, Object dragInfo, final DropAnimationInfo resetInfo) {
		resetInfo.setAnimationListener(new AnimationListenerAdapter() {

			@Override
			public void onAnimationStart(Animation animation) {
				if (QuickActionMenuHandler.getInstance().needShowActionMenu(x, y, dragView)) {
					resetInfo.setNeedToShowCircle(false);
					showQuickActionMenu(dragView.getOriginalView());
					QuickActionMenuHandler.getInstance().reset();
				}

			}

		});
	}
}
