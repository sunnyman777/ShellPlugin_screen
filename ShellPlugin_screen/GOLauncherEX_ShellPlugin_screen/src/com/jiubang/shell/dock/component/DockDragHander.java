package com.jiubang.shell.dock.component;

import java.util.ArrayList;

import android.graphics.Rect;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.view.GLView;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockLogicControler;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.FeatureItemInfo;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.launcher.IconUtilities;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderController;
import com.jiubang.shell.appdrawer.allapp.GLAllAppGridView;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.component.TransformationInfo;
import com.jiubang.shell.common.listener.TransformListener;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.dock.GLDock;
import com.jiubang.shell.dock.utils.GLDockViewUtil;
import com.jiubang.shell.drag.DragInfoTranslater;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.folder.GLAppFolder;
import com.jiubang.shell.folder.GLDockFolderGridVIew;
import com.jiubang.shell.folder.GLDockFolderIcon;
import com.jiubang.shell.screen.GLCellLayout;
import com.jiubang.shell.screen.component.GLScreenShortCutIcon;
import com.jiubang.shell.utils.IconUtils;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

/**
 * dock托拽动画处理类
 * 
 * @author zhujian
 * 
 */
public class DockDragHander implements BatchAnimationObserver {

	//	protected ConcurrentHashMap<Integer, ArrayList<IconView<?>>> mIconViewsHashMap; // 装载三行dock条的hashMap

	//	protected ArrayList<IconView<?>> mCurDockViewList; // 每条LineLayout所包含的DockIconView

	protected ArrayList<IconView<?>> mAniCurDockViewList; // 动画集合

	//	protected GLDockLineLayoutContainer mGLDockLineLayoutContainer; // 3条LineLayout的父容器

	protected GLDockLineLayout mOperaterLayout; // 当前操作的linelayout

	private boolean mIsExtrusionAnimation = false; // 动画是否做完

	public final static int EXTRUSION_ANIMATION_DURATION = 100;

	private int mCount;

	private int mChildCount;

	private ArrayList<Position> mInitRectList; // dock初始化区域集合

	private final static int DRAG_OVER_INVALIDATE = -1; // 拖拽结果无效
	private final static int DRAG_OVER_NEW_FOLDER = -2; // 拖拽过程中生成文件夹

	/**
	 * 拖拽结果常量
	 */
	public final static int DRAG_RESULT_FULL = -1; // 由于dock栏已满导致的，拖拽没成功
	public final static int DRAG_RESULT_NONE = 0; // 拖拽结果没影响
	public final static int DRAG_RESULT_INSERT = 1; // 拖拽插入新项
	public final static int DRAG_RESULT_NEW_FLODER = 2; // 拖拽生成一个新文件夹
	public final static int DRAG_RESULT_ADD_IN_FLODER = 3; // 拖拽到一个文件夹内
	public final static int DRAG_RESULT_EXCHANGE_POSITION = 4; // 与屏幕层图标换位

	public int mDragFolderIndex; // 拖拽生成文件夹时，文件夹的下标
	public int mDragResult = DRAG_RESULT_NONE; // 拖拽结果
	public int mDragIndex = -1;
	public int mDragInitIndex = -1; // 记录第一次抓起的位置
	public int mDragResultAfterHideFolder = DRAG_RESULT_NONE;
	
	private float mZoomInProportion;
	private float mZoomOutProportion;
	private IconView<?> mMergeFolderIconView = null; // 重合后将被合成的为文件夹的view

	public boolean mIsShowFolderAnimation = false; // 是否执行了文件夹合成准备动画

	private boolean mInsert = false;

	//	private OnDockDragHanderListener mListener;

	/**BEGIN 挤压移动到桌面动画逻辑*/
	protected IconView<?> mLastMoveToScreenView; //上一个挤压视图
	private int[] mCellXY = null; //在屏幕层当前屏上安放的网格
	private DragView mDragView = null;
	private int mExchangeIndex = -1; //dock栏被交换位置view的下标
	private AnimationSet mAnimationSet = null;
	private final int[] mTmpPoint = new int[2];
	private ValueAnimator mShakeAnimator = null;
	public final static int MAX_DOCK_TO_SCREEN_ANIMATION = 12;
	public final static int ICON_EXTRUSION_ANIMATION = 13;
	/**BEGIN 挤压移动到桌面动画逻辑*/

	public DockDragHander() {

		//		mIconViewsHashMap = new ConcurrentHashMap<Integer, ArrayList<IconView<?>>>();
		//		mCurDockViewList = new ArrayList<IconView<?>>();
		mAniCurDockViewList = new ArrayList<IconView<?>>();
		initZoomProportion();
	}

	//	public void setOperationListener(OnDockDragHanderListener listener) {
	//		mListener = listener;
	//	}

	//	public void setIconViewsHashMap(ConcurrentHashMap<Integer, ArrayList<IconView<?>>> infoHashMap) {
	//		mIconViewsHashMap = infoHashMap;
	//	}

	public boolean isExtrusionAnimation() {
		return mIsExtrusionAnimation;
	}

	public void setExtrusionAnimation(boolean extrusionAnimation) {
		this.mIsExtrusionAnimation = extrusionAnimation;
	}

