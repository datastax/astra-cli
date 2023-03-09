package com.dtsx.astra.cli.streaming;

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

import com.github.rvesse.airline.annotations.Command;

/**
 * Display information relative to a tenant.
 */
@Command(name = ServiceStreaming.CMD_EXIST, description = "Show existence of a tenant")
public class StreamingExistCmd extends AbstractStreamingCmd {
    
    /** {@inheritDoc} */
    public void execute() {
        ServiceStreaming.getInstance().showTenantExistence(getTenant());
    }

}
