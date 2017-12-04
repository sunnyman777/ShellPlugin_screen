package com.jiubang.shell.model;

import com.gau.golauncherex.plugin.shell.R;





/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  chaoziliang
 * @date  [2012-9-4]
 */
public interface IModelItemType {

	/***************** 无效的模型类型*****************************/
	public static final int INVALID_MODEL = 0;

	/***************** 普通模型*****************************/
	public static final int GENERAL_ICON = 1;
	public static final String GENERAL_FILE = "general.ms3d";
	public static final int[] GENERAL_TEXTURE = { 0 };

	/***************** 文件夹模型*****************************/
	public static final int FOLDER_ICON = 2;
	public static final String FOLDER_FILE = "folder.ms3d";
	public static final int[] FOLDER_TEXTURE = { R.drawable.gl_default_folder_bg};

	/***************** 联系人模型*****************************/
	public static final int CONTACT_ICON = 3;
	public static final String CONTACT_FILE = "contact.ms3d";
	public static final int[] CONTACT_TEXTURE = { 1 };

	/***************** 电话模型*****************************/
	public static final int DIAL_ICON = 4;
	public static final String DIAL_FILE = "dial.ms3d";
	public static final int[] DIAL_TEXTURE = { 0 };

	/***************** 短信模型*****************************/
	public static final int MESSAGE_ICON = 5;
	public static final String MESSAGE_FILE = "message.ms3d";
	public static final int[] MESSAGE_TEXTURE = { 0};

	/***************** 功能表模型*****************************/
	public static final int APPDRAW_ICON = 6;
	public static final String APPDRAW_FILE = "appdraw.ms3d";
	public static final int[] APPDRAW_TEXTURE = { 0 };

	/***************** 浏览器模型*****************************/
	public static final int BROWSER_ICON = 7;
	public static final String BROWSER_FILE = "browser.ms3d";
	public static final int[] BROWSER_TEXTURE = { 0 };

	/***************** 特效1模型*****************************/
	public static final int EFFECT_1_ICON = 8;
	public static final String EFFECT_1_FILE = "effect_1.ms3d";
	public static final int[] EFFECT_1_TEXTURE = { 0,
			1};

	/***************** 特效2模型*****************************/
	public static final int EFFECT_2_ICON = 9;
	public static final String EFFECT_2_FILE = "effect_2.ms3d";
	public static final int[] EFFECT_2_TEXTURE = { 0,
			1 };

	/***************** 特效3模型*****************************/
	public static final int EFFECT_3_ICON = 10;
	public static final String EFFECT_3_FILE = "effect_3.ms3d";
	public static final int[] EFFECT_3_TEXTURE = { 0,
			1 };

}
