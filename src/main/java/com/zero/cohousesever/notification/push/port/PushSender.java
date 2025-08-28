package com.zero.cohousesever.notification.push.port;

import com.zero.cohousesever.common.exception.CustomException;

public interface PushSender {
    void send(PushCommand command) throws CustomException;
}