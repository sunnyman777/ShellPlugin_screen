package com.jiubang.shell.scroller.effector.subscreen;

import android.graphics.drawable.Drawable;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLCanvas;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.SettingProxy;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.indicator.ScreenIndicator;
import com.jiubang.shell.screen.GLCellLayout;
import com.jiubang.shell.screen.GLWorkspace;
import com.jiubang.shell.scroller.ShellScreenScroller;
/**
 * 
 * <br>类描述: 玻璃特效
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2013-5-14]
 */
public class CardScaleEffector extends MSubScreenEffector {
	final static int RADIUS = 1;
	final static float SCALEMIN = 0.7f;
	final static float MAX_ANGLE = 150;
	final static int MIN_ALPHA = 0;
	float mRatio;
	float mScaleMin = SCALEMIN;
	float mScaleMax = 1.0f;
	private Drawable mCardBackground;
	private Drawable mLight;
	private int mBottomPadding;
	private int mRightPadding = 0;
	private int mPadding = 10;
	protected float mPageBackgroundAlpha = 0.0F;
	private float mShadowPadding;
	private boolean mIndicatorOnBottom = false;
	private int mIndicatorH;
	public CardScaleEffector() {
		mCombineBackground = false;
		mShadowPadding = ShellAdmin.sShellManager.getContext().getResources()
				.getDimensionPixelSize(R.dimen.gl_card_effect_shadow_padding);
		mIndicatorH = ShellAdmin.sShellManager.getContext().getResources()
			.getDimensionPixelSize(R.dimen.dots_indicator_height);
		SettingProxy.getShortCutSettingInfo();
		ScreenSettingInfo mScreenInfo = SettingProxy.getScreenSettingInfo();
		mIndicatorOnBottom = mScreenInfo.mIndicatorPosition
				.equals(ScreenIndicator.INDICRATOR_ON_BOTTOM);
	}
	@Override
	protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset,
 boolean first) {
		float offsetF = mScroller.getCurrentScreenDrawingOffset(first);
		// TODO Auto-generated method stub
		mNeedQuality = true;
		
		if (mHeight < mWidth) {
			mBottomPadding = 0;
			mRightPadding = GLCellLayout.getRightPadding();
		} else {
			mRightPadding = 0;
			mBottomPadding = GLCellLayout.getBottomPadding();
		}
		boolean hide = StatusBarHandler.isHide();
		if (mLight == null) {
			mLight = ShellAdmin.sShellManager.getContext().getResources()
					.getDrawable(R.drawable.gl_panel_highlight);
			mLight.setBounds(0, 0, mWidth - mRightPadding, mHeight);
		}
		if (mCardBackground == null) {
			mCardBackground = ShellAdmin.sShellManager.getContext().getResources()
					.getDrawable(R.drawable.gl_panel_frame);
			int statusH = StatusBarHandler.getStatusbarHeight();
			int bottom = mIndicatorOnBottom
					? mHeight - mBottomPadding - statusH + mIndicatorH
					: mHeight - mBottomPadding - statusH;
			mCardBackground.setBounds(0, 0, mWidth + mPadding * 2 - mRightPadding, bottom);
		}
		float t = (float) Math.sin(offsetF * mRatio);
		float doubleT = t * t;
		float s = mScaleMax - (mScaleMax - mScaleMin) * doubleT;
		float leftTop;
		float cell = 1.0f;
		if (first) {
			leftTop = offsetF + mScreenSize * (1 - s);
		} else {
			leftTop = offsetF;
			cell *= -1;
		}
		mAlpha = (int) (ALPHA - (ALPHA - MIN_ALPHA) * doubleT);
		int alpha = (int) (ALPHA - (ALPHA - MIN_ALPHA) * (1 - Math.abs(t)));
		if (mOrientation == ShellScreenScroller.HORIZONTAL) {
			canvas.translate(mScroll + leftTop, (1 - s) * HALF * mHeight);
		} else {
			canvas.translate((1 - s) * HALF * mWidth, mScroll + leftTop);
		}
		float tx = cell > 0 ? -(float) mWidth / 3 * s : (float) mWidth + (float) mWidth / 3;
		float ty = HALF * mHeight;
		float angleX = (s - mScaleMax) * MAX_ANGLE * cell;
		canvas.translate(tx, ty);
		canvas.rotateAxisAngle(-angleX, 0, 1, 0);
		canvas.translate(-tx, -ty);
		canvas.scale(s, s);
		int oldalpha = canvas.getAlpha();
		canvas.setAlpha(alpha);
		int screenH = GoLauncherActivityProxy.getScreenHeight();
		if (GLWorkspace.sLayoutScale == 1.0f && screenH == mHeight) {
			if (!hide) {
				canvas.save();
				canvas.translate(0, StatusBarHandler.getStatusbarHeight());
			}
			int yPadding = 0;
			if (mIndicatorOnBottom) {
				yPadding = mPadding;
			}
			canvas.translate(-mPadding, -yPadding);
			canvas.drawDrawable(mCardBackground);
			canvas.translate(mPadding, yPadding);
			canvas.clipRect(mShadowPadding, 0, mWidth - mShadowPadding - mRightPadding, mHeight);
			drawLight(canvas, screen, first);
			if (!hide) {
				canvas.restore();
			}
		}
		canvas.setAlpha(oldalpha);
		return true;

	}

	@Override
	public void onSizeChanged() {
		// TODO Auto-generated method stub
		super.onSizeChanged();
		mRatio = (float) (Math.PI / 2 / mScreenSize);
		mLight = null;
		mCardBackground = null;
	}

	public void drawLight(GLCanvas canvas, int screen, boolean first) {
		// TODO Auto-generated method stub
		canvas.save();
		int yPadding = 0;
		if (mIndicatorOnBottom) {
			yPadding = mPadding;
		}
		int offset = mScroller.getCurrentScreenOffset();
		if (screen == mScroller.getCurrentScreen()) {
			offset = -offset;
		} else if (first) {
			offset = mScreenSize - offset;
		} else {
			offset = -mScreenSize - offset;
		}
		canvas.translate(offset, -yPadding);
		canvas.drawDrawable(mLight);
		canvas.translate(-offset, yPadding);
		canvas.restore();

	}
}
