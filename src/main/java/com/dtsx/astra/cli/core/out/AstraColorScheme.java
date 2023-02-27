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

/**
 * Astra User Interface color scheme
 */
public interface AstraColorScheme {

    /** Official Astra color. */
    AnsiColorRGB purple300 = new AnsiColorRGB(175, 110, 195);

    /** Official Astra color. */
    AnsiColorRGB purple500 = new AnsiColorRGB(110, 46, 164);

    /** Official Astra color. */
    AnsiColorRGB yellow300 = new AnsiColorRGB(223, 161, 67);

    /** Official Astra color. */
    AnsiColorRGB yellow500 = new AnsiColorRGB(162, 91, 39);

    /** Official Astra color. */
    AnsiColorRGB green300 = new AnsiColorRGB(128, 189, 244);

    /** Official Astra color. */
    AnsiColorRGB green500 = new AnsiColorRGB(61, 126, 64);

    /** Official Astra color. */
    AnsiColorRGB blue300 = new AnsiColorRGB(129, 163, 231);

    /** Official Astra color. */
    AnsiColorRGB blue500 = new AnsiColorRGB(46, 101, 211);

    /** Official Astra color. */
    AnsiColorRGB red300 = new AnsiColorRGB(221, 127, 135);

    /** Official Astra color. */
    AnsiColorRGB red500 = new AnsiColorRGB(199, 49, 44);

    /** Official Astra color. */
    AnsiColorRGB magenta400 = new AnsiColorRGB(239, 134, 180);

    /** Official Astra color. */
    AnsiColorRGB magenta600 = new AnsiColorRGB(191, 57, 111);

    /** Official Astra color. */
    AnsiColorRGB cyan400 = new AnsiColorRGB(91, 176, 248);

    /** Official Astra color. */
    AnsiColorRGB cyan600 = new AnsiColorRGB(48, 113, 189);

    /** Official Astra color. */
    AnsiColorRGB orange400 = new AnsiColorRGB(239, 137, 67);

    /** Official Astra color. */
    AnsiColorRGB orange600 = new AnsiColorRGB(173, 84, 31);

    /** Official Astra color. */
    AnsiColorRGB neutral300 = new AnsiColorRGB(167, 170, 173);

    /** Official Astra color. */
    AnsiColorRGB neutral500 = new AnsiColorRGB(108, 111, 115);

    /** Official Astra color. */
    AnsiColorRGB teal400 = new AnsiColorRGB(85, 186, 185);

    /** Official Astra color. */
    AnsiColorRGB teal600 = new AnsiColorRGB(53, 123, 120);
}
