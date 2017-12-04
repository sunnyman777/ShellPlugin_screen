package com.jiubang.shell.folder.smartcard.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.text.TextUtils;

import com.jiubang.ggheart.apps.desks.diy.pref.PrefConst;
import com.jiubang.ggheart.apps.desks.diy.pref.PrivatePreference;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.launcher.PackageName;

/**
 * 
 * @author guoyiqing
 * 
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class LocalAppLoader {

	private static final int ONE_DAY = 24 * 60 * 60 * 1000;
	private static final int LESS_USE_APP_CREATE_INTERVAL = 15 * ONE_DAY;
	private static final int LESS_USE_TIME_LIMIT = 7 * 24 * 60 * 60 * 1000;
	private Comparator<UpdateAppItem> mUpdateComparator = new Comparator<UpdateAppItem>() {

		@Override
		public int compare(UpdateAppItem object1, UpdateAppItem object2) {
			if (object1.getUpdateTime() > object2.getUpdateTime()) {
				return -1;
			} else if (object1.getUpdateTime() == object2.getUpdateTime()) {
				return 0;
			} else {
				return 1;
			}
		}
	};

	public void load(Context context, CardBuildInfo info) {
		if (info == null) {
			return;
		}
		if (info.getAppItemInfos() == null) {
			info.setLessUseAppItems(null);
			info.setUpdateAppItems(null);
			return;
		}
		info.setLessUseAppItems(new ArrayList<LessUseAppItem>());
		info.setUpdateAppItems(new ArrayList<UpdateAppItem>());
		loadApps(context, info.getAppItemInfos(), info.getLessUseAppItems(),
				info.getUpdateAppItems());
		if (info.getUpdateAppItems() != null) {
			Collections.sort(info.getUpdateAppItems(), mUpdateComparator);
		}
	}

	public void loadApps(Context context, List<AppItemInfo> allAppItemInfos,
			List<LessUseAppItem> lessAppList, List<UpdateAppItem> updateAppList) {
		if (allAppItemInfos == null || allAppItemInfos.isEmpty()
				|| lessAppList == null || updateAppList == null) {
			return;
		}
			for (AppItemInfo appItemInfo : allAppItemInfos) {
				if (appItemInfo.getIsSysApp() || appItemInfo.mIntent == null
						|| appItemInfo.mIntent.getComponent() == null) {
					continue;
				}
				String packageName = appItemInfo.mIntent.getComponent()
						.getPackageName();
				if (TextUtils.isEmpty(packageName)
						|| packageName
								.equals(PackageName.GO_STORE_PACKAGE_NAME)
						|| packageName
								.equals(PackageName.GO_THEME_PACKAGE_NAME)
						|| packageName
								.equals(PackageName.GO_WIDGET_PACKAGE_NAME)
						|| packageName
								.equals(PackageName.RECOMMAND_CENTER_PACKAGE_NAME)
						|| packageName
								.equals(PackageName.GAME_CENTER_PACKAGE_NAME)
						|| packageName
								.equals(PackageName.FREE_THEME_PACKAGE_NAME)
						|| packageName.equals(context.getPackageName())) {
					continue;
				}
				long openTime = appItemInfo.getClickTime(context);
				long curTime = System.currentTimeMillis();
				long timeCache = 0;
				PrivatePreference pref = PrivatePreference
						.getPreference(context);
				long last = pref.getLong(
						PrefConst.KEY_SMART_CARD_LESS_CREATE_TIME, 0);
				boolean less = false;
				long sub = 0;
				if (System.currentTimeMillis() - last > LESS_USE_APP_CREATE_INTERVAL) {
					less = true;
				}
				if (openTime != 0) {
					timeCache = safeLongParse(openTime);
					sub = curTime - timeCache;
				} else {
					sub = curTime - getInstallTime(context, appItemInfo);
				}
				if (less && sub >= LESS_USE_TIME_LIMIT) {
					LessUseAppItem item = new LessUseAppItem();
					item.setAppName(appItemInfo.mTitle);
					item.setIcon(appItemInfo.mIcon);
					item.setPackage(appItemInfo.getAppPackageName());
					item.setIdleDay((int) (sub / ONE_DAY));
					item.setSize(parseSize(appItemInfo.getAppSize(context
							.getPackageManager())) + "M");
					lessAppList.add(item);
				}
				addUpdateApp(context, appItemInfo, timeCache, updateAppList);
			}
	}

	private void addUpdateApp(Context context, AppItemInfo info,
			long timeCache, List<UpdateAppItem> updateAppList) {
		PackageManager packageManager = context.getPackageManager();
		boolean updated = false;
		long updateTime = 0L;
		if (Build.VERSION.SDK_INT >= 9) {
			try {
				PackageInfo packageInfo = packageManager.getPackageInfo(
						info.getAppPackageName(), 0);
				updateTime = packageInfo.lastUpdateTime;
				updated |= packageInfo.firstInstallTime < packageInfo.lastUpdateTime
						&& timeCache < packageInfo.lastUpdateTime;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			updateTime = info.getTimeNotCache(packageManager);
			updated |= timeCache < updateTime;
		}
		if (updated) {
			UpdateAppItem item = new UpdateAppItem();
			item.setLastUpdateTime(updateTime);
			item.setAppName(info.mTitle);
			item.setIcon(info.mIcon);
			item.setMainIntent(info.mIntent);
			item.setUpdateTime(updateTime);
			item.setSize(parseSize(info.getAppSize(packageManager)) + "M");
			updateAppList.add(item);
		}
	}

	private String parseSize(long size) {
		final int m = 1024;
		float d = (size + 0.1f) / m;
		d /= m;
		d = ((float) (int) (d * 10)) / 10;
		return String.valueOf(d);
	}

	private long getInstallTime(Context context, AppItemInfo info) {
		PackageManager packageManager = context.getPackageManager();
		if (Build.VERSION.SDK_INT >= 9) {
			try {
				PackageInfo packageInfo = packageManager.getPackageInfo(
						info.getAppPackageName(), 0);
				return packageInfo.firstInstallTime;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			return info.getTimeNotCache(packageManager);
		}
		return 0;
	}

	private long safeLongParse(Object object) {
		try {
			return Long.parseLong(String.valueOf(object));
		} catch (NumberFormatException e) {
			return 0L;
		}
	}

}
