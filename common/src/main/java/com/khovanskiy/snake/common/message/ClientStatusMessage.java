package com.khovanskiy.snake.common.message;

import com.khovanskiy.snake.common.Direction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author victor
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClientStatusMessage extends Message {
    Direction direction;
}
