package com.jiubang.shell.screen.zero.search;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Bitmap;

import com.go.gl.widget.GLBaseAdapter;
import com.jiubang.ggheart.zeroscreen.search.bean.SearchResultInfo;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2012-10-23]
 */
public abstract class GLSearchListBaseAdapter extends GLBaseAdapter {
	protected ArrayList<SearchResultInfo> mDataSource;
	//联系人图像列表
	protected HashMap<String, SoftReference<Bitmap>> mContactsIconList = new HashMap<String, SoftReference<Bitmap>>();

	public GLSearchListBaseAdapter(ArrayList<SearchResultInfo> dataSource) {
		super();
		mDataSource = dataSource;
	}
	
	public GLSearchListBaseAdapter() {
		super();
	}

	@Override
	public int getCount() {
		if (mDataSource == null) {
			return 0;
		}
		return mDataSource.size();
	}

	@Override
	public Object getItem(int position) {
		if (mDataSource == null) {
			return null;
		}
		return mDataSource.get(position);
	}

	@Override
	public long getItemId(int position) {
		if (mDataSource == null || mDataSource.isEmpty()) {
			return 0;
		}
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		if (mDataSource == null || mDataSource.isEmpty()) {
			return 0;
		}
		return mDataSource.get(position) == null ? 0 : mDataSource.get(position).mType;
	}
	
	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 4;
	}

	public synchronized void updateDataSource(ArrayList<SearchResultInfo> dataSource) {
		mDataSource = dataSource;
		if (mContactsIconList != null) {
			mContactsIconList.clear();
		}
		notifyDataSetChanged();
	}

	public void recyle() {
		mDataSource = null;
		if (mContactsIconList != null) {
			mContactsIconList.clear();
		}
	}
}
