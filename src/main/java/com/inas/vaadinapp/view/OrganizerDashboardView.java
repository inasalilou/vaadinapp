package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Event;
import com.inas.vaadinapp.entity.EventStatus;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.EventService;
import com.inas.vaadinapp.service.ReservationService;
import com.inas.vaadinapp.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Dashboard Organisateur - EventManager")
@Route("organizer/dashboard")
public class OrganizerDashboardView extends VerticalLayout {

    private final EventService eventService;
    private final ReservationService reservationService;
    private final UserService userService;

    public OrganizerDashboardView(EventService eventService, ReservationService reservationService, UserService userService) {
        this.eventService = eventService;
        this.reservationService = reservationService;
        this.userService = userService;

        // Vérifier si l'utilisateur est connecté et est un organisateur
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
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");

        buildDashboard(currentUser);
    }

    private void buildDashboard(User user) {
        // Header avec bienvenue
        VerticalLayout header = new VerticalLayout();
        header.setPadding(true);
        header.setSpacing(false);
        header.setWidthFull();
        header.setHeight("200px");
        header.getStyle()
                .set("background", "linear-gradient(135deg, #764ba2 0%, #667eea 100%)")
                .set("color", "white")
                .set("border-radius", "0 0 16px 16px");

        H1 welcomeTitle = new H1("Bonjour, " + user.getPrenom() + " 👋");
        welcomeTitle.getStyle()
                .set("text-align", "center")
                .set("margin", "2rem 0 0.5rem 0")
                .set("font-size", "2.5rem");

        Paragraph welcomeSubtitle = new Paragraph("Bienvenue sur votre tableau de bord organisateur");
        welcomeSubtitle.getStyle()
                .set("text-align", "center")
                .set("margin", "0")
                .set("opacity", "0.9");

        header.add(welcomeTitle, welcomeSubtitle);

        // Section statistiques principales
        VerticalLayout statsSection = new VerticalLayout();
        statsSection.setPadding(true);
        statsSection.setSpacing(true);
        statsSection.setWidthFull();
        statsSection.getStyle().set("margin-top", "-2rem").set("position", "relative").set("z-index", "1");

        H2 statsTitle = new H2("📊 Vos statistiques");
        statsTitle.getStyle().set("text-align", "center").set("color", "#333").set("margin-bottom", "1rem");

        // Récupération des statistiques
        EventService.OrganizerStatistics eventStats = eventService.getOrganizerStatistics(user.getId());
        ReservationService.OrganizerReservationStatistics reservationStats = reservationService.getOrganizerReservationStatistics(user.getId());

        HorizontalLayout statsCards = new HorizontalLayout();
        statsCards.setWidthFull();
        statsCards.setSpacing(true);

        // Carte événements totaux
        Div totalEventsCard = createStatsCard(
                "🎪 Événements créés",
                String.valueOf(eventStats.getTotalEvents()),
                "Total de vos événements"
        );

        // Carte événements publiés
        Div publishedEventsCard = createStatsCard(
                "📅 Événements publiés",
                String.valueOf(eventStats.getPublishedEvents()),
                "Événements actifs"
        );

        // Carte réservations totales
        Div totalReservationsCard = createStatsCard(
                "🎫 Réservations totales",
                String.valueOf(reservationStats.getTotalReservations()),
                "Toutes les réservations reçues"
        );

        statsCards.add(totalEventsCard, publishedEventsCard, totalReservationsCard);

        // Deuxième ligne de statistiques
        HorizontalLayout revenueCards = new HorizontalLayout();
        revenueCards.setWidthFull();
        revenueCards.setSpacing(true);

        // Carte revenu total
        Div totalRevenueCard = createStatsCard(
                "💰 Revenus totaux",
                String.format("%.2f €", reservationStats.getTotalRevenue()),
                "Gains générés par vos événements"
        );

        // Carte revenu du mois
        Div monthRevenueCard = createStatsCard(
                "📈 Revenus ce mois",
                String.format("%.2f €", reservationStats.getCurrentMonthRevenue()),
                "Revenus du mois en cours"
        );

        // Carte places réservées
        Div placesReservedCard = createStatsCard(
                "👥 Places réservées",
                String.valueOf(reservationStats.getTotalPlacesReserved()),
                "Nombre total de places vendues"
        );

        revenueCards.add(totalRevenueCard, monthRevenueCard, placesReservedCard);

        statsSection.add(statsTitle, statsCards, revenueCards);

        // Section événements par statut
        VerticalLayout statusSection = new VerticalLayout();
        statusSection.setPadding(true);
        statusSection.setSpacing(true);
        statusSection.setWidthFull();

        H2 statusTitle = new H2("📋 Répartition par statut");
        statusTitle.getStyle().set("text-align", "center").set("color", "#333").set("margin-bottom", "1rem");

        HorizontalLayout statusCards = new HorizontalLayout();
        statusCards.setWidthFull();
        statusCards.setSpacing(true);

        // Carte brouillons
        Div draftCard = createStatusCard(
                "📝 Brouillons",
                String.valueOf(eventStats.getDraftEvents()),
                "Événements en préparation",
                "#6c757d"
        );

        // Carte publiés
        Div publishedCard = createStatusCard(
                "🟢 Publiés",
                String.valueOf(eventStats.getPublishedEvents()),
                "Événements actifs et visibles",
                "#28a745"
        );

        // Carte annulés
        Div cancelledCard = createStatusCard(
                "🔴 Annulés",
                String.valueOf(eventStats.getCancelledEvents()),
                "Événements annulés",
                "#dc3545"
        );

        // Carte terminés
        Div finishedCard = createStatusCard(
                "✅ Terminés",
                String.valueOf(eventStats.getFinishedEvents()),
                "Événements passés",
                "#17a2b8"
        );

        statusCards.add(draftCard, publishedCard, cancelledCard, finishedCard);
        statusSection.add(statusTitle, statusCards);

        // Section événements récents
        VerticalLayout recentEventsSection = new VerticalLayout();
        recentEventsSection.setPadding(true);
        recentEventsSection.setSpacing(true);
        recentEventsSection.setWidthFull();

        H2 recentTitle = new H2("🕒 Événements récents");
        recentTitle.getStyle().set("text-align", "center").set("color", "#333").set("margin-bottom", "1rem");

        List<Event> recentEvents = eventService.getRecentEventsByOrganizer(user.getId(), 5);
        VerticalLayout recentEventsList = new VerticalLayout();
        recentEventsList.setSpacing(true);
        recentEventsList.setWidthFull();

        if (recentEvents.isEmpty()) {
            Paragraph noEvents = new Paragraph("Aucun événement créé pour le moment.");
            noEvents.getStyle().set("text-align", "center").set("color", "#666").set("font-style", "italic");
            recentEventsList.add(noEvents);
        } else {
            for (Event event : recentEvents) {
                recentEventsList.add(createEventCard(event));
            }
        }

        recentEventsSection.add(recentTitle, recentEventsList);

        // Section raccourcis organisateur
        VerticalLayout shortcutsSection = new VerticalLayout();
        shortcutsSection.setPadding(true);
        shortcutsSection.setSpacing(true);
        shortcutsSection.setWidthFull();

        H2 shortcutsTitle = new H2("🚀 Actions organisateur");
        shortcutsTitle.getStyle().set("text-align", "center").set("color", "#333").set("margin-bottom", "1rem");

        HorizontalLayout shortcutsGrid = new HorizontalLayout();
        shortcutsGrid.setWidthFull();
        shortcutsGrid.setSpacing(true);

        // Créer un événement
        Div createEventShortcut = createShortcutCard(
                "Créer un événement",
                "Ajouter un nouvel événement",
                VaadinIcon.PLUS,
                "#28a745",
                () -> UI.getCurrent().navigate("organizer/event/new")
        );

        // Gérer mes événements
        Div manageEventsShortcut = createShortcutCard(
                "Gérer mes événements",
                "Modifier et gérer vos événements",
                VaadinIcon.EDIT,
                "#007bff",
                () -> UI.getCurrent().navigate("organizer/events")
        );

        // Voir les réservations
        Div reservationsShortcut = createShortcutCard(
                "Voir les réservations",
                "Consulter les réservations reçues",
                VaadinIcon.TICKET,
                "#ffc107",
                () -> UI.getCurrent().navigate("organizer-reservations")
        );

        // Profil organisateur
        Div profileShortcut = createShortcutCard(
                "Mon profil",
                "Gérer votre profil organisateur",
                VaadinIcon.USER,
                "#6c757d",
                () -> UI.getCurrent().navigate("profile")
        );

        shortcutsGrid.add(createEventShortcut, manageEventsShortcut, reservationsShortcut, profileShortcut);
        shortcutsSection.add(shortcutsTitle, shortcutsGrid);

        // Assembler tout
        add(header, statsSection, statusSection, recentEventsSection, shortcutsSection);
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

    private Div createStatusCard(String title, String value, String subtitle, String color) {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
                .set("padding", "1.5rem")
                .set("text-align", "center")
                .set("flex", "1")
                .set("min-width", "200px")
                .set("margin", "0.5rem")
                .set("border-left", "4px solid " + color);

        H3 cardTitle = new H3(title);
        cardTitle.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("color", color)
                .set("font-size", "1.1rem");

        H2 cardValue = new H2(value);
        cardValue.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("color", "#333")
                .set("font-size", "2rem");

        Span cardSubtitle = new Span(subtitle);
        cardSubtitle.getStyle()
                .set("color", "#666")
                .set("font-size", "0.85rem");

        card.add(cardTitle, cardValue, cardSubtitle);
        return card;
    }

    private Div createShortcutCard(String title, String description, VaadinIcon icon, String color, Runnable action) {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
                .set("padding", "1.5rem")
                .set("text-align", "center")
                .set("cursor", "pointer")
                .set("transition", "transform 0.2s, box-shadow 0.2s")
                .set("flex", "1")
                .set("min-width", "200px")
                .set("margin", "0.5rem");

        card.addClickListener(e -> action.run());

        // Hover effect
        card.getElement().addEventListener("mouseenter", e ->
                card.getStyle().set("transform", "translateY(-2px)").set("box-shadow", "0 8px 25px rgba(0,0,0,0.15)")
        );
        card.getElement().addEventListener("mouseleave", e ->
                card.getStyle().set("transform", "translateY(0)").set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
        );

        Icon cardIcon = new Icon(icon);
        cardIcon.setSize("2rem");
        cardIcon.getStyle().set("color", color).set("margin-bottom", "1rem");

        H4 cardTitle = new H4(title);
        cardTitle.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("color", "#333");

        Span cardDescription = new Span(description);
        cardDescription.getStyle()
                .set("color", "#666")
                .set("font-size", "0.9rem");

        card.add(cardIcon, cardTitle, cardDescription);
        return card;
    }

    private Div createEventCard(Event event) {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("padding", "1rem")
                .set("cursor", "pointer")
                .set("transition", "transform 0.2s")
                .set("margin-bottom", "0.5rem");

        card.addClickListener(e -> UI.getCurrent().navigate("event/" + event.getId()));

        // Hover effect
        card.getElement().addEventListener("mouseenter", e ->
                card.getStyle().set("transform", "translateY(-2px)")
        );
        card.getElement().addEventListener("mouseleave", e ->
                card.getStyle().set("transform", "translateY(0)")
        );

        HorizontalLayout cardContent = new HorizontalLayout();
        cardContent.setWidthFull();
        cardContent.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        // Informations événement
        VerticalLayout eventInfo = new VerticalLayout();
        eventInfo.setSpacing(false);
        eventInfo.setPadding(false);
        eventInfo.setWidthFull();

        H4 eventTitle = new H4(event.getTitre());
        eventTitle.getStyle()
                .set("margin", "0 0 0.25rem 0")
                .set("color", "#333");

        Span eventDate = new Span("📅 " + event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        eventDate.getStyle().set("color", "#666").set("font-size", "0.9rem");

        Span eventLocation = new Span("📍 " + event.getVille());
        eventLocation.getStyle().set("color", "#666").set("font-size", "0.9rem");

        // Statut de l'événement
        Span eventStatus = new Span("Statut: " + getStatusLabel(event.getStatus()));
        eventStatus.getStyle()
                .set("color", getStatusColor(event.getStatus()))
                .set("font-size", "0.8rem")
                .set("font-weight", "bold");

        eventInfo.add(eventTitle, eventDate, eventLocation, eventStatus);
        eventInfo.setFlexGrow(1, eventTitle);

        // Prix
        Span price = new Span(String.format("%.2f €", event.getPrixUnitaire()));
        price.getStyle()
                .set("color", "#28a745")
                .set("font-weight", "bold")
                .set("font-size", "1.1rem");

        cardContent.add(eventInfo, price);
        card.add(cardContent);

        return card;
    }

    private String getStatusLabel(EventStatus status) {
        switch (status) {
            case BROUILLON: return "Brouillon";
            case PUBLIE: return "Publié";
            case ANNULE: return "Annulé";
            case TERMINE: return "Terminé";
            default: return status.toString();
        }
    }

    private String getStatusColor(EventStatus status) {
        switch (status) {
            case BROUILLON: return "#6c757d";
            case PUBLIE: return "#28a745";
            case ANNULE: return "#dc3545";
            case TERMINE: return "#17a2b8";
            default: return "#666";
        }
    }
}
