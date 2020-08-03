package com.sty.tensorflow.pornidentifier.utils;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

public class ToastUtil {
    private static Toast toast = null;

    public static void show(Context context, String text) {
        try {
            if(toast != null) {
                toast.setText(text);
            }else {
                toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            }
            toast.show();
        }catch (Exception e) {
            //解决在子线程中调用Toast异常的情况处理
            Looper.prepare();
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }
}
