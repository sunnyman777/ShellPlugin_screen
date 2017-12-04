package com.jiubang.shell.screen.back;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.go.gl.graphics.ColorGLDrawable;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLView;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.util.LoadDexUtil;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.plugin.PluginClassLoader;
import com.jiubang.ggheart.plugin.theme.ThemeLauncherProxy;
import com.jiubang.ggheart.plugin.theme.inf.IThemeLauncherProxy;
import com.jiubang.shell.screen.back.GLMiddleMonitor.IMiddleCallback;


/**
 * 屏幕背景的绘制类（包括壁纸和中间层）
 * 
 * @author jiangxuwen
 * 
 */
public class GLBackWorkspace extends GLFrameLayout implements GLIWallpaperDrawer, IMiddleCallback {

	private static final String MIDDLE_MATCH_CODE = "Hello_this_is_MiddleFrame_welcome_you";
	
	private static final String ON_UPDATE_BG_XY = "onUpdateBgXY";
	private static final String ON_UPDATE_OFFSET = "onUpdateOffset";
	private static final String ON_UPDATE_SCREEN = "onUpdateScreen";

	public static final String STATE_ON_CREATE = "onCreate";
	public static final String STATE_ON_RESUME = "onResume";
	public static final String STATE_ON_PAUSE = "onPause";
	public static final String STATE_ON_STOP = "onStop";
	public static final String STATE_ON_DESTROY = "onDestroyed";
	public static final String STATE_ON_WAKEUP = "onWakeUp";
	public static final String STATE_ON_READ_VERSION = "onReadVersion";
	public static final String STATE_ON_STATUSBAR_CHANGE = "onStatusBarChange";
	public static final String METHOD_SET_LAUNCHER_PROXY = "setLauncherProxy";

	private GLView mMiddleView;
	private boolean mIsSurfaceView;
	protected Drawable mBackgroundDrawable;
	protected Bitmap mBitmap;
	private int mBgX;
	private int mBgY;
	private int mOffsetX;
	private int mOffsetY;
	// 以下的方法回调比较频繁，所以以静态方式保存，不需要多次反射来getMethod
	private static Method sMethodUpdateBgXY; // 更新壁纸裁剪位置的方法
	private static Method sMethodUpdateOffset; // 更新场景偏移量的方法
	private static Method sMethodUpdateScreen; // 更新当前屏幕的方法
	private static Method sMethodPause; //
	private static Method sMethodStop; //
	private static Method sMethodDestroy; //
	private static Method sMethodWakeUP; //
	private static Method sMethodResume; //
	private GLMiddleMonitor mMonitor;
	private GLCycloidDrawListener mCycloidListener;
	private boolean mUpdateBackground = false;
	private boolean mDrawCycloid = true;
	private int mMiddleScrollExtra;
	private LoadDexUtil mLoadDexUtil = null;
	private int mAlpha = 255;
	private boolean mDrawScreenEditBg;
	private ColorGLDrawable mScreenEditBg = null;

	public GLBackWorkspace(Context context) {
		super(context);
		mScreenEditBg = new ColorGLDrawable(Color.parseColor("#4d000000"));
	}

	private void registerMiddleCallback() {
		if (mMonitor != null) {
			mMonitor.registerCoverCallback(this);
		}
	}

