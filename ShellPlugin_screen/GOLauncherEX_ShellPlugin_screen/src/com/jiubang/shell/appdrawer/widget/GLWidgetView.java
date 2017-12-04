package com.jiubang.shell.appdrawer.widget;

import java.lang.ref.SoftReference;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.graphics.TextureListener;
import com.go.gl.graphics.TextureManager;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.widget.GLTextViewWrapper;
import com.go.util.BroadCaster.BroadCasterObserver;
import com.go.util.file.media.MediaThreadPoolManager;
import com.go.util.graphics.BitmapUtility;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.components.sidemenuadvert.widget.SideWidgetDataInfo;
import com.jiubang.ggheart.components.sidemenuadvert.widget.SideWidgetInfo;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.shell.common.component.SoftReferenceBitmapGLDrawable;
/**
 * 侧边栏内widget图标
 * @author wuziyi
 *
 */
public class GLWidgetView extends GLFrameLayout implements BroadCasterObserver, TextureListener {

	private GLTextViewWrapper mText;
	private Context mContext;
	private GLWidgetImageManager mWidgetImageManager;
	private boolean mShow;
	private boolean mPreShow;
	private GLDrawable mDefaultGLDrawable;
	private SoftReference<SoftReferenceBitmapGLDrawable> mContentImageRef = null;
	private SoftReferenceBitmapGLDrawable mContentImage = null;
	private Rect mContentImageRegion;
	/**
	 * 图片是否需要从网络获取（本地无法获取）
	 */
	private boolean mIsLoadIconByNet = false;
	/**
	 * 是否正在加载网络图片
	 */
	private boolean mIsLoaddingIconByNet = false;

	public GLWidgetView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public GLWidgetView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	private void init() {
		mWidgetImageManager = GLWidgetImageManager.getInstance(mContext);
		mContentImageRegion = new Rect();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int padding = DrawUtils.dip2px(4.5f);
		int paddingBottom = DrawUtils.dip2px(20.5f);
		mContentImageRegion.set(padding, padding, right - left - padding, bottom - top
				- paddingBottom);
		updateContentImageRefBounds(mDefaultGLDrawable);
		updateContentImageRefBounds(mContentImage);
		if (mContentImageRef != null && mContentImageRef.get() != null) {
			updateContentImageRefBounds(mContentImageRef.get());
		}
	}

	@Override
	protected void onFinishInflate() {
		mText = (GLTextViewWrapper) findViewById(R.id.appdrawer_slide_menu_widget_text);
		mText.showTextShadow();
	}

	@Override
	public void draw(GLCanvas canvas) {
		super.draw(canvas);
		if (mContentImage != null) {
			mContentImage.draw(canvas);
		} else if (mContentImageRef != null) {
			SoftReferenceBitmapGLDrawable drawable = mContentImageRef.get();
			if (drawable != null) {
				drawable.draw(canvas);
			} else {
				drawDefaultDrawable(canvas);
			}
		} else {
			drawDefaultDrawable(canvas);
		}
	}

	private void drawDefaultDrawable(GLCanvas canvas) {
		if (mDefaultGLDrawable != null) {
			mDefaultGLDrawable.draw(canvas);
		}
	}

	public void setDefaultDrawable(GLDrawable drawable) {
		if (drawable != null) {
			if (mDefaultGLDrawable != null) {
				mDefaultGLDrawable.clear();
			}
			mDefaultGLDrawable = drawable;
		}
	}

	public void startLoadIcon() {
		SideWidgetInfo info = (SideWidgetInfo) getTag();
		if (info instanceof SideWidgetDataInfo) {
			if (!mIsLoadIconByNet) {
				SoftReferenceBitmapGLDrawable drawable = mWidgetImageManager.getPackagePreView(
						this, info.getWidgetPkgName(), mContentImageRegion.bottom
								- mContentImageRegion.top, mContentImageRegion.right
								- mContentImageRegion.left);
				if (drawable != null) {
					setContentImage(drawable);
				}
			} else { // 如果已经加载过一次，本地不存在，则直接在网络获取
				loadIconInNet();
			}
		}
	}

	private void setContentImage(Bitmap bitmap) {
		try {
			setContentImage(new SoftReferenceBitmapGLDrawable(mContext.getResources(), bitmap));
		} catch (OutOfMemoryError e) {

		}
	}

	private void setContentImage(SoftReferenceBitmapGLDrawable drawable) {
		if (drawable != null) {
			updateContentImageRefBounds(drawable);
			mIsLoaddingIconByNet = false;
			drawable.registerTextureListener(this);
			if (mShow) {
				mContentImage = drawable;
				mContentImageRef = null;
			} else {
				mContentImage = null;
				mContentImageRef = new SoftReference<SoftReferenceBitmapGLDrawable>(drawable);
			}
			postInvalidate();
		}
	}

	private void updateContentImageReference() {
		if (mContentImageRef != null && mContentImageRef.get() == null) {
		}
		if (mShow/* || mPreShow*/ && mContentImage == null
				&& (mContentImageRef == null || mContentImageRef.get() == null)) {
			startLoadIcon();
		} else {
			if (mShow) {
				if (mContentImage == null && mContentImageRef != null) {
					mContentImage = mContentImageRef.get();
					mContentImageRef = null;
				}
			} else {
				if (mContentImage != null && mContentImageRef == null) {
					mContentImageRef = new SoftReference<SoftReferenceBitmapGLDrawable>(
							(SoftReferenceBitmapGLDrawable) mContentImage);
					mContentImage = null;
				}
			}
		}
	}

