package com.wushiyii.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Pair<LEFT, RIGHT> {

    private LEFT left;
    private RIGHT right;


    public static <LEFT, RIGHT> Pair<LEFT, RIGHT> of (LEFT left, RIGHT right) {
        return new Pair<>(left, right);
    }

}
