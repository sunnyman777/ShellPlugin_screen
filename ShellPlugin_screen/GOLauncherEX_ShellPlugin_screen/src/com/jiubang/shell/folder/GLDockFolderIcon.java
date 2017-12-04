package com.jiubang.shell.folder;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;

import com.go.commomidentify.IGoLauncherClassName;
import com.go.proxy.ApplicationProxy;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeConfig;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.XmlParserFactory;
import com.jiubang.ggheart.data.theme.bean.DeskFolderThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.FolderStyle;
import com.jiubang.ggheart.data.theme.parser.DeskFolderThemeParser;
import com.jiubang.ggheart.data.theme.parser.DodolThemeResourceParser;
import com.jiubang.ggheart.data.theme.parser.IParser;
import com.jiubang.ggheart.folder.FolderConstant;
import com.jiubang.shell.IShell;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.model.IModelState;

/**
 * 
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author dingzijian
 * @date [2013-2-21]
 */
public class GLDockFolderIcon extends BaseFolderIcon<DockItemInfo> {
	private UserFolderInfo mUserFolderInfo;
	private static final int CHANGE_ICON = 1;

	public GLDockFolderIcon(Context context, AttributeSet attrs) {
		super(context, attrs);

	}
	
	@Override
	public void refreshIcon() {
		if (mUserFolderInfo != null) {
			setTitle(mUserFolderInfo.mTitle);
			if (!mUserFolderInfo.getContents().isEmpty()) {
				mFolderContent = mUserFolderInfo.getContents();
				createFolderThumbnail(mFolderContent, -1);
				super.refreshIcon();
			}
		}
		checkSingleIconNormalStatus();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setGravity(Gravity.CENTER);
	}

	@Override
	public void setInfo(DockItemInfo info) {
		if (null != mInfo) {
			mInfo.unRegisterObserver(this);
		}
		if (info != null) {
			mUserFolderInfo = (UserFolderInfo) info.mItemInfo;
			super.setInfo(info);
			mInfo.registerObserver(this);
		}
	}

	@Override
	public void closeFolder(boolean animate, Object... objs) {
		ShellAdmin.sShellManager.getShell().showStage(IShell.STAGE_SCREEN, animate, objs);
	}

	
	@Override
	public void requestLayout() {
		// TODO Auto-generated method stub
		super.requestLayout();
	}
	public static final int CHANGE_ICON_STRING = 1;

	@Override
	public synchronized void cleanup() {
		if (mInfo != null) {
			mInfo.unRegisterObserver(this);
		}
		super.cleanup();
	}

