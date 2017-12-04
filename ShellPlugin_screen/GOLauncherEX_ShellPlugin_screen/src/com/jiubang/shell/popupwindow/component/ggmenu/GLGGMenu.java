package com.jiubang.shell.popupwindow.component.ggmenu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.graphics.ColorGLDrawable;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLRelativeLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLAdapterView;
import com.go.gl.widget.GLAdapterView.OnItemClickListener;
import com.go.gl.widget.GLImageView;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.util.AppUtils;
import com.go.util.graphics.DrawUtils;
import com.go.util.window.WindowControl;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenFrameMsgId;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.activationcode.invite.InviteActivity;
import com.jiubang.ggheart.activationcode.invite.InviteController;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.appfunc.controler.SwitchControler;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingMainActivity;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingQaTutorialActivity;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogLanguageChoice;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenUtils;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.MessageCenterActivity;
import com.jiubang.ggheart.apps.desks.diy.plugin.PluginManagerActivity;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeConstants;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeManageActivity;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeManageView;
import com.jiubang.ggheart.components.appmanager.SimpleAppManagerActivity;
import com.jiubang.ggheart.components.gohandbook.GoHandBookMainActivity;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.data.statistics.IGoLauncherUserBehaviorStatic;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.statistics.realtiemstatistics.RealTimeStatisticsContants;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.MenuBean;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;
import com.jiubang.ggheart.plugin.mediamanagement.inf.AppFuncContentTypes;
import com.jiubang.ggheart.plugin.notification.NotificationControler;
import com.jiubang.ggheart.screen.systemsettings.SystemSettingControler;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.indicator.DesktopIndicator;
import com.jiubang.shell.indicator.IndicatorListener;
import com.jiubang.shell.popupwindow.IPopupWindow;
import com.jiubang.shell.popupwindow.PopupWindowControler;
import com.jiubang.shell.popupwindow.component.GLPopupWindowLayer;
import com.jiubang.shell.popupwindow.component.GLPopupWindowLayer.PopupWindowLayoutParams;
import com.jiubang.shell.popupwindow.component.ggmenu.GGMenuData.TabData;
import com.jiubang.shell.screenedit.TabIndicatorUpdateListner;

/**
 * @author ruxueqin
 * 
 */
