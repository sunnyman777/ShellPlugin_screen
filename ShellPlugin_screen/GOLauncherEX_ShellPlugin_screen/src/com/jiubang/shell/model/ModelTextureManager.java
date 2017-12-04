package com.jiubang.shell.model;

import java.util.HashMap;
import java.util.Set;

import android.content.Context;

import com.go.gl.graphics.TextureListener;
import com.go.gl.graphics.TextureManager;
import com.jiubang.shell.analysis3dmode.TextureFactory;
import com.jiubang.shell.analysis3dmode.TextureInfo;

/**
 * 统一管理模型的纹理
 * 
 * @author chaoziliang
 */
public class ModelTextureManager implements TextureListener {

	/**
	 * 保存所有模型的纹理信息
	 */
	private HashMap<String, TextureInfo> mModelViewTextures = new HashMap<String, TextureInfo>();

	private static ModelTextureManager sInstance = null;

	private ModelTextureManager() {
	};

	public static synchronized ModelTextureManager getInstance() {
		if (sInstance == null) {
			sInstance = new ModelTextureManager();
		}

		return sInstance;
	}

	@Override
	public void onTextureInvalidate() {

		Set<String> keys = mModelViewTextures.keySet();
		for (String key : keys) {
			TextureInfo info = mModelViewTextures.get(key);
			if (info != null) {
				info.mTexID = TextureInfo.INVALID_TEXTURE;
			}
		}
	}

	public void setTextureInfo(String key, TextureInfo info) {

		TextureInfo tempInfo = mModelViewTextures.get(key);

		if (tempInfo == null) {
			mModelViewTextures.put(key, info);
			info.mReferCount++;
		} else {
			tempInfo.mReferCount++;
		}
	}

	public TextureInfo getTextureInfo(String key) {
		return mModelViewTextures.get(key);
	}

	public boolean getTexture(Set<String> keyS, Context content) {
		boolean isGenTexure = false;

		for (String key : keyS) {
			TextureInfo info = mModelViewTextures.get(key);
			if (info != null && info.mTexID == TextureInfo.INVALID_TEXTURE && info.bitmap != null) {
				info.mTexID = TextureFactory.getTexture(content, info.bitmap);
				isGenTexure = true;
			}
		}

		return isGenTexure;
	}

	/**
	 * 删除纹理
	 * @param key
	 */
	public void removeTexture(String key) {
		TextureInfo info = mModelViewTextures.get(key);
		if (info != null) {
			info.mReferCount--;
			if (info.mReferCount == 0) {
				TextureManager.getInstance().deleteTexture(info.mTexID);
				mModelViewTextures.remove(key);
			}
		}
	}

	/**
	 * 批量删除删除纹理
	 * @param key
	 */
	public void removeTextures(Set<String> keys) {
		for (String key : keys) {
			removeTexture(key);
		}
	}

}
