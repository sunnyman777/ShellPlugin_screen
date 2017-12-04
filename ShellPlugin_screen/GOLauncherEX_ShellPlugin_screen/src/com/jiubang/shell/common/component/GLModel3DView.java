package com.jiubang.shell.common.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.animation.Interpolator;

import com.go.gl.graphics.BitmapGLDrawable;
import com.go.gl.graphics.BitmapTexture;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLFrameLayout;
import com.jiubang.ggheart.launcher.IconUtilities;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.model.IModelItemType;

/**
 * 
 * <br>
 * 类描述: 主要绘制程序图标，包括功能表图标和屏幕层图标 <br>
 * 功能详细描述:
 * 
 * @author chaoziliang
 * @date [2012-9-7]
 */
public class GLModel3DView extends GLFrameLayout {

	// private ModelItem mModelItem = null; 模型相关
	protected int mIconWidth;
	private int mType = IModelItemType.INVALID_MODEL;

	protected BitmapGLDrawable mGlBitmap = null;

	// private BitmapGLDrawable mGLShadowBitmap = null;



	// -----------------------功能表特殊图标的发亮动画相关 -------------------//
	private boolean mDrawLight = false;
	private long mDrawTime;
	private Interpolator mAlphaInterpolator;
	private static final float ANIMATION_DURATION_1000 = 1000.0f;
	// -----------------------功能表特殊图标的发亮动画相关 -------------------//

	// -------------------------文件夹界面加号--------------------------------//
	private boolean mIsAddIcon = false;
//	private static BitmapGLDrawable sFolderCircleBitmap = null;
	// -------------------------文件夹界面加号--------------------------------//

	//-------------------------罩子-----------------------------//
//	private boolean mIsDrawCover = false;
//	protected BitmapGLDrawable mCoverDrawable;
	//-------------------------罩子-----------------------------//

	public GLModel3DView(Context context) {
		super(context);
		// mModelItem = new ModelItem(context);
		initView();
	}

	public GLModel3DView(Context context, int mIconWidth, int mIconHeight) {
		super(context);
		this.mIconWidth = mIconHeight;
		initView();
	}

	public GLModel3DView(Context context, int modelType) {
		super(context);
		// mModelItem = new ModelItem(context, modelType);
		initView();
	}

