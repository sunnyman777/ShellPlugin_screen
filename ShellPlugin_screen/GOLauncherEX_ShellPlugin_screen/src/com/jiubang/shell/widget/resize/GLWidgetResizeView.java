package com.jiubang.shell.widget.resize;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLView;
import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.shell.screen.GLCellLayout;
import com.jiubang.shell.utils.GLImageUtil;
/**
 * widget缩放view
 * @author dengdazhong
 *
 */
public class GLWidgetResizeView extends GLView implements IResizeListener {

	public static final int HIT_TYPE_NONE = -1; //初始值
	public static final int HIT_TYPE_LETF_TOP = 0; //左上
	public static final int HIT_TYPE_LETF_BOTTOM = 1; //左下
	public static final int HIT_TYPE_RIGHT_TOP = 2; //右上
	public static final int HIT_TYPE_RIGHT_BOTTOM = 3; //右下
	public int mHitType = HIT_TYPE_NONE; //点击的类型
	
	public static final int GROW_NONE = 1 << 0;
	public static final int GROW_LEFT_EDGE = 1 << 1;
	public static final int GROW_RIGHT_EDGE = 1 << 2;
	public static final int GROW_TOP_EDGE = 1 << 3;
	public static final int GROW_BOTTOM_EDGE = 1 << 4;
	public static final int MOVE = 1 << 5;
	
	private float mLastX;
	private float mLastY;
	private int mMotionEdge; //点击的边缘的类型（上下左右、4个角）
	private ModifyMode mMode = ModifyMode.None;
	
	private Rect mScreenRect = new Rect();
	private Rect mOriginalRect = new Rect(); // 原始大小
	private Rect mMinRect = new Rect(); // 最小大小
	private Rect mCurRect = new Rect();
	private Rect mLastRect = new Rect();
	
	private boolean mMaintainAspectRatio = false;
	private float mInitialAspectRatio; // 宽和高的比例
	
	private Drawable mResizeDrawableBorder; // 边框
	private Drawable mResizeDrawableWidthLeft; // 横向边框图标
	private Drawable mResizeDrawableWidthRight;
	private Drawable mResizeDrawableHeightTop; // 纵向边框图标
	private Drawable mResizeDrawableHeightBottom;
	private Drawable mResizeDrawableCommon;
	private Drawable mResizeDrawableLeft;
	private Drawable mResizeDrawableRight;
	private Drawable mResizeDrawableTop;
	private Drawable mResizeDrawableBottom;
	
	public GLWidgetResizeView(Context context) {
		super(context);
		initEdgeDrawable();
	}
	