	private void updateContentImageRefBounds(GLDrawable drawable) {
		if (drawable != null) {
			int width = mContentImageRegion.right - mContentImageRegion.left;
			int height = mContentImageRegion.bottom - mContentImageRegion.top;
			int drawableW = drawable.getIntrinsicWidth();
			int drawableH = drawable.getIntrinsicHeight();
			float centerX = mContentImageRegion.left + width / 2.0f;
			float centerY = mContentImageRegion.top + height / 2.0f;
			float scaleH = 1.0f * height / drawableH;
			float scaleW = 1.0f * width / drawableW;
			float scale = scaleH > scaleW ? scaleW : scaleH;
			if (scale > 1.0f) { // 图片比较小的时候不会拉大，如果要拉大，则去掉这段
				scale = 1.0f;
			}
			int left = (int) (centerX - drawableW * scale / 2.0f);
			int top = (int) (centerY - drawableH * scale / 2.0f);
			int right = (int) (centerX + drawableW * scale / 2.0f);
			int bottom = (int) (centerY + drawableH * scale / 2.0f);
			drawable.setBounds(left, top, right, bottom);
		}
	}

	public GLTextViewWrapper getText() {
		return mText;
	}

	public void setText(String string) {
		mText.setText(string);
	}

	@Override
	public void onBCChange(int msgId, int param, Object ...object) {
		SideWidgetInfo info = (SideWidgetInfo) getTag();
		//include info and widgetpkname null checking for avoid null exception
		if (info != null && info.getWidgetPkgName() != null && info.getWidgetPkgName().equals(object[0])) {
			if (info instanceof SideWidgetDataInfo) {
				if ((msgId == GLWidgetImageManager.MSG_ID_LOAD_IMAGE_COMPLETED)
						&& object[1] != null) { // 拿得到就说明已经安装好了，不需要过滤颜色，也不需要用默认图片了
					SoftReferenceBitmapGLDrawable drawable = (SoftReferenceBitmapGLDrawable) ((List) object[1]).get(0);
					setContentImage(drawable);
				} else if (msgId == GLWidgetImageManager.MSG_ID_LOAD_IMAGE_FAILED_BY_NAME_NO_FOUND) {
					// 本地拿不到则从网络获取图片
					mIsLoadIconByNet = true;
					loadIconInNet();
				}
			}
		}
	}

	/**
	 * 从服务器获取图标
	 */
	public void loadIconInNet() {
		SideWidgetInfo infoBean = (SideWidgetInfo) getTag();
		if (infoBean instanceof SideWidgetDataInfo && !mIsLoaddingIconByNet) { // 不是正在加载网络图片才进行加载)
			mIsLoaddingIconByNet = true;
			final SideWidgetDataInfo normalWidget = (SideWidgetDataInfo) infoBean;
			String imgUrl = normalWidget.getPreViewUrl();
			String imgPath = LauncherEnv.Path.SIDEMENU_WIDGET_PATH;
			String imgName = String.valueOf(normalWidget.getPreViewName()) + ".png";
			Bitmap bm = mWidgetImageManager.loadImage(imgPath, imgName, imgUrl, true, null,
					new AsyncImageLoadedCallBack() {
						@Override
						public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
							if (normalWidget.getPreViewUrl().equals(imgUrl)) {
								if (!normalWidget.isIsInstalled()) {
									setContentImageWithGray(imageBitmap);
								} else {
									setContentImage(imageBitmap);
								}
							} else {
								imageBitmap = null;
								imgUrl = null;
							}
						}
					});
			if (bm != null) {
				if (!normalWidget.isIsInstalled()) {
					setContentImageWithGray(bm);
				} else {
					setContentImage(bm);
				}
			}
		}
	}
	
	private void setContentImageWithGray(final Bitmap bitmap) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
				Bitmap bm = BitmapUtility.filteColor(bitmap);
				if (bm != null) {
					setContentImage(bm);
				} else {
					mIsLoaddingIconByNet = false;
				}
			}
		};
		MediaThreadPoolManager.getInstance(GLWidgetImageManager.THREAD_POOL_MANAGER_NAME).execute(
				runnable);
	}
	
	private void cancelLoadIcon() {
		// 目前只是取消本地load图，网络获取的无法取消
		SideWidgetInfo info = (SideWidgetInfo) getTag();
		GLWidgetImageManager.getInstance(mContext).cancelLoadThumbnail(info.getWidgetPkgName());
	}

	/**
	 * 正在显示
	 */
	public void onShow() {
		mShow = true;
		mPreShow = false;
		updateContentImageReference();
	}

	/**
	 * 退出显示与预加载
	 */
	public void onHide() {
		mShow = false;
		mPreShow = false;
		cancelLoadIcon();
		updateContentImageReference();
	}

	/**
	 * 预加载缩略图
	 */
	public void preShow() {
		mShow = false;
		mPreShow = true;
		updateContentImageReference();
	}
	
	@Override
	public void cleanup() {
		super.cleanup();
		if (mDefaultGLDrawable != null) {
			mDefaultGLDrawable.clear();
			mDefaultGLDrawable = null;
		}
		if (mContentImage != null) {
			mContentImage.clear();
			mContentImage = null;
		}
		if (mContentImageRef != null) {
			GLDrawable drawable = mContentImageRef.get();
			if (drawable != null) {
				drawable.clear();
			}
			mContentImageRef = null;
		}
		TextureManager.getInstance().unRegisterTextureListener(this);
	}

	@Override
	public void onTextureInvalidate() {
		if (mContentImage != null) {
			mContentImage.onTextureInvalidate();
		}
		if (mContentImageRef != null) {
			SoftReferenceBitmapGLDrawable drawable = mContentImageRef.get();
			if (drawable != null) {
				drawable.onTextureInvalidate();
			}
		}
	}
}