	@Override
	public void onBCChange(int msgId, int param, Object ...object) {
		switch (msgId) {
			case AppItemInfo.UNREADCHANGE :
				post(new Runnable() {

					@Override
					public void run() {
						checkSingleIconNormalStatus();
					}
				});
				break;
			case AppItemInfo.INCONCHANGE :
				Message message = new Message();
				message.what = CHANGE_ICON_STRING;
				// 必须放在msg queue首，不然会出现拖动后4个变5个，闪一下大图标才显示小图标
				mHandler.sendMessageAtFrontOfQueue(message);
				break;
			case DockItemInfo.ICONCHANGED :
				Message msg = new Message();
				msg.what = CHANGE_ICON_STRING;
				// 必须放在msg queue首，不然会出现拖动后4个变5个，闪一下大图标才显示小图标
				mHandler.sendMessageAtFrontOfQueue(msg);
				break;
		}
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case CHANGE_ICON_STRING :
					if (null == mInfo) {
						return;
					}

					//					if (mInfo.mItemInfo instanceof UserFolderInfo) {
					//						UserFolderInfo folder = (UserFolderInfo) mInfo.mItemInfo;
					//						// setmIsNotifyShow(true);
					//						// setmNotifyCount(folder.mTotleUnreadCount);
					//					}
					if (mInfo.isCustomStyle()) {
						useFolderFeatureIcon();
						refreshIcon();
					} else {
						resetResource();
						refreshIcon();
					}
					break;

				default :
					break;
			}
		};
	};

	/**
	 * 获得文件夹发开始的开口图片 获得文件夹最上面的罩子图片
	 * 
	 * @param icons
	 */
	public void useFolderFeatureIcon() {
		Drawable openIcon = null;
		Drawable closeIcon = null;
		// 文件夹样式
		FolderStyle folderStyle = null;
		DeskThemeControler themeControler = null;

		// 获取图标类型
		int type = mUserFolderInfo.getmFeatureIconType();
		// 图标的主题包
		String packageName = mUserFolderInfo.getmFeatureIconPackage();
		// 判断改主题是否有安装
		boolean isInstall = GoAppUtils.isAppExist(ApplicationProxy.getContext(), packageName);
		ImageExplorer imageExplorer = ImageExplorer.getInstance(ApplicationProxy.getContext());
		// GO主题类型
		if ((type == ImagePreviewResultType.TYPE_PACKAGE_RESOURCE || type == ImagePreviewResultType.TYPE_APP_ICON)
				&& isInstall) {
			// 如果使用的是默认主题的GO样式
			if (packageName.equals(IGoLauncherClassName.DEFAULT_THEME_PACKAGE)) {
				openIcon = imageExplorer.getDrawable(packageName, FolderConstant.DEFAULT_OPEN_RES);
				closeIcon = imageExplorer.getDrawable(packageName, FolderConstant.DEFAULT_CLOSE_RES);
			} else {
				InputStream inputStream = null;
				XmlPullParser xmlPullParser = null;
				DeskFolderThemeBean themeBean = null;
				String fileName = ThemeConfig.DESKTHEMEFILENAME;
				boolean isAtomTheme = ThemeManager.getInstance(ApplicationProxy.getContext())
						.isAtomTheme(packageName);
				if (isAtomTheme) { //如果是atom主题，不用解析
					return;
				}
				boolean isDodolTheme = ThemeManager.getInstance(ApplicationProxy.getContext())
						.isDodolTheme(packageName);
				if (isDodolTheme) {
					fileName = ThemeConfig.DODOLTHEMERESOURCE;
				}

				// 解析桌面中相关主题信息
				inputStream = ThemeManager.getInstance(ApplicationProxy.getApplication())
						.createParserInputStream(packageName, fileName);
				if (inputStream != null) {
					xmlPullParser = XmlParserFactory.createXmlParser(inputStream);
				} else {
					xmlPullParser = XmlParserFactory.createXmlParser(
							ApplicationProxy.getApplication(), fileName, packageName);
				}
				if (xmlPullParser != null) {
					themeBean = new DeskFolderThemeBean(packageName);
					if (isDodolTheme) {
						DodolThemeResourceParser parser = new DodolThemeResourceParser();
						parser.parseDeskFolderTheme(xmlPullParser, themeBean);
						parser = null;
					} else {
						IParser parser = new DeskFolderThemeParser();
						parser.parseXml(xmlPullParser, themeBean);
						parser = null;
					}

					if (themeBean != null && themeBean.mFolderStyle != null) {
						if (themeBean.mFolderStyle.mOpendFolder != null) {
							openIcon = imageExplorer.getDrawable(packageName,
									themeBean.mFolderStyle.mOpendFolder.mResName);
							if (null == openIcon) {
								openIcon = imageExplorer.getDrawable(
										IGoLauncherClassName.DEFAULT_THEME_PACKAGE,
										FolderConstant.DEFAULT_OPEN_RES);
							}
						}
						if (themeBean.mFolderStyle.mClosedFolder != null) {
							closeIcon = imageExplorer.getDrawable(packageName,
									themeBean.mFolderStyle.mClosedFolder.mResName);
							if (null == closeIcon) {
								closeIcon = imageExplorer.getDrawable(
										IGoLauncherClassName.DEFAULT_THEME_PACKAGE,
										FolderConstant.DEFAULT_CLOSE_RES);
							}
						}
					}
				}
				// 关闭inputStream
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} else {
			themeControler = AppCore.getInstance().getDeskThemeControler();
			if (themeControler != null /* && themeControler.isUesdTheme() */) {
				DeskThemeBean themeBean = themeControler.getDeskThemeBean();
				if (themeBean != null && themeBean.mScreen != null) {
					folderStyle = themeBean.mScreen.mFolderStyle;
				}
			}
			if (folderStyle != null && folderStyle.mOpendFolder != null) {
				Drawable tempDrawable = imageExplorer.getDrawable(folderStyle.mPackageName,
						folderStyle.mOpendFolder.mResName);
				if (tempDrawable != null && tempDrawable instanceof BitmapDrawable) {
					openIcon = tempDrawable;
				}
			}
			if (openIcon == null) {
				openIcon = mFolderOpenCover;
			}

			if (folderStyle != null && folderStyle.mClosedFolder != null
					&& type != ImagePreviewResultType.TYPE_IMAGE_FILE) {
				Drawable tempDrawable = imageExplorer.getDrawable(folderStyle.mPackageName,
						folderStyle.mClosedFolder.mResName);
				if (tempDrawable != null && tempDrawable instanceof BitmapDrawable) {
					closeIcon = tempDrawable;
				}
			}
			if (closeIcon == null && type != ImagePreviewResultType.TYPE_IMAGE_FILE) {
				closeIcon = mFolderCloseCover;
			}
		}
		boolean showThumbnail = type == ImagePreviewResultType.TYPE_PACKAGE_RESOURCE ? true : false;
		setThumbnailVisible(showThumbnail);
		setResource(mUserFolderInfo.getFeatureIcon(), closeIcon, openIcon);
	}

	public UserFolderInfo getUserFolderInfo() {
		return mUserFolderInfo;
	}
	@Override
	public void checkSingleIconNormalStatus() {
		if (mUserFolderInfo != null) {
			if (mUserFolderInfo.mTotleUnreadCount > 0) { // 通讯统计的未读数字
				mMultiView.setCurrenState(IModelState.STATE_COUNT,
						mUserFolderInfo.mTotleUnreadCount);
				//				mMultiView.setOnSelectClickListener(null);
			} else { // 没有任何状态
				super.checkSingleIconNormalStatus();
			}
		}
	}

	@Override
	protected void addIconBitmap(int index) {
		ItemInfo appItemInfo = (ItemInfo) mFolderContent.get(index);
		ShortCutInfo cutInfo = (ShortCutInfo) appItemInfo;
		cutInfo.registerObserver(this);
		Bitmap icon = ((BitmapDrawable) cutInfo.mIcon).getBitmap();
		mIconBitmaps.add(icon);
	}

	@Override
	protected int getFolderIconSize() {
		if (mInfo != null) {
			return mInfo.getBmpSize();
		}
		return 0;
	}
	
	@Override
	public void getHitRect(Rect outRect) {
		outRect.set(mLeft, mTop, mRight, mBottom);
	}
	
}
