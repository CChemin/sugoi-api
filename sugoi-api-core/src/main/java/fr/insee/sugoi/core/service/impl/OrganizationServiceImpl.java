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
package fr.insee.sugoi.core.service.impl;

import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.OrganizationService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Organization;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  @Autowired private RealmProvider realmProvider;

  @Autowired private StoreProvider storeProvider;

  @Override
  public Organization create(String realm, Organization organization, String storageName) {
    return storeProvider
        .getStoreForUserStorage(realm, storageName)
        .getWriter()
        .createOrganization(organization);
  }

  @Override
  public Organization create(String realm, Organization organization) {
    return create(realm, organization, realmProvider.load(realm).getDefaultUserStorageName());
  }

  @Override
  public void delete(String realm, String id, String storageName) {
    storeProvider.getStoreForUserStorage(realm, storageName).getWriter().deleteOrganization(id);
  }

  @Override
  public void delete(String realm, String id) {
    delete(realm, id, realmProvider.load(realm).getDefaultUserStorageName());
  }

  @Override
  public void update(String realm, Organization organization) {
    update(realm, organization, realmProvider.load(realm).getDefaultUserStorageName());
  }

  @Override
  public Organization findById(String realm, String id, String storage) {
    return storeProvider.getStoreForUserStorage(realm, storage).getReader().getOrganization(id);
  }

  @Override
  public Organization findById(String realm, String id) {
    return findById(realm, id, realmProvider.load(realm).getDefaultUserStorageName());
  }

  @Override
  public PageResult<Organization> findByProperties(
      String realm, Map<String, String> properties, String storageName) {
    return storeProvider
        .getStoreForUserStorage(realm, storageName)
        .getReader()
        .searchOrganizations(properties, new PageableResult(), "AND");
  }

  @Override
  public PageResult<Organization> findByProperties(String realm, Map<String, String> properties) {
    return findByProperties(
        realm, properties, realmProvider.load(realm).getDefaultUserStorageName());
  }

  @Override
  public PageResult<Organization> findAll(String realm, String storageName) {
    return storeProvider
        .getStoreForUserStorage(realm, storageName)
        .getReader()
        .searchOrganizations(new HashMap<>(), new PageableResult(), "AND");
  }

  @Override
  public PageResult<Organization> findAll(String realm) {
    return findAll(realm, realmProvider.load(realm).getDefaultUserStorageName());
  }

  @Override
  public void update(String realm, Organization organization, String storageName) {
    storeProvider
        .getStoreForUserStorage(realm, storageName)
        .getWriter()
        .updateOrganization(organization);
  }
}
