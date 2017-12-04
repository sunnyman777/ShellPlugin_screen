package com.jiubang.shell.common.component;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.proxy.GoLauncherActivityProxy;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.theme.GLAppDrawerThemeControler;
import com.jiubang.shell.utils.GLImageUtil;

/**
 * 
 * @author yangguanxiang
 *
 */
public abstract class GLLinearPanel extends GLFrameLayout {

	/**
	 * 普通
	 */
	public final static int TYPE_SIMPLE = 0;
	/**
	 * 除左右两边最外组件处于最边缘，内面的组件平均分配空间
	 */
	public final static int TYPE_AVERAGE_INSIDE = 1;
	/**
	 * 所有组件平均分配空间
	 */
	public final static int TYPE_AVERAGE_ALL = 2;
	/**
	 * 靠左排列
	 */
	public final static int LAYOUT_GRAVITY_LEFT = 0;
	/**
	 * 靠右排列
	 */
	public final static int LAYOUT_GRAVITY_RIGHT = 1;
	/**
	 * 靠左排列向右拉伸，且actionBar类型必须是TYPE_SIMPLE
	 */
	public final static int LAYOUT_GRAVITY_LEFT_STRETCH = 2;
	/**
	 * 靠右排列向右拉伸，且actionBar类型必须是TYPE_SIMPLE
	 */
	public final static int LAYOUT_GRAVITY_RIGHT_STRETCH = 3;

	private int mType = TYPE_SIMPLE;
	private boolean mShowDivider;
	protected GLDrawable mDividerV;
	protected GLDrawable mDividerH;
	protected int mDividerSpace;
	protected Paint mDividerPaint = new Paint();
	private GLView mStretchComponent;
	private ArrayList<GLView> mLeftComponentList = new ArrayList<GLView>();
	private ArrayList<GLView> mRightComponentList = new ArrayList<GLView>();
	private ArrayList<GLView> mComponentList = new ArrayList<GLView>();
	private int mItemPadding;

	protected AppFuncUtils mUtils;
	protected GLAppDrawerThemeControler mThemeCtrl;

	protected boolean mIsVerticalMode;

	public GLLinearPanel(Context context) {
		super(context);
		init();
	}

