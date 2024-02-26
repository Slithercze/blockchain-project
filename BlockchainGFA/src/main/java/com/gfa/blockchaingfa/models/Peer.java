package com.gfa.blockchaingfa.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "peers")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Peer {
    @Id
    private String ipAddress;
    private int port;
    private Timestamp lastActive;

    public String dejPath() {
        String schema = getIpAddress().matches("\\d+\\.\\d+\\.\\d+\\.\\d+") ? "http" : "http";
        return schema + "://" + getIpAddress() + ":" + getPort();
    }
}
