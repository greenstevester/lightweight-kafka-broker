package net.greensill.lwkb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@SpringBootApplication
public class LwKafkaBrokerApplication {

	@Value(value="${lw-kafka-broker.port-1}")
	private String brokerPort1;

	@Value(value="${lw-kafka-broker.port-2}")
	private String brokerPort2;

	@Value(value="${lw-kafka-broker.port-3}")
	private String brokerPort3;

	private final Environment env;

	public LwKafkaBrokerApplication(Environment env) {
		this.env = env;
	}


	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(LwKafkaBrokerApplication.class);
		Environment env = app.run(args).getEnvironment();
		logApplicationStartup(env);
	}

	private static void logApplicationStartup(Environment env) {
		String protocol = "http";
		if (env.getProperty("server.ssl.key-store") != null) {
			protocol = "https";
		}
		String serverPort = env.getProperty("server.port");
		String contextPath = env.getProperty("server.servlet.context-path");
		if (StringUtils.hasLength(contextPath)) {
			contextPath = "/";
		}
		String hostAddress = "localhost";
		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.warn("The host name could not be determined, using `localhost` as fallback");
		}
		log.info(
				"\n----------------------------------------------------------\n\t" +
						"Application '{}' is running! Host and port info:\n\t" +
						"Local: \t\t{}:localhost:{}\n\t" +
						"External: \t{}:{}:{}\n\t" +
						"Profile(s): \t{}\n----------------------------------------------------------",
				env.getProperty("spring.application.name"),
				protocol,
				serverPort,
				protocol,
				hostAddress,
				serverPort,
				env.getActiveProfiles()
		);
	}

	@Bean
	EmbeddedKafkaBroker embeddedKafkaBroker() {
		return  new EmbeddedKafkaBroker(3)
				.kafkaPorts(Integer.valueOf(brokerPort1),Integer.valueOf(brokerPort2),Integer.valueOf(brokerPort3))
				.brokerListProperty("spring.kafka.bootstrap-servers")
				.brokerListProperty("spring.kafka.consumer.group-id");
		}
}
