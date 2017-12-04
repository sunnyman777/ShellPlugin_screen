package com.jiubang.shell.common.management;

import java.util.concurrent.LinkedBlockingQueue;

import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * Job管理者
 * 用于管理多个任务串行执行的类
 * @author yangguanxiang
 *
 */
public class JobManager {

	private static final long TOTAL_DURATION_OFFSET = 1000;

	private static LinkedBlockingQueue<Job> sJobQueue = new LinkedBlockingQueue<Job>();

	private static JobListener sListener;

	/**
	 * 提交一个任务
	 * @param job
	 */
	public synchronized static void postJob(final Job job) {
		if (sJobQueue.isEmpty()) {
			sJobQueue.add(job);
			job.start();
		} else {
			sJobQueue.add(job);
		}
	}

	public static void setJobListener(JobListener listener) {
		sListener = listener;
	}

	/**
	 * 任务完成后回调，业务层不允许调用该方法
	 * @param job
	 */
	synchronized static void onJobFinished(final Job job) {
		if (sJobQueue.contains(job)) {
			sJobQueue.remove(job);
			if (sListener != null) {
				sListener.onJobEnd(job);
			}
//			ShellContainer.setDispatchTouchEvent(true);
			if (!sJobQueue.isEmpty()) {
				Job nextJob = sJobQueue.peek();
				nextJob.start();
			}
		}
	}

	/**
	 * Job类
	 * @author yangguanxiang
	 *
	 */
	public static class Job {
		private int mId;
		private Runnable mRunnable;
		private AnimationTask mAnimationTask;
		private boolean mAllowDispatchTouchEvent = true;

		/**
		 * 
		 * @param id
		 * @param runnable
		 * @param allowDispatchTouchEvent 是否允许传递触摸事件
		 */
		public Job(int id, Runnable runnable, boolean allowDispatchTouchEvent) {
			mId = id;
			mRunnable = runnable;
			mAllowDispatchTouchEvent = allowDispatchTouchEvent;
		}

		/**
		 * 
		 * @param id
		 * @param animationTask
		 * @param allowDispatchTouchEvent 是否允许传递触摸事件
		 */
		public Job(int id, AnimationTask animationTask, boolean allowDispatchTouchEvent) {
			mId = id;
			mAnimationTask = animationTask;
			mAllowDispatchTouchEvent = allowDispatchTouchEvent;
		}

		public void start() {
			if (sListener != null) {
				sListener.onJobStart(this);
			}
//			ShellContainer.setDispatchTouchEvent(mAllowDispatchTouchEvent);
			if (mRunnable != null) {
				ShellAdmin.sShellManager.getContentView().post(new Runnable() {

					@Override
					public void run() {
						mRunnable.run();
						JobManager.onJobFinished(Job.this);
					}
				});
			}
			if (mAnimationTask != null) {
				mAnimationTask.setJob(this);
				GLAnimationManager.startAnimation(mAnimationTask);
				if (!mAllowDispatchTouchEvent) {
					//避免一些特殊原因动画不能执行完成，导致卡死不能操作的问题
					long totalDuration = mAnimationTask.getTotalDuration();
					ShellAdmin.sShellManager.getContentView().postDelayed(new Runnable() {

						@Override
						public void run() {
							if (sJobQueue.contains(Job.this)) {
								GLAnimationManager.cancelAnimation(mAnimationTask);
							}
						}
					}, totalDuration + TOTAL_DURATION_OFFSET);
				}
			}
		}

		public int getId() {
			return mId;
		}

		public Runnable getRunnable() {
			return mRunnable;
		}

		public AnimationTask getAnimationTask() {
			return mAnimationTask;
		}

		public boolean isAllowDispatchTouchEvent() {
			return mAllowDispatchTouchEvent;
		}
	}

	/**
	 * Job状态监听者
	 * @author yangguanxiang
	 *
	 */
	public static interface JobListener {
		public void onJobStart(Job job);
		public void onJobEnd(Job job);
	}

}
