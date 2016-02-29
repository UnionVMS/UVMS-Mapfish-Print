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

import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;
import org.mapfish.print.config.access.AccessAssertion;
import org.mapfish.print.config.access.AccessAssertionPersister;
import org.mapfish.print.servlet.registry.Registry;

import java.util.Date;

/**
 * Represent a print job that has completed.  Contains the information about the print job.
 * @author jesseeichar on 3/18/14.
 */
public abstract class PrintJobStatus {
    /**
     * prefix for storing metadata about a job in the registry.
     */
    private static final String RESULT_METADATA = "resultMetadata_";

    private static final String JSON_APP = "appId";
    private static final String JSON_FILENAME = "fileName";
    private static final String JSON_SUCCESS = "success";
    private static final String JSON_ACCESS_ASSERTION = "access";
    private static final String JSON_START_DATE = "startDate";
    private static final String JSON_COMPLETION_DATE = "completionDate";
    private static final String JSON_REQUEST_COUNT = "requestCount";
    private final String referenceId;
    private final String appId;
    private final String fileName;
    private Date startDate;
    private Date completionDate;
    private long requestCount;
    private final AccessAssertion access;

    /**
     * Constructor.
     *
     * @param referenceId reference of the report.
     * @param appId       the appId used for loading the configuration.
     * @param fileName    the fileName to send to the client.
     * @param startDate the time when the print job started.
     * @param completionDate the time when the print job ended.
     * @param requestCount the total number of requests made when the job was submitted.
     * @param accessAssertion the an access control object for downloading this report.  Typically this is combined access of the
     *                        template and the configuration.
     */
    public PrintJobStatus(final String referenceId, final String appId, final Date startDate, final Date completionDate,
            final long requestCount, final String fileName, final AccessAssertion accessAssertion) {
        this.referenceId = referenceId;
        this.appId = appId;
        this.startDate = startDate;
        this.completionDate = completionDate;
        this.requestCount = requestCount;
        this.fileName = fileName;
        this.access = accessAssertion;
    }

    /**
     * Assert that the current is authorized to access this job.
     */
    public final void assertAccess() {
        this.access.assertAccess(
                getClass().getSimpleName() + " for app '" + this.appId + "' for print job '" + this.referenceId + "'", this);
    }

    /**
     * Store the data of a print job in the registry.
     *
     * @param registry the registry to writer to
     * @param persister a persister for converting the access assertion to json
     */
    public final void store(final Registry registry, final AccessAssertionPersister persister) throws JSONException {
        JSONObject metadata = new JSONObject();
        metadata.put(JSON_APP, this.appId);
        metadata.put(JSON_FILENAME, this.fileName);
        metadata.put(JSON_SUCCESS, this instanceof SuccessfulPrintJob);
        metadata.put(JSON_START_DATE, this.startDate.getTime());
        metadata.put(JSON_REQUEST_COUNT, this.requestCount);
        if (this.completionDate != null) {
            metadata.put(JSON_COMPLETION_DATE, this.completionDate.getTime());
        }
        metadata.put(JSON_ACCESS_ASSERTION, persister.marshal(this.access));
        addExtraParameters(metadata);
        registry.put(RESULT_METADATA + this.referenceId, metadata);
    }

    /**
     * Add extra information to the metadata object being put in the registry.
     *
     * @param metadata the json object that contains the metadata
     */
    protected abstract void addExtraParameters(JSONObject metadata) throws JSONException;

    /**
     * Construct a print job by reading the data from a registry.
     *
     * @param referenceId the reference id of the report to get information about.
     * @param registry    the registry to read from.
     * @param persister   the access persister to use for reading the access data from the registry
     */
    public static Optional<? extends PrintJobStatus> load(final String referenceId, final Registry registry,
                                                          final AccessAssertionPersister persister)
            throws JSONException, NoSuchReferenceException {
        if (registry.containsKey(RESULT_METADATA + referenceId)) {
            JSONObject metadata = registry.getJSON(RESULT_METADATA + referenceId);

            String appId = metadata.optString(JSON_APP, null);
            Date startDate = new Date(metadata.getLong(JSON_START_DATE));
            long requestCount = metadata.getLong(JSON_REQUEST_COUNT);

            JSONObject accessJSON = metadata.getJSONObject(JSON_ACCESS_ASSERTION);
            final AccessAssertion accessAssertion = persister.unmarshal(accessJSON);

            PrintJobStatus report;
            if (!metadata.has(JSON_COMPLETION_DATE)) {
                report = PendingPrintJob.load(metadata, referenceId, appId, startDate, requestCount, accessAssertion);
            } else {
                String fileName = metadata.getString(JSON_FILENAME);
                Date completionDate = new Date(metadata.getLong(JSON_COMPLETION_DATE));
                if (metadata.getBoolean(JSON_SUCCESS)) {
                    report = SuccessfulPrintJob.load(
                            metadata, referenceId, appId, startDate, completionDate, requestCount, fileName, accessAssertion);
                } else {
                    report = FailedPrintJob.load(
                            metadata, referenceId, appId, startDate, completionDate, requestCount, fileName, accessAssertion);
                }
            }
            return Optional.of(report);
        } else {
            throw new NoSuchReferenceException("invalid reference '" + referenceId + "'");
        }
    }

    public final String getReferenceId() {
        return this.referenceId;
    }

    public final String getAppId() {
        return this.appId;
    }

    public final String getFileName() {
        return this.fileName;
    }

    public final Date getStartDate() {
        return this.startDate;
    }

    public final void setCompletionDate(final Date completionDate) {
        this.completionDate = completionDate;
    }

    public final Date getCompletionDate() {
        return this.completionDate;
    }

    public final long getRequestCount() {
        return this.requestCount;
    }

    public final AccessAssertion getAccess() {
        return this.access;
    }

    /**
     * Get the elapsed time in ms.
     */
    public final long getElapsedTime() {
        if (this.completionDate == null) {
            return new Date().getTime() - this.startDate.getTime();
        } else {
            return this.completionDate.getTime() - this.startDate.getTime();
        }
    }
}
