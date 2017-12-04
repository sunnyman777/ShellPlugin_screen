package com.jiubang.shell.activationcode;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

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
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnTouchListener;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLButton;
import com.go.gl.widget.GLEditText;
import com.go.proxy.MsgMgrProxy;
import com.go.util.StringUtil;
import com.go.util.graphics.BitmapUtility;
import com.go.util.graphics.DrawUtils;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.ggheart.apps.desks.diy.WallpaperControler;
import com.jiubang.ggheart.apps.desks.diy.guide.RateGuideTask;
import com.jiubang.ggheart.apps.desks.diy.pref.PrefConst;
import com.jiubang.ggheart.apps.desks.diy.pref.PrivatePreference;
import com.jiubang.ggheart.plugin.common.OrientationTypes;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.IShell;
import com.jiubang.shell.IView;
import com.jiubang.shell.activationcode.ActivationCodeController.ReuqestDataListener;
import com.jiubang.shell.common.component.GLProgressBar;
import com.jiubang.shell.common.listener.RemoveViewAnimationListener;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.orientation.GLOrientationControler;

/**
 * 5.0桌面激活页面
 * @author caoyaming
 *
 */
public class GLActivationCodeLayer extends GLFrameLayout implements
		OnTouchListener, IView, GLView.OnClickListener, Callback, OnKeyListener {
	//最上层布局
	private GLLinearLayout mMainLayout;
	//激活码输入框
	private GLEditText mActivationCodeEditText;
	//体验按钮
	private GLButton mEnterLauncherBtn;
	//加载进度Layout
	private GLLinearLayout mGLProgressBarLayout;
	//加载进度条
	private GLProgressBar mGLProgressBar;
	private ColorGLDrawable mMasker;

	private GaussianBlurGLDrawable mGaussianBlurDrawable;
	private BlurGLDrawable mBlurGLDrawable;
	private IShell mIShell;
	private Context mContext;
	private boolean mIsBlockTouch = true;
	private boolean mIsBlockPressKey = true;

	private static final int MESSAGE_START_ENTER_ANIM = 1;
	private static final int MESSAGE_BG_FADE_OUT = 2;
	private static final int MESSAGE_START_BG_ANIM = 3;
	//激活桌面成功
	private static final int MESSAGE_ACTIVATION_SUCCESS = 4;
	//使用Toast显示提示消息
	private static final int MESSAGE_SHOW_TOAST = 5;
	//隐藏Loading
	private static final int MESSAGE_HIDE_LOADING_VIEW = 6;
		
	private static final long EXIT_DURATION = 650;

	private Handler mHandler = new Handler(this);
	private GLDrawable mBgDrawable;
	private long mBgFadeoutTime;
	private boolean mGaussian = true;
	static final float LOW_DENSITY = 1.0f;
	public GLActivationCodeLayer(Context context) {
		this(context, null);
	}
	public GLActivationCodeLayer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public GLActivationCodeLayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initView();
		initBlur();
	}
	/**
	 * 初始化View
	 */
	private void initView() {
		//最上层布局
		mMainLayout = (GLLinearLayout) findViewById(R.id.main_layout);
		//获取字体文件
		Typeface typeface = Typeface.createFromAsset(ShellAdmin.sShellManager.getActivity().getAssets(), "Roboto-Light.ttf");
		//激活码输入框
		mActivationCodeEditText = (GLEditText) findViewById(R.id.activation_code_edittext);
		//设置字体文件
		mActivationCodeEditText.getTextView().setTypeface(typeface);
		//TODO：为了便于调试而增加的代码,后续需要去除 
		mActivationCodeEditText.setText("test1234");
		mActivationCodeEditText.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(GLView v, MotionEvent event) {
				return true;
			}
		});
		//体验按钮
		mEnterLauncherBtn = (GLButton) findViewById(R.id.enter_launcher_btn);
		//设置点击事件
		mEnterLauncherBtn.setOnClickListener(this);
		mEnterLauncherBtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(GLView v, MotionEvent event) {
				return false;
			}
		});
		//启动动画
		startEnterAnimation();
		//加载进度Layout
		mGLProgressBarLayout = (GLLinearLayout) findViewById(R.id.progress_layout);
		//加载进度
		mGLProgressBar = (GLProgressBar) findViewById(R.id.progress);
		mGLProgressBar.setMode(GLProgressBar.MODE_INDETERMINATE);
		Drawable drawable = mContext.getResources().getDrawable(
				R.drawable.gl_progressbar_indeterminate_white);
		mGLProgressBar.setIndeterminateProgressDrawable(drawable);
	}
	/**
	 * 初始化背景
	 */
	private void initBlur() {
		BitmapDrawable bitmapDrawable = WallpaperControler.getInstance()
				.adjustToWallpaperDrawable(mContext.getResources(), R.drawable.gl_default_wallpaper);
		bitmapDrawable = clipCenterBitmap(bitmapDrawable);
		mBgDrawable = new BitmapGLDrawable(getResources(), bitmapDrawable.getBitmap());
		
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
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mMasker != null) {
			mMasker.setBounds(0, 0, DrawUtils.getRealWidth(), DrawUtils.getRealHeight());
		}
	}
	/**
	 * 启动进入激活页面动画
	 */
	private void startEnterAnimation() {
		if (mMainLayout != null) {
			mMainLayout.setVisibility(View.VISIBLE);
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
		case MESSAGE_START_ENTER_ANIM:
			startEnterAnimation();
			break;
		case MESSAGE_START_BG_ANIM:
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.SCREEN_GUIDE_ENTER_ANIM, -1, null, null);
			startBgAnim(msg.obj);
			break;
		case MESSAGE_BG_FADE_OUT:
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
				// setVisibility(GLView.GONE);
				GLOrientationControler.setSmallModle(false);
				GLOrientationControler.keepOrientationAllTheTime(false);
				GLOrientationControler.setOrientationType(OrientationTypes.AUTOROTATION);
				if (msg.obj instanceof RemoveViewAnimationListener) {
					((RemoveViewAnimationListener) msg.obj).onAnimationEnd(null);
				}
			}
			break;
		case MESSAGE_ACTIVATION_SUCCESS: 
			//从屏幕上移除激活页面
			mIShell.remove(IViewId.SHELL_ACTIVATION, true);
			break;
		case MESSAGE_SHOW_TOAST: 
			//使用Toast显示提示消息
			if (msg.obj != null && msg.obj instanceof String) {
				Toast.makeText(mContext, StringUtil.toString(msg.obj), Toast.LENGTH_SHORT).show();
			}
			break;
		case MESSAGE_HIDE_LOADING_VIEW: 
			//使用Toast显示提示消息
			hideLoadingView();
			break;
		default:
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
	/**
	 * 检查激活码结果监听器
	 */
	private ReuqestDataListener mCheckActivationCodeListener = new ReuqestDataListener() {
		@Override
		public void onFinish(String responseCode, String messageStr) {
			//检查完成
			if (ActivationCodeController.HTTP_CODE_VERIFICATION_SUCCESS_OPEN_INVITE.equals(responseCode) || ActivationCodeController.HTTP_CODE_VERIFICATION_SUCCESS_CLOSE_INVITE.equals(responseCode)) {
				//激活成功
				Message message = new Message();
				message.what = MESSAGE_ACTIVATION_SUCCESS;
				message.obj = messageStr;
				mHandler.sendMessage(message);
				//将激活码保存
				PrivatePreference preference = PrivatePreference.getPreference(mContext);
				preference.putString(PrefConst.KEY_LAUNCHER_ACTIVATION_CODE_VALUE, getActivationCode());
				preference.commit();
				//记录打开/关闭邀请入口标识
				if (ActivationCodeController.HTTP_CODE_VERIFICATION_SUCCESS_OPEN_INVITE.equals(responseCode)) {
					//打开邀请入口
					preference.putBoolean(PrefConst.KEY_LAUNCHER_ACTIVATION_INVITE_ENTR_FALG, true);
					preference.commit();
				} else  {
					//关闭邀请入口
					preference.putBoolean(PrefConst.KEY_LAUNCHER_ACTIVATION_INVITE_ENTR_FALG, false);
					preference.commit();
				}
			} else {
				//使用Toast显示出错误消息
				Message message = new Message();
				message.what = MESSAGE_SHOW_TOAST;
				message.obj = messageStr;
				mHandler.sendMessage(message);
			}
			//隐藏Loading
			mHandler.sendEmptyMessage(MESSAGE_HIDE_LOADING_VIEW);
		}
		@Override
		public void onException(String errorMessage) {
			//检查失败,使用Toast显示出错误消息
			Message message = new Message();
			message.what = MESSAGE_SHOW_TOAST;
			message.obj = errorMessage;
			mHandler.sendMessage(message);
			//隐藏Loading
			mHandler.sendEmptyMessage(MESSAGE_HIDE_LOADING_VIEW);
		}
	};
	@Override
	public void onClick(GLView view) {
		switch (view.getId()) {
		case R.id.enter_launcher_btn:
			//点击了立即体验按钮,获取用户输入的激活码
			String activationCode = getActivationCode();
			//判断用户是否输入了,如果没有输入,则使用Toast提示用户.
			if (TextUtils.isEmpty(activationCode)) {
				//未输入
				Toast.makeText(mContext, R.string.activationcode_enter_code, Toast.LENGTH_LONG).show();
				return;
			}
			//关闭软键盘
			InputMethodManager inputmanger = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
			//到服务端检查激活码是否正确.
			if (ActivationCodeController.getInstance(mContext).checkActivationCode(activationCode, mCheckActivationCodeListener)) {
				showLoadingView();
			}
			break;
		default:
			break;
		}
	}
	/**
	 * 获取用户输入的激活码
	 * @return 激活码
	 */
	private String getActivationCode() {
		if (mActivationCodeEditText == null) {
			return null;
		}
		//获取激活码值
		return StringUtil.toString(mActivationCodeEditText.getText());
	}
	/**
	 * 启动激活页面退出动画
	 * @param obj
	 */
	private void startExitAnimation(final Object obj) {
		if (mMainLayout.getAnimation() != null) {
			return;
		}
		int screenHeight = 0;
		if (getResources().getConfiguration().orientation == OrientationTypes.VERTICAL) {
			screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
		} else {
			screenHeight = mContext.getResources().getDisplayMetrics().widthPixels;
		}
		int enterTextLoc[] = new int[2];
		mMainLayout.getLocationOnScreen(enterTextLoc);
		float enterTextOffset = screenHeight - enterTextLoc[1];
		AnimationListener listener = new AnimationListenerAdapter() {
			@Override
			public void onAnimationEnd(Animation animation) {
				Message msg = mHandler.obtainMessage(MESSAGE_START_BG_ANIM, obj);
				mHandler.sendMessage(msg);
			}
		};
		Animation guideEnterAnim = AnimationHalper.getExitAnimation(AnimationHalper.sANIM_GUIDE_EXIT_BOTTOM, enterTextOffset);
		guideEnterAnim.setAnimationListener(listener);
		mMainLayout.setHasPixelOverlayed(false);
		mMainLayout.startAnimation(guideEnterAnim);
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
		if (bitmapWidth != windowWidth || bitmapHeight != windowHeight) {
			int left = (windowWidth - bitmapWidth) / 2;
			int top = (bitmapHeight - windowHeight) / 2;
			Bitmap bitmap = BitmapUtility.createBitmap(wallpaperBitmap.getBitmap(), windowWidth, windowHeight, left, top);
			bitmapDrawable = new BitmapDrawable(bitmap);
		} else {
			bitmapDrawable = wallpaperBitmap;
		}
		return bitmapDrawable;
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
			mIShell.show(IViewId.CORE_CONTAINER, false);
			mIShell.showCoverFrame(true);
		}
	}
	/**
	 * 显示Loading
	 */
	private void showLoadingView() {
		if (mGLProgressBarLayout != null && mGLProgressBar != null && (!mGLProgressBarLayout.isShown() || !mGLProgressBar.isShown())) {
			mGLProgressBarLayout.setVisibility(View.VISIBLE);
			mGLProgressBar.show();
		}
	}
	/**
	 * 隐藏Loading
	 */
	private void hideLoadingView() {
		if (mGLProgressBarLayout != null && mGLProgressBar != null && mGLProgressBarLayout.isShown()) {
			mGLProgressBarLayout.setVisibility(View.GONE);
			mGLProgressBar.hide();
		}
	}
	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return false;
	}
	@Override
	public void setShell(IShell shell) {
		mIShell = shell;
	}
	@Override
	public int getViewId() {
		return IViewId.SHELL_ACTIVATION;
	}
	@Override
	public void onAdd(GLViewGroup parent) {
	}
	@Override
	public void onRemove() {
	}
	/**
	 * 动画类
	 * @author caoyaming
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
			case sANIM_GUIDE_EXIT_TOP: {
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

			case sANIM_GUIDE_EXIT_BOTTOM: {
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
			case sANIM_GUIDE_EXIT_MARK: {
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
			case sANIM_GUIDE_BIG_LOGO: {
				ScaleAnimation scaleAnim = new ScaleAnimation(0.4f, 1f, 0.4f,
						1f, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				scaleAnim.setDuration(1300);

				TranslateAnimation translateAnim = new TranslateAnimation(0, 0,
						0, 0, Animation.RELATIVE_TO_SELF, 1.3f,
						Animation.RELATIVE_TO_SELF, 0);
				translateAnim.setDuration(1500);

				AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
				alphaAnim.setDuration(1500);

				anim.addAnimation(scaleAnim);
				anim.addAnimation(translateAnim);
				anim.addAnimation(alphaAnim);
			}
				break;

			case sANIM_GUIDE_FUN_INDICATE: {

				ScaleAnimation scaleAnim = new ScaleAnimation(0.5f, 1f, 0.5f,
						1f, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				scaleAnim.setDuration(1300);

				float fromX = 0;
				if (obj != null) {
					fromX = (Float) obj[0];
				}

				TranslateAnimation translateAnim = new TranslateAnimation(
						Animation.RELATIVE_TO_SELF, fromX,
						Animation.RELATIVE_TO_SELF, 0,
						Animation.RELATIVE_TO_SELF, 1.1f,
						Animation.RELATIVE_TO_SELF, 0);
				translateAnim.setDuration(1300);

				AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
				alphaAnim.setDuration(1300);

				anim.addAnimation(scaleAnim);
				anim.addAnimation(translateAnim);
				anim.addAnimation(alphaAnim);
			}
				break;

			case sANIM_GUIDE_TITLE: {

				TranslateAnimation translateAnim = new TranslateAnimation(0, 0,
						0, 0, Animation.RELATIVE_TO_SELF, 5.5f,
						Animation.RELATIVE_TO_SELF, 0);
				translateAnim.setDuration(1300);
				translateAnim.setStartOffset(750);

				AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
				alphaAnim.setDuration(1300);
				alphaAnim.setStartOffset(750);

				anim.addAnimation(translateAnim);
				anim.addAnimation(alphaAnim);
			}
				break;

			case sANIM_GUIDE_CONTENT: {

				TranslateAnimation translateAnim = new TranslateAnimation(0, 0,
						0, 0, Animation.RELATIVE_TO_PARENT, 0.3f,
						Animation.RELATIVE_TO_PARENT, 0);
				translateAnim.setDuration(1200);
				translateAnim.setStartOffset(1100);

				AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
				alphaAnim.setDuration(1200);
				alphaAnim.setStartOffset(1100);

				anim.addAnimation(translateAnim);
				anim.addAnimation(alphaAnim);
			}
				break;

			case sANIM_GUIDE_ENTER: {

				TranslateAnimation translateAnim = new TranslateAnimation(0, 0,
						0, 0, Animation.RELATIVE_TO_SELF, 2.0f,
						Animation.RELATIVE_TO_PARENT, 0);
				translateAnim.setDuration(1000);
				translateAnim.setStartOffset(1600);

				AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
				alphaAnim.setDuration(1000);
				alphaAnim.setStartOffset(1600);

				anim.addAnimation(translateAnim);
				anim.addAnimation(alphaAnim);
			}
				break;

			default:
				break;
			}
			return anim;
		}
	}
}
