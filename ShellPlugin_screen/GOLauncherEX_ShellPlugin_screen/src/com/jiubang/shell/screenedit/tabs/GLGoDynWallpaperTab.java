package com.jiubang.shell.screenedit.tabs;

import java.util.ArrayList;
import java.util.List;

import android.app.WallpaperInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.gau.go.gostaticsdk.StatisticsManager;
import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenFrameMsgId;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditStatistics;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.push.bean.PushInfo;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.push.util.ScreenEditPushConstants;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.push.util.ScreenEditPushController;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.screenedit.GLGoDynWallpaperAdapter;
import com.jiubang.shell.screenedit.GLWallpaperOperation;
import com.jiubang.shell.utils.ToastUtils;

/**
 * GO动态壁纸TAB
 * @author zouguiquan
 *
 */
public class GLGoDynWallpaperTab extends GLGridTab {

	private ScreenEditPushController mPushController;
	private GLWallpaperOperation mWallpaperOperation;

	public GLGoDynWallpaperTab(Context context, int tabId, int level) {
		super(context, tabId, level);
		mPreTabId = ScreenEditConstants.TAB_ID_WALLPAPER;

		mPushController = new ScreenEditPushController(m2DContext);
		mWallpaperOperation = new GLWallpaperOperation(context,
				ScreenEditPushConstants.REQUEST_ID_FOR_WALLPAPER);
	}

	@Override
	public ArrayList<Object> requestData() {
		ArrayList<Object> pushList = mWallpaperOperation.loadDynamicaWallpaper();
		mWallpaperOperation.saveCurrentPushApp();

		if (mPushController.checkToRequset(ScreenEditPushConstants.sWallpaperLastRequestTime)) {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.REQUEST_SCREEN_EDIT_PUSH_LIST, -1,
					ScreenEditPushConstants.REQUEST_ID_FOR_WALLPAPER,
					ScreenEditPushConstants.sWallpaperLastRequestTime);
		}

		return pushList;
	}
	
	@Override
	protected void onLoadDataStart() {
		super.onLoadDataStart();
		if (needChangeAnim()) {
			mBaseGrid.setVisibility(GLView.INVISIBLE);
		}
	}

	/**
	 * 因为数据是８小时向服务器请求一次，有可以两次点进来的时候，数据已经发生变化
	 * 所以为保证数据正确，每次退出时都清一下数据
	 */
	@Override
	public boolean onKeyBack() {
		boolean result = super.onKeyBack();

		if (mImageLoader != null) {
			mImageLoader.clear();
		}
		if (mDataList != null) {
			mDataList.clear();
		}

		return result;
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
		return new GLGoDynWallpaperAdapter(mContext, infoList, mImageLoader);
	}

	@Override
	public Bitmap onLoadImage(int position) {
		Bitmap bitmap = null;

		if (position >= getItemCount()) {
			return null;
		}

		PushInfo pushInfo = (PushInfo) getItem(position);
		if (pushInfo == null) {
			return null;
		}

		if (pushInfo.isHasInstall()) {

			Drawable drawable = mPushController.getAppIcon(pushInfo.getPackageName());
			bitmap = mEditController.composeIconMaskBitmap(drawable, true, false);
		} else {

			Bitmap b = null;
			if (pushInfo.getIconResPath() > 0) {
				Resources resources = mContext.getResources();
				b = BitmapFactory.decodeResource(resources, pushInfo.getIconResPath());
			} else {
				b = mPushController.loadImage(LauncherEnv.Path.SCREEN_EDIT_PUSH_CACHEICON_PATH,
						pushInfo.getPackageName() + ".png", pushInfo.getIconDownloadPath());
			}

			bitmap = mEditController.composeIconMaskBitmap(new BitmapDrawable(b), true, true);
		}

		return bitmap;
	}

	@Override
	public void onItemClick(GLAdapterView<?> adapter, GLView view, int position, long id) {
		super.onItemClick(adapter, view, position, id);

		PushInfo pushInfo = (PushInfo) view.getTag();
		if (pushInfo == null) {
			return;
		}

		if (!pushInfo.isHasInstall()) {
			String pkgName = pushInfo.getPackageName();
			String title = pushInfo.getName();
			String content = mContext.getString(R.string.fav_app);
			String[] linkArray = new String[] { pkgName, pushInfo.getDownloadurl() };
			String gaLink = LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK;
			CheckApplication.downloadAppFromMarketFTPGostore(m2DContext, content, linkArray,
					gaLink, title, System.currentTimeMillis(), GoAppUtils.isCnUser(m2DContext),
					CheckApplication.FROM_SCREENEDIT, 0, null);

			if (ScreenEditStatistics.sStatisticDebug) {
				StatisticsManager.getInstance(ApplicationProxy.getContext()).setDebugMode();
			}
			StatisticsManager.getInstance(ApplicationProxy.getContext())
					.upLoadBasicOptionStaticData(ScreenEditStatistics.STATISTICS_FUN_ID,
							pushInfo.getAppId(),
							ScreenEditStatistics.STATISTICS_OPERATE_RECOMMEND_CLICK,
							ScreenEditStatistics.STATISTICS_OPERATE_SUCCESS,
							Statistics.getUid(m2DContext), ScreenEditStatistics.STATISTICS_ENTER,
							ScreenEditStatistics.STATISTICS_TYPE_DYNAMICA_WALLPAPER, -1);
		} else {

			Intent intent = null;

			if (Build.VERSION.SDK_INT < 16) {
				intent = mWallpaperOperation.getWallpaperListIntent();

				if (intent != null) {
					String toast = mContext
							.getString(R.string.set_dynamical_wallpaper_toast_prefix)
							+ pushInfo.getName()
							+ mContext.getString(R.string.set_dynamical_wallpaper_toast_suffix);
					ToastUtils.showToast(toast, Toast.LENGTH_SHORT);
				}

			} else {
				WallpaperInfo wallpaperInfo = pushInfo.getWallpaperInfo();

				if (wallpaperInfo != null) {
					intent = new Intent();
					ComponentName componentName = new ComponentName(wallpaperInfo.getPackageName(),
							wallpaperInfo.getServiceName());
					intent.setAction("android.service.wallpaper.CHANGE_LIVE_WALLPAPER");
					intent.putExtra("android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT",
							componentName);
				}
			}

			if (intent != null) {
				m2DContext.startActivity(intent);
			}

			if (ScreenEditStatistics.sStatisticDebug) {
				StatisticsManager.getInstance(ApplicationProxy.getContext()).setDebugMode();
			}
			StatisticsManager.getInstance(ApplicationProxy.getContext())
					.upLoadBasicOptionStaticData(ScreenEditStatistics.STATISTICS_FUN_ID,
							pushInfo.getPackageName(),
							ScreenEditStatistics.STATISTICS_OPERATE_APP_CLICK,
							ScreenEditStatistics.STATISTICS_OPERATE_SUCCESS,
							Statistics.getUid(m2DContext), ScreenEditStatistics.STATISTICS_ENTER,
							ScreenEditStatistics.STATISTICS_TYPE_DYNAMICA_WALLPAPER, -1);
		}
	}

	public void log(String content) {
		if (true) {
			Log.d("widgetpush", content);
		}
	}
}
