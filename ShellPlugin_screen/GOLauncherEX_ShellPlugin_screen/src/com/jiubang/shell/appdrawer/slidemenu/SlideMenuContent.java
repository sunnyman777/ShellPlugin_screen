package com.jiubang.shell.appdrawer.slidemenu;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.scroller.FastVelocityTracker;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.widget.GLGridView;
import com.go.util.AppUtils;
import com.go.util.GotoMarketIgnoreBrowserTask;
import com.go.util.market.MarketConstant;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.ggheart.apps.desks.diy.pref.PrefConst;
import com.jiubang.ggheart.apps.desks.diy.pref.PrivatePreference;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.shell.appdrawer.recentapp.GLRecentAppClearButton;
import com.jiubang.shell.appdrawer.recentapp.GLRecentAppLinearGrid;
import com.jiubang.shell.appdrawer.slidemenu.slot.ISlideMenuViewSlot;
import com.jiubang.shell.appdrawer.slidemenu.slot.SlideMenuAppManagerSlot;
import com.jiubang.shell.appdrawer.slidemenu.slot.SlideMenuHideSlot;
import com.jiubang.shell.appdrawer.slidemenu.slot.SlideMenuSlotAdapter;
import com.jiubang.shell.appdrawer.slidemenu.slot.SlideMenuSmallToolAdapter;
import com.jiubang.shell.appdrawer.slidemenu.slot.SlideMenuThemeSlot;
import com.jiubang.shell.appdrawer.slidemenu.slot.SlideMenuWidgetSlot;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.screen.GLWorkspace;
import com.jiubang.shell.scroller.ShellScroller;
import com.jiubang.shell.scroller.ShellScrollerListener;

/**
 * 
 *
 */
public class SlideMenuContent extends GLLinearLayout implements ShellScrollerListener, OnClickListener {
	
//	private SlideMenuAdvertGrid mAdvertGridView;
	private GLGridView mFuntionViewSlots;
	private List<ISlideMenuViewSlot> mSlots;
	private GLRecentAppLinearGrid mRecentAppGird;
	//small tool: layout and dataset
	/*private GLSlideMenuSmallToolGridView mSmallToolsSlots;
	private List<SideToolsInfo> mToolsSlots;*/
	private ShellScroller mScroller;
	private FastVelocityTracker mVelocityTracker;
//	private DesktopIndicator mIndicator;
	private GLRecentAppClearButton mRecentClear;
	
	private ShellTextViewWrapper mPromotionText;
	private ShellTextViewWrapper mPromotionBtnDownload;
	
	private float mInterceptTouchDownX;
	private float mInterceptTouchDownY;
	private float mInterceptTouchMoveX;
	private float mInterceptTouchMoveY;
	private boolean mInterceptTouchMoved;
	private boolean mIsAllowToScroll = true;
	private int mCurrentTarget = -1;
	private SlideMenuSmallToolAdapter mToolsSlotAdapter;
	public SlideMenuContent(Context context) {
		super(context);
	}

	public SlideMenuContent(Context context, AttributeSet attrs) {
		super(context, attrs);
		initMenuData();
	}
	
	private void initMenuData() {
		mSlots = new ArrayList<ISlideMenuViewSlot>(4);

		ISlideMenuViewSlot widgetSlot = new SlideMenuWidgetSlot();
		mSlots.add(widgetSlot);

		ISlideMenuViewSlot appManagerSlot = new SlideMenuAppManagerSlot();
		mSlots.add(appManagerSlot);

		ISlideMenuViewSlot hideAppSlot = new SlideMenuHideSlot();
		mSlots.add(hideAppSlot);

		ISlideMenuViewSlot themeSlot = new SlideMenuThemeSlot();
		mSlots.add(themeSlot);
	}
	
	@Override
	protected void onFinishInflate() {
		initViews();
	}
	
