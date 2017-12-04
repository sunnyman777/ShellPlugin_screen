package com.jiubang.shell.appdrawer.widget;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.go.gl.graphics.GLDrawable;
import com.go.util.BroadCaster;
import com.go.util.file.media.MediaThreadPoolManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncNetBitmapOperator;
import com.jiubang.ggheart.appgame.base.manage.LruImageCache;
import com.jiubang.ggheart.apps.gowidget.GoWidgetConstant;
import com.jiubang.shell.common.component.SoftReferenceBitmapGLDrawable;

/**
 * Widget预览图加载管理类
 * @author yejijiong
 *
 */
public class GLWidgetImageManager extends BroadCaster {

	public static final String THREAD_POOL_MANAGER_NAME = "Widget_Image_Thread_Pool";
	public static final String ID_KEY = "_packageName";
	public static final int MSG_ID_LOAD_IMAGE_COMPLETED = 0;
	public static final int MSG_ID_LOAD_IMAGE_FAILED_BY_NAME_NO_FOUND = 1;
	public static final int MSG_ID_LOAD_IMAGE_FAILED_BY_OTHER_EXCEPTION = 2;
	private Handler mHandler;
	private Context mContext;
	private ConcurrentHashMap<String, Runnable> mLoadingImageRunableHashMap = new ConcurrentHashMap<String, Runnable>();
	private ConcurrentHashMap<String, SoftReference<SoftReferenceBitmapGLDrawable>> mImageThumbnailMap = new ConcurrentHashMap<String, SoftReference<SoftReferenceBitmapGLDrawable>>();
	private ConcurrentHashMap<String, BroadCasterObserver> mObserverHashMap = new ConcurrentHashMap<String, BroadCasterObserver>();
	private AsyncImageManager mAsyncImageManager;

	private static GLWidgetImageManager sInstance;
	
	private GLWidgetImageManager(Context context) {
		mContext = context;
		initHandler();
		mAsyncImageManager = new AsyncImageManager(new LruImageCache(LruImageCache.DEFAULT_SIZE));
	}
	
