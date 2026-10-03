package com.restaurantemarketplace.restaurant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "establishment_type")
public class EstablishmentTypeEntity extends PanacheEntityBase {

    @Id
    @Column(length = 50)
    String code;

    @Column(name = "display_name", nullable = false, length = 100)
    String displayName;

    @Column(nullable = false)
    boolean active;
}
