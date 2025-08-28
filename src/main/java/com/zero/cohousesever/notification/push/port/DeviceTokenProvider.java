package com.zero.cohousesever.notification.push.port;

import java.util.Optional;

public interface DeviceTokenProvider {
    Optional<String> findActiveTokenByMemberId(Long memberId);
}