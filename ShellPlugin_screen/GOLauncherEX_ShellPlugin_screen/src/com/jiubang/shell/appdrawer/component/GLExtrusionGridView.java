package com.jiubang.shell.appdrawer.component;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

import com.go.gl.animation.Animation;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.animation.TranslateValueAnimation;
import com.jiubang.shell.appdrawer.controler.IconViewOperation;
import com.jiubang.shell.appdrawer.controler.IconViewOperation.OnOperationIconViewListener;
import com.jiubang.shell.common.component.GLScrollableBaseGrid;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.drag.DragController.DragListener;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.drag.DropTarget;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * <br>
 * 类描述:可以挤压的gridview <br>
 * 功能详细描述:
 * 
 * @author wuziyi
 */
public abstract class GLExtrusionGridView extends GLScrollableBaseGrid implements OnOperationIconViewListener, DragSource, DropTarget, DragListener {

	protected static final int QUICK_LONG_PRESS_TIMEOUT = 150;
	protected boolean mInitExtrusonParams;
	private Rect mTouchRect = new Rect();
	protected IconViewOperation mIconOperation; // 动作类

	protected boolean mNeedRegetIconRectList = true;
	protected boolean mIsInScrollZone;
	protected GridViewHandler mGridViewHandler;
	protected final static int LAYOUT_PART_PAGE = 0;
	protected final static int SWITCH_DATA_POSITION = 1;
	protected final static int REMOVE_TEMP_GLVIEW = 2;
	protected final static int END = 3;
	protected GLView mTempView;
	
	public GLExtrusionGridView(Context context) {
		super(context);
		init();
	}

