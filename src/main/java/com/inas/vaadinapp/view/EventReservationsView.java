package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Event;
import com.inas.vaadinapp.entity.Reservation;
import com.inas.vaadinapp.entity.ReservationStatus;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.EventService;
import com.inas.vaadinapp.service.ReservationService;
import com.inas.vaadinapp.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@PageTitle("Réservations - EventManager")
@Route("organizer/event/reservations")
public class EventReservationsView extends VerticalLayout implements HasUrlParameter<String> {

    private final EventService eventService;
    private final ReservationService reservationService;
    private final UserService userService;

    private Event currentEvent;
    private ListDataProvider<Reservation> dataProvider;
    private Grid<Reservation> grid;

    // Filtres
    private ComboBox<ReservationStatus> statusFilter;
    private TextField searchField;

    // Statistiques
    private Div totalReservationsCard;
    private Div totalPlacesCard;
    private Div totalRevenueCard;

    public EventReservationsView(EventService eventService, ReservationService reservationService, UserService userService) {
        this.eventService = eventService;
        this.reservationService = reservationService;
        this.userService = userService;

        // Vérifier l'utilisateur
        User currentUser = VaadinSession.getCurrent().getAttribute(User.class);
        if (currentUser == null) {
            UI.getCurrent().navigate("login");
            return;
        }

        if (currentUser.getRole() != com.inas.vaadinapp.entity.Role.ORGANIZER &&
            currentUser.getRole() != com.inas.vaadinapp.entity.Role.ADMIN) {
            UI.getCurrent().navigate("dashboard");
            return;
        }

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f8f9fa");
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (parameter == null) {
            // Pas d'ID d'événement fourni
            Notification.show("ID d'événement manquant", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            UI.getCurrent().navigate("organizer/events");
            return;
        }

        try {
            Long eventId = Long.parseLong(parameter);
            loadEvent(eventId);
        } catch (NumberFormatException e) {
            Notification.show("ID d'événement invalide", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            UI.getCurrent().navigate("organizer/events");
        }
    }

    private void loadEvent(Long eventId) {
        Optional<Event> eventOpt = eventService.findById(eventId);
        User currentUser = VaadinSession.getCurrent().getAttribute(User.class);

        if (eventOpt.isPresent()) {
            currentEvent = eventOpt.get();

            // Vérifier que l'utilisateur est l'organisateur ou admin
            if (!currentEvent.getOrganisateur().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != com.inas.vaadinapp.entity.Role.ADMIN) {
                Notification.show("Vous n'avez pas accès à cet événement", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                UI.getCurrent().navigate("organizer/events");
                return;
            }

            // Charger l'interface
            setupUI();
            loadReservations();
        } else {
            Notification.show("Événement introuvable", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            UI.getCurrent().navigate("organizer/events");
        }
    }

    private void setupUI() {
        // Header avec informations de l'événement
        createHeader();

        // Section statistiques
        createStatisticsSection();

        // Section filtres et recherche
        createFiltersSection();

        // Grille des réservations
        createGridSection();

        // Boutons d'action
        createActionsSection();
    }

    private void createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);
        header.setAlignItems(Alignment.CENTER);

        H1 title = new H1("Réservations - " + currentEvent.getTitre());
        title.getStyle()
                .set("color", "#333")
                .set("margin-bottom", "0.5rem")
                .set("text-align", "center");

        Span eventInfo = new Span(
            "📅 " + currentEvent.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
            " • 📍 " + currentEvent.getVille() + " • 👥 " + currentEvent.getCapaciteMax() + " places"
        );
        eventInfo.getStyle()
                .set("color", "#666")
                .set("font-size", "1rem")
                .set("text-align", "center");

        Button backBtn = new Button("Retour aux événements", new Icon(VaadinIcon.ARROW_LEFT));
        backBtn.addClickListener(e -> UI.getCurrent().navigate("organizer/events"));

        header.add(title, eventInfo, backBtn);
        add(header);
    }

    private void createStatisticsSection() {
        VerticalLayout statsSection = new VerticalLayout();
        statsSection.setPadding(true);
        statsSection.setSpacing(true);
        statsSection.setWidthFull();
        statsSection.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        H2 statsTitle = new H2("📊 Statistiques des réservations");
        statsTitle.getStyle().set("text-align", "center").set("margin-bottom", "1rem");

        HorizontalLayout statsCards = new HorizontalLayout();
        statsCards.setWidthFull();
        statsCards.setSpacing(true);

        // Carte total réservations
        totalReservationsCard = createStatsCard(
                "🎫 Total réservations",
                "0",
                "Nombre total de réservations"
        );

        // Carte places réservées
        totalPlacesCard = createStatsCard(
                "👥 Places réservées",
                "0",
                "Nombre total de places vendues"
        );

        // Carte revenus totaux
        totalRevenueCard = createStatsCard(
                "💰 Revenus totaux",
                "0.00 dh",
                "Montant total généré"
        );

        statsCards.add(totalReservationsCard, totalPlacesCard, totalRevenueCard);
        statsSection.add(statsTitle, statsCards);
        add(statsSection);
    }

    private void createFiltersSection() {
        VerticalLayout filtersSection = new VerticalLayout();
        filtersSection.setPadding(true);
        filtersSection.setSpacing(true);
        filtersSection.setWidthFull();
        filtersSection.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        H3 filtersTitle = new H3("🔍 Filtres et recherche");

        HorizontalLayout filtersRow = new HorizontalLayout();
        filtersRow.setWidthFull();
        filtersRow.setSpacing(true);

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setItemLabelGenerator(status -> getStatusLabel(status));
        statusFilter.setClearButtonVisible(true);
        statusFilter.setPlaceholder("Tous les statuts");
        statusFilter.addValueChangeListener(e -> applyFilters());

        searchField = new TextField("Recherche");
        searchField.setPlaceholder("Nom utilisateur ou code réservation...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> applyFilters());

        Button resetBtn = new Button("Réinitialiser", new Icon(VaadinIcon.REFRESH));
        resetBtn.addClickListener(e -> {
            statusFilter.clear();
            searchField.clear();
            applyFilters();
        });

        filtersRow.add(statusFilter, searchField, resetBtn);
        filtersSection.add(filtersTitle, filtersRow);
        add(filtersSection);
    }

    private void createGridSection() {
        grid = new Grid<>(Reservation.class, false);
        configureGrid();
        grid.setSizeFull();

        VerticalLayout gridContainer = new VerticalLayout();
        gridContainer.setPadding(false);
        gridContainer.setSpacing(false);
        gridContainer.setSizeFull();
        gridContainer.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("padding", "1rem");

        gridContainer.add(grid);
        add(gridContainer);
    }

    private void createActionsSection() {
        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setPadding(true);
        actionsLayout.setSpacing(true);

        Button exportCsvBtn = new Button("📊 Exporter CSV", new Icon(VaadinIcon.DOWNLOAD));
        exportCsvBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        exportCsvBtn.addClickListener(e -> exportToCsv());

        Button refreshBtn = new Button("Actualiser", new Icon(VaadinIcon.REFRESH));
        refreshBtn.addClickListener(e -> {
            loadReservations();
            applyFilters();
        });

        actionsLayout.add(exportCsvBtn, refreshBtn);
        add(actionsLayout);
    }

    private void configureGrid() {
        // Client (nom + prénom)
        grid.addColumn(reservation ->
                reservation.getClient().getPrenom() + " " + reservation.getClient().getNom()
        ).setHeader("Client")
                .setAutoWidth(true)
                .setSortable(true);

        // Email client
        grid.addColumn(reservation -> reservation.getClient().getEmail())
                .setHeader("Email")
                .setAutoWidth(true);

        // Code réservation
        grid.addColumn(Reservation::getCodeReservation)
                .setHeader("Code réservation")
                .setAutoWidth(true)
                .setSortable(true);

        // Nombre de places
        grid.addColumn(Reservation::getNbPlaces)
                .setHeader("Places")
                .setAutoWidth(true)
                .setSortable(true);

        // Prix unitaire
        grid.addColumn(reservation -> String.format("%.2f dh", reservation.getPrixUnitaire()))
                .setHeader("Prix unitaire")
                .setAutoWidth(true);

        // Montant total
        grid.addColumn(reservation -> String.format("%.2f dh", reservation.getMontantTotal()))
                .setHeader("Total")
                .setAutoWidth(true)
                .setSortable(true);

        // Date de réservation
        grid.addColumn(reservation ->
                reservation.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        ).setHeader("Date réservation")
                .setAutoWidth(true)
                .setSortable(true);

        // Statut avec style
        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Statut")
                .setAutoWidth(true);

        // Actions
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Actions")
                .setAutoWidth(true);
    }

    private Span createStatusBadge(Reservation reservation) {
        Span badge = new Span(getStatusLabel(reservation.getStatus()));
        badge.getStyle()
                .set("padding", "0.25rem 0.5rem")
                .set("border-radius", "12px")
                .set("font-size", "0.85rem")
                .set("font-weight", "bold")
                .set("color", "white")
                .set("background-color", getStatusColor(reservation.getStatus()));

        return badge;
    }

    private HorizontalLayout createActionButtons(Reservation reservation) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        // Bouton Voir détails
        Button viewBtn = new Button(new Icon(VaadinIcon.EYE));
        viewBtn.getElement().setAttribute("title", "Voir les détails");
        viewBtn.addClickListener(e -> showReservationDetails(reservation));

        // Bouton Confirmer (seulement si EN_ATTENTE)
        Button confirmBtn = new Button(new Icon(VaadinIcon.CHECK));
        confirmBtn.getElement().setAttribute("title", "Confirmer la réservation");
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        confirmBtn.setVisible(reservation.getStatus() == ReservationStatus.EN_ATTENTE);
        confirmBtn.addClickListener(e -> confirmReservation(reservation));

        // Bouton Annuler (seulement si EN_ATTENTE ou CONFIRMEE)
        Button cancelBtn = new Button(new Icon(VaadinIcon.CLOSE));
        cancelBtn.getElement().setAttribute("title", "Annuler la réservation");
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelBtn.setVisible(reservation.getStatus() == ReservationStatus.EN_ATTENTE ||
                           reservation.getStatus() == ReservationStatus.CONFIRMEE);
        cancelBtn.addClickListener(e -> cancelReservation(reservation));

        actions.add(viewBtn, confirmBtn, cancelBtn);
        return actions;
    }

    private void loadReservations() {
        List<Reservation> reservations = reservationService.findByClient(0L).stream() // On utilise une liste vide pour commencer
                .filter(r -> r.getEvent().getId().equals(currentEvent.getId()))
                .sorted((r1, r2) -> r2.getDateReservation().compareTo(r1.getDateReservation()))
                .collect(Collectors.toList());

        // Charger les réservations depuis le repository directement car ReservationService ne les a pas toutes
        try {
            java.lang.reflect.Field reservationRepositoryField = ReservationService.class.getDeclaredField("reservationRepository");
            reservationRepositoryField.setAccessible(true);
            var reservationRepository = reservationRepositoryField.get(reservationService);

            var findByEventIdMethod = reservationRepository.getClass().getMethod("findByEventId", Long.class);
            reservations = (List<Reservation>) findByEventIdMethod.invoke(reservationRepository, currentEvent.getId());
        } catch (Exception e) {
            // Fallback: utiliser les réservations de l'événement
            reservations = currentEvent.getReservations();
        }

        dataProvider = new ListDataProvider<>(reservations);
        grid.setDataProvider(dataProvider);

        // Mettre à jour les statistiques
        updateStatistics(reservations);
    }

    private void updateStatistics(List<Reservation> reservations) {
        int totalReservations = reservations.size();
        int totalPlaces = reservations.stream()
                .filter(r -> r.getStatus() != ReservationStatus.ANNULEE)
                .mapToInt(Reservation::getNbPlaces)
                .sum();
        double totalRevenue = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMEE)
                .mapToDouble(Reservation::getMontantTotal)
                .sum();

        // Mettre à jour les cartes de statistiques
        updateStatsCard(totalReservationsCard, "🎫 Total réservations", String.valueOf(totalReservations), "Nombre total de réservations");
        updateStatsCard(totalPlacesCard, "👥 Places réservées", String.valueOf(totalPlaces), "Nombre total de places vendues");
        updateStatsCard(totalRevenueCard, "💰 Revenus totaux", String.format("%.2f dh", totalRevenue), "Montant total généré");
    }

    private void updateStatsCard(Div card, String title, String value, String subtitle) {
        // Vider la carte et la recréer
        card.removeAll();
        card.add(new H3(title), new H2(value), new Span(subtitle));
    }

    private void applyFilters() {
        dataProvider.setFilter(reservation -> {
            // Filtre par statut
            if (statusFilter.getValue() != null) {
                if (reservation.getStatus() != statusFilter.getValue()) {
                    return false;
                }
            }

            // Filtre par recherche
            if (searchField.getValue() != null && !searchField.getValue().trim().isEmpty()) {
                String searchTerm = searchField.getValue().toLowerCase().trim();
                String clientName = (reservation.getClient().getPrenom() + " " +
                                   reservation.getClient().getNom()).toLowerCase();
                String clientEmail = reservation.getClient().getEmail().toLowerCase();
                String reservationCode = reservation.getCodeReservation().toLowerCase();

                if (!clientName.contains(searchTerm) &&
                    !clientEmail.contains(searchTerm) &&
                    !reservationCode.contains(searchTerm)) {
                    return false;
                }
            }

            return true;
        });
    }

    private void confirmReservation(Reservation reservation) {
        try {
            User currentUser = VaadinSession.getCurrent().getAttribute(User.class);
            Reservation confirmed = reservationService.confirmReservation(reservation.getId(), currentUser.getId());

            Notification notification = new Notification(
                "Réservation confirmée pour " + confirmed.getClient().getPrenom() + " " + confirmed.getClient().getNom(),
                3000
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.open();

            loadReservations();
            applyFilters();
        } catch (Exception e) {
            Notification notification = new Notification("Erreur lors de la confirmation: " + e.getMessage(), 5000);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.open();
        }
    }

    private void cancelReservation(Reservation reservation) {
        try {
            User currentUser = VaadinSession.getCurrent().getAttribute(User.class);
            reservationService.cancelReservation(reservation.getId(), currentUser.getId());

            Notification notification = new Notification(
                "Réservation annulée pour " + reservation.getClient().getPrenom() + " " + reservation.getClient().getNom(),
                3000
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.open();

            loadReservations();
            applyFilters();
        } catch (Exception e) {
            Notification notification = new Notification("Erreur lors de l'annulation: " + e.getMessage(), 5000);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.open();
        }
    }

    private void showReservationDetails(Reservation reservation) {
        // Créer une boîte de dialogue avec les détails
        String details = String.format(
            "Détails de la réservation %s\n\n" +
            "Client: %s %s\n" +
            "Email: %s\n" +
            "Événement: %s\n" +
            "Places: %d\n" +
            "Prix unitaire: %.2f dh\n" +
            "Total: %.2f dh\n" +
            "Date: %s\n" +
            "Statut: %s",
            reservation.getCodeReservation(),
            reservation.getClient().getPrenom(),
            reservation.getClient().getNom(),
            reservation.getClient().getEmail(),
            currentEvent.getTitre(),
            reservation.getNbPlaces(),
            reservation.getPrixUnitaire(),
            reservation.getMontantTotal(),
            reservation.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            getStatusLabel(reservation.getStatus())
        );

        if (reservation.getCommentaire() != null && !reservation.getCommentaire().trim().isEmpty()) {
            details += "\n\nCommentaire: " + reservation.getCommentaire();
        }

        Notification notification = new Notification(details.replace("\n", "<br>"), 10000);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.open();
    }

    private void exportToCsv() {
        List<Reservation> allReservations = dataProvider.getItems().stream().collect(Collectors.toList());

        StringBuilder csv = new StringBuilder();
        csv.append("Code réservation,Client,Email,Places,Prix unitaire,Total,Date réservation,Statut\n");

        for (Reservation reservation : allReservations) {
            csv.append(reservation.getCodeReservation()).append(",");
            csv.append("\"").append(reservation.getClient().getPrenom()).append(" ").append(reservation.getClient().getNom()).append("\",");
            csv.append(reservation.getClient().getEmail()).append(",");
            csv.append(reservation.getNbPlaces()).append(",");
            csv.append(String.format("%.2f", reservation.getPrixUnitaire())).append(",");
            csv.append(String.format("%.2f", reservation.getMontantTotal())).append(",");
            csv.append(reservation.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append(",");
            csv.append(getStatusLabel(reservation.getStatus())).append("\n");
        }

        StreamResource resource = new StreamResource(
            "reservations_" + currentEvent.getTitre().replaceAll("[^a-zA-Z0-9]", "_") + ".csv",
            () -> new ByteArrayInputStream(csv.toString().getBytes())
        );

        // Télécharger le fichier
        Anchor downloadLink = new Anchor(resource, "");
        downloadLink.getElement().setAttribute("download", true);
        downloadLink.getElement().callJsFunction("click");

        Notification notification = new Notification("Export CSV généré avec succès", 3000);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.open();
    }

    private String getStatusLabel(ReservationStatus status) {
        switch (status) {
            case EN_ATTENTE: return "En attente";
            case CONFIRMEE: return "Confirmée";
            case ANNULEE: return "Annulée";
            default: return status.toString();
        }
    }

    private String getStatusColor(ReservationStatus status) {
        switch (status) {
            case EN_ATTENTE: return "#ffc107";
            case CONFIRMEE: return "#28a745";
            case ANNULEE: return "#dc3545";
            default: return "#666";
        }
    }

    private Div createStatsCard(String title, String value, String subtitle) {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
                .set("padding", "1.5rem")
                .set("text-align", "center")
                .set("flex", "1")
                .set("min-width", "200px")
                .set("margin", "0.5rem");

        H3 cardTitle = new H3(title);
        cardTitle.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("color", "#666")
                .set("font-size", "1rem");

        H2 cardValue = new H2(value);
        cardValue.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("color", "#333")
                .set("font-size", "2rem");

        Span cardSubtitle = new Span(subtitle);
        cardSubtitle.getStyle()
                .set("color", "#999")
                .set("font-size", "0.9rem");

        card.add(cardTitle, cardValue, cardSubtitle);
        return card;
    }
}
