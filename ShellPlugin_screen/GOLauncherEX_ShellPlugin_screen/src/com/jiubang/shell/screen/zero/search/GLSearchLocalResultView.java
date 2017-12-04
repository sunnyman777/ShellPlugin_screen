package com.jiubang.shell.screen.zero.search;

import java.util.ArrayList;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLRelativeLayout;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAbsListView;
import com.go.gl.widget.GLAbsListView.OnScrollListener;
import com.go.gl.widget.GLAdapterView;
import com.go.gl.widget.GLAdapterView.OnItemClickListener;
import com.go.gl.widget.GLListView;
import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.zeroscreen.StatisticsUtils;
import com.jiubang.ggheart.zeroscreen.search.bean.SearchResultInfo;
import com.jiubang.ggheart.zeroscreen.search.util.SearchUtils;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.screen.zero.search.GLSearchLocalListAdapter.ItemViewAppHolder;
import com.jiubang.shell.screen.zero.search.GLSearchLocalListAdapter.ItemViewHolder;

/**
 * 搜索结果显示view
 * @author liulixia
 *
 */
public class GLSearchLocalResultView extends GLRelativeLayout
		implements OnItemClickListener, OnScrollListener {
	private Context mContext;
	private SearchUtils mSearchUtils;
	private ShellTextViewWrapper mSearchNoData;
	private GLListView mSearchListView;
	private GLSearchLocalListAdapter mSearchListViewAdapter;
	
	public GLSearchLocalResultView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mSearchUtils = SearchUtils.getInstance(context);
	}
	
	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		mSearchNoData = (ShellTextViewWrapper) findViewById(R.id.appfunc_search_no_data_view);
		mSearchListView = (GLListView) findViewById(R.id.appfunc_search_result_listView);
		this.setClipChildren(true);
//		mSearchListView.setClipChildren(false);
		mSearchListView.setOnItemClickListener(this);
		mSearchListView.setOnScrollListener(this);
	}


	public void setNoData(boolean hasText) {
		if (mSearchListViewAdapter != null) {
			mSearchListViewAdapter.updateDataSource(null);
		}
		mSearchListView.setAdapter(null);
		mSearchListView.setVisibility(View.GONE);
		if (hasText) {
			mSearchNoData.setVisibility(View.VISIBLE);
		} else {
			mSearchNoData.setVisibility(View.GONE);
		}
	}

	public void onSearchFinish(ArrayList<SearchResultInfo> results, boolean hasText) {
		if (results == null || results.size() == 0) {
			setNoData(hasText);
		} else {
			if (mSearchListViewAdapter == null) {
				mSearchListViewAdapter = new GLSearchLocalListAdapter(mContext);
			}
			mSearchListView.setAdapter(mSearchListViewAdapter);
			if (mSearchListView.getVisibility() == View.GONE) {
				mSearchListView.setVisibility(View.VISIBLE);
				mSearchNoData.setVisibility(View.GONE);
			}
			
			mSearchListViewAdapter.updateDataSource((ArrayList<SearchResultInfo>) results.clone());
		}
	}
	
	public void recyle() {
		if (mSearchListViewAdapter != null) {
			mSearchListViewAdapter.recyle();
			mSearchListViewAdapter.updateDataSource(null);
		}
		mSearchListView.setAdapter(null);
		mSearchListView.setVisibility(View.GONE);
		mSearchNoData.setVisibility(View.GONE);
		mSearchUtils.recyle();
	}
	
	public void onDestory() {
		if (mSearchListViewAdapter != null) {
			mSearchListViewAdapter.destory();
			mSearchListViewAdapter = null;
		}
		
		if (mSearchUtils != null) {
			mSearchUtils.onDestory();
			mSearchUtils = null;
		}
	}

	@Override
	public void onScroll(GLAbsListView arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void onScrollStateChanged(GLAbsListView arg0, int arg1) {
		InputMethodManager imm = (InputMethodManager) mContext
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(getWindowToken(), 0);
		}
	}

	@Override
	public void onItemClick(GLAdapterView<?> arg0, GLView arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Object tag = arg1.getTag();
		if (tag instanceof ItemViewHolder) {
			long contactId = (Long) ((ItemViewHolder) tag).mItemTitle.getTag();
			SearchUtils.getInstance(mContext).saveSearchText();
			GuiThemeStatistics.getInstance(getContext()).guiStaticData(57, "",
					StatisticsUtils.SEARCH_GO, 1, "0", "1", "", "");
			Uri uri = Uri.withAppendedPath(
					ContactsContract.Contacts.CONTENT_URI,
					Long.toString(contactId));
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			if (mContext instanceof Application) {
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			mContext.startActivity(intent);
		} else if (tag instanceof ItemViewAppHolder) {
			SearchUtils.getInstance(mContext).saveSearchText();
			GuiThemeStatistics.getInstance(getContext()).guiStaticData(57, "",
					StatisticsUtils.SEARCH_GO, 1, "0", "1", "", "");
			Intent intent = (Intent) ((ItemViewAppHolder) tag).mItemTitle
					.getTag();
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					ICommonMsgId.START_ACTIVITY, -1, intent, null);
			String action = intent.getAction();
		}
	}
}
