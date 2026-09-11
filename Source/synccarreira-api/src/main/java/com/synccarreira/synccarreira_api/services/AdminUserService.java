package com.synccarreira.synccarreira_api.services;

import com.synccarreira.synccarreira_api.dto.*;
import com.synccarreira.synccarreira_api.entities.*;
import com.synccarreira.synccarreira_api.repositories.*;
import com.synccarreira.synccarreira_api.services.exceptions.BusinessException;
import com.synccarreira.synccarreira_api.services.exceptions.ConflictException;
import com.synccarreira.synccarreira_api.services.exceptions.ResourceNotFoundException;
import com.synccarreira.synccarreira_api.services.validation.DocumentoValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Gestão de usuários e perfis pelo administrador (RF-02):
 * criar, editar e desativar contas de alunos e psicólogas, com CPF único e
 * vínculo a instituição/turma (base do isolamento — RNF-08).
 */
@Service
public class AdminUserService {

    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private PsychologistRepository psychologistRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private InstitutionRepository institutionRepository;
    @Autowired private SchoolClassRepository schoolClassRepository;

    @Transactional(readOnly = true)
    public List<ManagedUserDTO> list(String perfil, Long institutionId, Boolean active) {
        return userRepository.findAll().stream()
                .map(u -> ManagedUserDTO.from(u, accountRepository.findById(u.getId()).orElse(null)))
                .filter(dto -> perfil == null || dto.perfil().equalsIgnoreCase(perfil))
                .filter(dto -> institutionId == null || institutionId.equals(dto.institutionId()))
                .filter(dto -> active == null || active == dto.active())
                .sorted(Comparator.comparing(ManagedUserDTO::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public ManagedUserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado. ID: " + id));
        return ManagedUserDTO.from(user, accountRepository.findById(id).orElse(null));
    }

    @Transactional
    public ManagedUserDTO create(ManagedUserInsertDTO dto) {
        if (userRepository.findByEmail(dto.email().trim()) != null) {
            throw new ConflictException("Já existe um usuário com esse e-mail.");
        }
        String cpf = normalizeCpf(dto.cpf());
        if (accountRepository.existsByCpf(cpf)) {
            throw new ConflictException("Já existe uma conta com esse CPF.");
        }

        boolean psicologa = "PSICOLOGA".equalsIgnoreCase(dto.perfil());
        User user;
        if (psicologa) {
            Psychologist p = new Psychologist();
            if (dto.crp() == null || dto.crp().isBlank()) {
                throw new BusinessException("CRP é obrigatório para psicóloga.");
            }
            if (dto.contractExpirationDate() == null) {
                throw new BusinessException("Data de vencimento do contrato é obrigatória para psicóloga.");
            }
            p.setCrp(dto.crp().trim());
            p.setContractExpirationDate(dto.contractExpirationDate());
            user = p;
        } else {
            Student s = new Student();
            s.setSchollarYear(dto.schoolYear());
            s.setSchoolType(dto.schoolType());
            user = s;
        }

        user.setName(dto.name().trim());
        user.setEmail(dto.email().trim());
        user.setPassword(passwordEncoder.encode(dto.password()));

        Role role = roleRepository.findByAuthority(psicologa ? "ROLE_PSICOLOGA" : "ROLE_USER");
        if (role == null) {
            throw new IllegalStateException("Papel de acesso não configurado. Rode a migração V2.");
        }
        user.addRole(role);

        user = persist(user, psicologa);

        Account account = new Account();
        account.setUser(user);
        account.setCpf(cpf);
        account.setActive(true);
        applyBindings(account, dto.institutionId(), dto.schoolClassId());
        account.setCreatedAt(Instant.now());
        accountRepository.save(account);

        return ManagedUserDTO.from(user, account);
    }

    @Transactional
    public ManagedUserDTO update(Long id, ManagedUserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado. ID: " + id));

        User byEmail = userRepository.findByEmail(dto.email().trim());
        if (byEmail != null && !byEmail.getId().equals(id)) {
            throw new ConflictException("E-mail já pertence a outro usuário.");
        }
        String cpf = normalizeCpf(dto.cpf());
        accountRepository.findByCpf(cpf)
                .filter(a -> !a.getId().equals(id))
                .ifPresent(a -> { throw new ConflictException("CPF já pertence a outra conta."); });

        user.setName(dto.name().trim());
        user.setEmail(dto.email().trim());

        if (user instanceof Student s) {
            s.setSchollarYear(dto.schoolYear());
            s.setSchoolType(dto.schoolType());
        } else if (user instanceof Psychologist p) {
            if (dto.crp() != null && !dto.crp().isBlank()) p.setCrp(dto.crp().trim());
            if (dto.contractExpirationDate() != null) p.setContractExpirationDate(dto.contractExpirationDate());
        }
        userRepository.save(user);

        Account account = accountRepository.findById(id).orElseGet(() -> {
            Account a = new Account();
            a.setUser(user);
            a.setActive(true);
            a.setCreatedAt(Instant.now());
            return a;
        });
        account.setCpf(cpf);
        applyBindings(account, dto.institutionId(), dto.schoolClassId());
        accountRepository.save(account);

        return ManagedUserDTO.from(user, account);
    }

    @Transactional
    public ManagedUserDTO setStatus(Long id, AccountStatusUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado. ID: " + id));
        if (user.hasRole("ROLE_ADMIN")) {
            throw new BusinessException("Não é possível desativar uma conta de administrador por aqui.");
        }
        Account account = accountRepository.findById(id).orElseGet(() -> {
            Account a = new Account();
            a.setUser(user);
            a.setCreatedAt(Instant.now());
            return a;
        });
        account.setActive(Boolean.TRUE.equals(dto.active()));
        account.setDeactivationReason(Boolean.TRUE.equals(dto.active()) ? null
                : (dto.reason() == null || dto.reason().isBlank() ? "Desativada pelo administrador" : dto.reason().trim()));
        accountRepository.save(account);
        return ManagedUserDTO.from(user, account);
    }

    // ---------------------------------------------------------------------

    private User persist(User user, boolean psicologa) {
        return psicologa
                ? psychologistRepository.save((Psychologist) user)
                : studentRepository.save((Student) user);
    }

    private void applyBindings(Account account, Long institutionId, Long schoolClassId) {
        if (institutionId != null) {
            account.setInstitution(institutionRepository.findById(institutionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Instituição não encontrada. ID: " + institutionId)));
        } else {
            account.setInstitution(null);
        }
        if (schoolClassId != null) {
            SchoolClass turma = schoolClassRepository.findById(schoolClassId)
                    .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada. ID: " + schoolClassId));
            if (account.getInstitution() != null
                    && turma.getInstitution() != null
                    && !turma.getInstitution().getId().equals(account.getInstitution().getId())) {
                throw new BusinessException("A turma não pertence à instituição selecionada.");
            }
            account.setSchoolClass(turma);
        } else {
            account.setSchoolClass(null);
        }
    }

    private String normalizeCpf(String raw) {
        String digits = DocumentoValidator.apenasDigitos(raw);
        if (!DocumentoValidator.isCpfValido(digits)) {
            throw new BusinessException("CPF inválido.");
        }
        return digits;
    }
}
