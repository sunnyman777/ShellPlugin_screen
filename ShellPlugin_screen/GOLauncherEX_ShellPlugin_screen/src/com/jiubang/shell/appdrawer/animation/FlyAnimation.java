package com.jiubang.shell.appdrawer.animation;

import java.util.ArrayList;

import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.view.GLView;

/**
 * 
 * <br>类描述:图标飞行动画（聚合，散开）
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-05-30]
 */
public class FlyAnimation extends AppdrawerAnimation {

	public static final float FROMSCALE = 1.0f;
	public static final float TOSCALE = 0.7f;

	private float[][] mFrom; // 动画里，图标要移动到的位置
	private float mIconHeight = 0; 
	private float mIconWidth = 0;
//	private static FlyAnimation sInstance;
	
	private int mRowCount = 0;
	private int mColCount = 0;
	
	private float mScreenWidth;
	private float mScreenHeight;
	
	private int mAppCount;

//	public static FlyAnimation getInstance() {
//		if (sInstance == null) {
//			sInstance = new FlyAnimation();
//		}
//		return sInstance;
//	}

//	private void flyAnimation(final Workspace workspace, final boolean isClose, final GLView view,
//			final float fromX, final float toX, final float fromY, final float toY,
//			final boolean isLast, final boolean blur, final boolean forceDrawBg) {
//		float fromScale = FROMSCALE;
//		float toScale = TOSCALE;
//		final float factor = 1.5f;
//
//		if (view instanceof GLWidgetView) {
//			toScale = 0;
//		}
//
//		view.setHasPixelOverlayed(false);
//		ScaleAnimation scaleAnimation = null;
//		TranslateAnimation tranAnim = null;
//		Animation alphaAnimation = null;
//		AnimationSet set = new AnimationSet(false);
//		if (isClose) {
//			scaleAnimation = new ScaleAnimation(fromScale, toScale, fromScale, toScale,
//					Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0);
//			tranAnim = new TranslateAnimation(0f, toX - fromX, 0f, toY - fromY);
//			tranAnim.setInterpolator(new AccelerateInterpolator(factor));
//			alphaAnimation = new AlphaAnimation(1, 0);
//		} else {
//			scaleAnimation = new ScaleAnimation(toScale, fromScale, toScale, fromScale,
//					Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0);
//			tranAnim = new TranslateAnimation(toX - fromX, 0f, toY - fromY, 0f);
//			tranAnim.setInterpolator(new DecelerateInterpolator(factor));
//			alphaAnimation = new AlphaAnimation(0, 1);
//		}
//		set.addAnimation(scaleAnimation);
//		set.addAnimation(tranAnim);
//		set.addAnimation(alphaAnimation);
//		set.setFillAfter(true);
//		set.setDuration(DURATION);
//		set.setInterpolator(new LinearInterpolator());
//		scaleAnimation.setAnimationListener(new AnimationListenerAdapter() {
//			@Override
//			public void onAnimationEnd(Animation animation) {
//				view.clearAnimation();
//				if (isLast) {
////					animationEnd(workspace, isClose, blur, forceDrawBg);
//				}
//			}
//		});
//		view.startAnimation(set);
//	}

//	public void runWorkspaceAni(final boolean isClose, 
//			final Workspace workspace, final boolean blur, final boolean forceDrawBg) {
//
//		final int cellLayoutCount = workspace.getCurrentScreen();
//		if (workspace.getChildAt(cellLayoutCount) != null
//				&& workspace.getChildAt(cellLayoutCount) instanceof CellLayout) {
//			final CellLayout cellLayout = (CellLayout) workspace.getChildAt(cellLayoutCount);
//			final int childCount = cellLayout.getChildCount();
//			Animation fade = getFadeOver(isClose, workspace);
//			workspace.startAnimation(fade);
//			if (childCount > 0) {
//				int xy[] = new int[2];
//				int screenXY[] = new int[2];
//				boolean isLast = false;
//				screenXY[0] = workspace.getLeft();
//				screenXY[1] = workspace.getTop();
//				for (int i = 0; i < childCount; i++) {
//					if (cellLayout.getChildAt(i) instanceof GLView) {
//						GLView view = cellLayout.getChildAt(i);
//						view.getLocationInWindow(xy);
//						isLast = i == childCount - 1;
//						final int toX = (workspace.getWidth() - view.getWidth()) / 2;
//						flyAnimation(workspace, isClose, view, xy[0], toX, xy[1], screenXY[1]
//								- view.getHeight(), isLast, blur, forceDrawBg);
//					}
//				}
//			} else {
//				workspace.postDelayed(new Runnable() {
//					
//					@Override
//					public void run() {
//						animationEnd(workspace, isClose, blur, forceDrawBg);
//					}
//				}, DURATION);
//				
//			}
//		}
//	}

