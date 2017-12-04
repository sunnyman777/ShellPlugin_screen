package com.jiubang.shell.appdrawer.animation;

import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.jiubang.shell.appdrawer.GLAppDrawerMainView;


/**
 * 
 * <br>类描述:功能表进出动画的顶层父类
 * <br>功能详细描述:
 * 
 * @author  songsiyu
 * @date  [2012-10-10]
 */
public abstract class AppdrawerAnimation {

	
	public static final int ALPHA = 255;
	public static final int NOALPHA = 0;
	public static final int DARKALPHA = (int) (ALPHA * 0.4f);
	public static final long DURATION = 450; // 动画持续时间
	public static final int FROM_NOMAL = 0;
	public static final int FROM_PREVIEW = 1;
	protected long mDuration = DURATION;
	
//	public abstract void runAppdrawerAni(GLAppDrawerMainView appdrawer, boolean isClose, ArrayList<GLView> list,
//			GLImageView[] buttons, boolean fadeBg,AnimationListener animationListener);

	/**
	 * <br>功能简述:开始屏幕层动画
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param isClosed
	 * @param workspace
	 * @param blur 是否需要模糊壁纸
	 * @param forceDrawBg 是否强制画背景
	 */
//	public abstract void runWorkspaceAni(boolean isClosed, Workspace workspace, boolean blur, boolean forceDrawBg);
	
//	public void runWorkspaceAni(boolean isClosed, Workspace workspace) {
//		runWorkspaceAni(isClosed, workspace, true, false);
//	}
	
//	public static Animation getFadeOver(boolean isClose, final Workspace workspace) {
//		if (isClose) {
//			
//			Animation fadeInAni = new Animation() {
//				@Override
//				protected void applyTransformation(float interpolatedTime, Transformation3D t) {
//					final int tempAlpha = (int) (NOALPHA + ((DARKALPHA - NOALPHA) * interpolatedTime));
//					workspace.setBackgroundMaskAlpha(tempAlpha);
//				}
//			};
//			
//			fadeInAni.setDuration(DURATION);
//			return fadeInAni;
//		} else {
//			Animation fadeOutAni = new Animation() {
//				@Override
//				protected void applyTransformation(float interpolatedTime, Transformation3D t) {
//					final int tempAlpha = (int) (DARKALPHA + ((NOALPHA - DARKALPHA) * interpolatedTime));
//					workspace.setBackgroundMaskAlpha(tempAlpha);
//				}
//
//			};
//			
//			fadeOutAni.setDuration(DURATION);
//			return fadeOutAni;
//		}
//	}
	
	public  Animation getFadeOver(boolean isClose, final GLAppDrawerMainView appdrawer) {
		if (isClose) {
			AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
//			Animation fadeAni = new Animation() {
//				@Override
//				protected void applyTransformation(float interpolatedTime, Transformation3D t) {
//					final int tempAlpha = (int) (255 + ((0 - 255) * interpolatedTime));
//					appdrawer.setBackgroundColor(tempAlpha);
//				}
//			};
			
			alphaAnimation.setDuration(mDuration);
			return alphaAnimation;
		} else {
//			Animation fadeAni = new Animation() {
//				@Override
//				protected void applyTransformation(float interpolatedTime, Transformation3D t) {
//					final int tempAlpha = (int) (0 + ((255 - 0) * interpolatedTime));
//					appdrawer.setBackgroundColor(tempAlpha);
//				}
//			};
			AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
			
			alphaAnimation.setDuration(mDuration);
			return alphaAnimation;
		}
	}
	
	public void setDuration(long duration) {
		mDuration = duration;
	}
	
//	public void animationEnd(Workspace workspace, boolean isClose, boolean blur, boolean forceDrawBg) {
//		if (isClose) {
//			workspace.appdrawerMode(blur, forceDrawBg);
//		} else {
//			workspace.setIsWorkspaceFlyAnimation(false);
//		}
//	}
	
//	public void animationEnd(final Appdrawer appdrawer, boolean isClose) {
//		if (isClose) {
//			appdrawer.setVisibility(View.INVISIBLE);
//			appdrawer.closeNewAppEffect();
//		} else {
//			//操作提示
//			OperationGuideData data = OperationGuideData
//					.getInstance(appdrawer.getContext());
//			boolean isFirst = data
//					.isFirst(OperationGuideData.FIRST_ENTER_APPDRAWER);
//			if (isFirst) {
//				LauncherApplication.sendMessage(IViewId.OPERATION_GUIDE_LAYER,
//						this, IOperationGuideMessageId.SHOW_CREATE_FOLDER_GUIDE,
//						IViewId.APPDRAWER, (Object[]) null);
//				data.negativeFirst(OperationGuideData.FIRST_ENTER_APPDRAWER);
//			}
//			appdrawer.mSlidingView.resetEffect(false);
//		}
//		appdrawer.updateAppdrawerAnimationFlag(false);
//		
//		for (Runnable runnable : sAnimationEndRunnables) {
//			runnable.run();
//		}
//		sAnimationEndRunnables.clear();
//	}
}
