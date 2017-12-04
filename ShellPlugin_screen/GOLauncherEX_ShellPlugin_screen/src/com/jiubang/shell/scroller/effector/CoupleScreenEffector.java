package com.jiubang.shell.scroller.effector;

import java.util.Random;

import android.annotation.SuppressLint;

import com.go.gl.graphics.GLCanvas;
import com.go.util.graphics.effector.united.EffectorControler;
import com.go.util.graphics.effector.united.IEffectorIds;
import com.jiubang.shell.scroller.ShellScreenScroller;
import com.jiubang.shell.scroller.ShellScreenScrollerEffector;
import com.jiubang.shell.scroller.ShellScreenScrollerListener;
import com.jiubang.shell.scroller.effector.gridscreen.GridScreenEffector;
import com.jiubang.shell.scroller.effector.subscreen.SubScreenEffector;


/**
 * 屏幕特效和单元格特效的合并类
 * 
 * @author yangguanxiang
 * 
 */
public class CoupleScreenEffector implements ShellScreenScrollerEffector {
	/** 没有特殊场所*/
	public final static int PLACE_NONE = 0;
	/** 桌面场所 */
	public final static int PLACE_DESK = 1;
	/** 功能表场所 */
	public final static int PLACE_MENU = 2;

	private GridScreenEffector mGridScreenEffector;
	private SubScreenEffector mSubScreenEffector;
	private ShellScreenScroller mScroller;
	/** 特效的类型 */
	int mEffectorType = IEffectorIds.SUBSCREEN_EFFECTOR_TYPE;
	// 特效的场所：Desk or Menu
	private int mPlaceType;
	private boolean mIsRandom = false;
	private boolean mIsCustomRandom = false;
	private Random mRandom;

	private int[] mDeskCustomRandomEffects;
	private int[] mAppIconCustomRandomEffects;

	private int mTop;
	private int mLeft;
	private boolean mVerticalSlide;
	private EffectorControler mEffectorControler;

	private int mType;
	
	/**
	 * 
	 * @param scroller
	 * @param placeType
	 *            特效作用的场所
	 * @param effectorType
	 *            特效的类型
	 */
	public CoupleScreenEffector(ShellScreenScroller scroller, int placeType) {
		assert scroller != null; // 如果为null也就没任何意义了
		//mEffectorType = effectorType;
		mPlaceType = placeType;
		mScroller = scroller;
		mScroller.setEffector(this);
		mRandom = new Random();
		mEffectorControler = EffectorControler.getInstance();
	}
	public CoupleScreenEffector(ShellScreenScroller scroller, int placeType, int effectorType) {
		assert scroller != null; // 如果为null也就没任何意义了
		mEffectorType = effectorType;
		mPlaceType = placeType;
		mScroller = scroller;
		mScroller.setEffector(this);
		mRandom = new Random();
	}

	/** 获取当前使用的特效 */
	public ShellScreenScrollerEffector getScrollerEffector() {
		if (mEffectorType == IEffectorIds.GRIDSCREEN_EFFECTOR_TYPE) {
			if (mGridScreenEffector == null) {
				ShellScreenScroller scroller = mScroller;
				mGridScreenEffector = new GridScreenEffector(mScroller); // 这里会引起onDetach
				mGridScreenEffector.setScreenGap(mLeft);
				mGridScreenEffector.setTopPadding(mTop);
				mScroller = scroller;
				mScroller.setEffector(this);
			}

			return mGridScreenEffector;
		} else if (mEffectorType == IEffectorIds.SUBSCREEN_EFFECTOR_TYPE) {
			if (mSubScreenEffector == null) {
				ShellScreenScroller scroller = mScroller;
				mSubScreenEffector = new SubScreenEffector(mScroller); // 这里会引起onDetach
				mSubScreenEffector.setScreenGap(mLeft);
				mSubScreenEffector.setTopPadding(mTop);
				mScroller = scroller;
				mScroller.setEffector(this);
			}
			return mSubScreenEffector;
		}
		return null;
	}

	/**
	 * 屏幕循环切换开关变化时调用，把非当前的特效与scroller绑定
	 */
	@Override
	public void onAttachReserveEffector(ShellScreenScrollerListener container) {
		if (mEffectorType == IEffectorIds.GRIDSCREEN_EFFECTOR_TYPE && null != mSubScreenEffector) {
			mSubScreenEffector.onAttach(container);
		} else if (mEffectorType == IEffectorIds.SUBSCREEN_EFFECTOR_TYPE && null != mGridScreenEffector) {
			mGridScreenEffector.onAttach(container);
		}
	}

