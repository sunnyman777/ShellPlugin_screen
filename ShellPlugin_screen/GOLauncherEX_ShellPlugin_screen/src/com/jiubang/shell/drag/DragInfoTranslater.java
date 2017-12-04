package com.jiubang.shell.drag;

import java.util.ArrayList;
import java.util.List;

import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderController;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderInfo;

/**
 * 
 * 拖拽view的信息转换者
 * 
 * @author jiangxuwen
 * 
 */
public class DragInfoTranslater {

	/**
	 * 创建适合屏幕层的快捷方式信息
	 * 
	 * @param FunItemInfo
	 *            dragInfo
	 * @return
	 */
	public static final ShortCutInfo createShortCutInfoForScreen(FunAppItemInfo dragInfo) {
		if (dragInfo == null) {
			return null;
		}
		ShortCutInfo shortCutInfo = new ShortCutInfo();
		shortCutInfo.mId = -1;
		shortCutInfo.mInScreenId = -1;
		shortCutInfo.mIntent = dragInfo.getIntent();
		shortCutInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
		shortCutInfo.mSpanX = 1;
		shortCutInfo.mSpanY = 1;
		shortCutInfo.mTitle = dragInfo.getTitle();

		final AppItemInfo ainfo = dragInfo.getAppItemInfo();
		if (ainfo != null) {
			shortCutInfo.mIcon = ainfo.getIcon();
			shortCutInfo.setRelativeItemInfo(ainfo);
		}

		return shortCutInfo;
	}
	/**
	 * 创建适合屏幕层的快捷方式信息
	 * 
	 * @param FunItemInfo
	 *            dragInfo
	 * @return
	 */
	public static final ShortCutInfo createShortCutInfoForScreen(ShortCutInfo dragInfo) {
		if (dragInfo == null) {
			return null;
		}
		ShortCutInfo shortCutInfo = new ShortCutInfo(dragInfo);
		shortCutInfo.mId = -1;
		shortCutInfo.mInScreenId = dragInfo.mInScreenId;
		shortCutInfo.mSpanX = 1;
		shortCutInfo.mSpanY = 1;
		return shortCutInfo;
	}
	
	/**
	 * 创建适合屏幕层的用户文件夹信息
	 * 
	 * @param FunFolderItemInfo
	 *            dragInfo
	 * @return
	 */
	public static final UserFolderInfo createUserFolderInfoForScreen(FunFolderItemInfo dragInfo) {
		UserFolderInfo folderInfo = new UserFolderInfo();
		folderInfo.mId = -1;
		folderInfo.mInScreenId = -1;
		folderInfo.mRefId = dragInfo.getFolderId();
		folderInfo.mTitle = dragInfo.getTitle();
		folderInfo.setFeatureTitle(dragInfo.getTitle());
		folderInfo.mSpanX = 1;
		folderInfo.mSpanY = 1;
		folderInfo.setFolderType(dragInfo.getFolderType());
		folderInfo.setFolderAdDataArray(dragInfo.getFolderAdDataArray());
		List<FunAppItemInfo> contents = dragInfo.getFolderContent();
		int size = contents.size();
		for (int i = 0; i < size; i++) {
			FunAppItemInfo funAppItemInfo = contents.get(i);
			if (funAppItemInfo == null) {
				continue;
			}
			// 装填其中的元素
			final AppItemInfo itemInfo = funAppItemInfo.getAppItemInfo();
			if (itemInfo != null) {
				ShortCutInfo shortCutInfo = new ShortCutInfo();
				shortCutInfo.mId = -1;
				shortCutInfo.mIcon = itemInfo.mIcon;
				shortCutInfo.mIntent = itemInfo.mIntent;
				shortCutInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
				shortCutInfo.mSpanX = 1;
				shortCutInfo.mSpanY = 1;
				shortCutInfo.mTitle = itemInfo.mTitle;
				shortCutInfo.mTimeInFolder = System.currentTimeMillis() + i;
				shortCutInfo.setRelativeItemInfo(itemInfo);
				folderInfo.add(shortCutInfo);
			}
		}
		return folderInfo;
	}

