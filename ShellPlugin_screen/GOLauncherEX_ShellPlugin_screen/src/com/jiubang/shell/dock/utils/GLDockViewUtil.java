package com.jiubang.shell.dock.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.graphics.Rect;
import android.view.View;

import com.go.gl.view.GLViewParent;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.SettingProxy;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenUtils;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.folder.FolderConstant;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.shell.dock.component.AbsGLLineLayout;
import com.jiubang.shell.dock.component.GLDockIconView;
import com.jiubang.shell.screen.CellUtils;

/**
 * 
 * <br>
 * 类描述:dockview内部工具类 <br>
 * 功能详细描述:
 * 
 * @author ruxueqin
 * @date [2012-9-20]
 */
public class GLDockViewUtil {
	/**
	 * 判断当前是否发光
	 */
	public void judgeShowCurrentIconLight(GLDockIconView view) {
		if (view == null) {
			return;
		}
		try {
			ItemInfo info = view.getInfo().mItemInfo;
			if (info.mItemType != IItemType.ITEM_TYPE_APPLICATION
					|| info.mItemType != IItemType.ITEM_TYPE_SHORTCUT) {
			} else if ((((ShortCutInfo) info).mIntent == null)
					|| (!ICustomAction.ACTION_BLANK
							.equals(((ShortCutInfo) info).mIntent.getAction()))) {
			}
		} catch (Exception e) {

		}
	}

	public static float easeOut(float begin, float end, float t) {
		t = 1 - t;
		return begin + (end - begin) * (1 - t * t * t);
	}

	/**
	 * 获取一个在文件夹内的图标在桌面的位置
	 * 
	 * @param targetRect
	 * @param sequenceNum
	 *            　文件夹内的第几个图标，1-4算在文件夹内位置，5或以上算文件夹中间位置
	 * @param targetView
	 *            用于获取排版参数的类似的dockIconView
	 * @return　
	 */
	public static Rect getAIconRectInAFolder(int sequenceNum,
			GLDockIconView targetView) {
		if (sequenceNum <= 0 || null == targetView) {
			// bad params
			return null;
		}

		// 桌面图标与dock大小不一致，在folderIcon算图标摆放位置时，是以桌面图标大小来计算的
		AbsGLLineLayout lineLayout = (AbsGLLineLayout) targetView.getGLParent();
		final int iconDockSize = DockUtil.getIconSize(lineLayout
				.getChildCount());

		// 计算上LineLayout在dockView里的排版参数
		int parentLeft = 0;
		int parentTop = 0;
		GLViewParent parent = targetView.getGLParent();
		if (null != parent && parent instanceof View) {
			View parentView = (View) parent;
			parentLeft += parentView.getLeft();
			parentTop += parentView.getTop();
		}

		// 处理第>=5以上图标
		if (sequenceNum > FolderConstant.MAX_ICON_COUNT) {
			int l = parentLeft + targetView.getLeft() + targetView.getWidth()
					/ 2;
			int t = parentTop + targetView.getTop() + targetView.getHeight()
					/ 2;
			int r = l;
			int b = t;
			return new Rect(l, t, r, b);
		}

		Rect rect = new Rect();
		final int col = (sequenceNum % 2 == 0) ? 1 : 0;
		final int row = (sequenceNum > 2) ? 1 : 0;

		final float first = iconDockSize * 0.12f;
		final float grap = iconDockSize * 0.015f;
		final int innerIconSize = (int) (iconDockSize - first * 2 - grap * 2) / 2;
		final float left = first + col * (innerIconSize + grap * 2);
		final float top = first + row * (innerIconSize + grap * 2);

		rect.left = parentLeft + targetView.getLeft()
				+ targetView.getPaddingLeft() + (int) left;
		rect.top = parentTop + targetView.getTop() + targetView.getPaddingTop()
				+ (int) top;
		rect.right = rect.left + innerIconSize;
		rect.bottom = rect.top + innerIconSize;

		return rect;
	}

