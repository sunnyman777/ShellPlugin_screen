package com.jiubang.shell.screen;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.gau.golauncherex.plugin.shell.R;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.SettingProxy;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;

/**
 * 根据新老用户获取不同的格子大小
 * @author dengdazhong
 *
 */
public class CellUtils {
	public static int sActualCellWidthPortRes = R.dimen.actual_cell_width_port;
	public static int sActualCellHeightPortRes = R.dimen.actual_cell_height_port;
	public static int sActualCellWidthLandRes = R.dimen.actual_cell_width_land;
	public static int sActualCellHeightLandRes = R.dimen.actual_cell_height_land;
	public static int sCellWidthPortRes = R.dimen.cell_width_port;
	public static int sCellHeightPortRes = R.dimen.cell_height_port;
	public static int sCellWidthLandRes = R.dimen.cell_width_land;
	public static int sCellHeightLandRes = R.dimen.cell_height_land;
	
	public static int sLeftGap = 0;
	public static int sRightGap = 0;
	
	public static void init(Context context) {
		PreferencesManager pm = new PreferencesManager(ApplicationProxy.getContext());
		boolean usingOldCellLayout = pm.getBoolean(IPreferencesIds.PREFERENCE_IS_USING_OLD_CELLLAYOUT, false);
		if (usingOldCellLayout) {
			sActualCellWidthPortRes = R.dimen.actual_cell_width_port_old;
			sActualCellHeightPortRes = R.dimen.actual_cell_height_port_old;
			sActualCellWidthLandRes = R.dimen.actual_cell_width_land_old;
			sActualCellHeightLandRes = R.dimen.actual_cell_height_land_old;
			sCellWidthPortRes = R.dimen.cell_width_port_old;
			sCellHeightPortRes = R.dimen.cell_height_port_old;
			sCellWidthLandRes = R.dimen.cell_width_land_old;
			sCellHeightLandRes = R.dimen.cell_height_land_old;
		} else {
			sActualCellWidthPortRes = R.dimen.actual_cell_width_port;
			sActualCellHeightPortRes = R.dimen.actual_cell_height_port;
			sActualCellWidthLandRes = R.dimen.actual_cell_width_land;
			sActualCellHeightLandRes = R.dimen.actual_cell_height_land;
			sCellWidthPortRes = R.dimen.cell_width_port;
			sCellHeightPortRes = R.dimen.cell_height_port;
			sCellWidthLandRes = R.dimen.cell_width_land;
			sCellHeightLandRes = R.dimen.cell_height_land;
		}
		
		measureScreenMargin(context);
	}
	
	private static void measureScreenMargin(Context context) {
		Log.v("ddz", "measureScreenMargin");
		sLeftGap = sRightGap = 0;
		if (SettingProxy.getDesktopSettingInfo().getMarginEnable()) {
			boolean isPortrait = DrawUtils.sHeightPixels > DrawUtils.sWidthPixels;
			Resources resources = context.getResources();

			int cellWidth;
			// 根据横竖屏加载不同尺寸信息
			if (isPortrait) {
				cellWidth = resources
						.getDimensionPixelSize(CellUtils.sCellWidthPortRes);
			} else {
				cellWidth = resources
						.getDimensionPixelSize(CellUtils.sCellWidthLandRes);
			}
			int columns = SettingProxy.getDesktopSettingInfo().getColumns();
			int a = (DrawUtils.sWidthPixels - columns * cellWidth)
					/ (columns - 1);
			int minA = DrawUtils.dip2px(24);
			if (a < minA) {
				a = minA;
			}
			sLeftGap = sRightGap = a / 2;
		}
	}
	
	public static int measureGoWidgetMinWidth(Context context, int column) {
		int cellWidth = context.getResources().getDimensionPixelSize(sCellWidthPortRes);
		return (column - 1) * cellWidth;
	}
	
	public static int measureGoWidgetMinHeight(Context context, int row) {
		int cellHeight = context.getResources().getDimensionPixelSize(sCellHeightPortRes);
		return (row - 1) * cellHeight;
	}
}