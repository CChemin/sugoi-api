/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.ldap.utils;

import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.controls.SimplePagedResultsControl;
import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LdapUtils {

  public static String buildDn(String id, String baseDn) {
    return "uid=" + id + "," + baseDn;
  }

  public static Filter filterRechercher(
      String typeRecherche,
      String identifiant,
      String nomCommun,
      String description,
      String organisationId,
      String mail,
      PageableResult pageable,
      List<String> habilitations,
      String certificat) {
    List<Filter> filters = new ArrayList<>();
    if (identifiant != null) {
      filters.add(LdapFilter.contains("uid", identifiant));
    }
    if (nomCommun != null) {

      filters.add(LdapFilter.contains("cn", nomCommun));
    }
    if (description != null) {
      filters.add(LdapFilter.contains("description", description));
    }
    if (organisationId != null) {
      filters.add(LdapFilter.likeFilterOrganisation(organisationId));
    }
    if (mail != null) {
      filters.add(LdapFilter.contains("mail", mail));
    }
    if (habilitations != null) {
      for (String habilitation : habilitations) {
        filters.add(LdapFilter.contains("inseeGroupeDefaut", habilitation));
      }
    }
    if (certificat != null) {
      filters.add(LdapFilter.contains("inseePropriete", "certificateId$" + certificat));
    }
    if (typeRecherche.equals(TypeRecherche.ET.getTypeRecherche())) {
      return LdapFilter.and(filters);
    }
    if (typeRecherche.equals(TypeRecherche.OU.getTypeRecherche())) {
      return LdapFilter.or(filters);
    }
    throw new RuntimeException("Impossible de determiner le type de la recherche");
  }

  public static Filter getFilterFromCriteria(Map<String, String> searchProperties) {
    return LdapFilter.and(
        searchProperties.keySet().stream()
            .filter(key -> searchProperties.get(key) != null)
            .map(key -> LdapFilter.contains(key, searchProperties.get(key)))
            .collect(Collectors.toList()));
  }

  public static <T> void setResponseControls(PageResult<T> page, SearchResult searchResult) {
    Control[] responseControl = searchResult.getResponseControls();
    if (responseControl == null) {
      return;
    }
    for (Control control : responseControl) {
      if (control instanceof SimplePagedResultsControl) {
        SimplePagedResultsControl prc = (SimplePagedResultsControl) control;
        if (prc.getCookie().getValueLength() > 0) {
          ASN1OctetString cookie = prc.getCookie();
          page.setSearchCookie(cookie.getValue());
          page.setHasMoreResult(true);
          // On met -1 car 0 par defaut et on veut preciser que la taille n'a pas ete
          // fournie
          page.setTotalElements(prc.getSize() == 0 ? -1 : prc.getSize());
        }
      }
    }
  }

  public static void setRequestControls(SearchRequest searchRequest, PageableResult pageable) {
    ASN1OctetString cookie = new ASN1OctetString(pageable.getCookie());
    searchRequest.addControl(new SimplePagedResultsControl(pageable.getSize(), cookie, true));
  }
}
