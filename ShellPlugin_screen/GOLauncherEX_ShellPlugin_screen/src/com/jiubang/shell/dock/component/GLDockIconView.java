/**
 * 
 */
package com.jiubang.shell.dock.component;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;

import com.gau.golauncherex.plugin.shell.R;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.util.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.launcher.AppIdentifier;
import com.jiubang.ggheart.plugin.ISecurityPoxy;
import com.jiubang.ggheart.plugin.notification.NotificationType;
import com.jiubang.shell.common.component.GLModel3DMultiView;
import com.jiubang.shell.common.component.GLModel3DView;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.model.CommonImageManager;
import com.jiubang.shell.model.IModelItemType;
import com.jiubang.shell.model.IModelState;

/**
 * @author zhujian
 * 
 */
public class GLDockIconView extends IconView<DockItemInfo> implements BroadCasterObserver {

	/**
	 * 后台数据信息
	 */
	// private DockItemInfo mInfo;

	private GLModel3DMultiView mMultiView;
	private GLModel3DView mItemView;
	/**
	 * 发给handler的消息，setimagebitmap
	 */
	public static final int CHANGE_ICON_STRING = 1;
	
	/**
	 * 是什么类型的通讯统计程序，默认为0,不是通讯统计程序类型定义在NotificationType.java
	 */
	private int mNotificationType = NotificationType.IS_NOT_NOTIFICSTION;
	/**
	 * @param context
	 */
	public GLDockIconView(Context context) {
		this(context, null);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public GLDockIconView(Context context, AttributeSet attrs) {
		super(context, attrs);
//		CommonImageManager.getInstance().registerObserver(this);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mMultiView = (GLModel3DMultiView) findViewById(R.id.multmodel);
		mItemView = (GLModel3DView) mMultiView.findViewById(R.id.model);
		setGravity(Gravity.CENTER);
		mMultiView.setBgImageDrawable(CommonImageManager.getInstance().getDrawable(
				CommonImageManager.RES_FOLDER_BG));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onBCChange(int msgId, int param, Object ...object) {
		switch (msgId) {
			case DockItemInfo.ICONCHANGED :
				Message msg = new Message();
				msg.what = CHANGE_ICON_STRING;
				// 必须放在msg queue首，不然会出现拖动后4个变5个，闪一下大图标才显示小图标
				mHandler.sendMessageAtFrontOfQueue(msg);
				break;
			case AppItemInfo.UNREADCHANGE :
			case AppItemInfo.IS_NEW_APP_CHANGE : {
				post(new Runnable() {

					@Override
					public void run() {
						checkSingleIconNormalStatus();
						invalidate();
					}
				});
				break;
			}
			/**
			 * 用于CN包的代码
			 */
			case ISecurityPoxy.DOCK_SECURITY_STATE_CHANGED :
				post(new Runnable() {

					@Override
					public void run() {
						checkSingleIconNormalStatus();
						invalidate();
					}
				});
				break;
			case DockItemInfo.INTENTCHANGED : {
				//不知道是啥
				break;
			}
			//			case IScreenFrameMsgId.COMMON_IMAGE_CHANGED : {
			//				if (mMultiView == null) {
			//					return;
			//				}
			//				mMultiView.setBgImageDrawable(CommonImageManager.getInstance().getDrawable(
			//						CommonImageManager.RES_FOLDER_BG));
			//			}
			//				break;
			default :
				break;
		}
	}

	/**
	 * 判断是不是、是什么通讯统计程序
	 * 
	 * @param info
	 *            被判断对象
	 * @return 什么类型
	 */
	private int whichNotificationType(DockItemInfo info) {
		int type = NotificationType.IS_NOT_NOTIFICSTION;
		if (info == null) {
			return type;
		}
		// 判断内容：
		// 1:info.mIntent 内部Intent
		// 2:info.getAppItemInfo().mIntent 外部Intent
		if (info.mItemInfo.mItemType == IItemType.ITEM_TYPE_APPLICATION
				|| info.mItemInfo.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
			type = AppIdentifier.whichTypeOfNotification(ShellAdmin.sShellManager.getActivity(),
					((ShortCutInfo) info.mItemInfo).getRelativeItemInfo());
		}
		if (info.mItemInfo.mItemType == IItemType.ITEM_TYPE_USER_FOLDER) {
			type = NotificationType.NOTIFICATIONTYPE_DESKFOLDER;
		}

		if (type == NotificationType.IS_NOT_NOTIFICSTION
				&& info.mItemInfo.getRelativeItemInfo() != null) {
			type = AppIdentifier.whichTypeOfNotification(ShellAdmin.sShellManager.getActivity(),
					info.mItemInfo.getRelativeItemInfo());
		}

		return type;
	}

	/**
	 * @return the mNotificationType
	 */
	public int getmNotificationType() {
		return mNotificationType;
	}

	public void reset() {
		mNotificationType = NotificationType.IS_NOT_NOTIFICSTION;
	}

	/**
	 * @return the info
	 */
	@Override
	public DockItemInfo getInfo() {
		return mInfo;
	}

	/**
	 * @param info
	 *            the info to set
	 */
	@Override
	public void setInfo(DockItemInfo info) {
		if (null != mInfo) {
			mInfo.unRegisterObserver(this);
		}
		super.setInfo(info);
		if (mInfo != null) {
			mInfo.registerObserver(this);
			// 判断是不是、是什么通讯统计程序
			mNotificationType = whichNotificationType(mInfo);
			setIconSize(mInfo.getBmpSize());
		}
	}

	@Override
	public void setIcon(BitmapDrawable icon) {
		if (icon != null) {
			mItemView.changeTexture(icon.getBitmap());
		}
	}

	// dock栏目没有文字，需要复写这个方法
	@Override
	public void getHitRect(Rect outRect) {
		// if (mItemView != null) {
		// mItemView.getHitRect(outRect);
		// }
		// outRect.right += mLeft;
		// outRect.bottom += mTop;
		// outRect.left += mLeft;
		// outRect.top += mTop;
		outRect.set(mLeft, mTop, mRight, mBottom);
	}

	@Override
	public void setTitle(CharSequence title) {
		// do nothing
	}

	@Override
	public void refreshIcon() {
		if (mInfo != null) {
			mItemView.setModelItem(IModelItemType.GENERAL_ICON);
			setIcon(mInfo.getIcon());
			checkSingleIconNormalStatus();
			post(new Runnable() {

				@Override
				public void run() {
					invalidate();
				}
			});
		}
		if (mIconRefreshObserver != null) {
			mIconRefreshObserver.onIconRefresh();
		}
	}

	@Override
	public Rect[] getOperationArea(Rect[] rect, Object... params) {
		if (rect == null) {
			rect = new Rect[2];
		}
		int count = 0;
		if (params.length > 0) {
			count = (Integer) params[0];
		}
		int bitmap_size = DockUtil.getIconSize(count);
		int mDockBgHeight = DockUtil.getBgHeight();

		ShellAdmin.sShellManager.getShell().getContainer().getLocation(this, mLoc);
		int left = mLoc[0];
		int top = mLoc[1];
		int right = left + mWidth;
		int bottom = top + mHeight;
		if (rect[0] == null) {
			rect[0] = new Rect(left, top, right, bottom);
		} else {
			rect[0].left = left;
			rect[0].top = top;
			rect[0].right = right;
			rect[0].bottom = bottom;
		}

		if (rect[1] == null) {
			rect[1] = new Rect();
		}
		if (GoLauncherActivityProxy.isPortait()) {
			rect[1].left = rect[0].left + bitmap_size / 5;
			rect[1].right = rect[0].right - bitmap_size / 5;
			rect[1].top = rect[0].top + mDockBgHeight / 5;
			rect[1].bottom = rect[0].bottom - mDockBgHeight / 5;
		} else {
			rect[1].left = rect[0].left + mDockBgHeight / 5;
			rect[1].right = rect[0].right - mDockBgHeight / 5;
			rect[1].top = rect[0].top + bitmap_size / 5;
			rect[1].bottom = rect[0].bottom - bitmap_size / 5;
		}

		getHitRect(mMaxInnerRect);
		if (rect[1].contains(mMaxInnerRect)) {
			rect[1] = mMaxInnerRect;
		}

		return rect;
	}

	@Override
	public void cleanup() {
		super.cleanup();
//		if (mInfo != null) {
//			mInfo.selfDestruct();
//		}
//		CommonImageManager.getInstance().unRegisterObserver(this);
	}

	@Override
	public void onIconRemoved() {
		if (mInfo != null) {
			mInfo.unRegisterObserver(this);
		}
//		CommonImageManager.getInstance().unRegisterObserver(this);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case CHANGE_ICON_STRING :
					if (null == mInfo || null == mInfo.getIcon()) {
						return;
					}
					BitmapDrawable drawable = mInfo.getIcon();
					setIconSize(mInfo.getBmpSize());
					setIcon(drawable);
					break;

				default :
					break;
			}
			if (mIconRefreshObserver != null) {
				mIconRefreshObserver.onIconRefresh();
			}
		};
	};

