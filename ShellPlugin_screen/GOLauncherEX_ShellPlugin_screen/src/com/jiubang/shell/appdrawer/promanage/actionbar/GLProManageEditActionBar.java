package com.jiubang.shell.appdrawer.promanage.actionbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View.MeasureSpec;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.proxy.GoLauncherActivityProxy;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.common.component.GLActionBar;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.drag.DropTarget;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 正在运行顶部编辑状态栏
 * @author yejijiong
 *
 */
public class GLProManageEditActionBar extends GLActionBar implements DropTarget {
	private GLProManageEditActionBarItem mInfoView;
	private GLProManageEditActionBarItem mLockView;
	/**
	 * 顶部栏高度
	 */
	private int mTopContainerHeight;
	private AppDrawerControler mAppDrawerControler = null;
	public static final int STATUS_LOCK = 0;
	public static final int STATUS_UNLOCK = 1;
	/**
	 * 锁定／解锁项当前状态
	 */
	private int mCurLockItemStatus = STATUS_LOCK;
	/**
	 * 锁定图标
	 */
	private Drawable mLockDrawable;
	/**
	 * 解锁图标
	 */
	private Drawable mUnLockDrawable;

	public GLProManageEditActionBar(Context context) {
		super(context);
		initViews();
		showDivider(true);
		mAppDrawerControler = AppDrawerControler
				.getInstance(ShellAdmin.sShellManager.getActivity());
	}

	private void initViews() {
		mInfoView = new GLProManageEditActionBarItem(mContext);
		mInfoView.setText(mContext.getString(R.string.infotext));
		mInfoView.setIconDrawable(mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeRunningInfoImg, true,
				R.drawable.gl_appdrawer_process_info_running));
		addComponent(mInfoView);

