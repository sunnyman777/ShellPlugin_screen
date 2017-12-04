package com.jiubang.shell.folder.smartcard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.widget.GLImageView;
import com.go.proxy.ApplicationProxy;
import com.go.util.AppUtils;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.manage.LruImageCache;
import com.jiubang.ggheart.apps.desks.diy.SpecialAppManager;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.smartcard.Recommanditem;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.folder.BaseFolderIcon;
import com.jiubang.shell.folder.GLAppFolder;
/**
 * 
 * @author dingzijian
 *
 */
public class GLLightGameView extends GLAbsCardView implements OnClickListener {

	private GLImageView mGameIcon;

	private GLSmartCardButton mPlayBtn;

	private ShellTextViewWrapper mGameTitle;

	private ShellTextViewWrapper mGameSummary;

	private GLSmartCardButton mAddBtn;

	private Recommanditem mRecommanditem;

	private AppDataEngine mDataEngine;

	public GLLightGameView(Context context) {
		super(context);
	}

	public GLLightGameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDataEngine = AppDataEngine.getInstance(mContext);
		mOrderLevel = ICardConst.ORDER_LEVEL_LIGHTGAME;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mGameIcon = (GLImageView) findViewById(R.id.gl_light_game_imageView);
		mGameTitle = (ShellTextViewWrapper) findViewById(R.id.gl_light_game_title);
		mGameSummary = (ShellTextViewWrapper) findViewById(R.id.gl_light_game_summary);
		mPlayBtn = (GLSmartCardButton) findViewById(R.id.gl_light_game_play_btn);
		mAddBtn = (GLSmartCardButton) findViewById(R.id.gl_light_game_add_btn);
		mAddBtn.setOnClickListener(this);
		mPlayBtn.setOnClickListener(this);
	}

	public void setInfo(Recommanditem recommanditem) {
		mRecommanditem = recommanditem;
		mGameTitle.setText(recommanditem.getAppName());
		mGameSummary.setText(recommanditem.getSummary());
		Bitmap bitmap = AsyncImageManager.getLruInstance(LruImageCache.DEFAULT_SIZE).loadImage(
				LauncherEnv.Path.SMARTCARD_RECOMMEND_ICON_PATH,
				String.valueOf(recommanditem.getIconUrl().hashCode()), recommanditem.getIconUrl(),
				true, null, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						setIcon(imageBitmap);
					}

				});
		if (bitmap != null) {
			setIcon(bitmap);
		}
	}
	private void setIcon(Bitmap imageBitmap) {
		BitmapDrawable icon = mDataEngine.createBitmapDrawable(new BitmapDrawable(imageBitmap));
		mGameIcon.setImageDrawable(icon);
	}

	@Override
	public void onClick(GLView v) {
		switch (v.getId()) {
			case R.id.gl_light_game_add_btn :
				BaseFolderIcon<?> folderIcon = GLAppFolder.getInstance().getCurFolderIcon();
				SpecialAppManager.getInstance().installSpecialApp(
						PackageName.RECOMMEND_APP_PACKAGE, ICustomAction.ACTION_LIGHTGAME,
						folderIcon, mRecommanditem);
				break;
			case R.id.gl_light_game_play_btn :
				AppUtils.gotoBrowser(ApplicationProxy.getContext(), mRecommanditem.getDownloadUrl());
				break;
			default :
				break;
		}
	}

}