public class GLGGMenu extends GLRelativeLayout implements ICleanable,
		IPopupWindow, OnItemClickListener, ScrollerAddViewListener,
		TabIndicatorUpdateListner, IndicatorListener {
	// 背景顶边距，目的是针对顶部有不可绘制限制的9 patch
	private int mBackgroundPaddingTop;

	private GlMenuGridViewsContainer mContainer;

	private DesktopIndicator mIndicator;
	private GLRelativeLayout mIndicatorLayout;

	public static int sTextColor;
	private TabData[] mTabs = null;
	private int mColNum;
	private int mMenulayout;
	private PopupWindowControler mPopupWindowControler;
	private GLPopupWindowLayer mParentLayer;

	private static final int COLOR_DARK = 0xff000000;
	private ColorGLDrawable mDarkDrawable = new ColorGLDrawable(COLOR_DARK);
	
	private MenuBean mMenuBean;

	/**
	 * @param context
	 * @param attrs
	 */
	public GLGGMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mContainer = (GlMenuGridViewsContainer) findViewById(R.id.container);
		mContainer.setIndicatorUpdateListner(this);

		mIndicator = (DesktopIndicator) findViewById(R.id.edit_indicator);
		mIndicator.setDefaultDotsIndicatorImage(
				R.drawable.gl_menu_indicator_cur,
				R.drawable.gl_menu_indicator_other);
		mIndicator.setSliderIndicator(R.drawable.gl_screenedit_indicator,
				R.drawable.gl_screenedit_indicator_bg);
		int sliderIndicatorHeight = (int) getResources().getDimension(
				R.dimen.screenedit_slider_indicator_height);
		mIndicator.setSliderIndicatorHeight(sliderIndicatorHeight);
		mIndicator.setIndicatorListener(this);
		mIndicator.setIsFromAddFrame(true);
		// 不允许指示器空白处的touch事件向下传递
		mIndicatorLayout = (GLRelativeLayout) findViewById(R.id.indicator_layout);
		mIndicatorLayout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(GLView v, MotionEvent event) {
				return true;
			}
		});

		mMenulayout = R.layout.gl_ggmenu_default;
		mColNum = GGMenuData.GGMENU_MAX_COLOUMNS;
		mPopupWindowControler = ShellAdmin.sShellManager.getShell()
				.getPopupWindowControler();
		loadResource();
		initMenuData(0);
		setHasPixelOverlayed(false);
		// onShow();
	}
	
	private void loadResource() {
		Activity activity = GoLauncherActivityProxy.getActivity();
		ThemeManager themeManager = ThemeManager.getInstance(activity);
		String packageName = themeManager.getScreenStyleSettingInfo().getGGmenuStyle();
		MenuBean menuBean = themeManager.getGGmenuBean(packageName);

		int textcolor = menuBean == null ? 0 : menuBean.mTextColor;

		sTextColor = textcolor;

		// Drawable background = GGMenuProvider.getBackgroundImage(menuBean,
		// activity, imageExplorer, packageName);
		// if (null != background) {
		// setBackgroundDrawable(background);
		// }

		// Drawable itemline = GGMenuProvider.getItemBackgroundImage(menuBean,
		// activity, imageExplorer, packageName);
		// if (null != itemline) {
		// mItemBackgroundDrawable = itemline;
		// }

		// Drawable drawableItemLine = GGMenuProvider.getItemLineImage(menuBean,
		// activity, imageExplorer, packageName);
		// if (null != drawableItemLine) {
		// mItemLineDrawable = drawableItemLine;
		// }

	}

	private void initMenuData(int position) {
		mTabs = initScreenMenuData();
		int length = mTabs.length;
		for (int i = 0; i < length; i++) {
			if (i != position) {
				mContainer.addView(getGGMenuGridView(i, false));
			} else {
				GLGGMenuGridView gridView = getGGMenuGridView(position, true);
				mContainer.addView(gridView);
			}
		}
		mContainer.setTabCount(length);
		mContainer.setmGridViews(length);
		mContainer.setAddViewListener(this);
		mContainer.gotoTab(position);

		// 显示指示器
		mIndicator.setCurrent(0);
		mIndicator.setTotal(length);
		if (length > 1) {
			mIndicatorLayout.setVisibility(View.VISIBLE);
		} else {
			mIndicatorLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void addView(int position) {
		
	}

	public GLGGMenuGridView getGGMenuGridView(int position, boolean animate) {
		if (position >= mTabs.length) {
			return null;
		}
		GLLayoutInflater inflater = ShellAdmin.sShellManager
				.getLayoutInflater();
		TabData tab = mTabs[position];
		GLGGMenuGridView gridView = (GLGGMenuGridView) inflater.inflate(
				mMenulayout, null);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setVerticalSpacing(getContext().getResources().getDimensionPixelSize(R.dimen.gl_menu_gridview_vertical_spacing));
		gridView.setAnimate(animate);
		gridView.setOnItemClickListener(this);
		int l = gridView.getPaddingLeft();
		int t = gridView.getPaddingTop();
		int r = gridView.getPaddingRight();
		int b = gridView.getPaddingBottom();
		boolean needPadding = false;
		// if (null != mItemBackgroundDrawable) {
		// gridView.setSelector(null);
		// needPadding = true;
		// }
		if (needPadding) {
			gridView.setPadding(l, t, r, b);
		}
		gridView.setNumColumns(mColNum);
		GLGGMenuAdapter adapter = new GLGGMenuAdapter(mContext,
				tab.getTextids(), tab.getDrawables(), tab.getIds());
		if (!animate) {
			adapter.checkItemState();
		}
		gridView.setAdapter(adapter);

		// 设置分割线
		// String curThemePkg = ThemeManager.getInstance(mContext)
		// .getCurThemePackage();
		// if (!curThemePkg.equals(IGoLauncherClassName.DEFAULT_THEME_PACKAGE_3)
		// && !curThemePkg
		// .equals(IGoLauncherClassName.DEFAULT_THEME_PACKAGE_3_NEWER)
		// && !curThemePkg
		// .equals(IGoLauncherClassName.DEFAULT_THEME_PACKAGE)) {
		// if (null != mItemLineDrawable) {
		// gridView.setDivLineDrawable(GLDrawable
		// .getDrawable(mItemLineDrawable));
		// }
		// }

		return gridView;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int containerHeight = mContainer.getMeasuredHeight()
				+ mIndicatorLayout.getMeasuredHeight();

		int width = getMeasuredWidth();
		int height = mBackgroundPaddingTop + containerHeight;
		setMeasuredDimension(width, height);
		PopupWindowLayoutParams params = new PopupWindowLayoutParams(width,
				height);
		params.x = 0;
		params.y = ShellAdmin.sShellManager.getShell().getContainer()
				.getHeight()
				- height - DrawUtils.getNavBarHeight();
		setLayoutParams(params);
	}

	@Override
	public void setBackgroundDrawable(Drawable d) {
		super.setBackgroundDrawable(d);

		if (null != d) {
			Rect rect = new Rect();
			d.getPadding(rect);
			mBackgroundPaddingTop = rect.top;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN /*
														 * &&
														 * mPopupWindow.isShowing
														 * ()
														 */
				&& event.getY() < 0) {
			// 在菜单之上点击,event.getY() < 0
			// mPopupWindow.dismiss();
		}

		return super.onTouchEvent(event);
	}

	@Override
	public void cleanup() {
		super.cleanup();
		if (mContainer != null) {
			mContainer.cleanup();
			mContainer = null;
		}
		// mItemBackgroundDrawable = null;
		// mItemLineDrawable = null;
		mTabs = null;
		mParentLayer = null;
		if (mDarkDrawable != null) {
			mDarkDrawable.clear();
			mDarkDrawable = null;
		}
		// 清空指示器
		if (mIndicator != null) {
			int count = mIndicator.getChildCount();
			for (int j = 0; j < count; j++) {
				GLViewGroup v1 = (GLViewGroup) mIndicator.getChildAt(j);
				for (int i = 0; i < v1.getChildCount(); i++) {
					GLView v2 = v1.getChildAt(i);
					if (v2 != null && v2 instanceof GLImageView) {
						((GLImageView) v2).setImageDrawable(null);
						v2 = null;
					}
				}
			}
			mIndicator.cleanup();
			mIndicator = null;
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			// bug ADT-2632 Menu弹出状态时，音量键无法调节
			// add by dengdazhong
			// if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode ==
			// KeyEvent.KEYCODE_VOLUME_UP) {
			// dismiss();
			// }
			return false;
		}
		if (keyCode == KeyEvent.KEYCODE_MENU
				|| keyCode == KeyEvent.KEYCODE_ENTER
				|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			mPopupWindowControler.dismiss(true);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			mPopupWindowControler.dismiss(true);
			return true;
		}
		return false;
	}

	/**
	 * 是否保持workspace的缩放情况
	 * 因为显示MENU会让workspace缩小，当点击菜单上关于“屏幕编辑”相关的选项时，会在原来缩小的基础上，再次缩小
	 */
	private boolean mNeedWorkspaceKeepScale = false;

	private int mClickMenuId = -1;

	@Override
	public void onItemClick(final GLAdapterView<?> parent, GLView view,
			final int position, long id) {
		if (position >= 0) {
			mClickMenuId = (int) parent.getAdapter().getItemId(position);
			// 有些菜单项点击之后，需要MENU有退出动画。
			boolean needAnimate = needAnimate(mClickMenuId);
			mPopupWindowControler.dismiss(needAnimate);
		}
	}
	
	private void dealWithItemClick(boolean animate) {
		boolean needKeepScale = needKeepScale(mClickMenuId);
		if (mClickMenuId != -1) {
			StatisticsData.countMenuData(ApplicationProxy.getContext(),
					mClickMenuId);
			doGGMenuOnItemSelected(mClickMenuId);
			mClickMenuId = -1;
		}
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_ON_GGMENU_SHOW, 0, animate,
				needKeepScale);
	}

	/**
	 * 菜单显示处理
	 */
	public void onShow() {
		PreferencesManager sharedPreferences = new PreferencesManager(
				ShellAdmin.sShellManager.getActivity(),
				IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
		// 判断是否要高亮锁定屏幕
		boolean needShowMenu = sharedPreferences.getBoolean(
				IPreferencesIds.SHOULD_SHOW_SCREEN_LOCK_GGMENU, false);
		if (needShowMenu) {
			Drawable drawable = mContext.getResources().getDrawable(
					R.drawable.gl_menuitem_deskunlock_light);
			String name = mContext
					.getString(R.string.menuitem_lockdesktop_unlock);
		}

		StatisticsData.countUserActionData(
				StatisticsData.DESK_ACTION_ID_OPEN_MENU,
				StatisticsData.USER_ACTION_DEFAULT,
				IPreferencesIds.DESK_ACTION_DATA);
	}

	private static final float DELETE_ICON_ALPHA_POW = 3; // 删除动画执行时，淡出的ALPHA动画的变化值次幂数
	private static final int MAX_DRAK_ALPHA = 200; // Dark最大的透明值，过大会导致底部全部看不见
	private static final int ANIMATION_DISTANCE = DrawUtils.dip2px(40); // 动画位置的距离
	
	@Override
	public void onEnter(GLPopupWindowLayer layer, boolean animate) {
		if (animate) {
			AnimationSet animationSet = new AnimationSet(true);
			Animation moveAnimation = new TranslateAnimation(0.0f, 0.0f,
					ANIMATION_DISTANCE, 0.0f) {
				protected void applyTransformation(float interpolatedTime,
						com.go.gl.animation.Transformation3D t) {
					super.applyTransformation(interpolatedTime, t);
					if (mParentLayer != null) {
						mParentLayer
								.getMiddleView()
								.setDarkAlpha(
										(int) ((Math.pow(interpolatedTime,
												DELETE_ICON_ALPHA_POW)) * MAX_DRAK_ALPHA));
					}
				}
			};
			Animation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
			animationSet.addAnimation(moveAnimation);
			animationSet.addAnimation(alphaAnimation);
			animationSet.setDuration(300);
			AnimationListener listener = new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					if (mParentLayer != null) {
						mParentLayer.getMiddleView().setDrawingCacheEnabled(
								true);
						mParentLayer.onEnter();
					}
				}

				@Override
				public void onAnimationProcessing(Animation arg0, float arg1) {
				}
			};
			AnimationTask animationTask = new AnimationTask(this, animationSet,
					listener, true, AnimationTask.PARALLEL);
			GLAnimationManager.startAnimation(animationTask);
		}
	}

	@Override
	public void onExit(GLPopupWindowLayer layer, final boolean animate) {
		if (animate) {
			mParentLayer.getMiddleView().setDrawingCacheEnabled(false);
			AnimationTask task = mContainer.getItemOutAnimationTask();
			if (task != null) {
				task.setBatchAnimationObserver(new BatchAnimationObserver() {

					@Override
					public void onStart(int what, Object[] params) {
						
					}

					@Override
					public void onFinish(int what, Object[] params) {
						popupwindowLayoutAnimationOut(animate);
					}
				}, 0, 0);
				GLAnimationManager.startAnimation(task);
			}
		}
	}

	@Override
	public void onWithEnter(boolean animate) {	
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_ON_GGMENU_SHOW, 1, animate,
				mNeedWorkspaceKeepScale);
	}

	@Override
	public void onWithExit(boolean animate) {
		// animate 为 false,则 onExit将不会执行。
		if (!animate) {
			if (mClickMenuId != -1) {
				StatisticsData.countMenuData(ApplicationProxy.getContext(),
						mClickMenuId);
				doGGMenuOnItemSelected(mClickMenuId);
				mClickMenuId = -1;
			}
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.SCREEN_ON_GGMENU_SHOW, 0, animate,
					mNeedWorkspaceKeepScale);
		}
	}
	
	public void popupwindowLayoutAnimationOut(final boolean animate) {
		final int orientationType = GoLauncherActivityProxy.getOrientation();
		Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
		alphaAnimation.setDuration(animate ? 300 : 0);
		alphaAnimation.setFillAfter(true);
		alphaAnimation.setAnimationListener(new AnimationListenerAdapter() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				super.onAnimationStart(animation);
				// 这里传入设备的横竖屏方向，是因为菜单的GLPopupWindowLayout会对屏幕方向进行锁定，而进入添加界面时会锁定竖屏
				// 假如当前屏幕方向是竖屏，那么点击菜单项的操作可以在GLPopupWindowLayout未退出之前
				// 假如当前屏幕方向是横屏，那么点击菜单项的操作必须在GLPopupWindowLayout退出之后
				if (orientationType == Configuration.ORIENTATION_PORTRAIT) {
					dealWithItemClick(animate);
				}
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				super.onAnimationEnd(animation);
				post(new Runnable() {
					@Override
					public void run() {
						if (mParentLayer != null) {
							// 必须菜单层先退出，才能处理点击事件
							// 否则，横竖屏的切换将会无效
							mParentLayer.onExit();
						}
						if (orientationType == Configuration.ORIENTATION_LANDSCAPE) {
							dealWithItemClick(animate);
						}
					}
				});
			}
		});
		if (mParentLayer != null) {
			mParentLayer.getMiddleView().startAnimation(alphaAnimation);
		}
	}

	public void setParentLayer(GLPopupWindowLayer layer) {
		mParentLayer = layer;
	}

	@Override
	public void updateIndicator(int num, int current) {
		if (num >= 0 && current >= 0 && current < num) {
			mIndicator.setTotal(num);
			mIndicator.setCurrent(current);
		}
	}

	@Override
	public void clickIndicatorItem(int index) {
		if (mContainer != null) {
			mContainer.snapToScreen(index, false, -1);
		}
	}

	@Override
	public void sliding(float percent) {
		if (0 <= percent && percent <= 100) {
			if (mContainer != null) {
				mContainer.getScreenScroller().setScrollPercent(percent);
			}
		}
	}

	@Override
	public void onScrollChanged(int offset) {
		Bundle dataBundle = new Bundle();
		dataBundle.putInt(DesktopIndicator.OFFSET, offset);
		if (mIndicator != null) {
			mIndicator.updateIndicator(
					DesktopIndicator.UPDATE_SLIDER_INDICATOR, dataBundle);
		}
	}

	@Override
	public void onScreenChanged(int newScreen) {
		Bundle dataBundle = new Bundle();
		dataBundle.putInt(DesktopIndicator.CURRENT, newScreen);
		if (mIndicator != null) {
			mIndicator.updateIndicator(DesktopIndicator.UPDATE_DOTS_INDICATOR,
					dataBundle);
		}
	}

	private TabData[] initScreenMenuData() {
		createGGMenuBean();
		// 主题的包名
		String packageName = ThemeManager
				.getInstance(ApplicationProxy.getContext())
				.getScreenStyleSettingInfo().getGGmenuStyle();
		ImageExplorer imageExplorer = ImageExplorer
				.getInstance(ApplicationProxy.getContext());
		// 第一页的菜单
		
		final int[] tabFirstPageIds = new int[] { GGMenuData.GLMENU_ID_ADD,
				GGMenuData.GLMENU_ID_WALLPAPER, GGMenuData.GLMENU_ID_THEME,
				GGMenuData.GLMENU_ID_EFFECT, GGMenuData.GLMENU_ID_SCREENEDIT,
				GGMenuData.GLMENU_ID_PREFERENCE,
				GGMenuData.GLMENU_ID_SYSSETTING, GGMenuData.GLMENU_ID_MESSAGE };
		final int[] tabFirstPageTextIds = new int[] {
				R.string.menuitem_addprogram, R.string.menuitem_wallpaper,
				R.string.menuitem_themesetting, R.string.menuitem_effect,
				R.string.menuitem_screensetting, R.string.menuitem_moresetting,
				R.string.menuitem_setting, R.string.menuitem_msgcenter };
		Drawable[] tabFirstPageImages = GGMenuProvider.getMenuItemImages(
				mMenuBean, tabFirstPageIds, ApplicationProxy.getContext(),
				imageExplorer, packageName);
		TabData tabFirst = new TabData("", tabFirstPageIds,
				tabFirstPageTextIds, tabFirstPageImages);
		// 第二页的菜单

		// 锁屏／解锁 处理逻辑
		int lockId = 0;
		int lockTextid = 0;
		ScreenSettingInfo info = SettingProxy.getScreenSettingInfo();
		if (null != info) {
			if (!info.mLockScreen) {
				lockId = GGMenuData.GLMENU_ID_LOCKEDIT;
				lockTextid = R.string.menuitem_lockdesktop_lock;
			} else {
				lockId = GGMenuData.GLMENU_ID_UNLOCKEDIT;
				lockTextid = R.string.menuitem_lockdesktop_unlock;
			}
		}
		final int[] tabSecondPageIds = new int[] {
				GGMenuData.GLMENU_ID_PLUGIN_MANAGEMENT,
				GGMenuData.GLMENU_ID_GOSTORE, GGMenuData.GLMENU_ID_GOLOCKER,
				GGMenuData.GLMENU_ID_CLEAR_SCREEN, lockId,
				GGMenuData.GLMENU_ID_FEEDBACK, GGMenuData.GLMENU_ID_RESTART,
				GGMenuData.GLMENU_ID_LANGUAGE };
		final int[] tabSecondPageTextIds = new int[] {
				R.string.menuitem_plugin_management, R.string.menuitem_gostore,
				R.string.menuitem_locker, R.string.clear_screen_title,
				lockTextid, R.string.menuitem_feedback,
				InviteController.isShowInviteMenu(mContext) ? R.string.menuitem_invite : R.string.menuitem_lockdesktop_restart,
				R.string.menuitem_language };
		Drawable[] tabSecondPageImages = GGMenuProvider.getMenuItemImages(
				mMenuBean, tabSecondPageIds, ApplicationProxy.getContext(),
				imageExplorer, packageName);
		TabData tabSecond = new TabData("", tabSecondPageIds,
				tabSecondPageTextIds, tabSecondPageImages);

		TabData[] tabs = new TabData[] { tabFirst, tabSecond };
		return tabs;
	}

	/**
	 * <br>
	 * 功能简述:创建菜单的Bean <br>
	 * 功能详细描述:为了快速打开菜单。桌面加载完成后就加载菜单的BEAN。当更换主题后才重新加载 <br>
	 * 注意:
	 */
	public void createGGMenuBean() {
		ThemeManager themeManager = ThemeManager.getInstance(ApplicationProxy
				.getContext());
		String packageName = themeManager.getScreenStyleSettingInfo()
				.getGGmenuStyle();
		mMenuBean = themeManager.getGGmenuBean(packageName);
	}

	private void doGGMenuOnItemSelected(int id) {
		switch (id) {
		case GGMenuData.GLMENU_ID_ADD: // 添加
			if (SettingProxy.getScreenSettingInfo().mLockScreen) {
				LockScreenHandler.showLockScreenNotification(ApplicationProxy
						.getContext());
				return;
			}
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
						IScreenFrameMsgId.SCREEN_ENTER_SCREEN_EDIT_LAYOUT, 1,
						ScreenEditConstants.TAB_ID_MAIN);
			break;

		case GGMenuData.GLMENU_ID_WALLPAPER: // 壁纸
			// gotoWallpaperSelect();
			// 判断当前是否锁屏
			if (SettingProxy.getScreenSettingInfo().mLockScreen) {
				LockScreenHandler.showLockScreenNotification(ApplicationProxy
						.getContext());
				return;
			}
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.SCREEN_ENTER_SCREEN_EDIT_LAYOUT, 1,
					ScreenEditConstants.TAB_ID_WALLPAPER);
			break;

		case GGMenuData.GLMENU_ID_SCREENEDIT: // 屏幕设置
			PreferencesManager sharedPreferencesScreenedit = new PreferencesManager(
					ApplicationProxy.getContext(),
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			sharedPreferencesScreenedit.putBoolean(
					IPreferencesIds.SHOULD_SHOW_PRIVIEW_EDIT, false);
			sharedPreferencesScreenedit.commit();
			// 3D
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.SCREEN_SHOW_PREVIEW, 1, true);
			break;

		case GGMenuData.GLMENU_ID_THEME: // 主题设置
			// MyThemes Process
			NotificationControler control = AppCore.getInstance()
					.getNotificationControler();
			control.updataGoTheme(ThemeConstants.LAUNCHER_FEATURED_THEME_ID, 0);
			PreferencesManager manager = new PreferencesManager(
					ApplicationProxy.getContext(),
					IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
			boolean has = manager
					.getBoolean(IPreferencesIds.HASNEWTHEME, false);
			if (has) {
				manager.putBoolean(IPreferencesIds.HASNEWTHEME, false);
				manager.commit();
				// GuiThemeStatistics.setCurrentEntry(GuiThemeStatistics.ENTRY_MENU_NEW,
				// mActivity);
				GuiThemeStatistics.guiStaticData("",
						GuiThemeStatistics.OPTION_CODE_LOGIN, 1,
						String.valueOf(GuiThemeStatistics.ENTRY_MENU_NEW), "",
						"", "");

			} else {
				GuiThemeStatistics.guiStaticData("",
						GuiThemeStatistics.OPTION_CODE_LOGIN, 1,
						String.valueOf(GuiThemeStatistics.ENTRY_MENU), "", "",
						"");

			}
			Intent mythemesIntent = new Intent();
			mythemesIntent.putExtra("entrance",
					ThemeManageView.LAUNCHER_THEME_VIEW_ID);
			mythemesIntent.setClass(ApplicationProxy.getContext(),
					ThemeManageActivity.class);
			ShellAdmin.sShellManager.getShell().startActivitySafely(
					mythemesIntent);

			break;

		case GGMenuData.GLMENU_ID_NOTIFICATIONBAR: // 通知栏
			try {
				WindowControl.setIsFullScreen(
						GoLauncherActivityProxy.getActivity(), false, true);
				WindowControl.expendNotification(GoLauncherActivityProxy
						.getActivity());
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		// case GGMenuData.GLMENU_ID_FACEBOOK_LIKE_US : // Facebook Like us
		// String facebookUrl = null;
		// if (Machine.getCountry(mActivity).equals("kr")) {
		// facebookUrl = "https://www.facebook.com/GoLauncherExKorea";
		// } else {
		// facebookUrl = "https://www.facebook.com/golauncher";
		// }
		// AppUtils.gotoBrowserInRunTask(mActivity, facebookUrl);
		// PreferencesManager sp = new PreferencesManager(mActivity,
		// IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
		// sp.putBoolean(IPreferencesIds.SHOULD_SHOW_LIKE_US_LIGHT, false);
		// sp.commit();
		// break;

		case GGMenuData.GLMENU_ID_PREFERENCE: // 更多设置
			ShellAdmin.sShellManager.getShell().startActivitySafely(
					new Intent(ApplicationProxy.getContext(),
							DeskSettingMainActivity.class));
			// startActivity(new Intent(mActivity,
			// DeskSettingMainActivity.class), null);
			break;

		case GGMenuData.GLMENU_ID_EFFECT:
			// NOTE:起特效设置 (修改为跳桌面添加模块)
			// startActivity(new Intent(mActivity,
			// GoEffectsSettingActivity.class), null);
			// 判断当前是否锁屏
			if (SettingProxy.getScreenSettingInfo().mLockScreen) {
				LockScreenHandler.showLockScreenNotification(ApplicationProxy
						.getContext());
				return;
			}
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.SCREEN_ENTER_SCREEN_EDIT_LAYOUT, 1,
					ScreenEditConstants.TAB_ID_EFFECTS);
			break;

		case GGMenuData.GLMENU_ID_UPDATE:
			// 点击跳转GOStore详情界面
			AppsDetail.gotoDetailDirectly(ApplicationProxy.getContext(),
					AppsDetail.START_TYPE_APPRECOMMENDED, ApplicationProxy
							.getContext().getPackageName());
			// GoStoreOperatorUtil.gotoStoreDetailDirectly(mActivity,
			// mActivity.getPackageName());
			break;

		case GGMenuData.GLMENU_ID_GOLOCKER:
			// AppUtils.gotoGolocker(mActivity);
			control = AppCore.getInstance().getNotificationControler();
			control.updataGoTheme(ThemeConstants.LOCKER_FEATURED_THEME_ID, 0);
			if (GoAppUtils.isGoLockerExist(ApplicationProxy.getContext())) {
				manager = new PreferencesManager(ApplicationProxy.getContext(),
						IPreferencesIds.FEATUREDTHEME_CONFIG,
						Context.MODE_PRIVATE);
				has = manager.getBoolean(IPreferencesIds.LOCKER_HASNEWTHEME,
						false);
				if (has) {
					manager.putBoolean(IPreferencesIds.LOCKER_HASNEWTHEME,
							false);
					manager.commit();
					GuiThemeStatistics
							.guiStaticData(
									"",
									GuiThemeStatistics.OPTION_CODE_LOGIN,
									1,
									String.valueOf(GuiThemeStatistics.ENTRY_MENU_LOCKER_NEW),
									"", "", "");
				} else {
					GuiThemeStatistics
							.guiStaticData(
									"",
									GuiThemeStatistics.OPTION_CODE_LOGIN,
									1,
									String.valueOf(GuiThemeStatistics.ENTRY_MENU_LOCKER),
									"", "", "");
				}
				mythemesIntent = new Intent();
				mythemesIntent.putExtra("entrance",
						ThemeManageView.LOCKER_THEME_VIEW_ID);
				mythemesIntent.setClass(ApplicationProxy.getContext(),
						ThemeManageActivity.class);
				ShellAdmin.sShellManager.getShell().startActivitySafely(
						mythemesIntent);
			} else {
				manager = new PreferencesManager(ApplicationProxy.getContext(),
						IPreferencesIds.FEATUREDTHEME_CONFIG,
						Context.MODE_PRIVATE);
				manager.putBoolean(IPreferencesIds.LOCKER_HASNEWTHEME, false);
				manager.commit();
				// modify by Ryan 2012.08.29
				CheckApplication
						.downloadAppFromMarketGostoreDetail(
								ApplicationProxy.getContext(),
								PackageName.GO_LOCK_PACKAGE_NAME,
								LauncherEnv.Url.GOLOCKER_IN_MENU_WITH_GOOGLE_REFERRAL_LINK);
			}
			break;

		case GGMenuData.GLMENU_ID_GOWIDGET:
			// 判断当前是否锁屏
			if (SettingProxy.getScreenSettingInfo().mLockScreen) {
				LockScreenHandler.showLockScreenNotification(ApplicationProxy
						.getContext());
				return;
			}
			break;

		case GGMenuData.GLMENU_ID_LANGUAGE:
			// NOTE:语言设置
			PreferencesManager sharedPreferencesLanguage = new PreferencesManager(
					ApplicationProxy.getContext(),
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			sharedPreferencesLanguage.putBoolean(
					IPreferencesIds.SHOULD_SHOW_LANGUAGE_GUIDE, false);
			sharedPreferencesLanguage.commit();
			showInstallLanguageTip(ApplicationProxy.getContext());
			break;

		case GGMenuData.GLMENU_ID_PLUGIN_MANAGEMENT: // 插件管理
		{
			// 增加插件管理响应
			Intent intent = new Intent(ApplicationProxy.getContext(),
					PluginManagerActivity.class);
			ShellAdmin.sShellManager.getShell().startActivitySafely(intent);
			break;
		}

		case GGMenuData.GLMENU_ID_SYSSETTING: // 系统设置
		{
			SystemSettingControler.getInstance(
					GoLauncherActivityProxy.getActivity()).startSystemSetting();
		}
			break;

		case GGMenuData.GLMENU_ID_FEEDBACK: // 意见反馈
		{
			DeskSettingQaTutorialActivity
					.startFeedbackIntent(GoLauncherActivityProxy.getActivity());

		}
			break;

		case GGMenuData.GLMENU_ID_GOSTORE: // Go精品
		{
			// Intent intent = new Intent();
			// intent.setClass(mActivity, GoStore.class);
			// 跳转到GO Store时把菜单项标识带上,用于GO精品入口统计=
			// intent.putExtra(GoStorePublicDefine.APP_ID_KEY,
			// GoStorePublicDefine.GOLAUNCHER_MENU_ID);
			// mActivity.startActivity(intent);
			if (ChannelConfig.getInstance(ApplicationProxy.getContext())
					.isNeedAppCenter()) {
				AppsManagementActivity
						.startAppCenter(
								GoLauncherActivityProxy.getActivity(),
								MainViewGroup.ACCESS_FOR_APPCENTER_THEME,
								false,
								RealTimeStatisticsContants.AppgameEntrance.DESK_MENU_GOSTORE);
			} else {
				AppsManagementActivity
						.startAppCenter(
								GoLauncherActivityProxy.getActivity(),
								MainViewGroup.ACCESS_FOR_APPCENTER_RECOMMEND,
								false,
								RealTimeStatisticsContants.AppgameEntrance.DESK_MENU_GOSTORE);
			}
			// Log.e(null, "colin 跳转go store-------");
			StatisticsData.countStatData(ApplicationProxy.getContext(),
					StatisticsData.ENTRY_KEY_MEUN);
			// GoStoreStatisticsUtil.setCurrentEntry(
			// GoStoreStatisticsUtil.ENTRY_TYPE_MENU, mActivity);
			AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(
					ApplicationProxy.getContext(),
					AppRecommendedStatisticsUtil.ENTRY_TYPE_MEUN_GOSTORE);
		}
			break;

		case GGMenuData.GLMENU_ID_LOCKEDIT: // 锁屏
		{
			// 锁屏
			ScreenUtils.showLockScreenDialog();
		}
			break;

		case GGMenuData.GLMENU_ID_UNLOCKEDIT: {
			// 解锁 处理逻辑
			ScreenSettingInfo info = SettingProxy.getScreenSettingInfo();
			info.mLockScreen = false;
			SettingProxy.updateScreenSettingInfo(info);
			LockScreenHandler.showUnlockScreenNotification(ApplicationProxy
					.getContext());

			// NOTE:updateItem
			// ThemeManager themeManager = ThemeManager.getInstance(mActivity);
			// ImageExplorer imageExplorer =
			// ImageExplorer.getInstance(mActivity);
			// String packageName = GoSettingControler.getInstance(mActivity)
			// .getScreenStyleSettingInfo().getGGmenuStyle();
			// MenuBean menuBean = themeManager.getGGmenuBean(packageName);
			// Drawable drawable = GGMenuProvider.getMenuItemImage(menuBean,
			// GGMenuData.GLMENU_ID_LOCKEDIT, mActivity, imageExplorer,
			// packageName);
			// String name =
			// mActivity.getString(R.string.menuitem_lockdesktop_lock);
			// mGGMenu.updateItem(GGMenuData.GLMENU_ID_UNLOCKEDIT,
			// GGMenuData.GLMENU_ID_LOCKEDIT, drawable, name);
		}
			break;

		case GGMenuData.GLMENU_ID_RESTART: // 重启
			if (InviteController.isShowInviteMenu(mContext)) {
				//启动邀请页面
				ShellAdmin.sShellManager.getShell().startActivitySafely(
						new Intent(ApplicationProxy.getContext(),
								InviteActivity.class));
			} else {
				//重启
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						ICommonMsgId.RESTART_GOLAUNCHER, -1, null, null); 
			}
			break;
		case GGMenuData.GLMENU_ID_MESSAGE: // 消息中心
			ShellAdmin.sShellManager.getShell().startActivitySafely(
					new Intent(ApplicationProxy.getContext(),
							MessageCenterActivity.class));
			break;

		case GGMenuData.GLMENU_ID_GOBACKUP: // go备份
			GoAppUtils.gotoGobackup(ApplicationProxy.getContext());
			break;
		case GGMenuData.GLMENU_ID_GOHDLAUNCHER:
			GoAppUtils.gotoHDLauncher(ApplicationProxy.getContext());
			break;
		case GGMenuData.GLMENU_ID_APPCENTER:
			AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(
					ApplicationProxy.getContext(),
					AppRecommendedStatisticsUtil.ENTRY_TYPE_MENU);
			AppsManagementActivity
					.startAppCenter(
							ApplicationProxy.getContext(),
							MainViewGroup.ACCESS_FOR_LAUNCHER_MENU,
							true,
							RealTimeStatisticsContants.AppgameEntrance.DESK_MENU_GOMARKET);
			break;
		case GGMenuData.GLMENU_ID_ONE_X_GUIDE:
			// 跳转去下载专用版
			if (AppUtils.gotoBrowser(ApplicationProxy.getContext(),
					"http://golauncher.goforandroid.com/?p=1728")) {
				PreferencesManager sharedPreferences = new PreferencesManager(
						ApplicationProxy.getContext(),
						IPreferencesIds.USERTUTORIALCONFIG,
						Context.MODE_PRIVATE);
				sharedPreferences.putBoolean(
						IPreferencesIds.SHOULD_SHOW_ONE_X_GUIDE, false);
				sharedPreferences.commit();
			}
			break;
		case GGMenuData.GLMENU_ID_GOHANDBOOK:
			// 跳转go手册
			ShellAdmin.sShellManager.getShell().startActivitySafely(
					new Intent(ApplicationProxy.getContext(),
							GoHandBookMainActivity.class));
			break;
		case GGMenuData.GLMENU_ID_MEDIA_MANAGEMENT_PLUGIN:
			if (MediaPluginFactory.isMediaPluginExist(ApplicationProxy
					.getContext())) {
				switch (AppFuncContentTypes.sType_for_setting) {
				case AppFuncContentTypes.IMAGE:
					SwitchControler.getInstance(ApplicationProxy.getContext())
							.showMediaManagementImageContent();
					break;
				case AppFuncContentTypes.MUSIC:
					SwitchControler.getInstance(ApplicationProxy.getContext())
							.showMediaManagementMusicContent();
					break;
				case AppFuncContentTypes.VIDEO:
					SwitchControler.getInstance(ApplicationProxy.getContext())
							.showMediaManagementVideoContent();
					break;
				default:
					SwitchControler.getInstance(ApplicationProxy.getContext())
							.showMediaManagementImageContent();
					break;
				}
			} else {
				MediaPluginFactory.showMediaPluginDownloadDialog();
			}
			break;

		// 清理屏幕
		case GGMenuData.GLMENU_ID_CLEAR_SCREEN:
			ShellAdmin.sShellManager.getShell().startActivitySafely(
					new Intent(ApplicationProxy.getContext(),
							SimpleAppManagerActivity.class));
			GuiThemeStatistics
					.goLauncherUserBehaviorStaticDataCache(IGoLauncherUserBehaviorStatic.APP_MANAGER_PORTAL_MENU);
			break;
		// case GGMenuData.GLMENU_ID_WIN_AWARD :
		// Intent webViewIntent = new Intent(mActivity,
		// ThemeWebViewActivity.class);
		// String url =
		// "http://gotest.3g.net.cn/gostore/webcontent/activity/20131015/us/introduction.jsp?"
		// +
		// "imei=" + Statistics.getVirtualIMEI(mActivity) + "&goid=" +
		// StatisticsManager.getGOID(mActivity) +
		// "&channel=" + Statistics.getUid(mActivity) + "&country=us&language="
		// + Machine.getLanguage(mActivity);
		// webViewIntent.putExtra("url", url);
		// // 统计活动入口点击数
		// webViewIntent.putExtra("entry", GuiThemeStatistics.ENTRY_WIN_AWARD);
		// startActivity(webViewIntent, null);
		//
		// break;

		default:
			break;
		}
	}
	
	/**
	 * 用于判断，点击菜单项后，哪些菜单项是需要菜单完成退出动画的
	 * @param menuId
	 * @return
	 */
	private boolean needAnimate(int menuId) {
		switch (menuId) {
		case GGMenuData.GLMENU_ID_ADD:
		case GGMenuData.GLMENU_ID_WALLPAPER:
		case GGMenuData.GLMENU_ID_EFFECT:
		case GGMenuData.GLMENU_ID_LOCKEDIT:
		case GGMenuData.GLMENU_ID_UNLOCKEDIT:
		case GGMenuData.GLMENU_ID_LANGUAGE:
		case GGMenuData.GLMENU_ID_RESTART:
		case -1:
			return true;
		default:
			return false;
		}
	}

	private boolean needKeepScale(int menuId) {
		switch (menuId) {
		case GGMenuData.GLMENU_ID_ADD:
		case GGMenuData.GLMENU_ID_WALLPAPER:
		case GGMenuData.GLMENU_ID_EFFECT:
		case GGMenuData.GLMENU_ID_SCREENEDIT:
			return true;
		default:
			return false;
		}
	}
	
	
	/**
	 * 弹出语言列表
	 */
	public void showInstallLanguageTip(final Context context) {
		DialogLanguageChoice languageChoiceDialog = new DialogLanguageChoice(
				GoLauncherActivityProxy.getActivity());
		languageChoiceDialog.show();
	}

}
