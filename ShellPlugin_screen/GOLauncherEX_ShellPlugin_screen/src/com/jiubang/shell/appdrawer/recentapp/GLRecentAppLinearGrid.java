package com.jiubang.shell.appdrawer.recentapp;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;
import com.jiubang.ggheart.apps.desks.appfunc.model.IBackgroundInfoChangedObserver;
import com.jiubang.ggheart.apps.desks.diy.guide.RateGuideTask;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunItemInfo;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.appdrawer.component.GLAppDrawerAppIcon;
import com.jiubang.shell.appdrawer.component.GLLinearScrollableGridView;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.common.component.HorScrollableGridViewHandler;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.utils.GLImageUtil;
/**
 * 最近打开的列表
 * @author wuziyi
 *
 */
public class GLRecentAppLinearGrid extends GLLinearScrollableGridView implements
		IBackgroundInfoChangedObserver, BatchAnimationObserver {
	
	private static final long ANIMATION_DURATION_ICON_SCALE = 200;
	private static final long ANIMATION_OFFSET_ICON_SCALE = 100;
	private static final int ANIMATION_KILL = 1;
	private static final int SHOW_LINE = 3;
	
	private long mStartOffset = -ANIMATION_OFFSET_ICON_SCALE;
	private boolean mDataUpdated = true;
	private AnimationTask mKillAnimTask;
	private AppDrawerControler mControler;
	
	private boolean mNeedKillAllAppAnimation = false;
	
	public GLRecentAppLinearGrid(Context context) {
		super(context);
		init();
	}

	public GLRecentAppLinearGrid(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		mKillAnimTask = new AnimationTask(false, AnimationTask.PARALLEL);
		mKillAnimTask.setBatchAnimationObserver(this, ANIMATION_KILL);

		mControler = AppDrawerControler.getInstance(ShellAdmin.sShellManager.getActivity());
		mControler.setRecentAppObserver(this);
		
		handleScrollerSetting();
		setFourceHorMode(true);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	public void onItemClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		if (view instanceof GLAppDrawerAppIcon) {
			GLAppDrawerAppIcon icon = (GLAppDrawerAppIcon) view;
			FunAppItemInfo info = icon.getInfo();
			Intent intent = info.getIntent();
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					ICommonMsgId.START_ACTIVITY, -1, intent, null);
			GuiThemeStatistics.sideOpStaticData("-1", "si_rnc_click", 1, "-1");
		}
	}

	@Override
	public boolean onItemLongClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		return true;
	}

	@Override
	public void callBackToChild(GLView view) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refreshGridView() {
		ArrayList<FunAppItemInfo> recentAppList = AppDrawerControler.getInstance(
				ShellAdmin.sShellManager.getActivity()).getRecentFunAppItems(SHOW_LINE * mNumColumns);
		if (recentAppList != null && !recentAppList.isEmpty()) {
			mNoData = false;
			setData(recentAppList);
		} else {
			mNoData = true;
			requestLayout();
		}
		mDataUpdated = false;
	}

	@Override
	public ShellBaseAdapter createAdapter(Context context, List infoList) {
		return new GLRecentAppAdapter(context, infoList);
	}

	@Override
	protected void onScrollStart() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleRowColumnSetting(boolean updateDB) {
		mNumRows = 1;
		mNumColumns = 4;
//		mNumColumns = AppFuncAutoFitManager.getInstance(ShellAdmin.sShellManager.getActivity())
//				.getFolderQuickAddBarItemCountV();
	}
	
	public void notifyDataSetChanged() {
		if (isVisible()) {
			refreshGridView();
		} else {
			mDataUpdated = true;
		}
	}
	
	public boolean isNeedRefreash() {
		return mDataUpdated;
	}
	
	@Override
	protected GLDrawable getNextLabelV() {
		GLDrawable drawable = GLImageUtil.getGLDrawable(R.drawable.gl_appdrawer_slide_menu_next);
		return drawable;
	}
	
	@Override
	protected GLDrawable getPreLabelV() {
		GLDrawable drawable = GLImageUtil.getGLDrawable(R.drawable.gl_appdrawer_slide_menu_pre);
		return drawable;
	}
	
	private void killAllIconAnimation() {
		mStartOffset = -ANIMATION_OFFSET_ICON_SCALE;
		mKillAnimTask.reset();
		int currentScreen = ((HorScrollableGridViewHandler) mScrollableHandler).getCurrentScreen();
		ArrayList<GLView> children = ((HorScrollableGridViewHandler) mScrollableHandler)
				.getChildren(currentScreen);
		if (children != null) {
			int count = children.size();
			for (int i = count - 1; i >= 0; i--) {
				IconView<FunItemInfo> icon = (IconView<FunItemInfo>) children.get(i);
				FunAppItemInfo info = (FunAppItemInfo) icon.getInfo();
				if (!info.isIgnore()) {
					Animation animation = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF,
							0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					animation.setDuration(ANIMATION_DURATION_ICON_SCALE);
					mStartOffset += ANIMATION_OFFSET_ICON_SCALE;
					animation.setStartOffset(mStartOffset);
					animation.setFillAfter(true);
					mKillAnimTask.addAnimation(icon, animation, null);
					//				mProManageList.remove(info);
				}
			}
			if (mKillAnimTask.isValid()) {
				ShellAdmin.sShellManager.getShell().show(IViewId.PROTECTED_LAYER, false);
				GLAnimationManager.startAnimation(mKillAnimTask);
			}
		}
	}

	@Override
	public void onStart(int what, Object[] params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFinish(int what, Object[] params) {
		switch (what) {
			case ANIMATION_KILL :
				refreshGridView();
				ShellAdmin.sShellManager.getShell().hide(IViewId.PROTECTED_LAYER, false);
				
				RateGuideTask.getInstacne(ShellAdmin.sShellManager.getActivity()).scheduleShowRateDialog(
						RateGuideTask.EVENT_RECENTLY_CLEAN);
				break;
			default :
				break;
		}
	}

	@Override
	public boolean handleChanges(MessageID msgId, Object obj1, Object obj2) {
		switch (msgId) {
			case CLEAR_RECENTAPP:
				if (!mNeedKillAllAppAnimation) {
					killAllIconAnimation();
				}
				break;
	
			default:
				break;
		}
		return false;
	}
	
	public void removeAllRecentAppItems() {
		mControler.removeAllRecentAppItems();
	}
	public void removeAllRecentAppItemsWithoutAnimation() {
		mNeedKillAllAppAnimation = true;
		mControler.removeAllRecentAppItems();
		mNeedKillAllAppAnimation = false;
	}
	@Override
	protected String getNoDateText() {
		return mContext.getString(R.string.appfunc_no_recent_data);
	}

	@Override
	protected void onScreenChange(int newScreen, int oldScreen) {
		// TODO Auto-generated method stub
		
	}

	public boolean hasNoData() {		 
		return mNoData;
	}
}
