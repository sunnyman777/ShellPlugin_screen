package com.jiubang.shell.common.component;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.shell.drag.DragScroller;
import com.jiubang.shell.indicator.VerticalIndicator;
import com.jiubang.shell.scroller.ShellCycloidScroller;
import com.jiubang.shell.scroller.ShellScroller;
import com.jiubang.shell.scroller.ShellScrollerEffector;
import com.jiubang.shell.scroller.ShellScrollerListener;
import com.jiubang.shell.scroller.effector.ShellVerticalListContainer;
import com.jiubang.shell.scroller.effector.ShellWaterFallEffector;

/**
 * 
 * @author yangguanxiang
 *
 */
public abstract class VerScrollableGridViewHandler extends AbsScrollableGridViewHandler implements ShellScrollerListener, ShellVerticalListContainer {

	/**
	 * 竖向滚动器
	 */
	private ShellScroller mScroller;
	/**
	 * 是否循环滚动
	 */
	private boolean mCycleMode = false; 
	/**
	 * 竖屏行数
	 */
	private int mTotalRows = 0;
	/**
	 * 每行高度
	 */
	private int mRowHeight = 0;
	/**
	 * 当前可见的第一行下标
	 */
	private int mFVisiableRow = 0;
	/**
	 * 当前可见的最后一行下标
	 */
	private int mLVisiableRow = 0;
	/**
	 * 列表可视区域的真实高度
	 */
	private int mActualHeight = 0;
	/**
	 * 列表实际高度（每行高度 * 行数）
	 */
	private int mTotalHeight = 0;
	/**
	 * TouchDown事件的Y坐标
	 */
	private int mTouchDownY;
	/**
	 * 竖向指示器
	 */
	private VerticalIndicator mIndicator;
	/**
	 * 指示器宽度
	 */
	private int mIndicatorWidth;
	/**
	 * 竖屏滚动特效类型
	 */
	private int mVerticalEffectorType = 0;
	/**
	 * 没有特效
	 */
	public final static int NO_VERTICAL_EFFECTOR = 0;
	/**
	 * 3D瀑布特效
	 */
	public final static int WATERFALL_VERTICAL_EFFECTOR = 1;
	/**
	 * 垂直滚动的特效
	 */
	private ShellScrollerEffector mVerticalEffector = null;
	/**
	 * 功能表顶部容器高度
	 */
	private int mTopContainerHeight = 0;
	/**
	 * 功能表底部容器高度
	 */
	private int mBottomContainerHeight = 0;
	/**
	 * 假图标与真图标的间隔距离（行高一半）
	 */
	private int mFakeIconMargin = 0;
	
	private SparseArray<ArrayList<GLView>> mHoldRow = new SparseArray<ArrayList<GLView>>();
	
	public VerScrollableGridViewHandler(Context context, GLScrollableBaseGrid view) {
		super(context, view);
		init();
	}
	
	private void init() {
		mScroller = new ShellScroller(mContext, this);
		mScroller.setOrientation(ShellScroller.VERTICAL);
		DisplayMetrics mMetrics = mContext.getResources().getDisplayMetrics();
		mIndicatorWidth = (int) (AppFuncConstants.SCROLL_SIZE * mMetrics.density);
		mTopContainerHeight = mContext.getResources().getDimensionPixelSize(R.dimen.appdrawer_top_bar_container_height);
		mBottomContainerHeight = mContext.getResources().getDimensionPixelSize(R.dimen.appdrawer_bottom_bar_container_height);
	}
	
	@Override
	public void setCycleScreenMode(boolean mode) {
		if (mCycleMode == mode) {
			return;
		}
		mCycleMode = mode;
		if (mScroller != null) {
			mScroller.setCycleMode(this, mode, mContext);
		}
	}
	
