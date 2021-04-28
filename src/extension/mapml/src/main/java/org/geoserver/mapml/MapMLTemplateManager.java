/* (c) 2021 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.mapml;
import freemarker.template.Template;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.wfs.json.GeoJSONBuilder;
import org.geoserver.wfs.json.GeoJSONGetFeatureResponse;
import org.geoserver.wfs.request.FeatureCollectionResponse;
import org.geoserver.wms.GetFeatureInfoRequest;
import org.geoserver.wms.WMS;
import org.geoserver.wms.featureinfo.FreeMarkerTemplateManager;
import org.geotools.feature.FeatureCollection;

/**
 *
 * @author prushforth
 */
public class MapMLTemplateManager extends FreeMarkerTemplateManager {

    public MapMLTemplateManager(
            OutputFormat format, WMS wms, GeoServerResourceLoader resourceLoader) {
        super(format, wms, resourceLoader);
    }
    
    @Override
    protected boolean templatesExist(Template header, Template footer, List<FeatureCollection> collections)
            throws IOException {
        if (header == null || footer == null) return false;
        else {
            for (FeatureCollection fc : collections) {
                Template content = getContentTemplate(fc, wms.getCharSet());
                if (content != null) return true;
            }
        }
        return false;
    }
    
    @Override
    protected String getTemplateFileName(String filename) {
        return filename + "_mapml.ftl";
    }

    @Override
    protected void handleContent(List<FeatureCollection> collections,
            OutputStreamWriter osw,
            GetFeatureInfoRequest request)
            throws IOException {
        for (int i = 0; i < collections.size(); i++) {
            FeatureCollection fc = collections.get(i);
            Template content = getContentTemplate(fc, wms.getCharSet());
            if (content == null) {
                handleMapMLWithoutTemplate(fc, osw);
            } else {
                String typeName = request.getQueryLayers().get(i).getName();
                processTemplate(typeName, fc, content, osw);
            }
        }
    }
    // this is forked from GeoJSONTemplateManager.  The GeoJSONGetFeatureResponse 
    // is structured differently than the MapMLGetFeatureOutputFormat.
    
    // for one thing, all the coordinate handling, including rounding and forced decimal 
    // notation is handled by the (non-static) GeoJSONBuilder class, whereas this
    // operation is handled by the MapMLGenerator class, which has recently been
    // refactored to have mostly non-static members so that the numDecimals and
    // force decimals settings can be passed in and used.  However, it looks like
    // the ftl system requires that such methods be static only, which I think
    // presents a problem: how to obtain and use the non-static numDecimals and
    // forceDecimal metadata in the static environs of ftl templates.
    private void handleMapMLWithoutTemplate(FeatureCollection collection, OutputStreamWriter osw)
            throws IOException {
        MapMLGetFeatureOutputFormat format =
                new MapMLGetFeatureOutputFormat(wms.getGeoServer());
        //boolean isComplex = collection.getSchema() instanceof SimpleFeatureType;
        //Writer outWriter = new BufferedWriter(osw);
        //final GeoJSONBuilder jsonWriter = new GeoJSONBuilder(outWriter);
        if (collection instanceof FeatureCollectionResponse) {
            format.write((FeatureCollectionResponse) collection, osw, null);
        }
    }
    
}
