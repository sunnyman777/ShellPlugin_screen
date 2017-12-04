package com.jiubang.shell.screenedit.tabs;

import java.util.ArrayList;
import java.util.List;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import com.gau.go.gostaticsdk.StatisticsManager;
import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.gl.widget.GLImageView;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.util.AppUtils;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenAdvertMsgId;
import com.golauncher.message.IScreenEditMsgId;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.WallpaperControler;
import com.jiubang.ggheart.apps.desks.diy.filter.FilterActivity;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditController;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditStatistics;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.bean.WallpaperItemInfo;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.push.WallpaperOperation;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.components.advert.AdvertConstants;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.screenedit.GLWallpaperAdapter;

/**
 * 壁纸TAB
 * @author zouguiquan
 *
 */
public class GLWallpaperTab extends GLGridTab {

	private static final String LIVE_WALLPAPER_CLASS_NAME = "com.android.wallpaper.livepicker.LiveWallpaperListActivity";
	private static final String KAD_URL = "http://69.28.52.42:8090/recommendedapp/manage/appcallback.action?cburl=&ctype=0&pname=com.gau.go.launcher.lwp.core&uid=3818431973086579092&aid=624&from=golaunchermagAdv&mapid=5525561&corpid=2";

	public GLWallpaperTab(Context context, int tabId, int level) {
		super(context, tabId, level);
		mLoadDataDelay = 250;
		mNeedAsyncLoadImage = false;
	}

	@Override
	public ArrayList<Object> requestData() {
		if (mDataList != null && mDataList.size() > 0) {
			return mDataList;
		}

		return ScreenEditController.getInstance().getWallPaperTabData();
	}
	
	@Override
	public void refreshTitle() {
		ScreenSettingInfo screenInfo = SettingProxy.getScreenSettingInfo();
		MsgMgrProxy.sendHandler(this, IDiyFrameIds.SCREEN_EDIT,
				IScreenEditMsgId.SCREEN_EDIT_REFRESH_TITLE, mTabId, screenInfo.mWallpaperScroll);
	}

	@Override
	public void handleAppChanged(int msgId, String pkgName, boolean showing) {
		super.handleAppChanged(msgId, pkgName, showing);

		if (showing) {
			refreshData();
		}
	}

	@Override
	public ShellBaseAdapter<Object> createAdapter(Context mContext, List<Object> infoList) {
		return new GLWallpaperAdapter(mContext, infoList);
	}

//	@Override
//	public Bitmap onLoadImage(int index) {
//		Bitmap result = null;
//		WallpaperItemInfo dto = (WallpaperItemInfo) getItem(index);
//		if (dto == null) {
//			return null;
//		}
//
//		result = drawableToBitmap(dto.getAppIcon());
//
//		return result;
//	}

//	private Bitmap drawableToBitmap(Drawable drawable) {
//		int width = (int) (drawable.getIntrinsicWidth() * DrawUtils.sDensity);
//		int height = (int) (drawable.getIntrinsicHeight() * DrawUtils.sDensity);
//		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//		Canvas canvas = new Canvas(bitmap);
//		drawable.setBounds(0, 0, width, height);
//		drawable.draw(canvas);
//		return bitmap;
//	}

