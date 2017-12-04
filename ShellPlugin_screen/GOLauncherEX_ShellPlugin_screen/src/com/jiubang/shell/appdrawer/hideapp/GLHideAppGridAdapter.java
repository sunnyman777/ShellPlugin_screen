package com.jiubang.shell.appdrawer.hideapp;

import java.util.List;

import android.content.Context;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunItemInfo;
import com.jiubang.shell.appdrawer.adapter.GLGridBaseAdapter;
import com.jiubang.shell.common.component.IconView;

/**
 * 隐藏程序显示grid的适配器
 * 
 * @author wuziyi
 * 
 */
public class GLHideAppGridAdapter extends GLGridBaseAdapter<FunAppItemInfo> {

	public GLHideAppGridAdapter(Context context, List<FunAppItemInfo> infoList) {
		super(context, infoList);
	}

	@Override
	public GLView getView(int position, GLView convertView, GLViewGroup parent) {
		FunAppItemInfo info = getItem(position);
		convertView = getViewByItem(info);
		IconView<FunItemInfo> icon = null;
		if (convertView != null) {
			mCacheMap.put(ConvertUtils.intentToString(info.getIntent()),
					convertView);
			icon = (IconView<FunItemInfo>) convertView;
			if (info == icon.getInfo()) {
				return convertView;
			}
		}
		icon = (IconView<FunItemInfo>) createView(info);
		icon.setInfo(info);
		mCacheMap.put(ConvertUtils.intentToString(info.getIntent()), icon);
		mViewHolder.put(ConvertUtils.intentToString(info.getIntent()), icon);
		return icon;
	}

	@Override
	public GLView getViewByItem(FunAppItemInfo info) {
		GLView view = null;
		String key = ConvertUtils.intentToString(info.getIntent());
		if (mViewHolder.containsKey(key)) {
			view = mViewHolder.get(key);
		}
		return view;
	}

	@Override
	public GLView removeViewByItem(FunAppItemInfo info) {
		String key = ConvertUtils.intentToString(info.getIntent());
		return removeView(key);
	}

	@Override
	protected GLView createView(FunAppItemInfo info) {
		GLView convertView = mInflater.inflate(
				R.layout.gl_appdrawer_allapp_icon, null);
		IconView<FunItemInfo> icon = (IconView<FunItemInfo>) convertView;
		icon.setEnableAppName(mIsShowAppName);
		return icon;
	}
}
