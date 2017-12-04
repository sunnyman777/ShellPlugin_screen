package com.jiubang.shell.screen.zero.navigation;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLRelativeLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.view.GLViewWrapper;
import com.go.gl.widget.GLAdapterView;
import com.go.gl.widget.GLAdapterView.OnItemClickListener;
import com.go.gl.widget.GLAdapterView.OnItemLongClickListener;
import com.go.gl.widget.GLGridView;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.VersionControl;
import com.go.util.device.Machine;
import com.go.util.file.FileUtil;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.tuiguanghuodong.double11.HttpDownloader;
import com.jiubang.ggheart.tuiguanghuodong.double11.PromotionController;
import com.jiubang.ggheart.zeroscreen.StatisticsUtils;
import com.jiubang.ggheart.zeroscreen.navigation.AddSiteActivity;
import com.jiubang.ggheart.zeroscreen.navigation.bean.ZeroScreenAdInfo;
import com.jiubang.ggheart.zeroscreen.navigation.bean.ZeroScreenAdSuggestSiteInfo;
import com.jiubang.ggheart.zeroscreen.navigation.data.NavigationController;
import com.jiubang.ggheart.zeroscreen.navigation.data.SuggestSiteObtain;
import com.jiubang.ggheart.zeroscreen.navigation.data.ToolUtil;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * <br>
 * 类描述:0屏view <br>
 * 功能详细描述:
 * 
 * @author licanhui
 * @date [2013-8-15]
 */
