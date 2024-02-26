package com.gfa.blockchaingfa.repositories;

import com.gfa.blockchaingfa.models.Peer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeerRepository extends CrudRepository<Peer, String> {
    @Query(value = "SELECT * FROM peers ORDER BY last_active DESC LIMIT 10", nativeQuery = true)
    List<Peer> findTenLastActivePeers();

    @Override
    List<Peer> findAll();
}
