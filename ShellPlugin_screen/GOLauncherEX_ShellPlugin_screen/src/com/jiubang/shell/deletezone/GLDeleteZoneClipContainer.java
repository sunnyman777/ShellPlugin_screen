package com.jiubang.shell.deletezone;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLFrameLayout;

/**
 * 
 * <br>类描述:用来裁剪删除动画的view的容器
 * <br>功能详细描述:
 * 
 * @author  panguowei
 * @date  [2013-1-4]
 */
public class GLDeleteZoneClipContainer extends GLFrameLayout {

	private boolean mIsClipChildren = false;

	public GLDeleteZoneClipContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		if (!mIsClipChildren) {
			super.dispatchDraw(canvas);
		} else {
			int save = canvas.save();
			canvas.clipRect(0, 0, getWidth(), getHeight());
			super.dispatchDraw(canvas);
			canvas.restoreToCount(save);
		}
	}

	public void setClipAnim(boolean clip) {
		mIsClipChildren = clip;
	}

	/**
	 * 
	 * <br>类描述:布局参数
	 * <br>功能详细描述:
	 * 
	 * @author  panguowei
	 * @date  [2012-10-26]
	 */
	public static class LayoutParams extends FrameLayout.LayoutParams {

		// X coordinate of the view in the layout.
		@ViewDebug.ExportedProperty
		public int x;
		// Y coordinate of the view in the layout.
		@ViewDebug.ExportedProperty
		public int y;

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
			x = 0;
			y = 0;
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
			x = 0;
			y = 0;
		}

		public LayoutParams(int x, int y) {
			super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			this.x = x;
			this.y = y;
		}
	}
}
