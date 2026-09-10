package de.freddy.glitchdeath;

public enum GlitchDeathMode {
    NAME,
    GLITCH,
    RANDOM;

    public static GlitchDeathMode fromString(String value) {
        return switch (value.toLowerCase()) {
            case "name", "normal" -> NAME;
            case "glitch" -> GLITCH;
            case "random" -> RANDOM;
            default -> null;
        };
    }
}
