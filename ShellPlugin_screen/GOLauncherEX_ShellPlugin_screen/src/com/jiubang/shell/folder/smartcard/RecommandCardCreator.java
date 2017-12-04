package com.jiubang.shell.folder.smartcard;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.gau.golauncherex.plugin.shell.R;
import com.go.proxy.ApplicationProxy;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderInfo;
import com.jiubang.ggheart.smartcard.RecommInfoLoader;
import com.jiubang.ggheart.smartcard.Recommanditem;
import com.jiubang.shell.folder.smartcard.GLRecommandAppView.OnNextBtnClickListener;
import com.jiubang.shell.folder.smartcard.data.CardBuildInfo;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author guoyiqing
 *
 */
public class RecommandCardCreator implements ICardViewCreator, OnNextBtnClickListener {
	private RecommInfoLoader mInfoLoader;
	private ArrayList<Recommanditem> mRecommanditems;
	private GLRecommandAppView mRecommandAppView;
	private CardBuildInfo mCardBuildInfo;

	public RecommandCardCreator() {
		mInfoLoader = RecommInfoLoader.getLoader(ApplicationProxy.getContext());
		
	}
	@Override
	public List<GLAbsCardView> creat(CardBuildInfo data) {
		List<GLAbsCardView> cardViews = new ArrayList<GLAbsCardView>();
		mCardBuildInfo = data;
		switch (data.getType()) {
			case GLAppFolderInfo.NO_RECOMMAND_FOLDER :
				break;
			default :
				mRecommanditems = (ArrayList<Recommanditem>) mInfoLoader.loadFoldsInfo().get(
						data.getType());
				Recommanditem recommanditem = getNextRecommandApp(data.getId());
				if (mRecommandAppView != null) {
					mRecommandAppView.cleanup();
				}
				mRecommandAppView = (GLRecommandAppView) ShellAdmin.sShellManager
						.getLayoutInflater().inflate(R.layout.gl_recommand_app_view, null);
				mRecommandAppView.setNextBtnClickListener(this);
				if (recommanditem != null) {
					updateRecommandView(recommanditem);
					cardViews.add(mRecommandAppView);
				}
				break;
		}
		return cardViews;
	}

	@Override
	public void onNextBtnClick() {
		updateRecommandView(getNextRecommandApp(mCardBuildInfo.getId()));
	}
	
	private void updateRecommandView(Recommanditem recommanditem) {
		boolean showNextBtn = mRecommanditems != null && mRecommanditems.size() > 1 ? true : false;
		mRecommandAppView.setInfo(recommanditem, showNextBtn);
	}

	public Recommanditem getNextRecommandApp(long folderId) {
		Recommanditem recommanditem = null;
		PreferencesManager preferencesManager = new PreferencesManager(
				ApplicationProxy.getContext(), IPreferencesIds.SMART_CARD_REC_APP_INDEX,
				Context.MODE_PRIVATE);
		int index = preferencesManager.getInt(String.valueOf(folderId), 0);
		if (mRecommanditems != null && !mRecommanditems.isEmpty()) {
			index++;
			index = index >= mRecommanditems.size() ? 0 : index;
			recommanditem = mRecommanditems.get(index);
			preferencesManager.putInt(String.valueOf(folderId), index);
			preferencesManager.commit();
		}
		return recommanditem;
	}
}
