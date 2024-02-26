package com.gfa.blockchaingfa.repositories;

import com.gfa.blockchaingfa.models.BlockData;
import com.gfa.blockchaingfa.models.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockDataRepository extends CrudRepository<BlockData, String> {
}
