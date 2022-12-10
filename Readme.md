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

Then run below main classes:

- `order-service/order-container/src/main/java/com/food/ordering/system/order/service/domain/OrderServiceApplication.java`
- `payment-service/payment-container/src/main/java/com/food/ordering/system/payment/service/domain/PaymentServiceApplication.java`
- `restaurant-service/restaurant-container/src/main/java/com/food/ordering/system/restaurant/service/domain/RestaurantServiceApplication.java`
- `customer-service/customer-container/src/main/java/com/food/ordering/system/customer/service/CustomerServiceApplication.java`

### A sample dependency graph for easy reference:
All the above four services follows Clean architecture and Domain driven design principles. According to those patterns, all the services have arranged in similar way. Below you can see a sample dependency graph which is clearly showing above design rules.

![Alt text](dependency-graph.png?raw=true "Order service dependency graph")

You can clearly see that in the dependency graph most independent component in domain-core module, which contains core domain logics. This module does not depend on any other low-level modules. All the other modules are just plugins for this domain-core module. 

### How services are interacted with each other?
Apache Kafka is used as the message-broker to build the event-driven architecture of all services. You can find the configurations and avro models related to kafka in `infrastructure/kafka` directory.

##### You can find further information in each service modules.