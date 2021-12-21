package com.fl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.fl.mybatispluginspringbootstarter.properties.FlMybatisProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = FlMybatisProperties.class)
@ActiveProfiles("test")
public class ServiceAppApplicationTests {

	@Autowired(required = false)
	private FlMybatisProperties flMybatisProperties;
	@Value(value = "${com.fl.mybatis.multiTenancy}")
	private String multiTenancy;

	@Test
	public void contextLoads() {
		assertNull(flMybatisProperties);
	}

	@Test
	public void getInfo4() {
		assertEquals("true", multiTenancy);

		System.out.println(multiTenancy);
	}

	@Test
	public void getInfo2() {
		assertEquals(true, flMybatisProperties.isMultiTenancy());

	}

	@Test
	public void getInfo3() {
		assertEquals(true, flMybatisProperties.isDyncQuery());
	}

}

