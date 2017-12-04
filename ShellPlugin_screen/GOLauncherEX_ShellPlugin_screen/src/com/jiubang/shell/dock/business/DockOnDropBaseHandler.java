package com.jiubang.shell.dock.business;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Rect;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnTouchListener;
import com.go.gl.view.GLViewGroup;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.util.ConvertUtils;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IFolderMsgId;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockLogicControler;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.FeatureItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.data.statistics.IGoLauncherUserBehaviorStatic;
import com.jiubang.ggheart.folder.FolderConstant;
import com.jiubang.shell.animation.DragAnimation;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.common.component.GLModel3DMultiView.FolderCoverAnimationListener;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.deletezone.GLDeleteZone;
import com.jiubang.shell.dock.GLDock;
import com.jiubang.shell.dock.component.AbsGLLineLayout;
import com.jiubang.shell.dock.component.DockDragHander;
import com.jiubang.shell.dock.component.GLDockIconView;
import com.jiubang.shell.dock.component.GLDockLineLayout;
import com.jiubang.shell.dock.component.Position;
import com.jiubang.shell.dock.component.ViewPositionTag;
import com.jiubang.shell.drag.DragInfoTranslater;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.drag.DropTarget;
import com.jiubang.shell.folder.GLAppFolder;
import com.jiubang.shell.folder.GLDockFolderGridVIew;
import com.jiubang.shell.folder.GLDockFolderIcon;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.popupwindow.component.actionmenu.QuickActionMenuHandler;
import com.jiubang.shell.preview.GLSense;
import com.jiubang.shell.screen.CellUtils;
import com.jiubang.shell.screen.GLWorkspace;
import com.jiubang.shell.utils.IconUtils;
/**
 * 
 * @author dingzijian
 *
 */
//CHECKSTYLE:OFF
public class DockOnDropBaseHandler {
	protected DragSource mDragSource;
	protected int mX;
	protected int mY;
	protected int mXOffset;
	protected int mYOffset;
	protected DragView mDragView;
	protected Object mDragInfo;
	protected int mFolderAnimationCenterX;
	protected int mFolderAnimationCenterY;
	protected DropAnimationInfo mResetInfo;
	protected DockLogicControler mDockLogicControler;
	protected DockDragHander mDockDragHander;
	protected GLDockLineLayout mDockLineLayout;
	protected boolean mNeedExtrusionAnimate;
	protected int mMergeFolderTYpe = -1;
	protected final static int ICON_TO_ICON_MERGE_FOLDER = 0;
	protected final static int ICON_TO_FOLDER_MERGE_FOLDER = 1;
	protected GLDockFolderIcon mGLDockFolderIcon = null; //做文件夹动画的view
	private GLDockIconView mTempView = null; // 占位
	protected long mFolderId = -1;
	protected float mCenterX; // view的中心点坐标 x
	protected float mCenterY; // view的中心点坐标 y
	protected final static int DOCK_TO_DOCK_CREATE_NEW_FOLDER = 0;
	protected final static int DOCK_TO_DOCK_CREATE_INTO_FOLDER = 1;
	protected final static int SCREEN_TO_DOCK_CREATE_INSERT_ICON_AND_FOLDER = 2;
	protected final static int APPDRAWER_TO_DOCK_CREATE_INSERT_ICON = 3;
	protected final static int APPDRAWER_TO_DOCK_CREATE_INSERT_FOLDER = 4;
	protected final static int FOLDER_TO_DOCK_CREATE_INSERT_ICON = 5;
	protected final static int FOLDER_FROM_DOCK_TO_DOCK_CREATE_INSERT_ICON = 6;
	private QuickActionMenuHandler mActionMenuHandler;

	public DockOnDropBaseHandler(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, DropAnimationInfo resetInfo,
			DockDragHander dockDragHander, GLDockLineLayout dockLineLayout) {
		mDragSource = source;
		mX = x;
		mY = y;
		mXOffset = xOffset;
		mYOffset = yOffset;
		mDragView = dragView;
		mDragInfo = dragInfo;
		mResetInfo = resetInfo;
		mDockLogicControler = DockLogicControler.getInstance();
		mDockDragHander = dockDragHander;
		mDockLineLayout = dockLineLayout;
		mActionMenuHandler = QuickActionMenuHandler.getInstance();
	}

	public DockOnDropBaseHandler(Object dragInfo, DropAnimationInfo resetInfo,
			DockDragHander dockDragHander, GLDockLineLayout dockLineLayout) {
		mDragInfo = dragInfo;
		mResetInfo = resetInfo;
		mDockLogicControler = DockLogicControler.getInstance();
		mDockDragHander = dockDragHander;
		mDockLineLayout = dockLineLayout;
		mActionMenuHandler = QuickActionMenuHandler.getInstance();
	}

	public boolean handleOnDrop() {
		GLView oriView = mDragView.getOriginalView();
		if (oriView != null && oriView instanceof IconView<?>) {
			float[] p = mDragView.getIconCenterPoint();
			mFolderAnimationCenterX = (int) p[0];
			mFolderAnimationCenterY = (int) p[1];
		} else {
			mFolderAnimationCenterX = mX - mXOffset;
			mFolderAnimationCenterY = mY - mYOffset;
		}

		if (mDockDragHander.mDragResult == DockDragHander.DRAG_RESULT_NONE) {
			mDockDragHander.hideFolder();
		}
		needExtrusionAnimate();
		return false;
	};

