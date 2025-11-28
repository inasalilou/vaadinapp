package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.User;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@PageTitle("Dashboard - EventManager")
@Route("dashboard")
public class DashboardView extends VerticalLayout {

    public DashboardView() {

        // Récupération utilisateur connecté
        User user = VaadinSession.getCurrent().getAttribute(User.class);

        String prenom = (user != null && user.getPrenom() != null)
                ? user.getPrenom().toUpperCase()
                : "INVITÉ";

        add(new H1("Bonjour, " + prenom + " 👋"));

        add(new Paragraph("Bienvenue sur votre espace client EventManager."));
        add(new Paragraph("Statistiques personnelles : (à compléter plus tard)"));


        // ----- Boutons -----
        Button eventsBtn = new Button("Voir les événements");
        Button reservationsBtn = new Button("Mes réservations");
        Button profileBtn = new Button("Mon profil");
        Button logoutBtn = new Button("Se déconnecter");

        // 👉 Redirections correctes
        eventsBtn.addClickListener(e ->
                UI.getCurrent().navigate("events")
        );

        reservationsBtn.addClickListener(e ->
                UI.getCurrent().navigate("my-reservations")
        );

        profileBtn.addClickListener(e ->
                UI.getCurrent().navigate("profile")
        );

        logoutBtn.addClickListener(e -> {
            VaadinSession.getCurrent().getSession().invalidate();
            VaadinSession.getCurrent().close();
            UI.getCurrent().navigate("login");
        });

        HorizontalLayout btnLayout = new HorizontalLayout(
                eventsBtn, reservationsBtn, profileBtn, logoutBtn
        );

        add(btnLayout);
    }
}
