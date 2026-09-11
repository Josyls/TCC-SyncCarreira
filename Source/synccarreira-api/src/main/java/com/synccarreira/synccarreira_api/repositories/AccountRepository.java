package com.synccarreira.synccarreira_api.repositories;

import com.synccarreira.synccarreira_api.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT a FROM Account a WHERE a.user.email = :email")
    Optional<Account> findByUserEmail(String email);

    boolean existsByCpf(String cpf);

    Optional<Account> findByCpf(String cpf);

    List<Account> findByInstitutionId(Long institutionId);

    List<Account> findBySchoolClassId(Long schoolClassId);
}
