package com.jiubang.shell.folder.smartcard.data;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;

/**
 * 
 * @author guoyiqing
 *
 */
public class UpdateAppItem {

	private int mColumn = 4;
	
	private BitmapDrawable mIcon;
	
	private String mSize;
	
	private long mUpdateTime;
	
	private String mAppName;
	
	private Intent mMainIntent;
	
	private long mLastUpdateTime;
	
	@Override
	public String toString() {
		return "mAppName:" + getAppName() + " mMainIntent:" + getMainIntent()
				+ " mSize:" + getSize() + " mUpdateTime:" + getUpdateTime();
	}

	public int getColumn() {
		return mColumn;
	}

	public void setColumn(int mColumn) {
		this.mColumn = mColumn;
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

	public long getUpdateTime() {
		return mUpdateTime;
	}

	public void setUpdateTime(long mUpdateTime) {
		this.mUpdateTime = mUpdateTime;
	}

	public String getAppName() {
		return mAppName;
	}

	public void setAppName(String mAppName) {
		this.mAppName = mAppName;
	}

	public Intent getMainIntent() {
		return mMainIntent;
	}

	public void setMainIntent(Intent mMainIntent) {
		this.mMainIntent = mMainIntent;
	}

	public long getLastUpdateTime() {
		return mLastUpdateTime;
	}

	public void setLastUpdateTime(long mLastUpdateTime) {
		this.mLastUpdateTime = mLastUpdateTime;
	}
	
}
