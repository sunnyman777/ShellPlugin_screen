package com.jiubang.shell.orientation;

import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.SettingProxy;
import com.go.util.device.Machine;
import com.go.util.window.OrientationControl;
import com.jiubang.ggheart.plugin.common.OrientationTypes;
import com.jiubang.shell.common.component.ShellContainer;
import com.jiubang.shell.common.management.GLAnimationManager;

/**
 * 横竖屏控制器
 * @author yangguanxiang
 *
 */
public class GLOrientationControler {

	private static boolean sKeepOrientationAllTheTime = false;
	/**
	 * 保持现有横竖屏状态
	 */
	public static void keepCurrentOrientation() {
		if (isKeepOrientationEnable()) {
			OrientationControl.keepCurrentOrientation(GoLauncherActivityProxy.getActivity());
		}
	}

	/**
	 * 把横竖屏状态转换交回系统管理
	 */
	public static void resetOrientation() {
		if (isKeepOrientationEnable()) {
			if (!ShellContainer.sIsTouching && GLAnimationManager.canResetOrientation()
					&& !sKeepOrientationAllTheTime) {
				int oriType = SettingProxy.getGravitySettingInfo().mOrientationType;
				OrientationControl.setOrientation(GoLauncherActivityProxy.getActivity(), oriType);
			}
		}
	}

	private static boolean isKeepOrientationEnable() {
	    //因为对Api >= 9的横屏时设置时可以自由切换横屏
	    boolean isOrienTypeKeep = false;
	    if (Machine.isSDKGreaterNine()) {
	        isOrienTypeKeep = getConfigOrientationType() == OrientationTypes.AUTOROTATION || getConfigOrientationType() == OrientationTypes.HORIZONTAL;
	    } else {
	        isOrienTypeKeep = getConfigOrientationType() == OrientationTypes.AUTOROTATION;
	    }
		return (!OrientationControl.isPreviewModel() && !OrientationControl.isSmallModle())
				&& isOrienTypeKeep;
	}

	/**
	 * 获取桌面设置屏幕状态
	 * @return
	 */
	public static int getConfigOrientationType() {
		return SettingProxy.getGravitySettingInfo().mOrientationType;
	}

	/**
	 * 设置屏幕状态
	 * @param type: OrientationControl.AUTOROTATION ...
	 */
	public static void setOrientationType(int type) {
		if (!sKeepOrientationAllTheTime) {
			OrientationControl.setOrientation(GoLauncherActivityProxy.getActivity(), type);
		}
	}

	/**
     * 设置屏幕预览状态
     * @param type: OrientationControl.AUTOROTATION ...
     */
    public static void setPreviewOrientationType(int type) {
        if (!sKeepOrientationAllTheTime) {
            int orientation = getRequestOrientation();
            if ((orientation == -1 || orientation == 6) && Machine.isSDKGreaterNine()) {
                OrientationControl.setByRoate(GoLauncherActivityProxy.getActivity(), type);
            } else {
                OrientationControl.setOrientation(GoLauncherActivityProxy.getActivity(), type);
            }
            
        }
    }
	
	/**
	 * 获取当前屏幕状态
	 * @return
	 */
	public static int getRequestOrientation() {
		return GoLauncherActivityProxy.getActivity().getRequestedOrientation();
	}

	/**
	 * 设置屏幕状态
	 * @param type: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED ...
	 */
	public static void setRequestOrientation(int type) {
		if (!sKeepOrientationAllTheTime) {
			GoLauncherActivityProxy.getActivity().setRequestedOrientation(type);
		}
	}

	public static void setSmallModle(boolean bool) {
		OrientationControl.setSmallModle(bool);
	}

	public static void setPreviewModel(boolean flag) {
		OrientationControl.setPreviewModel(flag);
	}

	/**
	 * true：在整个过程中保持横竖屏，不管中间是否调用了resetOrientation
	 * false： 释放保持横竖屏状态
	 * @param keep
	 */
	public static void keepOrientationAllTheTime(boolean keep) {
		if (keep) {
			sKeepOrientationAllTheTime = true;
			keepCurrentOrientation();
		} else {
			sKeepOrientationAllTheTime = false;
			resetOrientation();
		}
	}
	
	/**
	 * true：在整个过程中保持横竖屏，不管中间是否调用了resetOrientation
	 * int: 保持的状态：横屏/竖屏/自动旋转
	 * false： 释放保持横竖屏状态
	 * @param keep
	 */
	public static void keepOrientationAllTheTime(boolean keep, int type) {
		sKeepOrientationAllTheTime = keep;
		OrientationControl.setOrientation(GoLauncherActivityProxy.getActivity(), type);
	}
}
