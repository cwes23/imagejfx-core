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
package ijfx.ui.loading;

import ijfx.ui.main.ImageJFX;
import javafx.animation.Transition;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.PopupControl;
import javafx.stage.Stage;
import javafx.stage.Window;
import mongis.utils.animation.Animations;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class LoadingPopup extends PopupControl {

    private final DoubleProperty progressProperty = new SimpleDoubleProperty();
    private final StringProperty messageProperty = new SimpleStringProperty();

    private final ObjectProperty<EventHandler<? extends ActionEvent>> onCancel = new SimpleObjectProperty<>();

    private final ObjectProperty<Node> coveredNode = new SimpleObjectProperty();

    private final BooleanProperty canCancelProperty = new SimpleBooleanProperty(true);

    private final BooleanProperty showCloseButton = new SimpleBooleanProperty(false);

    private final ObjectProperty<Task> taskProperty = new SimpleObjectProperty<>();

    private final BooleanProperty closeOnFinished = new SimpleBooleanProperty();

    private final BooleanProperty taskRunningProperty = new SimpleBooleanProperty();

    private final BooleanProperty stageFocused = new SimpleBooleanProperty(true);
    
    private final ObjectProperty<Stage> stageProperty = new SimpleObjectProperty();
    
    private Scene lastScene;
    
 
    
    public LoadingPopup() {
        super();

        setSkin(new LoadingPopupSkinBase(this));
        taskProperty.addListener(this::onTaskChanged);
        taskRunningProperty.addListener(this::onRunningPropertyChanged);
        //setAutoFix(true);
        //setAutoHide(true);
        stageProperty.addListener(this::onStageChange);
        stageFocused.addListener(this::onAttachedWindowShow);
        

    }
    
    
    
    public LoadingPopup(Stage stage) {
        this();
        stageProperty.setValue(stage);
    }

    public LoadingPopup attachTo(Scene scene) {
        lastScene = scene;

        return this;
    }

    public LoadingPopup setOnCancel(EventHandler<? extends ActionEvent> eventHandler) {
        onCancel.setValue(eventHandler);
        return this;
    }

    public LoadingPopup bindProgressProperty(ReadOnlyDoubleProperty property) {
        progressProperty.bind(property);
        return this;
    }

    public LoadingPopup bindMessageProperty(ReadOnlyStringProperty property) {
        messageProperty.bind(property);
        return this;
    }

    public LoadingPopup bindTask(Task task) {
        taskProperty.setValue(task);
        return this;
    }

    public void onTaskChanged(Observable obs, Task oldValue, Task task) {

        messageProperty.unbind();
        progressProperty.unbind();
        canCancelProperty.unbind();
        taskRunningProperty.unbind();
        if (task != null) {
            if (task.isRunning() && showingProperty().getValue() == false && stageFocused.getValue()) {
                showOnScene(lastScene);
            }

            bindMessageProperty(task.messageProperty());

            bindProgressProperty(task.progressProperty());
            setOnCancel(this::cancelCurrentTask);

            taskRunningProperty.bind(task.runningProperty());
            showCloseButton.bind(taskRunningProperty.not());

        }

    }

    private void cancelCurrentTask(ActionEvent event) {
        taskProperty.getValue().cancel();
    }

    public BooleanProperty canCancelProperty() {
        return canCancelProperty;
    }

    public BooleanProperty closeOnFinishedProperty() {
        return closeOnFinished;
    }

    public LoadingPopup setCanCancel(Boolean value) {
        canCancelProperty.setValue(value);
        return this;
    }

    Window attachedWindow;

    private void listenToWindow(Window window) {

        if (window == attachedWindow) {
            return;
        }
        if (attachedWindow != null && window != attachedWindow) {
            toggleListening(window, false);
            attachedWindow = window;
            
        }
        if(attachedWindow != null) toggleListening(attachedWindow, true);

    }

    private void toggleListening(Window window, boolean shouldListen) {
        /*
        if (shouldListen) {
           
            window.showingProperty().addListener(this::onAttachedWindowShow);
        } else {
            window.showingProperty().removeListener(this::onAttachedWindowShow);
        }*/
    }

    private void onStageChange(Observable ov, Stage oldValue, Stage newValue) {
        stageFocused.bind(newValue.focusedProperty());
    }
    
    private void onAttachedWindowShow(Observable obs, Boolean oldValue, Boolean isWindowShowing) {

        boolean isTaskRunning = taskRunningProperty.getValue();
        boolean isPopupShowing = isShowing();


        if (isWindowShowing && isTaskRunning && !isPopupShowing) {
            showOnScene(lastScene);
        } else if (!isWindowShowing && isPopupShowing) {
            hide();
        }
    }

    private void stopListeningToWindow(Window window) {
        window.showingProperty().removeListener(this::onAttachedWindowShow);
    }

    public LoadingPopup showOnScene(Scene scene) {
        lastScene = scene;

        if (taskProperty.getValue() != null && taskProperty.getValue().isDone()) {
            return this;
        }

        // Scene scene = node.getScene();
        //super.show(node.getScene().getWindow());
        if (scene == null) {
            return this;
        }
        super.show(scene.getWindow());
        listenToWindow(scene.getWindow());
        double px, py, pw, ph, ww, wh;

        setPrefWidth(500);
        setPrefHeight(500);

        // width and height of the window
        ww = scene.getWindow().getWidth();
        wh = scene.getWindow().getHeight();

        // width and height of the popup
        pw = getPrefWidth();
        ph = getPrefHeight();

        // placing the the popup in the middle of the window
        px = scene.getWindow().getX();
        py = scene.getWindow().getY();

        px += (ww / 2) - (pw / 2);
        py += (wh / 2) - (ph / 2) + 15;
        setAnchorX(px);
        setAnchorY(py);
        getScene().getWindow().setX(px);
        getScene().getWindow().setY(py);
        //super.show(scene.getWindow(), px, py);
        return this;
    }

    /*
     *  Properties
     */
    public StringProperty messageProperty() {
        return messageProperty;
    }

    public DoubleProperty progressProperty() {
        return progressProperty;
    }

    public BooleanProperty cancelProperty() {
        return canCancelProperty;
    }

    public BooleanProperty showCloseButtonProperty() {
        return showCloseButton;
    }

    public ReadOnlyBooleanProperty taskRunningProperty() {
        return taskRunningProperty;
    }

    public LoadingPopup closeOnFinished() {
        return closeOnFinished(Boolean.TRUE);
    }

    public LoadingPopup closeOnFinished(Boolean closeOnFinished) {
        this.closeOnFinished.setValue(closeOnFinished);
        return this;
    }

    public void onRunningPropertyChanged(Observable obs, Boolean oldValue, Boolean newValue) {


        if (oldValue == true && newValue == false) { // task is finished
            taskRunningProperty.unbind();
            if (closeOnFinished.getValue()) {
                hide();
            }
        } else if (newValue == true && isShowing() == false) { // the task started running
            showOnScene(lastScene);
        }

    }

    public LoadingPopup showOnStart(Node node) {
        coveredNode.setValue(node);
        return this;

    }

    public Property<Task> taskProperty() {
        return taskProperty;
    }

    @Override
    public void hide() {
        Transition configure = Animations.FADEOUT.configure(getSkin().getNode(), Animations.getAnimationDurationAsDouble());
        configure.setOnFinished(event -> super.hide());
        configure.play();
    }

}
