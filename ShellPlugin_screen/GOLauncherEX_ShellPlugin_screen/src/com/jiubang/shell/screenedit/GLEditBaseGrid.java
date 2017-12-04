package com.jiubang.shell.screenedit;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View.MeasureSpec;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.animation.Translate3DAnimation;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLAdapterView;
import com.go.gl.widget.GLImageView;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.util.graphics.DrawUtils;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.common.component.GLScrollableBaseGrid;
import com.jiubang.shell.common.component.HorScrollableGridViewHandler;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.indicator.IndicatorListener;
import com.jiubang.shell.screenedit.tabs.GLBaseTab;
import com.jiubang.shell.screenedit.tabs.GLGridTab;
import com.jiubang.shell.screenedit.tabs.ScreenEditImageLoader;
import com.jiubang.shell.scroller.effector.CoupleScreenEffector;

/**
 * 
 * @author zouguiquan
 *
 */
public class GLEditBaseGrid extends GLScrollableBaseGrid {

	private GLBaseTab mBaseTab;

	public GLEditBaseGrid(Context context, GLBaseTab baseTab) {
		super(context);
		mBaseTab = baseTab;
		init();
	}

	private void init() {
		handleRowColumnSetting(false);
		handleScrollerSetting();
	}

	@Override
	protected void handleScrollerSetting() {
		mScrollableHandler = new HorScrollableGridViewHandler(mContext, this,
				CoupleScreenEffector.PLACE_NONE, false, true) {
			@Override
			public void onEnterLeftScrollZone() {
			}

			@Override
			public void onEnterRightScrollZone() {
			}

			@Override
			public void onEnterTopScrollZone() {
			}

			@Override
			public void onEnterBottomScrollZone() {
			}

			@Override
			public void onExitScrollZone() {
			}

			@Override
			public void onScrollFinish(int currentScreen) {
				super.onScrollFinish(currentScreen);
			}

			@Override
			protected void mesureChildInLayout(int columnWidth, int rowHeight, LayoutParams p,
					GLView child) {
				int childHeightSpec = GLViewGroup.getChildMeasureSpec(
						MeasureSpec.makeMeasureSpec(rowHeight, MeasureSpec.EXACTLY), 0, p.height);
				int childWidthSpec = GLViewGroup.getChildMeasureSpec(
						MeasureSpec.makeMeasureSpec(columnWidth, MeasureSpec.EXACTLY), 0, p.width);
				child.measure(childWidthSpec, childHeightSpec);
			}
		};

		ScreenSettingInfo screenSettingInfo = SettingProxy.getScreenSettingInfo();
		if (screenSettingInfo != null) {
			mScrollableHandler.setCycleScreenMode(screenSettingInfo.mScreenLooping);
		}
	}

	public void setIndicator(GLView indicator) {
		if (mScrollableHandler != null) {
			mScrollableHandler.setIndicator(indicator);
		}
	}

	public int getTotalPage() {
		return ((HorScrollableGridViewHandler) mScrollableHandler).getTotalScreen();
	}

	public int getCurrentScreen() {
		return ((HorScrollableGridViewHandler) mScrollableHandler).getCurrentScreen();
	}

