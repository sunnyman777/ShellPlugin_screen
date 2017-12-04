package com.jiubang.shell.scroller.effector.subscreen;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.FloatMath;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.InterpolatorValueAnimation;
import com.go.gl.graphics.BitmapGLDrawable;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.Texture;
import com.go.gl.graphics.geometry.GLGrid;
import com.go.gl.graphics.geometry.TextureGLObjectRender;
import com.go.gl.view.GLView;
import com.go.util.graphics.DrawUtils;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.screen.GLSuperWorkspace;
import com.jiubang.shell.scroller.ShellScreenScroller;

/**
 * 布料特效的绘制类
 * 
 * @author panguowei
 * 
 */
public class ClothEffector extends MSubScreenEffector {

	private Rect mWorkspaceRect; // workspace实际图标区域的大小(去掉dock，和上方指示器)
	TwistMesh mTwistMesh; // 继承网格变化的类，用于生成每个网格的坐标位置
	float mAngleRatio;
	// 分别绘制AB屏和背景的render
	TextureGLObjectRender mRenderScreenA ;
	TextureGLObjectRender mRenderScreenB;
	TextureGLObjectRender mRenderBg;
	
	private final static int ANIMATION_MAX_ALPHA = 255;
	/** 当前背景alpha */
	private int mBgAlpha = ANIMATION_MAX_ALPHA;
	
	private static final int FLIP_DEPTH = -DrawUtils.dip2px(100); // 旋转时的深度切换，旋转到90度的时候，深度为600
	/** 分割行列数的ＤＰ单位 */
	private static final int DIV_PADDING = 16;
	
	/** 背景bitmap */
	private Bitmap mBackgroundBitmap;
	
	private InterpolatorValueAnimation mAlphaAnimation = new InterpolatorValueAnimation(
			0);
	
	private static final int IN_OUT_ALPHA_DURATION = 300; // 动画时间
	
	public ClothEffector(Rect rect) {
		init(rect);
	}

	/**
	 * 
	 * 初始化信息
	 **/
	private void init(Rect rect) {
		cleanup();
		mRenderScreenA = new TextureGLObjectRender();
		mRenderScreenB = new TextureGLObjectRender();
		mRenderBg = new TextureGLObjectRender();
		mWorkspaceRect = rect;
		int divX = Math.max(1, mWorkspaceRect.width() / DrawUtils.dip2px(DIV_PADDING));
		int divY = Math.max(1, mWorkspaceRect.height() / DrawUtils.dip2px(DIV_PADDING));
		mTwistMesh = new TwistMesh(divX, divY, true);
		mTwistMesh.setBounds(mWorkspaceRect.left, mWorkspaceRect.top, mWorkspaceRect.right,
				mWorkspaceRect.bottom);
		mAngleRatio = 180.0f / mWorkspaceRect.width();
		initBackgroundRender();
	}
	
	/**
	 * 初始化背景图
	 */
	private void initBackgroundRender() {
		if (mWorkspaceRect.width() < 1 || mWorkspaceRect.height() < 1) {
			return;
		}
		if (mBackgroundBitmap != null) {
			mBackgroundBitmap.recycle();
		}
//		if (mCovertBitmap != null) {
//			mCovertBitmap.recycle();
//		}
		mBackgroundBitmap = getBackgroundBitmap();
//		mCovertBitmap = getBitmapFromNinePatch(R.drawable.effect_cloth_cover);
		mRenderBg.setTexture(mBackgroundBitmap);
//		mRenderCover.setTexture(mCovertBitmap);
	}
	
	private Bitmap getBackgroundBitmap() {
		// 初始化背景图，由于这是一张九切图，所以把它设置在一个view上再对这个view做绘图缓冲再设置到纹理
		View view = new View(ShellAdmin.sShellManager.getContext());
		view.setBackgroundDrawable(ShellAdmin.sShellManager.getContext().getResources().getDrawable(R.drawable.gl_smart_bg));
		view.layout(mWorkspaceRect.left, mWorkspaceRect.top, mWorkspaceRect.right, mWorkspaceRect.bottom);
		view.setDrawingCacheEnabled(true);
		return view.getDrawingCache();
	}
	
	@Override
	protected void drawView(GLCanvas canvas, int screen, int offset,
			boolean first) {
		if (first) {
			drawScreen(canvas, mContainer, mScroller);
		}
	}

