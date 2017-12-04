package com.jiubang.shell.common.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout.LayoutParams;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.animation.Transformation3D;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLImageView;
import com.go.gl.widget.GLImageView.ScaleType;
import com.jiubang.ggheart.components.CounterUtil;
import com.jiubang.shell.animation.Rotate3DAnimation;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.effect.EffectController;
import com.jiubang.shell.effect.EffectController.EffectListener;
import com.jiubang.shell.effect.IEffect;
import com.jiubang.shell.effect.ScaleValueEffect;
import com.jiubang.shell.model.IModelState;

/**
 * 
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author chaoziliang
 * @date [2012-9-7]
 */
public class GLModel3DMultiView extends GLFrameLayout {
	public static final int OPEN_FOLDER_TASK = 0;
	public static final int CLOSE_FOLDER_TASK = 1;

	// Target of Motion events
	private static int sExtraWidth = 10; // CHECKSTYLE IGNORE
	boolean mView = false;
	private static int sHitOutPadding;
	private static final float HIT_OUT_PADDING_SIZE = 0.4f;

	// 当前状态右上角图标的状态
	private int mCurrentState = IModelState.NO_STATE;
	/**
	 * 当前状态右下角图标的状态
	 */
	private int mCurrentLowerRightStatue = IModelState.NO_STATE;

	// 右上角图标是否接受点击事件
	private float mScale = 1;
	public float mMaxScale = 1.0f;
	public float mMinScale = 0.82f;
	public int mDuration = 70;

	public void setScale(float scale) {
		mScale = scale;
	}
	
	public float getScale() {
		return mScale;
	}
	
	public int getCurrentState() {
		return mCurrentState;
	}

	private GLImageView mImageView;
	/**
	 * 右下角图标
	 */
	private GLImageView mLowerRightImageView;
	private GLModel3DView mModelView;
	//	private NewAppEffect mNewAppEffect;
	private GLImageView mBgView;
	private GLImageView mCoverView;
	private OnSelectClickListener mSelectListener;
	private AnimationTask mReadyFolderTask;
	private AnimationTask mCancleBgTask;

	//	private AnimationTask mCoverTask;

	private SparseArray<Drawable> mDrawableCache;
	
	private EffectController mEffectController;
	private IEffect mPressEffect;

	public GLModel3DMultiView(Context context) {
		super(context);
		init();
	}

