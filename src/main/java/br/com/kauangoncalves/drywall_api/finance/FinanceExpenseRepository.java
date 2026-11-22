package br.com.kauangoncalves.drywall_api.finance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FinanceExpenseRepository extends JpaRepository<FinanceExpense, Long> {

    List<FinanceExpense> findAllByOwnerIdAndDateBetweenOrderByDateDescIdDesc(
            Long ownerId,
            LocalDate start,
            LocalDate end
    );

    List<FinanceExpense> findAllByOwnerIdOrderByDateDescIdDesc(Long ownerId);
}
