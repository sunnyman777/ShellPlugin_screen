package com.jiubang.shell.appdrawer.search;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLAbsListView;
import com.go.gl.widget.GLAbsListView.OnScrollListener;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.util.file.media.ThumbnailManager;
import com.go.util.graphics.DrawUtils;
import com.go.util.window.OrientationControl;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.appfunc.controler.MediaFileSuperVisor;
import com.jiubang.ggheart.apps.desks.appfunc.search.AppfuncSearchEngine;
import com.jiubang.ggheart.apps.desks.appfunc.search.ResponseHandler;
import com.jiubang.ggheart.apps.desks.diy.pref.PrefConst;
import com.jiubang.ggheart.apps.desks.diy.pref.PrivatePreference;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.statistics.realtiemstatistics.RealTimeStatisticsUtil;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.appdrawer.GLAppDrawer;
import com.jiubang.shell.appdrawer.component.GLAbsExtendFuncView;
import com.jiubang.shell.appdrawer.search.GLAppDrawerSearchBaseGridView.OnStartSearchResutlListener;
import com.jiubang.shell.appdrawer.search.GLAppDrawerSearchInputBar.SearchKeyChangedListener;
import com.jiubang.shell.appdrawer.search.GLAppDrawerSearchListView.ListBtnOnClickListener;
import com.jiubang.shell.appdrawer.search.adapter.GLAppDrawerSearchListAdapter;
import com.jiubang.shell.common.component.GLProgressBar;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.common.listener.RemoveViewAnimationListener;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
/**
 * 
 * @author dingzijian
 *
 */
