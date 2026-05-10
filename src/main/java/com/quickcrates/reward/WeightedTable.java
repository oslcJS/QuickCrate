package com.quickcrates.reward;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WeightedTable {
    private final List<Reward> rewards;
    private final int total;

    public WeightedTable(List<Reward> rewards) {
        this.rewards = rewards;
        int sum = 0;
        for (Reward r : rewards) sum += r.getWeight();
        this.total = Math.max(1, sum);
    }

    public Reward pick() {
        int n = ThreadLocalRandom.current().nextInt(total);
        for (Reward r : rewards) {
            n -= r.getWeight();
            if (n < 0) return r;
        }
        return rewards.get(rewards.size() - 1);
    }

    public List<Reward> all() { return rewards; }
}
