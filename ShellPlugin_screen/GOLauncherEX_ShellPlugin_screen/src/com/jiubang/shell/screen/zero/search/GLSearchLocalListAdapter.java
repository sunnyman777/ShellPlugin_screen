package com.jiubang.shell.screen.zero.search;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.view.View;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLImageView;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.zeroscreen.StatisticsUtils;
import com.jiubang.ggheart.zeroscreen.search.bean.SearchResultInfo;
import com.jiubang.ggheart.zeroscreen.search.contact.ContactDataItem;
import com.jiubang.ggheart.zeroscreen.search.contact.ContactDataItem.PhoneNumber;
import com.jiubang.ggheart.zeroscreen.search.util.SearchUtils;
import com.jiubang.ggheart.zeroscreen.tab.call.CallJumper;
import com.jiubang.ggheart.zeroscreen.tab.sms.SmsConstants;
import com.jiubang.ggheart.zeroscreen.tab.sms.SmsJumper;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 本地搜索结果集
 * @author liulixia
 *
 */
public class GLSearchLocalListAdapter extends GLSearchListBaseAdapter {

	private Context mContext;
	private GLLayoutInflater mInflater;
	private GoToCallListener mCallListener;
	private GoToSmsListener mSmsListener;
	private String mUnknownName;
	
	public GLSearchLocalListAdapter(ArrayList<SearchResultInfo> dataSource, Context context) {
		super(dataSource);
		mContext = context;
		mInflater = ShellAdmin.sShellManager.getLayoutInflater();
	}

	public GLSearchLocalListAdapter(Context context) {
		super();
		mContext = context;
		mInflater = ShellAdmin.sShellManager.getLayoutInflater();
		mCallListener = new GoToCallListener();
		mSmsListener = new GoToSmsListener();
		mUnknownName = context.getResources().getString(R.string.zero_screen_search_local_contact_name_unknown);
	}	

