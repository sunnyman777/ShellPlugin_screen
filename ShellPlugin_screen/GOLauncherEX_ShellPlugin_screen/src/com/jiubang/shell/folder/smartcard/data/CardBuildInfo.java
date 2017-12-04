package com.jiubang.shell.folder.smartcard.data;

import java.util.List;

import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.smartcard.Recommanditem;

/**
 * 
 * @author guoyiqing
 * 
 */
public class CardBuildInfo {

	private int mColumn = 4;

	private int mType;

	private long mId;

	private List<AppItemInfo> mAppItemInfos;

	private List<LessUseAppItem> mLessUseAppItems;

	private List<UpdateAppItem> mUpdateAppItems;

	private List<Recommanditem> mRecomAppItems;

	private List<Recommanditem> mRecomGameLightItems;

	public CardBuildInfo(int column, int type, long id, List<AppItemInfo> infos) {
		mColumn = column;
		mType = type;
		mId = id;
		mAppItemInfos = infos;
	}
	
	@Override
	public String toString() {
		return " mColumn:" + getColumn() + " mType:" + getType() + " mId:" + getId()
			    + " mLessUseAppItems:"
				+ getLessUseAppItems() + " mUpdateAppItems:" + getUpdateAppItems()
				+ " mRecomAppItems:" + getRecomAppItems()
				+ " mRecomGameLightItems:" + getRecomGameLightItems();
	}

	public int getType() {
		return mType;
	}

	public void setType(int mType) {
		this.mType = mType;
	}

	public long getId() {
		return mId;
	}

	public void setId(long mId) {
		this.mId = mId;
	}

	public int getColumn() {
		return mColumn;
	}

	public void setColumn(int mColumn) {
		this.mColumn = mColumn;
	}

	public List<AppItemInfo> getAppItemInfos() {
		return mAppItemInfos;
	}

	public void setAppItemInfos(List<AppItemInfo> mAppItemInfos) {
		this.mAppItemInfos = mAppItemInfos;
	}

	public List<LessUseAppItem> getLessUseAppItems() {
		return mLessUseAppItems;
	}

	public void setLessUseAppItems(List<LessUseAppItem> mLessUseAppItems) {
		this.mLessUseAppItems = mLessUseAppItems;
	}

	public List<UpdateAppItem> getUpdateAppItems() {
		return mUpdateAppItems;
	}

	public void setUpdateAppItems(List<UpdateAppItem> mUpdateAppItems) {
		this.mUpdateAppItems = mUpdateAppItems;
	}

	public List<Recommanditem> getRecomAppItems() {
		return mRecomAppItems;
	}

	public void setRecomAppItems(List<Recommanditem> mRecomAppItems) {
		this.mRecomAppItems = mRecomAppItems;
	}

	public List<Recommanditem> getRecomGameLightItems() {
		return mRecomGameLightItems;
	}

	public void setRecomGameLightItems(List<Recommanditem> mRecomGameLightItems) {
		this.mRecomGameLightItems = mRecomGameLightItems;
	}
}
