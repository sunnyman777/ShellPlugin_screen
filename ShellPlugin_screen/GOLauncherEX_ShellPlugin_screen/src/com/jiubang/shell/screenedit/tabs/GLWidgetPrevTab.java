package com.jiubang.shell.screenedit.tabs;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;

import com.go.gl.view.GLView;
import com.jiubang.shell.screenedit.GLHorScrollerView;

/**
 * 
 * @author zouguiquan
 *
 */
public class GLWidgetPrevTab extends GLBaseTab {

	protected GLHorScrollerView mHorScrollerView;
	private boolean mHasHeadView;

	public GLWidgetPrevTab(Context context, int tabId, int tabLevel) {
		super(context, tabId, tabLevel);
		mHorScrollerView = new GLHorScrollerView(context);
		mHorScrollerView.setBaseTab(this);

		mImageLoader.setFirstLoadPage(0);
		mImageLoader.setPageSize(getPageSize());
	}

	protected GLView getHeadView() {
		return null;
	}

	protected GLView getFootView() {
		return null;
	}

	public GLView getChildAt(int index) {
		if (mHasHeadView) {
			index++;
		}

		if (mHorScrollerView != null) {
			int count = mHorScrollerView.getChildCount();
			if (index >= 0 && index < count) {
				return mHorScrollerView.getChildAt(index);
			}
		}

		return null;
	}
	
	@Override
	protected void onloadDataFinish(ArrayList<Object> dataList) {
		if (dataList != null && dataList.size() > 0) {
			mImageLoader.setTotalSize(dataList.size());
			GLView headView = getHeadView();
			if (headView != null) {
				mHasHeadView = true;
			}
			mHorScrollerView.appendHeadView(headView);

			GLView backView = getFootView();
			mHorScrollerView.appendBackView(backView);

			mHorScrollerView.initChildView();
		}
	}

	@Override
	public ArrayList<Object> requestData() {
		return null;
	}
	
	@Override
	protected void onLoadDataStart() {
		super.onLoadDataStart();
		if (mNeedAsyncLoadImage) {
			mHorScrollerView.setVisibility(GLView.INVISIBLE);
		}
	}

	@Override
	public int getItemCount() {
		if (mDataList != null) {
			return mDataList.size();
		}
		return 0;
	}

	@Override
	public int getTotalPage() {
		return mHorScrollerView.getChildCount();
	}

	@Override
	public int getPageSize() {
		return 1;
	}

	@Override
	public void setIndicator(GLView indicator) {
		mHorScrollerView.setIndicator(indicator);
	}

	@Override
	public void clickIndicatorItem(int index) {
		mHorScrollerView.snapToScreen(index, true, GLHorScrollerView.SCROLLER_DURATION);
	}

	@Override
	public void sliding(float percent) {
	}

	@Override
	public GLView getContentView() {
		return mHorScrollerView;
	}

	@Override
	public void resetData() {
	}

	@Override
	public boolean onKeyBack() {
		boolean result = super.onKeyBack();
		
		mHasHeadView = false;
		if (mImageLoader != null) {
			mImageLoader.clear();
		}
		if (mDataList != null) {
			mDataList.clear();
		}

		return result;
	}

	@Override
	public void clear() {
		super.clear();
		mHorScrollerView.cleanup();
	}

	@Override
	public GLView getView(int position) {
		return null;
	}

	@Override
	public void setParam(Object[] params) {
	}

	@Override
	public Object getParam() {
		return null;
	}

	@Override
	public Bitmap onLoadImage(int index) {
		return null;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public void onLoadImageFinish(int index) {
	}

	@Override
	public void onLoadRangeImageFinish() {
		if (mNeedAsyncLoadImage) {
			mHorScrollerView.startChangeAnim();
		}
	}
}
