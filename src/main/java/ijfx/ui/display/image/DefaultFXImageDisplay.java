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
package ijfx.ui.display.image;

import ijfx.core.image.DisplayRangeService;
import ijfx.core.timer.Timer;
import ijfx.core.timer.TimerService;
import ijfx.core.utils.AxisUtils;
import ijfx.ui.main.ImageJFX;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.adapter.JavaBeanDoubleProperty;
import javafx.beans.property.adapter.JavaBeanDoublePropertyBuilder;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanProperty;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.display.DatasetView;
import net.imagej.display.DefaultImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imglib2.display.ColorTable;
import org.apache.commons.lang3.ArrayUtils;
import org.scijava.Priority;
import org.scijava.display.Display;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import rx.subjects.PublishSubject;

/**
 *
 * @author Cyril MONGIS
 */
@Plugin(type = Display.class, priority = Priority.HIGH_PRIORITY)
public class DefaultFXImageDisplay extends DefaultImageDisplay implements FXImageDisplay {

    /*
        Services
     */
    @Parameter
    private ImageDisplayService imageDisplayService;

    @Parameter
    private DisplayRangeService displayRangeService;

    @Parameter
    private TimerService timerService;

    /*
        Properties
     */
    private JavaBeanProperty<ColorTable> currentLUTProperty;

    private JavaBeanDoubleProperty currentLUTMinProperty;

    private JavaBeanDoubleProperty currentLUTMaxProperty;

    private JavaBeanDoubleProperty datasetMinProperty;

    private JavaBeanDoubleProperty datasetMaxProperty;

    private JavaBeanProperty<Number> currentChannelProperty;

    private JavaBeanProperty<int[]> compositeChannelsProperty;

    private IntegerProperty refreshPerSecond = new SimpleIntegerProperty();

    private final PublishSubject<Integer> publishSubject = PublishSubject.create();

    private final ExecutorService refreshThread = Executors.newFixedThreadPool(1);

    private final Boolean updateLock = Boolean.TRUE;

    public DefaultFXImageDisplay() {
        super();

        publishSubject
                .observeOn(ImageJFX.getPublishSubjectScheduler())
                .buffer(1, TimeUnit.SECONDS)
                .subscribe(list -> Platform.runLater(() -> refreshPerSecond.setValue(list.size())));

    }

    private DatasetView getDatasetView() {

        return imageDisplayService.getActiveDatasetView(this);

    }

    private Dataset getDataset() {
        return getDatasetView().getData();
    }

    public void updateAsync() {
        refreshThread.execute(() -> {
            if (getDatasetView() == null) {
                return;
            }
            Timer timer = timerService.getTimer(FXImageDisplay.class);
            timer.start();
            getDatasetView().getProjector().map();

            update();
            timer.elapsed("projection");
        });
    }

    @Override
    public void setCurrentLUT(ColorTable table) {

        table = displayRangeService.getEquivalentTable(table);
        getDataset().setColorTable(table, getCurrentChannel());
        getDatasetView().setColorTable(table, getCurrentChannel());
        updateAsync();
        currentLUTProperty.fireValueChangedEvent();
    }

    @Override
    public ColorTable getCurrentLUT() {
        return displayRangeService.getEquivalentTable(getDatasetView().getColorTables().get(getCurrentChannel()));
    }

    @Override
    public JavaBeanProperty<ColorTable> currentLUTProperty() {
        if (currentLUTProperty == null) {
            currentLUTProperty = generateBean("currentLUT");
        }
        return currentLUTProperty;
    }

    @Override
    public void setCurrentLUTMin(double min) {

        getDatasetView().setChannelRange(getCurrentChannel(), min, getCurrentLUTMax());
        getDataset().setChannelMinimum(getCurrentChannel(), min);

        currentLUTMinProperty().fireValueChangedEvent();

        updateAsync();
    }

    @Override
    public double getCurrentLUTMin() {
        double value = getDatasetView().getChannelMin(getCurrentChannel());
        return value;
    }

    @Override
    public JavaBeanDoubleProperty currentLUTMinProperty() {
        if (currentLUTMinProperty == null) {
            currentLUTMinProperty = generateDoubleBean("currentLUTMin");
        }
        return currentLUTMinProperty;
    }

    @Override
    public void setCurrentLUTMax(double max) {

        getDatasetView().setChannelRange(getCurrentChannel(), getCurrentLUTMin(), max);
        getDataset().setChannelMaximum(getCurrentChannel(), max);

        currentLUTMaxProperty().fireValueChangedEvent();
        updateAsync();
    }

    @Override
    public double getCurrentLUTMax() {
        double max = getDatasetView().getChannelMax(getCurrentChannel());
        return max;
    }

    @Override
    public JavaBeanDoubleProperty currentLUTMaxProperty() {
        if (currentLUTMaxProperty == null) {
            currentLUTMaxProperty = generateDoubleBean("currentLUTMax");
        }
        return currentLUTMaxProperty;
    }

    @Override
    public double getDatasetMin() {

        double min = displayRangeService.getDatasetMinimum(getDatasetView().getData(), getCurrentChannel()).doubleValue();

        return min;

    }

