package com.jiubang.shell.scroller.effector.subscreen;

import android.content.res.Resources;
import android.graphics.Rect;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.InterpolatorValueAnimation;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.scroller.ScreenScroller;
import com.go.util.graphics.DrawUtils;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.scroller.ShellScreenScroller;
import com.jiubang.shell.utils.MathUtils;

/**
 * 水晶特效
 * 
 * @author panguowei
 * 
 */
public class CrystalEffector extends MSubScreenEffector {

	private static final int FLIP_DEPTH = -DrawUtils.dip2px(300); // 旋转时的深度切换，旋转到90度的时候，深度为400
	private static final int DEPTH_ALL = DrawUtils.dip2px(30); // 整体深度
	private static final int DEPTH_BACK =  -DEPTH_ALL; // 后面板深度，在图标后面，为整体
																	// - 前面板
	private static final float DEPTH_ROTATE = DEPTH_BACK  / 2.0f; // 旋转轴心的z值，用于翻转动画时作旋转中心

	private static final int IN_OUT_ALPHA_DURATION = 300; // 动画时间

	private GLDrawable mFaceDrawable; // 前后面板
	private GLDrawable mTbDrawable; // 上下面板
	private GLDrawable mSideDrawable; // 侧边面板

	private int mWidth; // 宽高，上边距
	private int mHeight;
	private int mTop;

	private ShellScreenScroller mCurrentScroller; // 当前滚动器
	
	// 侧面板属性
	private float mSideOffset; // 左右 offset
	private float mSideStep; // 左右步进step
	private float mSideTopOffset; //上边距差
	
	// 正面板属性
	private float mFontOffset;
	private float mFontStep;
	private float mFontTopOffset;
	
	// 上下面板参数
	private float mTbOffset;
	private float mTbStep;
	private float mTbTopOffset;
	
	
	private boolean mEnableVerticalSlide = false;
	
	private float mRotatePercentage = 0;
	
	/**
	 * 动画透明度最大值
	 */
	public static final int ANIMATION_MAX_ALPHA = 255; 
	/**
	 * 是否已经执行了进入动画
	 */
	private boolean mHasDoInAnimate = false; //解决ADT-15010国内包：3D零屏，3D特效水晶切出零屏第一屏到第二屏是翻转特效
	
	private InterpolatorValueAnimation mAlphaAnimation = new InterpolatorValueAnimation(
			0);

	// 一些布局时计算好的参数，方便后面调用
	public CrystalEffector(Rect rect) {
		init(rect);
	}
	
	private void init(Rect rect) {
		mWidth = rect.width();
		mHeight = rect.height();
		mTop = rect.top;
		Resources res = ShellAdmin.sShellManager.getContext().getResources();
		if (mFaceDrawable == null) {
			mFaceDrawable = GLDrawable.getDrawable(res, R.drawable.gl_crystal_font);
		}
		if (mTbDrawable == null) {
			mTbDrawable = GLDrawable.getDrawable(res, R.drawable.gl_crystal_side);
		}
		if (mSideDrawable == null) {
			mSideDrawable = GLDrawable.getDrawable(res, R.drawable.gl_crystal_tb);
		}
		if (mFaceDrawable != null) {
			float height =  MathUtils.getDistance(0, 0, mWidth,
					mHeight);
			mFontOffset = height / 2;
			mFaceDrawable.setBounds(0, 0, mWidth * 2, (int) height);
			mFontStep =  mFaceDrawable.getBounds().width() * 1.5f
					- mWidth / 2 - mFontOffset;
			mFontTopOffset = height / 2	- mHeight / 2;
		}

		if (mSideDrawable != null) {
			float height = MathUtils.getDistance(0, 0, DEPTH_ALL,
					mHeight);
			mSideOffset = height / 2;
			mSideDrawable.setBounds(0, 0, mSideDrawable.getIntrinsicWidth(),
					(int) height);
			mSideStep = mSideDrawable.getBounds().width() * 1.5f
					- DEPTH_ALL / 2 - mSideOffset;
			mSideTopOffset = mSideDrawable.getBounds().height() / 2
					- mHeight / 2;
		}

		if (mTbDrawable != null) {
			float height = MathUtils.getDistance(0, 0, DEPTH_ALL,
					mWidth);
			mTbOffset = height / 2;
			mTbDrawable.setBounds(0, 0, mTbDrawable.getIntrinsicWidth(),
					(int) height);
			mTbStep = mTbDrawable.getBounds().width() * 1.5f
					- DEPTH_ALL / 2 - mTbOffset;
			mTbTopOffset = mTbDrawable.getBounds().height() / 2
					- mWidth / 2;
		}
	}
	