	/**
	 * 获取打开动态壁纸列表界面的Intent
	 * @return
	 */
	private Intent searchWallpaperListIntent() {
		Intent result = null;
		Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER, null);
		List<ResolveInfo> list = mContext.getPackageManager().queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		for (ResolveInfo resolveInfo : list) {
			if (resolveInfo.activityInfo == null) {
				continue;
			}

			String pkgName = resolveInfo.activityInfo.packageName;
			String className = resolveInfo.activityInfo.name;
			if (pkgName != null && pkgName.equals(WallpaperOperation.LIVE_WALLPAPER_PACKAGENAME)) {
				if (className != null) {
					result = new Intent();
					result.setComponent(new ComponentName(pkgName, className));
					return result;
				}
			}
		}
		return result;
	}

	@Override
	public void onItemClick(GLAdapterView<?> adapter, GLView view, int position, long arg3) {
		super.onItemClick(adapter, view, position, arg3);

		Object tag = view.getTag();
		if (tag == null || !(tag instanceof WallpaperItemInfo)) {
			return;
		}

		WallpaperItemInfo dto = (WallpaperItemInfo) tag;
		if (PackageName.PACKAGE_NAME.equals(dto.getPkgName())) {

			MsgMgrProxy
					.sendMessage(this, IDiyFrameIds.SCREEN_EDIT,
							IScreenEditMsgId.SCREEN_EDIT_CHANGE_TAB,
							ScreenEditConstants.TAB_ID_GOWALLPAPER);

			view.findViewById(R.id.screen_edit_item_new).setVisibility(View.GONE);
			//用户行为统计
			StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
					StatisticsData.USER_ACTION_FOUTEEN, IPreferencesIds.DESK_ACTION_DATA);

		} else if (ScreenEditController.GODYNAMICA_WALLPAPER.equals(dto.getPkgName())) {

			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_EDIT,
					IScreenEditMsgId.SCREEN_EDIT_CHANGE_TAB,
					ScreenEditConstants.TAB_ID_GODYNAMICA_WALLPAPER);

			StatisticsManager.getInstance(ApplicationProxy.getContext())
					.upLoadBasicOptionStaticData(ScreenEditStatistics.STATISTICS_FUN_ID, "0",
							ScreenEditStatistics.STATISTICS_OPERATE_MODULE_CLICK,
							ScreenEditStatistics.STATISTICS_OPERATE_SUCCESS,
							Statistics.getUid(m2DContext), ScreenEditStatistics.STATISTICS_ENTER,
							ScreenEditStatistics.STATISTICS_TYPE_DYNAMICA_WALLPAPER, -1);

		} else if (ScreenEditController.WALLPAPER_FILTER.equals(dto.getPkgName())) {

			ScreenEditController.isFirstShowScreenEditFilter(m2DContext, true);
			GLImageView newTag = (GLImageView) view.findViewById(R.id.screen_edit_item_new);
			if (newTag != null) {
				newTag.setVisibility(View.GONE);
			}

			if (WallpaperControler.getInstance().isLiveWallpaper()) {
				Toast.makeText(mContext, mContext.getString(R.string.wallpaper_filter_cannot_use),
						Toast.LENGTH_LONG).show();
			} else {
				Intent intent = new Intent(ShellAdmin.sShellManager.getActivity(),
						FilterActivity.class);
				intent.putExtra(FilterActivity.PAY_ENTRANCE_ID_INDEX, 0);
				ShellAdmin.sShellManager.getActivity().startActivity(intent);
			}
		} else if (PackageName.RECOMM_LIVEWALLPAPER_PKG_NAME.equals(dto.getPkgName())) {

			//推荐动态壁纸 update by caoyaming 2014-03-19 桌面推动态壁纸功能
			if (!GoAppUtils.isAppExist(mContext, dto.getPkgName())) {
				//上传点击统计,走15屏广告统计
				StatisticsData.updateAppClickData(mContext, dto.getPkgName(),
						AdvertConstants.ADVERT_STATISTICS_TYPE, "5525561", "624");

				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_ADVERT_BUSINESS,
						IScreenAdvertMsgId.REQUEST_ADVERT_STAT_CLICK_ACTION, -1, dto.getPkgName(),
						"624", KAD_URL, "5525561");

				//判断有没有安装推荐的动态壁纸,如果没有则跳转下载
				CheckApplication.downloadAppFromMarketGostoreDetail(mContext, dto.getPkgName(), "");
			} else {
				//打开该动态壁纸
				Intent intent = dto.getIntent();
				//如果SDK版本低于16,则重新获取Intent
				if (Build.VERSION.SDK_INT < 16) {
					intent = searchWallpaperListIntent();
					try {
						String toast = mContext
								.getString(R.string.set_dynamical_wallpaper_toast_prefix)
								+ dto.getmAppLabel()
								+ mContext.getString(R.string.set_dynamical_wallpaper_toast_suffix);
						DeskToast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
					}
				}
				try {
					//启动壁纸
					mContext.startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {

			if (dto.getPkgName() != null
					&& dto.getPkgName().equals(PackageName.MULTIPLEWALLPAPER_PKG_NAME)) {
				// TODO 这里是否需要用户行为统计？

				// 判断设备是否支持动态壁纸
				if (!isSupportedLiveWallpaper()) {
					// 不支持动态壁纸
					Toast.makeText(m2DContext,
							mContext.getText(R.string.not_support_live_wallpaper_toast),
							Toast.LENGTH_LONG).show();
				} else {
					if (!GoAppUtils.isAppExist(m2DContext, PackageName.MULTIPLEWALLPAPER_PKG_NAME)) {

						// 这里判断有没有安装多屏多壁纸，没有则跳转下载
						gotoDownloadMultipleWallpaper();
						// 先让桌面退出添加界面，恢复正常
						//							MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
						//									IDiyMsgIds.SCREEN_SMALL_TO_NORMAL, -1, null,
						//									null);

					} else {

						//							if (!GoSettingControler.getInstance(ApplicationProxy.getContext())
						//									.getScreenSettingInfo().mWallpaperScroll) {
						// 如果壁纸设置为竖屏模式，则弹Toast提示不可滚动
						//								Toast.makeText(
						//										mContext,
						//										mContext.getText(R.string.go_multiple_wallpaper_toast),
						//										Toast.LENGTH_LONG).show();
						//							}
						Intent intent = dto.getIntent();
						// 把壁纸模式传递给多屏多壁纸
						intent.putExtra("wallpaper_scroll",
								SettingProxy.getScreenSettingInfo().mWallpaperScroll);

						try {
							//								不能用2Dcontext启动
							ShellAdmin.sShellManager.getShell().startActivitySafely(intent);
							WallpaperControler.setWallpaperSetting(true);
						} catch (Exception e) {
						}
					}
				}

			} else {

				Intent intent = dto.getIntent();
				// 统计动态壁纸
				if (intent.getComponent().getClassName().equals(LIVE_WALLPAPER_CLASS_NAME)) {
					// 用户行为统计
					StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
							StatisticsData.USER_ACTION_THIRTEEN, IPreferencesIds.DESK_ACTION_DATA);
				} else {
					// 用户行为统计
					StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
							StatisticsData.USER_ACTION_FIFTEEN, IPreferencesIds.DESK_ACTION_DATA);
				}
				try {
					//不能用2d的context启动
					ShellAdmin.sShellManager.getShell().startActivitySafely(intent);
					//						m2DContext.startActivity(intent);
					WallpaperControler.setWallpaperSetting(true);
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	public Object requestTitleInfo() {
		ScreenSettingInfo screenInfo = SettingProxy.getScreenSettingInfo();
		return screenInfo.mWallpaperScroll;
	}

	// 以后做动态壁纸的设置用，勿删
	// ResolveInfo settingInfo = null;
	// WallpaperManager wm = (WallpaperManager)
	// mContext.getSystemService(Context.WALLPAPER_SERVICE);
	// WallpaperInfo wi = wm.getWallpaperInfo();
	//
	// if (wi != null && wi.getSettingsActivity() != null)
	// {
	// LabeledIntent li = new
	// LabeledIntent(mContext.getPackageName(),R.string.configure_wallpaper, 0);
	// li.setClassName(wi.getPackageName(), wi.getSettingsActivity());
	// settingInfo = pm.resolveActivity(li, 0);
	// }

	/**
	 * 跳转下载多屏多壁纸
	 */
	private void gotoDownloadMultipleWallpaper() {
		// String packageName = LauncherEnv.Plugin.MULTIPLEWALLPAPER_PKG_NAME;
		// TODO 由于Google电子市场还没有多屏多壁纸的包，这里用通讯统计的包测试，等有多屏多壁纸的包再修改
		String packageName = PackageName.MULTIPLEWALLPAPER_PKG_NAME;
		String url = LauncherEnv.Url.MULTIPLEWALLPAPER_URL;
		String linkArray[] = { packageName, url };
		String title = mContext.getString(R.string.go_multiple_wallpaper_title);
		String content = mContext.getString(R.string.go_multiple_wallpaper_tip_content);
		boolean isCnUser = GoAppUtils.isCnUser(mContext);

		CheckApplication.downloadAppFromMarketFTPGostore(m2DContext, content, linkArray,
				LauncherEnv.MULTIPLEWALLPAPER_GOOGLE_REFERRAL_LINK, title,
				System.currentTimeMillis(), isCnUser, CheckApplication.FROM_GO_FOLDER, 0, null);
	}

	/**
	 * 判断是否支持动态壁纸
	 * 
	 * @return
	 */
	private boolean isSupportedLiveWallpaper() {
		Intent i = new Intent();
		i.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
		return AppUtils.isAppExist(m2DContext, i);
	}
}
