package io.github.mohmk10.changeloghub.api.security;

import io.github.mohmk10.changeloghub.api.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final String frontendUrl;

    public OAuth2SuccessHandler(JwtTokenProvider jwtTokenProvider,
                                @Value("${app.frontend-url}") String frontendUrl) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2UserPrincipal principal = (OAuth2UserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        String redirectUrl = frontendUrl + "/auth/callback?token=" + token;
        response.sendRedirect(redirectUrl);
    }
}