	public void drawScreen(GLCanvas canvas, SubScreenContainer container,
			ShellScreenScroller scroller) {
		if (mWorkspaceRect.width() < 1 || mWorkspaceRect.height() < 1) {
			return;
		}
		int offset = scroller.getCurrentScreenOffset();
		if (offset > 0) {
			offset -= /*mWorkspaceRect.width()*/mScroller.getScreenSize();
		}
		offset = Math.abs(offset);
		
		boolean animationing = mAlphaAnimation.animate();
		mBgAlpha = (int) mAlphaAnimation.getValue();
		if (animationing) {
			container.invalidateScreen();
		} else if (mIsOnScrollEndAlphaing) {
			mIsOnScrollEndAlphaing = false;
			container.invalidateScreen();
		}
		int alpha = (int) (scroller.getCurrentDepth() * ANIMATION_MAX_ALPHA);
		if (mBgAlpha < alpha) {
			mBgAlpha = alpha;
		}
		
		final float currentDepth = scroller.getCurrentDepth() * FLIP_DEPTH;
		final int save = canvas.save();
		canvas.translate(scroller.getScroll(), 0); // 平移到当前屏
		
		canvas.translate(0, 0, currentDepth);
		
		int screenA = scroller.getDrawingScreenA();
		int screenB = scroller.getDrawingScreenB();
		if (mContainer instanceof GLSuperWorkspace) {
			if (((GLSuperWorkspace) mContainer).isShowingZero()) {
				screenA = screenA - 1;
				screenB = screenB - 1;
			}
		}
		
		
		// 判断绘制的先后顺序
		if (scroller.getCurrentScreen() == screenB) {
			//绘制A屏
			drawByRender(container.getScreenView(screenA), offset, canvas, mRenderScreenA); // 绘制A屏
			// 旋转180准备绘制B屏
			canvas.translate(mWorkspaceRect.width() / 2, 0);
			canvas.rotateAxisAngle(180, 0, 1, 0);
			canvas.translate(-mWorkspaceRect.width() / 2, 0);
			drawByRender(container.getScreenView(screenB), offset, canvas, mRenderScreenB); // 绘制B屏
			canvas.restoreToCount(save);
		} else {
			// 旋转180准备绘制B屏
			final int save2 = canvas.save();
			canvas.translate(mWorkspaceRect.width() / 2, 0);
			canvas.rotateAxisAngle(180, 0, 1, 0);
			canvas.translate(-mWorkspaceRect.width() / 2, 0);
			drawByRender(container.getScreenView(screenB), offset, canvas, mRenderScreenB); // 绘制B屏
			canvas.restoreToCount(save2);

			//绘制A屏
			drawByRender(container.getScreenView(screenA), offset, canvas, mRenderScreenA); // 绘制A屏
			canvas.restoreToCount(save);
		}
	}
	
	/**
	 * 绘制单屏
	 **/
	public void drawByRender(GLView child, int offset, GLCanvas canvas,
			TextureGLObjectRender render) {
		if (child == null || canvas == null) {
			return;
		}
		if (mContainer instanceof GLSuperWorkspace) {
			((GLSuperWorkspace) mContainer).setEnableCellLayoutDrawingCache(true);
			if (!child.isDrawingCacheEnabled()) {
				child.setDrawingCacheEnabled(true);
			}
		}
		if (!child.isDrawingCacheEnabled()) {
			child.setDrawingCacheEnabled(true);
		}
		
		int oldAlpha = canvas.getAlpha();
		if (mBgAlpha != oldAlpha) {
			canvas.setAlpha(mBgAlpha);
		}
		
		final float angleAbs = offset * mAngleRatio;
		float newRotatePercentage = angleAbs / 180;
		mTwistMesh.update(newRotatePercentage);
		mTwistMesh.setTexcoords(0, 0, 1, 1);
		mRenderBg.draw(canvas, mTwistMesh);
		
		if (mBgAlpha != oldAlpha) {
			canvas.setAlpha(oldAlpha);
		}
		child.setAlpha(ANIMATION_MAX_ALPHA);
		child.setDrawingCacheDepthBuffer(true);
		BitmapGLDrawable drawingCache = child.getDrawingCache(canvas);
		child.setDrawingCacheDepthBuffer(false);
		if (drawingCache == null) {
			return;
		}
		Texture oldTexture = render.mTexture;
		Texture texture = drawingCache.getTexture();

		if (oldTexture != texture) {
			if (oldTexture != null) {
				oldTexture.duplicate();
			}
			render.setTexture(texture);
		}
		mTwistMesh.setTexcoords(0,
				1.0f - (float) mWorkspaceRect.top / child.getHeight(),
				(float) mWorkspaceRect.right / child.getWidth(), 1.0f
						- (float) mWorkspaceRect.bottom / child.getHeight());
		render.draw(canvas, mTwistMesh);
		
//		if (mBgAlpha != oldAlpha) {
//			canvas.setAlpha(mBgAlpha);
//		}
//		mTwistMesh.setTexcoords(0, 0, 1, 1);
//		mRenderCover.draw(canvas, mTwistMesh);
//		if (mBgAlpha != oldAlpha) {
//			canvas.setAlpha(oldAlpha);
//		}
	}

	@Override
	public boolean isAnimationing() {
		return false;
	}

	@Override
	public void onThemeSwitch() {
		initBackgroundRender();
	}

