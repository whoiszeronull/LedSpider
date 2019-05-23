package com.hu.spider.led.panel;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.FileUtils;
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

	// spider operate mode, spider will act differently according to opeMode,
	// default mode is update the links sotred in db for missing files.
	private OpeMode opeMode;;

	// the thread pool for running spiders
	private static ExecutorService es;

	// the switch that determain whether to save the link and the html content
	// belongs to this link to the database which already exisitng in the database.
	// true: means to update(replace) the html content which already exsiting in the
	// databse.
	// false: means not to do anything, if the link and the content already
	// exisiting in the databse. Default is false.
//	private boolean update = true;

	private static LedLinkService lls = new LedLinkServiceImpl();

	public static final int MAX_LEVEL = 5;

	public LedPanelSpider(String link, int level, OpeMode opeMode, Set<String> alllinks) {
		super();
		this.link = link;
		this.curLevel = level;
		this.allLinks = alllinks;
		this.opeMode = opeMode;
	}

	public LedPanelSpider() {
	};

	@Override
	public void run() {

		switch (opeMode) {
		case CRAWL_UPDATE_ALL_LOGICALLY:
			crawlUpdateAllLogically();
			break;
		case CRAWL_UPDATE_MISSING_ONE:
			crawlUpdateMissingOne();
			break;
		case UPDATE_MISSING_ONE_ONLY:
			updateMissingOneOnly();
			break;
		default:
			System.out.println("Unknown operation mode!!");
		}

	}

	private void crawlUpdateAllLogically() {

		if (this.curLevel < 0 || this.curLevel > MAX_LEVEL) {
			System.out.println(
					"Crawl level out of bounds!! Limit is :[0-" + MAX_LEVEL + "]," + "current level=:" + curLevel);
			return;
		}

		try {

			this.html = HttpUtils.getHtml(this.link);

			if (curLevel + 1 < MAX_LEVEL) {
				this.spawn(this.link, this.html, curLevel + 1, this.allLinks);
			}

			if (verifyContent(html)) {
				if (isLinkExisting(link)) {
					updateExistingRecord(link, html);
				} else {
					insertLedLinkRecord(link, html);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void crawlUpdateMissingOne() {
	}

	private void updateMissingOneOnly() {
	}

	private boolean verifyContent(String html) {
		return TextVerifier.getInstance().verifiy(html);
	}

	// to check whether the link is existing in the dababase or not.
	private boolean isLinkExisting(String link) {
		// Check if there is such given link existing in the
		// database..
		LedLinkExample lle = new LedLinkExample();
		lle.createCriteria().andLinkEqualTo(link);
		return lls.countByExample(lle) > 0;
	}

	private void updateExistingRecord(String link, String html) {

		// store the html content and return the stored file path
		String filePath = storeContentToFile(link, html);

		LedLinkExample lle = new LedLinkExample();
		lle.createCriteria().andLinkEqualTo(link);

		// get the record based on the given existing link and update the record.
		LedLink record = lls.selectByExample(lle).get(0);

		String title = Jsoup.parse(html).getElementsByTag("title").text();
		record.setFileName(filePath);
		record.setTitle(title);

		lls.updateByExample(record, lle);

		System.out.println("updated existing record: " + record.toString());

	}

	// save the new records and update the existing records
	private void saveAndUpdate(String link, String html) {

		// Check if there is such given link existing in the
		// database..
		LedLinkExample lle = new LedLinkExample();
		lle.createCriteria().andLinkEqualTo(link);

		// if the count > 0, then it means this link already existing in the database.
		if (lls.countByExample(lle) > 0) {
			if (opeMode == OpeMode.UPDATE_OMIT) {
				// 测试用看用
				System.out.println("UPDATE disabled. record already existing in database, omited: " + link);
				return;
			} else {
				// get the record based on the given existing link and update the record.
				LedLink record = lls.selectByExample(lle).get(0);

				// saveStringToFile
				String filePath = generateFilePath(link);

				try {
					FileUtils.writeStringToFile(new File(filePath), html, "utf-16", false);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}

				String title = Jsoup.parse(html).getElementsByTag("title").text();
				record.setFileName(filePath);
				record.setTitle(title);

				lls.updateByExample(record, lle);
			}
		}

		// if the link and the content is not existing in the database then need
		// to be saved.
		insertLedLinkRecord(link, html);
	}

	// save the html content to a text file, and save the link info with
	// corresponding filename and title
	// info to the database. The dictory for saving files are:
	// hostShortName/linkLast(index). Here (index) means the number of the files
	// with same name linkLast. For example, if the link is :
	// https://www.abc.com/fantasy.html?id=10. Then the directory and file name will
	// be: products/abc/fantasy.html?id=10-YYMMdd
	private void insertLedLinkRecord(String link, String html) {

		String filePath = storeContentToFile(link, html);

		String title = Jsoup.parse(html).getElementsByTag("title").text();
		LedLink ledLink = new LedLink(link, filePath, title);
		lls.insert(ledLink);

		System.out.println("inserted record: " + ledLink.toString());

	}

	// store the html content to the file by using regular naming procedure and
	// return the file path
	private String storeContentToFile(String link, String html) {
		String filePath = generateFilePath(link);
		File text = new File(filePath);
		try {
			FileUtils.writeStringToFile(text, html, "utf-16");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return filePath;
	}

	private String addPostFixDate(String filePath) {
		SimpleDateFormat sdf = new SimpleDateFormat("-yyMMdd");
		String date = sdf.format(new Date());
		return filePath + date;
	}

	// The dictory for saving files are:
	// hostShortName/linkLast(index). Here (index) means the number of the files
	// with same name linkLast. For example, if the link is :
	// https://www.abc.com/fantasy.html?id=10. Then the directory and file name will
	// be: products/abc/fantasy.html?id=10-yyMMdd
	private String generateFilePath(String link) {
		String host;

		try {
			host = new URL(link).getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		String[] splits = link.split("/");
		String fileName = splits[splits.length - 1];
		return addPostFixDate("products/" + host + "/" + fileName);
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

//				new Thread(new LedPanelSpider(domainLink, curLevel, allLinks)).start();
				// put the new spider into the pool for running
				LedPanelSpider.getEs().execute(new LedPanelSpider(domainLink, curLevel, this.opeMode, allLinks));

			} else {

				// 测试看用
//				System.out.println(domainLink + " already existing.. Parsing omited~~~");
			}
		}

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

	public static ExecutorService getEs() {
		return es;
	}

	public static void setEs(ExecutorService es) {
		LedPanelSpider.es = es;
	}

	public OpeMode getOpeMode() {
		return opeMode;
	}

	public void setOpeMode(OpeMode opeMode) {
		this.opeMode = opeMode;
	}

}
