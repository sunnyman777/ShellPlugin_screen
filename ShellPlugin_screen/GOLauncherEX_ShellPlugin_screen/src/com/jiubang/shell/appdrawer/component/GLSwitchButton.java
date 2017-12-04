package com.jiubang.shell.appdrawer.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLImageView;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.theme.GLAppDrawerThemeControler;

/**
 * 切换按钮
 * @author yangguanxiang
 *
 */
public class GLSwitchButton extends GLImageView implements IButton {
	public final static int TYPE_SEARCH = 0;
	public final static int TYPE_SWITCH = 1;

	private int mType = TYPE_SEARCH;

	public GLSwitchButton(Context context) {
		super(context);
		setScaleType(ScaleType.CENTER);
		mType = MediaPluginFactory.isMediaPluginExist(mContext) ? TYPE_SWITCH : TYPE_SEARCH;
	}

	@Override
	public void loadResource() {
		GLAppDrawerThemeControler themeCtrl = GLAppDrawerThemeControler.getInstance(mContext);
		String themePkgName = SettingProxy.getFunAppSetting().getTabHomeBgSetting();
		Drawable pressBgDrawable = getResources().getDrawable(R.drawable.gl_allapp_bg_light);
		Drawable iconDrawable = null;
		switch (mType) {
			case TYPE_SEARCH :
				StateListDrawable stateDrawable = new StateListDrawable();
				stateDrawable.addState(new int[] { android.R.attr.state_pressed }, pressBgDrawable);
				setBackgroundDrawable(stateDrawable);
				iconDrawable = themeCtrl.getGLDrawable(
						themeCtrl.getThemeBean(themePkgName).mSwitchButtonBean.mSearchIcon,
						themePkgName, R.drawable.gl_appdrawer_switch_button_search);
				setImageDrawable(iconDrawable);
				break;
			case TYPE_SWITCH :
				stateDrawable = new StateListDrawable();
				stateDrawable.addState(new int[] { android.R.attr.state_pressed }, pressBgDrawable);
				setBackgroundDrawable(stateDrawable);
				iconDrawable = themeCtrl.getGLDrawable(
						themeCtrl.getThemeBean(themePkgName).mSwitchButtonBean.mAppIcon,
						themePkgName, R.drawable.gl_appdrawer_switch_button_app);
				setImageDrawable(iconDrawable);
				break;
			default :
				break;
		}
	}

	public boolean doClick(GLView btn) {
		switch (mType) {
			case TYPE_SEARCH :
				//				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
				//						IDiyFrameIds.APPFUNC_SEARCH_FRAME, true, null);
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
						ICommonMsgId.SHOW_EXTEND_FUNC_VIEW, 1, IViewId.APP_DRAWER_SEARCH, btn);
				break;
			case TYPE_SWITCH :
				MediaPluginFactory.getSwitchMenuControler().popupAppMenu(null);
				break;
			default :
				break;
		}
		return true;
	}

	@Override
	public boolean doLongClick() {
		return false;
	}

	public void setType(int type) {
		if (mType != type) {
			mType = type;
			loadResource();
		}
	}
}
