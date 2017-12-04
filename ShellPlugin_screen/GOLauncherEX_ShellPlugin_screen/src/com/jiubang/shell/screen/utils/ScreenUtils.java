package com.jiubang.shell.screen.utils;

import java.util.ArrayList;

import mobi.intuitit.android.content.LauncherIntent;
import mobi.intuitit.android.content.LauncherMetadata;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.view.View;

import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.view.GLViewParent;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.Search;
import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ScreenAppWidgetInfo;
import com.jiubang.ggheart.data.info.ScreenFolderInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.shell.screen.GLCellLayout;
import com.jiubang.shell.screen.GLWorkspace;
import com.jiubang.shell.widget.component.GLWidgetContainer;
import com.jiubang.shell.widget.component.GLWidgetView;

/**
 * 屏幕层工具类
 * 
 */
public class ScreenUtils {
	/**
	 * 根据{@link ItemInfo#mInScreenId} 获取在屏幕上对应的view
	 * 
	 * @param itemId
	 *            {@link ItemInfo#mInScreenId}
	 * @param screenIndex
	 *            屏幕索引，小于0遍历整个屏幕
	 * @param workspace
	 * @return
	 */
	public static GLView getViewByItemId(long itemId, int screenIndex, GLWorkspace workspace) {
		if (workspace == null) {
			return null;
		}
		if (screenIndex >= workspace.getChildCount()) {
			return null;
		}
		if (screenIndex < 0) {
			int screenCount = workspace.getChildCount();
			for (int screen = 0; screen < screenCount; screen++) {
				GLCellLayout currentScreen = (GLCellLayout) workspace.getChildAt(screen);
				if (currentScreen != null) {
					int count = currentScreen.getChildCount();
					for (int i = 0; i < count; i++) {
						GLView child = currentScreen.getChildAt(i);
						if (child != null) {
							Object tagObject = child.getTag();
							if (tagObject != null && tagObject instanceof ItemInfo
									&& ((ItemInfo) tagObject).mInScreenId == itemId) {
								return child;
							}
						}
					}
				}
			}
		} else {
			GLCellLayout currentScreen = (GLCellLayout) workspace.getChildAt(screenIndex);
			if (currentScreen != null) {
				int count = currentScreen.getChildCount();
				for (int i = 0; i < count; i++) {
					GLView child = currentScreen.getChildAt(i);
					if (child != null) {
						Object tagObject = child.getTag();
						if (tagObject != null && tagObject instanceof ItemInfo
								&& ((ItemInfo) tagObject).mInScreenId == itemId) {
							return child;
						}
					}
				}
			}
		}
		return null;
	}

	public static CharSequence getItemTitle(ItemInfo targetInfo) {
		if (targetInfo == null) {
			return null;
		}

		CharSequence title = null;
		if (targetInfo.mItemType == IItemType.ITEM_TYPE_APPLICATION
				|| targetInfo.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
			title = ((ShortCutInfo) targetInfo).mTitle;
		} else if (targetInfo.mItemType == IItemType.ITEM_TYPE_USER_FOLDER) {
			title = ((ScreenFolderInfo) targetInfo).mTitle;
		}
		return title;
	}

	public static float easeOut(float begin, float end, float t) {
		t = 1 - t;
		return begin + (end - begin) * (1 - t * t * t);
	}

	public static void removeViewByItemInfo(ItemInfo itemInfo, GLWorkspace workspace) {
		GLView targetView = getViewByItemId(itemInfo.mInScreenId, -1, workspace);
		if (targetView != null) {
			GLViewParent parent = targetView.getGLParent();
			if (parent != null && parent instanceof GLViewGroup) {
				((GLViewGroup) parent).removeView(targetView);
			}
		}
		itemInfo.selfDestruct();
	}

	public static boolean findVacant(int[] xy, int spanX, int spanY, int screenIndex,
			GLWorkspace workspace) {
		return findVacant(xy, spanX, spanY, screenIndex, workspace, null);
	}
	
	public static boolean findVacant(int[] xy, int spanX, int spanY, int screenIndex,
			GLWorkspace workspace, GLView ignoreView) {
		if (screenIndex < 0 || screenIndex >= workspace.getChildCount()) {
			return false;
		}

		final GLCellLayout destScreen = (GLCellLayout) workspace.getChildAt(screenIndex);
		if (destScreen == null) {
			return false;
		}
		boolean isExistVacant = destScreen.getVacantCell(xy, spanX, spanY, ignoreView);
		return isExistVacant;
	}

