package com.jiubang.shell.common.component;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;

import com.go.gl.animation.Animation;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.util.graphics.effector.united.IEffectorIds;
import com.jiubang.shell.appdrawer.GLAppDrawerMainView;
import com.jiubang.shell.appdrawer.component.GLAppDrawerBaseGrid;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.drag.DragScroller;
import com.jiubang.shell.indicator.DesktopIndicator;
import com.jiubang.shell.scroller.ShellScreenScroller;
import com.jiubang.shell.scroller.ShellScreenScrollerEffector;
import com.jiubang.shell.scroller.ShellScreenScrollerListener;
import com.jiubang.shell.scroller.effector.CoupleScreenEffector;
import com.jiubang.shell.scroller.effector.gridscreen.GridScreenContainer;
import com.jiubang.shell.scroller.effector.subscreen.CustomSubScreenContainer;

/**
 * 
 * @author yangguanxiang
 *
 */
public abstract class HorScrollableGridViewHandler extends AbsScrollableGridViewHandler
		implements
			ShellScreenScrollerListener,
			CustomSubScreenContainer,
			GridScreenContainer {

	private ShellScreenScroller mScreenScroller;
	private SparseArray<ArrayList<GLView>> mHoldScreen = new SparseArray<ArrayList<GLView>>();
	protected int mTotalScreens;
	protected int mCurrentScreen;
	private ShellScreenScrollerEffector mScreenEffector;
	private DesktopIndicator mIndicator;
	private int mPlaceType;
	private boolean mEnableScrollEffect;
	private int mFVisiableRow;
	//	private int mLVisiableRow;
	//	private int mOffset;
	private int mOrientation = ShellScreenScroller.HORIZONTAL;
	/**
	 * 循环模式
	 */
	private boolean mCycleMode = false;

	public HorScrollableGridViewHandler(Context context, GLScrollableBaseGrid view, int placeType,
			boolean verticalSlide, boolean enableScrollEffect) {

		super(context, view);
		mPlaceType = placeType;
		mEnableScrollEffect = enableScrollEffect;
		init(verticalSlide);
	}

	private void init(boolean verticalSlide) {
		mScreenScroller = new ShellScreenScroller(mContext, this);
		mScreenScroller.setDuration(350); // CHECKSTYLE IGNORE
		initEffector(verticalSlide);
	}

	private void initEffector(boolean verticalSlide) {
		mScreenScroller.setBackgroundAlwaysDrawn(true);
		mScreenScroller.setMaxOvershootPercent(0);
		if (mEnableScrollEffect) {
			mScreenEffector = new CoupleScreenEffector(mScreenScroller, mPlaceType);
			mScreenScroller.setDepthEnabled(true);
			mScreenScroller.setEffector(mScreenEffector);
			// 设置特效是否支持上下角度的滑动，这句必须在setCycleScreenMode之后调用，因为setCycleScreenMode会重新new一个新的screenScroller
			mScreenEffector.setVerticalSlide(verticalSlide);
		}
	}

	public void setEffectType(int type) {
		mScreenEffector.setType(type);
	}

	public void setEffectType(int[] type) {
		if (mScreenEffector instanceof CoupleScreenEffector) {
			((CoupleScreenEffector) mScreenEffector).setAppIconCustomRandomEffects(type);
			mScreenEffector.setType(IEffectorIds.EFFECTOR_TYPE_RANDOM_CUSTOM);
		}
	}

	@Override
	public void setCycleScreenMode(boolean mode) {
		if (mScreenScroller != null && mCycleMode != mode) {
			mCycleMode = mode;
			ShellScreenScroller.setCycleMode(this, mode);
			mScreenEffector.onAttachReserveEffector(this);
		}
	}

	@Override
	public void layoutChildren() {
		refreshPageCount();
		mFVisiableRow = mCurrentScreen * mGridView.mNumRows;
		clearHolder();
		for (int i = 0; i < mTotalScreens; i++) {
			makePage(i);
		}
		if (mScreenScroller.getScreenWidth() == 0) {
			mScreenScroller.setScreenSize(mGridView.getWidth(), mGridView.getHeight());
		}
		if (mScreenEffector != null) {
			mScreenEffector.notifyRegetScreenRect();
		}
	}

	private void refreshPageCount() {
		int itemScreen = mGridView.mNumColumns * mGridView.mNumRows;
		mTotalScreens = (mGridView.getItemCount() + (itemScreen - 1)) / itemScreen;
		if (0 == mTotalScreens) {
			mTotalScreens = 1;
		}
		mScreenScroller.setScreenCount(mTotalScreens);
		if (mCurrentScreen >= mTotalScreens) {
			mScreenScroller.setCurrentScreen(mTotalScreens - 1);
		}
		mCurrentScreen = mScreenScroller.getCurrentScreen();
		updateIndicator(mTotalScreens, mCurrentScreen);
	}

	private void makePage(int pageNum) {
		if (pageNum < 0 || pageNum > mTotalScreens - 1 || null == mGridView.mAdapter) {
			return;
		}
		final int pageSpacing = isVertical() ? pageNum * mGridView.getHeight() : pageNum
				* mGridView.getWidth();
		final int startPos = pageNum * mGridView.mNumColumns * mGridView.mNumRows;
		final int paddingTop = mGridView.getPaddingTop();
		final int paddingBottom = mGridView.getPaddingBottom();
		final int paddingLeft = mGridView.getPaddingLeft();
		final int paddingRight = mGridView.getPaddingRight();
		final int actualWidth = mGridView.getWidth() - paddingLeft - paddingRight - (mGridView.mNumColumns - 1) * mVerticalSpacing;
		final int actualHeight = mGridView.getHeight() - paddingTop - paddingBottom - (mGridView.mNumRows - 1) * mHorizontalSpacing;
		final int columnWidth = actualWidth / mGridView.mNumColumns;
		final int rowHeight = actualHeight / mGridView.mNumRows;

		GLScrollableBaseGrid.LayoutParams p = new GLScrollableBaseGrid.LayoutParams(
				GLScrollableBaseGrid.LayoutParams.MATCH_PARENT,
				GLScrollableBaseGrid.LayoutParams.MATCH_PARENT);
		int pos = startPos;
		int x = isVertical() ? paddingLeft : paddingLeft + pageSpacing;
		int y = isVertical() ? paddingTop + pageSpacing : paddingTop;
		ArrayList<GLView> holder = new ArrayList<GLView>();
		int count = mGridView.mAdapter.getCount();
		for (int i = 0; i < mGridView.mNumRows; i++) {
			for (int j = 0; j < mGridView.mNumColumns; j++) {
				if (pos < count) {
					GLView child;
					child = mGridView.obtainView(pos);
					child.setLayoutParams(p);
					if (child.isPressed()) {
						child.setPressed(false);
					}
					mGridView.addViewInLayout(child, pos, p, true);
					holder.add(child);
					mesureChildInLayout(columnWidth, rowHeight, p, child);
					int left = x;
					int top = y;
					int w = columnWidth;
					int h = rowHeight;
					child.layout(left, top, left + w, top + h);
					pos++;
					x += columnWidth + mVerticalSpacing;
					mGridView.callBackToChild(child);
				}
			}
			x = isVertical() ? paddingLeft : paddingLeft + pageSpacing;
			y += rowHeight + mHorizontalSpacing;
		}

		mHoldScreen.put(pageNum, holder);
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
	

	@SuppressLint("WrongCall")
	@Override
	public void draw(GLCanvas canvas) {
		mScreenScroller.invalidateScroll();

		if (mEnableScrollEffect) {
			if (!mScreenScroller.isFinished()) {
				if (mNeedClip) {
					clipCanvas(canvas);
				}
				mScreenScroller.onDraw(canvas);
			} else {
				// 只画当前屏幕
				long drawingTime = mGridView.getDrawingTime();
				ArrayList<GLView> children = mHoldScreen.get(mCurrentScreen);
				if (children != null && !children.isEmpty()) {
					for (GLView child : children) {
						if (child.isVisible()) {
							mGridView.drawChild(canvas, child, drawingTime);
						}
					}
				}
			}
		} else {
			long drawingTime = mGridView.getDrawingTime();
			int size = mHoldScreen.size();
			for (int i = 0; i < size; i++) {
				ArrayList<GLView> children = mHoldScreen.valueAt(i);
				if (children != null && !children.isEmpty()) {
					for (GLView child : children) {
						if (child.isVisible()) {
							mGridView.drawChild(canvas, child, drawingTime);
						}
					}
				}
			}
		}
	}

	protected void clipCanvas(GLCanvas canvas) {
		if (isVertical()) {
			canvas.clipRect(0, getScrollY(), mGridView.getWidth(),
					getScrollY() + mScreenScroller.getScreenHeight());
		} else {
			canvas.clipRect(getScrollX(), 0, getScrollX() + mScreenScroller.getScreenWidth(),
					mGridView.getHeight());
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, int action) {
		return mScreenScroller.onTouchEvent(event, action);
	}

	@Override
	public boolean isScrollFinished() {
		return mScreenScroller.isFinished();
	}

	@Override
	public GLView pointToView(final MotionEvent ev) {

		final float xf = ev.getX();
		final float yf = ev.getY();

		int x = (int) xf;
		int y = (int) yf;

		if (mGridView.getChildCount() > 0) {
			Rect frame = new Rect();
			final ArrayList<GLView> h = mHoldScreen.get(mCurrentScreen);

			if (isVertical()) {
				y += mGridView.getHeight() * mCurrentScreen;
			} else {
				x += mGridView.getWidth() * mCurrentScreen;
			}

			// fix possible nullPointerException when flinging too fast
			if (h != null) {
				int size = h.size();
				for (int i = size - 1; i >= 0; i--) {
					final GLView child = h.get(i);
					if (child != null && child.isVisible() && child.getGLParent() != null) {
						child.getHitRect(frame);
						if (frame.contains(x, y)) {
							ev.setLocation(x - child.getLeft(), y - child.getTop());
							boolean result = child.dispatchTouchEvent(ev);
							ev.setLocation(xf, yf);
							child.setTag(GLScrollableBaseGrid.SACCEPTEVENT, result);
							return child;
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void setPadding(float paddingFactor) {
		mScreenScroller.setPadding(paddingFactor);
	}

	@Override
	public ShellScreenScroller getScreenScroller() {
		return mScreenScroller;
	}

	@Override
	public void setScreenScroller(ShellScreenScroller scroller) {
		mScreenScroller = scroller;
	}

	@Override
	public void onFlingIntercepted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStart() {
		mGridView.onScrollStart();
	}

	@Override
	public void onFlingStart() {
		//		mGridView.setGLViewWrapperDeferredInvalidate(true);
	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		final int offset = mScreenScroller.getIndicatorOffset();
		Bundle dataBundle = new Bundle();
		dataBundle.putInt(DesktopIndicator.OFFSET, offset);
		onSlidingScreen(DesktopIndicator.UPDATE_SLIDER_INDICATOR, dataBundle);
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		mCurrentScreen = newScreen;
		mFVisiableRow = mCurrentScreen * mGridView.mNumRows;
		Bundle dataBundle = new Bundle();
		dataBundle.putInt(DesktopIndicator.CURRENT, newScreen);
		onSlidingScreen(DesktopIndicator.UPDATE_DOTS_INDICATOR, dataBundle);
		mGridView.onScreenChange(newScreen, oldScreen);
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		mCurrentScreen = currentScreen;
		mFVisiableRow = mCurrentScreen * mGridView.mNumRows;
		//		mGridView.setGLViewWrapperDeferredInvalidate(false);
		mGridView.onScrollFinish();
	}

	@Override
	public void invalidate() {
		mGridView.invalidate();
	}

	@Override
	public void scrollBy(int x, int y) {
		mGridView.scrollBy(x, y);
		//		mOffset = (int) (mScreenScroller.getProgress()
		//				* (isVertical() ? mScreenScroller.getScreenHeight() : mScreenScroller
		//						.getScreenWidth()) * mTotalScreens);
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
	public void setScreenSize(int width, int height) {
		mScreenScroller.setScreenSize(width, height);
	}

	@Override
	public void computeScrollOffset() {
		mScreenScroller.computeScrollOffset();
	}

	@Override
	public void drawScreenCell(GLCanvas canvas, int screen, int index) {
		final long drawingTime = mGridView.getDrawingTime();
		final ArrayList<GLView> views = mHoldScreen.get(screen);
		int position = index - screen * mGridView.mNumColumns * mGridView.mNumRows;
		if (views != null && views.size() > position) {
			GLView view = views.get(position);
			if (view.isVisible()) {
				mGridView.drawChild(canvas, view, drawingTime);
			}
		}
	}

	@Override
	public void drawScreenCell(GLCanvas canvas, int screen, int index, int alpha) {
		final long drawingTime = mGridView.getDrawingTime();
		final ArrayList<GLView> views = mHoldScreen.get(screen);
		int position = index - screen * mGridView.mNumColumns * mGridView.mNumRows;
		if (views != null && views.size() > position) {
			GLView view = views.get(position);
			if (view.isVisible()) {
				int oldAlpha = canvas.getAlpha();
				canvas.setCullFaceEnabled(false);
				canvas.setAlpha(alpha);
				mGridView.drawChild(canvas, view, drawingTime);
				canvas.setAlpha(oldAlpha);
				canvas.setCullFaceEnabled(true);
			}
		}
	}

	@Override
	public int getCellRow() {
		return mGridView.mNumRows;
	}

	@Override
	public int getCellCol() {
		return mGridView.mNumColumns;
	}

	@Override
	public int getCellCount() {
		if (mGridView.mAdapter != null) {
			return mGridView.mAdapter.getCount();
		} else {
			return mGridView.getChildCount();
		}
	}

	@Override
	public int getCellWidth() {
		if (mGridView.mAdapter != null && mGridView.mAdapter.getCount() > 0) {
			GLView view = mGridView.getViewAtPosition(0);
			if (view != null) {
				return view.getWidth();
			}
		}
		return 0;
	}

	@Override
	public int getCellHeight() {
		if (mGridView.mAdapter != null && mGridView.mAdapter.getCount() > 0) {
			GLView view = mGridView.getViewAtPosition(0);
			if (view != null) {
				return view.getHeight();
			}
		}
		return 0;
	}

	@Override
	public int getWidth() {
		return mGridView.getWidth();
	}

	@Override
	public int getHeight() {
		return mGridView.getHeight();
	}

	@Override
	public int getPaddingLeft() {
		return mGridView.getPaddingLeft();
	}

	@Override
	public int getPaddingRight() {
		return mGridView.getPaddingRight();
	}

	@Override
	public int getPaddingTop() {
		return mGridView.getPaddingTop();
	}

	@Override
	public int getPaddingBottom() {
		return mGridView.getPaddingBottom();
	}

	@Override
	public void drawScreenBackground(GLCanvas canvas, int screen) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawScreen(GLCanvas canvas, int screen) {
		final long drawingTime = mGridView.getDrawingTime();
		final int offset = (isVertical() ? mScreenScroller.getScreenHeight() : mScreenScroller
				.getScreenWidth()) * screen;
		if (isVertical()) {
			canvas.translate(0, -offset);
		} else {
			canvas.translate(-offset, 0);
		}
		final ArrayList<GLView> views = mHoldScreen.get(screen);
		if (null == views) {
			return;
		}
		for (int i = 0; i < views.size(); i++) {
			GLView view = views.get(i);
			if (view.isVisible()) {
				mGridView.drawChild(canvas, view, drawingTime);
			}
		}
	}

	@Override
	public void drawScreen(GLCanvas canvas, int screen, int alpha) {
		final long drawingTime = mGridView.getDrawingTime();
		final int offset = (isVertical() ? mScreenScroller.getScreenHeight() : mScreenScroller
				.getScreenWidth()) * screen;
		if (isVertical()) {
			canvas.translate(0, -offset);
		} else {
			canvas.translate(-offset, 0);
		}

		final ArrayList<GLView> views = mHoldScreen.get(screen);
		if (null == views) {
			return;
		}

		final int oldAlpha = canvas.getAlpha();
		canvas.setAlpha(alpha);
		for (int i = 0; i < views.size(); i++) {
			GLView view = views.get(i);
			if (view.isVisible()) {
				mGridView.drawChild(canvas, view, drawingTime);
			}
		}
		/*drawSortView(canvas, drawingTime, views);*/
		canvas.setAlpha(oldAlpha);
	}

	@Override
	public void updateIndicator(final int total, final int current) {
		if (mIndicator != null && total >= 0 && current >= 0 && current < total) {
			mGridView.post(new Runnable() {

				@Override
				public void run() {
					mIndicator.setTotal(total);
					mIndicator.setCurrent(current);
				}
			});
		}
	}

	@Override
	public void onSlidingScreen(int type, Bundle bundle) {
		if (mIndicator != null) {
			mIndicator.updateIndicator(type, bundle);
		}
	}

	@Override
	public void clickIndicatorItem(int index) {
		if (index >= 0 && index < mTotalScreens) {
			scrollTo(index, true);
		}
	}

	@Override
	public void sliding(float percent) {
		if (0 <= percent && percent <= 100) {
			mScreenScroller.setScrollPercent(percent);
		}
	}

	@Override
	public void setIndicator(GLView indicator) {
		if (indicator instanceof DesktopIndicator) {
			mIndicator = (DesktopIndicator) indicator;
		} else {
			throw new IllegalArgumentException("Please set DesktopIndicator");
		}
	}

	@Override
	public int getCurFirstVisiableRow() {
		return mFVisiableRow;
	}

	@Override
	public void onScrollLeft() {
		mScreenScroller.gotoScreen(mCurrentScreen - 1, 450, true);
	}

	@Override
	public void onScrollRight() {
		mScreenScroller.gotoScreen(mCurrentScreen + 1, 450, true);
	}

	@Override
	public void onScrollTop() {
		mScreenScroller.gotoScreen(mCurrentScreen - 1, 450, true);
	}

	@Override
	public void onScrollBottom() {
		mScreenScroller.gotoScreen(mCurrentScreen + 1, 450, true);
	}

	@Override
	public void onPostScrollRunnable(int direction) {

	}

	@Override
	public int getScrollType() {
		return mOrientation == ShellScreenScroller.HORIZONTAL
				? DragScroller.SCROLL_TYPE_HORIZONTAL
				: DragScroller.SCROLL_TYPE_VERTICAL;
	}

	@Override
	public Rect getScrollLeftRect() {
		Rect rect = new Rect();
		int[] loc = mGridView.getLocationInDragLayer();
		rect.left = loc[0] + mGridView.getLeft();
		rect.top = loc[1] + mGridView.getTop();
		rect.right = rect.left + mScrollZoneSize;
		rect.bottom = loc[1] + mGridView.getBottom();
		return rect;
	}

	@Override
	public Rect getScrollRightRect() {
		Rect rect = new Rect();
		int[] loc = mGridView.getLocationInDragLayer();
		rect.left = loc[0] + mGridView.getRight() - mScrollZoneSize;
		rect.top = loc[1] + mGridView.getTop();
		rect.right = loc[0] + mGridView.getRight();
		rect.bottom = loc[1] + mGridView.getBottom();
		return rect;
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
		return DragScroller.SCROLL_DELAY_HORIZONTAL;
	}

	@Override
	public int getNextScrollDelay() {
		return DragScroller.NEXT_SCROLL_DELAY_HORIZONTAL;
	}

	/**
	 * 注：该方法暂不支持竖向滚屏模式
	 */
	@Override
	public void layoutChildren(int firstIndex, int lastIndex) {
		int pageCount = mGridView.mNumColumns * mGridView.mNumRows;
		int firstScreen = firstIndex / pageCount;
		int lastScreen = lastIndex / pageCount;

		final int paddingTop = mGridView.getPaddingTop();
		final int paddingBottom = mGridView.getPaddingBottom();
		final int paddingLeft = mGridView.getPaddingLeft();
		final int paddingRight = mGridView.getPaddingRight();
		final int actualWidth = mGridView.getWidth() - paddingLeft - paddingRight - (mGridView.mNumColumns - 1) * mVerticalSpacing;
		final int actualHeight = mGridView.getHeight() - paddingTop - paddingBottom - (mGridView.mNumRows - 1) * mHorizontalSpacing;
		final int columnWidth = actualWidth / mGridView.mNumColumns;
		final int rowHeight = actualHeight / mGridView.mNumRows;

		int pos = firstIndex;
		for (int pageNum = firstScreen; pageNum <= lastScreen; pageNum++) {
			if (pageNum < 0 || pageNum > mTotalScreens - 1 || null == mGridView.mAdapter) {
				return;
			}
			int startRow = 0;
			int endRow = mGridView.mNumRows - 1;
			if (pageNum == firstScreen) {
				startRow = firstIndex % pageCount / mGridView.mNumColumns;
			}
			if (pageNum == lastScreen) {
				endRow = lastIndex % pageCount / mGridView.mNumColumns;
			}
			final int pageSpacing = pageNum * mGridView.getWidth();
			ArrayList<GLView> holder = mHoldScreen.get(pageNum);
			if (holder == null) {
				return;
			}
			int count = mGridView.mAdapter.getCount();
			for (int i = startRow; i <= endRow; i++) {
				int y = paddingTop + i * rowHeight;
				int x = paddingLeft + pageSpacing;
				int startColumn = 0;
				int lastColumn = mGridView.mNumColumns - 1;
				if (i == startRow && pageNum == firstScreen) {
					startColumn = firstIndex % mGridView.mNumColumns;
				}
				if (i == endRow && pageNum == lastScreen) {
					lastColumn = lastIndex % mGridView.mNumColumns;
				}
				for (int j = startColumn; j <= lastColumn; j++) {
					if (pos < count) {
						GLView child;
//						child = mGridView.getViewAtPosition(pos);
						child = mGridView.obtainView(pos);
						int left = x + j * columnWidth;
						int top = y;
						if (pos % pageCount < holder.size()) {
							if (child != holder.get(pos % pageCount)) {
								child.offsetLeftAndRight(left - child.getLeft());
								child.offsetTopAndBottom(top - child.getTop());
								holder.set(pos % pageCount, child);
							}
						} else {
							holder.add(child);
						}
						mGridView.attachViewToParent(child, pos, child.getLayoutParams());
						mGridView.callBackToChild(child);
						pos++;
						x += mVerticalSpacing;
					}
				}
				y += mHorizontalSpacing;
			}
			mHoldScreen.put(pageNum, holder);
		}
	}

	public void setVerticalSlide(boolean verticalSlide) {
		mScreenEffector.setVerticalSlide(verticalSlide);
	}

	@Override
	public int getCurLastVisiableRow() {
		return mFVisiableRow + mGridView.mNumRows - 1;
	}

	public int getTotalScreen() {
		return mTotalScreens;
	}

	public int getCurrentScreen() {
		return mCurrentScreen;
	}

	public void setOrientation(int orientation) {
		mOrientation = orientation;
		mScreenScroller.setOrientation(orientation);
	}

	private boolean isVertical() {
		return mOrientation == ShellScreenScroller.VERTICAL;
	}

	/**
	 * 获得相应板块的View
	 * @param screen
	 * @return
	 */
	@Override
	public ArrayList<GLView> getChildren(int screen) {
		return mHoldScreen.get(screen);
	}

	@Override
	public void resetOrientation() {
		mScreenScroller.setCurrentScreen(0);
		scrollBy(-getScrollX(), -getScrollY());
	}

	@Override
	public void scrollToFirst() {
		mScreenScroller.gotoScreen(0, 450, true);
	}

	@Override
	public void clearHolder() {
		int size = mHoldScreen.size();
		for (int i = 0; i < size; i++) {
			ArrayList<GLView> viewList = mHoldScreen.valueAt(i);
			viewList.clear();
		}
		mHoldScreen.clear();
	}

	@Override
	public void setViewInHolder(int index, GLView view) {
		int pageCount = mGridView.mNumColumns * mGridView.mNumRows;
		int page = index / pageCount;
		int location = index % pageCount;
		ArrayList<GLView> views = mHoldScreen.get(page);
		if (views != null) {
			if (location >= 0 && location < views.size()) {
				views.set(location, view);
			}
		}
	}

	@Override
	public void removeViewInHolder(int index) {
		int pageCount = mGridView.mNumColumns * mGridView.mNumRows;
		int page = index / pageCount;
		int location = index % pageCount;
		ArrayList<GLView> views = mHoldScreen.get(page);
		if (views != null) {
			if (location >= 0 && location < views.size()) {
				views.remove(location);
			}
		}
	}

	@Override
	public void addViewInHolder(GLView view) {
		ArrayList<GLView> lastScreen = mHoldScreen.get(mTotalScreens - 1);
		int pageCount = mGridView.mNumColumns * mGridView.mNumRows;
		if (lastScreen.size() < pageCount) {
			lastScreen.add(view);
		} else {
			ArrayList<GLView> newLastScreen = new ArrayList<GLView>();
			newLastScreen.add(view);
			mHoldScreen.put(mTotalScreens, newLastScreen);
		}
		refreshPageCount();
	}

	/**
	 * Grid执行手势动画
	 */
	public void doSwipeAnimation(AnimationTask task, int type, int size) {
		ArrayList<GLView> holder = mHoldScreen.get(mCurrentScreen);
		if (mCurrentScreen < 0 || mCurrentScreen > mTotalScreens - 1 || null == mGridView.mAdapter
				|| holder == null || holder.size() < 1) {
			return;
		}

		int paddingTop = mGridView.getPaddingTop();
		int paddingBottom = mGridView.getPaddingBottom();
		int paddingLeft = mGridView.getPaddingLeft();
		int paddingRight = mGridView.getPaddingRight();

		if (GoLauncherActivityProxy.isPortait()) { // 竖屏
			if (mGridView instanceof GLAppDrawerBaseGrid) {
				GLAppDrawerBaseGrid appDrawerGrid = (GLAppDrawerBaseGrid) mGridView;
				int[] paddingExtend;
				if (type == GLAppDrawerMainView.ANIMATION_TOP_CONTAINER_SHOW
						|| type == GLAppDrawerMainView.ANIMATION_BOTTOM_CONTAINER_SHOW) { // Grid高度变小，宽度不变
					paddingExtend = appDrawerGrid.getPaddingExtends(mGridView.getHeight() - size,
							appDrawerGrid.getOriginalPaddingTop(),
							appDrawerGrid.getOriginalPaddingBottom());

				} else { // Grid高度变大，宽度不变
					paddingExtend = appDrawerGrid.getPaddingExtends(mGridView.getHeight() + size,
							appDrawerGrid.getOriginalPaddingTop(),
							appDrawerGrid.getOriginalPaddingBottom());
				}
				paddingTop = appDrawerGrid.getOriginalPaddingTop() + paddingExtend[0];
				paddingBottom = appDrawerGrid.getOriginalPaddingBottom() + paddingExtend[1];
			}
		}

		int actualWidth;
		int actualHeight;
		if (GoLauncherActivityProxy.isPortait()) { // 竖屏
			actualWidth = mGridView.getWidth() - paddingLeft - paddingRight;
			if (type == GLAppDrawerMainView.ANIMATION_TOP_CONTAINER_SHOW
					|| type == GLAppDrawerMainView.ANIMATION_BOTTOM_CONTAINER_SHOW) { // Grid高度变小，宽度不变
				actualHeight = mGridView.getHeight() - size - paddingTop - paddingBottom;
			} else { // Grid高度变大，宽度不变
				actualHeight = mGridView.getHeight() + size - paddingTop - paddingBottom;
			}

		} else { // 横屏
			actualHeight = mGridView.getHeight() - paddingTop - paddingBottom;
			if (type == GLAppDrawerMainView.ANIMATION_TOP_CONTAINER_SHOW
					|| type == GLAppDrawerMainView.ANIMATION_BOTTOM_CONTAINER_SHOW) { // Grid宽度变小，高度不变
				actualWidth = mGridView.getWidth() - size - paddingLeft - paddingRight;
			} else { // Grid宽度变大，高度不变
				actualWidth = mGridView.getWidth() + size - paddingLeft - paddingRight;
			}
		}
		final int pageSpacing = isVertical()
				? mCurrentScreen * mGridView.getHeight()
				: mCurrentScreen * mGridView.getWidth();
		final int screenCount = mGridView.mNumColumns * mGridView.mNumRows; // 一屏的图标数
		final int startPos = mCurrentScreen * screenCount;
		final int columnWidth = actualWidth / mGridView.mNumColumns;
		final int rowHeight = actualHeight / mGridView.mNumRows;
		int pos = startPos;
		int x = isVertical() ? paddingLeft : paddingLeft + pageSpacing;
		int y = isVertical() ? paddingTop + pageSpacing : paddingTop;
		int count = mGridView.mAdapter.getCount();
		int rectsSize = holder.size();
		Rect[] rects = new Rect[rectsSize]; // 按新高宽计算后的矩形数组

		for (int i = 0; i < mGridView.mNumRows; i++) {
			for (int j = 0; j < mGridView.mNumColumns; j++) {
				if (pos < count) {
					Rect rect = new Rect(x, y, x + columnWidth, y + rowHeight);
					rects[i * mGridView.mNumColumns + j] = rect;
					pos++;
					x += columnWidth;
				}
			}
			x = isVertical() ? paddingLeft : paddingLeft + pageSpacing;
			y += rowHeight;
		}

		Animation moveAnimation = null;
		pos = startPos;
		int temp = 0;
		if (type == GLAppDrawerMainView.ANIMATION_TOP_CONTAINER_SHOW) {
			temp = size;
		} else if (type == GLAppDrawerMainView.ANIMATION_TOP_CONTAINER_HIDE) {
			temp = -size;
		}

		int extendSize = 0; // 由于行距/列距扩大导致图标内部的偏移量大小（扩大的距离一半）
		if (GoLauncherActivityProxy.isPortait()) { // 竖屏
			GLView child = holder.get(0);
			Rect rect = rects[0];
			if (child != null && rect != null) {
				extendSize = ((rect.bottom - rect.top) - (child.getBottom() - child.getTop())) / 2;
			}
			for (int i = 0; i < rectsSize; i++) {
				child = holder.get(i);
				if (child != null && rects[i] != null) {
					moveAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, rects[i].top + temp
							- child.getTop() + extendSize);
					moveAnimation.setDuration(GLAppDrawerMainView.SWIPE_ANIMATION_DURATION);
					moveAnimation.setFillEnabled(true);
					moveAnimation.setFillBefore(false);
					task.addAnimation(child, moveAnimation, null);
				}
			}
		} else { // 横屏
			GLView child = holder.get(0);
			Rect rect = rects[0];
			if (child != null && rect != null) {
				extendSize = ((rect.right - rect.left) - (child.getRight() - child.getLeft())) / 2;
			}
			for (int i = 0; i < rectsSize; i++) {
				child = holder.get(i);
				if (child != null && rects[i] != null) {
					moveAnimation = new TranslateAnimation(0.0f, rects[i].left + temp
							- child.getLeft() + extendSize, 0.0f, 0.0f);
					moveAnimation.setDuration(GLAppDrawerMainView.SWIPE_ANIMATION_DURATION);
					moveAnimation.setFillEnabled(true);
					moveAnimation.setFillBefore(false);
					task.addAnimation(child, moveAnimation, null);
				}
			}
		}
	}

	@Override
	public int getPositionOfChild(GLView view) {
		if (mGridView.indexOfChild(view) > -1) {
			int size = mHoldScreen.size();
			for (int i = 0; i < size; i++) {
				ArrayList<GLView> screen = mHoldScreen.valueAt(i);
				if (screen.contains(view)) {
					return i;
				}
			}
		}
		return -1;
	}
	
	@Override
	public int getIndexOfChild(GLView view) {
		if (mGridView.indexOfChild(view) > -1) {
			int index = -1;
			int size = mHoldScreen.size();
			for (int i = 0; i < size; i++) {
				index = index + size;
				ArrayList<GLView> screen = mHoldScreen.valueAt(i);
				int indexInList = screen.indexOf(view);
				if (indexInList != -1) {
					return index + indexInList;
				}
			}
		}
		return -1;
	}

	@Override
	public void scrollTo(int screen, boolean needAnimation) {
		if (needAnimation) {
			mScreenScroller.gotoScreen(screen, -1, true);
		} else {
			mScreenScroller.setCurrentScreen(screen);
		}
	}

	@Override
	public int getChildCount() {
		return mHoldScreen.size();
	}

	@Override
	public boolean isCircular() {
		return mScreenScroller.isCircular();
	}

	@Override
	public void setAccFactor(float factor) {
		// TODO Auto-generated method stub
		if (mScreenScroller != null) {
			mScreenScroller.setAccFactor(factor);
		}
	}

	@Override
	public boolean resetScrollState() {
		if (!mScreenScroller.isFinished()) {
			mScreenScroller.gotoScreen(mCurrentScreen, 0, false);
			return true;
		}
		return false;
	}
	
	@Override
	public Rect getScreenRect() {
		Rect rect = new Rect();
		mGridView.getDrawingRect(rect);
		return rect;
	}
	
	@Override
	public void invalidateScreen() {
		mGridView.invalidate();
	}
	
	@Override
	public GLView getScreenView(int screen) {
		return null;
	}
}
