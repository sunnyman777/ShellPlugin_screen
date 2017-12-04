package com.jiubang.shell.screen.zero.search;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.view.GLView.OnFocusChangeListener;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLAbsListView;
import com.go.gl.widget.GLAdapterView;
import com.go.gl.widget.GLAdapterView.OnItemClickListener;
import com.go.gl.widget.GLBaseAdapter;
import com.go.gl.widget.GLEditText;
import com.go.gl.widget.GLImageView;
import com.go.gl.widget.GLListView;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.util.AppUtils;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.zeroscreen.OnControlListener;
import com.jiubang.ggheart.zeroscreen.StatisticsUtils;
import com.jiubang.ggheart.zeroscreen.search.baidu.BaiduHotWordBean;
import com.jiubang.ggheart.zeroscreen.search.baidu.BaiduHotWordDataHelper;
import com.jiubang.ggheart.zeroscreen.search.bean.OnSearchListener;
import com.jiubang.ggheart.zeroscreen.search.bean.SearchResultInfo;
import com.jiubang.ggheart.zeroscreen.search.util.SearchUtils;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author liulixia
 *
 */
public class GLSearchLocalView extends GLLinearLayout
	implements OnSearchListener, OnClickListener, OnFocusChangeListener {
	public static final int MSG_SERACH_LOCAL_START = 0;
	public static final int MSG_SERACH_LOCAL_FINISH = 1;
	public static final int MSG_RESEARCH_VIEW = 2;
	public static final int MSG_GET_BAIDU_HOTWORD_SUCCESS = 3;
	
	private GLEditText mEditSearch = null;
	private GLListView mListView; // 弹出菜单项
	private MenuAdapter mMenuAdapter;
	private String[] mHistoryQueryTexts = null;
	private GLSearchLocalResultView mSearchLocalResultView = null;
	private SearchUtils mSearchUtils = null;
	private int mHistoryItemPadding = 0;
	private GLImageView mEditClean;
	private OnControlListener mControlListener;
	private String mHistorySearch = null;
	private boolean mHasKeyDown = false;
	private int mHistoryClearButtonWidth = 0;
	private int mHistoryClearButtonHeight = 0;
	protected GLLayoutInflater mInflater = null;

	private static final String BAIDU_SEARCH_URL = "http://m.baidu.com/s?from=1001148a&word=";
	
	//百度热词
	private GLLinearLayout mBaiduHotWordLayout = null;
	private ShellTextViewWrapper mLeftHotWord;
	private ShellTextViewWrapper mRightHotWord;
	private BaiduHotWordDataHelper mBaiduHotWordHelper = null;
	//判断是不是竖屏
	private boolean mIsPortrait = true;
		
	private GLImageView mEditImage;
	
	public GLSearchLocalView(Context context) {
		super(context);
		initView(context);
	}

	public GLSearchLocalView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	
	public void initView(Context context) {
		mSearchUtils = SearchUtils.getInstance(ShellAdmin.sShellManager.getActivity());
		String mContactTitle = mContext.getResources().getString(R.string.zero_screen_search_local_contact);
		String mAppTitle = mContext.getResources().getString(R.string.zero_screen_search_local_app);
		String mMoreContacts = mContext.getResources().getString(R.string.zero_screen_search_local_more_contacts);
		mSearchUtils.setTitiles(mContactTitle, mAppTitle, mMoreContacts);
		mBaiduHotWordHelper = BaiduHotWordDataHelper.getInstance();
		
		mInflater = ShellAdmin.sShellManager.getLayoutInflater();
		mInflater.inflate(R.layout.gl_zero_screen_search_local_view, this);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String text = "";
			int what = msg.what;
			switch (what) {
				case MSG_SERACH_LOCAL_START :
					break;
				case MSG_SERACH_LOCAL_FINISH :
					ArrayList<SearchResultInfo> results = (ArrayList<SearchResultInfo>) msg.obj;
					mSearchLocalResultView.onSearchFinish(results, mEditClean.getVisibility() == View.VISIBLE);
					Bundle data = msg.getData();
					text = data.getString("searchtext");
					showHistorySearchs(text);
					text = mEditSearch.getText().toString();
					mSearchUtils.searchLocalResources(text, false);
					break;
				case MSG_RESEARCH_VIEW :
					ArrayList<SearchResultInfo> refreshResults = (ArrayList<SearchResultInfo>) msg.obj;
					mSearchLocalResultView.onSearchFinish(refreshResults, mEditClean.getVisibility() == View.VISIBLE);
					text = mEditSearch.getText().toString();
					showHistorySearchs(text);
					break;
				case MSG_GET_BAIDU_HOTWORD_SUCCESS :
					ArrayList<BaiduHotWordBean> hotwords = (ArrayList<BaiduHotWordBean>) msg.obj;
					if (hotwords != null && hotwords.size() == 2) {
						BaiduHotWordBean bean = hotwords.get(0);
						mLeftHotWord.setText(bean.word);
						mLeftHotWord.setTag(bean.url);
						
						bean = hotwords.get(1);
						mRightHotWord.setText(bean.word);
						mRightHotWord.setTag(bean.url);
					}
					break;
				default :
					break;
			}
		};
	};

	private void showHistorySearchs(String s) {
		if (mHistoryQueryTexts != null && mHistoryQueryTexts.length > 0) {
			//只有搜索框未输入内容时才弹历史记录
			if (s == null || s.equals("")) {
				if (!isMenuShowing()) {
					mListView.setVisibility(View.VISIBLE);
					if (mMenuAdapter == null) {
						initMenuList(mHistoryQueryTexts);
					}
					mMenuAdapter.setItems(mHistoryQueryTexts);
					mMenuAdapter.notifyDataSetChanged();

				}
			} else {
				if (isMenuShowing()) {
					dismissMenu();
				}
			}
		}
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mEditSearch = (GLEditText) findViewById(R.id.edit_search);
		mEditClean = (GLImageView) findViewById(R.id.edit_search_clean);
		mEditClean.setOnClickListener(this);
		mEditClean.setVisibility(View.GONE);

		mEditImage = (GLImageView) findViewById(R.id.edit_search_image);
		if (mBaiduHotWordHelper.isHao360Search()) {
			mEditImage.setImageResource(R.drawable.gl_zero_screen_search_image_hao360);
		}
		
		// 百度热词
		mBaiduHotWordLayout = (GLLinearLayout) findViewById(R.id.baidu_hotword_layout);
		mLeftHotWord = (ShellTextViewWrapper) findViewById(R.id.hotword_left);
		mRightHotWord = (ShellTextViewWrapper) findViewById(R.id.hotword_right);
		
		mLeftHotWord.setClickable(true);
		mRightHotWord.setClickable(true);
		mLeftHotWord.setOnClickListener(this);
		mRightHotWord.setOnClickListener(this);
		mBaiduHotWordHelper.getHotWord(getContext(), mHandler);
				
		mSearchLocalResultView = (GLSearchLocalResultView) findViewById(R.id.search_local_result);
		mSearchLocalResultView.setVisibility(View.GONE);
		mListView = (GLListView) findViewById(R.id.history_search);
		mListView.setVisibility(View.GONE);
		mHistoryItemPadding = getResources().getDimensionPixelSize(
				R.dimen.zero_screen_search_local_history_list_item_padding);
		mHistoryClearButtonWidth = getResources().getDimensionPixelSize(R.dimen.zero_screen_search_local_history_clear_button_width);
		mHistoryClearButtonHeight = getResources().getDimensionPixelSize(R.dimen.zero_screen_search_local_history_clear_button_height);
		
		mHistorySearch = getResources().getString(R.string.zero_screen_search_local_history_text);

		String historyText = mSearchUtils.getHistorySearchText();
		onReloadHistoryText(true, historyText);
		mEditSearch.setOnFocusChangeListener(this);

		/**
		 *  国内包：3D零屏，搜索框，点击输入框搜索按钮没有跳转浏览器
			原因：由于直接设GLEditText的setOnKeyListener事件，插件未能监听到键盘事件，只有将GLEditText的EditText get出来设置键盘事件才能监听到。
		 */
		mEditSearch.getEditText().setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch (event.getAction()) {
					case KeyEvent.ACTION_DOWN :
						if (keyCode == KeyEvent.KEYCODE_ENTER) {
							InputMethodManager imm = (InputMethodManager) ApplicationProxy.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
							if (imm != null) {
								imm.hideSoftInputFromWindow(getWindowToken(), 0);
							}
							String searchWord = mEditSearch.getText().toString();
							mSearchUtils.saveSearchText();
							try {
								searchWord = URLEncoder.encode(searchWord, "UTF-8");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							if (mBaiduHotWordHelper.isHao360Search()) {
								String url = BaiduHotWordDataHelper.HAO360_HOT_WORD_SEARCH_URL.replace("XXX", searchWord);
								AppUtils.gotoBrowser(GoLauncherActivityProxy.getActivity(), url);
							} else {
								AppUtils.gotoBrowser(GoLauncherActivityProxy.getActivity(), BAIDU_SEARCH_URL + searchWord);
							}
							return true;
						}
						break;
					default :
						break;
				}
				return false;
			}
		});

		mEditSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (mSearchLocalResultView.getVisibility() == View.VISIBLE) {
					goToSearch(s.toString());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}
	
	public void setControlListener(OnControlListener listener) {
		mControlListener = listener;
	}

	private void goToSearch(String s) {
		if (!s.equals("")) {
			if (mEditClean.getVisibility() == View.GONE) {
				mEditClean.setVisibility(View.VISIBLE);
			}
		} else {
			if (mEditClean.getVisibility() == View.VISIBLE) {
				mEditClean.setVisibility(View.GONE);
			}
		}
		mSearchUtils.searchLocalResources(s, false);
	}

	/**
	 * 初始化菜单项
	 * */
	private void initMenuList(String[] menuItemNames) {
		GLLinearLayout layout = new GLLinearLayout(mContext);
		GLAbsListView.LayoutParams params = new GLAbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		layout.setLayoutParams(params);
		layout.setGravity(Gravity.CENTER_HORIZONTAL);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		ShellTextViewWrapper clearButton = new ShellTextViewWrapper(mContext);
		clearButton.setText(mHistorySearch);
		clearButton.setTextColor(Color.parseColor("#71a901"));
		clearButton.setTextSize(14f);
		clearButton.setGravity(Gravity.CENTER);
		clearButton.setPadding(0, 15, 0, 15);
		clearButton.setBackgroundResource(R.drawable.gl_zero_screen_search_history_clear_button);
		clearButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(GLView arg0) {
				mSearchUtils.deleteHistorySearchText("*deleteAllText*");
			}
		});
		LayoutParams paramsButton = new LayoutParams(mHistoryClearButtonWidth, mHistoryClearButtonHeight);
		paramsButton.topMargin = mHistoryItemPadding;
		paramsButton.bottomMargin = mHistoryItemPadding;
		paramsButton.gravity = Gravity.CENTER;
		clearButton.setLayoutParams(paramsButton);
		layout.addView(clearButton);
		mListView.addFooterView(layout);
		
		mMenuAdapter = new MenuAdapter(menuItemNames);
		mListView.setAdapter(mMenuAdapter);
		mListView.setCacheColorHint(Color.TRANSPARENT);
		mListView.setClickable(true);
		mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mListView.setDivider(null);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(GLAdapterView<?> arg0, GLView arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				if (isMenuShowing()) {
					dismissMenu();
				}
				HistoryItemView tag = (HistoryItemView) arg1.getTag();
				String text = tag.mHistoryText.getText().toString();
				mEditSearch.setText(text);
				mEditSearch.focusSearch(text.length());
			}
		});
	}

	/**
	 * 判断历史条目菜单是否显示
	 * */
	private boolean isMenuShowing() {
		return mListView.getVisibility() == View.VISIBLE;
	}

	/**
	 * 取消菜单
	 * */
	public void dismissMenu() {
		mListView.setVisibility(View.GONE);
	}

	/**
	 * 菜单项适配器
	 * */
	class MenuAdapter extends GLBaseAdapter {

		private String[] mItems;

		public MenuAdapter(String[] items) {
			this.mItems = items;
		}

		public void setItems(String[] items) {
			this.mItems = items;
		}

		@Override
		public int getCount() {
			return mItems == null ? 0 : mItems.length;
		}

		@Override
		public Object getItem(int position) {

			return mItems == null ? null : mItems[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public GLView getView(int arg0, GLView arg1, GLViewGroup arg2) {
			// TODO Auto-generated method stub
			if (mItems == null) {
				return arg1;
			}
			HistoryItemView itemView = null;
			if (arg1 == null) {
				arg1 = mInflater.inflate(R.layout.gl_zero_screen_search_local_history_item_view, null);
				itemView = new HistoryItemView();
				itemView.mHistoryText = (ShellTextViewWrapper) arg1.findViewById(R.id.search_text);
				itemView.mHistoryDeleteButton = (GLImageView) arg1.findViewById(R.id.search_text_delete);
				arg1.setTag(itemView);
			} else {
				itemView = (HistoryItemView) arg1.getTag();
			}
			
			final String text = mItems[arg0];
			itemView.mHistoryText.setText(text);
			itemView.mHistoryDeleteButton.setTag(text);
			itemView.mHistoryDeleteButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(GLView arg0) {
					mSearchUtils.deleteHistorySearchText(text);
				}
			});
			return arg1;
		}
	}
	
	/**
	 * 
	 * @author liulixia
	 *
	 */
	class HistoryItemView {
		public ShellTextViewWrapper mHistoryText;
		public GLImageView mHistoryDeleteButton;
	}

	@Override
	public void onSearchStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSearchFinish(String searchText, ArrayList<SearchResultInfo> results) {
		Message msg = mHandler.obtainMessage();
		msg.what = MSG_SERACH_LOCAL_FINISH;
		Bundle bundle = new Bundle();
		bundle.putString("searchtext", searchText);
		msg.setData(bundle);
		msg.obj = results;
		mHandler.sendMessage(msg);
	}

	@Override
	public void onRefreshList(ArrayList<SearchResultInfo> results) {
		Message msg = mHandler.obtainMessage();
		msg.what = MSG_RESEARCH_VIEW;
		msg.obj = results;
		mHandler.sendMessage(msg);
	}

	@Override
	public void onReloadHistoryText(boolean isAdded, String history) {
		if (isAdded) {
			// isAdded为true,表示点击了搜索项，把搜索词条保存起来，这时需把键盘收起来
			InputMethodManager imm = (InputMethodManager) ApplicationProxy.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				imm.hideSoftInputFromWindow(getWindowToken(), 0);
			}
		}
		if (history != null && !history.equals("")) {
			String[] items = history.split(",");
			int length = items.length;
			mHistoryQueryTexts = new String[length];
			for (int i = 0; i < length; i++) {
				mHistoryQueryTexts[i] = items[i];
			}
		} else {
			mHistoryQueryTexts = null;
		}
		
		// 正在显示历史记录，需重刷数据
		if (isMenuShowing()) {
			if (mHistoryQueryTexts == null) {
				mListView.setVisibility(View.GONE);
			}
			mMenuAdapter.setItems(mHistoryQueryTexts);
			mMenuAdapter.notifyDataSetChanged();
		}
	}

	
	public void onKeyBack() {
		clearSearchResults(false);
	}

	public void clearSearchResults(boolean needChangeHotwords) {
		if (mSearchLocalResultView.getVisibility() == View.VISIBLE) {
			mSearchLocalResultView.recyle();
			mSearchUtils.removeListener();
			mControlListener.showOrHideTabLayout(true, false);
			mSearchLocalResultView.setVisibility(View.GONE);
			mEditSearch.setText("");
			if (mEditSearch.hasFocus()) {
				mEditSearch.clearFocus();
			}
			dismissMenu();
			mEditClean.setVisibility(View.GONE);
			if (!mIsPortrait) {
				if (mBaiduHotWordLayout.getVisibility() == View.VISIBLE) {
					mBaiduHotWordLayout.setVisibility(View.GONE);
				}
			} else {
				mBaiduHotWordLayout.setVisibility(View.VISIBLE);
			}
			mBaiduHotWordHelper.getHotWord(getContext(), mHandler);
			return;
		}
		if (needChangeHotwords) {
			mBaiduHotWordHelper.getHotWord(getContext(), mHandler);
		}
	}

	
	public void enterToZeroScreen() {
		// TODO Auto-generated method stub
		
	}

	
	public void leaveToZeroScreen() {
		InputMethodManager imm = (InputMethodManager) ApplicationProxy.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(getWindowToken(), 0);
		}
		clearSearchResults(true);
	}

	
	public void onDestory() {
		removeAllViews();
		if (mSearchLocalResultView != null) {
			mSearchLocalResultView.onDestory();
			mSearchLocalResultView = null;
		}
		mSearchUtils = null;
		mEditSearch = null;
		mHistoryQueryTexts = null;
		if (mMenuAdapter != null) {
			mMenuAdapter.mItems = null;
			mMenuAdapter = null;
		}
		mListView.setAdapter(null);
		mBaiduHotWordHelper.clear();
		mBaiduHotWordHelper = null;
		mHandler = null;
	}

	public void onConfigurationChanged(boolean hideSofeInput, boolean isPortrait) {
		mIsPortrait = isPortrait;
		if (hideSofeInput && mSearchLocalResultView != null 
				&& mSearchLocalResultView.getVisibility() == View.VISIBLE) {
			InputMethodManager imm = (InputMethodManager) ApplicationProxy.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				imm.hideSoftInputFromWindow(getWindowToken(), 0);
			}
		}
		
		if (!isPortrait) {
			if (mBaiduHotWordLayout.getVisibility() == View.VISIBLE) {
				mBaiduHotWordLayout.setVisibility(View.GONE);
			}
		} else {
			if (mSearchLocalResultView.getVisibility() == View.GONE) {
				mBaiduHotWordLayout.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onFocusChange(GLView arg0, boolean arg1) {
		if (arg1) {
			// 显示历名搜索条词
			mControlListener.showOrHideTabLayout(false, false);
			mSearchLocalResultView.setVisibility(View.VISIBLE);
			mBaiduHotWordLayout.setVisibility(View.GONE);
			// 获取到焦点时添加监听器
			mSearchUtils.setSearchListener(this);
			// 保存点击数
			GuiThemeStatistics.getInstance(mContext).guiStaticData(57,
					"", StatisticsUtils.SEARCH_ADR, 1,
					"0", "1", "", "");
			String text = mEditSearch.getText().toString();
			goToSearch(text);
		}
		
	}

	@Override
	public void onClick(GLView arg0) {
		int viewId = arg0.getId();
		switch (viewId) {
			case R.id.hotword_left :
			case R.id.hotword_right :
				String url = (String) arg0.getTag();
				if (url != null && !url.equals("")) {
					AppUtils.gotoBrowser(mContext, url);
				}
				break;
			case R.id.edit_search_clean : 
				mEditSearch.setText("");
				mEditClean.setVisibility(View.GONE);
				break;
			default :
				break;
		}
	}
}
