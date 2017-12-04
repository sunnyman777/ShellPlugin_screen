package com.jiubang.shell.appdrawer.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.util.file.media.FileInfo;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.appfunc.controler.MediaFileSuperVisor;
import com.jiubang.ggheart.apps.appfunc.controler.MediaFileSuperVisor.FileEngineDataRefreshListener;
import com.jiubang.ggheart.apps.appfunc.setting.AppFuncAutoFitManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.FuncSearchResultItem;
import com.jiubang.ggheart.apps.desks.appfunc.search.AppfuncSearchEngine;
import com.jiubang.ggheart.apps.desks.appfunc.search.ResponseHandler;
import com.jiubang.ggheart.apps.desks.appfunc.search.SearchObserver;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.realtiemstatistics.RealTimeStatisticsContants;
import com.jiubang.ggheart.data.statistics.realtiemstatistics.RealTimeStatisticsUtil;
import com.jiubang.ggheart.launcher.IconUtilities;
/**
 * 
 * @author dingzijian
 *
 */
public class GLAppDrawerSearchHandler extends Handler
		implements
			SearchObserver,
			FileEngineDataRefreshListener {
	private AppfuncSearchEngine mSearchEngine;

	private GLAppDrawerSearchMainView mDrawerSearchMainView;

	private int mWebAppInsertPos = -1;

	private int mMoreWebAppInsertPos = -1;

	private ArrayList<Object> mWebAppList = null;

	private Object mLock;

	private Object mSearchStamp;

	private boolean mLocalAppWithoutRet;

	private boolean mSearchMedia;
	
	public static final int SEARCH_HISTROY_LITMIT = 6;
	public GLAppDrawerSearchHandler(GLAppDrawerSearchMainView drawerSearchMainView) {
		super();
		mDrawerSearchMainView = drawerSearchMainView;
		init();
	}

	private void init() {
		mSearchEngine = AppfuncSearchEngine.getInstance(ApplicationProxy.getContext());
		ResponseHandler.register(this);
		MediaFileSuperVisor controler = MediaFileSuperVisor.getInstance(ApplicationProxy.getContext());
		controler.registerFileObserver(null);
		controler.buildFileEngine();
		if (mSearchMedia) {
			controler.refreshAllMediaData();
			controler.setFileEngineDataRefreshListener(this);
		}
		mLock = new Object();
	}

	public void searchLocalAndWeb(String searchKey) {
		synchronized (mLock) {
			mSearchStamp = new Object();
			if (searchKey != null && !searchKey.equals("")) {
				Log.i("dzj", "searchKey" + searchKey);
				mDrawerSearchMainView.showProgressBar();
				clearWebCache();
				mSearchEngine.searchLocalAndWeb(searchKey, mSearchStamp);
			} else {
				getLocalSearchRecord();
			}
		}
	}

	public void getMoreWebRes() {
		synchronized (mLock) {
			if (mWebAppList != null && !mWebAppList.isEmpty() && mMoreWebAppInsertPos != -1) {
				mDrawerSearchMainView.notifyWebSearchResultChanged(mWebAppList,
						mMoreWebAppInsertPos);
				mWebAppList = null;
			} else {
				//跳转到android应用市场。
				AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(
						ApplicationProxy.getContext(),
						AppRecommendedStatisticsUtil.ENTRY_TYPE_APPFUNC_SEARCH);
				AppsManagementActivity.startAppCenter(ApplicationProxy.getContext(),
						MainViewGroup.ACCESS_FOR_APPFUNC_SEARCH, true,
						RealTimeStatisticsContants.AppgameEntrance.FUNC_SEARCH);
			}
		}
	}

	public void getLocalSearchRecord() {
		mSearchEngine.checkSearchHistroy(AppfuncSearchEngine.HISTORY_TYPE_LOCAL, false);
	}

	@Override
	public void onSearchSupported(boolean supported, Object timeStamp) {
		if (!supported) {
			tipsConnectFail(timeStamp);
		}
	}

	@Override
	public void onSearchStart(String searchKey, int type, int requestTimes) {

	}

	@Override
	public void onSearchFinsh(String searchKey, List<?> list, int type, int resultCount,
			int currentPage, Object timestamp) {
		synchronized (mLock) {
			if (mSearchStamp != timestamp) {
				clearWebCache();
				return;
			}
			@SuppressWarnings("unchecked")
			ArrayList<FuncSearchResultItem> resultItems = (ArrayList<FuncSearchResultItem>) list;
			ArrayList<FuncSearchResultItem> uiResultItems = new ArrayList<FuncSearchResultItem>(
					resultItems);
			ArrayList<Object> results = new ArrayList<Object>();
			if (type == AppfuncSearchEngine.LOCAL_SEARCH_FINISH) {
				Log.i("dzj", "localSearchKey--->" + searchKey);
				uiResultItems.remove(uiResultItems.size() - 1);
				ArrayList<FuncSearchResultItem> localApps = new ArrayList<FuncSearchResultItem>();
				ArrayList<FuncSearchResultItem> images = new ArrayList<FuncSearchResultItem>();
				ArrayList<FuncSearchResultItem> audios = new ArrayList<FuncSearchResultItem>();
				ArrayList<FuncSearchResultItem> videos = new ArrayList<FuncSearchResultItem>();
				HashMap<Integer, FuncSearchResultItem> map = new HashMap<Integer, FuncSearchResultItem>();
				String app = ApplicationProxy.getContext()
						.getString(R.string.appfunc_search_type_apps);
				String image = ApplicationProxy.getContext().getString(
						R.string.appfunc_search_type_image);
				String music = ApplicationProxy.getContext().getString(
						R.string.appfunc_search_type_music);
				String video = ApplicationProxy.getContext().getString(
						R.string.appfunc_search_type_video);
				for (FuncSearchResultItem resultItem : uiResultItems) {
					switch (resultItem.mType) {
						case FuncSearchResultItem.ITEM_TYPE_RESULT_HEADER :
							int key = -1;
							if (resultItem.mTitle.contains(image)) {
								key = FuncSearchResultItem.ITEM_TYPE_LOCAL_IMAGE;
							} else if (resultItem.mTitle.contains(app)) {
								key = FuncSearchResultItem.ITEM_TYPE_LOCAL_APPS;
							} else if (resultItem.mTitle.contains(music)) {
								key = FuncSearchResultItem.ITEM_TYPE_LOCAL_AUDIO;
							} else if (resultItem.mTitle.contains(video)) {
								key = FuncSearchResultItem.ITEM_TYPE_LOCAL_VIDEO;
							}
							map.put(key, resultItem);
							break;
						case FuncSearchResultItem.ITEM_TYPE_LOCAL_APPS :
							localApps.add(resultItem);
							break;
						case FuncSearchResultItem.ITEM_TYPE_LOCAL_IMAGE :
							images.add(resultItem);
							break;
						case FuncSearchResultItem.ITEM_TYPE_LOCAL_AUDIO :
							audios.add(resultItem);
							break;
						case FuncSearchResultItem.ITEM_TYPE_LOCAL_VIDEO :
							videos.add(resultItem);
							break;
						case FuncSearchResultItem.ITEM_TYPE_SEARCH_WEB :
							map.put((int) FuncSearchResultItem.ITEM_TYPE_SEARCH_WEB, resultItem);
							break;
						default :
							break;
					}
				}

				if (!localApps.isEmpty()) {
					genSearchResulInfoList(results, localApps,
							FuncSearchResultItem.ITEM_TYPE_LOCAL_APPS, map);
					mWebAppInsertPos = results.size();
//					results.add(map.get((int) FuncSearchResultItem.ITEM_TYPE_SEARCH_WEB));
					mLocalAppWithoutRet = false;
				} else {
					mLocalAppWithoutRet = true;
				}

				if (!images.isEmpty()) {
					genSearchResulInfoList(results, images,
							FuncSearchResultItem.ITEM_TYPE_LOCAL_IMAGE, map);
				}
				if (!audios.isEmpty()) {
					genSearchResulInfoList(results, audios,
							FuncSearchResultItem.ITEM_TYPE_LOCAL_AUDIO, map);
				}
				if (!videos.isEmpty()) {
					genSearchResulInfoList(results, videos,
							FuncSearchResultItem.ITEM_TYPE_LOCAL_VIDEO, map);
				}
				mDrawerSearchMainView.notifyLocalSearchResultChanged(results, false);
			} else if (type == AppfuncSearchEngine.SEARCH_TYPE_WEB && mSearchStamp == timestamp) {
				Log.i("dzj", "webSearchKey--->" + searchKey);
				uiResultItems.remove(0);
				genSearchResulInfoList(results, uiResultItems,
						FuncSearchResultItem.ITEM_TYPE_APP_CENTER_APPS, null);
				mWebAppList = results;
				int dstPos = 2 == results.size() ? 2 : Math.min(2, results.size() - 1);
				ArrayList<Object> firstRoundResult = null;
				if (uiResultItems.size() <= getColumNums()) {
					firstRoundResult = new ArrayList<Object>(results);
					dstPos = 0;
				} else {
					firstRoundResult = new ArrayList<Object>(results.subList(0, dstPos));
				}
				mWebAppList.removeAll(firstRoundResult);
				FuncSearchResultItem header = new FuncSearchResultItem();
				header.mType = FuncSearchResultItem.ITEM_TYPE_RESULT_HEADER;
				header.mTitle = ApplicationProxy.getContext().getString(
						R.string.appfunc_search_online_apps);
				firstRoundResult.add(0, header);
				int moreWebAppInsertDst = firstRoundResult.size();
				Resources res = ApplicationProxy.getContext().getResources();
				FuncSearchResultItem resultItem = new FuncSearchResultItem();
				resultItem.mType = FuncSearchResultItem.ITEM_TYPE_SEARCH_WEB;
				resultItem.mTitle = !mWebAppList.isEmpty()
						? res.getString(com.gau.go.launcherex.R.string.appfunc_search_get_more_apps)
						: res.getString(com.gau.go.launcherex.R.string.appfunc_search_in_market);
				firstRoundResult.add(resultItem);
				if (mLocalAppWithoutRet) {
					mWebAppInsertPos = 0;
					mLocalAppWithoutRet = false;
				}
				if (mWebAppInsertPos != -1) {
					mDrawerSearchMainView.notifyWebSearchResultChanged(firstRoundResult,
							mWebAppInsertPos);
					mMoreWebAppInsertPos = mWebAppInsertPos + moreWebAppInsertDst;
					RealTimeStatisticsUtil.upLoadAppDrawerSearch("s001", searchKey, null, null,
							null, "1");
				}
			}
		}
	}

	public void genSearchResulInfoList(ArrayList<Object> results,
			ArrayList<FuncSearchResultItem> srcList, int type,
			HashMap<Integer, FuncSearchResultItem> map) {
		if (map != null) {
			results.add(map.get(type));
		}
		ArrayList<FuncSearchResultItem> localAppsCopy = (ArrayList<FuncSearchResultItem>) srcList
				.clone();
		int colum = getColumNums();
		for (int i = 0, j = colum; j < srcList.size(); i += colum, j += colum) {
			SearchResultListInfo resultListInfo = new SearchResultListInfo();
			resultListInfo.setType(type);
			List<FuncSearchResultItem> tempSubList = srcList.subList(i, j);
			ArrayList<FuncSearchResultItem> funcSearchResultItems = new ArrayList<FuncSearchResultItem>(
					tempSubList);
			resultListInfo.setSearchResultItems(funcSearchResultItems);
			results.add(resultListInfo);
			localAppsCopy.removeAll(funcSearchResultItems);
		}
		SearchResultListInfo resultListInfo = new SearchResultListInfo();
		resultListInfo.setType(type);
		resultListInfo.setSearchResultItems(localAppsCopy);
		results.add(resultListInfo);
	}

	private int getColumNums() {
		int colum = -1;
		if (GoLauncherActivityProxy.isPortait()) {
			int iconStandardSize = DrawUtils.dip2px(56);
			int gridWidthV = GoLauncherActivityProxy.getScreenWidth();
			int iconWidth = Math.round(1.0f
					* IconUtilities.getStandardIconSize(ApplicationProxy.getContext())
					* DrawUtils.dip2px(80) / iconStandardSize);
			colum = gridWidthV / iconWidth;
		} else {
			AppFuncAutoFitManager autoFitManager = AppFuncAutoFitManager.getInstance(ApplicationProxy.getContext());
			colum = autoFitManager.getAppDrawerColumnsH();
		}
		return colum;
	}

	@Override
	public void onSearchException(String searchKey, Object timestamp, int type) {
		switch (type) {
			case AppfuncSearchEngine.SEARCH_TYPE_WEB :
				tipsConnectFail(timestamp);
				break;
			default :
				break;
		}
	}

	private void tipsConnectFail(Object timestamp) {
		if (mSearchStamp == timestamp && mLocalAppWithoutRet) {
			mDrawerSearchMainView.notifyLocalSearchResultChanged(null, false);
			mDrawerSearchMainView.post(new Runnable() {

				@Override
				public void run() {
					DeskToast.makeText(ApplicationProxy.getContext(), R.string.http_exception,
							Toast.LENGTH_SHORT).show();
				}
			});
			clearWebCache();
		}
	}

	private void clearWebCache() {
		if (mWebAppList != null) {
			mWebAppList.clear();
			mWebAppInsertPos = -1;
			mMoreWebAppInsertPos = -1;
		}
	}

	@Override
	public void onHistoryChange(List<?> list, int historyType, boolean hasHistory, boolean isNotifly) {
		switch (historyType) {
			case AppfuncSearchEngine.HISTORY_TYPE_LOCAL :
				if (hasHistory) {
					ArrayList<FuncSearchResultItem> resultItems = (ArrayList<FuncSearchResultItem>) list;
					ArrayList<Object> results = new ArrayList<Object>();
					ArrayList<FuncSearchResultItem> localApps = new ArrayList<FuncSearchResultItem>();
					ArrayList<FuncSearchResultItem> medias = new ArrayList<FuncSearchResultItem>();
					for (FuncSearchResultItem resultItem : resultItems) {
						switch (resultItem.mType) {
							case FuncSearchResultItem.ITEM_TYPE_LOCAL_HISTORY_APPS :
//								resultItem.mType = FuncSearchResultItem.ITEM_TYPE_LOCAL_APPS;
								localApps.add(resultItem);
								break;
							case FuncSearchResultItem.ITEM_TYPE_LOCAL_HISTORY_MEDIA :
								medias.add(resultItem);
								break;
						}
					}
					results.add(resultItems.get(0));
					if (!localApps.isEmpty()) {
						genSearchResulInfoList(results, localApps,
								FuncSearchResultItem.ITEM_TYPE_LOCAL_HISTORY_APPS, null);
					}
					if (!medias.isEmpty()) {
						genSearchResulInfoList(results, medias,
								FuncSearchResultItem.ITEM_TYPE_LOCAL_HISTORY_MEDIA, null);
					}
					if (results.size() >= SEARCH_HISTROY_LITMIT) {
						List<Object> subList = results.subList(SEARCH_HISTROY_LITMIT,
								results.size());
						ArrayList<Object> objects = new ArrayList<Object>(subList);
						results.removeAll(objects);
					}
//					FuncSearchResultItem resultItem = new FuncSearchResultItem();
//					resultItem.mType = FuncSearchResultItem.ITEM_TYPE_CLEAR_HISTORY;
//					resultItem.mTitle = GOLauncherApp.getContext().getString(
//							R.string.appfunc_search_clear_history);
//					results.add(resultItem);
					mDrawerSearchMainView.notifyLocalSearchResultChanged(results, true);
				} else {
					//产品需求，不显示这个没有历史记录的提示。
					//					mDrawerSearchMainView.showNoSearchHistory();
				}
				break;

			default :
				break;
		}
	}

	@Override
	public void onSearchWithoutData(String searchKey, int type, Object timestamp) {
		synchronized (mLock) {
			switch (type) {
				case AppfuncSearchEngine.SEARCH_ALL_RESOURCE :
					if (mSearchStamp == timestamp) {
						mLocalAppWithoutRet = true;
					}
					break;
				case AppfuncSearchEngine.SEARCH_TYPE_WEB :
					if (mLocalAppWithoutRet && mSearchStamp == timestamp) {
						mDrawerSearchMainView.notifyLocalSearchResultChanged(null, false);
						clearWebCache();
					} else if (mSearchStamp == timestamp) {
						RealTimeStatisticsUtil.upLoadAppDrawerSearch("s001", searchKey, null, null,
								null, "0");
					}
					break;
				default :

					break;
			}
		}

	}

	@Override
	public void dataRefreshFinish(int type, ArrayList<FileInfo> data) {
		if (mSearchMedia && mSearchEngine.setMediaData(type, data)) {
			mDrawerSearchMainView.dataInitFinish();
		}
	}

	public void saveSearchResultHistory(String searchKey, int type) {
		mSearchEngine.putSearchKeyToSharedPreference(searchKey, type);
	}

	public void clearSearchHistory() {
		mSearchEngine.clearSearchKeysSharedPreference(AppfuncSearchEngine.HISTORY_TYPE_LOCAL, true);
	}

	public boolean isSearchMedia() {
		return mSearchMedia;
	}

}
