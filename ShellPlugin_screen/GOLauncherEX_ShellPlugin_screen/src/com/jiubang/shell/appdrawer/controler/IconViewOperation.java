package com.jiubang.shell.appdrawer.controler;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.Interpolator;

import com.go.gl.animation.Animation;
import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.view.GLView;
import com.go.util.graphics.DrawUtils;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.shell.animation.DropAnimation;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.animation.TranslateValueAnimation;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.common.management.JobManager;
import com.jiubang.shell.common.management.JobManager.Job;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;

/**
 * 
 * <br>类描述:图标操作类
 * <br>功能详细描述:
 * 
 * @author  wuziyi
 */
public class IconViewOperation implements BatchAnimationObserver {

	private int mDragSourceIndex; //最开始拖拽图标的下标
	private int mDragTargetIndex; //最后放下图标的下标
	private int mInvisitIndex; //当前发生动画后看不见图标的位置下标
	private int mSourceBeforeTranslate; //当前发生动画前看不见图标的下标
	private int mCurrentScreenFirstIndex = -1; //当前屏第一个下标
	private int mCurrentScreenLastIndex = -1; //当前屏最后一个下标
	private int mPageCount; //每一页的个数
	private int mReadyFolderIndex;
	private Object mDragInfo;
	private DragView mDragView;

	private int mPreX;
	private int mPreY;
	
	private Rect mTouchRect; //拖拽挤压的区域(必须设置)

	private List<Rect> mReadyFolderRectList;
	private List<Rect> mIconRectList;
	private int[] mIconRelView = new int[2];

	private OnOperationIconViewListener mListener;

	private int mTime400 = DURATION_400;

	private Handler mHandler;
	
	private boolean mIsFling = false; //是否是快速拖拽的标志位
//	private boolean mIsDraging = false; //是否可以拖拽的标志位
	private boolean mIsAnimationing = false; //是否正在做动画的标志位
	private boolean mIsEnterOverlay = false; //是否进入图标重叠区域
	private boolean mIsEnableOverlay = true; //是否开启重叠区域判断
//	private int mAnimationCount;
	private long mAnimationStartTime;
	
	// debug用标识
	private boolean mIsInitedParmeter;
	private boolean mIsRunIntoIt;

	private static final float SHALFONE = 0.5f;
	private Interpolator mInterpolator;
	/**
	 * 挤压动画默认时长
	 */
	private static final int DURATION_400 = 400;
	/**
	 * 当前挤压敏感度，越接近0越敏感
	 */
	private static final int CURRENT_SCENE = 200;	
	private static final int TRANSLATE_ANIMAITION = 0;
	private static final int READY_OVERLAY_ANIMATION = 1;
	private static final int CANCLE_OVERLAY_ANIMATION = 2;  // 该消息暂时不用
	private static final int REMOVE_APP_ANIMATION = 3;

	public IconViewOperation(Rect touchRect, Context context) {
		this.mTouchRect = touchRect;
		mInterpolator = InterpolatorFactory.getInterpolator(InterpolatorFactory.EASE_IN_OUT);
		mIconRectList = new ArrayList<Rect>();
		mReadyFolderRectList = new ArrayList<Rect>();
		initHandler();
	}

	public void setOperationListener(OnOperationIconViewListener listener) {
		mListener = listener;
	}
	
	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case TRANSLATE_ANIMAITION:
						calculateTranslate(TRANSLATE_ANIMAITION, msg.arg1, msg.arg2);
						break;
						
					case READY_OVERLAY_ANIMATION:
						if (mListener != null) {
							mIsEnterOverlay = mListener.onEnterIconOverlay(msg.arg1, mDragInfo);
							mReadyFolderIndex = msg.arg1;
						}
						break;
	
					case CANCLE_OVERLAY_ANIMATION:
						exitIconOverlay();
						break;
						
					case REMOVE_APP_ANIMATION : 
						calculateTranslate(REMOVE_APP_ANIMATION, msg.arg1, msg.arg2);
						break;
						
