package com.jiubang.shell.popupwindow.component.ggmenu;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.animation.AnticipateOvershootInterpolator;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLGridView;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;

/**
 * 自定义的菜单GridView
 * 
 * @author ouyongqiang
 * 
 */
public class GLGGMenuGridView extends GLGridView {
	private int mColumns = GGMenuData.GGMENU_MAX_COLOUMNS;

	private int mVerticalSpacing;

	// 分割线高度
	private int mLineHeight;

	private GLDrawable mLineDrawable;

	private Rect[] mItemLineRects;

	/**
	 * 构造函数
	 * 
	 * @param context
	 *            程序上下文
	 * @param attr
	 *            属性集
	 */
	public GLGGMenuGridView(Context context, AttributeSet attr) {
		super(context, attr);
		setVerticalFadingEdgeEnabled(false);
		Resources resources = getResources();
		mLineHeight = (int) resources.getDimension(R.dimen.menu_divline_height);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		GLGGMenuAdapter apdater = (GLGGMenuAdapter) getAdapter();

		int count = apdater.getCount();
		int perRowHeight = 0;
		try {
			perRowHeight = ((GLGGMenuIcon) apdater.getItem(0)).getMeasuredHeight();
		} catch (Exception e) {
		}

		int rows = (count + mColumns - 1) / mColumns;
		int height = rows * perRowHeight + getPaddingTop() + getPaddingBottom() + mVerticalSpacing
				* (rows - 1);
		setMeasuredDimension(getMeasuredWidth(), height);
	}

	@Override
	public void setNumColumns(int numColumns) {
		mColumns = numColumns;
		super.setNumColumns(numColumns);
	}

	@Override
	public void setVerticalSpacing(int verticalSpacing) {
		super.setVerticalSpacing(verticalSpacing);
		mVerticalSpacing = verticalSpacing;
	}

	/**
	 * 设置分割线Drawable
	 * 
	 * @param divLine
	 */
	public void setDivLineDrawable(GLDrawable divLine) {
		if (divLine == null) {
			return;
		}
		if (mLineDrawable != null) {
			mLineDrawable.clear();
			mLineDrawable = null;
		}
		mLineDrawable = divLine;
	}
	
//	@Override
//	protected void onLayout(boolean changed, int l, int t, int r, int b) {
//		super.onLayout(changed, l, t, r, b);
//		
//		// 这里进行每一项间隔线条位置计算，如果放在dispatchDraw来算，则横竖屏切换后线条位置有错
//		GLGGMenuAdapter adapter = (GLGGMenuAdapter) getAdapter();
//		int count = adapter.getCount();
//		if (count > 1) {
//			int count_tmp = count - 1;
//			int row = (count % mColumns != 0) ? (count / mColumns) : (count / mColumns - 1);
//			int rectcount = count_tmp - row;
//			mItemLineRects = new Rect[rectcount];
//			int lineWidth = (null != mLineDrawable) ? mLineDrawable.getIntrinsicWidth() : 0;
//			for (int i = 0; i < rectcount; i++) {
//				int lineColumns = mColumns - 1;
//				int childviewrow = (i % lineColumns != 0 || i == 0)
//						? (i / lineColumns)
//						: (i / lineColumns);
//				int childviewindex = i + childviewrow;
//				GLGGMenuIcon item_tmp = (GLGGMenuIcon) adapter.getItem(childviewindex);
//				int view_r = (null != item_tmp) ? item_tmp.getRight() : 0;
//				int view_t = (null != item_tmp) ? item_tmp.getTop() : 0;
//				int view_b = (null != item_tmp) ? item_tmp.getBottom() : 0;
//				int left = view_r - lineWidth / 2;
//				int top = (view_t + view_b) / 2 - mLineHeight / 2;
//				int right = left + lineWidth;
//				int bottom = top + mLineHeight;
//				Rect rect = new Rect(left, top, right, bottom);
//				mItemLineRects[i] = rect;
//			}
//		}
//	}

//	@Override
//	protected void dispatchDraw(GLCanvas canvas) {
//		super.dispatchDraw(canvas);
//
//		GLGGMenuAdapter adapter = (GLGGMenuAdapter) getAdapter();
//		int count = adapter.getCount();
//		if (count > 1) {
//			if (null == mItemLineRects) {
//				int count_tmp = count - 1;
//				int row = (count % mColumns != 0) ? (count / mColumns) : (count / mColumns - 1);
//				int rectcount = count_tmp - row;
//				mItemLineRects = new Rect[rectcount];
//				int lineWidth = (null != mLineDrawable) ? mLineDrawable.getIntrinsicWidth() : 0;
//				for (int i = 0; i < rectcount; i++) {
//					int lineColumns = mColumns - 1;
//					int childviewrow = (i % lineColumns != 0 || i == 0)
//							? (i / lineColumns)
//							: (i / lineColumns);
//					int childviewindex = i + childviewrow;
//					GLGGMenuIcon item_tmp = (GLGGMenuIcon) adapter.getItem(childviewindex);
//					int view_r = (null != item_tmp) ? item_tmp.getRight() : 0;
//					int view_t = (null != item_tmp) ? item_tmp.getTop() : 0;
//					int view_b = (null != item_tmp) ? item_tmp.getBottom() : 0;
//					int l = view_r - lineWidth / 2;
//					int t = (view_t + view_b) / 2 - mLineHeight / 2;
//					int r = l + lineWidth;
//					int b = t + mLineHeight;
//					Rect rect = new Rect(l, t, r, b);
//					mItemLineRects[i] = rect;
//				}
//			}
//			int linecount = mItemLineRects.length;
//			if (mLineDrawable != null) {
//				for (int i = 0; i < linecount; i++) {
//					mLineDrawable.setBounds(mItemLineRects[i]);
//					mLineDrawable.draw(canvas);
//				}
//			}
//		}
//	}
	