	@Override
	protected void drawView(GLCanvas canvas, int screen, int offset,
			boolean first) {
		if (first && mContainer != null) {
			if (!mHasDoInAnimate) {
				onScrollStart();
			}
			drawScreen(canvas, mContainer, mScroller);
		}
	}

	/**
	 * 绘制screen
	 * 
	 * @param canvas
	 * @param view
	 * @param scroller
	 */
	public void drawScreen(GLCanvas canvas, SubScreenContainer container,
			ShellScreenScroller scroller) {
		mCurrentScroller = scroller;
		boolean cutFaceEnable = canvas.isCullFaceEnabled();
		if (cutFaceEnable) {
			canvas.setCullFaceEnabled(false);
		}
		int save = canvas.save();
		canvas.translate(mCurrentScroller.getScroll(), 0); // 平移到当前屏

		float offset = mCurrentScroller.getCurrentScreenOffset();
		// 确定当前的深度axisZ和旋转角度angleY，并写入canvas
//		final float axisZ = Math.abs(offset) / mWorkspaceWidth * FLIP_DEPTH;
		float axisZ = mCurrentScroller.getCurrentDepth() * FLIP_DEPTH;
		canvas.translate(mWidth / 2, 0, axisZ + DEPTH_ROTATE);

		int originAlpha = canvas.getAlpha();
		int animateAlpha = originAlpha;
		verticalSlide(canvas);

		int save2 = canvas.save();
		boolean animationing = mAlphaAnimation.animate();
		animateAlpha = (int) mAlphaAnimation.getValue();
		canvas.setAlpha(animateAlpha);
		if (animationing) {
			container.invalidateScreen();
		} else if (mIsOnScrollEndAlphaing) {
			mIsOnScrollEndAlphaing = false;
			mIsScrollReallyFinished = true;
			mHasDoInAnimate = false;
			container.invalidateScreen();
		}
		float newRotatePercentage = (float) (scroller.getScroll() % /*mWorkspaceWidth*/scroller.getScreenSize())
				/ /*mWorkspaceWidth*/scroller.getScreenSize();
		if (newRotatePercentage != 0 && newRotatePercentage != 1.0) {
			mRotatePercentage = newRotatePercentage;
		}
		final int panelRotate = (int) (-mRotatePercentage * 180);
		canvas.rotateAxisAngle(panelRotate, 0, 1, 0);
		canvas.translate(-mWidth / 2, 0, -DEPTH_ROTATE);
		
		
		// 绘制所有在底部的面板
		drawBasePanel(canvas);

		if (animateAlpha != originAlpha) {
			canvas.setAlpha(originAlpha);
		}
		canvas.restoreToCount(save2);

		save2 = canvas.save();
		final float angleY = offset / /*mWorkspaceWidth*/scroller.getScreenSize() * 180;
		canvas.rotateAxisAngle(angleY, 0, 1, 0);
		canvas.translate(-mWidth / 2, 0, -DEPTH_ROTATE);
		// 屏幕画上
		int currentScreen = mCurrentScroller.getCurrentScreen();
		if (currentScreen != ScreenScroller.INVALID_SCREEN) {
			canvas.setCullFaceEnabled(cutFaceEnable);
			container.drawScreen(canvas, currentScreen);
			canvas.setCullFaceEnabled(false);
		}
		canvas.restoreToCount(save2);

		if (animateAlpha != originAlpha) {
			canvas.setAlpha(animateAlpha);
		}
		// 绘制覆盖在图标上的面板
		canvas.rotateAxisAngle(panelRotate, 0, 1, 0);
		canvas.translate(-mWidth / 2, 0, -DEPTH_ROTATE);
		drawCoverPanel(canvas);

		if (originAlpha != animateAlpha) {
			canvas.setAlpha(originAlpha);
		}

		canvas.restoreToCount(save);
		if (cutFaceEnable) {
			canvas.setCullFaceEnabled(cutFaceEnable);
		}
	}
	
