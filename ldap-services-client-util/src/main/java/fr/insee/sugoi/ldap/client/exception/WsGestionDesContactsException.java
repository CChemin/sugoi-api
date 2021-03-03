package fr.insee.sugoi.ldap.client.exception;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import fr.insee.sugoi.converter.ouganext.ErrorResult;

@SuppressWarnings("serial")
public class WsGestionDesContactsException extends Exception {

  private String rootException;

  public WsGestionDesContactsException(ErrorResult error) {
    super(error.getMessage());
    this.rootException = error.getException();
  }

  public String getRootException() {
    return rootException;
  }

  /**
   * Génère un ErrorResult à partie d'une Response du web service supposée en
   * erreur. S'il est impossible de lire un objet ErrorResult dans la response,
   * l'erreur sera supposée à partir du code réponse.
   * 
   * @param response à anlyser
   * @return un ErrorResult le plus complet possible
   */
  public static ErrorResult determinerErreur(Response response) {
    ErrorResult error;
    try {
      error = response.readEntity(ErrorResult.class);
      if (error == null) {
        error = new ErrorResult();
        error.setException("Exception inconnue");
        error.setMessage("Raison inconnue");
      }
    } catch (ProcessingException e) {
      error = new ErrorResult();
      if (response.getStatus() == Status.BAD_REQUEST.getStatusCode()) {
        error.setException("Exception inconnue");
        error.setMessage("La requête n'a pas été acceptée par le Web Service");
      } else if (response.getStatus() == Status.FORBIDDEN.getStatusCode()) {
        error.setException("Exception inconnue");
        error.setMessage("Droits insuffisants.");
      } else {
        error.setException("Exception inconnue");
        error.setMessage("Raison inconnue");
      }
    }
    return error;
  }

}
