package com.jiubang.shell.model;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff.Mode;
import android.os.SystemClock;
import android.view.animation.AnimationUtils;

import com.go.gl.animation.Animation;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.util.Ray;
import com.go.gl.view.GLView;
import com.jiubang.shell.analysis3dmode.AbsMS3DModel;
import com.jiubang.shell.analysis3dmode.AbsMS3DModel.GroupAnimationListener;
import com.jiubang.shell.analysis3dmode.Matrix4f;
import com.jiubang.shell.analysis3dmode.Ms3DModel;
import com.jiubang.shell.analysis3dmode.Vector3f;

/**
 * 3d模型的bean类 
 * 实现了模型的基本操作
 * 
 * @author  guoweijie
 * @date  [2013-3-13]
 */
public class ModelItem implements Ms3DModel.AnimationListener, Ms3DModel.OnTextureMissedListener {

	private GLView mView;

	/**
	 * 模型的解析以及渲染类
	 */
	protected AbsMS3DModel mModel = null;

	/**
	 * 模型的名字
	 */
	protected String mFileName;

	private boolean mAnimating = false;

	public ModelItem(GLView view, String fileName, int[] resIds, boolean recyleImgAfterLoaded) {
		mView = view;
		mFileName = fileName;
		mModel = loadModle(mView.getContext(), fileName, resIds, recyleImgAfterLoaded);
		mModel.setAnimationListener(this);
		mModel.setOnTextureMissedListener(this);
	}

	public ModelItem(GLView view, String fileName, boolean recyleImgAfterLoaded) {
		mView = view;
		mFileName = fileName;
		mModel = loadModle(view.getContext(), fileName, recyleImgAfterLoaded);
		mModel.setAnimationListener(this);
		mModel.setOnTextureMissedListener(this);
	}
	
	/**
	 * 在别的apk里拿模型，找不到文件会抛出异常
	 */
	public ModelItem(Context context, GLView view, String fileName, boolean recyleImgAfterLoaded)
			throws IOException {
		mView = view;
		mFileName = fileName;
		mModel = loadModleThrowException(context, fileName, recyleImgAfterLoaded);
		mModel.setAnimationListener(this);
		mModel.setOnTextureMissedListener(this);
	}

	public void setTexture(Bitmap bitmap) {
		if (mModel != null) {
			Bitmap[] imgs = new Bitmap[1];
			imgs[0] = bitmap;
			mModel.setTexture(imgs);
		}
	}

	public void setTexture(Bitmap[] imgs) {
		if (mModel != null) {
			mModel.setTexture(imgs);
		}
	}

	public boolean getLoadFlag() {
		if (mModel != null) {
			return mModel.mIsLoadSuccess;
		}

		return false;
	}
	
	/**
	 * 设置颜色混合
	 * @param srcColor
	 * @param mode
	 */
	public void setColorFilter(int srcColor, Mode mode) {
		if (mModel != null) {
			mModel.setColorFilter(srcColor, mode);
		}
	}
	
	/**
	 * 某个组的u颜色混合的开关
	 * @param groupId
	 * @param enable
	 */
	public void enableGroupFilter(int groupId, boolean enable) {
		if (mModel != null && mModel.enableGroupFilter(groupId, enable)) {
			mView.invalidate();
		}
	}

	/**
	 * 设置某个组的绘制开关
	 * @param groupId
	 * @param enable
	 */
	public void enableGroupDraw(int groupId, boolean enable) {
		if (mModel != null) {
			if (mModel.enableGroupDraw(groupId, enable)) {
				mView.invalidate();
			}
		}
	}

	/**
	 * 设置一群组的绘制开关
	 * @param groups
	 * @param enable
	 */
	public void enableGroupDraw(int[] groups, boolean enable) {
		if (mModel != null && groups != null) {
			boolean result = false;
			for (int i = 0 ; i < groups.length; i++) {
				result |= mModel.enableGroupDraw(groups[i], enable);
			}
			if (result) {
				mView.invalidate();
			}
		}
	}

	/**
	 * 开始某个组的动画, 只支持Alpha动画
	 * @param groupId
	 * @param animation
	 * @param listener
	 */
	public void startGroupAnimation(int groupId, Animation animation, GroupAnimationListener listener) {
		if (mModel != null) {
			mModel.startGroupAnimation(groupId, animation, listener);
		}
	}

