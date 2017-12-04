package com.jiubang.shell.analysis3dmode;

import java.io.InputStream;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.opengl.GLES20;

import com.go.gl.animation.Animation;
import com.go.gl.graphics.BitmapRecycler;
import com.go.gl.graphics.BitmapTexture;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLVBO;
import com.go.gl.graphics.Texture;
import com.go.gl.graphics.TextureListener;
import com.go.gl.graphics.TextureLoadedListener;
import com.go.gl.graphics.TextureManager;
import com.go.gl.util.IBufferFactory;
import com.go.gl.util.Ray;
import com.go.gl.util.Vector4f;

/**
 * 模型基类
 * @author chendongcheng
 *
 */
public abstract class AbsMS3DModel implements TextureListener, TextureLoadedListener {

	public String mName = "";
	public boolean mIsLoadSuccess = false;
	public MS3DHeader mHeader;
	public MS3DVertex[] mpVertices;
	public MS3DTriangle[] mpTriangles;
	public MS3DGroup[] mpGroups;
	public MS3DMaterial[] mpMaterials;
	public Joint[] mpJoints;
	public float mTotalTime;
	public float mCurrentTime;
	public float mFps;
	public int mNumFrames;
	public int mNumPrimitives;
	private String mStrComment;

	protected FloatBuffer[] mpBufVertices;
	protected FloatBuffer[] mpBufTextureCoords;
	
	protected final static boolean USE_VBO = true;
	protected GLVBO[] mVerticesVBO;
	protected GLVBO[] mTexcoordsVBO;

	private Vector3f mvMax = new Vector3f(), mvMin = new Vector3f(), mvCenter = new Vector3f();
	private float mfRadius;
	private float mXLen;
	private float mYLen;
	private float mZLen;

	protected boolean mbInitBoundingBox;
	protected boolean mbDirtFlag = false;

	private FloatBuffer mBufJointLinePosition;
	private FloatBuffer mBufJointPointPosition;
	private int mJointPointCount, mJointLineCount;

	private Bitmap[] mTextureImg;
	protected BitmapTexture[] mTextures;
	protected boolean mValid;

	protected boolean mRecyleImgAfterLoaded;

	protected OnTextureMissedListener mOnTextureMissedListener;
	
	protected Matrix4f mLocalMatrix4f = new Matrix4f();
	
	protected boolean mDBG = false;
	
	public void setOnTextureMissedListener(OnTextureMissedListener listener) {
		mOnTextureMissedListener = listener;
	}

	public AbsMS3DModel(boolean recyleImgAfterLoaded) {
		mRecyleImgAfterLoaded = recyleImgAfterLoaded;
		if (mRecyleImgAfterLoaded) {
			TextureManager.getInstance().registerTextureListener(this);
		}
	}

	public AbsMS3DModel(Bitmap textureImg, boolean recyleImgAfterLoaded) {
		mRecyleImgAfterLoaded = recyleImgAfterLoaded;
		if (mRecyleImgAfterLoaded) {
			TextureManager.getInstance().registerTextureListener(this);
		}

		mTextureImg = new Bitmap[1];
		mTextureImg[0] = textureImg;
		bindTexture();
		mValid = true;
	}

	public AbsMS3DModel(Bitmap[] textureImg, boolean recyleImgAfterLoaded) {
		mRecyleImgAfterLoaded = recyleImgAfterLoaded;
		if (mRecyleImgAfterLoaded) {
			TextureManager.getInstance().registerTextureListener(this);
		}

		mTextureImg = textureImg;
		bindTexture();
		mValid = true;
	}

	public void setTexture(Bitmap[] imgs) {
		if (mTextureImg != null) {
			for (Bitmap bitmap : mTextureImg) {
				if (bitmap != null && !bitmap.isRecycled()) {
					bitmap.recycle();
				}
			}
		}
		mTextureImg = imgs;
		bindTexture();
	}

	public void setGroupTexture(int groupIndex, Bitmap img, boolean recycle) {
		if (null == mTextureImg) {
			mTextureImg = new Bitmap[mpGroups.length];
		}
		
		if (groupIndex >= 0 && groupIndex < mTextureImg.length) {
			Bitmap bitmap = mTextureImg[groupIndex];
			if (recycle && bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
			}

			mTextureImg[groupIndex] = img;
			bindTexture(groupIndex);
			
			if (mAnimations != null) {
				mAnimations[groupIndex] = null;
			}
		}
	}

	private void bindTexture(int groupIndex) {
		if (mTextures == null) {
			mTextures = new BitmapTexture[mTextureImg.length];
		}
		if (groupIndex >= 0 && groupIndex < mTextureImg.length) {
			recycleTexture(mTextures[groupIndex]);
			Bitmap bitmap = mTextureImg[groupIndex];
			mTextures[groupIndex] = BitmapTexture.createSharedTexture(bitmap);
			if (mTextures[groupIndex] != null) {
				mTextures[groupIndex].register();
				if (mRecyleImgAfterLoaded) {
					mTextures[groupIndex].setLoadedListener(this);
				}
			}
		}

	}

