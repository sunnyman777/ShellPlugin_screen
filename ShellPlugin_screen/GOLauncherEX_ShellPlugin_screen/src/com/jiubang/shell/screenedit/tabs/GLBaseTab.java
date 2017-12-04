package com.jiubang.shell.screenedit.tabs;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLView;
import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditController;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.indicator.IndicatorListener;
import com.jiubang.shell.screenedit.tabs.ScreenEditImageLoader.IImageLoaderListener;

/**
 * 类描述:所有tab的公共父类
 * @author zouguiquan
 *
 */
public abstract class GLBaseTab implements IndicatorListener, Callback, IImageLoaderListener {

	public static final String TAB = "GLBaseTab";

	public int mTabLevel; 							// 所属的Tab级别
	protected int mPreTabId;
	protected int mTabId; 							// tabId标识
	protected int mTabHeight;
	protected int mCurrentScreen;
	protected long mLoadDataDelay;

	protected Context mContext;
	protected Context m2DContext;

	protected GLLayoutInflater mGlInflater;
	protected ScreenEditController mEditController;
	protected ScreenEditImageLoader mImageLoader;
	protected ILoadDataListener mLoadDataListener;

	protected ArrayList<Object> mDataList;
	private Object mMutex = new Object();
	private Handler mHandler = new Handler(Looper.getMainLooper(), this);

	private static final long CLICK_TIME = 600;
	private boolean mRefresh;
	private boolean mNeedChangeAnim;				//Tab切换时是否需要动画	
	private boolean mNeedEnterAnim;					//Tab出现时是否需要动画
	private boolean mNeedExitAnim;					//Tab退出时是否需要动画
	private boolean mIsLoading;
	private long mLastTime; 						//上次的点击时间
	private boolean mFirstClickFlag = false; 		//保证第一次点击不会被防止重复点击判断屏蔽掉 false为未点击

	protected boolean mNeedShowProgress = true;
	protected boolean mNeedAsyncLoadImage = true;

	private static final int MESSAGE_LOAD_START = 1;
	private static final int MESSAGE_LOAD_FINISH = 2;

	public GLBaseTab(Context context, int tabId, int tabLevel) {
		mContext = context;
		m2DContext = ShellAdmin.sShellManager.getActivity();
		mTabId = tabId;
		mTabLevel = tabLevel;
		mGlInflater = ShellAdmin.sShellManager.getLayoutInflater();

		Resources resources = context.getResources();
		if (tabLevel == ScreenEditConstants.TAB_LEVEL_1) {
			mTabHeight = (int) resources.getDimension(R.dimen.screen_edit_tab_height_normal);
		} else if (tabLevel == ScreenEditConstants.TAB_LEVEL_2
				|| tabLevel == ScreenEditConstants.TAB_LEVEL_3) {
			mTabHeight = ScreenEditController.getInstance().getLargeTabHeight();
		}

		mImageLoader = new ScreenEditImageLoader(R.drawable.gl_ic_launcher_application);
		mImageLoader.setImageLoaderListener(this);

		mEditController = ScreenEditController.getInstance();
		mLastTime = System.currentTimeMillis();
	}

	public abstract ArrayList<Object> requestData();

	public abstract void setParam(Object[] params);

	public abstract Object getParam();

	public abstract int getItemCount();

	public abstract Object getItem(int position);

	public abstract GLView getView(int position);

	public abstract GLView getContentView();

	public abstract void resetData();

	public void startLoadData() {

		Message message = mHandler.obtainMessage(MESSAGE_LOAD_START);
		mHandler.sendMessage(message);

		Thread thread = new Thread("Thread_ScreenEditLoadData") {
			@Override
			public void run() {
				synchronized (mMutex) {

					mDataList = requestData();
					Message message = mHandler.obtainMessage(MESSAGE_LOAD_FINISH);
					message.obj = mDataList;
					mHandler.sendMessageDelayed(message, mLoadDataDelay);
				}
			}
		};
		thread.start();
	}
	
	public void refreshTitle() {
	}

	public void refreshData() {
		mRefresh = true;
		startLoadData();
	}

	public Object requestTitleInfo() {
		return null;
	}

	public void onTitleClick(Object... obj) {
	}

	public void onResume() {
		if (mImageLoader != null) {
			mImageLoader.resume();
		}
	}

	public boolean onKeyBack() {
		if (mImageLoader != null) {
			mImageLoader.pause();
		}
		return false;
	}
	
	public boolean onBackExit() {
		return false;
	}

	public void handleMessage(int msgId, int param, Object... objects) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case MESSAGE_LOAD_START :
				loadStart();
				break;

			case MESSAGE_LOAD_FINISH :
				ArrayList<Object> resultData = (ArrayList<Object>) msg.obj;
				loadFinish(resultData);
				break;

