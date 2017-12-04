package com.jiubang.shell.appdrawer.search.adapter;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.go.gl.view.GLLayoutInflater;
import com.go.gl.widget.GLBaseAdapter;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
/**
 * 
 * @author dingzijian
 *
 * @param <T>
 */
public abstract class GLAppDrawerSearchBaseGridAdapter<T> extends GLBaseAdapter {

	private List<T> mInfoList;
	protected GLLayoutInflater mInflater;

	protected GLAppDrawerSearchBaseGridAdapter() {
		mInflater = ShellAdmin.sShellManager.getLayoutInflater();
	}

	public void setInfoList(List<T> infoList) {
		mInfoList = infoList;
	}

	protected void log(String message) {
		Log.i("GLAppDrawerSearchBaseGridAdapter", message);
	}

	@Override
	public int getCount() {
		if (mInfoList != null) {
			return mInfoList.size();
		}
		return 0;
	}

	@Override
	public T getItem(int pos) {
		if (mInfoList != null && pos < mInfoList.size()) {
			return mInfoList.get(pos);
		}
		return null;
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}
	
	public void clearInfoList() {
		if (mInfoList != null) {
			mInfoList.clear();
		}
	}
	public void addInfoList(List<T> data, int pos) {
		if (mInfoList == null) {
			mInfoList = new ArrayList<T>();
		}
		if (pos > mInfoList.size()) {
			return;
		}
		mInfoList.addAll(pos, data);
	}

	public List<T> getInfoList() {
		return mInfoList;
	}

}
