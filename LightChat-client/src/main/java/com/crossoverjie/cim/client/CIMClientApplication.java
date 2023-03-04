package com.crossoverjie.LightChat.client;

import com.crossoverjie.LightChat.client.scanner.Scan;
import com.crossoverjie.LightChat.client.service.impl.ClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author crossoverJie
 */
@SpringBootApplication
public class LightChatClientApplication implements CommandLineRunner{

	private final static Logger LOGGER = LoggerFactory.getLogger(LightChatClientApplication.class);

	@Autowired
	private ClientInfo clientInfo ;
	public static void main(String[] args) {
        SpringApplication.run(LightChatClientApplication.class, args);
		LOGGER.info("启动 Client 服务成功");
	}

	@Override
	public void run(String... args) throws Exception {
		Scan scan = new Scan() ;
		Thread thread = new Thread(scan);
		thread.setName("scan-thread");
		thread.start();
		clientInfo.saveStartDate();
	}
}