package com.jiubang.shell.popupwindow.component.actionmenu;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLRelativeLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLImageView;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.IShell;
import com.jiubang.shell.animation.AlphaAnimation;
import com.jiubang.shell.animation.BackgroundAnimation;
import com.jiubang.shell.animation.Rotate3DAnimation;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.dock.GLDock;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.popupwindow.IPopupWindow;
import com.jiubang.shell.popupwindow.PopupWindowControler.ActionListener;
import com.jiubang.shell.popupwindow.component.GLPopupWindowLayer;
import com.jiubang.shell.popupwindow.component.GLPopupWindowLayer.PopupWindowLayoutParams;
import com.jiubang.shell.screen.GLScreen;

/**
 * 
 * @author zouguiquan
 *
 */
public class GLQuickActionMenu extends GLRelativeLayout
		implements
			IPopupWindow,
			GLView.OnClickListener {

	private GLViewGroup mItemGroup;
	private GLImageView mArrowUp;
	private GLImageView mArrowDown;
	private GLPopupWindowLayer mParentLayer;

	private GLView mTargetView;
	private ActionListener mListener;
	private Object mCallbackFlag;

	public int offsetY = 0;
	private int mAlpha;
	private boolean mshowTopArrow = true;
	private static final long APPEAR_TIME = 220;
	private static final long DISAPPEAR_TIME = 500;
	private int mArrowPaddingLeft = 0;
	private int mArrowPaddingRight = 0;

	public static final int CLICK_NO_ALPHA = 255;
	// 用来判断是否全屏的参数，目标view的高度超过这个值，就认为是全屏
	private final static float FULL_SCREEN_FACTOR = 0.8f;

	public GLQuickActionMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public GLQuickActionMenu(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GLQuickActionMenu(Context context) {
		this(context, null);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mItemGroup = (GLLinearLayout) findViewById(R.id.tracks);
		mArrowUp = (GLImageView) findViewById(R.id.arrow_up);
		mArrowDown = (GLImageView) findViewById(R.id.arrow_down);

		final Resources resources = mContext.getResources();
		mArrowPaddingLeft = resources.getDimensionPixelOffset(R.dimen.qa_arrow_padding_left);
		mArrowPaddingRight = resources.getDimensionPixelOffset(R.dimen.qa_arrow_padding_right);
	}

	@Override
	public void cleanup() {
		for (int i = 0; i < mItemGroup.getChildCount(); i++) {
			GLView child = mItemGroup.getChildAt(i);
			if (child instanceof GLMenuTextView) {
				((GLMenuTextView) child).selfDestruct();
			}
		}
		mParentLayer = null;
		super.cleanup();
	}

	public void computePostion(Rect locateRect) {
		int screenWidth = StatusBarHandler.getDisplayWidth();
		int screenHeight = StatusBarHandler.getDisplayHeight();
		
		// 计算菜单实际宽高
		setLayoutParams(new PopupWindowLayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		setFocusableInTouchMode(true);
		setFocusable(true);
		requestFocus();

		int rootHeight = getMeasuredHeight();
		int rootWidth = getMeasuredWidth();

		int center = locateRect.centerX();
		int startX = center - rootWidth / 2;
		if (rootWidth > screenWidth) {
			startX = 0;
		}
		if (center + rootWidth / 2 > screenWidth) {
			startX = screenWidth - rootWidth;
		}

		if (startX < 0) {
			startX = 0;
		}

		final float targetHeightRatio = (float) locateRect.height() / (float) screenHeight;
		boolean isFullScreen = targetHeightRatio > FULL_SCREEN_FACTOR;

		int startY = 0;
		if (isFullScreen) {
			startY = locateRect.centerY() - rootHeight / 2;
			mshowTopArrow = false;
		} else {
			if (locateRect.top > rootHeight + offsetY) {
				// 显示下箭头
				startY = locateRect.top - rootHeight;
				mshowTopArrow = true;
			} else {
				// 显示上箭头
				mshowTopArrow = false;
				startY = locateRect.bottom;
				if ((startY + rootHeight) > screenHeight) {
					startY = screenHeight - rootHeight;
				}
			}
		}

		if (mshowTopArrow) {
			showArrow(R.id.arrow_down, center - startX, rootWidth);
		} else {
			showArrow(R.id.arrow_up, center - startX, rootWidth);
		}

		PopupWindowLayoutParams params = (PopupWindowLayoutParams) getLayoutParams();
		params.x = startX;
		params.y = startY;
		params.width = rootWidth;
		params.height = rootHeight;
	}

	private void showArrow(int whichArrow, int requestedX, int contentWidth) {
		final GLView showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
		final GLView hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;

		showArrow.setVisibility(View.VISIBLE);
		final int arrowWidth = showArrow.getMeasuredWidth();

		GLRelativeLayout.LayoutParams param = (GLRelativeLayout.LayoutParams) showArrow
				.getLayoutParams();

		// 限制左右边界,箭头的起始位置相对于mContentView而不是屏幕
		int leftMargin = requestedX - arrowWidth / 2;
		leftMargin = Math.max(mArrowPaddingLeft, leftMargin);
		if (leftMargin + arrowWidth > contentWidth - mArrowPaddingRight) {
			leftMargin = contentWidth - mArrowPaddingRight - arrowWidth;
		}

		param.leftMargin = leftMargin;
		hideArrow.setVisibility(View.GONE);
	}

	public void setTargetView(GLView targetView) {
		mTargetView = targetView;
	}

	public void setOffsetY(int value) {
		offsetY = value;
	}

	public void setParentLayer(GLPopupWindowLayer layer) {
		mParentLayer = layer;
	}

	public GLViewGroup getItemGroup() {
		return mItemGroup;
	}

	public void setAlpha(int alpha) {
		mAlpha = alpha;
	}
	
	public void setActionListener(ActionListener listener) {
		mListener = listener;
	}
	
	public void setCallbackFlag(Object callbackFlag) {
		mCallbackFlag = callbackFlag;
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		if (mAlpha != CLICK_NO_ALPHA) {
			canvas.multiplyAlpha(mAlpha);
		}
		super.dispatchDraw(canvas);
	}

	@Override
	public void onEnter(final GLPopupWindowLayer layer, boolean animate) {
		float pivot = 0;
		Rotate3DAnimation rotateAnimation = null;
		if (mshowTopArrow) {
			pivot = getMeasuredHeight();
			rotateAnimation = new Rotate3DAnimation(-90, 0, 0f, -pivot, 0, 1, 0, 0);
		} else {
			pivot = 0;
			rotateAnimation = new Rotate3DAnimation(90, 0, 0, pivot, 0, 1, 0, 0);
		}

		rotateAnimation.setDuration(APPEAR_TIME);
		rotateAnimation.setRepeatCount(0);
		AlphaAnimation alphaAnimation = new AlphaAnimation(0, 255, this);
		alphaAnimation.setDuration(APPEAR_TIME);
		alphaAnimation.setRepeatCount(0);

		AnimationSet animationSet = new AnimationSet(true);
		animationSet.setInterpolator(InterpolatorFactory.getInterpolator(
				InterpolatorFactory.QUADRATIC, InterpolatorFactory.EASE_IN));
		animationSet.addAnimation(rotateAnimation);
		animationSet.addAnimation(alphaAnimation);

		AnimationTask animationTask = new AnimationTask(this, animationSet, new AnimationListenerAdapter() {
			
			@Override
			public void onAnimationEnd(Animation animation) {
				layer.onEnter();
			}
		}, true,
				AnimationTask.PARALLEL);

		GLAnimationManager.startAnimation(animationTask);
	}

	@Override
	public void onExit(final GLPopupWindowLayer layer, boolean animate) {
		float pivot = 0;
		Rotate3DAnimation rotateAnimation = null;
//		TranslateValue3DAnimation translateAnimation = null;
		if (mshowTopArrow) {
			pivot = getMeasuredHeight() / 2;
			rotateAnimation = new Rotate3DAnimation(0, -90, 0, -pivot, 0, 1, 0, 0);
//			translateAnimation = new TranslateValue3DAnimation(0, 0, 0, (int) (getHeight() * 0.45),
//					0, -120);
		} else {
			pivot = getMeasuredHeight() / 2;
			rotateAnimation = new Rotate3DAnimation(0, 90, 0, -pivot, 0, 1, 0, 0);
//			translateAnimation = new TranslateValue3DAnimation(0, 0, 0, -(int) (getHeight() * 0.45),
//					0, -120);
		}

		rotateAnimation.setDuration(DISAPPEAR_TIME);
		rotateAnimation.setRepeatCount(0);
//		translateAnimation.setDuration(mDisappearTime);
//		translateAnimation.setRepeatCount(0);
		AlphaAnimation alphaAnimation = new AlphaAnimation(255, 40, this);
		alphaAnimation.setDuration(DISAPPEAR_TIME);
		alphaAnimation.setRepeatCount(0);

		AnimationSet animationSet = new AnimationSet(true);
		animationSet.setInterpolator(InterpolatorFactory.getInterpolator(
				InterpolatorFactory.QUADRATIC, InterpolatorFactory.EASE_OUT));
		
		animationSet.addAnimation(alphaAnimation);	
		animationSet.addAnimation(rotateAnimation);
//		animationSet.addAnimation(translateAnimation);
		animationSet.setFillAfter(true);
		AnimationTask animationTask = new AnimationTask(this, animationSet,
				new AnimationListenerAdapter() {

					@Override
					public void onAnimationEnd(Animation animation) {
						post(new Runnable() {
							
							@Override
							public void run() {
								layer.onExit();
							}
						});
					}
				}, true, AnimationTask.PARALLEL);
		GLAnimationManager.startAnimation(animationTask);
	}

	@Override
	public void onWithEnter(boolean animate) {
		IShell shell = ShellAdmin.sShellManager.getShell();
		int stage = shell.getCurrentStage();
		if (stage == IShell.STAGE_SCREEN) {
			GLScreen screen = (GLScreen) shell.getView(IViewId.SCREEN);
			if (screen != null) {
				screen.hideBgAnimation(BackgroundAnimation.ANIMATION_TYPE_ALPHA, mTargetView, 1.0f,
						0.3f, APPEAR_TIME);
			}
			GLDock dock = (GLDock) shell.getView(IViewId.DOCK);
			if (dock != null) {
				dock.hideBgAnimation(
						BackgroundAnimation.ANIMATION_TYPE_ALPHA, mTargetView, 1.0f, 0.3f,
						APPEAR_TIME);
			}
		}
	}

	@Override
	public void onWithExit(boolean animate) {
		IShell shell = ShellAdmin.sShellManager.getShell();
		int stage = shell.getCurrentStage();
		if (stage == IShell.STAGE_SCREEN) {
			GLScreen screen = (GLScreen) shell.getView(IViewId.SCREEN);
			screen.showBgAnimation(BackgroundAnimation.ANIMATION_TYPE_ALPHA, mTargetView, 0.3f,
					1.0f, DISAPPEAR_TIME);
			GLDock dock = (GLDock) shell.getView(IViewId.DOCK);
			dock.showBgAnimation(BackgroundAnimation.ANIMATION_TYPE_ALPHA,
					mTargetView, 0.3f, 1.0f, DISAPPEAR_TIME);
		}
	}

	@Override
	public void onClick(GLView v) {
		Object tag = v.getTag();
		if (tag != null && tag instanceof Integer) {
			int id = ((Integer) tag).intValue();

			if (mListener != null) {
				mListener.onActionClick(id, mCallbackFlag);
			}
		}
		if (mParentLayer != null) {
			mParentLayer.exit(false);
		}
	}
}
