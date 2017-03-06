package com.sophie.fyp.ui;

import java.io.File;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class HelpScreen extends Application
{

	@Override
	public void start(Stage helpStage) throws Exception
	{
		Pane root = new Pane();
		Scene helpScene = new Scene(root,800,400);
		
        WebView browser = new WebView();
        
        
        helpStage.setTitle("Help Menu");
        helpStage.setScene(helpScene);
        File f = new File("help.html");
        String url = f.toURI().toURL().toString();
        browser.getEngine().load(url);
        root.getChildren().add(browser);
        helpStage.show();

	}

}
