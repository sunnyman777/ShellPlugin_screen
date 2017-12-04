package com.jiubang.shell.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.graphics.filters.GlowGLDrawable;
import com.go.proxy.SettingProxy;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.appdrawer.component.GLAppDrawerAppIcon;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.folder.GLAppDrawerFolderIcon;
import com.jiubang.shell.folder.GLScreenFolderIcon;
import com.jiubang.shell.gesture.OnMultiTouchGestureListener;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.screen.component.GLScreenShortCutIcon;

/**
 * 
 * @author yangguanxiang
 *
 */
public class IconUtils {
	private static int sIconTextPaddingTop = -1;
	private static IconUtils sInstance = null;
	private int mGlowColor = -1;
	public static int sScreenIconTextHeight = 0;
	public static int sAppDrawerIconTextHeight = 0;
	
	public static IconUtils getInstance() {
		if (sInstance == null) {
			sInstance = new IconUtils();
		}
		return sInstance;
	}
	
	
	public static float[] getIconCenterPoint(float iconViewCenterX, float iconViewCenterY,
			Class<?> iconClass) {
		float[] c = new float[2];
		c[0] = iconViewCenterX;
		int textHeight = 0;
		if (iconClass == GLScreenFolderIcon.class || iconClass == GLScreenShortCutIcon.class) {
			if (sScreenIconTextHeight <= 0) {
				IconView<?> iconView = (IconView<?>) ShellAdmin.sShellManager.getLayoutInflater()
						.inflate(R.layout.gl_screen_shortcut_icon, null);
				textHeight = iconView.getTextPadddingTop() + iconView.getTextLineHeight();
				iconView.cleanup();
			} else {
				textHeight = sScreenIconTextHeight + getIconTextPaddingTop();
			}
		} else if (iconClass == GLAppDrawerFolderIcon.class
				|| iconClass == GLAppDrawerAppIcon.class) {
			if (sAppDrawerIconTextHeight <= 0) {
				IconView<?> iconView = (IconView<?>) ShellAdmin.sShellManager.getLayoutInflater()
						.inflate(R.layout.gl_appdrawer_allapp_icon, null);
				textHeight = iconView.getTextPadddingTop() + iconView.getTextLineHeight() * 2;
				iconView.cleanup();
			} else {
				textHeight = sAppDrawerIconTextHeight;
			}
		}
		c[1] = iconViewCenterY - textHeight / 2;
		return c;
	}

	public static int getIconTextPaddingTop() {
		if (sIconTextPaddingTop == -1) {
			sIconTextPaddingTop = ShellAdmin.sShellManager.getContext().getResources()
					.getDimensionPixelSize(R.dimen.app_icon_text_pad);
		}
		return sIconTextPaddingTop;
	}
	
	
	/**
	 * 创建桌面图标点击高亮效果的GlowGLDrawable
	 */
	public GlowGLDrawable createGlowingOutline(Drawable icon) {
		if (icon == null) {
			return null;
		}
		if (mGlowColor == -1) {
			initGlowingOutColor();
		}
		GlowGLDrawable drawable = new GlowGLDrawable(ShellAdmin.sShellManager.getActivity().getResources(), 
				icon, DrawUtils.dip2px(12), false, false);
		drawable.setGlowColor(mGlowColor);
		drawable.setGlowStrength(/*DrawUtils.dip2px(12)*/24);
		return drawable;
	}
	
	public void initGlowingOutColor() {
		final Resources res = ShellAdmin.sShellManager.getContext().getResources();
		mGlowColor = res.getColor(R.color.icon_glow_color);

		final DesktopSettingInfo info = SettingProxy.getDesktopSettingInfo();
		if (info.mCustomAppBg) {
			mGlowColor = info.mPressColor;
		}
	}
	
	public int getGlowColor() {
		return mGlowColor;
	}
	
