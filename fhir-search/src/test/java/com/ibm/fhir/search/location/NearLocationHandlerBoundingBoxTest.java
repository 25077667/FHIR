/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.search.location;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.ibm.fhir.model.resource.Location;
import com.ibm.fhir.search.context.FHIRSearchContext;
import com.ibm.fhir.search.exception.FHIRSearchException;
import com.ibm.fhir.search.location.bounding.Bounding;
import com.ibm.fhir.search.location.bounding.BoundingBox;
import com.ibm.fhir.search.location.bounding.BoundingType;
import com.ibm.fhir.search.util.SearchUtil;

/**
 * Test the BoundingBox
 * Verified using https://www.geodatasource.com/distance-calculator
 */
public class NearLocationHandlerBoundingBoxTest {

    @Test
    public void testLocationBoundaryExample() throws FHIRSearchException {
        double latitude = 40;
        double longitude = -40;
        double distance = 1000.0;
        String unit = "km";
        NearLocationHandler handler = new NearLocationHandler();
        BoundingBox boundingBox = handler.createBoundingBox(latitude, longitude, distance, unit);
        assertNotNull(boundingBox);
        assertEquals(boundingBox.getMinLatitude(), 30.98662700557291);
        assertEquals(boundingBox.getMinLongitude(), -51.7266732000822);
        assertEquals(boundingBox.getMaxLatitude(), 49.01337299442709);
        assertEquals(boundingBox.getMaxLongitude(), -28.2733267999178);
        assertEquals(boundingBox.getType(), BoundingType.BOX);

        // The resulting diagnoal is ~2807.48 km
        // The side of the square is 1986 km.
        // The error is roughly .7% 
    }

    @Test
    public void testLocationBoundaryOrigin() throws FHIRSearchException {
        double latitude = 0;
        double longitude = 0;
        double distance = 10.0;
        String unit = "km";
        NearLocationHandler handler = new NearLocationHandler();
        BoundingBox boundingBox = handler.createBoundingBox(latitude, longitude, distance, unit);
        assertNotNull(boundingBox);
        assertEquals(boundingBox.getMinLatitude(), -0.09013372994427096);
        assertEquals(boundingBox.getMinLongitude(), -0.08983152841195216);
        assertEquals(boundingBox.getMaxLatitude(), 0.09013372994427096);
        assertEquals(boundingBox.getMaxLongitude(), 0.08983152841195216);
        assertEquals(boundingBox.getType(), BoundingType.BOX);
    }

    @Test
    public void testLocationBoundaryOriginNoDistance() throws FHIRSearchException {
        double latitude = 0;
        double longitude = 0;
        double distance = 0.0;
        String unit = "km";
        NearLocationHandler handler = new NearLocationHandler();
        BoundingBox boundingBox = handler.createBoundingBox(latitude, longitude, distance, unit);
        assertNotNull(boundingBox);
        assertEquals(boundingBox.getMinLatitude(), 0.0);
        assertEquals(boundingBox.getMinLongitude(), 0.0);
        assertEquals(boundingBox.getMaxLatitude(), 0.0);
        assertEquals(boundingBox.getMaxLongitude(), 0.0);
        assertEquals(boundingBox.getType(), BoundingType.BOX);
    }

    @Test
    public void testLocationBoundaryNorthPole() throws FHIRSearchException {
        double latitude = 90;
        double longitude = 0;
        double distance = 1.0;
        String unit = "km";
        NearLocationHandler handler = new NearLocationHandler();
        BoundingBox boundingBox = handler.createBoundingBox(latitude, longitude, distance, unit);
        assertNotNull(boundingBox);
        // At the high latitudes it's going to cover most of the area. 
        assertEquals(boundingBox.getMinLatitude(), 89.99098662700558);
        assertEquals(boundingBox.getMinLongitude(), -180.0);
        assertEquals(boundingBox.getMaxLatitude(), 90.0);
        assertEquals(boundingBox.getMaxLongitude(), 180.0);
        assertEquals(boundingBox.getType(), BoundingType.BOX);
    }

    @Test
    public void testLocationBoundarySouthPole() throws FHIRSearchException {
        double latitude = -90;
        double longitude = 0;
        double distance = 1.0;
        String unit = "km";
        NearLocationHandler handler = new NearLocationHandler();
        BoundingBox boundingBox = handler.createBoundingBox(latitude, longitude, distance, unit);
        assertNotNull(boundingBox);
        // At the low latitudes it's going to cover most of the area. 
        assertEquals(boundingBox.getMinLatitude(), -90.0);
        assertEquals(boundingBox.getMinLongitude(), -180.0);
        assertEquals(boundingBox.getMaxLatitude(), -89.99098662700558);
        assertEquals(boundingBox.getMaxLongitude(), 180.0);
        assertEquals(boundingBox.getType(), BoundingType.BOX);
    }

    @Test(expectedExceptions = { FHIRSearchException.class })
    public void testLocationBoundaryBadUnit() throws FHIRSearchException {
        double latitude = -90;
        double longitude = 0;
        double distance = 1.0;
        String unit = "FUDGE";
        NearLocationHandler handler = new NearLocationHandler();
        handler.createBoundingBox(latitude, longitude, distance, unit);
    }

    @Test
    public void testLocationBoundaryPositionsFromParameters() throws Exception {
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>(1);
        queryParms.put("near", Collections.singletonList("-90.0|0.0|1.0|km"));
        FHIRSearchContext ctx = SearchUtil.parseQueryParameters(Location.class, queryParms, true);
        NearLocationHandler handler = new NearLocationHandler();
        List<Bounding> bounding = handler.generateLocationPositionsFromParameters(ctx.getSearchParameters());
        assertNotNull(bounding);

        BoundingBox boundingBox = (BoundingBox) bounding.get(0);
        // At the low latitudes it's going to cover most of the area. 
        assertEquals(boundingBox.getMinLatitude(), -90.0);
        assertEquals(boundingBox.getMinLongitude(), -180.0);
        assertEquals(boundingBox.getMaxLatitude(), -89.99098662700558);
        assertEquals(boundingBox.getMaxLongitude(), 180.0);
        assertEquals(boundingBox.getType(), BoundingType.BOX);
    }
}