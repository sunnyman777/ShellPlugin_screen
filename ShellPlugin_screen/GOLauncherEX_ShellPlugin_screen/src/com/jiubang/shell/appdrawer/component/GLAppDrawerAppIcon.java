package com.jiubang.shell.appdrawer.component;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.widget.GLTextViewWrapper;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherLogicProxy;
import com.go.util.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.config.GOLauncherConfig;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.plugin.ISecurityPoxy;
import com.jiubang.shell.common.component.GLModel3DMultiView;
import com.jiubang.shell.common.component.GLModel3DView;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.listener.UpdateListener;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.model.CommonImageManager;
import com.jiubang.shell.model.IModelItemType;
import com.jiubang.shell.model.IModelState;
import com.jiubang.shell.utils.IconUtils;

/**
 * 功能表的图标基类
 * @author wuziyi
 */
public class GLAppDrawerAppIcon extends IconView<FunAppItemInfo> implements BroadCasterObserver {
	// 内部消息
	protected static final int MSG_SHOWTIPS = 0; // 弹出对话框
	protected static final int MSG_CHANGE_TITLE = 1;
	protected static final int MSG_CHANGE_ICON = 2;

	protected GLModel3DMultiView mMultiView;

	protected GLModel3DView mItemView;
	protected GLTextViewWrapper mTitleView;
	protected Handler mHandler;
	private FuncAppDataHandler mDataHandler;
	/**
	 * 右上角可更新应用数（应用中心，安卓市场）
	 */
	private int mUpdateableAppCount = -1;
	/**
	 * 是否需要显示右上角更新应用数（应用中心，安卓市场）
	 */
	private boolean mIsShowUpdateCount = false;

	public GLAppDrawerAppIcon(Context context) {
		this(context, null);
	}

	public GLAppDrawerAppIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		initHandler();
//		CommonImageManager.getInstance().registerObserver(this);
		mDataHandler = FuncAppDataHandler.getInstance(ShellAdmin.sShellManager.getActivity());
		setEnableAutoTextLine(true);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mMultiView = (GLModel3DMultiView) findViewById(R.id.multmodel);
		mItemView = (GLModel3DView) mMultiView.findViewById(R.id.model);
		mTitleView = (GLTextViewWrapper) findViewById(R.id.app_name);
		mMultiView.setBgImageDrawable(CommonImageManager.getInstance().getDrawable(
				CommonImageManager.RES_FOLDER_BG));
		initIconFromSetting(false);
	}

