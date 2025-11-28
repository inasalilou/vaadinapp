package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Event;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.EventService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.VaadinSession;

import java.util.Optional;

@Route("event/:eventId")
@PageTitle("Détail événement - EventManager")
public class EventDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;

    private Long eventId;
    private Event currentEvent;

    private H1 title = new H1();
    private Paragraph description = new Paragraph();
    private Span infos = new Span();
    private Span priceAndPlaces = new Span();
    private Span organizer = new Span();

    private Button reserveBtn = new Button("Réserver");
    private Button backBtn = new Button("Retour aux événements");

    public EventDetailView(EventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(title, description, infos, priceAndPlaces, organizer,
                new Span(" "), // petite séparation
                reserveBtn, backBtn);

        configureButtons();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> idOpt = event.getRouteParameters().get("eventId");
        if (idOpt.isEmpty()) {
            Notification.show("Aucun événement indiqué.");
            UI.getCurrent().navigate(EventListView.class);
            return;
        }

        try {
            this.eventId = Long.parseLong(idOpt.get());
        } catch (NumberFormatException e) {
            Notification.show("Identifiant d'événement invalide.");
            UI.getCurrent().navigate(EventListView.class);
            return;
        }

        this.currentEvent = eventService.findById(eventId).orElse(null);
        if (currentEvent == null) {
            Notification.show("Événement introuvable.");
            UI.getCurrent().navigate(EventListView.class);
            return;
        }

        loadEvent();
    }

    private void loadEvent() {
        title.setText(currentEvent.getTitre());

        description.setText(currentEvent.getDescription() != null
                ? currentEvent.getDescription()
                : "Pas de description.");

        String infosText = "Catégorie : " + currentEvent.getCategorie()
                + " | Ville : " + currentEvent.getVille()
                + " | Lieu : " + currentEvent.getLieu()
                + " | Début : " + currentEvent.getDateDebut()
                + " | Fin : " + currentEvent.getDateFin();
        infos.setText(infosText);

        int available = eventService.getAvailablePlaces(currentEvent.getId());
        priceAndPlaces.setText("Prix : " + currentEvent.getPrix() + " € | Places disponibles : " + available);

        if (currentEvent.getCreateur() != null) {
            User org = currentEvent.getCreateur();
            organizer.setText("Organisateur : " + org.getPrenom() + " " + org.getNom()
                    + " (" + org.getEmail() + ")");
        } else {
            organizer.setText("Organisateur : inconnu");
        }
    }

    private void configureButtons() {
        backBtn.addClickListener(e ->
                UI.getCurrent().navigate(EventListView.class));

        reserveBtn.addClickListener(e -> {
            if (currentEvent == null) {
                Notification.show("Événement introuvable.");
                return;
            }

            // Navigation vers la vue de réservation
            UI.getCurrent().navigate("event/" + currentEvent.getId() + "/reserve");
        });
    }
}
