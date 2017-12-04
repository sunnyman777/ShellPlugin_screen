package com.jiubang.shell.appdrawer.component;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;

import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLView;
import com.jiubang.shell.IShell;
import com.jiubang.shell.appdrawer.IActionBar;
import com.jiubang.shell.folder.BaseFolderIcon;
import com.jiubang.shell.folder.GLAppFolderMainView;
import com.jiubang.shell.folder.GLAppFolderMainView.FolderStatusListener;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.theme.GLAppDrawerThemeControler;

/**
 * 功能表工具条容器
 * @author wuziyi
 *
 */
public abstract class GLBarContainer extends GLFrameLayout implements FolderStatusListener {
	protected GLLayoutInflater mInflater;
	protected GLAppDrawerThemeControler mThemeCtrl;
	protected IShell mShell;
	protected ArrayList<IActionBar> mBarViewGroup;
	protected IActionBar mCurrentBar;
	protected boolean mIsVerticalMode;
	
	public GLBarContainer(Context context) {
		this(context, null);
		init();
	}

	public GLBarContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mThemeCtrl = GLAppDrawerThemeControler.getInstance(mContext);
		mInflater = ShellAdmin.sShellManager.getLayoutInflater();
	}

	public void setShell(IShell shell) {
		mShell = shell;
	}

	public void setBarViewGroup(ArrayList<IActionBar> barViewGroup) {
		if (barViewGroup != null && !barViewGroup.isEmpty()) {
			removeAllBarViews();
			mBarViewGroup = barViewGroup;
			mCurrentBar = barViewGroup.get(0);
			for (IActionBar bar : barViewGroup) {
				addBarView(bar);
			}
		}
	}
	
	@Override
	public void cancelLongPress() {
		if (mBarViewGroup != null) {
			for (IActionBar bar : mBarViewGroup) {
				if (bar instanceof GLView) {
					GLView child = (GLView) bar;
					child.cancelLongPress();
				}
			}
		}
		super.cancelLongPress();
	}
	
	protected void addBarView(IActionBar bar) {
		addView((GLView) bar, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	protected void removeBarView(IActionBar bar) {
		removeView((GLView) bar);
	}

	protected void removeAllBarViews() {
		removeAllViews();
	}
	
	public void setSidebarShowPersent(float persent) {
		
	}

	@Override
	public void onFolderOpen(BaseFolderIcon<?> baseFolderIcon, boolean animate,
			int curStatus, boolean reopen) {
//		for (IActionBar bar : mBarViewGroup) {
//			if (bar instanceof FolderStatusListener) {
//				((FolderStatusListener) bar).onFolderOpen(baseFolderIcon, offsetUp, offsetDown,
//						curStatus, reopen);
//			}
//		}
		AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
		alphaAnim.setFillAfter(true);
		alphaAnim.setDuration(GLAppFolderMainView.sFolderAnimationDuration);
		alphaAnim.setAnimationListener(new AnimationListenerAdapter() {
			@Override
			public void onAnimationEnd(Animation animation) {
				post(new Runnable() {

					@Override
					public void run() {
						setVisible(false);
					}
				});
			}
		});
		startAnimation(alphaAnim);
	}

	@Override
	public void onFolderClose(BaseFolderIcon<?> baseFolderIcon, boolean animate,
			int curStatus) {
//		for (IActionBar bar : mBarViewGroup) {
//			if (bar instanceof FolderStatusListener) {
//				((FolderStatusListener) bar).onFolderClose(baseFolderIcon, offsetUp, offsetDown,
//						curStatus);
//			}
//		}
		if (!isVisible()) {
			setVisible(true);
		}
		if (animate) {
			AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
			alphaAnim.setFillAfter(true);
			alphaAnim.setDuration(GLAppFolderMainView.sFolderAnimationDuration);
			startAnimation(alphaAnim);
		} else {
			clearAnimation();
		}
	}

	@Override
	public void onFolderStatusChange(int oldStatus, int newStatus) {
		for (IActionBar bar : mBarViewGroup) {
			if (bar instanceof FolderStatusListener) {
				((FolderStatusListener) bar).onFolderStatusChange(oldStatus, newStatus);
			}
		}
	}
	
	@Override
	public void onFolderReLayout(BaseFolderIcon<?> baseFolderIcon, int curStatus) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * <br>功能简述:处理横竖屏
	 * <br>功能详细描述:显示的当前屏应处理横竖屏情况
	 * <br>注意:
	 * @author zhangxi
	 * @data:2013-09-06
	 */
	public void onConfigurationChanged() {
		if (mBarViewGroup != null && !mBarViewGroup.isEmpty()) {
			for (IActionBar bar : mBarViewGroup) {
				bar.onConfigurationChanged();
			}
		}
	}

	
	public void setVerticalMode(boolean isVertical) {
		mIsVerticalMode = true;
	}

	public boolean isVerticalMode() {
		return mIsVerticalMode;
	}
	
	public abstract void switchBarView(IActionBar barView, boolean animate);
	
	public abstract void translateInAnimation(long duration);
	
	public abstract void translateOutAnimation(long duration);

}
