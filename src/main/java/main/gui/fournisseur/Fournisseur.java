package main.gui.fournisseur;

public class Fournisseur {
    private Integer numfou;
    private String nomfou;
    private String ruefou;
    private String posfou;
    private String vilfou;
    private String contact;

    public Fournisseur(Integer numfou,String nomfou, String ruefou, String posfou, String vilfou,String contact) {
        this.numfou = numfou;
        this.nomfou = nomfou;
        this.ruefou = ruefou;
        this.posfou = posfou;
        this.vilfou = vilfou;
        this.contact = contact;
    }

    public Integer getNumfou() {
        return numfou;
    }

    public String getNomfou() {
        return nomfou;
    }

    public String getRuefou() {
        return ruefou;
    }

    public String getPosfou() {
        return posfou;
    }

    public String getVilfou() {
        return vilfou;
    }

    public String getContact() {
        return contact;
    }

    public void setNumfou(Integer numfou) {
        this.numfou = numfou;
    }

    public void setNomfou(String nomfou) {
        this.nomfou = nomfou;
    }

    public void setRuefou(String ruefou) {
        this.ruefou = ruefou;
    }

    public void setPosfou(String posfou) {
        this.posfou = posfou;
    }

    public void setVilfou(String vilfou) {
        this.vilfou = vilfou;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

}