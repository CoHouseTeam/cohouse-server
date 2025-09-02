package com.zero.cohousesever.member.service.oauth2;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuth2ProviderFactory {

    private final List<OAuth2ProviderService> providers;

    public OAuth2ProviderService getProvider(String providerName) {
        return providers.stream()
                .filter(p -> p.getProvider().equalsIgnoreCase(providerName))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.OAUTH2_PROVIDER_UNSUPPORTED));
    }
}