    @Override
    public double getDatasetMax() {
        double max = displayRangeService.getDatasetMaximum(getDatasetView().getData(), getCurrentChannel()).doubleValue();
        return max;
    }

    @Override
    public JavaBeanDoubleProperty datasetMinProperty() {
        if (datasetMinProperty == null) {
            datasetMinProperty = generateDoubleBean("datasetMin");
        }
        return datasetMinProperty;
    }

    //Dummy functions
    public void setDatasetMin(double min) {
    }

    //Dummy functions
    public void setDatasetMax(double max) {
    }

    @Override
    public JavaBeanDoubleProperty datasetMaxProperty() {
        if (datasetMaxProperty == null) {
            datasetMaxProperty = generateDoubleBean("datasetMax");
        }
        return datasetMaxProperty;
    }

    /*
        Current channel
     */
    @Override
    public void setCurrentChannel(int channel) {
        if (AxisUtils.hasAxisType(this, Axes.CHANNEL)) {

            setPosition(channel, Axes.CHANNEL);

            updateAsync();
        } else {
            datasetMinProperty().fireValueChangedEvent();
            datasetMaxProperty().fireValueChangedEvent();
            currentLUTProperty().fireValueChangedEvent();
            currentLUTMinProperty().fireValueChangedEvent();
            currentLUTMaxProperty().fireValueChangedEvent();
        }
    }

    @Override
    public void setPosition(long position, AxisType axisType) {
        super.setPosition(position, axisType);
        if (Axes.CHANNEL.equals(axisType)) {
            currentChannelProperty().fireValueChangedEvent();

            datasetMinProperty().fireValueChangedEvent();
            datasetMaxProperty().fireValueChangedEvent();

            currentLUTProperty().fireValueChangedEvent();
            currentLUTMinProperty().fireValueChangedEvent();
            currentLUTMaxProperty().fireValueChangedEvent();
            currentLUTMinProperty().fireValueChangedEvent();
            currentLUTMaxProperty().fireValueChangedEvent();

        }
    }

    @Override
    public JavaBeanProperty<Number> currentChannelProperty() {
        if (currentChannelProperty == null) {
            currentChannelProperty = generateBean("currentChannel");
        }
        return currentChannelProperty;
    }

    @Override
    public int getCurrentChannel() {
        if (AxisUtils.hasAxisType(this, Axes.CHANNEL)) {
            return getIntPosition(Axes.CHANNEL);
        } else {
            return 0;
        }
    }

    /*
        Channel activation
     */
    public void setCompositeChannels(int[] channels) {
        for (int i = 0; i != getChannelNumber(); i++) {
            setChannelComposite(i, ArrayUtils.contains(channels, i), true);
        }
        compositeChannelsProperty().fireValueChangedEvent();
    }

    public int[] getCompositeChannels() {
        if (getDatasetView().getProjector() == null) {
            return new int[0];
        }
        return IntStream
                .range(0, getChannelNumber())
                .filter(i -> getDatasetView().getProjector().isComposite(i))
                .toArray();
    }

    public void setChannelComposite(int channel, boolean activated) {
        setChannelComposite(channel, activated, false);
    }

    public void setChannelComposite(int channel, boolean activated, boolean silent) {
        getDatasetView()
                .getProjector()
                .setComposite(channel, activated);

        if (!silent) {
            compositeChannelsProperty().fireValueChangedEvent();
        }
    }

    public JavaBeanProperty<int[]> compositeChannelsProperty() {
        if (compositeChannelsProperty == null) {
            compositeChannelsProperty = generateBean("compositeChannels");
            localize(new long[2]);
        }
        return compositeChannelsProperty;
    }


    /*
        Helper classes
     */
    public int getChannelNumber() {

        if (AxisUtils.hasAxisType(this, Axes.CHANNEL)) {
            return (int) dimension(dimensionIndex(Axes.CHANNEL));
        } else {
            return 1;
        }
    }

    private <T> JavaBeanProperty<T> generateBean(String name) {
        try {
            return JavaBeanObjectPropertyBuilder
                    .create()
                    .bean(this)
                    .name(name)
                    .build();
        } catch (Exception e) {
            ImageJFX.getLogger().log(Level.SEVERE, null, e);
        }
        return null;
    }

    private <T> JavaBeanDoubleProperty generateDoubleBean(String name) {
        try {
            return JavaBeanDoublePropertyBuilder
                    .create()
                    .bean(this)
                    .name(name)
                    .build();
        } catch (Exception e) {
            ImageJFX.getLogger().log(Level.SEVERE, null, e);
        }
        return null;
    }

    private <T> ReadOnlyProperty<T> generateReadOnlyBean(String name) {
        try {
            return JavaBeanObjectPropertyBuilder
                    .create()
                    .bean(this)
                    .name(name)
                    .build();
        } catch (Exception e) {
            ImageJFX.getLogger().log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public void update() {
        publishSubject.onNext(1);
        checkLUProperties();
        super.update();
    }

    public void checkProperties() {
        currentLUTProperty().fireValueChangedEvent();
    }

    private void checkLUProperties() {

        currentLUTMinProperty().fireValueChangedEvent();
        currentLUTMaxProperty().fireValueChangedEvent();

    }

    public IntegerProperty refreshPerSecond() {
        return refreshPerSecond;
    }
}
