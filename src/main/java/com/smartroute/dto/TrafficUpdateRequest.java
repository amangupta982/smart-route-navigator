package com.smartroute.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request body for traffic update operations.
 *
 * Example JSON (slow traffic):
 * { "from": 3, "to": 7, "multiplier": 2.5 }
 *
 * Example JSON (block road):
 * { "from": 3, "to": 7, "blocked": true }
 */
public class TrafficUpdateRequest {

    @NotNull
    @Min(0)
    private Integer from;

    @NotNull
    @Min(0)
    private Integer to;

    // 1.0 = normal, 2.0 = slow, 5.0 = very congested, 999 = effectively blocked
    @Positive
    private double multiplier = 1.0;

    private boolean blocked = false;

    // Getters and Setters
    public Integer getFrom() { return from; }
    public void setFrom(Integer from) { this.from = from; }

    public Integer getTo() { return to; }
    public void setTo(Integer to) { this.to = to; }

    public double getMultiplier() { return multiplier; }
    public void setMultiplier(double multiplier) { this.multiplier = multiplier; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}