	public GLModel3DView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GLModel3DView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// mModelItem = new ModelItem(context);
		initView();
	}

	/**
	 * <br>功能简述:初始化
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initView() {
		if (mIconWidth == 0) {
			//			if (!Machine.isTablet(mContext)) {
			//				mIconWidth = (int) getResources().getDimension(R.dimen.app_icon_size);
			//			} else {
			//				mIconWidth = (int) getResources().getDimension(R.dimen.app_icon_size_pad);
			//			}
			mIconWidth = IconUtilities.getIconSize(ShellAdmin.sShellManager.getActivity());
		}
	}

	public void setFolderAddIcon(boolean isfolderAddIcon) {
		this.mIsAddIcon = isfolderAddIcon;
	}
	@Override
	protected void dispatchDraw(GLCanvas canvas) {

		if (mGlBitmap != null) {
			mGlBitmap.draw(canvas);
		}
//		super.dispatchDraw(canvas);
		// 绘制功能表发光动画
//		if (mDrawLight) {
//			int old = canvas.save();
//			long delta = AnimationUtils.currentAnimationTimeMillis() - mDrawTime;
//			if (delta >= ANIMATION_DURATION_1000) {
//				delta = (long) ANIMATION_DURATION_1000;
//				mDrawTime = AnimationUtils.currentAnimationTimeMillis();
//			}
//			float out = mAlphaInterpolator.getInterpolation(delta / ANIMATION_DURATION_1000);
//			int alpha = (int) (ALPHA_MAX * out);
//			canvas.multiplyAlpha(alpha);
//			canvas.drawDrawable(mLightDrawable);
//			canvas.restoreToCount(old);
//			invalidate();
//		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(resolveSize(mIconWidth, widthMeasureSpec),
				resolveSize(mIconWidth, heightMeasureSpec));
		if (mGlBitmap != null) {
			setDrawableBounds(mGlBitmap);
		}
//		if (mCoverDrawable != null) {
//			setDrawableBounds(mCoverDrawable);
//		}
		
	}

	public void setModelItem(final int modelType) {
		mType = modelType;
	}

	/**
	 * 设置模型的纹理
	 * 
	 * @param BitmapGLDrawable
	 *            纹理的图片
	 */
	public void setTexture(BitmapGLDrawable drawable) {
		if (drawable == null || drawable.getBitmap() == null || drawable.getBitmap().isRecycled()) {
			return;
		}
		mGlBitmap = drawable;
		//		mGlBitmap.setBounds(0, 0, mIconWidth, mIconWidth);
		setDrawableBounds(mGlBitmap);
	}

	/**
	 * 设置模型的纹理
	 * 
	 * @param BitmapDrawable
	 *            纹理的图片
	 */
	public void setTexture(BitmapDrawable drawable) {
		if (drawable == null || drawable.getBitmap() == null || drawable.getBitmap().isRecycled()) {
			return;
		}
		mGlBitmap = new BitmapGLDrawable(drawable);
		//		mGlBitmap.setBounds(0, 0, mIconWidth, mIconWidth);
		setDrawableBounds(mGlBitmap);
	}

	/**
	 * 设置模型的纹理
	 * 
	 * @param Bitmap
	 *            纹理的图片
	 */
	public void setTexture(Bitmap bitmap) {
		if (bitmap == null || bitmap.isRecycled()) {
			return;
		}
		mGlBitmap = new BitmapGLDrawable(new BitmapDrawable(bitmap));
		//		mGlBitmap.setBounds(0, 0, mIconWidth, mIconWidth);
		setDrawableBounds(mGlBitmap);
	}

	/**
	 * <br>
	 * 功能简述: 图片阴影 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param src
	 * @return TODO 修正checkstyle的错误
	 */
	//	private Bitmap processingBitmap_Blur(Bitmap src) { // CHECKSTYLE IGNORE
	//		int width = src.getWidth();
	//		int height = src.getHeight();
	//
	//		BlurMaskFilter blurMaskFilter;
	//		Paint paintBlur = new Paint();
	//
	//		Bitmap dest = Bitmap.createBitmap(width, height,
	//				Bitmap.Config.ARGB_8888);
	//		Canvas canvas = new Canvas(dest);
	//
	//		// Create background in green
	//		Bitmap alpha = src.extractAlpha();
	//		paintBlur.setColor(0x33FFFFFF); // CHECKSTYLE IGNORE
	//		canvas.drawBitmap(alpha, 0, 0, paintBlur);
	//
	//		// Create outer blur, in Red
	//		blurMaskFilter = new BlurMaskFilter(2, BlurMaskFilter.Blur.OUTER);
	//		paintBlur.setMaskFilter(blurMaskFilter);
	//		canvas.drawBitmap(alpha, 0, 0, paintBlur);
	//
	//		if (alpha != null) {
	//			alpha.recycle();
	//			alpha = null;
	//		}
	//
	//		return dest;
	//	}

	public int getModelType() {
		// if (mModelItem != null) {
		// return mModelItem.getModelType();
		// }

		return mType;
	}

	public void clearTexture() {
		if (mGlBitmap != null) {
			mGlBitmap.clear();
		}
	}

	@Override
	public void cleanup() {
		clearTexture();
		mAlphaInterpolator = null;
	}

	/**
	 * 功能简述: 更改另一张纹理 功能详细描述: 注意:
	 * 
	 * @param key
	 * @param bitmap
	 */
	public void changeTexture(Bitmap bitmap) {
		// if (mModelItem != null) {
		// mModelItem.changeApplicationIcon(key, bitmap);
		// }

		if (mGlBitmap != null) {
			mGlBitmap.clear();
			mGlBitmap.setTexture(BitmapTexture.createSharedTexture(bitmap));
		} else {
			mGlBitmap = new BitmapGLDrawable(new BitmapDrawable(bitmap));
		}

		// clear 会把注册信息清理掉
		// 这里重新注册一次
		mGlBitmap.register();
		//		mGlBitmap.setBounds(0, 0, mIconWidth, mIconWidth);
		setDrawableBounds(mGlBitmap);
		invalidate();
	}

	/**
	 * 
	 * <br>
	 * 类描述:点击图标动画结束监听接口 <br>
	 * 功能详细描述:当单击图标动画完成后执行回调函数
	 * 
	 * @author yuanzhibiao
	 * @date [2012-9-6]
	 */
	public static interface ClickAnimationListener {
		void onClickAnimationEnd();
	}

	// --------------------Dock栏特殊图标发亮动画相关 ----------------------------//

	//	/**
	//	 * <br>
	//	 * 功能简述: 开始图标的发亮动画 <br>
	//	 * 功能详细描述: <br>
	//	 * 注意:
	//	 */
	//	public void startLightingAnimation(String type) {
	//		if (!mDrawLight) {
	//			if (FakeAppsManager.DOCK_ICON_MESSAGE_PATH.equals(type)) {
	//				if (mLightDrawable == null) {
	//					BitmapDrawable drawable = (BitmapDrawable) getResources()
	//							.getDrawable(R.drawable.dock_sms_part_2);
	//					mLightDrawable = new BitmapGLDrawable(drawable);
	//					mLightDrawable.setBounds(0, 0, mIconWidth, mIconWidth);
	//				}
	//			} else if (FakeAppsManager.DOCK_ICON_DIAL_PATH.equals(type)) {
	//				if (mLightDrawable == null) {
	//					BitmapDrawable drawable = (BitmapDrawable) getResources()
	//							.getDrawable(R.drawable.dock_phone_part_2);
	//					mLightDrawable = new BitmapGLDrawable(drawable);
	//					mLightDrawable.setBounds(0, 0, mIconWidth, mIconWidth);
	//				}
	//			} else {
	//				return;
	//			}
	//			if (mAlphaInterpolator == null) {
	//				mAlphaInterpolator = new AlphaInterpolator();
	//			}
	//			mDrawTime = AnimationUtils.currentAnimationTimeMillis();
	//			mDrawLight = true;
	//			invalidate();
	//		}
	//	}
	//
	//	/**
	//	 * <br>
	//	 * 功能简述: 结束图标的发亮动画 <br>
	//	 * 功能详细描述: <br>
	//	 * 注意:
	//	 */
	//	public void stopLightingAnimation() {
	//		if (mDrawLight) {
	//			mDrawLight = false;
	//			invalidate();
	//			if (mLightDrawable != null) {
	//				mLightDrawable.clear();
	//				mLightDrawable = null;
	//			}
	//			mAlphaInterpolator = null;
	//		}
	//	}
	//
	//	/**
	//	 * 
	//	 * <br>
	//	 * 类描述: Dock特殊图标发亮动画的插值器 <br>
	//	 * 功能详细描述:
	//	 * 
	//	 * @author chendongcheng
	//	 * @date [2012-10-22]
	//	 */
	//	// CHECKSTYLE IGNORE 12 LINES
	//	private class AlphaInterpolator implements Interpolator {
	//		@Override
	//		public float getInterpolation(float input) {
	//			if (input <= 0.4f) {
	//				return input / 0.4f;
	//			} else if (input <= 0.65f) {
	//				return 1 - (input - 0.4f) / 0.25f;
	//			}
	//			return 0;
	//		}
	//	}

	/**
	 * <br>
	 * 功能简述: 设置透明度 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param alpha
	 *            [0, 255]
	 */
	public void setAlpha(int alpha) {
		mGlBitmap.setAlpha(alpha);
	}

	/**
	 * <br>
	 * 功能简述: <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param srcColor
	 * @param mode
	 *            为 null 可以清除 color filter
	 */
	@Override
	public void setColorFilter(int srcColor, PorterDuff.Mode mode) {
		if (mGlBitmap != null) {
			mGlBitmap.setColorFilter(srcColor, mode);
		}
	}
	public void setAlphaFilter(int alpha) {
		if (mGlBitmap != null) {
			mGlBitmap.setAlpha(alpha);
		}
	}

	public static void recyleStaticResource() {
//		if (sCircleBitmap != null) {
//			sCircleBitmap.clear();
//			sCircleBitmap = null;
//		}
//
//		if (sFolderCircleBitmap != null) {
//			sFolderCircleBitmap.clear();
//			sFolderCircleBitmap = null;
//		}
	}

