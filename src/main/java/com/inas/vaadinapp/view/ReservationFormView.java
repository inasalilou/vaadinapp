package com.inas.vaadinapp.view;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.inas.vaadinapp.entity.Event;
import com.inas.vaadinapp.entity.Reservation;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.EventService;
import com.inas.vaadinapp.service.ReservationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("event/:eventId/reserve")
public class ReservationFormView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private final ReservationService reservationService;

    private Event currentEvent;
    private User currentUser;

    // Composants UI
    private H1 pageTitle;
    private Div eventCard;
    private Div reservationForm;
    private Span availabilityInfo;
    private Span priceInfo;
    private Span totalInfo;
    private IntegerField nbPlacesField;
    private TextArea commentField;
    private Button reserveBtn;
    private Button cancelBtn;

    public ReservationFormView(EventService eventService, ReservationService reservationService) {
        this.eventService = eventService;
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");

        // Vérifier l'authentification
        currentUser = VaadinSession.getCurrent().getAttribute(User.class);
        if (currentUser == null) {
            UI.getCurrent().navigate("login");
            return;
        }
    }

    /* -------------------- CHARGEMENT ET INITIALISATION -------------------- */

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String idStr = event.getRouteParameters().get("eventId").orElse(null);
        if (idStr == null) {
            Notification.show("Événement non spécifié.", 3000, Notification.Position.TOP_CENTER);
            UI.getCurrent().navigate("events");
            return;
        }

        Long eventId;
        try {
            eventId = Long.valueOf(idStr);
        } catch (NumberFormatException ex) {
            Notification.show("Identifiant d'événement invalide.", 3000, Notification.Position.TOP_CENTER);
            UI.getCurrent().navigate("events");
            return;
        }

        Optional<Event> opt = eventService.findById(eventId);
        if (opt.isEmpty()) {
            Notification.show("Événement introuvable.", 3000, Notification.Position.TOP_CENTER);
            UI.getCurrent().navigate("events");
            return;
        }

        currentEvent = opt.get();

        // Vérifier que l'événement est disponible pour réservation
        if (currentEvent.getStatus().toString().equals("TERMINE") ||
            currentEvent.getDateFin().isBefore(java.time.LocalDateTime.now())) {
            Notification.show("Cet événement n'est plus disponible pour réservation.", 4000, Notification.Position.TOP_CENTER);
            UI.getCurrent().navigate("event/" + currentEvent.getId());
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
        header.getStyle()
                .set("background", "white")
                .set("border-radius", "0 0 16px 16px");

        HorizontalLayout navBar = new HorizontalLayout();
        navBar.setWidthFull();
        navBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        navBar.setAlignItems(Alignment.CENTER);

        Button backBtn = new Button(" Retour à l'événement", new Icon(VaadinIcon.ARROW_LEFT));
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.addClickListener(e -> UI.getCurrent().navigate("event/" + currentEvent.getId()));

        pageTitle = new H1("Réserver des places");
        pageTitle.getStyle()
                .set("color", "#333")
                .set("margin", "0");

        navBar.add(backBtn, pageTitle);
        header.add(navBar);

        // Section informations événement
        createEventCard();

        // Section formulaire de réservation
        createReservationForm();

        // Assembler tout
        add(header, eventCard, reservationForm);
    }

    /* -------------------- CRÉATION DE L'INTERFACE -------------------- */

    private void createEventCard() {
        eventCard = new Div();
        eventCard.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("padding", "1.5rem")
                .set("margin", "1rem");

        VerticalLayout cardContent = new VerticalLayout();
        cardContent.setSpacing(true);
        cardContent.setPadding(false);

        // Titre et statut
        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setAlignItems(Alignment.CENTER);
        titleRow.setSpacing(true);

        H2 eventTitle = new H2(currentEvent.getTitre());
        eventTitle.getStyle()
                .set("margin", "0")
                .set("color", "#333");

        Span statusBadge = new Span(" " + currentEvent.getStatus().toString());
        statusBadge.getStyle()
                .set("background-color", "#28a745")
                .set("color", "white")
                .set("padding", "4px 12px")
                .set("border-radius", "16px")
                .set("font-size", "0.875rem")
                .set("font-weight", "bold");

        titleRow.add(eventTitle, statusBadge);

        // Informations principales
        HorizontalLayout infoRow = new HorizontalLayout();
        infoRow.setSpacing(true);
        infoRow.setWidthFull();

        VerticalLayout leftInfo = new VerticalLayout();
        leftInfo.setSpacing(false);
        leftInfo.setPadding(false);

        Span dateInfo = new Span(" " + currentEvent.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        Span locationInfo = new Span(" " + currentEvent.getLieu() + ", " + currentEvent.getVille());
        Span categoryInfo = new Span(" " + currentEvent.getCategorie().toString());

        leftInfo.add(dateInfo, locationInfo, categoryInfo);

        VerticalLayout rightInfo = new VerticalLayout();
        rightInfo.setSpacing(false);
        rightInfo.setPadding(false);

        Span priceInfo = new Span(" " + String.format("%.2f dh", currentEvent.getPrixUnitaire()) + " par personne");
        priceInfo.getStyle()
                .set("font-weight", "bold")
                .set("color", "#28a745")
                .set("font-size", "1.1rem");

        int availablePlaces = eventService.getAvailablePlaces(currentEvent.getId());
        Span availabilityInfo = new Span("✅ " + availablePlaces + " places disponibles");
        availabilityInfo.getStyle()
                .set("color", availablePlaces > 0 ? "#28a745" : "#dc3545")
                .set("font-weight", "bold");

        rightInfo.add(priceInfo, availabilityInfo);

        infoRow.add(leftInfo, rightInfo);
        infoRow.setFlexGrow(1, rightInfo);

        // Description si présente
        if (currentEvent.getDescription() != null && !currentEvent.getDescription().trim().isEmpty()) {
            Paragraph description = new Paragraph(currentEvent.getDescription());
            description.getStyle()
                    .set("margin-top", "1rem")
                    .set("line-height", "1.5")
                    .set("color", "#666");
            cardContent.add(description);
        }

        cardContent.add(titleRow, infoRow);
        eventCard.add(cardContent);
    }

    private void createReservationForm() {
        reservationForm = new Div();
        reservationForm.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("padding", "1.5rem")
                .set("margin", "1rem");

        VerticalLayout formContent = new VerticalLayout();
        formContent.setSpacing(true);
        formContent.setPadding(false);

        H3 formTitle = new H3("Détails de votre réservation");
        formTitle.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("color", "#333");

        // Sélecteur de places
        VerticalLayout placesSection = new VerticalLayout();
        placesSection.setSpacing(false);
        placesSection.setPadding(false);

        nbPlacesField = new IntegerField("Nombre de places");
        nbPlacesField.setMin(1);
        nbPlacesField.setMax(10);
        nbPlacesField.setValue(1);
        nbPlacesField.setStepButtonsVisible(true);
        nbPlacesField.setWidth("200px");

        // Informations de prix calculées dynamiquement
        HorizontalLayout priceSection = new HorizontalLayout();
        priceSection.setSpacing(true);
        priceSection.setAlignItems(Alignment.CENTER);
        priceSection.setWidthFull();

        Span unitPriceLabel = new Span("Prix unitaire :");
        priceInfo = new Span(String.format("%.2f dh", currentEvent.getPrixUnitaire()));
        priceInfo.getStyle()
                .set("font-weight", "bold")
                .set("color", "#666");

        Span multiplier = new Span("×");
        multiplier.getStyle().set("color", "#999");

        Span placesCount = new Span("1 place(s) =");
        totalInfo = new Span(String.format("%.2f dh", currentEvent.getPrixUnitaire()));
        totalInfo.getStyle()
                .set("font-weight", "bold")
                .set("color", "#28a745")
                .set("font-size", "1.2rem");

        priceSection.add(unitPriceLabel, priceInfo, multiplier, placesCount, totalInfo);

        // Vérification de disponibilité (mise à jour après création des boutons)
        availabilityInfo = new Span();

        placesSection.add(nbPlacesField, priceSection, availabilityInfo);

        // Calcul dynamique du prix
        nbPlacesField.addValueChangeListener(e -> {
            Integer places = e.getValue();
            if (places != null && places >= 1 && places <= 10) {
                double total = places * currentEvent.getPrixUnitaire();
                placesCount.setText(places + " place(s) =");
                totalInfo.setText(String.format("%.2f dh", total));
                updateAvailabilityInfo(places);
            }
        });

        // Commentaire optionnel
        commentField = new TextArea("Commentaire (optionnel)");
        commentField.setPlaceholder("Ex: Besoin de sièges côte à côte, régime alimentaire particulier...");
        commentField.setWidthFull();
        commentField.setMaxLength(500);
        commentField.setHelperText("Maximum 500 caractères");

        // Boutons d'action
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setJustifyContentMode(JustifyContentMode.END);

        cancelBtn = new Button("Annuler", new Icon(VaadinIcon.CLOSE));
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.addClickListener(e -> UI.getCurrent().navigate("event/" + currentEvent.getId()));

        reserveBtn = new Button("Procéder à la réservation", new Icon(VaadinIcon.CALENDAR));
        reserveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        reserveBtn.addClickListener(e -> showConfirmationDialog());

        // Vérification de disponibilité initiale (reserveBtn doit déjà exister)
        updateAvailabilityInfo(1);

        buttonsLayout.add(cancelBtn, reserveBtn);

        formContent.add(formTitle, placesSection, commentField, buttonsLayout);
        reservationForm.add(formContent);
    }

    private void updateAvailabilityInfo(int requestedPlaces) {
        int available = eventService.getAvailablePlaces(currentEvent.getId());

        if (available >= requestedPlaces) {
            availabilityInfo.setText(" " + available + " places disponibles - Réservation possible");
            availabilityInfo.getStyle()
                    .set("color", "#28a745")
                    .set("font-weight", "bold");
            reserveBtn.setEnabled(true);
        } else {
            availabilityInfo.setText(" Seulement " + available + " places disponibles - Impossible de réserver " + requestedPlaces + " places");
            availabilityInfo.getStyle()
                    .set("color", "#dc3545")
                    .set("font-weight", "bold");
            reserveBtn.setEnabled(false);
        }
    }

    /* -------------------- CONFIRMATION ET VALIDATION -------------------- */

    private void showConfirmationDialog() {
        Integer nbPlaces = nbPlacesField.getValue();
        String commentaire = commentField.getValue();

        // Validation
        if (nbPlaces == null || nbPlaces < 1 || nbPlaces > 10) {
            Notification.show("Veuillez sélectionner un nombre de places valide (1-10).", 3000, Notification.Position.TOP_CENTER);
            return;
        }

        // Vérifier à nouveau la disponibilité
        int available = eventService.getAvailablePlaces(currentEvent.getId());
        if (nbPlaces > available) {
            Notification.show("Désolé, il n'y a plus assez de places disponibles.", 4000, Notification.Position.TOP_CENTER);
            updateAvailabilityInfo(nbPlaces); // Met à jour l'affichage
            return;
        }

        // Créer la boîte de dialogue de confirmation
        Dialog confirmationDialog = new Dialog();
        confirmationDialog.setHeaderTitle("Confirmer votre réservation");
        confirmationDialog.setWidth("500px");

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setSpacing(true);
        dialogContent.setPadding(false);

        // Récapitulatif
        Div summaryCard = new Div();
        summaryCard.getStyle()
                .set("background", "#f8f9fa")
                .set("border-radius", "8px")
                .set("padding", "1rem")
                .set("margin-bottom", "1rem");

        VerticalLayout summaryContent = new VerticalLayout();
        summaryContent.setSpacing(false);
        summaryContent.setPadding(false);

        H4 summaryTitle = new H4("Récapitulatif de votre réservation");
        summaryTitle.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("color", "#333");

        // Détails de l'événement
        Span eventName = new Span(" Événement : " + currentEvent.getTitre());
        Span eventDate = new Span(" Date : " + currentEvent.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm")));
        Span eventLocation = new Span(" Lieu : " + currentEvent.getLieu() + ", " + currentEvent.getVille());

        // Détails de la réservation
        Span placesReserved = new Span(" Places réservées : " + nbPlaces);
        Span unitPrice = new Span(" Prix unitaire : " + String.format("%.2f dh", currentEvent.getPrixUnitaire()));
        Span totalPrice = new Span(" Total : " + String.format("%.2f dh", nbPlaces * currentEvent.getPrixUnitaire()));

        placesReserved.getStyle().set("font-weight", "bold");
        totalPrice.getStyle().set("font-weight", "bold").set("color", "#28a745").set("font-size", "1.1rem");

        summaryContent.add(summaryTitle, eventName, eventDate, eventLocation, placesReserved, unitPrice, totalPrice);

        if (commentaire != null && !commentaire.trim().isEmpty()) {
            Span commentInfo = new Span(" Commentaire : " + commentaire.trim());
            summaryContent.add(commentInfo);
        }

        summaryCard.add(summaryContent);
        dialogContent.add(summaryCard);

        // Avertissement
        Paragraph warning = new Paragraph(" Cette réservation sera en attente de confirmation. Vous recevrez une confirmation par email.");
        warning.getStyle()
                .set("color", "#856404")
                .set("background-color", "#fff3cd")
                .set("padding", "0.75rem")
                .set("border-radius", "4px")
                .set("border", "1px solid #ffeaa7")
                .set("font-size", "0.9rem");

        dialogContent.add(warning);

        confirmationDialog.add(dialogContent);

        // Boutons de la boîte de dialogue
        Button cancelDialogBtn = new Button("Annuler", e -> confirmationDialog.close());
        cancelDialogBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button confirmDialogBtn = new Button("Confirmer la réservation", new Icon(VaadinIcon.CHECK));
        confirmDialogBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmDialogBtn.addClickListener(e -> {
            confirmationDialog.close();
            processReservation(nbPlaces, commentaire);
        });

        confirmationDialog.getFooter().add(cancelDialogBtn, confirmDialogBtn);
        confirmationDialog.open();
    }

    private void processReservation(int nbPlaces, String commentaire) {
        try {
            Reservation reservation = reservationService.createReservation(
                    currentEvent.getId(),
                    currentUser.getId(),
                    nbPlaces,
                    commentaire
            );

            // Afficher le succès avec le code de réservation
            showSuccessDialog(reservation);

        } catch (IllegalArgumentException ex) {
            Notification.show("Erreur de réservation : " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER);
        } catch (Exception ex) {
            Notification.show("Une erreur inattendue s'est produite. Veuillez réessayer.", 5000, Notification.Position.TOP_CENTER);
        }
    }

    private void showSuccessDialog(Reservation reservation) {
        Dialog successDialog = new Dialog();
        successDialog.setHeaderTitle("Réservation confirmée !");
        successDialog.setWidth("500px");

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setSpacing(true);
        dialogContent.setAlignItems(Alignment.CENTER);

        // Icône de succès
        Icon successIcon = new Icon(VaadinIcon.CHECK_CIRCLE);
        successIcon.setSize("4rem");
        successIcon.setColor("#28a745");

        // Message de succès
        H3 successTitle = new H3("Votre réservation a été enregistrée");
        successTitle.getStyle()
                .set("color", "#28a745")
                .set("text-align", "center")
                .set("margin", "0");

        // Code de réservation
        Div codeContainer = new Div();
        codeContainer.getStyle()
                .set("background", "#f8f9fa")
                .set("border", "2px solid #28a745")
                .set("border-radius", "8px")
                .set("padding", "1rem")
                .set("margin", "1rem 0")
                .set("text-align", "center");

        H4 codeTitle = new H4("Code de réservation");
        codeTitle.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("color", "#333");

        Span reservationCode = new Span(reservation.getCodeReservation());
        reservationCode.getStyle()
                .set("font-family", "monospace")
                .set("font-size", "1.5rem")
                .set("font-weight", "bold")
                .set("color", "#28a745")
                .set("letter-spacing", "2px");

        codeContainer.add(codeTitle, reservationCode);

        // Informations supplémentaires
        Paragraph info = new Paragraph(
                "Conservez ce code précieusement. Il vous permettra de suivre et gérer votre réservation. " +
                "Vous recevrez bientôt un email de confirmation."
        );
        info.getStyle()
                .set("text-align", "center")
                .set("color", "#666")
                .set("font-size", "0.9rem")
                .set("margin", "0");

        dialogContent.add(successIcon, successTitle, codeContainer, info);

        successDialog.add(dialogContent);

        // Bouton de fermeture
        Button closeBtn = new Button("Voir mes réservations", new Icon(VaadinIcon.LIST));
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        closeBtn.addClickListener(e -> {
            successDialog.close();
            UI.getCurrent().navigate("my-reservations");
        });

        Button backToEventBtn = new Button("Retour à l'événement", new Icon(VaadinIcon.ARROW_LEFT));
        backToEventBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backToEventBtn.addClickListener(e -> {
            successDialog.close();
            UI.getCurrent().navigate("event/" + currentEvent.getId());
        });

        successDialog.getFooter().add(backToEventBtn, closeBtn);
        successDialog.open();
    }
}
