package com.jiubang.shell.screen.zero.navigation.data;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;

import com.gau.golauncherex.plugin.shell.R;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.go.gl.widget.GLImageView;
import com.go.proxy.ApplicationProxy;
import com.jiubang.ggheart.appgame.base.manage.LruCache;
import com.jiubang.ggheart.appgame.base.net.AppHttpAdapter;
import com.jiubang.ggheart.zeroscreen.navigation.data.NavigationController;
import com.jiubang.ggheart.zeroscreen.navigation.data.SuggestSiteObtain;
import com.jiubang.ggheart.zeroscreen.navigation.data.SuggestSiteOperator;
import com.jiubang.ggheart.zeroscreen.navigation.data.ToolUtil;
import com.jiubang.shell.common.component.ShellTextViewWrapper;

/**
 * @author zhangkai
 * 推荐网址的图标缓存器
 */
public class GLSuggestImageCache {
	/**
	 * 强引用图片缓存的大小，1M
	 */
	private static final int DEFAULT_MAX_MEMORY_SIZE = 1 * 1024 * 1024;

	/**
	 * 强引用缓存，线程安全
	 * 当缓存超过限定大小时，该缓存会把最久没有使用的图片从缓存中移除，直到小于限制值为止
	 */
	private LruCache<String, Bitmap> mLruCache = null;

	private static GLSuggestImageCache sINSTANCE;

	/**
	 * 弱引用缓存
	 */
	private ConcurrentHashMap<String, SoftReference<Bitmap>> mSoftCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>();

	public static final int TYPE_LOGO = 1;
	public static final int TYPE_FRONT = 2;

	protected static final int GET_FROM_DB_SUCCESS = 0;
	protected static final int GET_FRONT_LOGO = 1;

	protected static final int TEXTVIEW = 1;

	//	private static final String GET_LOGO_FROM_DB = "GET_LOGO_FROM_DB";
	//	private static final String GET_ICON_FROM_WEB = "GET_LOGO_FROM_DB";

	Map<String, GLImageView> map = new HashMap<String, GLImageView>();
	Map<String, ShellTextViewWrapper> mTextMap = new HashMap<String, ShellTextViewWrapper>();

	private Handler mHandler;

	private HandlerThread mGetUrlIconFromWebThread;
	private Handler mAsyHandler;

	private Context mContext;

	private GLSuggestImageCache(int maxMemorySize, Context context) {

		mContext = context;

		initLruCache(maxMemorySize);

		initHandler();

		initAsyHandler();

	}