	/**
	 * 更新布局数据
	 */
	private void updateLayoutParams() {
		float curPercent = mScroller.getScroll() * 1.0f / mScroller.getLastScroll(); // 记录当前滚动的百分比
		if (curPercent < 0) { // 判断避免比例小于0或大于1
			curPercent = 0;
		} else if (curPercent > 1) {
			curPercent = 1;
		}
		mTotalRows = (mGridView.getItemCount() + mGridView.mNumColumns - 1) / mGridView.mNumColumns;
		mActualHeight = mGridView.getHeight() - mGridView.getPaddingTop() - mGridView.getPaddingBottom() - (mGridView.mNumRows - 1) * mHorizontalSpacing;
		mRowHeight = mActualHeight / mGridView.mNumRows;
		mFakeIconMargin = mRowHeight / 3;
		
		mTotalHeight = mRowHeight * mTotalRows;
		mScroller.setSize(mGridView.getWidth(), mGridView.getHeight(), mGridView.getWidth(), mTotalHeight);
		mScroller.setPadding(mGridView.getPaddingTop(), mGridView.getPaddingBottom());
		mScroller.setScroll((int) (mScroller.getLastScroll() * curPercent)); // 还原layout前的滚动量百分比
	}
	
	/**
	 * 构造一行图标
	 * @param rowNum
	 */
	private void makeRow(int rowNum) {
		if (rowNum < 0 || rowNum > mTotalRows - 1 || null == mGridView.mAdapter) {
			return;
		}
		final int startPos = rowNum * mGridView.mNumColumns;
		final int paddingTop = mGridView.getPaddingTop();
		final int paddingLeft = mGridView.getPaddingLeft();
		final int paddingRight = mGridView.getPaddingRight();
		final int actualWidth = mGridView.getWidth() - paddingLeft - paddingRight - (mGridView.mNumColumns - 1) * mVerticalSpacing;
		final int columnWidth = actualWidth / mGridView.mNumColumns;
		final int rowSpacing = rowNum * mRowHeight;

		GLScrollableBaseGrid.LayoutParams p = new GLScrollableBaseGrid.LayoutParams(
				GLScrollableBaseGrid.LayoutParams.MATCH_PARENT, GLScrollableBaseGrid.LayoutParams.MATCH_PARENT);
		int pos = startPos;
		int x = paddingLeft;
		int y = paddingTop + rowSpacing;
		if (rowNum == 0) {
			y += mHorizontalSpacing;
		}
		ArrayList<GLView> holder = new ArrayList<GLView>();
		int count = mGridView.mAdapter.getCount();
		for (int i = 0; i < mGridView.mNumColumns; i++) {
			if (pos < count) {
				GLView child;
				child = mGridView.obtainView(pos);
				child.setLayoutParams(p);
				if (child.isPressed()) {
					child.setPressed(false);
				}
				holder.add(child);
				mGridView.addViewInLayout(child, pos, p, true);
//				int childHeightSpec = GLViewGroup.getChildMeasureSpec(
//						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 0, p.height);
//				int childWidthSpec = GLViewGroup.getChildMeasureSpec(
//						MeasureSpec.makeMeasureSpec(columnWidth, MeasureSpec.EXACTLY), 0, p.width);
//				child.measure(childWidthSpec, childHeightSpec);
				
				mesureChildInLayout(columnWidth, mRowHeight, p, child);
				child.layout(x, y, x + columnWidth, y + mRowHeight);
				pos++;
				x += columnWidth;
				mGridView.callBackToChild(child);
			}
		}
		mHoldRow.put(rowNum, holder);
		mGridView.invalidate();
	}
	
	protected void mesureChildInLayout(final int columnWidth, final int rowHeight,
			GLScrollableBaseGrid.LayoutParams p, GLView child) {
		boolean measured = false;
		int childHeightSpec = GLViewGroup.getChildMeasureSpec(
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 0, p.height);
		int childWidthSpec = GLViewGroup.getChildMeasureSpec(
				MeasureSpec.makeMeasureSpec(columnWidth, MeasureSpec.EXACTLY), 0, p.width);
		if (child instanceof IconView<?>) {
			IconView<?> icon = (IconView<?>) child;
			boolean autoTextLine = icon.isEnableAutoTextLine();
			if (autoTextLine) {
				childHeightSpec = GLViewGroup.getChildMeasureSpec(
						MeasureSpec.makeMeasureSpec(rowHeight, MeasureSpec.EXACTLY), 0, p.height);
				child.measure(childWidthSpec, childHeightSpec); // 用于自适应文字行数
				icon.setEnableAutoTextLine(false);
				childHeightSpec = GLViewGroup.getChildMeasureSpec(
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 0, p.height);
				child.measure(childWidthSpec, childHeightSpec);
				icon.setEnableAutoTextLine(autoTextLine);
				measured = true;
			}
		}
		if (!measured) {
			child.measure(childWidthSpec, childHeightSpec);
		}
	}
	
