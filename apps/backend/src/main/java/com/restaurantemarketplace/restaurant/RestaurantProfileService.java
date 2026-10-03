package com.restaurantemarketplace.restaurant;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.restaurantemarketplace.identity.AppUserEntity;
import com.restaurantemarketplace.identity.AppUserRepository;

@ApplicationScoped
public class RestaurantProfileService {

    private final RestaurantRepository restaurants;
    private final RestaurantAddressRepository addresses;
    private final EstablishmentTypeRepository establishmentTypes;
    private final RestaurantEstablishmentTypeRepository restaurantTypes;
    private final RestaurantMemberRepository members;
    private final AppUserRepository users;
    private final RestaurantAuthorization authorization;

    public RestaurantProfileService(
            RestaurantRepository restaurants,
            RestaurantAddressRepository addresses,
            EstablishmentTypeRepository establishmentTypes,
            RestaurantEstablishmentTypeRepository restaurantTypes,
            RestaurantMemberRepository members,
            AppUserRepository users,
            RestaurantAuthorization authorization) {
        this.restaurants = restaurants;
        this.addresses = addresses;
        this.establishmentTypes = establishmentTypes;
        this.restaurantTypes = restaurantTypes;
        this.members = members;
        this.users = users;
        this.authorization = authorization;
    }

    void registerOwner(UUID restaurantId, AppUserEntity user, Instant createdAt) {
        RestaurantMemberEntity member = new RestaurantMemberEntity();
        member.id = UUID.randomUUID();
        member.restaurantId = restaurantId;
        member.userId = user.id();
        member.role = RestaurantMemberRole.OWNER;
        member.createdAt = createdAt;
        members.persist(member);
    }

    @Transactional
    public RestaurantProfileView find(String restaurantId) {
        RestaurantEntity restaurant = getRestaurant(restaurantId);
        authorization.assertCanAccess(restaurant);
        return toView(restaurant.id);
    }

    @Transactional
    public List<EstablishmentTypeView> availableEstablishmentTypes() {
        return establishmentTypes.findActive().stream().map(RestaurantProfileService::toTypeView).toList();
    }

    @Transactional
    public RestaurantProfileView updateAddress(String restaurantId, RestaurantAddressInput input) {
        RestaurantEntity restaurant = getRestaurant(restaurantId);
        authorization.assertRestaurantRole(restaurant.id, RestaurantMemberRole.OWNER, RestaurantMemberRole.MANAGER);
        ensureProfileEditable(restaurant);
        if ((input.latitude == null) != (input.longitude == null)) {
            throw new RestaurantBusinessException(
                    "Latitude e longitude devem ser informadas em conjunto.");
        }

        Instant now = Instant.now();
        RestaurantAddressEntity address = addresses.findByIdOptional(restaurant.id).orElse(null);
        boolean newAddress = address == null;
        if (address == null) {
            address = new RestaurantAddressEntity();
            address.restaurantId = restaurant.id;
            address.createdAt = now;
        }
        address.postalCode = normalizePostalCode(input.postalCode);
        address.street = input.street.trim();
        address.number = input.number.trim();
        address.complement = trimToNull(input.complement);
        address.neighborhood = input.neighborhood.trim();
        address.city = input.city.trim();
        address.state = input.state.trim().toUpperCase(Locale.ROOT);
        address.latitude = input.latitude;
        address.longitude = input.longitude;
        address.updatedAt = now;
        if (newAddress) {
            addresses.persist(address);
        }
        restaurant.updatedAt = now;
        return toView(restaurant.id);
    }

    @Transactional
    public RestaurantProfileView setEstablishmentTypes(String restaurantId, List<String> typeCodes) {
        RestaurantEntity restaurant = getRestaurant(restaurantId);
        authorization.assertRestaurantRole(restaurant.id, RestaurantMemberRole.OWNER, RestaurantMemberRole.MANAGER);
        ensureProfileEditable(restaurant);

        Set<String> normalizedCodes = normalizeTypeCodes(typeCodes);
        List<EstablishmentTypeEntity> types = normalizedCodes.stream()
                .map(this::getActiveEstablishmentType)
                .toList();

        restaurantTypes.deleteByRestaurant(restaurant.id);
        Instant now = Instant.now();
        for (EstablishmentTypeEntity type : types) {
            RestaurantEstablishmentTypeEntity relation = new RestaurantEstablishmentTypeEntity();
            relation.id = UUID.randomUUID();
            relation.restaurantId = restaurant.id;
            relation.establishmentTypeCode = type.code;
            relation.createdAt = now;
            restaurantTypes.persist(relation);
        }
        restaurant.updatedAt = now;
        return toView(restaurant.id);
    }