	/**
	 * 初始化大小图标缩放比例
	 */
	public void initZoomProportion() {
		float smallIconSize = DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW);
		float bigIconSize = DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW - 1);
		mZoomInProportion = bigIconSize / smallIconSize;
		mZoomOutProportion = smallIconSize / bigIconSize;
	}

	/**
	 * 设置当前动画的viewGroup
	 * 
	 * @param viewGroup
	 */
	public void setOperaterLayout(GLView viewGroup) {
		if (viewGroup == null) {
			return;
		}
		//		mGLDockLineLayoutContainer = (GLDockLineLayoutContainer) viewGroup;

		//		mCurDockViewList = mIconViewsHashMap.get(mGLDockLineLayoutContainer.getCurLine());

		mOperaterLayout = (GLDockLineLayout) viewGroup;

		if (mOperaterLayout == null) {
			return;
		}
		mCount = mChildCount = mOperaterLayout.getChildCount();
		mInitRectList = new ArrayList<Position>();
		mInitRectList.clear();
		int count = mOperaterLayout.getChildCount();
		for (int i = 0; i < count; i++) {
			Position position = new Position();
			IconView<?> iconView = (IconView<?>) mOperaterLayout.getChildAt(i);
			position.outRect = iconView.getOperationArea(null, mChildCount)[0];
			position.innerRect = iconView.getOperationArea(null, mChildCount)[1];
			mInitRectList.add(position);
		}

	}

	public void doDragEnter(DragSource source, int dragIndex, int x, int y, DragView dragView,
			Object dragInfo) {
		if (!(source instanceof GLDock)) {
			setIsInsert(true);
		}
	}

	public void doDragExit(DragSource source, int dragIndex) {
		if (source instanceof GLDock) { // 拖拽源是DOCK栏
			if (mCount < mChildCount) { //
				return;
			}
			mCount--;
			onDragExitRelayoutView(mCount, mCount + 1, dragIndex, true);
			startExtrusion(mAniCurDockViewList, DockUtil.ANIMATION_ZOOM_NORMAL_TO_BIG, source,
					false);
		} else {
			if (mInsert || mChildCount == DockUtil.ICON_COUNT_IN_A_ROW) {
				setIsInsert(false);
				return;
			} else {
				mCount--;
				onDragExitRelayoutView(mCount, mCount, dragIndex, true);
				startExtrusion(mAniCurDockViewList, DockUtil.ANIMATION_ZOOM_NORMAL_TO_BIG, source,
						true);
			}

		}

	}

	/**
	 * 图标拖出Dock栏时，重新计算子View的排版
	 * 
	 * @param newCount
	 *            计算排版的图标数
	 * @param childCount
	 *            子View个数
	 * @param dragIndex
	 *            拖拽的下标位置
	 * @param hasDragView
	 *            拖拽源是否是DOCK
	 */
	private void onDragExitRelayoutView(int newCount, int childCount, int dragIndex,
			boolean hasDragView) {
		if (childCount == 0 || childCount != mOperaterLayout.getChildCount()) {

			return;
		}
		mOperaterLayout.setInitRect(newCount);
		GLView dragView = null;
		if (hasDragView) {
			dragView = mOperaterLayout.findViewByIndex(dragIndex);
		}
		ArrayList<Position> mChangeRectList = mOperaterLayout.getInitRectList();
		mAniCurDockViewList.clear();
		for (int i = 0; i < newCount; i++) {
			int viewIndex = i;
			if (i >= dragIndex) {
				viewIndex = i + 1;
			}
			IconView view = mOperaterLayout.findViewByIndex(viewIndex);
			if (view != null) {
				ViewPositionTag tag = (ViewPositionTag) view.getTag(R.integer.dock_view_left);
				Position position = mChangeRectList.get(i);
				if (GoLauncherActivityProxy.isPortait()) {
					tag.tempLayoutX = position.outRect.left
							+ (position.outRect.right - position.outRect.left) / 2;
				} else {
					tag.tempLayoutX = position.outRect.top
							+ (position.outRect.bottom - position.outRect.top) / 2;
				}
				mAniCurDockViewList.add(view);
				if (viewIndex > dragIndex) {
					view.setTag(R.integer.dock_index, viewIndex - 1);
				}
			}
		}
		if (dragView != null) {
			dragView.setTag(R.integer.dock_index, -1);
			mDragIndex = -1;
		}
	}

	public boolean doDragOver(DragSource source, int dragIndex, int x, int y, int xOffset,
			int yOffset, DragView sourceView, ItemInfo dragInfo) {

		if (mOperaterLayout == null) {

			return true;
		}
		// 计算Icon中心位置
		//		x = x - xOffset;
		//		y = y - yOffset;
		if (sourceView.getOriginalView() instanceof IconView<?>) {
			float[] p = sourceView.getIconCenterPoint();
			x = (int) p[0];
			y = (int) p[1];
		} else {
			x = x - xOffset;
			y = y - yOffset;
		}
		ArrayList<Position> list = mOperaterLayout.getInitRectList();
		if (list == null) {
			return false;
		}
		int count = list.size();
		boolean result = false;
		if (source instanceof GLDock) { // Dock内部的拖拽
			if (mCount < mChildCount) { // 处理图标插入，图标从Dock拖出又拖进
				dragInsert(list, count, x, y, true, sourceView, dragInfo, source);
			} else { // 图标在DOCK内的移动
				result = insideDragOver(list, dragIndex, x, y, sourceView, dragInfo, source);
			}

		} else {

			if (mChildCount == DockUtil.ICON_COUNT_IN_A_ROW) { // 图标个数已经达到最大值

				result = maxCountDragOver(source, list, dragIndex, x, y, sourceView, dragInfo);

			} else if (mInsert) { // 处理图标插入

				dragInsert(list, count, x, y, false, sourceView, dragInfo, source);

			} else { // 图标在DOCK内的移动

				result = insideDragOver(list, dragIndex, x, y, sourceView, dragInfo, source);
			}

		}
		return result;
	}

	private int mLastIndex = -1;

	private boolean maxCountDragOver(DragSource source, ArrayList<Position> list, int dragIndex,
			int x, int y, DragView sourceView, ItemInfo dragInfo) {

		boolean result = false;

		int count = list.size();
		if (count == 0) {
			return false;
		}

		for (int i = 0; i < count; i++) {
			Rect outRect = list.get(i).outRect;
			Rect innerRect = list.get(i).innerRect;

			if (GoLauncherActivityProxy.isPortait()) {
				int perIconW = mOperaterLayout.getWidth() / 5;
				int deltaW = (perIconW - outRect.width()) / 2;
				if (outRect.left - deltaW < x && outRect.right + deltaW > x) {
					if (innerRect.contains(x, y)
							|| (innerRect.left < x && innerRect.right > x && innerRect.top < y)) {

						exchangeIconAnimation(null, null, null, null, -1, true);

						if (sourceView.getDragViewType() == DragView.DRAGVIEW_TYPE_APPDRAWER_FOLDER
								|| sourceView.getDragViewType() == DragView.DRAGVIEW_TYPE_SCREEN_USERFOLDER) { // 如果抓起来的是文件夹就不能合成
							return false;
						}

						// 寻找被合成的view
						IconView view = mOperaterLayout.findViewByIndex(i);
						// 文件夹
						if (view instanceof GLDockFolderIcon) { // 已经是文件夹的进行添加操作
							if (!mIsShowFolderAnimation) {
								if (mMergeFolderIconView != view) {
									mMergeFolderIconView = view;
									mDragFolderIndex = i;
									showFolder(DRAG_RESULT_ADD_IN_FLODER);
									mIsShowFolderAnimation = true;
									result = true;
									break;
								}
							}
						} else if (view instanceof GLDockIconView) { // 合成文件夹
							if (!mIsShowFolderAnimation) {
								if (mMergeFolderIconView != view) {
									mMergeFolderIconView = view;
									mDragFolderIndex = i;
									showFolder(DRAG_RESULT_NEW_FLODER);
									mIsShowFolderAnimation = true;
									result = true;
									break;
								}
							}
						}
					} else {
						hideFolder();
						if ((x > outRect.left - deltaW && x < innerRect.left)
								|| (x < outRect.right + deltaW && x > innerRect.right)) {

							// 寻找被挤压的view
							if (mLastIndex != i) {
								exchangeIconAnimation(null, null, null, null, -1, true);
							}
							IconView view = mOperaterLayout.findViewByIndex(i);
							exchangeIconAnimation(source, view, sourceView, dragInfo, i, false);
							mDragResult = DRAG_RESULT_EXCHANGE_POSITION;
							mLastIndex = i;
							result = true;
							break;
						}
					}
				}
			} else {
				int perIconH = mOperaterLayout.getHeight() / 5;
				int deltaH = (perIconH - outRect.width()) / 2;

				if (deltaH + outRect.top < y && deltaH + outRect.bottom > y) {
					if (innerRect.contains(x, y)
							|| (innerRect.left < x && innerRect.top < y && innerRect.bottom > y)) {
						exchangeIconAnimation(null, null, null, null, -1, true);
						if (sourceView.getDragViewType() == DragView.DRAGVIEW_TYPE_APPDRAWER_FOLDER
								|| sourceView.getDragViewType() == DragView.DRAGVIEW_TYPE_SCREEN_USERFOLDER) { // 如果抓起来的是文件夹就不能合成
							return false;
						}
						// 寻找被合成的view
						IconView view = mOperaterLayout.findViewByIndex(i);
						// 文件夹
						if (view instanceof GLDockFolderIcon) { // 已经是文件夹的进行添加操作
							if (!mIsShowFolderAnimation) {
								if (mMergeFolderIconView != view) {
									mMergeFolderIconView = view;
									mDragFolderIndex = i;
									showFolder(DRAG_RESULT_ADD_IN_FLODER);
									mIsShowFolderAnimation = true;
									result = true;
									break;
								}
							}
						} else if (view instanceof GLDockIconView) { // 合成文件夹
							if (!mIsShowFolderAnimation) {
								if (mMergeFolderIconView != view) {
									mMergeFolderIconView = view;
									mDragFolderIndex = i;
									showFolder(DRAG_RESULT_NEW_FLODER);
									mIsShowFolderAnimation = true;
									result = true;
									break;
								}
							}
						}
					} else {
						hideFolder();
						if ((y > outRect.top - deltaH && x < innerRect.top)
								|| (y < outRect.bottom + deltaH && y > innerRect.bottom)) {
							// 寻找被挤压的view
							if (mLastIndex != i) {
								exchangeIconAnimation(null, null, null, null, -1, true);
							}
							IconView view = mOperaterLayout.findViewByIndex(i);
							exchangeIconAnimation(source, view, sourceView, dragInfo, i, false);
							mDragResult = DRAG_RESULT_EXCHANGE_POSITION;
							mLastIndex = i;
							result = true;
							break;
						}
					}
				}
			}
		}
		return result;

	}

	/**
	 * dock栏处理插入一项的情况
	 * 
	 * @param list
	 *            图标的位置信息
	 * @param count
	 * @param x
	 * @param y
	 * @param hasDragView
	 *            拖拽源是否是DOCK
	 */
	private void dragInsert(ArrayList<Position> list, int count, int x, int y, boolean hasDragView,
			DragView sourceView, ItemInfo dragInfo, DragSource source) {

		int insertIndex = findInsertIndex(list, count, x, y);

		if (insertIndex == DRAG_OVER_NEW_FOLDER) {

			if (GLAppFolder.getInstance().isFolderOpened()) {
				return;
			}

			if (sourceView.getDragViewType() == DragView.DRAGVIEW_TYPE_DOCK_FOLDER
					|| sourceView.getDragViewType() == DragView.DRAGVIEW_TYPE_APPDRAWER_FOLDER
					|| sourceView.getDragViewType() == DragView.DRAGVIEW_TYPE_SCREEN_USERFOLDER) { // 如果抓起来的是文件夹就不能合成
				return;
			}

			// 生成文件夹
			// 寻找被合成的view
			IconView view = mOperaterLayout.findViewByIndex(mDragFolderIndex);

			// 文件夹

			if (view instanceof GLDockFolderIcon) { //
				// 已经是文件夹的进行添加操作
				if (!mIsShowFolderAnimation) {
					if (mMergeFolderIconView != view) {
						mMergeFolderIconView = view;
						showFolder(DRAG_RESULT_ADD_IN_FLODER);
						mIsShowFolderAnimation = true;
					}

				}
			} else if (view instanceof GLDockIconView) { // 合成文件夹
				if (mMergeFolderIconView != view) {
					if (!mIsShowFolderAnimation) {
						mMergeFolderIconView = view;
						showFolder(DRAG_RESULT_NEW_FLODER);
						mIsShowFolderAnimation = true;
					}
				}
			}
			return;
		} else {
			hideFolder();
			if (insertIndex != DRAG_OVER_INVALIDATE) {
				mDragIndex = insertIndex;
				mCount++;
				mOperaterLayout.setInitRect(mCount);
				ArrayList<Position> changeRectList = mOperaterLayout.getInitRectList();
				GLView dragView = null;
				if (hasDragView) {
					dragView = mOperaterLayout.findViewByIndex(-1);
				}
				mAniCurDockViewList.clear();
				for (int i = count - 1; i >= 0; i--) {
					IconView view = mOperaterLayout.findViewByIndex(i);
					if (view != null) {
						ViewPositionTag tag = (ViewPositionTag) view
								.getTag(R.integer.dock_view_left);
						if (i < insertIndex) {
							if (GoLauncherActivityProxy.isPortait()) {
								tag.tempLayoutX = changeRectList.get(i).outRect.left
										+ (changeRectList.get(i).outRect.right - changeRectList
												.get(i).outRect.left) / 2;
							} else {
								tag.tempLayoutX = changeRectList.get(i).outRect.top
										+ (changeRectList.get(i).outRect.bottom - changeRectList
												.get(i).outRect.top) / 2;
							}
						}
						if (i >= insertIndex) {
							if (GoLauncherActivityProxy.isPortait()) {
								tag.tempLayoutX = changeRectList.get(i + 1).outRect.left
										+ (changeRectList.get(i + 1).outRect.right - changeRectList
												.get(i + 1).outRect.left) / 2;
							} else {
								tag.tempLayoutX = changeRectList.get(i + 1).outRect.top
										+ (changeRectList.get(i + 1).outRect.bottom - changeRectList
												.get(i + 1).outRect.top) / 2;
							}
							view.setTag(R.integer.dock_index, i + 1);
						}
						mAniCurDockViewList.add(view);
					}
				}

				if (dragView != null) {
					dragView.setTag(R.integer.dock_index, insertIndex);
				}

				startExtrusion(mAniCurDockViewList, DockUtil.ANIMATION_ZOOM_BIG_TO_SMALL, source,
						false);
				mDragIndex = insertIndex;
				mDragResult = DRAG_RESULT_INSERT;
				setIsInsert(false);
			} else {
				mDragResult = DRAG_RESULT_NONE;
			}
		}

	}

	/**
	 * 查找Dock栏图标添加一项时插入的下标
	 * 
	 * @param list
	 * @param count
	 * @param x
	 * @param y
	 * @return
	 */
	public int findInsertIndex(ArrayList<Position> list, int count, int x, int y) {
		if (count == 0 || mChildCount == 0) {
			return 0;
		}

		int index = DRAG_OVER_INVALIDATE;
		mDragFolderIndex = -1;

		if (GoLauncherActivityProxy.isPortait()) {
			if (x < list.get(0).innerRect.left) {
				index = 0;
			} else if (list.get(0).innerRect.contains(x, y)
					|| (list.get(0).innerRect.left < x && list.get(0).innerRect.right > x && list
							.get(0).innerRect.top < y)) {
				// 文件夹
				index = DRAG_OVER_NEW_FOLDER;
				mDragFolderIndex = 0;
			} else if (x > list.get(count - 1).innerRect.right) {
				index = count;
			} else if (list.get(count - 1).innerRect.contains(x, y)
					|| (list.get(count - 1).innerRect.left < x
							&& list.get(count - 1).innerRect.right > x && list.get(count - 1).innerRect.top < y)) {
				// 文件夹
				index = DRAG_OVER_NEW_FOLDER;
				mDragFolderIndex = count - 1;
			} else if (count >= 2) {

				for (int i = 0; i < count - 1; i++) {
					if ((i != 0 && list.get(i).innerRect.contains(x, y))
							|| (list.get(i).innerRect.left < x && list.get(i).innerRect.right > x && list
									.get(i).innerRect.top < y)) {
						// 文件夹
						index = DRAG_OVER_NEW_FOLDER;
						mDragFolderIndex = i;
					} else if (x > list.get(i).innerRect.right
							&& x < list.get(i + 1).innerRect.left) {
						index = i + 1;
						break;
					}
				}
			}
		} else {
			if (y > list.get(0).innerRect.top) {
				index = 0;
			} else if (list.get(0).innerRect.contains(x, y)
					|| (list.get(0).innerRect.left < x && list.get(0).innerRect.top < y && list
							.get(0).innerRect.bottom > y)) {
				// 文件夹
				index = DRAG_OVER_NEW_FOLDER;
				mDragFolderIndex = 0;
			} else if (y < list.get(count - 1).innerRect.bottom) {
				index = count;
			} else if (list.get(count - 1).innerRect.contains(x, y)
					|| (list.get(count - 1).innerRect.left < x
							&& list.get(count - 1).innerRect.top < y && list.get(count - 1).innerRect.bottom > y)) {
				// 文件夹
				index = DRAG_OVER_NEW_FOLDER;
				mDragFolderIndex = count;
			} else if (count >= 2) {

				for (int i = 0; i < count - 1; i++) {
					if (i != 0
							&& list.get(i).innerRect.contains(x, y)
							|| (list.get(i).innerRect.left < x && list.get(i).innerRect.top < y && list
									.get(i).innerRect.bottom > y)) {
						// 文件夹
						index = DRAG_OVER_NEW_FOLDER;
						mDragFolderIndex = i;
					} else if (y > list.get(i + 1).innerRect.bottom
							&& y < list.get(i).innerRect.top) {
						index = i + 1;
						break;
					}
				}
			}

		}
		return index;
	}

	/**
	 * 图标在Dock内部的拖拽处理
	 * 
	 * @param list
	 *            图标排版的位置信息
	 * @param dragIndex
	 * @param x
	 * @param y
	 * @param dragInfo
	 * @return
	 */
	private boolean insideDragOver(ArrayList<Position> list, int dragIndex, int x, int y,
			DragView sourceView, ItemInfo dragInfo, DragSource source) {
		boolean result = false;
		int count = list.size();
		if (count == 0) {
			return false;
		}

		for (int i = 0; i < count; i++) {
			Rect outRect = list.get(i).outRect;
			Rect innerRect = list.get(i).innerRect;

			if (GoLauncherActivityProxy.isPortait()) {
				if (outRect.left < x && outRect.right > x) {
					if (innerRect.contains(x, y)
							|| (innerRect.left < x && innerRect.right > x && innerRect.top < y)) {
						if (dragIndex != i) {

							if (sourceView.getDragViewType() == DragView.DRAGVIEW_TYPE_DOCK_FOLDER
									|| sourceView.getDragViewType() == DragView.DRAGVIEW_TYPE_APPDRAWER_FOLDER
									|| sourceView.getDragViewType() == DragView.DRAGVIEW_TYPE_SCREEN_USERFOLDER) { // 如果抓起来的是文件夹就不能合成
								return false;
							}

							// 寻找被合成的view
							IconView view = mOperaterLayout.findViewByIndex(i);
							// 文件夹
							if (view instanceof GLDockFolderIcon) { // 已经是文件夹的进行添加操作
								if (!mIsShowFolderAnimation) {
									if (mMergeFolderIconView != view) {
										mMergeFolderIconView = view;
										mDragFolderIndex = i;
										showFolder(DRAG_RESULT_ADD_IN_FLODER);
										mIsShowFolderAnimation = true;
										result = true;
										break;
									}
								}
							} else if (view instanceof GLDockIconView) { // 合成文件夹
								if (!mIsShowFolderAnimation) {
									if (mMergeFolderIconView != view) {
										mMergeFolderIconView = view;
										mDragFolderIndex = i;
										showFolder(DRAG_RESULT_NEW_FLODER);
										mIsShowFolderAnimation = true;
										result = true;
										break;
									}
								}
							}
						} else {
							hideFolder();
//							mDragResult = DRAG_RESULT_INSERT;
						}
					} else {
						hideFolder();
						if ((i < dragIndex && x < innerRect.left)
								|| (i > dragIndex && x > innerRect.right)) {
							doInsideExtrusion(dragIndex, i, true, list, source);
							mDragResult = DRAG_RESULT_INSERT;
							result = true;
							break;
						}

					}
				}
			} else {
				if (outRect.top < y && outRect.bottom > y) {
					if (innerRect.contains(x, y)
							|| (innerRect.left < x && innerRect.top < y && innerRect.bottom > y)) {
						if (dragIndex != i) {

							if (sourceView.getDragViewType() == DragView.DRAGVIEW_TYPE_DOCK_FOLDER
									|| sourceView.getDragViewType() == DragView.DRAGVIEW_TYPE_APPDRAWER_FOLDER
									|| sourceView.getDragViewType() == DragView.DRAGVIEW_TYPE_SCREEN_USERFOLDER) { // 如果抓起来的是文件夹就不能合成
								return false;
							}

							// 寻找被合成的view
							IconView view = mOperaterLayout.findViewByIndex(i);
							// 文件夹
							if (view instanceof GLDockFolderIcon) { // 已经是文件夹的进行添加操作
								if (!mIsShowFolderAnimation) {
									if (mMergeFolderIconView != view) {
										mMergeFolderIconView = view;
										mDragFolderIndex = i;
										showFolder(DRAG_RESULT_ADD_IN_FLODER);
										mIsShowFolderAnimation = true;
										result = true;
										break;
									}
								}
							} else if (view instanceof GLDockIconView) { // 合成文件夹
								if (!mIsShowFolderAnimation) {
									if (mMergeFolderIconView != view) {
										mMergeFolderIconView = view;
										mDragFolderIndex = i;
										showFolder(DRAG_RESULT_NEW_FLODER);
										mIsShowFolderAnimation = true;
										result = true;
										break;
									}
								}
							}
						} else {
							hideFolder();
						}
					} else {
						hideFolder();

						if ((i < dragIndex && y > innerRect.bottom)
								|| (i > dragIndex && y < innerRect.top)) {
							doInsideExtrusion(dragIndex, i, true, list, source);
							mDragResult = DRAG_RESULT_INSERT;
							result = true;
							break;
						}
					}
				}
			}
		}
		return result;
	}
	/**
	 * 取消图标重叠时，合成文件夹的背景图
	 */
	public void hideFolder() {
		if (mIsShowFolderAnimation) {
			if (mMergeFolderIconView != null) {
				mMergeFolderIconView.cancleFolderReady();
				mIsShowFolderAnimation = false;
				mMergeFolderIconView = null;
				mDragResult = mDragResultAfterHideFolder;
			}
		}

	}

	private void showFolder(int type) {
		if (mMergeFolderIconView != null) {
			mDragResultAfterHideFolder = mDragResult;
			mMergeFolderIconView.readyForFolder(true);
			mDragResult = type;
			mIsShowFolderAnimation = true;
		}
	}

	/**
	 * 图标内部的挤压
	 * 
	 * @param dragIndex
	 *            起始下标
	 * @param desIndex
	 *            目标下标
	 * @param hasDragView
	 *            拖拽源是否是DOCK
	 */
	private void doInsideExtrusion(int dragIndex, int desIndex, boolean hasDragView,
			ArrayList<Position> list, DragSource source) {
		mAniCurDockViewList.clear();
		IconView<?> dragView = null;
		if (hasDragView && dragIndex != -1) {
			dragView = mOperaterLayout.findViewByIndex(dragIndex);
		}
		if (dragIndex == desIndex) {
			return;
		}
		if (dragIndex > desIndex) {
			for (int j = dragIndex - 1; j >= desIndex; j--) {
				doExtrusionAnimation(j, j + 1, list);
			}
			startExtrusion(mAniCurDockViewList, DockUtil.ANIMATION_ZOOM_NORMAL, source, false);
		} else {
			for (int j = dragIndex + 1; j <= desIndex; j++) {
				doExtrusionAnimation(j, j - 1, list);
			}
			startExtrusion(mAniCurDockViewList, DockUtil.ANIMATION_ZOOM_NORMAL, source, false);
		}
		if (dragView != null) {
			dragView.setTag(R.integer.dock_index, desIndex);
		}
		mDragIndex = desIndex;

	}

	private void doExtrusionAnimation(int srcIndex, int desIndex, ArrayList<Position> list) {
		if (desIndex < 0) {
			return;
		}
		final IconView<?> iconView = mOperaterLayout.findViewByIndex(srcIndex);
		if (iconView != null) {
			iconView.setTag(R.integer.dock_index, desIndex);
			ViewPositionTag tag = (ViewPositionTag) iconView.getTag(R.integer.dock_view_left);
			if (GoLauncherActivityProxy.isPortait()) {
				tag.tempLayoutX = list.get(desIndex).outRect.left
						+ (list.get(desIndex).outRect.right - list.get(desIndex).outRect.left) / 2;
			} else {
				tag.tempLayoutX = list.get(desIndex).outRect.top
						+ (list.get(desIndex).outRect.bottom - list.get(desIndex).outRect.top) / 2;
			}
			mAniCurDockViewList.add(iconView);
		}
	};

	int mAnimationCount = 0;

	// 开始挤压的动画
	private void startExtrusion(ArrayList<IconView<?>> list, int type, DragSource source,
			boolean clearAnimate) {
		if (list == null || list.size() < 1) {
			return;
		}
		hideFolder();
		mIsExtrusionAnimation = true;
		AnimationTask task = new AnimationTask(false, AnimationTask.PARALLEL);
		task.setBatchAnimationObserver(this, ICON_EXTRUSION_ANIMATION, clearAnimate);
		for (IconView<?> iconView : list) {
			Animation traAnimation = null;
			Animation animationScale = null;
			animationScale = getZoomAnimation(type, source);
			AnimationSet animationSet = new AnimationSet(true);
			if (animationScale != null) {
				animationSet.addAnimation(animationScale);
			}
			ViewPositionTag tag = (ViewPositionTag) iconView.getTag(R.integer.dock_view_left);
			int fromX = tag.newLayoutX - tag.oldLayoutX;
			int toX = tag.tempLayoutX - tag.oldLayoutX;
			tag.newLayoutX = tag.tempLayoutX;
			if (GoLauncherActivityProxy.isPortait()) {
				traAnimation = new TranslateAnimation(fromX, toX, 0, 0);
			} else {
				traAnimation = new TranslateAnimation(0, 0, fromX, toX);
			}
			animationSet.addAnimation(traAnimation);
			animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
			// animationSet.setFillEnabled(true);
			// animationSet.setFillBefore(true);
			animationSet.setFillAfter(true);
			animationSet.setDuration(EXTRUSION_ANIMATION_DURATION);
			task.addAnimation(iconView, animationSet, null);
		}
		GLAnimationManager.startAnimation(task);
		list.clear();
	}

	// 放大缩小动画判定
	/**
	 * float fromX 动画起始时 X坐标上的伸缩尺寸 float toX 动画结束时 X坐标上的伸缩尺寸 float fromY
	 * 动画起始时Y坐标上的伸缩尺寸 float toY 动画结束时Y坐标上的伸缩尺寸 int pivotXType 动画在X轴相对于物件位置类型
	 * float pivotXValue 动画相对于物件的X坐标的开始位置 int pivotYType 动画在Y轴相对于物件位置类型 float
	 * pivotYValue 动画相对于物件的Y坐标的开始位置
	 * 
	 * @param type
	 * @return
	 */
	public Animation getZoomAnimation(int type, DragSource source) {

		Animation animationScale = null;
		int iconCount = mOperaterLayout.getChildCount();
		long duration = 100; // 动画时间
		if (source instanceof GLDock) {
			if (type == DockUtil.ANIMATION_ZOOM_NORMAL_TO_BIG) {
				if (iconCount == DockUtil.ICON_COUNT_IN_A_ROW) {
					animationScale = new ScaleAnimation(1f, mZoomInProportion, 1f,
							mZoomInProportion, Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					animationScale.setDuration(duration);
				}
			}

			else if (type == DockUtil.ANIMATION_ZOOM_BIG_TO_SMALL) {
				if (iconCount == DockUtil.ICON_COUNT_IN_A_ROW) {
					animationScale = new ScaleAnimation(mZoomInProportion, 1f, mZoomInProportion,
							1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					animationScale.setDuration(duration);
				}
			}

		} else {
			if (type == DockUtil.ANIMATION_ZOOM_NORMAL_TO_BIG) {
				if (iconCount == DockUtil.ICON_COUNT_IN_A_ROW - 1) {
					animationScale = new ScaleAnimation(mZoomOutProportion, 1f, mZoomOutProportion,
							1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					animationScale.setDuration(duration);
				}
			} else if (type == DockUtil.ANIMATION_ZOOM_BIG_TO_SMALL) {
				if (iconCount == DockUtil.ICON_COUNT_IN_A_ROW - 1) {
					animationScale = new ScaleAnimation(1f, mZoomOutProportion, 1f,
							mZoomOutProportion, Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					animationScale.setDuration(duration);
				}
			}

			else if (type == DockUtil.ANIMATION_ZOOM_NORMAL) {
				if (iconCount == DockUtil.ICON_COUNT_IN_A_ROW - 1) {
					animationScale = new ScaleAnimation(mZoomOutProportion, mZoomOutProportion,
							mZoomOutProportion, mZoomOutProportion, Animation.RELATIVE_TO_SELF,
							0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					animationScale.setDuration(duration);
				}
			}

		}

		// else if (type == DockUtil.ANIMATION_ZOOM_NORMAL_TO_SMALL) {
		// if (dockViewListSize == DockUtil.ICON_COUNT_IN_A_ROW - 1) {
		// animationScale = new ScaleAnimation(1f, mZoomOutProportion, 1f,
		// mZoomOutProportion, Animation.RELATIVE_TO_SELF, 0.5f,
		// Animation.RELATIVE_TO_SELF, 0.5f);
		// animationScale.setDuration(duration);
		// }
		// }

		// else if (type == DockUtil.ANIMATION_ZOOM_SMALL_TO_NORMAL) {
		// if (dockViewListSize == DockUtil.ICON_COUNT_IN_A_ROW - 1) {
		// animationScale = new ScaleAnimation(mZoomOutProportion, 1f,
		// mZoomOutProportion, 1f, Animation.RELATIVE_TO_SELF,
		// 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		// animationScale.setDuration(duration);
		// }
		// }

		// else if (type == DockUtil.ANIMATION_ZOOM_SMALL_TO_SMALL) {
		// if (dockViewListSize == DockUtil.ICON_COUNT_IN_A_ROW - 1) {
		// animationScale = new ScaleAnimation(mZoomOutProportion,
		// mZoomOutProportion, mZoomOutProportion,
		// mZoomOutProportion, Animation.RELATIVE_TO_SELF, 0.5f,
		// Animation.RELATIVE_TO_SELF, 0.5f);
		// animationScale.setDuration(duration);
		// }
		// }

		return animationScale;
	}
	public int getExchangeIndex() {
		return mExchangeIndex;
	}

	@Override
	public void onStart(int what, Object[] params) {

	}

	@Override
	public void onFinish(int what, Object[] params) {
		switch (what) {
			case MAX_DOCK_TO_SCREEN_ANIMATION :
				((TransformListener) mLastMoveToScreenView)
						.setTransformationInfo(new TransformationInfo());
				setShakeAnim();
				break;
			case ICON_EXTRUSION_ANIMATION :
				if (params[0] instanceof Boolean) {
					boolean clearAnimate = (Boolean) params[0];
					if (clearAnimate) {
						mOperaterLayout.post(new Runnable() {
							@Override
							public void run() {
								mOperaterLayout.clearChilderenAnimation();
							}
						});
					}
				}
				mIsExtrusionAnimation = false;
				break;
			default :
				mIsExtrusionAnimation = false;
				break;
		}
	}
	/**
	 * 更新某一行图标的图片size
	 * 
	 * @param rowid
	 */
	public void updateLineLayoutIconsSize() {
		int size = mOperaterLayout.getChildCount();
		int newsize = DockUtil.getIconSize(size);
		for (int i = 0; i < size; i++) {
			IconView<?> iconView = (IconView<?>) mOperaterLayout.getChildAt(i);
			DockItemInfo info = (DockItemInfo) iconView.getInfo();
			info.setBmpSize(newsize);
			DockLogicControler.getInstance().updateDockIcon(info);
		}
	}

	public ArrayList<Position> getInitRectList() {
		return mInitRectList;
	}

	public IconView<?> getMergedView() {
		return mMergeFolderIconView;
	}

	public void setIsInsert(boolean insert) {
		mInsert = insert;
//		Log.i("dzj", mInsert + "");
//		try {
//			throw new Exception();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	public void reset() {
		if (mOperaterLayout != null) {
			mCount = mChildCount = mOperaterLayout.getChildCount();
			mOperaterLayout.setInitRect(mCount);
			setOperaterLayout(mOperaterLayout);
		}
		mDragResult = DRAG_RESULT_NONE;
		mDragInitIndex = -1;
		mDragIndex = -1;
		mIsExtrusionAnimation = false;
		mMergeFolderIconView = null;
		mIsShowFolderAnimation = false;
		setIsInsert(false);
		mAniCurDockViewList.clear();
	}

	public int getPositionNeedCount() {
		return mCount;
	}

	/**
	 * <br>功能简述:设置view的动画
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void prepareAnimForMoveToScreen(IconView<?> iconView, int iconIndex) {
		hideFolder();
		//1:先清原来动画缓存
		if (mLastMoveToScreenView != null) {
			if (mLastMoveToScreenView.getAnimation() != null) {
				mLastMoveToScreenView.clearAnimation();
			}
		}
		//2:位移动画
		setTransactionAnim();

	}

	private void setTransactionAnim() {
		if (mExchangeIndex < 0) {
			return;
		}
		AnimationTask task = new AnimationTask(false, AnimationTask.PARALLEL);
		task.setBatchAnimationObserver(this, MAX_DOCK_TO_SCREEN_ANIMATION);
		if (mLastMoveToScreenView != null && mLastMoveToScreenView.isVisible()) {
			GLDockLineLayout lineLayout = mOperaterLayout;
			int count = lineLayout.getChildCount();
			lineLayout.setInitRect(count);
			Rect rect = lineLayout.getInitRectList().get(mExchangeIndex).outRect;
			int rectX = rect.left + (rect.right - rect.left) / 2;
			int rectY = rect.top + (rect.bottom - rect.top) / 2;
			GLCellLayout.cellsToCenterPoint(mCellXY[0], mCellXY[1], 1, 1, mTmpPoint);
			float[] iconCenterPoint = IconUtils.getIconCenterPoint(mTmpPoint[0], mTmpPoint[1],
					GLScreenShortCutIcon.class);
			GLDockViewUtil.transformPointScreen2Dock(iconCenterPoint);
			int viewX = (int) iconCenterPoint[0];
			int viewY = (int) iconCenterPoint[1];
			Animation transactionAnimation = new TranslateAnimation(0, viewX - rectX, 0, viewY
					- rectY);
			float screenIconSize = IconUtilities.getStandardIconSize(ApplicationProxy.getContext());
			float dockIconSize = DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW);
			float mZoomInProportion = screenIconSize / dockIconSize;
			Animation animationScale = new ScaleAnimation(1f, mZoomInProportion, 1f,
					mZoomInProportion, Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			animationScale.setDuration(100);
			mAnimationSet = new AnimationSet(true);
			mAnimationSet.addAnimation(animationScale);
			mAnimationSet.addAnimation(transactionAnimation);
			mAnimationSet.setInterpolator(new AccelerateDecelerateInterpolator());
			mAnimationSet.setDuration(100);
			mAnimationSet.setFillAfter(true);
			task.addAnimation(mLastMoveToScreenView, mAnimationSet, null);
		}

		GLAnimationManager.startAnimation(task);
	}

	public void clearMoveToScreenAnim() {
		if (mLastMoveToScreenView != null) {
			mLastMoveToScreenView.setTransformationInfo(null);
			mLastMoveToScreenView.clearAnimation();
			mLastMoveToScreenView = null;
		}

		if (mAnimationSet != null) {
			mAnimationSet.cancel();
			mAnimationSet = null;
		}
		if (mShakeAnimator != null) {
			mShakeAnimator.cancel();
			mShakeAnimator = null;
		}

		mCellXY = null;
	}

	private void setShakeAnim() {
		if (mShakeAnimator == null) {
			mShakeAnimator = ValueAnimator.ofFloat(0f, 1f);
			mShakeAnimator.setRepeatMode(ValueAnimator.REVERSE);
			mShakeAnimator.setRepeatCount(ValueAnimator.INFINITE);
			mShakeAnimator.setDuration(DockUtil.MOVE_TO_SCREEN_SHAKE_DURATION);
			mShakeAnimator.setStartDelay(DockUtil.MOVE_TO_SCREEN_TRANSACTION_DURATION);
			final float scale = 1.0f;
			mShakeAnimator.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					float r = ((Float) animation.getAnimatedValue()).floatValue();
					float y = r * (-10); //位移-10
					((TransformListener) mLastMoveToScreenView).setTranslateXY(0, y);
					float s = scale * (r * 1.05f + (1 - r)); //从1.0到1.05变化
					((TransformListener) mLastMoveToScreenView).setScaleXY(s, s);
					mLastMoveToScreenView.invalidate();
				}
			});
		} else {
			mShakeAnimator.cancel();
		}

		mShakeAnimator.start();
	}

	/**
	 * 
	 * <br>类描述:操作图标的回调接口
	 * <br>功能详细描述:
	 * 
	 * @author  zhujian
	 */
	//	public interface OnDockDragHanderListener {
	/**
	 * 
	 * 该方法用于通知dock桌面抓起来的view与dock的哪一个view发生挤压互换
	 * @param iconView  被挤压的view
	 * @param sourceView  被抓起的view
	 * @param dragInfo 被抓起的图标
	 * @param targetIndex 被挤压的图标的下标
	 * @param isCancel 是否取消进行的动画
	 */
	private void exchangeIconAnimation(final DragSource source, final IconView iconView,
			DragView dragView, Object dragInfo, final int targetIndex, boolean isCancel) {
		if (isCancel) {
			//取消动画
			clearMoveToScreenAnim();
		}
		if (iconView != null && iconView != mLastMoveToScreenView
				&& !(dragView.getOriginalView() instanceof GLDockIconView)) {
			mExchangeIndex = targetIndex;
			mLastMoveToScreenView = iconView;
			mDragView = dragView;
			iconView.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (isVacant(source)) {
						prepareAnimForMoveToScreen(iconView, targetIndex);
					}

				}
			}, 100);
		}
	};

	//	}

	private boolean checkScreenVacant(int spanX, int spanY, int[] cellXY, int[] cellXYRealPonts, DragSource source) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(spanX);
		list.add(spanY);
		return MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.GET_DROP_DOCK_LOCATION, 0, source,
				mDragView, cellXY, cellXYRealPonts, list);
	}

	private boolean isVacant(DragSource source) {
		boolean isVacant = false;
		int[] cellXYRealPonts = new int[2]; // 将要添加到的位置对应当前屏幕的坐标XY
		mCellXY = new int[2]; // 当前屏幕cellx celly
		isVacant = checkScreenVacant(1, 1, mCellXY, cellXYRealPonts, source);
		return isVacant;

	}
	public final boolean saveMoveToScreenData(DragView dragView, Object dragInfo,
			DragSource dragSource) {
		boolean flag = false;
		if (mLastMoveToScreenView != null && mCellXY != null && dragView != null) {
			DockLogicControler controler = DockLogicControler.getInstance();
			DockItemInfo info = (DockItemInfo) mLastMoveToScreenView.getInfo();
			ItemInfo itemInfo = info.mItemInfo;
			itemInfo.mCellX = mCellXY[0];
			itemInfo.mCellY = mCellXY[1];
			//发消息给屏幕层增加一个view
			boolean isAddSuccess = MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.EXCHANGE_ICON_FROM_DOCK, -1, info, dragView, dragInfo, dragSource);
			if (isAddSuccess) {
				mLastMoveToScreenView.setVisibility(GLView.INVISIBLE);
				FeatureItemInfo tag = (FeatureItemInfo) DragInfoTranslater.createItemInfoForDock(
						dragView.getDragViewType(), dragInfo);
				if (dragSource instanceof GLDockFolderGridVIew && mLastMoveToScreenView != null) {
					GLDockFolderIcon folderIcon = (GLDockFolderIcon) GLAppFolder.getInstance()
							.getCurFolderIcon();
					DockItemInfo dockItemInfo = (DockItemInfo) folderIcon.getInfo();
					UserFolderInfo userFolderInfo = (UserFolderInfo) dockItemInfo.mItemInfo;
					ItemInfo dragItemInfo = (ItemInfo) dragInfo;
					userFolderInfo.remove(dragItemInfo.mInScreenId);
					dragItemInfo.unRegisterObserver(dockItemInfo);
					Long deleteid = dragItemInfo.mInScreenId;
					GLAppFolderController.getInstance().removeAppFromScreenFolder(deleteid,
							userFolderInfo.mInScreenId);
					int size = userFolderInfo.getContents().size();
					int clickIndex = (Integer) folderIcon.getTag(R.integer.dock_index);
					boolean needReArrange = false;
					if (size < 2) {
						if (size == 1) {
							//scene1：文件夹中只剩一个icon，改文件夹为快捷图标
							ShortCutInfo shortCutInfo = userFolderInfo.getChildInfo(0);
							dockItemInfo.setInfo((FeatureItemInfo) shortCutInfo);
							dockItemInfo.setmRowId(mOperaterLayout.getLineID());
							dockItemInfo.setmIndexInRow(clickIndex);
							controler.updateDockItem(userFolderInfo.mInScreenId, dockItemInfo);
						} else if (info.equals(dockItemInfo)) {
							//scene2：文件夹为空，被挤压的是文件夹自己，则保留原文件夹，用作后面的挤压替换操作
							//ADT-16195
							//桌面和dock栏存在同样的图标，拖动桌面图标与dock栏图标合并文件夹后，拖动该文件夹图标至dock栏，图标消失
							//特殊情况，被挤压到桌面的是dragView所在文件夹， 不删除原文件夹，在后面更新数据库即可
						} else {
							//scene3：文件夹为空，且不是被挤压对象，从Dock栏中删除
							//ADT-16023
							//Dock栏，两个重复的图标同时放入dock栏后，将文件夹中的图标移到dock栏上，文件夹中已无图标但文件夹还在
							// 使用新的方法进行删除操作，删除后已经对数据列表进行排序整理，无需再整理
							controler.deleteShortcutItemAndReArrange(dockItemInfo.getmRowId(), userFolderInfo.mInScreenId);
//							needReArrange = true;
						}
					}
					// 将拖拽info绑定到被挤压的icon，达到挤压替换效果
					long oldId = info.mItemInfo.mInScreenId;
					info.setInfo(tag);
					info.setmRowId(mOperaterLayout.getLineID());
					// ADT-16650 桌面移动文件夹到dock栏，dock栏重启
					// 注释掉下面一行代码，
//					info.setmIndexInRow(mExchangeIndex);
					controler.updateDockItem(oldId, info);
					// 产生脏数据的一个路径
					// 1.Dock栏满员（5个），包含一个特殊文件夹（文件夹只包含一个icon）
					// 2.从上述特殊文件夹中拖拽iconA到Dock栏，挤压该文件夹右边的一个icon到屏幕层，松手，拖拽结束，Dock栏当前只剩4个icon
					// 3.从屏幕拖拽任意一个icon到iconA右边，松手，拖拽结束，此时检测已经存在脏数据
//					if (needReArrange) {
//						// step1:纠错缓存及数据库中的index
//						ArrayList<DockItemInfo> dockItemInfos = controler.getShortCutItems().get(
//								mOperaterLayout.getLineID());
//						for (DockItemInfo infoChecked : dockItemInfos) {
//							int index = infoChecked.getmIndexInRow();
//							if (index > clickIndex) {
//								controler.modifyShortcutItemIndex(infoChecked, --index);
//							}
//						}
//						// step2：纠错DockIconView绑定的临时index缓存
//						int childCount = mOperaterLayout.getChildCount();
//						for (int i = 0; i < childCount; i++) {
//							IconView dockIconView = (IconView) mOperaterLayout.getChildAt(i);
//							int index = (Integer) dockIconView.getTag(R.integer.dock_index);
//							DockItemInfo dockItemInfoCached = (DockItemInfo) dockIconView.getInfo();
//							if (index != dockItemInfoCached.getmIndexInRow()) {
//								dockIconView.setTag(R.integer.dock_index,
//										dockItemInfoCached.getmIndexInRow());
//							}
//						}
//					}
				} else {
					mExchangeIndex = (Integer) mLastMoveToScreenView.getTag(R.integer.dock_index);
					long oldId = info.mItemInfo.mInScreenId;
					info.setInfo(tag);
					controler.updateDockItem(oldId, info);
					if (dragSource instanceof GLAllAppGridView && tag instanceof UserFolderInfo) {
						controler.addDrawerFolderToDock((UserFolderInfo) tag); // 插入文件夹子项
					}
				}
				flag = true;
			}
		}
		return flag;
	}
	
	public float getZoomInProportion() {
		return mZoomInProportion;
	}

	public float getZoomOutProportion() {
		return mZoomOutProportion;
	}

}
