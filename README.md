## 背景
前面文章中，我们入门了如何`hook`安卓的App，修改数据。现在我们开始实战。我们的目标选择了wx。

在不久之前，我使用过一个插件，可以修改wx的`钱包余额`，那么我们今天就亲自操刀，来试试我们之前学习的成果。

博客地址：
https://moonlightshadow.cn/article/850132640760594432
https://www.sunofbeach.net/a/1400440513093496833

>修改微信钱包的余额！

![image](https://images.sunofbeaches.com/content/2021_06_03/850104252905816064.jpg?imageMogr2/thumbnail)

## 我们的目标apk
首先，我明确自己的wx的版本。并且把apk分享给大家。
获取目标apk位置，我之前的文章有写，如何获得已经安装的app的安装包。


```java
//通过包名，查询地址
adb shell pm path com.tencent.mm
```


![image](https://images.sunofbeaches.com/content/2021_06_03/850061094826278912.png?imageMogr2/thumbnail)

![image](https://images.sunofbeaches.com/content/2021_06_03/850061359516221440.png?imageMogr2/thumbnail)

链接：https://pan.baidu.com/s/1D0c0xCeX3iBxQ496PMa3Hw 
提取码：9527 

这个是我们的包，如果想一起学习的可以下载这个包，这样和我的代码可以对得上。
wx：`8.02`好像是这个版本。

## 寻找目标
先打开我们wx，进入钱包页面。

![image](https://images.sunofbeaches.com/content/2021_06_03/850060463860350976.png?imageMogr2/thumbnail)

![image](https://images.sunofbeaches.com/content/2021_06_03/850104854935240704.jpg?imageMogr2/thumbnail)

哎呦，不要在意这些细姐啦。这个时候看看我们的调试工具，发现了这个页面的`activity`名字


```java
com.tencent.mm.plugin.mall.ui.MallIndexUiv2
```

先记录下来，可能以后要用得上呢。

把刚刚导出来的apk，丢进去反编译工具`jadx`中，打开完成之后。


![image](https://images.sunofbeaches.com/content/2021_06_03/850062725957550080.png?imageMogr2/thumbnail)

反编译开始，点击`搜索`按钮，全局搜索下钱包这个关键词看看。

![image](https://images.sunofbeaches.com/content/2021_06_03/850063044649156608.png?imageMogr2/thumbnail)


![image](https://images.sunofbeaches.com/content/2021_06_03/850106653142417408.jpg?imageMogr2/thumbnail)


搜索框中输入钱包，选择类，方法，变量，代码。


![image](https://images.sunofbeaches.com/content/2021_06_03/850063334236487680.png?imageMogr2/thumbnail)

再次尴尬。搜索不到嘛，不过难不到我，我换个姿势和你交流。
打开Android SDK的`ddms`工具，具体位置可以参考我的目录


```java
D:\Program\Android\sdk\tools\monitor.bat
双击它，就可以打开ddms了
```

打开之后看到我们的手机，然后点击灰绿色的手机图标，把整个页面的UI，dump出来。在右边可以看到预览图，把鼠标移动到钱包是余额哪里点击一下。右边可以看到view的层级，下边可以看到view的id。这个id就是我们需要找的目标了。通过文字我们无法找到，那么通过这个id应该可以找到。


![image](https://images.sunofbeaches.com/content/2021_06_03/850063900052291584.png?imageMogr2/thumbnail)

![image](https://images.sunofbeaches.com/content/2021_06_03/850063969451245568.png?imageMogr2/thumbnail)

我们找这2个控件的id名字。

![image](https://images.sunofbeaches.com/content/2021_06_03/850064587511300096.png?imageMogr2/thumbnail)

这不就是我们最熟悉的`findViewById`？？

进去看看情况。

![image](https://images.sunofbeaches.com/content/2021_06_03/850065209870516224.png?imageMogr2/thumbnail)

发现了这个控件的名字，既然是这样，我们就看看他如何`setText`，和我们平时写的代码一样的。继续看看其他可疑的地方吧。

![image](https://images.sunofbeaches.com/content/2021_06_03/850065562242383872.png?imageMogr2/thumbnail)

发现了方法，设置零钱。
是不是很开心？根据方法的名字，我们知道，这是设置显示零钱的，入参是一个`string`，好像是我们找的地方了。还有一个动画。当我们首次进入页面，会显示一个零钱在增加的动画。emmmm就是你了。

这个时候，长得帅的网友可能就要问了，嘶~~代码我看不懂啊，哦多茄

![image](https://images.sunofbeaches.com/content/2021_06_03/850109056562495488.jpg?imageMogr2/thumbnail)

这个时候你就需要学习Android入门的知识了。

[点击这里按F进入](https://www.sunofbeach.net/course)

![image](https://images.sunofbeaches.com/content/2021_06_03/850114459371307008.png?imageMogr2/thumbnail)

### 验证想法

...

One year later

....



假如你已经学习过Android相关的知识，现在就是你大展身手的时刻。

验证下是不是我们找到的这个地方。

我们开始hook这个方法。

进行hook三部曲

类地址
```java
class：com.tencent.mm.plugin.wallet_core.ui.view.WcPayMoneyLoadingView
```

方法名字


```java
method：setNewMoney(String str)
```


编写我们的hook代码。


```java
if ("com.tencent.mm".equals(app.packageName)) {
            Log.d(TAG, "hook: 微信");
            Class c = XposedHelpers.findClass("com.tencent.mm.plugin.wallet_core.ui.view.WcPayMoneyLoadingView", app.classLoader);
            if (c != null) {
                XposedHelpers.findAndHookMethod(c, "setNewMoney", String.class, new XC_MethodHook() {
                
                    //方法执行前，修改参数值
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        param.args[0] = "4298920.10";
                    }
                });
}
```
编译，安装插件，激活，重启手机。打开微信我的钱包。

 
![image](https://images.sunofbeaches.com/content/2021_06_03/850080157749215232.png?imageMogr2/thumbnail)

有趣~

![image](https://images.sunofbeaches.com/content/2021_06_03/850115537999495168.jpg?imageMogr2/thumbnail)


### 总结

通过这个例子，我们经历了很多波折，最终写下几行代码，完成了我们开始的需求。从这个过程中我们通过多个工具，相互配合，达到了修改零钱余额的效果。

看看我们都做了什么。

1. `xposed`环境
2. `jadx`反编译找关键文字
3. `ddms`找关键控件的id
4. 从反编译代码中找id
5. 定位id可能正确的位置
6. 需要看得懂java代码
7. 需要分析出在那个位置设置了我们的余额
8. 通过编写xposed插件，hook方法

---

上面需要的知识比较零散，都是我自己慢慢实践积累的。如果我需要整其他App，我也是可以的，前提是给一点点时间。
如果大伙们对这个有兴趣的话可以一起探讨。

---

现在xposed插件开发工程师在市面上也有不少岗位，而且薪资不错，比app应用层开发高，大家如果对这个感兴趣，可以考虑这个方向。

--- 

下一篇文章，就是hook长按选择文本了

![image](https://images.sunofbeaches.com/content/2021_06_03/850118931745079296.jpg?imageMogr2/thumbnail)

![image](https://images.sunofbeaches.com/content/2021_06_03/850119757007945728.jpg?imageMogr2/thumbnail)


>不提供成品，请勿做非法用途
