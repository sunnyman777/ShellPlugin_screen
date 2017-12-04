package com.jiubang.shell.folder;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.scroller.ScreenScroller;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherLogicProxy;
import com.jiubang.ggheart.appgame.base.bean.AppDetailInfoBean;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.components.folder.advert.FolderAdController;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderInfo;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.common.component.GLScrollableBaseGrid;
import com.jiubang.shell.common.component.HorScrollableGridViewHandler;
import com.jiubang.shell.folder.adapter.GLAppFolderAdViewAdapter;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.scroller.effector.CoupleScreenEffector;
/**
 * 
 * @author dingzijian
 *
 */
public class GLAppFolderAdGridView extends GLScrollableBaseGrid {

//	private HashMap<Integer, ArrayList<AppDetailInfoBean>> mRoundMap;

	private long mLastUpdateTime = -1;

	private ArrayList<Integer> mShowPos;

	private PreferencesManager mPreferencesManager;

	private static final long EIGHT_HOUR = 1000 * 60 * 60 * 8;
	
	private ArrayList<AppDetailInfoBean> mDetailInfoBeans;
	
	private BaseFolderIcon<?> mFolderIcon;
	
	public GLAppFolderAdGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		handleScrollerSetting();
		handleRowColumnSetting(false);
//		mRoundMap = new HashMap<Integer, ArrayList<AppDetailInfoBean>>();
		mPreferencesManager = new PreferencesManager(ApplicationProxy.getContext(),
				IPreferencesIds.FOLDER_AD_PREFERENCES, Context.MODE_PRIVATE);
		mLastUpdateTime = mPreferencesManager.getLong(
				IPreferencesIds.FOLDER_AD_VIEW_LAST_OPEN_TIME, -1);
		mShowPos  = new ArrayList<Integer>();
		String pos = mPreferencesManager.getString(IPreferencesIds.FOLDER_AD_VIEW_LAST_SHOW_POS, null);
		if (pos != null) {
			String[] showPos = pos.split("/");
			for (String show : showPos) {
				mShowPos.add(Integer.valueOf(show));
			}
		}
		mDetailInfoBeans = new ArrayList<AppDetailInfoBean>();
	}

	@Override
	public void onItemClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		AppDetailInfoBean detailInfoBean = (AppDetailInfoBean) mAdapter.getItem(position);
		
		downLoadDialog(detailInfoBean);
