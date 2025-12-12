-- Create databases for the microservices
CREATE DATABASE IF NOT EXISTS appointment_db;
CREATE DATABASE IF NOT EXISTS patient_db;
CREATE DATABASE IF NOT EXISTS billing_db;
CREATE DATABASE IF NOT EXISTS notification_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON appointment_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON patient_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON billing_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON notification_db.* TO 'root'@'%';

FLUSH PRIVILEGES;
