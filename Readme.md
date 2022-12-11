# Food ordering system
This system has four main microservices which are mainly following event-driven architectural patterns.
- Order-Service.
- Payment-Service.
- Restaurant-Service.
- Customer-Service.

### How to run the services?
You need docker to run the application.
Run below scripts respectively:
- `infrastructure/docker-compose/start-zookeeper.bat`
- `infrastructure/docker-compose/start-kafka.bat`
- `infrastructure/docker-compose/init-kafka.bat`

Then run below main classes:

- `order-service/order-container/src/main/java/com/food/ordering/system/order/service/domain/OrderServiceApplication.java`
- `payment-service/payment-container/src/main/java/com/food/ordering/system/payment/service/domain/PaymentServiceApplication.java`
- `restaurant-service/restaurant-container/src/main/java/com/food/ordering/system/restaurant/service/domain/RestaurantServiceApplication.java`
- `customer-service/customer-container/src/main/java/com/food/ordering/system/customer/service/CustomerServiceApplication.java`

Or use `mvn clean install` command to build the project and create docker images.

### A sample dependency graph for easy reference:
All the above four services follows Clean architecture and Domain driven design principles. According to those patterns, all the services have arranged in similar way. Below you can see a sample dependency graph which is clearly showing above design rules.

![Alt text](dependency-graph.png?raw=true "Order service dependency graph")

You can clearly see that in the dependency graph most independent component is domain-core module, which contains core domain logics. This module does not depend on any other low-level modules. All the other modules are just plugins for this domain-core module. 

### How services are interacted with each other?
Apache Kafka is used as the message-broker to build the event-driven architecture across all services. You can find the configurations and avro models related to kafka in `infrastructure/kafka` directory.

![Alt text](system-overview.png?raw=true "System overview")

#### Main design principles

1.  Clean (Hexagonal) architecture -> Ports and adapters
2. Domain Driven Design (DDD)
3. SAGA Pattern: Process & Rollback transactions.
4. Outbox Pattern: Make the system more resilient and fault tolerance with outbox tables.
    - Prevent concurrency issues with Optimistic locks & DB Constraints.
    - Keep updating SAGA and Order Status for each operation.
5. CQRS pattern with event sourcing.

##### You can find further information in each service modules.