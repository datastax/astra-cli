package com.dtsx.astra.cli.core.out;

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

import java.io.Serializable;

/**
 * List of Colors used in Astra Context.
 */
public enum AstraAnsiColors implements Serializable {

    /** Official Astra color. */
    PURPLE_300(175, 110, 195),
    /** Official Astra color. */
    PURPLE_500(110, 46, 164),
    /** Official Astra color. */
    YELLOW_300(223, 161, 67),

    /** Official Astra color. */
    YELLOW_500(162, 91, 39),

    /** Official Astra color. */
    GREEN_300(128, 189, 244),

    /** Official Astra color. */
    GREEN_500(61, 126, 64),

    /** Official Astra color. */
    BLUE_300(129, 163, 231),

    /** Official Astra color. */
    BLUE_500(46, 101, 211),

    /** Official Astra color. */
    RED_300(221, 127, 135),

    /** Official Astra color. */
    RED_500(199, 49, 44),

    /** Official Astra color. */
    MAGENTA_400(239, 134, 180),

    /** Official Astra color. */
    MAGENTA_600(191, 57, 111),

    /** Official Astra color. */
    CYAN_400(91, 176, 248),

    /** Official Astra color. */
    CYAN_600(48, 113, 189),

    /** Official Astra color. */
    ORANGE_400(239, 137, 67),

    /** Official Astra color. */
    ORANGE_600(173, 84, 31),

    /** Official Astra color. */
    NEUTRAL_300(167, 170, 173),

    /** Official Astra color. */
    NEUTRAL_500(108, 111, 115),

    /** Official Astra color. */
    TEAL_400(85, 186, 185),

    /** Official Astra color. */
    TEAL_600(53, 123, 120);

    private final int red;
    
    private final int green;
    
    private final int blue;

    /**
     * Core constructor.
     *
     * @param red
     *      red code
     * @param green
     *      green code
     * @param blue
     *      blue code
     */
    AstraAnsiColors(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * Getter accessor for attribute 'red'.
     * @return
     *      current value of 'red'
     */
    public int getRed() {
        return red;
    }

    /**
     * Getter accessor for attribute 'green'.
     * @return
     *      current value of 'green'
     */
    public int getGreen() {
        return green;
    }

    /**
     * Getter accessor for attribute 'blue'.
     * @return
     *      current value of 'blue'
     */
    public int getBlue() {
        return blue;
    }
}
