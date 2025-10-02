package com.mobile.server;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ServerApplication.class)
@ActiveProfiles("test")
class ServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
