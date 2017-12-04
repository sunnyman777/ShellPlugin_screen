package com.jiubang.shell.appdrawer.search.adapter;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLAbsListView;
import com.go.gl.widget.GLAbsListView.LayoutParams;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.FuncSearchResultItem;
import com.jiubang.shell.appdrawer.search.GLAppDrawerSearchAppGrid;
import com.jiubang.shell.appdrawer.search.GLAppDrawerSearchBaseGridView.OnStartSearchResutlListener;
import com.jiubang.shell.appdrawer.search.GLAppDrawerSearchHeader;
import com.jiubang.shell.appdrawer.search.GLAppDrawerSearchListView;
import com.jiubang.shell.appdrawer.search.SearchResultListInfo;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
/**
 * 
 * @author dingzijian
 *
 */
public class GLAppDrawerSearchListAdapter extends GLAppDrawerSearchBaseGridAdapter<Object> {
	private ViewHolder mViewHolder;
	
	private boolean mShowHistory;
	
	public GLAppDrawerSearchListAdapter() {
		super();
	}

	@Override
	public GLView getView(int pos, GLView convertView, GLViewGroup parent) {
		Object resultItem = getItem(pos);
		GLFrameLayout itemMainLayout = null;
		if (convertView != null) {
			itemMainLayout = (GLFrameLayout) convertView;
			mViewHolder = (ViewHolder) itemMainLayout.getTag();
		} else {
			itemMainLayout = (GLFrameLayout) mInflater.inflate(
					R.layout.gl_appdrawer_search_list_item_layout, null);
			mViewHolder = new ViewHolder();
			mViewHolder.mListBtn = (ShellTextViewWrapper) itemMainLayout
					.findViewById(R.id.gl_search_list_btn);
			mViewHolder.mAppGrid = (GLAppDrawerSearchAppGrid) itemMainLayout
					.findViewById(R.id.gl_search_app_grid);
			mViewHolder.mSearchHeader = (GLAppDrawerSearchHeader) itemMainLayout
					.findViewById(R.id.gl_search_header);
			itemMainLayout.setTag(mViewHolder);
		}

		if (resultItem instanceof FuncSearchResultItem) {
			FuncSearchResultItem searchResultItem = (FuncSearchResultItem) resultItem;
			GLAppDrawerSearchListView listView = (GLAppDrawerSearchListView) parent;
			switch (searchResultItem.mType) {
				case FuncSearchResultItem.ITEM_TYPE_SEARCH_WEB : {
					mViewHolder.mListBtn.setOnClickListener(listView);
					mViewHolder.mAppGrid.setVisible(false);
					mViewHolder.mSearchHeader.setVisible(false);
					mViewHolder.mListBtn.setVisible(true);
					mViewHolder.mListBtn.setText(searchResultItem.mTitle);
					mViewHolder.mListBtn.setTag(searchResultItem);
					boolean needSetItemHeight = (itemMainLayout.getLayoutParams() == null || itemMainLayout
							.getLayoutParams().height != DrawUtils.dip2px(71)) ? true : false;
					if (needSetItemHeight) {
						setItemHeight(itemMainLayout, DrawUtils.dip2px(71));
					}
					return itemMainLayout;
				}
				case FuncSearchResultItem.ITEM_TYPE_RESULT_HEADER :
					mViewHolder.mSearchHeader.setText(searchResultItem.mTitle);
					mViewHolder.mSearchHeader.setVisible(true);
					mViewHolder.mAppGrid.setVisible(false);
					mViewHolder.mListBtn.setVisible(false);
					boolean needSetItemHeight = (itemMainLayout.getLayoutParams() == null || itemMainLayout
							.getLayoutParams().height != DrawUtils.dip2px(54)) ? true : false;
					if (needSetItemHeight) {
						setItemHeight(itemMainLayout, DrawUtils.dip2px(54));
					}
					mViewHolder.mSearchHeader.setClearBtnVisible(mShowHistory);
					mViewHolder.mSearchHeader.setClearBtnOnClickListener(listView);
					return itemMainLayout;

				default :
					break;
			}
		} else if (resultItem instanceof SearchResultListInfo) {
			@SuppressWarnings("unchecked")
			SearchResultListInfo searchResultListInfo = (SearchResultListInfo) resultItem;
			OnStartSearchResutlListener searchResutlListener = (OnStartSearchResutlListener) parent
					.getGLParent().getGLParent();
			mViewHolder.mAppGrid.setSearchResutlListener(searchResutlListener);
			mViewHolder.mAppGrid.setVisible(true);
			mViewHolder.mSearchHeader.setVisible(false);
			mViewHolder.mListBtn.setVisible(false);
			GLAppDrawerSearchAppGridAdapter adapter = (GLAppDrawerSearchAppGridAdapter) mViewHolder.mAppGrid
					.getAdapter();
			if (adapter == null) {
				adapter = new GLAppDrawerSearchAppGridAdapter();
				adapter.setInfoList(searchResultListInfo.getSearchResultItems());
				mViewHolder.mAppGrid.setAdapter(adapter);
			} else {
				adapter.setInfoList(searchResultListInfo.getSearchResultItems());
				adapter.notifyDataSetChanged();
			}
			boolean needSetItemHeight = (itemMainLayout.getLayoutParams() == null || itemMainLayout
					.getLayoutParams().height != LayoutParams.WRAP_CONTENT) ? true : false;
			if (needSetItemHeight) {
				setItemHeight(itemMainLayout, LayoutParams.WRAP_CONTENT);
			}
			return itemMainLayout;
		}
		return null;
	}

	private void setItemHeight(GLViewGroup glViewGroup, int height) {
		GLAbsListView.LayoutParams layoutParams = (LayoutParams) glViewGroup.getLayoutParams();
		if (layoutParams == null) {
			layoutParams = new GLAbsListView.LayoutParams(LayoutParams.MATCH_PARENT, height);
		} else {
			layoutParams.height = height;
		}
		glViewGroup.setLayoutParams(layoutParams);
	}
	
	public void setShowHistory(boolean showHistory) {
		this.mShowHistory = showHistory;
	}
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  dingzijian
	 * @date  [2012-10-31]
	 */
	class ViewHolder {
		ShellTextViewWrapper mListBtn;
		GLAppDrawerSearchAppGrid mAppGrid;
		GLAppDrawerSearchHeader mSearchHeader;
	}

}
