package com.jiubang.shell.screenedit.tabs;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;

import com.gau.golauncherex.plugin.shell.R;
import com.go.commomidentify.IGoLauncherClassName;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.animation.Translate3DAnimation;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.widget.GLImageView;
import com.go.proxy.MsgMgrProxy;
import com.go.util.AppUtils;
import com.go.util.file.FileUtil;
import com.go.util.graphics.DrawUtils;
import com.go.util.market.MarketConstant;
import com.golauncher.message.IAppCoreMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenEditMsgId;
import com.golauncher.message.IScreenFrameMsgId;
import com.golauncher.message.IWidgetMsgId;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConfig;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.gowidget.AbsWidgetInfo;
import com.jiubang.ggheart.apps.gowidget.BaseWidgetInfo;
import com.jiubang.ggheart.apps.gowidget.GoWidgetConstant;
import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.ggheart.apps.gowidget.GoWidgetProviderInfo;
import com.jiubang.ggheart.apps.gowidget.InnerWidgetInfo;
import com.jiubang.ggheart.apps.gowidget.InnerWidgetParser;
import com.jiubang.ggheart.apps.gowidget.WidgetParseInfo;
import com.jiubang.ggheart.apps.gowidget.gostore.GoStoreChannelControl;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;
import com.jiubang.ggheart.data.statistics.GLGoWeatherStatisticsUtil;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.XmlParserFactory;
import com.jiubang.ggheart.data.theme.bean.PreviewSpecficThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;
import com.jiubang.ggheart.data.theme.parser.ParseSpecficWidgetTheme;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.shell.common.component.GLDragLayer;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.screen.CellUtils;
import com.jiubang.shell.screenedit.TabFactory;
import com.jiubang.shell.screenedit.bean.GoWidgetInfo;
import com.jiubang.shell.widget.GoWidgetActionReceiver;

/**
 * 
 * @author zouguiquan
 *
 */
