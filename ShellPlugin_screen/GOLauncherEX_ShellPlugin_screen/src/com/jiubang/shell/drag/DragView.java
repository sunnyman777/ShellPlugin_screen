/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiubang.shell.drag;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.widget.FrameLayout;

import com.go.gl.animation.Animation;
import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.animation.Rotate3DAnimation;
import com.go.gl.animation.Transformation3D;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLContentView;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewParent;
import com.go.proxy.MsgMgrProxy;
import com.go.util.graphics.DrawUtils;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.IShell;
import com.jiubang.shell.animation.BreatheAnimation;
import com.jiubang.shell.common.component.GLDragLayer;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.effect.EffectController;
import com.jiubang.shell.effect.EffectController.EffectListener;
import com.jiubang.shell.effect.IEffect;
import com.jiubang.shell.effect.ScaleValueEffect;
import com.jiubang.shell.screen.GLWorkspace;
import com.jiubang.shell.utils.IconUtils;
import com.jiubang.shell.widget.component.GLWidgetView;

/**
 * 
 * <br>类描述:拖动视图
 * <br>功能详细描述:
 * 
 * @author  liuheng
 * @date  [2012-9-3]
 */
public class DragView extends GLView {

	public static final int DRAGVIEW_TYPE_SCREEN_ICON = 1; // 屏幕层图标
	public static final int DRAGVIEW_TYPE_SCREEN_USERFOLDER = 2; // 屏幕层用户文件夹
	public static final int DRAGVIEW_TYPE_SCREEN_LIVEFOLDER = 3; // 屏幕层系统文件夹
	public static final int DRAGVIEW_TYPE_SCREEN_WIDGET = 4; // 屏幕层widget
	public static final int DRAGVIEW_TYPE_DOCK_ICON = 5; // 快捷条图标
	public static final int DRAGVIEW_TYPE_DOCK_FOLDER = 6; // 快捷条文件夹
	public static final int DRAGVIEW_TYPE_APPDRAWER_ICON = 7; // 功能表图标
	public static final int DRAGVIEW_TYPE_APPDRAWER_FOLDER = 8; // 功能表文件夹
	public static final int DRAGVIEW_TYPE_FOLDER_ICON = 9; // 文件夹内部图标
	public static final int DRAGVIEW_TYPE_SCREEN_FAVORITE_ICON = 10; // 桌面推荐图标
	public static final int DRAGVIEW_TYPE_SCREEN_USERFOLDER_ELEMENT = 11;
	// Number of pixels to add to the dragged item for scaling. Should be even
	// for pixel alignment.

	// private Bitmap mBitmap;

	private boolean mViewAnimation = false; //view是否存在animation
	private Transformation3D mInitTransformation = new Transformation3D();
	private Transformation3D mTemp1 = new Transformation3D();
	private Transformation3D mTemp2 = new Transformation3D();

	private GLView mGLView;
	private int mRegistrationX;
	private int mRegistrationY;

	private int mHalfWidth;
	private int mHalfHeight;

	private Point mDragVisualizeOffset = null;
	private Rect mDragRegion = null;

	private float mScale = 1.0f;
	//	private float mAnimationScale = 1.0f;

	private GLDragLayer.LayoutParams mLayoutParams;
	private GLDragLayer mDragLayer = null;

	private int mInitX;
	private int mInitY;
	private int mTouchX;
	private int mTouchY;
	private boolean mDoReturnAnimation = false;

	/**
	 * 拖动层的深度，由外面设置
	 */
	// com.gtp.nextlauncher.drag.DragViewResetInfo.mLocationPoint[2]
	// 文件夹突出来，图标要在它上面，所以设置的
	final int mNum3 = 3;
	final int mNum4 = 4;
	final int mNum5 = 5;
	final int mNum6 = 6;
	final int mNum7 = 7;
	final int mNum8 = 8;
	final int mNum9 = 9;
	final int mNum10 = 10;
	final int mNum11 = 11;
	final int mNum12 = 12;
	final int mNum13 = 13;
	final int mNum14 = 14;
	final int mNum15 = 15;
	final int mNum16 = 16;
	final int mNum90 = 90;
	public static final int SDRAGVIEWINITDEPTH = 100;
	final int mNum255 = 255;
	final int mNum110 = 110;
	final int mNum180 = 180;
	private float mDragViewDepthPre = 0;
	private float mDragViewDepth = SDRAGVIEWINITDEPTH;
	private float mDragViewTransX = 0;
	private float mDragViewTransY = 0;
	int mChangeAlpha = mNum255;
	private float mRotateY;

