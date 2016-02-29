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

package org.mapfish.print.map;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.measure.unit.Unit;


/**
 * An enum for expressing distance units. Contains everything needed for
 * conversions and others.
 */
public enum DistanceUnit {
    // CSOFF: MagicNumber
    /**
     * Represents the meter unit.
     */
    M(null, 1.0, 1.0, new String[]{"m", "meter", "meters"}),
    /**
     * Represents the millimeter unit.
     */
    MM(DistanceUnit.M, 0.001, 0.001, new String[]{"mm", "millimeter", "millimeters"}),
    /**
     * Represents the centimeter unit.
     */
    CM(DistanceUnit.M, 0.01, 0.01, new String[]{"cm", "centimeter", "centimeters"}),
    /**
     * Represents the kilometer unit.
     */
    KM(DistanceUnit.M, 1000.0, 1000.0, new String[]{"km", "kilometer", "kilometers"}),

    /**
     * Represents the american foot unit.
     */
    FT(null, 1.0, 25.4 / 1000.0 * 12.0, new String[]{"ft", "foot", "feet"}),
    /**
     * Represents the american inch unit.
     */
    IN(DistanceUnit.FT, 1 / 12.0, 25.4 / 1000.0, new String[]{"in", "inch"}),
    /**
     * Represents the american yard unit.
     */
    YD(DistanceUnit.FT, 3.0, 25.4 / 1000.0 * 12.0 * 3.0, new String[]{"yd", "yard", "yards"}),
    /**
     * Represents the american mile unit.
     */
    MI(DistanceUnit.FT, 5280.0, 25.4 / 1000.0 * 12.0 * 5280.0, new String[]{"mi", "mile", "miles"}),

    /**
     * Represents the lat long degree unit.
     */
    DEGREES(null, 1.0, 40041470.0 / 360.0, new String[]{"\u00B0", "dd", "degree", "degrees"}),
    /**
     * Represents the lat long minute unit.
     */
    MINUTE(DistanceUnit.DEGREES, 1.0 / 60.0, 40041470.0 / 360.0, new String[]{"min", "minute", "minutes"}),
    /**
     * Represents the lat long second unit.
     */
    SECOND(DistanceUnit.DEGREES, 1.0 / 3600.0, 40041470.0 / 360.0, new String[]{"sec", "second", "seconds"}),

    /**
     * Represents the pixel unit.
     * The conversion factor is the one used by JasperReports (1 inch = 72 pixel).
     */
    PX(null, 1.0, 1 / 72.0 * (25.4 / 1000.0), new String[]{"px", "pixel"});

    /**
     * If null means that this is a base unit. Otherwise, point to the base unit.
     */
    private final DistanceUnit baseUnit;

    /**
     * Conversion factor to the base unit.
     */
    private final double baseFactor;

    /**
     * Conversion factor to meters.
     */
    private final double metersFactor;

    /**
     * All the ways to represent this unit as text.
     */
    private final String[] texts;

    /**
     * Cache all the units that share the same base unit.
     */
    private DistanceUnit[] allUnits = null;

    /**
     * Global dictionary of every textual representations of every units.
     */
    private static Map<String, DistanceUnit> translations = null;

    DistanceUnit(final DistanceUnit baseUnit, final double baseFactor, final double metersFactor, final String[] texts) {
        if (baseUnit == null) {
            this.baseUnit = this;
        } else {
            this.baseUnit = baseUnit;
        }
        this.baseFactor = baseFactor;
        this.texts = texts;
        this.metersFactor = metersFactor;
    }

    public boolean isBase() {
        return this.baseUnit == this;
    }

    /**
     * Convert values in this unit to the equivalent value in another unit.
     * <pre>
     * DistanceUnit.M.convertTo(1.0, DistanceUnit.MM)==1000.0
     * </pre>
     *
     * @param value      a value in the same unit as this {@link org.mapfish.print.map.DistanceUnit}
     * @param targetUnit the unit to convert value to (from this unit)
     */
    public double convertTo(final double value, final DistanceUnit targetUnit) {
        if (targetUnit == this) {
            return value;
        }
        if (isSameBaseUnit(targetUnit)) {
            return value * this.baseFactor / targetUnit.baseFactor;
        } else {
            return value * this.metersFactor / targetUnit.metersFactor;
        }
    }

    /**
     * Check if this unit and the target unit have the same "base" unit  IE inches and feet have same base unit.
     *
     * @param target the unit to compare to this unit.
     */
    public boolean isSameBaseUnit(final DistanceUnit target) {
        return target.baseUnit == this.baseUnit || target == this.baseUnit || target.baseUnit == this;
    }

    @Override
    public final String toString() {
        return this.texts[0];
    }

    /**
     * Parse the value and return the identified unit object.
     *
     * @param val the string to parse.
     * @return null if this unit is unknown
     */
    public static DistanceUnit fromString(final String val) {
        return getTranslations().get(val.toLowerCase());
    }

    /**
     * Return the sorted list (from smallest to biggest) of units sharing the
     * same base unit.
     *
     * @return the sorted list (from smallest to biggest) of units sharing the
     * same base unit.
     */
    public final synchronized DistanceUnit[] getAllUnits() {
        if (this.allUnits == null) {
            if (this.baseUnit != this) {
                this.allUnits = this.baseUnit.getAllUnits();
            } else {
                final DistanceUnit[] values = DistanceUnit.values();
                final List<DistanceUnit> list = new ArrayList<DistanceUnit>(values.length);
                for (int i = 0; i < values.length; ++i) {
                    DistanceUnit value = values[i];
                    if (value.baseUnit == this) {
                        list.add(value);
                    }
                }
                final DistanceUnit[] result = new DistanceUnit[list.size()];
                list.toArray(result);
                Arrays.sort(result, new Comparator<DistanceUnit>() {
                    public int compare(final DistanceUnit o1, final DistanceUnit o2) {
                        return Double.compare(o1.baseFactor, o2.baseFactor);
                    }
                });
                this.allUnits = result;
            }
        }

        return this.allUnits;
    }

    /**
     * Return the first unit that would give a value &gt;=1.
     *
     * @param value the value
     * @param unit  the unit of the value
     */
    public static DistanceUnit getBestUnit(final double value, final DistanceUnit unit) {
        DistanceUnit[] units = unit.getAllUnits();
        for (int i = units.length - 1; i >= 0; --i) {
            DistanceUnit cur = units[i];
            final double converted = Math.abs(unit.convertTo(1.0, cur) * value);
            if (converted >= 1.0) {
                return cur;
            }
        }
        return units[0];
    }

    private static synchronized Map<String, DistanceUnit> getTranslations() {
        if (translations == null) {
            translations = new HashMap<String, DistanceUnit>();
            final DistanceUnit[] values = DistanceUnit.values();
            for (DistanceUnit cur : values) {
                for (int j = 0; j < cur.texts.length; ++j) {
                    translations.put(cur.texts[j], cur);
                }
            }
        }
        return translations;
    }

    /**
     * Determine the unit of the given projection.
     *
     * @param projection the projection to determine
     */
    public static DistanceUnit fromProjection(final CoordinateReferenceSystem projection) {
        final Unit<?> projectionUnit = projection.getCoordinateSystem().getAxis(0).getUnit();
        return DistanceUnit.fromString(projectionUnit.toString());
    }
}
