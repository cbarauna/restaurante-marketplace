package com.restaurantemarketplace.restaurant;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Input;

@Input("RestaurantAddressInput")
@Description("Endereço operacional do restaurante.")
public class RestaurantAddressInput {

    @NotBlank
    @Pattern(regexp = "[0-9-]{8,9}")
    public String postalCode;

    @NotBlank
    @Size(max = 200)
    public String street;

    @NotBlank
    @Size(max = 30)
    public String number;

    @Size(max = 120)
    public String complement;

    @NotBlank
    @Size(max = 120)
    public String neighborhood;

    @NotBlank
    @Size(max = 120)
    public String city;

    @NotBlank
    @Pattern(regexp = "[A-Za-z]{2}")
    public String state;

    @DecimalMin("-90")
    @DecimalMax("90")
    public BigDecimal latitude;

    @DecimalMin("-180")
    @DecimalMax("180")
    public BigDecimal longitude;
}
