 package com.sophie.fyp.ui;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;

import com.sophie.fyp.crawler.ArffData;
import com.sophie.fyp.crawler.DataGatherer;
import com.sophie.fyp.mining.InstanceSetup;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
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
	private Button predict = new Button("Predict");
	private Label predictionLabel = new Label();
	private Label accuracyLabel = new Label();
	
	private TextField urlText = new TextField();
	private ChoiceBox<String> offerText = new ChoiceBox<String>(FXCollections.observableArrayList("?", "yes", "no"));
	private ChoiceBox<String> lfText = new ChoiceBox<String>(FXCollections.observableArrayList("?", "identical", 
			"veryclose", "similar", "different", "unique"));
	private String predictDefault = "This system is used for distinguishing phishing websites from benevolent ones.";
	private String accuracyDefault = "Just enter the requested values, hit the Predict button, and PhishGuard"
			+ " will tell you whether it thinks the website is a phishing website or not.";
	private BorderPane root;
	private ScrollPane scroll;
	private Pane ui;
	private Scene scene;
	private ProgressBar pBar;
	private String[] progressLabels = {"Ready", "Analyzing URL", "Collecting redirection status",
			"Counting spelling errors","Preparing classifier", "Setting up instance", "Classifying", "Complete!"};
	Label progressLabel = new Label();
	private EventHandler<ActionEvent> menuEventHandler = new EventHandler<ActionEvent>()
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
		scene = new Scene(root,800,600);
		
		scene.widthProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{			
				urlText.setPrefWidth(newValue.doubleValue() - 180.0);	
				predictionLabel.setPrefWidth(newValue.doubleValue() - 430);
				accuracyLabel.setPrefWidth(newValue.doubleValue() - 430);
				pBar.setPrefWidth(newValue.doubleValue() - 200);
				
				
			}
		});
		
		
		stage.setScene(scene);
		scroll = new ScrollPane();
		ui = new Pane();
		setUpUI(ui);
		scroll.setContent(ui);
		scroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		root.setCenter(scroll);		
		MenuBar menuBar = new MenuBar();
		setUpMenu(menuBar);		
		root.setTop(menuBar);		
		Pane right = new Pane();
		root.setRight(right);		
		predict.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event)
			{
				Task<Result> task = new Task<Result>()
				{
					@Override
					protected Result call() throws Exception
					{
						String url = urlText.getText();
						ArffData arffData = new ArffData();
						
						updateProgress(0.1, 1);
						updateMessage(progressLabels[1]);
						URL uri = new URL(url);
						
						String domain = uri.getHost();
						arffData.setUrlSimilarity(DataGatherer.readLevenshtein(domain));
						updateProgress(0.3, 1);
						updateMessage(progressLabels[2]);
						boolean redirection = DataGatherer.getRedirectionStatus(url);

						arffData.setRedirection(redirection);
						updateProgress(0.5, 1);
						updateMessage(progressLabels[3]);
						Response response = Jsoup.connect(url).execute();
						arffData.setSpellingErrors(DataGatherer.getSpellingErrors(response).size());
						Classifier rf;
						Instances instances;
						
						updateProgress(0.6, 1);
						updateMessage(progressLabels[4]);
						rf = (Classifier) SerializationHelper.read("RF100.model");
						instances = new DataSource("phishingData.arff").getDataSet();
				
						if (instances.classIndex() == -1)
							instances.setClassIndex(instances.numAttributes() - 1);
						String offers = offerText.getValue();
						String lf = lfText.getValue();
						updateProgress(0.8, 1);
						updateMessage(progressLabels[5]);
						Instance inst = InstanceSetup.setUpInstance(arffData, offers, lf, instances);
					    
						
						updateProgress(0.9, 1);
						updateMessage(progressLabels[6]);
						double clsLabel = rf.classifyInstance(inst);
						instances.add(inst); 
						rf.buildClassifier(instances);
						SerializationHelper.write("RF100.model", rf);
						Evaluation eval = new Evaluation(instances);
						eval.crossValidateModel(rf, instances, 10, new Random(1));
						boolean phishing = clsLabel ==0 ?true: false;
						Result result = new Result(phishing, eval.pctCorrect());
						updateProgress(10, 10);
						updateMessage(progressLabels[7]);
						updateValue(result);
						
						return result;						
					}					
				};
				
				pBar.progressProperty().bind(task.progressProperty());
		        progressLabel.textProperty().bind(task.messageProperty());
		        
				task.valueProperty().addListener(new ChangeListener<Result>(){

					@Override
					public void changed(ObservableValue<? extends Result> observable, Result oldValue, Result newValue)
					{
						// TODO Auto-generated method stub
						boolean phishing = newValue.isPhishing();
						if(phishing)
						{
							predictionLabel.setText("the given website IS a phishing website.");
						}
						else
						{
							predictionLabel.setText("the given website IS NOT a phishing website.");
						}
						
						accuracyLabel.setText("PhishGuard is " + String.format("%.4f%%", newValue.getAccuracy()) + 
								" confident in this prediction.");
						pBar.progressProperty().unbind();
						progressLabel.textProperty().unbind();
					}
					
				});
				
				task.exceptionProperty().addListener(new ChangeListener<Throwable>()
				{

					@Override
					public void changed(ObservableValue<? extends Throwable> observable, Throwable oldValue,
							Throwable newValue)
					{
						// TODO Auto-generated method stub
						if(newValue instanceof IOException)
						{
							predictionLabel.setText("This URL is not valid.");
						}
						else
						{
							predictionLabel.setText("A prediction could not be made.");
						}
						accuracyLabel.setText("");
						pBar.progressProperty().unbind();
						pBar.setProgress(0);
						progressLabel.textProperty().unbind();
						progressLabel.setText(progressLabels[0]);
					}
				});
				new Thread(task).start();
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
		
		urlText.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				predictionLabel.setText("");
				accuracyLabel.setText("");
				pBar.setProgress(0);
				progressLabel.setText(progressLabels[0]);
			}
		});
		
		urlText.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				if(!newValue) //if losing focus
				{
					String text = urlText.getText();
					if("".equals(text))
					{
						predictionLabel.setText(predictDefault);
						accuracyLabel.setText(accuracyDefault);
					}
				}
				
			}
		});
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
		predictionLabel.setPrefWidth(350);
		predictionLabel.setWrapText(true);
		predictionLabel.setText(predictDefault);
		left.getChildren().add(predictionLabel);
		
		
		accuracyLabel.setLayoutX(400);
		accuracyLabel.setLayoutY(300);
		accuracyLabel.setPrefWidth(350);
		accuracyLabel.setWrapText(true);
		accuracyLabel.setText(accuracyDefault);
		left.getChildren().add(accuracyLabel);
		
		pBar = new ProgressBar();
		pBar.setLayoutX(160);
		pBar.setLayoutY(500);
		pBar.setProgress(0);
		
		left.getChildren().add(pBar);
		progressLabel.setLayoutX(200);
		progressLabel.setLayoutY(450);
		progressLabel.setText(progressLabels[0]);
		left.getChildren().add(progressLabel);
	}
	
	
	
}
