package com.jiubang.shell.screen.zero.navigation;

import java.util.ArrayList;

import android.content.Context;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLBaseAdapter;
import com.jiubang.ggheart.zeroscreen.navigation.bean.ZeroScreenAdInfo;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
* ad适配器
*/

public class GLAdAdapter extends GLBaseAdapter {
	private ArrayList<ZeroScreenAdInfo> mAdInfos;
	private Context mContext;
	private GLLayoutInflater mInflater = null;

	public GLAdAdapter(Context context) {
		mContext = context;
		mInflater = ShellAdmin.sShellManager.getLayoutInflater();


	}

	public void refreshData(ArrayList<ZeroScreenAdInfo> AdInfos) {
		if (mAdInfos == null) {
			mAdInfos = new ArrayList<ZeroScreenAdInfo>();
		} else {
			mAdInfos.clear();
		}
		if (mAdInfos != null) {
			for (ZeroScreenAdInfo bean : AdInfos) {
				if (bean != null) {
					mAdInfos.add(bean);
				}
			}
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mAdInfos == null ? 0 : mAdInfos.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mAdInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public GLView getView(int position, GLView convertView, GLViewGroup parent) {
		// TODO Auto-generated method stub
		GLAdItemView adIcon = null;
		if (mAdInfos != null && position < mAdInfos.size()) {
			final ZeroScreenAdInfo adBean = mAdInfos.get(position);
			if (convertView != null && convertView instanceof GLAdItemView) {
				adIcon = (GLAdItemView) convertView;
			}

			if (adIcon == null) {
				adIcon = (GLAdItemView) mInflater.inflate(
						R.layout.gl_zero_screen_ad_item_layout, null);
			}
			adIcon.setAdBean(adBean);
			adIcon.setContex(mContext);
		}
		return adIcon;

	}

}