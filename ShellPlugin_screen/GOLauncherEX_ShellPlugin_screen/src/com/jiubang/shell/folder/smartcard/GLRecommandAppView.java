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
import com.jiubang.ggheart.appgame.base.manage.LruImageCache;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.smartcard.Recommanditem;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
/**
 * 
 * @author dingzijian
 *
 */
public class GLRecommandAppView extends GLAbsCardView implements OnClickListener {
	private GLImageView mAppIcon;

	private ShellTextViewWrapper mAppTitle;

	private ShellTextViewWrapper mAppSummary;

	private GLSmartCardButton mAddBtn;

	private GLSmartCardButton mNextBtn;

	private AppDataEngine mDataEngine;

	private OnNextBtnClickListener mNextBtnClickListener;

	private Recommanditem mRecommanditem;

	public GLRecommandAppView(Context context) {
		super(context);
	}

	public GLRecommandAppView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mOrderLevel = ICardConst.ORDER_LEVEL_APP;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mAppIcon = (GLImageView) findViewById(R.id.gl_recommand_app_imageView);
		mAppTitle = (ShellTextViewWrapper) findViewById(R.id.gl_recommand_app_title);
		mAppSummary = (ShellTextViewWrapper) findViewById(R.id.gl_recommand_app_summary);
		mNextBtn = (GLSmartCardButton) findViewById(R.id.gl_recommand_app_next_btn);
		mAddBtn = (GLSmartCardButton) findViewById(R.id.gl_recommand_app_add_btn);
		mAddBtn.setOnClickListener(this);
		mNextBtn.setOnClickListener(this);
		mDataEngine = AppDataEngine.getInstance(ApplicationProxy.getContext());
	}

	public void setInfo(Recommanditem recommanditem, boolean showNext) {
		mRecommanditem = recommanditem;
		mAppTitle.setText(recommanditem.getAppName());
		mAppSummary.setText(recommanditem.getSummary());
		mNextBtn.setVisible(showNext);
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
		mAppIcon.setImageDrawable(icon);
	}

	@Override
	public void onClick(GLView v) {
		switch (v.getId()) {
			case R.id.gl_recommand_app_add_btn :
				AppUtils.gotoMarket(ApplicationProxy.getContext(), mRecommanditem.getDownloadUrl());
				break;
			case R.id.gl_recommand_app_next_btn :
				if (mNextBtnClickListener != null) {
					mNextBtnClickListener.onNextBtnClick();
				}
				break;
			default :
				break;
		}
	}

	public void setNextBtnClickListener(OnNextBtnClickListener mNextBtnClickListener) {
		this.mNextBtnClickListener = mNextBtnClickListener;
	}
/**
 * 
 * @author dingzijian
 *
 */
	public interface OnNextBtnClickListener {
		public void onNextBtnClick();
	}
}
