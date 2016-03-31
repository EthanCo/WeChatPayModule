package net.sourceforge.simcpux;

/**
 * TODO 在此处进行微信支付配置
 */
public class WeChatConfig {
	// APP_ID 替换为你的应用从官方网站申请到的合法appId
	//TODO 请同时修改  androidmanifest.xml里面，.PayActivityd里的属性<data android:scheme="wxb4ba3c02aa476ea1"/>为新设置的appid
	public static final String APP_ID = "";

	public static final String APP_SECRET = "";

	//商户号
	public static final String MCH_ID = "";

	//  API密钥，在商户平台设置 (商户平台-API安全中设置)
	public static final String API_KEY = ""; //若提示签名错误 就是此处设置错误

	public static class ShowMsgActivity {
		public static final String STitle = "showmsg_title";
		public static final String SMessage = "showmsg_message";
		public static final String BAThumbData = "showmsg_thumb_data";
	}
}
