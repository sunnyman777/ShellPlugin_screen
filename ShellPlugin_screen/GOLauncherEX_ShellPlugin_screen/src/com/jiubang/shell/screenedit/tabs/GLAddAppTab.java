package com.jiubang.shell.screenedit.tabs;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.animation.Scale3DAnimation;
import com.go.gl.animation.Translate3DAnimation;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.gl.widget.GLImageView;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.util.SortHelper;
import com.go.util.sort.CompareClickedMethod;
import com.go.util.sort.CompareMethod;
import com.go.util.sort.CompareTimeMethod;
import com.go.util.sort.CompareTitleMethod;
import com.go.util.sort.IBaseCompareable;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenEditMsgId;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.desks.diy.pref.PrefConst;
import com.jiubang.ggheart.apps.desks.diy.pref.PrivatePreference;
import com.jiubang.ggheart.apps.gowidget.ScreenEditItemInfo;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.shell.animation.DropAnimation;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.common.component.GLDragLayer;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.screenedit.GLAppsAdapter;
import com.jiubang.shell.utils.ToastUtils;

/**
 * 添加应用程序
 * @author zouguiquan
 *
 */
public class GLAddAppTab extends GLGridTab implements BatchAnimationObserver {

	private GLView mFlyView;

	private ScreenEditItemInfo mItemInfo;
	private ArrayList<AppItemInfo> mAppList;

	private int mOrderType;

