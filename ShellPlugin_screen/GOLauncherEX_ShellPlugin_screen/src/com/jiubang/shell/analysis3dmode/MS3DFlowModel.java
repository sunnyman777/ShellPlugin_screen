package com.jiubang.shell.analysis3dmode;


import java.io.InputStream;
import java.nio.FloatBuffer;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.go.gl.graphics.GLShaderProgram;
import com.go.gl.graphics.Texture;
import com.go.gl.graphics.TextureShader;

/** 
 * @author chendongcheng
 */
public class MS3DFlowModel extends Ms3DModel {

	private float[][] mInitTextureCoords;
	private GLShaderProgram mShader;
	
	public MS3DFlowModel(boolean recyleImgAfterLoaded) {
		super(recyleImgAfterLoaded);
		mShader = TextureShader.getShader(TextureShader.MODE_ALPHA);
	}

	public MS3DFlowModel(Bitmap[] textureImg, boolean recyleImgAfterLoaded) {
		super(textureImg, recyleImgAfterLoaded);
		mShader = TextureShader.getShader(TextureShader.MODE_ALPHA);
	}

	public boolean loadModel(InputStream is) {
		super.loadModel(is);
		mInitTextureCoords = new float[mpGroups.length][];

		for (int i = 0; i < mpGroups.length; i++) {
			mInitTextureCoords[i] = new float[mpGroups[i].getTriangleCount() * 3 * 2];

			FloatBuffer textureBuffer = mpBufTextureCoords[i];
			for (int j = 0; j < textureBuffer.capacity(); j++) {
				mInitTextureCoords[i][j] = textureBuffer.get(j);
			}

		}
		return true;
	}

	public void changeUV(float u, float v) {
		synchronized (this) {
			for (int i = 0; i < mpBufTextureCoords.length; i++) {
				FloatBuffer textureBuffer = mpBufTextureCoords[i];
				textureBuffer.position(0);
				for (int j = 0; j < textureBuffer.capacity(); j++) {
					if (j % 2 == 1) {
						textureBuffer.put(mInitTextureCoords[i][j] + v);
					} else {
						textureBuffer.put(mInitTextureCoords[i][j] + u);
					}
				}
			}
		}
	}

	protected void beforeDrawConfig() {
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, Texture.WRAP_REPEAT);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, Texture.WRAP_REPEAT);

		GLES20.glBlendFunc(mSrcBlendMode, mDstBlendMode);
	}

	protected void afterDrawConfig() {
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
	}
}
