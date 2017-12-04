package com.jiubang.shell.preview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.graphics.ColorGLDrawable;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLTextViewWrapper;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.proxy.ValueReturned;
import com.go.util.graphics.DrawUtils;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDeleteZoneMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenFrameMsgId;
import com.golauncher.message.IScreenPreviewMsgId;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingUtils;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.preview.PreviewController;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.desks.diy.frames.tipsforgl.GuideControler;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.info.FavoriteInfo;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ScreenAppWidgetInfo;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.IShell;
import com.jiubang.shell.IView;
import com.jiubang.shell.animation.DragAnimation;
import com.jiubang.shell.animation.DragAnimation.DragAnimationInfo;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.common.component.ShellContainer;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.deletezone.GLDeleteZone;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.drag.DropTarget;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.orientation.GLOrientationControler;
import com.jiubang.shell.preview.GLScreenPreviewMsgBean.PreviewImg;
import com.jiubang.shell.screen.GLCellLayout;
import com.jiubang.shell.screen.GLCellLayout.LayoutParams;
import com.jiubang.shell.screen.GLScreen;
/**
 * 
 * @author jiangchao
 *
 */
public class GLSense extends GLFrameLayout
		implements
			IMessageHandler,
			IView,
			GLSenseWorkspace.ISenseWorkspaceListener,
			DragSource,
			DropTarget {
	public final static String FIELD_ABS_INDEX = "absolute_index";
	public final static String FIELD_DEST_SCREEN = "dest_screen";
	public final static String FIELD_SRC_SCREEN = "src_screen";
	public final static String FIELD_CUR_SCREEN_START_INDEX = "start_index";
	public final static String FIELD_SCREEN_COUNT = "screen_count";
	public final static String FIELD_SCROLL_DURATION = "scroll_duration";

	public final static int SCREEN_LOADED = 0x0; // 已经加载完毕
	public final static int SCREEN_LOADING = 0x1; // 正在加载
	public final static int FROM_SETTING = 0x2; // 从设置返回

	private final static int ASYNC_LOAD_VIEW = 1;
	private final static int TIP_TIMES = 5;

	// private GLSenseLayout mLayout; //最上层layout
	private GLSenseWorkspace mWorkspace; // 卡片管理容器
	private GLScreenPreviewMsgBean mPreviewBean; // 预览层数据包
	private boolean mHasAddPreviewBean = false;
	private List<Integer> mAsyncLoadListeners = new ArrayList<Integer>();
	private List<Integer> mReplaceFinishListeners = new ArrayList<Integer>();
	private Object mMutex;
	private int mCurrentScreen = -1;
	private boolean mStateAdd = false;

	public static boolean sScreenFrameStatus = false; // 是否隐藏状态栏
	public static boolean sPreLongscreenFrameStatus = false; // 是否隐藏状态栏(数据库中设置的状态)
	public static boolean sPreviewLongClick = false; // 是否长按键

	/**
	 * 此处为一个特殊处理 由于需求要求，在设置中点击屏幕设置，则会显示本界面
	 * 再点击返回键需要回到屏幕设置所在的设置界面，故由此标志位标识是否需要返回到设置界面
	 */
	private static boolean sNeedGotoSetting = false;
	private static boolean sBackFromSetting = false;

	private static boolean sIsEnterFromDragView = false; // 拖拽图标（或widget进入屏幕预览的标识）

	public static boolean sIsEnterFromQA = false;
	public static int sScreenW;
	public static int sScreenH;

	private PreviewController mPreviewController; // 预览控制

	private boolean mRefresh = false;

	private boolean mToMainScreen = false;

	private LinearLayout mTextLayout; // 卡片图标已满提示

	private boolean mTextLayoutvisiable = false;

	private boolean mDragapptextLayoutvisiable = false;

	private boolean mDragFinish = false;

	public static int sCurScreenId; // 记录当前屏的索引

	public static boolean sIsHOME = false; //  设置为默认桌面后 按home键跳屏幕预览
	public static boolean sDragFromeScreen = false ; // 判断是否从桌面拖元素进入
	private int mOrientation;
	private IShell mShell;
	private DragController mDragControler;
	private float[] mDragTransInfo = new float[5];
	private Context m2DContext;

	// 当前状态
	private final static int STATUS_NORMAL = 0; // 一般状态，表示简单的移动位置
	private final static int STATUS_DEL = 1; // 删除状态
	private final static int STATUS_NOSCREEN = 2;
	private int mStatus = STATUS_NORMAL;
	//  判断是否为图标快速拖到卡片上退出
	boolean mQuickdropflag = false;
	private GLTextViewWrapper mText;
	int mCaptionTextType = 0;
	
	private boolean mNeedSaveData = false; //是否需要调用savedate(),目前只会到调用了replacecard时调用
	private boolean mDropOnAddCard; // 拖拽进来的放手处于加号屏的时候置为true，因为有时候加号屏增加屏幕生效，但是却没有初始化，rect区域是｛0，0，0，0｝，不能作为退出动画的判断依据 -by Yugi 2013-5-22
	
	private GLDrawable mBackground = null;
	
	public GLSense(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		// TODO Auto-generated constructor stub
		getScreenWH();

		m2DContext = ShellAdmin.sShellManager.getActivity();
		mPreviewController = new PreviewController(m2DContext);

		mMutex = new Object();
		mOrientation = GLOrientationControler.getRequestOrientation();
		mBackground = new ColorGLDrawable(0x7f000000); // GLImageUtil.getGLDrawable(R.drawable.gl_sense_bg);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		int topD = StatusBarHandler.isHide() ? 0 : StatusBarHandler.getStatusbarHeight();

		int t = (int) (top + getContext().getResources().getDimension(R.dimen.sense_indicator_top));
		mWorkspace.layout(left, top + topD, right, bottom + topD);
		if (sIsHOME) {
			mText.setText(mContext.getString(R.string.choose_mainscreen));
			mText.layout(left, t / 2 + topD, right, bottom + topD);
		} else {
			if (mCaptionTextType == 1) {
				mText.layout(left, top + mWorkspace.mDragCaptionTop + topD, right, bottom + topD);
			} else if (mCaptionTextType == 2) {
				mText.layout(left, top + mWorkspace.mDragCaptionTop + topD, right, bottom + topD);
			} else {
				mText.setVisibility(View.INVISIBLE);
			}
		}

	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		MsgMgrProxy.registMsgHandler(this);
		mWorkspace = (GLSenseWorkspace) this.findViewById(R.id.senseWorkspace);
		mWorkspace.setListener(this);
//		this.setBackgroundColor(0x7f000000);
		mWorkspace.setPreviewController(mPreviewController);
		mRefresh = false;

		mText = (GLTextViewWrapper) findViewById(R.id.sense_text);
		mText.setTextColor(0xbbffffff);
	}

	
	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		drawBackground(canvas);
		super.dispatchDraw(canvas);
	}

	private void drawBackground(GLCanvas canvas) {
		if (mBackground != null && !mPreviewShow) {
			int width = getWidth();
			int height = getHeight() + DrawUtils.getNavBarHeight();
			mBackground.setBounds(0, 0, width, height);
			mBackground.draw(canvas);
		}
	}
	
	@Override
	public void setCaptionText(String s, int type) {
		try {
			mCaptionTextType = type;
			if (mCaptionTextType == 0) {
				mText.setVisibility(View.INVISIBLE);
			} else {
				mText.setVisibility(View.VISIBLE);
			}
			mText.setText(s);
		} catch (Exception e) {
		}
	}

	@Override
	public void setVisible(boolean visible, boolean animate, Object obj) {
		setVisible(visible);
	}

	@Override
	public void setShell(IShell shell) {
		this.mShell = shell;
		mDragControler = mShell.getDragController();
	}

	@Override
	public int getViewId() {
		return IViewId.SCREEN_PREVIEW;
	}

	@Override
	public void onAdd(GLViewGroup parent) {
		mDragControler.addDropTarget(this, IViewId.SCREEN_PREVIEW);
	}

	@Override
	public void onRemove() {
		ShellContainer.sEnableOrientationControl = true;
		GLOrientationControler.setPreviewModel(false);
		GLOrientationControler.resetOrientation();
		MsgMgrProxy.unRegistMsgHandler(this);
		mDragControler.removeDropTarget(this);
		sIsHOME = false;
		sDragFromeScreen = false ;
		mPreviewShow = false ;
//		GuideControler guideCloudView = GuideControler.getInstance(m2DContext);
//		guideCloudView.removeFromCoverFrame(GuideControler.CLOUD_ID_SCREEN_PRIVIEW);
		GLSense.sPreviewLongClick =  false ;
//		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
//				IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, false, null);
		MsgMgrProxy.sendBroadcast(this, IScreenFrameMsgId.SCREEN_FORCE_RELAYOUT, 0);
//退出时更新主屏状态
		for (int i = 0; i < mWorkspace.getChildCount(); i++) {
			GLCardLayout layout = (GLCardLayout) mWorkspace.getChildAt(i);
			if (layout.isHome()) {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
						IScreenFrameMsgId.SCREEN_SET_HOME, layout.getId());
			} 
		}	
		mShell.showStage(IShell.STAGE_SCREEN, false);
		mShell.hide(IViewId.DELETE_ZONE, false, true); 
	}

	@Override
	public boolean handleMessage(Object sender, int msgId, int param, Object... objects) {
		boolean ret = false;
		switch (msgId) {
			case IScreenPreviewMsgId.PREVIEW_INIT : {

				// 隐藏dock栏
//				mShell.hide(IViewId.DOCK, false);

				setBackFromSetting(false);
				if (objects[0] != null && objects[0] instanceof GLScreenPreviewMsgBean) {
					getScreenWH();
					if (mWorkspace != null) {
						mWorkspace.getDrawingResource();
					}
					init(param, (GLScreenPreviewMsgBean) objects[0]);
//					doAnimation();
					if (sIsHOME) {
						//隐藏加号屏 jiang设置为默认桌面后  按home键跳屏幕预览
						mWorkspace.hideAddCard();
					} else {
						// 进入过了屏幕预览就不响应home键处理
						PreferencesManager sharedPreferences = new PreferencesManager(
								ShellAdmin.sShellManager.getActivity(),
								IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
						sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_PREVIEW_HOME,
								false);
						sharedPreferences.commit();
						sIsHOME = false;
					}
				}
			}
				break;
			case IScreenPreviewMsgId.PREVIEW_DELETE_SCREEN : {
				final int cardIndex = param;
				final GLCardLayout cardLayout = (GLCardLayout) objects[0];
				mWorkspace.post(new Runnable() {
					@Override
					public void run() {
						mWorkspace.completeRemoveCard(cardLayout);
						mWorkspace.handleReplaceFinish();
						mWorkspace.endReplace();
						// 发信息给屏幕交换层，通知动画结束
						//					MsgMgrProxy.sendMessage(this, IDiyFrameIds.REPLACE_DRAG_FRAME,
						//							IDiyMsgIds.REPLACE_DRAG_FINISH, -1, null, null);
						// 放到Replace Back动画归位后在加上
						mWorkspace.showAddCard();
						mWorkspace.setCaptionY();
						mWorkspace.showCard(cardIndex);
						mWorkspace.unlock();
						//要求刷新屏幕层组件的屏幕索引
//						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, this,
//								IScreenFrameMsgId.SCREEN_REFRESH_INDEX, -1, true);
						//删除屏幕后显示已隐藏的小白云
						GuideControler guideCloudView = GuideControler.getInstance(m2DContext);
						guideCloudView.showPreviewGuide(GLSense.sCurScreenId);
					}
				});
				break;
			}
			case IScreenPreviewMsgId.PREVIEW_TO_MAIN_SCREEN_ANIMATE : {
				int screenIndex = 0;
				if (param > 0) {
					screenIndex = param;
				}
				// 要求预览层作离开动画

				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW,
						IScreenPreviewMsgId.PREVIEW_LEAVE_ANIMATE, screenIndex);
				if (GLCardLayout.sDrawRoom) {
					GLCardLayout.sDrawRoom = false;
				}
			}
				break;

			case IScreenPreviewMsgId.PREVIEW_LEAVE_ANIMATE : {
				if (param >= 0 && param < mWorkspace.getChildCount()) {
					preview(param);
					ret = true;
				}
			}
				break;

			case IScreenPreviewMsgId.PREVIEW_ENLARGE_CARD : {
				/*	if (object != null && (object instanceof Rect)) {
						Rect enlargeRect = mWorkspace.enlargeCard(param);
						if (enlargeRect != null) {
							((Rect) object).set(enlargeRect);
							ret = true;
						}
						enlargeRect = null;
					}*/
			}
				break;

			case IScreenPreviewMsgId.PREVIEW_RESUME_CARD : {
				if (param > -1) {
					mWorkspace.resumeCard(param);
					ret = true;
				}
			}
				break;

			case IScreenPreviewMsgId.PREVIEW_GET_ABS_SCREEN_INDEX : {
				/*if (object != null && object instanceof Bundle) {
					Bundle bundle = (Bundle) object;
					bundle.putInt(FIELD_ABS_INDEX,
							mWorkspace.getAbsScreenIndex(bundle.getInt(FIELD_ABS_INDEX)));
					ret = true;
				}*/
			}
				break;
			case IScreenPreviewMsgId.GET_PREVIEW_PARAM : {
				ValueReturned val = (ValueReturned) objects[0];
				ArrayList<Integer> list = new ArrayList<Integer>();
				list.add(GLSenseWorkspace.sCardPaddingLeft);
				list.add(GLSenseWorkspace.sCardWidth);
				list.add(GLSenseWorkspace.sCardHeight);
				int topD = StatusBarHandler.isHide() ? 0 : StatusBarHandler.getStatusbarHeight();
				list.add(GLSenseWorkspace.sMarginTop + topD);
				list.add(GLSenseWorkspace.sMarginLeft);
				list.add(GLSenseWorkspace.sSpaceX);
				list.add(GLSenseWorkspace.sSpaceY);
				list.add(GLSenseWorkspace.sHomeImageViewTop);
				val.mValue = list;
				val.mConmused = true;
			}
				break;
			case IFrameworkMsgId.SYSTEM_ON_RESUME : {
				GLOrientationControler.setRequestOrientation(mOrientation);
			}
				break;
					
			default :
				break;
		}
		return false;
	}

	private void handleSenseOnLongClickAction(GLCardLayout sense) {
		GuideControler.getInstance(m2DContext).hideCloudViewById(
				GuideControler.CLOUD_ID_SCREEN_PRIVIEW);
		final DragAnimationInfo dragAnimationInfo = new DragAnimationInfo(true,
				DragController.DRAG_ICON_SCALE, false, DragAnimation.DURATION_200, null);
		mDragControler.startDrag(sense, this, sense, DragController.DRAG_ACTION_MOVE,
				mDragTransInfo, dragAnimationInfo);

		//add by huyong 2013-05-26 for 更新屏幕的当前屏及主屏的索引
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_GET_HOME_CURRENT_SCREEN, -1, true);
		//add by huyong 2013-05-26 end		
	}

	@Override
	public int getMsgHandlerId() {
		return IDiyFrameIds.SCREEN_PREVIEW;
	}
	/**
	 * 是否拖拽图标（或widget）进入屏幕预览
	 * */
	public static boolean isEnterFromDragView() {
		return sIsEnterFromDragView;
	}
	private synchronized void init(final int param, GLScreenPreviewMsgBean msgBean) {
		if (sIsEnterFromDragView) {
			// 设置全屏显示
			sScreenFrameStatus = StatusBarHandler.isHide();
			// 得到当前屏幕状态
			if (!sScreenFrameStatus) {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						ICommonMsgId.SHOW_HIDE_STATUSBAR, -2, true, null);
			}
		}
		// 保护
		if (mHasAddPreviewBean) {
			return;
		}

		// 如果是从设置直接进来的，则点击返回键需要回到设置界面
		sNeedGotoSetting = (param & FROM_SETTING) == FROM_SETTING;
		// 正在加载的时候不能删除、移动屏幕
		final boolean enableUpdate = (param & SCREEN_LOADING) == SCREEN_LOADED;
		// mWorkspace.setEnableUpdate(enableUpdate);

		mPreviewBean = msgBean;

		mWorkspace.removeAllViews();

		if (sNeedGotoSetting) {
			// 从设置界面进入预览，没有进入动画
			// TODO: 设置当前屏幕id
			// mWorkspace.handleEnterFinished();
			notifyDesktop(false);
		} else {
			// 从非设置界面进入预览，有进入动画
			mWorkspace.setmStatus(GLSenseWorkspace.SENSE_WAIT_FOR_ENTERING);
//			mWorkspace.setBackgroundColor(0);
		}
		// TODO 临时做法
		// 这个方法不可靠，Workspace还自己在加视图
		if (null != msgBean) {
			GLSenseWorkspace.setCardCount(msgBean.screenPreviewList.size() + 1);
		}
		// 异步加载形式，保证进入速度
		asyncLoadBean();
	}

	/***
	 * <br>
	 * 功能简述:异步加载数据 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void asyncLoadBean() {
		mHasAddPreviewBean = true;

		// 上锁
		synchronized (mMutex) {
			// TODO 此处对内存情况进行检验，保证在解析XML时不会出现内存不足的情况
			OutOfMemoryHandler.gcIfAllocateOutOfHeapSize();
			try {
				List<GLScreenPreviewMsgBean.PreviewImg> previews = mPreviewBean.screenPreviewList;
				final int size = previews.size();

				sCurScreenId = mPreviewBean.currentScreenId;
				final int mainScreenId = mPreviewBean.mainScreenId;

				mWorkspace.setFirstLayout(false);

				int formCardIndex = -1;
				/*
				 * if (mDragView != null && mDragView.getTag() != null &&
				 * mDragView.getTag() instanceof ItemInfo) { ItemInfo itemInfo =
				 * (ItemInfo) mDragView.getTag(); formCardIndex =
				 * itemInfo.mScreenIndex; }
				 */

				for (int i = 0; i < size; i++) {
					// TODO 此处采用异步方式进行添加View
					final PreviewImg previewImg = previews.get(i);
					if (previewImg == null) {
						continue;
					}

					GLCardLayout card = null;

					try {
						card = new GLCardLayout(mContext, GLCardLayout.TYPE_PREVIEW,
								previewImg.previewView, previewImg.canDelete, mWorkspace);

						// 被捉起的view
						/*
						 * if (mDragView != null) { int index = i; boolean
						 * enough = MsgMgrProxy.sendMessage(this,
						 * IDiyFrameIds.SCREEN, IDiyMsgIds.IS_SET_CONTENT,
						 * index, mDragView, null); if (formCardIndex != i &&
						 * !enough) { card.setNoRoom(); } }
						 */
					} catch (OutOfMemoryError e) {
						OutOfMemoryHandler.handle();
					} catch (Exception e) {
					}

					// 作保护
					if (card == null) {
						continue;
					}

					// 设置主屏
					if (i == mainScreenId) {
						card.setHome(true);
					}

					// 设置当前屏
					if (sCurScreenId == i) {
						card.setCurrent(true);
					}
					//					setBackgroundDrawable(mWorkspace.mBorderImg);
					// 完成了上述的setHome　setCurrent　的UI操作才可把此card添加到桌面的UI框架中，
					// 因为此线程非main线程，如果先加入到UI框架中，setHome setCurrent会提示错误：
					// android.view.ViewRoot$CalledFromWrongThreadException:
					// Only the original thread that created a view hierarchy
					// can touch its views.
					mHandler.sendMessage(mHandler.obtainMessage(ASYNC_LOAD_VIEW, i, size, card));
				}
			
				// mDragView = null;
				if (!SettingProxy.getScreenSettingInfo().mLockScreen) {
					// 锁屏时不显示+号卡片

					GLCardLayout addCard = new GLCardLayout(mContext, GLCardLayout.TYPE_ADD, null,
							false, mWorkspace);
					mHandler.sendMessage(mHandler.obtainMessage(ASYNC_LOAD_VIEW, size, size,
							addCard));
				}
				if (GLSenseWorkspace.sCardCount > GLSenseWorkspace.MAX_CARD_NUMS) {
					mWorkspace.hideAddCard();
				}