	@SuppressLint("WrongCall")
	@Override
	public boolean onDraw(GLCanvas canvas) {
		getScrollerEffector().onDraw(canvas);
		return true;
	}

	@Override
	public void setType(int type) {
		int index = 0;
		mScroller.setAccFactor(ShellScreenScroller.DEFAULT_ACC);
		mScroller.setDepthEnabled(false);
		mIsRandom = false;
		mIsCustomRandom = false;
		if (mPlaceType == PLACE_NONE) { // 无特殊场所使用默认特效
			type = IEffectorIds.EFFECTOR_TYPE_DEFAULT;
		} else {
			switch (type) {
				case IEffectorIds.EFFECTOR_TYPE_RANDOM :
					int soucrceType = mPlaceType == PLACE_DESK
							? EffectorControler.TYPE_SCREEN_SETTING
							: EffectorControler.TYPE_APP_DRAWER_SETTING;
					int[] idArray = mEffectorControler.getRandomEffectIdArray(soucrceType);
					index = mRandom.nextInt(idArray.length);
					setType(idArray[index]);
					mIsRandom = true;
					mIsCustomRandom = false;
					return;
				case IEffectorIds.EFFECTOR_TYPE_RANDOM_CUSTOM :
					int[] randomEffects = mPlaceType == PLACE_DESK
							? mDeskCustomRandomEffects
							: mAppIconCustomRandomEffects;
					if (null != randomEffects) {
						index = mRandom.nextInt(randomEffects.length);
						setType(randomEffects[index]);
						mIsCustomRandom = true;
						mIsRandom = false;
					}
					return;
				case IEffectorIds.EFFECTOR_TYPE_CARD_FLIP :
					mScroller.setAccFactor(1.3F);
					break;
				case IEffectorIds.EFFECTOR_TYPE_CHARIOT :
				case IEffectorIds.EFFECTOR_TYPE_CYLINDER :
				case IEffectorIds.EFFECTOR_TYPE_SPHERE :
				case IEffectorIds.EFFECTOR_TYPE_CRYSTAL :
				case IEffectorIds.EFFECTOR_TYPE_CLOTH :
				case IEffectorIds.EFFECTOR_TYPE_SNAKE :
					mScroller.setDepthEnabled(true);
					break;
				default :
					break;
			}
		}
		
		mType = type;
		
		mEffectorType = mEffectorControler.getEffectorInfoById(type).mEffectorType;
		getScrollerEffector().setType(type);
	}

	@Override
	public void updateRandomEffect() {
		if (mIsRandom) {
			setType(IEffectorIds.EFFECTOR_TYPE_RANDOM);
		} else if (mIsCustomRandom) {
			setType(IEffectorIds.EFFECTOR_TYPE_RANDOM_CUSTOM);
		}
	}

	@Override
	public void onSizeChanged(int w, int h, int orientation) {
		if (mSubScreenEffector != null) {
			mSubScreenEffector.onSizeChanged(w, h, orientation);
		}
		if (mGridScreenEffector != null) {
			mGridScreenEffector.onSizeChanged(w, h, orientation);
		}
	}

	@Override
	public int getMaxOvershootPercent() {
		int result = getScrollerEffector().getMaxOvershootPercent();
		return result;
	}

	@Override
	public void onAttach(ShellScreenScrollerListener container) {
		assert container != null;
		mScroller = container.getScreenScroller();
		getScrollerEffector().onAttach(container);
	}

	@Override
	public void onDetach() {
		if (mEffectorType == IEffectorIds.GRIDSCREEN_EFFECTOR_TYPE) {
			if (mGridScreenEffector != null) {
				mGridScreenEffector.onDetach();
			}
		} else if (mEffectorType == IEffectorIds.SUBSCREEN_EFFECTOR_TYPE) {
			if (mSubScreenEffector != null) {
				mSubScreenEffector.onDetach();
			}
		}
		// getScrollerEffector().onDetach();
	}

	@Override
	public void setDrawQuality(int quality) {
		getScrollerEffector().setDrawQuality(quality);
	}

	@Override
	public void recycle() {
		getScrollerEffector().recycle();
	}

	public int getCurEffectorType() {
		return mEffectorType;
	}

