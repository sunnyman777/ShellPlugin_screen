package com.jiubang.shell.effect;

import com.go.gl.graphics.BitmapGLDrawable;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.Texture;
import com.go.gl.graphics.geometry.GLGrid;
import com.go.gl.graphics.geometry.TextureGLObjectRender;
import com.go.gl.view.GLView;
import com.go.util.graphics.DrawUtils;

/**
 * 水波纹特效
 * @author zouguiquan
 *
 */
public class WaveEffect extends AbstractEffect {

	private BitmapGLDrawable mBitmapGLDrawable;
	private WaveMesh mWaveMesh;
	//private WaveTextureRender mRender;
	private TextureGLObjectRender mRender;

	private int mFrameWidth, mFrameHeight;
	//水波开始震动的范围,数值越大,水波效果越明显
	private int mWaveSize;
	//衰减系数,数值越大,衰减幅度越少
	private int mDamping;
	//水波的震幅,数值越大,水波效果越明显
	private int mWaveDepth;
	//水波震动的时间
	private long mDuration;
	private int mDiv;

	private int mRadiusSize;
	private int mCenterX;
	private int mCenterY;
	
	public static final long DURATION = 450;

	public WaveEffect(GLView waveFrameLayout, int centerX, int centerY, long duration,
			int waveSize, int waveDepth, int damping) {
		super(duration);

		mWaveSize = waveSize;
		mDamping = damping;
		mDuration = duration;
		mWaveDepth = waveDepth;

		if (DrawUtils.sHeightPixels < 800) {
			mDiv = DrawUtils.dip2px(9);
		} else {
			mDiv = DrawUtils.dip2px(10);
		}

		initWave(waveFrameLayout, centerX, centerY);
	}
	
	public void setRadiusSize(int radiusSzie) {
		mRadiusSize = radiusSzie;
	}

	private void initWave(GLView waveFrameLayout, int centerX, int centerY) {

		mCenterX = centerX;
		mCenterY = centerY;
		
		mFrameWidth = waveFrameLayout.getWidth();
		mFrameHeight = waveFrameLayout.getHeight();
		int divX = Math.max(1, waveFrameLayout.getWidth() / mDiv);
		int divY = Math.max(1, waveFrameLayout.getHeight() / mDiv);

		mWaveMesh = new WaveMesh(divX, divY, true);
		mWaveMesh.setBounds(0, 0, mFrameWidth, mFrameHeight);
		mWaveMesh.setTexcoords(0, 1, 1, 0);
		mWaveMesh.setWavePoint(centerX, centerY, mWaveSize, mWaveDepth);
		//mRender = new WaveTextureRender();
		mRender = new TextureGLObjectRender();
	}

	@Override
	public void updateEffect(Object[] drawInfo) {
		if (drawInfo[0] != null && drawInfo[0] instanceof BitmapGLDrawable) {
			mBitmapGLDrawable = (BitmapGLDrawable) drawInfo[0];
		}
	}

	/**
	 * 
	 * @author zouguiquan
	 *
	 */
	private class WaveMesh extends GLGrid {
		private float mHBuffer1[];
		private float mHBuffer2[];
		private float[] mTexcoordArray2;
		private int mHStride;

		public WaveMesh(int xDiv, int yDiv, boolean fill) {
			super(xDiv, yDiv, fill);
			mHBuffer1 = new float[getVertexCount()];
			mHBuffer2 = new float[getVertexCount()];
			mHStride = xDiv + 1;
		}

		@Override
		public void setTexcoords(float u1, float v1, float u2, float v2) {
			super.setTexcoords(u1, v1, u2, v2);
			mTexcoordArray2 = new float[mTexcoordArray.length];
			System.arraycopy(mTexcoordArray, 0, mTexcoordArray2, 0, mTexcoordArray.length);
		}

		/**
		 * 激发水波纹
		 * @param centerX
		 * @param centerY
		 * @param stonesize
		 * @param stoneWeight
		 */
		public void setWavePoint(int centerX, int centerY, int waveSize, float stoneWeight) {

			int leftCol = (centerX - waveSize) / mDiv;
			if (leftCol < 0) {
				leftCol = 0;
			}

			int rightCol = (centerX + waveSize) / mDiv;
			if (rightCol > getDivX()) {
				rightCol = getDivX();
			}

			int topRow = (centerY - waveSize) / mDiv;
			if (topRow < 0) {
				topRow = 0;
			}

			int bottomRow = (centerY + waveSize) / mDiv;
			if (bottomRow > getDivY()) {
				bottomRow = getDivY();
			}

			for (int row = topRow; row <= bottomRow; row++) {
				for (int column = leftCol; column <= rightCol; column++) {
					int dx = column * mDiv - centerX;
					int dy = row * mDiv - centerY;
					if (dx * dx + dy * dy < waveSize * waveSize) {
//						int distance = (int) Math.sqrt(dx * dx + dy * dy);
//						mHBuffer1[row * mHStride + column] = -stoneWeight * (1 - (distance / waveSize));
						mHBuffer1[row * mHStride + column] = -stoneWeight;
					}
				}
			}
		}

