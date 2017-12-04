package com.jiubang.shell.animation;

import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.RotateAnimation;

/**
 * 
 * <br>类描述:此动画是屏幕层长按图标效果的创建类
 * <br>功能详细描述:
 * 
 * @author  yuanzhibiao
 * @date  [2012-8-9]
 */
public class WindmillAnimation extends AnimationSet {

	public WindmillAnimation(boolean shareInterpolator, float fromZ, float toZ, float fromAlpha,
			float toAlpha, float fromDegree, float toDegree) {
		super(shareInterpolator);

		DepthAnimation depthAnim = new DepthAnimation(fromZ, toZ, fromAlpha, toAlpha);
		RotateAnimation rotateAnim = new RotateAnimation(fromDegree, toDegree,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		addAnimation(depthAnim);
		addAnimation(rotateAnim);

	}
}
