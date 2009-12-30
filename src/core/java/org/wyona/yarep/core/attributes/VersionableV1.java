/*
 * Copyright 2009 Wyona
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.wyona.org/licenses/APACHE-LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wyona.yarep.core.attributes;

import org.wyona.yarep.core.Revision;

import java.util.Date;

/**
 * DEV (Not released yet, this interface is still subject to change!)
 */
public interface VersionableV1 {

    /**
     * Get revision of a node for a specific date (or just before)
     * @param date Point in time (or just before) for which a revision shall be returned
     * @return If no revision can be found for the specified point in time (or just before), then null shall be returned
     */
    public Revision getRevision(Date date) throws Exception;
}
