package com.gfa.blockchaingfa.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bouncycastle.jcajce.provider.digest.SHA256;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class BlockData {
    @Id
    private String hash;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "block_data_id", referencedColumnName = "hash")
    private List<Transaction> transactions;
    private String data;
    private long timestamp = System.currentTimeMillis() / 1000L;


    public BlockData(String data) {
        this.data = data;
    }

    public void setHash() {
        SHA256.Digest digest = new SHA256.Digest();

        String txt = transactions + data + timestamp;
        byte[] hash = digest.digest(txt.getBytes());
        this.hash = bytesToHex(hash);
    }

    public String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
