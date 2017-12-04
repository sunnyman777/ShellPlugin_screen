package com.jiubang.shell.screenedit.tabs;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.go.gl.widget.GLImageView;

/**
 * 添加模块中管理图片异步加载
 * @author zouguiquan
 *
 */
public class ScreenEditImageLoader implements Callback {

	private static final String LOADER_THREAD_NAME = "ScreenEditImageLoader";

	private final Handler mMainThreadHandler = new Handler(this);
	private LoaderThread mLoaderThread;
	private IImageLoaderListener mListener;

	private int mFirstLoadPage;									//添加模块的某个Tab，开始时默认加载第几页的图片
	private int mCurrentLoadPage;								//添加模块的某个Tab，当前正在加载第几面的图片
	private int mPageSize;
	private int mTotalSize;
	private boolean mLoadingRequested;
	private boolean mPaused;

	private int mDefaultResourceId;
	private static final int MESSAGE_REQUEST_LOADING = 1;
	private static final int MESSAGE_PHOTOS_LOADED = 2;

	private final ConcurrentHashMap<Integer, BitmapHolder> mBitmapCache = new ConcurrentHashMap<Integer, BitmapHolder>();
	private final ConcurrentHashMap<GLImageView, Integer> mPendingRequests = new ConcurrentHashMap<GLImageView, Integer>();

	public ScreenEditImageLoader(int defaultResourceId) {
		mDefaultResourceId = defaultResourceId;
	}
	
	public void setDefaultResource(int defaultResourceId) {
		mDefaultResourceId = defaultResourceId;
	}

	public void setCurrentLoadPage(int currentLoadPage) {
		if (currentLoadPage < 0) {
			currentLoadPage = 0;
		}
		mCurrentLoadPage = currentLoadPage;
	}

	public void setFirstLoadPage(int firstLoadPage) {
		if (firstLoadPage < 0) {
			firstLoadPage = 0;
		}
		mFirstLoadPage = firstLoadPage;
		mCurrentLoadPage = firstLoadPage;
	}

	public void setPageSize(int pageSize) {
		mPageSize = pageSize;
	}
	
	public void setTotalSize(int totalSize) {
		mTotalSize = totalSize;
	}

	/**
	 * 加载某个位置上的图片
	 * @param view
	 * @param index
	 */
	public void loadImage(GLImageView view, int index) {

		int startLoadIndex = 0;
		int endLoadIndex = 0;

		if (mPageSize == 1) {
			startLoadIndex = index;
			endLoadIndex = startLoadIndex + mPageSize;
		} else {
			startLoadIndex = mCurrentLoadPage * mPageSize;
			endLoadIndex = startLoadIndex + mPageSize;
			endLoadIndex = Math.min(endLoadIndex, mTotalSize);
		}

		Log.d("zgq", "startLoadIndex = " + startLoadIndex + " endLoadIndex= " + endLoadIndex
				+ " index= " + index);

		if (index < startLoadIndex || index >= endLoadIndex) {
			return;
		}
		
		if (index < 0) {
			view.setImageResource(mDefaultResourceId);
			mPendingRequests.remove(view);
		} else {
			boolean loaded = loadCachedPhoto(view, index);
			if (loaded) {
				mPendingRequests.remove(view);
			} else {
				mPendingRequests.put(view, index);
				if (!mPaused) {
					requestLoading();
				}
			}
		}
	}
	
