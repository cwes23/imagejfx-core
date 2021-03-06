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
package ijfx.explorer;

import ijfx.core.datamodel.Iconazable;
import ijfx.core.imagedb.ImageRecord;
import ijfx.core.metadata.MetaData;
import ijfx.core.timer.Timer;
import ijfx.core.timer.TimerService;
import ijfx.explorer.datamodel.Explorable;
import ijfx.explorer.datamodel.wrappers.ImageRecordIconizer;
import ijfx.explorer.events.DisplayedListChanged;
import ijfx.explorer.events.ExploredListChanged;
import ijfx.explorer.events.ExplorerSelectionChangedEvent;
import ijfx.ui.loading.LoadingScreenService;
import ijfx.ui.main.ImageJFX;
import ijfx.ui.utils.SelectionChange;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import mongis.utils.task.FluentTask;
import mongis.utils.task.ProgressHandler;
import org.scijava.Context;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import ijfx.core.imagedb.ExplorerService;

/**
 *
 * @author Cyril MONGIS, 2016
 */
@Plugin(type = Service.class)
public class DefaultExplorerViewService extends AbstractService implements ExplorerViewService {

    List<Explorable> explorableList = new ArrayList<>();

    List<Explorable> filteredList = new ArrayList<>();

    @Parameter
    private EventService eventService;

    @Parameter
    private LoadingScreenService loadingScreenService;

    @Parameter
    private TimerService timerService;

    @Parameter
    private ExplorerService imageRecordService;

    private final Logger logger = ImageJFX.getLogger();

    @Parameter
    private Context context;

    private Predicate<Explorable> lastFilter;
    private Predicate<Explorable> optionalFilter;

    private final IntegerProperty selected = new SimpleIntegerProperty(0);

    private List<Explorable> selectedItems = new ArrayList<>();

    int displayedItemState = 0;

    int filteredItemState = 0;

    @Override
    public void initialize() {

        // selectionManager.getChangeBuffer()
        //  .subscribe(this::notifySelectionChange);
    }

    @Override
    public void setItems(List<? extends Explorable> items) {

        int newState = ExplorableList.contentHashWithOrder(items);

        if (newState == displayedItemState) {
            return;
        }

        displayedItemState = newState;

        explorableList.clear();
        explorableList.addAll(items);
        setFilter(lastFilter);

    }

    @Override
    public void setFilter(Predicate<Explorable> predicate) {

        new FluentTask<Predicate<Explorable>, List<Explorable>>(predicate)
                .callback(this::filter)
                .then(this::setFilteredItems)
                .start();

    }

    protected List<Explorable> filter(Predicate<Explorable> predicate) {
        logger.info(String.format("Filtering %d items", getItems().size()));
        if (predicate == null && optionalFilter == null) {
            return getItems();
        } else if (predicate == null && optionalFilter != null) {
            predicate = optionalFilter;
        } else if (optionalFilter != null) {
            predicate = predicate.and(optionalFilter);
        }
        List<Explorable> collect = getItems().parallelStream().filter(predicate).collect(Collectors.toList());
        logger.info(String.format("Only %d items were kept", collect.size()));
        return collect;
    }

    @Override
    public List<Explorable> getItems() {
        return explorableList;
    }

    @Override
    public List<Explorable> getDisplayedItems() {
        return filteredList;
    }

    protected void setFilteredItems(List<Explorable> filteredItems) {

        // testing the filter state
        int newState = ExplorableList.contentHashWithOrder(filteredItems);

        // aborting if no changes
        if (newState == filteredItemState) {
            return;
        } else {
            filteredItemState = newState;
            this.filteredList = filteredItems;
            update();
        }

    }

    @Override
    public void setOptionalFilter(Predicate<Explorable> additionalFilter) {
        this.optionalFilter = additionalFilter;
        setFilter(lastFilter);
    }

    @Override
    public List<Explorable> getSelectedItems() {
        return selectedItems;
    }

    @Override
    public void select(Explorable explorable) {
        if (selectedItems.contains(explorable) == false) {
            selectedItems.add(explorable);
        }
    }

    @Override
    public void setSelected(List<Explorable> selectedList) {
        selectedItems.clear();
        selectedItems.addAll(selectedList);
    }

    @Override
    public ArrayList<String> getMetaDataKey(List<? extends Explorable> items) {
        ArrayList<String> keyList = new ArrayList<String>();
        items.forEach(plane -> {
            plane.getMetaDataSet().keySet().forEach(key -> {

                if (!keyList.contains(key)) {
                    keyList.add(key);
                }
            });
        });
        Collections.sort(keyList);
        return keyList;
    }

    public void open(Iconazable explorable) {

        new FluentTask<Void, Boolean>()
                .setName("Opening file...")
                .callback((progress, vd) -> {
                    try {
                        progress.setProgress(1, 5);
                        explorable.open();
                        progress.setProgress(1, 1);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .then(success -> {
                    if (success) {

                    }
                })
                .submit(loadingScreenService)
                .start();

    }

    @Override
    public void openSelection() {

        getSelectedItems().forEach(this::open);

    }

    @Override
    public void toggleSelection(Explorable explorable) {

        if (selectedItems.contains(explorable)) {
            selectedItems.remove(explorable);
        } else {
            selectedItems.add(explorable);
        }
    }

    public IntegerProperty selectedCountProperty() {
        return selected;
    }

    public Stream<Explorable> indexDirectory(ProgressHandler origProgress, File directory) {

        //if(progress == null) progress = new SilentProgressHandler();
        final ProgressHandler progress = ProgressHandler.check(origProgress);

        Timer timer = timerService.getTimer(this.getClass());
        timer.start();
        Collection<? extends ImageRecord> records = imageRecordService.getRecordsFromDirectory(progress, directory);
        timer.elapsed("record fetching");
        progress.setStatus("Reading folder...");

        progress.setTotal(records.size());

        return records
                .stream()
                .parallel()
                .map(record -> {
                    progress.increment(1);
                    return getSeries(record);
                })
                .flatMap(self -> self);

    }

    public Stream<Explorable> getSeries(ImageRecord record) {
        if (record.getMetaDataSet().containsKey(MetaData.SERIE_COUNT) && record.getMetaDataSet().get(MetaData.SERIE_COUNT).getIntegerValue() > 1) {

            int serieCount = record.getMetaDataSet().get(MetaData.SERIE_COUNT).getIntegerValue();

            return IntStream
                    .range(0, serieCount)
                    .mapToObj(i -> new ImageRecordIconizer(context, record, i));

        } else {
            return Stream.of(new ImageRecordIconizer(context, record));
        }
    }

    public void publishSelectionEvent() {
        eventService.publishLater(new ExplorerSelectionChangedEvent());
    }

    public void update() {
        eventService.publishLater(new DisplayedListChanged());
    }

}
