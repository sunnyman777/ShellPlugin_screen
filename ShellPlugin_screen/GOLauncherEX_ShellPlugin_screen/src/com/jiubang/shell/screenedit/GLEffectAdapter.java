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

/**
 * 
 * @author zouguiquan
 *
 */
public class GLEffectAdapter extends ShellBaseAdapter<Object> {

	private int mSelectedPos;

	public GLEffectAdapter(Context context, List<Object> infoList, int selectedPos) {
		super(context, infoList);
		mInfoList = infoList;
		mSelectedPos = selectedPos;
	}

	public void setSelectedPos(int pos) {
		mSelectedPos = pos;
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
		GLImageView install = (GLImageView) view.findViewById(R.id.screen_edit_item_state);
		install.setVisibility(GLView.INVISIBLE);

		ScreenEditItemInfo item = (ScreenEditItemInfo) mInfoList.get(position);
		if (item == null) {
			return convertView;
		}

		mText.setText(item.getTitle());
		image.setImageDrawable(item.getIcon());
		if (position == mSelectedPos) {
			install.setVisibility(GLView.VISIBLE);
			install.setImageResource(R.drawable.gl_folder_select);
		}
		view.setTag(item);

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