	public Bitmap getContactImage(String phoneNumber) {
		ContentResolver cr = mContext.getContentResolver();
		Bitmap bitmap = null;
		//通话电话号码获取头像uri   
		Uri uriNumber2Contacts = Uri.parse(SmsConstants.CONTACT_URI + phoneNumber);
		Cursor cursorCantacts = null;
		try {
			cursorCantacts = cr.query(uriNumber2Contacts, null, null, null, null);
			if (cursorCantacts.getCount() > 0) { //若游标不为0则说明有头像,游标指向第一条记录   
				cursorCantacts.moveToFirst();
				Long contactID = cursorCantacts
						.getLong(cursorCantacts.getColumnIndex("contact_id"));
				Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
						contactID);
				InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
				bitmap = BitmapFactory.decodeStream(input);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursorCantacts != null) {
				cursorCantacts.close();
			}
		}
		return bitmap;
	}
	
	/**
	 * 
	 * @author liulixia
	 *
	 */
	class GoToCallListener implements OnClickListener {
		private String mPhoneNum;
		public GoToCallListener(String phoneNum) {
			this.mPhoneNum = phoneNum;
		}
		
		public GoToCallListener() {
			
		}

		@Override
		public void onClick(GLView arg0) {
			mPhoneNum = (String) arg0.getTag();
			SearchUtils.getInstance(mContext).saveSearchText();
			GuiThemeStatistics.getInstance(mContext).guiStaticData(57,
					"", StatisticsUtils.SEARCH_GO, 1,
					"0", "1", "", "");
			CallJumper.getJumper().jumpToCall(mContext, mPhoneNum);
		}
	}
	
	/**
	 * 
	 * @author liulixia
	 *
	 */
	class GoToSmsListener implements OnClickListener {
		private String mPhoneNum;
		public GoToSmsListener(String phoneNum) {
			this.mPhoneNum = phoneNum;
		}
		
		public GoToSmsListener() {
			
		}
		
		@Override
		public void onClick(GLView arg0) {
			// TODO Auto-generated method stub
			mPhoneNum = (String) arg0.getTag();
			SearchUtils.getInstance(mContext).saveSearchText();
			GuiThemeStatistics.getInstance(mContext).guiStaticData(57,
					"", StatisticsUtils.SEARCH_GO, 1,
					"0", "1", "", "");
			SmsJumper.getJumper().jumpToSms(mContext, mPhoneNum);
		}
	}
	
	/**
	 * 
	 * @author liulixia
	 *
	 */
	class GoToContactInfoListener  implements OnClickListener {
		private long mContactId = 0;
		
		public GoToContactInfoListener(long contactId) {
			this.mContactId = contactId;
		}
		
		public GoToContactInfoListener() {
		}

		@Override
		public void onClick(GLView arg0) {
			// TODO Auto-generated method stub
			mContactId = (Long) arg0.getTag();
			SearchUtils.getInstance(mContext).saveSearchText();
			Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, Long.toString(mContactId));
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			mContext.startActivity(intent);
		}
		
	}
	
	/**
	 * 联系人item项
	 * @author liulixia
	 *
	 */
	class ItemViewHolder {
		public GLImageView mItemIcon;
		public ShellTextViewWrapper mItemTitle;
		public ShellTextViewWrapper mItemPhone;
		public GLImageView mItemCall;
		public GLImageView mItemSms;
		public GLLinearLayout mItemLayout;
		public GLImageView mDivider;
	}
	
	/**
	 * title项
	 * @author liulixia
	 *
	 */
	class TitleViewHolder {
		public ShellTextViewWrapper mTitle;
	}
	
	/**
	 * 应用item项
	 * @author liulixia
	 *
	 */
	class ItemViewAppHolder {
		public GLImageView mItemIcon;
		public ShellTextViewWrapper mItemTitle;
		public GLImageView mDivider;
	}
	
	/**
	 * 
	 * @author liulixia
	 *
	 */
	class GetContactIcon extends AsyncTask<Void, Void , Bitmap> {
		private GLImageView mImageView;
		private String mContactName;
		private String mContactPhone;
		
		public GetContactIcon(GLImageView image, String contactName, String contactPhone) {
			mImageView = image;
			mContactName = contactName;
			mContactPhone = contactPhone;
		}
		
		@Override
		protected Bitmap doInBackground(Void... params) {
			return getContactImage(mContactPhone);
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			if (result != null && mContactsIconList != null) {
				SoftReference<Bitmap> reference = new SoftReference<Bitmap>(result);
				mContactsIconList.put(mContactName, reference);
				String tag = (String) mImageView.getTag();
				if (tag.equals(mContactName)) {
					mImageView.setImageBitmap(result);
				}
			}
		}
	}
	
	@Override
	public void recyle() {
		super.recyle();
	}
	
	public void destory() {
		recyle();
		mContactsIconList = null;
		mCallListener = null;
		mSmsListener = null;
		mUnknownName = null;
		mContext = null;
	}

	@Override
	public GLView getView(int arg0, GLView arg1, GLViewGroup arg2) {
		// TODO Auto-generated method stub
		if (mDataSource == null || mDataSource.isEmpty()) {
			return arg1;
		}
		int type = getItemViewType(arg0);
		SearchResultInfo info = mDataSource.get(arg0);
		if (info == null) {
			return arg1;
		}
		ItemViewHolder itemHolder = null;
		TitleViewHolder titleHolder = null;
		ItemViewAppHolder itemAppHolder = null;
		if (arg1 == null) {
			switch(type) {
				case SearchResultInfo.ITEM_TYPE_APP:
					arg1 = mInflater.inflate(R.layout.gl_zero_screen_search_result_list_item_app, null);
					itemAppHolder = new ItemViewAppHolder();
					itemAppHolder.mItemIcon = (GLImageView) arg1.findViewById(R.id.search_result_list_item_icon);
					itemAppHolder.mItemTitle = (ShellTextViewWrapper) arg1.findViewById(R.id.appfunc_search_result_list_item_name);
					itemAppHolder.mDivider = (GLImageView) arg1.findViewById(R.id.appfunc_search_result_list_item_line);
					arg1.setTag(itemAppHolder);
					break;
				case SearchResultInfo.ITEM_TYPE_CONTACTS:
					arg1 = mInflater.inflate(R.layout.gl_zero_screen_search_result_list_item, null);
					itemHolder = new ItemViewHolder();
					itemHolder.mItemIcon = (GLImageView) arg1.findViewById(R.id.search_result_list_item_icon);
					itemHolder.mItemLayout = (GLLinearLayout) arg1.findViewById(R.id.appfunc_search_result_list_item_info);
					itemHolder.mItemPhone = (ShellTextViewWrapper) arg1.findViewById(R.id.appfunc_search_result_list_item_phone);
					itemHolder.mItemTitle = (ShellTextViewWrapper) arg1.findViewById(R.id.appfunc_search_result_list_item_name);
					itemHolder.mItemCall = (GLImageView) arg1.findViewById(R.id.search_result_list_item_call_icon);
					itemHolder.mItemSms = (GLImageView) arg1.findViewById(R.id.search_result_list_item_sms_icon);
					itemHolder.mDivider = (GLImageView) arg1.findViewById(R.id.appfunc_search_result_list_item_line);
					arg1.setTag(itemHolder);
					break;
				case SearchResultInfo.ITEM_TYPE_TITLE:
					arg1 = mInflater.inflate(R.layout.gl_zero_screen_search_result_list_title, null);
					titleHolder = new TitleViewHolder();
					titleHolder.mTitle = (ShellTextViewWrapper) arg1.findViewById(R.id.appfunc_search_result_list_title);
					arg1.setTag(titleHolder);
					break;
				case SearchResultInfo.ITEM_TYPE_GET_MROE_CONTACTS:
					arg1 = mInflater.inflate(R.layout.gl_zero_screen_search_result_list_more_contact, null);
					titleHolder = new TitleViewHolder();
					titleHolder.mTitle = (ShellTextViewWrapper) arg1.findViewById(R.id.appfunc_search_result_list_title);
					arg1.setTag(titleHolder);
					break;
			}
		} else {
			switch(type) {
				case SearchResultInfo.ITEM_TYPE_APP:
					itemAppHolder = (ItemViewAppHolder) arg1.getTag();
					break;
				case SearchResultInfo.ITEM_TYPE_CONTACTS:
					itemHolder = (ItemViewHolder) arg1.getTag();
					break;
				case SearchResultInfo.ITEM_TYPE_TITLE:
				case SearchResultInfo.ITEM_TYPE_GET_MROE_CONTACTS:
					titleHolder = (TitleViewHolder) arg1.getTag();
					break;
			}
		}
		
		switch(type) {
			case SearchResultInfo.ITEM_TYPE_APP:
				itemAppHolder.mItemIcon.setImageDrawable(info.mIcon);
				itemAppHolder.mItemTitle.setText(info.mTitle);
				itemAppHolder.mItemTitle.setTag(info.mIntent);
				if (info.mShowBottomLine) {
					itemAppHolder.mDivider.setVisibility(View.VISIBLE);
				} else {
					itemAppHolder.mDivider.setVisibility(View.GONE);
				}
				break;
			case SearchResultInfo.ITEM_TYPE_CONTACTS:
				final ContactDataItem person = info.mPersonInfo;
				final GLImageView icon = itemHolder.mItemIcon;
				icon.setTag(person.getName());
				List<PhoneNumber> phones = person.getPhones();
				//加载联系人图像
				Bitmap bitmap = null;
				if (mContactsIconList != null) {
					if (mContactsIconList.get(person.getName()) != null) {
						bitmap = mContactsIconList.get(person.getName()).get();
					}
				}
				if (bitmap == null && phones.size() > 0) {
					icon.setImageResource(R.drawable.gl_zero_screen_tab_default_icon);
					GetContactIcon getIcon = new GetContactIcon(icon, person.getName(), phones.get(0).number);
					getIcon.execute();
				} else {
					icon.setImageBitmap(bitmap);
				}
				
				String personName = person.getName();
				itemHolder.mItemTitle.setText((personName == null || personName.equals("")) ? mUnknownName : personName);
				
				String phone = phones.size() > 0 ? phones.get(0).number : null;
				if (phone != null) {
					itemHolder.mItemPhone.setVisibility(View.VISIBLE);
					itemHolder.mItemPhone.setText(phone);
					itemHolder.mItemCall.setTag(phone);
					itemHolder.mItemSms.setTag(phone);
					itemHolder.mItemCall.setOnClickListener(mCallListener);
					itemHolder.mItemSms.setOnClickListener(mSmsListener);
				} else {
					itemHolder.mItemPhone.setVisibility(View.GONE);
					itemHolder.mItemCall.setOnClickListener(null);
					itemHolder.mItemSms.setOnClickListener(null);
				}
				itemHolder.mItemTitle.setTag(person.getId());
				if (info.mShowBottomLine) {
					itemHolder.mDivider.setVisibility(View.VISIBLE);
				} else {
					itemHolder.mDivider.setVisibility(View.GONE);
				}
				break;
			case SearchResultInfo.ITEM_TYPE_TITLE:
			case SearchResultInfo.ITEM_TYPE_GET_MROE_CONTACTS:
				titleHolder.mTitle.setText(info.mTitle);
				if (type == SearchResultInfo.ITEM_TYPE_GET_MROE_CONTACTS) {
					titleHolder.mTitle.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(GLView arg0) {
							SearchUtils.getInstance(mContext).showMoreContacts();
						}
					});
				}
				break;
			
		}
		return arg1;
	}
}
