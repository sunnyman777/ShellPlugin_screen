/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jiubang.shell.analysis3dmode;

import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.opengl.GLES20;

import com.go.gl.animation.Transformation3D;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLShaderProgram;
import com.go.gl.graphics.GLVBO;
import com.go.gl.graphics.RenderContext;
import com.go.gl.graphics.Renderable;
import com.go.gl.graphics.Texture;
import com.go.gl.graphics.TextureShader;
//CHECKSTYLE IGNORE 1000 LINE
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  guoweijie
 * @date  [2013-3-13]
 */
public class Ms3DModel extends AbsMS3DModel {

	private GLShaderProgram mShader;
	boolean mCullFace = false;	//是否主动背面剔除
	
	private final float[] mSrcColor = new float[4]; // CHECKSTYLE IGNORE
	private int mPorterDuffMode = TextureShader.MODE_NONE;
	
	public Ms3DModel(boolean recyleImgAfterLoaded) {
		super(recyleImgAfterLoaded);
		mShader = TextureShader.getShader(TextureShader.MODE_ALPHA);
	}

	public Ms3DModel(Bitmap textureImg, boolean recyleImgAfterLoaded) {
		super(textureImg, recyleImgAfterLoaded);
		mShader = TextureShader.getShader(TextureShader.MODE_ALPHA);
	}

	public Ms3DModel(Bitmap[] textureImg, boolean recyleImgAfterLoaded) {
		super(textureImg, recyleImgAfterLoaded);
		mShader = TextureShader.getShader(TextureShader.MODE_ALPHA);
	}

	protected void beforeDrawConfig() {
		//GLES20.glBlendFunc(GLES20.GL_ONE_MINUS_DST_ALPHA, GLES20.GL_ONE_MINUS_DST_COLOR);
		GLES20.glBlendFunc(mSrcBlendMode, mDstBlendMode);
	}

	protected void afterDrawConfig() {
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		//GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
	}

	private Transformation3D mAnimationTransformation3d = new Transformation3D();
	private Renderable mDefaultRenderable = new Renderable() {
		@Override
		public void run(long timeStamp, RenderContext context) {
			synchronized (Ms3DModel.this) {
				TextureShader shader = (TextureShader) context.shader;
				if (shader == null || !shader.bind()) {
					return;
				}

				//beforeDrawConfig();

				shader.setMatrix(context.matrix, 0);
				shader.setAlpha(context.alpha);

				// 遍历所有的MS3D Group，渲染每一个Group
				for (int i = 0; i < mpGroups.length; i++) {
					if (mpGroups[i].getTriangleCount() == 0) {
						continue;
					}

					Texture texture = mTextures[i % mTextures.length];
					if (null == texture || !texture.bind()) {
						continue;
					}
					
					if (mGroupDrawEnables != null && !mGroupDrawEnables[i]) {
						continue;
					}
					
					boolean enableFilter = mFilterEnables[i];
					if (enableFilter) {
						context.color[0] = mSrcColor[0];
						context.color[1] = mSrcColor[1];
						context.color[2] = mSrcColor[2];
						context.color[3] = mSrcColor[3];
						shader.setMaskColor(context.color);
					} else {
						context.color[0] = 0;
						context.color[1] = 0;
						context.color[2] = 0;
						context.color[3] = 0;
						shader.setMaskColor(context.color);
					}
					
					final float oldAlpha = context.alpha;
					boolean more = false;
					if (mAnimations != null && mAnimations[i] != null && mGroupAnimationListener != null) {
						more = mAnimations[i].getTransformation(
								mGroupAnimationListener.getDrawingTime(), mAnimationTransformation3d);
						final float alpha = mAnimationTransformation3d.getAlpha();
						if (alpha != 1) {
							shader.setAlpha(alpha);
						}
					}

					mpBufVertices[i].position(0);
					mpBufTextureCoords[i].position(0);
					
					if (USE_VBO && mVerticesVBO != null) {
						mTexcoordsVBO[i].bindOnGLThread(mpBufTextureCoords[i], mpBufTextureCoords[i].capacity());
						shader.setTexCoord(0, 2);
						GLVBO.unbindOnGLThread();
						
						mVerticesVBO[i].bindOnGLThread(mpBufVertices[i], mpBufVertices[i].capacity());
						shader.setPosition(0, 3);
						GLVBO.unbindOnGLThread();
						
					} else {
						//绑定顶点数据
						shader.setPosition(mpBufVertices[i], 3);
						shader.setTexCoord(mpBufTextureCoords[i], 2);
					}
					
					beforeDrawConfig();
					GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mpGroups[i].getTriangleCount() * 3);
					
					shader.setAlpha(oldAlpha);
					
					if (more) {
						mGroupAnimationListener.invalidate();
					} else if (mAnimations != null && mAnimations[i] != null) {
						mAnimations[i] = null;
						if (mGroupAnimationListener != null) {
							mGroupAnimationListener.onGroupAnimationEnd(i);
						}
					}

				}
				afterDrawConfig();
			}
		}
	};

	public void render(GLCanvas canvas) {
		if (!mValid) {
			if (mRecyleImgAfterLoaded) {
				if (mOnTextureMissedListener != null) {
					mOnTextureMissedListener.onTextureMissed();
				}
			}
			mValid = true;
		}


		final int fadeAlpha = canvas.getAlpha();
		float alpha = 1;
		if (fadeAlpha < 255) {	//CHECKSTYLE IGNORE
			alpha = fadeAlpha * ONE_OVER_255;
		}
		
		if (mPorterDuffMode == TextureShader.MODE_NONE) {
			mShader = TextureShader.getShader(alpha >= 1
					? TextureShader.MODE_NONE
					: TextureShader.MODE_ALPHA);
		} else {
			mShader = TextureShader.getShader(mPorterDuffMode);
		}

		if (mShader == null) {
			return;
		}
		
		boolean cullFace = true;
		if (mCullFace) {
			cullFace = canvas.isCullFaceEnabled();
			canvas.setCullFaceEnabled(true);
		}
		
		RenderContext context = RenderContext.acquire();
		context.shader = mShader;
		context.alpha = alpha;
		canvas.getFinalMatrix(context);

		canvas.addRenderable(mDefaultRenderable, context);
		
		if (mCullFace) {
			canvas.setCullFaceEnabled(cullFace);
		}
		
	}

	@Override
	public void setColorFilter(int srcColor, Mode mode) {
		if (mode == null) {
			mPorterDuffMode = TextureShader.MODE_NONE;
			return;
		}
		//从ARGB转成(r, g, b, a)的alpha-premultiplied格式
		//CHECKSTYLE IGNORE 5 LINES
		final float a = (srcColor >>> 24) * ONE_OVER_255;
		mSrcColor[0] = (srcColor >>> 16 & 0xFF) * a * ONE_OVER_255;
		mSrcColor[1] = (srcColor >>> 8 & 0xFF) * a * ONE_OVER_255;
		mSrcColor[2] = (srcColor & 0xFF) * a * ONE_OVER_255;
		mSrcColor[3] = a;
		mPorterDuffMode = mode.ordinal();
	}
}
