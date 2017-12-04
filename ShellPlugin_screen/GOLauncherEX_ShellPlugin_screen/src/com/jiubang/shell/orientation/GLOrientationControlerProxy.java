package com.jiubang.shell.orientation;

import com.jiubang.ggheart.plugin.shell.IOrientationControler;

/**
 * 
 * @author yangguanxiang
 *
 */
public class GLOrientationControlerProxy implements IOrientationControler {

	@Override
	public void keepCurrentOrientation() {
		GLOrientationControler.keepCurrentOrientation();
	}

	@Override
	public void resetOrientation() {
		GLOrientationControler.resetOrientation();
	}

	@Override
	public int getConfigOrientationType() {
		return GLOrientationControler.getConfigOrientationType();
	}

	@Override
	public void setOrientationType(int type) {
		GLOrientationControler.setOrientationType(type);
	}

	@Override
	public void setPreviewOrientationType(int type) {
		GLOrientationControler.setPreviewOrientationType(type);
	}

	@Override
	public int getRequestOrientation() {
		return GLOrientationControler.getRequestOrientation();
	}

	@Override
	public void setRequestOrientation(int type) {
		GLOrientationControler.setRequestOrientation(type);
	}

	@Override
	public void setSmallModle(boolean bool) {
		GLOrientationControler.setSmallModle(bool);
	}

	@Override
	public void setPreviewModel(boolean flag) {
		GLOrientationControler.setPreviewModel(flag);
	}

	@Override
	public void keepOrientationAllTheTime(boolean keep) {
		GLOrientationControler.keepOrientationAllTheTime(keep);
	}

	@Override
	public void keepOrientationAllTheTime(boolean keep, int type) {
		GLOrientationControler.keepOrientationAllTheTime(keep, type);
	}

}
