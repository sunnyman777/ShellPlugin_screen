package com.jiubang.shell.folder.smartcard;

import java.util.ArrayList;
import java.util.List;

import com.gau.golauncherex.plugin.shell.R;
import com.jiubang.shell.folder.smartcard.data.CardBuildInfo;
import com.jiubang.shell.folder.smartcard.data.LessUseAppItem;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author guoyiqing
 * 
 */
public class LessUseAppCreator implements ICardViewCreator {

	@Override
	public List<GLAbsCardView> creat(CardBuildInfo buildData) {
		List<LessUseAppItem> apps = buildData.getLessUseAppItems();
		if (apps == null || apps.isEmpty()) {
			return null;
		}
		int colum = buildData.getColumn();
		if (apps.size() > colum) {
			apps = apps.subList(0, colum);
		}
		List<GLAbsCardView> views = new ArrayList<GLAbsCardView>();
		if (apps.size() > 1) {
			GLLessUseAppMutilLayout view = (GLLessUseAppMutilLayout) ShellAdmin.sShellManager
					.getLayoutInflater()
					.inflate(R.layout.gl_smartcard_lessuseapp_mutil_layout,
							null);
			view.setLessUseApps(colum, apps);
			views.add(view);
		} else {
			GLLessUseAppSingleLayout view = (GLLessUseAppSingleLayout) ShellAdmin.sShellManager
					.getLayoutInflater().inflate(
							R.layout.gl_smartcard_lessuseapp_single_layout,
							null);
			view.setLessUseApp(apps.get(0));
			views.add(view);
		}
		return views;
	}

}
