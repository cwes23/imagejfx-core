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
package ijfx.ui.mainwindow;

import ijfx.ui.activity.DisplayContainer;
import org.scijava.plugin.Plugin;
import ijfx.core.uiplugin.UiCommand;

/**
 * Launch the activity that contains all the ImageJ displays
 * @author cyril
 */
@Plugin(type = UiCommand.class,label = "Visiualize", priority= 100,iconPath = "fa:picture_alt")
public class VisualizeUiCommand extends AbstractActivityLauncher{
    public VisualizeUiCommand() {
        super(DisplayContainer.class);
    }
}