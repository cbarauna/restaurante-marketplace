package com.restaurantemarketplace.restaurant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Input;

@Input("CreateRestaurantInput")
@Description("Dados comerciais mínimos para iniciar o cadastro de um restaurante.")
public class CreateRestaurantInput {

    @NotBlank
    @Size(max = 150)
    public String tradeName;

    @NotBlank
    @Size(max = 200)
    public String legalName;

    @NotBlank
    @Pattern(regexp = "[0-9./-]{11,20}")
    public String taxId;

    @NotBlank
    @Email
    @Size(max = 254)
    public String email;

    @NotBlank
    @Size(max = 32)
    public String phone;
}