	public void setDeskCustomRandomEffects(int[] effects) {
		mDeskCustomRandomEffects = effects;
	}

	public void setAppIconCustomRandomEffects(int[] effects) {
		mAppIconCustomRandomEffects = effects;
	}

	@Override
	public void setScreenGap(int gap) {
		mLeft = gap;
		if (mSubScreenEffector != null) {
			mSubScreenEffector.setScreenGap(gap);
		}
		if (mGridScreenEffector != null) {
			mGridScreenEffector.setScreenGap(gap);
		}
	}

	@Override
	public void setTopPadding(int top) {
		mTop = top;
		if (mSubScreenEffector != null) {
			mSubScreenEffector.setTopPadding(top);
		}
		if (mGridScreenEffector != null) {
			mGridScreenEffector.setTopPadding(top);
		}
	}

	@Override
	public void setVerticalSlide(boolean verticalSlide) {
		mVerticalSlide = verticalSlide;
		if (mEffectorType == IEffectorIds.GRIDSCREEN_EFFECTOR_TYPE) {
			mGridScreenEffector.setVerticalSlide(verticalSlide);
		} else {
			mSubScreenEffector.setVerticalSlide(verticalSlide);
		}
	}
	
	@Override
	public boolean isNeedEnableNextWidgetDrawingCache() {
		if (mEffectorType == IEffectorIds.GRIDSCREEN_EFFECTOR_TYPE) {
			return mGridScreenEffector.isNeedEnableNextWidgetDrawingCache();
		} else {
			return mSubScreenEffector.isNeedEnableNextWidgetDrawingCache();
		}
	}
	
	@Override
	public boolean disableWallpaperScrollDelay() {
		if (mEffectorType == IEffectorIds.GRIDSCREEN_EFFECTOR_TYPE) {
			return mGridScreenEffector.disableWallpaperScrollDelay();
		} else {
			return mSubScreenEffector.disableWallpaperScrollDelay();
		}
	}
	
	@Override
	public void onScrollStart() {
		if (mEffectorType == IEffectorIds.GRIDSCREEN_EFFECTOR_TYPE) {
			mGridScreenEffector.onScrollStart();
		} else {
			mSubScreenEffector.onScrollStart();
		}
	}
	
	@Override
	public void onScrollEnd() {
		if (mEffectorType == IEffectorIds.GRIDSCREEN_EFFECTOR_TYPE) {
			mGridScreenEffector.onScrollEnd();
		} else {
			mSubScreenEffector.onScrollEnd();
		}
	}
	
	@Override
	public void onFlipStart() {
		if (mEffectorType == IEffectorIds.GRIDSCREEN_EFFECTOR_TYPE) {
			mGridScreenEffector.onFlipStart();
		} else {
			mSubScreenEffector.onFlipStart();
		}	
	}
	
	@Override
	public void onFlipInterupted() {
		if (mEffectorType == IEffectorIds.GRIDSCREEN_EFFECTOR_TYPE) {
			mGridScreenEffector.onFlipInterupted();
		} else {
			mSubScreenEffector.onFlipInterupted();
		}
	}
	
	@Override
	public boolean isAnimationing() {
		if (mEffectorType == IEffectorIds.GRIDSCREEN_EFFECTOR_TYPE) {
			return mGridScreenEffector.isAnimationing();
		} else {
			return mSubScreenEffector.isAnimationing();
		}
	}
	
	@Override
	public void onThemeSwitch() {
		if (mEffectorType == IEffectorIds.GRIDSCREEN_EFFECTOR_TYPE) {
			mGridScreenEffector.onThemeSwitch();
		} else {
			mSubScreenEffector.onThemeSwitch();
		}
	}
	
	@Override
	public void notifyRegetScreenRect() {
		if (mEffectorType != IEffectorIds.GRIDSCREEN_EFFECTOR_TYPE) {
			mSubScreenEffector.notifyRegetScreenRect();
		} else {
			mGridScreenEffector.notifyRegetScreenRect();
		}
	}
	
	@Override
	public void onScrollTouchUp() {
		if (mEffectorType != IEffectorIds.GRIDSCREEN_EFFECTOR_TYPE) {
			mSubScreenEffector.onScrollTouchUp();
		}
	}
	
	@Override
	public int getType() {
		return mType;
	}
	
	@Override
	public Object getEffector() {
		return getScrollerEffector().getEffector();
	}
}
