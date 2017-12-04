package com.jiubang.shell.common.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.AnimationSet;
import com.go.gl.view.GLView;
import com.jiubang.shell.common.management.JobManager.Job;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.orientation.GLOrientationControler;

/**
 * 动画管理者
 * @author yangguanxiang
 *
 */
public class GLAnimationManager {
	private static final int RESET_ORIENTATION_DELAY = 100;

	private static HashSet<AnimationTask> sRunningPool = new HashSet<AnimationTask>();

	/**
	 * 开始动画
	 * @param task
	 */
	public synchronized static void startAnimation(AnimationTask task) {
		if (task.isValid()) {
			if (task.mKeepOrientation) {
				GLOrientationControler.keepCurrentOrientation();
			}
			task.exec();
		}
	}

	/**
	 * 取消动画
	 * @param task
	 */
	public synchronized static void cancelAnimation(AnimationTask task) {
		synchronized (sRunningPool) {
			if (sRunningPool.contains(task)) {
				task.cancel();
				sRunningPool.remove(task);
			}
		}
	}

	/**
	 * 判断是否可以重置横竖屏
	 * @return
	 */
	public static boolean canResetOrientation() {
		boolean ret = false;
		synchronized (sRunningPool) {
			ret = sRunningPool.isEmpty();
			if (!ret) {
				boolean keep = false;
				for (AnimationTask task : sRunningPool) {
					keep = task.mKeepOrientation;
					if (keep) {
						break;
					}
				}
				if (!keep) {
					ret = true;
				}
			}
		}
		return ret;
	}

	/**
	 * 动画任务封装类
	 * @author yangguanxiang
	 *
	 */
	public static class AnimationTask {
		/**
		 * 同时执行
		 */
		public static final int PARALLEL = 0;
		/**
		 * 排队执行
		 */
		public static final int SERIAL = 1;

		private int mType = PARALLEL;
		private ArrayList<GLView> mTargetList;
		private ArrayList<Animation> mAnimationList;
		private ArrayList<AnimationListener> mListenerList;
		private BatchAnimationObserver mObserver;
		private int mWhat;
		private Object[] mParams;
		private boolean mKeepOrientation;
		private MainAnimationListener mMainListener;
		private Job mSelfJob;
		private boolean mIsFinished; //此变量用于标识task是否完成，用于动画没有执行或被pause导致业务逻辑无法继续的补救措施
		private boolean mUseRemediation; //是否使用补救措施

		/**
		 * 
		 * @param keepOrientation 是否保持屏幕状态，防止横竖屏切换
		 * @param type 执行类型 PARALLEL 或 SERIAL
		 */
		public AnimationTask(boolean keepOrientation, int type) {
			mKeepOrientation = keepOrientation;
			mType = type;
			mTargetList = new ArrayList<GLView>();
			mAnimationList = new ArrayList<Animation>();
			mListenerList = new ArrayList<AnimationListener>();
		}

		/**
		 * 
		 * @param target 执行动画的View
		 * @param animation 动画本身
		 * @param listener 动画监听器
		 * @param keepOrientation 是否保持屏幕状态，防止横竖屏切换
		 * @param type 执行类型 PARALLEL 或 SERIAL
		 */
		public AnimationTask(GLView target, Animation animation, AnimationListener listener,
				boolean keepOrientation, int type) {
			mKeepOrientation = keepOrientation;
			mType = type;

			mTargetList = new ArrayList<GLView>();
			mTargetList.add(target);

			mAnimationList = new ArrayList<Animation>();
			mAnimationList.add(animation);

			mListenerList = new ArrayList<AnimationListener>();
			mListenerList.add(listener);
		}

		/**
		 * 
		 * @param target 执行动画的View
		 * @param animation 动画本身
		 * @param listener 动画监听器
		 */
		public void addAnimation(GLView target, Animation animation, AnimationListener listener) {
			mTargetList.add(target);
			mAnimationList.add(animation);
			mListenerList.add(listener);
		}

		/**
		 * 设置批量动画观察者
		 * @param observer 观察者
		 * @param what 批量动画标识
		 * @param params 回调使用的参数
		 */
		public void setBatchAnimationObserver(BatchAnimationObserver observer, int what,
				Object... params) {
			mObserver = observer;
			mWhat = what;
			mParams = params;
		}

		public ArrayList<GLView> getTargetList() {
			return mTargetList;
		}

		public ArrayList<Animation> getAnimationList() {
			return mAnimationList;
		}

		public ArrayList<AnimationListener> getAnimationListenerList() {
			return mListenerList;
		}

