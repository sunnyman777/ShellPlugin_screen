package com.jiubang.shell.appdrawer.controler;

import java.util.ArrayList;

import android.util.SparseArray;

import com.jiubang.shell.IShell;
import com.jiubang.shell.drag.DragController;

/**
 * 状态管理者
 * @author wuziyi
 *
 */
public abstract class AbsStatusManager {
	
	public final static int GRID_NORMAL_STATUS = 0x10;
	public final static int GRID_EDIT_STATUS = 0x20;
	
	protected Status mCurStatus;
	
	protected int mCurSceneId;
	
	protected int mCurGridId;

	protected ArrayList<StatusChangeListener> mListeners;

	protected StatusFactory mStatusFactory;

	protected IShell mShell;
	
	protected DragController mDragController;

	protected SparseArray<Status> mStatusMap;
	
	protected AbsStatusManager() {
		super();
		mListeners = new ArrayList<StatusChangeListener>();
		mStatusFactory = new StatusFactory();
		mStatusMap = new SparseArray<Status>();
	}

	public Status getCurStatus() {
		return mCurStatus;
	}

	public void registListener(StatusChangeListener listener) {
		mListeners.add(listener);
	}

	/**
	 * 切换tab状态，强制赋值normalGrid状态
	 * @param tabStatusId
	 */
	public abstract void changeSceneStatus(int tabStatusId, Object... objects);

	/**
	 * 切换当前tab的gird状态
	 * @param gridStatusId
	 */
	public abstract void changeGridStatus(int gridStatusId, Object... objects);

	public abstract void changeStatus(int tabStatusId, int gridStatusId, Object... objects);

	protected Status generateStatusByCurId(int curId) {
		Status status = mStatusMap.get(curId);
		if (status == null) {
			status = mStatusFactory.createStatus(curId);
			if (null != status) {
				status.setShell(mShell);
				status.setDragControler(mDragController);
				mStatusMap.put(curId, status);
			}
		}
		return status;
	}

	public void setShell(IShell shell) {
		mShell = shell;
		if (mCurStatus != null) {
			mCurStatus.setShell(mShell);
		}
	}

	public void setDragController(DragController dragController) {
		mDragController = dragController;
		if (mCurStatus != null) {
			mCurStatus.setDragControler(mDragController);
		}
	}
}
