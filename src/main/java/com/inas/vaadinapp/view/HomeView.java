package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Category;
import com.inas.vaadinapp.entity.Event;
import com.inas.vaadinapp.service.EventService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.time.LocalDateTime;
import java.util.List;

@Route("")
@RouteAlias("home")
@PageTitle("Accueil - EventManager")
public class HomeView extends VerticalLayout {

    private final EventService eventService;
    private final Grid<Event> grid = new Grid<>(Event.class, false);

    public HomeView(EventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Événements disponibles");

        configureGrid();
        loadEvents();

        Button goToLogin = new Button("Se connecter", e -> getUI().ifPresent(ui -> ui.navigate("login")));
        Button goToRegister = new Button("S'inscrire", e -> getUI().ifPresent(ui -> ui.navigate("register")));

        add(title, goToLogin, goToRegister, grid);
    }

    private void configureGrid() {
        grid.addColumn(Event::getTitre).setHeader("Titre");
        grid.addColumn(Event::getCategorie).setHeader("Catégorie");
        grid.addColumn(Event::getVille).setHeader("Ville");
        grid.addColumn(Event::getDateDebut).setHeader("Date de début");
        grid.addColumn(Event::getPrix).setHeader("Prix (€)");
        grid.setWidthFull();
    }

    private void loadEvents() {
        // on réutilise searchEvents avec tous les filtres à null : retourne les événements PUBLIE
        List<Event> events = eventService.searchEvents(
                null,       // ville
                null,       // catégorie
                (LocalDateTime) null, // date début min
                null,       // date fin max
                null        // prix max
        );
        grid.setItems(events);
    }
}
