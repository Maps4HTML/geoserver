/* (c) 2021 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.mapml;

import freemarker.template.Template;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.logging.Logger;
import net.opengis.wfs.FeatureCollectionType;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.platform.ServiceException;
import org.geoserver.wms.GetFeatureInfoRequest;
import org.geoserver.wms.WMS;
import org.geoserver.wms.featureinfo.FreeMarkerTemplateManager;
import org.geoserver.wms.featureinfo.GetFeatureInfoOutputFormat;
import org.geotools.feature.FeatureCollection;
import org.geotools.util.logging.Logging;

/** @author prushforth */
public class MapMLFeatureInfoResponse extends GetFeatureInfoOutputFormat {
    private static final Logger LOGGER = Logging.getLogger("org.geoserver.mapml");
    private static final String FORMAT = MapMLConstants.MAPML_MIME_TYPE;
    private FreeMarkerTemplateManager templateManager;
    private WMS wms;

    public MapMLFeatureInfoResponse(final WMS wms, GeoServerResourceLoader resourceLoader) {
        super(FORMAT);
        this.wms = wms;
        this.templateManager =
                new MapMLTemplateManager(
                        FreeMarkerTemplateManager.OutputFormat.MAPML, wms, resourceLoader);
    }

    /**
     * Writes the document
     *
     * @param out The output stream to write to.
     * @throws ServiceException For problems with geoserver
     * @throws java.io.IOException For problems writing the output.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void write(
            FeatureCollectionType features, GetFeatureInfoRequest fInfoReq, OutputStream out)
            throws IOException {
        boolean usedTemplates = false;

        if (templateManager != null)
            // check before if there are free marker templates to customize response
            usedTemplates = templateManager.write(features, fInfoReq, out);

        if (!usedTemplates) {
            MapMLGetFeatureOutputFormat format =
                    new MapMLGetFeatureOutputFormat(wms.getGeoServer());
            
            // this is going to require some thinking about how to re-implement
            // the write method so that it can work with a null Operation
            format.write(features, out, null);
        }
    }

    @Override
    public String getCharset() {
        // MapML is always encoded as UTF-8; SettingsInfo.getCharset() notwithstanding
        return "UTF-8";
    }

    public FreeMarkerTemplateManager getTemplateManager() {
        return templateManager;
    }

}