	private boolean mAnimate = false;
	
	public void setAnimate(boolean flag) {
		mAnimate = flag;
	}
	
	/**
	 * 在item显示的时候，做一些动画特效
	 */
	@Override
	protected void layoutChildren() {
		// TODO Auto-generated method stub
		super.layoutChildren();
		int visibility = getVisibility();
		if (visibility == GLView.VISIBLE && mAnimate) {
			AnimationTask task = getItemInAnimationTask();
			if (task != null) {
				task.setBatchAnimationObserver(new BatchAnimationObserver() {

					@Override
					public void onStart(int what, Object[] params) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onFinish(int what, Object[] params) {
						// 检查菜单ITEM的未读消息
						GLGGMenuAdapter adapter = (GLGGMenuAdapter) getAdapter();
						if (adapter != null) {
							adapter.checkItemState();
						}
					}

				}, 0, 0);
			}
			GLAnimationManager.startAnimation(task);
			mAnimate = false;
		}
	}
	
	public AnimationTask getItemInAnimationTask() {
		int count = getChildCount();
		AnimationTask task = new AnimationTask(true, AnimationTask.PARALLEL);
		for (int i = 0; i < count; i++) {
			final GLView view = getChildAt(i);
			Animation animation = getChildAnimation(false, count, i);
			animation.setFillAfter(true);
			task.addAnimation(view, animation, null);
		}
		return task;
	}
	
	public AnimationTask getItemOutAnimationTask() {
		int count = getChildCount();
		AnimationTask task = new AnimationTask(true, AnimationTask.PARALLEL);
		for (int i = 0; i < count; i++) {
			final GLView view = getChildAt(i);
			Animation animation = getChildAnimation(true, count, i);
			animation.setFillAfter(true);
			task.addAnimation(view, animation, null);
		}
		return task;
	}
	
	private Animation getChildAnimation(boolean isOutAnimation, int count,
			int position) {
		int columns = GGMenuData.GGMENU_MAX_COLOUMNS;
		int rows = count / columns;
		// 动画步调的时间差
		int timeStep = 75;
		// 判断列数偶数或者奇数
		boolean isOddNum = false;
		if (columns % 2 != 0) {
			isOddNum = true;
		}
		// 基准数
		int datumNumber_1 = 0;
		int datumNumber_2 = 0;
		if (isOddNum) {
			// 是奇数
			datumNumber_1 = datumNumber_2 = columns / 2;
		} else {
			// 是偶数
			datumNumber_1 = columns / 2 - 1;
			datumNumber_2 = columns / 2;
		}

		Animation animation = isOutAnimation ? getOutAnimation()
				: getInAnimation();
		int remainder = position % columns;
		int timeOffset = isOutAnimation ? (rows - position / columns)
				* timeStep : (position / columns) * timeStep;
		// 徧离基准位置多少，计算时间差
		if (remainder <= datumNumber_1) {
			timeOffset += (datumNumber_1 - remainder) * timeStep;
		} else {
			timeOffset += (remainder - datumNumber_2) * timeStep;
		}
		animation.setDuration(isOutAnimation ? 200 : 425);
		animation.setStartOffset(timeOffset);
		return animation;
	}
	
	/**
	 * item隐藏时的动画
	 * @return
	 */
	private Animation getOutAnimation() {
		// 移动动画
		TranslateAnimation animation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
				TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
				TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
				TranslateAnimation.RELATIVE_TO_PARENT, 1.5f);
//		animation.setInterpolator(new AnticipateOvershootInterpolator());
//		animation.setInterpolator(new AccelerateInterpolator(4.0f));
		return animation;
	}
	
	/**
	 * item显示时的动画
	 * @return
	 */
	private Animation getInAnimation() {
		// 移动动画
		TranslateAnimation animation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
				TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
				TranslateAnimation.RELATIVE_TO_PARENT, 1.5f,
				TranslateAnimation.RELATIVE_TO_PARENT, 0.0f);
		// 渐变透明度动画
		animation.setInterpolator(new AnticipateOvershootInterpolator(1.5f));
		return animation;
	}
}
