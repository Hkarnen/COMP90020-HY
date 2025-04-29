package node;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class NodeUI extends Application {
    private Node node;
    private PeerConfig peerConfig;
    private Messenger messenger;
    
    // UI components
    private TextFlow chatArea;
    private ScrollPane chatScrollPane;
    //private TextArea logArea;
    private Button electionBtn;
    private Button quitBtn;
    private Button sendBtn;
    private ToggleButton autoBtn;
    private TextField idField;
    private TextField portField;
    private TextField configField;
    private TextField chatInput;

    // Logger
    private static final Logger logger = Logger.getLogger("NodeUI");
    
 // Formatters
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void start(Stage primaryStage) {
    	// Configure logger to use console handler
        configureLogger();
        
        // Input fields
        idField = new TextField(); 
        idField.setPromptText("Node ID"); 
        idField.setPrefWidth(60);
        
        portField = new TextField(); 
        portField.setPromptText("Port"); 
        portField.setPrefWidth(80);
        
        configField = new TextField(); 
        configField.setPromptText("Config file path"); 
        configField.setPrefWidth(200);

        chatInput = new TextField();
        chatInput.setPromptText("Type your message...");
        chatInput.setPrefWidth(400);
        
        Button startBtn = new Button("Start Node");
        startBtn.setOnAction(e -> initializeNode());
        
        // Create chat area with TextFlow for better styling
        chatArea = new TextFlow();
        chatArea.setPadding(new Insets(10));
        chatArea.setLineSpacing(5);
        
        chatScrollPane = new ScrollPane(chatArea);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setFitToHeight(true);

        // Action buttons
        sendBtn = new Button("Send");
        sendBtn.setDisable(true);
        sendBtn.setOnAction(e -> {
            String msg = chatInput.getText().trim();
            if (!msg.isEmpty()) {
                node.getChatManager().sendChat(msg);
                chatInput.clear();
            }
        });
        
        // Support Enter key for sending messages
        chatInput.setOnAction(e -> {
            if (!sendBtn.isDisabled()) {
                String msg = chatInput.getText().trim();
                if (!msg.isEmpty()) {
                    node.getChatManager().sendChat(msg);
                    chatInput.clear();
                }
            }
        });
        
        electionBtn = new Button("ELECTION"); 
        electionBtn.setDisable(true);
        electionBtn.setOnAction(e -> node.getElectionManager().initiateElection());
        
        quitBtn = new Button("QUIT"); 
        quitBtn.setDisable(true);
        quitBtn.setOnAction(e -> { 
            node.getShutdownManager().quit();
        	Platform.exit(); 
        	System.exit(0); 
        });
        
        autoBtn = new ToggleButton("AUTO ELECT");
        autoBtn.setDisable(true);
        autoBtn.setOnAction(e -> {
        	boolean enabled = autoBtn.isSelected();
        	node.getHeartbeatManager().setEnabled(enabled);
        });
        
        
        // Layout components
        HBox topBar = new HBox(10, idField, portField, configField, startBtn);
        topBar.setPadding(new Insets(10));
        
        HBox bottomBar = new HBox(10, chatInput,sendBtn);
        bottomBar.setPadding(new Insets(10));
        HBox.setHgrow(chatInput, Priority.ALWAYS);
        
        HBox controlBar = new HBox(10, electionBtn, autoBtn, quitBtn);
        controlBar.setPadding(new Insets(10));
        
        VBox bottomControls = new VBox(5, bottomBar, controlBar);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(chatScrollPane);
        root.setBottom(bottomControls);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chat App");
        primaryStage.setOnCloseRequest((WindowEvent ev) -> { 
        	node.getShutdownManager().quit();
        	Platform.exit(); 
        	System.exit(0); 
        });
        
        primaryStage.show();
        
        logger.info("Application started");
    }
    
    private void configureLogger() {
        try {
            // Try to load logging configuration from file
            LogManager.getLogManager().readConfiguration(NodeUI.class.getClassLoader().getResourceAsStream("properties/logging"));
            logger.info("Logging configuration loaded successfully");
        } 
        catch (IOException e) {
        	// If anything goes wrong, set up a simple configuration
            Logger rootLogger = Logger.getLogger("");
            
            // Remove existing handlers
            for (Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }
            
            // Add console handler that shows everything
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            rootLogger.addHandler(consoleHandler);
            
            // Set root logger to show all logs
            rootLogger.setLevel(Level.ALL);
        }
    }


    private void initializeNode() {
        try {
        	int id = Integer.parseInt(idField.getText().trim());
            int port = Integer.parseInt(portField.getText().trim());
            String configPath = configField.getText().trim();
            
            if (configPath.isEmpty()) {
                logger.warning("Error: Please provide a config file path");
                return;
            }
            
            boolean isBootstrap = PeerConfig.isBootstrap(configPath, id);
            peerConfig = PeerConfig.loadFromFile(configPath);
            peerConfig.getPeerMap().remove(id);


            messenger = new Messenger(this::displayChatMessage);
            node = new Node(id, port,isBootstrap, peerConfig, messenger);
            
            Thread serverThread = new Thread(node::startServer);
            serverThread.setDaemon(true);
            serverThread.start();

            logger.info("[Node " + id + "] Started on port " + port);
            sendBtn.setDisable(false);
            electionBtn.setDisable(false);
            autoBtn.setDisable(false);;
            quitBtn.setDisable(false);
            
            // Set window title to include node ID
            Stage stage = (Stage) idField.getScene().getWindow();
            stage.setTitle("Distributed Chat - Node " + id);

            if (!peerConfig.getPeerIds().isEmpty()&&!isBootstrap) {
                Thread joinThread = new Thread(() -> node.getMembershipManager().joinCluster());
                joinThread.setDaemon(true);
                joinThread.start();
            }
        } 
        catch (NumberFormatException ex) {
            logger.warning("Error: Invalid Node ID or Port number");
        }
        catch (Exception ex) {
            logger.warning("Error initializing node: " + ex.getMessage());
        }
    }

    /**
     * Display chat message in the chat area
     */
    private void displayChatMessage(Message message) {
        if (message == null || message.getType() != Message.Type.CHAT) {
            return;
        }
        
        Platform.runLater(() -> {
            // Get current time for timestamp
            String timestamp = LocalDateTime.now().format(timeFormatter);
            
            // Create timestamp text
            Text timeText = new Text("[" + timestamp + "] ");
            timeText.setFill(Color.GRAY);
            
            // Create sender text
            Text senderText = new Text("Node " + message.getSenderId() + ": ");
            senderText.setFont(Font.font("System", FontWeight.BOLD, 12));
            senderText.setFill(Color.BLUE);
            
            // Create message text
            Text messageText = new Text(message.getContent() + "\n");
            
            // Add to chat area
            chatArea.getChildren().addAll(timeText, senderText, messageText);
            
            // Ensure chat area scrolls to bottom
            chatScrollPane.setVvalue(1.0);
        });
    }

    public static void main(String[] args) {
    	launch(args);
    }
}

