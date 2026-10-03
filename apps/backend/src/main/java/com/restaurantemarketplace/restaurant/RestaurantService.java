package com.restaurantemarketplace.restaurant;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.restaurantemarketplace.identity.AppUserEntity;
import com.restaurantemarketplace.identity.CurrentUserService;

@ApplicationScoped
public class RestaurantService {

    private final RestaurantRepository restaurants;
    private final RestaurantStatusHistoryRepository history;
    private final RestaurantProfileService profiles;
    private final RestaurantAuthorization authorization;
    private final CurrentUserService currentUser;

    public RestaurantService(
            RestaurantRepository restaurants,
            RestaurantStatusHistoryRepository history,
            RestaurantProfileService profiles,
            RestaurantAuthorization authorization,
            CurrentUserService currentUser) {
        this.restaurants = restaurants;
        this.history = history;
        this.profiles = profiles;
        this.authorization = authorization;
        this.currentUser = currentUser;
    }

    @Transactional
    public RestaurantView create(CreateRestaurantInput input) {
        authorization.assertHasRole("restaurant-owner");
        String taxId = normalizeTaxId(input.taxId);
        if (taxId.length() < 11 || taxId.length() > 14) {
            throw new RestaurantBusinessException("O documento deve ter entre 11 e 14 dígitos.");
        }
        if (restaurants.findByTaxId(taxId).isPresent()) {
            throw new RestaurantBusinessException("Já existe um restaurante cadastrado com este documento.");
        }

        Instant now = Instant.now();
        AppUserEntity owner = currentUser.getOrCreate();
        RestaurantEntity restaurant = new RestaurantEntity();
        restaurant.id = UUID.randomUUID();
        restaurant.ownerUserId = owner.id();
        restaurant.tradeName = input.tradeName.trim();
        restaurant.legalName = input.legalName.trim();
        restaurant.taxId = taxId;
        restaurant.email = input.email.trim().toLowerCase(Locale.ROOT);
        restaurant.phone = input.phone.trim();
        restaurant.status = RestaurantStatus.DRAFT;
        restaurant.createdAt = now;
        restaurant.updatedAt = now;
        restaurants.persist(restaurant);
        profiles.registerOwner(restaurant.id, owner, now);
        recordTransition(restaurant.id, null, RestaurantStatus.DRAFT, "Cadastro criado", now);
        return toView(restaurant);
    }

    @Transactional
    public RestaurantView find(String restaurantId) {
        RestaurantEntity restaurant = getRestaurant(restaurantId);
        authorization.assertCanAccess(restaurant);
        return toView(restaurant);
    }

    @Transactional
    public List<RestaurantStatusHistoryView> history(String restaurantId) {
        RestaurantEntity restaurant = getRestaurant(restaurantId);
        authorization.assertCanAccess(restaurant);
        return history.findByRestaurant(restaurant.id).stream()
                .map(RestaurantService::toHistoryView)
                .toList();
    }

    @Transactional
    public List<RestaurantView> underReview() {
        authorization.assertHasRole("platform-admin");
        return restaurants.findUnderReview().stream().map(RestaurantService::toView).toList();
    }

    @Transactional
    public RestaurantView submit(String restaurantId) {
        RestaurantEntity restaurant = getRestaurant(restaurantId);
        authorization.assertRestaurantRole(restaurant.id, RestaurantMemberRole.OWNER, RestaurantMemberRole.MANAGER);
        ensureCurrentStatus(restaurant, RestaurantStatus.DRAFT, RestaurantStatus.CHANGES_REQUESTED);
        profiles.assertCompleteForSubmission(restaurant);

        Instant now = Instant.now();
        restaurant.submittedAt = now;
        restaurant.reviewNote = null;
        transition(restaurant, RestaurantStatus.UNDER_REVIEW, null, now);
        return toView(restaurant);
    }

    @Transactional
    public RestaurantView approve(String restaurantId) {
        authorization.assertHasRole("platform-admin");
        RestaurantEntity restaurant = getRestaurant(restaurantId);
        ensureCurrentStatus(restaurant, RestaurantStatus.UNDER_REVIEW);
        profiles.assertCompleteForSubmission(restaurant);

        Instant now = Instant.now();
        restaurant.reviewedAt = now;
        restaurant.reviewNote = null;
        transition(restaurant, RestaurantStatus.APPROVED, null, now);
        return toView(restaurant);
    }

