package com.nominal.lynx;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.data.mongodb.uri=mongodb://localhost:27017/lynx_core_test"
})
class LynxCoreApplicationTests {

	@Test
	void contextLoads() {
	}

}