	/**
	 * 创建适合屏幕层的快捷方式信息
	 * 
	 * @param FunItemInfo
	 *            dragInfo
	 * @return
	 */
	public static final ItemInfo createShortCutInfoForScreen(DockItemInfo dragInfo) {
		if (dragInfo == null) {
			return null;
		}
		ItemInfo itemInfo = null;
		if (dragInfo.mItemInfo instanceof ShortCutInfo) {
			itemInfo = new ShortCutInfo((ShortCutInfo) dragInfo.mItemInfo);
			itemInfo.mId = -1;
			itemInfo.mInScreenId = -1;
			final AppItemInfo ainfo = dragInfo.mItemInfo.getRelativeItemInfo();
			if (ainfo != null) {
				boolean isUserIcon = ((ShortCutInfo) itemInfo).mIsUserIcon;
				if (!isUserIcon || ((ShortCutInfo) itemInfo).mIcon == null) {
					((ShortCutInfo) itemInfo).mIcon = ainfo.getIcon();
				}
				((ShortCutInfo) itemInfo).setRelativeItemInfo(ainfo);
			}
		} else if (dragInfo.mItemInfo instanceof UserFolderInfo) {
			final UserFolderInfo srcFolder = (UserFolderInfo) dragInfo.mItemInfo;
			GLAppFolderController.getInstance().changFolderFrom(srcFolder,
					GLAppFolderInfo.FOLDER_FROM_SCREEN, GLAppFolderInfo.FOLDER_FROM_DOCK);
			UserFolderInfo folderInfo = new UserFolderInfo(srcFolder);
			folderInfo.mId = -1;
			folderInfo.mInScreenId = srcFolder.mInScreenId;
			folderInfo.mTitle = srcFolder.mTitle;
			folderInfo.setFeatureTitle(srcFolder.getFeatureTitle());
			folderInfo.mSpanX = 1;
			folderInfo.mSpanY = 1;
			ArrayList<ItemInfo> contents = srcFolder.getContents();
			folderInfo.addAll(contents);
			itemInfo = folderInfo;
			//			return folderInfo;
			//			itemInfo = new UserFolderInfo((UserFolderInfo) dragInfo.mItemInfo);
		}

		return itemInfo;
	}

	/**
	 * 创建适合屏幕层的item信息
	 * 
	 * @param int type
	 * @param Object
	 *            oldInfo
	 * @return
	 */
	public static ItemInfo createItemInfoForScreen(int type, Object oldInfo) {
		return createItemInfoForScreen(type, oldInfo, -1, -1);
	} // end createItemInfoForScreen

	/**
	 * 创建适合屏幕层的item信息
	 * 
	 * @param int type
	 * @param Object
	 *            oldInfo
	 * @return
	 */
	public static ItemInfo createItemInfoForScreen(int type, Object oldInfo, int cellX, int cellY) {
		ItemInfo info = null;
		if (oldInfo == null) {
			return info;
		}

		switch (type) {
			case DragView.DRAGVIEW_TYPE_APPDRAWER_ICON :
				info = createShortCutInfoForScreen((FunAppItemInfo) oldInfo);
				info.mCellX = cellX;
				info.mCellY = cellY;
				break;

			case DragView.DRAGVIEW_TYPE_APPDRAWER_FOLDER :
				info = createUserFolderInfoForScreen((FunFolderItemInfo) oldInfo);
				info.mCellX = cellX;
				info.mCellY = cellY;
				break;

			case DragView.DRAGVIEW_TYPE_DOCK_ICON :
			case DragView.DRAGVIEW_TYPE_DOCK_FOLDER :
				info = createShortCutInfoForScreen((DockItemInfo) oldInfo);
				// 如果是文件夹的话，screenId不能修改，否则对应的仔项数据找不到
				if (type != DragView.DRAGVIEW_TYPE_DOCK_FOLDER) {
					info.mInScreenId = -1;
				}
				info.mCellX = cellX;
				info.mCellY = cellY;
				break;
			case DragView.DRAGVIEW_TYPE_SCREEN_USERFOLDER_ELEMENT :
				info = createShortCutInfoForScreen((ShortCutInfo) oldInfo);
				info.mCellX = cellX;
				info.mCellY = cellY;
				break;
			default :
				info = (ItemInfo) oldInfo;
				info.mCellX = cellX;
				info.mCellY = cellY;
				info.mInScreenId = -1;
				break;
		}
		return info;
	} // end createItemInfoForScreen