public class GLAppDrawerSearchMainView extends GLAbsExtendFuncView
		implements
			SearchKeyChangedListener,
			ListBtnOnClickListener,
			OnStartSearchResutlListener,
			IMessageHandler,
			OnScrollListener {
	private GLAppDrawerSearchListView mSearchListView;

	private GLAppDrawerSearchInputBar mSearchInputBar;

	private GLAppDrawerSearchHandler mSearchHandler;

	private ShellTextViewWrapper mNoDataView;

	private GLProgressBar mProgressBar;
	
	private int mVisibleLastIndex = 0;
	
	public GLAppDrawerSearchMainView(Context context) {
		super(context);
		setVisible(false);
		MsgMgrProxy.registMsgHandler(this);
		ShellAdmin.sShellManager.getLayoutInflater().inflate(
				R.layout.gl_appdrawer_search_main_layout, this);
		mSearchHandler = new GLAppDrawerSearchHandler(this);
		mSearchInputBar = (GLAppDrawerSearchInputBar) findViewById(R.id.gl_search_input_bar);
		mSearchInputBar.setSearchKeyChangedListener(this);
		mSearchListView = (GLAppDrawerSearchListView) findViewById(R.id.gl_search_result_list);
		mSearchListView.setListBtnOnClickListener(this);
		mSearchListView.setOnScrollListener(this);
		mNoDataView = (ShellTextViewWrapper) findViewById(R.id.gl_search_no_data);
		mNoDataView.getTextView().setCompoundDrawablePadding(DrawUtils.dip2px(23));
		mNoDataView.setText(ApplicationProxy.getContext().getString(
				com.gau.go.launcherex.R.string.appfunc_search_tip_no_match_data_web));
		mProgressBar = (GLProgressBar) findViewById(R.id.gl_search_progress_bar);
		mProgressBar.setMode(GLProgressBar.MODE_INDETERMINATE);
		Resources res = getContext().getResources();
		mProgressBar.setIndeterminateProgressDrawable(res
				.getDrawable(R.drawable.gl_progressbar_indeterminate_white));

		setHasPixelOverlayed(false);
	}

	@Override
	public int getViewId() {
		return IViewId.APP_DRAWER_SEARCH;
	}

	private void showNoDataView(String text) {
		mNoDataView.setVisibility(View.VISIBLE);
		mNoDataView.setText(text);
	}

	void showProgressBar() {
		mProgressBar.setVisibility(View.VISIBLE);
	}

	private void hideProgressBar() {
		mProgressBar.setVisibility(View.INVISIBLE);
	}
	
	private void hideNoDataView() {
		mNoDataView.setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void afterSearchKeyChanged(final String searchKey) {
		clearData();
		mSearchHandler.searchLocalAndWeb(searchKey);
	}

	private void clearData() {
		GLAppDrawerSearchListAdapter listAdapter = (GLAppDrawerSearchListAdapter) mSearchListView
				.getAdapter();
		if (listAdapter != null) {
			listAdapter.clearInfoList();
			mSearchListView.setAdapter(null);
		}
		hideNoDataView();
		hideProgressBar();
	}
	
	public void showNoSearchHistory() {
		clearData();
		showNoDataView(ApplicationProxy.getContext().getString(
				com.gau.go.launcherex.R.string.appfunc_search_no_history));
	}
	
	public void notifyLocalSearchResultChanged(final ArrayList<Object> resultItems,
			final boolean isHistory) {
		post(new Runnable() {
			@Override
			public void run() {
				if (resultItems == null) {
					clearData();
					showNoDataView(ApplicationProxy.getContext().getString(
							com.gau.go.launcherex.R.string.appfunc_search_tip_no_match_data_web));
					return;
				}
				GLAppDrawerSearchListAdapter listAdapter = (GLAppDrawerSearchListAdapter) mSearchListView
						.getAdapter();
				if (listAdapter == null) {
					listAdapter = new GLAppDrawerSearchListAdapter();
				}
				listAdapter.setInfoList(resultItems);
				hideProgressBar();
				listAdapter.setShowHistory(isHistory);
				if (!isHistory) {
					PrivatePreference preference = PrivatePreference
							.getPreference(getApplicationContext());
					boolean show = preference.getBoolean(PrefConst.KEY_SHOW_APP_LOCATE_TIPS, true);
					if (show) {
						DeskToast.makeText(ApplicationProxy.getContext(),
								com.gau.go.launcherex.R.string.appfunc_search_locate_app,
								Toast.LENGTH_LONG).show();
						preference.putBoolean(PrefConst.KEY_SHOW_APP_LOCATE_TIPS, false);
						preference.commit();
					}
				}
				mSearchListView.setAdapter(listAdapter);
			}
		});
	}

	public void notifyWebSearchResultChanged(final ArrayList<Object> resultItems,
			final int insertPos) {
		post(new Runnable() {
			@Override
			public void run() {
				GLAppDrawerSearchListAdapter listAdapter = (GLAppDrawerSearchListAdapter) mSearchListView
						.getAdapter();
				hideProgressBar();
				if (listAdapter == null) {
					listAdapter = new GLAppDrawerSearchListAdapter();
					listAdapter.setInfoList(resultItems);
					mSearchListView.setAdapter(listAdapter);
				} else {
					listAdapter.addInfoList(resultItems, insertPos);
					listAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	@Override
	public void getMoreAppOnClick() {
		mSearchHandler.getMoreWebRes();
		RealTimeStatisticsUtil
				.upLoadAppDrawerSearch("more", mSearchInputBar.getEditTextSearchKey());
	}

	@Override
	public void onAppStart(String intent) {
		mSearchInputBar.showIM(false);
		mSearchHandler.saveSearchResultHistory(intent, AppfuncSearchEngine.HISTORY_TYPE_LOCAL);
	}

	@Override
	public void onMediaStart(String title) {
		mSearchHandler.saveSearchResultHistory(title, AppfuncSearchEngine.HISTORY_TYPE_LOCAL);
	}
	@Override
	protected void onVisibilityChanged(GLView changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
	}

	@Override
	public void setVisible(boolean visible, boolean animate, Object obj) {
		//		super.setVisible(visible, animate, obj);
		mSearchInputBar.clearAnimation();
		mSearchListView.clearAnimation();
		if (animate) {
			if (visible) {
				post(new Runnable() {

					@Override
					public void run() {
						if (mShell != null) {
							mShell.show(IViewId.PROTECTED_LAYER, false);
						}
						setVisible(true);
						Animation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
						alphaAnim.setDuration(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);

						Animation animation = new TranslateAnimation(0, 0,
								-mSearchInputBar.getHeight(), 0);
						animation.setDuration(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);

						alphaAnim.setAnimationListener(new AnimationListenerAdapter() {

							@Override
							public void onAnimationEnd(Animation animation) {
								if (mListener != null) {
									mListener.extendFuncViewOnEnter(GLAppDrawerSearchMainView.this);
								}
								mShell.hide(IViewId.PROTECTED_LAYER, false);
								setDrawingCacheEnabled(false);
								if (!mSearchHandler.isSearchMedia()) {
									dataInitFinish();
								}
								mSearchInputBar.showIM(true);
							}
						});
						GLAppDrawerSearchMainView.this.startAnimation(alphaAnim);
						mSearchInputBar.startAnimation(animation);
					}
				});
			} else {
				if (mListener != null) {
					mListener.extendFuncViewPreExit(this);
				}
				post(new Runnable() {

					@Override
					public void run() {
						if (mShell != null) {
							mShell.show(IViewId.PROTECTED_LAYER, false);
						}
					}
				});
				Animation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
				alphaAnim.setDuration(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
				Animation animation = new TranslateAnimation(0, 0, 0, -mSearchInputBar.getHeight());
				animation.setDuration(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
				if (obj instanceof RemoveViewAnimationListener) {
					alphaAnim.setAnimationListener((RemoveViewAnimationListener) obj);
				} else {
					alphaAnim.setAnimationListener(this);
				}
				mSearchInputBar.startAnimation(animation);
				startAnimation(alphaAnim);
			}
		}

	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK :
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
						ICommonMsgId.SHOW_EXTEND_FUNC_VIEW, 0, IViewId.APP_DRAWER_SEARCH);
				break;
			default :
				break;
		}
		return super.onKeyUp(keyCode, event);
	}
	
	public void dataInitFinish() {
		if (mSearchInputBar.getEditTextSearchKey() == null
				|| "".equals(mSearchInputBar.getEditTextSearchKey())) {
			mSearchHandler.getLocalSearchRecord();
		}
	}
	@Override
	public void onAdd(GLViewGroup parent) {
		super.onAdd(parent);
		RealTimeStatisticsUtil.upLoadAppDrawerSearch("g001");
	}
	@Override
	public void onRemove() {
		mSearchInputBar.setSearchKeyChangedListener(null);
		ResponseHandler.unregister(mSearchHandler);
		AppfuncSearchEngine.recyle();
		if (mSearchHandler.isSearchMedia()) {
			MediaFileSuperVisor.getInstance(ApplicationProxy.getContext()).destroyFileEngine();
			MediaFileSuperVisor.getInstance(ApplicationProxy.getContext()).setFileEngineDataRefreshListener(
					null);
		}
		ThumbnailManager.getInstance(ApplicationProxy.getContext()).clearAllObserver();
		MsgMgrProxy.unRegistMsgHandler(this);
		super.onRemove();
	}

	@Override
	public void clearHistoryOnClick() {
		clearData();
		mSearchHandler.clearSearchHistory();
		RealTimeStatisticsUtil
		.upLoadAppDrawerSearch("cl_his");
	}

	@Override
	public void clearSearchKey() {
		
	}
	
	@Override
	public boolean handleMessage(Object sender, int msgId, int param, Object... objects) {
		switch (msgId) {
			case IFrameworkMsgId.SYSTEM_ON_RESUME :
				OrientationControl.setOrientation(GoLauncherActivityProxy.getActivity(), param);
				break;
			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED :
				afterSearchKeyChanged(mSearchInputBar.getEditTextSearchKey());
				break;
			case IFrameworkMsgId.SYSTEM_ON_STOP:
				mSearchInputBar.showIM(false);
				break;
			default :
				break;
		}
		return false;
	}

	@Override
	public int getMsgHandlerId() {
		return IDiyFrameIds.APP_DRAWER_SEARCH;
	}

	@Override
	public void onScroll(GLAbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		GLAppDrawerSearchListAdapter adapter = (GLAppDrawerSearchListAdapter) mSearchListView
				.getAdapter();
		// 数据集最后一项的索引
		if (adapter == null) {
			return;
		}
		int lastIndex = adapter.getCount() - 1;
		if (mVisibleLastIndex == lastIndex) {
			// 如果是自动加载,可以在这里放置异步加载数据的代码

		}
	}

	@Override
	public void onScrollStateChanged(GLAbsListView view, int scrollState) {
		InputMethodManager methodManager = (InputMethodManager) ApplicationProxy.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL && methodManager != null
				&& methodManager.isActive()) {
			mSearchInputBar.showIM(false);
		}
	}

	@Override
	public void onAppLocated(String intent) {
		mSearchInputBar.showIM(false);
		mSearchHandler.saveSearchResultHistory(intent, AppfuncSearchEngine.HISTORY_TYPE_LOCAL);
	}
}
