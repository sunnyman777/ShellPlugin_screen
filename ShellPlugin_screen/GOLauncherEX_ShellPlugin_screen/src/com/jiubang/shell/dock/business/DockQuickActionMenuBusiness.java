package com.jiubang.shell.dock.business;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.util.file.media.ThumbnailManager;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IDockMsgId;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockLogicControler;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.OnDockSettingListener;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenUtils;
import com.jiubang.ggheart.apps.desks.imagepreview.ChangeIconPreviewActivity;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.apps.desks.settings.DockGestureRespond;
import com.jiubang.ggheart.components.IQuickActionId;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.FeatureItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.dock.component.GLDockIconView;
import com.jiubang.shell.dock.component.GLDockLineLayout;
import com.jiubang.shell.folder.GLDockFolderIcon;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.popupwindow.PopupWindowControler;
/**
 * 
 * @author dingzijian
 *
 */
public class DockQuickActionMenuBusiness implements PopupWindowControler.ActionListener {
	private PopupWindowControler mPopupWindowControler;

	private DockLogicControler mDockLogicControler;

	public DockQuickActionMenuBusiness() {
		mDockLogicControler = DockLogicControler.getInstance();
	}

	public boolean showQuickActionMenu(GLView target) {
		if (target == null) {
			return false;
		}
		int[] xy = new int[2];
		target.getLocationInWindow(xy);
		Rect targetRect = new Rect(xy[0], xy[1], xy[0] + target.getWidth(), xy[1]
				+ target.getHeight());
		mPopupWindowControler = ShellAdmin.sShellManager.getShell().getPopupWindowControler();
		Resources res = ShellAdmin.sShellManager.getContext().getResources();
		if (res != null) {
			mPopupWindowControler.addQuickActionMenuItem(IQuickActionId.CHANGE_ICON_DOCK,
					res.getDrawable(R.drawable.gl_icon_change),
					res.getString(R.string.menuitem_change_icon_dock));
			mPopupWindowControler.addQuickActionMenuItem(IQuickActionId.CHANGE_GESTURE_DOCK,
					res.getDrawable(R.drawable.gl_dock_menu_change_gesture),
					res.getString(R.string.menuitem_change_gesture_dock));
			mPopupWindowControler.addQuickActionMenuItem(IQuickActionId.DELETE,
					res.getDrawable(R.drawable.gl_icon_del), res.getString(R.string.deltext));
			mPopupWindowControler.showQuickActionMenu(targetRect, target, target, this, this);
			return true;
		} else {
			return false;
		}
	}

	public boolean hideQuickActionMenu(boolean dismissWithCallback) {
		if (mPopupWindowControler != null) {
			if (mPopupWindowControler.isShowing()) {
				if (dismissWithCallback) {
					mPopupWindowControler.cancel(true);
				} else {
					mPopupWindowControler.dismiss(true);
				}
				return true;
			}
			mPopupWindowControler = null;
		}
		return false;
	}

	public void actionChangeIcon(Bundle bundle, IconView<?> targetIcon) {
		IconView<?> view = targetIcon;
		if (view == null) {
			return;
		}
		DockItemInfo dockItemInfo = (DockItemInfo) view.getInfo();

		if (null == dockItemInfo || null == dockItemInfo.mItemInfo) {
			return;
		}

		FeatureItemInfo featureItemInfo = dockItemInfo.mItemInfo;

		// 数据库
		int imagetype = bundle.getInt(ImagePreviewResultType.TYPE_STRING);
		String packageStr = bundle.getString(ImagePreviewResultType.IMAGE_PACKAGE_NAME);
		String path = bundle.getString(ImagePreviewResultType.IMAGE_PATH_STRING);
		mDockLogicControler.updateShortCutItemIconForThreeD(dockItemInfo,
				featureItemInfo.mInScreenId, imagetype, 0, packageStr, path);

		// view修改
		Drawable icon = featureItemInfo.getFeatureIcon();
		if (null == icon || !(icon instanceof BitmapDrawable)) {
			return;
		}
		if (featureItemInfo instanceof ShortCutInfo) {
			ShortCutInfo shortCutInfo = (ShortCutInfo) featureItemInfo;
			shortCutInfo.setIcon(icon, true);
			((GLDockIconView) view).setIcon((BitmapDrawable) icon);
		} else if (featureItemInfo instanceof UserFolderInfo) {
			mDockLogicControler.updateFolderIconAsync(dockItemInfo, false);
		}
	}

