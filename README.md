Strong Image View
=================

非常健壮的 Android 自定义 ImageView, 有效防止内存溢出异常;

受SmartImageView(http://loopj.com/android-smart-image-view/) 启发;
同时感谢滕飞同学的图片压缩代码;

// 先把测试图片放到SD卡中
adb push bg_3.png /data/data/cn.erhu.android.strong/cache/smart_web_image_cache/bg_3+png

Config.java:
  在配置中保存图片文件大小和压缩比例的关系;

StrongImage.java
  仅仅一个接口, 方法:public Bitmap getBitmap(Context _context);

WebImage.java
  implements StrongImage;
  从缓存中取图片数据;

WebImageCache.java
  图片缓存类;
  1. 缓存分二级: 内存缓存和文件缓存
  2. 内存缓存: 使用 ConcurrentHashMap<Strong, SoftReference<Bitmap>>;
  3. 文件缓存: 缓存在安装包的cache目录下, 程序卸载时, 自动被删除;
  4. 缓存策略: 先从内存中取数据, 若未取到, 
              则从SD卡中获取, 若从SD卡中取到, 则将图片加入内存, 返回bitmap;
                            若从SD卡中未取到, 则从网络下载, 保存到SD卡, 递归调用上述过程, 重试3次;
StrongImageView.java
  extends ImageView
  外部程序设置图片接口;

StrongImageTask.java
  异步显示图片