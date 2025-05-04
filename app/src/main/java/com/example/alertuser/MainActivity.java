package com.example.alertuser;

import android.app.AppOpsManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    EditText timerEditText;
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerEditText = findViewById(R.id.editTextTimer);
        startButton = findViewById(R.id.startButton);

        startButton.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                return;
            }

            if (!hasUsageAccessPermission()) {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                return;
            }

            int minutes = Integer.parseInt(timerEditText.getText().toString());
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            prefs.edit().putInt("TIMER_MINUTES", minutes).apply();

            Intent intent = new Intent(this, UsageMonitorService.class);
            //intent.putExtra("TIMER_MINUTES", minutes);
            startForegroundService(intent);
        });
    }

    private boolean hasUsageAccessPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
}
