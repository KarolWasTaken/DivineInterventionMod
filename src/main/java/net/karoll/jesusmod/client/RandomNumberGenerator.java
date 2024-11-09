package net.karoll.jesusmod.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class RandomNumberGenerator {
    private List<Integer> numbers;
    private int currentIndex;

    public RandomNumberGenerator(int length) {
        initializeNumbers(length - 1);
    }

    private void initializeNumbers(int length) {
        numbers = new ArrayList<>();
        for (int i = 0; i <= length; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        currentIndex = 0;
    }

    public int getNextRandom() {
        if (currentIndex >= numbers.size()) {
            Collections.shuffle(numbers);
            currentIndex = 0;
        }
        return numbers.get(currentIndex++);
    }
}
