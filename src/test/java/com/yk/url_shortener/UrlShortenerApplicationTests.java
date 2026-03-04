package com.yk.url_shortener;

import com.yk.url_shortener.service.UrlEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class UrlShortenerApplicationTests {

	// Mock the Kafka producer so the full Spring context loads
	// without needing a real Kafka broker running during tests
	@MockitoBean
	private UrlEventProducer urlEventProducer;

	@Test
	void contextLoads() {
	}

}