	private void initViews() {
		mFuntionViewSlots = (GLGridView) findViewById(R.id.appdrawer_slide_menu_funtion_slots);
		
		//mSmallToolsSlots = (GLSlideMenuSmallToolGridView) findViewById(R.id.appdrawer_slide_menu_smalltools_slots);
		
		
		mRecentAppGird = (GLRecentAppLinearGrid) findViewById(R.id.appdrawer_slide_menu_recent_grid);
		//mSmallTools = 
		mRecentClear = (GLRecentAppClearButton) findViewById(R.id.appdrawer_slide_menu_recent_clear);
		
		mPromotionText = (ShellTextViewWrapper) findViewById(R.id.appdrawer_slide_menu_promotion_text);
		mPromotionBtnDownload = (ShellTextViewWrapper) findViewById(R.id.appdrawer_slide_menu_promotion_download);
		mPromotionBtnDownload.setOnClickListener(this);
		
//		mAdvertGridView.setIndicator(mIndicator);
		SlideMenuSlotAdapter slotAdapter = new SlideMenuSlotAdapter(mContext, mSlots);
		mRecentClear.setOnClickListener(this);
		mScroller = new ShellScroller(mContext, this);
		mScroller.setOrientation(ShellScroller.VERTICAL);
		mFuntionViewSlots.setNumColumns(2);
		mFuntionViewSlots.setAdapter(slotAdapter);
		//small tool: dataset init and adapter init
		/*mToolsSlots = new ArrayList();
		mToolsSlotAdapter = new SlideMenuSmallToolAdapter(mContext, mToolsSlots);
		mSmallToolsSlots.setNumColumns(4);
		mSmallToolsSlots.setAdapter(mToolsSlotAdapter);*/
		
//		mFuntionViewSlots.setOnItemClickListener(this);
		mVelocityTracker = new FastVelocityTracker();
		//add by zhangxi @2013-10-25 for Slidemenu Recent Text Italic
		

		ShellTextViewWrapper recentText =  (ShellTextViewWrapper) findViewById(R.id.appdrawer_slide_menu_recent_title_text);
		//small tool text title
		/*ShellTextViewWrapper recentText =  (ShellTextViewWrapper) findViewById(R.id.appdrawer_slide_menu_smalltool_title);*/
		recentText.setItalic();
		
		swithPromotionView();
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {		
		super.onLayout(changed, l, t, r, b);
		float curPercent = (float) mScroller.getScroll() / mScroller.getLastScroll(); // 记录当前滚动的百分比
		if (curPercent < 0) { // 判断避免比例小于0或大于1
			curPercent = 0;
		} else if (curPercent > 1) {
			curPercent = 1;
		}
		int childCount = getChildCount();
		GLView lastView = getChildAt(childCount - 1);
		int bottom = lastView.getBottom() + lastView.getPaddingBottom();
		int contentHeight = Math.max(getHeight(), bottom);
		mScroller.setSize(getWidth(), getHeight() + getPaddingTop(), getWidth(), contentHeight);
		mScroller.setPadding(getPaddingTop(), getPaddingBottom());
		mScroller.setScroll((int) (mScroller.getLastScroll() * curPercent)); // 还原layout前的滚动量百分比
	}
	
	public void addViewToSlot(ISlideMenuViewSlot slotView) {
		if (!mSlots.contains(slotView)) {
			mSlots.add(slotView);
			SlideMenuSlotAdapter adapter = (SlideMenuSlotAdapter) mFuntionViewSlots.getAdapter();
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}
		} else {
			throw new IllegalAccessError("has the same funtion slot");
		}
	}
	
	public void addViewsToSlot(List<ISlideMenuViewSlot> slotViews) {
		mSlots.clear();
		mSlots.addAll(slotViews);
		SlideMenuSlotAdapter adapter = (SlideMenuSlotAdapter) mFuntionViewSlots.getAdapter();
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		// TODO Auto-generated method stub
		
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
	public void onScrollFinish(int currentScroll) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		int action = ev.getAction() & MotionEvent.ACTION_MASK;
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				if (mRecentClear.isExtented()) {
					Rect clearRect = new Rect();
					int[] location = new int[2];
					mRecentClear.getLoactionInGLViewRoot(location);
					clearRect.set(location[0], location[1], location[0] + mRecentClear.getWidth(), location[1] + mRecentClear.getHeight());
					if (!clearRect.contains((int) ev.getRawX(),
							(int) ev.getRawY())) {
						mRecentClear.resetView();
					}
				}
				break;
	
			default:
				break;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int slop = (int) (ViewConfiguration.get(getContext()).getScaledTouchSlop());
		boolean ret = false;
		int action = ev.getAction() & MotionEvent.ACTION_MASK;
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				mInterceptTouchDownX = ev.getX();
				mInterceptTouchDownY = ev.getY();
				mInterceptTouchMoved = false;
				break;

			case MotionEvent.ACTION_MOVE :
				mVelocityTracker.addMovement(ev);
				mVelocityTracker.computeCurrentVelocity(1000);
				if (!mInterceptTouchMoved) {
					// 一旦超出拖动范围不会再更新，作为初始的拖动斜率
					mInterceptTouchMoveX = Math.abs(ev.getX() - mInterceptTouchDownX);
					mInterceptTouchMoveY = Math.abs(ev.getY() - mInterceptTouchDownY);
					mInterceptTouchMoved = mInterceptTouchMoveX > slop
							|| mInterceptTouchMoveY > slop;
				}
				if (mInterceptTouchMoved) {
					if (mInterceptTouchMoveY >= mInterceptTouchMoveX * GLWorkspace.SCROLL_TAN_MINDEGREE) {
						// 竖向滑动
						if (isAllowToScroll()) {
							ret = true;
						}
					}
				}
				break;

			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL :
				break;

			default :
				break;
		}
		return ret;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean ret = false;
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				if (isAllowToScroll()) {
					mScroller.onTouchEvent(event, action);
					ret = true;
				}
				break;

