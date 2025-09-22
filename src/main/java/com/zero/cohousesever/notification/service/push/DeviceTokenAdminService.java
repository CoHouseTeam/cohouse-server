package com.zero.cohousesever.notification.service.push;

import com.zero.cohousesever.notification.entity.DeviceToken;
import com.zero.cohousesever.notification.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceTokenAdminService {

    private final DeviceTokenRepository repo;

    /**
     * 무효/만료 토큰 비활성화
     */
    @Transactional
    public void deactivate(String token) {
        repo.findByToken(token).ifPresent(DeviceToken::deactivate);
    }
}