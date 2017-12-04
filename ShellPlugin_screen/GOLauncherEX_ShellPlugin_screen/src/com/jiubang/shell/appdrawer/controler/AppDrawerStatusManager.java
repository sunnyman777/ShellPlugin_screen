package com.jiubang.shell.appdrawer.controler;

/**
 * 功能表状态管理者
 * @author wuziyi
 *
 */
public class AppDrawerStatusManager extends AbsStatusManager {

	public final static int ALLAPP_TAB = 0x100;
//	public final static int RECENTAPP_TAB = 0x200;
//	public final static int PROMANAGER_TAB = 0x300;
	
	private static AppDrawerStatusManager sInstance;
	
	public static AppDrawerStatusManager getInstance() {
		if (sInstance == null) {
			sInstance = new AppDrawerStatusManager();
		}
		return sInstance;
	}
	
	@Override
	public void changeSceneStatus(int tabStatusId, Object... objects) {
		if (mCurSceneId != tabStatusId) {
			mCurSceneId = tabStatusId;
			int curId = mCurSceneId | GRID_NORMAL_STATUS;
//			changeGridStatus(GRID_NORMAL_STATUS);
			mCurGridId = GRID_NORMAL_STATUS;
			Status oldStatus = mCurStatus;
			mCurStatus = generateStatusByCurId(curId);
			for (StatusChangeListener listener : mListeners) {
				listener.onSceneStatusChange(oldStatus, mCurStatus, objects);
			}
		}
	}

	@Override
	public void changeGridStatus(int gridStatusId, Object... objects) {
		if (mCurGridId != gridStatusId) {
			mCurGridId = gridStatusId;
			int curId = mCurSceneId | mCurGridId;
			Status oldStatus = mCurStatus;
			mCurStatus = generateStatusByCurId(curId);
			for (StatusChangeListener listener : mListeners) {
				listener.onGridStatusChange(oldStatus, mCurStatus, objects);
			}
		}
	}

	@Override
	public void changeStatus(int tabStatusId, int gridStatusId, Object... objects) {
		if (mCurSceneId != tabStatusId && mCurGridId != gridStatusId) {
			mCurSceneId = tabStatusId;
			mCurGridId = gridStatusId;
			int curId = mCurSceneId | mCurGridId;
			Status oldStatus = mCurStatus;
			mCurStatus = generateStatusByCurId(curId);
			for (StatusChangeListener listener : mListeners) {
				listener.onSceneStatusChange(oldStatus, mCurStatus, objects);
			}
		} else if (mCurSceneId != tabStatusId) {
			changeSceneStatus(tabStatusId);
		} else if (mCurGridId != gridStatusId) {
			changeGridStatus(gridStatusId);
		}
	}

}
