package fr.insee.sugoi.converter.ouganext;

import java.util.List;

public interface Entites<T extends Entite> {

  /**
   * La liste des entites ou une liste vide.
   */
  public List<T> getListe();

}
