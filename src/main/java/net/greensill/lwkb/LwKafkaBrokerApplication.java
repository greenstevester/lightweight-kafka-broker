package net.greensill.lwkb;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@SpringBootApplication
public class LwKafkaBrokerApplication {

	public static final String RSOCKET_1 = "rsocket-1";
	@Value(value="${lw-kafka-broker.port-1}")
	private String brokerPort1;

	@Value(value="${lw-kafka-broker.port-2}")
	private String brokerPort2;

	@Value(value="${lw-kafka-broker.port-3}")
	private String brokerPort3;

	private final Environment env;

	@Lazy
	@Autowired
	private AdminClient adminClient;

	CountDownLatch countDownLatch;

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
	@Primary
	EmbeddedKafkaBroker embeddedKafkaBroker() {
		EmbeddedKafkaBroker embeddedKafkaBroker = new EmbeddedKafkaBroker(3)
				.kafkaPorts(Integer.valueOf(brokerPort1),Integer.valueOf(brokerPort2),Integer.valueOf(brokerPort3))
				.brokerListProperty("spring.kafka.bootstrap-servers")
				.brokerListProperty("spring.kafka.consumer.group-id");

		countDownLatch = new CountDownLatch(2);
		return embeddedKafkaBroker;
	}

	@EventListener(ApplicationReadyEvent.class)
	void inputTopic() throws ExecutionException, InterruptedException, TimeoutException {
		countDownLatch.await(3L, TimeUnit.SECONDS);
		log.info("adding topic");
		TopicBuilder
				       .name(RSOCKET_1)
				       .partitions(1)
				       .replicas(1)
				       .build();
		countDownLatch.await(3L, TimeUnit.SECONDS);
//		ListTopicsResult ltr = adminClient.listTopics().names().get();
//		boolean topicExists = adminClient.listTopics().names().get().contains("rsocket-1");
//		log.info("topicExists: {}", topicExists);

		System.out.println("createKafkaTopic(): Listing Topics...");
		ListTopicsResult listTopicsResult = adminClient.listTopics(new ListTopicsOptions().timeoutMs(900000));
		System.out.println("createKafkaTopic(): Retrieve Topic names...");
		KafkaFuture<Collection<TopicListing>> setKafkaFuture = listTopicsResult.listings();
		System.out.println("createKafkaTopic(): Display existing Topics...");
		while(!setKafkaFuture.isDone()) {
			System.out.println("Waiting...");
			Thread.sleep(10);
		}
		Collection<TopicListing> topicNames = setKafkaFuture.get(900, TimeUnit.SECONDS);
		System.out.println(topicNames);
		System.out.println("createKafkaTopic(): Check if Topic exists...");
		boolean topicExists = topicNames.contains(RSOCKET_1);
		log.info("topicExists:{}", topicExists);

	}

}