//		postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				mFolderIcon.closeFolder(true);
//
//			}
//		}, 200);
	}

	@Override
	public boolean onItemLongClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		return false;
	}

	@Override
	public void callBackToChild(GLView view) {

	}

	@Override
	public void refreshGridView() {

	}

	@Override
	public ShellBaseAdapter createAdapter(Context context, List infoList) {
		return new GLAppFolderAdViewAdapter(context, infoList);
	}

	@Override
	protected void onScrollStart() {

	}

	@Override
	protected void handleRowColumnSetting(boolean updateDB) {
		mNumRows = 1;
		mNumColumns = 4;
	}

	protected void initGridView(BaseFolderIcon<?> folderIcon, int column) {
		mFolderIcon = folderIcon;
//		int round = sparseArray.size();
//		mRoundMap.clear();
//		for (int i = 1; i <= round; i++) {
//			mRoundMap.put(i, sparseArray.get(i));
//		}
		if (mLastUpdateTime == -1 || mShowPos.isEmpty()) {
			mShowPos = new ArrayList<Integer>();
			for (int i = 0; i < column; i++) {
				mShowPos.add(0);
			}
			updateAdDataInfos(column, true);
		} else {
			if (column > mShowPos.size()) {
				mShowPos = new ArrayList<Integer>();
				for (int i = 0; i < column; i++) {
					mShowPos.add(0);
				}
			}
			if (System.currentTimeMillis() - mLastUpdateTime > EIGHT_HOUR) {
				for (int i = 0; i < mShowPos.size(); i++) {
					int pos = mShowPos.get(i);
					mShowPos.set(i, ++pos);
				}
				updateAdDataInfos(column, true);
			} else {
				updateAdDataInfos(column, false);
			}
		}

		int perHeight = (int) (GoLauncherLogicProxy.isLargeIcon() ? mContext.getResources().getDimension(
				R.dimen.folder_inside_large_icon_height_v) : mContext.getResources().getDimension(
				R.dimen.folder_inside_icon_height_v));
		android.widget.FrameLayout.LayoutParams gridParams = new android.widget.FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, perHeight, Gravity.CENTER);
		setLayoutParams(gridParams);
		setNumColumns(column);
		setData(mDetailInfoBeans);
	}

	private void updateAdDataInfos(int column, boolean saveData) {
		mDetailInfoBeans.clear();
		GLAppFolderInfo appFolderInfo = mFolderIcon.getFolderInfo();
		SparseArray<ArrayList<AppDetailInfoBean>> sparseArray = appFolderInfo.getFolderAdData();
		for (int i = 1; i <= column; i++) {
			boolean ret = getAppDetailInfoBeanList(sparseArray, i);
			if (!ret) {
				++column;
			}
		}
		if (saveData) {
			mLastUpdateTime = System.currentTimeMillis();
			mPreferencesManager.putLong(IPreferencesIds.FOLDER_AD_VIEW_LAST_OPEN_TIME,
					mLastUpdateTime);
			StringBuffer buffer = new StringBuffer();
			for (Integer pos : mShowPos) {
				buffer.append(pos + "/");
			}
			mPreferencesManager.putString(IPreferencesIds.FOLDER_AD_VIEW_LAST_SHOW_POS,
					buffer.toString());
			mPreferencesManager.commit();
		}
	}

	private boolean getAppDetailInfoBeanList(SparseArray<ArrayList<AppDetailInfoBean>> sparseArray,
			int i) {
		ArrayList<AppDetailInfoBean> arrayList = sparseArray.get(i);
		if (arrayList == null) {
			return true;
		} else if (arrayList.isEmpty()) {
			return false;
		}
		int count = i - mShowPos.size();
		for (int j = 0; j < count; j++) {
			mShowPos.add(0);
		}
		int pos = mShowPos.get(i - 1);
		if (pos >= arrayList.size()) {
			mShowPos.set(i - 1, 0);
			pos = 0;
		}
		AppDetailInfoBean appDetailInfoBean = arrayList.get(pos);
		if (appDetailInfoBean != null) {
			mDetailInfoBeans.add(appDetailInfoBean);
		}
		return true;
	}

	@Override
	protected void onScrollFinish() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleScrollerSetting() {
		if (mScrollableHandler == null) {
			mScrollableHandler = new HorScrollableGridViewHandler(mContext, this,
					CoupleScreenEffector.PLACE_NONE, false, true) {
				@Override
				protected void clipCanvas(GLCanvas canvas) {
				}

				@Override
				public void onEnterLeftScrollZone() {
				}

				@Override
				public void onEnterRightScrollZone() {
				}

				@Override
				public void onEnterTopScrollZone() {
				}

				@Override
				public void onEnterBottomScrollZone() {
				}

				@Override
				public void onExitScrollZone() {
				}

				@Override
				public void onScreenChanged(int newScreen, int oldScreen) {
					super.onScreenChanged(newScreen, oldScreen);
				}
			};
		}
		((HorScrollableGridViewHandler) mScrollableHandler)
				.setOrientation(ScreenScroller.HORIZONTAL);
	}

	@Override
	protected void onScreenChange(int newScreen, int oldScreen) {
		// TODO Auto-generated method stub
		
	}
	
	private Dialog downLoadDialog(final AppDetailInfoBean detailInfoBean) {
		DialogConfirm dialogConfirm = new DialogConfirm(
				ShellAdmin.sShellManager.getActivity());
		Resources res = ShellAdmin.sShellManager.getContext().getResources();
		dialogConfirm.show();
		dialogConfirm.setTitle(detailInfoBean.mName);
		dialogConfirm.setMessage(res.getString(R.string.fav_app));
		dialogConfirm.setPositiveButton(res.getString(R.string.ok),
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						FolderAdController.getInstance().downLoadApk(
								detailInfoBean);
						mFolderIcon.closeFolder(true);
					}

				});
		dialogConfirm.setNegativeButton(res.getString(R.string.cancel), null);

		return dialogConfirm;
	}
}
