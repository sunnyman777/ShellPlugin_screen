package com.jiubang.shell.folder.smartcard.data;

import android.graphics.drawable.BitmapDrawable;

/**
 * 
 * @author guoyiqing
 *
 */
public class LessUseAppItem {

	private int mColumn = 4;
	
	private BitmapDrawable mIcon;
	
	private String mSize;
	
	private int mIdleDay;
	
	private String mAppName;
	
	private String mPackage;
	
	@Override
	public String toString() {
		return " mAppName:" + getAppName() + " mPackage:" + getPackage()
				+ " mIdleDay:" + getIdleDay();
	}

	public BitmapDrawable getIcon() {
		return mIcon;
	}

	public void setIcon(BitmapDrawable mIcon) {
		this.mIcon = mIcon;
	}

	public String getSize() {
		return mSize;
	}

	public void setSize(String mSize) {
		this.mSize = mSize;
	}

	public int getIdleDay() {
		return mIdleDay;
	}

	public void setIdleDay(int mIdleDay) {
		this.mIdleDay = mIdleDay;
	}

	public String getAppName() {
		return mAppName;
	}

	public void setAppName(String mAppName) {
		this.mAppName = mAppName;
	}

	public String getPackage() {
		return mPackage;
	}

	public void setPackage(String mPackage) {
		this.mPackage = mPackage;
	}

	public int getColumn() {
		return mColumn;
	}

	public void setColumn(int mColumn) {
		this.mColumn = mColumn;
	}
	
}
