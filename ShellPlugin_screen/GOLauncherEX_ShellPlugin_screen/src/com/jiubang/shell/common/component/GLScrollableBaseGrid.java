package com.jiubang.shell.common.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLContentView;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.gl.widget.GLAdapterView.OnItemClickListener;
import com.go.gl.widget.GLAdapterView.OnItemLongClickListener;
import com.go.gl.widget.GLTextViewWrapper;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.common.listener.OnLayoutListener;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.indicator.DesktopIndicator;

/**
 * 
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author yangguanxiang
 */
@SuppressWarnings("rawtypes")
public abstract class GLScrollableBaseGrid extends GLAdapterView<ShellBaseAdapter>
		implements
			OnItemClickListener,
			OnItemLongClickListener {
	/**
	 * The adapter containing the data to be displayed by this view
	 */
	protected ShellBaseAdapter mAdapter;

	//	protected int mItemCount;

	//	protected boolean mIsPassToChildren = true;
	protected static final int SACCEPTEVENT = R.integer.app_icon_scale;

	private int mCheckTapPosition;

	public static final int TOUCH_STATE_DOWN = 3;
	public static final int TOUCH_STATE_TAP = 4;
	public static final int TOUCH_STATE_DONE_WAITING = 5;

	public final static int TOUCH_STATE_REST = 0;
	public final static int TOUCH_STATE_SCROLLING = 1;
	protected int mTouchState = TOUCH_STATE_REST;
	protected int mTouchSlop;

	static final int LAYOUT_NORMAL = 0;
	static final int LAYOUT_SCROLLING = 1;
	int mLayoutMode = LAYOUT_NORMAL;

	// 点中的View
	protected GLView mClickChild = null;
	/**
	 * Defines the selector's location and dimension at drawing time
	 */
	Rect mSelectorRect = new Rect();
	/**
	 * The selection's left padding
	 */
	int mSelectionLeftPadding = 0;

	/**
	 * The selection's top padding
	 */
	int mSelectionTopPadding = 0;

	/**
	 * The selection's right padding
	 */
	int mSelectionRightPadding = 0;

	/**
	 * The selection's bottom padding
	 */
	int mSelectionBottomPadding = 0;

	/**
	 * The last CheckForLongPress runnable we posted, if any
	 */
	private CheckForLongPress mPendingCheckForLongPress;

	/**
	 * The last CheckForTap runnable we posted, if any
	 */
	private Runnable mPendingCheckForTap;
	/**
	 * Acts upon click
	 */
	private GLScrollableBaseGrid.PerformClick mPerformClick;
	/**
	 * The data set used to store unused views that should be reused during the
	 * next layout to avoid creating new ones
	 */
	protected final RecycleBin mRecycler = new RecycleBin();

	private float mLastMotionX;
	private float mLastMotionY;

	protected int mNumColumns = 4;
	protected int mNumRows = 5;
	// private boolean mBlockLayouts;

	protected int mRowHeight = 0;
	protected int mColWidth = 0;
	//	protected int mScreenWidth;

	// 功能表用到的地方不用缓存
	// 1.每次都需要新建一个View，无论是否coverView是否为空，不然拖拽的时候就经常有问题
	// 2.功能表比较多逻辑，重复用两屏的缓存容易出现问题
	private boolean mNeedCache = false;

	private boolean mIsNeedDetectionVerticalSliding = false;
	private int mSlidingAngle = 30;

	/**
	 * 是否竖向滚动模式
	 */
	protected boolean mIsVerScroll;

	private OnLayoutListener mLayoutListener;

	public static final int UPDATEINDICATOR = 1001;

	protected AbsScrollableGridViewHandler mScrollableHandler;

	private boolean mPerformingLongPress;

	private int mHorizontalSpacing;

	private int mVerticalSpacing;

	public GLScrollableBaseGrid(Context context) {
		super(context);
		init();
	}

	public GLScrollableBaseGrid(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		init();
	}

	public GLScrollableBaseGrid(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		//		TypedArray a = context.obtainStyledAttributes(attrs,
		//				com_android_internal_R_styleable.GridView, defStyle, 0);
		//
		//		int numColumns = a.getInt(
		//				com_android_internal_R_styleable.GridView_numColumns, 4); // CHECKSTYLE IGNORE

		//		a.recycle();
		//
		//		a = context.obtainStyledAttributes(attrs, styleable.GLGridView,
		//				defStyle, 0);
		//
		//		int numRows = a.getInt(styleable.GLGridView_numRows, 4); // CHECKSTYLE IGNORE

		//		a.recycle();

		init();
	}

	private void init() {
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();

		setOnItemClickListener(this);
		setOnItemLongClickListener(this);
	}

	public void setCacheAble(boolean cache) {
		mNeedCache = cache;
	}

	public void setPadding(float paddingFactor) {
		mScrollableHandler.setPadding(paddingFactor);
	}

	/**
	 * 设置是否开启检测y轴方向切屏的方法,默认角度是30度
	 * 
	 * @param needDetection
	 */
	protected void setIsNeedDetectionVerticalSliding(boolean needDetection) {
		mIsNeedDetectionVerticalSliding = needDetection;
	}

	/**
	 * 设置是否开启检测y轴方向切屏的方法，第二个参数设置切屏的角度
	 * 
	 * @param needDetection
	 * @param angle
	 *            以x轴为立足轴，在angle角度内才会切屏
	 */
	protected void setIsNeedDetectionVerticalSliding(boolean needDetection, int angle) {
		mIsNeedDetectionVerticalSliding = needDetection;
		mSlidingAngle = angle;
	}

	public float calSlidingAngle(float deltaX, float deltaY) {
		float angle = 0;
		if (deltaX != 0 && deltaY != 0) {
			angle = (float) (Math.atan(deltaY / deltaX) * 180 / Math.PI);
		} else {
			if (deltaX == 0 && deltaY != 0) {
				angle = 90;
			}
		}
		return angle;
	}

	public void setNumRows(int numRows) {
		if (numRows != mNumRows) {
			mNumRows = numRows;
			// TODO:reLayout
			// requestLayoutIfNecessary();
		}
	}

	public void setNumColumns(int numColumns) {
		if (numColumns != mNumColumns) {
			mNumColumns = numColumns;
			// TODO:reLayout
			// requestLayoutIfNecessary();
		}
	}

	public int getNumColumns() {
		return mNumColumns;
	}

	public int getNumRows() {
		return mNumRows;
	}

	protected GLView obtainView(int position) {
		GLView scrapView = null;

		if (mNeedCache) {
			scrapView = mRecycler.getScrapView(position);
		}

		GLView child;
		synchronized (mAdapter) {
			if (scrapView != null) {
				child = mAdapter.getView(position, scrapView, this);

				if (child != scrapView) {
					mRecycler.addScrapView(scrapView);
				}
			} else {
				child = mAdapter.getView(position, null, this);
			}
		}
		return child;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		//		mScreenWidth = mWidth;
		if (mNeedCache) {
			final RecycleBin recycleBin = mRecycler;
			int size = getChildCount();
			for (int i = size - 1; i > 0; i--) {
				recycleBin.addScrapView(getChildAt(i));
			}
		}

		detachAllViewsFromParent();

		mScrollableHandler.layoutChildren();

		if (mLayoutListener != null) {
			mLayoutListener.onLayoutFinished(this);
		}
	}

	public void layoutPartPage(int firstIndex, int lastIndex) {
		if (firstIndex == lastIndex) {
			return;
		}
		int count = Math.abs(lastIndex - firstIndex);
		int start = Math.min(firstIndex, lastIndex);
		int end = Math.max(firstIndex, lastIndex);
		detachViewsFromParent(start, count + 1);
		mScrollableHandler.layoutChildren(start, end);
	}

	public abstract void callBackToChild(GLView view);

	@Override
	public void addView(GLView child, int index, android.view.ViewGroup.LayoutParams params) {
		child.setHapticFeedbackEnabled(false);
		super.addView(child, index, params);
	}

	public void setAdapter(ShellBaseAdapter adapter) {
		if (adapter instanceof OnLayoutListener) {
			setOnLayoutListener((OnLayoutListener) adapter);
		}
		mAdapter = adapter;
		mRecycler.clear();
		if (mAdapter != null) {
			if (mNeedCache) {
				mRecycler.setViewTypeCount(mAdapter.getViewTypeCount());
			}
		}
		requestLayout();
	}

	/**
	 * Sets the recycler listener to be notified whenever a View is set aside in
	 * the recycler for later reuse. This listener can be used to free resources
	 * associated to the View.
	 * 
	 * @param listener
	 *            The recycler listener to be notified of views set aside in the
	 *            recycler.
	 * 
	 * @see android.widget.AbsListView.RecycleBin
	 * @see android.widget.AbsListView.RecyclerListener
	 */
	public void setRecyclerListener(RecyclerListener listener) {
		mRecycler.mRecyclerListener = listener;
	}

	/**
	 * A RecyclerListener is used to receive a notification whenever a View is
	 * placed inside the RecycleBin's scrap heap. This listener is used to free
	 * resources associated to Views placed in the RecycleBin.
	 * 
	 * @see android.widget.AbsListView.RecycleBin
	 * @see android.widget.AbsListView#setRecyclerListener(android.widget.AbsListView.RecyclerListener)
	 */
	public static interface RecyclerListener {
		/**
		 * Indicates that the specified View was moved into the recycler's scrap
		 * heap. The view is not displayed on screen any more and any expensive
		 * resource associated with the view should be discarded.
		 * 
		 * @param view
		 */
		void onMovedToScrapHeap(GLView view);
	}

	/**
	 * The RecycleBin facilitates reuse of views across layouts. The RecycleBin
	 * has two levels of storage: ActiveViews and ScrapViews. ActiveViews are
	 * those views which were onscreen at the start of a layout. By
	 * construction, they are displaying current information. At the end of
	 * layout, all views in ActiveViews are demoted to ScrapViews. ScrapViews
	 * are old views that could potentially be used by the adapter to avoid
	 * allocating views unnecessarily.
	 * 
	 * @see android.widget.AbsListView#setRecyclerListener(android.widget.AbsListView.RecyclerListener)
	 * @see android.widget.AbsListView.RecyclerListener
	 */
	protected class RecycleBin {
		private RecyclerListener mRecyclerListener;

		/**
		 * The position of the first view stored in mActiveViews.
		 */
		private int mFirstActivePosition;

		/**
		 * Views that were on screen at the start of layout. This array is
		 * populated at the start of layout, and at the end of layout all view
		 * in mActiveViews are moved to mScrapViews. Views in mActiveViews
		 * represent a contiguous range of Views, with position of the first
		 * view store in mFirstActivePosition.
		 */
		private GLView[] mActiveViews = new GLView[0];

		/**
		 * Unsorted views that can be used by the adapter as a convert view.
		 */
		private ArrayList<GLView>[] mScrapViews;

		private int mViewTypeCount;

		private ArrayList<GLView> mCurrentScrap;

		@SuppressWarnings("unchecked")
		public void setViewTypeCount(int viewTypeCount) {
			if (viewTypeCount < 1) {
				throw new IllegalArgumentException("Can't have a viewTypeCount < 1");
			}
			// noinspection unchecked
			ArrayList<GLView>[] scrapViews = new ArrayList[viewTypeCount];
			for (int i = 0; i < viewTypeCount; i++) {
				scrapViews[i] = new ArrayList<GLView>();
			}
			mViewTypeCount = viewTypeCount;
			mCurrentScrap = scrapViews[0];
			mScrapViews = scrapViews;
		}

		public boolean shouldRecycleViewType(int viewType) {
			return viewType >= 0;
		}

		/**
		 * Clears the scrap heap.
		 */
		void clear() {
			if (mViewTypeCount == 1) {
				final ArrayList<GLView> scrap = mCurrentScrap;
				final int scrapCount = scrap.size();
				for (int i = 0; i < scrapCount; i++) {
					removeDetachedView(scrap.remove(scrapCount - 1 - i), false);
				}
			} else {
				final int typeCount = mViewTypeCount;
				for (int i = 0; i < typeCount; i++) {
					final ArrayList<GLView> scrap = mScrapViews[i];
					final int scrapCount = scrap.size();
					for (int j = 0; j < scrapCount; j++) {
						removeDetachedView(scrap.remove(scrapCount - 1 - j), false);
					}
				}
			}
		}

		/**
		 * Fill ActiveViews with all of the children of the AbsListView.
		 * 
		 * @param childCount
		 *            The minimum number of views mActiveViews should hold
		 * @param firstActivePosition
		 *            The position of the first view that will be stored in
		 *            mActiveViews
		 */
		void fillActiveViews(int childCount, int firstActivePosition) {
			if (mActiveViews.length < childCount) {
				mActiveViews = new GLView[childCount];
			}
			mFirstActivePosition = firstActivePosition;

			final GLView[] activeViews = mActiveViews;
			for (int i = 0; i < childCount; i++) {
				GLView child = getChildAt(i);
				GLScrollableBaseGrid.LayoutParams lp = (GLScrollableBaseGrid.LayoutParams) child
						.getLayoutParams();
				// Don't put header or footer views into the scrap heap
				if (lp != null && lp.mViewType != AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER) {
					// Note: We do place AdapterView.ITEM_VIEW_TYPE_IGNORE in
					// active views.
					// However, we will NOT place them into scrap views.
					activeViews[i] = child;
				}
			}
			for (int i = 0; i < activeViews.length; i++) {
				// Log.d("MyRecycler","We have recycled activeview "+i);
				// Log.d("MyRecycler","So whe we call it will be "+(i-mFirstActivePosition));
			}
		}

		/**
		 * Get the view corresponding to the specified position. The view will
		 * be removed from mActiveViews if it is found.
		 * 
		 * @param position
		 *            The position to look up in mActiveViews
		 * @return The view if it is found, null otherwise
		 */
		GLView getActiveView(int position) {
			int index = position - mFirstActivePosition;
			final GLView[] activeViews = mActiveViews;
			// Log.d("MyRecycler","We're recovering view "+index+" of a list of "+activeViews.length);
			if (index >= 0 && index < activeViews.length) {
				final GLView match = activeViews[index];
				activeViews[index] = null;
				return match;
			}
			return null;
		}

		/**
		 * @return A view from the ScrapViews collection. These are unordered.
		 */
		GLView getScrapView(int position) {
			ArrayList<GLView> scrapViews;
			if (mViewTypeCount == 1) {
				scrapViews = mCurrentScrap;
				int size = scrapViews.size();
				if (size > 0) {
					return scrapViews.remove(size - 1);
				} else {
					return null;
				}
			} else {
				int whichScrap = mAdapter.getItemViewType(position);
				if (whichScrap >= 0 && whichScrap < mScrapViews.length) {
					scrapViews = mScrapViews[whichScrap];
					int size = scrapViews.size();
					if (size > 0) {
						return scrapViews.remove(size - 1);
					}
				}
			}
			return null;
		}

		/**
		 * Put a view into the ScapViews list. These views are unordered.
		 * 
		 * @param scrap
		 *            The view to add
		 */
		public void addScrapView(GLView scrap) {
			GLScrollableBaseGrid.LayoutParams lp = (GLScrollableBaseGrid.LayoutParams) scrap
					.getLayoutParams();
			if (lp == null) {
				return;
			}

			// Don't put header or footer views or views that should be ignored
			// into the scrap heap
			int viewType = lp.mViewType;
			if (!shouldRecycleViewType(viewType)) {
				return;
			}

			if (mViewTypeCount == 1) {
				mCurrentScrap.add(scrap);
			} else {
				mScrapViews[viewType].add(scrap);
			}

			if (mRecyclerListener != null) {
				mRecyclerListener.onMovedToScrapHeap(scrap);
			}
		}

		/**
		 * Move all views remaining in mActiveViews to mScrapViews.
		 */
		void scrapActiveViews() {
			final GLView[] activeViews = mActiveViews;
			final boolean hasListener = mRecyclerListener != null;
			final boolean multipleScraps = mViewTypeCount > 1;

			ArrayList<GLView> scrapViews = mCurrentScrap;
			final int count = activeViews.length;
			for (int i = 0; i < count; ++i) {
				final GLView victim = activeViews[i];
				if (victim != null) {
					int whichScrap = ((GLScrollableBaseGrid.LayoutParams) victim.getLayoutParams()).mViewType;

					activeViews[i] = null;

					if (whichScrap == AdapterView.ITEM_VIEW_TYPE_IGNORE) {
						// Do not move views that should be ignored
						continue;
					}

					if (multipleScraps) {
						scrapViews = mScrapViews[whichScrap];
					}
					scrapViews.add(victim);

					if (hasListener) {
						mRecyclerListener.onMovedToScrapHeap(victim);
					}

				}
			}

			pruneScrapViews();
		}

		/**
		 * Makes sure that the size of mScrapViews does not exceed the size of
		 * mActiveViews. (This can happen if an adapter does not recycle its
		 * views).
		 */
		private void pruneScrapViews() {
			final int maxViews = mActiveViews.length;
			final int viewTypeCount = mViewTypeCount;
			final ArrayList<GLView>[] scrapViews = mScrapViews;
			for (int i = 0; i < viewTypeCount; ++i) {
				final ArrayList<GLView> scrapPile = scrapViews[i];
				int size = scrapPile.size();
				final int extras = size - maxViews;
				size--;
				for (int j = 0; j < extras; j++) {
					removeDetachedView(scrapPile.remove(size--), false);
				}
			}
		}

		/**
		 * Puts all views in the scrap heap into the supplied list.
		 */
		void reclaimScrapViews(List<GLView> views) {
			if (mViewTypeCount == 1) {
				views.addAll(mCurrentScrap);
			} else {
				final int viewTypeCount = mViewTypeCount;
				final ArrayList<GLView>[] scrapViews = mScrapViews;
				for (int i = 0; i < viewTypeCount; ++i) {
					final ArrayList<GLView> scrapPile = scrapViews[i];
					views.addAll(scrapPile);
				}
			}
		}
	}

	/**
	 * AbsListView extends LayoutParams to provide a place to hold the view
	 * type.
	 */
	public static class LayoutParams extends AdapterView.LayoutParams {
		/**
		 * View type for this view, as returned by
		 * {@link android.widget.Adapter#getItemViewType(int) }
		 */
		int mViewType;

		/**
		 * When this boolean is set, the view has been added to the AbsListView
		 * at least once. It is used to know whether headers/footers have
		 * already been added to the list view and whether they should be
		 * treated as recycled views or not.
		 */
		boolean mRecycledHeaderFooter;

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
		}

		public LayoutParams(int w, int h) {
			super(w, h);
		}

		public LayoutParams(int w, int h, int viewType) {
			super(w, h);
			this.mViewType = viewType;
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}
	}

	@Override
	public ShellBaseAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public GLView getSelectedView() {
		if (getItemCount() > 0 && mSelectedPosition >= 0) {
			return getChildAt(mSelectedPosition);
		} else {
			return null;
		}
	}

	@Override
	public void setSelection(int position) {
		mSelectedPosition = position;
		invalidate();
	}

	/**
	 * A base class for Runnables that will check that their view is still
	 * attached to the original window as when the Runnable was created.
	 * 
	 */
	private class WindowRunnnable {
		private int mOriginalAttachCount;

		public void rememberWindowAttachCount() {
			mOriginalAttachCount = getWindowAttachCount();
		}

		public boolean sameWindow() {
			return hasWindowFocus() && getWindowAttachCount() == mOriginalAttachCount;
		}
	}

	@Override
	public synchronized boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mPerformingLongPress) {
			mPerformingLongPress = false;
			return true;
		}
		/*
		 * This method JUST determines whether we want to intercept the motion.
		 * If we return true, onTouchEvent will be called and we do the actual
		 * scrolling there.
		 */

		/*
		 * Shortcut the most recurring case: the user is in the dragging state
		 * and he is moving his finger. We want to intercept this motion.
		 */
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}

		final float x = ev.getX();
		final float y = ev.getY();

		switch (action) {
			case MotionEvent.ACTION_MOVE :
				/*
				 * mIsBeingDragged == false, otherwise the shortcut would have
				 * caught it. Check whether the user has moved far enough from his
				 * original down touch.
				 */

				/*
				 * Locally do absolute value. mLastMotionX is set to the y value of
				 * the down event.
				 */
				final int xDiff = (int) Math.abs(x - mLastMotionX);
				final int yDiff = (int) Math.abs(y - mLastMotionY);

				final int touchSlop = mTouchSlop;
				boolean xMoved = xDiff > touchSlop;
				boolean yMoved = yDiff > touchSlop;

				if (xMoved || yMoved) {
					// Scroll if the user moved far enough along the X axis
					if (isAllowToScroll()) {
						mTouchState = TOUCH_STATE_SCROLLING;
						mScrollableHandler.onTouchEvent(ev, MotionEvent.ACTION_DOWN);
					}
				}

				break;

			case MotionEvent.ACTION_DOWN :
				// Remember location of down touch
				mLastMotionX = x;
				mLastMotionY = y;

				/*
				 * If being flinged and user touches the screen, initiate drag;
				 * otherwise don't. mScroller.isFinished should be false when being
				 * flinged.
				 */
				mTouchState = mScrollableHandler.isScrollFinished()
						? TOUCH_STATE_DOWN
						: TOUCH_STATE_SCROLLING;
				break;

			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP :
				// Release the drag
				mTouchState = TOUCH_STATE_REST;
				break;
		}

		/*
		 * The only time we want to intercept motion events is if we are in the
		 * drag mode.
		 */
		return mTouchState != TOUCH_STATE_REST;
	}

	@Override
	public synchronized boolean onTouchEvent(final MotionEvent ev) {
		final int action = ev.getAction();
		if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
			//避免点击效果消失太快，故延迟100毫秒
			postDelayed(new Runnable() {

				@Override
				public void run() {
					if (mClickChild != null) {
						if (mClickChild.isPressed()) {
							mClickChild.setPressed(false);
						}
						mClickChild.onTouchEvent(ev);
//						mClickChild = null;
					}
					positionSelector(getChildAt(0));
					setSelection(0);
				}
			}, 100);
		}

		if (mPerformingLongPress) {
			mPerformingLongPress = false;
			return true;
		}

		if (!mScrollableHandler.isScrollFinished() && isAllowToScroll()) {
			
//			if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
//				IconView.resetIconPressState();
//			}
			return mScrollableHandler.onTouchEvent(ev, action);
		}
		final int x = (int) ev.getX();
		final int y = (int) ev.getY();
		final GLView child;
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				mTouchState = TOUCH_STATE_DOWN;
				child = mScrollableHandler.pointToView(ev);
				if (child != null) {
					// FIXME Debounce
					if (mPendingCheckForTap == null) {
						mPendingCheckForTap = new CheckForTap();
					}

					mClickChild = child;

					boolean result = (Boolean) mClickChild.getTag(SACCEPTEVENT);
					if (!result) {
						mClickChild.setPressed(true);
					}

					GLContentView.postToGLThreadDelayed(mPendingCheckForTap,
							ViewConfiguration.getTapTimeout());
					// Remember where the motion event started
					mCheckTapPosition = getPositionForView(child);
				}
				// Remember where the motion event started
				mLastMotionX = x;
				break;
			case MotionEvent.ACTION_MOVE :
				if (mTouchState == TOUCH_STATE_SCROLLING || mTouchState == TOUCH_STATE_DOWN
						|| mTouchState == TOUCH_STATE_TAP) {

					// Scroll to follow the motion event
					final int deltaX = (int) (mLastMotionX - x);
					int tempX = Math.abs(deltaX);
					if (tempX >= mTouchSlop || mTouchState == TOUCH_STATE_SCROLLING) {
						mTouchState = TOUCH_STATE_SCROLLING;
						mLastMotionX = x;
						if (mClickChild != null) {
							if (mClickChild.isPressed()) {
								mClickChild.setPressed(false);
							}
							mClickChild.onTouchEvent(ev);
							GLContentView.removeCallback(mTouchState == TOUCH_STATE_DOWN
									? mPendingCheckForTap
									: mPendingCheckForLongPress);
//							mClickChild = null;
						}
					}
					final int deltaY = (int) (mLastMotionY - y);
					int tempY = Math.abs(deltaY);
					if (tempY >= mTouchSlop || mTouchState == TOUCH_STATE_SCROLLING) {
						mTouchState = TOUCH_STATE_SCROLLING;
						if (mClickChild != null) {
							if (mClickChild.isPressed()) {
								mClickChild.setPressed(false);
							}
							mClickChild.onTouchEvent(ev);
							GLContentView.removeCallback(mTouchState == TOUCH_STATE_DOWN
									? mPendingCheckForTap
									: mPendingCheckForLongPress);
//							mClickChild = null;
						}
					}
					// 如果开启了检测向上滑动是否要滑屏的开关，那么就要检测条件
					if (mIsNeedDetectionVerticalSliding) {
						float angle = calSlidingAngle(tempX, tempY);
						if (angle > mSlidingAngle) {
							return true;
						}
					}
				}
				// if touch in child
				if (mTouchState != TOUCH_STATE_SCROLLING) {
					if (mClickChild != null) {
						child = mScrollableHandler.pointToView(ev);
						if (child == null || child != mClickChild) {
							if (mClickChild.isPressed()) {
								mClickChild.setPressed(false);
							}
							mClickChild.onTouchEvent(ev);
							GLContentView.removeCallback(mTouchState == TOUCH_STATE_DOWN
									? mPendingCheckForTap
									: mPendingCheckForLongPress);
//							mClickChild = null;
						}
					}

				}
				break;
			case MotionEvent.ACTION_UP :
				if (mTouchState != TOUCH_STATE_SCROLLING) {
					child = mClickChild;
					if (child != null && child.equals(mScrollableHandler.pointToView(ev))) {
						if (mPerformClick == null) {
							mPerformClick = new PerformClick();
						}

						final GLScrollableBaseGrid.PerformClick performClick = mPerformClick;
						performClick.mChild = child;
						performClick.mClickMotionPosition = mCheckTapPosition;
						performClick.rememberWindowAttachCount();
						if (mTouchState == TOUCH_STATE_DOWN || mTouchState == TOUCH_STATE_TAP) {
							// final Handler handler = getHandler();
							//
							// if (handler != null) {
							// handler.removeCallbacks(mTouchState ==
							// TOUCH_STATE_DOWN ?
							// mPendingCheckForTap : mPendingCheckForLongPress);
							// }
							GLContentView.removeCallback(mTouchState == TOUCH_STATE_DOWN
									? mPendingCheckForTap
									: mPendingCheckForLongPress);
							mLayoutMode = LAYOUT_NORMAL;
							mTouchState = TOUCH_STATE_TAP;
							if (!mDataChanged) {
								boolean result = (Boolean) mClickChild.getTag(SACCEPTEVENT);
								if (!result) {
									GLContentView.postToGLThreadDelayed(new Runnable() {
										public void run() {
											if (child.isPressed()) {
												child.setPressed(false);
											}
											child.onTouchEvent(ev);
											if (!mDataChanged) {
												post(performClick);
											}
											mTouchState = TOUCH_STATE_REST;
										}
									}, 0);
								}

								// }, ViewConfiguration.getPressedStateDuration());
							}
							return true;
						}
					} else {
						resurrectSelection();
					}
				}

				// 如果开启了检测向上滑动是否要滑屏的开关，那么就要检测条件
				boolean isScroll = true;
				if (mTouchState == TOUCH_STATE_SCROLLING) {
					
//					if (mClickChild != null && mClickChild instanceof IconView<?>) {
//						((IconView<?>) mClickChild).start3DMultiViewUpAnimation();
//					}
					
					resurrectSelection();
					if (mIsNeedDetectionVerticalSliding) {
						final int deltaX = (int) Math.abs(mLastMotionX - x);
						final int deltaY = (int) Math.abs(mLastMotionY - y);
						float angle = calSlidingAngle(deltaX, deltaY);
						if (angle > mSlidingAngle) {
							isScroll = false;
						}
					}
				}

				mTouchState = TOUCH_STATE_REST;
				mCheckTapPosition = INVALID_POSITION;
				//				mClickChild = null;
				hideSelector();
				invalidate();

				final Handler handler = getHandler();
				if (handler != null) {
					handler.removeCallbacks(mPendingCheckForLongPress);
				}
				//避免点击效果消失太快，故延迟100毫秒
				postDelayed(new Runnable() {

					@Override
					public void run() {
						if (mClickChild != null) {
							if (mClickChild.isPressed()) {
								mClickChild.setPressed(false);
								mClickChild.onTouchEvent(ev);
							}
//							mClickChild = null;
						}
						positionSelector(getChildAt(0));
						setSelection(0);
					}
				}, 100);
				if (!isScroll) {
					return true;
				}
				break;
			case MotionEvent.ACTION_CANCEL :
				mTouchState = TOUCH_STATE_REST;
				resurrectSelection();
				break;
			default :
				break;
		}
		return isAllowToScroll() ? mScrollableHandler.onTouchEvent(ev, action) : true;
	}

	void hideSelector() {
		if (mSelectedPosition != INVALID_POSITION) {
			setSelection(INVALID_POSITION);
			mSelectorRect.setEmpty();
		}
	}

	// public abstract GLView getViewAtPosition(int pos);

	// @Override
	@SuppressWarnings("unchecked")
	public GLView getViewAtPosition(int pos) {
		GLView v = null;
		if (pos >= 0) {
			if (mAdapter != null) {
				if (pos < mAdapter.getCount()) {
					synchronized (mAdapter) {
						v = mAdapter.getViewByItem(mAdapter.getItem(pos));
					}
				}
			}
		}
		return v;
	}

	/**
	 * Attempt to bring the selection back if the user is switching from touch
	 * to trackball mode
	 * 
	 * @return Whether selection was set to something.
	 */
	boolean resurrectSelection() {
		if (getChildCount() <= 0) {
			return false;
		}

		final int childCount = getChildCount();

		if (childCount <= 0) {
			return false;
		}
		for (int i = 0; i < childCount; i++) {
			GLView child = getChildAt(i);
			if (child.isPressed()) {
				child.setPressed(false);
			}
		}
		positionSelector(getChildAt(0));
		setSelection(0);

		return true;
	}

	// public abstract GLView pointToView(final MotionEvent ev);

	/**
	 * 
	 * <br>
	 * 类描述: <br>
	 * 功能详细描述:
	 * 
	 * @author chaoziliang
	 * @date [2012-9-7]
	 */
	private class PerformClick extends WindowRunnnable implements Runnable {
		GLView mChild;
		int mClickMotionPosition;

		public void run() {
			// The data has changed since we posted this action in the event
			// queue,
			// bail out before bad things happen
			if (mDataChanged) {
				return;
			}
			final int realPosition = mClickMotionPosition;
			if (realPosition == INVALID_POSITION) {
				return;
			}
			if (mAdapter != null && realPosition < mAdapter.getCount() && sameWindow()
					&& !ShellAdmin.sShellManager.getShell().isViewVisible(IViewId.PROTECTED_LAYER)) {
				//				IconView.resetIconPressState();
				performItemClick(mChild, realPosition, mAdapter.getItemId(realPosition));
				setSelection(INVALID_POSITION);
			}
		}
	}

	void positionSelector(GLView child) {
		if (child != null) {
			final Rect selectorRect = mSelectorRect;
			selectorRect.set(child.getLeft(), child.getTop() + 0, child.getRight(),
					child.getBottom() + 0);
			positionSelector(selectorRect.left, selectorRect.top, selectorRect.right,
					selectorRect.bottom);
			refreshDrawableState();
		}
	}

	private void positionSelector(int l, int t, int r, int b) {
		mSelectorRect.set(l - mSelectionLeftPadding + getScrollX(), t - mSelectionTopPadding
				+ getScrollY(), r + mSelectionRightPadding + getScrollX(), b
				+ mSelectionBottomPadding + getScrollY());
	}

	/**
	 * 
	 * <br>
	 * 类描述: <br>
	 * 功能详细描述:
	 * 
	 * @author chaoziliang
	 * @date [2012-9-7]
	 */
	final class CheckForTap implements Runnable {
		public void run() {
			if (mTouchState == TOUCH_STATE_DOWN) {
				mTouchState = TOUCH_STATE_TAP;
				final GLView child = getViewAtPosition(mCheckTapPosition);
				if (child != null && !child.hasFocusable()) {
					mLayoutMode = LAYOUT_NORMAL;

					if (!mDataChanged) {

						// setPressed(true);
						setSelection(mCheckTapPosition);
						positionSelector(child);
						final int longPressTimeout = getLongPressTimeout();
						final boolean longClickable = isLongClickable();

						if (longClickable) {
							if (mPendingCheckForLongPress == null) {
								mPendingCheckForLongPress = new CheckForLongPress();
							}
							mPendingCheckForLongPress.rememberWindowAttachCount();
							GLContentView.postToGLThreadDelayed(mPendingCheckForLongPress,
									longPressTimeout);
						} else {
							mTouchState = TOUCH_STATE_DONE_WAITING;
						}
					} else {
						mTouchState = TOUCH_STATE_DONE_WAITING;
					}
				}
			}
		}
	}

	protected int getLongPressTimeout() {
		return ViewConfiguration.getLongPressTimeout();
	}

	/**
	 * 
	 * <br>
	 * 类描述: <br>
	 * 功能详细描述:
	 * 
	 * @author chaoziliang
	 * @date [2012-9-7]
	 */
	private class CheckForLongPress extends WindowRunnnable implements Runnable {
		public void run() {
			final int motionPosition = mCheckTapPosition;
			final GLView child = getViewAtPosition(motionPosition);
			if (child != null && mAdapter != null) {
				final int longPressPosition = motionPosition;
				final long longPressId = mAdapter.getItemId(motionPosition);

				boolean handled = false;
				if (sameWindow()
						&& !mDataChanged
						&& !ShellAdmin.sShellManager.getShell().isViewVisible(
								IViewId.PROTECTED_LAYER)) {
					handled = performLongPress(child, longPressPosition, longPressId);
				}
				if (handled) {
					if (child.isPressed()) {
						child.setPressed(false);
					}
					mTouchState = TOUCH_STATE_REST;
				} else {
					mTouchState = TOUCH_STATE_DONE_WAITING;
				}

			}
		}
	}

	private synchronized boolean performLongPress(final GLView child, final int longPressPosition,
			final long longPressId) {
		boolean handled = false;
		if (mTouchState != TOUCH_STATE_SCROLLING) {
			mPerformingLongPress = true;
			if (getOnItemLongClickListener() != null) {
				handled = getOnItemLongClickListener().onItemLongClick(GLScrollableBaseGrid.this,
						child, longPressPosition, longPressId);
			}
			if (handled) {
				performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
			}
		}
		return handled;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mScrollableHandler.setScreenSize(w, h);
	}

	@Override
	public void computeScroll() {
		mScrollableHandler.computeScrollOffset();
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		mScrollableHandler.draw(canvas);
		//		super.dispatchDraw(canvas);
	}

	public abstract void refreshGridView();

	public void setGLViewWrapperDeferredInvalidate(boolean flag) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			GLView child = getChildAt(i);
			if (child instanceof IconView) {
				IconView icon = (IconView) child;
				GLTextViewWrapper wrapper = (GLTextViewWrapper) icon.getTextView();
				wrapper.setUseDeferredInvalidate(flag);
			}
		}
	}

	public void setOnLayoutListener(OnLayoutListener layoutListener) {
		mLayoutListener = layoutListener;
	}

	@Override
	protected boolean addViewInLayout(GLView child, int index,
			android.view.ViewGroup.LayoutParams params, boolean preventRequestLayout) {
		return super.addViewInLayout(child, index, params, preventRequestLayout);
	}

	@Override
	protected boolean drawChild(GLCanvas canvas, GLView child, long drawingTime) {
		return super.drawChild(canvas, child, drawingTime);
	}

	public void setScrollableHandler(AbsScrollableGridViewHandler handler) {
		mScrollableHandler = handler;
	}

	public void setIndicator(GLView indicator) {
		mScrollableHandler.setIndicator(indicator);
		if (indicator != null && indicator instanceof DesktopIndicator) {
			((DesktopIndicator) indicator).setIndicatorListener(mScrollableHandler);
		}
	}

	public void clearData() {
		if (mAdapter != null) {
			mAdapter.clear();
			mAdapter = null;
		}
		if (mScrollableHandler != null) {
			mScrollableHandler.clearHolder();
		}
		setAdapter(mAdapter);
	}

	public synchronized void setData(List infoList) {
		if (mAdapter == null) {
			mAdapter = createAdapter(mContext, infoList);
		}
		mAdapter.setInfoList(infoList);
		setAdapter(mAdapter);
	}

	@Override
	public void attachViewToParent(GLView child, int index,
			android.view.ViewGroup.LayoutParams params) {
		super.attachViewToParent(child, index, params);
	}

	public int getTouchState() {
		return mTouchState;
	}

	public int[] getLocationInDragLayer() {
		int[] loc = new int[2];
		ShellAdmin.sShellManager.getShell().getContainer().getLocation(this, loc);
		return loc;
	}

	public abstract ShellBaseAdapter createAdapter(Context context, List infoList);

	protected abstract void onScrollStart();
	protected abstract void onScrollFinish();
	/**
	 * 横屏滚动下，屏幕改变回调，不支持竖屏
	 */
	protected abstract void onScreenChange(int newScreen, int oldScreen);

	public boolean isVerScroll() {
		return mIsVerScroll;
	}

	public boolean isScrollFinish() {
		return mScrollableHandler.isScrollFinished();
	}

	public int getItemCount() {
		if (mAdapter != null) {
			return mAdapter.getCount();
		}
		return 0;
	}

	protected abstract void handleScrollerSetting();

	protected abstract void handleRowColumnSetting(boolean updateDB);

	protected boolean isAllowToScroll() {
		return true;
	}
	
	/**
	 * 内部行间距
	 * @param horizontalSpacing
	 */
    public void setHorizontalSpacing(int horizontalSpacing) {
    	mScrollableHandler.setHorizontalSpacing(horizontalSpacing);
    }

    /**
     * 内部列间距
     * @param verticalSpacing
     */
    public void setVerticalSpacing(int verticalSpacing) {
    	mScrollableHandler.setVerticalSpacing(verticalSpacing);
    }
    
    /**
	 * 内部行间距
	 */
    public int getHorizontalSpacing() {
    	return mScrollableHandler.getHorizontalSpacing();
    }

    /**
     * 内部列间距
     */
    public int getVerticalSpacing() {
    	return mScrollableHandler.getVerticalSpacing();
    }
    
    protected int getCurrentScreenLastIndex() {
		int lVisiableRow = mScrollableHandler.getCurLastVisiableRow();
		int currentScreenLastIndex = (lVisiableRow + 1) * mNumColumns - 1;
		int allCount = getChildCount();
		if (currentScreenLastIndex >= allCount) {
			currentScreenLastIndex = allCount - 1;
		}
		return currentScreenLastIndex;
	}

	protected int getCurrentScreenFirstIndex() {
		int fVisiableRow = mScrollableHandler.getCurFirstVisiableRow();
		int firstIndex = fVisiableRow * mNumColumns;
		return firstIndex;
	}
	
	/**
	 * 获得一版能显示的图标数
	 * @return
	 */
	protected int getPageItemCount() {
		int pageCount = 0;
		if (mIsVerScroll) {
			int fVisiableRow = mScrollableHandler.getCurFirstVisiableRow();
			int lVisiableRow = mScrollableHandler.getCurLastVisiableRow();
			pageCount = mNumColumns * (lVisiableRow - fVisiableRow + 1);
		} else {
			pageCount = mNumColumns * mNumRows;
		}
		return pageCount;
	}
	
	public ArrayList<GLView> getCurScreenIcons() {
		ArrayList<GLView> iconList;
		if (isVerScroll()) {
			VerScrollableGridViewHandler handler = (VerScrollableGridViewHandler) mScrollableHandler;
			// handler.setClipCanvas(false);
			int firstRow = handler.getCurFirstVisiableRow();
			int lastRow = handler.getCurLastVisiableRow();
			iconList = new ArrayList<GLView>();
			for (int i = firstRow; i <= lastRow; i++) {
				List<GLView> iconListPerRow = handler.getChildren(i);
				if (iconListPerRow == null && handler.getChildCount() != 0) {
					throw new IllegalArgumentException(
							"Get the current row is:" + i
									+ " current last row is:"
									+ handler.getCurLastVisiableRow()
									+ " current total row is:"
									+ handler.getChildCount());
				}
				iconList.addAll(iconListPerRow);
			}
		} else {
			HorScrollableGridViewHandler handler = (HorScrollableGridViewHandler) mScrollableHandler;
			int currentScreen = handler.getCurrentScreen();
			iconList = handler.getChildren(currentScreen);
			if (iconList == null && handler.getChildCount() != 0) {
				throw new IllegalArgumentException("Get the current screen is:"
						+ currentScreen + " current total screen is:"
						+ handler.getChildCount());
			}
		}
		return iconList;
	}
}