package me.nathan.oauthclient.config;

import me.nathan.oauthclient.config.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import me.nathan.oauthclient.config.provider.DaeunOAuthConfiguration;
import me.nathan.oauthclient.config.provider.JeongminOAuthConfiguration;
import me.nathan.oauthclient.config.provider.NathanOAuthConfiguration;
import me.nathan.oauthclient.config.provider.WoojaeOAuthConfiguration;
import me.nathan.oauthclient.config.security.OAuth2AuthenticationFailureHandler;
import me.nathan.oauthclient.config.security.OAuth2AuthenticationSuccessHandler;
import me.nathan.oauthclient.config.security.TokenAuthenticationFilter;
import me.nathan.oauthclient.service.CustomOAuthUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EnableOAuth2Client
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomOAuthUserService customOAuthUserService;

    @Autowired
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;



    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers("/js/**")
                .antMatchers("/favicon*/**")
                .antMatchers("/static/**")
                .antMatchers("/main**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf().disable();

        http.headers()
                .frameOptions().sameOrigin(); // SockJS는 기본적으로 HTML iframe 요소를 통한 전송을 허용하지 않도록 설정되는데 해당 내용을 해제한다.

        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http
                .authorizeRequests()
                .antMatchers("/chat.html", "/ws-stomp/**", "/ws**", "/ws/chat/**").permitAll()
                .antMatchers("/", "/index", "/chat/**").permitAll()
                .antMatchers("/oauth/**", "/login/**", "/oauth2/**", "/loginPage").permitAll()
                .antMatchers("/api/**").permitAll()
                .anyRequest().authenticated();
        http.
                logout()
                .logoutSuccessUrl("/");

        http
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint())
                .and()
                .oauth2Login()
                .clientRegistrationRepository(clientRegistrationRepository())
                .tokenEndpoint()
                .and()
                .authorizationEndpoint()
                .authorizationRequestRepository(cookieOAuth2AuthorizationRequestRepository())
                .and()
                .userInfoEndpoint()
                .userService(customOAuthUserService)
                .and()
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler);

        // Add our custom Token based authentication filter
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
        return accessTokenResponseClient;
    }


    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        LoginUrlAuthenticationEntryPoint entryPoint = new LoginUrlAuthenticationEntryPoint("/loginPage");
        return entryPoint;
    }

    // 다중 Client 등록
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> clientRegistrations = new ArrayList<>();

        clientRegistrations.add(nathanOAuthConfiguration().getBuilder().build());
        clientRegistrations.add(woojaeOAuthConfiguration().getBuilder().build());
        clientRegistrations.add(jeongminOAuthConfiguration().getBuilder().build());
        clientRegistrations.add(daeunOAuthConfiguration().getBuilder().build());

        return new InMemoryClientRegistrationRepository(clientRegistrations);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public NathanOAuthConfiguration nathanOAuthConfiguration() {
        return new NathanOAuthConfiguration();
    }

    @Bean
    public WoojaeOAuthConfiguration woojaeOAuthConfiguration() {
        return new WoojaeOAuthConfiguration();
    }

    @Bean
    public JeongminOAuthConfiguration jeongminOAuthConfiguration() {
        return new JeongminOAuthConfiguration();
    }

    @Bean
    public DaeunOAuthConfiguration daeunOAuthConfiguration() {
        return new DaeunOAuthConfiguration();
    }

    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter();
    }

}
