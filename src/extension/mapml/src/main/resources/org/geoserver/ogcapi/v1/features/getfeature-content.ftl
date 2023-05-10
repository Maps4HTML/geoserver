<#-- put this file in $GEOSERVER_DATA_DIR/templates. Make sure your build includes
     the MapML extension -->
    <#-- 
    Body section of the GetFeature template, it's provided with one feature collection, and
    will be called multiple times if there are various feature collections
    -->

  <#if collection??>
    <#-- Expended only in OGC Features -->
    <h1><a href="${serviceLink("/collections/${collection}")}">${data.type.name}</a></h1>
  <#else>
    <h1>${data.type.name}</h1>
  </#if>

  <div class="row">
    <div class="col-xs col-lg-8">
      <div class="table-responsive-xs">
        <table class="table table-striped table-hover table-bordered">
          <thead>
            <tr>
              <th>fid</th>
            <#list data.type.attributes as attribute>
              <#if !attribute.isGeometry>
                <th>${attribute.name}</th>
              </#if>
            </#list>
            </tr>
          </thead>
          <tbody>
            <#list data.features as feature>
            <tr>
              <td>${feature.fid}</td>    
              <#list feature.attributes as attribute>
                <#if !attribute.isGeometry>
                  <td>${attribute.value?string}</td>
                </#if>
              </#list>
              </tr>
            </#list>
          </tbody>
        </table>
      </div>
    </div>
    <div class="col-xs col-lg-4 text-center p-5">
    <#assign lon  = featureInfo.getLatLonBoundingBox().centre().x>
    <#assign lat  = featureInfo.getLatLonBoundingBox().centre().y>
      <mapml-viewer style="width: 100%; height: 400px" controls zoom="2" projection="OSMTILE" lat="${lat}" lon="${lon}">
        <layer- label="OpenStreetMap" checked>
            <map-link rel="license" href="https://www.openstreetmap.org/copyright" title="© OpenStreetMap contributors CC BY-SA"></map-link>
            <map-extent units="OSMTILE">
                <map-input name="z" type="zoom" value="18" min="0" max="18"></map-input>
                <map-input name="x" type="location" units="tilematrix" axis="column" min="0" max="262144"></map-input>
                <map-input name="y" type="location" units="tilematrix" axis="row" min="0" max="262144"></map-input>
                <map-link rel="tile" tref="https://tile.openstreetmap.org/{z}/{x}/{y}.png"></map-link>
            </map-extent>
        </layer->
        <layer- id="${data.type.name}" label="${data.type.name}" checked>
            <map-meta name="projection" content="OSMTILE"></map-meta>
            <#assign upperLeft  = featureInfo.getLatLonBoundingBox().getUpperCorner()>
            <#assign lowerRight = featureInfo.getLatLonBoundingBox().getLowerCorner()>
            <map-meta name="extent" content="top-left-longitude=${upperLeft.x}, top-left-latitude=${upperLeft.y}, bottom-right-longitude=${lowerRight.x}, bottom-right-latitude=${lowerRight.y}"></map-meta>
            <#list data.features as feature>
              <map-feature>
                <map-featurecaption>Feature ${feature.fid}</map-featurecaption>
                <map-properties>
                  <#list feature.attributes as attribute>
                    <#if !attribute.isGeometry>
                      <p>${attribute.value?string}</p>
                    </#if>
                  </#list>
                </map-properties>
                <map-geometry cs="gcrs">
                  <#list feature.attributes as attribute>
                    <#if attribute.isGeometry>
                        <#if attribute.rawValue?string?trim?starts_with("MULTIPOINT")>
                            <map-multipoint>
                                <#assign points = attribute.rawValue?matches("\\((-?\\d+\\.-?\\d+).*?(\\))")>
                                    <map-coordinates>
                                       <#list points as pointstring>
                                         ${pointstring?replace(",","")?replace("(","")?replace(")","")}
                                       </#list>
                                    </map-coordinates>
                            </map-multipoint>
                        <#elseif attribute.rawValue?string?trim?starts_with("MULTILINESTRING")>  
                            <map-multilinestring>
                                <#-- linestrings matches each set of coordinates, including parentheses -->
                                <#assign linestrings = attribute.rawValue?matches("\\((-?\\d+\\.-?\\d+).*?(\\))")>
                                <#list linestrings as line>
                                    <map-coordinates>
                                        ${line?replace(",","")?replace("(","")?replace(")","")}
                                    </map-coordinates>
                                </#list>
                            </map-multilinestring>
                        <#elseif attribute.rawValue?string?trim?starts_with("MULTIPOLYGON")>
                            <map-multipolygon>
                                <#-- this pattern won't work for polygons with holes, I believe. Need sample data to test -->
                                <#assign polygons = attribute.rawValue?matches("\\((-?\\d+\\.-?\\d+).*?(\\))")>
                                <#list polygons as polygon>
                                    <map-polygon>
                                        <#-- boundaries matches each set of coordinates, including parentheses -->
                                        <#assign boundaries = polygon?matches("\\((-?\\d+\\.-?\\d+).*?(\\))")>
                                        <#list boundaries as boundary>
                                            <map-coordinates>
                                                ${boundary?replace(",","")?replace("(","")?replace(")","")}
                                            </map-coordinates>
                                        </#list>
                                    </map-polygon>
                                </#list>
                            </map-multipolygon>
                        <#elseif attribute.rawValue?string?trim?starts_with("POINT")>
                            <map-point><map-coordinates>
                                ${attribute.rawValue?matches(".*\\(((-?\\d+\\.\\d+).*?)\\)")?groups[1]}
                            </map-coordinates></map-point>
                        <#elseif attribute.rawValue?string?trim?starts_with("LINESTRING")>  
                            <map-linestring><map-coordinates>
                                ${attribute.rawValue?matches(".*\\(((-?\\d+\\.-?\\d+).*?)(\\)*?)")?groups[1]?replace(",","")}
                            </map-coordinates></map-linestring>
                        <#elseif attribute.rawValue?string?trim?starts_with("POLYGON")>  
                            <map-polygon>
                                <#-- boundaries matches each set of coordinates, including parentheses -->
                                <#assign boundaries = attribute.rawValue?matches("\\((-?\\d+\\.-?\\d+).*?(\\))")>
                                <#list boundaries as boundary>
                                    <map-coordinates>
                                        ${boundary?replace(",","")?replace("(","")?replace(")","")}
                                    </map-coordinates>
                                </#list>
                            </map-polygon>
                        <#elseif attribute.rawValue?string?trim?starts_with("GEOMETRYCOLLECTION")>  
                            <map-geometrycollection><map-coordinates>
                                    ${attribute.rawValue?string}
                            </map-coordinates></map-geometrycollection>
                        </#if>
                    </#if>
                  </#list>
                 </map-geometry>
              </map-feature>
            </#list>
        </layer->
      </mapml-viewer>
    </div>
  </div>
<script>
addEventListener("DOMContentLoaded", (event) => {document.querySelector('#${data.type.name}').zoomTo()});
</script>