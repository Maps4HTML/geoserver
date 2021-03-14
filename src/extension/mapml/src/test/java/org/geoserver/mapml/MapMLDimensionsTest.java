/* (c) 2021 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.mapml;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.HashMap;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.DimensionInfo;
import org.geoserver.catalog.DimensionPresentation;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.MetadataMap;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.data.test.SystemTestData;
import org.geoserver.wms.WMSDimensionsTestSupport;
import org.geoserver.wms.WMSInfo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/** @author prushforth */
public class MapMLDimensionsTest extends WMSDimensionsTestSupport {
    private XpathEngine xpath;

    @Before
    public void setup() {
        HashMap<String, String> m = new HashMap<>();
        m.put("html", "http://www.w3.org/1999/xhtml/");

        NamespaceContext ctx = new SimpleNamespaceContext(m);
        XMLUnit.setXpathNamespaceContext(ctx);
        xpath = XMLUnit.newXpathEngine();
    }

    @Override
    protected void onSetUp(SystemTestData testData) throws Exception {
        super.onSetUp(testData);

        GeoServerInfo global = getGeoServer().getGlobal();
        global.getSettings().setProxyBaseUrl("");
        getGeoServer().save(global);

        WMSInfo wms = getGeoServer().getService(WMSInfo.class);
        wms.getSRS().add("EPSG:3857");
        getGeoServer().save(wms);
    }

    @Test
    public void testElevationList() throws Exception {
        setupVectorDimension(
                ResourceInfo.ELEVATION,
                "elevation",
                DimensionPresentation.LIST,
                null,
                UNITS,
                UNIT_SYMBOL);

        Catalog catalog = getCatalog();
        ResourceInfo layerMeta = catalog.getLayerByName(getLayerId(V_TIME_ELEVATION)).getResource();
        MetadataMap mm = layerMeta.getMetadata();
        mm.put("mapml.dimension", "elevation");
        catalog.save(layerMeta);
        assertTrue(layerMeta instanceof FeatureTypeInfo);
        FeatureTypeInfo typeInfo = (FeatureTypeInfo) layerMeta;
        DimensionInfo elevationInfo =
                typeInfo.getMetadata().get(ResourceInfo.ELEVATION, DimensionInfo.class);
        assertTrue(elevationInfo.isEnabled());

        String path = "mapml/" + getLayerId(V_TIME_ELEVATION) + "/osmtile/";
        org.w3c.dom.Document doc = getMapML(path);

        assertXpathEvaluatesTo("1", "count(//html:link[@rel='image'][@tref])", doc);
        URL url = new URL(xpath.evaluate("//html:link[@rel='image']/@tref", doc));
        HashMap<String, String> vars = parseQuery(url);
        assertTrue(vars.get("elevation").equalsIgnoreCase("{elevation}"));

        assertXpathEvaluatesTo("1", "count(//html:link[@rel='query'][@tref])", doc);
        url = new URL(xpath.evaluate("//html:link[@rel='query']/@tref", doc));
        vars = parseQuery(url);
        assertTrue(vars.get("elevation").equalsIgnoreCase("{elevation}"));

        assertXpathEvaluatesTo(
                "1", "count(//html:select[@name='elevation'][@id='elevation'])", doc);
        assertXpathEvaluatesTo(
                "4", "count(//html:select[@name='elevation'][@id='elevation']/html:option)", doc);
    }

    /**
     * Executes a request using the GET method and returns the result as an MapML document.
     *
     * @param path The portion of the request after the context, example:
     * @param query A map representing kvp to be used by the request.
     * @return A result of the request parsed into a dom.
     */
    protected org.w3c.dom.Document getMapML(final String path, HashMap<String, String> query)
            throws Exception {
        MockHttpServletRequest request = createRequest(path, query);
        request.addHeader("Accept", "text/mapml");
        request.setMethod("GET");
        request.setContent(new byte[] {});
        String resp = dispatch(request, "UTF-8").getContentAsString();
        return dom(new ByteArrayInputStream(resp.getBytes()), true);
    }
    /**
     * Executes a request using the GET method and returns the result as an MapML document.
     *
     * @param path The portion of the request after the context, example:
     * @return A result of the request parsed into a dom.
     */
    protected org.w3c.dom.Document getMapML(final String path) throws Exception {
        MockHttpServletRequest request = createRequest(path, false);
        request.addHeader("Accept", "text/mapml");
        request.setMethod("GET");
        request.setContent(new byte[] {});
        String resp = dispatch(request, "UTF-8").getContentAsString();
        return dom(new ByteArrayInputStream(resp.getBytes()), true);
    }

    private HashMap<String, String> parseQuery(URL url) {
        String[] variableValues = url.getQuery().split("&");
        HashMap<String, String> vars = new HashMap<>(variableValues.length);
        // int i = variableValues.length;
        for (String variableValue : variableValues) {
            String[] varValue = variableValue.split("=");
            vars.put(varValue[0], varValue.length == 2 ? varValue[1] : "");
        }
        return vars;
    }
}