			default :
				break;
		}
		return true;
	}

	private void loadStart() {
		onLoadDataStart();
		if (mLoadDataListener != null) {
			mLoadDataListener.onLoadDataStart();
		}
	}

	protected void onLoadDataStart() {
		mIsLoading = true;
	}

	/**
	 * 加载完毕后，把数据提供给UI更新
	 * @param dataList
	 */
	private void loadFinish(ArrayList<Object> dataList) {
		onloadDataFinish(dataList);
		if (mLoadDataListener != null) {
			if (mRefresh) {
				mLoadDataListener.onRefreshDataFinish();
			} else {
				mLoadDataListener.onLoadDataFinish();
			}
		}

		if (mRefresh) {
			onRefreshDataFinish();
		}

		mIsLoading = false;
	}

	/**
	 * 每个Tab的UI更新逻辑
	 * @param dataList
	 */
	protected void onloadDataFinish(ArrayList<Object> dataList) {
	}

	protected void onRefreshDataFinish() {
		mRefresh = false;
	}

	public boolean isLoading() {
		return mIsLoading;
	}

	public boolean isMultipleClick() {
		boolean result = false;
		long curTime = System.currentTimeMillis();
		if (curTime - mLastTime < CLICK_TIME && mFirstClickFlag) {
			result = true;
		}
		mFirstClickFlag = true;
		mLastTime = curTime;
		return result;
	}

	public int getPreTabId() {
		return mPreTabId;
	}

	public int getCurrentScreen() {
		return mCurrentScreen;
	}

	public void setCurrentScreen(int currentScreen) {
		mCurrentScreen = currentScreen;
	}

	/**
	 *  获取tabId标签
	 * @return
	 */
	public int getTabId() {
		return mTabId;
	}

	/**
	 * 获取当前tab的高度
	 * @return
	 */
	public int getTabHeight() {
		return mTabHeight;
	}

	public int getTabLevel() {
		return mTabLevel;
	}

	/**
	 * 要对应用安装或卸载事件的处理
	 * @param msgId
	 * @param pkgName
	 * @param showing
	 */
	public void handleAppChanged(int msgId, String pkgName, boolean showing) {
		if (mDataList != null) {
			mDataList.clear();
		}
		if (mImageLoader != null) {
			mImageLoader.clear();
		}
	}

	/**
	 * 获取当前tab有多少页
	 * @return
	 */
	public int getTotalPage() {
		return 0;
	}

	/**
	 * 返回每一页包含多少个View
	 * @return
	 */
	public int getPageSize() {
		return 0;
	}

	public void setIndicator(GLView indicator) {
	}

	public void setLoadDataListener(ILoadDataListener listener) {
		mLoadDataListener = listener;
	}

	/***
	 * 查看桌面是否还能放下指定大小的组件
	 * 
	 * @param spanX 行
	 * @param spanY 列
	 * @return
	 */
	protected boolean checkScreenVacant(int spanX, int spanY) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(spanX);
		list.add(spanY);

		int[] centerXY = new int[2]; // 将要添加到的位置对应当前屏幕的坐标XY
		float[] translate = new float[2];
		return MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_EDIT_PRE_ADD, 0, list, centerXY, translate);
	}

	public ScreenEditImageLoader getImageLoader() {
		return mImageLoader;
	}

	public boolean needAsyncLoadImage() {
		return mNeedAsyncLoadImage;
	}

	public boolean needEnterAnim() {
		return mNeedEnterAnim;
	}

	public void setNeedEnterAnim(boolean needShowInAnim) {
		mNeedEnterAnim = needShowInAnim;
	}

	public boolean needChangeAnim() {
		return mNeedChangeAnim;
	}

	public void setNeedChangeAnim(boolean needChangeAnim) {
		mNeedChangeAnim = needChangeAnim;
	}

	public boolean needExitAnim() {
		return mNeedExitAnim;
	}

	public void setNeedExitAnim(boolean needExitAnim) {
		mNeedExitAnim = needExitAnim;
	}

	public boolean isRefresh() {
		return mRefresh;
	}
	
	public boolean needShowProgress() {
		return mNeedShowProgress;
	}

	/**
	 * 清除数据
	 */
	public void clear() {
		mGlInflater = null;
		mIsLoading = false;
		if (mDataList != null) {
			mDataList.clear();
		}
		if (mImageLoader != null) {
			mImageLoader.clear();
		}
		return;
	}

	/**
	 * 
	 * @author zouguiquan
	 *
	 */
	public interface ILoadDataListener {
		public void onLoadDataStart();
		public void onRefreshDataFinish();
		public void onLoadDataFinish();
	}
}
