 package com.sophie.fyp.ui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;

import javax.swing.JLabel;

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
import javafx.geometry.Pos;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.converters.ConverterUtils.DataSource;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class FYPUI extends javafx.application.Application
{
	private Button predict = new Button("Classify website");
	private Label predictionLabel = new Label();
	
	private Label urlLabel = new Label();
	private TextField urlText = new TextField();
	private ChoiceBox<String> offerText = new ChoiceBox<String>(FXCollections.observableArrayList("Offers Made?", "yes", "no"));
	private ChoiceBox<String> lfText = new ChoiceBox<String>(FXCollections.observableArrayList("Visual comparison?", "identical", 
			"veryclose", "similar", "different", "unique"));
	private ArffData data;
	private J48 model;
	private BorderPane root;
	private ScrollPane scroll;
	private Pane ui;
	private Scene scene;
	private ProgressBar pBar;
	private String[] progressLabels = {"Ready", "Analyzing URL", "Collecting redirection status",
			"Counting spelling errors","Preparing classifier", "Setting up instance", "Classifying", "Complete!"};
	Label progressLabel = new Label();
	Button visualizeButton = new Button("Visualize tree");
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
						changeTheme(Color.WHITESMOKE,toRGBCode(Color.BLACK));
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
			
			root.setStyle("-fx-background: " + toRGBCode(bgColor)+ ";");
			
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
	
	private EventHandler<ActionEvent> visualiser = new EventHandler<ActionEvent>()
	{
	
		@Override
		public void handle(ActionEvent event)
		{
			// TODO Auto-generated method stub
			final javax.swing.JFrame jf = 
				       new javax.swing.JFrame("PhishGuard Tree Visualizer: J48");
				     jf.setSize(1200,800);
				     jf.getContentPane().setLayout(new BorderLayout());
				     TreeVisualizer tv = null;
					try
					{
						tv = new TreeVisualizer(null,
						     model.graph(),
						     new PlaceNode2());
					}
					catch (Exception e1)
					{
						// Swallow exception						
					}
					String attributes = null;
					if(data == null)
					{
						attributes = "urlsimilarity: ?, redirection: "
				+ "?,spellingErrors: ?,lookandfeel: ?, offers: ?";
					}
					else
					{
						attributes = data.toString();
					}
					JLabel dataLabel = new JLabel("Last prediction: " +attributes);
					jf.getContentPane().add(dataLabel, BorderLayout.NORTH);
				     jf.getContentPane().add(tv, BorderLayout.CENTER);
				     jf.addWindowListener(new java.awt.event.WindowAdapter() {
				       public void windowClosing(java.awt.event.WindowEvent e) {
				         jf.dispose();
				       }
				     });

				     jf.setVisible(true);
				     tv.fitToScreen();
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
				urlText.setPrefWidth(newValue.doubleValue() - 150.0);	
				predictionLabel.setPrefWidth(newValue.doubleValue() - 430);
				pBar.setPrefWidth(newValue.doubleValue() - 250);
				urlLabel.setPrefWidth(newValue.doubleValue() - 40);
				
			}
		});
		Classifier rf;
		rf = (Classifier) SerializationHelper.read("J48.model");
		
		model = (J48) rf;
		visualizeButton.setOnAction(visualiser);
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
				Task<Boolean> task = new Task<Boolean>()
				{
					@Override
					protected Boolean call() throws Exception
					{
						String url = urlText.getText();
						if("".equals(url))
						{
							throw new IllegalStateException();
						}
						
						
						updateProgress(0.1, 1);
						updateMessage(progressLabels[1]);
						URL uri = new URL(url);
						if(data == null)
						{
							data = new ArffData();
						}
						String domain = uri.getHost();
						data.setUrlSimilarity(DataGatherer.readLevenshtein(domain));
						updateProgress(0.3, 1);
						updateMessage(progressLabels[2]);
						boolean redirection = DataGatherer.getRedirectionStatus(url);

						data.setRedirection(redirection);
						updateProgress(0.5, 1);
						updateMessage(progressLabels[3]);
						Response response = Jsoup.connect(url).execute();
						data.setSpellingErrors(DataGatherer.getSpellingErrors(response).size());

						Instances instances = new DataSource("phishingData.arff").getDataSet();
						
						updateProgress(0.6, 1);
						updateMessage(progressLabels[4]);
						
						
						if (instances.classIndex() == -1)
							instances.setClassIndex(instances.numAttributes() - 1);
						String offers = offerText.getValue();
						String lf = lfText.getValue();
						updateProgress(0.8, 1);
						updateMessage(progressLabels[5]);
						Instance inst = InstanceSetup.setUpInstance(data, offers, lf, instances);
					    
						
						updateProgress(0.9, 1);
						updateMessage(progressLabels[6]);
						double clsLabel = model.classifyInstance(inst);						
						boolean phishing = clsLabel ==0 ?true: false;
						
						updateProgress(10, 10);
						updateMessage(progressLabels[7]);
						
						updateValue(phishing);
						return phishing;						
					}					
				};
				
				pBar.progressProperty().bind(task.progressProperty());
		        progressLabel.textProperty().bind(task.messageProperty());
		        
				task.valueProperty().addListener(new ChangeListener<Boolean>(){

					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
					{
						// TODO Auto-generated method stub
						
						if(newValue)
						{
							predictionLabel.setText("The given website IS a phishing website.");
						}
						else
						{
							predictionLabel.setText("The given website IS NOT a phishing website.");
						}
						
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
						if(newValue instanceof IllegalStateException)
						{
							predictionLabel.setText("A URL is required.");
						}
						if(newValue instanceof IOException)
						{
							predictionLabel.setText("This URL is not valid.");
						}
						else
						{
							predictionLabel.setText("A prediction could not be made.");
						}
						pBar.progressProperty().unbind();
						pBar.setProgress(0);
						progressLabel.textProperty().unbind();
						progressLabel.setText(progressLabels[0]);
						newValue.printStackTrace();
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
		urlLabel.setText("Welcome to PhishGuard, a new system for phishing detection and defence! Please enter the URL of the website you wish to classify, optionally provide the other attributes shown, and "
				+ "hit \"Classify Website\". PhishGuard will then give you a prediction.");
		urlLabel.setLayoutX(10);
		urlLabel.setLayoutY(10);
		
		urlLabel.setWrapText(true);
		left.getChildren().add(urlLabel);
		
		Label urlIdentifier = new Label("URL:");
		urlIdentifier.setLayoutX(10);
		urlIdentifier.setLayoutY(90);
		left.getChildren().add(urlIdentifier);
		urlText.setLayoutX(80);
		urlText.setLayoutY(90);
		
		urlText.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				predictionLabel.setText("");
				pBar.setProgress(0);
				progressLabel.setText(progressLabels[0]);
			}
		});
		
		
		left.getChildren().add(urlText);
		
		
		Label offerLabel = new Label("Please enter whether or not the website makes you any offers in exchange for personal information. ");
		offerLabel.setLayoutX(10);
		offerLabel.setLayoutY(165);
		offerLabel.setPrefWidth(450);
		offerLabel.setWrapText(true);
		left.getChildren().add(offerLabel);

		offerText.setValue("Offers Made?");
		offerText.setLayoutX(80);
		offerText.setLayoutY(225);
		left.getChildren().add(offerText);
		
		Label lfLabel = new Label("Please enter how similar you think the website looks to other, benevolent websites you've visited in the past.");
		lfLabel.setLayoutX(10);
		lfLabel.setLayoutY(290);
		lfLabel.setPrefWidth(450);
		lfLabel.setWrapText(true);
		left.getChildren().add(lfLabel);
		
		lfText.setValue("Visual comparison?");
		lfText.setLayoutX(80);
		lfText.setLayoutY(375);
		left.getChildren().add(lfText);
		
		predict.setLayoutX(100);
		predict.setLayoutY(450);
		left.getChildren().add(predict);
		
		predictionLabel.setLayoutX(400);
		predictionLabel.setLayoutY(230);
		predictionLabel.setPrefWidth(350);
		predictionLabel.setWrapText(true);
		
		left.getChildren().add(predictionLabel);
		
		pBar = new ProgressBar();
		pBar.setLayoutX(160);
		pBar.setLayoutY(520);
		pBar.setProgress(0);
		
		left.getChildren().add(pBar);
		progressLabel.setLayoutX(10);
		progressLabel.setLayoutY(520);
		progressLabel.setAlignment(Pos.CENTER_RIGHT);
		progressLabel.setText(progressLabels[0]);
		left.getChildren().add(progressLabel);
		
		Label visualizerLabel = new Label("View the decision tree used to classify websites:");
		visualizerLabel.setLayoutX(350);
		visualizerLabel.setLayoutY(400);
		visualizerLabel.setWrapText(true);
		left.getChildren().add(visualizerLabel);
		
		visualizeButton.setLayoutX(400);
		visualizeButton.setLayoutY(450);
		left.getChildren().add(visualizeButton);
	}
	
	
	
}
