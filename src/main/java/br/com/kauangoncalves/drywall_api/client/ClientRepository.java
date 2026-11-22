package br.com.kauangoncalves.drywall_api.client;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findAllByOwnerIdOrderByNameAsc(Long ownerId);
}