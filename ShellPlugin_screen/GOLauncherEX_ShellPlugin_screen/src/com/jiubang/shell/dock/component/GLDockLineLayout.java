package com.jiubang.shell.dock.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.util.Log;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.view.GLView.OnLongClickListener;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.util.device.Machine;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IDockMsgId;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.diy.AppInvoker;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockLogicControler;
import com.jiubang.ggheart.apps.desks.diy.frames.tipsforgl.GuideControler;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.SysShortCutControler;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.FeatureItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderController;
import com.jiubang.shell.animation.BackgroundAnimation;
import com.jiubang.shell.animation.DragAnimation;
import com.jiubang.shell.animation.DragAnimation.DragAnimationInfo;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.appdrawer.controler.IconViewController;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.dock.GLDock;
import com.jiubang.shell.dock.GLDock.GLDockDragListener;
import com.jiubang.shell.dock.business.DockOnDropBaseHandler;
import com.jiubang.shell.dock.business.DockOnDropDockHandler;
import com.jiubang.shell.dock.business.DockOnDropFolderGridHandler;
import com.jiubang.shell.dock.business.DockOnDropOutSideHandler;
import com.jiubang.shell.dock.business.DockQuickActionMenuBusiness;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.drag.DropTarget;
import com.jiubang.shell.effect.EffectController.EffectListener;
import com.jiubang.shell.effect.IconCircleEffect;
import com.jiubang.shell.folder.BaseFolderIcon;
import com.jiubang.shell.folder.GLAppFolder;
import com.jiubang.shell.folder.GLDockFolderGridVIew;
import com.jiubang.shell.folder.GLDockFolderIcon;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.popupwindow.component.actionmenu.QuickActionMenuHandler;
import com.jiubang.shell.preview.GLSense;
import com.jiubang.shell.utils.GaussianBlurEffectUtils;

/**
 * @author dingzijian
 * 
 */
