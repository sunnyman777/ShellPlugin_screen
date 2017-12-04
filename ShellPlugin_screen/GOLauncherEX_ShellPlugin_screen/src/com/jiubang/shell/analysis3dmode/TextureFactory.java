package com.jiubang.shell.analysis3dmode;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * 
 * <br>类描述:纹理工厂
 * <br>功能详细描述:
 * 
 * @author  liuheng
 * @date  [2012-9-4]
 */
public class TextureFactory {
	public static int getTexture(Context context, int resID) {
		return getTexture(context, resID, GLES20.GL_CLAMP_TO_EDGE,
		        GLES20.GL_CLAMP_TO_EDGE);
	}

	public static int getTexture(Context context, Bitmap bitmap) {
		return getTexture(context, bitmap, GLES20.GL_REPEAT, GLES20.GL_REPEAT);
	}

	/**
	 * 创建一个纹理对象
	 * 
	 * @param context
	 *            - 应用程序环境
	 * @param resID
	 *            - R.java中的资源ID
	 * @param wrap_s_mode
	 *            - 纹理环绕S模式
	 * @param wrap_t_mode
	 *            - 纹理环绕T模式
	 * @return 申请好的纹理ID
	 */
	public static int getTexture(Context context, int resID, int wrap_s_mode,
	        int wrap_t_mode) {
		// 申请一个纹理对象ID
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		// 绑定这个申请来的ID为当前纹理操作对象
		int textureID = textures[0];
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);

		// 开始载入纹理
		InputStream is = context.getResources().openRawResource(resID);
		Bitmap bitmap;
		try {
			bitmap = BitmapFactory.decodeStream(is);
		}
		finally {
			try {
				is.close();
			}
			catch (IOException e) {
				// Ignore.
			}
		}

		// 绑定到纹理
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();

		// 设置当前纹理对象的过滤模式
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
		        GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
		        GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		// 设置环绕模式
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
		        wrap_s_mode);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
		        wrap_t_mode);

		return textureID;
	}

	public static int getTexture(Context context, Bitmap bitmap,
	        int wrap_s_mode, int wrap_t_mode) {
		// 申请一个纹理对象ID
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		// 绑定这个申请来的ID为当前纹理操作对象
		int textureID = textures[0];
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);

		// 绑定到纹理
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

		// 设置当前纹理对象的过滤模式
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
		        GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
		        GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		// 设置环绕模式
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
		        wrap_s_mode);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
		        wrap_t_mode);

		return textureID;
	}

}