public class GLGoWidgetSubTab extends GLWidgetPrevTab
		implements
			BatchAnimationObserver,
			OnClickListener {

	private static final String THEME_CATEGORY = "android.intent.category.DEFAULT"; // 主题包category
	private static final String WIDGET_PACKAGE_PREFIX = "com.gau.go.launcherex.gowidget.";

	private GLView mFlyView;
	private boolean mNeedConfig;									// 是否需要带设置的
	private String mConfigActivity = "";							// 带有设置的widget的activity名

	private int[] mRowLists = null; 								// 处理单个widget部分,几行
	private int[] mColLists = null; 								// 处理单个widget部分,几列
	private int[] mStyleTypeList = null; 							// widget样式数目	
	private final static float DEFAULT_DENSITY = 1.5F; 				// 480x800下的density

	private BaseWidgetInfo mDisplayInfo;
	//WidgetParseInfo
	private ArrayList<Object> mWidgetDatasScan;						// widget默认风格的样式列表数据
	//GoWidgetInfo
	private ArrayList<Object> mWidgetDatasDetail;					// 扫描出来有几套风格皮肤数据
	//LinkedList<GoWidgetInfo>
	private ConcurrentHashMap<Integer, ArrayList<Object>> mDatasDetail;	// 这个gowidget对应的每套风格皮肤的所有数据

	private boolean mHasLoaded = false;

	private int mSkinIndex = 0;
	private int mWidgetSkinType = DEFAULT_SKIN;
	private final static int DEFAULT_SKIN = 1000;
	private final static int GOWIDGET_SKIN = 1001;
	private final static int ON_FLY_FINISH = 4000;					// 添加动画完成时

	private String mRegexFilterTitle = "\\(?\\d{1}[x×]\\d{1}\\)?"; 	//对4.1以上系统的手机过滤掉(NxN)、NxN的字符串

	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ON_FLY_FINISH :
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_AUTO_FLY, ScreenEditConstants.ADD_WIDGET_ID,
							msg.obj);

					//用post方法执行removeFlyView()，解决飞入动画最后一帧闪的问题
					post(new Runnable() {
						@Override
						public void run() {
							removeFlyView();
						}
					});
					break;

				default :
					break;
			}
		};
	};

	public GLGoWidgetSubTab(Context context, int tabId, int level) {
		super(context, tabId, level);

		mCurrentScreen = 1;
		mPreTabId = ScreenEditConstants.TAB_ID_GOWIDGET;
		mImageLoader.setDefaultResource(R.drawable.transparent);

		mWidgetDatasScan = new ArrayList<Object>();
		mWidgetDatasDetail = new ArrayList<Object>();
		mDatasDetail = new ConcurrentHashMap<Integer, ArrayList<Object>>();
	}

	@Override
	public void setParam(Object[] params) {
		if (params == null) {
			return;
		}
		if (params.length > 0) {
			mDisplayInfo = (BaseWidgetInfo) params[0];
		}
	}

	@Override
	public Object getParam() {
		return mDisplayInfo;
	}

	@Override
	public ArrayList<Object> requestData() {

		if (mDisplayInfo != null && !mHasLoaded) {
			getWidgetInfo(mDisplayInfo);
			mHasLoaded = true;
		}

		if (mWidgetSkinType == DEFAULT_SKIN) {
			mDataList = mWidgetDatasScan;
		} else if (mWidgetSkinType == GOWIDGET_SKIN) {
			mDataList = mDatasDetail.get(mSkinIndex);
		}
		return mDataList;
	}

	@Override
	public void handleAppChanged(int msgId, String pkgName, boolean showing) {

		if (mDisplayInfo.getPkgName().equals(pkgName)) {
			super.handleAppChanged(msgId, pkgName, showing);
			resetData();

			if (showing) {
				if (msgId == IAppCoreMsgId.EVENT_INSTALL_APP
						|| msgId == IAppCoreMsgId.EVENT_INSTALL_PACKAGE) {

					refreshData();

				} else if (msgId == IAppCoreMsgId.EVENT_UNINSTALL_APP
						|| msgId == IAppCoreMsgId.EVENT_UNINSTALL_PACKAGE) {

					GLBaseTab preTab = TabFactory.getInstance(mContext).getTab(mPreTabId);
					preTab.handleAppChanged(msgId, pkgName, false);

					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_EDIT,
							IScreenEditMsgId.SCREEN_EDIT_HANDLE_KEY_BACK, -1);
				}
			}
		}
	}

	@Override
	public void refreshTitle() {
		int showSkinImg = mDatasDetail.size() > 1 ? GLView.VISIBLE : GLView.GONE;
		MsgMgrProxy.sendHandler(this, IDiyFrameIds.SCREEN_EDIT,
				IScreenEditMsgId.SCREEN_EDIT_REFRESH_TITLE, mTabId, showSkinImg);
	}

	@Override
	protected GLView getHeadView() {
		return getInfoPageView(mDisplayInfo);
	}

	@Override
	protected GLView getFootView() {
		GLView glView = null;
		if (mDisplayInfo != null && mDisplayInfo instanceof InnerWidgetInfo) {

			glView = mGlInflater.inflate(R.layout.gl_screenedit_innerwidget_more_page, null);
			GLImageView imageView = (GLImageView) glView.findViewById(R.id.image_more);
			ShellTextViewWrapper more = (ShellTextViewWrapper) glView.findViewById(R.id.text_more);

			final InnerWidgetInfo info = (InnerWidgetInfo) mDisplayInfo;
			if (info.mStatisticPackage.equals(Statistics.GOSWITCH_WIDGET_PACKAGE_NAME)) {
				imageView.setImageResource(R.drawable.gl_screenedit_innerwidget_goswitch_more);
			} else if (info.mStatisticPackage.equals(Statistics.WEATHER_WIDGET_PACKAGE_NAME)) {
				imageView.setImageResource(R.drawable.gl_screenedit_innerwidget_weather_more);
			}

			more.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(GLView v) {
					if (info.mStatisticPackage.equals(Statistics.GOSWITCH_WIDGET_PACKAGE_NAME)) {
						//是否已安装电子市场
						if (AppUtils.isMarketExist(m2DContext)) {
							//启动googlepaly
							GoAppUtils.gotoMarket(m2DContext, MarketConstant.APP_DETAIL
									+ PackageName.SWITCHER_PLUS_PACKAGE);
						} else {
							//启动浏览器进入googlepaly
							GoAppUtils.gotoBrowserInRunTask(m2DContext,
									MarketConstant.BROWSER_APP_DETAIL
											+ PackageName.SWITCHER_PLUS_PACKAGE);
						}
					} else if (info.mStatisticPackage
							.equals(Statistics.WEATHER_WIDGET_PACKAGE_NAME)) {
						//跳转到Google Play
						GoAppUtils.gotoBrowserIfFailtoMarket(m2DContext,
								LauncherEnv.GO_WEATHER_EX_GA, LauncherEnv.Url.GO_WEATHER_EX_URL);
						//统计
						GLGoWeatherStatisticsUtil.uploadOperationStatisticData(m2DContext,
								GLGoWeatherStatisticsUtil.ACTION_EDIT_MORE);
					}
				}
			});
		}
		return glView;
	}

	@Override
	public GLView getView(int position) {
		return getSubWidgetView(position);
	}

	/**
	 *  初始化info页面
	 * @param widgetInfo
	 * @return
	 */
	public GLView getInfoPageView(final BaseWidgetInfo widgetInfo) {

		if (widgetInfo == null || widgetInfo.getProviderInfo() == null) {
			return null;
		}

		GLView infoView = null;

		// Add by xiangliang 小于800分辨率手机才有另一种布局
		if (DrawUtils.sHeightPixels < 800) {
			infoView = mGlInflater.inflate(R.layout.gl_screenedit_gowidget_info_page_small, null);
		} else {
			infoView = mGlInflater.inflate(R.layout.gl_screenedit_gowidget_info_page, null);
		}

		GLImageView icon = (GLImageView) infoView.findViewById(R.id.widgetIcon);
		Drawable iconDrawable = getWidgetIcon(widgetInfo);

		if (iconDrawable != null) {
			icon.setImageDrawable(iconDrawable);
		}

		final String pkgString = widgetInfo.getPkgName();

		String nameString = mContext.getResources().getString(R.string.gowidget_info_name);
		String versionString = mContext.getResources().getString(R.string.gowidget_info_version);

		String designerString = null;
		if (pkgString.equals(PackageName.CLEAN_MASTER_PACKAGE)
				|| pkgString.equals(PackageName.CLEAN_MASTER_PACKAGE_CN)) {
			designerString = mContext.getResources().getString(
					R.string.gowidget_info_designer_for_cleanmaster);
		} else {
			designerString = mContext.getResources().getString(R.string.gowidget_info_designer);
		}

		int fbResourceId = 0;
		if (pkgString.equals(PackageName.CLEAN_MASTER_PACKAGE)
				|| pkgString.equals(PackageName.CLEAN_MASTER_PACKAGE_CN)) {
			fbResourceId = R.string.gowidget_appgame_info_fb_for_cleanmaster;
		} else {
			fbResourceId = R.string.gowidget_appgame_info_fb;
		}
		String fbString = mContext.getResources().getString(fbResourceId);

		// update by zhoujun 2012-08-13 end
		int blogResourceId = 0;
		if (pkgString.equals(PackageName.CLEAN_MASTER_PACKAGE)
				|| pkgString.equals(PackageName.CLEAN_MASTER_PACKAGE_CN)) {
			blogResourceId = R.string.gowidget_info_blog_for_cleanmaster;
		} else {
			blogResourceId = R.string.gowidget_info_blog;
		}
		String blogString = mContext.getResources().getString(blogResourceId);

		ShellTextViewWrapper infoText = null;
		infoText = (ShellTextViewWrapper) infoView.findViewById(R.id.text1);
		infoText.setText(nameString + widgetInfo.getProviderInfo().label);

		PackageManager pm = mContext.getPackageManager();
		PackageInfo pkgInfo = null;
		try {
			pkgInfo = pm.getPackageInfo(pkgString, 0);
			versionString = versionString + pkgInfo.versionName;
		} catch (Exception e) {
		}
		infoText = (ShellTextViewWrapper) infoView.findViewById(R.id.text2);
		infoText.setText(versionString);

		infoText = (ShellTextViewWrapper) infoView.findViewById(R.id.text3);
		infoText.setText(designerString);

		infoText = (ShellTextViewWrapper) infoView.findViewById(R.id.text4);
		infoText.setText(fbString);

		infoText = (ShellTextViewWrapper) infoView.findViewById(R.id.text5);
		infoText.setText(blogString);

		// 反馈、卸载
		ShellTextViewWrapper fbButton = (ShellTextViewWrapper) infoView.findViewById(R.id.fb);
		ShellTextViewWrapper uninstallButton = (ShellTextViewWrapper) infoView
				.findViewById(R.id.uninstall);

		fbButton.setOnClickListener(new GLView.OnClickListener() {

			@Override
			public void onClick(GLView v) {
				String widgetName = widgetInfo.getProviderInfo().label;
				sendMail(widgetName);
			}
		});

		// update by zhoujun 新增应用游戏中心的widget
		if (widgetInfo instanceof InnerWidgetInfo) {
			InnerWidgetInfo innerWidgetInfo = (InnerWidgetInfo) widgetInfo;
			if (innerWidgetInfo.mPrototype != GoWidgetBaseInfo.PROTOTYPE_NORMAL) {
				// go精品和应用游戏中心widget没有“卸载组件”
				uninstallButton.setVisibility(GLView.GONE);
			}
		} else {
			uninstallButton.setOnClickListener(new GLView.OnClickListener() {

				@Override
				public void onClick(GLView v) {
					deleteGoWidget(pkgString);
				}
			});
		}

		return infoView;
	}

	/**
	 * 意见反馈
	 * 
	 * @param context
	 * @param file
	 * @param body
	 */
	private void sendMail(String widgetName) {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		String[] receiver = new String[] { "golauncher@goforandroid.com" };

		String subject = mContext.getResources().getString(R.string.gowidget_info_fb_subject);
		subject = widgetName + " - " + subject;

		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, receiver);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		emailIntent.setType("plain/text");
		mContext.startActivity(emailIntent);
	}

	/**
	 * 卸载widget
	 * @param themePkg
	 */
	private void deleteGoWidget(String themePkg) {
		if (ICustomAction.PKG_GOWIDGET_SWITCH.equals(themePkg)) {
			try {
				// 卸载开关，广播通知开关关闭相关安全权限
				Intent intent = new Intent(ICustomAction.ACTION_ON_OFF_UNINSTALL_BROADCAST);
				mContext.sendBroadcast(intent);
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IWidgetMsgId.GOWIDGET_UNINSTALL_GOWIDGET_SWITCH, -1, null, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		Uri packageURI = Uri.parse("package:" + themePkg);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		Activity activity = ShellAdmin.sShellManager.getActivity();
		activity.startActivityForResult(uninstallIntent, IRequestCodeIds.REQUEST_GOWIDGET_UNINSTALL);
	}

	@Override
	public void onTitleClick(Object... obj) {
		int index = (Integer) obj[0];
		if (mSkinIndex == index) {
			return;
		}

		mSkinIndex = (Integer) obj[0];
		if (mSkinIndex == 0) {
			mWidgetSkinType = DEFAULT_SKIN;
		} else {
			mWidgetSkinType = GOWIDGET_SKIN;
		}

		if (mImageLoader != null) {
			mImageLoader.clear();
		}
		refreshData();
	}

	/**
	 * 初始化默认风格
	 * @param position
	 * @return
	 */
	public GLView getSubWidgetView(int position) {
		if (mWidgetDatasScan == null || mDatasDetail == null) {
			return null;
		}

		AbsWidgetInfo parseInfo = null;

		if (mWidgetSkinType == DEFAULT_SKIN) {
			parseInfo = (AbsWidgetInfo) mWidgetDatasScan.get(position);
		} else if (mWidgetSkinType == GOWIDGET_SKIN) {
			parseInfo = (AbsWidgetInfo) mDatasDetail.get(mSkinIndex).get(position);
		}

		if (parseInfo == null) {
			return null;
		}

		parseInfo.setAddIndex(position);

		GLLinearLayout subView = (GLLinearLayout) mGlInflater.inflate(
				R.layout.gl_screen_edit_gowidget_subview, null);

		ShellTextViewWrapper titleView = (ShellTextViewWrapper) subView
				.findViewById(R.id.widgetstyletitle);
		titleView.setText(filterTitle(parseInfo.title, mRegexFilterTitle));

		ShellTextViewWrapper formatView = (ShellTextViewWrapper) subView
				.findViewById(R.id.widgetstyleformat);
		String fromatStr = String.format("%dx%d", parseInfo.mCol, parseInfo.mRow);
		formatView.setText(fromatStr);

		GLImageView imageView = (GLImageView) subView.findViewById(R.id.widgetPreview);
		imageView.setTag(parseInfo);
		imageView.setOnClickListener(this);
		
		if (mImageLoader != null) {
			mImageLoader.loadImage(imageView, position);
		}

		subView.setTag(parseInfo);
		return subView;
	}

	private String filterTitle(String title, String regex) {
		if (Build.VERSION.SDK_INT < 16) {
			return title;
		} else {
			try {
				return title.replaceAll(regex, "");
			} catch (Exception e) {
			}
			return title;
		}
	}

	@Override
	public void onClick(GLView v) {
		if (mFlyView != null) {
			return;
		}

		if (v.getTag() == null) {
			return;
		}

		AbsWidgetInfo info = null;
		if (v.getTag() instanceof AbsWidgetInfo) {
			info = (AbsWidgetInfo) v.getTag();
		}

		if (info == null) {
			return;
		}

		int[] centerXY = new int[2]; 							// 将要添加到的位置对应当前屏幕的坐标XY
		float[] translate = new float[2];
		if (!checkScreenVacant(info.mCol, info.mRow, centerXY, translate)) {
			return;
		}

		GLView flayView = getChildAt(info.getAddIndex());
		if (flayView != null) {
			flyToScreen(flayView, centerXY, translate);	// 进行添加的飞行动画
		}
	}

	@Override
	public void clear() {
		super.clear();
		if (mWidgetDatasDetail != null) {
			mWidgetDatasDetail.clear();
			mWidgetDatasDetail = null;
		}

		if (mDatasDetail != null) {
			int size = mDatasDetail.size();
			for (int i = 0; i < size; i++) {
				ArrayList<Object> infos = mDatasDetail.get(i);
				if (null != infos) {
					infos.clear();
					infos = null;
				}
			}
			mDatasDetail.clear();
		}
		// scan
		if (mWidgetDatasScan != null) {
			mWidgetDatasScan.clear();
		}
	}

	/**
	 * 初始化widget view的所有信息
	 * @param info
	 * @return
	 */
	public void getWidgetInfo(BaseWidgetInfo info) {

		parseData(info);

		if (!info.getPkgName().startsWith("com.gtp.nextlauncher.widget")) {
			initWidgetArray(info);				//开始处理不同皮肤的
			getAllGoWidgetInfos(info);
			scanSkins();						//异步扫描
		}
	}

	/**
	 * 扫描主题的第一种方式
	 * @param info
	 */
	private void parseData(BaseWidgetInfo info) {

		if (info == null) {
			return;
		}

		String packageName = info.getPkgName();
		if (packageName.equals("")) {
			return;
		}

		mWidgetDatasScan.clear();

		// 内置的GOWidget
		if (info instanceof InnerWidgetInfo) {

			InnerWidgetInfo innerWidgetInfo = (InnerWidgetInfo) info;
			ArrayList<WidgetParseInfo> styleList = InnerWidgetParser.getWidgetParseInfos(
					m2DContext, innerWidgetInfo);
			if (styleList != null && styleList.size() > 0) {
				mWidgetDatasScan.addAll(styleList);
			}
			styleList = null;

		} else if (info instanceof GoWidgetProviderInfo) {

			int count = 0;
			try {
				Resources resources = mContext.getPackageManager().getResourcesForApplication(
						packageName);

				// 获取图片
				int drawableList = resources.getIdentifier(GoWidgetConstant.NEW_PREVIEW_LIST,
						"array", packageName);
				if (drawableList <= 0) {
					drawableList = resources.getIdentifier(GoWidgetConstant.PREVIEW_LIST, "array",
							packageName);
				}

				if (drawableList > 0) {
					final String[] extras = resources.getStringArray(drawableList);
					for (String extra : extras) {

						int res = resources.getIdentifier(extra, "drawable", packageName);
						if (res != 0) {
							WidgetParseInfo item = new WidgetParseInfo();
							item.resouceId = res;
							item.resouces = resources;

							item.themePackage = null;
							mWidgetDatasScan.add(item);
						}
					}
				}

				// 获取标题
				int titilList = resources.getIdentifier(GoWidgetConstant.STYLE_NAME_LIST, "array",
						packageName);
				if (titilList > 0) {
					final String[] titles = resources.getStringArray(titilList);
					count = 0;
					for (String titl : titles) {
						int res = resources.getIdentifier(titl, "string", packageName);
						if (res != 0) {
							WidgetParseInfo item = (WidgetParseInfo) mWidgetDatasScan.get(count);
							item.title = resources.getString(res);
							count++;
						}
					}
				}

				// 获取类型
				int typeList = resources.getIdentifier(GoWidgetConstant.TYPE_LIST, "array",
						packageName);
				if (typeList > 0) {
					final int[] typeLists = resources.getIntArray(typeList);
					count = 0;
					for (int types : typeLists) {

						WidgetParseInfo item = (WidgetParseInfo) mWidgetDatasScan.get(count);
						item.type = types;
						item.styleType = String.valueOf(types);
						count++;
					}
				}

				// 获取行数
				int rowList = resources.getIdentifier(GoWidgetConstant.ROW_LIST, "array",
						packageName);
				if (rowList > 0) {
					final int[] rowLists = resources.getIntArray(rowList);
					count = 0;
					for (int row : rowLists) {

						WidgetParseInfo item = (WidgetParseInfo) mWidgetDatasScan.get(count);
						item.mRow = row;
						count++;
					}
				}

				// 获取列数
				int colList = resources.getIdentifier(GoWidgetConstant.COL_LIST, "array",
						packageName);
				if (colList > 0) {
					final int[] colListS = resources.getIntArray(colList);
					count = 0;
					for (int col : colListS) {

						WidgetParseInfo item = (WidgetParseInfo) mWidgetDatasScan.get(count);
						item.mCol = col;
						count++;
					}
				}

				// 获取layout id
				int layoutIDList = resources.getIdentifier(GoWidgetConstant.LAYOUT_LIST, "array",
						packageName);
				if (layoutIDList > 0) {
					final String[] layouIds = resources.getStringArray(layoutIDList);
					count = 0;
					for (String id : layouIds) {

						WidgetParseInfo item = (WidgetParseInfo) mWidgetDatasScan.get(count);
						item.layoutID = id;
						count++;
					}
				}

				// 获取竖屏最小宽度
				int minWidthVer = resources.getIdentifier(GoWidgetConstant.MIN_WIDTH, "array",
						packageName);
				if (minWidthVer > 0) {
					final int[] widthIds = resources.getIntArray(minWidthVer);
					count = 0;
					for (int w : widthIds) {

						WidgetParseInfo item = (WidgetParseInfo) mWidgetDatasScan.get(count);
						//						item.minWidth = w;
						item.minWidth = DrawUtils.px2dip(CellUtils.measureGoWidgetMinWidth(
								mContext, item.mCol));
						count++;
					}
				}

				// 获取竖屏最小高度
				int minHeightVer = resources.getIdentifier(GoWidgetConstant.MIN_HEIGHT, "array",
						packageName);
				if (minHeightVer > 0) {
					final int[] widthIds = resources.getIntArray(minHeightVer);
					count = 0;
					for (int h : widthIds) {

						WidgetParseInfo item = (WidgetParseInfo) mWidgetDatasScan.get(count);
						//						item.minHeight = h;
						item.minHeight = DrawUtils.px2dip(CellUtils.measureGoWidgetMinHeight(
								mContext, item.mRow));
						count++;
					}
				}

				// 获取layout id
				int configActivityList = resources.getIdentifier(GoWidgetConstant.CONFIG_LIST,
						"array", packageName);
				if (configActivityList > 0) {
					mNeedConfig = true;

					final String[] layouIds = resources.getStringArray(configActivityList);
					count = 0;
					for (String id : layouIds) {

						WidgetParseInfo item = (WidgetParseInfo) mWidgetDatasScan.get(count);
						item.configActivty = id;
						count++;
					}
				} else {

					// 是否有统一的设置界面
					int resConfigId = resources.getIdentifier("configname", "string", packageName);
					if (resConfigId > 0) {
						mConfigActivity = resources.getString(resConfigId);
						if (mConfigActivity.equals("")) {
							mNeedConfig = false;
						} else {
							mNeedConfig = true;
						}
					}
				}

				int longkeyconfigActivityList = resources.getIdentifier(
						GoWidgetConstant.SETTING_LIST, "array", packageName);
				if (longkeyconfigActivityList > 0) {

					final String[] layouIds = resources.getStringArray(longkeyconfigActivityList);
					count = 0;
					for (String id : layouIds) {

						WidgetParseInfo item = (WidgetParseInfo) mWidgetDatasScan.get(count);
						item.longkeyConfigActivty = id;
						count++;
					}
				}

			} catch (NameNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 第二步初始化widget数据列表 
	 * @param info
	 */
	private void initWidgetArray(BaseWidgetInfo info) {
		try {

			// 内置的GOWidget
			if (info instanceof InnerWidgetInfo) {

				InnerWidgetInfo widgetInfo = (InnerWidgetInfo) info;
				if (widgetInfo.mBuildin == InnerWidgetInfo.BUILDIN_ALL) {
					// gostore
					mRowLists = m2DContext.getResources().getIntArray(widgetInfo.mRowList);
					mColLists = m2DContext.getResources().getIntArray(widgetInfo.mColumnList);
					mStyleTypeList = m2DContext.getResources().getIntArray(widgetInfo.mTypeList);
				}

			} else if (info instanceof GoWidgetProviderInfo) {

				String widgetPkg = info.getPkgName();
				Resources resources = mContext.getPackageManager().getResourcesForApplication(
						widgetPkg);
				if (resources == null) {
					return;
				}

				// 获取行数
				int rowList = resources
						.getIdentifier(GoWidgetConstant.ROW_LIST, "array", widgetPkg);
				if (rowList > 0) {
					mRowLists = resources.getIntArray(rowList);
				}

				// 获取列数
				int colList = resources
						.getIdentifier(GoWidgetConstant.COL_LIST, "array", widgetPkg);
				if (colList > 0) {
					mColLists = resources.getIntArray(colList);
				}

				// 如果typeId不存在则返回
				int styleTypeId = resources.getIdentifier(GoWidgetConstant.TYPE_LIST, "array",
						widgetPkg);
				if (styleTypeId > 0) {
					mStyleTypeList = resources.getIntArray(styleTypeId);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 异步加载图片
	 */
	@Override
	public Bitmap onLoadImage(int index) {

		Bitmap bitmap = null;
		AbsWidgetInfo widgetInfo = null;

		if (mWidgetSkinType == DEFAULT_SKIN) {
			if (index >= 0 && index < mWidgetDatasScan.size()) {
				widgetInfo = (AbsWidgetInfo) mWidgetDatasScan.get(index);
			}
		} else {
			ArrayList<Object> list = mDatasDetail.get(mSkinIndex);
			if (list != null && index >= 0 && index < list.size()) {
				widgetInfo = (AbsWidgetInfo) list.get(index);
			}
		}

		if (widgetInfo == null) {
			return null;
		}

		if (widgetInfo.resouces != null) {
			Drawable drawable = widgetInfo.resouces.getDrawable(widgetInfo.resouceId);
			if (drawable != null && drawable instanceof BitmapDrawable) {
				bitmap = ((BitmapDrawable) drawable).getBitmap();
			}
		}
		return bitmap;
	}

	/**
	 * 扫描所有主题包
	 * @return
	 */
	private List<ResolveInfo> getAllWidgetThemesInfo() {
		// 桌面主题包
		PackageManager pm = mContext.getPackageManager();
		// widget主题包
		Intent intent = new Intent(ICustomAction.ACTION_WIDGET_THEME_PACKAGE);
		intent.addCategory(THEME_CATEGORY);

		List<ResolveInfo> widgetThemes = pm.queryIntentActivities(intent, 0);
		return widgetThemes;
	}

	/**
	 * 扫描widget的风格数据
	 * @param info
	 */
	private void getAllGoWidgetInfos(BaseWidgetInfo info) {

		List<ResolveInfo> themes = getAllWidgetThemesInfo();

		ThemeBean themeBean = null;
		String appPackageName = null;
		String themeFileName = null;
		String loadingThemeName = mContext.getString(R.string.loading);
		String widgetPkg = info.getPkgName();

		int size = themes.size();
		for (int i = 0; i <= size; i++) {
			if (i == 0) {
				appPackageName = widgetPkg;
			} else {
				appPackageName = themes.get(i - 1).activityInfo.packageName.toString();
			}

			try {
				themeFileName = doParseData(widgetPkg, info);
			} catch (Exception e) {
			}

			// 增强短信配置文件判断
			if (widgetPkg.equals(PackageName.GOSMS_PACKAGE)) {
				if (!appPackageName.equals(PackageName.GOSMS_PACKAGE)) {
					themeFileName = "widget_smswidget.xml";
				} else {
					themeFileName = "widget_gosms.xml";
				}
			} else if (widgetPkg.equals("com.jiubang.app.news")) {
				themeFileName = "widget_newswidget.xml";
			}

			InputStream inputStream = XmlParserFactory.createInputStream(mContext, appPackageName,
					themeFileName);

			// for 任务管理器EX add by chenguanyu 2012.6.29
			if (inputStream == null
					&& widgetPkg.equals(PackageName.RECOMMAND_GOTASKMANAGER_PACKAGE)) {
				themeFileName = "widget_taskmanager.xml";
				inputStream = XmlParserFactory.createInputStream(mContext, appPackageName,
						themeFileName);
			}

			if (null == inputStream) {
				//				Log.i("GoWidgetManagerFrame", "no file:" + themeFileName + " in package:"
				//						+ appPackageName);
				continue;
			}

			GoWidgetInfo widgetItem = new GoWidgetInfo();
			widgetItem.title = loadingThemeName;

			themeBean = new PreviewSpecficThemeBean();
			if (themeBean != null) {
				themeBean.setPackageName(appPackageName);

				((PreviewSpecficThemeBean) themeBean).setInputStream(inputStream);

				widgetItem.packageName = appPackageName;
				widgetItem.themeBean = (PreviewSpecficThemeBean) themeBean;
				mWidgetDatasDetail.add(widgetItem);
			}
		}
	}

	/**
	 * 从widget包解析数据
	 * @param packageName
	 * @param info
	 * @return
	 * @throws NameNotFoundException
	 */
	private String doParseData(String packageName, BaseWidgetInfo info)
			throws NameNotFoundException {

		String widgetName = "";
		final int prefixLength = WIDGET_PACKAGE_PREFIX.length();
		if (IGoLauncherClassName.DEFAULT_THEME_PACKAGE.equals(packageName)) {

			InnerWidgetInfo innerWidgetInfo = (InnerWidgetInfo) info;
			// update by zhoujun
			// 应用游戏中心的widget和gostorewidget的packageName是一样的，这里需要进一步判断，否则会将应用中心widget的mTitle改为“GO
			// 精品”
			if (innerWidgetInfo.mPrototype == GoWidgetBaseInfo.PROTOTYPE_GOSTORE) {
				// GOSTORE widget 标题需要根据渠道号显示
				innerWidgetInfo.mTitle = GoStoreChannelControl.getChannelCheckName(mContext);
			}
			// update by zhoujun 2012-08-13 end
			String fileName = innerWidgetInfo.mThemeConfig;
			return fileName;
		}
		if (packageName.length() > prefixLength) {
			widgetName = packageName.substring(prefixLength);
		}
		return "widget_" + widgetName + ".xml";
	}

	/**
	 *  第三步 扫描皮肤
	 */
	private void scanSkins() {
		try {
			int count = mWidgetDatasDetail.size();
			for (int i = 0; i < count; i++) {
				GoWidgetInfo info = (GoWidgetInfo) mWidgetDatasDetail.get(i);
				parserThemeInfo(info);
			}
			// mScanSkinFinished = true;
		} catch (Exception e) {
			// 这里是异步扫描，可能在扫描过程中用户点击返回，这里可能出现空指针
		}
		if (null == mWidgetDatasDetail || mWidgetDatasDetail.isEmpty()) {
			return;
		}

		if (null != mDatasDetail && !mDatasDetail.isEmpty()) {
			/**
			 * 说明：这是gowidget主包内有>1套风格的处理，例如clock
			 */
			int oldDefaultStyleCount = getDefaultSkinStyleCount();
			int newDefaultStyleCount = mDatasDetail.get(0).size();

			Message msg = new Message();
			if (oldDefaultStyleCount != newDefaultStyleCount) {
				msg.what = DEFAULT_SKIN;
			} else {
				msg.what = GOWIDGET_SKIN;
			}
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * 获取未经异步扫描前，gowidget主包内有几张预览图，用于与异步扫描完后 对比数量是否有变化
	 * 
	 * @return
	 */
	public int getDefaultSkinStyleCount() {
		return (null != mWidgetDatasScan) ? mWidgetDatasScan.size() : 0;
	}

	/**
	 * 解析各个主题包中widget预览信息
	 * 
	 * @author penglong
	 * @param themePackage
	 *            主题包名 styleString 当前widget的style themeBean 需要填充的theme信息
	 */
	private void parserThemeInfo(GoWidgetInfo widgetItem) {
		if (null == widgetItem || null == widgetItem.themeBean || null == widgetItem.packageName) {
			return;
		}

		PreviewSpecficThemeBean themeBean = widgetItem.themeBean;
		String themePackage = widgetItem.packageName;

		InputStream inputStream = themeBean.getInputStream();

		// 扫描主题包内有几个主题
		XmlPullParser xmlPullParser = XmlParserFactory.createXmlParser(inputStream);
		ParseSpecficWidgetTheme parser = new ParseSpecficWidgetTheme();
		parser.parseXml(xmlPullParser, themeBean);
		parser = null;

		// 关闭inputStream
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
			}
		}

		Resources resTheme = null;
		try {
			resTheme = mContext.getPackageManager().getResourcesForApplication(themePackage);
		} catch (NameNotFoundException e) {
		}

		if (null == resTheme) {
			return;
		}

		ArrayList<String> widgetPreview = themeBean
				.getWidgetAttrib(ParseSpecficWidgetTheme.NEW_WIDGET_PREVIEW);
		if (widgetPreview == null) {
			widgetPreview = themeBean.getWidgetAttrib(ParseSpecficWidgetTheme.WIDGET_PREVIEW);
		}

		ArrayList<String> widgetTitle = themeBean
				.getWidgetAttrib(ParseSpecficWidgetTheme.WIDGET_TITLE);
		ArrayList<String> widgetThemeType = themeBean
				.getWidgetAttrib(ParseSpecficWidgetTheme.WIDGET_THEME_TYPE);
		ArrayList<Integer> themePositionList = themeBean.getThemePositionList();
		ArrayList<Integer> styleids = themeBean.getmStyleIdsList();

		GoWidgetInfo newGoWidgetInfo = null;
		if (null == themePositionList || 0 == themePositionList.size() || null == styleids
				|| styleids.isEmpty() || themePositionList.size() != styleids.size()) {
			return;
		} else {
			ArrayList<Object> widgetInfos = new ArrayList<Object>();
			for (int i = 0; i < themePositionList.size(); i++) {
				newGoWidgetInfo = new GoWidgetInfo();
				newGoWidgetInfo.packageName = widgetItem.packageName;

				try {
					newGoWidgetInfo.themeId = Integer.parseInt(widgetThemeType.get(i));
				} catch (Exception e) {
					newGoWidgetInfo.themeId = -1;
				}

				if (widgetTitle != null && i < widgetTitle.size()) {
					int resId = resTheme.getIdentifier(widgetTitle.get(i), "string", themePackage);
					if (resId != 0) {
						newGoWidgetInfo.title = resTheme.getString(resId);
					}
				}

				if (widgetPreview != null && i < widgetPreview.size()) {
					int res = resTheme
							.getIdentifier(widgetPreview.get(i), "drawable", themePackage);
					if (res != 0) {
						newGoWidgetInfo.resouceId = res;
						newGoWidgetInfo.resouces = resTheme;
					}
				}

				// 几行几列
				int typePosition = -1;
				int lengh = (null == mStyleTypeList) ? 0 : mStyleTypeList.length;
				for (int j = 0; j < lengh; j++) {
					if (styleids.get(i) == mStyleTypeList[j]) {
						newGoWidgetInfo.styleId = styleids.get(i);
						typePosition = j;
						break;
					}
				}
				// 没找到对应的typeId;typePosition是指当前type在widget包里的位置
				if (-1 == typePosition) {
					continue;
				}
				try {
					newGoWidgetInfo.mCol = mColLists[typePosition];
					newGoWidgetInfo.mRow = mRowLists[typePosition];
				} catch (Exception e) {
					continue;
				}

				widgetInfos.add(newGoWidgetInfo);
			}
			if (!widgetInfos.isEmpty()) {
				mDatasDetail.put(mDatasDetail.size(), widgetInfos);
			}
		}
		return;
	}

	@Override
	public void handleMessage(int msgId, int param, Object... objects) {
		switch (msgId) {
			case IScreenEditMsgId.SCREEN_EDIT_ADD_GOWIDGET_TO_SCREEN :
				addGoWidget(param);
				break;

			default :
				break;
		}
	}

	/**
	 * 添加gowidget至桌面
	 * @param index
	 */
	public void addGoWidget(int index) {
		//通知GLWorkspace可能滚动
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.ALLOW_SCREEN_TO_SCROLL, -1);

		final GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
		int gowidgetId = widgetManager.allocateWidgetId();
		int currentIndex = index;

		WidgetParseInfo info = null;
		GoWidgetInfo goWidgetInfo = null;

		if (mWidgetSkinType == GOWIDGET_SKIN) {
			// 不是默认风格，要通过styleID查找对应的info
			goWidgetInfo = (GoWidgetInfo) mDatasDetail.get(mSkinIndex).get(currentIndex);
			int styleid = goWidgetInfo.styleId;
			int datacount = mWidgetDatasScan.size();
			for (int i = 0; i < datacount; i++) {
				WidgetParseInfo widgetParseInfo = (WidgetParseInfo) mWidgetDatasScan.get(i);
				if (widgetParseInfo.styleType != null
						&& (Integer.valueOf(widgetParseInfo.styleType) == styleid)) {
					info = widgetParseInfo;
					break;
				}
			}
		} else if (mWidgetSkinType == DEFAULT_SKIN) {
			if (mWidgetDatasScan != null && currentIndex < mWidgetDatasScan.size()) {
				info = (WidgetParseInfo) mWidgetDatasScan.get(currentIndex);
			}
		}

		if (null == info) {
			return;
		}

		int prototype = GoWidgetBaseInfo.PROTOTYPE_NORMAL;
		Bundle bundle = new Bundle();

		String widgetPackage = mDisplayInfo.getPkgName();
		final AppWidgetProviderInfo provider = mDisplayInfo.getProviderInfo();

		provider.minHeight = DrawUtils.dip2px(info.minHeight);
		provider.minWidth = DrawUtils.dip2px(info.minWidth);

		if (mDisplayInfo instanceof GoWidgetProviderInfo) {

			// ADT-11992 3Ｄ插件：时钟－蓝色简约2＊2新式小部件缺少设置菜单
			if (info.longkeyConfigActivty == null || info.longkeyConfigActivty.equals("")) {
				provider.configure = null;
			}

			bundle.putInt(GoWidgetConstant.GOWIDGET_ID, gowidgetId);
			bundle.putInt(GoWidgetConstant.GOWIDGET_TYPE, info.type);
			bundle.putString(GoWidgetConstant.GOWIDGET_LAYOUT, info.layoutID);
			bundle.putParcelable(GoWidgetConstant.GOWIDGET_PROVIDER, provider);

			String themePackage = null;
			int themeid = 0;
			if (mWidgetSkinType == GOWIDGET_SKIN) {
				themePackage = goWidgetInfo.packageName;
				themeid = goWidgetInfo.themeId;
			} else if (mWidgetSkinType == DEFAULT_SKIN) {
				themePackage = info.themePackage;
				themeid = info.themeType;
			}

			bundle.putString(GoWidgetConstant.GOWIDGET_THEME, themePackage);
			bundle.putInt(GoWidgetConstant.GOWIDGET_THEMEID, themeid);

		} else if (mDisplayInfo instanceof InnerWidgetInfo) {

			InnerWidgetInfo innerWidgetInfo = (InnerWidgetInfo) mDisplayInfo;
			prototype = innerWidgetInfo.mPrototype;

			bundle.putInt(GoWidgetConstant.GOWIDGET_ID, gowidgetId);
			bundle.putInt(GoWidgetConstant.GOWIDGET_TYPE, info.type);
			bundle.putString(GoWidgetConstant.GOWIDGET_LAYOUT, info.layoutID);
			bundle.putParcelable(GoWidgetConstant.GOWIDGET_PROVIDER, provider);
			bundle.putString(GoWidgetConstant.GOWIDGET_THEME, info.themePackage);
			bundle.putInt(GoWidgetConstant.GOWIDGET_THEMEID, info.themeType);
		}

		bundle.putInt(GoWidgetConstant.GOWIDGET_PROTOTYPE, prototype);
		bundle.putBoolean(GoWidgetConstant.GOWIDGET_ADD_TO_SCREEN, true);
		bundle.putBoolean("gowidget_is3d", true);
		if (!"".equals(info.longkeyConfigActivty)) {
			ComponentName temp = new ComponentName(widgetPackage, info.longkeyConfigActivty);
			provider.configure = temp;
		}

		if (mNeedConfig) {
			if (mConfigActivity.equals("") && !info.configActivty.equals("")) {
				GoWidgetActionReceiver.sPenddingAddWidgetId = gowidgetId;
				startConfigActivity(bundle, widgetPackage, info.configActivty);
			} else if (!mConfigActivity.equals("")) {
				GoWidgetActionReceiver.sPenddingAddWidgetId = gowidgetId;
				startConfigActivity(bundle, widgetPackage, mConfigActivity);
			} else {
				//添加到当前桌面
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
						IScreenFrameMsgId.SCREEN_ADD_GO_WIDGET, gowidgetId, bundle);
			}

		} else {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.SCREEN_ADD_GO_WIDGET, gowidgetId, bundle);
		}
		bundle = null;
	}

	/**
	 * 启动设置界面
	 * @param bundle
	 * @param pkgName
	 * @param configure
	 */
	public void startConfigActivity(Bundle bundle, String pkgName, String configure) {
		try {
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setClassName(pkgName, configure);
			intent.putExtras(bundle);
			mContext.startActivity(intent);
		} catch (Exception e) {
			// 退出widget选择界面
			// sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.REMOVE_FRAME,
			// getId(), null, null);
			//			Log.i("widgetChooser", "startConfigActivity error: " + pkgName + "." + configure);
		}
	}

	/**
	 * 根据BaseWidgetInfo获取图标
	 * @param info
	 * @return
	 */
	private Drawable getWidgetIcon(BaseWidgetInfo info) {

		Drawable drawable = null;
		final String pkgName = info.getPkgName();

		if (pkgName.equals("")) {

			if (info.getProviderInfo().icon > 0) {
				drawable = mContext.getResources().getDrawable(info.getProviderInfo().icon);
			} else if (info.getIconPath() != null) {
				BitmapDrawable imgDrawable = null;
				if (FileUtil.isFileExist(info.getIconPath())) {
					try {
						Bitmap bitmap = BitmapFactory.decodeFile(info.getIconPath());
						final Resources resources = mContext.getResources();
						DisplayMetrics displayMetrics = resources.getDisplayMetrics();
						float density = displayMetrics.densityDpi;
						float scale = density / DEFAULT_DENSITY;
						bitmap = Bitmap.createScaledBitmap(bitmap,
								(int) (bitmap.getWidth() * scale),
								(int) (bitmap.getHeight() * scale), false);
						imgDrawable = new BitmapDrawable(resources, bitmap);
						imgDrawable.setTargetDensity(displayMetrics);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (imgDrawable == null) {
					imgDrawable = (BitmapDrawable) ImageExplorer.getInstance(mContext).getDrawable(
							IGoLauncherClassName.DEFAULT_THEME_PACKAGE, info.getIconPath());
				}
				drawable = imgDrawable;
			}
		} else {
			Resources resources;
			try {
				resources = mContext.getPackageManager().getResourcesForApplication(pkgName);
				if (resources != null) {
					drawable = resources.getDrawable(info.getProviderInfo().icon);
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}

		return drawable;
	}

	private void flyToScreen(GLView flyView, int[] centerXY, float[] translate) {

		int[] loc = new int[2];
		ShellAdmin.sShellManager.getShell().getContainer().getLocation(flyView, loc);
		int centerX = loc[0] + flyView.getWidth() / 2;
		int centerY = loc[1] + flyView.getHeight() / 2;
		mFlyView = cloneIcon(flyView, centerX, centerY, 0.7f);

		if (mFlyView == null) {
			return;
		}

		AnimationTask task = new AnimationTask(true, AnimationTask.PARALLEL);
		ShellAdmin.sShellManager.getShell().getDragLayer().addView(mFlyView);

		int offsetX = centerXY[0] - centerX;
		int offsetY = (int) (centerXY[1] - centerY - translate[0]);
		int duration = (int) (Math.sqrt(offsetX * offsetX + offsetY * offsetY + translate[1]
				* translate[1]) / GLGoWidgetTab.VELOCITY_FOR_AUTO_FLY_WIDGET);
		Animation moveAnimation = new Translate3DAnimation(0, offsetX, 0, -offsetY, 0, translate[1]);
		moveAnimation.setFillEnabled(true);
		moveAnimation.setFillAfter(true);
		moveAnimation.setDuration(duration);
		moveAnimation.setInterpolator(InterpolatorFactory
				.getInterpolator(InterpolatorFactory.EASE_OUT));

		AnimationSet set = new AnimationSet(false);
		set.addAnimation(moveAnimation);
		task.addAnimation(mFlyView, set, null);
		task.setBatchAnimationObserver(this, -1, flyView);

		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.LOCK_SCREEN_TO_SCROLL,
				-1);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_ENABLE_CELLLAYOUT_DRAWING_CACHE, 1);
		GLAnimationManager.startAnimation(task);
	}

	private GLView cloneIcon(GLView view, int centerX, int centerY, float screenScale) {

		GLView originalIcon = view;
		originalIcon.setVisible(true);
		GLView cloneView;
		Object itemInfo = view.getTag();
		if (itemInfo == null || !(itemInfo instanceof AbsWidgetInfo)) {
			return null;
		}

		AbsWidgetInfo info = (AbsWidgetInfo) itemInfo;
		cloneView = (GLLinearLayout) mGlInflater.inflate(R.layout.gl_screen_edit_gowidget_subview,
				null);

		cloneView.findViewById(R.id.widgetstyletitle).setVisibility(GLView.INVISIBLE);
		cloneView.findViewById(R.id.widgetstyleformat).setVisibility(GLView.INVISIBLE);
		GLImageView imageView = (GLImageView) cloneView.findViewById(R.id.widgetPreview);

		Resources resouces = null;
		if (itemInfo instanceof WidgetParseInfo) {
			resouces = ((WidgetParseInfo) itemInfo).resouces;
		} else if (itemInfo instanceof GoWidgetInfo) {
			resouces = ((GoWidgetInfo) itemInfo).resouces;
		}

		if (resouces == null) {
			return null;
		}

		try {
			imageView.setImageDrawable(resouces.getDrawable(info.resouceId));
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		cloneView.setTag(info);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(0, 0);
		GLDragLayer.LayoutParams lp = new GLDragLayer.LayoutParams(params);
		lp.width = view.getWidth();
		lp.height = view.getHeight();
		lp.x = centerX - lp.width / 2;
		lp.y = centerY - lp.height / 2;
		lp.customPosition = false;
		cloneView.setLayoutParams(lp);
		return cloneView;
	}

	private void removeFlyView() {
		if (mFlyView != null) {
			mFlyView.cleanup();
			ShellAdmin.sShellManager.getShell().getDragLayer().removeView(mFlyView);
			mFlyView = null;
		}
	}

	/***
	 * 查看桌面是否还能放下指定大小的组件
	 * 
	 * @param spanX
	 *            行
	 * @param spanY
	 *            列
	 * @return
	 */
	public boolean checkScreenVacant(int spanX, int spanY, int[] centerXY, float[] translate) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(spanX);
		list.add(spanY);
		return MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_EDIT_PRE_ADD, 0, list, centerXY, translate);
	}

	@Override
	public void onStart(int what, Object[] params) {
	}

	@Override
	public void onFinish(int what, Object[] params) {
		if (params != null && params.length > 0) {
			Message msg = new Message();
			msg.what = ON_FLY_FINISH;
			msg.obj = params[0];
			mHandler.sendMessage(msg);
		}
	}

	@Override
	public Object requestTitleInfo() {
		int size = mDatasDetail.size();
		if (mDatasDetail.size() < 1) {
			return null;
		}

		String[] array = new String[size];
		for (int i = 0; i < size; i++) {
			List<Object> list = mDatasDetail.get(i);
			if (list != null) {
				GoWidgetInfo info = (GoWidgetInfo) list.get(0);
				if (info != null && info.title != null) {
					array[i] = info.title;
				}
			}
		}
		return array;
	}
	
	@Override
	public boolean onBackExit() {
		if (ScreenEditConfig.sEXTERNAL_FROM_ID == ScreenEditConstants.EXTERNAL_ID_APPDRAWER_SLIDEMENU) {
			return true;
		}
		return super.onBackExit();
	}

	@Override
	public boolean onKeyBack() {
		if (mFlyView != null) {
			return true;
		}

		mCurrentScreen = 1;
		mWidgetSkinType = DEFAULT_SKIN;
		mHasLoaded = false;
		if (mWidgetDatasScan != null) {
			mWidgetDatasScan.clear();
		}
		if (mWidgetDatasDetail != null) {
			mWidgetDatasDetail.clear();
		}
		if (mDatasDetail != null) {
			mDatasDetail.clear();
		}

		return super.onKeyBack();
	}
}