package com.jiubang.shell.appdrawer.component;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;

import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.view.GLView;
import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.IAppDrawerMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.shell.appdrawer.IActionBar;
import com.jiubang.shell.appdrawer.allapp.actionbar.GLAllAppBottomActionBar;
import com.jiubang.shell.appdrawer.controler.StatusFactory;
import com.jiubang.shell.common.component.GLActionBar;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.folder.BaseFolderIcon;

/**
 * 功能表的底部容器
 * @author wuziyi
 *
 */
public class GLBottomBarContainer extends GLBarContainer implements BatchAnimationObserver {
	private static final int ANIMATION_TYPE_INOUT = 0;
	private static final int ANIMATION_DURATION = 200;

	private boolean mOnInOutAnimationFinish;

	public GLBottomBarContainer(Context context) {
		this(context, null);
		init();
	}

	public GLBottomBarContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	private void init() {
		setHasPixelOverlayed(false);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		//setVerticalMode(GoLauncherActivityProxy.isPortait());
		setVerticalMode(true);
		if (mBarViewGroup != null) {
			int size = mBarViewGroup.size();
			for (int i = 0; i < size; i++) {
				
				GLView bar = (GLView) mBarViewGroup.get(i);
				if (bar instanceof GLAllAppBottomActionBar) {
					continue;
				}
				bar.layout(0, 0, mWidth, mHeight);
				if (bar == mCurrentBar) {
					bar.setVisible(true);
					if (mOnInOutAnimationFinish) {
						if (bar instanceof IActionBar) {
							((IActionBar) bar).onInOutAnimationEnd(true);
						}
					}
				} else {
					bar.setVisible(false);
					if (mOnInOutAnimationFinish) {
						if (bar instanceof IActionBar) {
							((IActionBar) bar).onInOutAnimationEnd(false);
						}
					}
				}
			}
			if (mOnInOutAnimationFinish) {
				mOnInOutAnimationFinish = false;
			}
		}
	}

	@Override
	public void switchBarView(IActionBar barView, boolean animate) {
		if (mCurrentBar != barView) {
			mCurrentBar = barView;
			if (animate && isVisible()) {
				AnimationTask task = new AnimationTask(true, AnimationTask.PARALLEL);
				for (final IActionBar bar : mBarViewGroup) {
					GLView view = (GLView) bar;
					view.setVisible(true);
					view.setHasPixelOverlayed(false);
					if (bar == mCurrentBar) {
						Animation inAnimation = createInAnimation(ANIMATION_DURATION);
						task.addAnimation(view, inAnimation, null);
					} else {
						Animation outAnimation = createOutAnimation(ANIMATION_DURATION);
						task.addAnimation(view, outAnimation, null);
					}
				}
				task.setBatchAnimationObserver(this, ANIMATION_TYPE_INOUT);
				GLAnimationManager.startAnimation(task);
			} else {
				mOnInOutAnimationFinish = true;
				requestLayout();
			}
		}
	}

	private Animation createInAnimation(long duration) {
		int fromX = 0;
		int toX = 0;
		int fromY = 0;
		int toY = 0;
		if (mIsVerticalMode) {
			fromY = mHeight;
		} else {
			fromX = mWidth;
		}
		Animation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
		Animation transAnim = new TranslateAnimation(fromX, toX, fromY, toY);
		AnimationSet animSet = new AnimationSet(true);
		animSet.addAnimation(alphaAnim);
		animSet.addAnimation(transAnim);
		animSet.setDuration(duration);
		return animSet;
	}
	
	private Animation createOutAnimation(long duration) {
		int fromX = 0;
		int toX = 0;
		int fromY = 0;
		int toY = 0;
		if (mIsVerticalMode) {
			toY = mHeight;
		} else {
			toX = mWidth;
		}
		Animation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
		Animation transAnim = new TranslateAnimation(fromX, toX, fromY, toY);
		AnimationSet animSet = new AnimationSet(true);
		animSet.addAnimation(alphaAnim);
		animSet.addAnimation(transAnim);
		animSet.setDuration(duration);
		return animSet;
	}
	
	@Override
	public void translateInAnimation(long duration) {
		Animation animation = createInAnimation(duration);
		animation.setAnimationListener(new AnimationListenerAdapter() {

			@Override
			public void onAnimationStart(Animation animation) {
				for (IActionBar bar : mBarViewGroup) {
					bar.onParentInOutAnimationStart(true);
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				for (IActionBar bar : mBarViewGroup) {
					bar.onParentInOutAnimationEnd(true);
				}
			}
		});
		setHasPixelOverlayed(false);
		startAnimation(animation);
	}
	
	@Override
	public void translateOutAnimation(long duration) {
		Animation animation = createOutAnimation(duration);
		animation.setAnimationListener(new AnimationListenerAdapter() {

			@Override
			public void onAnimationStart(Animation animation) {
				for (IActionBar bar : mBarViewGroup) {
					bar.onParentInOutAnimationStart(false);
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				for (IActionBar bar : mBarViewGroup) {
					bar.onParentInOutAnimationEnd(false);
				}
			}
		});
		animation.setFillAfter(true);
		setHasPixelOverlayed(false);
		startAnimation(animation);
	}

	@Override
	public void onStart(int what, Object[] params) {
		if (mBarViewGroup != null) {
			int size = mBarViewGroup.size();
			for (int i = 0; i < size; i++) {
				GLView bar = (GLView) mBarViewGroup.get(i);
				if (bar == mCurrentBar) {
					if (bar instanceof IActionBar) {
						((IActionBar) bar).onInOutAnimationStart(true);
					}
				} else {
					if (bar instanceof IActionBar) {
						((IActionBar) bar).onInOutAnimationStart(false);
					}
				}
			}
		}
	}

	@Override
	public void onFinish(int what, Object[] params) {
		switch (what) {
			case ANIMATION_TYPE_INOUT :
				mOnInOutAnimationFinish = true;
				requestLayout();
				break;

			default :
				break;
		}
	}

	public void handleShowHomeKeyOnlyChange() {
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_ALL_APP_ACTION_BAR,
				IAppDrawerMsgId.APPDRAWER_ALL_APP_SHOW_HOME_KEY_ONLY_CHANGE, -1);
	}

	/**
	 * 处理底部栏主题改变
	 */
	public void handleBottomThemeChange() {
		SparseArray<ArrayList<IActionBar>> map = StatusFactory.getBottomBarGroupMap();
		for (int i = 0; i < map.size(); i++) {
			ArrayList<IActionBar> actionBarList = map.valueAt(i);
			for (IActionBar actionBar : actionBarList) {
				if (actionBar instanceof GLActionBar) {
					((GLActionBar) actionBar).loadResource();
				}
			}

		}
	}

	@Override
	public void onFolderReLayout(BaseFolderIcon<?> baseFolderIcon, int curStatus) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVerticalMode(boolean isVertical) {
		mIsVerticalMode = true;
	}
//	@Override
//	public void startSpark() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void pauseSpark() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void cancleSpark() {
//		// TODO Auto-generated method stub
//		
//	}
}
