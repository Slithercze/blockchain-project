package com.gfa.blockchaingfa.repositories;

import com.gfa.blockchaingfa.models.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, String> {
    @Override
    List<Transaction> findAll();
    Optional<Transaction> findNewTransactionByPreviousTransactionId(String previousTransactionId);

    @Query(value = "SELECT * FROM transaction WHERE block_data_id IS null", nativeQuery = true)
    List<Transaction> getMempool();

    @Query(value = "SELECT * FROM transaction WHERE block_data_id IS NOT null", nativeQuery = true)
    List<Transaction> getMinedTransactions();


}
