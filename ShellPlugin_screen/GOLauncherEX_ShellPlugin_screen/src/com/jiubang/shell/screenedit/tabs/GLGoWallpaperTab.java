package com.jiubang.shell.screenedit.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.go.commomidentify.IGoLauncherClassName;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.util.market.MarketConstant;
import com.go.util.window.WindowControl;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingUtils;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditController;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.bean.WallpaperSubInfo;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.screenedit.GLGoWallpaperAdapter;

/**
 * GO壁纸TAB
 * @author zouguiquan
 *
 */
public class GLGoWallpaperTab extends GLGridTab {

	//	private ArrayList<Object> mImages; 		// 墙纸应用大图
	private Map<String, List<Object>> mWallpaperData;

	public GLGoWallpaperTab(Context context, int tabId, int level) {
		super(context, tabId, level);
		mPreTabId = ScreenEditConstants.TAB_ID_WALLPAPER;
	}

	@Override
	public ArrayList<Object> requestData() {
		if (mDataList != null && mDataList.size() > 0) {
			return mDataList;
		}

		if (mWallpaperData == null || mWallpaperData.size() == 0) {
			mWallpaperData = ScreenEditController.getInstance().loadDrawables("wallpaperlist");
		}

		return (ArrayList<Object>) mWallpaperData.get("mThumbs");
	}

	@Override
	public void handleAppChanged(int msgId, String pkgName, boolean showing) {

		if (showing && IGoLauncherClassName.DEFAULT_THEME_PACKAGE_3_NEWER.equals(pkgName)) {
			super.handleAppChanged(msgId, pkgName, showing);

			if (mWallpaperData != null) {
				mWallpaperData.clear();
			}

			if (showing) {
				refreshData();
			}
		}
	}

	@Override
	public ShellBaseAdapter<Object> createAdapter(Context mContext, List<Object> infoList) {
		return new GLGoWallpaperAdapter(mContext, infoList, mImageLoader);
	}

	@Override
	public Bitmap onLoadImage(int index) {
		Bitmap result = null;

		WallpaperSubInfo imageItem = (WallpaperSubInfo) getItem(index);
		if (imageItem == null) {
			return null;
		}

		final Resources resources = imageItem.getResource();
		final int resId = imageItem.getImageResId();

		if (resources != null && resId > 0) {
			Drawable drawable = resources.getDrawable(resId);
			if (drawable != null) {
				result = mEditController.composeIconMaskBitmap(drawable, true, false);
			}
		}

		return result;
	}

	@Override
	public void onItemClick(GLAdapterView<?> adapter, GLView view, int position, long id) {
		super.onItemClick(adapter, view, position, id);

		Object tag = view.getTag();
		if (tag != null && tag instanceof String) {
			String str = (String) tag;
			if (str.equals("ui3.0")) {
				GoAppUtils.gotoMarket(m2DContext, MarketConstant.APP_DETAIL
						+ IGoLauncherClassName.DEFAULT_THEME_PACKAGE_3_NEWER);
				return;
			}
		}

		ArrayList<Object> images = (ArrayList<Object>) mWallpaperData.get("mImages");
		if (images == null) {
			return;
		}

		final WallpaperSubInfo imageItem = (WallpaperSubInfo) images.get(position);
		if (imageItem == null) {
			return;
		}

		//判断是否是付费主题，是否需要弹出toast，。提示付费
		boolean isCanUseTheme = DeskSettingUtils.isCanUseTheme(imageItem);
		if (!isCanUseTheme) {
			DeskSettingUtils.showNeedPayThemeToast();
			return;
		}

		if (imageItem.getType() == 0) {
			// 获取屏幕编辑底层
			// ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame)
			// GoLauncher
			// .getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
			// if (screenEditBoxFrame != null) {
			// screenEditBoxFrame.getTabView().setTap(BaseTab.TAB_WALLPAPER);
			// }
		} else {
			new Thread() {
				public void run() {
					boolean ret = WindowControl.setWallpaper(m2DContext, imageItem.getResource(),
							imageItem.getImageResId());
					if (!ret) {
						try {
							DeskToast
									.makeText(
											mContext,
											mContext.getString(com.gau.go.launcherex.R.string.set_wallpaper_error),
											Toast.LENGTH_SHORT).show();
						} catch (Exception e) {
							//e.printStackTrace();
						}
					}
				};
			}.start();
		}
	}

	@Override
	public void clear() {
		super.clear();
		if (mWallpaperData != null) {
			mWallpaperData.clear();
		}
	}
}
