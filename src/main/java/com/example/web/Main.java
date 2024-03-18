package com.example.web;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.concurrent.Worker.State;
import java.io.*;
import java.util.Objects;


public class Main extends Application {

    TabPane tabPane = new TabPane();
    int str = 1;
    Stage stage;
    WebHistory historial_T;
    String url_H = null;
    String current_Url =null;


    public void start(Stage primaryStage) {

        //---Anadir la pestaña "+" e inicializar la primera pestana---

        Tab newTab = new Tab("+");
        new_Tab();
        tabPane.getTabs().add(newTab);
        tabPane.setTabMaxWidth(120);

        //------------- Añadir una nueva pestaña ---------------

        newTab.setOnSelectionChanged (n->{
            new_Tab();
        });

        //--------------- Creacion de la escena ----------------

        primaryStage.setScene(new Scene(tabPane, 1280, 720));
        primaryStage.setTitle("Navegador Web JavaFX");
        primaryStage.show();
    }

    //--------------- Creacion de una pestaña----------------

    private void new_Tab(){
        Tab homeTab = new Tab("Nueva pestaña");
        homeTab.setStyle("-fx-max-width: 250;");
        TextField urlField = new TextField(url_H);
        WebView webView = new WebView();

        WebEngine engine = new WebEngine();
        engine=webView.getEngine();

        Button atras = new Button(" ← ");
        Button adelante = new Button(" → ");
        Button addFavorito = new Button(" ★ ");
        MenuButton favoritos = new MenuButton("Favoritos");
        MenuButton historial = new MenuButton("Historial");
        HBox navBt = new HBox(atras,adelante,addFavorito,favoritos,historial);

        BorderPane homeContent = new BorderPane();
        VBox navegacion =new VBox(navBt, urlField);
        homeContent.setTop(navegacion);
        homeContent.setCenter(webView);
        homeTab.setContent(homeContent);

        WebEngine finalEngine1 = engine;

        //----------- Url cargada desde el hitorial -------------

        if (url_H != null){
            finalEngine1.load(urlField.getText());
        }

        //-----Carga el url y espera la carga del sitio para renombrar la pestaña actual ------

        urlField.setOnAction(c -> {
            finalEngine1.load(urlField.getText());
        });

        finalEngine1.getLoadWorker().stateProperty().addListener(
                new ChangeListener<State>() {
                    public void changed(ObservableValue ov, State oldState, State newState) {
                        if (newState == State.SUCCEEDED) {
                            homeTab.setText(finalEngine1.getTitle());
                            urlField.setText(finalEngine1.getLocation());

                            if(Objects.equals(current_Url, finalEngine1.getLocation())){}
                            else{
                                current_Url = finalEngine1.getLocation();
                                add_Hist(current_Url);
                                historial.getItems().add(new MenuItem(current_Url));
                            }
                        }
                    }
                }
        );


        //------------ Avanzar a la pagina siguiente -------------

        atras.setOnAction(hb -> {
            historial_T = finalEngine1.getHistory();
            ObservableList<WebHistory.Entry> entries = historial_T.getEntries();
            historial_T.go(-1);
            urlField.setText(entries.get(historial_T.getCurrentIndex()).getUrl());
        });

        //------------ Regresar a la pagina anterior -------------

        adelante.setOnAction(hb -> {
            historial_T = finalEngine1.getHistory();
            ObservableList<WebHistory.Entry> entries = historial_T.getEntries();
            historial_T.go(1);
            urlField.setText(entries.get(historial_T.getCurrentIndex()).getUrl());
        });

        //------------ Añadir una pagina a favoritos -------------

        addFavorito.setOnAction(c -> {
            int chk;
            chk = check_Fav(urlField.getText());
            if(chk==0){
                favoritos.getItems().add(new MenuItem(urlField.getText()));
                add_Fav(urlField.getText());
            }

        });

        //---- Cambiar de pestaña actuliza historial y favoritos -----

        homeTab.setOnSelectionChanged(lH -> {
            historial.getItems().clear();
            favoritos.getItems().clear();

            try(Reader r = new FileReader("src/main/resources/com/example/web/historial.txt");
                 BufferedReader br = new BufferedReader(r)) {
                String ch;

                do {
                    ch = br.readLine();
                    if(ch != null){
                        MenuItem item=new MenuItem(ch);
                        historial.getItems().add(item);

                        item.setOnAction(hb -> {
                            //String url;
                            url_H=item.getText();
                            new_Tab();
                            url_H=null;
                        });
                    }
                } while(ch != null);
            } catch(IOException ex) {}

            try(Reader rd = new FileReader("src/main/resources/com/example/web/favoritos.txt");
                BufferedReader br = new BufferedReader(rd)) {
                String ch;

                do {
                    ch = br.readLine();
                    if(ch != null){
                        MenuItem item=new MenuItem(ch);
                        favoritos.getItems().add(item);

                        item.setOnAction(hb -> {
                            //String url;
                            url_H=item.getText();
                            new_Tab();
                            url_H=null;
                        });
                    }
                } while(ch != null);
            } catch(IOException ex) {}
        });

        //---------------------------------------------------------

        if(str==1){
            tabPane.getTabs().add(homeTab);
            str=0;
        }
        else{
            tabPane.getTabs().add(tabPane.getTabs().size()-1,homeTab) ;
            tabPane.getSelectionModel().select(tabPane.getTabs().size()-2);
        }
    }

    //--------- Guarda los favoritos en un archivo -------------

    private void add_Fav(String currentUrl) {
        String url=currentUrl;

        try {
            File file = new File("src/main/resources/com/example/web/favoritos.txt");
            FileWriter fileWriter = new FileWriter(file, true);

            BufferedWriter writer = new BufferedWriter(fileWriter);
            writer.write(url + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private int check_Fav(String currentUrl) {
        String url=currentUrl;
        try(Reader rd = new FileReader("src/main/resources/com/example/web/favoritos.txt");
            BufferedReader br = new BufferedReader(rd)) {
            String ch;
            do {
                ch = br.readLine();
                if(ch != null){
                    if(Objects.equals(url, ch)){
                        return 1;
                    }
                }
            } while(ch != null);
        } catch(IOException ex) {}
        return 0;
    }

    //--------- Guarda el historial en un archivo -------------

    private void add_Hist(String currentUrl) {
        String url=currentUrl;

        try {
            File file = new File("src/main/resources/com/example/web/historial.txt");
            FileWriter fileWriter = new FileWriter(file, true);

            BufferedWriter writer = new BufferedWriter(fileWriter);
            writer.write(url + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //-------------------------------------------------------

    public static void main(String[] args) {
        launch(args);
    }
}