	public void handleOnDropComplete(DropTarget target, boolean success) {
		DockItemInfo info = (DockItemInfo) mDragInfo;
		if (success) {
			if (!(target instanceof GLDock)) {
				Long id = info.mItemInfo.mInScreenId;
				if (mDockLineLayout.delDockItemAndReArrange(info)) {
					if (!(target instanceof GLWorkspace)) {
						if (info.mItemInfo instanceof UserFolderInfo) {
							mDockLogicControler.removeDockFolder(info.mItemInfo.mInScreenId);
						}
					}
					mDockLineLayout.updateIndexAfterExtrusion();
					//ADT-15980 dock 拖动删除dock栏图标时，dock栏上会闪一下被删除的图标
					//新的布局将延迟刷新，期间旧图标会刷新一次，造成闪烁现象，故此改为立即刷新
							mDockLineLayout.updateIconsSizeAndRequestLayout();
						}
//				if (target instanceof GLDeleteZone) {
//					GLAppFolder.getInstance().batchStartIconEditEndAnimation();
//				}
			}
		} else {
			if (target instanceof GLWorkspace || target instanceof GLSense
					|| target instanceof GLDeleteZone) {
				// 桌面已满或者合成文件夹失败复位动画
				resetAnimation(mDockDragHander.mDragInitIndex, mResetInfo, false, mDragView,
						new AnimationListenerHandler(
								AnimationListenerHandler.HOMING_ANIMATION_FROM_OUTSIDE_TO_DOCK),
						mDockDragHander.getPositionNeedCount() + 1);
				//						new AnimationListener() {
				//
				//							@Override
				//							public void onAnimationStart(Animation animation) {
				//
				//							}
				//
				//							@Override
				//							public void onAnimationRepeat(Animation animation) {
				//
				//							}
				//
				//							@Override
				//							public void onAnimationEnd(Animation animation) {
				////								mDockDragHander.hideFolder();
				////								if (mDockLineLayout.getLongClickViewIndex() != mDockDragHander.mDragInitIndex) {
				//									mDockLineLayout.updateIndexAfterExtrusion();
				//									mDockLineLayout.updateIconsSizeAndRequestLayout();
				////								} else {
				////									mDockLineLayout.clearChilderenAnimation();
				////								}
				////								GLAppFolder.getInstance().batchStartIconEditEndAnimation();
				//							}
				//						});
				//				mDockDragHander.reset();
			}
		}
	};

	public void needExtrusionAnimate() {
	};

	/**
	 * 功能简述: 处理内部拖拽新建新文件夹的方法 功能详细描述: 注意:
	 * 
	 * @param shortcutInfo
	 */
	protected boolean isMergeFolder(DragView dragView, Object dragInfo) {
		GuiThemeStatistics
				.goLauncherUserBehaviorStaticDataCache(IGoLauncherUserBehaviorStatic.LAUNCHER_OP_01);
		if (mDockDragHander.mDragFolderIndex < 0) {
			mDockDragHander.hideFolder();
			//TODO ???
			//			mHandler.sendEmptyMessage(NEED_TO_LAYOUT);
			onDropFinish();
			return false;
		}

		boolean ret = false;
		IconView mergeView_target = mDockDragHander.getMergedView();
		if (mergeView_target != null) {

			DockItemInfo itemInfo_target = (DockItemInfo) ((mergeView_target != null)
					? mergeView_target.getInfo()
					: null);

			ItemInfo itemInfo_drag = DragInfoTranslater.createItemInfoForDock(
					dragView.getDragViewType(), dragInfo);

			if (null != mergeView_target && null != itemInfo_target
					&& null != itemInfo_target.mItemInfo
					&& itemInfo_target.mItemInfo instanceof ShortCutInfo && null != itemInfo_drag
					&& itemInfo_drag instanceof ShortCutInfo) {
				// 添加文件夹
				ItemInfo oldInfo = itemInfo_target.mItemInfo;
				UserFolderInfo folderInfo = new UserFolderInfo();
				folderInfo.mTitle = ShellAdmin.sShellManager.getContext().getText(R.string.folder_name); // 文件夹名称
				folderInfo.mInScreenId = System.currentTimeMillis(); // 文件夹ID
				itemInfo_target.setInfo(folderInfo);
				mDockLogicControler.updateDockItem(oldInfo.mInScreenId, itemInfo_target);
				if (oldInfo.mInScreenId == 0 || oldInfo.mInScreenId == -1) {
					oldInfo.mInScreenId = System.currentTimeMillis() + 1;
				}
				if (((ShortCutInfo) oldInfo).mIntent != null
						&& ((ShortCutInfo) itemInfo_drag).mIntent != null) {
					final boolean flag = ConvertUtils.intentCompare(
							((ShortCutInfo) oldInfo).mIntent,
							((ShortCutInfo) itemInfo_drag).mIntent);

					if (flag) {
						mDockLogicControler.addItemToFolder(oldInfo, folderInfo);
					} else {
						mDockLogicControler.addItemToFolder(oldInfo, folderInfo);
						mDockLogicControler.addItemToFolder(itemInfo_drag, folderInfo);
					}

					// 移除抓起来的view和 被合成的view
					if (mDragSource instanceof GLDock) {
						int rowId;
						if (dragInfo instanceof DockItemInfo) {
							rowId = ((DockItemInfo) dragInfo).getmRowId();
						} else {
							rowId = mDockLineLayout.getLineID();
						}
						mDockLogicControler.deleteShortcutItemAndReArrange(rowId, itemInfo_drag.mInScreenId);
					}
					mergeFolderAnimation(mergeView_target, dragView, ICON_TO_ICON_MERGE_FOLDER,
							null, dragInfo, -1, flag);

					ret = true;
				}
			}

		} else {
			ret = false;
		}
		return ret;
	}
	/**
	 * 为做文件夹动画做准备
	 * 
	 * @param glDockFolderIcon
	 * @param dragView
	 * @param mergeView_target
	 */
	private void newLayoutReadyForFolderAnimation(GLDockFolderIcon glDockFolderIcon,
			DragView dragView, IconView mergeView_target) {
		GLDockLineLayout lineLayout = mDockLineLayout;
		//		ArrayList<IconView<?>> dockViewList = mIconViewsHashMap.get(rowId);

		// 被合成的view的下标
		int dragFolderIndex = mDockDragHander.mDragFolderIndex;
		int dragIndex = mDockDragHander.mDragIndex;

		// 计算区域范围
		Rect rect = lineLayout.getInitRectList().get(dragFolderIndex).outRect;

		LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		int childHeightSpec = GLViewGroup.getChildMeasureSpec(
				MeasureSpec.makeMeasureSpec(rect.height(), MeasureSpec.EXACTLY), 0, p.height);
		int childWidthSpec = GLViewGroup.getChildMeasureSpec(
				MeasureSpec.makeMeasureSpec(rect.width(), MeasureSpec.EXACTLY), 0, p.width);

		if (mDragSource instanceof GLDock) {
			// 移除抓起来的view和 被合成的view
			//			dockViewList.remove(dragView.getOriginalView());
			lineLayout.removeViewInLayout(dragView.getOriginalView());
		}
		// 移除抓起来的view和 被合成的view
		//		dockViewList.remove(mergeView_target);
		lineLayout.removeViewInLayout(mergeView_target);

		glDockFolderIcon.measure(childWidthSpec, childHeightSpec);
		if (GoLauncherActivityProxy.isPortait()) {
			glDockFolderIcon.layout(rect.left, 0, rect.right, rect.bottom - rect.top);
		} else {
			glDockFolderIcon.layout(0, rect.top, rect.right - rect.left, rect.bottom);
		}
		LayoutParams layoutParams = mDockLineLayout.getLayoutParams();
		if (mNeedExtrusionAnimate) {
			mTempView = new GLDockIconView(mDockLineLayout.getContext());
			// 判断新的的文件夹view添加到哪个位置
			if (dragFolderIndex > dragIndex) {
				lineLayout.addViewInLayout(mTempView, dragFolderIndex - 1, layoutParams, true);
				lineLayout.addView(glDockFolderIcon, dragFolderIndex, layoutParams);
				//				dockViewList.add(dragFolderIndex - 1, glDockFolderIcon);
			} else {
				lineLayout.addViewInLayout(mTempView, dragFolderIndex, layoutParams, true);
				lineLayout.addView(glDockFolderIcon, dragFolderIndex, layoutParams);

				//				dockViewList.add(dragFolderIndex, glDockFolderIcon);
			}
		} else {
			lineLayout.addView(glDockFolderIcon, dragFolderIndex, layoutParams);
			//			dockViewList.add(dragFolderIndex, glDockFolderIcon);
		}
		//		TODO ????? clearAnimationAndresetFlag();
		mDockLineLayout.clearChilderenAnimation();
	}

