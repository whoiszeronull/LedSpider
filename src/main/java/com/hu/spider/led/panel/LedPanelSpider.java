package com.hu.spider.led.panel;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;

import com.hu.spider.led.service.LedLinkService;
import com.hu.spider.led.service.impl.LedLinkServiceImpl;
import com.hu.spider.led.vo.LedLink;
import com.hu.spider.led.vo.LedLinkExample;
import com.hu.utils.httputils.HttpUtils;

public class LedPanelSpider implements Runnable {

	private String link;
	private String html;
	private int curLevel;
	private Set<String> allLinks;

	private LedLinkService lls = new LedLinkServiceImpl();

	public static final int MAX_LEVEL = 5;

	@Override
	public void run() {
		if (StringUtils.isBlank(this.link)) {
			System.out.println("input link error!!");
			return;
		}

		if (this.curLevel < 0 || this.curLevel > 5) {
			System.out.println("input level error!!");
			return;
		}

		try {
			this.html = HttpUtils.getHtml(this.link);
			if(StringUtils.isNotBlank(this.html)) {
				// 1.varify the html content has the required product parameters information.
				
				// 2.If step one true, then save the info the the database.
				String title = Jsoup.parse(this.html).getElementsByTag("title").text();
				LedLink ledLink = new LedLink(this.link, UUID.randomUUID().toString(), title);
				System.out.println(ledLink.toString());
				lls.save(ledLink);
				this.spawn(this.link, this.html, curLevel, this.allLinks);
				
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} 
	}

	// based on the given host name to filter out the links in the given string
	// content, and spawn more spider threads.
	private void spawn(String host, String content, int curLevel, Set<String> allLinks)
			throws ClientProtocolException, IOException, InterruptedException {
		
		if (curLevel >= 0 && curLevel < LedPanelSpider.MAX_LEVEL) {
			Set<String> domainLinks = HttpUtils.filterDomainLinks(host, content);
			for (String domainLink : domainLinks) {
				if (!allLinks.contains(domainLink)) {
					synchronized (Object.class) {
						allLinks.add(domainLink);
					}
					// 休眠50 ms to comfort the host server?
					Thread.sleep(50);
					new Thread(new LedPanelSpider(domainLink, curLevel + 1, allLinks)).start();
				} else {
					System.out.println(domainLink + " already existing.. Parsing omited~~~");
				}
			}
		} else {
			System.out.println("level " + curLevel + " has exceed the bounds!");
		}

	}

	public LedPanelSpider(String link, int level, Set<String> alllinks) {
		super();
		this.link = link;
		this.curLevel = level;
		this.allLinks = alllinks;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public int getCurLevel() {
		return curLevel;
	}

	public void setCurLevel(int curLevel) {
		this.curLevel = curLevel;
	}

	public static int getMaxLevel() {
		return MAX_LEVEL;
	}

	public Set<String> getAllLinks() {
		return allLinks;
	}

	public void setAllLinks(Set<String> allLinks) {
		this.allLinks = allLinks;
	}

}
