# Customer-Service

This service handles the customers of the restaurant.

### Main architectural patterns:
- Clean architecture.
- Domain Driven Design
- Command Query Responsibility Segregation (CQRS) pattern.

Once a customer is created, this service publishes an event to `customer` Kafka topic. Order services subscribing to this topic and delegate the customer changes into `customers` table in the database.
