package com.hu.spider.led.panel;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;

import com.hu.parser.ledparser.verifier.impl.TextVerifier;
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

	// the switch that determain whether to save the link and the html content
	// belongs to this link to the database which already exisitng in the database.
	// true: means to update(replace) the html content which already exsiting in the
	// databse.
	// false: means not to do anything, if the link and the content already
	// exisiting in the databse. Default is false.
	private boolean update = false;

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
			if (StringUtils.isNotBlank(this.html)) {
				// if the crawl depth belongs to [0~4], then keep crawling deeper.
				if (curLevel + 1 > 0 && curLevel + 1 < LedPanelSpider.MAX_LEVEL) {
					// get and filter for the links belong to the given host (or link's host)
					this.spawn(this.link, this.html, curLevel + 1, this.allLinks);
				}
				verifyAndExtract(this.link, this.html);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	// verify the given html content and extract and save the information if there
	// is.
	private void verifyAndExtract(String link, String html) {
		// 1.verify the html content has the required product parameters information.
		if (TextVerifier.getInstance().verifiy(html)) {
			// 2. If step one true, then check if there is such given link existing in the
			// database..
			LedLinkExample lle = new LedLinkExample();
			lle.createCriteria().andLinkEqualTo(link);
			// if the count > 0, then it means this link already existing in the database.
			if (lls.countByExample(lle) > 0) {
				if (this.update == false) {
					return;
				} else {
					
					//get the record based on the given existing link and update the record.
					LedLink record = lls.selectByExample(lle).get(0);
					// saveStringToFile
					String fileName = saveStringToFile(html);
					String title = Jsoup.parse(html).getElementsByTag("title").text();
					record.setFileName(fileName);
					record.setTitle(title);
					lls.updateByExample(record, lle);
				}
			}

			// otherwise the link and the content is not existing in the database which need
			// to be saved.
			save(link, html);
		}
	}

	// save the string to a file, and return the file name;
	private String saveStringToFile(String html) {
		
		//测试用下看看。
		return UUID.randomUUID().toString();
	}

	// save the html content to a text file, and save the link info with corresponding filename and title
	// info to the database.
	private void save(String link, String html) {
		// 1.save html content to a text file.
		String fileName = saveStringToFile(html);
		
		// 2.save LedLink info to the database.
		String title = Jsoup.parse(html).getElementsByTag("title").text();
		LedLink ledLink = new LedLink(link, fileName, title);
		lls.insert(ledLink);
	}

	// based on the given host name to filter out the links in the given string
	// content, and spawn more spider threads.
	private void spawn(String host, String content, int curLevel, Set<String> allLinks)
			throws ClientProtocolException, IOException, InterruptedException {
		Set<String> domainLinks = HttpUtils.filterDomainLinks(host, content);
		for (String domainLink : domainLinks) {
			if (!allLinks.contains(domainLink)) {
				synchronized (Object.class) {
					allLinks.add(domainLink);
				}
				// 休眠50 ms to comfort the host server?
				Thread.sleep(50);
				new Thread(new LedPanelSpider(domainLink, curLevel, allLinks)).start();
			} else {
				System.out.println(domainLink + " already existing.. Parsing omited~~~");
			}
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
