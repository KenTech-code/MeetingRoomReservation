# MeetingRoomReservation

A desktop Java application for managing meeting room reservations, user management, and booking availability.

---

## Project Overview

MeetingRoomReservation is a simple and efficient Java application designed to manage meeting room bookings within an organization. It allows users to register, book, edit, or cancel reservations and provides administrators with tools to manage rooms and users.

---

## Project Structure

```
MeetingRoomReservation/
├── AdminFrame.java
├── AvailabilityFrame.java
├── BookingFrame.java
├── DatabaseManager.java
├── EditCancelFrame.java
├── LoginFrame.java
├── Main.java
├── MainMenuFrame.java
├── ManageUsersFrame.java
├── RegisterUserFrame.java
├── RoomManagementFrame.java
├── .classpath
├── .gitignore
├── .project
└── meetingrooms.db (local database, not pushed)
```

> Note: Only Java source files and configuration files are tracked. The database file is local and should be created by the user.

---

## Requirements

- Java Development Kit (JDK) 8 or higher
- IDE (Eclipse, IntelliJ IDEA, NetBeans)
- SQLite (for database)

---

## Running the Application

1. **Clone the repository:**
```bash
git clone https://github.com/KenTech-code/MeetingRoomReservation.git
cd MeetingRoomReservation
```

2. **Open the project in your preferred Java IDE.**

3. **Create or connect your database:**
   - The application expects a SQLite database named `meetingrooms.db`.
   - You can create a sample database using SQL scripts:
```sql
CREATE TABLE rooms (
    id INTEGER PRIMARY KEY,
    name TEXT,
    capacity INTEGER
);

CREATE TABLE reservations (
    id INTEGER PRIMARY KEY,
    room_id INTEGER,
    reserved_by TEXT,
    start_time TEXT,
    end_time TEXT,
    FOREIGN KEY(room_id) REFERENCES rooms(id)
);
```

4. **Run the Main.java class** to start the application.

---

## Features

- User registration and login
- Admin panel for managing users
- Room management
- Booking, editing, and canceling reservations
- Availability calendar for rooms

---

## Important Notes

- Do **not commit your local database file** (`meetingrooms.db`) to GitHub.
- Keep sensitive information (if any) out of the repository.

---

## License

This project is open-source and free to use.

