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
	public long countByExample(LedLinkExample example) {
		try(SqlSession openSession = sqlSessionFactory.openSession();){
			LedLinkMapper mapper = openSession.getMapper(LedLinkMapper.class);
			long count;
			count = mapper.countByExample(example);
			return count;
		}
	}

	@Override
	public int deleteByExample(LedLinkExample example) {
		try(SqlSession openSession = sqlSessionFactory.openSession();){
			LedLinkMapper mapper = openSession.getMapper(LedLinkMapper.class);
			int count;
			count = mapper.deleteByExample(example);
			return count;
		}
	}

	@Override
	public int deleteByPrimaryKey(Integer id) {
		try(SqlSession openSession = sqlSessionFactory.openSession();){
			LedLinkMapper mapper = openSession.getMapper(LedLinkMapper.class);
			int count;
			count = mapper.deleteByPrimaryKey(id);
			return count;
		}
	}

	@Override
	public int insert(LedLink record) {
		try(SqlSession openSession = sqlSessionFactory.openSession();){
			LedLinkMapper mapper = openSession.getMapper(LedLinkMapper.class);
			int count;
			count = mapper.insert(record);
			openSession.commit();
			return count;
		}
	}

	@Override
	public int insertSelective(LedLink record) {
		try(SqlSession openSession = sqlSessionFactory.openSession();){
			LedLinkMapper mapper = openSession.getMapper(LedLinkMapper.class);
			int count;
			count = mapper.insertSelective(record);
			return count;
		}
	}

	@Override
	public List<LedLink> selectByExample(LedLinkExample example) {
		try(SqlSession openSession = sqlSessionFactory.openSession();){
			LedLinkMapper mapper = openSession.getMapper(LedLinkMapper.class);
			return mapper.selectByExample(example);
		}
	}

	@Override
	public LedLink selectByPrimaryKey(Integer id) {
		try(SqlSession openSession = sqlSessionFactory.openSession();){
			LedLinkMapper mapper = openSession.getMapper(LedLinkMapper.class);
			return mapper.selectByPrimaryKey(id);
		}
	}

	@Override
	public int updateByExampleSelective(LedLink record, LedLinkExample example) {
		try(SqlSession openSession = sqlSessionFactory.openSession();){
			LedLinkMapper mapper = openSession.getMapper(LedLinkMapper.class);
			int count;
			count = mapper.updateByExampleSelective(record, example);
			return count;
		}
	}

	@Override
	public int updateByExample(LedLink record, LedLinkExample example) {
		try(SqlSession openSession = sqlSessionFactory.openSession();){
			LedLinkMapper mapper = openSession.getMapper(LedLinkMapper.class);
			int count;
			count = mapper.updateByExample(record, example);
			return count;
		}
	}

	@Override
	public int updateByPrimaryKeySelective(LedLink record) {
		try(SqlSession openSession = sqlSessionFactory.openSession();){
			LedLinkMapper mapper = openSession.getMapper(LedLinkMapper.class);
			int count;
			count = mapper.updateByPrimaryKeySelective(record);
			return count;
		}
	}

	@Override
	public int updateByPrimaryKey(LedLink record) {
		try(SqlSession openSession = sqlSessionFactory.openSession();){
			LedLinkMapper mapper = openSession.getMapper(LedLinkMapper.class);
			int count;
			count = mapper.updateByPrimaryKey(record);
			return count;
		}
	}

}
