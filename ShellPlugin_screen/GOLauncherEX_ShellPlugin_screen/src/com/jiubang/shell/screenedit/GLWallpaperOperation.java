package com.jiubang.shell.screenedit;

import android.content.Context;

import com.gau.golauncherex.plugin.shell.R;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.push.WallpaperOperation;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.push.bean.WallpaperPushInfo;

/**
 * 
 */
public class GLWallpaperOperation extends WallpaperOperation {

	public GLWallpaperOperation(Context context, int pushType) {
		super(context, pushType);
	}
	
	@Override
	public void loadDefaultData() {
		String packageName = null;
		WallpaperPushInfo pushInfo = null;

		pushInfo = new WallpaperPushInfo();
		packageName = "com.go.livewallpaper.Fireworks";
		pushInfo.setPackageName(packageName);
		if (GoAppUtils.isAppExist(mContext, packageName)) {
			if (!checkInstalledContain(packageName)) {
				pushInfo.setHasInstall(true);
				pushInfo.setName(mPushController.getAppLable(packageName));
				mInstalledList.add(pushInfo);
			}
		} else {
			pushInfo.setName("Love fireworks live wallpaper");
			pushInfo.setIconResPath(R.drawable.gl_screenedit_push_icon_fireworks);
			pushInfo.setDownloadurl("https://play.google.com/store/apps/details?id=com.go.livewallpaper.Fireworks");
			mPushList.add(pushInfo);
		}

		pushInfo = new WallpaperPushInfo();
		packageName = "com.go.livewallpaper.fractalclock";
		pushInfo.setPackageName(packageName);
		if (GoAppUtils.isAppExist(mContext, packageName)) {
			if (!checkInstalledContain(packageName)) {
				pushInfo.setHasInstall(true);
				pushInfo.setName(mPushController.getAppLable(packageName));
				mInstalledList.add(pushInfo);
			}
		} else {
			pushInfo.setName("LWP+Fractal clock");
			pushInfo.setIconResPath(R.drawable.gl_screenedit_push_icon_fractalclock);
			pushInfo.setDownloadurl("https://play.google.com/store/apps/details?id=com.go.livewallpaper.fractalclock");
			mPushList.add(pushInfo);
		}
		
		pushInfo = new WallpaperPushInfo();
		packageName = "com.jiubang.livewallpaper.wave";
		pushInfo.setPackageName(packageName);
		if (GoAppUtils.isAppExist(mContext, packageName)) {
			if (!checkInstalledContain(packageName)) {
				pushInfo.setHasInstall(true);
				pushInfo.setName(mPushController.getAppLable(packageName));
				mInstalledList.add(pushInfo);
			}
		} else {
			pushInfo.setName("Ocean wave live wallpaper");
			pushInfo.setIconResPath(R.drawable.gl_screenedit_push_icon_wave);
			pushInfo.setDownloadurl("https://play.google.com/store/apps/details?id=com.jiubang.livewallpaper.wave");
			mPushList.add(pushInfo);
		}
		
		pushInfo = new WallpaperPushInfo();
		packageName = "com.go.livewallpaper.nexuspro";
		pushInfo.setPackageName(packageName);
		if (GoAppUtils.isAppExist(mContext, packageName)) {
			if (!checkInstalledContain(packageName)) {
				pushInfo.setHasInstall(true);
				pushInfo.setName(mPushController.getAppLable(packageName));
				mInstalledList.add(pushInfo);
			}
		} else {
			pushInfo.setName("Nexus Pro Live wallpaper");
			pushInfo.setIconResPath(R.drawable.gl_screenedit_push_icon_nexuspro);
			pushInfo.setDownloadurl("https://play.google.com/store/apps/details?id=com.go.livewallpaper.nexuspro");
			mPushList.add(pushInfo);
		}
	}
}
