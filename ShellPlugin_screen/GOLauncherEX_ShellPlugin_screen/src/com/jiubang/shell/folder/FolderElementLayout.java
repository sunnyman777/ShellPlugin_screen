package com.jiubang.shell.folder;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup.LayoutParams;

import com.go.gl.animation.InterpolatorValueAnimation;
import com.go.gl.graphics.BitmapGLDrawable;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLImageView;
import com.go.gl.widget.GLImageView.ScaleType;
import com.jiubang.ggheart.folder.FolderConstant;
import com.jiubang.shell.common.component.GLModel3DView;

/**
 * 装载文件夹元素的容器
 * @author yangguanxiang
 *
 */
public class FolderElementLayout extends GLViewGroup {
	public static final float GRAP_MULITIPLE = 0.14f;
	private static final long DURATION_EDIT_ANIM = 200;
	private BitmapGLDrawable mCacheDrawable;
	private boolean mIsAnimating;
	private Paint mPaint;
	private Canvas mCanvas;
	private InterpolatorValueAnimation mEditAnimation;
	private int mStatus = DRAW_STATUS_NORMAL;
	private static final int DRAW_STATUS_START_EDIT = 0;
	private static final int DRAW_STATUS_END_EDIT = 1;
	private static final int DRAW_STATUS_NORMAL = 2;
	private static final int DRAW_STATUS_EDITING = 3;
	private int mPreChildCount;
	private static final int BMP_CACHE_HIGHT_MAGNIFICATION = 2;
	private EditAnimationListener mEditAnimationListener;
	public FolderElementLayout(Context context) {
		super(context);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setFilterBitmap(true);
		mCanvas = new Canvas();
		mEditAnimation = new InterpolatorValueAnimation(0);
	}

	private void composeCacheDrawable(Bitmap base) {
		int count = getChildCount();
		if (count > 0 && mWidth > 0 && mHeight > 0) {
			try {
				if (base == null) {
					base = Bitmap.createBitmap(mWidth, mHeight * BMP_CACHE_HIGHT_MAGNIFICATION, Config.ARGB_8888);
				} else if (base.getWidth() != mWidth || base.getHeight() != mHeight * BMP_CACHE_HIGHT_MAGNIFICATION) {
					base.recycle();
					base = Bitmap.createBitmap(mWidth, mHeight * BMP_CACHE_HIGHT_MAGNIFICATION, Config.ARGB_8888);
				}
			} catch (OutOfMemoryError e) {
				invalidate();
				return;
			}
			mCanvas.setBitmap(base);
			//			int curColunm = 0;
			//			int row = 0;
			for (int i = 0; i < count; i++) {
				GLImageView icon = (GLImageView) getChildAt(i);
				if (icon.isVisible()) {
					Bitmap b = ((BitmapDrawable) icon.getDrawable()).getBitmap();
					//					Bitmap b = mIconBitmaps.get(i);
					int iconSize = icon.getWidth();
					if (b != null && !b.isRecycled()) {
						float scaleX = (float) iconSize / (float) b.getWidth();
						float scaleY = (float) iconSize / (float) b.getHeight();
						Matrix matrix = new Matrix();
						matrix.postScale(scaleX, scaleY);
						matrix.postTranslate(icon.getLeft(), icon.getTop());
						mCanvas.drawBitmap(b, matrix, mPaint);
						//						final float first = mIconSize * 0.12f;
						//						final float grap = mIconSize * 0.015f;
						//						final int innerIconSize = (int) (mIconSize - first * 2 - grap * 2) / 2;
						//						final float left = first + curColunm * (innerIconSize + grap * 2);
						//						final float top = first + row * (innerIconSize + grap * 2);
						//						float scaleX = (float) innerIconSize / (float) b.getWidth();
						//						float scaleY = (float) innerIconSize / (float) b.getHeight();
						//						Matrix matrix = new Matrix();
						//						matrix.postScale(scaleX, scaleY);
						//						matrix.postTranslate(left, top);
						//						mCanvas.drawBitmap(b, matrix, mPaint);
					}
					//					curColunm++;
					//					if (curColunm >= 2) {
					//						curColunm = 0;
					//						row++;
					//					}	
				}
			}
			mCacheDrawable = new BitmapGLDrawable(new BitmapDrawable(base));
			mCacheDrawable.setBounds(0, 0, mWidth, mHeight * BMP_CACHE_HIGHT_MAGNIFICATION);
			invalidate();
		}
	}

