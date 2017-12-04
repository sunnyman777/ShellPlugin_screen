package com.jiubang.shell.folder;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import com.go.gl.animation.Animation;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.view.GLRelativeLayout;
import com.go.gl.view.GLView;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderInfo;
import com.jiubang.shell.folder.smartcard.CardViewBuilder;
import com.jiubang.shell.folder.smartcard.GLSmartCardLayout;
import com.jiubang.shell.folder.smartcard.data.CardBuildInfo;
/**
 * 
 * @author dingzijian
 *
 */
public class GLAppFolderExpandContentLayout extends GLRelativeLayout {
	private GLSmartCardLayout mSmartCardLayout;

	private Handler mHandler;

	public static final int MSG_UPDATE_APP_FOLDER_EXPAND_CONTENT_LAYOUT = 1000;

	public GLAppFolderExpandContentLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_UPDATE_APP_FOLDER_EXPAND_CONTENT_LAYOUT :
						mSmartCardLayout = (GLSmartCardLayout) msg.obj;
						setVisibility(GLView.VISIBLE);
						LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
								LayoutParams.MATCH_PARENT);
						params.leftMargin = DrawUtils.dip2px(27);
						params.rightMargin = DrawUtils.dip2px(27);
						mSmartCardLayout.setLayoutParams(params);
						addView(mSmartCardLayout, params);
						TranslateAnimation expandTranslateAnimation = new TranslateAnimation(
								Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
								Animation.RELATIVE_TO_SELF, 0.15f, Animation.RELATIVE_TO_SELF, 0);
						expandTranslateAnimation
								.setDuration(GLAppFolderMainView.sFolderAnimationDuration);
						DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(
								0.5f);
						expandTranslateAnimation.setInterpolator(decelerateInterpolator);
						startAnimation(expandTranslateAnimation);
						break;

					default :
						break;
				}
			}
		};
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	protected void updateExpandContent(CardBuildInfo cardBuildInfo) {
		switch (cardBuildInfo.getType()) {
			case GLAppFolderInfo.NO_RECOMMAND_FOLDER :
				setVisibility(GLView.INVISIBLE);
				return;
			default :
				if (mSmartCardLayout == null) {
					CardViewBuilder.getBuilder(mContext).build(cardBuildInfo, mHandler);
				}
				break;
		}
	}

	public void removeAllContent() {
		removeAllViews();
		if (mSmartCardLayout != null) {
			mSmartCardLayout.cleanup();
			mSmartCardLayout = null;
		}
	}
}
