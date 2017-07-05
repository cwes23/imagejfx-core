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
package ijfx.ui.display.code;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author florian
 */
public class TextEditorPreferencies {
    
    private String theme = "darkTheme";
    private List<String> listOfTheme = new ArrayList<>();
    @org.scijava.plugin.Parameter(label = "enable autocompletion")
    private boolean autocompletion = true;
    @org.scijava.plugin.Parameter(label = "enable side panel")
    private boolean sidePanel = true;
    private File customCSS = null;

    public TextEditorPreferencies() {
        this.listOfTheme.add("darkTheme");
        this.listOfTheme.add("lightTheme");
    }

    public String getTheme() {
        return theme;
    }

    public List<String> getListOfTheme() {
        return listOfTheme;
    }

    public boolean isAutocompletion() {
        return autocompletion;
    }

    public boolean isSidePanel() {
        return sidePanel;
    }

    public File getCustomCSS() {
        return customCSS;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void setListOfTheme(List<String> listOfTheme) {
        this.listOfTheme = listOfTheme;
    }

    public void setAutocompletion(boolean autocompletion) {
        this.autocompletion = autocompletion;
    }

    public void setSidePanel(boolean sidePanel) {
        this.sidePanel = sidePanel;
    }

    public void setCustomCSS(File customCSS) {
        this.customCSS = customCSS;
    }
    
    
    
    
}
