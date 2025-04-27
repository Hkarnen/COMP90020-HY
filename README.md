# Project Progress & TODO List

Distributed chat demo using the **Fast Bully leader-election algorithm**.  
Each node is a standalone JVM process with a small Java FX GUI.

---

## Completed

| Area | Details |
|------|---------|
| **NodeGUI** | Java FX window to start a node, toggle AUTO heartbeat, type chat, trigger election, graceful quit. |
| **Fast Bully Election** | O(N) message complexity, two-phase timeout, election guard to avoid overlap. |
| **JSON Message Protocol** | `ELECTION, OK, COORDINATOR, CHAT, HEARTBEAT, JOIN, NEW_NODE, QUIT, PEER_DOWN`. |
| **Chat Relay** | All chat routed through the current leader; followers forward; leader broadcasts. |
| **Heartbeat Manager** | Toggleable AUTO button. Leader sends heartbeats; followers time-out and elect if lost. |
| **Graceful Quit** | QUIT message, leader re-broadcasts `PEER_DOWN`; peers prune the leaver. |
| **Unreachable-Peer Removal** | Leader detects first send-failure, issues `PEER_DOWN`, updates maps cluster-wide. |
| **Dynamic Join (bootstrap)** | `MembershipManager` lets a new node contact seeds and receive the full peer list + current leader. |

---

## In Progress

| Feature | Status | Notes |
|:--------|:------:|:------|
| Proper logging split |  Halfway | Right now, both logs and chats go to the console. Should display **only chat messages** in GUI and keep logs in console. |
| GUI input improvements | Halfway | Add a chat box for chat message |
| Hardcoded config loading | Halfway | Added a new class for extra node joining, but it still requires specifying the bootstrap configuration file; the next step could be to implement zero-configuration auto-discovery. |

---

## Immediate Tasks to Complete

| Task | Details |
|:-----|:--------|
| **Split Logs vs Chat Display** | Chat messages should update the GUI (`logArea`), while internal logs like elections/heartbeats stay in the console. |
| **(Optional) Support Dynamic Peer Joins** | Allow new nodes to join dynamically without reloading the config (stretch goal). |

---

## Future Enhancements 

- Dynamic peer discovery
- Display connected peers in the GUI
- Show current leader and chat history in real-time
- Optimize heartbeat intervals and timeouts
- Enhance GUI styling (colors, better layout)