	private final static int ON_FLY_FINISH = 1001;					// 添加动画完成时
	public static final float AUTO_FLY_SCALE = 1.5f;				// 飞行动画初始放大值
	public static final float VELOCITY_FOR_AUTO_FLY_APP = 7.6f; 	// 添加应用/文件夹图标飞的速率，单位px/ms

	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ON_FLY_FINISH :

					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_EDIT_ITEM_TO_SCREEN,
							ScreenEditConstants.ADD_APPS_ID, msg.obj);
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.ALLOW_SCREEN_TO_SCROLL, -1);

					//用post方法执行removeFlyView()，解决飞入动画最后一帧闪的问题
					post(new Runnable() {

						@Override
						public void run() {
							removeFlyView();
						}
					});
					break;
			}
		};
	};

	public GLAddAppTab(Context context, int tabId, int tabLevel) {
		super(context, tabId, tabLevel);
		mNeedAsyncLoadImage = false;
		mPreTabId = ScreenEditConstants.TAB_ID_MAIN;
	}

	@Override
	public void onResume() {
		PrivatePreference pref = PrivatePreference.getPreference(ApplicationProxy.getContext());
		mOrderType = pref.getInt(PrefConst.KEY_SCREEN_EDIT_APPS_ORDER_TYPE,
				ScreenEditConstants.TAB_APPS_ORDER_BY_NAME);
	}

	@Override
	public ArrayList<Object> requestData() {

		if (mDataList != null && mDataList.size() > 0 && !isRefresh()) {
			return mDataList;
		}

		if (mAppList == null || mAppList.size() <= 0) {
			final AppDataEngine engine = AppDataEngine.getInstance(ApplicationProxy.getContext());
			mAppList = engine.getCompletedAppItemInfosExceptHide();
		}

		ArrayList<Object> dataList = null;

		try {

			if (mAppList.size() > 0) {
				dataList = new ArrayList<Object>();

				CompareMethod<? extends IBaseCompareable> method = null;
				if (mOrderType == ScreenEditConstants.TAB_APPS_ORDER_BY_NAME) {
					method = new CompareTitleMethod();
				} else if (mOrderType == ScreenEditConstants.TAB_APPS_ORDER_BY_TIME) {
					method = new CompareTimeMethod(m2DContext);
					method.setOrder(CompareMethod.DESC);
				} else if (mOrderType == ScreenEditConstants.TAB_APPS_ORDER_BY_FREQUENCY) {
					method = new CompareClickedMethod(m2DContext);
					method.setOrder(CompareMethod.DESC);
				}

				if (method != null) {
					SortHelper.doSort(mAppList, method);
					for (AppItemInfo info : mAppList) {
						if (info.mIntent != null && info.mIntent.getComponent() != null) {
							dataList.add(info);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dataList;
	}

	@Override
	public void refreshTitle() {
		MsgMgrProxy.sendHandler(this, IDiyFrameIds.SCREEN_EDIT,
				IScreenEditMsgId.SCREEN_EDIT_REFRESH_TITLE, mTabId, mOrderType);
	}

	@Override
	public void handleAppChanged(int msgId, String pkgName, boolean showing) {

		super.handleAppChanged(msgId, pkgName, showing);

		if (mAppList != null) {
			mAppList.clear();
		}

		if (showing) {
			refreshData();
		}
	}

	@Override
	public ShellBaseAdapter<Object> createAdapter(Context mContext, List<Object> infoList) {
		return new GLAppsAdapter(mContext, infoList);
	}

	@Override
	protected void onRefreshDataFinish() {

		PrivatePreference pref = PrivatePreference.getPreference(ApplicationProxy.getContext());
		pref.putInt(PrefConst.KEY_SCREEN_EDIT_APPS_ORDER_TYPE, mOrderType);
		pref.commit();

		if (mOrderType == ScreenEditConstants.TAB_APPS_ORDER_BY_NAME) {
			ToastUtils.showToast(R.string.screenedit_app_sort_by_name, Toast.LENGTH_SHORT);
		} else if (mOrderType == ScreenEditConstants.TAB_APPS_ORDER_BY_TIME) {
			ToastUtils.showToast(R.string.screenedit_app_sort_by_time, Toast.LENGTH_SHORT);
		} else if (mOrderType == ScreenEditConstants.TAB_APPS_ORDER_BY_FREQUENCY) {
			ToastUtils.showToast(R.string.screenedit_app_sort_by_frequency, Toast.LENGTH_SHORT);
		}
		refreshTitle();

		super.onRefreshDataFinish();
	}

	@Override
	public void onTitleClick(Object... obj) {
		if (isLoading()) {
			return;
		}

		int orderType = (Integer) obj[0];
		if (mOrderType != orderType) {
			mOrderType = orderType;
			refreshData();
		}
	}

	@Override
	public Object requestTitleInfo() {
		return mOrderType;
	}

	@Override
	public void onItemClick(GLAdapterView<?> adapter, GLView view, int position, long arg3) {
		super.onItemClick(adapter, view, position, arg3);

		// 如果飞行动画正在进行，则不会进行第二次的添加
		if (mFlyView != null) {
			return;
		}

		if (!resetTag(view)) {
			return;
		}

		int[] centerXY = new int[2]; 					// 将要添加到的位置对应当前屏幕的坐标XY
		float[] translate = new float[2]; 				// 当前屏幕的缩放比例
		if (checkScreenVacant(1, 1, centerXY, translate)) {
			flyToScreen(view, centerXY, translate);		// 进行添加的飞行动画
		}
	}

	/**
	 * 进行飞行的动画
	 * @param flyView
	 * @param flyType
	 * @param centerXY 长度为2
	 * @param translate 长度为2
	 */
	private void flyToScreen(GLView flyView, int[] centerXY, float[] translate) {
		int[] loc = new int[2];
		ShellAdmin.sShellManager.getShell().getContainer().getLocation(flyView, loc);
		int centerX = (int) (loc[0] + flyView.getWidth() / 2 - flyView.getWidth()
				* (AUTO_FLY_SCALE - 1) / 2);
		int centerY = (int) (loc[1] + flyView.getHeight() / 2 - flyView.getHeight()
				* (AUTO_FLY_SCALE - 1) / 2);
		mFlyView = cloneIcon(flyView, centerX, centerY);
		if (mFlyView == null) {
			return;
		}
		AnimationTask task = new AnimationTask(true, AnimationTask.PARALLEL);
		ShellAdmin.sShellManager.getShell().getDragLayer().addView(mFlyView);

		int offsetX = centerXY[0] - centerX;
		int offsetY = (int) (centerXY[1] - centerY - translate[0]);
		int duration = (int) (Math.sqrt(offsetX * offsetX + offsetY * offsetY + translate[1]
				* translate[1]) / VELOCITY_FOR_AUTO_FLY_APP);
		if (duration > DropAnimation.DURATION_320) {
			duration = DropAnimation.DURATION_320;
		}
		Animation moveAnimation = new Translate3DAnimation(0, offsetX, 0, -offsetY, 0, translate[1]);
		moveAnimation.setFillEnabled(true);
		moveAnimation.setFillAfter(true);
		moveAnimation.setDuration(duration/*DropAnimation.DURATION_300*/);
		moveAnimation.setInterpolator(InterpolatorFactory
				.getInterpolator(InterpolatorFactory.QUADRATIC));
		Animation scaleAnimation = new Scale3DAnimation(AUTO_FLY_SCALE, 1f, AUTO_FLY_SCALE, 1f, 1f,
				1f);
		scaleAnimation.setFillEnabled(true);
		scaleAnimation.setFillAfter(true);
		scaleAnimation.setDuration(duration/*DropAnimation.DURATION_300*/);
		scaleAnimation.setInterpolator(InterpolatorFactory
				.getInterpolator(InterpolatorFactory.QUADRATIC));
		AnimationSet set = new AnimationSet(false);
		set.addAnimation(moveAnimation);
		set.addAnimation(scaleAnimation);
		task.addAnimation(mFlyView, set, null);
		task.setBatchAnimationObserver(this, 0, flyView);

		// 锁定屏幕不能移动
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.LOCK_SCREEN_TO_SCROLL,
				-1);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_ENABLE_CELLLAYOUT_DRAWING_CACHE, 1);
		GLAnimationManager.startAnimation(task);
	}

	private GLView cloneIcon(GLView view, int centerX, int centerY) {
		GLView originalIcon = view;
		originalIcon.setVisible(true);
		GLView cloneView = ShellAdmin.sShellManager.getLayoutInflater().inflate(
				R.layout.gl_screen_edit_item, null);
		Object itemInfo = view.getTag();
		if (itemInfo == null) {
			return null;
		}
		GLImageView image = (GLImageView) cloneView.findViewById(R.id.thumb);

		ShellTextViewWrapper text = (ShellTextViewWrapper) cloneView.findViewById(R.id.title);

		if (itemInfo instanceof AppItemInfo) {
			text.setText(((AppItemInfo) itemInfo).mTitle);
			image.setImageDrawable(((AppItemInfo) itemInfo).mIcon);
		} else if (itemInfo instanceof ShortCutInfo) {
			text.setText(((ShortCutInfo) itemInfo).mTitle);
			image.setImageDrawable(((ShortCutInfo) itemInfo).mIcon);
		} else {
			return null;
		}

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(0, 0);
		GLDragLayer.LayoutParams lp = new GLDragLayer.LayoutParams(params);
		lp.width = originalIcon.getWidth();
		lp.height = originalIcon.getHeight();
		lp.x = centerX - lp.width / 2;
		lp.y = centerY - lp.height / 2;
		lp.customPosition = false;
		cloneView.setLayoutParams(lp);
		return cloneView;
	}

	private void removeFlyView() {
		if (mFlyView != null) {
			ShellAdmin.sShellManager.getShell().getDragLayer().removeView(mFlyView);
			mFlyView.cleanup();
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
	private boolean checkScreenVacant(int spanX, int spanY, int[] centerXY, float[] translate) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(spanX);
		list.add(spanY);
		return MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_EDIT_PRE_ADD, 0, list, centerXY, translate);
	}

	/**
	 *  重置view的tag
	 * @param view
	 * @return
	 */
	private boolean resetTag(GLView view) {
		if (null == view || null == view.getTag()) {
			return false;
		}

		if (view.getTag() instanceof AppItemInfo) {
			ShortCutInfo ret = new ShortCutInfo();
			AppItemInfo info = (AppItemInfo) view.getTag();
			if (null == info) {
				return false;
			}
			ret.mIcon = info.mIcon;
			ret.mIntent = info.mIntent;
			ret.mItemType = IItemType.ITEM_TYPE_APPLICATION;
			ret.mSpanX = 1;
			ret.mSpanY = 1;
			ret.mTitle = info.mTitle;
			ret.mInScreenId = -1;
			ret.setRelativeItemInfo(info);
			view.setTag(ret);
			return true;
		} else if (view.getTag() instanceof ShortCutInfo) {
			ShortCutInfo info = (ShortCutInfo) view.getTag();
			info.mInScreenId = -1;
			return true;
		}

		return false;
	}

	@Override
	public int getItemCount() {
		return mDataList.size();
	}

	@Override
	public GLView getView(int position) {
		return null;
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
	public void setParam(Object[] params) {
		if (params != null) {
			mItemInfo = (ScreenEditItemInfo) params[0];
		}
	}

	@Override
	public Object getParam() {
		return mItemInfo;
	}
	
	@Override
	public boolean onKeyBack() {
		if (mFlyView != null) {
			return true;
		}
		
		return super.onKeyBack();
	}
}