	private void verticalSlide(GLCanvas canvas) {
		if (mEnableVerticalSlide) {
			final float yOffsetRatio = 2.1f;
			final int four = 4;
			float t = Math.min((1.0f / mWidth) * Math.abs(mCurrentScroller.getCurrentScreenOffset()) * 2, 1);
			final float absT = Math.abs(t);
			//初始阶段影响角度值是x轴的偏移量，t2会快速到达1，以后影响上下角度值就取决于y轴偏移量
			float t2 = Math.min(Math.max(absT * four, mCurrentScroller.getCurrentDepth()), 1);
			//滑动系数，越大越灵敏
			float yOffset = mCurrentScroller.getTouchDeltaY() / (float) mCurrentScroller.getScreenHeight()
					* 1.0f;
			float rotateX = Math.max(-1, Math.min(yOffset * yOffsetRatio, 1)) * 45;
			float angleX = 0;
			angleX = interpolate(angleX, rotateX, t2);
			canvas.translate(0, mHeight / 2);
			canvas.rotateAxisAngle(angleX * t2, 1, 0, 0);
			canvas.translate(0, -mHeight / 2);

		}
	}
	
	protected static float interpolate(float start, float end, float t) {
		return (end - start) * t + start;
	}
	
	public void enableVerticalSlide(boolean allowVerticalSlide) {
		mEnableVerticalSlide = allowVerticalSlide;
	}

	/***
	 * 绘制图标下层的东西，图标会在这些东西上面 包括：后面板，上下面板, 左右面板
	 * 
	 * @param canvas
	 * @param drawLeftFirst
	 */
	private void drawBasePanel(GLCanvas canvas) {
		if (mFaceDrawable == null || mSideDrawable == null
				|| mTbDrawable == null) {
			return;
		}

		canvas.translate(0, mTop);

		drawLeftSide(canvas);
		drawRightSide(canvas);

		drawTopSide(canvas);
		drawBottomSide(canvas);

		canvas.translate(0, -mTop);

	}

	/**
	 * 绘制上面板
	 * 
	 * @param canvas
	 */
	private void drawTopSide(GLCanvas canvas) {
		int save = canvas.save();

//		final int topOffset = mTbDrawable.getBounds().height() / 2
//				- mWorkspaceWidth / 2;
//		final float xOffset = mTbDrawable.getBounds().width() * 1.5f
//				- DEPTH_ALL / 2;
//		// 加入滚动其的offset值，算法是计算滚动器滚动的百分比,乘以完整的offset，然后用当前的offset减去之，从而保证范围在[0,mFaceRrawable.width
//		// - mWorkspaceWidth]之间
		
		
//		int drawOffset = (int) (mTbDrawable.getBounds().width() / 2 - mRotatePercentage
//				* (xOffset - mTbOffset));

		// 平移、旋转到上面板
		canvas.translate(mWidth, 0, DEPTH_BACK);
		canvas.rotate(90);
		canvas.rotateAxisAngle(-90, 0, 1, 0);
		canvas.clipRect(0, 0, DEPTH_ALL, mWidth);


		// 在面板中心点旋转画布45度
		canvas.translate(DEPTH_ALL / 2, mTbTopOffset, 0);
		canvas.rotate(-45, DEPTH_ALL / 2, mTbDrawable.getBounds().height() / 2);
		// canvas.translate(-drawOffset, topOffset , 0);

		// 三连画
//		canvas.translate(-drawOffset - mTbDrawable.getBounds().width(),
//				topOffset, 0);
//		mTbDrawable.draw(canvas);
//		canvas.translate(mTbDrawable.getBounds().width(), 0, 0);
//		mTbDrawable.draw(canvas);
//		canvas.translate(mTbDrawable.getBounds().width(), 0);
//		mTbDrawable.draw(canvas);
//		canvas.restoreToCount(save);
		float offsetX = -mTbDrawable.getBounds().width() * 1.5f + mRotatePercentage	* mTbStep;
		canvas.translate(offsetX, 0, 0);
		if (offsetX > -mTbDrawable.getBounds().width() - mTbOffset
				&& offsetX < mTbOffset) {
			mTbDrawable.draw(canvas);
		}
		offsetX += mTbDrawable.getBounds().width();
		canvas.translate(mTbDrawable.getBounds().width(), 0, 0);
		if (offsetX > -mTbDrawable.getBounds().width() - mTbOffset
				&& offsetX < mTbOffset) {
			mTbDrawable.draw(canvas);
		}
		offsetX += mTbDrawable.getBounds().width();
		canvas.translate(mTbDrawable.getBounds().width(), 0);

		if (offsetX > -mTbDrawable.getBounds().width() - mTbOffset
				&& offsetX < mTbOffset) {
			mTbDrawable.draw(canvas);
		}
		canvas.restoreToCount(save);
	}

