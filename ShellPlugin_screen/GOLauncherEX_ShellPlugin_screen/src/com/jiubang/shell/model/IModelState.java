package com.jiubang.shell.model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.gau.golauncherex.plugin.shell.R;
import com.go.util.graphics.BitmapUtility;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.theme.GLAppDrawerThemeControler;

/**
 * 
 * <br>类描述:模型状态
 * <br>功能详细描述:
 * 
 * @author  liuheng
 * @date  [2012-9-7]
 */
public class IModelState {

	/***************** 无大小 *****************************/
	public static final int NO_SIZE_STATE = -2;
	
	/***************** 无状态 *****************************/
	public static final int NO_STATE = -1;

	/***************** 卸载状态 *****************************/
	public static final int UNINSTALL_STATE = 0;

	/***************** 杀进程状态 *****************************/
	public static final int KILL_STATE = 1;
	
	/***************** 更新状态 *****************************/
	public static final int UPDATE_STATE = 2;

	/***************** 加锁状态 *****************************/
	public static final int LOCK_STATE = 3;
	
	/***************** 新程序状态 *****************************/
	public static final int NEW_STATE = 4;
	
	/***************** 搜索中的未下载的图标*****************************/
	public static final int STATE_DOWNLOAD = 5;
	
	/***************** 状态的个数 *****************************/
	public static final int STATE_COUNT = 6;
	
	public static final int[] RESIDS = { R.drawable.gl_uninstall_selector,
			R.drawable.gl_kill_process_selector, R.drawable.gl_appdrawer_app_update,
			R.drawable.gl_appdrawer_promanage_lock_icon, R.drawable.gl_new,
			R.drawable.gl_search_icon_download };

	private static Drawable[] sDrawables;
	static {
		sDrawables = new Drawable[RESIDS.length];
	}

	public static Drawable getStateDrawable(int state, Object... objs) {
		Resources res = ShellAdmin.sShellManager.getContext().getResources();
		// 可点击的小标志不能放在下面的缓存区，否则点击标志的时候，所有带标志的图标都会发生被点击的改变
		if (state == UNINSTALL_STATE || state == KILL_STATE || state == UPDATE_STATE) {
			return res.getDrawable(RESIDS[state]);
		} else if (state == STATE_COUNT) {
			Context context = ShellAdmin.sShellManager.getContext();
			int fontSize = context.getResources().getDimensionPixelSize(
					R.dimen.dock_notify_font_size);
			if (objs != null && objs.length > 0 && objs[0] instanceof Integer) {
				String countString = String.valueOf((Integer) objs[0]);
				int drawableRes = R.drawable.gl_stat_notify_no_nine;
				if (countString != null && countString.length() > 2) {
					drawableRes = R.drawable.gl_stat_notify;
				}
				int padding = context.getResources().getDimensionPixelSize(
						R.dimen.gl_notify_padding);
				return BitmapUtility.composeDrawableTextExpend(
						ShellAdmin.sShellManager.getActivity(), context.getResources()
								.getDrawable(drawableRes),
								countString, fontSize, padding);
			}
			return null;
		} else {
			if (sDrawables[state] == null) {
				if (state == NEW_STATE) {
					GLAppDrawerThemeControler themeCtrl = GLAppDrawerThemeControler.getInstance(ShellAdmin.sShellManager.getActivity());
					sDrawables[state] = themeCtrl.getDrawable(themeCtrl.getThemeBean().mAppIconBean.mNewApp, false, RESIDS[state]);
				} else {
					sDrawables[state] = res.getDrawable(RESIDS[state]);
				}
			}
			return sDrawables[state];
		}
	}
}
