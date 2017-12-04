package com.jiubang.shell.dock;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import android.view.animation.DecelerateInterpolator;

import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.graphics.ColorGLDrawable;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.util.BroadCaster.BroadCasterObserver;
import com.golauncher.message.IAppCoreMsgId;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IDockMsgId;
import com.golauncher.message.IFolderMsgId;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DockConstants;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockLogicControler;
import com.jiubang.ggheart.apps.desks.diy.frames.tipsforgl.GuideControler;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.DataType;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.GestureSettingInfo;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.IShell;
import com.jiubang.shell.IView;
import com.jiubang.shell.animation.BackgroundAnimation;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.appdrawer.animation.AnimationFactory;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.dock.business.DockQuickActionMenuBusiness;
import com.jiubang.shell.dock.component.GLDockLineLayout;
import com.jiubang.shell.dock.component.GLDockLineLayoutContainer;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.drag.DragController.DragListener;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.drag.DropTarget;
import com.jiubang.shell.folder.GLDockFolderIcon;
import com.jiubang.shell.screen.CellUtils;
import com.jiubang.shell.screen.GLCellLayout;
import com.jiubang.shell.theme.GLDockThemeControler;
import com.jiubang.shell.utils.GaussianBlurEffectUtils;

/**
 * 
 * @author dingzijian
 * 
 */
