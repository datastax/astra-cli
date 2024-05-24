package com.dtsx.astra.cli.core.exception;

import java.io.Serial;

/*-
 * #%L
 * Astra CLI
 * --
 * Copyright (C) 2022 - 2023 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * Raise for invalid region name in the command
 */
public class InvalidRegionException extends InvalidArgumentException {

	@Serial
    private static final long serialVersionUID = -6922527090484644547L;

	/**
     * Default constructor.
     *
     * @param cloud
     *      current cloud
     * @param region
     *      current region
     */
    public InvalidRegionException(String cloud, String region) {
        super(String.format("Region '%s' is invalid for the cloud '%s'.", region, cloud));
    }

    /**
     * Constructo with only the region
     *
     * @param region
     *      current region
     */
    public InvalidRegionException(String region) {
        super(String.format("Region '%s' does not exist.", region));
    }

}