	@Override
	protected void handleRowColumnSetting(boolean updateDB) {

		Resources resources = mContext.getResources();
		// 智能适应
		int horizontalpading = (int) resources
				.getDimension(R.dimen.screen_edit_view_horizontal_space);
		int viewWidth = (int) resources.getDimension(R.dimen.screen_edit_tab_view_width);
		
		mNumColumns = (StatusBarHandler.getDisplayWidth() - horizontalpading)
				/ (viewWidth + horizontalpading);
		int rightSpace = StatusBarHandler.getDisplayWidth() - horizontalpading - mNumColumns
				* (viewWidth + horizontalpading);
		if (rightSpace >= viewWidth) {
			++mNumColumns;
		}

		int viewheight = (int) resources.getDimension(R.dimen.screen_edit_tab_view_height);
		int tabHeight = mBaseTab.getTabHeight();

		if (DrawUtils.sHeightPixels <= 800) { // 保证 800分辨率的手机能显示三排
			viewheight = DrawUtils.dip2px(76);
		}
		mNumRows = tabHeight / viewheight;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ShellBaseAdapter createAdapter(Context mContext, List infoList) {

		if (!(mBaseTab instanceof GLGridTab)) {
			throw new IllegalArgumentException("GLEditBaseGrid mBaseTab must instanceof GLGridTab");
		}

		ShellBaseAdapter adapter = ((GLGridTab) mBaseTab).createAdapter(mContext, infoList);
		return adapter;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		layoutChildren();
	}

	private void layoutChildren() {

		HorScrollableGridViewHandler handler = (HorScrollableGridViewHandler) mScrollableHandler;
		int currentScreen = handler.getCurrentScreen();
		if (mBaseTab.needAsyncLoadImage() && !mBaseTab.getImageLoader().isLoadFinish(currentScreen)) {
			return;
		}

		if (mBaseTab.needEnterAnim()) {
			startEnterAnim();
		} else if (mBaseTab.needChangeAnim()) {
			startChangeAnim();
		} else if (mBaseTab.needExitAnim()) {
			startExitAnim();
		}
	}

	/**
	 * 第一级的出现动画
	 */
	public void startEnterAnim() {

		HorScrollableGridViewHandler handler = (HorScrollableGridViewHandler) mScrollableHandler;
		int currentScreen = handler.getCurrentScreen();
		int startIndex = currentScreen * mNumColumns * mNumRows;
		int endIndex = startIndex + mNumColumns * mNumRows;
		endIndex = Math.min(endIndex, getChildCount());

		float smallScale = 0.1f;
		float largeScale = 1.2f;
		float upSpan = largeScale - smallScale;
		float downSpan = largeScale - 1.0f;
		long totalDuration = ScreenEditConstants.DURATION_TAB_ENTER;

		long zoomInDuration = (long) (totalDuration * (upSpan / (upSpan + downSpan)));
		long zoomOutDuration = totalDuration - zoomInDuration;

		for (int pos = startIndex; pos < endIndex; pos++) {
			GLView view = getChildAt(pos);
			if (view != null) {

				AnimationTask task = new AnimationTask(false, AnimationTask.SERIAL);
				task.setBatchAnimationObserver(new BatchAnimationObserver() {

					@Override
					public void onStart(int what, Object[] params) {
					}

					@Override
					public void onFinish(int what, Object[] params) {
						if (mBaseTab != null) {
							mBaseTab.setNeedEnterAnim(false);
							mBaseTab.refreshTitle();
						}
					}
				}, 0);

				Animation scaleOne = new ScaleAnimation(0.1f, 1.1f, 0.1f, 1.1f,
						Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				scaleOne.setDuration(zoomInDuration);
				scaleOne.setFillEnabled(true);
				scaleOne.setFillAfter(false);

				Animation scaleTwo = new ScaleAnimation(1.1f, 1.0f, 1.1f, 1.0f,
						Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				scaleTwo.setDuration(zoomOutDuration);
				scaleTwo.setFillEnabled(true);
				scaleTwo.setFillAfter(false);

				task.addAnimation(view, scaleOne, null);
				task.addAnimation(view, scaleTwo, null);
				GLAnimationManager.startAnimation(task);
			}
		}
	}

	/**
	 * 跳转时的出现动画
	 */
	public void startChangeAnim() {

		HorScrollableGridViewHandler handler = (HorScrollableGridViewHandler) mScrollableHandler;
		int currentScreen = handler.getCurrentScreen();
		int startIndex = currentScreen * mNumColumns * mNumRows;
		int endIndex = startIndex + mNumColumns * mNumRows;
		endIndex = Math.min(endIndex, getChildCount());

		float smallScale = 0.7f;
		float largeScale = 1.1f;
		float upSpan = largeScale - smallScale;
		float downSpan = largeScale - 1.0f;
		long totalDuration = ScreenEditConstants.DURATION_TAB_ENTER;

		long zoomInDuration = (long) (totalDuration * (upSpan / (upSpan + downSpan)));
		long zoomOutDuration = totalDuration - zoomInDuration;

		for (int pos = startIndex; pos < endIndex; pos++) {
			final GLView view = getChildAt(pos);

			if (view != null) {
				AnimationSet animationSet = new AnimationSet(true);

				Translate3DAnimation tranAnimation = new Translate3DAnimation(0, 0, 0, 0,
						Animation.RELATIVE_TO_SELF, -0.15f, Animation.RELATIVE_TO_SELF, 0, 0, 0, 0,
						0);
				tranAnimation.setDuration(zoomInDuration);
				animationSet.addAnimation(tranAnimation);

				ScaleAnimation scaleUp = new ScaleAnimation(0.7f, 1.1f, 0.7f, 1.1f,
						Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				scaleUp.setDuration(zoomInDuration);

				ScaleAnimation scaleDown = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f,
						Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				scaleDown.setDuration(zoomOutDuration);
				scaleDown.setStartOffset(zoomInDuration);

				animationSet.addAnimation(scaleUp);
				animationSet.addAnimation(scaleDown);

				AlphaAnimation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
				alphaAnimation.setDuration(zoomInDuration);
				animationSet.addAnimation(alphaAnimation);
				view.setHasPixelOverlayed(false);

				if (pos == endIndex - 1) {
					animationSet.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationProcessing(Animation animation,
								float interpolatedTime) {
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							if (mBaseTab != null) {
								mBaseTab.setNeedChangeAnim(false);
								mBaseTab.refreshTitle();
							}
						}
					});
				}
				view.startAnimation(animationSet);
			}
		}

		setVisibility(VISIBLE);
	}

	private void startExitAnim() {
	}

	@Override
	public synchronized void setData(List infoList) {
		super.setData(infoList);
	}

	@Override
	protected void onScrollStart() {
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_ENABLE_CELLLAYOUT_DRAWING_CACHE, 1);
	}

	@Override
	public void onScrollFinish() {
		
		int currentScreen = ((HorScrollableGridViewHandler) mScrollableHandler)
				.getCurrentScreen();
		mBaseTab.setCurrentScreen(currentScreen);

		if (mBaseTab.needAsyncLoadImage()) {

			ScreenEditImageLoader imageLoader = mBaseTab.getImageLoader();
			if (imageLoader == null) {
				return;
			}

			imageLoader.setCurrentLoadPage(currentScreen);

			int startIndex = currentScreen * mNumColumns * mNumRows;
			int endIndex = startIndex + mNumColumns * mNumRows;
			endIndex = Math.min(endIndex, getChildCount());

			Log.d("zgq", "onScreenChange startIndex= " + startIndex + " endIndex= " + endIndex);

			for (int pos = startIndex; pos < endIndex; pos++) {
				GLView view = getChildAt(pos);
				if (view != null) {
					GLImageView imageView = (GLImageView) view.findViewById(R.id.thumb);
					if (imageView != null) {
						imageLoader.loadImage(imageView, pos);
					}
				}
			}
		}
	}

	/**
	 * 清空数据
	 */
	@Override
	public void clearData() {
		super.clearData();
	}

	public void setScrollPercent(float percent) {
		if (mScrollableHandler instanceof IndicatorListener) {
			((IndicatorListener) mScrollableHandler).sliding(percent);
		}
	}

	public void snapToScreen(int index, boolean needAnimation) {
		if (mScrollableHandler instanceof HorScrollableGridViewHandler) {
			HorScrollableGridViewHandler handler = (HorScrollableGridViewHandler) mScrollableHandler;
			handler.scrollTo(index, needAnimation);
		}
	}

	@Override
	protected void onScreenChange(int newScreen, int oldScreen) {
	}

	@Override
	public void callBackToChild(GLView view) {
	}

	@Override
	public void refreshGridView() {
	}

	@Override
	public boolean onItemLongClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		return true;
	}

	@Override
	public void onItemClick(GLAdapterView<?> parent, GLView view, int position, long id) {
	}
}