	// 旋转角度，x方向上的
	private float mDegree = 0;

	// 功能表编辑画法和其他的地方不一样
	private float mShowType;

	public static final float SHOWTYPE_NORMAL = 0;
	public static final float SHOWTYPE_APPDRAWER = 1;
	public static final float SHOWTYPE_APPDRAWER_GATHERVIEW = 2;
	public static final float SHOWTYPE_APPDRAWER_GATHERVIEW_FOR_QUICKLY = 3; // 快速聚合的模式
	public static final float SHOWTYPE_APPDRAWER_SINGLECHOOSEDVIEW = 4; // 单个选中的模式

	/**
	 * 是否开启深度的绘制
	 */
	private boolean mIsEnableZ = true;

	/**
	 * dragview的裁剪区域，设置这个变量用来画一个经过裁剪的，不完整的dragView
	 */
	private Rect mClipRect = null;

	/**
	 * @author liuyong 旋转角度值:不需要初始化角度，只要更新多指触摸的角度改变值
	 */
	private float mAngleValues;

	final float[] mTempVector1 = new float[3];	//CHECKSTYLE IGNORE
	final float[] mTempVector2 = new float[3];	//CHECKSTYLE IGNORE

	float mBiasX;	//投影到屏幕的位置的浮点数值和截断的整数值之间的误差
	float mBiasY;

	private static final int DELETE_RED_COVER = 0xAAFF0000; //拖拽到删除栏时变红的红色混合色

	private static final int DELETE_ALPHA = 0xCC; //拖拽到删除栏时的透明变化

	private int mDragViewType = -1; // 拖拽view的类型
	
	private EffectController mEffectController;

	/**
	 * Construct the drag view.
	 * <p>
	 * The registration point is the point inside our view that the touch events
	 * should be centered upon.
	 * 
	 * @param context
	 *            A context
	 * @param bitmap
	 *            The view that we're dragging around. We scale it up when we
	 *            draw it.
	 * @param registrationX
	 *            The x coordinate of the registration point.
	 * @param registrationY
	 *            The y coordinate of the registration point.
	 */
	public DragView(GLDragLayer dragLayer, Context context, GLView view, int registrationX,
			int registrationY) {
		super(context);
		mDragLayer = dragLayer;
		mGLView = view;
		mHalfWidth = view.getWidth() / 2;
		mHalfHeight = view.getHeight() / 2;

		// The point in our scaled bitmap that the touch events are located
		mRegistrationX = registrationX;
		mRegistrationY = registrationY;		
		
		mEffectController = new EffectController();
	}

