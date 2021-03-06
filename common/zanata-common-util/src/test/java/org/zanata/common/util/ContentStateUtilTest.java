/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.zanata.common.ContentState.*;

import org.junit.Test;
import org.zanata.common.ContentState;

public class ContentStateUtilTest {
    @Test
    public void testAll() {
        test(New, list(""), New);
        test(New, list("", ""), New);
        test(New, list("a", ""), New);
        test(New, list("", "a"), New);
        test(New, list("a"), NeedReview);
        test(New, list("a", "b"), NeedReview);

        test(NeedReview, list(""), New);
        test(NeedReview, list("", ""), New);
        test(NeedReview, list("a", ""), NeedReview);
        test(NeedReview, list("", "a"), NeedReview);
        test(NeedReview, list("a"), NeedReview);
        test(NeedReview, list("a", "b"), NeedReview);

        test(Approved, list(""), New);
        test(Approved, list("", ""), New);
        test(Approved, list("a", ""), New);
        test(Approved, list("", "a"), New);
        test(Approved, list("a"), Approved);
        test(Approved, list("a", "b"), Approved);
    }

    private void test(ContentState requestedState, List<String> contents,
            ContentState expectedState) {
        List<String> warnings = new ArrayList<String>();
        ContentState actualState =
                ContentStateUtil.determineState(requestedState, contents,
                        "resId", warnings);
        if (requestedState != expectedState) {
            assertFalse(warnings.isEmpty());
        }
        assertEquals(actualState, expectedState);
    }

    private static List<String> list(String... s) {
        return Arrays.asList(s);
    }

}
