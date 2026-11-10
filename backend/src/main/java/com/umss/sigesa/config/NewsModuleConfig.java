package com.umss.sigesa.config;

import com.umss.sigesa.application.port.out.NewsArticlePort;
import com.umss.sigesa.application.service.news.NewsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class NewsModuleConfig {

    @Bean
    NewsService newsService(NewsArticlePort newsArticlePort) {
        return new NewsService(newsArticlePort, Clock.systemUTC());
    }
}
