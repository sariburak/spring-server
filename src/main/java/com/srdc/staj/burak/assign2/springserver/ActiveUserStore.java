package com.srdc.staj.burak.assign2.springserver;

import org.springframework.context.annotation.Bean;

import java.util.List;

public class ActiveUserStore {
    public List<String> users;

    @Bean
    ActiveUserStore activeUserStore(){
        return new ActiveUserStore();

    }
}
