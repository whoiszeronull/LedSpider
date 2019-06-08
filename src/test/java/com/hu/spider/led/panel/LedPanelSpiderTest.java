package com.hu.spider.led.panel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.platform.commons.util.StringUtils;

import com.hu.utils.httputils.HttpUtils;

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
	private static String root = "http://www.newstar-led.com/";
//	private static String root = "http://www.newstar-led.com/p8-smd-led-screen";
//	private static String root = "http://www.liantronics.com/index.html";
//	private static String root = "http://www.vteam-lighting.cn/"; // not succeed.
//	private static String root = "http://www.vteam-lighting.cn/ecodot.html";
//	private static String root = "http://www.nexnovo.com/";
//	private static String root = "https://infiled.com/";

	private static int level = 0;
	private static Set<String> allLinks = Collections.synchronizedSet(new HashSet<>());

	// 要用mian 方法才能运行起来，不能用JUNIT测试方法。
	public static void main(String[] args) throws IOException {

		ThreadPoolExecutor exe = (ThreadPoolExecutor) Executors.newFixedThreadPool(200);
		LedPanelSpider.setEs(exe);

		Files.lines(Paths.get("google search results/respondingOnes-190530.txt"))
				.map(e -> new LedPanelSpider(e, level, OpeMode.CRAWL_UPDATE_MISSING_ONE, allLinks))
				.forEach(exe::execute);
//		.forEach(System.out::println);

		/*
		 * //bellow for single spider test. LedPanelSpider lps = new
		 * LedPanelSpider(root, level, OpeMode.CRAWL_UPDATE_MISSING_ONE, allLinks);
		 * ThreadPoolExecutor exe = (ThreadPoolExecutor)
		 * Executors.newFixedThreadPool(200); LedPanelSpider.setEs(exe);
		 * LedPanelSpider.getEs().execute(lps);
		 */

	}

