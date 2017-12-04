package com.jiubang.shell.appdrawer.recentapp;

import java.util.List;

import android.content.Context;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.info.FunItemInfo;
import com.jiubang.shell.appdrawer.adapter.GLGridBaseAdapter;
import com.jiubang.shell.common.component.IconView;

/**
 * 最近打开适配器
 * @author yejijiong
 *
 */
public class GLRecentAppAdapter extends GLGridBaseAdapter<FunItemInfo> {


	public GLRecentAppAdapter(Context context, List infoList) {
		super(context, infoList);
	}

	@SuppressWarnings("unchecked")
	@Override
	public GLView getView(int position, GLView convertView, GLViewGroup parent) {
		FunItemInfo info = getItem(position);
		convertView = getViewByItem(info);
		IconView<FunItemInfo> icon = null;
		if (convertView != null) {
			mCacheMap.put(ConvertUtils.intentToString(info.getIntent()), convertView);
			icon = (IconView<FunItemInfo>) convertView;
			if (info == icon.getInfo()) {
				return convertView;
			}
		}
		icon = (IconView<FunItemInfo>) createView(info);
		mCacheMap.put(ConvertUtils.intentToString(info.getIntent()), icon);
		mViewHolder.put(ConvertUtils.intentToString(info.getIntent()), icon);
		return icon;
	}

	@Override
	public GLView getViewByItem(FunItemInfo info) {
		GLView view = null;
		String key = ConvertUtils.intentToString(info.getIntent());
		if (mViewHolder.containsKey(key)) {
			view = mViewHolder.get(key);
		}
		return view;
	}

	@Override
	public GLView removeViewByItem(FunItemInfo info) {
		String key = ConvertUtils.intentToString(info.getIntent());
		return removeView(key);
	}

	@Override
	protected GLView createView(FunItemInfo info) {
		GLView convertView = mInflater.inflate(R.layout.gl_appdrawer_allapp_icon, null);
		IconView<FunItemInfo> icon = (IconView<FunItemInfo>) convertView;
		icon.setInfo(info);
		icon.setEnableAppName(mIsShowAppName);
		return icon;
	}

	//	/**
	//	 * 设置最多显示图标数
	//	 */
	//	public void setMaxAppCount(int maxAppCount) {
	//		mMaxAppCount = maxAppCount;
	//	}
}
