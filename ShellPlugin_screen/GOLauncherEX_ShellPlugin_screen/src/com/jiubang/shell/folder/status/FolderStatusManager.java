package com.jiubang.shell.folder.status;

import com.jiubang.shell.appdrawer.controler.AbsStatusManager;
import com.jiubang.shell.appdrawer.controler.Status;
import com.jiubang.shell.appdrawer.controler.StatusChangeListener;
import com.jiubang.shell.drag.DragController;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-3-11]
 */
public class FolderStatusManager extends AbsStatusManager {
	private static volatile FolderStatusManager sInstance;

	public final static int SCREEN_FOLDER_GRID = 11;

	public final static int DRAWER_FOLDER_GRID = 22;

	public final static int DOCK_FOLDER_GRID = 33;

	public final static int FOLDER_HIDE_STATUS = 44;

	public final static int FOLDER_SILENT_MODE_STATUS = 77;

	public final static int FOLDER_POWER_SAVING_MODE_STATUS = 66;
	
	private FolderStatusManager() {
	}
	public static FolderStatusManager getInstance() {
		if (sInstance == null) {
			sInstance = new FolderStatusManager();
		}
		return sInstance;
	}

	public DragController getDragController() {
		return mDragController;
	}
	@Override
	public void changeSceneStatus(int tabStatusId, Object... objects) {
		if (mCurSceneId != tabStatusId) {
			mCurSceneId = tabStatusId;
			int curId = mCurSceneId | GRID_NORMAL_STATUS;
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
	public void changeStatus(int sceneStatusId, int gridStatusId, Object... objects) {
		if (sceneStatusId == FOLDER_HIDE_STATUS) {
			mCurSceneId = FOLDER_HIDE_STATUS;
			mCurGridId = FOLDER_HIDE_STATUS;
		} else if (mCurSceneId != sceneStatusId && mCurGridId != gridStatusId) {
			mCurSceneId = sceneStatusId;
			mCurGridId = gridStatusId;
			int curId = mCurSceneId | mCurGridId;
			Status oldStatus = mCurStatus;
			mCurStatus = generateStatusByCurId(curId);
			for (StatusChangeListener listener : mListeners) {
				listener.onSceneStatusChange(oldStatus, mCurStatus, objects);
			}
		} else if (mCurSceneId != sceneStatusId) {
			changeSceneStatus(sceneStatusId);
		} else if (mCurGridId != gridStatusId) {
			changeGridStatus(gridStatusId);
		}
	}
}
