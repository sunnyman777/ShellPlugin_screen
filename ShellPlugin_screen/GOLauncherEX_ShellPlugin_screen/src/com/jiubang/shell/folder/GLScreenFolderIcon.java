package com.jiubang.shell.folder;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewDebug.ExportedProperty;

import com.gau.golauncherex.plugin.shell.R;
import com.go.commomidentify.IGoLauncherClassName;
import com.go.gl.animation.InterpolatorValueAnimation;
import com.go.gl.graphics.GLCanvas;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.GoLauncherLogicProxy;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.info.AppItemInfo;
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
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.model.IModelState;
import com.jiubang.shell.screen.GLCellLayout;
import com.jiubang.shell.screen.component.GLScreenShortCutIcon;
import com.jiubang.shell.utils.IconUtils;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-2-21]
 */
public class GLScreenFolderIcon extends BaseFolderIcon<UserFolderInfo> {

	public final static int INNER_ICON_SIZE = 4;
	private final static long BACK_DURATION = 300;
	private final static int STATE_NORMAL = 0;
	private final static int STATE_BACK_ANIMATE = 1;
	private InterpolatorValueAnimation mValueAnimation;
	private Averages mAverages;
	private int mState = STATE_NORMAL;

	public GLScreenFolderIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
//		initIconFromSetting(true);
		setTextViewBg(GoLauncherLogicProxy.getIsShowAppTitleBg());
		if (getGLParent() instanceof GLCellLayout && mTitleView != null && mTitleView.isVisible()) {
			IconUtils.sScreenIconTextHeight = mTitleView.getHeight();
		}
	}
	
	@Override
	public void refreshIcon() {
		if (mInfo != null) {
			setTitle(mInfo.mTitle);
			if (mInfo.getContents().size() >= 0) {
				mFolderContent = mInfo.getContents();
				createFolderThumbnail(mFolderContent, -1);
				super.refreshIcon();
			}
			checkSingleIconNormalStatus();
		}
	}

	//	@Override
	//	public void setInfo(UserFolderInfo info) {
	//		super.setInfo(info);
	//	}

	@Override
	@ExportedProperty
	public Object getTag() {
		if (mInfo != null) {
			return mInfo;
		}
		return super.getTag();
	}

