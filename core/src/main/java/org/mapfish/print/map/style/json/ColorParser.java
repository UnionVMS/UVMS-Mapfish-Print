/*
 * Copyright (C) 2014  Camptocamp
 *
 * This file is part of MapFish Print
 *
 * MapFish Print is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MapFish Print is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MapFish Print.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mapfish.print.map.style.json;

import com.google.common.base.Optional;
import org.geotools.styling.SLD;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses colors from text strings. Supports formats:
 * <ul>
 *     <li><code>#F00</code></li>
 *     <li><code>#FFAABB</code></li>
 *     <li><code>rgb(100,100,100)</code></li>
 *     <li><code>rgba(100,100,100, 100)</code></li>
 *     <li><code>hsl(100,100,100)</code></li>
 *     <li><code>hsla(100,100,100, 100)</code></li>
 *     <li><code>-65536</code></li>
 *     <li><code>red</code></li>
 * </ul>
 * The numbers in the rgb(..) and hsl(..) can have the following formats:
 * <ul>
 *     <li>0-255</li>
 *     <li>0%-100%</li>
 *     <li>0.0%-100.0%</li>
 *     <li>0.0-1.0</li>
 *     <li>0f-1f</li>
 *     <li>0.0f-1.0f</li>
 * </ul>
 * @author Jesse on 7/25/2014.
 */
public final class ColorParser {
    private static final float MAX_INT_COLOR = 255f;

    private static final String NUMBER_PATTERN = "\\s*(\\d*\\.?\\d*[%f]?)\\s*";
    private static final Pattern RGB_COLOR_EXTRACTOR = Pattern.compile(
            "rgb\\s*\\(" + NUMBER_PATTERN + "," + NUMBER_PATTERN + "," + NUMBER_PATTERN + "\\)");
    private static final Pattern RGBA_COLOR_EXTRACTOR = Pattern.compile(
            "rgba\\s*\\(" + NUMBER_PATTERN + "," + NUMBER_PATTERN + "," + NUMBER_PATTERN + "," + NUMBER_PATTERN + "\\)");

    private static final Pattern HSL_COLOR_EXTRACTOR = Pattern.compile(
            "hsl\\s*\\(" + NUMBER_PATTERN + "," + NUMBER_PATTERN + "," + NUMBER_PATTERN + "\\)");
    private static final Pattern HSLA_COLOR_EXTRACTOR = Pattern.compile(
            "hsla\\s*\\(" + NUMBER_PATTERN + "," + NUMBER_PATTERN + "," + NUMBER_PATTERN + "," + NUMBER_PATTERN + "\\)");


    private ColorParser() {
        // utility class so ignore
    }

    /**
     * Parse the string and convert it to a {@link java.awt.Color}.  See class description for details on the supported
     * color formats.
     *
     * @param colorString the color string encoded.
     */
    public static Color toColor(final String colorString) {
        String trimmedString = colorString.trim();
        Color color = null;
        if (trimmedString.startsWith("#")) {
            final int shortHexCode = 4;
            if (trimmedString.length() == shortHexCode) {
                StringBuilder builder = new StringBuilder("#");
                for (int i = 1; i < trimmedString.length(); i++) {
                    builder.append(trimmedString.charAt(i));
                    builder.append(trimmedString.charAt(i));
                }
                color = SLD.toColor(builder.toString());
            } else {
                color = SLD.toColor(trimmedString);
            }
        }

        if (color == null) {
            color = parseRgbColor(trimmedString);
        }

        if (color == null) {
            color = parseRgbaColor(trimmedString);
        }

        if (color == null) {
            color = parseHslColor(trimmedString);
        }

        if (color == null) {
            color = parseHslaColor(trimmedString);
        }

        if (color == null) {
            final Field[] fields = Color.class.getFields();
            for (Field field : fields) {
                if (field.getType() == Color.class && Modifier.isFinal(field.getModifiers())
                    && Modifier.isStatic(field.getModifiers()) && field.getName().equalsIgnoreCase(trimmedString)) {
                    try {
                        return (Color) field.get(null);
                    } catch (IllegalAccessException e) {
                        throw new Error(e);
                    }
                }
            }
            color = Color.decode(trimmedString);
        }

        return color;
    }
    // CSOFF: MagicNumber
    private static Color parseRgbColor(final String colorString) {
        final Matcher matcher = RGB_COLOR_EXTRACTOR.matcher(colorString);
        if (matcher.matches()) {
            String red = matcher.group(1);
            String green = matcher.group(2);
            String blue = matcher.group(3);
            return toColorRGBA(red, green, blue, "255");
        }

        return null;
    }
    private static Color parseRgbaColor(final String colorString) {
        final Matcher matcher = RGBA_COLOR_EXTRACTOR.matcher(colorString);
        if (matcher.matches()) {
            String red = matcher.group(1);
            String green = matcher.group(2);
            String blue = matcher.group(3);
            String alpha = matcher.group(4);
            return toColorRGBA(red, green, blue, alpha);
        }

        return null;
    }
    private static Color parseHslColor(final String colorString) {
        final Matcher matcher = HSL_COLOR_EXTRACTOR.matcher(colorString);
        if (matcher.matches()) {
            String hue = matcher.group(1);
            String saturation = matcher.group(2);
            String luminance = matcher.group(3);
            String alpha = "255";
            return toColorFromHSLA(hue, saturation, luminance, alpha);
        }

        return null;
    }
    private static Color parseHslaColor(final String colorString) {
        final Matcher matcher = HSLA_COLOR_EXTRACTOR.matcher(colorString);
        if (matcher.matches()) {
            String hue = matcher.group(1);
            String saturation = matcher.group(2);
            String luminance = matcher.group(3);
            String alpha = matcher.group(4);
            return toColorFromHSLA(hue, saturation, luminance, alpha);
        }

        return null;
    }
    // CSOFF: MagicNumber
    private static Color toColorRGBA(final String red, final String green, final String blue, final String alpha) {
        float finalRed = parsePercent(red).
                or(parseInt(red)).
                or(parseFloat(red)).
                or(parseDouble(red)).get();
        float finalGreen = parsePercent(green).
                or(parseInt(green)).
                or(parseFloat(green)).
                or(parseDouble(green)).get();
        float finalBlue = parsePercent(blue).
                or(parseInt(blue)).
                or(parseFloat(blue)).
                or(parseDouble(blue)).get();
        float finalAlpha = parsePercent(alpha).
                or(parseInt(alpha)).
                or(parseFloat(alpha)).
                or(parseDouble(alpha)).or(1.0f);

        return new Color(finalRed, finalGreen, finalBlue, finalAlpha);
    }

