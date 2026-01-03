package com.momoautoreply;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ACCESSIBILITY_SETTINGS = 1001;
    private static final int REQUEST_OVERLAY_PERMISSION = 1002;

    private TextView textViewStatus;
    private Button btnEnableAccessibility;
    private Button btnCheckServiceStatus;
    private Button btnSettings;
    private Button btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化UI组件
        initUI();

        // 设置按钮点击事件
        setButtonListeners();

        // 检查无障碍服务状态
        checkAccessibilityServiceStatus();

        // 检查并请求悬浮窗权限（如果需要）
        checkOverlayPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次回到主界面都检查服务状态
        checkAccessibilityServiceStatus();
    }

    /**
     * 初始化UI组件
     */
    private void initUI() {
        textViewStatus = findViewById(R.id.textView_status);
        btnEnableAccessibility = findViewById(R.id.btn_enable_accessibility);
        btnCheckServiceStatus = findViewById(R.id.btn_check_service_status);
        btnSettings = findViewById(R.id.btn_settings);
        btnExit = findViewById(R.id.btn_exit);
    }

    /**
     * 设置按钮点击事件
     */
    private void setButtonListeners() {
        // 开启无障碍服务按钮
        btnEnableAccessibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccessibilitySettings();
            }
        });

        // 检查服务状态按钮
        btnCheckServiceStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAccessibilityServiceStatus();
            }
        });

        // 设置按钮
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });

        // 退出按钮
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitApp();
            }
        });
    }

    /**
     * 检查无障碍服务状态
     */
    private void checkAccessibilityServiceStatus() {
        boolean isServiceEnabled = isAccessibilityServiceEnabled();
        updateServiceStatusUI(isServiceEnabled);
    }

    /**
     * 检查无障碍服务是否已开启
     * @return 是否开启
     */
    private boolean isAccessibilityServiceEnabled() {
        String serviceName = getPackageName() + "/" + MomoAccessibilityService.class.getName();
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED
            );
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );
            if (settingValue != null) {
                return settingValue.contains(serviceName);
            }
        }
        return false;
    }

    /**
     * 更新服务状态UI
     * @param isEnabled 服务是否开启
     */
    private void updateServiceStatusUI(boolean isEnabled) {
        if (isEnabled) {
            textViewStatus.setText(R.string.status_service_enabled);
            textViewStatus.setTextColor(getResources().getColor(R.color.success));
            btnEnableAccessibility.setEnabled(false);
            btnEnableAccessibility.setText(getString(R.string.status_service_enabled));
            btnEnableAccessibility.setBackgroundTintList(getResources().getColorStateList(R.color.success));
        } else {
            textViewStatus.setText(R.string.status_service_disabled);
            textViewStatus.setTextColor(getResources().getColor(R.color.error));
            btnEnableAccessibility.setEnabled(true);
            btnEnableAccessibility.setText(R.string.btn_enable_accessibility);
            btnEnableAccessibility.setBackgroundTintList(getResources().getColorStateList(R.color.primary));
            showEnableAccessibilityDialog();
        }
    }

    /**
     * 显示开启无障碍服务对话框
     */
    private void showEnableAccessibilityDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.msg_please_enable_accessibility)
                .setPositiveButton(R.string.btn_enable_accessibility, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openAccessibilitySettings();
                    }
                })
                .setNegativeButton(R.string.btn_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exitApp();
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * 打开无障碍服务设置页面
     */
    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivityForResult(intent, REQUEST_ACCESSIBILITY_SETTINGS);
    }

    /**
     * 打开应用设置页面
     */
    private void openSettings() {
        // 这里可以跳转到设置页面，目前暂时显示Toast
        Toast.makeText(this, "设置功能开发中...", Toast.LENGTH_SHORT).show();
    }

    /**
     * 退出应用
     */
    private void exitApp() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage("确定要退出应用吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 检查悬浮窗权限
     */
    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_ACCESSIBILITY_SETTINGS) {
            // 检查无障碍服务是否已开启
            boolean isEnabled = isAccessibilityServiceEnabled();
            updateServiceStatusUI(isEnabled);
            if (isEnabled) {
                Toast.makeText(this, R.string.msg_service_enabled_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.msg_service_enabled_failed, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "悬浮窗权限被拒绝，某些功能可能无法正常使用。", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 重启应用
     */
    private void restartApp() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}
