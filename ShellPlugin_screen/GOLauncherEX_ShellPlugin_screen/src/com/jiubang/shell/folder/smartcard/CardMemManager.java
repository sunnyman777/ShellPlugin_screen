package com.jiubang.shell.folder.smartcard;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

/**
 * 
 * @author guoyiqing
 * @date [2013-4-11]
 */
public class CardMemManager {

	private ActivityManager mActivityManager;
	private static int sTotal;
	private static final String TAG = "TaskManager";
	private static final String GO_LAUCNHER_PKG = "com.gau.go.launcherex";
	private final static String PACKAGE_LAUNCHER_ZH = "com.gau.go.launcherex.zh";

	public CardMemManager(Context context) {
		if (context == null) {
			return;
		}
		mActivityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
	}

	public Set<String> getAllRunningPackage() {
		Set<String> runningPkgs = new HashSet<String>();
		List<RunningAppProcessInfo> runs = mActivityManager
				.getRunningAppProcesses();
		for (RunningAppProcessInfo runningAppProcessInfo : runs) {
			if (runningAppProcessInfo.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND
					&& runningAppProcessInfo.importance != RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
				String[] pkgList = runningAppProcessInfo.pkgList;
				if (pkgList != null) {
					int size = pkgList.length;
					for (int i = 0; i < size; i++) {
						runningPkgs.add(pkgList[i]);
					}
				}
			}
		}
		return runningPkgs;
	}

	private List<String> getHomePkgs(Context context) {
		List<String> pkgs = new ArrayList<String>();
		if (context == null) {
			return pkgs;
		}
		PackageManager pManager = context.getPackageManager();
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> list = pManager.queryIntentActivities(resolveIntent,
				0);
		if (list != null && !list.isEmpty()) {
			for (ResolveInfo resolveInfo : list) {
				pkgs.add(resolveInfo.activityInfo.packageName);
			}
		}
		return pkgs;
	}

	public void killProcess(Context context, Set<String> pkgs) {
		int sdk = Build.VERSION.SDK_INT;
		if (pkgs != null && mActivityManager != null) {
			try {
				List<String> safes = getHomePkgs(context);
				for (String string : pkgs) {
					if (string.equals(GO_LAUCNHER_PKG)
							|| string.equals(PACKAGE_LAUNCHER_ZH)) {
						continue;
					}
					if (safes.contains(string)) {
						continue;
					}
					if (sdk >= 8) {
						mActivityManager.killBackgroundProcesses(string);
					} else {
						mActivityManager.restartPackage(string);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public float getMaxMem() {
		if (sTotal != 0) {
			return sTotal;
		}
		String path = "/proc/meminfo";
		String content = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path), 8);
			String line;
			if ((line = br.readLine()) != null) {
				content = line;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		int begin = content.indexOf(':');
		int end = content.indexOf('k');
		content = content.substring(begin + 1, end).trim();
		sTotal = Integer.parseInt(content);
		return sTotal;
	}

	/**
	 * <br>
	 * 注意:[0-100]
	 * 
	 * @return
	 */
	public int getCurrentUsedPercent() {
		float current = getCurrentMem();
		float max = getMaxMem();
		float per = current / max * 1024 * 100;
		return 100 - (int) per;
	}

	/**
	 * <br>
	 * 注意: 单位mb
	 * 
	 * @return
	 */
	public float getCurrentMem() {
		if (mActivityManager != null) {
			MemoryInfo outInfo = new MemoryInfo();
			mActivityManager.getMemoryInfo(outInfo);
			float current = outInfo.availMem;
			current /= 1024;
			current /= 1024;
			return current;
		}
		return 0;
	}

	
	
}