	@Override
	public void onActionClick(int action, Object target) {
		if (null == target || null == ((IconView) target).getInfo()) {
			return;
		}
		switch (action) {
			case IQuickActionId.DELETE :
				DockItemInfo dockItemInfo;
				if (target instanceof GLDockFolderIcon) {
					dockItemInfo = ((GLDockFolderIcon) target).getInfo();
					mDockLogicControler.removeDockFolder(dockItemInfo.mItemInfo.mInScreenId);
				} else {
					dockItemInfo = ((GLDockIconView) target).getInfo();
				}
				if (delDockItemAndReArrange(dockItemInfo)) {
					IconView<?> iconView = (IconView<?>) target;
					GLDockLineLayout dockLineLayout = (GLDockLineLayout) iconView.getGLParent();
					dockLineLayout.updateIconsSizeAndRequestLayout();
				}
				;
				break;
			case IQuickActionId.CHANGE_ICON_DOCK :
				String defaultNameString = "";
				Bitmap defaultBmp = null;
				Bundle bundle = new Bundle();
				FeatureItemInfo featureItemInfo = null;
				if (target instanceof GLDockFolderIcon) {
					featureItemInfo = ((GLDockFolderIcon) target).getInfo().mItemInfo;
				} else {
					featureItemInfo = ((GLDockIconView) target).getInfo().mItemInfo;
				}
				if (featureItemInfo instanceof UserFolderInfo) {
					ChangeIconPreviewActivity.sFromWhatRequester = ChangeIconPreviewActivity.DOCK_FOLDER_STYLE; // 文件夹
					CharSequence iconName = ((UserFolderInfo) featureItemInfo).mTitle;
					if (iconName != null) {
						defaultNameString = iconName.toString(); // 系统图标名称
					}
					featureItemInfo.mFeatureIconType = ImagePreviewResultType.TYPE_DEFAULT;
					defaultBmp = ScreenUtils.getFolderBackIcon().getBitmap();
				} else if (featureItemInfo instanceof ShortCutInfo) {
					ChangeIconPreviewActivity.sFromWhatRequester = ChangeIconPreviewActivity.DOCK_STYLE_FROM_EDIT; // 图标
					CharSequence iconName = ((ShortCutInfo) featureItemInfo).mTitle;
					if (iconName != null) {
						defaultNameString = iconName.toString(); // 系统图标名称
					} else {
						defaultNameString = ((ShortCutInfo) (((GLDockIconView) target).getInfo().mItemInfo))
								.getFeatureTitle(); // dock条自定义图标
					}
					BitmapDrawable drawableTemp = mDockLogicControler
							.getOriginalIcon((ShortCutInfo) featureItemInfo);
					if (drawableTemp != null) {
						defaultBmp = drawableTemp.getBitmap();
					}
				}
				bundle.putString(ChangeIconPreviewActivity.DEFAULT_NAME, defaultNameString);
				if (defaultBmp != null) {
					defaultBmp = ThumbnailManager.getInstance(
							ShellAdmin.sShellManager.getActivity()).getParcelableBitmap(defaultBmp);
					bundle.putParcelable(ChangeIconPreviewActivity.DEFAULT_ICON_BITMAP, defaultBmp);
				}
				Intent intent = new Intent(ShellAdmin.sShellManager.getActivity(),
						ChangeIconPreviewActivity.class);
				intent.putExtras(bundle);
				ShellAdmin.sShellManager.getShell().startActivityForResultSafely(intent,
						IRequestCodeIds.REQUEST_THEME_FORICON);
				break;

			case IQuickActionId.CHANGE_GESTURE_DOCK :
				showGestureSeletion((IconView<?>) target);
				break;
			default :
				break;
		}
	}

	/**
	 * 弹出手势选择框
	 */
	private void showGestureSeletion(final IconView<?> targetIcon) {
		DockItemInfo info = null;
		//		if (targetIcon != null) {
		info = (DockItemInfo) targetIcon.getInfo();
		//		}
		//		else {
		//			return;
		//		}
		DockGestureRespond aDockGestureRespond = new DockGestureRespond(GoLauncherActivityProxy.getActivity(),
				info);
		aDockGestureRespond.mListener = new OnDockSettingListener() {

			@Override
			public void setBlank() {
				// TODO Auto-generated method stub

			}

			@Override
			public void setAppFunIcon() {
				// TODO Auto-generated method stub

			}

			@Override
			public void selectShortCut(boolean clickOrGesture) {
				if (clickOrGesture) {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDockMsgId.DOCK_ENTER_SHORTCUT_SELECT, -1, null, null);
				} else {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDockMsgId.DOCK_ENTER_SHORTCUT_SELECT_FOR_GESTURE, -1, null, null);
				}

			}

			@Override
			public void resetToDefaultIcon() {
				mDockLogicControler.resetDockItemIcon((DockItemInfo) targetIcon.getInfo());

			}

			@Override
			public void onDataChange(int msg) {
				//		if (null == getCurretnIcon()) {
				//			return;
				//		}
				DockItemInfo info = (DockItemInfo) targetIcon.getInfo();
				mDockLogicControler.gestureDataChange(msg, info);

			}

			@Override
			public Bitmap getAppDefaultIcon() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		aDockGestureRespond.show(null);
	}

	/**
	 * <br>功能简述: 删除dock的一个选项，同时对剩余项进行顺序重排
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param id
	 * @param isFromDrag
	 * @return
	 */
	private boolean delDockItemAndReArrange(DockItemInfo info) {
		return mDockLogicControler.deleteShortcutItemAndReArrange(info.getmRowId(), info.mItemInfo.mInScreenId);
	}
}
