package com.sty.tensorflow.pornidentifier;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.sty.tensorflow.pornidentifier.utils.ToastUtil;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;

public class PornManager {
    private static final String TAG = PornManager.class.getSimpleName();
    public static boolean isInitialized = false;
    private Interpreter tflite;
    //与GPU中TensorFlow程序传值的通道
    private ByteBuffer imgData;
    //一张图像的像素数组
    private int[] intValues;

    private PornManager() {

    }

    private static class LazyHolder {
        private static PornManager instance = new PornManager();
    }


    public static PornManager getInstance() {
        return LazyHolder.instance;
    }

    public void init() {
        File file = new File(Environment.getExternalStorageDirectory() + "/sty/tensorflow/",
                "nsfw.tflite");
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        //加载模型
        tflite = new Interpreter(file, options);
        //获取到Python中定义的变量input，input为入口的意思
        //张量
        Tensor tensor = tflite.getInputTensor(tflite.getInputIndex("input"));
        String stringBuilder = "\n"
                + "dataType: " +
                tensor.dataType() +
                "\n" +
                "numBytes: " +
                tensor.numBytes() +
                "\n" +
                "shape: " +
                tensor.shape().length;
        Log.i(TAG, stringBuilder);
        //申请并清空内存
        imgData = ByteBuffer.allocateDirect(224 * 224 * 3 * 4);
        imgData.order(ByteOrder.LITTLE_ENDIAN);

        isInitialized = true;
    }

    public void run(Bitmap bitmap, Context context) {
        imgData.rewind(); //清空
        Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        intValues = new int[224 * 224];
        //bitmap --> int的数组
        scaleBitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224);
        //intValues --> 赋值给imgData
        for (int color : intValues) {
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);

            imgData.putFloat(b);
            imgData.putFloat(g);
            imgData.putFloat(r);
        }
        //最终获取的结果
        float[][] outArray = new float[1][2];
        //把程序传给GPU，然后GPU判断和执行
        tflite.run(imgData, outArray);
        //保留4位小数
        DecimalFormat df = new DecimalFormat("#0.0000");

        //outArray:入参出参对象
        //正常图像：outArray[0][0]
        //敏感图片：outArray[0][1]
        ToastUtil.show(context, "\n黄色图片：" + df.format(outArray[0][1])
                + "\n正常图片：" + df.format(outArray[0][0]));
    }

}
