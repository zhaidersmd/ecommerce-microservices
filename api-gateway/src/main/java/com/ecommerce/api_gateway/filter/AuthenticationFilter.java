package com.ecommerce.api_gateway.filter;

import com.ecommerce.api_gateway.util.JwtService;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator routeValidator;

    @Autowired
    private JwtService jwtService;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (((exchange, chain) -> {

            if (routeValidator.isSecured.test(exchange.getRequest())) {
                //header contains token ?
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Missing authorization header");
                }

                String authHeaders = exchange
                        .getRequest()
                        .getHeaders()
                        .get(org.springframework.http.HttpHeaders.AUTHORIZATION).get(0);

                String authToken = null;
                if (authHeaders!=null && authHeaders.startsWith("Bearer ")) {
                    authToken=authHeaders.substring(7);
                }

                try {
                    jwtService.validateToken(authToken);
                } catch (Exception e) {
                    System.out.println("invalid access!");
                    throw new RuntimeException("Unauthorized access to the app.");
                }



            }

            return chain.filter(exchange);


        }) );
    }

    public static class Config {




    }
}
