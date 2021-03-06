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
package ijfx.core.batch.item;

import ijfx.core.image.SilentImageDisplay;
import ijfx.core.metadata.MetaData;
import ijfx.core.metadata.MetaDataSet;
import ijfx.explorer.datamodel.Explorable;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class ExplorableBatchInputWrapper extends  AbstractLoaderWrapper<Explorable>{


    public ExplorableBatchInputWrapper(Explorable wrappedObject) {
        super(wrappedObject);
    }

    @Override
    public void load() {
        setDataset(getWrappedValue().getDataset());
        setDisplay(new SilentImageDisplay(getContext(), getDataset()));
    }

    @Override
    public String getName() {
        return getWrappedValue().getTitle();
    }

    @Override
    public String getSourceFile() {
        return getWrappedValue()
                .getMetaDataSet()
                .getOrDefault(MetaData.ABSOLUTE_PATH,MetaData.NULL)
                .getStringValue();
    }
    
    @Override
    public void save() {
        super.save();
        
    } 
    
  
    public String getDefaultSaveName() {
        MetaData m = getWrappedValue().getMetaDataSet().get(MetaData.SAVE_NAME);
        if( m== null || m == MetaData.NULL) {
            return null;
        }
        else {
            return m.getStringValue();
        }
    }
    
    @Override
    public MetaDataSet getMetaDataSet() {
        return getWrappedValue().getMetaDataSet();
    }
    
}