	@Override
	public void cleanup() {
		if (mRenderScreenA != null) {
			mRenderScreenA.clear();
			mRenderScreenA = null;
		}

		if (mRenderScreenB != null) {
			mRenderScreenB.clear();
			mRenderScreenB = null;
		}

		if (mRenderBg != null) {
			if (mRenderBg.mTexture != null) {
				mRenderBg.mTexture.clear();
				mRenderBg.mTexture = null;
			}
			mRenderBg.clear();
			mRenderBg = null;
		}
		
//		if (mRenderCover != null) {
//			mRenderCover.clear();
//			mRenderCover = null;
//		}
		
		if (mTwistMesh != null) {
			mTwistMesh = null;
		}
		
		if (mBackgroundBitmap != null) {
			mBackgroundBitmap.recycle();
			mBackgroundBitmap = null;
		}
		
//		if (mCovertBitmap != null) {
//			mCovertBitmap.recycle();
//			mCovertBitmap = null;
//		}
		
		mWorkspaceRect = null;
	}

	/**
	 * 计算布料特效绘制坐标的网格类
	 * @author panguowei
	 *
	 */
	class TwistMesh extends GLGrid {

		float[] mPositionArray2;
		Interpolator mInterpolator;

		public TwistMesh(int xDiv, int yDiv, boolean fill) {
			super(xDiv, yDiv, fill);
			mPositionArray2 = new float[mPositionArray.length];
			mInterpolator = new OvershootInterpolator(3);
		}
		
		@Override
		protected void onBoundsChange(float left, float top, float right, float bottom) {
			super.onBoundsChange(left, top, right, bottom);
			System.arraycopy(mPositionArray, 0, mPositionArray2, 0,
					mPositionArray.length);
		}

		/**
		 * 使用旋转比例去更新角度
		 * 
		 * @param percentage
		 */
		public void update(float percentage) {
			final int divY = getDivY();
			final float minRad = 0; // 最小转过的角度
			final float maxRad = (float) Math.PI; // 最大转过的角度
			final float maxDeltaRad = (float) Math.PI / 1.5f; // 最大的角度旋转差
			float rad = percentage * (float) (Math.PI + maxDeltaRad);
			final float deltaRad = (float) (maxDeltaRad / divY);
			for (int i = 0; i <= divY; ++i) {
				setRotate(i, -Math.min(rad, maxRad));
				rad = Math.max(minRad, rad - deltaRad);
			}
		}

		/**
		 * 设置第i行每个网格的角度
		 * 
		 * @param i
		 * @param rad
		 */
		private void setRotate(int i, float rad) {
			RectF rect = getBounds();
			final float centerX = (rect.left + rect.right) * 0.5f;
			final int divX = getDivX();
			final float[] pos1 = mPositionArray;
			final float[] pos2 = mPositionArray2;
			final float sin = FloatMath.sin(rad);
			final float cos = FloatMath.cos(rad);

			int index = getPositionArrayStride() * i;
			for (int j = 0; j <= divX; ++j) {
				float x = pos2[index] - centerX;
				float z = pos2[index + 2];
				pos1[index] = cos * x + sin * z + centerX;
				pos1[index + 2] = -sin * x + cos * z;
				index += 3;
			}
		}
	}
	
	@Override
	public void onAttach(SubScreenContainer container, ShellScreenScroller scroller) {
		super.onAttach(container, scroller);
		scroller.setDepthEnabled(true);
	}
	
	@Override
	public void onDetach() {
		mScroller.setDepthEnabled(false);
		super.onDetach();
		cleanup();
	}

	@Override
	public boolean isNeedEnableNextWidgetDrawingCache() {
		return false;
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset,
			boolean first) {
		return false;
	}
	
	@Override
	public void notifyRegetScreenRect() {
		if (mContainer != null) {
			Rect rect = mContainer.getScreenRect();
			init(rect);
		}
	}
	
	boolean mIsSCrollStarted = false;
	boolean mIsOnScrollEndAlphaing = false;
	
	@Override
	public void onScrollStart() {
		if (!mIsSCrollStarted) {
			startInOutAnimation(true);
			mIsSCrollStarted = true;
		}
		mIsOnScrollEndAlphaing = false;
	}

	@Override
	public void onScrollEnd() {
		if (mContainer instanceof GLSuperWorkspace) {
			GLSuperWorkspace superWorkspace = (GLSuperWorkspace) mContainer;
			superWorkspace.setEnableCellLayoutDrawingCache(false);
			superWorkspace.setEnableWidgetDrawingCache(false);
			superWorkspace.invalidate();
		}
	}

	@Override
	public void onScrollTouchUp() {
		mIsOnScrollEndAlphaing = true;
		mIsSCrollStarted = false;
		startInOutAnimation(false);
	}
	
	public void startInOutAnimation(boolean in) {
		float start;
		if (mAlphaAnimation.animate()) {
			start = mAlphaAnimation.getValue();
		} else {
			start = in ? 0 : ANIMATION_MAX_ALPHA;
		}
		float end = in ? ANIMATION_MAX_ALPHA : 0;
		mAlphaAnimation.start(start, end, IN_OUT_ALPHA_DURATION);
		mAlphaAnimation.animate();
	}
}
