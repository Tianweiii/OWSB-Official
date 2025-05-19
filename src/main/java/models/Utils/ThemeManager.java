package models.Utils;

import javafx.scene.Scene;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for managing application themes.
 * Handles switching between light and dark modes.
 */
public class ThemeManager {

    /**
     * Available theme types.
     */
    public enum Theme {
        LIGHT,
        DARK
    }

    // Theme stylesheet paths
    private static final String LIGHT_THEME_PATH = "@../css/light.css";
    private static final String DARK_THEME_PATH = "@../css/dark.css";

    // Module-specific stylesheet paths
    private static final Map<String, String> MODULE_STYLESHEETS = new HashMap<>();

    static {
        MODULE_STYLESHEETS.put("sales-manager", "@../css/sales-manager.css");
        // Add other module stylesheets as needed
    }

    /**
     * Applies a theme to a scene.
     *
     * @param scene The scene to apply the theme to
     * @param theme The theme to apply
     * @param moduleKey The module key for specific styling (optional)
     */
    public static void applyTheme(Scene scene, Theme theme, String moduleKey) {
        // Clear existing stylesheets
        scene.getStylesheets().clear();

        // Apply theme stylesheet
        String themePath = (theme == Theme.DARK) ? DARK_THEME_PATH : LIGHT_THEME_PATH;
        scene.getStylesheets().add(Objects.requireNonNull(ThemeManager.class.getResource(themePath)).toExternalForm());

        // Apply module-specific stylesheet if provided
        if (moduleKey != null && MODULE_STYLESHEETS.containsKey(moduleKey)) {
            String modulePath = MODULE_STYLESHEETS.get(moduleKey);
            scene.getStylesheets().add(Objects.requireNonNull(ThemeManager.class.getResource(modulePath)).toExternalForm());
        }
    }

    /**
     * Toggles between light and dark themes.
     *
     * @param scene The scene to toggle the theme for
     * @param currentTheme The current theme
     * @param moduleKey The module key for specific styling (optional)
     * @return The new theme after toggling
     */
    public static Theme toggleTheme(Scene scene, Theme currentTheme, String moduleKey) {
        Theme newTheme = (currentTheme == Theme.DARK) ? Theme.LIGHT : Theme.DARK;
        applyTheme(scene, newTheme, moduleKey);
        return newTheme;
    }

    /**
     * Registers a module-specific stylesheet.
     *
     * @param moduleKey The key to identify the module
     * @param stylesheetPath The path to the stylesheet
     */
    public static void registerModuleStylesheet(String moduleKey, String stylesheetPath) {
        MODULE_STYLESHEETS.put(moduleKey, stylesheetPath);
    }

    /**
     * Gets the path for a specific theme.
     *
     * @param theme The theme to get the path for
     * @return The stylesheet path
     */
    public static String getThemePath(Theme theme) {
        return (theme == Theme.DARK) ? DARK_THEME_PATH : LIGHT_THEME_PATH;
    }
}