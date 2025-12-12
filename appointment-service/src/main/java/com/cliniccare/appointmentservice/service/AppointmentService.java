package com.cliniccare.appointmentservice.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cliniccare.appointmentservice.entity.Appointment;
import com.cliniccare.appointmentservice.entity.AppointmentStatus;
import com.cliniccare.appointmentservice.rabbitmq.AppointmentEventPublisher;
import com.cliniccare.appointmentservice.repository.AppointmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository repo;
    private final AppointmentEventPublisher publisher;

    public Appointment create(Appointment appointment) {
        appointment.setDate(LocalDate.now());
        appointment.setTime(LocalTime.now());
        appointment.setStatus(AppointmentStatus.BOOKED);

        Appointment saved = repo.save(appointment);

        publisher.sendAppointmentBooked(saved);

        return saved;
    }

    public String cancel(Long id) {
        Appointment appt = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appt.setStatus(AppointmentStatus.CANCELLED);
        repo.save(appt);

        publisher.sendAppointmentCancelled(appt);

        return "Cancelled";
    }

    public Appointment getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
    }

    public List<Appointment> getAllAppointments() {
        return repo.findAll();
    }

    public List<Appointment> getByPatientId(String patientId) {
        return repo.findByPatientId(patientId);
    }
}
