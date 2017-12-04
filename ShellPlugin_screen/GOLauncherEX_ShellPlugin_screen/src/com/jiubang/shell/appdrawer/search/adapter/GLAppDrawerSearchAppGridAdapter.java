package com.jiubang.shell.appdrawer.search.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.NinePatchDrawable;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.proxy.ApplicationProxy;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.manage.LruImageCache;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.FuncSearchResultItem;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.shell.appdrawer.component.GLAppDrawerAppIcon;
import com.jiubang.shell.appdrawer.search.GLSearchMediaGridIconInfo;
import com.jiubang.shell.common.component.GLModel3DMultiView;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.model.IModelState;
/**
 * 
 * @author dingzijian
 *
 */
public class GLAppDrawerSearchAppGridAdapter
		extends
			GLAppDrawerSearchBaseGridAdapter<FuncSearchResultItem> {
	private NinePatchDrawable mMediaIconBg;

	public GLAppDrawerSearchAppGridAdapter() {
		super();
		mMediaIconBg = (NinePatchDrawable) ShellAdmin.sShellManager.getContext().getResources()
				.getDrawable(R.drawable.gl_search_media_icon_bg);
	}

	@Override
	public GLView getView(int pos, GLView convertView, GLViewGroup parent) {
		FuncSearchResultItem resultItem = getItem(pos);
		GLAppDrawerAppIcon drawerAppIcon = (GLAppDrawerAppIcon) convertView;
		if (drawerAppIcon == null) {
			drawerAppIcon = (GLAppDrawerAppIcon) mInflater.inflate(
					R.layout.gl_appdrawer_allapp_icon, null);
		} 
			GLModel3DMultiView multiView = drawerAppIcon.getMultiView();
		switch (resultItem.mType) {
			case FuncSearchResultItem.ITEM_TYPE_LOCAL_HISTORY_APPS :
			case FuncSearchResultItem.ITEM_TYPE_LOCAL_APPS :
				cancleLoadMediaImage(drawerAppIcon);
				drawerAppIcon.setInfo(resultItem.getFunAppItemInfo());
//				drawerAppIcon.setTitle(resultItem.mTitle);
				drawerAppIcon.setCurrenState(IModelState.NO_STATE);
//				drawerAppIcon.setTitleMaxLines(1);
				return drawerAppIcon;
			case FuncSearchResultItem.ITEM_TYPE_APP_CENTER_APPS :
				cancleLoadMediaImage(drawerAppIcon);
				setIcon(pos, drawerAppIcon, resultItem.recApp.info.icon,
						LauncherEnv.Path.APP_MANAGER_ICON_PATH,
						String.valueOf(resultItem.recApp.info.icon.hashCode()));
				drawerAppIcon.setTitle(resultItem.recApp.info.name);
//				drawerAppIcon.setCurrenState(IModelState.STATE_DOWNLOAD, null);
//				drawerAppIcon.setTitleMaxLines(1);
				return drawerAppIcon;
			default :
				multiView.setBgVisible(true);
				multiView.setBgImageDrawable(mMediaIconBg);
				drawerAppIcon.setCurrenState(IModelState.NO_STATE);
				drawerAppIcon.setTitle(resultItem.fileInfo.fileName);
				loadThumbnail(drawerAppIcon, resultItem);
//				drawerAppIcon.setTitleMaxLines(1);
				return drawerAppIcon;
		}
	}
	/**
	 * 
	 * 加载缩略图
	 * @param type
	 * @param id
	 * @param filePath
	 * @param imgWidth
	 */
	private void loadThumbnail(final GLAppDrawerAppIcon appIcon,
			final FuncSearchResultItem resultItem) {
		GLSearchMediaGridIconInfo iconInfo = (GLSearchMediaGridIconInfo) appIcon
				.getTag(R.integer.search_media_icon_tag);
		if (iconInfo == null) {
			iconInfo = new GLSearchMediaGridIconInfo(appIcon);
			appIcon.setTag(R.integer.search_media_icon_tag, iconInfo);
		}
		iconInfo.setFileItemFileInfo(resultItem.fileInfo);
	}

	private void cancleLoadMediaImage(final GLAppDrawerAppIcon appIcon) {
		GLSearchMediaGridIconInfo iconInfo = (GLSearchMediaGridIconInfo) appIcon
				.getTag(R.integer.search_media_icon_tag);
		if (iconInfo != null) {
			iconInfo.cancelLoadThumbnail();
		}
	}

	private void setIcon(final int position, final GLAppDrawerAppIcon appIcon, String imgUrl,
			String imgPath, String imgName) {
		appIcon.setTag(imgUrl);
		Bitmap bm = AsyncImageManager.getLruInstance(LruImageCache.DEFAULT_SIZE).loadImageForList(
				position, imgPath, imgName, imgUrl, true, false,
				AppGameDrawUtils.getInstance().mMaskIconOperator, new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imgUrl.equals(appIcon.getTag())) {
							appIcon.setIcon(new BitmapDrawable(imageBitmap));
						}
					}
				});
		if (bm != null) {
			appIcon.setIcon(new BitmapDrawable(bm));
		} else {
			appIcon.setIcon((BitmapDrawable) ApplicationProxy.getContext().getResources()
					.getDrawable(com.gau.go.launcherex.R.drawable.default_icon));
		}
	}
}