	public static void unbindShortcut(ShortCutInfo shortCutInfo) {
		if (shortCutInfo != null && shortCutInfo.mIcon != null) {
			shortCutInfo.selfDestruct();
		}
	}

	public static void unbindeUserFolder(UserFolderInfo folderInfo) {
		if (folderInfo != null) {
			folderInfo.clear();
			folderInfo.selfDestruct();
		}
	}

	public static void appwidgetReadyBroadcast(int appWidgetId, ComponentName cname,
			int[] widgetSpan, Context context) {
		if (GoWidgetManager.isGoWidget(appWidgetId)) {
			return;
		}

		Intent motosize = new Intent(ICustomAction.ACTION_SET_WIDGET_SIZE);

		motosize.setComponent(cname);
		motosize.putExtra("appWidgetId", appWidgetId);
		motosize.putExtra("spanX", widgetSpan[0]);
		motosize.putExtra("spanY", widgetSpan[1]);
		motosize.putExtra("com.motorola.blur.home.EXTRA_NEW_WIDGET", true);
		context.sendBroadcast(motosize);

		Intent ready = new Intent(LauncherIntent.Action.ACTION_READY)
				.putExtra(LauncherIntent.Extra.EXTRA_APPWIDGET_ID, appWidgetId)
				.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
				.putExtra(LauncherIntent.Extra.EXTRA_API_VERSION,
						LauncherMetadata.CurrentAPIVersion).setComponent(cname);
		context.sendBroadcast(ready);
	}

	public static int computeIndex(int cur, int src, int dst) {
		if (src > cur && dst <= cur) {
			++cur; // 从当前位置后面移到前面
		} else if (src < cur && dst >= cur) {
			--cur; // 从当前位置前面移到后面
		} else if (src == cur) {
			cur = dst;
		}
		return cur;
	}

	public static void unbindDesktopObject(ArrayList<ItemInfo> screenInfos) {
		if (screenInfos == null) {
			return;
		}

		for (ItemInfo itemInfo : screenInfos) {
			if (itemInfo == null) {
				continue;
			}

			final int itemType = itemInfo.mItemType;
			switch (itemType) {
				case IItemType.ITEM_TYPE_APPLICATION : {
					unbindShortcut((ShortCutInfo) itemInfo);
					break;
				}
				case IItemType.ITEM_TYPE_SHORTCUT : {
					unbindShortcut((ShortCutInfo) itemInfo);
					break;
				}

				case IItemType.ITEM_TYPE_USER_FOLDER : {
					unbindeUserFolder((UserFolderInfo) itemInfo);
					break;
				}

				default :
					break;
			}
		}
	}

	public static boolean ocuppiedArea(int screen, int id, Rect rect, GLWorkspace workspace) {
		int screenCount = workspace.getChildCount();
		if (screen < 0 || screen >= screenCount) {
			return false;
		}

		GLCellLayout cellLayout = (GLCellLayout) workspace.getChildAt(screen);
		if (cellLayout == null) {
			return false;
		}

		Rect r = new Rect();
		final int childCount = cellLayout.getChildCount();
		for (int i = 0; i < childCount; i++) {
			GLView view = cellLayout.getChildAt(i);
			if (view == null || view.getTag() == null) {
				continue;
			}

			ItemInfo it = (ItemInfo) view.getTag();
			if (it instanceof ScreenAppWidgetInfo && ((ScreenAppWidgetInfo) it).mAppWidgetId == id) {
				continue;
			}

			r.set(it.mCellX, it.mCellY, it.mCellX + it.mSpanX, it.mCellY + it.mSpanY);
			if (rect.intersect(r)) {
				return true;
			}
		}

		return false;
	}

	public static Search findSearchOnCurrentScreen(GLWorkspace workspace) {
		GLCellLayout currentScreen = workspace.getCurrentScreenView();
		if (currentScreen != null) {
			for (int i = 0; i < currentScreen.getChildCount(); i++) {
				GLView view = currentScreen.getChildAt(i);
				if (view != null && view instanceof GLWidgetContainer) {
					GLWidgetContainer container = (GLWidgetContainer) view;
					GLView widget = container.getWidget();
					if (widget != null && widget instanceof GLWidgetView) {
						View realView = ((GLWidgetView) widget).getView();
						if (realView != null && realView instanceof Search) {
							return (Search) realView;
						}
					}

				}
			}
		}
		return null;
	}
}