	public void setMiddleView(GLView middleView, boolean isSurfaceview) {
		if (middleView == null) {
			return;
		}
		
		if (mMiddleView != null) {
			middleView.setVisibility(mMiddleView.getVisibility());
		}
		// 每添加一个中间层时，先把上一个移除
		removeMiddleView();

		mMiddleView = middleView;
		mIsSurfaceView = isSurfaceview;
//		if (isSurfaceview) {
//			GLBackSurfaceView surfaceView = new GLBackSurfaceView(getContext());
//			surfaceView.setRenderer(middleView);
//			surfaceView.setZOrderOnTop(true);
//			WindowManager.LayoutParams param = new  WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
//			param.type = WindowManager.LayoutParams.TYPE_WALLPAPER;
//			GoLauncher.getContext().addContentView(surfaceView, param);
//			mMiddleView = surfaceView;
//			if (mShell instanceof ShellFrame) {
//				((ShellFrame) mShell).changePixelFormat(true);
//			}
//		} else {
//			mMiddleView = new GLMiddleView(getContext(), middleView);
//		}
		if (mMiddleView != null) {
			addView(mMiddleView);
			// 主题通过此方法进行数据初始化
			doMiddleViewMethod(STATE_ON_WAKEUP, MIDDLE_MATCH_CODE);
		}
		if (mMonitor == null) {
			mMonitor = new GLMiddleMonitor(getContext());
			registerMiddleCallback();
		}
	}

	public void removeMiddleView() {
		if (mMiddleView != null) {
			onStateMethod(STATE_ON_DESTROY);
			removeView(mMiddleView);
			mMiddleView.cleanup();
			mMiddleView = null;
		}
		mIsSurfaceView = false;
		if (mMonitor != null) {
			mMonitor.cleanup();
			mMonitor = null;
		}
		cleanUpStaticMethod();
	}

