package kr.neolab.samplecode;

import java.io.File;
import java.lang.reflect.Field;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kr.neolab.samplecode.Const.Broadcast;
import kr.neolab.samplecode.Const.JsonTag;
import kr.neolab.sdk.ink.structure.Stroke;
import kr.neolab.sdk.pen.offline.OfflineFileParser;
import kr.neolab.sdk.pen.penmsg.PenMsgType;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewConfiguration;

public class MainActivity extends Activity
{
	public static final String TAG = "pensdk.sample";

	private static final int REQUEST_CONNECT_DEVICE_SECURE = 4;

	private PenClientCtrl penClientCtrl;

	private SampleView mSampleView;

	// Notification
	protected Builder mBuilder;
	protected NotificationManager mNotifyManager;
	protected Notification mNoti;
	
	public InputPasswordDialog inputPassDialog;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		setContentView( R.layout.activity_main );

		try
		{
			ViewConfiguration config = ViewConfiguration.get( this );
			Field menuKeyField = ViewConfiguration.class.getDeclaredField( "sHasPermanentMenuKey" );

			if ( menuKeyField != null )
			{
				menuKeyField.setAccessible( true );
				menuKeyField.setBoolean( config, false );
			}
		}
		catch ( Exception ex )
		{
			// Ignore
		}

		mSampleView = new SampleView( this );

		setContentView( mSampleView );
		
		PendingIntent pendingIntent = PendingIntent.getBroadcast( this, 0, new Intent( "firmware_update" ), PendingIntent.FLAG_UPDATE_CURRENT );
		 
		mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(getApplicationContext());
		mBuilder.setContentTitle( "Update Pen" );
		mBuilder.setSmallIcon( R.drawable.ic_launcher_n );
		mBuilder.setContentIntent( pendingIntent );   

		penClientCtrl = PenClientCtrl.getInstance( getApplicationContext() );
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		unregisterReceiver( mBroadcastReceiver );
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		IntentFilter filter = new IntentFilter( Broadcast.ACTION_PEN_MESSAGE );
		filter.addAction( Broadcast.ACTION_PEN_DOT );
		filter.addAction( "firmware_update" );
		
