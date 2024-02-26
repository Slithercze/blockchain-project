package com.gfa.blockchaingfa.models;

import com.google.gson.Gson;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bouncycastle.jcajce.provider.digest.SHA256;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "blocks")
public class Block {
    @Id
    private String hash;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "block_data_id", referencedColumnName = "hash")
    private BlockData blockData;
    private String previousHash;
    private long timestamp = System.currentTimeMillis() / 1000L;
    private int nonce;

    public String calculateHash() {
        SHA256.Digest digest = new SHA256.Digest();
        Gson gson = new Gson(); // Create new Gson instance
        String jsonBlockData = gson.toJson(blockData);

        String txt = previousHash + timestamp + jsonBlockData + nonce;
        byte[] hash = digest.digest(txt.getBytes());
        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
