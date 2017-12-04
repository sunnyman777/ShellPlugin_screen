package com.jiubang.shell.deletezone;

import android.content.Context;
import android.widget.FrameLayout;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gowidget.core.IGoWidget3D;
import com.jiubang.shell.animation.AnimationConstant;
import com.jiubang.shell.drag.DragView;

/**
 * 
 * <br>类描述:用来进行删除动画的View
 * <br>功能详细描述:
 * 
 * @author  panguowei
 * @date  [2013-1-4]
 */
public class GLDeleteAnimView extends GLViewGroup {
	private GLView mOriginView;
	private float mScale = 1.0f;
	private GLViewGroup mLayer;
	private int mAlpha = AnimationConstant.ANIMATION_MAX_ALPHA;

	public GLDeleteAnimView(Context context, DragView dragView, GLViewGroup layer) {
		super(context);
		mOriginView = dragView.getOriginalView();
		float depth = dragView.getDragViewDepth();
		mScale = layer.getGLRootView().getProjectScale(depth);
		mLayer = layer;
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		int oldAlpha = canvas.getAlpha();
		canvas.multiplyAlpha(mAlpha);
		canvas.save();
		if (mScale != 1.0f) {
			canvas.scale(mScale, mScale, getWidth() / 2, getHeight() / 2);
		}
		if (mOriginView != null) {
			if (mOriginView instanceof IGoWidget3D) {
				canvas.translate(-mOriginView.getLeft(), -mOriginView.getTop());
				drawChild(canvas, mOriginView, getDrawingTime());
				canvas.translate(mOriginView.getLeft(), mOriginView.getTop());
			} else {
				mOriginView.draw(canvas);
			}
		}
		canvas.restore();
		canvas.setAlpha(oldAlpha);
	}

	public void setAlpha(int alpha) {
		mAlpha = alpha;
	}

	@Override
	public void cleanup() {
		super.cleanup();
		mLayer.removeView(this);
		mLayer = null;
		mOriginView = null;
	}

	/**
	 * <br>功能简述: 显示
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param left
	 * @param top
	 */
	public void show(int left, int top) {
		if (mOriginView != null) {
			mLayer.addView(this);
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(0, 0);
			GLDeleteZoneClipContainer.LayoutParams lp = new GLDeleteZoneClipContainer.LayoutParams(
					params);
			lp.width = mOriginView.getWidth();
			lp.height = mOriginView.getHeight();
			lp.x = left;
			lp.y = top;
			setLayoutParams(lp);
		}
	}
}
