# Hospital Microservices - Testing Guide

## Overview
This project consists of 4 microservices that communicate via RabbitMQ:

1. **Appointment Service** (Port 8081) - Manages appointments, publishes events
2. **Patient Service** (Port 8082) - Manages patient data, publishes events  
3. **Billing Service** (Port 3003) - Consumes appointment events, creates bills
4. **Notification Service** (Port 3004) - Consumes appointment events, creates notifications

## Architecture
- **Databases**: 
  - MySQL: appointment_db, patient_db
  - MongoDB: billing_db, notification_db
- **Message Broker**: RabbitMQ
- **Exchange**: hospital.exchange (TopicExchange)
- **Routing Keys**: 
  - appointment.created
  - patient.created

## Prerequisites
- Java 17
- Maven 3.x
- Docker Desktop (for RabbitMQ, MySQL, MongoDB)

## Step 1: Start Infrastructure Services

```powershell
# Start MySQL, MongoDB, and RabbitMQ
docker-compose up -d

# Verify all containers are running
docker ps

# Check RabbitMQ Management UI
# Open: http://localhost:15672
# Login: guest / guest
```

## Step 2: Build All Services

```powershell
# Build Appointment Service
cd appointment-service
./mvnw clean package -DskipTests
cd ..

# Build Patient Service
cd patient-service
./mvnw clean package -DskipTests
cd ..

# Build Billing Service
cd HOSPITAL/billing-service
../../mvnw clean package -DskipTests
cd ../..

# Build Notification Service
cd HOSPITAL/notification-service
../../mvnw clean package -DskipTests
cd ../..
```

## Step 3: Start All Services

Open 4 separate PowerShell terminals:

### Terminal 1 - Appointment Service
```powershell
cd appointment-service
./mvnw spring-boot:run
```

### Terminal 2 - Patient Service
```powershell
cd patient-service
./mvnw spring-boot:run
```

### Terminal 3 - Billing Service
```powershell
cd HOSPITAL/billing-service
../../mvnw spring-boot:run
```

### Terminal 4 - Notification Service
```powershell
cd HOSPITAL/notification-service
../../mvnw spring-boot:run
```

## Step 4: Test the System

### Test 1: Create an Appointment (triggers Billing + Notification)

```powershell
# Create an appointment
curl -X POST http://localhost:8081/api/appointments `
  -H "Content-Type: application/json" `
  -d '{
    "patientId": "P001",
    "doctorId": "D001",
    "date": "2025-12-15",
    "time": "10:00",
    "status": "SCHEDULED"
  }'
```

**Expected Results:**
- ✅ Appointment created in appointment-service
- ✅ Bill created in billing-service (check MongoDB)
- ✅ Notification created in notification-service (check MongoDB)

### Test 2: Create a Patient

```powershell
# Create a patient
curl -X POST http://localhost:8082/api/patients `
  -H "Content-Type: application/json" `
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "1234567890",
    "address": "123 Main St",
    "dateOfBirth": "1990-01-01"
  }'
```

### Test 3: Check Billing Service

```powershell
# Get all bills
curl http://localhost:3003/api/bills

# Get bill by appointment ID
curl http://localhost:3003/api/bills/appointment/{appointmentId}
```

### Test 4: Check Notification Service

```powershell
# Get all notifications
curl http://localhost:3004/api/notifications

# Get notifications by patient ID
curl http://localhost:3004/api/notifications/patient/{patientId}
```

### Test 5: Get All Appointments

```powershell
# Get all appointments
curl http://localhost:8081/api/appointments
```

### Test 6: Get All Patients

```powershell
# Get all patients
curl http://localhost:8082/api/patients
```

## Verify RabbitMQ Message Flow

1. Open RabbitMQ Management UI: http://localhost:15672
2. Login: guest / guest
3. Go to **Exchanges** tab → Check `hospital.exchange`
4. Go to **Queues** tab → Check:
   - `billing_queue`
   - `notification_queue`
5. Create an appointment and watch the message flow in real-time

## Service Ports Reference

| Service | Port | Database | Type |
|---------|------|----------|------|
| Appointment Service | 8081 | appointment_db | MySQL |
| Patient Service | 8082 | patient_db | MySQL |
| Billing Service | 3003 | billing_db | MongoDB |
| Notification Service | 3004 | notification_db | MongoDB |
| RabbitMQ AMQP | 5672 | - | - |
| RabbitMQ Management | 15672 | - | - |
| MySQL | 3306 | - | - |
| MongoDB | 27017 | - | - |

## Troubleshooting

### Services can't connect to databases
```powershell
# Check if containers are running
docker ps

# Restart containers
docker-compose restart
```

### RabbitMQ connection issues
```powershell
# Check RabbitMQ logs
docker logs hospital-rabbitmq

# Restart RabbitMQ
docker-compose restart rabbitmq
```

### Port conflicts
```powershell
# Check what's using a port (e.g., 8081)
netstat -ano | findstr :8081

# Kill process if needed
taskkill /PID <PID> /F
```

### Clean restart
```powershell
# Stop all services (Ctrl+C in each terminal)

# Stop and remove containers
docker-compose down -v

# Start fresh
docker-compose up -d

# Restart all microservices
```

## Testing Flow Diagram

```
1. POST /api/appointments
   ↓
2. Appointment Service saves to MySQL
   ↓
3. Publishes event to RabbitMQ (hospital.exchange → appointment.created)
   ↓
   ├→ 4a. Billing Service receives event → Creates Bill in MongoDB
   └→ 4b. Notification Service receives event → Creates Notification in MongoDB
```

## Success Indicators

✅ All 4 services start without errors
✅ RabbitMQ shows exchange `hospital.exchange` with 2 queues bound
✅ Creating an appointment triggers both billing and notification
✅ All REST endpoints respond correctly
✅ Logs show message publishing and consumption

## Notes

- Services use **guest/guest** for RabbitMQ
- MySQL root password is empty (local development only)
- All services use **hospital.exchange** as the common exchange
- Event-driven architecture ensures loose coupling between services
