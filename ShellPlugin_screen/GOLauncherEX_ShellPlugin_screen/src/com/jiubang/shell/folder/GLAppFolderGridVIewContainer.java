package com.jiubang.shell.folder;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.GestureSettingInfo;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderInfo;
import com.jiubang.shell.appdrawer.controler.Status;
import com.jiubang.shell.folder.status.FolderStatus;
import com.jiubang.shell.folder.status.FolderStatusManager;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.indicator.DesktopIndicator;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-3-11]
 */
public class GLAppFolderGridVIewContainer extends GLFrameLayout {

	private GLAppFolderBaseGridView<?> mCurGridView;

	private DesktopIndicator mIndicator;

	private boolean mFocreSetGridView = false;

	private GLFrameLayout mBackgroundLayout;

	public GLAppFolderGridVIewContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if (mBackgroundLayout == null) {
			mBackgroundLayout = new GLFrameLayout(mContext);
			LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT, Gravity.CENTER);
			mBackgroundLayout.setLayoutParams(layoutParams);
			mBackgroundLayout.setBackgroundResource(R.drawable.gl_folder_edit_bg);
			mBackgroundLayout.setHasPixelOverlayed(false);
			mBackgroundLayout.setVisibility(View.INVISIBLE);
		}
	}

	private void setGridView(GLAppFolderBaseGridView gridView) {
		if (mCurGridView != gridView || mFocreSetGridView) {
			removeAllViews();
			addView(mBackgroundLayout);
			mCurGridView = gridView;
			addView(gridView);
			mCurGridView.setIndicator(mIndicator);
			mFocreSetGridView = false;
		}
	}

	//	public GLAppFolderBaseGridView getCurGridView() {
	//		return mCurGridView;
	//	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mCurGridView != null) {
			return mCurGridView.onKeyDown(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (mCurGridView != null) {
			switch (keyCode) {
				case KeyEvent.KEYCODE_MENU :
					ShellAdmin.sShellManager.getShell().getPopupWindowControler()
							.showAppFolderMenu(true, this);
					return true;

				default :
					break;
			}
			return mCurGridView.onKeyUp(keyCode, event);
		}
		return super.onKeyUp(keyCode, event);
	}

	public void onGridStatusChange(Status oldStatus, Status newStatus) {
		GLAppFolderBaseGridView folderGridView = (GLAppFolderBaseGridView) newStatus.getGridView();
		folderGridView.setStatus((FolderStatus) newStatus);
	}
	
	public void onSceneStatusChange(Status oldStatus, Status newStatus, BaseFolderIcon<?> folderIcon) {
		initGridView(newStatus, folderIcon);
	}
	
	public void backgroudAnimation(int gridStatusId) {
		switch (gridStatusId) {
			case FolderStatusManager.GRID_EDIT_STATUS :
				mBackgroundLayout.setVisible(true);
				AlphaAnimation editAlphaAnimation = new AlphaAnimation(0.0f, 1.0f);
				editAlphaAnimation.setDuration(300);
				mBackgroundLayout.startAnimation(editAlphaAnimation);
				break;
			case FolderStatusManager.GRID_NORMAL_STATUS : {
				if (!mBackgroundLayout.isVisible()) {
					return;
				}
				AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
				alphaAnimation.setDuration(300);
				AnimationListener animationListener = new AnimationListenerAdapter() {

					@Override
					public void onAnimationEnd(Animation animation) {
						post(new Runnable() {

							@Override
							public void run() {
								mBackgroundLayout.setVisible(false);
							}
						});
					}
				};
				alphaAnimation.setAnimationListener(animationListener);
				mBackgroundLayout.startAnimation(alphaAnimation);
			}
			default :
				break;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initGridView(Status newStatus, BaseFolderIcon<?> folderIcon) {
		GLAppFolderBaseGridView folderGridView = (GLAppFolderBaseGridView) newStatus.getGridView();
		List<?> folderContent = null;
		GLAppFolderInfo folderInfo = folderIcon.getFolderInfo();
		switch (folderInfo.folderFrom) {
			case GLAppFolderInfo.FOLDER_FROM_APPDRAWER :
				folderContent = folderInfo.getAppDrawerFolderInfo().getFolderContent();
				break;
			default :
				folderContent = folderInfo.getScreenFoIderInfo().getContents();
				break;
		}
		folderGridView.setFolderIcon(folderIcon);
		folderGridView.setTag(folderContent);
		setGridView(folderGridView);
		folderGridView.setStatus((FolderStatus) newStatus);
		folderGridView.setData(folderContent);
	}

	public void onFolderNameChange(String name, long folderID) {
		if (mCurGridView != null && mCurGridView.getFolderIcon() != null) {
			mCurGridView.onFolderNameChange(name);
		}
	}

	public void onFolderContentNameChange(String name, long itemId) {
		mCurGridView.onFolderContentNameChange(name, itemId);
	}

	public int getFolderGridHeight() {
		return mCurGridView.getLayoutParams().height;
	}

	public int getFolderGridRows() {
		return mCurGridView.getNumRows();
	}
	public void showSortDialog() {
		mCurGridView.showSortDialog();
	}

	public void refreshFolderGridView() {
		mCurGridView.refreshGridView();
	}
	public void keepOpenFolder(boolean keep, Object dragInfo) {
		mCurGridView.keepOpenFolder(keep, dragInfo);
	}

	public void onFolderContentIconChange(Bundle bundle) {
		mCurGridView.onFolderContentIconChange(bundle);
	}

	public void onFolderContentUninstall(ArrayList<AppItemInfo> uninstallapps) {
		mCurGridView.onFolderContentUninstall(uninstallapps);
	}

	public void onFolderLocateApp(Intent intent) {
		if (mCurGridView != null) {
			mCurGridView.onFolderLocateApp(intent);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		return true;
	}
	public boolean onHomeAction(GestureSettingInfo info) {
		if (mCurGridView != null) {
			return mCurGridView.onHomeAction(info);
		}
		return false;
	}

	public DesktopIndicator getIndicator() {

		return mIndicator;

	}

	public DesktopIndicator setIndicator(DesktopIndicator indicator) {
		return mIndicator = indicator;
	}

	public int getCurGridViewColumns() {
		return mCurGridView.getNumColumns();
	}

	public int getCurGridViewRows() {
		return mCurGridView.getNumRows();
	}
	
	public void setFocreSetGridView() {
		this.mFocreSetGridView = true;
	}
	
	public List<GLView> getCurrentScreenIcons() {
		return mCurGridView.getCurScreenIcons();
	}
	
	public int getCurrentFolderType() {
		return mCurGridView.getFolderIcon().getFolderInfo().folderType;
	}
	
	void onResume() {
		if (mCurGridView != null) {
			mCurGridView.onResume();
		}
	}
}
