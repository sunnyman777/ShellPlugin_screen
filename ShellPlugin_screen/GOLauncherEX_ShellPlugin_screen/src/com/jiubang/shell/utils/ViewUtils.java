package com.jiubang.shell.utils;

import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.view.GLViewParent;
import com.go.proxy.GoLauncherActivityProxy;
import com.go.util.device.Machine;

/**
 * 
 * @author yangguanxiang
 *
 */
public class ViewUtils {

	/**
	 * 清理该ViewGroup下的所有儿子资源，不包括该ViewGroup
	 * @param group
	 */
	public static void cleanupAllChildren(GLViewGroup group) {
		int count = group.getChildCount();
		for (int i = 0; i < count; i++) {
			GLView child = group.getChildAt(i);
			if (child != null) {
				child.cleanup();
			}
		}
		group.removeAllViews();
	}

	/**
	 * 该儿子是否可见
	 * @param child
	 * @return
	 */
	public static boolean isVisibleOnTree(GLView child) {
		GLView view = child;
		boolean visible = true;
		while (view != null) {
			visible &= view.isVisible();
			GLViewParent parent = view.getGLParent();
			if (parent != view.getGLRootView()) {
				view = (GLView) parent;
			} else {
				view = null;
			}
		}
		return visible;
	}

	public static void autoFitDrawingCacheScale(GLView view) {
		if (Machine.isModel(Machine.S5360_MODEL)) {
			int w = view.getWidth();
			float scaleX = 1.0f;
			float threshold = GoLauncherActivityProxy.getScreenWidth();
			if (w >= threshold) {
				scaleX = 1.0f * threshold / w;
			} else if (w > 2) {
				scaleX = Double.valueOf(Math.pow(2, (int) (Math.log(w) / Math.log(2)) + 1) / w)
						.floatValue();
			}
			view.setDrawCacheScale(scaleX, 1.0f);
		}
	}
}
