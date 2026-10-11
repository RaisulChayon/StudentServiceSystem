# Student Service Management System

![Build and Test](https://github.com/YOUR-USERNAME/StudentServiceSystem/actions/workflows/build.yml/badge.svg)

A Java Swing desktop application that lets administrative staff record, track,
search and update student service requests (PRO101 Assessment 3).

## Features

- Enter student details: Student ID, full name, email and contact number
- Choose a service type: Academic Enquiry, IT Support, Assessment Support, General Enquiry
- Enter a description of the request (with a live character counter)
- Automatically generated, unique Request IDs (`REQ-0001`, `REQ-0002`, ...)
- Table of all requests with colour-coded status
- Update a request's status to **Pending**, **In Progress** or **Resolved**
- Search by **Request ID** or **Student ID**
- Filter the table by status (All / Pending / In Progress / Resolved)
- Input validation with clear messages and red highlighting of invalid fields
- Requests are saved to `service_requests.txt` and re-loaded on start-up
- Known students are auto-filled when their Student ID is re-entered
- Keyboard shortcuts: Alt+S submit, Alt+C clear, Alt+R search, Alt+A show all,
  Alt+U update status, Ctrl+S save

## Class design

| Class | Responsibility |
|---|---|
| `Student` | Stores and validates a student's details |
| `ServiceRequest` | One request: ID, student, type, description, status, timestamps |
| `StudentServiceSystem` | Business logic: create, store, search, update, summarise, save/load |
| `StudentServiceGUI` | Swing window; handles user interaction only |
| `InputValidator` | All validation rules in one place |
| `ValidationException` | Carries a list of validation errors back to the GUI |
| `RequestFileHandler` | Saves/loads requests to a text file |
| `ServiceType`, `RequestStatus` | Enums for the fixed lists of values |
| `Main` | Program entry point |

## Validation rules

| Field | Rule |
|---|---|
| Student ID | 6–10 letters or digits (e.g. `S1234567`) |
| Name | 2–50 characters; letters, spaces, hyphens, apostrophes |
| Email | `name@domain.tld`, max 100 characters |
| Contact number | 8–15 digits, optional leading `+`, spaces allowed |
| Service type | Must be selected |
| Description | 10–500 characters |

## How to run

**In an IDE (IntelliJ IDEA / Eclipse / NetBeans / VS Code):**
open the project folder as a Maven project, then run `studentservice.Main`.

**From the command line (requires JDK 17+ and Maven):**

```bash
mvn clean package
java -jar target/student-service-system-1.0.0.jar
```

## How to run the tests

Right-click `src/test/java` in your IDE and choose *Run All Tests*, or:

```bash
mvn test
```

37 JUnit 5 tests cover normal, invalid and boundary inputs for every major function.

## Continuous integration

`.github/workflows/build.yml` runs `mvn test` on GitHub Actions for every push,
so each commit shows a green tick (all tests passed) or a red cross.
