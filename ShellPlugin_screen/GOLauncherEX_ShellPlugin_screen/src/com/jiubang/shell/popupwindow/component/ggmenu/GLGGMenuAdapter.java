package com.jiubang.shell.popupwindow.component.ggmenu;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLBaseAdapter;
import com.go.proxy.ApplicationProxy;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.MenuBean;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 菜单项的适配器
 * 
 * @author ouyongqiang
 * 
 */
public class GLGGMenuAdapter extends GLBaseAdapter implements ICleanable {
	/**
	 * 程序上下文
	 */
	private Context mContext;

	private MenuBean mMenuBean;
	
	private ArrayList<GLGGMenuIcon> mIconList = null;
	
	GLLayoutInflater mInflater = ShellAdmin.sShellManager.getLayoutInflater();
	
	private int mIconSize = 0;
	
	public GLGGMenuAdapter(Context context, int[] textArray, Drawable[] imgArray,
			int[] menuItemIds) throws IllegalArgumentException {
		super();
		mContext = context;
		// 菜单图标继承ICONVIEW，修改图标大小时，会影响菜单图标的大小
		// 为了保持菜单图标的大小不变，给图标大小设置一个固定的值
		mIconSize = (int) mContext.getResources().getDimension(R.dimen.screen_icon_large_size);
		if (textArray.length != imgArray.length) {
			throw new IllegalArgumentException("textArray和imgResArray长度不一致");
		}
		mIconList = new ArrayList<GLGGMenuIcon>();
		int[] ignores = ignoreArray(menuItemIds);
		for (int i = 0; i < textArray.length; i++) {
			if (isIgnore(i, ignores)) {
				continue;
			}
			GLGGMenuItemInfo itemInfo = new GLGGMenuItemInfo();
			itemInfo.setIcon(imgArray[i]);
			itemInfo.setId(menuItemIds[i]);
			itemInfo.setTitle(mContext.getString(textArray[i]));
			GLGGMenuIcon icon = (GLGGMenuIcon) mInflater.inflate(R.layout.gl_menu_item, null, false);
			icon.setIconSize(mIconSize);
			icon.setInfo(itemInfo);
			mIconList.add(icon);
		}
	}
	
	public void checkItemState() {
		if (mIconList != null) {
			int count = mIconList.size();
			for (int i = 0; i < count; i++) {
				GLGGMenuIcon icon = (GLGGMenuIcon) mIconList.get(i);
				icon.checkSingleIconNormalStatus();
			}
		}
		this.notifyDataSetChanged();
	}

	private int[] ignoreArray(int[] ids) {
		if (GoAppUtils.isGoLockerExist(ApplicationProxy.getContext())) {
			return null;
		}

		int count = 0;
		// for (int i = 0; i < ids.length; i++)
		// {
		// if (GGMenuData.GGMENU_ID_LOCKER == ids[i])
		// {
		// count++;
		// }
		// }

		if (0 == count) {
			return null;
		}

		int[] ret = new int[count];
		// for (int i = 0, j = 0; i < ids.length; i++)
		// {
		// if (GGMenuData.GGMENU_ID_LOCKER == ids[i])
		// {
		// ret[j] = i;
		// j++;
		// }
		// }
		return ret;
	}