		/**
		 * 通过求四个方向的平均振幅形成水波纹
		 */
		public void updatePosition() {
			int index, left, right, top, bottom;
			int divX = getDivX();
			int divY = getDivY();

			for (int row = 1; row < divY; ++row) {
				for (int column = 1; column < divX; ++column) {
					index = row * mHStride + column;
					left = index - 1;
					right = index + 1;
					top = (row - 1) * mHStride + column;
					bottom = (row + 1) * mHStride + column;

					float value;
					value = mHBuffer1[left];
					value += mHBuffer1[right];
					value += mHBuffer1[top];
					value += mHBuffer1[bottom];
					value = value / 2f - mHBuffer2[index];
					value -= value / mDamping;
					mHBuffer2[index] = value;
					
					//清除回弹效果
					if (column == 1) {
						mHBuffer1[left] = value - value / mDamping;
					}
					if (column == divX - 1) {
						mHBuffer1[right] = value - value / mDamping;
					}
					if (row == 1) {
						mHBuffer1[top] = value - value / mDamping;
					}
					if (row == divY - 1) {
						mHBuffer1[bottom] = value - value / mDamping;
					}
				}
			}

			float temp[] = mHBuffer1;
			mHBuffer1 = mHBuffer2;
			mHBuffer2 = temp;

			updateTexCoor(mHBuffer1);
		}

		/**
		 * 通过两点之间的高度差计算折射后的纹理偏移量,线性近似,不准确
		 * 
		 * @param buffer
		 */
		private void updateTexCoor(float buffer[]) {

			final int divY = getDivY();
			final int divX = getDivX();
			final float[] texCoor = mTexcoordArray;
			float heightOffH;
			float xAxisOff;
			float hightOffV;
			float yAxisOff;
			int index;

			for (int i = 1; i < divY - 1; ++i) {
				for (int j = 1; j < divX; ++j) {
					
					index = i * mHStride + j;

					if (mRadiusSize > 0) {
						int dx = j * mDiv - mCenterX;
						int dy = i * mDiv - mCenterY;
						if (dx * dx + dy * dy <= mRadiusSize * mRadiusSize) {
							continue;
						}
					}

					heightOffH = buffer[index - 1] - buffer[index + 1];
					if (heightOffH != 0) {
						xAxisOff = heightOffH / (float) mFrameWidth;
						texCoor[index * 2] = mTexcoordArray2[index * 2] - xAxisOff;
					}

					hightOffV = buffer[index - mHStride] - buffer[index + mHStride];
					if (hightOffV != 0) {
						yAxisOff = hightOffV / (float) mFrameHeight;
						texCoor[index * 2 + 1] = mTexcoordArray2[index * 2 + 1] + yAxisOff;
					}
				}
			}
		}

		/**
		 * 通过两点之间的高度差计算折射后的纹理偏移量
		 * 
		 * @param h
		 * @param w
		 * @return
		 */
		public float computeBeamOffset(float h, float w) {
			if (h == 0) {
				return 0;
			}
			//水的折射率
			float refraction = 1.333f;
			float angle = (float) Math.atan(h / w);
			float beamAngle = (float) Math.asin(Math.sin(angle) / refraction);
			float offset = (float) (Math.tan(beamAngle) * h);
			return offset;
		}
	}

	@Override
	public void endEffect() {
		super.endEffect();
		mWaveMesh.clear();
		if (mBitmapGLDrawable != null) {
			mBitmapGLDrawable.clear();
			mBitmapGLDrawable = null;
		}
		mRender.clear();
		mRender.onClear();
	}

	@Override
	protected void effecting(GLCanvas canvas, float interpolatorTime,
			Object[] params) {
		if (params[0] instanceof BitmapGLDrawable) {
			mBitmapGLDrawable = (BitmapGLDrawable) params[0];
		}
		if (mBitmapGLDrawable != null) {
			Texture oldTexture = mRender.mTexture;
			Texture texture = mBitmapGLDrawable.getTexture();
			if (oldTexture != texture) {
				if (oldTexture != null) {
					oldTexture.duplicate();
				}
				mRender.setTexture(texture);
			}

			float t = interpolatorTime;

			t = Math.max(0, Math.min(t, 1));
			if (t >= 1) {
				
			} else {
				mWaveMesh.updatePosition();
				mRender.draw(canvas, mWaveMesh);
			}
		}
		
	}
}
