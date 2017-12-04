package com.jiubang.shell.popupwindow.component.ggmenu;

import android.content.Context;

import com.go.commomidentify.IGoLauncherClassName;
import com.go.proxy.ApplicationProxy;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingUtils;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.purchase.FunctionPurchaseManager;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;

/**
 * GGMenu控制器
 * @author yejijiong
 *
 */
public class GGMenuControler {

	private static GGMenuControler sInstance;
	public static final int GGMENU_ITEM_STATUS_NONE = -1;
	public static final int GGMENU_ITEM_STATUS_SHOW_HIGHT_LIGHT = 0;
	public static final int GGMENU_ITEM_STATUS_SHOW_COUNT = 1;
	public static final int GGMENU_ITEM_STATUS_SHOW_NEW_LOGO = 2;
	public static final int GGMENU_ITEM_STATUS_UNSHOW_NEW_LOGO = 3;
	
	public GGMenuControler() {
	}
	
	public static GGMenuControler getInstance() {
		if (sInstance == null) {
			sInstance = new GGMenuControler();
		}
		return sInstance;
	}

	public int checkMenuItemStatus(int itemId) {
		if (GGMenuData.GLMENU_ID_MESSAGE == itemId) {
			return GGMENU_ITEM_STATUS_SHOW_COUNT;
		} /*else if (GGMenuData.GLMENU_ID_SCREENEDIT == itemId) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			boolean needDrawInfo = sharedPreferences.getBoolean(
					IPreferencesIds.SHOULD_SHOW_PRIVIEW_EDIT, true);
			if (needDrawInfo) {
				return GGMENU_ITEM_STATUS_SHOW_HIGHT_LIGHT;
			}
		} else if (GGMenuData.GLMENU_ID_SHARE == itemId) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			boolean needDrawInfo = sharedPreferences.getBoolean(IPreferencesIds.SHOULD_SHOW_SHARE,
					true);
			if (needDrawInfo) {
				return GGMENU_ITEM_STATUS_SHOW_HIGHT_LIGHT;
			}
		} else if (GGMenuData.GLMENU_ID_UNLOCKEDIT == itemId) { // add by jiang 第一次屏幕锁定时，进入GGmenu字体高亮
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			boolean needShowMenu = sharedPreferences.getBoolean(
					IPreferencesIds.SHOULD_SHOW_SCREEN_LOCK_GGMENU, false);
			if (needShowMenu) {
				sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_SCREEN_LOCK_GGMENU, false);
				sharedPreferences.commit();
				return GGMENU_ITEM_STATUS_SHOW_HIGHT_LIGHT;
			}
		} else if (GGMenuData.GLMENU_ID_ONE_X_GUIDE == itemId) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			boolean needDrawInfo = sharedPreferences.getBoolean(
					IPreferencesIds.SHOULD_SHOW_ONE_X_GUIDE, true);
			if (needDrawInfo) {
				return GGMENU_ITEM_STATUS_SHOW_HIGHT_LIGHT;
			}
		} else if (GGMenuData.GLMENU_ID_LANGUAGE == itemId) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			boolean needDrawInfo = sharedPreferences.getBoolean(
					IPreferencesIds.SHOULD_SHOW_LANGUAGE_GUIDE, true);
			if (needDrawInfo) {
				return GGMENU_ITEM_STATUS_SHOW_HIGHT_LIGHT;
			}
		} else if (GGMenuData.GLMENU_ID_FACEBOOK_LIKE_US == itemId) {
			// like us
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			boolean needToshowLight = sharedPreferences.getBoolean(
					IPreferencesIds.SHOULD_SHOW_LIKE_US_LIGHT, true);
			if (needToshowLight) {
				return GGMENU_ITEM_STATUS_SHOW_HIGHT_LIGHT;
			}
		}*/ else if (GGMenuData.GLMENU_ID_THEME == itemId || GGMenuData.GLMENU_ID_GOLOCKER == itemId) {
			PreferencesManager sharedPreferences = new PreferencesManager(ApplicationProxy.getContext(),
					IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
			boolean bool = false;
			if (GGMenuData.GLMENU_ID_THEME == itemId) {
				FunctionPurchaseManager purchaseManager = FunctionPurchaseManager
						.getInstance(ApplicationProxy.getApplication());
				boolean hasPay = purchaseManager.isItemCanUse(FunctionPurchaseManager.PURCHASE_ITEM_AD);
				if (hasPay && DeskSettingUtils.isNoAdvert()) {
					// 如果已经是付费用户，则不再显示new标识
					bool = false;
				} else {
					bool = sharedPreferences.getBoolean(IPreferencesIds.HASNEWTHEME, false);
					
					if (!bool) {
						// UI3.0新主题
						ThemeInfoBean bean = null;
						if (GoAppUtils.isAppExist(ApplicationProxy.getContext(), IGoLauncherClassName.DEFAULT_THEME_PACKAGE_3_NEWER)) {
							bean = ThemeManager.getInstance(ApplicationProxy.getContext()).getThemeInfo(IGoLauncherClassName.DEFAULT_THEME_PACKAGE_3_NEWER);
						} else {
							bean = ThemeManager.getInstance(ApplicationProxy.getContext()).getThemeInfo(IGoLauncherClassName.DEFAULT_THEME_PACKAGE_3);
						}
						
						if (bean != null) {
							if (bean.getVerId() < ThemeManager.NEW_UI3_THEME_VERSION) {
								bool = sharedPreferences.getBoolean(IPreferencesIds.TIP_TO_UPDATE_UI3_THEME_HAS_CLICKED, true);
							} else {
								sharedPreferences.putBoolean(IPreferencesIds.TIP_TO_UPDATE_UI3_THEME_HAS_CLICKED, false);
								sharedPreferences.commit();
							}
						}
					}
					// end
				}
			} else if (GGMenuData.GLMENU_ID_GOLOCKER == itemId) {
				bool = sharedPreferences.getBoolean(IPreferencesIds.LOCKER_HASNEWTHEME, false);
			}
			
			if (bool) {
				return GGMENU_ITEM_STATUS_SHOW_NEW_LOGO;
			} else {
				return GGMENU_ITEM_STATUS_UNSHOW_NEW_LOGO;
			}
		} 
		return GGMENU_ITEM_STATUS_NONE;
	}
	
	/**
	 * 获取是否绘制菜单顶部斜线
	 * @return
	 */
	public boolean getIsDrawSlash() {
		String curThemePkg = ThemeManager.getInstance(ApplicationProxy.getContext()).getCurThemePackage();
		if (curThemePkg.equals(IGoLauncherClassName.DEFAULT_THEME_PACKAGE_3)
				|| curThemePkg.equals(IGoLauncherClassName.DEFAULT_THEME_PACKAGE_3_NEWER)
				|| curThemePkg.equals(IGoLauncherClassName.DEFAULT_THEME_PACKAGE)) {
			return true;
		}
		return false;
	}
}
