package com.jiubang.shell.screenedit;

import java.util.List;

import android.content.Context;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLImageView;
import com.jiubang.ggheart.apps.gowidget.GoWidgetProviderInfo;
import com.jiubang.ggheart.apps.gowidget.InnerWidgetInfo;
import com.jiubang.ggheart.apps.gowidget.gostore.GoStoreChannelControl;
import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.screenedit.tabs.ScreenEditImageLoader;

/**
 * 
 * @author zouguiquan
 *
 */
public class GLGoWidgetAdapter extends ShellBaseAdapter<Object> {

	private ScreenEditImageLoader mImageLoader;
	private String mChannelName = null;

	public GLGoWidgetAdapter(Context context, List<Object> infoList,
			ScreenEditImageLoader imageLoader) {
		super(context, infoList);
		mInfoList = infoList;
		mImageLoader = imageLoader;
		Context c = ShellAdmin.sShellManager.getActivity();
		mChannelName = GoStoreChannelControl.getChannelCheckName(c);
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

		Object item = mInfoList.get(position);
		if (item == null) {
			return view;
		}

		if (item instanceof ShortCutInfo) {

			ShortCutInfo info = (ShortCutInfo) item;
			text.setText(info.mTitle);
			view.setTag(info);

		} else if (item instanceof InnerWidgetInfo) {

			InnerWidgetInfo info = (InnerWidgetInfo) item;
			if (info.mPrototype == GoWidgetBaseInfo.PROTOTYPE_GOSTORE) {
				info.mTitle = mChannelName;
			}

			text.setText(info.getTitle());
			view.setTag(info);

		} else if (item instanceof GoWidgetProviderInfo) {
			GoWidgetProviderInfo info = (GoWidgetProviderInfo) item;
			text.setText(info.getTitle());
			view.setTag(info);
		}

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
