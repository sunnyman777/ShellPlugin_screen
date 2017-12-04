package com.jiubang.shell.appdrawer.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;

import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.animation.Translate3DAnimation;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.effector.united.IEffectorIds;
import com.go.util.log.Duration;
import com.golauncher.message.IAppDrawerMsgId;
import com.golauncher.message.ICommonMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.appfunc.setting.AppFuncAutoFitManager;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicator;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.appdrawer.adapter.GLGridBaseAdapter;
import com.jiubang.shell.appdrawer.controler.AppDrawerStatusManager;
import com.jiubang.shell.appdrawer.controler.Status;
import com.jiubang.shell.common.component.AbsScrollableGridViewHandler;
import com.jiubang.shell.common.component.AbsScrollableGridViewHandler.ScrollZoneListener;
import com.jiubang.shell.common.component.GLNoDataView;
import com.jiubang.shell.common.component.GLScrollableBaseGrid;
import com.jiubang.shell.common.component.HorScrollableGridViewHandler;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.component.VerScrollableGridViewHandler;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.folder.GLAppFolder;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.scroller.effector.CoupleScreenEffector;

/**
 * 
 * @author yangguanxiang
 *
 */
public abstract class GLAppDrawerBaseGrid extends GLExtrusionGridView
		implements
			IMessageHandler {
	protected AppFuncUtils mUtils;
//	protected FunAppSetting mSetting;
	protected FuncAppDataHandler mFuncAppDataHandler;
	protected Status mCurStatus;
	protected AppDrawerControler mControler;
	protected boolean mNoData;
	protected GLNoDataView mNoDataView;
	protected boolean mFirstLayout;
	protected SparseArray<AbsScrollableGridViewHandler> mGridViewHandlerMap = new SparseArray<AbsScrollableGridViewHandler>();
	protected ScrollZoneListener mScrollZoneListener;

	private static final float SCROOL_ACC_FACTOR = 1.2f;
	/**
	 * 原始PaddingTop
	 */
	private int mOriginalPaddingTop;
	/**
	 * 原始PaddingBottom
	 */
	private int mOriginalPaddingBottom;
	private Thread mUpdateGridSettingThread;

	public GLAppDrawerBaseGrid(Context context) {
		super(context);
		init();
	}

	public GLAppDrawerBaseGrid(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GLAppDrawerBaseGrid(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		MsgMgrProxy.registMsgHandler(this);
		mControler = AppDrawerControler.getInstance(ShellAdmin.sShellManager.getActivity());
		mUtils = AppFuncUtils.getInstance(ShellAdmin.sShellManager.getActivity());
		mFuncAppDataHandler = FuncAppDataHandler
				.getInstance(ShellAdmin.sShellManager.getActivity());

		initNoDataView();
		handleRowColumnSetting(false);
		handleScrollerSetting();
	}

	protected void initNoDataView() {
		mNoDataView = new GLNoDataView(mContext);
		GLScrollableBaseGrid.LayoutParams p = new GLScrollableBaseGrid.LayoutParams(
				GLScrollableBaseGrid.LayoutParams.MATCH_PARENT,
				GLScrollableBaseGrid.LayoutParams.MATCH_PARENT);
		mNoDataView.setLayoutParams(p);
		addViewInLayout(mNoDataView, 0, p, true);
	}

	@Override
	protected void handleScrollerSetting() {
		int direct = mFuncAppDataHandler.getSlideDirection();
		AbsScrollableGridViewHandler gridViewHandler = mGridViewHandlerMap.get(direct);
		if (direct == FunAppSetting.SCREENMOVEHORIZONTAL) {
			mIsVerScroll = false;
			if (gridViewHandler == null) {
				mScrollableHandler = new HorScrollableGridViewHandler(mContext, this,
						CoupleScreenEffector.PLACE_MENU, false, true) {
					
					@Override
					public void onEnterLeftScrollZone() {
						mIsInScrollZone = true;
						mIconOperation.doEnterScrollZone();
						if (mScrollZoneListener != null) {
							mScrollZoneListener.onEnterLeftScrollZone();
						}
					}

					@Override
					public void onEnterRightScrollZone() {
						mIsInScrollZone = true;
						mIconOperation.doEnterScrollZone();
						if (mScrollZoneListener != null) {
							mScrollZoneListener.onEnterRightScrollZone();
						}
					}

					@Override
					public void onEnterTopScrollZone() {
						mIsInScrollZone = true;
						mIconOperation.doEnterScrollZone();
						if (mScrollZoneListener != null) {
							mScrollZoneListener.onEnterTopScrollZone();
						}
					}

					@Override
					public void onEnterBottomScrollZone() {
						mIsInScrollZone = true;
						mIconOperation.doEnterScrollZone();
						if (mScrollZoneListener != null) {
							mScrollZoneListener.onEnterBottomScrollZone();
						}
					}

					@Override
					public void onExitScrollZone() {
						mIsInScrollZone = false;
						if (mScrollZoneListener != null) {
							mScrollZoneListener.onExitScrollZone();
						}
					}

					@Override
					public void onScrollLeft() {
						if (mIsInScrollZone) {
							if (!GLAppFolder.getInstance().isFolderOpened()
									&& !mIconOperation.isEnterOverlay()) {
								super.onScrollLeft();
								setPreviousScreenIndex();
							}
						}
					}

					@Override
					public void onScrollRight() {
						if (mIsInScrollZone) {
							if (!GLAppFolder.getInstance().isFolderOpened()
									&& !mIconOperation.isEnterOverlay()) {
								super.onScrollRight();
								setNextScreenIndex();
							}
						}
					}

					@Override
					public void onScrollTop() {
						if (mIsInScrollZone) {
							if (!GLAppFolder.getInstance().isFolderOpened()
									&& !mIconOperation.isEnterOverlay()) {
								super.onScrollTop();
								setPreviousScreenIndex();
							}
						}
					}

					@Override
					public void onScrollBottom() {
						if (mIsInScrollZone) {
							if (!GLAppFolder.getInstance().isFolderOpened()
									&& !mIconOperation.isEnterOverlay()) {
								super.onScrollBottom();
								setNextScreenIndex();
							}
						}
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
						if (mCurrentScreen == mTotalScreens - 1 && !isCircular()) {
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
				mGridViewHandlerMap.put(direct, mScrollableHandler);
			} else {
				mScrollableHandler = gridViewHandler;
			}
			int effectType = FuncAppDataHandler.getInstance(ShellAdmin.sShellManager.getActivity())
					.getIconEffect();
			if (effectType == IEffectorIds.EFFECTOR_TYPE_RANDOM_CUSTOM) {
				// 如果是随机自定义的特效
				FunAppSetting setting = null;
				if ((setting = SettingProxy.getFunAppSetting()) != null) {
					int[] effects = setting.getAppIconCustomRandomEffect();
					((HorScrollableGridViewHandler) mScrollableHandler).setEffectType(effects);
				}
			} else {
				// 一般特效
				((HorScrollableGridViewHandler) mScrollableHandler).setEffectType(effectType);
			}
		} else {
			mIsVerScroll = true;
			if (gridViewHandler == null) {
				mScrollableHandler = new VerScrollableGridViewHandler(mContext, this) {

					@Override
					public void onEnterLeftScrollZone() {
						if (GLAppFolder.getInstance().isFolderOpened()) {
							return;
						}
						mIconOperation.doEnterScrollZone();
						mIsInScrollZone = true;
						if (mScrollZoneListener != null) {
							mScrollZoneListener.onEnterLeftScrollZone();
						}
					}

					@Override
					public void onEnterRightScrollZone() {
						if (GLAppFolder.getInstance().isFolderOpened()) {
							return;
						}
						mIconOperation.doEnterScrollZone();
						mIsInScrollZone = true;
						if (mScrollZoneListener != null) {
							mScrollZoneListener.onEnterRightScrollZone();
						}
					}

					@Override
					public void onEnterTopScrollZone() {
						if (GLAppFolder.getInstance().isFolderOpened()) {
							return;
						}
						mIsInScrollZone = true;
						mIconOperation.doEnterScrollZone();
						if (mScrollZoneListener != null) {
							mScrollZoneListener.onEnterTopScrollZone();
						}
					}

					@Override
					public void onEnterBottomScrollZone() {
						if (GLAppFolder.getInstance().isFolderOpened()) {
							return;
						}
						mIsInScrollZone = true;
						mIconOperation.doEnterScrollZone();
						if (mScrollZoneListener != null) {
							mScrollZoneListener.onEnterBottomScrollZone();
						}
					}

					@Override
					public void onExitScrollZone() {
						mIsInScrollZone = false;
						if (mScrollZoneListener != null) {
							mScrollZoneListener.onExitScrollZone();
						}
						GLAppDrawerBaseGrid.this.onScrollFinish();
					}

					@Override
					public void onScrollTop() {
						if (mIsInScrollZone && !mIconOperation.isEnterOverlay()) {
							super.onScrollTop();
							generateRectList();
							mIconOperation.setIsFling(true);
						}
					}

					@Override
					public void onScrollBottom() {
						if (mIsInScrollZone && !mIconOperation.isEnterOverlay()) {
							super.onScrollBottom();
							generateRectList();
							mIconOperation.setIsFling(true);
						}
					}

				};
				mGridViewHandlerMap.put(direct, mScrollableHandler);
			} else {
				mScrollableHandler = gridViewHandler;
			}
			((VerScrollableGridViewHandler) mScrollableHandler)
					.setVerticalEffect(mFuncAppDataHandler.getVerticalScrollEffect());
		}
		boolean cycle = mFuncAppDataHandler.getScrollLoop() == FunAppSetting.ON;
		mScrollableHandler.setCycleScreenMode(cycle);
		mScrollableHandler.setAccFactor(SCROOL_ACC_FACTOR);
	}

	@Override
	protected void handleRowColumnSetting(boolean updateDB) {
		int standard = mFuncAppDataHandler.getStandard();
		setGridRowsAndColumns(standard, updateDB);
		mNeedRegetIconRectList = true;
	}

	public void setGridRowsAndColumns(int standard, boolean updateDB) {
		int smallerBound = mUtils.getSmallerBound();
		int row = 0;
		int column = 0;
		switch (standard) {
			case FunAppSetting.LINECOLUMNNUMXY_SPARSE : {
				if (GoLauncherActivityProxy.isPortait()) {
					if (smallerBound <= 240) {
						row += 3;
						column += 4;
					} else {
						row += 4;
						column += 4;
					}
					if ((mNumColumns != column) || (mNumRows != row)) {
						mNumColumns = column;
						mNumRows = row;
						updateRCNum(column, row);
						return;
					}
				} else {
					if (smallerBound <= 240) {
						row += 3;
						column += 4;
					} else {
						row += 3;
						column += 5;
					}
					if ((mNumColumns != column) || (mNumRows != row)) {
						mNumColumns = column;
						mNumRows = row;
						updateRCNum(column, row);
						return;
					}
				}
				updateRCNum(column, row);
				break;
			}
			case FunAppSetting.LINECOLUMNNUMXY_MIDDLE : {
				if (GoLauncherActivityProxy.isPortait()) {
					if (smallerBound <= 240) {
						row += 4;
						column += 4;
					} else {
						row += 4;
						column += 5;
					}
					if ((mNumColumns != column) || (mNumRows != row)) {
						mNumColumns = column;
						mNumRows = row;
						updateRCNum(column, row);
						return;
					}
				} else {
					row += 3;
					if (smallerBound <= 240) {
						column += 5;
					} else {
						column += 6;
					}
					if ((mNumColumns != column) || (mNumRows != row)) {
						mNumColumns = column;
						mNumRows = row;
						updateRCNum(column, row);
						return;
					}
				}
				updateRCNum(column, row);
				break;
			}
			case FunAppSetting.LINECOLUMNNUMXY_MIDDLE_2 : {
				if (GoLauncherActivityProxy.isPortait()) {
					if (smallerBound <= 240) {
						row += 4;
						column += 4;
					} else {
						row += 5;
						column += 4;
					}
					if ((mNumColumns != column) || (mNumRows != row)) {
						mNumColumns = column;
						mNumRows = row;
						updateRCNum(column, row);
						return;
					}
				} else {
					row += 3;
					if (smallerBound <= 240) {
						column += 5;
					} else {
						column += 6;
					}
					if ((mNumColumns != column) || (mNumRows != row)) {
						mNumColumns = column;
						mNumRows = row;
						updateRCNum(column, row);
						return;
					}
				}
				updateRCNum(column, row);
				break;
			}
			case FunAppSetting.LINECOLUMNNUMXY_THICK : {
				if (GoLauncherActivityProxy.isPortait()) {
					if (smallerBound <= 240) {
						column += 5;
						row += 4;
					} else {
						column += 5;
						row += 5;
					}
					if ((mNumColumns != column) || (mNumRows != row)) {
						mNumColumns = column;
						mNumRows = row;
						updateRCNum(column, row);
						return;
					}
				} else {
					if (smallerBound <= 240) {
						row += 4;
						column += 5;

					} else {
						row += 4;
						column += 6;

					}
					if ((mNumColumns != column) || (mNumRows != row)) {
						mNumColumns = column;
						mNumRows = row;
						updateRCNum(column, row);
						return;
					}
				}
				updateRCNum(column, row);
				break;
			}
			case FunAppSetting.LINECOLUMNNUMXY_DIY : {
				FunAppSetting setting = SettingProxy.getFunAppSetting();
				if (GoLauncherActivityProxy.isPortait()) {
					mNumColumns = setting.getColNum();
					mNumRows = setting.getRowNum();
				} else {
					mNumRows = setting.getColNum();
					mNumColumns = setting.getRowNum();
				}

				return;
			}
			case FunAppSetting.LINECOLUMNNUMXY_AUTO_FIT : {
				AppFuncAutoFitManager autoFitMgr = AppFuncAutoFitManager
						.getInstance(ShellAdmin.sShellManager.getActivity());
				if (GoLauncherActivityProxy.isPortait()) {
					mNumColumns = autoFitMgr.getAppDrawerColumnsV();
					mNumRows = autoFitMgr.getAppDrawerRowsV();
				} else {
					mNumColumns = autoFitMgr.getAppDrawerColumnsH();
					mNumRows = autoFitMgr.getAppDrawerRowsH();
				}
				updateRCNum(mNumColumns, mNumRows);
				break;
			}
			default : {
				if (GoLauncherActivityProxy.isPortait()) {
					if (smallerBound <= 240) {
						row += 3;
						column += 4;
					} else {
						row += 4;
						column += 4;
					}
					if ((mNumColumns != column) || (mNumRows != row)) {
						mNumColumns = column;
						mNumRows = row;
						updateRCNum(column, row);
						return;
					}
				} else {
					if (smallerBound <= 240) {
						row += 3;
						column += 4;
					} else {
						row += 3;
						column += 5;
					}
					if ((mNumColumns != column) || (mNumRows != row)) {
						mNumColumns = column;
						mNumRows = row;
						updateRCNum(column, row);
						return;
					}
				}
				break;
			}
		}
	}

	private void updateRCNum(final int column, final int row) {
		if (mUpdateGridSettingThread == null) {
			mUpdateGridSettingThread = new Thread(new Runnable() {
				@Override
				public void run() {
					android.os.Process
							.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
					FunAppSetting setting = SettingProxy.getFunAppSetting();
					setting.setColNum(column);
					setting.setRowNum(row);
					mUpdateGridSettingThread = null;
				}
			});
			mUpdateGridSettingThread.start();
		}
	}
	
	public void setCurGridStatus(Status curGridStatus) {
		mCurStatus = curGridStatus;
		// 各自grid处理各自的状态改变业务
		onGridStatusChange(curGridStatus.getGridStatusID());
	}

	@Override
	public boolean onItemLongClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		boolean ret = super.onItemLongClick(parent, view, position, id);
		ret |= mCurStatus.onLongClickUnderStatus(parent, view, position, id);
		return ret;
	}

	@Override
	public void onItemClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		mCurStatus.onClickUnderStatus(parent, view, position, id);
	}

	@Override
	protected void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
		Duration.setStart("Layout");
		if (isChanged) {
			handleRowColumnSetting(true);
		}
		super.onLayout(isChanged, left, top, right, bottom);
		if (mNoData) {
			mNoDataView.measure(MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY));
			mNoDataView.layout(left, top, right, bottom);
		}
		Log.i("wuziyi", "Layout time:" + Duration.getDuration("Layout"));
		Log.i("wuziyi", "GLAppDrawerBaseGrid onLayout:" + isChanged);
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		if (!mNoData) {
			super.dispatchDraw(canvas);
		} else {
			if (mControler.isInitedAllFunItemInfo()) {
				mNoDataView.draw(canvas);
			}
		}
	}

	@Override
	public synchronized boolean onTouchEvent(MotionEvent ev) {
		if (!mNoData) {
			return super.onTouchEvent(ev);
		} else {
			return false;
		}
	}

	@Override
	public void setTopViewId(int id) {

	}

	@Override
	public int getTopViewId() {
		return IViewId.APP_DRAWER;
	}

	@Override
	public void callBackToChild(GLView view) {
		super.callBackToChild(view);
		if (mCurStatus.getGridStatusID() == AppDrawerStatusManager.GRID_EDIT_STATUS) {
			if (view instanceof IconView) {
				IconView<?> iconView = (IconView<?>) view;
				iconView.startShake();
			}
		}
	}

	public abstract int getGridId();

	protected abstract void onGridStatusChange(int gridStatusID);

	/**
	 * 处理滚屏方向和滚屏特效改变
	 */
	public void handleScrollerSettingChange() {
		handleScrollerSetting();
		mNeedRegetIconRectList = true;
		mScrollableHandler.resetOrientation();
	}

	/**
	 * 处理滚动循环改变
	 */
	public void handleScrollLoopChange() {
		mScrollableHandler
				.setCycleScreenMode(mFuncAppDataHandler.getScrollLoop() == FunAppSetting.ON);
	}

	/**
	 * 处理显示应用程序名改变
	 */
	@SuppressWarnings("rawtypes")
	public void handleShowAppNameChange() {
		if (mAdapter != null) {
			int count = mAdapter.getCount();
			boolean isShowName = (mFuncAppDataHandler.getShowName() < FunAppSetting.APPNAMEVISIABLEYES)
					? false
					: true;
			for (int i = 0; i < count; i++) {
				IconView iconView = (IconView) getViewAtPosition(i);
				if (iconView != null) {
					iconView.setEnableAppName(isShowName);
				}
			}
			((GLGridBaseAdapter) mAdapter).setIsShowAppName(isShowName);
		}
	}

	/**
	 * 执行手势动画
	 */
	public void doSwipeAnimation(AnimationTask task, int type, int size) {
		if (!mNoData) { // 有数据由Grid执行动画 
			((HorScrollableGridViewHandler) mScrollableHandler).doSwipeAnimation(task, type, size);
		} else { // 无数据由NoDataView执行动画
			mNoDataView.doSwipeAnimation(task, type, size);
		}
	}

	/**
	 * 各自grid里面的文件夹，文件夹中的元素，抓起放手后的回调事件
	 * @param target
	 * @param dragInfo
	 * @param success
	 * @param resetInfo
	 * @param folderId
	 */
	public abstract void onFolderDropComplete(Object target, Object dragInfo, boolean success,
			DropAnimationInfo resetInfo, long folderId);

	public void setScrollZoneListener(ScrollZoneListener listener) {
		mScrollZoneListener = listener;
	}

	public void setOriginalPaddding(int originalPaddingTop, int originalPaddingBottom) {
		mOriginalPaddingTop = originalPaddingTop;
		mOriginalPaddingBottom = originalPaddingBottom;
	}

	public int getOriginalPaddingTop() {
		return mOriginalPaddingTop;
	}

	public int getOriginalPaddingBottom() {
		return mOriginalPaddingBottom;
	}

	public int[] getPaddingExtends(int height, int paddingTop, int paddingBottom) {
		int[] paddingExtends = new int[] { 0, 0 };
		int minDistance = DrawUtils.dip2px(8); // 指示器在下的情况下，顶部需要留出的空隙
		int actualHeight = height - paddingTop - paddingBottom;
		if (actualHeight > 0) {
			int rowHeight = actualHeight / mNumRows;
			int iconHeight = IconView.getIconHeight(2);
			boolean isIndicatorOnBottom = SettingProxy.getScreenSettingInfo().mAppDrawerIndicatorPosition
					.equals(ScreenIndicator.INDICRATOR_ON_BOTTOM);
			if (iconHeight > rowHeight) {
				int baseExtends = (iconHeight - rowHeight) / 2;
				if (!isVerScroll() && isIndicatorOnBottom) {
					paddingExtends[0] = baseExtends + minDistance;
				} else {
					paddingExtends[0] = baseExtends;
				}
				paddingExtends[1] = baseExtends;
			} else {
				if (!isVerScroll() && isIndicatorOnBottom) {
					int distance = (rowHeight - iconHeight) / 2;
					if (distance < minDistance) {
						paddingExtends[0] = minDistance - distance;
					}
				}
			}
		}
		return paddingExtends;
	}

	@Override
	public boolean handleMessage(Object sender, int msgId, int param, Object... objects) {
		boolean ret = false;
		switch (msgId) {
			case ICommonMsgId.COMMON_IMAGE_CHANGED :
				int count = getChildCount();
				for (int i = 0; i < count; i++) {
					IconView<?> icon = (IconView<?>) getChildAt(i);
					icon.reloadResource();
				}
				ret = true;
				break;
			case IAppDrawerMsgId.APPDRAWER_RESET_SCROLL_STATE :
				mScrollableHandler.resetScrollState();
				ret = true;
				break;
			default :
				break;
		}
		return ret;
	}
	
	@Override
	protected int getLongPressTimeout() {
		if (mCurStatus.getGridStatusID() == AppDrawerStatusManager.GRID_EDIT_STATUS) {
			return QUICK_LONG_PRESS_TIMEOUT;
		}
		return super.getLongPressTimeout();
	}
	
	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		if (mScrollableHandler instanceof HorScrollableGridViewHandler) {
			HorScrollableGridViewHandler handler = (HorScrollableGridViewHandler) mScrollableHandler;
			int currentScreen = handler.getCurrentScreen();
			ArrayList<GLView> children = handler.getChildren(currentScreen);
			if (children != null) {
				for (GLView child : children) {
					child.cancelLongPress();
				}
			}
		} else {
			VerScrollableGridViewHandler handler = (VerScrollableGridViewHandler) mScrollableHandler;
			int firstRow = handler.getCurFirstVisiableRow();
			int lastRow = handler.getCurLastVisiableRow();
			for (int i = firstRow; i <= lastRow; i++) {
				ArrayList<GLView> children = handler.getChildren(i);
				if (children != null) {
					for (GLView child : children) {
						child.cancelLongPress();
					}
				}
			}
		}
	}
	
	@Override
	protected boolean isAllowToScroll() {
		return !GLAppFolder.getInstance().isFolderOpened();
	}
	
	/**
	 * 执行退出编辑波浪动画
	 */
	public void doChildExitEditStateAnimation() { 
		if (isVerScroll()) {
			VerScrollableGridViewHandler handler = (VerScrollableGridViewHandler) mScrollableHandler;
			if (handler.getVerticalEffect() == VerScrollableGridViewHandler.WATERFALL_VERTICAL_EFFECTOR) {
				return; // 瀑布特效下由于动画支持不好，故不做退出动画
			}
		}
		// 以4行图标作为标准，单程动画时长为l，总时长4*l+l即5*l，每个延迟为4*l/mNumRows
		final int animationDuration = 100;
		int standardRow = 4; //标准行数
		List<GLView> list = getCurScreenIcons();
		int totalRow = list.size() / mNumColumns + 1;
//		AnimationTask task = new AnimationTask(false, AnimationTask.PARALLEL);
		int oneRowDelay = standardRow * animationDuration / mNumRows;
		for (int i = 0; i < totalRow; i++) {
			int rowDelay = (totalRow - 1 - i) * oneRowDelay;
			if (i == totalRow - 2) { // 倒数第二行特殊处理，缩短开始时间
				rowDelay = rowDelay / 2;
			}
			for (int j = i * mNumColumns; j < i * mNumColumns + mNumColumns && j < list.size(); j++) {
				final GLView child = list.get(j);
				if (child != null) {
					final float distance = DrawUtils.dip2px(/*30*/12);
					Animation translateAnimation = new Translate3DAnimation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, distance);
					translateAnimation.setDuration(animationDuration);
					translateAnimation.setStartOffset(rowDelay);
					
					AnimationListener listener = new AnimationListenerAdapter() {
						
						@Override
						public void onAnimationEnd(Animation animation) {
							Animation translateAnimation =  new Translate3DAnimation(0.0f, 0.0f, 0.0f, 0.0f, distance, 0.0f);
							translateAnimation.setDuration(/*12*/8 * animationDuration);
							translateAnimation.setInterpolator(InterpolatorFactory.getInterpolator(
									InterpolatorFactory.ELASTIC, InterpolatorFactory.EASE_OUT,
									new float[] { 0.5f, 0.5f }));
							child.startAnimation(translateAnimation);
						}
					};
//					task.addAnimation(child, translateAnimation, listener);
					
					translateAnimation.setAnimationListener(listener);
					child.startAnimation(translateAnimation);
				}
			}
		}
//		JobManager.postJob(new Job(-1, task, false));
//		GLAnimationManager.startAnimation(task);
	}
	
	public void scrollToFirst() {
		if (mFuncAppDataHandler != null && mGridViewHandlerMap != null) {
			int direct = mFuncAppDataHandler.getSlideDirection();
			AbsScrollableGridViewHandler gridViewHandler = mGridViewHandlerMap
					.get(direct);
			if (gridViewHandler != null) {
				gridViewHandler.scrollToFirst();
			}
		}
	}
	
	protected void showProtectLayer() {
		ShellAdmin.sShellManager.getShell().show(IViewId.PROTECTED_LAYER, false);
	}
	
	protected void hideProtectLayer() {
		ShellAdmin.sShellManager.getShell().hide(IViewId.PROTECTED_LAYER, false);
	}

}
