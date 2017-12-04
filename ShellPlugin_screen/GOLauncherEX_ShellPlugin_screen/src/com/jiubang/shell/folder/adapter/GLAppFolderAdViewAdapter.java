package com.jiubang.shell.folder.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLImageView;
import com.go.gl.widget.GLTextViewWrapper;
import com.jiubang.ggheart.appgame.base.bean.AppDetailInfoBean;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.manage.LruImageCache;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
/**
 * 
 * @author dingzijian
 *
 */
public class GLAppFolderAdViewAdapter extends ShellBaseAdapter<AppDetailInfoBean> {
	private AsyncImageManager mAsyncImageManager;
	private AppDataEngine mDataEngine;

	public GLAppFolderAdViewAdapter(Context context, List<AppDetailInfoBean> infoList) {
		super(context, infoList);
		mAsyncImageManager = AsyncImageManager.getLruInstance(LruImageCache.DEFAULT_SIZE);
		mDataEngine = AppDataEngine.getInstance(mContext);
	}

	@Override
	public GLView getView(int pos, GLView convertVIew, GLViewGroup parent) {
		AppDetailInfoBean detailInfoBean = getItem(pos);
		GLLinearLayout view = (GLLinearLayout) getViewByItem(detailInfoBean);
		if (view == null) {
			view = (GLLinearLayout) mInflater
					.inflate(R.layout.gl_folder_ad_adapter_view_item, null);
			mViewHolder.put(detailInfoBean.mAppId + "", view);
		}
		GLImageView icon = (GLImageView) view.findViewById(R.id.folder_ad_item_img_view);
		GLTextViewWrapper title = (GLTextViewWrapper) view
				.findViewById(R.id.folder_ad_item_text_view);
		title.setText(detailInfoBean.mName);
		setIcon(icon, detailInfoBean.mIconUrl, LauncherEnv.Path.FOLDER_AD_DATA_ICON_CACHE_PATH,
				String.valueOf(detailInfoBean.mIconUrl.hashCode()), true);
		return view;
	}

	@Override
	public GLView getViewByItem(AppDetailInfoBean appBean) {
		return mViewHolder.get(appBean.mAppId + "");
	}

	@Override
	public GLView removeViewByItem(AppDetailInfoBean appBean) {
		return mViewHolder.remove(appBean.mAppId + "");
	}
	/**
	 * 读取图标，然后设到imageview里
	 */
	private void setIcon(final GLImageView imageView, String imgUrl, String imgPath,
			String imgName, boolean setDefaultIcon) {
		imageView.setTag(imgUrl);
		Bitmap bm = mAsyncImageManager.loadImage(imgPath, imgName, imgUrl, true, null,
				new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageView.getTag().equals(imgUrl)) {
							setImageBitmap(imageView, imageBitmap);
						} else {
							imageBitmap = null;
							imgUrl = null;
						}
					}
				});
		if (bm != null) {
			setImageBitmap(imageView, bm);
		} else {
			if (setDefaultIcon) {
				imageView.setImageResource(android.R.drawable.sym_def_app_icon);
			} else {
				imageView.setImageDrawable(null);
			}
		}
	}
	private void setImageBitmap(final GLImageView imageView, Bitmap imageBitmap) {
		BitmapDrawable icon = mDataEngine.createBitmapDrawable(new BitmapDrawable(imageBitmap));
		imageView.setImageDrawable(icon);
	}
}
