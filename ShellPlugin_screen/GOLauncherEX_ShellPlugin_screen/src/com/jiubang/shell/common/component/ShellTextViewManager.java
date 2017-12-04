package com.jiubang.shell.common.component;

import java.util.HashSet;

import android.graphics.Typeface;

import com.jiubang.ggheart.apps.font.FontBean;
import com.jiubang.ggheart.data.DataType;

/**
 * 
 * @author yangguanxiang
 *
 */
public class ShellTextViewManager {
	private static HashSet<ShellTextStatusListener> sTextStatusListener = new HashSet<ShellTextStatusListener>();

	public synchronized static void registerListener(ShellTextStatusListener listener) {
		if (!sTextStatusListener.contains(listener)) {
			sTextStatusListener.add(listener);
		}
	}

	public synchronized static void unregisterListener(ShellTextStatusListener listener) {
		if (sTextStatusListener.contains(listener)) {
			sTextStatusListener.remove(listener);
		}
	}

	public synchronized static boolean notify(int dataType, Object... params) {
		if (dataType == DataType.DATATYPE_DESKFONTCHANGED) {
			if (params[0] instanceof FontBean) {
				FontBean bean = (FontBean) params[0];
				for (ShellTextStatusListener listener : sTextStatusListener) {
					if (listener != null) {
						listener.onFontTypeChanged(bean.mFontTypeface, bean.mFontStyle);
					}
				}
			}
			return true;
		}
		return false;
	}

	public void cleanup() {
		sTextStatusListener.clear();
	}

	/**
	 * 
	 * @author yangguanxiang
	 *
	 */
	public static interface ShellTextStatusListener {
		public void onFontTypeChanged(Typeface tf, int style);
	}
}