//				mWorkspace.setCaptionY();

			} catch (IndexOutOfBoundsException e) {
				return;
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			} catch (NullPointerException e) {
				// 异步加载，对象不保证一定存在，特别是横竖屏切换时，所以加保护
				e.printStackTrace();
			}
		}
	}

	private void getScreenWH() {
		sScreenW = DrawUtils.sWidthPixels;
		sScreenH = DrawUtils.sHeightPixels;

		// 设置PreviewController的显示模式，横或竖，以正确获取资源
		if (sScreenH > sScreenW) {
			PreviewController.sDisplayMode = PreviewController.PORT;
		} else {
			PreviewController.sDisplayMode = PreviewController.LAND;
		}
	}

	private static void setBackFromSetting(boolean value) {
		sBackFromSetting = value;
	}

	public static boolean backFromSetting() {
		return sBackFromSetting;
	}
	
	void doEnterAnimation() {
		int duration = 200;
		AnimationTask task = new AnimationTask(true, AnimationTask.PARALLEL);
		task.setBatchAnimationObserver(new BatchAnimationObserver() {
			
			@Override
			public void onStart(int what, Object[] params) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFinish(int what, Object[] params) {
				if (mShell.getCurrentStage() == IShell.STAGE_PREVIEW) {
					mWorkspace.handleEnterFinished();
					postDelayed(new Runnable() {

						@Override
						public void run() {
							mWorkspace.setCaptionY();
						}
					}, 120);
				}
			}
		}, -1);
		ScaleAnimation scaleAnimation = new ScaleAnimation(4.0f, 1.0f, 4.0f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		scaleAnimation.setDuration(duration);
		task.addAnimation(mWorkspace.getChildAt(sCurScreenId), scaleAnimation, null);
		
		
		//1,方向 2,时间 3,X轴距离 4,Y轴距离
		int x = 0;
		int y = 0;
		int time = 50;
		int cur_row = sCurScreenId / 3;
		int cur_column = sCurScreenId % 3;
		for (int i = 0; i < mWorkspace.getChildCount(); i++) {
			if (i != sCurScreenId) {
				int row = i / 3;
				int column = i % 3;
				y = -2 * (cur_row - row) * GLSenseWorkspace.sCardHeight;
				x = -2 * (cur_column - column) * GLSenseWorkspace.sCardHeight;
				TranslateAnimation up4 = new TranslateAnimation(x, 0, y, 0);
				//相隔一屏以上的调整延迟
				if (Math.abs(cur_row - row) >= 2) {
					time = 70;
				}
				if (Math.abs(cur_column - column) >= 2) {
					time = 70;
				}
				up4.setStartOffset(time);
				up4.setDuration(duration);
				if (i < GLSenseWorkspace.MAX_CARD_NUMS) {
					task.addAnimation(mWorkspace.getChildAt(i), up4, null);
				}
			}
		}
		GLAnimationManager.startAnimation(task);
	}
	
	
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ASYNC_LOAD_VIEW : {
					if (msg.obj != null && msg.obj instanceof GLCardLayout) {
						mWorkspace.addInScreen((GLCardLayout) (msg.obj));
						if (msg.arg1 == sCurScreenId) {
							if (!sNeedGotoSetting) {
								// 从非设置界面进入预览，才有进入动画
								// 加载完当前屏，进入动画
								notifyDesktop(false);
								mWorkspace.enterCard(sCurScreenId);
							}
						}

						if (msg.arg1 == msg.arg2) {
							// 当排版完成时要求调到当前屏所在屏
							mWorkspace.setFirstLayout(true);
							if (mPreviewBean != null
									&& mPreviewBean.screenPreviewList != null
									&& mPreviewBean.screenPreviewList.size() >= GLSenseWorkspace.MAX_CARD_NUMS) {
								// mWorkspace.hideAddCard();
							}
//							mWorkspace.setCaptionY();
						}
					}
					int minusScreen = 1; // 加号屏的个数，非锁屏为1，锁屏没有加号屏为0
					if (SettingProxy.getScreenSettingInfo().mLockScreen) {
						minusScreen = 0;
					}
					if (mPreviewBean.screenPreviewList.size() == mWorkspace.getChildCount() - minusScreen) {
						mShell.hide(IViewId.DOCK, false);
						doEnterAnimation();
					}
				}
					break;

				default :
					break;
			}
		};
	};
	
	@Override
	public boolean preAddCard() {
		return MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.PREVIEW_SCREEN_ADD, -1);
	}

	@Override
	public void removeCard(int cardId) {
		// TODO Auto-generated method stub
		mRefresh = true;
		//delete screen
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.PREVIEW_SCREEN_REMOVE, mDraggedViewStartId);
	}

	@Override
	public void setCardHome(int cardId) {
		// 设置主屏
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SCREEN_SET_HOME,
				cardId);
	}

	@Override
	public void setCurrent(int cardId) {
		// TODO Auto-generated method stub

	}
	//是否进入preview 方法标志位 进一次屏幕预览只可能执行一次
	boolean mPreviewShow =  false ;
	@Override
	public void preview(int cardId) {
		if (mPreviewShow) {
			return;
		}
		GuideControler guideCloudView = GuideControler.getInstance(m2DContext);
		guideCloudView.hideCloudViewById(GuideControler.CLOUD_ID_SCREEN_PRIVIEW);
//		this.setBackgroundColor(0);
//		mWorkspace.setBackgroundColor(0x7f000000);
		mPreviewShow = true ;
		mText.setVisibility(INVISIBLE);
		//点击卡片退出
		//		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IDiyMsgIds.SCREEN_GET_CELLLAYOUT,
		//				cardId, null, list);

		//屏幕层移动到指定屏幕
		//		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, this, IScreenFrameMsgId.SCREEN_ENTER,
		//				cardId, 300);

		// 获取对应的视图
		List<GLView> list = new ArrayList<GLView>();
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_GET_CELLLAYOUT, -1, list);
		GLCardLayout.sDrawRoom = false;

		//  拖动widget进入屏幕预览,缩小widget
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.PREVIEW_RESIZE_DRAGVIEW, 0, 0.0f);
		if (list.size() > 0 && cardId < list.size()) {
			try {
				mWorkspace.leaveCard(cardId, list.get(cardId));
			} catch (Exception e) {
				// 直接离开
				leave();
			}
			//			mIndicator.setVisibility(View.INVISIBLE);
			//让Dock栏立即显示
			mShell.show(IViewId.DOCK, false);
		} else {
			// 直接离开
			leave();
		}

		//		mShell.remove(IViewId.SCREEN_PREVIEW, false, false);
		//		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, this, IScreenFrameMsgId.SCREEN_ENTER,
		//				cardId, 200);

		//		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
		//				IDiyFrameIds.DOCK, null, null); // 让Dock栏立即显示

		// 发送进入屏消息 跳到相应屏幕
		//		Integer duration = new Integer(mWorkspace.getLeaveDuration());
		
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.PREVIEW_SCREEN_ENTER, cardId, 0);

	}

	@Override
	public void previewLongClick(int cardId) {
		GLCardLayout child = (GLCardLayout) mWorkspace.getChildAt(cardId);
		if (child != null) {
			//保持横竖屏
			sPreviewLongClick = true;
			DesktopSettingInfo info = SettingProxy.getDesktopSettingInfo();
			sPreLongscreenFrameStatus = !info.mShowStatusbar;

			// 得到当前屏幕状态
			if (!sPreLongscreenFrameStatus) {
				// //隐藏指示器
				// MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				// IDiyMsgIds.HIDDEN_INDICATOR, -1, null, null);
				// previewOperate = true;
				//			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
				//					IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, true, null);
				GLSenseWorkspace.showStatusBar = true;
			}
			mText.setVisibility(INVISIBLE);
			// 隐藏添加卡片
			mWorkspace.hideAddCard();
			// 开始起拖拽
			mCardRects = mWorkspace.getCurScreenRects();
			mCurScreenStartIndex = 0;
			//		GLCardLayout child = (GLCardLayout) mWorkspace.getChildAt(cardId);
			//		child.setBackgroundResource(R.drawable.folder_edit_light);
			//		child.postInvalidate();
			mBlankId = cardId;
			handleSenseOnLongClickAction(child);
		}
	}

	@Override
	public void firstLayoutComplete() {
		// TODO Auto-generated method stub

	}

	@Override
	public void leaveFinish() {
		leave();
	}

	@Override
	public void replaceFinish() {
		mLocked = false;
	}

	@Override
	public void updateIndicator(int current, int total) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIndicatorVisible(boolean isVisible) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		//更新一次当前屏
		for (int i = 0; i < mWorkspace.getChildCount(); i++) {
			GLCardLayout layout = (GLCardLayout) mWorkspace.getChildAt(i);
			if (layout.isCurrent()) {
				sCurScreenId = layout.getId();
			}
		}	
		if (sPreviewLongClick) {
			return false;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backHandle();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
				|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			// 屏蔽
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/***
	 * 按返回键的返回操作
	 */
	public void backHandle() {
		GLCardLayout.sDrawRoom = false;
		if (GLScreen.sIsShowPreview) {
			GuideControler guideCloudView = GuideControler.getInstance(m2DContext);
			guideCloudView.hideCloudViewById(GuideControler.CLOUD_ID_SCREEN_PRIVIEW);
			// 带动画退出
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.PREVIEW_TO_MAIN_SCREEN, 0);
		}
	}

	public void exitByStage() {
		if (mDropOnAddCard) {
			preview(mWorkspace.getLastEffectiveScreenIndex());
		} else {
			backHandle();
		}
	}
	

	
	@Override
	public void onDropCompleted(DropTarget target, Object dragInfo, boolean success,
			DropAnimationInfo resetInfo) {
		//		Log.i("jiang", "=-=-=-=-=-=onDropCompleted");
		if (dragInfo instanceof GLCardLayout) {
			GLCardLayout layout = (GLCardLayout) dragInfo;
			int cardId = layout.getId();
			//垃圾桶删除
			if (target instanceof GLDeleteZone && success) {
				// 2,保存移动屏幕数据
				//		Log.i("jiang", mDraggedViewStartId+"-"+mDraggedViewEndId);

				//				Bundle bundle1 = new Bundle();
				//				bundle1.putInt(GLSense.FIELD_SRC_SCREEN, mDraggedViewStartId);
				//				bundle1.putInt(GLSense.FIELD_DEST_SCREEN, mDraggedViewEndId);

				//		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, this,
				//				IScreenPreviewMsgId.PREVIEW_SAVE_SCREEN_DATA, 0, bundle1);

				//3,屏幕预览层移动结束后保存数据
				if (mWorkspace.getChildCount() > 1) {
					//删除有内容的屏幕时，去掉弹出删除提示框。
					//  1, 判断是否有内容
					/*if (layout.hasContent()) {
//						layout.setVisibility(View.INVISIBLE);
						mWorkspace.setNormal(cardId);
						mWorkspace.showAddCard();
						showDeleteDialog(layout);
						mDragControler.setKeepDragging(true);
						mLocked = false;
						sPreviewLongClick = false;
						return;
					}*/
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW,
							IScreenPreviewMsgId.PREVIEW_DELETE_SCREEN, 0, layout);
					layout.setNormal();
					layout.postInvalidate();
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.DELETE_ZONE,
							IDeleteZoneMsgId.DELETE_ZONE_CONTINUE_DELETE_ANIMATION, -1, layout);
				} else {
					//如果是最后一屏幕  提示不能删除
					mWorkspace.showToast(R.string.no_less_screen);
					layout.setNormal();
					layout.postInvalidate();
					resetAnimation(mBlankId, resetInfo);
				}
				//4,屏幕层删除一个屏幕
				//				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, this,
				//						IScreenPreviewMsgId.PREVIEW_SAVE_SCREEN_DATA, cardId, true);
			} else {
				//正常长按不触发换位
				if (mNeedSaveData) {
					//屏幕挤压换位
					saveData();
					mNeedSaveData = false;
					// 要求刷新屏幕层组件的屏幕索引
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_REFRESH_INDEX, -1, true);
				}
			}
			mWorkspace.showAddCard();
			//     	int a =	mWorkspace.getChildCount();
			//    	Rect frame = new Rect();
			//     	for (int i = 0; i < a; i++) {
			//     		mWorkspace.getChildAt(i).getHitRect(frame);
			//		}
			//		//屏幕层处理
			//	
			//	拖拽完一次恢复锁
			mLocked = false;
		} 
		mWorkspace.setCaptionY();
		sPreviewLongClick = false; 
	}

	@Override
	public boolean onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, DropAnimationInfo resetInfo) {
		
		if (dragInfo instanceof GLCardLayout) {
			GLCardLayout layout = (GLCardLayout) dragInfo;
			if (layout.isCurrent()) {
				layout.setBackgroundDrawable(mWorkspace.mBorderLightImg);
			} else {
				layout.setNormal();
			}
			layout.postInvalidate();

			//补位动画
			Rect mCurRc = new Rect();
			dragView.getHitRect(mCurRc);
			if (null != mCurRc) {
				//					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				//							IDiyMsgIds.PREVIEW_REPLACE_CARD_BACK, mDraggedView.getId(), mCurRc, mCardRects);
				//这里要传入最后空位的id
				//				Log.i("jiang", cardId + "=***" + mBlankId + "***" + dragView.getId());
				//				if(mBlankId == -1){
				//					mWorkspace.replaceBack( cardId +1, mCurRc);
				//				}else{
				//					mWorkspace.replaceBack(mBlankId , mCurRc);
//				layout.setVisibility(View.INVISIBLE);
				resetAnimation(mBlankId, resetInfo);
			}
			
			return false;
		} else {

			int centerX = x - xOffset;
			int centerY = y - yOffset;
			mCardRects = mWorkspace.getCurScreenRects();
			final int count = mCardRects != null ? mCardRects.size() : 0;
			for (int i = 0; i < count; i++) {
				Rect cardRect = mCardRects.get(i);
				if (cardRect.contains(centerX, centerY)) {
					mQuickdropflag = true;
				}
			}
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.PREVIEW_RESIZE_DRAGVIEW, 0, 0.0f);
			if (mQuickdropflag) {
				mQuickdropflag = false;
				Rect cardRects = null;
				boolean flag = false ; 
				if (mEnlargeIndex > -1) {
					cardRects = mWorkspace.getCurScreenRects().get(mEnlargeIndex);
					int w = (int) ((centerX - cardRects.left) / GLSenseWorkspace.sPreViewScaleFactor);
					int h = (int) ((centerY - cardRects.top) / GLSenseWorkspace.sPreViewScaleFactor);
					//判断是否加号屏幕
					boolean isadd = mWorkspace.isAddCard(mEnlargeIndex);
					int[] cellXY = new int[2];
					if (isadd) {
						clearTimer();
						int spanX = 1;
						int spanY = 1;
						GLView originView = dragView.getOriginalView();
						Object info = originView.getTag();
						// 如果是widget的话行列数可能就是非 1 x 1
						if (info instanceof ScreenAppWidgetInfo) {
							LayoutParams params = (LayoutParams) originView.getLayoutParams();
							spanX = params.cellHSpan;
							spanY = params.cellVSpan;
						} else if (info instanceof FavoriteInfo) {
							spanX = ((FavoriteInfo) info).mSpanX;
							spanY = ((FavoriteInfo) info).mSpanY;
						}
						// 必须在completeAddEmptyCard前执行
						GLCellLayout.pointToCellExact(w, h, spanX, spanY, cellXY); // 转化为对应的格子
						mWorkspace.completeAddEmptyCard();
						flag = true ;
					}
					//要求刷新屏幕层组件的屏幕索引
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_SET_CURRENTSCREEN, mEnlargeIndex);
					
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_SNAP_TO_SCREEN, mEnlargeIndex, false, 200);
					
					mDropOnAddCard = flag;
					boolean isSuccess = MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.PREVIEW_ADD_TO_SCREEN, ScreenEditConstants.ADD_APPS_ID,
							dragView, w, h, mEnlargeIndex, xOffset, yOffset, source, dragInfo,
							resetInfo , flag, cellXY);
					return isSuccess;
				}
				return true;
			} else {
				return false;
			}
		}

	}

	// 复位动画
	private void resetAnimation(final int dragIndex, DropAnimationInfo resetInfo) {
		int centerY = mCardRects.get(mBlankId)
				.centerY() ;
		//加状态栏高度
		int topD = StatusBarHandler.isHide() ? 0 : StatusBarHandler.getStatusbarHeight();
		centerY = centerY + topD;
		resetInfo.setLocationPoint(mCardRects.get(mBlankId).centerX(), centerY);
		resetInfo.setDuration(DragAnimation.DURATION_200);
		//				resetInfo.setDuration(2000);
		resetInfo.setLocationType(DropAnimationInfo.LOCATION_CENTER);
		resetInfo.setAnimationListener(new AnimationListenerAdapter() {
			
			@Override
			public void onAnimationEnd(Animation animation) {
				GLCardLayout card = (GLCardLayout) mWorkspace.getChildAt(mBlankId);
				if (card != null) {
					if (card.isCurrent()) {
						card.setBackgroundDrawable(mWorkspace.mBorderLightImg);
					} else {
						card.setBackgroundDrawable(mWorkspace.mBorderImg);
					}
				}
				GuideControler guideCloudView = GuideControler.getInstance(m2DContext);
				guideCloudView.showPreviewGuide(GLSense.sCurScreenId);
			}
		});
	}
	
	
	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		//				Log.i("jiang", " =-=-=-=-=-=onDragEnter");
		if (dragInfo instanceof GLCardLayout) {
			mDraggedViewStartId = ((GLCardLayout) dragInfo).getId();
		} else {
			if (dragInfo instanceof ScreenAppWidgetInfo || dragInfo instanceof FavoriteInfo) {
				ItemInfo info = (ItemInfo) dragInfo ;
				if (!(info.mSpanX == 1 && info.mSpanY == 1)) {
					//  拖动widget进入屏幕预览,缩小widget
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.PREVIEW_RESIZE_DRAGVIEW, 0,
							GLSenseWorkspace.sPreViewScaleFactor);
					mDragControler.resetCenterPosToTouchPoint();
				}
			}