	public String getFileName() {
		return mFileName;
	}
	
	private int mFrames;
	private int mMsPerFrame;
	private final static int SAMPLE_PERIOD_FRAMES = 10;
	private final static float SAMPLE_FACTOR = 1.0f / SAMPLE_PERIOD_FRAMES;
	private long mStartTime;

	private void updateTime() {
		long time = SystemClock.uptimeMillis();
		//if (mStartTime == 0) {
		//mStartTime = time;
		//}
		if (mFrames++ == SAMPLE_PERIOD_FRAMES) {
			mFrames = 0;
			long delta = time - mStartTime;
			mStartTime = time;
			mMsPerFrame = (int) (delta * SAMPLE_FACTOR);
		}
	}

	public void startAnimating(boolean isFillAfter, boolean isFont) {
		mAnimating = true;
		mModel.stopAnimate();
		long time = SystemClock.uptimeMillis();
		mStartTime = time;
		mView.invalidate();
	}

	private int mCurFrame;
	public void setCurFrame(int curFrame) {
		if (mCurFrame == curFrame) {
			return;
		}
		mCurFrame = curFrame;
		mModel.showFrame(mCurFrame);
		mModel.fillRenderBuffer(); //更新顶点缓存
		mView.invalidate();
	}

	public int getCurFrame() {
		return mCurFrame;
	}

	private boolean mFrameAnimating;
	private long mFrameAnimStartTime;
	private long mDuration;
	private int mStartFrame;
	private int mStopFrame;
	public void startFrameAnimation(int startFrame, int stopFrame, long duration) {
		mStartFrame = startFrame;
		mStopFrame = stopFrame;
		mDuration = duration;
		mFrameAnimStartTime = AnimationUtils.currentAnimationTimeMillis();
		mFrameAnimating = true;
		mView.invalidate();
	}

	private void onFrameAnimation() {
		long stepTime = AnimationUtils.currentAnimationTimeMillis() - mFrameAnimStartTime;
		float t = stepTime * 1.0f / mDuration;
		t = Math.max(0, Math.min(t, 1));
		if (t == 1) {
			mFrameAnimating = false;
		}
		int curFrame = (int) (mStartFrame + (mStopFrame - mStartFrame) * t);
		setCurFrame(curFrame);
	}
	/**
	 * 绘制模型
	 * @param textures
	 */
	public void render(GLCanvas canvas) {

		if (mAnimating && mModel.containsAnimation()) {
			//如果模型有动画，那么按时间就更新动画
			if (mMsPerFrame > 0) {
				mModel.animate(mMsPerFrame * 0.001f * 0.1f); //将毫秒数转化为秒, /1000
			}
			mModel.fillRenderBuffer(); //更新顶点缓存
			updateTime();
			mView.invalidate();
		} else {
			if (mFrameAnimating && mModel.containsAnimation()) {
				onFrameAnimation();
			}
		}
		mModel.fillRenderBuffer();
		mModel.render(canvas); //渲染模型
		
		/*if (mModel != null && mModel.mIsLoadSuccess) {
			mModel.render(canvas);
		}*/

		//updateTime();
	}

