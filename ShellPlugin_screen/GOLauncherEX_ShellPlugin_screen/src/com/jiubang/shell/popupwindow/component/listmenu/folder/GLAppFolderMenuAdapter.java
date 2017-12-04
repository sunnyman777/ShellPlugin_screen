package com.jiubang.shell.popupwindow.component.listmenu.folder;

import java.util.ArrayList;

import android.content.Context;

import com.jiubang.ggheart.apps.desks.appfunc.menu.BaseMenuItemInfo;
import com.jiubang.shell.popupwindow.component.listmenu.GLBaseMenuAdapter;
/**
 * 
 * @author dingzijian
 *
 */
public class GLAppFolderMenuAdapter extends GLBaseMenuAdapter {

	public GLAppFolderMenuAdapter(Context context) {
		super(context);
	}

	public GLAppFolderMenuAdapter(Context context, ArrayList<BaseMenuItemInfo> list) {
		super(context, list);
	}
	
}
