package com.BeatUp.BackEnd.common.config;

import com.BeatUp.BackEnd.common.util.PageableUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PagingConfig {

    private final PagingProperties pagingProperties;

    @PostConstruct
    public void init(){
        PageableUtil.setPagingProperties(pagingProperties);
    }
}