		mLockView = new GLProManageEditActionBarItem(mContext);
		mLockView.setText(mContext.getString(R.string.lock2text));
		mLockView.setIconDrawable(mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeRunningLockImg, true,
				R.drawable.gl_appdrawer_process_lock_running));
		addComponent(mLockView);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int childWidthMeasureSpec;
		int childHeightMeasureSpec;
		mTopContainerHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.appdrawer_top_bar_container_height);
		//		if (GoLauncherActivityProxy.isPortait()) {
		int paddingTop = StatusBarHandler.isHide() ? 0 : StatusBarHandler.getStatusbarHeight();
		mTopContainerHeight += paddingTop;
		int tabWidth = StatusBarHandler.getDisplayWidth() / 2;
		childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(tabWidth, MeasureSpec.EXACTLY);
		childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mTopContainerHeight,
				MeasureSpec.EXACTLY);

		//		} else {
		//			int tabHeight = StatusBarHandler.getDisplayHeight() / 2;
		//			childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mTopContainerHeight,
		//					MeasureSpec.EXACTLY);
		//			childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(tabHeight, MeasureSpec.EXACTLY);
		//		}
		mInfoView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
		mLockView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
	}

	@Override
	public void loadResource() {
		super.loadResource();
		mLockDrawable = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeRunningLockImg, true,
				R.drawable.gl_appdrawer_process_lock_running);
		mUnLockDrawable = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeRunningUnLockImg, true,
				R.drawable.gl_appdrawer_process_unlock_running);
	}

	@Override
	protected void onVisibilityChanged(GLView changedView, int visibility) {
		if (changedView == this) {
			if (visibility == GLView.VISIBLE) {
				ShellAdmin.sShellManager.getShell().getDragController()
						.addDropTarget(this, IViewId.APP_DRAWER);
			} else {
				ShellAdmin.sShellManager.getShell().getDragController().removeDropTarget(this);
			}
		}
	}

	@Override
	public int getTopViewId() {
		return IViewId.PRO_MANAGE;
	}

	@Override
	public boolean onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, DropAnimationInfo resetInfo) {
		boolean ret = false;
		FunAppItemInfo itemInfo = (FunAppItemInfo) dragInfo;
		Intent intent = itemInfo.getIntent();
		if (intent == null) {
			return false;
		}
		switch (checkDragOverXY(x, y)) {
			case POINT_LEFT :
				mAppDrawerControler.skipAppInfobyIntent(intent);
				GuiThemeStatistics.sideOpStaticData("-1", "rg_info", 1, "-1");
				ret = true;
				break;
			case POINT_RIGHT :
				if (itemInfo.isIgnore()) {
					mAppDrawerControler.delIgnoreAppItem(intent);
					GuiThemeStatistics.sideOpStaticData("-1", "rg_unlock", 1, "-1");
				} else {
					GuiThemeStatistics.sideOpStaticData("-1", "rg_lock", 1, "-1");
					mAppDrawerControler.addIgnoreAppItem(intent);
				}
				ret = true;
				break;
			default :
				break;
		}
		return ret;
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		checkDragOverXY(x, y);
	}

	@Override
	public void onDragExit(DragSource source, DropTarget nextTarget, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		changePointView(POINT_NONE);
	}

	@Override
	public void onDragMove(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
	}

	@Override
	public void setTopViewId(int id) {
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		return true;
	}

	@Override
	public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, Rect recycle) {
		return null;
	}

	/**
	 * 检查dragOver的x,y值落于边
	 */
	private int checkDragOverXY(int x, int y) {
		//		if (GoLauncherActivityProxy.isPortait()) { // 竖屏
		if (y >= 0 && y <= mTopContainerHeight) {
			int middle = GoLauncherActivityProxy.getScreenWidth() / 2;
			if (x <= middle) {
				// 落于左边
				changePointView(POINT_LEFT);
				return POINT_LEFT;
			} else {
				// 落于右边
				changePointView(POINT_RIGHT);
				return POINT_RIGHT;
			}
		} else {
			return POINT_NONE;
		}
		//		} else {
		//			if (x >= 0 && x <= mTopContainerHeight) {
		//				int middle = GoLauncherActivityProxy.getScreenHeight() / 2;
		//				if (y >= middle) {
		//					// 落于上边
		//					changePointView(POINT_INFO);
		//					return POINT_INFO;
		//				} else {
		//					// 落于下边
		//					changePointView(POINT_RIGHT);
		//					return POINT_RIGHT;
		//				}
		//			}
		//		}
		//		return POINT_NONE;
	}

	private static final int POINT_LEFT = 0;
	private static final int POINT_RIGHT = 1;
	private static final int POINT_NONE = 2;
	/**
	 * 改变手指落到的view的底色
	 */
	private void changePointView(int id) {
		switch (id) {
			case POINT_LEFT :
				mInfoView.setIsBeDragOver(true, false);
				mLockView.setIsBeDragOver(false, false);
				break;
			case POINT_RIGHT :
				mInfoView.setIsBeDragOver(false, false);
				mLockView.setIsBeDragOver(true, false);
				break;
			case POINT_NONE :
				mInfoView.setIsBeDragOver(false, false);
				mLockView.setIsBeDragOver(false, false);
				break;
			default :
				break;
		}
	}

	/**
	 * 设置锁定／解锁项当前处于哪种状态
	 */
	public void setLockItemStatus(int status) {
		if (mCurLockItemStatus == status) {
			return;
		}
		mCurLockItemStatus = status;
		switch (mCurLockItemStatus) {
			case STATUS_LOCK :
				mLockView.setIconDrawable(mLockDrawable);
				mLockView.setText(mContext.getString(R.string.lock2text));
				break;
			case STATUS_UNLOCK :
				mLockView.setIconDrawable(mUnLockDrawable);
				mLockView.setText(mContext.getString(R.string.unlock2text));
				break;
			default :
				break;
		}
	}

	@Override
	public boolean needDrawBg() {
		return false;
	}

	@Override
	public void onInOutAnimationStart(boolean in) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInOutAnimationEnd(boolean in) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConfigurationChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVerticalMode(boolean isVertical) {
		mIsVerticalMode = true;
	}

	@Override
	public void onParentInOutAnimationStart(boolean in) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onParentInOutAnimationEnd(boolean in) {
		// TODO Auto-generated method stub

	}
}
