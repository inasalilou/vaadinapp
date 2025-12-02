package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Reservation;
import com.inas.vaadinapp.entity.ReservationStatus;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.ReservationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Route("my-reservations")
@PageTitle("Mes réservations - EventManager")
public class MyReservationsView extends VerticalLayout {

    private final ReservationService reservationService;
    private Grid<Reservation> grid;
    private ListDataProvider<Reservation> dataProvider;
    private User currentUser;

    // Filtres et contrôles
    private ComboBox<ReservationStatus> statusFilter;
    private TextField codeSearchField;
    private Button searchButton;
    private Button resetButton;

    public MyReservationsView(ReservationService reservationService) {
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");

        // Vérification d'authentification
        currentUser = VaadinSession.getCurrent().getAttribute(User.class);
        if (currentUser == null) {
            Notification.show("Vous devez être connecté pour voir vos réservations.", 3000, Notification.Position.TOP_CENTER);
            UI.getCurrent().navigate("login");
            return;
        }

        buildUI();
    }

    private void buildUI() {
        // Header avec navigation
        VerticalLayout header = new VerticalLayout();
        header.setPadding(true);
        header.setSpacing(false);
        header.setWidthFull();
        header.getStyle()
                .set("background", "white")
                .set("border-radius", "0 0 16px 16px");

        HorizontalLayout navBar = new HorizontalLayout();
        navBar.setWidthFull();
        navBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        navBar.setAlignItems(Alignment.CENTER);

        Button backBtn = new Button("← Retour au tableau de bord", new Icon(VaadinIcon.ARROW_LEFT));
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.addClickListener(e -> UI.getCurrent().navigate("dashboard"));

        H2 pageTitle = new H2("Mes réservations");
        pageTitle.getStyle()
                .set("color", "#333")
                .set("margin", "0");

        navBar.add(backBtn, pageTitle);
        header.add(navBar);

        // Section filtres
        VerticalLayout filtersSection = new VerticalLayout();
        filtersSection.setPadding(true);
        filtersSection.setSpacing(true);
        filtersSection.setWidthFull();
        filtersSection.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("margin", "1rem")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        H3 filtersTitle = new H3("🔍 Filtrer et rechercher");
        filtersTitle.getStyle()
                .set("margin-bottom", "1rem")
                .set("color", "#333");

        // Ligne de filtres
        HorizontalLayout filtersRow = new HorizontalLayout();
        filtersRow.setWidthFull();
        filtersRow.setSpacing(true);
        filtersRow.setAlignItems(Alignment.END);

        // Filtre par statut
        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setPlaceholder("Tous les statuts");
        statusFilter.setClearButtonVisible(true);

        // Recherche par code
        codeSearchField = new TextField("Code de réservation");
        codeSearchField.setPlaceholder("Ex: EVT-01234");
        codeSearchField.setClearButtonVisible(true);
        codeSearchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));

        // Boutons d'action
        searchButton = new Button("Appliquer", new Icon(VaadinIcon.FILTER));
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> applyFilters());

        resetButton = new Button("Réinitialiser", new Icon(VaadinIcon.REFRESH));
        resetButton.addClickListener(e -> resetFilters());

        filtersRow.add(statusFilter, codeSearchField, searchButton, resetButton);
        filtersSection.add(filtersTitle, filtersRow);

        // Section grille
        VerticalLayout gridSection = new VerticalLayout();
        gridSection.setPadding(true);
        gridSection.setSpacing(true);
        gridSection.setWidthFull();

        H3 gridTitle = new H3("📋 Liste de vos réservations");
        gridTitle.getStyle()
                .set("margin-bottom", "1rem")
                .set("color", "#333");

        grid = new Grid<>(Reservation.class, false);
        configureGrid();

        Span statsInfo = new Span();
        statsInfo.getStyle()
                .set("color", "#666")
                .set("font-size", "0.9rem")
                .set("margin-top", "0.5rem");

        gridSection.add(gridTitle, grid, statsInfo);

        // Assembler tout
        add(header, filtersSection, gridSection);

        loadReservations();
    }

    /* -------------------- CONFIGURATION GRILLE -------------------- */

    private void configureGrid() {
        grid.setWidthFull();
        grid.setHeight("500px");

        // Colonne Code de réservation
        grid.addColumn(Reservation::getCodeReservation)
                .setHeader("Code réservation")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Événement avec indicateur visuel pour événements à venir
        grid.addComponentColumn(this::createEventCell)
                .setHeader("Événement")
                .setSortable(true)
                .setWidth("250px");

        // Colonne Date de réservation
        grid.addColumn(reservation ->
                reservation.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setHeader("Date réservation")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Nombre de places
        grid.addColumn(Reservation::getNbPlaces)
                .setHeader("Places")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Montant total
        grid.addColumn(reservation -> String.format("%.2f €", reservation.getMontantTotal()))
                .setHeader("Montant")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Statut avec style coloré
        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Statut")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Actions
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Actions")
                .setAutoWidth(true);

        // Pagination
        grid.setPageSize(10);
    }

    private VerticalLayout createEventCell(Reservation reservation) {
        VerticalLayout cell = new VerticalLayout();
        cell.setSpacing(false);
        cell.setPadding(false);

        // Titre de l'événement
        Span title = new Span(reservation.getEvent().getTitre());
        title.getStyle()
                .set("font-weight", "bold")
                .set("color", "#333");

        // Informations supplémentaires
        Span details = new Span(
                reservation.getEvent().getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                " • " + reservation.getEvent().getVille()
        );
        details.getStyle()
                .set("font-size", "0.85rem")
                .set("color", "#666");

        // Indicateur visuel pour événements à venir
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventDate = reservation.getEvent().getDateDebut();

        if (eventDate.isAfter(now)) {
            long daysUntil = java.time.Duration.between(now, eventDate).toDays();
            Span upcomingBadge = new Span();
            upcomingBadge.getStyle()
                    .set("background-color", "#28a745")
                    .set("color", "white")
                    .set("padding", "2px 6px")
                    .set("border-radius", "10px")
                    .set("font-size", "0.75rem")
                    .set("font-weight", "bold")
                    .set("margin-left", "8px");

            if (daysUntil == 0) {
                upcomingBadge.setText("AUJOURD'HUI");
            } else if (daysUntil == 1) {
                upcomingBadge.setText("DEMAIN");
            } else if (daysUntil <= 7) {
                upcomingBadge.setText("DANS " + daysUntil + " JOURS");
            } else {
                upcomingBadge.setText("À VENIR");
                upcomingBadge.getStyle().set("background-color", "#17a2b8");
            }

            HorizontalLayout titleRow = new HorizontalLayout();
            titleRow.setSpacing(false);
            titleRow.setAlignItems(Alignment.CENTER);
            titleRow.add(title, upcomingBadge);
            cell.add(titleRow);
        } else {
            cell.add(title);
        }

        cell.add(details);
        return cell;
    }

    private Span createStatusBadge(Reservation reservation) {
        Span badge = new Span(reservation.getStatus().toString());
        badge.getStyle()
                .set("padding", "4px 8px")
                .set("border-radius", "12px")
                .set("font-size", "0.8rem")
                .set("font-weight", "bold")
                .set("text-align", "center");

        switch (reservation.getStatus()) {
            case EN_ATTENTE:
                badge.getStyle()
                        .set("background-color", "#ffc107")
                        .set("color", "#000");
                break;
            case CONFIRMEE:
                badge.getStyle()
                        .set("background-color", "#28a745")
                        .set("color", "white");
                break;
            case ANNULEE:
                badge.getStyle()
                        .set("background-color", "#dc3545")
                        .set("color", "white");
                break;
        }

        return badge;
    }

    private HorizontalLayout createActionButtons(Reservation reservation) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        // Bouton Voir détails
        Button detailsBtn = new Button("Voir détails", new Icon(VaadinIcon.EYE));
        detailsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        detailsBtn.addClickListener(e -> UI.getCurrent().navigate("event/" + reservation.getEvent().getId()));

        // Bouton Annuler (seulement si EN_ATTENTE et plus de 48h avant l'événement)
        if (reservation.getStatus() == ReservationStatus.EN_ATTENTE) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime eventDate = reservation.getEvent().getDateDebut();
            long hoursUntil = java.time.Duration.between(now, eventDate).toHours();

            if (hoursUntil > 48) {
                Button cancelBtn = new Button("Annuler", new Icon(VaadinIcon.CLOSE));
                cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
                cancelBtn.addClickListener(e -> cancelReservation(reservation));
                actions.add(detailsBtn, cancelBtn);
            } else {
                // Annulation impossible (moins de 48h)
                Span tooLate = new Span("Annulation impossible");
                tooLate.getStyle()
                        .set("color", "#dc3545")
                        .set("font-size", "0.8rem")
                        .set("font-style", "italic");
                actions.add(detailsBtn, tooLate);
            }
        } else {
            actions.add(detailsBtn);
        }

        return actions;
    }

    /* -------------------- GESTION DES DONNÉES -------------------- */

    private void loadReservations() {
        List<Reservation> reservations = reservationService.findByClient(currentUser.getId());
        dataProvider = new ListDataProvider<>(reservations);
        grid.setDataProvider(dataProvider);

        updateStatsInfo(reservations);
    }

    private void applyFilters() {
        ReservationStatus selectedStatus = statusFilter.getValue();
        String codeSearch = codeSearchField.getValue();

        dataProvider.setFilter(reservation -> {
            // Filtre par statut
            if (selectedStatus != null && reservation.getStatus() != selectedStatus) {
                return false;
            }

            // Filtre par code de réservation
            if (codeSearch != null && !codeSearch.trim().isEmpty()) {
                if (reservation.getCodeReservation() == null ||
                    !reservation.getCodeReservation().toUpperCase().contains(codeSearch.toUpperCase().trim())) {
                    return false;
                }
            }

            return true;
        });

        List<Reservation> filteredItems = dataProvider.getItems().stream().collect(Collectors.toList());
        updateStatsInfo(filteredItems);
    }

    private void resetFilters() {
        statusFilter.clear();
        codeSearchField.clear();
        dataProvider.clearFilters();

        List<Reservation> allItems = dataProvider.getItems().stream().collect(Collectors.toList());
        updateStatsInfo(allItems);
    }

    private void updateStatsInfo(List<Reservation> reservations) {
        long total = reservations.size();
        long pending = reservations.stream().filter(r -> r.getStatus() == ReservationStatus.EN_ATTENTE).count();
        long confirmed = reservations.stream().filter(r -> r.getStatus() == ReservationStatus.CONFIRMEE).count();
        long cancelled = reservations.stream().filter(r -> r.getStatus() == ReservationStatus.ANNULEE).count();
        double totalAmount = reservations.stream()
                .filter(r -> r.getStatus() != ReservationStatus.ANNULEE)
                .mapToDouble(Reservation::getMontantTotal)
                .sum();

        // Trouver le Span statsInfo et mettre à jour son contenu
        VerticalLayout gridSection = (VerticalLayout) getComponentAt(getComponentCount() - 1);
        if (gridSection.getComponentCount() >= 3) {
            Span statsInfo = (Span) gridSection.getComponentAt(2);
            if (total > 0) {
                statsInfo.setText(String.format(
                        "Affichage de %d réservation(s) • En attente: %d • Confirmées: %d • Annulées: %d • Montant total: %.2f €",
                        total, pending, confirmed, cancelled, totalAmount
                ));
            } else {
                statsInfo.setText("Aucune réservation trouvée.");
            }
        }
    }

    private void cancelReservation(Reservation reservation) {
        try {
            reservationService.cancelReservation(reservation.getId(), currentUser.getId());
            Notification.show("Réservation " + reservation.getCodeReservation() + " annulée avec succès.", 3000, Notification.Position.TOP_CENTER);
            loadReservations(); // Refresh the data
        } catch (IllegalArgumentException ex) {
            Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }
}
