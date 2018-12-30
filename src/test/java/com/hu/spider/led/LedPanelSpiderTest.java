package com.hu.spider.led;

import java.util.HashSet;
import java.util.Set;

import com.hu.spider.led.panel.LedPanelSpider;

public class LedPanelSpiderTest {

//	private static String root = "http://www.mrled.cn/en/index.html";
//	private static String root = "http://www.mrled.cn/fixed_led_display.html";
//	private static String root = "http://www.desayopto.cn/m5.html";
//	private static String root = "http://www.szlamp.cn/";
//	private static String root = "http://www.mrled.cn/en/Product/57.html";
	private static String root = "https://www.yes-led.com/en/displayproduct.html?proID=2614608&proTypeID=164392";
	
	
//	private static String root = "http://www.baidu.com";
	
	private static int level = 3;
	private static Set<String> allLinks = new HashSet<>();

	public static void main(String[] args) {
		LedPanelSpider lps = new LedPanelSpider(root, level, allLinks);
		new Thread(lps).start();
	}

}
