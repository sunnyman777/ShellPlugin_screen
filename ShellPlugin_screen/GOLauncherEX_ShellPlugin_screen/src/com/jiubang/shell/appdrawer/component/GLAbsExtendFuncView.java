package com.jiubang.shell.appdrawer.component;

import android.content.Context;
import android.view.MotionEvent;

import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLViewGroup;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.shell.IShell;
import com.jiubang.shell.IView;
import com.jiubang.shell.common.listener.RemoveViewAnimationListener;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;

/**
 * 功能表扩展功能页面基类
 * @author yangguanxiang
 *
 */
public abstract class GLAbsExtendFuncView extends GLFrameLayout implements IView, AnimationListener {

	protected IShell mShell;
	protected IExtendFuncViewEventListener mListener;
	protected Animation mInAnimation;
	protected Animation mOutAnimation;

	public GLAbsExtendFuncView(Context context) {
		super(context);
	}
	
	@Override
	public void setVisible(boolean visible, boolean animate, Object obj) {
		if (animate) {
			setVisible(visible);
			if (visible) {
				if (mInAnimation != null) {
					AnimationTask task = new AnimationTask(true, AnimationTask.PARALLEL);
					task.addAnimation(this, mInAnimation, this);
					GLAnimationManager.startAnimation(task);
				} else {
					if (mListener != null) {
						mListener.extendFuncViewOnEnter(this);
					}
				}
			} else {
				if (mListener != null) {
					mListener.extendFuncViewPreExit(this);
				}
				if (mOutAnimation != null) {
					AnimationTask task = new AnimationTask(true, AnimationTask.PARALLEL);
					if (obj instanceof RemoveViewAnimationListener) {
						task.addAnimation(this, mOutAnimation, (RemoveViewAnimationListener) obj);
					} else {
						task.addAnimation(this, mOutAnimation, this);
					}
					GLAnimationManager.startAnimation(task);
				} else {
					if (obj instanceof RemoveViewAnimationListener) {
						((RemoveViewAnimationListener) obj).onAnimationEnd(null);
					}
				}
			}
			
		} else {
			setVisible(visible);
			if (visible) {
				if (mListener != null) {
					mListener.extendFuncViewOnEnter(this);
				}
			} else {
				if (mListener != null) {
					mListener.extendFuncViewPreExit(this);
				}
			}
		}
	}

	@Override
	public void setShell(IShell shell) {
		mShell = shell;
	}

	public void setExtendFuncViewEventListener(IExtendFuncViewEventListener listener) {
		mListener = listener;
	}

	@Override
	public void onAnimationStart(Animation animation) {
		setDrawingCacheEnabled(true);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (animation == mInAnimation) {
			if (mListener != null) {
				mListener.extendFuncViewOnEnter(this);
			}
		} else if (animation == mOutAnimation) {
			if (mListener != null) {
				mListener.extendFuncViewOnExit(this);
			}
		}
		setDrawingCacheEnabled(false);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}
	
	@Override
	public void onAnimationProcessing(Animation animation, float interpolatedTime) {
		
	}

	public void setInAnimation(Animation animation) {
		mInAnimation = animation;
	}

	public void setOutAnimation(Animation animation) {
		mOutAnimation = animation;
	}

	@Override
	public void cleanup() {
		super.cleanup();
		mListener = null;
//		mBg = null;
	}
	
	@Override
	public void onAdd(GLViewGroup parent) {
		if (mListener != null) {
			mListener.extendFuncViewPreEnter(this);
		}
	}
	
	protected void setStatusBarPadding() {
		int paddingTop = StatusBarHandler.isHide() ? 0 : StatusBarHandler.getStatusbarHeight();
		setPadding(getPaddingLeft(), paddingTop + getPaddingTop(), getPaddingRight(),
				getPaddingBottom());
	}
	@Override
	public void onRemove() {
		if (mListener != null) {
			mListener.extendFuncViewOnExit(this);
		}
		setDrawingCacheEnabled(false);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		super.dispatchTouchEvent(ev);
		return true;
	}
}
