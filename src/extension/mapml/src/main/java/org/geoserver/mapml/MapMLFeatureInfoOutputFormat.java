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

/**
 *
 * @author prushforth
 */
public class MapMLFeatureInfoOutputFormat extends GetFeatureInfoOutputFormat {
    private static final Logger LOGGER = Logging.getLogger("org.geoserver.mapml");
    private static final String FORMAT = MapMLConstants.MAPML_MIME_TYPE;
    private FreeMarkerTemplateManager templateManager;
    private WMS wms;

    public MapMLFeatureInfoOutputFormat(final WMS wms, GeoServerResourceLoader resourceLoader) {
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
            FeatureCollectionType results, GetFeatureInfoRequest request, OutputStream out)
            throws ServiceException, IOException {
        templateManager.write(results, request, out);
    }
    
    @Override
    public String getCharset() {
        // MapML is always encoded as UTF-8; SettingsInfo.getCharset() notwithstanding
        return "UTF-8";
    }
  
    public FreeMarkerTemplateManager getTemplateManager() {
        return templateManager;
    }

    /** */
    private final class MapMLTemplateManager extends FreeMarkerTemplateManager {

        public MapMLTemplateManager(
                FreeMarkerTemplateManager.OutputFormat format, WMS wms, GeoServerResourceLoader resourceLoader) {
            super(format, wms, resourceLoader);
        }

        @Override
        protected boolean templatesExist(
                Template header, Template footer, List<FeatureCollection> collections)
                throws IOException {
            return true;
        }

        @Override
        protected void handleContent(
                List<FeatureCollection> collections,
                OutputStreamWriter osw,
                GetFeatureInfoRequest request)
                throws IOException {
            for (int i = 0; i < collections.size(); i++) {
                FeatureCollection fc = collections.get(i);
                Template content = getContentTemplate(fc, wms.getCharSet());
                String typeName = request.getQueryLayers().get(i).getName();
                processTemplate(typeName, fc, content, osw);
            }
        }

        @Override
        protected String getTemplateFileName(String filename) {
            return filename + ".ftl";
        }
    }
}
