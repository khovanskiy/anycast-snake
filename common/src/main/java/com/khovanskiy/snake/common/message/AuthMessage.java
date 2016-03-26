package com.khovanskiy.snake.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * @author victor
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuthMessage extends Message {
    UUID token;
}