	/**
	 * 绘制下面板
	 * 
	 * @param canvas
	 */
	private void drawBottomSide(GLCanvas canvas) {
		int save = canvas.save();

//		final int topOffset = mTbDrawable.getBounds().height() / 2
//				- mWorkspaceWidth / 2;
//		final float xOffset = mTbDrawable.getBounds().width() * 1.5f
//				- DEPTH_ALL / 2;


		// 平移、旋转到下面板
		canvas.translate(mWidth, -mHeight, 0);
		canvas.rotate(90);
		canvas.rotateAxisAngle(90, 0, 1, 0);
		canvas.clipRect(0, 0, DEPTH_ALL, mWidth);


		// 在面板中心点旋转画布45度
		canvas.translate(DEPTH_ALL / 2, mTbTopOffset, 0);
		canvas.rotate(-45, DEPTH_ALL / 2, mTbDrawable.getBounds().height() / 2);
		// canvas.translate(-drawOffset, topOffset , 0);

//		canvas.translate(-drawOffset - mTbDrawable.getBounds().width(),
//				topOffset, 0);
//		mTbDrawable.draw(canvas);
//		canvas.translate(mTbDrawable.getBounds().width(), 0, 0);
//		mTbDrawable.draw(canvas);
//		canvas.translate(mTbDrawable.getBounds().width(), 0);
//		mTbDrawable.draw(canvas);
		// 三连画
		float offsetX = -mTbDrawable.getBounds().width() * 1.5f + mRotatePercentage	* mTbStep;
		canvas.translate(offsetX, 0, 0);
		if (offsetX > -mTbDrawable.getBounds().width() - mTbOffset
				&& offsetX < mTbOffset) {
			mTbDrawable.draw(canvas);
		}
		offsetX += mTbDrawable.getBounds().width();
		canvas.translate(mTbDrawable.getBounds().width(), 0, 0);
		if (offsetX > -mTbDrawable.getBounds().width() - mTbOffset
				&& offsetX < mTbOffset) {
			mTbDrawable.draw(canvas);
		}
		offsetX += mTbDrawable.getBounds().width();
		canvas.translate(mTbDrawable.getBounds().width(), 0);

		if (offsetX > -mTbDrawable.getBounds().width() - mTbOffset
				&& offsetX < mTbOffset) {
			mTbDrawable.draw(canvas);
		}
		canvas.restoreToCount(save);
	}

