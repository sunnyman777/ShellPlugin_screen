package com.jiubang.shell.appdrawer.search.adapter;

import android.util.Log;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.jiubang.ggheart.apps.desks.appfunc.model.FuncSearchResultItem;
import com.jiubang.shell.appdrawer.component.GLAppDrawerAppIcon;
import com.jiubang.shell.appdrawer.search.GLSearchMediaGridIconInfo;
/**
 * 
 * @author dingzijian
 *
 */
public class GLAppDrawerSearchMediaGridAdapter
		extends
			GLAppDrawerSearchBaseGridAdapter<FuncSearchResultItem> {

	@Override
	public GLView getView(int pos, GLView convert, GLViewGroup parennt) {
		FuncSearchResultItem resultItem = getItem(pos);
		GLAppDrawerAppIcon drawerAppIcon = (GLAppDrawerAppIcon) convert;
		if (drawerAppIcon == null) {
			Log.i("dzj", "new appIcon-------->" + resultItem.mTitle);
			drawerAppIcon = (GLAppDrawerAppIcon) mInflater.inflate(
					R.layout.gl_appdrawer_allapp_icon, null);
		} else {
			Log.i("dzj", "reuse appIcon-------->" + resultItem.mTitle);
		}
		//		GLModel3DMultiView multiView = drawerAppIcon.getMultiView();
		//		multiView.setBgVisible(true);
		//		multiView.setBgImageDrawable(mContext.getResources().getDrawable(
		//				R.drawable.gl_search_media_icon_bg));
		drawerAppIcon.setTitle(resultItem.fileInfo.fileName);
		loadThumbnail(drawerAppIcon, resultItem);
		return drawerAppIcon;
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
		GLSearchMediaGridIconInfo iconInfo = (GLSearchMediaGridIconInfo) appIcon.getTag();
		if (iconInfo == null) {
			iconInfo = new GLSearchMediaGridIconInfo(appIcon);
			appIcon.setTag(iconInfo);
		}
		iconInfo.setFileItemFileInfo(resultItem.fileInfo);
	}

}
