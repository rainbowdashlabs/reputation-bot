package de.chojo.repbot.listener;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MessageListenerTest {

    @Test
    public void testFlip() {
        var result = divide(1);
        Assertions.assertEquals(1, result[0]);
        Assertions.assertEquals(0, result[1]);

        result = divide(2);
        Assertions.assertEquals(1, result[0]);
        Assertions.assertEquals(1, result[1]);

        result = divide(3);
        Assertions.assertEquals(2, result[0]);
        Assertions.assertEquals(1, result[1]);

        result = divide(4);
        Assertions.assertEquals(2, result[0]);
        Assertions.assertEquals(2, result[1]);

        result = divide(5);
        Assertions.assertEquals(3, result[0]);
        Assertions.assertEquals(2, result[1]);

        result = divide(6);
        Assertions.assertEquals(3, result[0]);
        Assertions.assertEquals(3, result[1]);

        result = divide(7);
        Assertions.assertEquals(4, result[0]);
        Assertions.assertEquals(3, result[1]);
    }

    public int[] divide(int total) {
        int left = 0, right = 0;
        for (var i = 1; i <= total; i++) {
            if (leftSide(i, total)) left++;
            else right++;
        }
        return new int[]{left, right};
    }

    private boolean leftSide(int num, int total) {
        return num <= total / 2 + total % 2;
    }
}
