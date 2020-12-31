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
import fr.insee.sugoi.core.service.ApplicationService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.Application;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationServiceImpl implements ApplicationService {

  @Autowired private RealmProvider realmProvider;

  @Autowired private StoreProvider storeProvider;

  @Override
  public Application create(String realm, Application application, String storageName) {
    return storeProvider
        .getStoreForUserStorage(realm, storageName)
        .getWriter()
        .createApplication(application);
  }

  @Override
  public Application create(String realm, Application application) {
    return create(realm, application, realmProvider.load(realm).getDefaultUserStorageName());
  }

  @Override
  public void delete(String realm, String id, String storageName) {
    storeProvider.getStoreForUserStorage(realm, storageName).getWriter().deleteApplication(id);
  }

  @Override
  public void delete(String realm, String id) {
    delete(realm, id, realmProvider.load(realm).getDefaultUserStorageName());
  }

  @Override
  public void update(String realm, Application application, String storageName) {
    storeProvider
        .getStoreForUserStorage(realm, storageName)
        .getWriter()
        .updateApplication(application);
  }

  @Override
  public void update(String realm, Application application) {
    update(realm, application, realmProvider.load(realm).getDefaultUserStorageName());
  }

  @Override
  public Application findById(String realm, String id, String storage) {
    return storeProvider.getStoreForUserStorage(realm, storage).getReader().getApplication(id);
  }

  @Override
  public Application findById(String realm, String id) {
    return findById(realm, id, realmProvider.load(realm).getDefaultUserStorageName());
  }

  @Override
  public PageResult<Application> findByProperties(
      String realm, Map<String, String> properties, String storageName) {
    return storeProvider
        .getStoreForUserStorage(realm, storageName)
        .getReader()
        .searchApplications(properties, new PageableResult(), "AND");
  }

  @Override
  public PageResult<Application> findByProperties(String realm, Map<String, String> properties) {
    return findByProperties(
        realm, properties, realmProvider.load(realm).getDefaultUserStorageName());
  }

  @Override
  public PageResult<Application> findAll(String realm, String storageName) {
    return storeProvider
        .getStoreForUserStorage(realm, storageName)
        .getReader()
        .searchApplications(new HashMap<>(), new PageableResult(), "AND");
  }

  @Override
  public PageResult<Application> findAll(String realm) {
    return findAll(realm, realmProvider.load(realm).getDefaultUserStorageName());
  }
}