	private void cleanUpStaticMethod() {
		sMethodPause = null;
		sMethodUpdateBgXY = null; 
		sMethodUpdateOffset = null; 
		sMethodUpdateScreen = null; 
		sMethodPause = null; 
		sMethodStop = null; 
		sMethodDestroy = null; 
		sMethodWakeUP = null; 
		sMethodResume = null; 
	}
	/**
	 * 由于surfaceView在桌面控制，所以传进来的是一个view，需要isSurfaceView来确定是否组装surfaceView
	 * 
	 * @param pkgName
	 * @param isSurfaceView
	 */
	public void setMiddleView(String pkgName, boolean isSurfaceView) {
		try {
			GLView mainView = null;
		/*	Context remoteContext = getContext().createPackageContext(pkgName,
					Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
			final ThemeInfoBean themeInfoBean = ThemeManager.getInstance(getContext()).getCurThemeInfoBean();
			final String[] classDexNames = themeInfoBean.getClassDexNames();
			// 收费主题的处理
			if (Machine.IS_ICS && classDexNames != null && classDexNames.length > 0) {
				Resources resources = remoteContext.getResources();
				final int length = classDexNames.length;
				int[] dexIds = new int[length];
				for (int i = 0; i < length; i++) {
					dexIds[i] = resources.getIdentifier(classDexNames[i], "raw", pkgName);
				}
				final int versionCode = themeInfoBean.getVerId();
				final String viewPath = themeInfoBean.getMiddleViewPath();Log.i("classloader", "=========VMStack.getCallingClassLoader()" + VMStack.getCallingClassLoader());
				mLoadDexUtil = LoadDexUtil.getInstance(ApplicationProxy.getContext());
				if (mLoadDexUtil != null) {
					mainView = mLoadDexUtil.createDexAppView(pkgName, dexIds, versionCode, viewPath);
				}
			} */
			if (mainView == null) {
				mainView = createAppView(pkgName);
			}
//			setMiddleView(mainView, isSurfaceView);
			
			if (mainView != null) {
				try {
					Method method = mainView.getClass().getMethod(METHOD_SET_LAUNCHER_PROXY,
							IThemeLauncherProxy.class);
					method.invoke(mainView, new ThemeLauncherProxy());
				} catch (Throwable t) {
					t.printStackTrace();
				}
				setMiddleView(mainView, isSurfaceView);
			} else if (mMiddleView != null) {
				removeMiddleView();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	protected void dispatchDraw(GLCanvas canvas) {
		if (!mIsSurfaceView) {
			drawBackground(canvas);
			drawCycloidScreen(canvas);
			super.dispatchDraw(canvas);
		}

		if (mDrawScreenEditBg) {
			mScreenEditBg.setBounds(0, 0, getWidth(), getHeight());
			mScreenEditBg.draw(canvas);
		}
	}

	public void drawBackground(GLCanvas canvas) {
		if (mBitmap != null && mBitmap.isRecycled()) {
			// 如果背景壁纸被其他应用更改了，图片会失效
			mBitmap = null;
			mBackgroundDrawable = null;
			return;
		}
		if (mBitmap != null) {
			canvas.translate(-mOffsetX, 0);
			canvas.drawBitmap(mBitmap, mBgX, -mBgY, null);
			canvas.translate(mOffsetX, 0);
		} else if (mBackgroundDrawable != null) {
			canvas.translate(-mOffsetX + mBgX, -mBgY);
			canvas.drawDrawable(mBackgroundDrawable);
			canvas.translate(mOffsetX - mBgX, mBgY);
		}
		// updateWallpaperOffset();
	}

	private void drawCycloidScreen(GLCanvas canvas) {
		if (mCycloidListener == null || !mDrawCycloid) {
			return;
		}
		final int alpha = mCycloidListener.getCurrentAlpha();
		if (alpha != 0) {
			final int preAlpha = canvas.getAlpha();
			canvas.setAlpha(alpha);
			int tempX = mBgX;
			int scroll = mCycloidListener.getCycloidScroll();
			mBgX = mCycloidListener.getBackgroundX(scroll);
			drawBackground(canvas);
			mBgX = tempX;
			canvas.setAlpha(preAlpha);
		}
	}
	
	/**
	 * 设置背景内容
	 * 
	 * @param drawable
	 * @param bitmap
	 */
	public void setBackground(Drawable drawable, Bitmap bitmap) {
		if (!mIsSurfaceView) {
			mBackgroundDrawable = drawable;
			mBitmap = bitmap;
			if (Machine.IS_HONEYCOMB) {
//				invalidate();
			}
		}
	}

	public void updateXY(int x, int y) {
		if (!mIsSurfaceView) {
			mBgX = x;
			// mBgY = y;
		}
		if (mMiddleView != null) {
			doMiddleViewMethod(ON_UPDATE_BG_XY, x, y);
		}
	}

	private GLView createAppView(String packName) {
		Context remoteContext = null;
		try {
			 remoteContext = ApplicationProxy.getApplication().createPackageContext(packName,
					Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
			
			Resources resources = remoteContext.getResources();
			
			int resourceId = resources.getIdentifier("middle_root_view_gl", "layout", packName);
			// 如果拿到的 id 为0，证明不存在该文件，提示用户升级该主题
			if (resourceId <= 0) {
				Toast.makeText(mContext, "Please update this SUPER THEME to fit the 3D engine!", Toast.LENGTH_LONG).show();
				return null;
			}
			
			PluginClassLoader pluginClassLoader = new PluginClassLoader(remoteContext.getClassLoader(), getClass().getClassLoader());
			GLLayoutInflater inflater = GLLayoutInflater.from(remoteContext);
			inflater.setClassLoader(pluginClassLoader);
			
			// 载入这个类
//			Class clazz = pluginClassLoader.loadClass("com.gau.go.launcherex.theme.middle.ui.MiddleViewAcidPlanet3D");
//
//			Method method = clazz.getMethod("createMiddleView", Context.class);
//
//			Object middleView = method.invoke(clazz, remoteContext);
			
			GLView mainView = inflater.inflate(resourceId, null);
			if (mainView != null) {
				return mainView;
			}
//			if (middleView != null && middleView instanceof GLView) {
//				return ((GLView) middleView);
//			}
			
		} catch (OutOfMemoryError e) {
			// e.printStackTrace();
			return null;
		} catch (Exception e) {
			return null;
		} catch (Error e) {
			return null;
		} finally {
			if (remoteContext != null) {
				GLLayoutInflater.remove(remoteContext);
			}
		}
		return null;
	}

	/**
	 * 桌面状态发生变更，告之中间层进行调整
	 * 
	 * @param stateId
	 *            状态的Id
	 */
	public void onStateMethod(String state) {
		// surfaceView需要额外处理一些线程的机制
		if (mIsSurfaceView) {
//			((GLBackSurfaceView) mMiddleView).onStateMethod(state);
		}
		doMiddleViewMethod(state);
	}

	private void doMiddleViewMethod(String methodName, Object... params) {
		if (mMiddleView == null) {
			return;
		}
		GLView tempView = mMiddleView;
//		if (mIsSurfaceView) {
//			tempView = ((GLBackSurfaceView) mMiddleView).getRenderer();
//		}
		try {
			Class tempClass = mMiddleView.getClass();
			Method tempMethod = null;
			if (methodName.equals(ON_UPDATE_BG_XY)) {
				if (sMethodUpdateBgXY == null) {
					sMethodUpdateBgXY = tempClass.getMethod(methodName, Integer.TYPE, Integer.TYPE);
				}
				tempMethod = sMethodUpdateBgXY;
			} else if (methodName.equals(ON_UPDATE_OFFSET)) {
				if (sMethodUpdateOffset == null) {
					sMethodUpdateOffset = tempClass.getMethod(methodName, Integer.TYPE);
				}
				tempMethod = sMethodUpdateOffset;
			} else if (methodName.equals(ON_UPDATE_SCREEN)) {
				if (sMethodUpdateScreen == null) {
					sMethodUpdateScreen = tempClass.getMethod(methodName, Integer.TYPE,
							Integer.TYPE);
				}
				tempMethod = sMethodUpdateScreen;
			} else if (methodName.equals(STATE_ON_PAUSE)) {
				if (sMethodPause == null) {
					sMethodPause = tempClass.getMethod(methodName);
				}
				tempMethod = sMethodPause;
			} else if (methodName.equals(STATE_ON_STOP)) {
				if (sMethodStop == null) {
					sMethodStop = tempClass.getMethod(methodName);
				}
				tempMethod = sMethodStop;
			} else if (methodName.equals(STATE_ON_RESUME)) {
				if (sMethodResume == null) {
					sMethodResume = tempClass.getMethod(methodName);
				}
				tempMethod = sMethodResume;
			} else if (methodName.equals(STATE_ON_DESTROY)) {
				if (sMethodDestroy == null) {
					sMethodDestroy = tempClass.getMethod(methodName);
				}
				tempMethod = sMethodDestroy;
			} else if (methodName.equals(STATE_ON_WAKEUP)) {
				if (sMethodWakeUP == null) {
					sMethodWakeUP = tempClass.getMethod(methodName, Object.class);
				}
				tempMethod = sMethodWakeUP;
			}
			if (tempMethod != null) {
				tempMethod.invoke(mMiddleView, params);
			}
		} catch (Exception e) {
			Log.i("BackWorkspace", "doMiddleViewMethod() has exception = " + e.getMessage());
		}
	} // end doMiddleViewMethod

	@Override
	public void setAlpha(int alpha) {
		if (!mIsSurfaceView) {
			if (mAlpha != alpha) {
				mAlpha = alpha;
			}
		}
	}

	@Override
	public Drawable getBackgroundDrawable() {
		return mBackgroundDrawable;
	}

	@Override
	public Bitmap getBackgroundBitmap() {
		return mBitmap;
	}

	@Override
	public void updateOffsetX(int offsetX, boolean drawCycloid) {
		mDrawCycloid = drawCycloid;
		if (!mIsSurfaceView) {
			mOffsetX = offsetX;
			if (Machine.IS_HONEYCOMB) {
//				invalidate();
			}
		}
		if (mMiddleView != null) {
			final int offset = mMiddleScrollExtra != 0 ? -mMiddleScrollExtra : -offsetX + mBgX;
			doMiddleViewMethod(ON_UPDATE_OFFSET, offset);
		}
	}

	@Override
	public void updateOffsetY(int offsetY, boolean drawCycloid) {
		if (!mIsSurfaceView) {
			final int backWorkspaceOffsetY = offsetY == 0 ? StatusBarHandler.getStatusbarHeight()
					: 0;
			mOffsetY = backWorkspaceOffsetY;
			mBgY = offsetY;
		}
		if (Machine.IS_HONEYCOMB) {
//			invalidate();
		}
	}

	@Override
	public void updateScreen(int newScreen, int oldScreen) {
		if (mMiddleView != null) {
			doMiddleViewMethod(ON_UPDATE_SCREEN, newScreen, oldScreen);
		}
	}

	@Override
	public void doDraw(GLCanvas canvas, int bgX, int bgY) {
		boolean resetAlpha = false;
		final int preAlpha = canvas.getAlpha();
		if (mAlpha >= 0 && mAlpha < 255) {
			resetAlpha = true;
			canvas.setAlpha(mAlpha);
		}
		try {
			if (mBitmap != null) {
				canvas.drawBitmap(mBitmap, bgX, bgY, null);
			} else if (mBackgroundDrawable != null) {
				canvas.translate(bgX, bgY);
				canvas.drawDrawable(mBackgroundDrawable);
				canvas.translate(-bgX, -bgY);
			}
			if (!mIsSurfaceView) {
				canvas.translate(0, -mBgY);
				super.dispatchDraw(canvas);
				canvas.translate(0, mBgY);
			}
		} catch (Exception e) {

		} finally {
			if (resetAlpha) {
				canvas.setAlpha(preAlpha);
			}
		}
	} // end doDraw

	public void hideMiddleView() {
		if (mMiddleView != null) {
			mMiddleView.setVisibility(GLView.INVISIBLE);
		}
	}

	public void showMiddleView() {
		if (mMiddleView != null) {
			mMiddleView.setVisibility(GLView.VISIBLE);
		}
	}

	@Override
	public void handleRemoveMiddleView() {
		removeMiddleView();
	}

	@Override
	public void handleHideMiddleView() {
		hideMiddleView();
	}

	@Override
	public void handleShowMiddleView() {
		showMiddleView();
	}

	@Override
	public void setCycloidDrawListener(GLCycloidDrawListener listener) {
		mCycloidListener = listener;
	}

	@Override
	public void setUpdateBackground(boolean bool) {
		if (mUpdateBackground != bool) {
			mUpdateBackground = bool;
		}
	} // end setDrawBackColor

	@Override
	public boolean needUpdateBackground() {
		return mUpdateBackground;
	}

	@Override
	public void setMiddleScrollEnabled(boolean enable) {
		if (GoLauncherActivityProxy.isPortait() && !enable) {
			mMiddleScrollExtra = DrawUtils.sWidthPixels >> 1;
		} else {
			mMiddleScrollExtra = 0;
		}
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		// TODO Auto-generated method stub
		try {
			super.onRestoreInstanceState(state);
		} catch (Exception e) {
			// TODO: handle exception
			Log.i("BackWorkspace", "onRestoreInstanceState has exception " + e.getMessage());
		}
	}
	
	/**
	 * 返回中间层是否为surfaceView的标识
	 * @return
	 */
	public boolean hasSurfaceView() {
		return mIsSurfaceView;
	}

	public void needDrawScreenEditBg(boolean value) {
		mDrawScreenEditBg = value;
	}

	public void drawScreenEditBg(boolean show, float interpolator) {
		if (mScreenEditBg != null) {
			int alpha = 0;
			if (show) {
				alpha = (int) (255 * interpolator);
			} else {
				alpha = (int) (255 * (1 - interpolator));
			}
			mScreenEditBg.setAlpha(alpha);
		}
	}
}
