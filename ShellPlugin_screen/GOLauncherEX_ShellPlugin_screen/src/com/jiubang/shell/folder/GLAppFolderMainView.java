package com.jiubang.shell.folder;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.view.GLRelativeLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLImageView;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.GoLauncherLogicProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.util.graphics.DrawUtils;
import com.golauncher.message.IAppDrawerMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.ggheart.appgame.base.bean.AppDetailInfoBean;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogStatusObserver;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.pref.PrefConst;
import com.jiubang.ggheart.apps.desks.diy.pref.PrivatePreference;
import com.jiubang.ggheart.components.renamewindow.RenameActivity;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.GestureSettingInfo;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.data.statistics.IGoLauncherUserBehaviorStatic;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderController;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderInfo;
import com.jiubang.ggheart.plugin.shell.folder.GLDockFolderModifyActivity;
import com.jiubang.ggheart.plugin.shell.folder.GLDrawerFolderModifyActivity;
import com.jiubang.ggheart.plugin.shell.folder.GLScreenFolderModifyActivity;
import com.jiubang.shell.IShell;
import com.jiubang.shell.IView;
import com.jiubang.shell.appdrawer.controler.Status;
import com.jiubang.shell.appdrawer.controler.StatusChangeListener;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.component.ShellContainer;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.folder.smartcard.data.CardBuildInfo;
import com.jiubang.shell.folder.status.FolderStatusManager;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.indicator.DesktopIndicator;
import com.jiubang.shell.model.CommonImageManager;
import com.jiubang.shell.orientation.GLOrientationControler;
import com.jiubang.shell.utils.GLImageUtil;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-3-11]
 */
