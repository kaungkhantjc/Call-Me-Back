package com.justicecoder.cmb;
import android.widget.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.*;
import android.app.AlertDialog;
import android.content.*;
import android.view.*;
import android.provider.ContactsContract;
import android.net.Uri;
import android.database.Cursor;
import android.content.pm.PackageManager;


public class MainActivity extends UtilsActivity implements View.OnClickListener,
 CompoundButton.OnCheckedChangeListener

{
	ImageView ivMenu, ivContact, ivClose;
	TextView tvTitle, tvDev;
	EditText edt;
	Button btnSend;
	
	/* In this case, "Switch" which is available since API level 14, 
	 * is prettier than Toogle button. So I used Toogle for 
	 * under API level 14 devices and Switch for higher ones.
	 */
	Switch btnSwitchForSdk14;
	ToggleButton toggleForFroyo;
	
	SharedPreferences sp;
	SharedPreferences.Editor spEditor;
	
	private boolean canCloseAppAfterSending = false;
	private int OPERATOR_POSITION = 0;
	private String phone_number = ""; 
	 
	
	private boolean isActivityFirstResume = true;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		spEditor = sp.edit();
		
		setupRequiredViews ();		
		tvDev.setText(decodeString(DEV_STR));
		
		boolean switch_state = sp.getBoolean(PREF_SWITCH_STATE, false);
		
		if (isCurrentApiLevelLowerThanOrEquals(13)){
		toggleForFroyo.setChecked(switch_state);
		toggleForFroyo.setOnCheckedChangeListener(this);
		} else {
		btnSwitchForSdk14.setChecked(switch_state);
		btnSwitchForSdk14.setOnCheckedChangeListener(this);
		}
		
		OPERATOR_POSITION = sp.getInt(PREF_OPERATOR, 0);
		setupTitleBy (OPERATOR_POSITION);
		
		String phone_number = sp.getString(PREF_PHONE_NUMBER, "");
		edt.setText(phone_number);
	
		ivMenu.setOnClickListener(this);
		btnSend.setOnClickListener(this);
		ivContact.setOnClickListener(this);
		ivClose.setOnClickListener(this);
		edt.addTextChangedListener(phoneTextWatcher);
		tvDev.setOnClickListener(this);
		
		}
		
	private TextWatcher phoneTextWatcher = new TextWatcher(){
		@Override public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {}
		@Override public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {}
		@Override
		public void afterTextChanged(Editable p1) {
			
			String input = p1.toString();
			if (input.trim().length() == 0){ 
			/* I checked this to avoid IndexOutOfBoundsException.
			 * I don't use input.isEmpty() method because
			 * it's only available for API Level 9 and above
			 */
				return; 
			} if (!input.startsWith("0")){
				edt.removeTextChangedListener(phoneTextWatcher);
				edt.setText("09" + input);
				edt.setSelection(2 + input.length());
				edt.addTextChangedListener(phoneTextWatcher);
			} else if (!input.startsWith("09")){
				edt.removeTextChangedListener(phoneTextWatcher);
				edt.setText("09");
				edt.setSelection(2);
				edt.addTextChangedListener(phoneTextWatcher);	
			} else if (input.length() == 11){
				spEditor.putString(PREF_PHONE_NUMBER, input).commit();
				hideKeyboard(edt);
			} 
			
	}};
	
	private void setupRequiredViews()
	{
		ivMenu = findViewById(R.id.mainImageViewMenu);
		tvTitle = findViewById(R.id.mainTextViewTitle);
		tvDev = findViewById(R.id.mainTextViewDev);
		edt = findViewById(R.id.mainEditText1);
		ivContact = findViewById(R.id.mainImageView1);
		ivClose = findViewById(R.id.mainImageViewClose);
		btnSend = findViewById(R.id.mainButton1);
		
		if (isCurrentApiLevelLowerThanOrEquals(13)){
		toggleForFroyo = findViewById(R.id.mainToggleButton1);
		} else {
		btnSwitchForSdk14 = findViewById(R.id.mainSwitch1);
		}
	}

	private void setupTitleBy(int operator_position)
	{
		int operator_name_res = 0; // initialize operator name resource id
		switch (operator_position){
			case 0: 
				operator_name_res = R.string.operator_telenor;  
			break;
			case 1: 
				operator_name_res = R.string.operator_mytel;  
				break;		
			case 2: 
				operator_name_res = R.string.operator_ooredoo;  
				break;		
			case 3: 
				operator_name_res = R.string.operator_mpt;  
				break;
		}
		changeMenu(operator_name_res, operator_position);
	}
	
	public void showPopupMenu(View v) {
	
		if (isCurrentApiLevelLowerThanOrEquals(10)){ 
			/* API Level 10 and under do not support Popup Menu.	
			 * So I used Dialog to choose items 
			 */	 
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Select Operator");
			final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_item);
			arrayAdapter.add("Telenor");
			arrayAdapter.add("Mytel");
			arrayAdapter.add("Ooredoo");
			arrayAdapter.add("MPT");

			builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int oeprator_position) {
						setupTitleBy(oeprator_position);
				}});	
			builder.show();	
			
		} else {	
		PopupMenu popup = new PopupMenu(this, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.popup_menu, popup.getMenu());
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
					@Override
					public boolean onMenuItemClick(MenuItem item)
					{
						setupTitleByMenuItemPosition(item.getItemId());
						return false;
					}});
		popup.show();
		}	
	} 

	private void setupTitleByMenuItemPosition(int menuItemPosition) {
		switch (menuItemPosition){
			case R.id.menu_telenor:
				changeMenu (R.string.operator_telenor, 0);
			break;
			
			case R.id.menu_mytel:
				changeMenu (R.string.operator_mytel, 1);
				break;
				
			case R.id.menu_ooredoo:
				changeMenu (R.string.operator_ooredoo, 2);
				break;
				
			case R.id.menu_mpt:
				changeMenu (R.string.operator_mpt, 3);
				break;
		}
	}

	private void changeMenu(int operator_name_res, int position )
	{
		tvTitle.setText(getString(operator_name_res));
		OPERATOR_POSITION = position;
		spEditor.putInt(PREF_OPERATOR, position).commit();
	}

	private void checkRequirementsBeforeSendCMB(String phone_number)
	{
		this.phone_number = phone_number;
		
		if (phone_number.trim().length() == 0) {
			edt.setError("Add Phone Number"); 
			return; // return means codes ended here
		}
		else if (phone_number.length() != 11) {
			edt.setError("Total number count must be 11");
			return;
		}
		
		else if (isCurrentApiLevelLowerThanOrEquals(21)){
			sendCallMeBack(); return;
			
		}	else if (!isCallPermissionGranted() && shouldShowCallRationale()){
			showCallRationale();
			return; 
		
		} else if (!isCallPermissionGranted()) {
			requestCallPermission(); 
			return;
		} 
		
		sendCallMeBack();
	}
	
	private void sendCallMeBack(){
		
		isActivityFirstResume = false;
		spEditor.putString(PREF_PHONE_NUMBER, phone_number).commit();
		
		String ussd_prefix = "";
		switch (OPERATOR_POSITION)
		{
			case 0: ussd_prefix = "*1*" ; break;
			case 1: ussd_prefix = "*521*"; break;
			case 2: ussd_prefix = "*122*"; break;
			case 4: ussd_prefix = "*222*"; break;
		}
		Intent i = new Intent(Intent.ACTION_CALL);
		i.setData(Uri.parse("tel:" + ussd_prefix + phone_number + "%23"));
		startActivity(i);
		
		
	}
	
	private void checkRequirementsBeforePickContact()
	{
		if (isCurrentApiLevelLowerThanOrEquals(21)){
			pickContact(); return;

		} else if (!isContactPermissionGranted() && shouldShowContactRationale()){
			showContactRationale();
			return;

		} else if (!isContactPermissionGranted()) {
			requestContactPermission();
			return;
		}	
		pickContact();
	}
	
	private void pickContact()
	{
		Intent i=new Intent(Intent.ACTION_PICK);
		i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
		startActivityForResult(i, ACTIVITY_RESULT_PICK_CONTACT);
	}
	

	private void handleIntentForContact(Intent data)
	{
			Uri contactUri = data.getData();
			String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
			Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
				String number = cursor.getString(numberIndex);
				number = number.replace(" ", "");

				handleNumber(number);		
			} 
			cursor.close();
	}

	private void handleNumber(String number)
	{
		number = number.startsWith("+959") ? "09" + number.substring(4, number.length()) : number;

		int operator_name_res = 0;

		if (number.startsWith("097")){
			operator_name_res = R.string.operator_telenor;
			OPERATOR_POSITION = 0;
		} else if (number.startsWith("096")){
			operator_name_res = R.string.operator_mytel;
			OPERATOR_POSITION = 1;
		} else if (number.startsWith("099")){
			operator_name_res = R.string.operator_ooredoo;
			OPERATOR_POSITION = 2;
		} else if (number.startsWith("094") || number.startsWith("092")){
			operator_name_res = R.string.operator_mpt;
			OPERATOR_POSITION = 3;
		}

		changeMenu(operator_name_res, OPERATOR_POSITION);

		edt.removeTextChangedListener(phoneTextWatcher);
		edt.setText(number);
		edt.setSelection(number.length());
		edt.addTextChangedListener(phoneTextWatcher);	

		spEditor.putString(PREF_PHONE_NUMBER, number).commit();
		spEditor.putInt(PREF_OPERATOR, OPERATOR_POSITION).commit();
	}
	
	@Override
	public void onClick(View v)
	{
		switch (v.getId()){
			case R.id.mainImageViewMenu:
				showPopupMenu(v);
				break;

			case R.id.mainImageViewClose:
				closeApp();
				break;

			case R.id.mainButton1:
				String phone_number = edt.getText().toString().trim();		
				checkRequirementsBeforeSendCMB(phone_number);
				break;

			case R.id.mainImageView1:
				checkRequirementsBeforePickContact();
				break;
				
			case R.id.mainTextViewDev:
				visitDev();
			break;
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton p1, boolean switch_state)
	{
		canCloseAppAfterSending = switch_state;
		spEditor.putBoolean(PREF_SWITCH_STATE, switch_state).commit();
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case REQUEST_CODE_READ_CONTACT: 
					if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
						pickContact();
					} else {
						showContactRationale();
					}
			break;
			
			case REQUEST_CODE_CALL_PHONE: 
					if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
						sendCallMeBack();
					} else {
						showCallRationale();
					}
			 break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode){
			case ACTIVITY_RESULT_PICK_CONTACT:
				if (resultCode == RESULT_OK){
					handleIntentForContact (data);
				}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume()
	{
		if (!isActivityFirstResume){
			closeApp();
		} 
		super.onResume();
	} 
	
	
	
	
} 
