package com.jiubang.shell.analysis3dmode;

import android.graphics.Bitmap;

/**
 * 
 * <br>类描述:纹理基本结构
 * <br>功能详细描述:
 * 
 * @author  liuheng
 * @date  [2012-9-4]
 */
public class TextureInfo {
	public static final int INVALID_TEXTURE = -1;
	/**
	 * 纹理id
	 */
	public int mTexID = INVALID_TEXTURE;

	/**
	 * 引用次数
	 */
	public int mReferCount = 0;

	/**
	 * 纹理图片,只是索引，不管理它的生命周期
	 */
	public Bitmap bitmap = null;
}
