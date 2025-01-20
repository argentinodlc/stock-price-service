package com.fada.stockpriceservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tickers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ticker {
    @Id
    private String symbol;
}