
# EnergyShare Energy Marketplace

The Energy Marketplace Service is responsible
for publishing solar energy surplus offers
and managing consumer requests.

Producers publish offers containing the available
energy quantity in kWh, price per kWh, location
zone, and availability period.

Consumers can browse available offers and
request an energy reservation.

The marketplace validates available surplus
through the Energy Monitoring Service using
synchronous OpenFeign communication.

When a reservation is created, the Marketplace
Service can publish a RabbitMQ event for the
Energy Transaction Service.

Transactions and payments are simulated in
the EnergyShare prototype.

The marketplace uses Spring Boot and MongoDB.
    