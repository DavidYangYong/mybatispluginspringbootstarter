package com.fl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.fl.mybatispluginspringbootstarter.autoconfigure.FlMybatisPluginAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {FlMybatisPluginAutoConfiguration.class})
@TestPropertySource("classpath:application.yml")
public class ServiceConfigApplicationTests {

	@Autowired(required = false)
	private FlMybatisPluginAutoConfiguration flMybatisPluginAutoConfiguration;

	@Test
	public void contextLoads() {
	}

	@Test
	public void getInfo2() {
		assertNull(flMybatisPluginAutoConfiguration);
		assertEquals(null, flMybatisPluginAutoConfiguration.getFlMybatisProperties()
		);
	}

}