	private boolean isTheSameIconInToFolder(UserFolderInfo info, Object dragInfo) {
		int size = info.getContents().size();
		Intent desIntent = null;
		if (dragInfo instanceof DockItemInfo) {
			ShortCutInfo shortCutInfo = (ShortCutInfo) ((DockItemInfo) dragInfo).mItemInfo;
			desIntent = shortCutInfo.mIntent;
		} else if (dragInfo instanceof FunAppItemInfo) {
			desIntent = ((FunAppItemInfo) dragInfo).getAppItemInfo().mIntent;

		} else if (dragInfo instanceof ShortCutInfo) {
			desIntent = ((ShortCutInfo) dragInfo).mIntent;
		}
		for (int i = 0; i < size; i++) {
			ShortCutInfo app = (ShortCutInfo) info.getContents().get(i);
			if (ConvertUtils.intentToStringCompare(app.mIntent, desIntent)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 合成文件夹动画。
	 * 
	 * @param mergeViewTarget
	 * @param dragView
	 * @param id
	 */
	protected void mergeFolderAnimation(final IconView<?> mergeViewTarget, DragView dragView,
			int type, DropAnimationInfo resetInfo, Object dragInfo, int toAddIndex,
			boolean isDuplicate) {
		mDockLineLayout.showProtectLayer();
		mDockLineLayout.setIsFolderAnimationBegin(true);
		mMergeFolderTYpe = type;
		if (type == ICON_TO_ICON_MERGE_FOLDER) {
			// 生成新的文件夹view
			DockItemInfo info = (DockItemInfo) mergeViewTarget.getInfo();
			if (info.mItemInfo instanceof UserFolderInfo) {
				mGLDockFolderIcon = (GLDockFolderIcon) mDockLineLayout.getAdapter().initIcon(info);
				mGLDockFolderIcon.refreshIcon();
				mGLDockFolderIcon.setOnTouchListener((OnTouchListener) mDockLineLayout
						.getGLParent());
				mGLDockFolderIcon.setOnClickListener(mDockLineLayout);
				mGLDockFolderIcon.setOnLongClickListener(mDockLineLayout);
				mGLDockFolderIcon.setFocusable(true);
			}

			newLayoutReadyForFolderAnimation(mGLDockFolderIcon, dragView, mergeViewTarget);

			mergeViewTarget.cleanup();

			mGLDockFolderIcon.setFolderCoverAnimationListner(new AnimationListenerHandler(
					AnimationListenerHandler.CREATE_FOLDER_COVER_ANIMATION));
			mGLDockFolderIcon.createFolderAnimation(mFolderAnimationCenterX,
					mFolderAnimationCenterY, null);

		} else {
			resetInfo.setDuration(0);
			mGLDockFolderIcon.refreshForAddIcon(toAddIndex);
			newLayoutReadyForFolderAnimation(mGLDockFolderIcon, dragView, mergeViewTarget);
			mGLDockFolderIcon.setFolderCoverAnimationListner(new AnimationListenerHandler(
					AnimationListenerHandler.ADD_IN_FOLDER_COVER_ANIMATION));
			mGLDockFolderIcon.addInFolderAnimation(mFolderAnimationCenterX,
					mFolderAnimationCenterY, toAddIndex, null);
			//					new AnimationListener() {
			//
			//						@Override
			//						public void onAnimationStart(Animation animation) {
			//
			//						}
			//
			//						@Override
			//						public void onAnimationRepeat(Animation animation) {
			//
			//						}
			//
			//						@Override
			//						public void onAnimationEnd(Animation animation) {
			//							mDockLineLayout.setIsFolderAnimationBegin(false);
			//							mDockLineLayout.postDelayed(new Runnable() {
			//
			//								@Override
			//								public void run() {
			//									GLAppFolder.getInstance().batchStartIconEditEndAnimation();
			//								}
			//
			//							}, 50);
			//
			//							if (((GLDockFolderIcon) mergeViewTarget).getFolderChildCount() > 4) {
			//								mDockLineLayout.postDelayed(new Runnable() {
			//									@Override
			//									public void run() {
			//										((GLDockFolderIcon) mergeViewTarget)
			//												.refreshForAddIcon(BaseFolderIcon.MAX_ICON_COUNT);
			//									}
			//								}, 1000);
			//							}
			//						}
			//					}
//					);
		}
	}
	//	@Override
	//	public void onFolderCoverAnimationEnd(boolean isOpened, GLView icon) {
	//		if (isOpened /*|| mIsMoreThanFourIconInFoler*/) {
	//			return;
	//		}
	//		mDockLineLayout.setIsFolderAnimationBegin(false);
	//		mDockLineLayout.getGLRootView().postOnFrameRendered(new Runnable() {
	//
	//			@Override
	//			public void run() {
	//				doFolderTaskLessThanFour();
	//			}
	//		});
	//	}

	/**
	 * 针对文件夹里面小于4个的处理
	 */
	//	private void doFolderTaskLessThanFour() {
	////		if (mDockDragHander.mIsShowFolderAnimation) {
	//			
	//				//				TODO ????? mHandler.sendEmptyMessage(FOLDER_ANIMATION_END);
	//			}
	//		}
	//	}

	private void doInsideExtrusion() {
		GLDockLineLayout lineLayout = mDockLineLayout;
		int count = lineLayout.getChildCount();
		if (count == 0) {
			onDropFinish();
			return;
		}
		lineLayout.setInitRect(count);
		ArrayList<Position> changeRectList = lineLayout.getInitRectList();
		AnimationTask task = new AnimationTask(false, AnimationTask.PARALLEL);
		task.setBatchAnimationObserver(new AnimationListenerHandler(
				AnimationListenerHandler.EXTRUSION_ANIMATION),
				AnimationListenerHandler.EXTRUSION_ANIMATION, task);
		for (int i = 0; i < count; i++) {
			IconView view = (IconView) lineLayout.getChildAt(i);
			Animation animation = null;
			if (view != null && view.isVisible()) {
				Rect rect = changeRectList.get(i).outRect;
				if (GoLauncherActivityProxy.isPortait()) {
					int rectCenter = rect.left + (rect.right - rect.left) / 2;
					int viewCenter = view.getLeft() + (view.getRight() - view.getLeft()) / 2;
					animation = new TranslateAnimation(0, rectCenter - viewCenter, 0, 0);
				} else {
					int rectCenter = rect.top + (rect.bottom - rect.top) / 2;
					int viewCenter = view.getTop() + (view.getBottom() - view.getTop()) / 2;
					animation = new TranslateAnimation(0, 0, 0, rectCenter - viewCenter);
				}

				animation.setInterpolator(new AccelerateDecelerateInterpolator());
				animation.setDuration(100);
				animation.setFillAfter(true);
				task.addAnimation(view, animation, null);
			}
		}
		GLAnimationManager.startAnimation(task);
	}
	/**
	 * 功能简述: 处理内部拖拽到文件夹中 功能详细描述: 注意:
	 * 
	 * @param resetInfo
	 * @param dragInfo
	 * @return
	 */
	protected boolean isIntoFolder(DragView dragView, Object dragInfo, DropAnimationInfo restInfo) {
		GuiThemeStatistics
		.goLauncherUserBehaviorStaticDataCache(IGoLauncherUserBehaviorStatic.LAUNCHER_OP_02);
		if (mDockDragHander.mDragFolderIndex < 0) {
			mDockDragHander.hideFolder();
			onDropFinish();
			return false;
		}

		boolean ret = false;

		IconView mergeViewTarget = mDockDragHander.getMergedView();

		if (mergeViewTarget != null) {
			// try {
			DockItemInfo itemInfoTarget = (DockItemInfo) mergeViewTarget.getInfo();
			ItemInfo itemInfoDrag = DragInfoTranslater.createItemInfoForDock(
					dragView.getDragViewType(), dragInfo);

			if (null != itemInfoTarget && null != itemInfoTarget.mItemInfo
					&& itemInfoTarget.mItemInfo instanceof UserFolderInfo && null != itemInfoDrag
					&& itemInfoDrag instanceof ShortCutInfo) {
				UserFolderInfo folderInfo = (UserFolderInfo) itemInfoTarget.mItemInfo;

				// 处理自己跟自己合并文件夹的问题
				boolean isDuplicate = isTheSameIconInToFolder(folderInfo, dragInfo);
				if (mFolderId != folderInfo.mInScreenId && !isDuplicate) {
					mDockLogicControler.addItemToFolder(itemInfoDrag, folderInfo);
					//					mDockControler.updateFolderIconAsync(itemInfoTarget, false);
					// invalidate(); // 插入文件夹后需要刷新界面，解决挤压后重影的问题
				}
				mGLDockFolderIcon = (GLDockFolderIcon) mergeViewTarget;

				int toAddIndex = getAddInFolderIndex(mGLDockFolderIcon, dragInfo);
				if (isDuplicate && folderInfo.getContents().size() >= FolderConstant.MAX_ICON_COUNT) {
					// 图标去重
					mDockLogicControler.removeDockFolderItems(folderInfo,
							(ShortCutInfo) itemInfoDrag);
					mDockLogicControler.addItemToFolder(itemInfoDrag, folderInfo);
					toAddIndex = folderInfo.getContents().size();
				}
				// 移除抓起来的view和 被合成的view
				if (mDragSource instanceof GLDock) {
					int rowId;
					if (dragInfo instanceof DockItemInfo) {
						rowId = ((DockItemInfo) dragInfo).getmRowId();
					} else {
						rowId = mDockLineLayout.getLineID();
					}
					mDockLogicControler.deleteShortcutItemAndReArrange(rowId, itemInfoDrag.mInScreenId);
				}
				mergeFolderAnimation(mergeViewTarget, dragView, ICON_TO_FOLDER_MERGE_FOLDER,
						restInfo, dragInfo, toAddIndex, isDuplicate);

				ret = true;
			}
			// }
			// catch (Exception e) {
			// }
		} else {
			ret = false;
		}

		return ret;

	}
	/**
	 * 获取是进入文件夹的应用是第几个
	 * 
	 * @param folderIcon
	 * @param dragInfo
	 * @return
	 */
	private int getAddInFolderIndex(GLDockFolderIcon folderIcon, Object dragInfo) {
		DockItemInfo info = folderIcon.getInfo();
		UserFolderInfo userFolderInfo = (UserFolderInfo) info.mItemInfo;
		int size = userFolderInfo.getContents().size();
		int toAddIndex = size;
		Intent desIntent = null;
		if (dragInfo instanceof DockItemInfo) {
			ShortCutInfo shortCutInfo = (ShortCutInfo) ((DockItemInfo) dragInfo).mItemInfo;
			desIntent = shortCutInfo.mIntent;
		} else if (dragInfo instanceof FunAppItemInfo) {
			desIntent = ((FunAppItemInfo) dragInfo).getAppItemInfo().mIntent;

		} else if (dragInfo instanceof ShortCutInfo) {
			desIntent = ((ShortCutInfo) dragInfo).mIntent;
		}
		for (int i = 0; i < size; i++) {
			ShortCutInfo app = (ShortCutInfo) userFolderInfo.getContents().get(i);
			if (ConvertUtils.intentToStringCompare(app.mIntent, desIntent)) {
				toAddIndex = i;
				break;
			}
		}

		return toAddIndex;
	}

	//	@Override
	//	public void onFinish(int what, Object[] params) {
	//		switch (what) {
	//			case FOLDER_ANIMATION_END :
	//				if (mMergeFolderTYpe == ICON_TO_FOLDER_MERGE_FOLDER
	//						|| mMergeFolderTYpe == ICON_TO_ICON_MERGE_FOLDER) {
	//					onDropAnimationFinish();
	//				}
	//				AnimationTask task = (AnimationTask) params[0];
	//				task.cleanup();
	//				break;
	//
	//			default :
	//				break;
	//		}
	//
	//	}
	// 复位动画
	protected void resetAnimation(final int dragIndex, DropAnimationInfo resetInfo,
			boolean isInDock, final DragView dragView, AnimationListener animationListener,
			int count) {
		if (!isInDock) {
			backAnimation();
		}

		if (!caculateCenterXY(dragIndex, count)) {
			return;
		}
		resetInfo.setLocationPoint(mCenterX, mCenterY);
		resetInfo.setDuration(DragAnimation.DURATION_200);
		resetInfo.setLocationType(DropAnimationInfo.LOCATION_CENTER);
		resetInfo.setAnimationListener(animationListener);
		// 交換位置
		mDockLogicControler.extrudeShortcutItem(mDockLineLayout.getLineID(), mDockDragHander.mDragInitIndex, mDockDragHander.mDragIndex);
	}

	// 归位动画
	private void backAnimation() {
		//			int curRow = getLineLayoutContainer().getCurLine();
		//			mCurDockViewList = mIconViewsHashMap.get(curRow);
		int childCount = mDockLineLayout.getChildCount();
		AnimationTask task = new AnimationTask(true, AnimationTask.PARALLEL);
		task.setBatchAnimationObserver(new AnimationListenerHandler(
				AnimationListenerHandler.HOMING_ANIMATION),
				AnimationListenerHandler.HOMING_ANIMATION);
		for (int i = 0; i < childCount; i++) {

			IconView view = (IconView) mDockLineLayout.getChildAt(i);
			view.setTag(R.integer.dock_index, i);

			Animation animation = null;
			Animation animationScale = null;
			AnimationSet animationSet = animationSet = new AnimationSet(true);
			animationScale = mDockDragHander.getZoomAnimation(DockUtil.ANIMATION_ZOOM_BIG_TO_SMALL,
					mDragSource);
			if (animationScale != null) {
				animationSet.addAnimation(animationScale);
			}
			if (view.isVisible()) {
				ViewPositionTag tag = (ViewPositionTag) view.getTag(R.integer.dock_view_left);
				int fromX = tag.newLayoutX - tag.oldLayoutX;
				if (GoLauncherActivityProxy.isPortait()) {
					animation = new TranslateAnimation(fromX, 0, 0, 0);
				} else {
					animation = new TranslateAnimation(0, 0, fromX, 0);
				}
				animation.setFillAfter(true);
				animationSet.addAnimation(animation);
				animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
				animationSet.setDuration(DockDragHander.EXTRUSION_ANIMATION_DURATION);
				animationSet.setFillAfter(true);
				task.addAnimation(view, animationSet, null);
			}
		}
		GLAnimationManager.startAnimation(task);

	}
	protected boolean caculateCenterXY(int dragIndex, int count) {
		mDockLineLayout.setInitRect(count);
		ArrayList<Position> changeRectList = mDockLineLayout.getInitRectList();
		if (dragIndex < 0 || changeRectList.size() == dragIndex) {
			onDropFinish();
			return false;
		}
		float left = changeRectList.get(dragIndex).outRect.left;
		float right = changeRectList.get(dragIndex).outRect.right;
		float top = changeRectList.get(dragIndex).outRect.top;
		float bottom = changeRectList.get(dragIndex).outRect.bottom;
//		int xOffset = 0;
//		int yOffset = 0;
//		if (Machine.IS_SDK_ABOVE_KITKAT) {
//			if (DrawUtils.getNavBarLocation() == DrawUtils.NAVBAR_LOCATION_BOTTOM) {
//				yOffset = DrawUtils.getNavBarHeight();
//			} else {
//				xOffset = DrawUtils.getNavBarWidth();
//			}
//		}
		mCenterX = left + (right - left) / 2/* - xOffset*/;
		mCenterY = top + (bottom - top) / 2/* - yOffset*/;
		if (!GoLauncherActivityProxy.isPortait() && !StatusBarHandler.isHide()) {
			mCenterY = mCenterY + StatusBarHandler.getStatusbarHeight();
		}
		// 竖屏下，拖拽还原动画卡顿，x坐标计算有误差
		if (GoLauncherActivityProxy.isPortait()) {
			mCenterX += CellUtils.sLeftGap;
		}
		return true;
	}

	protected boolean dragOverHandle(int dragType, boolean mergeFolder, boolean intoFolder,
			DragView dragView, int toIndexInRow, AbsGLLineLayout lineLayout, int rowId,
			Object dragInfo) {
		boolean ret = false;
		int count = 0;
		if (lineLayout != null) {
			count = lineLayout.getChildCount();
		}
		switch (dragType) {
			case SCREEN_TO_DOCK_CREATE_INSERT_ICON_AND_FOLDER :
				FeatureItemInfo screenInfo = (FeatureItemInfo) DragInfoTranslater
						.createItemInfoForDock(dragView.getDragViewType(), dragInfo);
				screenItemDragHandle(screenInfo, count, rowId, toIndexInRow);
				ret = true;
				break;
			case APPDRAWER_TO_DOCK_CREATE_INSERT_ICON :
				FeatureItemInfo appDrawerIconInfo = (FeatureItemInfo) DragInfoTranslater
						.createItemInfoForDock(dragView.getDragViewType(), dragInfo);
				appFunDragHandle(appDrawerIconInfo, count, rowId, toIndexInRow);
				ret = true;
				break;
			case APPDRAWER_TO_DOCK_CREATE_INSERT_FOLDER :
				UserFolderInfo appDrawerFolderInfo = (UserFolderInfo) DragInfoTranslater
						.createItemInfoForDock(dragView.getDragViewType(), dragInfo);
				appFunFolderDragHandle(appDrawerFolderInfo, count, rowId, toIndexInRow);
				ret = true;
				break;
			case FOLDER_TO_DOCK_CREATE_INSERT_ICON :
				ShortCutInfo folerIconInfoFromOthers = (ShortCutInfo) DragInfoTranslater
						.createItemInfoForDock(dragView.getDragViewType(), dragInfo);
				folderDragHandle(folerIconInfoFromOthers, count, rowId, toIndexInRow);
				ret = true;
				break;
			case FOLDER_FROM_DOCK_TO_DOCK_CREATE_INSERT_ICON :
				ShortCutInfo folerIconInfoFromDock = (ShortCutInfo) DragInfoTranslater
						.createItemInfoForDock(dragView.getDragViewType(), dragInfo);
				folderItemDragHandle(folerIconInfoFromDock, count, rowId, toIndexInRow);
				mDockLineLayout.deleteFolderItem(null, null, folerIconInfoFromDock, rowId);
				ret = true;
				break;
			default :
				break;
		}
		return ret;

	}
	/***
	 * 屏幕层的图标（包括文件夹）拖动处理
	 * 
	 * @param tag
	 * @param oldcount
	 * @param rowId
	 * @param toIndexInRow
	 */
	private void screenItemDragHandle(FeatureItemInfo tag, int oldcount, int rowId, int toIndexInRow) {
		// 插入
		DockItemInfo dockItemInfo = new DockItemInfo(tag.mItemType,
				DockUtil.getIconSize(oldcount + 1));
		dockItemInfo.setInfo(tag);
		dockItemInfo.setmRowId(rowId);
		dockItemInfo.setmIndexInRow(toIndexInRow);
		mDockLineLayout.refShortcutItem(dockItemInfo, oldcount, rowId, false, null, false, false);
	}

	/***
	 * 功能表文件夹里面的图标拖动处理
	 * 
	 * @param oldcount
	 * @param rowId
	 * @param toIndexInRow
	 */
	private void appFunFolderDragHandle(UserFolderInfo tag, int oldcount, int rowId,
			int toIndexInRow) {
		// 插入
		DockItemInfo dockItemInfo = new DockItemInfo(tag.mItemType,
				DockUtil.getIconSize(oldcount + 1));
		tag.mInScreenId = System.currentTimeMillis();
		dockItemInfo.setInfo(tag);
		dockItemInfo.setmRowId(rowId);
		dockItemInfo.setmIndexInRow(toIndexInRow);
		mDockLineLayout.refShortcutItem(dockItemInfo, oldcount, rowId, true, tag, false, false);
	}

	private void appFunDragHandle(FeatureItemInfo tag, int oldcount, int rowId, int toIndexInRow) {
		DockItemInfo dockItemInfo = new DockItemInfo(tag.mItemType,
				DockUtil.getIconSize(oldcount + 1));
		dockItemInfo.setInfo(tag);
		dockItemInfo.setmRowId(rowId);
		dockItemInfo.setmIndexInRow(toIndexInRow);
		mDockLineLayout.refShortcutItem(dockItemInfo, oldcount, rowId, false, null, false, false);
	}

	/***
	 * 功能表的图标和屏幕层文件夹里面的图标拖动处理
	 * 
	 * @param oldcount
	 * @param rowId
	 * @param toIndexInRow
	 */
	private void folderDragHandle(ShortCutInfo info, int oldcount, int rowId, int toIndexInRow) {
		ShortCutInfo tag = info;
		DockItemInfo dockItemInfo = new DockItemInfo(IItemType.ITEM_TYPE_SHORTCUT,
				DockUtil.getIconSize(oldcount + 1));
		dockItemInfo.setInfo(tag);
		dockItemInfo.setmRowId(rowId);
		dockItemInfo.setmIndexInRow(toIndexInRow);
		mDockLineLayout.refShortcutItem(dockItemInfo, oldcount, rowId, false, null, false, false);
	}

	/***
	 * DOCK条文件夹里面的图标拖动处理
	 * 
	 * @param oldcount
	 * @param rowId
	 * @param toIndexInRow
	 */
	private void folderItemDragHandle(ShortCutInfo info, int oldcount, int rowId, int toIndexInRow) {
		// 插入
		ShortCutInfo tag = info;
		DockItemInfo dockItemInfo = new DockItemInfo(IItemType.ITEM_TYPE_SHORTCUT,
				DockUtil.getIconSize(oldcount + 1));
		dockItemInfo.setInfo(tag);
		// 索引
		dockItemInfo.setmRowId(rowId);
		dockItemInfo.setmIndexInRow(toIndexInRow);
		mDockLineLayout.refShortcutItem(dockItemInfo, oldcount, rowId, false, null, false, false);
	}
	// 复位动画
	protected void resetAnimationForOutFrame(DragView dragView, int index,
			DropAnimationInfo resetInfo, int count) {

		if (!caculateCenterXY(index, count)) {
			return;
		}

		if (dragView != null) {
			GLView originalView = dragView.getOriginalView();
			if (!(originalView instanceof GLDockIconView) && originalView instanceof IconView
					&& ((IconView) originalView).isEnableAppName()) {
				float[] iconCenterPoint = IconUtils.getIconCenterPoint(mCenterX, mCenterY,
						GLDockIconView.class);
				resetInfo.setLocationPoint(iconCenterPoint[0], iconCenterPoint[1]);
				resetInfo.setLocationType(DropAnimationInfo.LOCATION_ICON_CENTER);
			} else {
				resetInfo.setLocationPoint(mCenterX, mCenterY);
				resetInfo.setLocationType(DropAnimationInfo.LOCATION_CENTER);
			}
		} else {
			resetInfo.setLocationPoint(mCenterX, mCenterY);
			resetInfo.setLocationType(DropAnimationInfo.LOCATION_CENTER);
		}
		resetInfo.setDuration(DragAnimation.DURATION_200);
		resetInfo.setAnimationListener(new AnimationListenerHandler(
				AnimationListenerHandler.HOMING_ANIMATION_TO_OUTSIDE));
		//		resetInfo.setAnimationListener(new AnimationListener() {
		//
		//			@Override
		//			public void onAnimationStart(Animation animation) {
		//
		//			}
		//
		//			@Override
		//			public void onAnimationRepeat(Animation animation) {
		//
		//			}
		//
		//			@Override
		//			public void onAnimationEnd(Animation animation) {
		//				// TODO ????
		//				mDockLineLayout.updateIndexAfterExtrusion();
		//				onDropAnimationFinish();
		//				mDockLineLayout.post(new Runnable() {
		//
		//					@Override
		//					public void run() {
		//						if (GLAppFolder.getInstance().isFolderOpened()) {
		//							MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_FOLDER, this,
		//									IScreenFrameMsgId.FOLDER_RELAYOUT, -1);
		//						}
		//					}
		//				});
		//
		//			}
		//		});

	}
	private void resetAnimationForFolder(final int dragIndex, DropAnimationInfo resetInfo,
			boolean isInDock, int count) {
		if (!isInDock) {
			backAnimation();
		}

		if (!caculateCenterXY(dragIndex, count)) {
			return;
		}

		resetInfo.setLocationPoint(mCenterX, mCenterY);
		resetInfo.setDuration(DragAnimation.DURATION_200);
		resetInfo.setLocationType(DropAnimationInfo.LOCATION_CENTER);
		resetInfo.setAnimationListener(new AnimationListenerHandler(
				AnimationListenerHandler.HOMING_ANIMATION_TO_FOLDER));
		//		resetInfo.setAnimationListener(new AnimationListener() {
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
		//
		//			@Override
		//			public void onAnimationEnd(Animation animation) {
		//				// TODO Auto-generated method stub
		//				//				mHandler.sendEmptyMessage(FOLDER_DELETE_ITEM);
		//				onDropAnimationFinish();
		//			}
		//		});

	}

	public void onFolderDropComplete(Object sender, Object target, boolean success) {
		if (success) {
			if (!(target instanceof GLDock) && !(target instanceof GLDockFolderGridVIew)) {
				mDockLineLayout.deleteFolderItem(sender, null, mDragInfo, mDockLineLayout.getLineID());
				mDockLineLayout.handleDeletedFolder();
				onDropFinish();
			}
		} 
//		mDockLineLayout.post(new Runnable() {
//
//			@Override
//			public void run() {
				if (GLAppFolder.getInstance().isFolderOpened() && !GLAppFolder.getInstance().isFolderClosing()) {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_FOLDER,
							IFolderMsgId.FOLDER_KEEP_OPEN, -1, true);
				}
//
//			}
//		});
	}
	/**
	 * 
	 * @author dingzijian
	 *
	 */
	protected class AnimationListenerHandler
			implements
				AnimationListener,
				FolderCoverAnimationListener,
				BatchAnimationObserver {
		private int mAnimationType = -1;
		public static final int HOMING_ANIMATION_TO_FOLDER = 1000;
		public static final int HOMING_ANIMATION_TO_OUTSIDE = 1001;
		public static final int HOMING_ANIMATION_TO_DOCK = 1002;
		public static final int HOMING_ANIMATION_FROM_OUTSIDE_TO_DOCK = 1003;
		public static final int ADD_IN_FOLDER_ANIMATION = 1004;
		public static final int ADD_IN_FOLDER_COVER_ANIMATION = 1005;
		public final static int EXTRUSION_ANIMATION = 1006;
		public final static int HOMING_ANIMATION = 1007;
		public final static int CREATE_FOLDER_COVER_ANIMATION = 1008;
		
		private boolean mShowQuickMenu;
		public AnimationListenerHandler(int animationType, Object... parm) {
			mAnimationType = animationType;
		}
		@Override
		public void onAnimationEnd(Animation arg0) {
			switch (mAnimationType) {
				case HOMING_ANIMATION_TO_FOLDER :
					onDropFinish();
					break;
				case HOMING_ANIMATION_TO_OUTSIDE :
					mDockLineLayout.updateIndexAfterExtrusion();
					mDockLineLayout.handleDeletedFolder();
					onDropFinish();
					mDockLineLayout.post(new Runnable() {
						@Override
						public void run() {
							if (GLAppFolder.getInstance().isFolderOpened()) {
								MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_FOLDER,
										IFolderMsgId.FOLDER_RELAYOUT, -1);
							}
						}
					});
					break;
				case HOMING_ANIMATION_TO_DOCK :
					if (!mShowQuickMenu) {
						mDockLineLayout.updateIndexAfterExtrusion();
						mDockLineLayout.updateIconsSizeAndRequestLayout();
						mShowQuickMenu = false;
					}
					break;
				case HOMING_ANIMATION_FROM_OUTSIDE_TO_DOCK :
					mDockLineLayout.updateIndexAfterExtrusion();
					mDockLineLayout.updateIconsSizeAndRequestLayout();
					break;
//				case ADD_IN_FOLDER_ANIMATION :
//					mDockLineLayout.setIsFolderAnimationBegin(false);
//					mDockLineLayout.postDelayed(new Runnable() {
//						@Override
//						public void run() {
//							GLAppFolder.getInstance().batchStartIconEditEndAnimation();
//						}
//
//					}, 50);
//					if (mGLDockFolderIcon.getFolderChildCount() > 4) {
//						mDockLineLayout.postDelayed(new Runnable() {
//							@Override
//							public void run() {
//								mGLDockFolderIcon.refreshForAddIcon(BaseFolderIcon.MAX_ICON_COUNT);
//							}
//						}, 1000);
//					}
//					break;
//				case CREATE_FOLDER_ANIMATION:
//					// 解决当从DOCK文件夹拖动图标到DOCK上合并文件夹后进行图标删除，无法获取当前VIEW的问题
//					GLDockLineLayoutContainer dockLineLayoutContainer = (GLDockLineLayoutContainer) mDockLineLayout
//							.getGLParent();
//					dockLineLayoutContainer.setCurrentIcon(mGLDockFolderIcon);
//					mGLDockFolderIcon.post(new Runnable() {
//						@Override
//						public void run() {
//							mGLDockFolderIcon.openFolder();
//						}
//					});
//					break;
				default :
					break;
			}

		}

		@Override
		public void onAnimationRepeat(Animation arg0) {

		}

		@Override
		public void onAnimationStart(Animation arg0) {
			switch (mAnimationType) {
				case HOMING_ANIMATION_TO_DOCK :
					if (mShowQuickMenu = mActionMenuHandler.needShowActionMenu(mX, mY, mDragView)) {
						mResetInfo.setNeedToShowCircle(false);
						DockQuickActionMenuBusiness actionMenuBusiness = new DockQuickActionMenuBusiness();
						actionMenuBusiness.showQuickActionMenu(mDragView.getOriginalView());
					}
					break;

				default :
					break;
			}

		}
		
		@Override
		public void onAnimationProcessing(Animation animation, float interpolatedTime) {
			
		}
		
		@Override
		public void onStart(int what, Object[] params) {

		}
		@Override
		public void onFinish(int what, Object[] params) {
			switch (what) {
				case EXTRUSION_ANIMATION :
					switch (mMergeFolderTYpe) {
						case ICON_TO_ICON_MERGE_FOLDER :
							updateIndexAfterFolderAnimate();
							mGLDockFolderIcon.post(new Runnable() {
								@Override
								public void run() {
									mGLDockFolderIcon.openFolder();
									mGLDockFolderIcon.setFolderCoverAnimationListner(null);
								}
							});
							break;
						case ICON_TO_FOLDER_MERGE_FOLDER :
							updateIndexAfterFolderAnimate();
							handleFolderIconThumail();
							mGLDockFolderIcon.setFolderCoverAnimationListner(null);
							break;
						default :
							break;
					}
					AnimationTask task = (AnimationTask) params[0];
					task.cleanup();
					break;
				default :
					break;
			}
		}
		private void updateIndexAfterFolderAnimate() {
			mDockLineLayout.updateIndexAfterFolderAnimate(mTempView);
			mDockLineLayout.handleDeletedFolder();
			onDropFinish();
		}
		private void handleFolderIconThumail() {
//			mDockLineLayout.postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					GLAppFolder.getInstance().batchStartIconEditEndAnimation();
//				}
//
//			}, 50);
			if (mGLDockFolderIcon.getFolderChildCount() > 4) {
				mDockLineLayout.postDelayed(new Runnable() {
					@Override
					public void run() {
						mGLDockFolderIcon.refreshForAddIcon(FolderConstant.MAX_ICON_COUNT);
					}
				}, 1000);
			}
		}
		@Override
		public void onFolderCoverAnimationEnd(boolean isOpened, GLView icon) {
			if (isOpened) {
				return;
			}
			mGLDockFolderIcon.setFolderCoverAnimationListner(null);
			mDockLineLayout.setIsFolderAnimationBegin(false);
			mDockLineLayout.post(new Runnable() {
				@Override
				public void run() {
					if (mNeedExtrusionAnimate) {
						doInsideExtrusion();
					} else {
						mDockLineLayout.handleDeletedFolder();
						onDropFinish();
						switch (mAnimationType) {
							case CREATE_FOLDER_COVER_ANIMATION :
								mGLDockFolderIcon.post(new Runnable() {
									@Override
									public void run() {
										mGLDockFolderIcon.openFolder();

									}
								});
								break;
							case ADD_IN_FOLDER_COVER_ANIMATION :
								handleFolderIconThumail();
								break;
							default :
								break;
						}
					}
					
				}
			});
		}
	}
	protected void onDropFinish() {
		mDockLineLayout.post(new Runnable() {
			@Override
			public void run() {
				if (mTempView != null) {
					mDockLineLayout.removeViewInLayout(mTempView);
					mTempView.clearAnimation();
					mTempView.cleanup();
					mTempView = null;
				}
				mDockLineLayout.updateIconsSizeAndRequestLayout();
				mDockLineLayout.setIsFolderAnimationBegin(false);
				mDockLineLayout.hideProtectLayer();
				if (GLAppFolder.getInstance().isFolderOpened()) {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_FOLDER,
							IFolderMsgId.FOLDER_RELAYOUT, -1);
				}
			}
		});

	}
}
