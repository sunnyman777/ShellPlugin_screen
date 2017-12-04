package com.jiubang.shell.screenedit;

import java.util.List;

import android.content.Context;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLImageView;
import com.jiubang.ggheart.apps.gowidget.ScreenEditItemInfo;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.screenedit.tabs.ScreenEditImageLoader;

/**
 * 
 * @author zouguiquan
 *
 */
public class GLSysWidgetAdapter extends ShellBaseAdapter<Object> {

	private ScreenEditImageLoader mImageLoader;

	public GLSysWidgetAdapter(Context context, List<Object> infoList,
			ScreenEditImageLoader imageLoader) {
		super(context, infoList);
		mInfoList = infoList;
		mImageLoader = imageLoader;
	}

	@Override
	public GLView getView(int position, GLView convertView, GLViewGroup parent) {

		GLView view = null;
		if (convertView != null) {
			view = convertView;
		} else {
			view = mInflater.inflate(R.layout.gl_screen_edit_item, null);
		}
		
		ShellTextViewWrapper text = (ShellTextViewWrapper) view.findViewById(R.id.title);
		GLImageView imageView = (GLImageView) view.findViewById(R.id.thumb);
		imageView.setImageResource(R.drawable.gl_ic_launcher_application);

		ScreenEditItemInfo item = (ScreenEditItemInfo) mInfoList.get(position);
		if (item == null) {
			return view;
		}
		
		text.setText(item.getTitle());
		view.setTag(item);

		if (mImageLoader != null) {
			mImageLoader.loadImage(imageView, position);
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
