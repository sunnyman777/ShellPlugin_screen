package com.jiubang.shell.appdrawer.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.MsgMgrProxy;
import com.go.util.graphics.DrawUtils;
import com.golauncher.message.IAppDrawerMsgId;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.ggheart.apps.appfunc.setting.AppFuncAutoFitManager;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConfig;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.gowidget.GoWidgetProviderInfo;
import com.jiubang.ggheart.apps.gowidget.InnerWidgetInfo;
import com.jiubang.ggheart.apps.systemwidget.SystemWidgetLoader;
import com.jiubang.ggheart.components.sidemenuadvert.SideAdvertControl;
import com.jiubang.ggheart.components.sidemenuadvert.widget.SideWidgetDataInfo;
import com.jiubang.ggheart.components.sidemenuadvert.widget.SideWidgetInfo;
import com.jiubang.ggheart.components.sidemenuadvert.widget.SideWidgetSpecialInfo;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.common.component.GLScrollableBaseGrid;
import com.jiubang.shell.common.component.HorScrollableGridViewHandler;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.screen.GLWorkspace;
import com.jiubang.shell.scroller.effector.CoupleScreenEffector;

/**
 * widget管理显示界面的gird
 * 
 * @author wuziyi
 * 
 */
public class GLWidgetGrid extends GLScrollableBaseGrid {

	private boolean mLoadNextBatchIcon = true;
	
	public GLWidgetGrid(Context context) {
		super(context);
		init();
	}

	public GLWidgetGrid(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		handleScrollerSetting();
	}

