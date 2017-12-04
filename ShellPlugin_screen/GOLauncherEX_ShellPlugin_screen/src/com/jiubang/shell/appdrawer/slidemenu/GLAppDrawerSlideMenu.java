package com.jiubang.shell.appdrawer.slidemenu;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.InterpolatorValueAnimation;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.proxy.MsgMgrProxy;
import com.go.util.graphics.DrawUtils;
import com.golauncher.message.IAppDrawerMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.shell.appdrawer.GLAppDrawer;
import com.jiubang.shell.appdrawer.slidemenu.slot.ISlideMenuViewSlot;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.utils.ViewUtils;

/**
 * 功能表侧边栏
 * @author wuziyi
 *
 */
public class GLAppDrawerSlideMenu extends GLLinearLayout
		implements
			OnClickListener {
	private boolean mIsFirstEnter = true;
	private SlideMenuActionListener mSlideMenuActionListener;
	private InterpolatorValueAnimation mAnimation;
	private boolean mIsDrawingAnimation;
	private float mRightMark;
//	private int mCurrentAlpha;
	
	private SlideMenuContent mSlideMenuContent;
//	private static final int FULL_ALPHA = 255;
	
	private Animation mExtendFunOutAnimation;
	private Animation mExtendFunInAnimation;
	
	public GLAppDrawerSlideMenu(Context context) {
		super(context);
		setHasPixelOverlayed(false);
	}
	
	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub
		super.setAlpha(alpha);
	}
	
	public GLAppDrawerSlideMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		setHasPixelOverlayed(false);
	}
	
	@Override
	public void draw(GLCanvas canvas) {
		float oldRight = getRight();
		float curValue =  mAnimation.getValue();
		float persent = curValue / getWidth();
		if (mAnimation.isFinished() && mIsDrawingAnimation) {
			float dstValue = mAnimation.getDstValue();
			if (mSlideMenuActionListener != null) {
				if (dstValue > 0) {
					mSlideMenuActionListener.onSlideMenuShowEnd();
				} else if (dstValue <= 0) {
					mSlideMenuActionListener.onSlideMenuHideEnd();
				}
				mIsDrawingAnimation = false;
			}
			curValue = dstValue;
			//回调执行完毕
		} else if (!mAnimation.isFinished()) {
			invalidate();
		}
		// 下面是位移算法
		offsetLeftAndRight((int) (curValue - oldRight));
		if (mRightMark != curValue) {
			if (mSlideMenuActionListener != null) {
				mSlideMenuActionListener.onSlideMenuShowPersent(persent);
			}
			mRightMark = curValue;
		}
		super.draw(canvas);
		getBackground().setBounds(0, 0, getWidth(), getHeight() + DrawUtils.getNavBarHeight());
	}
	
	@Override
	protected void onFinishInflate() {
		initViews();
	}
	
	private void initViews() {
		mSlideMenuContent = (SlideMenuContent) findViewById(R.id.appdrawer_slide_menu_content);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int paddingTop = StatusBarHandler.isHide() ? 0 : StatusBarHandler.getStatusbarHeight();
		mPaddingTop = paddingTop;
		super.onLayout(changed, l, t, r, b);
		float curValue = mAnimation.getValue();
		offsetLeftAndRight((int) (curValue - getWidth()));
		float persent = curValue / getWidth();
		if (mRightMark != curValue) {
			if (mSlideMenuActionListener != null) {
				mSlideMenuActionListener.onSlideMenuShowPersent(persent);
			}
			mRightMark = curValue;
		}
		getBackground().setBounds(0, 0, getWidth(), getHeight() + DrawUtils.getNavBarHeight());
	}
	
	public void addViewToSlot(ISlideMenuViewSlot slotView) {
		
	}
	
	public void addViewsToSlot(List<ISlideMenuViewSlot> slotViews) {
		
	}
	
	@Override
	public void onClick(GLView view) {
		// TODO Auto-generated method stub
		
	}

	public void setValueAnimation(InterpolatorValueAnimation mSlideAnimation) {
		mAnimation = mSlideAnimation;
	}
	
	public void startSlideAnimation(InterpolatorValueAnimation mSlideAnimation) {
		mAnimation = mSlideAnimation;
		if (mSlideMenuActionListener != null) {
			float dstValue = mAnimation.getDstValue();
			if (dstValue > 0) {
				mSlideMenuActionListener.onSlideMenuShowStart();
			} else if (dstValue <= 0) {
				mSlideMenuActionListener.onSlideMenuHideStart();
			}
		}
		mIsDrawingAnimation = true;
		invalidate();
	}
	
	public void handleExtendFuncAnim(boolean visible, int animateType) {
		setVisible(visible);
		if (animateType != GLAppDrawer.EXTEND_FUNC_ANIM_TYPE_NONE) {
			AnimationTask task = new AnimationTask(true, AnimationTask.PARALLEL);
			Animation animation = null;
			if (visible) {
				animation = mExtendFunInAnimation;
			} else {
				animation = mExtendFunOutAnimation;
			}
			if (animation != null) {
				task.addAnimation(this, animation, new AnimationListenerAdapter() {

					@Override
					public void onAnimationStart(Animation animation) {
						GLAppDrawerSlideMenu.this.setDrawingCacheEnabled(true);
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						GLAppDrawerSlideMenu.this.setDrawingCacheEnabled(false);
					}
				});
			}
			GLAnimationManager.startAnimation(task);
		}
	}

	public void setSlideMenuActionListener(SlideMenuActionListener slideMenuActionListener) {
		mSlideMenuActionListener = slideMenuActionListener;
	}

	public void setExtendFuncInAnimation(Animation animation) {
		mExtendFunInAnimation = animation;
	}

	public void setExtendFuncOutAnimation(Animation animation) {
		mExtendFunOutAnimation = animation;
	}

	public void onHomeAction() {
		//TODO
	}
	
	@Override
	protected void onVisibilityChanged(GLView changedView, int visibility) {
		if (changedView == this) {
			if (visibility == VISIBLE) {
				mSlideMenuContent.refreashRecentGrid();
				mSlideMenuContent.resetClearView();
				mSlideMenuContent.swithPromotionView();
				if (mIsFirstEnter) {
					mIsFirstEnter = false;
				}
			} else {
				if (mSlideMenuContent != null) {
					mSlideMenuContent.reSetPromotion();
				}
			}
		}
	}
	
	public void enterFuntionSlot(int slotId, boolean needAnimation, Object...objs) {
		mSlideMenuContent.enterFuntionSlot(slotId, needAnimation, objs);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		ViewUtils.autoFitDrawingCacheScale(this);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean ret = false;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
					IAppDrawerMsgId.APPDRAWER_SLIDE_MENU_ACTION, 0);
			return true;
		}
		if (ret) {
			return ret;
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}
}

