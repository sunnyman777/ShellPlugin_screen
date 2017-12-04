package com.jiubang.shell.folder.smartcard.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;

/**
 * 
 * @author guoyiqing
 * @date [2013-4-11]
 */
public class CardMemManager {

	private ActivityManager mActivityManager;
	private static int sTotal;
	
	public CardMemManager(Context context) {
		if (context == null) {
			return;
		}
		mActivityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
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
