package com.jiubang.shell.folder.smartcard;

import java.util.ArrayList;
import java.util.List;

import com.gau.golauncherex.plugin.shell.R;
import com.go.proxy.ApplicationProxy;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderInfo;
import com.jiubang.ggheart.smartcard.RecommInfoLoader;
import com.jiubang.ggheart.smartcard.Recommanditem;
import com.jiubang.shell.folder.smartcard.data.CardBuildInfo;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
/**
 * 
 * @author dingzijian
 *
 */
public class LightGameCreator implements ICardViewCreator {
	private RecommInfoLoader mInfoLoader;
	private ArrayList<Recommanditem> mRecommanditems;

	public LightGameCreator() {
		mInfoLoader = RecommInfoLoader.getLoader(ApplicationProxy.getContext());
	}
	@Override
	public List<GLAbsCardView> creat(CardBuildInfo buildData) {
		List<GLAbsCardView> cardViews = new ArrayList<GLAbsCardView>();
		switch (buildData.getType()) {
			case GLAppFolderInfo.TYPE_RECOMMAND_GAME :
				if (mRecommanditems == null) {
					mRecommanditems = (ArrayList<Recommanditem>) mInfoLoader.loadLightGamesInfo();
				}
				Recommanditem recommanditem = mRecommanditems.isEmpty() ? null : mRecommanditems
						.get(0);
				if (recommanditem != null
						&& System.currentTimeMillis() < recommanditem.getShowEndTime()) {
					GLLightGameView gameView = (GLLightGameView) ShellAdmin.sShellManager
							.getLayoutInflater().inflate(R.layout.gl_light_game_layout, null);
					gameView.setInfo(recommanditem);
					cardViews.add(gameView);
				}
				break;

			default :
				break;
		}
		return cardViews;
	}
}