	/**
	 * 更新当前可见的首行和末行下标
	 */
	private void updateCurVisibleRow() {
		if (mRowHeight == 0) {
			return;
		}
		int firstVisiableRow = getScrollY() / mRowHeight;
		mFVisiableRow = firstVisiableRow;
		if (mFVisiableRow > 0) {
			mFVisiableRow--; // 顶部多画一行
		} else {
			mFVisiableRow = 0;
		}
		if (mTotalRows > mGridView.mNumRows) {
			mLVisiableRow = firstVisiableRow + mGridView.mNumRows; // 顶部也会多画一行
			if (mLVisiableRow == mTotalRows) {
				mLVisiableRow = mTotalRows - 1;
			}
		} else {
			mLVisiableRow = mTotalRows - 1;
		}
	}

	@Override
	public void layoutChildren() {
		updateLayoutParams();
		updateCurVisibleRow();
		clearHolder();
		for (int i = 0; i < mTotalRows; i++) {
			makeRow(i);
		}
		if (mIndicator != null) {
			// 指示器更新相关参数
			mIndicator.setParameter(mTotalRows, mGridView.mNumRows, mScroller.getScroll(), mScroller.getLastScroll());
		}
	}

	@SuppressLint("WrongCall")
	@Override
	public void draw(GLCanvas canvas) {
//		updateCurVisibleRow();
		mScroller.onDraw(canvas);
		canvas.save();
		if (mNeedClip) {
			canvas.clipRect(0, getScrollY(), mGridView.getWidth(),
					getScrollY() + mGridView.getHeight()); // 裁剪画布
		}
		final long drawingTime = mGridView.getDrawingTime();
		int i = 0;
		for (i = mFVisiableRow; i <= mLVisiableRow && i < mTotalRows; i++) {
			drawRow(canvas, i, drawingTime);
		}
		
		// 竖向循环模式下绘制假图标
		if (mTotalRows > mGridView.mNumRows && mCycleMode) {
			int scroll = mScroller.getScroll();
			int lastScroll = mScroller.getLastScroll();
			if (scroll > lastScroll) { // 拉到底部时在底部绘制顶部的假图标
				int drawRowCounts = (scroll - lastScroll - mFakeIconMargin) / mRowHeight;
				canvas.translate(0, mTotalHeight + mFakeIconMargin + mGridView.getPaddingTop());
				for (i = 0; i <= drawRowCounts && i < mTotalRows; i++) {
					drawRow(canvas, i, drawingTime);
				}
			} else if (scroll < 0) { // 拉到顶部时在顶部绘制底部的假图标
				int drawRowCounts = (Math.abs(scroll) - mFakeIconMargin) / mRowHeight + 1; // 需要多画一行
				canvas.translate(0, -mTotalHeight - mFakeIconMargin - mGridView.getPaddingTop());
				for (i = mTotalRows - 1; i >= mTotalRows - 1 - drawRowCounts && i >= 0; i--) {
					drawRow(canvas, i, drawingTime);
				}
			}
		}
		
		canvas.restore(); // 还原偏移量
	}
	
