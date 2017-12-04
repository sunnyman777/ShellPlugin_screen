package com.jiubang.shell.folder.smartcard;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.widget.GLButton;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.folder.smartcard.data.CardMemManager;

/**
 * 
 * @author guoyiqing
 * 
 */
public class GLMemCardView extends GLAbsCardView implements OnClickListener {

	private CardMemManager mMemManager;
	public float mLastMem;
	private Toast mToast;
	private GLButton mBoostTextView;
	private long mLastClickTime;

	public GLMemCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GLMemCardView(Context context) {
		super(context);
		init();
	}

	private void init() {
		mOrderLevel = ICardConst.ORDER_LEVEL_MEMCLEAN;
		mCardType = ICardConst.CARD_TYPE_MEMCLEAN;
	}

	public void setMemMamager(CardMemManager manager) {
		mMemManager = manager;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mBoostTextView = (GLButton) findViewById(R.id.smartcard_mem_boost);
		mBoostTextView.setOnClickListener(this);
		ShellTextViewWrapper detail = (ShellTextViewWrapper) findViewById(R.id.smartcard_memclean_detail);
		detail.setHasPixelOverlayed(false);
		detail.setTextColor(0xBBFFFFFF);
	}

	/**
	 * 
	 * <br>
	 * 类描述: One Key Clean Thread
	 * 
	 * @author guoyiqing
	 * @date [2013-4-11]
	 */
	class CleanProcessThread extends Thread {

		private static final String CLEAN_THREAD = "mem_clean_thread";

		public CleanProcessThread() {
			setName(CLEAN_THREAD);
		}

		@Override
		public void run() {
			super.run();
			if (mMemManager == null) {
				return;
			}
			mLastMem = mMemManager.getCurrentMem();
			AppDrawerControler controler = AppDrawerControler
					.getInstance(mContext);
			controler.terminateAllProManageTask(controler
					.getProManageFunAppItems());
			postCalMem(500);
		}
	}

	private void postCalMem(int delay) {
		postDelayed(new Runnable() {

			@Override
			public void run() {
				if (mMemManager == null) {
					return;
				}
				float current = mMemManager.getCurrentMem();
				String formate = getContext().getString(R.string.smartcard_memclean_release_tip);
				float des = Math.max(current - mLastMem, 0.01f);
				des = ((float) (int) (des * 10)) / 10;
				formate = formate + " " + des + " M";
				showToast(formate);
			}
		}, delay);
	}

	private void showToast(String tip) {
		if (mToast == null) {
			mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
		}
		mToast.setText(tip);
		mToast.cancel();
		postDelayed(new Runnable() {

			@Override
			public void run() {
				mToast.show();
			}
		}, 100);
	}

	@Override
	public void onClick(GLView v) {
		switch (v.getId()) {
		case R.id.smartcard_mem_boost:
			long current = System.currentTimeMillis();
			if (current - mLastClickTime < 500) {
				return;
			}
			mLastClickTime = current;
			new CleanProcessThread().start();
			if (mOnCardClickListener != null) {
				mOnCardClickListener.onDismissClick(this);
			}
			break;
		default:
			break;
		}
	}

}
