package io.github.mohmk10.changeloghub.api.security;

import io.github.mohmk10.changeloghub.api.entity.User;
import io.github.mohmk10.changeloghub.api.service.UserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    public OAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Long githubId = ((Number) oAuth2User.getAttribute("id")).longValue();
        String username = oAuth2User.getAttribute("login");
        String email = oAuth2User.getAttribute("email");
        String avatarUrl = oAuth2User.getAttribute("avatar_url");

        User user = userService.createOrUpdateUser(githubId, username, email, avatarUrl);

        return new OAuth2UserPrincipal(user, oAuth2User.getAttributes());
    }
}
