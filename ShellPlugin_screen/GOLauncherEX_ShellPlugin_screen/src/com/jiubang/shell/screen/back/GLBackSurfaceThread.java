package com.jiubang.shell.screen.back;

import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.view.SurfaceHolder;

/**
 * 
 * @author jiangxuwen
 * 
 */
public class GLBackSurfaceThread extends Thread {
	private SurfaceHolder mThreadSurfaceHolder;
	private GLBackSurfaceView mThreadSurfaceView;
	private boolean mThreadRun = false;

	public GLBackSurfaceThread(SurfaceHolder surfaceHolder, GLBackSurfaceView surfaceView) {
		mThreadSurfaceHolder = surfaceHolder;
		mThreadSurfaceView = surfaceView;
	}

	public void setRunning(boolean b) {
		mThreadRun = b;
	}

	@Override
	public void run() {
		while (!Thread.interrupted() && mThreadRun) {
			Canvas canvas = null;

			try {
				canvas = mThreadSurfaceHolder.lockCanvas(null);
				synchronized (mThreadSurfaceHolder) {
					if (canvas != null) {
						int saveCount = canvas.save();
						canvas.drawColor(0, Mode.CLEAR);
						mThreadSurfaceView.onDraw(canvas);
						canvas.restoreToCount(saveCount);
					}
				}
				sleep(15);
			} catch (InterruptedException e) {
				// e.printStackTrace();
			} finally {
				if (canvas != null) {
					mThreadSurfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}
}
