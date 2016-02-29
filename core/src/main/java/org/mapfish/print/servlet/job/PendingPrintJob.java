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

package org.mapfish.print.servlet.job;

import org.json.JSONException;
import org.json.JSONObject;
import org.mapfish.print.config.access.AccessAssertion;

import java.util.Date;

/**
 * Represents a pending print job.
 *
 */
public class PendingPrintJob extends PrintJobStatus {
    private static final String JSON_RUNNING = "running";
    private boolean running = false;

    /**
     * Constructor.
     *
     * @param referenceId    reference of the report.
     * @param appId          the appId used for loading the configuration.
     * @param startDate      the start date.
     * @param requestCount   the total number of requests made when the job was submitted.
     * @param access         the an access control object for downloading this report.  Typically this is combined access of the
     *                       template and the configuration.
     */
    public PendingPrintJob(
            final String referenceId, final String appId, final Date startDate, final long requestCount, final AccessAssertion access) {
        super(referenceId, appId, startDate, null, requestCount, null, access);
    }

    /**
     * Construct a new instance from the values provided.
     *
     * @param metadata       the metadata retrieved from the registry.  Only need it to get the extra information that is not stored by
     *                       parent class.
     * @param referenceId    reference of the report.
     * @param appId          the appId used for loading the configuration.
     * @param startDate      the start date.
     * @param requestCount   the total number of requests made when the job was submitted.
     * @param reportAccess   the an access control object for downloading this report.  Typically this is combined access of the
     *                        template and the configuration.
     */
    public static PendingPrintJob load(final JSONObject metadata, final String referenceId, final String appId,
                                       final Date startDate, final long requestCount, final AccessAssertion reportAccess)
            throws JSONException {
        PendingPrintJob job = new PendingPrintJob(referenceId, appId, startDate, requestCount, reportAccess);
        job.setRunning(metadata.getBoolean(JSON_RUNNING));
        return job;
    }

    @Override
    protected final void addExtraParameters(final JSONObject metadata) throws JSONException {
        metadata.put(JSON_RUNNING, this.running);
    }

    public final boolean isRunning() {
        return this.running;
    }

    public final void setRunning(final boolean running) {
        this.running = running;
    }
}