//	@Override
//	protected void addFolderThumbnail(List<?> content, int skipIndex) {
//		ArrayList<ItemInfo> appItemInfos = (ArrayList<ItemInfo>) content;
//		ArrayList<Bitmap> iconBitmaps = new ArrayList<Bitmap>(
//				MAX_ICON_COUNT);
//		final int count = Math.min(MAX_ICON_COUNT, appItemInfos.size());
//		for (int i = 0; i < count; i++) {
//			ItemInfo appItemInfo = appItemInfos.get(i);
//			ShortCutInfo cutInfo = (ShortCutInfo) appItemInfo;
//			cutInfo.registerObserver(this);
//			Bitmap icon = ((BitmapDrawable)cutInfo.mIcon).getBitmap();
//			if (skipIndex == i) {
//				iconView.setVisible(false);
//			} else {
//				iconView.setVisible(true);
//			}
//			iconBitmaps.add(icon);
//		}
//		int iconSize = getIconSize();
//		addIconView(iconBitmaps,iconSize);
//	}

	private IconView<ShortCutInfo> createScreenIcon(ShortCutInfo info) {
		IconView iconView = mItemView.getElement(info);
		if (iconView != null) {
			iconView.refreshIcon();
			return iconView;
		}
		GLScreenShortCutIcon screenShortCutIcon = (GLScreenShortCutIcon) mGlInflater.inflate(
				R.layout.gl_screen_shortcut_icon, null);
		int iconSize = screenShortCutIcon.getIconSize();
		final float first = iconSize * 0.12f;
		final float grap = iconSize * 0.015f;
		final int innerIconSize = (int) (iconSize - first * 2 - grap * 2) / 2;
		screenShortCutIcon.setIconSize(innerIconSize);
		screenShortCutIcon.setInfo(info);
		screenShortCutIcon.setEnableAppName(false);
		mTitleView.setTextSize(GoLauncherLogicProxy.getAppFontSize());
		return screenShortCutIcon;
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
//		if (mAutoFit) {
//			canvas.clipRect(0, 0, mWidth, mHeight);
//		}
		
		if (mState == STATE_NORMAL) {
			super.dispatchDraw(canvas);
		} else if (mState == STATE_BACK_ANIMATE) {
			if (mAverages != null && mValueAnimation != null && mTFInfo != null) {
				if (mValueAnimation.animate()) {
					//    				final int saveCount = canvas.save();
					final float value = mValueAnimation.getValue();
					final float scaleX = mAverages.mStartSx + mAverages.mAScaleX * value;
					final float scaleY = mAverages.mStartSy + mAverages.mAScaleY * value;
					final float translateX = mAverages.mStartTx + mAverages.mATransX * value;
					final float translateY = mAverages.mStartTy + mAverages.mATransY * value;
					setScaleXY(scaleX, scaleY);
					setTranslateXY(translateX, translateY);
					//    				canvas.scale(scaleX, scaleY, mTFInfo.mPivotX, mTFInfo.mPivotY);
					//    				canvas.translate(translateX, translateY);
					super.dispatchDraw(canvas);
					//    	    		canvas.restoreToCount(saveCount);
					invalidate();
				} else {
					super.dispatchDraw(canvas);
					mState = STATE_NORMAL;
					mAverages = null;
					mValueAnimation = null;
					mTFInfo = null;
				}
			}
		}
	}

	@Override
	public void animateToSolution() {
		if (mTFInfo != null) {
			if (mValueAnimation == null) {
				mValueAnimation = new InterpolatorValueAnimation(0);
			}
			mAverages = new Averages();
			mAverages.mStartSx = mTFInfo.mScaleX;
			mAverages.mStartSy = mTFInfo.mScaleY;
			mAverages.mStartTx = mTFInfo.mTranslationX;
			mAverages.mStartTy = mTFInfo.mTranslationY;
			mAverages.mATransX = -mTFInfo.mTranslationX;
			mAverages.mATransY = -mTFInfo.mTranslationY;
			mAverages.mAScaleX = 1.0f - mTFInfo.mScaleX;
			mAverages.mAScaleY = 1.0f - mTFInfo.mScaleY;

			mValueAnimation.start(1.0f, BACK_DURATION);
			mValueAnimation.animate();
			mState = STATE_BACK_ANIMATE;
			invalidate();
		}
	} // end animateToSolution

	/**
	 * 平均变化值
	 * @author jiangxuwen
	 *
	 */
	class Averages {

		float mStartTx;
		float mStartTy;

		float mATransX;
		float mATransY;

		float mStartSx;
		float mStartSy;

		float mAScaleX;
		float mAScaleY;
	}

	@Override
	public void closeFolder(boolean animate, Object...objs) {
		if (GLAppFolder.getInstance().isFolderOpened()) {
			ShellAdmin.sShellManager.getShell().showStage(IShell.STAGE_SCREEN, animate, objs);
		}
	}

	/**
	 * 获得文件夹发开始的开口图片 获得文件夹最上面的罩子图片
	 * 
	 * @param icons
	 */
	public void useFolderFeatureIcon() {
		if (mInfo == null) {
			return;
		}
		Drawable openIcon = null;
		Drawable closeIcon = null;
		// 文件夹样式
		FolderStyle folderStyle = null;
		DeskThemeControler themeControler = null;

		// 获取图标类型
		int type = ((UserFolderInfo) mInfo).getmFeatureIconType();
		// 图标的主题包
		String packageName = ((UserFolderInfo) mInfo).getmFeatureIconPackage();
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
		//		BitmapGLDrawable bg = new BitmapGLDrawable((BitmapDrawable) mInfo.getFeatureIcon());
		//		BitmapGLDrawable open = new BitmapGLDrawable((BitmapDrawable) openIcon);
		//		BitmapGLDrawable close = new BitmapGLDrawable((BitmapDrawable) closeIcon);
		boolean showThumbnail = type == ImagePreviewResultType.TYPE_PACKAGE_RESOURCE ? true : false;
		setThumbnailVisible(showThumbnail);
		setResource(mInfo.getFeatureIcon(), closeIcon, openIcon);
	}

	@Override
	public void checkSingleIconNormalStatus() {
		if (mInfo != null) {
			if (mInfo.mTotleUnreadCount > 0) { // 通讯统计的未读数字
				mMultiView.setCurrenState(IModelState.STATE_COUNT, mInfo.mTotleUnreadCount);
//				mMultiView.setOnSelectClickListener(null);
			} else { // 没有任何状态
				super.checkSingleIconNormalStatus();
			}
		}
	}

	@Override
	public void setInfo(UserFolderInfo info) {
		// TODO Auto-generated method stub
		if (null != mInfo) {
			mInfo.unRegisterObserver(this);
		}
		super.setInfo(info);
		if (mInfo != null) {
			mInfo.registerObserver(this);
		}
	}
	
	@Override
	public void onBCChange(int msgId, int param, Object ...object) {
		// TODO Auto-generated method stub
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
				post(new Runnable() {
					
					@Override
					public void run() {
						refreshIcon();
						
					}
				});
//				mItemView.onIconRefresh();
				break;
			default :
				break;
		}

	}
	@Override
	protected void addIconBitmap(int index) {
		if (index >= mFolderContent.size()) {
			return;
		}
		ItemInfo appItemInfo = (ItemInfo) mFolderContent.get(index);
		ShortCutInfo cutInfo = (ShortCutInfo) appItemInfo;
		cutInfo.registerObserver(this);
		Bitmap icon = ((BitmapDrawable) cutInfo.mIcon).getBitmap();
		mIconBitmaps.add(icon);
	}
	@Override
	protected int getFolderIconSize() {
		return getIconSize();
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initIconFromSetting(true);
	}
}
