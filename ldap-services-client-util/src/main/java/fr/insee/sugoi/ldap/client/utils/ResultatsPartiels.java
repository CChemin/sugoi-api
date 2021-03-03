package fr.insee.sugoi.ldap.client.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import fr.insee.sugoi.converter.ouganext.Entite;

public class ResultatsPartiels<T extends Entite> {

  private List<T> listeResultats;
  private int nombreResultatsTotaux;
  private int firstIndexPartiel;

  // cas de recgherche paginee
  private URI nextlocation = null;

  /**
   * Si aucun résultat renvoit une liste vide.
   * 
   * @return la liste des résuktats éventuellement vide.
   */
  public List<T> getListeResultats() {
    if (listeResultats == null) {
      listeResultats = new ArrayList<>();
    }
    return listeResultats;
  }

  public void setListeResultats(List<T> listeResultats) {
    this.listeResultats = listeResultats;
  }

  public int getNombreResultatsTotaux() {
    return nombreResultatsTotaux;
  }

  public void setNombreResultatsTotaux(int nombreResultatsTotaux) {
    this.nombreResultatsTotaux = nombreResultatsTotaux;
  }

  public int getFirstIndexPartiel() {
    return firstIndexPartiel;
  }

  public void setFirstIndexPartiel(int firstIndexPartiel) {
    this.firstIndexPartiel = firstIndexPartiel;
  }

  public URI getNextlocation() {
    return nextlocation;
  }

  public void setNextlocation(URI nextlocation) {
    this.nextlocation = nextlocation;
  }

}
