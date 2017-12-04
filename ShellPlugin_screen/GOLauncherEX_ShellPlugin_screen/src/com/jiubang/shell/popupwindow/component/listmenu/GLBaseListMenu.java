package com.jiubang.shell.popupwindow.component.listmenu;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.widget.GLListView;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.SettingProxy;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.popupwindow.PopupWindowControler;
import com.jiubang.shell.popupwindow.component.GLPopupWindowLayer;
import com.jiubang.shell.theme.GLAppDrawerThemeControler;
import com.jiubang.shell.theme.ShellThemeManager;

/**
 * 菜单基类
 */
public class GLBaseListMenu extends GLListView  {
	protected static final long ANIMATION_DURATION = 100;
	protected Drawable mMenuBgV; // 背景(竖屏)
	protected Drawable mMenuDividerV; // 分割线（竖屏）
	/**
	 * 菜单项被选中后的背景图
	 */
	protected Drawable mItemSelectedBg;
	protected int mTextColor; // 颜色
	protected GLBaseMenuAdapter mAdapter;
	private boolean mInitialized;

	/**
	 * 程序上下文
	 */
	protected Activity mActivity;
	/**
	 * 主题控制器
	 */
	protected GLAppDrawerThemeControler mThemeCtrl;
	private PopupWindowControler mPopupWindowControler;

	public GLBaseListMenu() {
		super(ShellAdmin.sShellManager.getContext());
		mActivity = ShellAdmin.sShellManager.getActivity();
		mThemeCtrl = GLAppDrawerThemeControler.getInstance(mContext);
		loadThemeResource();
		mPopupWindowControler = ShellAdmin.sShellManager.getShell().getPopupWindowControler();
		setHasPixelOverlayed(false);
	}

	protected void loadThemeResource() {
		String curPackageName = ShellThemeManager
				.getInstance(mContext).getCurrentThemePackage();
		String packageName = null;
		if (!curPackageName.equals(SettingProxy.getFunAppSetting()
				.getTabHomeBgSetting())) {
			packageName = SettingProxy.getFunAppSetting()
					.getTabHomeBgSetting();
		}
		if (!GoAppUtils.isAppExist(mActivity, packageName)) {
			packageName = curPackageName;
		}
		mMenuBgV = mThemeCtrl.getDrawable(mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuBgV,
				packageName, R.drawable.gl_list_menu_bg_vertical);
		mMenuDividerV = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuDividerV, packageName,
				R.drawable.gl_list_menu_line);
		mTextColor = mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuTextColor;
		mItemSelectedBg = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuItemSelected, packageName,
				R.drawable.gl_list_menu_item_background);
	}
	
	private void initialize() {
		mAdapter.setTextColor(mTextColor);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		if (mItemSelectedBg != null) {
			setSelector(mItemSelectedBg);
		}
		setLayoutParams(layoutParams);
		setAdapter(mAdapter);
		setAlwaysDrawnWithCacheEnabled(true);
		setSelectionAfterHeaderView();
		setSmoothScrollbarEnabled(true);
		mInitialized = true;
	}
	
	public void setMenuAdapter(GLBaseMenuAdapter adapter) {
		if (adapter == null) {
			return;
		}
		mAdapter = adapter;
	}
	
	public void setItemPadding(int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
		mAdapter.setItemPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		try {
			if (ev.getX() > getLeft() && ev.getX() < getRight() && ev.getY() > getTop()
					&& ev.getY() < getBottom()) {
				return super.onTouchEvent(ev);
			} else {
				if (ev.getAction() == MotionEvent.ACTION_UP) {
					mPopupWindowControler.dismiss(true);
				}
			}
			return super.onTouchEvent(ev);
		} catch (Exception e) {
			return false;
		}

	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			return false;
		}
		if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_ENTER
				|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			mPopupWindowControler.dismiss(true);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			mPopupWindowControler.dismiss(true);
			return true;
		}
		return false;
	}

	public void cleanup() {
		super.cleanup();
		mAdapter = null;
		mActivity = null;
		mThemeCtrl = null;
	}
	
	/**
	 * 菜单显示时调用，处理背景图和分割线
	 */
	public void onShow() {
		clearFocus(); // 防止快速双击菜单残留点击颜色
		if (!mInitialized) {
			initialize();
		}
		if (GoLauncherActivityProxy.isPortait()) {
			if (mMenuBgV != null) {
				setBackgroundDrawable(mMenuBgV);
			}

			if (mMenuDividerV != null) {
				setDivider(mMenuDividerV);
			}
		} else {
			if (mMenuBgV != null) {
				setBackgroundDrawable(mMenuBgV);
			}

			if (mMenuDividerV != null) {
				setDivider(mMenuDividerV);
			}
		}
		setFooterDividersEnabled(false);
	}
	
	public void doShowAnimation(final GLPopupWindowLayer layer, boolean distinctionOrientation) {
		float pointXValue = 0.7f;
		//modified by zhangxi @2013-09-04
		float pointYValue = 0.0f;
//		if (!GoLauncherActivityProxy.isPortait() && distinctionOrientation) {
//			pointXValue = 0.0f;
//			pointYValue = 1.0f;
//		}
		doShowAnimation(layer, pointXValue, pointYValue);
	}
	
	public void doShowAnimation(final GLPopupWindowLayer layer, float pointXValue, float pointYValue) {
		onShow();
		Animation scaleAnimation = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f,
				Animation.RELATIVE_TO_SELF, pointXValue, Animation.RELATIVE_TO_SELF, pointYValue);
		AnimationSet animationSet = new AnimationSet(true);
		Animation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
		animationSet.addAnimation(scaleAnimation);
		animationSet.addAnimation(alphaAnimation);
		animationSet.setDuration(ANIMATION_DURATION);
		AnimationListener listener = new AnimationListenerAdapter() {

			@Override
			public void onAnimationEnd(Animation animation) {
				if (layer != null) {
					layer.onEnter();
				}
			}
		};
		AnimationTask animationTask = new AnimationTask(this, animationSet, listener, true,
				AnimationTask.PARALLEL);
		GLAnimationManager.startAnimation(animationTask);
	}
	
	public void doHideAnimation(final GLPopupWindowLayer layer, boolean distinctionOrientation) {
		float pointXValue = 0.7f;
		//modified by zhangxi @2013-09-04
		float pointYValue = 0.0f;
//		if (!GoLauncherActivityProxy.isPortait() && distinctionOrientation) {
//			pointXValue = 0.0f;
//			pointYValue = 1.0f;
//		}
		doHideAnimation(layer, pointXValue, pointYValue);
	}
	
	public void doHideAnimation(final GLPopupWindowLayer layer, float pointXValue, float pointYValue) {
		Animation scaleAnimation = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
				Animation.RELATIVE_TO_SELF, pointXValue, Animation.RELATIVE_TO_SELF, pointYValue);
		AnimationSet animationSet = new AnimationSet(true);
		Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
		animationSet.addAnimation(scaleAnimation);
		animationSet.addAnimation(alphaAnimation);
		animationSet.setDuration(ANIMATION_DURATION);
		animationSet.setFillAfter(true);
		AnimationListener listener = new AnimationListenerAdapter() {

			@Override
			public void onAnimationEnd(Animation animation) {
				post(new Runnable() {

					@Override
					public void run() {
						if (layer != null) {
							layer.onExit();
						}
					}
				});
			}
		};
		AnimationTask animationTask = new AnimationTask(this, animationSet, listener, true,
				AnimationTask.PARALLEL);
		GLAnimationManager.startAnimation(animationTask);
	}
	
}