	/**
	 * 创建桌面图标托拽移动时显示在底部的图标框GlowGLDrawable
	 */
	public GlowGLDrawable createDragOutline(Drawable icon, int color) {
		if (icon == null) {
			return null;
		}
		GlowGLDrawable drawable = new GlowGLDrawable(ShellAdmin.sShellManager.getActivity().getResources(), 
				icon, 2, false, true);
		drawable.setGlowColor(color);
		drawable.setGlowStrength(80);
		return drawable;
	}
	
	// 速度减少的倍数
	private static final float VOLECITY_SCALE = DrawUtils.sDensity;
	private static final float MAX_FLING_DEGREES = 35f;
	
	/**
	 * 屏幕层图标甩动删除
	 * @param source
	 * @param x
	 * @param y
	 * @param xOffset
	 * @param yOffset
	 * @param dragView
	 * @param dragInfo
	 * @param resetInfo
	 * @param direction
	 * @param velocityX
	 * @param velocityY
	 */
	public boolean iconFlyToDelete(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, DropAnimationInfo resetInfo, int direction,
			int velocityX, int velocityY, AnimationListener listener) {
		if (direction != OnMultiTouchGestureListener.DIRECTION_UP) {
			return false;
		}
		// Do a quick dot product test to ensure that we are flinging upwards
		PointF vel = new PointF(velocityX, velocityY);
		PointF upVec = new PointF(0f, -1f);
		float theta = (float) Math.acos(((vel.x * upVec.x) + (vel.y * upVec.y))
				/ (vel.length() * upVec.length()));
		if (theta > Math.toRadians(MAX_FLING_DEGREES)) { // 向左右的角度在35度之内才响应
			return false;
		}
		
		float actullyVelocityX = velocityX / 1000f / VOLECITY_SCALE;
		float actullyVelocityY = velocityY / 1000f / VOLECITY_SCALE;
		
		int xDuration = 0;
		if (actullyVelocityX > 0) { // 飞右方向
			xDuration = (int) (Math.abs((x + xOffset + dragView.getWidth() / 2)
					/ actullyVelocityX));
		} else if (actullyVelocityX < 0) { // 飞左方向
			xDuration = (int) (Math.abs((StatusBarHandler.getDisplayWidth() - x + xOffset + dragView
					.getWidth() / 2) / actullyVelocityX));
		} else {
			xDuration = 65535; // 表示无限大
		}
		int yDuration = actullyVelocityY == 0 ? 65535 : (int) (Math.abs((y + yOffset + dragView.getHeight() / 2) / actullyVelocityY));
		int minDuration = Math.min(xDuration, yDuration);
		int xDestination = 0;
		int yDestination = 0;
		if (xDuration < yDuration) { // 先到左或右边缘
			if (actullyVelocityX > 0) {
				xDestination = StatusBarHandler.getDisplayWidth() + dragView.getWidth() / 2;
			} else {
				xDestination = -dragView.getWidth() / 2;
			}
			yDestination = (int) (y + yOffset + actullyVelocityY * minDuration);
		} else { // 先到顶部边缘
			yDestination = -dragView.getHeight() / 2;
			xDestination = (int) (x + xOffset + actullyVelocityX * minDuration);
		}
		
		resetInfo.setLocationPoint(xDestination, yDestination);
		resetInfo.setLocationType(DropAnimationInfo.LOCATION_CENTER);
		if (minDuration < 100) { // 过快则进行减速运动
			resetInfo.setInterpolatorType(InterpolatorFactory.VISCOUS_FLUID);
			resetInfo.setEase(InterpolatorFactory.EASE_OUT);
		} else if (minDuration > 200) { // 过慢则默认使用最大的一个值
			minDuration = 200;
		}
		resetInfo.setDuration(2 * minDuration);
		resetInfo.setAnimationListener(listener);
		
		// 透明动画
		Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
		List<Animation> exAnimationList = new ArrayList<Animation>();
		exAnimationList.add(alphaAnimation);
		resetInfo.setExAnimationList(exAnimationList);
		alphaAnimation.setDuration((int) (2 * minDuration));
		dragView.setHasPixelOverlayed(false);
		return true;
	}
}
