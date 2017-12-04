package com.jiubang.shell.common.component;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.graphics.BitmapGLDrawable;
import com.go.gl.graphics.ColorGLDrawable;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.graphics.ext.BlurGLDrawable;
import com.go.gl.graphics.ext.GaussianBlurGLDrawable;
import com.go.gl.graphics.ext.GaussianBlurProcessor;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLRelativeLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnTouchListener;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLImageView;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.VersionControl;
import com.go.util.device.Machine;
import com.go.util.graphics.BitmapUtility;
import com.go.util.graphics.DrawUtils;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.ggheart.apps.desks.diy.WallpaperControler;
import com.jiubang.ggheart.apps.desks.diy.guide.RateGuideTask;
import com.jiubang.ggheart.plugin.common.OrientationTypes;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.IShell;
import com.jiubang.shell.IView;
import com.jiubang.shell.common.listener.RemoveViewAnimationListener;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.orientation.GLOrientationControler;

/**
 * 评分引导页
 * @author zgq
 * 
 */
public class GLGuideLayer extends GLFrameLayout
		implements
			OnTouchListener,
			IView,
			GLView.OnClickListener,
			Callback {

	private GLImageView mGuideBiglogo;
	private GLViewGroup mGuideFunction;
	private GLViewGroup mGuideMainContent;

	private ShellTextViewWrapper mGuideTitle;
	private ShellTextViewWrapper mGuideNewContent;
	private ShellTextViewWrapper mGuideUpdateVersion;
	private ShellTextViewWrapper mAgreementText1;
	private GLLinearLayout mAgreementLine2;
	
	private ShellTextViewWrapper mAgreementLinkKo;
	private ShellTextViewWrapper mAgreementLink;

	private GLLinearLayout mGuideUpdateContent;

	private ShellTextViewWrapper mGuideEnter;
	private ShellTextViewWrapper mGuideRate;
	private GLLinearLayout mActionGroup;
	private ColorGLDrawable mMasker;

	private GaussianBlurGLDrawable mGaussianBlurDrawable;
	private BlurGLDrawable mBlurGLDrawable;
	private IShell mIShell;
	private Context mContext;

	private boolean mIsBlockTouch = true;
	private boolean mIsBlockPressKey = true;
	private boolean mIsUpdate = false;

	private static final int MESSAGE_START_ENTER_ANIM = 1;
	private static final int MESSAGE_BG_FADE_OUT = 2;
	private static final int MESSAGE_START_BG_ANIM = 3;

	private static final long EXIT_DURATION = 650;

	private Handler mHandler = new Handler(this);
	private GLDrawable mBgDrawable;
	private long mBgFadeoutTime;
	private boolean mGaussian = true;
	static final float LOW_DENSITY = 1.0f;
	
	private final String mAgreementAddrEN = "http://smsftp.3g.cn/soft/3GHeart/golauncher/license/golauncher_user_license_agreement_en.HTML";
	private final String mAgreementAddrCN = "http://smsftp.3g.cn/soft/3GHeart/golauncher/license/golauncher_user_license_agreement_cn.HTML";
	
	public GLGuideLayer(Context context) {
		this(context, null);
	}

	public GLGuideLayer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GLGuideLayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initView();
		initBlur();
	}
	
	public void onResume() {		
		if (mGaussian) {
			if (mGaussianBlurDrawable == null) {
				initBlur();
			}
		} else {
			if (mBlurGLDrawable == null) {
				initBlur();
			}
		}
		
		invalidate();
	}

	private void initView() {
		
//		float density = mContext.getResources().getDisplayMetrics().density;
//		int widthPixels = mContext.getResources().getDisplayMetrics().widthPixels;
//		int heightPixels = mContext.getResources().getDisplayMetrics().heightPixels;
//		
//		Log.d("displayMetrics", "density= " + density + " widthPixels= " + widthPixels + " heightPixels= " + heightPixels);

		mGuideBiglogo = (GLImageView) findViewById(R.id.guideBiglogo);
		mGuideFunction = (GLViewGroup) findViewById(R.id.guideFunction);
		mGuideMainContent = (GLViewGroup) findViewById(R.id.mainContent);
		mGuideMainContent.setOnClickListener(this);

		mActionGroup = (GLLinearLayout) findViewById(R.id.actionGroup);
		mGuideEnter = (ShellTextViewWrapper) findViewById(R.id.guideEnter);
		mGuideEnter.setOnClickListener(this);
		mGuideRate = (ShellTextViewWrapper) findViewById(R.id.guideRate);
		mGuideRate.setOnClickListener(this);
		mGuideTitle = (ShellTextViewWrapper) findViewById(R.id.guideTitle);
		mGuideNewContent = (ShellTextViewWrapper) findViewById(R.id.guideNewContent);
		mGuideUpdateVersion = (ShellTextViewWrapper) findViewById(R.id.guideUpdateVersion);
		mGuideUpdateContent = (GLLinearLayout) findViewById(R.id.guideUpdateContent);
		Typeface typeface = Typeface.createFromAsset(ShellAdmin.sShellManager.getActivity().getAssets(), "Roboto-Light.ttf");

		mAgreementText1 = (ShellTextViewWrapper) findViewById(R.id.agreementText1);
		mAgreementLine2 = (GLLinearLayout) findViewById(R.id.agreementLine2);
	
		if (VersionControl.isNewUser()) {
//		if (false) {
			mIsUpdate = false;
			mGuideRate.setVisibility(View.GONE);
			rejectEnterBtn();
			mGuideNewContent.setVisibility(View.VISIBLE);
			mGuideFunction.setVisibility(View.VISIBLE);

			
			mGuideBiglogo.setBackgroundResource(R.drawable.gl_guide_go_logo);
			mGuideTitle.setText(R.string.guide_new_user_title);
			mGuideTitle.getTextView().setTypeface(typeface);
			mGuideNewContent.getTextView().setTypeface(typeface);
			mGuideNewContent.setText(R.string.guide_new_user_content);
			
			mAgreementText1.setVisibility(View.VISIBLE);
			mAgreementLine2.setVisibility(View.VISIBLE);
			String language = Machine.getLanguage(mContext);
			if (language.equals("ko")) {
				mAgreementLinkKo = (ShellTextViewWrapper) findViewById(R.id.agreementLinkKo);
				mAgreementLinkKo.setVisibility(View.VISIBLE);
				mAgreementLinkKo.setOnClickListener(this);
			} else {
				mAgreementLink = (ShellTextViewWrapper) findViewById(R.id.agreementLink);
				mAgreementLink.setVisibility(View.VISIBLE);
				mAgreementLink.setOnClickListener(this);
			}
		} else {
			mIsUpdate = true;
			if (RateGuideTask.getInstacne(mContext).isRateEnvOk()) {
				mGuideRate.setVisibility(View.VISIBLE);
			} else {
				mGuideRate.setVisibility(View.GONE);
				rejectEnterBtn();
			}
			mGuideUpdateVersion.setVisibility(View.VISIBLE);
			mGuideUpdateContent.setVisibility(View.VISIBLE);
			mMasker = new ColorGLDrawable(Color.parseColor("#44000000"));
			
			mGuideBiglogo.setBackgroundResource(R.drawable.gl_guide_update_logo);
			GLRelativeLayout.LayoutParams layoutParams = (GLRelativeLayout.LayoutParams) mGuideTitle
					.getLayoutParams();
			layoutParams.topMargin = (int) mContext.getResources().getDimension(R.dimen.gl_guide_update_title_margin_top);
			mGuideTitle.setTextSize(20);
			mGuideTitle.getTextView().setTypeface(typeface);
			mGuideTitle.setText(R.string.guide_update_title);
			mGuideUpdateVersion.setText(R.string.updateVersion);
			mGuideEnter.setText(R.string.guide_update_enter);
			
			addUpdateSummaryContent(R.string.updatelog1);
			addUpdateSummaryContent(R.string.updatelog2);
			addUpdateSummaryContent(R.string.updatelog3);
			addUpdateSummaryContent(R.string.updatelog4);
			addUpdateSummaryContent(R.string.updatelog5);
			addUpdateSummaryContent(R.string.updatelog6);
			addUpdateSummaryContent(R.string.updatelog7);
			addUpdateSummaryContent(R.string.updatelog8);
		}
		startEnterAnimation();
	}
	
	/**
	 * 如果评分按钮是隐藏，要调整进入桌面按钮的位置
	 */
	private void rejectEnterBtn() {
	 	LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mGuideEnter.getLayoutParams();
	 	layoutParams.leftMargin = 0;
	 	layoutParams.rightMargin = 0;
	 	layoutParams.width = DrawUtils.dip2px(145);
	 	layoutParams.weight = 0;
	 	mActionGroup.setGravity(Gravity.CENTER_HORIZONTAL);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mMasker != null) {
			mMasker.setBounds(0, 0, DrawUtils.getRealWidth(), DrawUtils.getRealHeight());
		}
	}

	private void addUpdateSummaryContent(int resId) {
		String content = mContext.getResources().getString(resId);
		if (content == null || "".equals(content)) {
			return;
		}
		
		ShellTextViewWrapper textView = new ShellTextViewWrapper(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.topMargin = DrawUtils.dip2px(5);

		textView.setText(content);
		textView.setTextColor(Color.parseColor("#aaffffff"));
		if (DrawUtils.sDensity <= LOW_DENSITY) {
			textView.setTextSize(13);
		} else {
			textView.setTextSize(14);
		}
		
		textView.setGravity(Gravity.LEFT);

		mGuideUpdateContent.addView(textView, params);
	}

	private void startEnterAnimation() {

		mGuideMainContent.setVisibility(View.VISIBLE);
		mActionGroup.setVisibility(View.VISIBLE);

//		AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
//		alphaAnim.setDuration(2000);
//
//		mGuideMainContent.setHasPixelOverlayed(false);
//		mGuideMainContent.startAnimation(alphaAnim);
//		mGuideEnter.startAnimation(alphaAnim);

		//		mGuideBiglogo.startAnimation(AnimationHalper
		//				.getEnterAnimation(AnimationHalper.sANIM_GUIDE_BIG_LOGO));
		//		mFunction1.startAnimation(AnimationHalper.getEnterAnimation(
		//				AnimationHalper.sANIM_GUIDE_FUN_INDICATE, 1.5f));
		//		mFunction2.startAnimation(AnimationHalper.getEnterAnimation(
		//				AnimationHalper.sANIM_GUIDE_FUN_INDICATE, 0.8f));
		//		mFunction3.startAnimation(AnimationHalper.getEnterAnimation(
		//				AnimationHalper.sANIM_GUIDE_FUN_INDICATE, 0f));
		//		mFunction4.startAnimation(AnimationHalper.getEnterAnimation(
		//				AnimationHalper.sANIM_GUIDE_FUN_INDICATE, -0.8f));
		//		mFunction5.startAnimation(AnimationHalper.getEnterAnimation(
		//				AnimationHalper.sANIM_GUIDE_FUN_INDICATE, -1.5f));
		//
		//		mGuideTitle.startAnimation(AnimationHalper
		//				.getEnterAnimation(AnimationHalper.sANIM_GUIDE_TITLE));
		//		if (mIsUpdate) {
		//			mGuideUpdateVersion.startAnimation(AnimationHalper
		//					.getEnterAnimation(AnimationHalper.sANIM_GUIDE_CONTENT));
		//			mGuideUpdateContent.startAnimation(AnimationHalper
		//					.getEnterAnimation(AnimationHalper.sANIM_GUIDE_CONTENT));
		//		} else {
		//			mGuideNewContent.startAnimation(AnimationHalper
		//					.getEnterAnimation(AnimationHalper.sANIM_GUIDE_CONTENT));
		//		}
		//
		//		mGuideEnter.startAnimation(AnimationHalper
		//				.getEnterAnimation(AnimationHalper.sANIM_GUIDE_ENTER));
		//		mGuideEnterBg.startAnimation(AnimationHalper
		//				.getEnterAnimation(AnimationHalper.sANIM_GUIDE_ENTER));
	}
	
	private void initBlur() {
		
		if (!mIsUpdate) {
			BitmapDrawable bitmapDrawable = WallpaperControler.getInstance().adjustToWallpaperDrawable(mContext.getResources(),
					R.drawable.gl_default_wallpaper);
			bitmapDrawable = clipCenterBitmap(bitmapDrawable);
			mBgDrawable = new BitmapGLDrawable(getResources(), bitmapDrawable.getBitmap());
		} else { 
			BitmapDrawable bitmapDrawable = null;
			if (WallpaperControler.getInstance().isLiveWallpaper()) {
				bitmapDrawable = WallpaperControler.getInstance().adjustToWallpaperDrawable(mContext.getResources(),
						R.drawable.gl_default_wallpaper);
				bitmapDrawable = clipCenterBitmap(bitmapDrawable);
			} else {
				bitmapDrawable = getWallpaperBitmap();
				if (bitmapDrawable == null) {
					bitmapDrawable = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.gl_default_wallpaper);
					bitmapDrawable = clipCenterBitmap(bitmapDrawable);
				}
			}
			mBgDrawable = new BitmapGLDrawable(getResources(), bitmapDrawable.getBitmap());
		}

		int totalSteps = GaussianBlurProcessor.getDesiredBlurStep(DrawUtils.sDensity);
		int blurRadius = GaussianBlurProcessor.getDesiredBlurRadius(DrawUtils.sDensity);
		if (mBgDrawable != null) {
			if (mGaussian) {
				mGaussianBlurDrawable = new GaussianBlurGLDrawable(mBgDrawable, blurRadius);
				mGaussianBlurDrawable.setBlurStep(totalSteps, 1);
			} else {
				boolean translucent = true;
				if (mBgDrawable.getOpacity() == PixelFormat.OPAQUE) {
					translucent = false;
				}
				mBlurGLDrawable = new BlurGLDrawable(mBgDrawable, translucent);
				mBlurGLDrawable.setBlurStep(totalSteps, 1);
			}
		}
	}
	
	@Override
	protected void dispatchDraw(GLCanvas canvas) {

		if (!mGaussian) {
			if (mBlurGLDrawable != null) {
				mBlurGLDrawable.draw(canvas);
				if (!mBlurGLDrawable.isBlurDone()) {
					invalidate();
				}
			}
		} else {
			if (mGaussianBlurDrawable != null) {
				mGaussianBlurDrawable.draw(canvas);
				if (!mGaussianBlurDrawable.isBlurDone()) {
					invalidate();
				}
			}
		}
		if (mMasker != null) {
			mMasker.draw(canvas);
		}
		super.dispatchDraw(canvas);
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case MESSAGE_START_ENTER_ANIM :
				startEnterAnimation();
				break;
			case MESSAGE_START_BG_ANIM :
				mGuideMainContent.setVisibility(View.INVISIBLE);
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
						IScreenFrameMsgId.SCREEN_GUIDE_ENTER_ANIM, -1, null, null);
				startBgAnim(msg.obj);
				break;

			case MESSAGE_BG_FADE_OUT :
				long now = System.currentTimeMillis();
				long delta = now - mBgFadeoutTime;

				int alpha = 255;
				if (delta > 0 && delta < EXIT_DURATION) {
					alpha = (int) ((1.0 - (float) delta / (float) EXIT_DURATION) * 255);
					if (mMasker != null) {
						mMasker.setAlpha(alpha);
					}
					mBgDrawable.setAlpha(alpha);
					if (!mGaussian) {
						mBlurGLDrawable.setAlpha(alpha);
					} else {
						mGaussianBlurDrawable.setAlpha(alpha);
					}
					invalidate();
					Message nextMsg = mHandler.obtainMessage(MESSAGE_BG_FADE_OUT, msg.obj);
					mHandler.sendMessageDelayed(nextMsg, 20);
				} else if (delta >= EXIT_DURATION) {
//					setVisibility(GLView.GONE);
					GLOrientationControler.setSmallModle(false);
					GLOrientationControler.keepOrientationAllTheTime(false);
					GLOrientationControler.setOrientationType(OrientationTypes.AUTOROTATION);
					if (msg.obj instanceof RemoveViewAnimationListener) {
						((RemoveViewAnimationListener) msg.obj).onAnimationEnd(null);
					}
				}
				break;
			default :
				break;
		}

		return true;
	}
	
	@Override
	public void cleanup() {
		if (mMasker != null) {
			mMasker.clear();
		}
		if (mGaussian) {
			mGaussianBlurDrawable.clear();
			mGaussianBlurDrawable = null;
		} else {
			mBlurGLDrawable.clear();
			mBlurGLDrawable = null;
		}
		mBgDrawable.clear();

		super.cleanup();
	}

	private void startBgAnim(Object obj) {
		mBgFadeoutTime = System.currentTimeMillis();
		Message msg = mHandler.obtainMessage(MESSAGE_BG_FADE_OUT, obj);
		mHandler.sendMessageDelayed(msg, 60);
	}

	@Override
	public void onClick(GLView view) {
		switch (view.getId()) {
			case R.id.guideEnter :
				mIShell.remove(IViewId.SHELL_GUIDE, true);
				break;
			case R.id.guideRate :
				RateGuideTask.getInstacne(mContext).rateFromeGuideFrame();
				break;
			case R.id.agreementLinkKo :
			case R.id.agreementLink :
				
				String language = Machine.getLanguage(mContext);
				String addr = null;
				if (language.equals("zh")) {
					addr = mAgreementAddrCN;
				} else {
					addr = mAgreementAddrEN;
				}
				Intent intent = new Intent();
				intent.setData(Uri.parse(addr));
				intent.setAction(Intent.ACTION_VIEW);
				mContext.startActivity(intent);
				break;
			default :
				break;
		}
	}

	private void startExitAnimation(final Object obj) {

		if (mGuideMainContent.getAnimation() != null || mActionGroup.getAnimation() != null) {
			return;
		}

		Animation topAnimation = null;
		int contentLoc[] = new int[2];
		float toYDelta = 0;

		int screenHeight = 0;
		if (getResources().getConfiguration().orientation == OrientationTypes.VERTICAL) {
			screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
		} else {
			screenHeight = mContext.getResources().getDisplayMetrics().widthPixels;
		}
		
		int enterTextLoc[] = new int[2];
		mActionGroup.getLocationOnScreen(enterTextLoc);
		float enterTextOffset = screenHeight - enterTextLoc[1];

		if (mIsUpdate) {
			mGuideUpdateContent.getLocationOnScreen(contentLoc);
			toYDelta = mGuideUpdateContent.getHeight() + contentLoc[1];
		} else {
			mGuideNewContent.getLocationOnScreen(contentLoc);
			toYDelta = mGuideNewContent.getHeight() + contentLoc[1];
		}

		topAnimation = AnimationHalper.getExitAnimation(AnimationHalper.sANIM_GUIDE_EXIT_TOP,
				toYDelta);
		mGuideMainContent.setHasPixelOverlayed(false);
		mGuideMainContent.setAnimation(topAnimation);

		AnimationListener listener = new AnimationListenerAdapter() {

			@Override
			public void onAnimationEnd(Animation animation) {
				Message msg = mHandler.obtainMessage(MESSAGE_START_BG_ANIM, obj);
				mHandler.sendMessage(msg);
			}
		};

		Animation guideEnterAnim = AnimationHalper.getExitAnimation(
				AnimationHalper.sANIM_GUIDE_EXIT_BOTTOM, enterTextOffset);
		guideEnterAnim.setAnimationListener(listener);
		mActionGroup.setHasPixelOverlayed(false);
		mActionGroup.startAnimation(guideEnterAnim);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		boolean flag = false;
		if (mIsBlockPressKey) {
			flag = true;
		}
		return flag;
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		boolean flag = false;
		if (mIsBlockPressKey) {
			flag = true;
		}
		return flag;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean flag = false;
		if (mIsBlockPressKey) {
			flag = true;
		}
		return flag;
	}

	@Override
	public boolean onTouch(GLView v, MotionEvent event) {
		boolean flag = false;
		if (mIsBlockTouch) {
			flag = true;
		}
		return flag;
	}

	@Override
	public void setVisible(boolean visible, boolean animate, Object obj) {
		if (visible) {
			setVisible(visible);
			GLOrientationControler.setSmallModle(true);
			GLOrientationControler.setOrientationType(OrientationTypes.VERTICAL);
			mIShell.hide(IViewId.CORE_CONTAINER, false);
			mIShell.showCoverFrame(false);
			RateGuideTask.getInstacne(mContext).guideStatistics();
		} else {
			startExitAnimation(obj);
//			GLOrientationControler.setSmallModle(false);
//			GLOrientationControler.keepOrientationAllTheTime(false);
//			GLOrientationControler.setOrientationType(OrientationControl.AUTOROTATION);
			mIShell.show(IViewId.CORE_CONTAINER, false);
			mIShell.showCoverFrame(true);
		}
	}

	@Override
	public void setShell(IShell shell) {
		mIShell = shell;
	}

	@Override
	public int getViewId() {
		return IViewId.SHELL_GUIDE;
	}

	@Override
	public void onAdd(GLViewGroup parent) {
	}

	@Override
	public void onRemove() {
	}

	private BitmapDrawable getWallpaperBitmap() {

		WallpaperManager wallPaperManager = WallpaperManager.getInstance(mContext);
		Drawable drawable = wallPaperManager.getDrawable();

		if (drawable == null) {
			return null;
		}

		return clipCenterBitmap((BitmapDrawable) drawable);
	}

	private BitmapDrawable clipCenterBitmap(BitmapDrawable wallpaperBitmap) {
		if (wallpaperBitmap == null) {
			return null;
		}
		
		BitmapDrawable bitmapDrawable = null;
		int windowWidth = 0;
		int windowHeight = 0;
		if (getResources().getConfiguration().orientation == OrientationTypes.VERTICAL) {
			windowWidth = getResources().getDisplayMetrics().widthPixels;
			windowHeight = DrawUtils.getRealHeight();
		} else {
			windowWidth = DrawUtils.getRealHeight();
			windowHeight = DrawUtils.getRealWidth();
		}

		int bitmapWidth = wallpaperBitmap.getIntrinsicWidth();
		int bitmapHeight = wallpaperBitmap.getIntrinsicHeight();
		
//		Log.d("bitmap", "windowWidth= " + windowWidth + " windowHeight= " + windowHeight);
//		Log.d("bitmap", "bitmapWidth= " + bitmapWidth + " bitmapHeight= " + bitmapHeight);

		if (bitmapWidth != windowWidth || bitmapHeight != windowHeight) {

			int left = (windowWidth - bitmapWidth) / 2;
			int top = (bitmapHeight - windowHeight) / 2;
			
//			Log.d("bitmap", "left= " + left + " top= " + top);
			
			Bitmap bitmap = BitmapUtility.createBitmap(wallpaperBitmap.getBitmap(), windowWidth,
					windowHeight, left, top);
			bitmapDrawable = new BitmapDrawable(bitmap);
		} else {
			bitmapDrawable = wallpaperBitmap;
		}

		return bitmapDrawable;
	}

	/**
	 * 
	 * @author zouguiquan
	 * 
	 */
	static class AnimationHalper {

		public static final int sANIM_GUIDE_FUN_INDICATE = 1;
		public static final int sANIM_GUIDE_BIG_LOGO = 2;
		public static final int sANIM_GUIDE_TITLE = 3;
		public static final int sANIM_GUIDE_CONTENT = 4;
		public static final int sANIM_GUIDE_ENTER = 5;

		public static final int sANIM_GUIDE_EXIT_TOP = 6;
		public static final int sANIM_GUIDE_EXIT_BOTTOM = 7;
		public static final int sANIM_GUIDE_EXIT_MARK = 8;

		public static Animation getExitAnimation(int type, Object... obj) {
			AnimationSet anim = new AnimationSet(true);
			switch (type) {
				case sANIM_GUIDE_EXIT_TOP : {
					float toY = 0;
					if (obj != null) {
						toY = (Float) obj[0];
					}

					TranslateAnimation translateAnim = new TranslateAnimation(0, 0, 0, -toY);
					translateAnim.setDuration(EXIT_DURATION);
					translateAnim.setFillAfter(true);

					AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
					alphaAnim.setDuration(EXIT_DURATION);
					alphaAnim.setFillAfter(true);

					anim.setFillAfter(true);
					anim.addAnimation(translateAnim);
					anim.addAnimation(alphaAnim);
				}
					break;

				case sANIM_GUIDE_EXIT_BOTTOM : {
					float toY = 0;
					if (obj != null) {
						toY = (Float) obj[0];
					}

					TranslateAnimation translateAnim = new TranslateAnimation(0, 0, 0, toY);
					translateAnim.setDuration(EXIT_DURATION);
					translateAnim.setFillAfter(true);

					AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
					alphaAnim.setDuration(EXIT_DURATION);
					alphaAnim.setFillAfter(true);

					anim.setFillAfter(true);
					anim.addAnimation(translateAnim);
					anim.addAnimation(alphaAnim);
				}
					break;
				case sANIM_GUIDE_EXIT_MARK : {
					AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
					alphaAnim.setDuration(EXIT_DURATION);
					alphaAnim.setFillAfter(true);
					anim.addAnimation(alphaAnim);
				}
					break;
			}
			return anim;
		}

		public static Animation getEnterAnimation(int type, Object... obj) {
			AnimationSet anim = new AnimationSet(true);
			switch (type) {
				case sANIM_GUIDE_BIG_LOGO : {
					ScaleAnimation scaleAnim = new ScaleAnimation(0.4f, 1f, 0.4f, 1f,
							Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					scaleAnim.setDuration(1300);

					TranslateAnimation translateAnim = new TranslateAnimation(0, 0, 0, 0,
							Animation.RELATIVE_TO_SELF, 1.3f, Animation.RELATIVE_TO_SELF, 0);
					translateAnim.setDuration(1500);

					AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
					alphaAnim.setDuration(1500);

					anim.addAnimation(scaleAnim);
					anim.addAnimation(translateAnim);
					anim.addAnimation(alphaAnim);
				}
					break;

				case sANIM_GUIDE_FUN_INDICATE : {

					ScaleAnimation scaleAnim = new ScaleAnimation(0.5f, 1f, 0.5f, 1f,
							Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					scaleAnim.setDuration(1300);

					float fromX = 0;
					if (obj != null) {
						fromX = (Float) obj[0];
					}

					TranslateAnimation translateAnim = new TranslateAnimation(
							Animation.RELATIVE_TO_SELF, fromX, Animation.RELATIVE_TO_SELF, 0,
							Animation.RELATIVE_TO_SELF, 1.1f, Animation.RELATIVE_TO_SELF, 0);
					translateAnim.setDuration(1300);

					AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
					alphaAnim.setDuration(1300);

					anim.addAnimation(scaleAnim);
					anim.addAnimation(translateAnim);
					anim.addAnimation(alphaAnim);
				}
					break;

				case sANIM_GUIDE_TITLE : {

					TranslateAnimation translateAnim = new TranslateAnimation(0, 0, 0, 0,
							Animation.RELATIVE_TO_SELF, 5.5f, Animation.RELATIVE_TO_SELF, 0);
					translateAnim.setDuration(1300);
					translateAnim.setStartOffset(750);

					AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
					alphaAnim.setDuration(1300);
					alphaAnim.setStartOffset(750);

					anim.addAnimation(translateAnim);
					anim.addAnimation(alphaAnim);
				}
					break;

				case sANIM_GUIDE_CONTENT : {

					TranslateAnimation translateAnim = new TranslateAnimation(0, 0, 0, 0,
							Animation.RELATIVE_TO_PARENT, 0.3f, Animation.RELATIVE_TO_PARENT, 0);
					translateAnim.setDuration(1200);
					translateAnim.setStartOffset(1100);

					AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
					alphaAnim.setDuration(1200);
					alphaAnim.setStartOffset(1100);

					anim.addAnimation(translateAnim);
					anim.addAnimation(alphaAnim);
				}
					break;

				case sANIM_GUIDE_ENTER : {

					TranslateAnimation translateAnim = new TranslateAnimation(0, 0, 0, 0,
							Animation.RELATIVE_TO_SELF, 2.0f, Animation.RELATIVE_TO_PARENT, 0);
					translateAnim.setDuration(1000);
					translateAnim.setStartOffset(1600);

					AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
					alphaAnim.setDuration(1000);
					alphaAnim.setStartOffset(1600);

					anim.addAnimation(translateAnim);
					anim.addAnimation(alphaAnim);
				}
					break;

				default :
					break;
			}
			return anim;
		}
	}
}
