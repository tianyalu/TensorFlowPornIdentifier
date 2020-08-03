package com.sty.tensorflow.pornidentifier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.sty.tensorflow.pornidentifier.utils.PermissionUtils;

public class MainActivity extends AppCompatActivity {
    private String[] needPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private int[] data = {
            R.drawable.nsfw01,
            R.drawable.nsfw_test_02,
            R.drawable.nsfw_test_05,
            R.drawable.nsfw_test_10,
            R.drawable.nsfw_test_15,
            R.drawable.nsfw_test_16,
            R.drawable.nsfw_test_17,
            R.drawable.nsfw_test_50,
            R.drawable.timg
    };

    private ImageView imageView;
    private Button btnNext;
    private PornManager pornManager;
    private int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!PermissionUtils.checkPermissions(this, needPermissions)) {
            PermissionUtils.requestPermissions(this, needPermissions);
        }else if(!PornManager.isInitialized) {
            PornManager.getInstance().init();
        }
        initView();
    }

    private void initView() {
        imageView = findViewById(R.id.img);
        btnNext = findViewById(R.id.btn_next);
        pornManager = PornManager.getInstance();

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnNextClicked();
            }
        });
    }

    private void onBtnNextClicked() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), data[i++%9]);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                    }
                });
                if(!PornManager.isInitialized) {
                    PornManager.getInstance().init();
                }
                pornManager.run(bitmap, MainActivity.this);
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.REQUEST_PERMISSIONS_CODE) {
            if (!PermissionUtils.verifyPermissions(grantResults)) {
                PermissionUtils.showMissingPermissionDialog(this);
            } else if(!PornManager.isInitialized) {
                PornManager.getInstance().init();
            }
        }
    }
}
