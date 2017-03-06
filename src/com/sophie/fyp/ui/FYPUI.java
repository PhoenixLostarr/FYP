package com.sophie.fyp.ui;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;

import com.sophie.fyp.crawler.ArffData;
import com.sophie.fyp.crawler.DataGatherer;
import com.sophie.fyp.mining.InstanceSetup;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class FYPUI extends javafx.application.Application
{
	Button predict = new Button("Predict");
	Label predictionLabel = new Label();
	Label accuracyLabel = new Label();
	
	TextField urlText = new TextField();
	ChoiceBox<String> offerText = new ChoiceBox<String>(FXCollections.observableArrayList("?", "yes", "no"));
	ChoiceBox<String> lfText = new ChoiceBox<String>(FXCollections.observableArrayList("?", "identical", 
			"veryclose", "similar", "different", "unique"));
	
	BorderPane root;
	Pane ui;
	EventHandler<ActionEvent> menuEventHandler = new EventHandler<ActionEvent>()
	{

		@Override
		public void handle(ActionEvent event)
		{
			// TODO Auto-generated method stub
			Object obj = event.getSource();
			if(obj instanceof MenuItem)
			{
				MenuItem item = (MenuItem) obj;
				switch(item.getText())
				{
					case "Small":
					{
						changeFontSize(11);
						break;
					}
					case "Default":
					{
						changeFontSize(13);
						break;
					}
					case "Large":
					{
						changeFontSize(16);
						break;
					}
					case "Largest":
					{
						changeFontSize(18);
						break;
					}
					case "Help":
					{
						showHelp();
						break;
					}
					case "Dark":
					{
						changeTheme(Color.DIMGRAY.darker(), toRGBCode(Color.WHITESMOKE));
						break;
					}
					case "Light":
					{
						changeTheme(Color.WHITESMOKE,toRGBCode(Color.DIMGRAY.darker()));
						break;
					}
					case "Blue":
					{
						changeTheme(Color.DARKSLATEBLUE, toRGBCode(Color.WHITESMOKE));
						break;
					}
					default:
					{
						return;
					}
				}
			}
		}

		private void showHelp()
		{
			Stage stage = new Stage();
            HelpScreen screen = new HelpScreen();
			try
			{
				screen.start(stage);
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void changeFontSize(int size)
		{
			for(Node label: ui.getChildren())
			{
				label.setStyle("-fx-font: " + size + "px \"System Regular\";");
			}
			
		}
		
		private void changeTheme(Color bgColor, String hexCode)
		{
			root.setBackground(new Background(new BackgroundFill(bgColor, CornerRadii.EMPTY,
				    Insets.EMPTY)));
			
			for(Node node: ui.getChildren())
			{
				//only change the font of labels
				if(node instanceof Label)
				{
					Label label = (Label) node;
					label.setTextFill(Color.web(hexCode));
				}
				node.setStyle("-fx-text-color: " + hexCode + ";");
			}
		}
		
		public String toRGBCode( Color color )
	    {
	        return String.format( "#%02X%02X%02X",
	            (int)( color.getRed() * 255 ),
	            (int)( color.getGreen() * 255 ),
	            (int)( color.getBlue() * 255 ) );
	    }
	};

	public static void main(String[] args)
	{
		
		launch(args);
	}
	@Override
	public void start(Stage stage) throws Exception
	{
		
		stage.setTitle("PhishGuard Phishing Detection Program");
		root = new BorderPane();
		stage.setScene(new Scene(root,800,600));
//		root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY,
//			    Insets.EMPTY)));
		
		ui = new Pane();
		setUpUI(ui);
		root.setLeft(ui);
		
		MenuBar menuBar = new MenuBar();
		setUpMenu(menuBar);		
		root.setTop(menuBar);
		
		Pane right = new Pane();
		root.setRight(right);
		
		predict.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event)
			{
				String url = urlText.getText();
				ArffData arffData = new ArffData();
				try
				{
					URL uri = new URL(url);
					
					String domain = uri.getHost();
					arffData.setUrlSimilarity(DataGatherer.readLevenshtein(domain));
					
					boolean redirection = DataGatherer.getRedirectionStatus(url);

					arffData.setRedirection(redirection);
					Response response = Jsoup.connect(url).execute();
					arffData.setSpellingErrors(DataGatherer.getSpellingErrors(response).size());
				}
				catch (IOException e1)
				{
					predictionLabel.setText("The given URL is not valid.");
					accuracyLabel.setText("");
					return;
				}
					
				

				Classifier rf;
				Instances instances;
				try
				{
					rf = (Classifier) SerializationHelper.read("RF100.model");
					instances = new DataSource("phishingData.arff").getDataSet();
				}
				catch (Exception e1)
				{
					predictionLabel.setText("There was a problem generating the model.");
					accuracyLabel.setText("");
					return;
				}
				if (instances.classIndex() == -1)
					instances.setClassIndex(instances.numAttributes() - 1);
				String offers = offerText.getValue();
				String lf = lfText.getValue();
				
				Instance inst = InstanceSetup.setUpInstance(arffData, offers, lf, instances);
			    
				try
				{
					double clsLabel = rf.classifyInstance(inst);
					if(clsLabel == 0)
					{
						predictionLabel.setText("the given website IS a phishing website.");
					}
					else
					{
						predictionLabel.setText("the given website IS NOT a phishing website.");
					}
					instances.add(inst); 
					rf.buildClassifier(instances);
					SerializationHelper.write("RF100.model", rf);
					Evaluation eval = new Evaluation(instances);
					eval.crossValidateModel(rf, instances, 10, new Random(1));
					accuracyLabel.setText("PhishGuard is " + String.format("%.4f%%", eval.pctCorrect()) + 
							" confident in this prediction.");
					
					 
				}
				catch (Exception e)
				{
					predictionLabel.setText("a prediction could not be made.");
					accuracyLabel.setText("");
					return;
				}
			}});
		stage.show();
	}
	
	private void setUpMenu(MenuBar menuBar)
	{
		Menu optionMenu = new Menu("Options");
		Menu fontSize = new Menu("Font Size");
		ToggleGroup toggleFontSize = new ToggleGroup();
		RadioMenuItem smallFont = new RadioMenuItem("Small");
		RadioMenuItem regularFont = new RadioMenuItem("Default");
		RadioMenuItem largeFont = new RadioMenuItem("Large");
		RadioMenuItem largestFont = new RadioMenuItem("Largest");
		
		smallFont.setToggleGroup(toggleFontSize);
		regularFont.setToggleGroup(toggleFontSize);
		largeFont.setToggleGroup(toggleFontSize);
		largestFont.setToggleGroup(toggleFontSize);
		
		smallFont.setOnAction(menuEventHandler);
		regularFont.setOnAction(menuEventHandler);
		largeFont.setOnAction(menuEventHandler);
		largestFont.setOnAction(menuEventHandler);
		
		fontSize.getItems().addAll(smallFont, regularFont,largeFont,largestFont);
		
		Menu themeMenu = new Menu("Theme");
		ToggleGroup toggleTheme = new ToggleGroup();
		RadioMenuItem darkTheme = new RadioMenuItem("Dark");
		RadioMenuItem lightTheme = new RadioMenuItem("Light");
		RadioMenuItem blueTheme = new RadioMenuItem("Blue");
		darkTheme.setToggleGroup(toggleTheme);
		lightTheme.setToggleGroup(toggleTheme);
		blueTheme.setToggleGroup(toggleTheme);
		
		darkTheme.setOnAction(menuEventHandler);
		lightTheme.setOnAction(menuEventHandler);
		blueTheme.setOnAction(menuEventHandler);
		
		themeMenu.getItems().addAll(darkTheme,lightTheme,blueTheme);
		optionMenu.getItems().addAll(fontSize,themeMenu);
		
		
		
		Menu helpMenu = new Menu("Help");
		MenuItem showHelp = new MenuItem("Help");
		showHelp.setOnAction(menuEventHandler);
		helpMenu.getItems().add(showHelp);
		
		
		menuBar.getMenus().addAll(optionMenu,helpMenu);
	}
	
	private void setUpUI(Pane left)
	{	
		
		Label urlLabel = new Label("URL:");
		urlLabel.setLayoutX(10);
		urlLabel.setLayoutY(50);
		left.getChildren().add(urlLabel);
		
		
		urlText.setLayoutX(150);
		urlText.setLayoutY(50);
		left.getChildren().add(urlText);
		
		
		Label offerLabel = new Label("Offers:");
		offerLabel.setLayoutX(10);
		offerLabel.setLayoutY(150);
		left.getChildren().add(offerLabel);
		
		
		offerText.setValue("?");
		offerText.setLayoutX(150);
		offerText.setLayoutY(150);
		left.getChildren().add(offerText);
		
		Label lfLabel = new Label("Look and feel:");
		lfLabel.setLayoutX(10);
		lfLabel.setLayoutY(250);
		left.getChildren().add(lfLabel);
		
		
		lfText.setValue("?");
		lfText.setLayoutX(150);
		lfText.setLayoutY(250);
		left.getChildren().add(lfText);
		
		predict.setLayoutX(100);
		predict.setLayoutY(350);
		left.getChildren().add(predict);
		
		predictionLabel.setLayoutX(400);
		predictionLabel.setLayoutY(200);
		left.getChildren().add(predictionLabel);
		
		
		accuracyLabel.setLayoutX(400);
		accuracyLabel.setLayoutY(300);
		left.getChildren().add(accuracyLabel);
		
		
	}
	
	
	
}
