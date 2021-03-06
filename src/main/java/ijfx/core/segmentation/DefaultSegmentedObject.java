/*
    This file is part of ImageJ FX.

    ImageJ FX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ImageJ FX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
    
     Copyright 2015,2016 Cyril MONGIS, Michael Knop
	
 */
package ijfx.core.segmentation;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import ijfx.core.image.DatasetUtilsService;
import ijfx.core.image.ImagePlaneService;
import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.core.metadata.MetaDataSetType;
import ijfx.core.metadata.MetaDataSetUtils;
import ijfx.core.overlay.OverlayStatService;
import ijfx.core.overlay.OverlayStatistics;
import ijfx.explorer.datamodel.AbstractTaggable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.overlay.Overlay;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultSegmentedObject extends AbstractTaggable implements SegmentedObject {

    @Parameter
    DatasetUtilsService datasetUtilsService;

    @Parameter
    ImagePlaneService imagePlaneService;

    RandomAccessibleInterval<? extends RealType> source;
    private Overlay overlay;
    private MetaDataSet set = new MetaDataSet();

    @Parameter
    private OverlayStatService overlayStatsService;

    public DefaultSegmentedObject() {
        set.setType(MetaDataSetType.OBJECT);
    }

    @JsonSetter(value = "overlay")
    public void setOverlay(Overlay overlay) {
        this.overlay = overlay;
        set.putGeneric(MetaData.NAME, overlay.getName());
    }

    @Override
    @JsonGetter(value = "overlay")
    public Overlay getOverlay() {
        return overlay;
    }

    @JsonSetter(value = "metadataset")
    public void setMetaDataSet(MetaDataSet set) {
        this.set = set;
    }

    @Override
    @JsonSetter("metadataset")
    public MetaDataSet getMetaDataSet() {
        return set;
    }

    public DefaultSegmentedObject(Overlay overlay, OverlayStatistics overlayStatistics) {
        this();
        overlay.getContext().inject(this);
        setOverlay(overlay);

        set.merge(overlayStatsService.getStatisticsAsMap(overlayStatistics));

    }

    @JsonIgnore
    public RandomAccessibleInterval<? extends RealType> getPixelSource() {

        if (source == null) {
            try {
                Dataset dataset = datasetUtilsService.openSource(this, true);
                long[] nonPlanarPosition = MetaDataSetUtils.getNonPlanarPosition(getMetaDataSet());
                source = imagePlaneService.planeView(dataset, nonPlanarPosition);
                
            } catch (IOException ex) {
                Logger.getLogger(DefaultSegmentedObject.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return source;
    }

    public <T extends RealType<T>> DefaultSegmentedObject setSource(RandomAccessibleInterval<T> interval) {
        this.source = interval;
        return this;
    }

}
