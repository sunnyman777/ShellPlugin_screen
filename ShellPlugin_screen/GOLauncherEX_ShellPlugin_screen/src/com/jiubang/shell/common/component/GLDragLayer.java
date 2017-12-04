package com.jiubang.shell.common.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.proxy.GoLauncherActivityProxy;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.statistics.StaticScreenSettingInfo;

/**
 * 拖拽层
 * @author yangguanxiang
 *
 */
public class GLDragLayer extends GLFrameLayout {

	private int mPageLeftPadding;
	private int mPageTopPadding;
	private int mPageRightPadding;
	private int mPageBottomPadding;
	private int mCellWidth;
	private int mCellHeight;
	private int mWidthGap;
	private int mHeightGap;

	private int mCurrentScreen = ScreenSettingInfo.DEFAULT_MAIN_SCREEN;

	//private SettingScreenInfo mSettingScreenInfo;

	private int mXCells = StaticScreenSettingInfo.sScreenCulumn;
	private int mYCells = StaticScreenSettingInfo.sScreenRow;
	
	public GLDragLayer(Context context) {
		super(context);
	}

	public GLDragLayer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			GLView child = getChildAt(i);
			if (child.getVisibility() == GLView.GONE) {
				continue;
			}

			final ViewGroup.LayoutParams flp = (ViewGroup.LayoutParams) child.getLayoutParams();
			if (flp instanceof LayoutParams) {
				final LayoutParams lp = (LayoutParams) flp;

				//				Log.i("pl", "x:"+lp.x+" y:"+lp.y+" lp.width"+lp.width+" lp.height"+lp.height);
				child.layout(lp.x, lp.y, lp.x + lp.width, lp.y + lp.height);
			} else {
				child.layout(0, 0, right - left, bottom - top);
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

		int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
			//throw new RuntimeException("CellLayout cannot have UNSPECIFIED dimensions");
		}

		final int xAxisCells = mXCells;
		final int yAxisCells = mYCells;
		final int pageLeftPadding = mPageLeftPadding;
		final int pageTopPadding = mPageTopPadding;
		final int pageRightPadding = mPageRightPadding;
		final int pageBottomPadding = mPageBottomPadding;
		final int cellWidth = mCellWidth;
		final int cellHeight = mCellHeight;

		int hSpaceLeft = widthSpecSize - pageLeftPadding - pageRightPadding
				- (cellWidth * xAxisCells);
		mWidthGap = hSpaceLeft / (xAxisCells - 1);
		int vSpaceLeft = heightSpecSize - pageTopPadding - pageBottomPadding
				- (cellHeight * yAxisCells);
		if ((yAxisCells - 1) == 0) {
			mHeightGap = 0;
		} else {
			mHeightGap = vSpaceLeft / (yAxisCells - 1);
		}

		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			GLView child = getChildAt(i);
			if (child.getVisibility() == GLView.GONE) {
				continue;
			}

			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();

			if (lp instanceof LayoutParams) {
				if (((LayoutParams) lp).customPosition) {
					((LayoutParams) lp).setup(mCurrentScreen, cellWidth, cellHeight,
							mWidthGap, mHeightGap, pageLeftPadding, pageTopPadding);
				}
				int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width,
						MeasureSpec.EXACTLY);
				int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height,
						MeasureSpec.EXACTLY);
				child.measure(childWidthMeasureSpec, childheightMeasureSpec);
			} else {
				child.measure(widthMeasureSpec, heightMeasureSpec);
			}

			setMeasuredDimension(widthSpecSize, heightSpecSize);
		}

		setMeasuredDimension(widthSpecSize, heightSpecSize);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}
	
	/**
	 * 
	 * <br>类描述: 自定义LayoutParams
	 * <br>功能详细描述:
	 *  
	 * @author 
	 * @date  
	 */
	public static class LayoutParams extends FrameLayout.LayoutParams {
		/*
		 * 第几屏
		 * -1则当当前屏处理
		 */
		public int screenIndex = 0;

		/**
		 * Horizontal location of the item in the grid.
		 */
		@ViewDebug.ExportedProperty
		public int cellX;

		/**
		 * Vertical location of the item in the grid.
		 */
		@ViewDebug.ExportedProperty
		public int cellY;

		/**
		 * Number of cells spanned horizontally by the item.
		 */
		@ViewDebug.ExportedProperty
		public int cellHSpan;

		/**
		 * Number of cells spanned vertically by the item.
		 */
		@ViewDebug.ExportedProperty
		public int cellVSpan;

		/**
		 * Is this item currently being dragged
		 */
		public boolean isDragging;

		// X coordinate of the view in the layout.
		@ViewDebug.ExportedProperty
		public int x;
		// Y coordinate of the view in the layout.
		@ViewDebug.ExportedProperty
		public int y;

		boolean mRegenerateId;

		boolean mDropped;

		public boolean customPosition;

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
			cellHSpan = 1;
			cellVSpan = 1;
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
			cellHSpan = 1;
			cellVSpan = 1;
		}

		public LayoutParams(int index, int cellX, int cellY, int cellHSpan, int cellVSpan) {
			super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			this.screenIndex = index;
			this.cellX = cellX;
			this.cellY = cellY;
			this.cellHSpan = cellHSpan;
			this.cellVSpan = cellVSpan;
		}

		public void setup(int curScreen, int cellWidth, int cellHeight, int widthGap,
				int heightGap, int hStartPadding, int vStartPadding) {
			int screenWidth = GoLauncherActivityProxy.getScreenWidth();

			int screenOffset = (screenIndex - curScreen) * screenWidth;

			width = cellHSpan * cellWidth + ((cellHSpan - 1) * widthGap) - leftMargin - rightMargin;
			height = cellVSpan * cellHeight + ((cellVSpan - 1) * heightGap) - topMargin
					- bottomMargin;

			x = hStartPadding + cellX * (cellWidth + widthGap) + leftMargin + screenOffset;
			y = vStartPadding + cellY * (cellHeight + heightGap) + topMargin;
		}
	}
}
