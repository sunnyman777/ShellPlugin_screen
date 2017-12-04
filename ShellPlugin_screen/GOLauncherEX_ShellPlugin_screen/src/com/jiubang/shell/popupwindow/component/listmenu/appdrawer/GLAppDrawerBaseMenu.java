package com.jiubang.shell.popupwindow.component.listmenu.appdrawer;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLAdapterView;
import com.go.gl.widget.GLAdapterView.OnItemClickListener;
import com.go.proxy.SettingProxy;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.data.info.AppSettingDefault;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.popupwindow.IPopupWindow;
import com.jiubang.shell.popupwindow.PopupWindowControler;
import com.jiubang.shell.popupwindow.component.GLPopupWindowLayer;
import com.jiubang.shell.popupwindow.component.GLPopupWindowLayer.PopupWindowLayoutParams;
import com.jiubang.shell.popupwindow.component.listmenu.GLBaseListMenu;

/**
 * 功能表弹出基类菜单
 * @author yejijiong
 *
 */
public class GLAppDrawerBaseMenu extends GLViewGroup implements OnItemClickListener, IPopupWindow {
	protected GLBaseListMenu mListMenu;
	protected Activity mActivity;
	protected int mTopBarSize;
	protected int mBottomBarSize;
	private PopupWindowControler mPopupWindowControler;
	public GLAppDrawerBaseMenu() {
		super(ShellAdmin.sShellManager.getContext());
		setClipChildren(true);
		mActivity = ShellAdmin.sShellManager.getActivity();
		mListMenu = new GLBaseListMenu();
		addView(mListMenu, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mListMenu.setOnItemClickListener(this);
		mBottomBarSize = mContext.getResources().getDimensionPixelSize(
				R.dimen.appdrawer_bottom_bar_container_height);
		mTopBarSize = mContext.getResources().getDimensionPixelSize(
				R.dimen.appdrawer_top_bar_container_height);
		mPopupWindowControler = ShellAdmin.sShellManager.getShell().getPopupWindowControler();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		mListMenu.layout(0, 0, mWidth, mHeight);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		FunAppSetting appSetting = SettingProxy.getFunAppSetting();
		int width = 0;
		int statusBarHeight = StatusBarHandler.isHide() ? 0 : StatusBarHandler.getStatusbarHeight();
		width = DrawUtils.dip2px(208);
		int listMeasureWidth = mListMenu.getMeasuredWidth();
		int listMeasureHeight = mListMenu.getMeasuredHeight();
		if (listMeasureWidth == 0 || listMeasureHeight == 0) {
			int listWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
			int listHeightSpec = MeasureSpec.makeMeasureSpec(
					MeasureSpec.getSize(heightMeasureSpec)
							- statusBarHeight
							- (appSetting.getShowTabRow() == AppSettingDefault.SHOW_TAB_ROW
									? mTopBarSize
									: 0), MeasureSpec.AT_MOST);
			mListMenu.measure(listWidthSpec, listHeightSpec);
			listMeasureWidth = mListMenu.getMeasuredWidth();
			listMeasureHeight = mListMenu.getMeasuredHeight();
		}
		//		setMeasuredDimension(MeasureSpec.makeMeasureSpec(listMeasureWidth, MeasureSpec.EXACTLY),
		//				MeasureSpec.makeMeasureSpec(listMeasureHeight, MeasureSpec.EXACTLY));
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		PopupWindowLayoutParams params = new PopupWindowLayoutParams(listMeasureWidth,
				listMeasureHeight);
		params.x = ShellAdmin.sShellManager.getShell().getContainer().getWidth() - listMeasureWidth;
		int paddingTop = StatusBarHandler.isHide() ? 0 : StatusBarHandler.getStatusbarHeight()
				- DrawUtils.dip2px(6);

		if (appSetting.getShowTabRow() == AppSettingDefault.SHOW_TAB_ROW) {
			params.y = mTopBarSize + paddingTop;
		} else {
			params.y = paddingTop;
		}
		setLayoutParams(params);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mListMenu.onTouchEvent(event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return mListMenu.onKeyUp(keyCode, event);
	}

	@Override
	public void onItemClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		mPopupWindowControler.dismiss(false);
	}

	@Override
	public void onEnter(final GLPopupWindowLayer layer, boolean animate) {
		if (animate) {
			mListMenu.doShowAnimation(layer, true);
		}
	}

	@Override
	public void onExit(GLPopupWindowLayer layer, boolean animate) {
		if (animate) {
			mListMenu.doHideAnimation(layer, true);
		}
	}

	@Override
	public void onWithEnter(boolean animate) {

	}

	@Override
	public void onWithExit(boolean animate) {

	}

	@Override
	public void cleanup() {
		super.cleanup();
		mListMenu.cleanup();
	}
}
