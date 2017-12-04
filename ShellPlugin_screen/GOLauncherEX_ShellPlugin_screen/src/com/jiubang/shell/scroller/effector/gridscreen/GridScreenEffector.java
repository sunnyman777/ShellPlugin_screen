package com.jiubang.shell.scroller.effector.gridscreen;

import android.graphics.Rect;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.go.gl.graphics.GLCanvas;
import com.go.util.graphics.effector.united.IEffectorIds;
import com.jiubang.shell.scroller.ShellScreenScroller;
import com.jiubang.shell.scroller.ShellScreenScrollerEffector;
import com.jiubang.shell.scroller.ShellScreenScrollerListener;



/**
 * 只绘制当前两屏网格的特效类
 * @author dengweiming
 *
 */
public class GridScreenEffector implements ShellScreenScrollerEffector {
	/** 低质量绘图 */
	public final static int DRAW_QUALITY_LOW = 0;
	/** 中等质量绘图 */
	public final static int DRAW_QUALITY_MID = 1;
	/** 高等质量绘图 */
	public final static int DRAW_QUALITY_HIGH = 2;

	final static Interpolator DECELERATEINTERPOLATOR3 = new DecelerateInterpolator(1.5f);
	final static Interpolator DECELERATEINTERPOLATOR5 = new DecelerateInterpolator(2.5f);

	GridScreenContainer mContainer;
	ShellScreenScroller mScroller;
	int mOrientation;
	int mScreenSize;
	MGridScreenEffector[] mRandomEffectors;
	MGridScreenEffector mEffector;
	int mCurrentIndex;
	int mType;
	int mQuality;
	int mGap;
	int mTopPadding;
	boolean mVerticalSlide = false;
	int mBackgroundColor = 0x00000000;

	public GridScreenEffector(ShellScreenScroller scroller) {
		assert scroller != null; // 如果为null也就没任何意义了
		mScroller = scroller;
		mScroller.setEffector(this);
	}
	
	@Override
	public boolean onDraw(GLCanvas canvas) {
		if (mOrientation == ShellScreenScroller.VERTICAL) {
			return false; // 暂时不支持垂直滚屏特效
		}

		final int screenA = mScroller.getDrawingScreenA();
		final int screenB = mScroller.getDrawingScreenB();
		final int scroll = mScroller.getScroll() + mGap * 2;

		boolean bgDrawn = mScroller.isBackgroundAlwaysDrawn();
		boolean combinebg = mEffector != null && mEffector.isCombineBackground();
		if (!combinebg) {
			bgDrawn |= mScroller.drawBackground(canvas, mScroller.getScroll());
		}
		if (!bgDrawn) {
			if (mBackgroundColor != 0x00000000) {
				canvas.drawColor(mBackgroundColor);
			}
		}

		int offset = mScroller.getCurrentScreenOffset();
		if (offset > 0) {
			offset -= mScreenSize;
		}
		//		offset += mGap * 2;

		final int top = mTopPadding;

		if (mScroller.isFinished() || mType != IEffectorIds.EFFECTOR_TYPE_SNAKE && offset == 0 && mScroller.getCurrentDepth() == 0) {
			drawScreenBg(mContainer, canvas, screenA - 1, offset
					- mScreenSize, top, scroll);
			drawScreenBg(mContainer, canvas, screenA, offset, top,
					scroll);
			drawScreenBg(mContainer, canvas, screenA + 1, offset
					+ mScreenSize, top, scroll);
			MGridScreenEffector.drawScreen(mContainer, canvas, mScroller, screenA, offset, top,
					scroll);
		} else if (mEffector == null) {
			MGridScreenEffector.drawScreen(mContainer, canvas, mScroller, screenA, offset, top,
					scroll);
			MGridScreenEffector.drawScreen(mContainer, canvas, mScroller, screenB, offset
					+ mScreenSize, top, scroll);
		} else {
			canvas.save();
//			if (mEffector.isCurrentScreenOnTop() && screenA == mScroller.getCurrentScreen()) {
//				drawScreenBg(mContainer, canvas, screenB, offset
//						+ mScreenSize, top, scroll);
//				mEffector.drawScreen(canvas, screenB, offset + mScreenSize, top, scroll);
//				drawScreenBg(mContainer, canvas, screenA, offset, top,
//						scroll);
//				mEffector.drawScreen(canvas, screenA, offset, top, scroll);
//			} else {
//				drawScreenBg(mContainer, canvas, screenA, offset, top,
//						scroll);
//				mEffector.drawScreen(canvas, screenA, offset, top, scroll);
//				drawScreenBg(mContainer, canvas, screenB, offset
//						+ mScreenSize, top, scroll);
//				mEffector.drawScreen(canvas, screenB, offset + mScreenSize, top, scroll);
//			}
			
			// 适配float和int型两种绘制方式
			if (mEffector.isFloatAdapted()) {
				drawEffectorByFloat(canvas, screenA, screenB, top, offset, scroll);
			} else {
				drawByEffectorInt(canvas, screenA, screenB, top, offset, scroll);
			}
			canvas.restore();
		}
		return true;
	}
	
