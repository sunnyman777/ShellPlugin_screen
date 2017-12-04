package com.jiubang.shell.dock.component;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Rect;
import android.view.ViewGroup.LayoutParams;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.SettingProxy;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.screen.CellUtils;

/**
 * dock行排版
 * 
 * @author dingzijian
 * 
 */
public abstract class AbsGLLineLayout extends GLViewGroup {

	private boolean mIsXhdpi = false; //由于新布局的代码暂时屏幕，所以这个值只为false

	private ArrayList<Position> mInitRectList = new ArrayList<Position>();

	protected int mLineID = -1; // 对应于数据库第几行
	
	public AbsGLLineLayout(Context context) {
		super(context);
	}

	public void setLineID(int id) {
		mLineID = id;
	}

	public int getLineID() {
		return mLineID;
	}

	public void updateLayout() {

	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (GoLauncherActivityProxy.isPortait()) {
			layoutPort(changed, l, t, r, b);
		} else {
			layoutLand(changed, l, t, r, b);
		}
	}

	private void layoutPort(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();
		if (count == 0) {
			return;
		}
		int iconSize = DockUtil.getIconSize(count);
		int iconWidth = (r - l) / count;
		for (int i = 0; i < count; i++) {
			IconView view = (IconView) getChildAt(i);
			int left = (iconWidth - iconSize) / 2 + iconWidth * i;
			int top = 0;
			int right = left + iconSize;
			view.layout(left, top, right, b);
			ViewPositionTag tag = (ViewPositionTag) view
					.getTag(R.integer.dock_view_left);
			if (tag == null) {
				tag = new ViewPositionTag();
				view.setTag(R.integer.dock_view_left, tag);
			}
			tag.oldLayoutX = tag.newLayoutX = tag.tempLayoutX = left
					+ (right - left) / 2;
		}
		setInitRect(count);
	}

	private void layoutLand(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();
		if (count == 0) {
			return;
		}
		int iconSize = DockUtil.getIconSize(count);
		int iconHeight = (b - t) / count;
		for (int i = 0; i < count; i++) {
			IconView view = (IconView) getChildAt(i);
			int left = 0;
			int top = iconHeight * (count - i - 1) + (iconHeight - iconSize)
					/ 2; // 控制点击范围
			int right = r;
			int bottom = top + iconSize;
			view.layout(left, top, right, bottom);
			ViewPositionTag tag = (ViewPositionTag) view
					.getTag(R.integer.dock_view_left);
			if (tag == null) {
				tag = new ViewPositionTag();
				view.setTag(R.integer.dock_view_left, tag);
			}
			tag.oldLayoutX = tag.newLayoutX = tag.tempLayoutX = top
					+ (bottom - top) / 2;
		}
		setInitRect(count);

	}

	public void setInitRect(int count) {
		mInitRectList = new ArrayList<Position>();
		int mDockBgHeight = DockUtil.getBgHeight();
		int bitmap_size = DockUtil.getIconSize(count);
		if (count == 0) {
			return;
		}
		GLView parent = (GLView) getGLParent();
		int tempH = parent.getBottom();

		int tempW = parent.getRight();

		if (GoLauncherActivityProxy.isPortait()) {
			if (SettingProxy.getDesktopSettingInfo().getMarginEnable()) {
				int iconWidth = (tempW - CellUtils.sLeftGap) / count;
				for (int i = 0; i < count; i++) {
					int left = (iconWidth - bitmap_size) / 2 + iconWidth * i;
					int top = parent.getTop();
					int right = left + bitmap_size;
					int bottom = tempH;
					Position position = new Position();
					position.outRect = new Rect(left, top, right, bottom);
					int width = bitmap_size;
					int height = mDockBgHeight;
					position.innerRect = new Rect(left + width / 5, top + height / 5, right - width
							/ 5, bottom - height / 5);
					mInitRectList.add(position);
				}
			} else {
				int iconWidth = tempW / count;
				for (int i = 0; i < count; i++) {
					int left = (iconWidth - bitmap_size) / 2 + iconWidth * i;
					int top = parent.getTop();
					int right = left + bitmap_size;
					int bottom = tempH;
					Position position = new Position();
					position.outRect = new Rect(left, top, right, bottom);
					int width = bitmap_size;
					int height = mDockBgHeight;
					position.innerRect = new Rect(left + width / 5, top + height / 5, right - width
							/ 5, bottom - height / 5);
					mInitRectList.add(position);
				}
			}

		} else {
			if (!StatusBarHandler.isHide()) {
				tempH = tempH - StatusBarHandler.getStatusbarHeight();
			}
			if (mIsXhdpi) {
				int a = (tempH - count * bitmap_size) / (count + 1);
				int paddingLand = a / 2 >= DrawUtils.dip2px(12) ? a / 2 : DrawUtils.dip2px(12);
				int iconHeight = (tempH - 2 * paddingLand) / count;
				for (int i = 0; i < count; i++) {
					int left = parent.getLeft();
					int top = iconHeight * (count - i - 1) + paddingLand; // 控制点击范围
					int right = tempW;
					int bottom = top + iconHeight;
					Position position = new Position();
					position.outRect = new Rect(left, top, right, bottom);
					int width = mDockBgHeight;
					int height = iconHeight;
					position.innerRect = new Rect(left + width / 5, top + height / 5, right - width
							/ 5, bottom - height / 5);
					mInitRectList.add(position);
				}
			} else {
				int iconHeight = tempH / count;
				for (int i = 0; i < count; i++) {
					int left = parent.getLeft();
					int top = iconHeight * (count - i - 1) + (iconHeight - bitmap_size) / 2; // 控制点击范围
					int right = tempW;
					int bottom = top + bitmap_size;
					Position position = new Position();
					position.outRect = new Rect(left, top, right, bottom);
					int width = mDockBgHeight;
					int height = bitmap_size;
					position.innerRect = new Rect(left + width / 5, top + height / 5, right - width
							/ 5, bottom - height / 5);
					mInitRectList.add(position);
				}
			}

		}
	}

	public ArrayList<Position> getInitRectList() {

		return mInitRectList;

	}
	
	@Override
	public boolean addViewInLayout(GLView child, int index, LayoutParams params,
			boolean preventRequestLayout) {
		return super.addViewInLayout(child, index, params, preventRequestLayout);
	}

	@Override
	public boolean addViewInLayout(GLView child, int index, LayoutParams params) {
		return super.addViewInLayout(child, index, params);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		measureChildren(widthMeasureSpec, heightMeasureSpec);
	}

}
