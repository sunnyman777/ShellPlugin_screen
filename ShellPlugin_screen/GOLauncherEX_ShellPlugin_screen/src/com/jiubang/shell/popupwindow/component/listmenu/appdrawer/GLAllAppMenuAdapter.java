package com.jiubang.shell.popupwindow.component.listmenu.appdrawer;

import java.util.ArrayList;

import android.content.Context;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.jiubang.ggheart.apps.desks.appfunc.menu.AllAppMenuControler;
import com.jiubang.ggheart.apps.desks.appfunc.menu.AppFuncAllAppMenuItemInfo;
import com.jiubang.ggheart.apps.desks.appfunc.menu.BaseMenuItemInfo;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.popupwindow.component.listmenu.GLBaseMenuAdapter;
import com.jiubang.shell.popupwindow.component.listmenu.GLBaseMenuItemView;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-9-27]
 */
public class GLAllAppMenuAdapter extends GLBaseMenuAdapter {

	private AllAppMenuControler mControler;
	
	public GLAllAppMenuAdapter(Context context, ArrayList<BaseMenuItemInfo> list) {
		super(context, list);
		mControler = AllAppMenuControler.getInstance();
	}

	@Override
	public GLView getView(int position, GLView convertView, GLViewGroup parent) {
		GLView view = super.getView(position, convertView, parent);
		BaseMenuItemInfo info = mList.get(position);
		if (info.mActionId == AppFuncAllAppMenuItemInfo.ACTION_RUNNING) {
			if (view == null || view.getId() != R.id.promanage_menu_item) {
				GLLayoutInflater inflater = ShellAdmin.sShellManager.getLayoutInflater();
				view = inflater.inflate(R.layout.gl_appdrawer_promanage_menu_item, null);
			}
		} else {
			GLBaseMenuItemView txtView = (GLBaseMenuItemView) view
					.findViewById(R.id.gl_appdrawer_base_menu_text);
			//先清除各种状态
			txtView.setTitleNum(0);
			txtView.setNewDrawable(false);
			int type = mControler.checkMenuItemStatus(info.mActionId);
			if (type == AllAppMenuControler.ITEM_STATUS_SHOW_NUM) {
				txtView.setTitleNum(getBeancount());
			} else if (type == AllAppMenuControler.ITEM_STATUS_SHOW_NEW) {
				txtView.setNewDrawable(true);
			}
		}
		return view;
	}
	
	private static final String GOSTORECOUNT = "gostorecount";
	/**
	 * <br>
	 * 功能简述:获取保存在shareprefencd中的应用程序可更新个数。 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	private int getBeancount() {
		// 从shareprefencd里得到数字
		PreferencesManager preferences = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE,
				Context.MODE_PRIVATE);
		int mBeancount = preferences.getInt(GOSTORECOUNT, 0);
		return mBeancount;
	}
}
