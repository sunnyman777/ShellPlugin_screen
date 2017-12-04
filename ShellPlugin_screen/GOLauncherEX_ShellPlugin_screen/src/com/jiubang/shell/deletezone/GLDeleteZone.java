package com.jiubang.shell.deletezone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.Toast;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.animation.Transformation3D;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLRelativeLayout;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLImageView;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.ICommonMsgId;
import com.golauncher.message.IDeleteZoneMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.IShell;
import com.jiubang.shell.IView;
import com.jiubang.shell.animation.AnimationConstant;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.dock.GLDock;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.drag.DragController.DragListener;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.drag.DropTarget;
import com.jiubang.shell.preview.GLSense;
import com.jiubang.shell.screen.GLWorkspace;
import com.jiubang.shell.utils.GLImageUtil;
import com.jiubang.shell.utils.ToastUtils;

/**
 * 垃圾桶
 * @author yangguanxiang
 *
 */
public class GLDeleteZone extends GLRelativeLayout
		implements
			IView,
			DropTarget,
			DragListener,
			AnimationListener,
			IMessageHandler {
	// 当前状态
	private final static int STATUS_NORMAL = 0; // 一般状态，表示简单的移动位置
	private final static int STATUS_DEL = 1; // 删除状态
	private final static int STATUS_UNINSTALL = 2; // 卸载状态
	
	private static final float HALF = 0.5f;

	private static final int ANIMATION_DURATION_IN_OUT = 300;

	private static final int ANIMATION_DURATION_SLIDE = 200; // 删除动画，第一个滑动和缩小图标的动画时间

	private static final int ANIMATION_DURATION_GONE = 300; // 删除动画，第二个飞走的动画的时间

//	private static final int ANIMATION_DURATION_BG_COLLAPSE = 200; // 删除动画，背景射线图收起的动画时间

	private static final float DELETE_ICON_X_EXPAND = 1.5f; // 删除动画执行时，第一个缩放动画的X轴放大比例

	private static final float DELETE_ICON_X_COL = /*0.2f*/1.0f / DELETE_ICON_X_EXPAND; // 删除动画执行时，第二个动画的X轴放大比例

	private static final float DELETE_ICON_Y_COL = 0.5f; // 删除动画执行时，第一个动画的Y轴放大比例

	private static final float DELETE_ICON_Y_EXPAND = 1.0f / DELETE_ICON_Y_COL; // 删除动画执行时，第二个动画的X轴放大比例

//	private static final float RED_RAY_BG_X_COL = 0.1f; // 删除动画执行后，红色射线背景收拢动画的X轴收缩比例

	private static final float DELETE_ICON_ALPHA_POW = 3; // 删除动画执行时，淡出的ALPHA动画的变化值次幂数

	// 是否处于计时状态
	private final static int HANDLE_OVER_TRASH_EADGE = 1;
	private final static long TRASH_WAIT_DURATION = 1500;
	private int mStatus = STATUS_NORMAL;
	
	private GLImageView mTrashcanView;
//	private GLImageView mAnimBg;
//	private GLFrameLayout mAnimBgContainer;
	private GLDeleteZoneClipContainer mAnimClipContainer;
	private GLDrawable mBgNormal;
	private GLDrawable mBgHover;
	private GLDrawable mTrashNormal;
	private GLDrawable mTrashOpen;
	private IShell mShell;
	private DragController mDragControler;
	private boolean mIsDrop;
	private GLDeleteAnimView mDeleteAnimView;
	private int mStartAnimationToY;

	private int mCenterX;

	private int mCenterY;

	private DragView mDragView;

	private Object mDragInfo;
	private Intent mUninstallIntent;
//	private boolean mFlagFromSense = false;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case HANDLE_OVER_TRASH_EADGE : {
					if (mDragInfo instanceof ShortCutInfo || mDragInfo instanceof FunAppItemInfo
							|| mDragInfo instanceof DockItemInfo) {
						Object object = mDragInfo instanceof DockItemInfo
								? ((DockItemInfo) mDragInfo).mItemInfo
								: mDragInfo;
						if (!(object instanceof UserFolderInfo)) {
							AppItemInfo appItemInfo = object instanceof ShortCutInfo
									? ((ShortCutInfo) object).getRelativeItemInfo()
									: ((FunAppItemInfo) object).getAppItemInfo();
							boolean isShortCut = object instanceof ShortCutInfo
									? ((ShortCutInfo) object).mItemType == IItemType.ITEM_TYPE_SHORTCUT
									: false;
							if (appItemInfo != null && !appItemInfo.getIsSysApp() && !isShortCut) {
								// 处于卸载状态
								mStatus = STATUS_UNINSTALL;
								mUninstallIntent = appItemInfo.mIntent;
								showUninstallToast();
							}
						}
					}
				}
					break;

				default :
					break;
			}
		};
	};
	private boolean mForceHandleStatusBar;
	
	public GLDeleteZone(Context context, AttributeSet attrs) {
		super(context, attrs);
		MsgMgrProxy.registMsgHandler(this);
	}

	@Override
	protected void onFinishInflate() {
		mTrashcanView = (GLImageView) findViewById(R.id.trashcan);
//		mAnimBg = (GLImageView) findViewById(R.id.trashcan_anim_bg);
//		mAnimBgContainer = (GLFrameLayout) findViewById(R.id.trashcan_anim_bg_container);
		mAnimClipContainer = (GLDeleteZoneClipContainer) findViewById(R.id.trashcan_clip_container);
	}

	private void showUninstallToast() {
//		vibrate();
		ToastUtils.showToast(R.string.drag_uninstall_tip,  Toast.LENGTH_SHORT);
	}
	
	@Override
	public void getHitRect(Rect outRect) {
		outRect.left = mLeft;
		outRect.top = mTop;
		outRect.bottom = mTop + mTrashcanView.getHeight();
		if (!GoLauncherActivityProxy.isPortait() && ShortCutSettingInfo.sEnable) {
			int dockSize = DockUtil.getBgHeight();
			outRect.right = mRight - dockSize;
		} else {
			outRect.right = mRight;
		}
	}

	@Override
	public boolean onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, DropAnimationInfo resetInfo) {
		mCenterX = x - xOffset;
		mCenterY = y - yOffset;
		if (mStatus == STATUS_DEL) {
			mHandler.removeMessages(HANDLE_OVER_TRASH_EADGE);
			if (!(source instanceof GLSense)) {
				mIsDrop = true;
				mDragControler.setDelayDropCompleted(true);
				activeDeleteAnimation(mCenterX, mCenterY, dragView, dragInfo);
			}
		} else {
			final Intent uninstallIntent = mUninstallIntent;
			postDelayed(new Runnable() {
				@Override
				public void run() {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.UNINSTALL_APP_FROM_DELETEZONE, 1, uninstallIntent);
//					AppUtils.uninstallPackage(ShellAdmin.sShellManager.getActivity(), mPackageName);
				}
			}, 300);
//			if (mDragView != null) {
//				mDragView.cleanup();
//			}
			mDragView = null;
			mDragInfo = null;
			mUninstallIntent = null;
			mShell.hide(IViewId.DELETE_ZONE, true);
			return false;
		}
		return true;
	}

	private void activeDeleteAnimation(int centerX, int centerY, DragView dragView, Object dragInfo) {
		// 显示红色的发散射线背景
		// 如果没有对齐顶部的黑色空隙，对齐之
		if (dragView == null) {
			return;
		}
		dragView.setAlpha(0);
		dragView.setVisibility(GONE);
		dragView.clearAnimation();
//		mAnimBg.clearAnimation();
//		if (mAnimBgContainer.getTop() != mTrashcanView.getHeight() / 2) {
//			final GLRelativeLayout.LayoutParams lp = (GLRelativeLayout.LayoutParams) mAnimBgContainer
//					.getLayoutParams();
//			lp.topMargin = mTrashcanView.getHeight() / 2;
//			mAnimBgContainer.setLayoutParams(lp);
//		}
//		// 将图片scroll到与拖拽view对齐
//		mAnimBgContainer.scrollTo(-(centerX - mAnimBg.getWidth() / 2), 0);
//		mAnimBgContainer.setVisible(true);

		if (mAnimClipContainer.getTop() != mTrashcanView.getHeight() / 2) {
			final GLRelativeLayout.LayoutParams lp = (GLRelativeLayout.LayoutParams) mAnimClipContainer
					.getLayoutParams();
			lp.topMargin = mTrashcanView.getHeight() / 2;
			mAnimClipContainer.setLayoutParams(lp);
		}
		mAnimClipContainer.setClipAnim(false);
		mDeleteAnimView = new GLDeleteAnimView(mContext, dragView,
				mAnimClipContainer);
		mDeleteAnimView.show(0, 0);
		mAnimClipContainer.setVisible(true);

		final Animation animation = createDeleteAnimation(dragView, dragInfo, centerX, centerY);
		//		mDeleteAnimView.startAnimation(animation);
		AnimationTask task = new AnimationTask(mDeleteAnimView, animation, this, true,
				AnimationTask.PARALLEL);
		GLAnimationManager.startAnimation(task);
	}

	private Animation createDeleteAnimation(final DragView dragView, final Object dragInfo,
			int centerX, int centerY) {
		AnimationSet set = new AnimationSet(true);

		// 通过投影算出拖拽view在屏幕层显示时的XY坐标，保存在res，注意拿到的Y是相反数，运算时需要取反
		float[] res = new float[3]; // CHECKSTYLE IGNORE
		getGLRootView().projectFromWorldToReferencePlane(dragView.getLeft(), -dragView.getTop(),
				dragView.getDragViewDepth(), res);

		// 算出删除栏黑色空隙的Y坐标
		float clipTopR = mStartAnimationToY + mTrashcanView.getHeight() / 2;

		// 计算双方Y坐标的差值Δy，由于动画作用在拖拽view上，相应长度。转换方式为用这个Δy去除以投影的放大系数。
		float moveDistanceY = clipTopR + res[1];

		// Y轴压缩，X轴放大动画
		ScaleAnimation scale1 = new ScaleAnimation(AnimationConstant.DRAG_SCALE_UP,
				DELETE_ICON_X_EXPAND, AnimationConstant.DRAG_SCALE_UP, DELETE_ICON_Y_COL,
				Animation.RELATIVE_TO_SELF, HALF, Animation.RELATIVE_TO_SELF, 0.0f);
		scale1.setDuration(ANIMATION_DURATION_SLIDE);

//		scale1.setAnimationListener(new AnimationListenerAdapter() {
//			@Override
//			public void onAnimationEnd(Animation animation) {
//				// 开启裁剪，以便下一个飞出去的动画能够被父容器正确裁掉
//				mAnimClipContainer.setClipAnim(true);
//			}
//		});

		// 平移到删除栏中间的黑色空隙的平移动画
		TranslateAnimation translate = new TranslateAnimation(centerX - dragView.getWidth() / 2,
				centerX - dragView.getWidth() / 2, -moveDistanceY, 0);
		translate.setDuration(ANIMATION_DURATION_SLIDE);

		// 放大动画，将压扁的图标恢复为原样
		ScaleAnimation scale2 = new ScaleAnimation(1.0f, DELETE_ICON_X_COL, 1.0f,
				DELETE_ICON_Y_EXPAND, Animation.RELATIVE_TO_SELF, HALF, Animation.RELATIVE_TO_SELF,
				1.0f); /* {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation3D t) {
				super.applyTransformation(interpolatedTime, t);
				if (mDeleteAnimView != null) {
					mDeleteAnimView.setAlpha((int) ((1.0f - Math.pow(interpolatedTime,
							DELETE_ICON_ALPHA_POW)) * AnimationConstant.ANIMATION_MAX_ALPHA));
				}
			}
		};*/
		scale2.setStartOffset(ANIMATION_DURATION_SLIDE);
		scale2.setDuration(/*ANIMATION_DURATION_GONE*/ANIMATION_DURATION_SLIDE);
//		if ((dragInfo instanceof ItemInfo && ((ItemInfo) dragInfo).mItemType == IItemType.ITEM_TYPE_APP_WIDGET)
//				|| dragInfo instanceof GoWidgetInfo) {
//			// APP WIDGET 高度太大，要加一个平移裁剪的来移出
//			TranslateAnimation translate2 = new TranslateAnimation(0, 0, 0,
//					-dragView.getHeight() / 2.0f);
//			translate2.setStartOffset(ANIMATION_DURATION_SLIDE);
//			translate2.setDuration(ANIMATION_DURATION_GONE);
//			set.addAnimation(translate2);
//		}
		
		// 图标移动出顶部的动画
		TranslateAnimation translate2 = new TranslateAnimation(0, 0, 0,
				-dragView.getHeight() /*/ 2.0f - mTrashcanView.getHeight()*/);
		translate2.setStartOffset(ANIMATION_DURATION_SLIDE);
		translate2.setDuration(ANIMATION_DURATION_GONE);
		set.addAnimation(translate2);
		// 缩小动画，将图标以中点为锚点做等比例缩小到0.2，中间加上透明变化
		ScaleAnimation scale3 = new ScaleAnimation(1.0f, 0.2f, 1.0f, 0.2f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f) {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation3D t) {
				super.applyTransformation(interpolatedTime, t);
				if (mDeleteAnimView != null) {
					mDeleteAnimView.setAlpha((int) ((1.0f - Math.pow(interpolatedTime,
							DELETE_ICON_ALPHA_POW)) * AnimationConstant.ANIMATION_MAX_ALPHA));
				}
			}
		};
		scale3.setStartOffset(ANIMATION_DURATION_SLIDE);
		scale3.setDuration(ANIMATION_DURATION_GONE);
		set.addAnimation(scale3);
		
		set.addAnimation(scale2);
		set.addAnimation(scale1);
		set.addAnimation(translate);
		set.setFillAfter(true);
		return set;
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		openTrashcan();
		mDragView = dragView;
		mDragInfo = dragInfo;
		mStatus = STATUS_DEL;
		mDragControler.setDragScroller(null);
		mHandler.sendEmptyMessageDelayed(HANDLE_OVER_TRASH_EADGE, TRASH_WAIT_DURATION);
	}

	private void openTrashcan() {
		mBgHover = GLImageUtil.getGLDrawable(R.drawable.gl_trashcan_bg_hover);
		mTrashOpen = GLImageUtil.getGLDrawable(R.drawable.gl_trashcan_open);
		mTrashcanView.setBackgroundDrawable(mBgHover);
		mTrashcanView.setImageDrawable(mTrashOpen);
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {

	}

	@Override
	public void onDragExit(DragSource source, DropTarget nextTarget, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		if (!mIsDrop && !mDragControler.isKeepDragging()) {
			closeTrashcan();
			mHandler.removeMessages(HANDLE_OVER_TRASH_EADGE);
		}
	}

	private void closeTrashcan() {
		mBgNormal = GLImageUtil.getGLDrawable(R.drawable.gl_trashcan_bg_normal);
		mTrashNormal = GLImageUtil.getGLDrawable(R.drawable.gl_trashcan);
		mTrashcanView.setBackgroundDrawable(mBgNormal);
		mTrashcanView.setImageDrawable(mTrashNormal);
	}

	@Override
	public void onDragMove(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTopViewId(int id) {

	}

	@Override
	public int getTopViewId() {
		return IViewId.SCREEN;
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
	public void setVisible(boolean visible, boolean animate, Object obj) {
		mForceHandleStatusBar = false;
		if (obj instanceof Object[]) {
			Object[] b = (Object[]) obj;
			if (b.length > 0 && b[0] instanceof Boolean) {
				mForceHandleStatusBar = (Boolean) b[0];
			}
		}
		if (animate) {
			int height = mTrashcanView.getDrawable().getIntrinsicHeight();
			int fromY = -height;
			int toY = 0;
			if (visible) {
				mIsDrop = false;
				setVisible(true);
				Animation inAnimation = new TranslateAnimation(0.0f, 0.0f, fromY, toY);
				inAnimation.setDuration(ANIMATION_DURATION_IN_OUT);
				AnimationTask task = new AnimationTask(this, inAnimation, new AnimationListenerAdapter() {

					@Override
					public void onAnimationEnd(Animation animation) {
						mDragControler.addDropTarget(GLDeleteZone.this, getTopViewId());
					}
				}, false, AnimationTask.PARALLEL);
				GLAnimationManager.startAnimation(task);
				
				if (mForceHandleStatusBar || mShell.getCurrentStage() != IShell.STAGE_PREVIEW) {
					if (!StatusBarHandler.isHide()) {
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								ICommonMsgId.SHOW_HIDE_STATUSBAR, -2, true, null);
					}
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_SHOW_OR_HIDE_INDICATOR, 0);
				}
			} else {
				Animation outAnimation = new TranslateAnimation(0.0f, 0.0f, toY, fromY);
				outAnimation.setDuration(ANIMATION_DURATION_IN_OUT);
				AnimationTask task = new AnimationTask(this, outAnimation, new AnimationListenerAdapter() {

					@Override
					public void onAnimationStart(Animation animation) {
						mDragControler.removeDropTarget(GLDeleteZone.this);
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						setVisible(false);
						post(new Runnable() {
							@Override
							public void run() {
								clearAnimation();
							}
						});
						if (mDeleteAnimView != null) {
							mDeleteAnimView.cleanup();
							mDeleteAnimView = null;
						}
						if (mForceHandleStatusBar || mShell.getCurrentStage() != IShell.STAGE_PREVIEW) {
							if (!StatusBarHandler.isHide()) {
								MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
										ICommonMsgId.SHOW_HIDE_STATUSBAR, -2, false, null);
							}
							MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
									IScreenFrameMsgId.SCREEN_SHOW_OR_HIDE_INDICATOR, 1);
						}
					}
				}, false, AnimationTask.PARALLEL);
				GLAnimationManager.startAnimation(task);
			}
		}
	}
	@Override
	public void setShell(IShell shell) {
		mShell = shell;
	}

	public void setDragController(DragController controler) {
		mDragControler = controler;
	}

	@Override
	public int getViewId() {
		return IViewId.DELETE_ZONE;
	}

	@Override
	public void onAdd(GLViewGroup parent) {
		//		mBgNormal = GLImageUtil.getGLDrawable(R.drawable.gl_trash_bg_normal);
		//		mBgHover = GLImageUtil.getGLDrawable(R.drawable.gl_trash_bg_hover);
		//		mTrashNormal = GLImageUtil.getGLDrawable(R.drawable.gl_trash);
		//		mTrashOpen = GLImageUtil.getGLDrawable(R.drawable.gl_trash_open);
		//		mTrashView.setBackgroundDrawable(mBgNormal);
		//		mTrashView.setImageDrawable(mTrashNormal);
	}

	@Override
	public void onRemove() {
		if (mBgNormal != null) {
			mBgNormal.clear();
			mBgNormal = null;
		}
		if (mBgHover != null) {
			mBgHover.clear();
			mBgHover = null;
		}
		if (mTrashNormal != null) {
			mTrashNormal.clear();
			mTrashNormal = null;
		}
		if (mTrashOpen != null) {
			mTrashOpen.clear();
			mTrashOpen = null;
		}
	}

	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		if (source instanceof GLDock || source instanceof GLWorkspace || source instanceof GLSense) {
			mShell.show(IViewId.DELETE_ZONE, true, true); //forceHandleStatus = true 强制隐藏状态栏
		}
	}

	@Override
	public void onDragEnd() {
		if (!mIsDrop && mShell.isViewVisible(IViewId.DELETE_ZONE)) {
			mShell.hide(IViewId.DELETE_ZONE, true, true); //forceHandleStatus = true 强制显示状态栏
		}
	}

	@Override
	public void onAnimationStart(Animation animation) {
		
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		closeTrashcan();

//		final ScaleAnimation collapsepanBgAnimation = new ScaleAnimation(1.0f, RED_RAY_BG_X_COL,
//				1.0f, 1.0f, Animation.RELATIVE_TO_SELF, HALF, Animation.RELATIVE_TO_SELF, 0.0f);
//		collapsepanBgAnimation.setDuration(ANIMATION_DURATION_BG_COLLAPSE);
//
//		final ScaleAnimation alphaAnimation = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f,
//				Animation.RELATIVE_TO_SELF, HALF, Animation.RELATIVE_TO_SELF, 0.0f);
//		alphaAnimation.setDuration(ANIMATION_DURATION_GONE);
//		alphaAnimation.setStartOffset(ANIMATION_DURATION_BG_COLLAPSE);
//
//		final AnimationSet set = new AnimationSet(false);
//		set.addAnimation(alphaAnimation);
//		set.addAnimation(collapsepanBgAnimation);
//		set.setFillAfter(false);
//
//		set.setAnimationListener(new AnimationListener() {
//			@Override
//			public void onAnimationEnd(Animation animation) {
//				mAnimBgContainer.setVisible(false);
				post(new Runnable() {
					@Override
					public void run() {
						if (mIsDrop && mDragControler != null
								&& mDragControler.isDelayDropCompleted()) {
							mDragControler.continueDropCompleted(GLDeleteZone.this, false, true);
						}
//						if (mDragView != null) {
////							mDragView.getOriginalView().cleanup();
//							mDragView.cleanup();
//						}
						mDragView = null;
						mDragInfo = null;
						mShell.hide(IViewId.DELETE_ZONE, true, true);
						
					}
				});
//			}
//
//			@Override
//			public void onAnimationStart(Animation animation) {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void onAnimationRepeat(Animation animation) {
//				// TODO Auto-generated method stub
//
//			}
//		});
//		mAnimBg.startAnimation(set);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}
	
	@Override
	public void onAnimationProcessing(Animation animation, float interpolatedTime) {
		
	}

	@Override
	public boolean handleMessage(Object sender, int msgId, int param, Object... objects) {
		switch (msgId) {
			case IDeleteZoneMsgId.DELETE_ZONE_CONTINUE_DELETE_ANIMATION :
				mIsDrop = true;
				activeDeleteAnimation(mCenterX, mCenterY, mDragView, mDragInfo);
				mDragView = null;
				mDragInfo = null;
				break;
			case IDeleteZoneMsgId.DELETE_ZONE_CLOSE_TRASHCAN :
				closeTrashcan();
				break;
			default :
				break;
		}
		return false;
	}
	@Override
	public int getMsgHandlerId() {
		return IDiyFrameIds.DELETE_ZONE;
	}
}