public class GLAppFolderMainView extends GLRelativeLayout
		implements
			IView,
			StatusChangeListener,
			OnClickListener {

	private ShellTextViewWrapper mFolderNameEdit;

	private GLRelativeLayout mFolderContentLayout;

	private GLAppFolderGridVIewContainer mFolderGridVIewContainer;

	private GLImageView mMenuBtn;

	private GLImageView mEditBtn;

	private ShellTextViewWrapper mFolderModeBtn;

	private BaseFolderIcon<?> mCurFolderIcon;

	private CommonImageManager mImageManager;

	private static final long DEFAULT_FOLDER_ANIM_DURATION = 300;
	public static long sFolderAnimationDuration = DEFAULT_FOLDER_ANIM_DURATION;

	private FolderStatusListener mStatusListener;

	private SparseArray<FolderViewAnimationListener> mAnimationListeners;

	private int mCurStatus;

	private boolean mReOpen;

	private boolean mIsClosing;

	private GLAppFolderExpandContentLayout mFolderExpandContentLayout;

	private boolean mIsFolderOpened = false;

	private boolean mNeedFolderAnimation = true;

	private OpenFolderAnimationTask mOpenFolderAnimationTask;

	public static final int FOLDER_OPEN_ANIMATION = 0x1;
	public static final int FOLDER_CLOSE_ANIMATION = 0x2;

	public GLAppFolderMainView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GLAppFolderMainView(Context context) {
		super(context);
		init();
	}

	private void init() {
		mImageManager = CommonImageManager.getInstance();
		FolderStatusManager.getInstance().registListener(this);
		mAnimationListeners = new SparseArray<GLAppFolderMainView.FolderViewAnimationListener>();
		setPadding(0, StatusBarHandler.getStatusbarHeight(), 0, 0);
		mOpenFolderAnimationTask = new OpenFolderAnimationTask(true, AnimationTask.PARALLEL);
	}
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setHasPixelOverlayed(false);
		mFolderNameEdit = (ShellTextViewWrapper) findViewById(R.id.folder_rename_edittext);
		mFolderNameEdit.setOnClickListener(this);
		mFolderNameEdit.setDispatchTouchEventEnabled(false);

		mFolderContentLayout = (GLRelativeLayout) findViewById(R.id.folder_content_layout);

		mFolderGridVIewContainer = (GLAppFolderGridVIewContainer) findViewById(R.id.folder_grid_view);
		mFolderGridVIewContainer.setIndicator((DesktopIndicator) findViewById(R.id.folder_indicator));

		mFolderExpandContentLayout = (GLAppFolderExpandContentLayout) findViewById(R.id.folder_ad_content_layout);
		updateExpandContent();

		mMenuBtn = (GLImageView) findViewById(R.id.folder_menu_btn);
		mMenuBtn.setOnClickListener(this);

		mEditBtn = (GLImageView) findViewById(R.id.folder_edit_btn);
		mEditBtn.setOnClickListener(this);

		mFolderModeBtn = (ShellTextViewWrapper) findViewById(R.id.folder_mode_btn);
		mFolderModeBtn.setOnClickListener(this);

		int width = (int) (GoLauncherActivityProxy.getScreenWidth() / 3);
		mFolderModeBtn.getLayoutParams().width = width;

		loadResource();
	}

	public void setGameFolderUI(boolean show) {
		mFolderModeBtn.setVisible(show);
		Drawable drawableLeft = null;
		if (show) {
			boolean gameAccleterate = PrivatePreference
					.getPreference(ApplicationProxy.getContext()).getBoolean(
							PrefConst.KEY_GAME_FOLDER_ACCELERATE_SWITCH, true);
			drawableLeft = gameAccleterate ? mContext.getResources().getDrawable(
					R.drawable.gl_folder_accelerate_on) : mContext.getResources().getDrawable(
					R.drawable.gl_folder_accelerate_off);
			updateGameFolderModeText();
		}
		ShellTextViewWrapper viewWrapper = (ShellTextViewWrapper) mFolderContentLayout
				.findViewById(R.id.folder_rename_edittext);
		viewWrapper.getTextView().setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null,
				null);
		viewWrapper.getTextView().setCompoundDrawablePadding(DrawUtils.dip2px(4));
	}

	private void updateGameFolderModeText() {
		Resources resources = mContext.getResources();
		switch (mCurStatus) {
			case FolderStatusManager.GRID_NORMAL_STATUS :
			case FolderStatusManager.GRID_EDIT_STATUS :
				mFolderModeBtn.setText(resources
						.getString(R.string.app_folder_menu_game_mode_normal));
				break;
			case FolderStatusManager.FOLDER_SILENT_MODE_STATUS :
				mFolderModeBtn.setText(resources
						.getString(R.string.app_folder_menu_game_mode_silent));
				break;
			case FolderStatusManager.FOLDER_POWER_SAVING_MODE_STATUS :
				mFolderModeBtn.setText(resources
						.getString(R.string.app_folder_menu_game_mode_power_saving));
				break;
			default :
				break;
		}
	}

	@Override
	public void setVisible(boolean visible, boolean animate, Object obj) {
		if (isVisible() != visible) {
			if (visible) {
				Object[] objects = (Object[]) obj;
				mIsFolderOpened = true;
				onShow(animate, objects);
			} else {
				mIsFolderOpened = false;
				onHide(animate);
			}
		}
	}

	public boolean isFolderOpened() {
		return mIsFolderOpened;
	}

	private void onShow(boolean animate, Object[] objects) {
		GLOrientationControler.keepOrientationAllTheTime(true);
		ShellAdmin.sShellManager.getShell().show(IViewId.PROTECTED_LAYER, false);
		ShellContainer.clearLongPressEvent();
		mCurFolderIcon = (BaseFolderIcon<?>) objects[0];
		if (mCurFolderIcon instanceof GLAppDrawerFolderIcon) {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_FOLDER_ACTION_BAR,
					IAppDrawerMsgId.FOLDER_ACTION_BAR_ICON_ELEMENT_START_DOWN_ANIMATION, -1,
					mCurFolderIcon.getInfo());
		}
		changeStatus(mCurFolderIcon, (Integer) objects[1]);
		updateExpandContent();
		if (animate) {
			mNeedFolderAnimation = true;
			post(new Runnable() {
				@Override
				public void run() {
					setVisible(true);
				}
			});
		} else {
			setVisible(true);
			handleAnimFinish(FOLDER_OPEN_ANIMATION);
		}
		if (mStatusListener != null) {
			mStatusListener.onFolderOpen(mCurFolderIcon, animate, mCurStatus, mReOpen);
		}
		mReOpen = false;
		//		FolderAdController.getInstance().folderAdDataNeedUpdate(FolderAdDataRequestor.TYPE_ID_GAME);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return super.onInterceptTouchEvent(ev);
	}

	//	private void calculateFolderWidth() {
	//		GLRelativeLayout.LayoutParams layoutParams = (LayoutParams) mFolderContentLayout
	//				.getLayoutParams();
	//		if (!GoLauncherActivityProxy.isPortait() && mCurFolderIcon instanceof GLAppDrawerFolderIcon) {
	//			int topBarSize = mContext.getResources().getDimensionPixelSize(
	//					R.dimen.appdrawer_top_bar_container_height);
	//			int bottomBarSize = mContext.getResources().getDimensionPixelSize(
	//					R.dimen.appdrawer_bottom_bar_container_height);
	//			layoutParams.width = StatusBarHandler.getDisplayWidth() - topBarSize - bottomBarSize;
	//			layoutParams.leftMargin = topBarSize;
	//		} else {
	//			layoutParams.width = LayoutParams.MATCH_PARENT;
	//			layoutParams.leftMargin = 0;
	//		}
	//	}

	private void changeStatus(BaseFolderIcon<?> baseFolderIcon, int statusID) {
		if (baseFolderIcon instanceof GLAppDrawerFolderIcon) {
			FolderStatusManager.getInstance().changeStatus(FolderStatusManager.DRAWER_FOLDER_GRID,
					statusID);
			mFolderNameEdit.setText(mCurFolderIcon.getFolderInfo().getAppDrawerFolderInfo()
					.getTitle());
		} else if (baseFolderIcon instanceof GLScreenFolderIcon) {
			FolderStatusManager.getInstance().changeStatus(FolderStatusManager.SCREEN_FOLDER_GRID,
					statusID);
			mFolderNameEdit.setText(mCurFolderIcon.getFolderInfo().getScreenFoIderInfo().mTitle);
		} else if (baseFolderIcon instanceof GLDockFolderIcon) {
			FolderStatusManager.getInstance().changeStatus(FolderStatusManager.DOCK_FOLDER_GRID,
					statusID);
			mFolderNameEdit.setText(mCurFolderIcon.getFolderInfo().getScreenFoIderInfo().mTitle);
		}
	}
	/**
	 * 
	 * @author dingzijian
	 *
	 */
	class OpenFolderAnimationTask extends AnimationTask implements BatchAnimationObserver {
		private static final long ANIMATION_START_OFFSET = 30;
		private long mStartOffset = -ANIMATION_START_OFFSET;
		public OpenFolderAnimationTask(boolean keepOrientation, int type) {
			super(keepOrientation, type);
			setBatchAnimationObserver(this, GLAppFolderMainView.FOLDER_OPEN_ANIMATION);
		}
		void startOpenFolderAnimation() {

			mStartOffset = -ANIMATION_START_OFFSET;
			reset();

			Animation bgAlphaAnimation = new AlphaAnimation(0.0f, 1.0f);
			bgAlphaAnimation.setDuration(GLAppFolderMainView.sFolderAnimationDuration);
			TranslateAnimation folderNameTranslateAnimation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
					Animation.RELATIVE_TO_SELF, -0.5f, Animation.RELATIVE_TO_SELF, 0);
			folderNameTranslateAnimation.setDuration(GLAppFolderMainView.sFolderAnimationDuration);

			addAnimation(GLAppFolderMainView.this, bgAlphaAnimation, null);
			addAnimation(mFolderNameEdit, folderNameTranslateAnimation, null);

			List<GLView> children = mFolderGridVIewContainer.getCurrentScreenIcons();

			float[] scaleXY = mCurFolderIcon.getThumbnailScaleXY();

			int count = 0;
			for (GLView view : children) {
				final IconView<?> iconView = (IconView<?>) view;
				iconView.showTitle(false, false);
				int[] childLocation = new int[2];
				int[] location = new int[2];
				view.getLocationOnScreen(childLocation);
				mCurFolderIcon.getThumbnailLocationCenter(count, location);
				float x = location[0];
				float y = location[1];

				TranslateAnimation translateAnimation = new TranslateAnimation(Animation.ABSOLUTE,
						x - childLocation[0], Animation.ABSOLUTE, 0, Animation.ABSOLUTE, y
								- childLocation[1], Animation.ABSOLUTE, 0);
				ScaleAnimation scaleAnimation = new ScaleAnimation(scaleXY[0], 1.0f, scaleXY[1],
						1.0f, Animation.ABSOLUTE, x - childLocation[0], Animation.ABSOLUTE, y
								- childLocation[1]);

				AnimationSet animationSet = new AnimationSet(false);
				animationSet.addAnimation(translateAnimation);
				animationSet.addAnimation(scaleAnimation);
				if (count >= mFolderGridVIewContainer.getCurGridViewColumns()) {
					AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
					AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator(2.0f);
					alphaAnimation.setInterpolator(accelerateInterpolator);
					animationSet.addAnimation(alphaAnimation);
				}
				animationSet.setDuration(GLAppFolderMainView.sFolderAnimationDuration);
				view.setHasPixelOverlayed(false);
				mStartOffset += ANIMATION_START_OFFSET;
				animationSet.setStartOffset(mStartOffset);
				addAnimation(view, animationSet, new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {

					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}

					@Override
					public void onAnimationProcessing(Animation animation, float interpolatedTime) {

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						if (mCurFolderIcon instanceof GLAppDrawerFolderIcon) {
							if (iconView.isEnableAppName()) {
								iconView.showTitle(true, true);
							}
						} else if (mCurFolderIcon instanceof GLScreenFolderIcon
								|| mCurFolderIcon instanceof GLDockFolderIcon) {
							if (GoLauncherLogicProxy.getIsShowAppTitle()) {
								iconView.showTitle(true, true);
							}
						}

					}
				});
				count++;
			}
			if (isValid()) {
				ShellAdmin.sShellManager.getShell().show(IViewId.PROTECTED_LAYER, false);
				GLAnimationManager.startAnimation(this);
			} else {
				handleAnimFinish(FOLDER_OPEN_ANIMATION);
			}
		}
		@Override
		public void onStart(int what, Object[] params) {

		}
		@Override
		public void onFinish(final int what, Object[] params) {
			switch (what) {
				case FOLDER_OPEN_ANIMATION :
					handleAnimFinish(FOLDER_OPEN_ANIMATION);
					break;
				default :
					break;
			}
		}
	}
	private void handleAnimFinish(int animationType) {
		switch (animationType) {
			case FOLDER_OPEN_ANIMATION :
				if (mAnimationListeners.size() > 0) {
					for (int i = 0; i < mAnimationListeners.size(); i++) {
						mAnimationListeners.valueAt(i).onFolderOpenEnd(mCurStatus);
					}
				}
				ShellAdmin.sShellManager.getShell().onFolderOpened();
				break;
			case FOLDER_CLOSE_ANIMATION :
				if (mAnimationListeners.size() > 0) {
					for (int i = 0; i < mAnimationListeners.size(); i++) {
						mAnimationListeners.valueAt(i).onFolderCloseEnd(mCurStatus, mCurFolderIcon,
								mReOpen);
					}
				}
				setVisible(false);
				ShellAdmin.sShellManager.getShell().onFolderClosed(mReOpen, mCurFolderIcon,
						mCurStatus);
				mIsClosing = false;
				if (mFolderModeBtn.isVisible()
						&& mCurStatus != FolderStatusManager.GRID_EDIT_STATUS) {
					PrivatePreference privatePreference = PrivatePreference
							.getPreference(ApplicationProxy.getContext());
					privatePreference.putInt(PrefConst.KEY_GAME_FOLDER_MODE, mCurStatus);
					privatePreference.commit();
				}
				mFolderExpandContentLayout.removeAllContent();
				break;
			default :
				break;
		}
		GLOrientationControler.keepOrientationAllTheTime(false);
		ShellAdmin.sShellManager.getShell().hide(IViewId.PROTECTED_LAYER, false);
	}

	//	private void activeIconInAnimation() {
	//		mFolderGridVIewContainer.activeIconInAnimation(null);
	//	}

	private void onHide(boolean animate) {
		//		ShellAdmin.sShellManager.getShell().hide(IViewId.PROTECTED_LAYER, false);
		if (mStatusListener != null) {
			mStatusListener.onFolderClose(mCurFolderIcon, animate, mCurStatus);
		}
		if (animate) {
			startFolderCloseAnimation();
		} else {
			handleAnimFinish(FOLDER_CLOSE_ANIMATION);
		}
	}

	private void startFolderCloseAnimation() {
		Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
		alphaAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {
				mIsClosing = true;
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				handleAnimFinish(FOLDER_CLOSE_ANIMATION);
			}

			@Override
			public void onAnimationProcessing(Animation arg0, float arg1) {

			}
		});
		alphaAnimation.setDuration(DEFAULT_FOLDER_ANIM_DURATION);
		startAnimation(alphaAnimation);
	}

	@Override
	public void setShell(IShell shell) {
		FolderStatusManager.getInstance().setShell(shell);
	}

	@Override
	public int getViewId() {

		return IViewId.APP_FOLDER;
	}

	@Override
	public void onAdd(GLViewGroup parent) {

	}

	@Override
	public void onRemove() {

	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return mFolderGridVIewContainer.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return mFolderGridVIewContainer.onKeyDown(keyCode, event);
	}
	@Override
	public void onSceneStatusChange(Status oldStatus, Status curStatus, Object[] objects) {
		mCurStatus = curStatus.getGridStatusID();
		mFolderGridVIewContainer.onSceneStatusChange(oldStatus, curStatus, mCurFolderIcon);
		setFolderTypeUI();
	}

	private void setFolderTypeUI() {
		switch (mCurFolderIcon.getFolderInfo().folderType) {
			case GLAppFolderInfo.NO_RECOMMAND_FOLDER :
				mFolderExpandContentLayout.setVisibility(GLView.GONE);
				setGameFolderUI(false);
				return;
			case GLAppFolderInfo.TYPE_RECOMMAND_GAME :
				setGameFolderUI(true);
				break;
			default :
				//				if (GoLauncherActivityProxy.isPortait() && mFolderGridVIewContainer.getCurGridViewRows() <= 2
				//						&& isAdDataReady()) {
				//					mFolderExpandContentLayout.setVisibility(View.VISIBLE);
				//					//文件夹广告打开统计
				//					GuiThemeStatistics.folderAdStaticData("", "g001", 1);
				setGameFolderUI(false);
				break;
		}
		mFolderExpandContentLayout.setVisibility(View.VISIBLE);
		//		mFolderAdGridView.initGridView(mCurFolderIcon,
		//				mFolderGridVIewContainer.getCurGridViewColumns());
	}

	private boolean isAdFolder() {
		switch (mCurFolderIcon.getFolderInfo().folderType) {
			case GLAppFolderInfo.NO_RECOMMAND_FOLDER :
				return false;
			default :
				return true;
		}
	}

	private boolean isAdDataReady() {
		GLAppFolderInfo appFolderInfo = mCurFolderIcon.getFolderInfo();
		SparseArray<ArrayList<AppDetailInfoBean>> folderAdData = appFolderInfo.getFolderAdData();
		if (folderAdData != null && folderAdData.size() > 0) {
			return true;
		}
		return false;
	}
	@Override
	public void onGridStatusChange(Status oldStatus, Status newStatus, Object[] objects) {
		mCurStatus = newStatus.getGridStatusID();
		if (mStatusListener != null) {
			mStatusListener.onFolderStatusChange(oldStatus.getGridStatusID(),
					newStatus.getGridStatusID());
		}
		mFolderGridVIewContainer.onGridStatusChange(oldStatus, newStatus);
		updateGameFolderModeText();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (!super.dispatchTouchEvent(ev)) {
			mCurFolderIcon.closeFolder(true);
		}
		return true;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mNeedFolderAnimation) {
			mOpenFolderAnimationTask.startOpenFolderAnimation();
			mNeedFolderAnimation = false;
		}
		Drawable bg = getBackground();
		if (bg != null) {
			bg.setBounds(0, 0, getWidth(), getHeight() + DrawUtils.getNavBarHeight());
		}
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  dingzijian
	 * @date  [2013-3-15]
	 */
	public interface FolderStatusListener {
		public void onFolderOpen(BaseFolderIcon<?> baseFolderIcon, boolean animate, int curStatus, boolean reopen);

		public void onFolderClose(BaseFolderIcon<?> baseFolderIcon, boolean animate, int curStatus);

		public void onFolderStatusChange(int oldStatus, int newStatus);

		public void onFolderReLayout(BaseFolderIcon<?> baseFolderIcon, int curStatus);
	}
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  dingzijian
	 * @date  [2013-3-18]
	 */
	public interface FolderViewAnimationListener {
		public void onFolderOpenEnd(int curStatus);

		public void onFolderCloseEnd(int curStatus, BaseFolderIcon<?> baseFolderIcon,
				boolean needReopen);
	}
	void setFolderStatusListener(FolderStatusListener statusListener) {
		this.mStatusListener = statusListener;
	}
	void addFolderViewAnimationListener(FolderViewAnimationListener animationListener, int viewID) {
		if (animationListener != null) {
			mAnimationListeners.put(viewID, animationListener);
		}
	}

	void removeFolderViewAnimationListener(int viewID) {
		mAnimationListeners.remove(viewID);
	}
	@Override
	public void onClick(GLView v) {
		DialogStatusObserver observer = DialogStatusObserver.getInstance();
		if (observer.isDialogShowing()) {
			return;
		}
		switch (v.getId()) {
			case R.id.folder_rename_edittext :
				if (SettingProxy.getScreenSettingInfo().mLockScreen) { // 判断当前是否锁屏
					LockScreenHandler.showLockScreenNotification(ShellAdmin.sShellManager
							.getActivity());
					return;
				}
				Intent intent = new Intent(ApplicationProxy.getContext(), RenameActivity.class);
				intent.putExtra(RenameActivity.NAME, mFolderNameEdit.getText());
				intent.putExtra(RenameActivity.HANDLERID, IDiyFrameIds.APP_FOLDER);
				intent.putExtra(RenameActivity.ITEMID, mCurFolderIcon.getFolderInfo().folderId);
				intent.putExtra(RenameActivity.SHOW_RECOMMENDEDNAME, true);
				intent.putExtra(RenameActivity.FINISH_WHEN_CHANGE_ORIENTATION, false);
				ShellAdmin.sShellManager.getShell().startActivityForResultSafely(intent,
						IRequestCodeIds.REQUEST_RENAME);
				GuiThemeStatistics
						.goLauncherUserBehaviorStaticDataCache(IGoLauncherUserBehaviorStatic.FOLDER_OPEN_01);
				break;
			case R.id.folder_menu_btn :
				ShellAdmin.sShellManager.getShell().getPopupWindowControler()
						.showAppFolderMenu(true, mFolderGridVIewContainer);
				break;
			case R.id.folder_edit_btn :
				if (SettingProxy.getScreenSettingInfo().mLockScreen) { // 判断当前是否锁屏
					LockScreenHandler.showLockScreenNotification(ShellAdmin.sShellManager
							.getActivity());
					return;
				}
				Intent newFolderIntent = null;
				Bundle bundle = new Bundle();
				if (mCurFolderIcon instanceof GLAppDrawerFolderIcon) {
					newFolderIntent = new Intent(ApplicationProxy.getContext(),
							GLDrawerFolderModifyActivity.class);
					//					newFolderIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					bundle.putLong(GLDrawerFolderModifyActivity.FOLDER_ID,
							mCurFolderIcon.getFolderInfo().folderId);
				} else if (mCurFolderIcon instanceof GLScreenFolderIcon) {
					newFolderIntent = new Intent(ApplicationProxy.getContext(),
							GLScreenFolderModifyActivity.class);
					bundle.putLong(GLScreenFolderModifyActivity.FOLDER_ID,
							mCurFolderIcon.getFolderInfo().folderId);
				} else {
					newFolderIntent = new Intent(ApplicationProxy.getContext(),
							GLDockFolderModifyActivity.class);
					bundle.putLong(GLDockFolderModifyActivity.FOLDER_ID,
							mCurFolderIcon.getFolderInfo().folderId);
				}
				newFolderIntent.putExtras(bundle);
				ShellAdmin.sShellManager.getShell().startActivityForResultSafely(newFolderIntent,
						IRequestCodeIds.REQUEST_MODIFY_FOLDER);
				GuiThemeStatistics
						.goLauncherUserBehaviorStaticDataCache(IGoLauncherUserBehaviorStatic.FOLDER_OPEN_03);
				break;
			case R.id.folder_mode_btn :
				ShellAdmin.sShellManager.getShell().getPopupWindowControler()
						.showAppFolderGameModeMenu(true, mFolderGridVIewContainer);
				break;
			default :
				break;
		}
	}

	public void onNameChange(String name, long itemId) {
		if (mFolderNameEdit == null || mFolderGridVIewContainer == null) {
			return;
		}
		if (isVisible() && mCurFolderIcon.getFolderInfo().folderId == itemId) {
			mFolderNameEdit.setText(name);
			mFolderGridVIewContainer.onFolderNameChange(name, itemId);
		} else if (!isVisible()) {
			//文件夹关闭的情况下更改名字，只有桌面文件夹才会出现。
			GLAppFolderController folderController = GLAppFolderController.getInstance();
			GLAppFolderInfo folderInfo = folderController.getFolderInfoById(itemId,
					GLAppFolderInfo.FOLDER_FROM_SCREEN);
			if (folderInfo != null) {
				GLScreenFolderIcon folderIcon = (GLScreenFolderIcon) GLAppFolder.getInstance()
						.getFolderIcon(folderInfo);
				folderIcon.getInfo().mTitle = name;
				folderIcon.getInfo().setFeatureTitle(name);
				folderIcon.refreshIcon();
				folderController.updateScreenFolderItem(folderIcon.getInfo());
			}
		} else {
			mFolderGridVIewContainer.onFolderContentNameChange(name, itemId);
		}
	}

	public void onFolderModify() {
		//如果是功能表的修改文件夹，功能表layout后会发消息过来，不需要主动onfoldermodify
		if (!(mCurFolderIcon instanceof GLAppDrawerFolderIcon)) {
			if (isVisible()) {
				relayoutFolder(mCurFolderIcon);
			}
		}
		//		if (isVisible()) {
		//			changeStatus(mCurFolderIcon, FolderStatusManager.GRID_NORMAL_STATUS);
		//		}
	}

	public void relayoutFolder(BaseFolderIcon<?> baseFolderIcon) {
		if (baseFolderIcon != null) {
			mCurFolderIcon = baseFolderIcon;
		}
		mCurFolderIcon.refreshIcon();
		mFolderGridVIewContainer.refreshFolderGridView();
		setFolderTypeUI();
		requestLayout();
		if (mStatusListener != null) {
			mStatusListener.onFolderReLayout(mCurFolderIcon, mCurStatus);
		}
	}

	public void onConfigurationChanged(int param) {
		updateExpandContent();
	}

	public void keepOpenFolder(boolean keep, Object dragInfo) {
		if (isVisible() && !mIsClosing) {
			mFolderGridVIewContainer.keepOpenFolder(keep, dragInfo);
		}
	}

	public void onFolderContentIconChange(Bundle iconBundle) {
		mFolderGridVIewContainer.onFolderContentIconChange(iconBundle);
	}

	public void onFolderContentUninstall(ArrayList<AppItemInfo> uninstallapps) {
		mFolderGridVIewContainer.onFolderContentUninstall(uninstallapps);
	}

	public void onFolderLocateApp(Intent intent) {
		mFolderGridVIewContainer.onFolderLocateApp(intent);
	}

	public BaseFolderIcon<?> getCurFolderIcon() {
		return mCurFolderIcon;
	}

	public int getFolderFrom() {
		return mCurFolderIcon.getFolderInfo().folderFrom;
	}

	public boolean onHomeAction(GestureSettingInfo info) {
		if (isVisible()) {
			return mFolderGridVIewContainer.onHomeAction(info);
		}
		return false;
	}

	public boolean isClosing() {
		return mIsClosing;
	}

	public GLAppFolderGridVIewContainer getGLAppFolderGridVIewContainer() {
		return mFolderGridVIewContainer;
	}

	public void setForceRefreshGLAppFolderGrid() {
		mFolderGridVIewContainer.setFocreSetGridView();
	}

	public void loadResource() {
		loadButtonResource();
	}

	private void loadButtonResource() {
		Drawable drawable = GLImageUtil.getGLDrawable(R.drawable.gl_folder_plus_btn);
		if (drawable != null) {
			mEditBtn.setBackgroundDrawable(getBtnBg());
			mEditBtn.setImageDrawable(drawable);
		}
		drawable = GLImageUtil.getGLDrawable(R.drawable.gl_folder_menu_btn);
		if (drawable != null) {
			mMenuBtn.setBackgroundDrawable(getBtnBg());
			mMenuBtn.setImageDrawable(drawable);
		}
	}

	private Drawable getBtnBg() {
		StateListDrawable stateDrawableBg = new StateListDrawable();
		Drawable pressBgDrawable = mContext.getResources().getDrawable(
				R.drawable.gl_allapp_bg_light);
		stateDrawableBg.addState(new int[] { android.R.attr.state_pressed }, pressBgDrawable);
		return stateDrawableBg;
	}

	void onResume() {
		mFolderGridVIewContainer.onResume();
	}

	private void updateExpandContent() {
		if (mCurFolderIcon != null) {
			if (GoLauncherActivityProxy.isPortait()) {
				mFolderExpandContentLayout.setVisibility(GLView.VISIBLE);
				GLAppFolderInfo appFolderInfo = mCurFolderIcon.getFolderInfo();
				CardBuildInfo cardBuildInfo = new CardBuildInfo(
						mFolderGridVIewContainer.getCurGridViewColumns(), appFolderInfo.folderType,
						appFolderInfo.folderId, appFolderInfo.getFolderAppItemInfos());
				mFolderExpandContentLayout.updateExpandContent(cardBuildInfo);
			} else {
				mFolderExpandContentLayout.setVisibility(GLView.GONE);
			}
		}
	}
}
