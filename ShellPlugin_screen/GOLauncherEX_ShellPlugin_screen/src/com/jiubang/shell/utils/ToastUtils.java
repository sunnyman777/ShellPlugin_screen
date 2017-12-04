package com.jiubang.shell.utils;

import android.widget.Toast;

import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author yangguanxiang
 *
 */
public class ToastUtils {

	public static void showToast(int resId, int duration) {
		Toast.makeText(ShellAdmin.sShellManager.getActivity(),
				ShellAdmin.sShellManager.getContext().getString(resId), duration).show();
	}

	public static void showToast(String text, int duration) {
		Toast.makeText(ShellAdmin.sShellManager.getActivity(), text, duration).show();
	}
}
