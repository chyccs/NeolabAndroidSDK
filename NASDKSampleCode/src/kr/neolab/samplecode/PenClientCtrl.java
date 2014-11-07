package kr.neolab.samplecode;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import kr.neolab.sdk.pen.IPenCtrl;
import kr.neolab.sdk.pen.PenCtrl;
import kr.neolab.sdk.pen.penmsg.IPenMsgListener;
import kr.neolab.sdk.pen.penmsg.PenMsg;
import kr.neolab.sdk.pen.penmsg.PenMsgType;
import kr.neolab.sdk.util.NLog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PenClientCtrl implements IPenMsgListener
{
	public static PenClientCtrl myInstance;

	private IPenCtrl iPenCtrl;

	public static int USING_SECTION_ID = 3;
	public static int USING_OWNER_ID = 27;

	public static int[] USING_NOTES = new int[] { 301, 302, 303, 28, 50, 101, 102, 103, 201, 202, 203, 600, 601, 602, 603, 605, 606, 607, 608 };

	private Context context;

	private SharedPreferences mPref;

	private boolean isConnected = false;
	private boolean isAuthorized = false;

	private String curPass = "0000", newPass = "0000";
	
	public String getCurrentPassword()
	{
		return curPass;
	}

	private PenClientCtrl( Context context )
	{
		this.context = context;

		iPenCtrl = PenCtrl.getInstance();

		// Specify where to store the offline data. (Unless otherwise specified,
		// is stored in the default external storage)
		// inPath = this.getFilesDir().getAbsolutePath();
		// iPenCtrl.setOfflineDataLocation(inPath);

		// start up pen controller
		iPenCtrl.startup();

		// regist callback interface
		iPenCtrl.setListener( this );

		mPref = PreferenceManager.getDefaultSharedPreferences( context );
	}

	public static synchronized PenClientCtrl getInstance( Context context )
	{
		if ( myInstance == null )
		{
			myInstance = new PenClientCtrl( context );
		}

		return myInstance;
	}

	public boolean isAuthorized()
	{
		return isAuthorized;
	}

	public boolean isConnected()
	{
		return isConnected;
	}

	public void connect( String address )
	{
		iPenCtrl.connect( address );
	}

	public void disconnect()
	{
		iPenCtrl.disconnect();
	}

	public void upgradePen(File fwFile) 
	{   
		iPenCtrl.upgradePen(fwFile);
	}
	
	public void suspendPenUpgrade()
	{
		iPenCtrl.suspendPenUpgrade();
	}
	
	public void inputPassword( String password )
	{
		curPass = password;
		iPenCtrl.inputPassword( password );
	}

	public void reqSetupPassword( String oldPassword, String newPassword )
	{
		iPenCtrl.reqSetupPassword( oldPassword, newPassword );
	}

	public void reqOfflineDataList() 
	{
		iPenCtrl.reqOfflineDataList();
	}
	
	public void reqPenStatus() 
	{
		iPenCtrl.reqPenStatus();
	}
	
	public void reqSetupAutoPowerOnOff(boolean setOn) 
	{
		iPenCtrl.reqSetupAutoPowerOnOff( setOn );
	}
	
    public void reqSetupPenBeepOnOff( boolean setOn )
    {
		iPenCtrl.reqSetupPenBeepOnOff( setOn );
    }

	public void reqSetupPenTipColor( int color )
	{
		iPenCtrl.reqSetupPenTipColor( color );
	}

	public void reqSetupAutoShutdownTime( short minute )
	{
		iPenCtrl.reqSetupAutoShutdownTime( minute );
	}

	public void reqSetupPenSensitivity( short level )
	{
		iPenCtrl.reqSetupPenSensitivity( level );
	}
	
	@Override
	public void onReceiveDot( int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color )
	{
		sendPenDotByBroadcast( sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color );
	}

	@Override
	public void onReceiveMessage( PenMsg penMsg )
	{
		switch ( penMsg.penMsgType )
		{
			// Pens when the connection is complete (that is still going through the certification process state)
			case PenMsgType.PEN_CONNECTION_SUCCESS:

				isConnected = true;

				break;

			// Fired when ready to use pen
			case PenMsgType.PEN_AUTHORIZED:

				isAuthorized = true;

				// notify using note
				iPenCtrl.reqAddUsingNote( USING_SECTION_ID, USING_OWNER_ID, USING_NOTES );
//				iPenCtrl.reqAddUsingNote( USING_SECTION_ID, USING_OWNER_ID );
//				iPenCtrl.reqAddUsingNoteAll();
				
				// to request offline data list
				iPenCtrl.reqOfflineDataList();
				
				//iPenCtrl.reqOfflineData( 4, 301, 0 );
				iPenCtrl.reqOfflineData( USING_SECTION_ID, USING_OWNER_ID, 301 );
				
				break;

			case PenMsgType.PEN_DISCONNECTED:

				isConnected = false;
				isAuthorized = false;

				break;

			case PenMsgType.PASSWORD_REQUEST:
			{
				JSONObject job = penMsg.getContentByJSONObject();
				
				try
				{
					int count = job.getInt( Const.JsonTag.INT_PASSWORD_RETRY_COUNT );
					
					NLog.d("password count : " + count);
					
					// Initial password is 0000. If you have not changed the transmission apart since 0000
					if ( count == 0 )
					{
						inputPassword(curPass);
						return;
					}
				}
				catch ( JSONException e )
				{
					e.printStackTrace();
				}
			}
				break;
				
			// Pen a response to the status request (whenever a message comes in should be reflected in the app)
			case PenMsgType.PEN_STATUS:
			{
				JSONObject job = penMsg.getContentByJSONObject();

				if ( job == null )
				{
					return;
				}

				NLog.d( job.toString() );
				
				mPref = PreferenceManager.getDefaultSharedPreferences( context );

				SharedPreferences.Editor editor = mPref.edit();

				try
				{
					String stat_version = job.getString( Const.JsonTag.STRING_PROTOCOL_VERSION );

					int stat_timezone = job.getInt( Const.JsonTag.INT_TIMEZONE_OFFSET );
					long stat_timetick = job.getLong( Const.JsonTag.LONG_TIMETICK );
					int stat_forcemax = job.getInt( Const.JsonTag.INT_MAX_FORCE );
					int stat_battery = job.getInt( Const.JsonTag.INT_BATTERY_STATUS );
					int stat_usedmem = job.getInt( Const.JsonTag.INT_MEMORY_STATUS );

					int stat_pencolor = job.getInt( Const.JsonTag.INT_PEN_COLOR );

					boolean stat_autopower = job.getBoolean( Const.JsonTag.BOOL_AUTO_POWER_ON );
					boolean stat_accel = job.getBoolean( Const.JsonTag.BOOL_ACCELERATION_SENSOR );
					boolean stat_hovermode = job.getBoolean( Const.JsonTag.BOOL_HOVER );
					boolean stat_beep = job.getBoolean( Const.JsonTag.BOOL_BEEP );

					int stat_autopower_time = job.getInt( Const.JsonTag.INT_AUTO_POWER_OFF_TIME );
					int stat_sensitivity = job.getInt( Const.JsonTag.INT_PEN_SENSITIVITY );

					editor.putBoolean( Const.Setting.KEY_ACCELERATION_SENSOR, stat_accel );
					editor.putString( Const.Setting.KEY_AUTO_POWER_OFF_TIME, ""+stat_autopower_time );
					editor.putBoolean( Const.Setting.KEY_AUTO_POWER_ON, stat_autopower );
					editor.putBoolean( Const.Setting.KEY_BEEP, stat_beep );
					editor.putString( Const.Setting.KEY_PEN_COLOR, ""+stat_pencolor );
					editor.putString( Const.Setting.KEY_SENSITIVITY, ""+stat_sensitivity );

					editor.putString( Const.Setting.KEY_PASSWORD, getCurrentPassword() );
					
					editor.commit();
				}
				catch ( Exception e )
				{
					e.printStackTrace();
				}
			}
				break;

			// Pen password change success response
			case PenMsgType.PASSWORD_SETUP_SUCCESS:
			{
				if ( curPass != newPass )
				{
					curPass = newPass;
				}
			}
				break;

			// Pen password change fails, the response
			case PenMsgType.PASSWORD_SETUP_FAILURE:
			{
				if ( curPass != newPass )
				{
					newPass = curPass;
				}
			}
			break;
		}

		sendPenMsgByBroadcast( penMsg );
	}

	private void sendPenMsgByBroadcast( PenMsg penMsg )
	{
		Intent i = new Intent( Const.Broadcast.ACTION_PEN_MESSAGE );
		i.putExtra( Const.Broadcast.MESSAGE_TYPE, penMsg.getPenMsgType() );
		i.putExtra( Const.Broadcast.CONTENT, penMsg.getContent() );

		context.sendBroadcast( i );
	}

	private void sendPenDotByBroadcast( int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color )
	{
		Intent i = new Intent( Const.Broadcast.ACTION_PEN_DOT );
		i.putExtra( Const.Broadcast.SECTION_ID, sectionId );
		i.putExtra( Const.Broadcast.OWNER_ID, ownerId );
		i.putExtra( Const.Broadcast.NOTE_ID, noteId );
		i.putExtra( Const.Broadcast.PAGE_ID, pageId );
		i.putExtra( Const.Broadcast.X, x );
		i.putExtra( Const.Broadcast.Y, y );
		i.putExtra( Const.Broadcast.FX, fx );
		i.putExtra( Const.Broadcast.FY, fy );
		i.putExtra( Const.Broadcast.PRESSURE, pressure );
		i.putExtra( Const.Broadcast.TIMESTAMP, timestamp );
		i.putExtra( Const.Broadcast.TYPE, type );
		i.putExtra( Const.Broadcast.COLOR, color );

		context.sendBroadcast( i );
	}
}