	public GLWidgetResizeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initEdgeDrawable();
	}
	
	public void initEdgeDrawable() {
		Resources resources = getResources();
		try {
			mResizeDrawableBorder = resources.getDrawable(R.drawable.gl_widget_resize_border);
			mResizeDrawableCommon = resources.getDrawable(R.drawable.gl_widget_resize_common);
			mResizeDrawableLeft = resources.getDrawable(R.drawable.gl_widget_resize_width_left);
			mResizeDrawableRight = resources.getDrawable(R.drawable.gl_widget_resize_width_right);
			mResizeDrawableTop = resources.getDrawable(R.drawable.gl_widget_resize_width_top);
			mResizeDrawableBottom = resources.getDrawable(R.drawable.gl_widget_resize_width_bottom);
			mResizeDrawableWidthRight = mResizeDrawableCommon;
			mResizeDrawableWidthLeft = mResizeDrawableCommon;
			mResizeDrawableHeightTop = mResizeDrawableCommon;
			mResizeDrawableHeightBottom = mResizeDrawableCommon;
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		}
	}
	
	public void changeDrawable() {
		final Rect r = mCurRect;
		if (r.left <= mScreenRect.left + 5) {
			mResizeDrawableWidthLeft = mResizeDrawableLeft;
		} else {
			mResizeDrawableWidthLeft = mResizeDrawableCommon;
		}
		if (r.right >= mScreenRect.right - 5) {
			mResizeDrawableWidthRight = mResizeDrawableRight;
		} else {
			mResizeDrawableWidthRight = mResizeDrawableCommon;
		}
		if (r.top <= mScreenRect.top + 5) {
			mResizeDrawableHeightTop = mResizeDrawableTop;
		} else {
			mResizeDrawableHeightTop = mResizeDrawableCommon;
		}
		if (r.bottom >= mScreenRect.bottom - 5) {
			mResizeDrawableHeightBottom = mResizeDrawableBottom;
		} else {
			mResizeDrawableHeightBottom = mResizeDrawableCommon;
		}
	}
	
	
	@Override
	public void draw(GLCanvas canvas) {
		int left = mCurRect.left/* + 1 */;
		int right = mCurRect.right /* + 1 */;
		int top = mCurRect.top /* + 4 */;
		int bottom = mCurRect.bottom /* + 4 */;

		int widthWidthLeft = mResizeDrawableWidthLeft.getIntrinsicWidth() / 2;
		int widthWidthRight = mResizeDrawableWidthRight.getIntrinsicWidth() / 2;
		int widthHeightLeft = mResizeDrawableWidthLeft.getIntrinsicHeight() / 2;
		int widthHeightRight = mResizeDrawableWidthRight.getIntrinsicHeight() / 2;
		int heightTopHeight = mResizeDrawableHeightTop.getIntrinsicHeight() / 2;
		int heightTopWidth = mResizeDrawableHeightTop.getIntrinsicWidth() / 2;
		int heightBottomHeight = mResizeDrawableHeightBottom.getIntrinsicHeight() / 2;
		int heightBottomWidth = mResizeDrawableHeightBottom.getIntrinsicWidth() / 2;

		int xMiddle = (mCurRect.right + mCurRect.left) / 2;
		int yMiddle = (mCurRect.bottom + mCurRect.top) / 2;

		GLDrawable tmpGLDrawable;
		tmpGLDrawable = GLImageUtil.getGLDrawable(mResizeDrawableBorder);
		tmpGLDrawable.setBounds(mCurRect);
		tmpGLDrawable.draw(canvas);
		
		tmpGLDrawable = GLImageUtil.getGLDrawable(mResizeDrawableWidthLeft);
		tmpGLDrawable.setBounds(left - widthWidthLeft, yMiddle - widthHeightLeft, left + widthWidthLeft,
				yMiddle + widthHeightLeft);
		tmpGLDrawable.draw(canvas);

		tmpGLDrawable = GLImageUtil.getGLDrawable(mResizeDrawableWidthRight);
		tmpGLDrawable.setBounds(right - widthWidthRight, yMiddle - widthHeightRight, right
				+ widthWidthRight, yMiddle + widthHeightRight);
		tmpGLDrawable.draw(canvas);

		tmpGLDrawable = GLImageUtil.getGLDrawable(mResizeDrawableHeightTop);
		tmpGLDrawable.setBounds(xMiddle - heightTopWidth, top - heightTopHeight, xMiddle
				+ heightTopWidth, top + heightTopHeight);
		tmpGLDrawable.draw(canvas);

		tmpGLDrawable = GLImageUtil.getGLDrawable(mResizeDrawableHeightBottom);
		tmpGLDrawable.setBounds(xMiddle - heightBottomWidth, bottom - heightBottomHeight, xMiddle
				+ heightBottomWidth, bottom + heightBottomHeight);
		tmpGLDrawable.draw(canvas);
	}

	public void setSize(Rect screenRect, Rect originalRect, Rect minRect) {
		mScreenRect.set(screenRect);
		mOriginalRect.set(originalRect);
		mCurRect.set(originalRect);
		mLastRect.set(originalRect);
//		mMinRect = minRect;
		changeDrawable();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :
				int edge = getHit(event.getX(), event.getY()); //通过xy坐标点获取点击位置在那条边上（角上）
				mMotionEdge = edge;
				if (edge != GROW_NONE) {
					mLastX = event.getX();
					mLastY = event.getY();
					setMode((edge == MOVE) ? ModifyMode.Move : ModifyMode.Grow);
					break;
				} else {
					//不在区域范围内就取消编辑
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SCREEN_EXIT_RESIZE_WIDGET, -1, null);
				}
				break;
			case MotionEvent.ACTION_UP :
				setMode(ModifyMode.None);
				//只有大小有变化才进行处理
				if (mHitType != HIT_TYPE_NONE) {
					onReSizeCompleted(mCurRect);
				}
				break;
			case MotionEvent.ACTION_MOVE :
				boolean outOfSide = checkMoveOutOfSide(mMotionEdge, event.getX(), event.getY());
				if (!outOfSide) {
					handleMotion(mMotionEdge, event.getX() - mLastX, event.getY() - mLastY);
					changeDrawable();
					mLastX = event.getX();
					mLastY = event.getY();
				}
				
				break;
		}
		return true;
	}

	/**
	 * 判断是否超出边界
	 * @param edge
	 * @param dx
	 * @param dy
	 * @return
	 */
	public boolean checkMoveOutOfSide(int edge , float dx, float dy) {
		if ((edge & GROW_LEFT_EDGE) == GROW_LEFT_EDGE) {
			if (dx >= (float) mCurRect.right) {
				return true;
			}
		} 
		
		if ((edge & GROW_RIGHT_EDGE) == GROW_RIGHT_EDGE) {
			if (dx <= (float) mCurRect.left) {
				return true;
			}
		} 
		
		if ((edge & GROW_TOP_EDGE) == GROW_TOP_EDGE) {
			if (dy >= (float) mCurRect.bottom) {
				return true;
			}
		} 
		
		if ((edge & GROW_BOTTOM_EDGE) == GROW_BOTTOM_EDGE) {
			if (dy <= (float) mCurRect.top) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 通过xy坐标点获取点击位置在那条边上（角上）
	 * @param x
	 * @param y
	 * @return
	 */
	public int getHit(float x, float y) {
		Rect r = computeLayout(); //获取裁剪区域
		final float hysteresis = 20F;
		int retval = GROW_NONE;

		//主要判断是否在上下左右中心点上，目前不需要只在中心点。屏蔽下面的
		boolean verticalCheck = true;
		boolean horizCheck = true;
//		boolean verticalCheck = (y >= r.top - mResizeDrawableHeightTop.getIntrinsicHeight() / 2) && (y < r.bottom + mResizeDrawableHeightBottom.getIntrinsicHeight() / 2);
//		boolean horizCheck = (x >= r.left - mResizeDrawableWidthLeft.getIntrinsicWidth() / 2) && (x < r.right + mResizeDrawableWidthRight.getIntrinsicWidth() / 2);
		
		if (r == null || mResizeDrawableWidthLeft == null || mResizeDrawableWidthRight == null
			|| mResizeDrawableHeightTop == null || mResizeDrawableHeightBottom == null) {
			return retval;
		}
		
		//用 |= 方法 判断点击那条边局，边距可以叠加
		if ((Math.abs(r.left - x) < mResizeDrawableWidthLeft.getIntrinsicWidth() / 2) && verticalCheck) {
			retval |= GROW_LEFT_EDGE;
		}
		if ((Math.abs(r.right - x) <  mResizeDrawableWidthRight.getIntrinsicWidth() / 2) && verticalCheck) {
			retval |= GROW_RIGHT_EDGE;
		}
		if ((Math.abs(r.top - y) < mResizeDrawableHeightTop.getIntrinsicHeight() / 2) && horizCheck) {
			retval |= GROW_TOP_EDGE;
		}
		if ((Math.abs(r.bottom - y) < mResizeDrawableHeightBottom.getIntrinsicHeight() / 2) && horizCheck) {
			retval |= GROW_BOTTOM_EDGE;
		}

		// 不在4个边缘上，且在裁剪区域内部
		if (retval == GROW_NONE && r.contains((int) x, (int) y)) {
			retval = MOVE;
		}

		return retval;
	}
	
	private void handleMotion(int edge, float dx, float dy) {
		Rect r = computeLayout();
		if (r.width() == 0 || r.height() == 0) {
			return;
		}
		if (edge == GROW_NONE) {
			return;
		} else if (edge == MOVE) {
			//在裁剪区域内，不处理
			// Convert to image space before sending to moveBy().
			// moveBy(dx * (mCurRect.width() / r.width()),
			// dy * (mCurRect.height() / r.height()));
		} else {
			//在4个边（4个角）上
			if (((GROW_LEFT_EDGE | GROW_RIGHT_EDGE) & edge) == 0) {
				dx = 0;
			}

			if (((GROW_TOP_EDGE | GROW_BOTTOM_EDGE) & edge) == 0) {
				dy = 0;
			}

			// Convert to image space before sending to growBy().
			float xDelta = dx * (mCurRect.width() / r.width()); // mCurRect.width()
																// /
																// r.width()比例系数基本为一
			float yDelta = dy * (mCurRect.height() / r.height());
			/*
			 * growBy((((edge & GROW_LEFT_EDGE) != 0) ? -1 : 1) * xDelta,
			 * (((edge & GROW_TOP_EDGE) != 0) ? -1 : 1) * yDelta);
			 */
			//遍历edge，判断哪些边需要进行修改
			if ((edge & GROW_LEFT_EDGE) == GROW_LEFT_EDGE) {
				growBy(-1 * xDelta, 0, true);
			} 
			if ((edge & GROW_RIGHT_EDGE) == GROW_RIGHT_EDGE) {
				growBy(1 * xDelta, 0, false);
			} 
			if ((edge & GROW_TOP_EDGE) == GROW_TOP_EDGE) {
				growBy(0, -1 * yDelta, true);
			} 
			if ((edge & GROW_BOTTOM_EDGE) == GROW_BOTTOM_EDGE) {
				growBy(0, 1 * yDelta, false);
			}
			
			setHitType(edge);
		}
	}
	
	/**
	 * 设置点击的类型
	 * @param edge
	 */
	public void setHitType(int edge) {
		mHitType = HIT_TYPE_NONE;
		
		//左上角
		if ((edge & GROW_LEFT_EDGE) == GROW_LEFT_EDGE && (edge & GROW_TOP_EDGE) == GROW_TOP_EDGE) {
			mHitType = HIT_TYPE_LETF_TOP;
		}
		
		//左下角
		else  if ((edge & GROW_LEFT_EDGE) == GROW_LEFT_EDGE && (edge & GROW_BOTTOM_EDGE) == GROW_BOTTOM_EDGE) {
			mHitType = HIT_TYPE_LETF_BOTTOM;
		} 
		
		//右上角
		else  if ((edge & GROW_RIGHT_EDGE) == GROW_RIGHT_EDGE && (edge & GROW_TOP_EDGE) == GROW_TOP_EDGE) {
			mHitType = HIT_TYPE_RIGHT_TOP;
		} 
		
		//右下角
		else  if ((edge & GROW_RIGHT_EDGE) == GROW_RIGHT_EDGE && (edge & GROW_BOTTOM_EDGE) == GROW_BOTTOM_EDGE) {
			mHitType = HIT_TYPE_RIGHT_BOTTOM;
		} 
		
		if (mHitType == HIT_TYPE_NONE) {
			//如果点击的是左边/上边,当作左上角
			if ((edge & GROW_LEFT_EDGE) == GROW_LEFT_EDGE || (edge & GROW_TOP_EDGE) == GROW_TOP_EDGE) {
				mHitType = HIT_TYPE_LETF_TOP;
			}
			
			//如果点击的是右边/下边,当作右下角
			else if ((edge & GROW_RIGHT_EDGE) == GROW_RIGHT_EDGE || (edge & GROW_BOTTOM_EDGE) == GROW_BOTTOM_EDGE) {
				mHitType = HIT_TYPE_RIGHT_BOTTOM;
			} 
		}
	}
	
	private void growBy(float dx, float dy, boolean from) {

		if (mMaintainAspectRatio) {
			if (dx != 0) {
				dy = dx / mInitialAspectRatio;
			} else if (dy != 0) {
				dx = dy * mInitialAspectRatio;
			}
		}

		// Don't let the cropping rectangle grow too fast.
		// Grow at most half of the difference between the image rectangle and
		// the cropping rectangle.
		Rect r = new Rect(mCurRect);
		if (dx > 0F && r.width() + 2 * dx > mScreenRect.width()) {
			float adjustment = (mScreenRect.width() - r.width()) / 2F;
			dx = adjustment;
			if (mMaintainAspectRatio) {
				dy = dx / mInitialAspectRatio;
			}
		}
		if (dy > 0F && r.height() + 2 * dy > mScreenRect.height()) {
			float adjustment = (mScreenRect.height() - r.height()) / 2F;
			dy = adjustment;
			if (mMaintainAspectRatio) {
				dx = dy * mInitialAspectRatio;
			}
		}

		if (from) {
			r.set((int) (r.left - dx), (int) (r.top - dy), r.right, r.bottom);
		} else {
			// 不可进入Dock栏范围
			float index_y = r.bottom + dy > mScreenRect.bottom ? r.bottom : r.bottom + dy;
			float index_x = r.right + dx > mScreenRect.right ? r.right : r.right + dx;
			r.set(r.left, r.top, (int) index_x, (int) index_y);
		}
		if (r.width() < mMinRect.width()) {
			r.left = mCurRect.left;
			r.right = mCurRect.right;
		}
		if (r.height() < mMinRect.height()) {
			r.top = mCurRect.top;
			r.bottom = mCurRect.bottom;
		}
		mCurRect.set(r);
		invalidate();
	}
	
	private Rect computeLayout() {
		RectF r = new RectF(mCurRect.left, mCurRect.top, mCurRect.right, mCurRect.bottom);
		return new Rect(Math.round(r.left), Math.round(r.top), Math.round(r.right),
				Math.round(r.bottom));
	}
	
	public void setMode(ModifyMode mode) {
		if (mode != mMode) {
			mMode = mode;
			invalidate();
		}
	}
	
	/**
	 * ModifyMode
	 * @author Administrator
	 *
	 */
	enum ModifyMode {
		None, Move, Grow
	}

	@Override
	public void onSizeChanging(Rect rect) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReSizeCompleted(Rect rect) {
		float minWidth = GLCellLayout.sCellRealWidth;
		float minHeight = GLCellLayout.sCellRealHeight;
		if (minHeight <= 0 || minWidth <= 0) {
			return;
		}
		
		rect.offset(-GLCellLayout.getLeftPadding(), -GLCellLayout.getTopPadding());
		
		// 如果是拉上边/左边/左上角，那么以bottom（right）为坐标定位基准
		if (mHitType == HIT_TYPE_LETF_TOP) {
			final float right = Math.round(rect.right / minWidth) * minWidth;
			final float bottom = Math.round(rect.bottom / minHeight) * minHeight;
			final float left = right - (Math.max(Math.round(rect.width() / minWidth), 1) * minWidth);
			final float top = bottom - (Math.max(Math.round(rect.height() / minHeight), 1) * minHeight);
			rect.set((int) left, (int) top, (int) right, (int) bottom);
		}
		
		// 左下角，那么以right（top）为坐标定位基准
		else if (mHitType == HIT_TYPE_LETF_BOTTOM) {
			final float right = Math.round(rect.right / minWidth) * minWidth;
			final float top = Math.round(rect.top / minHeight) * minHeight;
			final float left = right - (Math.max(Math.round(rect.width() / minWidth), 1) * minWidth);
			final float bottom = top + (Math.max(Math.round(rect.height() / minHeight), 1) * minHeight);
			rect.set((int) left, (int) top, (int) right, (int) bottom);
		}
				
		
		//右上角，那么以left（bottom）为坐标定位基准
		else if (mHitType == HIT_TYPE_RIGHT_TOP) {
			final float left = Math.round(rect.left / minWidth) * minWidth;
			final float bottom = Math.round(rect.bottom / minHeight) * minHeight;
			final float right = left + (Math.max(Math.round(rect.width() / minWidth), 1) * minWidth);
			final float top = bottom - (Math.max(Math.round(rect.height() / minHeight), 1) * minHeight);
			rect.set((int) left, (int) top, (int) right, (int) bottom);
		}
		
		
		// 如果是拉底边/右边/右下角，那么以left（top）为坐标定位基准
		else if (mHitType == HIT_TYPE_RIGHT_BOTTOM) {
			final float left = Math.round(rect.left / minWidth) * minWidth;
			final float top = Math.round(rect.top / minHeight) * minHeight;
			final float right = left + (Math.max(Math.round(rect.width() / minWidth), 1) * minWidth);
			final float bottom = top + (Math.max(Math.round(rect.height() / minHeight), 1) * minHeight);
			rect.set((int) left, (int) top, (int) right, (int) bottom);
		}
		
		rect.offset(GLCellLayout.getLeftPadding(), GLCellLayout.getTopPadding());
		
		int[] position = new int[4];
		boolean collision = MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_RECT_TO_POSITION, -1, rect, position);
		if (!collision) {
			mLastRect.set(rect);
		} else {
			mCurRect.set(mLastRect);
		}
		changeDrawable();
	}
}
