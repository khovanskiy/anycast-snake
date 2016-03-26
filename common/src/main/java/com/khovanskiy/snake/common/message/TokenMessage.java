package com.khovanskiy.snake.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.InetAddress;
import java.util.UUID;

/**
 * @author victor
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TokenMessage extends Message {
    UUID token;
    InetAddress address;
    int port;
}