	@Override
	public void onItemClick(GLAdapterView<?> parent, GLView v, int position,
			long id) {
		// 跳到对应的widget添加界面
		SideWidgetInfo info = (SideWidgetInfo) v.getTag();
		// 以下代码烂得一B，请勿学习
		if (info instanceof SideWidgetSpecialInfo) {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
					IAppDrawerMsgId.APPDRAWER_EXIT, -1, false, 0);
			SideWidgetSpecialInfo specialInfo = (SideWidgetSpecialInfo) info;
			
			ScreenEditConfig.sEXTERNAL_FROM_ID = ScreenEditConstants.EXTERNAL_ID_APPDRAWER_SLIDEMENU;
			if (specialInfo.getType() == SideWidgetSpecialInfo.SIDEWIDGET_SYSTEM_INFO) {
				if (Build.VERSION.SDK_INT < 16) {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.GOTO_SCREEN_EDIT_TAB, 
							GLWorkspace.SCREEN_TO_SMALL_LEVEL_ONE,
							ScreenEditConstants.TAB_MAIN);
				} else {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.GOTO_SCREEN_EDIT_TAB, 
							GLWorkspace.SCREEN_TO_SMALL_LEVEL_TWO,
							ScreenEditConstants.TAB_SYSTEMWIDGET);
				}
			} else if (specialInfo.getType() == SideWidgetSpecialInfo.SIDEWIDGET_GOMARKET_INFO) {
				SideWidgetSpecialInfo normalInfo = (SideWidgetSpecialInfo) info;
				
				InnerWidgetInfo innerWidgetInfo = (InnerWidgetInfo) normalInfo.getObject();
				
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
						IScreenFrameMsgId.GOTO_SCREEN_EDIT_TAB, 
						GLWorkspace.SCREEN_TO_SMALL_LEVEL_TWO,
						ScreenEditConstants.TAB_ADDGOWIDGET, innerWidgetInfo);
			}
		} else if (info instanceof SideWidgetDataInfo) {
			//点击已安装widget,直接跑到widget添加界面,需要传给一个GoWidgetProviderInfo
			SideWidgetDataInfo normalInfo = (SideWidgetDataInfo) info;
			if (normalInfo.isIsInstalled()) {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
						IAppDrawerMsgId.APPDRAWER_EXIT, -1, false, 0);
				switch (normalInfo.getType()) {
					case SideWidgetDataInfo.SIDEWIDGET_DOWNLOAD_INFO :
						boolean isWidgetApp = SystemWidgetLoader.isWidgetApp(
								ShellAdmin.sShellManager.getActivity(), normalInfo.getWidgetPkgName());
						String widgetPkgName = normalInfo.getWidgetPkgName();
						
						if (isWidgetApp) {
//							throw new IllegalAccessError("the application no widgetInfo");
							PackageManager pm = mContext.getPackageManager();
							MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
									ICommonMsgId.START_ACTIVITY, -1, pm.getLaunchIntentForPackage(normalInfo.getWidgetPkgName()), null);
						} else {
							
							ScreenEditConfig.sEXTERNAL_FROM_ID = ScreenEditConstants.EXTERNAL_ID_APPDRAWER_SLIDEMENU;
							if (Build.VERSION.SDK_INT < 16) {
								//打开列表同时弹出弹系统widget列表
								MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
										IScreenFrameMsgId.GOTO_SCREEN_EDIT_TAB, 
										GLWorkspace.SCREEN_TO_SMALL_LEVEL_ONE,
										ScreenEditConstants.TAB_MAIN, null);
							} else {
								if (widgetPkgName != null) {
									//直接跳到某个系统widget添加界面
									MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
											IScreenFrameMsgId.GOTO_SCREEN_EDIT_TAB, 
											GLWorkspace.SCREEN_TO_SMALL_LEVEL_TWO,
											ScreenEditConstants.TAB_ADDSYSTEMWIDGET, widgetPkgName);
								}
							}
						}
						break;
						
					case SideWidgetDataInfo.SIDEWIDGET_LOCALXML_INFO :
						GoWidgetProviderInfo providerInfo = new GoWidgetProviderInfo();
						providerInfo.getProviderInfo().label = normalInfo.getTitle();
						providerInfo.getProviderInfo().icon = normalInfo.getWidgetIconIDFromPkg(ShellAdmin.sShellManager.getActivity());
						providerInfo.getProviderInfo().provider = new ComponentName(info.getWidgetPkgName(), "");
						
						ScreenEditConfig.sEXTERNAL_FROM_ID = ScreenEditConstants.EXTERNAL_ID_APPDRAWER_SLIDEMENU;
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
								IScreenFrameMsgId.GOTO_SCREEN_EDIT_TAB,
								GLWorkspace.SCREEN_TO_SMALL_LEVEL_TWO,
								ScreenEditConstants.TAB_ADDGOWIDGET, providerInfo);
						break;
	
					default:
						break;
				}
			} else {
				// 只能处理下载
				normalInfo.clickSelf(ShellAdmin.sShellManager.getActivity());
			}
			
		}
	}

	@Override
	public void refreshGridView() {
		ArrayList<SideWidgetInfo> tmpList = SideAdvertControl.getAdvertControlInstance(mContext).getGoWidgetInfo();
		setData(tmpList);
	}

	@Override
	public ShellBaseAdapter createAdapter(Context context, List infoList) {
		return new GLWidgetAdapter(context, infoList);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		setVerticalSpacing(DrawUtils.dip2px(12));
		setHorizontalSpacing(DrawUtils.dip2px(15));
		handleRowColumnSetting(false);
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void handleScrollerSetting() {
		if (mScrollableHandler == null) {
			mScrollableHandler = new HorScrollableGridViewHandler(mContext,
					this, CoupleScreenEffector.PLACE_MENU, false, true) {

				@Override
				public void onEnterLeftScrollZone() {
				}

				@Override
				public void onEnterRightScrollZone() {
				}

				@Override
				public void onEnterTopScrollZone() {
				}

				@Override
				public void onEnterBottomScrollZone() {
				}

				@Override
				public void onExitScrollZone() {
				}

				@Override
				public void onScrollLeft() {
				}

				@Override
				public void onScrollRight() {
				}

				@Override
				public void onScrollTop() {
				}

				@Override
				public void onScrollBottom() {
				}
			};
		}
	}

	@Override
	public boolean onItemLongClick(GLAdapterView<?> parent, GLView view,
			int position, long id) {
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER,
					ICommonMsgId.SHOW_EXTEND_FUNC_VIEW, 0, IViewId.WIDGET_MANAGE);
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void handleRowColumnSetting(boolean updateDB) {
		Context context = ShellAdmin.sShellManager.getActivity();
		AppFuncAutoFitManager autoFitManager = AppFuncAutoFitManager
				.getInstance(context);
		int iconHeight = autoFitManager.getWidgetHeight();
		int iconWidth = autoFitManager.getWidgetWidth();
		mNumRows = (mHeight - getPaddingTop() - getPaddingBottom()) / (iconHeight + getHorizontalSpacing());
		mNumColumns = (mWidth - getPaddingLeft() - getPaddingRight()) / (iconWidth + getVerticalSpacing());
//		mNumRows = 3;
		// 最大列数不能超过3列
		mNumColumns = Math.min(3, mNumColumns);
	}

	@Override
	public void callBackToChild(GLView view) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onScrollStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onScrollFinish() {
		// TODO Auto-generated method stub
		
	}
	
	public void startLoadIcon() {
		onVisiblePositionChanged();
	}
	
	private void onVisiblePositionChanged() {
		int size = getChildCount();
		int firstVisibleIndex = getCurrentScreenFirstIndex();
		int lastVisibleIndex = getCurrentScreenLastIndex();
		int singleScreenCells = getPageItemCount();
		int preShowStartPos = firstVisibleIndex;
		int endPos = lastVisibleIndex;
		if (!mLoadNextBatchIcon) {
			if (firstVisibleIndex > singleScreenCells - 1) {
				preShowStartPos -= singleScreenCells;
				if (preShowStartPos < 0) {
					preShowStartPos = 0;
				}
			}
		}
		int preShowEndPos = endPos;
		if (lastVisibleIndex < size - 1) {
//			endPos--;
			if (mLoadNextBatchIcon) {
				preShowEndPos += singleScreenCells;
				if (preShowEndPos >= size) {
					preShowEndPos = size - 1;
				}
			}
		}
		for (int index = 0; index < size; index++) {
			GLWidgetView widgetView = (GLWidgetView) getChildAt(index);
			if (index >= preShowStartPos && index < firstVisibleIndex) {
				widgetView.preShow();
			} else if (index >= firstVisibleIndex && index <= endPos) {
				widgetView.onShow();
			} else if (index > endPos && index <= preShowEndPos) {
				widgetView.preShow();
			} else {
				widgetView.onHide();
			}
		}
	}

	@Override
	protected void onScreenChange(int newScreen, int oldScreen) {
		int totalScreen = ((HorScrollableGridViewHandler) mScrollableHandler).getTotalScreen();
		if (oldScreen == 0 && newScreen == totalScreen - 1) {
			mLoadNextBatchIcon = false;
		} else if (oldScreen == totalScreen - 1 && newScreen == 0) {
			mLoadNextBatchIcon = true;
		} else if (newScreen > oldScreen) {
			mLoadNextBatchIcon = true;
		} else {
			mLoadNextBatchIcon = false;
		}
		onVisiblePositionChanged();
	}
}

