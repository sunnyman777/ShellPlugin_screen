package com.jiubang.shell.theme;

import android.content.Context;

import com.go.proxy.ApplicationProxy;
import com.jiubang.ggheart.data.theme.ThemeManager;

/**
 * 插件包主题管理者
 * @author yangguanxiang
 *
 */
public class ShellThemeManager {
	private static ShellThemeManager sInstance;
	private ThemeManager mThemeManager;
	private ShellThemeManager(Context context) {
		mThemeManager = ThemeManager.getInstance(ApplicationProxy.getContext());
	}
	public static ShellThemeManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ShellThemeManager(context);
		}
		return sInstance;
	}

	public boolean isUse3dTheme() {
		String pkgName = mThemeManager.getCurThemePackage();
		return is3dTheme(pkgName);
	}

	public String getCurrentThemePackage() {
		return mThemeManager.getCurThemePackage();
	}

	public boolean is3dTheme(String pkgName) {
//		return IGoLauncherClassName.DEFAULT_THEME_PACKAGE_3.equals(pkgName);
		return false;
	}
}
