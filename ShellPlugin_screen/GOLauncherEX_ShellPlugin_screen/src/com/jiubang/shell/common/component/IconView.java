package com.jiubang.shell.common.component;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.graphics.BitmapGLDrawable;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLTextViewWrapper;
import com.go.proxy.GoLauncherLogicProxy;
import com.go.proxy.SettingProxy;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.launcher.IconUtilities;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.animation.AsyncRotateAnimation;
import com.jiubang.shell.common.component.GLModel3DMultiView.FolderCoverAnimationListener;
import com.jiubang.shell.common.listener.OnLayoutListener;
import com.jiubang.shell.common.listener.TransformListener;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.effect.EffectController.EffectListener;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.utils.IconUtils;

/**
 * 
 * <br>
 * 类描述: 图标类，包含图标和文字信息 <br>
 * 功能详细描述:
 * 
 * @author panguowei
 * @param <T>
 */
public abstract class IconView<T> extends GLLinearLayout implements TransformListener, BatchAnimationObserver {
	public static boolean sEnableStateAnimation = true;
	
	public static final int DEFAULT_TEXT_MAX_LINES = 2;
	public static final int DEFAULT_TEXT_MIN_LINES = 1;

	// 点击时候的半透效果
	public static final int CLICK_HALF_ALPHA = 128;

	// 不透明的效果
	public static final int CLICK_NO_ALPHA = 255;

	/**
	 * 图标锚点的取值范围
	 */
	private final float mRangeStart = 0.35f;
	private final float mRangeEnd = 0.65f;
	/**
	 * 初始的旋转角度
	 */
	private float mDegree;
	private int mExtraWidth = 2;
	private int mChangeAlpha = CLICK_NO_ALPHA;
	protected GLView mIconView = null;
	protected GLView mTextView = null;

	/**
	 * 锚点X
	 */
	private int mAchorPicX;
	/**
	 * 锚点Y
	 */
	private int mAchorPicY;

	/*private NinePatchGLDrawable mSortBitmap = null;
	private boolean mIsShowSortBitmap = false;*/

	//	private ValueAnimation mValueAnimation = null; // 用于放大缩小做动画
	/*	private float mScale = 0; // 目前播放到的scale比例值
		private boolean mIsInAnimation = false; // 进入的放大动画中
		private boolean mOutAnimation = false; // 放大动画结束是否需要播放缩小动画
	*/
	// private boolean mIsPress = false;
	private boolean mIsShowPress = true; // touch时播放光圈效果

	private boolean mCanClean = true;

	private OnLayoutListener mLayoutListener;

	/*	private Rect mTempRect = new Rect();*/

	protected T mInfo;

	/**
	 * 拖拽响应区域数组
	 */
	//	protected Rect[] mOperationArea;
	protected Rect mMaxInnerRect = new Rect();
	protected int[] mLoc = new int[2];

	protected TransformationInfo mTFInfo;
	
	private int mIconTextLine;
	
	private FolderCoverAnimationListener mFolderIconAnimationListener; 

	protected boolean mAutoFit = false;
	
	private static IconView<?> sPressView;
	
	protected boolean mEnableAutoTextLine = false;
	
	public IconView(Context context) {
		this(context, null);
	}

