package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Event;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.EventService;
import com.inas.vaadinapp.service.ReservationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.Optional;

@Route("event/:eventId/reserve")
public class ReservationFormView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private final ReservationService reservationService;

    // L’événement actuellement réservé
    private Event currentEvent;

    // UI
    private final H2 title = new H2("Réserver un événement");
    private final Paragraph eventInfo = new Paragraph();

    private final IntegerField nbPlacesField = new IntegerField("Nombre de places");
    private final TextArea commentField = new TextArea("Commentaire (optionnel)");

    private final Button confirmBtn = new Button("Confirmer");
    private final Button cancelBtn = new Button("Annuler");

    public ReservationFormView(EventService eventService,
                               ReservationService reservationService) {
        this.eventService = eventService;
        this.reservationService = reservationService;

        add(title, eventInfo, nbPlacesField, commentField, confirmBtn, cancelBtn);

        configureForm();
    }

    /* ---------------------- CHARGEMENT DE L'ÉVÉNEMENT ---------------------- */

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // On récupère l'ID dans l’URL : /event/{id}/reserve
        String idStr = event.getRouteParameters().get("eventId").orElse(null);
        if (idStr == null) {
            event.rerouteTo("events");
            return;
        }

        Long eventId;
        try {
            eventId = Long.valueOf(idStr);
        } catch (NumberFormatException ex) {
            event.rerouteTo("events");
            return;
        }

        Optional<Event> opt = eventService.findById(eventId);
        if (opt.isEmpty()) {
            event.rerouteTo("events");
            return;
        }

        currentEvent = opt.get();

        // Texte d’info en haut du formulaire
        eventInfo.setText(
                currentEvent.getTitre() + " — " +
                        currentEvent.getVille() + " — " +
                        currentEvent.getDateDebut()
        );
    }

    /* ---------------------- CONFIGURATION FORMULAIRE ---------------------- */

    private void configureForm() {
        nbPlacesField.setMin(1);
        nbPlacesField.setMax(10);
        nbPlacesField.setValue(1);

        commentField.setPlaceholder("Ex : Besoin de sièges côte à côte…");
        commentField.setWidthFull();

        confirmBtn.addClickListener(e -> onConfirm());

        cancelBtn.addClickListener(e -> {
            if (currentEvent != null) {
                UI.getCurrent().navigate("event/" + currentEvent.getId());
            } else {
                UI.getCurrent().navigate("events");
            }
        });
    }

    /* ---------------------- CONFIRMATION ---------------------- */

    private void onConfirm() {
        if (currentEvent == null) {
            Notification.show("Événement introuvable.");
            UI.getCurrent().navigate("events");
            return;
        }

        // Récupérer l’utilisateur connecté depuis la session
        User user = VaadinSession.getCurrent().getAttribute(User.class);
        if (user == null) {
            Notification.show("Veuillez vous connecter pour réserver.");
            UI.getCurrent().navigate("login");
            return;
        }

        Integer nbPlaces = nbPlacesField.getValue();
        if (nbPlaces == null || nbPlaces <= 0 || nbPlaces > 10) {
            Notification.show("Le nombre de places doit être entre 1 et 10.");
            return;
        }

        String commentaire = commentField.getValue();

        // 🔥 On délègue la logique métier à ReservationService
        // (méthode à adapter si le nom est différent dans ton service)
        reservationService.createReservation(
                currentEvent.getId(),
                user.getId(),
                nbPlaces,
                commentaire
        );

        Notification.show("Réservation effectuée avec succès !");
        UI.getCurrent().navigate("event/" + currentEvent.getId());
    }
}