	/*-------------------------------静态函数------------------------*/
	protected AbsMS3DModel loadModle(Context context, final String fileName, int[] resIds, boolean recyleImgAfterLoaded) {

		InputStream is = null;
		try {
			is = context.getResources().getAssets().open(fileName);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Ms3DModel model = null;
		try {
			Bitmap[] imgs = new Bitmap[resIds.length];
			for (int i = 0; i < resIds.length; i++) {
				imgs[i] = BitmapFactory.decodeResource(context.getResources(), resIds[i]);
			}
			//Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
			model = new Ms3DModel(imgs, recyleImgAfterLoaded);
			model.loadModel(is);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return model;
	}

	/*-------------------------------静态函数------------------------*/
	protected AbsMS3DModel loadModle(Context context, String fileName, boolean recyleImgAfterLoaded) {

		InputStream is = null;
		try {
			is = context.getResources().getAssets().open(fileName);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Ms3DModel model = new Ms3DModel(recyleImgAfterLoaded);

		try {
			model.loadModel(is);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return model;
	}
	
	/**
	 * <br>功能简述:加载模型，找不到对应文件会抛出异常
	 * <br>功能详细描述:用于在别的apk里加载模型
	 * <br>注意:
	 * @param context
	 * @param fileName
	 * @param recyleImgAfterLoaded
	 * @return
	 * @throws IOException
	 */
	protected AbsMS3DModel loadModleThrowException(Context context, String fileName,
			boolean recyleImgAfterLoaded) throws IOException {

		InputStream is = null;
		is = context.getResources().getAssets().open(fileName);

		Ms3DModel model = new Ms3DModel(recyleImgAfterLoaded);

		try {
			model.loadModel(is);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return model;
	}

	@Override
	public void onAnimationEnd() {
		mAnimating = false;
		if (mAnimationListener != null) {
			mAnimationListener.onAnimationFinish();
		}
		//		mModel.animate(0);
		//		mView.invalidate();
	}

	public void stopAnimating() {
		mAnimating = false;
		mModel.stopAnimate();
	}
	
	public Vector3f getVMin() {
		return null == mModel ? null : mModel.getVMin();
	}
	
	public Vector3f getVMax() {
		return null == mModel ? null : mModel.getVMax();
	}

	public Vector3f getSphereCenter() {
		if (mModel != null) {
			return mModel.getSphereCenter();
		}
		return null;
	}

	public float getSphereRadius() {
		if (mModel != null) {
			return mModel.getSphereRadius();
		}
		return -1;
	}

	public float getXLen() {
		if (mModel != null) {
			return mModel.getXLen();
		}
		return -1;
	}

	public float getYLen() {
		if (mModel != null) {
			return mModel.getYLen();
		}
		return -1;
	}

	public float getZLen() {
		if (mModel != null) {
			return mModel.getZLen();
		}
		return -1;
	}

	public int getGroupCount() {
		if (mModel != null) {
			return mModel.getGroupCount();
		}
		return -1;
	}

	public void setBlendMode(int srcFactor, int dstFactor) {
		if (mModel != null) {
			mModel.setBlendMode(srcFactor, dstFactor);
		}
	}
	
	public void setJointFrame(int jointIndex, Matrix4f matKeyframe) {
		if (mModel != null) {
			mModel.setJointFrame(jointIndex, matKeyframe);
		}
	}
	
	public void updateVertex() {
		if (mModel != null) {
			mModel.updateVertex();
			mModel.fillRenderBuffer();
		}
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
		public void onAnimationFinish();
	}

	private AnimationListener mAnimationListener;

	public void setAnimationListener(AnimationListener listener) {
		mAnimationListener = listener;
	}
	
	public void setRecycleBitmapWhenCleanup(boolean recycle) {
		if (mModel != null) {
			mModel.setRecycleBitmapWhenCleanup(recycle);
		}
	}

	public void cleanup() {
		if (mModel != null) {
			mModel.cleanup();
		}
	}

	private OnBitmapNeedLoadedListener mOnBitmapNeedLoadedListener;

	public void setOnBitmapNeedLoadedListener(OnBitmapNeedLoadedListener listener) {
		mOnBitmapNeedLoadedListener = listener;
	}

	/**
	 * 
	 * <br>类描述:纹理丢失的监听器
	 * <br>功能详细描述:
	 * 
	 * @author  guoweijie
	 * @date  [2013-3-25]
	 */
	public interface OnBitmapNeedLoadedListener {
		public void onBitmapNeedLoaded();
	}

	@Override
	public void onTextureMissed() {
		if (mOnBitmapNeedLoadedListener != null) {
			mOnBitmapNeedLoadedListener.onBitmapNeedLoaded();
		}
	}

	/**
	 * 射线与模型的精确碰撞检测
	 * @param ray - 转换到模型空间中的射线
	 * @param trianglePosOut - 返回的拾取后的三角形顶点位置
	 * @return 如果相交，返回true
	 */
	public boolean intersect(Ray ray, com.go.gl.util.Vector3f[] trianglePosOut) {
		return mModel.intersect(ray, trianglePosOut);
	}

	public int intersectGroup(Ray ray, com.go.gl.util.Vector3f[] trianglePosOut, int[] groupIndices) {
		return mModel.intersectGroup(ray, trianglePosOut, groupIndices);
	}
	
	public void setGroupTexture(int groupIndex, Bitmap img, boolean recycle) {
		mModel.setGroupTexture(groupIndex, img, recycle);
	}
}