	public GLExtrusionGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public GLExtrusionGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mIconOperation = new IconViewOperation(mTouchRect, getContext());
		mIconOperation.setOperationListener(this);
		mGridViewHandler = new GridViewHandler();
		mTempView = new GLView(mContext);
		mTempView.setVisible(false);
		mTempView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}
	
	protected void generateRectList() {
		int firstIndex = getCurrentScreenFirstIndex();
		int lastIndex = getCurrentScreenLastIndex();
		ArrayList<Rect> outerRectList = new ArrayList<Rect>();
		ArrayList<Rect> innerRectList = new ArrayList<Rect>();
		for (int i = firstIndex; i <= lastIndex; i++) {
			GLView tempView = getChildAt(i);
			if (tempView instanceof IconView) {
				IconView<?> icon = (IconView<?>) tempView;
				outerRectList.add(icon.getOperationArea(null)[0]);
				innerRectList.add(icon.getOperationArea(null)[1]);
			} else {
				outerRectList.add(new Rect(0, 0, 0, 0));
				innerRectList.add(new Rect(0, 0, 0, 0));
			}
		}
		mIconOperation.initParameter(outerRectList, innerRectList, mIsVerScroll, getPageItemCount());
		mIconOperation.setScreenFirstAndLastIndex(firstIndex, lastIndex);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mNeedRegetIconRectList = true;
	}

	@Override
	protected void onScrollStart() {
		
	}
	
	@Override
	protected void onScrollFinish() {
//		Log.i("wuziyi", "onScrollFinish");
		generateRectList();
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
//		Log.i("wuziyi", "GLExtrusionGridView:onLayout:" + changed);
		if (getChildCount() == 0) {
			return;
		}
		if (mNeedRegetIconRectList) {
			int[] location = new int[2];
			ShellAdmin.sShellManager.getShell().getContainer().getLocation(this, location);
			mTouchRect.set(location[0], location[1] + getPaddingTop(), getWidth() + location[0], getHeight() + location[1]);
			int pageCount = getPageItemCount();
			int firstIndex = getCurrentScreenFirstIndex();
			int lastIndex = getCurrentScreenLastIndex();
			ArrayList<Rect> outerRectList = new ArrayList<Rect>();
			ArrayList<Rect> innerRectList = new ArrayList<Rect>();
			
			GLView tempView = getChildAt(firstIndex);
			if (tempView != null) {
				IconView<?> icon = (IconView<?>) tempView;
				Rect outerArea = icon.getOperationArea(null)[0];
				Rect innerArea = icon.getOperationArea(null)[1];
				int outerWidth = outerArea.width();
				int outerHeight = outerArea.height();
				int innerWidth = innerArea.width();
				int innerHeight = innerArea.height();
				int outerOriginalLeft = outerArea.left;
				int innerOriginalLeft = innerArea.left;
				int outerX = outerArea.left;
				int outerY = outerArea.top;
				int innerX = innerArea.left;
				int innerY = innerArea.top;
				for (int i = 0; i < pageCount; i++) {
					outerRectList.add(new Rect(outerX, outerY, outerX + outerWidth, outerY + outerHeight));
					innerRectList.add(new Rect(innerX, innerY, innerX + innerWidth, innerY + innerHeight));
					if ((i + 1) % mNumColumns == 0) {
						outerX = outerOriginalLeft;
						outerY = outerY + outerHeight;
						innerX = innerOriginalLeft;
						innerY = innerY + outerHeight;
					} else {
						outerX = outerX + outerWidth;
						innerX = innerX + outerWidth;
					}
				}
				mIconOperation.initParameter(outerRectList, innerRectList, mNeedRegetIconRectList, getPageItemCount());
				mIconOperation.setScreenFirstAndLastIndex(firstIndex, lastIndex);
				mNeedRegetIconRectList = false;
			}
			
//			for (int i = firstIndex; i <= lastIndex; i++) {
//				GLView tempView = getChildAt(i);
//				if (tempView instanceof IconView) {
//					IconView<?> icon = (IconView<?>) tempView;
//					outerRectList.add(icon.getOperationArea(null)[0]);
//					innerRectList.add(icon.getOperationArea(null)[1]);
//				} else {
//					outerRectList.add(new Rect(0, 0, 0, 0));
//					innerRectList.add(new Rect(0, 0, 0, 0));
//				}
//			}
		}
	}

	@Override
	public boolean onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo,
			DropAnimationInfo resetInfo) {
		mIconOperation.doDrop(x, y, xOffset, yOffset, dragView,	dragInfo, resetInfo);
		return true;
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		mIconOperation.doDragEnter(source, x, y, xOffset, yOffset, dragView, dragInfo);
		ShellAdmin.sShellManager.getShell().getDragController().setDragScroller(mScrollableHandler);
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		if (mIsInScrollZone) {
			mIconOperation.doEnterScrollZone();
			return;
		}
		mIconOperation.doDragOver(x, y, xOffset, yOffset, dragView,	dragInfo);
	}

	@Override
	public void onDragExit(DragSource source, DropTarget nextTarget, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		mIconOperation.doDragExit(x, y, xOffset, yOffset, dragView, dragInfo);
	}

	@Override
	public void onDragMove(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		if (mIsInScrollZone) {
			return;
		}
//		mIconOperation.doDragMove(x, y, xOffset, yOffset);
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		return true;
	}

	@Override
	public Rect estimateDropLocation(DragSource source, int x, int y,
			int xOffset, int yOffset, DragView dragView, Object dragInfo,
			Rect recycle) {
		return null;
	}

	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		Log.i("wuziyi", "onDragStart");
		if (source == this) {
			generateRectList();
		}
		mIconOperation.doDragStart();
	}

	@Override
	public void onDropCompleted(DropTarget target, Object dragInfo,
			boolean success, DropAnimationInfo resetInfo) {
		mIconOperation.doDropCompleted(target, dragInfo, success, resetInfo);
	}
	
	@Override
	public void callBackToChild(GLView view) {
		Animation animation = view.getAnimation();
		if (animation instanceof TranslateValueAnimation) {
			animation.cancel();
			view.clearAnimation();
		}
	}

	@Override
	public void switchAnimationStart(Object dragInfo, int targetIndex,
			int sourceIndex) {
		dataChangeOnMoveStart(dragInfo, targetIndex, sourceIndex);
//		Message msg = mGridViewHandler.obtainMessage(SWITCH_DATA_POSITION);
//		msg.arg1 = targetIndex;
//		msg.arg2 = sourceIndex;
//		msg.obj = dragInfo;
//		mGridViewHandler.sendMessage(msg);
	}
	
	@Override
	public void switchAnimationEnd(Object dragInfo, final int targetIndex, final int sourceIndex) {
		dataChangeOnMoveEnd(dragInfo, targetIndex, sourceIndex);
		layoutPartPage(sourceIndex, targetIndex);
//		generateRectList();
//		Message msg = mGridViewHandler.obtainMessage(LAYOUT_PART_PAGE);
//		msg.arg1 = targetIndex;
//		msg.arg2 = sourceIndex;
//		msg.obj = dragInfo;
//		mGridViewHandler.sendMessage(msg);
	}

	@Override
	public void dataChange(Object dragInfo, int targetIndex, int sourceIndex) {
		dataChangeOnDrop(dragInfo, targetIndex, sourceIndex);
	}
	
	@Override
	public boolean onEnterIconOverlay(int index, Object dragInfo) {
		return false;
	}
	
	@Override
	public void onExitIconOverlay() {
		
	}
	
	@Override
	public boolean onDropInIconOverlay(Object dragInfo, int targetIndex, int sourceIndex, int invisitIndex, int dragViewCenterX, int dragViewCenterY, DragView dragView, DropAnimationInfo resetInfo) {
		dataChangeOnDrop(dragInfo, invisitIndex, sourceIndex);
		return true;
	}
	
	@Override
	public GLView getViewDoAnimation(int index) {
		return getChildAt(index);
	}
	
	protected int getTargetRowLastIndex(int targetRow) {
		int targetRowLastIndex = (targetRow + 1) * mNumColumns - 1;
		int allCount = getChildCount();
		if (targetRowLastIndex >= allCount) {
			targetRowLastIndex = allCount - 1;
		}
		return targetRowLastIndex;
	}

	protected int getTargetRowFirstIndex(int targetRow) {
		int firstIndex = targetRow * mNumColumns;
		return firstIndex;
	}

	@Override
	public boolean onItemLongClick(GLAdapterView<?> parent, GLView view,
			int position, long id) {
		// 位置赋值
		mIconOperation.setDragSourceAndTargetIndex(position, position);

		return true;
	}
	
	/**
	 * 启动挤压动画(暂时只供进入／创建文件夹使用)
	 * @param targetIndex 目标位置
	 */
	public void removeIconExtrusion(int fromIndex) {
		int invisitIndex = mIconOperation.getSourceBeforeTranslate();
		GLView view = getChildAt(invisitIndex);
		if (view != null) {
			mScrollableHandler.setViewInHolder(invisitIndex, mTempView);
			removeViewsInLayout(invisitIndex, 1);
			addViewInLayout(mTempView, invisitIndex, mTempView.getLayoutParams(), false);
			mIconOperation.removeIconExtrusion(fromIndex, getChildCount() - 1);
		} else {
//			throw new IllegalArgumentException("Get from operation index is:" + invisitIndex + " current total view is:" + getChildCount() + " current viewholder size:" + mScrollableHandler.getChildCount() + "currrent Row:" + mNumRows + " current Columns" + mNumColumns);
			/* 根据上面的抛错收集的信息推测，执行到这里时，有可能中途有layout行为提前发生，因为index与view数量相同
			 * 方案一：给layout上个同步锁，风险较大，但较安全
			 * 方案二：如下实现，直接判定为结束，但弊端是有可能index不对
			*/
			if (invisitIndex == getChildCount()) {
				removeIconAnimationEnd(mIconOperation.getDragInfo(), fromIndex);
			}
		}
		
	}

	@Override
	public void onDragEnd() {
		mIconOperation.doDragEnd();
	}
	
	@Override
	public void removeIconAnimationEnd(Object dragInfo, int removeIndex) {
//		requestLayout();
	}
	
	public abstract void dataChangeOnMoveStart(Object dragInfo, final int targetIndex, final int sourceIndex);
	
	public abstract void dataChangeOnMoveEnd(Object dragInfo, final int targetIndex, final int sourceIndex);
	
	public abstract void dataChangeOnDrop(Object dragInfo, final int targetIndex, final int sourceIndex);
	
	/**
	 * 主要用于某些需要放到UI上执行的方法，还有是延时执行的方法
	 * @author wuziyi
	 *
	 */
	protected class GridViewHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
//				case LAYOUT_PART_PAGE :
//					dataChangeOnMoveEnd(msg.obj, msg.arg1, msg.arg2);
//					layoutPartPage(msg.arg2, msg.arg1);
//					generateRectList();
//					break;
//				case SWITCH_DATA_POSITION :
//					dataChangeOnMoveStart(msg.obj, msg.arg1, msg.arg2);
//					break;
				case REMOVE_TEMP_GLVIEW : 
					removeViewInLayout(mTempView);
					break;
				default:
					break;
			}
		}
	}
	
}
