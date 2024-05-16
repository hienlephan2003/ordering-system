# Food Ordering Microservice

## Overview
This project is a food ordering server implemented with microservice architecture. It utilizes Spring Boot as the framework, JPA for data persistence, PostgreSQL as the database, and Kafka for event messaging. The project follows Domain-Driven Design (DDD) principles, employs the Saga pattern for managing distributed transactions, and utilizes the Outbox pattern for reliable event publishing.

## Technologies Used
- Spring Boot
- JPA (Java Persistence API)
- PostgreSQL
- Kafka

## Features
- **Microservice Architecture**: The project is divided into multiple microservices to enable scalability, maintainability, and flexibility.
- **Event Messaging with Kafka**: Kafka is used for asynchronous event messaging between microservices, ensuring loose coupling and reliability.
- **Domain-Driven Design (DDD)**: The project follows DDD principles to model the domain and business logic effectively.
- **Saga Pattern**: Sagas are used to manage distributed transactions across multiple microservices, ensuring data consistency.
- **Outbox Pattern**: The Outbox pattern is employed to reliably publish events to Kafka without risking data inconsistencies.

## Getting Started
1. **Prerequisites**: Make sure you have Java, Maven, PostgreSQL, and Kafka installed on your system.
2. **Clone the Repository**: `git clone https://github.com/hienlephan2003/ordering-system`
3. **Build the Project**: `mvn clean install`
4. **Set up PostgreSQL**: Create a database and configure the connection in the application properties.
5. **Start Kafka**: Run docker-compose file to start kafka zookeeper.
6. **Run the Application**: Start each microservice by running the Spring Boot applications.
7. **Explore the APIs**: Use tools like Postman to explore and interact with the APIs exposed by the microservices.

## Documentation
- For detailed API documentation, refer to the Swagger documentation provided with each microservice.
- For understanding the domain model and business logic, refer to the codebase and relevant documentation within each microservice.

## License
This project is licensed under the [MIT License](LICENSE).
