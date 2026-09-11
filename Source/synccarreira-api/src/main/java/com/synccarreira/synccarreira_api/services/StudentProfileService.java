package com.synccarreira.synccarreira_api.services;

import com.synccarreira.synccarreira_api.dto.StudentProfileDTO;
import com.synccarreira.synccarreira_api.dto.StudentProfileUpdateDTO;
import com.synccarreira.synccarreira_api.entities.StudentProfile;
import com.synccarreira.synccarreira_api.entities.User;
import com.synccarreira.synccarreira_api.repositories.InterestRepository;
import com.synccarreira.synccarreira_api.repositories.StudentProfileRepository;
import com.synccarreira.synccarreira_api.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;

/** Perfil e interesses do aluno logado (RF-14). */
@Service
public class StudentProfileService {

    @Autowired private StudentProfileRepository profileRepository;
    @Autowired private InterestRepository interestRepository;
    @Autowired private CurrentUser currentUser;

    @Transactional(readOnly = true)
    public StudentProfileDTO getMine() {
        StudentProfile profile = profileRepository.findById(currentUser.userId()).orElse(null);
        return StudentProfileDTO.of(profile, interestRepository.findAll());
    }

    @Transactional
    public StudentProfileDTO updateMine(StudentProfileUpdateDTO dto) {
        User user = currentUser.user();
        StudentProfile profile = profileRepository.findById(user.getId()).orElseGet(() -> {
            StudentProfile p = new StudentProfile();
            p.setUser(user);
            p.setCreatedAt(Instant.now());
            return p;
        });

        profile.setBio(trimOrNull(dto.bio()));
        profile.setPhone(trimOrNull(dto.phone()));
        profile.setCity(trimOrNull(dto.city()));

        profile.getInterests().clear();
        if (dto.interestIds() != null && !dto.interestIds().isEmpty()) {
            profile.getInterests().addAll(new HashSet<>(interestRepository.findAllById(dto.interestIds())));
        }

        profile = profileRepository.save(profile);
        return StudentProfileDTO.of(profile, interestRepository.findAll());
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
