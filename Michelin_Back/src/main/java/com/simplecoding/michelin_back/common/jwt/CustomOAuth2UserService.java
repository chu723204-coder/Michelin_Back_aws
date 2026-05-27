package com.simplecoding.michelin_back.common.jwt;

import com.simplecoding.michelin_back.member.entity.Member;
import com.simplecoding.michelin_back.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2Attributes oAuth2Attributes = OAuth2Attributes.of(registrationId, attributes);

        Member member = saveOrUpdate(oAuth2Attributes);

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getMemberGrade())),
                attributes,
                userNameAttributeName
        );
    }

    private Member saveOrUpdate(OAuth2Attributes attributes) {
        Member member = memberRepository.findByEmail(attributes.getEmail())
                .orElse(attributes.toMember());
        return memberRepository.save(member);
    }
}