		public boolean isValid() {
			return !mTargetList.isEmpty() && !mAnimationList.isEmpty()
					&& mTargetList.size() == mAnimationList.size()
					&& mTargetList.size() == mListenerList.size()
					&& mAnimationList.size() == mListenerList.size();
		}

		private void exec() {
			//			if (mTargetList.size() != mAnimationList.size()
			//					|| mTargetList.size() != mListenerList.size()
			//					|| mAnimationList.size() != mListenerList.size()) {
			//				throw new IllegalArgumentException("three list size must be the same");
			//			}
			synchronized (sRunningPool) {
				boolean needAddToRunningPool = false;
				mMainListener = new MainAnimationListener(this);
				int size = mTargetList.size();
				if (size > 0) {
					for (int i = 0; i < size; i++) {
						GLView target = mTargetList.get(i);
						Animation animation = mAnimationList.get(i);
						AnimationListener listener = mListenerList.get(i);
						if (target != null && animation != null) {
							if (target.getAnimation() != animation || animation.hasEnded()) {
								if (listener != null) {
									mMainListener.mListenerMap.put(animation, listener);
								}
								mMainListener.mAnimationSet.add(animation);
								animation.setAnimationListener(mMainListener);
								needAddToRunningPool = true;
							}
							if (mType == PARALLEL) {
								execAnim(target, animation);
							}
						}
					}
					if (mType == SERIAL) {
						GLView target = mTargetList.get(0);
						Animation animation = mAnimationList.get(0);
						execAnim(target, animation);
					}
				}
				if (needAddToRunningPool) {
					//					Log.i("Test", "add Task: " + this);
					sRunningPool.add(this);
				}

				if (mUseRemediation) {
					//动画没有执行或被pause导致业务逻辑无法继续的补救措施，保证onFinished方法被调用
					ShellAdmin.sShellManager.getContentView().postDelayed(new Runnable() {

						@Override
						public void run() {
							synchronized (AnimationTask.this.mMainListener.mAnimationSet) {
								if (!AnimationTask.this.mIsFinished) {
									for (GLView target : mTargetList) {
										if (target != null) {
											Animation animation = target.getAnimation();
											if (animation != null && !animation.hasEnded()) {
												onAnimationCancel(animation);
											}
										}
									}
								}
							}
						}
					}, getTotalDuration() + 100);
				}
			}

		}

		private void exec(int index) {
			GLView target = mTargetList.get(index);
			Animation animation = mAnimationList.get(index);
			execAnim(target, animation);
		}

		private void cancel() {
			for (GLView target : mTargetList) {
				if (target != null) {
					Animation animation = target.getAnimation();
					if (animation != null) {
						if (animation.hasStarted()) {
							target.clearAnimation();
						} else {
							onAnimationCancel(animation);
						}
					}
				}
			}
		}

		private void execAnim(GLView target, Animation animation) {
			if (target != null) {
				Animation curAnimation = target.getAnimation();
				if (curAnimation != null) {
					//					if (curAnimation.hasStarted()) {
					//						curAnimation.cancel();
					//					} else {
					//						onAnimationCancel(curAnimation);
					//					}
					if (!curAnimation.hasStarted()) {
						onAnimationCancel(curAnimation);
					}
					//					Log.i("Test", "clearAnimation: " + this);
					target.clearAnimation();
				}
				target.startAnimation(animation);
			}
		}

		private void onAnimationCancel(Animation animation) {
			//			Log.i("Test", "onAnimationCancel: " + this);
			synchronized (sRunningPool) {
				for (AnimationTask task : sRunningPool) {
					if (task.mMainListener != null) {
						synchronized (task.mMainListener.mAnimationSet) {
							if (task.mMainListener.mAnimationSet.contains(animation)) {
								task.mMainListener.onAnimationEnd(animation);
								break;
							}
						}
					}
				}
			}
		}

		public void cleanup() {
			reset();
			mObserver = null;
			mSelfJob = null;
		}

		public void reset() {
			mIsFinished = false;
			mTargetList.clear();
			mAnimationList.clear();
			mListenerList.clear();
		}

		void setJob(Job job) {
			mSelfJob = job;
		}

		/**
		 * 获取动画总时长（此方法未必准确）
		 * @return
		 */
		long getTotalDuration() {
			long duration = 0;
			if (mType == SERIAL) {
				for (Animation anim : mAnimationList) {
					duration += anim.getDuration() + anim.getStartOffset();
					if (anim instanceof AnimationSet) {
						duration = getAnimationSetDuration((AnimationSet) anim);
					}
				}
			} else {
				long maxDuration = 0;
				for (Animation anim : mAnimationList) {
					long d = anim.getDuration() + anim.getStartOffset();
					if (anim instanceof AnimationSet) {
						d = getAnimationSetDuration((AnimationSet) anim);
					}
					if (d > maxDuration) {
						maxDuration = d;
					}
				}
				duration = maxDuration;
			}
			return duration;
		}

