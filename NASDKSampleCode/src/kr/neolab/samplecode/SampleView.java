package kr.neolab.samplecode;

import java.util.ArrayList;

import kr.neolab.sdk.graphic.Renderer;
import kr.neolab.sdk.ink.structure.Dot;
import kr.neolab.sdk.ink.structure.DotType;
import kr.neolab.sdk.ink.structure.Stroke;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SampleView extends SurfaceView implements SurfaceHolder.Callback
{
	private SampleThread mSampleThread;

	// paper background
	private Bitmap background = null;

	// draw the strokes
	private ArrayList<Stroke> strokes = new ArrayList<Stroke>();

	private Stroke stroke = null;

	private int sectionId = 0, ownerId = 0, noteId = 0, pageId = 0;

	private float scale = 11, offsetX = 0, offsetY = 0;

	public SampleView( Context context )
	{
		super( context );

		getHolder().addCallback( this );
		mSampleThread = new SampleThread( this.getHolder(), this );
	}

	public void setPageSize( float width, float height )
	{
		if ( getWidth() <= 0 || getHeight() <= 0 || width <= 0 || height <= 0 )
		{
			return;
		}

		float width_ratio = getWidth() / width;
		float height_ratio = getHeight() / height;

		scale = Math.min( width_ratio, height_ratio );

		int docWidth = (int) (width * scale);
		int docHeight = (int) (height * scale);

		int mw = getWidth() - docWidth;
		int mh = getHeight() - docHeight;

		offsetX = mw / 2;
		offsetY = mh / 2;

		background = Bitmap.createBitmap( docWidth, docHeight, Bitmap.Config.ARGB_8888 );
		background.eraseColor( Color.parseColor( "#F9F9F9" ) );
	}

	@Override
	public void draw( Canvas canvas )
	{
		canvas.drawColor( Color.LTGRAY );

		if ( background != null )
		{
			canvas.drawBitmap( background, offsetX, offsetY, null );
		}

		if ( strokes != null && strokes.size() > 0 )
		{
			Renderer.draw( canvas, strokes.toArray( new Stroke[0] ), scale, offsetX, offsetY );
		}
	}

	@Override
	public void surfaceChanged( SurfaceHolder arg0, int arg1, int arg2, int arg3 )
	{
	}

	@Override
	public void surfaceCreated( SurfaceHolder arg0 )
	{
		mSampleThread = new SampleThread( getHolder(), this );
		mSampleThread.setRunning( true );
		mSampleThread.start();
	}

	@Override
	public void surfaceDestroyed( SurfaceHolder arg0 )
	{
		mSampleThread.setRunning( false );

		boolean retry = true;

		while ( retry )
		{
			try
			{
				mSampleThread.join();
				retry = false;
			}
			catch ( InterruptedException e )
			{
				e.getStackTrace();
			}
		}
	}

	public void addDot( int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int force, long timestamp, int type, int color )
	{
		if ( this.sectionId != sectionId || this.ownerId != ownerId || this.noteId != noteId || this.pageId != pageId )
		{
			strokes = new ArrayList<Stroke>();

			this.sectionId = sectionId;
			this.ownerId = ownerId;
			this.noteId = noteId;
			this.pageId = pageId;
		}

		if ( DotType.isPenActionDown( type ) || stroke == null || stroke.isReadOnly() )
		{
			stroke = new Stroke( sectionId, ownerId, noteId, pageId, color );
			strokes.add( stroke );
		}

		stroke.add( new Dot( x, y, fx, fy, force, type, timestamp ) );
	}

	public void addStrokes( Stroke[] strs )
	{
		for ( Stroke stroke : strs )
		{
			strokes.add( stroke );
		}
	}

	public class SampleThread extends Thread
	{
		private SurfaceHolder surfaceholder;
		private SampleView mSampleiView;
		private boolean running = false;

		public SampleThread( SurfaceHolder surfaceholder, SampleView mView )
		{
			this.surfaceholder = surfaceholder;
			this.mSampleiView = mView;
		}

		public void setRunning( boolean run )
		{
			running = run;
		}

		@Override
		public void run()
		{
			setName( "SampleThread" );

			Canvas mCanvas;

			while ( running )
			{
				mCanvas = null;

				try
				{
					mCanvas = surfaceholder.lockCanvas(); // lock canvas

					synchronized ( surfaceholder )
					{
						if ( mCanvas != null )
						{
							mSampleiView.draw( mCanvas );
						}
					}
				}
				finally
				{
					if ( mCanvas != null )
					{
						surfaceholder.unlockCanvasAndPost( mCanvas ); // unlock
																		// canvas
					}
				}
			}
		}
	}
}
