package com.jiubang.shell.appdrawer.component;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.MotionEvent;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.InterpolatorValueAnimation;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.graphics.NinePatchGLDrawable;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.util.SingleThreadProxy;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.FadePainter;
import com.golauncher.message.IAppDrawerMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.appdrawer.GLAppDrawerMainView;
import com.jiubang.shell.appdrawer.GridContainerTouchListener;
import com.jiubang.shell.appdrawer.allapp.GLAllAppAdapter;
import com.jiubang.shell.appdrawer.allapp.GLAllAppGridView;
import com.jiubang.shell.appdrawer.controler.AppDrawerStatusManager;
import com.jiubang.shell.appdrawer.controler.Status;
import com.jiubang.shell.appdrawer.controler.StatusFactory;
import com.jiubang.shell.common.component.AbsScrollableGridViewHandler.ScrollZoneListener;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.component.VerScrollableGridViewHandler;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.folder.BaseFolderIcon;
import com.jiubang.shell.folder.GLAppDrawerFolderGridView;
import com.jiubang.shell.folder.GLAppFolderMainView.FolderStatusListener;
import com.jiubang.shell.folder.GLAppFolderMainView.FolderViewAnimationListener;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.indicator.DesktopIndicator;
import com.jiubang.shell.indicator.VerticalIndicator;
import com.jiubang.shell.utils.GLImageUtil;

/**
 * 功能表的gird容器
 * @author wuziyi
 *
 */