	private void bindTexture() {
		if (mTextures == null) {
			mTextures = new BitmapTexture[mTextureImg.length];
		}
		for (int i = 0; i < mTextureImg.length; i++) {
			recycleTexture(mTextures[i]);
			Bitmap bitmap = mTextureImg[i];
			mTextures[i] = BitmapTexture.createSharedTexture(bitmap);
			if (mTextures[i] != null) {
				mTextures[i].register();
				if (mRecyleImgAfterLoaded) {
					mTextures[i].setLoadedListener(this);
				}
			}
		}

	}

	private void recycleTexture(BitmapTexture texture) {
		if (texture != null) {
			texture.clear();
		}
	}

	@Override
	public void onTextureInvalidate() {
		mValid = false;
	}

	public String getComment() {
		return mStrComment;
	}

	public void setComment(String comment) {
		this.mStrComment = comment;
	}

	public boolean loadModel(InputStream is) {

		MS3DLoader loader = new MS3DLoader();
		boolean resultOK = loader.load(is, this);

		if (!resultOK) {
			return false;
		}

		mCurrentTime = 0.0f;
		mTotalTime = mNumFrames / mFps;

		mpBufVertices = new FloatBuffer[mpGroups.length];
		mpBufTextureCoords = new FloatBuffer[mpGroups.length];
		mFilterEnables = new boolean[mpGroups.length];


		final int groups = mpGroups.length;
		if (USE_VBO && mTexcoordsVBO == null) {
			mTexcoordsVBO = new GLVBO[groups];
			for (int i = 0; i < groups; ++i) {
				mTexcoordsVBO[i] = new GLVBO(false);
			}
		}
		for (int i = 0; i < groups; i++) {
			mpBufVertices[i] = IBufferFactory.newFloatBuffer(mpGroups[i].getTriangleCount() * 3 * 3);
			mpBufTextureCoords[i] = IBufferFactory.newFloatBuffer(mpGroups[i].getTriangleCount() * 3 * 2);

			for (int j = 0; j < mpGroups[i].getTriangleCount(); j++) {
				// fill
				MS3DTriangle triangle = mpTriangles[mpGroups[i].getTriangleIndicies()[j]];

				for (int k = 0; k < 3; k++) {
					float s = triangle.getS()[k];
					float t = triangle.getT()[k];
					mpBufTextureCoords[i].put(s);
					mpBufTextureCoords[i].put(t);
				}
			}
			mpBufTextureCoords[i].rewind();
			if (USE_VBO) {
				mTexcoordsVBO[i].invalidateData();
			}
		}

		mbDirtFlag = true;
		animate(0.0f);
		mbInitBoundingBox = true;
		fillRenderBuffer();
		mbInitBoundingBox = false;
		return true;
	}

	//	public void changeUV(float v) {
	//		synchronized (this) {
	//			for (int i = 0; i < mpBufTextureCoords.length; i++) {
	//				FloatBuffer textureBuffer = mpBufTextureCoords[i];
	//				textureBuffer.position(0);
	//				for (int j = 0; j < textureBuffer.capacity(); j++) {
	//					if (j % 2 == 1) {
	//						textureBuffer.put(mInitTextureCoords[i][j] + v);
	//					} else {
	//						textureBuffer.put(mInitTextureCoords[i][j]);
	//					}
	//				}
	//			}
	//		}
	//	}

