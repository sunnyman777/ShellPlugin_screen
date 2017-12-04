package com.jiubang.shell.appdrawer.search;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.gau.golauncherex.plugin.shell.R;
import com.go.proxy.ApplicationProxy;
import com.go.util.file.media.AudioFile;
import com.go.util.file.media.FileInfo;
import com.go.util.file.media.ImageFile;
import com.go.util.file.media.MediaBroadCaster.MediaBroadCasterObserver;
import com.go.util.file.media.ThumbnailManager;
import com.jiubang.ggheart.launcher.IconUtilities;
import com.jiubang.shell.appdrawer.component.GLAppDrawerAppIcon;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
/**
 * 
 * @author dingzijian
 *
 */
public class GLSearchMediaGridIconInfo implements MediaBroadCasterObserver {
	private String mMediaType;

	private GLAppDrawerAppIcon mAppIcon;
	
	private FileInfo mFileInfo;
	
	private BitmapDrawable mCover;
	
	private BitmapDrawable mMask;
	
	private BitmapDrawable mDefaultImage;
	
	private BitmapDrawable mDefaultAudio;
	
	private BitmapDrawable mDefaultVideo;
	
	
	public GLSearchMediaGridIconInfo(GLAppDrawerAppIcon appIcon) {
		mAppIcon = appIcon;
		mMask = (BitmapDrawable) ShellAdmin.sShellManager.getContext().getResources()
				.getDrawable(R.drawable.gl_search_media_icon_mask);
		mDefaultImage = (BitmapDrawable) ApplicationProxy.getContext().getResources()
				.getDrawable(com.gau.go.launcherex.R.drawable.app_func_search_result_image_icon);
		mDefaultAudio = (BitmapDrawable) ApplicationProxy.getContext().getResources()
				.getDrawable(com.gau.go.launcherex.R.drawable.app_func_search_result_audio_icon);
		mDefaultVideo = (BitmapDrawable) ApplicationProxy.getContext().getResources()
				.getDrawable(com.gau.go.launcherex.R.drawable.app_func_search_result_video_icon);
	}

	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		int picId = mFileInfo.thumbnailId;
		if (mMediaType == ThumbnailManager.TYPE_ALBUM) {
			picId = ((AudioFile) mFileInfo).albumId;
		}
		if (param == picId) {
			if (msgId == ThumbnailManager.MSG_ID_LOAD_IMAGE_COMPLETED && object != null) {
				setIcon((Bitmap) object);
			}
		}
	}

	public String getMediaType() {
		return mMediaType;
	}

	public void setMediaType(String mMediaType) {
		this.mMediaType = mMediaType;
	}

	public FileInfo getFileInfo() {
		return mFileInfo;
	}

	public void setFileInfo(FileInfo mFileInfo) {
		this.mFileInfo = mFileInfo;
	}
	
	
	/**
	 * 取消加载缩略图
	 * @param type
	 * @param id
	 */
	public void cancelLoadThumbnail() {
		ThumbnailManager manager = ThumbnailManager.getInstance(ShellAdmin.sShellManager
				.getContext());
		manager.cancelLoadThumbnail(this, mMediaType, mFileInfo.thumbnailId);
	}
	
	/**
	 * 绑定FileInfo
	 * @param info
	 */
	public void setFileItemFileInfo(FileInfo info) {
		if (info == null) {
			return;
		}
		// 当重新绑定新的FileInfo时取消上一次的缩略图加载
		if (mFileInfo != null && mMediaType != null && !mMediaType.equals("")) {
			cancelLoadThumbnail();
		}
		int res = -1;
		mFileInfo = info;
		if (mFileInfo instanceof AudioFile) { // 音乐
			mMediaType = ThumbnailManager.TYPE_ALBUM;
			res = R.drawable.gl_search_music_icon_cover;
			loadThumbnail(mMediaType, ((AudioFile) mFileInfo).albumId, mFileInfo.thumbnailPath);
		} else if (mFileInfo instanceof ImageFile) { // 图片
			mMediaType = ThumbnailManager.TYPE_IMAGE;
			res = R.drawable.gl_search_image_icon_cover_pic;
			loadThumbnail(mMediaType, mFileInfo.thumbnailId, mFileInfo.thumbnailPath);

		} else { // 视频
			mMediaType = ThumbnailManager.TYPE_VIDEO;
			res = R.drawable.gl_search_video_icon_cover;
			loadThumbnail(mMediaType, mFileInfo.thumbnailId, mFileInfo.thumbnailPath);
		}
		mCover = (BitmapDrawable) ShellAdmin.sShellManager.getContext().getResources()
				.getDrawable(res);
	}
	
	private void setIcon(final Bitmap bitmap) {
		final BitmapDrawable drawable = IconUtilities.composeAppIconDrawable(null, mCover,
				new BitmapDrawable(bitmap), mMask, 1);
		mAppIcon.setIcon(drawable);
	}

	/**
	 * 
	 * 加载缩略图
	 * @param type
	 * @param id
	 * @param filePath
	 * @param imgWidth
	 */
	private void loadThumbnail(String type, int id, String filePath) {
		Bitmap bitmap = ThumbnailManager.getInstance(ShellAdmin.sShellManager.getContext())
				.getThumbnail(this, type, id, filePath, mAppIcon.getIconSize());
		if (bitmap != null) {
			setIcon(bitmap);
		} else {
			BitmapDrawable bd = null;
			// 设置默认图片
			if (mMediaType == ThumbnailManager.TYPE_ALBUM) { // 使用默认音乐图标
				mAppIcon.setIcon(mDefaultAudio);
			} else if (mMediaType == ThumbnailManager.TYPE_IMAGE) { // 使用默认图片图标
				mAppIcon.setIcon(mDefaultImage);
			} else { // 使用默认视频图片
				mAppIcon.setIcon(mDefaultVideo);
			}
		}
	}
	
}
