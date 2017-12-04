package com.jiubang.shell.appdrawer.search;

import java.util.ArrayList;

import com.jiubang.ggheart.apps.desks.appfunc.model.FuncSearchResultItem;
/**
 * 
 * @author dingzijian
 *
 */
public class SearchResultListInfo {
	
private int mType ;

private ArrayList<FuncSearchResultItem> mSearchResultItems;

public int getType() {
	return mType;
}

public void setType(int mType) {
	this.mType = mType;
}

public ArrayList<FuncSearchResultItem> getSearchResultItems() {
	return mSearchResultItems;
}

public void setSearchResultItems(ArrayList<FuncSearchResultItem> searchResultItems) {
	this.mSearchResultItems = searchResultItems;
}

}
