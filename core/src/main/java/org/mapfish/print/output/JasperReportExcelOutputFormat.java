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

package org.mapfish.print.output;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import java.io.OutputStream;

/**
 * An PDF output format that uses Jasper reports to generate the result.
 *
 * @author Jesse
 * @author sbrunner
 */
public final class JasperReportExcelOutputFormat extends AbstractJasperReportOutputFormat implements OutputFormat {

    @Override
    public String getContentType() {
        return "application/vnd.ms-excel";
    }

    @Override
    public String getFileSuffix() {
        return "xls";
    }

    @Override
    protected void doExport(final OutputStream outputStream, final Print print) throws JRException {
        JRXlsExporter exporter = new JRXlsExporter();

        exporter.setExporterInput(new SimpleExporterInput(print.print));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        exporter.exportReport();
    }
}
