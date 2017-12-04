package com.jiubang.shell.appdrawer.controler;

import com.go.gl.view.GLView;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.dock.component.GLDockIconView;

/**
 * to control the bussiness logic, which including 'new flag of icon view' and others
 * @author hanson
 * 
 */
public class IconViewController {

	private static IconViewController sViewController;

	public static IconViewController getInstance() {
		if (sViewController == null) {
			sViewController = new IconViewController();
		}
		return sViewController;
	}

	/**
	 * remove the new flag from iconview whose data stored in databsae
	 * @param view
	 */
	public void removeIconNewFlag(GLView view) {
		try {
			if (view != null && view instanceof IconView) {
				// fixme refactor remove the new flag from the icon
				IconView glview = (IconView) view;
				Object viewobj = glview.getInfo();

				if (viewobj instanceof ShortCutInfo) {
					// glworkspace
					ShortCutInfo shortcut = (ShortCutInfo) viewobj;
					AppItemInfo appinfo = shortcut.getRelativeItemInfo();
					if (appinfo != null) {
						appinfo.setIsNewApp(false);
					}
				} else if (viewobj instanceof FunAppItemInfo) {
					// this including folder,appdrawer
					FunAppItemInfo info = (FunAppItemInfo) viewobj;
					AppItemInfo appinfo = info.getAppItemInfo();
					if (appinfo != null) {
						appinfo.setIsNewApp(false);
					}
				} else if (glview instanceof GLDockIconView) {
					//dock
					GLDockIconView info = (GLDockIconView) glview;
					AppItemInfo appinfo = info.getInfo().mItemInfo
							.getRelativeItemInfo();
					if (appinfo != null) {
						appinfo.setIsNewApp(false);
						//fixme: move the bussiness logic(above) from appinfo to here
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
