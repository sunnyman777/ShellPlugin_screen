package com.jiubang.shell.screen;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.effector.united.IEffectorIds;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingUtils;
import com.jiubang.ggheart.data.info.EffectSettingInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.zeroscreen.ZeroScreenParamId;
import com.jiubang.ggheart.zeroscreen.navigation.AddSiteActivity;
import com.jiubang.ggheart.zeroscreen.navigation.bean.ZeroScreenAdInfo;
import com.jiubang.shell.IShell;
import com.jiubang.shell.screen.zero.GLZeroScreenView;
import com.jiubang.shell.screen.zero.navigation.GLNavigationView;
import com.jiubang.shell.scroller.ShellScreenScroller;
import com.jiubang.shell.scroller.ShellScreenScrollerEffector;
import com.jiubang.shell.scroller.ShellScreenScrollerListener;
import com.jiubang.shell.scroller.effector.CoupleScreenEffector;
import com.jiubang.shell.scroller.effector.gridscreen.GridScreenContainer;
import com.jiubang.shell.scroller.effector.subscreen.SubScreenContainer;


/**
 * 
 * <br>类描述:包含0屏及workspace的容器
 * <br>功能详细描述:主要负责0屏和workspace间滑动事件处理
 * 
 * @author  ruxueqin
 * @date  [2013-8-19]
 */
