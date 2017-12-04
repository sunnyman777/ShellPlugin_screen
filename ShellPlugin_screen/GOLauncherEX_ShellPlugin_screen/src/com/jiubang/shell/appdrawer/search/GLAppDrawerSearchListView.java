package com.jiubang.shell.appdrawer.search;

import android.content.Context;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.widget.GLListView;
import com.go.proxy.ApplicationProxy;
import com.jiubang.ggheart.apps.desks.appfunc.model.FuncSearchResultItem;
import com.jiubang.shell.appdrawer.search.GLAppDrawerSearchHeader.ClearBtnOnClickListener;
import com.jiubang.shell.appdrawer.search.adapter.GLAppDrawerSearchListAdapter;
/**
 * 
 * @author dingzijian
 *
 */
public class GLAppDrawerSearchListView extends GLListView implements
//ShellScrollerListener ,
			OnClickListener,
			ClearBtnOnClickListener {
	//	private ShellScroller mScroller;
	//	private FastVelocityTracker mVelocityTracker;
	private ListBtnOnClickListener mListBtnOnClickListener;
	//	private float mInterceptTouchDownX;
	//	private float mInterceptTouchDownY;
	//	private float mInterceptTouchMoveX;
	//	private float mInterceptTouchMoveY;
	//	private boolean mInterceptTouchMoved;
	//	private GLAppDrawerSearchListAdapter mAdapter;

	public GLAppDrawerSearchListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//		setOrientation(VERTICAL);
		//		mScroller = new ShellScroller(getContext(), this);
		//		mScroller.setOrientation(ShellScroller.VERTICAL);
		//		mVelocityTracker = new FastVelocityTracker();
		setScrollingCacheEnabled(true);
	}

	//	@Override
	//	public void onScrollChanged(int newScroll, int oldScroll) {
	//		// TODO Auto-generated method stub
	//
	//	}
	//
	//	@Override
	//	public ShellScroller getScroller() {
	//		return mScroller;
	//	}
	//
	//	@Override
	//	public void setScroller(ShellScroller scroller) {
	//		mScroller = scroller;
	//	}
	//
	//	@Override
	//	public void onScrollFinish(int currentScroll) {
	//		// TODO Auto-generated method stub
	//
	//	}
	//	

	//
	//	@Override
	//	public boolean onTouchEvent(MotionEvent event) {
	//		boolean ret = false;
	//		int action = event.getAction() & MotionEvent.ACTION_MASK;
	//		switch (action) {
	//			case MotionEvent.ACTION_DOWN :
	//				mScroller.onTouchEvent(event, action);
	//				ret = true;
	//				break;
	//
	//			case MotionEvent.ACTION_MOVE :
	//				mScroller.onTouchEvent(event, action);
	//				ret = true;
	//				break;
	//
	//			case MotionEvent.ACTION_UP :
	//			case MotionEvent.ACTION_CANCEL :
	//				mScroller.onTouchEvent(event, action);
	//				ret = false;
	//				break;
	//
	//			default :
	//				break;
	//		}
	//		return ret;
	//	}
	//	@Override
	//	public void computeScroll() {
	//		mScroller.computeScrollOffset();
	//	}

	//	public void setAdapter(GLAppDrawerSearchListAdapter searchAppGridAdapter) {
	//		mAdapter = searchAppGridAdapter;
	//		removeAllViews();
	//		int count = searchAppGridAdapter.getCount();
	//		for (int i = 0; i < count; i++) {
	//			GLView view = searchAppGridAdapter.getView(i, null, this);
	//			if (view instanceof GLAppDrawerSearchAppGrid) {
	//				setGetMoreAppBtnOnClickListener((GetMoreAppBtnOnClickListener) view);
	//			}
	//			if (view.getGLParent() == null) {
	//				addView(view);
	//			}
	//		}
	//	}
	//	@Override
	//	protected void onLayout(boolean changed, int l, int t, int r, int b) {
	//		super.onLayout(changed, l, t, r, b);
	//		int childCount = getChildCount();
	//		if(childCount <= 0){
	//			return;
	//		}
	//		float curPercent = (float) mScroller.getScroll() / mScroller.getLastScroll(); // 记录当前滚动的百分比
	//		if (curPercent < 0) { // 判断避免比例小于0或大于1
	//			curPercent = 0;
	//		} else if (curPercent > 1) {
	//			curPercent = 1;
	//		}
	//		
	//		GLView lastView = getChildAt(childCount - 1);
	//		int bottom = lastView.getBottom() + lastView.getPaddingBottom();
	//		int contentHeight = Math.max(getHeight(), bottom);
	//		mScroller.setSize(getWidth(), getHeight(), getWidth(), contentHeight);
	//		mScroller.setPadding(getPaddingTop(), getPaddingBottom());
	//		mScroller.setScroll((int) (mScroller.getLastScroll() * curPercent)); // 还原layout前的滚动量百分比
	//	}

	//	@SuppressLint("WrongCall")
	//	@Override
	//	protected void dispatchDraw(GLCanvas canvas) {
	//		mScroller.onDraw(canvas);
	//		canvas.save();
	//		canvas.clipRect(0, getScrollY(), getWidth(), getScrollY() + getHeight());
	//		super.dispatchDraw(canvas);
	//		canvas.restore();
	//	}
	@Override
	public void cleanup() {

		super.cleanup();
	}
	@Override
	public void onClick(GLView glView) {
		switch (glView.getId()) {
			case R.id.gl_search_list_btn :
				handleListBtnOnClick((FuncSearchResultItem) glView.getTag());
				break;
			default :
				break;
		}
	}
	
	public void handleListBtnOnClick(FuncSearchResultItem searchResultItem) {
		switch (searchResultItem.mType) {
			case FuncSearchResultItem.ITEM_TYPE_SEARCH_WEB :
				if (mListBtnOnClickListener != null) {
					mListBtnOnClickListener.getMoreAppOnClick();
					searchResultItem.mTitle = ApplicationProxy.getContext().getResources()
							.getString(com.gau.go.launcherex.R.string.appfunc_search_in_market);
					GLAppDrawerSearchListAdapter adapter = (GLAppDrawerSearchListAdapter) getAdapter();
					adapter.notifyDataSetInvalidated();
				}
				break;
			case FuncSearchResultItem.ITEM_TYPE_CLEAR_HISTORY :
				if (mListBtnOnClickListener != null) {
					mListBtnOnClickListener.clearHistoryOnClick();
				}
				break;
			default :
				break;
		}
	}
	
	public void setListBtnOnClickListener(
			ListBtnOnClickListener mGetMoreAppBtnOnClickListener) {
		this.mListBtnOnClickListener = mGetMoreAppBtnOnClickListener;
	}

/**
 * 
 * @author dingzijian
 *
 */
	public interface ListBtnOnClickListener {
		public void getMoreAppOnClick();
		
		public void clearHistoryOnClick();
	}

	@Override
	public void clearBtnOnClick(GLSearchClearButton glview) {
		if (mListBtnOnClickListener != null) {
			mListBtnOnClickListener.clearHistoryOnClick();
		}
	}
	
}
