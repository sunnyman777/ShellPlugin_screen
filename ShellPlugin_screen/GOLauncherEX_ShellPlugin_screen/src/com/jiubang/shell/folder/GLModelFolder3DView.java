package com.jiubang.shell.folder;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.Gravity;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.widget.GLImageView;
import com.jiubang.shell.common.component.GLModel3DView;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.component.IconView.IconRefreshObserver;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-2-21]
 */
public class GLModelFolder3DView extends GLModel3DView implements IconRefreshObserver {

	public FolderElementLayout mIconViewLayout;
	public GLModelFolder3DView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GLModelFolder3DView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	@Override
	protected void onFinishInflate() {
		mIconViewLayout = new FolderElementLayout(mContext);
		mIconViewLayout.setHasPixelOverlayed(false);
		android.widget.FrameLayout.LayoutParams layoutParams = new android.widget.FrameLayout.LayoutParams(
				getIconWidth(), getIconWidth(), Gravity.CENTER);
		addView(mIconViewLayout, layoutParams);
		super.onFinishInflate();
	}

	public <T> void addIconView(List<Bitmap> iconBitmaps, int iconSize) {
		mIconViewLayout.addIconBitmap(iconBitmaps, iconSize);
		//		mIconViewLayout.rebuildCacheDrawable();
	}

	@Override
	public void setColorFilter(int srcColor, Mode mode) {
		super.setColorFilter(srcColor, mode);
		mIconViewLayout.setColorFilter(srcColor, mode);
	}
	@Override
	public void setAlphaFilter(int alpha) {
		super.setAlphaFilter(alpha);
		mIconViewLayout.setAlpha(alpha);
	}
	@Override
	public void setAlpha(int alpha) {
		super.setAlpha(alpha);
		mIconViewLayout.setAlpha(alpha);
	}

	@Override
	public void onIconRefresh() {
		mIconViewLayout.rebuildCacheDrawable();
	}
	
	public void setFolderThumbnailVisible(boolean visible) {
		mIconViewLayout.setChildrenVisible(visible);
	}
	
	@Override
	public void setIconWidth(int iconWidth) {
		mIconWidth = iconWidth;
		if (mGlBitmap != null) {
			mGlBitmap.setBounds(0, 0, mIconWidth, mIconWidth);
		}
		android.widget.FrameLayout.LayoutParams layoutParams = (android.widget.FrameLayout.LayoutParams) mIconViewLayout
				.getLayoutParams();
		layoutParams.width = mIconWidth;
		layoutParams.height = mIconWidth;
		requestLayout();
	}
	
	public IconView getElement(Object info) {
		int count = mIconViewLayout.getChildCount();
		for (int i = 0; i < count; i++) {
			IconView icon = (IconView) mIconViewLayout.getChildAt(i);
			if (icon.getInfo() == info) {
				return icon;
			} 
		}
		return null;
	}
	
	public GLImageView getElement(int location) {
		int count = mIconViewLayout.getChildCount();
		if (location >= count) {
			location = count - 1;
		}
		GLImageView icon = (GLImageView) mIconViewLayout.getChildAt(location);
		return icon;
	}
	
	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		drawChild(canvas, mIconViewLayout, getDrawingTime());
	}
	
	public void startEditAnimation() {
		mIconViewLayout.startEditAnimation();
	}
	
	public void endEditAnimation() {
		mIconViewLayout.endEditAnimation();
	}
	
	public int getFolderChildCount() {
		return mIconViewLayout.getChildCount();
	}
	
	public boolean isEditStatus() {
		return mIconViewLayout.isEditStatus();
	}
	
	public void resetElementTranslate() {
		mIconViewLayout.resetElementTranslate();
	}
	
	public void resetElementStatus() {
		mIconViewLayout.resetElementStatus();
	}
	
	public void setEditAnimationListener(BaseFolderIcon baseFolderIcon) {
		mIconViewLayout.setEditAnimationListener(baseFolderIcon);
	}
	
	/**
	 * 获得文件夹中小图标与正常图标的缩小比例
	 * @return
	 */
	public float[] getThumbnailScaleXY() {
		return mIconViewLayout.getScaleXY();
	}

	/**
	 * 获得文件夹内小图标目标位置中心点<br>
	 * 注意：必须保证已经往FolderIcon里添加了该view
	 * @param target 目标位置
	 * @return
	 */
	public void getThumbnailLocationCenter(int target, int[] locate) {
		mIconViewLayout.getChildLocation(target, locate);
	}
	
}