		private long getAnimationSetDuration(AnimationSet set) {
			long maxDuration = set.getDuration() + set.getStartOffset();
			List<Animation> animList = set.getAnimations();
			for (Animation anim : animList) {
				long subDuration = anim.getStartOffset() + anim.getDuration();
				if (subDuration > maxDuration) {
					maxDuration = subDuration;
				}
			}
			return maxDuration;
		}

		/**
		 * 是否使用补救措施，保证onFinished方法被调用，默认不使用
		 * 说明：某些特殊场景可能使动画没有执行或被pause导致在onFinished中的业务逻辑无法继续，
		 * 使用该方法能够在动画时间后忽略动画是否完成强制执行onFinished，从而保证业务正确完成
		 * @param use
		 */
		public void useRemediation(boolean use) {
			mUseRemediation = use;
		}
	}

	/**
	 * 
	 * @author yangguanxiang
	 *
	 */
	private static class MainAnimationListener extends AnimationListenerAdapter {

		public HashMap<Animation, AnimationListener> mListenerMap = new HashMap<Animation, AnimationListener>();
		public HashSet<Animation> mAnimationSet = new HashSet<Animation>();
		private AnimationTask mTask;
		private boolean mFirstAnimStart = true;
		private byte[] mLock = new byte[0];
		public MainAnimationListener(AnimationTask task) {
			mTask = task;
		}

		@Override
		public void onAnimationStart(Animation animation) {
			if (mListenerMap.containsKey(animation)) {
				AnimationListener listener = mListenerMap.get(animation);
				if (listener != null) {
					listener.onAnimationStart(animation);
				}
			}

			synchronized (mLock) {
				if (mFirstAnimStart) {
					if (mTask.mObserver != null) {
						mTask.mObserver.onStart(mTask.mWhat, mTask.mParams);
					}
					mFirstAnimStart = false;
				}
			}
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			synchronized (mAnimationSet) {
				//				Log.i("Test", "remove animation from animation set: " + mTask);
				mAnimationSet.remove(animation);
				if (mAnimationSet.isEmpty()) {
					if (!mTask.mIsFinished) {
						mTask.mIsFinished = true;
						if (mListenerMap.containsKey(animation)) {
							AnimationListener listener = mListenerMap.get(animation);
							if (listener != null) {
								listener.onAnimationEnd(animation);
							}
						}
						if (mTask.mObserver != null) {
							mTask.mObserver.onFinish(mTask.mWhat, mTask.mParams);
						}
						if (mTask.mSelfJob != null) {
							JobManager.onJobFinished(mTask.mSelfJob);
						}
						synchronized (sRunningPool) {
							//							Log.i("Test", "remove task: " + mTask);
							sRunningPool.remove(mTask);
							if (sRunningPool.isEmpty()) {
								ShellAdmin.sShellManager.getShell().getContainer().getGLRootView()
										.postDelayed(new Runnable() {

											@Override
											public void run() {
												GLOrientationControler.resetOrientation();
											}
										}, RESET_ORIENTATION_DELAY);
							} else {
								boolean keep = false;
								for (AnimationTask task : sRunningPool) {
									keep = task.mKeepOrientation;
									if (keep) {
										break;
									}
								}
								if (!keep) {
									ShellAdmin.sShellManager.getShell().getContainer()
											.getGLRootView().postDelayed(new Runnable() {

												@Override
												public void run() {
													GLOrientationControler.resetOrientation();
												}
											}, RESET_ORIENTATION_DELAY);
								}
							}
						}
					}
				} else {
					if (mListenerMap.containsKey(animation)) {
						AnimationListener listener = mListenerMap.get(animation);
						if (listener != null) {
							listener.onAnimationEnd(animation);
						}
					}
					if (mTask.mType == AnimationTask.SERIAL) {
						int index = mTask.mAnimationList.indexOf(animation);
						mTask.exec(++index);
					}
				}
				mListenerMap.remove(animation);
				animation.setAnimationListener(null);
			}
		}
		@Override
		public void onAnimationRepeat(Animation animation) {
			if (mListenerMap.containsKey(animation)) {
				AnimationListener listener = mListenerMap.get(animation);
				if (listener != null) {
					listener.onAnimationRepeat(animation);
				}
			}
		}
	}

	/**
	 * 
	 * 类描述: 批量动画状态观察者
	 * 
	 * @author  yangguanxiang
	 */
	public static interface BatchAnimationObserver {
		public void onStart(int what, Object[] params);

		public void onFinish(int what, Object[] params);
	}

}
