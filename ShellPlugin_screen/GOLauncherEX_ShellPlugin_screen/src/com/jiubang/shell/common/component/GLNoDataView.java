package com.jiubang.shell.common.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.LinearLayout.LayoutParams;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLImageView;
import com.go.gl.widget.GLTextViewWrapper;
import com.go.proxy.GoLauncherActivityProxy;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.shell.appdrawer.GLAppDrawerMainView;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 功能表Grid无数据显示组件，包括一张图片和文字
 * @author yejijiong
 *
 */
public class GLNoDataView extends GLLinearLayout {
	private GLImageView mNoDataBG;
	private GLTextViewWrapper mNoDataText; // 默认无文字
	private Context mContext;
	private int mBGPaddingTop;
	private int mNoDataBGHeight;
	private int mNoDataBGWidth;
	private int mTextPaddingTop;
	private AppFuncUtils mUtils;
	
	public GLNoDataView(Context context) {
		super(context);
		mContext = context;
		mUtils = AppFuncUtils.getInstance(ShellAdmin.sShellManager.getActivity());
		init();
	}
	
	private void init() {
		setGravity(Gravity.CENTER_HORIZONTAL);
		setOrientation(VERTICAL);
		mNoDataBG = new GLImageView(mContext);
		Drawable bgDrawable = mContext.getResources().getDrawable(
				R.drawable.gl_appdrawer_recentapp_no_data_bg);
		mNoDataBGWidth = bgDrawable.getIntrinsicWidth();
		mNoDataBGHeight = bgDrawable.getIntrinsicHeight();
		mNoDataBG.setBackgroundDrawable(bgDrawable);
		mNoDataText = new GLTextViewWrapper(mContext);
		mNoDataText.setGravity(Gravity.CENTER_HORIZONTAL/* | Gravity.TOP*/);
		mNoDataText.setSingleLine();
		mNoDataText.setTextSize(mUtils.getStandardSize(15));
		addView(mNoDataBG, new LayoutParams(mNoDataBGWidth, mNoDataBGHeight));
		addView(mNoDataText, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		
		mBGPaddingTop = mUtils.getStandardSize(70);
		mTextPaddingTop = mBGPaddingTop + mNoDataBGHeight + mUtils.getStandardSize(44);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int noDataBGLeft = (right - left - mNoDataBGWidth) / 2;
		mNoDataBG.layout(noDataBGLeft, mBGPaddingTop, noDataBGLeft + mNoDataBGWidth, mBGPaddingTop + mNoDataBGHeight);
		mNoDataText.layout(0, mTextPaddingTop, right, bottom);
	}
	
	public void setNoDataText(String text) {
		mNoDataText.setText(text);
	}
	
	/**
	 * 执行手势动画
	 */
	public void doSwipeAnimation(AnimationTask task, int type, int size) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			Animation moveAnimation = null;
			GLView child = getChildAt(i);
			if (GoLauncherActivityProxy.isPortait()) { // 竖屏
				float startOffsetY;
				float endOffsetY;
				if (type == GLAppDrawerMainView.ANIMATION_TOP_CONTAINER_SHOW) {
					startOffsetY = 0.0f;
					endOffsetY = size;
					moveAnimation = new TranslateAnimation(0.0f, 0.0f, startOffsetY, endOffsetY);
				} else if (type == GLAppDrawerMainView.ANIMATION_TOP_CONTAINER_HIDE) {
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
				task.addAnimation(child, moveAnimation, null);
			}
		}
	}
}