	private void drawEffectorByFloat(GLCanvas canvas, int screenA, int screenB, int top, int offset, int scroll) {
		float offsetFloat = getCurrentScreenDrawingOffset();
		float scrollFloat = mScroller.getScrollFloat();
		if (mEffector.isCurrentScreenOnTop() && screenA == mScroller.getCurrentScreen()) {
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenB, offset
					+ mScreenSize, top, scroll);
			mEffector.drawScreen(canvas, screenB, offsetFloat + mScreenSize, top, scrollFloat);
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenA, offset, top,
					scroll);
			mEffector.drawScreen(canvas, screenA, offsetFloat, top, scrollFloat);
		} else {
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenA, offset, top,
					scroll);
			mEffector.drawScreen(canvas, screenA, offsetFloat, top, scrollFloat);
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenB, offset
					+ mScreenSize, top, scroll);
			mEffector.drawScreen(canvas, screenB, offsetFloat + mScreenSize, top, scrollFloat);
		}
	}
	
	private void drawByEffectorInt(GLCanvas canvas, int screenA, int screenB, int top, int offset, int scroll) {
		if (mEffector.isCurrentScreenOnTop() && screenA == mScroller.getCurrentScreen()) {
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenB, offset
					+ mScreenSize, top, scroll);
			mEffector.drawScreen(canvas, screenB, offset + mScreenSize, top, scroll);
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenA, offset, top,
					scroll);
			mEffector.drawScreen(canvas, screenA, offset, top, scroll);
		} else {
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenA, offset, top,
					scroll);
			mEffector.drawScreen(canvas, screenA, offset, top, scroll);
			MGridScreenEffector.drawScreenBackground(mContainer, canvas, screenB, offset
					+ mScreenSize, top, scroll);
			mEffector.drawScreen(canvas, screenB, offset + mScreenSize, top, scroll);
		}
	}
	
	
	private float getCurrentScreenDrawingOffset() {
		float offset = mScroller.getCurrentScreenOffsetFloat();
		if (mScroller.getCurrentScreenOffset() > 0) {
			offset -= mScreenSize;
		}
		return offset;
	}


	private void drawScreenBg(GridScreenContainer container, GLCanvas canvas, int screen,
			int offset, int topPadding, int scroll) {
		if (mEffector != null && !mEffector.needDrawBackground()) {
			return;
		}
		MGridScreenEffector.drawScreenBackground(mContainer, canvas, screen, offset, topPadding,
				scroll);
	}
	
	@Override
	public void setType(int type) {
		MGridScreenEffector oldEffector = mEffector;
		if (type == IEffectorIds.EFFECTOR_TYPE_RANDOM) {
			Rect rect = mContainer.getScreenRect();
			if (mType != type) {
				mRandomEffectors = new MGridScreenEffector[] { new BinaryStarEffector(),
						new ChariotEffector(), new ShutterEffector(), new ChordEffector(),
						new CylinderEffector(), new SphereEffector(), new SnakeEffector(rect) };
				mCurrentIndex = -1;
			}
			mType = type;
			int index = (int) (Math.random() * mRandomEffectors.length);
			if (index == mCurrentIndex) {
				index = (index + 1) % mRandomEffectors.length;
			}
			mEffector = mRandomEffectors[index];
			mCurrentIndex = index;
		} else if (mType == type) {
			return;
		} else {
			mType = type;
			mRandomEffectors = null;
			switch (type) {
				case IEffectorIds.EFFECTOR_TYPE_BINARY_STAR :
					mEffector = new BinaryStarEffector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_SNAKE :
					if (mContainer != null) {
						Rect rect = mContainer.getScreenRect();
						if (rect != null) {
							mEffector = new SnakeEffector(rect);
						}
					}
					break;
				case IEffectorIds.EFFECTOR_TYPE_CHARIOT :
					mEffector = new ChariotEffector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_SHUTTER :
					mEffector = new ShutterEffector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_CHORD :
					mEffector = new ChordEffector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_CYLINDER :
					mEffector = new CylinderEffector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_SPHERE :
					mEffector = new SphereEffector();
					break;
//				case GRID_EFFECTOR_TYPE_SCRIBBLE:
//					 mEffector = new ScribbleEffector();
//					 break;
//				case GRID_EFFECTOR_TYPE_ZOOM :
//					mEffector = new ZoomEffector();
//					break;
//				case GRID_EFFECTOR_TYPE_FLYAWAY :
//					mEffector = new FlyAwayEffector();
//					break;
				default :
					mEffector = null;
					break;
			}
		}
		mScroller.setInterpolator(mEffector == null
				? DECELERATEINTERPOLATOR3
				: DECELERATEINTERPOLATOR5);
		if (oldEffector != mEffector) {
			if (oldEffector != null) {
				oldEffector.onDetach();
			}
			if (mEffector != null) {
				mEffector.setDrawQuality(mQuality);
				mEffector.onAttach(mContainer, mScroller);
			}
		}
	}

	@Override
	public void updateRandomEffect() {
		if (mType == IEffectorIds.EFFECTOR_TYPE_RANDOM) {
			setType(IEffectorIds.EFFECTOR_TYPE_RANDOM);
		}
	}

	@Override
	public void onSizeChanged(int w, int h, int orientation) {
		mOrientation = mScroller.getOrientation();
		mScreenSize = mScroller.getScreenSize();
		if (mEffector != null) {
			mEffector.onSizeChanged(w, h);
		}

	}

	@Override
	public int getMaxOvershootPercent() {
		return 0;
	}

	@Override
	public void onAttach(ShellScreenScrollerListener container) {
		if (container != null && container instanceof GridScreenContainer) {
			ShellScreenScroller scroller = container.getScreenScroller();
			mContainer = (GridScreenContainer) container;
			if (scroller == null) {
				throw new IllegalArgumentException("Container has no ScreenScroller.");
			} else if (mScroller != scroller) {
				mScroller = scroller;
				mOrientation = mScroller.getOrientation();
				mScreenSize = mScroller.getScreenSize();
				int oldType = mType;
				mType = IEffectorIds.EFFECTOR_TYPE_DEFAULT;
				mEffector = null;
				setType(oldType);
			}
		} else {
			throw new IllegalArgumentException(
					"container is not an instance of GridScreenEffector.GridScreenContainer");
		}

	}

	@Override
	public void onDetach() {
		mContainer = null;
		mScroller = null;
		//		mRandomEffectors = null;
		if (mEffector != null) {
			mEffector.onDetach();
		}
	}

	@Override
	public void setDrawQuality(int quality) {
		mQuality = quality;
		if (mEffector != null) {
			mEffector.setDrawQuality(quality);
		}
	}

	@Override
	public void recycle() {
		mRandomEffectors = null;
	}

	@Override
	public void setScreenGap(int gap) {
		mGap = gap;
	}

	@Override
	public void setTopPadding(int top) {
		mTopPadding = top;
	}

	@Override
	public void setVerticalSlide(boolean verticalSlide) {
		mVerticalSlide = verticalSlide;
		if (mEffector != null) {
			mEffector.setVerticalSlide(verticalSlide);
		}
	}

	@Override
	public void onAttachReserveEffector(ShellScreenScrollerListener container) {
	}

	@Override
	public boolean isAnimationing() {
		return mEffector != null && mEffector.isAnimationing();
	}


	@Override
	public boolean isNeedEnableNextWidgetDrawingCache() {
		return mEffector == null ? true : mEffector
				.isNeedEnableNextWidgetDrawingCache();
	}

	@Override
	public boolean disableWallpaperScrollDelay() {
		return mEffector == null ? true : mEffector
				.disableWallpaperScrollDelay();
	}

	@Override
	public void onScrollStart() {
		if (mEffector != null) {
			mEffector.onScrollStart();
		}
	}

	@Override
	public void onScrollEnd() {
		if (mEffector != null) {
			mEffector.onScrollEnd();
		}
	}

	@Override
	public void onFlipStart() {
		if (mEffector != null) {
			mEffector.onFlipStart();
		}
	}

	@Override
	public void onFlipInterupted() {
		if (mEffector != null) {
			mEffector.onFlipInterupted();
		}
	}

	@Override
	public void onThemeSwitch() {
		if (mEffector != null) {
			mEffector.onThemeSwitch();
		}
	}

	@Override
	public void notifyRegetScreenRect() {
		if (mEffector != null) {
			mEffector.notifyRegetScreenRect();
		}
	}

	@Override
	public void onScrollTouchUp() {
	}

	@Override
	public int getType() {
		return mType;
	}
	
	@Override
	public MGridScreenEffector getEffector() {
		return mEffector;
	}
}
