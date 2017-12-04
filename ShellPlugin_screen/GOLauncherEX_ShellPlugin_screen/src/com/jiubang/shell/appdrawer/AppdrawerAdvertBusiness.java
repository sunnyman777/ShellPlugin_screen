package com.jiubang.shell.appdrawer;

import com.golauncher.message.IDiyFrameIds;
import com.jiubang.core.message.IMessageHandler;

/**
 * 功能表相关广告模块
 * @author liuheng
 *
 */
public class AppdrawerAdvertBusiness implements IMessageHandler {

	@Override
	public int getMsgHandlerId() {
		return IDiyFrameIds.APPDRAWER_ADVERT_BUSINESS;
	}

	@Override
	public boolean handleMessage(Object arg0, int arg1, int arg2,
			Object... arg3) {
		return false;
	}

}
