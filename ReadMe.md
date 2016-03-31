# WeChatPay Module #
对于微信支付封装的一个Module  

## 使用 ##

1. 申请微信支付获得APPID，设置MCH_ID(商户号)
2. 在微信商户平台中设置API_KEY (API密钥) (在商户平台-API安全中设置)
3. 设置包名(必须一致)  
4. 设置应用签名([使用签名工具生成](https://open.weixin.qq.com/zh_CN/htmledition/res/dev/download/sdk/Gen_Signature_Android.apk)，在手机上先装好正式版打包后的app，然后使用该签名工具生成)  
5. 添加此Module依赖
5. 在主app module 中，在app包名下新建wxapi文件夹，放入WXPayEntryActivity.java，该Activity会在支付完成后调用。 
6. 使用WeChatPayDirector类的pay() 进行支付  

## 注意事项 ##
send_music_thumb_backup.png 其实 为 jpg 格式 ，AS不能识别强制更改的格式，需改回为send_music_thumb_backup.jpg

## 其他 ##
> [官方文档](https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=8_5)  
> [Android微信支付彻底扫坑](http://www.eoeandroid.com/thread-918154-1-1.html?_dsign=88b40678)  