package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Event;
import com.inas.vaadinapp.entity.User;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Dashboard - EventManager")
@Route("dashboard")
public class DashboardView extends VerticalLayout {

    private final UserService userService;
    private final ReservationService reservationService;

    public DashboardView(UserService userService, ReservationService reservationService) {
        this.userService = userService;
        this.reservationService = reservationService;

        // Vérifier si l'utilisateur est connecté
        User currentUser = VaadinSession.getCurrent().getAttribute(User.class);
        if (currentUser == null) {
            UI.getCurrent().navigate("login");
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
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("color", "white")
                .set("border-radius", "0 0 16px 16px");

        H1 welcomeTitle = new H1("Bonjour, " + user.getPrenom() + " 👋");
        welcomeTitle.getStyle()
                .set("text-align", "center")
                .set("margin", "2rem 0 0.5rem 0")
                .set("font-size", "2.5rem");

        Paragraph welcomeSubtitle = new Paragraph("Bienvenue sur votre tableau de bord EventManager");
        welcomeSubtitle.getStyle()
                .set("text-align", "center")
                .set("margin", "0")
                .set("opacity", "0.9");

        header.add(welcomeTitle, welcomeSubtitle);

        // Section statistiques
        VerticalLayout statsSection = new VerticalLayout();
        statsSection.setPadding(true);
        statsSection.setSpacing(true);
        statsSection.setWidthFull();
        statsSection.getStyle().set("margin-top", "-2rem").set("position", "relative").set("z-index", "1");

        H2 statsTitle = new H2("📊 Vos statistiques");
        statsTitle.getStyle().set("text-align", "center").set("color", "#333").set("margin-bottom", "1rem");

        // Récupération des statistiques
        UserService.UserStatistics stats = userService.getUserStatistics(user.getId());

        HorizontalLayout statsCards = new HorizontalLayout();
        statsCards.setWidthFull();
        statsCards.setSpacing(true);

        // Carte réservations
        Div reservationsCard = createStatsCard(
                "🎫 Réservations",
                String.valueOf(stats.getReservationsCount()),
                "Total de vos réservations"
        );

        // Carte événements organisés (si organisateur)
        Div eventsCard = createStatsCard(
                "📅 Événements organisés",
                String.valueOf(stats.getEventsCreated()),
                "Événements que vous avez créés"
        );

        // Carte montant dépensé
        Div spentCard = createStatsCard(
                "💰 Dépensé",
                String.format("%.2f €", stats.getTotalSpent()),
                "Montant total de vos réservations"
        );

        statsCards.add(reservationsCard, eventsCard, spentCard);
        statsSection.add(statsTitle, statsCards);

        // Section événements à venir
        VerticalLayout upcomingSection = new VerticalLayout();
        upcomingSection.setPadding(true);
        upcomingSection.setSpacing(true);
        upcomingSection.setWidthFull();

        H2 upcomingTitle = new H2("📅 Événements à venir");
        upcomingTitle.getStyle().set("text-align", "center").set("color", "#333").set("margin-bottom", "1rem");

        List<Event> upcomingEvents = getUpcomingEvents(user);
        VerticalLayout upcomingEventsList = new VerticalLayout();
        upcomingEventsList.setSpacing(true);
        upcomingEventsList.setWidthFull();

        if (upcomingEvents.isEmpty()) {
            Paragraph noEvents = new Paragraph("Aucun événement à venir dans vos réservations.");
            noEvents.getStyle().set("text-align", "center").set("color", "#666").set("font-style", "italic");
            upcomingEventsList.add(noEvents);
        } else {
            for (Event event : upcomingEvents) {
                upcomingEventsList.add(createEventCard(event));
            }
        }

        upcomingSection.add(upcomingTitle, upcomingEventsList);

        // Section raccourcis
        VerticalLayout shortcutsSection = new VerticalLayout();
        shortcutsSection.setPadding(true);
        shortcutsSection.setSpacing(true);
        shortcutsSection.setWidthFull();

        H2 shortcutsTitle = new H2("🚀 Actions rapides");
        shortcutsTitle.getStyle().set("text-align", "center").set("color", "#333").set("margin-bottom", "1rem");

        HorizontalLayout shortcutsGrid = new HorizontalLayout();
        shortcutsGrid.setWidthFull();
        shortcutsGrid.setSpacing(true);

        // Raccourci événements
        Div eventsShortcut = createShortcutCard(
                "Voir les événements",
                "Découvrez tous les événements disponibles",
                VaadinIcon.CALENDAR,
                "#28a745",
                () -> UI.getCurrent().navigate("events")
        );

        // Raccourci réservations
        Div reservationsShortcut = createShortcutCard(
                "Mes réservations",
                "Gérez vos réservations existantes",
                VaadinIcon.TICKET,
                "#007bff",
                () -> UI.getCurrent().navigate("my-reservations")
        );

        // Raccourci profil (✅ corrigé ici)
        Div profileShortcut = createShortcutCard(
                "Mon profil",
                "Modifiez vos informations personnelles",
                VaadinIcon.USER,
                "#6c757d",
                () -> UI.getCurrent().navigate("profile")
        );

        // Raccourci déconnexion
        Div logoutShortcut = createShortcutCard(
                "Se déconnecter",
                "Quitter votre session sécurisée",
                VaadinIcon.SIGN_OUT,
                "#dc3545",
                () -> {
                    VaadinSession.getCurrent().getSession().invalidate();
                    VaadinSession.getCurrent().close();
                    UI.getCurrent().navigate("login");
                }
        );

        shortcutsGrid.add(eventsShortcut, reservationsShortcut, profileShortcut, logoutShortcut);
        shortcutsSection.add(shortcutsTitle, shortcutsGrid);

        // Section notifications (simulées pour l'instant)
        VerticalLayout notificationsSection = new VerticalLayout();
        notificationsSection.setPadding(true);
        notificationsSection.setSpacing(true);
        notificationsSection.setWidthFull();

        H2 notificationsTitle = new H2("🔔 Notifications");
        notificationsTitle.getStyle().set("text-align", "center").set("color", "#333").set("margin-bottom", "1rem");

        VerticalLayout notificationsList = new VerticalLayout();
        notificationsList.setSpacing(false);
        notificationsList.setWidthFull();

        // Notification exemple - événements à venir
        if (!upcomingEvents.isEmpty()) {
            Div upcomingNotification = createNotificationCard(
                    "Événements à venir",
                    "Vous avez " + upcomingEvents.size() + " événement(s) réservé(s) dans les prochains jours.",
                    VaadinIcon.CALENDAR_CLOCK,
                    "#ffc107"
            );
            notificationsList.add(upcomingNotification);
        }

        // Notification exemple - solde
        if (stats.getTotalSpent() > 0) {
            Div balanceNotification = createNotificationCard(
                    "Historique d'achats",
                    "Vous avez dépensé " + String.format("%.2f €", stats.getTotalSpent()) + " en réservations.",
                    VaadinIcon.EURO,
                    "#28a745"
            );
            notificationsList.add(balanceNotification);
        }

        // Notification de bienvenue si nouveau
        if (stats.getReservationsCount() == 0) {
            Div welcomeNotification = createNotificationCard(
                    "Bienvenue !",
                    "Découvrez notre catalogue d'événements et faites votre première réservation.",
                    VaadinIcon.HEART,
                    "#e83e8c"
            );
            notificationsList.add(welcomeNotification);
        }

        if (notificationsList.getComponentCount() == 0) {
            Paragraph noNotifications = new Paragraph("Aucune notification pour le moment.");
            noNotifications.getStyle().set("text-align", "center").set("color", "#666").set("font-style", "italic");
            notificationsList.add(noNotifications);
        }

        notificationsSection.add(notificationsTitle, notificationsList);

        // Assembler tout
        add(header, statsSection, upcomingSection, shortcutsSection, notificationsSection);
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
                .set("min-width", "200px");

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
                .set("min-width", "200px");

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
        cardContent.setAlignItems(Alignment.CENTER);

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

        eventInfo.add(eventTitle, eventDate, eventLocation);
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

    private Div createNotificationCard(String title, String message, VaadinIcon icon, String color) {
        Div notification = new Div();
        notification.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("padding", "1rem")
                .set("margin-bottom", "0.5rem")
                .set("border-left", "4px solid " + color);

        HorizontalLayout notificationContent = new HorizontalLayout();
        notificationContent.setAlignItems(Alignment.START);
        notificationContent.setSpacing(true);

        Icon notificationIcon = new Icon(icon);
        notificationIcon.getStyle().set("color", color).set("margin-top", "0.25rem");

        VerticalLayout textContent = new VerticalLayout();
        textContent.setSpacing(false);
        textContent.setPadding(false);

        H5 notificationTitle = new H5(title);
        notificationTitle.getStyle()
                .set("margin", "0 0 0.25rem 0")
                .set("color", "#333");

        Span notificationMessage = new Span(message);
        notificationMessage.getStyle()
                .set("color", "#666")
                .set("font-size", "0.9rem");

        textContent.add(notificationTitle, notificationMessage);

        notificationContent.add(notificationIcon, textContent);
        notification.add(notificationContent);

        return notification;
    }

    private List<Event> getUpcomingEvents(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextWeek = now.plusDays(7);

        return reservationService.findByClient(user.getId()).stream()
                .map(reservation -> reservation.getEvent())
                .filter(event -> event.getDateDebut().isAfter(now) && event.getDateDebut().isBefore(nextWeek))
                .distinct()
                .sorted((e1, e2) -> e1.getDateDebut().compareTo(e2.getDateDebut()))
                .collect(Collectors.toList());
    }
}
