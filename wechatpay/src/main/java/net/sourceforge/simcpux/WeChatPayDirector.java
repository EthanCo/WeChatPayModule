package net.sourceforge.simcpux;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 调用该类pay()进行支付
 */
public class WeChatPayDirector {
    private Activity activity;
    private String price = "1"; //价格
    private String ipAddress = "127.0.0.1";
    private String notifyUrl = "http://121.40.35.3/test"; //通知URL
    private String body = "describe";

    public WeChatPayDirector(Activity activity) {
        this.activity = activity;
        msgApi = WXAPIFactory.createWXAPI(activity, null);

        req = new PayReq();
        sb = new StringBuffer();

        msgApi.registerApp(WeChatConfig.APP_ID);
    }

    /**
     * 付款-包括传入ip地址 - 未安装微信判断
     *
     * @param price     价格
     * @param describe  描述
     * @param notifyUrl 通知URL
     * @param context
     */
    public void pay(float price, String describe, String notifyUrl, Context context) {
        boolean isPaySupported = msgApi.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
        if (!isPaySupported) {
            this.ipAddress = Util.getIpAddress(context);
            Toast.makeText(context, "请先安装微信 ~", Toast.LENGTH_SHORT).show();
        } else {
            Log.i("Z-WeChatPayDirector", "ip: " + ipAddress);
            pay(price, describe, notifyUrl);
        }
    }

    /**
     * 付款
     *
     * @param price     价格
     * @param describe  描述
     * @param notifyUrl 通知URL
     */
    private void pay(float price, String describe, String notifyUrl) {
        price *= 100;
        this.price = String.valueOf(Math.round(price));
        this.body = describe;
        this.notifyUrl = notifyUrl;
        //生成prepay_id
        GetPrepayIdTask getPrepayId = new GetPrepayIdTask();
        getPrepayId.execute();

        String packageSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
    }

    private static final String TAG = "WeChatPayDirector";

    PayReq req;
    IWXAPI msgApi;
    //TextView show;
    Map<String, String> resultunifiedorder;
    StringBuffer sb;


    /**
     * 生成签名
     */

    private String genPackageSign(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append("key=");
        sb.append(WeChatConfig.API_KEY);


        String packageSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
        Log.e("orion", packageSign);
        return packageSign;
    }

    private String genAppSign(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append("key=");
        sb.append(WeChatConfig.API_KEY);

        this.sb.append("sign str\n" + sb.toString() + "\n\n");
        String appSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
        Log.e("orion", appSign);
        return appSign;
    }

    private String toXml(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        for (int i = 0; i < params.size(); i++) {
            sb.append("<" + params.get(i).getName() + ">");


            sb.append(params.get(i).getValue());
            sb.append("</" + params.get(i).getName() + ">");
        }
        sb.append("</xml>");

        Log.e("orion", sb.toString());
        return sb.toString();
    }

    private class GetPrepayIdTask extends AsyncTask<Void, Void, Map<String, String>> {

        private ProgressDialog dialog;


        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(activity, "提示", "正在获取预支付订单...");
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            if (dialog != null) {
                dialog.dismiss();
            }
            sb.append("prepay_id\n" + result.get("prepay_id") + "\n\n");
            //show.setText(sb.toString());
            Log.i("Z-GetPrepayIdTask", "onPostExecute : " + sb.toString());

            resultunifiedorder = result;

            //TODO
            genPayReq();
            sendPayReq();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Map<String, String> doInBackground(Void... params) {

            String url = String.format("https://api.mch.weixin.qq.com/pay/unifiedorder");
            String entity = genProductArgs();

            Log.e("orion", entity);

            byte[] buf = Util.httpPost(url, entity);

            String content = new String(buf);
            Log.e("orion", content);
            Map<String, String> xml = decodeXml(content);

            return xml;
        }
    }


    public Map<String, String> decodeXml(String content) {

        try {
            Map<String, String> xml = new HashMap<String, String>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {

                String nodeName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:

                        break;
                    case XmlPullParser.START_TAG:

                        if ("xml".equals(nodeName) == false) {
                            //实例化student对象
                            xml.put(nodeName, parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }

            return xml;
        } catch (Exception e) {
            Log.e("orion", e.toString());
        }
        return null;


    }


    private String genNonceStr() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }

    private long genTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }


    private String genOutTradNo() {
        Random random = new Random();
        SimpleDateFormat format = new SimpleDateFormat("yyMMddmmss");
        return MD5.getMessageDigest((format.format(new Date()) + String.valueOf(random.nextInt(999000)) + price).getBytes());
    }


    //
    private String genProductArgs() {
        StringBuffer xml = new StringBuffer();

        try {
            String nonceStr = genNonceStr();


            xml.append("</xml>");
            List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
           /* packageParams.add(new BasicNameValuePair("appid", Constants.APP_ID));
            packageParams.add(new BasicNameValuePair("body", "weixin"));
            packageParams.add(new BasicNameValuePair("mch_id", Constants.MCH_ID));
            packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));
            packageParams.add(new BasicNameValuePair("notify_url", "http://121.40.35.3/test"));
            packageParams.add(new BasicNameValuePair("out_trade_no", genOutTradNo()));
            packageParams.add(new BasicNameValuePair("spbill_create_ip", "127.0.0.1"));
            packageParams.add(new BasicNameValuePair("total_fee", "1"));
            packageParams.add(new BasicNameValuePair("trade_type", "APP"));*/

            packageParams.add(new BasicNameValuePair("appid", WeChatConfig.APP_ID));
            packageParams.add(new BasicNameValuePair("body", body));
            packageParams.add(new BasicNameValuePair("mch_id", WeChatConfig.MCH_ID));
            packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));
            packageParams.add(new BasicNameValuePair("notify_url", notifyUrl));
            packageParams.add(new BasicNameValuePair("out_trade_no", genOutTradNo()));
            packageParams.add(new BasicNameValuePair("spbill_create_ip", ipAddress));
            packageParams.add(new BasicNameValuePair("total_fee", price));
            packageParams.add(new BasicNameValuePair("trade_type", "APP"));


            String sign = genPackageSign(packageParams);
            packageParams.add(new BasicNameValuePair("sign", sign));


            String xmlstring = toXml(packageParams);

            return new String(xmlstring.toString().getBytes(), "ISO8859-1");
            //return xmlstring;

        } catch (Exception e) {
            Log.e(TAG, "genProductArgs fail, ex = " + e.getMessage());
            return null;
        }


    }

    private void genPayReq() {

        req.appId = WeChatConfig.APP_ID;
        req.partnerId = WeChatConfig.MCH_ID;
        req.prepayId = resultunifiedorder.get("prepay_id");
        req.packageValue = "Sign=WXPay";
        req.nonceStr = genNonceStr();
        req.timeStamp = String.valueOf(genTimeStamp());


        List<NameValuePair> signParams = new LinkedList<NameValuePair>();
        signParams.add(new BasicNameValuePair("appid", req.appId));
        signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
        signParams.add(new BasicNameValuePair("package", req.packageValue));
        signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
        signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
        signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));

        req.sign = genAppSign(signParams);

        sb.append("sign\n" + req.sign + "\n\n");

        //show.setText(sb.toString());
        Log.i("Z-WeChatPayDirector", "genPayReq : " + sb.toString());

        Log.e("orion", signParams.toString());

    }

    private void sendPayReq() {
        msgApi.registerApp(WeChatConfig.APP_ID);
        msgApi.sendReq(req);
        Log.i("Z-WeChatPayDirector", "sendPayReq : ");
    }
}

