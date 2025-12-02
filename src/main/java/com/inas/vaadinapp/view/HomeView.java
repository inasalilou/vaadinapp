package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Category;
import com.inas.vaadinapp.entity.Event;
import com.inas.vaadinapp.service.EventService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route("")
@RouteAlias("home")
@PageTitle("Accueil - EventManager")
public class HomeView extends VerticalLayout {

    private final EventService eventService;
    private VerticalLayout featuredEventsLayout;
    private VerticalLayout allEventsLayout;
    private ComboBox<Category> categoryFilter;
    private TextField cityFilter;
    private DatePicker dateFilter;
    private Button searchButton;

    public HomeView(EventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");

        createHeader();
        createFeaturedSection();
        createSearchSection();
        createEventsSection();

        loadFeaturedEvents();
        loadAllEvents();
    }

    private void createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.CENTER);
        header.setPadding(true);
        header.setSpacing(false);
        header.setWidthFull();
        header.setHeight("400px");
        header.getStyle()
                .set("background", "linear-gradient(135deg, rgba(102, 126, 234, 0.9) 0%, rgba(118, 75, 162, 0.9) 100%)")
                .set("color", "white")
                .set("text-align", "center");

        H1 mainTitle = new H1("EventManager");
        mainTitle.getStyle()
                .set("font-size", "3.5rem")
                .set("margin", "0")
                .set("font-weight", "700")
                .set("text-shadow", "2px 2px 4px rgba(0,0,0,0.3)");

        H2 subtitle = new H2("Découvrez et réservez vos événements préférés");
        subtitle.getStyle()
                .set("font-size", "1.5rem")
                .set("margin", "1rem 0")
                .set("font-weight", "300")
                .set("opacity", "0.9");

        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setSpacing(true);
        actionButtons.setJustifyContentMode(JustifyContentMode.CENTER);

        Button exploreBtn = new Button("Explorer les événements", new Icon(VaadinIcon.SEARCH));
        exploreBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        exploreBtn.getStyle().set("background-color", "white").set("color", "#667eea");

        Button loginBtn = new Button("Se connecter", e -> UI.getCurrent().navigate("login"));
        loginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        loginBtn.getStyle().set("color", "white");

        Button registerBtn = new Button("S'inscrire", e -> UI.getCurrent().navigate("register"));
        registerBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        registerBtn.getStyle().set("color", "white");

        actionButtons.add(exploreBtn, loginBtn, registerBtn);

        header.add(mainTitle, subtitle, actionButtons);
        add(header);
    }

    private void createFeaturedSection() {
        VerticalLayout featuredSection = new VerticalLayout();
        featuredSection.setWidthFull();
        featuredSection.setPadding(true);
        featuredSection.setSpacing(true);
        featuredSection.getStyle()
                .set("background-color", "white")
                .set("margin-top", "-2rem")
                .set("border-radius", "16px 16px 0 0")
                .set("position", "relative")
                .set("z-index", "1");

        H2 featuredTitle = new H2("🌟 Événements à la une");
        featuredTitle.getStyle()
                .set("text-align", "center")
                .set("color", "#333")
                .set("margin-bottom", "1rem");

        featuredEventsLayout = new VerticalLayout();
        featuredEventsLayout.setWidthFull();
        featuredEventsLayout.setSpacing(true);
        featuredEventsLayout.setPadding(false);

        featuredSection.add(featuredTitle, featuredEventsLayout);
        add(featuredSection);
    }

    private void createSearchSection() {
        VerticalLayout searchSection = new VerticalLayout();
        searchSection.setWidthFull();
        searchSection.setPadding(true);
        searchSection.setSpacing(true);
        searchSection.getStyle().set("background-color", "#f8f9fa");

        H3 searchTitle = new H3("🔍 Rechercher des événements");
        searchTitle.getStyle()
                .set("text-align", "center")
                .set("color", "#333")
                .set("margin-bottom", "1rem");

        HorizontalLayout searchFilters = new HorizontalLayout();
        searchFilters.setWidthFull();
        searchFilters.setSpacing(true);
        searchFilters.setAlignItems(Alignment.END);

        categoryFilter = new ComboBox<>("Catégorie");
        categoryFilter.setItems(Category.values());
        categoryFilter.setPlaceholder("Toutes les catégories");
        categoryFilter.setClearButtonVisible(true);

        cityFilter = new TextField("Ville");
        cityFilter.setPlaceholder("Toutes les villes");
        cityFilter.setClearButtonVisible(true);

        dateFilter = new DatePicker("Date");
        dateFilter.setPlaceholder("Toutes les dates");

        searchButton = new Button("Rechercher", new Icon(VaadinIcon.SEARCH));
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> searchEvents());

        searchFilters.add(categoryFilter, cityFilter, dateFilter, searchButton);

        searchSection.add(searchTitle, searchFilters);
        add(searchSection);
    }

    private void createEventsSection() {
        VerticalLayout eventsSection = new VerticalLayout();
        eventsSection.setWidthFull();
        eventsSection.setPadding(true);
        eventsSection.setSpacing(true);

        H2 eventsTitle = new H2("📅 Tous les événements");
        eventsTitle.getStyle()
                .set("text-align", "center")
                .set("color", "#333")
                .set("margin-bottom", "1rem");

        allEventsLayout = new VerticalLayout();
        allEventsLayout.setWidthFull();
        allEventsLayout.setSpacing(true);
        allEventsLayout.setPadding(false);

        eventsSection.add(eventsTitle, allEventsLayout);
        add(eventsSection);
    }

    private void loadFeaturedEvents() {
        featuredEventsLayout.removeAll();
        List<Event> featuredEvents = eventService.getPopularEvents(3); // Top 3 événements populaires

        if (featuredEvents.isEmpty()) {
            Paragraph noEvents = new Paragraph("Aucun événement à la une pour le moment.");
            noEvents.getStyle().set("text-align", "center").set("color", "#666");
            featuredEventsLayout.add(noEvents);
            return;
        }

        HorizontalLayout featuredCards = new HorizontalLayout();
        featuredCards.setWidthFull();
        featuredCards.setSpacing(true);
        featuredCards.setJustifyContentMode(JustifyContentMode.CENTER);

        for (Event event : featuredEvents) {
            featuredCards.add(createFeaturedEventCard(event));
        }

        featuredEventsLayout.add(featuredCards);
    }

    private void loadAllEvents() {
        allEventsLayout.removeAll();
        List<Event> allEvents = eventService.searchEvents(null, null, null, null, null);

        if (allEvents.isEmpty()) {
            Paragraph noEvents = new Paragraph("Aucun événement disponible pour le moment.");
            noEvents.getStyle().set("text-align", "center").set("color", "#666");
            allEventsLayout.add(noEvents);
            return;
        }

        // Afficher les événements en grille responsive
        int cardsPerRow = 3;
        HorizontalLayout currentRow = new HorizontalLayout();
        currentRow.setWidthFull();
        currentRow.setSpacing(true);

        for (int i = 0; i < allEvents.size(); i++) {
            currentRow.add(createEventCard(allEvents.get(i)));

            if ((i + 1) % cardsPerRow == 0 || i == allEvents.size() - 1) {
                allEventsLayout.add(currentRow);
                if (i < allEvents.size() - 1) {
                    currentRow = new HorizontalLayout();
                    currentRow.setWidthFull();
                    currentRow.setSpacing(true);
                }
            }
        }
    }

    private Div createFeaturedEventCard(Event event) {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
                .set("overflow", "hidden")
                .set("cursor", "pointer")
                .set("transition", "transform 0.2s, box-shadow 0.2s")
                .set("width", "350px")
                .set("border", "2px solid #667eea");

        card.addClickListener(e -> UI.getCurrent().navigate("event/" + event.getId()));

        // Hover effect
        card.getElement().addEventListener("mouseenter", e ->
            card.getStyle().set("transform", "translateY(-4px)").set("box-shadow", "0 8px 25px rgba(0,0,0,0.15)")
        );
        card.getElement().addEventListener("mouseleave", e ->
            card.getStyle().set("transform", "translateY(0)").set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
        );

        // Image placeholder
        Div imageDiv = new Div();
        imageDiv.getStyle()
                .set("height", "180px")
                .set("background", "linear-gradient(45deg, #667eea, #764ba2)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("color", "white")
                .set("font-size", "3rem");

        Icon eventIcon = new Icon(VaadinIcon.CALENDAR);
        imageDiv.add(eventIcon);

        // Content
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(false);

        H3 title = new H3(event.getTitre());
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "1.25rem")
                .set("color", "#333");

        Span category = new Span(event.getCategorie().toString());
        category.getStyle()
                .set("color", "#667eea")
                .set("font-weight", "bold")
                .set("font-size", "0.875rem");

        Span location = new Span("📍 " + event.getVille());
        location.getStyle().set("color", "#666").set("font-size", "0.875rem");

        Span date = new Span("📅 " + event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        date.getStyle().set("color", "#666").set("font-size", "0.875rem");

        Span price = new Span("💰 " + String.format("%.2f €", event.getPrixUnitaire()));
        price.getStyle()
                .set("color", "#28a745")
                .set("font-weight", "bold")
                .set("font-size", "1.1rem");

        Span popularBadge = new Span("⭐ Populaire");
        popularBadge.getStyle()
                .set("background-color", "#ffc107")
                .set("color", "#000")
                .set("padding", "2px 8px")
                .set("border-radius", "12px")
                .set("font-size", "0.75rem")
                .set("font-weight", "bold");

        content.add(title, category, location, date, price, popularBadge);

        card.add(imageDiv, content);
        return card;
    }

    private Div createEventCard(Event event) {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("overflow", "hidden")
                .set("cursor", "pointer")
                .set("transition", "transform 0.2s, box-shadow 0.2s")
                .set("flex", "1")
                .set("min-width", "300px")
                .set("max-width", "400px");

        card.addClickListener(e -> UI.getCurrent().navigate("event/" + event.getId()));

        // Hover effect
        card.getElement().addEventListener("mouseenter", e ->
            card.getStyle().set("transform", "translateY(-2px)").set("box-shadow", "0 4px 16px rgba(0,0,0,0.15)")
        );
        card.getElement().addEventListener("mouseleave", e ->
            card.getStyle().set("transform", "translateY(0)").set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
        );

        // Image placeholder
        Div imageDiv = new Div();
        imageDiv.getStyle()
                .set("height", "120px")
                .set("background", "linear-gradient(45deg, #f093fb, #f5576c)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("color", "white")
                .set("font-size", "2rem");

        Icon eventIcon = new Icon(VaadinIcon.CALENDAR);
        imageDiv.add(eventIcon);

        // Content
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(false);

        H4 title = new H4(event.getTitre());
        title.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("color", "#333")
                .set("font-size", "1.1rem");

        HorizontalLayout details = new HorizontalLayout();
        details.setSpacing(true);
        details.setWidthFull();

        Span category = new Span(event.getCategorie().toString());
        category.getStyle()
                .set("color", "#667eea")
                .set("font-weight", "bold")
                .set("font-size", "0.8rem");

        Span location = new Span("📍 " + event.getVille());
        location.getStyle().set("color", "#666").set("font-size", "0.8rem");

        details.add(category, location);
        details.setFlexGrow(1, location);

        Span date = new Span("📅 " + event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        date.getStyle().set("color", "#666").set("font-size", "0.8rem");

        Span price = new Span("💰 " + String.format("%.2f €", event.getPrixUnitaire()));
        price.getStyle()
                .set("color", "#28a745")
                .set("font-weight", "bold")
                .set("font-size", "1rem");

        content.add(title, details, date, price);

        card.add(imageDiv, content);
        return card;
    }

    private void searchEvents() {
        Category selectedCategory = categoryFilter.getValue();
        String selectedCity = cityFilter.getValue();
        LocalDate selectedDate = dateFilter.getValue();

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (selectedDate != null) {
            startDate = selectedDate.atStartOfDay();
            endDate = selectedDate.atTime(23, 59, 59);
        }

        List<Event> filteredEvents = eventService.searchEvents(
                selectedCity,
                selectedCategory,
                startDate,
                endDate,
                null
        );

        // Update the events section with filtered results
        allEventsLayout.removeAll();

        if (filteredEvents.isEmpty()) {
            Paragraph noResults = new Paragraph("Aucun événement trouvé pour ces critères.");
            noResults.getStyle().set("text-align", "center").set("color", "#666");
            allEventsLayout.add(noResults);
            return;
        }

        // Display filtered events in grid
        HorizontalLayout currentRow = new HorizontalLayout();
        currentRow.setWidthFull();
        currentRow.setSpacing(true);

        for (int i = 0; i < filteredEvents.size(); i++) {
            currentRow.add(createEventCard(filteredEvents.get(i)));

            if ((i + 1) % 3 == 0 || i == filteredEvents.size() - 1) {
                allEventsLayout.add(currentRow);
                if (i < filteredEvents.size() - 1) {
                    currentRow = new HorizontalLayout();
                    currentRow.setWidthFull();
                    currentRow.setSpacing(true);
                }
            }
        }
    }
}