public class GLSuperWorkspace extends GLFrameLayout
		implements
		ShellScreenScrollerListener,
			SubScreenContainer,
			GridScreenContainer {

	private ShellScreenScroller mScroller;
	private CoupleScreenEffector mDeskScreenEffector; // 配合滚动器使用的效果器，主要作用是实现循环滚动

	private GLWorkspace mWorkspace;
	private GLZeroScreenView mZeroScreenView;

	private float mDownX;
	private float mLastX;
	private float mDownY;
	private float mLastY;
	private int mTouchSlop;
	private boolean mIsDownInZero;
	private boolean mInterceptTouchMoved;
	private float mInterceptTouchMoveX;
	private float mInterceptTouchMoveY;
	
	
	private EffectSettingInfo mEffectInfo;
	private boolean mIsChangeZeroEffect; //记录是否有滚动到0屏幕切换特效
	
	private ZeroScreenHandler mZeroScreenHandler;
	private NoZeroScreenHandler mNoZeroScreenHandler;

	public GLSuperWorkspace(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		mScroller = new ShellScreenScroller(context, this);
		mScroller.setCurrentScreen(1);
		mScroller.setDuration(450);
		mScroller.setBackgroundAlwaysDrawn(false);
		mDeskScreenEffector = new CoupleScreenEffector(mScroller, CoupleScreenEffector.PLACE_DESK);


		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		
		mWorkspace = new GLWorkspace(context);
		mWorkspace.setId(R.id.diyworkspace);
		addView(mWorkspace);
		mWorkspace.setScrollerAndEffector(mScroller, mDeskScreenEffector);
		mZeroScreenHandler = new ZeroScreenHandler(this);
		mNoZeroScreenHandler = new NoZeroScreenHandler(this);
		// 判断是否需开启0屏
		if (DeskSettingUtils.getIsShowZeroScreen()) {
			mZeroScreenView = new GLZeroScreenView(getContext());
			addView(mZeroScreenView, 0);
			mWorkspace.setZeroHandler(mZeroScreenHandler);
		} else {
			mWorkspace.setZeroHandler(mNoZeroScreenHandler);
		}
	}

	public GLWorkspace getGLWorkSpace() {
		return mWorkspace;
	}
	
//	public AbsZeroHandler getZerohandler() {
//		return mZeroHandler;
//	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final float x = ev.getX(); 
		final float y = ev.getY();
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN :
				if (isShowingZero()) {
					int[] location = new int[2];
					mZeroScreenView.getLocationInWindow(location);
					if (x >= location[0] && x <= location[0] + DrawUtils.sWidthPixels) {
						mIsDownInZero = true;
					} else {
						mIsDownInZero = false;
					}
				}
				mDownX = x;
				mDownY = y;
				mInterceptTouchMoveX = 0;
				mInterceptTouchMoveY = 0;
				mInterceptTouchMoved = false;
				mScroller.onTouchEvent(ev, ev.getAction());
				break;

			case MotionEvent.ACTION_MOVE :
				if (!mInterceptTouchMoved) {
					// 一旦超出拖动范围不会再更新，作为初始的拖动斜率
					mInterceptTouchMoveX = Math.abs(x - mDownX);
					mInterceptTouchMoveY = Math.abs(y - mDownY);
					mInterceptTouchMoved = mInterceptTouchMoveX > mTouchSlop
							|| mInterceptTouchMoveY > mTouchSlop;
				}
				
				if (mInterceptTouchMoved) {
					if (mIsDownInZero && mInterceptTouchMoveY < mInterceptTouchMoveX) {
						return true;
					}
				}
				break;
				
			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL :
			{
				//判断是否横向，横向才给scroller处理
				if (mIsDownInZero && (Math.abs(mLastX - mDownX) > Math.abs(mLastY - mDownY)
						&& Math.abs(mLastX - mDownX) >= mTouchSlop)) {
					mScroller.onTouchEvent(ev, ev.getAction());
				}
			}
				break;

			default :
				break;
		}
		
		mLastX = x;
		mLastY = y;
		
		// ！！把事件传给workspace,workspace用此事件来做触屏事件判断S
		MotionEvent evTmp = MotionEvent.obtain(ev);
		mWorkspace.setMOtionEventForAnalys(evTmp);
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mScroller.onTouchEvent(event, event.getAction());
		return true;
	}
	
	
	@SuppressLint("WrongCall")
	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		if (!mScroller.isFinished()) {
			mScroller.onDraw(canvas);
		} else {
			super.dispatchDraw(canvas);
		}
	}

	@Override
	public void computeScroll() {
		mScroller.computeScrollOffset();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (mWorkspace != null) {
			mWorkspace.onSizeChanged(w, h, oldw, oldh);
		} else {
			mScroller.setScreenSize(w, h);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int count = getChildCount();
		int width = right - left;
		int layoutleft = left;
		int layoutright = 0;
		
		int gap = (int) (width * (1 - mWorkspace.sLayoutScale) * mWorkspace.sLayoutScale / 2); //长按进入添加界面后的每个celllayout的间隔
		for (int i = 0; i < count; i++) {
			GLView child = getChildAt(i);
			if (child instanceof GLWorkspace) {
				layoutleft = layoutright;
				
				int childCount = mWorkspace.getChildCount();
				layoutright = layoutleft + width * childCount + gap * (childCount - 1);  //glworkspace的整体宽度需要计算长按时的每个屏幕间隔宽度。否则添加界面最后一屏获取不了点击事件
			} else {
				layoutleft = layoutright;
				layoutright = layoutleft + width;
			}
			child.layout(layoutleft, top, layoutright, bottom);
		}
		
		if (isShowingZero()) {
			mScroller.setScreenCount(mWorkspace.getChildCount() + 1);
		} else {
			mScroller.setScreenCount(mWorkspace.getChildCount());
		}
		
	}

	@Override
	public ShellScreenScroller getScreenScroller() {
		return mScroller;
	}

	@Override
	public void setScreenScroller(ShellScreenScroller scroller) {
		this.mScroller = scroller;
		mWorkspace.setScrollerAndEffector(mScroller, mDeskScreenEffector);
	}

	@Override
	public void onFlingIntercepted() {
		if (mWorkspace != null) {
			mWorkspace.onFlingIntercepted();
		}
	}

	@Override
	public void onScrollStart() {
		if (mWorkspace != null) {
			mWorkspace.onScrollStart();
		}
	}

	@Override
	public void onFlingStart() {
		if (mWorkspace != null) {
			mWorkspace.onFlingStart();
		}
	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		changeZeroScreenEffect(newScroll, oldScroll); //切换滚动到0屏幕特效
		checkZeroScreenDock(newScroll, oldScroll); //检查是否滑进0屏幕
		if (mWorkspace != null) {
			mWorkspace.onScrollChanged(newScroll, oldScroll);
		}
	}
	
	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		if (mWorkspace != null) {
			mWorkspace.onScreenChanged(newScreen, oldScreen);
		}
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		if (isShowingZero() && mZeroScreenView != null) {
			//滑动到0屏
			if (currentScreen == 0) {
				mZeroScreenView.enterToZeroScreen();
				showDockAndIndicator(1f);
			} else {
				mZeroScreenView.leaveToZeroScreen();
				showDockAndIndicator(0);
			}
			mZeroScreenView.destroyChildrenDrawingCache();
			
			
//			if (mScroller == null || mScroller.getEffector() == null) {
//				return;
//			} 
//			
//			if (mEffectInfo == null) {
//				mEffectInfo = GoSettingControler.getInstance(ApplicationProxy.getContext()).getEffectSettingInfo();
//			}
//			int dbType = mEffectInfo.mEffectorType; //真实的特效
//			ShellScreenScrollerEffector scrollerEffector =  mScroller.getEffector();
//			int scrollerType = scrollerEffector.getType();
//			//如果没有显示0屏。例如进入添加界面。要判断是否修改了0屏特效。恢复原来的特效
//			if (mIsChangeZeroEffect && scrollerType != dbType) {
//				scrollerEffector.setType(dbType);
//				mIsChangeZeroEffect = false;
//			}
			
		}

		if (mWorkspace != null) {
			mWorkspace.onScrollFinish(currentScreen);
		}
	}

	@Override
	public void drawScreen(GLCanvas canvas, int screen) {
		if (isShowingZero()) {
			if (screen == 0) {
				drawScreenZeroScreen(canvas, screen);
			} else {
				screen--;
				mWorkspace.drawScreen(canvas, screen);
			}
		} else {
			mWorkspace.drawScreen(canvas, screen);
		}
	}

	@Override
	public void drawScreen(GLCanvas canvas, int screen, int alpha) {
		if (isShowingZero()) {
			if (screen == 0) {
				drawScreenZeroScreen(canvas, screen);
			} else {
				screen--;
				mWorkspace.drawScreen(canvas, screen, alpha);
			}
		} else {
			mWorkspace.drawScreen(canvas, screen, alpha);
		}
	}
	
	@Override
	public void drawScreenCell(GLCanvas canvas, int screen, int index) {
		//因为格子特效为了适配功能表做了特殊偏移。所以需要偏移回来位置才正确。screen不需要判断减1，因为格子特效是多少个屏幕就偏移多少个屏幕
		canvas.translate(screen * getWidth(), 0); 
		if (isShowingZero()) {
			if (screen == 0) {
				drawScreenZeroScreen(canvas, screen);
			} else {
				screen--;
				index = index - mWorkspace.getGridCount();
				mWorkspace.drawScreenCell(canvas, screen, index);
			}
		} else {
			mWorkspace.drawScreenCell(canvas, screen, index);
		}
	
	}
	
	@Override
	public void drawScreenCell(GLCanvas canvas, int screen, int index, int alpha) {
		//因为格子特效为了适配功能表做了特殊偏移。所以需要偏移回来位置才正确。screen不需要判断减1，因为格子特效是多少个屏幕就偏移多少个屏幕
		canvas.translate(screen * getWidth(), 0); 
		if (isShowingZero()) {
			if (screen == 0) {
				drawScreenZeroScreen(canvas, screen);
			} else {
				screen--;
				index = index - mWorkspace.getGridCount();
				mWorkspace.drawScreenCell(canvas, screen, index, alpha);
			}
		} else {
			mWorkspace.drawScreenCell(canvas, screen, index, alpha);
		}
	
	}

	/**
	 * <br>功能简述:画0屏的特效
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param canvas
	 * @param screen
	 */
	public void drawScreenZeroScreen(GLCanvas canvas, int screen) {
		GLZeroScreenView zeroScreenView = (GLZeroScreenView) getChildAt(screen);
		if (zeroScreenView != null) {
			if (!mScroller.isFinished()) {
				zeroScreenView.buildChildrenDrawingCache(); //开启drawingcache
				zeroScreenView.draw(canvas);
			}
		}
		
//		canvas.reset();
//		final int orientation = mScroller.getOrientation();
//		final int scroll = mScroller.getScroll();
//		canvas.save();
//		if (orientation == ShellScreenScroller.HORIZONTAL) {
//			canvas.translate(-scroll, 0);
//		} else {
//			canvas.translate(0, -scroll);
//		}
//		
//		GLZeroScreenView zeroScreenView = (GLZeroScreenView) getChildAt(screen);
//		if (zeroScreenView != null) {
//			if (!mScroller.isFinished()) {
//				zeroScreenView.buildChildrenDrawingCache(); //开启drawingcache
//				zeroScreenView.draw(canvas);
//			}
//		}
//		
//		canvas.restore();
	}
	
	/**
	 * <br>功能简述:传递返回按钮时间给screenview
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void onKeyBack() {
		//是否在0屏，且0屏幕是否存在
		if (isInZeroScreen()) {
			if (mZeroScreenView != null) {
				mZeroScreenView.onKeyBack();
			}
		}
	}

	/**
	 * <br>功能简述:处理发给0屏的消息
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param who
	 * @param type
	 * @param msgId
	 * @param param
	 * @param object
	 * @param objects
	 */
	public void handMessage(Object who, int type, int msgId, int param, Object object, List objects) {
		if (isInZeroScreen()) {
			switch (param) {
			//导航区添加网址 
				case ZeroScreenParamId.SCREEN_ZERO_NAVIGATION_ADD :
					if (mZeroScreenView != null) {
						mZeroScreenView.addNewWeb(object); //导航区添加新网址
					}
					break;
				case ZeroScreenParamId.SCREEN_ZERO_NAVIGATION_SHOW :
					if (mZeroScreenView != null) {
						mZeroScreenView.showOrHideTabLayout(true, true);
					}
					break;
				case ZeroScreenParamId.SCREEN_ZERO_NAVIGATION_HIDE :
					if (mZeroScreenView != null) {
						mZeroScreenView.showOrHideTabLayout(false, true);
					}
					break;
				case ZeroScreenParamId.SCREEN_ZERO_NAVIGATION_HOME :
				if (mZeroScreenView != null) {
					if (mZeroScreenView.isShowOrHide() && !isDefault()) {
						ArrayList<ZeroScreenAdInfo> zeroScreenAdInfos = mZeroScreenView
								.getZeroScreenAdInfos();
						int position = mZeroScreenView.getClickPosition();
						if (zeroScreenAdInfos != null && position != -1) {
							Intent intent = new Intent(getContext(),
									AddSiteActivity.class);
							intent.putExtra(
									GLNavigationView.ZERO_SCREEN_AD_POSITION_ONE,
									zeroScreenAdInfos.get(0).mUrl);
							intent.putExtra(
									GLNavigationView.ZERO_SCREEN_AD_POSITION_TWO,
									zeroScreenAdInfos.get(1).mUrl);
							intent.putExtra(
									GLNavigationView.ZERO_SCREEN_AD_POSITION_THREE,
									zeroScreenAdInfos.get(2).mUrl);
							intent.putExtra(
									GLNavigationView.ZERO_SCREEN_AD_POSITION_FOUR,
									zeroScreenAdInfos.get(3).mUrl);
							intent.putExtra(
									GLNavigationView.ZERO_SCREEN_AD_POSITION,
									position);
							intent.putExtra(
									GLNavigationView.ZERO_SCREEN_AD_POSITION_FIVE,
									zeroScreenAdInfos.get(4).mUrl);
							intent.putExtra(
									GLNavigationView.ZERO_SCREEN_AD_POSITION_SIX,
									zeroScreenAdInfos.get(5).mUrl);
							getContext().startActivity(intent);
						}
					} else if (mZeroScreenView.isShowOrHideNavigationView()
							&& !isDefault()) {
						mZeroScreenView.showOrHideTabLayout(false, false);
						mZeroScreenView.cleanSearchResult();
					} else {
						mZeroScreenView.showOrHideTabLayout(true, true);
						mZeroScreenView.cleanSearchResult();
					}
				}
					break;
				default :    
					break;
			}
		} else {
			if (param == ZeroScreenParamId.SCREEN_ZERO_SEARCH_HIDE_RESULT) {
				//点击go小部件时，会跳出0屏，这时清除搜索数据
				if (mZeroScreenView != null) {
					mZeroScreenView.cleanSearchResult();
				}
			}
		}
	}

	/**
	 * <br>功能简述:是否在0屏上
	 * <br>功能详细描述:屏幕数=2 且index在0的位置
	 * <br>注意:
	 * @return
	 */
	public boolean isInZeroScreen() {
		//判断设置是否开启0屏
		if (isShowingZero()) {
			if (mScroller != null) {
				if (mScroller.getCurrentScreen() == 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * <br>功能简述:判断是否显示０屏的唯一方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean isShowingZero() {
		return getChildCount() > 0 && getChildAt(0) instanceof GLZeroScreenView;
	}
	
	/**
	 * <br>功能简述:暂时移除０屏，不清空
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean removeZeroScreen() {
		if (isShowingZero()) {
			removeView(mZeroScreenView);
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SCREEN_ZERO_SETINDICATOR, -1, false);
			mWorkspace.setZeroHandler(mNoZeroScreenHandler);
			return true;
		}
		
		return false;
	}
	
	/**
	 * <br>功能简述:把移除的０屏重新加入
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean addZeroScreen() {
		if (!isShowingZero() && mZeroScreenView != null) {
			int current = mWorkspace.getCurrentScreen();
			addView(mZeroScreenView, 0);
			mWorkspace.setZeroHandler(mZeroScreenHandler);
			mZeroScreenView.changeConfiguration(false);
			mWorkspace.updateIndicatorItems();
			mWorkspace.setCurrentScreen(current);
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SCREEN_ZERO_SETINDICATOR, -1, true);
			return true;
		}
		
		return false;
	}
	
	/**
	 * <br>功能简述:滚动到0屏
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void setIsShowZeroScreen(Object flag) {
		try {
			boolean isShow = (Boolean) flag;
			if (isShow) {
				mZeroScreenView = new GLZeroScreenView(getContext());
//				addView(mZeroScreenView, 0);
//				mZeroHandler = mZeroScreenHandler;
//				mWorkspace.setZeroHandler(mZeroHandler);
//				mScroller.gotoScreen(mScroller.getDstScreen() + 1, 0, false);
				addZeroScreen();
			} else {
				removeZeroScreen();
				if (mZeroScreenView != null) {
					mZeroScreenView.onDestory();
				}
//					//渐变指示器
//					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
//							IDiyMsgIds.DOCK_ZERO_SCREEN_MOVE, -1, 0f, null);
//					
//					//滑动隐藏dock条
//					MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK,
//							IDiyMsgIds.DOCK_ZERO_SCREEN_MOVE, -1, 0f, null);
//					mScroller.gotoScreen(mScroller.getDstScreen() - 1, 0, false);
				}
//			}
			if (mEffectInfo == null) {
				mEffectInfo = SettingProxy.getEffectSettingInfo();
			}
			int dbType = mEffectInfo.mEffectorType; // 真实的特效
			mScroller.getEffector().setType(dbType);
			requestLayout();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean isDefault() {
		PackageManager pm = getContext().getPackageManager();
		boolean isDefault = false;
		List<ComponentName> prefActList = new ArrayList<ComponentName>();
		// Intent list cannot be null. so pass empty list
		List<IntentFilter> intentList = new ArrayList<IntentFilter>();
		pm.getPreferredActivities(intentList, prefActList, null);
		if (0 != prefActList.size()) {
			for (int i = 0; i < prefActList.size(); i++) {
				if (getContext().getPackageName().equals(
						prefActList.get(i).getPackageName())) {
					isDefault = true;
					break;
				}
			}
		}
		return isDefault;
	}
	
	/**
	 * <br>功能简述:滚动到0屏
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void goToZeroScreen() {
		if (isShowingZero()) {
			mScroller.gotoScreen(0, 200, false);
		}
	}


	@Override
	public int getCellCount() {
		if (isShowingZero()) {
			return mWorkspace.getCellCount() + mWorkspace.getGridCount(); //总格数需要加多一屏 
		} else {
			return mWorkspace.getCellCount();
		}
		
	}

	@Override
	public int getCellWidth() {
		return mWorkspace.getCellWidth();
	}

	@Override
	public int getCellHeight() {
		return mWorkspace.getCellHeight();
	}

	@Override
	public int getCellRow() {
		return mWorkspace.getCellRow();
	}

	@Override
	public int getCellCol() {
		return mWorkspace.getCellCol();
	}
	
	/**
	 * 检查是否滑进0屏幕
	 */
	public void checkZeroScreenDock(int newScroll, int oldScroll) {
		//判断是否开启0屏
		if (!isShowingZero()) {
			return;
		}
		boolean isLooping = SettingProxy.getScreenSettingInfo().mScreenLooping;
		int currentScreen = mScroller.getCurrentScreen();
		int screenCount = mScroller.getScreenCount();
		if (newScroll == 0 || currentScreen  == 0 || currentScreen  == 1 || (isLooping && currentScreen == screenCount - 1)) {
			int screenWidth = GoLauncherActivityProxy.getScreenWidth();
			int moveX = newScroll;
			float hidePercent = 0;
			//是否循环滚屏
			if (isLooping) {
				int lastScreenX = (screenCount - 1) * screenWidth;
				int halfScreenWidth = screenWidth / 2;
			
				//判断是否0-最后一屏 前半部分
				if (moveX <= 0 && moveX <= halfScreenWidth) {
					hidePercent = 1 - Math.abs(moveX) / (float) screenWidth;
				}
				//判断是否0-最后一屏 后半部分
				else if (lastScreenX < moveX && moveX <= lastScreenX + halfScreenWidth) {
					hidePercent = Math.abs(moveX - lastScreenX) / (float) screenWidth;
				}
				
				//判断是否0-1屏切换
				else if (0 <= moveX && moveX <= screenWidth) {
					hidePercent = 1 - moveX / (float) screenWidth;
				}
			} else {
				//判断是否0-1屏切换
				if (moveX <= screenWidth) {
					if (moveX >= 0) {
						hidePercent = 1 - moveX / (float) screenWidth;
					} else {
						hidePercent = 1;
					}
				}
			}
			showDockAndIndicator(hidePercent);
		} else {
			showDockAndIndicator(0);
		}
	}
	
	/**
	 * <br>功能简述:设置dock条和指示器的显示位置和透明度
	 * <br>功能详细描述: 1：100%隐藏。 0：显示
	 * <br>注意:
	 * @param hidePercent
	 */
	public void showDockAndIndicator(float hidePercent) {
		
		if (mWorkspace.getShell() != null) {
			int curStage = mWorkspace.getShell().getCurrentStage();
			//判断当前是否在屏幕，因为在预览界面可能会触发
			if (curStage == IShell.STAGE_SCREEN) {
				//渐变指示器
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SCREEN_ZERO_INDICATOR_AND_DOCKMOVE, -1, hidePercent);
				if (ShortCutSettingInfo.sEnable) {
					// 滑动隐藏dock条
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.DOCK,
							IScreenFrameMsgId.SCREEN_ZERO_INDICATOR_AND_DOCKMOVE, -1,
							hidePercent);
				}
			}
		}
	}

	@Override
	public void drawScreenBackground(GLCanvas canvas, int screen) {
		mWorkspace.drawScreenBackground(canvas, screen);

	}

	@Override
	public void invalidateScreen() {
		mWorkspace.invalidateScreen();
		
	}

	@Override
	public GLView getScreenView(int screen) {
		return mWorkspace.getScreenView(screen);
	}

	@Override
	public Rect getScreenRect() {
		return mWorkspace.getScreenRect();
	}
	
	public float getTranslateY() {
		return mWorkspace.getTranslateY();
	}

	public float getTranslateZ() {
		return mWorkspace.getTranslateZ();
	}
	
	/**
	 * <br>功能简述:切换滚动到0屏幕特效
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param newScroll
	 * @param oldScroll
	 */
	public void changeZeroScreenEffect(int newScroll, int oldScroll) {
		if (mScroller == null || mScroller.getEffector() == null) {
			return;
		}

		if (isShowingZero()) {

			if (mEffectInfo == null) {
				mEffectInfo = SettingProxy.getEffectSettingInfo();
			}
			int dbType = mEffectInfo.mEffectorType; // 真实的特效
			int screenWidth = getWidth();
			ShellScreenScrollerEffector scrollerEffector = mScroller
					.getEffector();
			int scrollerType = scrollerEffector.getType();
			boolean isLooping = SettingProxy.getScreenSettingInfo().mScreenLooping;
			// 记录是否正在切换0屏。防止再第一屏左右滑动的特效会切换的bug
			// 判断是否循环滚屏切超过最后一屏
			if ((isLooping && newScroll >= (screenWidth * (mScroller
					.getScreenCount() - 1))) || newScroll <= screenWidth) {
				int zeroEffectType = IEffectorIds.EFFECTOR_TYPE_DEFAULT; // 0屏需要显示的特效
				if (scrollerType != zeroEffectType) {
					scrollerEffector.setType(zeroEffectType);
					mIsChangeZeroEffect = true; // 记录0屏切换时候更换了特效效果
				}
			} else {
				// 判断当前特效是否做了修改
				if (mIsChangeZeroEffect && scrollerType != dbType) {
					scrollerEffector.setType(dbType);
					mIsChangeZeroEffect = false;
				}
			}
		}
	}
	
	public void setEnableWidgetDrawingCache(final boolean cache) {
		mWorkspace.setEnableWidgetDrawingCache(cache);
	}
	
	public void setEnableCellLayoutDrawingCache(final boolean cache) {
		mWorkspace.setEnableCellLayoutDrawingCache(cache);
	}
	
	public void changeZeroScreenLayout() {
		if (mZeroScreenView != null) {
			mZeroScreenView.changeConfiguration(true);
		}
	}
	
	public void statusBarLayout() {
		if (mZeroScreenView != null) {
			mZeroScreenView.requestLayout();
			mZeroScreenView.invalidate();

		}
	}
}
