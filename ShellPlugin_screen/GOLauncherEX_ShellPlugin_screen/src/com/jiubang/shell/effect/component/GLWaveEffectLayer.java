package com.jiubang.shell.effect.component;

import android.content.Context;
import android.view.View;

import com.go.gl.graphics.BitmapGLDrawable;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.launcher.IconUtilities;
import com.jiubang.shell.effect.AbstractEffect.EffectItemObserver;
import com.jiubang.shell.effect.EffectController;
import com.jiubang.shell.effect.EffectController.EffectListener;
import com.jiubang.shell.effect.IEffect;
import com.jiubang.shell.effect.WaveEffect;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 做水波纹的层
 * @author zgq
 *
 */
public class GLWaveEffectLayer extends GLFrameLayout {
	private static final int THRESHOLD_1 = Math.round(70 / 2.0f * DrawUtils.sDensity);
	private static final int THRESHOLD_2 = Math.round(120 / 2.0f * DrawUtils.sDensity);
	private static final float FACTOR_0 = 1.0f;
	private static final float FACTOR_1 = 1.1f * 2.0f / DrawUtils.sDensity;
	private static final float FACTOR_2 = 1.2f * 2.0f / DrawUtils.sDensity;
	private GLView mWaveFrameLayout;
	private BitmapGLDrawable mBitmapGLDrawable;
	private EffectController mEffectControler;

	private EffectItemObserver mObserver = new EffectItemObserver() {

		@Override
		public void onStart(IEffect effect) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onEffect(IEffect effect) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onEnd(IEffect effect) {
			// TODO Auto-generated method stub
			mBitmapGLDrawable.clear();
			mBitmapGLDrawable = null;
			mWaveFrameLayout.setDrawingCacheEnabled(false);
			setVisibility(View.GONE);
		}
	};
	
	public GLWaveEffectLayer(Context context) {
		super(context);
		mEffectControler = new EffectController();
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {

		if (mBitmapGLDrawable == null) {
			if (!mWaveFrameLayout.isDrawingCacheEnabled()) {
				mWaveFrameLayout.setDrawingCacheEnabled(true);
			}
			mWaveFrameLayout.setDrawingCacheDepthBuffer(true);
			mBitmapGLDrawable = mWaveFrameLayout.getDrawingCache(canvas);
			mWaveFrameLayout.setDrawingCacheDepthBuffer(false);
		}

//		if (mEffectControler != null) {
//			mEffectControler.updateEffect(0, mBitmapGLDrawable);
			mEffectControler.doEffect(canvas, getDrawingTime(), this, mBitmapGLDrawable);
//		}
	}

	public void wave(GLView waveFrameLayout, int centerX, int centerY,
			long duration, int waveSize, int waveDepth, int damping, EffectListener listener, long delay) {
		if (waveFrameLayout == null) {
			return;
		}
		if (!isVisible()) {
			setVisible(true);
		}
		mWaveFrameLayout = waveFrameLayout;

		WaveEffect iEffect = new WaveEffect(waveFrameLayout, centerX, centerY, duration, waveSize,
				waveDepth, damping);
		int iconHeight = IconUtilities.getIconSize(ShellAdmin.sShellManager.getActivity());
		float factor = FACTOR_0;
		if (iconHeight < THRESHOLD_1) {
			factor = FACTOR_2;
		} else if (iconHeight >= THRESHOLD_1 && iconHeight < THRESHOLD_2) {
			factor = FACTOR_1;
		}
		if (factor < FACTOR_0) {
			factor = FACTOR_0;
		}
		int radiusSize = (int) Math.round(iconHeight / 2.0f / Math.sin(Math.toRadians(45)) * factor);
		iEffect.setRadiusSize(radiusSize);
		iEffect.setEffectObserver(mObserver);
		iEffect.setDelay(delay);
		addEffect(iEffect, listener);
	}

	private void addEffect(IEffect iEffect, EffectListener listener) {
		mEffectControler.cleanEffectListener();
		mEffectControler.addEffectListener(listener);
		mEffectControler.clearEffect();
		mEffectControler.addEffect(iEffect);
		mEffectControler.startAllEffect(null, this);
	}
}
