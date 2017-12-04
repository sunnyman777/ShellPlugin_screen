package com.jiubang.shell.theme;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLDrawable;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.SettingProxy;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockLogicControler;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.DockItemControler;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.shell.utils.GLImageUtil;

/**
 * Dock栏主题控制器
 * @author yangguanxiang
 *
 */
public class GLDockThemeControler {
	private static GLDockThemeControler sInstance;

	private Context mContext;
	private ShellThemeManager mManager;
	private GLDockThemeControler(Context context) {
		mContext = context;
		mManager = ShellThemeManager.getInstance(context);
	}

	public static GLDockThemeControler getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new GLDockThemeControler(context);
		}
		return sInstance;
	}

	public GLDrawable getDockBgDrawable() {
		Drawable drawable = null;
		ShortCutSettingInfo info = SettingProxy.getShortCutSettingInfo();
		if (info.mCustomBgPicSwitch) {
			drawable = DockLogicControler.getDockBgDrawable();
		} else {
			if (mManager.isUse3dTheme()) {
				drawable = mContext.getResources().getDrawable(R.drawable.gl_dock_bg);
				if (drawable != null) {
					if (drawable instanceof BitmapDrawable) {
						BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
						bitmapDrawable.setTargetDensity(ApplicationProxy.getContext().getResources()
								.getDisplayMetrics());
					}
				}
			} else {
				drawable = DockLogicControler.getDockBgDrawable();
			}
		}
		GLDrawable bg = null;
		if (drawable != null) {
			bg = GLImageUtil.getGLDrawable(drawable);
		}
		return bg;
	}

	public void useStyleForSpecialIcons(String packageName) {
		DockItemControler controler = AppCore.getInstance().getDockItemControler();
//		if (mManager.is3dTheme(packageName)) {
//			int[] ids = new int[] { R.drawable.gl_ui4_phone,
//					R.drawable.gl_ui4_contacts,
//					R.drawable.gl_ui4_allapps, R.drawable.gl_ui4_messaging,
//					R.drawable.gl_ui4_browser };
//			Drawable[] drawables = new Drawable[5];
//			for (int i = 0; i < 5; i++) {
//				drawables[i] = mContext.getResources().getDrawable(ids[i]);
//			}
//			controler.use3dDefaultStyle(drawables);
//		} else {
			controler.useStyle(packageName);
//		}
	}
}
