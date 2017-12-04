package com.jiubang.shell.system.setting;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;

/**
 * 用于获取、设置系统音量相关的类。</br>
 * 提供了用户获取当前音量、最大音量、设置当前音量的方法。</br>
 * 方法中类型参数可以为如下之一：
 *      AudioManager.STREAM_VOICE_CALL    语音通话音量
 *      AudioManager.STREAM_SYSTEM        系统音量
 *      AudioManager.STREAM_RING          来电铃声音量
 *      AudioManager.STREAM_MUSIC         媒体音量
 *      AudioManager.STREAM_ALARM         闹钟音量
 *      AudioManager.STREAM_NOTIFICATION  通知音量
 *      AudioManager.STREAM_DTMF          双音多频音量
 * @author huchao
 *
 */
public class VolumeInfo {
	/**
	 * 获得音量大小
	 * @param context  View运行的Context
	 * @param type  音量类型，可见类说明处各个值
	 * @return 当前指定音量大小，获取失败则返回-1
	 */
	public static int getCurrent(Context context, int type) {
		if (!checkType(type)) {
			return -1;
		}

		AudioManager manager = (AudioManager) context.getSystemService(Activity.AUDIO_SERVICE);
		return manager.getStreamVolume(type);
	}

	/**
	 * 获得最大音量大小
	 * @param context  View运行的Context
	 * @param type  音量类型，可见类说明处各个值
	 * @return  指定的最大音量大小，获取失败则返回-1
	 */
	public static int getMax(Context context, int type) {
		if (!checkType(type)) {
			return -1;
		}

		AudioManager manager = (AudioManager) context.getSystemService(Activity.AUDIO_SERVICE);
		return manager.getStreamMaxVolume(type);
	}

	/**
	 * 设置当前音量大小
	 * @param context  View运行的Context
	 * @param type  音量类型，可见类说明处各个值
	 * @param volume  音量大小(必须在0 -- getMax()范围内，否则直接返回false)
	 * @param flag    音量改变时，系统需要作出的回应，可以为如下类型的或集合：
	 *      AudioManager.FLAG_ALLOW_RINGER_MODES        Whether to include ringer modes as possible options when changing volume.
	 *      AudioManager.FLAG_PLAY_SOUND                Whether to play a sound when changing the volume.
	 *      AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE  Removes any sounds/vibrate that may be in the queue, or are playing (related to changing volume).
	 *      AudioManager.FLAG_SHOW_UI                   Show a toast containing the current volume.
	 *      AudioManager.FLAG_VIBRATE                   Whether to vibrate if going into the vibrate ringer mode.
	 * @return  设置音量是否成功
	 */
	public static boolean setCurrent(Context context, int type, int volume, int flag) {
		if (!checkType(type)) {
			return false;
		}

		if (volume < 0 || volume > getMax(context, type)) {
			return false;
		}

		if (!checkFlag(flag)) {
			return false;
		}

		AudioManager manager = (AudioManager) context.getSystemService(Activity.AUDIO_SERVICE);
		manager.setStreamVolume(type, volume, flag);
		return true;
	}

	/**
	 * 判断当前铃声是否为打开状态(注意：判断是以系统设置为准，如果系统中设置铃声为打开状态，则铃声音量为0也算打开状态)
	 * @param context  View运行的Context
	 * @return  当前铃声是否为打开状态
	 */
	public static boolean isRingerOn(Context context) {
		AudioManager manager = (AudioManager) context.getSystemService(Activity.AUDIO_SERVICE);
		int ringerMode = manager.getRingerMode();
		if (ringerMode == AudioManager.RINGER_MODE_SILENT
				|| ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
			return false;
		}
		return true;
	}