	private void updateJointsHelper() {
		if (!mDBG) {
			return;
		}
		if (!containsJoint()) {
			return;
		}
		if (mBufJointPointPosition == null) {
			mJointPointCount = mpJoints.length;
			mBufJointPointPosition = IBufferFactory.newFloatBuffer(mpJoints.length * 3); // CHECKSTYLE IGNORE
		}
		mBufJointPointPosition.position(0);

		for (int i = 0, n = mpJoints.length; i < n; i++) {
			Joint joint = mpJoints[i];

			float x = joint.mMatGlobal.m03;
			float y = joint.mMatGlobal.m13;
			float z = joint.mMatGlobal.m23;

			mBufJointPointPosition.put(x);
			mBufJointPointPosition.put(y);
			mBufJointPointPosition.put(z);
		}

		mBufJointPointPosition.position(0);

		// fill joint line buffer
		if (mBufJointLinePosition == null) {
			mJointLineCount = mpJoints.length * 2;
			mBufJointLinePosition = IBufferFactory.newFloatBuffer(mpJoints.length * 2 * 3); // CHECKSTYLE IGNORE
		}
		mBufJointLinePosition.position(0);

		for (int i = 0, n = mpJoints.length; i < n; i++) {
			Joint joint = mpJoints[i];

			float x0, y0, z0;
			float x1, y1, z1;
			x0 = joint.mMatGlobal.m03;
			y0 = joint.mMatGlobal.m13;
			z0 = joint.mMatGlobal.m23;
			if (joint.mParentId == -1) {
				// no parent
				x1 = x0;
				y1 = y0;
				z1 = z0;
			} else {
				joint = mpJoints[joint.mParentId];
				x1 = joint.mMatGlobal.m03;
				y1 = joint.mMatGlobal.m13;
				z1 = joint.mMatGlobal.m23;
			}

			mBufJointLinePosition.put(x0);
			mBufJointLinePosition.put(y0);
			mBufJointLinePosition.put(z0);

			mBufJointLinePosition.put(x1);
			mBufJointLinePosition.put(y1);
			mBufJointLinePosition.put(z1);
		}

		mBufJointLinePosition.position(0);
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  guoweijie
	 * @date  [2013-3-21]
	 */
	public interface AnimationListener {
		public void onAnimationEnd();
	}

	private AnimationListener mAnimationListener;

	public void setAnimationListener(AnimationListener listener) {
		mAnimationListener = listener;
	}

	public void stopAnimate() {
		mCurrentTime = 0.0f;
	}
	
	public void setJointFrame(int jointIndex, Matrix4f matKeyframe) {
		if (jointIndex >= mpJoints.length || jointIndex < 0) {
			return;
		}
		Joint joint = mpJoints[jointIndex];
		matKeyframe.mul(joint.mMatJointRelative, matKeyframe);
		// 乘以父矩阵，得到最终矩阵
		if (joint.mParentId == -1) {
			joint.mMatGlobal.set(matKeyframe);
		} else {
			matKeyframe.mul(mpJoints[joint.mParentId].mMatGlobal, matKeyframe);
			joint.mMatGlobal.set(matKeyframe);
		}
	}
	
	public void updateVertex() {
		// 更新点线渲染的骨骼帮助信息
		updateJointsHelper();

		// 开始更新每个顶点
		for (int i = 0, n = mpVertices.length; i < n; i++) {
			MS3DVertex vertex = mpVertices[i];

			if (vertex.getBoneID() == -1) {
				// 如果该顶点不受骨骼影响，那么就无需计算
				vertex.mvTransformedLocation.set(vertex.getLocation());
			} else {
				// 通过骨骼运算，得到顶点的当前位置
				transformVertex(vertex);
			}
		}

		mbDirtFlag = true;
	}

	/**
	 * 根据时间来更新模型动画
	 * 
	 * @param timedelta
	 *            - 本次tick时间
	 */
	public void showFrame(int curFrame) {

		// 首先要更新每个骨骼节点的当前位置信息
		float time = mTotalTime / mNumFrames * curFrame;
		for (int i = 0; i < mpJoints.length; i++) {
			Joint joint = mpJoints[i];
			// 如果不包含动画信息那就无需更新
			if (joint.mNumTranslationKeyframes == 0 && joint.mNumRotationKeyframes == 0) {
				joint.mMatGlobal.set(joint.mMatJointAbsolute);
				continue;
			}

			// 开始进行插值计算
			// 首先进行旋转插值
			Matrix4f matKeyframe = getJointRotation(i, time);
			// 进行偏移的线性插值
			matKeyframe.setTranslation(getJointTranslation(i, time));
			// 乘以节点本身的相对矩阵
			matKeyframe.mul(joint.mMatJointRelative, matKeyframe);

			// 乘以父矩阵，得到最终矩阵
			if (joint.mParentId == -1) {
				joint.mMatGlobal.set(matKeyframe);
			} else {
				matKeyframe.mul(mpJoints[joint.mParentId].mMatGlobal, matKeyframe);
				joint.mMatGlobal.set(matKeyframe);
			}
		}
		// 更新点线渲染的骨骼帮助信息
		updateJointsHelper();

		// 开始更新每个顶点
		for (int i = 0, n = mpVertices.length; i < n; i++) {
			MS3DVertex vertex = mpVertices[i];

			if (vertex.getBoneID() == -1) {
				// 如果该顶点不受骨骼影响，那么就无需计算
				vertex.mvTransformedLocation.set(vertex.getLocation());
			} else {
				// 通过骨骼运算，得到顶点的当前位置
				transformVertex(vertex);
			}
		}

		mbDirtFlag = true;
	}

	/**
	 * 根据时间来更新模型动画
	 * 
	 * @param timedelta
	 *            - 本次tick时间
	 */
	public void animate(float timedelta) {
		// 累加时间
		mCurrentTime += timedelta;

		if (mCurrentTime > mTotalTime) {
			if (mAnimationListener != null) {
				mAnimationListener.onAnimationEnd();
			}
		}
		mLocalMatrix4f.setIdentity();
		// 首先要更新每个骨骼节点的当前位置信息
		for (int i = 0; i < mpJoints.length; i++) {
			Joint joint = mpJoints[i];
			// 如果不包含动画信息那就无需更新
			if (joint.mNumTranslationKeyframes == 0 && joint.mNumRotationKeyframes == 0) {
				joint.mMatGlobal.set(joint.mMatJointAbsolute);
				continue;
			}

			// 开始进行插值计算
			// 首先进行旋转插值
			Matrix4f matKeyframe = getJointRotation(i, mCurrentTime);
			// 进行偏移的线性插值
			matKeyframe.setTranslation(getJointTranslation(i, mCurrentTime));
			// 乘以节点本身的相对矩阵
			matKeyframe.mul(joint.mMatJointRelative, matKeyframe);
			
			// 乘以父矩阵，得到最终矩阵
			if (joint.mParentId == -1) {
				joint.mMatGlobal.set(matKeyframe);
			} else {
				matKeyframe.mul(mpJoints[joint.mParentId].mMatGlobal, matKeyframe);
				joint.mMatGlobal.set(matKeyframe);
			}
			
		}
		// 更新点线渲染的骨骼帮助信息
		updateJointsHelper();

		// 开始更新每个顶点
		for (int i = 0, n = mpVertices.length; i < n; i++) {
			MS3DVertex vertex = mpVertices[i];

			if (vertex.getBoneID() == -1) {
				// 如果该顶点不受骨骼影响，那么就无需计算
				vertex.mvTransformedLocation.set(vertex.getLocation());
			} else {
				// 通过骨骼运算，得到顶点的当前位置
				transformVertex(vertex);
			}
		}

		mbDirtFlag = true;
	}
	//CHECKSTYLE IGNORE 1000 LINE
	static Matrix4f sTmpMatrixJointRotation = new Matrix4f();

	private Matrix4f getJointRotation(int jointIndex, float time) {
		Quat4f quat = lerpKeyframeRotate(mpJoints[jointIndex].mpRotationKeyframes, time);

		Matrix4f matRot = sTmpMatrixJointRotation;
		matRot.set(quat);
		return matRot;
	}

	static Quat4f sTmpQuatLerp = new Quat4f();
	static Quat4f sTmpQuatLerpLeft = new Quat4f(), sTmpQuatLerpRight = new Quat4f();

	/**
	 * 根据传入的时间，计算插值后的旋转量
	 * 
	 * @param frames
	 *            旋转量关键帧数组
	 * @param time
	 *            目标时间
	 * @return 插值后的旋转量数据
	 */
	private Quat4f lerpKeyframeRotate(Keyframe[] frames, float time) {
		Quat4f quat = sTmpQuatLerp;
		//int frameIndex = 0;
		int numFrames = frames.length;

		// 这里可以使用二分查找进行优化
		//		while (frameIndex < numFrames && frames[frameIndex].mfTime < time) {
		//			++frameIndex;
		//		}
		int startIndex = 0;
		int stopIndex = numFrames - 1;
		int frameIndex = (startIndex + stopIndex) / 2;
		while (startIndex < stopIndex) {
			float fTime = frames[frameIndex].mfTime;
			if (fTime < time) {
				int tempIndex = frameIndex + 1;
				if (tempIndex >= numFrames) {
					frameIndex = numFrames - 1;
					break;
				} else if (frames[tempIndex].mfTime >= time) {
					frameIndex = tempIndex;
					break;
				}

				startIndex = frameIndex + 1;
				frameIndex = (startIndex + stopIndex) / 2;
			} else if (fTime > time) {
				int tempIndex = frameIndex - 1;
				if (tempIndex < 0) {
					frameIndex = 0;
					break;
				} else if (frames[tempIndex].mfTime < time) {
					break;
				}
				stopIndex = frameIndex;
				frameIndex = (startIndex + stopIndex) / 2;
			} else {
				break;
			}
		}

		// 首先处理边界情况
		if (frameIndex == 0) {
			quat.set(frames[0].mvParam);
		} else if (frameIndex == numFrames) {
			quat.set(frames[numFrames - 1].mvParam);
		} else {
			int prevFrameIndex = frameIndex - 1;
			// 找到最邻近的两帧
			Keyframe right = frames[frameIndex];
			Keyframe left = frames[prevFrameIndex];
			// 计算好插值因子
			float timeDelta = right.mfTime - left.mfTime;
			float interpolator = (time - left.mfTime) / timeDelta;
			// 进行四元数插值
			Quat4f quatRight = sTmpQuatLerpRight;
			Quat4f quatLeft = sTmpQuatLerpLeft;

			quatRight.set(right.mvParam);
			quatLeft.set(left.mvParam);
			quat.interpolate(quatLeft, quatRight, interpolator);
		}

		return quat;
	}
	private Vector3f getJointTranslation(int jointIndex, float time) {
		Vector3f translation = lerpKeyframeLinear(mpJoints[jointIndex].mpTranslationKeyframes, time);

		return translation;
	}

	static Vector3f sTmpVectorLerp = new Vector3f();

	/**
	 * 根据传入的时间，返回插值后的位置信息
	 * 
	 * @param frames
	 *            偏移量关键帧数组
	 * @param time
	 *            目标时间
	 * @return 插值后的位置信息
	 */
	private Vector3f lerpKeyframeLinear(Keyframe[] frames, float time) {
		int frameIndex = 0;
		int numFrames = frames.length;

		// 这里可以使用二分查找进行优化
		while (frameIndex < numFrames && frames[frameIndex].mfTime < time) {
			++frameIndex;
		}

		// 首先处理边界情况
		Vector3f parameter = sTmpVectorLerp;
		if (frameIndex == 0) {
			parameter.set(frames[0].mvParam.x, frames[0].mvParam.y, frames[0].mvParam.z);
		} else if (frameIndex == numFrames) {
			parameter.set(frames[numFrames - 1].mvParam.x, frames[numFrames - 1].mvParam.y,
					frames[numFrames - 1].mvParam.z);
		} else {
			int prevFrameIndex = frameIndex - 1;
			// 得到临近两帧
			Keyframe right = frames[frameIndex];
			Keyframe left = frames[prevFrameIndex];
			// 计算插值因子
			float timeDelta = right.mfTime - left.mfTime;
			float interpolator = (time - left.mfTime) / timeDelta;
			// 进行简单的线性插值
			parameter.interpolate(left.mvParam, right.mvParam, interpolator);
		}

		return parameter;
	}

	/**
	 * 填充渲染缓存数据
	 */
	public void fillRenderBuffer() {
		if (!mbDirtFlag) {
			//如果模型数据没有更新，那么就无需重新填充
			return;
		}
		synchronized (AbsMS3DModel.this) {
			
			final int groups = mpGroups.length;
			if (USE_VBO && mVerticesVBO == null) {
				mVerticesVBO = new GLVBO[groups];
				for (int i = 0; i < groups; ++i) {
					mVerticesVBO[i] = new GLVBO(false);
				}
			}
			
			Vector3f position = null;
			//遍历所有Group
			for (int i = 0; i < groups; i++) {
				//获得该Group内所有的三角形索引
				int[] indexes = mpGroups[i].getTriangleIndicies();
				mpBufVertices[i].position(0);
				int vertexIndex = 0;
				//遍历每一个三角形
				for (int j = 0; j < indexes.length; j++) {
					//从三角形池内找到对应三角形
					MS3DTriangle triangle = mpTriangles[indexes[j]];
					//遍历三角形的每个顶点
					for (int k = 0; k < 3; k++) {
						//从顶点池中找到相应顶点
						MS3DVertex vertex = mpVertices[triangle.getVertexIndicies()[k]];
						//获得最新的位置
						//如果模型带骨骼，那么就是当前的变换后的位置
						//否则就是初始位置
						//具体的变换过程请参考animate(float timedelta)函数
						position = vertex.mvTransformedLocation;
						//填充顶点位置信息到缓存中
						mpBufVertices[i].put(position.x);
						mpBufVertices[i].put(position.y);
						mpBufVertices[i].put(position.z);

						if (mbInitBoundingBox) {
							//计算模型绑定框，仅在模型载入时启用
							mvMin.x = Math.min(mvMin.x, position.x);
							mvMin.y = Math.min(mvMin.y, position.y);
							mvMin.z = Math.min(mvMin.z, position.z);

							mvMax.x = Math.max(mvMax.x, position.x);
							mvMax.y = Math.max(mvMax.y, position.y);
							mvMax.z = Math.max(mvMax.z, position.z);
							
						}
					}
				}

				mpBufVertices[i].position(0);
				if (USE_VBO) {
					mVerticesVBO[i].invalidateData();
				}
			}

			if (mbInitBoundingBox) {
				//计算动态绑定球
				float distance = Vector3f.distance(mvMin, mvMax);
				mfRadius = distance * 0.5f;
				mXLen = mvMax.x - mvMin.x;
				mYLen = mvMax.y - mvMin.y;
				mZLen = mvMax.z - mvMin.z;
				
				mvCenter.set(mvMin);
				mvCenter.add(mvMax);
				mvCenter.scale(0.5f);

				mbInitBoundingBox = false;
			}

			mbDirtFlag = false;
		}
	}

	private static final int[] JOINT_INDEXES = new int[4], // CHECKSTYLE IGNORE
			JOINT_WEIGHTS = new int[4]; // CHECKSTYLE IGNORE
	private static final float[] WEIGHTS = new float[4]; // CHECKSTYLE IGNORE

	static Vector3f stmp = new Vector3f(), stmpResult = new Vector3f(), stmpPos = new Vector3f();

	/**
	 * transfrom vertex by joint matrix
	 * 
	 * @param vertex
	 * @return
	 */
	private Vector3f transformVertex(MS3DVertex vertex) {
		Vector3f position = vertex.mvTransformedLocation;
		fillJointIndexesAndWeights(vertex, JOINT_INDEXES, JOINT_WEIGHTS);
		
		if (JOINT_INDEXES[0] < 0 || JOINT_INDEXES[0] >= mpJoints.length || mCurrentTime < 0.0f) {
			position.set(vertex.getLocation());
		} else {
			// count valid weights
			int numWeight = 0;
			for (int i = 0; i < 4; i++) { // CHECKSTYLE IGNORE
				if (JOINT_WEIGHTS[i] > 0 && JOINT_INDEXES[i] >= 0 && JOINT_INDEXES[i] < mpJoints.length) {
					++numWeight;
				} else {
					break;
				}
			}

			// init
			position.zero();
			// CHECKSTYLE IGNORE 2 LINES
			for (int i = 0; i < 4; i++) {
				WEIGHTS[i] = (float) JOINT_WEIGHTS[i] * 0.01f; // /100.0f
			}
			if (numWeight == 0) {
				numWeight = 1;
				WEIGHTS[0] = 1.0f;
			}

			for (int i = 0; i < numWeight; i++) {
				Joint joint = mpJoints[JOINT_INDEXES[i]];

				Matrix4f mat = joint.mMatJointAbsolute;
				// Vector3f tmp = tmp;
				Vector3f result = stmpResult;
				Vector3f pos = stmpPos;
				pos.set(vertex.getLocation());
				mat.invTransform(pos, stmp);
				joint.mMatGlobal.transform(stmp, result);

				position.x += result.x * WEIGHTS[i];
				position.y += result.y * WEIGHTS[i];
				position.z += result.z * WEIGHTS[i];
			}
		}
		
		return position;
	}

	/**
	 * 填充顶点的骨骼和权重信息，以便统一计算。
	 * 
	 * @param vertex
	 * @param jointIndexes
	 * @param jointWeights
	 */
	private void fillJointIndexesAndWeights(MS3DVertex vertex, int[] jointIndexes, int[] jointWeights) {
		jointIndexes[0] = vertex.getBoneID();
		if (vertex.mpBoneIndexes == null) {
			for (int i = 0; i < 3; i++) { // CHECKSTYLE IGNORE
				jointIndexes[i + 1] = 0;
			}
		} else {
			for (int i = 0; i < 3; i++) { // CHECKSTYLE IGNORE
				jointIndexes[i + 1] = vertex.mpBoneIndexes[i] & 0xff; // CHECKSTYLE IGNORE
			}
		}

		jointWeights[0] = 100; // CHECKSTYLE IGNORE
		for (int i = 0; i < 3; i++) { // CHECKSTYLE IGNORE
			jointWeights[i + 1] = 0;
		}

		if (vertex.mpWeights != null && vertex.mpWeights[0] != 0 && vertex.mpWeights[1] != 0
				&& vertex.mpWeights[2] != 0) {
			int sum = 0;
			for (int i = 0; i < 3; i++) { // CHECKSTYLE IGNORE
				jointWeights[i] = vertex.mpWeights[i] & 0xff; // CHECKSTYLE IGNORE
				sum += jointWeights[i];
			}

			jointWeights[3] = 100 - sum; // CHECKSTYLE IGNORE
		}
	}

	protected int mSrcBlendMode = GLES20.GL_ONE;
	protected int mDstBlendMode = GLES20.GL_ONE_MINUS_SRC_ALPHA;

	public void setBlendMode(int srcFactor, int dstFactor) {
		mSrcBlendMode = srcFactor;
		mDstBlendMode = dstFactor;
	}

	protected final static float ONE_OVER_255 = 1 / 255.0f; // CHECKSTYLE IGNORE

	public abstract void render(GLCanvas canvas);
	//	{
	//		if (!mValid) {
	//			if (mRecyleImgAfterLoaded) {
	//				if (mOnTextureMissedListener != null) {
	//					mOnTextureMissedListener.onTextureMissed();
	//				}
	//			}
	//			//bindTexture();
	//			mValid = true;
	//		}
	//
	//		RenderContext context = RenderContext.acquire();
	//		context.shader = mShader;
	//		//context.texture = mTextures;
	//
	//		final int fadeAlpha = canvas.getAlpha();
	//		float alpha = 1;
	//		if (fadeAlpha < 255) {	//CHECKSTYLE IGNORE
	//			alpha = fadeAlpha * ONE_OVER_255;
	//		}
	//		context.alpha = alpha;
	//
	//		canvas.getFinalMatrix(context);
	//
	//		canvas.addRenderable(mDefaultRenderable, context);
	//	}
	
	public abstract void setColorFilter(int srcColor, Mode mode);

	/**
	 * 渲染骨骼帮助信息
	 * 
	 * @param gl
	 */
	public void renderJoints(GL10 gl) {
		if (!containsJoint()) {
			return;
		}
		// TODO:改用GLES20实现
		throw new RuntimeException("TODO implemented by GLES20");
		// //为保证骨骼始终可见，暂时禁用深度测试
		// gl.glDisable(GL10.GL_DEPTH_TEST);
		// //设置点和线的宽度
		// gl.glPointSize(4.0f);
		// gl.glLineWidth(2.0f);
		// //仅仅启用顶点数据
		// // gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		//
		// //渲染骨骼连线
		// gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);//设置颜色
		// gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mBufJointLinfePosition);
		// //提交渲染
		// gl.glDrawArrays(GL10.GL_LINES, 0, mJointLineCount);
		//
		// //渲染关节点
		// gl.glColor4f(1.0f, 1.0f, 0.0f, 1.0f);//设置颜色
		// gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mBufJointPointPosition);
		// //提交渲染
		// gl.glDrawArrays(GL10.GL_POINTS, 0, mJointPointCount);
		//
		// //重置
		// // gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		// gl.glPointSize(1.0f);
		// gl.glLineWidth(1.0f);
		// gl.glEnable(GL10.GL_DEPTH_TEST);
		// gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);//设置颜色
	}
	
	public Vector3f getVMin() {
		return mvMin;
	}
	
	public Vector3f getVMax() {
		return mvMax;
	}

	public Vector3f getSphereCenter() {
		return mvCenter;
	}

	public float getSphereRadius() {
		return mfRadius;
	}

	public float getXLen() {
		return mXLen;
	}

	public float getYLen() {
		return mYLen;
	}

	public float getZLen() {
		return mZLen;
	}
	
	public int getGroupCount() {
		return mpGroups.length;
	}

	public boolean containsAnimation() {
		return mNumFrames > 0 && mpJoints != null && mpJoints.length > 0;
	}

	public boolean containsJoint() {
		return mpJoints != null && mpJoints.length > 0;
	}
	
	private boolean mRecycleBitmapWhenCleanup = true;
	
	public void setRecycleBitmapWhenCleanup(boolean recycle) {
		mRecycleBitmapWhenCleanup = recycle;
	}

	public void cleanup() {
		synchronized (AbsMS3DModel.this) {
			if (mTextureImg != null) {
				for (int i = 0; i < mTextures.length; i++) {
					recycleTexture(mTextures[i]);
					if (mTextureImg[i] != null && !mTextureImg[i].isRecycled()
							&& mRecycleBitmapWhenCleanup) {
						mTextureImg[i].recycle();
						mTextureImg[i] = null;
					}
					mTextures[i] = null;
				}
			}
			if (USE_VBO && mVerticesVBO != null) {
				for (int i = 0; i < mVerticesVBO.length; ++i) {
					mVerticesVBO[i].clear();
					mVerticesVBO[i] = null;
				}
				mVerticesVBO = null;
			}
			if (USE_VBO && mTexcoordsVBO != null) {
				for (int i = 0; i < mTexcoordsVBO.length; ++i) {
					mTexcoordsVBO[i].clear();
					mTexcoordsVBO[i] = null;
				}
				mTexcoordsVBO = null;
			}
		}
		TextureManager.getInstance().unRegisterTextureListener(this);
	}

	@Override
	public void onTextureLoaded(Texture texture) {
		for (int i = 0; i < mTextureImg.length; i++) {
			BitmapRecycler.recycleBitmapDeferred(mTextures[i].getBitmap());
			mTextures[i].resetBitmap();
			mTextureImg[i] = null;
		}

	}

	/**
	 * 
	 * <br>类描述:纹理丢失的监听器
	 * <br>功能详细描述:
	 * 
	 * @author  guoweijie
	 * @date  [2013-3-25]
	 */
	public interface OnTextureMissedListener {
		public void onTextureMissed();
	}

	private static com.go.gl.util.Vector3f sV0 = new com.go.gl.util.Vector3f();
	private static com.go.gl.util.Vector3f sV1 = new com.go.gl.util.Vector3f();
	private static com.go.gl.util.Vector3f sV2 = new com.go.gl.util.Vector3f();
	private static Vector4f sLocation = new Vector4f();
	/**
	 * 射线与模型的精确碰撞检测
	 * @param ray - 转换到模型空间中的射线
	 * @param trianglePosOut - 返回的拾取后的三角形顶点位置
	 * @return 如果相交，返回true
	 */
	public boolean intersect(Ray ray, com.go.gl.util.Vector3f[] trianglePosOut) {
		boolean bFound = false;
		//存储着射线原点与三角形相交点的距离
		//我们最后仅仅保留距离最近的那一个
		float closeDis = 0.0f;

		for (int i = 0; i < mpGroups.length; i++) {
			//遍历每个Group
			mpBufVertices[i].position(0);
			int vertexCount = mpBufVertices[i].limit() / 3;
			int triangleCount = vertexCount / 3;
			//由于我们提交渲染的Buffer数据是以Triangle List的形式填充的，不牵扯到索引值
			//因此，每3个顶点就组成一个三角形
			for (int idxTriangle = 0; idxTriangle < triangleCount; idxTriangle++) {
				//遍历每个三角形
				//填充三角形数据，顶点v0, v1, v2
				read(mpBufVertices[i], sV0);
				read(mpBufVertices[i], sV1);
				read(mpBufVertices[i], sV2);
				//进行射线和三角行的碰撞检测
				if (ray.intersectTriangle(sV0, sV1, sV2, sLocation)) {
					//如果发生了相交
					if (!bFound) {
						//如果是初次检测到，需要存储射线原点与三角形交点的距离值
						bFound = true;
						closeDis = sLocation.w;
						trianglePosOut[0].set(sV0);
						trianglePosOut[1].set(sV1);
						trianglePosOut[2].set(sV2);
					} else {
						//如果之前已经检测到相交事件，则需要把新相交点与之前的相交数据相比较
						//最终保留离射线原点更近的
						if (closeDis > sLocation.w) {
							closeDis = sLocation.w;
							trianglePosOut[0].set(sV0);
							trianglePosOut[1].set(sV1);
							trianglePosOut[2].set(sV2);
						}
					}
				}
			}
			//重置Buffer
			mpBufVertices[i].position(0);
		}

		return bFound;
	}

	/**
	 * 射线与模型的精确碰撞检测
	 * @param ray - 转换到模型空间中的射线
	 * @param trianglePosOut - 返回的拾取后的三角形顶点位置
	 * @return 如果相交，返回true
	 */
	public int intersectGroup(Ray ray, com.go.gl.util.Vector3f[] trianglePosOut, int[] groupIndices) {
		if (groupIndices == null || groupIndices.length < 1) {
			return -1;
		}
		int pickedGroupIndex = -1;
		//存储着射线原点与三角形相交点的距离
		//我们最后仅仅保留距离最近的那一个
		for (int index = 0; index < groupIndices.length; index++) {
			int i = groupIndices[index];
			if (i < 0 || i >= mpGroups.length) {
				continue;
			}
			FloatBuffer tempBuffer = mpBufVertices[i].duplicate();
			//遍历每个Group
			tempBuffer.position(0);
			int vertexCount = mpBufVertices[i].limit() / 3;
			int triangleCount = vertexCount / 3;
			//由于我们提交渲染的Buffer数据是以Triangle List的形式填充的，不牵扯到索引值
			//因此，每3个顶点就组成一个三角形
			for (int idxTriangle = 0; idxTriangle < triangleCount; idxTriangle++) {
				//遍历每个三角形
				//填充三角形数据，顶点v0, v1, v2
				read(tempBuffer, sV0);
				read(tempBuffer, sV1);
				read(tempBuffer, sV2);
				//进行射线和三角行的碰撞检测
				if (ray.intersectTriangle(sV0, sV1, sV2, sLocation)) {
					//如果发生了相交
					trianglePosOut[0].set(sV0);
					trianglePosOut[1].set(sV1);
					trianglePosOut[2].set(sV2);
					pickedGroupIndex = i;
					break;
				}
			}
			//重置Buffer
			tempBuffer.position(0);
			if (pickedGroupIndex != -1) {
				return pickedGroupIndex;
			}
		}

		return pickedGroupIndex;
	}
	
//	public void transformGroup(int groupIndex, Transformation3D transform) {
//		FmpBufVertices[groupIndex]
//	}

	public void read(FloatBuffer fb, com.go.gl.util.Vector3f v) {
		v.x = fb.get();
		v.y = fb.get();
		v.z = fb.get();
	}
	
	protected boolean[] mGroupDrawEnables;
	
	public boolean enableGroupDraw(int groupId, boolean enable) {
		int groupCount = getGroupCount();
		if (groupId < 0 || groupId >= groupCount) {
			return false;
		}
		if (mGroupDrawEnables == null) {
			mGroupDrawEnables = new boolean[groupCount];
			for (int i = 0; i < groupCount; i++) {
				mGroupDrawEnables[i] = true;
			}
		}
		if (mGroupDrawEnables[groupId] != enable) {
			mGroupDrawEnables[groupId] = enable;
			return true;
		}
		return false;
	}
	
	protected boolean[] mFilterEnables;
	/**
	 * 设置某个组是否需要开启颜色混合. 调用前必须确保调用过{@link #setColorFilter(int, Mode)}了
	 * @param groupId
	 * @param enable
	 * @return
	 */
	public boolean enableGroupFilter(int groupId, boolean enable) {
		if (mFilterEnables != null && groupId >= 0 && groupId <= mFilterEnables.length) {
			if (mFilterEnables[groupId] != enable) {
				mFilterEnables[groupId] = enable;
				return true;
			}
		}
		return false;
	}
	
	protected Animation[] mAnimations;
	protected GroupAnimationListener mGroupAnimationListener;
	
	/**
	 * 设置某个组的动画
	 * @param groupId
	 * @param animation 只支持Alpha动画
	 * @param listener
	 */
	public void startGroupAnimation(int groupId, Animation animation, GroupAnimationListener listener) {
		//必须要有一个LISTENER
		if (listener == null) {
			return;
		}
		if (mAnimations != null && mGroupAnimationListener != null) {
			for (int i = 0; i < mAnimations.length; i++) {
				if (mAnimations[i] != null) {
					mGroupAnimationListener.onGroupAnimationEnd(i);
				}
			}
		}
		mGroupAnimationListener = listener;
		if (mAnimations == null) {
			mAnimations = new Animation[mpGroups.length];
		}
		if (groupId >= 0 && groupId <= mAnimations.length) {
			mAnimations[groupId] = animation;
		}
	}

	/**
	 * 模型 GroupAnimation 的监听器
	 * @author chendongcheng
	 *
	 */
	public interface GroupAnimationListener {
		public void invalidate();
		public long getDrawingTime();
		public void onGroupAnimationStart(int groupId);
		public void onGroupAnimationEnd(int groupId);
	}
	
}