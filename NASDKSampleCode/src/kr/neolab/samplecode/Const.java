package kr.neolab.samplecode;

public class Const
{
	public class Setting 
	{
		public final static String KEY_PASSWORD = "password";
		public final static String KEY_PEN_COLOR = "pen_color";
		public final static String KEY_AUTO_POWER_ON = "auto_power_onoff";
		public final static String KEY_ACCELERATION_SENSOR = "acceleration_sensor_onoff";
		public final static String KEY_BEEP = "beep_onoff";
		public final static String KEY_AUTO_POWER_OFF_TIME = "auto_power_off_time";
		public final static String KEY_SENSITIVITY = "sensitivity";
	}
	
	public class JsonTag
	{
		public final static String STRING_PROTOCOL_VERSION = "protocol_version";
		public final static String INT_TIMEZONE_OFFSET = "timezone";
		public final static String LONG_TIMETICK = "timetick";
		public final static String INT_MAX_FORCE = "force_max";
		public final static String INT_BATTERY_STATUS = "battery";
		public final static String INT_MEMORY_STATUS = "used_memory";
		public final static String INT_PEN_COLOR = "pen_tip_color";
		public final static String BOOL_AUTO_POWER_ON = "auto_power_onoff";
		public final static String BOOL_ACCELERATION_SENSOR = "acceleration_sensor_onoff";
		public final static String BOOL_HOVER = "hover_mode";
		public final static String BOOL_BEEP = "beep";
		public final static String INT_AUTO_POWER_OFF_TIME = "auto_power_off_time";
		public final static String INT_PEN_SENSITIVITY = "sensitivity";
		
		public final static String INT_TOTAL_SIZE = "total_size";
		public final static String INT_SENT_SIZE = "sent_size";
		public final static String INT_RECEIVED_SIZE = "received_size";
		
		public final static String INT_SECTION_ID = "section_id";
		public final static String INT_OWNER_ID = "owner_id";
		public final static String INT_NOTE_ID = "note_id";
		public final static String INT_PAGE_ID = "page_id";
		public final static String STRING_FILE_PATH = "file_path";
		
		public final static String INT_PASSWORD_RETRY_COUNT = "retry_count";
		public final static String INT_PASSWORD_RESET_COUNT = "reset_count";
		
		public final static String BOOL_RESULT = "result";
	}
	
	public class Broadcast
	{
		public static final String ACTION_PEN_MESSAGE = "action_pen_message";
		public static final String MESSAGE_TYPE = "message_type";
		public static final String CONTENT = "content";
		
		public static final String ACTION_PEN_DOT = "action_pen_dot";
		public static final String SECTION_ID = "sectionId";
		public static final String OWNER_ID = "ownerId";
		public static final String NOTE_ID = "noteId";
		public static final String PAGE_ID = "pageId";
		public static final String X = "x";
		public static final String Y = "y";
		public static final String FX = "fx";
		public static final String FY = "fy";
		public static final String PRESSURE = "pressure";
		public static final String TIMESTAMP = "timestamp";
		public static final String TYPE = "type";
		public static final String COLOR = "color";
		
		public static final String ACTION_PEN_UPDATE = "action_firmware_update";
	}
}
