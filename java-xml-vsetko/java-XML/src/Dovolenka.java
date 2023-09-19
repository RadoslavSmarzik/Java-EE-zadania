public class Dovolenka {
    private String miesto;
    private int pocetDni;
    private String krajina = "";
    private int rok = 0;

    public Dovolenka(String miesto, int pocetDni) {
        this.miesto = miesto;
        this.pocetDni = pocetDni;
    }

    public Dovolenka(String miesto, String krajina, int pocetDni, int rok) {
        this.miesto = miesto;
        this.pocetDni = pocetDni;
        this.krajina = krajina;
        this.rok = rok;
    }

    public String getMiesto() {
        return miesto;
    }

    public int getPocetDni() {
        return pocetDni;
    }

    public String getKrajina() {
        return krajina;
    }

    public int getRok() {
        return rok;
    }
}