		registerReceiver( mBroadcastReceiver, filter );
	}

	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		switch ( requestCode )
		{
			case REQUEST_CONNECT_DEVICE_SECURE:
				// When DeviceListActivity returns with a device to connect
				if ( resultCode == Activity.RESULT_OK )
				{
					String address = null;

					if ( (address = data.getStringExtra( DeviceListActivity.EXTRA_DEVICE_ADDRESS )) != null )
					{
						penClientCtrl.connect( address );
					}
				}
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate( R.menu.main, menu );

		return super.onCreateOptionsMenu( menu );
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		// Handle presses on the action bar items
		switch ( item.getItemId() )
		{
			case R.id.action_setting:

				if ( penClientCtrl.isAuthorized() )
				{
					startActivity( new Intent( MainActivity.this, SettingActivity.class ) );
				}

				return true;

			case R.id.action_connect:

				if ( !penClientCtrl.isConnected() )
				{
					startActivityForResult( new Intent( MainActivity.this, DeviceListActivity.class ), 4 );
				}

				return true;

			case R.id.action_disconnect:

				if ( penClientCtrl.isConnected() )
				{
					penClientCtrl.disconnect();
				}

				return true;

			case R.id.action_offline_list:

				if ( penClientCtrl.isAuthorized() )
				{
					// to process saved offline data
					penClientCtrl.reqOfflineDataList();
				}

				return true;

			case R.id.action_upgrade:

				if ( penClientCtrl.isAuthorized() )
				{
					// location of firmware (you should locate file in this directory.)
					String pathFirmware = getExternalStoragePath() + "/neolab/firmware/NEO1.zip";

					// To request a firmware upgrade.
					penClientCtrl.upgradePen( new File( pathFirmware ) );
				}

				return true;

			case R.id.action_pen_status:

				if ( penClientCtrl.isAuthorized() )
				{
					// request connected to the current state of the pen provided.
					penClientCtrl.reqPenStatus();
				}

				return true;

			case R.id.action_offline:

				// to process saved offline data
				this.parseOfflineData();

				return true;

			default:
				return super.onOptionsItemSelected( item );
		}
	}

	public String getExternalStoragePath()
	{
		if ( Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED ) )
		{
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		else
		{
			return Environment.MEDIA_UNMOUNTED;
		}
	}

	private void handleDot( int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int force, long timestamp, int type, int color )
	{
		mSampleView.addDot( sectionId, ownerId, noteId, pageId, x, y, fx, fy, force, timestamp, type, color );
	}

	private void handleMsg( int penMsgType, String content )
	{
		Log.d( TAG, "handleMsg : " + penMsgType );

		switch ( penMsgType )
		{
			// Message of the attempt to connect a pen
			case PenMsgType.PEN_CONNECTION_TRY:

				Util.showToast( this, "try to connect." );

				break;

			// Pens when the connection is completed (state certification process is not yet in progress)
			case PenMsgType.PEN_CONNECTION_SUCCESS:

				Util.showToast( this, "connection is successful." );

				break;

			// Message when a connection attempt is unsuccessful pen
			case PenMsgType.PEN_CONNECTION_FAILURE:

				Util.showToast( this, "connection has failed." );

				break;

			// When you are connected and disconnected from the state pen
			case PenMsgType.PEN_DISCONNECTED:

				Util.showToast( this, "connection has been terminated." );

				break;

			// Pen transmits the state when the firmware update is processed.
			case PenMsgType.PEN_FW_UPGRADE_STATUS:
			{
				try
				{
					JSONObject job = new JSONObject( content );

					int total = job.getInt( JsonTag.INT_TOTAL_SIZE );
					int sent = job.getInt( JsonTag.INT_SENT_SIZE );

					this.onUpgrading( total, sent );

					Log.d( TAG, "pen fw upgrade status => total : " + total + ", progress : " + sent );
				}
				catch ( JSONException e )
				{
					e.printStackTrace();
				}
			}
				break;

			// Pen firmware update is complete
			case PenMsgType.PEN_FW_UPGRADE_SUCCESS:

				this.onUpgradeSuccess();

				Util.showToast( this, "file transfer is complete." );

				break;

			// Pen Firmware Update Fails
			case PenMsgType.PEN_FW_UPGRADE_FAILURE:

				this.onUpgradeFailure( false );

				Util.showToast( this, "file transfer has failed." );

				break;

			// When the pen stops randomly during the firmware update
			case PenMsgType.PEN_FW_UPGRADE_SUSPEND:

				this.onUpgradeFailure( true );

				Util.showToast( this, "file transfer is suspended." );

				break;

			// Offline Data List response of the pen
			case PenMsgType.OFFLINE_DATA_NOTE_LIST:

				try
				{
					JSONArray list = new JSONArray( content );

					for ( int i = 0; i < list.length(); i++ )
					{
						JSONObject jobj = list.getJSONObject( i );

						int sectionId = jobj.getInt( JsonTag.INT_SECTION_ID );
						int ownerId = jobj.getInt( JsonTag.INT_OWNER_ID );
						int noteId = jobj.getInt( JsonTag.INT_NOTE_ID );

						Log.d( TAG, "offline(" + (i + 1) + ") note => sectionId : " + sectionId + ", ownerId : " + ownerId + ", noteId : " + noteId );
					}
				}
				catch ( JSONException e )
				{
					e.printStackTrace();
				}

				// if you want to get offline data of pen, use this function.
				// you can call this function, after complete download.
				//
				// iPenCtrl.reqOfflineData( sectionId, ownerId, noteId );

				Util.showToast( this, "offline data list is received." );

				break;

			// Messages for offline data transfer begins
			case PenMsgType.OFFLINE_DATA_SEND_START:

				break;

			// Offline data transfer completion
			case PenMsgType.OFFLINE_DATA_SEND_SUCCESS:

				break;

			// Offline data transfer failure
			case PenMsgType.OFFLINE_DATA_SEND_FAILURE:

				break;

			// Progress of the data transfer process offline
			case PenMsgType.OFFLINE_DATA_SEND_STATUS:
			{
				try
				{
					JSONObject job = new JSONObject( content );

					int total = job.getInt( JsonTag.INT_TOTAL_SIZE );
					int received = job.getInt( JsonTag.INT_RECEIVED_SIZE );

					Log.d( TAG, "offline data send status => total : " + total + ", progress : " + received );
				}
				catch ( JSONException e )
				{
					e.printStackTrace();
				}
			}
				break;

			// When the file transfer process of the download offline
			case PenMsgType.OFFLINE_DATA_FILE_CREATED:
			{
				try
				{
					JSONObject job = new JSONObject( content );

					int sectionId = job.getInt( JsonTag.INT_SECTION_ID );
					int ownerId = job.getInt( JsonTag.INT_OWNER_ID );
					int noteId = job.getInt( JsonTag.INT_NOTE_ID );
					int pageId = job.getInt( JsonTag.INT_PAGE_ID );

					String filePath = job.getString( JsonTag.STRING_FILE_PATH );

					Log.d( TAG, "offline data file created => sectionId : " + sectionId + ", ownerId : " + ownerId + ", noteId : " + noteId + ", pageId : " + pageId + " filePath : " + filePath );
				}
				catch ( JSONException e )
				{
					e.printStackTrace();
				}
			}
				break;

			// Ask for your password in a message comes when the pen
			case PenMsgType.PASSWORD_REQUEST:
			{
				int retryCount = -1, resetCount = -1;

				try
				{
					JSONObject job = new JSONObject( content );

					retryCount = job.getInt( JsonTag.INT_PASSWORD_RETRY_COUNT );
					resetCount = job.getInt( JsonTag.INT_PASSWORD_RESET_COUNT );
				}
				catch ( JSONException e )
				{
					e.printStackTrace();
				}

				inputPassDialog = new InputPasswordDialog( this, this, retryCount, resetCount );
				inputPassDialog.show();
			}
				break;
		}
	}

	public void inputPassword( String password )
	{
		penClientCtrl.inputPassword( password );
	}

	private void onUpgrading( int total, int progress )
	{
		mBuilder.setContentText( "Sending" ).setProgress( total, progress, false );
		mNotifyManager.notify( 0, mBuilder.build() );
	}

	private void onUpgradeFailure( boolean isSuspend )
	{
		if ( isSuspend )
		{
			mBuilder.setContentText( "file transfer is suspended." ).setProgress( 0, 0, false );
		}
		else
		{
			mBuilder.setContentText( "file transfer has failed." ).setProgress( 0, 0, false );
		}
		mNotifyManager.notify( 0, mBuilder.build() );
	}

	private void onUpgradeSuccess()
	{
		mBuilder.setContentText( "The file transfer is complete." ).setProgress( 0, 0, false );
		mNotifyManager.notify( 0, mBuilder.build() );
	}

	private void parseOfflineData()
	{
		// obtain saved offline data file list
		String[] files = OfflineFileParser.getOfflineFiles();

		if ( files == null || files.length == 0 )
		{
			return;
		}

		for ( String file : files )
		{
			try
			{
				// create offline file parser instance
				OfflineFileParser parser = new OfflineFileParser( file );

				// parser return array of strokes
				Stroke[] strokes = parser.parse();

				if ( strokes != null )
				{
					mSampleView.addStrokes( strokes );
				}

				// delete data file
				parser.delete();
				parser = null;
			}
			catch ( Exception e )
			{
				Log.e( TAG, "parse file exeption occured.", e );
			}
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
			else if ( Broadcast.ACTION_PEN_DOT.equals( action ) )
			{
				int sectionId = intent.getIntExtra( Broadcast.SECTION_ID, 0 );
				int ownerId = intent.getIntExtra( Broadcast.OWNER_ID, 0 );
				int noteId = intent.getIntExtra( Broadcast.NOTE_ID, 0 );
				int pageId = intent.getIntExtra( Broadcast.PAGE_ID, 0 );
				int x = intent.getIntExtra( Broadcast.X, 0 );
				int y = intent.getIntExtra( Broadcast.Y, 0 );
				int fx = intent.getIntExtra( Broadcast.FX, 0 );
				int fy = intent.getIntExtra( Broadcast.FY, 0 );
				int force = intent.getIntExtra( Broadcast.PRESSURE, 0 );
				long timestamp = intent.getLongExtra( Broadcast.TIMESTAMP, 0 );
				int type = intent.getIntExtra( Broadcast.TYPE, 0 );
				int color = intent.getIntExtra( Broadcast.COLOR, 0 );

				handleDot( sectionId, ownerId, noteId, pageId, x, y, fx, fy, force, timestamp, type, color );
			}
			else if ( Broadcast.ACTION_PEN_DOT.equals( action ))
			{
				penClientCtrl.suspendPenUpgrade();
			}
		}
	};
}
