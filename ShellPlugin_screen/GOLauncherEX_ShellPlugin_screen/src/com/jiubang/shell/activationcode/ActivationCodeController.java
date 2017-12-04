package com.jiubang.shell.activationcode;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.gau.golauncherex.plugin.shell.R;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.go.util.AppUtils;
import com.go.util.StringUtil;
import com.go.util.device.Machine;
import com.jiubang.ggheart.activationcode.invite.HttpDataOperator;
import com.jiubang.ggheart.appgame.appcenter.help.RecommAppsUtils;
import com.jiubang.ggheart.appgame.base.net.AppGameNetRecord;
import com.jiubang.ggheart.appgame.base.net.AppHttpAdapter;
import com.jiubang.ggheart.data.statistics.Statistics;

/**
 * 5.0版本激活码功能管理类
 * @author caoyaming
 *
 */
public class ActivationCodeController {
	//验证激活码请求地址(正式)
	//private static final String CHECK_ACTIVATION_CODE_URL = "http://api.goforandroid.com/activationcode/nlValifyclient.do";
	//验证激活码请求地址(正式)
	private static final String CHECK_ACTIVATION_CODE_URL = "http://indchat.3g.cn:8080/activationcode/nlValifyclient.do";
	//===============服务端返回码=======================
	//验证成功且打开邀请入口(200)
	public static final String HTTP_CODE_VERIFICATION_SUCCESS_OPEN_INVITE = "200"; 
	//验证成功且关闭邀请入口(201)
	public static final String HTTP_CODE_VERIFICATION_SUCCESS_CLOSE_INVITE = "201"; 
	//系统处理失败(400)
	private static final String HTTP_CODE_SYSTEM_HANDLE_FAILURE = "400"; 
	//不存在email(401)
	private static final String HTTP_CODE_EMAIL_NOT_EXIST = "401"; 
	//激活码超过androidid数,暂定每个激活码可激活５个androidid(402)
	private static final String HTTP_CODE_EXCEED_ACTIVATION_NUMBER = "402"; 
	//邮箱验证超过失败次数,暂定每个邮箱每天连续失败次数为10次,则当天锁定邮箱(403)
	private static final String HTTP_CODE_TRANSCEND_FAILURE_NUMBER = "403"; 
	//邮箱被锁(404)
	private static final String HTTP_CODE_EMAIL_LOCK = "404"; 
	//激活码无效(405)---如:激活码存在,但被退款.
	private static final String HTTP_CODE_ACTIVATION_CODE_INVALID = "405"; 
	
