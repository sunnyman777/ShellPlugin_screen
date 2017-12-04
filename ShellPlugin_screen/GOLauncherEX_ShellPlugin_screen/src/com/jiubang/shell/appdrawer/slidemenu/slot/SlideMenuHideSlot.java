package com.jiubang.shell.appdrawer.slidemenu.slot;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.proxy.SettingProxy;
import com.go.util.file.media.ThumbnailManager;
import com.jiubang.ggheart.apps.appfunc.controler.AppConfigControler;
import com.jiubang.ggheart.apps.desks.appfunc.HideAppActivity;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.common.controler.InvokeLockControler;
import com.jiubang.ggheart.common.password.PasswordActivity.ActionResultCallBack;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 侧边栏隐藏功能块
 * @author wuziyi
 *
 */
public class SlideMenuHideSlot extends AbsSlideMenuSlot {

	@Override
	public int getFuntionNameResId() {
		return R.string.slide_menu_hide_title;
	}

	@Override
	public int getIconResId() {
		return R.drawable.gl_appdrawer_slide_menu_hide_app;
	}

	@Override
	public int getBackgroundResId() {
		return R.drawable.gl_appdrawer_slide_menu_hide_bg;
	}

	@Override
	public int getViewId() {
		return IViewId.HIDE_APP_MANAGE;
	}

	@Override
	public void showExtendFunctionView(GLView view, boolean needAnimation, Object...objs) {
//		super.showExtendFunctionView(view);
		// 这个也是有些特殊
		enterHideApp(view, needAnimation, objs);
		String opCode = "si_hide";
		GuiThemeStatistics.sideOpStaticData("-1", opCode, 1, "-1");
	}

	@Override
	public void hideExtendFunctionView(GLView view, boolean needAnimation, Object...objs) {
		super.hideExtendFunctionView(view, needAnimation, objs);

	}
	
	private void enterHideApp(final GLView view, final boolean needAnimation, Object...objs) {
		final Context context = ShellAdmin.sShellManager.getActivity().getApplicationContext();
		if (SettingProxy.getScreenSettingInfo().mLockScreen) { // 判断当前是否锁定编辑
			LockScreenHandler.showLockScreenNotification(context);
			return;
		}
//		List<AppItemInfo> list = AppConfigControler.getInstance(context).getHideApps();
		
		final Runnable runnable = new Runnable() {

			@Override
			public void run() {
				if (AppConfigControler.getInstance(context).isExistHideApp()) {
					SlideMenuHideSlot.super.showExtendFunctionView(view, needAnimation);
				} else {
					Intent hideIntent = new Intent(ShellAdmin.sShellManager.getActivity(),
							HideAppActivity.class);
					ShellAdmin.sShellManager.getShell().startActivitySafely(hideIntent);
				}
			}
		};
		boolean needShowLock = true;
		if (objs != null && objs.length > 0 && objs[0] instanceof Boolean) {
			needShowLock = (Boolean) objs[0];
		}
		if (SettingProxy.getDeskLockSettingInfo().mLockHideApp
				&& needShowLock) {
			BitmapDrawable drawable = (BitmapDrawable) view.getContext().getResources()
					.getDrawable(R.drawable.gl_hide_app_icon);
			String title = view.getContext().getString(R.string.menuitem_hide_tilt);
			InvokeLockControler.getInstance(context)
					.startLockAction(
							InvokeLockControler.ACTION_ID_LOCKHIDEAPP,
							new ActionResultCallBack() {

								@Override
								public void onUnlockSuccess(int actionId) {
									runnable.run();
								}

								@Override
								public void onUnlockFail(int actionId) {
								}
							},
							ShellAdmin.sShellManager.getActivity(),
							ThumbnailManager.getInstance(context).getParcelableBitmap(
									drawable.getBitmap()), title);
		} else {
			runnable.run();
		}
		
//		if (AppConfigControler.getInstance(context).isExistHideApp()) {
//			// 如果有隐藏程序数据
//			if (GoSettingControler.getInstance(context).getDeskLockSettingInfo().mLockHideApp) {
//				BitmapDrawable drawable = (BitmapDrawable) view.getContext()
//						.getResources().getDrawable(R.drawable.gl_hide_app_icon);
//				String title = view.getContext().getString(R.string.menuitem_hide_tilt);
//				InvokeLockControler.getInstance(context).startLockAction(
//						InvokeLockControler.ACTION_ID_LOCKHIDEAPP,
//						new ActionResultCallBack() {
//
//							@Override
//							public void onUnlockSuccess(int actionId) {
//								SlideMenuHideSlot.super.showExtendFunctionView(view, needAnimation);
//							}
//
//							@Override
//							public void onUnlockFail(int actionId) {
//							}
//						},
//						ShellAdmin.sShellManager.getActivity(),
//						ThumbnailManager.getInstance(context).getParcelableBitmap(
//								drawable.getBitmap()), title);
//			} else {
//				super.showExtendFunctionView(view, needAnimation);
//			}
//		} else {
//			Intent hideIntent = new Intent(ShellAdmin.sShellManager.getActivity(),
//					HideAppActivity.class);
//			ShellAdmin.sShellManager.getShell().startActivitySafely(hideIntent);
//		}
	}

}