	public IconView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// CHECKSTYLE IGNORE
		mExtraWidth = (int) (context.getResources().getDisplayMetrics().density * mExtraWidth);
		//		mValueAnimation = new ValueAnimation(0);
		//		mOperationArea = new Rect[2];
		setHasPixelOverlayed(false);
	}

	@Override
	public void setAlpha(int alpha) {
		super.setAlpha(alpha);
		mChangeAlpha = alpha;
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		final int oldAlpha = canvas.getAlpha();

		if (mChangeAlpha != CLICK_NO_ALPHA) {
			canvas.multiplyAlpha(mChangeAlpha);
		}
		if (mDegree != 0) {
			canvas.rotate(mDegree, getWidth() / 2, getHeight() / 2);
		}
		if (mTFInfo != null) {
			int saveCount = canvas.save();
			canvas.scale(mTFInfo.mScaleX, mTFInfo.mScaleY, mTFInfo.mPivotX, mTFInfo.mPivotY);
			canvas.translate(mTFInfo.mTranslationX, mTFInfo.mTranslationY);
			super.dispatchDraw(canvas);
			canvas.restoreToCount(saveCount);
		} else {
			super.dispatchDraw(canvas);
		}

		canvas.setAlpha(oldAlpha);

		//		if (mValueAnimation.animate()) {
		//			invalidate();
		//		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mIconView = getChildAt(0);
		mTextView = getChildAt(1);
		if (mTextView != null) {
			GLTextViewWrapper txtWrapper = (GLTextViewWrapper) mTextView;
			txtWrapper.showTextShadow();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void getHitRect(Rect outRect) {
		if (mIconView != null && mTextView != null) {
			mIconView.getHitRect(outRect);
			if (mTextView.getVisibility() != GONE) {
				// 显示程序文字，计算图标和文字哪个宽确定点击范围
				outRect.left = Math.min(outRect.left, mTextView.getLeft());
				outRect.right = Math.max(outRect.right, mTextView.getRight());
				outRect.bottom = Math.max(outRect.bottom, mTextView.getBottom());
			}
		}
		outRect.right += mLeft;
		outRect.bottom += mTop;
		outRect.left += mLeft;
		outRect.top += mTop;
	}

	public void setDegree(float degree) {
		mDegree = degree;
	}

	public float getDegree() {
		return mDegree;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mLayoutListener != null) {
			mLayoutListener.onLayoutFinished(this);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mEnableAutoTextLine) {
			autoFitTextLine();
		}
	}

	private void autoFitTextLine() {
		if (mTextView != null) {
			int iconHeight = 0;
			int textHeight = 0;
			if (mIconView != null) {
				iconHeight = mIconView.getHeight();
			}
			if (mTextView != null) {
				GLTextViewWrapper txtWrapper = (GLTextViewWrapper) mTextView;
				int lineHeight = txtWrapper.getTextView().getLineHeight();
				textHeight = lineHeight * 2 + IconUtils.getIconTextPaddingTop();
				if (mMeasuredHeight < iconHeight + textHeight) {
					if (mIconTextLine != DEFAULT_TEXT_MIN_LINES) {
						txtWrapper.setSingleLine();
						txtWrapper.setMinLines(DEFAULT_TEXT_MAX_LINES);
						txtWrapper.setMaxLines(DEFAULT_TEXT_MIN_LINES);
						mIconTextLine = DEFAULT_TEXT_MIN_LINES;
					}
				} else {
					if (mIconTextLine != DEFAULT_TEXT_MAX_LINES) {
						txtWrapper.setSingleLine(false);
						txtWrapper.setMinLines(DEFAULT_TEXT_MAX_LINES);
						txtWrapper.setMaxLines(DEFAULT_TEXT_MAX_LINES);
						mIconTextLine = DEFAULT_TEXT_MAX_LINES;
					}
				}
			}
		}
	}

	public int getTextLineHeight() {
		if (mTextView != null && mTextView.isVisible()) {
			GLTextViewWrapper txtWrapper = (GLTextViewWrapper) mTextView;
			int lineHeight = txtWrapper.getTextView().getLineHeight();
			return lineHeight;
		}
		return 0;
	}

	public int getTextPadddingTop() {
		return IconUtils.getIconTextPaddingTop();
	}

	public static int getIconHeight(int textLine) {
		int totalHeight = 0;
		Context context = ShellAdmin.sShellManager.getContext();
		TextView textView = new TextView(context);
		textView.setTextSize(GoLauncherLogicProxy.getAppFontSize());
		int lineHeight = textView.getLineHeight();
		int iconHeight = IconUtilities.getIconSize(ShellAdmin.sShellManager.getActivity());
		totalHeight = lineHeight * textLine + IconUtils.getIconTextPaddingTop() + iconHeight;
		return totalHeight;
	}

	public void setLayoutListener(OnLayoutListener listener) {
		mLayoutListener = listener;
	}

	public GLView getTextView() {
		return mTextView;
	}
	/*
		*//**
		* <br>
		* 功能简述: 是否显示对齐的那个背景图 <br>
		* 功能详细描述: <br>
		* 注意:
		* 
		* @param isShow
		*/
	/*
	public void isShowSortingBG(boolean isShow) {
	if (mIsShowSortBitmap != isShow) {
		mIsShowSortBitmap = isShow;
		if (mSortBitmap == null) {
			mSortBitmap = new NinePatchGLDrawable(
					(NinePatchDrawable) getResources().getDrawable(
							R.drawable.auto_sorint_bg));
			mSortBitmap.setBounds(0, 0, getWidth(), getHeight());
		}
		if (mIsShowSortBitmap) {
			mIsInAnimation = true;
			mOutAnimation = false;
			mValueAnimation.start(mScale, 1, (long) (200 * (1 - mScale))); // CHECKSTYLE
																			// IGNORE
		} else if (mIsInAnimation) {
			mOutAnimation = true;
		} else {
			mValueAnimation.start(1, 0, 200); // CHECKSTYLE IGNORE
		}
		invalidate();
	}
	}*/

	/**
	 * <br>
	 * 功能简述:是否打开应用程序名称显示的功能 <br>
	 * 功能详细描述: <br>
	 * 注意:设置后程序名称的view会变为gone，排版改变
	 * 
	 * @param enable
	 */
	public void setEnableAppName(boolean enable) {
		if (enable) {
			if (mTextView.getVisibility() == GONE) {
				mTextView.setVisible(true);
			}
		} else if (mTextView.getVisibility() != GONE) {
			mTextView.setVisibility(GONE);
		}
	}

	/**
	 * <br>
	 * 功能简述:是否显示程序名 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	public boolean isEnableAppName() {
		if (mTextView == null) {
			return false;
		} else {
			return mTextView.getVisibility() != GONE;
		}
	}

	@Override
	public void cleanup() {

		if (!mCanClean) {
			return;
		}

		/*if (mSortBitmap != null) {
			mSortBitmap.clear();
			mSortBitmap = null;
		}*/
		if (sPressView == this) {
			sPressView = null;
		}
		mIconView = null;
		mTextView = null;
		super.cleanup();
		mInfo = null;
	}

	public void setColorFilter(int srcColor, Mode mode) {
		if (mIconView instanceof GLModel3DMultiView) {
			GLModel3DMultiView icon = (GLModel3DMultiView) mIconView;
			icon.setColorFilter(srcColor, mode);
		} else if (mIconView instanceof GLModel3DView) {
			GLModel3DView icon = (GLModel3DView) mIconView;
			icon.setColorFilter(srcColor, mode);
		}
	}

	public void setAlphaFilter(int alpha) {
		if (mIconView instanceof GLModel3DMultiView) {
			GLModel3DMultiView icon = (GLModel3DMultiView) mIconView;
			icon.setAlphaFilter(alpha);
		} else if (mIconView instanceof GLModel3DView) {
			GLModel3DView icon = (GLModel3DView) mIconView;
			icon.setAlphaFilter(alpha);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		boolean result = super.onTouchEvent(event);

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN : {
				if (!mIsShowPress) {
					return result;
				}

				if (mIconView != null) {
					if (mIconView instanceof GLModel3DMultiView) {
						sPressView = this;
						GLModel3DMultiView icon = (GLModel3DMultiView) mIconView;
						icon.startPressAnimation();
					} else if (mIconView instanceof GLModel3DView) {
						//去掉触摸就出现水波纹的效果
//						GLModel3DView icon = (GLModel3DView) mIconView;
//						icon.startTouchAnimation(null, mIsShowPress);
					}

				}
				// setPressFlag(false);
				break;
			}
			case MotionEvent.ACTION_MOVE :
			case MotionEvent.ACTION_CANCEL :				
				if (mIconView != null) {
					mIconView.onTouchEvent(event);
				}
				break;
			
			default : {
				// setPressFlag(false);
				break;
			}
		}
		return result;
	}

	// public void isPressAnim(boolean flag) {
	// if (flag) {
	// if (mIconView != null) {
	// if (mIconView instanceof GLModel3DMultiView) {
	// GLModel3DMultiView icon = (GLModel3DMultiView) mIconView;
	// icon.startPressAnimation();
	// } else if (mIconView instanceof GLModel3DView) {
	// GLModel3DView icon = (GLModel3DView) mIconView;
	// icon.startClickAnimation(null);
	// }
	// }
	// }
	// }

	// public void setPressFlag(boolean flag) {
	// mIsPress = flag;
	// }

	/**
	 * <br>
	 * 功能简述:设置touch时播放光圈效果动画 <br>
	 * 功能详细描述: 如果不需要光圈动画，直接设置 false 否则设置为true <br>
	 * 注意:
	 * 
	 * @param flag
	 */
	public void setPressAnimFlag(boolean flag) {
		mIsShowPress = flag;
	}

	public void setIsCanClean(boolean clean) {
		mCanClean = clean;
	}

	public void setInfo(T info) {
		mInfo = info;
		refreshIcon();
	}

	public T getInfo() {
		return mInfo;
	}

	public GLView getIconView() {
		return mIconView;
	}

	public abstract void setTitle(CharSequence title);
	public abstract void setIcon(BitmapDrawable drawable);
	public abstract void refreshIcon();
	public abstract void onIconRemoved();
	public abstract void reloadResource();

	public int getIconSize() {
		if (mIconView != null && mIconView instanceof GLModel3DMultiView) {
			GLModel3DMultiView multiView = (GLModel3DMultiView) mIconView;
			return multiView.getIconSize();
		} else {
			GLView glView = findViewById(R.id.model);
			if (glView != null && glView instanceof GLModel3DView) {
				GLModel3DView model3dView = (GLModel3DView) glView;
				return model3dView.getIconWidth();
			}
		}
		return -1;
	}

	public void setIconSize(int size) {
		if (mIconView != null && mIconView instanceof GLModel3DMultiView) {
			GLModel3DMultiView multiView = (GLModel3DMultiView) mIconView;
			multiView.setIconSize(size);
		} else {
			GLView glView = findViewById(R.id.model);
			if (glView != null && glView instanceof GLModel3DView) {
				GLModel3DView model3dView = (GLModel3DView) glView;
				model3dView.setIconWidth(size);
			}
		}
	}

	protected IconRefreshObserver mIconRefreshObserver;

	public void registerIconRefreshObserver(IconRefreshObserver observer) {
		mIconRefreshObserver = observer;
	}

	public void unregisterIconRefreshObserver() {
		mIconRefreshObserver = null;
	}

	/**
	 * 
	 * @author yangguanxiang
	 *
	 */
	public static interface IconRefreshObserver {
		public void onIconRefresh();
	}

	public int getAchorPicX() {
		return mAchorPicX;
	}

	public int getAchorPicY() {
		return mAchorPicY;
	}

	public void calculateAchorPicXY() {
		if (mAchorPicX == 0 && mAchorPicY == 0) {
			float tmp = mRangeStart + (mRangeEnd - mRangeStart) * (float) Math.random();
			if (mIconView != null) {
				mAchorPicX = (int) (mIconView.getWidth() * tmp);
				tmp = mRangeStart + (mRangeEnd - mRangeStart) * (float) Math.random();
				mAchorPicY = (int) (mIconView.getHeight() * tmp);
			}
		}
	}

	/**
	 * 获取响应区域
	 * return Rect[] - 0:OuterRect, 1:InnerRect
	 */
	public Rect[] getOperationArea(Rect[] rect, Object... params) {
		if (rect == null) {
			rect = new Rect[2];
		}
		//		ShellAdmin.sShellManager.getShell().getDragLayer().getLocationInDragLayer(this, mLoc);
		//		int left = mLoc[0];
		//		int top = mLoc[1];
		//		int right = left + mWidth;
		//		int bottom = top + mHeight;
		//		if (mOperationArea[0] == null) {
		//			mOperationArea[0] = new Rect(left, top, right, bottom);
		//		} else {
		//			mOperationArea[0].left = left;
		//			mOperationArea[0].top = top;
		//			mOperationArea[0].right = right;
		//			mOperationArea[0].bottom = bottom;
		//		}
		//
		//		if (mOperationArea[1] == null) {
		//			mOperationArea[1] = new Rect();
		//		}
		//		mOperationArea[1].left = mOperationArea[0].left + mOperationArea[0].width() / 5;
		//		mOperationArea[1].right = mOperationArea[0].right - mOperationArea[0].width() / 5;
		//		mOperationArea[1].top = mOperationArea[0].top + mOperationArea[0].height() / 5;
		//		mOperationArea[1].bottom = mOperationArea[0].bottom - mOperationArea[0].height() / 5;
		//
		//		getHitRect(mMaxInnerRect);
		//		if (mOperationArea[1].contains(mMaxInnerRect)) {
		//			mOperationArea[1] = mMaxInnerRect;
		//		}
		//		return mOperationArea;

		ShellAdmin.sShellManager.getShell().getContainer().getLocation(this, mLoc);
		int left = mLoc[0];
		int top = mLoc[1];
		int right = left + mWidth;
		int bottom = top + mHeight;
		if (rect[0] == null) {
			rect[0] = new Rect(left, top, right, bottom);
		} else {
			rect[0].left = left;
			rect[0].top = top;
			rect[0].right = right;
			rect[0].bottom = bottom;
		}

		if (rect[1] == null) {
			rect[1] = new Rect();
		}
		rect[1].left = rect[0].left + rect[0].width() / 5;
		rect[1].right = rect[0].right - rect[0].width() / 5;
		rect[1].top = rect[0].top + rect[0].height() / 5;
		rect[1].bottom = rect[0].bottom - rect[0].height() / 5;

		getHitRect(mMaxInnerRect);
		if (rect[1].contains(mMaxInnerRect)) {
			rect[1] = mMaxInnerRect;
		}
		return rect;
	}

	public Bitmap getIconTexture() {
		if (mIconView != null && mIconView instanceof GLModel3DMultiView) {
			return ((GLModel3DMultiView) mIconView).getIconTexture();
		}
		return null;
	}

	public void showTitle(final boolean show, boolean animate) {
		if (mTextView != null && mTextView.isVisible() != show) {
			mTextView.clearAnimation();
			if (animate) {
				int fromAlpha = 0;
				int toAlpha = 1;
				if (!show) {
					fromAlpha = 1;
					toAlpha = 0;
				} else {
					mTextView.setVisible(show);
				}
				AlphaAnimation alphaAnimation = new AlphaAnimation(fromAlpha, toAlpha);
				alphaAnimation.setStartTime(Animation.START_ON_FIRST_FRAME);
				alphaAnimation.setDuration(250);
				alphaAnimation.setAnimationListener(new AnimationListenerAdapter() {

					@Override
					public void onAnimationEnd(Animation animation) {
						if (!show) {
							mTextView.post(new Runnable() {

								@Override
								public void run() {
									mTextView.setVisible(show);
								}
							});

						}
					}
				});
				mTextView.setAnimation(alphaAnimation);
			} else {
				mTextView.setVisible(show);
			}

		}
	}
	/**
	 * 停止所有动画
	 */
	public void stopShake() {
		if (mIconView != null) {
//			mIconView.setDrawingCacheEnabled(false);
			mIconView.clearAnimation();
		}
	}

	/**
	 * <br>
	 * 功能简述:抖动动画开启 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	public void startShake() {
		if (mIconView != null) {
//			int[] loc = new int[2];
//			ShellAdmin.sShellManager.getShell().getContainer()
//					.getDrawingCacheAnchor(mIconView, loc);
//			Point p = new Point(loc[0], loc[1]);
//			mIconView.setDrawingCacheAnchor(p);
//			Transformation3D t = new Transformation3D();
//			t.setTranslate(loc[0], loc[1]);
//			t.
//			mIconView.setDrawingCacheTransform(t);
//			mIconView.setDrawingCacheEnabled(true);

			calculateAchorPicXY();
			Animation animation = new AsyncRotateAnimation(getAchorPicX(), getAchorPicY(), 2.5f,
					-2.5f, true);
			animation.setDuration(200);
			animation.setInterpolator(InterpolatorFactory
					.getInterpolator(InterpolatorFactory.LINEAR));
			animation.setRepeatCount(Animation.INFINITE);
			mIconView.startAnimation(animation);

		}
	}
	
	public void set3DMultiViewScale(float scale) {
		if (mIconView != null && mIconView instanceof GLModel3DMultiView) {
			GLModel3DMultiView multiView = (GLModel3DMultiView) mIconView;
			multiView.setScale(scale);
			multiView.invalidate();
			if (scale == 1.0f && sPressView == this) {
				sPressView = null;
			}
		}
	}

	public float get3DMultiViewScale() {
		if (mIconView != null && mIconView instanceof GLModel3DMultiView) {
			GLModel3DMultiView multiView = (GLModel3DMultiView) mIconView;
			return multiView.getScale();
		}
		return 1.0f;
	}

	public void start3DMultiViewUpAnimation() {
		if (mIconView != null && mIconView instanceof GLModel3DMultiView) {
			GLModel3DMultiView multiView = (GLModel3DMultiView) mIconView;
			multiView.startUpAnimation();
		}
	}

	/**
	 * 点击图标有缩放效果
	 * @param listener
	 */
	public void startClickEffect(EffectListener listener, long duration,
			boolean allowDispatchTouchEvent) {
		if (mIconView != null && mIconView instanceof GLModel3DMultiView) {
			GLModel3DMultiView multiView = (GLModel3DMultiView) mIconView;
			multiView.startClickEffect(listener, this, duration, allowDispatchTouchEvent);
		}
	}
	
	public void cleanEffect() {
		if (mIconView instanceof GLModel3DMultiView) {
			GLModel3DMultiView multiView = (GLModel3DMultiView) mIconView;
			multiView.cleanEffect();
		}
	}
	
	public boolean hasAnimation() {
		if (mIconView != null && mIconView instanceof GLModel3DMultiView) {
			GLModel3DMultiView multiView = (GLModel3DMultiView) mIconView;
			return multiView.hasAnimation();
		}
		return false;
	}

	/**
	 * 准备合成文件夹，在图标背后生成文件夹背景
	 */
	public void readyForFolder(boolean needAnimation) {
		if (mIconView != null && mIconView instanceof GLModel3DMultiView) {
			GLModel3DMultiView icon = (GLModel3DMultiView) mIconView;
			icon.readyFolderBg(needAnimation, this);
		}
	}

	/**
	 * 取消当前view的准备文件夹状态，默认使用动画
	 */
	public void cancleFolderReady() {
		cancleFolderReady(true);
	}
	
	public void cancleFolderReady(boolean needAnimation) {
		if (mIconView instanceof GLModel3DMultiView) {
			GLModel3DMultiView icon = (GLModel3DMultiView) mIconView;
			icon.cancleFolderReady(needAnimation, this, false);
		}
	}

	public void setFolderCoverAnimationListner(FolderCoverAnimationListener listener) {
		mFolderIconAnimationListener = listener;
//		if (mIconView instanceof GLModel3DMultiView) {
//			GLModel3DMultiView icon = (GLModel3DMultiView) mIconView;
//			icon.setFolderIconAnimationListener(listener);
//		}
	}

	@Override
	public void setScaleXY(float scaleX, float scaleY) {
		if (mTFInfo != null) {
			mTFInfo.mScaleX = scaleX;
			mTFInfo.mScaleY = scaleY;
		}
	}

	@Override
	public void setPivotXY(float pivotX, float pivotY) {
		if (mTFInfo != null) {
			mTFInfo.mPivotX = pivotX;
			mTFInfo.mPivotY = pivotY;
		}
	}

	@Override
	public void setTranslateXY(float transX, float transY) {
		if (mTFInfo != null) {
			mTFInfo.mTranslationX = transX;
			mTFInfo.mTranslationY = transY;
		}
	}

	@Override
	public void setTransformationInfo(TransformationInfo info) {
		mTFInfo = info;
	}

	@Override
	public TransformationInfo getTransformationInfo() {
		return mTFInfo;
	}

	@Override
	public void setAutoFit(boolean autoFit) {
		mAutoFit = autoFit;
	}
	
	@Override
	public void animateToSolution() {

	}

	public void drawIcon(GLCanvas canvas) {
		GLModel3DView icon = null;
		if (mIconView instanceof GLModel3DMultiView) {
			GLModel3DMultiView multiView = (GLModel3DMultiView) mIconView;
			icon = multiView.getModelView();
		} else if (mIconView instanceof GLModel3DView) {
			icon = (GLModel3DView) mIconView;
		}
		if (icon != null) {
			icon.dispatchDraw(canvas);
		}
	}

