package com.jiubang.shell.appdrawer.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.view.GLRelativeLayout;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLImageView;
import com.go.proxy.GoLauncherLogicProxy;
import com.go.proxy.SettingProxy;
import com.go.util.graphics.DrawUtils;
import com.go.util.log.Loger;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.theme.GLAppDrawerThemeControler;
/**
 * 功能表组合view组件
 * @author zhangxi
 *
 */
public class GLAppDrawerComItem extends GLRelativeLayout {
	
	//for dip
	private final static int SIDEBAR_THREEICON_PADDING = R.dimen.appdrawer_sidebar_icon_padding;
	private final static int SIDEBAR_THREEICON_MAX_WIDTH = R.dimen.appdrawer_sidebar_icon_maxwidth;
	private static final int FULL_ALPHLA = 255;

	/**
	 * 侧边栏图标默认及选中
	 */
	protected Drawable mSidebarNor;
	/**
	 * 图标文字颜色
	 */
	protected int mTtitleColor;
	/**
	 * 工具类引用
	 */
	protected AppFuncUtils mUtils;
	/**
	 * 主题控制器
	 */
	protected GLAppDrawerThemeControler mThemeCtrl;
	/**
	 * 文字尺寸
	 */
	protected float mTextSize;
	
	protected int mSideIconPadding;  //icon最小宽
	protected int mSideIconMaxWight;	//icon最大宽
	
	/**
	 * 控件规格，以Hight Density为标准
	 */
	protected static final int SIDE_TEXT_SIZE_ID = R.dimen.appdrawer_sidebar_text_size;

	private GLImageView mSidebarIcon;
//	private GLImageView mLogoIcon;
	private ShellTextViewWrapper mTitleText;
	private ShellTextViewWrapper mExchangeTitleText;
	
//	private float mSideBarIconpersent = 1.0f;
	
	public GLAppDrawerComItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public GLAppDrawerComItem(Context context) {
		super(context);
		mUtils = AppFuncUtils.getInstance(ShellAdmin.sShellManager.getActivity());
		mThemeCtrl = GLAppDrawerThemeControler.getInstance(mContext);
		mSideIconPadding = mContext.getResources().getDimensionPixelSize(SIDEBAR_THREEICON_PADDING);
		mSideIconMaxWight = mContext.getResources().getDimensionPixelSize(SIDEBAR_THREEICON_MAX_WIDTH);
		initView();
		loadResource();
	}
	
	private void initView() {
		mSidebarIcon = new GLImageView(mContext);
		LayoutParams sidebarLayout = new LayoutParams(mSideIconMaxWight, LayoutParams.WRAP_CONTENT);
		sidebarLayout.setMargins(mSideIconPadding, 0, mSideIconPadding, 0);
		sidebarLayout.addRule(GLRelativeLayout.CENTER_VERTICAL, GLRelativeLayout.TRUE);
		mSidebarIcon.setId(1);
		
		mTitleText = new ShellTextViewWrapper(mContext);
		mTitleText.getTextView().setTypeface(GoLauncherLogicProxy.getAppTypeface(), GoLauncherLogicProxy.getAppTypefaceStyle());
		mTitleText.setText(mContext.getString(R.string.sidebar_title));
		mTextSize = mContext.getResources().getDimensionPixelSize(SIDE_TEXT_SIZE_ID);
		mTitleText.setTextSize(DrawUtils.px2sp(mTextSize));
		LayoutParams textLayout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		textLayout.addRule(RIGHT_OF, 1);
		textLayout.addRule(GLRelativeLayout.CENTER_VERTICAL, GLRelativeLayout.TRUE);
		
		mExchangeTitleText = new ShellTextViewWrapper(mContext);
		mExchangeTitleText.getTextView().setTypeface(GoLauncherLogicProxy.getAppTypeface(), GoLauncherLogicProxy.getAppTypefaceStyle());
		mExchangeTitleText.setText(mContext.getString(R.string.app_name));
		mExchangeTitleText.setTextSize(DrawUtils.px2sp(mTextSize));
		mExchangeTitleText.setVisibility(GLView.INVISIBLE);
		LayoutParams exchangeLayout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		exchangeLayout.addRule(RIGHT_OF, 1);
		exchangeLayout.addRule(GLRelativeLayout.CENTER_VERTICAL, GLRelativeLayout.TRUE);

		
		addView(mSidebarIcon, sidebarLayout);
		addView(mTitleText, textLayout);
		addView(mExchangeTitleText, exchangeLayout);
		
//		//横屏初始化
//		if (!GoLauncherActivityProxy.isPortait()) {
//			mTitleText.setVisibility(GLView.GONE);
//		}
	}

	/**
	 * 获取公共图片资源
	 */
	public void loadResource() {
		String themePkgName = SettingProxy.getFunAppSetting()
				.getTabHomeBgSetting();
		try {
			mSidebarNor = mThemeCtrl.getGLDrawable(
					mThemeCtrl.getThemeBean(themePkgName).mSidebarBean.mSidebarIcon, themePkgName,
					R.drawable.gl_appdrawer_sidebar);
			mSidebarIcon.setBackgroundDrawable(mSidebarNor);

			mTtitleColor = mThemeCtrl.getThemeBean(themePkgName).mSidebarBean.mTitleColor;
			mTitleText.setTextColor(mTtitleColor);
			mExchangeTitleText.setTextColor(mTtitleColor);

		} catch (Exception e) {
			Loger.i("AppFuncTabSingleTitle.getTabImages()", "err theme exception!");
		}
	}
	
	public void setSidebarShowPersent(float persent) {
		if (persent < 0.5f) {
			if (mExchangeTitleText.isVisible()) {
				mExchangeTitleText.setVisibility(GLView.INVISIBLE);
				mTitleText.setVisibility(GLView.VISIBLE);
			}
			float remapPersent = InterpolatorFactory.remapTime(0, 0.5f, persent);
			int invisitableAlpha = (int) (FULL_ALPHLA * (1 - remapPersent));
			mTitleText.setAlpha(invisitableAlpha);
		} else {
			if (!mExchangeTitleText.isVisible()) {
				mExchangeTitleText.setVisibility(GLView.VISIBLE);
				mTitleText.setVisibility(GLView.INVISIBLE);
			}
			float remapPersent = InterpolatorFactory.remapTime(0.5f, 1.0f, persent);
			int visitableAlpha = (int) (FULL_ALPHLA * remapPersent);
			mExchangeTitleText.setAlpha(visitableAlpha);
		}
	}
	
	public void setTextInVisible(int visibility)
	{
		mTitleText.setVisibility(visibility);
	}
	
	public int getTextVisibility()
	{
		return mTitleText.getVisibility();
	}
}
