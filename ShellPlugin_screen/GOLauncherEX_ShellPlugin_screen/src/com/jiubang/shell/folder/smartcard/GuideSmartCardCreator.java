package com.jiubang.shell.folder.smartcard;

import java.util.ArrayList;
import java.util.List;

import com.gau.golauncherex.plugin.shell.R;
import com.go.proxy.ApplicationProxy;
import com.jiubang.ggheart.apps.desks.diy.pref.PrefConst;
import com.jiubang.ggheart.apps.desks.diy.pref.PrivatePreference;
import com.jiubang.shell.folder.smartcard.data.CardBuildInfo;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
/**
 * 
 * @author dingzijian
 *
 */
public class GuideSmartCardCreator implements ICardViewCreator {

	@Override
	public List<GLAbsCardView> creat(CardBuildInfo buildData) {
		List<GLAbsCardView> absCardViews = null;
		PrivatePreference preference = PrivatePreference.getPreference(ApplicationProxy
				.getContext());
		boolean show = preference.getBoolean(PrefConst.KEY_SMART_CARD_GUIDE_PAGE_WEATHER_THE_SHOW,
				true);
		if (show) {
			absCardViews = new ArrayList<GLAbsCardView>();
			GLSmartCardGuidePage guidePage1 = (GLSmartCardGuidePage) ShellAdmin.sShellManager
					.getLayoutInflater().inflate(R.layout.gl_smart_card_guide_page, null);
			guidePage1.setGuidePage1();
			GLSmartCardGuidePage guidePage2 = (GLSmartCardGuidePage) ShellAdmin.sShellManager
					.getLayoutInflater().inflate(R.layout.gl_smart_card_guide_page, null);
			guidePage2.setGuidePage2();
			absCardViews.add(guidePage1);
			absCardViews.add(guidePage2);
			preference.putBoolean(PrefConst.KEY_SMART_CARD_GUIDE_PAGE_WEATHER_THE_SHOW, false);
			preference.commit();
		}
		return absCardViews;

	}

}