	/**
	 * 绘制瀑布特效部分
	 * @param canvas
	 */
	@Override
	public void drawWaterFallEffector(GLCanvas canvas , int drawPart) {
		int scroll = mScroller.getScroll();
		int lastScroll = mScroller.getLastScroll();
		int topDrawRowCounts = 0;
		int bottomDrawRowCounts = 0;
		int i = 0;
		final long drawingTime = mGridView.getDrawingTime();
		float angleTranslate = ShellWaterFallEffector.getWaterAngleTranslate();
		if (scroll >= 0 || scroll <= lastScroll) { // 画正常部分瀑布特效图标
			if (drawPart == ShellVerticalListContainer.PART_UP) {
				topDrawRowCounts = /*(int) (mTopContainerHeight * angleTranslate / mRowHeight) +*/ mGridView.getNumRows();
				for (i = mFVisiableRow + 1; i > mFVisiableRow - topDrawRowCounts && i >= 0; i--) { // 画顶部
					drawRow(canvas, i, drawingTime);
				}
			} else if (drawPart == ShellVerticalListContainer.PART_DOWN) {
				bottomDrawRowCounts = /*(int) (mBottomContainerHeight * angleTranslate / mRowHeight) +*/ mGridView.getNumRows();
			    for (i = mLVisiableRow - 1; i < mLVisiableRow + bottomDrawRowCounts && i < mTotalRows; i++) { // 画底部
					drawRow(canvas, i, drawingTime);
			    }
			}
		}
		
		if (mCycleMode && mTotalRows > mGridView.getNumRows()) { // 循环模式下绘制假图标
			int topDistance = (int) ((mBottomContainerHeight + mGridView.getPaddingTop() + mFakeIconMargin) * angleTranslate);
			int bottomDistance = (int) ((mTopContainerHeight + mGridView.getPaddingTop() + mFakeIconMargin) * angleTranslate);
			if (drawPart == ShellVerticalListContainer.PART_UP && scroll < mActualHeight) { // 顶部画假图标
 				topDrawRowCounts = (topDistance - scroll) / mRowHeight + 1;
 				canvas.save();
				canvas.translate(0, -mTotalHeight - mFakeIconMargin - mGridView.getPaddingTop());
				for (i = mTotalRows - 1; i >= mTotalRows - 1 - topDrawRowCounts && i >= 0; i--) {
					drawRow(canvas, i, drawingTime);
				}
				canvas.restore();
			} else if (drawPart == ShellVerticalListContainer.PART_DOWN && scroll > lastScroll - mActualHeight) { // 底部画假图标
				int drawRowCounts = (scroll - (lastScroll - bottomDistance)) / mRowHeight;
				canvas.save();
				canvas.translate(0, mTotalHeight + mFakeIconMargin + mGridView.getPaddingTop());
				for (i = 0; i <= drawRowCounts && i < mTotalRows; i++) {
					drawRow(canvas, i, drawingTime);
				}
				canvas.restore();
			}
		}
	}
	
