package com.cliniccare.appointmentservice.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cliniccare.appointmentservice.entity.Appointment;
import com.cliniccare.appointmentservice.service.AppointmentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService service;

    @PostMapping
    public Appointment create(@RequestBody Appointment a) {
        return service.create(a);
    }

    @GetMapping("/{id}")
    public Appointment getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/all")
    public List<Appointment> getAllAppointments() {
        return service.getAllAppointments();
    }

    @GetMapping
    public List<Appointment> getByPatientId(@RequestParam String patientId) {
        return service.getByPatientId(patientId);
    }

    @PutMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id) {
        return service.cancel(id);
    }
}
