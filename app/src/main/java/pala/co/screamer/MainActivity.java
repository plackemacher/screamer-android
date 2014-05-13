package pala.co.screamer;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends Activity implements SensorEventListener {

	private static final int COLOR_HAPPY = Color.GREEN;
	private static final int COLOR_SCARED = Color.RED;

	private static final int[] SOUND_RESOURCE_IDS = new int[] {
			//R.raw.scream1,
			R.raw.scream2
	};

	private FrameLayout mFrameLayout;

	private SensorManager mSensorManager;
	private MediaPlayer mMediaPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mFrameLayout = (FrameLayout)findViewById(android.R.id.primary);
		mFrameLayout.setBackgroundColor(COLOR_HAPPY);

		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

		final Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if(sensor != null) {
			mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
		}

		createSound();
	}

	@Override
	protected void onDestroy() {
		mSensorManager.unregisterListener(this);

		destroySound();

		super.onDestroy();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		final float magnitude = calculateMagnitude(event.values);

		if(magnitude < 5.0) {
			mFrameLayout.setBackgroundColor(COLOR_SCARED);

			playSound();
		} else {
			mFrameLayout.setBackgroundColor(COLOR_HAPPY);

			stopSound();
		}
	}

	private float calculateMagnitude(float[] values) {
		if(values.length != 3) throw new RuntimeException("Expected 3 values!");

		return (float)Math.sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2]);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	private static int randomSoundResId() {
		final int index = new Random().nextInt(SOUND_RESOURCE_IDS.length);

		return SOUND_RESOURCE_IDS[index];
	}

	private void createSound() {
		mMediaPlayer = MediaPlayer.create(this, randomSoundResId());
	}

	private void destroySound() {
		mMediaPlayer.release();
	}

	private void playSound() {
		if(!mMediaPlayer.isPlaying()) {
			mMediaPlayer.setLooping(true);
			mMediaPlayer.start();
		}
	}

	private void stopSound() {
		if(mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
			AssetFileDescriptor afd = getResources().openRawResourceFd(randomSoundResId());

			mMediaPlayer.reset();
			try {
				mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
				afd.close();
				mMediaPlayer.prepare();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
}