	private void initLruCache(int maxMemorySize) {
		mLruCache = new LruCache<String, Bitmap>(maxMemorySize) {

			@Override
			protected void entryRemoved(boolean evicted, String key, Bitmap oldValue,
					Bitmap newValue) {
				//如果超过了大小，就把从强引用移除的图片加入到弱引用中
				if (evicted) {
					mSoftCache.put(key, new SoftReference<Bitmap>(oldValue));
				}
			}

			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}

		};
	}

	public static GLSuggestImageCache getInstance(Context context) {
		if (sINSTANCE == null) {
			synchronized (GLSuggestImageCache.class) {
				if (sINSTANCE == null) {
					sINSTANCE = new GLSuggestImageCache(DEFAULT_MAX_MEMORY_SIZE, context);
				}
			}
		}

		return sINSTANCE;
	}

	/**
	 * 设置一个键值
	 * 
	 * @param key
	 * @param value
	 */
	public void set(String key, Bitmap value) {
		if (key == null || value == null) {
			return;
		}
		if (mLruCache != null) {
			mLruCache.put(key, value);
		}
	}

	/**
	 * 获取值，没有则返回空
	 * 
	 * @param key
	 * @return
	 */
	public Bitmap get(final String key, final int type, final GLImageView imageView,
			final ShellTextViewWrapper textView) {
		
		if (key == null) {
			return null;
		}
		//先从强引用缓存中取，如果取不到的话，再从弱引用缓存里面取
		Bitmap bitmap = null;
		bitmap = mLruCache.get(key);
		if (bitmap == null) {
			SoftReference<Bitmap> softReference = mSoftCache.get(key);
			if (softReference != null) {
				bitmap = softReference.get();
			}

			// 若还为空 将图片从数据库读出来并放入缓存器中
			if (bitmap == null) {
				mAsyHandler.post(new Runnable() {
					@Override
					public void run() {
						Bitmap b = ToolUtil.decodeByteArray(SuggestSiteOperator.getInstance(
								mContext).getIconByteByUrl(key, type));
						if (b != null) {
							set(key, b);

							if (type == TYPE_LOGO) {
								Message msg = Message.obtain();
								msg.what = GET_FROM_DB_SUCCESS;
								msg.obj = key;
								msg.arg1 = type;
								mHandler.sendMessage(msg);

							} else if (type == TYPE_FRONT) {
								Message msg = Message.obtain();
								msg.what = GET_FRONT_LOGO;
								msg.obj = key;
								msg.arg1 = type;
								mHandler.sendMessage(msg);
								/*MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
										IDiyMsgIds.SCREEN_ZERO_SEND_MESSAGE,
										ZeroScreenParamId.SCREEN_ZERO_NAVIGATION_LOGO_FROM_NET, b,
										null)*/;
							}
						} else {
							try {
								THttpRequest request = null;
								if (type == GLSuggestImageCache.TYPE_LOGO) {
									request = new THttpRequest(key, null, new IConnectListener() {

										@Override
										public void onException(THttpRequest arg0, int arg1) {

										}

										@Override
										public void onFinish(THttpRequest arg0, IResponse response) {
											if (response != null
													&& response.getResponse() != null
													&& IResponse.RESPONSE_TYPE_BYTEARRAY == response
															.getResponseType()) {

												byte[] data = (byte[]) response.getResponse();

												NavigationController.getInstance(mContext)
														.insertZeroScreenAdSuggestSiteLogoIcon(
																data, key);

												Message msg = Message.obtain();
												msg.what = GET_FROM_DB_SUCCESS;
												msg.obj = key;
												msg.arg1 = type;
												mHandler.sendMessage(msg);
											}
										}

										@Override
										public void onStart(THttpRequest arg0) {

										}

									});

								} else if (type == GLSuggestImageCache.TYPE_FRONT) {
									final String frontUrl = SuggestSiteObtain.getInstance()
											.getFrontIconUrl(key);

									if (frontUrl == null || frontUrl.length() < 1) {
										return;
									}

									request = new THttpRequest(frontUrl, null,
											new IConnectListener() {

												@Override
												public void onException(THttpRequest arg0, int arg1) {

												}

												@Override
												public void onFinish(THttpRequest arg0,
														IResponse response) {
													if (response != null
															&& response.getResponse() != null
															&& IResponse.RESPONSE_TYPE_BYTEARRAY == response
																	.getResponseType()) {
														byte[] data = (byte[]) response
																.getResponse();
														NavigationController
																.getInstance(mContext)
																.insertZeroScreenAdSuggestSiteFrontPic(
																		data, frontUrl);
														Message msg = Message.obtain();
														msg.what = GET_FRONT_LOGO;
														msg.obj = key;
														msg.arg1 = type;
														mHandler.sendMessage(msg);
														/*GoLauncher
																.sendMessage(
																		this,
																		IDiyFrameIds.SCREEN,
																		IDiyMsgIds.SCREEN_ZERO_SEND_MESSAGE,
																		ZeroScreenParamId.SCREEN_ZERO_NAVIGATION_LOGO_FROM_NET,
																		ToolUtil.decodeByteArray(data),
																		null);*/
													}
												}

												@Override
												public void onStart(THttpRequest arg0) {

												}
											});
								}

								AppHttpAdapter.getInstance(ApplicationProxy.getContext()).addTask(request);
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (URISyntaxException e) {
								e.printStackTrace();
							}

						}

					}
				});
			}
		}

		return bitmap;
	}
	
	private HttpURLConnection mUrlConn;
	
	public InputStream getInputStream(String urlStr) throws IOException {
		InputStream is = null;
		try {
			URL url = new URL(urlStr);
			mUrlConn = (HttpURLConnection) url
					.openConnection();
			mUrlConn.setReadTimeout(30000);
			mUrlConn.setConnectTimeout(30000);
			is = mUrlConn.getInputStream();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return is;
	}
	
	private void initAsyHandler() {
		mGetUrlIconFromWebThread = new HandlerThread("get-web-site-icon-from-service");
		mGetUrlIconFromWebThread.start();
		mAsyHandler = new Handler();
	}

	private void initHandler() {
		mHandler = new Handler(new Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {

					case GET_FROM_DB_SUCCESS :
						final String url = (String) msg.obj;
						final int typ = msg.arg1;
						GLImageView v = map.get(url);
						asynView(v, null, typ);
						return true;
					case GET_FRONT_LOGO :
						final String front_url = (String) msg.obj;
						final int front_type = msg.arg1;
						GLImageView front_ImageView = map.get(front_url);
						ShellTextViewWrapper front_textView = mTextMap.get(front_url);
						asynView(front_ImageView, front_textView, front_type);
						return true;
					default :
						break;
				}

				return false;
			}
		});
	}

	public void clear() {
		mLruCache.evictAll();
		mSoftCache.clear();
		mTextMap.clear();
		map.clear();
	}

	public void remove(String key) {
		mLruCache.remove(key);
		mSoftCache.remove(key);
		mTextMap.remove(key);
		map.remove(key);
	}

	public void recycle(String key) {
		Bitmap bitmap = mLruCache.remove(key);
		if (bitmap == null) {
			SoftReference<Bitmap> softReference = mSoftCache.remove(key);
			if (softReference == null) {
				return;
			}
			bitmap = softReference.get();
		}
		if (bitmap == null) {
			return;
		}
		if (!bitmap.isRecycled()) {
			bitmap.recycle();
		}
		bitmap = null;
	}

	/**
	 * 异步显示热门网址LOGO图标
	 */
	public void asynView(GLImageView imageView, ShellTextViewWrapper textView, int type) {
		if (imageView == null) {
			return;
		}

		String logourl = (String) imageView.getTag();

		if (logourl == null || logourl.length() == 0) {
			return;
		}
		
		Bitmap b = get(logourl, type, imageView, textView);

		if (b != null) {
			if (type == TYPE_FRONT) {
				imageView.setImageDrawable(new  BitmapDrawable(ApplicationProxy.getContext().getResources(),
						b));
				textView.setText(null);
			} else {
				imageView.setImageBitmap(b);
			}
		} else {
			if (type == TYPE_LOGO) {
				imageView.setImageResource(R.drawable.gl_website);
			} else {
				mTextMap.put(logourl, textView);
			}
			map.put(logourl, imageView);
		}
	}
}
