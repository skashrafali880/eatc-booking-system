package com.eatc.model;

import com.eatc.exception.ValidationException;

import java.time.LocalDateTime;

public final class Review {
    private final int rating;
    private final String comment;
    private final LocalDateTime createdAt;

    public Review(int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5 inclusive");
        }
        if (comment == null || comment.isBlank()) {
            throw new ValidationException("Review comment must not be blank");
        }
        this.rating = rating;
        this.comment = comment.trim();
        this.createdAt = LocalDateTime.now();
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Review{" +
                "rating=" + rating +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
