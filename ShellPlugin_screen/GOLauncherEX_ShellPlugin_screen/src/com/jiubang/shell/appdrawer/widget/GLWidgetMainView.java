package com.jiubang.shell.appdrawer.widget;

import android.content.Context;
import android.view.KeyEvent;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.view.GLViewGroup;
import com.go.proxy.MsgMgrProxy;
import com.go.util.BroadCaster.BroadCasterObserver;
import com.go.util.graphics.DrawUtils;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.components.sidemenuadvert.SideAdvertControl;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.appdrawer.component.GLAbsExtendFuncView;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * widget管理层
 * 
 * @author wuziyi
 * 
 */
public class GLWidgetMainView extends GLAbsExtendFuncView implements
		OnClickListener, IMessageHandler, BroadCasterObserver {

	private GLWidgetContainer mGridViewGroup;
	private ShellTextViewWrapper mTxtTitle;
	private boolean mNoAnimationEnter;
	public GLWidgetMainView(Context context) {
		super(context);
		inflateWidgetView();
		setHasPixelOverlayed(false);
		setStatusBarPadding();
	}

	private void inflateWidgetView() {
		GLLayoutInflater glLayoutInflater = ShellAdmin.sShellManager
				.getLayoutInflater();
//		if (GoLauncherActivityProxy.isPortait()) {
//			glLayoutInflater.inflate(R.layout.hide_app_frame_layout_port, this);
//		} else {
			glLayoutInflater.inflate(R.layout.gl_slide_menu_widget, this);
			mTxtTitle = (ShellTextViewWrapper) findViewById(R.id.title);
			mTxtTitle.showTextShadow();
//		}
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int offset = DrawUtils.dip2px(6);
		mTxtTitle.layout(mTxtTitle.getLeft(), mTxtTitle.getTop() + offset, mTxtTitle.getRight(),
				mTxtTitle.getBottom() + offset);
		if (mNoAnimationEnter) {
			mGridViewGroup.startLoadIcon();
			mNoAnimationEnter = false;
		}
	}

	private void initView() {
		mGridViewGroup = (GLWidgetContainer) findViewById(R.id.widget_viewgroup);
		mGridViewGroup.setGridView();
	}

	@Override
	public void onAdd(GLViewGroup parent) {
		initView();
		mGridViewGroup.refreshGridView();
		MsgMgrProxy.registMsgHandler(this);
		SideAdvertControl.getAdvertControlInstance(mContext).registerObserver(this);
		super.onAdd(parent);
	}
	
	@Override
	public void onRemove() {
		MsgMgrProxy.unRegistMsgHandler(this);
		SideAdvertControl.getAdvertControlInstance(mContext).unRegisterObserver(this);
		GLWidgetImageManager.destory();
		super.onRemove();
	}

	@Override
	public boolean handleMessage(Object sender, int msgId, int param,
			Object... objects) {
		switch (msgId) {
//			case IScreenFrameMsgId.COMMON_EVENT_UNINSTALL_APP:
//			case IScreenFrameMsgId.COMMON_EVENT_UNINSTALL_PACKAGE:
//				mGridViewGroup.refreshGridView();
			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED:
//				removeAllViews();
//				inflateWidgetView();
//				onAdd(this);
				break;
			case ICommonMsgId.COMMON_ON_HOME_ACTION:
				// 这个是3D插件的home键消息
				removeWidgetView();
				break;
			case IFrameworkMsgId.SYSTEM_ON_RESUME:
//				mGridViewGroup.refreshGridView();
//				setupLockPic();
				break;
			default:
			break;
		}
		return false;
	}

	private void removeWidgetView() {
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
				ICommonMsgId.SHOW_EXTEND_FUNC_VIEW, 0, IViewId.WIDGET_MANAGE);
	}

	@Override
	public void onClick(GLView v) {
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return mGridViewGroup.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return mGridViewGroup.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		return mGridViewGroup.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		return mGridViewGroup.onKeyMultiple(keyCode, repeatCount, event);
	}

	@Override
	public int getViewId() {
		return IViewId.WIDGET_MANAGE;
	}

	@Override
	public int getMsgHandlerId() {
		return IDiyFrameIds.APP_DRAWER_WIDGET_MANAGE;
	}

	@Override
	public void onBCChange(int msgId, int param, Object ...object) {
		switch (msgId) {
			case SideAdvertControl.APP_CHANGE:
				String pkg = (String) object[0];
				mGridViewGroup.reloadIconPreView(pkg);
				mGridViewGroup.refreshGridView();
				break;
	
			default:
				break;
		}
		
	}
	
	@Override
	public void setVisible(boolean visible, boolean animate, Object obj) {
		super.setVisible(visible, animate, obj);
		if (visible) {
			if (!animate) {
				mNoAnimationEnter = true;
			}
		}
	}
	
	@Override
	public void onAnimationEnd(Animation animation) {
		super.onAnimationEnd(animation);
		if (animation == mInAnimation) {
			mGridViewGroup.startLoadIcon();
		}
	}
}
