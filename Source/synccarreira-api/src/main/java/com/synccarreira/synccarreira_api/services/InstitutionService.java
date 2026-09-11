package com.synccarreira.synccarreira_api.services;

import com.synccarreira.synccarreira_api.dto.InstitutionDTO;
import com.synccarreira.synccarreira_api.dto.InstitutionInsertDTO;
import com.synccarreira.synccarreira_api.dto.InstitutionUpdateDTO;
import com.synccarreira.synccarreira_api.entities.Institution;
import com.synccarreira.synccarreira_api.repositories.InstitutionRepository;
import com.synccarreira.synccarreira_api.repositories.SchoolClassRepository;
import com.synccarreira.synccarreira_api.services.exceptions.BusinessException;
import com.synccarreira.synccarreira_api.services.exceptions.ConflictException;
import com.synccarreira.synccarreira_api.services.exceptions.ResourceNotFoundException;
import com.synccarreira.synccarreira_api.services.validation.DocumentoValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestão de instituições parceiras (RF-01). CNPJ obrigatório, validado por
 * dígito verificador e único. Só o administrador acessa (checado no controller).
 */
@Service
public class InstitutionService {

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private SchoolClassRepository schoolClassRepository;

    @Transactional(readOnly = true)
    public List<InstitutionDTO> findAll() {
        return institutionRepository.findAll().stream()
                .sorted((a, b) -> a.getLegalName().compareToIgnoreCase(b.getLegalName()))
                .map(InstitutionDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public InstitutionDTO findById(Long id) {
        return new InstitutionDTO(getOrThrow(id));
    }

    @Transactional
    public InstitutionDTO create(InstitutionInsertDTO dto) {
        String cnpj = normalizeCnpj(dto.cnpj());
        if (institutionRepository.existsByCnpj(cnpj)) {
            throw new ConflictException("Já existe uma instituição com o CNPJ informado.");
        }
        Institution entity = new Institution();
        entity.setLegalName(dto.legalName().trim());
        entity.setTradeName(trimOrNull(dto.tradeName()));
        entity.setCnpj(cnpj);
        entity.setType(dto.type());
        entity.setActive(true);
        entity = institutionRepository.save(entity);
        return new InstitutionDTO(entity);
    }

    @Transactional
    public InstitutionDTO update(Long id, InstitutionUpdateDTO dto) {
        Institution entity = getOrThrow(id);
        String cnpj = normalizeCnpj(dto.cnpj());
        institutionRepository.findByCnpj(cnpj)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ConflictException("Já existe outra instituição com o CNPJ informado.");
                });
        entity.setLegalName(dto.legalName().trim());
        entity.setTradeName(trimOrNull(dto.tradeName()));
        entity.setCnpj(cnpj);
        entity.setType(dto.type());
        entity.setActive(Boolean.TRUE.equals(dto.active()));
        return new InstitutionDTO(institutionRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        Institution entity = getOrThrow(id);
        if (schoolClassRepository.existsByInstitutionId(id)) {
            throw new ConflictException(
                    "Não é possível excluir: a instituição possui turmas vinculadas. Desative-a.");
        }
        institutionRepository.delete(entity);
    }

    private Institution getOrThrow(Long id) {
        return institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instituição não encontrada. ID: " + id));
    }

    private String normalizeCnpj(String raw) {
        String digits = DocumentoValidator.apenasDigitos(raw);
        if (!DocumentoValidator.isCnpjValido(digits)) {
            throw new BusinessException("CNPJ inválido.");
        }
        return digits;
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
