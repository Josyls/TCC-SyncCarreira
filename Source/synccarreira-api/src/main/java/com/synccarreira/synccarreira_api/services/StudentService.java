package com.synccarreira.synccarreira_api.services;

import com.synccarreira.synccarreira_api.dto.*;
import com.synccarreira.synccarreira_api.entities.Role;
import com.synccarreira.synccarreira_api.entities.Student;
import com.synccarreira.synccarreira_api.entities.User;
import com.synccarreira.synccarreira_api.repositories.RoleRepository;
import com.synccarreira.synccarreira_api.repositories.StudentRepository;
import com.synccarreira.synccarreira_api.services.exceptions.DatabaseException;
import com.synccarreira.synccarreira_api.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public Page<StudentDTO> findAll(Pageable pageable) {
        Page<Student> result = studentRepository.findAll(pageable);
        return result.map(StudentDTO::new);
    }

    @Transactional(readOnly = true)
    public StudentDTO findById(Long id) {
        Student entity = studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado"));
        return new StudentDTO(entity);
    }

    @Transactional
    public StudentDTO insert(StudentInsertDTO dto) {
        Student entity = new Student();
        copyDtoToEntity(dto, entity);
        entity = studentRepository.save(entity);
        return new StudentDTO(entity);
    }

    @Transactional
    public StudentDTO update(Long id, @Valid StudentUpdateDTO dto) {
        try {
            Student entity = studentRepository.getReferenceById(id);
            copyDtoToEntity(dto, entity);
            entity = studentRepository.save(entity);
            return new StudentDTO(entity);
        }
        catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Resource not found");
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resource not found");
        }
        try {
            studentRepository.deleteById(id);
        }
        catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Referential integrity failure");
        }
    }

    @Transactional(readOnly = true)
    public StudentScoreDTO getScore(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found. ID: " + id));
        return new StudentScoreDTO(student);
    }

    private void copyDtoToEntity(StudentInsertDTO dto, Student entity) {
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setSchoolType(dto.getSchoolType());
        entity.setSchollarYear(dto.getSchollarYear());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.getRoles().clear();
        Optional<Role> role = roleRepository.findById(dto.getRoleId());
        role.ifPresent(entity::addRole);
    }

    private void copyDtoToEntity(StudentUpdateDTO dto, Student entity) {
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setSchoolType(dto.getSchoolType());
        entity.setSchollarYear(dto.getSchollarYear());
        entity.getRoles().clear();
        Optional<Role> role = roleRepository.findById(dto.getRoleId());
        role.ifPresent(entity::addRole);
    }
}
