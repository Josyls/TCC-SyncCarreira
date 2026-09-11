package com.synccarreira.synccarreira_api.services;

import com.synccarreira.synccarreira_api.dto.psicologa.CuratedLinkDTOs;
import com.synccarreira.synccarreira_api.entities.CuratedLink;
import com.synccarreira.synccarreira_api.repositories.CuratedLinkRepository;
import com.synccarreira.synccarreira_api.repositories.InstitutionRepository;
import com.synccarreira.synccarreira_api.security.CurrentUser;
import com.synccarreira.synccarreira_api.services.exceptions.ForbiddenOperationException;
import com.synccarreira.synccarreira_api.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/** Curadoria de links de fontes externas confiáveis (RF-06, RN-05). */
@Service
public class CuratedLinkService {

    @Autowired private CuratedLinkRepository linkRepository;
    @Autowired private InstitutionRepository institutionRepository;
    @Autowired private CurrentUser currentUser;

    /** Links da psicóloga logada (gestão). */
    @Transactional(readOnly = true)
    public List<CuratedLinkDTOs.View> listMine() {
        return linkRepository.findByPsychologistId(currentUser.userId()).stream()
                .sorted(Comparator.comparing(CuratedLink::getCategory).thenComparing(CuratedLink::getTitle))
                .map(CuratedLinkDTOs.View::new)
                .toList();
    }

    /** Links ativos visíveis ao aluno logado (globais + da sua instituição). RN-05. */
    @Transactional(readOnly = true)
    public List<CuratedLinkDTOs.View> listForStudent() {
        Long instId = currentUser.account()
                .map(a -> a.getInstitution() != null ? a.getInstitution().getId() : null)
                .orElse(null);
        List<CuratedLink> links = instId != null
                ? linkRepository.findActiveForInstitution(instId)
                : linkRepository.findByActiveTrue();
        return links.stream()
                .sorted(Comparator.comparing(CuratedLink::getCategory).thenComparing(CuratedLink::getTitle))
                .map(CuratedLinkDTOs.View::new)
                .toList();
    }

    /** Instituições que a psicóloga/admin logado pode associar a um link. */
    @Transactional(readOnly = true)
    public List<CuratedLinkDTOs.InstitutionOption> institutionOptions() {
        if (currentUser.isAdmin()) {
            return institutionRepository.findAll().stream()
                    .sorted(Comparator.comparing(i -> i.getLegalName().toLowerCase()))
                    .map(i -> new CuratedLinkDTOs.InstitutionOption(i.getId(), i.getLegalName()))
                    .toList();
        }
        return currentUser.account()
                .filter(a -> a.getInstitution() != null)
                .map(a -> List.of(new CuratedLinkDTOs.InstitutionOption(
                        a.getInstitution().getId(), a.getInstitution().getLegalName())))
                .orElse(List.of());
    }

    @Transactional
    public CuratedLinkDTOs.View create(CuratedLinkDTOs.Insert dto) {
        CuratedLink link = new CuratedLink();
        link.setPsychologist(currentUser.user());
        link.setTitle(dto.title().trim());
        link.setUrl(dto.url().trim());
        link.setDescription(dto.description() != null ? dto.description().trim() : null);
        link.setCategory(dto.category());
        link.setActive(true);
        link.setCreatedAt(Instant.now());
        if (dto.institutionId() != null) {
            link.setInstitution(institutionRepository.findById(dto.institutionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instituição não encontrada.")));
        }
        return new CuratedLinkDTOs.View(linkRepository.save(link));
    }

    @Transactional
    public CuratedLinkDTOs.View update(Long id, CuratedLinkDTOs.Update dto) {
        CuratedLink link = owned(id);
        link.setTitle(dto.title().trim());
        link.setUrl(dto.url().trim());
        link.setDescription(dto.description() != null ? dto.description().trim() : null);
        link.setCategory(dto.category());
        link.setActive(Boolean.TRUE.equals(dto.active()));
        link.setInstitution(dto.institutionId() == null ? null
                : institutionRepository.findById(dto.institutionId())
                .orElseThrow(() -> new ResourceNotFoundException("Instituição não encontrada.")));
        return new CuratedLinkDTOs.View(linkRepository.save(link));
    }

    @Transactional
    public void delete(Long id) {
        linkRepository.delete(owned(id));
    }

    private CuratedLink owned(Long id) {
        CuratedLink link = linkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Link não encontrado. ID: " + id));
        if (!currentUser.isAdmin() && !link.getPsychologist().getId().equals(currentUser.userId())) {
            throw new ForbiddenOperationException("Este link foi cadastrado por outra psicóloga.");
        }
        return link;
    }
}
