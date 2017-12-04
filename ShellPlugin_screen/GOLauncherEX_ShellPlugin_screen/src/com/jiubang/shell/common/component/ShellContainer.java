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

package com.jiubang.shell.common.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.view.MotionEvent;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.ICleanup;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.graphics.RenderContext;
import com.go.gl.graphics.Renderable;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewParent;
import com.go.proxy.SettingProxy;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.ggheart.plugin.shell.ShellUtil;
import com.jiubang.shell.IShell;
import com.jiubang.shell.common.listener.ScreenShotListener;
import com.jiubang.shell.deletezone.GLDeleteZone;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.effect.component.GLWaveEffectLayer;
import com.jiubang.shell.gesture.MultiTouchDetector;
import com.jiubang.shell.gesture.OnMultiTouchGestureListener;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.popupwindow.component.GLPopupWindowLayer;
import com.jiubang.shell.utils.GLImageUtil;
import com.jiubang.shell.utils.ToastUtils;
import com.jiubang.shell.widget.component.GLWidgetLayer;

/**
 * A ViewGroup that coordinated dragging across its dscendants
 */
public class ShellContainer extends GLFrameLayout implements ICleanup {
	private static ShellContainer sInstance = null;
	private static boolean sDispatchTouchEvent = true;
	public static boolean sIsTouching = false;
	public static boolean sEnableOrientationControl = true;

	private static final int STATE_SINGLE_TOUCH = 0;
	private static final int STATE_MULTI_TOUCH = 1;
	private int mState = STATE_SINGLE_TOUCH;

	DragController mDragController;

	private ScreenShotRenderable mScreenShotRenderable;

	private ScreenShotListener mScreenShotListener;

	private byte[] mScreenShotLock = new byte[0];

	private boolean mScreenShotFlag;

	private MultiTouchDetector mMultiTouchDetector;

	private GLView mBackWorkspace;
//	private GLView mCoreContainer;
	private IShell mShell;
	private int mOldPaddingLeft;
	private int mOldPaddingTop;
	private int mOldPaddingRight;
	private int mOldPaddingBottom;
	
	private GLDrawable mTransparentStatusBarMask;
	private boolean mIsMultiTouchable = true;
	
	public ShellContainer(Context context) {
		super(context);
		sInstance = this;
		mMultiTouchDetector = new MultiTouchDetector(mContext);
		setChildrenDrawingOrderEnabled(true);
		
		mOldPaddingLeft = getPaddingLeft();
		mOldPaddingTop = getPaddingTop();
		mOldPaddingRight = getPaddingRight();
		mOldPaddingBottom = getPaddingBottom();
		changeNavBarLocation();
		mTransparentStatusBarMask = GLImageUtil
				.getGLDrawable(R.drawable.gl_transparent_status_bar_mask);
		
		initViews(context);
		
	}

