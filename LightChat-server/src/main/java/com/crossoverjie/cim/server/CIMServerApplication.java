package com.crossoverjie.LightChat.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.crossoverjie.LightChat.server.config.AppConfiguration;
import com.crossoverjie.LightChat.server.kit.RegistryZK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.net.InetAddress;

/**
 * @author crossoverJie
 */
@SpringBootApplication
public class LightChatServerApplication implements CommandLineRunner {

	private final static Logger LOGGER = LoggerFactory.getLogger(LightChatServerApplication.class);

	@Autowired
	private AppConfiguration appConfiguration;

	@Value("${server.port}")
	private int httpPort;

	public static void main(String[] args) {
		SpringApplication.run(LightChatServerApplication.class, args);
		LOGGER.info("Start LightChat server success!!!");
	}

	@Override
	public void run(String... args) throws Exception {
		//获得本机IP
//		String addr = InetAddress.getLocalHost().getHostAddress();
		String addr = "1.12.254.186";
		Thread thread = new Thread(new RegistryZK(addr, appConfiguration.getLightChatServerPort(), httpPort));
		thread.setName("registry-zk");
		thread.start();
	}

}