	public GLLinearPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mIsVerticalMode = GoLauncherActivityProxy.isPortait();
		mUtils = AppFuncUtils.getInstance(ShellAdmin.sShellManager.getActivity());
		mThemeCtrl = GLAppDrawerThemeControler.getInstance(mContext);
		loadResource();
	}

	public void resetResource() {
		mDividerSpace = 0;
		mDividerV = null;
		mDividerH = null;
	}

	public void loadResource() {
		mDividerSpace = mUtils.getStandardSize(2);
		mDividerV = GLImageUtil.getGLDrawable(R.drawable.gl_appdrawer_icon_line);
		mDividerH = GLImageUtil.getGLDrawable(R.drawable.gl_appdrawer_icon_line_h);
	}

	public synchronized void addComponent(GLView view) throws IllegalArgumentException {
		addComponent(view, LAYOUT_GRAVITY_LEFT);
	}

	public synchronized void addComponent(GLView view, int layoutGravity)
			throws IllegalArgumentException {
		if (this.indexOfChild(view) < 0) {
			if (layoutGravity == LAYOUT_GRAVITY_LEFT_STRETCH
					|| layoutGravity == LAYOUT_GRAVITY_RIGHT_STRETCH) {
				if (mStretchComponent != null) {
					throw new IllegalArgumentException(
							"Only one stretch component can be in action bar");
				} else {
					mStretchComponent = view;
					super.addView(view);
					if (layoutGravity == LAYOUT_GRAVITY_LEFT_STRETCH) {
						mLeftComponentList.add(view);
					} else if (layoutGravity == LAYOUT_GRAVITY_RIGHT_STRETCH) {
						mRightComponentList.add(view);
					}
					return;
				}
			}
			super.addView(view);
			if (layoutGravity == LAYOUT_GRAVITY_LEFT) {
				mLeftComponentList.add(view);
			} else if (layoutGravity == LAYOUT_GRAVITY_RIGHT) {
				mRightComponentList.add(view);
			}
		}
	}

	public synchronized void removeComponent(GLView view) {
		super.removeView(view);
		if (mLeftComponentList.contains(view)) {
			mLeftComponentList.remove(view);
		} else if (mRightComponentList.contains(view)) {
			mRightComponentList.remove(view);
		}
		if (mStretchComponent == view) {
			mStretchComponent = null;
		}
	}

	public synchronized void replaceComponent(GLView oldView, GLView newView) {
		if (mLeftComponentList.contains(oldView)) {
			mLeftComponentList.add(mLeftComponentList.indexOf(oldView), newView);
			mLeftComponentList.remove(oldView);
		} else if (mRightComponentList.contains(oldView)) {
			mRightComponentList.add(mLeftComponentList.indexOf(oldView), newView);
			mRightComponentList.remove(oldView);
		}
		if (mStretchComponent == oldView) {
			mStretchComponent = null;
		}
		super.removeView(oldView);
		super.addView(newView);
		requestLayout();
	}

	public synchronized void removeAllComponents() {
		super.removeAllViews();
		mLeftComponentList.clear();
		mRightComponentList.clear();
		mComponentList.clear();
		mStretchComponent = null;
		mItemPadding = 0;
	}

	public boolean isStretch(GLView view) {
		return mStretchComponent == view;
	}

	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		setVerticalMode(GoLauncherActivityProxy.isPortait());
		mItemPadding = 0;
		mComponentList.clear();
		switch (mType) {
			case TYPE_SIMPLE :
				layoutSimpleType();
				break;
			case TYPE_AVERAGE_INSIDE :
			case TYPE_AVERAGE_ALL :
				layoutAverageType();
				break;
			default :
				break;
		}
	}

	private int computeStretchComponentSpace() {
		int dividerSpace = 0;
		if (mShowDivider && mDividerV != null) {
			dividerSpace = mDividerSpace;
		}
		int space = 0;
		if (mIsVerticalMode) {
			space = mWidth - mPaddingLeft - mPaddingRight;
			for (int i = 0; i < mLeftComponentList.size(); i++) {
				GLView component = mLeftComponentList.get(i);
				int width = component.getWidth();
				if (component != mStretchComponent) {
					space -= width;
				}
			}
			for (int i = 0; i < mRightComponentList.size(); i++) {
				GLView component = mRightComponentList.get(i);
				int width = component.getWidth();
				if (component != mStretchComponent) {
					space -= width;
				}
			}
		} else {
			space = mHeight - mPaddingTop - mPaddingBottom;
			for (int i = 0; i < mLeftComponentList.size(); i++) {
				GLView component = mLeftComponentList.get(i);
				int height = component.getHeight();
				if (component != mStretchComponent) {
					space -= height;
				}
			}
			for (int i = 0; i < mRightComponentList.size(); i++) {
				GLView component = mRightComponentList.get(i);
				int height = component.getHeight();
				if (component != mStretchComponent) {
					space -= height;
				}
			}
		}
		return space -= dividerSpace * (mLeftComponentList.size() + mRightComponentList.size() - 1);
	}

	private void layoutSimpleType() {
		int dividerSpace = 0;
		if (mIsVerticalMode) {
			if (mShowDivider && mDividerV != null) {
				dividerSpace = mDividerSpace;
			}
			int startLeft = mPaddingLeft;
			for (int i = 0; i < mLeftComponentList.size(); i++) {
				GLView component = mLeftComponentList.get(i);
				int width = 0;
				int height = component.getHeight() == 0 ? mHeight : component.getHeight();
				if (component != mStretchComponent) {
					width = component.getWidth();
				} else {
					width = computeStretchComponentSpace();
				}
				int top = computeTop(component);
				component.layout(startLeft, top, startLeft + width, top + height);
				startLeft += width + dividerSpace;
			}
			int startRight = mWidth - mPaddingRight;
			for (int i = 0; i < mRightComponentList.size(); i++) {
				GLView component = mRightComponentList.get(i);
				int width = 0;
				int height = component.getHeight() == 0 ? mHeight : component.getHeight();
				if (component != mStretchComponent) {
					width = component.getWidth();
				} else {
					width = computeStretchComponentSpace();
				}
				int top = computeTop(component);
				component.layout(startRight - width, top, startRight, top + height);
				startRight -= width + dividerSpace;
			}
		} else {
			if (mShowDivider && mDividerH != null) {
				dividerSpace = mDividerSpace;
			}
			int startTop = mPaddingTop;
			for (int i = 0; i < mRightComponentList.size(); i++) {
				GLView component = mRightComponentList.get(i);
				int height = 0;
				int width = component.getWidth() == 0 ? mWidth : component.getWidth();
				if (component != mStretchComponent) {
					height = component.getHeight();
				} else {
					height = computeStretchComponentSpace();
				}
				int left = computeLeft(component);
				component.layout(left, startTop, left + width, startTop + height);
				startTop += height + dividerSpace;
			}
			int startBottom = mHeight - mPaddingBottom;
			for (int i = 0; i < mLeftComponentList.size(); i++) {
				GLView component = mLeftComponentList.get(i);
				int height = 0;
				int width = component.getWidth() == 0 ? mWidth : component.getWidth();
				if (component != mStretchComponent) {
					height = component.getHeight();
				} else {
					height = computeStretchComponentSpace();
				}
				int left = computeLeft(component);
				component.layout(left, startBottom - height, left + width, startBottom);
				startBottom -= height + dividerSpace;
			}
		}
	}

	private int computeLeft(GLView component) {
		int left = Math.round(mPaddingLeft
				+ (mWidth - mPaddingLeft - mPaddingRight - component.getWidth()) / 2.0f);
		return left;
	}

	private int computeTop(GLView component) {
		int top = Math.round(mPaddingTop
				+ (mHeight - mPaddingTop - mPaddingBottom - component.getHeight()) / 2.0f);
		return top;
	}

	private void layoutAverageType() {
		mComponentList = new ArrayList<GLView>();
		mComponentList.addAll(mLeftComponentList);
		mComponentList.addAll(mRightComponentList);
		int dividerSpace = 0;
		int size = mComponentList.size();
		if (mIsVerticalMode) {
			if (mShowDivider && mDividerV != null) {
				dividerSpace = mDividerSpace;
			}
			int displayAreaSize = mWidth - mPaddingLeft - mPaddingRight;
			int spaceSize = displayAreaSize;
			for (int i = 0; i < size; i++) {
				GLView component = mComponentList.get(i);
				spaceSize -= component.getWidth();
			}
			spaceSize -= dividerSpace * (size - 1);
			if (mType == TYPE_AVERAGE_INSIDE) {
				int avg = size - 1;
				if (avg < 1) {
					avg = 1;
				}
				mItemPadding = spaceSize / avg;
			} else {
				mItemPadding = spaceSize / (size + 1);
			}
			int startLeft = mPaddingLeft;
			if (mType == TYPE_AVERAGE_ALL) {
				startLeft += mItemPadding;
			}
			int startRight = mWidth - mPaddingRight;
			if (mType == TYPE_AVERAGE_ALL) {
				startRight -= mItemPadding;
			}
			for (int i = 0; i < size; i++) {
				GLView component = mComponentList.get(i);
				int width = component.getWidth();
				int height = component.getHeight() == 0 ? mHeight : component.getHeight();
				int top = computeTop(component);
				if (mLeftComponentList.contains(component)) {
					//					component.setXY(startLeft, top);
					component.layout(startLeft, top, startLeft + width, top + height);
					startLeft += width + mItemPadding + dividerSpace;
				} else if (mRightComponentList.contains(component)) {
					//					component.setXY(startRight - width, top);
					component.layout(startRight - width, top, startRight, top + height);
					startRight -= width + mItemPadding + dividerSpace;
				}
				//				layoutStretchComponent(component);
			}
		} else {
			if (mShowDivider && mDividerH != null) {
				dividerSpace = mDividerSpace;
			}
			int displayAreaSize = mHeight - mPaddingTop - mPaddingBottom;
			int spaceSize = displayAreaSize;
			for (int i = 0; i < size; i++) {
				GLView component = mComponentList.get(i);
				spaceSize -= component.getHeight();
			}
			if (mType == TYPE_AVERAGE_INSIDE) {
				int avg = size - 1;
				if (avg < 1) {
					avg = 1;
				}
				mItemPadding = spaceSize / avg;
			} else {
				mItemPadding = spaceSize / (size + 1);
			}
			int startTop = mPaddingTop;
			if (mType == TYPE_AVERAGE_ALL) {
				startTop += mItemPadding;
			}
			int startBottom = mHeight - mPaddingBottom;
			if (mType == TYPE_AVERAGE_ALL) {
				startBottom -= mItemPadding;
			}
			for (int i = 0; i < size; i++) {
				GLView component = mComponentList.get(i);
				int width = component.getWidth() == 0 ? mWidth : component.getWidth();
				int height = component.getHeight();
				int left = computeLeft(component);
				if (mRightComponentList.contains(component)) {
					//					component.setXY(left, startTop);
					component.layout(left, startTop, left + width, startTop + height);
					startTop += height + mItemPadding + dividerSpace;
				} else if (mLeftComponentList.contains(component)) {
					//					component.setXY(left, startBottom - height);
					component.layout(left, startBottom - height, left + width, startBottom);
					startBottom -= height + mItemPadding + dividerSpace;
				}
				//				layoutStretchComponent(component);
			}
		}
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		super.dispatchDraw(canvas);
		if (mShowDivider) {
			if (mIsVerticalMode && mDividerV != null) {
				if (mType == TYPE_SIMPLE) {
					for (int i = 0; i < mLeftComponentList.size(); i++) {
						GLView component = mLeftComponentList.get(i);
						int pos = component.getLeft() + component.getWidth();
						if (i == mLeftComponentList.size() - 1) {
							if (component == mStretchComponent && !mRightComponentList.isEmpty()) {
								GLImageUtil.drawImage(canvas, mDividerV, GLImageUtil.CENTERMODE,
										pos, mPaddingTop, pos + mDividerSpace, mHeight
												- mPaddingBottom, mDividerPaint);
							}
						} else {
							GLImageUtil.drawImage(canvas, mDividerV, GLImageUtil.CENTERMODE, pos,
									mPaddingTop, pos + mDividerSpace, mHeight - mPaddingBottom,
									mDividerPaint);
						}
					}
					for (int i = 0; i < mRightComponentList.size() - 1; i++) {
						GLView component = mRightComponentList.get(i);
						int pos = component.getLeft() - mDividerSpace;
						if (i == mRightComponentList.size() - 1) {
							if (component == mStretchComponent && !mLeftComponentList.isEmpty()) {
								GLImageUtil.drawImage(canvas, mDividerV, GLImageUtil.CENTERMODE,
										pos, mPaddingTop, pos + mDividerSpace, mHeight
												- mPaddingBottom, mDividerPaint);
							}
						} else {
							GLImageUtil.drawImage(canvas, mDividerV, GLImageUtil.CENTERMODE, pos,
									mPaddingTop, pos + mDividerSpace, mHeight - mPaddingBottom,
									mDividerPaint);
						}
					}
				} else if (mType == TYPE_AVERAGE_INSIDE || mType == TYPE_AVERAGE_ALL) {
					int size = mComponentList.size();
					for (int i = 1; i < size; i++) {
						GLView component = mComponentList.get(i);
						int pos = component.getLeft() - mItemPadding / 2 - mDividerSpace;
						GLImageUtil.drawImage(canvas, mDividerV, GLImageUtil.CENTERMODE, pos,
								mPaddingTop, pos + mDividerSpace, mHeight - mPaddingBottom,
								mDividerPaint);
					}
				}
			} else if (!mIsVerticalMode && mDividerH != null) {
				if (mType == TYPE_SIMPLE) {
					for (int i = 0; i < mRightComponentList.size(); i++) {
						GLView component = mRightComponentList.get(i);
						int pos = component.getTop() + component.getHeight();
						if (i == mRightComponentList.size() - 1) {
							if (component == mStretchComponent && !mLeftComponentList.isEmpty()) {
								GLImageUtil.drawImage(canvas, mDividerH, GLImageUtil.CENTERMODE,
										mPaddingLeft, pos, mWidth - mPaddingRight, pos
												+ mDividerSpace, mDividerPaint);
							}
						} else {
							GLImageUtil.drawImage(canvas, mDividerH, GLImageUtil.CENTERMODE,
									mPaddingLeft, pos, mWidth - mPaddingRight, pos + mDividerSpace,
									mDividerPaint);
						}
					}
					for (int i = 0; i < mLeftComponentList.size(); i++) {
						GLView component = mLeftComponentList.get(i);
						int pos = component.getTop() - mDividerSpace;
						if (i == mLeftComponentList.size() - 1) {
							if (component == mStretchComponent && !mRightComponentList.isEmpty()) {
								GLImageUtil.drawImage(canvas, mDividerH, GLImageUtil.CENTERMODE,
										mPaddingLeft, pos, mWidth - mPaddingRight, pos
												+ mDividerSpace, mDividerPaint);
							}
						} else {
							GLImageUtil.drawImage(canvas, mDividerH, GLImageUtil.CENTERMODE,
									mPaddingLeft, pos, mWidth - mPaddingRight, pos + mDividerSpace,
									mDividerPaint);
						}
					}

				} else if (mType == TYPE_AVERAGE_INSIDE || mType == TYPE_AVERAGE_ALL) {
					int size = mComponentList.size();
					for (int i = 1; i < size; i++) {
						GLView component = mComponentList.get(i);
						int pos = component.getTop() + component.getHeight() + mItemPadding / 2;
						GLImageUtil.drawImage(canvas, mDividerH, GLImageUtil.CENTERMODE,
								mPaddingLeft, pos, mWidth - mPaddingRight, pos + mDividerSpace,
								mDividerPaint);
					}
				}
			}
		}
	}

	public void setType(int type) {
		mType = type;
	}

	public void showDivider(boolean show) {
		mShowDivider = show;
	}

	public void setVerticalMode(boolean isVertical) {
		mIsVerticalMode = isVertical;
	}

	public boolean isVerticalMode() {
		return mIsVerticalMode;
	}
}
