package com.pkmprojects.shoppiq;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.pkmprojects.shoppiq.config.PaginationProperties;

import java.util.Arrays;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(PaginationProperties.class)
public class ShoppiqApplication {

	private static final Logger log = LoggerFactory.getLogger(ShoppiqApplication.class);

	private final Environment env;

	public ShoppiqApplication(Environment env) {
		this.env = env;
	}

	@PostConstruct
	void logStartup() {
		String[] active = env.getActiveProfiles();
		boolean aiEnabled = "true".equals(env.getProperty("shoppiq.ai.enabled"));
		log.debug("[AI-STARTUP] Active profiles: {} — ai-enabled={}", Arrays.toString(active), aiEnabled);
		if (aiEnabled) {
			log.debug("[AI-STARTUP] NVIDIA_API_KEY present: {}",
					env.getProperty("NVIDIA_API_KEY") != null ? "yes (len=" + env.getProperty("NVIDIA_API_KEY").length() + ")" : "NO");
		}
	}

	static void main(String[] args) {
		SpringApplication.run(ShoppiqApplication.class, args);
	}

}
