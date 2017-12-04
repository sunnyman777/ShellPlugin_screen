package com.jiubang.shell.model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import com.gau.golauncherex.plugin.shell.R;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.ICommonMsgId;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.AppFuncBaseThemeBean;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.theme.GLAppDrawerThemeControler;
import com.jiubang.shell.utils.GLImageUtil;

/**
 * 公用图片资源管理类
 */
public class CommonImageManager {
	private static CommonImageManager sInstance;
	public static final int RES_FOLDER_BG = 0;
	public static final int RES_FOLDER_CLOSE_COVER = 1;
	public static final int RES_FOLDER_OPEN_COVER = 2;
	public static final int RES_FOLDER_ACTION_BAR_ICON_NORMAL_FOLDER_BG = 3;
	public static final int RES_FOLDER_ACTION_BAR_ICON_NEW_FOLLER_BG = 4;

	public static final int RES_ACTION_BAR_NEXTPAGE_V = 5;
	public static final int RES_ACTION_BAR_NEXTPAGE_H = 6;
	public static final int RES_ACTION_BAR_PREPAGE_V = 7;
	public static final int RES_ACTION_BAR_PREPAGE_H = 8;
	public static final int RES_ACTION_BAR_CROSSOVER = 9;

	public static final int RES_APPDRAWER_PREVIEW_ICON_BG = 10;
	public static final int RES_APPDRAWER_PREVIEW_ICON_PRESSED_BG = 11;
	public static final int RES_APPDRAWER_PREVIEW_ICON_FULL_BG = 12;
	public static final int RES_APPDRAWER_PREVIEW_ICON_ADDSCREEN_BG = 13;
	
	private SparseArray<Drawable> mCache = new SparseArray<Drawable>();

	private Context mContext;

	private GLAppDrawerThemeControler mThemeCtrl;

	public static void buildInstance(Context context) {
		if (sInstance == null) {
			sInstance = new CommonImageManager(context);
		}
	}

	public static CommonImageManager getInstance() {
		if (sInstance == null) {
			buildInstance(ShellAdmin.sShellManager.getContext());
		}
		return sInstance;
	}

	private CommonImageManager(Context context) {
		mContext = context;
		mThemeCtrl = GLAppDrawerThemeControler.getInstance(mContext);
	}