	/**
	 * 绘制左面板
	 * 
	 * @param canvas
	 */
	private void drawLeftSide(GLCanvas canvas) {
		int save = canvas.save();

		// 平移、旋转到左面板
		canvas.translate(0, 0, DEPTH_BACK);
		canvas.rotateAxisAngle(-90, 0, 1, 0);
		canvas.clipRect(0, 0, DEPTH_ALL, mHeight); //裁剪																																																																				

		// 在面板中心点旋转画布45度
		canvas.translate(DEPTH_ALL / 2, mSideTopOffset, 0);
		canvas.rotate(-45, 0, mSideDrawable.getBounds().height() / 2);

		// 跳到绘制的起始点
		// 判断范围，然后三连画
		float offsetX = -mSideDrawable.getBounds().width() * 1.5f + mRotatePercentage * mSideStep;
		canvas.translate(offsetX, 0, 0);
		if (offsetX > -mSideDrawable.getBounds().width() - mSideOffset
				&& offsetX < mSideOffset) {
			mSideDrawable.draw(canvas);
		}
		offsetX += mSideDrawable.getBounds().width();
		canvas.translate(mSideDrawable.getBounds().width(), 0, 0);
		if (offsetX > -mSideDrawable.getBounds().width() - mSideOffset
				&& offsetX < mSideOffset) {
			mSideDrawable.draw(canvas);
		}
		offsetX += mSideDrawable.getBounds().width();
		canvas.translate(mSideDrawable.getBounds().width(), 0);

		if (offsetX > -mSideDrawable.getBounds().width() - mSideOffset
				&& offsetX < mSideOffset) {
			mSideDrawable.draw(canvas);
		}
		canvas.restoreToCount(save);
	}

	/**
	 * 绘制右面板
	 * 
	 * @param canvas
	 */
	private void drawRightSide(GLCanvas canvas) {
		int save = canvas.save();
		
		// 平移、旋转到右面板
		canvas.translate(mWidth, 0, 0);
		canvas.rotateAxisAngle(90, 0, 1, 0);
		canvas.clipRect(0, 0, DEPTH_ALL, mHeight);
		
		// 在面板中心点旋转画布45度
		canvas.translate(DEPTH_ALL / 2, mSideTopOffset, 0);
		canvas.rotate(-45, 0, mSideDrawable.getBounds().height() / 2);

		// 跳到绘制的起始点
		// 判断范围，然后三连画
		float offsetX = -mSideDrawable.getBounds().width() * 1.5f + mRotatePercentage * mSideStep;
		canvas.translate(offsetX, 0, 0);
		if (offsetX > -mSideDrawable.getBounds().width() - mSideOffset
				&& offsetX < mSideOffset) {
			mSideDrawable.draw(canvas);
		}
		offsetX += mSideDrawable.getBounds().width();
		canvas.translate(mSideDrawable.getBounds().width(), 0);

		if (offsetX > -mSideDrawable.getBounds().width() - mSideOffset
				&& offsetX < mSideOffset) {
			mSideDrawable.draw(canvas);
		}

		offsetX += mSideDrawable.getBounds().width();
		canvas.translate(mSideDrawable.getBounds().width(), 0);

		if (offsetX > -mSideDrawable.getBounds().width() - mSideOffset
				&& offsetX < mSideOffset) {
			mSideDrawable.draw(canvas);
		}
		
		canvas.restoreToCount(save);
	}

