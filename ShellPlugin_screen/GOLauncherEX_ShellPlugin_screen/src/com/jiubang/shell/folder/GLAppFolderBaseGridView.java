package com.jiubang.shell.folder;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.appfunc.setting.AppFuncAutoFitManager;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogSingleChoice;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.GestureSettingInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderController;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.appdrawer.allapp.actionbar.GLPreviewBar;
import com.jiubang.shell.appdrawer.component.GLExtrusionGridView;
import com.jiubang.shell.common.component.HorScrollableGridViewHandler;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.drag.DropTarget;
import com.jiubang.shell.folder.GLAppFolderMainView.FolderViewAnimationListener;
import com.jiubang.shell.folder.status.FolderStatus;
import com.jiubang.shell.folder.status.FolderStatusManager;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.popupwindow.component.actionmenu.QuickActionMenuHandler;
import com.jiubang.shell.scroller.effector.CoupleScreenEffector;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-3-11]
 * @param <T> ?
 */
public abstract class GLAppFolderBaseGridView<T extends BaseFolderIcon<?>>
		extends
			GLExtrusionGridView implements FolderViewAnimationListener {
	private static final int CLOSE_FOLDER_DELAY = 500;
	protected FolderStatus mStatus;
	protected DragController mDragController;
	protected T mFolderIcon;
	protected Runnable mCloseFolderRunnable;
	protected GLAppFolderController mController;
	private AnimationTask mOpenFolderAnimationTask;
	/**
	 * 排序风格
	 */
	protected static final int SORTTYPE_LETTER = 0;
	protected static final int SORTTYPE_TIMENEAR = 1;
	protected static final int SORTTYPE_TIMEREMOTE = 2;
	/**
	 * 文件夹元素下标如果发生变化，文件夹关闭后就要更新数据库。
	 */
	protected boolean mPositionChange;

	protected boolean mIsDragging;
	
	public GLAppFolderBaseGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GLAppFolderBaseGridView(Context context) {
		super(context);
		init();
	}

	protected void init() {
		mDragController = FolderStatusManager.getInstance().getDragController();
		//		mDragController.addDropTarget(this, getTopViewId());
		//		mDragController.addDragListener(this);
		mController = GLAppFolderController.getInstance();
		mIconOperation.setIsEnableOverlay(false);
		mOpenFolderAnimationTask = new AnimationTask(true, AnimationTask.PARALLEL);
//		mOpenFolderAnimationTask.setBatchAnimationObserver(this, FOLDER_OPEN_ANIMATION);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		mNeedRegetIconRectList = true;
		if (changed) {
			handleRowColumnSetting(false);
			caculateRowsNum(bottom - top);
		}
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	public void setDragController(DragController dragger) {
		mDragController = dragger;
	}
	
	@Override
	protected void handleRowColumnSetting(boolean updateDB) {
		Context context = ShellAdmin.sShellManager.getActivity();
		AppFuncAutoFitManager autoFitManager = AppFuncAutoFitManager.getInstance(context);
		int iconWidth = autoFitManager.getIconWidth();
		if (DrawUtils.sDensity >= 2.0f) {
			int padding = (getWidth() - mNumColumns * iconWidth) / (mNumColumns + 1) / 2;
			if (padding < 12) {
				padding = 12;
			}
			setPadding(padding, getPaddingTop(), padding, getPaddingBottom());
		}
	}
	
	@Override
	public void setTopViewId(int id) {

	}
	@Override
	protected void handleScrollerSetting() {
		if (mScrollableHandler == null) {
			mScrollableHandler = new HorScrollableGridViewHandler(mContext, this,
					CoupleScreenEffector.PLACE_MENU, false, true) {
				@Override
				public void onEnterLeftScrollZone() {
					mIsInScrollZone = true;
				}

				@Override
				public void onEnterRightScrollZone() {
					mIsInScrollZone = true;
				}

				@Override
				public void onEnterTopScrollZone() {
					mIsInScrollZone = true;
				}

				@Override
				public void onEnterBottomScrollZone() {
					mIsInScrollZone = true;
				}

				@Override
				public void onExitScrollZone() {
					mIsInScrollZone = false;
				}

				@Override
				public void onScrollLeft() {
					super.onScrollLeft();
					setPreviousScreenIndex();
				}

				@Override
				public void onScrollRight() {
					super.onScrollRight();
					setNextScreenIndex();
				}

				@Override
				public void onScrollTop() {
					super.onScrollTop();
					setPreviousScreenIndex();
				}

				@Override
				public void onScrollBottom() {
					super.onScrollBottom();
					setNextScreenIndex();
				}

				/**
				 * 计算上一屏的首末下标
				 */
				private void setPreviousScreenIndex() {
					if (mCurrentScreen == 0 && !isCircular()) {
						return;
					}
					int desScreen = mCurrentScreen - 1;
					if (desScreen < 0) {
						desScreen = mTotalScreens - 1;
					}
					int firstRow = desScreen * mNumRows;
					int lastRow = firstRow + mNumRows - 1;
					int firstIndex = getTargetRowFirstIndex(firstRow);
					int lastIndex = getTargetRowLastIndex(lastRow);
					mIconOperation.setIsFling(true);
					mIconOperation.setScreenFirstAndLastIndex(firstIndex, lastIndex);
				}

				/**
				 * 计算下一屏的首末下标
				 */
				private void setNextScreenIndex() {
					if (mCurrentScreen == mTotalScreens && !isCircular()) {
						return;
					}
					int desScreen = mCurrentScreen + 1;
					if (desScreen >= mTotalScreens) {
						desScreen = 0;
					}
					int firstRow = desScreen * mNumRows;
					int lastRow = firstRow + mNumRows - 1;
					int firstIndex = getTargetRowFirstIndex(firstRow);
					int lastIndex = getTargetRowLastIndex(lastRow);
					mIconOperation.setIsFling(true);
					mIconOperation.setScreenFirstAndLastIndex(firstIndex, lastIndex);
				}
			};
		}
	}
	@Override
	public int getTopViewId() {
		return IViewId.APP_FOLDER;
	}

	public void setStatus(FolderStatus status) {
		mStatus = status;
		onGirdStatusChange(status.getGridStatusID());
	}

	public abstract void onGirdStatusChange(int gridStatusId);

	@Override
	public boolean onDropInIconOverlay(Object dragInfo, int targetIndex, int sourceIndex,
			int dragViewCenterX, int dragViewCenterY, int invisitIndex, DragView dragView,
			DropAnimationInfo resetInfo) {
		return false;
	}

	@Override
	public boolean onEnterIconOverlay(int index, Object dragInfo) {

		return false;
	}

	@Override
	public void onExitIconOverlay() {

	}

	public void setFolderIcon(T folderIcon) {
		this.mFolderIcon = folderIcon;
	}

	public T getFolderIcon() {
		return mFolderIcon;
	}
	@SuppressWarnings("unchecked")
	@Override
	public void dataChangeOnMoveStart(Object dragInfo, int targetIndex, int sourceIndex) {
		//		mPositionChange = true;
		//		int count = mAdapter.getCount();
		//		if (targetIndex >= count || sourceIndex >= count) {
		//			return;
		//		}
		//		Object itemInfo = mAdapter.getItem(sourceIndex);
		//		mAdapter.remove(itemInfo);
		//		mAdapter.insert(itemInfo, targetIndex);
		//		mFolderIcon.refreshIcon();
	}

	@Override
	public void dataChangeOnMoveEnd(Object dragInfo, int targetIndex, int sourceIndex) {
		mPositionChange = true;
		int count = mAdapter.getCount();
		if (targetIndex >= count || sourceIndex >= count) {
			return;
		}
		Object itemInfo = mAdapter.getItem(sourceIndex);
		mAdapter.remove(itemInfo);
		mAdapter.insert(itemInfo, targetIndex);
	}

	@Override
	public void dataChangeOnDrop(Object dragInfo, int targetIndex, int sourceIndex) {
		post(new Runnable() {

			@Override
			public void run() {
				mFolderIcon.refreshIcon();

			}
		});
	}
	@Override
	public void onDragExit(DragSource source, DropTarget nextTarget, int x, int y, int xOffset,
			int yOffset, DragView dragView, final Object dragInfo) {
		super.onDragExit(source, nextTarget, x, y, xOffset, yOffset, dragView, dragInfo);
		GLAppFolderGridVIewContainer appFolderGridVIewContainer = (GLAppFolderGridVIewContainer) getGLParent();
		appFolderGridVIewContainer.backgroudAnimation(FolderStatusManager.GRID_NORMAL_STATUS);
		int[] location = new int[2];
		getLocationOnScreen(location);
		if (y >= location[1] + getHeight() || y < location[1]) {
			mCloseFolderRunnable = new Runnable() {

				@Override
				public void run() {
					mFolderIcon.closeFolder(true);
					//					int skipIndex = mAdapter.getPosition(dragInfo);
					//					mFolderIcon.refreshFolderIcon(skipIndex);

					// 召唤垃圾桶出来
					if ((dragInfo instanceof ShortCutInfo && mDragController.isDragging())
							&& !ShellAdmin.sShellManager.getShell().isViewVisible(
									IViewId.DELETE_ZONE)) {
						ShellAdmin.sShellManager.getShell().show(IViewId.DELETE_ZONE, true);
						GLAppFolder.getInstance().batchStartIconEditAnimation();
					}
				}
			};
			mGridViewHandler.postDelayed(mCloseFolderRunnable, CLOSE_FOLDER_DELAY);
		}
	}

	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		mIsDragging = true;
		GLAppFolderGridVIewContainer appFolderGridVIewContainer = (GLAppFolderGridVIewContainer) getGLParent();
		appFolderGridVIewContainer.backgroudAnimation(FolderStatusManager.GRID_EDIT_STATUS);
		if (!(this instanceof GLAppDrawerFolderGridView)) {
			QuickActionMenuHandler.getInstance().onDragStart(source, info, dragAction);
		}
		super.onDragStart(source, info, dragAction);
	}

	@Override
	public void onDragEnd() {
		QuickActionMenuHandler.getInstance().onDragEnd();
		mIsDragging = false;
		super.onDragEnd();
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		mGridViewHandler.removeCallbacks(mCloseFolderRunnable);
		if (!(this instanceof GLAppDrawerFolderGridView)) {
			QuickActionMenuHandler.getInstance().onDragEnter(source, x, y, xOffset, yOffset,
					dragView, dragInfo);
		}
		super.onDragEnter(source, x, y, xOffset, yOffset, dragView, dragInfo);
	}

	@Override
	protected void onVisibilityChanged(GLView changedView, int visibility) {
		if (changedView instanceof GLAppFolderMainView) {
			switch (visibility) {
				case GLView.VISIBLE :
					mStatus.onGridViewShow();
					break;
				case GLView.INVISIBLE :
				case GLView.GONE :
					mStatus.onGridViewHide();
					mScrollableHandler.scrollTo(0, false);
					if (!mIsDragging) {
						mAdapter.cleanUpViewHolder();
						removeAllViewsInLayout();
						mScrollableHandler.clearHolder();
					}
					break;
				default :
					break;
			}
		}
	}

	public abstract void onFolderNameChange(String name);

	public abstract void showSortDialog();

	protected DialogSingleChoice createSortDialog() {
		DialogSingleChoice sortDialog = new DialogSingleChoice(
				ShellAdmin.sShellManager.getActivity());
		sortDialog.show();
		String title = mContext.getString(R.string.dlg_sortChangeTitle);
		sortDialog.setTitle(title);
		final CharSequence[] items = mContext.getResources().getTextArray(
				R.array.folder_select_sort_style);
		sortDialog.setItemData(items, -1, false);
		return sortDialog;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onDropCompleted(DropTarget target, Object dragInfo, boolean success,
			DropAnimationInfo resetInfo) {
		mIsDragging = false;
		if (success && !(target instanceof GLPreviewBar) && target != this) {
			clearDeletedView(dragInfo, true);
		}
		if (!success && isVisible()) {
			resetInfo.setNeedToShowCircle(false);
			mIconOperation.resetDragIcon(resetInfo);
		}
		super.onDropCompleted(target, dragInfo, success, resetInfo);
	}

	public void clearDeletedView(Object info, boolean clearAll) {
		GLView child = mAdapter.removeViewByItem(info);
		if (child != null) {
			int index = mIconOperation.getSourceBeforeTranslate();
			mScrollableHandler.removeViewInHolder(index);
			detachViewFromParent(child);
			if (getChildCount() == 1 && clearAll) {
				clearAllViews();
			}
		}
	}

	private void clearAllViews() {
		mScrollableHandler.clearHolder();
		detachAllViewsFromParent();
		mAdapter.getViewHolder().clear();
	}

	public void clearDeletedView(Object info) {
		clearDeletedView(info, true);
	}

	private void caculateRowsNum(int height) {
		int numRow = 1;
		int perRowHeight = IconView.getIconHeight(2);
		if (height > perRowHeight) {
			numRow = height / perRowHeight;
		}
		setNumRows(numRow);
	}
	@Override
	public void setNumRows(int numRows) {
		if (numRows == 0) {
			try {
				throw new Exception();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.setNumRows(numRows);
	}
	@Override
	public void setData(List infoList) {
		super.setData(infoList);
	}

	protected int getContentSize() {
		List<?> list = (List<?>) getTag();
		return list.size();
	}

	public List<GLView> getChildren(int screen) {
		return mScrollableHandler.getChildren(screen);
	}

	public int getCurrentScreen() {
		return ((HorScrollableGridViewHandler) mScrollableHandler).getCurrentScreen();
	}

	@Override
	public void refreshGridView() {
		setData((List) getTag());
	}
	@Override
	public void onFolderOpenEnd(int curStatus) {
	}
	@Override
	public void onFolderCloseEnd(int curStatus, BaseFolderIcon<?> baseFolderIcon, boolean needReopen) {
	}

	public void keepOpenFolder(boolean keep, Object dragInfo) {
		if (mCloseFolderRunnable != null) {
			if (keep) {
				mGridViewHandler.removeCallbacks(mCloseFolderRunnable);
			} else {
				mGridViewHandler.postDelayed(mCloseFolderRunnable, CLOSE_FOLDER_DELAY);
			}
		}
	}

	public FolderStatus getStatus() {
		return mStatus;
	}

	public void onFolderContentIconChange(Bundle bundle) {

	}

	public void onFolderContentNameChange(String name, long itemId) {

	}

	public abstract void onFolderContentUninstall(ArrayList<AppItemInfo> uninstallapps);

	/**
	 * 
	 * @param intent 目标的intent
	 * @return true：需要执行滚动，false：不需要执行滚动
	 */
	protected boolean scrollToTargetItem(int index) {
		if (index != -1) {
			if (index <= getCurrentScreenLastIndex() && index >= getCurrentScreenFirstIndex()) {
				return false;
			} else {
				int idx = index / getPageItemCount();
				mScrollableHandler.scrollTo(idx, true);
			}
		}
		return true;
	}

	public boolean onHomeAction(GestureSettingInfo info) {
		if (mStatus != null) {
			return mStatus.onHomeAction(info);
		}
		return false;
	}

	public void onFolderLocateApp(Intent intent) {

	}
	
	void onResume() {
		mStatus.onResume();
	}
}
