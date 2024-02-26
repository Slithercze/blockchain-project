package com.gfa.blockchaingfa.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Work {
    private List<Transaction> txList;
    private String previousHash;
    private String difficulty;
    private String height;

}
