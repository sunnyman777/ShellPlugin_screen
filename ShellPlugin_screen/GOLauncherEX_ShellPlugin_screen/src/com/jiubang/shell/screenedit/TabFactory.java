package com.jiubang.shell.screenedit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;

import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.shell.screenedit.tabs.GLAddAppTab;
import com.jiubang.shell.screenedit.tabs.GLAddFolderTab;
import com.jiubang.shell.screenedit.tabs.GLBaseTab;
import com.jiubang.shell.screenedit.tabs.GLEffectTab;
import com.jiubang.shell.screenedit.tabs.GLGoDynWallpaperTab;
import com.jiubang.shell.screenedit.tabs.GLGoShortCutTab;
import com.jiubang.shell.screenedit.tabs.GLGoWallpaperTab;
import com.jiubang.shell.screenedit.tabs.GLGoWidgetSubTab;
import com.jiubang.shell.screenedit.tabs.GLGoWidgetTab;
import com.jiubang.shell.screenedit.tabs.GLMainTab;
import com.jiubang.shell.screenedit.tabs.GLSysWidgetSubTab;
import com.jiubang.shell.screenedit.tabs.GLSysWidgetTab;
import com.jiubang.shell.screenedit.tabs.GLWallpaperTab;

/**
 * 
 * <br>类描述:tab的数据管理类
 * <br>功能详细描述:创建和获取、销毁各类tab
*/
public class TabFactory {

	private HashMap<Integer, GLBaseTab> mTabs;
	private Context mContext;

	private static TabFactory sInstance;

	public static TabFactory getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new TabFactory(context);
		}
		return sInstance;
	}

	private TabFactory(Context context) {
		mContext = context;
		mTabs = new HashMap<Integer, GLBaseTab>();
	}

	public Set<Entry<Integer, GLBaseTab>> getTabEntrySet() {
		return mTabs.entrySet();
	}

	/**
	 * 每次换Tab的时候，都通过这个方法获取目标Tab
	 * 
	 * @param tabId
	 * @return
	 */
	public GLBaseTab getTab(int tabId) {
		GLBaseTab tab = mTabs.get(tabId);
		if (tab == null) {
			try {
				tab = produceTab(tabId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return tab;
	}

	/**
	 * 根据名称生成Tab
	 * 
	 * @param tag
	 * @return
	 */
	private GLBaseTab produceTab(int tabId) {
		GLBaseTab tab = mTabs.get(tabId);
		if (tab != null) {
			return tab;
		}

		if (tabId == ScreenEditConstants.TAB_ID_MAIN) {
			tab = new GLMainTab(mContext, tabId, ScreenEditConstants.TAB_LEVEL_1);
		} else if (tabId == ScreenEditConstants.TAB_ID_APPS) {
			tab = new GLAddAppTab(mContext, tabId, ScreenEditConstants.TAB_LEVEL_2);
		} else if (tabId == ScreenEditConstants.TAB_ID_FOLDER) {
			tab = new GLAddFolderTab(mContext, tabId, ScreenEditConstants.TAB_LEVEL_2);
		} else if (tabId == ScreenEditConstants.TAB_ID_GOWIDGET) {
			tab = new GLGoWidgetTab(mContext, tabId, ScreenEditConstants.TAB_LEVEL_2);
		} else if (tabId == ScreenEditConstants.TAB_ID_SUB_GOWIDGET) {
			tab = new GLGoWidgetSubTab(mContext, tabId, ScreenEditConstants.TAB_LEVEL_3);
		} else if (tabId == ScreenEditConstants.TAB_ID_SYSTEMWIDGET) {
			tab = new GLSysWidgetTab(mContext, tabId, ScreenEditConstants.TAB_LEVEL_2);
		} else if (tabId == ScreenEditConstants.TAB_ID_SUB_SYSTEMWIDGET) {
			tab = new GLSysWidgetSubTab(mContext, tabId, ScreenEditConstants.TAB_LEVEL_3);
		} else if (tabId == ScreenEditConstants.TAB_ID_GOSHORTCUT) {
			tab = new GLGoShortCutTab(mContext, tabId, ScreenEditConstants.TAB_LEVEL_2);
		} else if (tabId == ScreenEditConstants.TAB_ID_WALLPAPER) {
			tab = new GLWallpaperTab(mContext, tabId, ScreenEditConstants.TAB_LEVEL_1);
		} else if (tabId == ScreenEditConstants.TAB_ID_GOWALLPAPER) {
			tab = new GLGoWallpaperTab(mContext, tabId, ScreenEditConstants.TAB_LEVEL_1);
		} else if (tabId == ScreenEditConstants.TAB_ID_GODYNAMICA_WALLPAPER) {
			tab = new GLGoDynWallpaperTab(mContext, tabId, ScreenEditConstants.TAB_LEVEL_1);
		} else if (tabId == ScreenEditConstants.TAB_ID_EFFECTS) {
			tab = new GLEffectTab(mContext, tabId, ScreenEditConstants.TAB_LEVEL_1);
		}

		mTabs.put(tabId, tab);
		return tab;
	}

	/**
	 * 清空指定tab的数据
	 * @param tag
	 */
	public void removeData(int tag) {
		GLBaseTab tab = mTabs.get(tag);
		if (tab != null) {
			mTabs.remove("" + tag);
		}
	}

	public void clearData() {
		Iterator<Entry<Integer, GLBaseTab>> it = mTabs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, GLBaseTab> entry = it.next();
			GLBaseTab tab = (GLBaseTab) entry.getValue();
			if (tab != null) {
				tab.clear();
				tab = null;
			}
		}
		mTabs.clear();
		mContext = null;
		sInstance = null;
	}

	/**
	 * 获取指定Tab的级别
	 * 
	 * @param tabId
	 * @return
	 */
	public int getTabLevel(int tabId) {
		if (mTabs != null) {
			GLBaseTab tab = mTabs.get(tabId);
			if (tab != null) {
				return tab.getTabLevel();
			}
		}
		return ScreenEditConstants.TAB_LEVEL_1;
	}
}
