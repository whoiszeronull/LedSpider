package com.hu.spider.led.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import com.hu.spider.led.vo.LedLink;
import com.hu.spider.led.vo.LedLinkExample;

public class LedLinkMapperTest {

	private static String resource;
	private static InputStream inputStream;
	private static SqlSessionFactory sqlSessionFactory;

	static {
		resource = "mybatis/mybatis-config.xml";
		try {
			inputStream = Resources.getResourceAsStream(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
	}

	
	@Test
	public void testUpdateByExample() {
		
		System.out.println("LedLinkMapperTest.testUpdateByExample()");
		
		try (SqlSession session = sqlSessionFactory.openSession();) {
			LedLinkMapper llm = session.getMapper(LedLinkMapper.class);
			
			LedLinkExample lle = new LedLinkExample();
			lle.createCriteria().andLinkEqualTo("http://www.mrled.cn/en/Product/57.html");
			
			LedLink ledLink = llm.selectByExample(lle).get(0);
			
			ledLink.setFileName("bbb.txt");
			ledLink.setTitle("thasdfa test title");
			
			System.out.println("ledLink: " + ledLink.toString());
			
			int d = llm.updateByExample(ledLink, lle);
			System.out.println(d);
			//对数据库改动的操作需要commit;
			session.commit();
		}
	}
	
//	@Test
	public void testUpdate1() {
		System.out.println("LedLinkMapperTest.testUpdate1()");
		try (SqlSession session = sqlSessionFactory.openSession();) {
			LedLinkMapper llm = session.getMapper(LedLinkMapper.class);
			LedLink ledLink = new LedLink();
			ledLink.setId(12);
			ledLink.setFileName("bbb.txt");
			ledLink.setTitle("thasdfa test title");
			ledLink.setLink("www.yaham.com");
			
			System.out.println(ledLink.toString());
			int d = llm.updateByPrimaryKey(ledLink);
			System.out.println(d);
			//对数据库改动的操作需要commit;
			session.commit();
		}
	}
	
//	@Test
	public void tertInsert1() {
		try (SqlSession session = sqlSessionFactory.openSession();) {
			LedLinkMapper llm = session.getMapper(LedLinkMapper.class);
			LedLink ledLink = new LedLink();
			ledLink.setFileName("bbb.txt");
			ledLink.setTitle("thasdfa test title");
			ledLink.setLink("www.creatled.com");
			
			System.out.println(ledLink.toString());
			
			llm.insert(ledLink);
			
			//对数据库改动的操作需要commit;
			session.commit();
		}
	}
	
//	@Test
	public void tertSelect() {
		try (SqlSession session = sqlSessionFactory.openSession();) {
			LedLinkMapper llm = session.getMapper(LedLinkMapper.class);
			LedLink ledLink = llm.selectByPrimaryKey(7);
			System.out.println(ledLink.toString());
			llm.insert(ledLink);
			
		}
	}
	
//	@Test
	public void testDelete() {
		System.out.println("LedLinkMapperTest.testDelete()");
		try (SqlSession session = sqlSessionFactory.openSession();) {
			LedLinkMapper llm = session.getMapper(LedLinkMapper.class);
			int d = llm.deleteByPrimaryKey(1);
			System.out.println(d);
			//对数据库改动的操作需要commit;
			session.commit();
		}
	}

}