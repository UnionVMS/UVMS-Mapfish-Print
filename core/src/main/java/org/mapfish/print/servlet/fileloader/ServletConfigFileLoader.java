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

package org.mapfish.print.servlet.fileloader;

import javax.servlet.ServletContext;
import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableListIterator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A plugin that loads the config resources from urls starting with prefix:
 * {@value org.mapfish.print.servlet.fileloader.ServletConfigFileLoader#PREFIX}://.
 *
 * @author Jesse on 4/28/2014.
 */
public final class ServletConfigFileLoader extends AbstractFileConfigFileLoader {

    private static final String PREFIX = "servlet";
    private static final int PREFIX_LENGTH = (PREFIX + "://").length();

    @Autowired
    private ServletContext servletContext;

    @Override
    protected Iterator<File> resolveFiles(final URI fileURI) {
        if (fileURI.getScheme() != null && fileURI.getScheme().equals("file") && new File(fileURI).exists()) {
            return Iterators.singletonIterator(new File(fileURI));
        }
        if (fileURI.toString().startsWith(PREFIX)) {
            String path = fileURI.toString().substring(PREFIX_LENGTH);
            final String realPath = this.servletContext.getRealPath(path);
            if (realPath == null) {
                return new UnmodifiableListIterator<File>() {
                    public boolean hasNext() {
                        return false;
                    }

                    public File next() {
                        throw new NoSuchElementException();
                    }

                    public boolean hasPrevious() {
                        return false;
                    }

                    public File previous() {
                        throw new NoSuchElementException();
                    }

                    public int nextIndex() {
                        return 0;
                    }

                    public int previousIndex() {
                        return -1;
                    }
                };
            }
            return Iterators.singletonIterator(new File(realPath));
        }
        return new UnmodifiableListIterator<File>() {
            public boolean hasNext() {
                return false;
            }

            public File next() {
                throw new NoSuchElementException();
            }

            public boolean hasPrevious() {
                return false;
            }

            public File previous() {
                throw new NoSuchElementException();
            }

            public int nextIndex() {
                return 0;
            }

            public int previousIndex() {
                return -1;
            }
        };
    }

    @Override
    public String getUriScheme() {
        return PREFIX;
    }

    public void setServletContext(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
