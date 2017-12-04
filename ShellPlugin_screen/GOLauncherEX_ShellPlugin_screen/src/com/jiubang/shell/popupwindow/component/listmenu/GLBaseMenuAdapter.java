package com.jiubang.shell.popupwindow.component.listmenu;

import java.util.ArrayList;

import android.content.Context;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLBaseAdapter;
import com.jiubang.ggheart.apps.desks.appfunc.menu.BaseMenuItemInfo;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * <br>类描述: 功能表菜单适配器
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-10-29]
 */
public class GLBaseMenuAdapter extends GLBaseAdapter {
	protected Context mContext;
	protected ArrayList<BaseMenuItemInfo> mList;
	protected int mTextColor;
	private int mItemPaddingLeft;
	private int mItemPaddingTop;
	private int mItemPaddingRight;
	private int mItemPaddingBottom;
	private int mTextSize = -1; // -1时使用预定义值
	private int mItemLayout = -1;

	public GLBaseMenuAdapter(Context context) {
		this.mContext = context;
	}

	public GLBaseMenuAdapter(Context context, ArrayList<BaseMenuItemInfo> list) {
		this.mList = list;
		this.mContext = context;
	}

	public void setItemList(ArrayList<BaseMenuItemInfo> itemList) {
		mList = itemList;
	}

	@Override
	public int getCount() {
		if (mList != null) {
			return mList.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (mList != null && position > -1 && position < mList.size()) {
			return mList.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setTextColor(int color) {
		mTextColor = color;
	}

	@Override
	public GLView getView(int position, GLView convertView, GLViewGroup parent) {
		BaseMenuItemInfo info = mList.get(position);
		if (info.mCustomStyle) {
			return convertView;
		}
		if (convertView == null || convertView.getId() != R.id.base_menu_item) {
			GLLayoutInflater inflater = ShellAdmin.sShellManager.getLayoutInflater();
			convertView = inflater.inflate(R.layout.gl_base_menu_item, null);
		}

		convertView.setTag(info);
		ShellTextViewWrapper textView = (ShellTextViewWrapper) convertView
				.findViewById(R.id.gl_appdrawer_base_menu_text);
		//GLCleanButtonComponent cleanButton = (GLCleanButtonComponent) convertView.findViewById(R.id.clean_button_component);
		/*if (position != 0 ) {
			cleanButton.setVisibility(View.GONE);
			textView.setGravity(Gravity.TOP);
		} else {
			cleanButton.setVisibility(View.VISIBLE);
		}*/
		textView.setPadding(mItemPaddingLeft, mItemPaddingTop, mItemPaddingRight,
				mItemPaddingBottom);
		textView.setTextPadding(mItemPaddingLeft, mItemPaddingTop, mItemPaddingRight,
				mItemPaddingBottom);
		textView.setTextColor(mTextColor);
		if (mTextSize > -1) {
			textView.setTextSize(mTextSize);
		}
		

		
		if (info.mText != null) {
			textView.setText(info.mText);
		} else if (info.mTextId != -1) {
			textView.setText(ShellAdmin.sShellManager.getActivity().getResources()
					.getString(info.mTextId));
		}
		return convertView;
	}

	public void setItemPadding(int left, int top, int right, int bottom) {
		mItemPaddingLeft = left;
		mItemPaddingTop = top;
		mItemPaddingRight = right;
		mItemPaddingBottom = bottom;
	}

	public void setItemTextSize(int size) {
		mTextSize = size;
	}

	public void setItemLayout(int resId) {
		mItemLayout = resId;
	}
}
