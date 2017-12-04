/**
 * 
 */
package com.jiubang.shell.popupwindow.component.ggmenu;

import android.graphics.drawable.Drawable;

import com.jiubang.ggheart.data.info.BaseItemInfo;

/**
 * @author liuxinyang
 *
 */
public class GLGGMenuItemInfo extends BaseItemInfo {
	/**
	 * the icon used to show
	 */
	private Drawable mIcon;
	
	private String mTitle;
	
	private int mId;
	
	public GLGGMenuItemInfo() {
		
	}
	
	public GLGGMenuItemInfo(Drawable icon, String title, int id) {
		mIcon = icon;
		mTitle = title;
		mId = id;
	}
	
	public void setIcon(Drawable icon) {
		mIcon = icon;
	}
	
	public Drawable getIcon() {
		return mIcon;
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public void setId(int id) {
		mId = id;
	}
	
	public int getId() {
		return mId;
	}
}
