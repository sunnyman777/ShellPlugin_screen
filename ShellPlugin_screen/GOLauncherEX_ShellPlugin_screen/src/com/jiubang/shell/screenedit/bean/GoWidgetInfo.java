package com.jiubang.shell.screenedit.bean;

import com.jiubang.ggheart.apps.gowidget.AbsWidgetInfo;
import com.jiubang.ggheart.data.theme.bean.PreviewSpecficThemeBean;
/**
 * 
 * <br>类描述:widget的javabean （现在使用）
 * <br>功能详细描述:
 * 
 */
public class GoWidgetInfo extends AbsWidgetInfo {
	
	public String packageName;
	public PreviewSpecficThemeBean themeBean;
	public int themeId;
	public int styleId;

	public GoWidgetInfo() {
		resouces = null;
		resouceId = -1;
		title = "";
		themeId = -1;
		mRow = 0;
		mCol = 0;
	}
}