//CHECKSTYLE:OFF
public class GLDock extends GLViewGroup
		implements
			IView,
			IMessageHandler,
			DragSource,
			DropTarget,
			DragListener,
			BroadCasterObserver,
			BackgroundAnimation {

	private final static int DRAW_STATUS_NORMAL = 1; // 正常状态
	private final static int HANDLE_INIT_DOCK_FRAME = 0; // 初始化
	private final static int HANDLE_DESK_THEME_CHANGED = 1; // 主题改变

	protected GLDockLineLayoutContainer mGLDockLineLayoutContainer; // 3条LineLayout的父容器

	private DockLogicControler mDockControler; // 逻辑控制器

	protected int mDockBgHeight; // dock栏的背景高度

	protected int mLayoutH; // 当前排版区域高,与横竖屏相关

	protected int mLayoutW; // 当前排版区域宽,与横竖屏相关

	protected int mIconPortraitH; // 竖屏图标高,即LineLayout的行高

	protected int mIconLandscapeW; // 横屏图标宽，即LineLayout的列宽

	protected int mDrawStatus = DRAW_STATUS_NORMAL; // 绘制状态

	private IShell mShell;

	private boolean mIsAsycnLoadFinished = false; // 标志dockview是否初始化完全(启动桌面时，异步拿后台数据)

	private GLDockThemeControler mThemeControler;

	private static final int ALPHA_FULL = 255;

	private int mChangeAlpha = ALPHA_FULL;

	private int mVisibleState = GLView.VISIBLE;

	private boolean mIsInOutAnimating = false;

	public boolean mIsRedBg = false;	//是否需要添加红色背景

	public static final boolean ISBIGSCRENPHONE = false;
	
	public static int sZeroScreenDockTranslate; 

	private GLDockDragListener mDockDragListener;

	private ColorGLDrawable mRedDrawable; //用于绘制红色背景

	public GLDock(Context context) {
		super(context);
		mContext = context;
		init();
	}

	private void init() {
		SettingProxy.getInstance(ApplicationProxy.getContext()).registerObserver(this);
		mGLDockLineLayoutContainer = new GLDockLineLayoutContainer(mContext);
		mDockBgHeight = DockUtil.getBgHeight();
		mIconPortraitH = mDockBgHeight;
		mIconLandscapeW = mDockBgHeight;

		mDockControler = DockLogicControler.getInstance();
		mThemeControler = GLDockThemeControler.getInstance(mContext);
		setHasPixelOverlayed(false);
		mRedDrawable = new ColorGLDrawable(Color.parseColor("#4cff0000"));
		MsgMgrProxy.registMsgHandler(this);
		addView(mGLDockLineLayoutContainer);
	}


	/**
	 * <br>功能简述:设置背景红色
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param canvas
	 */
	public void setRedBg(GLCanvas canvas) {
		if (mIsRedBg) {
			if (mRedDrawable != null) {
				if (GoLauncherActivityProxy.isPortait()) {
					mRedDrawable.setBounds(0, mLayoutH - mDockBgHeight, mLayoutW, mLayoutH);
				} else {
					int left = mLayoutW - mDockBgHeight;
					int right = mLayoutW;
					int top = 0;
					int bottom = mLayoutH;

					mRedDrawable.setBounds(left, top, right, bottom);
				}
			}
			canvas.drawDrawable(mRedDrawable);
		}
	}
	@Override
	public void setDragController(DragController dragController) {
		mGLDockLineLayoutContainer.setDragController(dragController);
		dragController.addDragListener(this);
	}

	@Override
	public boolean handleMessage(Object sender, int msgId, int param, Object... objects) {
		switch (msgId) {
			case IAppCoreMsgId.EVENT_LOAD_FINISH :
				mHandler.sendEmptyMessage(HANDLE_INIT_DOCK_FRAME);
				break;
			case IAppCoreMsgId.EVENT_THEME_CHANGED:
				mHandler.sendEmptyMessage(HANDLE_DESK_THEME_CHANGED);
				break;
			case ICommonMsgId.CHANGE_ICON_STYLE:
				Bundle bundle = (Bundle) objects[0];
				DockQuickActionMenuBusiness actionMenuBusiness = new DockQuickActionMenuBusiness();
				actionMenuBusiness.actionChangeIcon(bundle, getCurretnIcon());
				break;
			case IAppCoreMsgId.EVENT_UNINSTALL_APP :
				if (objects[1] instanceof ArrayList<?> && mIsAsycnLoadFinished) {
					mDockControler.unInstallApp((ArrayList<AppItemInfo>) objects[1]);
				}
				break;
			case IAppCoreMsgId.EVENT_UNINSTALL_PACKAGE:
				if (objects[0] instanceof String && mIsAsycnLoadFinished) {
					mDockControler.handleEventUninstallPackage((String) objects[0]);
				}
				break;
			case IAppCoreMsgId.EVENT_CHANGE_APP:
				if (param == AppDataEngine.EVENT_CHANGE_APP_DISABLE
						|| param == AppDataEngine.EVENT_CHANGE_APP_COMPONENT) {
					if (objects[1] instanceof ArrayList<?> && mIsAsycnLoadFinished) {
						ArrayList<AppItemInfo> infoList = new ArrayList<AppItemInfo>(1);
						infoList.add(((ArrayList<AppItemInfo>) objects[1]).get(0));
						mDockControler.unInstallApp(infoList);
					}
				}
				break;
			case IAppCoreMsgId.EVENT_UPDATE_PACKAGE:
			case IAppCoreMsgId.EVENT_UPDATE_APP:
				if (objects != null && objects[1] != null) {
					List<List<AppItemInfo>> data = (List<List<AppItemInfo>>) objects[1];
					if (data.get(1) != null) {
						ArrayList<AppItemInfo> disableList = (ArrayList<AppItemInfo>) data.get(1);
						if (!disableList.isEmpty()) {
							mDockControler.unInstallApp(disableList);
						}
					}
				}
				break;
			case IAppCoreMsgId.EVENT_REFLUSH_TIME_IS_UP : {
				mDockControler.reloadFolderContent();
				mDockControler.handleEventReflushTimeIsUp();
			}
				break;

			case IAppCoreMsgId.EVENT_REFLUSH_SDCARD_IS_OK : {
				mDockControler.reloadFolderContent();
				mDockControler.handleEventReflashSdcardIsOk();
			}
				break;

			case IAppCoreMsgId.EVENT_SD_MOUNT : {
				mDockControler.reloadFolderContent();
				mDockControler.handleEventSdMount();
			}
				break;
			case ICommonMsgId.COMMON_ON_HOME_ACTION : {
				if (isVisible()) {
					return onHomeAction((GestureSettingInfo) objects[0]);
				}
				break;
			}
			case ICommonMsgId.COMMON_IMAGE_CHANGED : {
				mGLDockLineLayoutContainer.reloadIconRes();
			}
				break;
			case IDockMsgId.DOCK_ADD_SHORTCUT:
			case IDockMsgId.DOCK_ADD_APPLICATION:
				if (mDockDragListener != null) {
					mDockDragListener.onAddApplictionOrShortCut(objects[0]);
				}
				break;
			case IDockMsgId.DOCK_ADD_ICON_ADD_ONE:
				if (mDockDragListener != null) {
					mDockDragListener.onAddAppFromLongClickBlank(param, objects[0]);
				}
				break;
			case IDockMsgId.DOCK_SETTING_CHANGED_ROW:
				doWithRowChange();
				break;
			case IDockMsgId.DOCK_SETTING_NEED_UPDATE:
			case IDockMsgId.DOCK_SETTING_CHANGED:
				doWithCyleSettingChange();
				break;
			case IDockMsgId.DOCK_SETTING_CHANGED_STYLE:
				changeStyle(objects[0]);
				break;
			case IDockMsgId.DOCK_RESET_DEFAULT:
				resetToDefaultIcon();
				break;
			case IDockMsgId.DOCK_APP_UNINSTALL_NOTIFICATION:
				break;
			case IDockMsgId.DOCK_SHOW:
				SettingProxy.updateEnable(true);
				boolean justUpdateDB = false;
				if (objects.length > 0 && objects[0] instanceof Boolean) {
					justUpdateDB = (Boolean) objects[0];
				}
				if (!justUpdateDB) {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_NEED_TO_LAYOUT_BY_DOCK, 1);
					if (param == DockConstants.HIDE_ANIMATION) {
						setVisible(true, true, null);
					} else {
						setVisible(true, false, null);
					}
				}
				break;
			case IDockMsgId.DOCK_HIDE:
				justUpdateDB = false;
				if (objects.length > 0 && objects[0] instanceof Boolean) {
					justUpdateDB = (Boolean) objects[0];
				}
				if (!justUpdateDB) {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_NEED_TO_LAYOUT_BY_DOCK, 0);
					if (param == DockConstants.HIDE_ANIMATION) {
						setVisible(false, true, null);
					} else {
						setVisible(false, false, null);
					}
				}
				SettingProxy.updateEnable(false);
				break;
			case IDockMsgId.DOCK_ADD_APPLICATION_GESTURE:
				Object objectApp = objects[0];
				if (getCurretnIcon() != null && (objectApp != null)
						&& (objectApp instanceof ShortCutInfo)) {
					ShortCutInfo info = (ShortCutInfo) objectApp;
					IconView view = getCurretnIcon();
					if (null != view.getInfo()) {
						DockItemInfo dockItemInfo = (DockItemInfo) view.getInfo();
						if (null != dockItemInfo) {
							mDockControler.changeApp(dockItemInfo.mItemInfo.mInScreenId, info,
									DockUtil.CHANGE_FROM_GESTURE);
						}
					}
				}

				break;
			case IDockMsgId.DOCK_ADD_SHORTCUT_FOR_GESTURE:
				Object objectShort = objects[0];
				if (getCurretnIcon() != null && (objectShort != null)
						&& (objectShort instanceof ShortCutInfo)) {
					ShortCutInfo info = (ShortCutInfo) objectShort;
					IconView view = getCurretnIcon();
					if (null != view.getInfo()) {
						DockItemInfo dockItemInfo = (DockItemInfo) view.getInfo();
						if (null != dockItemInfo) {
							mDockControler.changeApp(dockItemInfo.mItemInfo.mInScreenId, info,
									DockUtil.CHANGE_FROM_GESTURE);
						}
					}
				}

				break;
			case IFolderMsgId.ON_FOLDER_DROP_COMPELETE : {
				Object target = objects[0];
				Object dragInfo = objects[1];
				boolean success = (Boolean) objects[2];
				DropAnimationInfo resetInfo = (DropAnimationInfo) objects[3];
				Object dragView = objects[4];
				if (mDockDragListener != null) {
					mDockDragListener.onDockFolderDropComplete(sender, target, dragInfo, success,
							resetInfo, dragView);
				}
			}
				break;
			case IDockMsgId.DOCK_FOLDER_ON_DRAG_FLING : {
				if (objects != null && objects.length > 1) {
					final ItemInfo dragInfo = (ItemInfo) objects[0];
					final GLDockFolderIcon folderIcon = (GLDockFolderIcon) objects[1];
					if (mDockDragListener != null) {
						mDockDragListener.onDragFingDockFolderIcon(dragInfo, folderIcon);
					}
				}
			}
				break;
			case IDockMsgId.DOCK_ON_DRAG_FLING : {
				if (objects != null && objects.length > 0) {
					if (objects[0] != null && objects[0] instanceof DockItemInfo) {
						DockItemInfo info = (DockItemInfo) objects[0];
						Long id = info.mItemInfo.mInScreenId;
						if (mDockDragListener != null) {
							mDockDragListener.onDragFingDockIcon(info, id);
						}
					}
				}
			}
				break;
			case IDockMsgId.DOCK_ON_SCREEN_FOLDER_OPEN :
			case IDockMsgId.DOCK_ON_SCREEN_FOLDER_CLOSE :
				setAlpha(param);
				break;
			//??????解决横屏同时响应挤压与进入屏幕预览界面 导致UI不对的问题
			//			case IScreenFrameMsgId.ICON_FROM_SCREEN_TO_DOCK_TO_SCRENN :
			//				mDockDragHander.hideFolder();
			//				clearMoveToScreenAnim();
			//				if (!mIsFromDockDrag) {
			//					mCurDockViewList = mIconViewsHashMap.get(curRow);
			//					int count = mCurDockViewList.size();
			//					if (count < 5) {
			//						mHandler.sendEmptyMessage(NEED_TO_LAYOUT);
			//					}
			//				}
			//				break;
			case IFolderMsgId.FOLDER_APP_LESS_TWO :
				if (mDockDragListener != null) {
					mDockDragListener.onDockFolderIconLessTwo(objects);
				}
				break;
			//设置红色背景
			case IDockMsgId.DOCK_ADD_ICON_RED_BG: {
				if (param == 1) {
					mIsRedBg = true;;
				} else {
					mIsRedBg = false;
				}
				invalidate();
			}
				break;
			case IDockMsgId.DELETE_DOCK_ITEM:
				if (mDockDragListener != null) {
					mDockDragListener.onDockAppDeleted((Long) objects[0]);
				}
				//				delDockItem((Long) objects[0], false);
				break;
			case ICommonMsgId.COMMON_FULLSCREEN_CHANGED : {
				if (!GoLauncherActivityProxy.isPortait()) {
					requestLayout();
					invalidate();
				}
			}
			break;
			//0屏移动隐藏效果	
			case IScreenFrameMsgId.SCREEN_ZERO_INDICATOR_AND_DOCKMOVE :
				float movePercent = (Float) objects[0];
				sZeroScreenDockTranslate = (int) (mDockBgHeight * movePercent);
				int state = GLView.VISIBLE;
				if (movePercent == 1f) {
					state = GLView.INVISIBLE;
				}
				
				if (getVisibility() != state) {
					setVisibility(state);
				}
				invalidate();
				break;
				
			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED : {
				//横竖屏幕数据重新绑定一次
				mGLDockLineLayoutContainer.onConfigurationChanged();
				if (null != getCurretnIcon()) {
					getCurretnIcon().setVisibility(GLView.VISIBLE);
				}
			}
				break;
			case IDockMsgId.DOCK_START_IOS_ANIMATION : {
				startIOSAnimation();
			}
				break;
			case IDockMsgId.DOCK_ENABLE_BLUR : {
				GaussianBlurEffectUtils
						.enableBlurWithZoomOutAnimation(getCurDockLineLayout(), null);
			}
				break;
			case IDockMsgId.DOCK_DISABLE_BLUR : {
				if (param == 1) {
					GaussianBlurEffectUtils.disableBlurWithZoomInAnimation(getCurDockLineLayout(),
							null);
				} else {
					GaussianBlurEffectUtils.disableBlurWithoutAnimation(getCurDockLineLayout());
				}
			}
				break;
			default :
				break;
		}
		return false;
	}

	/**
	 * 处理数据改变
	 * 
	 * @param dataType
	 *            改变的数据类型
	 * @return 是否已处理
	 */
	private boolean handleAppCoreChange(int dataType, Object object) {
		boolean ret = false;
		switch (dataType) {
			
			case DataType.DATATYPE_DESKTOPSETING : {
				requestLayout();
			}
				break;
			default :
				break;
		}
		return ret;
	}
	
	private boolean onHomeAction(GestureSettingInfo gestureSettingInfo) {
		if (isVisible()) {
			DockQuickActionMenuBusiness actionMenuBusiness = new DockQuickActionMenuBusiness();
			return actionMenuBusiness.hideQuickActionMenu(false);
		}
		return false;
	}

	@Override
	public int getMsgHandlerId() {
		return IDiyFrameIds.DOCK;
	}

	/***
	 * DOCK风格更改了
	 * 
	 * @param object
	 */
	private void changeStyle(Object object) {
		if (object instanceof String) {
			String style = (String) object;
			// 5个特殊图标
			mThemeControler.useStyleForSpecialIcons(style);
		}
	}

	/**
	 * 设置项发生改变，忽略是否是自适应模式
	 */
	private void doWithCyleSettingChange() {
		if (mGLDockLineLayoutContainer != null) {
			mGLDockLineLayoutContainer.setCycle(getSettingInfo().mAutoRevolve); // 设置循环模式
		}
	}
	/**
	 * 修改设置里面的行数
	 * 
	 * @throws IllegalAccessException
	 */
	private void doWithRowChange() throws IllegalArgumentException {
		// 1:infoMap改变
		mDockControler.doWithRowChange();
		ShortCutSettingInfo settingInfo = SettingProxy.getShortCutSettingInfo();
		int numOfRowInSetting = settingInfo.mRows;
		if (numOfRowInSetting <= 0 || numOfRowInSetting > DockUtil.TOTAL_ROWS) {
			throw new IllegalArgumentException("setting row is wrong.row = " + numOfRowInSetting);
		}
		mGLDockLineLayoutContainer.bindDockIconData(mDockControler.getShortCutItems());
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case HANDLE_INIT_DOCK_FRAME :
					mIsAsycnLoadFinished = false;
					// 5个特殊图标，使用主题
					mThemeControler.useStyleForSpecialIcons(getSettingInfo().mStyle);
					mGLDockLineLayoutContainer.bindDockIconData(mDockControler.getShortCutItems());
					if (!ShortCutSettingInfo.sEnable) {
						setVisible(false, false, null);
					}
					mIsAsycnLoadFinished = true;
					break;
				case HANDLE_DESK_THEME_CHANGED :
					doThemeChanged();
					break;
				default :
					break;
			}
		}
	};

	private void doThemeChanged() {
		mDockControler.reloadFolderContent();
		doSettingChange();
		invalidate();
	}

	private void doSettingChange() {
		SettingProxy.updateShortcutSettingInfo();
		mThemeControler.useStyleForSpecialIcons(getSettingInfo().mStyle);
		if (mGLDockLineLayoutContainer != null) {
			mGLDockLineLayoutContainer.setCycle(getSettingInfo().mAutoRevolve); // 设置循环模式
		}
		mDockControler.controlNotification();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		mLayoutH = b - t;
		mLayoutW = r - l;
		if (GoLauncherActivityProxy.isPortait()) {
			layoutPort(changed, l, t, r, b);
		} else {
			layoutLand(changed, l, t, r, b);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		measureChildren(widthMeasureSpec, heightMeasureSpec);

		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

		int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
			throw new RuntimeException("CellLayout cannot have UNSPECIFIED dimensions");
		}
	}

	protected void layoutPort(boolean changed, int l, int t, int r, int b) {
		final int top = b - mIconPortraitH;
		if (SettingProxy.getDesktopSettingInfo().getMarginEnable()) {
			mGLDockLineLayoutContainer.layout(l + CellUtils.sLeftGap, top, r
					- CellUtils.sRightGap, b);
		} else {
			mGLDockLineLayoutContainer.layout(l, top, r, b);
		}

	}

	protected void layoutLand(boolean changed, int l, int t, int r, int b) {
		final int left = r - mIconLandscapeW;
		if (!StatusBarHandler.isHide()) {
			t = t + StatusBarHandler.getStatusbarHeight();
		}
		mGLDockLineLayoutContainer.layout(left, t, r, b);
	}

	@Override
	public void dispatchDraw(GLCanvas canvas) {
		final int oldAlpha = canvas.getAlpha();
		if (mChangeAlpha != ALPHA_FULL) {
			canvas.multiplyAlpha(mChangeAlpha);
		}
		switch (mDrawStatus) {
			case DRAW_STATUS_NORMAL :
				dispatchDrawNormal(canvas);
				break;
			default :
				break;
		}
		canvas.setAlpha(oldAlpha);
	}

	/**
	 * 绘制普通状态
	 * 
	 * @param canvas
	 */
	protected void dispatchDrawNormal(GLCanvas canvas) {
		setRedBg(canvas);
		super.dispatchDraw(canvas);
	}

	public void setDockViewTag(IconView<?> v) {
		DockItemInfo dockItemInfo = (DockItemInfo) v.getInfo();
		ItemInfo itemInfo = dockItemInfo.mItemInfo;
		v.setTag(itemInfo);
	}

	protected void setDrawStatus(int status) {
		mDrawStatus = status;
	}

	/**
	 * 获取LineLayout的父容器
	 */
	private GLDockLineLayoutContainer getLineLayoutContainer() {
		if (mGLDockLineLayoutContainer == null) {
			mGLDockLineLayoutContainer = new GLDockLineLayoutContainer(mContext);
		}
		return mGLDockLineLayoutContainer;
	}

	/***
	 * 设置信息
	 * 
	 * @return
	 */
	private ShortCutSettingInfo getSettingInfo() {
		return SettingProxy.getShortCutSettingInfo();
	}

	@Override
	public void setShell(IShell shell) {
		mShell = shell;
		mGLDockLineLayoutContainer.setIshell(mShell);
	}