	public boolean isLoadFinish(int currentScreen) {
		int startLoadIndex = currentScreen * mPageSize;
		int endLoadIndex = startLoadIndex + mPageSize;
		endLoadIndex = Math.min(endLoadIndex, mTotalSize);

		for (int i = startLoadIndex; i < endLoadIndex; i++) {
			BitmapHolder holder = mBitmapCache.get(i);
			if (holder == null || holder.mBitmapRmef == null || holder.mBitmapRmef.get() == null) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 从缓存中查找图片
	 * @param view
	 * @param photoId
	 * @return
	 */
	private boolean loadCachedPhoto(GLImageView view, int photoId) {
		BitmapHolder holder = mBitmapCache.get(photoId);
		if (holder == null) {
			holder = new BitmapHolder();
			mBitmapCache.put(photoId, holder);
		} else if (holder.mState == BitmapHolder.LOADED) {
			if (holder.mBitmapRmef == null) {
				view.setImageResource(mDefaultResourceId);
				return true;
			}

			Bitmap bitmap = holder.mBitmapRmef.get();
			if (bitmap != null) {
				Log.d("zgq", "loadCachedPhoto photoId= " + photoId);
				
				view.setImageBitmap(bitmap);
				return true;
			}

			holder.mBitmapRmef = null;
		}

		view.setImageResource(mDefaultResourceId);
		holder.mState = BitmapHolder.NEEDED;
		return false;
	}

	public void stop() {
		pause();

		if (mLoaderThread != null) {
			mLoaderThread.quit();
			mLoaderThread = null;
		}

		mPendingRequests.clear();
		mBitmapCache.clear();
	}

	public void clear() {
		mCurrentLoadPage = 0;
		mPendingRequests.clear();
		mBitmapCache.clear();
	}

	public void pause() {
		mPaused = true;
	}

	public void resume() {
		mPaused = false;
		if (!mPendingRequests.isEmpty()) {
			requestLoading();
		}
	}

	private void requestLoading() {
		if (!mLoadingRequested) {
			mLoadingRequested = true;
			mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_LOADING);
		}
	}

	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case MESSAGE_REQUEST_LOADING : {
				mLoadingRequested = false;
				if (!mPaused) {
					if (mLoaderThread == null) {
						mLoaderThread = new LoaderThread();
						mLoaderThread.start();
					}

					mLoaderThread.requestLoading();
				}
				return true;
			}

			case MESSAGE_PHOTOS_LOADED : {
				if (!mPaused) {
					processLoadedImages();
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 把加载完的图片缓存起来
	 */
	private void processLoadedImages() {
		Iterator<GLImageView> iterator = mPendingRequests.keySet().iterator();
		while (iterator.hasNext()) {
			GLImageView view = iterator.next();
			int photoId = mPendingRequests.get(view);
			boolean loaded = loadCachedPhoto(view, photoId);
			if (loaded) {
				if (mListener != null) {
					mListener.onLoadImageFinish(photoId);
				}
				iterator.remove();
			}
		}

		if (!mPendingRequests.isEmpty()) {
			requestLoading();
		} else {
			if (mFirstLoadPage == mCurrentLoadPage && isLoadFinish(mFirstLoadPage)) {
				if (mListener != null) {
					mListener.onLoadRangeImageFinish();
				}
			}
		}
	}

	private void cacheBitmap(int id, Bitmap bitmap) {
		if (mPaused) {
			return;
		}

		BitmapHolder holder = new BitmapHolder();
		holder.mState = BitmapHolder.LOADED;
		if (bitmap != null) {
			holder.mBitmapRmef = new SoftReference<Bitmap>(bitmap);
		}
		mBitmapCache.put(id, holder);
	}

	private void obtainPhotoIdsToLoad(ArrayList<Integer> photoIds) {
		photoIds.clear();

		Iterator<Integer> iterator = mPendingRequests.values().iterator();
		while (iterator.hasNext()) {
			int id = iterator.next();
			BitmapHolder holder = mBitmapCache.get(id);
			if (holder != null && holder.mState == BitmapHolder.NEEDED) {
				holder.mState = BitmapHolder.LOADING;
				photoIds.add(id);
			}
		}
	}

	/**
	 * 加载图片的线程
	 * @author zouguiquan
	 *
	 */
	private class LoaderThread extends HandlerThread implements Callback {

		private final ArrayList<Integer> mPhotoIds = new ArrayList<Integer>();
		private Handler mLoaderThreadHandler;

		public LoaderThread() {
			super(LOADER_THREAD_NAME);
		}

		public void requestLoading() {
			if (mLoaderThreadHandler == null) {
				mLoaderThreadHandler = new Handler(getLooper(), this);
			}
			mLoaderThreadHandler.sendEmptyMessage(0);
		}

		public boolean handleMessage(Message msg) {
			handleLoadPhotos();
			mMainThreadHandler.sendEmptyMessage(MESSAGE_PHOTOS_LOADED);
			return true;
		}

		private void handleLoadPhotos() {
			obtainPhotoIdsToLoad(mPhotoIds);

			int count = mPhotoIds.size();
			if (count == 0) {
				return;
			}
			
			for (int i = 0; i < mPhotoIds.size(); i++) {
				int id = mPhotoIds.get(i);
				Bitmap bitmap = null;
				if (mListener != null) {
					bitmap = mListener.onLoadImage(id);
				}
				cacheBitmap(id, bitmap);
//				mPhotoIds.remove(Integer.valueOf(id));
			}
		}
	}

	public void release() {
		mBitmapCache.clear();
		mPendingRequests.clear();
	}

	/**
	 * 
	 * @author zouguiquan
	 *
	 */
	private static class BitmapHolder {
		private static final int NEEDED = 0;
		private static final int LOADING = 1;
		private static final int LOADED = 2;

		int mState;
		SoftReference<Bitmap> mBitmapRmef;
	}

	public void setImageLoaderListener(IImageLoaderListener listener) {
		mListener = listener;
	}

	/**
	 * 图片加载状态变化时与外界交互的接口
	 * @author zouguiquan
	 *
	 */
	public interface IImageLoaderListener {
		public Bitmap onLoadImage(int index);
		public void onLoadImageFinish(int index);
		public void onLoadRangeImageFinish();
	}
}
