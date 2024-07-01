package demo;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import elemental.json.Json;
import elemental.json.JsonArray;

import java.io.Serializable;

//import com.vaadin.flow.component.dependency.JsModule;
//import com.vaadin.flow.component.UI;

//import java.io.Serializable;

//@JsModule("./src/electron-bridge.js")
@Push(transport = Transport.WEBSOCKET)
@Theme(ValoTheme.THEME_NAME)
public class AppUI extends UI {
    @Override
    protected void init(VaadinRequest request) {
        TextField nameField = new TextField();
        nameField.setCaption("Seu nome");
        //String versao = callElectronUiApi("get-version");
        Button button = new Button("Veja a magica", event ->
               // callElectronUiApi(new String[]{"getVersion"}),
                new Notification(
                        "Ola " + nameField.getValue()
                ).show(getPage())
        );
        VerticalLayout content = new VerticalLayout();
        content.addComponents(nameField, button);
        setContent(content);
    }

    /*
    private void callElectronUiApi(String[] args) {
        JsonArray paramsArray = Json.createArray();
        int i = 0;
        for (String arg : args) {
            paramsArray.set(i, Json.create(arg));
            i++;
        }
        getPage().getJavaScript().execute(
                "callElectronUiApi(" + paramsArray.toJson() + ")"
        );
    }
     */
    /*
    private void callElectronUiApi(Serializable... args) {
        UI.getCurrent().getPage().getJavaScript().execute("callElectronUiApi($0)", args);
    }

     */
}