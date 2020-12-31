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
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.model.User;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  @Autowired private StoreProvider storeProvider;

  @Autowired private RealmProvider realmProvider;

  @Override
  public User create(String realm, User user, String storage) {
    return storeProvider.getStoreForUserStorage(realm, storage).getWriter().createUser(user);
  }

  @Override
  public User create(String realm, User user) {
    return create(realm, user, realmProvider.load(realm).getDefaultUserStorageName());
  }

  @Override
  public void update(String realm, User user, String storage) {
    storeProvider.getStoreForUserStorage(realm, storage).getWriter().updateUser(user);
  }

  @Override
  public void update(String realm, User user) {
    update(realm, user, realmProvider.load(realm).getDefaultUserStorageName());
  }

  @Override
  public void delete(String realmName, String id, String storage) {
    storeProvider.getStoreForUserStorage(realmName, storage).getWriter().deleteUser(id);
  }

  @Override
  public void delete(String realmName, String id) {
    delete(realmName, id, realmProvider.load(realmName).getDefaultUserStorageName());
  }

  @Override
  public User findById(String realmName, String id, String storage) {
    try {
      return storeProvider.getStoreForUserStorage(realmName, storage).getReader().getUser(id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public User findById(String realmName, String id) {
    return findById(realmName, id, realmProvider.load(realmName).getDefaultUserStorageName());
  }

  @Override
  public PageResult<User> findByProperties(
      String realm, Map<String, String> properties, PageableResult pageable, String storage) {
    try {
      return storeProvider
          .getStoreForUserStorage(realm, storage)
          .getReader()
          .searchUsers(properties, pageable, "");
    } catch (Exception e) {
      throw new RuntimeException("Erreur lors de la récupération des utilisateurs");
    }
  }

  @Override
  public PageResult<User> findByProperties(
      String realm, Map<String, String> properties, PageableResult pageable) {
    return findByProperties(
        realm, properties, pageable, realmProvider.load(realm).getDefaultUserStorageName());
  }

  @Override
  public PageResult<User> findAll(String realm, String storage) {
    return storeProvider
        .getStoreForUserStorage(realm, storage)
        .getReader()
        .searchUsers(new HashMap<>(), new PageableResult(), "");
  }

  @Override
  public PageResult<User> findAll(String realm) {
    return findAll(realm, realmProvider.load(realm).getDefaultUserStorageName());
  }
}
