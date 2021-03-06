package com.earyant;

import com.earyant.config.WxCpDemoInMemoryConfigStorage;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.cp.api.*;
import me.chanjar.weixin.cp.bean.WxCpXmlMessage;
import me.chanjar.weixin.cp.bean.WxCpXmlOutMessage;
import me.chanjar.weixin.cp.bean.WxCpXmlOutTextMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@SpringBootApplication
public class QywechatApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(QywechatApplication.class, args);
		initWeixin();
	}


	private static WxCpConfigStorage wxCpConfigStorage;
	private static WxCpService wxCpService;
	private static WxCpMessageRouter wxCpMessageRouter;

	private static void initWeixin() throws IOException {
		try (InputStream is1 = ClassLoader
				.getSystemResourceAsStream("test-config.xml")) {
			WxCpDemoInMemoryConfigStorage config = WxCpDemoInMemoryConfigStorage
					.fromXml(is1);

			wxCpConfigStorage = config;
			wxCpService = new WxCpServiceImpl();
			wxCpService.setWxCpConfigStorage(config);

			WxCpMessageHandler handler = new WxCpMessageHandler() {
				@Override
				public WxCpXmlOutMessage handle(WxCpXmlMessage wxMessage,
												Map<String, Object> context, WxCpService wxService,
												WxSessionManager sessionManager) {
					WxCpXmlOutTextMessage m = WxCpXmlOutMessage.TEXT().content("测试加密消息")
							.fromUser(wxMessage.getToUserName())
							.toUser(wxMessage.getFromUserName()).build();
					return m;
				}
			};

			WxCpMessageHandler oauth2handler = new WxCpMessageHandler() {
				@Override
				public WxCpXmlOutMessage handle(WxCpXmlMessage wxMessage,
												Map<String, Object> context, WxCpService wxService,
												WxSessionManager sessionManager) {
					String href = "<a href=\""
							+ wxService.oauth2buildAuthorizationUrl(
							wxCpConfigStorage.getOauth2redirectUri(), null)
							+ "\">测试oauth2</a>";
					return WxCpXmlOutMessage.TEXT().content(href)
							.fromUser(wxMessage.getToUserName())
							.toUser(wxMessage.getFromUserName()).build();
				}
			};

			wxCpMessageRouter = new WxCpMessageRouter(wxCpService);
			wxCpMessageRouter.rule().async(false).content("哈哈") // 拦截内容为“哈哈”的消息
					.handler(handler).end().rule().async(false).content("oauth")
					.handler(oauth2handler).end();

		}
	}

}