public class GLNavigationView extends GLLinearLayout implements
		OnItemClickListener, OnItemLongClickListener, OnClickListener {

	private GLGridView mGridView;
	private GLAdAdapter mGridViewAdapter;
	private ArrayList<ZeroScreenAdInfo> mZeroScreenAdInfos;
	protected GLLayoutInflater mInflater = null;

	public static boolean sISLONGCLICK = false; // 长按事件标志
//	protected Context mContext;
	private PreferencesManager mPreferenceManager;
	private GLAdItemView mOnclickView; // 被点击的view
//	private ScrollView mScrollView;
	private GLLinearLayout mTopLayout;
	private GLLinearLayout mBottomLayout;
    private GLLinearLayout mWebsLayout;
    private GLViewWrapper mGlWrapper;
	private WebView mWebView;
	private int mClickPosition = -1;
	// private RelativeLayout mListTitleLayout;
	private Handler mHandler;
	private ArrayList<ZeroScreenAdSuggestSiteInfo> mHotList;
	public static final int KSUGGESTION_OBTAIN_SUCCESS = 1;

	private GLRelativeLayout mNavigationAdLayout = null;
	
	private void initHandler() {
		mHandler = new Handler(new Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {

				// 接收到“获取完所有的top网站数据”消息
				case KSUGGESTION_OBTAIN_SUCCESS:
					mHotList = SuggestSiteObtain.getInstance()
							.getZeroScreenAdSuggestSiteInfoList(mContext);
					loadHtmlFromServer(SuggestSiteObtain.getInstance()
							.getWebUrl());
					if (mHotList != null && mHotList.size() != 0) {
						SuggestSiteObtain.getInstance().updateTime();
						// 填充数据
						makeZeroAdInfoList(mHotList);
						if (mGridView != null && mGridViewAdapter != null) {
							mGridViewAdapter.refreshData(mZeroScreenAdInfos);
							mGridViewAdapter.notifyDataSetChanged();

						}
					}
					return true;
				default:
					break;
				}

				return false;
			}
		});

	}

	public GLNavigationView(Context context) {
		super(context);
		initView(context);
	}

	public GLNavigationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		mGridView = (GLGridView) findViewById(R.id.ad_grid_view);
		mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mGridView.setOnItemClickListener(this);
		mGridView.setOnItemLongClickListener(this);
		mGridViewAdapter = new GLAdAdapter(mContext);
		mGridViewAdapter.refreshData(mZeroScreenAdInfos);
		mGridView.setAdapter(mGridViewAdapter);
		mTopLayout = (GLLinearLayout) findViewById(R.id.top);
		mBottomLayout = (GLLinearLayout) findViewById(R.id.bottom);
		mTopLayout.setVisibility(View.GONE);
		mBottomLayout.setVisibility(View.GONE);
		mTopLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(GLView v) {
				// TODO Auto-generated method stub
				cancelKillDrawable();
			}
		});
		mBottomLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(GLView v) {
				// TODO Auto-generated method stub
				cancelKillDrawable();
			}
		});
		mNavigationAdLayout = (GLRelativeLayout) findViewById(R.id.navigation_layout);
		mWebsLayout  = (GLLinearLayout) findViewById(R.id.web_layout);
		mWebView = new WebView(ApplicationProxy.getContext());
		initWebView(mContext);
	}

	private void makeZeroAdInfoList(ArrayList<ZeroScreenAdSuggestSiteInfo> list) {

		// mZeroScreenAdInfos.clear();

		boolean isOperator = false;
		int num = 6;

		if (mPreferenceManager != null) {
			isOperator = mPreferenceManager.getBoolean(
					IPreferencesIds.PREFERENCE_ZERO_SCREEN_IS_BE_OPERTATOR,
					false);
		}

		if (isOperator) {
			num = 4;
		}

		for (int i = 0; i < num; i++) {
			ZeroScreenAdSuggestSiteInfo sugInfo = list.get(i);
			ZeroScreenAdInfo info = new ZeroScreenAdInfo();
			info.mTitle = sugInfo.mTitle;
			info.mUrl = ToolUtil.HTTPHEAD.concat(sugInfo.mUrl);
			info.mDomain = sugInfo.mDomain;
			info.mDesignedColor = sugInfo.mBackColor;
			info.mLogoIcon = null;
			info.mCustomColor = -1;
			info.mPosition = i;
			info.mIsPlus = false;
			info.mIsRecommend = false;
			info.mIsShowDel = false;
			mZeroScreenAdInfos.remove(i);
			mZeroScreenAdInfos.add(i, info);
			NavigationController.getInstance(mContext).updateZeroScreenAdInfo(
					info);
		}
		addPlusIcon(mZeroScreenAdInfos.size(), false);
	}

	private void addPlusIcon(int size, boolean isInit) {
		if (size <= 7) {
			for (int i = 0; i < 8 - size; i++) {
				ZeroScreenAdInfo info = new ZeroScreenAdInfo();
				info.setInfoNull(size + i);
				mZeroScreenAdInfos.add(info);
				if (isInit) {
					NavigationController.getInstance(mContext)
							.insertZeroScreenAdInfo(info);
				} else {
					NavigationController.getInstance(mContext)
							.updateZeroScreenAdInfo(info);
				}
			}
		}
	}

	public void initView(Context context) {
		mContext = context;
		mInflater = ShellAdmin.sShellManager.getLayoutInflater();
		initHandler();
		mPreferenceManager = new PreferencesManager(mContext);
		SuggestSiteObtain.getInstance()
				.startgetSuggestSites(mContext, mHandler);
		mZeroScreenAdInfos = NavigationController.getInstance(mContext)
				.createZeroScreenAdInfo();
		addPlusIcon(mZeroScreenAdInfos.size(), true);
		mInflater.inflate(R.layout.gl_zero_screen_navigation_view, this);
	}

	// }
	public void initWebView(Context context) {
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		mWebView.loadUrl("file:///android_asset/zero_webview.html");

		boolean ret = false;
		
		//判断sd卡里面是否已经有这个网页
		File file  = new File(PromotionController.PROMOTION_FILE_PATH + File.separator
				+ "zero_webview" + ".html");
		
		if (file.exists() && !VersionControl.getFirstRun() && Machine.isNetworkOK(context)) {
			mWebView.loadUrl(WEBVIEW_HTML_DIR);
			ret = true;
		}

		//如果没有，且存在sd卡则去下载
		if (!ret) {
			String url = getWebUrl(context);
			if (FileUtil.isSDCardAvaiable() && Machine.isNetworkOK(context)
					&& url != null) {
				loadHtmlFromServer(url);
			} else if (Machine.isNetworkOK(context) && url != null) {
				mWebView.loadUrl(url);
			}
		}
		
		mGlWrapper = new GLViewWrapper(mContext);
		mGlWrapper.setView(mWebView, null);
    	mWebsLayout.addView(mGlWrapper);
	}

	private ArrayList<GLAdItemView> getAllItems() {
		ArrayList<GLAdItemView> adIconsList = new ArrayList<GLAdItemView>();
		for (int i = 0; i < mGridView.getChildCount(); i++) {
			adIconsList.add((GLAdItemView) mGridView.getChildAt(i));
		}
		return adIconsList;
	}

	
	@Override
	public boolean onItemLongClick(GLAdapterView<?> arg0, GLView view,
			int position, long id) {
		// TODO Auto-generated method stub
		if (position < 4) {
			return true;
		}
		GLAdItemView icon = getAllItems().get(position);
		if (!icon.getAdBean().mIsPlus) {
			sISLONGCLICK = true;
			this.setBackgroundColor(Color.parseColor("#80000000"));
			ArrayList<GLAdItemView> list = getAllItems();
			for (int i = 4; i < list.size(); i++) {
				GLAdItemView item = list.get(i);
				if (!item.getAdBean().mIsPlus) {
					item.setOnClick(this);
					item.isShowKillDrawable(true);
				}
				mTopLayout.setVisibility(View.VISIBLE);
				mBottomLayout.setVisibility(View.VISIBLE);
			}

		}

		return true;
	}

	@Override
	public void onItemClick(GLAdapterView<?> arg0, GLView view, int position,
			long id) {
		// TODO Auto-generated method stub
		if (sISLONGCLICK) {
			return;
		}

		if (mGridViewAdapter != null) {
			mGridViewAdapter.notifyDataSetChanged();
		}

		mOnclickView = getAllItems().get(position);
		ZeroScreenAdInfo info = mOnclickView.getAdBean();
		if (!info.mIsPlus) {
			GuiThemeStatistics.getInstance(mContext).guiStaticData(
					57,
					info.mUrl,
					StatisticsUtils.URL_GO,
					1,
					"0",
					"2",
					String.valueOf(info.mPosition),
					info.mTitle + StatisticsUtils.PROTOCOL_DIVIDER
							+ (info.mIsRecommend ? "1" : "2"));
			Uri uri = Uri.parse(mOnclickView.getAdBean().mUrl);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			mContext.startActivity(intent);
		} else {
			Intent intent = new Intent(ApplicationProxy.getContext(), AddSiteActivity.class);
			intent.putExtra(ZERO_SCREEN_AD_POSITION_ONE,
					mZeroScreenAdInfos.get(0).mUrl);
			intent.putExtra(ZERO_SCREEN_AD_POSITION_TWO,
					mZeroScreenAdInfos.get(1).mUrl);
			intent.putExtra(ZERO_SCREEN_AD_POSITION_THREE,
					mZeroScreenAdInfos.get(2).mUrl);
			intent.putExtra(ZERO_SCREEN_AD_POSITION_FOUR,
					mZeroScreenAdInfos.get(3).mUrl);
			intent.putExtra(ZERO_SCREEN_AD_POSITION_FIVE,
					mZeroScreenAdInfos.get(4).mUrl);
			intent.putExtra(ZERO_SCREEN_AD_POSITION_SIX,
					mZeroScreenAdInfos.get(5).mUrl);
			intent.putExtra(ZERO_SCREEN_AD_POSITION_SEVEN,
					mZeroScreenAdInfos.get(6).mUrl);
			intent.putExtra(ZERO_SCREEN_AD_POSITION_EIGHT,
					mZeroScreenAdInfos.get(7).mUrl);
			intent.putExtra(ZERO_SCREEN_AD_POSITION, position);
			mClickPosition = position;
			mContext.startActivity(intent);
		}

	}

	// 重置状态
	public void cancelKillDrawable() {

		ArrayList<GLAdItemView> list = getAllItems();
		for (int i = 4; i < list.size(); i++) {
			GLAdItemView item = list.get(i);
			item.isShowKillDrawable(false);
		}
		sISLONGCLICK = false;
		mTopLayout.setVisibility(View.GONE);
		mBottomLayout.setVisibility(View.GONE);
		this.setBackgroundColor(Color.TRANSPARENT);
	}

	// 重置状态
	public void cancelKillDrawable(int position) {

		ArrayList<GLAdItemView> list = getAllItems();
		list.get(position).setPlusDrawable();
		list.get(position).isShowKillDrawable(false);

		int k = 0;
		for (int i = 0; i < mZeroScreenAdInfos.size(); i++) {
			if (mZeroScreenAdInfos.get(i).mIsPlus) {
				k++;
			}
		}

		if (k == 4) {
			sISLONGCLICK = false;
			mTopLayout.setVisibility(View.GONE);
			mBottomLayout.setVisibility(View.GONE);
			this.setBackgroundColor(Color.TRANSPARENT);
		}
	}

	// 删除一个网址
	private void deleteWeb(GLView v) {
		ZeroScreenAdInfo delBean = ((GLAdItemView) v).getAdBean();
		GuiThemeStatistics.getInstance(mContext).guiStaticData(
				57,
				delBean.mUrl,
				StatisticsUtils.URL_DEL,
				1,
				"0",
				"2",
				String.valueOf(delBean.mPosition),
				delBean.mTitle + StatisticsUtils.PROTOCOL_DIVIDER
						+ (delBean.mIsRecommend ? "1" : "2"));
		delBean.setInfoNull(delBean.mPosition);
		cancelKillDrawable(delBean.mPosition);
		mGridViewAdapter.refreshData(mZeroScreenAdInfos);
		// mGridViewAdapter.notifyDataSetChanged();
		NavigationController.getInstance(mContext).updateZeroScreenAdInfo(
				delBean);
		if (mPreferenceManager != null) {
			mPreferenceManager.putBoolean(
					IPreferencesIds.PREFERENCE_ZERO_SCREEN_IS_BE_OPERTATOR,
					true);
			mPreferenceManager.commit();
		}

	}

	// 增加一个新的网址
	public void addNewWeb(Object info) {
		ZeroScreenAdInfo zero = (ZeroScreenAdInfo) info;
		ZeroScreenAdInfo nullBean = mZeroScreenAdInfos.get(zero.mPosition);
		nullBean.mTitle = zero.mTitle;
		nullBean.mIsPlus = false;
		nullBean.mUrl = zero.mUrl;
		nullBean.mIsRecommend = zero.mIsRecommend;
		nullBean.mPosition = zero.mPosition;
		nullBean.mCustomColor = zero.mCustomColor;
		mGridViewAdapter.refreshData(mZeroScreenAdInfos);
		mGridViewAdapter.notifyDataSetChanged();
		NavigationController.getInstance(mContext).updateZeroScreenAdInfo(
				(ZeroScreenAdInfo) info);
		if (mPreferenceManager != null) {
			mPreferenceManager.putBoolean(
					IPreferencesIds.PREFERENCE_ZERO_SCREEN_IS_BE_OPERTATOR,
					true);
			mPreferenceManager.commit();
		}
	}

	public void onKeyBack() {
		// TODO Auto-generated method stub

		cancelKillDrawable();
	}

	public void enterToZeroScreen() {
		// TODO Auto-generated method stub
		if (mGridViewAdapter != null) {
			mGridViewAdapter.notifyDataSetChanged();
		}
	}

	public void leaveToZeroScreen() {
		// TODO Auto-generated method stub
		cancelKillDrawable();
	}

	@Override
	public void onClick(GLView v) {
		// TODO Auto-generated method stub
		deleteWeb(v);
	}

	public void onDestory() {
		// TODO Auto-generated method stub
		mZeroScreenAdInfos = null;
		mGridView = null;
		mGridViewAdapter = null;
		mOnclickView = null;
	}

	public void changeNumColumns(int numColumns) {
		/*if (mScrollView != null) {
			mScrollView.fullScroll(ScrollView.FOCUS_UP);
		}*/
		if (mGridView != null) {
			mGridView.setNumColumns(numColumns);
			if (numColumns == 4) {
				android.view.ViewGroup.LayoutParams mp = mGridView
						.getLayoutParams();
				mp.height = getResources().getDimensionPixelSize(
						R.dimen.zero_screen_ad_port_height);
				mGridView.setLayoutParams(mp);
			} else {
				android.view.ViewGroup.LayoutParams mp = mGridView
						.getLayoutParams();
				mp.height = getResources().getDimensionPixelSize(
						R.dimen.zero_screen_ad_land_height);
				mGridView.setLayoutParams(mp);
			}

		}

		int topHeight = DrawUtils.dip2px(86f);
		if (numColumns != 4) {
			topHeight = DrawUtils.dip2px(59f);
		}
		
		if (mNavigationAdLayout != null) {
			GLRelativeLayout.LayoutParams params = (GLRelativeLayout.LayoutParams) mNavigationAdLayout.getLayoutParams();
			params.topMargin = topHeight;
			mNavigationAdLayout.setLayoutParams(params);
		}
		
		if (mTopLayout != null) {
			GLRelativeLayout.LayoutParams params = (GLRelativeLayout.LayoutParams) mTopLayout.getLayoutParams();
			params.height = topHeight;
			mTopLayout.setLayoutParams(params);
		}
	}

	public void showKillDrawable() {
		if (sISLONGCLICK) {
			postDelayed(new Runnable() {
				@Override
				public void run() {
					drawableState();
				}
			}, 100);
		}
	}

	public void drawableState() {
		ArrayList<GLAdItemView> list = getAllItems();
		for (int i = 4; i < list.size(); i++) {
			GLAdItemView item = list.get(i);
			item.isShowKillDrawable(true);
			item.setOnClick(new OnClickListener() {

				@Override
				public void onClick(GLView v) {
					// TODO Auto-generated method stub
					deleteWeb(v);
				}
			});
		}
		mTopLayout.setVisibility(View.VISIBLE);
		mBottomLayout.setVisibility(View.VISIBLE);
	}

	public ArrayList<ZeroScreenAdInfo> getZeroScreenAdInfos() {
		return mZeroScreenAdInfos;
	}

	public int getClickPosition() {
		return mClickPosition;
	}

	private String getWebUrl(Context context) {
		return mPreferenceManager.getString(
				IPreferencesIds.PREFERENCE_ZERO_SCREEN_WEB_VIEW_URL, null);
	}
	
	public final static String WEBVIEW_HTML_DIR = "file://"
			+ PromotionController.PROMOTION_FILE_PATH + File.separator
			+ "zero_webview" + ".html";

	
	
	
	private void loadHtmlFromServer(final String url) {
		if (mWebView == null) {
			return;
		}
		if (FileUtil.isSDCardAvaiable()) {
			new Thread() {
				@Override
				public void run() {
					HttpDownloader downloader = new HttpDownloader();
					downloader.setIsOverrided(true);
					boolean download = downloader.downfile(url,
							PromotionController.PROMOTION_FILE_PATH,
							"zero_webview" + ".html");
					if (download) {
						mWebView.loadUrl(WEBVIEW_HTML_DIR);
					}

				}
			}.start();
		} else {
			mWebView.loadUrl(url);
		}

	}


	public static final String ZERO_SCREEN_AD_POSITION = "zero_ad_screen_position";

	public static final String ZERO_SCREEN_AD_POSITION_ONE = "zero_screen_ad_position_one";
	public static final String ZERO_SCREEN_AD_POSITION_TWO = "zero_screen_ad_position_two";
	public static final String ZERO_SCREEN_AD_POSITION_THREE = "zero_screen_ad_position_three";
	public static final String ZERO_SCREEN_AD_POSITION_FOUR = "zero_screen_ad_position_four";
	public static final String ZERO_SCREEN_AD_POSITION_FIVE = "zero_screen_ad_position_five";
	public static final String ZERO_SCREEN_AD_POSITION_SIX = "zero_screen_ad_position_six";
	public static final String ZERO_SCREEN_AD_POSITION_SEVEN = "zero_screen_ad_position_seven";
	public static final String ZERO_SCREEN_AD_POSITION_EIGHT = "zero_screen_ad_position_eight";

	public static final String ZERO_SCREEN_POSITION_AD_ONE = "aiTaobao";
	public static final String ZERO_SCREEN_POSITION_AD_TWO = "amazon";
	public static final String ZERO_SCREEN_POSITION_AD_THREE = "3gmenhu";
	public static final String ZERO_SCREEN_POSITION_AD_FOUR = "taobao";
	public static final String ZERO_SCREEN_POSITION_AD_FIVE = "youyuan";
	public static final String ZERO_SCREEN_POSITION_AD_SIX = "baidu ";

	public static final String ZERO_SCREEN_POSITION_AD_ONE_WEBURL = ToolUtil.HTTPHEAD
			.concat("r.m.taobao.com/m3?p=mm_34021965_3435520_11130726&c=1002");
	public static final String ZERO_SCREEN_POSITION_AD_TWO_WEBURL = ToolUtil.HTTPHEAD
			.concat("www.amazon.cn/gp/aw?tag=3gnav-23");
	public static final String ZERO_SCREEN_POSITION_AD_SIX_WEBURL = ToolUtil.HTTPHEAD
			.concat("m.baidu.com/s?from=1001148a&word=");
	public static final String ZERO_SCREEN_POSITION_AD_FIVE_WEBURL = ToolUtil.HTTPHEAD
			.concat("da.3g.cn/jbad/adv.php?id=8075&releaseid=80751&chid=27&seatid=514&waped=9&rd=119");
	public static final String ZERO_SCREEN_POSITION_AD_THREE_WEBURL = ToolUtil.HTTPHEAD
			.concat("xuan.3g.cn/index.php?fr=golaunch0");
	public static final String ZERO_SCREEN_POSITION_AD_FOUR_WEBURL = ToolUtil.HTTPHEAD
			.concat("goappcenter.3g.net.cn/recommendedapp/redirect.do?from=taobao");
	
	
}
