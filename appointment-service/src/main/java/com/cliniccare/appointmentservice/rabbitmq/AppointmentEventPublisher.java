package com.cliniccare.appointmentservice.rabbitmq;

import com.cliniccare.appointmentservice.entity.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AppointmentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${cliniccare.rabbitmq.exchange}")
    private String exchange;

    @Value("${cliniccare.rabbitmq.routingKey}")
    private String routingKey;

    public void publishAppointmentCreatedEvent(Appointment appointment) {

        Map<String, Object> event = new HashMap<>();
        event.put("appointmentId", String.valueOf(appointment.getAppointmentId()));
        event.put("date", appointment.getDate().toString());
        event.put("time", appointment.getTime().toString());
        event.put("patientId", appointment.getPatientId());
        event.put("doctorId", appointment.getDoctorId());
        event.put("status", appointment.getStatus().name());

        rabbitTemplate.convertAndSend(exchange, routingKey, event);

        System.out.println("📤 Événement envoyé : " + event);
    }

    public void sendAppointmentBooked(Appointment appointment) {
        Map<String, Object> event = new HashMap<>();
        event.put("appointmentId", String.valueOf(appointment.getAppointmentId()));
        event.put("date", appointment.getDate().toString());
        event.put("time", appointment.getTime().toString());
        event.put("patientId", appointment.getPatientId());
        event.put("doctorId", appointment.getDoctorId());
        event.put("status", appointment.getStatus().name());
        event.put("eventType", "APPOINTMENT_BOOKED");

        rabbitTemplate.convertAndSend(exchange, routingKey, event);

        System.out.println("✅ Rendez-vous réservé : " + appointment.getAppointmentId());
    }

    public void sendAppointmentCancelled(Appointment appointment) {
        Map<String, Object> event = new HashMap<>();
        event.put("appointmentId", String.valueOf(appointment.getAppointmentId()));
        event.put("date", appointment.getDate().toString());
        event.put("time", appointment.getTime().toString());
        event.put("patientId", appointment.getPatientId());
        event.put("doctorId", appointment.getDoctorId());
        event.put("status", appointment.getStatus().name());
        event.put("eventType", "APPOINTMENT_CANCELLED");

        rabbitTemplate.convertAndSend(exchange, routingKey, event);

        System.out.println("❌ Rendez-vous annulé : " + appointment.getAppointmentId());
    }
}
