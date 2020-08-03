# 基于`TensorFlow`实现图片鉴黄

本文基于`TensorFlow`并通过雅虎的 [`open_nsfw`](https://github.com/yahoo/open_nsfw) 简单地实现`Android`上的图片鉴黄效果。

## 一、基础知识

### 1.1 `TensorFlow`简介

`TensorFlow`是一个软件库或框架，由`Google`团队设计，以最简单的方式实现机器学习和深度学习概念。他结合了优化技术的计算机代数，便于计算许多数学表达式。

`TensorFlow`有详细记录，包含大量机器学习库，并提供了一些重要的功能和方法。

`TensorFlow` 也是一个`Google`产品，它包括各种机器学习和深度学习算法。`TensorFlow`可以训练和运行深度神经网络，用于手写数字分类、图像识别和各种序列模型的创建。

### 1.2 `TensorFlow Lite`简介

现在越来越多的移动设备集成了定制硬件来更有效地处理机器学习带来的工作负载。`TensorFlow Lite`支持`Android`神经网络`API(Android Neural Networks API)`利用这些新的加速器硬件。当加速器硬件不可用的时候，`TensorFlow Lite`会执行优化`CPU`，这可以确保模型仍然可以很快地运行在一个大的设备上。

#### 1.2.1 `TensorFlow Lite`的特点

`TensorFlow Lite`特点如下：

> 1. 轻量级：允许在具有很小的二进制大小和快速初始化启动的机器学习模型设备上进行推理；
> 2. 跨平台：能够运行在许多不同的平台上，首先支持`Android`和`IOS`平台；
> 3. 快速：针对移动设备进行了优化，包括显著提高模型加载时间和支持硬件加速。

## 二、实现

先看一下实现效果吧：

![image](https://github.com/tianyalu/TensorFlowPornIdentifier/raw/master/show/show.png)

### 2.1 总体思路

图片鉴黄主要思路是通过`nsfw.tflite`模型文件生成`Interpreter`，然后通过`Interpreter`获取`python`中定义的入口`ByteBuffer`的张量（`Tensor`），然后把要鉴别的文件做归一化处理，输入到`ByteBuffer`中，通过运行`Interpreter`获取结果即可，其流程如下图所示：

![image](https://github.com/tianyalu/TensorFlowPornIdentifier/raw/master/show/tensorflow_picture_porn_process.png)

### 2.2 实现步骤

#### 2.2.1 添加依赖

在`build.gradle`中添加`TensorFlow`依赖：

```groovy
implementation 'org.tensorflow:tensorflow-lite:0.0.0-nightly'
implementation 'org.tensorflow:tensorflow-lite-gpu:0.0.0-nightly'
```

#### 2.2.2 拷贝训练模型文件

将`resources`目录下的`nsfw.tflite`文件拷贝到手机`SD`存储卡上。

#### 2.2.3 加载训练模型文件

```java
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
	//申请并清空内存
  imgData = ByteBuffer.allocateDirect(224 * 224 * 3 * 4);
  imgData.order(ByteOrder.LITTLE_ENDIAN);

  isInitialized = true;
}
```

#### 2.2.4 运行并获取检测结果

```java
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
```



