package com.hu.spider.led.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.hu.spider.led.dao.LedLinkMapper;
import com.hu.spider.led.service.LedLinkService;
import com.hu.spider.led.vo.LedLink;
import com.hu.spider.led.vo.LedLinkExample;

public class LedLinkServiceImpl implements LedLinkService{

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
	
	@Override
	public void save(LedLink ledLink) {
		try(SqlSession openSession = sqlSessionFactory.openSession();){
			LedLinkMapper mapper = openSession.getMapper(LedLinkMapper.class);
			mapper.insert(ledLink);
			openSession.commit();
		}
	}

	@Override
	public long countByExample(LedLinkExample example) {
		return 0;
	}

	@Override
	public int deleteByExample(LedLinkExample example) {
		return 0;
	}

	@Override
	public int deleteByPrimaryKey(Integer id) {
		return 0;
	}

	@Override
	public int insert(LedLink record) {
		return 0;
	}

	@Override
	public int insertSelective(LedLink record) {
		return 0;
	}

	@Override
	public List<LedLink> selectByExample(LedLinkExample example) {
		return null;
	}

	@Override
	public LedLink selectByPrimaryKey(Integer id) {
		return null;
	}

	@Override
	public int updateByExampleSelective(LedLink record, LedLinkExample example) {
		return 0;
	}

	@Override
	public int updateByExample(LedLink record, LedLinkExample example) {
		return 0;
	}

	@Override
	public int updateByPrimaryKeySelective(LedLink record) {
		return 0;
	}

	@Override
	public int updateByPrimaryKey(LedLink record) {
		return 0;
	}

}
