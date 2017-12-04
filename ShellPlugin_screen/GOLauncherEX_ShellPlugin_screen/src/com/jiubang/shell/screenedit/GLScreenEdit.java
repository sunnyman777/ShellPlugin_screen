package com.jiubang.shell.screenedit;

import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewDebug.CapturedViewProperty;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLRelativeLayout;
import com.go.gl.view.GLViewGroup;
import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.IAppCoreMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenEditMsgId;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditController;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.IShell;
import com.jiubang.shell.IView;
import com.jiubang.shell.screen.GLWorkspace;
import com.jiubang.shell.screen.back.GLBackWorkspace;
import com.jiubang.shell.screenedit.tabs.GLBaseTab;

/**
 * 
 * @author zouguiquan
 *
 */
public class GLScreenEdit extends GLRelativeLayout
		implements
			IView,
			IMessageHandler,
			AnimationListener {

	private GLScreenEditTitle mEditTitle;
	private GLScreenEditContainer mEditContainer;

	private GLBackWorkspace mBackWorkspace;
	private GLDrawable mBgDrawable;
	private GLBaseTab mBaseTab;
	private TabFactory mTabFactory;
	private IShell mIShell;

	private int mCurrentTab;
	private int mContainerHeight;
	private int mTitleHeight;
	private int mIndicateHeight;

	public GLScreenEdit(Context context) {
		this(context, null);
	}

	public GLScreenEdit(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTabFactory = TabFactory.getInstance(context);
		mTitleHeight = (int) getResources().getDimension(R.dimen.screen_edit_tabtitle_height);
		mIndicateHeight = (int) getResources().getDimension(R.dimen.screen_edit_indicator_height);
		Drawable drawable = getResources().getDrawable(R.drawable.gl_screenedit_bg);
		mBgDrawable = GLDrawable.getDrawable(drawable);
		mBgDrawable.setAlpha(0);
	}

	private void setupView() {
		mEditTitle = (GLScreenEditTitle) findViewById(R.id.gl_screen_edit_title);
		mEditContainer = (GLScreenEditContainer) findViewById(R.id.gl_screen_edit_container);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		MsgMgrProxy.registMsgHandler(this);
		setupView();
	}

	@Override
	public void setVisible(boolean visible, boolean animate, Object obj) {

		mBackWorkspace = (GLBackWorkspace) mIShell.getView(IViewId.BACK_WORKSPACE);
		mEditTitle.setBackWorkspace(mBackWorkspace);

		if (visible) {
			// 隐藏dock栏
			mIShell.hide(IViewId.DOCK, true);

			int tabId = 0;
			Object[] params = null;
			if (obj != null && obj instanceof Object[]) {
				Object[] array = (Object[]) obj;
				tabId = (Integer) array[0];
				if (array.length > 1) {
					params = new Object[array.length - 1];
					for (int i = 1; i < array.length; i++) {
						params[i - 1] = array[i];
					}
				}
			}

			GLBaseTab baseTab = mTabFactory.getTab(tabId);
			if (baseTab == null) {
				return;
			}

			if (baseTab.getTabLevel() == ScreenEditConstants.TAB_LEVEL_2) {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
						IScreenFrameMsgId.SCREEN_CHANGE_TO_SMALL,
						GLWorkspace.SCREEN_TO_SMALL_LEVEL_TWO);
			}

			mCurrentTab = tabId;
			mBaseTab = baseTab;
			mBaseTab.setParam(params);
			mBaseTab.setNeedEnterAnim(animate);

			updateContainerHeight(baseTab.getTabHeight());
			mEditTitle.startShowIn(mBaseTab, animate);
			mEditContainer.setCurrentTab(mBaseTab);
		} else {
			mIShell.show(IViewId.DOCK, true);
			if (animate) {
				if (obj instanceof AnimationListener) {
					AnimationListener listener = (AnimationListener) obj;
					startExitAnimation(listener);
				}
			}
		}
	}

	private void startExitAnimation(final AnimationListener listener) {
		setHasPixelOverlayed(false);
		AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
		alphaAnim.setDuration(ScreenEditConstants.DURATION_EDIT_EXIT);
		alphaAnim.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				listener.onAnimationStart(animation);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				listener.onAnimationRepeat(animation);
			}
			
			@Override
			public void onAnimationProcessing(Animation animation, float interpolatedTime) {
				mBackWorkspace.drawScreenEditBg(false, interpolatedTime);
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				listener.onAnimationEnd(animation);
				mBackWorkspace.needDrawScreenEditBg(false);
			}
		});
		startAnimation(alphaAnim);
	}

	/**
	 * 
	 * @param containerHeight
	 */
	private void updateContainerHeight(int containerHeight) {
		if (mContainerHeight == containerHeight) {
			return;
		}

		Resources resources = mContext.getResources();
		int indicateHeight = (int) resources.getDimension(R.dimen.screen_edit_indicator_height);
		GLRelativeLayout.LayoutParams params = (GLRelativeLayout.LayoutParams) mEditContainer
				.getLayoutParams();
		params.height = indicateHeight + containerHeight;
		mEditContainer.setTabHeight(containerHeight);
		mContainerHeight = containerHeight;
	}

	@Override
	public boolean handleMessage(Object sender, int msgId, int param, Object... objects) {
		switch (msgId) {

			case IScreenEditMsgId.SCREEN_EDIT_CHANGE_TAB : {

				int tabId = param;
				GLBaseTab baseTab = mTabFactory.getTab(tabId);
				if (baseTab == null) {
					return true;
				}

				if (baseTab.getTabLevel() == ScreenEditConstants.TAB_LEVEL_2) {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_CHANGE_TO_SMALL,
							GLWorkspace.SCREEN_TO_SMALL_LEVEL_TWO);
				}

				mCurrentTab = tabId;
				mBaseTab = baseTab;
				mBaseTab.setNeedChangeAnim(true);
				mBaseTab.setParam(objects);
				mEditTitle.startChangeTabAnim(mBaseTab);
				mEditContainer.startChangeTabAnim(this);
			}
				break;

			case IScreenEditMsgId.SCREEN_EDIT_REFRESH_TITLE :
				mEditTitle.refreshTitle(param, objects);
				break;

			case IScreenEditMsgId.SCREEN_EDIT_HANDLE_KEY_BACK :
				handleKeyBack();
				break;

			case IScreenEditMsgId.SCREEN_EDIT_ADD_GOWIDGET_TO_SCREEN :
				mBaseTab.handleMessage(msgId, param, objects);
				break;

			case IScreenEditMsgId.SCREEN_EDIT_DRAW_BACKGROUND :
				if (objects != null && objects.length > 0) {
					float interpolator = (Float) objects[0];
					int alpha = (int) (255 * interpolator);
					Log.d("screenedit", "alpha = " + alpha);
					if (mBgDrawable != null) {
						mBgDrawable.setAlpha(alpha);
						invalidate();
					}
				}
				break;

			case IAppCoreMsgId.EVENT_INSTALL_APP :
			case IAppCoreMsgId.EVENT_INSTALL_PACKAGE :
			case IAppCoreMsgId.EVENT_UNINSTALL_APP :
			case IAppCoreMsgId.EVENT_UNINSTALL_PACKAGE : {

				String pkgName = null;
				if (objects != null && objects.length > 0) {
					pkgName = (String) objects[0];
				}

				if (pkgName == null || pkgName.equals("")) {
					return true;
				}

				int tabId = mBaseTab.getTabId();
				for (Map.Entry<Integer, GLBaseTab> entry : mTabFactory.getTabEntrySet()) {
					GLBaseTab baseTab = entry.getValue();
					if (entry.getKey() == tabId) {
						baseTab.handleAppChanged(msgId, pkgName, true);
					} else {
						baseTab.handleAppChanged(msgId, pkgName, false);
					}
				}
			}
				break;
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			return handleKeyBack();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 
	 * @return
	 */
	private boolean handleKeyBack() {

		//屏蔽返回键的情况
		//1:添加层自身在做动画
		//2:当前Tab正在加载数据
		//3:如果屏幕在滚动
		//4:在做缩放动画
		if (mEditTitle.getAnimation() != null
				|| mEditContainer.getAnimation() != null
				|| mBaseTab.isLoading()
				|| mBaseTab.onKeyBack()
				|| !MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
						IScreenFrameMsgId.SCREEN_IS_SCROLL_FINISHED, -1)
				|| MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
						IScreenFrameMsgId.SCREEN_IS_SCALE_ANIM_FINISHED, -1)) {
			return true;
		}

		//处理某些tab直接退出添加模块的情况，如从功能表侧边栏跳进来
		if (mBaseTab.onBackExit()) {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SCREEN_SHOW_HOME,
					-1);
			return true;
		}

		//如果当前Tab是二级界面
		if (mBaseTab.getTabLevel() == ScreenEditConstants.TAB_LEVEL_2) {
			MsgMgrProxy
					.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_CHANGE_TO_SMALL,
							GLWorkspace.SCREEN_TO_SMALL_LEVEL_ONE);
		}

		if (mBaseTab.getPreTabId() > 0) {
			mCurrentTab = mBaseTab.getPreTabId();
			mBaseTab = mTabFactory.getTab(mCurrentTab);
			mEditTitle.startChangeTabAnim(mBaseTab);
			mEditContainer.startChangeTabAnim(this);
		} else {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SCREEN_SHOW_HOME,
					-1);
		}

		return true;
	}

	@Override
	public void setShell(IShell shell) {
		mIShell = shell;
	}

	@Override
	public int getViewId() {
		return IViewId.SCREEN_EIDT;
	}

	@Override
	@CapturedViewProperty
	public int getMsgHandlerId() {
		return IDiyFrameIds.SCREEN_EDIT;
	}

	@Override
	public void onAdd(GLViewGroup parent) {
	}

	@Override
	public void onRemove() {
		mTabFactory.clearData();
		mEditContainer.selfDestruct();
		ScreenEditController.destroy();
		MsgMgrProxy.unRegistMsgHandler(this);
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		post(new Runnable() {

			@Override
			public void run() {
				
				post(new Runnable() {
					@Override
					public void run() {
						if (mEditTitle.getAnimation() != null) {
							mEditTitle.clearAnimation();
						}
						if (mEditContainer.getAnimation() != null) {
							mEditContainer.clearAnimation();
						}
						mEditContainer.setHasPixelOverlayed(true);

						if (mBaseTab != null) {
							updateContainerHeight(mBaseTab.getTabHeight());
							mEditContainer.setCurrentTab(mBaseTab);
						}
					}
				});
			}
		});
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
	}

	@Override
	public void onAnimationStart(Animation arg0) {
	}

	@Override
	public void onAnimationProcessing(Animation arg0, float arg1) {
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {

		if (mBgDrawable != null) {
			int tabHeight = mBaseTab.getTabHeight();
			int totalHeight = mTitleHeight + tabHeight + mIndicateHeight;
			mBgDrawable.setBounds(0, getHeight() - totalHeight, getWidth(), getHeight());
			mBgDrawable.draw(canvas);
		}
		super.dispatchDraw(canvas);
	}
}