//	@Test
	// 从给定的文件夹里面读取文本文件，然后提取每一行的数据，检查是否是正常的URL，然后如果是的话保存起来并去重.
	public void saveLinksFromFilesToASingleTXT() {

		Set<String> mergeURLs = Collections.synchronizedSet(new HashSet<String>());
		Set<String> failedURLs = Collections.synchronizedSet(new HashSet<String>());
		File txtFiles = new File("D:\\Google search results");

		ThreadPoolExecutor exe = (ThreadPoolExecutor) Executors.newCachedThreadPool();

		if (txtFiles.exists() && txtFiles.isDirectory()) {
			File[] files = txtFiles.listFiles();

			for (File file : files) {
				exe.submit(new Runnable() {
					@Override
					public void run() {
						if (file.getName().endsWith(".txt") && file.getName().startsWith("Google-")) {
							System.out.println("processing file: " + file);
							List<String> lines;
							try {
								lines = FileUtils.readLines(file, "UTF-8");
							} catch (IOException e) {
								e.printStackTrace();
								throw new RuntimeException(e);
							}
							System.out.println("Total lines: " + lines.size());

							for (String url : lines) {
								exe.submit(new Runnable() {
									@Override
									public void run() {
										try {
											if (HttpUtils.verifyURL(url)) {
												mergeURLs.add(url);
											}
										} catch (IOException e) {
											try {
												if (HttpUtils.verifyURL("http://" + url)) {
													mergeURLs.add("http://" + url);
												}
											} catch (Exception e1) {
												try {
													if (HttpUtils.verifyURL("https://" + url)) {
														mergeURLs.add("https://" + url);
													}
												} catch (IOException e2) {
													failedURLs.add(url);
												}

											}
										}
									}
								});

							}

						}

					}
				});

			}
		}

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		while (true) {
			if (exe.getActiveCount() == 0)
				break;
		}

		StringBuilder sb = new StringBuilder();
		for (String url : mergeURLs) {
			sb.append(url + System.lineSeparator());
		}
		try {
			FileUtils.writeStringToFile(new File("D:\\Google search results\\0-total merged OK results.txt"),
					sb.toString(), "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}

		StringBuilder sb2 = new StringBuilder();
		for (String url : failedURLs) {
			sb.append(url + System.lineSeparator());
		}
		try {
			FileUtils.writeStringToFile(new File("D:\\Google search results\\1-failed results.txt"), sb2.toString(),
					"utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

//	@Test
	public void readfromsinglefile() throws InterruptedException {

		Set<String> mergeURLs = Collections.synchronizedSet(new HashSet<String>());
		Set<String> failedURLs = Collections.synchronizedSet(new HashSet<String>());
		File txtFiles = new File("D:\\Google search results");

		ThreadPoolExecutor exe = (ThreadPoolExecutor) Executors.newCachedThreadPool();

		File file = new File("D:\\Google search results\\Google-LED Display.txt");
		exe.submit(new Runnable() {
			@Override
			public void run() {
				if (file.getName().endsWith(".txt") && file.getName().startsWith("Google-")) {
					System.out.println("processing file: " + file);
					List<String> lines;
					try {
						lines = FileUtils.readLines(file, "UTF-8");
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
					System.out.println("Total lines: " + lines.size());

					for (String url : lines) {
						exe.submit(new Runnable() {
							@Override
							public void run() {
								try {
									if (HttpUtils.verifyURL(url)) {
										mergeURLs.add(url);
										System.out.println("ok: " + url);
									}
								} catch (IOException e) {
									try {
										if (HttpUtils.verifyURL("http://" + url)) {
											mergeURLs.add("http://" + url);
											System.out.println("ok: http://" + url);
										}
									} catch (Exception e1) {
										try {
											if (HttpUtils.verifyURL("https://" + url)) {
												mergeURLs.add("https://" + url);
												System.out.println("ok: https://" + url);
											}
										} catch (IOException e2) {
											failedURLs.add(url);
											System.out.println("Failed--- " + url);
										}

									}
								}
							}
						});

					}

				}

			}
		});

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		System.out.println("test thread going to while cycle");
		while (true) {
			if (exe.getActiveCount() == 0 || exe.awaitTermination(15, TimeUnit.MINUTES))
				break;

		}

		StringBuilder sb = new StringBuilder();
		for (String url : mergeURLs) {
			sb.append(url + System.lineSeparator());
		}
		try {
			FileUtils.writeStringToFile(new File("D:\\Google search results\\0-total merged OK results.txt"),
					sb.toString(), "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}

		StringBuilder sb2 = new StringBuilder();
		for (String url : failedURLs) {
			sb.append(url + System.lineSeparator());
		}
		try {
			FileUtils.writeStringToFile(new File("D:\\Google search results\\1-failed results.txt"), sb2.toString(),
					"utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	@Test
	public void mergeAllGoogleSearchResultsToOneTXT() throws IOException {
		Set<String> mergeURLs = new HashSet<String>();
		File dir = new File("D:\\Google search results");
		String[] list = dir.list();

		System.out.println("total " + list.length + " files need to process!");
		for (String oneFile : list) {
			if (oneFile.endsWith(".txt") && oneFile.startsWith("Google-")) {
				List<String> urls;
				try {
					urls = FileUtils.readLines(new File(dir, oneFile), "utf-8");
					System.out.println(oneFile + " has lines : " + urls.size());
					mergeURLs.addAll(urls);
					System.out.println("after merging, current mergeURLs size: " + mergeURLs.size());
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}

		System.out.println("merge completed, total lines: " + mergeURLs.size());
		File savedFile = new File("D:\\Google search results\\1-merge to unqiue URLs.txt");
		System.out.println("going to save into the file: " + savedFile.toString());
		saveStringSetToFile(mergeURLs, savedFile);
	}

	private void saveStringSetToFile(Set<String> set, File file) throws IOException {

		Set<String> strs = set;
		StringBuilder sb = new StringBuilder();
		for (String url : strs) {
			sb.append(url.trim() + System.lineSeparator());
		}
		FileUtils.writeStringToFile(file, sb.toString(), "utf-8");

	}

	private void saveStringListToFile(List<String> list, File file) throws IOException {

		List<String> strs = list;
		StringBuilder sb = new StringBuilder();
		for (String url : strs) {
			sb.append(url.trim() + System.lineSeparator());
		}
		FileUtils.writeStringToFile(file, sb.toString(), "utf-8");

	}

	// read the urls and verify the urls to double check whether it is responding or
	// not, and save the good ones into one file, save the non-repsonding ones into
	// the other file
//	@Test
	public void verifyURLsAndSaveIntoTwoGoodAndBadTXT() throws IOException, InterruptedException {
		System.out.println("LedPanelSpiderTest.verifyURLsAndSaveIntoTwoGoodAndBadTXT()");
		Set<String> respondingURLs = Collections.synchronizedSet(new HashSet<String>());
		Set<String> non_respondingURLs = Collections.synchronizedSet(new HashSet<String>());

		File respondingOnes = new File("google search results/respondingOnes.txt");
		File nonRespondingOnes = new File("google search results/non-RespondingOnes.txt");

		File sourceFile = new File("google search results/All merged possible LED display candidate hosts-190530.txt");

		List<String> lines = FileUtils.readLines(sourceFile, "utf-8");

		ThreadPoolExecutor exe = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		for (String url : lines) {
			exe.submit(new Thread(new Runnable() {
				@Override
				public void run() {
					String result = HttpUtils.verifyAndReturnCompleteURLWithProtocal(url);
					if (StringUtils.isNotBlank(result)) {
						respondingURLs.add(result);
						System.out.println("OK: " + result);
					} else {
						non_respondingURLs.add(url);
						System.out.println("Not OK: " + url);
					}
				}
			}));
		}

		exe.submit(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("current active threads: " + exe.getActiveCount());
			}
		});

		System.out.println("waiting..........................");
		Thread.sleep(100000);

		System.out.println("work done!!");
		saveStringSetToFile(respondingURLs, respondingOnes);
		saveStringSetToFile(non_respondingURLs, nonRespondingOnes);
		System.out.println("total lines: " + lines.size());
		System.out.println("saved respondings ones: " + respondingURLs.size() + " into the file: "
				+ respondingOnes.getAbsolutePath());
		System.out.println("saved non-responding ones: " + non_respondingURLs.size() + " into the file: "
				+ nonRespondingOnes.getAbsolutePath());

	}
}
