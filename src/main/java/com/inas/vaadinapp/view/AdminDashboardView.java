package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.*;
import com.inas.vaadinapp.service.EventService;
import com.inas.vaadinapp.service.ReservationService;
import com.inas.vaadinapp.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PageTitle("Admin Dashboard - EventManager")
@Route("admin/dashboard")
public class AdminDashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;
    private final EventService eventService;
    private final ReservationService reservationService;

    // Statistiques principales
    private Div totalUsersCard;
    private Div totalEventsCard;
    private Div totalReservationsCard;
    private Div totalRevenueCard;

    public AdminDashboardView(UserService userService, EventService eventService, ReservationService reservationService) {
        this.userService = userService;
        this.eventService = eventService;
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f8f9fa");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Vérifier que l'utilisateur est un admin
        User currentUser = VaadinSession.getCurrent().getAttribute(User.class);
        if (currentUser == null) {
            event.rerouteTo("login");
            return;
        }

        if (currentUser.getRole() != Role.ADMIN) {
            event.rerouteTo("dashboard");
            return;
        }

        // Construire le dashboard seulement si l'authentification est OK
        if (getComponentCount() == 0) {
            buildDashboard();
        }
    }

    private void buildDashboard() {
        // Header
        createHeader();

        // Statistiques principales
        createMainStatistics();

        // Graphiques et analyses détaillées
        createChartsSection();

        // Statistiques détaillées
        createDetailedStatistics();

        // Actions administratives
        createAdminActions();
    }

    private void createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);
        header.setAlignItems(Alignment.CENTER);

        H1 title = new H1(" Administration - EventManager");
        title.getStyle()
                .set("color", "#333")
                .set("margin-bottom", "0.5rem")
                .set("text-align", "center");

        Span subtitle = new Span("Vue d'ensemble globale de la plateforme");
        subtitle.getStyle()
                .set("color", "#666")
                .set("font-size", "1.1rem")
                .set("text-align", "center");

        Span lastUpdate = new Span("Dernière mise à jour: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        lastUpdate.getStyle()
                .set("color", "#999")
                .set("font-size", "0.9rem")
                .set("text-align", "center");

        header.add(title, subtitle, lastUpdate);
        add(header);
    }

    private void createMainStatistics() {
        VerticalLayout statsSection = new VerticalLayout();
        statsSection.setPadding(true);
        statsSection.setSpacing(true);
        statsSection.setWidthFull();

        H2 statsTitle = new H2(" Statistiques principales");
        statsTitle.getStyle().set("text-align", "center").set("margin-bottom", "1rem");

        HorizontalLayout statsGrid = new HorizontalLayout();
        statsGrid.setWidthFull();
        statsGrid.setSpacing(true);

        // Calculer les statistiques
        List<User> allUsers = userService.findUsersWithFilters(null, null, null);
        ReservationService.OrganizerReservationStatistics reservationStats = getGlobalReservationStats();

        // Carte utilisateurs
        totalUsersCard = createStatsCard(
                " Utilisateurs",
                String.valueOf(allUsers.size()),
                "Total des utilisateurs inscrits"
        );

        // Carte événements
        totalEventsCard = createStatsCard(
                " Événements",
                String.valueOf(eventService.findAll().size()),
                "Total des événements créés"
        );

        // Carte réservations
        totalReservationsCard = createStatsCard(
                " Réservations",
                String.valueOf(reservationStats.getTotalReservations()),
                "Total des réservations effectuées"
        );

        // Carte revenus
        totalRevenueCard = createStatsCard(
                " Revenus",
                String.format("%.2f dh", reservationStats.getTotalRevenue()),
                "Revenus totaux générés"
        );

        statsGrid.add(totalUsersCard, totalEventsCard, totalReservationsCard, totalRevenueCard);
        statsSection.add(statsTitle, statsGrid);
        add(statsSection);
    }

    private void createChartsSection() {
        VerticalLayout chartsSection = new VerticalLayout();
        chartsSection.setPadding(true);
        chartsSection.setSpacing(true);
        chartsSection.setWidthFull();

        H2 chartsTitle = new H2(" Analyses détaillées");
        chartsTitle.getStyle().set("text-align", "center").set("margin-bottom", "1rem");

        HorizontalLayout chartsGrid = new HorizontalLayout();
        chartsGrid.setWidthFull();
        chartsGrid.setSpacing(true);

        // Tableaux de statistiques détaillées au lieu de graphiques
        VerticalLayout usersStats = createStatsTable(" Utilisateurs par rôle", getUsersByRoleStats());
        VerticalLayout eventsStats = createStatsTable(" Événements par statut", getEventsByStatusStats());
        VerticalLayout reservationsStats = createStatsTable(" Réservations par statut", getReservationsByStatusStats());
        VerticalLayout revenueStats = createStatsTable(" Métriques financières", getRevenueStats());

        chartsGrid.add(usersStats, eventsStats, reservationsStats, revenueStats);
        chartsSection.add(chartsTitle, chartsGrid);
        add(chartsSection);
    }

    private void createDetailedStatistics() {
        VerticalLayout detailedSection = new VerticalLayout();
        detailedSection.setPadding(true);
        detailedSection.setSpacing(true);
        detailedSection.setWidthFull();

        H2 detailedTitle = new H2(" Statistiques détaillées");
        detailedTitle.getStyle().set("text-align", "center").set("margin-bottom", "1rem");

        HorizontalLayout detailedGrid = new HorizontalLayout();
        detailedGrid.setWidthFull();
        detailedGrid.setSpacing(true);

        // Statistiques utilisateurs détaillées
        VerticalLayout usersStats = createDetailedStatsCard("Utilisateurs par rôle", getUsersStatsDetails());
        VerticalLayout eventsStats = createDetailedStatsCard("Événements par statut", getEventsStatsDetails());
        VerticalLayout reservationsStats = createDetailedStatsCard("Réservations par statut", getReservationsStatsDetails());
        VerticalLayout platformStats = createDetailedStatsCard("Métriques plateforme", getPlatformStatsDetails());

        detailedGrid.add(usersStats, eventsStats, reservationsStats, platformStats);
        detailedSection.add(detailedTitle, detailedGrid);
        add(detailedSection);
    }

    private void createAdminActions() {
        VerticalLayout actionsSection = new VerticalLayout();
        actionsSection.setPadding(true);
        actionsSection.setSpacing(true);
        actionsSection.setWidthFull();

        H2 actionsTitle = new H2(" Actions administratives");
        actionsTitle.getStyle().set("text-align", "center").set("margin-bottom", "1rem");

        HorizontalLayout actionsGrid = new HorizontalLayout();
        actionsGrid.setWidthFull();
        actionsGrid.setSpacing(true);

        Button manageUsersBtn = new Button("Gérer les utilisateurs", new Icon(VaadinIcon.USERS));
        manageUsersBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        manageUsersBtn.addClickListener(e -> UI.getCurrent().navigate("admin/users"));

        Button manageEventsBtn = new Button("Gérer les événements", new Icon(VaadinIcon.CALENDAR));
        manageEventsBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        manageEventsBtn.addClickListener(e -> UI.getCurrent().navigate("admin/events"));

        Button manageReservationsBtn = new Button("Gérer les réservations", new Icon(VaadinIcon.TICKET));
        manageReservationsBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        manageReservationsBtn.addClickListener(e -> UI.getCurrent().navigate("admin/reservations"));

        Button systemSettingsBtn = new Button("Paramètres système", new Icon(VaadinIcon.COGS));
        systemSettingsBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        systemSettingsBtn.addClickListener(e -> UI.getCurrent().navigate("admin/settings"));

        Button exportDataBtn = new Button("Exporter les données", new Icon(VaadinIcon.DOWNLOAD));
        exportDataBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        exportDataBtn.addClickListener(e -> exportPlatformData());

        actionsGrid.add(manageUsersBtn, manageEventsBtn, manageReservationsBtn, systemSettingsBtn, exportDataBtn);
        actionsSection.add(actionsTitle, actionsGrid);
        add(actionsSection);
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


    private VerticalLayout createDetailedStatsCard(String title, List<String> stats) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.setWidth("300px");
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("margin", "0.5rem");

        H4 cardTitle = new H4(title);
        cardTitle.getStyle()
                .set("text-align", "center")
                .set("margin-bottom", "1rem")
                .set("color", "#333");

        for (String stat : stats) {
            Paragraph statLine = new Paragraph(stat);
            statLine.getStyle().set("margin", "0.25rem 0");
            card.add(statLine);
        }

        return card;
    }

    private List<String> getUsersStatsDetails() {
        List<User> allUsers = userService.findUsersWithFilters(null, null, null);
        Map<Role, Long> usersByRole = allUsers.stream()
                .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));

        return List.of(
            " Administrateurs: " + usersByRole.getOrDefault(Role.ADMIN, 0L),
            " Organisateurs: " + usersByRole.getOrDefault(Role.ORGANIZER, 0L),
            " Clients: " + usersByRole.getOrDefault(Role.CLIENT, 0L),
            " Taux organisateurs: " + String.format("%.1f%%",
                (double) usersByRole.getOrDefault(Role.ORGANIZER, 0L) / allUsers.size() * 100)
        );
    }

    private List<String> getEventsStatsDetails() {
        Map<EventStatus, Long> eventsByStatus = eventService.findAll().stream()
                .collect(Collectors.groupingBy(Event::getStatus, Collectors.counting()));

        long totalEvents = eventService.findAll().size();
        return List.of(
            " Brouillons: " + eventsByStatus.getOrDefault(EventStatus.BROUILLON, 0L),
            " Publiés: " + eventsByStatus.getOrDefault(EventStatus.PUBLIE, 0L),
            " Annulés: " + eventsByStatus.getOrDefault(EventStatus.ANNULE, 0L),
            " Terminés: " + eventsByStatus.getOrDefault(EventStatus.TERMINE, 0L),
            " Taux de succès: " + String.format("%.1f%%",
                (double) eventsByStatus.getOrDefault(EventStatus.PUBLIE, 0L) / totalEvents * 100)
        );
    }

    private List<String> getReservationsStatsDetails() {
        ReservationService.OrganizerReservationStatistics stats = getGlobalReservationStats();

        return List.of(
            " En attente: " + getReservationCountByStatus(ReservationStatus.EN_ATTENTE),
            " Confirmées: " + getReservationCountByStatus(ReservationStatus.CONFIRMEE),
            " Annulées: " + getReservationCountByStatus(ReservationStatus.ANNULEE),
            " Places réservées: " + stats.getTotalPlacesReserved(),
            " CA généré: " + String.format("%.2f dh", stats.getTotalRevenue())
        );
    }

    private List<String> getPlatformStatsDetails() {
        List<User> allUsers = userService.findUsersWithFilters(null, null, null);
        long totalUsers = allUsers.size();
        long totalEvents = eventService.findAll().size();
        ReservationService.OrganizerReservationStatistics reservationStats = getGlobalReservationStats();

        return List.of(
            " Utilisateurs actifs: " + totalUsers,
            " Événements totaux: " + totalEvents,
            " Réservations totales: " + reservationStats.getTotalReservations(),
            " Revenus totaux: " + String.format("%.2f dh", reservationStats.getTotalRevenue()),
            " Événements/utilisateur: " + String.format("%.2f", (double) totalEvents / totalUsers),
            " Revenus/événement: " + String.format("%.2f dh",
                totalEvents > 0 ? reservationStats.getTotalRevenue() / totalEvents : 0)
        );
    }

    private ReservationService.OrganizerReservationStatistics getGlobalReservationStats() {
        try {
            // Essayer d'utiliser la méthode globale si elle existe
            var method = ReservationService.class.getMethod("getReservationStatistics");
            ReservationService.ReservationStatistics globalStats = (ReservationService.ReservationStatistics) method.invoke(reservationService);

            return new ReservationService.OrganizerReservationStatistics(
                (int) globalStats.getTotalReservations(),
                (int) (globalStats.getConfirmedReservations() + globalStats.getPendingReservations()),
                globalStats.getTotalRevenue(),
                0.0, // Current month revenue - à calculer séparément
                globalStats.getTotalPlacesReserved()
            );
        } catch (Exception e) {
            // Fallback: calculer manuellement
            return new ReservationService.OrganizerReservationStatistics(0, 0, 0.0, 0.0, 0);
        }
    }

    private long getReservationCountByStatus(ReservationStatus status) {
        try {
            var method = ReservationService.class.getMethod("findByStatus", ReservationStatus.class);
            @SuppressWarnings("unchecked")
            List<Reservation> reservations = (List<Reservation>) method.invoke(reservationService, status);
            return reservations.size();
        } catch (Exception e) {
            return 0;
        }
    }


    private void exportPlatformData() {
        // Simulation d'export - en production, générer un vrai rapport
        Notification.show("Fonctionnalité d'export en développement", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
    }


    private VerticalLayout createStatsTable(String title, List<String> stats) {
        VerticalLayout table = new VerticalLayout();
        table.setPadding(true);
        table.setSpacing(false);
        table.setWidth("350px");
        table.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("margin", "0.5rem");

        H4 tableTitle = new H4(title);
        tableTitle.getStyle()
                .set("text-align", "center")
                .set("margin-bottom", "1rem")
                .set("color", "#333");

        table.add(tableTitle);

        for (String stat : stats) {
            Div statRow = new Div();
            statRow.getStyle()
                    .set("padding", "0.5rem")
                    .set("border-bottom", "1px solid #eee")
                    .set("display", "flex")
                    .set("justify-content", "space-between");

            statRow.setText(stat);
            table.add(statRow);
        }

        return table;
    }

    private List<String> getUsersByRoleStats() {
        List<User> allUsers = userService.findUsersWithFilters(null, null, null);
        Map<Role, Long> usersByRole = allUsers.stream()
                .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));
        long totalUsers = allUsers.size();

        return List.of(
            " Administrateurs: " + usersByRole.getOrDefault(Role.ADMIN, 0L) +
                " (" + String.format("%.1f%%", (double) usersByRole.getOrDefault(Role.ADMIN, 0L) / totalUsers * 100) + ")",
            " Organisateurs: " + usersByRole.getOrDefault(Role.ORGANIZER, 0L) +
                " (" + String.format("%.1f%%", (double) usersByRole.getOrDefault(Role.ORGANIZER, 0L) / totalUsers * 100) + ")",
            " Clients: " + usersByRole.getOrDefault(Role.CLIENT, 0L) +
                " (" + String.format("%.1f%%", (double) usersByRole.getOrDefault(Role.CLIENT, 0L) / totalUsers * 100) + ")"
        );
    }

    private List<String> getEventsByStatusStats() {
        Map<EventStatus, Long> eventsByStatus = eventService.findAll().stream()
                .collect(Collectors.groupingBy(Event::getStatus, Collectors.counting()));
        long totalEvents = eventService.findAll().size();

        return List.of(
            " Brouillons: " + eventsByStatus.getOrDefault(EventStatus.BROUILLON, 0L) +
                " (" + String.format("%.1f%%", (double) eventsByStatus.getOrDefault(EventStatus.BROUILLON, 0L) / totalEvents * 100) + ")",
            " Publiés: " + eventsByStatus.getOrDefault(EventStatus.PUBLIE, 0L) +
                " (" + String.format("%.1f%%", (double) eventsByStatus.getOrDefault(EventStatus.PUBLIE, 0L) / totalEvents * 100) + ")",
            " Annulés: " + eventsByStatus.getOrDefault(EventStatus.ANNULE, 0L) +
                " (" + String.format("%.1f%%", (double) eventsByStatus.getOrDefault(EventStatus.ANNULE, 0L) / totalEvents * 100) + ")",
            " Terminés: " + eventsByStatus.getOrDefault(EventStatus.TERMINE, 0L) +
                " (" + String.format("%.1f%%", (double) eventsByStatus.getOrDefault(EventStatus.TERMINE, 0L) / totalEvents * 100) + ")"
        );
    }

    private List<String> getReservationsByStatusStats() {
        return List.of(
            " En attente: " + getReservationCountByStatus(ReservationStatus.EN_ATTENTE),
            " Confirmées: " + getReservationCountByStatus(ReservationStatus.CONFIRMEE),
            " Annulées: " + getReservationCountByStatus(ReservationStatus.ANNULEE),
            " Taux de confirmation: " + getConfirmationRate() + "%"
        );
    }

    private List<String> getRevenueStats() {
        ReservationService.OrganizerReservationStatistics stats = getGlobalReservationStats();

        return List.of(
            " Revenus totaux: " + String.format("%.2f dh", stats.getTotalRevenue()),
            " Revenus ce mois: " + String.format("%.2f dh", stats.getCurrentMonthRevenue()),
            " Places réservées: " + stats.getTotalPlacesReserved(),
            " Panier moyen: " + String.format("%.2f dh",
                stats.getTotalReservations() > 0 ? stats.getTotalRevenue() / stats.getTotalReservations() : 0)
        );
    }

    private String getConfirmationRate() {
        long total = getReservationCountByStatus(ReservationStatus.EN_ATTENTE) +
                    getReservationCountByStatus(ReservationStatus.CONFIRMEE) +
                    getReservationCountByStatus(ReservationStatus.ANNULEE);

        if (total == 0) return "0.0";

        long confirmed = getReservationCountByStatus(ReservationStatus.CONFIRMEE);
        return String.format("%.1f", (double) confirmed / total * 100);
    }
}
