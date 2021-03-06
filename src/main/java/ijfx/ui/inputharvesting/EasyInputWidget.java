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
package ijfx.ui.inputharvesting;

import javafx.beans.property.Property;
import javafx.scene.Node;
import org.scijava.widget.WidgetModel;

/**
 * Base for quickly create FXInputWidget.
 * This class focuses the most important part of the widget : 
 *  - create the node
 *  - choosing which property will be use as value
 *  - which type of object the will be handled
 * 
 * @author Cyril MONGIS
 */
public abstract class EasyInputWidget<T> extends AbstractFXInputWidget<T>{

    Node node;
    
    
    public void set(WidgetModel model) {
        super.set(model);
        
        node = createComponent();
        
        bindProperty(getProperty());
        
    }
    
    @Override
    public boolean supports(WidgetModel model) {
        return super.supports(model) && handles(model);
    }
    
    public abstract Property<T> getProperty();
    
    public abstract  Node createComponent();
    
    public abstract boolean handles(WidgetModel model);
    
    
    public Node getComponent() {
        return node;
    }
    
}
