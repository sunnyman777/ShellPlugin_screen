package com.jiubang.shell.appdrawer.slidemenu.slot;

import com.go.gl.view.GLView;
import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;

/**
 * 侧边栏功能块
 * @author wuziyi
 *
 */
public abstract class AbsSlideMenuSlot implements ISlideMenuViewSlot {

	@Override
	public void showExtendFunctionView(GLView view, boolean needAnimation, Object...objs) {
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
				ICommonMsgId.SHOW_EXTEND_FUNC_VIEW, 1, getViewId(), view, needAnimation);
	}
	
	@Override
	public void hideExtendFunctionView(GLView view, boolean needAnimation, Object...objs) {
		// TODO Auto-generated method stub
		
	}

}