	public GLModel3DMultiView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		if (sHitOutPadding == 0) {
			sExtraWidth = (int) (mContext.getResources().getDisplayMetrics().density * sExtraWidth);
			sHitOutPadding = (int) (mContext.getResources().getDrawable(R.drawable.gl_uninstall)
					.getIntrinsicWidth() * HIT_OUT_PADDING_SIZE);
		}
		mDrawableCache = new SparseArray<Drawable>();
		mEffectController = new EffectController();
		
	}

	public void setOnSelectClickListener(OnSelectClickListener l) {
		mSelectListener = l;
	}

	public OnSelectClickListener getOnSelectClickListener() {
		return mSelectListener;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mImageView = (GLImageView) findViewById(R.id.imge);
		if (mImageView != null) {
			mImageView.setIsClearForUpdate(false);
			mImageView.setScaleType(ScaleType.FIT_XY);
			mImageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(GLView v) {
					if (mSelectListener != null) {
						mSelectListener.onClick(GLModel3DMultiView.this);
					}
				}
			});
			mImageView.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(GLView v) {
					mImageView.setPressed(false);
					return false;
				}
			});
		}

		mLowerRightImageView = (GLImageView) findViewById(R.id.lower_right_imge);
		if (mLowerRightImageView != null) {
			mLowerRightImageView.setIsClearForUpdate(false);
		}

		mModelView = (GLModel3DView) findViewById(R.id.model);

		setCurrenState(mCurrentState);

		mBgView = new GLImageView(mContext);
		LayoutParams layoutParams = new LayoutParams(mModelView.getIconWidth(),
				mModelView.getIconWidth(), Gravity.CENTER);
		addView(mBgView);
		mBgView.setLayoutParams(layoutParams);
		mBgView.setVisibility(GLView.INVISIBLE);

		mCoverView = new GLImageView(mContext);
		layoutParams = new LayoutParams(mModelView.getIconWidth(), mModelView.getIconWidth(),
				Gravity.CENTER);
		addView(mCoverView);
		mCoverView.setLayoutParams(layoutParams);
		mCoverView.setVisibility(GLView.INVISIBLE);

		mBgView.setIsClearForUpdate(false);
		mCoverView.setIsClearForUpdate(false);
	}


	@Override
	protected void dispatchDraw(GLCanvas canvas) {

		if (mEffectController != null) {
			mEffectController.doEffect(canvas, getDrawingTime(), this);
		}
		
//		canvas.scale(mScale, mScale, getWidth() / 2, getHeight() / 2);
		
		final long drawingTime = getDrawingTime();

		if (mBgView != null && (mBgView.isVisible() || mBgView.getAnimation() != null)) {
			drawChild(canvas, mBgView, drawingTime);
		}

		if (mModelView != null && (mModelView.isVisible() || mModelView.getAnimation() != null)) {
			drawChild(canvas, mModelView, drawingTime);
		}

		if (mCoverView != null && (mCoverView.isVisible() || mCoverView.getAnimation() != null)) {
			drawChild(canvas, mCoverView, drawingTime);
		}

		if (mImageView != null && (mImageView.isVisible() || mImageView.getAnimation() != null)) {
			if (!IconView.sEnableStateAnimation) {
				Animation animation = mImageView.getAnimation();
				if (animation != null) {
					//getTransformation保证动画一定开始，clearAnimation才能调用listener中的onAnimationEnd方法
					animation.getTransformation(drawingTime, new Transformation3D()); 
					mImageView.clearAnimation();
				}
			}
			if (mImageView.isVisible()) {
				drawChild(canvas, mImageView, drawingTime);
			}
		}

		if (mLowerRightImageView != null
				&& (mLowerRightImageView.isVisible() || mLowerRightImageView.getAnimation() != null)) {
//			drawChild(canvas, mLowerRightImageView, drawingTime);
			int save = canvas.save();
			canvas.translate(mLowerRightImageView.getLeft(), mLowerRightImageView.getTop());
			mLowerRightImageView.draw(canvas);
			canvas.restoreToCount(save);
		}
	}
	
	private long mPaddingCallBackTime;
	/**
	 * 
	 * @param listener
	 * @param glView
	 * @param duration
	 * @param allowDispatchTouchEvent
	 */
	public void startClickEffect(EffectListener listener, GLView glView, long duration,
			boolean allowDispatchTouchEvent) {
				
		long now = System.nanoTime();
		if (Math.abs(now - mPaddingCallBackTime) < 250) {
			return;
		}
		
		if (mEffectController != null && !mEffectController.isAllEffectCompleted()) {
			mEffectController.setCallbackFlag(glView);
			mEffectController.addEffectListener(new EffectListener() {
				
				@Override
				public void onEffectStart(Object object) {
				}
				
				@Override
				public void onEffectComplete(Object object) {
					mPaddingCallBackTime = System.nanoTime();
				}
			});
			mEffectController.addEffectListener(listener);
		} else {
			if (listener != null) {
				mPaddingCallBackTime = System.nanoTime();
				listener.onEffectComplete(glView);
			}
		}
	}
	
	public boolean isEffectAnimation() {
		return !mEffectController.isAllEffectCompleted();
	}
	
	public void startUpAnimation() {
		mPressEffect.setHoldEnding(false);
	}
	
	public void startPressAnimation() {
		if (mModelView != null) {
			
			// 搞掉这两句就完美了
			mEffectController.cleanEffectListener();
			mEffectController.clearEffect();
			
			mEffectController.setEffectMode(EffectController.EFFECT_MODE_SEQUENCE);
			mPressEffect = new ScaleValueEffect(mMinScale, mMaxScale, getWidth() / 2, getHeight() / 2, mDuration);
			mPressEffect.setHoldEnding(true);
			IEffect effectUp = new ScaleValueEffect(mMaxScale, mMinScale, getWidth() / 2, getHeight() / 2, mDuration);
			mEffectController.addEffect(mPressEffect);
			mEffectController.addEffect(effectUp);
			mEffectController.startAllEffect(null, this);			
		}
	}
	
	public void cleanEffect() {
		mEffectController.clearEffect();
	}
	
	public boolean hasAnimation() {
		if (mEffectController != null) {
			return !mEffectController.isAllEffectCompleted();
		}
		return false;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mImageView != null) {
			int iconSize = getIconSize();
			int iconOffsetX = 0;
			int iconOffsetY = 0;
			if (iconSize > mModelView.getWidth()) {
				iconOffsetX = (iconSize - mModelView.getWidth()) / 2;
			}
			if (iconSize > mModelView.getHeight()) {
				iconOffsetY = (iconSize - mModelView.getHeight()) / 2;
			}
			int counterSize = CounterUtil.getCounterIconSize(iconSize);
			int offset = CounterUtil.getXRightMargin(iconSize);
			int t = mModelView.getTop() - offset - iconOffsetY;
			int b = t + counterSize;
			int r = mModelView.getRight() + offset + iconOffsetX;
			int l = r - counterSize;
//			int l = (int) (mImageView.getLeft() + mWidth / 2 - mWidth * 0.17f);
//			int t = (int) (mImageView.getTop() - mWidth / 2 + mWidth * 0.17f);
//			int r = l + mImageView.getWidth();
//			int b = t + mImageView.getHeight();
			mImageView.layout(l, t, r, b);
		}
		if (mLowerRightImageView != null) {
			int l = (int) (mLowerRightImageView.getLeft() + mWidth / 2 - mWidth * 0.17f);
			int t = (int) (mLowerRightImageView.getTop() + mWidth / 2 - mWidth * 0.17f);
			int r = l + mLowerRightImageView.getWidth();
			int b = t + mLowerRightImageView.getHeight();
			mLowerRightImageView.layout(l, t, r, b);
		}

	}

	@Override
	public void getHitRect(Rect outRect) {
		outRect.set(mLeft, mTop - sHitOutPadding, mRight + sHitOutPadding, mBottom);
	}

	/**
	 * 右上角的图标被点中的监听者
	 */
	public interface OnSelectClickListener {
		/**
		 * Called when a view has been clicked.
		 * 
		 * @param v
		 *            The view that was clicked.
		 */
		void onClick(GLView v);
	}

	public void setCurrenState(int state, Object... objs) {
		if (mCurrentState != state || state == IModelState.STATE_COUNT) {
			boolean animate = true;
			if (objs != null && objs.length > 0 && objs[0] instanceof Boolean) {
				animate = (Boolean) objs[0];
			}
			int oldState = mCurrentState;
			mCurrentState = state;
			if (mImageView != null) {
				if (mCurrentState != IModelState.NO_STATE
						&& mCurrentState != IModelState.NO_SIZE_STATE) {
					Drawable drawable = getStateDrawable(state, objs);
					if (drawable != null) {
						final Drawable d = drawable;
						mImageView.clearAnimation();
						if (mImageView.isVisible()) {
							if (oldState == mCurrentState || !animate) {
								mImageView.setImageDrawable(d);
							} else {
								startCancelStateAnimation(new AnimationListenerAdapter() {
									@Override
									public void onAnimationEnd(Animation animation) {
										mImageView.setImageDrawable(d);
										startRefreshStateAnimation();
									}
								});
							}
						} else {
							mImageView.setVisible(true);
							mImageView.setImageDrawable(d);
							if (animate) {
								startRefreshStateAnimation();
							}
						}
					}
				} else if (mCurrentState == IModelState.NO_SIZE_STATE) {
					if (animate) {
						startCancelStateAnimation(new AnimationListenerAdapter() {
							@Override
							public void onAnimationEnd(Animation animation) {
								post(new Runnable() {

									@Override
									public void run() {
										mImageView.setVisibility(GLView.GONE);
									}
								});
							}
						});
					} else {
						mImageView.setVisibility(GLView.GONE);
					}
				} else {
					if (animate) {
						startCancelStateAnimation(new AnimationListenerAdapter() {
							@Override
							public void onAnimationEnd(Animation animation) {
								post(new Runnable() {

									@Override
									public void run() {
										mImageView.setVisibility(GLView.INVISIBLE);
									}
								});
							}
						});
					} else {
						mImageView.setVisibility(GLView.INVISIBLE);
					}
				}
				invalidate();
			}
		}
	}

	private Drawable getStateDrawable(int state, Object... objs) {
		Drawable drawable = mDrawableCache.get(mCurrentState);
		if (state == IModelState.UNINSTALL_STATE || state == IModelState.KILL_STATE
				|| state == IModelState.UPDATE_STATE) {
			if (drawable == null) {
				mDrawableCache.put(mCurrentState,
						IModelState.getStateDrawable(mCurrentState, objs));
			}
		}
		drawable = mDrawableCache.get(mCurrentState);
		if (drawable == null) {
			drawable = IModelState.getStateDrawable(mCurrentState, objs);
		}
		if (drawable != null) {
			drawable.setBounds(0, 0, mImageView.getWidth(), mImageView.getHeight());
			mImageView.setScaleType(ScaleType.FIT_XY);
		}
		return drawable;
	}
	
	private void startRefreshStateAnimation() {
		ScaleAnimation inAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		inAnimation.setDuration(300);
		mImageView.startAnimation(inAnimation);
	}
	
	private void startCancelStateAnimation(AnimationListener listener) {
		ScaleAnimation outAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f,
				0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		outAnimation.setDuration(300);
		outAnimation.setAnimationListener(listener);
		mImageView.startAnimation(outAnimation);
	}

	// private void setCurrenLightImage(int state) {
	// if (mCurrentState != IModelState.NO_STATE
	// && IModelState.RESLIGHTIDS[state] != -1) {
	// mImageView.setImageResource(IModelState.RESLIGHTIDS[mCurrentState]);
	// }
	// }

	@Override
	public void cleanup() {
		mModelView.cleanup();
		mSelectListener = null;
	}

	//	public void setNewAppEffrct(NewAppEffect effect) {
	//		this.mNewAppEffect = effect;
	//	}
	//
	//	public NewAppEffect getNewAppEffect() {
	//		return mNewAppEffect;
	//	}
	//
	//	public void removeEffect() {
	//		removeView(mNewAppEffect);
	//		mNewAppEffect.cleanup();
	//		mNewAppEffect = null;
	//	}

	public void setColorFilter(int srcColor, PorterDuff.Mode mode) {
		if (mModelView != null) {
			mModelView.setColorFilter(srcColor, mode);
		}
	}

	public void setAlphaFilter(int alpha) {
		if (mModelView != null) {
			mModelView.setAlphaFilter(alpha);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return super.onInterceptTouchEvent(ev);
	}

	public int getHitOutPadding() {
		return sHitOutPadding;
	}

	public Bitmap getIconTexture() {
		if (mModelView != null) {
			return mModelView.getTexture();
		}
		return null;
	}

	public GLModel3DView getModelView() {
		return mModelView;
	}

	public void setBgImageDrawable(Drawable drawable) {
		mBgView.setImageDrawable(drawable);
	}

	public Drawable getBgDrawable() {
		return mBgView.getDrawable();
	}

	public void setBgVisible(boolean visible) {
		if (mBgView != null) {
			mBgView.setVisible(visible);
		}
	}

	public void setCoverImageDrawable(Drawable drawable) {
		mCoverView.setImageDrawable(drawable);
	}

	public void setCoverVisible(boolean visible) {
		if (mCoverView != null) {
			mCoverView.setVisible(visible);
		}
	}

	/**
	 * 放大文件夹背景
	 */
	public void readyFolderBg(boolean needAnimation, BatchAnimationObserver observer) {
//		mBgView.clearAnimation();
//		mCoverView.clearAnimation();
		mBgView.setVisibility(GLView.VISIBLE);
		mReadyFolderTask = new AnimationTask(false, AnimationTask.PARALLEL);
		mReadyFolderTask.setBatchAnimationObserver(observer, OPEN_FOLDER_TASK);
		ScaleAnimation animation = new ScaleAnimation(1f, 1.24f, 1f, 1.24f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		if (needAnimation) {
			animation.setDuration(200);
		}
		animation.setFillAfter(true);
		if (mCoverView != null && mCoverView.isVisible()) {
			AnimationSet animationSet = new AnimationSet(true);
			animationSet.addAnimation(animation);
			Rotate3DAnimation rotateAnimation = new Rotate3DAnimation(0, 40,
					Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1,
					Animation.RELATIVE_TO_SELF, 0, 1, 0, 0);
			if (needAnimation) {
				rotateAnimation.setDuration(250);
				rotateAnimation.setStartOffset(200);
			}
			rotateAnimation.setFillAfter(true);
			animationSet.addAnimation(rotateAnimation);
			animationSet.setFillAfter(true);
			mReadyFolderTask.addAnimation(mCoverView, animationSet, null);
			//			mCoverView.startAnimation(animationSet);
		} else {
			AlphaAnimation alpha = new AlphaAnimation(1, 0.5f);
			if (needAnimation) {
				alpha.setDuration(200);
			}
			alpha.setFillAfter(true);
			mModelView.setHasPixelOverlayed(false);
			mReadyFolderTask.addAnimation(mModelView, alpha, null);
			//			mModelView.startAnimation(alpha);
		}
		if (mImageView != null && mCurrentState != IModelState.NO_STATE) {
			mImageView.setVisibility(GLView.INVISIBLE);
		}
		mReadyFolderTask.addAnimation(mBgView, animation, null);
		//		mBgView.startAnimation(animation);
		GLAnimationManager.startAnimation(mReadyFolderTask);
	}

	/**
	 * 缩小文件夹背景
	 * @param isFolderIcon 当前view是否文件夹
	 */
	public void cancleFolderReady(boolean needAnimation, BatchAnimationObserver observer, final boolean isFolderIcon) {
		if (!needAnimation) {
			mBgView.clearAnimation();
			mCoverView.clearAnimation();
			mModelView.clearAnimation();
			return;
		}
		mCancleBgTask = new AnimationTask(false, AnimationTask.PARALLEL);
		mCancleBgTask.setBatchAnimationObserver(observer, CLOSE_FOLDER_TASK);
		ScaleAnimation animation = new ScaleAnimation(1.24f, 1f, 1.24f, 1f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(200);
		animation.setFillAfter(false);
		animation.setStartOffset(250);
		if (mCoverView != null && mCoverView.isVisible()) {
			AnimationSet animationSet = new AnimationSet(true);
			ScaleAnimation animation2 = new ScaleAnimation(1.24f, 1f, 1.24f, 1f,
					Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			animation2.setDuration(200);
			animation2.setFillAfter(false);
			animation2.setStartOffset(250);
			Rotate3DAnimation rotateAnimation = new Rotate3DAnimation(40, 0,
					Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1,
					Animation.RELATIVE_TO_SELF, 0, 1, 0, 0);
			rotateAnimation.setDuration(250);
			rotateAnimation.setFillAfter(false);
			animationSet.addAnimation(rotateAnimation);
			animationSet.addAnimation(animation2);
			animationSet.setFillAfter(false);
			mCancleBgTask.addAnimation(mCoverView, animationSet, null);
			//			mCoverView.startAnimation(animationSet);
		} else {
			AlphaAnimation alpha = new AlphaAnimation(0.5f, 1);
			alpha.setDuration(200);
			alpha.setFillAfter(false);
			mCancleBgTask.addAnimation(mModelView, alpha, new AnimationListenerAdapter() {

				@Override
				public void onAnimationEnd(Animation animation) {
					mModelView.setHasPixelOverlayed(true);
				}
			});
			//			mModelView.startAnimation(alpha);
		}
		if (mImageView != null && mCurrentState != IModelState.NO_STATE) {
			mImageView.setVisibility(GLView.VISIBLE);
		}
		mCancleBgTask.addAnimation(mBgView, animation, new AnimationListenerAdapter() {

			@Override
			public void onAnimationEnd(Animation animation) {
				if (!isFolderIcon) {
					mBgView.setVisibility(GLView.INVISIBLE);
				}
			}
		});
		//		mBgView.startAnimation(animation);
		//		ShellContainer.setDispatchTouchEvent(false);
//		if (isBlockTouchEvent) {
//			JobManager.postJob(new Job(CLOSE_FOLDER_TASK, mCancleBgTask, false));
//		} else {
			GLAnimationManager.startAnimation(mCancleBgTask);
//		}
	}

	public void setIconSize(int size) {
		if (mModelView != null) {
			mModelView.setIconWidth(size);
			if (mBgView != null) {
				LayoutParams layoutParams = (LayoutParams) mBgView.getLayoutParams();
				layoutParams.width = mModelView.getIconWidth();
				layoutParams.height = mModelView.getIconWidth();
				mBgView.requestLayout();
			}
			if (mCoverView != null) {
				LayoutParams layoutParams = (LayoutParams) mCoverView.getLayoutParams();
				layoutParams.width = mModelView.getIconWidth();
				layoutParams.height = mModelView.getIconWidth();
				mCoverView.requestLayout();
			}
		}
	}

	public int getIconSize() {
		if (mModelView != null) {
			return mModelView.getIconWidth();
		} else {
			return -1;
		}
	}
	
	/**
	 * 设置图标右下角图标状态
	 * @param state
	 */
	public void setLowerRightState(int state) {
		if (mCurrentLowerRightStatue != state) {
			mCurrentLowerRightStatue = state;
			if (mLowerRightImageView != null) {
				if (mCurrentLowerRightStatue != IModelState.NO_STATE
						&& mCurrentLowerRightStatue != IModelState.NO_SIZE_STATE) {
					mLowerRightImageView.setVisibility(GLView.VISIBLE);
					Drawable drawable = mDrawableCache.get(mCurrentLowerRightStatue);
					if (drawable == null) {
						mLowerRightImageView.setImageDrawable(IModelState.getStateDrawable(
								mCurrentLowerRightStatue, null));
					}/* else {
						mLowerRightImageView.setImageDrawable(drawable);
						}*/
				} else if (mCurrentLowerRightStatue == IModelState.NO_SIZE_STATE) {
					mLowerRightImageView.setVisibility(GLView.GONE);
				} else {
					mLowerRightImageView.setVisibility(GLView.INVISIBLE);
				}
				invalidate();
			}
		}
	}

	/**
	 * 文件夹动画监听
	 * @author wuziyi
	 *
	 */
	public interface FolderCoverAnimationListener {
		/**
		 * 文件夹 盖子/图标底背景 动画完成
		 * @param isOpened true：代表文件夹打开动画完成。false：代表文件夹关闭动画完成。
		 */
		public void onFolderCoverAnimationEnd(boolean isOpened, GLView icon);
	}
	
}