public class GLGridViewContainer extends GLFrameLayout
		implements
			FolderStatusListener,
			FolderViewAnimationListener,
			ScrollZoneListener {

	private DesktopIndicator mHorIndicator;
	private VerticalIndicator mVerIndicator;
	private GLAppDrawerBaseGrid mCurGridView;
	private int mCurTabID = -1;
	private int mCurGridID = AppDrawerStatusManager.GRID_NORMAL_STATUS;
	/**
	 * 功能表边缘图片（用于图标拖动至边缘显示高亮图片）
	 */
	private NinePatchGLDrawable mBorderBgDrawable;
	/**
	 * 功能表边缘图片大小
	 */
	private int mBorderBgSize;

	private static final int BORDER_NONE = 0;
	private static final int BORDER_LEFT = 1;
	private static final int BORDER_TOP = 2;
	private static final int BORDER_RIGHT = 3;
	private static final int BORDER_BOTTOM = 4;
	/**
	 * 当前需要绘制高亮颜色的边缘
	 */
	private int mCurBorder = BORDER_NONE;
	private final int mNum90 = 90;
	private final int mNum180 = 180;

	/**
	 * 竖屏第一张阴影图
	 */
	private GLDrawable mShadowDrawableFirstV = null;
	/**
	 * 竖屏第二张阴影图
	 */
	private GLDrawable mShadowDrawableSecondV = null;
	/**
	 * 横屏第一张阴影图
	 */
	private GLDrawable mShadowDrawableFirstH = null;
	/**
	 * 横屏第二张阴影图
	 */
	private GLDrawable mShadowDrawableSecondH = null;

	private int mTopBarSize = 0;
	private int mBottomBarSize = 0;
	private Bitmap mAppDrawerBg = null;
	private FuncAppDataHandler mFuncAppDataHandler;
	FadePainter mFadePainter;
	private int mWaterFallEffectorSizeH = 0;
	private InterpolatorValueAnimation mAnimation;
	private float mTouchX;
	private GridContainerTouchListener mListener;
	private int mCurrentAlpha = FULL_ALPHA;
	private static final int FULL_ALPHA = 255;
	private static final int END_ALPHA = (int) (FULL_ALPHA * 0.5f);
	public static final float VIEW_OFFSET_PERSENT = 0.2f;

	public GLGridViewContainer(Context context) {
		this(context, null);
		init();
	}

	public GLGridViewContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setHasPixelOverlayed(false);
		mHorIndicator = new DesktopIndicator(mContext);
		mVerIndicator = new VerticalIndicator(mContext);
		mBorderBgDrawable = new NinePatchGLDrawable((NinePatchDrawable) mContext.getResources()
				.getDrawable(R.drawable.gl_appdrawer_border_bg));
		mBorderBgSize = ShellAdmin.sShellManager.getContext().getResources()
				.getDimensionPixelSize(R.dimen.appdrawer_border_bg_size);
		mFuncAppDataHandler = FuncAppDataHandler
				.getInstance(ShellAdmin.sShellManager.getActivity());
		mFadePainter = new FadePainter();
		mTopBarSize = mContext.getResources().getDimensionPixelSize(
				R.dimen.appdrawer_top_bar_container_height);
		mBottomBarSize = mContext.getResources().getDimensionPixelSize(
				R.dimen.appdrawer_bottom_bar_container_height);
		mWaterFallEffectorSizeH = mContext.getResources().getDimensionPixelSize(
				R.dimen.appdrawer_waterfall_effector_size);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int indicatorSize = DrawUtils.dip2px(25);
		int[] paddingExtends; // 防止GridView图标超过范围
		if (!mCurGridView.isVerScroll()) {
			mCurGridView.setOriginalPaddding(indicatorSize, 0);
			paddingExtends = mCurGridView.getPaddingExtends(mHeight, indicatorSize, 0);
			mCurGridView.setPadding(0, indicatorSize + paddingExtends[0], 0, 0 + paddingExtends[1]);
			mHorIndicator.layout(0, mHeight, mWidth, mHeight + indicatorSize);
		} else {
			paddingExtends = mCurGridView.getPaddingExtends(mHeight, 0, 0);
			mCurGridView.setPadding(0, paddingExtends[0], 0, 0);
			DisplayMetrics mMetrics = mContext.getResources().getDisplayMetrics();
			int verIndicatorWidth = (int) (AppFuncConstants.SCROLL_SIZE * mMetrics.density);
			mVerIndicator.layout(mWidth - verIndicatorWidth, 0, mWidth, mHeight);
		}
		mCurGridView.layout(0, 0, mWidth, mHeight);
		// 上面的layout会把组建放回正常位置
		if (mAnimation != null) {
			offsetLeftAndRight((int) (mAnimation.getValue() * VIEW_OFFSET_PERSENT));
		}
		updateShadowDrawableBounds();
	}
	

	public void setCurStatus(Status tabStatus) {
		showTab(tabStatus);
		mCurGridView.setCurGridStatus(tabStatus);
		//fixme,animation:move the grid view up and down
		if (mCurGridID == tabStatus.getGridStatusID()) {
			return;
		}
		mCurGridID = tabStatus.getGridStatusID();
		if (tabStatus.getGridStatusID() == AppDrawerStatusManager.GRID_EDIT_STATUS) {
			int indicatorSize = DrawUtils.dip2px(25);
			TranslateAnimation animation = null;
			AnimationTask task = new AnimationTask(false, AnimationTask.PARALLEL);
			animation = new TranslateAnimation(0, 0, 0, -indicatorSize);
			animation.setDuration(300);
			animation.setFillEnabled(true);
			animation.setFillAfter(true);
			task.addAnimation(mHorIndicator, animation, null);
			GLAnimationManager.startAnimation(task);
		} else if (tabStatus.getGridStatusID() == AppDrawerStatusManager.GRID_NORMAL_STATUS) {
			int indicatorSize = DrawUtils.dip2px(25);
			TranslateAnimation animation = null;
			AnimationTask task = new AnimationTask(false, AnimationTask.PARALLEL);
			animation = new TranslateAnimation(0, 0, -indicatorSize, 0);
			animation.setDuration(300);
			animation.setFillEnabled(true);
			animation.setFillAfter(true);
			task.addAnimation(mHorIndicator, animation, null);
			GLAnimationManager.startAnimation(task);
		}
	}

	public boolean isScrollFinish() {
		return mCurGridView.isScrollFinish();
	}

	public boolean isVerScroll() {
		return mCurGridView.isVerScroll();
	}

	private void showTab(Status tabStatus) {
		int tabStringID = tabStatus.getTabStatusID();
		if (mCurTabID == tabStringID) {
			return;
		}
		mCurTabID = tabStringID;
		mCurGridView = (GLAppDrawerBaseGrid) tabStatus.getGridView();
		if (mCurTabID == AppDrawerStatusManager.ALLAPP_TAB) {
			mCurGridView.setScrollZoneListener(this);
		}
		showCurGridView();
		notifyGridDataSetChange();
	}

	/**
	 * 通知当前GridView的适配器刷新
	 */
	public void notifyGridDataSetChange() {
		if (mCurGridView != null) {
			mCurGridView.refreshGridView();
		}
	}