//	@Override
//	protected void onLayout(boolean changed, int l, int t, int r, int b) {
//		super.onLayout(changed, l, t, r, b);
////		initIconFromSetting(false);
//	}
	@Override
	public void setIcon(BitmapDrawable drawable) {
		if (drawable != null) {
			mItemView.changeTexture(drawable.getBitmap());
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitleView.setText(title);
	}

	public void setTitleHide(int hide) {
		mTitleView.setVisibility(hide);
	}

	public GLModel3DMultiView getMultiView() {
		return mMultiView;
	}

	@Override
	public void setInfo(FunAppItemInfo info) {
		FunAppItemInfo oldInfo = mInfo;
		if (oldInfo != null) {
			oldInfo.unRegisterObserver(this);
		}
		super.setInfo(info);
		if (mInfo != null) {
			mInfo.registerObserver(this);
		}
	}

	@Override
	public void onBCChange(int msgId, int param, Object ...object) {
		switch (msgId) {
			case FunAppItemInfo.RESETBEAN : {
				if (mInfo != null) {
					//				try {
					mItemView.setTexture(mInfo.getAppItemInfo().getIcon());
					//				} catch (Exception e) {
					//					e.printStackTrace();
					//				}
				}
				break;
			}
			case AppItemInfo.INCONCHANGE : {
				Message msg = mHandler.obtainMessage();
				msg.what = MSG_CHANGE_ICON;
				mHandler.sendMessage(msg);
				break;
			}
			case AppItemInfo.TITLECHANGE : {
				Message msg = mHandler.obtainMessage();
				msg.what = MSG_CHANGE_TITLE;
				mHandler.sendMessage(msg);
				break;
			}

			case AppItemInfo.UNREADCHANGE : {
				int currentState = mMultiView.getCurrentState();
				if (currentState != IModelState.UNINSTALL_STATE
						&& currentState != IModelState.KILL_STATE) {
					post(new Runnable() {

						@Override
						public void run() {
							// 为了避免在功能表编辑抖动状态下，因为换右上角标志，引发刷新
							checkSingleIconNormalStatus();
						}
					});
				}
				break;
			}

			case AppItemInfo.IS_NEW_APP_CHANGE : {
				if (object[0] instanceof Boolean) {
					int currentState = mMultiView.getCurrentState();
					if (currentState != IModelState.UNINSTALL_STATE
							&& currentState != IModelState.KILL_STATE) {
						post(new Runnable() {

							@Override
							public void run() {
								// 为了避免在功能表编辑抖动状态下，因为换右上角标志，引发刷新
								checkSingleIconNormalStatus();
							}
						});
					}
				}
				break;
			}
			case ISecurityPoxy.SECURITY_STATE_CHANGED : {
				int currentState = mMultiView.getCurrentState();
				if (currentState != IModelState.UNINSTALL_STATE
						&& currentState != IModelState.KILL_STATE) {
					post(new Runnable() {

						@Override
						public void run() {
							// 为了避免在功能表编辑抖动状态下，因为换右上角标志，引发刷新
							checkSingleIconNormalStatus();
						}
					});
				}
				break;
			}
			//			case IScreenFrameMsgId.COMMON_IMAGE_CHANGED : {
//				mMultiView.setBgImageDrawable(CommonImageManager.getInstance().getDrawable(
//						CommonImageManager.RES_FOLDER_BG));
//				break;
//			}
			default :
				break;
		}
	}

	private void initHandler() {
		/**
		 * @edit by huangshaotao
		 * @date 2012-4-26 使用主线程的looper来初始化handler。因为在处理sd加载事件时是在子线程中进行的，
		 *       这时候如果需要创建applicationIcon对象，
		 *       而子线程又没有looper就会报java.lang.RuntimeException: Can't create
		 *       handler inside thread that has not called Looper.prepare() 异常
		 *       所以这里给handler指定用主线程的looper
		 */
		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_SHOWTIPS : {
						//						showTips();
						break;
					}
					case MSG_CHANGE_TITLE : {
						if (mInfo != null) {
							mTitleView.setText(mInfo.getTitle());
						}
						break;
					}
					case MSG_CHANGE_ICON : {
						if (mInfo != null) {
							mItemView.changeTexture(mInfo.getAppItemInfo().getIcon().getBitmap());
						}
					}
					default :
						break;
				}
				invalidate();
				if (mIconRefreshObserver != null) {
					mIconRefreshObserver.onIconRefresh();
				}
			}
		};
	}

	@Override
	public void refreshIcon() {
		if (mInfo != null) {
			mItemView.setModelItem(IModelItemType.GENERAL_ICON);
			mItemView.changeTexture(mInfo.getAppItemInfo().getIcon().getBitmap());
			String title = mInfo.getTitle();
			if (title == null || title.trim().equals("")) {
				title = "Loading...";
			}
			if (mTitleView != null) {
				mTitleView.setText(title);
				mTitleView.setTextSize(GoLauncherLogicProxy.getAppFontSize());
				//				mTextView.setMaxLines(DEFAULT_TEXT_MAX_LINES);
				//				mTextView.setMinLines(DEFAULT_TEXT_MAX_LINES);
			}
			if (mIconRefreshObserver != null) {
				mIconRefreshObserver.onIconRefresh();
			}
		}
	}

	@Override
	public void cleanup() {
		if (mInfo != null) {
			mInfo.unRegisterObserver(this);
		}
		super.cleanup();
		//		CommonImageManager.getInstance().unRegisterObserver(this);
	}

	@Override
	public void onIconRemoved() {
//		CommonImageManager.getInstance().unRegisterObserver(this);
	}

	/**
	 * 检查单个图标在Normal状态下的当前状态
	 */
	@Override
	public void checkSingleIconNormalStatus() {
		if (mInfo != null) {
//			int dangerLevel = ISecurityPoxy.DANGER_LEVEL_NONE;
//			try {
//				dangerLevel = SecurityPoxyFactory.getSecurityPoxy().getDangerLevel(mInfo);
//			} catch (UnsupportSecurityPoxyException e) {
//			}
//			if (dangerLevel > ISecurityPoxy.DANGER_LEVEL_UNKNOW) {
//				if (dangerLevel == ISecurityPoxy.DANGER_LEVEL_SAFE) { // 扫描结果安全
//					mMultiView.setCurrenState(IModelState.SAFE_STATE, null);
//				} else if (dangerLevel == ISecurityPoxy.DANGER_LEVEL_UNSAFE) { // 扫描结果危险
//					mMultiView.setCurrenState(IModelState.DANGER_STATE, null);
//				}
//				mMultiView.setOnSelectClickListener(null);
//			} else 
			if (mInfo.isNew()) { // 显示New标识
				mMultiView.setCurrenState(IModelState.NEW_STATE);
				mMultiView.setOnSelectClickListener(null);
			} else if (mInfo.isUpdate() && mDataHandler.isShowAppUpdate()) { // 如果有更新信息，显示更新图标
				mMultiView.setCurrenState(IModelState.UPDATE_STATE);
				mMultiView.setOnSelectClickListener(new UpdateListener(mInfo));
			} else if (mIsShowUpdateCount && mUpdateableAppCount > 0
					&& mDataHandler.isShowAppUpdate()) { // 安卓应用市场的数字
				mMultiView.setCurrenState(IModelState.STATE_COUNT, mUpdateableAppCount);
				mMultiView.setOnSelectClickListener(null);
			} else if (mInfo.getUnreadCount() > 0) { // 通讯统计的未读数字
				mMultiView.setCurrenState(IModelState.STATE_COUNT, mInfo.getUnreadCount());
				mMultiView.setOnSelectClickListener(null);
			} else { // 没有任何状态
				mMultiView.setCurrenState(IModelState.NO_STATE);
				mMultiView.setOnSelectClickListener(null);
			}
		}
	}

	/**
	 * 检查该图标是否需要显示可更新应用数
	 */
	public boolean checkNeedShowUpdateCount() {
		if (mInfo == null) {
			return false;
		}
		mIsShowUpdateCount = showUpdateCount(mInfo);
		if (mIsShowUpdateCount) {
			updateBeanListCount();
		}
		return mIsShowUpdateCount;
	}

	/**
	 * <br>功能简述: 判断图标是否需要显示更新数字
	 * <br>功能详细描述:
	 * <br>注意: 只有GO精品或应用中心的图标上面需要显示
	 * @param appInfo
	 * @return
	 * add by zhoujun 2012-09-26
	 */
	private boolean showUpdateCount(FunAppItemInfo appInfo) {
		if (null != appInfo.getIntent() && null != appInfo.getIntent().getComponent()) {
			String commponent = appInfo.getIntent().getComponent().toString();
			if (AppFuncConstants.APPGAME_APP_CENTER_COMPENTANME.equals(commponent)
					|| AppFuncConstants.GOSTORECOMPONENTNAME.equals(commponent)) {
				final ChannelConfig channelConfig = GOLauncherConfig.getInstance(ApplicationProxy.getContext()).getChannelConfig();
				if (!channelConfig.isNeedAppCenter()) {
					// 国内353渠道包，只有Go精品图标上面需要显示更新数字
					if (AppFuncConstants.GOSTORECOMPONENTNAME.equals(commponent)) {
						return true;
					}
				} else {
					//其他渠道，只有应用中心图标上需要显示更新数字
					if (AppFuncConstants.APPGAME_APP_CENTER_COMPENTANME.equals(commponent)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 更新右上角可更新应用数
	 */
	public void updateBeanListCount() {
		new AsyncTask<Void, Void, Integer>() {
			@Override
			protected Integer doInBackground(Void... params) {
				return AppDrawerControler.getInstance(ShellAdmin.sShellManager.getActivity())
						.getUpdateableAppCount();
			};

			protected void onPostExecute(Integer count) {
				int currentState = mMultiView.getCurrentState();
				if (mUpdateableAppCount != count
						&& currentState != IModelState.UNINSTALL_STATE
						&& currentState != IModelState.KILL_STATE) {
					mUpdateableAppCount = count;
					checkSingleIconNormalStatus();
				}
			};
		}.execute();
	}

	@Override
	public void setPressed(boolean pressed) {
		super.setPressed(pressed);
		if (pressed) {
			setAlpha(CLICK_HALF_ALPHA);
		} else {
			setAlpha(CLICK_NO_ALPHA);
		}
	}
	
	@Override
	public void reloadResource() {
		if (mMultiView != null) {
			mMultiView.setBgImageDrawable(CommonImageManager.getInstance().getDrawable(
					CommonImageManager.RES_FOLDER_BG));
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (getGLParent() instanceof GLAppDrawerBaseGrid && mTitleView != null
				&& mTitleView.isVisible()) {
			IconUtils.sAppDrawerIconTextHeight = mTitleView.getHeight();
		}
	}
}
