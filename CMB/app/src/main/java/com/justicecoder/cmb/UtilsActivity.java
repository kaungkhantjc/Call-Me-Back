package com.justicecoder.cmb;

import android.util.Base64;
import android.os.Build;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.app.*;
import android.content.DialogInterface;
import android.Manifest;
import android.content.pm.*;
import android.content.Intent;
import android.net.Uri;

public class UtilsActivity extends Activity
{
	public final String DEV_STR = "Q01CIGFwcCB3YXMgZGV2ZWxvcGVkIGJ5IEthdW5nIEtoYW50IEt5YXcu";
	
	public final String PREF_OPERATOR = "PREF_OPERATOR";
	public final String PREF_SWITCH_STATE = "PREF_SWITCH_STATE";
	public final String PREF_PHONE_NUMBER = "PREF_PHONE_NUMBER";
	
	public final int REQUEST_CODE_READ_CONTACT = 1;
	public final int REQUEST_CODE_CALL_PHONE = 2;
	public final int ACTIVITY_RESULT_PICK_CONTACT = 3;
	
	
	public String decodeString (String s){
		return new String (Base64.decode(s, Base64.DEFAULT));
	}

	public boolean isCurrentApiLevelLowerThanOrEquals (int sdk){
		return Build.VERSION.SDK_INT <= sdk;
	}
	
	public void hideKeyboard (EditText edt){
		InputMethodManager im = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		if(im.isActive() && edt!= null){
			im.hideSoftInputFromWindow(edt.getWindowToken(), 0);
		}	
	}
	
	public void closeApp (){
			finish();
	}
	
	public void showAlert (Exception e){
		final AlertDialog ad = new AlertDialog.Builder(UtilsActivity.this).create();
		ad.setMessage(e.getClass().getName() + "\n" + e.getMessage());
		ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					ad.dismiss();
				}});
		ad.show();	
	}
	
	
	public void showContactRationale (){
		final AlertDialog ad = new AlertDialog.Builder(UtilsActivity.this).create();
		ad.setMessage(getString(R.string.read_contact_rationale));
		ad.setButton(AlertDialog.BUTTON_POSITIVE, "Allow", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					requestContactPermission();
				}});
				
		ad.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					ad.dismiss();
				}});
		ad.show();		
	}
	
	public void showCallRationale (){
		final AlertDialog ad = new AlertDialog.Builder(UtilsActivity.this).create();
		ad.setMessage(getString(R.string.call_phone_rationale));
		ad.setButton(AlertDialog.BUTTON_POSITIVE, "Allow", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					requestCallPermission();
				}});

		ad.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					ad.dismiss();
				}});
		ad.show();		
	}
	
	// Request Permission methods for API level 22 and above
	
	private boolean isPermissionGranted (String permission){
		return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
	}
	
	public boolean isContactPermissionGranted (){
	return isCurrentApiLevelLowerThanOrEquals(21) ||
	isPermissionGranted(Manifest.permission.READ_CONTACTS);
	};
		
	public boolean isCallPermissionGranted (){
		return isCurrentApiLevelLowerThanOrEquals(21) ||
			isPermissionGranted(Manifest.permission.CALL_PHONE);
	};
	
	public boolean shouldShowContactRationale(){
		return shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS);
	}
	
	public boolean shouldShowCallRationale(){
		return shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE);
	}
	
	public void requestContactPermission (){
		requestPermissions(new String[] {Manifest.permission.READ_CONTACTS}, REQUEST_CODE_READ_CONTACT );
	}
	
	public void requestCallPermission (){
		requestPermissions(new String[] {Manifest.permission.CALL_PHONE}, REQUEST_CODE_CALL_PHONE );
	}

	public void visitDev(){
		try
		{
			PackageManager packageManager = getPackageManager();
			ApplicationInfo appInfo = packageManager.getApplicationInfo("com.facebook.katana", 0);
			if (appInfo.enabled)
			{ Intent i1 = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/100012183557720")); 
			   startActivity(i1); 
			 } else  { 
			Intent i2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/kaungkhantkyawprofile")); 
			startActivity(i2); 
			}
		}
		catch (Exception e) {
			Intent i3 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/kaungkhantkyawprofile")); 
		startActivity(i3); 
	     }
	}
	

	
}