	/**
	 * 绘制前面板
	 * */
	private void drawFontFace(GLCanvas canvas) {
		/*int save = canvas.save();
		final int topOffset = mFaceDrawable.getBounds().height() / 2
				- mWorkspaceHeight / 2;

		float xOffset = mFaceDrawable.getBounds().width() * 1.5f
				- mWorkspaceWidth / 2;
		int drawOffset = (int) (mFaceDrawable.getBounds().height() / 2 - mRotatePercentage
				* xOffset);
		if (mRotatePercentage > 0.5f) {
			canvas.translate(mWorkspaceWidth / 2, 0, DEPTH_ROTATE);
			canvas.rotateAxisAngle(180, 0, 1, 0);
			canvas.translate(-mWorkspaceWidth / 2, 0, -DEPTH_ROTATE);
			drawOffset += xOffset;
		}
		canvas.clipRect(0, 0, mWorkspaceWidth, mWorkspaceHeight);
		canvas.rotate(-45, mWorkspaceWidth / 2, mWorkspaceHeight / 2);

		// 三连画
		canvas.translate(-drawOffset - mFaceDrawable.getBounds().width(),
				topOffset, DEPTH_FRONT);
		mFaceDrawable.draw(canvas);
		canvas.translate(mFaceDrawable.getBounds().width(), 0, 0);
		mFaceDrawable.draw(canvas);
		canvas.translate(mFaceDrawable.getBounds().width(), 0);
		mFaceDrawable.draw(canvas);

		canvas.restoreToCount(save);*/
		float offsetX = -mFaceDrawable.getBounds().width() * 1.5f + mRotatePercentage * mFontStep;
		int save = canvas.save();
		if (mRotatePercentage > 0.5f) {
			canvas.translate(mWidth / 2, 0, DEPTH_ROTATE);
			canvas.rotateAxisAngle(180, 0, 1, 0);
			canvas.translate(-mWidth / 2, 0, -DEPTH_ROTATE);
			offsetX -= mFontStep;
		}
		canvas.clipRect(0, 0, mWidth, mHeight);
		
		// 在面板中心点旋转画布45度
		canvas.translate(mWidth / 2, mFontTopOffset, 0);
		canvas.rotate(-45, 0,  mFaceDrawable.getBounds().height() / 2);
		
		// 判断范围，然后三连画
		canvas.translate(offsetX, 0, 0);
		if (offsetX > -mFaceDrawable.getBounds().width() - mFontOffset
				&& offsetX < mFontOffset) {
			mFaceDrawable.draw(canvas);
		}
//		mFaceDrawable.draw(canvas);
		offsetX += mFaceDrawable.getBounds().width();
		canvas.translate(mFaceDrawable.getBounds().width(), 0, 0);
		if (offsetX > -mFaceDrawable.getBounds().width() - mFontOffset
				&& offsetX < mFontOffset) {
			mFaceDrawable.draw(canvas);
		}
//		mFaceDrawable.draw(canvas);
		offsetX += mFaceDrawable.getBounds().width();
		canvas.translate(mFaceDrawable.getBounds().width(), 0, 0);
		if (offsetX > -mFaceDrawable.getBounds().width() - mFontOffset
				&& offsetX < mFontOffset) {
			mFaceDrawable.draw(canvas);
		}
//		mFaceDrawable.draw(canvas);
		canvas.restoreToCount(save);
		
	}

	/**
	 * 绘制覆盖在图标上的面 包括：上面板 
	 * 
	 * @param canvas
	 * @param drawLeftFirst
	 */
	private void drawCoverPanel(GLCanvas canvas) {
		if (mFaceDrawable == null || mSideDrawable == null
				|| mTbDrawable == null) {
			return;
		}
		canvas.translate(0, mTop);
		drawFontFace(canvas);
		canvas.translate(0, -mTop);
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

	boolean mIsScrollReallyFinished = true;
	boolean mIsSCrollStarted = false;
	boolean mIsOnScrollEndAlphaing = false;
	public boolean isAnimationing() {
		return !mIsScrollReallyFinished;
	}
	
	public void onThemeSwitch() {
	}

	@Override
	public void cleanup() {
		if (mFaceDrawable != null) {
			mFaceDrawable.clear();
			mFaceDrawable = null;
		}
		if (mSideDrawable != null) {
			mSideDrawable.clear();
			mSideDrawable = null;
		}
		if (mTbDrawable != null) {
			mTbDrawable.clear();
			mTbDrawable = null;
		}
		mCurrentScroller = null;
		mAlphaAnimation = null;
	}

	@Override
	public void onScrollStart() {
		
		mHasDoInAnimate = true;
		if (!mIsSCrollStarted) {
			startInOutAnimation(true);
			mIsSCrollStarted = true;
		}
		
		mIsScrollReallyFinished = false;
		mIsOnScrollEndAlphaing = false;
	}

	@Override
	public void onScrollEnd() {
		mIsScrollReallyFinished = false;
		mIsOnScrollEndAlphaing = true;
		mIsSCrollStarted = false;
		startInOutAnimation(false);
	}

	@Override
	public boolean isNeedEnableNextWidgetDrawingCache() {
		return true;
	}

	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset,
			boolean first) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void onDetach() {
		mScroller.setDepthEnabled(false);
		super.onDetach();
		cleanup();
	}
	
	@Override
	public void notifyRegetScreenRect() {
		if (mContainer != null) {
			Rect rect = mContainer.getScreenRect();
			if (rect != null) {
				init(rect);
			}
		}
	}
}