	/**
	 * 获得文件夹中小图标与正常图标的缩小比例
	 * @return
	 */
	public float[] getScaleXY() {
		float scaleXY[] = new float[2];
		GLImageView icon = (GLImageView) getChildAt(0);
//		int iconSize = icon.getWidth();
		scaleXY[0] = (float)  icon.getWidth() / (float) getWidth();
		scaleXY[1] = (float)  icon.getHeight() / (float) getHeight();
//		Matrix matrix = new Matrix();
//		matrix.postTranslate(icon.getLeft() + icon.getPaddingLeft(),
//				icon.getTop() + icon.getPaddingTop());
		return scaleXY;
	}

	/**
	 * 获得文件夹内小图标目标位置中心点<br>
	 * 注意：必须保证已经往FolderIcon里添加了该view
	 * @param target 目标位置
	 * @return
	 */
	public float[] getLocationCenter(int target) {
		if (target < 0) {
			return null;
		}
		float centerXY[] = new float[2];
		if (target < 4) {
			getChildCenterXY(target, centerXY);
		} else if ((getChildCount() - 1) % 2 == 0) {
			getChildCenterXY(2, centerXY);
		} else {
			getChildCenterXY(3, centerXY);
		}
		return centerXY;
	}

	private void getChildCenterXY(int target, float[] centerXY) {
		GLImageView icon = (GLImageView) getChildAt(target);
		if (icon != null) {
			centerXY[0] = icon.getLeft() + getIconPadding() + icon.getWidth() / 2;
			centerXY[1] = icon.getTop() + getIconPadding() + icon.getHeight() / 2;
		}
	}
	
