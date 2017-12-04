package com.jiubang.shell.theme;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;

import com.go.gl.graphics.GLDrawable;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.AppFuncBaseThemeBean;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.theme.bean.GLAppDrawerThemeBean;
import com.jiubang.shell.utils.GLImageUtil;

/**
 * 
 * @author yangguanxiang
 *
 */
public class GLAppDrawerThemeControler {

	private static final Pattern PATTERN = Pattern.compile("\\d+");
	private static GLAppDrawerThemeControler sInstance;
	private Context mContext;
	private ShellThemeManager mManager;
	private AppFuncThemeController mAppFuncThemeControler;
	private GLAppDrawerThemeBean mGLAppDrawerThemeBean;

	public static GLAppDrawerThemeControler getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new GLAppDrawerThemeControler(context);
		}
		return sInstance;
	}

	private GLAppDrawerThemeControler(Context context) {
		mContext = context;
		mManager = ShellThemeManager.getInstance(context);
		mAppFuncThemeControler = AppFuncThemeController.getInstance(ShellAdmin.sShellManager
				.getActivity());
	}

	public AppFuncBaseThemeBean getThemeBean() {
		if (mManager.isUse3dTheme()) {
			if (mGLAppDrawerThemeBean == null) {
				mGLAppDrawerThemeBean = new GLAppDrawerThemeBean();
			}
			return mGLAppDrawerThemeBean;
		} else {
			return mAppFuncThemeControler.getThemeBean();
		}
	}

	public AppFuncBaseThemeBean getThemeBean(String pkgName) {
		if (mManager.is3dTheme(pkgName)) {
			if (mGLAppDrawerThemeBean == null) {
				mGLAppDrawerThemeBean = new GLAppDrawerThemeBean();
			}
			return mGLAppDrawerThemeBean;
		} else {
			return mAppFuncThemeControler.getThemeBean();
		}
	}

	public Drawable getDrawable(String drawableName, boolean addToHashMap, int defResId) {
		Drawable drawable = null;
		if (mManager.isUse3dTheme()) {
			if (drawableName == null || "".equals(drawableName.trim())) {
				return drawable;
			}
			AppFuncUtils appFuncUtils = AppFuncUtils.getInstance(ShellAdmin.sShellManager
					.getActivity());
			boolean matches = false;
			try {
				matches = PATTERN.matcher(drawableName).matches();
			} catch (PatternSyntaxException e) {
				// do nothing
			}

			if (!matches) {
				int resourceId = mContext.getResources().getIdentifier(drawableName, "drawable",
						mContext.getPackageName()); //这里应该从3D主题包取，但现在没有3D主题包，故只从插件包取默认图片
				if (resourceId > 0) {
					//先从功能表主题资源管理器取
					drawable = appFuncUtils.getDrawableFromPicManager(resourceId);
					// 如果没有，再从插件包取
					if (drawable == null) {
						try {
							drawable = mContext.getResources().getDrawable(resourceId);
						} catch (NotFoundException e) {
							//do nothing
						}
						if (drawable != null && addToHashMap) {
							// 加入功能表主题资源管理器
							appFuncUtils.addToPicManager(resourceId, drawable);
						}
					}
				}
			} else {
				int drawableId = Integer.valueOf(drawableName).intValue();
				try {
					drawable = mContext.getResources().getDrawable(drawableId);
				} catch (NotFoundException e) {
					//do nothing
				}
			}
		} else {
			try {
				drawable = mAppFuncThemeControler.getDrawable(drawableName, addToHashMap);
			} catch (Throwable t) {
				//do nothing
			}
		}
		if (drawable == null && defResId > 0) {
			drawable = mContext.getResources().getDrawable(defResId);
		}
		return drawable;
	}

	/**
	 * 
	 * @param drawableName
	 * @param addToHashMap
	 * @param defResId 默认资源id
	 * @return
	 */
	public GLDrawable getGLDrawable(String drawableName, boolean addToHashMap, int defResId) {
		Drawable drawable = getDrawable(drawableName, addToHashMap, defResId);
		return GLImageUtil.getGLDrawable(drawable);
	}

	/**
	 * 
	 * @param drawableName
	 * @param pkgName
	 * @param defResId 默认资源id
	 * @return
	 */
	public Drawable getDrawable(String drawableName, String pkgName, int defResId) {
		Drawable drawable = null;
		if (mManager.is3dTheme(pkgName)) {
			if (drawableName == null || "".equals(drawableName.trim())) {
				return drawable;
			}
			AppFuncUtils appFuncUtils = AppFuncUtils.getInstance(ShellAdmin.sShellManager
					.getActivity());
			boolean matches = false;
			try {
				matches = PATTERN.matcher(drawableName).matches();
			} catch (PatternSyntaxException e) {
				// do nothing
			}

			if (!matches) {
				int resourceId = mContext.getResources().getIdentifier(drawableName, "drawable",
						pkgName);
				if (resourceId > 0) {
					//先从功能表主题资源管理器取
					drawable = appFuncUtils.getDrawableFromPicManager(resourceId);
					// 如果没有，再从插件包取
					if (drawable == null) {
						try {
							drawable = mContext.getResources().getDrawable(resourceId);
						} catch (NotFoundException e) {
							//do nothing
						}
						if (drawable != null && mManager.getCurrentThemePackage().equals(pkgName)) {
							// 加入功能表主题资源管理器
							appFuncUtils.addToPicManager(resourceId, drawable);
						}
					}
				}
			} else {
				int drawableId = Integer.valueOf(drawableName).intValue();
				try {
					drawable = mContext.getResources().getDrawable(drawableId);
				} catch (NotFoundException e) {
					//do nothing
				}
			}
		} else {
			try {
				drawable = mAppFuncThemeControler.getDrawable(drawableName, pkgName);
			} catch (Throwable t) {
				//do nothing
			}
		}
		if (drawable == null && defResId > 0) {
			drawable = mContext.getResources().getDrawable(defResId);
		}
		return drawable;
	}

	public GLDrawable getGLDrawable(String drawableName, String pkgName, int defResId) {
		Drawable drawable = getDrawable(drawableName, pkgName, defResId);
		return GLImageUtil.getGLDrawable(drawable);
	}

	/**
	 * tab栏底部栏主题发生变化，重新获取数据
	 */
	public void parseTabHomeTheme() {
		//		if (!mManager.isUse3dTheme()) {
		mAppFuncThemeControler.parseTabHomeTheme(false);
		//		}
	}

	/**
	 * 重新加载主题资源
	 */
	public void reloadThemeData() {
		//		if (!mManager.isUse3dTheme()) {
		mAppFuncThemeControler.reloadThemeData();
		//		}
	}

	/**
	 * 重新加载文件夹主题资源
	 */
	public void parseFolderTheme(String packageName) {
		if (!mManager.is3dTheme(packageName)) {
			mAppFuncThemeControler.parseFolderTheme();
		}
	}

	/**
	 * 是否为默认主题
	 * 
	 * @return
	 */
	public boolean isDefaultTheme() {
		String curThemeName = mManager.getCurrentThemePackage();
		if (ThemeManager.isAsDefaultThemeToDo(curThemeName)) {
			return true;
		} else {
			return false;
		}
	}
}
