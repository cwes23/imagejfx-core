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

import org.scijava.display.Display;
import ijfx.core.formats.Script;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.IndexRange;
import org.scijava.script.ScriptLanguage;

/**
 *
 * @author cyril
 */

public interface ScriptDisplay extends Display<Script> {
    
    final StringProperty selectedTextProperty = new SimpleStringProperty();
    final StringProperty textProperty= new SimpleStringProperty();
    ObjectProperty<IndexRange> selectionProperty = new SimpleObjectProperty<>();
    
    ScriptLanguage getLanguage();
    
    void setLanguage(ScriptLanguage language);
    void editText(String newValue);
    void copyText ();
    void pasteText ();
    void undo();
    void redo();
    
    void setSelectedText(String text);
    StringProperty selectedTextProperty();
    StringProperty textProperty();
    ObjectProperty<IndexRange> selectionProperty();
    void setText(ObservableValue textValue);
    void setSelection(IndexRange indexRange);
    String getText();
    
}