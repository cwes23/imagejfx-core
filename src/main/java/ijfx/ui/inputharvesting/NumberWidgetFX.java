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

import ijfx.ui.utils.ConvertedProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import mongis.utils.StringUtils;
import org.scijava.plugin.Plugin;
import org.scijava.widget.InputWidget;
import org.scijava.widget.NumberWidget;
import org.scijava.widget.WidgetModel;

/**
 *
 * @author Cyril MONGIS
 */
@Plugin(type = InputWidget.class)
public class NumberWidgetFX extends AbstractFXInputWidget<Number> implements NumberWidget<Node> {

    private Node component;

    public static String CLASS_INVALID = "danger";

    private ConvertedProperty<Number, Double> converter = new ConvertedProperty<Number, Double>()
            .forward(Number::doubleValue)
            .backward(Double::new);

    private SpinnerValueFactory factory;

    public NumberWidgetFX() {

    }

    public Number check(Number number, Number def) {
        if(number == null) {
            number = def;
        }
        if (number.intValue() == Integer.MIN_VALUE
                || number.intValue() == Integer.MAX_VALUE
                || number.doubleValue() == Double.MIN_VALUE
                || number.doubleValue() == Double.MIN_VALUE) {
            return def;
        } else {
            return number;
        }
    }

    @Override
    public void set(WidgetModel model) {
        super.set(model);

        Number min = check(model.getMin(),0);
        Number max = check(model.getMax(),10000);

       
        Number stepSize = check(model.getStepSize(),1);

        if (model.isStyle((SLIDER_STYLE))) {
            Slider slider = new Slider();

            Label sliderLabel = new Label();
            HBox hbox = new HBox();
            hbox.getChildren().addAll(sliderLabel, slider);
            sliderLabel.setPrefWidth(60);
            sliderLabel.setMaxWidth(60);
            sliderLabel.getStyleClass().add("warning");
            hbox.setSpacing(10.0);

            slider.setMin(min.doubleValue());
            slider.setMax(max.doubleValue());
            slider.setBlockIncrement(stepSize.doubleValue());
            bindProperty(slider.valueProperty());

            sliderLabel.textProperty().bind(Bindings.createStringBinding(() -> StringUtils.numberToString(slider.getValue(), 3), slider.valueProperty()));

            component = hbox;

        } else {

            if (model.isType(Double.class) || model.isType(double.class)) {
                factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(min.doubleValue(), max.doubleValue(), min.doubleValue(), stepSize.doubleValue());
            } else if (model.isType(long.class) || model.isType(Long.class)) {
                factory = new LongSpinnerValueFactory(min.longValue(), max.longValue(), min.longValue(), stepSize.longValue());
            } else {
                factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min.intValue(), max.intValue(), min.intValue(), stepSize.intValue());
            }

            //factory = new NumberSpinnerValueFactory(min, max, 0, stepSize, (Class<? extends Number>) model.getItem().getType());
            Spinner spinner = new Spinner(factory);
            spinner.setEditable(true);

            bindProperty(factory.valueProperty());

            //bindProperty(converter.frontProperty());
            component = spinner;
        }

    }

    @Override
    public Node getComponent() {
        return component;
    }

    @Override
    public Class<Node> getComponentType() {
        return Node.class;
    }

    @Override
    public boolean supports(WidgetModel model) {
        return super.supports(model)
                && isOneOf(model,
                        Double.class,
                        double.class,
                        int.class,
                        Integer.class,
                        Long.class,
                        long.class);
    }

    private class LongSpinnerValueFactory extends SpinnerValueFactory<Long> {

        final long min, max, initial, stepSize;

        public LongSpinnerValueFactory(long min, long max, long initial, long stepSize) {
            this.min = min;
            this.max = max;
            this.initial = initial;
            this.stepSize = stepSize;
        }

        public long get() {
            return getValue();
        }

        @Override
        public void decrement(int steps) {
            long value = get() - (stepSize * steps);
            setValue(Math.max(min, value));
        }

        @Override
        public void increment(int steps) {
            long value = get() + (stepSize * steps);
            setValue(Math.min(max, value));
        }
    }

}
