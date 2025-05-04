package com.example.alertuser;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;

import java.time.LocalDate;
import java.util.Random;

public class OverlayService extends Service {

    private static WindowManager windowManager;
    private static View overlayView;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showPopup();
        return START_NOT_STICKY;
    }

    private void showPopup() {

        // Remove existing overlay before creating a new one
        removePopupSafely();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        overlayView = inflater.inflate(R.layout.popup_overlay, null);

        //overlayView = LayoutInflater.from(this).inflate(R.layout.popup_overlay, null);

        TextView message = overlayView.findViewById(R.id.messageText);
        TextView quote = overlayView.findViewById(R.id.quoteText);
        Button closeBtn = overlayView.findViewById(R.id.closeButton);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int count = prefs.getInt("count", 0) + 1;
        String currDate = String.valueOf(LocalDate.now());
        String prevDate = prefs.getString("prevDate", currDate) ;

        if (!prevDate.equals(currDate))
            prefs.edit().putInt("count", 0).apply();
        else
            prefs.edit().putInt("count", count).apply();

        prefs.edit().putString("prevDate", currDate).apply();

        quote.setText(getRandomQuote());
        message.setText("Limit exceeded " + count + " times");

        closeBtn.setOnClickListener(v -> {
            try {
                if (overlayView != null && windowManager != null) {
                    if (overlayView.getWindowToken() != null && overlayView.isAttachedToWindow()) {
                        windowManager.removeView(overlayView);
                    }
                    overlayView = null; // Clean up
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace(); // View not attached
            } catch (Exception e) {
                e.printStackTrace(); // Any other unexpected error
            }

            stopSelf();
        });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER;
        windowManager.addView(overlayView, params);
    }

    private void removePopupSafely() {
        try {
            if (overlayView != null && windowManager != null) {
                if (overlayView.isAttachedToWindow()) {
                    windowManager.removeView(overlayView);
                }
                overlayView = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getRandomQuote() {
        String[] quotes = {
                "Keep pushing forward.",
                "Discipline is the bridge between goals and accomplishment.",
                "Stay focused and never give up.",
                "Success is the sum of small efforts repeated.",
                "Dream big. Work hard. Stay humble."
        };
        return quotes[new Random().nextInt(quotes.length)];
    }

    @Override
    public void onDestroy() {
        removePopupSafely();
        super.onDestroy();
        /*try {
            if (windowManager != null && overlayView != null && overlayView.isAttachedToWindow()) {
                windowManager.removeView(overlayView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
