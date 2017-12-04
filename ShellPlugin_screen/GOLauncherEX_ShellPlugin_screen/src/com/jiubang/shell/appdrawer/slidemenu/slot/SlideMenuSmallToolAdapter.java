package com.jiubang.shell.appdrawer.slidemenu.slot;

import java.util.List;

import android.content.Context;

import com.gau.go.gostaticsdk.utiltool.DrawUtils;
import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.jiubang.ggheart.components.sidemenuadvert.tools.SideToolsInfo;
import com.jiubang.shell.appdrawer.adapter.GLGridBaseAdapter;
import com.jiubang.shell.common.component.SmallToolIconVIew;
/**
 * SlideMenuSmallToolAdapter
 * @author hanson
 * 2014-04-24
 */
public class SlideMenuSmallToolAdapter extends GLGridBaseAdapter<SideToolsInfo> {

	public SlideMenuSmallToolAdapter(Context context, List infoList) {
		super(context, infoList);
		
	}

	@Override
	public GLView getView(int position, GLView convertView, GLViewGroup parent) {
		SideToolsInfo slot = mInfoList.get(position);
		convertView = getViewByItem(slot);
		if (convertView == null) {
			convertView = createView(slot);
			SmallToolIconVIew view = (SmallToolIconVIew) convertView;
			
			mViewHolder.put(String.valueOf(slot.getToolsPkgName()), convertView);
		}
		return convertView;
	}

	@Override
	protected GLView createView(SideToolsInfo info) {
		GLView convertView = mInflater.inflate(R.layout.gl_appdrawer_slidemenu_smalltool_icon, null);
		SmallToolIconVIew view = (SmallToolIconVIew) convertView;
		view.setInfo(info);
		//fixme: move to dimen file
		view.setIconSize(DrawUtils.dip2px(53));
		//mContext.getResources().getDimensionPixelSize(
		//R.dimen.appdrawer_folder_action_bar_icon_size)
		return convertView;
	}

	@Override
	public GLView getViewByItem(SideToolsInfo t) {
		return mViewHolder.get(String.valueOf(t.getToolsPkgName()));
	}

	@Override
	public GLView removeViewByItem(SideToolsInfo t) {
		return mViewHolder.remove(String.valueOf(t.getToolsPkgName()));
	}
	
	
}
