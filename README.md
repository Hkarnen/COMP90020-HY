# Project Progress & TODO List

This project implements a distributed **Fast Bully Election** system with a simple **chat application** over TCP sockets. Below is the current development status, completed features, and next steps.

---

## ✅ Completed Features
- Basic **Node** startup using a JavaFX **GUI** (NodeUI).
- **Fast Bully Algorithm** implemented for leader election.
- **Chat messages** routed through the elected leader.
- **Heartbeat mechanism** for leader failure detection (currently disabled for easier manual testing).
- Refactored clean **Messenger** and **Manager** classes with proper dependencies.
- **JSON**-based message protocol.

---

## ⚡ In Progress

| Feature | Status | Notes |
|:--------|:------:|:------|
| Proper logging split |  Halfway | Right now, both logs and chats go to the console. Should display **only chat messages** in GUI and keep logs in console. |
| Heartbeat timeout handling |  Disabled | Heartbeat-based auto-election is off for now. Manual election works via button. |
| GUI input improvements | Halfway | Add a chat box for chat message |
| Node server startup |  Mixed | `Node.start()` still has an old Scanner input. Should be removed fully once GUI is finalized. |
| Hardcoded config loading | Halfway | Added a new class for extra node joining, but it still requires specifying the bootstrap configuration file; the next step could be to implement zero-configuration auto-discovery. |



---

## ✏️ Immediate Tasks to Complete

| Task | Details |
|:-----|:--------|
| **Split Logs vs Chat Display** | Chat messages should update the GUI (`logArea`), while internal logs like elections/heartbeats stay in the console. |
| **Remove Scanner from Node** | `Node.start()` still uses Scanner for typing commands. Remove it as input is now handled by the GUI. |
| **Add Chat TextField** | Instead of a fixed "HELLO" button, add a **TextField** + **Send button** for typing any chat message.(completed) |
| **Implement Graceful Quit** | When pressing the QUIT button, broadcast a "PEER_DOWN" message (optional for MVP) before exiting.(completed) |
| **Reactivate Heartbeat Detection** | After stabilizing chat, reactivate automatic leader re-election on heartbeat loss. |
| **(Optional) Support Dynamic Peer Joins** | Allow new nodes to join dynamically without reloading the config (stretch goal). |

---

## Future Enhancements 

- Dynamic peer discovery
- Display connected peers in the GUI
- Show current leader and chat history in real-time
- Optimize heartbeat intervals and timeouts
- Enhance GUI styling (colors, better layout)



