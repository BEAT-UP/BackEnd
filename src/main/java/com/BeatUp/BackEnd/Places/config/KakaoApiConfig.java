package com.BeatUp.BackEnd.Places.config;


import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@ConfigurationProperties(prefix = "kakao.api")
@Data
public class KakaoApiConfig {

    private String key;
    private String baseUrl;
    private Duration timeout = Duration.ofMillis(1500);
    private int retryCount = 1;
    private int maxConnections = 50;

    @Bean
    public WebClient kakaoWebClient(){
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) timeout.toMillis())
                .responseTimeout(timeout)
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(timeout.toSeconds(), TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(timeout.toSeconds(), TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Authorization", "KakaoAK" + key)
                .defaultHeader("Content-Type", "application.json:charset=UTF-8")
                .build();
    }
}