	/**
	 * 创建适合屏幕层的快捷方式信息
	 * 
	 * @param FunItemInfo
	 *            dragInfo
	 * @return
	 */
	public static final ShortCutInfo createShortCutInforDockFromAppDrawer(FunAppItemInfo dragInfo) {
		if (dragInfo == null) {
			return null;
		}
		ShortCutInfo shortCutInfo = new ShortCutInfo();
		shortCutInfo.mId = -1;
		shortCutInfo.mInScreenId = System.currentTimeMillis();
		shortCutInfo.mIntent = dragInfo.getIntent();
		shortCutInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
		shortCutInfo.mSpanX = 1;
		shortCutInfo.mSpanY = 1;
		shortCutInfo.mTitle = dragInfo.getTitle();

		final AppItemInfo ainfo = dragInfo.getAppItemInfo();
		if (ainfo != null) {
			shortCutInfo.mIcon = ainfo.getIcon();
			shortCutInfo.setRelativeItemInfo(ainfo);
		}

		return shortCutInfo;
	}

	/**
	 * 创建适合屏幕层的用户文件夹信息
	 * 
	 * @param FunFolderItemInfo
	 *            dragInfo
	 * @return
	 */
	public static final UserFolderInfo createUserFolderInforDockFromAppDrawer(
			FunFolderItemInfo dragInfo) {
		UserFolderInfo folderInfo = new UserFolderInfo();
		folderInfo.mId = -1;
		folderInfo.mInScreenId = System.currentTimeMillis();
		folderInfo.mRefId = dragInfo.getFolderId();
		folderInfo.mTitle = dragInfo.getTitle();
		folderInfo.setFeatureTitle(dragInfo.getTitle());
		folderInfo.mSpanX = 1;
		folderInfo.mSpanY = 1;
		List<FunAppItemInfo> contents = dragInfo.getFolderContent();
		int size = contents.size();
		folderInfo.setFolderType(dragInfo.getFolderType());
		folderInfo.setFolderAdDataArray(dragInfo.getFolderAdDataArray());
		for (int i = 0; i < size; i++) {
			FunAppItemInfo funAppItemInfo = contents.get(i);
			if (funAppItemInfo == null) {
				continue;
			}
			// 装填其中的元素
			final AppItemInfo itemInfo = funAppItemInfo.getAppItemInfo();
			if (itemInfo != null) {
				ShortCutInfo shortCutInfo = new ShortCutInfo();
				shortCutInfo.mId = -1;
				shortCutInfo.mIcon = itemInfo.mIcon;
				shortCutInfo.mIntent = itemInfo.mIntent;
				shortCutInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
				shortCutInfo.mSpanX = 1;
				shortCutInfo.mSpanY = 1;
				shortCutInfo.mTitle = itemInfo.mTitle;
				shortCutInfo.mTimeInFolder = System.currentTimeMillis() + i;
				shortCutInfo.setRelativeItemInfo(itemInfo);
				folderInfo.add(shortCutInfo);
			}
		}
		return folderInfo;
	}

	/**
	 * 创建适合dock层的item信息
	 * 
	 * @param int type
	 * @param Object
	 *            oldInfo
	 * @return
	 */
	public static ItemInfo createItemInfoForDock(int type, Object oldInfo) {
		ItemInfo info = null;
		if (oldInfo == null) {
			return info;
		}
		switch (type) {
			case DragView.DRAGVIEW_TYPE_APPDRAWER_ICON :
				info = createShortCutInforDockFromAppDrawer((FunAppItemInfo) oldInfo);
				break;

			case DragView.DRAGVIEW_TYPE_APPDRAWER_FOLDER :
				info = createUserFolderInforDockFromAppDrawer((FunFolderItemInfo) oldInfo);
				break;
			case DragView.DRAGVIEW_TYPE_SCREEN_USERFOLDER_ELEMENT :
			case DragView.DRAGVIEW_TYPE_FOLDER_ICON :
			case DragView.DRAGVIEW_TYPE_SCREEN_ICON :
				ShortCutInfo shortCutInfo = new ShortCutInfo((ShortCutInfo) oldInfo);
				shortCutInfo.mInScreenId = ((ShortCutInfo) oldInfo).mInScreenId;
				shortCutInfo.setRelativeItemInfo(((ShortCutInfo) oldInfo).getRelativeItemInfo());
				info = shortCutInfo;
				break;
			case DragView.DRAGVIEW_TYPE_SCREEN_USERFOLDER :
				UserFolderInfo folderInfo = (UserFolderInfo) oldInfo;
				info = folderInfo;
				GLAppFolderController.getInstance().changFolderFrom(folderInfo,
						GLAppFolderInfo.FOLDER_FROM_SCREEN, GLAppFolderInfo.FOLDER_FROM_DOCK);
				break;
			case DragView.DRAGVIEW_TYPE_DOCK_ICON :
				info = ((DockItemInfo) oldInfo).mItemInfo;
				break;
			default :
				break;
		}
		return info;
	} // end createItemInfoForDock

}