	public void reloadResource() {
		if (mMultiView != null) {
			mMultiView.setBgImageDrawable(CommonImageManager.getInstance().getDrawable(
					CommonImageManager.RES_FOLDER_BG));
		}
	}

	@Override
	public void checkSingleIconNormalStatus() {
		if (mInfo != null && mInfo.mItemInfo.getRelativeItemInfo() != null) {
//			int dangerLevel = ISecurityPoxy.DANGER_LEVEL_NONE;
//			try {
//				dangerLevel = SecurityPoxyFactory.getSecurityPoxy().getDangerLevel(mInfo.mItemInfo.getRelativeItemInfo());
//			} catch (UnsupportSecurityPoxyException e) {
//			}
//			if (dangerLevel > ISecurityPoxy.DANGER_LEVEL_UNKNOW) {
//				if (dangerLevel == ISecurityPoxy.DANGER_LEVEL_SAFE) { // 扫描结果安全
//					mMultiView.setCurrenState(IModelState.SAFE_STATE, null);
//				} else if (dangerLevel == ISecurityPoxy.DANGER_LEVEL_UNSAFE) { // 扫描结果危险
//					mMultiView.setCurrenState(IModelState.DANGER_STATE, null);
//				}
////				mMultiView.setOnSelectClickListener(null);
//			} else 
			if (mInfo.mItemInfo.getRelativeItemInfo().isNew()) { // 显示New标识
				mMultiView.setCurrenState(IModelState.NEW_STATE);
//				mMultiView.setOnSelectClickListener(null);
			} else if (mInfo.mItemInfo.getRelativeItemInfo().getUnreadCount() > 0) { // 通讯统计的未读数字
				mMultiView.setCurrenState(IModelState.STATE_COUNT, mInfo.mItemInfo.getRelativeItemInfo().getUnreadCount());
//				mMultiView.setOnSelectClickListener(null);
			} else { // 没有任何状态
				mMultiView.setCurrenState(IModelState.NO_STATE);
//				mMultiView.setOnSelectClickListener(null);
			}
		}
	};
}
