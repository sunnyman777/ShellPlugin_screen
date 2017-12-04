package com.jiubang.shell.dock.component;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.view.GLView.OnLongClickListener;
import com.go.gl.view.GLViewGroup;
import com.go.proxy.GoLauncherActivityProxy;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderController;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderInfo;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.folder.GLAppFolder;
import com.jiubang.shell.folder.GLDockFolderIcon;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
/**
 * 
 * @author dingzijian
 *
 */
public class GLDockLinerLayoutAdapter {
	protected ConcurrentHashMap<Integer, ArrayList<DockItemInfo>> mItemInfoHashMap;

	public GLDockLinerLayoutAdapter(ConcurrentHashMap<Integer, ArrayList<DockItemInfo>> hashMap) {
		mItemInfoHashMap = hashMap;
	}
	public int getCount() {
		return mItemInfoHashMap.size();
	}

	public ArrayList<DockItemInfo> getItem(int pos) {
		return mItemInfoHashMap.get(pos);
	}

//	public long getItemId(int arg0) {
//		return 0;
//	}

	public AbsGLLineLayout getView(int pos, GLView convertVIew, GLViewGroup parent) {
		ArrayList<IconView<?>> dockViewList = initIcon(pos);
		int listCount = dockViewList.size();
		GLDockLineLayout layout = (GLDockLineLayout) convertVIew;
		GLDockLineLayoutContainer container = (GLDockLineLayoutContainer) parent;
		if (layout == null) {
			layout = (GLDockLineLayout) getLineLayout(container.getDragController());
		}
		if (layout != null) {
			layout.cleanUpAllViews();
			layout.removeAllViews();
			layout.setLineID(pos);
			for (int i = 0; i < listCount; i++) {
				IconView<?> view = dockViewList.get(i);
				DockItemInfo dockItemInfo = (DockItemInfo) view.getInfo();
				dockItemInfo.setmIndexInRow(i);
				view.setTag(R.integer.dock_index, i);
				view.setOnTouchListener(container);
				view.setOnClickListener((OnClickListener) layout);
				view.setOnLongClickListener((OnLongClickListener) layout);
				layout.addView(view);
			}
			layout.setOperaterLayout();
			layout.setGLDockLinerLayoutAdapter(this);
		}
		return layout;
	}

	protected ArrayList<IconView<?>> initIcon(int row) {
		ArrayList<IconView<?>> m_IconViews = new ArrayList<IconView<?>>();
		IconView<?> dockIconView = null;
		for (int i = 0; i < DockUtil.ICON_COUNT_IN_A_ROW; i++) {
			dockIconView = initIcon(row, i); // 初始化每个图标
			if (null != dockIconView) {
				int addIndex = 0;
				int listSize = m_IconViews.size();
				for (int j = 0; j < listSize; j++) {
					IconView<?> view = m_IconViews.get(j);
					if (((DockItemInfo) view.getInfo()).getmIndexInRow() < ((DockItemInfo) dockIconView
							.getInfo()).getmIndexInRow()) {
						addIndex++;
					} else {
						break;
					}
				}
				m_IconViews.add(addIndex, dockIconView);
			}
		}
		return m_IconViews;
	}

	protected IconView<?> initIcon(int rowid, int index) {
		DockItemInfo info = null;
		try {
			info = mItemInfoHashMap.get(rowid).get(index);

		} catch (Exception e) {
			// 后台数据异常
		}
		if (null == info) {
			// 说明这一行这个索引没有数据
			return null;
		}
		return initIcon(info);
	}

	public IconView<?> initIcon(DockItemInfo info) {
		if (null == info) {
			return null;
		}
		if (info.mItemInfo instanceof UserFolderInfo) {
			GLAppFolderInfo appFolderInfo = new GLAppFolderInfo((UserFolderInfo) info.mItemInfo,
					GLAppFolderInfo.FOLDER_FROM_DOCK);
			GLAppFolderController.getInstance().addFolderInfo(appFolderInfo);
			GLDockFolderIcon dockIconView = (GLDockFolderIcon) getView(info);
//			dockIconView.setInfo(info);
			if (info.isCustomStyle()) {
				dockIconView.useFolderFeatureIcon();
			}
			//			dockIconView.setOnTouchListener(mGLDockLineLayoutContainer);
			//			dockIconView.setOnClickListener(this);
			//			dockIconView.setOnLongClickListener(this);
			dockIconView.setFocusable(true);
			return dockIconView;
		} else {
			IconView<?> iconView = (GLDockIconView) getView(info);
			//			iconView.setOnTouchListener(mGLDockLineLayoutContainer);
			//			iconView.setOnClickListener(this);
			//			iconView.setOnLongClickListener(this);
			iconView.setFocusable(true);
			return iconView;
		}
	}

	private IconView<?> getView(DockItemInfo info) {
		GLLayoutInflater inflater = ShellAdmin.sShellManager.getLayoutInflater();
		IconView iconView = null;
		if (info.mItemInfo instanceof UserFolderInfo) {
			GLAppFolder appFolder = GLAppFolder.getInstance();
			GLAppFolderController controller = GLAppFolderController.getInstance();
			GLAppFolderInfo folderInfo = controller
					.getFolderInfoById(((UserFolderInfo) info.mItemInfo).mInScreenId,
							GLAppFolderInfo.FOLDER_FROM_DOCK);
			iconView = appFolder.getFolderIcon(folderInfo);
			iconView.setInfo(info);
			iconView.setIconSize(info.getBmpSize());
			iconView.setVisible(true);
		} else {
			iconView = (IconView<?>) inflater.inflate(R.layout.gl_dock_icon, null);
			iconView.setInfo(info);
			iconView.setIconSize(info.getBmpSize());
		}
		return iconView;
	}
	
	private AbsGLLineLayout getLineLayout(DragController controller) {
		GLDockLineLayout layout = new GLDockLineLayout(GoLauncherActivityProxy.getActivity(), controller);
		return layout;
	}
	
	public synchronized void modifyData(int lineId) {
		ArrayList<DockItemInfo> oldDockItemInfos = mItemInfoHashMap.get(lineId);
		DockItemInfo dockItemInfoArray[] = new DockItemInfo[oldDockItemInfos.size()];
		for (DockItemInfo dockItemInfo : oldDockItemInfos) {
			int index = dockItemInfo.getmIndexInRow();
			dockItemInfoArray[index] = dockItemInfo;
		}
		if (dockItemInfoArray.length > 0) {
			ArrayList<DockItemInfo> newDockItemInfos = new ArrayList<DockItemInfo>();
			for (DockItemInfo dockItemInfo : dockItemInfoArray) {
				newDockItemInfos.add(dockItemInfo);
			}
			oldDockItemInfos = mItemInfoHashMap.replace(lineId, newDockItemInfos);
			oldDockItemInfos.clear();
		}
	}
}