	//Context
	private Context mContext;
	//当前类对象
	private static ActivationCodeController sInstance;
	private ActivationCodeController(Context context) {
		mContext = context;

	}
	/**
	 * 获取单例对象
	 * @param context 
	 * @return
	 */
	public static ActivationCodeController getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ActivationCodeController(context);
		}
		return sInstance;
	}
	/**
	 * 检查桌面激活码是否正确
	 * @param activationCode 激活码
	 * @param listener 结果监听
	 */
	public boolean checkActivationCode(final String activationCode, final ReuqestDataListener listener) {
		if (listener == null) {
			return false;
		}
		//判断网络是否可用
		if (!Machine.isNetworkOK(mContext)) {
			//网络不可用
			listener.onException(mContext.getResources().getString(R.string.activationcode_network_unavailable));
			return false;
		}
		//创建线程验证激活码
		new Thread(new Runnable() {
			@Override
			public void run() {
				THttpRequest request = null;
				try {
					request = new THttpRequest(CHECK_ACTIVATION_CODE_URL, createHttpRequestData(activationCode).getBytes(), new IConnectListener() {
						@Override
						public void onStart(THttpRequest request) {
							//请求开始
						}
						@Override
						public void onFinish(THttpRequest request, IResponse response) {
							//请求完成
							if (response != null && response.getResponse() != null) {
								//获取返回码
								String responseCode = StringUtil.toString(response.getResponse());
								//返回处理结果
								listener.onFinish(responseCode, convertResponseCodeToMessage(responseCode));
							} else {
								//激活失败	
								listener.onException(mContext == null ? "" : mContext.getResources().getString(R.string.activationcode_activation_failure));
							}
						}
						@Override
						public void onException(THttpRequest request, int reason) {
							//请求失败	
							listener.onException(mContext == null ? "" : mContext.getResources().getString(R.string.activationcode_activation_failure));
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					//请求失败	
					listener.onException(mContext == null ? "" : mContext.getResources().getString(R.string.activationcode_activation_failure));
					return;
				}
				if (request != null) {
					request.setOperator(new HttpDataOperator(false));
					request.setNetRecord(new AppGameNetRecord(mContext, true));
					AppHttpAdapter httpAdapter = AppHttpAdapter.getInstance(mContext);
					httpAdapter.addTask(request, true);
				}
			}
		}).start();
		return true;
	}
	/**
	 * 创建请求参数
	 * @return
	 */
	private String createHttpRequestData(String activationCode) {
		JSONObject requestJsonObject = new JSONObject();
		//协议版本号(必填,目前是:1)
		try {
			requestJsonObject.put("pver", "1"); 
			//软件版本(必填)
			requestJsonObject.put("version", AppUtils.getVersionCodeByPkgName(mContext, mContext.getPackageName()));
			//语言(国家),优先拿SIM卡
			requestJsonObject.put("lang", RecommAppsUtils.language(mContext));
			//渠道(必填)
			requestJsonObject.put("channel", StringUtil.toInteger(Statistics.getUid(mContext), -1));
			//手机系统id(必填)
			requestJsonObject.put("androidid", StringUtil.toString(Machine.getAndroidId()));	
			//用户邮箱(没有邮箱可"")
			requestJsonObject.put("email", StringUtil.toString(Machine.getGmail(mContext)));				
			//激活码(必填)
			requestJsonObject.put("code", activationCode);		
			//产品id(必填,next桌面为:0;GO桌面prime限免为:1;主题大礼包:2;GO桌面泄密包为:3)
			requestJsonObject.put("pid", 3);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return requestJsonObject.toString();
	}
	/**
	 * 将返回码转换成提示消息
	 * @param responseCode 返回码
	 * @return
	 */
	private String convertResponseCodeToMessage(String responseCode) {
		int resourcesId = 0;
		if (HTTP_CODE_VERIFICATION_SUCCESS_OPEN_INVITE.equals(responseCode) || HTTP_CODE_VERIFICATION_SUCCESS_CLOSE_INVITE.equals(responseCode)) {
			//验证成功(200)
			resourcesId = R.string.activationcode_activation_success;
		} else if (HTTP_CODE_SYSTEM_HANDLE_FAILURE.equals(responseCode)) {
			//系统处理失败(400)
			resourcesId = R.string.activationcode_system_handle_failure;
		} else if (HTTP_CODE_EMAIL_NOT_EXIST.equals(responseCode)) {
			//不存在email(401)
			resourcesId = R.string.activationcode_email_not_exist;
		} else if (HTTP_CODE_EXCEED_ACTIVATION_NUMBER.equals(responseCode)) {
			//激活码超过androidid数,暂定每个激活码可激活５个androidid(402)
			resourcesId = R.string.activationcode_activation_code_lapsed;
		} else if (HTTP_CODE_TRANSCEND_FAILURE_NUMBER.equals(responseCode)) {
			//邮箱验证超过失败次数,暂定每个邮箱每天连续失败次数为10次,则当天锁定邮箱(403)
			resourcesId = R.string.activationcode_transcend_failure_number;
		} else if (HTTP_CODE_EMAIL_LOCK.equals(responseCode)) {
			//邮箱被锁(404)
			resourcesId = R.string.activationcode_email_lock;
		} else if (HTTP_CODE_ACTIVATION_CODE_INVALID.equals(responseCode)) {
			//激活码无效(405)---如:激活码存在,但被退款.
			resourcesId = R.string.activationcode_activation_code_invalid;
		} else {
			//激活失败
			resourcesId = R.string.activationcode_activation_failure;
		}  
		return mContext.getResources().getString(resourcesId);
	}
	
	/**
	 * 请求数据监听器
	 * @author caoyaming
	 *
	 */
	public interface ReuqestDataListener {
		/**
		 * 请求数据成功
		 * @param responseCode 返回码
		 * @param message 消息
		 */
		public void onFinish(String responseCode, String message);
		/**
		 * 请求数据失败
		 * @param errorMessage 错误消息
		 */
		public void onException(String errorMessage);
	}
	
}