	private Drawable onDemandResource(int type) {
		Drawable drawable = mCache.get(type);
		if (drawable != null) {
			return drawable;
		} else {
			String folderThemePkg = ThemeManager.getInstance(ApplicationProxy.getContext())
					.getScreenStyleSettingInfo().getFolderStyle();
			//loadFolderResource(folderThemePkg);
			//Drawable drawable = null;
			Resources res = mContext.getResources();
			switch (type) {
				case RES_FOLDER_ACTION_BAR_ICON_NORMAL_FOLDER_BG :
					drawable = res.getDrawable(R.drawable.gl_appdrawer_folder_action_bar_folder_bg);
					mCache.put(RES_FOLDER_ACTION_BAR_ICON_NORMAL_FOLDER_BG, drawable);
					break;
				case RES_FOLDER_ACTION_BAR_ICON_NEW_FOLLER_BG :
					drawable = GLImageUtil
							.getGLDrawable(R.drawable.gl_appdrawer_folder_action_bar_new_folder_bg);
					mCache.put(RES_FOLDER_ACTION_BAR_ICON_NEW_FOLLER_BG, drawable);
					break;
				case RES_ACTION_BAR_NEXTPAGE_V :
					drawable = res.getDrawable(R.drawable.gl_appdrawer_actionbar_nextpage_label_v);
					mCache.put(RES_ACTION_BAR_NEXTPAGE_V, drawable);
					break;
				case RES_ACTION_BAR_NEXTPAGE_H :
					drawable = res.getDrawable(R.drawable.gl_appdrawer_actionbar_nextpage_label_h);
					mCache.put(RES_ACTION_BAR_NEXTPAGE_H, drawable);
					break;
				case RES_ACTION_BAR_PREPAGE_V :
					drawable = res.getDrawable(R.drawable.gl_appdrawer_actionbar_prepage_label_v);
					mCache.put(RES_ACTION_BAR_PREPAGE_V, drawable);
					break;
				case RES_ACTION_BAR_PREPAGE_H :
					drawable = res.getDrawable(R.drawable.gl_appdrawer_actionbar_prepage_label_h);
					mCache.put(RES_ACTION_BAR_PREPAGE_H, drawable);
					break;
				case RES_ACTION_BAR_CROSSOVER :
					drawable = res.getDrawable(R.drawable.gl_appdrawer_actionbar_crossover);
					mCache.put(RES_ACTION_BAR_CROSSOVER, drawable);
					break;
				case RES_APPDRAWER_PREVIEW_ICON_BG :
					drawable = res.getDrawable(R.drawable.gl_appdrawer_preview_icon_bg);
					mCache.put(RES_APPDRAWER_PREVIEW_ICON_BG, drawable);
					break;
				case RES_APPDRAWER_PREVIEW_ICON_PRESSED_BG :
					drawable = res.getDrawable(R.drawable.gl_appdrawer_preview_icon_pressed_bg);
					mCache.put(RES_APPDRAWER_PREVIEW_ICON_PRESSED_BG, drawable);
					break;
				case RES_APPDRAWER_PREVIEW_ICON_FULL_BG :
					drawable = res.getDrawable(R.drawable.gl_appdrawer_preview_icon_full_bg);
					mCache.put(RES_APPDRAWER_PREVIEW_ICON_FULL_BG, drawable);
					break;
				case RES_APPDRAWER_PREVIEW_ICON_ADDSCREEN_BG:
					drawable = res.getDrawable(R.drawable.gl_appdrawer_preview_add_icon_bg);
					mCache.put(RES_APPDRAWER_PREVIEW_ICON_ADDSCREEN_BG, drawable);
					break;
			}

			//the following are the folder resources

			AppFuncBaseThemeBean themeBean = null;
			if (folderThemePkg == null) {
				themeBean = mThemeCtrl.getThemeBean();
			} else {
				themeBean = mThemeCtrl.getThemeBean(folderThemePkg);
			}
			switch (type) {
				case RES_FOLDER_BG :
					drawable = mThemeCtrl.getDrawable(
							themeBean.mFoldericonBean.mFolderIconBottomPath, folderThemePkg,
							R.drawable.gl_default_folder_bg);
					mCache.put(RES_FOLDER_BG, drawable);
					break;
				case RES_FOLDER_CLOSE_COVER :
					drawable = mThemeCtrl.getDrawable(
							themeBean.mFoldericonBean.mFolderIconTopClosedPath, folderThemePkg,
							R.drawable.gl_default_folder_top);
					mCache.put(RES_FOLDER_CLOSE_COVER, drawable);
					break;
				case RES_FOLDER_OPEN_COVER :
					drawable = mThemeCtrl.getDrawable(
							themeBean.mFoldericonBean.mFolderIconTopOpenPath, folderThemePkg,
							R.drawable.gl_default_folder_open_top);
					mCache.put(RES_FOLDER_OPEN_COVER, drawable);
					break;
			}
		}
		//reload the resource after init
		drawable = mCache.get(type);
		return drawable;
	}

	/**
	 * 加载文件夹相关的资源
	 */
	private void loadFolderResource(String packageName) {
		AppFuncBaseThemeBean themeBean = null;
		if (packageName == null) {
			themeBean = mThemeCtrl.getThemeBean();
		} else {
			themeBean = mThemeCtrl.getThemeBean(packageName);
		}
		Drawable drawable = null;
		drawable = mThemeCtrl.getDrawable(themeBean.mFoldericonBean.mFolderIconBottomPath,
				packageName, R.drawable.gl_default_folder_bg);
		mCache.put(RES_FOLDER_BG, drawable);

		drawable = mThemeCtrl.getDrawable(themeBean.mFoldericonBean.mFolderIconTopClosedPath,
				packageName, R.drawable.gl_default_folder_top);
		mCache.put(RES_FOLDER_CLOSE_COVER, drawable);

		drawable = mThemeCtrl.getDrawable(themeBean.mFoldericonBean.mFolderIconTopOpenPath,
				packageName, R.drawable.gl_default_folder_open_top);
		mCache.put(RES_FOLDER_OPEN_COVER, drawable);
	}

	public Drawable getDrawable(int type) {
		return onDemandResource(type);
	}

	public void reloadFolderResource() {
		String folderThemePkg = ThemeManager.getInstance(ApplicationProxy.getContext())
				.getScreenStyleSettingInfo().getFolderStyle();
		mThemeCtrl.parseFolderTheme(folderThemePkg);
		loadFolderResource(folderThemePkg);
		//		broadCast(IScreenFrameMsgId.COMMON_IMAGE_CHANGED, -1, null, null);
		MsgMgrProxy.sendBroadcast(this, ICommonMsgId.COMMON_IMAGE_CHANGED, -1);
	}
}
