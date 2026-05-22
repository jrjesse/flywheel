package com.antigravity.sales.core.model;

public enum CompanySize {
    MICRO("1-10"),
    SMALL("11-50"),
    MEDIUM("51-200"),
    LARGE("200-500"),
    ENTERPRISE("500+"),
    UNKNOWN("Desconhecido");

    private final String label;

    CompanySize(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    // Helper to map old string sizes to new Enum
    public static CompanySize fromString(String text) {
        if (text == null) return UNKNOWN;
        for (CompanySize b : CompanySize.values()) {
            if (b.label.equals(text) || b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return UNKNOWN;
    }
}
