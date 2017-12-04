package com.jiubang.shell.folder.smartcard;

import java.util.ArrayList;
import java.util.List;

import com.gau.golauncherex.plugin.shell.R;
import com.jiubang.shell.folder.smartcard.data.CardBuildInfo;
import com.jiubang.shell.folder.smartcard.data.UpdateAppItem;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author guoyiqing
 * 
 */
public class UpdateAppCardCreator implements ICardViewCreator {

	@Override
	public List<GLAbsCardView> creat(CardBuildInfo buildData) {
		List<UpdateAppItem> apps = buildData.getUpdateAppItems();
		if (apps == null || apps.isEmpty()) {
			return null;
		}
		List<GLAbsCardView> views = new ArrayList<GLAbsCardView>();
		if (apps.size() > 1) {
			GLUpdateAppMutilLayout view = (GLUpdateAppMutilLayout) ShellAdmin.sShellManager
					.getLayoutInflater().inflate(
							R.layout.gl_smartcard_updateapp_mutil_layout, null);
			view.setUpdateApps(buildData.getColumn() , apps);
			views.add(view);
		} else {
			GLUpdateAppSingleLayout view = (GLUpdateAppSingleLayout) ShellAdmin.sShellManager
					.getLayoutInflater()
					.inflate(R.layout.gl_smartcard_updateapp_single_layout,
							null);
			view.setUpdateAppItem(apps.get(0));
			views.add(view);
		}
		return views;
	}

}
