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
package ijfx.ui.plugin.statusbar;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author cyril
 */
@Plugin(type = Command.class, menuPath = "Plugins > Test > FXStatusBar")
public class StatusBarTest extends ContextCommand {

    @Parameter
    StatusService statusService;

    @Override
    public void run() {
        try {
            for (int i = 0; i != 100; i++) {

                statusService.showStatus(i + "/ " + 100);
                statusService.showProgress(i, 100);
                Thread.sleep(1000);

            }
        } catch (InterruptedException ex) {
            Logger.getLogger(StatusBarTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}