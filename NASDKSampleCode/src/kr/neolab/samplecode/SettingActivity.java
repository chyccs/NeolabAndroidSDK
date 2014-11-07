package kr.neolab.samplecode;

import org.json.JSONException;
import org.json.JSONObject;

import kr.neolab.samplecode.Const.Broadcast;
import kr.neolab.samplecode.Const.JsonTag;
import kr.neolab.sdk.pen.penmsg.PenMsgType;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.widget.EditText;

public class SettingActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	public static final String TAG = "pensdk.sample";
	
	private EditTextPreference mPasswordPref;
	
	private PenClientCtrl penClient;
	
	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		addPreferencesFromResource( R.xml.pref_settings );

		penClient = PenClientCtrl.getInstance( getApplicationContext() );
				
		mPasswordPref = (EditTextPreference) getPreferenceScreen().findPreference( Const.Setting.KEY_PASSWORD );

		EditText myEditText = (EditText) mPasswordPref.getEditText();
		myEditText.setKeyListener( DigitsKeyListener.getInstance( false, true ) );
	}

	public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key )
	{
		if ( key.equals( Const.Setting.KEY_PASSWORD ) )
		{
			String oldPassword = penClient.getCurrentPassword();
			String newPassword = sharedPreferences.getString( Const.Setting.KEY_PASSWORD, "0000" );

			penClient.reqSetupPassword( oldPassword, newPassword );
		}
		else if ( key.equals( Const.Setting.KEY_AUTO_POWER_ON ) )
		{
			boolean value = sharedPreferences.getBoolean( Const.Setting.KEY_AUTO_POWER_ON, true );

			penClient.reqSetupAutoPowerOnOff( value );
		}
		else if ( key.equals( Const.Setting.KEY_BEEP ) )
		{
			boolean value = sharedPreferences.getBoolean( Const.Setting.KEY_BEEP, true );
			
			penClient.reqSetupPenBeepOnOff( value );
		}
		else if ( key.equals( Const.Setting.KEY_AUTO_POWER_OFF_TIME ) )
		{
			short value = Short.parseShort( sharedPreferences.getString( Const.Setting.KEY_AUTO_POWER_OFF_TIME, "10" ) );

			penClient.reqSetupAutoShutdownTime( value );
		}
		else if ( key.equals( Const.Setting.KEY_SENSITIVITY ) )
		{
			short value = Short.parseShort( sharedPreferences.getString( Const.Setting.KEY_SENSITIVITY, "0" ) );

			penClient.reqSetupPenSensitivity( value );
		}
		else if ( key.equals( Const.Setting.KEY_PEN_COLOR ) )
		{
			int value = Integer.parseInt(sharedPreferences.getString( Const.Setting.KEY_PEN_COLOR, "-15198184" ) );

			penClient.reqSetupPenTipColor( value );
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
		IntentFilter filter = new IntentFilter( Broadcast.ACTION_PEN_MESSAGE );
		registerReceiver( mBroadcastReceiver, filter );
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( this );
		unregisterReceiver( mBroadcastReceiver );
	}

	public void handleMsg( int penMsgType, String content )
	{
		Log.d( TAG, "handleMsg : " + penMsgType);
		
		switch ( penMsgType )
		{
			// Response to the pen automatically set End Time
			case PenMsgType.PEN_SETUP_AUTO_SHUTDOWN_RESULT:
			{
				try
				{
					JSONObject job = new JSONObject( content );

					Log.d( TAG, "pen setup auto shutdown result : " + job.getBoolean( JsonTag.BOOL_RESULT ) );
				}
				catch ( JSONException e )
				{
					e.printStackTrace();
				}
			}
				break;

			// Response to the pen sensitivity setting
			case PenMsgType.PEN_SETUP_SENSITIVITY_RESULT:
			{
				try
				{
					JSONObject job = new JSONObject( content );
					Log.d( TAG, "pen setup sensitivity result : " + job.getBoolean( JsonTag.BOOL_RESULT ) );
				}
				catch ( JSONException e )
				{
					e.printStackTrace();
				}
			}
				break;
				
			// Response to the pen auto power on setting
			case PenMsgType.PEN_SETUP_AUTO_POWER_ON_RESULT:
			{
				try
				{
					JSONObject job = new JSONObject( content );
					Log.d( TAG, "pen auto power on setting result : " + job.getBoolean( JsonTag.BOOL_RESULT ) );
				}
				catch ( JSONException e )
				{
					e.printStackTrace();
				}
			}
				break;
				
			// Response to the beep on/off setting			
			case PenMsgType.PEN_SETUP_BEEP_RESULT:
			{
				try
				{
					JSONObject job = new JSONObject( content );
					Log.d( TAG, "beep on/off setting result : " + job.getBoolean( JsonTag.BOOL_RESULT ) );
				}
				catch ( JSONException e )
				{
					e.printStackTrace();
				}
			}
				break;
				
			// Response to the pen color setting
			case PenMsgType.PEN_SETUP_PEN_COLOR_RESULT:
			{
				try
				{
					JSONObject job = new JSONObject( content );
					Log.d( TAG, "pen color setting result : " + job.getBoolean( JsonTag.BOOL_RESULT ) );
				}
				catch ( JSONException e )
				{
					e.printStackTrace();
				}
			}
				break;
				
				
				
			// Pen password change success response
			case PenMsgType.PASSWORD_SETUP_SUCCESS:
				break;

			// Pen password change fails, the response
			case PenMsgType.PASSWORD_SETUP_FAILURE:
				break;
		}
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive( Context context, Intent intent )
		{
			String action = intent.getAction();

			if ( Broadcast.ACTION_PEN_MESSAGE.equals( action ) )
			{
				int penMsgType = intent.getIntExtra( Broadcast.MESSAGE_TYPE, 0 );
				String content = intent.getStringExtra( Broadcast.CONTENT );

				handleMsg( penMsgType, content );
			}
		}
	};
}
