package com.jiubang.shell.appdrawer.component;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.InterpolatorValueAnimation;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLFrameLayout;
import com.go.proxy.ApplicationProxy;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.shell.appdrawer.component.GLCleanView.OnCleanButtonClickListener;
import com.jiubang.shell.common.component.GLCircleProgressBar;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.theme.GLAppDrawerThemeControler;
import com.jiubang.shell.utils.GLImageUtil;

/**
 * 
 * @author yangguanxiang
 *
 */
public class GLMemoryCleanButton extends GLFrameLayout {
	private static final int PROGRESS_BAR_BG_ALPHA = 64;
	private GLCircleProgressBar mProgressBar;
	private GLCleanView mCleanView;
	private OnCleanButtonClickListener mOnCleanButtonClickListener;
	private OnRefreshAnimationListener mOnRefreshAnimationListener;

	private GLDrawable mCleanViewStyleGreen;
	private GLDrawable mCleanViewStyleYellow;
	private GLDrawable mCleanViewStyleRed;

	private int mCleanViewColorGreen;
	private int mCleanViewColorYellow;
	private int mCleanViewColorRed;

	/**
	 * 动画持续时间
	 */
	private static final int ANIMATION_DURATION = 450;
	private InterpolatorValueAnimation mAnimation;
	/**
	 * 无动画
	 */
	private static final int STATUS_NONE = 0;
	/**
	 * 内存减少动画（即第一次动画）
	 */
	private static final int STATUS_REDUCE = 1;
	/**
	 * 内存增加动画（即第二次动画）
	 */
	private static final int STATUS_ADD = 2;
	/**
	 * 内存条动画状态
	 */
	private int mAnimationStatus = STATUS_NONE;
	private long mTotalMemory = 0;
	private long mUsedMemory;
	private float mMemoryPercent;
	private float mTempMemoryPercent;

	private AppDrawerControler mAppDrawerControler;

	public GLMemoryCleanButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mAppDrawerControler = AppDrawerControler
				.getInstance(ShellAdmin.sShellManager.getActivity());
		loadResource(false);

