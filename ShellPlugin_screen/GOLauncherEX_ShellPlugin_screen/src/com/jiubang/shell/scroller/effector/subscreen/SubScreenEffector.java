package com.jiubang.shell.scroller.effector.subscreen;

import android.graphics.Color;
import android.graphics.Rect;

import com.go.gl.graphics.ColorGLDrawable;
import com.go.gl.graphics.GLCanvas;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.effector.united.IEffectorIds;
import com.jiubang.shell.screen.GLSuperWorkspace;
import com.jiubang.shell.screen.GLWorkspace;
import com.jiubang.shell.scroller.ShellScreenScroller;
import com.jiubang.shell.scroller.ShellScreenScrollerEffector;
import com.jiubang.shell.scroller.ShellScreenScrollerListener;

/**
 * 只绘制当前两屏的特效类
 * @author dengweiming
 *
 */
public class SubScreenEffector implements ShellScreenScrollerEffector {

	/** 低质量绘图 */
	public final static int DRAW_QUALITY_LOW = 0;
	/** 中等质量绘图 */
	public final static int DRAW_QUALITY_MID = 1;
	/** 高等质量绘图 */
	public final static int DRAW_QUALITY_HIGH = 2;

	public final static int MAXOVERSHOOTPERCENT = 100;

	SubScreenContainer mContainer;
	ShellScreenScroller mScroller;
	MSubScreenEffector mEffector;
	MSubScreenEffector[] mRandomEffectors;
	int mCurrentIndex;
	int mType;
	int mBackgroundColor = 0x00000000;
	// 黑色的背景，用来遮罩，替换canvas.drawColor的方法
	ColorGLDrawable mBackgroundDrawable;
	int mScreenSize;
	int mOrientation;
	int mQuality;
	int mGap;
	int mTopPadding;
	boolean mVerticalSlide = false; //是否支持上下滑动

	public SubScreenEffector(ShellScreenScroller scroller) {
		assert scroller != null; // 如果为null也就没任何意义了
		mScroller = scroller;
		mScroller.setEffector(this);
		mBackgroundDrawable = new ColorGLDrawable(Color.parseColor("#FF000000"));
		mBackgroundDrawable.setBounds(0, 0, DrawUtils.sWidthPixels, DrawUtils.sHeightPixels);
	}

	@Override
	public boolean onDraw(GLCanvas canvas) {
		final int scroll = mScroller.getScroll();
		boolean bgDrawn = mScroller.isBackgroundAlwaysDrawn();
		boolean combinebg = mEffector != null && mEffector.isCombineBackground();
		if (!combinebg) {
			bgDrawn |= mScroller.drawBackground(canvas, scroll);
		}
		int curOffset = mScroller.getCurrentScreenOffset();
		int offset = curOffset;
		if (offset > 0) {
			offset -= mScreenSize;
		}
		if (!bgDrawn) {
//			canvas.drawColor(mBackgroundColor);
			final int saveCount = canvas.save();
			float depth = 0;
			float transY = 0;
			if (mContainer instanceof GLSuperWorkspace && GLWorkspace.sLayoutScale < 1.0f) {
				depth = ((GLSuperWorkspace) mContainer).getTranslateZ();
				transY = ((GLSuperWorkspace) mContainer).getTranslateY();
			}
			if (mScroller.getOrientation() == ShellScreenScroller.HORIZONTAL) {
				canvas.translate(scroll, -transY, -depth);
			} else {
				canvas.translate(0, scroll, -depth);
			}
			mBackgroundDrawable.draw(canvas);
			canvas.restoreToCount(saveCount);
		}
		final int screenA = mScroller.getDrawingScreenA();
		final int screenB = mScroller.getDrawingScreenB();
		if (mScroller.isFinished()
				|| (mType == IEffectorIds.EFFECTOR_TYPE_DEFAULT && offset == 0 && mScroller
						.getCurrentDepth() == 0)) {
			MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenA, offset, bgDrawn,
					mVerticalSlide);
			if (mScroller.getLayoutScale() < 1.0f) {
				MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenA - 1, offset
						- mScreenSize, bgDrawn, mVerticalSlide);
				MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenA + 1, offset
						+ mScreenSize, bgDrawn, mVerticalSlide);
			}
		} else if (mEffector == null) {
			float offsetFloat = mScroller.getCurrentScreenDrawingOffset(true);
			MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenA, offsetFloat, bgDrawn,
					mVerticalSlide);
			MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenB, offsetFloat
					+ mScreenSize, bgDrawn, mVerticalSlide);
			if (mScroller.getLayoutScale() < 1.0f) {
				if (Math.abs(offsetFloat) >= mScreenSize / 2) {
					MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenB + 1, offsetFloat
							+ mScreenSize + mScreenSize, bgDrawn, mVerticalSlide);
				} else {
					MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenA - 1, offsetFloat
							- mScreenSize, bgDrawn, mVerticalSlide);
				}
			}
		} else {
			mEffector.onScrollChanged(scroll, curOffset);
			if (mEffector.toReverse()) {
				mEffector.drawView(canvas, screenB, offset + mScreenSize, false);
				mEffector.drawView(canvas, screenA, offset, true);
			} else {
				mEffector.drawView(canvas, screenA, offset, true);
				mEffector.drawView(canvas, screenB, offset + mScreenSize, false);
			}
		}
		return true;
	}

