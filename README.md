# COMP90020 Distributed Algorithms Project  
**Leader-Based Chat with the Bully Election Algorithm**

**Team HY**  
- **Houston Karnen** – 1254942  
- **Yuxing Huang** – 1368750  

---

## 📘 Overview

This project implements a **distributed peer-to-peer chat system** with **Garcia-Molina’s Bully Election Algorithm**. Each node runs as a standalone Java process that:

- Elects a leader through manual or automatic (heartbeat) mechanisms
- Routes chat messages via the leader to maintain order and consistency
- Allows new nodes to join dynamically through UDP multicast
- Handles graceful shutdowns and notifies other peers

The system includes a JavaFX GUI for visualization and debugging.

---

## 📁 Project Structure
### Note
Extract zip file, you will see another COMP90020-HY-BULLY folder. Files are in there



```plaintext
src/
└── node/
    ├── Node.java              # Main class coordinating all managers
    ├── NodeUI.java            # JavaFX user interface
    ├── Message.java           # JSON-based message format
    ├── Messenger.java         # TCP communication and failure detection
    ├── MessageHandler.java    # Parses and delegates received messages
    ├── ElectionManager.java   # Implements Bully election algorithm
    ├── HeartbeatManager.java  # Automatic leader failure detection
    ├── ChatManager.java       # Leader-based chat logic
    ├── ShutdownManager.java   # Handles graceful exits and peer removal
    ├── MembershipManager.java # JOIN and NEW_NODE messages for discovery
    ├── DiscoveryManager.java  # Multicast-based dynamic discovery
    └── PeerConfig.java        # Manages peer ID-port map and config loading

```

## Compile & Run
### Command Line
```
javac --module-path "PATH_TO_FX/lib" --add-modules javafx.controls,javafx.fxml -classpath "lib/json.jar" -d out src/node/*.java
```

```
java --module-path "PATH_TO_FX/lib" --add-modules javafx.controls,javafx.fxml -classpath "out;lib/json.jar" node.NodeUI
```
### Eclipse
1. Import the project
2. Add external libraries - **`lib/json.jar`**
3. Create a Run Configuration with VM arguments and run NodeUI
```
--module-path "C:\path\to\javafx\lib" --add-modules javafx.controls,javafx.fxml
```

## GUI Instructions
| Element | Function |
| ------- | -------- |
ID | Unique identifier
Port | Local TCP port
Config file | Optional `.properties` file with initial peer list
Send | Broadcast chat
Election | Start manual election
Auto toggle | Enable / disable auto-election (all must be open at the same time - or at least leader)
Quit | Graceful shutdown

## Demo Scenarios
1. Static bootstrap: Start with config file for 3 nodes; no leader yet
2. Manual election: Press election button to elect leader; messages possible
3. Auto election: Press auto elect button for all nodes; kill leader
4. Dynamic join: Add new node ID and port distinct from config file 
5. Quit: Graceful & crash quit for leader & follower

## External Libraries
- `org.json`
- JavaFX

