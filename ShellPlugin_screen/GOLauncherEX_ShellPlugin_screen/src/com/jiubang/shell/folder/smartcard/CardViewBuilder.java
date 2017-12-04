package com.jiubang.shell.folder.smartcard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.jiubang.shell.folder.GLAppFolderExpandContentLayout;
import com.jiubang.shell.folder.smartcard.data.CardBuildInfo;
import com.jiubang.shell.folder.smartcard.data.LocalAppLoader;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author guoyiqing
 * 
 */
public class CardViewBuilder {

	private static CardViewBuilder sInstance;
	private List<ICardViewCreator> mCreators;
	private Comparator<GLAbsCardView> mComparator;
	private LocalAppLoader mLoader;
	private Context mContext;
	private SparseArray<Integer> mCurrentCardTypeMap;
	
	private CardViewBuilder(Context context) {
		mContext = context;
		mCurrentCardTypeMap = new SparseArray<Integer>();
		mLoader = new LocalAppLoader();
		mCreators = new ArrayList<ICardViewCreator>();
		mCreators.add(new MemCleanCreator(context));
		mCreators.add(new LessUseAppCreator());
		mCreators.add(new UpdateAppCardCreator());
		mCreators.add(new LightGameCreator());
		mCreators.add(new RecommandCardCreator());
		mCreators.add(new GuideSmartCardCreator());
		mComparator = new Comparator<GLAbsCardView>() {

			@Override
			public int compare(GLAbsCardView object1, GLAbsCardView object2) {
				if (object1.isShowed() == object2.isShowed()) {
					return -(object1.getOrderLevel() - object2.getOrderLevel());
				} else if (object1.isShowed()) {
					return 1;
				} else {
					return -1;
				}
			}
		};
	}

	public static synchronized CardViewBuilder getBuilder(Context context) {
		if (sInstance == null) {
			sInstance = new CardViewBuilder(context);
		}
		return sInstance;
	}
	/**
	 * 
	 * @author dingzijian
	 *
	 */
	class BuildSmartCardAsyncTask extends AsyncTask<CardBuildInfo, Void, CardBuildInfo> {
		
		private Handler mHandler;
		
		public BuildSmartCardAsyncTask(Handler handler) {
			mHandler = handler;
		}
		@Override
		protected CardBuildInfo doInBackground(CardBuildInfo... params) {
			mLoader.load(mContext, params[0]);
			return params[0];
		}
		
		@Override
		protected void onPostExecute(CardBuildInfo result) {
			GLSmartCardLayout layout = (GLSmartCardLayout) ShellAdmin.sShellManager.getLayoutInflater()
					.inflate(R.layout.gl_smartcard_layout, null);
			List<GLAbsCardView> views = buildViews(result);
			if (views.isEmpty()) {
				layout.setVisibility(GLView.GONE);
			} else {
				int typeId = result.getType();
				Integer next = mCurrentCardTypeMap.get(typeId);
				if (next == null) {
					next = 0;
				} else {
					int length = views.size();
					boolean has = false;
					for (int i = 0; i < length; i++) {
						if (views.get(i).getCardType() == next) {
							next = i + 1;
							has = true;
							break;
						}
					}
					if (!has) {
						next = 0;
					}
				}
				layout.setCurrentCardTypeMap(mCurrentCardTypeMap);
				layout.setCardViews(typeId, next, views);
				Message message = mHandler.obtainMessage(
						GLAppFolderExpandContentLayout.MSG_UPDATE_APP_FOLDER_EXPAND_CONTENT_LAYOUT,
						layout);
				mHandler.sendMessage(message);
			}
		}
	}
	
	public void build(CardBuildInfo data, Handler handler) {
		BuildSmartCardAsyncTask buildSmartCardAsyncTask = new BuildSmartCardAsyncTask(handler);
		buildSmartCardAsyncTask.execute(data);
	}

	private List<GLAbsCardView> buildViews(CardBuildInfo data) {
		List<GLAbsCardView> views = new ArrayList<GLAbsCardView>();
		List<GLAbsCardView> temps = null;
		for (ICardViewCreator creator : mCreators) {
			temps = creator.creat(data);
			if (temps != null) {
				views.addAll(temps);
			}
		}
		Collections.sort(views, mComparator);
		return views;
	}

}