	private void initViews(Context context) {
		GLLayoutInflater inflater = ShellAdmin.sShellManager.getLayoutInflater();

		//初始化核心业务容器
		CoreContainer coreContainer = new CoreContainer(context);
		coreContainer.setId(IViewId.CORE_CONTAINER);
		addView(coreContainer, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		//初始化全屏Widget展现层
		GLWidgetLayer widgetLayer = new GLWidgetLayer(context);
		widgetLayer.setId(IViewId.WIDGET_LAYER);
		widgetLayer.setVisibility(GLView.GONE);
		addView(widgetLayer, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		
		//初始化水波纹特效层
		GLWaveEffectLayer waveEffectLayer = new GLWaveEffectLayer(context);
		waveEffectLayer.setId(IViewId.WAVE_EFFECT_LAYER);
		waveEffectLayer.setVisibility(GLView.GONE);
		addView(waveEffectLayer, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		//初始化垃圾桶
		inflater.inflate(R.layout.gl_delete_zone, this, true);
		GLDeleteZone deleteZone = (GLDeleteZone) findViewById(R.id.delete_zone);
		deleteZone.setVisibility(GLView.GONE);

		//初始化拖拽层
		GLDragLayer dragLayer = new GLDragLayer(context);
		dragLayer.setId(IViewId.DRAG_LAYER);
		addView(dragLayer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		//初始化弹出窗口层
		GLPopupWindowLayer popupWindowLayer = new GLPopupWindowLayer(context);
		popupWindowLayer.setId(IViewId.POPUP_WINDOW_LAYER);
		popupWindowLayer.setVisibility(GLView.GONE);
		addView(popupWindowLayer, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		//初始化全屏进度条
		GLProgressBar progressBar = new GLProgressBar(context);
		progressBar.setId(IViewId.FULLSCREEN_PROGRESS_BAR);
		progressBar.setVisibility(GLView.INVISIBLE);
		addView(progressBar, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		//初始化保护层
		GLProtectLayer protectLayer = new GLProtectLayer(context);
		protectLayer.setId(IViewId.PROTECTED_LAYER);
		protectLayer.setVisibility(GLView.INVISIBLE);
		addView(protectLayer,
				new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		mBackWorkspace = coreContainer.findViewById(IViewId.BACK_WORKSPACE);
	}

	public void setOnMultiTouchGestureListener(OnMultiTouchGestureListener listener) {
		mMultiTouchDetector.setOnMultiTouchGestureListener(listener);
	}
	
	public void setMultiTouchable(boolean enable) {
		mIsMultiTouchable = enable;
	}
	
	private boolean isMultiTouchable() {
		return mIsMultiTouchable && !mShell.isViewVisible(IViewId.POPUP_WINDOW_LAYER)
				&& !mShell.isViewVisible(IViewId.WIDGET_LAYER)
				&& !mShell.isViewVisible(IViewId.FULLSCREEN_PROGRESS_BAR)
				&& !mShell.isViewVisible(IViewId.SHELL_GUIDE);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		if (sDispatchTouchEvent || mDragController.isDragging()) {
			if (action == MotionEvent.ACTION_DOWN) {
				sIsTouching = true;
//				if (sEnableOrientationControl) {
//					GLOrientationControler.keepCurrentOrientation();
//				}
			}
			MotionEvent event = MotionEvent.obtain(ev);
			// 因为workspace的TouchEvent总是返回true，需要在此传TouchEvent给mScreenBackground
			boolean ret = super.dispatchTouchEvent(ev) | mBackWorkspace.dispatchTouchEvent(event);
			if (!ret || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
				doTouchUp();
			}
			return ret;
		} else {
			if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
				doTouchUp();
			}
			return true;
		}
	}

	private void doTouchUp() {
		sIsTouching = false;
//		if (sEnableOrientationControl) {
//			GLOrientationControler.resetOrientation();
//		}
		if (!mDragController.isDragging()) {
			IconView.resetIconPressState();
		}
	}
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mTransparentStatusBarMask.setBounds(0, 0, right, StatusBarHandler.getStatusbarHeight());
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean ret = false;
		if (isMultiTouchable() && ev.getPointerCount() > 1) {
			mState = STATE_MULTI_TOUCH;
		} else {
			mState = STATE_SINGLE_TOUCH;
		}
		if (mDragController != null) {
			ret = mDragController.onInterceptTouchEvent(ev);
			if (isMultiTouchable()) {
				if (!ret) {
					ret = mMultiTouchDetector.onTouchEvent(ev);
				}
				if (!ret) {
					ret = mState == STATE_MULTI_TOUCH;
				}
			}
			return ret;
		} else {
			return super.onInterceptTouchEvent(ev);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		boolean ret = false;
		if (mDragController != null) {
			ret = mDragController.onTouchEvent(ev);
			if (!ret && mState == STATE_MULTI_TOUCH && isMultiTouchable()) {
				ret = mMultiTouchDetector.onTouchEvent(ev);
			}
			return ret;
			//			return mDragController.onTouchEvent(ev);
		} else {
			return super.onTouchEvent(ev);
		}
	}

	@Override
	public void cleanup() {
		mDragController = null;
	}

	public void setDragController(DragController controller) {
		mDragController = controller;
	}

	public void setShell(IShell shell) {
		mShell = shell;
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		super.dispatchDraw(canvas);
		if (Machine.isSupportAPITransparentStatusBar()
				&& SettingProxy.getThemeSettingInfo().mIsShowStatusbarBg) {
			canvas.drawDrawable(mTransparentStatusBarMask);
		}
		synchronized (mScreenShotLock) {
			if (mScreenShotFlag) {
				canvas.addRenderable(mScreenShotRenderable, null);
				mScreenShotFlag = false;
			}
		}
	}

	public float getLocation(GLView child, int[] loc) {
		loc[0] = 0;
		loc[1] = 0;
		return getDescendantCoordRelativeToSelf(child, loc);
	}

	/**
	 * Given a coordinate relative to the descendant, find the coordinate in
	 * this DragLayer's coordinates.
	 * 
	 * @param descendant
	 *            The descendant to which the passed coordinate is relative.
	 * @param coord
	 *            The coordinate that we want mapped.
	 * @return The factor by which this descendant is scaled relative to this
	 *         DragLayer.
	 */
	//	public float getDescendantCoordRelativeToSelf(GLView descendant, int[] coord) {
	//		float scale = 1.0f;
	//		float[] pt = { coord[0], coord[1] };
	//		pt[0] += descendant.getLeft();
	//		pt[1] += descendant.getTop();
	//		GLViewParent viewParent = descendant.getGLParent();
	//		if (descendant instanceof FolderElementLayout) {
	//			Log.i("Test", "view: " + descendant + " pt[0]: " + pt[0]);
	//		}
	//		while (viewParent instanceof GLView && viewParent != this) {
	//			final GLView view = (GLView) viewParent;
	//			pt[0] += view.getLeft() - view.getScrollX();
	//			pt[1] += view.getTop() - view.getScrollY();
	//			if (descendant instanceof FolderElementLayout) {
	//				Log.i("Test",
	//						"view: " + view + " left: " + view.getLeft() + " scrollX: "
	//								+ view.getScrollX() + " pt[0]: " + pt[0]);
	//			}
	////			if (descendant instanceof FolderElementLayout) {
	////				Log.i("Test",
	////						"view: " + view + " top: " + view.getTop() + " scrollY: "
	////								+ view.getScrollY() + " pt[1]: " + pt[1]);
	////			}
	//			viewParent = view.getGLParent();
	//		}
	//		if (descendant instanceof FolderElementLayout) {
	//			Log.i("Test", "pt[0]: " + pt[0] + " container width: " + this.getWidth());
	//		}
	//		coord[0] = (int) Math.round(pt[0] % this.getWidth());
	//		if (coord[0] < 0) {//必须为正余数
	//			coord[0] += this.getWidth();
	//		}
	//		coord[1] = (int) Math.round(pt[1] % this.getHeight());
	//		if (coord[1] < 0) {//必须为正余数
	//			coord[1] += this.getHeight();
	//		}
	//		return scale;
	//	}

	public float getDescendantCoordRelativeToSelf(GLView descendant, int[] coord) {
		float scale = 1.0f;
		float[] pt = { coord[0], coord[1] };
		// TODO:LH
		// descendant.getMatrix().mapPoints(pt);
		//scale *= descendant.getScaleX();
		pt[0] += descendant.getLeft();
		pt[1] += descendant.getTop();
		GLViewParent viewParent = descendant.getGLParent();
		while (viewParent instanceof GLView && viewParent != this) {
			final GLView view = (GLView) viewParent;
			// TODO:LH
			// view.getMatrix().mapPoints(pt);
			// scale *= view.getScaleX();
			pt[0] += view.getLeft() - view.getScrollX();
			pt[1] += view.getTop() - view.getScrollY();
			viewParent = view.getGLParent();
		}
		coord[0] = (int) Math.round(pt[0] % this.getWidth());
		coord[1] = (int) Math.round(pt[1]);
		return scale;
	}

	public void captureScreen(int x, int y, int width, int height, ScreenShotListener listener) {
		synchronized (mScreenShotLock) {
			if (mScreenShotRenderable == null) {
				mScreenShotRenderable = new ScreenShotRenderable();
			}
			mScreenShotRenderable.mX = x;
			mScreenShotRenderable.mY = y;
			mScreenShotRenderable.mWidth = width;
			mScreenShotRenderable.mHeight = height;
			mScreenShotListener = listener;
			mScreenShotFlag = true;
			invalidate();
		}
	}

	/**
	 * 
	 * @author yangguanxiang
	 *
	 */
	class ScreenShotRenderable implements Renderable {
		int mX;
		int mY;
		int mWidth;
		int mHeight;
		@Override
		public void run(long timeStamp, RenderContext context) {
			try {
				int[] pixels = new int[mWidth * mHeight];
				ShellUtil.saveScreenshot(mX, mY, mWidth, mHeight, pixels);
				Bitmap bitmap = Bitmap.createBitmap(pixels, mWidth, mHeight, Config.ARGB_8888);
				//			GLCanvas.saveBitmap(bitmap, "/sdcard/screen_shot.png");
				//			Arrays.fill(pixels, 0);
				if (mScreenShotListener != null) {
					mScreenShotListener.onScreenShot(bitmap);
				}
			} catch (OutOfMemoryError e) {
				ToastUtils.showToast(R.string.gl_snapshot_create_image_error_oom,
						Toast.LENGTH_SHORT);
			} catch (UnsatisfiedLinkError e) {
				ToastUtils.showToast(R.string.shellutil_init_failed, Toast.LENGTH_SHORT);
			}
		}
	}

	@Override
	public void cancelLongPress() {

	}

	public static void setDispatchTouchEvent(boolean flag) {
		sDispatchTouchEvent = flag;
		//		if (!flag && sInstance != null) {
		//			sInstance.cancelLongPress();
		//		}
	}

	public static void clearLongPressEvent() {
		if (sInstance != null) {
			sInstance.cancelLongPress();
			int count = sInstance.getChildCount();
			for (int i = 0; i < count; i++) {
				GLView child = sInstance.getChildAt(i);
				child.cancelLongPress();
			}
		}
	}

	public void changeNavBarLocation() {
		int navbarLocation = DrawUtils.getNavBarLocation();
		if (navbarLocation == DrawUtils.NAVBAR_LOCATION_RIGHT) {
			setPadding(mOldPaddingLeft, mOldPaddingTop, mOldPaddingRight  + DrawUtils.getNavBarWidth(), mOldPaddingBottom);
		} else {
			setPadding(mOldPaddingLeft, mOldPaddingTop, mOldPaddingRight, mOldPaddingBottom + DrawUtils.getNavBarHeight());
		}
	}

	
	//	public void getDrawingCacheAnchor(GLView target, int[] loc) {
	//		int screenRight = StatusBarHandler.getDisplayWidth();
	//		int screenBottom = StatusBarHandler.getDisplayHeight();
	//		loc[0] += target.getLeft();
	//		loc[1] += target.getTop();
	//		GLViewParent viewParent = target.getGLParent();
	//		while (viewParent instanceof GLView && viewParent != this) {
	//			final GLView view = (GLView) viewParent;
	//			loc[0] += view.getLeft() - view.getScrollX();
	//			loc[1] += view.getTop() - view.getScrollY();
	//			viewParent = view.getGLParent();
	//		}
	//		loc[0] = (int) Math.round(loc[0] % StatusBarHandler.getDisplayWidth());
	//		loc[1] = (int) Math.round(loc[1] % StatusBarHandler.getDisplayHeight());
	//		if (loc[0] < 0) {
	//			loc[0] += screenRight;
	//		}
	//		if (loc[1] < 0) {
	//			loc[1] += screenBottom;
	//		}
	//	}
}
