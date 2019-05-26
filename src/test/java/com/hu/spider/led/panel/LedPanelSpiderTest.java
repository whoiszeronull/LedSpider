package com.hu.spider.led.panel;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

public class LedPanelSpiderTest {

//	private static String root = "http://www.desayopto.cn/m5.html";
//	private static String root = "http://www.szlamp.cn/";
//	private static String root = "http://47.99.14.21/ProductInfoCategory?categoryId=90828";
//	private static String root = "http://47.99.14.21/productinfo/324082.html";
//	private static String root = "http://www.mrled.cn/en/Product/57.html";
//	private static String root = "https://www.yes-led.com/en/displayproduct.html?proID=2614608&proTypeID=164392"; // not succeed.
//	private static String root = "https://www.barco.com/zh-CN/";
//	private static String root = "https://www.barco.com/en/"; //not succeed!
//	private static String root = "http://www.chainzone.com/";
//	private static String root = "http://www.univiewled.cn/"; //not succeed
//	private static String root = "http://www.univiewled.cn/pro_other-91.html";
//	private static String root = "http://www.sbcled.cn/products_detail/productId=618.html";
//	private static String root = "http://www.lighthouse-tech.com/";
//	private static String root = "http://www.lighthouse-tech.com/zh-CN/TechnicalArea/ProductDetail?p=Infinity"; // not succeed.
//	private static String root = "http://www.createled.cn/";
//	private static String root = "https://www.yaham.com/pro_list.html?#c28"; // not succeed.
//	private static String root = "http://www.newstar-led.com/";
	private static String root = "http://www.newstar-led.com/p8-smd-led-screen";
//	private static String root = "http://www.liantronics.com/index.html";
//	private static String root = "http://www.vteam-lighting.cn/"; // not succeed.
//	private static String root = "http://www.vteam-lighting.cn/ecodot.html";
//	private static String root = "http://www.nexnovo.com/";

	private static int level = 0;
	private static Set<String> allLinks = new HashSet<>();

	//要用mian 方法才能运行起来，不能用JUNIT测试方法。
	public static void main(String[] args) {
		
		LedPanelSpider lps = new LedPanelSpider(root, level,OpeMode.CRAWL_UPDATE_MISSING_ONE, allLinks);

		ExecutorService es = Executors.newCachedThreadPool();

		LedPanelSpider.setEs(es);
		LedPanelSpider.getEs().execute(lps);

	}

}
