package com.jiubang.shell.folder;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IFolderMsgId;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.shell.IShell;
import com.jiubang.shell.appdrawer.component.GLAppDrawerBaseGrid;
import com.jiubang.shell.appdrawer.controler.AppDrawerStatusManager;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.model.IModelState;
import com.jiubang.shell.utils.IconUtils;

/**
 * 
 * @author dingzijian
 *
 */
public class GLAppDrawerFolderIcon extends BaseFolderIcon<FunFolderItemInfo> {
	private static final String TAG = "GLAppDrawerFolderIcon";
	public GLAppDrawerFolderIcon(Context context) {
		super(context);
		init();
	}

	public GLAppDrawerFolderIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	protected void init() {
		super.init();
		setEnableAutoTextLine(true);
		AppDrawerControler.getInstance(ShellAdmin.sShellManager.getActivity()).registerObserver(
				this);
	}
	
	@Override
	public void refreshIcon() {
		if (mInfo != null) {
			setTitle(mInfo.getTitle());
			if (!mInfo.getFolderContent().isEmpty()) {
				mFolderContent = mInfo.getFolderContent();
				createFolderThumbnail(mFolderContent, -1);
				mItemView.onIconRefresh();
				super.refreshIcon();
			}
		}
	}

	protected void addIconBitmap(int intdex) {
		FunAppItemInfo appItemInfo = (FunAppItemInfo) mFolderContent.get(intdex);
		Bitmap iconBitmap = appItemInfo.getAppItemInfo().mIcon.getBitmap();
		appItemInfo.registerObserver(this);
		mIconBitmaps.add(iconBitmap);
	}
	
	@Override
	public void onBCChange(int msgId, int param, Object ...object) {
		switch (msgId) {
			case AppDrawerControler.HIDE_APPS :
				refreshIcon();
				break;
			case FunFolderItemInfo.UPDATA_UNREAD :
				if (AppDrawerStatusManager.getInstance().getCurStatus().getGridStatusID() != AppDrawerStatusManager.GRID_EDIT_STATUS) {
					post(new Runnable() {
						
						@Override
						public void run() {
							// 为了避免在功能表编辑抖动状态下，因为换右上角标志，引发刷新
							checkSingleIconNormalStatus();
						}
					});
				}
//				if (mShakeTask == null) {
//					checkSingleIconNormalStatus();
//				}
//				checkSingleIconNormalStatus();
				break;
			case AppItemInfo.INCONCHANGE :
				post(new Runnable() {
					
					@Override
					public void run() {
						refreshIcon();
						
					}
				});
//				mItemView.onIconRefresh();
				break;
			case FunFolderItemInfo.ADDITEM :
				if (GLAppFolder.getInstance().isFolderOpened(getFolderInfo().folderId)) {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_FOLDER,
							IFolderMsgId.FOLDER_RELAYOUT, 0, this);
				}
				break;
			default :
				break;
		}
		super.onBCChange(msgId, param, object);
	}

	@Override
	public void closeFolder(boolean animate, Object...objs) {
		ShellAdmin.sShellManager.getShell().showStage(IShell.STAGE_APP_DRAWER, animate, objs);

	}
	
	@Override
	public void setInfo(FunFolderItemInfo info) {
		FunFolderItemInfo oldInfo = mInfo;
		if (oldInfo != null) {
			oldInfo.unRegisterObserver(this);
		}
		super.setInfo(info);
		if (mInfo != null) {
			mInfo.registerObserver(this);
		}
	}
	
	@Override
	public void setPressed(boolean pressed) {
		super.setPressed(pressed);
		if (pressed) {
			setAlpha(CLICK_HALF_ALPHA);
		} else {
			setAlpha(CLICK_NO_ALPHA);
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
//		initIconFromSetting(false);
		if (getGLParent() instanceof GLAppDrawerBaseGrid && mTitleView != null
				&& mTitleView.isVisible()) {
			IconUtils.sAppDrawerIconTextHeight = mTitleView.getHeight();
		}
	}
	
	@Override
	public void onIconRemoved() {
		AppDrawerControler.getInstance(ShellAdmin.sShellManager.getActivity()).unRegisterObserver(
				this);
		if (mInfo != null) {
			mInfo.unRegisterObserver(this);
		}
		super.onIconRemoved();
	}
	
	@Override
	public void checkSingleIconNormalStatus() {
		if (mInfo != null) {
			if (mInfo.isNew()) {
				mMultiView.setCurrenState(IModelState.NEW_STATE);
				mMultiView.setOnSelectClickListener(null);
			} else if (mInfo.getUnreadCount() > 0) { // 通讯统计的未读数字
				mMultiView.setCurrenState(IModelState.STATE_COUNT, mInfo.getUnreadCount());
				mMultiView.setOnSelectClickListener(null);
			} else { // 没有任何状态
				super.checkSingleIconNormalStatus();
			}
		}
	}

	@Override
	protected int getFolderIconSize() {
		return getIconSize();
	}

	@Override
	public void onEditAnimationFinish() {
		if (getFolderChildCount() == 8) {
			post(new Runnable() {
				@Override
				public void run() {
					resetElementTranslate();
					refreshIcon();

				}
			});
		}
		super.onEditAnimationFinish();
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initIconFromSetting(false);
	}
}