    private static Optional<Float> parsePercent(final String colorString) {
        if (colorString.endsWith("%")) {
            return Optional.of(parseDouble(colorString.substring(0, colorString.length() - 1)).get() / 100f);
        }

        return Optional.absent();
    }

    private static Color toColorFromHSLA(final String hue, final String saturation, final String luminance, final String alpha) {
        float finalHue = parsePercent(hue).
                or(parseInt(hue)).
                or(parseFloat(hue)).
                or(parseDouble(hue)).get();
        float finalSaturation = parsePercent(saturation).
                or(parseInt(saturation)).
                or(parseFloat(saturation)).
                or(parseDouble(saturation)).get();
        float finalLuminance = parsePercent(luminance).
                or(parseInt(luminance)).
                or(parseFloat(luminance)).
                or(parseDouble(luminance)).get();
        float finalAlpha = parsePercent(alpha).
                or(parseInt(alpha)).
                or(parseFloat(alpha)).
                or(parseDouble(alpha)).or(1.0f);

        if (finalSaturation < 0.0f || finalSaturation > 1.0f) {
            String message = "Color parameter outside of expected range - Saturation (" + saturation + ")";
            throw new IllegalArgumentException(message);
        }
        if (finalLuminance < 0.0f || finalLuminance > 1.0f) {
            String message = "Color parameter outside of expected range - Luminance (" + luminance + ")";
            throw new IllegalArgumentException(message);
        }

        if (finalAlpha < 0.0f || finalAlpha > 1.0f) {
            String message = "Color parameter outside of expected range - Alpha (" + alpha + ")";
            throw new IllegalArgumentException(message);
        }

        float q;

        if (finalLuminance < 0.5) {
            q = finalLuminance * (1 + finalSaturation);
        } else {
            q = (finalLuminance + finalSaturation) - (finalSaturation * finalLuminance);
        }

        float p = 2 * finalLuminance - q;

        float red = hueToRGB(p, q, finalHue + (1.0f / 3.0f));
        float green = hueToRGB(p, q, finalHue);
        float blue = hueToRGB(p, q, finalHue - (1.0f / 3.0f));
        return new Color(red, green, blue, finalAlpha);
    }
    private static float hueToRGB(final float p, final float q, final float hue)
    {
        float finalHue = hue;
        if (finalHue < 0) {
            finalHue += 1;
        }

        if (finalHue > 1) {
            finalHue -= 1;
        }

        if (6 * finalHue < 1) {
            return p + ((q - p) * 6 * finalHue);
        }

        if (2 * finalHue < 1) {
            return q;
        }

        if (3 * finalHue < 2) {
            return p + ((q - p) * 6 * ((2.0f / 3.0f) - finalHue));
        }

        return p;
    }
    private static Optional<Float> parseFloat(final String stringForm) {
        try {
            return Optional.of(Float.parseFloat(stringForm));
        } catch (NumberFormatException e) {
            return Optional.absent();
        }
    }   private static Optional<Float> parseDouble(final String stringForm) {
        try {
            return Optional.of((float) Double.parseDouble(stringForm));
        } catch (NumberFormatException e) {
            return Optional.absent();
        }
    }
    private static Optional<Float> parseInt(final String stringForm) {
        try {
            final int i = Integer.parseInt(stringForm);
            if (i == 0) {
                return Optional.of(0f);
            }
            return Optional.of(i / MAX_INT_COLOR);
        } catch (NumberFormatException e) {
            return Optional.absent();
        }
    }

    /**
     * Check if the given color string can be parsed.
     * @param colorString The color to parse.
     */
    public static boolean canParseColor(final String colorString) {
        try {
            return ColorParser.toColor(colorString) != null;
        } catch (Exception exc) {
            return false;
        }
    }

    /**
     * Get the "rgb(...)" representation for a color.
     * @param color The color.
     */
    public static String toRGB(final Color color) {
        return "rgb("
                + color.getRed() + ", "
                + color.getGreen() + ", "
                + color.getBlue() + ")";
    }
}
