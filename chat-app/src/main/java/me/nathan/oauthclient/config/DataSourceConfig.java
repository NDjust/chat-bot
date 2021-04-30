package me.nathan.oauthclient.config;

import me.nathan.oauthclient.OauthClientApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackageClasses = OauthClientApplication.class)
public class DataSourceConfig {
}
