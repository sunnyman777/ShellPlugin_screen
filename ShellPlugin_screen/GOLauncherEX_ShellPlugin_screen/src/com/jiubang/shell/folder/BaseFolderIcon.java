package com.jiubang.shell.folder;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLImageView;
import com.go.gl.widget.GLTextViewWrapper;
import com.go.proxy.ApplicationProxy;
import com.go.util.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.apps.desks.diy.pref.PrefConst;
import com.jiubang.ggheart.apps.desks.diy.pref.PrivatePreference;
import com.jiubang.ggheart.folder.FolderConstant;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderInfo;
import com.jiubang.shell.IShell;
import com.jiubang.shell.common.component.GLModel3DMultiView;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.folder.FolderElementLayout.EditAnimationListener;
import com.jiubang.shell.folder.status.FolderStatusManager;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.model.CommonImageManager;
import com.jiubang.shell.model.IModelState;

/**
 * 合并文件夹容器 ,处理图标与图标的文件夹合并、多选聚合合并
 * 
 * @author dingzijian
 * @param <T>
 * @param <T>
 */
public abstract class BaseFolderIcon<T> extends IconView<T>
		implements
			BroadCasterObserver,
			BatchAnimationObserver,
			EditAnimationListener {
	protected GLModel3DMultiView mMultiView;
	protected GLModelFolder3DView mItemView;
	protected GLTextViewWrapper mTitleView;
	protected Drawable mFolderBg;
	protected Drawable mFolderCloseCover;
	protected Drawable mFolderOpenCover;
	protected GLLayoutInflater mGlInflater;
	private GLAppFolderInfo mFolderInfo;
	private boolean mCustomStyle;
	public final static int JUMP_IN_FOLDER_TASK = 3;
	//	private AnimationTask mZoomTask;
	protected ArrayList<?> mFolderContent;
	protected boolean mRecyled;
	protected boolean mShowThunbnail = true;
	/**
	 * 文件夹里面缩略图的个数
	 */
	protected ArrayList<Bitmap> mIconBitmaps;
//	protected boolean mRefreshForAddIcon;
	public BaseFolderIcon(Context context) {
		this(context, null);
	}

	public BaseFolderIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		//		CommonImageManager.getInstance().registerObserver(this);
	}
	@Override
	public void refreshIcon() {

	}
	protected void init() {
		mGlInflater = ShellAdmin.sShellManager.getLayoutInflater();
		mIconBitmaps = new ArrayList<Bitmap>();
	}
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mMultiView = (GLModel3DMultiView) findViewById(R.id.multmodel);
		mItemView = (GLModelFolder3DView) mMultiView.findViewById(R.id.model);
		mItemView.setEditAnimationListener(this);
		mTitleView = (GLTextViewWrapper) findViewById(R.id.app_name);
		loadResource();
		setIcon(null);
	}

	public void setTitle(CharSequence title) {
		if (mTitleView != null) {
			mTitleView.setText(title);
		}
	}
	/**
	 * 文件夹icon，参数直接给null，icon只能使用当前主题的bg与mask
	 */
	@Override
	public void setIcon(BitmapDrawable drawable) {
		//				mItemView.changeTexture(mFolderBg.getBitmap());
		mMultiView.setBgImageDrawable(mFolderBg);
		mMultiView.setBgVisible(true);
		if (mShowThunbnail) {
			mMultiView.setCoverImageDrawable(mFolderCloseCover);
			mMultiView.setCoverVisible(true);
		}
		//				setCoverDrawable(mFolderCloseCover);
	}

	public <k> void addIconView(List<Bitmap> iconBitmaps, int iconSize) {
		mItemView.addIconView(iconBitmaps, iconSize);
	}

	protected void createFolderThumbnail(List<?> folderContent, int skipIndex) {
		addIconBitmaps();
		int contentSize = mFolderContent.size();
		if (contentSize > FolderConstant.MAX_ICON_COUNT + 1) {
			addIconBitmap(contentSize - 2);
		}
		if (contentSize > FolderConstant.MAX_ICON_COUNT) {
			addIconBitmap(contentSize - 1);
		}
		addIconView(mIconBitmaps, getFolderIconSize());
	}
	
	private void addIconBitmaps() {
		int contentSize = mFolderContent.size();
		int count = Math.min(FolderConstant.MAX_ICON_COUNT, contentSize);
		mIconBitmaps.clear();
		for (int i = 0; i < count; i++) {
			addIconBitmap(i);
		}
	}
	public GLModel3DMultiView getMultiView() {
		return mMultiView;
	}

	public void loadResource() {
		mCustomStyle = false;
		mShowThunbnail = true;
		mFolderBg = CommonImageManager.getInstance().getDrawable(CommonImageManager.RES_FOLDER_BG);
		mFolderCloseCover = CommonImageManager.getInstance().getDrawable(
				CommonImageManager.RES_FOLDER_CLOSE_COVER);
		mFolderOpenCover = CommonImageManager.getInstance().getDrawable(
				CommonImageManager.RES_FOLDER_OPEN_COVER);
	}

	public void setResource(Drawable folderBg, Drawable folderCloseCover,
			Drawable folderOpenCover) {
		mCustomStyle = true;
		mFolderBg = folderBg;
		mFolderCloseCover = folderCloseCover;
		mFolderOpenCover = folderOpenCover;
		mMultiView.setBgImageDrawable(mFolderBg);
		mMultiView.setBgVisible(true);
		if (mShowThunbnail) {
			mMultiView.setCoverImageDrawable(mFolderCloseCover);
			mMultiView.setCoverVisible(true);
		}
	}

	@Override
	public synchronized void cleanup() {
		super.cleanup();
		onIconRemoved();
		mRecyled = true;
	}

	@Override
	public void onIconRemoved() {
		//		CommonImageManager.getInstance().unRegisterObserver(this);
		GLAppFolder.getInstance().removeFolderIcon(mFolderInfo);
	}

	public IconView getElement(Object info) {
		return mItemView.getElement(info);
	}

	/**
	 * 创建文件夹的动画
	 * @param dragViewCenterX
	 * @param dragViewCenterY
	 */
	public void createFolderAnimation(int dragViewCenterX, int dragViewCenterY) {
		createFolderAnimation(dragViewCenterX, dragViewCenterY, null);
	}

	/**
	 * 创建文件夹的动画
	 * @param dragViewCenterX
	 * @param dragViewCenterY
	 */
	public void createFolderAnimation(int dragViewCenterX, int dragViewCenterY,
			AnimationListener listener) {
		AnimationTask zoomTask = new AnimationTask(false, AnimationTask.PARALLEL);
		zoomTask.setBatchAnimationObserver(this, JUMP_IN_FOLDER_TASK);
		float[] centerXY = new float[2];
		centerXY[0] = mMultiView.getWidth() / 2;
		centerXY[1] = mMultiView.getHeight() / 2;
		GLImageView icon = mItemView.getElement(0);
		addIconAnimation(0, centerXY, 1, icon, listener, zoomTask);
		if (mFolderContent.size() > 1) {
			float[] dragCenter = new float[2];
			float[] iconRelView = new float[2];
			int[] location = new int[2];
			iconRelView[0] = getWidth() / 2 - mMultiView.getWidth() / 2;
			iconRelView[1] = getHeight() / 2 - mMultiView.getHeight() / 2;
			ShellAdmin.sShellManager.getShell().getContainer().getLocation(mMultiView, location);
			dragCenter[0] = dragViewCenterX - location[0] + mMultiView.getLeft();
			dragCenter[1] = dragViewCenterY - location[1] - iconRelView[1] + mMultiView.getTop();
			//		float scale = ShellAdmin.sShellManager.getShell().getDragLayer().getGLRootView().getProjectScale(DragController.DRAG_ICON_Z_DEPTH);
			float scale = DragController.DRAG_ICON_SCALE;
			GLImageView icon2 = mItemView.getElement(1);
			if (icon2 != null) {
				addIconAnimation(1, dragCenter, scale, icon2, null, zoomTask);
			}
		}
//		ShellContainer.setDispatchTouchEvent(false);
		GLAnimationManager.startAnimation(zoomTask);
		
//		JobManager.postJob(new Job(JUMP_IN_FOLDER_TASK, zoomTask, false));
	}

	/**
	 * 往文件夹里添加元素的动画
	 * @param dragViewCenterX 被抓起的view X中点
	 * @param dragViewCenterY 被抓起的view Y中点
	 * @param target 进入文件夹中的第几个位置
	 * @param icon 被抓起的view
	 */
	public void addInFolderAnimation(int dragViewCenterX, int dragViewCenterY, int target, AnimationListener listener) {
		AnimationTask zoomTask = new AnimationTask(false, AnimationTask.PARALLEL);
		zoomTask.setBatchAnimationObserver(this, JUMP_IN_FOLDER_TASK);
		float[] dragCenter = new float[2];
		float[] iconRelView = new float[2];
		int[] location = new int[2];
		iconRelView[0] = getWidth() / 2 - mMultiView.getWidth() / 2 - mMultiView.getPaddingLeft();
		iconRelView[1] = getHeight() / 2 - mMultiView.getHeight() / 2 - mMultiView.getPaddingTop();
		ShellAdmin.sShellManager.getShell().getContainer().getLocation(mMultiView, location);
		dragCenter[0] = dragViewCenterX - location[0] + mMultiView.getLeft();
		dragCenter[1] = dragViewCenterY - location[1] - iconRelView[1] + mMultiView.getTop();
		//		float scale = ShellAdmin.sShellManager.getShell().getDragLayer().getGLRootView()
		//				.getProjectScale(DragController.DRAG_ICON_Z_DEPTH);
		float scale = DragController.DRAG_ICON_SCALE;
		GLView icon = mItemView.getElement(target);
		icon.setVisible(true);
		addIconAnimation(target, dragCenter, scale, icon, listener, zoomTask);

		GLAnimationManager.startAnimation(zoomTask);
//		JobManager.postJob(new Job(JUMP_IN_FOLDER_TASK, zoomTask, false));
	}

	private void addIconAnimation(int target, float[] sourceCenter, float DragSourceScale,
			GLView icon, AnimationListener listener, AnimationTask zoomTask) {
		float[] scaleXY = mItemView.mIconViewLayout.getLocationCenter(target);
		AnimationSet set = new AnimationSet(true);
		//		if (target < MAX_ICON_COUNT) {
		mItemView.mIconViewLayout.setIsAnimating(true);
		float itemWidth = mItemView.getWidth();
		float itemHeight = mItemView.getHeight();
		if (itemWidth != itemHeight) {
			itemWidth = Math.max(itemWidth, itemHeight);
			itemHeight = itemWidth;
		}
		itemWidth = itemWidth * DragSourceScale;
		itemHeight = itemHeight * DragSourceScale;
		float scaleX = itemWidth / (float) icon.getWidth();
		float scaleY = itemHeight / (float) icon.getHeight();
		ScaleAnimation scale = new ScaleAnimation(scaleX, 1f, scaleY, 1f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		set.addAnimation(scale);
		TranslateAnimation translate = new TranslateAnimation(sourceCenter[0] - scaleXY[0], 0,
				sourceCenter[1] - scaleXY[1], 0);
		set.addAnimation(translate);
		//		} else {
		//			ScaleAnimation scale = new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		//			set.addAnimation(scale);
		//		}
		set.setDuration(300);
		set.setFillAfter(false);
		zoomTask.addAnimation(icon, set, listener);
	}

	public void setFolderInfo(GLAppFolderInfo mFolderInfo) {
		this.mFolderInfo = mFolderInfo;
	}

	public GLAppFolderInfo getFolderInfo() {
		return mFolderInfo;
	}

	@Override
	public void onFinish(int what, Object[] params) {
		switch (what) {
			case JUMP_IN_FOLDER_TASK :
				mItemView.mIconViewLayout.setIsAnimating(false);
				cancleFolderReady();
				break;

			default :
				break;
		}
		super.onFinish(what, params);
	}
	@Override
	public void onEditAnimationFinish() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStart(int what, Object[] params) {

	}

	public void openFolder() {
		openFolder(FolderStatusManager.GRID_NORMAL_STATUS);
	}

	public void openFolder(int status) {
		if (mFolderInfo.folderType == GLAppFolderInfo.TYPE_RECOMMAND_GAME
				&& status == FolderStatusManager.GRID_NORMAL_STATUS) {
			status = PrivatePreference.getPreference(ApplicationProxy.getContext()).getInt(
					PrefConst.KEY_GAME_FOLDER_MODE, status);
		}
		ShellAdmin.sShellManager.getShell().showStage(IShell.STAGE_APP_FOLDER, true, this, status);
	}

	public abstract void closeFolder(boolean animate, Object...objs);
	/**
	 * <br>功能简述:拖出文件夹时刷新使用
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param skipIndex
	 */
	public void refreshFolderIcon(int skipIndex) {
		if (mFolderContent == null || skipIndex < 0 || skipIndex >= mFolderContent.size()) {
			return;
		}
		createFolderThumbnail(mFolderContent, skipIndex);
		mItemView.onIconRefresh();
	}

	/**
	 * <br>功能简述:这个方法刷新FOLDERICON不会导致foldericon重新layout
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void rebuildIconCache() {
		mItemView.onIconRefresh();
	}
	
	
	public void setFolderThumbnailVisible(boolean visible) {
		mItemView.setFolderThumbnailVisible(visible);
	}
	
	public void setThumbnailVisible(boolean visible) {
		mShowThunbnail = visible;
		mItemView.setVisible(visible);
		Drawable drawable = visible == false ? null : mFolderCloseCover;
		mMultiView.setCoverImageDrawable(drawable);
		mMultiView.setCoverVisible(visible);
	}
	
	@Override
	public void reloadResource() {
		if (!mCustomStyle) {
			resetResource();
		}
	}
	
	public void resetResource() {
		loadResource();
		setIcon(null);
		setThumbnailVisible(true);
	}
	
	@Override
	public void onBCChange(int msgId, int param, Object ...object) {
		
	}

	public boolean isRecyled() {
		return mRecyled;
	}
	
	@Override
	public void checkSingleIconNormalStatus() {
		mMultiView.setCurrenState(IModelState.NO_STATE);
		mMultiView.setOnSelectClickListener(null);
	}
	
	/**
	 * 返回当前icon的图标
	 * @return
	 */
	public Drawable getIcon() {
		// TODO：返回适当的图片
		if (mMultiView != null) {
			return mMultiView.getBgDrawable();
		}
		return null;
	}
	
	public void startEditAnimation() {
		mItemView.startEditAnimation();
	}
	
	public void endEditAnimation() {
		mItemView.endEditAnimation();
	}
	
	public int getFolderChildCount() {
		return mItemView.getFolderChildCount();
	}
	
	protected abstract void addIconBitmap(int index);
	
	public void refreshForAddIcon(int target) {
		if (target > FolderConstant.MAX_ICON_COUNT + 1) {
			int childCount = getFolderChildCount();
			addIconBitmaps();
			if (childCount % 2 == 0) {
				addIconBitmap(mFolderContent.size() - 3);
				addIconBitmap(mFolderContent.size() - 2);
				addIconBitmap(mFolderContent.size() - 1);
			} else {
				addIconBitmap(mFolderContent.size() - 4);
				addIconBitmap(mFolderContent.size() - 3);
				addIconBitmap(mFolderContent.size() - 2);
				addIconBitmap(mFolderContent.size() - 1);
			}
			addIconView(mIconBitmaps, getIconSize());
			mItemView.onIconRefresh();
		} else {
			refreshIcon();
		}
	}
	
	@Override
	public void cancleFolderReady(boolean needAnimation) {
		if (mIconView instanceof GLModel3DMultiView) {
			GLModel3DMultiView icon = (GLModel3DMultiView) mIconView;
			icon.cancleFolderReady(needAnimation, this, true);
		}
	}
	
	protected boolean canAddIcon() {
		return mItemView.isEditStatus();
	};
	
	public boolean isIconEnterAnimateFinish() {
		return !mItemView.mIconViewLayout.isAnimating();
	}
	
	public void resetElementTranslate() {
		mItemView.resetElementTranslate();
	}
	
	public void resetElementStatus() {
		mItemView.resetElementStatus();
	}
	
	protected abstract int getFolderIconSize();
	
	/**
	 * 获得文件夹中小图标与正常图标的缩小比例
	 * @return
	 */
	public float[] getThumbnailScaleXY() {
		return mItemView.getThumbnailScaleXY();
	}

	/**
	 * 获得文件夹内小图标目标位置中心点<br>
	 * 注意：必须保证已经往FolderIcon里添加了该view
	 * @param target 目标位置
	 * @return
	 */
	public void getThumbnailLocationCenter(int target, int[] locate) {
		mItemView.getThumbnailLocationCenter(target, locate);
	}
	
}