package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Event;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.EventService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Route("event/:eventId")
@PageTitle("Détail événement - EventManager")
public class EventDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;

    private Long eventId;
    private Event currentEvent;

    public EventDetailView(EventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");

        // Le contenu sera chargé dynamiquement dans beforeEnter
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> idOpt = event.getRouteParameters().get("eventId");
        if (idOpt.isEmpty()) {
            Notification.show("Aucun événement indiqué.", 3000, Notification.Position.TOP_CENTER);
            UI.getCurrent().navigate("events");
            return;
        }

        try {
            this.eventId = Long.parseLong(idOpt.get());
        } catch (NumberFormatException e) {
            Notification.show("Identifiant d'événement invalide.", 3000, Notification.Position.TOP_CENTER);
            UI.getCurrent().navigate("events");
            return;
        }

        this.currentEvent = eventService.findById(eventId).orElse(null);
        if (currentEvent == null) {
            Notification.show("Événement introuvable.", 3000, Notification.Position.TOP_CENTER);
            UI.getCurrent().navigate("events");
            return;
        }

        buildUI();
    }

    private void buildUI() {
        removeAll();

        // Header avec navigation
        VerticalLayout header = new VerticalLayout();
        header.setPadding(true);
        header.setSpacing(false);
        header.setWidthFull();
        header.getStyle().set("background", "white").set("border-radius", "0 0 8px 8px");

        HorizontalLayout navBar = new HorizontalLayout();
        navBar.setWidthFull();
        navBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        navBar.setAlignItems(Alignment.CENTER);

        Button backBtn = new Button("← Retour aux événements", new Icon(VaadinIcon.ARROW_LEFT));
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.addClickListener(e -> UI.getCurrent().navigate("events"));

        H2 pageTitle = new H2("Détails de l'événement");
        pageTitle.getStyle().set("margin", "0").set("color", "#333");

        navBar.add(backBtn, pageTitle);
        header.add(navBar);

        // Section principale avec image et informations
        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.setWidthFull();
        mainContent.setSpacing(true);
        mainContent.setPadding(true);

        // Image de l'événement
        VerticalLayout imageSection = new VerticalLayout();
        imageSection.setWidth("400px");
        imageSection.setSpacing(false);

        Div imageContainer = new Div();
        imageContainer.getStyle()
                .set("width", "100%")
                .set("height", "250px")
                .set("border-radius", "8px")
                .set("overflow", "hidden")
                .set("background", "linear-gradient(45deg, #667eea, #764ba2)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("box-shadow", "0 4px 8px rgba(0,0,0,0.1)");

        if (currentEvent.getImageUrl() != null && !currentEvent.getImageUrl().trim().isEmpty()) {
            Image eventImage = new Image(currentEvent.getImageUrl(), "Image de l'événement");
            eventImage.getStyle()
                    .set("width", "100%")
                    .set("height", "100%")
                    .set("object-fit", "cover");
            imageContainer.add(eventImage);
        } else {
            // Image placeholder avec icône
            Icon imageIcon = new Icon(VaadinIcon.CALENDAR);
            imageIcon.setSize("4rem");
            imageIcon.getStyle().set("color", "white").set("opacity", "0.8");
            imageContainer.add(imageIcon);
        }

        imageSection.add(imageContainer);

        // Informations détaillées
        VerticalLayout infoSection = new VerticalLayout();
        infoSection.setWidthFull();
        infoSection.setSpacing(true);
        infoSection.setPadding(false);

        // Titre et catégorie
        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setSpacing(false);
        titleSection.setPadding(false);

        H1 eventTitle = new H1(currentEvent.getTitre());
        eventTitle.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("color", "#333")
                .set("font-size", "2.5rem");

        Span categoryBadge = new Span(currentEvent.getCategorie().toString());
        categoryBadge.getStyle()
                .set("background-color", "#667eea")
                .set("color", "white")
                .set("padding", "4px 12px")
                .set("border-radius", "16px")
                .set("font-size", "0.875rem")
                .set("font-weight", "bold");

        titleSection.add(eventTitle, categoryBadge);

        // Description
        VerticalLayout descriptionSection = new VerticalLayout();
        descriptionSection.setSpacing(false);
        descriptionSection.setPadding(false);

        H3 descTitle = new H3("Description");
        descTitle.getStyle().set("margin", "1rem 0 0.5rem 0").set("color", "#555");

        Paragraph description = new Paragraph(
                currentEvent.getDescription() != null && !currentEvent.getDescription().trim().isEmpty()
                        ? currentEvent.getDescription()
                        : "Aucune description disponible pour cet événement."
        );
        description.getStyle()
                .set("line-height", "1.6")
                .set("color", "#666")
                .set("margin", "0");

        descriptionSection.add(descTitle, description);

        // Informations pratiques
        VerticalLayout detailsSection = new VerticalLayout();
        detailsSection.setSpacing(true);
        detailsSection.setPadding(true);
        detailsSection.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("margin-top", "1rem");

        H3 detailsTitle = new H3("Informations pratiques");
        detailsTitle.getStyle().set("margin", "0 0 1rem 0").set("color", "#333");

        // Date et heure
        HorizontalLayout dateLayout = new HorizontalLayout();
        dateLayout.setSpacing(true);
        dateLayout.setAlignItems(Alignment.CENTER);

        Icon dateIcon = new Icon(VaadinIcon.CALENDAR);
        dateIcon.setColor("#667eea");

        VerticalLayout dateInfo = new VerticalLayout();
        dateInfo.setSpacing(false);
        dateInfo.setPadding(false);

        Span dateLabel = new Span("Date et heure");
        dateLabel.getStyle().set("font-weight", "bold").set("color", "#333");

        Span dateValue = new Span(
                "Du " + currentEvent.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                " au " + currentEvent.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
        dateValue.getStyle().set("color", "#666");

        dateInfo.add(dateLabel, dateValue);
        dateLayout.add(dateIcon, dateInfo);

        // Lieu
        HorizontalLayout locationLayout = new HorizontalLayout();
        locationLayout.setSpacing(true);
        locationLayout.setAlignItems(Alignment.CENTER);

        Icon locationIcon = new Icon(VaadinIcon.MAP_MARKER);
        locationIcon.setColor("#28a745");

        VerticalLayout locationInfo = new VerticalLayout();
        locationInfo.setSpacing(false);
        locationInfo.setPadding(false);

        Span locationLabel = new Span("Lieu");
        locationLabel.getStyle().set("font-weight", "bold").set("color", "#333");

        Span locationValue = new Span(currentEvent.getLieu() + ", " + currentEvent.getVille());
        locationValue.getStyle().set("color", "#666");

        locationInfo.add(locationLabel, locationValue);
        locationLayout.add(locationIcon, locationInfo);

        // Prix et places
        HorizontalLayout priceLayout = new HorizontalLayout();
        priceLayout.setSpacing(true);
        priceLayout.setAlignItems(Alignment.CENTER);

        Icon priceIcon = new Icon(VaadinIcon.EURO);
        priceIcon.setColor("#ffc107");

        VerticalLayout priceInfo = new VerticalLayout();
        priceInfo.setSpacing(false);
        priceInfo.setPadding(false);

        int availablePlaces = eventService.getAvailablePlaces(currentEvent.getId());

        Span priceLabel = new Span("Prix et disponibilité");
        priceLabel.getStyle().set("font-weight", "bold").set("color", "#333");

        Span priceValue = new Span(String.format("%.2f € par personne", currentEvent.getPrixUnitaire()));
        priceValue.getStyle().set("color", "#28a745").set("font-weight", "bold");

        Span placesValue = new Span(availablePlaces + " places disponibles sur " + currentEvent.getCapaciteMax());
        placesValue.getStyle().set("color", availablePlaces > 0 ? "#666" : "#dc3545");

        priceInfo.add(priceLabel, priceValue, placesValue);
        priceLayout.add(priceIcon, priceInfo);

        detailsSection.add(detailsTitle, dateLayout, locationLayout, priceLayout);

        // Organisateur
        VerticalLayout organizerSection = new VerticalLayout();
        organizerSection.setSpacing(true);
        organizerSection.setPadding(true);
        organizerSection.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("margin-top", "1rem");

        H3 organizerTitle = new H3("Organisateur");
        organizerTitle.getStyle().set("margin", "0 0 1rem 0").set("color", "#333");

        if (currentEvent.getOrganisateur() != null) {
            User organizer = currentEvent.getOrganisateur();

            HorizontalLayout organizerInfo = new HorizontalLayout();
            organizerInfo.setSpacing(true);
            organizerInfo.setAlignItems(Alignment.CENTER);

            Icon userIcon = new Icon(VaadinIcon.USER);
            userIcon.setColor("#6c757d");

            VerticalLayout orgDetails = new VerticalLayout();
            orgDetails.setSpacing(false);
            orgDetails.setPadding(false);

            Span orgName = new Span(organizer.getPrenom() + " " + organizer.getNom());
            orgName.getStyle().set("font-weight", "bold").set("color", "#333");

            Span orgEmail = new Span(organizer.getEmail());
            orgEmail.getStyle().set("color", "#666").set("font-size", "0.9rem");

            if (organizer.getTelephone() != null && !organizer.getTelephone().trim().isEmpty()) {
                Span orgPhone = new Span("📞 " + organizer.getTelephone());
                orgPhone.getStyle().set("color", "#666").set("font-size", "0.9rem");
                orgDetails.add(orgName, orgEmail, orgPhone);
            } else {
                orgDetails.add(orgName, orgEmail);
            }

            organizerInfo.add(userIcon, orgDetails);
            organizerSection.add(organizerTitle, organizerInfo);
        } else {
            Span noOrganizer = new Span("Informations sur l'organisateur non disponibles.");
            noOrganizer.getStyle().set("color", "#666").set("font-style", "italic");
            organizerSection.add(organizerTitle, noOrganizer);
        }

        infoSection.add(titleSection, descriptionSection, detailsSection, organizerSection);

        mainContent.add(imageSection, infoSection);

        // Section des actions
        VerticalLayout actionsSection = new VerticalLayout();
        actionsSection.setWidthFull();
        actionsSection.setPadding(true);
        actionsSection.setSpacing(true);
        actionsSection.getStyle().set("background", "white").set("margin-top", "1rem");

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        // Bouton réserver avec vérification d'authentification
        Button reserveBtn = new Button("Réserver maintenant", new Icon(VaadinIcon.CALENDAR));
        reserveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        reserveBtn.getStyle().set("background-color", "#28a745").set("color", "white");

        if (availablePlaces > 0) {
            reserveBtn.addClickListener(e -> handleReservation());
        } else {
            reserveBtn.setText("Événement complet");
            reserveBtn.setEnabled(false);
            reserveBtn.setIcon(new Icon(VaadinIcon.CLOSE_CIRCLE));
        }

        Button shareBtn = new Button("Partager", new Icon(VaadinIcon.SHARE));
        shareBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        buttonsLayout.add(reserveBtn, shareBtn);
        actionsSection.add(buttonsLayout);

        // Assembler tout
        add(header, mainContent, actionsSection);
    }

    private void handleReservation() {
        // Vérifier si l'utilisateur est connecté
        User currentUser = VaadinSession.getCurrent().getAttribute(User.class);

        if (currentUser == null) {
            // Rediriger vers login avec message
            Notification.show("Veuillez vous connecter pour réserver.", 3000, Notification.Position.TOP_CENTER);
            UI.getCurrent().navigate("login");
            return;
        }

        // Rediriger vers la page de réservation
        UI.getCurrent().navigate("event/" + currentEvent.getId() + "/reserve");
    }
}
