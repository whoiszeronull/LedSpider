package com.hu.spider.led.panel;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;

import com.hu.parser.ledparser.verifier.impl.TextVerifier;
import com.hu.spider.led.service.LedLinkService;
import com.hu.spider.led.service.impl.LedLinkServiceImpl;
import com.hu.spider.led.vo.LedLink;
import com.hu.spider.led.vo.LedLinkExample;
import com.hu.spider.led.vo.LedLinkExample.Criteria;
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
			try {
				crawlUpdateAllLogically();
			} catch (IOException | InterruptedException e1) {
				e1.printStackTrace();
				throw new RuntimeException(e1);
			}
			break;

		case CRAWL_UPDATE_MISSING_ONE:
			try {
				crawlUpdateMissingOne();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			break;

		case UPDATE_ALL_EXISTING_RECORDS:
			updateAllExistingRecords();
			break;
			
		case UPDATE_MISSING_ONE_ONLY:
			updateMissingOneOnly();
			break;

		default:
			System.out.println("Unknown operation mode!!");
		}

	}

	/**
	 * 1.scan the database for each ledLink record.
	 * 2.download the webpage file from the existing link;
	 * 3.store the file to the products/hostname/pageformatted name, and return the filepath.
	 * 4.update the existing ledLink record based on the new filename(filePath)
	 */
	private void updateAllExistingRecords() {
	}

	private void crawlUpdateAllLogically() throws ClientProtocolException, IOException, InterruptedException {

		ensureCrawlLevel();

		html = HttpUtils.getHtml(link);

		if (curLevel + 1 < MAX_LEVEL) {
			spawn(link, html, curLevel + 1, allLinks);
		}

		if (verifyContent(html)) {
			if (isLinkExisting(link)) {
				updateExistingRecordThruLink(link, html);
			} else {
				insertLedLinkRecord(link, html);
			}
		}

	}

	private void crawlUpdateMissingOne() throws ClientProtocolException, IOException, InterruptedException {

		ensureCrawlLevel();

		html = HttpUtils.getHtml(link);
		
		// 1.Get record from database based on the link info for checking.
		LedLink record;
		if ((record = getLedLinkRecord(link)) != null) {
			// 2.If the record existing, then check if the associated data file is existing
			// or
			// not, if not then download the webpage file and store the file and update the
			// ledLink .
			if (!(new File(record.getFileName()).exists())) {
				if(verifyContent(html)) {
					updateExistingRecordThruLink(link, html);
				}
			}
		} else {
			// 3.If the link record not existing, then download the data file and strored
			// and update the ledLink record.
			// store the html content and return the stored file path
			if(verifyContent(html)) {
				insertLedLinkRecord(link, html);
			}
		}

		if (curLevel + 1 < MAX_LEVEL) {
			spawn(link, html, curLevel + 1, allLinks);
		}
	}

	/**
	 * get the ledlink record from database if the record is found based on the
	 * given link info, other wise return null
	 * 
	 * @param link2
	 * @return
	 */
	private LedLink getLedLinkRecord(String link2) {
		LedLinkExample lle = new LedLinkExample();
		lle.createCriteria().andLinkEqualTo(link2);
		List<LedLink> list = lls.selectByExample(lle);

		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	private void updateMissingOneOnly() {
	}

	private boolean verifyContent(String html) {
		return TextVerifier.getInstance().verifiy(html);
	}

	// to check whether the link is existing in the dababase or not.
	private boolean isLinkExisting(String link2) {
		// Check if there is such given link existing in the
		// database..
		LedLinkExample lle = new LedLinkExample();
		lle.createCriteria().andLinkEqualTo(link2);
		return lls.countByExample(lle) > 0;
	}

	/**
	 * store the html content to a file and update the database existing ledlink
	 * record for the new file name;
	 * 
	 * @param link
	 * @param html
	 * @throws IOException
	 */
	private void updateExistingRecordThruLink(String link2, String html2) throws IOException {

		// store the html content and return the stored file path
		String filePath = storeContentToFile(link2, html2);

		LedLink record = getLedLinkRecord(link2);

		String title = Jsoup.parse(html2).getElementsByTag("title").text();
		record.setFileName(filePath);
		record.setTitle(title);

		System.out.println("going to update the existing record: " + record.toString());
		lls.updateByPrimaryKey(record);

		System.out.println("updated existing record: " + record.toString());

	}

//	

	/**
	 * save the html content to a text file and return a filePath as fileName for
	 * ledLink record, and save the link info with corresponding filename and title
	 * info to the database. The dictory for saving files are:
	 * hostName/linkLast-yyMMdd. For example, if the link is :
	 * https://www.abc.com/fantasy.html?id=10. Then the directory and file name will
	 * be: products/abc/fantasy.html?id=10-yyMMdd
	 */
	private void insertLedLinkRecord(String link, String html) throws IOException {

		String filePath = storeContentToFile(link, html);

		String title = Jsoup.parse(html).getElementsByTag("title").text();
		LedLink ledLink = new LedLink(link, filePath, title);
		
		System.out.println("going to insert new ledlink record: " + ledLink.toString());
		
		lls.insert(ledLink);

		System.out.println("inserted record: " + ledLink.toString());

	}

	// store the html content to the file by using regular naming procedure and
	// return the file path
	private String storeContentToFile(String link, String html) throws IOException {
		String filePath = generateFilePath(link);
		File text = new File(filePath);

		FileUtils.writeStringToFile(text, html, "utf-16");

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

	// to ensure the crawl level is within the limits from 0 to MAX_LEVEL which
	// pre-set in program.
	private void ensureCrawlLevel() {
		if (this.curLevel < 0 || this.curLevel > MAX_LEVEL) {
			System.out.println(
					"Crawl level out of bounds!! Limit is :[0-" + MAX_LEVEL + "]," + "current level=:" + curLevel);
			throw new RuntimeException("crawl level is out of bound[0~" + MAX_LEVEL + "]. Current level:" + curLevel);
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

	// save the new records and update the existing records
//		private void saveAndUpdate(String link, String html) {
	//
//			// Check if there is such given link existing in the
//			// database..
//			LedLinkExample lle = new LedLinkExample();
//			lle.createCriteria().andLinkEqualTo(link);
	//
//			// if the count > 0, then it means this link already existing in the database.
//			if (lls.countByExample(lle) > 0) {
//				if (opeMode == OpeMode.UPDATE_OMIT) {
//					// 测试用看用
//					System.out.println("UPDATE disabled. record already existing in database, omited: " + link);
//					return;
//				} else {
//					// get the record based on the given existing link and update the record.
//					LedLink record = lls.selectByExample(lle).get(0);
	//
//					// saveStringToFile
//					String filePath = generateFilePath(link);
	//
//					try {
//						FileUtils.writeStringToFile(new File(filePath), html, "utf-16", false);
//					} catch (IOException e) {
//						e.printStackTrace();
//						throw new RuntimeException(e);
//					}
	//
//					String title = Jsoup.parse(html).getElementsByTag("title").text();
//					record.setFileName(filePath);
//					record.setTitle(title);
	//
//					lls.updateByExample(record, lle);
//				}
//			}
	//
//			// if the link and the content is not existing in the database then need
//			// to be saved.
//			insertLedLinkRecord(link, html);
//		}

//	private void updateExistingRecordFile(LedLink record) throws ClientProtocolException, IOException {
//		String html2;
//		String link2 = record.getLink();
//
//		html2 = HttpUtils.getHtml(link2);
//
//		String filePath = storeContentToFile(link2, html2);
//		record.setFileName(filePath);
//
//		String title = Jsoup.parse(html2).getElementsByTag("title").text();
//		record.setFileName(filePath);
//		record.setTitle(title);
//
//		lls.updateByPrimaryKey(record);
//	}
//
//	private void insertLedLinkRecord(String link2) throws ClientProtocolException, IOException {
//		insertLedLinkRecord(link2, HttpUtils.getHtml(link2));
//	}

}
