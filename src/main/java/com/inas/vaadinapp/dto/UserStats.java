package com.inas.vaadinapp.dto;

public class UserStats {

    private long nbEvenementsCrees;
    private long nbReservations;
    private double montantDepense;

    public UserStats() {
    }

    public UserStats(long nbEvenementsCrees, long nbReservations, double montantDepense) {
        this.nbEvenementsCrees = nbEvenementsCrees;
        this.nbReservations = nbReservations;
        this.montantDepense = montantDepense;
    }

    public long getNbEvenementsCrees() {
        return nbEvenementsCrees;
    }

    public void setNbEvenementsCrees(long nbEvenementsCrees) {
        this.nbEvenementsCrees = nbEvenementsCrees;
    }

    public long getNbReservations() {
        return nbReservations;
    }

    public void setNbReservations(long nbReservations) {
        this.nbReservations = nbReservations;
    }

    public double getMontantDepense() {
        return montantDepense;
    }

    public void setMontantDepense(double montantDepense) {
        this.montantDepense = montantDepense;
    }
}