		mProgressBar = new GLCircleProgressBar(context);
		int stroke = getResources()
				.getDimensionPixelSize(R.dimen.promanage_memory_bar_stroke_width);
		mProgressBar.setStroke(stroke);
		mProgressBar.setBackgroundColor(mCleanViewColorGreen);
		mProgressBar.setBackgroundAlpha(PROGRESS_BAR_BG_ALPHA);
		mProgressBar.setProgressColor(mCleanViewColorGreen);
		addView(mProgressBar);
		mCleanView = new GLCleanView(context);
		addView(mCleanView);
	}

	public void loadResource(boolean useTheme) {
		if (useTheme) {
			GLAppDrawerThemeControler themeCtrl = GLAppDrawerThemeControler
					.getInstance(ApplicationProxy.getContext());

			if (mCleanViewStyleGreen != null) {
				mCleanViewStyleGreen.clear();
			}
			mCleanViewStyleGreen = themeCtrl.getGLDrawable(
							themeCtrl.getThemeBean().mAllAppMenuBean.mMenuMemoryCleanBtnNormal,
							true,
							R.drawable.gl_appdrawer_promanage_clean_button_bg_green);
			if (mCleanViewStyleYellow != null) {
				mCleanViewStyleYellow.clear();
			}
			mCleanViewStyleYellow = themeCtrl.getGLDrawable(
							themeCtrl.getThemeBean().mAllAppMenuBean.mMenuMemoryCleanBtnTensity,
							true,
							R.drawable.gl_appdrawer_promanage_clean_button_bg_yellow);
			if (mCleanViewStyleRed != null) {
				mCleanViewStyleRed.clear();
			}
			mCleanViewStyleRed = themeCtrl.getGLDrawable(
							themeCtrl.getThemeBean().mAllAppMenuBean.mMenuMemoryCleanBtnSeriousness,
							true,
							R.drawable.gl_appdrawer_promanage_clean_button_bg_red);

			mCleanViewColorGreen = themeCtrl.getThemeBean().mAllAppMenuBean.mMenuMemoryCleanProgressBarNormal;
			mCleanViewColorYellow = themeCtrl.getThemeBean().mAllAppMenuBean.mMenuMemoryCleanProgressBarTensity;
			mCleanViewColorRed = themeCtrl.getThemeBean().mAllAppMenuBean.mMenuMemoryCleanProgressBarSeriousness;
		} else {
			if (mCleanViewStyleGreen != null) {
				mCleanViewStyleGreen.clear();
			}
			mCleanViewStyleGreen = GLImageUtil
					.getGLDrawable(R.drawable.gl_appdrawer_promanage_clean_button_bg_green);
			if (mCleanViewStyleYellow != null) {
				mCleanViewStyleYellow.clear();
			}
			mCleanViewStyleYellow = GLImageUtil
					.getGLDrawable(R.drawable.gl_appdrawer_promanage_clean_button_bg_yellow);
			if (mCleanViewStyleRed != null) {
				mCleanViewStyleRed.clear();
			}
			mCleanViewStyleRed = GLImageUtil
					.getGLDrawable(R.drawable.gl_appdrawer_promanage_clean_button_bg_red);

			mCleanViewColorGreen = getResources().getColor(
					R.color.promanage_memory_bar_progress_green);
			mCleanViewColorYellow = getResources().getColor(
					R.color.promanage_memory_bar_progress_yellow);
			mCleanViewColorRed = getResources().getColor(
					R.color.promanage_memory_bar_progress_red);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		mProgressBar.layout(0, 0, mWidth, mHeight);
		int offset = DrawUtils.dip2px(8);
		mCleanView.layout(offset, offset, mWidth - offset, mHeight - offset);
	}

	public void setProgress(float progress) {
		mProgressBar.setProgress(progress);

		if (progress < 0.7f) {
			mProgressBar.setBackgroundColor(mCleanViewColorGreen);
			mProgressBar.setProgressColor(mCleanViewColorGreen);
			mCleanView.setBg(mCleanViewStyleGreen);
		} else if (progress >= 0.7f && progress < 0.9f) {
			mProgressBar.setBackgroundColor(mCleanViewColorYellow);
			mProgressBar.setProgressColor(mCleanViewColorYellow);
			mCleanView.setBg(mCleanViewStyleYellow);
		} else if (progress >= 0.9f) {
			mProgressBar.setBackgroundColor(mCleanViewColorRed);
			mProgressBar.setProgressColor(mCleanViewColorRed);
			mCleanView.setBg(mCleanViewStyleRed);
		}
	}

	@Override
	public void setPressed(boolean pressed) {
		mCleanView.setPressed(pressed);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				setPressed(true);
				break;
			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP :
				setPressed(false);
				break;
			default :
				break;
		}
		return super.onTouchEvent(event);
	}

	public void setOnCleanButtonClickListener(OnCleanButtonClickListener listener) {
		mCleanView.setOnCleanButtonClickListener(listener);
	}

	public void setOnRefreshAnimationListener(OnRefreshAnimationListener listener) {
		mOnRefreshAnimationListener = listener;
	}

	public void refresh(boolean animate) {
		calculateMemory(animate);
		if (animate) {
			if (mTempMemoryPercent == 0) {
				mAnimationStatus = STATUS_ADD;
			} else {
				mAnimationStatus = STATUS_REDUCE;
			}
			startRefreshAnimation();
		} else {
			mAnimationStatus = STATUS_NONE;
			if (mOnRefreshAnimationListener != null) {
				mOnRefreshAnimationListener.onRefreshEnd(mUsedMemory, mTotalMemory, mMemoryPercent);
			}
			mTempMemoryPercent = mMemoryPercent;
			mAnimation = null;
		}
	}

	private void startRefreshAnimation() {
		if (mAnimationStatus == STATUS_ADD) {
			mAnimation = new InterpolatorValueAnimation(0);
			mAnimation.start(mMemoryPercent, ANIMATION_DURATION);
		} else if (mAnimationStatus == STATUS_REDUCE) {
			mAnimation = new InterpolatorValueAnimation(mTempMemoryPercent);
			mAnimation.start(0, ANIMATION_DURATION);
			if (mOnRefreshAnimationListener != null) {
				mOnRefreshAnimationListener.onRefreshStart(mUsedMemory, mTotalMemory,
						mMemoryPercent);
			}
		}
		invalidate();
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		if (mAnimation != null) {
			if (!mAnimation.isFinished()) {
				mAnimation.animate();
				float progress = mAnimation.getValue();
				Log.i("Test", "progress: " + progress);
				setProgress(progress);
				mUsedMemory = (long) (mTotalMemory * progress);
				if (mOnRefreshAnimationListener != null) {
					mOnRefreshAnimationListener.onRefreshing(mUsedMemory, mTotalMemory,
							mMemoryPercent);
				}
				invalidate();
			} else {
				float progress = mAnimation.getDstValue();
				if (mAnimationStatus == STATUS_ADD) {
					mAnimationStatus = STATUS_NONE;
					mTempMemoryPercent = mMemoryPercent;
					mAnimation = null;
					setProgress(progress);
					mUsedMemory = (long) (mTotalMemory * progress);
					if (mOnRefreshAnimationListener != null) {
						mOnRefreshAnimationListener.onRefreshEnd(mUsedMemory, mTotalMemory,
								mMemoryPercent);
					}
				} else if (mAnimationStatus == STATUS_REDUCE) {
					mAnimationStatus = STATUS_ADD;
					startRefreshAnimation();
					setProgress(progress);
					mUsedMemory = (long) (mTotalMemory * progress);
				}
			}
		}
		super.dispatchDraw(canvas);
	}

	/**
	 * 计算内存值
	 * */
	private void calculateMemory(boolean animate) {
		long availableMemory = mAppDrawerControler.retriveAvailableMemory() / 1024;
		while (mTotalMemory == 0) {
			mTotalMemory = mAppDrawerControler.retriveTotalMemory() / 1024;
		}

		mUsedMemory = mTotalMemory - availableMemory;
		mMemoryPercent = 1.0f * mUsedMemory / mTotalMemory;
		if (!animate) {
			setProgress(mMemoryPercent);
		}
	}

	/**
	 * 
	 * @author yangguanxiang
	 *
	 */
	public static interface OnRefreshAnimationListener {
		public void onRefreshStart(long usedMemory, long totalMemory, float memoryPercent);
		public void onRefreshing(long usedMemory, long totalMemory, float memoryPercent);
		public void onRefreshEnd(long usedMemory, long totalMemory, float memoryPercent);
	}

}
