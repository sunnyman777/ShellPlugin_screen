package com.jiubang.shell.popupwindow.component.ggmenu;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.activationcode.invite.InviteController;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.MenuItemBean;
/**
 * @author 
 * 
 */
public class GGMenuProvider {
	public static Drawable[] getMenuItemImages(DeskThemeBean.MenuBean menuBean, int[] ids,
			Context context, ImageExplorer imageExplorer) {
		int len = ids.length;
		Drawable[] ret = new Drawable[len];
		for (int i = 0; i < len; i++) {
			ret[i] = getMenuItemImage(menuBean, ids[i], context, imageExplorer);
		}
		return ret;
	}

	public static Drawable[] getMenuItemImages(DeskThemeBean.MenuBean menuBean, int[] ids,
			Context context, ImageExplorer imageExplorer, String packageName) {
		int len = ids.length;
		Drawable[] ret = new Drawable[len];
		for (int i = 0; i < len; i++) {
			ret[i] = getMenuItemImage(menuBean, ids[i], context, imageExplorer, packageName);
		}
		return ret;
	}

	public static Drawable getMenuItemImage(DeskThemeBean.MenuBean menuBean, int menuid,
			Context context, ImageExplorer imageExplorer) {
		Drawable ret = null;
		// 从主题获取
		if (null != menuBean && null != menuBean.mItems) {

			int len = menuBean.mItems.size();
			for (int i = 0; i < len; i++) {
				MenuItemBean itemBean = menuBean.mItems.get(i);
				if (null == itemBean) {
					continue;
				}
				if (itemBean.mId == menuid) {
					if (null != itemBean.mImage) {
						String resName = itemBean.mImage.mResName;
						boolean needDrawInfo = false;
						if (menuid == GGMenuData.GLMENU_ID_SCREENEDIT) {
							PreferencesManager sharedPreferences = new PreferencesManager(context,
									IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
							needDrawInfo = sharedPreferences.getBoolean(
									IPreferencesIds.SHOULD_SHOW_PRIVIEW_EDIT, true);
						} else if (GGMenuData.GLMENU_ID_LANGUAGE == menuid) {
							PreferencesManager sharedPreferences_language = new PreferencesManager(
									context, IPreferencesIds.USERTUTORIALCONFIG,
									Context.MODE_PRIVATE);
							needDrawInfo = sharedPreferences_language.getBoolean(
									IPreferencesIds.SHOULD_SHOW_LANGUAGE_GUIDE, true);

						} else if (GGMenuData.GLMENU_ID_ONE_X_GUIDE == menuid) {
							PreferencesManager sharedPre = new PreferencesManager(context,
									IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
							needDrawInfo = sharedPre.getBoolean(
									IPreferencesIds.SHOULD_SHOW_ONE_X_GUIDE, true);
						} 
						if (needDrawInfo) {
							if (itemBean.mHighColorImage != null
									&& itemBean.mHighColorImage.mResName != null) {
								resName = itemBean.mHighColorImage.mResName;
							}
						}

						ret = getDrawable(imageExplorer, resName);
					}
					break;
				}
			}
		}
		// 从主程序获取
		if (null == ret) {
			switch (menuid) {
				case GGMenuData.GLMENU_ID_ADD :
					ret = getDrawable(context, R.drawable.menuitem_add);
					break;

				case GGMenuData.GLMENU_ID_WALLPAPER :
					ret = getDrawable(context, R.drawable.menuitem_wallpaper);
					break;

				case GGMenuData.GLMENU_ID_THEME :
					ret = getDrawable(context, R.drawable.menuitem_theme);
					break;

				// case GGMenuData.GGMENU_ID_RECOMMEND_APP:
				// ret = getDrawable(context, R.drawable.main_menu_recommend);
				// break;

				case GGMenuData.GLMENU_ID_SCREENEDIT :
//					PreferencesManager sharedPreferences = new PreferencesManager(context,
//							IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
//					boolean needDrawInfo = sharedPreferences.getBoolean(IPreferencesIds.SHOULD_SHOW_PRIVIEW_EDIT, true);
//					if (needDrawInfo) {
//						ret = getDrawable(context, R.drawable.menuitem_screenedit_light);
//					} else {
						ret = getDrawable(context, R.drawable.menuitem_screenedit);
//					}
					break;

				case GGMenuData.GLMENU_ID_PREFERENCE :
					ret = getDrawable(context, R.drawable.menuitem_preference);
					break;

				case GGMenuData.GLMENU_ID_EFFECT :
					ret = getDrawable(context, R.drawable.menuitem_effect);
					break;

				case GGMenuData.GLMENU_ID_SYSSETTING :
					ret = getDrawable(context, R.drawable.menuitem_syssetting);
					break;

				case GGMenuData.GLMENU_ID_GOLOCKER :
					ret = getDrawable(context, R.drawable.menuitem_golocker);
					break;

				case GGMenuData.GLMENU_ID_GOWIDGET :
					ret = getDrawable(context, R.drawable.menuitem_gowidget);
					break;

				case GGMenuData.GLMENU_ID_NOTIFICATION :
					ret = getDrawable(context, R.drawable.menuitem_notification);
					break;

				case GGMenuData.GLMENU_ID_LANGUAGE :
//					PreferencesManager sharedPreferences_language = new PreferencesManager(context,
//							IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
//					boolean needDrawInfo_language = sharedPreferences_language.getBoolean(
//							IPreferencesIds.SHOULD_SHOW_LANGUAGE_GUIDE, true);
//					if (needDrawInfo_language) {
//						ret = getDrawable(context, R.drawable.menuitem_language_light);
//					} else {
						ret = getDrawable(context, R.drawable.menuitem_language);
//					}
					break;

				case GGMenuData.GLMENU_ID_UPDATE :
					ret = getDrawable(context, R.drawable.menuitem_update);
					break;

				/*				case GGMenuData.GLMENU_ID_RATE :
									ret = getDrawable(context, R.drawable.menuitem_grade);
									break;*/

				case GGMenuData.GLMENU_ID_NOTIFICATIONBAR :
					ret = getDrawable(context, R.drawable.menuitem_notificationbar);
					break;

				case GGMenuData.GLMENU_ID_FEEDBACK :
					ret = getDrawable(context, R.drawable.menuitem_fb);
					break;

				case GGMenuData.GLMENU_ID_GOSTORE :
					ret = getDrawable(context, R.drawable.menuitem_gostore);
					break;

				case GGMenuData.GLMENU_ID_LOCKEDIT :
					ret = getDrawable(context, R.drawable.menuitem_desklock);
					break;

				case GGMenuData.GLMENU_ID_UNLOCKEDIT :
					ret = getDrawable(context, R.drawable.menuitem_deskunlock);
					break;

				// case GGMenuData.GGMENU_ID_UNLOCK_DESKTOP:
				// ret = getDrawable(context,
				// R.drawable.main_menu_unlockscreen);
				// break;

				// case GGMenuData.GLMENU_ID_RESTART:
				// ret = getDrawable(context, R.drawable.menuitem_restart);
				// break;
				//
				// case GGMenuData.GGMENU_ID_NEW_FOLDER:
				// ret = getDrawable(context, R.drawable.menuitem_newfolder);
				// break;
				//
				// case GGMenuData.GGMENU_ID_APPDRAWER_SETTING:
				// ret = getDrawable(context, R.drawable.menuitem_preference);
				// break;

				// case GGMenuData.GGMENU_ID_APPDRAWER_SEARCH:
				// ret = getDrawable(context,
				// R.drawable.appfunc_search_menu_icon);
				// break;

				// case GGMenuData.GGMENU_ID_APPDRAWER_LOCK:
				// ret = getDrawable(context, R.drawable.menuitem_desklock);
				// break;

				case GGMenuData.GLMENU_ID_MESSAGE :
					ret = getDrawable(context, R.drawable.menuitem_message);
					break;

				case GGMenuData.GLMENU_ID_GOBACKUP :
					ret = getDrawable(context, R.drawable.menuitem_gobackup);
					break;

				case GGMenuData.GLMENU_ID_ONE_X_GUIDE :
//					PreferencesManager sharedPre = new PreferencesManager(context,
//							IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
//					boolean needDraw = sharedPre.getBoolean(
//							IPreferencesIds.SHOULD_SHOW_ONE_X_GUIDE, true);
//					if (needDraw) {
//						ret = getDrawable(context, R.drawable.menuitem_one_x_light);
//					} else {
						ret = getDrawable(context, R.drawable.menuitem_one_x);
//					}
					break;
				case GGMenuData.GLMENU_ID_GOHANDBOOK :
					ret = getDrawable(context, R.drawable.menuitem_gohandbook);
					break;
				case GGMenuData.GLMENU_ID_MEDIA_MANAGEMENT_PLUGIN :
					ret = getDrawable(context, R.drawable.menuitem_media_management_plugin);
					break;
				case GGMenuData.GLMENU_ID_PLUGIN_MANAGEMENT :
					ret = getDrawable(context, R.drawable.menuitem_plugin);
					break;
				default :
					ret = getDrawable(context, R.drawable.menu_null);
					break;
			}
		}
		return ret;
	}

	public static Drawable getMenuItemImage(DeskThemeBean.MenuBean menuBean, int menuid,
			Context context, ImageExplorer imageExplorer, String packageName) {
		Drawable ret = null;
		// 从主题获取
		if (null != menuBean && null != menuBean.mItems) {

			int len = menuBean.mItems.size();
			for (int i = 0; i < len; i++) {
				MenuItemBean itemBean = menuBean.mItems.get(i);
				if (null == itemBean) {
					continue;
				}
				if (itemBean.mId == menuid) {
					if (null != itemBean.mImage) {
						String resName = itemBean.mImage.mResName;
						boolean needDrawInfo = false;
						if (menuid == GGMenuData.GLMENU_ID_SCREENEDIT) {
							PreferencesManager sharedPreferences = new PreferencesManager(context,
									IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
							needDrawInfo = sharedPreferences.getBoolean(
									IPreferencesIds.SHOULD_SHOW_PRIVIEW_EDIT, true);
						} else if (GGMenuData.GLMENU_ID_LANGUAGE == menuid) {
							PreferencesManager sharedPreferences_language = new PreferencesManager(
									context, IPreferencesIds.USERTUTORIALCONFIG,
									Context.MODE_PRIVATE);
							needDrawInfo = sharedPreferences_language.getBoolean(
									IPreferencesIds.SHOULD_SHOW_LANGUAGE_GUIDE, true);

						} else if (GGMenuData.GLMENU_ID_ONE_X_GUIDE == menuid) {
							PreferencesManager sharedPre = new PreferencesManager(context,
									IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
							needDrawInfo = sharedPre.getBoolean(
									IPreferencesIds.SHOULD_SHOW_ONE_X_GUIDE, true);
						} 
						if (needDrawInfo) {
							if (itemBean.mHighColorImage != null
									&& itemBean.mHighColorImage.mResName != null) {
								resName = itemBean.mHighColorImage.mResName;
							}
						}
						ret = getDrawable(imageExplorer, resName, packageName);
						break;
					}
				}
			}
		}
		// 从主程序获取
		if (null == ret) {
			switch (menuid) {
				case GGMenuData.GLMENU_ID_ADD :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_add);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_add_old);
					}
					break;

				case GGMenuData.GLMENU_ID_WALLPAPER :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_wallpaper);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_wallpaper_old);
					}
					break;

				case GGMenuData.GLMENU_ID_THEME :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_theme);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_theme_old);
					}
					break;

				// case GGMenuData.GGMENU_ID_RECOMMEND_APP:
				// ret = getDrawable(context, R.drawable.main_menu_recommend);
				// break;

				case GGMenuData.GLMENU_ID_SCREENEDIT :
					//					PreferencesManager sharedPreferences = new PreferencesManager(context,
					//							IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
					//					boolean needDrawInfo = sharedPreferences.getBoolean(
					//							IPreferencesIds.SHOULD_SHOW_PRIVIEW_EDIT, true);
					//					if (needDrawInfo) {
					//						ret = getDrawable(context, R.drawable.menuitem_screenedit_light);
					//					} else {
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_screenedit);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_screenedit_old);
					}
					//					}
					break;

				case GGMenuData.GLMENU_ID_PREFERENCE :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_preference);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_preference_old);
					}
					break;
				case GGMenuData.GLMENU_ID_EFFECT :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_effect);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_effect_old);
					}
					break;

				case GGMenuData.GLMENU_ID_GOLOCKER :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_golocker);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_golocker_old);
					}
					break;

				case GGMenuData.GLMENU_ID_GOWIDGET :
					ret = getDrawable(context, R.drawable.menuitem_gowidget);
					break;

				case GGMenuData.GLMENU_ID_NOTIFICATION :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_notification);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_notification_old);
					}
					break;

				case GGMenuData.GLMENU_ID_LANGUAGE :
					//					PreferencesManager sharedPreferences_language = new PreferencesManager(context,
					//							IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
					//					boolean needDrawInfo_language = sharedPreferences_language.getBoolean(
					//							IPreferencesIds.SHOULD_SHOW_LANGUAGE_GUIDE, true);
					//					if (needDrawInfo_language) {
					//						ret = getDrawable(context, R.drawable.menuitem_language_light);
					//					} else {
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_language);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_language_old);
					}
					//					}
					break;

				case GGMenuData.GLMENU_ID_SYSSETTING :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_syssetting);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_syssetting_old);
					}
					break;

				case GGMenuData.GLMENU_ID_UPDATE :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_update);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_update_old);
					}
					break;

				/*				case GGMenuData.GLMENU_ID_RATE :
									ret = getDrawable(context, R.drawable.menuitem_grade);
									break;*/

				case GGMenuData.GLMENU_ID_NOTIFICATIONBAR :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_notificationbar);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_notificationbar_old);
					}
					break;

				case GGMenuData.GLMENU_ID_FEEDBACK :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_fb);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_fb_old);
					}
					break;

				case GGMenuData.GLMENU_ID_GOSTORE :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_gostore);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_gostore_old);
					}
					break;

				case GGMenuData.GLMENU_ID_LOCKEDIT :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_desklock);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_desklock_old);
					}
					break;

				case GGMenuData.GLMENU_ID_UNLOCKEDIT :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_deskunlock);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_deskunlock_old);
					}
					break;

				// case GGMenuData.GGMENU_ID_UNLOCK_DESKTOP:
				// ret = getDrawable(context,
				// R.drawable.main_menu_unlockscreen);
				// break;

				case GGMenuData.GLMENU_ID_RESTART :
					if (InviteController.isShowInviteMenu(context)) {
						//显示邀请图标
						ret = getDrawable(context, R.drawable.menuitem_invite_icon);
					} else {
						//显示重启图标
						if (ThemeManager.isInnerTheme(packageName)) {
							ret = getDrawable(context, R.drawable.menuitem_restart);
						} else {
							ret = getDrawable(context, R.drawable.menuitem_restart_old);
						}
					}
					break;

				// case GGMenuData.GGMENU_ID_NEW_FOLDER:
				// ret = getDrawable(context, R.drawable.menuitem_newfolder);
				// break;
				// case GGMenuData.GGMENU_ID_APPDRAWER_SETTING:
				// ret = getDrawable(context, R.drawable.menuitem_preference);
				// break;

				// case GGMenuData.GGMENU_ID_APPDRAWER_SEARCH:
				// ret = getDrawable(context,
				// R.drawable.appfunc_search_menu_icon);
				// break;

				// case GGMenuData.GGMENU_ID_APPDRAWER_LOCK:
				// ret = getDrawable(context, R.drawable.menuitem_desklock);
				// break;
				case GGMenuData.GLMENU_ID_MESSAGE :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_message);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_message_old);
					}
					break;
				case GGMenuData.GLMENU_ID_GOBACKUP :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_gobackup);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_gobackup_old);
					}
					break;
				case GGMenuData.GLMENU_ID_GOHDLAUNCHER :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_pad);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_pad_old);
					}
					break;
				case GGMenuData.GLMENU_ID_APPCENTER :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_appcenter);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_appcenter_old);
					}
					break;
				case GGMenuData.GLMENU_ID_GAMEZONE :
					ret = getDrawable(context, R.drawable.menuitem_gamezone);
					break;
				case GGMenuData.GLMENU_ID_ONE_X_GUIDE :
					//					PreferencesManager sharedPre = new PreferencesManager(context,
					//							IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
					//					boolean needDraw = sharedPre.getBoolean(
					//							IPreferencesIds.SHOULD_SHOW_ONE_X_GUIDE, true);
					//					if (needDraw) {
					//						ret = getDrawable(context, R.drawable.menuitem_one_x_light);
					//					} else {
					ret = getDrawable(context, R.drawable.menuitem_one_x);
					//					}
					break;
				case GGMenuData.GLMENU_ID_GOHANDBOOK :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_gohandbook);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_gohandbook_old);
					}
					break;
				case GGMenuData.GLMENU_ID_MEDIA_MANAGEMENT_PLUGIN :
					ret = getDrawable(context, R.drawable.menuitem_media_management_plugin);
					break;
				case GGMenuData.GLMENU_ID_PLUGIN_MANAGEMENT :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menuitem_plugin);
					} else {
						ret = getDrawable(context, R.drawable.menuitem_plugin_old);
					}
					break;

				case GGMenuData.GLMENU_ID_CLEAR_SCREEN :
					if (ThemeManager.isInnerTheme(packageName)) {
						ret = getDrawable(context, R.drawable.menu_clean_screen);
					} else {
						ret = getDrawable(context, R.drawable.menu_clean_screen_old);
					}
					break;
				case GGMenuData.GLMENU_ID_WIN_AWARD :
					ret = getDrawable(context, R.drawable.menuitem_win_award);
					break;
				default :
					ret = getDrawable(context, R.drawable.menu_null);
					break;
			}
		}
		return ret;
	}

	public static Drawable getBackgroundImage(DeskThemeBean.MenuBean menuBean, Context context,
			ImageExplorer imageExplorer) {
		Drawable background = null;
		if (null != menuBean && null != menuBean.mBackground) {
			background = getDrawable(imageExplorer, menuBean.mBackground.mResName);
		}
		if (null == background) {
			background = getDrawable(context, R.drawable.glmenu_bg);
		}
		return background;
	}

	public static Drawable getBackgroundImage(DeskThemeBean.MenuBean menuBean, Context context,
			ImageExplorer imageExplorer, String packageName) {
		Drawable background = null;
		if (null != menuBean && null != menuBean.mBackground) {
			background = getDrawable(imageExplorer, menuBean.mBackground.mResName, packageName);
		}
		if (null == background) {
			background = getDrawable(context, R.drawable.glmenu_bg);
		}
		return background;
	}

	public static Drawable getItemLineImage(DeskThemeBean.MenuBean menuBean, Context context,
			ImageExplorer imageExplorer, String packageName) {
		Drawable background = null;
		if (null != menuBean && null != menuBean.mItemLineBean) {
			background = getDrawable(imageExplorer, menuBean.mItemLineBean.mResName, packageName);
		}
		if (null == background) {
			background = getDrawable(context, R.drawable.glmenu_item_line);
		}
		return background;
	}

	public static Drawable getNewMessageNotifyImage(DeskThemeBean.MenuBean menuBean,
			Context context, ImageExplorer imageExplorer, String packageName) {
		Drawable background = null;
		if (null != menuBean && null != menuBean.mNewMessageNotify) {
			background = getDrawable(imageExplorer, menuBean.mNewMessageNotify.mResName,
					packageName);
		}
		if (null == background) {
			background = getDrawable(context, R.drawable.glmenu_message_news);
		}
		return background;
	}

	public static Drawable getNewMessageNotifyImage(DeskThemeBean.MenuBean menuBean,
			Context context, ImageExplorer imageExplorer) {
		Drawable background = null;
		if (null != menuBean && null != menuBean.mNewMessageNotify) {
			background = getDrawable(imageExplorer, menuBean.mNewMessageNotify.mResName);
		}
		if (null == background) {
			background = getDrawable(context, R.drawable.glmenu_message_news);
		}
		return background;
	}

	public static Drawable getItemLineImage(DeskThemeBean.MenuBean menuBean, Context context,
			ImageExplorer imageExplorer) {
		Drawable background = null;
		if (null != menuBean && null != menuBean.mItemLineBean) {
			background = getDrawable(imageExplorer, menuBean.mItemLineBean.mResName);
		}
		if (null == background) {
			background = getDrawable(context, R.drawable.glmenu_item_line);
		}
		return background;
	}

	public static Drawable getUnselectTabLineImage(DeskThemeBean.MenuBean menuBean,
			Context context, ImageExplorer imageExplorer, String packageName) {
		Drawable background = null;
		if (null != menuBean && null != menuBean.mUnselectTabLineBean) {
			background = getDrawable(imageExplorer, menuBean.mUnselectTabLineBean.mResName,
					packageName);
		}
		if (null == background) {
			background = getDrawable(context, R.drawable.glmenu_unselect_topline);
		}
		return background;
	}

	public static Drawable getUnselectTabLineImage(DeskThemeBean.MenuBean menuBean,
			Context context, ImageExplorer imageExplorer) {
		Drawable background = null;
		if (null != menuBean && null != menuBean.mUnselectTabLineBean) {
			background = getDrawable(imageExplorer, menuBean.mUnselectTabLineBean.mResName);
		}
		if (null == background) {
			background = getDrawable(context, R.drawable.glmenu_unselect_topline);
		}
		return background;
	}

	public static Drawable getSelectTabLineImage(DeskThemeBean.MenuBean menuBean, Context context,
			ImageExplorer imageExplorer, String packageName) {
		Drawable background = null;
		if (null != menuBean && null != menuBean.mSelectTabLineBean) {
			background = getDrawable(imageExplorer, menuBean.mSelectTabLineBean.mResName,
					packageName);
		}
		if (null == background) {
			background = getDrawable(context, R.drawable.glmenu_select_topline);
		}
		return background;
	}

	public static Drawable getSelectTabLineImage(DeskThemeBean.MenuBean menuBean, Context context,
			ImageExplorer imageExplorer) {
		Drawable background = null;
		if (null != menuBean && null != menuBean.mSelectTabLineBean) {
			background = getDrawable(imageExplorer, menuBean.mSelectTabLineBean.mResName);
		}
		if (null == background) {
			background = getDrawable(context, R.drawable.glmenu_select_topline);
		}
		return background;
	}

	public static Drawable getItemBackgroundImage(DeskThemeBean.MenuBean menuBean, Context context,
			ImageExplorer imageExplorer) {
		Drawable itembackground = null;
		if (null != menuBean && null != menuBean.mItemBackground) {
			itembackground = getDrawable(imageExplorer, menuBean.mItemBackground.mResName);
		}
		if (null == itembackground) {
			itembackground = getDrawable(context, R.drawable.menu_item_background);
		}
		return itembackground;
	}

	public static Drawable getItemBackgroundImage(DeskThemeBean.MenuBean menuBean, Context context,
			ImageExplorer imageExplorer, String packageName) {
		Drawable itembackground = null;
		if (null != menuBean && null != menuBean.mItemBackground) {
			itembackground = getDrawable(imageExplorer, menuBean.mItemBackground.mResName,
					packageName);
		}
		if (null == itembackground) {
			itembackground = getDrawable(context, R.drawable.menu_item_background);
		}
		return itembackground;
	}

	private static Drawable getDrawable(ImageExplorer imageExplorer, String resName,
			String packageName) {
		Drawable ret = null;
		if (null == imageExplorer || null == resName) {
			return ret;
		}
		try {
			ret = imageExplorer.getDrawable(packageName, resName);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	private static Drawable getDrawable(ImageExplorer imageExplorer, String resName) {
		Drawable ret = null;
		if (null == imageExplorer || null == resName) {
			return ret;
		}
		try {
			ret = imageExplorer.getDrawable(resName);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	private static Drawable getDrawable(Context context, int resId) {
		Drawable ret = null;
		if (null == context) {
			return ret;
		}
		try {
			ret = context.getResources().getDrawable(resId);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
}
