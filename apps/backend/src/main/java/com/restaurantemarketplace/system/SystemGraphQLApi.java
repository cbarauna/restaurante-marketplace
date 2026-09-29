package com.restaurantemarketplace.system;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
@ApplicationScoped
public class SystemGraphQLApi {

    private final String applicationName;
    private final String applicationVersion;

    public SystemGraphQLApi(
            @ConfigProperty(name = "quarkus.application.name") String applicationName,
            @ConfigProperty(name = "quarkus.application.version") String applicationVersion) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
    }

    @Query("systemInfo")
    @Description("Retorna a identificação e o estado básico do backend.")
    public SystemInfo systemInfo() {
        return new SystemInfo(applicationName, applicationVersion, "UP");
    }
}
