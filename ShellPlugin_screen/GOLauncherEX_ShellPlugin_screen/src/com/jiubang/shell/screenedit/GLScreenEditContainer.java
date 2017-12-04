package com.jiubang.shell.screenedit;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLRelativeLayout;
import com.go.gl.view.GLView;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.shell.common.component.GLProgressBar;
import com.jiubang.shell.indicator.DesktopIndicator;
import com.jiubang.shell.screenedit.tabs.GLBaseTab;
import com.jiubang.shell.screenedit.tabs.GLBaseTab.ILoadDataListener;

/**
 * 
 * @author zouguiquan
 *
 */
public class GLScreenEditContainer extends GLRelativeLayout
		implements
			AnimationListener,
			ILoadDataListener {
	
	private GLLinearLayout mTabContainer;
	private DesktopIndicator mIndicator;
	private GLProgressBar mGLProgressBar;
	
	private GLBaseTab mBaseTab;
	
	public GLScreenEditContainer(Context context) {
		this(context, null);
	}

	public GLScreenEditContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setupView();
	}

	private void setupView() {
		mTabContainer = (GLLinearLayout) findViewById(R.id.container);
		mIndicator = (DesktopIndicator) findViewById(R.id.indicator);
		mIndicator.setDefaultDotsIndicatorImage(R.drawable.gl_screen_edit_indicator_cur,
				R.drawable.gl_screen_edit_indicator_other);
		mIndicator.setSliderIndicator(R.drawable.gl_screenedit_indicator, R.drawable.gl_screenedit_indicator_bg);
		int sliderIndicatorHeight = (int) getResources().getDimension(R.dimen.screenedit_slider_indicator_height);
		mIndicator.setSliderIndicatorHeight(sliderIndicatorHeight);
		mIndicator.setIsFromAddFrame(true);

		mGLProgressBar = (GLProgressBar) findViewById(R.id.progress);
		mGLProgressBar.setMode(GLProgressBar.MODE_INDETERMINATE);
		Drawable drawable = mContext.getResources().getDrawable(
				R.drawable.gl_progressbar_indeterminate_white);
		mGLProgressBar.setIndeterminateProgressDrawable(drawable);
	}
	
	public void setTabHeight(int tabHeight) {
		GLRelativeLayout.LayoutParams params = (GLRelativeLayout.LayoutParams) mTabContainer
				.getLayoutParams();
		params.height = tabHeight;
	}

	public void setCurrentTab(GLBaseTab baseTab) {
		mTabContainer.removeAllViews();
		mBaseTab = baseTab;
		
		if (baseTab != null) {
			mIndicator.setVisibility(INVISIBLE);
			baseTab.setLoadDataListener(this);
			baseTab.setIndicator(mIndicator);
			baseTab.resetData();
			baseTab.onResume();
			baseTab.startLoadData();
		}
	} 
	
	@Override
	public void onLoadDataStart() {	
		if (mBaseTab.needShowProgress()) {
			mGLProgressBar.show();
		}
	}

	@Override
	public void onLoadDataFinish() {
		mGLProgressBar.hide();
		final GLView glView = mBaseTab.getContentView();

		if (glView != null) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			mTabContainer.addView(glView, params);
			
			mIndicator.setVisibility(VISIBLE);
			mIndicator.setCurrent(mBaseTab.getCurrentScreen());
			mIndicator.setIndicatorListener(mBaseTab);
			mIndicator.setTotal(mBaseTab.getTotalPage());
		}
	}
	
	@Override
	public void onRefreshDataFinish() {
		mGLProgressBar.hide();
	}

	public void startChangeTabAnim(AnimationListener listener) {
		
		AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
		alphaAnim.setDuration(ScreenEditConstants.DURATION_TAB_CHANGE);
		
		alphaAnim.setAnimationListener(listener);
		setHasPixelOverlayed(false);
		startAnimation(alphaAnim);
	}
	
	public void selfDestruct() {
		cleanup();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		
	}

	@Override
	public void onAnimationStart(Animation animation) {
		
	}
	
	@Override
	public void onAnimationProcessing(Animation animation, float interpolatedTime) {
	}
}
