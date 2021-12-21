package com.fl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fl.mybatispluginspringbootstarter.autoconfigure.FlMybatisPluginAutoConfiguration;
import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {FlMybatisPluginAutoConfiguration.class, PageHelperAutoConfiguration.class})
@ActiveProfiles("test")
public class ServiceConfigApplicationTests {

	@MockBean
	private PageHelperAutoConfiguration pageHelperAutoConfiguration;
	@MockBean
	private FlMybatisPluginAutoConfiguration flMybatisPluginAutoConfiguration;

	@Test
	public void contextLoads() {
	}

	@Test
	public void getInfo1() {
		assertNotNull(pageHelperAutoConfiguration);
		//assertEquals(null, flMybatisPluginAutoConfiguration.getFlMybatisProperties()
		//	);
	}

	@Test
	public void getInfo2() {
		assertNotNull(flMybatisPluginAutoConfiguration);

	}

	@Test
	public void getInfo3() {
		assertEquals(null, flMybatisPluginAutoConfiguration.getFlMybatisProperties()
		);
	}
}

