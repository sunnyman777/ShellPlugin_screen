package com.jiubang.shell.screenedit;

import java.util.List;

import android.content.Context;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLImageView;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.common.component.ShellTextViewWrapper;

/**
 * 
 * @author zouguiquan
 *
 */
public class GLAppsAdapter extends ShellBaseAdapter<Object> {

	public GLAppsAdapter(Context context, List<Object> infoList) {
		super(context, infoList);
		mInfoList = infoList;
	}

	@Override
	public GLView getView(int position, GLView convertView, GLViewGroup parent) {

		GLView view = null;
		if (convertView != null) {
			view = convertView;
		} else {
			view = mInflater.inflate(R.layout.gl_screen_edit_item, null);
		}

		GLImageView image = (GLImageView) view.findViewById(R.id.thumb);
		ShellTextViewWrapper mText = (ShellTextViewWrapper) view.findViewById(R.id.title);

		AppItemInfo item = (AppItemInfo) mInfoList.get(position);
		if (item != null) {
			mText.setText(item.mTitle);
			view.setTag(item);
			image.setImageDrawable(item.mIcon);
		}

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
