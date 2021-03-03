package fr.insee.sugoi.ldap.client.service.impl;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import fr.insee.sugoi.ldap.client.resources.TargetsWsGestionContacts;
import fr.insee.sugoi.converter.ouganext.Organisation;
import fr.insee.sugoi.converter.ouganext.Organisations;

public class ClientWsOrganisation extends ClientWsEntite<Organisation> {

  public ClientWsOrganisation(TargetsWsGestionContacts targets) {
    this.targets = targets;
  }

  @Override
  protected Class<Organisation> getClasse() {
    return Organisation.class;
  }

  @Override
  protected Organisations recupererListeEntites(Response response) {
    return response.readEntity(Organisations.class);
  }

  @Override
  protected WebTarget getTargetUnique(String id) {
    return targets.getOrganisationTarget(id);
  }

  @Override
  protected WebTarget getTargetUnique(String id, String domaine) {
    return targets.getOrganisationTarget(id, domaine);
  }

  @Override
  protected WebTarget getTargetMultiple(String domaine) {
    return targets.getOrganisationsTarget(domaine);
  }

  @Override
  protected String getNomLog() {
    return "organisation";
  }

}
