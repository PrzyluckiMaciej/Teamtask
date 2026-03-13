# Teamtask

A team-focused task and project management web application built with **Spring Boot** and **Java**, featuring an HTML frontend and a **MySQL** database backend.

---

## Features

- Create and manage tasks and projects
- Assign tasks to team members
- Track task progress and status
- Collaborative team workspace
- Web-based interface accessible from any browser

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java, Spring Boot |
| Frontend | HTML |
| Database | MySQL |
| Build Tool | Maven |

---

## Prerequisites

Make sure you have the following installed before running the project:

- [Java 17+](https://adoptium.net/)
- [Maven 3.8+](https://maven.apache.org/) (or use the included `mvnw` wrapper)
- [MySQL 8+](https://dev.mysql.com/downloads/)

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/PrzyluckiMaciej/Teamtask.git
cd Teamtask
```

### 2. Set up the database

Log into MySQL and create a database for the application:

```sql
CREATE DATABASE teamtask;
```

### 3. Configure the application

Edit `src/main/resources/application.properties` and update the database connection settings:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/teamtask
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
spring.jpa.hibernate.ddl-auto=update
```

### 4. Build and run

Using the Maven wrapper (no separate Maven installation required):

```bash
# On Linux/macOS
./mvnw spring-boot:run

# On Windows
mvnw.cmd spring-boot:run
```

Or, if you have Maven installed globally:

```bash
mvn spring-boot:run
```

### 5. Open the app

Once running, open your browser and go to:

```
http://localhost:8080
```

---

## Building a JAR

To build a standalone JAR file for deployment:

```bash
./mvnw clean package
java -jar target/teamtask-*.jar
```