	/**
	 * 判断当前震动是否为打开状态
	 * @param context  View运行的Context
	 * @return  当前震动是否为打开状态
	 */
	public static boolean isVibrateOn(Context context) {
		AudioManager manager = (AudioManager) context.getSystemService(Activity.AUDIO_SERVICE);
		int ringerMode = manager.getRingerMode();
		if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
			return true;
		}
		if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
			return false;
		}
		// ringerMode == AudioManager.RINGER_MODE_NORMAL
		if (manager.getVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION) == AudioManager.VIBRATE_SETTING_ON
				&& manager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER) == AudioManager.VIBRATE_SETTING_ON) {
			return true;
		}
		return false;
	}

	/**
	 * 设置当前为有铃声有震动模式
	 * @param context  Context
	 * @return  设置是否成功
	 */
	public static boolean setHasRingerHasVibrate(Context context) {
		AudioManager manager = (AudioManager) context.getSystemService(Activity.AUDIO_SERVICE);
		manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

		manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
				AudioManager.VIBRATE_SETTING_ON);
		manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
		return true;
	}

	/**
	 * 设置当前为有铃声无振动模式
	 * @param context  Context
	 * @return  设置是否成功
	 */
	public static boolean setHasRingerNotVibrate(Context context) {
		AudioManager manager = (AudioManager) context.getSystemService(Activity.AUDIO_SERVICE);
		manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

		manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
				AudioManager.VIBRATE_SETTING_OFF);
		manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
				AudioManager.VIBRATE_SETTING_OFF);
		return true;
	}

	/**
	 * 设置当前为无铃声有震动模式
	 * @param context  Context
	 * @return  设置是否成功
	 */
	public static boolean setNotRingerHasVibrate(Context context) {
		AudioManager manager = (AudioManager) context.getSystemService(Activity.AUDIO_SERVICE);
		manager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

		manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
				AudioManager.VIBRATE_SETTING_ON);
		manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
		return true;
	}

	/**
	 * 设置当前为无铃声无振动模式
	 * @param context  Context
	 * @return  设置是否成功
	 */
	public static boolean setNotRingerNotVibrate(Context context) {
		AudioManager manager = (AudioManager) context.getSystemService(Activity.AUDIO_SERVICE);
		manager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

		manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
				AudioManager.VIBRATE_SETTING_OFF);
		manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
				AudioManager.VIBRATE_SETTING_OFF);
		return true;
	}

	/**
	 * 判断音量类型是否是已定义的音量类型
	 * @param type	音量类型
	 * @return
	 */
	private static boolean checkType(int type) {
		int[] streamTypes = { AudioManager.STREAM_VOICE_CALL, AudioManager.STREAM_SYSTEM,
				AudioManager.STREAM_RING, AudioManager.STREAM_MUSIC, AudioManager.STREAM_ALARM,
				AudioManager.STREAM_NOTIFICATION, AudioManager.STREAM_DTMF };
		for (int stream : streamTypes) {
			if (stream == type) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断音量改变时，系统需要作出的回应的标志是否已定义的类型
	 * @param flag	回应标志
	 * @return
	 */
	private static boolean checkFlag(int flag) {
		int total = AudioManager.FLAG_ALLOW_RINGER_MODES | AudioManager.FLAG_PLAY_SOUND
				| AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_SHOW_UI
				| AudioManager.FLAG_VIBRATE;
		if ((flag & (~total)) == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 设置系统铃声和震动状态
	 * @param silent 静音／非静音
	 * @param vibrate 震动／非震动
	 * @return
	 */
	public static boolean setRinerAndVibrate(Context context, boolean silent, boolean vibrate) {
		AudioManager manager = (AudioManager) context.getSystemService(Activity.AUDIO_SERVICE);
		int ringerMode = manager.getRingerMode();
		int vibrateRingerSetting = manager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
		if (silent) {
			if (vibrate) { //静音＆震动
				manager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
				manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
						AudioManager.VIBRATE_SETTING_ON);
				manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
						AudioManager.VIBRATE_SETTING_ON);
			} else { //静音＆非震动
				manager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
						AudioManager.VIBRATE_SETTING_ON);
				manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
						AudioManager.VIBRATE_SETTING_ON);
			}
		} else {
			if (vibrate) { //非静音＆震动
				manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
						AudioManager.VIBRATE_SETTING_ON);
				manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
						AudioManager.VIBRATE_SETTING_ON);
			} else { //非静音＆非震动
				if (ringerMode == AudioManager.RINGER_MODE_SILENT
						&& vibrateRingerSetting == AudioManager.VIBRATE_SETTING_OFF
						|| ringerMode == AudioManager.RINGER_MODE_SILENT
						&& vibrateRingerSetting == AudioManager.VIBRATE_SETTING_ON
						|| ringerMode == AudioManager.RINGER_MODE_NORMAL
						&& vibrateRingerSetting == AudioManager.VIBRATE_SETTING_OFF
						|| ringerMode == AudioManager.RINGER_MODE_NORMAL
						&& vibrateRingerSetting == AudioManager.VIBRATE_SETTING_ON
						|| ringerMode == AudioManager.RINGER_MODE_VIBRATE
						&& vibrateRingerSetting == AudioManager.VIBRATE_SETTING_OFF) {
					manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
							AudioManager.VIBRATE_SETTING_OFF);
					manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
							AudioManager.VIBRATE_SETTING_OFF);
				} else if (ringerMode == AudioManager.RINGER_MODE_VIBRATE
						&& vibrateRingerSetting == AudioManager.VIBRATE_SETTING_ON
						|| ringerMode == AudioManager.RINGER_MODE_VIBRATE
						&& vibrateRingerSetting == AudioManager.VIBRATE_SETTING_ONLY_SILENT
						|| ringerMode == AudioManager.RINGER_MODE_NORMAL
						&& vibrateRingerSetting == AudioManager.VIBRATE_SETTING_ON
						|| ringerMode == AudioManager.RINGER_MODE_NORMAL
						&& vibrateRingerSetting == AudioManager.VIBRATE_SETTING_ONLY_SILENT
						|| ringerMode == AudioManager.RINGER_MODE_SILENT
						&& vibrateRingerSetting == AudioManager.VIBRATE_SETTING_ONLY_SILENT) {
					manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
							AudioManager.VIBRATE_SETTING_ONLY_SILENT);
					manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
							AudioManager.VIBRATE_SETTING_ONLY_SILENT);
				}
			}
		}

		return true;
	}
}