			case MotionEvent.ACTION_MOVE :
				if (isAllowToScroll()) {
					mScroller.onTouchEvent(event, action);
					ret = true;
				}
				break;

			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL :
				if (isAllowToScroll()) {
					mScroller.onTouchEvent(event, action);
					ret = false;
				}
				break;

			default :
				break;
		}
		return ret;
//		return super.onTouchEvent(event);
	}
	
	@SuppressLint("WrongCall")
	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		mScroller.onDraw(canvas);
		canvas.save();
		canvas.clipRect(0, getScrollY() + getPaddingTop(), getWidth(),
				getScrollY() + getHeight());
		super.dispatchDraw(canvas);
		canvas.restore();
	}
	
	@Override
	public void computeScroll() {
		mScroller.computeScrollOffset();
	}
	
	private boolean isAllowToScroll() {
		return mIsAllowToScroll;
	}
	
	public void refreashRecentGrid() {
		//small tool layout refresh
		/*mSmallToolsSlots.refreshGridView();*/
	//	if (mRecentAppGird.isNeedRefreash()) {
			mRecentAppGird.refreshGridView();
	//	}
	}
	
	@Override
	public void onClick(GLView v) {
		int viewId = v.getId();
		switch (viewId) {
			case R.id.appdrawer_slide_menu_recent_clear:
				if (mRecentClear.isExtented()) {
					//to count only has data
					if (!mRecentAppGird.hasNoData()) {
						updateCleanCount();
					}
					PrivatePreference pref = PrivatePreference.getPreference(mContext);
				    int value = pref.getInt(PrefConst.KEY_SLIDEMENU_SHOW_PROMOTION_AD, 0);
				    if (value == 3) {
				    	
				    	promotionModule();
						swithPromotionView();

				    }
				    					    
			    	if (mRecentAppGird.getVisibility() == GLView.VISIBLE) {
						mRecentAppGird.removeAllRecentAppItems();
			    	} else {
			    		mRecentAppGird.removeAllRecentAppItemsWithoutAnimation();
			    		refreashRecentGrid();
			    	}
				    
					
				}
				                                                                                                             
				GuiThemeStatistics.sideOpStaticData("-1", "si_rnc_clear", 1, "-1");
				break;
			case R.id.appdrawer_slide_menu_promotion_download:

				Log.d("slidemenucontent", "onclick =" + mCurrentTarget);
				if (mCurrentTarget == 0) {
					promoteCleanMaster();
				} else if (mCurrentTarget == 1) {
					promoteDU();
				}
				break;
	
			default:
				break;
		}
	}
	
	private void promotionModule() {
		 PrivatePreference pref = PrivatePreference.getPreference(mContext);
	     int onClickCount = pref.getInt(PrefConst.KEY_SLIDEMENU_SHOW_PROMOTION_AD, 0);
	     Log.d("slidemenucontent", "promotionModule onClickCount=" + onClickCount);
	     if (onClickCount != 3) {
	    	 return;
	     }
	    /*
	     * if not install clean master then promote du
	     * if not install du then promote clean master
	     * else both app is not installed then use 50% chance to promote
	     */
	     /* promotionTarget = -1: promote nothing as both app is installed
	      * promotionTarget = 0: promote clean master
	      * promotionTarget = 1: promote DU Speed booster
	      */
	    int promotionTarget = -1;
	    boolean cm = AppUtils.isAppExist(mContext, PackageName.CLEAN_MASTER_PACKAGE);
	    boolean du = AppUtils.isAppExist(mContext, PackageName.DU_SPEED_BOOSTER);
	    
	    if (cm && !du) {
	    	promotionTarget = 1;
	    } else if (du && !cm) {
	    	promotionTarget = 0;
	    } else if (!cm && !du) {
	    	 //only promote clean master
			 promotionTarget = 0;
			/*Random chance = new Random();
			int changeOfGo = chance.nextInt(2);
			 if (changeOfGo == 0) {
				 //promote clean master
				 promotionTarget = 0;
			 } else {
				 //promote du
				 promotionTarget = 1;
			 }*/
		} else if (cm && du) {
			promotionTarget = -1;
		}
		//promote du or cm by the value of promotionTarget
		//------------------post data to server-------------------
		/*
		 * f000: show the app
		 * a000: click the download button
		 * b000: installed event
		 */
	    mCurrentTarget = promotionTarget;
	    if (promotionTarget == 0) {
	    	 
	    	GuiThemeStatistics.guiStaticData("20", 40, "2463865", "f000", 1, "", "", "", "", "601");
	    } else if (promotionTarget == 1) {
	    	GuiThemeStatistics.guiStaticData("20", 40, "5377557", "f000", 1, "", "", "", "", "602");
	    } else {
	    	return;
	    }
	    //updatecount
	    updateCleanCount();
	}
	
	private void promoteCleanMaster() {
		//click the donwload button 
		GuiThemeStatistics.guiStaticData("20", 40, "2463865", "a000", 1, "", "", "", "", "601");
		PrivatePreference pref = PrivatePreference.getPreference(mContext);
	    pref.putLong(PrefConst.KEY_SLIDEMENU_SHOW_CLEAN_MASTER_AD_TIMESTAMP, System.currentTimeMillis());
	    pref.commit();
	 
		if (GoAppUtils.isMarketExist(mContext)) {
			GoAppUtils.gotoMarket(mContext, MarketConstant.APP_DETAIL
					+ PackageName.CLEAN_MASTER_PACKAGE
					+ LauncherEnv.Plugin.CLEAN_MASTER_SLIDEMENU_PROMOTION);
		} else {
			AppUtils.gotoBrowser(mContext, MarketConstant.BROWSER_APP_DETAIL
					+ PackageName.CLEAN_MASTER_PACKAGE
					+ LauncherEnv.Plugin.CLEAN_MASTER_SLIDEMENU_PROMOTION);
		}
		 
	}
	private void promoteDU() {
		//click the download button
		GuiThemeStatistics.guiStaticData("20", 40, "5377557", "a000", 1, "", "", "", "", "602");
		PrivatePreference pref = PrivatePreference.getPreference(mContext);
	    pref.putLong(PrefConst.KEY_SLIDEMENU_SHOW_DU_SPEED_TIMESTAMP, System.currentTimeMillis());
	    pref.commit();
		GotoMarketIgnoreBrowserTask.startExecuteTask(ShellAdmin.sShellManager.getActivity(), LauncherEnv.Url.DU_SPEED_BOOSTER_URL);
	}
	
	public void resetClearView() {
		if (mRecentClear.isExtented()) {
			mRecentClear.resetView();
		}
	}

	public boolean enterFuntionSlot(int slotId, boolean needAnimation, Object...objs) {
		SlideMenuSlotAdapter adapter = (SlideMenuSlotAdapter) mFuntionViewSlots.getAdapter();
		GLView view = adapter.getViewByKey(String.valueOf(slotId));
		if (view != null) {
			ISlideMenuViewSlot slot = (ISlideMenuViewSlot) view.getTag();
			slot.showExtendFunctionView(view, needAnimation, objs);
			return true;
		}
		return false;
	}

	private void updateCleanCount() {
		PrivatePreference pref = PrivatePreference.getPreference(mContext);
	    int value = pref.getInt(PrefConst.KEY_SLIDEMENU_SHOW_PROMOTION_AD, 0);
	    Log.d("slidemenucontent", "update count pre =" + value);
	    if (value > 5) {
	    	return;
	    }
	    pref.putInt(PrefConst.KEY_SLIDEMENU_SHOW_PROMOTION_AD, ++value);
	    pref.commit();
	}
	public void swithPromotionView() {
		 Log.d("slidemenucontent", "swithPromotionView =" + mCurrentTarget);
		if (mCurrentTarget == 0 || mCurrentTarget == 1) {
			if (mPromotionText.getVisibility() == GLView.GONE) {
				mPromotionText.setVisibility(VISIBLE);
				mPromotionBtnDownload.setVisibility(VISIBLE);
			}
			
			if (mRecentAppGird.getVisibility() == GLView.VISIBLE) {

				mRecentAppGird.setVisibility(GONE);
			}
			
		} else {
			if (mPromotionText.getVisibility() == GLView.VISIBLE) {
				mPromotionText.setVisibility(GONE);
				mPromotionBtnDownload.setVisibility(GONE);
			}
			
			if (mRecentAppGird.getVisibility() == GLView.GONE) {
				mRecentAppGird.setVisibility(VISIBLE);
			}
		}
	}
	public void reSetPromotion() {
		mCurrentTarget = -1;
		Log.d("slidemenucontent", "reSetPromotion =" + mCurrentTarget);
		swithPromotionView();
	}

	@Override
	public void onScrollStart(int currentScroll) {
		// TODO Auto-generated method stub
		
	}
}