	/**
	 * 获取15个初始化系统程序，可能找到不足15个 不足的部分，用功能表中的应用填补 外部用完负责释放ArrayList<DockItemInfo>
	 * 
	 * @param engine
	 * @return
	 */
	public static ArrayList<DockItemInfo> getInitDockData() {
		ArrayList<DockItemInfo> list = new ArrayList<DockItemInfo>();
		// 获取常用应用
		ArrayList<AppItemInfo> mDefaultInitAppList = null;
		ArrayList<AppItemInfo> dbItemInfos = null;
		try {
			String[] packageName = ScreenUtils.getDefaultInitAppPkg();
			final AppDataEngine dataEngine = AppDataEngine.getInstance(ApplicationProxy.getContext());
			dbItemInfos = dataEngine.getAllAppItemInfos();
			mDefaultInitAppList = new ArrayList<AppItemInfo>();
			for (int i = 0; i < packageName.length; i++) {
				if (mDefaultInitAppList.size() > DockUtil.DOCK_COUNT) {
					break;
					}
				for (AppItemInfo dbItemInfo : dbItemInfos) {
					if (null != dbItemInfo.mIntent.getComponent()) {
						String dbPackageName = dbItemInfo.mIntent.getComponent().getPackageName();
						if (dbPackageName.equals(packageName[i])) {
							mDefaultInitAppList.add(dbItemInfo);
							break;
						}
					}
				}
			}
			final int size = mDefaultInitAppList.size();
			if (size > ScreenUtils.sScreenInitedDefaultAppCount) {
				for (int i = ScreenUtils.sScreenInitedDefaultAppCount; i < size; i++) {
					AppItemInfo dbItemInfo = mDefaultInitAppList.get(i);
					DockItemInfo dockItemInfo = new DockItemInfo(IItemType.ITEM_TYPE_APPLICATION,
									DockUtil.ICON_COUNT_IN_A_ROW);
							ShortCutInfo shortCutInfo = (ShortCutInfo) dockItemInfo.mItemInfo;
							shortCutInfo.mFeatureIconType = ImagePreviewResultType.TYPE_DEFAULT;
							shortCutInfo.mFeatureTitle = dbItemInfo.getTitle();
							shortCutInfo.mIntent = dbItemInfo.mIntent;

							list.add(dockItemInfo);
						}
					}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mDefaultInitAppList != null) {
				mDefaultInitAppList.clear();
				mDefaultInitAppList = null;
				}
			if (dbItemInfos != null) {
				dbItemInfos.clear();
				dbItemInfos = null;
			}
		}
		//不足15个，用功能表应用填补空缺
		if (list.size() < DockUtil.DOCK_COUNT) {
			ArrayList<AppItemInfo> mAllAppItemList = null;
			List<AppItemInfo> infoRemoved = new ArrayList<AppItemInfo>();
			try {
				String[] packageNames = ScreenUtils.getDefaultInitAppPkg();
				final AppDataEngine dataEngine = AppDataEngine.getInstance(ApplicationProxy.getContext());
				mAllAppItemList = (ArrayList<AppItemInfo>) dataEngine.getAllAppItemInfos().clone();
				// 去重
				for (AppItemInfo info : mAllAppItemList) {
					for (String pkgName : packageNames) {
						boolean needRemoved = false;
						if (info.mIntent.getComponent() == null) {
							needRemoved = true;
						} else {
							String funcAppPkgName = info.mIntent.getComponent().getPackageName();
							// 排除常用应用
							if (funcAppPkgName.equals(pkgName)) {
								needRemoved = true;
							} else {
								//dock条固定四个快捷方式去重
								for (String protogenicAppPkgName : ScreenUtils.PROTOGENIC_APP_PKGS) {
									if (protogenicAppPkgName.equals(pkgName)) {
										needRemoved = true;
					break;
				}
			}
							}
						}
						if (needRemoved) {
							infoRemoved.add(info);
						}
					}
				}
				mAllAppItemList.removeAll(infoRemoved);
				// 排除已经添加到屏幕广告页中的应用
				infoRemoved.clear();
				int removeCount = ScreenUtils.sScreenInitedDefaultAppCountAppFunc;
				if (mAllAppItemList.size() <= removeCount) {
					mAllAppItemList.clear();
				} else {
					int count = 0;
					while (count++ < removeCount) {
						mAllAppItemList.remove(0);
					}
				}
				Iterator<AppItemInfo> iteratorAllApp = mAllAppItemList.iterator();
				while (iteratorAllApp.hasNext() && list.size() < DockUtil.DOCK_COUNT) {
					AppItemInfo dbItemInfo = iteratorAllApp.next();
					DockItemInfo dockItemInfo = new DockItemInfo(IItemType.ITEM_TYPE_APPLICATION,
							DockUtil.ICON_COUNT_IN_A_ROW);
					ShortCutInfo shortCutInfo = (ShortCutInfo) dockItemInfo.mItemInfo;
					shortCutInfo.mFeatureIconType = ImagePreviewResultType.TYPE_DEFAULT;
					shortCutInfo.mFeatureTitle = dbItemInfo.getTitle();
					shortCutInfo.mIntent = dbItemInfo.mIntent;

					list.add(dockItemInfo);
					iteratorAllApp.remove();
				}
		} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (infoRemoved != null) {
					infoRemoved.clear();
					infoRemoved = null;
		}
				if (mAllAppItemList != null) {
					mAllAppItemList.clear();
					mAllAppItemList = null;
				}
			}
		}
		return list;
	}
	
	/**
	 * <br>功能简述: 将屏幕层坐标转换成Dock层坐标
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param point 传入的屏幕层原始坐标，不能为空，必须只能包含x，y两个维度
	 */
	public static void transformPointScreen2Dock(int[] point) {
		if (point == null || point.length != 2) {
			return;
		}
		if (SettingProxy.getDesktopSettingInfo().getMarginEnable()) {
			point[0] -=  CellUtils.sLeftGap;
		}
	}
	
	/**
	 * <br>功能简述: 将屏幕层坐标转换成Dock层坐标
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param point 传入的屏幕层原始坐标，不能为空，必须只能包含x，y两个维度
	 */
	public static void transformPointScreen2Dock(float[] point) {
		if (point == null || point.length != 2) {
			return;
		}
		if (SettingProxy.getDesktopSettingInfo().getMarginEnable()) {
			point[0] -=  CellUtils.sLeftGap;
		}
	}
}
