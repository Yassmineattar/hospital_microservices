package com.example.patientservice.controller;

import com.example.patientservice.events.PatientEventPublisher;
import com.example.patientservice.model.Patient;
import com.example.patientservice.repository.PatientRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientRepository patientRepository;
    private final PatientEventPublisher eventPublisher;

    public PatientController(PatientRepository patientRepository,
                             PatientEventPublisher eventPublisher) {
        this.patientRepository = patientRepository;
        this.eventPublisher = eventPublisher;
    }

    // GET /patients -> liste complète (sans pagination, pour tests simples)
    @GetMapping("/all")
    public List<Patient> getAllPatientsNoPage() {
        return patientRepository.findAll();
    }

    // GET /patients -> liste paginée
    @GetMapping
    public Page<Patient> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return patientRepository.findAll(pageable);
    }

    // POST /patients -> créer un patient
    @PostMapping
    public Patient createPatient(@RequestBody Patient patient) {
        Patient saved = patientRepository.save(patient);
        // Publier l’événement "CREATED"
        eventPublisher.publishPatientCreated(saved);
        return saved;
    }

    // DELETE /patients/{id} -> supprimer un patient
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {

        return patientRepository.findById(id)
                .map(existing -> {
                    // supprimer en base
                    patientRepository.delete(existing);

                    // publier event
                    eventPublisher.publishPatientDeleted(existing);

                    // réponse HTTP 204
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }


    // GET /patients/{id} -> récupérer un patient par id
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return patientRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /patients/{id} -> mettre à jour un patient
    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody Patient updatedPatient) {

        return patientRepository.findById(id)
                .map(existing -> {
                    existing.setFirstName(updatedPatient.getFirstName());
                    existing.setLastName(updatedPatient.getLastName());
                    existing.setEmail(updatedPatient.getEmail());
                    existing.setPhone(updatedPatient.getPhone());
                    existing.setAddress(updatedPatient.getAddress());
                    existing.setDateOfBirth(updatedPatient.getDateOfBirth());

                    Patient saved = patientRepository.save(existing);

                    // Publier l’événement "UPDATED"
                    eventPublisher.publishPatientUpdated(saved);

                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }



    // GET /patients/search?keyword=... -> recherche full-text simple
    @GetMapping("/search")
    public Page<Patient> searchPatients(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return patientRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword, keyword, keyword, pageable
                );
    }
}