	public static GLWidgetImageManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new GLWidgetImageManager(context);
		}
		return sInstance;
	}
	
	/**
	 * 从widget的包中获取预览图
	 * @param observer
	 * @param packageName
	 * @return
	 */
	public SoftReferenceBitmapGLDrawable getPackagePreView(BroadCasterObserver observer, String packageName, int imgHeight, int imgWidth) {
		SoftReferenceBitmapGLDrawable drawable = null;
		if (mImageThumbnailMap.containsKey(packageName)) {
			SoftReference<SoftReferenceBitmapGLDrawable> ref = mImageThumbnailMap.get(packageName);
			if (ref != null) {
				drawable = ref.get();
			}
		}
		if (drawable == null) {
			registerObserver(observer);
			mObserverHashMap.put(packageName, observer);
			startLoadPackagePreView(packageName, imgHeight, imgWidth);
		}
		return drawable;
	}
	
	private void initHandler() {
		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				String packageName = msg.getData().getString(ID_KEY);
				switch (msg.what) {
				case MSG_ID_LOAD_IMAGE_COMPLETED:
					SoftReferenceBitmapGLDrawable bitmap = (SoftReferenceBitmapGLDrawable) msg.obj;
					SoftReference<SoftReferenceBitmapGLDrawable> imageRef = new SoftReference<SoftReferenceBitmapGLDrawable>(
						bitmap);
					if (mImageThumbnailMap.containsKey(packageName)) {
						mImageThumbnailMap.replace(packageName, imageRef);
					} else {
						mImageThumbnailMap.put(packageName, imageRef);
					}
					mLoadingImageRunableHashMap.remove(packageName);
					List<Object> objs = new ArrayList<Object>();
					objs.add(bitmap);
					broadCast(
							msg.what, -1, packageName, objs);
					unRegisterBroadCasterObserver(packageName);
					break;
				case MSG_ID_LOAD_IMAGE_FAILED_BY_NAME_NO_FOUND:
				case MSG_ID_LOAD_IMAGE_FAILED_BY_OTHER_EXCEPTION:
					mLoadingImageRunableHashMap.remove(packageName);
					broadCast(msg.what,
							-1, packageName, null);
					unRegisterBroadCasterObserver(packageName);
					break;
				default:
					break;
				}
			}
		};
	}
	
	private void unRegisterBroadCasterObserver(String packageName) {
		if (mObserverHashMap != null && mObserverHashMap.containsKey(packageName)) {
			BroadCasterObserver observer = mObserverHashMap.get(packageName);
			unRegisterObserver(observer);
			mObserverHashMap.remove(packageName);
		}
	}
	
	private void startLoadPackagePreView(final String packageName, final int imgHeight, final int imgWidth) {
		if (mLoadingImageRunableHashMap != null
				&& !mLoadingImageRunableHashMap.containsKey(packageName)) {
			mLoadingImageRunableHashMap.put(packageName, new Runnable() {
				@Override
				public void run() {
					android.os.Process
							.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
					Bundle bundle = new Bundle();
					bundle.putString(ID_KEY, packageName);
					try {
						Resources resources = mContext.getPackageManager().getResourcesForApplication(packageName);
						// 获取图片
						int drawableList = resources.getIdentifier(GoWidgetConstant.NEW_PREVIEW_LIST, "array",
								packageName);
						if (drawableList <= 0) {
							drawableList = resources.getIdentifier(GoWidgetConstant.PREVIEW_LIST, "array",
									packageName);
						}
						if (drawableList > 0) {
							final String[] extras = resources.getStringArray(drawableList);
							for (String extra : extras) {
								int res = resources.getIdentifier(extra, "drawable", packageName);
								if (res != 0) {
									if (mHandler != null) {
										Message message = mHandler
												.obtainMessage(MSG_ID_LOAD_IMAGE_COMPLETED);
										message.obj = getScaleImage(resources, res, imgHeight,
												imgWidth);
										message.setData(bundle);
										mHandler.sendMessage(message);
									}
									break;
								}
							}
						}
					} catch (NameNotFoundException e) {
						e.printStackTrace();
						if (mHandler != null) {
							Message message = mHandler
									.obtainMessage(MSG_ID_LOAD_IMAGE_FAILED_BY_NAME_NO_FOUND);
							message.setData(bundle);
							mHandler.sendMessage(message);
						}
					} catch (Throwable e) {
						e.printStackTrace();
						if (mHandler != null) {
							Message message = mHandler
									.obtainMessage(MSG_ID_LOAD_IMAGE_FAILED_BY_OTHER_EXCEPTION);
							message.setData(bundle);
							mHandler.sendMessage(message);
						}
					}
				}
			});
			MediaThreadPoolManager.getInstance(THREAD_POOL_MANAGER_NAME)
			.execute(mLoadingImageRunableHashMap.get(packageName));
		}
	}
	
	private SoftReferenceBitmapGLDrawable getScaleImage(Resources res, int resId, int imgHeight, int imgWidth) {
		Bitmap bitmap = null;
		Options options = new Options();
		options.inSampleSize = 1;
		options.inJustDecodeBounds = true;
		bitmap = BitmapFactory.decodeResource(res, resId, options);
		float scaleH = options.outHeight / imgHeight;
		float scaleW = options.outWidth / imgWidth;
		float scale = scaleH > scaleW ? scaleW : scaleH;
		options.inSampleSize = Math.round(scale);
		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeResource(res, resId, options);
		return new SoftReferenceBitmapGLDrawable(mContext.getResources(), bitmap); 
	}
	
	public synchronized static void destory() {
		if (sInstance != null) {
			sInstance.clearAllObserver();
			for (SoftReference<SoftReferenceBitmapGLDrawable> ref : sInstance.mImageThumbnailMap.values()) {
				GLDrawable drawable = ref.get();
				if (drawable != null) { // 显示调用清除纹理方法
					drawable.clear();
				}
			}
			sInstance.mImageThumbnailMap.clear();
			Collection<Runnable> loadingRunables = sInstance.mLoadingImageRunableHashMap
					.values();
			for (Runnable runnable : loadingRunables) {
				MediaThreadPoolManager.getInstance(THREAD_POOL_MANAGER_NAME)
						.cancel(runnable);
			}
			sInstance.mLoadingImageRunableHashMap.clear();
			sInstance.mObserverHashMap.clear();
			sInstance.mAsyncImageManager.clear(); // 可能存在泄漏
			sInstance.mContext = null;
			sInstance = null;
		}
	}
	
	/**
	 * 取消预览图加载
	 * 
	 * @param type
	 * @param id
	 */
	public void cancelLoadThumbnail(String packageName) {
		if (mLoadingImageRunableHashMap != null) {
			Runnable runnable = mLoadingImageRunableHashMap.remove(packageName);
			if (runnable != null) {
				MediaThreadPoolManager.getInstance(THREAD_POOL_MANAGER_NAME)
						.cancel(runnable);
			}
		}
		unRegisterBroadCasterObserver(packageName);
	}
	
	public Bitmap loadImage(final String imgPath, final String imgName, final String imgUrl,
			final boolean isCache, final AsyncNetBitmapOperator operator,
			final AsyncImageLoadedCallBack callBack) {
		return mAsyncImageManager.loadImage(imgPath, imgName, imgUrl, isCache, operator, callBack);
	}
	
}