	public void startFlyAnimation(int width, int height, boolean isClose, ArrayList<GLView> list,
			AnimationListener animationListener, int row, int colum) {
		if (list == null || list.isEmpty()) {
			return;
		}
		mRowCount = row;
		mColCount = colum;
		mScreenWidth = width;
		mScreenHeight = height;
		int count = list.size();
		mIconHeight = list.get(0).getHeight();
		mIconWidth = list.get(0).getWidth();
		computCoordinate();
		int[] xy = new int[2];
		for (int i = 0; i < count && i < mFrom.length; i++) {
			//如果没有底部按钮，就在最后一个view动画调用animationEnd
			boolean isLast = i == count - 1;
			GLView view = list.get(i);
			view.getLocationInWindow(xy);
			flyAnimation(isClose, view, mFrom[i][0], xy[0], mFrom[i][1], xy[1], isLast,
					animationListener);
		}
	}
	
//	private void buttonsAniamtion(GLImageView[] buttons, GLAppDrawerMainView appdrawer, boolean isClose, boolean isLast) {
//		int[] xy = new int[2];
//		float screenY = appdrawer.getHeight();
//		float toY = screenY + mIconHeight * 1.5f;
//		buttons[0].getLocationInWindow(xy);
//		flyAnimation(appdrawer, isClose, buttons[0], mFrom[0][0], xy[0], toY, xy[1], false);
//		buttons[1].getLocationInWindow(xy);
//		flyAnimation(appdrawer, isClose, buttons[1], xy[0], xy[0], toY, xy[1], false);
//		buttons[2].getLocationInWindow(xy);
//		flyAnimation(appdrawer, isClose, buttons[2], mFrom[mColCount - 1][0], xy[0], toY, xy[1], isLast);
//	}

	private void flyAnimation(final boolean isClose, final GLView view, float fromX, float toX,
			float fromY, float toY, final boolean isLast, AnimationListener listener) {
		Animation animation = null;
		if (isClose) {
			animation = new TranslateAnimation(0, fromX - toX, 0, fromY - toY);
		} else {
			animation = new TranslateAnimation(fromX - toX, 0, fromY - toY, 0);
		}
		if (isLast) {
			animation.setAnimationListener(listener);
		}
		animation.setDuration(mDuration);
		view.startAnimation(animation);
	}
	
	private void computCoordinate() {
		mAppCount = mColCount * mRowCount;
		mFrom = new float[mAppCount][2];
		//第一行和最后一行特殊处理
		computFirstRow();
		computLastRowReverse();
		//第二行到倒数第二行
		float a = mScreenHeight / 2 - mIconHeight / 2;
		int halfCol = (mRowCount - 2) / 2;
		float spaceY; //每一行图标之间的Y坐标差，以「第二行的顶贴紧屏幕的顶，中间一行处于最中间」算出。
		if (halfCol == 0) {
			spaceY = a;
		} else {
			spaceY = a / halfCol;
		}
		 
		for (int i = 1; i < mRowCount - 1; i++) {
			computPerRowY(i, spaceY); //计算每一行的Y值
			computPerRowX(i); //计算每一行的X值
		}
	}
	
	
	private void computFirstRow() {
		int firstIndex = 0;
		int lastIndex = mColCount;
		for (int i = firstIndex; i < lastIndex; i++) {
			//第一行高度固定
			mFrom[i][1] = -mIconHeight;
		}
		computFirstRowX(firstIndex, lastIndex);
	}
	
	//第一行高度固定
	private void computLastRowReverse() {
		int firstIndex = mAppCount - mColCount;
		int lastIndex = mAppCount;
		for (int i = firstIndex; i < lastIndex; i++) {
			//最后一行高度固定
			mFrom[i][1] = mScreenHeight;
		}
		computFirstRowX(firstIndex, lastIndex);
	}
	
	private void computFirstRowX(int firstIndex, int lastIndex) {
		
		float startX = -mIconWidth * 2.5f;
		float a = mScreenWidth / 2 - mIconWidth / 2 - startX;
		int halfCol = mColCount / 2;
		float spaceX = a / halfCol; //每一个图标之间的X坐标差，以「第一个图标的left在startX处，中间一个图标在最中间」算出。
		
		for (int i = firstIndex; i < lastIndex; i++) {
			int index = i - firstIndex;
			mFrom[i][0] = startX + spaceX * index;
			//如果列数是偶数，中间一个图标位置空出
			if (index >= mColCount / 2 && mColCount % 2 == 0) {
				mFrom[i][0] += spaceX;
			}
		}
	}
	
	private void computPerRowY(int row, float spaceY) {
		int firstIndex = row * mColCount;
		int lastIndex = row * mColCount + mColCount;
		for (int i = firstIndex; i < lastIndex; i++) {
			mFrom[i][1] = (row - 1) * spaceY;
			//如果行数是偶数，中间一行图标位置空出
			if ((row >= mRowCount / 2 && mRowCount % 2 == 0) || mRowCount == 3) {
				mFrom[i][1] += spaceY;
			}
		}
	}
	
	private void computPerRowX(int row) {
		
		int firstIndex = row * mColCount;
		int lastIndex = row * mColCount + mColCount;
		int leftCount = 0;
		
		//中间行及其以上的行，左边图标分多一个；中间行以下的行，右边图标多分一个
		if (row < (mRowCount + 1) / 2 && mColCount % 2 != 0) {
			leftCount = mColCount / 2;
		} else {
			leftCount = mColCount / 2 - 1;
		}
		
		//　越靠近中间行，左右散开越远
		float midRow = ((float) mRowCount - 1) / 2;
		float offset = mIconWidth * 1.5f * (midRow - Math.abs(row - midRow));
		
		for (int i = firstIndex; i < lastIndex; i++) {
			int index = i - firstIndex;
			//左半部分的图标放在屏幕左边，右半部分的图标放在屏幕右边，左边的再减一个offset值，右边的再加一个offset值
			if (index <= leftCount) {
				mFrom[i][0] = -((leftCount - index) * 2 + 1.5f) * mIconWidth - offset;
			} else {
				mFrom[i][0] = -((leftCount - index) * 2 + 1.5f) * mIconWidth + mScreenWidth + offset;
			}
		}
	}
}