	public void getChildLocation(int target, int[] locate) {
		if (target < 4) {
			GLImageView icon = (GLImageView) getChildAt(target);
			if (icon != null) {
				icon.getLocationOnScreen(locate);
			} else {
				getLocationOnScreen(locate);
				locate[0] += getWidth() / 4;
				locate[1] += getHeight() / 4;
			}
		} else {
			getLocationOnScreen(locate);
			locate[0] += getWidth() / 4;
			locate[1] += getHeight() / 4;
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();
		GLModel3DView glModel3DView = (GLModel3DView) getGLParent();
		int folderSize = glModel3DView.getIconWidth();
		final int offset = (int) (folderSize * FolderElementLayout.GRAP_MULITIPLE);
		for (int i = 0; i < count; i++) {
			GLImageView folderInsideIcon = (GLImageView) getChildAt(i);
			final int size = folderInsideIcon.getLayoutParams().width;
			switch (i) {
				case 0 :
					folderInsideIcon.layout(offset, offset, size + offset, size + offset);
					break;
				case 1 :
					folderInsideIcon.layout(folderSize - (size + offset), offset, folderSize
							- offset, size + offset);
					break;
				case 2 :
					folderInsideIcon.layout(offset, folderSize - (size + offset), size + offset,
							folderSize - offset);
					break;
				case 3 :
					folderInsideIcon.layout(folderSize - (size + offset), folderSize
							- (size + offset), folderSize - offset, folderSize - offset);
					break;
				case 4 :
					folderInsideIcon.layout(offset, folderSize - offset, size + offset, folderSize
							+ size - offset);
					break;
				case 5 :
					folderInsideIcon.layout(folderSize - (size + offset), folderSize - offset,
							folderSize - offset, folderSize + size - offset);
					break;
				case 6 :
					folderInsideIcon.layout(offset, folderSize - offset + size, size + offset,
							folderSize + size - offset + size);
					break;
				case 7 :
					folderInsideIcon.layout(folderSize - (size + offset), folderSize - offset
							+ size, folderSize - offset, folderSize + size - offset + size);
					break;
				default :
					break;
			}
		}
		rebuildCacheDrawable();
	}

	public void rebuildCacheDrawable() {
		Bitmap base = null;
		if (mCacheDrawable != null) {
			base = mCacheDrawable.getBitmap();
			if (base != null) {
				base.eraseColor(Color.TRANSPARENT);
			}
			mCacheDrawable.clear();
			mCacheDrawable = null;
		}
		composeCacheDrawable(base);
	}
	
	public void setChildrenVisible(boolean visible) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).setVisible(visible);
		}
	}
	
	public void setIsAnimating(boolean isAnimating) {
		mIsAnimating = isAnimating;
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		boolean needClip = getChildCount() >= FolderConstant.MAX_ICON_COUNT;
		if (getChildCount() == FolderConstant.MAX_ICON_COUNT && mStatus == DRAW_STATUS_NORMAL) {
			needClip = false;
		}
		if (needClip && !mIsAnimating) {
			GLView childTop = getChildAt(0);
			GLView childBottom = getChildAt(3);
			int left = childTop.getLeft();
			int top = childTop.getTop();
			int right = childBottom.getRight();
			int bottom = childBottom.getBottom();
			canvas.clipRect(left, top, right, bottom);
		}
		switch (mStatus) {
			case DRAW_STATUS_NORMAL :
				
				break;
			case DRAW_STATUS_START_EDIT :
				if (mEditAnimation.isFinished()) {
					canvas.translate(0, -mEditAnimation.getDstValue());
					mStatus = DRAW_STATUS_EDITING;
					mEditAnimationListener.onEditAnimationFinish();
				} else {
					mEditAnimation.animate();
					canvas.translate(0, -mEditAnimation.getValue());
				}
				break;
			case DRAW_STATUS_END_EDIT :
				if (!mEditAnimation.isFinished()) {
					mEditAnimation.animate();
					canvas.translate(0, -mEditAnimation.getValue());
					invalidate();
				} else {
					mStatus = DRAW_STATUS_NORMAL;
					post(new Runnable() {
						@Override
						public void run() {
							int childCount = getChildCount();
							switch (childCount) {
								case 7 :
									removeViewAt(4);
									break;
								default :
									break;
							}
						}
					});
				}
				break;
			case DRAW_STATUS_EDITING :
				canvas.translate(0, -mEditAnimation.getDstValue());
				break;
			default :
				break;
		}
		if (mIsAnimating) {
			if (!needClip) {
				super.dispatchDraw(canvas);
			} else {
				int count = getChildCount();
				int j = 3;
				if (count % 2 == 0) {
					j = 4;
					if (count == 4
							&& mEditAnimation.getDstValue() == getChildAt(0).getBottom()
									- getIconPadding()) {
						j = 2;
					}
				}
				for (int i = count - j; i <= count - 1; i++) {
					GLView child = getChildAt(i);
					if (child != null) {
						drawChild(canvas, child, getDrawingTime());
					}
				}
			}
		} else {
			if (mCacheDrawable != null) {
				mCacheDrawable.draw(canvas);
			} else {
				super.dispatchDraw(canvas);
				rebuildCacheDrawable();
			}
		}
		
		if (!mEditAnimation.isFinished()) {
			invalidate();
		}
	}
	
	@Override
	public void cleanup() {
		super.cleanup();
		recycleCacheDrawable();
	}

	private void recycleCacheDrawable() {
		if (mCacheDrawable != null) {
			mCacheDrawable.clear();
			Bitmap base = mCacheDrawable.getBitmap();
			if (base != null) {
				base.recycle();
			}
		}
	}
	
	public void startEditAnimation() {
		int childCount = getChildCount();
		switch (childCount) {
			case 4 :
				if (mStatus == DRAW_STATUS_NORMAL || mPreChildCount == 3) {
					mEditAnimation.start(getChildAt(0).getTop(), getChildAt(0).getBottom()
							- getIconPadding(), DURATION_EDIT_ANIM);
				}
				break;
			case 5 :
				mEditAnimation.start(getChildAt(0).getTop(), getChildAt(0).getBottom()
						- getIconPadding(), DURATION_EDIT_ANIM);
				break;
			case 6 :
				if (mPreChildCount == 5
						&& getChildCount() == 6
						&& mEditAnimation.getDstValue() == getChildAt(3).getBottom()
								- getIconPadding()) {
					break;
				}
				if (mStatus == DRAW_STATUS_NORMAL) {
					mEditAnimation.start(getChildAt(0).getTop(), getChildAt(3).getBottom()
							- getIconPadding(), DURATION_EDIT_ANIM);
				} else if (mStatus == DRAW_STATUS_EDITING) {
					mEditAnimation.start(getChildAt(3).getTop(), getChildAt(3).getBottom()
							- getIconPadding(), DURATION_EDIT_ANIM);
				}
				break;
			case 7 :
				if (mStatus == DRAW_STATUS_NORMAL) {
					mEditAnimation.start(getChildAt(0).getTop(), getChildAt(3).getBottom()
							- getIconPadding(), DURATION_EDIT_ANIM);
				}
				break;
			case 8 :
				if (mStatus == DRAW_STATUS_EDITING) {
					mEditAnimation.start(getChildAt(5).getTop(), getChildAt(5).getBottom()
							- getIconPadding(), DURATION_EDIT_ANIM);
				}
				break;
			default :
				break;
		}
		updateStatus(DRAW_STATUS_START_EDIT);
	}

	private void updateStatus(int status) {
		if (status == DRAW_STATUS_NORMAL && getChildAt(0) != null) {
			int value = getChildAt(0).getTop() - getIconPadding();
			mEditAnimation.setValue(value);
			mEditAnimation.setDstValue(value);
		}
		mStatus = status;
		invalidate();
	}
	
	public void endEditAnimation() {
		if (mStatus == DRAW_STATUS_NORMAL || GLAppFolder.getInstance().isFolderOpened()) {
			updateStatus(DRAW_STATUS_NORMAL);
			return;
		}
		int childCount = getChildCount();
		if (childCount > FolderConstant.MAX_ICON_COUNT) {
			mEditAnimation.start(getChildAt(3).getBottom(), getChildAt(0).getTop()
					- getIconPadding(), DURATION_EDIT_ANIM);
			updateStatus(DRAW_STATUS_END_EDIT);
		} else if (childCount == FolderConstant.MAX_ICON_COUNT) {
			mEditAnimation.start(getChildAt(0).getBottom(), getChildAt(0).getTop()
					- getIconPadding(), DURATION_EDIT_ANIM);
			updateStatus(DRAW_STATUS_END_EDIT);
		} else {
			updateStatus(DRAW_STATUS_NORMAL);
		}
	}
	public void addIconBitmap(List<Bitmap> bitmaps, int iconSize) {
		mPreChildCount = getChildCount();
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).cleanup();
		}
		removeAllViews();
		if (bitmaps.size() < 4) {
			updateStatus(DRAW_STATUS_NORMAL);
		}
		final float first = iconSize * 0.12f;
		final float grap = iconSize * 0.015f;
		final int innerIconSize = (int) (iconSize - first * 2 - grap * 2) / 2;
		for (int i = 0; i < bitmaps.size(); i++) {
			GLImageView folderInsideIcon = new GLImageView(mContext);
			LayoutParams layoutParams = new LayoutParams(innerIconSize, innerIconSize);
			addView(folderInsideIcon, layoutParams);
			folderInsideIcon.setImageBitmap(bitmaps.get(i));
			folderInsideIcon.setScaleType(ScaleType.CENTER_INSIDE);
		}
	}
	
	private int getIconPadding() {
		GLModel3DView glModel3DView = (GLModel3DView) getGLParent();
		int folderSize = glModel3DView.getIconWidth();
		final int offset = (int) (folderSize * FolderElementLayout.GRAP_MULITIPLE);
		return offset;
	}
	/**
	 * 
	 * @author dingzijian
	 *
	 */
	public interface EditAnimationListener {
		public void onEditAnimationFinish();
	}
	
	protected boolean isEditStatus() {
		return mStatus == DRAW_STATUS_EDITING;
	}
	
	public void resetElementTranslate() {
		int value = getChildAt(3).getBottom() - getIconPadding();
		mEditAnimation.setValue(value);
		mEditAnimation.setDstValue(value);
	}
	
	public void resetElementStatus() {
		updateStatus(DRAW_STATUS_NORMAL);
	}
	
	public boolean isAnimating() {
		return mIsAnimating;
	}

	public void setEditAnimationListener(EditAnimationListener mEditAnimationListener) {
		this.mEditAnimationListener = mEditAnimationListener;
	}
}