//	/**
//	 * 通知当前GridView的适配器刷新
//	 */
//	public void notifyGridDataSetChange(int firstIndex) {
//		mCurGridView.layoutPartPage(firstIndex, mCurGridView.getChildCount());
//	}

	/**
	 * 显示当前Grid
	 * @param tabId
	 */
	private void showCurGridView() {
		removeAllViews();
		addView(mCurGridView);
		if (!mCurGridView.isVerScroll()) {
			addView(mHorIndicator);
			mCurGridView.setIndicator(mHorIndicator);
		} else {
			addView(mVerIndicator);
			mCurGridView.setIndicator(mVerIndicator);
		}
	}

	/**
	 * 处理滚动设置改变
	 */
	public void handleScrollerSettingChange() {
		SparseArray<GLExtrusionGridView> gridViewMap = StatusFactory.getGridViewMap();
		GLExtrusionGridView gridView;
		for (int i = 0; i < gridViewMap.size(); i++) {
			gridView = gridViewMap.get(gridViewMap.keyAt(i));
			if (gridView instanceof GLAppDrawerBaseGrid) {
				((GLAppDrawerBaseGrid) gridView).handleScrollerSettingChange();
			}
		}
		if (mCurTabID == AppDrawerStatusManager.ALLAPP_TAB/*
				|| mCurTabID == AppDrawerStatusManager.PROMANAGER_TAB*/) {
			showCurGridView();
		}
	}

	/**
	 * 处理滚动循环改变
	 */
	public void handleScrollLoopChange() {
		SparseArray<GLExtrusionGridView> gridViewMap = StatusFactory.getGridViewMap();
		GLExtrusionGridView gridView;
		for (int i = 0; i < gridViewMap.size(); i++) {
			gridView = gridViewMap.get(gridViewMap.keyAt(i));
			if (gridView instanceof GLAppDrawerBaseGrid) {
				((GLAppDrawerBaseGrid) gridView).handleScrollLoopChange();
			}
		}
	}

	/**
	 * 处理行列数改变
	 */
	public void handleRowColumnSettingChange() {
		if (mCurGridView != null) { 
			SparseArray<GLExtrusionGridView> gridViewMap = StatusFactory.getGridViewMap();
			GLExtrusionGridView gridView;
			for (int i = 0; i < gridViewMap.size(); i++) {
				gridView = gridViewMap.get(gridViewMap.keyAt(i));
				if (gridView instanceof GLAppDrawerBaseGrid) {
					((GLAppDrawerBaseGrid) gridView).handleRowColumnSetting(true);
				}
			}
			requestLayout();
			mCurGridView.requestLayout(); // 这里由于不会调用下层的onLayout，需要主动请求
		}
	}

	/**
	 * 处理显示应用程序名改变
	 */
	public void handleShowAppNameChange() {
		SparseArray<GLExtrusionGridView> gridViewMap = StatusFactory.getGridViewMap();
		GLExtrusionGridView gridView;
		for (int i = 0; i < gridViewMap.size(); i++) {
			gridView = gridViewMap.get(gridViewMap.keyAt(i));
			if (gridView instanceof GLAppDrawerBaseGrid) {
				((GLAppDrawerBaseGrid) gridView).handleShowAppNameChange();
			} else if (gridView instanceof GLAppDrawerFolderGridView) {
				((GLAppDrawerFolderGridView) gridView).handleShowAppNameChange();
			}
		}
	}

	/**
	 * 处理所有程序图标标志改变，进行刷新
	 */
	public void handleAllAppIconStateChange() {
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_ALL_APP_GRID_VIEW,
				IAppDrawerMsgId.APPDRAWER_ALL_APP_ICON_STATE_CHANGE, -1);
	}

	/**
	 * 执行手势动画
	 */
	public void doSwipeAnimation(AnimationTask task, int type, int size) {
		Animation moveAnimation = null;
		if (mCurTabID == AppDrawerStatusManager.ALLAPP_TAB/*
				|| mCurTabID == AppDrawerStatusManager.PROMANAGER_TAB*/) {
			if (GoLauncherActivityProxy.isPortait()) { // 竖屏
				float startOffsetY;
				float endOffsetY;
				if (type == GLAppDrawerMainView.ANIMATION_TOP_CONTAINER_SHOW
						|| type == GLAppDrawerMainView.ANIMATION_BOTTOM_CONTAINER_HIDE) {
					startOffsetY = 0.0f;
					endOffsetY = size;
					moveAnimation = new TranslateAnimation(0.0f, 0.0f, startOffsetY, endOffsetY);
				} else if (type == GLAppDrawerMainView.ANIMATION_TOP_CONTAINER_HIDE
						|| type == GLAppDrawerMainView.ANIMATION_BOTTOM_CONTAINER_SHOW) {
					startOffsetY = 0.0f;
					endOffsetY = -size;
					moveAnimation = new TranslateAnimation(0.0f, 0.0f, startOffsetY, endOffsetY);
				}
			} else { // 横屏
				float startOffsetX;
				float endOffsetX;
				if (type == GLAppDrawerMainView.ANIMATION_TOP_CONTAINER_SHOW
						|| type == GLAppDrawerMainView.ANIMATION_BOTTOM_CONTAINER_HIDE) {
					startOffsetX = 0.0f;
					endOffsetX = size / 2;
					moveAnimation = new TranslateAnimation(startOffsetX, endOffsetX, 0.0f, 0.0f);
				} else if (type == GLAppDrawerMainView.ANIMATION_TOP_CONTAINER_HIDE
						|| type == GLAppDrawerMainView.ANIMATION_BOTTOM_CONTAINER_SHOW) {
					startOffsetX = 0.0f;
					endOffsetX = -size / 2;
					moveAnimation = new TranslateAnimation(startOffsetX, endOffsetX, 0.0f, 0.0f);
				}
			}
			if (moveAnimation != null) {
				moveAnimation.setDuration(GLAppDrawerMainView.SWIPE_ANIMATION_DURATION);
				moveAnimation.setFillEnabled(true);
				moveAnimation.setFillBefore(false);
				task.addAnimation(mHorIndicator, moveAnimation, null);
			}
		}
		mCurGridView.doSwipeAnimation(task, type, size);
	}

	@Override
	public void onFolderOpen(BaseFolderIcon<?> baseFolderIcon, boolean animate, int curStatus,
			boolean reopen) {
		mHorIndicator.setVisible(false);
		mVerIndicator.setVisible(false);
		if (mCurGridView instanceof FolderStatusListener) {
			((FolderStatusListener) mCurGridView).onFolderOpen(baseFolderIcon, animate, curStatus,
					reopen);
		}
	}

	@Override
	public void onFolderClose(BaseFolderIcon<?> baseFolderIcon, boolean animate, int curStatus) {
		mHorIndicator.setVisible(true);
		mVerIndicator.setVisible(true);
		if (mCurGridView instanceof FolderStatusListener) {
			((FolderStatusListener) mCurGridView).onFolderClose(baseFolderIcon, animate, curStatus);
		}
	}

	@Override
	public void onFolderStatusChange(int oldStatus, int newStatus) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFolderOpenEnd(int curStatus) {
		if (mCurGridView instanceof FolderViewAnimationListener) {
			((FolderViewAnimationListener) mCurGridView).onFolderOpenEnd(curStatus);
		}
	}

	@Override
	public void onFolderCloseEnd(int curStatus, BaseFolderIcon<?> baseFolderIcon, boolean needReopen) {
		if (mCurGridView instanceof FolderViewAnimationListener) {
			((FolderViewAnimationListener) mCurGridView).onFolderCloseEnd(curStatus,
					baseFolderIcon, needReopen);
		}
	}

	public void onFolderDropComplete(Object target, Object dragInfo, boolean success,
			DropAnimationInfo resetInfo, long folderId) {
		mCurGridView.onFolderDropComplete(target, dragInfo, success, resetInfo, folderId);
	}

	@Override
	public void onFolderReLayout(BaseFolderIcon<?> baseFolderIcon, int curStatus) {
		if (mCurGridView instanceof FolderStatusListener) {
			((FolderStatusListener) mCurGridView).onFolderReLayout(baseFolderIcon, curStatus);
		}
	}

	/**
	 * 重新获取应用中心可更新应用数目
	 */
	public void regetUpdateableAppsCount() {
		SparseArray<GLExtrusionGridView> gridViewMap = StatusFactory.getGridViewMap();
		GLExtrusionGridView gridView = gridViewMap.get(AppDrawerStatusManager.ALLAPP_TAB);
		if (gridView != null && gridView.getAdapter() != null) {
			((GLAllAppAdapter) gridView.getAdapter()).regetUpdateableAppsCount();
		}
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		if (mAnimation != null) {
			int curValue = (int) mAnimation.getValue();
			int offset = (int) (curValue * VIEW_OFFSET_PERSENT - getLeft());
			if (offset != 0) {
				// 理论上不应该做这个的，但offset之后，下面执行draw出来的画面是还没生效
				offsetLeftAndRight(offset);
				invalidate();
			}
		}
		canvas.save();
		canvas.multiplyAlpha(mCurrentAlpha);
		super.dispatchDraw(canvas);
		canvas.restore();
		
		if (mCurBorder != BORDER_NONE) {
			canvas.save();
			switch (mCurBorder) {
				case BORDER_LEFT :
					canvas.rotate(mNum180, mBorderBgSize / 2, mHeight / 2);
					break;
				case BORDER_TOP :
					canvas.rotate(-mNum90, mWidth / 2, 0);
					break;
				case BORDER_RIGHT :
					break;
				case BORDER_BOTTOM :
					canvas.rotate(mNum90, mWidth / 2, mHeight);
					break;
			}
			mBorderBgDrawable.draw(canvas);
			canvas.restore();
		}
		if (GoLauncherActivityProxy.isPortait()) {
			if (mShadowDrawableFirstV != null) {
				mShadowDrawableFirstV.draw(canvas);
			}
			if (mShadowDrawableSecondV != null) {
				mShadowDrawableSecondV.draw(canvas);
			}
		} else {
			if (mShadowDrawableFirstH != null) {
				canvas.save();
				canvas.clipRect(0, -mWaterFallEffectorSizeH, mWidth, 0);
				mShadowDrawableFirstH.draw(canvas);
				canvas.restore();
			}
			if (mShadowDrawableSecondH != null) {
				canvas.save();
				canvas.clipRect(0, mHeight, mWidth, mHeight + mWaterFallEffectorSizeH);
				mShadowDrawableSecondH.draw(canvas);
				canvas.restore();
			}
		}
	}

	@Override
	public void onEnterLeftScrollZone() {
		mCurBorder = BORDER_LEFT;
		mBorderBgDrawable.setBounds(0, 0, mBorderBgSize, mHeight);
	}

	@Override
	public void onEnterRightScrollZone() {
		mCurBorder = BORDER_RIGHT;
		mBorderBgDrawable.setBounds(mWidth - mBorderBgSize, 0, mWidth, mHeight);
	}

	@Override
	public void onEnterTopScrollZone() {
		mCurBorder = BORDER_TOP;
		mBorderBgDrawable
				.setBounds(mWidth / 2 - mBorderBgSize, -mWidth / 2, mWidth / 2, mWidth / 2);
	}

	@Override
	public void onEnterBottomScrollZone() {
		mCurBorder = BORDER_BOTTOM;
		mBorderBgDrawable.setBounds(mWidth / 2 - mBorderBgSize, mHeight - mWidth / 2, mWidth / 2,
				mHeight + mWidth / 2);
	}

	@Override
	public void onExitScrollZone() {
		mCurBorder = BORDER_NONE;
	}

	public void setAppDrawerBg(Bitmap bg) {
		mAppDrawerBg = bg;
		buildShadowDrawable();
	}

	/**
	 * 创建边缘淡化图片
	 */
	public void buildShadowDrawable() {
		clearShadowDrawable();
		if (mAppDrawerBg == null) {
			return;
		}
		int direct = mFuncAppDataHandler.getSlideDirection();
		if (direct == FunAppSetting.SCREENMOVEVERTICAL
				&& mFuncAppDataHandler.getVerticalScrollEffect() == VerScrollableGridViewHandler.WATERFALL_VERTICAL_EFFECTOR) { // 竖滑瀑布
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					int screenWidth = StatusBarHandler.getDisplayWidth();
					int screenHeight = StatusBarHandler.getDisplayHeight();
					Matrix matrix = new Matrix();
					int width = mAppDrawerBg.getWidth();
					int height = mAppDrawerBg.getHeight();
					float scaleWidth = ((float) screenWidth) / width;
					float scaleHeight = ((float) screenHeight) / height;
					BitmapShader bitmapShader = null;
					matrix.postScale(scaleWidth, scaleHeight);
					bitmapShader = new BitmapShader(mAppDrawerBg, TileMode.CLAMP, TileMode.CLAMP);
					bitmapShader.setLocalMatrix(matrix);
					buildWaterTopBottomShadow(bitmapShader, screenWidth, screenHeight);
				}
			};
			SingleThreadProxy.postRunable(runnable);
		}
	}

	/**
	 * 创建瀑布上下边缘淡化图片
	 * @param bitmapShader
	 * @param bitmapShaderH
	 * @param screenWidth
	 * @param screenHeight
	 */
	private void buildWaterTopBottomShadow(BitmapShader bitmapShader, int screenWidth,
			int screenHeight) {
		AppFuncUtils mUtils = AppFuncUtils.getInstance(ShellAdmin.sShellManager.getActivity());
		int statusBarHeight = mUtils.getStatusBarHeight();
		int waterTopSizeV = 0;
		int waterTopSizeH = 0;
		int waterBottomSizeV = 0;
		int waterBottomSizeH = 0;
		if (GoLauncherActivityProxy.isPortait()) {
			waterTopSizeV = mTopBarSize;
			waterBottomSizeV = mBottomBarSize;
			waterTopSizeH = (int) (1.0f * screenHeight / (screenWidth - statusBarHeight) * mWaterFallEffectorSizeH);
			waterBottomSizeH = waterBottomSizeV;
		} else {
			waterTopSizeV = (int) (1.0f * screenHeight / (screenWidth - statusBarHeight) * mTopBarSize);
			waterBottomSizeV = (int) (1.0f * screenHeight / (screenWidth - statusBarHeight) * mBottomBarSize);
			waterTopSizeH = mWaterFallEffectorSizeH;
			waterBottomSizeH = mWaterFallEffectorSizeH;
		}
		// 竖屏瀑布顶部
		Bitmap waterTopV = Bitmap.createBitmap(screenWidth, waterTopSizeV, Bitmap.Config.ARGB_8888);
		Canvas waterTopCanvasV = new Canvas(waterTopV);
		Rect waterTopRectV = new Rect(0, 0, screenWidth, waterTopSizeV);
		mFadePainter.drawFadeBitmap(waterTopCanvasV, waterTopRectV, FadePainter.DIR_FROM_TOP,
				bitmapShader);
		mShadowDrawableFirstV = GLImageUtil.getGLDrawable(new BitmapDrawable(waterTopV));

		// 竖屏瀑布底部
		Bitmap waterBottomV = Bitmap.createBitmap(screenWidth, waterBottomSizeV,
				Bitmap.Config.ARGB_8888);
		Canvas waterBottomCanvasV = new Canvas(waterBottomV);
		waterBottomCanvasV.translate(0, -(screenHeight - waterBottomSizeV)); // 这里要将画布向上移，不然画到的超出可视范围
		Rect waterBottomRectV = new Rect(0, screenHeight - waterBottomSizeV, screenWidth,
				screenHeight);
		mFadePainter.drawFadeBitmap(waterBottomCanvasV, waterBottomRectV,
				FadePainter.DIR_FROM_BOTTOM, bitmapShader);
		mShadowDrawableSecondV = GLImageUtil.getGLDrawable(new BitmapDrawable(waterBottomV));

		// 横屏瀑布顶部
		Bitmap waterTopH = Bitmap.createBitmap(screenWidth, waterTopSizeH, Bitmap.Config.ARGB_8888);
		Canvas waterTopCanvasH = new Canvas(waterTopH);
		Rect waterTopRectH = new Rect(0, 0, screenWidth, waterTopSizeH);
		mFadePainter.drawFadeBitmap(waterTopCanvasH, waterTopRectH, FadePainter.DIR_FROM_TOP,
				bitmapShader);
		mShadowDrawableFirstH = GLImageUtil.getGLDrawable(new BitmapDrawable(waterTopH));

		// 横屏瀑布底部
		Bitmap waterBottomH = Bitmap.createBitmap(screenWidth, waterBottomSizeH,
				Bitmap.Config.ARGB_8888);
		Canvas waterBottomCanvasH = new Canvas(waterBottomH);
		waterBottomCanvasH.translate(0, -(screenHeight - waterBottomSizeH)); // 这里要将画布向上移，不然画到的超出可视范围
		Rect waterBottomRectH = new Rect(0, screenHeight - waterBottomSizeH, screenWidth,
				screenHeight);
		mFadePainter.drawFadeBitmap(waterBottomCanvasH, waterBottomRectH,
				FadePainter.DIR_FROM_BOTTOM, bitmapShader);
		mShadowDrawableSecondH = GLImageUtil.getGLDrawable(new BitmapDrawable(waterBottomH));

		updateShadowDrawableBounds();
	}

	private void updateShadowDrawableBounds() {
		if (GoLauncherActivityProxy.isPortait()) {
			if (mShadowDrawableFirstV != null) {
				mShadowDrawableFirstV.setBounds(0, -mTopBarSize, mWidth, 0);
			}
			if (mShadowDrawableSecondV != null) {
				mShadowDrawableSecondV.setBounds(0, mHeight, mWidth, mHeight + mBottomBarSize);
			}
		} else {
			if (mShadowDrawableFirstH != null) {
				mShadowDrawableFirstH.setBounds(-mTopBarSize, -mWaterFallEffectorSizeH, mWidth
						+ mBottomBarSize, 0);
			}
			if (mShadowDrawableSecondH != null) {
				mShadowDrawableSecondH.setBounds(-mTopBarSize, mHeight, mWidth + mBottomBarSize,
						mHeight + mWaterFallEffectorSizeH);
			}
		}
	}

	/**
	 * 销毁所有边缘淡化图片
	 */
	private void clearShadowDrawable() {
		if (mShadowDrawableFirstV != null) {
			mShadowDrawableFirstV.clear();
			mShadowDrawableFirstV = null;
		}
		if (mShadowDrawableSecondV != null) {
			mShadowDrawableSecondV.clear();
			mShadowDrawableSecondV = null;
		}
		if (mShadowDrawableFirstH != null) {
			mShadowDrawableFirstH.clear();
			mShadowDrawableFirstH = null;
		}
		if (mShadowDrawableSecondH != null) {
			mShadowDrawableSecondH.clear();
			mShadowDrawableSecondH = null;
		}
	}

	public void handleInidcatorThemeChange() {
		mHorIndicator.applyTheme();
	}

	public void locateApp(Intent intent) {
		if (mCurGridView instanceof GLAllAppGridView) {
			GLAllAppGridView allAppGrid = (GLAllAppGridView) mCurGridView;
			allAppGrid.locateApp(intent);
		}
	}
	
	public void setFontSizeColor(int size, int color) {
//		SparseArray<GLExtrusionGridView> gridViewMap = StatusFactory.getGridViewMap();
//		GLExtrusionGridView gridView;
//		for (int i = 0; i < gridViewMap.size(); i++) {
//			gridView = gridViewMap.get(gridViewMap.keyAt(i));
//			if (gridView instanceof GLAppDrawerBaseGrid) {
//				int count = gridView.getChildCount();
//				for (int j = 0; j < count; j++) {
//					IconView icon = (IconView) gridView.getViewAtPosition(j);
//					icon.setFontSize(size);
//					icon.setTitleColor(color);
//				}
//			}
//		}
		
		if (mCurGridView != null) {
			int count = mCurGridView.getChildCount();
			for (int i = 0; i < count; i++) {
				IconView<?> icon = (IconView<?>) mCurGridView.getChildAt(i);
				icon.setFontSize(size);
				icon.setTitleColor(color);
			}
		}
	}
	
	public DesktopIndicator getHorIndicator() {
		return mHorIndicator;
	}
	
	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		if (mCurGridView != null) {
			mCurGridView.cancelLongPress();
		}
	}
	
	public ArrayList<GLView> getCurScreenIcons() {
		return (ArrayList<GLView>) mCurGridView.getCurScreenIcons();
	}
	
	public int getCurGridRow() {
		return mCurGridView.getNumRows();
	}
	
	public int getCurGridCol() {
		return mCurGridView.getNumColumns();
	}
	
	public void scrollToFirst() {
		if (mCurGridView != null) {
			mCurGridView.scrollToFirst();
		}
	}

	public void dragFlingDelete(boolean isDragFromFolder, Object dragInfo) {
		if (mCurGridView instanceof GLAllAppGridView) {
			((GLAllAppGridView) mCurGridView).dragFlingDelete(isDragFromFolder,
					dragInfo);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		boolean ret = false;
		if (GLAppDrawerMainView.isSlideMenuShow()) {
			int action = ev.getAction() & MotionEvent.ACTION_MASK;
			switch (action) {
				case MotionEvent.ACTION_DOWN :
					mTouchX = ev.getRawX();
					break;
				case MotionEvent.ACTION_MOVE :
					float offsetX = ev.getRawX() - mTouchX;
					mTouchX = ev.getRawX();
					if (mListener != null) {
						mListener.onGridContainerTouchMove(offsetX);
					}
					break;
				case MotionEvent.ACTION_UP :
				case MotionEvent.ACTION_CANCEL :
					if (mListener != null) {
						mListener.onGridContainerTouchUp(ev.getRawX());
					}
					mTouchX = 0;
					break;

				default :
					break;
			}
			ret = true;
		} else {
			ret = super.onTouchEvent(ev);
		}
		return ret;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean ret = false;
		int action = ev.getAction() & MotionEvent.ACTION_MASK;
		if (GLAppDrawerMainView.isSlideMenuShow()) {
			// 侧边栏已经展开了
			switch (action) {
				case MotionEvent.ACTION_DOWN :
					ret = true;
					break;
				case MotionEvent.ACTION_MOVE :
					ret = true;
					break;
				case MotionEvent.ACTION_UP :
					ret = true;
					break;

				default :
					break;
			}
		} else {
			int x = (int) ev.getX();
			int y = (int) ev.getY();
			if (action == MotionEvent.ACTION_DOWN) {
				RectF rect = getTriggerShowSlideMenuArea();
				if (rect.contains(x, y)) {
					if (mListener != null) {
						mListener.onGridContainerTriggerShowSlideMenuArea();
					}
					ret = true;
				}
			}
		}
		return ret;
	}
	
	private RectF getTriggerShowSlideMenuArea() {
		float left = 0;
		float top = 0;
		float bottom = GoLauncherActivityProxy.getScreenHeight();
		float scale = 0.03f;
		final String samsung = "samsung";
		String brand = android.os.Build.BRAND;
		String manufacturer = android.os.Build.MANUFACTURER;
		if ((brand != null && brand.toLowerCase().contains(samsung))
				|| (manufacturer != null && manufacturer.toLowerCase().contains(samsung))
				|| Machine.isONE_X()) {
			scale = 0.05f;
		}
		float right = 0;
		if (GoLauncherActivityProxy.isPortait()) {
			right = GoLauncherActivityProxy.getScreenWidth() * scale;
		} else {
			right = GoLauncherActivityProxy.getScreenHeight() * scale;
		}
		return new RectF(left, top, right, bottom);
	}
	
	public void setValueAnimation(InterpolatorValueAnimation mSlideAnimation) {
		mAnimation = mSlideAnimation;
	}

	public void startSlideAnimation(InterpolatorValueAnimation mSlideAnimation) {
		mAnimation = mSlideAnimation;
		invalidate();
	}

	public void setGridContainerTouchListener(GridContainerTouchListener listener) {
		mListener = listener;
	}
	
	@Override
	public void setAlpha(int alpha) {
		mCurrentAlpha = alpha;
		super.setAlpha(alpha);
	}

	public void resetAlpha() {
		mCurrentAlpha = FULL_ALPHA;
	}

	public void setSidebarShowPersent(float persent) {
		setBlurAlphaProportion(persent);
		int alpha = (int) (FULL_ALPHA - (FULL_ALPHA - END_ALPHA) * persent);
		setAlpha(alpha);
	}
}
