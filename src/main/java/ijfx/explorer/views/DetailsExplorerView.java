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
package ijfx.explorer.views;

import com.google.common.collect.Lists;
import ijfx.core.metadata.MetaData;
import ijfx.explorer.datamodel.Explorable;
import ijfx.explorer.widgets.ExplorerIconCell;
import ijfx.ui.display.annotation.DefaultAnnotationDialog;
import ijfx.ui.main.ImageJFX;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import mongis.utils.panecell.DataClickEvent;

import org.scijava.plugin.Plugin;

/**
 *
 * @author sapho
 */
@Plugin(type = ExplorerView.class, priority = 0.7, label = "Details", iconPath = "fa:table")
public class DetailsExplorerView extends BorderPane implements ExplorerView {

    @FXML
    private Button last, next;

    @FXML
    private Label title, subtitle;

    @FXML
    private ImageView imageView;

    @FXML
    private VBox leftVBox, rightVBox;

    @FXML
    private TableView<MetaData> tableView;

    @FXML
    private TableColumn<Explorable, String> keyColumn, valueColumn;

    private List<? extends Explorable> itemList;

    private Explorable currentItem;

    private final List<Explorable> currentItemList = new ArrayList<>();

    private Consumer<DataClickEvent<Explorable>> eventHandler;

    private List<? extends Explorable> selectedItems = new ArrayList<>();

    private static final String FXMLWAY = "/ijfx/ui/display/image/DetailsDisplay2.fxml";

    private ExplorerIconCell leftCell = new ExplorerIconCell();

    private ExplorerIconCell rightCell = new ExplorerIconCell();


    public DetailsExplorerView() {

        loadFXML();

        leftVBox.getChildren().add(leftCell);
        rightVBox.getChildren().add(rightCell);

        leftCell.onScreenProperty().setValue(Boolean.TRUE);
        rightCell.onScreenProperty().setValue(Boolean.TRUE);

        last.setOnAction(this::onDisplayPreviousExplorable);
        next.setOnAction(this::onDisplayNextExplorable);

        keyColumn.setCellValueFactory(
                new PropertyValueFactory<>("name"));

        valueColumn.setCellValueFactory(
                new PropertyValueFactory<>("value"));

    }

    @Override
    public Node getUIComponent() {
        return this;
    }

    @Override
    public void setItems(List<? extends Explorable> items) {

        this.itemList = items;

        refresh();
    }

    @Override
    public List<? extends Explorable> getSelectedItems() {
        if (currentItem == null) {
            return new ArrayList<>();
        } else {
            return Lists.newArrayList(currentItem);
        }
    }

    @Override
    public void setSelectedItem(List<? extends Explorable> itemList) {

        selectedItems = itemList;

        setData(selectedItems.get(0));

        if (selectedItems.size() > 0) {
            setCurrentItem(itemList.get(0));
        }
        ImageJFX.getLogger().info(String.format("Current item  " + currentItem));
        refresh();

    }

    @Override
    public SelectionModel getSelectionModel() {
        return null;

    }

    @Override
    public void refresh() {
        checkSelection();
    }

    @Override
    public void setOnItemClicked(Consumer<DataClickEvent<Explorable>> eventHandler) {

        this.eventHandler = eventHandler;

    }

    @Override
    public List<? extends Explorable> getItems() {

        if (currentItem != null && !currentItemList.contains(currentItem)) {
            currentItemList.add(currentItem);
            return currentItemList;

        } else {
            ImageJFX.getLogger().info(String.format("Current item is null : nothing selected"));
            return null;
        }
    }

    private void loadFXML() {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(FXMLWAY));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();

        } catch (IOException ex) {
            Logger.getLogger(DefaultAnnotationDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setCurrentItem(Explorable explorable) {

        currentItem = explorable;

    }

    public Explorable getDisplayedItem() {
        return currentItem;
    }

    private void setData(Explorable exp) {

        int index = itemList.indexOf(exp);

        title.setText(exp.getTitle());
        subtitle.setText(exp.getSubtitle());

        if (index > 0) {
            leftCell.setItem(itemList.get(index - 1));
        }

        if (index < itemList.size()) {
            rightCell.setItem(itemList.get(index + 1));
        }

        imageView.setImage(exp.getImage());

        tableView.getItems().clear();

        exp.getMetaDataSet().entrySet().stream().forEach((entry) -> {
            tableView.getItems().add(entry.getValue());
        });

    }

    private void checkSelection() {

        if (selectedItems.size() == 0 && itemList.size() > 0) {
            eventHandler.accept(new DataClickEvent<Explorable>(itemList.get(0), null, false));
        }

        if (selectedItems.size() > 1) {
            eventHandler.accept(new DataClickEvent<Explorable>(currentItem, null, true));
        } else {

        }
    }

    private void onDisplayPreviousExplorable(ActionEvent event) {
        int index = itemList.indexOf(currentItem) - 1;
        if (index >= 0 && index + 1 < itemList.size()) {
            eventHandler.accept(new DataClickEvent<Explorable>(itemList.get(index), null, false));
        }

    }

    private void onDisplayNextExplorable(ActionEvent event) {
        int index = itemList.indexOf(currentItem) + 1;
        if (index >= 0 && index + 1 < itemList.size()) {
            eventHandler.accept(new DataClickEvent<Explorable>(itemList.get(index), null, false));
        }

    }

}
