package com.jiubang.shell.folder.adapter;

import java.util.List;

import android.content.Context;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.shell.appdrawer.adapter.GLGridBaseAdapter;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.folder.BaseFolderIcon;
import com.jiubang.shell.folder.GLAppFolderBaseGridView;
import com.jiubang.shell.screen.component.GLScreenShortCutIcon;
import com.jiubang.shell.screen.component.GLScreenShortCutIcon.TitleChangeListener;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-3-11]
 */
public class GLScreenFolderAdapter extends GLGridBaseAdapter<ShortCutInfo>
		implements
			TitleChangeListener {
	private GLAppFolderBaseGridView<BaseFolderIcon<?>> mBaseGridView;
	public GLScreenFolderAdapter(Context context, List infoList) {
		super(context, infoList);

	}
	@Override
	public GLView getView(int position, GLView convertView, GLViewGroup parent) {
		mBaseGridView = (GLAppFolderBaseGridView<BaseFolderIcon<?>>) parent;
		ShortCutInfo info = getItem(position);
		convertView = getViewByItem(info);
		IconView<ShortCutInfo> icon = null;
		if (convertView != null) {
			mCacheMap.put(ConvertUtils.intentToString(info.mIntent), convertView);
			icon = (IconView<ShortCutInfo>) convertView;
			if (info == icon.getInfo()) {
				return convertView;
			}
		} else if (info instanceof ShortCutInfo) {
			icon = (IconView<ShortCutInfo>) createView((ShortCutInfo) info);
		}
		if (icon != null) {
			icon.setInfo(info);
			mCacheMap.put(ConvertUtils.intentToString(info.mIntent), icon);
		}
		return icon;
	}
	@Override
	public GLView getViewByItem(ShortCutInfo info) {
		GLView view = null;
		String key = ConvertUtils.intentToString(info.mIntent);
		if (mViewHolder.containsKey(key)) {
			view = mViewHolder.get(key);
		}
		return view;
	}

	@Override
	public GLView removeViewByItem(ShortCutInfo info) {
		String key = ConvertUtils.intentToString(info.mIntent);
		return removeView(key);
	}

	@Override
	protected GLView createView(ShortCutInfo info) {
		GLView convertView = mInflater.inflate(R.layout.gl_screen_shortcut_icon, null);
		IconView<ShortCutInfo> icon = (IconView<ShortCutInfo>) convertView;
		GLScreenShortCutIcon shortCutIcon = (GLScreenShortCutIcon) icon;
		shortCutIcon.setTitleChangeListener(this);
		return icon;
	}

	@Override
	public void onTitleChange(String newTitle) {
		if (mBaseGridView != null) {
			mBaseGridView.post(new Runnable() {

				@Override
				public void run() {
					mBaseGridView.requestLayout();

				}
			});
		}
	}

}