//CHECKSTYLE:OFF
public class GLDockLineLayout extends AbsGLLineLayout
		implements
			OnLongClickListener,
			OnClickListener,
			GLDockDragListener,
			BackgroundAnimation {

	private DockDragHander mDockDragHander;

	private DragController mDragController;

	private float[] mDragTransInfo = new float[5];

	private int mLongClickBlankIndexInRow; // 长按空白时的索引

	private GLDockLinerLayoutAdapter mAdapter;

	private boolean mIsFolderAnimationBegin = false;

	//	private int mClickViewIndex = -1;

	private DockLogicControler mDockLogicControler;
	
	private BaseFolderIcon<?> mDeletedFolderIcon = null;
	
	//	public int getLongClickViewIndex() {
	//		return mClickViewIndex;
	//	}

	public GLDockLineLayout(Context context, DragController dragController) {
		super(context);
		mDockDragHander = new DockDragHander();
		mDragController = dragController;
		setOnLongClickListener(this);
		mDockLogicControler = DockLogicControler.getInstance();
	}

	public IconView findViewByIndex(int index) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			IconView view = (IconView) getChildAt(i);
			Object obj = view.getTag(R.integer.dock_index);
			if (obj != null && obj instanceof Integer) {
				if ((Integer) obj == index) {
					return view;
				}
			}
		}
		return null;
	}

	public Rect getDockRect() {
//		int xOffset = 0;
//		int yOffset = 0;
//		if (Machine.IS_SDK_ABOVE_KITKAT) {
//			if (DrawUtils.getNavBarLocation() == DrawUtils.NAVBAR_LOCATION_BOTTOM) {
//				yOffset = DrawUtils.getNavBarHeight();
//			} else {
//				xOffset = DrawUtils.getNavBarWidth();
//			}
//		}
		GLView parent = (GLView) getGLParent();
//		if (GoLauncherActivityProxy.isPortait()) {
		return new Rect(parent.getLeft(), parent.getTop(), parent.getRight(), parent.getBottom());
//		} else {
//			return new Rect(ShellAdmin.sShellManager.getShell().getContainer().getWidth() - xOffset
//					- DockUtil.getBgHeight(), 0, ShellAdmin.sShellManager.getShell().getContainer()
//					.getWidth(), ShellAdmin.sShellManager.getShell().getContainer().getHeight());
//		}
	}

	@Override
	public boolean onLongClick(GLView v) {
		if (!isVisible()) {
			return true;
		}
		if (SettingProxy.getScreenSettingInfo().mLockScreen) {
			LockScreenHandler.showLockScreenNotification(ShellAdmin.sShellManager.getActivity());
			return true;
		}
		IconViewController.getInstance().removeIconNewFlag(v);
		if (v instanceof IconView) {

			//处理大屏幕手机的需求
			//			if (mIsBigScreenPhone) {
			//				ret = longClickBlankForBigScreen(v);
			//				if (ret) {
			//					return true;
			//				}
			//			}

			IconView<?> dockIconView = (IconView<?>) v;
			try {
				//检查是否显示自定义手势提示
				GuideControler guideCloudView = GuideControler.getInstance(mContext);
				guideCloudView.showDockGesture();
				setDockViewTag(dockIconView);
				/*mClickViewIndex = */mDockDragHander.mDragIndex = mDockDragHander.mDragInitIndex = ((DockItemInfo) dockIconView
						.getInfo()).getmIndexInRow();
				DragAnimationInfo dragAnimationInfo = new DragAnimationInfo(true,
						DragController.DRAG_ICON_SCALE, false, DragAnimation.DURATION_200, null);
				//				mIsFromDockDrag = true;
				mDragController.startDrag(dockIconView, (DragSource) (getGLParent().getGLParent()),
						dockIconView.getInfo(), DragController.DRAG_ACTION_MOVE, mDragTransInfo,
						dragAnimationInfo);
				mDockDragHander.setOperaterLayout(this);
				mDockDragHander.setExtrusionAnimation(false);
				return true;
			} catch (Throwable e) {
				// 有异常，不执行显示对话框操作
				e.printStackTrace();
			}
		}

		if (v instanceof AbsGLLineLayout) {
			return longClickBlank(v);
		}
		return false;

	}

	private void setDockViewTag(IconView<?> v) {
		DockItemInfo dockItemInfo = (DockItemInfo) v.getInfo();
		ItemInfo itemInfo = dockItemInfo.mItemInfo;
		v.setTag(itemInfo);
	}

	// 点击空白处，弹出添加应用的窗口
	private boolean longClickBlank(GLView longClickBlank) {

		//			mLongClickBlankRow = getLineLayoutContainer().getCurLine();
		mLongClickBlankIndexInRow = ((GLDockLineLayoutContainer) getGLParent()).getClickBlankIndex();
		//
		//			AbsGLLineLayout curLineLayout = (AbsGLLineLayout) getLineLayoutContainer().getChildAt(
		//					mLongClickBlankRow);
		final int count = /*curLineLayout.*/getChildCount();
		// 准备DOCK长按图标添加层
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
				IDiyFrameIds.DOCK_ADD_ICON_FRAME, null, null);
		// 初始化添加层数据
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK_ADD_ICON_FRAME,
				IDockMsgId.DOCK_ADD_ICON_INIT, mLongClickBlankIndexInRow, count, null);
		return true;
	}

	// 点击空白处，弹出添加应用的窗口
	//		private boolean longClickBlankForBigScreen(GLView longClickBlank) {
	//
	//			mLongClickBlankRow = getLineLayoutContainer().getCurLine();
	//			mLongClickBlankIndexInRow = getClickBlankIndexForBigScreen(longClickBlank);
	//			if (mLongClickBlankIndexInRow < 0) {
	//				return false;
	//			}
	//			AbsGLLineLayout curLineLayout = (AbsGLLineLayout) getLineLayoutContainer().getChildAt(
	//					mLongClickBlankRow);
	//			final int count = curLineLayout.getChildCount();
	//			// 准备DOCK长按图标添加层
	//			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
	//					IDiyFrameIds.DOCK_ADD_ICON_FRAME, null, null);
	//
	//			// 初始化添加层数据
	//			MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK_ADD_ICON_FRAME,
	//					IDiyMsgIds.DOCK_ADD_ICON_INIT, mLongClickBlankIndexInRow, count, null);
	//			return true;
	//		}

	//		private int getClickBlankIndexForBigScreen(GLView longClickBlank) {
	//			Point downPoint = getLineLayoutContainer().getDownPoint();
	//			AbsGLLineLayout curLineLayout = getLineLayoutContainer().getCurLineLayout();
	//			int index = -1;
	//			if (downPoint != null && curLineLayout != null) {
	//				int bitmap_size = DockUtil.getIconSize(curLineLayout.getChildCount());
	//				int insidePaddingPortrait = (longClickBlank.getWidth() - bitmap_size) / 2;
	//				int insidePaddingLand = (longClickBlank.getHeight() - bitmap_size) / 2;
	//				int areaLeft = longClickBlank.getLeft();
	//				int areaRight = longClickBlank.getRight();
	//				int areaTop = longClickBlank.getLeft();
	//				int areaBottom = longClickBlank.getRight();
	//				if (GoLauncherActivityProxy.isPortait()) {
	//					if (areaLeft < downPoint.x && downPoint.x < (areaLeft + insidePaddingPortrait)) {
	//						index = (Integer) longClickBlank.getTag(R.integer.dock_index);
	//					} else if (areaRight > downPoint.x
	//							&& downPoint.x > (areaRight - insidePaddingPortrait)) {
	//						index = (Integer) longClickBlank.getTag(R.integer.dock_index) + 1;
	//					}
	//				} else {
	//					if (areaTop < downPoint.y && downPoint.y < (areaTop + insidePaddingLand)) {
	//						index = (Integer) longClickBlank.getTag(R.integer.dock_index);
	//					} else if (areaBottom > downPoint.y
	//							&& downPoint.y > (areaBottom - insidePaddingLand)) {
	//						index = (Integer) longClickBlank.getTag(R.integer.dock_index) + 1;
	//					}
	//				}
	//			}
	//			return index;
	//		}

	private void startDockApp(final IconView<?> icon, final DockItemInfo info) {
		if (info.mItemInfo instanceof ShortCutInfo) {
			Rect rect = new Rect();
			icon.getGlobalVisibleRect(rect);
			final ArrayList<Object> posArrayList = new ArrayList<Object>();
			posArrayList.add(rect);
			post(new Runnable() {
				@Override
				public void run() {

					AppItemInfo appInfo = info.mItemInfo.getRelativeItemInfo();

					if (info.mItemInfo instanceof ShortCutInfo) {

						Intent intent = (null != appInfo && null != appInfo.mIntent)
								? appInfo.mIntent
								: ((ShortCutInfo) info.mItemInfo).mIntent;

						intent = DockUtil.filterDockBrowserIntent(getContext(),
								info.mItemInfo.mItemType, intent);
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								ICommonMsgId.START_ACTIVITY, -1, intent, posArrayList);
					}
				}
			});
		}
	}

	@Override
	public void onClick(GLView v) {
		if (v instanceof IconView) {
			final IconView<?> view = (IconView<?>) v;
			final DockItemInfo info = (DockItemInfo) view.getInfo();
			if (null != info) {
				if (view instanceof GLDockFolderIcon) {
					((IconView<?>) view).startClickEffect(new EffectListener() {

						@Override
						public void onEffectStart(Object object) {
						}

						@Override
						public void onEffectComplete(Object object) {
							((BaseFolderIcon<?>) view).openFolder();
						}
					}, IconCircleEffect.ANIM_DURATION_CLICK, false);
				} else {
					if (DockUtil.isDockAppdrawer(info.mItemInfo.getRelativeItemInfo().mIntent)) {
						startDockApp(view, info);
					} else if (DockUtil.isBrowserApp(info)) {
						PreferencesManager ps = new PreferencesManager(mContext);
						int openCount = ps
								.getInt(IPreferencesIds.PREFERENCES_OPEN_BROWSER_COUNT, 0);
						int dockOpenCount = ps
                                .getInt(IPreferencesIds.PREFERENCES_OPEN_DOCK_BROWSER_COUNT, 0);
						if (40 > openCount && Machine.isNetworkOK(mContext)) {
							ps.putInt(IPreferencesIds.PREFERENCES_OPEN_BROWSER_COUNT, ++openCount);
//							ps.putInt(IPreferencesIds.PREFERENCES_OPEN_DOCK_BROWSER_COUNT, ++dockOpenCount);
							ps.commit();
							AppInvoker.sIsClickFromDockBrowser = true;
						}
						
						boolean isNeedDisplay = ps.getBoolean(IPreferencesIds.PREFERENCES_HAD_OPEN_BROWSER, true);
						if (isNeedDisplay) {
						    ps.putInt(IPreferencesIds.PREFERENCES_OPEN_DOCK_BROWSER_COUNT, ++dockOpenCount);
						    ps.commit();
                            AppInvoker.sIsClickFromDockBrowser = true;
						}
						
						//CN包需要使用新的记录，为不影响200包，增加新的sp字段
						if (!Statistics.is200ChannelUid(mContext)) {
							int openCountforCN = ps.getInt(IPreferencesIds.PREFERENCES_OPEN_BROWSER_COUNT_CN, 0);
							if (40 > openCountforCN && Machine.isNetworkOK(mContext)) {
								ps.putInt(IPreferencesIds.PREFERENCES_OPEN_BROWSER_COUNT_CN, ++openCountforCN);
								if (openCountforCN == 2 || openCountforCN == 20 || openCountforCN == 40) {
									ps.putBoolean(IPreferencesIds.PREFERENCES_BROWSER_ADVERT_NEED_SHOW_CN,
											true);
								}
								ps.commit();
								AppInvoker.sIsClickFromDockBrowser = true;
							}
						}
						GuiThemeStatistics.guiStaticData(51, null, "browser", -1, "-1", "-1", "-1",
								"-1");

						view.startClickEffect(new EffectListener() {

							@Override
							public void onEffectComplete(Object callBackFlag) {
								startDockApp(view, info);
							}

							@Override
							public void onEffectStart(Object callBackFlag) {
							}
						}, IconCircleEffect.ANIM_DURATION_CLICK, false);

					} else {
						view.startClickEffect(new EffectListener() {

							@Override
							public void onEffectComplete(Object callBackFlag) {
								startDockApp(view, info);
							}

							@Override
							public void onEffectStart(Object callBackFlag) {
							}
						}, IconCircleEffect.ANIM_DURATION_CLICK, false);
					}
				}
			}
		}
	}

	@Override
	public boolean onDrop(DragSource source, final int x, final int y, int xOffset, int yOffset,
			final DragView dragView, Object dragInfo, final DropAnimationInfo resetInfo) {
		//过滤无效类型
		if (filterDragViewType(dragView.getDragViewType())) {
			return false;
		}
		DockOnDropBaseHandler onDropHandler = null;
		if (source instanceof GLDock) {
			onDropHandler = new DockOnDropDockHandler(source, x, y, xOffset, yOffset, dragView,
					dragInfo, resetInfo, mDockDragHander, this);
		} else if (source instanceof GLDockFolderGridVIew) {
			onDropHandler = new DockOnDropFolderGridHandler(source, x, y, xOffset, yOffset,
					dragView, dragInfo, resetInfo, mDockDragHander, this);
		} else {
			onDropHandler = new DockOnDropOutSideHandler(source, x, y, xOffset, yOffset, dragView,
					dragInfo, resetInfo, mDockDragHander, this);
		}
		boolean ret = onDropHandler.handleOnDrop();
//		if (mDockDragHander.mDragResult != DockDragHander.DRAG_RESULT_ADD_IN_FLODER) {
//			GLAppFolder.getInstance().batchStartIconEditEndAnimation();
//		}
		return ret;
	}

	/**
	 * <br>功能简述: 挤压动画后，更新index
	 * <br>功能详细描述: 仅仅按照各自的index进行数列排序，不改变原先的index
	 * <br>注意:
	 */
	public synchronized void updateIndexAfterExtrusion() {
//		if (modifyIndexInRowWithTag()) {
		//仅仅只做数列排序, 后期进行代码整理，验证通过后，此步骤可以注释掉
			mAdapter.modifyData(mLineID);
//		}
	}

	@Deprecated
	/**
	 * <br>功能简述: 根据Tag中保持的中间变量index用来最后更新排序
	 * <br>功能详细描述: 该方法已经被弃用，容易造成各种难以预测的脏数据
	 * <br>注意:
	 * @return
	 */
	private synchronized boolean modifyIndexInRowWithTag() {
		int size = getChildCount();
		boolean ret = false;
		int rowId = 0;
		for (int i = 0; i < size; i++) {
			IconView dockIconView = (IconView) getChildAt(i);
			int index = (Integer) dockIconView.getTag(R.integer.dock_index);
			DockItemInfo dockItemInfo = (DockItemInfo) dockIconView.getInfo();
			if (index != dockItemInfo.getmIndexInRow()) {
				DockLogicControler.getInstance().modifyShortcutItemIndex(dockItemInfo, index);
				ret = true;
				rowId = dockItemInfo.getmRowId();
			}
		}
		if (ret) {
			DockLogicControler.getInstance().checkDirtyDataAfterModifyIndex(rowId, true);
		}
		return ret;
	}

	public synchronized void updateIndexAfterFolderAnimate(final GLView tempView) {
		post(new Runnable() {
			@Override
			public void run() {
				if (tempView != null) {
					removeViewInLayout(tempView);
				}
				modifyIndexInRowWithView();
				mAdapter.modifyData(mLineID);
			}
		});
	}

	private synchronized void modifyIndexInRowWithView() {
		int size = getChildCount();
		for (int i = 0; i < size; i++) {
			IconView dockIconView = (IconView) getChildAt(i);
			dockIconView.setTag(R.integer.dock_index, i);
			DockItemInfo oldItemInfo = (DockItemInfo) dockIconView.getInfo();
			DockLogicControler.getInstance().modifyShortcutItemIndex(oldItemInfo, i);
		}
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		if (isFolderOpened() || filterDragViewType(dragView.getDragViewType())) {
			return;
		}
		QuickActionMenuHandler.getInstance().onDragEnter(source, x, y, xOffset, yOffset, dragView,
				dragInfo);
		if (source instanceof GLDock) {
			return;
		}
		mDockDragHander.setOperaterLayout(this);
		mDockDragHander.doDragEnter(source, mDockDragHander.mDragIndex, x, y, dragView, dragInfo);
	}

	private boolean isFolderOpened() {
		if (GLAppFolder.getInstance().isFolderOpened()) {
			mDockDragHander.setIsInsert(true);
			return true;
		}
		return false;
	}

	private boolean filterDragViewType(int type) {
		switch (type) {
			case DragView.DRAGVIEW_TYPE_SCREEN_WIDGET :

			case DragView.DRAGVIEW_TYPE_SCREEN_FAVORITE_ICON :

				return true;

			default :
				return false;
		}
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		if (isFolderOpened() || filterDragViewType(dragView.getDragViewType())
				|| mDockDragHander.isExtrusionAnimation() /*挤压动画正在进行*/) {
			return;
		}
		//TODO 这种类似的flag，后期考虑移除掉
		//		mNeedExtrusionAnimate = false;

		mDockDragHander.doDragOver(source, mDockDragHander.mDragIndex, x, y, xOffset, yOffset,
				dragView, null);
	}

	public void cleanUpAllViews() {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			GLView glView = getChildAt(i);
			if (glView instanceof GLDockIconView) {
				((GLDockIconView) glView).onIconRemoved();
//				glView.cleanup();
			}
		}
	}
	@Override
	public void onDragExit(DragSource source, DropTarget nextTarget, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		if (filterDragViewType(dragView.getDragViewType())) {
//			Log.i("dzj", "1");
			return;
		}
		DockQuickActionMenuBusiness actionMenuBusiness = new DockQuickActionMenuBusiness();
		actionMenuBusiness.hideQuickActionMenu(false);
		mDockDragHander.clearMoveToScreenAnim();
//		Log.i("dzj", "nextTarget----> " + nextTarget);
		if (nextTarget instanceof GLSense) {
			if (!mIsFolderAnimationBegin) {
				mDockDragHander.hideFolder();
			}
			mDockDragHander.doDragExit(source, mDockDragHander.mDragIndex);
			return;
		}
		// 加上“nextTarget instanceof GLDock”判断条件，覆盖从Dock条拖拽一个icon到边角的极端情况
		if (getDockRect().contains(x, y) || x <= 0 || y <= 0 || nextTarget instanceof GLDock) {
			if (!mIsFolderAnimationBegin) {
				mDockDragHander.hideFolder();
			}
//			Log.i("dzj", "rect = " + getDockRect().toString() + "    x = " + x + "   y = " + y);
			return;
		} else {
			if (!mIsFolderAnimationBegin) {
				mDockDragHander.hideFolder();
			}
			mDockDragHander.doDragExit(source, mDockDragHander.mDragIndex);
		}
	}

	@Override
	public void onDragMove(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {

	}

	@Override
	public void onDropCompleted(DropTarget target, Object dragInfo, boolean success,
			DropAnimationInfo resetInfo) {
		DockOnDropBaseHandler baseHandler = new DockOnDropBaseHandler(dragInfo, resetInfo,
				mDockDragHander, this);
		baseHandler.handleOnDropComplete(target, success);
	}

	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		QuickActionMenuHandler.getInstance().onDragStart(source, info, dragAction);

	}

	public void setOperaterLayout() {
		mDockDragHander.setOperaterLayout(this);
	}

	@Override
	public void onDragEnd() {
		QuickActionMenuHandler.getInstance().onDragEnd();
		post(new Runnable() {
			@Override
			public void run() {
				mDockDragHander.clearMoveToScreenAnim();
				mDockDragHander.reset();
			}
		});
	}

	public void clearChilderenAnimation() {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).clearAnimation();
		}
		clearAnimation();
	}

	public void setGLDockLinerLayoutAdapter(GLDockLinerLayoutAdapter adapter) {
		this.mAdapter = adapter;
	}
	/**
	 * 更新某一行图标的图片size
	 * 
	 * @param rowid
	 */
	public void updateIconsSizeAndRequestLayout() {
		clearChilderenAnimation();
		GLDockLineLayoutContainer container = (GLDockLineLayoutContainer) getGLParent();
		container.requestLayoutDockIcons();
		updateLineLayoutIconsSize();
	}
	
	/**
	 * <br>功能简述:外部手动添加一个新的App或者Shortcut时候，更新indexTag
	 * <br>功能详细描述: 
	 * <br>注意:
	 * @param insertIndex
	 */
	private void updateIndexTagStatic(final int insertIndex) {
		int size = getChildCount();
		for (int i = 0; i < size; i++) {
			IconView dockIconView = (IconView) getChildAt(i);
			int newIndex = i < insertIndex ? i : i + 1;
			dockIconView.setTag(R.integer.dock_index, newIndex);
		}
	}
	
	public void addApplication(AppItemInfo appItemInfo, boolean appearAnim) {
		int iconsize = getChildCount() == DockUtil.ICON_COUNT_IN_A_ROW - 1 ? DockUtil
				.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW) : DockUtil.getIconSize(1);
		//ADT-15867 重新获取当前行号，可能弹出添加窗口后dock页有切换
		GLDockLineLayoutContainer glDockLIneLayoutContainer = (GLDockLineLayoutContainer) getGLParent();
		if (glDockLIneLayoutContainer.isRowChangedWhileAddIconFrameIsTop()) {
			mLongClickBlankIndexInRow = glDockLIneLayoutContainer.getClickBlankIndex();
			glDockLIneLayoutContainer.setRowChangedWhileAddIconFrameIsTop(false);
		}
		DockItemInfo dockItemInfo = createDockItemInfo(appItemInfo, mLineID,
				mLongClickBlankIndexInRow, iconsize);

		if (dockItemInfo != null) {
			updateIndexTagStatic(mLongClickBlankIndexInRow);
			refShortcutItem(dockItemInfo, getChildCount(), mLineID, false, null, true, true);
		}
		if (appearAnim) {
			playAppearAnim(mLongClickBlankIndexInRow);
		} else {
			mLongClickBlankIndexInRow++;
			// 通知dock添加图标层添加图标完成，可以继续点击添加下一个图标
			sendDockAddFrameAddFinish();
		}
	}
	protected DockItemInfo createDockItemInfo(ShortCutInfo info, int rowid, int indexinrow,
			int iconsize) {
		if (null == info || null == info.mIntent || rowid < 0 || rowid >= DockUtil.TOTAL_ROWS
				|| indexinrow < 0 || indexinrow >= DockUtil.ICON_COUNT_IN_A_ROW) {
			// M9,返回的intent为null
			return null;
		}

		String title = null;
		Intent intent = info.mIntent;
		if (null != info.mTitle) {
			title = info.mTitle.toString();
		}
		BitmapDrawable icon = null;
		if (null != info.mIcon && info.mIcon instanceof BitmapDrawable) {
			icon = (BitmapDrawable) info.mIcon;
		}

		DockItemInfo dockItemInfo = null;
		SysShortCutControler sysShortCutControler = AppCore.getInstance().getSysShortCutControler();
		if (sysShortCutControler != null) {
			// 新增
			sysShortCutControler.addSysShortCut(intent, title, icon);

			dockItemInfo = new DockItemInfo(IItemType.ITEM_TYPE_SHORTCUT, iconsize);
			dockItemInfo.setmRowId(rowid);
			dockItemInfo.setmIndexInRow(indexinrow);
			info.mInScreenId = System.currentTimeMillis();
			mDockLogicControler.prepareItemInfo(info);
			dockItemInfo.setInfo(info);
		}

		return dockItemInfo;
	}

	public void addShortcut(ShortCutInfo shortCutInfo, boolean appearAnim) {
		int iconsize = getChildCount() == DockUtil.ICON_COUNT_IN_A_ROW - 1 ? DockUtil
				.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW) : DockUtil.getIconSize(1);

		//ADT-15867 重新获取当前行号，可能弹出添加窗口后dock页有切换
		GLDockLineLayoutContainer glDockLIneLayoutContainer = (GLDockLineLayoutContainer) getGLParent();
		if (glDockLIneLayoutContainer.isRowChangedWhileAddIconFrameIsTop()) {
			mLongClickBlankIndexInRow = glDockLIneLayoutContainer.getClickBlankIndex();
			glDockLIneLayoutContainer.setRowChangedWhileAddIconFrameIsTop(false);
		}
		DockItemInfo dockInfo = createDockItemInfo(shortCutInfo, mLineID,
				mLongClickBlankIndexInRow, iconsize);
		if (dockInfo != null) {
			updateIndexTagStatic(mLongClickBlankIndexInRow);
			refShortcutItem(dockInfo, getChildCount(), mLineID, false, null, true, true);
		}
		if (appearAnim) {
			playAppearAnim(mLongClickBlankIndexInRow);
		} else {
			mLongClickBlankIndexInRow++;
			// 通知dock添加图标层添加图标完成，可以继续点击添加下一个图标
			sendDockAddFrameAddFinish();
		}
	}
	/***
	 * 刷UI和数据库
	 * 
	 * @param dockItemInfo
	 * @param oldcount
	 * @param rowId
	 * @param isInsert
	 * @param tag
	 */
	public void refShortcutItem(DockItemInfo dockItemInfo, int oldcount, int rowId,
			boolean isInsert, UserFolderInfo tag, boolean isRelayout, boolean isUpdateSize) {
		// 先add UI,再add DB
		//			if (refreshUiAdd(dockItemInfo, isRelayout)) {
		//				ArrayList<IconView<?>> list = getRowDockIcons(rowId);
		//				if (null != list) {
		boolean insert = insertShortcutItemAndReArrange(dockItemInfo);
		if (isInsert && insert) {
			mDockLogicControler.addDrawerFolderToDock(tag); // 插入文件夹子项
		}
		// 注释掉以下代码，index顺序已经在方法insertShortcutItemAndReArrange中进行重排
//		int count = getChildCount();
//		boolean hasModified = false;
//		for (int i = 0; i < count; i++) {
//			IconView<?> dockIconView = (IconView<?>) getChildAt(i);
//			int index = ((DockItemInfo) dockIconView.getInfo()).getmIndexInRow();
//			if (index != i) {
//				// 可能响应到其他图标的索引更新
//				mDockLogicControler.modifyShortcutItemIndex((DockItemInfo) dockIconView.getInfo(),
//						i);
//				hasModified = true;
//			}
//		}
//		if (hasModified) {
//			DockLogicControler.getInstance().checkDirtyDataAfterModifyIndex(rowId, true);
//		}
		if (isRelayout) {
//			modifyIndexInRowWithTag();
			updateIconsSizeAndRequestLayout();
		}
		//				}
		//			}
		// UI同步数据库校验
		//			verifyData();
	}
	/***
	 * 插入
	 * 
	 * @param info
	 * @return
	 */
	public boolean insertShortcutItemAndReArrange(DockItemInfo info) {
		boolean ret = false;
		// 索引判断
		int index = info.getmIndexInRow();
		if (0 <= index && index < DockUtil.ICON_COUNT_IN_A_ROW) {
			ret = mDockLogicControler.insertShortcutItemAndReArrange(info);
		}
		return ret;
	}
	/**
	 * <br>
	 * 功能简述:通知dock添加图标层添加图标完成，可以继续点击添加下一个图标 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	public void sendDockAddFrameAddFinish() {
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK_ADD_ICON_FRAME,
				IDockMsgId.DOCK_ADD_ICON_ADD_FINISH, -1, 0, null);
	}

	protected DockItemInfo createDockItemInfo(AppItemInfo appItemInfo, int rowid, int indexinrow,
			int iconsize) {
		if (appItemInfo == null || rowid < 0 || rowid >= DockUtil.TOTAL_ROWS || indexinrow < 0
				|| indexinrow >= DockUtil.ICON_COUNT_IN_A_ROW) {
			return null;
		}

		DockItemInfo dockItemInfo = new DockItemInfo(IItemType.ITEM_TYPE_APPLICATION, iconsize);
		dockItemInfo.setmRowId(rowid);
		dockItemInfo.setmIndexInRow(indexinrow);

		ShortCutInfo shortCutInfo_application = new ShortCutInfo();
		shortCutInfo_application.mItemType = IItemType.ITEM_TYPE_APPLICATION;
		shortCutInfo_application.mIntent = appItemInfo.mIntent;
		shortCutInfo_application.mFeatureIconType = ImagePreviewResultType.TYPE_DEFAULT;
		shortCutInfo_application.mInScreenId = System.currentTimeMillis();
		DockLogicControler.getInstance().prepareItemInfo(shortCutInfo_application);

		dockItemInfo.setInfo(shortCutInfo_application);

		return dockItemInfo;
	}

	public void showProtectLayer() {
		ShellAdmin.sShellManager.getShell().show(IViewId.PROTECTED_LAYER, false);
	}

	public void hideProtectLayer() {
		ShellAdmin.sShellManager.getShell().hide(IViewId.PROTECTED_LAYER, false);
	}
	/**
	 * 更新某一行图标的图片size
	 * 
	 * @param rowid
	 */
	public void updateLineLayoutIconsSize() {
		int size = getChildCount();
		int newsize = DockUtil.getIconSize(size);
		for (int i = 0; i < size; i++) {
			IconView<?> iconView = (IconView<?>) getChildAt(i);
			DockItemInfo info = (DockItemInfo) iconView.getInfo();
			info.setBmpSize(newsize);
			//			if (info.mItemInfo instanceof UserFolderInfo) {
			//				// 因为folder图标刷新是异步，会出现闪一下的情况，所以这里先同步seticon
			//				info.setIcon(info.getIcon());
			//			}
			// 重设icon size大小
			if (iconView instanceof GLDockFolderIcon) {
				iconView.setIconSize(newsize);
			}
			DockLogicControler.getInstance().updateDockIcon(info);
		}
	}

	public void setIsFolderAnimationBegin(boolean mIsFolderAnimationBegin) {
		this.mIsFolderAnimationBegin = mIsFolderAnimationBegin;
	}
	// 归位动画

	public GLDockLinerLayoutAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public void onAddApplictionOrShortCut(Object object) {
		if (object instanceof AppItemInfo) {
			addApplication((AppItemInfo) object, false);
		} else if (object instanceof ShortCutInfo) {
			addShortcut((ShortCutInfo) object, false);
		}
	}

	@Override
	public void onDockFolderDropComplete(Object sender, Object target, Object dragInfo, boolean success,
			DropAnimationInfo resetInfo, Object dragView) {
		DockOnDropBaseHandler baseHandler = new DockOnDropBaseHandler(dragInfo, resetInfo,
				mDockDragHander, this);
		baseHandler.onFolderDropComplete(sender, target, success);
//		GLAppFolder.getInstance().batchStartIconEditEndAnimation();
	}
	/***
	 * 要求删除文件夹内Item
	 * 
	 * @param objects
	 * @param object
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public boolean deleteFolderItem(Object sender, List objects, Object object, int rowid) {
		GLDockFolderIcon clickView;
		// ADT-16967 在dock栏操作时，桌面出现FC 
		if (sender != null && sender instanceof GLDockFolderGridVIew) {
			clickView = ((GLDockFolderGridVIew) sender).getFolderIcon();
		} else {
			clickView = (GLDockFolderIcon) GLAppFolder.getInstance().getCurFolderIcon();
		}
		ItemInfo itemInfo = (ItemInfo) object;
		//		if (itemInfo == null || clickView == null) {
		//			return false;
		//		}
		DockItemInfo dockItemInfo = (DockItemInfo) clickView.getInfo();
		if (null != dockItemInfo && dockItemInfo.mItemInfo instanceof UserFolderInfo) {
			UserFolderInfo folderInfo = (UserFolderInfo) dockItemInfo.mItemInfo;
			folderInfo.remove(itemInfo.mInScreenId);
			itemInfo.unRegisterObserver(dockItemInfo);
			Long deleteid = itemInfo.mInScreenId;
			if (null != objects && !objects.isEmpty()) {
				deleteid = (Long) objects.get(0);
			}
			GLAppFolderController.getInstance().removeAppFromScreenFolder(deleteid,
					folderInfo.mInScreenId);
			boolean ret = deleteFolderOrNot(clickView, true, rowid);
			// 删除一项后，刷新文件夹图标
			if (ret) {
				mDockLogicControler.updateFolderIconAsync(dockItemInfo, false);
			}
			return ret;
		}
		return false;
	}
	/**
	 * 判断是否删除文件夹
	 * 
	 * @param userFolderInfo
	 * @param deleteOne
	 *            剩一个图标时是否删除文件夹
	 * @return
	 */
	private boolean deleteFolderOrNot(IconView dockIconView, boolean deleteOne, int rowid) {
		if (dockIconView == null) {
			return false;
		}
		DockItemInfo dockItemInfo = (DockItemInfo) dockIconView.getInfo();
		if (null == dockIconView.getInfo() || null == dockItemInfo.mItemInfo
				|| !(dockItemInfo.mItemInfo instanceof UserFolderInfo)) {
			return false;
		}

		UserFolderInfo userFolderInfo = (UserFolderInfo) dockItemInfo.mItemInfo;
		boolean ret = false;
		int count = 0;
		synchronized (userFolderInfo) {
			/**
			 * ADT-3723 非必现：桌面文件夹消失，有消失动画 修改方法：对userFolderInfo加锁
			 */
			count = userFolderInfo.getChildCount();
		}
		if (count == 0) {
			BaseFolderIcon<?> folderIcon = (BaseFolderIcon<?>) dockIconView;
			folderIcon.closeFolder(true);
			// delete folder
//			DockItemInfo deleteDockFolderViewInfo = (DockItemInfo) dockIconView.getInfo();
			// mHandler.sendEmptyMessage(DockConstant.HANDLE_ANIMATION_DELETE_FOLDER);
//			Long id = deleteDockFolderViewInfo.mItemInfo.mInScreenId;
//			delDockItem(id);
			mDeletedFolderIcon = folderIcon;
			ret = true;
		} else if (deleteOne && count == 1) {
			((GLDockFolderIcon) dockIconView).closeFolder(true);
			ShortCutInfo shortCutInfo = userFolderInfo.getChildInfo(0);
			// 已经禁用tag中的index来更新缓存及数据库，index更新已经统一做处理，此处把相关无效代码注释掉，消除安全隐患
//			int viewIndex = (Integer) dockIconView.getTag(R.integer.dock_index);
			// delete folder & move item to desktop
			DockItemInfo info = (DockItemInfo) dockIconView.getInfo();
			info.setInfo((FeatureItemInfo) shortCutInfo);
			info.setmRowId(rowid);
//			info.setmIndexInRow(viewIndex);
			mDockLogicControler.changeAppForThreeD(info, userFolderInfo.mInScreenId, shortCutInfo,
					DockUtil.CHANGE_FROM_DELETEFOLER);
			mDockLogicControler.removeDockFolder(userFolderInfo.mInScreenId);
			ret = true;
		} else {
			ret = false;
		}

		return ret;
	}

	private void handleDockFolderDragFlingEvent(Object dragInfo, GLDockFolderIcon folderIcon) {
		boolean isDelete = deleteFolderItem(null, null, dragInfo, mLineID);
		if (isDelete) {
			// Dock文件夹只有一个icon时候，向上快速甩动删除icon，会产生空文件夹， 此处加上空文件夹处理代码
			handleDeletedFolder();
			updateIconsSizeAndRequestLayout();
		}
		folderIcon.closeFolder(true);
		folderIcon.refreshIcon();
	}

	/**
	 * <br>功能简述: 删除dock的一个选项，同时对剩余项进行顺序重排
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param rowId dock条行号
	 * @param id 被删除选项唯一标识
	 * @return
	 */
	public boolean delDockItemAndReArrange(DockItemInfo info) {
		return mDockLogicControler.deleteShortcutItemAndReArrange(info.getmRowId(), info.mItemInfo.mInScreenId);
	}

	@Override
	public void onDragFingDockFolderIcon(ItemInfo dragInfo, GLDockFolderIcon folderIcon) {
		handleDockFolderDragFlingEvent(dragInfo, folderIcon);
	}

	@Override
	public void onDragFingDockIcon(DockItemInfo info, long id) {
		if (delDockItemAndReArrange(info)) {
			if (info.mItemInfo instanceof UserFolderInfo) {
				mDockLogicControler.removeDockFolder(info.mItemInfo.mInScreenId);
			}
			updateIconsSizeAndRequestLayout();
		}
	}

	@Override
	public void onDockFolderIconLessTwo(Object[] objects) {
		boolean isDelete = false;
		if (objects.length > 1 && objects[1] instanceof ShortCutInfo) {
			ShortCutInfo deleteInfo = (ShortCutInfo) objects[1];
			isDelete = deleteFolderItem(null, null, deleteInfo, mLineID);
		} else {
			GLDockFolderIcon clickView = (GLDockFolderIcon) objects[0];
			isDelete = deleteFolderOrNot(clickView, true, mLineID);
		}
		if (isDelete) {
			handleDeletedFolder();
			updateIconsSizeAndRequestLayout();
		}

	}
	private void addDockIconExtrusion() {
		//ADT-15867 重新获取当前行号，可能弹出添加窗口后dock页有切换
		GLDockLineLayoutContainer glDockLIneLayoutContainer = (GLDockLineLayoutContainer) getGLParent();
		if (glDockLIneLayoutContainer.isRowChangedWhileAddIconFrameIsTop()) {
			mLongClickBlankIndexInRow = glDockLIneLayoutContainer.getClickBlankIndex();
			glDockLIneLayoutContainer.setRowChangedWhileAddIconFrameIsTop(false);
		}
		int count = getChildCount();
		if (count == 0) {
			return;
		}
		setInitRect(count + 1);
		ArrayList<Position> changeRectList = getInitRectList();
		AnimationTask task = new AnimationTask(false, AnimationTask.PARALLEL);
		for (int i = 0; i < count; i++) {
			IconView view = (IconView<?>) getChildAt(i);
			Animation animation = null;
			Animation animationScale = null;
			AnimationSet animationSet = new AnimationSet(true);
			if (count == 4) {
				animationScale = new ScaleAnimation(1f, mDockDragHander.getZoomOutProportion(), 1f,
						mDockDragHander.getZoomOutProportion(), Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				animationScale.setDuration(100);
				animationSet.addAnimation(animationScale);
			}
			if (view != null && view.isVisible()) {
				Rect rect;
				if (i >= mLongClickBlankIndexInRow) {
					rect = changeRectList.get(i + 1).outRect;
					view.setTag(R.integer.dock_index, i + 1);
				} else {
					rect = changeRectList.get(i).outRect;
					view.setTag(R.integer.dock_index, i);
				}
				if (GoLauncherActivityProxy.isPortait()) {
					int rectCenter = rect.left + (rect.right - rect.left) / 2;
					int viewCenter = view.getLeft() + (view.getRight() - view.getLeft()) / 2;
					animation = new TranslateAnimation(0, rectCenter - viewCenter, 0, 0);
				} else {
					int rectCenter = rect.top + (rect.bottom - rect.top) / 2;
					int viewCenter = view.getTop() + (view.getBottom() - view.getTop()) / 2;
					animation = new TranslateAnimation(0, 0, 0, rectCenter - viewCenter);
				}
				animationSet.addAnimation(animation);
				animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
				animationSet.setDuration(100);
				animationSet.setFillAfter(true);
				task.addAnimation(view, animationSet, null);
			}
		}
		GLAnimationManager.startAnimation(task);
	}
	@Override
	public void onDockAppDeleted(long id) {
		DockItemInfo info = mDockLogicControler.getShortcutItem(id);
		if (info != null) {
			if (delDockItemAndReArrange(info)) {
				updateIconsSizeAndRequestLayout();
			}
		}
	}

	@Override
	public void onAddAppFromLongClickBlank(int index, Object info) {
		if (info instanceof AppItemInfo) {
			addApplication((AppItemInfo) info, true);
		} else if (info instanceof ShortCutInfo) {
			addShortcut((ShortCutInfo) info, true);
		}
	}

	@Override
	public void hideBgAnimation(int type, GLView glView, Object... params) {
		switch (type) {
			case BackgroundAnimation.ANIMATION_TYPE_ALPHA :
				float from = (Float) params[0];
				float to = (Float) params[1];
				long duration = (Long) params[2];
				AnimationTask task = new AnimationTask(false, AnimationTask.PARALLEL);
				for (int i = 0; i < getChildCount(); i++) {
					IconView<?> iconView = (IconView<?>) getChildAt(i);
					if (iconView == glView) {
						continue;
					}
					Animation animation = new AlphaAnimation(from, to);
					animation.setFillAfter(true);
					animation.setDuration(duration);
					task.addAnimation(iconView, animation, null);
				}
				GLAnimationManager.startAnimation(task);
				break;
			case BackgroundAnimation.ANIMATION_TYPE_BLUR :
				GaussianBlurEffectUtils.enableBlurWithZoomOutAnimation(this, null);
				break;
			default :
				break;
		}
		if (type == BackgroundAnimation.ANIMATION_TYPE_ALPHA) {
			
		}

	}

	@Override
	public void showBgAnimation(int type, GLView glView, Object... params) {
		switch (type) {
			case BackgroundAnimation.ANIMATION_TYPE_ALPHA :
				float from = (Float) params[0];
				float to = (Float) params[1];
				long duration = (Long) params[2];
				AnimationTask task = new AnimationTask(false, AnimationTask.PARALLEL);
				for (int i = 0; i < getChildCount(); i++) {
					IconView<?> iconView = (IconView<?>) getChildAt(i);
					if (iconView == glView) {
						continue;
					}
					Animation animation = new AlphaAnimation(from, to);
					animation.setDuration(duration);
					task.addAnimation(iconView, animation, null);
				}
				GLAnimationManager.startAnimation(task);
				break;
			case BackgroundAnimation.ANIMATION_TYPE_BLUR :
				GaussianBlurEffectUtils.disableBlurWithZoomInAnimation(this, null);
				break;
			default :
				break;
		}
	}
	
	public void handleDeletedFolder() {
		if (mDeletedFolderIcon instanceof GLDockFolderIcon) {
			DockItemInfo dockItemInfo = ((GLDockFolderIcon) mDeletedFolderIcon).getInfo();
			mDockLogicControler.removeDockFolder(dockItemInfo.mItemInfo.mInScreenId);
			delDockItemAndReArrange(dockItemInfo);
		}
		mDeletedFolderIcon = null;
	}
	
	@Override
	public void showBgWithoutAnimation(int type, GLView glView, Object... params) {
		switch (type) {
			case BackgroundAnimation.ANIMATION_TYPE_ALPHA :
				for (int i = 0; i < getChildCount(); i++) {
					IconView<?> iconView = (IconView<?>) getChildAt(i);
					if (iconView == glView) {
						continue;
					}
					iconView.setAlpha(255);
				}
				break;
			case BackgroundAnimation.ANIMATION_TYPE_BLUR :
				GaussianBlurEffectUtils.disableBlurWithoutAnimation(this);
			default :
				break;
		}
	}

	@Override
	public GLDockLineLayout getCurDockLineLayout() {
		return this;
	}
	
	/**
	 * <br>功能简述: 播放新增图标出现动画
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param index
	 */
	private void playAppearAnim(int index) {
		int childCount = getChildCount();
		if (index < 0 || index >= childCount) {
			Log.w(VIEW_LOG_TAG, "illegal index " + index + ", childCount " + childCount);
			return;
		}
		GLView animView = getChildAt(index);
		Animation animationScale = new ScaleAnimation(0.0f, 1.0f, 0.0f,
				1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		animationScale.setDuration(200);
		animationScale.setInterpolator(new AccelerateInterpolator());
		animationScale.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mLongClickBlankIndexInRow++;
				// 通知dock添加图标层添加图标完成，可以继续点击添加下一个图标
				sendDockAddFrameAddFinish();
			}

			@Override
			public void onAnimationProcessing(Animation animation, float interpolatedTime) {
				
			}
		});
		animView.startAnimation(animationScale);
	}
}