	public GLView getOriginalView() {
		return mGLView;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mGLView.getWidth(), mGLView.getHeight());
	}

	public void setDragViewDepth(float z) {
		if (z == mDragViewDepth) {
			return;
		}

		if (mDragViewDepthPre != 0) {
			//XXX:用抛出异常的方法检测这种情况有无出现
			throw new RuntimeException(
					"DragView.setDragViewDepth: mDragViewDepthPre != 0 (for test)");
		}

		mDragViewDepthPre = mDragViewDepth;
		mDragViewDepth = z;

		invalidate();
	}

	public float getDragViewDepth() {
		return this.mDragViewDepth;
	}

	/**
	 * 是否要启动深度绘制
	 * 
	 * @param on
	 */
	public void setEnableDepth(boolean on) {
		mIsEnableZ = on;
	}

	@Override
	protected void onDraw(GLCanvas canvas) {
		
		
		
		final int oldAlpha = canvas.getAlpha();
		if (mChangeAlpha != mNum255 && !mGLView.isPressed()) {
			canvas.multiplyAlpha(mChangeAlpha);
		}

		if (mIsEnableZ) {
			canvas.translate(mDragViewTransX, mDragViewTransY, mDragViewDepth);
		}

		if (mScale != 1) {
			canvas.scale(mScale, mScale, mHalfWidth, mHalfHeight);
		}

		if (mGLView != null) {

			// 如果drawing为true，那么returnAnimation下面的部分在这一帧就不画，因为同时画会出现残影
			if (mDegree != 0) {
				// 功能表编辑状态下，拖动图标到屏幕层，旋转的角度
				if (mShowType == SHOWTYPE_APPDRAWER && !mDoReturnAnimation) {
					if (this.getTop() >= 0) {
						canvas.translate(-this.getLeft(), -this.getTop());
						canvas.rotateAxisAngle(mDegree, 1, 0, 0);
						canvas.translate(this.getLeft(), this.getTop());
					} else if (this.getTop() < 0 && this.getTop() > -mGLView.getHeight() / 2) {
						float degree = (float) (Math.acos((float) (mGLView.getHeight() / 2 - this
								.getTop()) / mGLView.getHeight())
								* mNum180 / Math.PI);
						canvas.rotateAxisAngle(-degree, 1, 0, 0);
					}
				} else if (mShowType == SHOWTYPE_APPDRAWER_GATHERVIEW
						|| mShowType == SHOWTYPE_APPDRAWER_GATHERVIEW_FOR_QUICKLY
						|| mShowType == SHOWTYPE_APPDRAWER_SINGLECHOOSEDVIEW && !mDoReturnAnimation) {
					if (this.getTop() >= 0) {
						canvas.translate(0, -this.getTop());
						canvas.rotateAxisAngle(mDegree, 1, 0, 0);
						if (mViewAnimation) {
							canvas.translate(0, this.getTop() + mRotateY);
							canvas.rotateAxisAngle(-mDegree, 1, 0, 0);
							canvas.translate(0, -this.getTop() - mRotateY);
						}
						canvas.translate(0, this.getTop());
					} else if (this.getBottom() > 0) {
						// 这么做得原因主要是为了消除因为toRelativeBottomViewPoint()计算时候在两个坐标系之间切换会照成top的坐标在临界点变化很大
						int tempY = Math.abs(this.getTop());
						if (tempY > mRotateY) {
							tempY = (int) mRotateY;
						}
						canvas.translate(0, tempY);
						canvas.rotateAxisAngle(mDegree, 1, 0, 0);
						if (mViewAnimation) {
							canvas.translate(0, mRotateY - tempY);
							canvas.rotateAxisAngle(-mDegree, 1, 0, 0);
							canvas.translate(0, -(mRotateY - tempY));
						}
						canvas.translate(0, -tempY);
					} else {
						canvas.translate(0, mRotateY);
						canvas.rotateAxisAngle(mDegree, 1, 0, 0);
						if (mViewAnimation) {
							canvas.rotateAxisAngle(-mDegree, 1, 0, 0);
						}
						canvas.translate(0, -mRotateY);
					}
				}
			}
			//			boolean finish = false;
			//			if (mDoReturnAnimation) {
			//				finish = mFailureReturnAnimation.applyTransformation(canvas, mGLView.getHeight(),
			//						mIconHeight, mShowType, mRotateY);
			//				if (!finish) {
			//					invalidate();
			//				}
			//			}
			//
			//			boolean successFinish = false;
			//			if (mDoSuccessAnimation) {
			//				successFinish = mSuccessScaleAnimation.applyTransformation(canvas, mShowType,
			//						mIconLeft, mIconTop, mRotateY, mDegree, mIconHeight);
			//				if (!successFinish) {
			//					invalidate();
			//				}
			//			}

			if (mBiasX != 0 && mBiasY != 0) {
				canvas.translate(mBiasX, mBiasY);
			}
			
			if (mEffectController != null) {
				mEffectController.doEffect(canvas, getDrawingTime(), this);
			}

			// 裁剪画
			if (mClipRect != null) {
				int i = canvas.save();
				canvas.clipRect(mClipRect);
				mGLView.draw(canvas);
				canvas.restoreToCount(i);
			} else {
				// 不裁剪画
				mGLView.draw(canvas);
			}
			canvas.setAlpha(oldAlpha);

			//			if (mDoReturnAnimation) {
			//				if (finish) {
			//					if (mFailureListener != null) {
			//						mFailureListener.failAnimationFinish();
			//						mFailureListener = null;
			//					}
			//				}
			//			}

			//			if (mDoSuccessAnimation) {
			//				if (successFinish) {
			//					if (mSuccessAnimationListener != null) {
			//						mSuccessAnimationListener.successAnimationFinish();
			//						mSuccessAnimationListener = null;
			//					}
			//				}
			//			}
		}
	}

	public void setAlpha(int alpha) {
		mChangeAlpha = alpha;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mGLView = null;
	}

	public void onTweenValueChanged(float value, float oldValue) {
		//		mAnimationScale = (1.0f + ((mScale - 1.0f) * value)) / mScale;
		//		invalidate();
	}

	public void onTweenStarted() {
	}

	public void onTweenFinished() {
	}

	/**
	 * workspace缩放时设置视觉偏差
	 * 
	 * @param p
	 */
	public void setDragVisualizeOffset(Point p) {
		mDragVisualizeOffset = p;
	}

	public Point getDragVisualizeOffset() {
		return mDragVisualizeOffset;
	}

	public void setDragRegion(Rect r) {
		mDragRegion = r;
	}

	public Rect getDragRegion() {
		return mDragRegion;
	}

	public void setPaint(Paint paint) {
		invalidate();
	}

	public int getInitX() {
		return mInitX;
	}

	public void setInitX(int x) {
		mInitX = x;
	}

	public int getInitY() {
		return mInitY;
	}

	public void setInitY(int y) {
		mInitY = y;
	}

	private void calcNormalPos(int touchX, int touchY, GLDragLayer.LayoutParams lp, float[] point,
			float[] tempVector) {
		final float w = lp.width;
		final float h = lp.height;

		int centerX = touchX - mRegistrationX;
		int centerY = touchY - mRegistrationY;

		final float[] loc = tempVector;
		GLContentView rootView = mDragLayer.getGLRootView();
		if (rootView != null) {
			if (mIsEnableZ) {
				rootView.unprojectFromReferencePlane(centerX, -centerY, mDragViewDepth
						- mDragViewDepthPre, loc);
			} else {
				rootView.unprojectFromReferencePlane(centerX, -centerY, 0, loc);
			}
			loc[1] = -loc[1];
		}

		point[0] = loc[0] - w  / 2;
		point[1] = loc[1] - h  / 2;
	}

	/**
	 * Create a window containing this view and show it.
	 * 
	 * @param touchX
	 *            the x coordinate the user touched in screen coordinates
	 * @param touchY
	 *            the y coordinate the user touched in screen coordinates
	 * @param 长度为5的数组
	 *            ，存放应该平移的x,y,z轴的值，显示类型(功能表/其他),alpha值(透明度)
	 * @param screenX,view最开始显示的x轴位置
	 * @param screenY,view最开始显示的x轴位置
	 */
	public void show(int touchX, int touchY, float[] transInfo, int screenX, int screenY,
			boolean enableZ) {
		mIsEnableZ = enableZ;
		mTouchX = touchX;
		mTouchY = touchY;
		Animation animation = mGLView.getAnimation();
		if (animation != null && animation instanceof Rotate3DAnimation) {
			mViewAnimation = true;
		}
		mDragViewTransX = transInfo[0];
		mDragViewTransY = transInfo[1];
		mDragViewDepth = transInfo[2];
		mShowType = transInfo[mNum3];

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(0, 0);
		GLDragLayer.LayoutParams lp = new GLDragLayer.LayoutParams(params);

		lp.width = mGLView.getWidth();
		lp.height = mGLView.getHeight();

		mBiasX = 0;
		mBiasY = 0;

		if (mShowType == SHOWTYPE_NORMAL) {
			final float[] point = mTempVector1;
			calcNormalPos(touchX, touchY, lp, point, mTempVector2);
			lp.x = (int) point[0];
			lp.y = (int) point[1];
			mBiasX = point[0] - lp.x;
			mBiasY = point[1] - lp.y;
		} else {
			lp.x = screenX;
			lp.y = screenY;
		}

		mInitX = lp.x;
		mInitY = lp.y;

		lp.customPosition = false;
		setLayoutParams(lp);
		mLayoutParams = lp;

		if (mDragLayer.indexOfChild(this) == -1) {
			mDragLayer.addView(this);
		}
	}

	public void resetCenterPosToTouchPoint() {
		offsetLeftAndRight(mTouchX - getCenterX());
		offsetTopAndBottom(mTouchY - getCenterY());
		mLayoutParams.x = mTouchX - mHalfWidth;
		mLayoutParams.y = mTouchY - mHalfHeight;
		mRegistrationX = 0;
		mRegistrationY = 0;
		move(mTouchX, mTouchY);
	}

	/**
	 * Move the window containing this view.
	 * 
	 * @param touchX
	 *            the x coordinate the user touched in screen coordinates
	 * @param touchY
	 *            the y coordinate the user touched in screen coordinates
	 */
	boolean move(int touchX, int touchY) {
		mTouchX = touchX;
		mTouchY = touchY;
		GLDragLayer.LayoutParams lp = mLayoutParams;
		if (null == lp) {
			return false;
		}
		int x = getLeft();
		int y = getTop();
		mBiasX = 0;
		mBiasY = 0;
		float[] point = mTempVector1;
		if (mShowType > SHOWTYPE_NORMAL) {
			// calcTranslateXAndY();
			toRelativeBottomViewPoint(touchX, touchY, point, mTempVector2);
			// 计算view的左上角
			x = (int) (point[0] - mRegistrationX - mHalfWidth);
			y = (int) (point[1] - mRegistrationY - mHalfHeight);
		} else {
			calcNormalPos(touchX, touchY, lp, point, mTempVector2);
			x = (int) point[0];
			y = (int) point[1];
			mBiasX = point[0] - x;
			mBiasY = point[1] - y;
		}
		offsetLeftAndRight(x - getLeft());
		offsetTopAndBottom(y - getTop());
		lp.x = x;
		lp.y = y;
		invalidate();
		return true;
	}

	public void remove() {
		mDragLayer.removeView(this);
	}

	public void removeInlayout() {
		mDragLayer.removeViewInLayout(this);
	}

	/**
	 * 功能简述:将屏幕坐标转化为相对的面的坐标 功能详细描述: 注意:
	 * 
	 * @param x
	 *            touch的x轴
	 * @param y
	 *            touch的y轴
	 * @param point
	 *            转化后的点坐标
	 */
	private void toRelativeBottomViewPoint(float x, float y, float[] point, float[] tmpVector) {
		final float[] loc = tmpVector;
		GLContentView rootView = getGLRootView();
		// 当前的touch点所处的深度
		float viewDepth = (float) ((this.getTop() + this.getHalfHeight() + mRegistrationY) * Math
				.cos((mNum90 + mDegree) * Math.PI / mNum180));

		float depth = mDragViewDepth + viewDepth;
		if (depth < mDragViewDepth) {
			depth = mDragViewDepth;
		}
		if (rootView != null) {
			rootView.unprojectFromReferencePlane(x, -y, depth, loc);
		}

		// 得到的res为投影到depth深度上的点，再乘以功能表底下的面的变化的逆矩阵，得到了touch点相对于该面的坐标值
		mTemp1.set(mInitTransformation);
		mTemp2.clear();
		mTemp2.setTranslate(mDragViewTransX, mDragViewTransY, mDragViewDepth);
		mTemp1.compose(mTemp2);
		if (this.getTop() >= 0) {
			mTemp2.setRotateAxisAngle(mDegree, 1, 0, 0);
			mTemp1.compose(mTemp2);
		} else if (this.getTop() < 0 && this.getTop() > -mGLView.getHeight() / 2) {
			float degree = (float) (Math.acos((float) (mGLView.getHeight() / 2 - this.getTop())
					/ mGLView.getHeight())
					* mNum180 / Math.PI);
			mTemp2.setRotateAxisAngle(-degree, 1, 0, 0);
			mTemp1.compose(mTemp2);
		}

		loc[2] = depth;
		mTemp1.inverseRotateAndTranslateVector(loc, 0, point, 0, 1);
		point[1] = -point[1];
	}

	/**
	 * 获取拖拽view开始down的坐标
	 * 
	 * @return
	 */
	public int[] getMotionDownXY() {
		int motionDownX = mInitX + mHalfWidth + mRegistrationX;
		int motionDownY = mInitY + mHalfHeight + mRegistrationY;
		int[] motionDownXY = new int[2];
		motionDownXY[0] = motionDownX;
		motionDownXY[1] = motionDownY;
		return motionDownXY;
	}

	public GLFrameLayout getDragLayer() {
		return mDragLayer;
	}

	public GLDragLayer.LayoutParams getLayoutParams() {
		return mLayoutParams;
	}

	//	public void setAnimationScale(float scale) {
	//		mAnimationScale = scale;
	//	}

	public void setDegree(float degree) {
		mDegree = degree;
	}

	public void setAngleValues(float mAngleValues) {
		this.mAngleValues = mAngleValues;
	}

	public float getAngleValues() {
		return mAngleValues;
	}

	//	public float getAnimationScale() {
	//		return mAnimationScale;
	//	}

	public int getHalfWidth() {
		return mHalfWidth;
	}

	public int getHalfHeight() {
		return mHalfHeight;
	}

	public void setHalfWidth(int halfWidth) {
		mHalfWidth = halfWidth;
	}

	public void setHalfHeight(int halfHeight) {
		mHalfHeight = halfHeight;
	}

	public float getShowType() {
		return mShowType;
	}

	public float getRelateRotateY() {
		return mRotateY;
	}

	public float[] getDragViewInfo() {
		float[] point = new float[3];
		point[0] = mDragViewTransX;
		point[1] = mDragViewTransY;
		point[2] = mDragViewDepth;
		return point;
	}

	/**
	 * 
	 * <br>类描述:失败动画结束回调接口
	 * <br>功能详细描述:
	 * 
	 * @author  liuheng
	 * @date  [2012-9-4]
	 */
	protected interface DragViewFailureAnimationListener {

		/**
		 * <br>功能简述:失败动画结束回调
		 * <br>功能详细描述:
		 * <br>注意:
		 */
		public void failAnimationFinish();
	}

	/**
	 * 
	 * <br>类描述:成功动画结束回调接口
	 * <br>功能详细描述:
	 * 
	 * @author  liuheng
	 * @date  [2012-9-4]
	 */
	public interface DragViewSuccessAnimationListener {
		/**
		 * <br>功能简述:成功动画结束回调
		 * <br>功能详细描述:
		 * <br>注意:
		 */
		public void successAnimationFinish();
	}

	/**
	 * <br>功能简述: 提供当前的透明变化值 
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public int getAlpha() {
		return mChangeAlpha;
	}

	/**
	 * <br>功能简述:设置这个dragview的裁剪区域
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param l
	 * @param t
	 * @param r
	 * @param b
	 */
	public void setClipRect(Rect rect) {
		if (rect == null) {
			mClipRect = null;
		} else {
			if (mClipRect == null) {
				mClipRect = new Rect(rect);
			} else {
				mClipRect.set(rect);
			}
		}
	}
	/**
	 * <br>功能简述:开启/关闭拖拽到垃圾桶栏时的红色混合
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param enable
	 */
	public void enableDeleteCover(boolean enable) {
		Mode mode = enable ? Mode.SRC_ATOP : null;
		int alpha = enable ? DELETE_ALPHA : 0xFF; //CHECKSTYLE IGNORE
		if (mGLView instanceof IconView) {
			IconView view = (IconView<?>) mGLView;
			view.setColorFilter(DELETE_RED_COVER, mode);
			view.setAlphaFilter(alpha);
		} else if (mGLView instanceof GLWidgetView) {
			GLWidgetView view = (GLWidgetView) mGLView;
			view.setColorFilter(DELETE_RED_COVER, mode);
			view.setAlpha(alpha);
		} /*else if (mGLView instanceof FolderViewContainer) {
			FolderViewContainer view = (FolderViewContainer) mGLView;
			view.setColorFilter(DELETE_RED_COVER, mode);
			view.setAlphaFilter(alpha);
			} */
		/*else if (mGLView instanceof MultiGatherView) {
			MultiGatherView view = (MultiGatherView) mGLView;
			view.setColorFilter(DELETE_RED_COVER, mode);
			view.setAlpha(alpha);
		}*/
	}

	@Override
	public void invalidate() {
		final int w = getWidth(); // * scale?
		final int h = getHeight();
		GLViewParent glParent = getGLParent();
		if (glParent != null) {
			((GLView) glParent).invalidate(mTouchX - w, mTouchY - h, mTouchX + w, mTouchY + h);
		}
	}

	public int getCenterX() {
		return mLayoutParams.x + mHalfWidth;
	}

	public int getCenterY() {
		return mLayoutParams.y + mHalfHeight;
	}

	public void setDragViewType(int viewType) {
		mDragViewType = viewType;
	}

	/**
	 * 返回当前被拖拽View的类型
	 * @return
	 */
	public int getDragViewType() {
		return mDragViewType;
	}

	public void setScale(float scale) {
		mScale = scale;
	}

	public float getScale() {
		return mScale;
	}

	/**
	 * 如果抓起的是IconView类型，此方法用于获取IconView的图片中点
	 * @return
	 */
	public float[] getIconCenterPoint() {
		float[] p = new float[2];
		if (mGLView instanceof IconView<?>) {
			p = IconUtils.getIconCenterPoint(getCenterX(), getCenterY(), mGLView.getClass());
		}
		return p;
	}

	public void startBreatheAnim(float oriScale) {
		BreatheAnimation scaleAnimation = new BreatheAnimation(this, oriScale, 1f);
		scaleAnimation.setDuration(850);
		scaleAnimation.setRepeatCount(Animation.INFINITE);
		startAnimation(scaleAnimation);
	}

	public void startWaveEffect(IShell shell, float offsetx, float offsety, EffectListener listener, long delay) {

		float center[] = null;
		if (getOriginalView() instanceof IconView<?>) {
			center = getIconCenterPoint();
			center[0] += offsetx;
			center[1] += offsety;
		} else {
			center = new float[2];
			center[0] = getCenterX() + offsetx;
			center[1] = getCenterY() + offsety;
		}
		
		if (GLWorkspace.sLayoutScale == 1) {
			int waveSize = DrawUtils.dip2px(15);
			int damping = 14;
			int waveDepth = 35;
			if (shell != null) {
				shell.wave(IViewId.CORE_CONTAINER, (int) center[0], (int) center[1], 600,
						waveSize, waveDepth, damping, listener, delay);
			}
		} else {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.EFFECT_START_WAVE,
					0, center[0], center[1], shell, listener, delay);
		}
	}

	public void startDropScaleEffect(float curScale, float finalScale, int duration, boolean isNeedToShowCircle) {
		if (isNeedToShowCircle) {
			// 如果能用一个3／4周期的sin曲线插值器代替就好了
			float deapestScale = finalScale - (curScale - finalScale);
			IEffect scalePress = new ScaleValueEffect(deapestScale, curScale, getWidth() / 2, getHeight() / 2, duration / 3 * 2);
			scalePress.setInterpolation(InterpolatorFactory.getInterpolator(InterpolatorFactory.LINEAR, InterpolatorFactory.EASE_IN_OUT));
			IEffect scaleUp = new ScaleValueEffect(finalScale, deapestScale, getWidth() / 2, getHeight() / 2, duration / 3 * 1);
			scaleUp.setInterpolation(InterpolatorFactory.getInterpolator(InterpolatorFactory.LINEAR, InterpolatorFactory.EASE_IN));
			mEffectController.addEffect(scalePress);
			mEffectController.addEffect(scaleUp);
			mEffectController.setEffectMode(EffectController.EFFECT_MODE_SEQUENCE);
		} else {
			IEffect scale = new ScaleValueEffect(finalScale, curScale, getWidth() / 2, getHeight() / 2, duration);
			scale.setInterpolation(InterpolatorFactory.getInterpolator(InterpolatorFactory.LINEAR));
			mEffectController.addEffect(scale);
			mEffectController.setEffectMode(EffectController.EFFECT_MODE_TOGETHER);
		}
		mEffectController.startAllEffect(null, this);
	}

}
