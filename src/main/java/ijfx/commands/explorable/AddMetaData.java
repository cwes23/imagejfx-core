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
package ijfx.commands.explorable;

import ijfx.core.metadata.MetaData;
import ijfx.explorer.datamodel.Explorable;
import ijfx.ui.inputharvesting.TextWidgetFX;
import java.util.List;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Cyril MONGIS
 */
@Plugin(type = ExplorableDisplayCommand.class, label="Add",iconPath="fa:plus",initializer = "init",priority = 100)
public class AddMetaData extends AbstractExplorableDisplayCommand{

   
    @Parameter(label = "Key")
    String key;
    
    @Parameter(label = "Value")
    String value;
    
   
    
    @Override
    public void run(List<? extends Explorable> items) {

        MetaData m = MetaData.create(key, value);
        items.forEach(item->item.getMetaDataSet().put(m));
        
        display.update();
        
    }
    
    
    
    
    
}