//	@Override
//	public boolean onDraw(GLCanvas canvas) {
//		final int extraX = mGap * 2;
//		final int scroll = mScroller.getScroll();
//		boolean bgDrawn = mScroller.isBackgroundAlwaysDrawn();
//		boolean combinebg = mEffector != null && mEffector.isCombineBackground();
//		if (!combinebg) {
//			bgDrawn |= mScroller.drawBackground(canvas, scroll);
//		}
//		if (!bgDrawn) {
//			canvas.drawColor(mBackgroundColor);
//		}
//		int curOffset = mScroller.getCurrentScreenOffset();
//		int offset = curOffset;
//		if (offset > 0) {
//			offset -= mScreenSize;
//		}
//		final int screenA = mScroller.getDrawingScreenA();
//		final int screenB = mScroller.getDrawingScreenB();
//		if (offset == 0 && mScroller.getCurrentDepth() == 0) {
//			MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenA, offset + extraX,
//					 bgDrawn, mVerticalSlide);
//			if (mScroller.getLayoutScale() < 1.0f) {
//				MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenA - 1, offset
//						+ extraX - mScreenSize, bgDrawn, mVerticalSlide);
//				MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenA + 1, offset
//						+ extraX + mScreenSize, bgDrawn, mVerticalSlide);
//			}
//		} else if (mEffector == null) {
//			MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenA, offset + extraX,
//					bgDrawn, mVerticalSlide);
//			MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenB, offset + extraX
//					+ mScreenSize, bgDrawn, mVerticalSlide);
//			if (mScroller.getLayoutScale() < 1.0f) {
//				if (Math.abs(offset) >= mScreenSize / 2) {
//					MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenB + 1, offset
//							+ extraX + mScreenSize + mScreenSize, bgDrawn, mVerticalSlide);
//				} else {
//					MSubScreenEffector.drawView(mContainer, mScroller, canvas, screenA - 1, offset
//							+ extraX - mScreenSize, bgDrawn, mVerticalSlide);
//				}
//			}
//
//		} else {
//			mEffector.onScrollChanged(mScroller.getScroll() + extraX, curOffset);
//			if (mEffector.toReverse()) {
//				mEffector.drawView(canvas, screenB, offset + mScreenSize, false);
//				mEffector.drawView(canvas, screenA, offset, true);
//			} else {
//				mEffector.drawView(canvas, screenA, offset, true);
//				mEffector.drawView(canvas, screenB, offset + mScreenSize, false);
//			}
//		}
//		return true;
//	}
	
	@Override
	public void setType(int type) {
		MSubScreenEffector oldEffector = mEffector;
		if (type <= IEffectorIds.EFFECTOR_TYPE_RANDOM) {
			if (mRandomEffectors == null) {
				mRandomEffectors = new MSubScreenEffector[] { new BounceEffector(),
						new BulldozeEffector(), new CuboidInsideEffector(),
						new CuboidOutsideEffector(), new FlipEffector(), new RollEffector(),
						new WaveEffector(), new WindmillEffector(), };
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
				case IEffectorIds.EFFECTOR_TYPE_BOUNCE :
					mEffector = new BounceEffector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_BULLDOZE :
					mEffector = new BulldozeEffector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_CUBOID1 :
					mEffector = new CuboidInsideEffector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_CUBOID2 :
					mEffector = new CuboidOutsideEffector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_FLIP :
					mEffector = new FlipEffector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_FLIP2 :
					mEffector = new Flip2Effector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_ROLL :
					mEffector = new RollEffector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_WAVE :
					mEffector = new WaveEffector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_WAVE_FLIP :
					mEffector = new WaveFlipEffector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_CARD_FLIP:
					mEffector = new CardScaleEffector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_WINDMILL :
					mEffector = new WindmillEffector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_STACK :
					mEffector = new StackEffector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_FLYIN :
					mEffector = new FlyinEffector();
					break;	
				case IEffectorIds.EFFECTOR_TYPE_CROSSFADE :
					mEffector = new CrossFadeEffector();
					break;	
				case IEffectorIds.EFFECTOR_TYPE_PAGETURN :
					mEffector = new PageturnEffector();
					break;
				case IEffectorIds.EFFECTOR_TYPE_CURVE :
					mEffector = new CurveEffector();
					break;	
				//				case EFFECTOR_TYPE_CHORD:
				//					mEffector = new ChordScreenEffector();
				//					break;
				//				case EFFECTOR_TYPE_ZOOM:
				//					mEffector = new ZoomScreenEffector();
				//					break;
					
//Next Launcher Effect					
//				case EFFECTOR_TYPE_FLYAWAY :
//					mEffector = new FlyAwayScreenEffectorForAppDrawer();
//					break;
//				case EFFECTOR_TYPE_CUBOID :
//					mEffector = new CuboidScreenEffector();
//					break;
//				case EFFECTOR_TYPE_ALPHA :
//					mEffector = new AlphaEffector();
//					break;
//				case EFFECTOR_TYPE_DOCK_FLIP :
//					mEffector = new DockFlipEffector();
//					break;
				case IEffectorIds.EFFECTOR_TYPE_CRYSTAL :
					if (mContainer != null) {
						Rect rect = mContainer.getScreenRect();
						if (rect != null) {
							mEffector = new CrystalEffector(rect);
							break;
						}
					}
				case IEffectorIds.EFFECTOR_TYPE_CLOTH :
					if (mContainer != null) {
						Rect rect = mContainer.getScreenRect();
						if (rect != null) {
							mEffector = new ClothEffector(rect);
							break;
						}
					}
					break;
				default :
					mEffector = null;
					break;
			}
		}
		if (oldEffector != mEffector) {
			if (oldEffector != null) {
				oldEffector.onDetach();
			}
			if (mEffector != null) {
				mEffector.setDrawQuality(mQuality);
				mEffector.onAttach(mContainer, mScroller);
			} else {
				mScroller.setOvershootPercent(MAXOVERSHOOTPERCENT);
				MSubScreenEffector.sAlphaRatio = (float) Math.PI / (MSubScreenEffector.RADIUS * 2) / mScreenSize;
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
			mEffector.onSizeChanged();
		}
		MSubScreenEffector.sAlphaRatio = (float) Math.PI / (MSubScreenEffector.RADIUS * 2) / mScreenSize;
	}

	@Override
	public int getMaxOvershootPercent() {
		return mEffector == null ? MAXOVERSHOOTPERCENT : mEffector.getMaxOvershootPercent();
	}

	@Override
	public void onAttach(ShellScreenScrollerListener container) {
		if (container != null && container instanceof SubScreenContainer) {
			ShellScreenScroller scroller = container.getScreenScroller();
			mContainer = (SubScreenContainer) container;
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
					"container is not an instance of SubScreenEffector.SubScreenContainer");
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

	// TODO:GridScreenEffector需要这样的参数，提到接口层
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
		if (mEffector != null) {
			mEffector.onScrollTouchUp();
		}
	}

	@Override
	public int getType() {
		return mType;
	}

	@Override
	public MSubScreenEffector getEffector() {
		return mEffector;
	}
}