    @Transactional
    public RestaurantView reject(String restaurantId, String reason) {
        authorization.assertHasRole("platform-admin");
        return reviewWithReason(restaurantId, RestaurantStatus.REJECTED, reason);
    }

    @Transactional
    public RestaurantView requestChanges(String restaurantId, String reason) {
        authorization.assertHasRole("platform-admin");
        return reviewWithReason(restaurantId, RestaurantStatus.CHANGES_REQUESTED, reason);
    }

    @Transactional
    public RestaurantView activate(String restaurantId) {
        authorization.assertHasRole("platform-admin");
        RestaurantEntity restaurant = getRestaurant(restaurantId);
        ensureCurrentStatus(restaurant, RestaurantStatus.APPROVED);

        Instant now = Instant.now();
        restaurant.activatedAt = now;
        transition(restaurant, RestaurantStatus.ACTIVE, null, now);
        return toView(restaurant);
    }

    @Transactional
    public RestaurantView suspend(String restaurantId, String reason) {
        authorization.assertHasRole("platform-admin");
        requireReason(reason);
        RestaurantEntity restaurant = getRestaurant(restaurantId);
        ensureCurrentStatus(restaurant, RestaurantStatus.ACTIVE);
        transition(restaurant, RestaurantStatus.SUSPENDED, reason.trim(), Instant.now());
        return toView(restaurant);
    }

    @Transactional
    public RestaurantView reactivate(String restaurantId) {
        authorization.assertHasRole("platform-admin");
        RestaurantEntity restaurant = getRestaurant(restaurantId);
        ensureCurrentStatus(restaurant, RestaurantStatus.SUSPENDED);
        transition(restaurant, RestaurantStatus.ACTIVE, null, Instant.now());
        return toView(restaurant);
    }

    private RestaurantView reviewWithReason(String restaurantId, RestaurantStatus target, String reason) {
        requireReason(reason);
        RestaurantEntity restaurant = getRestaurant(restaurantId);
        ensureCurrentStatus(restaurant, RestaurantStatus.UNDER_REVIEW);

        Instant now = Instant.now();
        restaurant.reviewedAt = now;
        restaurant.reviewNote = reason.trim();
        transition(restaurant, target, reason.trim(), now);
        return toView(restaurant);
    }

    private void transition(RestaurantEntity restaurant, RestaurantStatus target, String reason, Instant now) {
        RestaurantStatus previous = restaurant.status;
        restaurant.status = target;
        restaurant.updatedAt = now;
        recordTransition(restaurant.id, previous, target, reason, now);
    }

    private void recordTransition(
            UUID restaurantId,
            RestaurantStatus previous,
            RestaurantStatus target,
            String reason,
            Instant changedAt) {
        RestaurantStatusHistoryEntity entry = new RestaurantStatusHistoryEntity();
        entry.id = UUID.randomUUID();
        entry.restaurantId = restaurantId;
        entry.previousStatus = previous;
        entry.newStatus = target;
        entry.reason = reason;
        entry.changedBy = authorization.currentActor();
        entry.changedAt = changedAt;
        history.persist(entry);
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

    private void ensureCurrentStatus(RestaurantEntity restaurant, RestaurantStatus... allowed) {
        for (RestaurantStatus status : allowed) {
            if (restaurant.status == status) {
                return;
            }
        }
        throw new RestaurantBusinessException(
                "Transição inválida para restaurante no estado " + restaurant.status + ".");
    }

    private static void requireReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new RestaurantBusinessException("A justificativa é obrigatória.");
        }
    }

    private static String normalizeTaxId(String taxId) {
        return taxId.replaceAll("\\D", "");
    }

    private static RestaurantView toView(RestaurantEntity restaurant) {
        return new RestaurantView(
                restaurant.id,
                restaurant.tradeName,
                restaurant.legalName,
                restaurant.taxId,
                restaurant.email,
                restaurant.phone,
                restaurant.status,
                restaurant.reviewNote,
                restaurant.submittedAt,
                restaurant.reviewedAt,
                restaurant.activatedAt,
                restaurant.createdAt,
                restaurant.updatedAt,
                restaurant.version);
    }

    private static RestaurantStatusHistoryView toHistoryView(RestaurantStatusHistoryEntity entry) {
        return new RestaurantStatusHistoryView(
                entry.id,
                entry.previousStatus,
                entry.newStatus,
                entry.reason,
                entry.changedBy,
                entry.changedAt);
    }
}