//	public void setCoverDrawable(GLDrawable drawable) {
//		if (!(drawable instanceof BitmapGLDrawable)) {
//			return;
//		}
//		BitmapGLDrawable bitmapDrawable = (BitmapGLDrawable) drawable;
//		if (mCoverDrawable != null) {
//			mCoverDrawable.clear();
//		}
//		mCoverDrawable = bitmapDrawable;
//		//		mCoverDrawable.setBounds(0, 0, mIconWidth, mIconWidth);
//		setDrawableBounds(mCoverDrawable);
//		mIsDrawCover = true;
//		//		invalidate();
//	}
//
//	public void clearCoverDrawable() {
//		if (mCoverDrawable != null) {
//			mCoverDrawable.clear();
//			mCoverDrawable = null;
//		}
//		mIsDrawCover = false;
//		invalidate();
//	}

	public int getIconWidth() {
		if (mIconWidth == 0) {
			mIconWidth = IconUtilities.getIconSize(ShellAdmin.sShellManager.getActivity());
		}
		return mIconWidth;
	}

	public void setIconWidth(int iconWidth) {
		mIconWidth = iconWidth;
//		if (mCoverDrawable != null) {
//			mCoverDrawable.setBounds(0, 0, mIconWidth, mIconWidth);
//		}
		if (mGlBitmap != null) {
			mGlBitmap.setBounds(0, 0, mIconWidth, mIconWidth);
		}
		requestLayout();
	}

	public Bitmap getTexture() {
		Bitmap bitmap = null;
		if (mGlBitmap != null) {
			bitmap = mGlBitmap.getBitmap();
		}
		return bitmap;
	}

	protected void setDrawableBounds(GLDrawable drawable) {
		if ((mMeasuredWidth == 0 || mMeasuredHeight == 0) || mIconWidth <= mMeasuredWidth
				&& mIconWidth <= mMeasuredHeight) {
			drawable.setBounds(0, 0, mIconWidth, mIconWidth);
		} else {
			int offsetW = 0;
			int offsetH = 0;
			if (mIconWidth > mMeasuredWidth) {
				offsetW = (mIconWidth - mMeasuredWidth) / 2;
			}
			if (mIconWidth > mMeasuredHeight) {
				offsetH = (mIconWidth - mMeasuredHeight) / 2;
			}

			drawable.setBounds(-offsetW, -offsetH, mIconWidth - offsetW, mIconWidth - offsetH);
		}
	}
}
