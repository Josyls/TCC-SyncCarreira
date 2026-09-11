package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.entities.Interest;
import com.synccarreira.synccarreira_api.entities.StudentProfile;

import java.util.List;

/** Perfil e interesses do aluno (RF-14). */
public record StudentProfileDTO(
        String bio,
        String phone,
        String city,
        List<Long> interestIds,
        List<InterestOption> allInterests
) {
    public record InterestOption(Long id, String name) {}

    public static StudentProfileDTO of(StudentProfile profile, List<Interest> catalog) {
        return new StudentProfileDTO(
                profile == null ? null : profile.getBio(),
                profile == null ? null : profile.getPhone(),
                profile == null ? null : profile.getCity(),
                profile == null ? List.of()
                        : profile.getInterests().stream().map(Interest::getId).sorted().toList(),
                catalog.stream()
                        .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                        .map(i -> new InterestOption(i.getId(), i.getName()))
                        .toList()
        );
    }
}
