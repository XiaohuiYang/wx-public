package com.yxh.wx.cookie;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.felix.scr.annotations.Component;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.yxh.wx.utils.GlobalUtils;

@Component
public class CookiePool {
	private Logger logger = LoggerFactory.getLogger(CookiePool.class);
	
	private List<String> cookies = new ArrayList<String>();
	private int size = 1;
	Random r =  new Random();
	
	@Test
	public void getBatchCookies() {
		for (int i=0; i<size; i++) {
			String result = getNUID();
			if (result == null) {
				i--;
				continue;
			}
			cookies.add(result);
		}
 	}
	
	public String getNUID() {
		HttpResponse<String> request;
		try {
			HttpClient httpClient = HttpClients.custom()
				    .disableCookieManagement()
//				    .disableRedirectHandling()
				    .build();
			Unirest.setHttpClient(httpClient);
			request = Unirest.get("http://weixin.sogou.com/weixin?query=" + GlobalUtils.randomString(2)).header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36").asString();
			String SNUID =request.getHeaders().get("Set-Cookie").get(1).split(";")[0];
			SNUID = SNUID.indexOf("SNUID") != -1 ? SNUID.split("=")[1] : "";
			logger.info("SNUID: {}", SNUID);
			return SNUID;
		} catch (UnirestException e) {
			logger.error("get SNUID error {}", e.getMessage());
			return null;
		}
	}
	
	public String nuid() {
		int size = cookies.size();
		if (size == 0) {
			return null;
		}
		return cookies.get(r.nextInt(size));
	}

}
