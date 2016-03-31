package com.bocweb.cunyou;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import net.sourceforge.simcpux.WeChatConfig;
import net.sourceforge.simcpux.WeChatPayDirector;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String APP_ID = WeChatConfig.APP_ID;
    public static final String UNIFIED_ORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder";


    private IWXAPI api;

    private void regToWx() {
        api = WXAPIFactory.createWXAPI(this, APP_ID, true);
        api.registerApp(APP_ID);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnPay).setOnClickListener(this);

        regToWx();
        commitOrder();
    }

    private void commitOrder() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPay:
                WeChatPayDirector derictor = new WeChatPayDirector(this);
                derictor.pay(0.02F, "优乐美", "http://121.40.35.3/test", this);
                /*String text = "xxx";

                WXTextObject textObj = new WXTextObject();
                textObj.text = text;

                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = textObj;
                msg.description = text;

                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = String.valueOf(System.currentTimeMillis());
                req.message = msg;

                api.sendReq(req);*/
                break;
            default:
        }
    }
}