	private boolean isIgnore(int index, int[] ignores) {
		if (null != ignores) {
			int len = ignores.length;
			for (int i = 0; i < len; i++) {
				if (index == ignores[i]) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int getCount() {
		return (null != mIconList) ? mIconList.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return (null != mIconList) ? mIconList.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		if (mIconList != null) {
			GLGGMenuIcon iconview = mIconList.get(position);
			if (iconview != null) {
				GLGGMenuItemInfo itemInfo = iconview.getInfo();
				if (itemInfo != null) {
					return itemInfo.getId();
				}
			}
		}
		return -1;
	}

	/**
	 * 插入菜单项到指定位置
	 * 
	 * @param text
	 *            菜单项的显示文本Id
	 * @param imgRes
	 *            菜单项的图片资源Id
	 * @param id
	 *            菜单项的Id
	 * @param menuItemLayout
	 *            菜单项的布局文件ID
	 * @param index
	 *            插入的位置，如果是菜单项的大小则插入到最后的位置
	 */
	public void addMenuItem(int text, Drawable imgRes, int id, int menuItemLayout, int index) {
		String tmpText = mContext.getString(text);
		GLGGMenuItemInfo itemInfo = new GLGGMenuItemInfo();
		itemInfo.setTitle(tmpText);
		itemInfo.setIcon(imgRes);
		itemInfo.setId(id);

		GLGGMenuIcon icon = (GLGGMenuIcon) mInflater.inflate(R.layout.gl_menu_item, null, false);
		icon.setInfo(itemInfo);
		mIconList.add(index, icon);
	}

	/**
	 * 插入菜单项到指定位置
	 * 
	 * @param text
	 *            菜单项的显示文本
	 * @param imgRes
	 *            菜单项的图片资源Id
	 * @param id
	 *            菜单项的Id
	 * @param menuItemLayout
	 *            菜单项的布局文件ID
	 * @param index
	 *            插入的位置，如果是菜单项的大小则插入到最后的位置
	 */
	public void addMenuItem(String text, Drawable imgRes, int id, int menuItemLayout, int index) {
		GLGGMenuItemInfo itemInfo = new GLGGMenuItemInfo();
		itemInfo.setTitle(text);
		itemInfo.setIcon(imgRes);
		itemInfo.setId(id);

		GLGGMenuIcon icon = (GLGGMenuIcon) mInflater.inflate(R.layout.gl_menu_item, null, false);
		icon.setInfo(itemInfo);
		mIconList.add(index, icon);
	}

	/**
	 * 删除菜单项
	 * 
	 * @param id
	 *            菜单项的Id
	 */
	public boolean removeMenuItem(int id) {
		int index = -1;
		for (int i = 0; i < mIconList.size(); i++) {
			if (mIconList.get(i).getInfo().getId() == id) {
				index = i;
				break;
			}
		}
		if (index != -1) {
			mIconList.remove(index);
		}
		return true;
	}

	@Override
	public GLView getView(int position, GLView convertView, GLViewGroup parent) {
		if (null == mIconList) {
			return convertView;
		}
		GLGGMenuIcon icon = mIconList.get(position);
		return icon;
//		if (null == mGGMenuItems) {
//			// 菜单已关闭，已释放
//			return convertView;
//		}
//		GLGGMenuItem item = mGGMenuItems.get(position);
//		if (item != null) {
//			if (0 == GLGGMenu.sTextColor) {
//				item.bind(mTextArray.get(position), mImgArray.get(position));
//			} else {
//				item.bind(mTextArray.get(position), GLGGMenu.sTextColor, mImgArray.get(position));
//			}
//		}
//
//		int itemId = mMenuItemIds.get(position);
//		int highTextColor = 0xff7ca500;
//		if (mMenuBean != null) {
//			highTextColor = mMenuBean.mHighLightTextColor;
//		}
//		int type = GGMenuControler.getInstance().checkMenuItemStatus(itemId);
//		switch (type) {
//			case GGMenuControler.GGMENU_ITEM_STATUS_SHOW_HIGHT_LIGHT :
//				item.bind(mTextArray.get(position), highTextColor, mImgArray.get(position));
//				break;
//			case GGMenuControler.GGMENU_ITEM_STATUS_SHOW_COUNT :
//				item.generatorMessageCountImage();
//				break;
//			case GGMenuControler.GGMENU_ITEM_STATUS_SHOW_NEW_LOGO :
//				item.addNewThemeLogo();
//				break;
//			case GGMenuControler.GGMENU_ITEM_STATUS_UNSHOW_NEW_LOGO :
//				item.removeNewThemeLogo();
//				break;
//			default :
//				break;
//		}
//		return item;
	}

	/**
	 * 更新其中一项
	 * 
	 * @param index
	 *            更改项的索引
	 * @param drawable
	 * @param name
	 */
//	public void updateItem(int index, Drawable drawable, String name) {
//		if (null == drawable || null == name || index >= getCount()) {
//			return;
//		}
//
//		mTextArray.remove(index);
//		mTextArray.add(index, name);
//
//		mImgArray.remove(index);
//		mImgArray.add(index, drawable);
//
//		notifyDataSetChanged();
//	}

	/**
	 * 更新其中一项
	 * 
	 * @param index
	 *            更改项的索引
	 * @param drawable
	 * @param name
	 */
//	public boolean updateItem(int oldid, int newid, Drawable drawable, String name) {
//		boolean ret = false;
//		if (null == drawable || null == name) {
//			return false;
//		}
//
//		int count = mMenuItemIds.size();
//		for (int i = 0; i < count; i++) {
//			int id = mMenuItemIds.get(i);
//			if (id == oldid) {
//				mMenuItemIds.remove(i);
//				mMenuItemIds.add(i, newid);
//
//				mImgArray.remove(i);
//				mImgArray.add(i, drawable);
//
//				mTextArray.remove(i);
//				mTextArray.add(i, name);
//				ret = true;
//				break;
//			}
//		}
//		notifyDataSetChanged();
//		return ret;
//	}

	@Override
	public void cleanup() {
		if (mIconList != null) {
			for (GLGGMenuIcon icon : mIconList) {
				if (icon != null) {
					icon.cleanup();
				}
			}
			mIconList.clear();
			mIconList = null;
		}
	}
}
