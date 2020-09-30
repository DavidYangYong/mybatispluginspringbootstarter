package com.fl;

import static org.junit.Assert.assertEquals;

import com.fl.mybatispluginspringbootstarter.properties.FlMybatisProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {FlMybatisProperties.class})
@TestPropertySource("classpath:application.yml")
public class ServiceAppApplicationTests {

	@Autowired
	private FlMybatisProperties flMybatisProperties;

	@Test
	public void contextLoads() {
	}

	@Test
	public void getInfo2() {
		assertEquals(true, flMybatisProperties.isEnabled());
	}

}

