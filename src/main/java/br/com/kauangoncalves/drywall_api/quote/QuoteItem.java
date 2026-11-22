package br.com.kauangoncalves.drywall_api.quote;

import java.math.BigDecimal;

import br.com.kauangoncalves.drywall_api.material.Material;
import br.com.kauangoncalves.drywall_api.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "quote_items")
public class QuoteItem extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private Material material;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    protected QuoteItem() {
    }

    public QuoteItem(
            Quote quote,
            Material material,
            String description,
            String unit,
            BigDecimal quantity,
            BigDecimal unitPrice) {

        this.quote = quote;
        this.material = material;
        this.description = description;
        this.unit = unit;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.total = quantity.multiply(unitPrice);
    }

    public Quote getQuote() {
        return quote;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDescription() {
        return description;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotal() {
        return total;
    }
}