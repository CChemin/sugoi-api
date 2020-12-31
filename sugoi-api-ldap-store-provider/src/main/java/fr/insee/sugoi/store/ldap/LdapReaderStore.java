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
package fr.insee.sugoi.store.ldap;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import fr.insee.sugoi.core.exceptions.EntityNotFoundException;
import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.ldap.utils.LdapFactory;
import fr.insee.sugoi.ldap.utils.LdapUtils;
import fr.insee.sugoi.ldap.utils.mapper.AddressLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.OrganizationLdapMapper;
import fr.insee.sugoi.ldap.utils.mapper.UserLdapMapper;
import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.User;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LdapReaderStore implements ReaderStore {

  private LDAPConnectionPool ldapPoolConnection;

  private static final Logger logger = LogManager.getLogger(LdapReaderStore.class);

  private Map<String, String> config;

  public LdapReaderStore(Map<String, String> config) {
    logger.debug("Configuring LdapReaderStore with config : {}", config);
    try {
      LdapFactory.getSingleConnection(config);
      this.ldapPoolConnection = LdapFactory.getConnectionPool(config);
      this.config = config;
    } catch (LDAPException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public User getUser(String id) {
    logger.debug("Searching user {}", id);
    SearchResultEntry entry = getEntryByDn("uid=" + id + "," + config.get("user_source"));
    return UserLdapMapper.mapFromSearchEntry(entry);
  }

  @Override
  public Organization getOrganization(String id) {
    SearchResultEntry entry = getEntryByDn("uid=" + id + "," + config.get("organization_source"));
    Organization org = OrganizationLdapMapper.mapFromSearchEntry(entry);
    if (org.getAttributes().containsKey("adressDn")) {
      SearchResultEntry result = getEntryByDn(org.getAttributes().get("adressDn").toString());
      org.setAddress(AddressLdapMapper.mapFromSearchEntry(result));
    }
    return org;
  }

  @Override
  public PageResult<User> searchUsers(
      Map<String, String> properties, PageableResult pageable, String typeRecherche) {
    try {
      PageResult<User> page = new PageResult<>();
      Filter filter = LdapUtils.filterRechercher(typeRecherche, pageable);
      SearchRequest searchRequest =
          new SearchRequest(
              config.get("user_source"), SearchScope.SUBORDINATE_SUBTREE, filter, "*", "+");
      LdapUtils.setRequestControls(searchRequest, pageable);
      SearchResult searchResult = ldapPoolConnection.search(searchRequest);
      List<User> users =
          searchResult.getSearchEntries().stream()
              .map(e -> UserLdapMapper.mapFromSearchEntry(e))
              .collect(Collectors.toList());
      LdapUtils.setResponseControls(page, searchResult);
      page.setResults(users);
      return page;
    } catch (LDAPSearchException e) {
      throw new RuntimeException("Fail to execute user search", e);
    }
  }

  public SearchResultEntry getEntryByDn(String dn) {
    try {
      logger.debug("Fetching {}", dn);
      SearchResultEntry entry = ldapPoolConnection.getEntry(dn, "+", "*");

      return entry;
    } catch (LDAPException e) {
      throw new EntityNotFoundException("Entry not found");
    }
  }

  @Override
  public PageResult<User> getUsersInGroup(String appName, String groupName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PageResult<Organization> searchOrganizations(
      Map<String, String> searchProperties, PageableResult pageable, String searchOperator) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Group getGroup(String appName, String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PageResult<Group> searchGroups(
      String appName,
      Map<String, String> searchProperties,
      PageableResult pageable,
      String searchOperator) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean validateCredentials(User user, String credential) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Application getApplication(String applicationName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PageResult<Application> searchApplications(
      Map<String, String> searchProperties, PageableResult pageable, String searchOperator) {
    // TODO Auto-generated method stub
    return null;
  }
}