    void assertCompleteForSubmission(RestaurantEntity restaurant) {
        RestaurantProfileCompletenessView completeness = completeness(restaurant.id);
        if (!completeness.complete()) {
            throw new RestaurantBusinessException(
                    "O perfil está incompleto. Pendências: " + completeness.missingRequirements() + ".");
        }
    }

    private RestaurantProfileView toView(UUID restaurantId) {
        RestaurantAddressView address = addresses.findByIdOptional(restaurantId)
                .map(RestaurantProfileService::toAddressView)
                .orElse(null);
        List<EstablishmentTypeView> types = restaurantTypes.findByRestaurant(restaurantId).stream()
                .map(relation -> establishmentTypes.findByIdOptional(relation.establishmentTypeCode)
                        .orElseThrow(() -> new RestaurantBusinessException("Tipo de estabelecimento não encontrado.")))
                .map(RestaurantProfileService::toTypeView)
                .toList();
        List<RestaurantMemberView> memberViews = members.findByRestaurant(restaurantId).stream()
                .map(this::toMemberView)
                .toList();
        return new RestaurantProfileView(
                restaurantId,
                address,
                types,
                memberViews,
                completeness(restaurantId));
    }

    private RestaurantProfileCompletenessView completeness(UUID restaurantId) {
        List<RestaurantProfileRequirement> missing = new ArrayList<>();
        if (addresses.findByIdOptional(restaurantId).isEmpty()) {
            missing.add(RestaurantProfileRequirement.ADDRESS);
        }
        if (restaurantTypes.count("restaurantId", restaurantId) == 0) {
            missing.add(RestaurantProfileRequirement.ESTABLISHMENT_TYPE);
        }
        if (members.countOwners(restaurantId) == 0) {
            missing.add(RestaurantProfileRequirement.OWNER);
        }
        return new RestaurantProfileCompletenessView(missing.isEmpty(), List.copyOf(missing));
    }

    private RestaurantEntity getRestaurant(String restaurantId) {
        final UUID id;
        try {
            id = UUID.fromString(restaurantId);
        } catch (IllegalArgumentException exception) {
            throw new RestaurantBusinessException("Identificador de restaurante inválido.");
        }
        return restaurants.findByIdOptional(id)
                .orElseThrow(() -> new RestaurantBusinessException("Restaurante não encontrado."));
    }

    private EstablishmentTypeEntity getActiveEstablishmentType(String code) {
        EstablishmentTypeEntity type = establishmentTypes.findByIdOptional(code)
                .orElseThrow(() -> new RestaurantBusinessException(
                        "Tipo de estabelecimento inválido: " + code + "."));
        if (!type.active) {
            throw new RestaurantBusinessException("Tipo de estabelecimento inativo: " + code + ".");
        }
        return type;
    }

    private static Set<String> normalizeTypeCodes(List<String> typeCodes) {
        if (typeCodes == null || typeCodes.isEmpty()) {
            throw new RestaurantBusinessException("Informe ao menos um tipo de estabelecimento.");
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String code : typeCodes) {
            if (code == null || code.isBlank()) {
                throw new RestaurantBusinessException("O código do tipo de estabelecimento é obrigatório.");
            }
            normalized.add(code.trim().toUpperCase(Locale.ROOT));
        }
        return normalized;
    }

    private static void ensureProfileEditable(RestaurantEntity restaurant) {
        if (restaurant.status != RestaurantStatus.DRAFT
                && restaurant.status != RestaurantStatus.CHANGES_REQUESTED) {
            throw new RestaurantBusinessException(
                    "O perfil não pode ser alterado no estado " + restaurant.status + ".");
        }
    }

    private static String normalizePostalCode(String postalCode) {
        return postalCode.replaceAll("\\D", "");
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static RestaurantAddressView toAddressView(RestaurantAddressEntity address) {
        return new RestaurantAddressView(
                address.postalCode,
                address.street,
                address.number,
                address.complement,
                address.neighborhood,
                address.city,
                address.state,
                address.latitude,
                address.longitude);
    }

    private static EstablishmentTypeView toTypeView(EstablishmentTypeEntity type) {
        return new EstablishmentTypeView(type.code, type.displayName);
    }

    private RestaurantMemberView toMemberView(RestaurantMemberEntity member) {
        AppUserEntity user = users.findByIdOptional(member.userId)
                .orElseThrow(() -> new RestaurantBusinessException("Usuário do membro não encontrado."));
        return new RestaurantMemberView(
                member.id,
                user.id(),
                user.oidcSubject(),
                user.email(),
                user.displayName(),
                member.role,
                member.createdAt);
    }
}