//	/**
//	 * 应用搜索时，图标被定位的背景
//	 */
	public void startLocatAppAnimation() {
		new LocateAppAnimation(LocateAppAnimation.START_LOCATE_ANIMATION);
	}
	
	private static final float CORNER_RADIUS = 8.0f;
	private float mCornerRadius;
	// 透明度参数
	//	private int mAlpha = 255;
	private int mLabelShadowColor; // 标签后面的圆角矩形阴影的颜色
	private Bitmap mTextBg = null; //  文字背景

	//	private Drawable mBackground; //selector

	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	public void initIconFromSetting(boolean isShowTitleBg) {
		Resources res = getResources();
		mLabelShadowColor = res.getColor(R.color.bubble_dark_background);
		DeskThemeControler themeControler = AppCore.getInstance().getDeskThemeControler();
		if (themeControler != null && themeControler.isUsedTheme()) {
			DeskThemeBean themeBean = themeControler.getDeskThemeBean();
			if (themeBean != null && themeBean.mScreen != null) {
				if (themeBean.mScreen.mIconStyle != null) {
					if (themeBean.mScreen.mIconStyle.mIconBackgroud != null) {
						if (themeBean.mScreen.mIconStyle.mIconBackgroud.mColor != 0) {
							mLabelShadowColor = themeBean.mScreen.mIconStyle.mIconBackgroud.mColor;
						}
					}
				}
			}
		}


		if (GoLauncherLogicProxy.getCustomTitleColor()) {
			int color = GoLauncherLogicProxy.getAppTitleColor();
			if (color != 0) {
				setTitleColor(color);
			} else {
				setTitleColor(Color.WHITE);
			}
		} else {
			if (themeControler != null && themeControler.isUsedTheme()) {
				DeskThemeBean themeBean = themeControler.getDeskThemeBean();
				if (themeBean != null && themeBean.mScreen != null) {
					if (null != themeBean.mScreen.mFont) {
						int color = themeBean.mScreen.mFont.mColor;
						if (color == 0) {
							setTitleColor(Color.WHITE);
						} else {
							setTitleColor(color);
						}
					}
				}
			} else {
				setTitleColor(Color.WHITE);
			}
		}

		final float scale = DrawUtils.sDensity;
		mCornerRadius = CORNER_RADIUS * scale;

//		setFontTypeFace(GoLauncher.getAppTypeface(), GoLauncher.getAppTypefaceStyle());

		if (isShowTitleBg && isEnableAppName()) {
			showTitle(GoLauncherLogicProxy.getIsShowAppTitle(), false);
			setTextViewBg(GoLauncherLogicProxy.getIsShowAppTitleBg());
		}
		
		setFontSize(GoLauncherLogicProxy.getAppFontSize());
	}

	/**
	 * <br>功能简述:切换主题后，某些主题是需要修改字体颜色的。
	 * <br>功能详细描述:这里用来设置修改主题后，设置的字体颜色。而非设置项里面的颜色。
	 * <br>注意:
	 */
	public void refreshScreenIconTextColor() {
	    
	    DeskThemeControler themeControler = AppCore.getInstance().getDeskThemeControler();
	    if (GoLauncherLogicProxy.getCustomTitleColor()) {
            int color = GoLauncherLogicProxy.getAppTitleColor();
            if (color != 0) {
                setTitleColor(color);
            } else {
                setTitleColor(Color.WHITE);
            }
        } else {
            if (themeControler != null && themeControler.isUsedTheme()) {
                DeskThemeBean themeBean = themeControler.getDeskThemeBean();
                if (themeBean != null && themeBean.mScreen != null) {
                    if (null != themeBean.mScreen.mFont) {
                        int color = themeBean.mScreen.mFont.mColor;
                        if (color == 0) {
                            setTitleColor(Color.WHITE);
                        } else {
                            setTitleColor(color);
                        }
                    }
                }
            } else {
                setTitleColor(Color.WHITE);
            }
        }
	}
	
	/**
	 * 文字背景
	 */
	public void setTextViewBg(boolean isShow) {
		GLTextViewWrapper txtWrapper = (GLTextViewWrapper) mTextView;
		if (txtWrapper == null) {
			return;
		}
		if (!isShow) {
			txtWrapper.setTextPadding(0, 0, 0, 0);
		} else {
			int padding = DrawUtils.dip2px(2);
			txtWrapper.setTextPadding(padding, 0, padding, 0);
		}

		if (txtWrapper.getWidth() == 0 || txtWrapper.getHeight() == 0) {
			return;
		}

		if (!isShow) {
			txtWrapper.setBackgroundDrawable(null);
		} else {
			Canvas canvas = new Canvas();
			if (mTextBg == null) {
				mTextBg = Bitmap.createBitmap(txtWrapper.getWidth(), txtWrapper.getHeight(),
						Config.ARGB_8888);
			} else {
				if (mTextBg.getWidth() != txtWrapper.getWidth()
						|| mTextBg.getHeight() != txtWrapper.getHeight()) {
					mTextBg.recycle();
					mTextBg = Bitmap.createBitmap(txtWrapper.getWidth(), txtWrapper.getHeight(),
							Config.ARGB_8888);
				} else {
					mTextBg.eraseColor(Color.TRANSPARENT);
				}
			}

			canvas.setBitmap(mTextBg);
			RectF rect = new RectF(0, 0, txtWrapper.getWidth(), txtWrapper.getHeight());
			//			if (mAlpha < 255) {
			//				final int shadowAlpha = mLabelShadowColor >>> 24;
			//				final int newColor = mAlpha * shadowAlpha >> 8 << 24;
			//				mPaint.setColor(newColor);
			//			} else {
			mPaint.setColor(mLabelShadowColor);
			//			}
			canvas.drawRoundRect(rect, mCornerRadius, mCornerRadius, mPaint);
			BitmapGLDrawable bd = new BitmapGLDrawable(new BitmapDrawable(mTextBg));
			txtWrapper.setBackgroundDrawable(bd);
		}
	}

	public void setFontSize(int fontSize) {
		GLTextViewWrapper txtWrapper = (GLTextViewWrapper) mTextView;
		txtWrapper.setTextSize(fontSize);
	}

	public void setTitleColor(int color) {
		if (mTextView == null) {
			return;
		}
		GLTextViewWrapper txtWrapper = (GLTextViewWrapper) mTextView;
		txtWrapper.setTextColor(color);
		if (color == Color.WHITE && !getShadowState()) {
			txtWrapper.showTextShadow();
		} else {
			txtWrapper.hideTextShadow();
		}
	}
	
	private boolean getShadowState() {
		DesktopSettingInfo desktopSettingInfo = SettingProxy.getDesktopSettingInfo();
		if (desktopSettingInfo != null) {
			return !desktopSettingInfo.isTransparentBg();
		}
		return true;
	}

	public void setCurrenState(int state, Object... objs) {
		if (mIconView instanceof GLModel3DMultiView) {
			GLModel3DMultiView icon = (GLModel3DMultiView) mIconView;
			icon.setCurrenState(state, objs);
		}
	}
	
	public void setLowerRightState(int state) {
		if (mIconView instanceof GLModel3DMultiView) {
			GLModel3DMultiView icon = (GLModel3DMultiView) mIconView;
			icon.setLowerRightState(state);
		}
	}
	
	public abstract void checkSingleIconNormalStatus();
	
	/**
	 * 
	 * @author liuheng
	 *
	 */
	class LocateAppAnimation extends AnimationListenerAdapter {
		int mAnimationStep = -1;
		public static final int START_LOCATE_ANIMATION = 0;
		public static final float SCALEFACTOR = 1.3f;
		public LocateAppAnimation(int animationStep) {
			mAnimationStep = animationStep;
			if (mAnimationStep == START_LOCATE_ANIMATION) {
				ScaleAnimation scaleAnimation1 = new ScaleAnimation(1.0f, SCALEFACTOR, 1.0f,
						SCALEFACTOR, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
						0.5f);
				scaleAnimation1.setAnimationListener(this);
				scaleAnimation1.setDuration(300); //设置动画持续时间 
				setAnimation(scaleAnimation1);
				scaleAnimation1.startNow();
			}
		}
		@Override
		public void onAnimationEnd(Animation arg0) {
			ScaleAnimation scaleAnimation = null;
			switch (mAnimationStep) {
				case START_LOCATE_ANIMATION :
					scaleAnimation = new ScaleAnimation(SCALEFACTOR, 1.0f, SCALEFACTOR, 1.0f,
							Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					break;
				case 1 :
					scaleAnimation = new ScaleAnimation(1.0f, SCALEFACTOR, 1.0f, SCALEFACTOR,
							Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					break;
				case 2 :
					scaleAnimation = new ScaleAnimation(SCALEFACTOR, 1.0f, SCALEFACTOR, 1.0f,
							Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					break;
				case 3 :
					ShellAdmin.sShellManager.getShell().hide(IViewId.PROTECTED_LAYER, false);
					break;
				default :
					break;
			}
			mAnimationStep++;
			if (scaleAnimation != null) {
				scaleAnimation.setAnimationListener(this);
				scaleAnimation.setDuration(300);
				setAnimation(scaleAnimation);
				scaleAnimation.startNow();
			}
		}
	}
	
	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		if (isPressed()) {
			setPressed(false);
		}
	}
	
	@Override
	public void onStart(int what, Object[] params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFinish(int what, Object[] params) {
		switch (what) {
			case GLModel3DMultiView.OPEN_FOLDER_TASK :
				if (mFolderIconAnimationListener != null) {
					mFolderIconAnimationListener.onFolderCoverAnimationEnd(true, this);
				}
				break;
			case GLModel3DMultiView.CLOSE_FOLDER_TASK :
				if (mFolderIconAnimationListener != null) {
					mFolderIconAnimationListener.onFolderCoverAnimationEnd(false, this);
				}
				break;

			default :
				break;
		}
	}
	
	public static void resetIconPressState() {
		if (sPressView != null) {
			sPressView.start3DMultiViewUpAnimation();
			sPressView = null;
		}
	}
	
	public void setTitleSingleLine(boolean singleLine) {
		if (mTextView != null) {
			GLTextViewWrapper txtWrapper = (GLTextViewWrapper) mTextView;
			TextView txtView = txtWrapper.getTextView();
			txtView.setSingleLine(singleLine);
			if (singleLine) {
				txtView.setMaxLines(DEFAULT_TEXT_MIN_LINES);
			}
		}
	}

	public boolean isEnableAutoTextLine() {
		return mEnableAutoTextLine;
	}

	/**
	 * 是否开启行列数自适应（默认开启，最大2行，最少1行）
	 * @param enableAutoTextLine
	 */
	public void setEnableAutoTextLine(boolean enableAutoTextLine) {
		this.mEnableAutoTextLine = enableAutoTextLine;
	}
}
