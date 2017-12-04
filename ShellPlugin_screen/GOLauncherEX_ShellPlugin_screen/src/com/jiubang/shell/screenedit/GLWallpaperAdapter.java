package com.jiubang.shell.screenedit;

import java.util.List;

import android.content.Context;
import android.view.View;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLImageView;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditController;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.bean.WallpaperItemInfo;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author zouguiquan
 *
 */
public class GLWallpaperAdapter extends ShellBaseAdapter<Object> {

	private Context m2DContext;

	public GLWallpaperAdapter(Context context, List<Object> infoList) {
		super(context, infoList);
		mInfoList = infoList;
		m2DContext = ShellAdmin.sShellManager.getActivity();
	}

	@Override
	public GLView getView(int position, GLView convertView, GLViewGroup parent) {

		GLView view = null;
		if (convertView != null) {
			view = convertView;
		} else {
			view = mInflater.inflate(R.layout.gl_screen_edit_item, null);
		}

		GLImageView imageView = (GLImageView) view.findViewById(R.id.thumb);
		ShellTextViewWrapper text = (ShellTextViewWrapper) view.findViewById(R.id.title);
		GLImageView newTag = (GLImageView) view.findViewById(R.id.screen_edit_item_new);
		newTag.setImageResource(R.drawable.transparent);

		WallpaperItemInfo dto = (WallpaperItemInfo) getItem(position);
		if (dto == null) {
			return view;
		}

		// 添加界面去除小红点--isFristShowScreenEditGOWallpaper() 和 isFirstShowScreenEditFilter()直接返回false
		if (ScreenEditController.WALLPAPER_FILTER.equals(dto.getPkgName())
				&& ScreenEditController.isFirstShowScreenEditFilter(m2DContext, false)) {
			newTag.setVisibility(View.VISIBLE);
		}

		// 如果是非200渠道go桌面主题则不显示小红点
		if (PackageName.PACKAGE_NAME.equals(dto.getPkgName())
				&& !Statistics.getUid(mContext).equals("200")) {
			newTag.setVisibility(View.GONE);
		}

		imageView.setImageDrawable(dto.getAppIcon());
		text.setText(dto.getmAppLabel());
		view.setTag(dto);

		return view;
	}

	@Override
	public GLView getViewByItem(Object t) {
		return null;
	}

	@Override
	public GLView removeViewByItem(Object t) {
		return null;
	}

}
