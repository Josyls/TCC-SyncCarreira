package com.synccarreira.synccarreira_api.services;

import com.synccarreira.synccarreira_api.dto.PsychologistDTO;
import com.synccarreira.synccarreira_api.dto.PsychologistInsertDTO;
import com.synccarreira.synccarreira_api.dto.PsychologistUpdateDTO;
import com.synccarreira.synccarreira_api.dto.StudentInsertDTO;
import com.synccarreira.synccarreira_api.entities.Psychologist;
import com.synccarreira.synccarreira_api.entities.Role;
import com.synccarreira.synccarreira_api.entities.Student;
import com.synccarreira.synccarreira_api.repositories.PsychologistRepository;
import com.synccarreira.synccarreira_api.repositories.RoleRepository;
import com.synccarreira.synccarreira_api.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PsychologistService {

    @Autowired
    private PsychologistRepository psychologistRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<PsychologistDTO> findAll() {
        return psychologistRepository.findAll()
                .stream()
                .map(PsychologistDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public PsychologistDTO findById(Long id) {
        Psychologist psychologist = psychologistRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource not found."));
        return new PsychologistDTO(psychologist);
    }

    @Transactional
    public PsychologistDTO create(PsychologistInsertDTO dto) {
        if (psychologistRepository.existsByNameAndCrp(dto.getName(), dto.getCrp())) {
            throw new IllegalArgumentException(
                    "Já existe um(a) psicólogo(a) cadastrado(a) com o nome '" + dto.getName() +
                    "' e CRP '" + dto.getCrp() + "'.");
        }
        Psychologist psychologist = new Psychologist();
        copyDtoToEntity(dto, psychologist);
        psychologist = psychologistRepository.save(psychologist);
        return new PsychologistDTO(psychologist);
    }

    @Transactional
    public PsychologistDTO update(Long id, PsychologistUpdateDTO dto) {
        Psychologist psychologist = psychologistRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource not found."));
        copyDtoToEntity(dto, psychologist);
        psychologist = psychologistRepository.save(psychologist);
        return new PsychologistDTO(psychologist);
    }

    @Transactional
    public void delete(Long id) {
        if (!psychologistRepository.existsById(id)) {
            throw new EntityNotFoundException("Psicóloga não encontrada. ID: " + id);
        }
        psychologistRepository.deleteById(id);
    }

    public void validateIfContractIsActive(Long id) {
        Psychologist psychologist = psychologistRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource not found."));
        if (!psychologist.isContractValid()) {
            throw new IllegalStateException(
                    "Operação bloqueada: o contrato da psicóloga '" +
                            psychologist.getName() + "' está vencido desde " +
                            psychologist.getContractExpirationDate() + ".");
        }
    }

    private void copyDtoToEntity(PsychologistInsertDTO dto, Psychologist entity) {
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setContractExpirationDate(dto.getContractExpirationDate());
        entity.setCrp(dto.getCrp());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.getRoles().clear();
        Optional<Role> role = roleRepository.findById(dto.getRoleId());
        role.ifPresent(entity::addRole);
    }

    private void copyDtoToEntity(PsychologistUpdateDTO dto, Psychologist entity) {
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setContractExpirationDate(dto.getContractExpirationDate());
        entity.setCrp(dto.getCrp());
        entity.getRoles().clear();
        Optional<Role> role = roleRepository.findById(dto.getRoleId());
        role.ifPresent(entity::addRole);
    }
}
