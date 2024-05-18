package com.example.mysensor;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

public class GyroscopeActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private Sensor accelerometerSensor;
    private TextView gyroData;
    private ImageView indicator;

    private float velocityX = 0;
    private float velocityY = 0;
    private float positionX = 0;
    private float positionY = 0;
    private final float FRICTION = 0.9f;
    private final float BOUNCE = 0.8f;
    private static final float MAX_VELOCITY = 10.0f;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyroscope);

        gyroData = findViewById(R.id.gyroData);
        indicator = findViewById(R.id.indicator);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (gyroscopeSensor != null) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            gyroData.setText("Gyroscope sensor not available");
        }

        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            gyroData.setText("Accelerometer sensor not available");
        }

        handler.post(updatePosition);
    }

    private Runnable updatePosition = new Runnable() {
        @Override
        public void run() {
            positionX += velocityX;
            positionY += velocityY;

            velocityX *= FRICTION;
            velocityY *= FRICTION;

            float halfWidth = indicator.getWidth() / 2f;
            float halfHeight = indicator.getHeight() / 2f;
            float screenWidth = getWidth();
            float screenHeight = getHeight();

            if (positionX < -halfWidth) {
                positionX = -halfWidth;
                velocityX = -velocityX * BOUNCE;
            } else if (positionX > screenWidth - halfWidth) {
                positionX = screenWidth - halfWidth;
                velocityX = -velocityX * BOUNCE;
            }
            if (positionY < -halfHeight) {
                positionY = -halfHeight;
                velocityY = -velocityY * BOUNCE;
            } else if (positionY > screenHeight - halfHeight) {
                positionY = screenHeight - halfHeight;
                velocityY = -velocityY * BOUNCE;
            }

            indicator.setTranslationX(positionX);
            indicator.setTranslationY(positionY);

            handler.postDelayed(this, 16);
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            String gyroText = "Gyroscope Data:\n" +
                    "x: " + x + "\n" +
                    "y: " + y + "\n" +
                    "z: " + z;
            gyroData.setText(gyroText);

            velocityX += x;
            velocityY += y;

            int color = Color.rgb((int) (Math.abs(z) * 255 / Math.PI), 64, 129);
            indicator.setColorFilter(color);
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float normalizedX = x / SensorManager.GRAVITY_EARTH;
            float normalizedY = y / SensorManager.GRAVITY_EARTH;

            velocityX = -normalizedX * MAX_VELOCITY;
            velocityY = normalizedY * MAX_VELOCITY;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            String accuracyMessage;
            switch (accuracy) {
                case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                    accuracyMessage = "Gyroscope accuracy is high";
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                    accuracyMessage = "Gyroscope accuracy is medium";
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                    accuracyMessage = "Gyroscope accuracy is low";
                    break;
                case SensorManager.SENSOR_STATUS_UNRELIABLE:
                    accuracyMessage = "Gyroscope accuracy is unreliable";
                    break;
                default:
                    accuracyMessage = "Gyroscope accuracy is unknown";
                    break;
            }
            gyroData.setText(accuracyMessage);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(updatePosition);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        handler.post(updatePosition);
    }

    private int getWidth() {
        return findViewById(android.R.id.content).getWidth();
    }

    private int getHeight() {
        return findViewById(android.R.id.content).getHeight();
    }
}
