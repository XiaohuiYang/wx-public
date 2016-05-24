package com.yxh.wx.crawler;

import java.util.Date;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.felix.scr.annotations.Reference;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.yxh.wx.cookie.CookiePool;

public class Crawler {
	
	@Reference
	CookiePool cookiePool;
	
	private Logger logger = LoggerFactory.getLogger(Crawler.class);
	
	private String apiRoot = "http://weixin.sogou.com";
	private String  userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36";
	private String userCookie = "CXID=B3EBF622BC23A4DD15784FC9617F7C36; SUID=52FC111B142D900A55B72DFB0004A20B; SUV=1439361586856051; pgv_pvi=2340838400; GOTO=Af99046; ssuid=2533552660; ABTEST=7|" + new Date().getTime() / 1000  + "|v1; weixinIndexVisited=1; sct=28; ld=Lkllllllll2Q1IgtlllllVbA1FwlllllpenAGyllllwllllljZlll5@@@@@@@@@@; ad=$lllllllll2qHhTElllllVboMpolllllpe4DUkllll9lllll9llll5@@@@@@@@@@; SNUID={SNUID}; IPLOC=CN4201";
	private Map<String, String> mockHeaders = Maps.newHashMap(ImmutableMap.<String, String>builder()
			.put("Host", "weixin.sogou.com")
			.put("User-Agent", userAgent)
			.build());
	
	private int skipPage = 0;
	private int totalPage = 10;
	private int interval = 10; // 60s
	
	public void startCrawler() {
		String snuid = cookiePool.nuid();
		logger.info("get SNUID from pool: {} ", snuid);
		mockHeaders.put("Cookie", userCookie.replace("{SNUID}", snuid));
		for (int i=1+skipPage; i<=totalPage; i++) {
			returnList(i);
			try {
				TimeUnit.SECONDS.sleep(interval);
			} catch (InterruptedException e) {
				logger.error("Faile to sleep");
			}
		}
	}

	private void returnList(int i) {
		String keyword = "nodejsss";
		String url = apiRoot + ("/weixin?query=${keyword}&sourceid=inttime_day&type=2&interation=&tsn=1&t=" + new Date().getTime()).replace("${keyword}", keyword);
		try {
			logger.info("request url : {}", url);
			HttpResponse<String> request = Unirest.get(url).headers(mockHeaders).asString();
			logger.info("retrieved body:{}", request.getBody());
			ensureResult(request.getBody());
			handleResutl(request.getBody());
		} catch (UnirestException e) {
			logger.error("", e);
		}

	}

	private void handleResutl(String body) {
		Elements els = Jsoup.parse(body).select(".wx-rb .txt-box");
		ListIterator<Element> iter = els.listIterator();
		while (iter.hasNext()) {
			Element e = iter.next();
			String title = getFirstElementText(e.select("h4 a"));
		    String link = e.select("h4 a").get(0).attr("href");
		    Element $weixinAccount = e.select(".s-p a#weixin_account").get(0);
		    String weixinAccountName = $weixinAccount.attr("title");
		    String weixinAccountLink = $weixinAccount.attr("href");
		    handleRedirectUrl(link);
		}
	}
	
	private void handleRedirectUrl(String url) {
		logger.info("request url : {}", url);
		HttpResponse<String> request;
		try {
			request = Unirest.get(url).headers(mockHeaders).asString();
		} catch (UnirestException e) {
			logger.error("Failed to get page {}", e.getMessage());
		}
		logger.info("retrieved body:{}", request.getBody());
	}

	private String getFirstElementText(Elements elements) {
		if (elements == null || elements.size() <= 0) {
			return "";
		}
		return elements.get(0).text();
	}
	
	private void ensureResult(String body) {
		if (body.indexOf("您的访问出错了") >= 0) {
			throw new RuntimeException("Reached list request limit.");
		  }
	}

}
