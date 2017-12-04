package com.jiubang.shell.system.setting;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

/**
 * 提供屏幕信息获取、设置的类。</br>
 * 权限：见具体方法所需要的权限。</br>
 * 提供了屏幕超时时间获取、设置，屏幕亮度获取、设置方法。
 * @author huchao
 *
 */
public class ScreenInfo {
	/**
	 * 获取屏幕超时时间
	 * @param context  View运行的Context
	 * @return 屏幕超时时间(毫秒)
	 */
	public static int getLightTimeout(Context context) {
		int time = 0;
		try {
			time = android.provider.Settings.System.getInt(context.getContentResolver(),
					android.provider.Settings.System.SCREEN_OFF_TIMEOUT);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return time;
	}

	/**
	 * 设置屏幕超时时间
	 * @param context  View运行的Context
	 * 权限：&lt;uses-permission android:name="android.permission.WRITE_SETTINGS" /&gt;
	 * @param miniSecond  屏幕超时时间(毫秒)
	 * @return  设置屏幕超时时间是否成功
	 */
	public static boolean setLightTimeout(Context context, int miniSecond) {
		return android.provider.Settings.System.putInt(context.getContentResolver(),
				android.provider.Settings.System.SCREEN_OFF_TIMEOUT, miniSecond);
	}

	/**
	 * 判断屏幕是否设置为自动亮度调整
	 * @param context  View运行的Context
	 * @return  屏幕是否为自动亮度调整
	 */
	public static boolean isAutoBrightness(Context context) {
		int value = 0;
		try {
			if (Build.VERSION.SDK_INT >= 8) {
				// Android2.2(Api level 8)
				value = Settings.System.getInt(context.getContentResolver(),
						Settings.System.SCREEN_BRIGHTNESS_MODE);
				return value == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
			} else {
				value = Settings.System.getInt(context.getContentResolver(),
						"screen_brightness_mode");
				return value == 1;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 设置屏幕是否为自动亮度调整
	 * 权限：&lt;uses-permission android:name="android.permission.WRITE_SETTINGS" /&gt;
	 * @param context  View运行的Context
	 * @param isOn     是否开启自动亮度调整
	 * @return  设置自动亮度调整是否成功
	 */
	public static boolean setAutoBrightness(Context context, boolean isOn) {
		int value = 0;
		String name = "";

		if (Build.VERSION.SDK_INT >= 8) {
			// Android2.2(Api level 8)
			if (isOn) {
				value = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
			} else {
				value = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
			}
			name = Settings.System.SCREEN_BRIGHTNESS_MODE;
		} else {
			if (isOn) {
				value = 1;
			} else {
				value = 0;
			}
			name = "screen_brightness_mode";
		}

		return Settings.System.putInt(context.getContentResolver(), name, value);
	}

	/**
	 * 获得当前屏幕亮度
	 * @param context  View运行的Context
	 * @return  当前屏幕亮度(0(最暗) -- 255(最亮))
	 */
	public static int getBrightness(Context context) {
		try {
			ContentResolver resolver = context.getContentResolver();
			return android.provider.Settings.System.getInt(resolver,
					Settings.System.SCREEN_BRIGHTNESS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 设置屏幕亮度
	 * 权限：&lt;uses-permission android:name="android.permission.WRITE_SETTINGS" /&gt;
	 * @param context  View运行的Context
	 * @param window   Activity当前运行的Window，如果指定为null的话，则只是修改系统属性，不会立即奏效
	 * @param brightness  屏幕亮度(0(最暗) -- 255(最亮))
	 * @return  设置屏幕亮度是否成功
	 */
	public static boolean setBrightness(Context context, Window window, int brightness) {
		if (brightness < 0 || brightness > 255) {
			return false;
		}
		ContentResolver resolver = context.getContentResolver();
		if (!android.provider.Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS,
				brightness)) {
			return false;
		}

		if (window != null) {
			WindowManager.LayoutParams lp = window.getAttributes();
			lp.screenBrightness = Float.valueOf(brightness / 255.0f);
			window.setAttributes(lp);
		}

		return true;
	}
	
	/**
	 * 设置屏幕亮度跟随系统
	 * 权限：&lt;uses-permission android:name="android.permission.WRITE_SETTINGS" /&gt;
	 * @param window   Activity当前运行的Window，如果指定为null的话，则只是修改系统属性，不会立即奏效
	 * @return  设置屏幕亮度是否成功
	 */
	public static boolean setBrightnessWithSystem(Window window) {
		if (window != null) {
			WindowManager.LayoutParams lp = window.getAttributes();
			lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
			window.setAttributes(lp);
		}
		return true;
	}
	
	/**
	 * 屏幕是否处于打开状态
	 * @param context  View运行的Context
	 * @return  屏幕是否处于打开状态
	 */
	public static boolean isScreenOn(Context context) {
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return powerManager.isScreenOn();
	}
}
