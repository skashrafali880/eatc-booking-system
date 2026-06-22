package com.eatc.model;

import com.eatc.exception.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Subject {
    private final String subjectId;
    private final String name;
    private final BigDecimal price;

    public Subject(String subjectId, String name, BigDecimal price) {
        this.subjectId = requireText(subjectId, "Subject ID");
        this.name = requireText(name, "Subject name");
        if (price == null) {
            throw new ValidationException("Subject price must not be null");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Subject price must be greater than zero");
        }
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Subject{" +
                "subjectId='" + subjectId + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price.toPlainString() +
                '}';
    }
}