//	public void onClick(GLView v) {
//		
//		if (v instanceof IconView) {
//			//判断是否全部显示dock条。因为0屏滑动的时候不能点击dock条
//			if (sZeroScreenDockTranslate != 0) {
//				return;
//			}
//			
//			final IconView<?> view = (IconView<?>) v;
//			final DockItemInfo info = (DockItemInfo) view.getInfo();
//			if (null != info) {
//				if (view instanceof GLDockFolderIcon) {
//					
//					((IconView<?>) view).startClickEffect(new EffectListener() {
//						
//						@Override
//						public void onEffectStart(Object object) {
//						}
//						
//						@Override
//						public void onEffectComplete(Object object) {
//							((BaseFolderIcon<?>) view).openFolder();
//						}
//					}, IconEffect.ANIM_DURATION_CLICK, false);
//					
//				} 
//			}
//			
//				else {
//					
//					if (isFuncApp(info)) {
//						startDockApp(view, info);
//					} else if (isBrowserApp(info)) {
//						PreferencesManager ps = new PreferencesManager(mContext);
//						int openCount = ps.getInt(IPreferencesIds.PREFERENCES_OPEN_BROWSER_COUNT, 0);
//						if (40 > openCount && Machine.isNetworkOK(mContext)) {
//							ps.putInt(IPreferencesIds.PREFERENCES_OPEN_BROWSER_COUNT, ++openCount);
//							if (openCount == 2 || openCount == 20 || openCount == 40) {
//								ps.putBoolean(IPreferencesIds.PREFERENCES_BROWSER_ADVERT_NEED_SHOW, true);
//							}
//							ps.commit();
//							AppInvoker.sIsClickFromDockBrowser = true;
//						}
//						GuiThemeStatistics.guiStaticData(51, null, "browser", 1, "-1", "-1", "-1", "-1");
//
//						view.startClickEffect(new EffectListener() {
//
//							@Override
//							public void onEffectComplete(Object callBackFlag) {
//								startDockApp(view, info);
//							}
//
//							@Override
//							public void onEffectStart(Object callBackFlag) {
//							}
//						}, IconEffect.ANIM_DURATION_CLICK, false);
//
//					} else {
//						view.startClickEffect(new EffectListener() {
//
//							@Override
//							public void onEffectComplete(Object callBackFlag) {
//								startDockApp(view, info);
//							}
//						}；
//					}
//				}
//		}
//		}
						
	@Override
	public boolean onDrop(DragSource source, final int x, final int y, int xOffset, int yOffset,
			final DragView dragView, Object dragInfo, final DropAnimationInfo resetInfo) {
		if (mDockDragListener != null) {
			return mDockDragListener.onDrop(source, x, y, xOffset, yOffset, dragView, dragInfo,
					resetInfo);
		}
		return false;
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		if (mDockDragListener != null) {
			mDockDragListener.onDragEnter(source, x, y, xOffset, yOffset, dragView, dragInfo);
		}

	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		if (mDockDragListener != null) {
			mDockDragListener.onDragOver(source, x, y, xOffset, yOffset, dragView, dragInfo);
		}
	}

	@Override
	public void onDragExit(DragSource source, DropTarget nextTarget, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		if (mDockDragListener != null) {
			mDockDragListener.onDragExit(source, nextTarget, x, y, xOffset, yOffset, dragView,
					dragInfo);
		}

	}

	@Override
	public void onDragMove(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
	}

	@Override
	public void setTopViewId(int id) {

	}

	@Override
	public int getTopViewId() {
		return IViewId.DOCK;
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		return true;
	}

	@Override
	public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, Rect recycle) {
		return null;
	}

	@Override
	public void onDropCompleted(final DropTarget target, Object dragInfo, boolean success,
			DropAnimationInfo resetInfo) {
		if (mDockDragListener != null) {
			mDockDragListener.onDropCompleted(target, dragInfo, success, resetInfo);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (!isVisible() || mIsInOutAnimating) {
			return true;
		} else {
			return super.dispatchTouchEvent(ev);
		}
	}
	@Override
	public void setVisible(boolean visible, boolean animate, Object obj) {
		if (!ShortCutSettingInfo.sEnable || isVisible() == visible) {
			return;
		}
		if (mIsInOutAnimating) {
			if ((visible && mVisibleState == GLView.INVISIBLE)
					|| (!visible && mVisibleState == GLView.VISIBLE)) {
				clearAnimation();
			}
		}
		mVisibleState = visible ? GLView.VISIBLE : GLView.INVISIBLE;
		// 显示dock
		if (visible) {
			if (animate) {
				mIsInOutAnimating = true;
				setVisible(visible);
				int animationType;
				AnimationSet animationSet = null;
				if (obj instanceof Integer && obj != null) {
					animationType = (Integer) obj;
				} else if (obj instanceof Object[] && ((Object[]) obj).length > 0
						&& ((Object[]) obj)[0] instanceof Integer) {
					animationType = (Integer) ((Object[]) obj)[0];
				} else {
					animationType = AnimationFactory.SHOW_ANIMATION;
				}
				animationSet = AnimationFactory.getPopupAnimation(animationType,
						AnimationFactory.DEFAULT_DURATION, mDockBgHeight, true);
				AnimationTask task = new AnimationTask(this, animationSet, new AnimationListenerAdapter() {

					@Override
					public void onAnimationEnd(Animation animation) {
						mIsInOutAnimating = false;

					}
				}, true, AnimationTask.PARALLEL);
				GLAnimationManager.startAnimation(task);
			} else {
				// 没动画
				// 直接执行
				clearAnimation();
				setVisible(true);
				mIsInOutAnimating = false;
			}
		}
		// 隐藏dock
		else {
			// 有动画
			// 应该放到onAnimationEnd的方法执行
			if (animate) {
				mIsInOutAnimating = true;
				int animationType;
				AnimationSet animationSet = null;
				if (obj instanceof Integer && obj != null) {
					animationType = (Integer) obj;
				} else if (obj instanceof Object[] && ((Object[]) obj).length > 0
						&& ((Object[]) obj)[0] instanceof Integer) {
					animationType = (Integer) ((Object[]) obj)[0];
				} else {
					animationType = AnimationFactory.HIDE_ANIMATION;
				}
				animationSet = AnimationFactory.getPopupAnimation(animationType,
						AnimationFactory.DEFAULT_DURATION, mDockBgHeight, true);
				AnimationTask task = new AnimationTask(this, animationSet, new AnimationListenerAdapter() {

					@Override
					public void onAnimationEnd(Animation animation) {
						post(new Runnable() {

							@Override
							public void run() {
								if (mVisibleState == GLView.INVISIBLE) {
									setVisible(false);
								}
								mIsInOutAnimating = false;
							}
						});
					}
				}, true, AnimationTask.PARALLEL);
				GLAnimationManager.startAnimation(task);
			} else {
				// 没动画
				// 直接执行
				clearAnimation();
				setVisible(false);
				mIsInOutAnimating = false;

			}
		}
	}

	@Override
	public int getViewId() {
		return IViewId.DOCK;
	}

	@Override
	public void onAdd(GLViewGroup parent) {

	}

	@Override
	public void onRemove() {

	}

	@Override
	public void getHitRect(Rect outRect) {

		if (GoLauncherActivityProxy.isPortait()) {
			outRect.left = 0;
			outRect.top = mLayoutH - mDockBgHeight;
			outRect.right = mLayoutW;
			outRect.bottom = mLayoutH;
		} else {
			outRect.left = mLayoutW - mDockBgHeight;
			outRect.top = 0;
			outRect.right = mLayoutW;
			outRect.bottom = mLayoutH;
		}

		// super.getHitRect(outRect);
	}

	public void resetToDefaultIcon() {
		mDockControler.resetDockItemIcon((DockItemInfo) getCurretnIcon().getInfo());
	}

	@Override
	public void setAlpha(int alpha) {
		mChangeAlpha = alpha;
	}

	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		if (mDockDragListener != null) {
			mDockDragListener.onDragStart(source, info, dragAction);
		}

	}

	@Override
	public void onDragEnd() {
		if (mDockDragListener != null) {
			mDockDragListener.onDragEnd();
		}
	}

	public IconView<?> getCurretnIcon() {
		return mGLDockLineLayoutContainer.getCurrentIcon();
	}

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		mGLDockLineLayoutContainer.cancelLongPress();
	}
	/**
	 * 
	 * @author dingzijian
	 *
	 */
	public interface GLDockDragListener {
		public boolean onDrop(DragSource source, final int x, final int y, int xOffset,
				int yOffset, final DragView dragView, Object dragInfo,
				final DropAnimationInfo resetInfo);

		public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
				DragView dragView, Object dragInfo);

		public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
				DragView dragView, Object dragInfo);

		public void onDragExit(DragSource source, DropTarget nextTarget, int x, int y, int xOffset,
				int yOffset, DragView dragView, Object dragInfo);

		public void onDragMove(DragSource source, int x, int y, int xOffset, int yOffset,
				DragView dragView, Object dragInfo);

		public void onDropCompleted(final DropTarget target, Object dragInfo, boolean success,
				DropAnimationInfo resetInfo);

		public void onDragStart(DragSource source, Object info, int dragAction);

		public void onDragEnd();

		public void onAddApplictionOrShortCut(Object object);

		public void onDockFolderDropComplete(Object sender, Object target, Object dragInfo, boolean success,
				DropAnimationInfo resetInfo, Object dragView);

		public void onDragFingDockFolderIcon(ItemInfo dragInfo, GLDockFolderIcon folderIcon);

		public void onDragFingDockIcon(DockItemInfo info, long id);

		public void onDockFolderIconLessTwo(Object[] deleteInfo);

		public void onDockAppDeleted(long id);

		public void onAddAppFromLongClickBlank(int index, Object info);

		public GLDockLineLayout getCurDockLineLayout();
	}

	public void setGLDockDragListener(GLDockDragListener mDockDragListener) {
		this.mDockDragListener = mDockDragListener;
	}

	private GLDockLineLayout getCurDockLineLayout() {
		if (mDockDragListener != null) {
			return mDockDragListener.getCurDockLineLayout();
		}
		return null;
	}

	public void hideBgAnimation(int type, GLView glView, Object... params) {
		getCurDockLineLayout().hideBgAnimation(type, glView, params);
	}

	public void showBgAnimation(int type, GLView glView, Object... params) {
		getCurDockLineLayout().showBgAnimation(type, glView, params);
	}

	@Override
	protected void onVisibilityChanged(GLView changedView, int visibility) {
		if (changedView == this) {
			GuideControler guideControler = GuideControler.getInstance(mContext);
			if (visibility == GONE || visibility == INVISIBLE) {
				guideControler.hideCloudViewById(GuideControler.CLOUD_ID_DOCK_GESTURE);
			} else {
				guideControler.reshowCloudViewById(GuideControler.CLOUD_ID_DOCK_GESTURE);
			}
		}
	}
	
	private void startIOSAnimation() {
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.setInterpolator(new DecelerateInterpolator(0.8f));
		
		long duration = getIOSAnimDuration() + 200;
		TranslateAnimation translateAnimation;
		if (GoLauncherActivityProxy.isPortait()) {
			translateAnimation = new TranslateAnimation(0, 0, mDockBgHeight, 0);
		} else {
			translateAnimation = new TranslateAnimation(mDockBgHeight, 0, 0, 0);
		}
		translateAnimation.setDuration(duration);
		animationSet.addAnimation(translateAnimation);

		Animation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
		alphaAnimation.setDuration(duration);
		animationSet.addAnimation(alphaAnimation);
		
		startAnimation(animationSet);
	}
	
	private long getIOSAnimDuration() {
		int weight = 0;
		if (GLCellLayout.sRows % 2 == 0) {
			weight += GLCellLayout.sRows / 2;
		} else {
			weight += GLCellLayout.sRows / 2 + 1;
		}
		
		if (GLCellLayout.sColumns % 2 == 0) {
			weight += GLCellLayout.sColumns / 2;
		} else {
			weight += GLCellLayout.sColumns / 2 + 1;
		}
		
		weight = weight / 2 - 1;
		
		return 200 + weight * 200 + weight * 120;
	}

	@Override
	public void onBCChange(int msgId, int param, Object... objects) {
		switch (msgId) {
			case IAppCoreMsgId.APPCORE_DATACHANGE : {
				handleAppCoreChange(param, objects[0]);
				break;
			}
			default :
				break;
		}
	}

	@Override
	public void showBgWithoutAnimation(int type, GLView glView, Object... params) {
		getCurDockLineLayout().showBgWithoutAnimation(type, glView, params);
	}
}
