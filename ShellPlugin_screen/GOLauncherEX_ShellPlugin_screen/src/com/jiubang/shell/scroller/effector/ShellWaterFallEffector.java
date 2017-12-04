package com.jiubang.shell.scroller.effector;

import android.content.Context;
import android.util.FloatMath;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLCanvas;
import com.jiubang.shell.scroller.ShellScroller;
import com.jiubang.shell.scroller.ShellScrollerEffector;
import com.jiubang.shell.scroller.ShellScrollerListener;

/**
 * 瀑布特效类
 * @author yejijiong
 *
 */
public class ShellWaterFallEffector implements ShellScrollerEffector {
	/**
	 * 顶部绕X轴旋转的Y偏移量
	 */
	private float mTopTranslateY = 0;
	/**
	 * 底部绕X轴旋转的Y偏移坐标
	 */
	private float mBottomTranslateY = 0;
	/**
	 * 旋转角度
	 */
	public static final int ROTATEX_ANGLE = 55;
	/**
	 * 瀑布特效绘制接口容器
	 */
	private ShellVerticalListContainer mContainer;
	/**
	 * 竖向滚动器
	 */
	private ShellScroller mScroller;
	/**
	 * 单屏可视部分实际高度
	 */
	private int mGridHeight = 0;
	/**
	 * 单屏可视部分实际宽度
	 */
	private int mGridWidth = 0;
	/**
	 * 功能表顶部容器高度
	 */
	private int mTopContainerHeight = 0;
	/**
	 * 功能表底部容器高度
	 */
	private int mBottomContainerHeight = 0;
	
	public ShellWaterFallEffector(Context context) {
		mTopContainerHeight = context.getResources().getDimensionPixelSize(R.dimen.appdrawer_top_bar_container_height);
		mBottomContainerHeight = context.getResources().getDimensionPixelSize(R.dimen.appdrawer_bottom_bar_container_height);
	}
	
	/**
	 * 斜边与临边的差距倍数
	 * @return
	 */
	public static float getWaterAngleTranslate() {
		float angle = (float) (ROTATEX_ANGLE * Math.PI / 180);
		return 1.0f / FloatMath.cos(angle);
	}

	@Override
	public boolean onDraw(GLCanvas canvas) {
		if (mContainer != null) {
			calculateTranslate();
			int scroll = mScroller.getScroll();
			// 画顶部瀑布特效
			canvas.save();
			canvas.clipRect(0, scroll - mTopContainerHeight, mGridWidth, scroll);
			canvas.translate(0, -mTopTranslateY, 0);
			canvas.rotateAxisAngle(-ROTATEX_ANGLE, 1, 0, 0);
			canvas.translate(0, mTopTranslateY, 0);
			mContainer.drawWaterFallEffector(canvas, ShellVerticalListContainer.PART_UP);
			canvas.restore();

			// 画底部瀑布特效
			canvas.save();
			int bottomFoldTop = scroll + mGridHeight;
			canvas.clipRect(0, bottomFoldTop, mGridWidth, bottomFoldTop + mBottomContainerHeight/* * getWaterAngleTranslate()*/);
			canvas.translate(0, -mBottomTranslateY, 0);
			canvas.rotateAxisAngle(ROTATEX_ANGLE, 1, 0, 0);
			canvas.translate(0, mBottomTranslateY, 0);
			mContainer.drawWaterFallEffector(canvas, ShellVerticalListContainer.PART_DOWN);
			canvas.restore();
			return true;
		}
		return false;
	}

	@Override
	public void onAttach(ShellScroller scroller, ShellScrollerListener container) {
		assert scroller != null;
		if (container != null && container instanceof ShellVerticalListContainer) {
			mContainer = (ShellVerticalListContainer) container;
			if (mScroller != scroller) {
				mScroller = scroller;
			}
		} else {
			throw new IllegalArgumentException(
					"container is not an instance of SubScreenEffector.SubScreenContainer");
		}
	}

	@Override
	public void onDetach() {
	}

	@Override
	public void onSizeChanged(int w, int h, int orientation) {
		mGridHeight = h;
		mGridWidth = w;
	}

	@Override
	public void setDrawQuality(int quality) {
	}
	
	/**
	 * 计算选择相关参数
	 */
	private void calculateTranslate() {
		mTopTranslateY = mScroller.getScroll();
		mBottomTranslateY = mScroller.getScroll() + mGridHeight;
	}

}
