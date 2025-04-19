package node;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class NodeUI extends Application {
    private Node node;
    private PeerConfig peerConfig;
    private Messenger messenger;
    private ElectionManager electionManager;
    private ChatManager chatManager;

    private TextArea logArea;
    private Button helloBtn;
    private Button electionBtn;
    private Button quitBtn;
    private TextField idField;
    private TextField portField;
    private TextField configField;

    @Override
    public void start(Stage primaryStage) {
        idField = new TextField(); idField.setPromptText("Node ID"); idField.setPrefWidth(60);
        portField = new TextField(); portField.setPromptText("Port"); portField.setPrefWidth(80);
        configField = new TextField(); configField.setPromptText("Config file path"); configField.setPrefWidth(200);
        Button startBtn = new Button("Start Node");
        startBtn.setOnAction(e -> initializeNode());

        logArea = new TextArea(); logArea.setEditable(false);

        helloBtn = new Button("HELLO"); helloBtn.setDisable(true);
        helloBtn.setOnAction(e -> chatManager.sendHello());
        electionBtn = new Button("ELECTION"); electionBtn.setDisable(true);
        electionBtn.setOnAction(e -> electionManager.initiateElection());
        quitBtn = new Button("QUIT"); quitBtn.setDisable(true);
        quitBtn.setOnAction(e -> { Platform.exit(); System.exit(0); });

        HBox topBar = new HBox(10, idField, portField, configField, startBtn);
        topBar.setPadding(new Insets(10));
        HBox bottomBar = new HBox(10, helloBtn, electionBtn, quitBtn);
        bottomBar.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(logArea);
        root.setBottom(bottomBar);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Node UI");
        primaryStage.setOnCloseRequest((WindowEvent ev) -> { Platform.exit(); System.exit(0); });
        primaryStage.show();
    }

    private void initializeNode() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            int port = Integer.parseInt(portField.getText().trim());
            peerConfig = PeerConfig.loadFromFile(configField.getText().trim());
            peerConfig.getPeerMap().remove(id);

            messenger = new Messenger(this::appendLog);
            node = new Node(id, port, peerConfig);
            electionManager = new ElectionManager(node, peerConfig, messenger, this::appendLog);
            chatManager = new ChatManager(node, peerConfig, messenger, this::appendLog);

            MessageHandler messageHandler =
                    new MessageHandler(node, electionManager, chatManager, this::appendLog);
            node.setManagers(electionManager, chatManager, messageHandler);

            Thread serverThread = new Thread(node::startServer);
            serverThread.setDaemon(true);
            serverThread.start();

            appendLog("[Node " + id + "] Started on port " + port);
            helloBtn.setDisable(false);
            electionBtn.setDisable(false);
            quitBtn.setDisable(false);
        } catch (Exception ex) {
            appendLog("Error initializing node: " + ex.getMessage());
        }
    }

    private void appendLog(String txt) {
        Platform.runLater(() -> logArea.appendText(txt + "\n"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}