					default:
						break;
				}
			}
		};
	}
	
	private void calculateTranslate(final int animationType, final int source, final int target) {
		AnimationTask task = new AnimationTask(true, AnimationTask.PARALLEL);
		if (source != target) {
			int begin = source;
			int end = target;
			if (source > mCurrentScreenLastIndex) {
				begin = mCurrentScreenLastIndex;
			} else if (source < mCurrentScreenFirstIndex) {
				begin = mCurrentScreenFirstIndex;
			}
			if (target > mCurrentScreenLastIndex) {
				end = mCurrentScreenLastIndex;
			} else if (target < mCurrentScreenFirstIndex) {
				end = mCurrentScreenFirstIndex;
			}
			task.setBatchAnimationObserver(IconViewOperation.this, animationType, source, target);
			// 抓取图标往后移动
			if (end > begin) {
				for (int i = begin; i <= end; i++) {
					int location = i;
					if (i == begin) {
						if (begin == mCurrentScreenFirstIndex) {
							addScaleToTask(task, location);
						}
					} else {
						addTranslateToTask(location, location - 1, task);
					}
				}
				// 抓取图标往前移动
			} else if (end < begin) {
				// 做一下参数调整
				int temp = begin;
				begin = end;
				end = temp;
				for (int i = begin; i <= end; i++) {
					int location = i;
					if (i == end) {
						if (end == mCurrentScreenLastIndex) {
							addScaleToTask(task, location);
						}
					} else {
						addTranslateToTask(location, location + 1, task);
					}
				}
			}
			mInvisitIndex = target;
		}
		onTranslateStart(animationType, source, target);
		if (!task.getAnimationList().isEmpty()) {
			startBatchAnimation(animationType, task);
		}
		onTranslateEnd(animationType, source, target);
	}
	
	private void startBatchAnimation(int animationType, AnimationTask task) {
		mIsAnimationing = true;
		mAnimationStartTime = System.currentTimeMillis();
		Job animationJob = new Job(animationType, task, false);
		JobManager.postJob(animationJob);
//		GLAnimationManager.startAnimation(task);
	}

	private void onTranslateEnd(final int animationType, final int source,
			final int target) {
		Job animationEnd = new Job(animationType, new Runnable() {
			@Override
			public void run() {
				switch (animationType) {
					case TRANSLATE_ANIMAITION :
						if (mListener != null) {
							mListener.switchAnimationEnd(mDragInfo, target, source);
						}
						mSourceBeforeTranslate = target;
						break;
	
					case REMOVE_APP_ANIMATION :
						if (mListener != null) {
							mListener.removeIconAnimationEnd(mDragInfo, target);
						}
						mSourceBeforeTranslate = target;
						break;
					default:
						break;
				}
				
			}
		}, true);
		JobManager.postJob(animationEnd);
	}
	
	private void onTranslateStart(int animationType, final int source, final int target) {
		switch (animationType) {
			case TRANSLATE_ANIMAITION :
				if (mListener != null) {
//					Runnable runnable = new Runnable() {
//						@Override
//						public void run() {
							mListener.switchAnimationStart(mDragInfo, target, source);
//						}
//					};
//					Job startJob = new Job(TRANSLATE_ANIMAITION, runnable, true);
//					mJobManager.postJob(startJob);
				}
				break;
	
			default:
				break;
		}
	}
	
	/**
	 * 触发长按事件的时候需要被调用
	 * @param sourceIndex 长按的图标下标
	 */
	public void setDragSourceAndTargetIndex(int sourceIndex, int targetIndex) {
		mDragSourceIndex = sourceIndex;
		mInvisitIndex = sourceIndex;
		mDragTargetIndex = targetIndex;
		mSourceBeforeTranslate = sourceIndex;
	}

	/**
	 * 拿到当前屏的第一个和最后一个图标的下标
	 * @param firstIndex
	 * @param lastIndex
	 */
	public void setScreenFirstAndLastIndex(int firstIndex, int lastIndex) {
		mCurrentScreenFirstIndex = firstIndex;
		mCurrentScreenLastIndex = lastIndex;
	}
	
	public void setIsFling(boolean isFling) {
		mIsFling = isFling;
	}
	
	public void setIsEnableOverlay(boolean isEnableOverlay) {
		mIsEnableOverlay = isEnableOverlay;
	}

	public int getInvisitIndex() {
		return mInvisitIndex;
	}
	
	public int getSourceBeforeTranslate() {
		return mSourceBeforeTranslate;
	}
	
	public int getDragTargetIndex() {
		return mDragTargetIndex;
	}
	
	public int getDragSourceIndex() {
		return mDragSourceIndex;
	}

	public Object getDragInfo() {
		return mDragInfo;
	}
	
	public DragView getDragView() {
		return mDragView;
	}
	
	public boolean isEnterOverlay() {
		return mIsEnterOverlay;
	}

	/**
	 * 第一次长按的时候需要被调用
	 * @param curScreenIconList
	 * @param isClearRectList
	 * @param pageCount
	 */
	public void initParameter(List<Rect> iconRectList, List<Rect> readyFolderRectList, boolean isClearRectList, int pageCount) {
		mIsRunIntoIt = true;
		if (isClearRectList || mIconRectList.size() != pageCount) {
			mPageCount = pageCount;
			mIconRectList = iconRectList;
			mReadyFolderRectList = readyFolderRectList;
			mIsInitedParmeter = true;
		}
	}
	
	public void doDragStart() {
		// 由于animation有可能不回调animationEnd，因此这标记真心TMD恐怖，所以在每次抓起的时候重置
		mIsAnimationing = false; 
		mReadyFolderIndex = -1;
	}
	
	public void doDragEnd() {
//		mDragInfo = null;
//		mDragView = null;
	}

	public void doDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		mDragInfo = dragInfo;
		mDragView = dragView;
	}

	public void doDragMove(int x, int y, int xOffset, int yOffset) {
		
	}
	
	private void addTranslateToTask(int fromIndex, int targetIndex, AnimationTask task) {
		Rect from = mIconRectList.get(fromIndex - mCurrentScreenFirstIndex);
		Rect target = mIconRectList.get(targetIndex - mCurrentScreenFirstIndex);
		int fromX = from.left;
		int fromY = from.top;
		int toX = target.left;
		int toY = target.top;
		GLView iconView = mListener.getViewDoAnimation(fromIndex);
		if (iconView != null && iconView.isVisible()) {
//			Animation animation = iconView.getAnimation();
			TranslateValueAnimation translateAnimation;
//			if (animation instanceof TranslateValueAnimation) {
//				translateAnimation = (TranslateValueAnimation) animation;
//				translateAnimation.reStartValueAnimation(toX, toY);
//			} else {
				translateAnimation = new TranslateValueAnimation(fromX, toX, fromY, toY, mTime400);
				translateAnimation.setDuration(mTime400);
				translateAnimation.setInterpolator(mInterpolator);
				// 这三行代码很神奇，不要问为什么，不这样搞的话，动画闪的你眼花 wuziyi
//				translateAnimation.setFillEnabled(true);
				translateAnimation.setFillAfter(false);
//				translateAnimation.setFillBefore(false);
//			}
			task.addAnimation(iconView, translateAnimation, null);
		}
	}

	public void doDragOver(int x, int y, int xOffset, int yOffset, DragView dragView,
			Object dragInfo) {
		final int absX = Math.abs(mPreX - x);
		mPreX = x;
		final int absY = Math.abs(mPreY - y);
		mPreY = y;
		// 拖拽时的变化值不应该定为15，有些机型无法满足以下条件
		if (absX > DrawUtils.sTouchSlop
				|| absY > DrawUtils.sTouchSlop) {
			exitIconOverlay();
			mIsFling = true;
			return;
		} else {
			mIsFling = false;
		}
		// 如果需要最高灵敏度，这个判断要取消，但因太高灵敏度导致出现动画并行，不担保完全与其他业务同在的情况下正确
		if (mIsAnimationing) {
			return;
		}
		if (mListener == null) {
			return;
		}
		int dragViewIconX = x - xOffset - mIconRelView[0];
		int dragViewIconY = y - yOffset - mIconRelView[1];
		if (!mTouchRect.contains(dragViewIconX, dragViewIconY)) {
			mIsFling = true;
			return;
		}
		int targetIndex = -1;
		if (mIsEnableOverlay) {
			targetIndex = calculateTarget(dragViewIconX, dragViewIconY);
		} else {
			targetIndex = calculateNearestTarget(dragViewIconX, dragViewIconY);
		}
		if (targetIndex == -1) {
			return;
		}
//		if (mInvisitIndex == targetIndex) {
//			return;
//		}
		if (mDragTargetIndex == targetIndex) {
			return;
		}
		mDragTargetIndex = targetIndex;
		Log.i("wuziyi", "targetIndex: " + targetIndex);
		mHandler.removeMessages(TRANSLATE_ANIMAITION);
		Message msg = mHandler.obtainMessage(TRANSLATE_ANIMAITION);
		msg.arg1 = mInvisitIndex;
		msg.arg2 = targetIndex;
		mHandler.sendMessageDelayed(msg, CURRENT_SCENE);
	}

	private int calculateTarget(int dragViewIconX, int dragViewIconY) {
		int targetIndex = -1;
		//计算可以挤压的图标的下标
		for (int i = mCurrentScreenFirstIndex, j = 0; i <= mCurrentScreenLastIndex; i++, j++) {
			Rect readFolderRect = mReadyFolderRectList.get(j);
			Rect iconRect = mIconRectList.get(j);
			int iconCenterX = iconRect.left + iconRect.width() / 2;
			if (iconRect.contains(dragViewIconX, dragViewIconY)) {
				if (readFolderRect.contains(dragViewIconX, dragViewIconY)) {
					// 准备文件夹
					// 把挤压动画收回，并且重置一下目标位置
					// 自身位置不触发
					if (mInvisitIndex != i) {
						mHandler.removeMessages(TRANSLATE_ANIMAITION);
						if (!mHandler.hasMessages(READY_OVERLAY_ANIMATION) && mReadyFolderIndex != i) {
							if (mIsEnterOverlay) {
								exitIconOverlay();
							}
							mDragTargetIndex = -1;
							Message msg = mHandler.obtainMessage();
							msg.what = READY_OVERLAY_ANIMATION;
							msg.arg1 = i;
							mHandler.sendMessageDelayed(msg, CURRENT_SCENE);
						}
					}
					break;
				} else {
					mHandler.removeMessages(READY_OVERLAY_ANIMATION);
					exitIconOverlay();
					
					targetIndex = i;
					// 图标往前移动
					if (mInvisitIndex > targetIndex) {
						if (dragViewIconX >= readFolderRect.right && targetIndex < mCurrentScreenLastIndex) {
							targetIndex++;
						} else if (dragViewIconX <= readFolderRect.left) {
							// 不变
						} else if (dragViewIconY >= readFolderRect.top || dragViewIconY <= readFolderRect.bottom) {
//							targetIndex = -1;
							if (dragViewIconX >= iconCenterX && targetIndex < mCurrentScreenLastIndex) {
								targetIndex++;
							} else {
								// 不变
							}
						} 
					// 图标往后移动
					} else if (mInvisitIndex < targetIndex) {
						if (dragViewIconX >= readFolderRect.right) {
							// 不变
						} else if (dragViewIconX <= readFolderRect.left && targetIndex > mCurrentScreenFirstIndex) {
							targetIndex--;
						} else if (dragViewIconY >= readFolderRect.top || dragViewIconY <= readFolderRect.bottom) {
//							targetIndex = -1;
							if (dragViewIconX <= iconCenterX && targetIndex > mCurrentScreenFirstIndex) {
								targetIndex--;
							} else {
								// 不变
							}
						} 
					}
				}
				break;
			}
		}
		// 当上面情况全部不中，且当前该版图标不满一屏时
		if (mCurrentScreenLastIndex - mCurrentScreenFirstIndex < mPageCount - 1 && targetIndex == -1) {
			Rect rect = mIconRectList.get(mCurrentScreenLastIndex - mCurrentScreenFirstIndex);
			if ((dragViewIconX > rect.right && dragViewIconY > rect.top) || dragViewIconY > rect.bottom) {
				targetIndex = mCurrentScreenLastIndex;
			}
		}
		return targetIndex;
	}
	
	public void doDragExit(int x, int y, int xOffset, int yOffset, DragView dragView,
			Object dragInfo) {
		mHandler.removeMessages(READY_OVERLAY_ANIMATION);
		exitIconOverlay();
		mHandler.removeMessages(TRANSLATE_ANIMAITION);
//		if (mHandler.hasMessages(TRANSLATE_ANIMAITION)) {
//			mHandler.removeMessages(TRANSLATE_ANIMAITION);
//			// 立即启动当前的挤压动画
//			calculateTranslate(TRANSLATE_ANIMAITION, mInvisitIndex, mDragTargetIndex);
//		}
	}

	private void addScaleToTask(AnimationTask task, int location) {
		GLView iconView = mListener.getViewDoAnimation(location);
		if (iconView != null && iconView.isVisible()) {
			ScaleAnimation animation = new ScaleAnimation(1.0f, 0f, 1.0f, 0f,
					Animation.RELATIVE_TO_SELF, SHALFONE, Animation.RELATIVE_TO_SELF, SHALFONE);
			animation.setDuration(mTime400);
			// 这三行代码很神奇，不要问为什么，不这样搞的话，动画闪的你眼花 wuziyi
//			animation.setFillEnabled(true);
			animation.setFillAfter(false);
//			animation.setFillBefore(false);
			task.addAnimation(iconView, animation, null);
		}
	}

	public boolean doDrop(int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo, DropAnimationInfo resetInfo) {
		boolean success = false;
		if (mListener == null) {
			return success;
		}
		int dragViewIconX = x - xOffset - mIconRelView[0];
		int dragViewIconY = y - yOffset - mIconRelView[1];
		if (mIsAnimationing) {
			long curTime = System.currentTimeMillis();
			long passTime = curTime - mAnimationStartTime;
			int duration = DURATION_400 - (int) passTime;
			if (duration < DropAnimation.DURATION_210) {
				resetInfo.setDuration(DropAnimation.DURATION_210);
			} else {
				resetInfo.setDuration(duration);
			}
			mAnimationStartTime = 0;
			mListener.dataChange(dragInfo, mDragTargetIndex, mDragSourceIndex);
			resetDragIcon(resetInfo);
			success = true;
		} else if (mIsEnterOverlay) {
			// 快速放手时，并且文件夹没有做好打开准备，就计算最近点
			resetInfo.setNeedToShowCircle(false);
			mListener.onDropInIconOverlay(dragInfo, mReadyFolderIndex, mDragSourceIndex, mInvisitIndex, dragViewIconX, dragViewIconY, dragView, resetInfo);
			mReadyFolderIndex = -1;
			mIsEnterOverlay = false;
			resetDragIcon(resetInfo);
			success = true;
		} else if (mDragTargetIndex == -1 || mIsFling) {
			// 这TMD有可能中点在屏幕外，要调整回来
			if (dragViewIconX < mTouchRect.left) {
				dragViewIconX = mTouchRect.left + 4;
			} else if (dragViewIconX > mTouchRect.right) {
				dragViewIconX = mTouchRect.right - 4;
			}
			if (dragViewIconY < mTouchRect.top) {
				dragViewIconY = mTouchRect.top + 4;
			} else if (dragViewIconY > mTouchRect.bottom) {
				dragViewIconY = mTouchRect.bottom - 4;
			}
			int targetIndex = calculateNearestTarget(dragViewIconX,
					dragViewIconY);
			Log.i("wuziyi", "计算结果：" + targetIndex);
			if (targetIndex == -1) {
//				Rect first = mIconRectList.get(0);
//				int left = first.left;
//				int top = first.top;
//				Rect last = mIconRectList.get(mIconRectList.size() - 1);
//				int right = last.right;
//				int botton = last.bottom;
//				throw new IllegalArgumentException("Drop X point:" + dragViewIconX + " Drop Y point:" + dragViewIconY + "Icon Rect:" + left + " " + top + " " + right + " " + botton);
				return false;
			}
//			if (mDragSourceIndex == targetIndex) {
//				throw new NullPointerException("草泥马，这是耍我么？ mIsFling：" + mIsFling + " mDragSourceIndex:" + mDragSourceIndex + " mDragTargetIndex:" + mDragTargetIndex);
//				return true;
//			}
			mDragTargetIndex = targetIndex;
			mTime400 = 200;
			resetInfo.setDuration(DropAnimation.DURATION_250);
			mHandler.removeMessages(TRANSLATE_ANIMAITION);
			calculateTranslate(TRANSLATE_ANIMAITION, mInvisitIndex, mDragTargetIndex);
			mListener.dataChange(dragInfo, mDragTargetIndex, mDragSourceIndex);
			resetDragIcon(resetInfo);
			success = true;
		} else {
//			resetInfo.setNeedToShowCircle(false);
			resetInfo.setDuration(DropAnimation.DURATION_250);
			if (mHandler.hasMessages(TRANSLATE_ANIMAITION)) {
				mTime400 = 200;
				mHandler.removeMessages(TRANSLATE_ANIMAITION);
				calculateTranslate(TRANSLATE_ANIMAITION, mInvisitIndex, mDragTargetIndex);
			}
			mListener.dataChange(dragInfo, mDragTargetIndex, mDragSourceIndex);
			resetDragIcon(resetInfo);
			success = true;
		}
		mHandler.removeMessages(READY_OVERLAY_ANIMATION);
		if (success) {
//			resetDragIcon(resetInfo);
		}
		return success;
	}
	
	public void doDropCompleted(Object target, Object dragInfo, boolean success,
			DropAnimationInfo resetInfo) {
//		if (!success) {
//			resetDragIcon(resetInfo);
//		}
	}
	
	/**
	 * 启动挤压补位动画(暂时只供进入／创建文件夹使用)
	 * @param targetIndex 目标位置
	 */
	public void removeIconExtrusion(int fromIndex, int targetIndex) {
//		if (fromIndex >= mCurrentScreenLastIndex) {
//			mInvisitIndex = fromIndex;
//			mListener.removeIconAnimationEnd(mDragInfo, fromIndex);
////			mSourceBeforeTranslate = mInvisitIndex;
//		} else {
			mDragTargetIndex = targetIndex;
			calculateTranslate(REMOVE_APP_ANIMATION, fromIndex, mDragTargetIndex);
//		}
	}

	public void resetDragIcon(DropAnimationInfo resetInfo) {
		int duration = resetInfo.getDuration();
		if (duration == -1) {
			resetInfo.setDuration(DropAnimation.DURATION_210);
		}
		float[] point = resetInfo.getLocationPoint();
		if (point[0] == -1 && point[1] == -1 && resetInfo.getDuration() > 0) {
			int location;
			location = mDragTargetIndex;
//			Rect locationRect = mIconRectList.get(location % mPageCount);
			Log.i("wuziyi", "mDragTargetIndex:" + mDragTargetIndex);
			Log.i("wuziyi", "mCurrentScreenFirstIndex:" + mCurrentScreenFirstIndex);
			int index = location - mCurrentScreenFirstIndex;
			if (index < 0 || mIconRectList.isEmpty()) {
				index = 0;
			} else if (index >= mIconRectList.size()) {
				index = index - (mIconRectList.size() - 1);
			}
			if (mIconRectList.isEmpty()) {
				GoAppUtils.postLogInfo("wuziyi", "IconViewOperation debug infos mIsRunIntoIt:" + mIsRunIntoIt + " mIsInitedParmeter:" + mIsInitedParmeter);
				return;
			}
			Rect locationRect = mIconRectList.get(index);
			resetInfo.setLocationType(DropAnimationInfo.LOCATION_CENTER);
			resetInfo.setLocationPoint(locationRect.left + locationRect.width() / 2, locationRect.top + locationRect.height() / 2);
		}
	}

	/**
	 * <br>功能简述:获取最接近图标的下标位置并且记录，用于 快速放手/不需要进行重叠判断 的情景
	 * <br>功能详细描述:
	 * <br>注意:当XY在范围内，必定能拿到最近点
	 * @return 
	 */
	private int calculateNearestTarget(int dragViewIconX, int dragViewIconY) {
		int targetIndex = -1;
		//计算可以挤压的图标的下标
		for (int i = mCurrentScreenFirstIndex, j = 0; i <= mCurrentScreenLastIndex; i++, j++) {
//			Rect readFolderRect = mReadyFolderRectList.get(j);
			Rect iconRect = mIconRectList.get(j);
			if (iconRect.contains(dragViewIconX, dragViewIconY)) {
//				if (readFolderRect.contains(dragViewIconX, dragViewIconY)) {
//					// 啥都不干！
//				} else {
					int iconCenterX = iconRect.left + iconRect.width() / 2;
					targetIndex = i;
					// 图标往前移动
					if (mInvisitIndex > targetIndex) {
						if (dragViewIconX >= iconCenterX && targetIndex < mCurrentScreenLastIndex) {
							targetIndex++;
						} else if (dragViewIconX <= iconCenterX) {
							// 不变
						}
//						else if (dragViewIconY >= readFolderRect.top || dragViewIconY <= readFolderRect.bottom) {
//							targetIndex = -1;
//						} 
					// 图标往后移动
					} else if (mInvisitIndex < targetIndex) {
						if (dragViewIconX >= iconCenterX) {
							// 不变
						} else if (dragViewIconX <= iconCenterX && targetIndex > mCurrentScreenFirstIndex) {
							targetIndex--;
						}
//						else if (dragViewIconY >= readFolderRect.top || dragViewIconY <= readFolderRect.bottom) {
//							targetIndex = -1;
//						} 
					}
//				}
				break;
			}
		}
		// 当上面情况全部不中，且当前该版图标不满一屏时
		if (mCurrentScreenLastIndex - mCurrentScreenFirstIndex < mPageCount - 1 && targetIndex == -1) {
			Rect rect = mIconRectList.get(mCurrentScreenLastIndex - mCurrentScreenFirstIndex);
			if ((dragViewIconX > rect.right && dragViewIconY > rect.top) || dragViewIconY > rect.bottom) {
				targetIndex = mCurrentScreenLastIndex;
			}
		}
		return targetIndex;
	}

	/**
	 * 处理进入了滚屏空间的事件
	 */
	public void doEnterScrollZone() {
		exitIconOverlay();
	}

	/**
	 * 
	 * <br>类描述:操作图标的回调接口
	 * <br>功能详细描述:
	 * 
	 * @author  wuziyi
	 */
	public interface OnOperationIconViewListener {
		/**
		 * 该方法在元素移除补位动画完成时调用
		 * @param removeIndex
		 */
		public void removeIconAnimationEnd(Object dragInfo, int removeIndex);
		/**
		 * 该方法用于挤压换位动画完成后，通知局部刷新gridView
		 * @param dragInfo 被抓起的图标
		 * @param targetIndex 到这里
		 * @param sourceIndex 从这里
		 */
		public void switchAnimationEnd(Object dragInfo, int targetIndex, int sourceIndex);
		/**
		 * 该方法用于挤压换位动画完成后，通知局部刷新gridView
		 * @param dragInfo 被抓起的图标
		 * @param targetIndex 到这里
		 * @param sourceIndex 从这里
		 */
		public void switchAnimationStart(Object dragInfo, int targetIndex, int sourceIndex);
		/**
		 * 该方法在放手后，通知刷新内存和数据库
		 * @param dragInfo 被抓起的图标
		 * @param targetIndex 到这里
		 * @param sourceIndex 从这里
		 */
		public void dataChange(Object dragInfo, int targetIndex, int sourceIndex);
		/**
		 * 功能简述:图标拖动到进入重叠位置，（功能表是生准备成临时文件夹情况）
		 * 功能详细描述:
		 * 注意:
		 * @return
		 */
		public boolean onEnterIconOverlay(int index, Object dragInfo);
		/**
		 * 功能简述:图标拖动离开重叠区域（功能表是隐藏临时文件夹情况）
		 * 功能详细描述:
		 * 注意:
		 */
		public void onExitIconOverlay();
		/**
		 * 在图标重叠的区域放手
		 * @param dragInfo
		 * @param targetIndex
		 * @param sourceIndex
		 * @param dragViewCenterX
		 * @param dragViewCenterY
		 * @return
		 */
		public boolean onDropInIconOverlay(Object dragInfo, int targetIndex, int sourceIndex, int invisitIndex, int dragViewCenterX, int dragViewCenterY, DragView dragView, DropAnimationInfo resetInfo);

		/**
		 * 从grid中获取需要做动画的view
		 * @param index 元素在列表中的位置
		 * @return
		 */
		public GLView getViewDoAnimation(int index);
	}

	@Override
	public void onStart(int what, Object[] params) {
//		mAnimationCount++;
	}

	@Override
	public void onFinish(int what, Object[] params) {
//		switch (what) {
//			case TRANSLATE_ANIMAITION :
//				if (mListener != null) {
//					int source = (Integer) params[0];
//					int target = (Integer) params[1];
//					mListener.switchAnimationEnd(mDragInfo, target, source);
//					mSourceBeforeTranslate = target;
//				}
//				break;
//				
//			case REMOVE_APP_ANIMATION : 
//				if (mListener != null) {
//					int source = (Integer) params[0];
//					int target = (Integer) params[1];
//					mListener.removeIconAnimationEnd(mDragInfo, target);
//					mSourceBeforeTranslate = target;
//				}
//				break;
//	
//			default:
//				break;
//		}
		mTime400 = DURATION_400;
//		mAnimationCount--;
//		if (mAnimationCount == 0) {
			mIsAnimationing = false;
//			mAnimationStartTime = 0;
//		}
	}
	
	private void exitIconOverlay() {
		if (mIsEnterOverlay) {
			if (mListener != null) {
				mListener.onExitIconOverlay();
				mReadyFolderIndex = -1;
			}
			mIsEnterOverlay = false;
		}
	}
	
	public void postJobToQueue(Job job) {
		JobManager.postJob(job);
	}
	
	private void resetOperation() {
		mDragSourceIndex = 0; //最开始拖拽图标的下标
		mDragTargetIndex = 0; //最后放下图标的下标
		mInvisitIndex = 0; //当前发生动画后看不见图标的位置下标
		mSourceBeforeTranslate = 0; //当前发生动画前看不见图标的下标
		mCurrentScreenFirstIndex = -1; //当前屏第一个下标
		mCurrentScreenLastIndex = -1; //当前屏最后一个下标
		mDragInfo = null;
		mDragView = null;

		mTime400 = DURATION_400;
		
		mIsFling = false; //是否是快速拖拽的标志位
		mIsAnimationing = false; //是否正在做动画的标志位
		mIsEnterOverlay = false; //是否进入图标重叠区域
		mIsEnableOverlay = true; //是否开启重叠区域判断
	}
	
}
