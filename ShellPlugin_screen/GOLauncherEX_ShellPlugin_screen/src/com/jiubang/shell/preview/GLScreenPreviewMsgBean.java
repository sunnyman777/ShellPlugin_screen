package com.jiubang.shell.preview;

import java.util.ArrayList;

import com.go.gl.view.GLView;


/**
 * 初始化屏幕预览时，屏幕层需传送的消息结构
 * 
 */
public class GLScreenPreviewMsgBean {
	public int mainScreenId = -1;
	public int currentScreenId = -1;

	/***
	 * 
	 * <br>类描述:卡片信息
	 * <br>功能详细描述:
	 * 
	 */
	public static class PreviewImg {
		public GLView previewView; // 各个屏幕预览图
		public int screenId; // 屏幕ID
		public boolean canDelete = false; // 是否可被删除
	}

	public ArrayList<PreviewImg> screenPreviewList = new ArrayList<PreviewImg>();
}
