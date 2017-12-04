package com.jiubang.shell.screenedit.tabs;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.gl.widget.GLAdapterView.OnItemClickListener;
import com.go.gl.widget.GLAdapterView.OnItemLongClickListener;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.screenedit.GLEditBaseGrid;

/**
 * 
 * @author zouguiquan
 *
 */
public class GLGridTab extends GLBaseTab implements OnItemClickListener, OnItemLongClickListener {

	protected GLEditBaseGrid mBaseGrid;

	public GLGridTab(Context context, int tabId, int tabLevel) {
		super(context, tabId, tabLevel);
		mBaseGrid = new GLEditBaseGrid(context, this);
		mBaseGrid.setOnItemClickListener(this);
		mBaseGrid.setCacheAble(true);

		mImageLoader.setFirstLoadPage(0);
		mImageLoader.setPageSize(getPageSize());
	}

	@Override
	protected void onloadDataFinish(ArrayList<Object> dataList) {
		if (dataList != null && dataList.size() > 0) {
			mImageLoader.setTotalSize(dataList.size());
			mBaseGrid.setData(dataList);
		}
	}

	@Override
	public int getTotalPage() {
		return mBaseGrid.getTotalPage();
	}

	@Override
	public int getPageSize() {
		return mBaseGrid.getNumColumns() * mBaseGrid.getNumRows();
	}

	@Override
	public void setIndicator(GLView indicator) {
		mBaseGrid.setIndicator(indicator);
	}

	@Override
	public void clickIndicatorItem(int index) {
		mBaseGrid.snapToScreen(index, true);
	}

	@Override
	public void sliding(float percent) {
	}

	@Override
	public int getItemCount() {
		if (mDataList != null) {
			return mDataList.size();
		}
		return 0;
	}

	@Override
	public GLView getView(int position) {
		GLView glView = null;
		if (mBaseGrid != null) {
			glView = mBaseGrid.getChildAt(position);
		}
		return glView;
	}

	@Override
	public GLView getContentView() {
		return mBaseGrid;
	}

	@Override
	public void clear() {
		super.clear();
		mBaseGrid.cleanup();
	}

	@Override
	public ArrayList<Object> requestData() {
		return null;
	}

	@Override
	protected void onLoadDataStart() {
		super.onLoadDataStart();
		if (needChangeAnim()) {
			mBaseGrid.setVisibility(GLView.INVISIBLE);
		}
	}

	@Override
	public void resetData() {
		if (mBaseGrid.getCurrentScreen() != mCurrentScreen) {
			mBaseGrid.snapToScreen(mCurrentScreen, false);
			mImageLoader.setCurrentLoadPage(mCurrentScreen);
		}
	}

	@Override
	public void setParam(Object[] params) {
	}

	@Override
	public Object getParam() {
		return null;
	}

	@Override
	public void onItemClick(GLAdapterView<?> arg0, GLView arg1, int arg2, long arg3) {
	}

	@Override
	public boolean onItemLongClick(GLAdapterView<?> arg0, GLView arg1, int arg2, long arg3) {
		return false;
	}

	@Override
	public Bitmap onLoadImage(int index) {
		return null;
	}

	@Override
	public void onLoadImageFinish(int index) {
	}

	@Override
	public void onLoadRangeImageFinish() {

		if (needChangeAnim()) {
			Log.d("zgq", "GLGridTab onLoadRangeImageFinish startChangeAnim");
			mBaseGrid.startChangeAnim();
		}
	}

	@Override
	public Object getItem(int position) {
		if (mDataList != null && position < mDataList.size()) {
			return mDataList.get(position);
		}
		return null;
	}

	public ShellBaseAdapter<Object> createAdapter(Context mContext, List<Object> infoList) {
		return null;
	}

	@Override
	public boolean onKeyBack() {
		super.onKeyBack();
		mCurrentScreen = 0;
		return false;
	}
}
