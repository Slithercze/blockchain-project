package com.gfa.blockchaingfa.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bouncycastle.jcajce.provider.digest.SHA256;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Transaction {
    @Id
    private String hash;
    private long amount = 1;
    private String toAddress;
    private long timestamp = System.currentTimeMillis();
    private String previousTransactionId;
    private String signature;


    public String calculateHash() {
        SHA256.Digest digest = new SHA256.Digest();
        String txt = toAddress + previousTransactionId + amount + timestamp;
        byte[] hash = digest.digest(txt.getBytes());
        return bytesToHex(hash);
    }

    public String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