//			mShell.hide(IViewId.DELETE_ZONE, true, this);
			sDragFromeScreen = true ;
			mShell.hide(IViewId.DELETE_ZONE, true, true);
		}
	}

	public void scaleWidget() {
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.PREVIEW_RESIZE_DRAGVIEW, 0, GLSenseWorkspace.sPreViewScaleFactor);
		mDragControler.resetCenterPosToTouchPoint();
	}
	//   ==============挤压=============
	private List<Rect> mCardRects;
	private int mCurScreenStartIndex = 0;  //获取当前屏中的第一个子视图的索引
	// 换位参数
	// 换位锁屏
	// 挤压忽略值
	private boolean mLocked = false;
	private final static float REPLACE_IGNORE = 10;

	private int mDraggedViewStartId = 0;
	private int mDraggedViewEndId = 0;
	//最后的空位,用于拖拽卡片的最后补位
	private int mBlankId = 0;

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
//		Log.i("jiang", "=-=-=-=-=-=onDragOver");
		if (mLocked) {
			return;
		}
		if (dragInfo instanceof GLCardLayout) {

			sSHORTCUNTFLAGH = false;
			mBlankId = ((GLCardLayout) dragInfo).getId();

			int centerX = x - xOffset;
			int centerY = y - yOffset;
			final int count = mCardRects != null ? mCardRects.size() : 0;
			for (int i = 0; i < count; i++) {
				final Rect cardRect = mCardRects.get(i);
				if (cardRect.contains(centerX, centerY)) {
//					Log.i("jiang", "要求屏幕预览层交换位置");
					// 要求屏幕预览层交换位置
					//				dragView.setId(4);
					mDraggedViewEndId = i;
					// 要求屏幕预览层交换位置
					if (dragView != null && (mCurScreenStartIndex + i) != mBlankId) {
						//						Log.i("jiang", ((GLCardLayout)dragInfo).getId()+"// 要求屏幕预览层交换位置"+i);
						final int srcScreenIndex = ((GLCardLayout) dragInfo).getId();
						final int destScreenIndex = i + mCurScreenStartIndex;
						List<Rect> rects = mCardRects;
						// TODO 通知屏幕层保存设置
//						Log.i("jiang", "===111====replaceCard");
						mWorkspace.replaceCard(srcScreenIndex, destScreenIndex, rects);
						mBlankId = destScreenIndex;
						mNeedSaveData = true ;
						mRefresh = true;
						mLocked = true;
						return;
					}
				}
			}
			// 挤压操作
			if (count > 0 && dragView != null) {
				Rect rect = new Rect();
				dragView.getHitRect(rect);
				int ret = doHollowReplace(rect, mCardRects.get(0), mCardRects.get(count - 1),
						DrawUtils.dip2px(REPLACE_IGNORE));
				if (-1 == ret) {
					return;
				}
				if (0 == ret) {
					return;
				}
				//mDraggedViewEndId赋值为最后一屏,以处理放手时不处于某一张卡片区域
				mDraggedViewEndId = mCardRects.size() - 1;
				int replaceIndex = mCurScreenStartIndex + ret * (count - 1);
				//				// 要求屏幕预览层交换位置
				if (replaceIndex != mBlankId) {

					int srcScreenIndex = ((GLCardLayout) dragInfo).getId();
					int destScreenIndex = mBlankId = replaceIndex;
					//屏幕层数据
					mWorkspace.replaceCard(srcScreenIndex, destScreenIndex, mCardRects);
					mRefresh = true;
					mNeedSaveData = true ;
					mLocked = true;
				}
			}
		} else {
			sSHORTCUNTFLAGH = true;
			int centerX = x - xOffset;
			int centerY = y - yOffset;
			// TODO:不需要每次都去执行getCurScreenRects();
			mCardRects = mWorkspace.getCurScreenRects();
			final int count = mCardRects != null ? mCardRects.size() : 0;
			Rect cardRect_temp = null;
			int cardid = -1;
			
			switch (mState) {
				case STATE_NONE : {
					//						Log.i("jiang",mCardRects.size()+"         "+ centerX+"==="+centerY);
					for (int i = 0; i < count; i++) {
						Rect cardRect = mCardRects.get(i);
						if (cardRect.contains(centerX, centerY)) {
							cardid = i;
							cardRect_temp = cardRect;
						}
					}
					if (cardid != -1 && cardRect_temp != null) {
						//判断是否是加号卡
						boolean isadd = mWorkspace.isAddCard(cardid);
						if (isadd) {
							// 如果是加号,则让这个加号变为一个屏幕
							mWorkspace.setNormal(cardid);
							Rect enlargeRect = mWorkspace.enlargeCard(cardid);
							if (enlargeRect != null) {
								cardRect_temp.set(enlargeRect);
							}
							mState = STATE_ADD;
							mEnlargeIndex = cardid;
							mEnlargeRect = enlargeRect;
							startTimer(cardid);
							break;
						}
						startTimer(cardid);
						enlargeCard(cardid);
					}
				}
					break;
				case STATE_ENLARGE : {
					if (mEnlargeRect != null
							&& !mEnlargeRect.contains((int) centerX, (int) centerY)) {
						//								Log.i("jiang2", "STATE_ENLARGE*****" + mEnlargeRect);
						clearTimer();
						// 取消放大的卡片
						resumeCard(mEnlargeIndex);
					}
				}
				case STATE_RED : {
					//						if (mEnlargeRect != null && !mEnlargeRect.contains((int) centerX, (int) centerY)) {
					//							clearTimer();
					//							resumeNormal(false);
					//						}
				}
					break;
				case STATE_ADD : {
					// 变为加号
					/* if (mEnlargeRect != null
							&& !mEnlargeRect.contains((int) centerX, (int) centerY)) {
						Log.i("jiang2", "STATE_ADD*****" + mEnlargeRect);
						clearTimer();
						mWorkspace.setAdd(cardid);
						// 变成一个+号的样子
						// 把这个cardLayout放大
							mWorkspace.resumeCard(cardid);
							mState = STATE_NONE;
							mEnlargeRect = null;
							mEnlargeIndex = -1;

					}*/
					if (mEnlargeRect != null
							&& !mEnlargeRect.contains((int) centerX, (int) centerY)) {
						clearTimer();
						mWorkspace.setAdd(cardid);
						// 取消放大的卡片
						resumeCard(mEnlargeIndex);
						//								mQuickdropflag = false ;
					}
				}
					break;
				default :
					break;
			}
			//				}
		}
	}

	/*
	 * retrun 0 在第一个前 1 在最后个后 -1其他
	 */
	private int doHollowReplace(Rect curRc, Rect firstRc, Rect lastRc, int ignore) {
		// 检测参数
		if (0 == curRc.width() && 0 == curRc.height()) {
			return -1;
		}
		// 第一个左前
		if (curRc.centerX() <= firstRc.left - ignore || curRc.centerY() <= firstRc.top - ignore) {
			return 0;
		}
		// 最后一个后
		// 右
		// 下
		if ((curRc.centerX() >= lastRc.right + ignore && curRc.centerY() >= lastRc.top + ignore)
				|| curRc.centerY() >= lastRc.bottom + ignore) {
			return 1;
		}

		return -1;
	}

	@Override
	public void onDragExit(DragSource source, DropTarget nextTarget, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		
		if (!sPreLongscreenFrameStatus && GLSenseWorkspace.showStatusBar) {
			// previewOperate = true;
//			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
//					IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, false, null);
			GLSenseWorkspace.showStatusBar = false;
		}
		
		// 当前拖拽的不是屏幕预览卡片的处理
		if (!(dragInfo instanceof GLCardLayout)) {
			int cardid = -1;
			if (mDropOnAddCard) {
				cardid = mWorkspace.getLastEffectiveScreenIndex();
				mDropOnAddCard = false;
			} else {
				// 判断是否在卡片上
				int centerX = x - xOffset;
				int centerY = y - yOffset;
				mCardRects = mWorkspace.getCurScreenRects();
				final int count = mCardRects != null ? mCardRects.size() : 0;
				for (int i = 0; i < count; i++) {
					Rect cardRect = mCardRects.get(i);
					if (cardRect.contains(centerX, centerY)) {
						cardid = i;
					}
				}
			}

			if (cardid != -1) {
				preview(cardid);
			} else {
				backHandle();
			}
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
		return IViewId.SCREEN_PREVIEW;
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		return true;
	}

	@Override
	public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, Rect recycle) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getHitRect(Rect outRect) {
		outRect.left = mLeft;
		outRect.top = mTop ;
		outRect.right = mRight;
		outRect.bottom = mBottom;
	}

	private DialogConfirm mDelDialog = null;

	private View mView = null;
	protected boolean mPositiveButtonClicked;
	/**
	 * <br>功能简述:显示删除有内容的屏幕提示框
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param v
	 */
	void showDeleteDialog(GLView v) {
		mPositiveButtonClicked = false;
		final GLView dragInfo = v;
		String title = mContext.getString(R.string.del_title_tip);
		String content = mContext.getString(R.string.del_content_tip);
		mDelDialog = null;
		mDelDialog = new DialogConfirm(m2DContext);
		mDelDialog.show();
		mDelDialog.setTitle(title);
		mDelDialog.setMessage(content);
		mDelDialog.setPositiveButton(null, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mWorkspace.getChildCount() > 1) {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW,
							IScreenPreviewMsgId.PREVIEW_DELETE_SCREEN, 0, (GLCardLayout) dragInfo);
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.DELETE_ZONE,
							IDeleteZoneMsgId.DELETE_ZONE_CONTINUE_DELETE_ANIMATION, -1,
							(GLCardLayout) dragInfo);
					mDragControler.releaseDragging(null);
				} else {
					//如果是最后一屏幕  提示不能删除
					mWorkspace.showToast(R.string.no_less_screen);
				}
				((GLCardLayout) dragInfo).setNormal();
				((GLCardLayout) dragInfo).postInvalidate();
				mWorkspace.showAddCard();
				mLocked = false;
				mPositiveButtonClicked = true;
			}

		});

		mDelDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (mDelDialog != null) {
					mDelDialog = null;
					if (!mPositiveButtonClicked) {
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.DELETE_ZONE,
								IDeleteZoneMsgId.DELETE_ZONE_CLOSE_TRASHCAN, -1);
						DropAnimationInfo resetInfo = new DropAnimationInfo();
						//换屏后拖到垃圾桶需要同步一次
						if (mNeedSaveData) {
							//屏幕挤压换位
							saveData();
							mNeedSaveData = false;
						}
						resetAnimation(mBlankId, resetInfo);
						mDragControler.releaseDragging(resetInfo);
					}
				}
			}
		});
	}

	private void leave() {
		post(new Runnable() {
			@Override
			public void run() {
				mShell.remove(IViewId.SCREEN_PREVIEW, false, false);
				// dock栏
				if (GLScreen.sDockVisible) {
					mShell.show(IViewId.DOCK, false);
				}
//				//要求刷新屏幕层组件的屏幕索引
//				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, this,
//						IScreenFrameMsgId.SCREEN_REFRESH_INDEX, -1, true);
				notifyDesktop(true);
				mDropOnAddCard = false;
			}
		});

	}

	/**
	 * 设置桌面是否可见
	 * 
	 * @param show
	 *            true显示桌面
	 */
	private void notifyDesktop(boolean show) {
		GuideControler guideControler = GuideControler.getInstance(m2DContext);
		if (show) {

			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.PREVIEW_NOTIFY_DESKTOP, 1, true);
			// 弹出高级版付费广告
			DeskSettingUtils.showGuidePrimeDialog(m2DContext, 306);
			//			mFrameManager.setFrameVisiable(IDiyFrameIds.DOCK, View.VISIBLE);
//			guideControler.show
			
		} else {

			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.PREVIEW_NOTIFY_DESKTOP, 0, true);

			//			mFrameManager.setFrameVisiable(IDiyFrameIds.DOCK, View.GONE);
		}
	}

	@Override
	public void setVisible(boolean visible) {
		// TODO Auto-generated method stub
		super.setVisible(visible);
		if (!visible) {
			notifyDesktop(true);
		}
	}

	// 卡片换位后的数据保存
	private void saveData() {
		// 保存数据库
		Bundle bundle1 = new Bundle();
		bundle1.putInt(GLSense.FIELD_SRC_SCREEN, mDraggedViewStartId);
		bundle1.putInt(GLSense.FIELD_DEST_SCREEN, mDraggedViewEndId);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.PREVIEW_SAVE_SCREEN_DATA, 0, bundle1);
	}

	//=============图标拖入处理
	private final static String TAG = "previewdrag";
	public static boolean sSHORTCUNTFLAGH = false;
	// 特殊需求
	// 第一次切屏要延时
	private boolean mFirstSnapScreen = true;

	// 提示框
	// 操作延时
	private static final int REQUEST_TIME_OUT = 3;
	private final static long TIME_OUT_DURATION = 2000;
	//	private final static int TIP_TIMES = 5;

	// 状态
	private final static int STATE_NONE = 0;
	private final static int STATE_ENLARGE = 1;
	private final static int STATE_RED = 2;
	private final static int STATE_ADD = 3;
	private int mState;

	// Card矩形区域列表
	//	private List<Rect> mCardRects = new ArrayList<Rect>();
	// 放大
	private Rect mEnlargeRect;
	private int mEnlargeIndex = -1;

	private void enlargeCard(int index) {
		// 要求预览层放大指定索引的卡片

		mEnlargeRect = mWorkspace.enlargeCard(index);

		mState = STATE_ENLARGE;
		mEnlargeIndex = index;

	}

	private void resumeCard(int index) {
		mWorkspace.resumeCard(index);
		mState = STATE_NONE;
		mEnlargeRect = null;
		mEnlargeIndex = -1;
	}
	/*** =====延迟1秒退回到主页面==== */
	private static final int BACK_TO_WORKSPACE_DELAY = 1000;
	private static final int BACKTOWORKSPACE = 0;
	private int mScreenIndex = -1;
	private ToWorkspaceRunnable mToWorkspaceRunnable;
	private void startTimer(int index) {
		if (mScreenIndex == index) {
			return;
		}
		mScreenIndex = index;
		clearTimer();
		mToWorkspaceRunnable = new ToWorkspaceRunnable();
		mHandler.postDelayed(mToWorkspaceRunnable, BACK_TO_WORKSPACE_DELAY);
	}
	private void clearTimer() {
		mScreenIndex = -1;
		if (mToWorkspaceRunnable != null) {
			mHandler.removeCallbacks(mToWorkspaceRunnable);
		}
	}
	/**
	 * 
	 *
	 */
	private class ToWorkspaceRunnable implements Runnable {
		public ToWorkspaceRunnable() {
		}

		@Override
		public void run() {
			Message message = Message.obtain();
			message.what = BACKTOWORKSPACE;
			message.arg1 = mScreenIndex;
			mToWorkspaceHandler.sendMessage(message);
		}
	}

	private Handler mToWorkspaceHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case BACKTOWORKSPACE :
					int absolute_index = mWorkspace.getAbsScreenIndex(mEnlargeIndex);
					//				
					//				// 刷新桌面网格
					//				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					//						IDiyMsgIds.CHECK_GRID_STATE, screenIndex, null, null);
					
					doFinishState(absolute_index);

					preview(absolute_index);
					break;
			}
		}
	};

	/**
	 * 退出预览层前的处理
	 * 
	 * @param screenIndex
	 */
	private void doFinishState(int screenIndex) {
		switch (mState) {
			case STATE_ADD :// 如果放在+上，需先添加一个屏幕
				// 添加一个屏幕
				mWorkspace.completeAddEmptyCard();
				mCurrentScreen = screenIndex;
				mStateAdd = true;
				break;
			case STATE_RED :
				//				resumeNormal(true);
				break;
			default :
				break;
		}
	}

	@Override
	public void setDragController(DragController dragger) {
		// TODO Auto-generated method stub
		
	}
}