	/**
	 * 绘制一行图标
	 * @param rowNum
	 */
	private void drawRow(GLCanvas canvas, int rowNum, long drawingTime) {
		ArrayList<GLView> list = mHoldRow.get(rowNum);
		if (list != null) {
			for (GLView view : list) {
				if (view != null && view.isVisible()) {
					mGridView.drawChild(canvas, view, drawingTime);
				}
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, int action) {
		if (mScroller.onTouchEvent(event, action)) {
			if (mCycleMode) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN :
						mTouchDownY = (int) event.getY(0);
						break;
					case MotionEvent.ACTION_UP :
					case MotionEvent.ACTION_CANCEL :
						doCycleScroll((int) event.getY(0));
						break;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isScrollFinished() {
		return mScroller.isFinished();
	}

	@Override
	public void setPadding(float paddingFactor) {
		mScroller.setPaddingFactor(paddingFactor);
	}

	@Override
	public GLView pointToView(MotionEvent event) {
		final float xf = event.getX();
		final float yf = event.getY();

		int x = (int) xf;
		int y = (int) yf;

		if (mGridView.getChildCount() > 0) {
			Rect frame = new Rect();
			y += mScroller.getScroll();

			for (int i = mLVisiableRow; i >= mFVisiableRow; i--) {
				mHoldRow.get(i);
				ArrayList<GLView> list = mHoldRow.get(i);
				if (list != null) {
					int size = list.size();
					for (int j = size - 1; j >= 0; j--) {
						GLView child = list.get(j);
						if (child != null && child.isVisible() && child.getGLParent() != null) {
							child.getHitRect(frame);
							if (frame.contains(x, y)) {
								event.setLocation(x - child.getLeft(), y - child.getTop());
								boolean result = child.dispatchTouchEvent(event);
								event.setLocation(xf, yf);
								child.setTag(GLScrollableBaseGrid.SACCEPTEVENT, result);
								return child;
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void setScreenSize(int width, int height) {
	}

	@Override
	public void computeScrollOffset() {
		mScroller.computeScrollOffset();
	}

	@Override
	public void clickIndicatorItem(int index) {
	}

	@Override
	public void sliding(float percent) {
	}

	@Override
	public void updateIndicator(int total, int current) {
		if (mIndicator != null) { // 更新指示器
			mIndicator.setCurOffset(mScroller.getScroll());
		}
	}

	@Override
	public void onSlidingScreen(int type, Bundle bundle) {
	}

	@Override
	public void setIndicator(GLView indicator) {
		if (indicator instanceof VerticalIndicator) {
			mIndicator = (VerticalIndicator) indicator;
		} else {
			throw new IllegalArgumentException("Please set VerticalIndicator");
		}
	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		updateCurVisibleRow();
		updateIndicator(newScroll, mScroller.getLastScroll());
	}

	@Override
	public void invalidate() {
		mGridView.invalidate();		
	}

	@Override
	public void scrollBy(int x, int y) {
		mGridView.scrollBy(x, y);
	}

	@Override
	public int getScrollX() {
		return mGridView.getScrollX();
	}

	@Override
	public int getScrollY() {
		return mGridView.getScrollY();
	}

	@Override
	public ShellScroller getScroller() {
		return mScroller;
	}

	@Override
	public void setScroller(ShellScroller scroller) {
		mScroller = scroller;
	}
	

	@Override
	public void onScrollStart(int currentScroll) {
		mGridView.onScrollStart();
	}
	
	@Override
	public void onScrollFinish(int currentScroll) {
//		mGridView.setGLViewWrapperDeferredInvalidate(false);
		mGridView.onScrollFinish();
	}
	
	/**
	 * 满足条件则进行竖向循环滚动，返回true;不满足条件返回false
	 * 
	 * @return
	 */
	protected boolean doCycleScroll(int touchUpY) {
		if (mCycleMode && mTotalRows > mGridView.getNumRows()) {
			int threshold = 0;
			int screenWidth = GoLauncherActivityProxy.getScreenWidth();
			if ((GoLauncherActivityProxy.isPortait() && screenWidth <= 320)
					|| (!GoLauncherActivityProxy.isPortait() && screenWidth <= 480)) { // 小屏手机由于较难滑，缩小阀值
				threshold = DrawUtils.dip2px(10);
			} else {
				threshold = DrawUtils.dip2px(30);
			}
			int scroll = mScroller.getScroll();
			int lastScroll = mScroller.getLastScroll();
			if ((touchUpY < mTouchDownY) && scroll > lastScroll + threshold) { // 超过底部条件判断
				if (mScroller instanceof ShellCycloidScroller) {
					((ShellCycloidScroller) mScroller).scrollWithCycle(mFakeIconMargin
							+ mActualHeight - (mScroller.getScroll() - mScroller.getLastScroll() - mGridView.getPaddingTop()));
					return true;
				}
			} else if ((touchUpY > mTouchDownY) && scroll < -threshold) { // 超过顶部条件判断
				if (mScroller instanceof ShellCycloidScroller) {
					((ShellCycloidScroller) mScroller).scrollWithCycle(-(mFakeIconMargin
							+ mActualHeight + mScroller.getScroll() + mGridView.getPaddingTop()));
					return true;
				}
			}
		}
		return false;
	}
	
	public void setVerticalEffect(int type) {
		if (mVerticalEffectorType == type) {
			return;
		}
		switch (type) {
			case NO_VERTICAL_EFFECTOR :
				mVerticalEffector = null;
				break;
			case WATERFALL_VERTICAL_EFFECTOR :
				mVerticalEffector = new ShellWaterFallEffector(mContext);
				break;
			default :
				return;
		}
		mVerticalEffectorType = type;
		mScroller.setEffector(mVerticalEffector);
	}
	
	public int getVerticalEffect() {
		return mVerticalEffectorType;
	}

	@Override
	public int getCurFirstVisiableRow() {
		int ret = (getScrollY() - mGridView.getPaddingTop()) / mRowHeight;
		if (ret < 0) {
			ret = 0;
		}
		return ret;
	}
	

	@Override
	public int getCurLastVisiableRow() {
		return mLVisiableRow;
	}

	@Override
	public void onScrollLeft() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScrollRight() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScrollTop() {
		int curLoacation = mScroller.getScroll();
		if (curLoacation >= 0) {
			mScroller.setScroll(curLoacation - 3);
		}
	}

	@Override
	public void onScrollBottom() {
		int curLoacation = mScroller.getScroll();
		if (curLoacation <= mScroller.getLastScroll()) {
			mScroller.setScroll(curLoacation + 3);
		}
	}

	@Override
	public void onPostScrollRunnable(int direction) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int getScrollType() {
		return DragScroller.SCROLL_TYPE_VERTICAL;
	}

	@Override
	public Rect getScrollLeftRect() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rect getScrollRightRect() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rect getScrollTopRect() {
		Rect rect = new Rect();
		int[] loc = mGridView.getLocationInDragLayer();
		rect.left = loc[0] + mGridView.getLeft();
		rect.top = loc[1] + mGridView.getTop();
		rect.right = loc[0] + mGridView.getRight();
		rect.bottom = rect.top + mScrollZoneSize;
		return rect;
	}

	@Override
	public Rect getScrollBottomRect() {
		Rect rect = new Rect();
		int[] loc = mGridView.getLocationInDragLayer();
		rect.left = loc[0] + mGridView.getLeft();
		rect.top = loc[1] + mGridView.getBottom() - mScrollZoneSize;
		rect.right = loc[0] + mGridView.getRight();
		rect.bottom = loc[1] + mGridView.getBottom();
		return rect;
	}
	
	@Override
	public int getScrollDelay() {
		return DragScroller.SCROLL_DELAY_VERTICAL;
	}

	@Override
	public int getNextScrollDelay() {
		return DragScroller.NEXT_SCROLL_DELAY_VERTICAL;
	}

	@Override
	public void layoutChildren(int firstIndex, int lastIndex) {
		int firstRow = firstIndex / mGridView.mNumColumns;
		int lastRow = lastIndex / mGridView.mNumColumns;
		int pos = firstIndex;
		for (int rowNum = firstRow; rowNum <= lastRow; rowNum++) {
			if (rowNum < 0 || rowNum > mTotalRows - 1 || null == mGridView.mAdapter) {
				return;
			}
			final int paddingTop = mGridView.getPaddingTop();
			final int paddingLeft = mGridView.getPaddingLeft();
			final int paddingRight = mGridView.getPaddingRight();
			final int actualWidth = mGridView.getWidth() - paddingLeft - paddingRight;
			final int columnWidth = actualWidth / mGridView.mNumColumns;
			final int rowSpacing = rowNum * mRowHeight;
			
			int x = paddingLeft;
			int y = paddingTop + rowSpacing;
			if (rowNum == 0) {
				y += mHorizontalSpacing;
			}
			ArrayList<GLView> holder = mHoldRow.get(rowNum);
			int count = mGridView.mAdapter.getCount();
			int startColumn = 0;
			int lastColumn = mGridView.mNumColumns - 1;
			if (rowNum == firstRow) {
				startColumn = firstIndex % mGridView.mNumColumns;
			}
			if (rowNum == lastRow) {
				lastColumn = lastIndex % mGridView.mNumColumns;
			}
			for (int i = startColumn; i <= lastColumn; i++) {
				if (pos < count) {
					int left = x + i * columnWidth;
					GLView child;
					child = mGridView.getViewAtPosition(pos);
					if (child != holder.get(pos % mGridView.mNumColumns)) {
						child.offsetLeftAndRight(left - child.getLeft());
						child.offsetTopAndBottom(y - child.getTop());
						holder.set(pos % mGridView.mNumColumns, child);
					}
					mGridView.attachViewToParent(child, pos, child.getLayoutParams());
					pos++;
					mGridView.callBackToChild(child);
				}
			}
//			mHoldRow.put(rowNum, holder);
		}
	}
	
	@Override
	public void resetOrientation() {
		mScroller.setScroll(0);
		scrollBy(-getScrollX(), -getScrollY());
	}
	
	@Override
	public void scrollToFirst() {
		if (mScroller.getScroll() == 0) {
			return;
		}
		mScroller.setScroll(mScroller.getScroll() - 10);
		mScroller.flingByScroll(-(3 * mScroller.getScroll()));
	}
	
	@Override
	public void clearHolder() {
		int size = mHoldRow.size();
		for (int i = 0; i < size; i++) {
			ArrayList<GLView> viewList = mHoldRow.valueAt(i);
			viewList.clear();
		}
		mHoldRow.clear();
	}
	
	/**
	 * 获得相应板块的View
	 * @param screen
	 * @return
	 */
	@Override
	public ArrayList<GLView> getChildren(int index) {
		return mHoldRow.get(index);
	}
	
	@Override
	public void setViewInHolder(int index, GLView view) {
		int row = index / mGridView.mNumColumns;
		int location = index % mGridView.mNumColumns;
		ArrayList<GLView> views = mHoldRow.get(row);
		if (views != null) {
			if (location >= 0 && location < views.size()) {
				views.set(location, view);
			}
		}
	}
	
	@Override
	public void removeViewInHolder(int index) {
		int row = index / mGridView.mNumColumns;
		int location = index % mGridView.mNumColumns;
		ArrayList<GLView> views = mHoldRow.get(row);
		if (views != null) {
			if (location >= 0 && location < views.size()) {
				views.remove(location);
			}
		}
	}
	
	@Override
	public void addViewInHolder(GLView view) {
		ArrayList<GLView> lastRow = mHoldRow.get(mTotalRows - 1);
		if (lastRow.size() < mGridView.mNumColumns) {
			lastRow.add(view);
		} else {
			ArrayList<GLView> newLastRow = new ArrayList<GLView>();
			newLastRow.add(view);
			mHoldRow.put(mTotalRows, newLastRow);
		}
		updateLayoutParams();
	}

	
	@Override
	public int getPositionOfChild(GLView view) {
		if (mGridView.indexOfChild(view) > -1) {
			int size = mHoldRow.size();
			for (int i = 0; i < size; i++) {
				ArrayList<GLView> row = mHoldRow.valueAt(i);
				if (row.contains(view)) {
					return i;
				}
			}
		}
		return -1;
	}
	
	@Override
	public int getIndexOfChild(GLView view) {
		if (mGridView.indexOfChild(view) > -1) {
			int size = mHoldRow.size();
			for (int i = 0; i < size; i++) {
				ArrayList<GLView> row = mHoldRow.valueAt(i);
				int index = row.indexOf(view);
				if (index != -1) {
					return index;
				}
			}
		}
		return -1;
	}
	
	@Override
	public void scrollTo(int row, boolean needAnimation) {
		ArrayList<GLView> list = mHoldRow.get(row);
		int scroll = mScroller.getScroll();
		int lastScroll = mScroller.getLastScroll();
		if (list != null) {
			GLView view = list.get(0);
			int moveScroll = view.getTop() - scroll;
			if (moveScroll > 0 && scroll == lastScroll) { // 在最下方还要继续向下，直接跳出
				return;
			}
			if (needAnimation) {
				if (moveScroll < 0 && scroll == lastScroll) { // 从底部向上滚
					mScroller.setScroll(scroll - 10);
				}
				mScroller.flingByScroll(moveScroll);
			} else {
				if (scroll + moveScroll > lastScroll) {
					mScroller.setScroll(lastScroll);
				} else {
					mScroller.setScroll(moveScroll);
				}
			}
		}
	}
	
	@Override
	public int getChildCount() {
		return mHoldRow.size();
	}
	
	@Override
	public boolean isCircular() {
		return mScroller.isCircular();
	}

	@Override
	public boolean resetScrollState() {
		if (mScroller.getScroll() < 0) {
			mScroller.setScroll(0);
			return true;
		} else if (mScroller.getScroll() > mScroller.getLastScroll()) {
			mScroller.setScroll(mScroller.getLastScroll());
			return true;
		}
		return false;
	}
}
